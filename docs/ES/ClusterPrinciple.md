
<!-- TOC -->

- [1. ~~ES集群运行原理~~](#1-es集群运行原理)
    - [1.1. ~~zen discovery集群发现机制~~](#11-zen-discovery集群发现机制)
        - [1.1.1. 集群配置](#111-集群配置)
        - [1.1.2. 集群发现的一般步骤](#112-集群发现的一般步骤)
    - [1.2. 集群容错](#12-集群容错)
        - [1.2.1. 集群容错整体流程](#121-集群容错整体流程)
        - [1.1.3. ~~Master选举~~](#113-master选举)
        - [1.2.2. ★★★split-brain(脑分裂问题)](#122-★★★split-brain脑分裂问题)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 集群容错整体流程：Master选举(假如宕机节点是Master)(可能产生脑分裂) ---> 分片容错 ---> Master重启故障机 ---> 数据同步。 
2. 脑分裂： **<font color = "clime">正确设置最少投票通过数量(discovery.zen.minimum_master_nodes = 候选节点/+1)参数，即半数机制，可以避免脑分裂问题。</font>**  

# 1. ~~ES集群运行原理~~
## 1.1. ~~zen discovery集群发现机制~~  
&emsp; **<font color = "red">ES的discovery机制：集群中各个节点互相发现然后组成一个集群的机制，同时discovery机制也负责es集群的master选举。</font>**    

### 1.1.1. 集群配置  
&emsp; Zen Discovery是Elasticsearch集群发现机制的默认实现，底层通信依赖transport组件，完成Elasticsearch集群的配置主要有下面几个参数：  

* cluster.name 指定集群的名称。  
* node.name 节点名称。  
* network.host 节点绑定的IP。  
* node.master 可选值为true/false，决定该节点类型为master eligible或data node。  
* discovery.zen.ping.unicast.hosts gossip路由服务的IP地址，即集群发现协议通信的公共节点，可以写多个，有节点启动时会向里面的IP发送消息，获取集群其他节点的信息，最后加入集群。  

&emsp; Elasticsearch集群是点对点(P2P)的分布式系统架构，数据索引、搜索操作是node之间直接通信的，没有中心式的master节点，但Elasticsearch集群内的节点也分成master node和data node两种角色。  

&emsp; 正常情况下，Elasticsearch集群只有一个master节点，它负责维护整个集群的状态信息，集群的元数据信息，有新的node加入或集群内node宕机下线时，重新分配shard，并同步node的状态信息给所有的node节点，这样所有的node节点都有一份完整的cluster state信息。  

### 1.1.2. 集群发现的一般步骤

1. 节点配置文件network.host绑定内网地址，配置各自的node.name信息，cluster.name设置为相同的值。  
2. discovery.zen.ping.unicast.hosts配置了几个gossip路由的node。  
3. 所有node都可以发送ping消息到路由node，再从路由node获取cluster state回来。  
4. 所有node执行master选举。  
5. 所有node都会跟master进行通信，然后加入master的集群。  


## 1.2. 集群容错  

### 1.2.1. 集群容错整体流程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-76.png)  
&emsp; ①第一步：Master选举(假如宕机节点是Master)  
&emsp; &emsp; 1)脑裂：可能会产生多个Master节点  
&emsp; &emsp; 2)解决：discovery.zen.minimum_master_nodes=N/2+1  
&emsp; ②第二步：Replica容错，新的(或者原有)Master节点会将丢失的Primary对应的某个副本提升为Primary  
&emsp; ③第三步：Master节点会尝试重启故障机  
&emsp; ④第四步：数据同步，Master会将宕机期间丢失的数据同步到重启机器对应的分片上去  

&emsp; 如果宕机节点不是Master，将省去Master选举的步骤。  

### 1.1.3. ~~Master选举~~
&emsp; 配置文件elasticsearch.yml中node.master设置为true的，将成为master eligible node，也叫master候选节点，只有master eligible node才能被选举成master node。如果是个小集群，那么所有节点都可以是master eligible node，10个节点以上的集群，可以考虑拆分master node和data node，一般建议master eligible node给3个即可。  

&emsp; master选举过程是自动完成的，有几个参数可以影响选举的过程：  

* discovery.zen.ping_timeout：选举超时时间，默认3秒，网络状况不好时可以增加超时时间。
discovery.zen.join_timeout：有新的node加入集群时，会发送一个join request到master node，同样因为网络原因可以调大，如果一次超时，默认最多重试20次。  
* discovery.zen.master_election.ignore_non_master_pings：如果master node意外宕机了，集群进行重新选举，如果此值为true，那么只有master eligible node才有资格被选为master。  
* discovery.zen.minimum_master_nodes：新选举master时，要求必须有多少个master eligible node去连接那个新选举的master。而且还用于设置一个集群中必须拥有的master eligible node。如果这些要求没有被满足，那么master node就会被停止，然后会重新选举一个新的master。这个参数必须设置为master eligible node的quorum数量。一般避免说只有两个master eligible node，因为2的quorum还是2。如果在那个情况下，任何一个master候选节点宕机了，集群就无法正常运作了。 

### 1.2.2. ★★★split-brain(脑分裂问题)  
<!-- 
Elasticsearch 中的节点(比如共 20 个)，其中的 10 个 选了一个master，另外 10 个选了另一个master，怎么办？  
1、当集群 master 候选数量不小于 3 个时， 可以通过设置最少投票通过数量 ( discovery.zen.minimum_master_nodes) 超过所有候选节点一半以上来解决脑裂问题；  
2、当候选数量为两个时， 只能修改为唯一的一个 master 候选， 其他作为 data 节 点， 避免脑裂问题。  
-->
&emsp; 在Elasticsearch集群中，master node非常重要，并且只有一个，相当于整个集群的大脑，控制将整个集群状态的更新，如果Elasticsearch集群节点之间出现区域性的网络中断，比如10个节点的Elasticsearch集群，4台node部署在机房A区，6台node部署在机房B区，如果A区与B区的交换机故障，导致两个区隔离开来了，那么没有master node的那个区，会触发master选举，如果选举了新的master，那么 **<font color = "red">整个集群会出现两个master node，这种现象叫做脑分裂。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-6.png)  
&emsp; 这样现象很严重，会破坏集群的数据，该如何避免呢？  
&emsp; **<font color = "clime">正确设置最少投票通过数量(discovery.zen.minimum_master_nodes)参数，可以避免脑分裂问题。</font>**  
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

