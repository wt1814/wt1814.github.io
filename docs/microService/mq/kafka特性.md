

<!-- TOC -->

- [1. kafka特性](#1-kafka特性)
    - [1.1. Kafka的高效读写机制](#11-kafka的高效读写机制)
        - [1.1.1. 顺序读写](#111-顺序读写)
            - [1.1.1.1. 顺序写入](#1111-顺序写入)
            - [1.1.1.2. Memory Mapped Files](#1112-memory-mapped-files)
        - [1.1.2. 基于 Sendfile 实现零拷贝（Zero Copy）](#112-基于-sendfile-实现零拷贝zero-copy)
        - [1.1.3. 批量发送](#113-批量发送)
        - [1.1.4. 数据压缩](#114-数据压缩)
        - [1.1.5. 总结](#115-总结)
    - [1.2. 关于 Kafka 和其他消息队列的区别](#12-关于-kafka-和其他消息队列的区别)
    - [1.3. kafka可靠性，如何保证消息队列不丢失](#13-kafka可靠性如何保证消息队列不丢失)
        - [1.3.1. 关于 ACK 机制](#131-关于-ack-机制)
        - [1.3.2. 关于设置分区](#132-关于设置分区)
        - [1.3.3. 关闭 unclean leader 选举](#133-关闭-unclean-leader-选举)
        - [1.3.4. 关于发送消息](#134-关于发送消息)
        - [1.3.5. Consumer 端丢失消息](#135-consumer-端丢失消息)
        - [1.3.6. 故障处理](#136-故障处理)
    - [1.4. kafka数据一致性，通过HW来保证](#14-kafka数据一致性通过hw来保证)
    - [1.5. kafa高可用性](#15-kafa高可用性)
    - [如何让 Kafka 的消息有序？](#如何让-kafka-的消息有序)

<!-- /TOC -->


# 1. kafka特性


<!-- 

https://mp.weixin.qq.com/s?__biz=MzIzNTIzNzYyNw==&mid=2247484048&idx=1&sn=2de06942948b02842fd042e7783cbc61&chksm=e8eb7b04df9cf2121a98984fae0207d422b9d643a2c3ff4d7c91cabfe2e8b4d530a62c376035&scene=178&cur_album_id=1338677566013800448#rd

https://mp.weixin.qq.com/s?__biz=MzIzNTIzNzYyNw==&mid=2247484078&idx=1&sn=3b0e754674804d61bde1df498f0bd092&chksm=e8eb7b3adf9cf22c370346b0eb2aa516f517daf4d0fa5977f41412d64f22cfe86f8eec90c58c&scene=178&cur_album_id=1338677566013800448#rd

Kafka 面试必问：聊聊 acks 参数对消息持久化的影响！ 
https://mp.weixin.qq.com/s/PePsJzuKEIfQpCH1KbxrCg

Kafka 副本机制
https://juejin.im/post/6844903950009794567

-->
<!-- 
https://mp.weixin.qq.com/s/kguKr_k-BrcQz4G5gag8gg
-->

## 1.1. Kafka的高效读写机制  
如何提升 Producer 的性能？批量，异步，压缩  
<!-- 

https://mp.weixin.qq.com/s/nSa2CPjbMFdOsYB2Dt0kYg
如何实现高吞吐量
https://blog.csdn.net/BeiisBei/article/details/104360272

-->

&emsp; Kafka 的消息是保存或缓存在磁盘上的，一般认为在磁盘上读写数据是会降低性能的，因为寻址会比较消耗时间，但是实际上，Kafka 的特性之一就是高吞吐率。Kafka 之所以能这么快，是因为：「顺序写磁盘、大量使用内存页 、零拷贝技术的使用」..  


### 1.1.1. 顺序读写  
<!-- 
kafka的消息是不断追加到文件中的，这个特性使kafka可以充分利用磁盘的顺序读写性能

顺序读写不需要硬盘磁头的寻道时间，只需很少的扇区旋转时间，所以速度远快于随机读写

生产者负责写入数据，Kafka会将消息持久化到磁盘，保证不会丢失数据，Kafka采用了俩个技术提高写入的速度。

1.顺序写入：在大学的计算机组成（划重点）里我们学过，硬盘是机械结构，需要指针寻址找到存储数据的位置，所以，如果是随机IO，磁盘会进行频繁的寻址，导致写入速度下降。Kafka使用了顺序IO提高了磁盘的写入速度，Kafka会将数据顺序插入到文件末尾，消费者端通过控制偏移量来读取消息，这样做会导致数据无法删除，时间一长，磁盘空间会满，kafka提供了2种策略来删除数据：基于时间删除和基于partition文件的大小删除。

2.Memory Mapped Files：这个和Java NIO中的内存映射基本相同，在大学的计算机原理里我们学过（划重点），mmf直接利用操作系统的Page来实现文件到物理内存的映射，完成之后对物理内存的操作会直接同步到硬盘。mmf通过内存映射的方式大大提高了IO速率，省去了用户空间到内核空间的复制。它的缺点显而易见--不可靠，当发生宕机而数据未同步到硬盘时，数据会丢失，Kafka提供了produce.type参数来控制是否主动的进行刷新，如果kafka写入到mmp后立即flush再返回给生产者则为同步模式，反之为异步模式。
-->

&emsp; 下面从数据写入和读取两方面分析，为大家分析下为什么 Kafka 速度这么快。  

    数据写入 Kafka 会把收到的消息都写入到硬盘中，它绝对不会丢失数据。为了优化写入速度 Kafka 采用了两个技术， 顺序写入和 Memory Mapped File 。

#### 1.1.1.1. 顺序写入  
&emsp; Kafka 会把收到的消息都写入到硬盘中，它绝对不会丢失数据。为了优化写入速度 Kafka 采用了两个技术， 顺序写入和 MMFile（Memory Mapped File）。  
&emsp; 磁盘读写的快慢取决于你怎么使用它，也就是顺序读写或者随机读写。在顺序读写的情况下，磁盘的顺序读写速度和内存持平。因为硬盘是机械结构，每次读写都会寻址->写入，其中寻址是一个“机械动作”，它是最耗时的。「所以硬盘最讨厌随机 I/O，最喜欢顺序 I/O」。为了提高读写硬盘的速度，Kafka 就是使用顺序 I/O。  
&emsp; 而且 Linux 对于磁盘的读写优化也比较多，包括 read-ahead 和 write-behind，磁盘缓存等。  
&emsp; 如果在内存做这些操作的时候，一个是 Java 对象的内存开销很大，另一个是随着堆内存数据的增多，Java 的 GC 时间会变得很长。  
&emsp; 使用磁盘操作有以下几个好处：  

* 磁盘顺序读写速度超过内存随机读写。
* JVM 的 GC 效率低，内存占用大。使用磁盘可以避免这一问题。
* 系统冷启动后，磁盘缓存依然可用。

&emsp; 下图就展示了 Kafka 是如何写入数据的， 每一个 Partition 其实都是一个文件 ，收到消息后 Kafka 会把数据插入到文件末尾（虚框部分）：
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-23.png)  
&emsp; 这种方法有一个缺陷——没有办法删除数据 ，所以 Kafka 是不会删除数据的，它会把所有的数据都保留下来，每个 消费者（Consumer）对每个 Topic 都有一个 Offset 用来表示读取到了第几条数据 。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-24.png)  
&emsp; 一般情况下 Offset 由客户端 SDK 负责保存 ，会保存到 Zookeeper 里面 。关于存在硬盘中的消息，Kafka 也有它的解决方法，可以基于时间和 Partition 文件的大小，正常 Kafka 是默认七天的保存，也可以通过命令来修改，以 users topic 为例。  

    修改kafka 7天 默认保存周期
    kafka-topics.sh --zookeeper 6 --alter --topic users --config retention.ms=100000

&emsp; 所以，为了避免磁盘被撑满的情况，Kakfa 提供了两种策略来删除数据：  

1. 「基于时间」 （默认七天）  
2. 「基于 Partition 文件大小」  

#### 1.1.1.2. Memory Mapped Files  
&emsp; 这个和Java NIO中的内存映射基本相同，在大学的计算机原理里我们学过（划重点），mmf （Memory Mapped Files）直接利用操作系统的Page来实现文件到物理内存的映射，完成之后对物理内存的操作会直接同步到硬盘。mmf 通过内存映射的方式大大提高了IO速率，省去了用户空间到内核空间的复制。它的缺点显而易见--不可靠，当发生宕机而数据未同步到硬盘时，数据会丢失，Kafka 提供了produce.type参数来控制是否主动的进行刷新，如果 Kafka 写入到 mmf 后立即flush再返回给生产者则为同步模式，反之为异步模式。  
&emsp; Kafka 提供了一个参数 producer.type 来控制是不是主动 Flush：  

* 如果 Kafka 写入到 mmf 之后就立即 Flush，然后再返回 Producer 叫同步 (Sync)。
* 如果 Kafka 写入 mmf 之后立即返回 Producer 不调用 Flush 叫异步 (Async)。

    数据读取 Kafka 在读取磁盘时做了哪些优化？  

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

### 1.1.3. 批量发送
&emsp; Kafka允许进行批量发送消息，producter发送消息的时候，可以将消息缓存在本地，等到了固定条件发送到 Kafka 。  

* 等消息条数到固定条数。  
* 一段时间发送一次。  

### 1.1.4. 数据压缩
&emsp; Kafka还支持对消息集合进行压缩，Producer可以通过GZIP或Snappy格式对消息集合进行压缩。压缩的好处就是减少传输的数据量，减轻对网络传输的压力。  
&emsp; Producer压缩之后，在Consumer需进行解压，虽然增加了CPU的工作，但在对大数据处理上，瓶颈在网络上而不是CPU，所以这个成本很值得。  
&emsp; 注意：「批量发送」和「数据压缩」一起使用，单条做数据压缩的话，效果不明显！  

### 1.1.5. 总结  
&emsp; 以上，便是Apache Kafka虽然使用了硬盘存储，但是仍然可以速度很快的原因。它把所有的消息都变成一个批量的文件，并且进行合理的批量压缩，减少网络 IO 损耗，通过 mmap 提高 I/O 速度。写入数据的时候由于单个 Partion 是末尾添加，所以速度最优；读取数据的时候配合 Sendfile 直接暴力输出。  

## 1.2. 关于 Kafka 和其他消息队列的区别  
&emsp; Kafka的设计目标是高吞吐量，它比其它消息系统快的原因体现在以下几方面：  
1. Kafka操作的是序列文件I / O（序列文件的特征是按顺序写，按顺序读），为保证顺序，Kafka强制点对点的按顺序传递消息，这意味着，一个consumer在消息流（或分区）中只有一个位置。  
2. Kafka不保存消息的状态，即消息是否被“消费”。一般的消息系统需要保存消息的状态，并且还需要以随机访问的形式更新消息的状态。而Kafka 的做法是保存Consumer在Topic分区中的位置offset，在offset之前的消息是已被“消费”的，在offset之后则为未“消费”的，并且offset是可以任意移动的，这样就消除了大部分的随机IO。
3. Kafka支持点对点的批量消息传递。  
4. Kafka的消息存储在OS pagecache（页缓存，page cache的大小为一页，通常为4K，在Linux读写文件时，它用于缓存文件的逻辑内容，从而加快对磁盘上映像和数据的访问）。  

    RabbitMQ:分布式，支持多种MQ协议，重量级；
    ActiveMQ：与RabbitMQ类似；
    ZeroMQ：以库的形式提供，使用复杂，无持久化；
    Redis：单机、纯内存性好，持久化较差； 
    Kafka：分布式，消息不是使用完就丢失【较长时间持久化】，吞吐量高【高性能】，轻量灵活



## 1.3. kafka可靠性，如何保证消息队列不丢失

Producer 如何保证数据发送不丢失？ack 机制，重试机制  

### 1.3.1. 关于 ACK 机制

Kafka 的交付语义？

    交付语义一般有at least once、at most once和exactly once。kafka 通过 ack 的配置来实现前两种。  
    
<!-- 

为保证 producer 发送的数据，能可靠的发送到指定的 topic，topic 的每个 partition 收到 producer 数据后，都需要向 producer 发送 ack（acknowledgement确认收到），如果 producer 收到 ack，就会进行下一轮的发送，否则重新发送数据。  
a) 副本数据同步策略主要有如下两种  
方案	优点	缺点
半数以上完成同步，就发送ack	延迟低	选举新的 leader 时，容忍n台节点的故障，需要2n+1个副本
全部完成同步，才发送ack	选举新的 leader 时，容忍n台节点的故障，需要 n+1 个副本	延迟高

Kafka 选择了第二种方案，原因如下：

* 同样为了容忍 n 台节点的故障，第一种方案需要的副本数相对较多，而 Kafka 的每个分区都有大量的数据，第一种方案会造成大量的数据冗余；
* 虽然第二种方案的网络延迟会比较高，但网络延迟对 Kafka 的影响较小。  

b) ISR

采用第二种方案之后，设想一下情景：leader 收到数据，所有 follower 都开始同步数据，但有一个 follower 挂了，迟迟不能与 leader 保持同步，那 leader 就要一直等下去，直到它完成同步，才能发送 ack，这个问题怎么解决呢？

leader 维护了一个动态的 in-sync replica set(ISR)，意为和 leader 保持同步的 follower 集合。当 ISR 中的follower 完成数据的同步之后，leader 就会给 follower 发送 ack。如果 follower 长时间未向 leader 同步数据，则该 follower 将会被踢出 ISR，该时间阈值由 replica.lag.time.max.ms 参数设定。leader 发生故障之后，就会从 ISR 中选举新的 leader。（之前还有另一个参数，0.9 版本之后 replica.lag.max.messages 参数被移除了）
c) ack应答机制

对于某些不太重要的数据，对数据的可靠性要求不是很高，能够容忍数据的少量丢失，所以没必要等 ISR 中的follower全部接收成功。

所以Kafka为用户提供了三种可靠性级别，用户根据对可靠性和延迟的要求进行权衡，选择以下的acks 参数配置

    0：producer 不等待 broker 的 ack，这一操作提供了一个最低的延迟，broker 一接收到还没有写入磁盘就已经返回，当 broker 故障时有可能丢失数据；

    1：producer 等待 broker 的 ack，partition 的 leader 落盘成功后返回 ack，如果在 follower 同步成功之前 leader 故障，那么将会丢失数据；

    -1（all）：producer 等待 broker 的 ack，partition 的 leader 和 follower 全部落盘成功后才返回 ack。但是 如果在 follower 同步完成后，broker 发送 ack 之前，leader 发生故障，那么就会造成数据重复。

-->

&emsp; 关于 ACK 机制 ，不了解的小伙伴，可以看这里：Kafka 架构深入 ，通过 ACK 机制保证消息送达。Kafka 采用的是至少一次（At least once），消息不会丢，但是可能会重复传输。  
&emsp; acks 的默认值即为1，代表我们的消息被leader副本接收之后就算被成功发送。我们可以配置 acks = all ，代表则所有副本都要接收到该消息之后该消息才算真正成功被发送。  

---
&emsp; 为保证生产者发送的数据，能可靠的发送到指定的topic，topic的每个partition收到生产者发送的数据后，都需要向生产者发送ack（确认收到），如果生产者收到ack，就会进行下一轮的发送，否则重新发送数据。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-25.png)  
那么kafka什么时候向生产者发送ack  
确保follower和leader同步完成，leader在发送ack给生产者，这样才能确保leader挂掉之后，能再follower中选举出新的leader后，数据不会丢失  
那多少个follower同步完成后发送ack  
方案1：半数已经完成同步，就发送ack  
方案2：全部完成同步，才发送ack（kafka采用这种方式）  
采用第二种方案后，设想以下场景，leader收到数据，所有的follower都开始同步数据，但是有一个follower因为某种故障，一直无法完成同步，那leader就要一直等下，直到他同步完成，才能发送ack，这样就非常影响效率，这个问题怎么解决？  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-26.png)  
Leader维护了一个动态的ISR列表（同步副本的作用），只需要这个列表的中的follower和leader同步；当ISR中的follower完成数据的同步之后，leader就会给生产者发送ack，如果follower长时间未向leader同步数据，则该follower将被剔除ISR，这个时间阈值也是自定义的；同样leader故障后，就会从ISR中选举新的leader

怎么选择ISR的节点呢？  

首先通信的时间要快，要和leader要可以很快的完成通信，这个时间默认是10s  

然后就看leader数据差距，消息条数默认是10000条（后面版本被移除）  

为什么移除：因为kafka发送消息是批量发送的，所以会一瞬间leader接受完成，但是follower还没有拉取，所以会频繁的踢出加入ISR，这个数据会保存到zk和内存中，所以会频繁的更新zk和内存。  

 

但是对于某些不太重要的数据，对数据的可靠性要求不是很高，能够容忍数据的少量丢失，所以没必要等ISR中的follower全部接受成功  

所以kafka为用户提供了三种可靠性级别，用户可以根据可靠性和延迟进行权衡，这个设置在kafka的生成中设置：acks参数设置  

A、acks为0  

生产者不等ack，只管往topic丢数据就可以了，这个丢数据的概率非常高  

B、ack为1  

Leader落盘后就会返回ack，会有数据丢失的现象，如果leader在同步完成后出现故障，则会出现数据丢失  

C、ack为-1（all）  

Leader和follower（ISR）落盘才会返回ack，会有数据重复现象，如果在leader已经写完成，且follower同步完成，但是在返回ack的出现故障，则会出现数据重复现象；极限情况下，这个也会有数据丢失的情况，比如follower和leader通信都很慢，所以ISR中只有一个leader节点，这个时候，leader完成落盘，就会返回ack，如果此时leader故障后，就会导致丢失数据  



### 1.3.2. 关于设置分区
&emsp; 为了保证 leader 副本能有 follower 副本能同步消息，一般会为 topic 设置 replication.factor >= 3。这样就可以保证每个 分区(partition) 至少有 3 个副本，以确保消息队列的安全性。  

### 1.3.3. 关闭 unclean leader 选举
&emsp; 生产者发送的消息会被发送到 leader 副本，然后 follower 副本才能从 leader 副本中拉取消息进行同步。多个 follower 副本之间的消息同步情况不一样，当配置了 unclean.leader.election.enable = false 的话，当 leader 副本发生故障时就不会从 follower 副本中和 leader 同步程度达不到要求的副本中选择出 leader ，这样降低了消息丢失的可能性。  

### 1.3.4. 关于发送消息
&emsp; 为了得到更好的性能，Kafka 支持在生产者一侧进行本地buffer，也就是累积到一定的条数才发送，如果这里设置不当是会丢消息的。  
&emsp; 生产者端设置：producer.type=async, sync，默认是 sync。  
&emsp; 当设置为 async，会大幅提升性能，因为生产者会在本地缓冲消息，并适时批量发送。  
&emsp; 如果对可靠性要求高，那么这里可以设置为 sync 同步发送。  
&emsp; 一般时候还需要设置：min.insync.replicas> 1 ，消息至少要被写入到这么多副本才算成功，也是提升数据持久性的一个参数，与acks配合使用。  
但如果出现两者相等， 还需要设置 replication.factor = min.insync.replicas + 1 ，避免在一个副本挂掉，整个分区无法工作的情况！

### 1.3.5. Consumer 端丢失消息
&emsp; consumer端丢失消息的情形比较简单：  
&emsp; 如果在消息处理完成前就提交了offset，那么就有可能造成数据的丢失。  
&emsp; 由于Kafka consumer默认是自动提交位移的，所以在后台提交位移前一定要保证消息被正常处理了，因此不建议采用很重的处理逻辑，如果处理耗时很长，则建议把逻辑放到另一个线程中去做。  
&emsp; 为了避免数据丢失，现给出几点建议：设置 enable.auto.commit=false  
&emsp; 关闭自动提交位移，在消息被完整处理之后再手动提交位移。  

### 1.3.6. 故障处理  
由于我们并不能保证 Kafka 集群中每时每刻 follower 的长度都和 leader 一致（即数据同步是有时延的），那么当leader 挂掉选举某个 follower 为新的 leader 的时候（原先挂掉的 leader 恢复了成为了 follower），可能会出现leader 的数据比 follower 还少的情况。为了解决这种数据量不一致带来的混乱情况，Kafka 提出了以下概念：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-27.png)  
* LEO（Log End Offset）：指的是每个副本最后一个offset；  
* HW（High Wather）：指的是消费者能见到的最大的 offset，ISR 队列中最小的 LEO。  

消费者和 leader 通信时，只能消费 HW 之前的数据，HW 之后的数据对消费者不可见。  

针对这个规则：  

* 当follower发生故障时：follower 发生故障后会被临时踢出 ISR，待该 follower 恢复后，follower 会读取本地磁盘记录的上次的 HW，并将 log 文件高于 HW 的部分截取掉，从 HW 开始向 leader 进行同步。等该 follower 的 LEO 大于等于该 Partition 的 HW，即 follower 追上 leader 之后，就可以重新加入 ISR 了。
* 当leader发生故障时：leader 发生故障之后，会从 ISR 中选出一个新的 leader，之后，为保证多个副本之间的数据一致性，其余的 follower 会先将各自的 log 文件高于 HW 的部分截掉，然后从新的 leader 同步数据。

所以数据一致性并不能保证数据不丢失或者不重复，这是由 ack 控制的。HW 规则只能保证副本之间的数据一致性！


## 1.4. kafka数据一致性，通过HW来保证  

Kafka 的消息是否是有序的？  

    Topic 级别无序，Partition 有序  

Kafka 在 Topic 级别本身是无序的，只有 partition 上才有序，所以为了保证处理顺序，可以自定义分区器，将需顺序处理的数据发送到同一个 partition

---
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-28.png)  

**HW保证数据存储的一致性**  

A、Follower故障

Follower发生故障后会被临时提出LSR，待该follower恢复后，follower会读取本地的磁盘记录的上次的HW，并将该log文件高于HW的部分截取掉，从HW开始想leader进行同步，等该follower的LEO大于等于该Partition的hw，即follower追上leader后，就可以重新加入LSR

B、Leader故障

Leader发生故障后，会从ISR中选出一个新的leader，之后，为了保证多个副本之间的数据一致性，其余的follower会先将各自的log文件高于hw的部分截掉（新leader自己不会截掉），然后从新的leader同步数据

注意：这个是为了保证多个副本间的数据存储的一致性，并不能保证数据不丢失或者不重复

 
**精准一次（幂等性），保证数据不重复**  
Ack设置为-1，则可以保证数据不丢失，但是会出现数据重复（at least once）

Ack设置为0，则可以保证数据不重复，但是不能保证数据不丢失（at most once）

但是如果鱼和熊掌兼得，该怎么办？这个时候就就引入了Exactl once（精准一次）

 

在0.11版本后，引入幂等性解决kakfa集群内部的数据重复，在0.11版本之前，在消费者处自己做处理

如果启用了幂等性，则ack默认就是-1，kafka就会为每个生产者分配一个pid，并未每条消息分配seqnumber，如果pid、partition、seqnumber三者一样，则kafka认为是重复数据，就不会落盘保存；但是如果生产者挂掉后，也会出现有数据重复的现象；所以幂等性解决在单次会话的单个分区的数据重复，但是在分区间或者跨会话的是数据重复的是无法解决的



## 1.5. kafa高可用性

<!-- 

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



## 如何让 Kafka 的消息有序？  
Kafka 在 Topic 级别本身是无序的，只有 partition 上才有序，所以为了保证处理顺序，可以自定义分区器，将需顺序处理的数据发送到同一个 partition  


