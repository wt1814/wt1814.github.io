
<!-- TOC -->

- [1. Kafka网络IO优化](#1-kafka网络io优化)
    - [1.1. 零拷贝](#11-零拷贝)
    - [1.2. 批量操作数据](#12-批量操作数据)
        - [1.2.1. 批量读写](#121-批量读写)
        - [1.2.2. 批量压缩](#122-批量压缩)

<!-- /TOC -->


# 1. Kafka网络IO优化
<!-- 
kafka——高效读写数据
https://www.jianshu.com/p/ce8253609b6b
-->

## 1.1. 零拷贝

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


## 1.2. 批量操作数据
### 1.2.1. 批量读写  
&emsp; Kafka数据读写也是批量的而不是单条的。  
&emsp; 除了利用底层的技术外，Kafka还在应用程序层面提供了一些手段来提升性能。最明显的就是使用批次。在向Kafka写入数据时，可以启用批次写入，这样可以避免在网络上频繁传输单个消息带来的延迟和带宽开销。假设网络带宽为10MB/S，一次性传输10MB的消息比传输1KB的消息10000万次显然要快得多。  




### 1.2.2. 批量压缩  
&emsp; 在很多情况下，系统的瓶颈不是CPU或磁盘，而是网络IO，对于需要在广域网上的数据中心之间发送消息的数据流水线尤其如此。进行数据压缩会消耗少量的CPU资源，不过对于kafka而言，网络IO更应该需要考虑。  

1. 如果每个消息都压缩，但是压缩率相对很低，所以Kafka使用了批量压缩，即将多个消息一起压缩而不是单个消息压缩  
2. Kafka允许使用递归的消息集合，批量的消息可以通过压缩的形式传输并且在日志中也可以保持压缩格式，直到被消费者解压缩  
3. Kafka支持多种压缩协议，包括Gzip和Snappy压缩协议  

&emsp; Kafka速度的秘诀在于，它把所有的消息都变成一个批量的文件，并且进行合理的批量压缩，减少网络IO损耗，通过mmap提高I/O速度，写入数据的时候由于单个 Partition 是末尾添加所以速度最优；读取数据的时候配合sendfile直接暴力输出。  


