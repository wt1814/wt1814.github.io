

<!-- TOC -->

- [1. kafka特性](#1-kafka特性)
    - [1.1. 高性能(读写机制)](#11-高性能读写机制)
        - [1.1.1. 顺序读写](#111-顺序读写)
        - [1.1.2. ~~基于Sendfile实现零拷贝(Zero Copy)~~](#112-基于sendfile实现零拷贝zero-copy)
    - [1.2. 高可用与数据一致性(副本机制)](#12-高可用与数据一致性副本机制)
    - [1.3. 可靠性](#13-可靠性)
        - [1.3.1. ★★★消费语义介绍](#131-★★★消费语义介绍)
        - [1.3.2. 可靠性(如何保证消息队列不丢失?)](#132-可靠性如何保证消息队列不丢失)
        - [1.3.3. 幂等（重复消费）和事务](#133-幂等重复消费和事务)
    - [1.4. 如何让Kafka的消息有序？](#14-如何让kafka的消息有序)

<!-- /TOC -->


# 1. kafka特性
<!-- 
面试官：说说Kafka处理请求的全流程 
https://mp.weixin.qq.com/s/LEmybNmD5XwkBtcTPHcaEA
kafka——高效读写数据
https://www.jianshu.com/p/ce8253609b6b
-->

## 1.1. 高性能(读写机制)  
<!--
Kafka的特性之一就是高吞吐率，但是Kafka的消息是保存或缓存在磁盘上的，一般认为在磁盘上读写数据是会降低性能的，但是Kafka即使是普通的服务器，Kafka也可以轻松支持每秒百万级的写入请求，超过了大部分的消息中间件，这种特性也使得Kafka在日志处理等海量数据场景广泛应用。Kafka会把收到的消息都写入到硬盘中，防止丢失数据。为了优化写入速度Kafka采用了两个技术顺序写入和MMFile 。

因为硬盘是机械结构，每次读写都会寻址->写入，其中寻址是一个“机械动作”，它是最耗时的。所以硬盘最讨厌随机I/O，最喜欢顺序I/O。为了提高读写硬盘的速度，Kafka就是使用顺序I/O。这样省去了大量的内存开销以及节省了IO寻址的时间。但是单纯的使用顺序写入，Kafka的写入性能也不可能和内存进行对比，因此Kafka的数据并不是实时的写入硬盘中 。

Kafka充分利用了现代操作系统分页存储来利用内存提高I/O效率。Memory Mapped Files(后面简称mmap)也称为内存映射文件，在64位操作系统中一般可以表示20G的数据文件，它的工作原理是直接利用操作系统的Page实现文件到物理内存的直接映射。完成MMP映射后，用户对内存的所有操作会被操作系统自动的刷新到磁盘上，极大地降低了IO使用率。

Kafka服务器在响应客户端读取的时候，底层使用ZeroCopy技术，直接将磁盘无需拷贝到用户空间，而是直接将数据通过内核空间传递输出，数据并没有抵达用户空间。

-->
&emsp; Kafka的消息是保存或缓存在磁盘上的，一般认为在磁盘上读写数据是会降低性能的，因为寻址会比较消耗时间，但是实际上，Kafka的特性之一就是高吞吐率。 **Kafka之所以能这么快，是因为：「顺序写磁盘、大量使用内存页、零拷贝技术的使用」..**  

&emsp; ~~数据写入Kafka会把收到的消息都写入到硬盘中。为了优化写入速度，Kafka使用了：顺序写入和Memory Mapped File 。~~  

### 1.1.1. 顺序读写  
&emsp; kafka的消息是不断追加到文件中的，这个特性使kafka可以充分利用磁盘的顺序读写性能。Kafka会将数据顺序插入到文件末尾，消费者端通过控制偏移量来读取消息，这样做会导致数据无法删除，时间一长，磁盘空间会满，kafka提供了2种策略来删除数据：基于时间删除和基于partition文件的大小删除。  

    顺序读写不需要硬盘磁头的寻道时间，只需很少的扇区旋转时间，所以速度远快于随机读写。

### 1.1.2. ~~基于Sendfile实现零拷贝(Zero Copy)~~  
<!-- Kafka 提供了produce.type参数来控制是否主动的进行刷新，如果 Kafka 写入到 mmf 后立即flush再返回给生产者则为同步模式，反之为异步模式。 -->  
<!-- 
在这之前先来了解一下零拷贝(直接让操作系统的 Cache 中的数据发送到网卡后传输给下游的消费者)：平时从服务器读取静态文件时，服务器先将文件从复制到内核空间，再复制到用户空间，最后再复制到内核空间并通过网卡发送出去，而零拷贝则是直接从内核到内核再到网卡，省去了用户空间的复制。

Kafka把所有的消息存放到一个文件中，当消费者需要数据的时候直接将文件发送给消费者，比如10W的消息共10M，全部发送给消费者，10M的消息在内网中传输是非常快的，假如需要1s，那么kafka的tps就是10w。Zero copy对应的是Linux中sendfile函数，这个函数会接受一个offsize来确定从哪里开始读取。现实中，不可能将整个文件全部发给消费者，他通过消费者传递过来的偏移量来使用零拷贝读取指定内容的数据返回给消费者。

在Linux kernel2.2 之后出现了一种叫做"零拷贝(zero-copy)"系统调用机制，就是跳过“用户缓冲区”的拷贝，建立一个磁盘空间和内存的直接映射，数据不再复制到“用户态缓冲区”，系统上下文切换减少为2次，可以提升一倍的性能。

--------
&emsp; 这个和Java NIO中的内存映射基本相同，mmf (Memory Mapped Files)直接利用操作系统的Page来实现文件到物理内存的映射，完成之后对物理内存的操作会直接同步到硬盘。**mmf通过内存映射的方式大大提高了IO速率，省去了用户空间到内核空间的复制。它的缺点显而易见，不可靠，当发生宕机而数据未同步到硬盘时，数据会丢失。**  

&emsp; Kafka 提供了一个参数 producer.type 来控制是不是主动 Flush：  

* 如果 Kafka 写入到 mmf 之后就立即 Flush，然后再返回 Producer 叫同步 (Sync)。  
* 如果 Kafka 写入 mmf 之后立即返回 Producer 不调用 Flush 叫异步 (Async)。  
-->

&emsp; [IO性能优化之零拷贝](/docs/microService/communication/Netty/zeroCopy.md)  

&emsp; Kafka 的数据传输通过TransportLayer来完成，其子类 PlaintextTransportLayer 通过Java NIO的FileChannel的transferTo和transferFrom方法实现零拷贝。  

```java
@Override
public long transferFrom(FileChannel fileChannel, long position, long count) throws IOException {
   return fileChannel.transferTo(position, count, socketChannel);
}
```

&emsp; Java NIO 的 FileChannel 的 transferTo 和 transferFrom 方法是基于sendfile方式实现零拷贝。  

&emsp; 注：transferTo 和 transferFrom 并不保证一定能使用零拷贝。 **<font color = "clime">实际上是否能使用零拷贝与操作系统相关，如果操作系统提供 sendfile 这样的零拷贝系统调用，则这两个方法会通过这样的系统调用充分利用零拷贝的优势，否则并不能通过这两个方法本身实现零拷贝。</font>**  


## 1.2. 高可用与数据一致性(副本机制)
[kafka副本机制](/docs/microService/mq/kafka/kafkaReplica.md)  

## 1.3. 可靠性
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-118.png)  

### 1.3.1. ★★★消费语义介绍  
<!--
&emsp; Kafka在producer和consumer之间提供的语义保证。显然，Kafka可以提供的消息交付语义保证有多种：  

* At most once——消息可能会丢失但绝不重传。
* At least once——消息可以重传但绝不丢失。
* Exactly once——每一条消息只被传递一次。

&emsp; 值得注意的是，这个问题被分成了两部分：发布消息的持久性保证和消费消息的保证。 
-->
&emsp; **消息传递语义介绍：**  
&emsp; 消息传递语义message delivery semantic，简单说就是消息传递过程中消息传递的保证性。主要分为三种：  

* at most once：最多一次。消息可能丢失也可能被处理，但最多只会被处理一次。  
* at least once：至少一次。消息不会丢失，但可能被处理多次。可能重复，不会丢失。  
* exactly once：精确传递一次。消息被处理且只会被处理一次。不丢失不重复就一次。  


&emsp; at most once，最多一次，可以理解为可能发生消息丢失；at least once，至少一次，可以理解为可能发生重复消费。kafka通过ack的配置来实现这两种。  
&emsp; 理想情况下肯定是希望系统的消息传递是严格exactly once，也就是保证不丢失、只会被处理一次，但是很难做到。 **exactly once也被称为幂等性。**  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-119.png)  


### 1.3.2. 可靠性(如何保证消息队列不丢失?)  
[kafka如何保证消息队列不丢失?](/docs/microService/mq/kafka/kafkaReliability.md)  

### 1.3.3. 幂等（重复消费）和事务
[kafka幂等和事务](/docs/microService/mq/kafka/kafkaTraction.md)

## 1.4. 如何让Kafka的消息有序？  
&emsp; Kafka无法做到消息全局有序，只能做到 Partition 维度的有序。所以如果想要消息有序，就需要从Partition维度入手。一般有两种解决方案。

* 单Partition，单Consumer。通过此种方案强制消息全部写入同一个Partition内，但是同时也牺牲掉了Kafka高吞吐的特性了，所以一般不会采用此方案。  
* **多Partition，多Consumer，指定key使用特定的Hash策略，使其消息落入指定的Partition 中，从而保证相同的key对应的消息是有序的。** 此方案也是有一些弊端，比如当Partition个数发生变化时，相同的key对应的消息会落入到其他的Partition上，所以一旦确定Partition个数后就不能在修改Partition个数了。  
