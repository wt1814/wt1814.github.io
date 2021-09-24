<!-- TOC -->

- [1. kafka特性](#1-kafka特性)
    - [1.1. 高性能(读写机制) ，Kafka为什么吞吐量大、速度快？](#11-高性能读写机制-kafka为什么吞吐量大速度快)
    - [1.2. 高可用与数据一致性(副本机制)](#12-高可用与数据一致性副本机制)
    - [1.3. 可靠性](#13-可靠性)
        - [1.3.1. ★★★消费语义介绍](#131-★★★消费语义介绍)
        - [1.3.2. 可靠性（如何保证消息队列不丢失?）](#132-可靠性如何保证消息队列不丢失)
        - [1.3.3. 幂等（重复消费）和事务](#133-幂等重复消费和事务)
    - [1.4. 如何让Kafka的消息有序？](#14-如何让kafka的消息有序)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  

* 高性能：磁盘I/O-顺序读写、基于Sendfile实现零拷贝。  
* 高可用：Kafka副本机制。  
* 可靠性：副本的一致性保证、可以保证消息队列不丢失、幂等（重复消费）和事务。  
* 如何让Kafka的消息有序？  

# 1. kafka特性
<!-- 
面试官：说说Kafka处理请求的全流程 
https://mp.weixin.qq.com/s/LEmybNmD5XwkBtcTPHcaEA

-->

## 1.1. 高性能(读写机制) ，Kafka为什么吞吐量大、速度快？ 
<!--
Kafka的特性之一就是高吞吐率，但是Kafka的消息是保存或缓存在磁盘上的，一般认为在磁盘上读写数据是会降低性能的，但是Kafka即使是普通的服务器，Kafka也可以轻松支持每秒百万级的写入请求，超过了大部分的消息中间件，这种特性也使得Kafka在日志处理等海量数据场景广泛应用。Kafka会把收到的消息都写入到硬盘中，防止丢失数据。为了优化写入速度Kafka采用了两个技术顺序写入和MMFile 。

因为硬盘是机械结构，每次读写都会寻址->写入，其中寻址是一个“机械动作”，它是最耗时的。所以硬盘最讨厌随机I/O，最喜欢顺序I/O。为了提高读写硬盘的速度，Kafka就是使用顺序I/O。这样省去了大量的内存开销以及节省了IO寻址的时间。但是单纯的使用顺序写入，Kafka的写入性能也不可能和内存进行对比，因此Kafka的数据并不是实时的写入硬盘中 。

Kafka充分利用了现代操作系统分页存储来利用内存提高I/O效率。Memory Mapped Files(后面简称mmap)也称为内存映射文件，在64位操作系统中一般可以表示20G的数据文件，它的工作原理是直接利用操作系统的Page实现文件到物理内存的直接映射。完成MMP映射后，用户对内存的所有操作会被操作系统自动的刷新到磁盘上，极大地降低了IO使用率。

Kafka服务器在响应客户端读取的时候，底层使用ZeroCopy技术，直接将磁盘无需拷贝到用户空间，而是直接将数据通过内核空间传递输出，数据并没有抵达用户空间。

-->
&emsp; Kafka的消息是保存或缓存在磁盘上的，一般认为在磁盘上读写数据是会降低性能的，因为寻址会比较消耗时间，但是实际上，Kafka的特性之一就是高吞吐率。 **Kafka之所以能这么快，是因为：「顺序写磁盘、大量使用内存页、零拷贝技术的使用」..**  

&emsp; [持久化/磁盘I/O-顺序读写](/docs/microService/mq/kafka/kafkaPersistence.md)  
&emsp; [内存和零拷贝/网络IO](/docs/microService/mq/kafka/kafkaZeroCopy.md)  
&emsp; [网络IO优化/网络IO](/docs/microService/mq/kafka/networkIO.md)  

## 1.2. 高可用与数据一致性(副本机制)
&emsp; 参考[kafka副本机制](/docs/microService/mq/kafka/kafkaReplica.md)  

## 1.3. 可靠性
&emsp; 可靠性保证：确保系统在各种不同的环境下能够发生一致的行为。  
&emsp; Kafka的保证：  

* 保证分区消息的顺序
	* 如果使用同一个生产者往同一个分区写入消息，而且消息B在消息A之后写入
	* 那么Kafka可以保证消息B的偏移量比消息A的偏移量大，而且消费者会先读取消息A再读取消息B
* 只有当消息被写入分区的所有同步副本时（文件系统缓存），它才被认为是已提交
	* 生产者可以选择接收不同类型的确认，控制参数acks
* 只要还有一个副本是活跃的，那么“已提交的消息就不会丢失”  
* 消费者只能读取已经提交的消息  

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

### 1.3.2. 可靠性（如何保证消息队列不丢失?）  
<!-- 
如何保证Kafka的可靠性、幂等性和有序性
https://zhuanlan.zhihu.com/p/380956215
-->
&emsp; [kafka如何保证消息队列不丢失?](/docs/microService/mq/kafka/kafkaReliability.md)  

### 1.3.3. 幂等（重复消费）和事务
&emsp; [kafka幂等性](/docs/microService/mq/kafka/kafkaIdempotent.md)  
&emsp; [kafka事务](/docs/microService/mq/kafka/kafkaTraction.md)  

## 1.4. 如何让Kafka的消息有序？  
&emsp; Kafka无法做到消息全局有序，只能做到Partition维度的有序。所以如果想要消息有序，就需要从Partition维度入手。一般有两种解决方案：

* 单Partition，单Consumer。通过此种方案强制消息全部写入同一个Partition内，但是同时也牺牲掉了Kafka高吞吐的特性了，所以一般不会采用此方案。  
* **多Partition，多Consumer，指定key使用特定的Hash策略，使其消息落入指定的Partition 中，从而保证相同的key对应的消息是有序的。** 此方案也是有一些弊端，比如当Partition个数发生变化时，相同的key对应的消息会落入到其他的Partition上，所以一旦确定Partition个数后就不能在修改Partition个数了。  
