
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
    - [1.5. 数据库](#15-数据库)
        - [1.5.1. SQL语句](#151-sql语句)
            - [1.5.1.1. 基本查询语句](#1511-基本查询语句)
            - [1.5.1.2. 连接查询](#1512-连接查询)
            - [1.5.1.3. ~~高级查询~~](#1513-高级查询)
        - [1.5.2. MySql函数](#152-mysql函数)
        - [1.5.3. MySql优化](#153-mysql优化)
            - [1.5.3.1. SQL分析](#1531-sql分析)
                - [1.5.3.1.1. Expain](#15311-expain)
            - [1.5.3.2. SQL优化](#1532-sql优化)
            - [1.5.3.3. 索引优化](#1533-索引优化)
            - [1.5.3.4. 碎片优化](#1534-碎片优化)
        - [1.5.4. 数据库分布式](#154-数据库分布式)
            - [1.5.4.1. 大数据量操作](#1541-大数据量操作)
            - [1.5.4.2. MySql瓶颈](#1542-mysql瓶颈)
            - [1.5.4.3. 数据库分布式](#1543-数据库分布式)
            - [1.5.4.4. 主从复制](#1544-主从复制)
                - [1.5.4.4.1. 主从复制原理](#15441-主从复制原理)
                - [1.5.4.4.2. 主从复制实现](#15442-主从复制实现)
                - [1.5.4.4.3. 主从复制问题](#15443-主从复制问题)
                - [1.5.4.4.4. 高可用实现](#15444-高可用实现)
                - [1.5.4.4.5. 读写分离实现](#15445-读写分离实现)
            - [1.5.4.5. 分区](#1545-分区)
            - [1.5.4.6. 分库分表](#1546-分库分表)
                - [1.5.4.6.1. 分库分表](#15461-分库分表)
                - [1.5.4.6.2. 分库分表查询](#15462-分库分表查询)
            - [1.5.4.7. 数据迁移](#1547-数据迁移)

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


## 1.5. 数据库
### 1.5.1. SQL语句  
#### 1.5.1.1. 基本查询语句
1. 基本查询SQL执行顺序：from -> on -> join -> where -> group by ->  avg,sum.... ->having -> select -> distinct -> order by -> top,limit。 
2. distinct关键字：Distinct与Count(聚合函数)，COUNT()会过滤掉为NULL的项。  
3. 分组函数  
&emsp; **<font color = "clime">查询结果集中有统计数据时，就需要使用分组函数。</font>**  
&emsp; **<font color = "red">Group By分组函数中，查询只能得到组相关的信息。组相关的信息(统计信息)：count,sum,max,min,avg。</font> 在select指定的字段要么包含在Group By语句的后面，作为分组的依据；要么被包含在聚合函数中。group by是对结果集分组，而不是查询字段分组。**  
&emsp; **<font color = "red">Group By含有去重效果。</font>**  
1. 普通Limit语句需要全表扫描。  
&emsp; 建立主键或唯一索引，利用索引：`SELECT * FROM 表名称 WHERE id_pk > (pageNum*10) LIMIT M`  
&emsp; 基于索引再排序：`SELECT * FROM 表名称 WHERE id_pk > (pageNum*10) ORDER BY id_pk ASC LIMIT M`
2. **<font color = "blue">ORDER BY与limit（分页再加排序）</font>**  
&emsp; ORDER BY排序后，用LIMIT取前几条，发现返回的结果集的顺序与预期的不一样。    
&emsp; 如果order by的列有相同的值时，MySQL会随机选取这些行，为了保证每次都返回的顺序一致可以额外增加一个排序字段（比如：id），用两个字段来尽可能减少重复的概率。  

#### 1.5.1.2. 连接查询
1. **关键字in：**  
&emsp; **<font color = "clime">in查询里面的数量最大只能1000。</font>**  
&emsp; **<font color = "red">确定给定的值是否与子查询或列表中的值相匹配。in在查询的时候，首先查询子查询的表，然后将内表和外表做一个笛卡尔积，然后按照条件进行筛选。所以</font><font color = "clime">相对内表比较小的时候，in的速度较快。</font>**  
2. exists指定一个子查询，检测行的存在。<font color = "clime">遍历循环外表，然后看外表中的记录有没有和内表的数据一样的。匹配上就将结果放入结果集中。</font><font color = "red">exists内层查询语句不返回查询的记录，而是返回一个真假值。</font>  
&emsp; **<font color = "clime">in和exists的区别：</font><font color = "red">如果子查询得出的结果集记录较少，主查询中的表较大且又有索引时应该用in，反之如果外层的主查询记录较少，子查询中的表大，又有索引时使用exists。</font>**  
3. **UNION与UNION ALL：** 默认地，UNION 操作符选取不同的值。如果允许重复的值，请使用UNION ALL。  

#### 1.5.1.3. ~~高级查询~~


### 1.5.2. MySql函数
&emsp; **<font color = "red">控制流程函数、字符串函数、数学函数、日期时间函数、聚合函数</font>**  


### 1.5.3. MySql优化
&emsp; <font color = "red">MySql性能由综合因素决定，抛开业务复杂度，影响程度依次是硬件配置、MySQL配置、数据表设计、索引优化。</font>  
1. SQL语句的优化。  
    &emsp; `对查询语句的监控、分析、优化是SQL优化的一般步骤。`常规调优思路：  
    1. 查看慢查询日志slowlog，分析slowlog，分析出查询慢的语句。  
    2. 按照一定优先级，进行一个一个的排查所有慢语句。  
    3. 分析top sql，进行explain调试，查看语句执行时间。  
    4. 调整[索引](/docs/SQL/7.index.md)或语句本身。 
2. 表结构设计： **<font color = "red">单库单表无法满足时，可以拆分表结构（主从复制、分库分表），或者使用ES搜索引擎。</font>**  
3. 服务器的优化。  

#### 1.5.3.1. SQL分析
1. **<font color = "clime">SQL分析语句有EXPLAIN与explain extended、show warnings、proceduer analyse、profiling、trace。</font>**  
2. <font color = "red">用explain extended查看执行计划会比explain多一列filtered。filtered列给出了一个百分比的值，这个百分比值和rows列的值一起使用，可以估计出那些将要和explain中的前一个表进行连接的行的数目。前一个表就是指explain的id列的值比当前表的id小的表。</font>  
&emsp; mysql中有一个explain 命令可以用来分析select 语句的运行效果，例如explain可以获得select语句使用的索引情况、排序的情况等等。除此以外，explain 的extended 扩展能够在原本explain的基础上额外的提供一些查询优化的信息，这些信息可以通过mysql的show warnings命令得到。  
3. profiling  
&emsp; 使用profiling命令可以了解SQL语句消耗资源的详细信息（每个执行步骤的开销）。可以清楚了解到SQL到底慢在哪个环节。   
4. trace  
&emsp; 查看优化器如何选择执行计划，获取每个可能的索引选择的代价。  


##### 1.5.3.1.1. Expain
&emsp; expain信息列分别是id、select_type、table、partitions、 **<font color = "red">type</font>** 、possible_keys、 **<font color = "red">key</font>** 、 **<font color = "red">key_len</font>** 、ref、rows、filtered、 **<font color = "red">Extra</font>** 。  
* **<font color = "clime">type单表查询类型要达到range级别（只检索给定范围的行，使用一个索引来选择行，非全表扫描）。</font>**  
* key_len表示使用的索引长度，key_len可以衡量索引的好坏，key_len越小 索引效果越好。 **<font color = "blue">可以根据key_len来判断联合索引是否生效。</font>**  
* extra额外的信息，常见的不太友好的值，如下：Using filesort，Using temporary。   


#### 1.5.3.2. SQL优化
1. 基本查询优化：  
2. 子查询优化：
2. 关联查询优化：使用索引、 **<font color = "bllue">驱动表选择、条件谓词下推</font>** ......  
&emsp; 谓词下推，就是在将过滤条件下推到离数据源更近的地方，最好就是在table_scan时就能过滤掉不需要的数据。  

#### 1.5.3.3. 索引优化
1. 创建索引：为了使索引的使用效率更高，在创建索引时，必须考虑在哪些字段上创建索引和创建什么类型的索引。  
2. 索引失效。  
3. 索引条件下推：  
&emsp; 索引下推简而言之就是在复合索引由于某些条件（比如 like %aa）失效的情况下，当存在失效的过滤字段在索引覆盖范围内，使用比较的方式在不回表的情况下进一步缩小查询的范围。其实就是对索引失效的进一步修复。  
&emsp; **<font color = "clime">~~MySQL 5.6 引入了「索引下推优化」，可以在索引遍历过程中，对索引中包含的字段先做判断，直接过滤掉不满足条件的记录，减少回表次数。~~</font>**  
    * 关闭ICP：索引--->回表--->条件过滤。  
    * 开启ICP：索引--->条件过滤--->回表。</font>在支持ICP后，`MySQL在取出索引数据的同时，判断是否可以进行where条件过滤，`<font color = "blue">将where的部分过滤操作放在存储引擎层提前过滤掉不必要的数据，</font>减少了不必要数据被扫描带来的IO开销。  

#### 1.5.3.4. 碎片优化


### 1.5.4. 数据库分布式
#### 1.5.4.1. 大数据量操作

#### 1.5.4.2. MySql瓶颈

#### 1.5.4.3. 数据库分布式
&emsp; **数据库拆分过程基本遵循的顺序是：1).垂直拆分(业务拆分)、2).读写分离、3).分库分表(水平拆分)。每个拆分过程都能解决业务上的一些问题，但同时也面临了一些挑战。**  
1.  **分表和分区的区别：**  
    1. 实现方式上：
        * mysql的分表是真正的分表，一张表分成很多表后，每一个小表都是完整的一张表，都对应三个文件，一个.MYD数据文件，.MYI索引文件，.frm表结构文件。  
        * 分区不一样，一张大表进行分区后，还是一张表，不会变成多张表，但是存放数据的区块变多了。  
    2. 数据处理上： 
        * 分表后，数据都是存放在分表里，总表只是一个外壳，存取数据发生在一个一个的分表里面。  
        * 分区不存在分表的概念，分区只不过把存放数据的文件分成了许多小块，分区后的表还是一张表。数据处理还是由自己来完成。  
2. **分表和分区的联系：**  
    1. 都能提高mysql的性能，在高并发状态下都有一个良好的表面。 
    2. **<font color = "clime">分表和分区不矛盾，可以相互配合。</font>** 对于那些大访问量，并且表数据比较多的表，可以采取分表和分区结合的方式(如果merge这种分表方式，不能和分区配合的话，可以用其他的分表试)，访问量不大，但是表数据很多的表，可以采取分区的方式等。
3. **分库分表与读写分离：** `读写分离实现了数据库读能力的水平扩展，分库分表实现了写能力的水平扩展。`  
    1. 存储能力的水平扩展：在读写分离的情况下，每个集群中的master和slave基本上数据是完全一致的，从存储能力来说，存在海量数据的情况下，可能由于磁盘空间的限制，无法存储所有的数据。而在分库分表的情况下，可以搭建多个mysql主从复制集群，每个集群只存储部分分片的数据，实现存储能力的水平扩展。  
    2. 写能力的水平扩展：在读写分离的情况下，由于每个集群只有一个master，所有的写操作压力都集中在这一个节点上，在写入并发非常高的情况下，这里会成为整个系统的瓶颈。  

&emsp; 而在分库分表的情况下，每个分片所属的集群都有一个master节点，都可以执行写入操作，实现写能力的水平扩展。此外减小建立索引开销，降低写操作的锁操作耗时等，都会带来很多显然的好处。  

#### 1.5.4.4. 主从复制
##### 1.5.4.4.1. 主从复制原理  
1. 对于每一个主从复制的连接，都有三个线程。拥有多个从库的主库为每一个连接到主库的从库创建一个binlog输出线程，每一个从库都有它自己的I/O线程和SQL线程。  
2. 同步方式可以划分为：异步、半同步和同步。`在MySQL5.7中，带来了全新的多线程复制技术。`  
3. 复制类型有三种：基于行的复制、基于语句的复制、混合模式复制。  
    * 并非所有修改数据的语句都可以使用基于语句的复制进行复制。使用基于语句的复制时，任何非确定性行为都难以复制。  
    * 基于行的复制会产生大量的日志。  
    * MySQL5.1及其以后的版本推荐使用混合模式的复制，它是<font color = "clime">根据事件的类型实时的改变binlog的格式。当设置为混合模式时，默认为基于语句的格式，但在特定的情况下它会自动转变为基于行的模式。</font>  

##### 1.5.4.4.2. 主从复制实现


##### 1.5.4.4.3. 主从复制问题
1. 复制过程
	1. 大对象blog,text传输： **<font color = "clime">解决的办法就是在主从库上增加max_allowed_packet参数的大小。</font>**  
2. 错误
	1. 主从不一致后锁表 
	2. 跳过错误
	3. 数据损坏或丢失  
		1. 主库意外关闭  
		2. 备库意外关闭
		3. 主库二进制日志损坏
		4. 备库中继日志损坏
		5. 二进制日志与InnoDB事务日志不同步
	4. 未定义的服务器ID
3. 性能
	1. 如何查看主从延迟？  
	2. 产生延迟的两种方式：
		1. `突然产生延迟，然后再跟上。可以通过备库上的慢查询日志来进行优化。`在备库上开启log_slow_slave_statement选项，可以在慢查询日志中记录复制线程执行的语句。
		2. 稳定的延迟增大
	3. 并行复制  
4. <font color = "red">复制问题要分清楚是master的问题，还是slave的问题。master问题找二进制日志binlog，slave问题找中继日志relaylog。</font>  

##### 1.5.4.4.4. 高可用实现


##### 1.5.4.4.5. 读写分离实现
1. 应用层解决方案：  
    1. 驱动实现
        * com.mysql.jdbc.ReplicationDriver
        * Sharding-jdbc
    2. MyBatis plugin(sqlType: select,update,insert)  
    3. SpringAOP + mybatis plugin + 注解
    4. Spring动态数据源 + mybatis plugin
2. 常见代理中间件有MyCat...  

#### 1.5.4.5. 分区



#### 1.5.4.6. 分库分表
##### 1.5.4.6.1. 分库分表
1. 数据切分方式：  
    * 垂直分库，一般根据业务维度拆分，分布式项目中单项目单库。  
    * **<font color = "clime">`水平分库主要根据用户属性（如地市）拆分物理数据库。`一种常见的方式是将全省划分为多个大区。可以复合分片字段拆分，即按照用户属性（如地市）拆分后，再按照时间拆分。</font>**  
    * 垂直分表，基于列字段进行的。一般是表中的字段较多，将不常用的，数据较大，长度较长（比如text类型字段）的拆分到“扩展表”。  
    * ~~水平分表：针对数据量比较大的单张表。~~ **<font color = "red">MySql水平分表必须使用MyISAM引擎。</font>**  
2. 水平分库无论怎么分，只要能通过拆分字段和分片策略，找到具体的库就可以。  

##### 1.5.4.6.2. 分库分表查询
1. 非partition key的查询 / 分库分表多维度查询  
	* 基因法
	* 映射法
	* 冗余法
	* NoSQL法：ES、Hbase等。  
    
        **<font color = "blue">B2B模式（有买家、卖家），订单表采用`冗余法（买家库和卖家库）和基因法`结合。</font>**  
2. 跨分片的排序order by、分组group by以及聚合count等函数  
&emsp; 这些是一类问题，因为它们<font color = "red">都需要基于全部数据集合进行计算。多数的代理都不会自动处理合并工作，部分支持聚合函数MAX、MIN、COUNT、SUM。</font>  
&emsp; **<font color = "red">解决方案：分别在各个节点上执行相应的函数处理得到结果后，在应用程序端进行合并。</font>** 每个结点的查询可以并行执行，因此很多时候它的速度要比单一大表快很多。但如果结果集很大，对应用程序内存的消耗是一个问题。  
3. 跨分片的排序分页  
&emsp; <font color = "red">一般来讲，分页时需要按照指定字段进行排序。`当排序字段是分片字段时，通过分片规则可以比较容易定位到指定的分片；`而当排序字段非分片字段时，情况就会变得比较复杂了。</font>为了最终结果的准确性，需要在不同的分片节点中将数据进行排序并返回，并将不同分片返回的结果集进行汇总和再次排序，最后再返回给用户。  
4. 跨节点Join的问题  
&emsp; tddl、MyCAT等都支持跨分片join。如果中间不支持，跨库Join的几种解决思路：  

	* `在程序中进行拼装。`  
	* 全局表
	* 字段冗余 

5. ~~**<font color = "blue">小结：分库分表分片键设计</font>**~~  
&emsp; ~~分库分表时设计拆分字段考虑因素：1). 是否有必要按照地区、时间拆分表；2)参考B2B模式（有买家、卖家），订单表采用`冗余法（买家库和卖家库）和基因法`结合。~~  
6. 基因法详解  
&emsp; 如果拆分成16张表，则需要截取二进制订单id的最后LOG(16,2)=4位，作为分库/分表基因，订单id的最后4位采用从用户id那边获取的4位基因。这样就满足按照订单号和用户（买家、卖家）id查询。   


#### 1.5.4.7. 数据迁移
1. 现在有一个未分库分表的系统，未来要分库分表，如何设计才可以让系统从未分库分表**动态切换**到分库分表上？
    * 停机迁移方案
    * 双写迁移方案 


