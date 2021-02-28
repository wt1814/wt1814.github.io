
<!-- TOC -->

- [1. Netty实战](#1-netty实战)
    - [1.1. Netty常见应用场景](#11-netty常见应用场景)
    - [1.2. 简单示例](#12-简单示例)
    - [1.3. 使用Netty实现IM聊天](#13-使用netty实现im聊天)
    - [1.4. 基于Netty搭建消息推送系统](#14-基于netty搭建消息推送系统)

<!-- /TOC -->

# 1. Netty实战  
<!-- 
Netty主要应用于以下领域：  
* 高性能的RPC框架：常用于微服务之间的高性能远程调用(如Dubbo)
* 游戏行业：Netty可以很轻松地定制和开发一个私有协议栈，
* 即时通讯：Netty基于Java NIO，并且做了一些优化，支持高性能的即时通讯
-->

## 1.1. Netty常见应用场景
&emsp; Netty的常见应用场景：(Netty主要用来做网络通信)  

* 作为RPC框架的网络通信工具：在分布式系统中，不同服务节点之间经常需要相互调用，这个时候就需要RPC框架了。不同服务节点之间的通信是如何做的呢？可以使用Netty来做。比如调用另外一个节点的方法的话，至少是要让对方知道调用的是哪个类中的哪个方法以及相关参数吧！
* 实现一个自己的HTTP服务器：通过Netty可以自己实现一个简单的HTTP服务器(作为Java后端开发，一般使用Tomcat作为HTTP服务器比较多)。一个最基本的HTTP服务器可要以处理常见的HTTP Method的请求，比如POST请求、GET请求等等。
* 实现一个即时通讯系统：使用Netty可以实现一个可以聊天类似微信的即时通讯系统。
* 实现消息推送系统：市面上有很多消息推送系统都是基于Netty 来做的。

## 1.2. 简单示例  
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

## 1.3. 使用Netty实现IM聊天  
<!-- 
使用 Netty 实现 IM 聊天贼简单，看不懂就锤爆艿艿的狗头~ 
https://mp.weixin.qq.com/s/5X1znb_G61CV6NxJ_MvmZw
-->

## 1.4. 基于Netty搭建消息推送系统  
<!-- 
参考《Netty4核心原理与手写RPC框架实战》第4篇
-->

