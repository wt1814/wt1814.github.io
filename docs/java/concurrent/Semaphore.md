
<!-- TOC -->

- [1. Semaphore，信号量](#1-semaphore信号量)
    - [1.1. Semaphore介绍](#11-semaphore介绍)
    - [1.2. API](#12-api)
    - [1.3. 使用示例](#13-使用示例)

<!-- /TOC -->


# 1. Semaphore，信号量

## 1.1. Semaphore介绍
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

## 1.2. API  
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

## 1.3. 使用示例  
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
