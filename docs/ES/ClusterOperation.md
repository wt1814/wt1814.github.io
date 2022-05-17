
<!-- TOC -->

- [1. ES集群操作](#1-es集群操作)
    - [1.1. 集群健康状态检查](#11-集群健康状态检查)
    - [1.2. 集群重启](#12-集群重启)
    - [1.3. 集群扩容](#13-集群扩容)

<!-- /TOC -->


# 1. ES集群操作
<!-- 
Elasticsearch 跨网络、跨集群同步选型指南 
https://mp.weixin.qq.com/s/1yXNrSdyk_cR8V7wqJarRw

亿级日增量的ES线上环境集群部署，上干货！ 
https://mp.weixin.qq.com/s/8PjfMqZGDkOk_hv4iIaqNg


Elasticsearch技术解析与实战 第6章集群管理  

-->

## 1.1. 集群健康状态检查  
&emsp; 要检查集群运行状况，可以在Kibana控制台中运行以下命令 GET /_cluster/health，得到如下信息：  

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

&emsp; <font color = "red">Elasticsearch集群存在三种健康状态</font>(单节点Elasticsearch也可以算是一个集群)。  

* 绿色：集群健康完好，一切功能齐全正常，所有分片和副本都可以正常工作。  
* 黄色：预警状态，所有主分片功能正常，但至少有一个副本是不能正常工作的。此时集群是可以正常工作的，但是高可用性在某种程度上会受影响。  
* **红色：集群不可正常使用。某个或某些分片及其副本异常不可用，这时集群的查询操作还能执行，但是返回的结果会不准确。对于分配到这个分片的写入请求将会报错，最终会导致数据的丢失。**  

&emsp; <font color = "red">当集群状态为红色时，它将会继续从可用的分片提供搜索请求服务，但是需要尽快修复那些未分配的分片。</font>  

## 1.2. 集群重启
&emsp; 如果Elasticsearch集群做了一些离线的维护操作时，如扩容磁盘，升级版本等，需要对集群进行启动，节点数较多时，从第一个节点开始启动，到最后一个节点启动完成，耗时可能较长，有时候还可能出现某几个节点因故障无法启动，排查问题、修复故障后才能加入到集群中，此时集群会干什么呢？    

&emsp; 假设10个节点的集群，每个节点有1个shard，升级后重启节点，结果有3台节点因故障未能启动，需要耗费时间排查故障，如下图所示：  
![image](http://www.wt1814.com/static/view/images/ES/es-9.png)    

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


## 1.3. 集群扩容  
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
&emsp; (1)每台节点的Shard数量越少，每个shard分配的CPU、内存和IO资源越多，单个Shard的性能越好，当一台机器一个Shard时，单个Shard性能最好。  
&emsp; (2)稳定的Master节点对于群集健康非常重要！理论上讲，应该尽可能的减轻Master节点的压力，分片数量越多，Master节点维护管理shard的任务越重，并且节点可能就要承担更多的数据转发任务，可增加“仅协调”节点来缓解Master节点和Data节点的压力，但是在集群中添加过多的仅协调节点会增加整个集群的负担，因为选择的主节点必须等待每个节点的集群状态更新确认。  
&emsp; (3)反过来说，如果相同资源分配相同的前提下，shard数量越少，单个shard的体积越大，查询性能越低，速度越慢，这个取舍应根据实际集群状况和结合应用场景等因素综合考虑。  
&emsp; (4)数据节点和Master节点一定要分开，集群规模越大，这样做的意义也就越大。  
&emsp; (5)数据节点处理与数据相关的操作，例如CRUD，搜索和聚合。这些操作是I / O，内存和CPU密集型的，所以他们需要更高配置的服务器以及更高的带宽，并且集群的性能冗余非常重要。  
&emsp; (6)由于仅投票节不参与Master竞选，所以和真正的Master节点相比，它需要的内存和CPU较少。但是，所有候选节点以及仅投票节点都可能是数据节点，所以他们都需要快速稳定低延迟的网络。  
&emsp; (7)高可用性(HA)群集至少需要三个主节点，其中至少两个不是仅投票节点。即使其中一个节点发生故障，这样的群集也将能够选举一个主节点。生产环境最好设置3台仅Master候选节点(node.master = true	 node.data = true)。  
&emsp; (8)为确保群集仍然可用，集群不能同时停止投票配置中的一半或更多节点。只要有一半以上的投票节点可用，群集仍可以正常工作。这意味着，如果存在三个或四个主节点合格的节点，则群集可以容忍其中一个节点不可用。如果有两个或更少的主机资格节点，则它们必须都保持可用。  
-->
