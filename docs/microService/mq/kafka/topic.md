<!-- TOC -->

- [1. 分区](#1-分区)
    - [1.2. 为什么分区？](#12-为什么分区)
    - [1.3. ★★★物理分区分配](#13-★★★物理分区分配)
    - [1.4. 如何选择合适的分区数？](#14-如何选择合适的分区数)
    - [1.5. ~~分区存储数据(日志存储)~~](#15-分区存储数据日志存储)

<!-- /TOC -->

# 1. 分区

## 1.2. 为什么分区？  

* 首先，它们允许日志扩展到超出单个服务器所能容纳的大小。每个单独的分区都必须适合托管它的服务器，但是一个Topic可能有很多分区，因此它可以处理任意数量的数据。 
* 其次每个服务器充当其某些分区的Leader，也可能充当其他分区的Follwer，因此集群中的负载得到了很好的平衡。  

<!-- 
&emsp;从数据组织形式来说，kafka有三层形式，kafka有多个主题，每个主题有多个分区，每个分区又有多条消息。  
&emsp;而每个分区可以分布到不同的机器上，这样一来，从服务端来说，分区可以实现高伸缩性，以及负载均衡，动态调节的能力。  
-->

## 1.3. ★★★物理分区分配
&emsp; **在创建主题时，Kafka 会首先决定如何在broker间分配分区副本，** 它遵循以下原则：  

* 在所有broker上均匀地分配分区副本；  
* <font color = "clime">确保分区的每个副本分布在不同的broker上；</font>  
* 如果使用了broker.rack 参数为broker 指定了机架信息，那么会尽可能的把每个分区的副本分配到不同机架的broker上，以避免一个机架不可用而导致整个分区不可用。  

&emsp; 基于以上原因，如果在一个单节点上创建一个3副本的主题，通常会抛出下面的异常：  

```text
Error while executing topic command : org.apache.kafka.common.errors.InvalidReplicationFactor   
Exception: Replication factor: 3 larger than available brokers: 1.
```

## 1.4. 如何选择合适的分区数？  
&emsp; 在Kafka中，性能与分区数有着必然的关系，在设定分区数时一般也需要考虑性能的因素。对不同的硬件而言，其对应的性能也会不太一样。**可以使用Kafka 本身提供的用于生产者性能测试的kafka-producer-perf-test.sh和用于消费者性能测试的kafka-consumer-perf-test.sh来进行测试。**  
&emsp; 增加合适的分区数可以在一定程度上提升整体吞吐量，但超过对应的阈值之后吞吐量不升反降。如果应用对吞吐量有一定程度上的要求，则建议在投入生产环境之前对同款硬件资源做一个完备的吞吐量相关的测试，以找到合适的分区数阈值区间。  
&emsp; 分区数的多少还会影响系统的可用性。如果分区数非常多，如果集群中的某个broker节点宕机，那么就会有大量的分区需要同时进行leader角色切换，这个切换的过程会耗费一笔可观的时间，并且在这个时间窗口内这些分区也会变得不可用。  
&emsp; 分区数越多也会让Kafka的正常启动和关闭的耗时变得越长，与此同时，主题的分区数越多不仅会增加日志清理的耗时，而且在被删除时也会耗费更多的时间。  

## 1.5. ~~分区存储数据(日志存储)~~  
<!-- 
消息在分区上的存储（Partition、Replica、Log和LogSegment的关系）
&emsp; 假设有一个 Kafka 集群，Broker 个数为 3，Topic 个数为 1，Partition 个数为 3，Replica 个数为 2。Partition 的物理分布如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-83.png)  
&emsp; 从上图可以看出，该 Topic 由三个 Partition 构成，并且每个 Partition 由主从两个副本构成。每个 Partition 的主从副本分布在不同的 Broker 上，通过这点也可以看出，当某个 Broker 宕机时，可以将分布在其他 Broker 上的从副本设置为主副本，因为只有主副本对外提供读写请求，当然在最新的 2.x 版本中从副本也可以对外读请求了。将主从副本分布在不同的Broker上从而提高系统的可用性。   
&emsp; Partition 的实际物理存储是以 Log 文件的形式展示的，而每个Log文件又以多个LogSegment组成。Kafka 为什么要这么设计呢？其实原因比较简单，随着消息的不断写入，Log 文件肯定是越来越大，Kafka 为了方便管理，将一个大文件切割成一个一个的LogSegment来进行管理；每个LogSegment由数据文件和索引文件构成，数据文件是用来存储实际的消息内容，而索引文件是为了加快消息内容的读取。  
&emsp; 可能又有朋友会问，Kafka本身消费是以Partition维度顺序消费消息的，磁盘在顺序读的时候效率很高完全没有必要使用索引啊。其实Kafka为了满足一些特殊业务需求，比如要随机消费 Partition 中的消息，此时可以先通过索引文件快速定位到消息的实际存储位置，然后进行处理。  

&emsp; **总结一下 Partition、Replica、Log 和 LogSegment 之间的关系。** 消息是以 Partition 维度进行管理的，为了提高系统的可用性，每个 Partition 都可以设置相应的 Replica 副本数，一般在创建 Topic 的时候同时指定 Replica 的个数；Partition 和 Replica 的实际物理存储形式是通过 Log 文件展现的，为了防止消息不断写入，导致 Log 文件大小持续增长，所以将 Log 切割成一个一个的 LogSegment 文件。  
&emsp; 注意： 在同一时刻，每个主 Partition 中有且只有一个 LogSegment 被标识为可写入状态，当一个 LogSegment 文件大小超过一定大小后（比如当文件大小超过 1G，这个就类似于 HDFS 存储的数据文件，HDFS 中数据文件达到 128M 的时候就会被分出一个新的文件来存储数据），就会新创建一个 LogSegment 来继续接收新写入的消息。 


&emsp; 在创建主题时，Kafka系统会将分区分配到各个代理节点（Broker）。例如，现有3个代理节点，准备创建一个包含6个分区、3 个副本的主题，那么Kafka系统就会有18个分区副本，这18个分区副本将被分配到3个代理节点中。  * 主题(Topic)：用来区分不同的业务消息，类似于数据库中的表。  

* 分区(Partition)：是主题物理意义上的分组。一个主题可以分为多个分区，每个分区是一个有序的队列。  
* 片段(Segment)：每个分区又可以分为多个片段文件。  
* 偏移量(Offset)：每个分区都由一系列有序的、不可修改的消息组成，这些消息被持续追加到分区中，分区中的每条消息记录都有一个连续的序号，即Offset值，Offset值用来标识这条消息的唯一性。  


-->
<!-- 深入理解kafka：核心设计 第5章 -->

&emsp; 在Kafka系统中，消息以主题作为基本单位。不同的主题之间是相互独立、互不干扰的。每个主题又可以分为若干个分区，每个分区用来存储一部分的消息。  
1. 分区文件存储  
&emsp; 在 Kafka 系统中，一个主题（Topic）下包含多个不同的分区（Partition），每个分区为单独的一个目录。分区的命名规则为：主题名+有序序号。第一个分区的序号从0开始，序号最大值等于分区总数减1。  
&emsp; 主题的存储路径由“log.dirs”属性决定。代理节点中主题分区的存储分布如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-108.png)  
&emsp; 每个分区相当于一个超大的文件被均匀分割成的若干个大小相等的片段（Segment），但是每个片段的消息数据量不一定相等。因此，过期的片段数据才能被快速地删除。  
&emsp; 片段文件的生命周期由代理节点server.properties文件中配置的参数决定，这样，快速删除无用的数据可以有效地提高磁盘利用率。  

2. 片段文件存储  
&emsp; 片段文件由索引文件和数据文件组成：后缀为“.index”的是索引文件，后缀为“.log”的是数据文件。  
&emsp; 查看某一个分区的片段，输出结果如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-109.png)  
&emsp; Kafka系统中，索引文件并没有给数据文件中的每条消息记录都建立索引，而是采用了稀疏存储的方式——每隔一定字节的数据建立一条索引，如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-110.png)  
&emsp; 提示：  
&emsp; 稀疏存储索引避免了索引文件占用过多的磁盘空间。  
&emsp; 将索引文件存储在内存中，虽然没有建立索引的Message，不能一次就定位到所在的数据文件上的位置，但是稀疏索引的存在会极大地减少顺序扫描的范围。  
