<!-- TOC -->

- [1. Netty服务端创建](#1-netty服务端创建)
    - [1.1. ServerBootstrap示例](#11-serverbootstrap示例)
    - [1.2. Netty服务端创建时序图](#12-netty服务端创建时序图)
    - [1.3. Netty服务端创建源码分析](#13-netty服务端创建源码分析)
    - [1.4. 客户端接入源码分析](#14-客户端接入源码分析)

<!-- /TOC -->

# 1. Netty服务端创建  
<!-- 
高性能 Netty 源码解析之服务端创建 
https://mp.weixin.qq.com/s/ZvLLSxA42aEWjXvRvot74A
-->

<!-- 
《Netty权威指南》第13章
Netty服务端启动源码分析
https://mp.weixin.qq.com/s/PKwt7cN1hRbqmEvAmSDcOA
-->

## 1.1. ServerBootstrap示例  
&emsp; 在netty源码包中，执行io.netty.example.echo.EchoServer的#main(args) 方法，启动服务端。EchoServer源码如下：  

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

* 第 7 至 15 行：配置SSL ，暂时可以忽略。
* 第 17 至 20 行：创建两个EventLoopGroup对象。
    * boss 线程组：用于服务端接受客户端的连接。
    * worker 线程组：用于进行客户端的SocketChannel的数据读写。
* 第 22 行：创建 io.netty.example.echo.EchoServerHandler 对象。
* 第 24 行：创建 ServerBootstrap 对象，用于设置服务端的启动配置。
    * 第 26 行：调用 #group(EventLoopGroup parentGroup, EventLoopGroup childGroup) 方法，设置使用的 EventLoopGroup 。
    * 第 27 行：调用 #channel(Class<? extends C> channelClass) 方法，设置要被实例化的 Channel 为 NioServerSocketChannel 类。该 Channel 内嵌了 java.nio.channels.ServerSocketChannel 对象。
    * 第 28 行：调用 #option(ChannelOption<T> option, T value) 方法，设置 NioServerSocketChannel 的可选项。在 io.netty.channel.ChannelOption 类中，枚举了相关的可选项。
    * 第 29 行：调用 #handler(ChannelHandler handler) 方法，设置 NioServerSocketChannel 的处理器。在本示例中，使用了 io.netty.handler.logging.LoggingHandler 类，用于打印服务端的每个事件。
    * 第 30 至 40 行：调用 #childHandler(ChannelHandler handler) 方法，设置连入服务端的 Client 的 SocketChannel 的处理器。在本实例中，使用 ChannelInitializer 来初始化连入服务端的 Client 的 SocketChannel 的处理器。
* 第 44 行：先调用 #bind(int port) 方法，绑定端口，后调用 ChannelFuture#sync() 方法，阻塞等待成功。这个过程，就是“启动服务端”。
* 第 48 行：先调用 #closeFuture() 方法，监听服务器关闭，后调用 ChannelFuture#sync() 方法，阻塞等待成功。注意，此处不是关闭服务器，而是“监听”关闭。
* 第 49 至 54 行：执行到此处，说明服务端已经关闭，所以调用 EventLoopGroup#shutdownGracefully() 方法，分别关闭两个 EventLoopGroup 对象。


## 1.2. Netty服务端创建时序图  
![image](http://www.wt1814.com/static/view/images/microService/netty/netty-28.png)  

<!-- 
&emsp; 上面是关于启动一个Netty的客户端或服务端的一个流程时序图。从步骤1可以看到，ServerBootstrap 是直接面对用户的，用户通过 ServerBootstrap 来进行设置的。ServerBootstrap 是 Netty 的启动辅助类。它的功能就是提供方法来配置启动的参数。底层是通过门面模式对各种能力进行抽象和封装，这样避免了像原生那样，需要认识并操作多个类才能启动一个应用，这是降低开发难度的途径之一。同时因为这样而引起需要配置或可以配置参数过多，ServerBootstrap 使用了 Builder 模式来解决了这个问题。  
&emsp; 在步骤2，核心内容是给 ServerBootstrap 设置并绑定 Reactor 线程池(Reactor 线程池指的是线程模型 Reactor 模式)。ServerBootstrap 通过 group 方法设置单或双线程池。在 Netty 中，所有线程池的父类是 EventLoop，EventLoop 负责处理注册到本线程多路复用 Selector 上面的 Channel，Selector 的轮询操作由绑定的 EventLoop 线程 run 方法驱动，在一个循环体内循环执行。Netty 的 EventLoop 不仅可以处理网络 IO 事件，而且还可以处理用户自定义的 Task 和定时任务 Task。这样个中间都可以由同一个 EventLoop 进行处理。从调度层面看，不存在从 EventLoop 线程中启动其他类型的线程用于异步执行另外的任务，这样避免了多线程并发操作和锁竞争，提升了 IO 线程的处理和调度性能。  
&emsp; 在步骤3，核心内容是设置 ServerSocketChannel 类型并通过 ServerBootstrap 的方法绑定到服务端。这一步比较简单和关键，Netty 已经封装好了 Channel 的初始化等底层实现细节和工作原理(也就是 Netty 会怎么使用 Channel 工作)，所以用户只需要在服务端使用 NIO 模式，那就设置 NioServerSocketChannel.class 即可。通过 ServerBootstrap 的 channel() 方法设置进去后，实际上背后 Netty 会根据 class 反射得到实例对象。由于只在启动时才会进行反射调用，所以对性能的影响并不大。  
&emsp; 在步骤4，核心内容是EventLoop 会在 TCP 链路建立时创建 ChannelPipeline。  
&emsp; 在步骤5，核心内容是添加并设置 ChannelHandler。当有外部请求进行的时候，最后实际的业务逻辑处理的地方都是在 ChannelHandler 里面的。但是，它不仅仅是限于业务逻辑，它还可以充当“过滤器”(编解码)，“拦截器”(安全认证，限流等)，可谓是多合一的组合。之所以说 Netty 是简洁好用，那是因为它提供了许多已经实现的 ChannelHandler 供我们使用。例如  

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
-->

## 1.3. Netty服务端创建源码分析  
&emsp; 首先是步骤2，服务端使用的Reactor模型，所以需要创建两个 NioEventLoopGroup，它们都是属于 EventLoopGroup 的子类。  

```java
EventLoopGroup bossGroup = new NioEventLoopGroup();
EventLoopGroup workerGroup = new NioEventLoopGroup();
```

&emsp; **<font color = "red">bossGroup负责的是接受请求，workerGroup负责的是处理请求。通过 ServerBootstrap 的方法 group() 传入之后，会设置成为 ServerBootstrap 的 parentGroup 和 childGroup。</font>**  

```java
public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup) {
    super.group(parentGroup);
    if (this.childGroup != null) {
        //...
    } else {
        this.childGroup = (EventLoopGroup)ObjectUtil.checkNotNull(childGroup, "childGroup");
        return this;
    }
}
```
&emsp; 其中 parentGroup 是传给 ServerBootstrap 的父类 AbstractBootstrap。这是因为 AbstractBootstrap 需要使用 parentGroup 来注册监听外部请求(OP_ACCEPT 事件)的 Channel。这样子的话，无论是否使用 Reactor 模型，都可以在同一个 EventLoopGroup 中注册一个监听外部请求的 channel。  
&emsp; 接着是步骤3，是设置服务端的 Channel。

```java
b.channel(NioServerSocketChannel.class);
```
&emsp; 这个 channel 实际上是 AbstractBootstrap 抽象类的方法，因为这个是可以共用的。然后 AbstractBootstrap 会交给其成员函数 channelFactory()，通过实例化参数来控制怎么根据 class 来生成 channel。  

```java
public B channel(Class<? extends C> channelClass) {
    return this.channelFactory((io.netty.channel.ChannelFactory)(new ReflectiveChannelFactory((Class)ObjectUtil.checkNotNull(channelClass, "channelClass"))));
}
```
&emsp; 这个 Channel 工厂主要有两种: NioUdtProvider 和 ReflectiveChannelFactory。前者根据类型来创建，后者根据反射来实例化对象的。  
&emsp; Netty 通过 Channel 工厂类来创建不同类型的 Channel。对于服务端来说，需要创建的是 NioServerSocketChannel。  
&emsp; 设置完 Channel 后，需要进行 TCP 的一些参数。这些参数一般来说都在 ChannelOption 类里面。有兴趣的同学可以去连接一下。我们这里只设置一个参数  

```java
b.option(ChannelOption.SO_BACKLOG, 100)
```
&emsp; 作为服务端，主要是设置 TCP 的 backlog 参数，这个设置之后主要是调用底层 C 对应的接口。

```java
int listen(int fd, int backlog);
```
&emsp; 为什么要设置这个参数呢？这个参数实际上指定内核为此套接口排队的最大连接个数。对于给定的套接字接口，内核要维护两个对列：未连接队列和已连接队列。那是因为在 TCP 的三次握手过程中三个分节来分隔这两个队列。下面是整个过程的一个讲解：  

    如果服务器处于 listen 时，收到客户端的 syn 分节(connect)时在未完成队列中创建一个新的条目，然后用三路握手的第二个分节即服务器的 syn 响应客户端。
    新条目会直到第三个分节到达前(客户端对服务器 syn 的 ack)都会一直保留在未完成连接队列中，如果三路握手完成，该条目将从未完成队列搬到已完成队列的尾部。
    当进程调用 accept 时，从已完成队列的头部取一条目给进程，当已完成队列为空的时候进程就睡眠，直到有条目在已完成连接队列中才唤醒。

&emsp; 现在说到了重点，backlog 其实是两个队列的总和的最大值，大多数实现默认值为 5。但是高并发的情况之下，并不够用。因为可能客户端 syn 的到达以及等待三路握手第三个分节的到达延时而增大。 所以需要根据实际场景和网络状况进行灵活配置。  
&emsp; 接着是步骤5，是设置服务端的 Handler。Handler 分为两种，一种是 子类中的 Handler 是 NioServerSocketChannel 对应的 ChannelPipeline 的 Handler，另一种是父类中的 Handler 是客户端新接入的连接 SocketChannel 对应的 ChannelPipeline 的 Handler。  

```java
b.handler().childHandler();
```

&emsp; 上面代码有两个 handler 方法，区别在于 handler() 方法是 NioServerSocketChannel使用的，所有连接该监听端口的客户端都会执行它；父类 AbstractBootstrap 中的 Handler 是个工厂类，它为每个接入的客户端都创建一个新的 Handler。  

&emsp; 接着是步骤6，就是绑定本地端口然后启动服务。这是比较重要的一步，来分析下 ServerBootstrap 的 bind 方法。  

```java
private ChannelFuture doBind(final SocketAddress localAddress) {
    //初始化一个 channel，
    final ChannelFuture regFuture = initAndRegister();
    //获取 channel
    final Channel channel = regFuture.channel();
    if (regFuture.cause() != null) {
        return regFuture;
    }
    //如果这个 channel 的注册事件完成了
    if (regFuture.isDone()) {
        //再产生一个异步任务，进行端口监听
        ChannelPromise promise = channel.newPromise();
        doBind0(regFuture, channel, localAddress, promise);
        return promise;
    } else {
        //设置一个进度条的任务，等待注册事件完成后，就开始端口的监听
        final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
        regFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Throwable cause = future.cause();
                if (cause != null) {
                    promise.setFailure(cause);
                } else {
                    promise.registered();
                    doBind0(regFuture, channel, localAddress, promise);
                }
            }
        });
        return promise;
    }
}
```
&emsp; 上面有两个比较着重讲的方法 initAndRegister() 和 doBind0()。下面先看 initAndRegister() 方法。  

```java
final ChannelFuture initAndRegister() {
    Channel channel = null;
    try {
        //通过 channel 工厂生成一个 channel
        channel = channelFactory.newChannel();
        init(channel);
    } catch (Throwable t) {
        if (channel != null) {
            channel.unsafe().closeForcibly();
            return new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE).setFailure(t);
        }
        return new DefaultChannelPromise(new FailedChannel(), GlobalEventExecutor.INSTANCE).setFailure(t);
    }
    //将这个 channel 注册进 parentEventLoop
    ChannelFuture regFuture = config().group().register(channel);
    if (regFuture.cause() != null) {
        if (channel.isRegistered()) {
            channel.close();
        } else {
            channel.unsafe().closeForcibly();
        }
    }
```
&emsp; 在 ChannelFactory 生成一个 channel 后，就进行了 ServerBootstrap.init() 方法的调用。这个方法的主要作用是给 channel 进行一些参数和配置的设置。

```java
void init(Channel channel) {
    setChannelOptions(channel, newOptionsArray(), logger);	//设置 channel 的 option
    setAttributes(channel, attrs0().entrySet().toArray(EMPTY_ATTRIBUTE_ARRAY)); //设置属性

    ChannelPipeline p = channel.pipeline();

    final EventLoopGroup currentChildGroup = childGroup;
    final ChannelHandler currentChildHandler = childHandler;
    final Entry<ChannelOption<?>, Object>[] currentChildOptions;
    synchronized (childOptions) {
        currentChildOptions = childOptions.entrySet().toArray(EMPTY_OPTION_ARRAY);
    }
    final Entry<AttributeKey<?>, Object>[] currentChildAttrs = childAttrs.entrySet().toArray(EMPTY_ATTRIBUTE_ARRAY);

    p.addLast(new ChannelInitializer<Channel>() {
        @Override
        public void initChannel(final Channel ch) {
            final ChannelPipeline pipeline = ch.pipeline();		//获取 pipeline
            ChannelHandler handler = config.handler();			//这里获取的 handler，对应的是 AbstractBootstrap 的 handler，这个是通过 ServerBootstrap.handler() 方法设置的
            if (handler != null) {
                pipeline.addLast(handler);	//添加进入 pipeline，这个是为了让每个处理的都能首先调用这个 handler
            }
            //执行任务，设置子 handler。这里对用的是 ServerBootstrap.childHandler() 方法设置的 handler
            ch.eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    pipeline.addLast(new ServerBootstrapAcceptor(
                            ch, currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
                }
            });
        }
    });
}
```
&emsp; 然后到了 doBind0() 方法。

```java
private static void doBind0(
        final ChannelFuture regFuture, final Channel channel,
        final SocketAddress localAddress, final ChannelPromise promise) {
    //执行到这里，说明任务已经被注册到 loopgroup
    //所以可以开始一个监听端口的任务
    channel.eventLoop().execute(new Runnable() {
        @Override
        public void run() {
            if (regFuture.isSuccess()) {
                channel.bind(localAddress, promise).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                promise.setFailure(regFuture.cause());
            }
        }
    });
}
```

&emsp; 上述代码是不断地添加 handler 进入 pipleline，所以可以来看看 NioServerSocketChannel 的 ChannelPipiline 的组成。  

```text
			----> in stream
Handler handler -> ServerBootstrapAccept -> Tail Handler
			<---- out stream
```

&emsp; 到此，Netty 服务器监听的相关资源初始化已经完毕了。但是上面我只是粗略地讲了简单的步骤，还有一步比较重要的需要细讲一下，那就是 -- 注册 NioServerSocketChannel 到 Reactor 线程上的多路复用器上。  
&emsp; 注册的代码在 initAndRegister() 方法里面的代码

```java
ChannelFuture regFuture = config().group().register(channel);
```
&emsp; config().group() 指的是 ServerBootstrap 的 parentLoopGroup，而 register() 其实是 parentLoopGroup 的父类 MultithreadEventLoopGroup 的 register()。  

```java
public EventLoop next() {
    return (EventLoop) super.next();
}
```
&emsp; 这里的 next() 方法调用的是其父类 MultithreadEventExecutorGroup 的 next()

```java
    public EventExecutor next() {
        return chooser.next();
    }
```
&emsp; 这里的 chooser 是 MultithreadEventExecutorGroup 的成员属性，它可以对根据目前 ExectuorGroup 中的 EventExecutor 的情况策略选择 EventExecutor。这里默认使用的是 DefaultEventExecutorChooserFactory，这个是基于轮询策略操作的。它里面有两个内部类，它们的区别在于轮询的方式不相同。例如 9 个 EventExecutor，第一个请求给第一个 EventExecutor，第二个请求给第二个 EventExecutor...直到第九个请求给第九个 EventExecutor，到了第十个请求，又从头再来，给第一个 EventExecutor。  

|名字|	说明|
|---|---|
|PowerOfTwoEventExecutorChooser	|按位与(&)操作符|
|GenericEventExecutorChooser	|取模(%)运算符|

&emsp; chooserFactory 最后会选择出EventExecutor后，就可以将Channel进行注册了。在Netty的NioEventLoopGroup 中EventExecutor都是SingleThreadEventLoop来承担的(如果继续跟进代码的话，会发现其实 EventExecutor 实际上就是一个 Java 原生的线程池，最后实现的是一个ExecutorService)。  

&emsp; 接下来，获取到了 EventExecutor 后，就可以让它帮忙注册了。  

```java
public final void register(EventLoop eventLoop, final ChannelPromise promise) {
    
    //...省略非必要代码
    AbstractChannel.this.eventLoop = eventLoop;
    //判断是否是自身发起的操作；
    //如果是，则不存在并发操作，直接注册
    //如果不是，封装成一个 Task 放入消息对列异步执行
    if (eventLoop.inEventLoop()) {
        register0(promise);
    } else {
        try {
            eventLoop.execute(new Runnable() {
                @Override
                public void run() {
                    register0(promise);
                }
            });
        } catch (Throwable t) {
            //...省略非必要代码
        }
    }
}
```
&emsp; 发现实际注册的是 register0()，继续跟进

```java
private void register0(ChannelPromise promise) {
    try {
        //检查是否在注册的过程中被取消了，同时确保 channel 是处于开启状态
        if (!promise.setUncancellable() || !ensureOpen(promise)) {
            return;
        }
        boolean firstRegistration = neverRegistered;
        doRegister();	//进行注册
        neverRegistered = false;
        registered = true;

        //在被注册前首先调用 channel 的 handlerAdded 方法，这个算是生命周期方法吧
        pipeline.invokeHandlerAddedIfNeeded();

        safeSetSuccess(promise);
        pipeline.fireChannelRegistered();

        //channel只有在注册的时候才会调用一次 channelActive，后面都不会调用了。同时也是防止"取消注册"或"重新注册"的事件会反复调用 channelActive 
        if (isActive()) {
            if (firstRegistration) {
                pipeline.fireChannelActive();
            } else if (config().isAutoRead()) {
                //这里需要注意，如果之前 channel 被注册了而且设置了 autoRead 这意味着我们需要开始读取以便我们处理入站数据。
                beginRead();
            }
        }
    } catch (Throwable t) {
        // 关掉 channel 避免 FD 泄漏
        closeForcibly();
        closeFuture.setClosed();
        safeSetFailure(promise, t);
    }
}
```
&emsp; 发现虽然上面的方法写着 register，但实际上只是调用了一下 Netty 定义的生命周期函数。实际将 Channel 挂到 Selector 的代码在 doRegister() 方法里面。

```java
protected void doRegister() throws Exception {
    boolean selected = false;
    for (;;) {
        try {
            selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);
            return;
        } catch (CancelledKeyException e) {
            //...省略
        }
    }
}
```
&emsp; 上面代码可能有让人疑惑的地方。为什么注册 OP_ACCEPT(16) 到多路复用器上，怎么注册 0 呢？0 表示已注册，但不进行任何操作。这样做的原因是

    注册方法是多态的。它既可以被 NioServerSocketChannel 用来监听客户端的连接接入，也可以注册 SocketChannel 用来监听网络读或写操作。
    通过 SelectionKey 的 interceptOps(int pos) 可以方便修改监听的操作位。所以，此处注册需要获取 SelectionKey 并给 AbstractNioChannel 的成员变量 selectionKey 赋值。

&emsp; 当注册成功后，触发了 ChannelRegistered 事件，这个事件也是整个 pipeline 都会触发的。  

&emsp; ChannelRegistered 触发完后，就会判断是否 ServerSocketChannel 监听是否成功，如果成功，需要出发 NioServerSocketChannel 的 ChannelActive 事件。  

```java
if(isAcitve()) {
    pipeline.fireChannelActive();
}
```
&emsp; isAcitve() 方法也是多态。如果服务端判断是否监听启动；如果是客户端查看 TCP 是否连接完成。channelActive() 事件在 ChannelPipeline 中传递，完成之后根据配置决定是否自动出发 Channel 读操作，下面是代码实现  

```java
public ChannlePipeline fireChannelActive() {
    head.fireChannelActive();
    if(channel.config().isAutoRead()) {
        channel.read();
    }
    
    return this;
}
```
&emsp; AbstractChannel 的读操作出发了 ChannelPipeline 的读操作，最终调用到 HeadHandler 的读方法，代码如下  

```java
public void read(ChannelHandlerContext ctx){
unsafe.beginRead();
}
```
&emsp; 继续看 AbstractUnsafe 的 beginRead 方法，代码如下  

```java
public void beginRead() {
    if(!isAcitve()) {
        return;
    }
    
    try {
        doBeginRead();
    }
    
    //...省略代码
}
```
&emsp; 由于不同类型的 Channel 对于读操作的处理是不同的，所以合格 beginRead 也算是多态方法。对于 NIO 的 channel，无论是客户端还是服务端，都是修改网络监听操作位为自身感兴趣的事件。  

```java
protected void doBeginRead() throws Exception {
    // Channel.read() or ChannelHandlerContext.read() was called
    final SelectionKey selectionKey = this.selectionKey;
    if (!selectionKey.isValid()) {
        return;
    }

    readPending = true;

    final int interestOps = selectionKey.interestOps();
    if ((interestOps & readInterestOp) == 0) {
        selectionKey.interestOps(interestOps | readInterestOp);
    }
}
```
&emsp; JDK SelectionKey 有四种操作类型，分别为：  

```text
    OP_READ = 1<<0
    OP_WRITE = 1<<2
    OP_CONNECT = 1<<3
    OP_ACCEPT = 1<<4
```
&emsp; 每个操作位代表一种网络操作类型，分别为 0001，0010，0100，1000，这样做的好处是方便地通过位操作来进行网络操作位的状态判断和状态修改，从而提升操作性能。  


## 1.4. 客户端接入源码分析  
&emsp; 负责处理网络读写，连接和客户端情感求接入的Reactor线程是NioEventLoop，分析一下客户端是怎么接入的。当多路复用器检测到准备就绪的channel，默认执行processSelectedKeysOptimized，代码如下  

```java
private void processSelectedKeys() {
    if (selectedKeys != null) {
        processSelectedKeysOptimized();
    } else {
        processSelectedKeysPlain(selector.selectedKeys());
    }
}
```
&emsp; 由于 selectedKeys 不为空，所以执行 processSelectedKeysOptimized 方法。然后再看方法代码  

```java
if (a instanceof AbstractNioChannel) {
    (k, (AbstractNioChannel) a);
} else {
    @SuppressWarnings("unchecked")
    NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
    processSelectedKey(k, task);
}
```
&emsp; 由于 NioEventLoop 属于 AbstractNioChannel，所以执行 processSelectedKey 方法。processSelectedKey 顾名思义，就是处理所选择 selectionKey。看方法核心代码  

```java
int readyOps = k.readyOps();
if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
    // 删除OP_CONNECT，否则Selector.select(..)将始终不阻塞返回
    int ops = k.interestOps();
    ops &= ~SelectionKey.OP_CONNECT;
    k.interestOps(ops);

    unsafe.finishConnect();
}

if ((readyOps & SelectionKey.OP_WRITE) != 0) {
    // 调用forceFlush，它还会在没有东西可写时清除OP_WRITE
    ch.unsafe().forceFlush();
}

// 还要检查readOps是否为0，以解决JDK中可能导致的错误到一个旋转循环
if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
    unsafe.read();
}
```
&emsp; 已经完整看到了在 NioEventLoop 如何处理四种事件。来看看读事件。可以发现读事件是使用 unsafe 来实现的。unsafe 有两种实现，分别为 NioByteUnsafe 和 NioMessageUnsafe。由于是 NioEventLoop，所以使用 NioByteUnsafe。现在来看看它的 read() 方法。  

```java
public void read() {
    assert eventLoop().inEventLoop();
    final ChannelConfig config = config();
    final ChannelPipeline pipeline = pipeline();
    final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
    allocHandle.reset(config);

    boolean closed = false;
    Throwable exception = null;
    try {
        try {
            //第一部分，读取 SocketChannel
            do {
                int localRead = doReadMessages(readBuf);
                if (localRead == 0) {
                    break;
                }
                if (localRead < 0) {
                    closed = true;
                    break;
                }

                allocHandle.incMessagesRead(localRead);
            } while (allocHandle.continueReading());
        } catch (Throwable t) {
            exception = t;
        }
        //第二部分，开始触发 fireChannelRead 方法
        int size = readBuf.size();
        for (int i = 0; i < size; i ++) {
            readPending = false;
            pipeline.fireChannelRead(readBuf.get(i));
        }
        readBuf.clear();
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();

        if (exception != null) {
            closed = closeOnReadError(exception);

            pipeline.fireExceptionCaught(exception);
        }

        if (closed) {
            inputShutdown = true;
            if (isOpen()) {
                close(voidPromise());
            }
        }
    } finally {
        //
    }
}
```
&emsp; 上面代码拆成两部分走，第一部分是负责监听的 ServerSocketChannel 获取对应的 SocketChannel，第二部分是执行 headChannelHandlerContext 的 fireChannelRead 方法。  

&emsp; 先来看第一部分的代码，这部分主要的代码是 doReadMessages 方法  

```java
protected int doReadMessages(List<Object> buf) throws Exception {
    //获取到外部的客户端 SocketChannel
    SocketChannel ch = SocketUtils.accept(javaChannel());
    try {
        if (ch != null) {
            //封装成为 Netty 的 NioSocketChannel
            buf.add(new NioSocketChannel(this, ch));
            return 1;
        }
    } 
    //... 省略代码
    return 0;
}
```
&emsp; 然后第二部分的代码是主要是 pipeline.fireChannelRead()。当调用后，执行的是属于 ServerBootstrapAcceptor 的 ChannelHandlerContext，事件在 ChannelPipeline 中传递，代码如下：  

```java
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    Channel child = (Channel) msg;
    child.pipeline().addLast();
    //代码省略
    child.unsafe().register(child.newPromise());
}
```
&emsp; 该方法主要有三个步骤：  

    将启动时传入的 childHandler 加入到客户端 SocketChannel 的 ChannelPipeline
    设置客户端 SocketChannel 的 TCP 参数
    注册 SocketChannel 到多路复用器上

&emsp; 以上三个步骤执行完之后，看一下 NioServerSocketChannel 的 register 方法。  
&emsp; NioServerSocketChannel 注册方法与 ServerSocketChannel 的一致，也是 Channel 注册到 Reactor线程的多路复用器上。由于注册的操作位是 0，所以，此时的 NioserverSocketChannel 还不能读取客户端发送的消息。  
&emsp; 执行完注册操作之后，紧接着会触发 ChannelReadComplete 事件。继续分析 ChannelReadComplete 在 ChannelPipeline 中的处理流程：Netty 的 Header 和 Tail 本身不关注 ChannelReadComplete 事件直接透传，执行完 ChannelReadComplete 后，接着执行 PipeLine 的 read() 方法，最终执行 HeadHandler() 的 read() 方法。  
&emsp; HeadHandler read() 方法是用来将网络操作位修改读操作。创建 NioSocketChannel 的时候已经将 AbstractNioChannel 的 readInterestOp 设置为 OP_READ。这样，执行 selectionKey.interestOps(interestOps | readInterestOp) 操作时就会把操作位设置为 OP_READ。  

```java
protected AbstractNioByteChannel(Channel parent, EventLoop eventLoop, SelectableChannel ch) {
	super(parent, eventLoop, ch, SelectionKey.OP_READ);
}
```
&emsp; 对此，新接入的客户端连接处理完成，可以进行网络读写等 IO 操作。






