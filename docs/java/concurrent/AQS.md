<!-- TOC -->

- [1. AQS](#1-aqs)
    - [1.1. 简介](#11-简介)
    - [1.2. 类图：](#12-类图)
    - [1.3. 属性](#13-属性)
        - [1.3.1. 同步状态state](#131-同步状态state)
        - [1.3.2. 同步队列](#132-同步队列)
            - [1.3.2.1. 队列描述](#1321-队列描述)
            - [1.3.2.2. 入列以及出列动作](#1322-入列以及出列动作)
                - [1.3.2.2.1. 入列](#13221-入列)
                - [1.3.2.2.2. 出列](#13222-出列)
        - [1.3.3. 等待队列](#133-等待队列)
    - [1.4. 成员方法](#14-成员方法)
        - [1.4.1. 独占模式](#141-独占模式)
            - [1.4.1.1. 获取同步状态--acquire()](#1411-获取同步状态--acquire)
            - [1.4.1.2. 释放同步状态--release()](#1412-释放同步状态--release)
        - [1.4.2. 共享模式](#142-共享模式)
            - [1.4.2.1. 获取同步状态--acquireShared-1](#1421-获取同步状态--acquireshared-1)
            - [1.4.2.2. 释放同步状态--releaseShared](#1422-释放同步状态--releaseshared)
        - [1.4.3. AQS的模板方法设计模式，自定义同步器](#143-aqs的模板方法设计模式自定义同步器)

<!-- /TOC -->

&emsp; <font color = "blue">本节重点概况：</font>  
1. 同步状态，通过state控制同步状态
2. 同步队列，双向链表，每个节点代表一个线程，节点有5个状态。入列采用CAS算法设置尾节点+死循环自旋。  
3. 独占模式下，获取同步状态、释放同步状态
4. 共享模式下，获取同步状态、释放同步状态

# 1. AQS  
&emsp; AQS是AbstractQueuedSynchronizer的简称，翻译成中文就是抽象队列同步器 ，这三个单词分开来看：  

* Abstract (抽象)：AQS 是一个抽象类，只实现一些主要的逻辑，有些方法推迟到子类实现；
* Queued (队列)：AQS 是用先进先出队列来存储数据的；
* Synchronizer (同步)：即AQS实现同步功能；

## 1.1. 简介  
&emsp; **<font color = "red">AQS是JUC并发包中的核心基础组件。它是构建锁或者其他同步组件(如ReentrantLock、ReentrantReadWriteLock、Semaphore等)的基础框架。</font>**  
1. **<font color = "lime">内部实现的关键是：先进先出的队列、state同步状态</font>**  
2. **<font color = "lime">拥有两种线程模式：独占模式、共享模式。</font>**  
    * 独占式：有且只有一个线程能获取到锁，如：ReentrantLock。又可分为公平锁和非公平锁：
        * 公平锁：按照线程在队列中的排队顺序，先到者先拿到锁。  
        * 非公平锁：当线程要获取锁时，无视队列顺序直接去抢锁，谁抢到就是谁的。    
    * 共享式：可以多个线程同时获取到锁，如：Semaphore/CountDownLatch。   

## 1.2. 类图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-14.png)   

## 1.3. 属性  
&emsp; AQS核心思想是，<font color = "red">如果被请求的共享资源空闲，则将当前请求资源的线程设置为有效的工作线程，并且将共享资源设置为锁定状态。</font><font color = "lime">如果被请求的共享资源被占用，那么就需要一套线程阻塞等待以及被唤醒时锁分配的机制。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-2.png)   
&emsp; <font color = "red">AQS使用一个int state成员变量来表示同步状态，通过内置的FIFO队列来完成获取资源线程的排队工作，即将暂时获取不到锁的线程加入到队列中。AQS使用CAS对该同步状态进行原子操作实现对其值的修改。</font>  

### 1.3.1. 同步状态state  

```java
//AQS使用一个int类型的成员变量state来表示同步状态，是由volatile修饰的。当state>0时表示已经获取了锁，当state = 0时表示释放了锁。
private volatile int state;
//获取state值
protected final int getState() {
    return state;
}
//设置state值
protected final void setState(int newState) {
    state = newState;
}
/**使用CAS设置当前状态，该方法能够保证状态设置的原子性；*/
protected final boolean compareAndSetState(int expect, int update) {
    // See below for intrinsics setup to support this
    return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
}
```
&emsp; 使用int类型的成员变量state来控制同步状态。  

* 是由volatile修饰的，保证多线程中的可见性。  
* 并且提供了几个访问这个字段的方法：getState()、setState、compareAndSetState。这几个方法都是final修饰的，说明子类中无法重写它们。另外它们都是protected修饰的，说明只能在子类中使用这些方法。  

&emsp; **<font color = "red">怎么通过state控制同步状态？</font>**  
&emsp; 通过修改state字段代表的同步状态来实现多线程的独占模式或者共享模式。例如：当state=0时，则说明没有任何线程占有共享资源的锁，当state=1时，则说明有线程目前正在使用共享变量，其他线程必须加入同步队列进行等待。  
&emsp; <font color = "red">在独占模式下，可以把state的初始值设置成0，每当某个线程要进行某项独占操作前，都需要判断state的值是不是0，如果不是0的话意味着别的线程已经进入该操作，则本线程需要阻塞等待；如果是0的话就把state的值设置成1，自己进入该操作。这个先判断再设置的过程可以通过CAS操作保证原子性，把这个过程称为尝试获取同步状态。如果一个线程获取同步状态成功了，那么在另一个线程尝试获取同步状态的时候发现state的值已经是1了就一直阻塞等待，直到获取同步状态成功的线程执行完了需要同步的操作后释放同步状态，也就是把state的值设置为0，并通知后续等待的线程。ReentrantLock 允许重入，所以同一个线程多次获得同步锁的时候，state 会递增，比如重入 5 次，那么 state=5。而在释放锁的时候，同样需要释放5次直到state=0其他线程才有资格获得锁。</font>  
&emsp; 在共享模式下的道理也差不多，比如说某项操作允许10个线程同时进行，超过这个数量的线程就需要阻塞等待。那么就可以把state的初始值设置为10，一个线程尝试获取同步状态的意思就是先判断state的值是否大于0，如果不大于0的话意味着当前已经有10个线程在同时执行该操作，本线程需要阻塞等待；如果state的值大于0，那么可以把state的值减1后进入该操作，每当一个线程完成操作的时候需要释放同步状态，也就是把state的值加1，并通知后续等待的线程。  

### 1.3.2. 同步队列 
&emsp; **<font color = "lime">一句话概述：双向链表，每个节点代表一个线程，节点有5个状态。入列采用CAS算法设置尾节点+死循环自旋。</font>**

#### 1.3.2.1. 队列描述  
&emsp; <font color = "red">AQS队列在内部维护了一个先进先出FIFO的双向链表，</font>在双向链表中，每个节点都有两个指针，分别指向直接前驱节点和直接后继节点。使用双向链表的优点之一，就是从任意一个节点开始都很容易访问它的前驱节点和后继节点。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-18.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-33.png)  

```java
/*等待队列的队首结点(懒加载，这里体现为竞争失败的情况下，加入同步队列的线程执行到enq方法的时候会创
建一个Head结点)。该结点只能被setHead方法修改。并且结点的waitStatus不能为CANCELLED*/
private transient volatile Node head;
/**等待队列的尾节点，也是懒加载的。(enq方法)。只在加入新的阻塞结点的情况下修改*/
private transient volatile Node tail;
```
&emsp; <font color = "red">CLH同步队列中，一个Node 节点表示一个线程，</font>它保存着线程的引用(thread)、状态(waitStatus)、前驱节点(prev)、后继节点(next)，condition队列的后续节点(nextWaiter)如下图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-16.png)  

```java
static final class Node {
    //共享模式
    static final Node SHARED = new Node();
    //独占模式
    static final Node EXCLUSIVE = null;
    //因为超时或者中断，节点会被设置为取消状态，被取消的节点时不会参与到竞争中的，它会一直保持取消状态不会转变为其他状态；
    static final int CANCELLED =  1;
    //后继节点的线程处于等待状态，而当前节点的线程如果释放了同步状态或者被取消，将会通知后继节点，使后继节点的线程得以运行
    static final int SIGNAL    = -1;
    //节点在等待队列中，节点线程等待在Condition上，当其他线程对Condition调用了signal()后，改节点将会从等待队列中转移到同步队列中，加入到同步状态的获取中
    static final int CONDITION = -2;
    //表示下一次共享式同步状态获取将会无条件地传播下去
    static final int PROPAGATE = -3;
    //等待状态
    volatile int waitStatus;
    //前驱节点
    volatile Node prev;
    //后继节点
    volatile Node next;
    //当前节点的线程
    volatile Thread thread;
}
```

<!-- 
```java
static final class Node {
    /** Marker to indicate a node is waiting in shared mode */
    // 标记一个节点,在 共享模式 下等待
    static final Node SHARED = new Node();
 
    /** Marker to indicate a node is waiting in exclusive mode */
    // 标记一个节点,在 独占模式 下等待
    static final Node EXCLUSIVE = null;

    /** waitStatus value to indicate thread has cancelled */
    // waitStatus 的值,表示该节点从队列中取消
    static final int CANCELLED =  1;
 
    /** waitStatus value to indicate successor's thread needs unparking */
    // waitStatus 的值,表示后继节点在等待唤醒
    // 只有处于 signal 状态的节点,才能被唤醒
    static final int SIGNAL    = -1;
 
    /** waitStatus value to indicate thread is waiting on condition */
    // waitStatus 的值,表示该节点在等待一些条件
    static final int CONDITION = -2;
 
 /**
     * waitStatus value to indicate the next acquireShared should
     * unconditionally propagate
    */
    // waitStatus 的值,表示有资源可以使用,新 head 节点需要唤醒后继节点
    // 如果是在共享模式下,同步状态应该无条件传播下去
    static final int PROPAGATE = -3;

 // 节点状态,取值为 -3,-2,-1,0,1
    volatile int waitStatus;

 // 前驱节点
    volatile Node prev;

 // 后继节点
    volatile Node next;

 // 节点所对应的线程
    volatile Thread thread;

 // condition 队列中的后继节点
    Node nextWaiter;

 // 判断是否是共享模式
    final boolean isShared() {
        return nextWaiter == SHARED;
    }

 /**
 * 返回前驱节点
 */
    final Node predecessor() throws NullPointerException {
        Node p = prev;
        if (p == null)
            throw new NullPointerException();
        else
            return p;
    }

    Node() {    // Used to establish initial head or SHARED marker
    }

 /**
 * 将线程构造成一个 Node 节点,然后添加到 condition 队列中
 */
    Node(Thread thread, Node mode) {     // Used by addWaiter
        this.nextWaiter = mode;
        this.thread = thread;
    }

 /**
 * 等待队列用到的方法
 */
    Node(Thread thread, int waitStatus) { // Used by Condition
        this.waitStatus = waitStatus;
        this.thread = thread;
    }
}
```
-->
&emsp; **结点状态waitStatus：** Node结点是对每一个等待获取资源的线程的封装，其包含了需要同步的线程本身及其等待状态，如是否被阻塞、是否等待唤醒、是否已经被取消等。变量waitStatus则表示当前Node结点的等待状态，共有5种取值CANCELLED、SIGNAL、CONDITION、PROPAGATE、0。  

* CANCELLED(1)：表示当前结点已取消调度。当timeout或被中断(响应中断的情况下)，会触发变更为此状态，进入该状态后的结点将不会再变化。  
* SIGNAL(-1)：表示后继结点在等待当前结点唤醒。后继结点入队时，会将前继结点的状态更新为SIGNAL。  
* CONDITION(-2)：表示结点等待在Condition上，当其他线程调用了Condition的signal()方法后，CONDITION状态的结点将从等待队列转移到同步队列中，等待获取同步锁。  
* PROPAGATE(-3)：共享模式下，前继结点不仅会唤醒其后继结点，同时也可能会唤醒后继的后继结点。  
* 0：新结点入队时的默认状态。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-17.png)  
&emsp; 注意，负值表示结点处于有效等待状态，而正值表示结点已被取消。所以源码中很多地方用>0、<0来判断结点的状态是否正常。  

#### 1.3.2.2. 入列以及出列动作
&emsp; 每个Node是一个线程封装。<font color = "red">在AQS中，当线程在竞争锁失败之后，会封装成Node加入到AQS队列尾部，首节点是获取同步状态成功的节点。</font>  

##### 1.3.2.2.1. 入列  
&emsp; 未获取到锁的线程会创建节点，线程安全(CAS算法设置尾节点+死循环自旋)的加入队列尾部。   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-19.png)  
&emsp; CLH队列入列就是tail指向新节点、新节点的prev指向当前最后的节点，当前最后一个节点的next指向当前节点。addWaiter方法如下：  

1. 将当前线程封装成Node  
2. <font color = "lime">当前链表中的tail节点是否为空，如果不为空，则通过cas操作把当前线程的node添加到AQS队列</font>  
3. <font color = "lime">如果为空或者cas失败，调用enq将节点添加到AQS队列</font>  

```java
/**
 * 构造Node
 */
private Node addWaiter(Node mode) {
    Node node = new Node(Thread.currentThread(), mode);
    // Try the fast path of enq; backup to full enq on failure
    // 快速尝试添加尾节点
    Node pred = tail;
    if (pred != null) {
        node.prev = pred;
        // CAS设置尾节点
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    //多次尝试
    enq(node);
    return node;
}
```
&emsp; addWaiter设置尾节点失败的话，调用enq(Node node)方法设置尾节点，<font color = "red">enq就是通过自旋操作把当前节点加入到队列中。</font>  
&emsp; enq方法如下：  

```java
private Node enq(final Node node) {
    //死循环尝试，直到成功为止
    for (;;) {
        Node t = tail;
        //tail 不存在，设置为首节点
        if (t == null) { // Must initialize
            if (compareAndSetHead(new Node()))
                tail = head;
        } else {
            node.prev = t;
            if (compareAndSetTail(t, node)) {
                t.next = node;
                return t;
            }
        }
    }
}
```

##### 1.3.2.2.2. 出列  
&emsp; 首节点的线程释放同步状态后，将会唤醒它的后继节点(next)，而后继节点将会在获取同步状态成功时将自己设置为首节点。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-24.png)  

```java
Node h = head;
if (h != null && h.waitStatus != 0)
unparkSuccessor(h);
```

```java
private void unparkSuccessor(Node node) {
    //获取wait状态
    int ws = node.waitStatus;
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);// 将等待状态waitStatus设置为初始值0
    Node s = node.next;//后继结点
    if (s == null || s.waitStatus > 0) {//若后继结点为空，或状态为CANCEL(已失效)，则从后尾部往前遍历找到一个处于正常阻塞状态的结点进行唤醒
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread);//使用LockSupprot唤醒结点对应的线程
}
```

### 1.3.3. 等待队列
<!-- 
https://mp.weixin.qq.com/s/WEV7fqPnyurDtSqMF0S2Wg
-->
&emsp; synchronized控制同步的时候，可以配合Object的wait()、notify()，notifyAll() 系列方法可以实现等待/通知模式。而Lock提供了条件Condition接口，配合await()，signal()，signalAll() 等方法也可以实现等待/通知机制。  
<!-- 
ConditionObject实现了Condition接口，给AQS提供条件变量的支持 。  

&emsp; Condition队列与CLH队列：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-15.png)  

&emsp; ConditionObject队列与CLH队列的关系：  

* 调用了await()方法的线程，会被加入到conditionObject等待队列中，并且唤醒CLH队列中head节点的下一个节点。
* 线程在某个ConditionObject对象上调用了singnal()方法后，等待队列中的firstWaiter会被加入到AQS的CLH队列中，等待被唤醒。
* 当线程调用unLock()方法释放锁时，CLH队列中的head节点的下一个节点(在本例中是firtWaiter)，会被唤醒。

&emsp; 区别：  

* ConditionObject对象都维护了一个单独的等待队列 ，AQS所维护的CLH队列是同步队列，它们节点类型相同，都是Node。  

-->

## 1.4. 成员方法  
<!--
https://www.cnblogs.com/chengxiao/archive/2017/07/24/7141160.html
https://www.cnblogs.com/waterystone/p/4920797.html
-->

&emsp; AQS定义两种资源共享方式：独占Exclusive和共享Share。独占模式和共享模式下在什么情况下会往CLH同步队列里添加节点，什么情况下会从CLH同步队列里移除节点，以及线程阻塞和恢复的实现细节？  

* 独占式：有且只有一个线程能获取到锁，如：ReentrantLock。  
* 共享式：可以多个线程同时获取到锁，如：CountDownLatch。  

### 1.4.1. 独占模式
#### 1.4.1.1. 获取同步状态--acquire()
&emsp; **<font color = "red">每个节点自旋观察自己的前一节点是不是Header节点，如果是，就去尝试获取锁。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-20.png)  
&emsp; acquire(int arg)是独占模式下线程获取同步状态的顶层入口。  
&emsp; <font color = "lime">独占模式获取同步状态流程如下：</font>  
1. 调用使用者重写的tryAcquire方法，tryAcquire()尝试直接去获取资源，如果成功则直接返回(这里体现了非公平锁，每个线程获取锁时会尝试直接抢占加锁一次，而CLH队列中可能还有别的线程在等待)；  
2. addWaiter()将该线程加入等待队列的尾部，并标记为独占模式；  
3. acquireQueued()使线程阻塞在等待队列中获取资源，一直获取到资源后才返回。如果在整个等待过程中被中断过，则返回true，否则返回false。
4. 如果线程在等待过程中被中断过，它是不响应的。只是获取资源后才再进行自我中断selfInterrupt()，将中断补上。

        如果获取到资源，线程直接返回，否则进入等待队列，直到获取到资源为止，且整个过程忽略中断的影响。这也正是lock()的语义，当然不仅仅只限于lock()。获取到资源后，线程就可以去执行其临界区代码了。 

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-31.png)  
<!-- 
　a.首先，调用使用者重写的tryAcquire方法，若返回true，意味着获取同步状态成功，后面的逻辑不再执行；若返回false，也就是获取同步状态失败，进入b步骤；
　　　　b.此时，获取同步状态失败，构造独占式同步结点，通过addWatiter将此结点添加到同步队列的尾部(此时可能会有多个线程结点试图加入同步队列尾部，需要以线程安全的方  式添加)；
　　　　c.该结点以在队列中尝试获取同步状态，若获取不到，则阻塞结点线程，直到被前驱结点唤醒或者被中断。

-->
&emsp; 源码解析：  

```java
public final void acquire(long arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

```java
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;//标记是否成功拿到资源
    try {
        boolean interrupted = false;//标记等待过程中是否被中断过

        //又是一个“自旋”！
        for (;;) {
            final Node p = node.predecessor();//拿到前驱
            //如果前驱是head，即该结点已成老二，那么便有资格去尝试获取资源(可能是老大释放完资源唤醒自己的，当然也可能被interrupt了)。
            if (p == head && tryAcquire(arg)) {
                setHead(node);//拿到资源后，将head指向该结点。所以head所指的标杆结点，就是当前获取到资源的那个结点或null。
                p.next = null; // setHead中node.prev已置为null，此处再将head.next置为null，就是为了方便GC回收以前的head结点。也就意味着之前拿完资源的结点出队了！
                failed = false; // 成功获取资源
                return interrupted;//返回等待过程中是否被中断过
            }

            //如果自己可以休息了，就通过park()进入waiting状态，直到被unpark()。如果不可中断的情况下被中断了，那么会从park()中醒过来，发现拿不到资源，从而继续进入park()等待。
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;//如果等待过程中被中断过，哪怕只有那么一次，就将interrupted标记为true
        }
    } finally {
        if (failed) // 如果等待过程中没有成功获取资源(如timeout，或者可中断的情况下被中断了)，那么取消结点在队列中的等待。
            cancelAcquire(node);
    }
}
```

#### 1.4.1.2. 释放同步状态--release()
&emsp; release(int)是独占模式下线程释放共享资源的顶层入口。它会释放指定量的资源，如果彻底释放了(即state=0)，它会唤醒等待队列里的其他线程来获取资源。这也正是unlock()的语义，当然不仅仅只限于unlock()。  

&emsp; 源码分析：  

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;//找到头结点
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);//唤醒等待队列里的下一个线程
        return true;
    }
    return false;
}
```

&emsp; unparkSuccessor：唤醒后继结点　 

```java
private void unparkSuccessor(Node node) {
    //获取wait状态
    int ws = node.waitStatus;
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);// 将等待状态waitStatus设置为初始值0
    Node s = node.next;//后继结点
    if (s == null || s.waitStatus > 0) {//若后继结点为空，或状态为CANCEL(已失效)，则从后尾部往前遍历找到一个处于正常阻塞状态的结点　　　　　进行唤醒
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread);//使用LockSupprot唤醒结点对应的线程
}
```
&emsp; release的同步状态相对简单，需要找到头结点的后继结点进行唤醒，若后继结点为空或处于CANCEL状态，从后向前遍历找寻一个正常的结点，唤醒其对应线程。  

### 1.4.2. 共享模式
&emsp; 共享式与独占式的区别：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-22.png)  

#### 1.4.2.1. 获取同步状态--acquireShared-1　　
&emsp; 共享锁获取流程：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-23.png)  

#### 1.4.2.2. 释放同步状态--releaseShared  
&emsp; releaseShared()是共享模式下线程释放共享资源的顶层入口。它会释放指定量的资源，如果成功释放且允许唤醒等待线程，它会唤醒等待队列里的其他线程来获取资源。  

```java
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {//尝试释放资源
        doReleaseShared();//唤醒后继结点
        return true;
    }
    return false;
}
```
&emsp; 释放掉资源后，唤醒后继。跟独占模式下的release()相似，但有一点稍微需要注意：独占模式下的tryRelease()在完全释放掉资源(state=0)后，才会返回true去唤醒其他线程，这主要是基于独占下可重入的考量；而共享模式下的releaseShared()则没有这种要求，共享模式实质就是控制一定量的线程并发执行，那么拥有资源的线程在释放掉部分资源时就可以唤醒后继等待结点。例如，资源总量是13，A(5)和B(7)分别获取到资源并发运行，C(4)来时只剩1个资源就需要等待。A在运行过程中释放掉2个资源量，然后tryReleaseShared(2)返回true唤醒C，C一看只有3个仍不够继续等待；随后B又释放2个，tryReleaseShared(2)返回true唤醒C，C一看有5个够自己用了，然后C就可以跟A和B一起运行。而ReentrantReadWriteLock读锁的tryReleaseShared()只有在完全释放掉资源(state=0)才返回true，所以自定义同步器可以根据需要决定tryReleaseShared()的返回值。  

&emsp; doReleaseShared()主要用于唤醒后继。  

```java
private void doReleaseShared() {
    for (;;) {//死循环，共享模式，持有同步状态的线程可能有多个，采用循环CAS保证线程安全
        Node h = head;
        if (h != null && h != tail) {
            int ws = h.waitStatus;
            if (ws == Node.SIGNAL) {
                if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                    continue;          
                unparkSuccessor(h);//唤醒后继结点
            }
            else if (ws == 0 &&
                        !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                continue;                
        }
        if (h == head)              
            break;
    }
}
```

### 1.4.3. AQS的模板方法设计模式，自定义同步器  
&emsp; AQS的设计是基于模板方法模式的，如果需要自定义同步器一般的方式是这样(模板方法模式很经典的一个应用)：  
1. 使用者继承AbstractQueuedSynchronizer并重写指定的方法。  
2. 将AQS组合在自定义同步组件的实现中，并调用其模板方法，而这些模板方法会调用使用者重写的方法。  

&emsp; AQS提供的模板方法有：  

```java
isHeldExclusively()//该线程是否正在独占资源。只有用到condition才需要去实现它。
tryAcquire(int)//独占方式。尝试获取资源，成功则返回true，失败则返回false。
tryRelease(int)//独占方式。尝试释放资源，成功则返回true，失败则返回false。
tryAcquireShared(int)//共享方式。尝试获取资源。负数表示失败；0表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。
tryReleaseShared(int)//共享方式。尝试释放资源，成功则返回true，失败则返回false。
```
&emsp; 默认情况下，每个方法都抛出UnsupportedOperationException。这些方法的实现必须是内部线程安全的，并且通常应该简短而不是阻塞。AQS类中的其他方法都是final ，所以无法被其他类使用，只有这几个方法可以被其他类使用。  
&emsp; 以ReentrantLock为例，state初始化为0，表示未锁定状态。A线程lock()时，会调用tryAcquire()独占该锁并将state+1。此后，其他线程再tryAcquire()时就会失败，直到A线程unlock()到state=0(即释放锁)为止，其它线程才有机会获取该锁。  

&emsp; 自定义同步器示例：  
&emsp; 基于AQS实现的不可重入的独占锁的demo，来自《Java并发编程之美》：  

```java
public class NonReentrantLock implements Lock,Serializable{

    //内部类,自定义同步器
    static class Sync extends AbstractQueuedSynchronizer {
        //是否锁已经被持有
        public boolean isHeldExclusively() {
            return getState() == 1;
        }

        //如果state为0 则尝试获取锁
        public boolean tryAcquire(int arg) {
            assert arg== 1 ;
            //CAS设置状态,能保证操作的原子性，当前为状态为0,操作成功状态改为1
            if(compareAndSetState(0, 1)){
                //设置当前独占的线程
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        //尝试释放锁，设置state为0
        public boolean tryRelease(int arg) {

            assert arg ==1;
            //如果同步器同步器状态等于0,则抛出监视器非法状态异常
            if(getState() == 0)
                throw new IllegalMonitorStateException();
            //设置独占锁的线程为null
            setExclusiveOwnerThread(null);
            //设置同步状态为0
            setState(0);
            return true;
        }

        //返回Condition,每个Condition都包含了一个Condition队列
        Condition newCondition(){
            return new ConditionObject();
        }

    }

    //创建一个Sync来做具体的工作
    private final Sync sync= new Sync ();

    @Override
    public void lock() {
        sync.acquire(1);
    }

    public boolean isLocked() {
        return sync.isHeldExclusively();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }

}
```

&emsp; NonReentrantLockDemoTest:  

```java
public class NonReentrantLockDemoTest {

    private static NonReentrantLock nonReentrantLock = new NonReentrantLock();

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                nonReentrantLock.lock();
                try {
                    System.out.println(Thread.currentThread().getName());
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    nonReentrantLock.unlock();
                }
            });
            thread.start();
        }
    }

}
```
&emsp; 运行结果：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-32.png)  