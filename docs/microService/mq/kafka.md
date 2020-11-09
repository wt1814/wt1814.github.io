<!-- TOC -->

- [1. kafka](#1-kafka)
    - [1.1. kafka拓扑结构及相关概念](#11-kafka拓扑结构及相关概念)
        - [1.1.1. 存储策略](#111-存储策略)
    - [Kafka 工作流程分析](#kafka-工作流程分析)
    - [1.2. kafka使用场景](#12-kafka使用场景)

<!-- /TOC -->

# 1. kafka
&emsp; Apache Kafka是分布式发布-订阅消息系统。它最初由LinkedIn公司开发，之后成为Apache项目的一部分。Kafka是一种快速、可扩展的、设计内在就是分布式的，分区的和可复制的提交日志服务。  

## 1.1. kafka拓扑结构及相关概念  
&emsp; 一个典型的 Kafka 包含若干Producer、若干 Broker、若干 Consumer 以及一个 Zookeeper 集群。Zookeeper 是 Kafka 用来负责集群元数据管理、控制器选举等操作的。Producer 是负责将消息发送到 Broker 的，Broker 负责将消息持久化到磁盘，而 Consumer 是负责从Broker 订阅并消费消息。Kafka体系结构如下所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-3.png)  
<!-- 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-1.png)  
--> 
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

### 1.1.1. 存储策略  
&emsp; 1）kafka以topic来进行消息管理，每个topic包含多个partition，每个partition对应一个逻辑log，有多个segment组成。  
&emsp; 2）每个segment中存储多条消息，消息id由其逻辑位置决定，即从消息id可直接定位到消息的存储位置，避免id到位置的额外映射。  
&emsp; 3）每个part在内存中对应一个index，记录每个segment中的第一条消息偏移。  
&emsp; 4）发布者发到某个topic的消息会被均匀的分布到多个partition上（或根据用户指定的路由规则进行分布），broker收到发布消息往对应partition的最后一个segment上添加该消息，当某个segment上的消息条数达到配置值或消息发布时间超过阈值时，segment上的消息会被flush到磁盘，只有flush到磁盘上的消息订阅者才能订阅到，segment达到一定的大小后将不会再往该segment写数据，broker会创建新的segment。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-5.png)  

&emsp; Kafka 中消息是以 topic 进行分类的，生产者生产消息，消费者消费消息，都是面向 topic 的。  
&emsp; 在 Kafka 中，一个 topic 可以分为多个 partition，一个 partition 分为多个 segment，每个 segment 对应两个文件：.index 和 .log 文件  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-6.png)  
&emsp; topic 是逻辑上的概念，而 patition 是物理上的概念，每个 patition 对应一个 log 文件，而 log 文件中存储的就是 producer 生产的数据，patition 生产的数据会被不断的添加到 log 文件的末端，且每条数据都有自己的 offset。  

&emsp; 消费组中的每个消费者，都是实时记录自己消费到哪个 offset，以便出错恢复，从上次的位置继续消费。  
&emsp; **消息存储原理**  
&emsp; 由于生产者生产的消息会不断追加到 log 文件末尾，为防止 log 文件过大导致数据定位效率低下，Kafka 采取了分片和索引机制，将每个 partition 分为多个 segment。每个 segment 对应两个文件——.index文件和 .log文件。这些文件位于一个文件夹下，该文件夹的命名规则为：topic名称+分区序号。  
 
<!-- 
https://mp.weixin.qq.com/s/nSa2CPjbMFdOsYB2Dt0kYg
-->

## Kafka 工作流程分析  
<!-- 
https://blog.csdn.net/BeiisBei/article/details/108599723

https://mp.weixin.qq.com/s/OB-ZVy70vHClCtep43gr_A

https://mp.weixin.qq.com/s/ITLN-DHxYc5w6qrlFD8HWQ
-->


## 1.2. kafka使用场景  
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