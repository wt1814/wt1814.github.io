
<!-- TOC -->

- [1. NioEventLoop的启动](#1-nioeventloop的启动)
    - [1.1. 服务端启动](#11-服务端启动)
    - [1.2. 新连接接入](#12-新连接接入)

<!-- /TOC -->


# 1. NioEventLoop的启动  
&emsp; Netty中会在服务端启动和新连接接入时通过chooser选择器，分别为NioServerSocketChannel与NioSocketChannel选择绑定一个NioEventLoop，接下来我们就分别从这两个方面梳理NioEventLoop的启动源码。  

## 1.1. 服务端启动
&emsp; 首先我们结合下图看下Netty服务启动过程中，NioServerSocketChannel绑定的NioEventLoop启动流程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-123.png)  
&emsp; bind()部分源码我们在之前服务端启动过程中进行过说明，我们进一步跟踪进入doBind0()方法中可以看到channel.eventLoop().execute的执行，需要说明的是这里其实启动的NioServerSocketChannel绑定的 bossGroup，用来负责处理新连接接入的。  

```java
/**
* read by jsf
* 
* @param regFuture
* @param channel
* @param localAddress
* @param promise
*/
private static void doBind0(final ChannelFuture regFuture, final Channel channel, final SocketAddress localAddress,
        final ChannelPromise promise) {
    //该方法向 NioServerSocketChannel 的 eventLoop 提交了一个任务，当 future(其实就是 promise) 成功后执行
    //NioServerSocketChannel 的 bind 方法，并添加一个关闭监听器。我们主要关注 bind 方法。
    // This method is invoked before channelRegistered() is triggered. Give user
    // handlers a chance to set up
    // the pipeline in its channelRegistered() implementation.
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
&emsp; 进入NioEventLoop父类SingleThreadEventExecutor中的execute方法，改方法通过inEventLoop()会首先判断当前的线程是否是NioEventLoop本身绑定的线程，结合inEventLoop的代码可以看到NioEventLoop本身线程还未初始化为空，这里返回false，执行启动线程操作，同时会任务放入任务队列中。  


```java
@Override
public void execute(Runnable task) {
    if (task == null) {
        throw new NullPointerException("task");
    }

    //首先判断当前线程是否是该EventLoop绑定的线程
    boolean inEventLoop = inEventLoop();
    //把传入的任务加入任务对立
    addTask(task);
    if (!inEventLoop) {//如果不是同一条线程
        startThread();
        if (isShutdown() && removeTask(task)) {
            reject();
        }
    }

    if (!addTaskWakesUp && wakesUpForTask(task)) {
        wakeup(inEventLoop);
    }
}
```

```java
@Override
public boolean inEventLoop(Thread thread) {
    return thread == this.thread;
}
```


&emsp; 继续跟踪进入startThread()方法中  

```java
private void startThread() {
    if (state == ST_NOT_STARTED) {
        if (STATE_UPDATER.compareAndSet(this, ST_NOT_STARTED, ST_STARTED)) {
            try {
                doStartThread();
            } catch (Throwable cause) {
                STATE_UPDATER.set(this, ST_NOT_STARTED);
                PlatformDependent.throwException(cause);
            }
        }
    }
}
```

&emsp; 在 doStartThread()中主要实现了以下功能：  
&emsp; 1、执行传入的ThreadPerTaskExecutor的execute方法，创建一个新的线程，并与这个NioEventLoop对象绑定；  
&emsp; 2、在开启的线程中执行SingleThreadEventExecutor.this.run()，也就是NioEventLoop的run方法，开始NioEventLoop的执行操作；  


```java
private void doStartThread() {
    assert thread == null;
    //线程执行器通过线程工厂创建线程
    executor.execute(new Runnable() {
        @Override
        public void run() {
            //开启线程，并赋值
            thread = Thread.currentThread();
            if (interrupted) {
                thread.interrupt();
            }

            boolean success = false;
            updateLastExecutionTime();
            try {
                //执行NioEventLoop的run方法
                SingleThreadEventExecutor.this.run();
                success = true;
            } catch (Throwable t) {
                logger.warn("Unexpected exception from an event executor: ", t);
            } finally {
                for (;;) {
                    int oldState = state;
                    if (oldState >= ST_SHUTTING_DOWN || STATE_UPDATER.compareAndSet(
                            SingleThreadEventExecutor.this, oldState, ST_SHUTTING_DOWN)) {
                        break;
                    }
                }

                // Check if confirmShutdown() was called at the end of the loop.
                if (success && gracefulShutdownStartTime == 0) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Buggy " + EventExecutor.class.getSimpleName() + " implementation; " +
                                SingleThreadEventExecutor.class.getSimpleName() + ".confirmShutdown() must " +
                                "be called before run() implementation terminates.");
                    }
                }

                try {
                    // Run all remaining tasks and shutdown hooks.
                    for (;;) {
                        if (confirmShutdown()) {
                            break;
                        }
                    }
                } finally {
                    try {
                        cleanup();
                    } finally {
                        STATE_UPDATER.set(SingleThreadEventExecutor.this, ST_TERMINATED);
                        threadLock.release();
                        if (!taskQueue.isEmpty()) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("An event executor terminated with " +
                                        "non-empty task queue (" + taskQueue.size() + ')');
                            }
                        }

                        terminationFuture.setSuccess(null);
                    }
                }
            }
        }
    });
}
```

&emsp; OK到这一步，基于服务端启动绑定端口的NioServerSocketChannel，也就是服务端Channel绑定的NioEventLoop已经启动。

## 1.2. 新连接接入
&emsp; 首先我们结合下图看下当有客户端接入时，创建NioSocketChannel，然后绑定NioEventLoop并启动的流程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-124.png)  
&emsp; 服务端启动时会在NioServerSocketChannel的任务链中添加ServerBootstrapAcceptor对象，这就是用来处理新新连接接入的  

```java
p.addLast(new ChannelInitializer<Channel>() {
    @Override
    public void initChannel(final Channel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        ChannelHandler handler = config.handler();
        if (handler != null) {
            pipeline.addLast(handler);
        }

        // 服务端NioServerSocketChannel的pipeline中添加ServerBootstrapAcceptor
        ch.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                pipeline.addLast(new ServerBootstrapAcceptor(
                        ch, currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
            }
        });
    }
});
```

&emsp; 在新连接接入事件触发时，执行unsafe.read();  

```java
private void processSelectedKey(SelectionKey k, AbstractNioChannel ch) {
    final AbstractNioChannel.NioUnsafe unsafe = ch.unsafe();
    if (!k.isValid()) {
        final EventLoop eventLoop;
        try {
            eventLoop = ch.eventLoop();
        } catch (Throwable ignored) {
            // If the channel implementation throws an exception because there is no event loop, we ignore this
            // because we are only trying to determine if ch is registered to this event loop and thus has authority
            // to close ch.
            return;
        }
        // Only close ch if ch is still registered to this EventLoop. ch could have deregistered from the event loop
        // and thus the SelectionKey could be cancelled as part of the deregistration process, but the channel is
        // still healthy and should not be closed.
        // See https://github.com/netty/netty/issues/5125
        if (eventLoop != this || eventLoop == null) {
            return;
        }
        // close the channel if the key is not valid anymore
        unsafe.close(unsafe.voidPromise());
        return;
    }

    try {
        int readyOps = k.readyOps();
        // We first need to call finishConnect() before try to trigger a read(...) or write(...) as otherwise
        // the NIO JDK channel implementation may throw a NotYetConnectedException.
        if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
            // remove OP_CONNECT as otherwise Selector.select(..) will always return without blocking
            // See https://github.com/netty/netty/issues/924
            int ops = k.interestOps();
            ops &= ~SelectionKey.OP_CONNECT;
            k.interestOps(ops);

            unsafe.finishConnect();
        }

        // Process OP_WRITE first as we may be able to write some queued buffers and so free memory.
        if ((readyOps & SelectionKey.OP_WRITE) != 0) {
            // Call forceFlush which will also take care of clear the OP_WRITE once there is nothing left to write
            ch.unsafe().forceFlush();
        }

        // Also check for readOps of 0 to workaround possible JDK bug which may otherwise lead
        // to a spin loop
        //新连接接入
        if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
            unsafe.read();
        }
    } catch (CancelledKeyException ignored) {
        unsafe.close(unsafe.voidPromise());
    }
}
```

&emsp; unsafe.read()的具体实现为NioMessageUnsafe中的read()，在read()方法中主要实现了两个功能：  

&emsp; 1、创建客户端Channel，也就是NioSocketChannel；  
&emsp; 2、开始服务端NioServerSocketChannel的任务链传递，首先执行之前已经加入任务链的ServerBootstrapAcceptor中的channelRead  

```java
@Override
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
            do {
                //这里创建客户端连接,也就是NioSocketChannelChannel
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

        int size = readBuf.size();
        for (int i = 0; i < size; i ++) {
            readPending = false;
            //在这里开始NioServerSocketChannel的任务链传递，会首先执行ServerBootstrapAcceptor中的channelRead
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
        // Check if there is a readPending which was not processed yet.
        // This could be for two reasons:
        // * The user called Channel.read() or ChannelHandlerContext.read() in channelRead(...) method
        // * The user called Channel.read() or ChannelHandlerContext.read() in channelReadComplete(...) method
        //
        // See https://github.com/netty/netty/issues/2254
        if (!readPending && !config.isAutoRead()) {
            removeReadOp();
        }
    }
}
```

&emsp; 接下来在ServerBootstrapAcceptor中的channelRead中会获取到传入的NioSocketChannel，针对NioSocketChannel主要会执行以下操作：  

&emsp; 1、配置childHandler任务链；  
&emsp; 2、配置childOptions；  
&emsp; 3、为NioSocketChannel分配NioEventLoop  

```java
@Override
@SuppressWarnings("unchecked")
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    final Channel child = (Channel) msg;

    //配置childHandler任务链
    child.pipeline().addLast(childHandler);


    //配置childOptions
    setChannelOptions(child, childOptions, logger);

    for (Entry<AttributeKey<?>, Object> e: childAttrs) {
        child.attr((AttributeKey<Object>) e.getKey()).set(e.getValue());
    }

    try {
        //为新连接分配NioEventLoop，并启动执行
        childGroup.register(child).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    forceClose(child, future.cause());
                }
            }
        });
    } catch (Throwable t) {
        forceClose(child, t);
    }
}
```

&emsp; 看以看到EventLoopGroup中register具体实实现：  

&emsp; 1、关于next()，我们之前讲过是专门用来分配NioEventLoop；  
&emsp; 2、register()主要负责了EventLoop的绑定和启动；  

```java
@Override
public ChannelFuture register(ChannelPromise promise) {
    return next().register(promise);
}
```

```java
@Override
public final void register(EventLoop eventLoop, final ChannelPromise promise) {
    if (eventLoop == null) {
        throw new NullPointerException("eventLoop");
    }
    if (isRegistered()) {
        promise.setFailure(new IllegalStateException("registered to an event loop already"));
        return;
    }
    if (!isCompatible(eventLoop)) {
        promise.setFailure(
                new IllegalStateException("incompatible event loop type: " + eventLoop.getClass().getName()));
        return;
    }

    //与NioEventLoop绑定
    AbstractChannel.this.eventLoop = eventLoop;

    //首先判断线程是否一致,当前线程是NioServerSocketChannel的线程,与当前创建NioSocketChannel的eventLoop线程不一致
    if (eventLoop.inEventLoop()) {
        register0(promise);
    } else {
        try {
            //在这里NioEventLoop启动
            eventLoop.execute(new Runnable() {
                @Override
                public void run() {
                    register0(promise);
                }
            });
        } catch (Throwable t) {
            logger.warn(
                    "Force-closing a channel whose registration task was not accepted by an event loop: {}",
                    AbstractChannel.this, t);
            closeForcibly();
            closeFuture.setClosed();
            safeSetFailure(promise, t);
        }
    }
}
```

&emsp; 上面代码中的 eventLoop.execute我们已经分析过，经过一系列的流程，最后会执行NioEventLoop的run方法开始轮询感兴趣的IO事件。  

&emsp; 以上我们主要从服务启动与客户端连接两个方面分析了NioEventLoop的启动流程与源码，其实也就对应NioServerSocketChannel与NioSocketChannel分别绑定的NioEventLoop，其中有错误和不足之处还请指正与海涵。  