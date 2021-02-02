

<!-- TOC -->

- [1. ~~ES集群~~](#1-es集群)
    - [1.1. ES集群基本概念](#11-es集群基本概念)
        - [1.1.1. 节点(Node)](#111-节点node)
        - [1.1.2. ~~集群(cluster)~~](#112-集群cluster)
        - [1.1.3. 分片(Shard)](#113-分片shard)
        - [1.1.4. 副本(Replica)](#114-副本replica)
        - [1.1.5. 路由(routing)](#115-路由routing)
    - [1.2. ~~zen discovery集群发现机制~~](#12-zen-discovery集群发现机制)
        - [1.2.1. 集群配置](#121-集群配置)
        - [1.2.2. 集群发现的一般步骤](#122-集群发现的一般步骤)
        - [1.2.3. master选举](#123-master选举)
    - [1.3. 集群健康状态](#13-集群健康状态)
    - [1.4. 集群容错](#14-集群容错)
        - [1.4.1. 集群容错流程](#141-集群容错流程)
        - [1.4.2. ※※※split-brain(脑分裂问题)](#142-※※※split-brain脑分裂问题)
    - [1.5. 集群重启](#15-集群重启)
    - [1.6. 集群扩容](#16-集群扩容)

<!-- /TOC -->


# 1. ~~ES集群~~  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-21.png)  

## 1.1. ES集群基本概念
&emsp; 在数据库系统中，为了保证高可用性，可能会做主从复制、分库分表等操作。在ES中也有相同的基本操作。ES集群架构如下：  
....

### 1.1.1. 节点(Node)  
&emsp; 运行了单个实例的ES主机称为节点，它是集群的一个成员，可以存储数据、参与集群索引及搜索操作。节点通过为其配置的ES集群名称确定其所要加入的集群。  
<!--
5.节点(node)
一个节点是一个逻辑上独立的服务，它是集群的一部分，可以存储数据，并参与集群 的索引和搜索功能。就像集群一样，节点也有唯一的名字，在启动的时候分配。如果你不 
想要默认名称，你可以定义任何你想要的节点名。这个名字在管理中很重要，在网络中 Elasticsearch集群通过节点名称进行管理和通信。一个节点可以被配置加入一个特定的集 群。默认情况下，每个节点会加入名为Elasticsearch的集群中，这意味着如果你在网络上启 动多个节点，如果网络畅通，他们能彼此发现并自动加入一个名为Elasticsearch的集群中 在一个集群中，你可以拥有多个你想要的节点。当网络没有集群运行的时候，只要启动任何 —个节点，这个节点会默认生成一个新的集群，这个集群会有一个节点。  
-->

### 1.1.2. ~~集群(cluster)~~  
&emsp; ES可以作为一个独立的单个搜索服务器。不过，一般为了处理大型数据集，实现容错和高可用性，ES可以运行在许多互相合作的服务器上。这些服务器的集合称为集群。  

&emsp; **<font color = "red">ES集群节点类型：</font>**  
&emsp; 集群由多个节点构成，每一台主机则称为一台节点，在伪集群中每一个ES实例则为一个节点。  

&emsp; **集群中节点分类：**  
* Master：主节点，每个集群都有且只有一个  
  * 尽量避免Master节点 node.data ＝ true
  * 主节点的主要职责是和集群操作相关的内容，如创建或删除索引，跟踪哪些节点是群集的一部分，并决定哪些分片分配给相关的节点。稳定的主节点对集群的健康是非常重要的，默认情况下任何一个集群中的节点都有可能被选为主节点，索引数据和搜索查询等操作会占用大量的cpu，内存，io资源，为了确保一个集群的稳定，分离主节点和数据节点是一个比较好的选择。
* Data node（数据节点）： 即 Data 节点。数据节点主要是存储索引数据的节点，主要对文档进行增删改查操作，聚合操作等。数据节点对 CPU、内存、IO 要求较高，在优化的时候需要监控数据节点的状态。
* voting：投票节点  
  * Node.voting_only = true（仅投票节点，即使配置了data.master = true，也不会参选, 但是仍然可以作为数据节点）。  
* coordinating：协调节点  
  * 每一个节点都隐式的是一个协调节点，如果同时设置了data.master = false和data.data=false，那么此节点将成为仅协调节点。
* Master-eligible node（候选节点）：	  
 

&emsp; **关于集群节点类型的两个配置：node.master和node.data**   

* node.master = true	 node.data = true  
&emsp; 这是ES节点默认配置，既作为候选节点又作为数据节点，这样的节点一旦被选举为Master，压力是比较大的，通常来说Master节点应该只承担较为轻量级的任务，比如创建删除索引，分片均衡等。  
* node.master = true	 node.data = false  
&emsp; 只作为候选节点，不作为数据节点，可参选Master节点，当选后成为真正的Master节点。  
* node.master = false	 node.data = false  
&emsp; 既不当候选节点，也不作为数据节点，那就是仅协调节点，负责负载均衡  
* node.master=false		node.data=true  
&emsp; 不作为候选节点，但是作为数据节点，这样的节点主要负责数据存储和查询服务。    

### 1.1.3. 分片(Shard)  
&emsp; **ES的“分片(shard)”机制可将一个索引内部的数据分布地存储于多个节点，** 它通过将一个索引切分为多个底层物理的Lucene索引完成索引数据的分割存储功能，这每一个物理的Lucene索引称为一个分片(shard)。  
&emsp; 这样的好处是可以把一个大的索引拆分成多个，分布到不同的节点上。降低单服务器的压力，构成分布式搜索，提高整体检索的效率（分片数的最优值与硬件参数和数据量大小有关）。分片的数量只能在索引创建前指定，并且索引创建后不能更改。  
<!-- 
7.分片(shard)
分片是单个Lucene实例，这是Elasticsearch管理的比较底层的功能。索引是指向主 分片和副本分片的逻辑空间。对于使用，只需要指定分片的数量，其他不需要做过多的 事情。在开发使用的过程中，我们对应的对象都是索引，Elasticsearch会自动管理集群中 所有的分片，当发生故障的时候，Elasticsearch会把分片移动到不同的节点或者添加新的 T＞点。
一个索引可以存储很大的数据，这些空间可以超过一个节点的物理存储的限制。例如， 十亿个文档占用磁盘空间为1TB。仅从单个节点搜索可能会很慢，还有一台物理机器也不 一定能存储这么多的数据。为了解决这一问题，Elasticsearch将索引分解成多个分片：当你 创建一个索引，你可以简单地定义你想要的分片数量。每个分片本身是一个全功能的、独立 的单元，可以托管在集群中的任何节点。
-->

### 1.1.4. 副本(Replica)  
<!--
10,复制(replica)
复制是一个非常有用的功能，不然会有单点问题。当网络中的某个节点出现问题的时 候，复制可以对故障进行转移，保证系统的高可用。因此，Elasticsearch允许你创建一个或 多个拷贝，你的索引分片就形成了所谓的副本或副本分片。  
复制是重要的，主要的原因有：  

* 它提供了高可用性，当节点失败的时候不受影响。需要注意的是，一个复制的分片 不会存储在同一个节点中。  
* 它允许你扩展搜索量，提高并发量，因为搜索可以在所有副本上并行执行。  

每个索引可以拆分成多个分片。索引可以复制零个或者多个分片。一旦复制，每个索引 就有了主分片和副本分片。分片的数量和副本的数量可以在创建索引时定义。当创建索引 后，你可以随时改变副本的数量，但你不能改变分片的数量。  
默认情况下，每个索引分配5个分片和一个副本，这意味着你的集群节点至少要有两个 节点，你将拥有5个主要的分片和5个副本分片共计10个分片。  


8,主分片(primary shard )
每个文档都存储在一个分片中，当你存储一个文档的时候，系统会首先存储在主分片 中，然后会复制到不同的副本中。默认情况下，一个索引有5个主分片。你可以事先制定分 片的数量，当分片一旦建立，则分片的数量不能修改。
9.副本分片(replica shard )
每一个分片有零个或多个副本。副本主要是主分片的复制，其中有两个目的： 口增加高可用性：当主分片失败的时候，可以从副本分片中选择一个作为主分片， 口提高性能：当查询的时候可以到主分片或者副本分片中进行查询。默认情况下，一 个主分片配有一个副本，但副本的数量可以在后面动态地配置增加。副本分片必须 部署在不同的节点上，不能部署在和主分片相同的节点上。
分片主要有两个很重要的原因是：  

	* 允许水平分割扩展数据。  
	* 允许分配和并行操作(可能在多个节点上)从而提高性能和吞吐量。  

这些很强大的功能对用户来说是透明的，你不需要做什么操作，系统会自动处理。  
-->
&emsp; 副本是一个分片的精确复制，每个分片可以有零个或多个副本。<font color = "red">副本的作用：一是提高系统的容错性，当某个节点某个分片损坏或丢失时可以从副本中恢复；二是提高es的查询效率，es会自动对搜索请求进行负载均衡。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-75.png)  

### 1.1.5. 路由(routing)  
&emsp; 当存储一个文档的时候，它会存储在唯一的主分片中，具体哪个分片是通过散列值进行选择：默认情况下，这个值是由文档的ID生成。如果文档有一个指定的父文档，则从父文档ID中生成，该值可以在存储文档的时候进行修改。   

## 1.2. ~~zen discovery集群发现机制~~  
&emsp; **<font color = "red">es的discovery机制：集群中各个节点互相发现然后组成一个集群的机制，同时discovery机制也负责es集群的master选举。</font>**    

### 1.2.1. 集群配置  
&emsp; Zen Discovery是Elasticsearch集群发现机制的默认实现，底层通信依赖transport组件，完成Elasticsearch集群的配置主要有下面几个参数：  

* cluster.name 指定集群的名称。  
* node.name 节点名称。  
* network.host 节点绑定的IP。  
* node.master 可选值为true/false，决定该节点类型为master eligible或data node。  
* discovery.zen.ping.unicast.hosts gossip路由服务的IP地址，即集群发现协议通信的公共节点，可以写多个，有节点启动时会向里面的IP发送消息，获取集群其他节点的信息，最后加入集群。  

&emsp; Elasticsearch集群是点对点(P2P)的分布式系统架构，数据索引、搜索操作是node之间直接通信的，没有中心式的master节点，但Elasticsearch集群内的节点也分成master node和data node两种角色。  

&emsp; 正常情况下，Elasticsearch集群只有一个master节点，它负责维护整个集群的状态信息，集群的元数据信息，有新的node加入或集群内node宕机下线时，重新分配shard，并同步node的状态信息给所有的node节点，这样所有的node节点都有一份完整的cluster state信息。  

### 1.2.2. 集群发现的一般步骤

1. 节点配置文件network.host绑定内网地址，配置各自的node.name信息，cluster.name设置为相同的值。  
2. discovery.zen.ping.unicast.hosts配置了几个gossip路由的node。  
3. 所有node都可以发送ping消息到路由node，再从路由node获取cluster state回来。  
4. 所有node执行master选举。  
5. 所有node都会跟master进行通信，然后加入master的集群。  

### 1.2.3. master选举
&emsp; 配置文件elasticsearch.yml中node.master设置为true的，将成为master eligible node，也叫master候选节点，只有master eligible node才能被选举成master node。如果是个小集群，那么所有节点都可以是master eligible node，10个节点以上的集群，可以考虑拆分master node和data node，一般建议master eligible node给3个即可。  

&emsp; master选举过程是自动完成的，有几个参数可以影响选举的过程：  

* discovery.zen.ping_timeout：选举超时时间，默认3秒，网络状况不好时可以增加超时时间。
discovery.zen.join_timeout：有新的node加入集群时，会发送一个join request到master node，同样因为网络原因可以调大，如果一次超时，默认最多重试20次。  
* discovery.zen.master_election.ignore_non_master_pings：如果master node意外宕机了，集群进行重新选举，如果此值为true，那么只有master eligible node才有资格被选为master。  
* discovery.zen.minimum_master_nodes：新选举master时，要求必须有多少个master eligible node去连接那个新选举的master。而且还用于设置一个集群中必须拥有的master eligible node。如果这些要求没有被满足，那么master node就会被停止，然后会重新选举一个新的master。这个参数必须设置为master eligible node的quorum数量。一般避免说只有两个master eligible node，因为2的quorum还是2。如果在那个情况下，任何一个master候选节点宕机了，集群就无法正常运作了。  

## 1.3. 集群健康状态  
&emsp; 要检查群集运行状况，可以在 Kibana 控制台中运行以下命令 GET /_cluster/health，得到如下信息：  

```json
{
  "cluster_name" : "xxxBug",
  "status" : "yellow",
  "timed_out" : false,
  "number_of_nodes" : 1,
  "number_of_data_nodes" : 1,
  "active_primary_shards" : 9,
  "active_shards" : 9,
  "relocating_shards" : 0,
  "initializing_shards" : 0,
  "unassigned_shards" : 5,
  "delayed_unassigned_shards" : 0,
  "number_of_pending_tasks" : 0,
  "number_of_in_flight_fetch" : 0,
  "task_max_waiting_in_queue_millis" : 0,
  "active_shards_percent_as_number" : 64.28571428571429
}
```

&emsp; <font color = "red">Elasticsearch集群存在三种健康状态</font>(单节点 Elasticsearch 也可以算是一个集群)。  

* 绿色：集群健康完好，一切功能齐全正常，所有分片和副本都可以正常工作。  
* 黄色：预警状态，所有主分片功能正常，但至少有一个副本是不能正常工作的。此时集群是可以正常工作的，但是高可用性在某种程度上会受影响。  
* **红色：集群不可正常使用。某个或某些分片及其副本异常不可用，这时集群的查询操作还能执行，但是返回的结果会不准确。对于分配到这个分片的写入请求将会报错，最终会导致数据的丢失。**  

&emsp; <font color = "red">当集群状态为红色时，它将会继续从可用的分片提供搜索请求服务，但是需要尽快修复那些未分配的分片。</font>  

## 1.4. 集群容错  

### 1.4.1. 集群容错流程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-76.png)  
&emsp; ①第一步：Master选举（假如宕机节点是Master）  
&emsp; &emsp; 1)脑裂：可能会产生多个Master节点  
&emsp; &emsp; 2)解决：discovery.zen.minimum_master_nodes=N/2+1  
&emsp; ②第二步：Replica容错，新的（或者原有）Master节点会将丢失的Primary对应的某个副本提升为Primary  
&emsp; ③第三步：Master节点会尝试重启故障机  
&emsp; ④第四步：数据同步，Master会将宕机期间丢失的数据同步到重启机器对应的分片上去  

### 1.4.2. ※※※split-brain(脑分裂问题)  
<!-- 
Elasticsearch 中的节点（比如共 20 个），其中的 10 个 选了一个master，另外 10 个选了另一个master，怎么办？  
1、当集群 master 候选数量不小于 3 个时， 可以通过设置最少投票通过数量 （ discovery.zen.minimum_master_nodes） 超过所有候选节点一半以上来解决脑裂问题；  
2、当候选数量为两个时， 只能修改为唯一的一个 master 候选， 其他作为 data 节 点， 避免脑裂问题。  
-->
&emsp; 在Elasticsearch集群中，master node非常重要，并且只有一个，相当于整个集群的大脑，控制将整个集群状态的更新，如果Elasticsearch集群节点之间出现区域性的网络中断，比如10个节点的Elasticsearch集群，4台node部署在机房A区，6台node部署在机房B区，如果A区与B区的交换机故障，导致两个区隔离开来了，那么没有master node的那个区，会触发master选举，如果选举了新的master，那么 **<font color = "red">整个集群会出现两个master node，这种现象叫做脑分裂。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-6.png)  
&emsp; 这样现象很严重，会破坏集群的数据，该如何避免呢？  
&emsp; **<font color = "lime">正确设置最少投票通过数量(discovery.zen.minimum_master_nodes)参数，可以避免脑分裂问题。</font>**  
&emsp; discovery.zen.minimum_master_nodes参数表示至少需要多少个master eligible node，才可以成功地选举出master，否则不进行选举。  

&emsp; 足够的master eligible node计算公式：quorum = master_eligible_nodes / 2 + 1  

&emsp; 如上图10个node的集群，如果全部是master eligible node，那么quorum = 10/2 + 1 = 6。  
&emsp; 如果有3个master eligible node，7个data node，那么quorum = 3/2 + 1 = 2。  
&emsp; 如果集群只有2个节点，并且全是master eligible node，那么quorum = 2/2 + 1 = 2，问题就来了，如果随便一个node宕机，在只剩下一个node情况下，无法满足quorum的值，master永远选举不成功，集群就彻底无法写入了，所以只能设置成1，后果是只要这两个node之间网络断了，就会发生脑分裂的现象。  
&emsp; 所以一个Elasticsearch集群至少得有3个node，全部为master eligible node的话，quorum = 3/2 + 1 = 2。如果设置minimum_master_nodes=2，分析一下会不会出现脑分裂的问题。  

&emsp; 场景一：A区一个node，为master，B区两个node，为master eligible node  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-7.png)  
&emsp; A区因为只剩下一个node，无法满足quorum的条件，此时master取消当前的master角色，且无法选举成功。  
&emsp; B区两个master eligible node，满足quorum条件，成功选举出master。  
&emsp; 此时集群还是只有一个master，待网络故障恢复后，集群数据正常。  

&emsp; 场景二：A区一个node，为master eligible node，B区2个node，其中一个是master  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-8.png)    
&emsp; A区只有一个master eligible node，不满足quorum的条件，无法进行选举。  
&emsp; B区原本的master存在，不需要进行选举，并且满quorum的条件，master角色可以保留。  
&emsp; 此时集群还是一个master，正常。  

&emsp; 综上所述：3个节点的集群，全部为master eligible node，配置discovery.zen.minimum_master_nodes: 2，就可以避免脑裂问题的产生。  

## 1.5. 集群重启
&emsp; 如果Elasticsearch集群做了一些离线的维护操作时，如扩容磁盘，升级版本等，需要对集群进行启动，节点数较多时，从第一个节点开始启动，到最后一个节点启动完成，耗时可能较长，有时候还可能出现某几个节点因故障无法启动，排查问题、修复故障后才能加入到集群中，此时集群会干什么呢？    

&emsp; 假设10个节点的集群，每个节点有1个shard，升级后重启节点，结果有3台节点因故障未能启动，需要耗费时间排查故障，如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-9.png)    

&emsp; 整个过程步骤如下：  
1. 集群已完成master选举(node6)，master发现未加入集群的node1、node2、node3包含的shard丢失，便立即发出shard恢复的指令。
2. 在线的7台node，将其中一个replica shard升级为primary shard，并且进行为这些primary shard复制足够的replica shard。
3. 执行shard rebalance操作。
4. 故障的3台节点已排除，启动成功后加入集群。
5. 这3台节点发现自己的shard已经在集群中的其他节点上了，便删除本地的shard数据。
6. master发现新的3台node没有shard数据，重新执行一次shard rebalance操作。

&emsp; 这个过程可以发现，多做了四次IO操作，shard复制，shard首次移动，shard本地删除，shard再次移动，这样凭空造成大量的IO压力，如果数据量是TB级别的，那费时费力不讨好。  

&emsp; 出现此类问题的原因是节点启动的间隔时间不能确定，并且节点越多，这个问题越容易出现，如果可以设置集群等待多少个节点启动后，再决定是否对shard进行移动，这样IO压力就能小很多。  

&emsp; 针对这个问题，有下面几个参数：  

* gateway.recover_after_nodes：集群必须要有多少个节点时，才开始做shard恢复操作。
* gateway.expected_nodes: 集群应该有多少个节点
* gateway.recover_after_time: 集群启动后等待的shard恢复时间

&emsp; 如上面的案例，可以这样设置：  

    gateway.recover_after_nodes: 8
    gateway.expected_nodes: 10
    gateway.recover_after_time: 5m

&emsp; 这三个参数的含义：集群总共有10个节点，必须要有8个节点加入集群时，才允许执行shard恢复操作，如果10个节点未全部启动成功，最长的等待时间为5分钟。  
&emsp; 这几个参数的值可以根据实际的集群规模来设置，并且只能在elasticsearch.yml文件里设置，没有动态修改的入口。  
&emsp; 上面的参数设置合理的情况，集群启动是没有shard移动的现象，这样集群启动的时候就可以由之前的几小时，变成几秒钟。  


## 1.6. 集群扩容  
<!-- 
elasticsearch集群扩容和容灾
https://www.cnblogs.com/hello-shf/p/11543468.html
-->
&emsp; 因为集群是可以动态增加和下线节点的，quorum的值也会跟着改变。minimum_master_nodes参数值需要通过api随时修改的，特别是在节点上线和下线的时候，都需要作出对应的修改。而且一旦修改过后，这个配置就会持久化保存下来。  

&emsp; 修改api请求如下：  

```
PUT /_cluster/settings
{
    "persistent" : {
        "discovery.zen.minimum_master_nodes" : 2
    }
}
```

&emsp; 响应报文：  

```
{
  "acknowledged": true,
  "persistent": {
    "discovery": {
      "zen": {
        "minimum_master_nodes": "2"
      }
    }
  },
  "transient": {}
}
```

&emsp; 也可以通过命令查询当前的配置：  

```
GET /_cluster/settings
```

&emsp; 响应结果如下：  

```
{
  "persistent": {
    "discovery": {
      "zen": {
        "minimum_master_nodes": "1"
      }
    }
  },
  "transient": {}
}
```


<!-- 
小结：提高ES分布式系统的可用性以及性能最大化  
&emsp; （1）每台节点的Shard数量越少，每个shard分配的CPU、内存和IO资源越多，单个Shard的性能越好，当一台机器一个Shard时，单个Shard性能最好。  
&emsp; （2）稳定的Master节点对于群集健康非常重要！理论上讲，应该尽可能的减轻Master节点的压力，分片数量越多，Master节点维护管理shard的任务越重，并且节点可能就要承担更多的数据转发任务，可增加“仅协调”节点来缓解Master节点和Data节点的压力，但是在集群中添加过多的仅协调节点会增加整个集群的负担，因为选择的主节点必须等待每个节点的集群状态更新确认。  
&emsp; （3）反过来说，如果相同资源分配相同的前提下，shard数量越少，单个shard的体积越大，查询性能越低，速度越慢，这个取舍应根据实际集群状况和结合应用场景等因素综合考虑。  
&emsp; （4）数据节点和Master节点一定要分开，集群规模越大，这样做的意义也就越大。  
&emsp; （5）数据节点处理与数据相关的操作，例如CRUD，搜索和聚合。这些操作是I / O，内存和CPU密集型的，所以他们需要更高配置的服务器以及更高的带宽，并且集群的性能冗余非常重要。  
&emsp; （6）由于仅投票节不参与Master竞选，所以和真正的Master节点相比，它需要的内存和CPU较少。但是，所有候选节点以及仅投票节点都可能是数据节点，所以他们都需要快速稳定低延迟的网络。  
&emsp; （7）高可用性（HA）群集至少需要三个主节点，其中至少两个不是仅投票节点。即使其中一个节点发生故障，这样的群集也将能够选举一个主节点。生产环境最好设置3台仅Master候选节点（node.master = true	 node.data = true）。  
&emsp; （8）为确保群集仍然可用，集群不能同时停止投票配置中的一半或更多节点。只要有一半以上的投票节点可用，群集仍可以正常工作。这意味着，如果存在三个或四个主节点合格的节点，则群集可以容忍其中一个节点不可用。如果有两个或更少的主机资格节点，则它们必须都保持可用。  
-->
