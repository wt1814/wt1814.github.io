
<!-- TOC -->

- [1. ThreadPoolExecutor-1](#1-threadpoolexecutor-1)
    - [1.1. 属性](#11-属性)
    - [1.2. 构造函数](#12-构造函数)
    - [1.3. ※※※线程池工作流程（execute成员方法的源码）](#13-※※※线程池工作流程execute成员方法的源码)

<!-- /TOC -->


# 1. ThreadPoolExecutor-1
<!--
https://mp.weixin.qq.com/s/0OsdfR3nmZTETw4p6B1dSA
https://mp.weixin.qq.com/s/why85dCrH8ej2wPA08e3nA
https://mp.weixin.qq.com/s/b9zF6jcZQn6wdjzo8C-TmA
 深入分析线程池的实现原理 
https://mp.weixin.qq.com/s/L4u374rmxEq9vGMqJrIcvw
-->

## 1.1. 属性  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-3.png) 
 
&emsp; **<font color = "red">线程池存在5种状态：</font>**  

* RUNNING：在这个状态的线程池能判断接收新提交的任务，并且也能处理阻塞队列中的任务。  
* SHUTDOWN：处于关闭的状态，该线程池不能接收新提交的任务，但是可以处理阻塞队列中已经保存的任务，在线程处于RUNNING状态，调用shutdown()方法能切换为该状态。  
* STOP：线程池处于该状态时既不能接收新的任务也不能处理阻塞队列中的任务，并且能中断现在线程中的任务。当线程处于RUNNING和SHUTDOWN状态，调用shutdownNow()方法就可以使线程变为该状态。  
* TIDYING：在SHUTDOWN状态下阻塞队列为空，且线程中的工作线程数量为0就会进入该状态，当在STOP状态下时，只要线程中的工作线程数量为0就会进入该状态。  
* TERMINATED：在TIDYING状态下调用terminated()方法就会进入该状态。可以认为该状态是最终的终止状态。  

&emsp; 线程池状态切换图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-4.png)  

## 1.2. 构造函数  
&emsp; 在ThreadPoolExecutor类中提供了四个构造方法：   

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
&emsp; ThreadPoolExecutor继承了AbstractExecutorService类，并提供了四个构造器。前面三个构造器都是调用的第四个构造器进行的初始化工作。下面解释一下构造器中各个参数的含义：  

* int  corePoolSize：线程池的核心线程数大小。默认情况下，在创建了线程池后，线程池中的线程数为0，当有任务来之后，就会创建一个线程去执行任务，当线程池中的线程数目达到corePoolSize后，就会把到达的任务放到缓存队列当中。默认情况下可以一直存活。可以通过设置allowCoreThreadTimeOut为True，此时核心线程数就是0，此时keepAliveTime控制所有线程的超时时间。  
* int  maximumPoolSize：线程池允许的最大线程数大小。当workQueue满了，不能添加任务的时候，这个参数才会生效。  
* long  keepAliveTime：空闲线程（超出corePoolSize的线程）的生存时间。这些线程如果长时间没有执行任务，并且超过了keepAliveTime设定的时间，就会消亡。  
* TimeUnit  unit：参数keepAliveTime的单位。有7种取值，在TimeUnit类中有7种静态属性：TimeUnit.DAYS；TimeUnit.HOURS；  
* BlockingQueue<Runnable\>  workQueue：任务阻塞队列，是java.util.concurrent下的主要用来控制线程同步的工具。如果BlockQueue是空的，从BlockingQueue取东西的操作将会被阻断进入等待状态，直到BlockingQueue进了东西才会被唤醒。同样,如果BlockingQueue是满的,任何试图往里存东西的操作也会被阻断进入等待状态,直到BlockingQueue里有空间才会被唤醒继续操作。具体的实现类有LinkedBlockingQueue,ArrayBlockingQueued等。一般其内部的都是通过Lock和Condition(显示锁Lock及Condition的学习与使用)来实现阻塞和唤醒。  
* ThreadFactory threadFactory：创建线程的工厂。  
* RejectedExecutionHandler  handler：<font color = "red">当提交任务数超过maxmumPoolSize+workQueue之和时，任务会交给RejectedExecutionHandler来处理，执行拒绝策略。</font>有四种策略，默认是AbortPolicy。内置拒绝策略均实现了RejectedExecutionHandler接口，若以下策略仍无法满足实际需要，可以扩展RejectedExecutionHandler接口。  

    | 名称 | Condition |  
    |----|----|  
    |AbortPolicy (默认)|丢弃任务并抛出RejectedExecutionException异常。| 
    |CallerRunsPolicy|在线程池当前正在运行的Thread线程池中处理被拒绝的任务。主线程会被阻塞，其余任务只能在被拒绝的任务执行完之后才会继续被提交到线程池执行。|
    |DiscardOldestPolicy|丢弃队列最前面的任务，将被拒绝的任务添加到等待队列中。|
    |DiscardPolicy|丢弃任务，但是不抛出异常。|

## 1.3. ※※※线程池工作流程（execute成员方法的源码）
1. 线程池刚创建时，里面没有一个线程。任务队列是作为参数传进来的。不过，就算队列里面有任务，线程池也不会马上执行它们，而是创建线程。当一个线程完成任务时，它会从队列中取下一个任务来执行。  
2. 当调用execute()方法添加一个任务时，线程池会做如下判断：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-5.png)  
	1. 如果线程池中的线程数量少于corePoolSize(核心线程数量)，那么会直接开启一个新的核心线程来执行任务，即使此时有空闲线程存在。   
	2. 如果线程池中线程数量大于等于corePoolSize(核心线程数量)，那么任务会被插入到任务队列中排队，等待被执行。此时并不添加新的线程。如果是无界队列，则线程大小一直会是核心线程池的大小。   
	3. 如果在步骤2中由于任务队列已满导致无法将新任务进行排队，这个时候有两种情况：  
        &emsp; 线程数量[未]达到maximumPoolSize(线程池最大线程数)，立刻启动一个非核心线程来执行任务。  
        &emsp; 线程数量[已]达到maximumPoolSize(线程池最大线程数)，将会执行拒绝策略。  
3. 当一个线程空闲，超过一定的时间（keepAliveTime）时，线程池会判断，如果当前运行的线程数大于corePoolSize，那么这个线程就被停掉。所以线程池的所有任务完成后，它最终会收缩到corePoolSize的大小。  