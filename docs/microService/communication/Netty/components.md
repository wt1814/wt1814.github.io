<!-- TOC -->

- [1. Netty核心组件](#1-netty核心组件)
    - [1.1. Bootstrap & ServerBootstrap](#11-bootstrap--serverbootstrap)
    - [1.2. ~~线程模型之EventLoop~~](#12-线程模型之eventloop)
    - [channel相关](#channel相关)
    - [1.4. ByteBuf](#14-bytebuf)
    - [1.5. 总结：核心组件之间的关系](#15-总结核心组件之间的关系)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. `由netty运行流程可以看出Netty核心组件有Bootstrap、channel相关、EventLoop、byteBuf...`  
2. Bootstrap和ServerBootstrap是针对于Client和Server端定义的引导类，主要用于配置各种参数，并启动整个Netty服务。  
3. `EventLoop线程模型`  
    1. **EventLoop定义了Netty的核心抽象，用于处理连接（一个channel）的生命周期中所发生的事件。<font color = "clime">EventLoop的主要作用实际就是负责监听网络事件并调用事件处理器进行相关I/O操作的处理。</font>**  
    2. **<font color = "red">Channel与EventLoop：</font>**  
    &emsp; 当一个连接到达时，Netty就会创建一个Channel，然后从EventLoopGroup中分配一个EventLoop来给这个Channel绑定上，在该Channel的整个生命周期中都是由这个绑定的EventLoop来服务的。  
    3. **<font color = "red">EventLoopGroup与EventLoop：</font>**  
    &emsp; EventLoopGroup是EventLoop的集合，一个EventLoopGroup包含一个或者多个EventLoop。可以将EventLoop看做EventLoopGroup线程池中的一个工作线程。  
4. Channel  
    1. **在Netty中，Channel是一个Socket连接的抽象，它为用户提供了关于底层Socket状态（是否是连接还是断开）以及对Socket的读写等操作。**  
    2. ChannelHandler  
    &emsp; **ChannelHandler主要用来处理各种事件，这里的事件很广泛，比如可以是连接、数据接收、异常、数据转换等。**  
    3. ChannelPipeline  
    &emsp; Netty的ChannelHandler为处理器提供了基本的抽象，目前可以认为每个ChannelHandler的实例都类似于一种为了响应特定事件而被执行的回调。从应用程序开发人员的角度来看，它充当了所有处理入站和出站数据的应用程序逻辑的拦截载体。ChannelPipeline提供了ChannelHandler链的容器，并定义了用于在该链上传播入站和出站事件流的API。当Channel被创建时，它会被自动地分配到它专属的ChannelPipeline。  
    4. ChannelHandlerContext  
    &emsp; 当ChannelHandler被添加到ChannelPipeline时，它将会被分配一个ChannelHandlerContext，它代表了ChannelHandler和ChannelPipeline之间的绑定。ChannelHandlerContext的主要功能是管理它所关联的ChannelHandler和在同一个ChannelPipeline中的其他ChannelHandler之间的交互。  

# 1. Netty核心组件 
<!--
★★★
https://www.sohu.com/a/372108949_268033
Netty架构原理 
https://www.sohu.com/a/372108949_268033
Netty源码
https://mp.weixin.qq.com/s/I9PGsWo7-ykGf2diKklGtA
你要的Netty常见面试题总结，敖丙搞来了！
https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA

-->

&emsp; 涉及的Netty核心组件主要有：  

* Bootstrap && ServerBootstrap
* EventLoopGroup
* EventLoop
* Channel
* ChannelHandler
* ChannelFuture
* ChannelPipeline
* ChannelHandlerContext
* ByteBuf

## 1.1. Bootstrap & ServerBootstrap  
&emsp; Bootstrap和ServerBootstrap是针对于Client和Server端定义的引导类，主要用于配置各种参数，并启动整个Netty服务。区别如下：  

* Bootstrap是客户端引导类，而ServerBootstrap是服务端引导类。
* Bootstrap通常使用connect()方法连接到远程的主机和端口，作为一个TCP客户端。
* ServerBootstrap通常使用bind()方法绑定本地的端口，等待客户端来连接。
* ServerBootstrap可以处理Accept事件，这里面childHandler是用来处理Channel请求的，可以查看chaildHandler()方法的注解：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-51.png)  
* Bootstrap客户端引导只需要一个EventLoopGroup，但是一个ServerBootstrap通常需要两个(上面的boosGroup和workerGroup)。  

## 1.2. ~~线程模型之EventLoop~~  
&emsp; **EventLoop定义了Netty的核心抽象，用于处理连接（一个channel）的生命周期中所发生的事件。<font color = "clime">EventLoop的主要作用实际就是负责监听网络事件并调用事件处理器进行相关I/O操作的处理。</font>**  

&emsp; **<font color = "red">Channel与EventLoop：</font>**  
&emsp; 当一个连接到达时，Netty就会创建一个Channel，然后从EventLoopGroup中分配一个EventLoop来给这个Channel绑定上，在该Channel的整个生命周期中都是由这个绑定的EventLoop来服务的。  

&emsp; **<font color = "red">EventLoopGroup与EventLoop：</font>**  
&emsp; EventLoopGroup是EventLoop的集合，一个EventLoopGroup包含一个或者多个EventLoop。可以将EventLoop看做EventLoopGroup线程池中的一个工作线程。  

* 一个 EventLoopGroup包含一个或多个EventLoop，即EventLoopGroup: EventLoop = 1 : n。
* 一个 EventLoop在它的生命周期内，只能与一个Thread绑定，即EventLoop : Thread = 1 : 1。
* 所有EventLoop处理的I/O事件都将在它专有的Thread上被处理，从而保证线程安全，即Thread : EventLoop = 1 : 1。
* 一个 Channel在它的生命周期内只能注册到一个EventLoop上，即Channel : EventLoop = n : 1。
* 一个EventLoop可被分配至一个或多个Channel，即EventLoop : Channel = 1 : n。

-----------

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-137.png)  

EventLoopGroup 包含多个 EventLoop（每一个 EventLoop 通常内部包含一个线程），上面我们已经说了 EventLoop 的主要作用实际就是负责监听网络事件并调用事件处理器进行相关 I/O 操作的处理。  
并且 EventLoop 处理的 I/O 事件都将在它专有的 Thread 上被处理，即 Thread 和 EventLoop 属于 1 : 1 的关系，从而保证线程安全。  
上图是一个服务端对 EventLoopGroup 使用的大致模块图，其中 Boss EventloopGroup 用于接收连接，Worker EventloopGroup 用于具体的处理（消息的读写以及其他逻辑处理）。  
从上图可以看出：当客户端通过 connect 方法连接服务端时，bossGroup 处理客户端连接请求。当客户端处理完成后，会将这个连接提交给 workerGroup 来处理，然后 workerGroup 负责处理其 IO 相关操作。  

## channel相关  
&emsp; 参考[Channel相关](/docs/microService/communication/Netty/channel.md)  

## 1.4. ByteBuf  
&emsp; 针对于NIO中的Buffer类，Netty提供了ByteBuf来替代。ByteBuf声明了两个指针：一个读指针，一个写指针，使得读写操作进行分离，简化buffer的操作流程。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-52.png)  
&emsp; 另外Netty提供了几种ByteBuf的实现：  

* Pooled和Unpooled池化和非池化
* Heap和Direct，堆内存和堆外内存，NIO中创建Buffer也可以指定
* Safe和Unsafe，安全和非安全

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-53.png)  
&emsp; 对于多种创建Buffer的方式，可以直接使用：  

```java
ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
ByteBuf buffer = allocator.buffer(length);
```
&emsp; 使用这种方式，Netty将最大努力的使用池化、Unsafe、对外内存的方式创建buffer。  


## 1.5. 总结：核心组件之间的关系  

* 一个 Channel对应一个 ChannelPipeline
* 一个 ChannelPipeline 包含一条双向的 ChannelHandlerContext链
* 一个 ChannelHandlerContext中包含一个ChannelHandler
* 一个 Channel会绑定到一个EventLoop上
* 一个 NioEventLoop 维护了一个 Selector(使用的是 Java 原生的 Selector)
* 一个 NioEventLoop 相当于一个线程
