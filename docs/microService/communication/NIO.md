

<!-- TOC -->

- [1. NIO简介](#1-nio简介)

<!-- /TOC -->




<!-- 

-->


# 1. NIO简介  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-14.png)  

&emsp; **<font color = "red">NIO基本组件</font>**  
&emsp; NIO读写是I/O的基本过程。读写操作中使用的核心部件有：Channels、Buffers、Selectors。在标准I/O中，使用字符流和字节流；在NIO中使用通道和缓冲区。  

* Channel(通道)：在缓冲区和位于通道另一侧的服务之间进行数据传输，支持单向或双向传输，支持阻塞或非阻塞模式。  
    &emsp; 通道列表：  

    |通道类型|作用|
    |---|---|
    |FileChannel|从文件中读写数据|
    |SocketChannel|能通过TCP读写网络中的数据|
    |DatagramChannel|能通过UDP读写网络中的数据|
    |ServerSocketChannel|可以监听新进来的TCP连接，对每一个新进来的连接都会创建一个SocketChannel|
* Buffer(缓冲区)：高效数据容器。本质上是一块内存区，用于数据的读写，NIO Buffer将其包裹并提供开发时常用的接口，便于数据操作。  
    &emsp; NIO中的所有I/O都是通过一个通道开始的。数据总是从缓冲区写入通道，并从通道读取到缓冲区。从通道读取：创建一个缓冲区，然后请求通道读取数据。通道写入：创建一个缓冲区，填充数据，并要求通道写入数据。  
    &emsp; 在NIO中使用的核心缓冲区有：CharBuffer、DoubleBuffer、IntBuffer、LongBuffer、ByteBuffer、ShortBuffer、FloatBuffer。上述缓冲区覆盖了通过I/O发送的基本数据类型：characters，double，int，long，byte，short和float。
* Selectors（选择器）  
    &emsp; Selector(IO复用器/选择器)：多路复用的重要组成部分，检查一个或多个Channel(通道)是否是可读、写状态，实现单线程管理多Channel(通道)，优于使用多线程或线程池产生的系统资源开销。如果应用程序有多个通道(连接)打开，但每个连接的流量都很低，则可考虑使用Selectors。


