<!-- TOC -->

- [1. kafka](#1-kafka)
    - [1.1. kafka拓扑结构及相关概念](#11-kafka拓扑结构及相关概念)
        - [1.1.1. 存储策略](#111-存储策略)
    - [1.2. kafka使用场景](#12-kafka使用场景)

<!-- /TOC -->

# 1. kafka
&emsp; Apache Kafka是分布式发布-订阅消息系统。它最初由LinkedIn公司开发，之后成为Apache项目的一部分。Kafka是一种快速、可扩展的、设计内在就是分布式的，分区的和可复制的提交日志服务。  

## 1.1. kafka拓扑结构及相关概念  
&emsp; **kafka拓扑结构**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-3.png)  
<!-- 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-1.png)  
-->
&emsp; **kafka相关概念：**  
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

<!-- 
    Producer：生产者，也就是发送消息的一方。生产者负责创建消息，然后将其发送到 Kafka。
    Consumer：消费者，也就是接受消息的一方。消费者连接到 Kafka 上并接收消息，进而进行相应的业务逻辑处理。
    Consumer Group：一个消费者组可以包含一个或多个消费者。使用多分区 + 多消费者方式可以极大提高数据下游的处理速度，同一消费组中的消费者不会重复消费消息，同样的，不同消费组中的消费者消息消息时互不影响。Kafka 就是通过消费组的方式来实现消息 P2P 模式和广播模式。
    Broker：服务代理节点。Broker 是 Kafka 的服务节点，即 Kafka 的服务器。
    Topic：Kafka 中的消息以 Topic 为单位进行划分，生产者将消息发送到特定的 Topic，而消费者负责订阅 Topic 的消息并进行消费。
    Partition：Topic 是一个逻辑的概念，它可以细分为多个分区，每个分区只属于单个主题。同一个主题下不同分区包含的消息是不同的，分区在存储层面可以看作一个可追加的日志（Log）文件，消息在被追加到分区日志文件的时候都会分配一个特定的偏移量（offset）。
    Offset：offset 是消息在分区中的唯一标识，Kafka 通过它来保证消息在分区内的顺序性，不过 offset 并不跨越分区，也就是说，Kafka 保证的是分区有序性而不是主题有序性。
    Replication：副本，是 Kafka 保证数据高可用的方式，Kafka 同一 Partition 的数据可以在多 Broker 上存在多个副本，通常只有主副本对外提供读写服务，当主副本所在 broker 崩溃或发生网络一场，Kafka 会在 Controller 的管理下会重新选择新的 Leader 副本对外提供读写服务。
    Record：实际写入 Kafka 中并可以被读取的消息记录。每个 record 包含了 key、value 和 timestamp。

https://mp.weixin.qq.com/s/ITLN-DHxYc5w6qrlFD8HWQ

https://mp.weixin.qq.com/s?__biz=MjM5ODI5Njc2MA==&mid=2655826006&idx=1&sn=40260eb24d57c7dc651e864e2c1bc522&chksm=bd74fd818a037497fc7630776314f7b3b7ae34d93ec3390ed94eabb691978175d56d2c2581b5&mpshare=1&scene=1&srcid=&sharer_sharetime=1565613484904&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=ecc4386bb884a7b11f67eb046d1cfb51287e0b646d7dd5b2a83d57aff04863855b0c470a00d68836a0037bb73afb0da3faead154742a453e19c3bc9f3761889c6a136ee40c2cd73673f9301952f69a42&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=ZNpwaCm7lig8GxObuYnsLOy5YLlTGrfL0TUgNX%2BGTylF4nuPDQtCUafUJSn7768P

Topic是一个逻辑上的概念，而partition是物理上的概念

每个partition又有副本的概念

每个partition对应于一个log文件，该log文件中存储的就是生产者生成的数据，生产者生成的数据会不断的追加到该log的文件末端，且每条数据都有自己的offset，消费者都会实时记录自己消费到了那个offset，以便出错的时候从上次的位置继续消费，这个offset就保存在index文件中

kafka的offset是分区内有序的，但是在不同分区中是无顺序的，kafka不保证数据的全局有序
-->

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