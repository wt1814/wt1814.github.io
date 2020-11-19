

<!-- TOC -->

- [1. kafka特性](#1-kafka特性)
    - [1.1. Kafka的高效读写机制](#11-kafka的高效读写机制)
        - [1.1.1. 顺序读写](#111-顺序读写)
            - [1.1.1.1. 顺序写入](#1111-顺序写入)
            - [1.1.1.2. Memory Mapped Files](#1112-memory-mapped-files)
        - [1.1.2. 基于 Sendfile 实现零拷贝（Zero Copy）](#112-基于-sendfile-实现零拷贝zero-copy)
    - [1.2. kafka可靠性（如何保证消息队列不丢失）](#12-kafka可靠性如何保证消息队列不丢失)
        - [1.2.1. Producer 端丢失消息(ACK机制)](#121-producer-端丢失消息ack机制)
        - [1.2.2. Consumer 端丢失消息](#122-consumer-端丢失消息)
    - [1.3. kafka数据一致性，通过HW来保证](#13-kafka数据一致性通过hw来保证)
    - [1.4. kafa高可用性](#14-kafa高可用性)
    - [1.5. 如何让 Kafka 的消息有序？](#15-如何让-kafka-的消息有序)
    - [1.6. 消息重复消费](#16-消息重复消费)
    - [消息漏消费](#消息漏消费)
    - [1.7. 提升 Producer 的性能](#17-提升-producer-的性能)
        - [1.7.1. 批量发送](#171-批量发送)
        - [1.7.2. 数据压缩](#172-数据压缩)

<!-- /TOC -->


# 1. kafka特性

## 1.1. Kafka的高效读写机制  
&emsp; Kafka 的消息是保存或缓存在磁盘上的，一般认为在磁盘上读写数据是会降低性能的，因为寻址会比较消耗时间，但是实际上，Kafka 的特性之一就是高吞吐率。Kafka 之所以能这么快，是因为：「顺序写磁盘、大量使用内存页、零拷贝技术的使用」..  

### 1.1.1. 顺序读写  
&emsp; 数据写入 Kafka 会把收到的消息都写入到硬盘中，它绝对不会丢失数据。为了优化写入速度，Kafka 采用了两个技术：顺序写入和 Memory Mapped File 。

#### 1.1.1.1. 顺序写入  
&emsp; kafka的消息是不断追加到文件中的，这个特性使kafka可以充分利用磁盘的顺序读写性能。Kafka会将数据顺序插入到文件末尾，消费者端通过控制偏移量来读取消息，这样做会导致数据无法删除，时间一长，磁盘空间会满，kafka提供了2种策略来删除数据：基于时间删除和基于partition文件的大小删除。  

    顺序读写不需要硬盘磁头的寻道时间，只需很少的扇区旋转时间，所以速度远快于随机读写。

#### 1.1.1.2. Memory Mapped Files  
&emsp; 这个和Java NIO中的内存映射基本相同，mmf （Memory Mapped Files）直接利用操作系统的Page来实现文件到物理内存的映射，完成之后对物理内存的操作会直接同步到硬盘。mmf 通过内存映射的方式大大提高了IO速率，省去了用户空间到内核空间的复制。它的缺点显而易见--不可靠，当发生宕机而数据未同步到硬盘时，数据会丢失。  
<!-- Kafka 提供了produce.type参数来控制是否主动的进行刷新，如果 Kafka 写入到 mmf 后立即flush再返回给生产者则为同步模式，反之为异步模式。 --> 
&emsp; Kafka 提供了一个参数 producer.type 来控制是不是主动 Flush：  

* 如果 Kafka 写入到 mmf 之后就立即 Flush，然后再返回 Producer 叫同步 (Sync)。  
* 如果 Kafka 写入 mmf 之后立即返回 Producer 不调用 Flush 叫异步 (Async)。  

### 1.1.2. 基于 Sendfile 实现零拷贝（Zero Copy）  
<!-- 
在这之前先来了解一下零拷贝(直接让操作系统的 Cache 中的数据发送到网卡后传输给下游的消费者)：平时从服务器读取静态文件时，服务器先将文件从复制到内核空间，再复制到用户空间，最后再复制到内核空间并通过网卡发送出去，而零拷贝则是直接从内核到内核再到网卡，省去了用户空间的复制。

Kafka把所有的消息存放到一个文件中，当消费者需要数据的时候直接将文件发送给消费者，比如10W的消息共10M，全部发送给消费者，10M的消息在内网中传输是非常快的，假如需要1s，那么kafka的tps就是10w。Zero copy对应的是Linux中sendfile函数，这个函数会接受一个offsize来确定从哪里开始读取。现实中，不可能将整个文件全部发给消费者，他通过消费者传递过来的偏移量来使用零拷贝读取指定内容的数据返回给消费者。

在Linux kernel2.2 之后出现了一种叫做"零拷贝(zero-copy)"系统调用机制，就是跳过“用户缓冲区”的拷贝，建立一个磁盘空间和内存的直接映射，数据不再复制到“用户态缓冲区”，系统上下文切换减少为2次，可以提升一倍的性能。
-->
&emsp; 作为一个消息系统，不可避免的便是消息的拷贝，常规的操作，一条消息，需要从创建者的socket到应用，再到操作系统内核，然后才能落盘。同样，一条消息发送给消费者也要从磁盘到内核到应用再到接收者的socket，中间经过了多次不是很有必要的拷贝。  
&emsp; 传统 Read/Write 方式进行网络文件传输，在传输过程中，文件数据实际上是经过了四次 Copy 操作，其具体流程细节如下：  

* 调用 Read 函数，文件数据被 Copy 到内核缓冲区。  
* Read 函数返回，文件数据从内核缓冲区 Copy 到用户缓冲区。
* Write 函数调用，将文件数据从用户缓冲区 Copy 到内核与 Socket 相关的缓冲区。
* 数据从 Socket 缓冲区 Copy 到相关协议引擎。

    硬盘—>内核 buf—>用户 buf—>Socket 相关缓冲区—>协议引擎  
    
&emsp; 而 Sendfile 系统调用则提供了一种减少以上多次 Copy，提升文件传输性能的方法。在内核版本 2.1 中，引入了 Sendfile 系统调用，以简化网络上和两个本地文件之间的数据传输。Sendfile 的引入不仅减少了数据复制，还减少了上下文切换。相较传统 Read/Write 方式，2.1 版本内核引进的 Sendfile 已经减少了内核缓冲区到 User 缓冲区，再由 User 缓冲区到 Socket 相关缓冲区的文件 Copy。而在内核版本 2.4 之后，文件描述符结果被改变，Sendfile 实现了更简单的方式，再次减少了一次 Copy 操作。  
&emsp; Kafka 把所有的消息都存放在一个一个的文件中，当消费者需要数据的时候 Kafka 直接把文件发送给消费者，配合 mmap 作为文件读写方式，直接把它传给 Sendfile。  

## 1.2. kafka可靠性（如何保证消息队列不丢失）
<!-- 
 面试官问：Kafka 会不会丢消息？怎么处理的? 
 https://mp.weixin.qq.com/s/tioD1yMABXu8BLj3dGtTmg

 Kafka消息中间件到底会不会丢消息 
 https://mp.weixin.qq.com/s/uxYUEJRTEIULeObRIl209A
-->

### 1.2.1. Producer 端丢失消息(ACK机制)
&emsp; Producer 如何保证数据发送不丢失？ack 机制，重试机制。  
&emsp; 为保证 producer 发送的数据，能可靠的发送到指定的 topic，topic 的每个 partition 收到 producer 数据后，都需要向 producer 发送 ack（acknowledgement确认收到），如果 producer 收到 ack，就会进行下一轮的发送，否则重新发送数据。  

&emsp; **a) 副本数据同步策略主要有如下两种**  

|方案	|优点	|缺点|
|---|---|---|
|半数以上完成同步，就发送ack	|延迟低	|选举新的 leader 时，容忍n台节点的故障，需要2n+1个副本|
|全部完成同步，才发送ack|	选举新的 leader 时，容忍n台节点的故障，需要 n+1 个副本|	延迟高|

&emsp; Kafka 选择了第二种方案，原因如下：

* 同样为了容忍 n 台节点的故障，第一种方案需要的副本数相对较多，而 Kafka 的每个分区都有大量的数据，第一种方案会造成大量的数据冗余；
* 虽然第二种方案的网络延迟会比较高，但网络延迟对 Kafka 的影响较小。  

&emsp; **b) ISR**   
&emsp; 采用第二种方案之后，设想一下情景：leader 收到数据，所有 follower 都开始同步数据，但有一个 follower 挂了，迟迟不能与 leader 保持同步，那 leader 就要一直等下去，直到它完成同步，才能发送 ack，这个问题怎么解决呢？  
&emsp; leader 维护了一个动态的 in-sync replica set(ISR，保持同步的副本)，意为和 leader 保持同步的 follower 集合。当 ISR 中的follower 完成数据的同步之后，leader 就会给 follower 发送 ack。如果 follower 长时间未向 leader 同步数据，则该 follower 将会被踢出 ISR，该时间阈值由 replica.lag.time.max.ms 参数设定。leader 发生故障之后，就会从 ISR 中选举新的 leader。（之前还有另一个参数，0.9 版本之后 replica.lag.max.messages 参数被移除了）  

&emsp; **c) ack应答机制**  
&emsp; 对于某些不太重要的数据，对数据的可靠性要求不是很高，能够容忍数据的少量丢失，所以没必要等 ISR 中的follower全部接收成功。所以Kafka为用户提供了三种可靠性级别。  
    
    Kafka 的交付语义？交付语义一般有at least once、at most once和exactly once。kafka 通过 ack 的配置来实现前两种。  

&emsp; Kafka 通过 request.required.acks和min.insync.replicas 两个参数配合，在一定程度上保证消息不会丢失。  
&emsp; request.required.acks 可设置为 1、0、-1 三种情况。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-61.png)  
&emsp; 设置为 1 时代表当 Leader 状态的 Partition 接收到消息并持久化时就认为消息发送成功，如果 ISR 列表的 Replica 还没来得及同步消息，Leader 状态的 Partition 对应的 Broker 宕机，则消息有可能丢失。    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-62.png)  
&emsp; 设置为 0 时代表 Producer 发送消息后就认为成功，消息有可能丢失。    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-63.png)  
&emsp; 设置为-1 时，代表 ISR 列表中的所有 Replica 将消息同步完成后才认为消息发送成功；但是如果只存在主 Partition 的时候，Broker 异常时同样会导致消息丢失。所以此时就需要min.insync.replicas参数的配合，该参数需要设定值大于等于 2，当 Partition 的个数小于设定的值时，Producer 发送消息会直接报错。  

&emsp; 上面这个过程看似已经很完美了，但是假设如果消息在同步到部分从 Partition 上时，主 Partition 宕机，此时消息会重传，虽然消息不会丢失，但是会造成同一条消息会存储多次。在新版本中 Kafka 提出了幂等性的概念，通过给每条消息设置一个唯一 ID，并且该 ID 可以唯一映射到 Partition 的一个固定位置，从而避免消息重复存储的问题。  

### 1.2.2. Consumer 端丢失消息
&emsp; consumer端丢失消息的情形比较简单：  
&emsp; 如果在消息处理完成前就提交了offset，那么就有可能造成数据的丢失。  
&emsp; 由于Kafka consumer默认是自动提交位移的，所以在后台提交位移前一定要保证消息被正常处理了，因此不建议采用很重的处理逻辑，如果处理耗时很长，则建议把逻辑放到另一个线程中去做。  
&emsp; 为了避免数据丢失，现给出几点建议：设置 enable.auto.commit=false  
&emsp; 关闭自动提交位移，在消息被完整处理之后再手动提交位移。  

## 1.3. kafka数据一致性，通过HW来保证  
&emsp; 由于并不能保证 Kafka 集群中每时每刻 follower 的长度都和 leader 一致（即数据同步是有时延的），那么当leader 挂掉选举某个 follower 为新的 leader 的时候（原先挂掉的 leader 恢复了成为了 follower），可能会出现leader 的数据比 follower 还少的情况。为了解决这种数据量不一致带来的混乱情况，Kafka 提出了以下概念：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-27.png)  

* LEO（Log End Offset）：指的是每个副本最后一个offset；  
* HW（High Wather）：指的是消费者能见到的最大的 offset，ISR 队列中最小的 LEO。  

&emsp; 消费者和 leader 通信时，只能消费 HW 之前的数据，HW 之后的数据对消费者不可见。  

&emsp; 针对这个规则：  

* 当follower发生故障时：follower 发生故障后会被临时踢出 ISR，待该 follower 恢复后，follower 会读取本地磁盘记录的上次的 HW，并将 log 文件高于 HW 的部分截取掉，从 HW 开始向 leader 进行同步。等该 follower 的 LEO 大于等于该 Partition 的 HW，即 follower 追上 leader 之后，就可以重新加入 ISR 了。
* 当leader发生故障时：leader 发生故障之后，会从 ISR 中选出一个新的 leader，之后，为保证多个副本之间的数据一致性，其余的 follower 会先将各自的 log 文件高于 HW 的部分截掉，然后从新的 leader 同步数据。

&emsp; 所以数据一致性并不能保证数据不丢失或者不重复，这是由 ack 控制的。HW 规则只能保证副本之间的数据一致性！


## 1.4. kafa高可用性

Kafka 本身是一个分布式系统，同时采用了 Zookeeper 存储元数据信息，提高了系统的高可用性。  
Kafka 使用多副本机制，当状态为 Leader 的 Partition 对应的 Broker 宕机或者网络异常时，Kafka 会通过选举机制从对应的 Replica 列表中重新选举出一个 Replica 当做 Leader，从而继续对外提供读写服务（当然，需要注意的一点是，在新版本的 Kafka 中，Replica 也可以对外提供读请求了），利用多副本机制在一定程度上提高了系统的容错性，从而提升了系统的高可用。  

<!-- 
Kafka 副本机制
https://juejin.im/post/6844903950009794567
https://mp.weixin.qq.com/s/OB-ZVy70vHClCtep43gr_A
https://mp.weixin.qq.com/s/kguKr_k-BrcQz4G5gag8gg

-->
通过副本来保证数据的高可用，producer ack、重试、自动 Leader 选举，Consumer 自平衡  


分区与副本
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-29.png)  


在分布式数据系统中，通常使用分区来提高系统的处理能力，通过副本来保证数据的高可用性。多分区意味着并发处理的能力，这多个副本中，只有一个是 leader，而其他的都是 follower 副本。仅有 leader 副本可以对外提供服务。多个 follower 副本通常存放在和 leader 副本不同的 broker 中。通过这样的机制实现了高可用，当某台机器挂掉后，其他 follower 副本也能迅速”转正“，开始对外提供服务。

为什么 follower 副本不提供读服务？

这个问题本质上是对性能和一致性的取舍。试想一下，如果 follower 副本也对外提供服务那会怎么样呢？首先，性能是肯定会有所提升的。但同时，会出现一系列问题。类似数据库事务中的幻读，脏读。比如你现在写入一条数据到 kafka 主题 a，消费者 b 从主题 a 消费数据，却发现消费不到，因为消费者 b 去读取的那个分区副本中，最新消息还没写入。而这个时候，另一个消费者 c 却可以消费到最新那条数据，因为它消费了 leader 副本。Kafka 通过 WH 和 Offset 的管理来决定 Consumer 可以消费哪些数据，已经当前写入的数据。
watermark
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-30.png)  

只有 Leader 可以对外提供读服务，那如何选举 Leader

kafka 会将与 leader 副本保持同步的副本放到 ISR 副本集合中。当然，leader 副本是一直存在于 ISR 副本集合中的，在某些特殊情况下，ISR 副本中甚至只有 leader 一个副本。当 leader 挂掉时，kakfa 通过 zookeeper 感知到这一情况，在 ISR 副本中选取新的副本成为 leader，对外提供服务。但这样还有一个问题，前面提到过，有可能 ISR 副本集合中，只有 leader，当 leader 副本挂掉后，ISR 集合就为空，这时候怎么办呢？这时候如果设置 unclean.leader.election.enable 参数为 true，那么 kafka 会在非同步，也就是不在 ISR 副本集合中的副本中，选取出副本成为 leader。

副本的存在就会出现副本同步问题

Kafka 在所有分配的副本 (AR) 中维护一个可用的副本列表 (ISR)，Producer 向 Broker 发送消息时会根据ack配置来确定需要等待几个副本已经同步了消息才相应成功，Broker 内部会ReplicaManager服务来管理 flower 与 leader 之间的数据同步。
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-31.png)  

## 1.5. 如何让 Kafka 的消息有序？  
&emsp; Kafka 在 Topic 级别本身是无序的，只有 partition 上才有序，所以为了保证处理顺序，可以自定义分区器，将需顺序处理的数据发送到同一个 partition

&emsp; Kafka 无法做到消息全局有序，只能做到 Partition 维度的有序。所以如果想要消息有序，就需要从 Partition 维度入手。一般有两种解决方案。

* 单 Partition，单 Consumer。通过此种方案强制消息全部写入同一个 Partition 内，但是同时也牺牲掉了 Kafka 高吞吐的特性了，所以一般不会采用此方案。  
* 多 Partition，多 Consumer，指定 key 使用特定的 Hash 策略，使其消息落入指定的 Partition 中，从而保证相同的 key 对应的消息是有序的。此方案也是有一些弊端，比如当 Partition 个数发生变化时，相同的 key 对应的消息会落入到其他的 Partition 上，所以一旦确定 Partition 个数后就不能在修改 Partition 个数了。  


## 1.6. 消息重复消费
<!-- 

有哪些情形会造成重复消费？
Rebalance
一个consumer正在消费一个分区的一条消息，还没有消费完，发生了rebalance(加入了一个consumer)，从而导致这条消息没有消费成功，rebalance后，另一个consumer又把这条消息消费一遍。
消费者端手动提交
如果先消费消息，再更新offset位置，导致消息重复消费。
消费者端自动提交
设置offset为自动提交，关闭kafka时，如果在close之前，调用 consumer.unsubscribe() 则有可能部分offset没提交，下次重启会重复消费。
生产者端
生产者因为业务问题导致的宕机，在重启之后可能数据会重发

-->

&emsp; 有遇到过消息重复消费的情况吗？是怎么解决的？  
&emsp; 有，发生过两次重复消费的情况。发现用户的"xx"计数偶现大于实际情况，排查日志发现大概意思是心跳检测异常导致 commit 还没有来得及提交，对应的 Partition 被重新分配给其他的 Consumer 消费导致消息被重复消费。   

1. 解决方式 1：调整降低消费端的消费速率、提高心跳检测周期。  
&emsp; 通过方案 1 调整参数后，还是会出现重复消费的情况，只是出现的概率降低了。  
2. 解决方案 2：在业务层增加 Redis，在一定周期内，相同 key 对应的消息认为是同一条，如果 Redis 内不存在则正常消费消费，反之直接抛弃。  

## 消息漏消费  
<!-- 
https://www.cnblogs.com/luozhiyun/p/11811835.html
-->

## 1.7. 提升 Producer 的性能
&emsp; 如何提升 Producer 的性能？批量，异步，压缩  

### 1.7.1. 批量发送
&emsp; Kafka允许进行批量发送消息，producter发送消息的时候，可以将消息缓存在本地，等到了固定条件发送到 Kafka 。  

* 等消息条数到固定条数。  
* 一段时间发送一次。  

### 1.7.2. 数据压缩
&emsp; Kafka还支持对消息集合进行压缩，Producer可以通过GZIP或Snappy格式对消息集合进行压缩。压缩的好处就是减少传输的数据量，减轻对网络传输的压力。  
&emsp; Producer压缩之后，在Consumer需进行解压，虽然增加了CPU的工作，但在对大数据处理上，瓶颈在网络上而不是CPU，所以这个成本很值得。  
&emsp; 注意：「批量发送」和「数据压缩」一起使用，单条做数据压缩的话，效果不明显！  
