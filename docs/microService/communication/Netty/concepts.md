
<!-- TOC -->

- [1. netty核心概念](#1-netty核心概念)
    - [1.1. Netty简介](#11-netty简介)
    - [1.2. Netty项目架构](#12-netty项目架构)
    - [1.3. Netty逻辑架构](#13-netty逻辑架构)
    - [1.4. Netty核心组件](#14-netty核心组件)
        - [1.4.1. channel](#141-channel)
        - [1.4.2. EventLoop](#142-eventloop)
        - [1.4.3. ChannelFuture](#143-channelfuture)
        - [1.4.4. ChannelHandler 和 ChannelPipeline](#144-channelhandler-和-channelpipeline)
    - [1.5. 简单示例](#15-简单示例)

<!-- /TOC -->

# 1. netty核心概念  
<!-- 
你要的Netty常见面试题总结，敖丙搞来了！
https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA
-->

## 1.1. Netty简介  

&emsp; 为什么要用 Netty？  
&emsp; Netty是由JBoss开发，基于Java NIO的一个高性能通信框架。之前几篇文章介绍了Java NIO的一些基本的概念和API。但在实际的网络开发中，其实很少使用Java NIO原生的API。主要有以下原因：  

* 原生API使用单线程模型，不能很好利用多核优势；  
* 原生API是直接使用的IO数据，没有做任何封装处理，对数据的编解码、TCP的粘包和拆包、客户端断连、网络的可靠性和安全性方面没有做处理；  

&emsp; (1)NIO的类库和API繁杂，使用麻烦，需要熟练掌握Selector、ServerSocketChannek、SockctChannek、ByteBuffer等。  
&emsp; (2)需要貝备其他的额外技能做铺垫，例如熟悉Java多线程编程。这是因为NIO编程涉 及到Reactor模式，必须对多线程和网路编程非常熟悉，才能编写出高质量的NIO程序。  
&emsp; (3)可靠性能力补齐，工作量和难度都非常大。例如客户端面临断连重连、网络闪断、 半包读写、失败缓存、网络拥塞和异常码流的处理等问题，NIO编程的特点是功能开发相对容易，但是可靠性能力补齐的工作量和难度都非常大。  
&emsp; **<fong color = "red">(4)JDKNIO的BUG,例如見名昭著的epoll bug，它会导致Selector空轮询，最终导致CPU100%。官方声称在JDK 1.6版本的update18修复了该问题,但是直到JDK 1.7版本该问题仍旧存在，只不过该BUG发生概率降低了一些而已，它并没有得到根本性解决。该BUG以及与该BUG相关的问题单可以参见以下链接内容。**</font>  

* http://bugs.java.com/bugdatabase/viewbug.do?bug_id=6403933  
* http://bugs.java.com/bugdalabase/viewbug.do?bug_id=21477l9  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-18.png)  

## 1.2. Netty项目架构  
&emsp; **Netty的项目结构：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-26.png)  

&emsp; **Netty的整体架构图：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-25.png)  

* Core ：核心部分，是底层的网络通用抽象和部分实现。
    * Extensible Event Model ：可拓展的事件模型。Netty 是基于事件模型的网络应用框架。
    * Universal Communication API ：通用的通信 API 层。Netty 定义了一套抽象的通用通信层的 API 。
    * Zero-Copy-Capable Rich Byte Buffer ：支持零拷贝特性的 Byte Buffer 实现。
* Transport Services ：传输( 通信 )服务，具体的网络传输的定义与实现。
    * Socket & Datagram ：TCP 和 UDP 的传输实现。
    * HTTP Tunnel ：HTTP通道的传输实现。
    * In-VM Piple ：JVM内部的传输实现。  
* Protocol Support ：协议支持。Netty对于一些通用协议的编解码实现。例如：HTTP、Redis、DNS等等。

&emsp; **各个项目作用：**  

* common 项目，该项目是一个通用的工具类项目，几乎被所有的其它项目依赖使用，它提供了一些数据类型处理工具类，并发编程以及多线程的扩展，计数器等等通用的工具类。  
* buffer 项目，该项目下是 Netty 自行实现的一个 Byte Buffer 字节缓冲区。该包的实现相对于 JDK 自带的 ByteBuffer 有很多优点：无论是 API 的功能，使用体验，性能都要更加优秀。它提供了一系列( 多种 )的抽象定义以及实现，以满足不同场景下的需要。  

        该项目实现了 Netty 架构图中的 Zero-Copy-Capable Rich Byte Buffer 。

* transport 项目，该项目是网络传输通道的抽象和实现。它定义通信的统一通信 API ，统一了 JDK 的 OIO、NIO ( 不包括 AIO )等多种编程接口。  
&emsp; 另外，它提供了多个子项目，实现不同的传输类型。例如：transport-native-epoll、transport-native-kqueue、transport-rxtx、transport-udt 和 transport-sctp 等等。  

        该项是核心项目，实现了 Netty 架构图中 Transport Services、Universal Communication API 和 Extensible Event Model 等多部分内容。

* codec 项目，该项目是协议编解码的抽象与部分实现：JSON、Google Protocol、Base64、XML等等。  
&emsp; 另外，它提供了多个子项目，实现不同协议的编解码。例如：codec-dns、codec-haproxy、codec-http、codec-http2、codec-mqtt、codec-redis、codec-memcached、codec-smtp、codec-socks、codec-stomp、codec-xml 等等。  

        该项目实现了Netty 架构图中的 Protocol Support 。

* handler 项目，该项目是提供内置的连接通道处理器( ChannelHandler )实现类。例如：SSL 处理器、日志处理器等等。  
&emsp; 另外，它提供了一个子项目 handler-proxy ，实现对 HTTP、Socks 4、Socks 5 的代理转发。   
* example 项目，该项目是提供各种 Netty 使用示例。  

&emsp; **Netty中开发者最经常打交道的五个组件：ByteBuf，Channel，pipeline，ChannelHandler、EventLoop。**  

## 1.3. Netty逻辑架构  
<!-- 

《Netty权威指南》第20章
-->

&emsp; Netty采用了典型的三层网络架构进行设计和开发，逻辑架构如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-27.png)  

* Reactor通信调度层  
&emsp; 它由一系列辅助类完成，包括Reactor线程NioEvenlLoop及其父类，NioSocketChannel/ NioServerSocketChannel及其父类，ByteBuffer以及由其衍生出来的各种Buffer，Unsafe以及其衍生出的各种内部类等。该层的主要职责就是监听网络的读写和连接操作，负责将网络层的数据读取到内存缓冲区中，然后触发务种网络事件，例如连接创建、连接激活、 读申件、写事件等，将这些少件触发到PipeLine中，由PipeLine管理的职责链来进行后续的处理。  
* 职责链ChannelPipeline  
&emsp; 它负责事件在职责链中的有序传播，同时负责动态地编排职责链。职责链可以选择监听和处理自己关心的事件，它可以拦截处理和向后/向前传播事件。不同应用的Handler用于消息的编解码，它可以将外部的协议消息转换成内部的POJO对象，这样上层业务则只需要关心处理业务逻辑即可，不需要感知底层的协议差异和线程模型差异，实现了架构层面的分层隔离。  
* 业务逻辑编排层(Service ChannelHandler)  
&emsp; 业务逻辑编排层通常有两类：一类是纯粹的业务逻辑编排，还有一类是其他的应用层协议插件，用于特定协议相关的会话和链路管理。例如CMPP协议，用于管理和中国移动短信系统的对接。  
&emsp; 架构的不同层面，需要关心和处理的对象都不同，通常情况下，对于业务开发者，只需要关心职责链的拦截和业务Handler的编排。因为应用层协议栈往往是开发一次，到处运行，所以实际上对于业务开发者来说，只需要关心服务层的业务逻辑开发即可。各种应用协议以插件的形式提供，只有协议开发人员需要关注协议插件，对于其他业务开发人员来说，只需关心业务逻辑定制。这种分层的架构设计理念实现了NIO框架各层之间的解耦，便于上层业务协议栈的开发和业务逻辑的定制。  

## 1.4. Netty核心组件 
<!-- 

https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA

-->

### 1.4.1. channel  
&emsp; Channel接口是Netty对网络操作抽象类，它除了包括基本的I/O 操作，如 bind()、connect()、read()、write()等。  
&emsp; 比较常用的Channel接口实现类是NioServerSocketChannel（服务端）和NioSocketChannel（客户端），这两个 Channel 可以和 BIO 编程模型中的ServerSocket以及Socket两个概念对应上。Netty 的 Channel 接口所提供的 API，大大地降低了直接使用 Socket 类的复杂性。  

### 1.4.2. EventLoop  
&emsp; EventLoop（事件循环）接口可以说是 Netty 中最核心的概念了！  
&emsp; EventLoop 定义了 Netty 的核心抽象，用于处理连接的生命周期中所发生的事件。  
&emsp; EventLoop 的主要作用实际就是负责监听网络事件并调用事件处理器进行相关 I/O 操作的处理。

&emsp; Channel和EventLoop之间的联系：  
&emsp; Channel为Netty 网络操作(读写等操作)抽象类，EventLoop 负责处理注册到其上的Channel 处理 I/O 操作，两者配合参与 I/O 操作。

### 1.4.3. ChannelFuture  
&emsp; Netty 是异步非阻塞的，所有的 I/O 操作都为异步的。  
&emsp; 因此，不能立刻得到操作是否执行成功，但是，可以通过 ChannelFuture 接口的 addListener() 方法注册一个 ChannelFutureListener，当操作执行成功或者失败时，监听就会自动触发返回结果。  
&emsp; 并且，还可以通过ChannelFuture 的 channel() 方法获取关联的Channel。  

```java
public interface ChannelFuture extends Future<Void> {
    Channel channel();

    ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> var1);
     ......

    ChannelFuture sync() throws InterruptedException;
}
```
&emsp; 另外，还可以通过 ChannelFuture 接口的 sync()方法让异步的操作变成同步的。  

### 1.4.4. ChannelHandler 和 ChannelPipeline  
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


## 1.5. 简单示例  

<!-- 
https://www.cnblogs.com/jmcui/p/9154842.html
-->
&emsp; 所有的Netty服务端/客户端都至少需要两个部分：  
1. 至少一个ChannelHandler —— 该组件实现了对数据的处理。  
2. 引导 —— 这是配置服务器的启动代码。  

&emsp; 服务端：  

```java
public class EchoServer {

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        final EchoServerHandler serverHandler = new EchoServerHandler();
        //1、创建EventLoopGroup以进行事件的处理，如接受新连接以及读/写数据
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            //2、创建ServerBootstrap，引导和绑定服务器
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group, group)
                    //3、指定所使用的NIO传输Channel
                    .channel(NioServerSocketChannel.class)
                    //4、使用指定的端口设置套接字地址
                    .localAddress(new InetSocketAddress(port))
                    //5、添加一个 EchoServerHandler 到子 Channel的 ChannelPipeline
                    //当一个新的连接被接受时，一个新的子Channel将会被创建，而 ChannelInitializer 将会把一个你的EchoServerHandler 的实例添加到该 Channel 的 ChannelPipeline 中
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(serverHandler);
                        }
                    });
            //6、异步地绑定服务器，调用sync()方法阻塞等待直到绑定完成
            ChannelFuture channelFuture = bootstrap.bind().sync();
            System.out.println(EchoServer.class.getName() + "started and listening for connections on" + channelFuture.channel().localAddress());
            //7、获取 Channel 的 CloseFuture，并且阻塞当前线程直到它完成
            channelFuture.channel().closeFuture().sync();

        } finally {
            //8、关闭 EventLoopGroup 释放所有的资源
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new EchoServer(9999).start();
    }
}
```

&emsp; EchoServerHandler.java  
```java
@ChannelHandler.Sharable //标识一个Channel-Handler 可以被多个Channel安全的共享
public class EchoServerHandler extends ChannelHandlerAdapter {


    /**
     * 对于每个传入的消息都要调用
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        System.out.println("Server received：" + in.toString(CharsetUtil.UTF_8));
        //将接收到的消息写给发送者，而不冲刷出站消息
        //ChannelHandlerContext 发送消息。导致消息向下一个ChannelHandler流动
        //Channel 发送消息将会导致消息从 ChannelPipeline的尾端开始流动
        ctx.write(in);
    }

    /**
     * 通知 ChannelHandlerAdapter 最后一次对channel-Read()的调用是当前批量读取中的最后一条消息
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //暂存于ChannelOutboundBuffer中的消息，在下一次调用flush()或者writeAndFlush()方法时将会尝试写出到套接字
        //将这份暂存消息冲刷到远程节点，并且关闭该Channel
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 在读取操作期间，有异常抛出时会调用
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
```

&emsp; 客户端：  

```java
public class EchoClient {

    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            //创建Bootstrap
            Bootstrap bootstrap = new Bootstrap();
            //指定 EventLoopGroup 以处理客户端事件；适应于NIO的实现
            bootstrap.group(group)
                    //适用于NIO传输的Channel类型
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    //在创建Channel时，向ChannelPipeline中添加一个EchoClientHandler实例
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoClientHandler());
                        }
                    });
            //连接到远程节点，阻塞等待直到连接完成
            ChannelFuture channelFuture = bootstrap.connect().sync();
            //阻塞，直到Channel 关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            //关闭线程池并且释放所有的资源
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new EchoClient("127.0.0.1", 9999).start();

        System.out.println("------------------------------------");

        new EchoClient("127.0.0.1", 9999).start();

        System.out.println("------------------------------------");

        new EchoClient("127.0.0.1", 9999).start();
    }
}
```

&emsp; EchoClientHandler.java  
```java
@ChannelHandler.Sharable //标记该类的实例可以被多个Channel共享
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * 当从服务器接收到一条消息时被调用
     *
     * @param ctx
     * @param msg ByteBuf (Netty 的字节容器) 作为一个面向流的协议，TCP 保证了字节数组将会按照服务器发送它们的顺序接收
     * @throws Exception
     */
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println("Client" + ctx.channel().remoteAddress() + "connected");
        System.out.println(msg.toString(CharsetUtil.UTF_8));
    }

    /**
     * 在到服务器的连接已经建立之后将被调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx)  {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rock!", CharsetUtil.UTF_8));
    }


    /**
     * 在处理过程中引发异常时被调用
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
```
