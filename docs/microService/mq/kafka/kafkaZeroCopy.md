


# 内存和零拷贝

<!--

https://mp.weixin.qq.com/s/QIK1N-ePm6DQE4tMQ9N3Gw
kafka——高效读写数据
https://www.jianshu.com/p/ce8253609b6b
-->


## Page Cache
&emsp; 为了优化读写性能，Kafka利用了操作系统本身的Page Cache，就是利用操作系统自身的内存而不是JVM空间内存。这样做的好处有：  

* 避免Object消耗：如果是使用 Java 堆，Java对象的内存消耗比较大，通常是所存储数据的两倍甚至更多。
* 避免GC问题：随着JVM中数据不断增多，垃圾回收将会变得复杂与缓慢，使用系统缓存就不会存在GC问题

&emsp; 相比于使用JVM或in-memory cache等数据结构，利用操作系统的Page Cache更加简单可靠。

&emsp; 首先，操作系统层面的缓存利用率会更高，因为存储的都是紧凑的字节结构而不是独立的对象。  
&emsp; 其次，操作系统本身也对于Page Cache做了大量优化，提供了 write-behind、read-ahead以及flush等多种机制。  
&emsp; 再者，即使服务进程重启，系统缓存依然不会消失，避免了in-process cache重建缓存的过程。  

&emsp; 通过操作系统的Page Cache，Kafka的读写操作基本上是基于内存的，读写速度得到了极大的提升。  

## 零拷贝

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
