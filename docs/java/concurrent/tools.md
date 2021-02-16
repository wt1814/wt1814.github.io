


<!-- TOC -->

- [1. Tools，工具类](#1-tools工具类)
    - [1.1. CountDownLatch，线程计数器](#11-countdownlatch线程计数器)
        - [1.1.1. API](#111-api)
        - [1.1.2. 使用示例](#112-使用示例)
        - [1.1.3. CountDownLatch源码分析](#113-countdownlatch源码分析)
            - [1.1.3.1. 类结构](#1131-类结构)
            - [1.1.3.2. await()](#1132-await)
            - [1.1.3.3. countDown()](#1133-countdown)
        - [1.1.4. 总结](#114-总结)
    - [1.2. CyclicBarrier，回环栅栏](#12-cyclicbarrier回环栅栏)
        - [1.2.1. API](#121-api)
        - [1.2.2. 实际使用场景](#122-实际使用场景)
        - [1.2.3. 使用示例](#123-使用示例)
        - [1.2.4. CycliBarriar和CountdownLatch有什么区别？](#124-cyclibarriar和countdownlatch有什么区别)
        - [1.2.5. ※※※CycliBarriar和CountdownLatch具体使用](#125-※※※cyclibarriar和countdownlatch具体使用)
    - [1.3. Semaphore，信号量-控制同时访问的线程个数](#13-semaphore信号量-控制同时访问的线程个数)
        - [1.3.1. API](#131-api)
        - [1.3.2. 使用示例](#132-使用示例)
    - [1.4. Exchanger，交换器](#14-exchanger交换器)

<!-- /TOC -->

&emsp; **总结：**  

* Semaphore，计数信号量，用于控制访问共享资源的线程的数目。  
* 多个线程间相互协作完成任务：  
    * CountDownLatch，线程计数器，主线程等待辅线程完成各自的工作后再执行。  
    * CyclicBarrier，回环栅栏，多个线程互相等待，直到到达某个公共屏障点之后，再全部同时执行。  
* Exchanger，交换器  

# 1. Tools，工具类  
<!--
并发工具类Phaser、Exchanger使用 
https://mp.weixin.qq.com/s/6evcGMWJ8VSNh-lmYJEbrQ


https://mp.weixin.qq.com/s/JCen6ppvWYNDnB5KCsrNEA
https://mp.weixin.qq.com/s/Ib8lpezEmfDDh3Dy4Q6iDA

java中如何模拟真正的同时并发请求？
https://www.cnblogs.com/yougewe/p/9745198.html

多线程进阶－CyclicBarrier 源码超详细解析，学到就赚到 
   https://mp.weixin.qq.com/s/odsutVotjJjXFX4nAhb54w

-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-9.png)  
&emsp; Java提供了同步工具类：CountDownLatch(计数器)、CyclicBarrier(栅栏)、Semaphore(信号量)。这几个工具类是为了能够更好控制线程之间的通讯问题。  

## 1.1. CountDownLatch，线程计数器  
<!-- 
CountDownLatch实践
https://mp.weixin.qq.com/s/wDxfDcbJCQs99huLyztnCQ

https://mp.weixin.qq.com/s/JynLduej5FHPTmKf6nhsOA
-->

<!-- 
闭锁是一种同步工具类，可以延迟线程的进度直到其到达终止状态。闭锁的作 用相当于一扇门：在闭锁到达结束状态之前，这扇门一直是关闭的，并且没有任何线程能通过， 当到达结束状态时，这扇门会打开并允许所有的线程通过。当闭锁到达结束状态后，将不会再 改变状态，因此这扇门将永远保持打开状态。闭锁可以用来确保某些活动直到其他活动都完成 后才继续执行，例如：  

* 确保某个计算在其需要的所有资源都被初始化之后才继续执行。二元闭锁(包括两个状 态)可以用来表示“资源R已经被初始化”，而所有需要R的操作都必须先在这个闭锁 上等待。  
* 确保某个服务在其依赖的所有其他服务都已经启动之后才启动。每个服务都有一个相关 的二元闭锁。当启动服务S时，将首先在S依赖的其他服务的闭锁上等待，在所有依赖 的服务都启动后会释放闭锁S,这样其他依赖S的服务才能继续执行。  
* 等待直到某个操作的所有参与者(例如，在多玩家游戏中的所有玩家)都就绪再继续执 行。在这种情况中，当所有玩家都准备就绪时，闭锁将到达结束状态。  

CountDownLatch是一种灵活的闭锁实现，可以在上述各种情况中使用，它可以使一个或 多个线程等待一组事件发生。闭锁状态包括一个计数器，该计数器被初始化为一个正数，表示 需要等待的事件数量。countDown方法递减计数器，表示有一个事件已经发生了，而await方 法等待计数器达到零，这表示所有需要等待的事件都已经发生。如果计数器的值非零，那么 await会一直阻塞直到计数器为零，或者等待中的线程中断，或者等待超时。

FutureTask也可以用做闭锁。  
FutureTask在Executor框架中表示异步任务，此外还可以用来表示一些时间较长的计算， 这些计算可以在使用计算结果之前启动。  
-->

&emsp; <font color = "lime">CountDownLatch是由AQS实现的，创建CountDownLatch时设置计数器count其实就是设置AQS.state=count，也就是重入次数。await()方法调用获取锁的方法，由于AQS.state=count表示锁被占用且重入次数为count，所以获取不到锁线程被阻塞并进入AQS队列。countDown()方法调用释放锁的方法，每释放一次AQS.state减1，当AQS.state变为0时表示处于无锁状态了，就依次唤醒AQS队列中阻塞的线程来获取锁，继续执行逻辑代码。</font>  

&emsp; java.util.concurrent.CountDownLatch类， **<font color = "red">能够使一个线程等待其他线程完成各自的工作后再执行。</font>** <font color = "red">利用它可以实现类似计数器的功能。比如有一个任务A，它要等待其他4个任务执行完毕之后才能执行，此时就可以利用CountDownLatch来实现这种功能了。</font>  
&emsp; **<font color = "lime">CountDownLatch的操作是原子操作，同时只能有一个线程去操作这个计数器，也就是同时只能有一个线程去减这个计数器里面的值。</font>** 可以向CountDownLatch对象设置一个初始的数字作为计数值，任何调用这个对象上的await()方法都会阻塞，直到这个计数器的计数值被其他的线程减为0为止。所以在当前计数到达零之前，await方法会一直受阻塞。之后，会释放所有等待的线程，await的所有后续调用都将立即返回。这种现象只出现一次——计数无法被重置。如果需要重置计数，请考虑使用CyclicBarrier。  
    
&emsp; **注意：**  
1. <font color = "red">CountDownLatch对象不能被重复利用，也就是不能修改计数器的值。</font>  
2. CountDownLatch代表的计数器的大小可以为0，意味着在一个线程调用await方法时会立即返回。  
3. 如果某些线程中有阻塞操作的话，最好使用带有超时时间的await方法，以免该线程调用await方法之后永远得不到执行。  

&emsp; **CountDownLatch与Thread的join()方法方法对比：**  
&emsp; Thread的join方法，这个方法表示一个线程将等待另一个线程执行完才能继续执行。  
&emsp; CountDownLatch代表的是一个计数器，不论是否在同一线程中，不论线程是否执行完成，都可以随时随地调用CountDownLatch的countDown方法，而Thread的成员方法join只能在一个线程中对另一个线程对象调用，而且方法返回的前提是线程已经执行完成。  
&emsp; 所以使用CountDownLatch会比join方法更灵活。  

### 1.1.1. API  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-8.png)  
&emsp; **构造函数：**  

```java
//创建对象时，指定计数器大小
public CountDownLatch(int count) {
    if (count < 0) throw new IllegalArgumentException("count < 0");
    this.sync = new Sync(count);
}
```
&emsp; **成员方法：**  

```java
//创建对象时，指定计数器大小
public CountDownLatch(int count) {
    if (count < 0) throw new IllegalArgumentException("count < 0");
    this.sync = new Sync(count);
}
//返回当前计数
public long getCount() {
    return sync.getCount();
}
//计数减1，如果计数达到零，则释放所有等待的线程
public void countDown() {
    sync.releaseShared(1);
}
//使当前线程在计数至0之前一直等待，计数为0时返回
public void await() throws InterruptedException {
    sync.acquireSharedInterruptibly(1);
}
//使当前线程在指定时间内等待计数器为0，超时或计数为0的时候则返回
public boolean await(long timeout, TimeUnit unit)
        throws InterruptedException {
    return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
}
```
&emsp; CountDownLatch内部通过共享锁实现。在创建CountDownLatch实例时，需要传递一个int型的参数：count，该参数为计数器的初始值，也可以理解为该共享锁可以获取的总次数。当某个线程调用await()方法，程序首先判断count的值是否为0，如果不会0的话则会一直等待直到为0为止。当其他线程调用countDown()方法时，则执行释放共享锁状态，使count值- 1。当在创建CountDownLatch时初始化的count参数，必须要有count线程调用countDown方法才会使计数器count等于0，锁才会释放，前面等待的线程才会继续运行。注意CountDownLatch不能回滚重置。 

### 1.1.2. 使用示例  

<!-- 
java多线程累加计数
https://blog.csdn.net/wzmde007/article/details/79641084
-->

&emsp; CountDownLatch的作用是让线程等待其它线程完成一组操作后才能执行，否则就一直等待。  
&emsp; 举个开会的例子：  

1. 老板先进入会议室准备材料
2. 等待5个员工陆续进入会议室
3. 员工到齐开始开会

&emsp; 老板不能一来就开会，必须要等员工都到了再开会，用CountDownLatch实现如下：  

```java
public class CountDownLatchTest {
    private static CountDownLatch countDownLatch = new CountDownLatch(5);

    // Boss线程，等待员工到齐开会
    static class BossThread extends Thread {
        @Override
        public void run() {
            System.out.println("Boss进入会议室准备材料...");
            System.out.println("Boss在会议室等待...");
            try {
                countDownLatch.await(); // Boss等待
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Boss等到人齐了，开始开会...");
        }
    }

    // 员工到达会议室
    static class EmpleoyeeThread extends Thread {
        @Override
        public void run() {
            System.out.println("员工" + Thread.currentThread().getName()
                    + "，到达会议室....");
            countDownLatch.countDown();
        }
    }

    public static void main(String[] args) {
        // Boss线程启动
        new BossThread().start();

        // 员工到达会议室
        for (int i = 0; i < countDownLatch.getCount(); i++) {
            new EmpleoyeeThread().start();
        }
    }
}
```

&emsp; 控制台输出：  

```java
Boss进入会议室准备材料...
Boss在会议室等待...
员工Thread-2，到达会议室....
员工Thread-3，到达会议室....
员工Thread-4，到达会议室....
员工Thread-1，到达会议室....
员工Thread-5，到达会议室....
Boss等到人齐了，开始开会...
```

&emsp; 总结CountDownLatch的使用步骤：(比如线程A需要等待线程B和线程C执行后再执行)  
1. 创建CountDownLatch对象，设置要等待的线程数N；  
2. 等待线程A调用await()挂起；  
3. 线程B执行后调用countDown()，使N-1；线程C执行后调用countDown()，使N-1；  
4. 调用countDown()后检查N=0了，唤醒线程A，在await()挂起的位置继续执行。  

### 1.1.3. CountDownLatch源码分析  
<!-- 
一文搞懂 CountDownLatch 用法和源码！
https://mp.weixin.qq.com/s/m60Tq18Vbp5ml-qAp505VQ
-->
&emsp; CountDownLatch是通过一个计数器来实现的，在new 一个CountDownLatch对象的时候需要带入该计数器值，该值就表示了线程的数量。<font color = "red">每当一个线程完成自己的任务后，计数器的值就会减1。当计数器的值变为0时，就表示所有的线程均已经完成了任务，然后就可以恢复等待的线程继续执行了。</font>  

#### 1.1.3.1. 类结构  
&emsp; CountDownLatch只有一个属性Sync，Sync是继承了AQS的内部类。  
&emsp; 创建CountDownLatch时传入一个count值，count值被赋值给AQS.state。  
&emsp; CountDownLatch是通过AQS共享锁实现的，AQS共享锁和独占锁原理只有很细微的区别，这里大致介绍下：  

* 线程调用acquireSharedInterruptibly()方法获取不到锁时，线程被构造成结点进入AQS阻塞队列。  
* 当有线程调用releaseShared()方法将当前线程持有的锁彻底释放后，会唤醒AQS阻塞队列中等锁的线程，如果AQS阻塞队列中有连续N个等待共享锁的线程，就将这N个线程依次唤醒。  

```java
public class CountDownLatch {
    private static final class Sync extends AbstractQueuedSynchronizer {
        Sync(int count) {
            setState(count);
        }
    }
    
    private final Sync sync;
    
    public CountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }
}
```

#### 1.1.3.2. await()  
&emsp; await()是将当前线程阻塞，理解await()的原理就是要弄清楚await()是如何将线程阻塞的。  
&emsp; await()调用的就是AQS获取共享锁的方法。当AQS.state=0时才能获取到锁，由于创建CountDownLatch时设置了state=count，此时是获取不到锁的，所以调用await()的线程挂起并构造成结点进入AQS阻塞队列。  

&emsp; <font color = "lime">创建CountDownLatch时设置AQS.state=count，可以理解成锁被重入了count次。await()方法获取锁时锁被占用了，只能阻塞。</font>  

```java
/**
 * CountDownLatch.await()调用的就是AQS获取共享锁的方法acquireSharedInterruptibly()
 */
public void await() throws InterruptedException {
    sync.acquireSharedInterruptibly(1);
}

/**
 * 获取共享锁
 * 如果获取锁失败，就将当前线程挂起，并将当前线程构造成结点加入阻塞队列
 * 判断是否获取锁成功的方法由CountDownLatch的内部类Sync实现
 */
public final void acquireSharedInterruptibly(int arg)
        throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
    if (tryAcquireShared(arg) < 0)          // 尝试获取锁的方法由CountDownLatch的内部类Sync实现
        doAcquireSharedInterruptibly(arg);  // 获取锁失败，就将当前线程挂起，并将当前线程构造成结点加入阻塞队列
}

/**
 * CountDownLatch.Sync实现AQS获取锁的方法
 * 只有AQS.state=0时获取锁成功。
 * 创建CountDownLatch时设置了state=count，调用await()时state不为0，返回-1，表示获取锁失败。
 */
protected int tryAcquireShared(int acquires) {
    return (getState() == 0) ? 1 : -1;
}
```

#### 1.1.3.3. countDown()  
&emsp; <font color = "red">countDown()方法是将count-1，如果发现count=0了，就唤醒阻塞的线程。</font>  
&emsp; countDown()调用AQS释放锁的方法，每次将state减1。当state减到0时是无锁状态了，就依次唤醒AQS队列中阻塞的线程来获取锁，继续执行逻辑代码。  

```java
/**
 *  CountDownLatch.await()调用的就是AQS释放共享锁的方法releaseShared()
 */
public void countDown() {
    sync.releaseShared(1);
}

/**
 * 释放锁
 * 如果锁被全部释放了，依次唤醒AQS队列中等待共享锁的线程
 * 锁全部释放指的是同一个线程重入了N次需要N次解锁，最终将state变回0
 * 具体释放锁的方法由CountDownLatch的内部类Sync实现
 */
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) { // 释放锁，由CountDownLatch的内部类Sync实现
        doReleaseShared();       // 锁全部释放之后，依次唤醒等待共享锁的线程
        return true;
    }
    return false;
}

/**
 * CountDownLatch.Sync实现AQS释放锁的方法
 * 释放一次，将state减1
 * 如果释放之后state=0，表示当前是无锁状态了，返回true
 */
protected boolean tryReleaseShared(int releases) {
    for (;;) {
        int c = getState();
        if (c == 0)
            return false;
        // state每次减1
        int nextc = c-1;
        if (compareAndSetState(c, nextc))
            return nextc == 0;// state=0时，无锁状态，返回true
    }
}
```

### 1.1.4. 总结  
&emsp; CountDownLatch用于一个线程A需要等待另外多个线程(B、C)执行后再执行的情况。  
&emsp; 创建CountDownLatch时设置一个计数器count，表示要等待的线程数量。线程A调用await()方法后将被阻塞，线程B和线程C调用countDown()之后计数器count减1。当计数器的值变为0时，就表示所有的线程均已经完成了任务，然后就可以恢复等待的线程A继续执行了。  
&emsp; <font color = "lime">CountDownLatch是由AQS实现的，创建CountDownLatch时设置计数器count其实就是设置AQS.state=count，也就是重入次数。await()方法调用获取锁的方法，由于AQS.state=count表示锁被占用且重入次数为count，所以获取不到锁线程被阻塞并进入AQS队列。countDown()方法调用释放锁的方法，每释放一次AQS.state减1，当AQS.state变为0时表示处于无锁状态了，就依次唤醒AQS队列中阻塞的线程来获取锁，继续执行逻辑代码。</font>  

## 1.2. CyclicBarrier，回环栅栏  
<!--
CyclicBarrier 使用详解
https://www.jianshu.com/p/333fd8faa56e

栅栏(Barrier)类似于闭锁，它能阻塞一组线程直到某个事件发生[CPJ4,4.3]。栅栏与闭锁 的关键区别在于，所有线程必须同时到达栅栏位置，才能继续执行。闭锁用于等待事件，而栅 栏用于等待其他线程。栅栏用于实现一些协议，例如几个家庭决定在某个地方集合：“所有人 6:00在麦当劳碰头，到了以后要等其他人，之后再讨论下一步要做的事情。”

JUC并发编程之CyclicBarrier源码
https://mp.weixin.qq.com/s/qfJy9hre0_Ax6444nn2XTQ
-->
&emsp; CyclicBarrier字面意思是回环栅栏， **<font color = "red">允许一组线程互相等待，直到到达某个公共屏障点 (common barrier point)之后，再全部同时执行。</font>** 叫做回环是因为当所有等待线程都被释放以后，CyclicBarrier可以被重用。  
&emsp; 应用场景： **<font color = "red"> CyclicBarrier适用于多线程结果合并的操作，用于多线程计算数据，最后合并计算结果的应用场景。</font>** <font color = "lime">比如需要统计多个Excel中的数据，然后等到一个总结果。</font>可以通过多线程处理每一个Excel，执行完成后得到相应的结果，最后通过barrierAction来计算这些线程的计算结果，得到所有Excel的总和。  

### 1.2.1. API  
&emsp; **构造函数：**  

```java
//创建对象的时候指定计算器大小
public CyclicBarrier(int parties) {
    this(parties, null);
}
//创建对象的时候指定计算器大小，在所有线程都运行到栅栏的时候，barrierAction会在其他线程恢复执行之前优先执行
public CyclicBarrier(int parties, Runnable barrierAction) {
    if (parties <= 0) throw new IllegalArgumentException();
    this.parties = parties;
    this.count = parties;
    this.barrierCommand = barrierAction;
}
```

&emsp; **成员方法：**  

```java
//返回计数器数值
public int getParties() {
    
}
//返回当前在栅栏处等待的线程数目
public int getNumberWaiting() {

}
//线程在调用处等待，直到与计数器数值相同数量的线程都到调用此方法，所有线程恢复执行
//用来挂起当前线程，直至所有线程都到达 barrier 状态再同时执行后续任务
public int await() throws InterruptedException, BrokenBarrierException {

}
//让这些线程等待至一定的时间，如果还有线程没有到达 barrier 状态就直接让到达barrier的线程执行后续任务。
public int await(long timeout, TimeUnit unit)

}
//移除栅栏。执行本操作后可以继续在其他线程中使用await操作
public void reset() {

}
//
public boolean isBroken() {

}
```

### 1.2.2. 实际使用场景  
&emsp; 多线程协作处理大量数据，结合AtomicReferenceFieldUpdater，await后处理结果。  

### 1.2.3. 使用示例  

```java
public class CyclicBarrierExample {


    public static void main(String[] args) {
        final int totalThread = 10;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(totalThread);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < totalThread; i++) {
            executorService.execute(() -> {
                System.out.print("before..");
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.print("after..");
            });
        }
        executorService.shutdown();
    }
}
//    before..before..before..before..before..before..before..before..before..before..after..after..after..after..after..after..after..after..after..after..
```

### 1.2.4. CycliBarriar和CountdownLatch有什么区别？  

* <font color = "red">CountDownLatch的作用是允许1或N个线程等待其他线程完成执行；而CyclicBarrier则是允许N个线程相互等待。</font>  
* CountDownLatch的计数器无法被重置；CyclicBarrier的计数器可以被重置后使用，因此它被称为是循环的barrier。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/tools-1.png)

### 1.2.5. ※※※CycliBarriar和CountdownLatch具体使用  
&emsp; [多线程处理大数据量](/docs/java/concurrent/bigData.md)    

## 1.3. Semaphore，信号量-控制同时访问的线程个数  
<!-- 
计数信号量(Counting Semaphore)用来控制同时访问某个特定资源的操作数量，或者同时 执行某个指定操作的数量[CPJ 3.4.1]。计数信号量还可以用来实现某种资源池，或者对容器施 加边界。
Semaphore中管理着一组虚拟的许可(permit),许可的初始数量可通过构造函数来指定。 在执行操作时可以首先获得许可(只要还有剩余的许可)，并在使用以后释放许可。如果没有 许可，那么acquire将阻塞直到有许可(或者直到被中断或者操作超时)。release方法将返回 一个许可给信号量。e计算信号量的一种简化形式是二值信号量，即初始值为1的Semaphore。 二值信号量可以用做互斥体(mutex),并具备不可重入的加锁语义:谁拥有这个唯一的许可，谁 就拥有了互斥锁。

Semaphore可以用于实现资源池，例如数据库连接池。我们可以构造一个固定长度的资源 池，当池为空时，请求资源将会失败，但你真正希望看到的行为是阻塞而不是失败，并且当池 非空时解除阻塞。如果将Semaphore的计数值初始化为池的大小，并在从池中获取一个资源 之前首先调用acquire方法获取一个许可，在将资源返回给池之后调用release释放许可，那么 acquire将一直阻塞直到资源池不为空。在第12章的有界缓冲类中将使用这项技术。(在构造阻 塞对象池时，一种更简单的方法是使用BlockingQueue来保存池的资源。)
同样，你也可以使用Semaphore将任何一种容器变成有界阻塞容器，如程序清单5-]4中 的BmmdedHashSet所示。信号量的计数值会初始化为容器容量的最大值。add操作在向底层容 器中添加一个元素之前，首先要获取一个许可。如果add操作没有添加任何元素，那么会立刻释放许可。同样，remove操作释放一个许可，使更多的元素能够添加到容器中。

-->

&emsp; Semaphore类，一个计数信号量。从概念上讲，信号量维护了一个许可集合。如有必要，在许可可用前会阻塞每一个acquire()，然后再获取该许可。每个 release()添加一个许可，从而可能释放一个正在阻塞的获取者。但是，不使用实际的许可对象，Semaphore只对可用许可的号码进行计数，并采取相应的行动。  
&emsp; 使用场景： **<font color = "red">Semaphore通常用于限制可以访问某些资源(物理或逻辑的)的线程数目。Semaphore可以用来构建一些对象池，资源池之类的， 比如数据库连接池。</font>**   

&emsp; **Semaphore与ReentrantLock：**  
&emsp; 信号量为多线程协作提供了更为强大的控制方法。信号量是对锁的扩展。无论是内部锁synchronized还是重入锁ReentrantLock，一次都允许一个线程访问一个资源，而信号量却可以指定多个线程同时访问某一个资源。  
&emsp; Semaphore基本能完成ReentrantLock的所有工作，使用方法也与之类似，通过acquire()与release()方法来获得和释放临界资源。经实测，Semaphone.acquire()方法默认为可响应中断锁，与ReentrantLock.lockInterruptibly()作用效果一致，也就是说在等待临界资源的过程中可以被Thread.interrupt()方法中断。  
&emsp; 此外，Semaphore 也实现了可轮询的锁请求与定时锁的功能，除了方法名tryAcquire与tryLock不同，其使用方法与ReentrantLock几乎一致。Semaphore也提供了公平与非公平锁的机制，也可在构造函数中进行设定。  
&emsp; Semaphore的锁释放操作也由手动进行，因此与ReentrantLock一样，为避免线程因抛出异常而无法正常释放锁的情况发生，释放锁的操作也必须在finally代码块中完成。  

### 1.3.1. API  
&emsp; **构造函数：**  

```java
//创建具有给定的许可数和非公平的公平设置的Semaphore。
public Semaphore(int permits) {
    sync = new NonfairSync(permits);
}
//创建具有给定的许可数和给定的公平设置的Semaphore
public Semaphore(int permits, boolean fair) {
    sync = fair ? new FairSync(permits) : new NonfairSync(permits);
}
```

&emsp; **成员方法：**  

```java
//用来获取一个许可，若无许可能够获得，则会一直等待，直到获得许可。
public void acquire() throws InterruptedException
//获取 permits 个许可
public void acquire(int permits)
// 类似于acquire()，但是不会响应中断。
public void acquireUninterruptibly()
// 尝试获取，如果成功则为true，否则false。这个方法不会等待，立即返回。
public boolean tryAcquire()
//尝试获取permits个许可，若在指定的时间内获取成功，则立即返回true，否则则立即返回false
public boolean tryAcquire(int permits, long timeout, TimeUnit unit)
//尝试获取一个许可，若在指定的时间内获取成功，则立即返回 true，否则则立即返回 false
public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException
//用于在现场访问资源结束后，释放一个许可，以使其他等待许可的线程可以进行资源访问。
public void release()
```

### 1.3.2. 使用示例  
&emsp; JDK文档中提供使用信号量的实例。这个实例很好的解释了如何通过信号量控制资源访问。  

```java
public class Pool {
    private static final int MAX_AVAILABLE = 100;
    private final Semaphore available = new Semaphore(MAX_AVAILABLE, true);
    public Object getItem() throws InterruptedException {
        available.acquire();
        // 申请一个许可
        // 同时只能有100个线程进入取得可用项，
        // 超过100个则需要等待
        return getNextAvailableItem();
    }

    public void putItem(Object x) {
        // 将给定项放回池内，标记为未被使用
        if (markAsUnused(x)) {
            available.release();
            // 新增了一个可用项，释放一个许可，请求资源的线程被激活一个
        }
    }

    // 仅作示例参考，非真实数据
    protected Object[] items = new Object[MAX_AVAILABLE]; // 用于对象池复用对象
    protected boolean[] used = new boolean[MAX_AVAILABLE]; // 标记作用

    protected synchronized Object getNextAvailableItem() {
        for (int i = 0; i < MAX_AVAILABLE; ++i) {
            if (!used[i]) {
                used[i] = true;
                return items[i];
            }
        }
        return null;
    }

    protected synchronized boolean markAsUnused(Object item) {
        for (int i = 0; i < MAX_AVAILABLE; ++i) {
            if (item == items[i]) {
                if (used[i]) {
                    used[i] = false;
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }
}
```
&emsp; 此实例简单实现了一个对象池，对象池最大容量为100。因此，当同时有100个对象请求时，对象池就会出现资源短缺，未能获得资源的线程就需要等待。当某个线程使用对象完毕后，就需要将对象返回给对象池。此时，由于可用资源增加，因此，可以激活一个等待该资源的线程。  

## 1.4. Exchanger，交换器  

<!-- 
另一种形式的栅栏是Exchanger,它是一种两方(Two-Party)栅栏，各方在栅栏位置上交 换数据[CPJ 3.4.3]。当两方执行不对称的操作时，Exchanger会非常有用，例如当一个线程向缓 冲区写入数据，而另一个线程从缓冲区中读取数据。这些线程可以使用Exchanger来汇合，并 将满的缓冲区与空的缓冲区交换。当两个线程通过Exchanger交换对象时，这种交换就把这两 个对象安全地发布给另一方。
数据交换的时机取决于应用程序的响应需求。最简草的方案是，当缓冲区被填满时， 由填充任务进行交换，当缓冲区为空时，由清空任务进行交换/这样会把需要交换的次数 降至最低，但如果新数据的到达率不可预测，那么一些数据的处理过程就将延迟。另一个 方法是，不仅当缓冲被填满时进行交换，并且当缓冲被填充到一定程度并保持一定时间后， 也进行交换。
-->

&emsp; <font color = "lime">Exchanger是一个用于线程间协作的工具类，用于两个线程间交换。Exchanger提供了一个交换的同步点，在这个同步点两个线程能够交换数据。</font>  

&emsp; 具体交换数据是通过exchange()方法来实现的，如果一个线程先执行exchange方法，那么它会同步等待另一个线程也执行exchange方法，这个时候两个线程就都达到了同步点，两个线程就可以交换数据。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-34.png)  

&emsp; 用一个简单的例子来看下Exchanger的具体使用。两方做交易，如果一方先到要等另一方也到了才能交易，交易就是执行exchange方法交换数据。  

```java
public class ExchangerTest {
    private static Exchanger<String> exchanger = new Exchanger<String>();
    static String goods = "电脑";
    static String money = "$1000";

    public static void main(String[] args) throws InterruptedException {
        System.out.println("准备交易，一手交钱一手交货...");
        // 卖家
        new Thread() {
            public void run() {
                System.out.println(getName() + " 卖家到了，已经准备好货：" + goods);
                try {
                    String money = exchanger.exchange(goods);
                    System.out.println(getName() + " 卖家收到钱：" + money);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.start();
        Thread.sleep(3000);
        // 买家
        new Thread() {
            public void run() {
                try {
                    System.out.println(getName() + " 买家到了，已经准备好钱：" + money);
                    String goods = exchanger.exchange(money);
                    System.out.println(getName() + " 买家收到货：" + goods);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }
}
```

&emsp; 输出结果：  

```java
准备交易，一手交钱一手交货...
Thread-0 卖家到了，已经准备好货：电脑
Thread-1 买家到了，已经准备好钱：$1000
Thread-1 买家收到货：电脑
Thread-0 卖家收到钱：$1000
```

 