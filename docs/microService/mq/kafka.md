<!-- TOC -->

- [1. kafka](#1-kafka)
    - [1.1. kafka介绍](#11-kafka介绍)
    - [1.2. kafka拓扑结构及相关概念](#12-kafka拓扑结构及相关概念)
        - [1.2.1. 生产者（Producer）与消费者（Consumer）](#121-生产者producer与消费者consumer)
        - [1.2.2. Broker 和集群（Cluster）](#122-broker-和集群cluster)
        - [1.2.3. 主题（Topic）与分区（Partition）](#123-主题topic与分区partition)
        - [1.2.4. offset](#124-offset)
        - [1.2.5. replica，副本机制，高可用性](#125-replica副本机制高可用性)
        - [1.2.6. leader和follower](#126-leader和follower)
        - [1.2.7. ISR](#127-isr)
        - [1.2.8. Zookeeper](#128-zookeeper)
    - [1.4. kafka使用场景](#14-kafka使用场景)

<!-- /TOC -->

# 1. kafka
&emsp; **kafka中文文档：** https://kafka.apachecn.org/  

## 1.1. kafka介绍  
&emsp; Apache Kafka是 一个分布式流处理平台。流处理平台有以下三种特性:

* 可以发布和订阅流式的记录。这一方面与消息队列或者企业消息系统类似。
* 可以储存流式的记录，并且有较好的容错性。
* 可以在流式记录产生时就进行处理。 

## 1.2. kafka拓扑结构及相关概念  
&emsp; 一个典型的Kafka包含若干Producer、Broker集群、若干 Consumer 以及一个 Zookeeper 集群。Kafka体系结构如下所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-3.png)  
<!-- 
https://segmentfault.com/a/1190000020512524?utm_source=tag-newest

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-1.png)  

Zookeeper 是 Kafka 用来负责集群元数据管理、控制器选举等操作的。Producer 是负责将消息发送到 Broker 的，Broker 负责将消息持久化到磁盘，而 Consumer 是负责从Broker 订阅并消费消息。

1. producer：  
&emsp; 消息生产者，发布消息到 kafka 集群的终端或服务。  
2. broker：  
&emsp; kafka 集群中包含的服务器。
3. topic：  
&emsp; 每条发布到 kafka 集群的消息属于的类别，即 kafka 是面向 topic 的。  
4. partition：  
&emsp; partition 是物理上的概念，每个 topic 包含一个或多个 partition。kafka 分配的单位是 partition。  
5. consumer：  
&emsp; 从 kafka 集群中消费消息的终端或服务。  
6. Consumer group：  
&emsp; high-level consumer API 中，每个 consumer 都属于一个 consumer group，每条消息只能被 consumer group 中的一个 Consumer 消费，但可以被多个 consumer group 消费。  
7. replica：  
&emsp; partition 的副本，保障 partition 的高可用。  
8. leader：  
&emsp; replica 中的一个角色， producer 和 consumer 只跟 leader 交互。  
9. follower：  
&emsp; replica 中的一个角色，从 leader 中复制数据。  
10. controller：  
&emsp; kafka 集群中的其中一个服务器，用来进行 leader election 以及 各种 failover。  
11. zookeeper：  
&emsp; kafka 通过 zookeeper 来存储集群的 meta 信息。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-4.png)  
* Broker 注册：Broker 是分布式部署并且之间相互独立，Zookeeper 用来管理注册到集群的所有 Broker 节点。
* Topic 注册：在 Kafka 中，同一个 Topic 的消息会被分成多个分区并将其分布在多个 Broker 上，这些分区信息及与 Broker 的对应关系也都是由 Zookeeper 在维护
* 生产者负载均衡：由于同一个 Topic 消息会被分区并将其分布在多个 Broker 上，因此，生产者需要将消息合理地发送到这些分布式的 Broker 上。
* 消费者负载均衡：与生产者类似，Kafka 中的消费者同样需要进行负载均衡来实现多个消费者合理地从对应的 Broker 服务器上接收消息，每个消费者分组包含若干消费者，每条消息都只会发送给分组中的一个消费者，不同的消费者分组消费自己特定的 Topic 下面的消息，互不干扰。


&emsp; kafka 在 zookeeper 中的存储结构如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-2.png)  
--> 
    
### 1.2.1. 生产者（Producer）与消费者（Consumer）  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-16.png)  
&emsp; 对于Kafka客户端有两种基本类型：生产者（Producer）和 消费者（Consumer）。除此之外，还有用来做数据集成的 Kafka Connect API 和流式处理的 Kafka Streams 等高阶客户端，但这些高阶客户端底层仍然是生产者和消费者API，只不过是在上层做了封装。  

* Producer ：消息生产者，向 Kafka broker 发消息的客户端；
* Consumer ：消息消费者，向 Kafka broker 取消息的客户端；

### 1.2.2. Broker 和集群（Cluster）  
&emsp; 一个 Kafka 服务器也称为 Broker，它接受生产者发送的消息并存入磁盘；Broker 同时接受服务消费者拉取分区消息的请求，返回目前已经提交的消息。使用特定的机器硬件，一个 Broker 每秒可以处理成千上万的分区和百万量级的消息。  
&emsp; 若干个 Broker 组成一个 集群（Cluster），其中集群内某个 Broker 会成为集群控制器（Cluster Controller），它负责管理集群，包括分配分区到 Broker、监控 Broker 故障等。在集群内，一个分区由一个 Broker 负责，这个 Broker 也称为这个分区的 Leader；当然一个分区可以被复制到多个 Broker 上来实现冗余，这样当存在 Broker 故障时可以将其分区重新分配到其他 Broker 来负责。下图是一个样例：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-17.png)  

### 1.2.3. 主题（Topic）与分区（Partition）
<!-- 
Kafka学习笔记（四）分区机制
https://blog.csdn.net/haogenmin/article/details/109449016

“消费组中的消费者个数如果超过topic的分区，那么就会有消费者消费不到数据”这句话是否正确？如果正确，那么有没有什么hack的手段？  
https://www.cnblogs.com/luozhiyun/p/11811835.html

创建topic时如何选择合适的分区数？ topic分区
https://www.cnblogs.com/luozhiyun/p/11811835.html



-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-18.png)  
&emsp; 在 Kafka 中，消息以 主题（Topic）来分类，每一个主题都对应一个「消息队列」，这有点儿类似于数据库中的表。但是如果我们把所有同类的消息都塞入到一个“中心”队列中，势必缺少可伸缩性，无论是生产者/消费者数目的增加，还是消息数量的增加，都可能耗尽系统的性能或存储。  

### 1.2.4. offset
<!-- 
https://www.cnblogs.com/luozhiyun/p/11811835.html

消费者提交消费位移时提交的是当前消费到的最新消息的offset还是offset+1?  
在旧消费者客户端中，消费位移是存储在 ZooKeeper 中的。而在新消费者客户端中，消费位移存储在 Kafka 内部的主题__consumer_offsets 中。  
当前消费者需要提交的消费位移是offset+1  

-->

### 1.2.5. replica，副本机制，高可用性  
<!-- 
副本机制
https://blog.csdn.net/haogenmin/article/details/109449944


深入理解Kafka必知必会
https://www.cnblogs.com/luozhiyun/p/12079527.html


Kafka中的ISR、AR又代表什么？ISR的伸缩又指什么#
分区中的所有副本统称为 AR（Assigned Replicas）。所有与 leader 副本保持一定程度同步的副本（包括 leader 副本在内）组成ISR（In-Sync Replicas），ISR 集合是 AR 集合中的一个子集。

ISR的伸缩：
leader 副本负责维护和跟踪 ISR 集合中所有 follower 副本的滞后状态，当 follower 副本落后太多或失效时，leader 副本会把它从 ISR 集合中剔除。如果 OSR 集合中有 follower 副本“追上”了 leader 副本，那么 leader 副本会把它从 OSR 集合转移至 ISR 集合。默认情况下，当 leader 副本发生故障时，只有在 ISR 集合中的副本才有资格被选举为新的 leader，而在 OSR 集合中的副本则没有任何机会（不过这个原则也可以通过修改相应的参数配置来改变）。

replica.lag.time.max.ms ： 这个参数的含义是 Follower 副本能够落后 Leader 副本的最长时间间隔，当前默认值是 10 秒。

unclean.leader.election.enable：是否允许 Unclean 领导者选举。开启 Unclean 领导者选举可能会造成数据丢失，但好处是，它使得分区 Leader 副本一直存在，不至于停止对外提供服务，因此提升了高可用性。


阐述下 Kafka 中的领导者副本(Leader Replica)和追随者副本 (Follower Replica)的区别
这道题表面上是考核你对 Leader 和 Follower 区别的理解，但很容易引申到 Kafka 的同步 机制上。因此，我建议你主动出击，一次性地把隐含的考点也答出来，也许能够暂时把面试 官“唬住”，并体现你的专业性。

你可以这么回答:Kafka 副本当前分为领导者副本和追随者副本。只有 Leader 副本才能 对外提供读写服务，响应 Clients 端的请求。Follower 副本只是采用拉(PULL)的方 式，被动地同步 Leader 副本中的数据，并且在 Leader 副本所在的 Broker 宕机后，随时 准备应聘 Leader 副本。

通常来说，回答到这个程度，其实才只说了 60%，因此，我建议你再回答两个额外的加分 项。

强调 Follower 副本也能对外提供读服务。自 Kafka 2.4 版本开始，社区通过引入新的 Broker 端参数，允许 Follower 副本有限度地提供读服务。
强调 Leader 和 Follower 的消息序列在实际场景中不一致。很多原因都可能造成 Leader 和 Follower 保存的消息序列不一致，比如程序 Bug、网络问题等。这是很严重 的错误，必须要完全规避。你可以补充下，之前确保一致性的主要手段是高水位机制， 但高水位值无法保证 Leader 连续变更场景下的数据一致性，因此，社区引入了 Leader Epoch 机制，来修复高水位值的弊端。关于“Leader Epoch 机制”，国内的资料不是 很多，它的普及度远不如高水位，不妨大胆地把这个概念秀出来，力求惊艳一把。

-->

### 1.2.6. leader和follower  


### 1.2.7. ISR  

什么是 AR，ISR？  

    AR：Assigned Replicas。AR 是主题被创建后，分区创建时被分配的副本集合，副本个 数由副本因子决定。ISR：In-Sync Replicas。Kafka 中特别重要的概念，指代的是 AR 中那些与 Leader 保 持同步的副本集合。在 AR 中的副本可能不在 ISR 中，但 Leader 副本天然就包含在 ISR 中。关于 ISR，还有一个常见的面试题目是如何判断副本是否应该属于 ISR。目前的判断 依据是：Follower 副本的 LEO 落后 Leader LEO 的时间，是否超过了 Broker 端参数 replica.lag.time.max.ms 值。如果超过了，副本就会被从 ISR 中移除。  

### 1.2.8. Zookeeper  
&emsp; kafka中Zookeeper作用：集群管理，元数据管理。    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-4.png)  
* Broker 注册：Broker 是分布式部署并且之间相互独立，Zookeeper 用来管理注册到集群的所有 Broker 节点。
* Topic 注册：在 Kafka 中，同一个 Topic 的消息会被分成多个分区并将其分布在多个 Broker 上，这些分区信息及与 Broker 的对应关系也都是由 Zookeeper 在维护
* 生产者负载均衡：由于同一个 Topic 消息会被分区并将其分布在多个 Broker 上，因此，生产者需要将消息合理地发送到这些分布式的 Broker 上。
* 消费者负载均衡：与生产者类似，Kafka 中的消费者同样需要进行负载均衡来实现多个消费者合理地从对应的 Broker 服务器上接收消息，每个消费者分组包含若干消费者，每条消息都只会发送给分组中的一个消费者，不同的消费者分组消费自己特定的 Topic 下面的消息，互不干扰。

---
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-21.png)  
&emsp; 注意：producer 不在 zk 中注册，消费者在 zk 中注册。


## 1.4. kafka使用场景  
&emsp; **kafka常用使用场景：**  

* 日志收集：一个公司可以用Kafka可以收集各种服务的log，通过kafka以统一接口服务的方式开放给各种consumer；  
* 消息系统：解耦生产者和消费者、缓存消息等；  
* 用户活动跟踪：kafka经常被用来记录web用户或者app用户的各种活动，如浏览网页、搜索、点击等活动，这些活动信息被各个服务器发布到kafka的topic中，然后消费者通过订阅这些topic来做实时的监控分析，亦可保存到数据库；  
* 运营指标：kafka也经常用来记录运营监控数据。包括收集各种分布式应用的数据，生产各种操作的集中反馈，比如报警和报告；  
* 流式处理：比如spark streaming和storm。  


<!-- 

全网最通俗易懂的 Kafka 入门
https://mp.weixin.qq.com/s?__biz=Mzg2MjEwMjI1Mg==&mid=2247490770&idx=2&sn=1008bcdaed680ed1413e2ead6320bec0&chksm=ce0dab51f97a224771a468245ed4f99f338a51a97505f2e78790cc8a6360f4f22c5e4f07cca2&mpshare=1&scene=1&srcid=&sharer_sharetime=1575464964037&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=e2a6a5ccea4b8ce41e290743b191d123754ef664941f31b9abdbdf28c289f875664f750548bc9da8bbbbabbeaa6a6d5fbb9efc00d2f33e693de36420dd87f9348fb89d058eb4d5ccbcfd806790431b8e&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=AQTbt4i1KPDzS6vieYS4x5I%3D&pass_ticket=UIzvXMBOSWKDgIz4M7cQoxQ548Mbvo9Oik9jB6kaYK60loRzg3FsHZUpAHYbC4%2By

-->

<!-- 
草捏子
Kafka中副本机制的设计和原理 
https://mp.weixin.qq.com/s/yIPIABpAzaHJvGoJ6pv0kg
Kafka 消费者的使用和原理 
https://mp.weixin.qq.com/s/cmDRWi2tmw0reHoUf5UriQs
Kafka中的再均衡 
https://mp.weixin.qq.com/s/UiSpj3WctvdcdXXAwjcI-Q

-->
