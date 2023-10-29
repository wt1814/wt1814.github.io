
<!-- TOC -->

- [1. 并发编程](#1-并发编程)
    - [1.1. 线程池-多线程](#11-线程池-多线程)
        - [1.1.1. 线程Thread](#111-线程thread)
            - [1.1.1.1. 线程状态详解](#1111-线程状态详解)
        - [1.1.2. 线程池框架](#112-线程池框架)
        - [1.1.3. ThreadPoolExecutor详解](#113-threadpoolexecutor详解)
        - [1.1.4. 线程池的正确使用](#114-线程池的正确使用)
        - [1.1.5. ForkJoinPool详解](#115-forkjoinpool详解)
        - [1.1.6. ~~CompletionService~~](#116-completionservice)
        - [1.1.7. Future相关](#117-future相关)
        - [1.1.8. ~~CompletableFuture~~](#118-completablefuture)
    - [1.2. 并发编程](#12-并发编程)
        - [1.2.1. 并发编程原理](#121-并发编程原理)
            - [1.2.1.1. ~~CPU多核缓存架构及JMM~~](#1211-cpu多核缓存架构及jmm)
            - [1.2.1.2. 并发安全问题产生原因](#1212-并发安全问题产生原因)
            - [1.2.1.3. 硬件解决并发安全](#1213-硬件解决并发安全)
            - [1.2.1.4. Java解决并发安全](#1214-java解决并发安全)
        - [1.2.2. 并发安全解决](#122-并发安全解决)
            - [1.2.2.1. 线程安全解决方案](#1221-线程安全解决方案)
            - [1.2.2.2. Synchronized](#1222-synchronized)
                - [1.2.2.2.1. Synchronized介绍](#12221-synchronized介绍)
                - [1.2.2.2.2. Synchronized使用](#12222-synchronized使用)
            - [1.2.2.3. ~~Synchronized使用是否安全~~](#1223-synchronized使用是否安全)
                - [1.2.2.3.1. Synchronized底层原理](#12231-synchronized底层原理)
                - [1.2.2.3.2. Synchronized优化](#12232-synchronized优化)
            - [1.2.2.4. Volatile](#1224-volatile)
            - [1.2.2.5. ThreadLocal](#1225-threadlocal)
                - [1.2.2.5.1. ThreadLocal原理](#12251-threadlocal原理)
                - [1.2.2.5.2. ThreadLocal应用](#12252-threadlocal应用)
        - [1.2.3. 线程通信(生产者消费者问题)](#123-线程通信生产者消费者问题)
        - [1.2.4. 线程活跃性](#124-线程活跃性)
    - [1.3. JUC](#13-juc)
        - [1.3.1. CAS](#131-cas)
        - [1.3.2. AQS](#132-aqs)
            - [1.3.2.1. LockSupport类](#1321-locksupport类)
        - [1.3.3. LOCK](#133-lock)
            - [1.3.3.1. ReentrantLock，重入锁](#1331-reentrantlock重入锁)
                - [1.3.3.1.1. 读写锁](#13311-读写锁)
        - [1.3.4. Atomic](#134-atomic)
            - [1.3.4.1. AtomicStampedReference与AtomicMarkableReference](#1341-atomicstampedreference与atomicmarkablereference)
            - [1.3.4.2. LongAdder](#1342-longadder)
        - [1.3.5. Collections](#135-collections)
            - [1.3.5.1. CopyOnWriteArrayList](#1351-copyonwritearraylist)
            - [1.3.5.2. ConcurrentHashMap](#1352-concurrenthashmap)
            - [1.3.5.3. BlockingQueue](#1353-blockingqueue)
        - [1.3.6. tools](#136-tools)
            - [1.3.6.1. CountDownLatch](#1361-countdownlatch)
            - [1.3.6.2. CyclicBarrier](#1362-cyclicbarrier)
            - [1.3.6.3. Semaphore](#1363-semaphore)

<!-- /TOC -->


# 1. 并发编程 

## 1.1. 线程池-多线程

### 1.1.1. 线程Thread
1. 创建线程的方式：Thread、Runnable、Callable、线程池相关（Future, ThreadPool, `@Async`）...  
2. 线程状态 
3. thread.yield()，线程让步     
&emsp; yield会使当前线程让出CPU执行时间片，与其他线程一起重新竞争CPU时间片。  
4. thread.join()，线程加入  
&emsp; 把指定的线程加入到当前线程，可以将两个交替执行的线程合并为顺序执行的线程。比如在线程B中调用了线程A的Join()方法，直到线程A执行完毕后，才会继续执行线程B。  
5. thread.interrupt()，线程中断（将线程做特殊标记的动作）  
    &emsp; thread.interrupt()用来中断线程，即将线程的中断状态位设置为true，`注意中断操作并不会终止线程，`不像stop()会立即终止一个运行中的线程，`中断仅仅是将线程中断位设置为true（默认false）`。线程会不断的检查中断位，如果线程处于阻塞状态（sleep、join、wait）且中断，就会抛出InterreptException来唤醒线程，交由应用程序处理；如果线程未阻塞且中断，也要交由应用程序处理；`是终止线程，还是继续执行需要根据实际情况做出合理的响应。`  
    &emsp; **<font color = "red">线程在不同状态下对于中断所产生的反应：</font>**    
    * NEW和TERMINATED对于中断操作几乎是屏蔽的；  
    * RUNNABLE和BLOCKED类似， **<font color = "cclime">对于中断操作只是设置中断标志位并没有强制终止线程，对于线程的终止权利依然在程序手中；</font>**  
    * WAITING/TIMED_WAITING状态下的线程对于中断操作是敏感的，它们会抛出异常并清空中断标志位。  

#### 1.1.1.1. 线程状态详解
1. 通用的线程周期。操作系统层面有5个状态，分别是：New（新建）、Runnable（就绪）、Running（运行）、Blocked（阻塞）、Dead（死亡）。  
2. Java线程状态均来自Thread类下的State这一内部枚举类中所定义的状态：  
![image](http://182.92.69.8:8081/img/java/concurrent/thread-2.png)  
&emsp; 线程状态切换图示：  
![image](http://182.92.69.8:8081/img/java/concurrent/thread-5.png) 
&emsp; ⚠️⚠️⚠️`对象` `执行动作` 形成`线程`。`影响线程状态的相关java类：Object类、Synchronized关键字、Thread类。`  

-----------


&emsp; `⚠️⚠️⚠️线程的资源有不少，但应该包含CPU资源和锁资源这两类。`  
&emsp; **<font color = "clime">只有runnable到running时才会占用cpu时间片，其他都会出让cpu时间片。</font>**  

* sleep(long mills)：让出CPU资源，但是不会释放锁资源。  
* wait()：让出CPU资源和锁资源。  

&emsp; 锁是用来线程同步的，sleep(long mills)虽然让出了CPU，但是不会让出锁，其他线程可以利用CPU时间片了，但如果其他线程要获取sleep(long mills)拥有的锁才能执行，则会因为无法获取锁而不能执行，继续等待。  
&emsp; 但是那些没有和sleep(long mills)竞争锁的线程，一旦得到CPU时间片即可运行了。  

1. 新建状态（NEW）：  
    1. 一个尚未启动的线程处于这一状态。用new语句创建的线程处于新建状态，此时它和其他Java对象一样，仅仅在堆区中被分配了内存，并初始化其成员变量的值。  
    2. 操作  
        * new Thread()
2. 就绪状态（Runnable）：  
    1. 当一个线程对象创建后，其他线程调用它的start()方法，该线程就进入就绪状态，Java虚拟机会为它创建方法调用栈和程序计数器。处于这个状态的线程位于可运行池中，等待获得CPU的使用权。<!-- Runnable (可运行/运行状态，等待CPU的调度)(要注意：即使是正在运行的线程，状态也是Runnable，而不是Running) -->  
    2. 操作  
        * 处于阻塞的线程：obj.notify()唤醒线程； obj.notifyAll()唤醒线程； 
        * 处于等待的线程：obj.wait(time)，thread.join(time)等待时间time耗尽。
        * 被synchronized标记的代码，获取到同步监视器。  
        * 调用了thread.start()启动线程；
3. **<font color = "red">阻塞状态（BLOCKED）：</font>**  
    1. **<font color = "clime">阻塞状态是指线程因为某些原因`放弃CPU`，暂时停止运行。</font>** 当线程处于阻塞状态时，Java虚拟机不会给线程分配CPU。直到线程重新进入就绪状态（获取监视器锁），它才有机会转到运行状态。  
    2. 操作  
        * **等待阻塞（o.wait->等待对列）：运行的线程执行wait()方法，JVM会把该线程放入等待池中。(wait会释放持有的锁)**
        * **同步阻塞（lock->锁池）：运行的线程在获取对象的同步锁时，若该同步锁被别的线程占用，则JVM会把该线程放入锁池(lock pool)中。**
        * **其他阻塞状态（sleep/join）：当前线程执行了sleep()方法，或者调用了其他线程的join()方法，或者发出了I/O请求时，就会进入这个状态。**
4. **<font color = "red">等待状态（WAITING）：</font>**  
    1. **<font color = "clime">一个正在无限期等待另一个线程执行一个特别的动作的线程处于这一状态。</font>**  
    2. 操作  
        * obj.wait() 释放同步监视器obj，并进入阻塞状态。  
        * threadA中调用threadB.join()，threadA将Waiting，直到threadB终止。
    3. 阻塞和等待的区别：  
5. <font color = "red">计时等待（TIMED_WAITING）：</font>  
    1. 一个正在限时等待另一个线程执行一个动作的线程处于这一状态。  
    2. 操作
        * obj.wait(time)
        * thread.sleep(time)； threadA中调用threadB.join(time)
6. 终止状态（TERMINATED）：  
    &emsp; 一个已经退出的线程处于这一状态。线程会以下面三种方式结束，结束后就是死亡状态。
    * 正常结束：run()或 call()方法执行完成，线程正常结束。
    * 异常结束：线程抛出一个未捕获的Exception或Error。
    * 调用stop：直接调用该线程的stop()方法来结束该线程—该方法通常容易导致死锁，不推荐使用。
7. 注意：由于wait()/wait(time)导致线程处于Waiting/TimedWaiting状态，当线程被notify()/notifyAll()/wait等待时间到之后，如果没有获取到同步监视器。会直接进入Blocked阻塞状态。  
8. 线程状态切换示意图：  
![image](http://182.92.69.8:8081/img/java/concurrent/thread-6.png) 


### 1.1.2. 线程池框架
1. **线程池通过线程复用机制，并对线程进行统一管理，** 具有以下优点：  
    * 降低系统资源消耗。通过复用已存在的线程，降低线程创建和销毁造成的消耗；  
    * 提高响应速度。当有任务到达时，无需等待新线程的创建便能立即执行；  
    * 提高线程的可管理性。线程是稀缺资源，如果无限制的创建，不仅会消耗大量系统资源，还会降低系统的稳定性，使用线程池可以进行对线程进行统一的分配、调优和监控。  
2. 线程池框架Executor：  
    ![image](http://182.92.69.8:8081/img/java/concurrent/threadPool-21.png)   
    &emsp; Executor：所有线程池的接口。  
    &emsp; ExecutorService：扩展了Executor接口。添加了一些用来管理执行器生命周期和任务生命周期的方法。  
    &emsp; ThreadPoolExecutor（创建线程池方式一）：线程池的具体实现类。  
    &emsp; Executors（创建线程池方式二）：提供了一系列静态的工厂方法用于创建线程池，返回的线程池都实现了ExecutorService接口。  
    
    &emsp; Executor框架由三个部分组成：  

    * 工作任务：Runnable/Callable接口
        * 工作任务就是Runnable/Callable接口的实现，可以被线程池执行。
    * **<font color = "red">执行机制（创建线程池的分类）：</font>** Executor接口、ExecutorService接口、ScheduledExecutorService接口
        * ThreadPoolExecutor 是最核心的线程池实现，用来执行被提交的任务。
        * ScheduledThreadPoolExecutor是任务调度的线程池实现，可以在给定的延迟后运行命令，或者定期执行命令（它比Timer更灵活）。  
        * ForkJoinPool是一个并发执行框架。
    * 异步计算的结果：Future接口
        * 实现Future接口的FutureTask类，代表异步计算的结果。  
3. 线程池执行，ExecutorService的API：execute()，提交不需要返回值的任务；`submit()，提交需要返回值的任务，返回值类型是Future`。   
4. **<font color = "clime">Executors返回线程池对象的弊端如下：</font>**  
	* SingleThreadExecutor（单线程）和FixedThreadPool（定长线程池，可控制线程最大并发数）：允许请求的队列长度为Integer.MAX_VALUE，可能堆积大量的请求，从而导致OOM。  
	* CachedThreadPool和ScheduledThreadPool：允许创建的线程数量为Integer.MAX_VALUE，可能会创建大量线程，从而导致OOM。   

### 1.1.3. ThreadPoolExecutor详解
1. 理解构造函数中参数：核心线程数大小、最大线程数大小、空闲线程（超出corePoolSize的线程）的生存时间、参数keepAliveTime的单位、任务阻塞队列、创建线程的工厂（可以通过这个工厂来创建有业务意义的线程名字）。  
    * [阻塞队列](/docs/java/concurrent/BlockingQueue.md)，线程池所使用的缓冲队列，常用的是：SynchronousQueue、ArrayBlockingQueue、LinkedBlockingQueue。   
    * 拒绝策略，默认AbortPolicy（拒绝任务，抛异常）， **<font color = "clime">可以选用CallerRunsPolicy（任务队列满时，不进入线程池，由主线程执行）。</font>**  
2. 线程池中核心方法调用链路：  
    ![image](http://182.92.69.8:8081/img/java/concurrent/threadPool-17.png)  
    ![image](http://182.92.69.8:8081/img/java/concurrent/threadPool-14.png)  
    ![image](http://182.92.69.8:8081/img/java/concurrent/threadPool-20.png)  
    &emsp; 线程运行流程：查看execute方法。  
    &emsp; <font color = "clime">线程池创建时`没有设置成预启动加载`，首发线程数为0。</font><font color = "red">任务队列是作为参数传进来的。即使队列里面有任务，线程池也不会马上执行它们，而是创建线程。</font>当一个线程完成任务时，它会从队列中取下一个任务来执行。当调用execute()方法添加一个任务时，线程池会做如下判断：  
    1. 如果当前工作线程总数小于corePoolSize，则直接创建核心线程执行任务（任务实例会传入直接用于构造工作线程实例）。  
    2. 如果当前工作线程总数大于等于corePoolSize，判断线程池是否处于运行中状态，同时尝试用非阻塞方法向任务队列放入任务，这里会二次检查线程池运行状态，如果当前工作线程数量为0，则创建一个非核心线程并且传入的任务对象为null。  
    3. 如果向任务队列投放任务失败（任务队列已经满了），则会尝试创建非核心线程传入任务实例执行。  
    4. 如果创建非核心线程失败，此时需要拒绝执行任务，调用拒绝策略处理任务。  
3. 线程复用机制：    
&emsp; **线程池将线程和任务进行解耦，线程是线程，任务是任务，摆脱了之前通过Thread创建线程时的一个线程必须对应一个任务的限制。**  
&emsp; **<font color = "red">在线程池中，同一个线程可以从阻塞队列中不断获取新任务来执行，其核心原理在于线程池对Thread进行了封装（内部类Worker），并不是每次执行任务都会调用Thread.start() 来创建新线程，而是让每个线程去执行一个“循环任务”，在这个“循环任务”中不停的检查是否有任务需要被执行。</font>** 如果有则直接执行，也就是调用任务中的run方法，将run方法当成一个普通的方法执行，通过这种方式将只使用固定的线程就将所有任务的run方法串联起来。  
&emsp; 源码解析：`runWorker()方法中，有任务时，while (task != null || (task = getTask()) != null) 循环获取；没有任务时，清除空闲线程。`  
4. 线程池保证核心线程不被销毁？  
    &emsp; `ThreadPoolExecutor【回收线程都是等【while死循环】里getTask()获取不到任务，返回null时，】调用processWorkerExit方法从Set集合中remove掉线程。`  
    1. getTask()返回null又分为2两种场景：  
        1. 线程正常执行完任务，`并且已经等到超过keepAliveTime时间，大于核心线程数，那么会返回null`，结束外层的runWorker中的while循环。
        2. 当调用shutdown()方法，会将线程池状态置为shutdown，并且需要等待正在执行的任务执行完，阻塞队列中的任务执行完才能返回null。
    2. `getTask()不返回null的情况有获取到任务，或获取不到任务，但线程数小于等于核心线程数。`  

### 1.1.4. 线程池的正确使用
1. **<font color = "clime">线程池设置：</font>**   
    1. `使用自定义的线程池。`共享的问题在于会干扰，如果有一些异步操作的平均耗时是1秒，另外一些是100秒，这些操作放在一起共享一个线程池很可能会出现相互影响甚至饿死的问题。`建议根据异步业务类型，合理设置隔离的线程池。`  
    2. `确定线程池的大小（CPU可同时处理线程数量大部分是CPU核数的两倍）`  
        1. 线程数设置，`建议核心线程数core与最大线程数max一致`  
            &emsp; `CPU密集型`的意思就是该任务需要`大量运算`，而没有阻塞，CPU一直全速运行。IO密集型，即该任务需要大量的IO，即大量的阻塞。  
            * 如果是CPU密集型应用（多线程处理复杂算法），则线程池大小设置为N+1，减少线程切换。
            * 如果是IO密集型（网络IO/磁盘IO）应用（多线程用于数据库数据交互、文件上传下载、网络数据传输等），则线程池大小设置为2N。
            * 如果是混合型，将任务分为CPU密集型和IO密集型，然后分别使用不同的线程池去处理，从而使每个线程池可以根据各自的工作负载来调整。  
        2. 阻塞队列设置  
        &emsp; `线程池的任务队列本来起缓冲作用，`但是如果设置的不合理会导致线程池无法扩容至max，这样无法发挥多线程的能力，导致一些服务响应变慢。队列长度要看具体使用场景，取决服务端处理能力以及客户端能容忍的超时时间等。队列长度要根据使用场景设置一个上限值， **<font color = "red">如果响应时间要求较高的系统可以设置为0。</font>**  
        &emsp; `队列大小200或500-1000。`  
    3. `线程池的优雅关闭：`处于SHUTDOWN的状态下的线程池依旧可以调用shutdownNow。所以可以结合shutdown，shutdownNow，awaitTermination更加优雅关闭线程池。  
2. **<font color = "clime">线程池使用：</font>**    
    1. `线程池未处理异常：`
        1. 线程遇到未处理的异常就结束了。ThreadPoolExecutor中将异常传递给afterExecute()方法，而afterExecute()没有做任何处理。这种处理方式能够保证提交的任务抛出了异常不会影响其他任务的执行，同时也不会对用来执行该任务的线程产生任何影响。然而afterExecute()没有做任何处理，所以如果任务抛出了异常，也无法立刻感知到。即使感知到了，也无法查看异常信息。    
        2. `当线程池中线程频繁出现未捕获的异常，那线程的复用率就大大降低了，需要不断地创建新线程。`  
    2. `线程池中线程中异常尽量手动捕获。`  
3. **<font color = "clime">线程池的监控：</font>**  
&emsp; 通过重写线程池的beforeExecute、afterExecute和shutdown等方式就可以实现对线程的监控。  
4. @Async方法没有执行的问题分析：  
&emsp; @Async异步方法默认使用Spring创建ThreadPoolTaskExecutor(参考TaskExecutionAutoConfiguration)，其中默认核心线程数为8，默认最大队列和默认最大线程数都是Integer.MAX_VALUE，队列使用LinkedBlockingQueue，容量是：Integet.MAX_VALUE，空闲线程保留时间：60s，线程池拒绝策略：AbortPolicy。创建新线程的条件是队列填满时，而这样的配置队列永远不会填满，如果有@Async注解标注的方法长期占用线程（比如HTTP长连接等待获取结果），在核心8个线程数占用满了之后，新的调用就会进入队列，外部表现为没有执行。  

### 1.1.5. ForkJoinPool详解
1. <font color = "clime">ForkJoinPool的两大核心是 分而治之和工作窃取 算法。</font>  
2. 分而治之：<font color = "red">ForkJoinPool的计算方式是大任务拆中任务，中任务拆小任务，最后再汇总。</font>  
3. 工作窃取算法  
&emsp; <font color = "clime">每个工作线程都有自己的工作队列WorkQueue。这是一个双端队列，它是线程私有的。</font>双端队列的操作：push、pop、poll。push/pop只能被队列的所有者线程调用，而poll是由其它线程窃取任务时调用的。  
    1. ForkJoinTask中fork的子任务，将放入运行该任务的工作线程的队头，工作线程将以LIFO的顺序来处理工作队列中的任务；  
    2. **<font color = "clime">`为了最大化地利用CPU，空闲的线程将随机从其它线程的队列中“窃取”任务来执行。从工作队列的尾部窃取任务，以减少竞争；`</font>**  
    3. **<font color = "clime">`当只剩下最后一个任务时，还是会存在竞争，是通过CAS来实现的；`</font>**    


### 1.1.6. ~~CompletionService~~
&emsp; CompletionService 提供了异步任务的执行与结果的封装，轻松实现多线程任务， **<font color = "clime">并方便的集中处理上述任务的结果（且任务最先完成的先返回）。</font>**  
&emsp; CompletionService，内部通过阻塞队列+FutureTask，实现了任务先完成可优先获取到，即结果按照完成先后顺序排序。  

&emsp; java.util.concurrent.CompletionService是对ExecutorService封装的一个增强类，优化了获取异步操作结果的接口。主要解决了Future阻塞的问题。  

```java
private final Executor executor;
private final AbstractExecutorService aes;
private final BlockingQueue<Future<V>> completionQueue;
```

----------------
&emsp; CompletionService 之所以能够做到这点，是因为它没有采取依次遍历 Future 的方式，而是在内部维护了一个保存Future类型的的结果队列，当任务的任务完成后马上将结果放入队列，那么从队列中取到的就是最早完成的结果。  
&emsp; 通过使用BlockingQueue的take或poll方法，则可以得到结果。在BlockingQueue不存在元素时，这两个操作会阻塞，一旦有结果加入，则立即返回。  
&emsp; 如果队列为空，那么 take() 方法会阻塞直到队列中出现结果为止。CompletionService 还提供一个 poll() 方法，返回值与 take() 方法一样，不同之处在于它不会阻塞，如果队列为空则立刻返回 null。这算是给用户多一种选择。  

### 1.1.7. Future相关
1. **Future是一个接口，它可以对具体的Runnable或者Callable任务进行取消、判断任务是否已取消、查询任务是否完成、获取任务结果。**  
2. JDK1.5为Future接口提供了一个实现类FutureTask，表示一个可以取消的异步运算。它有启动和取消运算、查询运算是否完成和取回运算结果等方法。  


### 1.1.8. ~~CompletableFuture~~

&emsp; 为什么引入CompletableFuture？  
&emsp; 对于jdk1.5的Future，虽然提供了异步处理任务的能力，但是获取结果的方式很不优雅，还是需要通过阻塞（或者轮训）的方式。如何避免阻塞呢？其实就是注册回调。  
&emsp; 业界结合观察者模式实现异步回调。也就是当任务执行完成后去通知观察者。比如Netty的ChannelFuture，可以通过注册监听实现异步结果的处理。   


&emsp; CompletableFuture 可以很方便的实现异步任务的封装 **<font color = "clime">并实现结果的联合等一系列操作，</font>** 轻松实现 任务的并行。  

* thenCombine：结合两个CompletionStage的结果，进行转化后返回。  
* applyToEither：两个CompletionStage，谁计算的快，就用那个CompletionStage的结果进行下一步的处理。  
* ...

------------------


&emsp; <font color = "red">从Java 8开始引入了CompletableFuture，它针对Future做了改进，可以传入回调对象，当异步任务完成或者发生异常时，自动调用回调对象的回调方法。</font>  
&emsp; ⚠️注：异步回调，主线程不会阻塞。  
&emsp; CompletableFuture提供了丰富的API对结果进行处理。  

-----------


&emsp; 在Java 8中，新增加了一个包含50个方法左右的类: `CompletableFuture，默认依靠fork/join框架启动新的线程实现异步与并发的，`提供了非常强大的Future的扩展功能，可以帮助我们简化异步编程的复杂性，提供了函数式编程的能力， **<font color = 'clime">可以通过`回调函数`的方式处理返回结果，并且提供了转换和组合CompletableFuture的方法。   
&emsp; 主要是为了解决Future模式的缺点：   
1. Future虽然可以实现异步获取线程的执行结果，但是Future没有提供通知机制，调用方无法得知Future什么时候执行完的问题。  
2. 想要获取Future的结果，要么使用阻塞， 在future.get()的地方等待Future返回结果，这时会变成同步操作。要么使用isDone()方法进行轮询，又会耗费无谓的 CPU 资源。  
3. 从 Java 8 开始引入了CompletableFuture，它针对Future做了改进，可以传入回调对象，当异步任务完成或者发生异常时，自动调用回调对象的回调方法。  

&emsp; **CompletionStage介绍：**    
1. `CompletionStage（完成阶段）`  
    &emsp; CompletionStage: 代表异步任务执行过程中的某一个阶段，一个阶段完成以后可能会触发另外一个阶段  
    &emsp; 一个阶段的执行可以是一个Function，Consumer或者Runnable。比如：  
	```java
    stage.thenApply(x -> square(x))
    	 .thenAccept(x -> System.out.print(x))
    	 .thenRun(() -> System.out.println())
	```
    &emsp; 一个阶段的执行可能是被单个阶段的完成触发，也可能是由多个阶段一起触发  

2. CompletionStage接口实现流式编程  
    &emsp; 此接口包含38个方法、这些方法主要是为了支持函数式编程中流式处理。  


## 1.2. 并发编程
### 1.2.1. 并发编程原理
#### 1.2.1.1. ~~CPU多核缓存架构及JMM~~
&emsp; `⚠⚠⚠声明：并发安全并不是java独有的，其他语言，不同操作系统都存在并发安全。究其原因是因为cpu多级缓存架构。`  
1. CPU多核缓存架构
1. JMM
    1. JMM内存划分：线程对变量的所有操作都必须在 **<font color = "red">工作内存</font>** 进行，而不能直接读写 **<font color = "red">主内存</font>** 中的变量。    
    2. 单个线程操作时，8种内存间交换操作指令。  
    3. 线程之间的通信和同步。线程之间的通信过程：线程对变量的操作（读取赋值等）必须在工作内存中进行，首先要将变量从主内存拷贝到自己的工作内存空间，然后对变量进行操作，操作完成后再将变量写回主内存，不能直接操作主内存中的变量，</font>各个线程中的工作内存中存储着主内存中的变量副本拷贝，<font color = "red">因此不同的线程间无法访问对方的工作内存，线程间的通信（传值）必须通过主内存来完成。</font>    

#### 1.2.1.2. 并发安全问题产生原因
1. **并发安全的3个问题：**  

    * 原子性：线程切换带来的原子性问题。（[Volatile](/docs/java/concurrent/Volatile.md)不保证原子性）
    * 可见性：缓存不能及时刷新导致的可见性问题。
    * 有序性：编译优化带来的有序性问题。  

&emsp; **<font color = "clime">`【缓存不能及时刷新】/可见性 (【内存系统重排序】)` 和`【编译器优化】/有序性` 都是`重排序`的一种。</font>**   

2. **~~重排序：~~**  
    * **<font color = "blue">重排序分类：1). 编译器优化；2). `指令重排序(CPU优化行为)；`3). 内存系统重排序：内存系统没有重排序，但是由于有缓存的存在，使得程序整体上会表现出乱序的行为。</font>**     
        * 对于编译器，JMM的编译器重排序规则会禁止特定类型的编译器重排序（不是所有的编译器重排序都要禁止）。  
        * 对于处理器重排序，JMM的处理器重排序规则会要求Java编译器在生成指令序列时，插入特定类型的内存屏障指令， **<font color = "clime">通过内存屏障指令来禁止特定类型的处理器重排序</font>** （不是所有的处理器重排序都要禁止）。 

    * 重排序遵守的规则：重排序遵守数据依赖性、重排序遵守as-if-serial语义。  
    * 重排序对多线程的影响。  

3. 伪共享问题
    1. CPU具有多级缓存，越接近CPU的缓存越小也越快；CPU缓存中的数据是以缓存行为单位处理的；CPU缓存行（通常是64字节）能带来免费加载数据的好处，所以处理数组性能非常高。  
    2. **CPU缓存行也带来了弊端，多线程处理不相干的变量时会相互影响，也就是伪共享。**  
    &emsp; 设想如果有个long类型的变量a，它不是数组的一部分，而是一个单独的变量，并且还有另外一个long类型的变量b紧挨着它，那么当加载a的时候将免费加载b。  
    &emsp; 看起来似乎没有什么毛病，但是如果一个CPU核心的线程在对a进行修改，另一个CPU核心的线程却在对b进行读取。  
    3. 避免伪共享的主要思路就是让不相干的变量不要出现在同一个缓存行中；一是在两个long类型的变量之间再加7个long类型（字节填充）；二是创建自己的long类型，而不是用原生的；三是使用java8提供的注解。  
    &emsp; 高性能原子类[LongAdder](/docs/java/concurrent/LongAdder.md)可以解决类伪共享问题。   

#### 1.2.1.3. 硬件解决并发安全
1. `【缓存一致性协议，解决可见性】`    
    1. 怎么解决缓存一致性问题呢？使用总线锁或缓存锁。  
        * 总线锁：cpu从主内存读取数据到高速缓存，会在总线对这个数据加锁，这样其他cpu无法去读或写这个数据，直到这个cpu使用完数据释放锁之后其他cpu才能读取该数据。  
        * 缓存锁：只要保证多个CPU缓存的同一份数据是一致的就可以了，`【基于缓存一致性协议来实现】`。  
    2. MESI缓存一致性协议  
        1. 缓存一致性协议有很多种，MESI（Modified-Exclusive-Shared-Invalid）协议其实是目前使用很广泛的缓存一致性协议，x86处理器所使用的缓存一致性协议就是基于MESI的。  
        2. 其他cpu通过 总线嗅探机制 可以感知到数据的变化从而将自己缓存里的数据失效。  
        &emsp; 总线嗅探， **<font color = "red">每个CPU不断嗅探总线上传播的数据来检查自己缓存值是否过期了，如果处理器发现自己的缓存行对应的内存地址被修改，就会将当前处理器的缓存行设置为无效状态，当处理器对这个数据进行修改操作的时候，会重新从内存中把数据读取到处理器缓存中。</font>**    
        2. 总线嗅探会带来总线风暴。  
2. `【操作系统的内存屏障，解决有序性】`   
    &emsp; 不同CPU硬件对于JVM的内存屏障规范实现指令不一样。  
    &emsp; Intel CPU硬件级内存屏障实现指令：  
    * Ifence：是一种Load Barrier读屏障，实现LoadLoad屏障
    * sfence：是一种Store Barrier写屏障，实现StoreStore屏障
    * mfence：是一种全能型的屏障，具备Ifencce和sfence的能留，具备所有屏障能力

#### 1.2.1.4. Java解决并发安全
1. JMM中的happens-before原则：  
    &emsp; JSR-133内存模型 **<font color = "red">使用`happens-before`的概念来阐述操作之间的`内存可见性`。在JMM中，如果一个操作执行的结果需要对另一个操作可见，那么这两个操作之间必须要存在happens-before关系。</font>** 这里提到的两个操作既可以是在一个线程之内，也可以是在不同线程之间。  
    &emsp; happens-before关系的定义如下：

    * 如果一个操作happens-before另一个操作，那么第一个操作的执行结果将对第二个操作可见，而且第一个操作的执行顺序排在第二个操作之前。  
    * 两个操作之间存在happens-before关系，并不意味着Java平台的具体实现必须要按照happens-before关系指定的顺序来执行。如果重排序之后的执行结果，与按happens-before关系来执行的结果一致，那么JMM也允许这样的重排序。  

    `happens-before原则有管理锁定（lock）规则、volatile变量规则、线程启动规则（Thread.start()）、线程终止规则（Thread.join()）、线程中断规则（Thread.interrupt()）...`    
    &emsp; volatile变量规则就是使用内存屏障保证线程可见性。  
2. 内存屏障  
    &emsp; JVM底层简化了内存屏障硬件指令的实现。  
        * lock前缀：lock指令不是一种内存屏障，但是它能完成类似内存屏障的功能。  
    
    &emsp; Java中如何保证底层操作的有序性和可见性？可以通过内存屏障。`内存屏障，禁止处理器重排序，保障缓存一致性。`  
    &emsp; `内存屏障的作用：（~~原子性~~、可见性、有序性）`  
    1. `（保障可见性）它会强制将对缓存的修改操作立即写入主存`； `【如果是写操作，会触发总线嗅探机制（MESI）】`，会导致其他CPU中对应的缓存行无效，也有 [伪共享问题](/docs/java/concurrent/PseudoSharing.md)。   
    2. `（保障有序性）阻止屏障两侧的指令重排序。`   
3. java并发原语：Java内存模型，除了定义了一套规范，还提供了一系列原语，封装了底层实现后，供开发者直接使用。  

### 1.2.2. 并发安全解决
#### 1.2.2.1. 线程安全解决方案
1. 线程安全解决方案
	1. 阻塞/互斥同步（悲观锁）
	2. 非阻塞同步（乐观锁，CAS） 
	3. 无同步方案（线程封闭）
		* 栈封闭（类变量变局部变量）
		* 线程本地存储（Thread Local Storage）
	4. 不可变对象
2. Java并发原语  
	Java内存模型，除了定义了一套规范，还提供了一系列原语，封装了底层实现后，供开发者直接使用。  
	* 原子性可以通过synchronized和Lock来实现。  
	* 可见性可以通过Volatile、synchronized、final来实现。  
	* 有序性可以通过synchronized或者Lock、volatile来实现。  

#### 1.2.2.2. Synchronized
##### 1.2.2.2.1. Synchronized介绍

##### 1.2.2.2.2. Synchronized使用
1. Java基础：对象和方法
    * 类和对象
        * xxx.Class
        * 类名 对象名
        * 实例化：new 类名();
    * 方法
        * 普通方法
        * 静态/类 方法
1. synchronized可以修饰代码块或者方法：  
    ```java
    synchronized (lock){
        // 被保护的代码块
    }
    public synchronized void method() {
        // 被保护的方法
    }
    ```
2. 类锁和对象锁  
    1. `类锁：当Synchronized修饰静态方法或Synchronized修饰代码块传入某个class对象（Synchronized (XXXX.class)）时被称为类锁。`
    2. `对象锁：当Synchronized修饰非静态方法或Synchronized修饰代码块时传入非class对象（Synchronized (this)）时被称为对象锁。`
3. String锁：由于在JVM中具有String常量池缓存的功能，因此相同字面量是同一个锁。  


#### 1.2.2.3. ~~Synchronized使用是否安全~~
&emsp; 共有 `类锁 + 对象锁 + 类锁 * 对象锁`种情况。    
1. 类锁
2. 对象锁
3. 类锁和对象锁
4. 不安全场景

##### 1.2.2.3.1. Synchronized底层原理
1. Synchronized底层实现：`查看Synchronized的字节码。`  
    * Synchronized方法同步：依靠的是方法修饰符上的ACC_Synchronized实现。  
    * Synchronized代码块同步：使用monitorenter和monitorexit指令实现。   
&emsp; 每一个对象都会和一个监视器monitor关联。监视器被占用时会被锁住，其他线程无法来获取该monitor。   
&emsp; 线程执行monitorenter指令时尝试获取对象的monitor的所有权，当monitor被占用时就会处于锁定状态。  
2. **<font color = "clime">Java对象头的MarkWord中除了存储锁状态标记外，还存有ptr_to_heavyweight_monitor（也称为管程或监视器锁）的起始地址，每个对象都存在着一个monitor与之关联。</font>**  
3. C++    
&emsp; **<font color = "clime">在Java虚拟机（HotSpot）中，Monitor是基于C++实现的，在虚拟机的ObjectMonitor.hpp文件中。</font><font color = "blue">monitor运行的机制过程如下：(_EntryList队列、_Owner区域、_WaitSet队列)</font>**  
![image](http://182.92.69.8:8081/img/java/concurrent/multi-55.png)  
    * `想要获取monitor的线程，首先会进入_EntryList队列。`  
    * `当某个线程获取到对象的monitor后，进入Owner区域，设置为当前线程，`同时计数器count加1。  
    * **如果线程调用了wait()方法，则会进入WaitSet队列。** 它会释放monitor锁，即将owner赋值为null，count自减1，进入WaitSet队列阻塞等待。  
    * 如果其他线程调用 notify() / notifyAll()，会唤醒WaitSet中的某个线程，该线程再次尝试获取monitor锁，成功即进入Owner区域。  
    * 同步方法执行完毕了，线程退出临界区，会将monitor的owner设为null，并释放监视锁。  
4. linux操作系统互斥锁mutex（内核态）  
&emsp; <font color = "clime">重量级锁是依赖对象内部的monitor锁来实现的，而monitor又依赖操作系统的MutexLock(互斥锁)来实现的，所以重量级锁也称为互斥锁。</font>  
&emsp; **<font color = "clime">为什么说重量级线程开销很大？</font>**  
&emsp; 当系统检查到锁是重量级锁之后，会把等待想要获得锁的线程进行阻塞，`被阻塞的线程不会消耗cpu`。 **<font color = "clime">`但是阻塞或者唤醒一个线程时，都需要操作系统来帮忙，这就需要从用户态转换到内核态(向内核申请)，而转换状态是需要消耗很多时间的，有可能比用户执行代码的时间还要长。`</font>**  

&emsp; 内置锁在Java中被抽象为监视器锁（monitor）。在JDK 1.6之前，监视器锁可以认为直接对应底层操作系统中的互斥量（mutex）。这种同步方式的成本非常高，包括系统调用引起的内核态与用户态切换、线程阻塞造成的线程切换等。因此，后来称这种锁为“重量级锁”。  


-----------------

&emsp; **<font color = "clime">互斥锁（Mutex）是在`原子操作API`的基础上实现的信号量行为。</font>** 互斥锁不能进行递归锁定或解锁，能用于交互上下文但是不能用于中断上下文，同一时间只能有一个任务持有互斥锁，而且只有这个任务可以对互斥锁进行解锁。当无法获取锁时，线程进入睡眠等待状态。  
&emsp; 互斥锁是信号量的特例。信号量的初始值表示有多少个任务可以同时访问共享资源，如果初始值为1，表示只有1个任务可以访问，信号量变成互斥锁（Mutex）。但是互斥锁和信号量又有所区别，互斥锁的加锁和解锁必须在同一线程里对应使用，所以互斥锁只能用于线程的互斥；信号量可以由一个线程释放，另一个线程得到，所以信号量可以用于线程的同步。   


##### 1.2.2.3.2. Synchronized优化
1. **<font color = "clime">锁降级：</font>** <font color = "red">Hotspot在1.8开始有了锁降级。在STW期间JVM进入安全点时，如果发现有闲置的monitor（重量级锁对象），会进行锁降级。</font>   
2. 锁升级  
    &emsp; 锁主要存在四种状态，依次是：无锁状态（普通对象）、偏向锁状态、轻量级锁状态、重量级锁状态，它们会随着竞争的激烈而逐渐升级。锁升级流程如下：   
    ![image](http://182.92.69.8:8081/img/java/concurrent/multi-79.png)   
    ![image](http://182.92.69.8:8081/img/java/concurrent/multi-80.png)   
    ![image](http://182.92.69.8:8081/img/java/concurrent/multi-86.png)   
	1. 偏向锁：  
        ![image](http://182.92.69.8:8081/img/java/concurrent/multi-81.png)   
        1.  **<font color = "bule">偏向锁状态</font>**  
            * **<font color = "clime">匿名偏向(Anonymously biased)</font>** 。在此状态下thread pointer为NULL(0)，意味着还没有线程偏向于这个锁对象。第一个试图获取该锁的线程将会面临这个情况，使用原子CAS指令可将该锁对象绑定于当前线程。这是允许偏向锁的类对象的初始状态。
            * **<font color = "clime">可重偏向(Rebiasable)</font>** 。在此状态下，偏向锁的epoch字段是无效的（与锁对象对应的class的mark_prototype的epoch值不匹配）。下一个试图获取锁对象的线程将会面临这个情况，使用原子CAS指令可将该锁对象绑定于当前线程。**在批量重偏向的操作中，未被持有的锁对象都被置于这个状态，以便允许被快速重偏向。**
            * **<font color = "clime">已偏向(Biased)</font>** 。这种状态下，thread pointer非空，且epoch为有效值——意味着其他线程正在持有这个锁对象。  
        4. 偏向锁的设置/取消：  
            &emsp; `偏向锁是默认开启的，而且开始时间一般是比应用程序启动慢几秒，`如果不想有这个延迟，那么可以使用-XX:BiasedLockingStartUpDelay=0；  
            &emsp; 如果不想要偏向锁，那么可以通过-XX:-UseBiasedLocking = false来设置；  
            &emsp; 在启动代码的时候，要设置一个JVM参数，-XX:BiasedLockingStartupDelay=0，这个参数可以关闭JVM的偏向延迟，JVM默认会设置一个4秒钟的偏向延迟，也就是说 `★★★JVM启动4秒钟内创建出的所有对象都是不可偏向的（也就是上图中的无锁不可偏向状态），如果对这些对象去加锁，加的会是轻量锁而不是偏向锁。`  
        2. 偏向锁获取： 
            1. 判断是偏向锁时，检查对象头Mark Word中记录的`Thread Id`是否是当前线程ID。  
            2. 如果对象头Mark Word中Thread Id不是当前线程ID，则`进行CAS操作，企图将当前线程ID替换进Mark Word`。如果当前对象锁状态处于匿名偏向锁状态（可偏向未锁定），则会替换成功（ **<font color = "clime">将Mark Word中的Thread id由匿名0改成当前线程ID，</font>** 在当前线程栈中找到内存地址最高的可用Lock Record，将线程ID存入）。  
            3. 如果对象锁已经被其他线程占用，则会替换失败，开始进行偏向锁撤销，`这也是偏向锁的特点，一旦出现线程竞争，就会撤销偏向锁；` 
        3. 偏向锁撤销： 
            1. 【等到安全点】，检查持有偏向锁的`线程是否还存活`。如果线程还存活，则检查线程是否在执行同步代码块中的代码，如果是，则升级为轻量级锁，进行CAS竞争锁； 
            2. `如果持有偏向锁的线程未存活，或者持有偏向锁的线程未在执行同步代码块中的代码`， **<font color = "red">则进行校验`是否允许重偏向`。</font>**   
                1. **<font color = "clime">如果不允许重偏向，则撤销偏向锁，将Mark Word设置为无锁状态（未锁定不可偏向状态），然后升级为轻量级锁，进行CAS竞争锁；</font><font color = "blue">(偏向锁被重置为无锁状态，这种策略是为了提高获得锁和释放锁的效率。)</font>**     
                2. 如果允许重偏向，设置为匿名偏向锁状态，CAS将偏向锁重新指向线程A（在对象头和线程栈帧的锁记录中存储当前线程ID）； 
            3. 唤醒暂停的线程，从安全点继续执行代码。 
	2. 轻量级锁：
		1. 偏向锁升级为轻量级锁之后，对象的Markword也会进行相应的的变化。   
            1. 线程在自己的栈桢中创建锁记录LockRecord。
            2. 将锁对象的对象头中的MarkWord复制到线程刚刚创建的锁记录中。
            3. 将锁记录中的Owner指针指向锁对象。
            4. 将锁对象的对象头的MarkWord替换为指向锁记录的指针。
		2. 自旋锁：轻量级锁在加锁过程中，用到了自旋锁。自旋锁分为固定次数自旋锁（在JDK 1.6之前，自旋次数默认是10次）和自适应自旋锁。
		3. 新线程获取轻量级锁
			1. 获取轻量锁过程当中会在当前线程的虚拟机栈中创建一个Lock Record的内存区域去存储获取锁的记录DisplacedMarkWord。
			2. 然后使用CAS操作将锁对象的Mark Word更新成指向刚刚创建的Lock Record的内存区域DisplacedMarkWord的地址。  
		4. 已经获取轻量级锁的线程的解锁： **<font color = "red">轻量级锁的锁释放逻辑其实就是获得锁的逆向逻辑，通过CAS操作把线程栈帧中的LockRecord替换回到锁对象的MarkWord中。</font>** 
    3. 重量级锁  
    &emsp; **<font color = "clime">为什么有了自旋锁还需要重量级锁？</font>**  
    &emsp; 自旋是消耗CPU资源的，如果锁的时间长，或者自旋线程多，CPU会被大量消耗；重量级锁有等待队列，所有拿不到锁的线程进入等待队列，不需要消耗CPU资源。  
    &emsp; 偏向锁、自旋锁都是用户空间完成。重量级锁是需要向内核申请。  
  

#### 1.2.2.4. Volatile
1. **<font color = "clime">Volatile的特性：</font>**  
    1. 不支持原子性。<font color = "red">它只对Volatile变量的单次读/写具有原子性；</font><font color = "clime">但是对于类似i++这样的复合操作不能保证原子性。</font>    
    2. 实现了可见性。 **Volatile提供happens-before的保证，使变量在多个线程间可见。**  
    3. <font color = "red">实现了有序性，禁止进行指令重排序。</font>  
2. `Volatile底层原理（happens-before中Volatile的特殊规则）：`查看Volatile的汇编代码。    
    * **<font color = "clime">在Volatile写前插入`写`-写[屏障](/docs/java/concurrent/ConcurrencySolve.md)（禁止上面的普通写与下面的Volatile写重排序），在Volatile写后插入写-`读`屏障（禁止上面的Volatile写与下面可能有的Volatile读/写重排序）。</font>**  
    * **<font color = "clime">在Volatile读后插入`读`-读屏障（禁止下面的普通读操作与上面的Volatile读重排序）、读-`写`屏障（禁止下面所有的普通写操作和上面Volatile读重排序）。</font>**  
3. Volatile为什么不安全（不保证原子性，线程切换）？  
&emsp; 两个线程执行i++（i++的过程可以分为三步，首先获取i的值，其次对i的值进行加1，最后将得到的新值写回到缓存中），线程1获取i值后被挂起，线程2执行...  
4. volatile使用场景：  
    &emsp; 关键字Volatile用于多线程环境下的`单次操作（单次读或者单次写）`。即Volatile主要使用的场合是在多个线程中可以感知实例变量被更改了，并且可以获得最新的值使用，也就是用多线程读取共享变量时可以获得最新值使用。  
    1. 全局状态标志。
    2. DCL详解：  
        1. 为什么两次判断？线程1调用第一个if(singleton==null)，可能会被挂起。  
        2. `为什么要加volatile关键字？`  
        &emsp; singleton = new Singleton()非原子性操作，包含3个步骤：分配内存 ---> 初始化对象 ---> 将singleton对象指向分配的内存空间。第3步一旦执行了，那singleton对象就不等于null了。  
        &emsp; **<font color = "clime">因为指令重排序，可能编程1->3->2。如果是这种顺序，会导致别的线程拿到半成品的实例。</font>**  

#### 1.2.2.5. ThreadLocal
&emsp; ThreadLocal的作用是每一个线程创建一个副本。  

##### 1.2.2.5.1. ThreadLocal原理
1. ThreadLocal是如何实现线程隔离的？   
    ![image](http://182.92.69.8:8081/img/java/concurrent/multi-85.png)  
    &emsp; ThreadLocal之所以能达到变量的线程隔离，其实就是每个线程都有一个自己的ThreadLocalMap对象来存储同一个threadLocal实例set的值，而`取值的时候也是根据同一个threadLocal实例去自己的ThreadLocalMap里面找，自然就互不影响了，从而达到线程隔离的目的！`  
2. ThreadLocal源码/内存模型：  
    1. **<font color = "red">ThreadLocal的#set()、#getMap()方法：线程调用threadLocal对象的set(Object value)方法时，数据并不是存储在ThreadLocal对象中，</font><font color = "clime">而是将值存储在每个Thread实例的threadLocals属性中。</font>** 即当前线程调用ThreadLocal类的set或get方法时，实际上调用的是ThreadLocalMap类对应的 get()、set()方法。  
    &emsp; ~~Thread ---> ThreadLocal.ThreadLocalMap~~
    2. **<font color = "clime">ThreadLocal.ThreadLocalMap，</font>Map中`Key是一个ThreadLocal实例，Value是设置的值。`ThreadLocalMap结构中Entry继承WeakReference，所以Entry对应key的引用（ThreadLocal实例）是一个弱引用，Entry对Value的引用是强引用。  
    &emsp; <font color = "clime">Entry的作用即是：为其属主线程建立起一个ThreadLocal实例与一个线程持有对象之间的对应关系。</font>** 一个线程可能有多个ThreadLocal实例，编码中定义多个ThreadLocal实例，即存在多个Entry的情况。    
3. **ThreadLocal内存泄露：**  
    &emsp; ThreadLocalMap使用ThreadLocal的弱引用作为key，<font color = "red">如果一个ThreadLocal不存在外部强引用时，Key（ThreadLocal实例）会被GC回收，这样就会导致ThreadLocalMap中key为null，而value还存在着强引用，只有thread线程退出以后，value的强引用链条才会断掉。</font>  
    &emsp; **<font color = "clime">但如果当前线程迟迟不结束的话，这些key为null的Entry的value就会一直存在一条强引用链：Thread Ref -> Thread -> ThreaLocalMap -> Entry -> value。永远无法回收，造成内存泄漏。</font>**  
    &emsp; 解决方案：`调用remove()方法`
4. **ThreadLocalMap的key被回收后，如何获取值？**  
    &emsp; ThreadLocal#get() ---> setInitialValue() ---> ThreadLocalMap.set(this, value); 。  
    &emsp; 通过nextIndex()不断获取table上的槽位，直到遇到第一个为null的地方，此处也将是存放具体entry的位置，在线性探测法的不断冲突中，如果遇到非空entry中的key为null，可以表明key的弱引用已经被回收，但是由于线程仍未结束生命周期被回收，而导致该entry仍未从table中被回收，那么则会在这里尝试通过replaceStaleEntry()方法，将null key的entry回收掉并set相应的值。  

##### 1.2.2.5.2. ThreadLocal应用

        1. 在进行对象跨层次传递的时候，使用ThreadLocal可以避免多次传递，打破层次间的束缚。   
        2. 线程间层次隔离。  
        3. 进行事务操作，用于存储线程事务信息。  
        4. 数据库连接，Session会话管理。  

1. ThreadLocal使用场景：  
    1. 线程安全问题。
    2. 业务中变量传递。1)ThreadLocal实现同一线程下多个类之间的数据传递；2)ThreadLocal实现线程内的缓存，避免重复调用。
    3. ThreadLocal+MDC实现链路日志增强。
    4. ThreadLocal 实现数据库读写分离下强制读主库。
2. ~~ThreadLocal三大坑~~
    1. 内存泄露
    2. ThreadLocal无法在`父子线程（new Thread()）`之间传递。使用类InheritableThreadLocal可以在子线程中取得父线程继承下来的值。   
    3. 线程池中线程上下文丢失。TransmittableThreadLocal是阿里巴巴开源的专门解决InheritableThreadLocal的局限性，实现线程本地变量在线程池的执行过程中，能正常的访问父线程设置的线程变量。  
    4. 并行流中线程上下文丢失。问题同线程池中线程上下文丢失。  
3. ThreadLocal优化：FastThreadLocal

### 1.2.3. 线程通信(生产者消费者问题)

1. 生产者消费者问题，Java能实现的几种方法：  
    * wait() / notify()方法
    * await() / signal()方法
    * BlockingQueue阻塞队列方法
    * 信号量
    * 管道
2. 单机中实现线程通信的方式：  
    1. 等待、通知机制。wait/notify/notifyAll（synchronized同步方法或同步块中使用）实现内存可见性，及生产消费模式的相互唤醒机制；  
    2. 等待、通知机制。同步锁（Lock）的Condition（await\signal\signalAll）；  
    3. Thread#join()；  
    4. 管道，共享内存，实现数据的共享，满足读写模式。管道通信就是使用java.io.PipedInputStream和java.io.PipedOutputStream进行通信。  
3. 分布式系统中说的两种通信机制：共享内存机制和消息通信机制。  

### 1.2.4. 线程活跃性


## 1.3. JUC
### 1.3.1. CAS
1. **<font color = "clime">CAS，Compare And Swap，即比较并交换。一种无锁原子算法，CAS是一种乐观锁。</font>**  
2. CAS函数  
&emsp; **<font color = "clime">在函数CAS(V,E,N)中有3个参数：从内存中读取的值E，计算的结果值V，内存中的当前值N（可能已经被其他线程改变）。</font>**  
&emsp; **<font color = "clime">函数流程：</font>** 1. 读取当前值E；2. 计算结果值V；<font color = "clime">3. 将读取的当前值E和当前新值N作比较，如果相等，更新为V；</font>4. 如果不相等，再次读取当前值E计算结果V，将E再和新的当前值N比较，直到相等。 
3. **`CAS缺点：`**  
    * 循环时间长开销大。自旋CAS如果长时间不成功，会给CPU带来非常大的执行开销。  
    * **<font color = "red">只能保证一个共享变量的原子操作。</font> <font color = "clime">从Java1.5开始JDK提供了AtomicReference类来保证引用对象之间的原子性，可以把多个变量放在一个对象里来进行CAS操作。</font>**  
    * ABA问题。  
4. ABA问题详解
    1. 什么是ABA问题？  
    &emsp; ABA示例：  
    &emsp; 1).在多线程的环境中，线程a从共享的地址X中读取到了对象A。  
    &emsp; 2).在线程a准备对地址X进行更新之前， **<font color = "clime">线程a挂起</font>** 。线程b将地址X中的值修改为了B。  
    &emsp; 3).接着线程b或者线程c将地址X中的值又修改回了A。  
    &emsp; 4).线程a恢复，接着对地址X执行CAS，发现X中存储的还是对象A，对象匹配，CAS成功。  
    2. ABA问题需不需要解决？   
    &emsp; ~~如果依赖中间变化的状态，需要解决。如果不是依赖中间变化的状态，对业务结果无影响。~~  
    3. 解决ABA问题  
    &emsp; **<font color = "red">ABA问题的解决思路就是使用版本号。在变量前面追加上版本号，每次变量更新的时候把版本号加一，那么A－B－A 就会变成1A-2B－3A。</font>**   
    &emsp; **<font color = "clime">从Java1.5开始JDK的atomic包里提供了[AtomicStampedReference](/docs/java/concurrent/6.AtomicStampedReference.md)和AtomicMarkableReference类来解决ABA问题。</font>**  

### 1.3.2. AQS
1. 属性
    1. 同步状态，通过state控制同步状态。  
    2. 同步队列，`双向链表`，每个节点代表一个线程，节点有5个状态。
        * 入列addWaiter()：未获取到锁的线程会创建节点，`线程安全（CAS算法设置尾节点+死循环自旋）`的加入队列尾部。  
        * 出列unparkSuccessor()：首节点的线程释放同步状态后，`将会唤醒(LockSupport.unpark)它的后继节点(next)`，而后继节点将会在获取同步状态成功时将自己设置为首节点。
        * 入列或出列都会使用到[LockSupport](/docs/java/concurrent/LockSupport.md)工具类来阻塞、唤醒线程。    
2. 方法
    0. 执行 加锁/解锁（控制同步状态state） 、队列 、 阻塞/唤醒 这3个步骤。  
    1. 独占模式：  
        * **<font color = "blue">获取同步状态</font>**   
            1. `调用使用者重写的tryAcquire方法，` **<font color = "blue">tryAcquire()尝试直接去获取资源，</font>** 如果成功则直接返回。
            2. tryAcquire()获取资源失败，则`调用addWaiter()将该线程加入等待队列的尾部`，并标记为独占模式。
            3. acquireQueued()使线程阻塞在等待队列中获取资源，一直获取到资源后才返回。如果在整个等待过程中被中断过，则返回true，否则返回false。
            4. 如果线程在等待过程中被中断过，它是不响应的。只是获取资源后才再进行自我中断selfInterrupt()，将中断补上。
        * 释放同步状态  
    2. 共享模式下，获取同步状态、释放同步状态。  

#### 1.3.2.1. LockSupport类
&emsp; LockSupport（support，支持）是一个线程阻塞工具类，所有的方法都是静态方法，可以让线程在任意位置阻塞，当然阻塞之后肯定得有唤醒的方法。  
&emsp; LockSupport主要有两类方法：park和unpark。 

### 1.3.3. LOCK
#### 1.3.3.1. ReentrantLock，重入锁
1. ReentrantLock与synchronized比较：非公平、非`阻塞`、超时/限时`等待`、可被`中断`、可实现选择性通知  
    1. （支持非公平）ReenTrantLock可以指定是公平锁还是非公平锁。而synchronized只能是非公平锁。所谓的公平锁就是先等待的线程先获得锁。  
    2. （支持非阻塞）Lock接口可以尝试非阻塞地获取锁，当前线程尝试获取锁。如果这一时刻锁没有被其他线程获取到，则成功获取并持有锁。  
    3. （支持超时/限时等待）Lock接口可以在指定的截止时间之前获取锁，如果截止时间到了依旧无法获取锁，则返回。可以让线程尝试获取锁，并在无法获取锁的时候立即返回或者等待一段时间。  
    4. （可被中断）Lock接口能被中断地获取锁，与synchronized不同，获取到锁的线程能够响应中断，当获取到的锁的线程被中断时，中断异常将会被抛出，同时锁会被释放。可以使线程在等待锁的时候响应中断。  
    5. （可实现选择性通知，锁可以绑定多个条件）ReenTrantLock提供了一个Condition(条件)类，用来实现分组唤醒需要唤醒的一些线程，而不是像synchronized要么随机唤醒一个线程要么唤醒全部线程。  
2. **<font color = "red">lock()方法描述：</font>**  
    ![image](http://182.92.69.8:8081/img/java/concurrent/multi-87.png)  
    1. 在初始化ReentrantLock的时候，如果不传参数是否公平，那么默认使用非公平锁，也就是NonfairSync。  
    2. 1). <font color = "clime">调用ReentrantLock的lock方法的时候，实际上是调用了NonfairSync的lock方法，这个方法①先用CAS操作`compareAndSetState(0, 1)`，去尝试抢占该锁。如果成功，就把当前线程设置在这个锁上，表示抢占成功。</font>         
        &emsp; `“非公平”体现在，如果占用锁的线程刚释放锁，state置为0，而排队等待锁的线程还未唤醒时，新来的线程就直接抢占了该锁，那么就“插队”了。`   
        2). ②如果失败，则`调用acquire()模板方法`，等待抢占。   
    3. `AQS的acquire()模板方法：`  
        1. AQS#acquire()调用子类NonfairSync#tryAcquire()#nonfairTryAcquire()。 **<font color = "blue">如果锁状态是0，再次CAS抢占锁。</font>** 如果锁状态不是0，判断是否当前线程。    
        2. acquireQueued(addWaiter(Node.EXCLUSIVE), arg) )，其中addWaiter(Node.EXCLUSIVE)入等待队列。  
        3. acquireQueued(final Node node, int arg)，使线程阻塞在等待队列中获取资源，一直获取到资源后才返回。如果在整个等待过程中被中断过，则返回true，否则返回false。
        4. 如果线程在等待过程中被中断过，它是不响应的。`只是获取资源后才再进行自我中断selfInterrupt()，将中断补上。`  

    &emsp; 用一张流程图总结一下非公平锁的获取锁的过程。  
    ![image](http://182.92.69.8:8081/img/java/concurrent/multi-75.png)  

##### 1.3.3.1.1. 读写锁
1. ReentrantReadWriteLock  
    1. 读写锁ReentrantReadWriteLock：读读共享，`读写互斥`，写写互斥。  
    2. **<font color = "red">ReentrantReadWriteLock缺点：`读写锁互斥`，只有当前没有线程持有读锁或者写锁时，才能获取到写锁，</font><font color = "clime">这`可能会导致写线程发生饥饿现象`，</font><font color = "red">即读线程太多导致写线程迟迟竞争不到锁而一直处于等待状态。StampedLock()可以解决这个问题。</font>**  
2. StampedLock，Stamped，有邮戳的  
    1. StampedLock有3种模式：写锁writeLock、悲观读锁readLock、乐观读锁tryOptimisticRead。  
    2. StampedLock通过乐观读锁tryOptimisticRead解决ReentrantReadWriteLock的写锁饥饿问题。乐观读锁模式下，一个线程获取的乐观读锁之后，不会阻塞其他线程获取写锁。    
    3. **<font color = "clime">同时允许多个乐观读和一个写线程同时进入临界资源操作，那`读取的数据可能是错的怎么办？`</font>**    
    &emsp; **<font color = "clime">`通过版本号控制。`</font>** 乐观读不能保证读取到的数据是最新的，所以`将数据读取到局部变量的时候需要通过 lock.validate(stamp) 校验是否被写线程修改过`，若是修改过则需要上悲观读锁，再重新读取数据到局部变量。`即乐观读失败后，再次使用悲观读锁。`    

### 1.3.4. Atomic
#### 1.3.4.1. AtomicStampedReference与AtomicMarkableReference
1. AtomicStampedReference每次修改都会让stamp值加1，类似于版本控制号。 
2. **<font color = "clime">AtomicStampedReference可以知道引用变量中途被更改了几次。有时候，并不关心引用变量更改了几次，只是单纯的关心是否更改过，所以就有了AtomicMarkableReference。</font>**  

#### 1.3.4.2. LongAdder
1. LongAdder重要属性：有一个全局变量`volatile long base`值、父类Striped64中存在一个`volatile Cell[] cells;`数组，其长度是2的幂次方。  
2. LongAdder原理：  
    1. CAS操作：当并发不高的情况下都是通过CAS来直接操作base值，如果CAS失败，则针对LongAdder中的Cell[]数组中的Cell进行CAS操作，减少失败的概率。
    2. 解决伪共享：每个Cell都使用@Contended注解进行修饰，而@Contended注解可以进行缓存行填充，从而解决伪共享问题。  

### 1.3.5. Collections
#### 1.3.5.1. CopyOnWriteArrayList
1. CopyOnWriteArrayList  
&emsp; CopyOnWrite，写时复制。`读操作时不加锁以保证性能不受影响。`  
&emsp; **<font color = "clime">`写操作时加锁，复制资源的一份副本，在副本上执行写操作，写操作完成后将资源的引用指向副本。`</font>** CopyOnWriteArrayList源码中，`基于ReentrantLock保证了增加元素和删除元素动作的互斥。`   
&emsp; **优点：** 可以对CopyOnWrite容器进行并发的读，而不需要加锁，因为当前容器不会添加任何元素。`所以CopyOnWrite容器也是一种读写分离的思想，读和写不同的容器。`  
&emsp; **<font color = "clime">缺点：** **1.占内存（写时复制，new两个对象）；2.不能保证数据实时一致性。</font>**  
&emsp; **使用场景：** <font color = "clime">CopyOnWrite并发容器用于读多写少的并发场景。比如白名单，黑名单，商品类目的访问和更新场景。</font>  

#### 1.3.5.2. ConcurrentHashMap
1. ConcurrentHashMap，JDK1.8  
    &emsp; **<font color = "red">从jdk1.8开始，ConcurrentHashMap类取消了Segment分段锁，采用`Node + CAS + Synchronized`来保证并发安全。</font>**  
    &emsp; **<font color = "clime">jdk1.8中的ConcurrentHashMap中synchronized只锁定当前链表或红黑树的首节点，只要节点hash不冲突，就不会产生并发，相比JDK1.7的ConcurrentHashMap效率又提升了许多。</font>**  
    1. **<font color = "clime">put()流程：</font>**
        1. 根据key计算出hashcode。  
        2. `整个过程自旋添加节点。`  
        2. 判断是否需要进行初始化数组。  
        3. <font color = "red">为当前key定位出Node，如果为空表示此数组下无节点，当前位置可以直接写入数据，利用CAS尝试写入，失败则进入下一次循环。</font>  
        4. **<font color = "blue">如果当前位置的hashcode == MOVED == -1，表示其他线程插入成功正在进行扩容，则当前线程`帮助进行扩容`。</font>**  
        5. <font color = "red">如果都不满足，则利用synchronized锁写入数据。</font>  
        6. 如果数量大于TREEIFY_THRESHOLD则要转换为红黑树。 
        7. 最后通过addCount来增加ConcurrentHashMap的长度，并且还可能触发扩容操作。  
    2. 协助扩容  
        &emsp; `ConcurrentHashMap并没有直接加锁，而是采用CAS实现无锁的并发同步策略，最精华的部分是它可以利用多线程来进行协同扩容。简单来说，它把Node数组当作多个线程之间共享的任务队列，然后通过维护一个指针来划分每个线程锁负责的区间，每个线程通过区间逆向遍历来实现扩容，一个已经迁移完的bucket会被替换为一个ForwardingNode节点，标记当前bucket已经被其他线程迁移完了。`   
    3. **<font color = "clime">get()流程：为什么ConcurrentHashMap的读操作不需要加锁？</font>**  
        1. 在1.8中ConcurrentHashMap的get操作全程不需要加锁，这也是它比其他并发集合（比如hashtable、用Collections.synchronizedMap()包装的hashmap）安全效率高的原因之一。  
        2. get操作全程不需要加锁是因为Node的成员val是用volatile修饰的，和数组用volatile修饰没有关系。  
        3. 数组用volatile修饰主要是保证在数组扩容的时候保证可见性。  
2. ~~ConcurrentHashMap，JDK1.7~~  
    1. 在JDK1.7中，ConcurrentHashMap类采用了分段锁的思想，Segment(段) + HashEntry(哈希条目) + ReentrantLock。  
    2. Segment继承ReentrantLock(可重入锁)，从而实现并发控制。Segment的个数一旦初始化就不能改变，默认Segment的个数是16个，也可以认为ConcurrentHashMap默认支持最多16个线程并发。  
    3. put()方法：  
        1. 获取 ReentrantLock 独占锁，获取不到，scanAndLockForPut 获取。  
        2. scanAndLockForPut 这个方法可以确保返回时，当前线程一定是获取到锁的状态。  

#### 1.3.5.3. BlockingQueue
1. 阻塞队列：当队列是空的时候，从队列中获取元素的操作将会被`阻塞`；或者当队列是满时，往队列里添加元素的操作会被`阻塞`。  
2. `线程池所使用的缓冲队列，常用的是：SynchronousQueue（无缓冲等待队列）、ArrayBlockingQueue（有界缓冲等待队列）、LinkedBlockingQueue（无界缓冲等待队列）。`   
3. SynchronousQueue，没有容量，是无缓冲等待队列，是一个不存储元素的阻塞队列，会直接将任务交给消费者，必须等队列中的元素被消费后才能继续添加新的元素。  
4. LinkedBlockingQueue不同于ArrayBlockingQueue，它如果不指定容量，默认为Integer.MAX_VALUE，也就是无界队列。所以为了避免队列过大造成机器负载或者内存爆满的情况出现，在使用的时候建议手动传一个队列的大小。  
5. <font color = "red">ArrayBlockingQueue与LinkedBlockingQueue：</font> ArrayBlockingQueue预先分配好一段连续内存，更稳定；LinkedBlockingQueue读写锁分离，吞吐量更大。  

### 1.3.6. tools
#### 1.3.6.1. CountDownLatch
0. CountDownLatch中count down是倒数的意思，latch则是门闩的含义。整体含义可以理解为倒数的门栓，似乎有一点“三二一，芝麻开门”的感觉。CountDownLatch的作用也是如此，在构造CountDownLatch的时候需要传入一个整数n，在这个整数“倒数”到0之前，主线程需要等待在门口，而这个“倒数”过程则是由各个执行线程驱动的，每个线程执行完一个任务“倒数”一次。总结来说，CountDownLatch的作用就是等待其他的线程都执行完任务，必要时可以对各个任务的执行结果进行汇总，然后主线程才继续往下执行。  
1. java.util.concurrent.CountDownLatch类， **<font color = "red">能够使一个线程等待其他线程完成各自的工作后再执行。</font>** <font color = "red">利用它可以实现类似计数器的功能。</font><font color = "blue">比如有一个任务A，它要等待其他4个任务执行完毕之后才能执行，此时就可以利用CountDownLatch来实现这种功能了。</font>  
2. CountDownLatch的典型应用场景，大体可分为两类：结束信号、开始信号。  
&emsp; 主线程创建、启动N个异步任务，期望当这N个任务全部执行完毕结束后，主线程才可以继续往下执行。即将CountDownLatch作为任务的结束信号来使用。   
2. **<font color = "clime">countDown()方法是将count-1，如果发现count=0了，就唤醒</font><font color = "blue">阻塞的主线程。</font>**  
&emsp; `⚠️注：特别注意主线程会被阻塞。`  
3. <font color = "red">CountDownLatch对象不能被重复利用，也就是不能修改计数器的值。</font>CountDownLatch是一次性的，计数器的值只能在构造方法中初始化一次，之后没有任何机制再次对其设置值，当CountDownLatch使用完毕后，它不能再次被使用。    
4. <font color = "clime">CountDownLatch是由AQS实现的，创建CountDownLatch时设置计数器count，其实就是设置AQS.state=count，也就是重入次数。  
    * await()方法调用获取锁的方法，由于AQS.state=count表示锁被占用且重入次数为count，所以获取不到锁线程被阻塞并进入AQS队列。  
    * countDown()方法调用释放锁的方法，每释放一次AQS.state减1，当AQS.state变为0时表示处于无锁状态了，就依次唤醒AQS队列中阻塞的线程来获取锁，继续执行逻辑代码。</font>  

#### 1.3.6.2. CyclicBarrier
&emsp; CyclicBarrier字面意思是回环栅栏， **<font color = "blue">允许一组线程互相等待，直到到达某个公共屏障点 (common barrier point)之后，再全部同时执行。</font>** 叫做回环是因为当所有等待线程都被释放以后，CyclicBarrier可以被重用。  

&emsp; **<font color = "clime">CyclicBarrier用途有两个：</font>**   

* 让一组线程等待至某个状态后再同时执行。
* 让一组线程等待至某个状态后，执行指定的任务。

#### 1.3.6.3. Semaphore
&emsp; Semaphore类，一个计数信号量。从概念上讲，信号量维护了一个许可集合。如有必要，在许可可用前会阻塞每一个acquire()，然后再获取该许可。每个 release()添加一个许可，从而可能释放一个正在阻塞的获取者。但是，不使用实际的许可对象，Semaphore只对可用许可的号码进行计数，并采取相应的行动。  
&emsp; 使用场景： **<font color = "red">Semaphore通常用于限制可以访问某些资源（物理或逻辑的）的线程数目。Semaphore可以用来构建一些对象池，资源池之类的，比如数据库连接池。</font>**   
