<!-- TOC -->

- [1. Netty客户端创建](#1-netty客户端创建)
    - [1.1. ServerBootstrap使用示例](#11-serverbootstrap使用示例)
    - [1.2. Netty客户端创建时序图](#12-netty客户端创建时序图)
    - [1.3. Netty客户端创建源码分析](#13-netty客户端创建源码分析)
        - [1.3.1. doConnect 连接服务端](#131-doconnect-连接服务端)
        - [1.3.2. 异步连接结果通知](#132-异步连接结果通知)
        - [1.3.3. 超时机制](#133-超时机制)

<!-- /TOC -->



# 1. Netty客户端创建
<!--
~~

-->

<!-- 
《Netty权威指南》第14章
https://mp.weixin.qq.com/s?__biz=MzIxNTQzMDM0Ng==&mid=2247483815&idx=1&sn=df1ffcf0cdc5bdd6db25beb689cf6640&chksm=97992402a0eead1445a8cfcfd043818b3c37c5ed230a874c19d67604e3833ec60025aaf8ea84&scene=178&cur_album_id=1471431037254680580#rd
Netty客户端启动源码分析
https://mp.weixin.qq.com/s/54wmqi2Y3_E6o_hCLxsJKw
-->

&emsp; 相比于服务端，客户端考虑的东西更多：  

* 线程模型
* 异步连接
* 客户端连接超时

## 1.1. ServerBootstrap使用示例  
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
            // 创建 Bootstrap 对象
            Bootstrap b = new Bootstrap();
            b.group(group) // 设置使用的 EventLoopGroup
             .channel(NioSocketChannel.class) // 设置要被实例化的为 NioSocketChannel 类
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

&emsp; <font color = "lime">**Netty启动客户端：**</font>  
1. 创建Bootstrap客户端启动对象。  
2. 配置workerGroup，负责处理连接的读写就绪事件。  
3. 配置父Channel，一般为NioSocketChannel。  
4. 给父Channel配置参数。  
5. 配置父Channel与Handler之间的关系。  
6. 连接服务器。

## 1.2. Netty客户端创建时序图  
![image](http://182.92.69.8:8081/img/microService/netty/netty-29.png)  

<!-- 
&emsp; <font color = "lime">**客户端创建的流程有以下步骤：**</font>

1. 创建 Bootstrap 实例，通过API设置客户端的参数，异步发起连接  
2. 创建客户端连接，IO读写的Reactor线程组NioEventLoopGroup  
3. 指定 Channel 类型  
4. 创建默认的 Channel Handler Pipeline，用于调度和执行网络事件  
5. 异步发起 TCP 连接，判断连接是否成功。如果成功，则直接将 NioSocketChannel 注册到多路复用器上。监听读操作位，用于数据报读取和消息发送；如果不成功，则注册连接监听位到多路复用器，等待连接结果。  
6. 注册对应的网络监听状态位到多路复用器  
7. 由多路复用器在 IO 轮询各 Channel，处理连接结果  
8. 如果来连接成功，设置 Future 结果，发送连接成功事件，触发 ChannelPipileline  
9. 由 ChannelPipileline 调度执行系统和用户的 ChannelHandler 执行业务逻辑  
-->

## 1.3. Netty客户端创建源码分析  
&emsp; 第一步是设置 IO 线程组的接口。由于客户端相对于服务端来说，只需要一个处理 IO 的读写线程组。

```java
public B group(EventLoopGroup group) {
    ObjectUtil.checkNotNull(group, "group");
    if (this.group != null) {
        throw new IllegalStateException("group set already");
    }
    this.group = group;
    return self();
}
```
&emsp; 然后是 TCP 的参数。

```java
public <T> B option(ChannelOption<T> option, T value) {
    //...
    synchronized (options) {
        if (value == null) {
            options.remove(option);
        } else {
            options.put(option, value);
        }
    }
    return self();
}
```
&emsp; TCP 的参数主要有这些：  

|参数名称|	作用|
|---|---|
|SO_TIMEOUT	|控制读取操作将阻塞多少毫秒。如果返回 0，计时器就被禁止，该线程无限期阻塞|
|SO_SNDBUF	|套接字使用的发送缓冲区大小|
|SO_RCVBUF	|套接字使用的接受缓冲区大小|
|SO_REUSEADDR	|用于决定如果网络上仍然有数据向旧的 ServerSocket 传输数据，是否允许新的 ServerSocket 绑定到与旧的 ServerSocket 同样的端口上。SO_REUSEADDR 选项的默认值与操作系统有关，在某些系统中，允许重用端口，而在某些系统上不允许重用端口|
|CONNECT_TIMEOUT_MILLIS	|客户端连接超时时间，由于 NIO 原生客户端不提供连接超时接口，因此 Netty 采用的是自定义的连接超时定时器负责检测和超时控制。|
|TCP_NODELAY|激活或禁止 TCP_NODELAY 套接字选项，它决定是否使用 Nagle 算法。如果是时间敏感型的应用，建议关闭 Nagle 算法|

&emsp; 设置完参数，就是进行 channel 的设置。与服务端不一样的是，客户端是用的 NioSocketChannel。这里依旧是使用反射工厂实例化 ReflectiveChannelFactory。  

```java
public B channel(Class<? extends C> channelClass) {
    return channelFactory(new ReflectiveChannelFactory<C>(
            ObjectUtil.checkNotNull(channelClass, "channelClass")
    ));
}
```
&emsp; 然后开始设置 handler。示例代码中使用的是 ChannelInitializer，实际上它实现了 ChannelInboundHandlerAdapter 这个 adapter。当 TCP 链路注册成功之后，调用 channelRegistered 方法，channelRegistered 方法就会进行 initChannel 的方法调用实现 ChannelHandler 的设置。  

```java
public final void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    // 注册成功成功后，就调用 initChannel 方法
    if (initChannel(ctx)) {
        // 调用完 initChannel 后就调用 pipeline 中的每一个 Channel
        ctx.pipeline().fireChannelRegistered();

        // 同时就可以移除这个作用是为了初始化的 Channel
        removeState(ctx);
    } else {
        // Called initChannel(...) before which is the expected behavior, so just forward the event.
        ctx.fireChannelRegistered();
    }
}
```
&emsp; 上面的 initChannel 主要是将当前的 ChannelContext 传进去进行初始化  

```java
private boolean initChannel(ChannelHandlerContext ctx) throws Exception {
    if (initMap.add(ctx)) { // 这里是防止重复初始化
        try { 
            //这里才是真正调用 initChannel 方法
            //这里是示例里面初始化的 channelHandler
            initChannel((C) ctx.channel());
        } catch (Throwable cause) {
            // 如果发生异常就传递异常
            exceptionCaught(ctx, cause);
        } finally {
            ChannelPipeline pipeline = ctx.pipeline();
            if (pipeline.context(this) != null) {
                pipeline.remove(this);
            }
        }
        return true;
    }
    return false;
}
```
&emsp; 最后一步比较重要，就是发起客户端的连接，如下  

```java
ChannelFuture f = b.connect(host,port).sync();
```
&emsp; 前面基本上的步骤都是进行客户端的设置。现在去看看 connect 的代码究竟做了什么。  

```java
private ChannelFuture doResolveAndConnect(final SocketAddress remoteAddress, final SocketAddress localAddress) {
    // 新建一个 Channel 并注册
    final ChannelFuture regFuture = initAndRegister();
    final Channel channel = regFuture.channel();
    // 如果注册完毕了，就进行监听
    
    if (regFuture.isDone()) {
        if (!regFuture.isSuccess()) {
            return regFuture;
        }
        return doResolveAndConnect0(channel, remoteAddress, localAddress, channel.newPromise());
    } else {	
        // 注册没有完成，就封装成 PendingRegistrationPromise 等待注册完成
        final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
        regFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                // Directly obtain the cause and do a null check so we only need one volatile read in case of a
                // failure.
                Throwable cause = future.cause();
                if (cause != null) {
                    // 设置失败的信息
                    promise.setFailure(cause);
                } else {
                    // 如果注册成功，就进行连接
                    promise.registered();
                    doResolveAndConnect0(channel, remoteAddress, localAddress, promise);
                }
            }
        });
        return promise;
    }
}
```
### 1.3.1. doConnect 连接服务端  
&emsp; 上面的流程跟服务端差不多，主要区别在于 doResolveAndConnect0 方法。进去看看  

```java
private ChannelFuture doResolveAndConnect0(final Channel channel, SocketAddress remoteAddress,
                                            final SocketAddress localAddress, final ChannelPromise promise) {
    try {
        final EventLoop eventLoop = channel.eventLoop();
        AddressResolver<SocketAddress> resolver;
        try {
            //根据 eventloop 获取解析器
            resolver = this.resolver.getResolver(eventLoop);
        } catch (Throwable cause) {
            channel.close();
            return promise.setFailure(cause);
        }
        
        // 解析器不知道如何处理指定的远程地址，或者它已经被解析。
        if (!resolver.isSupported(remoteAddress) || resolver.isResolved(remoteAddress)) {
            doConnect(remoteAddress, localAddress, promise);
            return promise;
        }

        final Future<SocketAddress> resolveFuture = resolver.resolve(remoteAddress);
        // 如果解析完成的化
        if (resolveFuture.isDone()) {
            final Throwable resolveFailureCause = resolveFuture.cause();
            // 查看有没有报错，如果有就立马关闭
            if (resolveFailureCause != null) {
                channel.close();
                promise.setFailure(resolveFailureCause);
            } else {
                // 
                doConnect(resolveFuture.getNow(), localAddress, promise);
            }
            return promise;
        }

        // 如果上面没执行完，索性加个监听器等待解析完毕
        resolveFuture.addListener(new FutureListener<SocketAddress>() {
            @Override
            public void operationComplete(Future<SocketAddress> future) throws Exception {
                if (future.cause() != null) {
                    channel.close();
                    promise.setFailure(future.cause());
                } else {
                    //如果没有报错，就开始连接
                    doConnect(future.getNow(), localAddress, promise);
                }
            }
        });
    } catch (Throwable cause) {
        promise.tryFailure(cause);
    }
    return promise;
}
```
&emsp; 执行完上面的代码，最终还是要进行一个 connect 方法  

```java
private static void doConnect(
        final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise connectPromise) {

    // 获取 channel，往 channel 里面塞任务
    final Channel channel = connectPromise.channel();
    channel.eventLoop().execute(new Runnable() {
        @Override
        public void run() {
            //鉴定地址是否为空
            if (localAddress == null) {
                channel.connect(remoteAddress, connectPromise);
            } else {
                channel.connect(remoteAddress, localAddress, connectPromise);
            }
            connectPromise.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        }
    });
}
```
&emsp; 从这里开始，连接操作切换到了 Netty 的 NIO 线程 NioEventLoop 中进行，此时客户端返回，连接操作异步执行。NioEventLoop 进行服务端连接，一般有三种情况  

    连接成功，返回 true
    暂时无法连接成功，服务端没有 ack 应答，所以返回 false
    连接失败，抛出异常

&emsp; 如果是第二种的话，Netty 会继续将 NioEventLoop 的 selectionKey 继续设为 OP_CONNECT，然 selector 继续进行轮询。  

```java
protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
    if (localAddress != null) {
        doBind0(localAddress);
    }

    boolean success = false;
    try {
        boolean connected = SocketUtils.connect(javaChannel(), remoteAddress);
        //如果返回 false 继续连接
        if (!connected) {
            selectionKey().interestOps(SelectionKey.OP_CONNECT);
        }
        success = true;
        return connected;
    } finally {
        //如果失败了就直接关闭链路，进入失败处理流程
        if (!success) {
            doClose();
        }
    }
}
```
&emsp; 目前为止，客户端的已经完成了“发起连接”这个动作了。如果成功还好，如果是第二种情况返回 false，后面成功了怎么处理呢？继续往下走~  

### 1.3.2. 异步连接结果通知  
&emsp; 由于上面再NioSocketChannel如果暂时不能成功注册的话，就返回继续将 selectionKey设为OP_CONNECT。那么后续交给了“线程池” NioEventLoopGroup继续去监听实行。NioEventLoopGroup是一个线程池集合。而里面每一个线程池其实是NioEventLoop。所以后续异步连接结果还是由 NioEventLoop 继续处理。去看看它的源码。  

```java
private void processSelectedKey(SelectionKey k, AbstractNioChannel ch) {
    //...
    try {
        //获取 selectionkey
        int readyOps = k.readyOps();
        //判断是不是连接事件
        if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
            // 更改操作位
            int ops = k.interestOps();
            ops &= ~SelectionKey.OP_CONNECT;
            k.interestOps(ops);
            //关闭连接
            unsafe.finishConnect();
        }
    } 
}
```
&emsp; 而 unsafe.finishConnect 的方法是  

```java
public final void finishConnect() {
    assert eventLoop().inEventLoop();

    try {
        //查看是否活跃
        boolean wasActive = isActive();
        //判断 JDK 的 SocketChannel 的连接结果，
        //如果是 true 则是成功
        //如果是其他或异常则失败
        doFinishConnect();
        fulfillConnectPromise(connectPromise, wasActive);
    } catch (Throwable t) {
        fulfillConnectPromise(connectPromise, annotateConnectException(t, requestedRemoteAddress));
    } 
}
```
&emsp; 最后调用了 fulfillConnectPromise 方法  

```java
private void fulfillConnectPromise(ChannelPromise promise, boolean wasActive) {
    if (promise == null) {
        return;
    }
    boolean active = isActive();
    boolean promiseSet = promise.trySuccess();
    if (!wasActive && active) {
        pipeline().fireChannelActive();
    }
    if (!promiseSet) {
        close(voidPromise());
    }
}
```
&emsp; 上面主要是如果是 channel 是活跃 active 状态就直接激活链路了，调用 fireChannelActive 让事件在 Pipeline 上面传播。fireChannelActive 主要是修改网络监听位ww为读操作。  

### 1.3.3. 超时机制  
&emsp; 上面讲了连接失败的情况，如果客户端连接超时怎么办呢？要知道原生的 Java NIO 过于简洁（同时也是一种好处吧，给了开发一个极大的发挥空间），所以 Netty 要自己实现这个超时的机制。  
&emsp; 首先，需要使用 Netty 的超时机制，要在客户端启动的时候进行设置  

```java
b.group(group).channel(NioSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
```
&emsp; 发起连接的同时，启动超时检测定时器  

```java
int connectTimeoutMillis = config().getConnectTimeoutMillis();
if(connectTimeoutMillis>0) {
	connectTimeoutFuture = eventLoop().schedule(new Runnable() {
    	@Override
        public void run() {
        	ChannelPromise connectPromise = AbstractNioChannel.this.connectPromise;
            ConnectTimeoutException cause = 
            			new ConnectTimeoutException("connect time out : " + remoteAddress);
            if(connectPromise!=null&&connectPromise.tryFailure()) {
            	close(voidPromise());
            }
        }
    }, connectTimeoutMillis, TimeUnit.MILLISECONDS);
}
```
&emsp; 当超时定时器的任务发现可客户端连接超时，就构造异常设置到 connectPromise，关闭客户端连接释放句柄。最后还需要异常定时器  

```java
if(connectTimeoutFuture!=null) {
    connectTimeoutFuture.cancel(false);
}
connectPromise = null;
```
