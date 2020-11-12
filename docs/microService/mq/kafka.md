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
    - [1.3. Kafka 工作流程分析](#13-kafka-工作流程分析)
        - [1.3.1. kafka生产过程](#131-kafka生产过程)
        - [1.3.2. Broker保存消息](#132-broker保存消息)
            - [1.3.2.1. 存储策略](#1321-存储策略)
            - [1.3.2.2. 存储方式](#1322-存储方式)
            - [1.3.2.3. 存储策略](#1323-存储策略)
        - [1.3.3. Kafka消费过程分析](#133-kafka消费过程分析)
            - [1.3.3.1. 消费者组](#1331-消费者组)
            - [1.3.3.2. 消费方式](#1332-消费方式)
            - [1.3.3.3. 消息交付语义](#1333-消息交付语义)
    - [1.4. kafka使用场景](#14-kafka使用场景)

<!-- /TOC -->

# 1. kafka
&emsp; **kafka中文文档：** https://kafka.apachecn.org/  

## 1.1. kafka介绍  
&emsp; Apache Kafka® 是 一个分布式流处理平台。流处理平台有以下三种特性:

* 可以让你发布和订阅流式的记录。这一方面与消息队列或者企业消息系统类似。
* 可以储存流式的记录，并且有较好的容错性。
* 可以在流式记录产生时就进行处理。 

## 1.2. kafka拓扑结构及相关概念  
&emsp; 一个典型的 Kafka 包含若干Producer、若干 Broker、若干 Consumer 以及一个 Zookeeper 集群。Zookeeper 是 Kafka 用来负责集群元数据管理、控制器选举等操作的。Producer 是负责将消息发送到 Broker 的，Broker 负责将消息持久化到磁盘，而 Consumer 是负责从Broker 订阅并消费消息。Kafka体系结构如下所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-3.png)  
<!-- 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-1.png)  

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
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-18.png)  
&emsp; 在 Kafka 中，消息以 主题（Topic）来分类，每一个主题都对应一个「消息队列」，这有点儿类似于数据库中的表。但是如果我们把所有同类的消息都塞入到一个“中心”队列中，势必缺少可伸缩性，无论是生产者/消费者数目的增加，还是消息数量的增加，都可能耗尽系统的性能或存储。  

### 1.2.4. offset

### 1.2.5. replica，副本机制，高可用性  
<!-- 
副本机制
https://blog.csdn.net/haogenmin/article/details/109449944
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

## 1.3. Kafka 工作流程分析  
<!-- 
https://mp.weixin.qq.com/s/OB-ZVy70vHClCtep43gr_A
https://mp.weixin.qq.com/s/ITLN-DHxYc5w6qrlFD8HWQ
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-19.png)  

### 1.3.1. kafka生产过程  
&emsp; producer 采用推（push）模式将消息发布到 broker，每条消息都被追加（append）到分区（patition）中，属于顺序写磁盘（顺序写磁盘效率比随机写内存要高，保障 kafka 吞吐率）。  
<!--
Kafka Producer 向 Broker 发送消息使用 Push 模式，Consumer 消费采用的 Pull 模式。拉取模式，让 consumer 自己管理 offset，可以提供读取性能
-->

&emsp; producer 写入消息流程如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-20.png)  
1. producer 先从 zookeeper 的 "/brokers/…/state"节点找到该 partition 的 leader ；
2. producer 将消息发送给该 leader ；
3. leader 将消息写入本地 log ；
4. followers 从 leader pull 消息，写入本地 log 后向 leader 发送 ACK ；
5. leader 收到所有 ISR 中的 replication 的 ACK 后，增加 HW（high watermark，最后 commit 的 offset）并向 producer 发送 ACK ；

### 1.3.2. Broker保存消息  
#### 1.3.2.1. 存储策略  
&emsp; 1）kafka以topic来进行消息管理，每个topic包含多个partition，每个partition对应一个逻辑log，有多个segment组成。  
&emsp; 2）每个segment中存储多条消息，消息id由其逻辑位置决定，即从消息id可直接定位到消息的存储位置，避免id到位置的额外映射。  
&emsp; 3）每个part在内存中对应一个index，记录每个segment中的第一条消息偏移。  
&emsp; 4）发布者发到某个topic的消息会被均匀的分布到多个partition上（或根据用户指定的路由规则进行分布），broker收到发布消息往对应partition的最后一个segment上添加该消息，当某个segment上的消息条数达到配置值或消息发布时间超过阈值时，segment上的消息会被flush到磁盘，只有flush到磁盘上的消息订阅者才能订阅到，segment达到一定的大小后将不会再往该segment写数据，broker会创建新的segment。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-5.png)  

&emsp; Kafka 中消息是以 topic 进行分类的，生产者生产消息，消费者消费消息，都是面向 topic 的。  
&emsp; 在 Kafka 中，一个 topic 可以分为多个 partition，一个 partition 分为多个 segment，每个 segment 对应两个文件：.index 和 .log 文件。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-6.png)  
&emsp; topic 是逻辑上的概念，而 patition 是物理上的概念，每个 patition 对应一个 log 文件，而 log 文件中存储的就是 producer 生产的数据，patition 生产的数据会被不断的添加到 log 文件的末端，且每条数据都有自己的 offset。  

&emsp; 消费组中的每个消费者，都是实时记录自己消费到哪个 offset，以便出错恢复，从上次的位置继续消费。  
&emsp; **消息存储原理**  
&emsp; 由于生产者生产的消息会不断追加到 log 文件末尾，为防止 log 文件过大导致数据定位效率低下，Kafka 采取了分片和索引机制，将每个 partition 分为多个 segment。每个 segment 对应两个文件——.index文件和 .log文件。这些文件位于一个文件夹下，该文件夹的命名规则为：topic名称+分区序号。  
 
<!-- 
https://mp.weixin.qq.com/s/nSa2CPjbMFdOsYB2Dt0kYg
-->

#### 1.3.2.2. 存储方式  
&emsp; 物理上把 topic 分成一个或多个 patition（对应 server.properties 中的 num.partitions=3 配 置），每个 patition 物理上对应一个文件夹（该文件夹存储该 patition 的所有消息和索引文 件），如下：  

```text
[root@hadoop102 logs]$ ll 
drwxrwxr-x. 2 demo demo 4096 8 月 6 14:37 first-0 
drwxrwxr-x. 2 demo demo 4096 8 月 6 14:35 first-1 
drwxrwxr-x. 2 demo demo 4096 8 月 6 14:37 first-2 

[root@hadoop102 logs]$ cd first-0 
[root@hadoop102 first-0]$ ll 
-rw-rw-r--. 1 demo demo 10485760 8 月 6 14:33 00000000000000000000.index 
-rw-rw-r--. 1 demo demo 219 8 月 6 15:07 00000000000000000000.log 
-rw-rw-r--. 1 demo demo 10485756 8 月 6 14:33 00000000000000000000.timeindex 
-rw-rw-r--. 1 demo demo 8 8 月 6 14:37 leader-epoch-checkpoint
```

#### 1.3.2.3. 存储策略  
&emsp; 无论消息是否被消费，kafka 都会保留所有消息。有两种策略可以删除旧数据：  

* 基于时间：log.retention.hours=168
* 基于大小：log.retention.bytes=1073741824

&emsp; 需要注意的是，因为 Kafka 读取特定消息的时间复杂度为 O(1)，即与文件大小无关， 所以这里删除过期文件与提高 Kafka 性能无关。  

### 1.3.3. Kafka消费过程分析  
<!-- 

https://mp.weixin.qq.com/s/nSa2CPjbMFdOsYB2Dt0kYg

-->

#### 1.3.3.1. 消费者组  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-22.png)  
&emsp; 消费者是以 consumer group 消费者组的方式工作，由一个或者多个消费者组成一个组， 共同消费一个 topic。每个分区在同一时间只能由 group 中的一个消费者读取，但是多个 group 可以同时消费这个 partition。在图中，有一个由三个消费者组成的 group，有一个消费者读取主题中的两个分区，另外两个分别读取一个分区。某个消费者读取某个分区，也可以叫做某个消费者是某个分区的拥有者。  
&emsp; 在这种情况下，消费者可以通过水平扩展的方式同时读取大量的消息。另外，如果一个消费者失败了，那么其他的 group 成员会自动负载均衡读取之前失败的消费者读取的分区。  

#### 1.3.3.2. 消费方式
&emsp; consumer 采用 pull（拉）模式从 broker 中读取数据。  
&emsp; push（推）模式很难适应消费速率不同的消费者，因为消息发送速率是由 broker 决定的。 它的目标是尽可能以最快速度传递消息，但是这样很容易造成 consumer 来不及处理消息，典型的表现就是拒绝服务以及网络拥塞。而 pull 模式则可以根据 consumer 的消费能力以适当的速率消费消息。  
&emsp; 对于 Kafka 而言，pull 模式更合适，它可简化 broker 的设计，consumer 可自主控制消费 消息的速率，同时 consumer 可以自己控制消费方式——即可批量消费也可逐条消费，同时还能选择不同的提交方式从而实现不同的传输语义。  
&emsp; pull 模式不足之处是，如果 kafka 没有数据，消费者可能会陷入循环中，一直等待数据 到达。为了避免这种情况，在拉取请求中有参数，允许消费者请求在等待数据到达 的“长轮询”中进行阻塞（并且可选地等待到给定的字节数，以确保大的传输大小）。  

#### 1.3.3.3. 消息交付语义  
&emsp; Kafka 在 producer 和 consumer 之间提供的语义保证。显然，Kafka可以提供的消息交付语义保证有多种：  

* At most once——消息可能会丢失但绝不重传。
* At least once——消息可以重传但绝不丢失。
* Exactly once——这正是人们想要的, 每一条消息只被传递一次。

&emsp; 值得注意的是，这个问题被分成了两部分：发布消息的持久性保证和消费消息的保证。  


## 1.4. kafka使用场景  
&emsp; **kafka常用使用场景：**  

* 日志收集：一个公司可以用Kafka可以收集各种服务的log，通过kafka以统一接口服务的方式开放给各种consumer；  
* 消息系统：解耦生产者和消费者、缓存消息等；  
* 用户活动跟踪：kafka经常被用来记录web用户或者app用户的各种活动，如浏览网页、搜索、点击等活动，这些活动信息被各个服务器发布到kafka的topic中，然后消费者通过订阅这些topic来做实时的监控分析，亦可保存到数据库；  
* 运营指标：kafka也经常用来记录运营监控数据。包括收集各种分布式应用的数据，生产各种操作的集中反馈，比如报警和报告；  
* 流式处理：比如spark streaming和storm。  


<!-- 

http://blog.51cto.com/littledevil

 Java人应该知道的SpringBoot For Kafka (上) 
https://mp.weixin.qq.com/s/2U2jSgA95-D0_N4HxwOnWA
 Java人应该知道的SpringBoot For Kafka (下) 
https://mp.weixin.qq.com/s/JB660Pgypr-PvkkdGOlhag

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

<!-- 
小赵
Kafka系列第7篇：你必须要知道集群内部工作原理的一些事！
https://mp.weixin.qq.com/s/5uTiunLJZvNqly6xdMjbzw
-->