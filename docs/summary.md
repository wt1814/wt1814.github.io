
<!-- TOC -->

- [1. 总结](#1-总结)
    - [1.1. Java基础](#11-java基础)
    - [1.2. 设计模式](#12-设计模式)
    - [1.3. JVM](#13-jvm)
        - [1.3.1. JDK、JRE、JVM](#131-jdkjrejvm)
        - [1.3.2. 编译成Class字节码文件](#132-编译成class字节码文件)
        - [1.3.3. 类加载](#133-类加载)
        - [1.3.4. 内存结构](#134-内存结构)
        - [1.3.5. JVM执行](#135-jvm执行)
        - [1.3.6. GC](#136-gc)
        - [1.3.7. JVM调优](#137-jvm调优)
    - [1.4. 并发编程](#14-并发编程)
        - [1.4.1. 线程Thread](#141-线程thread)
        - [1.4.2. 并发编程](#142-并发编程)
        - [1.4.3. 线程池](#143-线程池)
        - [1.4.4. JUC](#144-juc)
            - [1.4.4.1. CAS](#1441-cas)
            - [1.4.4.2. AQS](#1442-aqs)
                - [1.4.4.2.1. LockSupport类](#14421-locksupport类)
            - [1.4.4.3. LOCK](#1443-lock)
                - [1.4.4.3.1. ReentrantLock，重入锁](#14431-reentrantlock重入锁)
                    - [1.4.4.3.1.1. 读写锁](#144311-读写锁)
            - [1.4.4.4. Atomic](#1444-atomic)
                - [1.4.4.4.1. AtomicStampedReference与AtomicMarkableReference](#14441-atomicstampedreference与atomicmarkablereference)
                - [1.4.4.4.2. LongAdder](#14442-longadder)
            - [1.4.4.5. Collections](#1445-collections)
                - [1.4.4.5.1. CopyOnWriteArrayList](#14451-copyonwritearraylist)
                - [1.4.4.5.2. ConcurrentHashMap](#14452-concurrenthashmap)
                - [1.4.4.5.3. BlockingQueue](#14453-blockingqueue)
            - [1.4.4.6. tools](#1446-tools)
                - [1.4.4.6.1. CountDownLatch](#14461-countdownlatch)
                - [1.4.4.6.2. CyclicBarrier](#14462-cyclicbarrier)
                - [1.4.4.6.3. Semaphore](#14463-semaphore)

<!-- /TOC -->


# 1. 总结  

## 1.1. Java基础

## 1.2. 设计模式

## 1.3. JVM
### 1.3.1. JDK、JRE、JVM

### 1.3.2. 编译成Class字节码文件

### 1.3.3. 类加载

### 1.3.4. 内存结构

### 1.3.5. JVM执行

### 1.3.6. GC

### 1.3.7. JVM调优

## 1.4. 并发编程
### 1.4.1. 线程Thread


### 1.4.2. 并发编程

### 1.4.3. 线程池

### 1.4.4. JUC
#### 1.4.4.1. CAS
1. CAS函数  
&emsp; **<font color = "clime">在函数CAS(V,E,N)中有3个参数：从内存中读取的值E，计算的结果值V，内存中的当前值N（可能已经被其他线程改变）。</font>**  
&emsp; **<font color = "clime">函数流程：</font>** 1. 读取当前值E，2. 计算结果值V，<font color = "clime">3. 将读取的当前值E和当前新值N作比较，如果相等，更新为V；</font>4. 如果不相等，再次读取当前值E计算结果V，将E再和新的当前值N比较，直到相等。 
2. **CAS缺点：**  
    * 循环时间长开销大。自旋CAS如果长时间不成功，会给CPU带来非常大的执行开销。  
    * **<font color = "red">只能保证一个共享变量的原子操作。</font> <font color = "clime">从Java1.5开始JDK提供了AtomicReference类来保证引用对象之间的原子性，可以把多个变量放在一个对象里来进行CAS操作。</font>**  
    * ABA问题。  
3. ABA问题详解
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

#### 1.4.4.2. AQS
1. 属性
    1. 同步状态，通过state控制同步状态。  
    2. 同步队列，`双向链表`，每个节点代表一个线程，节点有5个状态。
        * 入列addWaiter()：未获取到锁的线程会创建节点，`线程安全（CAS算法设置尾节点+死循环自旋）`的加入队列尾部。  
        * 出列unparkSuccessor()：首节点的线程释放同步状态后，`将会唤醒(LockSupport.unpark)它的后继节点(next)`，而后继节点将会在获取同步状态成功时将自己设置为首节点。
        * 入列或出列都会使用到[LockSupport](/docs/java/concurrent/LockSupport.md)工具类来阻塞、唤醒线程。    
2. 方法
    1. 独占模式：  
        * **<font color = "blue">获取同步状态</font>**   
            1. 调用使用者重写的tryAcquire方法， **<font color = "blue">tryAcquire()尝试直接去获取资源，</font>** 如果成功则直接返回；
            2. tryAcquire()获取资源失败，则调用addWaiter()将该线程加入等待队列的尾部，并标记为独占模式；
            3. acquireQueued()使线程阻塞在等待队列中获取资源，一直获取到资源后才返回。如果在整个等待过程中被中断过，则返回true，否则返回false。
            4. 如果线程在等待过程中被中断过，它是不响应的。只是获取资源后才再进行自我中断selfInterrupt()，将中断补上。
        * 释放同步状态  
    2. 共享模式下，获取同步状态、释放同步状态。  

##### 1.4.4.2.1. LockSupport类
&emsp; LockSupport是一个线程阻塞工具类，所有的方法都是静态方法，可以让线程在任意位置阻塞，当然阻塞之后肯定得有唤醒的方法。  
&emsp; LockSupport主要有两类方法：park和unpark。 

#### 1.4.4.3. LOCK
##### 1.4.4.3.1. ReentrantLock，重入锁
1. ReentrantLock与synchronized比较
    1. （支持非公平）ReenTrantLock可以指定是公平锁还是非公平锁。而synchronized只能是非公平锁。所谓的公平锁就是先等待的线程先获得锁。  
    2. Lock接口可以尝试非阻塞地获取锁，当前线程尝试获取锁。如果这一时刻锁没有被其他线程获取到，则成功获取并持有锁。  
    3. （可被中断）Lock接口能被中断地获取锁，与synchronized不同，获取到锁的线程能够响应中断，当获取到的锁的线程被中断时，中断异常将会被抛出，同时锁会被释放。可以使线程在等待锁的时候响应中断；  
    4. （支持超时/限时等待）Lock接口可以在指定的截止时间之前获取锁，如果截止时间到了依旧无法获取锁，则返回。可以让线程尝试获取锁，并在无法获取锁的时候立即返回或者等待一段时间；  
    5. （可实现选择性通知，锁可以绑定多个条件）ReenTrantLock提供了一个Condition(条件)类，用来实现分组唤醒需要唤醒的一些线程，而不是像synchronized要么随机唤醒一个线程要么唤醒全部线程。  
2. **<font color = "red">lock()方法描述：</font>**  
    1. 在初始化ReentrantLock的时候，如果不传参数是否公平，那么默认使用非公平锁，也就是NonfairSync。  
    2. 1). <font color = "clime">调用ReentrantLock的lock方法的时候，实际上是调用了NonfairSync的lock方法，这个方法①先用CAS操作`compareAndSetState(0, 1)`，去尝试抢占该锁。如果成功，就把当前线程设置在这个锁上，表示抢占成功。</font>  
       2). ②如果失败，则调用acquire模板方法，等待抢占。   
       `“非公平”体现在，如果占用锁的线程刚释放锁，state置为0，而排队等待锁的线程还未唤醒时，新来的线程就直接抢占了该锁，那么就“插队”了。`   
    3. AQS的acquire模板方法：  
        1. AQS#acquire()调用子类NonfairSync#tryAcquire()#nonfairTryAcquire()。 **<font color = "blue">如果锁状态是0，再次CAS抢占锁。</font>** 如果锁状态不是0，判断是否当前线程。    
        2. acquireQueued(addWaiter(Node.EXCLUSIVE), arg) )，其中addWaiter(Node.EXCLUSIVE)入等待队列。  
        3. acquireQueued(final Node node, int arg)，使线程阻塞在等待队列中获取资源，一直获取到资源后才返回。如果在整个等待过程中被中断过，则返回true，否则返回false。
        4. 如果线程在等待过程中被中断过，它是不响应的。只是获取资源后才再进行自我中断selfInterrupt()，将中断补上。  

    &emsp; 用一张流程图总结一下非公平锁的获取锁的过程。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-75.png)  

###### 1.4.4.3.1.1. 读写锁
1. ReentrantReadWriteLock  
&emsp; **<font color = "red">ReentrantReadWriteLock缺点：读写锁互斥，只有当前没有线程持有读锁或者写锁时，才能获取到写锁，</font><font color = "clime">这可能会导致写线程发生饥饿现象，</font><font color = "red">即读线程太多导致写线程迟迟竞争不到锁而一直处于等待状态。StampedLock()可以解决这个问题。</font>**  
2. StampedLock  
    1. StampedLock有3种模式：写锁 writeLock、悲观读锁 readLock、乐观读锁 tryOptimisticRead。  
    &emsp; StampedLock通过乐观读锁tryOptimisticRead解决ReentrantReadWriteLock的写锁饥饿问题。乐观读锁模式下，一个线程获取的乐观读锁之后，不会阻塞其他线程获取写锁。    
    2. **<font color = "clime">同时允许多个乐观读和一个写线程同时进入临界资源操作，那读取的数据可能是错的怎么办？</font>**    
    &emsp; **<font color = "clime">通过版本号控制。</font>** 乐观读不能保证读取到的数据是最新的，所以将数据读取到局部变量的时候需要通过 lock.validate(stamp) 校验是否被写线程修改过，若是修改过则需要上悲观读锁，再重新读取数据到局部变量。`即乐观读失败后，再次使用悲观读锁。`    

#### 1.4.4.4. Atomic
##### 1.4.4.4.1. AtomicStampedReference与AtomicMarkableReference
1. AtomicStampedReference每次修改都会让stamp值加1，类似于版本控制号。 
2. **<font color = "clime">AtomicStampedReference可以知道，引用变量中途被更改了几次。有时候，并不关心引用变量更改了几次，只是单纯的关心是否更改过，所以就有了AtomicMarkableReference。</font>**  

##### 1.4.4.4.2. LongAdder
1. LongAdder重要属性：有一个全局变量`volatile long base`值、父类Striped64中存在一个`volatile Cell[] cells;`数组，其长度是2的幂次方。  
2. LongAdder原理：  
    1. CAS操作：当并发不高的情况下都是通过CAS来直接操作base值，如果CAS失败，则针对LongAdder中的Cell[]数组中的Cell进行CAS操作，减少失败的概率。
    2. 解决伪共享：每个Cell都使用@Contended注解进行修饰，而@Contended注解可以进行缓存行填充，从而解决伪共享问题。  

#### 1.4.4.5. Collections
##### 1.4.4.5.1. CopyOnWriteArrayList
1. CopyOnWriteArrayList  
&emsp; CopyOnWrite，写时复制。读操作时不加锁以保证性能不受影响；  
&emsp; **<font color = "clime">`写操作时加锁，`复制资源的一份副本，在副本上执行写操作，写操作完成后将资源的引用指向副本。</font>** CopyOnWriteArrayList源码中，基于ReentrantLock保证了增加元素和删除元素动作的互斥。   
&emsp; **优点：** 可以对CopyOnWrite容器进行并发的读，而不需要加锁，因为当前容器不会添加任何元素。`所以CopyOnWrite容器也是一种读写分离的思想，读和写不同的容器。`  
&emsp; **<font color = "clime">缺点：** **1.占内存(写时复制，new两个对象)；2.不能保证数据实时一致性。</font>**  
&emsp; **使用场景：** <font color = "clime">CopyOnWrite并发容器用于读多写少的并发场景。比如白名单，黑名单，商品类目的访问和更新场景。</font>

##### 1.4.4.5.2. ConcurrentHashMap
1. ConcurrentHashMap，JDK1.8  
    &emsp; **<font color = "red">从jdk1.8开始，ConcurrentHashMap类取消了Segment分段锁，采用Node + CAS + Synchronized来保证并发安全。</font>**  
    &emsp; **<font color = "clime">jdk1.8中的ConcurrentHashMap中synchronized只锁定当前链表或红黑树的首节点，只要节点hash不冲突，就不会产生并发，相比JDK1.7的ConcurrentHashMap效率又提升了许多。</font>**  
    1. **<font color = "clime">put()流程：</font>**
        1. 根据 key 计算出 hashcode 。  
        2. 整个过程自旋添加节点。  
        2. 判断是否需要进行初始化数组。  
        3. <font color = "red">为当前key定位出Node，如果为空表示此数组下无节点，当前位置可以直接写入数据，利用CAS尝试写入，失败则进入下一次循环。</font>  
        4. **<font color = "blue">如果当前位置的hashcode == MOVED == -1，表示其他线程插入成功正在进行扩容，则当前线程`帮助进行扩容`。</font>**  
        5. <font color = "red">如果都不满足，则利用synchronized锁写入数据。</font>  
        6. 如果数量大于TREEIFY_THRESHOLD则要转换为红黑树。 
        7. 最后通过addCount来增加ConcurrentHashMap的长度，并且还可能触发扩容操作。  
    2. **<font color = "clime">get()流程：为什么ConcurrentHashMap的读操作不需要加锁？</font>**  
        1. 在1.8中ConcurrentHashMap的get操作全程不需要加锁，这也是它比其他并发集合（比如hashtable、用Collections.synchronizedMap()包装的hashmap）安全效率高的原因之一。  
        2. get操作全程不需要加锁是因为Node的成员val是用volatile修饰的，和数组用volatile修饰没有关系。  
        3. 数组用volatile修饰主要是保证在数组扩容的时候保证可见性。  
2. ConcurrentHashMap，JDK1.7  
    1. 在JDK1.7中，ConcurrentHashMap类采用了分段锁的思想，Segment(段) + HashEntry(哈希条目) + ReentrantLock。  
    2. Segment继承ReentrantLock(可重入锁)，从而实现并发控制。Segment的个数一旦初始化就不能改变，默认Segment的个数是16个，也可以认为ConcurrentHashMap默认支持最多16个线程并发。  

##### 1.4.4.5.3. BlockingQueue
1. 阻塞队列：当队列是空的时候，从队列中获取元素的操作将会被阻塞，或者当队列是满时，往队列里添加元素的操作会被阻塞。  
2. `线程池所使用的缓冲队列，常用的是：SynchronousQueue（无缓冲等待队列）、ArrayBlockingQueue（有界缓冲等待队列）、LinkedBlockingQueue（无界缓冲等待队列）。`   
3. SynchronousQueue，没有容量，是无缓冲等待队列，是一个不存储元素的阻塞队列，会直接将任务交给消费者，必须等队列中的元素被消费后才能继续添加新的元素。  
4. LinkedBlockingQueue不同于ArrayBlockingQueue，它如果不指定容量，默认为Integer.MAX_VALUE，也就是无界队列。所以为了避免队列过大造成机器负载或者内存爆满的情况出现，在使用的时候建议手动传一个队列的大小。  
5. <font color = "red">ArrayBlockingQueue与LinkedBlockingQueue：</font> ArrayBlockingQueue预先分配好一段连续内存，更稳定；LinkedBlockingQueue读写锁分离，吞吐量更大。  

#### 1.4.4.6. tools
##### 1.4.4.6.1. CountDownLatch
1. java.util.concurrent.CountDownLatch类， **<font color = "red">能够使一个线程等待其他线程完成各自的工作后再执行。</font>** <font color = "red">利用它可以实现类似计数器的功能。</font><font color = "blue">比如有一个任务A，它要等待其他4个任务执行完毕之后才能执行，此时就可以利用CountDownLatch来实现这种功能了。</font>  
2. **<font color = "clime">countDown()方法是将count-1，如果发现count=0了，就唤醒</font><font color = "blue">阻塞的主线程。</font>**  
&emsp; ⚠️注：特别注意主线程会被阻塞。  
3. <font color = "red">CountDownLatch对象不能被重复利用，也就是不能修改计数器的值。</font>CountDownLatch是一次性的，计数器的值只能在构造方法中初始化一次，之后没有任何机制再次对其设置值，当CountDownLatch使用完毕后，它不能再次被使用。    
4. <font color = "clime">CountDownLatch是由AQS实现的，创建CountDownLatch时设置计数器count其实就是设置AQS.state=count，也就是重入次数。  
    * await()方法调用获取锁的方法，由于AQS.state=count表示锁被占用且重入次数为count，所以获取不到锁线程被阻塞并进入AQS队列。  
    * countDown()方法调用释放锁的方法，每释放一次AQS.state减1，当AQS.state变为0时表示处于无锁状态了，就依次唤醒AQS队列中阻塞的线程来获取锁，继续执行逻辑代码。</font>  

##### 1.4.4.6.2. CyclicBarrier
&emsp; CyclicBarrier字面意思是回环栅栏， **<font color = "blue">允许一组线程互相等待，直到到达某个公共屏障点 (common barrier point)之后，再全部同时执行。</font>** 叫做回环是因为当所有等待线程都被释放以后，CyclicBarrier可以被重用。  

&emsp; **<font color = "clime">CyclicBarrier用途有两个：</font>**   

* 让一组线程等待至某个状态后再同时执行。
* 让一组线程等待至某个状态后，执行指定的任务。

##### 1.4.4.6.3. Semaphore
&emsp; Semaphore类，一个计数信号量。从概念上讲，信号量维护了一个许可集合。如有必要，在许可可用前会阻塞每一个acquire()，然后再获取该许可。每个 release()添加一个许可，从而可能释放一个正在阻塞的获取者。但是，不使用实际的许可对象，Semaphore只对可用许可的号码进行计数，并采取相应的行动。  
&emsp; 使用场景： **<font color = "red">Semaphore通常用于限制可以访问某些资源(物理或逻辑的)的线程数目。Semaphore可以用来构建一些对象池，资源池之类的，比如数据库连接池。</font>**   
