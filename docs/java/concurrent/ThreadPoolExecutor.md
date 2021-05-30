<!-- TOC -->

- [1. ~~ThreadPoolExecutor~~](#1-threadpoolexecutor)
    - [1.1. 属性](#11-属性)
        - [1.1.1. 线程池状态](#111-线程池状态)
    - [1.2. 构造函数](#12-构造函数)
    - [1.3. 线程池工作流程(execute成员方法的源码)](#13-线程池工作流程execute成员方法的源码)
        - [1.3.1. execute()](#131-execute)
        - [1.3.2. addWorker()，创建新的线程](#132-addworker创建新的线程)
        - [1.3.3. 内部类work()，工作线程的实现](#133-内部类work工作线程的实现)
        - [1.3.4. runWorker()，线程复用机制](#134-runworker线程复用机制)
            - [1.3.4.1. getTask() ，线程池如何保证核心线程不被销毁？](#1341-gettask-线程池如何保证核心线程不被销毁)
            - [1.3.4.2. processWorkerExit()](#1342-processworkerexit)
                - [1.3.4.2.1. tryTerminate()](#13421-tryterminate)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 理解构造函数中参数：  
    * [阻塞队列](/docs/java/concurrent/BlockingQueue.md)，线程池所使用的缓冲队列，常用的是：SynchronousQueue、ArrayBlockingQueue、LinkedBlockingQueue。   
    * 拒绝策略，默认AbortPolicy（拒绝任务，抛异常），可以选用CallerRunsPolicy（任务队列满时，不进入线程池，由主线程执行）。  

2. 线程运行流程：查看execute方法。  
    &emsp; <font color = "clime">线程池创建时没有设置成预启动加载，首发线程数为0。</font><font color = "red">任务队列是作为参数传进来的。即使队列里面有任务，线程池也不会马上执行它们，而是创建线程。</font>当一个线程完成任务时，它会从队列中取下一个任务来执行。当调用execute()方法添加一个任务时，线程池会做如下判断：  
    1. 如果当前工作线程总数小于corePoolSize，则直接创建核心线程执行任务（任务实例会传入直接用于构造工作线程实例）。  
    2. 如果当前工作线程总数大于等于corePoolSize，判断线程池是否处于运行中状态，同时尝试用非阻塞方法向任务队列放入任务，这里会二次检查线程池运行状态，如果当前工作线程数量为0，则创建一个非核心线程并且传入的任务对象为null。  
    3. 如果向任务队列投放任务失败（任务队列已经满了），则会尝试创建非核心线程传入任务实例执行。  
    4. 如果创建非核心线程失败，此时需要拒绝执行任务，调用拒绝策略处理任务。  
 
3. 线程复用机制：    
&emsp; **线程池将线程和任务进行解耦，线程是线程，任务是任务，摆脱了之前通过Thread创建线程时的一个线程必须对应一个任务的限制。**  
&emsp; **<font color = "red">在线程池中，同一个线程可以从阻塞队列中不断获取新任务来执行，其核心原理在于线程池对Thread进行了封装（内部类Worker），并不是每次执行任务都会调用Thread.start() 来创建新线程，而是让每个线程去执行一个“循环任务”，在这个“循环任务”中不停的检查是否有任务需要被执行。</font>** 如果有则直接执行，也就是调用任务中的run方法，将run方法当成一个普通的方法执行，通过这种方式将只使用固定的线程就将所有任务的run方法串联起来。  
&emsp; 源码解析：runWorker()方法中，有任务时，while循环获取；没有任务时，清除空闲线程。  
5. 线程池保证核心线程不被销毁？  
&emsp; 获取任务getTask()方法里allowCoreThreadTimeOut值默认为true，线程take()会一直阻塞，等待任务的添加。   


# 1. ~~ThreadPoolExecutor~~
<!--
https://mp.weixin.qq.com/s/hnulrvdxbLDQSN1_3PXtFQ

https://mp.weixin.qq.com/s/0OsdfR3nmZTETw4p6B1dSA
https://mp.weixin.qq.com/s/b9zF6jcZQn6wdjzo8C-TmA
面试官：线程池是如何重复利用空闲的线程来执行任务的？ 
https://mp.weixin.qq.com/s/aKv2bRUQ-0rnaSUud84VEw
https://mp.weixin.qq.com/s/i71TbgqVHyHdqjMdBqklvA
-->
<!-- 
~~
深入分析线程池的实现原理 
https://mp.weixin.qq.com/s/L4u374rmxEq9vGMqJrIcvw
线程池源码解析系列：为什么要使用位运算表示线程池状态 
https://mp.weixin.qq.com/s/5gvBukbh1dfa1lzsoWq8xA
-->

## 1.1. 属性

```java
//任务缓存队列，用来存放等待执行的任务
private final BlockingQueue<Runnable> workQueue;
//全局锁，对线程池状态等属性修改时需要使用这个锁
private final ReentrantLock mainLock = new ReentrantLock();
//线程池中工作线程的集合，访问和修改需要持有全局锁
private final HashSet<Worker> workers = new HashSet<Worker>();
// 终止条件
private final Condition termination = mainLock.newCondition();
//线程池中曾经出现过的最大线程数
private int largestPoolSize;
//已完成任务的数量
private long completedTaskCount;
//线程工厂
private volatile ThreadFactory threadFactory;
//任务拒绝策略
private volatile RejectedExecutionHandler handler;
//线程存活时间
private volatile long keepAliveTime;
//是否允许核心线程超时
private volatile boolean allowCoreThreadTimeOut;
//核心池大小，若allowCoreThreadTimeOut被设置，核心线程全部空闲超时被回收的情况下会为0
private volatile int corePoolSize;
//最大池大小，不得超过CAPACITY
private volatile int maximumPoolSize;
//默认的任务拒绝策略
private static final RejectedExecutionHandler defaultHandler = new AbortPolicy();

private static final RuntimePermission shutdownPerm = new RuntimePermission("modifyThread");

private final AccessControlContext acc;
```

### 1.1.1. 线程池状态
<!-- 
https://mp.weixin.qq.com/s/b9zF6jcZQn6wdjzo8C-TmA
https://mp.weixin.qq.com/s/0OsdfR3nmZTETw4p6B1dSA
-->
&emsp; 线程池状态控制主要围绕原子整型成员变量ctl：  

```java
//AtomicInteger是原子类 ctlOf()返回值为RUNNING
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
//高3位表示线程状态
private static final int COUNT_BITS = Integer.SIZE - 3;
//低29位表示workerCount容量
private static final int COUNT_MASK = (1 << COUNT_BITS) - 1;

//能接收任务且能处理阻塞队列中的任务
private static final int RUNNING    = -1 << COUNT_BITS;
//不能接收新任务，但可以处理队列中但任务
private static final int SHUTDOWN   =  0 << COUNT_BITS;
//不能接收新任务，不处理队列任务
private static final int STOP       =  1 << COUNT_BITS;
//所有任务都终止
private static final int TIDYING    =  2 << COUNT_BITS;
//什么都不做
private static final int TERMINATED =  3 << COUNT_BITS;

// 通过与的方式，获取ctl的高3位，也就是线程池的运行状态
// 通过ctl值获取运行状态
private static int runStateOf(int c)     { return c & ~COUNT_MASK; }
//通过与的方式，获取ctl的低29位，也就是线程池中工作线程的数量
// 通过ctl值获取工作线程数
private static int workerCountOf(int c)  { return c & COUNT_MASK; }
//通过或的方式，将线程池状态和线程池中工作线程的数量打包成ctl
//通过运行状态和工作线程数计算ctl的值，或运算
private static int ctlOf(int rs, int wc) { return rs | wc; }

private static boolean runStateLessThan(int c, int s) {
    return c < s;
}

private static boolean runStateAtLeast(int c, int s) {
    return c >= s;
}
//SHUTDOWN状态的值是0，比它大的均是线程池停止或清理状态，比它小的是运行状态
private static boolean isRunning(int c) {
    return c < SHUTDOWN;
}

// CAS操作线程数增加1
private boolean compareAndIncrementWorkerCount(int expect) {
    return ctl.compareAndSet(expect, expect + 1);
}

// CAS操作线程数减少1
private boolean compareAndDecrementWorkerCount(int expect) {
    return ctl.compareAndSet(expect, expect - 1);
}

// 线程数直接减少1
private void decrementWorkerCount() {
    ctl.addAndGet(-1);
}
```

&emsp; **<font color = "red">线程池状态：</font>**  
&emsp; 整型包装类型Integer实例的大小是4 byte，一共32 bit，也就是一共有32个位用于存放0或者1。在ThreadPoolExecutor实现中，使用32位的整型包装类型存放工作线程数和线程池状态。其中，低29位用于存放工作线程数，而高3位用于存放线程池状态。 **<font color = "red">线程池存在5种状态：</font>**  

* RUNNING：高3位为111，在这个状态的线程池能判断接收新提交的任务，并且也能处理阻塞队列中的任务。  
* SHUTDOWN：高3位为000，处于关闭的状态，该线程池不能接收新提交的任务，但是可以处理阻塞队列中已经保存的任务，在线程处于RUNNING状态，调用shutdown()方法能切换为该状态。  
* STOP：高3位为001，线程池处于该状态时既不能接收新的任务也不能处理阻塞队列中的任务，并且能中断现在线程中的任务。当线程处于RUNNING和SHUTDOWN状态，调用shutdownNow()方法就可以使线程变为该状态。  
* TIDYING：高3位为010，在SHUTDOWN状态下阻塞队列为空，且线程中的工作线程数量为0就会进入该状态，当在STOP状态下时，只要线程中的工作线程数量为0就会进入该状态。  
* TERMINATED：高3位为011，在TIDYING状态下调用terminated()方法就会进入该状态。可以认为该状态是最终的终止状态。  

&emsp; 线程池状态的所有转换情况，如下：  

* RUNNING -> SHUTDOWN：调用shutdown()，可能在finalize()中隐式调用   
* (RUNNING or SHUTDOWN) -> STOP：调用shutdownNow()  
* SHUTDOWN -> TIDYING：当缓存队列和线程池都为空时  
* STOP -> TIDYING：当线程池为空时  
* TIDYING -> TERMINATED：当terminated()方法执行结束时  

&emsp; 通常情况下，线程池有如下两种状态转换流程：
    
* RUNNING -> SHUTDOWN -> TIDYING -> TERMINATED
* RUNNING -> STOP -> TIDYING -> TERMINATED

&emsp; 线程池状态切换图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-4.png)  

## 1.2. 构造函数  
<!-- 
一般来说，这里的BlockingQueue有以下三种选择：
    SynchronousQueue：
    一个不存储元素的阻塞队列，每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态。
    因此，如果线程池中始终没有空闲线程（任务提交的平均速度快于被处理的速度），可能出现无限制的线程增长。
    LinkedBlockingQueue：
    基于链表结构的阻塞队列，如果不设置初始化容量，其容量为Integer.MAX_VALUE，即为无界队列。
    因此，如果线程池中线程数达到了corePoolSize，且始终没有空闲线程（任务提交的平均速度快于被处理的速度），任务缓存队列可能出现无限制的增长。
    ArrayBlockingQueue：基于数组结构的有界阻塞队列，按FIFO排序任务。
-->
```java
public ThreadPoolExecutor(int corePoolSize,
                        int maximumPoolSize,
                        long keepAliveTime,
                        TimeUnit unit,
                        BlockingQueue<Runnable> workQueue,
                        ThreadFactory threadFactory,
                        RejectedExecutionHandler handler) {
    if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
        throw new IllegalArgumentException();
    if (workQueue == null || threadFactory == null || handler == null)
        throw new NullPointerException();
    this.acc = System.getSecurityManager() == null ?
            null :
            AccessController.getContext();
    this.corePoolSize = corePoolSize;
    this.maximumPoolSize = maximumPoolSize;
    this.workQueue = workQueue;
    this.keepAliveTime = unit.toNanos(keepAliveTime);
    this.threadFactory = threadFactory;
    this.handler = handler;
}
```
&emsp; ThreadPoolExecutor构造函数中各个参数的含义：  

* int  corePoolSize：  
&emsp; 线程池的核心线程数大小。默认情况下，在创建了线程池后，线程池中的线程数为0，当有任务来之后，就会创建一个线程去执行任务，当线程池中的线程数目达到corePoolSize后，就会把到达的任务放到缓存队列当中。默认情况下可以一直存活。可以通过设置allowCoreThreadTimeOut为True，此时核心线程数就是0，此时keepAliveTime控制所有线程的超时时间。  
* int  maximumPoolSize：  
&emsp; 线程池允许的最大线程数大小。当workQueue满了，不能添加任务的时候，这个参数才会生效。  
* long  keepAliveTime：  
&emsp; 空闲线程（超出corePoolSize的线程）的生存时间。这些线程如果长时间没有执行任务，并且超过了keepAliveTime设定的时间，就会消亡。  
* TimeUnit unit：  
&emsp; 参数keepAliveTime的单位。有7种取值，在TimeUnit类中有7种静态属性：TimeUnit.DAYS...；  
* BlockingQueue<Runnable\>  workQueue：  
&emsp; 任务阻塞队列，是java.util.concurrent下主要用来控制线程同步的工具。如果BlockQueue是空的，从BlockingQueue取东西的操作将会被阻断进入等待状态，直到BlockingQueue进了东西才会被唤醒。同样，如果BlockingQueue是满的，任何试图往里存东西的操作也会被阻断进入等待状态，直到BlockingQueue里有空间才会被唤醒继续操作。  
&emsp; <font color = "clime">线程池所使用的缓冲队列，常用的是：java.util.concurrent.ArrayBlockingQueue、LinkedBlockingQueue、SynchronousQueue。</font>**  
&emsp; 具体的实现类有LinkedBlockingQueue，ArrayBlockingQueued等。一般其内部的都是通过Lock和Condition来实现阻塞和唤醒。  
* ThreadFactory threadFactory：  
&emsp; 用户设置创建线程的工厂，可以通过这个工厂来创建有业务意义的线程名字。可以对比下自定义的线程工厂和默认的线程工厂创建的名字。   
    
    |默认产生线程的名字	|自定义线程工厂产生名字|
    |---|---|
    |pool-5-thread-1|testPool-1-thread-1|

    &emsp; 阿里开发手册也有明确说到，需要指定有意义的线程名字。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-18.png)  

* RejectedExecutionHandler handler：  
&emsp; <font color = "red">当提交任务数超过maxmumPoolSize+workQueue之和时，任务会交给RejectedExecutionHandler来处理，执行拒绝策略。</font>有四种策略，<font color = "clime">默认是AbortPolicy(丢弃任务并抛出RejectedExecutionException异常)</font>。内置拒绝策略均实现了RejectedExecutionHandler接口，若以下策略仍无法满足实际需要，可以扩展RejectedExecutionHandler接口。  

    | 名称 | Condition |  
    |----|----|  
    |AbortPolicy (默认)|丢弃任务并抛出RejectedExecutionException异常。| 
    |<font color = "clime">CallerRunsPolicy</font>|<font color = "clime">在线程池当前正在运行的Thread线程池中处理被拒绝的任务。主线程会被阻塞，其余任务只能在被拒绝的任务执行完之后才会继续被提交到线程池执行。</font>|
    |DiscardOldestPolicy|丢弃队列最前面的任务，将被拒绝的任务添加到等待队列中。|
    |DiscardPolicy|丢弃任务，但是不抛出异常。|


## 1.3. 线程池工作流程(execute成员方法的源码)
<!-- 
提交任务
https://mp.weixin.qq.com/s/L4u374rmxEq9vGMqJrIcvw
-->
&emsp; 线程池中核心方法调用链路：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-17.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-14.png)  
&emsp; 流程概述：  
&emsp; <font color = "clime">线程池创建时没有设置成预启动加载，首发线程数为0。</font><font color = "red">任务队列是作为参数传进来的。即使队列里面有任务，线程池也不会马上执行它们，而是创建线程。</font>当一个线程完成任务时，它会从队列中取下一个任务来执行。当调用execute()方法添加一个任务时，线程池会做如下判断：  
1. 如果当前工作线程总数小于corePoolSize，则直接创建核心线程执行任务（任务实例会传入直接用于构造工作线程实例）。  
2. 如果当前工作线程总数大于等于corePoolSize，判断线程池是否处于运行中状态，同时尝试用非阻塞方法向任务队列放入任务，这里会二次检查线程池运行状态，如果当前工作线程数量为0，则创建一个非核心线程并且传入的任务对象为null。  
3. 如果向任务队列投放任务失败（任务队列已经满了），则会尝试创建非核心线程传入任务实例执行。  
4. 如果创建非核心线程失败，此时需要拒绝执行任务，调用拒绝策略处理任务。  

        为什么需要二次检查线程池的运行状态，当前工作线程数量为0，尝试创建一个非核心线程并且传入的任务对象为null？这个可以看API注释：
        如果一个任务成功加入任务队列，依然需要二次检查是否需要添加一个工作线程（因为所有存活的工作线程有可能在最后一次检查之后已经终结）或者执行当前方法的时候线程池是否已经shutdown了。所以需要二次检查线程池的状态，必须时把任务从任务队列中移除或者在没有可用的工作线程的前提下新建一个工作线程。


### 1.3.1. execute()  

<!-- 
1. 线程池刚创建时，里面没有一个线程。任务队列是作为参数传进来的。不过，就算队列里面有任务，线程池也不会马上执行它们，而是创建线程。当一个线程完成任务时，它会从队列中取下一个任务来执行。  
2. 当调用execute()方法添加一个任务时，线程池会做如下判断：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-5.png)  
	1. 如果线程池中的线程数量少于corePoolSize(核心线程数量)，那么会直接开启一个新的核心线程来执行任务，即使此时有空闲线程存在。   
	2. 如果线程池中线程数量大于等于corePoolSize(核心线程数量)，那么任务会被插入到任务队列中排队，等待被执行。此时并不添加新的线程。如果是无界队列，则线程大小一直会是核心线程池的大小。   
	3. 如果在步骤2中由于任务队列已满导致无法将新任务进行排队，这个时候有两种情况：  
        &emsp; 线程数量[未]达到maximumPoolSize(线程池最大线程数)，立刻启动一个非核心线程来执行任务。  
        &emsp; 线程数量[已]达到maximumPoolSize(线程池最大线程数)，将会执行拒绝策略。  
3. 当一个线程空闲，超过一定的时间(keepAliveTime)时，线程池会判断，如果当前运行的线程数大于corePoolSize，那么这个线程就被停掉。所以线程池的所有任务完成后，它最终会收缩到corePoolSize的大小。  
-->

```java
public void execute(Runnable command) {
    if (command == null)
        throw new NullPointerException();
    //获取线程池控制状态
    int c = ctl.get();
    //1.当前池中线程比核心数少，新建一个线程执行任务
    if (workerCountOf(c) < corePoolSize) {
        if (addWorker(command, true))//创建worker,addWorker方法boolean参数用来判断是否创建核心线程
            return;
        c = ctl.get();//失败则再次获取线程池控制状态
    }
    //2.核心池已满，但任务队列未满，添加到队列中
    if (isRunning(c) && workQueue.offer(command)) {
        int recheck = ctl.get();
        //任务成功添加到队列以后，再次检查是否需要添加新的线程，因为已存在的线程可能被销毁了
        if (! isRunning(recheck) && remove(command))
            reject(command); //如果线程池处于非运行状态，并且把当前的任务从任务队列中移除成功，则拒绝该任务
        else if (workerCountOf(recheck) == 0) //如果之前的线程已被销毁完，新建一个线程
            addWorker(null, false);  //3.核心池已满，队列已满，试着创建一个新线程
    }
    //3.线程数超过核心线程数且任务队列中数据已满。
    else if (!addWorker(command, false))
        reject(command); //如果创建新线程失败了，说明线程池被关闭或者线程池完全满了，拒绝任务
}
```

### 1.3.2. addWorker()，创建新的线程  
&emsp; addWorker()方法主要负责创建新的线程并执行任务。完成了如下几件任务：  
1. 原子性的增加workerCount。
2. 将用户给定的任务封装成为一个worker，并将此worker添加进workers集合中。
3. 启动worker对应的线程。
4. 若线程启动失败，回滚worker的创建动作，即从workers中移除新添加的worker，并原子性的减少workerCount。

```java
/**
* 该方法的返回值代表是否成功新增一个工作线程。如果返回false说明没有新创建工作线程，如果返回true说明创建和启动工作线程成功
* @param firstTask 用于指定新增的线程执行的第一个任务
* @param core 表示是否创建核心线程
* @return
*/
private boolean addWorker(Runnable firstTask, boolean core) {
    retry:
    // 注意这是一个死循环 - 最外层循环
    for (int c = ctl.get();;) {
        // 这个是十分复杂的条件，这里先拆分多个与(&&)条件：
        // 1. 线程池状态至少为SHUTDOWN状态，也就是rs >= SHUTDOWN(0)
        // 2. 线程池状态至少为STOP状态，也就是rs >= STOP(1)，或者传入的任务实例firstTask不为null，或者任务队列为空
        // 其实这个判断的边界是线程池状态为shutdown状态下，不会再接受新的任务，在此前提下如果状态已经到了STOP、或者传入任务不为空、或者任务队列为空(已经没有积压任务)都不需要添加新的线程
        if (runStateAtLeast(c, SHUTDOWN)
            && (runStateAtLeast(c, STOP)
                || firstTask != null
                || workQueue.isEmpty()))
            return false;
        // 注意这也是一个死循环 - 二层循环
        for (;;) {
            // 这里每一轮循环都会重新获取工作线程数wc
            // 1. 如果传入的core为true，表示将要创建核心线程，通过wc和corePoolSize判断，如果wc >= corePoolSize，则返回false表示创建核心线程失败
            // 1. 如果传入的core为false，表示将要创非建核心线程，通过wc和maximumPoolSize判断，如果wc >= maximumPoolSize，则返回false表示创建非核心线程失败
            if (workerCountOf(c)
                >= ((core ? corePoolSize : maximumPoolSize) & COUNT_MASK))
                return false;
            // 成功通过CAS更新工作线程数wc，则break到最外层的循环
            if (compareAndIncrementWorkerCount(c))
                break retry;
            // 走到这里说明了通过CAS更新工作线程数wc失败，这个时候需要重新判断线程池的状态是否由RUNNING已经变为SHUTDOWN
            c = ctl.get();  // Re-read ctl
            // 如果线程池状态已经由RUNNING已经变为SHUTDOWN，则重新跳出到外层循环继续执行
            if (runStateAtLeast(c, SHUTDOWN))
                continue retry;
            // 如果线程池状态依然是RUNNING，CAS更新工作线程数wc失败说明有可能是并发更新导致的失败，则在内层循环重试即可
            // else CAS failed due to workerCount change; retry inner loop
        }
    }
    // 标记工作线程是否启动成功
    boolean workerStarted = false;
    // 标记工作线程是否创建成功
    boolean workerAdded = false;
    Worker w = null;
    try {
        // 传入任务实例firstTask创建Worker实例，Worker构造里面会通过线程工厂创建新的Thread对象，所以下面可以直接操作Thread t = w.thread
        // 这一步Worker实例已经创建，但是没有加入工作线程集合或者启动它持有的线程Thread实例
        w = new Worker(firstTask);
        final Thread t = w.thread;
        if (t != null) {
            // 这里需要全局加锁，因为会改变一些指标值和非线程安全的集合
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                // Recheck while holding lock.
                // Back out on ThreadFactory failure or if
                // shut down before lock acquired.
                int c = ctl.get();
                // 这里主要在加锁的前提下判断ThreadFactory创建的线程是否存活或者判断获取锁成功之后线程池状态是否已经更变为SHUTDOWN
                // 1. 如果线程池状态依然为RUNNING，则只需要判断线程实例是否存活，需要添加到工作线程集合和启动新的Worker
                // 2. 如果线程池状态小于STOP，也就是RUNNING或者SHUTDOWN状态下，同时传入的任务实例firstTask为null，则需要添加到工作线程集合和启动新的Worker
                // 对于2，换言之，如果线程池处于SHUTDOWN状态下，同时传入的任务实例firstTask不为null，则不会添加到工作线程集合和启动新的Worker
                // 这一步其实有可能创建了新的Worker实例但是并不启动(临时对象，没有任何强引用)，这种Worker有可能成功下一轮GC被收集的垃圾对象
                if (isRunning(c) ||
                    (runStateLessThan(c, STOP) && firstTask == null)) {
                    if (t.isAlive()) // precheck that t is startable
                        throw new IllegalThreadStateException();
                    // 把创建的工作线程实例添加到工作线程集合
                    workers.add(w);
                    int s = workers.size();
                    // 尝试更新历史峰值工作线程数，也就是线程池峰值容量
                    if (s > largestPoolSize)
                        largestPoolSize = s;
                    // 这里更新工作线程是否启动成功标识为true，后面才会调用Thread#start()方法启动真实的线程实例
                    workerAdded = true;
                }
            } finally {
                mainLock.unlock();
            }
            // 如果成功添加工作线程，则调用Worker内部的线程实例t的Thread#start()方法启动真实的线程实例
            if (workerAdded) {
                t.start();
                // 标记线程启动成功
                workerStarted = true;
            }
        }
    } finally {
        // 线程启动失败，需要从工作线程集合移除对应的Worker
        if (! workerStarted)
            addWorkerFailed(w);
    }
    return workerStarted;
}

// 添加Worker失败
private void addWorkerFailed(Worker w) {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        // 从工作线程集合移除之
        if (w != null)
            workers.remove(w);
        // wc数量减1
        decrementWorkerCount();
        // 基于状态判断尝试终结线程池
        tryTerminate();
    } finally {
        mainLock.unlock();
    }
}
```

### 1.3.3. 内部类work()，工作线程的实现  
&emsp; **线程池中的每一个具体的工作线程被包装为内部类Worker实例，** Worker继承于AbstractQueuedSynchronizer(AQS)，实现了Runnable接口：  

```java
private final class Worker extends AbstractQueuedSynchronizer implements Runnable{
    /**
        * This class will never be serialized, but we provide a
        * serialVersionUID to suppress a javac warning.
        */
    private static final long serialVersionUID = 6138294804551838833L;

    // 保存ThreadFactory创建的线程实例，如果ThreadFactory创建线程失败则为null
    final Thread thread;
    // 保存传入的Runnable任务实例
    Runnable firstTask;
    // 记录每个线程完成的任务总数
    volatile long completedTasks;
    
    // 唯一的构造函数，传入任务实例firstTask，注意可以为null
    Worker(Runnable firstTask) {
        // 禁止线程中断，直到runWorker()方法执行
        setState(-1); // inhibit interrupts until runWorker
        this.firstTask = firstTask;
        // 通过ThreadFactory创建线程实例，注意一下Worker实例自身作为Runnable用于创建新的线程实例
        this.thread = getThreadFactory().newThread(this);
    }

    // 委托到外部的runWorker()方法，注意runWorker()方法是线程池的方法，而不是Worker的方法
    public void run() {
        runWorker(this);
    }

    // Lock methods
    //
    // The value 0 represents the unlocked state.
    // The value 1 represents the locked state.
    //  是否持有独占锁，state值为1的时候表示持有锁，state值为0的时候表示已经释放锁
    protected boolean isHeldExclusively() {
        return getState() != 0;
    }

    // 独占模式下尝试获取资源，这里没有判断传入的变量，直接CAS判断0更新为1是否成功，成功则设置独占线程为当前线程
    protected boolean tryAcquire(int unused) {
        if (compareAndSetState(0, 1)) {
            setExclusiveOwnerThread(Thread.currentThread());
            return true;
        }
        return false;
    }
    
    // 独占模式下尝试是否资源，这里没有判断传入的变量，直接把state设置为0
    protected boolean tryRelease(int unused) {
        setExclusiveOwnerThread(null);
        setState(0);
        return true;
    }
    
    // 加锁
    public void lock() { 
        acquire(1); 
    }

    // 尝试加锁
    public boolean tryLock() { 
        return tryAcquire(1); 
    }

    // 解锁
    public void unlock() { 
        release(1); 
    }

    // 是否锁定
    public boolean isLocked() { 
        return isHeldExclusively(); 
    }
    
    // 启动后进行线程中断，注意这里会判断线程实例的中断标志位是否为false，只有中断标志位为false才会中断
    void interruptIfStarted() {
        Thread t;
        if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
            try {
                t.interrupt();
            } catch (SecurityException ignore) {
            }
        }
    }
}
```
&emsp; Worker的构造函数里面的逻辑十分重要，通过ThreadFactory创建的Thread实例同时传入Worker实例，因为Worker本身实现了Runnable，所以可以作为任务提交到线程中执行。只要Worker持有的线程实例w调用Thread#start()方法就能在合适时机执行Worker#run()。简化一下逻辑如下：  

```java
// addWorker()方法中构造
Worker worker = createWorker();
// 通过线程池构造时候传入
ThreadFactory threadFactory = getThreadFactory();
// Worker构造函数中
Thread thread = threadFactory.newThread(worker);
// addWorker()方法中启动
thread.start();
```
&emsp; Worker继承自AQS，这里使用了AQS的独占模式，有个技巧是构造Worker的时候，把AQS的资源(状态)通过setState(-1)设置为-1，这是因为Worker实例刚创建时AQS中state的默认值为0，此时线程尚未启动，不能在这个时候进行线程中断，见Worker#interruptIfStarted()方法。Worker中两个覆盖AQS的方法tryAcquire()和tryRelease()都没有判断外部传入的变量，前者直接CAS(0,1)，后者直接setState(0)。  

### 1.3.4. runWorker()，线程复用机制  

&emsp; 线程复用原理：  
&emsp; 线程池将线程和任务进行解耦，线程是线程，任务是任务，摆脱了之前通过 Thread 创建线程时的一个线程必须对应一个任务的限制。  
&emsp; 在线程池中，同一个线程可以从阻塞队列中不断获取新任务来执行，其核心原理在于线程池对 Thread 进行了封装（内部类Worker），并不是每次执行任务都会调用 Thread.start() 来创建新线程，而是让每个线程去执行一个“循环任务”，在这个“循环任务”中不停的检查是否有任务需要被执行，如果有则直接执行，也就是调用任务中的 run 方法，将 run 方法当成一个普通的方法执行，通过这种方式将只使用固定的线程就将所有任务的 run 方法串联起来。  

-----
&emsp; **<font color = "clime">worker中的线程start 后，执行的是worker的run()方法，而run()方法会调用ThreadPoolExecutor类的runWorker()方法，runWorker()方法实现了线程池中的线程复用机制。</font>** runWorker()方法的核心流程：  
1. Worker先执行一次解锁操作，用于解除不可中断状态。  
2. <font color = "red">通过while循环调用getTask()方法从任务队列中获取任务（当然，首轮循环也有可能是外部传入的firstTask任务实例）。</font>  
3. 如果线程池更变为STOP状态，则需要确保工作线程是中断状态并且进行中断处理，否则要保证工作线程必须不是中断状态。  
&emsp; 执行任务实例Runnale#run()方法，任务实例执行之前和之后（包括正常执行完毕和异常执行情况）分别会调用钩子方法beforeExecute()和afterExecute()。    
4. while循环跳出意味着runWorker()方法结束和工作线程生命周期结束（Worker#run()生命周期完结），会调用processWorkerExit()处理工作线程退出的后续工作。  

<!-- 

-->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-16.png)  

&emsp; **从runWorker()方法的实现可以看出，runWorker()方法中主要调用了getTask()方法(从任务队列中获取任务)和processWorkerExit()方法(处理线程退出的后续工作)。**  

```java
final void runWorker(Worker w) {
    // 获取当前线程，实际上和Worker持有的线程实例是相同的
    Thread wt = Thread.currentThread();
    // 获取Worker中持有的初始化时传入的任务对象，这里注意存放在临时变量task中
    Runnable task = w.firstTask;
    // 设置Worker中持有的初始化时传入的任务对象为null
    w.firstTask = null;
    // 由于Worker初始化时AQS中state设置为-1，这里要先做一次解锁把state更新为0，允许线程中断
    w.unlock(); // allow interrupts
    // 记录线程是否因为用户异常终结，默认是true
    boolean completedAbruptly = true;
    try {
        // 初始化任务对象不为null，或者从任务队列获取任务不为空(从任务队列获取到的任务会更新到临时变量task中)
        // getTask()由于使用了阻塞队列，这个while循环如果命中后半段会处于阻塞或者超时阻塞状态，getTask()返回为null会导致线程跳出死循环使线程终结
        while (task != null || (task = getTask()) != null) {
            // Worker加锁，本质是AQS获取资源并且尝试CAS更新state由0更变为1
            w.lock();
            // If pool is stopping, ensure thread is interrupted;
            // if not, ensure thread is not interrupted.  This
            // requires a recheck in second case to deal with
            // shutdownNow race while clearing interrupt
            // 如果线程池正在停止(也就是由RUNNING或者SHUTDOWN状态向STOP状态变更)，那么要确保当前工作线程是中断状态
            // 否则，要保证当前线程不是中断状态
            if ((runStateAtLeast(ctl.get(), STOP) ||
                    (Thread.interrupted() &&
                    runStateAtLeast(ctl.get(), STOP))) &&
                !wt.isInterrupted())
                wt.interrupt();
            try {
                // 钩子方法，任务执行前
                beforeExecute(wt, task);
                try {
                    // todo 线程启动
                    task.run();
                    // 钩子方法，任务执行后 - 正常情况
                    afterExecute(task, null);
                } catch (Throwable ex) {
                    // 钩子方法，任务执行后 - 异常情况
                    afterExecute(task, ex);
                    throw ex;
                }
            } finally {
                // 清空task临时变量，这个很重要，否则while会死循环执行同一个task。准备通过getTask()获取下一个任务
                task = null;
                // 累加Worker完成的任务数
                w.completedTasks++;
                // Worker解锁，本质是AQS释放资源，设置state为0
                w.unlock();
            }
        }
        // 走到这里说明某一次getTask()返回为null，线程正常退出
        completedAbruptly = false;
    } finally {
        // 处理线程退出，completedAbruptly为true说明由于用户异常导致线程非正常退出
        
        //到这里，线程执行结束，需要执行结束线程的一些清理工作
        //线程执行结束可能有两种情况：
        //1.getTask()返回null，也就是说，这个worker的使命结束了，线程执行结束
        //2.任务执行过程中发生了异常
        //第一种情况，getTask()返回null，那么getTask()中会将workerCount递减
        //第二种情况，workerCount没有进行处理，这个递减操作会在processWorkerExit()中处理
        processWorkerExit(w, completedAbruptly);
    }
}
```

#### 1.3.4.1. getTask() ，线程池如何保证核心线程不被销毁？ 
&emsp; getTask()方法是工作线程在while死循环中获取任务队列中的任务对象的方法。  

&emsp; 线程池如何保证核心线程不被销毁的？设置allowCoreThreadTimeOut(true) 可以使核心线程销毁。    

```java
private Runnable getTask() {
    // 记录上一次从队列中拉取的时候是否超时
    boolean timedOut = false; // Did the last poll() time out?
    // 注意这是死循环
    for (;;) {
        int c = ctl.get();

        // Check if queue empty only if necessary.
        // 第一个if：如果线程池状态至少为SHUTDOWN，也就是rs >= SHUTDOWN(0)，则需要判断两种情况(或逻辑)：
        // 1. 线程池状态至少为STOP(1)，也就是线程池正在停止，一般是调用了shutdownNow()方法
        // 2. 任务队列为空
        // 如果在线程池至少为SHUTDOWN状态并且满足上面两个条件之一，则工作线程数wc减去1，然后直接返回null
        if (runStateAtLeast(c, SHUTDOWN)
            && (runStateAtLeast(c, STOP) || workQueue.isEmpty())) {
            decrementWorkerCount();
            return null;
        }
        // 跑到这里说明线程池还处于RUNNING状态，重新获取一次工作线程数
        int wc = workerCountOf(c);

        // Are workers subject to culling?
        // timed临时变量勇于线程超时控制，决定是否需要通过poll()此带超时的非阻塞方法进行任务队列的任务拉取
        // 1.allowCoreThreadTimeOut默认值为false，如果设置为true，则允许核心线程也能通过poll()方法从任务队列中拉取任务
        // 2.工作线程数大于核心线程数的时候，说明线程池中创建了额外的非核心线程，这些非核心线程一定是通过poll()方法从任务队列中拉取任务
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
        // 第二个if：
        // 1.wc > maximumPoolSize说明当前的工作线程总数大于maximumPoolSize，说明了通过setMaximumPoolSize()方法减少了线程池容量
        // 或者 2.timed && timedOut说明了线程命中了超时控制并且上一轮循环通过poll()方法从任务队列中拉取任务为null
        // 并且 3. 工作线程总数大于1或者任务队列为空，则通过CAS把线程数减去1，同时返回null，
        // CAS把线程数减去1失败会进入下一轮循环做重试
        if ((wc > maximumPoolSize || (timed && timedOut))
            && (wc > 1 || workQueue.isEmpty())) {
            if (compareAndDecrementWorkerCount(c))
                return null;
            continue;
        }

        try {
            // 如果timed为true，通过poll()方法做超时拉取，keepAliveTime时间内没有等待到有效的任务，则返回null
            // 如果timed为false，通过take()做阻塞拉取，会阻塞到有下一个有效的任务时候再返回(一般不会是null)
            Runnable r = timed ?
                workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                workQueue.take();
            // 这里很重要，只有非null时候才返回，null的情况下会进入下一轮循环
            if (r != null)
                return r;
            // 跑到这里说明上一次从任务队列中获取到的任务为null，一般是workQueue.poll()方法超时返回null
            timedOut = true;
        } catch (InterruptedException retry) {
            timedOut = false;
        }
    }
}
```
&emsp; 这个方法中，有两处十分庞大的if逻辑，对于第一处if可能导致工作线程数减去1直接返回null的场景有：  
1. 线程池状态为SHUTDOWN，一般是调用了shutdown()方法，并且任务队列为空。  
2. 线程池状态为STOP。  

&emsp; 对于第二处if，逻辑有点复杂，先拆解一下：  

```java
// 工作线程总数大于maximumPoolSize，说明了通过setMaximumPoolSize()方法减少了线程池容量
boolean b1 = wc > maximumPoolSize;
// 允许线程超时同时上一轮通过poll()方法从任务队列中拉取任务为null
boolean b2 = timed && timedOut;
// 工作线程总数大于1
boolean b3 = wc > 1;
// 任务队列为空
boolean b4 = workQueue.isEmpty();
boolean r = (b1 || b2) && (b3 || b4);
if (r) {
    if (compareAndDecrementWorkerCount(c)){
        return null;
    }else{
        continue;
    }
}
```
&emsp; 这段逻辑大多数情况下是针对非核心线程。在execute()方法中，当线程池总数已经超过了corePoolSize并且还小于maximumPoolSize时，当任务队列已经满了的时候，会通过addWorker(task,false)添加非核心线程。而这里的逻辑恰好类似于addWorker(task,false)的反向操作，用于减少非核心线程，使得工作线程总数趋向于corePoolSize。如果对于非核心线程，上一轮循环获取任务对象为null，这一轮循环很容易满足timed && timedOut为true，这个时候getTask()返回null会导致Worker#runWorker()方法跳出死循环，之后执行processWorkerExit()方法处理后续工作，而该非核心线程对应的Worker则变成“游离对象”，等待被JVM回收。当allowCoreThreadTimeOut设置为true的时候，这里分析的非核心线程的生命周期终结逻辑同时会适用于核心线程。那么可以总结出keepAliveTime的意义：  

* 当允许核心线程超时，也就是allowCoreThreadTimeOut设置为true的时候，此时keepAliveTime表示空闲的工作线程的存活周期。  
* 默认情况下不允许核心线程超时，此时keepAliveTime表示空闲的非核心线程的存活周期。  

&emsp; 在一些特定的场景下，配置合理的keepAliveTime能够更好地利用线程池的工作线程资源。

#### 1.3.4.2. processWorkerExit()  
&emsp; processWorkerExit()方法是为将要终结的Worker做一次清理和数据记录工作（因为processWorkerExit()方法也包裹在runWorker()方法finally代码块中，其实工作线程在执行完processWorkerExit()方法才算真正的终结）。 

```java
private void processWorkerExit(Worker w, boolean completedAbruptly) {
    // 因为抛出用户异常导致线程终结，直接使工作线程数减1即可
    // 如果没有任何异常抛出的情况下是通过getTask()返回null引导线程正常跳出runWorker()方法的while死循环从而正常终结，这种情况下，在getTask()中已经把线程数减1
    if (completedAbruptly) // If abrupt, then workerCount wasn't adjusted
        decrementWorkerCount();

    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        // 全局的已完成任务记录数加上此将要终结的Worker中的已完成任务数
        completedTaskCount += w.completedTasks;
        // 工作线程集合中移除此将要终结的Worker
        workers.remove(w);
    } finally {
        mainLock.unlock();
    }
     
    // 见下一小节分析，用于根据当前线程池的状态判断是否需要进行线程池terminate处理
    tryTerminate();

    int c = ctl.get();
    // 如果线程池的状态小于STOP，也就是处于RUNNING或者SHUTDOWN状态的前提下：
    // 1.如果线程不是由于抛出用户异常终结，如果允许核心线程超时，则保持线程池中至少存在一个工作线程
    // 2.如果线程由于抛出用户异常终结，或者当前工作线程数，那么直接添加一个新的非核心线程
    if (runStateLessThan(c, STOP)) {
        if (!completedAbruptly) {
            // 如果允许核心线程超时，最小值为0，否则为corePoolSize
            int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
            // 如果最小值为0，同时任务队列不空，则更新最小值为1
            if (min == 0 && ! workQueue.isEmpty())
                min = 1;
            // 工作线程数大于等于最小值，直接返回不新增非核心线程
            if (workerCountOf(c) >= min)
                return; // replacement not needed
        }
        addWorker(null, false);
    }
}
```
&emsp; 代码的后面部分区域，会判断线程池的状态，如果线程池是RUNNING或者SHUTDOWN状态的前提下，如果当前的工作线程由于抛出用户异常被终结，那么会新创建一个非核心线程。如果当前的工作线程并不是抛出用户异常被终结(正常情况下的终结)，那么会这样处理：

* allowCoreThreadTimeOut为true，也就是允许核心线程超时的前提下，如果任务队列空，则会通过创建一个非核心线程保持线程池中至少有一个工作线程。  
* allowCoreThreadTimeOut为false，如果工作线程总数大于corePoolSize则直接返回，否则创建一个非核心线程，也就是会趋向于保持线程池中的工作线程数量趋向于corePoolSize。  

&emsp; processWorkerExit()执行完毕之后，意味着该工作线程的生命周期已经完结。  

##### 1.3.4.2.1. tryTerminate()  
&emsp; 每个工作线程终结的时候都会调用tryTerminate()方法：  

```java
final void tryTerminate() {
    for (;;) {
        int c = ctl.get();
        // 判断线程池的状态，如果是下面三种情况下的任意一种则直接返回：
        // 1.线程池处于RUNNING状态
        // 2.线程池至少为TIDYING状态，也就是TIDYING或者TERMINATED状态，意味着已经走到了下面的步骤，线程池即将终结
        // 3.线程池至少为STOP状态并且任务队列不为空
        if (isRunning(c) ||
            runStateAtLeast(c, TIDYING) ||
            (runStateLessThan(c, STOP) && ! workQueue.isEmpty()))
            return;
        // 工作线程数不为0，则中断工作线程集合中的第一个空闲的工作线程
        if (workerCountOf(c) != 0) { // Eligible to terminate
            interruptIdleWorkers(ONLY_ONE);
            return;
        }

        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // CAS设置线程池状态为TIDYING，如果设置成功则执行钩子方法terminated()
            if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                try {
                    terminated();
                } finally {
                    // 最后更新线程池状态为TERMINATED
                    ctl.set(ctlOf(TERMINATED, 0));
                    // 唤醒阻塞在termination条件的所有线程，这个变量的await()方法在awaitTermination()中调用
                    termination.signalAll();
                }
                return;
            }
        } finally {
            mainLock.unlock();
        }
        // else retry on failed CAS
    }
}

// 中断空闲的工作线程，onlyOne为true的时候，只会中断工作线程集合中的某一个线程
private void interruptIdleWorkers(boolean onlyOne) {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        for (Worker w : workers) {
            Thread t = w.thread;
            // 这里判断线程不是中断状态并且尝试获取锁成功的时候才进行线程中断
            if (!t.isInterrupted() && w.tryLock()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                } finally {
                    w.unlock();
                }
            }
            // 这里跳出循环，也就是只中断集合中第一个工作线程
            if (onlyOne)
                break;
        }
    } finally {
        mainLock.unlock();
    }
}
```
&emsp; tryTerminate()方法的第二个if代码逻辑：工作线程数不为0，则中断工作线程集合中的第一个空闲的工作线程。方法API注释中有这样一段话：  

    If otherwise eligible to terminate but workerCount is nonzero, interrupts an idle worker to ensure that shutdown signals propagate.
    当满足终结线程池的条件但是工作线程数不为0，这个时候需要中断一个空闲的工作线程去确保线程池关闭的信号得以传播。
