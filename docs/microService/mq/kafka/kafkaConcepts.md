<!-- TOC -->

- [1. kafka](#1-kafka)
    - [1.1. kafka介绍](#11-kafka介绍)
    - [1.2. ~~kafka拓扑结构及相关概念~~](#12-kafka拓扑结构及相关概念)
    - [1.3. kafka使用场景](#13-kafka使用场景)

<!-- /TOC -->

<!-- 

《Kafka成神之路》- 索引类型
https://mp.weixin.qq.com/s/QPHPugWlbfeh8HhQvELSSQ

-->

# 1. kafka
&emsp; **kafka中文文档：** https://kafka.apachecn.org/  

## 1.1. kafka介绍  
&emsp; Apache Kafka是一个分布式流处理平台。流处理平台有以下三种特性:

* 可以发布和订阅流式的记录。这一方面与消息队列或者企业消息系统类似。
* 可以储存流式的记录，并且有较好的容错性。
* 可以在流式记录产生时就进行处理。 

## 1.2. ~~kafka拓扑结构及相关概念~~  
&emsp; 一个典型的Kafka包含若干Producer、Broker集群、若干Consumer以及一个Zookeeper集群。Kafka体系结构图如下所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-3.png)  

1. producer：  
&emsp; 消息生产者，发布消息到kafka集群的终端或服务。  
2. broker：  
&emsp; kafka 集群中包含的服务器。
3. topic：  
&emsp; 每条发布到 kafka 集群的消息属于的类别，即kafka是面向topic的。主题，表示一类消息，consumer通过订阅Topic来消费消息，一个Broker节点可以有多个Topic，每个Topic又包含N个partition(分区或者分片)。    
4. partition：  
&emsp; 每个 topic 包含一个或多个 partition。partition 是一个有序且不可变的消息序列，它是以 append log 文件形式存储的，partition 用于存放 Producer 生产的消息，然后 Consumer 消费 partition 上的消息，每个 partition 只能被一个 Consumer 消费。partition 还有副本的概念。    
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
&emsp; 存储 Kafka 集群的元数据信息，比如记录注册的 Broker 列表，topic 元数据信息，partition 元数据信息等等。   

## 1.3. kafka使用场景  
&emsp; Kafka的用途有哪些？使用场景如何？  

* 消息系统：Kafka 和传统的消息系统（也称作消息中间件）都具备系统解耦、冗余存储、流量削峰、缓冲、异步通信、扩展性、可恢复性等功能。与此同时，Kafka 还提供了大多数消息系统难以实现的消息顺序性保障及回溯消费的功能。
* 存储系统：Kafka 把消息持久化到磁盘，相比于其他基于内存存储的系统而言，有效地降低了数据丢失的风险。也正是得益于 Kafka 的消息持久化功能和多副本机制，可以把 Kafka 作为长期的数据存储系统来使用，只需要把对应的数据保留策略设置为“永久”或启用主题的日志压缩功能即可。
* 流式处理平台：Kafka 不仅为每个流行的流式处理框架提供了可靠的数据来源，还提供了一个完整的流式处理类库，比如窗口、连接、变换和聚合等各类操作。
