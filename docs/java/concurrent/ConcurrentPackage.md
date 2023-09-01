


![image](http://182.92.69.8:8081/img/draw/JUC.png)  
<!-- 


https://mp.weixin.qq.com/mp/appmsgalbum?__biz=MzAxMjEwMzQ5MA==&action=getalbum&album_id=1348578428257353729&subscene=159&subscene=21&scenenote=https%3A%2F%2Fmp.weixin.qq.com%2Fs%3F__biz%3DMzAxMjEwMzQ5MA%3D%3D%26mid%3D2448889549%26idx%3D2%26sn%3D9aa5c65aba21960a7f8a0d5a4ea9e989%26scene%3D21%23wechat_redirect#wechat_redirect
-->

&emsp; [CAS](/docs/java/concurrent/CAS.md)  
&emsp; [AQS](/docs/java/concurrent/AQS.md)  
&emsp; &emsp; [LockSupport](/docs/java/concurrent/LockSupport.md)  
&emsp; [Lock](/docs/java/concurrent/Lock.md)  
&emsp; &emsp; [ReentrantLock使用](/docs/java/concurrent/ReentrantLockUse.md)  
&emsp; &emsp; [ReentrantLock解析](/docs/java/concurrent/ReentrantLock.md)  
&emsp; &emsp; [Condition](/docs/java/concurrent/Condition.md)  
&emsp; &emsp; [读写锁](/docs/java/concurrent/ReadWriteLock.md)  
&emsp; [Atmoic](/docs/java/concurrent/Atmoic.md)  
&emsp; &emsp; [AtomicStampedReference与AtomicMarkableReference](/docs/java/concurrent/AtomicStampedReference.md)  
&emsp; &emsp; [LongAdder](/docs/java/concurrent/LongAdder.md)  
&emsp; [Collections](/docs/java/concurrent/jihe.md)  
&emsp; &emsp; [CopyOnWriteArrayList](/docs/java/concurrent/CopyOnWriteArrayList.md)  
&emsp; &emsp; [ConcurrentHashMap，JDK1.8](/docs/java/concurrent/ConcurrentHashMap.md)  
&emsp; &emsp; [ConcurrentHashMap，JDK1.7](/docs/java/concurrent/ConcurrentHashMap7.md)  
&emsp; &emsp; [BlockingQueue](/docs/java/concurrent/BlockingQueue.md)  
&emsp; [tools](/docs/java/concurrent/tools.md)  
&emsp; &emsp; [CountDownLatch](/docs/java/concurrent/CountDownLatch.md)  
&emsp; &emsp; [CyclicBarrier](/docs/java/concurrent/CyclicBarrier.md)  
&emsp; &emsp; [Semaphore](/docs/java/concurrent/Semaphore.md)  
&emsp; &emsp; [Exchanger](/docs/java/concurrent/Exchanger.md)  


&emsp; 本章描述java.util.concurrent的API接口文档，相关方法使用的详情参考：http://www.matools.com/api/java8 。  
1. JUC基于[CAS](/docs/java/concurrent/CAS.md)和[AQS](/docs/java/concurrent/AQS.md)实现。  
2. JUC包括5部分： 
    * [locks](/docs/java/concurrent/Lock.md)：显式锁相关；  
    * [atomic](/docs/java/concurrent/Atmoic.md)：原子变量类相关；  
    * [collections](/docs/java/concurrent/Collections.md)：并发容器相关；  
    * [executor](/docs/java/concurrent/ThreadPool.md)：线程池相关；  
    * [tools](/docs/java/concurrent/tools.md)：同步工具相关，如信号量、闭锁、栅栏等功能；  

![image](http://182.92.69.8:8081/img/java/concurrent/concurrent-1.png)  


JUC类图：https://xmind.app/m/tJy5/#  
<!-- 

悲观锁： 总是假设最坏的情况， 每次去拿数据的时候都认为别人会修改， 所以每次在拿数据的时候都会上锁， 这样别人想拿这个数据就会阻塞直到它拿到锁。传统的关系型数据库里边就用到了很多这种锁机制， 比如行锁， 表锁等， 读锁， 写锁等， 都是在做操作之前先上锁。再比如 Java 里面的同步原语 synchronized 关键字的实现也是悲观锁。

乐观锁： 顾名思义， 就是很乐观， 每次去拿数据的时候都认为别人不会修改， 所以不会上锁，  但是在更新的时候会判断一下在此期间别人有没有去更新这个数据，  可以使用版本号等机制。乐观锁适用于多读的应用类型， 这样可以提高吞吐量， 像数据库提供的类似于write_condition 机制， 其实都是提供的乐观锁。在 Java 中 java.util.concurrent.atomic 包下面的原子变量类就是使用了乐观锁的一种实现方式 CAS 实现的。

乐观锁的实现方式：
1、使用版本标识来确定读到的数据与提交时的数据是否一致。提交后修改版本标   识， 不一致时可以采取丢弃和再次尝试的策略。
2、java 中的 Compare and Swap 即 CAS ， 当多个线程尝试使用 CAS 同时更新同一个变量时， 只有其中一个线程能更新变量的值， 而其它线程都失败， 失败的线程并不会被挂起， 而是被告知这次竞争中失败， 并可以再次尝试。	CAS 操 作中包含三个操作数 —— 需要读写的内存位置( V)、进行比较的预期原值( A) 和拟写入的新值(B)。如果内存位置 V 的值与预期原值 A 相匹配，那么处理器会自动将该位置值更新为新值 B。否则处理器不做任何操作。

-->