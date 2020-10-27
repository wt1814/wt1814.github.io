<!-- TOC -->

- [1. 启动器Bootstrap](#1-启动器bootstrap)
    - [1.1. Netty服务端创建](#11-netty服务端创建)
        - [1.1.1. ServerBootstrap 示例](#111-serverbootstrap-示例)
        - [1.1.2. Netty服务端创建时序图](#112-netty服务端创建时序图)
        - [1.1.3. Netty服务端创建源码分析](#113-netty服务端创建源码分析)
        - [1.1.4. 客户端接入源码分析](#114-客户端接入源码分析)
    - [1.2. Netty客户端创建](#12-netty客户端创建)

<!-- /TOC -->

# 1. 启动器Bootstrap  

## 1.1. Netty服务端创建  
<!-- 
《Netty权威指南》第13章
高性能 Netty 源码解析之服务端创建 
 https://mp.weixin.qq.com/s/ZvLLSxA42aEWjXvRvot74A
-->

### 1.1.1. ServerBootstrap 示例  
&emsp; 在netty源码包中，执行 io.netty.example.echo.EchoServer 的 #main(args) 方法，启动服务端。EchoServer源码如下：  

```java
public final class EchoServer {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        // 配置 SSL
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        // Configure the server.
        // 创建两个 EventLoopGroup 对象
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 创建 boss 线程组 用于服务端接受客户端的连接
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 创建 worker 线程组 用于进行 SocketChannel 的数据读写
        // 创建 EchoServerHandler 对象
        final EchoServerHandler serverHandler = new EchoServerHandler();
        try {
            // 创建 ServerBootstrap 对象
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup) // 设置使用的 EventLoopGroup
             .channel(NioServerSocketChannel.class) // 设置要被实例化的为 NioServerSocketChannel 类
             .option(ChannelOption.SO_BACKLOG, 100) // 设置 NioServerSocketChannel 的可选项
             .handler(new LoggingHandler(LogLevel.INFO)) // 设置 NioServerSocketChannel 的处理器
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception { // 设置连入服务端的 Client 的 SocketChannel 的处理器
                     ChannelPipeline p = ch.pipeline();
                     if (sslCtx != null) {
                         p.addLast(sslCtx.newHandler(ch.alloc()));
                     }
                     //p.addLast(new LoggingHandler(LogLevel.INFO));
                     p.addLast(serverHandler);
                 }
             });

            // Start the server.
            // 绑定端口，并同步等待成功，即启动服务端
            ChannelFuture f = b.bind(PORT).sync();

            // Wait until the server socket is closed.
            // 监听服务端关闭，并阻塞等待
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            // 优雅关闭两个 EventLoopGroup 对象
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
```

* 第 7 至 15 行：配置 SSL ，暂时可以忽略。
* 第 17 至 20 行：创建两个 EventLoopGroup 对象。
    * boss 线程组：用于服务端接受客户端的连接。
    * worker 线程组：用于进行客户端的 SocketChannel 的数据读写。
    * 关于为什么是两个 EventLoopGroup 对象，我们在后续的文章，进行分享。
* 第 22 行：创建 io.netty.example.echo.EchoServerHandler 对象。
* 第 24 行：创建 ServerBootstrap 对象，用于设置服务端的启动配置。
    * 第 26 行：调用 #group(EventLoopGroup parentGroup, EventLoopGroup childGroup) 方法，设置使用的 EventLoopGroup 。
    * 第 27 行：调用 #channel(Class<? extends C> channelClass) 方法，设置要被实例化的 Channel 为 NioServerSocketChannel 类。在下文中，我们会看到该 Channel 内嵌了 java.nio.channels.ServerSocketChannel 对象。是不是很熟悉 😈 ？
    * 第 28 行：调用 #option(ChannelOption<T> option, T value) 方法，设置 NioServerSocketChannel 的可选项。在 io.netty.channel.ChannelOption 类中，枚举了相关的可选项。
    * 第 29 行：调用 #handler(ChannelHandler handler) 方法，设置 NioServerSocketChannel 的处理器。在本示例中，使用了 io.netty.handler.logging.LoggingHandler 类，用于打印服务端的每个事件。详细解析，见后续文章。
    * 第 30 至 40 行：调用 #childHandler(ChannelHandler handler) 方法，设置连入服务端的 Client 的 SocketChannel 的处理器。在本实例中，使用 ChannelInitializer 来初始化连入服务端的 Client 的 SocketChannel 的处理器。
* 第 44 行：先调用 #bind(int port) 方法，绑定端口，后调用 ChannelFuture#sync() 方法，阻塞等待成功。这个过程，就是“启动服务端”。
* 第 48 行：先调用 #closeFuture() 方法，监听服务器关闭，后调用 ChannelFuture#sync() 方法，阻塞等待成功。😈 注意，此处不是关闭服务器，而是“监听”关闭。
* 第 49 至 54 行：执行到此处，说明服务端已经关闭，所以调用 EventLoopGroup#shutdownGracefully() 方法，分别关闭两个 EventLoopGroup 对象。

### 1.1.2. Netty服务端创建时序图  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-28.png)  
&emsp; 上面是关于启动一个 Netty 的客户端或服务端的一个流程时序图。从步骤1可以看到，ServerBootstrap 是直接面对用户的，用户通过 ServerBootstrap 来进行设置的。ServerBootstrap 是 Netty 的启动辅助类。它的功能就是提供方法来配置启动的参数。底层是通过门面模式对各种能力进行抽象和封装，这样避免了像原生那样，需要认识并操作多个类才能启动一个应用，这是降低开发难度的途径之一。同时因为这样而引起需要配置或可以配置参数过多，ServerBootstrap 使用了 Builder 模式来解决了这个问题。  
&emsp; 在步骤2，核心内容是给 ServerBootstrap 设置并绑定 Reactor 线程池（Reactor 线程池指的是线程模型 Reactor 模式）。ServerBootstrap 通过 group 方法设置单或双线程池。在 Netty 中，所有线程池的父类是 EventLoop，EventLoop 负责处理注册到本线程多路复用 Selector 上面的 Channel，Selector 的轮询操作由绑定的 EventLoop 线程 run 方法驱动，在一个循环体内循环执行。Netty 的 EventLoop 不仅可以处理网络 IO 事件，而且还可以处理用户自定义的 Task 和定时任务 Task。这样个中间都可以由同一个 EventLoop 进行处理。从调度层面看，不存在从 EventLoop 线程中启动其他类型的线程用于异步执行另外的任务，这样避免了多线程并发操作和锁竞争，提升了 IO 线程的处理和调度性能。  
&emsp; 在步骤3，核心内容是设置 ServerSocketChannel 类型并通过 ServerBootstrap 的方法绑定到服务端。这一步比较简单和关键，Netty 已经封装好了 Channel 的初始化等底层实现细节和工作原理（也就是 Netty 会怎么使用 Channel 工作），所以用户只需要在服务端使用 NIO 模式，那就设置 NioServerSocketChannel.class 即可。通过 ServerBootstrap 的 channel() 方法设置进去后，实际上背后 Netty 会根据 class 反射得到实例对象。由于只在启动时才会进行反射调用，所以对性能的影响并不大。  
&emsp; 在步骤4，核心内容是EventLoop 会在 TCP 链路建立时创建 ChannelPipeline。  
&emsp; 在步骤5，核心内容是添加并设置 ChannelHandler。ChannelHandler 之前也说过，其实你可以当它作是 Service 层。当有外部请求进行的时候，最后实际的业务逻辑处理的地方都是在 ChannelHandler 里面的。但是，它不仅仅是限于业务逻辑，它还可以充当“过滤器”（编解码），“拦截器”（安全认证，限流等），可谓是多合一的组合。之所以说 Netty 是简洁好用，那是因为它提供了许多已经实现的 ChannelHandler 供我们使用。例如  

|说明|类名|
|---|---|
|系统编解码框架	|ByteToMessageCodec|
|通用基于长度的半包解码器|LengthFieldBasedFrameDecoder|
|码流日志打印|LoggingHandler|
|SSL 安全认证|SslHandler|
|链路空闲检测|IdleStateHandler|
|流量整形|ChannelTrafficShapingHandler|
|Base64 编解码|Base64Decoder 和 Base64Encoder|

&emsp; 在步骤6，步骤7，核心都是组件内部已经开始启动运行内部代码了，剩下只需要等待客户端请求和业务逻辑处理了。

### 1.1.3. Netty服务端创建源码分析  


### 1.1.4. 客户端接入源码分析  



## 1.2. Netty客户端创建
<!-- 
《Netty权威指南》第14章
https://mp.weixin.qq.com/s?__biz=MzIxNTQzMDM0Ng==&mid=2247483815&idx=1&sn=df1ffcf0cdc5bdd6db25beb689cf6640&chksm=97992402a0eead1445a8cfcfd043818b3c37c5ed230a874c19d67604e3833ec60025aaf8ea84&scene=178&cur_album_id=1471431037254680580#rd
-->




