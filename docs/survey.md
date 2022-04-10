

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
            - [1.5.2.1. MySql瓶颈](#1521-mysql瓶颈)
            - [1.5.2.2. 数据库拆分](#1522-数据库拆分)
            - [1.5.2.3. 主从复制](#1523-主从复制)
            - [1.5.2.4. 高可用](#1524-高可用)
            - [1.5.2.5. 读写分离实现](#1525-读写分离实现)
            - [1.5.2.6. 分区](#1526-分区)
            - [1.5.2.7. 分库分表](#1527-分库分表)
                - [1.5.2.7.1. 分库分表](#15271-分库分表)
                - [1.5.2.7.2. 分库分表查询](#15272-分库分表查询)
                - [1.5.2.7.3. 数据迁移](#15273-数据迁移)
        - [1.5.3. MySql架构](#153-mysql架构)
            - [1.5.3.1. MySql运行流程](#1531-mysql运行流程)
            - [1.5.3.2. Server层之binLog日志](#1532-server层之binlog日志)
            - [1.5.3.3. 存储引擎层](#1533-存储引擎层)
            - [1.5.3.4. InnoDB体系结构](#1534-innodb体系结构)
                - [1.5.3.4.1. InnoDB内存结构-性能](#15341-innodb内存结构-性能)
                - [1.5.3.4.2. InnoDB磁盘结构-可靠性](#15342-innodb磁盘结构-可靠性)
                - [1.5.3.4.3. 两阶段提交和崩溃恢复](#15343-两阶段提交和崩溃恢复)
            - [1.5.3.5. 索引事务锁](#1535-索引事务锁)
                - [1.5.3.5.1. 索引](#15351-索引)
                - [1.5.3.5.2. MySql事务](#15352-mysql事务)
                - [1.5.3.5.3. MVCC](#15353-mvcc)
                - [1.5.3.5.4. MySql锁](#15354-mysql锁)
    - [1.6. Spring](#16-spring)
        - [1.6.1. Spring IOC](#161-spring-ioc)
        - [1.6.2. Spring DI](#162-spring-di)
            - [1.6.2.1. DI中循环依赖](#1621-di中循环依赖)
            - [1.6.2.2. Bean生命周期](#1622-bean生命周期)
            - [1.6.2.3. 容器相关特性](#1623-容器相关特性)
        - [1.6.3. Spring AOP](#163-spring-aop)
        - [1.6.4. Spring事务](#164-spring事务)
        - [1.6.5. SpringMVC](#165-springmvc)
    - [1.7. Mybatis](#17-mybatis)
    - [1.8. SpringBoot](#18-springboot)
    - [1.9. Spring Cloud](#19-spring-cloud)
        - [1.9.1. Zuul](#191-zuul)
        - [1.9.2. Hytrix](#192-hytrix)
    - [1.10. Dubbo](#110-dubbo)
        - [1.10.1. Dubbo和Spring Cloud](#1101-dubbo和spring-cloud)
        - [1.10.2. Dubbo](#1102-dubbo)
    - [1.11. Zookeeper](#111-zookeeper)
    - [1.12. 分布式](#112-分布式)
        - [1.12.1. CAP](#1121-cap)
        - [1.12.2. 分布式ID](#1122-分布式id)
        - [1.12.3. 分布式事务](#1123-分布式事务)
            - [1.12.3.1. PC](#11231-pc)
            - [1.12.3.2. PC](#11232-pc)
            - [1.12.3.3. TCC](#11233-tcc)
            - [1.12.3.4. saga](#11234-saga)
            - [1.12.3.5. 消息模式，异步方式](#11235-消息模式异步方式)
            - [1.12.3.6. 分布式事务框架Seata](#11236-分布式事务框架seata)
            - [1.12.3.7. 分布式事务的选型](#11237-分布式事务的选型)
        - [1.12.4. 分布式锁](#1124-分布式锁)
            - [1.12.4.1. RedisLock](#11241-redislock)
            - [1.12.4.2. Redisson](#11242-redisson)
            - [1.12.4.3. ZK分布式锁](#11243-zk分布式锁)
            - [1.12.4.4. MySql分布式锁](#11244-mysql分布式锁)
            - [1.12.4.5. 分布式锁选型（各种锁的对比）](#11245-分布式锁选型各种锁的对比)
    - [1.13. 并发系统三高](#113-并发系统三高)
    - [1.14. Redis](#114-redis)
        - [1.14.1. Redis数据类型](#1141-redis数据类型)
        - [1.14.2. 数据结构](#1142-数据结构)
        - [1.14.3. Redis原理](#1143-redis原理)
            - [1.14.3.1. Redis内存操作](#11431-redis内存操作)
        - [1.14.4. Redis内置功能](#1144-redis内置功能)
        - [1.14.5. Redis高可用](#1145-redis高可用)
        - [1.14.6. Redis常见问题与优化](#1146-redis常见问题与优化)
    - [1.15. 分布式限流和降级](#115-分布式限流和降级)
    - [1.16. mq](#116-mq)
        - [1.16.1. mq](#1161-mq)
        - [1.16.2. kafka](#1162-kafka)
    - [1.17. 网络IO](#117-网络io)
        - [1.17.1. IO模型](#1171-io模型)
        - [1.17.2. IO性能优化之零拷贝](#1172-io性能优化之零拷贝)

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
1. 创建索引  
2. 索引失效
3. 覆盖索引  
4. 索引条件下推  
&emsp; 索引下推简而言之就是在复合索引由于某些条件（比如 like %aa）失效的情况下，当存在失效的过滤字段在索引覆盖范围内，使用比较的方式在【不回表】的情况下进一步缩小查询的范围。其实就是对索引失效的进一步修复。 

### 1.5.2. 分布式数据库  
#### 1.5.2.1. MySql瓶颈  
&emsp; <font color = "clime">`不管是IO瓶颈，还是CPU瓶颈，最终都会导致数据库的活跃连接数增加，进而逼近甚至达到数据库可承载活跃连接数的阈值。在业务Service来看就是，可用数据库连接少甚至无连接可用。`</font>  

#### 1.5.2.2. 数据库拆分  

#### 1.5.2.3. 主从复制  
1. 主从复制原理  
    1. 对于每一个主从复制的连接，都有三个线程。  
    2. 同步方式可以划分为：异步、半同步和同步。`在MySQL5.7中，带来了全新的多线程复制技术。`  
    3. 复制类型有三种：基于行的复制、基于语句的复制、混合模式复制。  
    &emsp; 注：Mysql到Elasticsearch实时增量同步，多采用基于行复制。    
    &emsp; MySQL5.1及其以后的版本推荐使用混合模式的复制，它是<font color = "clime">根据事件的类型实时的改变binlog的格式。当设置为混合模式时，默认为基于语句的格式，但在特定的情况下它会自动转变为基于行的模式。</font>  

#### 1.5.2.4. 高可用  


#### 1.5.2.5. 读写分离实现  
&emsp; 读写分离的实现，`可以在应用层解决，也可以通过中间件实现。`  

#### 1.5.2.6. 分区  

#### 1.5.2.7. 分库分表  
##### 1.5.2.7.1. 分库分表  
**<font color = "clime">`水平分库`主要根据`用户属性（如地市）`拆分物理数据库。一种常见的方式是将全省划分为多个大区。`可以复合分片字段拆分，即按照用户属性（如地市）拆分后，再按照时间拆分。`</font>**  

##### 1.5.2.7.2. 分库分表查询  
1. 非partition key的查询 / 分库分表多维度查询  
    1. 冗余法、映射法、NoSQL法：ES、Hbase等。
    2. 基因法：    
    &emsp; 如果拆分成16张表，则需要截取二进制订单id的最后LOG(16,2)=4位，作为分库/分表基因，订单id的最后4位采用从用户id那边获取的4位基因。这样就满足按照订单号和用户（买家、卖家）id查询。   
2. 跨分片的分组group by以及聚合count等函数  
    &emsp; **<font color = "red">解决方案：分别在各个节点上执行相应的函数处理得到结果后，在应用程序端进行合并。</font>** 每个结点的查询可以并行执行，因此很多时候它的速度要比单一大表快很多。但如果结果集很大，对应用程序内存的消耗是一个问题。  
3. 跨分片的排序分页  
    &emsp; 常见的分片策略有随机分片和连续分片这两种。“跨库分页”的四种方案：  
    1. 全局视野法、业务折衷法-禁止跳页查询（对应es中的scroll方法）、业务折衷法-允许模糊数据  
    2. 终极武器-二次查询法  
    &emsp; 第一次查询：按照`limit 总数据/分库数,分页数`查询，获取到最小排序字段值和每个分库的最大排序字段值。  
    &emsp; 第二次查询：`between` 最小排序字段值,最大排序字段值。  
4. 跨节点Join的问题  

##### 1.5.2.7.3. 数据迁移  


### 1.5.3. MySql架构
#### 1.5.3.1. MySql运行流程
1. MySQL整个查询执行过程，总的来说分为5个步骤。 
3. `MySQL更新流程：`  
    1. 事务提交前 --- **<font color = "clime">内存操作</font>** ：  
        1. 数据加载到缓冲池buffer poll；  
        2. `写回滚日志undo log；`  
        3. 更新（update 语句）缓冲池数据；  
        4. 写redo log buffer。  
    2. 事务提交：`redo log与bin log两阶段提交。`  
    3. 事务提交后：后台线程将buffer poll中数据落盘。  
    ![image](http://www.wt1814.com/static/view/images/SQL/sql-174.png)  
    ![image](http://www.wt1814.com/static/view/images/SQL/sql-183.png)  

#### 1.5.3.2. Server层之binLog日志  
1. **<font color = "clime">binlog是mysql的逻辑日志，并且由Server层进行记录，`使用任何存储引擎的mysql数据库都会记录binlog日志`。</font>**  
2. 在实际应用中，主要用在两个场景：主从复制和数据恢复。  


#### 1.5.3.3. 存储引擎层  



#### 1.5.3.4. InnoDB体系结构  
##### 1.5.3.4.1. InnoDB内存结构-性能  
1. InnoDB内存主要有两大部分：缓冲池、重做日志缓冲。  
&emsp; InnoDB`内存缓冲池中的数据page要完成持久化`的话，是通过两个流程来完成的，`一个是脏页落盘；一个是预写redo log日志`。  


##### 1.5.3.4.2. InnoDB磁盘结构-可靠性  
1. undoLog  
2. redoLog  
3. DoubleWrite  

##### 1.5.3.4.3. 两阶段提交和崩溃恢复  


#### 1.5.3.5. 索引事务锁  
##### 1.5.3.5.1. 索引  
1. 评价一个数据结构作为索引的优劣最重要的指标就是在查找过程中磁盘I/O操作次数的渐进复杂度。  
2. InnoDB使用的数据结构选择。  
3. 联合索引(col1, col2,col3)也是一棵B+Tree，其`非叶子节点存储的是第一个关键字的索引`，而`叶节点存储的则是三个关键字col1、col2、col3三个关键字的数据，且按照col1、col2、col3的顺序进行排序`。  


##### 1.5.3.5.2. MySql事务  
1. 并发事务处理带来的问题：脏读、丢失修改、不可重复读、幻读。   
2. 事务的四大特性（ACID）：原子性（Atomicity）、一致性（Consistency）、`隔离性（解决并发事务）`（Isolation）、持久性（Durability）。  
3. SQL标准定义了四个隔离级别（隔离性）：读取未提交、读取已提交、可重复读（可以阻止脏读和不可重复读，幻读仍有可能发生，但MySql的可重复读解决了幻读）、可串行化。  
4. Innodb事务实现原理  

##### 1.5.3.5.3. MVCC  
1. **<font color = "clime">多版本并发控制（MVCC）是MySql在在读取已提交、可重复读两种隔离级别解决`（1）读-写冲突的无锁并发控制`、`（2）解决并发事务问题（脏读、~~丢失修改~~、幻读、不可重复读）`。</font>**  
2. <font color = "clime">`MVCC与锁：MVCC主要解决读写问题，锁解决写写问题。`两者结合才能更好的控制数据库隔离性，保证事务正确提交。</font>  
2. **<font color = "clime">InnoDB有两个非常重要的模块来实现MVCC。</font>**   
3. Read View判断可见性的规则  
4. 在读取已提交、可重复读两种隔离级别下会使用MVCC。  
5. MVCC解决了幻读没有？  
    &emsp; 回答这个问题前，先要了解下什么是快照读、什么是当前读。  

        当前读:select...lock in share mode; select...for update;
        当前读:update、insert、delete
        快照读:不加锁的非阻塞读，select

    &emsp; 幻读：在一个事务中使用相同的 SQL 两次读取，第二次读取到了其他事务新插入的行，则称为发生了幻读。  
    &emsp; 例如：  
    1. 事务1第一次查询：select * from user where id < 10 时查到了 id = 1 的数据；  
    2. 事务2插入了 id = 2 的数据；  
    3. 事务1使用同样的语句第二次查询时，查到了 id = 1、id = 2 的数据，出现了幻读。  

    &emsp; MVCC解决了快照读的幻读：  
    &emsp; <font color = "clime">对于快照读，MVCC 因为从 ReadView读取，所以必然不会看到新插入的行，所以天然就解决了幻读的问题。</font>  
    &emsp; <font color = "clime">而对于当前读的幻读，MVCC是无法解决的。需要使用 Gap Lock 或 Next-Key Lock(Gap Lock + Record Lock)来解决。</font>其实原理也很简单，用上面的例子稍微修改下以触发当前读：select * from user where id < 10 for update。`若只有MVCC，当事务1执行第二次查询时，操作的数据集已经发生变化，所以结果也会错误；`当使用了 Gap Lock 时，Gap 锁会锁住 id < 10 的整个范围，因此其他事务无法插入 id < 10 的数据，从而防止了幻读。  
6. 小结：MVCC在在读取已提交隔离级别能解决脏读；在可重复读隔离级别：能解决不可重复读问题，其中在快照读下还能解决幻读问题。  

##### 1.5.3.5.4. MySql锁  
1. 锁的分类：1)共享读锁、排他写锁；2）锁的粒度：表锁、页锁、行锁；3）按使用方式（读写两步操作）：乐观锁、悲观锁。  
2. InnoDB共有七种类型的锁：共享/排它锁、（表级）意向锁、记录锁（Record lock）、间隙锁（Gap lock）、临键锁（Next-key lock）、插入意向锁、自增锁。  
3. InnoDB存储引擎的锁的算法有三种：行锁、间隙锁、临键锁，是记录锁与间隙锁的组合，它的封锁范围，既包含索引记录，又包含索引区间。  
4. 乐观锁和悲观锁：（修改数据都包含读和写两步） 
    1. 乐观锁
    2. 悲观锁  


## 1.6. Spring  
### 1.6.1. Spring IOC
1. Spring容器就是个Map映射, IOC底层就是反射机制，AOP底层是动态代理。   
&emsp; Spring中的IoC的实现原理就是工厂模式加反射机制。  
2. Spring bean容器刷新的核心，12个步骤完成IoC容器的创建及初始化工作。  
&emsp; **<font color = "blue">（⚠`利用工厂和反射创建Bean。主要包含3部分：1).容器本身--创建容器、2).容器扩展--预处理、后置处理器、3).事件，子容器，★★★实例化Bean。`）</font>**   


### 1.6.2. Spring DI  
1. 加载时机：SpringBean默认单例，非懒加载，即容器启动时就加载。  
2. 加载流程：  
    1. doCreateBean()创建Bean有三个关键步骤：2.createBeanInstance()实例化、5.populateBean()属性填充、6.initializeBean()初始化。  

#### 1.6.2.1. DI中循环依赖  


#### 1.6.2.2. Bean生命周期  
![image](http://www.wt1814.com/static/view/images/SSM/Spring/spring-10.png)  
&emsp; SpringBean的生命周期主要有4个阶段：  
1. 实例化（Instantiation），可以理解为new一个对象；
2. 属性赋值（Populate），可以理解为调用setter方法完成属性注入；
3. 初始化（Initialization），包含：  
    * 激活Aware方法  
    * 【前置处理】  
    * 激活自定义的init方法 
    * 【后置处理】 
4. 销毁（Destruction）---注册Destruction回调函数。 


#### 1.6.2.3. 容器相关特性  
1. FactoryBean  
2. Spring可二次开发常用接口（扩展性） 
    &emsp; Spring为了用户的开发方便和特性支持，开放了一些特殊接口和类，用户可进行实现或者继承，常见的有：  

    &emsp; **Spring IOC阶段：**  
    &emsp; [事件多播器](/docs/SSM/Spring/feature/EventMulticaster.md)  
    &emsp; [事件](/docs/SSM/Spring/feature/Event.md)  

    &emsp; **Spring DI阶段：**  
    &emsp; [Aware接口](/docs/SSM/Spring/feature/Aware.md)  
    &emsp; [后置处理器](/docs/SSM/Spring/feature/BeanFactoryPostProcessor.md)  
    &emsp; [InitializingBean](/docs/SSM/Spring/feature/InitializingBean.md)  

### 1.6.3. Spring AOP  
1. AOP功能  
2. AOP失效  
3. SpringAOP解析  
    1. **<font color = "blue">自动代理触发的时机：AspectJAnnotationAutoProxyCreator是一个后置处理器BeanPostProcessor，</font>** 因此Spring AOP是在这一步，进行代理增强！  
    2. **<font color = "clime">代理类的生成流程：1). `获取当前的Spring Bean适配的advisors；`2). `创建代理类`。</font>**   

### 1.6.4. Spring事务  
1. 事务属性  
2. 事务失效  

### 1.6.5. SpringMVC  

## 1.7. Mybatis  
1. Mybatis的功能架构分为三层：API接口层、核心处理层、基础支持层。  
2. Mybatis Sql执行流程。  
3. Mapper接口动态代理类的生成。  
4. Mybaits插件的实现主要用了拦截器、责任链和动态代理。 动态代理可以对SQL语句执行过程中的某一点进行拦截，当配置多个插件时，责任链模式可以进行多次拦截。 


## 1.8. SpringBoot
1. SpringBoot特点  
2. SpringBoot启动流程  
    1. 创建SpringApplication对象；  
        1. 构造过程一般是对构造函数的一些成员属性赋值，做一些初始化工作。SpringApplication有6个属性：资源加载器、资源类集合、应用类型、4).初始化器、5).监听器、包含main函数的主类。  
        2. SpringApplication初始化中第4步和第5步都是利用SpringBoot的SPI机制来加载扩展实现类。  
    2. 运行run()方法，分3步：创建 ---> 刷新 ---> 刷新后的后置处理   
    ![image](http://www.wt1814.com/static/view/images/microService/boot/boot-9.png)  
    ```text
    // 【7】刷新容器，IOC 容器初始化（如果是 Web 应用还会创建嵌入式的 Tomcat），扫描、创建、加载所有组件
    // 1）在context刷新前做一些准备工作，比如初始化一些属性设置，属性合法性校验和保存容器中的一些早期事件等；
    // 2）让子类刷新其内部bean factory,注意SpringBoot和Spring启动的情况执行逻辑不一样
    // 3）对bean factory进行配置，比如配置bean factory的类加载器，后置处理器等
    // 4）完成bean factory的准备工作后，此时执行一些后置处理逻辑，子类通过重写这个方法来在BeanFactory创建并预准备完成以后做进一步的设置
    // 在这一步，所有的bean definitions将会被加载，但此时bean还不会被实例化
    // 5）执行BeanFactoryPostProcessor的方法即调用bean factory的后置处理器：
    // BeanDefinitionRegistryPostProcessor（触发时机：bean定义注册之前）和BeanFactoryPostProcessor（触发时机：bean定义注册之后bean实例化之前）
    // 6）注册bean的后置处理器BeanPostProcessor，注意不同接口类型的BeanPostProcessor；在Bean创建前后的执行时机是不一样的
    // 7）初始化国际化MessageSource相关的组件，比如消息绑定，消息解析等
    // 8）初始化事件广播器，如果bean factory没有包含事件广播器，那么new一个SimpleApplicationEventMulticaster广播器对象并注册到bean factory中
    // 9）AbstractApplicationContext定义了一个模板方法onRefresh，留给子类覆写，比如ServletWebServerApplicationContext覆写了该方法来创建内嵌的tomcat容器
    // 10）注册实现了ApplicationListener接口的监听器，之前已经有了事件广播器，此时就可以派发一些early application events
    // 11）完成容器bean factory的初始化，并初始化所有剩余的单例bean。这一步非常重要，一些bean postprocessor会在这里调用。
    // 12）完成容器的刷新工作，并且调用生命周期处理器的onRefresh()方法，并且发布ContextRefreshedEvent事件
    ```
3. SpringBoot自动配置  
    1. 启动对象的注入  
    2. 自动装配入口  
4. 内置Tomcat  

## 1.9. Spring Cloud
&emsp; **<font color = "clime">Spring Cloud各组件运行流程：</font>**  
1. 外部或者内部的非Spring Cloud项目都统一通过微服务网关（Zuul）来访问内部服务。客户端的请求首先经过负载均衡（Ngnix），再到达服务网关（Zuul集群）；  
2. 网关接收到请求后，从注册中心（Eureka）获取可用服务；  
3. 由Ribbon进行负载均衡后，分发到后端的具体实例；  
4. 微服务之间也可通过Feign进行通信处理业务；  
5. Hystrix负责处理服务超时熔断；Hystrix dashboard，Turbine负责监控Hystrix的熔断情况，并给予图形化的展示；  
6. 服务的所有的配置文件由配置服务管理，配置服务的配置文件放在git仓库，方便开发人员随时改配置。  


### 1.9.1. Zuul  
&emsp; **<font color = "red">Zuul提供了四种过滤器的API，分别为前置（Pre）、路由（Route）、后置（Post）和错误（Error）四种处理方式。</font>**  

### 1.9.2. Hytrix  
1. 服务雪崩  
2. 熔断是一种降级策略。Hystrix中有三种降级方案(fallback，回退方案/降级处理方案)。  
3. <font color = "clime">熔断的对象是服务之间的请求；`1).熔断策略会根据请求的数量（资源）分为信号量和线程池，2).还有请求的时间（即超时熔断），3).请求错误率（即熔断触发降级）。`</font>  
&emsp; 信号量隔离：  
![image](http://www.wt1814.com/static/view/images/microService/SpringCloudNetflix/cloud-8.png)  


## 1.10. Dubbo  
### 1.10.1. Dubbo和Spring Cloud  
1. 两个框架在开始目标就不一致：Dubbo定位服务治理；Spirng Cloud是一个生态。  
2. Dubbo底层是使用Netty这样的NIO框架，是基于TCP协议传输的，配合以Hession序列化完成RPC通信。而SpringCloud是基于Http协议+Rest接口调用远程过程的通信。  

### 1.10.2. Dubbo  
1. Dubbo分层  
2. RPC层包含配置层config、代理层proxy、服务注册层register、路由层cluster、监控层monitor、远程调用层protocol。`⚠️dubbo远程调用可以分为2步：1.Invoker的生成；2.代理的生成。`      
    1. **<font color = "red">`服务代理层proxy`：服务接口透明代理，生成服务的客户端Stub和服务器端Skeleton，以ServiceProxy为中心，扩展接口为ProxyFactory。</font>**  
    &emsp; **<font color = "red">Proxy层封装了所有接口的透明化代理，而在其它层都以Invoker为中心，</font><font color = "blue">只有到了暴露给用户使用时，才用Proxy将Invoker转成接口，或将接口实现转成 Invoker，也就是去掉Proxy层RPC是可以Run的，只是不那么透明，不那么看起来像调本地服务一样调远程服务。</font>**  
    &emsp; dubbo实现接口的透明代理，封装调用细节，让用户可以像调用本地方法一样调用远程方法，同时还可以通过代理实现一些其他的策略，比如：负载、降级......  
    2. **<font color = "red">`远程调用层protocol`：封装RPC调用，以Invocation, Result为中心，扩展接口为Protocol, Invoker, Exporter。</font>**  
3. 服务提供者暴露服务的主过程：  
    ![image](http://www.wt1814.com/static/view/images/microService/Dubbo/dubbo-29.png)   
    ![image](http://www.wt1814.com/static/view/images/microService/Dubbo/dubbo-53.png)   
    服务代理层proxy生成Invoker，远程调用层protocol通过Protocol协议类的export方法暴露服务。  

## 1.11. Zookeeper  
1. ZK服务端  
    1. ZK服务端通过ZAB协议保证数据顺序一致性。消息广播（数据读写流程）和崩溃恢复。  
    2. 数据一致性  
2. ZK客户端  
    1. zookeeper引入了watcher机制来实现客户端和服务端的发布/订阅功能。 
3. ZK应用场景：统一命名服务，生成分布式ID、分布式锁、队列管理、元数据/配置信息管理，数据发布/订阅、分布式协调、集群管理，HA高可用性。  
4. ZK羊群效应  

## 1.12. 分布式  
### 1.12.1. CAP  

### 1.12.2. 分布式ID  

### 1.12.3. 分布式事务  
#### 1.12.3.1. PC  
1. 流程：  
    1). 执行事务，但不提交事务。  
2. 二阶段提交问题： 
    1). 正常运行：同步阻塞问题。2).丢失消息导致的数据不一致问题。 3).单点故障问题。 4). 二阶段无法解决的问题：协调者和参与者同时宕机。   

#### 1.12.3.2. PC  
1. 流程：3PC也就是多了一个阶段（一个询问的阶段），分别是准备、预提交和提交这三个阶段。  
2. 特点：  
    1. 【超时机制】也降低了参与者的阻塞范围，因为参与者不会一直持有事务资源并处于阻塞状态。  
    2. 解决单点故障问题。  
    3. 数据不一致问题依然存在。  

#### 1.12.3.3. TCC  
1. TCC是一种业务层面或者是应用层的两阶段、【补偿型】的事务。  
2. TCC是Try（检测及资源锁定或者预留）、Commit（确认）、Cancel（取消）的缩写，业务层面需要写对应的三个方法。  
3. TCC问题：  
    1. 允许空回滚  
    2. 接口幂等  
    3. 防止资源悬挂  


#### 1.12.3.4. saga  


#### 1.12.3.5. 消息模式，异步方式  
1. XA、TCC、Saga等都属于同步方式。还有基于消息的异步方式。   
2. 依据是否保证投递到订阅者，分为可靠消息及最大努力交付消息。可靠消息的两种实现方案：基于本地消息表、基于事务消息。  


#### 1.12.3.6. 分布式事务框架Seata  
&emsp; AT 模式是无侵入的分布式事务解决方案，适用于不希望对业务进行改造的场景，几乎0学习成本。  

#### 1.12.3.7. 分布式事务的选型  


### 1.12.4. 分布式锁  
1. 使用场景：1).实现幂等；2).避免破坏数据的正确性：在分布式环境下解决多实例对数据的访问一致性。  
2. 实现分布式锁的细节：确保互斥、不能死锁、避免活锁、实现更多锁特性：锁中断、锁重入、锁超时等。确保客户端只能解锁自己持有的锁。   


#### 1.12.4.1. RedisLock
1. redis加锁、解锁操作。  
2. RedLock红锁  
3. 使用redis分布式锁的注意点：  
    1. 采用Master-Slave模式  
    2. 

#### 1.12.4.2. Redisson  
1. RedissonLock解决客户端死锁问题（自动延期）  
    1. 死锁问题  
    2. Redisson实现了多种锁：重入锁、公平锁、联锁、红锁、读写锁、信号量Semaphore 和 CountDownLatch...   

#### 1.12.4.3. ZK分布式锁  
1. 对于ZK来说，实现分布式锁的核心是临时顺序节点和监听机制。 ZK实现分布式锁要注意羊群效应。  
2. ZooKeeper分布式锁的缺点： 1). 需要依赖zookeeper；2). 性能低。频繁地“写”zookeeper。集群节点数越多，同步越慢，获取锁的过程越慢。   

#### 1.12.4.4. MySql分布式锁  


#### 1.12.4.5. 分布式锁选型（各种锁的对比）


## 1.13. 并发系统三高  
&emsp; 三高：高性能、高可用，以及高可扩展。  


&emsp; 从架构视角来看，<font color = "red">`★★★秒杀系统本质是一个（分布式）高一致、（高并发）高性能、（高并发）高可用的三高系统。`</font>  

## 1.14. Redis
### 1.14.1. Redis数据类型  
1. Redis各个数据类型的使用场景：分析存储类型和可用的操作。  
2. Redis扩展数据类型  
&emsp; Bitmap：二值状态统计。常用场景：用户签到、统计活跃用户、用户在线状态。  
&emsp; HyperLogLog用于基数统计，例如UV（独立访客数）。  
3. redis Big key问题：  
    1. bigKey如何发现？
    2. 如何删除：Redis4.0支持异步删除。  
    3. 如何优化：1). 拆分；2). 本地缓存。  

### 1.14.2. 数据结构  
1. 底层数据结构  
    1. 3种链表：双端链表LinkedList、压缩列表Ziplist、快速列表Quicklist。  
    2. 整数集合  
    3. 跳跃表  
2. SDS详解    
    1. 属性  
    2. 字符串追加操作 
    3. Redis字符串的性能优势：动态扩展、避免缓冲区溢出、内存分配优化（空间预分配、惰性空间回收）。 
3. Dictht   
    1. rehash
    2. 渐进式rehash  
4. 数据类型  
    &emsp; **<font color = "clime">Redis根据不同的使用场景和内容大小来判断对象使用哪种数据结构，从而优化对象在不同场景下的使用效率和内存占用。</font>**  
    ![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-106.png)  

### 1.14.3. Redis原理  
&emsp; Redis的性能非常之高，每秒可以承受10W+的QPS，它如此优秀的性能主要取决于以下几个方面：  

1. 磁盘I/O：
    * 纯内存操作
    * [虚拟内存机制](/docs/microService/Redis/RedisVM.md)  
    * 合理的数据编码
2. ~~网络I/O：~~  
    * [Reids事件/使用IO多路复用技术Reactor](/docs/microService/Redis/RedisEvent.md)  
    * [合理的线程模型](/docs/microService/Redis/RedisMultiThread.md)  
    &emsp; 4核的机器建议设置为2或3个线程，8核的建议设置为6个线程， **<font color = "clime">`线程数一定要小于机器核数，尽量不超过8个。`</font>**    
    * [简单快速的Redis协议](/docs/microService/Redis/RESP.md)  
3. ......  

#### 1.14.3.1. Redis内存操作 
1. Redis过期键的删除  
2. Redis内存淘汰  
    1. Redis内存淘汰算法  
    2. Redis内存淘汰策略  
3. Redis持久化  
    1. RDB  
        1. bgsave 命令创建一个子进程，会有阻塞。  
    2. AOF  
        1. 写后日志  
        2. AOF流程重写阻塞。  
        3. 重写后的日志文件变小，降低了文件占用空间，除此之外，另一个目的是：更小的AOF 文件可以更快地被Redis加载。 
    3. AOF重写阻塞详解  

### 1.14.4. Redis内置功能  
1. Redis事务  
2. Redis和Lua  
3. RedisPipeline/批处理  
4. Redis实现消息队列  

### 1.14.5. Redis高可用  
1. Redis主从复制  
2. Redis读写分离  
3. Redis哨兵模式  
4. Redis集群模式  
    1. **<font color = "red">根据执行分片的位置，可以分为三种分片方式：</font>** 客户端分片、代理分片、服务器分片：官方Redis Cluster。  
    2. Redis集群的服务端  
        1. 数据分布  
        2. 集群限制  
        3. 故障转移  
    3. Redis集群的客户端  
        1. 采用客户端直连节点的方式。  
        2. 请求重定向  
        3. ASK重定向  
        
### 1.14.6. Redis常见问题与优化
    

## 1.15. 分布式限流和降级  
1. 限流算法  
2. 阿里Sentinel  


## 1.16. mq 
### 1.16.1. mq  
1. mq的好处  
2. 消息的推拉机制  
3. 消息队列使用事项  
	1. 保障高可用  
	2. 消息丢失  
	3. 重复消费 
	4. 顺序消费  
	5. 消息积压  

### 1.16.2. kafka  

1. kafka生产者  
	1. Producer发送消息的过程  
	2. 提升Producer的性能？异步，批量，压缩，多线程....  
2. 消息分区  
	1. 服务端物理分区分配  
	2. 客户端怎么分区  
		1. 生产者  
		2. 消费者  
	3. 分区数设置  
	4. 分区后保持有序，查看顺序消费。  
    5. 数据落盘：零拷贝、顺序读写、异步落盘。  
3. kafka消费者  
	1. 消费者/消费者组/消费者组重平衡  
	2. 消费者位移管理  
	3. 怎样消费  
		* 消费者分区分配策略：轮询RoundRobin、range策略。
		* 消费语义：至少一次、至多一次、正好一次。
4. kafka特性  
	* 高并发：支持百万级TPS。    
		* 高性能：磁盘I/O-顺序读写、【大量使用内存页】、基于Sendfile实现零拷贝。  
		* 高可用：Kafka副本机制。  
	* 分布式：  
		* 可靠性：
			1. ~~副本的一致性保证~~  
				1. Kafka副本中只有Leader可以和客户端交互，进行读写，其他副本是只能同步，不能分担读写压力。  
				2. 服务端Leader的选举：从ISR（保持同步的副本）集合副本中选取。  
			2. 可以保证消息队列不丢失  
				1. 在Producer端、Broker端、Consumer端都有可能丢失消息。  
				2. Producer端：  
					1. 为防止Producer端丢失消息， **<font color = "red">除了将ack设置为all，表明所有副本 Broker 都要接收到消息，才算“已提交”。</font>**  
					2. `还可以使用带有回调通知的发送API，即producer.send(msg, callback)`。  
				3. Broker端:  
				&emsp; ★★★Kafka`没有`提供`同步`刷盘的方式。要完全让kafka保证单个broker不丢失消息是做不到的，只能通过调整刷盘机制的参数缓解该情况。  
				&emsp; 为了解决该问题，kafka通过producer和broker协同处理单个broker丢失参数的情况。 **<font color = "red">`一旦producer发现broker消息丢失，即可自动进行retry。`</font>** 除非retry次数超过阀值（可配置），消息才会丢失。此时需要生产者客户端手动处理该情况。  
				4. ~~Consumer端：~~  
				&emsp; 采用手动提交位移。  
				5. `★★★方案二：使用补偿机制`  
				&emsp; 服务端丢失消息处理：建立消息表，发送消息前保存表记录，发送后更新表记录。  
				&emsp; 客户端丢失消息处理：服务端提供查询接口。 
			3. 幂等（重复消费）和事务。  
				1. Kafka幂等是针对生产者角度的特性。kafka只保证producer单个会话中的单个分区幂等。   
				2. Kafka幂等性实现机制：（producer_id和序列号，进行比较）  
				3. Kafka消费者的幂等性（kafka怎样保证消息仅被消费一次？）  
		* 如何让Kafka的消息有序？  
			业务上实现有序消费（⚠️★★★着重看看）  
			1. 根据不同的业务场景，以发送端或者消费端时间戳为准  


## 1.17. 网络IO  
### 1.17.1. IO模型  
1. 五种I/O模型  
	1. 网络IO的本质就是socket流的读取，通常一次IO读操作会涉及到两个对象和两个阶段。  
	2. 同步阻塞I/O  
	3. 同步 非阻塞（轮询）I/O  
	4. 多路复用I/O  
		1. 为什么有多路复用？  
		2. 多路复用能支持更多的并发连接请求。  
2. I/O多路复用详解  
	1. ~~select,poll,epoll只是I/O多路复用模型中第一阶段，即获取网络数据、用户态和内核态之间的拷贝。~~ 此阶段会阻塞线程。   
	2. select()  
	3. poll()  
	4. epoll()  
		1. epoll的三个函数   
		2. epoll机制的工作模式  
		3. 
3. 多路复用之Reactor模式  
	1. Reactor模式核心组成部分包括Reactor线程和worker线程池， 而根据Reactor的数量和线程池的数量，又将Reactor分为三种模型。  
		* Reactor负责监听（建立连接）和处理请求（分发事件、读写）。
		* 线程池负责处理事件。 
	2. 单线程模型（单Reactor单线程）  
	&emsp; Reactor单线程模型，指的是所有的IO操作都在同一个NIO线程上面完成。 单个NIO线程会成为系统瓶颈，并且会有节点故障问题。   
	3. 多线程模型（单Reactor多线程）  
	&emsp; Rector多线程模型与单线程模型最大的区别就是有一组NIO线程处理IO操作。在极个别特殊场景中，一个NIO线程（Acceptor线程）负责监听和处理所有的客户端连接可能会存在性能问题。 
	4. 主从多线程模型（多Reactor多线程）  
	&emsp; 主从Reactor多线程模型中，Reactor线程拆分为mainReactor和subReactor两个部分， mainReactor只处理连接事件，读写事件交给subReactor来处理。 业务逻辑还是由线程池来处理。
 	&emsp; mainRactor只处理连接事件，用一个线程来处理就好。处理读写事件的subReactor个数一般和CPU数量相等，一个subReactor对应一个线程。  


### 1.17.2. IO性能优化之零拷贝
1. 仅CPU方式  
2. CPU & DMA（Direct Memory Access，直接内存访问）方式  
&emsp; 最主要的变化是，CPU不再和磁盘直接交互，而是DMA和磁盘交互并且将数据从磁盘缓冲区拷贝到内核缓冲区，因此减少了2次CPU拷贝。共2次CPU拷贝，2次DMA拷贝，4次状态切换。之后的过程类似。  
3. 零拷贝技术的几个实现手段包括：mmap+write、sendfile、sendfile+DMA收集、splice等。  
4. mmap（内存映射）  
&emsp; mmap是Linux提供的一种内存映射文件的机制，它实现了将内核中读缓冲区地址与用户空间缓冲区地址进行映射，从而实现内核缓冲区与用户缓冲区的共享， 又减少了一次cpu拷贝。总共包含1次cpu拷贝，2次DMA拷贝，4次状态切换。此流程中，cpu拷贝从4次减少到1次，但状态切换还是4次。  
&emsp; mmap+write简单来说就是使用mmap替换了read+write中的read操作，减少了一次CPU的拷贝。  
5. sendfile（函数调用）  
&emsp; sendfile建立了两个文件之间的传输通道。 通过使用【sendfile数据可以直接在内核空间进行传输，】因此避免了用户空间和内核空间的拷贝，同时由于使用sendfile替代了read+write从而节省了一次系统调用，也就是2次上下文切换。   
6. sendfile+DMA收集   
7. splice方式  
&emsp; splice系统调用是Linux在2.6版本引入的，其不需要硬件支持，并且不再限定于socket上，实现两个普通文件之间的数据零拷贝。  
&emsp; splice系统调用可以在内核缓冲区和socket缓冲区之间建立管道来传输数据，避免了两者之间的CPU拷贝操作。  
&emsp; splice也有一些局限，它的两个文件描述符参数中有一个必须是管道设备。  






