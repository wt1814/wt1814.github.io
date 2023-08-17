
<!-- TOC -->

- [1. ~~CountDownLatch，线程计数器~~](#1-countdownlatch线程计数器)
    - [1.1. API](#11-api)
    - [1.2. 使用示例](#12-使用示例)
    - [1.3. CountDownLatch源码分析](#13-countdownlatch源码分析)
        - [1.3.0.1. 类结构](#1301-类结构)
        - [1.3.1. await()](#131-await)
        - [1.3.2. countDown()](#132-countdown)
    - [1.4. 总结](#14-总结)
    - [CountDownLatch场景使用](#countdownlatch场景使用)

<!-- /TOC -->

&emsp; **<font color = "red">~~总结：~~</font>**  
1. java.util.concurrent.CountDownLatch类， **<font color = "red">能够使一个线程等待其他线程完成各自的工作后再执行。</font>**    
&emsp; CountDownLatch中count down是倒数的意思，latch则是门闩的含义。整体含义可以理解为倒数的门栓，似乎有一点“三二一，芝麻开门”的感觉。CountDownLatch的作用也是如此，在构造CountDownLatch的时候需要传入一个整数n，在这个整数“倒数”到0之前，主线程需要等待在门口，而这个“倒数”过程则是由各个执行线程驱动的，每个线程执行完一个任务“倒数”一次。总结来说，CountDownLatch的作用就是等待其他的线程都执行完任务，必要时可以对各个任务的执行结果进行汇总，然后主线程才继续往下执行。  
2. CountDownLatch的原理：  
    1. <font color = "clime">CountDownLatch是由AQS实现的，创建CountDownLatch时设置计数器count其实就是设置AQS.state=count，也就是重入次数。  
        * await()方法调用获取锁的方法，由于AQS.state=count表示锁被占用且重入次数为count，所以获取不到锁线程被阻塞并进入AQS队列。  
        * countDown()方法调用释放锁的方法，每释放一次AQS.state减1，当AQS.state变为0时表示处于无锁状态了，就依次唤醒AQS队列中阻塞的线程来获取锁，继续执行逻辑代码。</font>  
        &emsp; 如果发现count=0了，就唤醒</font><font color = "blue">阻塞的主线程。</font>
    2. <font color = "red">CountDownLatch对象不能被重复利用，也就是不能修改计数器的值。</font>CountDownLatch是一次性的，计数器的值只能在构造方法中初始化一次，之后没有任何机制再次对其设置值，当CountDownLatch使用完毕后，它不能再次被使用。    
3. CountDownLatch的使用：  
    1. java.util.concurrent.CountDownLatch类， **<font color = "red">能够使一个线程等待其他线程完成各自的工作后再执行。</font>** <font color = "red">利用它可以实现类似计数器的功能。</font><font color = "blue">比如有一个任务A，它要等待其他4个任务执行完毕之后才能执行，此时就可以利用CountDownLatch来实现这种功能了。</font>  
    2. CountDownLatch的典型应用场景，大体可分为两类：结束信号、开始信号。  
    &emsp; 主线程创建、启动N个异步任务，期望当这N个任务全部执行完毕结束后，主线程才可以继续往下执行。即将CountDownLatch作为任务的结束信号来使用。   


# 1. ~~CountDownLatch，线程计数器~~  
&emsp; CountDownLatch做为jdk提供的多线程同步工具，CountDownLatch其实本质上可以看做一个线程计数器，统计多个线程执行完成的情况，适用于控制一个或多个线程等待，直到所有线程都执行完毕的场景，因此可以利用其功能特点实现获取多个线程的执行结果。  
  
<!-- 
https://mp.weixin.qq.com/s/jC5CdNS4n16JigFACV7CAw
-->
<!-- 
CountDownLatch实践
https://mp.weixin.qq.com/s/wDxfDcbJCQs99huLyztnCQ

https://mp.weixin.qq.com/s/JynLduej5FHPTmKf6nhsOA

https://www.jianshu.com/p/128476015902
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
&emsp; java.util.concurrent.CountDownLatch类， **<font color = "red">能够使一个线程等待其他线程完成各自的工作后再执行。</font>** <font color = "red">利用它可以实现类似计数器的功能。比如有一个任务A，它要等待其他4个任务执行完毕之后才能执行，此时就可以利用CountDownLatch来实现这种功能了。</font>  
&emsp; **<font color = "clime">CountDownLatch的操作是原子操作，同时只能有一个线程去操作这个计数器，也就是同时只能有一个线程去减这个计数器里面的值。</font>** 可以向CountDownLatch对象设置一个初始的数字作为计数值，任何调用这个对象上的await()方法都会阻塞，直到这个计数器的计数值被其他的线程减为0为止。所以在当前计数到达零之前，await方法会一直受阻塞。之后，会释放所有等待的线程，await的所有后续调用都将立即返回。这种现象只出现一次——计数无法被重置。如果需要重置计数，请考虑使用CyclicBarrier。  
    
&emsp; **注意：**  
1. <font color = "red">CountDownLatch对象不能被重复利用，也就是不能修改计数器的值。</font>CountDownLatch是一次性的，计数器的值只能在构造方法中初始化一次，之后没有任何机制再次对其设置值，当CountDownLatch使用完毕后，它不能再次被使用。    
2. CountDownLatch代表的计数器的大小可以为0，意味着在一个线程调用await方法时会立即返回。  
3. 如果某些线程中有阻塞操作的话，最好使用带有超时时间的await方法，以免该线程调用await方法之后永远得不到执行。  

&emsp; **CountDownLatch与Thread的join()方法方法对比：**  
&emsp; Thread的join方法，这个方法表示一个线程将等待另一个线程执行完才能继续执行。  
&emsp; CountDownLatch代表的是一个计数器，不论是否在同一线程中，不论线程是否执行完成，都可以随时随地调用CountDownLatch的countDown方法，而Thread的成员方法join只能在一个线程中对另一个线程对象调用，而且方法返回的前提是线程已经执行完成。  
&emsp; 所以使用CountDownLatch会比join方法更灵活。  

## 1.1. API  
![image](http://182.92.69.8:8081/img/java/concurrent/concurrent-8.png)  
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

## 1.2. 使用示例  
&emsp; CountDownLatch的典型应用场景，大体可分为两类：结束信号、开始信号。  
&emsp; 主线程创建、启动N个异步任务，期望当这N个任务全部执行完毕结束后，主线程才可以继续往下执行。即将CountDownLatch作为任务的结束信号来使用。  
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

```text
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

## 1.3. CountDownLatch源码分析  
<!-- 
一文搞懂 CountDownLatch 用法和源码！
https://mp.weixin.qq.com/s/m60Tq18Vbp5ml-qAp505VQ
-->
&emsp; CountDownLatch是通过一个计数器来实现的，在new 一个CountDownLatch对象的时候需要带入该计数器值，该值就表示了线程的数量。<font color = "red">每当一个线程完成自己的任务后，计数器的值就会减1。当计数器的值变为0时，就表示所有的线程均已经完成了任务，然后就可以恢复等待的线程继续执行了。</font>  

### 1.3.0.1. 类结构  
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

### 1.3.1. await()  
&emsp; await()是将当前线程阻塞，理解await()的原理就是要弄清楚await()是如何将线程阻塞的。  
&emsp; await()调用的就是AQS获取共享锁的方法。当AQS.state=0时才能获取到锁，由于创建CountDownLatch时设置了state=count，此时是获取不到锁的，所以调用await()的线程挂起并构造成结点进入AQS阻塞队列。  

&emsp; <font color = "clime">创建CountDownLatch时设置AQS.state=count，可以理解成锁被重入了count次。await()方法获取锁时锁被占用了，只能阻塞。</font>  

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

### 1.3.2. countDown()  
&emsp; **<font color = "clime">countDown()方法是将count-1，如果发现count=0了，就唤醒阻塞的线程。</font>**  
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

## 1.4. 总结  
&emsp; CountDownLatch用于一个线程A需要等待另外多个线程(B、C)执行后再执行的情况。  
&emsp; 创建CountDownLatch时设置一个计数器count，表示要等待的线程数量。线程A调用await()方法后将被阻塞，线程B和线程C调用countDown()之后计数器count减1。当计数器的值变为0时，就表示所有的线程均已经完成了任务，然后就可以恢复等待的线程A继续执行了。  
&emsp; <font color = "clime">CountDownLatch是由AQS实现的，创建CountDownLatch时设置计数器count其实就是设置AQS.state=count，也就是重入次数。await()方法调用获取锁的方法，由于AQS.state=count表示锁被占用且重入次数为count，所以获取不到锁线程被阻塞并进入AQS队列。countDown()方法调用释放锁的方法，每释放一次AQS.state减1，当AQS.state变为0时表示处于无锁状态了，就依次唤醒AQS队列中阻塞的线程来获取锁，继续执行逻辑代码。</font>  



------------------

## CountDownLatch场景使用
场景一：让多个线程等待：模拟并发，让并发线程一起执行CountDownLatch充当的是一个发令枪的角色，比如田径赛跑时，运动员会在起跑线做准备动作，等到发令枪一声响，运动员就会奋力奔跑。   

```java
CountDownLatch countDownLatch = new CountDownLatch(1);
for (int i = 0; i < 5; i++) {
    new Thread(() -> {
        try {
            //准备完毕……运动员都阻塞在这，等待号令
            countDownLatch.await();
            String parter = "【" + Thread.currentThread().getName() + "】";
            System.out.println(parter + "开始执行……");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }).start();
}

Thread.sleep(2000);// 裁判准备发令
System.out.println("裁判发令开始");
countDownLatch.countDown();// 发令枪：执行发令
```

执行结果  

```java
裁判发令开始
【Thread-1】开始执行……
【Thread-3】开始执行……
【Thread-0】开始执行……
【Thread-2】开始执行……
【Thread-4】开始执行……
```


场景二：让单个线程等待：多个线程(任务)完成后，进行汇总合并  
很多时候，的并发任务，存在前后依赖关系；比如数据详情页需要同时调用多个接口获取数据，并发请求获取到数据后、需要进行结果合并；或者多个数据操作完成后，需要数据check；  
这其实都是：在多个线程(任务)完成后，进行汇总合并的场景。  

```java
CountDownLatch countDownLatch = new CountDownLatch(5);
for (int i = 0; i < 5; i++) {
    final int index = i;
    new Thread(() -> {
        try {
            Thread.sleep(1000 + ThreadLocalRandom.current().nextInt(1000));
            System.out.println("完成任务" + index + Thread.currentThread().getName());
            countDownLatch.countDown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }).start();
}

countDownLatch.await();// 主线程在阻塞，当计数器==0，就唤醒主线程往下执行。
System.out.println("主线程:在所有任务运行完成后，进行结果汇总");
```

执行结果  

```java
完成任务2Thread-2
完成任务3Thread-3
完成任务1Thread-1
完成任务0Thread-0
完成任务4Thread-4
主线程:在所有任务运行完成后，进行结果汇总
```



