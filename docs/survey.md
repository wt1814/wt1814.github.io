

<!-- TOC -->

- [1. 知识点概况](#1-知识点概况)
    - [1.1. Java](#11-java)
    - [1.2. 设计模式](#12-设计模式)
    - [1.3. JVM](#13-jvm)
        - [1.3.1. 类加载](#131-类加载)
        - [1.3.2. 运行时数据区/内存结构](#132-运行时数据区内存结构)
        - [1.3.3. GC](#133-gc)
            - [1.3.3.1. 安全点和安全区域](#1331-安全点和安全区域)
            - [1.3.3.2. 回收算法与分代回收](#1332-回收算法与分代回收)
            - [1.3.3.3. 回收对象](#1333-回收对象)
            - [1.3.3.4. 垃圾回收器](#1334-垃圾回收器)
            - [1.3.3.5. JVM调优](#1335-jvm调优)
    - [1.4. 多线程和并发](#14-多线程和并发)
        - [1.4.1. 多线程](#141-多线程)
            - [1.4.1.1. 线程Thread](#1411-线程thread)
            - [1.4.1.2. 多线程-ThreadPoolExecutor](#1412-多线程-threadpoolexecutor)
            - [1.4.1.3. ForkJoinPool详解](#1413-forkjoinpool详解)
            - [1.4.1.4. CompletionService](#1414-completionservice)
            - [1.4.1.5. CompletableFuture](#1415-completablefuture)
        - [1.4.2. 并发编程](#142-并发编程)
            - [1.4.2.1. 并发编程原理](#1421-并发编程原理)
            - [1.4.2.2. 并发安全解决](#1422-并发安全解决)
        - [1.4.3. JUC](#143-juc)
            - [1.4.3.1. CAS](#1431-cas)
            - [1.4.3.2. AQS](#1432-aqs)
            - [1.4.3.3. 锁](#1433-锁)
                - [1.4.3.3.1. ReentrantLock，重入锁](#14331-reentrantlock重入锁)
                - [1.4.3.3.2. 读写锁](#14332-读写锁)
            - [1.4.3.4. Atomic](#1434-atomic)
                - [1.4.3.4.1. AtomicStampedReference与AtomicMarkableReference](#14341-atomicstampedreference与atomicmarkablereference)
                - [1.4.3.4.2. LongAdder](#14342-longadder)
            - [1.4.3.5. Collections](#1435-collections)
                - [1.4.3.5.1. CopyOnWriteArrayList](#14351-copyonwritearraylist)
                - [1.4.3.5.2. ConcurrentHashMap](#14352-concurrenthashmap)
                - [1.4.3.5.3. BlockingQueue](#14353-blockingqueue)
            - [1.4.3.6. tools](#1436-tools)
    - [1.5. 数据库](#15-数据库)
        - [1.5.1. SQL优化](#151-sql优化)
            - [1.5.1.1. SQL分析](#1511-sql分析)
            - [1.5.1.2. SQL优化](#1512-sql优化)
            - [1.5.1.3. 索引优化](#1513-索引优化)
        - [1.5.2. 分布式数据库](#152-分布式数据库)

<!-- /TOC -->


# 1. 知识点概况
## 1.1. Java
1. 集合
    1. 集合Collections
    2. HashMap
2. JDK 1.8
1. SPI
2. 泛型
    1. 为什么使用泛型？泛型的好处

## 1.2. 设计模式
1. 设计原则
2. 3种设计模式
3. 日常开发常用的设计模式
4. Spring中使用到的设计模式


## 1.3. JVM
### 1.3.1. 类加载
1. 类加载流程
2. 类加载器：双亲委派模型
3. 破坏双亲委派
    1. 为什么破坏？JDK、Tomcat、Spring
    2. `破坏双亲委派两种方式：`  
        1. 继承ClassLoader，重写loadClass()方法。  
        2. `使用线程上下文类加载器(Thread Context ClassLoader)`

### 1.3.2. 运行时数据区/内存结构
1. JVM内存结构
    1. 程序计数器
    2. JVM栈/【方法】【栈】
    3. 【GC】【堆】
    4. 方法区/永久代
    &emsp; jdk1.8及之后：无永久代。<font color = "clime">字符串常量池、静态变量（`值可变`）仍在堆，</font> 但类型信息、字段、`方法`、<font color = "red">常量（`值固定`）</font>保存在本地内存的元空间。  
    5. MetaSpace存储类的元数据信息
2. 类存储内存小结
    1. 变量
        1. 常量final static
        2. 静态变量static
        3. 全局变量
        4. 局部变量 
    2. ~~静态方法和实例方法~~  
    &emsp; `静态方法`会在程序运行的时候`直接装载进入方法区`。而实例方法会在new的时候以对象的方法装载进入堆中。  
    &emsp; 最大的区别在于内存的区别，由于main函数为static静态方法，会直接在运行的时候装载进入内存区，实例方法必须new，在堆中创建内存区域，再进行引用。  
3. 内存(堆栈)中的对象
    1. 创建对象
        1. **<font color = "clime">对象创建过程：1. 检测类是否被加载；2. 为对象分配内存；3. 将分配内存空间的对象初始化零值；4. 对对象进行其他设置；5.执行init方法。</font>**  
        2. ~~对象分配内存流程详解~~  
            1. 堆内存分配策略  
            2. 内存分配全流程  
            3. 分配内存两种方式  
            4. 线程安全问题
        3. 逃逸分析
4. 对象生命周期  
5. ~~对象大小~~  
6. 内存泄漏  

### 1.3.3. GC  
&emsp; ⚠️⚠️⚠️一句话小结：`垃圾回收器` 在 `安全点/安全区域` 采用`回收算法` `分代/整堆` 回收 `(堆)根不可达的对象 或 (方法区)类/常量`。  

#### 1.3.3.1. 安全点和安全区域  
&emsp; `在安全点上中断的是活跃运行的用户线程，对于已经挂起的线程该怎么处理呢？`**<font color = "blue">`已经挂起的线程`会被认定为处在`安全区域`内，中断的时候不需要考虑安全区域中的线程。</font>**  
&emsp; 当前安全区域的线程要被唤醒离开安全区域时，先检查能否离开，如果GC完成了，那么线程可以离开，否则它必须等待，直到收到安全离开的信号为止。  

#### 1.3.3.2. 回收算法与分代回收
1. GC算法  
2. 分代回收  
3. 跨代引用假说  
4. 各种GC：局部GC和整堆回收  

#### 1.3.3.3. 回收对象  
1. 堆中对象的存活
    1. 标记对象
    2. 进行回收（Object#finalize()⽅法）  
2. 方法区（类和常量）回收/类的卸载阶段  
3. null与GC  

#### 1.3.3.4. 垃圾回收器  
1. 垃圾回收器分类  
2. CMS  
    1. 特点  
    2. 回收流程：3次标记、2次清除  
        3. 预清理：（`三色标记法的漏标问题处理`） **<font color = "red">这个阶段是用来</font><font color = "blue">处理</font><font color = "clime">前一个并发标记阶段因为引用关系改变导致没有标记到的存活对象的。`如果发现对象的引用发生变化，则JVM会标记堆的这个区域为Dirty Card。`那些能够从Dirty Card到达的对象也被标记（标记为存活），当标记做完后，这个Dirty Card区域就会消失。</font>**  
        4. 可终止的预处理。这个阶段`尝试着去承担下一个阶段Final Remark阶段足够多的工作`。   
    3. 
3. G1  
    1. G1的收集过程可能有4个阶段：新生代GC、老年代并发标记周期、混合回收、如果需要可能会进行Full GC。   
    2. 最终标记： **<font color = "blue">去处理剩下的SATB（开始快照）日志缓冲区和所有更新，找出所有未被访问的存活对象，同时安全完成存活数据计算。</font>**   
4. 三色标记，并发标记阶段  
    1. 多标/错标（浮动垃圾）  
    2. 漏标：把本来应该存活的垃圾，标记为了死亡。这就会导致非常严重的错误。  

#### 1.3.3.5. JVM调优  
1. JVM调优  
2. JVM问题排查  


## 1.4. 多线程和并发  
### 1.4.1. 多线程
#### 1.4.1.1. 线程Thread  
1. ⚠️⚠️⚠️`对象` `执行动作` 形成`线程`。`影响线程状态的相关java类：Object类、Synchronized关键字、Thread类。`  
2. `⚠️⚠️⚠️线程的资源有不少，但应该包含CPU资源和锁资源这两类。`  
&emsp; **<font color = "clime">只有runnable到running时才会占用cpu时间片，其他都会出让cpu时间片。</font>**  
3. 线程状态切换  

#### 1.4.1.2. 多线程-ThreadPoolExecutor  
1. 线程池原理：  
    1. ThreadPoolExecutor#execute()执行机制  
    2. ~~线程池复用机制~~  
    3. 线程池保证核心线程不被销毁？    
2. 线程池的正确使用：  
    1. 线程池设置：  
        1. 使用自定义的线程池。合理设置隔离的线程池。  
        2. 确定线程池的大小（CPU可同时处理线程数量大部分是CPU核数的两倍）  
            1. CPU密集型的意思就是该任务需要大量运算，而没有阻塞，CPU一直全速运行。IO密集型，即该任务需要大量的IO，即大量的阻塞。  
            2. 阻塞队列设置： 如果响应时间要求较高的系统可以设置为0。队列大小一般为200或500-1000。  
    2. 线程池使用  
        1. 线程池中线程中异常尽量手动捕获
        &emsp; `当线程池中线程频繁出现未捕获的异常，那线程的复用率就大大降低了，需要不断地创建新线程。`  
3. 线程池的监控  

#### 1.4.1.3. ForkJoinPool详解  


#### 1.4.1.4. CompletionService  
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

#### 1.4.1.5. CompletableFuture  
&emsp; 对于jdk1.5的Future，虽然提供了异步处理任务的能力，但是获取结果的方式很不优雅，还是需要通过阻塞（或者轮训）的方式。如何避免阻塞呢？其实就是注册回调。  

&emsp; <font color = "red">从Java 8开始引入了CompletableFuture，它针对Future做了改进，可以传入回调对象，当异步任务完成或者发生异常时，自动调用回调对象的回调方法。</font>  
&emsp; ⚠️注：异步回调，主线程不会阻塞。  
&emsp; CompletableFuture提供了丰富的API对结果进行处理。   


### 1.4.2. 并发编程  
#### 1.4.2.1. 并发编程原理 
1. CPU多核缓存架构及JMM  
&emsp; `⚠⚠⚠声明：并发安全并不是java独有的，其他语言，不同操作系统都存在并发安全。究其原因是因为cpu多级缓存架构。`  
2. 并发安全问题产生原因  
    1. &emsp; **<font color = "clime">`【缓存不能及时刷新】/可见性 (【内存系统重排序】)` 和`【编译器优化】/有序性` 都是`重排序`的一种。</font>**   
    2. 重排序  
    3. 伪共享问题  
3. 操作系统解决并发安全问题
    1. 缓存一致性协议  
    2. 操作系统的内存屏障  
4. Java解决并发安全  
    1. JMM中的happens-before原则：JSR-133内存模型 **<font color = "red">使用`happens-before`的概念来阐述操作之间的`内存可见性`。</font>**  
    2. 内存屏障：，禁止处理器重排序，保障缓存一致性。    
        1. JVM底层简化了内存屏障硬件指令的实现。  
        2. 内存屏障两个作用：1).保障可见性；2）.保障有序性，阻止屏障两侧的指令重排序。   

#### 1.4.2.2. 并发安全解决  
1. 线程安全解决方案  
2. synchronized  
    1. Synchronized使用  
    2. Synchronized底层原理  
    3. Synchronized优化
3. Volatile  
    1. Volatile的特性  
    2. `Volatile底层原理（happens-before中Volatile的特殊规则）：`查看Volatile的汇编代码。  
    3. Volatile为什么不安全（不保证原子性，线程切换）？  
    4. volatile使用场景  
4. ThreadLocal  
    1. ThreadLocal原理  
        1. ThreadLocal是如何实现线程隔离的？   
        2. ThreadLocal源码/内存模型   
        3. ThreadLocal内存泄露  
        4. ThreadLocalMap的key被回收后，如何获取值？ 
    2.  ThreadLocal使用  
        1. 使用场景  
        2. ~~ThreadLocal三大坑~~
            1. 内存泄露
            2. ThreadLocal无法在`父子线程（new Thread()）`之间传递。使用类InheritableThreadLocal可以在子线程中取得父线程继承下来的值。   
            3. 线程池中线程上下文丢失。TransmittableThreadLocal是阿里巴巴开源的专门解决InheritableThreadLocal的局限性，实现线程本地变量在线程池的执行过程中，能正常的访问父线程设置的线程变量。  
            4. 并行流中线程上下文丢失。问题同线程池中线程上下文丢失。  
        3. ~~ThreadLocal优化：FastThreadLocal~~  
        &emsp; FastThreadLocal直接使用数组避免了hash冲突的发生，具体做法是：每一个FastThreadLocal实例创建时，分配一个下标index；分配index使用AtomicInteger实现，每个FastThreadLocal都能获取到一个不重复的下标。  

### 1.4.3. JUC  
#### 1.4.3.1. CAS  
1. CAS函数  
2. CAS缺点  

#### 1.4.3.2. AQS  
1. 执行 加锁/解锁（控制同步状态state） 、队列 、 阻塞/唤醒 这3个步骤。  
2. LockSupport（support，支持）是一个线程阻塞工具类，所有的方法都是静态方法，可以让线程在任意位置阻塞，当然阻塞之后肯定得有唤醒的方法。LockSupport主要有两类方法：park和unpark。  

#### 1.4.3.3. 锁  
##### 1.4.3.3.1. ReentrantLock，重入锁  
1. ReentrantLock特点  
2. ReentrantLock#lock()方法  

##### 1.4.3.3.2. 读写锁  
1. ReentrantReadWriteLock  
    1. 读写锁ReentrantReadWriteLock：读读共享，`读写互斥`，写写互斥。  
    2. **<font color = "red">ReentrantReadWriteLock缺点：`读写锁互斥`，只有当前没有线程持有读锁或者写锁时，才能获取到写锁，</font><font color = "clime">这`可能会导致写线程发生饥饿现象`，</font><font color = "red">即读线程太多导致写线程迟迟竞争不到锁而一直处于等待状态。StampedLock()可以解决这个问题。</font>**  
2. StampedLock，Stamped，有邮戳的  
    1. StampedLock有3种模式：写锁writeLock、悲观读锁readLock、乐观读锁tryOptimisticRead。  
    2. StampedLock通过乐观读锁tryOptimisticRead解决ReentrantReadWriteLock的写锁饥饿问题。乐观读锁模式下，一个线程获取的乐观读锁之后，不会阻塞其他线程获取写锁。    
    3. **<font color = "clime">同时允许多个乐观读和一个写线程同时进入临界资源操作，那`读取的数据可能是错的怎么办？`</font>**    
    &emsp; **<font color = "clime">`通过版本号控制。`</font>** 乐观读不能保证读取到的数据是最新的，所以`将数据读取到局部变量的时候需要通过 lock.validate(stamp) 校验是否被写线程修改过`，若是修改过则需要上悲观读锁，再重新读取数据到局部变量。`即乐观读失败后，再次使用悲观读锁。`  

#### 1.4.3.4. Atomic
##### 1.4.3.4.1. AtomicStampedReference与AtomicMarkableReference
1. AtomicStampedReference每次修改都会让stamp值加1，类似于版本控制号。 
2. **<font color = "clime">AtomicStampedReference可以知道引用变量中途被更改了几次。有时候，并不关心引用变量更改了几次，只是单纯的关心是否更改过，所以就有了AtomicMarkableReference。</font>**  

##### 1.4.3.4.2. LongAdder
1. LongAdder重要属性：有一个全局变量`volatile long base`值、父类Striped64中存在一个`volatile Cell[] cells;`数组，其长度是2的幂次方。  
2. LongAdder原理：  
    1. CAS操作：当并发不高的情况下都是通过CAS来直接操作base值，如果CAS失败，则针对LongAdder中的Cell[]数组中的Cell进行CAS操作，减少失败的概率。
    2. 解决伪共享：每个Cell都使用@Contended注解进行修饰，而@Contended注解可以进行缓存行填充，从而解决伪共享问题。  

#### 1.4.3.5. Collections  
##### 1.4.3.5.1. CopyOnWriteArrayList
1. CopyOnWriteArrayList  
&emsp; CopyOnWrite，写时复制。`读操作时不加锁以保证性能不受影响。`  
&emsp; **<font color = "clime">`写操作时加锁，复制资源的一份副本，在副本上执行写操作，写操作完成后将资源的引用指向副本。`</font>** CopyOnWriteArrayList源码中，`基于ReentrantLock保证了增加元素和删除元素动作的互斥。`   
&emsp; **优点：** 可以对CopyOnWrite容器进行并发的读，而不需要加锁，因为当前容器不会添加任何元素。`所以CopyOnWrite容器也是一种读写分离的思想，读和写不同的容器。`  
&emsp; **<font color = "clime">缺点：** **1.占内存（写时复制，new两个对象）；2.不能保证数据实时一致性。</font>**  
&emsp; **使用场景：** <font color = "clime">CopyOnWrite并发容器用于读多写少的并发场景。比如白名单，黑名单，商品类目的访问和更新场景。</font>  

##### 1.4.3.5.2. ConcurrentHashMap
1. ConcurrentHashMap，JDK1.8  
	1. put()流程  
	2. 协助扩容  
	&emsp; `ConcurrentHashMap并没有直接加锁，而是采用CAS实现无锁的并发同步策略，最精华的部分是它可以利用多线程来进行协同扩容。简单来说，它把Node数组当作多个线程之间共享的任务队列，然后通过维护一个指针来划分每个线程锁负责的区间，每个线程通过区间逆向遍历来实现扩容，一个已经迁移完的bucket会被替换为一个ForwardingNode节点，标记当前bucket已经被其他线程迁移完了。`   
	3. &emsp; `ConcurrentHashMap并没有直接加锁，而是采用CAS实现无锁的并发同步策略，最精华的部分是它可以利用多线程来进行协同扩容。简单来说，它把Node数组当作多个线程之间共享的任务队列，然后通过维护一个指针来划分每个线程锁负责的区间，每个线程通过区间逆向遍历来实现扩容，一个已经迁移完的bucket会被替换为一个ForwardingNode节点，标记当前bucket已经被其他线程迁移完了。`   
2. ConcurrentHashMap，JDK1.7  

##### 1.4.3.5.3. BlockingQueue  


#### 1.4.3.6. tools  
1. CountDownLatch  
    &emsp; <font color = "clime">CountDownLatch是由AQS实现的，创建CountDownLatch时设置计数器count，其实就是设置AQS.state=count，也就是重入次数。  
    * await()方法调用获取锁的方法，由于AQS.state=count表示锁被占用且重入次数为count，所以获取不到锁线程被阻塞并进入AQS队列。  
    * countDown()方法调用释放锁的方法，每释放一次AQS.state减1，当AQS.state变为0时表示处于无锁状态了，就依次唤醒AQS队列中阻塞的线程来获取锁，继续执行逻辑代码。</font>  
2. CyclicBarrier  
3. Semaphore通常用于限制可以访问某些资源（物理或逻辑的）的线程数目。Semaphore可以用来构建一些对象池，资源池之类的，比如数据库连接池。  


## 1.5. 数据库
### 1.5.1. SQL优化  
#### 1.5.1.1. SQL分析  
1. `小结：`**<font color = "clime">SQL分析语句有profiling（`资源`）、proceduer analyse（`表结构`）、EXPLAIN与explain extended、show warnings（警告）、trace（执行计划）。</font>**  
2. &emsp; expain信息列分别是id、select_type、table、partitions、`【type】`、possible_keys、`key`、`key_len`、ref、`rows`、filtered、 `【Extra】`。  
&emsp; `⚠注：一个表的连接类型，是否使用到了索引，索引长度，扫描行数，还有额外信息。`  
* **<font color = "clime">`type，单表的访问方法。`单表查询类型要达到range级别（只检索给定范围的行，使用一个索引来选择行，非全表扫描）。</font>**  
* key_len表示使用的索引长度，key_len可以衡量索引的好坏。key_len越小，索引效果越好。 **<font color = "blue">可以根据key_len来判断联合索引是否生效。</font>**  
* **<font color = "red">extra：额外的信息，该列包含MySQL解决查询的详细信息。注意，常见的不太友好的值，如Using filesort（外部排序）、Using temporary（使用了临时表），意思MYSQL根本不能使用索引，常出现在使用order by。</font>**  


#### 1.5.1.2. SQL优化  


#### 1.5.1.3. 索引优化  


### 1.5.2. 分布式数据库  




