

# Java IO
<!-- 
Java BIO  AIO
https://mp.weixin.qq.com/s/3OtbG6jegOS4m2GbyOF2lQ

https://blog.csdn.net/u010541670/article/details/91890649
-->

## BIO  
&emsp; **Java BIO即Block I/O，同步并阻塞的IO。**  
&emsp; BIO就是传统的java.io包下面的代码实现。  

## NIO
&emsp; **<font color = "red">NIO，同步非阻塞I/O，基于io多路复用模型，即select，poll，epoll。</font>** NIO 与原来的 I/O 有同样的作用和目的, 它们之间最重要的区别是数据打包和传输的方式。原来的 I/O 以流的方式处理数据，而 NIO 以块的方式处理数据。  
&emsp; 面向流的 I/O 系统一次一个字节地处理数据。一个输入流产生一个字节的数据，一个输出流消费一个字节的数据。为流式数据创建过滤器非常容易。链接几个过滤器，以便每个过滤器只负责单个复杂处理机制的一部分，这样也是相对简单的。不利的一面是，面向流的 I/O 通常相当慢。  
&emsp; 一个面向块 的 I/O 系统以块的形式处理数据。每一个操作都在一步中产生或者消费一个数据块。按块处理数据比按(流式的)字节处理数据要快得多。但是面向块的 I/O 缺少一些面向流的 I/O 所具有的优雅性和简单性。  


&emsp; **<font color = "red">NIO基本组件：</font>**  
&emsp; NIO读写是I/O的基本过程。读写操作中使用的核心部件有：Channel、Buffer、Selector。在标准I/O中，使用字符流和字节流；在NIO中使用通道和缓冲区。  

* Channel(通道)：  
&emsp; 在缓冲区和位于通道另一侧的服务之间进行数据传输，支持单向或双向传输，支持阻塞或非阻塞模式。  
&emsp; 通道列表：  

    |通道类型|作用|
    |---|---|
    |FileChannel|从文件中读写数据|
    |SocketChannel|能通过TCP读写网络中的数据|
    |DatagramChannel|能通过UDP读写网络中的数据|
    |ServerSocketChannel|可以监听新进来的TCP连接，对每一个新进来的连接都会创建一个SocketChannel|
* Buffer(缓冲区)：  
&emsp; 高效数据容器。Buffer也被称为内存缓冲区，本质上就是内存中的一块，用于数据的读写，NIO Buffer将其包裹并提供开发时常用的接口，便于数据操作。  
&emsp; NIO中的所有I/O都是通过一个通道开始的。数据总是从缓冲区写入通道，并从通道读取到缓冲区。从通道读取：创建一个缓冲区，然后请求通道读取数据。通道写入：创建一个缓冲区，填充数据，并要求通道写入数据。  
&emsp; 在NIO中使用的核心缓冲区有：CharBuffer、DoubleBuffer、IntBuffer、LongBuffer、ByteBuffer、ShortBuffer、FloatBuffer。上述缓冲区覆盖了通过I/O发送的基本数据类型：characters，double，int，long，byte，short和float。
* Selectors(IO复用器/选择器)：  
&emsp; 多路复用的重要组成部分，检查一个或多个Channel(通道)是否是可读、写状态，实现单线程管理多Channel(通道)，优于使用多线程或线程池产生的系统资源开销。如果应用程序有多个通道(连接)打开，但每个连接的流量都很低，则可考虑使用Selectors。


## AIO  
&emsp; **<font color = "red">Java AIO即Async非阻塞，是异步非阻塞的IO。</font>**  

## 各自适用场景  
&emsp; BIO方式适用于连接数目比较小且固定的架构，这种方式对服务器资源要求比较高，并发局限于应用中，JDK1.4以前的唯一选择，但程序直观简单易理解。  
&emsp; NIO方式适用于连接数目多且连接比较短（轻操作）的架构，比如聊天服务器，并发局限于应用中，编程比较复杂，JDK1.4开始支持。  
&emsp; AIO方式适用于连接数目多且连接比较长（重操作）的架构，比如相册服务器，充分调用OS参与并发操作，编程比较复杂，JDK7开始支持。  
