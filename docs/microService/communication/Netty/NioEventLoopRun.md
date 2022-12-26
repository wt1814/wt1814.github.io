
<!-- TOC -->

- [1. ~~NioEventLoop的执行，解决NIO的bug~~](#1-nioeventloop的执行解决nio的bug)
    - [1.1. NioEventLoop的执](#11-nioeventloop的执)
        - [1.1.1. 检测IO事件](#111-检测io事件)
        - [1.1.2. 处理IO事件](#112-处理io事件)
        - [1.1.3. 线程任务的执行](#113-线程任务的执行)
    - [1.2. Netty解决NIO的bug](#12-netty解决nio的bug)
        - [1.2.1. NIO的空轮询bug](#121-nio的空轮询bug)
        - [1.2.2. netty如何解决NIO空轮询bug的？](#122-netty如何解决nio空轮询bug的)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. netty如何解决NIO空轮询bug的？  
&emsp; **selectCnt在正常逻辑时，会被重新赋值为1，在出现空轮询bug时会累加，直到大于阈值512，则触发重构selector操作。从这里可以看到netty并没有真正解决NIO的epoll模型的bug，而是采用替换selector的操作巧妙的避开了空轮询bug！**  
&emsp; **把旧的Selector中已注册的SelectionKey，全部挪到新的 Selector中去。**  


# 1. ~~NioEventLoop的执行，解决NIO的bug~~  
<!-- 
https://www.cnblogs.com/dafanjoy/p/10507662.html

https://blog.csdn.net/qq_43049310/article/details/113688981
-->


## 1.1. NioEventLoop的执
&emsp; NioEventLoop的执行都是在run()方法的for循环中完成的。  

```java
@Override
protected void run() {
    //循环处理IO事件和task任务
    for (;;) {
        try {
            try {
                switch (selectStrategy.calculateStrategy(selectNowSupplier, hasTasks())) {
                case SelectStrategy.CONTINUE:
                    continue;

                case SelectStrategy.BUSY_WAIT:
                    // fall-through to SELECT since the busy-wait is not supported with NIO

                case SelectStrategy.SELECT:
                    //通过cas操作标识select方法的唤醒状态，执行select操作
                    select(wakenUp.getAndSet(false));

                    // 'wakenUp.compareAndSet(false, true)' is always evaluated
                    // before calling 'selector.wakeup()' to reduce the wake-up
                    // overhead. (Selector.wakeup() is an expensive operation.)
                    //
                    // However, there is a race condition in this approach.
                    // The race condition is triggered when 'wakenUp' is set to
                    // true too early.
                    //
                    // 'wakenUp' is set to true too early if:
                    // 1) Selector is waken up between 'wakenUp.set(false)' and
                    //    'selector.select(...)'. (BAD)
                    // 2) Selector is waken up between 'selector.select(...)' and
                    //    'if (wakenUp.get()) { ... }'. (OK)
                    //
                    // In the first case, 'wakenUp' is set to true and the
                    // following 'selector.select(...)' will wake up immediately.
                    // Until 'wakenUp' is set to false again in the next round,
                    // 'wakenUp.compareAndSet(false, true)' will fail, and therefore
                    // any attempt to wake up the Selector will fail, too, causing
                    // the following 'selector.select(...)' call to block
                    // unnecessarily.
                    //
                    // To fix this problem, we wake up the selector again if wakenUp
                    // is true immediately after selector.select(...).
                    // It is inefficient in that it wakes up the selector for both
                    // the first case (BAD - wake-up required) and the second case
                    // (OK - no wake-up required).

                    if (wakenUp.get()) {
                        selector.wakeup();
                    }
                    // fall through
                default:
                }
            } catch (IOException e) {
                // If we receive an IOException here its because the Selector is messed up. Let's rebuild
                // the selector and retry. https://github.com/netty/netty/issues/8566
                rebuildSelector0();
                handleLoopException(e);
                continue;
            }

            cancelledKeys = 0;
            needsToSelectAgain = false;
            // 这个比例是处理IO事件所需的时间和花费在处理task时间的比例
            final int ioRatio = this.ioRatio;
            if (ioRatio == 100) {
                try {
                    processSelectedKeys();
                } finally {
                    // Ensure we always run tasks.
                    runAllTasks();
                }
            } else {
                //IO处理的开始时间
                final long ioStartTime = System.nanoTime();
                try {
                    //处理IO事件的函数
                    processSelectedKeys();
                } finally {
                    // Ensure we always run tasks.
                    // 当前时间减去处理IO事件开始的时间就是处理IO事件花费的时间
                    final long ioTime = System.nanoTime() - ioStartTime;
                    //执行任务方法
                    runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
                }
            }
        } catch (Throwable t) {
            handleLoopException(t);
        }
        // Always handle shutdown even if the loop processing threw an exception.
        try {
            //判断线程池是否shutdown，关闭的话释放所有资源
            if (isShuttingDown()) {
                closeAll();
                if (confirmShutdown()) {
                    return;
                }
            }
        } catch (Throwable t) {
            handleLoopException(t);
        }
    }
}
```

&emsp; 通过上面的代码我们把NioEventLoop中run方法主要归纳为以下三个功能：  

&emsp; 1、通过select()检测IO事件；  
&emsp; 2、通过processSelectedKeys()处理IO事件；  
&emsp; 3、runAllTasks()处理线程任务队列；  

![image](http://182.92.69.8:8081/img/microService/netty/netty-126.png)  

&emsp; 接下来我们就从这三个方面入手，对NioEventLoop的具体执行代码进行解析。  

### 1.1.1. 检测IO事件
&emsp; IO事件的监测，主要通过 select(wakenUp.getAndSet(false) 方法实现，具体的代码分析如下：  

```java
private void select(boolean oldWakenUp) throws IOException {
    Selector selector = this.selector;
    try {
        int selectCnt = 0;
        long currentTimeNanos = System.nanoTime();
        //定义select操作的截止时间
        long selectDeadLineNanos = currentTimeNanos + delayNanos(currentTimeNanos);

        for (;;) {
            //计算当前select操作是否超时
            long timeoutMillis = (selectDeadLineNanos - currentTimeNanos + 500000L) / 1000000L;
            if (timeoutMillis <= 0) {
                //如果已经超时，且没有执行过轮询操作，则执行selectNow()非阻塞操作，直接跳出循环
                if (selectCnt == 0) {
                    selector.selectNow();
                    selectCnt = 1;
                }
                break;
            }

            // If a task was submitted when wakenUp value was true, the task didn't get a chance to call
            // Selector#wakeup. So we need to check task queue again before executing select operation.
            // If we don't, the task might be pended until select operation was timed out.
            // It might be pended until idle timeout if IdleStateHandler existed in pipeline.
            if (hasTasks() && wakenUp.compareAndSet(false, true)) {
                //如果有需要任务队列，同样跳出循环
                selector.selectNow();
                selectCnt = 1;
                break;
            }

            //执行select阻塞操作，其阻塞时间为timeoutMillis
            int selectedKeys = selector.select(timeoutMillis);
            selectCnt ++;

            if (selectedKeys != 0 || oldWakenUp || wakenUp.get() || hasTasks() || hasScheduledTasks()) {
                // - Selected something,
                // - waken up by user, or
                // - the task queue has a pending task.
                // - a scheduled task is ready for processing
                //1、如果轮询到select事件 2、wekup为true,表示轮询线程已经被唤醒 3、任务队列不为空 4、定时任务对队列有任务
                //上述条件只要满足一个就跳出select循环
                break;
            }
            if (Thread.interrupted()) {
                // Thread was interrupted so reset selected keys and break so we not run into a busy loop.
                // As this is most likely a bug in the handler of the user or it's client library we will
                // also log it.
                //
                // See https://github.com/netty/netty/issues/2426
                if (logger.isDebugEnabled()) {
                    logger.debug("Selector.select() returned prematurely because " +
                            "Thread.currentThread().interrupt() was called. Use " +
                            "NioEventLoop.shutdownGracefully() to shutdown the NioEventLoop.");
                }
                //线程被中断，同样跳出select循环
                selectCnt = 1;
                break;
            }


            long time = System.nanoTime();//记录当前时间
            //如果 当前时间-超时时间>起始时间 也就是 当前时间-起始时间>超时时间
            if (time - TimeUnit.MILLISECONDS.toNanos(timeoutMillis) >= currentTimeNanos) {
                // timeoutMillis elapsed without anything selected.
                //满足条件，则是一次正常的select操作，否则就是一次空轮询操作
                selectCnt = 1;
            } else if (SELECTOR_AUTO_REBUILD_THRESHOLD > 0 &&
                    selectCnt >= SELECTOR_AUTO_REBUILD_THRESHOLD) {
                // The code exists in an extra method to ensure the method is not too big to inline as this
                // branch is not very likely to get hit very frequently.
                //如果轮询次数大于SELECTOR_AUTO_REBUILD_THRESHOLD,则对当前selector进行处理
                //执行selectRebuildSelector操作，把当前selector的selectedKeys注册到一个新的selector上
                selector = selectRebuildSelector(selectCnt);
                selectCnt = 1;
                break;
            }

            currentTimeNanos = time;
        }

        if (selectCnt > MIN_PREMATURE_SELECTOR_RETURNS) {
            if (logger.isDebugEnabled()) {
                logger.debug("Selector.select() returned prematurely {} times in a row for Selector {}.",
                        selectCnt - 1, selector);
            }
        }
    } catch (CancelledKeyException e) {
        if (logger.isDebugEnabled()) {
            logger.debug(CancelledKeyException.class.getSimpleName() + " raised by a Selector {} - JDK bug?",
                    selector, e);
        }
        // Harmless exception - log anyway
    }
}
```

&emsp; Netty通过创建一个新的selector，且把原有selector上的SelectionKey同步到新的selector上的方式，解决了selector空轮询的bug，我们看下具体的代码实现  

```java
private Selector selectRebuildSelector(int selectCnt) throws IOException {
    // The selector returned prematurely many times in a row.
    // Rebuild the selector to work around the problem.
    logger.warn(
            "Selector.select() returned prematurely {} times in a row; rebuilding Selector {}.",
            selectCnt, selector);

    //重新创建一个Selector
    rebuildSelector();
    Selector selector = this.selector;

    // Select again to populate selectedKeys.
    selector.selectNow();
    return selector;
}
```

```java
public void rebuildSelector() {
    if (!inEventLoop()) {
        //如果当前线程和NioEventLoop绑定的线程不一致
        execute(new Runnable() {
            @Override
            public void run() {
                rebuildSelector0();
            }
        });
        return;
    }
    //具体实现
    rebuildSelector0();
}
```

```java
private void rebuildSelector0() {
    final Selector oldSelector = selector;
    final SelectorTuple newSelectorTuple;

    if (oldSelector == null) {
        return;
    }

    try {
        //创建一个新的selector
        newSelectorTuple = openSelector();
    } catch (Exception e) {
        logger.warn("Failed to create a new Selector.", e);
        return;
    }

    // Register all channels to the new Selector.
    int nChannels = 0;
    for (SelectionKey key: oldSelector.keys()) { //遍历oldSelector上所有的SelectionKey
        Object a = key.attachment();
        try {
            if (!key.isValid() || key.channel().keyFor(newSelectorTuple.unwrappedSelector) != null) {
                continue;
            }

            int interestOps = key.interestOps();
            key.cancel();//取消原有的SelectionKey上的事件
            //在channel上注册新的Selector
            SelectionKey newKey = key.channel().register(newSelectorTuple.unwrappedSelector, interestOps, a);
            if (a instanceof AbstractNioChannel) {
                // Update SelectionKey
                //把新的SelectionKey赋给AbstractNioChannel
                ((AbstractNioChannel) a).selectionKey = newKey;
            }
            nChannels ++;
        } catch (Exception e) {
            logger.warn("Failed to re-register a Channel to the new Selector.", e);
            if (a instanceof AbstractNioChannel) {
                AbstractNioChannel ch = (AbstractNioChannel) a;
                ch.unsafe().close(ch.unsafe().voidPromise());
            } else {
                @SuppressWarnings("unchecked")
                NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
                invokeChannelUnregistered(task, key, e);
            }
        }
    }
    //替换新的selector
    selector = newSelectorTuple.selector;
    unwrappedSelector = newSelectorTuple.unwrappedSelector;

    try {
        // time to close the old selector as everything else is registered to the new one
        oldSelector.close();
    } catch (Throwable t) {
        if (logger.isWarnEnabled()) {
            logger.warn("Failed to close the old Selector.", t);
        }
    }

    if (logger.isInfoEnabled()) {
        logger.info("Migrated " + nChannels + " channel(s) to the new Selector.");
    }
}
```

&emsp; 通过上面的代码可以看到，NioEventLoop的select方法主要实现三方面的功能  

&emsp; 1、结合超时时间、任务队列及select本身唤醒状态进行是否跳出for(;;)循环的逻辑判断；  

&emsp; 2、进行select阻塞操作，selector检测到事则跳出for(;;)循环；  

&emsp; 3、通过创建一个新的selector的方式解决selector空轮询的bug问题；  

### 1.1.2. 处理IO事件

&emsp; IO事件处理的核心方法是processSelectedKeys()，看下其代码的具体实现  

```java
private void processSelectedKeys() {
    //NioEventLoop的构造函数中通过openSelector()方法初始化selectedKeys，并赋给对应的selector
    if (selectedKeys != null) {
        //selectedKeys不为空
        processSelectedKeysOptimized();
    } else {
        processSelectedKeysPlain(selector.selectedKeys());
    }
}
```

&emsp; 如果selectedKeys不为空，也就是检测到注册的IO事件，则执行processSelectedKeysOptimized()方法  

```java
private void processSelectedKeysOptimized() {
    //遍历selectedKeys
    for (int i = 0; i < selectedKeys.size; ++i) {
        final SelectionKey k = selectedKeys.keys[i];
        // null out entry in the array to allow to have it GC'ed once the Channel close
        // See https://github.com/netty/netty/issues/2363
        selectedKeys.keys[i] = null;

        //拿到SelectionKey对应的channel
        final Object a = k.attachment();

        if (a instanceof AbstractNioChannel) {
            //如果是AbstractNioChannel的对象，执行processSelectedKey
            processSelectedKey(k, (AbstractNioChannel) a);
        } else {
            //否则转换为一个NioTask对象
            @SuppressWarnings("unchecked")
            NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
            processSelectedKey(k, task);
        }

        if (needsToSelectAgain) {
            // null out entries in the array to allow to have it GC'ed once the Channel close
            // See https://github.com/netty/netty/issues/2363
            selectedKeys.reset(i + 1);

            selectAgain();
            i = -1;
        }
    }
}
```

&emsp; 通过遍历selectedKeys，拿到所有触发IO事件的SelectionKey与其对应Channel，然后交给processSelectedKey()方法处理  

```java
private void processSelectedKey(SelectionKey k, AbstractNioChannel ch) {
    //拿到该AbstractNioChannel的unsafe对象
    final AbstractNioChannel.NioUnsafe unsafe = ch.unsafe();
    if (!k.isValid()) {
        final EventLoop eventLoop;
        try {
            //拿到绑定的eventLoop
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
        //针对检测到的IO事件进行处理
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
        //新连接接入事件
        if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
            unsafe.read();
        }
    } catch (CancelledKeyException ignored) {
        unsafe.close(unsafe.voidPromise());
    }
}
```

&emsp; 从上面的代码中可以看到NioEventLoop处理IO事件的流程中，会循环从SelectedSelectionKeySet中获取触发事件的SelectionKey，Netty在这里对JDK中NIO的Selector进行了优化，在NioEventLoop构造函数中通过openSelector()方法用自定义的SelectedSelectionKeySet替代Selector原有的selectedKeys与publicSelectedKeys。  

```java
private SelectorTuple openSelector() {
    final Selector unwrappedSelector;
    try {
        //创建一个Selector
        unwrappedSelector = provider.openSelector();
    } catch (IOException e) {
        throw new ChannelException("failed to open a new selector", e);
    }

    if (DISABLE_KEY_SET_OPTIMIZATION) {
        return new SelectorTuple(unwrappedSelector);
    }


    //通过反射的方式获取 sun.nio.ch.SelectorImpl 类
    Object maybeSelectorImplClass = AccessController.doPrivileged(new PrivilegedAction<Object>() {
        @Override
        public Object run() {
            try {

                return Class.forName(
                        "sun.nio.ch.SelectorImpl",
                        false,
                        PlatformDependent.getSystemClassLoader());
            } catch (Throwable cause) {
                return cause;
            }
        }
    });

    if (!(maybeSelectorImplClass instanceof Class) ||
        // ensure the current selector implementation is what we can instrument.
        !((Class<?>) maybeSelectorImplClass).isAssignableFrom(unwrappedSelector.getClass())) {
        if (maybeSelectorImplClass instanceof Throwable) {
            Throwable t = (Throwable) maybeSelectorImplClass;
            logger.trace("failed to instrument a special java.util.Set into: {}", unwrappedSelector, t);
        }
        return new SelectorTuple(unwrappedSelector);
    }

    final Class<?> selectorImplClass = (Class<?>) maybeSelectorImplClass;
    //初始化一个SelectedSelectionKeySet对象
    final SelectedSelectionKeySet selectedKeySet = new SelectedSelectionKeySet();

    Object maybeException = AccessController.doPrivileged(new PrivilegedAction<Object>() {
        @Override
        public Object run() {
            try {
                //获取"sun.nio.ch.SelectorImpl"类的selectedKeys属性
                Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
                //获取"sun.nio.ch.SelectorImpl"类的publicSelectedKeys属性
                Field publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");

                if (PlatformDependent.javaVersion() >= 9 && PlatformDependent.hasUnsafe()) {
                    // Let us try to use sun.misc.Unsafe to replace the SelectionKeySet.
                    // This allows us to also do this in Java9+ without any extra flags.
                    long selectedKeysFieldOffset = PlatformDependent.objectFieldOffset(selectedKeysField);
                    long publicSelectedKeysFieldOffset =
                            PlatformDependent.objectFieldOffset(publicSelectedKeysField);

                    if (selectedKeysFieldOffset != -1 && publicSelectedKeysFieldOffset != -1) {
                        //把selectedKeySet赋值给selectedKeys
                        PlatformDependent.putObject(
                                unwrappedSelector, selectedKeysFieldOffset, selectedKeySet);
                        //把selectedKeySet赋值给publicSelectedKeys
                        PlatformDependent.putObject(
                                unwrappedSelector, publicSelectedKeysFieldOffset, selectedKeySet);
                        return null;
                    }
                    // We could not retrieve the offset, lets try reflection as last-resort.
                }

                Throwable cause = ReflectionUtil.trySetAccessible(selectedKeysField, true);
                if (cause != null) {
                    return cause;
                }
                cause = ReflectionUtil.trySetAccessible(publicSelectedKeysField, true);
                if (cause != null) {
                    return cause;
                }

                selectedKeysField.set(unwrappedSelector, selectedKeySet);
                publicSelectedKeysField.set(unwrappedSelector, selectedKeySet);
                return null;
            } catch (NoSuchFieldException e) {
                return e;
            } catch (IllegalAccessException e) {
                return e;
            }
        }
    });

    if (maybeException instanceof Exception) {
        selectedKeys = null;
        Exception e = (Exception) maybeException;
        logger.trace("failed to instrument a special java.util.Set into: {}", unwrappedSelector, e);
        return new SelectorTuple(unwrappedSelector);
    }
    selectedKeys = selectedKeySet;
    logger.trace("instrumented a special java.util.Set into: {}", unwrappedSelector);
    return new SelectorTuple(unwrappedSelector,
                                new SelectedSelectionKeySetSelector(unwrappedSelector, selectedKeySet));
}
```

&emsp; SelectedSelectionKeySet 代码实现如下  

```java
final class SelectedSelectionKeySet extends AbstractSet<SelectionKey> {

    SelectionKey[] keys;
    int size;

    //构造函数中初始化一个1024长度的数组
    SelectedSelectionKeySet() {
        keys = new SelectionKey[1024];
    }

    @Override
    public boolean add(SelectionKey o) {
        if (o == null) {
            return false;
        }

        //向数组中添加元素
        keys[size++] = o;
        if (size == keys.length) {
            //进行扩容
            increaseCapacity();
        }

        return true;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<SelectionKey> iterator() {
        return new Iterator<SelectionKey>() {
            private int idx;

            @Override
            public boolean hasNext() {
                return idx < size;
            }

            @Override
            public SelectionKey next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return keys[idx++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    void reset() {
        reset(0);
    }

    void reset(int start) {
        Arrays.fill(keys, start, size, null);
        size = 0;
    }

    private void increaseCapacity() {
        SelectionKey[] newKeys = new SelectionKey[keys.length << 1];
        System.arraycopy(keys, 0, newKeys, 0, size);
        keys = newKeys;
    }
}
```

&emsp; 这里的优化点主要在于用底层为数组实现的SelectedSelectionKeySet 代替HashSet类型的selectedKeys与publicSelectedKeys，因为HashSet的add方法最大的时间复杂度可能为O(n)，而SelectedSelectionKeySet 主要就是用数组实现一个基本的add方法，时间复杂度为O(1)，在这一点上相比HashSet要简单很多。  

### 1.1.3. 线程任务的执行
&emsp; NioEventLoop中通过runAllTasks方法执行线程任务  

```java
protected boolean runAllTasks(long timeoutNanos) {
    //把需要执行的定时任务从scheduledTaskQueue转移到taskQueue
    fetchFromScheduledTaskQueue();
    Runnable task = pollTask();
    if (task == null) {
        afterRunningAllTasks();
        return false;
    }

    //计算截止时间
    final long deadline = ScheduledFutureTask.nanoTime() + timeoutNanos;
    long runTasks = 0;
    long lastExecutionTime;
    for (;;) {
        //执行task任务
        safeExecute(task);

        runTasks ++;

        // Check timeout every 64 tasks because nanoTime() is relatively expensive.
        // XXX: Hard-coded value - will make it configurable if it is really a problem.
        if ((runTasks & 0x3F) == 0) {
            //每执行64次任务，进行一次超时检查
            lastExecutionTime = ScheduledFutureTask.nanoTime();
            if (lastExecutionTime >= deadline) {
                //如果超出最大执行时间就跳出循环
                break;
            }
        }

        task = pollTask();//继续获取任务
        if (task == null) {
            lastExecutionTime = ScheduledFutureTask.nanoTime();
            break;
        }
    }

    afterRunningAllTasks();
    this.lastExecutionTime = lastExecutionTime;
    return true;
}
```

&emsp; NioEventLoop中维护了两组任务队列，一种是普通的taskQueue，一种是定时任务scheduledTaskQueue，而runAllTasks()方法首先会把scheduledTaskQueue队列中的定时任务转移到taskQueue中，然后在截止时间内循环执行  

```java
private boolean fetchFromScheduledTaskQueue() {
    long nanoTime = AbstractScheduledEventExecutor.nanoTime();
    //从定时任务队列中拉取任务
    Runnable scheduledTask  = pollScheduledTask(nanoTime);
    while (scheduledTask != null) {
        //把获取的scheduledTask插入taskQueue
        if (!taskQueue.offer(scheduledTask)) {
            // No space left in the task queue add it back to the scheduledTaskQueue so we pick it up again.
            scheduledTaskQueue().add((ScheduledFutureTask<?>) scheduledTask);
            return false;
        }
        scheduledTask  = pollScheduledTask(nanoTime);
    }
    return true;
}
```

&emsp; 定时任务队列ScheduledFutureTask是个优先级任务队列，会根据截止时间与任务id，保证截止时间最近的任务优先执行  

```java
final class ScheduledFutureTask<V> extends PromiseTask<V> implements ScheduledFuture<V>, PriorityQueueNode
```

```java
@Override
public int compareTo(Delayed o) {
    if (this == o) {
        return 0;
    }
    ScheduledFutureTask<?> that = (ScheduledFutureTask<?>) o;
    long d = deadlineNanos() - that.deadlineNanos();
    if (d < 0) {
        return -1;
    } else if (d > 0) {
        return 1;
    } else if (id < that.id) {
        return -1;
    } else if (id == that.id) {
        throw new Error();
    } else {
        return 1;
    }
}
```


## 1.2. Netty解决NIO的bug

### 1.2.1. NIO的空轮询bug  
&emsp; JDK1.5开始引入了epoll基于事件响应机制来优化NIO。相较于select和poll机制来说，epoll机制将事件处理交给了操作系统内核(操作系统硬中断)来处理，优化了elect和poll模型的无效遍历问题。  
&emsp; 但是JDK中epoll的实现却是有漏洞的，其中最有名的就是NIO空轮询bug。理论上无客户端连接时Selector.select() 方法会阻塞，但空轮询bug导致：即使无客户端连接，NIO照样不断的从select本应该阻塞的Selector.select()中wake up出来，导致CPU100%问题。如下图所示：  
![image](http://182.92.69.8:8081/img/microService/netty/netty-118.png)  


&emsp; 如上图所示，NIO程序一直处于while死循环中，不断向cpu申请资源导致CPU 100%！官方声称在JDK1.6版本的update18修复了该问题，但是直到JDK1.7、JDK1.8版本该问题仍旧存在，只不过该BUG发生概率降低了一些而已，它并没有被根本解决。  

### 1.2.2. netty如何解决NIO空轮询bug的？  
![image](http://182.92.69.8:8081/img/microService/netty/netty-113.png)  
&emsp; **selectCnt在正常逻辑时，会被重新赋值为1，在出现空轮询bug时会累加，直到大于阈值512，则触发重构selector操作。从这里可以看到netty并没有真正解决NIO的epoll模型的bug，而是采用替换selector的操作巧妙的避开了空轮询bug！**  
![image](http://182.92.69.8:8081/img/microService/netty/netty-114.png)  

![image](http://182.92.69.8:8081/img/microService/netty/netty-115.png)  

&emsp; **把旧的Selector中已注册的SelectionKey，全部挪到新的 Selector中去。**  
![image](http://182.92.69.8:8081/img/microService/netty/netty-116.png)  

&emsp; 成员变量this.selector 指向新的Selector的引用  
![image](http://182.92.69.8:8081/img/microService/netty/netty-117.png)  


<!-- 
&emsp; 1、 BUG出现的原因：  
&emsp; 若Selector的轮询结果为空，也没有wakeup或新消息处理，则发生N多次空轮询，使得CPU使用率100%  

&emsp; 2、Netty中的解决思路：  
&emsp; 对Selector()方法中的阻塞定时 select（timeMIllinois）操作的 次数进行统计，每完成一次select操作进行一次计数，若在循环周期内    发生N次空轮询，如果N值大于BUG阈值（默认为512），就进行空轮询BUG处理。重建Selector，判断是否是其他线程发起的重建请求，若不是则将原SocketChannel从旧的Selector上去除注册，重新注册到新的    Selector上，并将原来的Selector关闭。  
&emsp; 源码：已将部分代码省略，全部代码在io.netty.channel.nio.NioEventLoop类当中的select()方法中。  

&emsp; select方法分三个部分：  

```java
//第一部分：超时处理逻辑
//第二部分：定时阻塞select(timeMillins)  
// 第三步： 解决空轮询 BUG
            long time = System.nanoTime();
            //此处的逻辑就是： 当前时间 - 循环开始时间 >= 定时select的时间timeoutMillis，说明已经执行过一次阻塞select()
            if (time - TimeUnit.MILLISECONDS.toNanos(timeoutMillis) >= currentTimeNanos) {
                selectCnt = 1; // 说明发生过一次阻塞式轮询
                
                // 如果空轮询的次数大于空轮询次数阈值 SELECTOR_AUTO_REBUILD_THRESHOLD(512)
            } else if (SELECTOR_AUTO_REBUILD_THRESHOLD > 0 && selectCnt >= SELECTOR_AUTO_REBUILD_THRESHOLD) {
                /*  setlctRebuildSelector():
                *   1.首先创建一个新的Selecor
                *   2.将旧的Selector上面的键及其一系列的信息放到新的selector上面。
                /*
                selector = this.selectRebuildSelector(selectCnt);  
                selectCnt = 1;
                break;
            }
            currentTimeNanos = time;
```

-----------

&emsp; NioEventLoop的执行都是在run()方法的for循环中完成的。  
![image](http://182.92.69.8:8081/img/microService/netty/netty-112.png)  
&emsp; NioEventLoop中维护了一个线程，线程启动时会调用NioEventLoop的run方法，执行I/O任务和非I/O任务：  

* I/O任务  
&emsp; 即selectionKey中ready的事件，如accept、connect、read、write等，由processSelectedKeys方法触发。  
* 非IO任务  
&emsp; 添加到taskQueue中的任务，如register0、bind0等任务，由runAllTasks方法触发。


&emsp; 两种任务的执行时间比由变量ioRatio控制，默认为50，则表示允许非IO任务执行的时间与IO任务的执行时间相等。  

&emsp; NioEventLoop.run 方法实现  

```java
protected void run() {
    for (;;) {
        boolean oldWakenUp = wakenUp.getAndSet(false);
        try {
            if (hasTasks()) {
                selectNow();
            } else {
                select(oldWakenUp);
                if (wakenUp.get()) {
                    selector.wakeup();
                }
            }

            cancelledKeys = 0;
            needsToSelectAgain = false;
            final int ioRatio = this.ioRatio;
            if (ioRatio == 100) {
                processSelectedKeys();
                runAllTasks();
            } else {
                final long ioStartTime = System.nanoTime();

                processSelectedKeys();

                final long ioTime = System.nanoTime() - ioStartTime;
                runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
            }

            if (isShuttingDown()) {
                closeAll();
                if (confirmShutdown()) {
                    break;
                }
            }
        } catch (Throwable t) {
            logger.warn("Unexpected exception in the selector loop.", t);

            // Prevent possible consecutive immediate failures that lead to
            // excessive CPU consumption.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Ignore.
            }
        }
    }
}
```

&emsp; hasTasks()方法判断当前taskQueue是否有元素。  
&emsp; 1、 如果taskQueue中有元素，执行 selectNow() 方法，最终执行selector.selectNow()，该方法会立即返回。  

```java
void selectNow() throws IOException {
    try {
        selector.selectNow();
    } finally {
        // restore wakup state if needed
        if (wakenUp.get()) {
            selector.wakeup();
        }
    }
}
```

&emsp; 2、 如果taskQueue没有元素，执行 select(oldWakenUp) 方法，代码如下：  

```java
private void select(boolean oldWakenUp) throws IOException {
    Selector selector = this.selector;
    try {
        int selectCnt = 0;
        long currentTimeNanos = System.nanoTime();
        long selectDeadLineNanos = currentTimeNanos + delayNanos(currentTimeNanos);
        for (;;) {
            long timeoutMillis = (selectDeadLineNanos - currentTimeNanos + 500000L) / 1000000L;
            if (timeoutMillis <= 0) {
                if (selectCnt == 0) {
                    selector.selectNow();
                    selectCnt = 1;
                }
                break;
            }

            int selectedKeys = selector.select(timeoutMillis);
            selectCnt ++;

            if (selectedKeys != 0 || oldWakenUp || wakenUp.get() || hasTasks() || hasScheduledTasks()) {
                // - Selected something,
                // - waken up by user, or
                // - the task queue has a pending task.
                // - a scheduled task is ready for processing
                break;
            }
            if (Thread.interrupted()) {
                // Thread was interrupted so reset selected keys and break so we not run into a busy loop.
                // As this is most likely a bug in the handler of the user or it's client library we will
                // also log it.
                //
                // See https://github.com/netty/netty/issues/2426
                if (logger.isDebugEnabled()) {
                    logger.debug("Selector.select() returned prematurely because " +
                            "Thread.currentThread().interrupt() was called. Use " +
                            "NioEventLoop.shutdownGracefully() to shutdown the NioEventLoop.");
                }
                selectCnt = 1;
                break;
            }

            long time = System.nanoTime();
            if (time - TimeUnit.MILLISECONDS.toNanos(timeoutMillis) >= currentTimeNanos) {
                // timeoutMillis elapsed without anything selected.
                selectCnt = 1;
            } else if (SELECTOR_AUTO_REBUILD_THRESHOLD > 0 &&
                    selectCnt >= SELECTOR_AUTO_REBUILD_THRESHOLD) {
                // The selector returned prematurely many times in a row.
                // Rebuild the selector to work around the problem.
                logger.warn(
                        "Selector.select() returned prematurely {} times in a row; rebuilding selector.",
                        selectCnt);

                rebuildSelector();
                selector = this.selector;

                // Select again to populate selectedKeys.
                selector.selectNow();
                selectCnt = 1;
                break;
            }

            currentTimeNanos = time;
        }

        if (selectCnt > MIN_PREMATURE_SELECTOR_RETURNS) {
            if (logger.isDebugEnabled()) {
                logger.debug("Selector.select() returned prematurely {} times in a row.", selectCnt - 1);
            }
        }
    } catch (CancelledKeyException e) {
        if (logger.isDebugEnabled()) {
            logger.debug(CancelledKeyException.class.getSimpleName() + " raised by a Selector - JDK bug?", e);
        }
        // Harmless exception - log anyway
    }
}
```

&emsp; 这个方法解决了Nio中臭名昭著的bug：selector的select方法导致cpu100%。  
&emsp; 1、delayNanos(currentTimeNanos)：计算延迟任务队列中第一个任务的到期执行时间（即最晚还能延迟多长时间执行），默认返回1s。每个SingleThreadEventExecutor都持有一个延迟执行任务的优先队列PriorityQueue，启动线程时，往队列中加入一个任务。  

```java
protected long delayNanos(long currentTimeNanos) {  
    ScheduledFutureTask<?> delayedTask = delayedTaskQueue.peek();  
    if (delayedTask == null) {  
        return SCHEDULE_PURGE_INTERVAL;  
    }  
    return delayedTask.delayNanos(currentTimeNanos);  
}  
  
//ScheduledFutureTask  
public long delayNanos(long currentTimeNanos) {  
    return Math.max(0, deadlineNanos() - (currentTimeNanos - START_TIME));  
}  
public long deadlineNanos() {  
    return deadlineNanos;  
}  
```

&emsp; 2、如果延迟任务队列中第一个任务的最晚还能延迟执行的时间小于500000纳秒，且selectCnt == 0（selectCnt 用来记录selector.select方法的执行次数和标识是否执行过selector.selectNow()），则执行selector.selectNow()方法并立即返回。  
&emsp; 3、否则执行selector.select(timeoutMillis)，这个方法已经在深入浅出NIO Socket分析过。  
&emsp; 4、如果已经存在ready的selectionKey，或者selector被唤醒，或者taskQueue不为空，或则scheduledTaskQueue不为空，则退出循环。  
&emsp; 5、如果 selectCnt 没达到阈值SELECTOR_AUTO_REBUILD_THRESHOLD（默认512），则继续进行for循环。其中 currentTimeNanos 在select操作之后会重新赋值当前时间，如果selector.select(timeoutMillis)行为真的阻塞了timeoutMillis，第二次的timeoutMillis肯定等于0，此时selectCnt 为1，所以会直接退出for循环。  
&emsp; 6、如果触发了epool cpu100%的bug，会发生什么？  
selector.select(timeoutMillis)操作会立即返回，不会阻塞timeoutMillis，导致 currentTimeNanos 几乎不变，这种情况下，会反复执行selector.select(timeoutMillis)，变量selectCnt 会逐渐变大，当selectCnt 达到阈值，则执行rebuildSelector方法，进行selector重建，解决cpu占用100%的bug。  

```java
public void rebuildSelector() {  
        if (!inEventLoop()) {  
            execute(new Runnable() {  
                @Override  
                public void run() {  
                    rebuildSelector();  
                }  
            });  
            return;  
        }  
        final Selector oldSelector = selector;  
        final Selector newSelector;  
        if (oldSelector == null) {  
            return;  
        }  
        try {  
            newSelector = openSelector();  
        } catch (Exception e) {  
            logger.warn("Failed to create a new Selector.", e);  
            return;  
        }  
        // Register all channels to the new Selector.  
        int nChannels = 0;  
        for (;;) {  
            try {  
                for (SelectionKey key: oldSelector.keys()) {  
                    Object a = key.attachment();  
                    try {  
                        if (key.channel().keyFor(newSelector) != null) {  
                            continue;  
                        }  
                        int interestOps = key.interestOps();  
                        key.cancel();  
                        key.channel().register(newSelector, interestOps, a);  
                        nChannels ++;  
                    } catch (Exception e) {  
                        logger.warn("Failed to re-register a Channel to the new Selector.", e);  
                        if (a instanceof AbstractNioChannel) {  
                            AbstractNioChannel ch = (AbstractNioChannel) a;  
                            ch.unsafe().close(ch.unsafe().voidPromise());  
                        } else {  
                            @SuppressWarnings("unchecked")  
                            NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;  
                            invokeChannelUnregistered(task, key, e);  
                        }  
                    }  
                }  
            } catch (ConcurrentModificationException e) {  
                // Probably due to concurrent modification of the key set.  
                continue;  
            }  
  
            break;  
        }    
        selector = newSelector;  
        try {  
            // time to close the old selector as everything else is registered to the new one  
            oldSelector.close();  
        } catch (Throwable t) {  
            if (logger.isWarnEnabled()) {  
                logger.warn("Failed to close the old Selector.", t);  
            }  
        }    
        logger.info("Migrated " + nChannels + " channel(s) to the new Selector.");  
    }  
```

&emsp; rebuildSelector过程：  
&emsp; 1、通过方法openSelector创建一个新的selector。  
&emsp; 2、将old selector的selectionKey执行cancel。  
&emsp; 3、将old selector的channel重新注册到新的selector中。  

&emsp; 对selector进行rebuild后，需要重新执行方法selectNow，检查是否有已ready的selectionKey。  
&emsp; 方法selectNow()或select(oldWakenUp)返回后，执行方法processSelectedKeys和runAllTasks。  
&emsp; 1、processSelectedKeys 用来处理有事件发生的selectkey，这里对优化过的方法processSelectedKeysOptimized进行分析：  

```java
private void processSelectedKeysOptimized(SelectionKey[] selectedKeys) {
    for (int i = 0;; i ++) {
        final SelectionKey k = selectedKeys[i];
        if (k == null) {
            break;
        }
        // null out entry in the array to allow to have it GC'ed once the Channel close
        // See https://github.com/netty/netty/issues/2363
        selectedKeys[i] = null;

        final Object a = k.attachment();

        if (a instanceof AbstractNioChannel) {
            processSelectedKey(k, (AbstractNioChannel) a);
        } else {
            @SuppressWarnings("unchecked")
            NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
            processSelectedKey(k, task);
        }

        if (needsToSelectAgain) {
            // null out entries in the array to allow to have it GC'ed once the Channel close
            // See https://github.com/netty/netty/issues/2363
            for (;;) {
                i++;
                if (selectedKeys[i] == null) {
                    break;
                }
                selectedKeys[i] = null;
            }

            selectAgain();
            // Need to flip the optimized selectedKeys to get the right reference to the array
            // and reset the index to -1 which will then set to 0 on the for loop
            // to start over again.
            //
            // See https://github.com/netty/netty/issues/1523
            selectedKeys = this.selectedKeys.flip();
            i = -1;
        }
    }
}
```

&emsp; 在优化过的方法中，有事件发生的selectkey存放在数组selectedKeys中，通过遍历selectedKeys，处理每一个selectkey，具体处理过程，会在后续进行分析。  

&emsp; 2、runAllTasks 处理非I/O任务。  
&emsp; 如果 ioRatio 不为100时，方法runAllTasks的执行时间只能为ioTime * (100 - ioRatio) / ioRatio，其中ioTime 是方法processSelectedKeys的执行时间。  

```java
protected boolean runAllTasks(long timeoutNanos) {
    fetchFromScheduledTaskQueue();
    Runnable task = pollTask();
    if (task == null) {
        return false;
    }

    final long deadline = ScheduledFutureTask.nanoTime() + timeoutNanos;
    long runTasks = 0;
    long lastExecutionTime;
    for (;;) {
        try {
            task.run();
        } catch (Throwable t) {
            logger.warn("A task raised an exception.", t);
        }
        runTasks ++;
        // Check timeout every 64 tasks because nanoTime() is relatively expensive.
        // XXX: Hard-coded value - will make it configurable if it is really a problem.
        if ((runTasks & 0x3F) == 0) {
            lastExecutionTime = ScheduledFutureTask.nanoTime();
            if (lastExecutionTime >= deadline) {
                break;
            }
        }
        task = pollTask();
        if (task == null) {
            lastExecutionTime = ScheduledFutureTask.nanoTime();
            break;
        }
    }
    this.lastExecutionTime = lastExecutionTime;
    return true;
}

```

&emsp; 方法fetchFromScheduledTaskQueue把scheduledTaskQueue中已经超过延迟执行时间的任务移到taskQueue中等待被执行。  

```java
private void fetchFromScheduledTaskQueue() {
    if (hasScheduledTasks()) {
        long nanoTime = AbstractScheduledEventExecutor.nanoTime();
        for (;;) {
            Runnable scheduledTask = pollScheduledTask(nanoTime);
            if (scheduledTask == null) {
                break;
            }
            taskQueue.add(scheduledTask);
        }
    }
}
```

&emsp; 依次从taskQueue任务task执行，每执行64个任务，进行耗时检查，如果已执行时间超过预先设定的执行时间，则停止执行非IO任务，避免非IO任务太多，影响IO任务的执行。  
-->