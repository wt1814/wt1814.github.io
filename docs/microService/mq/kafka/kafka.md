<!-- TOC -->

- [1. kafka](#1-kafka)
    - [1.1. kafka介绍](#11-kafka介绍)
    - [1.2. kafka拓扑结构及相关概念](#12-kafka拓扑结构及相关概念)
    - [1.3. kafka使用场景](#13-kafka使用场景)

<!-- /TOC -->

<!-- 

面试题是否有必要深入了解其背后的原理？我觉得应该刨根究底（下）
https://mp.weixin.qq.com/s/mRBtvAZ2_oJW91R9JiQpgQ

《Kafka成神之路》- 索引类型
https://mp.weixin.qq.com/s/QPHPugWlbfeh8HhQvELSSQ

Kafka 疑问之offset位移
https://blog.csdn.net/yujianping_123/article/details/97398373
Kafka学习笔记:位移主题Offsets Topic
https://www.pianshen.com/article/17331469353/

Kafka 术语
https://www.kancloud.cn/nicefo71/kafka/1470866	
Kafka 副本机制
https://www.kancloud.cn/nicefo71/kafka/1473376
Kafka Controller
https://www.kancloud.cn/nicefo71/kafka/1473379
Kafka 高水位和 Leader Epoch 机制
https://www.kancloud.cn/nicefo71/kafka/1473380
-->

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

## 1.3. kafka使用场景  
&emsp; Kafka的用途有哪些？使用场景如何？  

* 消息系统：Kafka 和传统的消息系统（也称作消息中间件）都具备系统解耦、冗余存储、流量削峰、缓冲、异步通信、扩展性、可恢复性等功能。与此同时，Kafka 还提供了大多数消息系统难以实现的消息顺序性保障及回溯消费的功能。
* 存储系统：Kafka 把消息持久化到磁盘，相比于其他基于内存存储的系统而言，有效地降低了数据丢失的风险。也正是得益于 Kafka 的消息持久化功能和多副本机制，可以把 Kafka 作为长期的数据存储系统来使用，只需要把对应的数据保留策略设置为“永久”或启用主题的日志压缩功能即可。
* 流式处理平台：Kafka 不仅为每个流行的流式处理框架提供了可靠的数据来源，还提供了一个完整的流式处理类库，比如窗口、连接、变换和聚合等各类操作。



<!-- 

全网最通俗易懂的 Kafka 入门
https://mp.weixin.qq.com/s?__biz=Mzg2MjEwMjI1Mg==&mid=2247490770&idx=2&sn=1008bcdaed680ed1413e2ead6320bec0&chksm=ce0dab51f97a224771a468245ed4f99f338a51a97505f2e78790cc8a6360f4f22c5e4f07cca2&mpshare=1&scene=1&srcid=&sharer_sharetime=1575464964037&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=e2a6a5ccea4b8ce41e290743b191d123754ef664941f31b9abdbdf28c289f875664f750548bc9da8bbbbabbeaa6a6d5fbb9efc00d2f33e693de36420dd87f9348fb89d058eb4d5ccbcfd806790431b8e&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=AQTbt4i1KPDzS6vieYS4x5I%3D&pass_ticket=UIzvXMBOSWKDgIz4M7cQoxQ548Mbvo9Oik9jB6kaYK60loRzg3FsHZUpAHYbC4%2By

-->

