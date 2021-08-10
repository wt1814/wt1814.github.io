
<!-- TOC -->

- [1. Netty运行流程](#1-netty运行流程)
    - [1.1. Netty服务端启动](#11-netty服务端启动)
    - [1.2. Netty客户端启动](#12-netty客户端启动)
    - [1.3. Netty整体运行流程](#13-netty整体运行流程)

<!-- /TOC -->


# 1. Netty运行流程
<!-- 
https://blog.csdn.net/Yang_Hai_Long_1_2/article/details/78380358
-->


## 1.1. Netty服务端启动  
&emsp; 在netty源码包中，执行io.netty.example.echo.EchoServer的#main(args) 方法，启动服务端。EchoServer源码如下：   

```java
public final class EchoServer {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    public static void main(String[] args) throws Exception {
        // 配置 SSL
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        // 因为Netty用的是主从模式的开发模式，所以要设置二个主从组给这个服务器引擎,
        // 同时这个二个group可以在创建的时候可以指定这个组当中可以有多少个线程，默认是1个
        // 创建boss与worker线程组（事件循环组）
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 创建 boss 线程组 用于服务端接受客户端的连接
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 创建 worker 线程组 用于进行 SocketChannel 的数据读写
        // 创建 EchoServerHandler 对象
        final EchoServerHandler serverHandler = new EchoServerHandler();
        try {
            // 创建服务器启动辅助类，服务端是 ServerBootstrap
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup); // 设置使用的 EventLoopGroup
            
            // 进行配置这个服务器引擎ServerBootStrap，刚创建时是不能直接启动这个这个服务器引擎的，要配置三个基本的配置的
            // 这个三个基本的配置如下:channel,handler,option
            b.channel(NioServerSocketChannel.class) // 设置要被实例化的为 NioServerSocketChannel 类
            .option(ChannelOption.SO_BACKLOG, 100) // 设置 NioServerSocketChannel 的可选项，这个表示的是如果服务器如果如果不过来这么多的通道，最多只能让100个通道时进行阻塞等待
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
            // 绑定服务器端口并启动服务器，同步等待服务器启动完毕
            ChannelFuture f = b.bind(PORT).sync();

            // Wait until the server socket is closed.
            // 监听服务端关闭，并阻塞等待
            // 阻塞启动线程，并同步等待服务器关闭，因为如果不阻塞启动线程，则会在finally块中执行优雅关闭，导致服务器也会被关闭了
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

&emsp; **<font color = "clime">服务器执行流程概述：</font>**  
1. 创建服务器启动辅助类，服务端是ServerBootstrap。需要设置事件循环组EventLoopGroup，如果使用reactor主从模式，需要创建2个： **<font color = "clime">创建boss线程组用于服务端接受客户端的连接；创建worker线程组用于进行 SocketChannel的数据读写。</font>**  
2. 对ServerBootstrap进行配置，配置项有channel,handler,option。  
3. 绑定服务器端口并启动服务器，同步等待服务器启动完毕。  
4. 阻塞启动线程，并同步等待服务器关闭，因为如果不阻塞启动线程，则会在finally块中执行优雅关闭，导致服务器也会被关闭了。  


## 1.2. Netty客户端启动  
&emsp; 在netty源码包中，EchoClient示例如下：  

```java
public final class EchoClient {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    public static void main(String[] args) throws Exception {
        // Configure SSL.git
        // 配置 SSL
        final SslContext sslCtx;
        if (SSL) {
            sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        // Configure the client.
        // 创建一个 EventLoopGroup 对象
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // 创建事件组对象
            Bootstrap b = new Bootstrap();
            b.group(group); // 设置使用的 EventLoopGroup

            b.channel(NioSocketChannel.class) // 设置要被实例化的为 NioSocketChannel 类
            .option(ChannelOption.TCP_NODELAY, true) // 设置 NioSocketChannel 的可选项
            .handler(new ChannelInitializer<SocketChannel>() { // 设置 NioSocketChannel 的处理器
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    if (sslCtx != null) {
                        p.addLast(sslCtx.newHandler(ch.alloc(), HOST, PORT));
                    }
                    //p.addLast(new LoggingHandler(LogLevel.INFO));
                    p.addLast(new EchoClientHandler());
                }
            });

            // Start the client.
            // 连接服务器，并同步等待成功，即启动客户端
            ChannelFuture f = b.connect(HOST, PORT).sync();

            // Wait until the connection is closed.
            // 监听客户端关闭，并阻塞等待
            f.channel().closeFuture().sync();
        } finally {
            // Shut down the event loop to terminate all threads.
            // 优雅关闭一个 EventLoopGroup 对象
            group.shutdownGracefully();
        }
    }
}
```

&emsp; **netty客户端执行流程概述：(与服务端类似)**  
1. 创建服务端启动辅助类Bootstrap。需要设置事件循环组EventLoopGroup。  
2. 对Bootstrap进行配置，配置项有channel,handler,option。  
3. 绑定端口并启动服务端，同步等待服务器启动完毕。  
4. 阻塞启动线程，并同步等待服务器关闭，因为如果不阻塞启动线程，则会在finally块中执行优雅关闭，导致服务器也会被关闭了。  

## 1.3. Netty整体运行流程
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-87.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-44.png)  
