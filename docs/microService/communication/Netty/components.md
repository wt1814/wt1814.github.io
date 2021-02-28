<!-- TOC -->

- [1. Netty核心组件](#1-netty核心组件)
    - [1.1. Bootstrap & ServerBootstrap](#11-bootstrap--serverbootstrap)
    - [1.2. EventLoopGroup && EventLoop](#12-eventloopgroup--eventloop)
    - [1.3. channel相关](#13-channel相关)
        - [1.3.1. channel](#131-channel)
        - [1.3.2. ChannelHandler](#132-channelhandler)
            - [1.3.2.1. ChannelInboundHandler](#1321-channelinboundhandler)
            - [1.3.2.2. ChannelOutboundHandler](#1322-channeloutboundhandler)
        - [1.3.3. ChannelHandlerAdapter](#133-channelhandleradapter)
        - [1.3.4. ChannelPipeline](#134-channelpipeline)
        - [1.3.5. ChannelHandlerContext](#135-channelhandlercontext)
        - [1.3.6. ChannelFuture && ChannelPromise](#136-channelfuture--channelpromise)
            - [1.3.6.1. 与ChannelHandler、ChannelPipeline的关联使用](#1361-与channelhandlerchannelpipeline的关联使用)
    - [1.4. ByteBuf](#14-bytebuf)
    - [1.5. 总结：核心组件之间的关系](#15-总结核心组件之间的关系)

<!-- /TOC -->

&emsp; 小结：  
&emsp; 由netty运行流程可以看出Netty核心组件有Bootstrap、EventLoop、channel相关、byteBuf...  

# 1. Netty核心组件 
<!--
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
&emsp; Bootstrap和ServerBootstrap是Netty程序的引导类，主要用于配置各种参数，并启动整个Netty服务。  
&emsp; Bootstrap和ServerBootstrap是针对于Client和Server端定义的两套启动类，区别如下：  

* Bootstrap是客户端引导类，而ServerBootstrap是服务端引导类。
* Bootstrap通常使用connect()方法连接到远程的主机和端口，作为一个TCP客户端。
* ServerBootstrap通常使用bind()方法绑定本地的端口，等待客户端来连接。
* ServerBootstrap可以处理Accept事件，这里面childHandler是用来处理Channel请求的，可以查看chaildHandler()方法的注解：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-51.png)  
* Bootstrap客户端引导只需要一个EventLoopGroup，但是一个ServerBootstrap通常需要两个(上面的boosGroup和workerGroup)。  

## 1.2. EventLoopGroup && EventLoop  


&emsp; EventLoop(事件循环)接口可以说是Netty中最核心的概念了！  
&emsp; **EventLoop定义了Netty的核心抽象，用于处理连接的生命周期中所发生的事件。EventLoop的主要作用实际就是负责监听网络事件并调用事件处理器进行相关I/O操作的处理。**  

&emsp; EventLoopGroup是EventLoop的集合，一个EventLoopGroup包含一个或者多个EventLoop。可以将EventLoop看做EventLoopGroup线程池中的一个工作线程。  

* 一个 EventLoopGroup包含一个或多个EventLoop，即EventLoopGroup: EventLoop = 1 : n
* 一个 EventLoop在它的生命周期内，只能与一个Thread绑定，即EventLoop : Thread = 1 : 1
* 所有EventLoop处理的I/O事件都将在它专有的Thread上被处理，从而保证线程安全，即Thread : EventLoop = 1 : 1
* 一个 Channel在它的生命周期内只能注册到一个EventLoop上，即Channel : EventLoop = n : 1
* 一个EventLoop可被分配至一个或多个Channel，即EventLoop : Channel = 1 : n

&emsp; 当一个连接到达时，Netty就会创建一个Channel，然后从EventLoopGroup中分配一个EventLoop来给这个Channel绑定上，在该Channel的整个生命周期中都是有这个绑定的EventLoop来服务的。

&emsp; **<font color = "red">Channel与EventLoop：</font>**  
&emsp; Channel为Netty网络操作(读写等操作)抽象类，EventLoop负责处理注册到其上的Channel 处理 I/O 操作，两者配合参与 I/O 操作。 

## 1.3. channel相关
### 1.3.1. channel  
<!-- 
&emsp; Channel接口是Netty对网络操作抽象类，它除了包括基本的I/O 操作，如 bind()、connect()、read()、write()等。  
&emsp; 比较常用的Channel接口实现类是NioServerSocketChannel(服务端)和NioSocketChannel(客户端)，这两个 Channel 可以和 BIO 编程模型中的ServerSocket以及Socket两个概念对应上。Netty 的 Channel 接口所提供的 API，大大地降低了直接使用 Socket 类的复杂性。  
-->
&emsp; 类似于NIO的Channel，Netty提供了自己的Channel和其子类实现，用于异步I/0操作和其他相关的操作。    
&emsp; **在Netty中, Channel是一个Socket连接的抽象, 它为用户提供了关于底层 Socket 状态(是否是连接还是断开)以及对Socket的读写等操作。** 每当Netty建立了一个连接后, 都会有一个对应的Channel实例。并且，有父子channel的概念。服务器连接监听的channel ，也叫 parent channel。对应于每一个 Socket 连接的channel，也叫 child channel。  
&emsp; 既然channel 是 Netty 抽象出来的网络 I/O 读写相关的接口，为什么不使用JDK NIO 原生的 Channel 而要另起炉灶呢，主要原因如下：  

* JDK 的SocketChannel 和 ServersocketChannel没有统一的 Channel 接口供业务开发者使用，对于用户而言，没有统一的操作视图，使用起来并不方便。
* JDK 的 SocketChannel和 ScrversockctChannel的主要职责就是网络 I/O 操作，由于它们是SPI类接口，由具体的虚拟机厂家来提供，所以通过继承 SPI 功能直接实现 ServersocketChannel 和 SocketChannel 来扩展其工作量和重新Channel 功类是差不多的。
* Netty 的 ChannelPipeline Channel 需要够跟 Netty 的整体架构融合在一起，例如 I/O 模型、基的定制模型，以及基于元数据描述配置化的 TCP 参数等，这些JDK SocketChannel 和ServersocketChannel都没有提供，需要重新封装。
* 自定义的 Channel ，功实现更加灵活。

&emsp; 基于上述4原因，它的设计原理比较简单，Netty重新设计了Channel接口，并且给予了很多不同的实现。但是功能却比较繁杂，主要的设计理念如下：

* 在 Channel 接口层，相关联的其他操作封装起来，采用 Facade 模式进行统一封装，将网络 I/O 操作、网络 I/O 统一对外提供。
* Channel 接口的定义尽量大而全，统一的视图，由不同子类实现不同的功能，公共功能在抽象父类中实现，最大程度上实现接口的重用。
* 具体实现采用聚合而非包含的方式，将相关功能的类聚合在 Channel中，由 Channel 统一负责分配和调度，功能实现更加灵活。

### 1.3.2. ChannelHandler  
&emsp; ChannelHandler 是Netty中最常用的组件。ChannelHandler 主要用来处理各种事件，这里的事件很广泛，比如可以是连接、数据接收、异常、数据转换等。  
&emsp; ChannelHandler 有两个核心子类 ChannelInboundHandler 和 ChannelOutboundHandler，其中 ChannelInboundHandler 用于接收、处理入站( Inbound )的数据和事件，而 ChannelOutboundHandler 则相反，用于接收、处理出站( Outbound )的数据和事件。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-88.png)  


#### 1.3.2.1. ChannelInboundHandler  
&emsp; **ChannelInboundHandler处理入站数据以及各种状态变化，当Channel状态发生改变会调用ChannelInboundHandler中的一些生命周期方法。** 这些方法与Channel的生命密切相关。  
&emsp; 入站数据，就是进入socket的数据。下面展示一些该接口的生命周期API：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-54.png)  
&emsp; 当某个 ChannelInboundHandler的实现重写 channelRead()方法时，它将负责显式地释放与池化的 ByteBuf 实例相关的内存。Netty 为此提供了一个实用方法ReferenceCountUtil.release()。  

```java
@Sharable
public class DiscardHandler extends ChannelInboundHandlerAdapter {
 @Override
 public void channelRead(ChannelHandlerContext ctx, Object msg) {
  ReferenceCountUtil.release(msg);
 }
}
```
&emsp; 这种方式还挺繁琐的，Netty提供了一个SimpleChannelInboundHandler，重写channelRead0()方法，就可以在调用过程中会自动释放资源。  

```java
public class SimpleDiscardHandler
 extends SimpleChannelInboundHandler<Object> {
 @Override
 public void channelRead0(ChannelHandlerContext ctx,
         Object msg) {
   // 不用调用ReferenceCountUtil.release(msg)也会释放资源
 }
}
```

#### 1.3.2.2. ChannelOutboundHandler   
&emsp; 出站操作和数据将由ChannelOutboundHandler处理。它的方法将被Channel、ChannelPipeline以及 ChannelHandlerContext调用。ChannelOutboundHandler的一个强大的功能是可以按需推迟操作或者事件，这使得可以通过一些复杂的方法来处理请求。例如，如果到远程节点的写入被暂停了，那么可以推迟冲刷操作并在稍后继续。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-55.png)  
&emsp; ChannelPromise与ChannelFuture: ChannelOutboundHandler中的大部分方法都需要一个ChannelPromise参数， 以便在操作完成时得到通知。ChannelPromise是ChannelFuture的一个子类，其定义了一些可写的方法，如setSuccess()和setFailure()，从而使ChannelFuture不可变。  

### 1.3.3. ChannelHandlerAdapter  
&emsp; ChannelHandlerAdapter顾名思义,就是handler的适配器。你需要知道什么是适配器模式，假设有一个A接口，我们需要A的subclass实现功能,但是B类中正好有我们需要的功能，不想复制粘贴B中的方法和属性了，那么可以写一个适配器类Adpter继承B实现A，这样一来Adapter是A的子类并且能直接使用B中的方法，这种模式就是适配器模式。  
&emsp; 就比如Netty中的SslHandler类，想使用ByteToMessageDecoder中的方法进行解码，但是必须是ChannelHandler子类对象才能加入到ChannelPipeline中，通过如下签名和其实现细节(SslHandler实现细节就不贴了)就能够作为一个handler去处理消息了。  

```java
public class SslHandler extends ByteToMessageDecoder implements ChannelOutboundHandler
```

&emsp; ChannelHandlerAdapter提供了一些实用方法isSharable()如果其对应的实现被标注为Sharable， 那么这个方法将返回 true， 表示它可以被添加到多个 ChannelPipeline中 。如果想在自己的ChannelHandler中使用这些适配器类，只需要扩展他们，重写那些想要自定义的方法即可。  

### 1.3.4. ChannelPipeline  
&emsp; 每一个新创建的 Channel 都将会被分配一个新的 ChannelPipeline。这项关联是永久性的；Channel 既不能附加另外一个 ChannelPipeline，也不能分离其当前的。在 Netty 组件的生命周期中，这是一项固定的操作，不需要开发人员的任何干预。  
&emsp; Netty 的 ChannelHandler 为处理器提供了基本的抽象， 目前你可以认为每个 ChannelHandler 的实例都类似于一种为了响应特定事件而被执行的回调。从应用程序开发人员的角度来看， 它充当了所有处理入站和出站数据的应用程序逻辑的拦截载体。ChannelPipeline提供了 ChannelHandler 链的容器，并定义了用于在该链上传播入站和出站事件流的 API。当 Channel 被创建时，它会被自动地分配到它专属的 ChannelPipeline。  

&emsp; ChannelHandler 安装到 ChannelPipeline 中的过程如下所示：  

* 一个ChannelInitializer的实现被注册到了ServerBootstrap中
* 当 ChannelInitializer.initChannel()方法被调用时，ChannelInitializer将在 ChannelPipeline中安装一组自定义的 ChannelHandler
* ChannelInitializer 将它自己从 ChannelPipeline中移除  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-56.png)  
&emsp; 如上图所示：这是一个同时具有入站和出站 ChannelHandler 的 ChannelPipeline的布局，并且印证了我们之前的关于 ChannelPipeline主要由一系列的 ChannelHandler 所组成的说法。ChannelPipeline还提供了通过 ChannelPipeline 本身传播事件的方法。如果一个入站事件被触发，它将被从 ChannelPipeline的头部开始一直被传播到 Channel Pipeline 的尾端。  
&emsp; 从事件途经 ChannelPipeline的角度来看， ChannelPipeline的头部和尾端取决于该事件是入站的还是出站的。然而 Netty 总是将 ChannelPipeline的入站口(图 的左侧)作为头部，而将出站口(该图的右侧)作为尾端。当你完成了通过调用 ChannelPipeline.add*()方法将入站处理器( ChannelInboundHandler)和 出 站 处 理 器 ( ChannelOutboundHandler ) 混 合 添 加 到 ChannelPipeline之 后 ， 每 一 个ChannelHandler 从头部到尾端的顺序位置正如同我们方才所定义它们的一样。因此，如果你将图 6-3 中的处理器( ChannelHandler)从左到右进行编号，那么第一个被入站事件看到的 ChannelHandler 将是1，而第一个被出站事件看到的 ChannelHandler将是 5。  
&emsp; 在 ChannelPipeline 传播事件时，它会测试 ChannelPipeline 中的下一个 ChannelHandler 的类型是否和事件的运动方向相匹配。如果不匹配， ChannelPipeline 将跳过该ChannelHandler 并前进到下一个，直到它找到和该事件所期望的方向相匹配的为止。(当然， ChannelHandler也可以同时实现ChannelInboundHandler接口和 ChannelOutboundHandler 接口。)  

### 1.3.5. ChannelHandlerContext  
&emsp; 当 ChannelHandler 被添加到 ChannelPipeline 时，它将会被分配一个 ChannelHandlerContext ，它代表了 ChannelHandler 和 ChannelPipeline 之间的绑定。ChannelHandlerContext 的主要功能是管理它所关联的ChannelHandler和在同一个 ChannelPipeline 中的其他ChannelHandler之间的交互。  
&emsp; 如果调用Channel或ChannelPipeline上的方法,会沿着整个ChannelPipeline传播,如果调用ChannelHandlerContext上的相同方法,则会从对应的当前ChannelHandler进行传播。  
&emsp; ChannelHandlerContext API如下表所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-57.png)  

* ChannelHandlerContext 和 ChannelHandler之间的关联(绑定)是永远不会改变的，所以缓存对它的引用是安全的；
* 如同在本节开头所解释的一样，相对于其他类的同名方法，ChannelHandlerContext的方法将产生更短的事件流， 应该尽可能地利用这个特性来获得最大的性能。  

### 1.3.6. ChannelFuture && ChannelPromise 
&emsp; Netty 是异步非阻塞的，所有的 I/O 操作都为异步的。因此，不能立刻得到操作是否执行成功，但是，可以通过 ChannelFuture 接口的 addListener() 方法注册一个 ChannelFutureListener，当操作执行成功或者失败时，监听就会自动触发返回结果。  
&emsp; 并且，还可以通过ChannelFuture 的 channel() 方法获取关联的Channel。  

```java
public interface ChannelFuture extends Future<Void> {
    Channel channel();
    ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> var1);
    ChannelFuture sync() throws InterruptedException;
}
```
&emsp; 另外，还可以通过 ChannelFuture 接口的 sync()方法让异步的操作变成同步的。  

<!-- 
ChannelHandler 和 ChannelPipeline  
&emsp; 下面这段代码指定了序列化编解码器以及自定义的 ChannelHandler 处理消息。  

```java
b.group(eventLoopGroup)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                    ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                    ch.pipeline().addLast(new KryoClientHandler());
                }
            });
```
&emsp; ChannelHandler 是消息的具体处理器。他负责处理读写操作、客户端连接等事情。  
&emsp; ChannelPipeline 为 ChannelHandler 的链，提供了一个容器并定义了用于沿着链传播入站和出站事件流的 API 。当 Channel 被创建时，它会被自动地分配到它专属的 ChannelPipeline。  
&emsp; 可以在 ChannelPipeline 上通过 addLast() 方法添加一个或者多个ChannelHandler ，因为一个数据或者事件可能会被多个 Handler 处理。当一个 ChannelHandler 处理完之后就将数据交给下一个 ChannelHandler 。  
-->

#### 1.3.6.1. 与ChannelHandler、ChannelPipeline的关联使用  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-58.png)  
&emsp; 从ChannelHandlerContext访问channel  

```java
ChannelHandlerContext ctx = ..;
// 获取channel引用
Channel channel = ctx.channel();
// 通过channel写入缓冲区
channel.write(Unpooled.copiedBuffer("Netty in Action",
CharsetUtil.UTF_8));
```

&emsp; 从ChannelHandlerContext访问ChannelPipeline  

```java
ChannelHandlerContext ctx = ..;
// 获取ChannelHandlerContext
ChannelPipeline pipeline = ctx.pipeline();
// 通过ChannelPipeline写入缓冲区
pipeline.write(Unpooled.copiedBuffer("Netty in Action",
CharsetUtil.UTF_8));
```
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-59.png)  
&emsp; 有时候不想从头传递数据，想跳过几个handler，从某个handler开始传递数据。必须获取目标handler之前的handler关联的ChannelHandlerContext。

```java
ChannelHandlerContext ctx = ..;
// 直接通过ChannelHandlerContext写数据,发送到下一个handler
ctx.write(Unpooled.copiedBuffer("Netty in Action", CharsetUtil.UTF_8));
```
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-60.png)  

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
