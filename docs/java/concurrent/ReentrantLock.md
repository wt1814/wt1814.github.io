
<!-- TOC -->

- [1. ReentrantLock](#1-reentrantlock)
    - [1.1. ReentrantLock，重入锁](#11-reentrantlock重入锁)
        - [1.1.1. ReentrantLock解析](#111-reentrantlock解析)
            - [1.1.1.1. ReentrantLock类层次结构](#1111-reentrantlock类层次结构)
            - [1.1.1.2. 构造函数](#1112-构造函数)
            - [1.1.1.3. 成员方法](#1113-成员方法)
            - [1.1.1.4. 非公平锁NonfairSync](#1114-非公平锁nonfairsync)
                - [1.1.1.4.1. 获取锁lock()](#11141-获取锁lock)
                - [1.1.1.4.2. 释放锁unlock()](#11142-释放锁unlock)
            - [1.1.1.5. 公平锁FairSync](#1115-公平锁fairsync)
        - [1.1.2. ReentrantLock与synchronized比较](#112-reentrantlock与synchronized比较)
        - [1.1.3. 使用示例](#113-使用示例)
    - [1.2. Condition，等待/通知机制](#12-condition等待通知机制)
        - [1.2.1. Condition类提供的方法](#121-condition类提供的方法)
        - [1.2.2. Condition与wait/notify](#122-condition与waitnotify)
        - [1.2.3. 使用示例](#123-使用示例)

<!-- /TOC -->

# 1. ReentrantLock 
<!-- 
&emsp; **<font color = "lime">一句话概述：</font>**  
&emsp; ReentrantLock默认使用非公平锁NonfairSync，调用ReentrantLock.lock()也是调用NonfairSync.lock()。流程：  

1. 先用CAS操作，去尝试抢占该锁。如果成功，就把当前线程设置在这个锁上，表示抢占成功。
2. 如果失败，则调用AbstractQueuedSynchronizer的acquire模板方法，等待抢占。独占模式获取同步状态流程如下：
    1. 调用使用者重写的tryAcquire方法，tryAcquire()尝试直接去获取资源，如果成功则直接返回(这里体现了非公平锁，每个线程获取锁时会尝试直接抢占加锁一次，而CLH队列中可能还有别的线程在等待)；
    2. addWaiter()将该线程加入等待队列的尾部，并标记为独占模式；
    3. acquireQueued()使线程阻塞在等待队列中获取资源，一直获取到资源后才返回。如果在整个等待过程中被中断过，则返回true，否则返回false。
    4. 如果线程在等待过程中被中断过，它是不响应的。只是获取资源后才再进行自我中断selfInterrupt()，将中断补上。
-->
## 1.1. ReentrantLock，重入锁  
&emsp; ReentrantLock(Re-Entrant-Lock)，可重入互斥锁，具有与synchronized隐式锁相同的基本行为和语义，但扩展了功能。  

### 1.1.1. ReentrantLock解析  
#### 1.1.1.1. ReentrantLock类层次结构  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-26.png)  

&emsp; ReentrantLock实现了Lock接口，<font color = "red">内部有三个内部类：Sync、NonfairSync、FairSync。  

* </font>Sync是一个抽象类型，它继承[AbstractQueuedSynchronizer](/docs/java/concurrent/AQS.md)。AbstractQueuedSynchronizer是一个模板类，它实现了许多和锁相关的功能，并提供了钩子方法供用户实现，比如tryAcquire，tryRelease等。Sync实现了AbstractQueuedSynchronizer的tryRelease方法。
* NonfairSync和FairSync两个类继承自Sync，实现了lock方法，然后分别公平抢占和非公平抢占针对tryAcquire有不同的实现。  

#### 1.1.1.2. 构造函数  

```java
//创建 ReentrantLock实例
public ReentrantLock() {
    sync = new NonfairSync();
}
//创建具有给定的公平政策ReentrantLock实例。true，公平锁；false，非公平锁。
public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
```

#### 1.1.1.3. 成员方法  

```java
/*获取锁，有以下三种情况：
    锁空闲：直接获取锁并返回，同时设置锁持有者数量为1；
    当前线程持有锁：直接获取锁并返回，同时锁持有者数量递增1；
    其他线程持有锁：当前线程会休眠等待，直至获取锁为止；*/
public void lock() {
    sync.lock();
}
/*一个获取可中断锁的尝试。
        获取锁，逻辑和lock()方法一样，但这个方法在获取锁过程中能响应中断。*/
public void lockInterruptibly() throws InterruptedException {
    sync.acquireInterruptibly(1);
}
/*一个非块结构的获取锁尝试，获取成功返回：true，获取失败返回：false, 这个方法不会等待，有以下三种情况：
        锁空闲：直接获取锁并返回：true，同时设置锁持有者数量为：1；
        当前线程持有锁：直接获取锁并返回：true，同时锁持有者数量递增1；
        其他线程持有锁：获取锁失败，返回：false；*/
public boolean tryLock() {
    return sync.nonfairTryAcquire(1);
}
/*一个获取超时失效锁的尝试。
        逻辑和tryLock()差不多，只是这个方法是带时间的。*/
public boolean tryLock(long timeout, TimeUnit unit)
        throws InterruptedException {
    return sync.tryAcquireNanos(1, unit.toNanos(timeout));
}
/*释放锁，每次锁持有者数量递减1，直到0为止。*/
public void unlock() {
    sync.release(1);
}
/*返回一个这个锁的Condition实例，可以实现 synchronized关键字类似wait/ notify实现多线程通信的功能。*/
public Condition newCondition() {
    return sync.newCondition();
}
```

#### 1.1.1.4. 非公平锁NonfairSync  
&emsp; ReentrantLock主要利用CAS+AQS队列来实现。  

##### 1.1.1.4.1. 获取锁lock()  
&emsp; lock方法描述：  

1. 在初始化ReentrantLock的时候，如果不传参数是否公平，那么默认使用非公平锁，也就是NonfairSync。  
2. <font color = "lime">调用ReentrantLock的lock方法的时候，实际上是调用了NonfairSync的lock方法，这个方法先用CAS操作，去尝试抢占该锁。如果成功，就把当前线程设置在这个锁上，表示抢占成功。如果失败，则调用acquire模板方法，等待抢占。</font>    

&emsp; 调用ReentrantLock中的lock()方法，源码的调用过程时序图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-25.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-27.png)   

```java
static final class NonfairSync extends Sync {
    private static final long serialVersionUID = 7316153563782823691L;

    /**
        * Performs lock.  Try immediate barge, backing up to normal
        * acquire on failure.
        */
    final void lock() {
        if (compareAndSetState(0, 1))
            setExclusiveOwnerThread(Thread.currentThread());
        else
            acquire(1);
    }
}
```
&emsp; 首先用一个CAS操作，判断state是否是0(表示当前锁未被占用)，如果是0则把它置为1，并且设置当前线程为该锁的独占线程，表示获取锁成功。当多个线程同时尝试占用同一个锁时，CAS操作只能保证一个线程操作成功。  

&emsp; **<font color = "lime">“非公平”即体现在这里，如果占用锁的线程刚释放锁，state置为0，而排队等待锁的线程还未唤醒时，新来的线程就直接抢占了该锁，那么就“插队”了。</font>**  

&emsp; 如果CAS失败，会执行acquire(1)方法。acquire(1)实际上使用的是AbstractQueuedSynchronizer的acquire方法。(再次理解下AQS独占模式下的获取锁过程)  

&emsp; 若当前有三个线程去竞争锁，假设线程A的CAS操作成功，返回。那么线程B和C则设置state失败，走到了else里面。  

```java
public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
}
```
1. 第一步。尝试去获取锁。如果尝试获取锁成功，方法直接返回。  
&emsp; acquire方法内部先使用tryAcquire这个钩子方法去尝试再次获取锁，这个方法在NonfairSync这个类中其实就是使用了nonfairTryAcquire，具体实现原理是先比较当前锁的状态是否是0，如果是0，则尝试去原子抢占这个锁(设置状态为1，然后把当前线程设置成独占线程)，如果当前锁的状态不是0，就去比较当前线程和占用锁的线程是不是一个线程，如果是，会去增加状态变量的值，从这里看出可重入锁之所以可重入，就是同一个线程可以反复使用它占用的锁。如果以上两种情况都不通过，则返回失败false。代码如下：  

```java
//tryAcquire(arg)
final boolean nonfairTryAcquire(int acquires) {
    //获取当前线程
    final Thread current = Thread.currentThread();
    //获取state变量值
    int c = getState();
    if (c == 0) { //没有线程占用锁
        if (compareAndSetState(0, acquires)) {
            //占用锁成功,设置独占线程为当前线程
            setExclusiveOwnerThread(current);
            return true;
        }
    } else if (current == getExclusiveOwnerThread()) { //当前线程已经占用该锁
        int nextc = c + acquires;
        if (nextc < 0) // overflow
            throw new Error("Maximum lock count exceeded");
        // 更新state值为新的重入次数
        setState(nextc);
        return true;
    }
    //获取锁失败
    return false;
}
```
<!-- 非公平锁tryAcquire的流程是：检查state字段，若为0，表示锁未被占用，那么尝试占用，若不为0，检查当前锁是否被自己占用，若被自己占用，则更新state字段，表示重入锁的次数。如果以上两点都没有成功，则获取锁失败，返回false。-->

2. tryAcquire一旦返回false，就会则进入acquireQueued流程，也就是基于CLH队列的抢占模式：  
3. 第二步，入队：由于上文中提到线程A已经占用了锁，所以B和C执行tryAcquire失败，并且入等待队列。如果线程A拿着锁死死不放，那么B和C就会被挂起。
  
    ```java
    /**
    * 将新节点和当前线程关联并且入队列
    * @param mode 独占/共享
    * @return 新节点
    */
    private Node addWaiter(Node mode) {
        //初始化节点，设置关联线程和模式(独占 or 共享)
        Node node = new Node(Thread.currentThread(), mode);
        // 获取尾节点引用
        Node pred = tail;
        // 尾节点不为空，说明队列已经初始化过
        if (pred != null) {
            node.prev = pred;
            // 设置新节点为尾节点
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        // 尾节点为空,说明队列还未初始化，需要初始化head节点并入队新节点
        enq(node);
        return node;
    }
    ```

&emsp; B、C线程同时尝试入队列，由于队列尚未初始化，tail==null，故至少会有一个线程会走到enq(node)。假设同时走到了enq(node)里。  

    ```java
    /**
    * 初始化队列并且入队新节点
    */
    private Node enq(final Node node) {
        //开始自旋
        for (;;) {
            Node t = tail;
            if (t == null) { // Must initialize
                // 如果tail为空,则新建一个head节点,并且tail指向head
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
                node.prev = t;
                // tail不为空,将新节点入队
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }
    ```
&emsp; 这里体现了经典的自旋+CAS组合来实现非阻塞的原子操作。由于compareAndSetHead的实现使用了unsafe类提供的CAS操作，所以只有一个线程会创建head节点成功。假设线程B成功，之后B、C开始第二轮循环，此时tail已经不为空，两个线程都走到else里面。假设B线程compareAndSetTail成功，那么B就可以返回了，C由于入队失败还需要第三轮循环。最终所有线程都可以成功入队。  
&emsp; 当B、C入等待队列后，此时AQS队列如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-73.png)  
4. 第三步，挂起。B和C相继执行acquireQueued(final Node node, int arg)。这个方法让已经入队的线程尝试获取锁，若失败则会被挂起。  

    ```java
    /**
    * 已经入队的线程尝试获取锁
    */
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true; //标记是否成功获取锁
        try {
            boolean interrupted = false; //标记线程是否被中断过
            for (;;) {
                final Node p = node.predecessor(); //获取前驱节点
                //如果前驱是head,即该结点已成老二，那么便有资格去尝试获取锁
                if (p == head && tryAcquire(arg)) {
                    setHead(node); // 获取成功,将当前节点设置为head节点
                    p.next = null; // 原head节点出队,在某个时间点被GC回收
                    failed = false; //获取成功
                    return interrupted; //返回是否被中断过
                }
                // 判断获取失败后是否可以挂起,若可以则挂起
                if (shouldParkAfterFailedAcquire(p, node) &&
                        parkAndCheckInterrupt())
                    // 线程若被中断,设置interrupted为true
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
    ```
&emsp; 假设B和C在竞争锁的过程中A一直持有锁，那么它们的tryAcquire操作都会失败，因此会走到第2个if语句中。再看下shouldParkAfterFailedAcquire和parkAndCheckInterrupt方法。  

    ```java
    /**
    * 判断当前线程获取锁失败之后是否需要挂起.
    */
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        //前驱节点的状态
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
            // 前驱节点状态为signal,返回true
            return true;
        // 前驱节点状态为CANCELLED
        if (ws > 0) {
            // 从队尾向前寻找第一个状态不为CANCELLED的节点
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            // 将前驱节点的状态设置为SIGNAL
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }

    /**
    * 挂起当前线程,返回线程中断状态并重置
    */
    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }
    ```
&emsp; 线程入队后能够挂起的前提是，它的前驱节点的状态为SIGNAL，它的含义是“Hi，前面的兄弟，如果你获取锁并且出队后，记得把我唤醒！”。所以shouldParkAfterFailedAcquire会先判断当前节点的前驱是否状态符合要求，若符合则返回true，然后调用parkAndCheckInterrupt，将自己挂起。如果不符合，再看前驱节点是否>0(CANCELLED)，若是那么向前遍历直到找到第一个符合要求的前驱，若不是则将前驱节点的状态设置为SIGNAL。  
&emsp; 整个流程中，如果前驱结点的状态不是SIGNAL，那么自己就不能安心挂起，需要去找个安心的挂起点，同时可以再尝试下看有没有机会去尝试竞争锁。  

&emsp; 最终队列可能会如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-74.png) 

&emsp; 用一张流程图总结一下非公平锁的获取锁的过程。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-75.png) 

##### 1.1.1.4.2. 释放锁unlock()
&emsp; unlock方法的时序图
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-72.png)  

```java
public void unlock() {
    sync.release(1);
}
  
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```
&emsp; 调用unlock方法，其实是直接调用AbstractQueuedSynchronizer的release操作。(再次理解下AQS独占模式下的释放锁过程)  

&emsp; 解锁流程大致为先尝试释放锁，若释放成功，那么查看头结点的状态是否为SIGNAL，如果是则唤醒头结点的下个节点关联的线程，如果释放失败那么返回false表示解锁失败。每次都只唤起头结点的下一个节点关联的线程。  
<!-- 
1、调用unlock方法，其实是直接调用AbstractQueuedSynchronizer的release操作。
2、进入release方法，内部先尝试tryRelease操作,主要是去除锁的独占线程，然后将状态减一，这里减一主要是考虑到可重入锁可能自身会多次占用锁，只有当状态变成0，才表示完全释放了锁。
3、一旦tryRelease成功，则将CHL队列的头节点的状态设置为0，然后唤醒下一个非取消的节点线程。
4、一旦下一个节点的线程被唤醒，被唤醒的线程就会进入acquireQueued代码流程中，去获取锁。
-->
&emsp; tryRelease的执行过程  

```java
/**
 * 释放当前线程占用的锁
 * @param releases
 * @return 是否释放成功
 */
protected final boolean tryRelease(int releases) {
    // 计算释放后state值
    int c = getState() - releases;
    // 如果不是当前线程占用锁,那么抛出异常
    if (Thread.currentThread() != getExclusiveOwnerThread())
        throw new IllegalMonitorStateException();
    boolean free = false;
    if (c == 0) {
        // 锁被重入次数为0,表示释放成功
        free = true;
        // 清空独占线程
        setExclusiveOwnerThread(null);
    }
    // 更新state值
    setState(c);
    return free;
}
```
&emsp; 这里入参为1。tryRelease的过程为：当前释放锁的线程若不持有锁，则抛出异常。若持有锁，计算释放后的state值是否为0，若为0表示锁已经被成功释放，并且则清空独占线程，最后更新state值，返回free。   


#### 1.1.1.5. 公平锁FairSync  
&emsp; 公平锁和非公平锁不同之处在于，公平锁在获取锁的时候，不会先去检查state状态，而是直接执行aqcuire(1)。

### 1.1.2. ReentrantLock与synchronized比较 
&emsp; Java提供了两种锁机制来控制多个线程对共享资源的互斥访问，第一个是JVM实现的synchronized，而另一个是JDK实现的ReentrantLock。  
&emsp; ReentrantLock与synchronized的联系：Lock接口提供了与synchronized关键字类似的同步功能，但需要在使用时手动获取锁和释放锁。ReentrantLock和synchronized都是可重入的互斥锁。  
&emsp; **<font color = "red">Lock接口与synchronized关键字的区别(Lock的优势全部体现在构造函数、方法中)：</font>**  
1. (支持非公平)ReenTrantLock可以指定是公平锁还是非公平锁。而synchronized只能是非公平锁。所谓的公平锁就是先等待的线程先获得锁。  
2. Lock接口可以尝试非阻塞地获取锁，当前线程尝试获取锁。如果这一时刻锁没有被其他线程获取到，则成功获取并持有锁。  
3. (可被中断)Lock接口能被中断地获取锁，与synchronized不同，获取到锁的线程能够响应中断，当获取到的锁的线程被中断时，中断异常将会被抛出，同时锁会被释放。 可以使线程在等待锁的时候响应中断；  
4. (支持超时/限时等待)Lock接口可以在指定的截止时间之前获取锁，如果截止时间到了依旧无法获取锁，则返回。可以让线程尝试获取锁，并在无法获取锁的时候立即返回或者等待一段时间；  
5. (可实现选择性通知，锁可以绑定多个条件)ReenTrantLock提供了一个Condition(条件)类，用来实现分组唤醒需要唤醒的一些线程，而不是像synchronized要么随机唤醒一个线程要么唤醒全部线程。  
  
&emsp; **什么时候选择用ReentrantLock代替synchronized？**  
&emsp; 在确实需要一些synchronized所没有的特性的时候，比如时间锁等候、可中断锁等候、无块结构锁、多个条件变量或者锁投票。ReentrantLock还具有可伸缩性的好处，应当在高度争用的情况下使用它，但是请记住，大多数synchronized块几乎从来没有出现过争用，所以可以把高度争用放在一边。建议用synchronized开发，直到确实证明synchronized不合适，而不要仅仅是假设如果使用ReentrantLock“性能会更好”。  

### 1.1.3. 使用示例  
&emsp; 在使用重入锁时，一定要在程序最后释放锁。一般释放锁的代码要写在finally里。否则，如果程序出现异常，Lock就永远无法释放了。(synchronized的锁是JVM最后自动释放的。)  

```java
private final ReentrantLock lock = new ReentrantLock();

try {
if (lock.tryLock(5, TimeUnit.SECONDS)) { //如果已经被lock，尝试等待5s，看是否可以获得锁，如果5s后仍然无法获得锁则返回false继续执行
    // lock.lockInterruptibly();可以响应中断事件
    try {
        //操作
    } finally {
        lock.unlock();
    }
}
} catch (InterruptedException e) {
    e.printStackTrace(); //当前线程被中断时(interrupt)，会抛InterruptedException
}
```

## 1.2. Condition，等待/通知机制  
&emsp; 关键字synchronized与wait()和notify()/notifyAll()方法相结合可以实现等待/通知机制。ReentrantLock结合Condition也可以实现等待/通知机制。  
&emsp; Condition又称等待条件，它实现了对锁更精确的控制。

### 1.2.1. Condition类提供的方法  
&emsp; 等待方法：  

```java
// 当前线程进入等待状态，如果其他线程调用 condition 的 signal 或者 signalAll 方法并且当前线程获取 Lock 从 await 方法返回，如果在等待状态中被中断会抛出被中断异常
void await() throws InterruptedException
// 当前线程进入等待状态直到被通知，中断或者超时
long awaitNanos(long nanosTimeout)
// 同第二个方法，支持自定义时间单位
boolean await(long time, TimeUnit unit)throws InterruptedException
// 当前线程进入等待状态直到被通知，中断或者到了某个时间
boolean awaitUntil(Date deadline) throws InterruptedException
```

&emsp; 唤醒方法：  

```java
// 唤醒一个等待在 condition 上的线程，将该线程从等待队列中转移到同步队列中，如果在同步队列中能够竞争到 Lock 则可以从等待方法中返回
void signal()
// 与 1 的区别在于能够唤醒所有等待在 condition 上的线程
void signalAll()
```

### 1.2.2. Condition与wait/notify    
&emsp; Condition中的await()方法相当于Object的wait()方法，Condition中的signal()方法相当于Object的notify()方法，Condition中的signalAll()相当于Object的notifyAll()方法。  
&emsp;  **Condition 与 wait/notify的区别：**    
&emsp; 在使用notify()/notifyAll()方法进行通知时，被通知的线程是由JVM随机选择的。但使用ReentrantLock结合Condition类是可以实现“选择性通知”。  

|对比项| Condition | Object监视器 |  
| ---- | ---- | ---- |  
|使用条件|获取锁|获取锁，创建Condition对象| 
|等待队列的个数|一个|多个| 
|是否支持通知指定等待队列|支持|不支持| 
|是否支持当前线程释放锁进入等待状态|支持|支持| 
|是否支持当前线程释放锁并进入超时等待状态|支持|支持| 
|是否支持当前线程释放锁并进入等待状态直到指定最后期限|支持|不支持| 
|是否支持唤醒等待队列中的一个任务|支持|支持| 
|是否支持唤醒等待队列中的全部任务|支持|支持|   

### 1.2.3. 使用示例  
1. 启动waiter和signaler两个线程。  
2. waiter线程获取到锁，检查flag=false不满足条件，执行condition.await()方法将线程阻塞等待并释放锁。  
3. signaler线程获取到锁之后更改条件，将flag变为true，执行condition.signalAll()通知唤醒等待线程，释放锁。  
4. waiter线程被唤醒获取到锁，自旋检查flag=true满足条件，继续执行。  

```java
public class ConditionTest {
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition condition = lock.newCondition();
    private static volatile boolean flag = false;

    public static void main(String[] args) {
        Thread waiter = new Thread(new waiter());
        waiter.start();
        Thread signaler = new Thread(new signaler());
        signaler.start();
    }

    static class waiter implements Runnable {

        @Override
        public void run() {
            lock.lock();
            try {
                while (!flag) {
                    System.out.println(Thread.currentThread().getName() + "当前条件不满足等待");
                    try {
                        condition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(Thread.currentThread().getName() + "接收到通知条件满足");
            } finally {
                lock.unlock();
            }
        }
    }

    static class signaler implements Runnable {

        @Override
        public void run() {
            lock.lock();
            try {
                flag = true;
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }
}
```
&emsp; 输出结果：  

    Thread-0当前条件不满足等待
    Thread-0接收到通知，条件满足
