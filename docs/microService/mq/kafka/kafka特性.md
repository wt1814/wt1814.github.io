

<!-- TOC -->

- [1. kafka特性](#1-kafka特性)
    - [1.1. Kafka的高性能（读写机制）](#11-kafka的高性能读写机制)
        - [1.1.1. 顺序读写](#111-顺序读写)
            - [1.1.1.1. 顺序写入](#1111-顺序写入)
            - [1.1.1.2. Memory Mapped Files](#1112-memory-mapped-files)
        - [1.1.2. 基于 Sendfile 实现零拷贝（Zero Copy）](#112-基于-sendfile-实现零拷贝zero-copy)
    - [1.2. kafa高可用性（副本机制）](#12-kafa高可用性副本机制)
        - [1.2.1. Kafka副本的功能](#121-kafka副本的功能)
        - [1.2.2. Leader的选举(ISR副本)](#122-leader的选举isr副本)
            - [1.2.2.1. Unclear Leader选举](#1221-unclear-leader选举)
        - [1.2.3. 副本的同步(LEO和HW)](#123-副本的同步leo和hw)
            - [1.2.3.1. Leader Epoch](#1231-leader-epoch)
    - [1.3. kafka可靠性（如何保证消息队列不丢失?）](#13-kafka可靠性如何保证消息队列不丢失)
        - [1.3.1. Broker端丢失消息](#131-broker端丢失消息)
            - [1.3.1.1. ACK机制与ISR列表](#1311-ack机制与isr列表)
        - [1.3.2. Producer端丢失消息](#132-producer端丢失消息)
        - [1.3.3. Consumer 端丢失消息](#133-consumer-端丢失消息)
    - [1.4. kafka数据一致性，通过HW来保证](#14-kafka数据一致性通过hw来保证)
    - [1.5. 如何让 Kafka 的消息有序？](#15-如何让-kafka-的消息有序)
    - [1.6. 消息重复消费](#16-消息重复消费)

<!-- /TOC -->


# 1. kafka特性
<!-- 

面试官：说说Kafka处理请求的全流程 
https://mp.weixin.qq.com/s/LEmybNmD5XwkBtcTPHcaEA
-->


## 1.1. Kafka的高性能（读写机制）  
&emsp; Kafka 的消息是保存或缓存在磁盘上的，一般认为在磁盘上读写数据是会降低性能的，因为寻址会比较消耗时间，但是实际上，Kafka 的特性之一就是高吞吐率。Kafka 之所以能这么快，是因为：「顺序写磁盘、大量使用内存页、零拷贝技术的使用」..  

### 1.1.1. 顺序读写  
&emsp; 数据写入 Kafka 会把收到的消息都写入到硬盘中。为了优化写入速度，Kafka 采用了两个技术：顺序写入和 Memory Mapped File 。

#### 1.1.1.1. 顺序写入  
&emsp; kafka的消息是不断追加到文件中的，这个特性使kafka可以充分利用磁盘的顺序读写性能。Kafka会将数据顺序插入到文件末尾，消费者端通过控制偏移量来读取消息，这样做会导致数据无法删除，时间一长，磁盘空间会满，kafka提供了2种策略来删除数据：基于时间删除和基于partition文件的大小删除。  

    顺序读写不需要硬盘磁头的寻道时间，只需很少的扇区旋转时间，所以速度远快于随机读写。

#### 1.1.1.2. Memory Mapped Files  
&emsp; 这个和Java NIO中的内存映射基本相同，mmf （Memory Mapped Files）直接利用操作系统的Page来实现文件到物理内存的映射，完成之后对物理内存的操作会直接同步到硬盘。mmf 通过内存映射的方式大大提高了IO速率，省去了用户空间到内核空间的复制。它的缺点显而易见，不可靠，当发生宕机而数据未同步到硬盘时，数据会丢失。  
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
    
&emsp; 而 Sendfile 系统调用则提供了一种减少以上多次 Copy，提升文件传输性能的方法。<font color = "red">在内核版本 2.1 中，引入了 Sendfile 系统调用，以简化网络上和两个本地文件之间的数据传输。</font>Sendfile 的引入不仅减少了数据复制，还减少了上下文切换。相较传统 Read/Write 方式，2.1 版本内核引进的 Sendfile 已经减少了内核缓冲区到 User 缓冲区，再由 User 缓冲区到 Socket 相关缓冲区的文件 Copy。而在内核版本 2.4 之后，文件描述符结果被改变，Sendfile 实现了更简单的方式，再次减少了一次 Copy 操作。  
&emsp; Kafka 把所有的消息都存放在一个一个的文件中，当消费者需要数据的时候 Kafka 直接把文件发送给消费者，配合 mmap 作为文件读写方式，直接把它传给 Sendfile。  


## 1.2. kafa高可用性（副本机制）
<!-- 
Kafka 副本机制
https://juejin.im/post/6844903950009794567
https://mp.weixin.qq.com/s/OB-ZVy70vHClCtep43gr_A
https://www.kancloud.cn/nicefo71/kafka/1473376

Kafka中副本机制的设计和原理 
https://mp.weixin.qq.com/s/yIPIABpAzaHJvGoJ6pv0kg
副本机制
https://blog.csdn.net/haogenmin/article/details/109449944
深入理解Kafka必知必会
https://www.cnblogs.com/luozhiyun/p/12079527.html

-->

&emsp; 在分布式数据系统中，通常使用分区来提高系统的处理能力，通过副本来保证数据的高可用性。Kafka中一个分区可以拥有多个副本，副本可分布于多台机器上。而在多个副本中，只会有一个Leader副本与客户端交互，也就是读写数据。其他则作为Follower副本，负责同步Leader的数据，当Leader宕机时，从Follower选举出新的Leader，从而解决分区单点问题。   

<!-- 
多分区意味着并发处理的能力，这多个副本中，只有一个是 leader，而其他的都是 follower 副本。仅有 leader 副本可以对外提供服务。多个 follower 副本通常存放在和 leader 副本不同的 broker 中。通过这样的机制实现了高可用，当某台机器挂掉后，其他 follower 副本也能迅速”转正“，开始对外提供服务。
-->
### 1.2.1. Kafka副本的功能  
&emsp; 副本机制的使用在计算机的世界里是很常见的，比如MySQL、ZooKeeper、CDN等都有使用副本机制。使用副本机制所能带来的好处有以下几种：  

* 提供数据冗余，提高可用性；
* 提供扩展性，增加读操作吞吐量；
* 改善数据局部，降低系统延时。

&emsp; 但并不是每个好处都能获得，这还是和具体的设计有关，比如Kafka只具有第一个好处，即提高可用性。这是因为**副本中只有Leader可以和客户端交互，进行读写，其他副本是只能同步，不能分担读写压力。**  

<!--
https://mp.weixin.qq.com/s/yIPIABpAzaHJvGoJ6pv0kg

&emsp; 为什么 follower 副本不提供读服务？
这个问题本质上是对性能和一致性的取舍。试想一下，如果 follower 副本也对外提供服务那会怎么样呢？首先，性能是肯定会有所提升的。但同时，会出现一系列问题。类似数据库事务中的幻读，脏读。比如你现在写入一条数据到 kafka 主题 a，消费者 b 从主题 a 消费数据，却发现消费不到，因为消费者 b 去读取的那个分区副本中，最新消息还没写入。而这个时候，另一个消费者 c 却可以消费到最新那条数据，因为它消费了 leader 副本。Kafka 通过 WH 和 Offset 的管理来决定 Consumer 可以消费哪些数据，已经当前写入的数据。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-30.png)  
-->


### 1.2.2. Leader的选举(ISR副本)
<!-- 
只有 Leader 可以对外提供读服务，那如何选举 Leader

kafka 会将与 leader 副本保持同步的副本放到 ISR 副本集合中。当然，leader 副本是一直存在于 ISR 副本集合中的，在某些特殊情况下，ISR 副本中甚至只有 leader 一个副本。当 leader 挂掉时，kakfa 通过 zookeeper 感知到这一情况，在 ISR 副本中选取新的副本成为 leader，对外提供服务。但这样还有一个问题，前面提到过，有可能 ISR 副本集合中，只有 leader，当 leader 副本挂掉后，ISR 集合就为空，这时候怎么办呢？这时候如果设置 unclean.leader.election.enable 参数为 true，那么 kafka 会在非同步，也就是不在 ISR 副本集合中的副本中，选取出副本成为 leader。  
-->
&emsp; 当Leader宕机时，要从Follower中选举出新的Leader，但并不是所有的Follower都有资格参与选举。因为有的Follower的同步情况滞后，如果让它成为Leader将会导致消息丢失。  
&emsp; 而为了避免这个情况，Kafka引入了ISR（In-Sync Replica）副本的概念，这是一个集合，里面存放的是和Leader保持同步的副本并含有Leader。这是一个动态调整的集合，当副本由同步变为滞后时会从集合中剔除，而当副本由滞后变为同步时又会加入到集合中。  
&emsp; 那么如何判断一个副本是同步还是滞后呢？Kafka在0.9版本之前，是根据replica.lag.max.messages参数来判断，其含义是同步副本所能落后的最大消息数，当Follower上的最大偏移量落后Leader大于replica.lag.max.messages时，就认为该副本是不同步的了，会从ISR中移除。  
&emsp; 如果ISR的值设置得过小，会导致Follower经常被踢出ISR，而如果设置过大，则当Leader宕机时，会造成较多消息的丢失。在实际使用时，很难给出一个合理值，这是因为当生产者为了提高吞吐量而调大batch.size时，会发送更多的消息到Leader上，这时候如果不增大replica.lag.max.messages，则会有Follower频繁被踢出ISR的现象  
&emsp; 而当Follower发生Fetch请求同步后，又被加入到ISR中，ISR将频繁变动。鉴于该参数难以设定，Kafka在0.9版本引入了一个新的参数replica.lag.time.max.ms，默认10s，含义是当Follower超过10s没发送Fetch请求同步Leader时，就会认为不同步而被踢出ISR。从时间维度来考量，能够很好地避免生产者发送大量消息到Leader副本导致分区ISR频繁收缩和扩张的问题。  

#### 1.2.2.1. Unclear Leader选举    
&emsp; 当ISR集合为空时，即没有同步副本（Leader也挂了），无法选出下一个Leader，Kafka集群将会失效。而为了提高可用性，Kafka提供了unclean.leader.election.enable参数，当设置为true且ISR集合为空时，会进行Unclear Leader选举，允许在非同步副本中选出新的Leader，从而提高Kafka集群的可用性，但这样会造成消息丢失。在允许消息丢失的场景中，是可以开启此参数来提高可用性的。而其他情况，则不建议开启，而是通过其他手段来提高可用性。  

### 1.2.3. 副本的同步(LEO和HW)  
<!-- 
副本的存在就会出现副本同步问题

Kafka 在所有分配的副本 (AR) 中维护一个可用的副本列表 (ISR)，Producer 向 Broker 发送消息时会根据ack配置来确定需要等待几个副本已经同步了消息才相应成功，Broker 内部会ReplicaManager服务来管理 flower 与 leader 之间的数据同步。
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-31.png)  
-->
&emsp; 副本的本质其实是一个消息日志，为了让副本正常同步，需要通过一些变量记录副本的状态，如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-75.png)  
&emsp; 其中LEO（Last End Offset）记录了日志的下一条消息偏移量，即当前最新消息的偏移量加一。而HW（High Watermark）界定了消费者可见的消息，消费者可以消费小于HW的消息，而大于等于HW的消息将无法消费。HW和LEO的关系是HW一定小于LEO。  
&emsp; 下面介绍下HW的概念，其可翻译为高水位或高水印，这一概念通常用于在流式处理领域（如Flink、Spark等），流式系统将保证在HW为t时刻时，创建时间小于等于t时刻的所有事件都已经到达或可被观测到。而在Kafka中，HW的概念和时间无关，而是和偏移量有关，主要目的是为了保证一致性。  
&emsp; 试想如果一个消息到达了Leader，而Follower副本还未来得及同步，但该消息能已被消费者消费了，这时候Leader宕机，Follower副本中选出新的Leader，消息将丢失，出现不一致的现象。所以Kafka引入HW的概念，当消息被同步副本同步完成时，才让消息可被消费。  
&emsp; 上述即是LEO和HW的基本概念，下面看下具体是如何工作的。  
&emsp; 在每个副本中都存有LEO和HW，而Leader副本中除了存有自身的LEO和HW，还存储了其他Follower副本的LEO和HW值，为了区分我们把Leader上存储的Follower副本的LEO和HW值叫做远程副本的LEO和HW值，如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-76.png)  
&emsp; 之所以这么设计，是为了HW的更新，Leader需保证HW是ISR副本集合中LEO的最小值。关于具体的更新，分为Follower副本和Leader副本来看。  
&emsp; Follower副本更新LEO和HW的时机只有向Leader拉取了消息之后，会用当前的偏移量加1来更新LEO，并且用Leader的HW值和当前LEO的最小值来更新HW：  

    CurrentOffset + 1 -> LEO
    min(LEO, LeaderHW) -> HW

&emsp; LEO的更新，很好理解。那为什么HW要取LEO和LeaderHW的最小值，为什么不直接取LeaderHW，LeaderHW不是一定大于LEO吗？我们在前文简单的提到了LeaderHW是根据同步副本来决定，所以LeaderHW一定小于所有同步副本的LEO，而并不一定小于非同步副本的LEO，所以如果一个非同步副本在拉取消息，那LEO是会小于LeaderHW的，则应用当前LEO值来更新HW。  
&emsp; 说完了Follower副本上LEO和HW的更新，下面看Leader副本。   
&emsp; 正常情况下Leader副本的更新时机有两个：一、收到生产者的消息；二、被Follower拉取消息。  
&emsp; 当收到生产者消息时，会用当前偏移量加1来更新LEO，然后取LEO和远程ISR副本中LEO的最小值更新HW。  

    CurrentOffset + 1 -> LEO
    min(LEO, RemoteIsrLEO) -> HW

&emsp; 而当Follower拉取消息时，会更新Leader上存储的Follower副本LEO，然后判断是否需要更新HW，更新的方式和上述相同。  

    FollowerLEO -> RemoteLEO
    min(LEO, RemoteIsrLEO) -> HW

&emsp; 除了这两种正常情况，而当发生故障时，例如Leader宕机，Follower被选为新的Leader，会尝试更新HW。还有副本被踢出ISR时，也会尝试更新HW。  

&emsp; 下面看下更新LEO和HW的示例，假设分区中有两个副本，min.insync.replica=1。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-77.png)  
&emsp; 从上述过程中，可以看到remoteLEO、LeaderHW和FollowerHW的更新发生于Follower更新LEO后的第二轮Fetch请求，而这也意味着，更新需要额外一次Fetch请求。而这也将导致在Leader切换时，会存在数据丢失和数据不一致的问题。下面是数据丢失的示例：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-78.png)  
&emsp; 当B作为Follower已经Fetch了最新的消息，但是在发送第二轮Fetch时，未来得及处理响应，宕机了。当重启时，会根据HW更新LEO，将发生日志截断，消息m1被丢弃。  
&emsp; 这时再发送Fetch请求给A，A宕机了，则B未能同步到消息m1，同时B被选为Leader，而当A重启时，作为Follower同步B的消息时，会根据A的HW值更新HW和LEO，因此由2变成了1，也将发生日志截断，而已发送成功的消息m1将永久丢失。  

&emsp; 数据不一致的情况如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-79.png)  
&emsp; A作为Leader，A已写入m0、m1两条消息，且HW为2，而B作为Follower，只有m0消息，且HW为1。若A、B同时宕机，且B重启时，A还未恢复，则B被选为Leader。  

    集群处于上述这种状态有两种情况可能导致，一、宕机前，B不在ISR中，因此A未待B同步，即更新了HW，且unclear leader为true，允许B成为Leader；二、宕机前，B同步了消息m1，且发送了第二轮Fetch请求，Leader更新HW，但B未将消息m1落地到磁盘，宕机了，当再重启时，消息m1丢失，只剩m0。

&emsp; 在B重启作为Leader之后，收到消息m2。A宕机重启后向成为Leader的B发送Fetch请求，发现自己的HW和B的HW一致，都是2，因此不会进行消息截断，而这也造成了数据不一致。  

#### 1.2.3.1. Leader Epoch 
&emsp; 为了解决HW可能造成的数据丢失和数据不一致问题，Kafka引入了Leader Epoch机制，在每个副本日志目录下都有一个leader-epoch-checkpoint文件，用于保存Leader Epoch信息，其内容示例如下：  

    0 0
    1 300
    2 500

&emsp; 上面每一行为一个Leader Epoch，分为两部分，前者Epoch，表示Leader版本号，是一个单调递增的正整数，每当Leader变更时，都会加1，后者StartOffset，为每一代Leader写入的第一条消息的位移。  
&emsp; 例如第0代Leader写的第一条消息位移为0，而第1代Leader写的第一条消息位移为300，也意味着第0代Leader在写了0-299号消息后挂了，重新选出了新的Leader。下面我们看下Leader Epoch如何工作：  

1. 当副本成为Leader时：  
&emsp; 当收到生产者发来的第一条消息时，会将新的epoch和当前LEO添加到leader-epoch-checkpoint文件中。  
2. 当副本成为Follower时：
    1. 向Leader发送LeaderEpochRequest请求，请求内容中含有Follower当前本地的最新Epoch；
    2. Leader将返回给Follower的响应中含有一个LastOffset，其取值规则为：
        * 若FollowerLastEpoch = LeaderLastEpoch，则取Leader LEO；
        * 否则，取大于FollowerLastEpoch的第一个Leader Epoch中的StartOffset。
    3. Follower在拿到LastOffset后，若LastOffset < LEO，将截断日志；
    4. Follower开始正常工作，发送Fetch请求；

&emsp; 再回顾看下数据丢失和数据不一致的场景，在应用了LeaderEpoch后发生什么改变：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-80.png)  
&emsp; 当B作为Follower已经Fetch了最新的消息，但是发送第二轮Fetch时，未来得及处理响应，宕机了。当重启时，会向A发送LeaderEpochRequest请求。如果A没宕机，由于 FollowerLastEpoch = LeaderLastEpoch，所以将LeaderLEO，即2作为LastOffset给A，又因为LastOffset=LEO，所以不会截断日志。  
&emsp; 这种情况比较简单，而图中所画的情况是A宕机的情况，没返回LeaderEpochRequest的响应的情况。这时候B会被选作Leader，将当前LEO和新的Epoch写进leader-epoch-checkpoint文件中。  
&emsp; 当A作为Follower重启后，发送LeaderEpochRequest请求，包含最新的epoch值0，当B收到请求后，由于FollowerLastEpoch < LeaderLastEpoch，所以会取大于FollowerLastEpoch的第一个Leader Epoch中的StartOffset，即2。当A收到响应时，由于LEO = LastOffset，所以不会发生日志截断，也就不会丢失数据。  
&emsp; 下面是数据不一致情况：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-81.png)  
&emsp; A作为Leader，A已写入m0、m1两条消息，且HW为2，而B作为Follower，只有消息m0，且HW为1，A、B同时宕机。B重启，被选为Leader，将写入新的LeaderEpoch（1, 1）。B开始工作，收到消息m2时。这是A重启，将作为Follower将发送LeaderEpochRequert（FollowerLastEpoch=0），B返回大于FollowerLastEpoch的第一个LeaderEpoch的StartOffset，即1，小于当前LEO值，所以将发生日志截断，并发送Fetch请求，同步消息m2，避免了消息不一致问题。  
&emsp; 你可能会问，m2消息那岂不是丢失了？是的，m2消息丢失了，但这种情况的发送的根本原因在于min.insync.replicas的值设置为1，即没有任何其他副本同步的情况下，就认为m2消息为已提交状态。LeaderEpoch不能解决min.insync.replicas为1带来的数据丢失问题，但是可以解决其所带来的数据不一致问题。而我们之前所说能解决的数据丢失问题，是指消息已经成功同步到Follower上，但因HW未及时更新引起的数据丢失问题。  

## 1.3. kafka可靠性（如何保证消息队列不丢失?）
<!-- 

Kafka消息中间件到底会不会丢消息 
https://mp.weixin.qq.com/s/uxYUEJRTEIULeObRIl209A
Kafka 无消息丢失配置
https://www.kancloud.cn/nicefo71/kafka/1471586

-->
&emsp; Kafka存在丢消息的问题，消息丢失会发生在Broker，Producer和Consumer三种。  

### 1.3.1. Broker端丢失消息
&emsp; Broker丢失消息是由于Kafka本身的原因造成的，kafka为了得到更高的性能和吞吐量，将数据异步批量的存储在磁盘中。消息的刷盘过程，为了提高性能，减少刷盘次数，kafka采用了批量刷盘的做法。即，按照一定的消息量，和时间间隔进行刷盘。这种机制也是由于linux操作系统决定的。将数据存储到linux操作系统种，会先存储到页缓存（Page cache）中，按照时间或者其他条件进行刷盘（从page cache到file），或者通过fsync命令强制刷盘。数据在page cache中时，如果系统挂掉，数据会丢失。  
&emsp; 上图简述了broker写数据以及同步的一个过程。broker写数据只写到PageCache中，而pageCache位于内存。这部分数据在断电后是会丢失的。pageCache的数据通过linux的flusher程序进行刷盘。刷盘触发条件有三：  

* 主动调用sync或fsync函数
* 可用内存低于阀值
* dirty data时间达到阀值。dirty是pagecache的一个标识位，当有数据写入到pageCache时，pagecache被标注为dirty，数据刷盘以后，dirty标志清除。

&emsp; Broker配置刷盘机制，是通过调用fsync函数接管了刷盘动作。从单个Broker来看，pageCache的数据会丢失。  
&emsp; Kafka没有提供同步刷盘的方式。要完全让kafka保证单个broker不丢失消息是做不到的，只能通过调整刷盘机制的参数缓解该情况。比如，减少刷盘间隔，减少刷盘数据量大小。时间越短，性能越差，可靠性越好（尽可能可靠）。这是一个选择题。  
&emsp; 为了解决该问题，kafka通过producer和broker协同处理单个broker丢失参数的情况。一旦producer发现broker消息丢失，即可自动进行retry。除非retry次数超过阀值（可配置），消息才会丢失。此时需要生产者客户端手动处理该情况。那么producer是如何检测到数据丢失的呢？是通过ack机制。  

#### 1.3.1.1. ACK机制与ISR列表  
&emsp; kafka为了保证高可用性，采用了副本机制。  
&emsp; **a) 副本数据同步策略主要有如下两种**  

|方案	|优点	|缺点|
|---|---|---|
|半数以上完成同步，就发送ack|延迟低	|选举新的 leader 时，容忍n台节点的故障，需要2n+1个副本|
|全部完成同步，才发送ack|选举新的 leader 时，容忍n台节点的故障，需要 n+1 个副本|延迟高|

&emsp; Kafka 选择了第二种方案，原因如下：

* 同样为了容忍 n 台节点的故障，第一种方案需要的副本数相对较多，而 Kafka 的每个分区都有大量的数据，第一种方案会造成大量的数据冗余；
* 虽然第二种方案的网络延迟会比较高，但网络延迟对 Kafka 的影响较小。  

&emsp; **b) ISR**   
&emsp; 采用第二种方案之后，设想一下情景：leader 收到数据，所有 follower 都开始同步数据，但有一个 follower 挂了，迟迟不能与 leader 保持同步，那 leader 就要一直等下去，直到它完成同步，才能发送 ack，这个问题怎么解决呢？  
&emsp; leader 维护了一个动态的 in-sync replica set(ISR，保持同步的副本)，意为和 leader 保持同步的 follower 集合。当 ISR 中的follower 完成数据的同步之后，leader 就会给 follower 发送 ack。如果 follower 长时间未向 leader 同步数据，则该 follower 将会被踢出 ISR，该时间阈值由 replica.lag.time.max.ms 参数设定。leader 发生故障之后，就会从 ISR 中选举新的 leader。（之前还有另一个参数，0.9 版本之后 replica.lag.max.messages 参数被移除了）  

&emsp; **c) ack应答机制**  
&emsp; 对于某些不太重要的数据，对数据的可靠性要求不是很高，能够容忍数据的少量丢失，所以没必要等 ISR 中的follower全部接收成功。kafka的request.required.acks 可设置为 1、0、-1 三种情况。  
<!-- 
    acks=0，producer不等待broker的响应，效率最高，但是消息很可能会丢。
    acks=1，leader broker收到消息后，不等待其他follower的响应，即返回ack。也可以理解为ack数为1。此时，如果follower还没有收到leader同步的消息leader就挂了，那么消息会丢失。按照上图中的例子，如果leader收到消息，成功写入PageCache后，会返回ack，此时producer认为消息发送成功。但此时，按照上图，数据还没有被同步到follower。如果此时leader断电，数据会丢失。
    acks=-1，leader broker收到消息后，挂起，等待所有ISR列表中的follower返回结果后，再返回ack。-1等效与all。这种配置下，只有leader写入数据到pagecache是不会返回ack的，还需要所有的ISR返回“成功”才会触发ack。如果此时断电，producer可以知道消息没有被发送成功，将会重新发送。如果在follower收到数据以后，成功返回ack，leader断电，数据将存在于原来的follower中。在重新选举以后，新的leader会持有该部分数据。数据从leader同步到follower，需要2步：
        数据从pageCache被刷盘到disk。因为只有disk中的数据才能被同步到replica。
        数据同步到replica，并且replica成功将数据写入PageCache。在producer得到ack后，哪怕是所有机器都停电，数据也至少会存在于leader的磁盘内。
-->
    
    Kafka 的交付语义？交付语义一般有at least once、at most once和exactly once。kafka 通过 ack 的配置来实现前两种。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-61.png)  
&emsp; 设置为 1 时代表当 Leader 状态的 Partition 接收到消息并持久化时就认为消息发送成功，如果 ISR 列表的 Replica 还没来得及同步消息，Leader 状态的 Partition 对应的 Broker 宕机，则消息有可能丢失。    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-62.png)  
&emsp; 设置为 0 时代表 Producer 发送消息后就认为成功，消息有可能丢失。    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-63.png)  
&emsp; 设置为-1 时，代表 ISR 列表中的所有 Replica 将消息同步完成后才认为消息发送成功；但是如果只存在主 Partition 的时候，Broker 异常时同样会导致消息丢失。所以此时就需要min.insync.replicas参数的配合，该参数需要设定值大于等于 2，当 Partition 的个数小于设定的值时，Producer 发送消息会直接报错。  

&emsp; 上面这个过程看似已经很完美了，但是假设如果消息在同步到部分从Partition 上时，主 Partition 宕机，此时消息会重传，虽然消息不会丢失，但是会造成同一条消息会存储多次。在新版本中 Kafka 提出了幂等性的概念，通过给每条消息设置一个唯一 ID，并且该 ID 可以唯一映射到 Partition 的一个固定位置，从而避免消息重复存储的问题。  

### 1.3.2. Producer端丢失消息  
&emsp; Producer丢失消息，发生在生产者客户端。  
&emsp; 为了提升效率，减少IO，producer在发送数据时可以将多个请求进行合并后发送。被合并的请求咋发送一线缓存在本地buffer中。缓存的方式和前文提到的刷盘类似，producer可以将请求打包成“块”或者按照时间间隔，将buffer中的数据发出。通过buffer可以将生产者改造为异步的方式，而这可以提升发送效率。  
&emsp; 但是，buffer中的数据就是危险的。在正常情况下，客户端的异步调用可以通过callback来处理消息发送失败或者超时的情况，但是，一旦producer被非法的停止了，那么buffer中的数据将丢失，broker将无法收到该部分数据。又或者，当Producer客户端内存不够时，如果采取的策略是丢弃消息（另一种策略是block阻塞），消息也会被丢失。抑或，消息产生（异步产生）过快，导致挂起线程过多，内存不足，导致程序崩溃，消息丢失。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-73.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-73.png)  
&emsp; 根据上图，可以想到几个解决的思路：  

* 异步发送消息改为同步发送消。或者service产生消息时，使用阻塞的线程池，并且线程数有一定上限。整体思路是控制消息产生速度。  
* 扩大Buffer的容量配置。这种方式可以缓解该情况的出现，但不能杜绝。  
* service不直接将消息发送到buffer（内存），而是将消息写到本地的磁盘中（数据库或者文件），由另一个（或少量）生产线程进行消息发送。相当于是在buffer和service之间又加了一层空间更加富裕的缓冲层。  

### 1.3.3. Consumer 端丢失消息
&emsp; Consumer消费消息有下面几个步骤：  

* 接收消息
* 处理消息
* 反馈“处理完毕”（commited）

&emsp; Consumer的消费方式主要分为两种：  

* 自动提交offset，Automatic Offset Committing
* 手动提交offset，Manual Offset Control

&emsp; Consumer自动提交的机制是根据一定的时间间隔，将收到的消息进行commit。commit过程和消费消息的过程是异步的。也就是说，可能存在消费过程未成功（比如抛出异常），commit消息已经提交了。此时消息就丢失了。  

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("group.id", "test");
// 自动提交开关
props.put("enable.auto.commit", "true");
// 自动提交的时间间隔，此处是1s
props.put("auto.commit.interval.ms", "1000");
props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Arrays.asList("foo", "bar"));
while (true) {
        // 调用poll后，1000ms后，消息状态会被改为 committed
  ConsumerRecords<String, String> records = consumer.poll(100);
  for (ConsumerRecord<String, String> record : records)
    insertIntoDB(record); // 将消息入库，时间可能会超过1000ms
```

&emsp; 上面的示例是自动提交的例子。如果此时，insertIntoDB(record)发生异常，消息将会出现丢失。接下来是手动提交的例子：  

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("group.id", "test");
// 关闭自动提交，改为手动提交
props.put("enable.auto.commit", "false");
props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Arrays.asList("foo", "bar"));
final int minBatchSize = 200;
List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
while (true) {
        // 调用poll后，不会进行auto commit
  ConsumerRecords<String, String> records = consumer.poll(100);
  for (ConsumerRecord<String, String> record : records) {
    buffer.add(record);
  }
  if (buffer.size() >= minBatchSize) {
    insertIntoDb(buffer);
                // 所有消息消费完毕以后，才进行commit操作
    consumer.commitSync();
    buffer.clear();
  }
```
&emsp; 将提交类型改为手动以后，可以保证消息“至少被消费一次”(at least once)。但此时可能出现重复消费的情况，重复消费不属于本篇讨论范围。  
&emsp; 上面两个例子，是直接使用Consumer的High level API，客户端对于offset等控制是透明的。也可以采用Low level API的方式，手动控制offset，也可以保证消息不丢，不过会更加复杂。  

```java
 try {
     while(running) {
         ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
         for (TopicPartition partition : records.partitions()) {
             List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
             for (ConsumerRecord<String, String> record : partitionRecords) {
                 System.out.println(record.offset() + ": " + record.value());
             }
             long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
             // 精确控制offset
             consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
         }
     }
 } finally {
   consumer.close();
 }
```

## 1.4. kafka数据一致性，通过HW来保证  
&emsp; 由于并不能保证 Kafka 集群中每时每刻 follower 的长度都和 leader 一致（即数据同步是有时延的），那么当leader 挂掉选举某个 follower 为新的 leader 的时候（原先挂掉的 leader 恢复了成为了 follower），可能会出现leader 的数据比 follower 还少的情况。为了解决这种数据量不一致带来的混乱情况，Kafka 提出了以下概念：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-27.png)  

* LEO（Log End Offset）：指的是每个副本最后一个offset；  
* HW（High Wather）：指的是消费者能见到的最大的 offset，ISR 队列中最小的 LEO。  

&emsp; 消费者和 leader 通信时，只能消费 HW 之前的数据，HW 之后的数据对消费者不可见。  

&emsp; 针对这个规则：  

* 当follower发生故障时：follower 发生故障后会被临时踢出 ISR，待该 follower 恢复后，follower 会读取本地磁盘记录的上次的 HW，并将 log 文件高于 HW 的部分截取掉，从 HW 开始向 leader 进行同步。等该 follower 的 LEO 大于等于该 Partition 的 HW，即 follower 追上 leader 之后，就可以重新加入 ISR 了。
* 当leader发生故障时：leader 发生故障之后，会从 ISR 中选出一个新的 leader，之后，为保证多个副本之间的数据一致性，其余的 follower 会先将各自的 log 文件高于 HW 的部分截掉，然后从新的 leader 同步数据。

&emsp; 所以数据一致性并不能保证数据不丢失或者不重复，这是由 ack 控制的。HW 规则只能保证副本之间的数据一致性！



## 1.5. 如何让 Kafka 的消息有序？  
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

