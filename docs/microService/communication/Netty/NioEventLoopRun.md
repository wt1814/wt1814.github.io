
<!-- TOC -->

- [1. ~~NioEventLoop的执行，解决NIO的bug~~](#1-nioeventloop的执行解决nio的bug)
    - [1.1. NIO的空轮询bug](#11-nio的空轮询bug)
    - [1.2. netty如何解决NIO空轮询bug的？](#12-netty如何解决nio空轮询bug的)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. netty如何解决NIO空轮询bug的？  
&emsp; **selectCnt在正常逻辑时，会被重新赋值为1，在出现空轮询bug时会累加，直到大于阈值512，则触发重构selector操作。从这里可以看到netty并没有真正解决NIO的epoll模型的bug，而是采用替换selector的操作巧妙的避开了空轮询bug！**  
&emsp; **把旧的Selector中已注册的SelectionKey，全部挪到新的 Selector中去。**  


# 1. ~~NioEventLoop的执行，解决NIO的bug~~  
<!-- 
https://www.cnblogs.com/dafanjoy/p/10507662.html
https://www.jianshu.com/p/9acf36f7e025

https://blog.csdn.net/qq_43049310/article/details/113688981
-->

## 1.1. NIO的空轮询bug  
&emsp; JDK1.5开始引入了epoll基于事件响应机制来优化NIO。相较于select和poll机制来说，epoll机制将事件处理交给了操作系统内核(操作系统硬中断)来处理，优化了elect和poll模型的无效遍历问题。  
&emsp; 但是JDK中epoll的实现却是有漏洞的，其中最有名的就是NIO空轮询bug。理论上无客户端连接时Selector.select() 方法会阻塞，但空轮询bug导致：即使无客户端连接，NIO照样不断的从select本应该阻塞的Selector.select()中wake up出来，导致CPU100%问题。如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-118.png)  


&emsp; 如上图所示，NIO程序一直处于while死循环中，不断向cpu申请资源导致CPU 100%！官方声称在JDK1.6版本的update18修复了该问题，但是直到JDK1.7、JDK1.8版本该问题仍旧存在，只不过该BUG发生概率降低了一些而已，它并没有被根本解决。  

## 1.2. netty如何解决NIO空轮询bug的？  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-113.png)  
&emsp; **selectCnt在正常逻辑时，会被重新赋值为1，在出现空轮询bug时会累加，直到大于阈值512，则触发重构selector操作。从这里可以看到netty并没有真正解决NIO的epoll模型的bug，而是采用替换selector的操作巧妙的避开了空轮询bug！**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-114.png)  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-115.png)  

&emsp; **把旧的Selector中已注册的SelectionKey，全部挪到新的 Selector中去。**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-116.png)  

&emsp; 成员变量this.selector 指向新的Selector的引用  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-117.png)  


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
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-112.png)  
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