
<!-- TOC -->

- [1. 总结](#1-总结)
    - [1.1. Java](#11-java)
        - [1.1.1. Java基础](#111-java基础)
        - [1.1.2. Java基础数据类型](#112-java基础数据类型)
            - [1.1.2.1. String](#1121-string)
            - [1.1.2.2. Java基本数据类型](#1122-java基本数据类型)
        - [1.1.3. Java集合框架](#113-java集合框架)
            - [1.1.3.1. Java集合框架](#1131-java集合框架)
            - [1.1.3.2. HashMap](#1132-hashmap)
                - [1.1.3.2.1. HashMap源码](#11321-hashmap源码)
                - [1.1.3.2.2. HashMap安全](#11322-hashmap安全)
            - [1.1.3.3. Collection](#1133-collection)
        - [1.1.4. JDK1.8](#114-jdk18)
            - [1.1.4.1. 接口的默认方法与静态方法](#1141-接口的默认方法与静态方法)
            - [1.1.4.2. Lambda表达式](#1142-lambda表达式)
            - [1.1.4.3. Stream](#1143-stream)
            - [1.1.4.4. Optional](#1144-optional)
            - [1.1.4.5. DateTime](#1145-datetime)
        - [1.1.5. Java异常](#115-java异常)
        - [1.1.6. Java范型](#116-java范型)
        - [1.1.7. 自定义注解](#117-自定义注解)
        - [1.1.8. 反射](#118-反射)
        - [1.1.9. IO](#119-io)
        - [1.1.10. SPI与线程上下文类加载器](#1110-spi与线程上下文类加载器)
    - [1.2. 设计模式](#12-设计模式)
        - [1.2.1. 七大设计原则](#121-七大设计原则)
        - [1.2.2. 继承和组合/复用规则](#122-继承和组合复用规则)
        - [1.2.3. 设计模式详解](#123-设计模式详解)
            - [1.2.3.1. 5种创建型设计模式](#1231-5种创建型设计模式)
            - [1.2.3.2. 7种结构型设计模式](#1232-7种结构型设计模式)
            - [1.2.3.3. 两种动态代理](#1233-两种动态代理)
            - [1.2.3.4. 11种行为型设计模式](#1234-11种行为型设计模式)
            - [1.2.3.5. 设计模式大讨论](#1235-设计模式大讨论)
                - [1.2.3.5.1. 结构型模式的讨论](#12351-结构型模式的讨论)
                - [1.2.3.5.2. 行为型模式的讨论](#12352-行为型模式的讨论)
            - [1.2.3.6. 设计模式混编](#1236-设计模式混编)
            - [1.2.3.7. 常使用的设计模式](#1237-常使用的设计模式)
    - [1.3. JVM](#13-jvm)
        - [1.3.1. JDK、JRE、JVM](#131-jdkjrejvm)
        - [1.3.2. 编译成Class字节码文件](#132-编译成class字节码文件)
        - [1.3.3. 类加载](#133-类加载)
            - [1.3.3.1. JVM类的加载](#1331-jvm类的加载)
            - [1.3.3.2. JVM类加载器](#1332-jvm类加载器)
        - [1.3.4. 运行时数据区/内存结构](#134-运行时数据区内存结构)
            - [1.3.4.1. JVM内存结构](#1341-jvm内存结构)
                - [1.3.4.1.1. JVM内存结构](#13411-jvm内存结构)
                - [1.3.4.1.2. 常量池详解](#13412-常量池详解)
            - [1.3.4.2. 内存(堆栈)中的对象](#1342-内存堆栈中的对象)
                - [1.3.4.2.1. 创建对象](#13421-创建对象)
                - [1.3.4.2.2. 对象生命周期](#13422-对象生命周期)
                - [1.3.4.2.3. 对象大小](#13423-对象大小)
            - [1.3.4.3. 内存泄露](#1343-内存泄露)
        - [1.3.5. JVM执行](#135-jvm执行)
        - [1.3.6. GC](#136-gc)
            - [1.3.6.1. GC-回收位置/安全点](#1361-gc-回收位置安全点)
            - [1.3.6.2. 回收算法与分代回收](#1362-回收算法与分代回收)
            - [1.3.6.3. GC-回收对象](#1363-gc-回收对象)
                - [1.3.6.3.1. 堆中对象的存活](#13631-堆中对象的存活)
                - [1.3.6.3.2. 方法区(类和常量)回收/类的卸载阶段](#13632-方法区类和常量回收类的卸载阶段)
                - [1.3.6.3.3. null与GC](#13633-null与gc)
            - [1.3.6.4. GC-垃圾回收器](#1364-gc-垃圾回收器)
                - [1.3.6.4.1. 垃圾回收器](#13641-垃圾回收器)
                - [1.3.6.4.2. CMS回收器](#13642-cms回收器)
                - [1.3.6.4.3. G1回收器](#13643-g1回收器)
                - [1.3.6.4.4. 三色标记，并发标记阶段](#13644-三色标记并发标记阶段)
        - [1.3.7. JVM调优](#137-jvm调优)
            - [1.3.7.1. JVM调优-基础](#1371-jvm调优-基础)
            - [1.3.7.2. JVM调优](#1372-jvm调优)
            - [1.3.7.3. JVM问题排查](#1373-jvm问题排查)
            - [1.3.7.4. Arthas工具](#1374-arthas工具)
    - [1.4. 多线程和并发](#14-多线程和并发)
        - [1.4.1. 线程Thread](#141-线程thread)
            - [1.4.1.1. 线程状态详解](#1411-线程状态详解)
        - [1.4.2. 线程池-多线程](#142-线程池-多线程)
            - [1.4.2.1. 线程池框架](#1421-线程池框架)
            - [1.4.2.2. ThreadPoolExecutor详解](#1422-threadpoolexecutor详解)
            - [1.4.2.3. 线程池的正确使用](#1423-线程池的正确使用)
            - [1.4.2.4. ForkJoinPool详解](#1424-forkjoinpool详解)
            - [1.4.2.5. Future相关](#1425-future相关)
            - [1.4.2.6. ~~CompletionService~~](#1426-completionservice)
            - [1.4.2.7. ~~CompletableFuture~~](#1427-completablefuture)
        - [1.4.3. 并发编程](#143-并发编程)
            - [1.4.3.1. 并发编程原理](#1431-并发编程原理)
                - [1.4.3.1.1. ~~CPU缓存及JMM~~](#14311-cpu缓存及jmm)
                - [1.4.3.1.2. 并发安全问题产生原因](#14312-并发安全问题产生原因)
                - [1.4.3.1.3. 并发安全解决底层](#14313-并发安全解决底层)
                - [1.4.3.1.4. 伪共享问题](#14314-伪共享问题)
            - [1.4.3.2. 线程安全解决](#1432-线程安全解决)
                - [1.4.3.2.1. 线程安全解决方案](#14321-线程安全解决方案)
                - [1.4.3.2.2. Synchronized](#14322-synchronized)
                    - [1.4.3.2.2.1. Synchronized介绍](#143221-synchronized介绍)
                    - [1.4.3.2.2.2. Synchronized使用](#143222-synchronized使用)
                - [1.4.3.2.3. Synchronized使用是否安全](#14323-synchronized使用是否安全)
                    - [1.4.3.2.3.1. Synchronized底层原理](#143231-synchronized底层原理)
                    - [1.4.3.2.3.2. Synchronized优化](#143232-synchronized优化)
                - [1.4.3.2.4. Volatile](#14324-volatile)
                - [1.4.3.2.5. ThreadLocal](#14325-threadlocal)
                    - [1.4.3.2.5.1. ThreadLocal原理](#143251-threadlocal原理)
                    - [1.4.3.2.5.2. ThreadLocal应用](#143252-threadlocal应用)
            - [1.4.3.3. 线程通信(生产者消费者问题)](#1433-线程通信生产者消费者问题)
            - [1.4.3.4. 线程活跃性](#1434-线程活跃性)
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
        - [1.5.2. MySql优化](#152-mysql优化)
            - [1.5.2.1. SQL分析](#1521-sql分析)
                - [1.5.2.1.1. Expain](#15211-expain)
            - [1.5.2.2. SQL优化](#1522-sql优化)
            - [1.5.2.3. 索引优化](#1523-索引优化)
            - [1.5.2.4. 碎片优化](#1524-碎片优化)
        - [1.5.3. 数据库分布式](#153-数据库分布式)
            - [1.5.3.1. 大数据量操作](#1531-大数据量操作)
            - [1.5.3.2. MySql瓶颈](#1532-mysql瓶颈)
            - [1.5.3.3. 数据库分布式](#1533-数据库分布式)
            - [1.5.3.4. 主从复制](#1534-主从复制)
                - [1.5.3.4.1. 主从复制原理](#15341-主从复制原理)
                - [1.5.3.4.2. 主从复制实现](#15342-主从复制实现)
                - [1.5.3.4.3. 主从复制问题](#15343-主从复制问题)
                - [1.5.3.4.4. 高可用实现](#15344-高可用实现)
                - [1.5.3.4.5. 读写分离实现](#15345-读写分离实现)
            - [1.5.3.5. 分区](#1535-分区)
            - [1.5.3.6. 分库分表](#1536-分库分表)
                - [1.5.3.6.1. 分库分表](#15361-分库分表)
                - [1.5.3.6.2. 分库分表查询](#15362-分库分表查询)
                - [1.5.3.6.3. 跨分片的排序分页](#15363-跨分片的排序分页)
            - [1.5.3.7. 数据迁移](#1537-数据迁移)
        - [1.5.4. 索引事物锁](#154-索引事物锁)
            - [1.5.4.1. 索引底层原理](#1541-索引底层原理)
            - [1.5.4.2. ~~各种索引~~（还需要总结）](#1542-各种索引还需要总结)
            - [1.5.4.3. MySql事务（还需要总结）](#1543-mysql事务还需要总结)
            - [1.5.4.4. MVCC](#1544-mvcc)
            - [1.5.4.5. MySql锁](#1545-mysql锁)
            - [1.5.4.6. MySql死锁和锁表](#1546-mysql死锁和锁表)
        - [1.5.5. MySql架构](#155-mysql架构)
            - [1.5.5.1. MySql运行流程](#1551-mysql运行流程)
            - [1.5.5.2. Server层之binLog日志](#1552-server层之binlog日志)
            - [1.5.5.3. 存储引擎层](#1553-存储引擎层)
            - [1.5.5.4. InnoDB体系结构](#1554-innodb体系结构)
                - [1.5.5.4.1. InnoDB内存结构-性能](#15541-innodb内存结构-性能)
                    - [1.5.5.4.1.1. BufferPool](#155411-bufferpool)
                    - [1.5.5.4.1.2. 写缓冲ChangeBuffer](#155412-写缓冲changebuffer)
                    - [1.5.5.4.1.3. AdaptiveHashIndex](#155413-adaptivehashindex)
                - [1.5.5.4.2. InnoDB磁盘结构-可靠性](#15542-innodb磁盘结构-可靠性)
                    - [1.5.5.4.2.1. BufferPool落盘表空间](#155421-bufferpool落盘表空间)
                    - [1.5.5.4.2.2. undoLog](#155422-undolog)
                    - [1.5.5.4.2.3. redoLog](#155423-redolog)
                    - [1.5.5.4.2.4. DoubleWrite](#155424-doublewrite)
                - [1.5.5.4.3. ~~两阶段提交和崩溃恢复~~](#15543-两阶段提交和崩溃恢复)

<!-- /TOC -->


# 1. 总结  
## 1.1. Java
### 1.1.1. Java基础
1. static关键字：  
    1. 方便在没有创建对象的情况下来进行调用（方法/变量）。  
    2. static使用： 1). static修饰变量、 2). 修饰方法、 3). static 可以修饰代码块，主要分为两种，一种直接定义在类中，使用static{}，这种被称为静态代码块，一种是在类中定义静态内部类，使用static class xxx来进行定义、 4). static可以和单例模式一起使用，通过双重检查锁来实现线程安全的单例模式、 5).静态导包。     
    3. 静态方法可以调用成员变量吗？ `注⚠️：static静态方法引用类变量，变量需要static修饰。`  
    4. static与JVM： 
        1. 类的加载流程中，准备阶段、初始化阶段。  
        2. static作为类变量，只被加载一次。  
        3. static变量被存放在方法区中。    

### 1.1.2. Java基础数据类型
#### 1.1.2.1. String
1. String 类是用final关键字修饰的，所以认为其是不可变对象。反射可以改变String对象。  
&emsp; **<font color = "clime">为什么Java字符串是不可变的？</font>** 原因大致有以下三个：  
    * 为了实现字符串常量池。字符串常量池可以节省大量的内存空间。  
    * 为了线程安全。  
    * 为了 HashCode 的不可变性。String类经常被用作HashMap的key。  
2. String创建了几个对象？  
&emsp; `String str1 = "java";`创建一个对象放在常量池中。  
&emsp; `String str2 = new String("java");`创建两个对象，`字面量"java"创建一个对象放在常量池中`，new String()又创建一个对象放在堆中。如果常量池中已经存在，则是创建了一个对象。  
&emsp; `String str3 = "hello "+"java";`创建了一个对象。  
&emsp; `String str5 = str3 + "java";`创建了三个对象。
3. String不可变，安全；StringBuilder可变，线程不安全；StringBuffer可变，线程安全。  

#### 1.1.2.2. Java基本数据类型

|数据类型|字节|位数|默认值|取值范围|
|---|---|---|---|---|
|byte	|1	|8|0	|-128-127|
|short	|2	|16|0	|-32768-32767|
|int	|4	|32|0	|-2147483648-2147483647|
|long	|8	|64|0| |	
|float	|4	|32|0.0f| |	
|double	|8	|64|0.0d| |	
|char	|2	|16|'\u0000'| |	
|boolean	|4|32	|false	| |

&emsp; char的包装类型是Character。  

&emsp; java对象大小查看【JVM内存】章节。  

### 1.1.3. Java集合框架
#### 1.1.3.1. Java集合框架
1. `基本数据结构：数组、链表、Hash、树。`集合框架又有是否安全之分。  
2. Java集合框架：  
    * List：有序，可重复。List有ArrayList、LinkedList、Vector。
    * Set：无序，不可重复(唯一)。Set有HashSet、LinkedHashSet、TreeSet。
    * Map：存储键值对。Map有HashMap、LinkedHashMap、TreeMap、Hashtable。     
3. 快速失败机制：单线程迭代器中直接删除元素或多线程使用非安全的容器都会抛出ConcurrentModificationException异常。  
&emsp; **<font color = "clime">采用安全失败(fail-safe)机制的集合容器，在遍历时不是直接在集合内容上访问的，而是先复制原有集合内容，再在拷贝的集合上进行遍历。</font>**  
4. 排序：  
    * Comparable，自然排序（自身属性，整数(大小排序)，字符串(字典序)）。  
    * Comparator，定制排序。  

#### 1.1.3.2. HashMap
##### 1.1.3.2.1. HashMap源码
1. HashMap数据结构：  
    1. Hash表数据结构：  
    &emsp; 初始容量为16；  
    &emsp; HashMap在发生hash冲突的时候用的是链地址法。JDK1.7中使用头插法，JDK1.8使用尾插法。  
    &emsp; loadFactor加载因子0.75f；  
    2. 树形化结构：  
    &emsp; 树形化：把链表转换成红黑树，树化需要满足以下两个条件：链表长度大于等于8；table数组长度大于等于64。  
    &emsp; 解除树形化：阈值6。
2. HashMap成员方法：  
    1. hash()函数/扰动函数：  
    &emsp; hash函数会根据传递的key值进行计算， 1)首先计算key的hashCode值， 2)然后再对hashcode进行无符号右移操作， 3)最后再和hashCode进行异或 ^ 操作。（即让hashcode的高16位和低16位进行异或操作。）   
    &emsp; **<font color = "clime">这样做的好处是增加了随机性，减少了碰撞冲突的可能性。</font>**    
    2. put()函数：  
        1. 在put的时候，首先对key做hash运算，计算出该key所在的index。
        2. 如果没碰撞，直接放到数组中；
        3. 如果碰撞了，如果key是相同的，则替换掉原来的值；
        4. 如果key不同，需要判断目前数据结构是链表还是红黑树，根据不同的情况来进行插入。
        5. 最后判断哈希表是否满了(当前哈希表大小*负载因子)，如果满了，则扩容。  
    2. 扩容机制：
        1. 扩容时机：JDK 1.8扩容条件是数组长度大于阈值或链表转为红黑树且数组元素小于64时。  
        2. JDK1.8扩容流程：  
            1. 新建数组，扩容为原数组两倍。  
            2. 循环原table，把原table中的每个链表中的每个元素放入新table。 
                * 首先计算hash值：index = HashCode（Key） & （Length - 1）    
                * 单节点迁移。  
                * 如果节点是红黑树类型的话则需要进行红黑树的拆分：`拆分成高低位链表，如果链表长度大于6，需要把链表升级成红黑树。`
                * 对链表进行迁移。会对链表中的节点进行分组，进行迁移后，一类的节点位置在原索引，一类在原索引+旧数组长度。 ~~通过 hash & oldCap(原数组大小)的值来判断，若为0则索引位置不变，不为0则新索引=原索引+旧数组长度~~  
            3. 返回新数组。  

##### 1.1.3.2.2. HashMap安全
&emsp; HashMap的线程不安全体现在会造成死循环、数据丢失、数据覆盖这些问题。其中死循环和数据丢失是在JDK1.7中出现的问题，在JDK1.8中已经得到解决，然而1.8中仍会有数据覆盖这样的问题。  
1. 在jdk1.8中，在多线程环境下，会发生数据覆盖的情况。  
&emsp; 假设两个线程A、B都在进行put操作，并且hash函数计算出的插入下标是相同的，当线程A执行完第六行代码后由于时间片耗尽导致被挂起，而线程B得到时间片后在该下标处插入了元素，完成了正常的插入，`然后线程A获得时间片，由于之前已经进行了hash碰撞的判断，所以此时不会再进行判断，而是直接进行插入，这就导致了线程B插入的数据被线程A覆盖了，从而线程不安全。`  
2. `HashMap导致CPU100% 的原因是因为 HashMap 死循环导致的。`  
&emsp; 导致死循环的根本原因是JDK 1.7扩容采用的是“头插法”，会导致同一索引位置的节点在扩容后顺序反掉。而JDK 1.8之后采用的是“尾插法”，扩容后节点顺序不会反掉，不存在死循环问题。  
&emsp; 导致死循环示例：线程1、2同时扩容。线程1指向节点和下一节点，线程挂起。线程2完成扩容，此时线程1唤醒。线程1继续完成头节点插入，形成闭环。   
&emsp; 发生死循环后，剩余元素无法搬运，并且线程不会停止，因此会造成CPU100%。  
3. 线程安全的hashMap：Hashtable、Collections.synchronizedMap、[ConcurrentHashMap](/docs/java/concurrent/ConcurrentHashMap.md)。 

#### 1.1.3.3. Collection
1. HashSet基于HashMap实现： **<font color = "clime">存储在HashSet中的数据作为Map的key，而Map的value统一为PRESENT。</font>**  
    &emsp; 添加元素，如何保证值不重复？  
    &emsp; HashSet#add通过 map.put() 方法来添加元素。HashSet的add(E e)方法，会将e作为key，PRESENT作为value插入到map集合中。  
    * 如果e(新插入的key)存在，HashMap#put返回原key对应的value值（注意新插入的value会覆盖原value值），Hashset#add返回false，表示插入值重复，插入失败。  
    * 如果e(新插入的key)不存在，HashMap#put返回null值，Hashset#add返回true，表示插入值不重复，插入成功。  

### 1.1.4. JDK1.8
#### 1.1.4.1. 接口的默认方法与静态方法
1. 接口的默认方法与静态方法  
    * <font color = "clime">接口中的default方法会被子接口继承，也可以被其实现类所调用。default方法被继承时，可以被子接口覆写。</font>  
    * <font color = "clime">接口中的`static方法`不能被继承，也不能被实现类调用，`只能被自身调用`。即不能通过接口实现类的方法调用静态方法，直接通过接口名称调用。但是静态变量会被继承。</font>  

#### 1.1.4.2. Lambda表达式
1. **<font color = "clime">函数式接口的实例创建三种方式：lambda表达式；方法引用；构造方法引用。</font>**   
2. Lambda表达式作用域，访问外层作用域定义的局部变量、类的属性：  
    * <font color = "clime">访问局部变量：lambda表达式若访问了局部变量，则局部变量必须是final的。若局部变量没有加final关键字，系统会自动添加，此后再修改该局部变量，会编译错误。</font>  
    * <font color = "clime">访问类的属性：lambda内部使用this关键字（或不使用）访问或修改全局变量、实例方法。</font>    

#### 1.1.4.3. Stream
&emsp; **<font color = "clime">使用并行流parallelStream()有线程安全问题。例如：parallelStream().forEach()内部修改集合会有问题。解决方案：1.使用锁； 2.使用collect和reduce操作(Collections框架提供了同步的包装)。</font>**  

#### 1.1.4.4. Optional
&emsp; 使用Optional时尽量不直接调用Optional.get()方法，Optional.isPresent()更应该被视为一个私有方法，应依赖于其他像Optional.orElse()，Optional.orElseGet()，Optional.map()等这样的方法。  

&emsp; 抛出异常可以使用：  

```java
//todo Optional.ofNullable(storeInfo) 创建对象
Optional.ofNullable(storeInfo).orElseThrow(()->new Exception("失败"));  
```

#### 1.1.4.5. DateTime


### 1.1.5. Java异常
1. throws和throw：  
    * throws用在`函数上`，后面跟的是`异常类`，可以跟多个；
    * throw用在`函数内`，后面跟的是`异常对象`。  
2. 异常捕获后再次抛出。
    * 捕获后抛出原来的异常，希望保留最新的异常抛出点。 
    * 捕获后抛出新的异常，希望抛出完整的异常链。  
3. 自定义异常
4. 统一异常处理

### 1.1.6. Java范型
1. 范型：编译时，范型类型检查、范型擦除。  
    1. 范型擦除：Java会在编译Class文件时，将范型擦除成原始类型Object。  
    2. 运行时，如何获取范型信息？  
2. 利用反射越过泛型检查  
&emsp; 反射是获取类Class文件进行操作。通过反射获取对象后可以获得相应的add方法，并向方法里面传入任何对象。  


### 1.1.7. 自定义注解


### 1.1.8. 反射
1. 什么是反射？  
	&emsp; 反射是在`运行状态`能够动态的获取该类的属性和方法，并且能够任意的使用该类的属性和方法，这种动态获取类信息以及动态的调用对象的方法的功能就是反射。  
2. 反射的适用场景？  
	1. 情景一，不得已而为之
	2. 情景二，动态加载（可以最大限度的体现Java的灵活性，并降低类的耦合性：多态）
	3. 情景三：避免将程序写死到代码里  
	4. 开发通用框架 - 反射最重要的用途就是开发各种通用框架。很多框架（比如 Spring）都是配置化的（比如通过 XML 文件配置 JavaBean、Filter 等），为了保证框架的通用性，它们可能需要根据配置文件加载不同的对象或类，调用不同的方法，这个时候就必须用到反射——运行时动态加载需要加载的对象。  
    
	&emsp; **<font color = "clime">平常开发涉及的框架中使用反射的有：动态代理、JDBC中的加载数据库驱动程序、Spring框架中加载bean对象。</font>**  
3. 反射的优缺点？  
	* 优点：  
		1）能够运行时动态获取类的实例，提高灵活性；
		2）与动态编译结合
	* 缺点：  
		1）使用反射性能较低，需要解析字节码，将内存中的对象进行解析。
			解决方案：
			1、通过setAccessible(true)关闭JDK的安全检查来提升反射速度；
			2、多次创建一个类的实例时，有缓存会快很多
			3、ReflflectASM工具类，通过字节码生成的方式加快反射速度
			4、尽量不要getMethods()后再遍历筛选，而直接用getMethod(methodName)来根据方法名获取方法。
		2）`相对不安全，破坏了封装性`（因为通过反射可以获得私有方法和属性）  
4. 反射的原理：  
	&emsp; 调用反射的总体流程如下：  
	* 准备阶段：编译期装载所有的类，将每个类的元信息保存至Class类对象中，每一个类对应一个Class对象。  
	* 获取Class对象：调用x.class/x.getClass()/Class.forName() 获取x的Class对象clz（这些方法的底层都是native方法，是在JVM底层编写好的，涉及到了JVM底层，就先不进行探究了）。  
	* 进行实际反射操作：通过clz对象获取Field/Method/Constructor对象进行进一步操作。  
5. 自定义注解 + 反射 实际应用。  

### 1.1.9. IO
1. **<font color = "clime">将大文件数据全部读取到内存中，可能会发生OOM异常。</font>** I/O读写大文件解决方案：  
    * 使用BufferedInputStream进行包装。
    * 逐行读取。
    * 并发读取：1)逐行批次打包；2)大文件拆分成小文件。
    * 零拷贝方案：
        * FileChannel，分配读取到已分配固定长度的 java.nio.ByteBuffer。
        * 内存文件映射，MappedByteBuffer。采用内存文件映射不能读取超过2GB的文件。文件超过2GB，会报异常。


### 1.1.10. SPI与线程上下文类加载器
&emsp; SPI，service provider interface，服务提供者接口，一种扩展机制。`相比面向接口的多态，实现动态编译。面向接口的多态，加载的实体类是在编码中，而SPI是写在配置文件中。`    
&emsp; **<font color = "clime">JDK提供的SPI机制：</font>**  
1. 提供一个接口；  
2. 服务提供方实现接口，并在META-INF/services/中暴露实现类地址；  
3. 服务调用方依赖接口，使用java.util.ServiceLoader类调用。  

&emsp; 这个是针对厂商或者插件的，第三方提供服务的功能。  

--------

&emsp; JDK的SPI内部使用线程上下文类加载器实现，破坏了双亲委派模型，是为了适用所有场景。ServiceLoader中的load方法：  

```java
public static <S> ServiceLoader<S> load(Class<S> service) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return ServiceLoader.load(service, cl);
}
```

&emsp; `Dubbo的SPI并没有破坏双亲委派模型。`自己实现的框架，接口类和实现类一般都是由SystemClassLoader加载器来加载的，这时候双亲委派模型仍然可以正常使用。很多框架使用SPI方式的原因，不是因为双亲委派模型满足不了类加载需求，而是看重了SPI的易扩展性。  


-----

&emsp; 个人的简单理解：  
&emsp; SPI（Service Provider Interface），服务提供发现接口。热插拔、动态替换。  
&emsp; `SPI与多态比较：`  
&emsp; 多态，一个接口在一个包中有多个实现；  
&emsp; 而SPI提供的接口的实现一般在多个包中，例如JDBC的实现mysql、oracle，web容器有tomcat、jetty等。  
&emsp; 一个接口在b、c包中有实现，在a包中可替换所依赖的包（b或c），动态实现某一个功能。  


## 1.2. 设计模式
### 1.2.1. 七大设计原则
* 针对单个类的设计原则：  
    * 单一职责原则。   
    * 开闭原则（ **<font color = "clime">对已经使用的类的改动是通过增加代码进行的，而不是修改现有代码，实现一个热插拔的效果</font>** ）；
* 要依赖抽象或接口：  
    * 依赖倒置原则（ **<font color = "clime">为了实现这一原则，就要求在编程的时候针对抽象类或者接口编程，而不是针对具体实现编程</font>** ）； 
    * 接口隔离原则；     
    * 里氏替换原则（ **<font color = "clime">子类可以扩展父类的功能，但不能改变父类原有的功能</font>** ）。  
* 类与类：  
    * 合成复用原则（ **<font color = "red">尽量使用对象[组合(has-a)/聚合(contanis-a)](/docs/java/Design/compose.md)，而不是继承关系达到软件复用的目的</font>** ）；
    * 迪米特法则（一个对象应当对其他对象尽可能少的了解）。  

### 1.2.2. 继承和组合/复用规则
1. 类和类之间的关系有三种：is-a（继承或泛化）、has-a（关联或聚合）和use-a（依赖）。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/design/design-27.png)  
2. 组合的含义更像是一个对象(类)由各方面构成，这些方面并非来自于继承，但有时候却是必不可少的。`如果说继承是垂直结构，那么组合是横向结构。`  
3. `对于委托，类与类之间或对象与对象之间可以没有任何逻辑上的关系(比如继承关系和组合关系)，仅仅只是委托方和被委托方的关系。`不过，继承而来的方法本就会自动查找，所以这些方法不需要委托。`而组合经常会结合委托一起使用，或者说组合的过程中本就依赖于委托，`比如对于房子.煮饭()这个方法调用请求，应该委托或转发给厨房.煮饭()。  
&emsp; ~~委托是将一部分功能分割出去完成，即委托者（delegator）将自己委托给受托者（delegatee），`受托者方法中参数为委托者对象`；然后委托者调用受托者类对象。~~  

### 1.2.3. 设计模式详解
1. 常用设计模式有23种（不包含简单工厂模式）。 **<font color = "red">这23种设计模式的本质是面向对象设计原则的实际运用，是对类的封装性、继承性和多态性，以及类的关联关系和组合关系的充分理解。</font>**  
2.  **<font color = "red">结构型设计模式：多个类协同完成一个功能； 行为型设计模式，算法模式。</font>**  

#### 1.2.3.1. 5种创建型设计模式
1. 单例模式
    1. `单例模式与static静态类`：静态使用于一些非状态Bean，单例模式使用于状态Bean。  
    2. ~~单例模式适用场景~~：全局只有一个示例，例如：数据库连接池、spring单例bean...  
    3. 编码
    ★★★双重校验锁的形式：DCL详解参考[Volatile](/docs/java/concurrent/Volatile.md)。`注⚠️：static、volicate、双重校验+锁synchronized。`  

    ```java
    public class LazyDoubleCheckSingleton {
        
        private static volatile LazyDoubleCheckSingleton lazy = null;

        private LazyDoubleCheckSingleton(){
            
        }
        
        public static LazyDoubleCheckSingleton getInstance(){
            // 第一重检测
            if(lazy == null){
                // 锁定代码块
                synchronized (LazyDoubleCheckSingleton.class){
                    // 第二重检测
                    if(lazy == null){
                        // 实例化对象
                        lazy = new LazyDoubleCheckSingleton();
                        //1.分配内存给这个对象
                        //2.初始化对象
                        //3.设置 lazy 指向刚分配的内存地址
                    }
                }
            } 
            // todo 返回实例
            return lazy;
        }
        
        /**
        * 逻辑操作
        **/
        public void showMessage(){
            System.out.println("Hello World!");
        }
        
    }
    ```

    ```java
    public static void main(String[] args){
        // 
        LazyDoubleCheckSingleton instance = LazyDoubleCheckSingleton.getInstance();
        //
        instance.methodOne();
    }
    ```
    &emsp; <font color = "red">只有在singleton == null的情况下再进行加锁创建对象，如果singleton!=null，就直接返回就行了，并没有进行并发控制。大大的提升了效率。</font>   
    &emsp; <font color = "clime">从上面的代码中可以看到，其实整个过程中进行了两次singleton == null的判断，所以这种方法被称之为"双重校验锁"。</font>   
    &emsp; <font color = "clime">还有值得注意的是，双重校验锁的实现方式中，静态成员变量singleton必须通过volatile来修饰，保证其初始化不被重排，否则可能被引用到一个未初始化完成的对象。</font>   
2. 简单工厂模式和抽象工厂模式
3. 建造者模式： **<font color = "red">建造者模式适用于创建对象需要很多步骤，但是步骤的顺序不一定固定。如果一个对象有非常复杂的内部结构(很多属性)，可以将复杂对象的创建和使用进行分离。</font>**  
4. 原型模式：
    克隆的结果有2种，一种是浅复制，另一种是深复制。  
    * 浅复制: 对值类型的成员变量进行值的复制，对引用类型的成员变量只复制引用，不复制引用的对象。  
    * 深复制:  **<font color = "clime">对值类型的成员变量进行值的复制，对引用类型的成员变量也进行引用对象的复制。</font>**  

#### 1.2.3.2. 7种结构型设计模式
1. 外观模式/门面模式：**提供了一个统一的接口，用来访问子系统中的一群接口。**  
2. 适配器模式
    1. 平时开发中，面向接口编程，注入其他类，从而进行调用。  
    &emsp; 适配器(Adapter)模式中，Adapter，适配器类，即实现目标接口Target，又继承Adaptee类。适配器模式的核心角色，其他两个角色都是已经存在的角色，而适配器角色是需要新建立的，它的职责非常简单：把源角色转换为目标角色。`转换的方式有：即能通过继承，又能通过类关联的方式。`  
    2. **适配器模式有3种形式：对象适配器、类适配器、接口适配器。**  
        * `对象适配器（平时使用最多）`：不使用多继承或继承的方式，而是使用直接关联，或者称为委托的方式。  
        * 类适配器：Adapter类继承Adaptee（被适配类），同时实现Target接口（因为Java不支持多继承，所以只能通过接口的方法来实现多继承），在Client类中可以根据需要选择并创建任一种符合需求的子类，来实现具体功能。 
        * 接口适配器：通过抽象类来实现适配。即适配器类是一个抽象类。  
    3. 适配的思想。  
3. 代理模式：<font color = "red">提供了对目标对象另外的访问方式，即通过代理访问目标对象。</font>  
4. 装饰器模式：`可替代继承(有继承就可以改写装饰器模式)`，此设计模式重点在于对已有的功能进行扩展。实际开发中， **<font color = "red">大多数用于对老项目的某些功能进行扩展。</font>** 新项目中一般不怎么用此模式。`装饰器模式解决继承的【臃肿】`。      
&emsp; Decorator，装饰角色，一般是一个抽象类，继承自或实现Component（抽象构件），在它的属性里面有一个变量指向Component抽象构件，这是装饰器最关键的地方。  
&emsp; 在Mybatis中，Cache的实现类LruCache、FifoCache等都是装饰一个类PerpetualCache。常见代码格式，就是装饰类中会有个被装饰类的属性，并且这个属性还是构造方法的参数。  
5. 桥接模式（if/else）
6. 组合模式
7. 享元模式（池化技术）  
    &emsp; 享元模式：①将对象的公共部分抽取出来成为内部状态(实现共享)，②将随时间改变、不可共享的部分作为外部状态(通过更换外部状态实现对象复用)，从而减少创建对象的数量，以减少内存开销和提高性能。  
    &emsp; 核心：共享和复用，共享(内部状态 - intrinsicState)，复用(外部状态 - extrinsicState)  
    &emsp; 使用场景：  

    * 共享：当只存在内部状态时，可以在多线程中共享使用(String常量池)  
    * 复用：通过改变外部状态，可以更好实现对象的复用(线程池)  

    &emsp; PS：外部状态在线程间需考虑并发问题，因此不适合共享，但当对象被使用完成后，通过修改外部状态，使其可以复用于下一次的访问需求。  


#### 1.2.3.3. 两种动态代理
1. 动态编程  
&emsp; 动态编程是相对于静态编程而言，平时我们大多讨论的都是静态编程，java便是一种静态编程语言，它 的类型检查是在编译期间完成的。而动态编程是绕过了编译期间，在运行时完成类型检查。java有如下方法实现动态编程：动态代理，动态编译  
3. JDK动态代理：  
    0. 两大知识点：反射、因单继承只能为接口生成代理对象。  
    1. Java动态代理类位于java.lang.reflect包下，一般主要涉及到以下两个重要的类或接口，`一个是InvocationHandler接口、另一个则是Proxy类。`  
        * Proxy类。该类即为动态代理类。Proxy.newProxyInstance()生成代理对象；  
        * InvocationHandler接口。 **<font color = "clime">在使用动态代理时，需要定义一个位于代理类与委托类之间的中介类，中介类被要求实现InvocationHandler接口。</font>** 通过代理对象调用一个方法的时候，这个方法的调用会被转发为由InvocationHandler这个接口的invoke方法来进行调用。  
    2. <font color = "clime">JDK动态代理的实现，大致流程：</font>使用`反射`来创建代理类。  
        1. <font color = "red">为接口创建代理类的字节码文件。</font>   
        2. <font color = "red">使用ClassLoader将字节码文件加载到JVM。</font>  
        3. <font color = "red">创建代理类实例对象，执行对象的目标方法。</font>  
    3. `限制：JDK动态代理为什么只能使用接口？`  
    &emsp; JDK动态代理是为接口生成代理对象，该代理对象继承了JAVA标准类库Proxy.java类并且实现了目标对象。由于JAVA遵循`单继承`多实现原则，所以JDK无法利用继承来为目标对象生产代理对象。   
2. 动态编译  
    &emsp; 动态编译就是利用字节码修改技术，来操作java字节码在运行期间jvm中动态生成新类或者对已有类进行修改。动态编译时在java 6开始支持的，主要是通过一个JavaCompiler接口来完成的。可以解决需要动态插入代码的场景，比如动态代理的实现，实现AOP编程。  
    &emsp; 操作java字节码的工具有两个比较流行，一个是ASM，一个是Javassit。  

    * ASM：直接操作字节码指令，执行效率高，要是使用者掌握Java类字节码文件格式及指令，对使用者的要求比较高。
    * Javassit 提供了更高级的API，执行效率相对较差，但无需掌握字节码指令的知识，对使用者要求较低。

    &emsp; 应用层面来讲一般使用建议优先选择Javassit，如果后续发现Javassit 成为了整个应用的效率瓶颈的话可以再考虑ASM.当然如果开发的是一个基础类库，或者基础平台，还是直接使用ASM吧，相信从事这方面工作的开发者能力应该比较高。
4. CGLIB代理
    1. 依赖ASM字节码工具，通过动态生成`实现接口或继承类`的类字节码，实现动态代理。  
    &emsp; `针对接口，生成实现接口的类，即implements方式；针对类，生成继承父类的类，即extends方式。`  
    2. **<font color = "clime">CGLIB基于继承类生成动态代理需要注意：</font>**  
        1. final声明的类是不能被代理的；
        2. `类中的private,final方法不能被代理，static方法不生成代理方法。`

#### 1.2.3.4. 11种行为型设计模式
1. 模板方法模式
2. 策略模式(if/else)
3. 责任链模式(if/else)
4. 观察者模式

#### 1.2.3.5. 设计模式大讨论

##### 1.2.3.5.1. 结构型模式的讨论
&emsp; 代理模式VS装饰模式  
&emsp; 对于两个模式，首先要说的是，装饰模式就是代理模式的一个特殊应用，两者的共同点是都具有相同的接口， **<font color = "red">不同点则是代理模式着重对代理过程的控制，而装饰模式则是对类的功能进行加强或减弱，它着重类的功能变化。</font>**   


&emsp; 装饰模式VS适配器模式  
&emsp; 装饰模式和适配器模式在通用类图上没有太多的相似点，差别比较大，但是它们的功能有相似的地方：都是包装作用，都是通过委托方式实现其功能。不同点是：<font color = "clime">装饰模式包装的是自己的兄弟类，隶属于同一个家族(相同接口或父类)，</font><font color = "red">适配器模式则修饰非血缘关系类，把一个非本家族的对象伪装成本家族的对象，注意是伪装，因此它的本质还是非相同接口的对象。</font>   

##### 1.2.3.5.2. 行为型模式的讨论


#### 1.2.3.6. 设计模式混编


#### 1.2.3.7. 常使用的设计模式
&emsp; 面试题：你使用过哪些设计模式？ 根据实际使用，设计模式分3类：  

* 框架：SpringAOP、池化（享元模式）、 mq
* 不自觉使用的设计模式，如外观/门面模式、 **<font color = "clime">对象适配器模式（Service层调用）</font>** 。  
* 需要编码：单例模式与static静态类，工厂模式，模板方法，3个if/else的优化：桥接模式、策略模式、责任链模式，观察者模式...  

## 1.3. JVM
### 1.3.1. JDK、JRE、JVM
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-4.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-145.png)  
1. <font color = "red">JVM由4大部分组成：类加载器ClassLoader，运行时数据区Runtime Data Area，执行引擎Execution Engine，本地方法调用Native Interface。</font>  
2. **<font color = "clime">JVM各组件的作用（JVM执行程序的过程）：</font>**   
    1. 编译：首先通过类加载器（ClassLoader）把Java代码转换成字节码；  
    2. 加载：运行时数据区（Runtime Data Area）再把字节码加载到内存中；  
    3. 运行：<font color = "red">而字节码文件只是JVM的一套指令集规范，并不能直接交给底层操作系统去执行，因此`需要特定的命令解析器执行引擎（Execution Engine），将字节码翻译成底层系统指令，再交由CPU去执行；`</font>  
    4. 而这个过程中需要调用其他语言的本地库接口（Native Interface）来实现整个程序的功能。  

### 1.3.2. 编译成Class字节码文件
&emsp; ......

### 1.3.3. 类加载
#### 1.3.3.1. JVM类的加载
1. 类加载流程：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-5.png)  
2. 加载：查找并加载类的二进制数据。加载主要做三件事：找到类文件 -> 放入方法区 -> 开个入口（最后生成一个代表此类的java.lang.Class对象，作为访问方法区这些数据结构的入口）。  
&emsp; **<font color = "red">一句话概括：把代码数据加载到内存中，加载完成后，在方法区实例化一个对应的Class对象。</font>**  
3. 验证：确保被加载的类的正确性。验证阶段大致会完成4个阶段的检验动作：1. 文件格式验证、2. 元数据验证、3. 字节码验证、4. 符号引用验证。  
4. 准备(Preparation)  
&emsp; <font color = "red">为类的静态变量分配内存，并将其初始化为默认值，这些内存都将在方法区中分配。</font>对于该阶段有以下几点需要注意：  
    1. <font color = "red">这时候进行内存分配的仅包括类变量(static)，而不包括实例变量，实例变量会在对象实例化时随着对象一块分配在Java堆中。</font>  
    2. <font color = "red">这里所设置的初始值"通常情况"下是数据类型默认的零值(如0、0L、null、false等)，比如定义了public static int value=111 ，那么 value 变量在准备阶段的初始值就是 0 而不是111(初始化阶段才会复制)。</font>  
    * <font color = "red">特殊情况：比如给value变量加上了fianl关键字public static final int value=111，那么准备阶段value的值就被赋值为 111。</font>  
5. 解析(Resolution)： **<font color = "red">将常量池内的符号引用转换为直接引用</font>** ，得到类或者字段、方法在内存中的指针或者偏移量，确保类与类之间相互引用正确性，完成内存结构布局，以便直接调用该方法。  
&emsp; `为什么要用符号引用呢？` **<font color = "blue">这是因为类加载之前，javac会将源代码编译成.class文件，这个时候javac是不知道被编译的类中所引用的类、方法或者变量它们的引用地址在哪里，所以只能用符号引用来表示。</font>**  
&emsp; **<font color = "clime">解析过程在某些情况下可以在初始化阶段之后再开始，这是为了支持Java的动态绑定。</font>**   
6. 初始化：执行`static代码块(cinit)进行初始化`，如果存在父类，先对父类进行初始化。  
7. 扩展：从class文件与JVM加载机制理解final、static、static final  

#### 1.3.3.2. JVM类加载器
1. JVM默认提供三个类加载器：启动类加载器、扩展类加载器、应用类加载器。  
&emsp; 自定义类加载器：需要继承自ClassLoader，`重写方法findClass()`（⚠`破坏类加载器是重写loadClass()方法`）。      
2. 双亲委派模型，一个类加载器首先将类加载请求转发到父类加载器，只有当父类加载器无法完成时才尝试自己加载。  
&emsp; 双亲委派模型中，类加载器之间的父子关系一般不会以继承（Inheritance）的关系来实现，而是使用组合（Composition）关系来复用父加载器的代码的。  
&emsp; `好处：避免类的重复加载；防止核心API被随意篡改。`   
3. tomcat类加载器  
    &emsp; 根据实际的应用场景，分析下 tomcat 类加载器需要解决的几个问题

    * 为了避免类冲突，每个 webapp 项目中各自使用的类库要有隔离机制
    * 不同 webapp 项目支持共享某些类库
    * 类加载器应该支持热插拔功能，比如对 jsp 的支持、webapp 的 reload 操作

    &emsp; 为了解决以上问题，tomcat设计了一套类加载器，如下图所示。在 Tomcat 里面最重要的是 Common 类加载器，它的父加载器是应用程序类加载器，负责加载 ${catalina.base}/lib、${catalina.home}/lib 目录下面所有的 .jar 文件和 .class 文件。下图的虚线部分，有 catalina 类加载器、share 类加载器，并且它们的 parent 是 common 类加载器，默认情况下被赋值为 Common 类加载器实例，即 Common 类加载器、catalina 类加载器、 share 类加载器都属于同一个实例。当然，如果通过修改 catalina.properties 文件的 server.loader 和 shared.loader 配置，从而指定其创建不同的类加载器。  

    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/tomcat/tomcat-1.png)  

3. 破坏双亲委派模型：  
    1. 破坏双亲委派模型：继承ClassLoader，重写loadClass()方法。  
    1. `双亲委派模型有一个问题：顶层ClassLoader无法加载底层ClassLoader的类，典型例子JNDI、JDBC。`
        * **<font color = "clime">JDBC是启动类加载器加载，但 mysql 驱动是应用类加载器，而 JDBC 运行时又需要去访问子类加载器加载的驱动，就破坏了该模型。所以加入了`线程上下文类加载器(Thread Context ClassLoader)`，</font>** 可以通过Thread.setContextClassLoaser()设置该类加载器，然后顶层ClassLoader再使用Thread.getContextClassLoader()获得底层的ClassLoader进行加载。  
    2. Tomcat中使用了自定义ClassLoader，使得一个Tomcat中可以加载多个应用。一个Tomcat可以部署N个web应用，但是每个web应用都有自己的classloader，互不干扰。比如web1里面有com.test.A.class，web2里面也有com.test.A.class，`如果没打破双亲委派模型的话，那么web1加载完后，web2再加载的话会冲突。`    
    3. Spring破坏双亲委派模型  
    &emsp; Spring要对用户程序进行组织和管理，而`用户程序一般放在WEB-INF目录下，由WebAppClassLoader类加载器加载，而Spring由Common类加载器或Shared类加载器加载。`   
    &emsp; 那么Spring是如何访问WEB-INF下的用户程序呢？   
    &emsp; 使用线程上下文类加载器。 Spring加载类所用的classLoader都是通过Thread.currentThread().getContextClassLoader()获取的。当线程创建时会默认创建一个AppClassLoader类加载器（对应Tomcat中的WebAppclassLoader类加载器）：setContextClassLoader(AppClassLoader)。   
    &emsp; 利用这个来加载用户程序。即任何一个线程都可通过getContextClassLoader()获取到WebAppclassLoader。  

### 1.3.4. 运行时数据区/内存结构
#### 1.3.4.1. JVM内存结构
##### 1.3.4.1.1. JVM内存结构
&emsp; Java虚拟机在执行Java程序的过程中会把它管理的内存划分成若干个不同的数据区域。JDK1.8和之前的版本略有不同。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-7.png)  
1. 运行时数据区。线程独享：程序计数器、JVM栈、本地方法栈；线程共享区：堆、方法区（元空间）。  
2. 程序计数器看作是当前线程所执行的字节码的行号指示器。  
3. JVM栈/【方法】【栈】  
    1. <font color = "red">JVM栈描述Java方法执行的内存模型。</font>Java虚拟机栈是线程私有的。Java虚拟机栈会出现两种异常：StackOverFlowError和OutOfMemoryError。    
    2. 【方法】栈  
    &emsp; Java虚拟机栈中出栈入栈的元素称为“栈帧”，栈对应线程，栈帧对应方法。每个方法被执行的时候，都会创建一个栈帧，把栈帧压入栈，当方法正常返回或者抛出未捕获的异常时，栈帧就会出栈。  
    3. 方法【栈】  
        &emsp; <font color = "red">Java虚拟机栈是由一个个栈帧组成，每个栈帧中都拥有：局部变量表、操作数栈、动态链接、方法出口信息。</font>  
        ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-9.png)  
        * 局部变量表，局部变量表用于保存函数参数和局部变量。  
        * 操作数栈，操作数栈用于保存计算过程的中间结果，作为计算过程中变量临时的存储空间。  
        * 动态连接：每个栈帧中包含一个在常量池中对当前方法的引用， 目的是支持方法调用过程的动态连接。  
        * 方法返回地址
    4. `为什么不把基本类型放堆中呢？`   
    &emsp; 因为其占用的空间一般是 1~8 个字节——需要空间比较少，而且因为是基本类型，所以不会出现动态增长的情况——长度固定，因此栈中存储就够了。  
4. 【GC】【堆】  
    1. 【GC】堆：分代回收，堆分为新生代、老年代，默认比例1: 2。 新生代又按照8: 1: 1划分为Eden区和两个Survivor区。  
    2. **<font color = "blue">在Eden区中，JVM为每个线程分配了一个私有缓存区域[TLAB(Thread Local Allocation Buffer)](/docs/java/JVM/MemoryObject.md)。</font>** `新生代 -> Eden -> TLAB`      
    3. GC【堆】：  
        1. 堆中存放对象。  
        2. 堆是分配对象存储的唯一选择吗？[逃逸分析](/docs/java/JVM/escape.md)  
5. 方法区/永久代：
    1. 在类加载阶段，在Java堆中生成一个代表这个类的java.lang.Class对象，作为对方法区中这些数据的访问入口。  
    2. <font color = "clime">方法区的演进：</font>  
        1. 为什么JDK1.8移除元空间  
            1. 由于PermGen内存经常会溢出，引发java.lang.OutOfMemoryError: PermGen，因此JVM的开发者希望这一块内存可以更灵活地被管理，不要再经常出现这样的OOM。  
            2. 移除PermGen可以促进HotSpot JVM与JRockit VM的融合，因为JRockit没有永久代。  
        2. 演进历程：  
            * jdk1.6及之前：有永久代(permanent generation)。静态变量存放在永久代上。  
            * jdk1.7：有永久代，但已经逐步“去永久代”。[字符串常量池](/docs/java/JVM/ConstantPool.md) <font color = "red">、静态变量</font>移除，保存在堆中。  
            * jdk1.8及之后：无永久代。类型信息、字段、方法、<font color = "red">常量</font>保存在本地内存的元空间，<font color = "clime">但字符串常量池、静态变量仍在堆。</font>  
6. MetaSpace存储类的元数据信息。  
&emsp; 元空间与永久代之间最大的区别在于：元数据空间并不在虚拟机中，而是使用本地内存。元空间的内存大小受本地内存限制。  
7. 扩展点：静态方法和实例方法  
&emsp; `静态方法`会在程序运行的时候`直接装载进入方法区`。而实例方法会在new的时候以对象的方法装载进入堆中。  
&emsp; 最大的区别在于内存的区别，由于main函数为static静态方法，会直接在运行的时候装载进入内存区，实例方法必须new，在堆中创建内存区域。再进行引用。  

##### 1.3.4.1.2. 常量池详解
&emsp; **<font color = "clime">常量池分为以下三种：class文件常量池、运行时常量池、全局字符串常量池。</font>**   


#### 1.3.4.2. 内存(堆栈)中的对象
##### 1.3.4.2.1. 创建对象
1. **<font color = "clime">对象创建过程：1. 检测类是否被加载；2. 为对象分配内存；3. 将分配内存空间的对象初始化零值；4. 对对象进行其他设置；5.执行init方法。</font>**   

        当类加载完成之后，紧接着就是对象分配内存空间和初始化的过程
        首先为对象分配合适大小的内存空间
        接着为实例变量赋默认值
        设置对象的头信息，对象hash码、GC分代年龄、元数据信息等
        执行构造函数(init)初始化

2. 步骤二：对象分配内存流程详解：  
    * 堆内存分配策略：  
    &emsp; 分配策略有：对象优先在Eden分配、大对象直接进入老年代、长期存活的对象将进入老年代、动态对象年龄判定、空间分配担保。  
    &emsp; `空间分配担保：` **<font color = "clime">JVM在发生Minor GC之前，虚拟机会检查老年代最大可用的`连续空间`是否大于新生代所有对象的`总空间`。</font>**   
    * **<font color = "blue">`内存分配全流程：`逃逸分析 ---> 没有逃逸，尝试栈上分配 ---> 是否满足直接进入老年代的条件 ---> `尝试TLAB分配` ---> `新生代Eden区分配`。</font>**  
    * 分配内存两种方式：指针碰撞（内存空间绝对规整）；空闲列表（内存空间是不连续的）。
        * 标记-整理或复制 ---> 空间规整 ---> 指针碰撞； 
        * 标记-清除 ---> 空间不规整 ---> 空闲列表。       
    * 线程安全问题：1).采用CAS； **<font color = "clime">2).线程本地分配缓冲（TLAB）。</font>**  
        &emsp; **<font color = "blue">TLAB详解：</font>**  
        * 线程本地分配缓存，这是一个线程专用的内存分配区域。可以加速对象的分配。TLAB是在堆中开辟的内存区域。默认情况下，TLAB空间的内存非常小，仅占有整个Eden空间的1%。  
        * **<font color = "blue">TLAB通常很小，所以放不下大对象。`JVM设置了最大浪费空间`。</font>**  
        &emsp; 当大对象申请内存时，当剩余的空间小于最大浪费空间，那该TLAB属于的线程在重新向Eden区申请一个TLAB空间。进行对象创建，还是空间不够，那这个对象太大了，去Eden区直接创建吧！  
        &emsp; 当剩余的空间大于最大浪费空间，那这个大对象直接去Eden区创建。剩余空间还需要使用。
3. 逃逸分析
    1. <font color = "red">通过逃逸分析算法可以分析出某一个方法中的某个对象是否会被其它方法或者线程访问到。</font>如果分析结果显示某对象并不会被其他方法引用或被其它线程访问，则有可能在编译期间做一些深层次的优化。   
    2. 对于NoEscape（ **<font color = "clime">没有逃逸</font>** ）状态的对象不一定分配在堆中，具体会有这种优化情况：   
        1. 对象可能分配在栈上。  
        2. `分离对象或标量替换。`  
        &emsp; **<font color = "clime">在HotSpot中并没有真正的实现"栈"中分配对象的功能，取而代之的是一个叫做"标量替换"的折中办法。</font>**  
        &emsp; 什么是标量？标量，不可再分，基本数据类型；相对的是聚合量，可再分，引用类型。  
        &emsp; **当JVM通过逃逸分析，确定要将对象分配到栈上时，即时编译可以将对象打散，将对象替换为一个个很小的局部变量，将这个打散的过程叫做标量替换。** 
        3. 消除同步锁 

##### 1.3.4.2.2. 对象生命周期

##### 1.3.4.2.3. 对象大小
1. 在JVM中，对象在内存中的布局分为三块区域：对象头、实例数据和对齐填充。  
    * 实例数据：存放类的属性数据信息，包括父类的属性信息，如果是数组的实例部分还包括数组的长度，这部分内存按4字节对齐。    
    * 对齐填充：JVM要求对象起始地址必须是8字节的整数倍(8字节对齐)。填充数据不是必须存在的，仅仅是为了字节对齐。   
2. JVM中对象头的方式有以下两种（以32位JVM为例）  
    &emsp; 普通对象：  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-60.png)   
    &emsp; 数组对象：  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-61.png)   

    对象头：包含Mark Word、class pointer、array length共3部分。  
    1. Mark Word：  
    &emsp; **<font color = "red">由于对象头信息是与对象自身定义的数据无关的额外存储成本，考虑到Java虚拟机的空间使用效率，</font>** **<font color = "clime">Mark Word被设计成一个非固定的动态数据结构，</font>** 以便在极小的空间内存储尽量多的信息。它会根据对象的状态复用自己的存储空间。  
    &emsp; 这部分主要用来存储对象自身的运行时数据，如hashcode、gc分代年龄等。mark word的位长度为JVM的一个Word大小，也就是说32位JVM的Mark word为32位，64位JVM为64位。
    为了让一个字大小存储更多的信息，JVM将字的最低两个位设置为标记位，  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-67.png)   
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-68.png)   
    2. class pointer：  
    &emsp; 这一部分用于存储对象的类型指针，该指针指向它的类元数据，JVM通过这个指针确定对象是哪个类的实例。该指针的位长度为JVM的一个字大小，即32位的JVM为32位，64位的JVM为64位。 
    3. array length：  
    &emsp; 如果对象是一个数组，那么对象头还需要有额外的空间用于存储数组的长度，这部分数据的长度也随着JVM架构的不同而不同：32位的JVM上，长度为32位；64位JVM则为64位。64位JVM如果开启+UseCompressedOops选项，该区域长度也将由64位压缩至32位。  

#### 1.3.4.3. 内存泄露
1. 内存溢出与内存泄露  
&emsp; **<font color = "red">内存溢出out of memory</font>** ，是指<font color = "red">程序在申请内存时，`没有足够的内存空间供其使用`</font>，出现out of memory。  
&emsp; **<font color = "blue">内存泄露memory leak</font>** ，是指<font color = "red">程序在申请内存后，`无法释放已申请的内存空间`</font>。一次内存泄露危害可以忽略，但内存泄露堆积后果很严重，无论多少内存，迟早会被占光。内存泄露，会导致频繁的Full GC。  
&emsp; 所以内存泄漏可能会导致内存溢出，但内存溢出并不完全都是因为内存泄漏，也有可能使用了太多的大对象导致。  
2. 内存溢出影响  
&emsp; **<font color = "clime">问题：`JVM堆内存溢出后，其他线程是否可继续工作？`</font>**  
&emsp; 当一个线程抛出OOM异常后，它所占据的内存资源会全部被释放掉，从而不会影响其他线程的运行！  
&emsp; **<font color = "red">其实发生OOM的线程一般情况下会死亡，也就是会被终结掉，该线程持有的对象占用的heap都会被gc了，释放内存。</font><font color = "clime">因为`发生OOM之前要进行gc，就算其他线程能够正常工作，也会因为频繁gc产生较大的影响。`</font>**  

### 1.3.5. JVM执行
&emsp; ...

### 1.3.6. GC
&emsp; ⚠️⚠️⚠️一句话小结：`垃圾回收器` 在 `安全点/安全区域` 采用`回收算法` `分代/整堆` 回收 `(堆)根不可达的对象 或 (方法区)类/常量`。  

#### 1.3.6.1. GC-回收位置/安全点
1. 安全点  
&emsp; **<font color = "clime">可达性分析算法必须是在一个确保一致性的内存快照中进行。</font>**   
&emsp; **<font color = "clime">安全点意味着在这个点时，所有工作线程的状态是确定的，JVM可以安全地执行GC。</font>**  
2. `安全区域和线程中断`  
&emsp; `在安全点上中断的是活跃运行的用户线程，对于已经挂起的线程该怎么处理呢？`**<font color = "blue">`已经挂起的线程`会被认定为处在`安全区域`内，中断的时候不需要考虑安全区域中的线程。</font>**  
&emsp; 当前安全区域的线程要被唤醒离开安全区域时，先检查能否离开，如果GC完成了，那么线程可以离开，否则它必须等待，直到收到安全离开的信号为止。  

#### 1.3.6.2. 回收算法与分代回收
1. GC算法  
    * **<font color = "clime">标记-清除算法分为两个阶段：标记阶段和清除阶段。</font>** 不足：清除过程中，扫描两次，效率不高；清除后，产生空间碎片。  
    * `复制：1).（非标记-复制）只扫描一次；` 2). 没有碎片，空间连续； 3). 50%的内存空间始终空闲浪费。  
    * 标记-整理：1). 没有碎片，空间连续； 2). 不会产生内存减半； 3). 扫描两次，指针需要调整(移动对象)，效率偏低。  
    &emsp; **<font color = "clime">标记-清除和标记-整理都需要扫描两次。</font>**   
2. 新生代采用复制算法；老年代采用标记-整理算法。 **<font color = "clime">注意：CMS回收老年代，但采用标记-清除算法；CMS收集器也会在内存空间的碎片化程度已经大到影响对象分配时，采用标记-整理算法收集一次（晋升失败(promotion failed) 或 并发模式失败(concurrent mode failure)），以获得规整的内存空间。</font>**    
    * `新生代采用复制算法。新生代存活率低，存活对象少。标记-清除算法效率高，搬运对象也比较少。`  
3. 分代回收流程  
4. 跨代引用假说（跨代引用相对于同代引用仅占少数）  
&emsp; **既然跨代引用只是少数，那么就没必要去扫描整个老年代，也不必专门记录每一个对象是否存在哪些跨代引用，只需在新生代上建立一个全局的数据结构，称为记忆集(Remembered Set)，这个结构把老年代划分为若干个小块，标识出老年代的哪一块内存会存在跨代引用。此后当发生Minor GC时，只有包含了跨代引用的小块内存里的对象才会被加入GC Roots进行扫描。**  
&emsp; `卡表是记忆集的一种实现方式。`  
5. 各种GC：  
    * `Partial GC(局部 GC)：并不收集整个 GC 堆的模式。`  
        * Young GC：只收集 Young Gen 的 GC，Young GC 还有种说法就叫做 Minor GC。  
        * Old GC：只收集 old gen 的 GC，只有垃圾收集器 CMS 的 concurrent collection 是这个模式。  
        * Mixed GC：收集整个 Young Gen 以及部分 old gen 的 GC，只有垃圾收集器 G1 有这个模式。  
    * `Full GC：收集整个堆，包括新生代，老年代，永久代(在 JDK 1.8 及以后，永久代被移除，换为 metaspace 元空间)等所有部分的模式。`  
6. YGC触发时机：eden区快要被占满的时候；在full gc前会先执行young gc。  
7. Full GC   
&emsp; **<font color = "red">Full GC的触发时机：</font>**   
    ⚠️注：是否满足年轻代晋升 --- 老年代或永久代空间是否充足 --- 系统主动调用
    1. 老年代`不满足`年轻代晋升  
        1. 统计得到的Minor GC晋升到老年代的`平均大小`大于老年代的剩余空间  
        &emsp; Hotspot为了避免由于新生代对象晋升到老年代导致老年代空间不足的现象，在进行Minor GC时，做了一个判断，如果之前统计所得到的Minor GC晋升到老年代的`平均大小`大于老年代的剩余空间，那么就直接触发Full GC。  
        2. `空间分配担保失败`  
        &emsp; **<font color = "clime">JVM在发生Minor GC之前，虚拟机会检查老年代`最大可用的连续空间`是否大于新生代所有对象的`总空间`，</font>** 如果大于，则此次Minor GC是安全的；如果小于，则虚拟机会查看HandlePromotionFailure设置项的值是否允许担保失败。如果HandlePromotionFailure=true，那么会继续检查老年代`最大可用连续空间`是否大于历次晋升到老年代的对象的平均大小，如果大于则尝试进行一次Minor GC，但这次Minor GC依然是有风险的；如果小于或者HandlePromotionFailure=false，则改为进行一次Full GC。   
        3. `CMS GC时出现promotion failed（晋升失败）和concurrent mode failure（并发模式失败）`  
        &emsp; 执行CMS GC的过程中同时有对象要放入老年代，而此时老年代空间不足（可能是GC过程中浮动垃圾过多导致暂时性的空间不足），便会报Concurrent Mode Failure错误，并触发Full GC。  
    2. 老年代或永久代的`不足`
        1. 老年代空间不足(92%)  
        &emsp; 老年代空间不足的常见场景为大对象直接进入老年代、长期存活的对象进入老年代等。  
        &emsp; 为了避免以上原因引起的Full GC，应当尽量不要创建过大的对象以及数组。除此之外，可以通过-Xmn虚拟机参数调大新生代的大小，让对象尽量在新生代被回收掉，不进入老年代。还可以通过 -XX:MaxTenuringThreshold调大对象进入老年代的年龄，让对象在新生代多存活一段时间。  
        2. JDK 1.7及以前的永久代空间不足  
        &emsp; 为避免以上原因引起的Full GC，可采用的方法为增大永久代空间或转为使用CMS GC。  
    3. <font color = "red">系统调用System.gc()</font>  
    &emsp; 只是建议虚拟机执行Full GC，但是虚拟机不一定真正去执行。不建议使用这种方式，而是让虚拟机管理内存。  

#### 1.3.6.3. GC-回收对象

##### 1.3.6.3.1. 堆中对象的存活
1. 存活标准
    1. 引用计数法、根可达性分析法  
        1. **<font color = "clime">不可回收对象包含 1). 方法区中，类静态属性(static)引用的对象； 2). 方法区中，常量(final static)引用的对象；</font>** 
        2. `由以上可得java 全局变量 不可被回收。`  
    2. 四种引用  
        * **<font color = "red">软引用：SoftReference object=new  SoftReference(new Object()); 。当堆使用率临近阈值时，才会去回收软引用的对象。</font>**  
        * **<font color = "red">弱引用：WeakReference object=new  WeakReference (new Object();。ThreadLocal中有使用。只要发现弱引用，不管系统堆空间是否足够，都会将对象进行回收。</font>**  

                软引用和弱引用的使用：
                软引用，弱引用都非常适合来保存那些可有可无的缓存数据，如果这么做，当系统内存不足时，这些缓存数据会被回收，不会导致内存溢出。而当内存资源充足时，这些缓存数据又可以存在相当长的时间，从而起到加速系统的作用。  
                假如⼀个应⽤需要读取⼤量的本地图⽚，如果每次读取图⽚都从硬盘读取会严重影响性能，如果⼀次性全部加载到内存⼜可能造成内存溢出，这时可以⽤软引⽤解决这个问题。  

        * 虚引用：PhantomReference。虚引用是所有类型中最弱的一个。 **<font color = "red">一个持有虚引用的对象，和没有引用几乎是一样的，随时可能被垃圾回收器回收。</font>**    
2. 对象生存还是死亡？  
    1. Object#finalize()⽅法介绍：  
    &emsp; Object#finalize()⽅法什么时候被调⽤？析构函数(finalization)的⽬的是什么？  
    &emsp; 垃圾回收器(garbage colector)决定回收某对象时，就会运⾏该对象的finalize()⽅法，但是在Java中很不幸，如果内存总是充⾜的，那么垃圾回收可能永远不会进⾏，也就是说filalize() 可能永远不被执⾏，显然指望它做收尾⼯作是靠不住的。  
    &emsp; 那么finalize()究竟是做什么的呢？它最主要的⽤途是回收特殊渠道申请的内存。Java程序有垃圾回收器，所以⼀般情况下内存问题不⽤程序员操⼼。但有⼀种JNI(Java Native Interface)调⽤non-Java程序(C或C++)， finalize()的⼯作就是回收这部分的内存。  
    2. <font color = "red">在可达性分析算法中，不可达的对象也不是一定会死亡的，它们暂时都处于“缓刑”阶段，要真正宣告一个对象“死亡”，至少要经历两次标记过程。</font>  
        1. <font color = "red">判断有没有必要执行Object#finalize()方法</font>  
        &emsp; **<font color = "clime">如果对象在进行可达性分析后发现没有与GC Roots相连接的引用链，那它将会被第一次标记并且进行一次筛选，</font>** 筛选的条件是此对象是否有必要执行finalize()方法。  
        &emsp; 另外，有两种情况都视为“没有必要执行”：1)对象没有覆盖finaliza()方法；2)finalize()方法已经被虚拟机调用过。  
        2. <font color = "red">如何执行？</font>F-Queue的队列  
        &emsp; 如果这个对象被判定为有必要执行finalize()方法，那么此对象将会放置在一个叫做F-Queue的队列中，并在稍后由一个虚拟机自动建立的、低优先级的Finalizer线程去执行它。  
        3. <font color = "red">执行死亡还是逃脱死亡？</font>  
        &emsp; **<font color = "clime">首先，需要知道，finalize()方法是对象逃脱死亡命运的最后一次机会，稍后收集器将对F-Queue队列中的对象进行第二次小规模的标记。</font>**  
        &emsp; 逃脱死亡：对象想在finalize()方法中成功拯救自己，只要重新与引用链上的任何一个对象建立关联即可，例如把自己(this关键字)赋值给某个类变量或者对象的成员变量，这样在第二次标记时它将被移出“即将回收”的集合。  
        &emsp; 执行死亡：对象没有执行逃脱死亡，那就是死亡了。  
        &emsp; 注：任何对象的finalize()方法都只会被系统调用一次。  


##### 1.3.6.3.2. 方法区(类和常量)回收/类的卸载阶段
1. Java虚拟机规范对方法区是否实现垃圾回收没有做出强制的规定。存在未实现或未能完整实现方法区类型卸载的垃圾回收器（例如JDK 11的zGC收集器）。    
&emsp; 方法区的回收效果比较难令人满意，条件很苛刻，但是回收又是很有必要的。在大量使用反射、动态代理、CGLib等字节码框架，动态生成JSP以及OSGi这类频繁自定义类加载器的场景中，通常都需要Java虚拟机具备类型卸载的能力，以保证不会对方法区造成过大的内存压力。  
2. **<font color = "clime">方法区的垃圾收集主要回收两部分：废弃的常量和不再使用的类型。</font>**  
3. 类的卸载
    1. 类需要同时满足下面3个条件才能算是 “无用的类” ：  
        * 该类所有的实例都已经被回收，也就是 Java 堆中不存在该类的任何实例。
        * 加载该类的 ClassLoader 已经被回收。
        * 该类对应的 java.lang.Class 对象没有在任何地方被引用，无法在任何地方通过反射访问该类的方法。
    2. `一个类何时结束生命周期，取决于代表它的Class对象何时结束生命周期。`   
    &emsp; 注：`由java虚拟机自带的三种类加载加载的类在虚拟机的整个生命周期中是不会被卸载的，`由用户自定义的类加载器所加载的类才可以被卸载。     
    3. `★★★方法区是在Full GC时回收。`  
    &emsp; Full GC: 收集整个堆，包括新生代，老年代，永久代(在 JDK 1.8 及以后，永久代被移除，换为 metaspace 元空间)等所有部分的模式。  

##### 1.3.6.3.3. null与GC  
&emsp; 《深入理解Java虚拟机》作者的观点：在需要“不使用的对象应手动赋值为null”时大胆去用，但不应当对其有过多依赖，更不能当作是一个普遍规则来推广。  
&emsp; **<font color = "red">虽然代码片段已经离开了变量xxx的`作用域`，但在此之后，没有任何对运行时栈的读写，placeHolder所在的索引还没有被其他变量重用，所以GC判断其为存活。</font>**    
&emsp; 加上`int replacer = 1;`和将placeHolder赋值为null起到了同样的作用：断开堆中placeHolder和栈的联系，让GC判断placeHolder已经死亡。    
&emsp; “不使用的对象应手动赋值为null”的原理，一切根源都是来自于JVM的一个“bug”：代码离开变量作用域时，并不会自动切断其与堆的联系。    


#### 1.3.6.4. GC-垃圾回收器
##### 1.3.6.4.1. 垃圾回收器
1. 根据收集器的指标（性能考虑因素）分类（`两个关键指标，停顿时间和吞吐量`）：  
    * **<font color = "clime">吞吐量：运行用户代码时间/(运行用户代码时间+垃圾收集时间)。</font>**  
    * 停顿时间：执行垃圾收集时，程序的工作线程被暂停的时间。  
    * 内存占有（堆空间）：Java堆区所占的内存大小。  
    * 垃圾收集开销：吞吐量的补数，垃圾收集器所占时间与总时间的比例。  
    * 收集频率：相对于应用程序的执行，收集操作发生的频率。  
    * 快速：一个对象从诞生到被回收所经历的时间。  

&emsp; <font color  = "red">其中内存占用、吞吐量和停顿时间，三者共同构成了一个“不可能三角”。</font>   

    * 停顿时间越短就越适合需要和用户交互的程序，良好的响应速度能提升用户体验；  
    * 高吞吐量则可以高效地利用CPU时间，尽快完成程序的运算任务，主要适合在后台运算而不需要太多交互的任务。  

2. 根据运行时，线程执行方式分类：  
    * 串行收集器 -> Serial和Serial Old  
    &emsp; **<font color = "red">只能有一个垃圾回收线程执行，用户线程暂停。</font>** 适用于内存比较小的嵌入式设备 。  
    * 并行收集器【吞吐量优先】 -> Parallel Scanvenge、Parallel Old  
    &emsp; **<font color = "red">多条垃圾收集线程并行工作，但此时用户线程仍然处于等待状态。</font>** 适用于科学计算、后台处理等交互场景 。  
    * 并发收集器【停顿时间优先】 -> CMS、G1  
    &emsp; **<font color = "red">用户线程和垃圾收集线程同时执行</font><font color = "blue">（但并不一定是并行的，可能是交替执行的），</font><font color = "red">垃圾收集线程在执行的时候不会停顿用户线程的运行。</font>** 适用于相对时间有要求的场景，比如Web。  
3. `JDK 7u4后的7和JDK8默认使用的都是ParallelScavenge+ParallelOld。`  

##### 1.3.6.4.2. CMS回收器
1. **<font color = "clime">CMS在某些阶段是并发，即CMS GC时并不是全部并发执行。大部分并发，但也有停顿(STW)，只是停顿时间更少。因为CMS是并发收集器，为了不影响用户线程使用，所以采用标记-清除算法。</font>**   
2. CMS GC执行流程：(**<font color = "clime">3次标记、2次清除</font>**)  
    1. 初始标记：标记GCRoots能直接关联到的对象。   
    2. 并发标记：进行GCRoots Tracing（可达性分析）过程，GC与用户线程并发执行。
    3. 预清理：（`三色标记法的漏标问题处理`） **<font color = "red">这个阶段是用来</font><font color = "blue">处理</font><font color = "clime">前一个并发标记阶段因为引用关系改变导致没有标记到的存活对象的。如果发现对象的引用发生变化，则JVM会标记堆的这个区域为Dirty Card。那些能够从Dirty Card到达的对象也被标记（标记为存活），当标记做完后，这个Dirty Card区域就会消失。</font>**  
    4. 可终止的预处理。这个阶段`尝试着去承担下一个阶段Final Remark阶段足够多的工作`。  
    5. 重新标记（remark）：修正并发标记期间，因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录。
    6. 并发清除：并发、标记-清除，GC与用户线程并发执行。   
    7. 并发重置。
3. `CMS的特点：`  
    1. 划时代的并发收集器。`关注停顿时间。⚠️注：GC的两个关键指标：停顿时间和吞吐量。`    
    2. `吞吐量低。`并发执行，线程切换。  
    3. **<font color = "blue">并发执行，`产生浮动垃圾（参考三色标记法中“错标”）`。</font>**  
    4. 使用"标记-清除"算法，产生空间碎片。CMS GC在老生代回收时产生的内存碎片会导致老生代的利用率变低；或者可能在老生代总内存大小足够的情况下，却不能容纳新生代的晋升行为（由于没有连续的内存空间可用），导致触发FullGC。  
        &emsp; 针对这个问题，`Sun官方给出了以下解决内存碎片问题的方法：`  
        * 增大Xmx或者减少Xmn  
        * `在应用访问量最低的时候，在程序中主动调用System.gc()，比如每天凌晨。`  
        * 在应用启动并完成所有初始化工作后，主动调用System.gc()，它可以将初始化的数据压缩到一个单独的chunk中，以腾出更多的连续内存空间给新生代晋升使用。  
        * `降低-XX:CMSInitiatingOccupancyFraction参数（内存占用率，默认70%）以提早执行CMS GC动作，`虽然CMS GC不会进行内存碎片的压缩整理，但它会合并老年代中相邻的free空间。这样就可以容纳更多的新生代晋升行为。 
        * CMS收集器提供了一个-XX：+UseCMS-CompactAtFullCollection开关参数（默认是开启的，此参数从JDK 9开始废弃），用于在CMS收集器不得不进行Full GC时开启内存碎片的合并整理过程。`还提供了另外一个参数-XX：CMSFullGCsBefore-Compaction（此参数从JDK 9开始废弃），这个参数的作用是要求CMS收集器在执行过若干次（数量由参数值决定）不整理空间的Full GC之后，下一次进入Full GC前会先进行碎片整理（默认值为0，表示每次进入Full GC时都进行碎片整理）。`  
    5. `晋升失败（新生代垃圾回收）与并发模式失败（CMS垃圾回收）`：都会退化成单线程的Full GC。  
        * 晋升失败(promotion failed)：`当新生代发生垃圾回收`， **老年代有足够的空间可以容纳晋升的对象，但是由于空闲空间的碎片化，导致晋升失败。** ~此时会触发单线程且带压缩动作的Full GC。~  
        * 并发模式失败(concurrent mode failure)：`当CMS在执行回收时`，新生代发生垃圾回收，同时老年代又没有足够的空间容纳晋升的对象时。CMS垃圾回收会退化成单线程的Full GC。所有的应用线程都会被暂停，老年代中所有的无效对象都被回收。  
    6. 减少remark阶段停顿：在执行并发操作之前先做一次Young GC。  

##### 1.3.6.4.3. G1回收器
1. G1是一种服务端应用使用的垃圾收集器，目标是用在`多核、大内存`的机器上， **<font color = "clime">G1在大多数情况下可以`实现指定的GC暂停时间，同时还能保持较高的吞吐量`。</font>**   
2. G1特点（为什么要选择G1？）  
    1. 并行和并发
    2. 分代收集：G1逻辑分代但物理不分代，将整个Java堆划分为多个大小相等的独立区域(Region)。E区、S区、H区、O区。  
    3. **<font color = "clime">`空间整合（不产生内存碎片）：`</font>** 与CMS的“标记--清理”算法不同，<font color = "red">G1从整体来看是基于“标记整理”算法实现的收集器；从局部上来看是基于“复制”算法实现的。</font>这两种算法都意味着<font color = "clime">G1运作期不会产生内存空间碎片</font>，收集后能提供规整的可用内存。这种特性有利于程序长时间运行，分配大对象吋不会因为无法找到连续内存空而提前触发下一次GC。  
    4. **<font color = "clime">`可预测的停顿：`</font>** 这是G1相对于CMS的另一个大优势，<font color = "red">降低停顿时间是G1和CMS共同的关注点，但G1除了追求低停顿外，还能建立可预测的停顿时间模型，</font>能让使用者明确指定在一个长度为M毫秒的时间片段内，消耗在垃圾收集上的时间不得超过N毫秒。  
3. 回收流程：G1的收集过程可能有4个阶段：新生代GC、老年队并发标记周期、混合回收、如果需要可能会进行Full GC。   
    1. 老年队并发标记周期  
    &emsp; **<font color = "clime">当整个堆内存（包括老年代和新生代）被占满一定大小的时候（默认是45%，可以通过-XX:InitiatingHeapOccupancyPercent进行设置），老年代回收过程会被启动。</font>**  
    &emsp; **<font color = "clime">老年队并发标记周期，回收百分之百为垃圾的内存分段，</font>** H区（本质是o区）Humongous对象会独占整个内存分段。  
    2. 混合回收MixGC  
    &emsp; 老年代并发标记过程结束以后，紧跟着就会开始混合回收过程。混合回收的意思是年轻代和老年代会同时被回收。  
    &emsp; **<font color = "blue">步骤分2步：全局并发标记（global concurrent marking）、拷贝存活对象（evacuation）。</font>**  
        1. 全局并发标记  
            1. 初始标记
            2. 根区域扫描
            3. 并发标记
            4. 最终标记： **<font color = "blue">去处理剩下的SATB（开始快照）日志缓冲区和所有更新，找出所有未被访问的存活对象，同时安全完成存活数据计算。</font>**   
            5. 清除垃圾


##### 1.3.6.4.4. 三色标记，并发标记阶段
1. 三色：  
    * 黑色：本对象已访问过，而且本对象 引用到 的其他对象 也全部访问过了。  
    * 灰色：本对象已访问过，但是本对象 引用到 的其他对象 尚未全部访问完。全部访问后，会转换为黑色。  
    * 白色：尚未访问过。  
2. 三色标记流程： 1).根对象黑色... **<font color = "clime">如果标记结束后对象仍为白色，意味着已经“找不到”该对象在哪了，不可能会再被重新引用。</font>**  
3. **<font color = "clime">`多标/错标`，本应该回收 但是 没有回收掉的内存，被称之为“浮动垃圾”</font>** ，并不会影响垃圾回收的正确性，只是需要等到下一轮垃圾回收才被清除。  
4. **<font color = "clime">漏标：把本来应该存活的垃圾，标记为了死亡。这就会导致非常严重的错误。</font>**   
	1. 两个必要条件：1). 灰色指向白色的引用消失。2). 黑色重新指向白色；  
  &emsp; 新增对象不算漏标。  
	2. CMS采用增量更新（针对新增的引用，将其记录下来等待遍历）， **<font color = "clime">关注引用的增加（黑色重新指向白色），`把黑色重新标记为灰色`，下次重新扫描属性。</font>** 破坏了条件“黑指向白”。    
    &emsp; `CMS预清理阶段`：（`三色标记法的漏标问题处理`） **<font color = "red">这个阶段是用来</font><font color = "blue">处理</font><font color = "clime">前一个并发标记阶段因为引用关系改变导致没有标记到的存活对象的。如果发现对象的引用发生变化，则JVM会标记堆的这个区域为Dirty Card。那些能够从Dirty Card到达的对象也被标记（标记为存活），当标记做完后，这个Dirty Card区域就会消失。</font>**  
	3. G1采用开始时快照技术SATB， **<font color = "clime">关注引用的删除（灰色指向白色的引用消失），当B->D消失时，要把这个引用推到GC的堆栈，保证D还能被GC扫描到。破坏了条件“灰指向白的引用消失”。</font>** 保存在GC堆栈中的删除引用，会在`最终标记remark阶段处理`。    
	4. 使用SATB会大大减少扫描对象。  

### 1.3.7. JVM调优
#### 1.3.7.1. JVM调优-基础
1. JVM参数：

    |参数|描述|
    |---|---|
    |-Xms|用于在JVM启动时设置初始堆大小|
    |-Xmx|用于设置最大堆大小|
    |-Xmn|设置新生区的大小，剩下的空间用于老年区|
    |-XX：PermGen|用于设置永久区存初始大小|
    |-XX：MaxPermGen|用于设置Perm Gen的最大尺寸|
    |-XX：SurvivorRatio|提供Eden区域的比例|
    |-XX：NewRatio|用于提供老年代/新生代大小的比例，默认值为2|
2. JVM命令行调优工具：  
    * Jps：虚拟机进程状况工具。  
    * Jstack：java线程堆栈跟踪工具。  
    &emsp; **<font color = "clime">生成线程快照的主要目的是定位线程出现长时间停顿的原因，如线程间死锁、死循环、请求外部资源导致的长时间等待等都是导致线程长时间停顿的常见原因。</font>**  
    &emsp; **`线程出现停顿的时候，通过jstack来查看各个线程的调用堆栈，就可以知道没有响应的线程到底在后台做什么事情，或者等待什么。`**  
    * Jmap：java内存映像工具。  
    &emsp; <font color = "red">jmap（JVM Memory Map）命令用于生成heap dump文件。如果不使用这个命令，</font> **<font color = "red">还可以使用-XX:+HeapDumpOnOutOfMemoryError参数来让虚拟机出现OOM的时候自动生成dump文件。</font>**   
    &emsp; jmap -dump:live,format=b,file=path pid。 **<font color = "blue">参数lime表示需要抓取目前在生命周期内的内存对象。</font>**   
    * Jhat：虚拟机堆转储快照分析工具。  
    * Jstat：虚拟机统计信息监视工具。  
    * Jinfo：java配置信息工具。  

#### 1.3.7.2. JVM调优
1. 内存设置  
&emsp; 如何将各分区调整到合适的大小，分析活跃数据的大小是很好的切入点。  
&emsp; **活跃数据的大小是指，应用程序稳定运行时长期存活对象在堆中占用的空间大小，也就是Full GC后堆中老年代占用空间的大小。** 
2. GC调优
    1. <font color = "clime">`GC的优化主要有2个维度，一是频率，二是时长。`</font> **<font color = "clime">如果满足下面的指标，则一般不需要进行GC调优：</font>**    
        * Minor GC执行时间不到50ms;
        * Minor GC执行不频繁，约10秒一次；
        * Full GC执行时间不到1s;
        * Full GC执行频率不算频繁，不低于10分钟1次。
    2. Young GC、Full GC优化策略 参考 1.2.3节。

#### 1.3.7.3. JVM问题排查
1. 快速恢复业务：隔离故障服务器。  
2. FGC过高  
&emsp; **<font color = "clime">`FGC过高可能是内存参数设置不合理，也有可能是代码中某个位置读取数据量较大导致系统内存耗尽。`FGC过高可能导致CPU飚高。</font>**  
&emsp; **<font color = "clime">解决思路（`FGC过高参考CPU飚高`）：FGC过高一般会导致CPU过高，打印线程堆栈信息。查看线程堆栈是用户线程，还是GC线程。如果是GC线程，打印内存快照进行分析（`查看内存溢出`），`进行Full GC优化`。</font>**  
3. CPU飚高  
&emsp; **<font color = "red">CPU过高可能是系统频繁的进行Full GC，导致系统缓慢。</font><font color = "clime">而平常也可能遇到比较耗时的计算，导致CPU过高的情况。</font>**  
&emsp; **<font color = "clime">怎么区分导致CPU过高的原因具体是Full GC次数过多还是代码中有比较耗时的计算？</font>** `如果是Full GC次数过多，那么通过jstack得到的线程信息会是类似于VM Thread之类的线程`；而`如果是代码中有比较耗时的计算，那么得到的就是一个线程的具体堆栈信息。` 

        1. 通过top命令找到CPU消耗最高的进程，并记住进程ID。  
        2. 再次通过top -Hp [进程 ID]找到CPU消耗最高的线程ID，并记住线程ID。  
        3. 通过JDK提供的jstack工具dump线程堆栈信息到指定文件中。具体命令：jstack -l [进程 ID] >jstack.log。  
        4. 由于刚刚的线程ID是十进制的，而堆栈信息中的线程ID是16进制的，因此需要将10进制的转换成16进制的，并用这个线程ID在堆栈中查找。使用printf "%x\n" [十进制数字] ，可以将10进制转换成16进制。  
        5. 通过刚刚转换的16进制数字从堆栈信息里找到对应的线程堆栈。就可以从该堆栈中看出端倪。      
4. **<font color = "blue">★★★CPU高，查看所有进程占用率要远小于100。</font>**
    1. 可能多个线程执行同一方法，每个线程占有不高，但总和比较大。  
    2. 可以使用arthas工具的thread -n -i分析。
5. 内存溢出OOM  
	1. 解决方案： **<font color = "clime">不止一种</font>**
		1. 修改JVM启动参数，直接增加内存。  
		2. `检查错误日志，查看“OutOfMemory”错误前是否有其它异常或错误。`  
		3. `对代码进行走查和分析，找出可能发生内存溢出的位置。` 
		4. `使用内存查看工具动态查看内存快照。` 
    2. 使用内存查看工具动态分析内存快照
        2. 步骤一：保存内存快照（两种方法）： 
            1. 方式一：添加JVM参数，(-XX:+HeapDumpOnOutOfMemoryError)，让JVM遇到OOM异常时能输出堆内信息，可以通过（-XX:+HeapDumpPath）参数设置堆内存溢出快照输出的文件地址。  
            2. 方式二：jmap命令。`注：线上环境不能直接使用jmap命令。找到未进行GC的一个节点，从线上环境摘除。然后再使用jmap命令。`  
            3. 附录： **<font color = "blue">jvm内存快照dump文件太大：</font>** 
                * **<font color = "clime">live参数表示需要抓取目前在生命周期内的内存对象，也就是说GC收不走的对象，然后绝大部分情况下，需要的看的就是这些内存。</font>**   
                * 如果Dump文件太大，可能需要加上-J-Xmx512m这种参数指定最大堆内存，即jhat -J-Xmx512m -port 9998 /tmp/dump.dat。
                * 如果dump文件太大，使用linux下的mat，既Memory Analyzer Tools。   
        3. 步骤二：使用内存查看工具分析堆dump文件
            1. 步骤一：查看对象占比；
            2. 步骤二：查看大对象的引用链。  

#### 1.3.7.4. Arthas工具


## 1.4. 多线程和并发


### 1.4.1. 线程Thread
1. 创建线程的方式：Thread、Runnable、Callable、线程池相关（Future, ThreadPool, `@Async`）...  
2. 线程状态 
3. thread.yield()，线程让步     
&emsp; yield会使当前线程让出CPU执行时间片，与其他线程一起重新竞争CPU时间片。  
4. thread.join()，线程加入  
&emsp; 把指定的线程加入到当前线程，可以将两个交替执行的线程合并为顺序执行的线程。比如在线程B中调用了线程A的Join()方法，直到线程A执行完毕后，才会继续执行线程B。  
5. thread.interrupt()，线程中断  
    &emsp; **<font color = "red">线程在不同状态下对于中断所产生的反应：</font>**    
    * NEW和TERMINATED对于中断操作几乎是屏蔽的；  
    * RUNNABLE和BLOCKED类似， **<font color = "cclime">对于中断操作只是设置中断标志位并没有强制终止线程，对于线程的终止权利依然在程序手中；</font>**  
    * WAITING/TIMED_WAITING状态下的线程对于中断操作是敏感的，它们会抛出异常并清空中断标志位。  

#### 1.4.1.1. 线程状态详解
1. 通用的线程周期。操作系统层面有5个状态，分别是：New（新建）、Runnable（就绪）、Running（运行）、Blocked（阻塞）、Dead（死亡）。  
2. Java线程状态均来自Thread类下的State这一内部枚举类中所定义的状态：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/thread-2.png)  
&emsp; 线程状态切换图示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/thread-5.png) 
&emsp; ⚠️⚠️⚠️`对象` `执行动作` 形成`线程`。`影响线程状态的相关java类：Object类、Synchronized关键字、Thread类。`  
1. 新建状态（NEW）：  
    1. 一个尚未启动的线程处于这一状态。用new语句创建的线程处于新建状态，此时它和其他Java对象一样，仅仅在堆区中被分配了内存，并初始化其成员变量的值。  
    2. 操作  
        * new Thread()
2. 就绪状态（Runnable）：  
    1. 当一个线程对象创建后，其他线程调用它的start()方法，该线程就进入就绪状态，Java虚拟机会为它创建方法调用栈和程序计数器。处于这个状态的线程位于可运行池中，等待获得CPU的使用权。<!-- Runnable (可运行/运行状态，等待CPU的调度)(要注意：即使是正在运行的线程，状态也是Runnable，而不是Running) -->  
    2. 操作  
        * 调用了thread.start()启动线程；
        * 处于阻塞的线程：obj.notify()唤醒线程； obj.notifyAll()唤醒线程； 
        * 处于等待的线程：obj.wait(time)，thread.join(time)等待时间time耗尽。
        * 被synchronized标记的代码，获取到同步监视器。  
3. **<font color = "red">阻塞状态（BLOCKED）：</font>**  
    1. **<font color = "clime">阻塞状态是指线程因为某些原因`放弃CPU`，暂时停止运行。</font>** 当线程处于阻塞状态时，Java虚拟机不会给线程分配CPU。直到线程重新进入就绪状态(获取监视器锁)，它才有机会转到运行状态。  
    2. 操作  
        * **等待阻塞(o.wait->等待对列)：运行的线程执行wait()方法，JVM会把该线程放入等待池中。(wait会释放持有的锁)**
        * **同步阻塞(lock->锁池)：运行的线程在获取对象的同步锁时，若该同步锁被别的线程占用，则JVM会把该线程放入锁池(lock pool)中。**
        * **其他阻塞状态(sleep/join)：当前线程执行了sleep()方法，或者调用了其他线程的join()方法，或者发出了I/O请求时，就会进入这个状态。**
4. **<font color = "red">等待状态（WAITING）：</font>**  
    1. **<font color = "clime">一个正在无限期等待另一个线程执行一个特别的动作的线程处于这一状态。</font>**  
    2. 操作  
        * obj.wait() 释放同步监视器obj，并进入阻塞状态。  
        * threadA中调用threadB.join()，threadA将Waiting，直到threadB终止。
    3. 阻塞和等待的区别：  
5. <font color = "red">计时等待（TIMED_WAITING）：</font>  
    1. 一个正在限时等待另一个线程执行一个动作的线程处于这一状态。  
    2. 操作
        * obj.wait(time)
        * thread.sleep(time)； threadA中调用threadB.join(time)
6. 终止状态（TERMINATED）：  
    &emsp; 一个已经退出的线程处于这一状态。线程会以下面三种方式结束，结束后就是死亡状态。
    * 正常结束：run()或 call()方法执行完成，线程正常结束。
    * 异常结束：线程抛出一个未捕获的Exception或Error。
    * 调用stop：直接调用该线程的stop()方法来结束该线程—该方法通常容易导致死锁，不推荐使用。
7. 注意：由于wait()/wait(time)导致线程处于Waiting/TimedWaiting状态，当线程被notify()/notifyAll()/wait等待时间到之后，如果没有获取到同步监视器。会直接进入Blocked阻塞状态。  
8. 线程状态切换示意图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/thread-6.png) 


### 1.4.2. 线程池-多线程
#### 1.4.2.1. 线程池框架
1. **线程池通过线程复用机制，并对线程进行统一管理，** 具有以下优点：  
    * 降低系统资源消耗。通过复用已存在的线程，降低线程创建和销毁造成的消耗；  
    * 提高响应速度。当有任务到达时，无需等待新线程的创建便能立即执行；  
    * 提高线程的可管理性。线程是稀缺资源，如果无限制的创建，不仅会消耗大量系统资源，还会降低系统的稳定性，使用线程池可以进行对线程进行统一的分配、调优和监控。  
2. 线程池框架Executor：  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-21.png)   
    &emsp; Executor：所有线程池的接口。  
    &emsp; ExecutorService：扩展了Executor接口。添加了一些用来管理执行器生命周期和任务生命周期的方法。  
    &emsp; ThreadPoolExecutor（创建线程池方式一）：线程池的具体实现类。  
    &emsp; Executors（创建线程池方式二）：提供了一系列静态的工厂方法用于创建线程池，返回的线程池都实现了ExecutorService 接口。  
    
    &emsp; Executor框架由三个部分组成：  

    * 工作任务：Runnable/Callable 接口
        * 工作任务就是Runnable/Callable接口的实现，可以被线程池执行
    * **<font color = "red">执行机制（创建线程池的分类）：</font>** Executor接口、ExecutorService接口、ScheduledExecutorService接口
        * ThreadPoolExecutor 是最核心的线程池实现，用来执行被提交的任务。
        * ScheduledThreadPoolExecutor 是任务调度的线程池实现，可以在给定的延迟后运行命令，或者定期执行命令(它比Timer更灵活)
        * ForkJoinPool是一个并发执行框架
    * 异步计算的结果：Future接口
        * 实现Future接口的FutureTask类，代表异步计算的结果
3. 线程池执行，ExecutorService的API：execute()，提交不需要返回值的任务；`submit()，提交需要返回值的任务，返回值类型是Future`。   
4. **<font color = "clime">Executors返回线程池对象的弊端如下：</font>**  
	* SingleThreadExecutor（单线程）和FixedThreadPool（定长线程池，可控制线程最大并发数）：允许请求的队列长度为Integer.MAX_VALUE，可能堆积大量的请求，从而导致OOM。  
	* CachedThreadPool和ScheduledThreadPool：允许创建的线程数量为Integer.MAX_VALUE，可能会创建大量线程，从而导致OOM。   

#### 1.4.2.2. ThreadPoolExecutor详解
1. 理解构造函数中参数：核心线程数大小、最大线程数大小、空闲线程（超出corePoolSize的线程）的生存时间、参数keepAliveTime的单位、任务阻塞队列、创建线程的工厂（可以通过这个工厂来创建有业务意义的线程名字）。  
    * [阻塞队列](/docs/java/concurrent/BlockingQueue.md)，线程池所使用的缓冲队列，常用的是：SynchronousQueue、ArrayBlockingQueue、LinkedBlockingQueue。   
    * 拒绝策略，默认AbortPolicy（拒绝任务，抛异常）， **<font color = "clime">可以选用CallerRunsPolicy（任务队列满时，不进入线程池，由主线程执行）。</font>**  
2. 线程池中核心方法调用链路：  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-17.png)  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-14.png)  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-20.png)  
    &emsp; 线程运行流程：查看execute方法。  
    &emsp; <font color = "clime">线程池创建时`没有设置成预启动加载`，首发线程数为0。</font><font color = "red">任务队列是作为参数传进来的。即使队列里面有任务，线程池也不会马上执行它们，而是创建线程。</font>当一个线程完成任务时，它会从队列中取下一个任务来执行。当调用execute()方法添加一个任务时，线程池会做如下判断：  
    1. 如果当前工作线程总数小于corePoolSize，则直接创建核心线程执行任务（任务实例会传入直接用于构造工作线程实例）。  
    2. 如果当前工作线程总数大于等于corePoolSize，判断线程池是否处于运行中状态，同时尝试用非阻塞方法向任务队列放入任务，这里会二次检查线程池运行状态，如果当前工作线程数量为0，则创建一个非核心线程并且传入的任务对象为null。  
    3. 如果向任务队列投放任务失败（任务队列已经满了），则会尝试创建非核心线程传入任务实例执行。  
    4. 如果创建非核心线程失败，此时需要拒绝执行任务，调用拒绝策略处理任务。  
3. 线程复用机制：    
&emsp; **线程池将线程和任务进行解耦，线程是线程，任务是任务，摆脱了之前通过Thread创建线程时的一个线程必须对应一个任务的限制。**  
&emsp; **<font color = "red">在线程池中，同一个线程可以从阻塞队列中不断获取新任务来执行，其核心原理在于线程池对Thread进行了封装（内部类Worker），并不是每次执行任务都会调用Thread.start() 来创建新线程，而是让每个线程去执行一个“循环任务”，在这个“循环任务”中不停的检查是否有任务需要被执行。</font>** 如果有则直接执行，也就是调用任务中的run方法，将run方法当成一个普通的方法执行，通过这种方式将只使用固定的线程就将所有任务的run方法串联起来。  
&emsp; 源码解析：`runWorker()方法中，有任务时，while (task != null || (task = getTask()) != null) 循环获取；没有任务时，清除空闲线程。`  
4. 线程池保证核心线程不被销毁？  
    &emsp; `ThreadPoolExecutor回收线程都是等while死循环里getTask()获取不到任务，返回null时，调用processWorkerExit方法从Set集合中remove掉线程。`  
    1. getTask()返回null又分为2两种场景：  
        1. 线程正常执行完任务，`并且已经等到超过keepAliveTime时间，大于核心线程数，那么会返回null`，结束外层的runWorker中的while循环。
        2. 当调用shutdown()方法，会将线程池状态置为shutdown，并且需要等待正在执行的任务执行完，阻塞队列中的任务执行完才能返回null。
    2. `getTask()不返回null的情况有获取到任务，或获取不到任务，但线程数小于等于核心线程数。`  

#### 1.4.2.3. 线程池的正确使用
1. **<font color = "clime">线程池设置：</font>**   
    1. `使用自定义的线程池。`共享的问题在于会干扰，如果有一些异步操作的平均耗时是1秒，另外一些是100秒，这些操作放在一起共享一个线程池很可能会出现相互影响甚至饿死的问题。`建议根据异步业务类型，合理设置隔离的线程池。`  
    2. `确定线程池的大小（CPU可同时处理线程数量大部分是CPU核数的两倍）`  
        1. 线程数设置，`建议核心线程数core与最大线程数max一致`
            * 如果是CPU密集型应用（多线程处理复杂算法），则线程池大小设置为N+1。
            * 如果是IO密集型（网络IO/磁盘IO）应用（多线程用于数据库数据交互、文件上传下载、网络数据传输等），则线程池大小设置为2N。
            * 如果是混合型，将任务分为CPU密集型和IO密集型，然后分别使用不同的线程池去处理，从而使每个线程池可以根据各自的工作负载来调整。  
        2. 阻塞队列设置  
        &emsp; `线程池的任务队列本来起缓冲作用，`但是如果设置的不合理会导致线程池无法扩容至max，这样无法发挥多线程的能力，导致一些服务响应变慢。队列长度要看具体使用场景，取决服务端处理能力以及客户端能容忍的超时时间等。队列长度要根据使用场景设置一个上限值，如果响应时间要求较高的系统可以设置为0。  
        &emsp; `队列大小200或500-1000。`  
    3. `线程池的优雅关闭：`处于SHUTDOWN的状态下的线程池依旧可以调用shutdownNow。所以可以结合shutdown，shutdownNow，awaitTermination，更加优雅关闭线程池。  
2. **<font color = "clime">线程池使用：</font>**    
    1. `线程池未处理异常：`
        1. 线程遇到未处理的异常就结束了。ThreadPoolExecutor中将异常传递给afterExecute()方法，而afterExecute()没有做任何处理。这种处理方式能够保证提交的任务抛出了异常不会影响其他任务的执行，同时也不会对用来执行该任务的线程产生任何影响。然而afterExecute()没有做任何处理，所以如果任务抛出了异常，也无法立刻感知到。即使感知到了，也无法查看异常信息。    
        2. `当线程池中线程频繁出现未捕获的异常，那线程的复用率就大大降低了，需要不断地创建新线程。`  
    2. `线程池中线程中异常尽量手动捕获。`  
3. **<font color = "clime">线程池的监控：</font>**  
&emsp; 通过重写线程池的beforeExecute、afterExecute和shutdown等方式就可以实现对线程的监控。  
4. @Async方法没有执行的问题分析：  
&emsp; @Async异步方法默认使用Spring创建ThreadPoolTaskExecutor(参考TaskExecutionAutoConfiguration)，其中默认核心线程数为8，默认最大队列和默认最大线程数都是Integer.MAX_VALUE，队列使用LinkedBlockingQueue，容量是：Integet.MAX_VALUE，空闲线程保留时间：60s，线程池拒绝策略：AbortPolicy。创建新线程的条件是队列填满时，而这样的配置队列永远不会填满，如果有@Async注解标注的方法长期占用线程(比如HTTP长连接等待获取结果)，在核心8个线程数占用满了之后，新的调用就会进入队列，外部表现为没有执行。  


#### 1.4.2.4. ForkJoinPool详解
1. <font color = "clime">ForkJoinPool的两大核心是 分而治之和工作窃取 算法。</font>  
2. 分而治之：<font color = "red">ForkJoinPool的计算方式是大任务拆中任务，中任务拆小任务，最后再汇总。</font>  
3. 工作窃取算法  
&emsp; <font color = "clime">每个工作线程都有自己的工作队列WorkQueue。这是一个双端队列，它是线程私有的。</font>双端队列的操作：push、pop、poll。push/pop只能被队列的所有者线程调用，而poll是由其它线程窃取任务时调用的。  
    1. ForkJoinTask中fork的子任务，将放入运行该任务的工作线程的队头，工作线程将以LIFO的顺序来处理工作队列中的任务；  
    2. **<font color = "clime">`为了最大化地利用CPU，空闲的线程将随机从其它线程的队列中“窃取”任务来执行。从工作队列的尾部窃取任务，以减少竞争；`</font>**  
    3. **<font color = "clime">`当只剩下最后一个任务时，还是会存在竞争，是通过CAS来实现的；`</font>**    


#### 1.4.2.5. Future相关
1. **Future是一个接口，它可以对具体的Runnable或者Callable任务进行取消、判断任务是否已取消、查询任务是否完成、获取任务结果。**  
2. JDK1.5为Future接口提供了一个实现类FutureTask，表示一个可以取消的异步运算。它有启动和取消运算、查询运算是否完成和取回运算结果等方法。  


#### 1.4.2.6. ~~CompletionService~~
&emsp; CompletionService 提供了异步任务的执行与结果的封装，轻松实现多线程任务， **<font color = "clime">并方便的集中处理上述任务的结果(且任务最先完成的先返回)。</font>**  
&emsp; 内部通过阻塞队列+FutureTask，实现了任务先完成可优先获取到，即结果按照完成先后顺序排序。  

#### 1.4.2.7. ~~CompletableFuture~~
&emsp; CompletableFuture 可以很方便的实现异步任务的封装 **<font color = "clime">并实现结果的联合等一系列操作，</font>** 轻松实现 任务的并行。  

* thenCombine：结合两个CompletionStage的结果，进行转化后返回。  
* applyToEither：两个CompletionStage，谁计算的快，就用那个CompletionStage的结果进行下一步的处理。  
* ...


### 1.4.3. 并发编程
#### 1.4.3.1. 并发编程原理
##### 1.4.3.1.1. ~~CPU缓存及JMM~~
1. JMM
    1. JMM内存划分：线程对变量的所有操作都必须在工作内存进行，而不能直接读写主内存中的变量。    
    2. 单个线程操作时，8种内存间交换操作指令。  
    3. 线程之间的通信和同步。线程之间的通信过程：线程对变量的操作（读取赋值等）必须在工作内存中进行，首先要将变量从主内存拷贝到自己的工作内存空间，然后对变量进行操作，操作完成后再将变量写回主内存，不能直接操作主内存中的变量，</font>各个线程中的工作内存中存储着主内存中的变量副本拷贝，<font color = "red">因此不同的线程间无法访问对方的工作内存，线程间的通信（传值）必须通过主内存来完成。</font>    


##### 1.4.3.1.2. 并发安全问题产生原因
1. **并发安全的3个问题：**  

    * 原子性：线程切换带来的原子性问题；（[Volatile](/docs/java/concurrent/Volatile.md)不保证原子性）
    * 可见性：缓存不能及时刷新导致的可见性问题；
    * 有序性：编译优化带来的有序性问题  

&emsp; **<font color = "clime">`【缓存不能及时刷新】/可见性 (【内存系统重排序】)` 和`【编译器优化】/有序性` 都是`重排序`的一种。</font>**   

2. **~~重排序：~~**  
    * **<font color = "blue">重排序分类：1). 编译器优化；2). 指令重排序(CPU优化行为)；3). 内存系统重排序：内存系统没有重排序，但是由于有缓存的存在，使得程序整体上会表现出乱序的行为。</font>**     
        * 对于编译器，JMM的编译器重排序规则会禁止特定类型的编译器重排序（不是所有的编译器重排序都要禁止）。  
        * 对于处理器重排序，JMM的处理器重排序规则会要求Java编译器在生成指令序列时，插入特定类型的内存屏障指令， **<font color = "clime">通过内存屏障指令来禁止特定类型的处理器重排序</font>** （不是所有的处理器重排序都要禁止）。 

    * 重排序遵守的规则：重排序遵守数据依赖性、重排序遵守as-if-serial语义。  
    * 重排序对多线程的影响

##### 1.4.3.1.3. 并发安全解决底层
1. 缓存一致性协议  
    1. 怎么解决缓存一致性问题呢？使用总线锁或缓存锁。  
        * 总线锁：cpu从主内存读取数据到高速缓存，会在总线对这个数据加锁，这样其他cpu无法去读或写这个数据，直到这个cpu使用完数据释放锁之后其他cpu才能读取该数据。  
        * 缓存锁：只要保证多个CPU缓存的同一份数据是一致的就可以了，基于缓存一致性协议来实现。  
    2. MESI缓存一致性协议  
        1. 缓存一致性协议有很多种，MESI（Modified-Exclusive-Shared-Invalid）协议其实是目前使用很广泛的缓存一致性协议，x86处理器所使用的缓存一致性协议就是基于MESI的。  
        2. 其他cpu通过 总线嗅探机制 可以感知到数据的变化从而将自己缓存里的数据失效。  
        &emsp; 总线嗅探， **<font color = "red">每个CPU不断嗅探总线上传播的数据来检查自己缓存值是否过期了，如果处理器发现自己的缓存行对应的内存地址被修改，就会将当前处理器的缓存行设置为无效状态，当处理器对这个数据进行修改操作的时候，会重新从内存中把数据读取到处理器缓存中。</font>**    
        2. 总线嗅探会带来总线风暴。  
2. 内存屏障：    
    &emsp; Java中如何保证底层操作的有序性和可见性？可以通过内存屏障。`内存屏障，禁止处理器重排序，保障缓存一致性。`  
    &emsp; `内存屏障的作用：（~~原子性~~、可见性、有序性）`  
    1. `（保障可见性）它会强制将对缓存的修改操作立即写入主存；` 如果是写操作，会触发总线嗅探机制(MESI)，会导致其他CPU中对应的缓存行无效，也有 [伪共享问题](/docs/java/concurrent/PseudoSharing.md)。   
    2. `（保障有序性）阻止屏障两侧的指令重排序。`   
3. JMM中的happens-before原则：  
    &emsp; JSR-133内存模型 **<font color = "red">使用`happens-before`的概念来阐述操作之间的`内存可见性`。在JMM中，如果一个操作执行的结果需要对另一个操作可见，那么这两个操作之间必须要存在happens-before关系。</font>** 这里提到的两个操作既可以是在一个线程之内，也可以是在不同线程之间。  
    &emsp; happens-before关系的定义如下：

    * 如果一个操作happens-before另一个操作，那么第一个操作的执行结果将对第二个操作可见，而且第一个操作的执行顺序排在第二个操作之前。  
    * 两个操作之间存在happens-before关系，并不意味着Java平台的具体实现必须要按照happens-before关系指定的顺序来执行。如果重排序之后的执行结果，与按happens-before关系来执行的结果一致，那么JMM也允许这样的重排序。  

&emsp; **<font color = "clime">happens-before原则有管理锁定（lock）规则、volatile变量规则、线程启动规则（Thread.start()）、线程终止规则（Thread.join()）、线程中断规则（Thread.interrupt()）...</font>**  
&emsp; volatile变量规则就是使用内存屏障保证线程可见性。  

##### 1.4.3.1.4. 伪共享问题
1. CPU具有多级缓存，越接近CPU的缓存越小也越快；CPU缓存中的数据是以缓存行为单位处理的；CPU缓存行（通常是64字节）能带来免费加载数据的好处，所以处理数组性能非常高。  
2. **CPU缓存行也带来了弊端，多线程处理不相干的变量时会相互影响，也就是伪共享。**  
&emsp; 设想如果有个long类型的变量a，它不是数组的一部分，而是一个单独的变量，并且还有另外一个long类型的变量b紧挨着它，那么当加载a的时候将免费加载b。  
&emsp; 看起来似乎没有什么毛病，但是如果一个CPU核心的线程在对a进行修改，另一个CPU核心的线程却在对b进行读取。  
3. 避免伪共享的主要思路就是让不相干的变量不要出现在同一个缓存行中；一是在两个long类型的变量之间再加7个long类型(字节填充)；二是创建自己的long类型，而不是用原生的；三是使用java8提供的注解。  
&emsp; 高性能原子类[LongAdder](/docs/java/concurrent/LongAdder.md)可以解决类伪共享问题。   

#### 1.4.3.2. 线程安全解决
##### 1.4.3.2.1. 线程安全解决方案
1. 线程安全解决方案
	1. 阻塞/互斥同步（悲观锁）
	2. 非阻塞同步（乐观锁，CAS） 
	3. 无同步方案（线程封闭）
		* 栈封闭（类变量变局部变量）
		* 线程本地存储（Thread Local Storage）
	4. 不可变对象
2. Java并发原语  
	Java内存模型，除了定义了一套规范，还提供了一系列原语，封装了底层实现后，供开发者直接使用。  
	* 原子性可以通过synchronized和Lock来实现。  
	* 可见性可以通过Volatile、synchronized、final来实现。  
	* 有序性可以通过synchronized或者Lock、volatile来实现。  

##### 1.4.3.2.2. Synchronized
###### 1.4.3.2.2.1. Synchronized介绍


###### 1.4.3.2.2.2. Synchronized使用
1. 对象和方法
    * 类和对象
        * xxx.Class
        * 类名 对象名
        * 实例化：new 类名();
    * 方法
        * 普通方法
        * 静态/类 方法
1. synchronized可以修饰代码块或者方法：  
    ```java
    synchronized (lock){
        //被保护的代码块
    }
    public synchronized void method() {
        被保护的方法
    }
    ```
2. 类锁和对象锁  
    1. `类锁：当Synchronized修饰静态方法或Synchronized修饰代码块传入某个class对象（Synchronized (XXXX.class)）时被称为类锁。`
    2. `对象锁：当Synchronized修饰非静态方法或Synchronized修饰代码块时传入非class对象（Synchronized (this)）时被称为对象锁。`
3. String锁：由于在JVM中具有String常量池缓存的功能，因此相同字面量是同一个锁。  


##### 1.4.3.2.3. Synchronized使用是否安全
&emsp; 共有 `类锁 + 对象锁 + 类锁 * 对象锁`种情况。    
1. 类锁
2. 对象锁
3. 类锁和对象锁
4. 不安全场景

###### 1.4.3.2.3.1. Synchronized底层原理
1. Synchronized底层实现：`查看Synchronized的字节码。`  
    * Synchronized方法同步：依靠的是方法修饰符上的ACC_Synchronized实现。  
    * Synchronized代码块同步：使用monitorenter和monitorexit指令实现。   
每一个对象都会和一个监视器monitor关联。监视器被占用时会被锁住，其他线程无法来获取该monitor。   
线程执行monitorenter指令时尝试获取对象的monitor的所有权，当monitor被占用时就会处于锁定状态。  
2. **<font color = "clime">Java对象头的MarkWord中除了存储锁状态标记外，还存有ptr_to_heavyweight_monitor（也称为管程或监视器锁）的起始地址，每个对象都存在着一个monitor与之关联。</font>**  
3. C++    
&emsp; **<font color = "clime">在Java虚拟机（HotSpot）中，Monitor是基于C++实现的，在虚拟机的ObjectMonitor.hpp文件中。</font><font color = "blue">monitor运行的机制过程如下：(_EntryList队列、_Owner区域、_WaitSet队列)</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-55.png)  
    * `想要获取monitor的线程，首先会进入_EntryList队列。`  
    * `当某个线程获取到对象的monitor后，进入Owner区域，设置为当前线程，`同时计数器count加1。  
    * **如果线程调用了wait()方法，则会进入WaitSet队列。** 它会释放monitor锁，即将owner赋值为null，count自减1，进入WaitSet队列阻塞等待。  
    * 如果其他线程调用 notify() / notifyAll()，会唤醒WaitSet中的某个线程，该线程再次尝试获取monitor锁，成功即进入Owner区域。  
    * 同步方法执行完毕了，线程退出临界区，会将monitor的owner设为null，并释放监视锁。  
4. linux互斥锁mutex（内核态）  
&emsp; <font color = "clime">重量级锁是依赖对象内部的monitor锁来实现的，而monitor又依赖操作系统的MutexLock(互斥锁)来实现的，所以重量级锁也称为互斥锁。</font>  
&emsp; **<font color = "clime">为什么说重量级线程开销很大？</font>**  
&emsp; 当系统检查到锁是重量级锁之后，会把等待想要获得锁的线程进行阻塞，`被阻塞的线程不会消耗cpu`。 **<font color = "clime">`但是阻塞或者唤醒一个线程时，都需要操作系统来帮忙，这就需要从用户态转换到内核态(向内核申请)，而转换状态是需要消耗很多时间的，有可能比用户执行代码的时间还要长。`</font>**  

###### 1.4.3.2.3.2. Synchronized优化
1. **<font color = "clime">锁降级：</font>** <font color = "red">Hotspot在1.8开始有了锁降级。在STW期间JVM进入安全点时，如果发现有闲置的monitor（重量级锁对象），会进行锁降级。</font>   
2. 锁升级  
    &emsp; 锁主要存在四种状态，依次是：无锁状态（普通对象）、偏向锁状态、轻量级锁状态、重量级锁状态，它们会随着竞争的激烈而逐渐升级。锁升级流程如下：   
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-79.png)   
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-80.png)   
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-86.png)   
	1. 偏向锁：  
        ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-81.png)   
        1.  **<font color = "bule">偏向锁状态</font>**  
            * **<font color = "clime">匿名偏向(Anonymously biased)</font>** 。在此状态下thread pointer为NULL(0)，意味着还没有线程偏向于这个锁对象。第一个试图获取该锁的线程将会面临这个情况，使用原子CAS指令可将该锁对象绑定于当前线程。这是允许偏向锁的类对象的初始状态。
            * **<font color = "clime">可重偏向(Rebiasable)</font>** 。在此状态下，偏向锁的epoch字段是无效的（与锁对象对应的class的mark_prototype的epoch值不匹配）。下一个试图获取锁对象的线程将会面临这个情况，使用原子CAS指令可将该锁对象绑定于当前线程。**在批量重偏向的操作中，未被持有的锁对象都被置于这个状态，以便允许被快速重偏向。**
            * **<font color = "clime">已偏向(Biased)</font>** 。这种状态下，thread pointer非空，且epoch为有效值——意味着其他线程正在持有这个锁对象。
        2. 偏向锁获取： 
            1. 判断是偏向锁时，检查对象头Mark Word中记录的`Thread Id`是否是当前线程ID。  
            2. 如果对象头Mark Word中Thread Id不是当前线程ID，则`进行CAS操作，企图将当前线程ID替换进Mark Word`。如果当前对象锁状态处于匿名偏向锁状态（可偏向未锁定），则会替换成功（ **<font color = "clime">将Mark Word中的Thread id由匿名0改成当前线程ID，</font>** 在当前线程栈中找到内存地址最高的可用Lock Record，将线程ID存入）。  
            3. 如果对象锁已经被其他线程占用，则会替换失败，开始进行偏向锁撤销，`这也是偏向锁的特点，一旦出现线程竞争，就会撤销偏向锁；` 
        3. 偏向锁撤销： 
            1. 等到安全点，检查持有偏向锁的`线程是否还存活`。如果线程还存活，则检查线程是否在执行同步代码块中的代码，如果是，则升级为轻量级锁，进行CAS竞争锁； 
            2. `如果持有偏向锁的线程未存活，或者持有偏向锁的线程未在执行同步代码块中的代码`， **<font color = "red">则进行校验`是否允许重偏向`。</font>**   
                1. **<font color = "clime">如果不允许重偏向，则撤销偏向锁，将Mark Word设置为无锁状态（未锁定不可偏向状态），然后升级为轻量级锁，进行CAS竞争锁；</font><font color = "blue">(偏向锁被重置为无锁状态，这种策略是为了提高获得锁和释放锁的效率。)</font>**     
                2. 如果允许重偏向，设置为匿名偏向锁状态，CAS将偏向锁重新指向线程A（在对象头和线程栈帧的锁记录中存储当前线程ID）； 
            3. 唤醒暂停的线程，从安全点继续执行代码。 
	2. 轻量级锁：
		1. 偏向锁升级为轻量级锁之后，对象的Markword也会进行相应的的变化。   
            1. 线程在自己的栈桢中创建锁记录LockRecord。
            2. 将锁对象的对象头中的MarkWord复制到线程刚刚创建的锁记录中。
            3. 将锁记录中的Owner指针指向锁对象。
            4. 将锁对象的对象头的MarkWord替换为指向锁记录的指针。
		2. 自旋锁：轻量级锁在加锁过程中，用到了自旋锁。自旋锁分为固定次数自旋锁（在JDK 1.6之前，自旋次数默认是10次）和自适应自旋锁。
		3. 新线程获取轻量级锁
			1. 获取轻量锁过程当中会在当前线程的虚拟机栈中创建一个Lock Record的内存区域去存储获取锁的记录DisplacedMarkWord。
			2. 然后使用CAS操作将锁对象的Mark Word更新成指向刚刚创建的Lock Record的内存区域DisplacedMarkWord的地址。  
		4. 已经获取轻量级锁的线程的解锁： **<font color = "red">轻量级锁的锁释放逻辑其实就是获得锁的逆向逻辑，通过CAS操作把线程栈帧中的LockRecord替换回到锁对象的MarkWord中。</font>** 
    3. 重量级锁  
    &emsp; **<font color = "clime">为什么有了自旋锁还需要重量级锁？</font>**  
    &emsp; 自旋是消耗CPU资源的，如果锁的时间长，或者自旋线程多，CPU会被大量消耗；重量级锁有等待队列，所有拿不到锁的线程进入等待队列，不需要消耗CPU资源。  
    &emsp; 偏向锁、自旋锁都是用户空间完成。重量级锁是需要向内核申请。  

        内置锁在Java中被抽象为监视器锁（monitor）。在JDK 1.6之前，监视器锁可以认为直接对应底层操作系统中的互斥量（mutex）。这种同步方式的成本非常高，包括系统调用引起的内核态与用户态切换、线程阻塞造成的线程切换等。因此，后来称这种锁为“重量级锁”。
  

##### 1.4.3.2.4. Volatile
1. **<font color = "clime">Volatile的特性：</font>**  
    1. 不支持原子性。<font color = "red">它只对Volatile变量的单次读/写具有原子性；</font><font color = "clime">但是对于类似i++这样的复合操作不能保证原子性。</font>    
    2. 实现了可见性。 **Volatile提供happens-before的保证，使变量在多个线程间可见。**  
    3. <font color = "red">实现了有序性，禁止进行指令重排序。</font>  
2. `Volatile底层原理（happens-before中Volatile的特殊规则）：`查看Volatile的汇编代码。    
    * **<font color = "clime">在Volatile写前插入写-写[屏障](/docs/java/concurrent/ConcurrencySolve.md)（禁止上面的普通写与下面的Volatile写重排序），在Volatile写后插入写-读屏障（禁止上面的Volatile写与下面可能有的Volatile读/写重排序）。</font>**  
    * **<font color = "clime">在Volatile读后插入读-读屏障（禁止下面的普通读操作与上面的Volatile读重排序）、读-写屏障（禁止下面所有的普通写操作和上面Volatile读重排序）。</font>**  
3. Volatile为什么不安全（不保证原子性，线程切换）？  
&emsp; 两个线程执行i++（i++的过程可以分为三步，首先获取i的值，其次对i的值进行加1，最后将得到的新值写回到缓存中），线程1获取i值后被挂起，线程2执行...  
4. volatile使用场景：  
    &emsp; 关键字Volatile用于多线程环境下的单次操作（单次读或者单次写）。即Volatile主要使用的场合是在多个线程中可以感知实例变量被更改了，并且可以获得最新的值使用，也就是用多线程读取共享变量时可以获得最新值使用。  
    1. 全局状态标志。
    2. DCL详解：  
        1. 为什么两次判断？线程1调用第一个if(singleton==null)，可能会被挂起。  
        2. 为什么要加volatile关键字？  
        &emsp; singleton = new Singleton()非原子性操作，包含3个步骤：分配内存 ---> 初始化对象 ---> 将singleton对象指向分配的内存空间(这步一旦执行了，那singleton对象就不等于null了)。  
        &emsp; **<font color = "clime">因为指令重排序，可能编程1->3->2。如果是这种顺序，会导致别的线程拿到半成品的实例。</font>**  

##### 1.4.3.2.5. ThreadLocal
&emsp; ThreadLocal的作用是每一个线程创建一个副本。  

1. 在进行对象跨层次传递的时候，使用ThreadLocal可以避免多次传递，打破层次间的束缚。   
2. 线程间层次隔离。  
3. 进行事务操作，用于存储线程事务信息。  
4. 数据库连接，Session会话管理。  

###### 1.4.3.2.5.1. ThreadLocal原理
1. ThreadLocal源码/内存模型：  
    1. **<font color = "red">ThreadLocal的#set()、#getMap()方法：线程调用threadLocal对象的set(Object value)方法时，数据并不是存储在ThreadLocal对象中，</font><font color = "clime">而是将值存储在每个Thread实例的threadLocals属性中。</font>** 即，当前线程调用ThreadLocal类的set或get方法时，实际上调用的是ThreadLocalMap类对应的 get()、set()方法。  
    &emsp; ~~Thread ---> ThreadLocal.ThreadLocalMap~~
    2. **<font color = "clime">ThreadLocal.ThreadLocalMap，</font>Map中`Key是一个ThreadLocal实例，Value是设置的值。`ThreadLocalMap结构中Entry继承WeakReference，所以Entry对应key的引用(ThreadLocal实例)是一个弱引用，Entry对Value的引用是强引用。  
    &emsp; <font color = "clime">Entry的作用即是：为其属主线程建立起一个ThreadLocal实例与一个线程持有对象之间的对应关系。</font>** 一个线程可能有多个ThreadLocal实例，编码中定义多个ThreadLocal实例，即存在多个Entry的情况。    
2. ThreadLocal是如何实现线程隔离的？   
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-85.png)  
    &emsp; ThreadLocal之所以能达到变量的线程隔离，其实就是每个线程都有一个自己的ThreadLocalMap对象来存储同一个threadLocal实例set的值，而取值的时候也是根据同一个threadLocal实例去自己的ThreadLocalMap里面找，自然就互不影响了，从而达到线程隔离的目的！  
3. **ThreadLocal内存泄露：**  
    &emsp; ThreadLocalMap使用ThreadLocal的弱引用作为key，<font color = "red">如果一个ThreadLocal不存在外部强引用时，Key(ThreadLocal实例)会被GC回收，这样就会导致ThreadLocalMap中key为null，而value还存在着强引用，只有thead线程退出以后，value的强引用链条才会断掉。</font>  
    &emsp; **<font color = "clime">但如果当前线程迟迟不结束的话，这些key为null的Entry的value就会一直存在一条强引用链：Thread Ref -> Thread -> ThreaLocalMap -> Entry -> value。永远无法回收，造成内存泄漏。</font>**  
    &emsp; 解决方案：`调用remove()方法`
4. **ThreadLocalMap的key被回收后，如何获取值？**  
    &emsp; ThreadLocal#get() ---> setInitialValue() ---> ThreadLocalMap.set(this, value); 。  
    &emsp; 通过nextIndex()不断获取table上的槽位，直到遇到第一个为null的地方，此处也将是存放具体entry的位置，在线性探测法的不断冲突中，如果遇到非空entry中的key为null，可以表明key的弱引用已经被回收，但是由于线程仍未结束生命周期被回收，而导致该entry仍未从table中被回收，那么则会在这里尝试通过replaceStaleEntry()方法，将null key的entry回收掉并set相应的值。  

###### 1.4.3.2.5.2. ThreadLocal应用
1. ThreadLocal使用场景：  
    1. 线程安全问题。
    2. 业务中变量传递。1)ThreadLocal实现同一线程下多个类之间的数据传递；2)ThreadLocal实现线程内的缓存，避免重复调用。
    3. ThreadLocal+MDC实现链路日志增强。
    4. ThreadLocal 实现数据库读写分离下强制读主库。
2. ~~ThreadLocal三大坑~~
    1. 内存泄露
    2. ThreadLocal无法在`父子线程（new Thread()）`之间传递。使用类InheritableThreadLocal可以在子线程中取得父线程继承下来的值。   
    3. 线程池中线程上下文丢失。TransmittableThreadLocal是阿里巴巴开源的专门解决InheritableThreadLocal的局限性，实现线程本地变量在线程池的执行过程中，能正常的访问父线程设置的线程变量。  
    4. 并行流中线程上下文丢失。问题同线程池中线程上下文丢失。  
3. ThreadLocal优化：FastThreadLocal

#### 1.4.3.3. 线程通信(生产者消费者问题)

#### 1.4.3.4. 线程活跃性


### 1.4.4. JUC
#### 1.4.4.1. CAS
1. **<font color = "clime">CAS，Compare And Swap，即比较并交换。一种无锁原子算法，CAS是一种乐观锁。</font>**  
2. CAS函数  
&emsp; **<font color = "clime">在函数CAS(V,E,N)中有3个参数：从内存中读取的值E，计算的结果值V，内存中的当前值N（可能已经被其他线程改变）。</font>**  
&emsp; **<font color = "clime">函数流程：</font>** 1. 读取当前值E；2. 计算结果值V；<font color = "clime">3. 将读取的当前值E和当前新值N作比较，如果相等，更新为V；</font>4. 如果不相等，再次读取当前值E计算结果V，将E再和新的当前值N比较，直到相等。 
3. **`CAS缺点：`**  
    * 循环时间长开销大。自旋CAS如果长时间不成功，会给CPU带来非常大的执行开销。  
    * **<font color = "red">只能保证一个共享变量的原子操作。</font> <font color = "clime">从Java1.5开始JDK提供了AtomicReference类来保证引用对象之间的原子性，可以把多个变量放在一个对象里来进行CAS操作。</font>**  
    * ABA问题。  
4. ABA问题详解
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
            1. `调用使用者重写的tryAcquire方法，` **<font color = "blue">tryAcquire()尝试直接去获取资源，</font>** 如果成功则直接返回；
            2. tryAcquire()获取资源失败，则`调用addWaiter()将该线程加入等待队列的尾部`，并标记为独占模式；
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
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-87.png)  
    1. 在初始化ReentrantLock的时候，如果不传参数是否公平，那么默认使用非公平锁，也就是NonfairSync。  
    2. 1). <font color = "clime">调用ReentrantLock的lock方法的时候，实际上是调用了NonfairSync的lock方法，这个方法①先用CAS操作`compareAndSetState(0, 1)`，去尝试抢占该锁。如果成功，就把当前线程设置在这个锁上，表示抢占成功。</font>         
        `“非公平”体现在，如果占用锁的线程刚释放锁，state置为0，而排队等待锁的线程还未唤醒时，新来的线程就直接抢占了该锁，那么就“插队”了。`   
        2). ②如果失败，则`调用acquire()模板方法`，等待抢占。   
    3. `AQS的acquire模板方法：`  
        1. AQS#acquire()调用子类NonfairSync#tryAcquire()#nonfairTryAcquire()。 **<font color = "blue">如果锁状态是0，再次CAS抢占锁。</font>** 如果锁状态不是0，判断是否当前线程。    
        2. acquireQueued(addWaiter(Node.EXCLUSIVE), arg) )，其中addWaiter(Node.EXCLUSIVE)入等待队列。  
        3. acquireQueued(final Node node, int arg)，使线程阻塞在等待队列中获取资源，一直获取到资源后才返回。如果在整个等待过程中被中断过，则返回true，否则返回false。
        4. 如果线程在等待过程中被中断过，它是不响应的。只是获取资源后才再进行自我中断selfInterrupt()，将中断补上。  

    &emsp; 用一张流程图总结一下非公平锁的获取锁的过程。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-75.png)  

###### 1.4.4.3.1.1. 读写锁
1. ReentrantReadWriteLock  
    1. 读写锁ReentrantReadWriteLock：读读共享，`读写互斥`，写写互斥。  
    2. **<font color = "red">ReentrantReadWriteLock缺点：`读写锁互斥`，只有当前没有线程持有读锁或者写锁时，才能获取到写锁，</font><font color = "clime">这`可能会导致写线程发生饥饿现象`，</font><font color = "red">即读线程太多导致写线程迟迟竞争不到锁而一直处于等待状态。StampedLock()可以解决这个问题。</font>**  
2. StampedLock，Stamped，有邮戳的  
    1. StampedLock有3种模式：写锁writeLock、悲观读锁readLock、乐观读锁tryOptimisticRead。  
    2. StampedLock通过乐观读锁tryOptimisticRead解决ReentrantReadWriteLock的写锁饥饿问题。乐观读锁模式下，一个线程获取的乐观读锁之后，不会阻塞其他线程获取写锁。    
    3. **<font color = "clime">同时允许多个乐观读和一个写线程同时进入临界资源操作，那读取的数据可能是错的怎么办？</font>**    
    &emsp; **<font color = "clime">通过版本号控制。</font>** 乐观读不能保证读取到的数据是最新的，所以将数据读取到局部变量的时候需要通过 lock.validate(stamp) 校验是否被写线程修改过，若是修改过则需要上悲观读锁，再重新读取数据到局部变量。`即乐观读失败后，再次使用悲观读锁。`    

#### 1.4.4.4. Atomic
##### 1.4.4.4.1. AtomicStampedReference与AtomicMarkableReference
1. AtomicStampedReference每次修改都会让stamp值加1，类似于版本控制号。 
2. **<font color = "clime">AtomicStampedReference可以知道引用变量中途被更改了几次。有时候，并不关心引用变量更改了几次，只是单纯的关心是否更改过，所以就有了AtomicMarkableReference。</font>**  

##### 1.4.4.4.2. LongAdder
1. LongAdder重要属性：有一个全局变量`volatile long base`值、父类Striped64中存在一个`volatile Cell[] cells;`数组，其长度是2的幂次方。  
2. LongAdder原理：  
    1. CAS操作：当并发不高的情况下都是通过CAS来直接操作base值，如果CAS失败，则针对LongAdder中的Cell[]数组中的Cell进行CAS操作，减少失败的概率。
    2. 解决伪共享：每个Cell都使用@Contended注解进行修饰，而@Contended注解可以进行缓存行填充，从而解决伪共享问题。  

#### 1.4.4.5. Collections
##### 1.4.4.5.1. CopyOnWriteArrayList
1. CopyOnWriteArrayList  
&emsp; CopyOnWrite，写时复制。`读操作时不加锁以保证性能不受影响。`  
&emsp; **<font color = "clime">`写操作时加锁，复制资源的一份副本，在副本上执行写操作，写操作完成后将资源的引用指向副本。`</font>** CopyOnWriteArrayList源码中，`基于ReentrantLock保证了增加元素和删除元素动作的互斥。`   
&emsp; **优点：** 可以对CopyOnWrite容器进行并发的读，而不需要加锁，因为当前容器不会添加任何元素。`所以CopyOnWrite容器也是一种读写分离的思想，读和写不同的容器。`  
&emsp; **<font color = "clime">缺点：** **1.占内存(写时复制，new两个对象)；2.不能保证数据实时一致性。</font>**  
&emsp; **使用场景：** <font color = "clime">CopyOnWrite并发容器用于读多写少的并发场景。比如白名单，黑名单，商品类目的访问和更新场景。</font>

##### 1.4.4.5.2. ConcurrentHashMap
1. ConcurrentHashMap，JDK1.8  
    &emsp; **<font color = "red">从jdk1.8开始，ConcurrentHashMap类取消了Segment分段锁，采用`Node + CAS + Synchronized`来保证并发安全。</font>**  
    &emsp; **<font color = "clime">jdk1.8中的ConcurrentHashMap中synchronized只锁定当前链表或红黑树的首节点，只要节点hash不冲突，就不会产生并发，相比JDK1.7的ConcurrentHashMap效率又提升了许多。</font>**  
    1. **<font color = "clime">put()流程：</font>**
        1. 根据key计算出hashcode。  
        2. `整个过程自旋添加节点。`  
        2. 判断是否需要进行初始化数组。  
        3. <font color = "red">为当前key定位出Node，如果为空表示此数组下无节点，当前位置可以直接写入数据，利用CAS尝试写入，失败则进入下一次循环。</font>  
        4. **<font color = "blue">如果当前位置的hashcode == MOVED == -1，表示其他线程插入成功正在进行扩容，则当前线程`帮助进行扩容`。</font>**  
        5. <font color = "red">如果都不满足，则利用synchronized锁写入数据。</font>  
        6. 如果数量大于TREEIFY_THRESHOLD则要转换为红黑树。 
        7. 最后通过addCount来增加ConcurrentHashMap的长度，并且还可能触发扩容操作。  
    2. **<font color = "clime">get()流程：为什么ConcurrentHashMap的读操作不需要加锁？</font>**  
        1. 在1.8中ConcurrentHashMap的get操作全程不需要加锁，这也是它比其他并发集合（比如hashtable、用Collections.synchronizedMap()包装的hashmap）安全效率高的原因之一。  
        2. `get操作全程不需要加锁是因为Node的成员val是用volatile修饰的，`和数组用volatile修饰没有关系。  
        3. 数组用volatile修饰主要是保证在数组扩容的时候保证可见性。  
2. ~~ConcurrentHashMap，JDK1.7~~  
    1. 在JDK1.7中，ConcurrentHashMap类采用了分段锁的思想，Segment(段) + HashEntry(哈希条目) + ReentrantLock。  
    2. Segment继承ReentrantLock(可重入锁)，从而实现并发控制。Segment的个数一旦初始化就不能改变，默认Segment的个数是16个，也可以认为ConcurrentHashMap默认支持最多16个线程并发。  
    3. put()方法：  
        1. 获取 ReentrantLock 独占锁，获取不到，scanAndLockForPut 获取。  
        2. scanAndLockForPut 这个方法可以确保返回时，当前线程一定是获取到锁的状态。  

##### 1.4.4.5.3. BlockingQueue
1. 阻塞队列：当队列是空的时候，从队列中获取元素的操作将会被`阻塞`；或者当队列是满时，往队列里添加元素的操作会被`阻塞`。  
2. `线程池所使用的缓冲队列，常用的是：SynchronousQueue（无缓冲等待队列）、ArrayBlockingQueue（有界缓冲等待队列）、LinkedBlockingQueue（无界缓冲等待队列）。`   
3. SynchronousQueue，没有容量，是无缓冲等待队列，是一个不存储元素的阻塞队列，会直接将任务交给消费者，必须等队列中的元素被消费后才能继续添加新的元素。  
4. LinkedBlockingQueue不同于ArrayBlockingQueue，它如果不指定容量，默认为Integer.MAX_VALUE，也就是无界队列。所以为了避免队列过大造成机器负载或者内存爆满的情况出现，在使用的时候建议手动传一个队列的大小。  
5. <font color = "red">ArrayBlockingQueue与LinkedBlockingQueue：</font> ArrayBlockingQueue预先分配好一段连续内存，更稳定；LinkedBlockingQueue读写锁分离，吞吐量更大。  

#### 1.4.4.6. tools
##### 1.4.4.6.1. CountDownLatch
0. CountDownLatch中count down是倒数的意思，latch则是门闩的含义。整体含义可以理解为倒数的门栓，似乎有一点“三二一，芝麻开门”的感觉。CountDownLatch的作用也是如此，在构造CountDownLatch的时候需要传入一个整数n，在这个整数“倒数”到0之前，主线程需要等待在门口，而这个“倒数”过程则是由各个执行线程驱动的，每个线程执行完一个任务“倒数”一次。总结来说，CountDownLatch的作用就是等待其他的线程都执行完任务，必要时可以对各个任务的执行结果进行汇总，然后主线程才继续往下执行。  
1. java.util.concurrent.CountDownLatch类， **<font color = "red">能够使一个线程等待其他线程完成各自的工作后再执行。</font>** <font color = "red">利用它可以实现类似计数器的功能。</font><font color = "blue">比如有一个任务A，它要等待其他4个任务执行完毕之后才能执行，此时就可以利用CountDownLatch来实现这种功能了。</font>  
2. CountDownLatch的典型应用场景，大体可分为两类：结束信号、开始信号。  
&emsp; 主线程创建、启动N个异步任务，我们期望当这N个任务全部执行完毕结束后，主线程才可以继续往下执行。即将CountDownLatch作为任务的结束信号来使用。   
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
&emsp; 使用场景： **<font color = "red">Semaphore通常用于限制可以访问某些资源（物理或逻辑的）的线程数目。Semaphore可以用来构建一些对象池，资源池之类的，比如数据库连接池。</font>**   


## 1.5. 数据库
### 1.5.1. SQL语句  
#### 1.5.1.1. 基本查询语句
1. 基本查询SQL执行顺序：from -> on -> join -> where -> group by ->  avg,sum.... ->having -> select -> distinct -> order by -> top,limit。 
2. distinct关键字：Distinct与Count(聚合函数)，COUNT()会过滤掉为NULL的项。  
3. 分组函数  
&emsp; **<font color = "clime">查询结果集中有统计数据时，就需要使用分组函数。</font>**  
&emsp; **<font color = "red">Group By分组函数中，查询只能得到组相关的信息。组相关的信息(统计信息)：count,sum,max,min,avg。</font> 在select指定的字段要么包含在Group By语句的后面，作为分组的依据；要么被包含在聚合函数中。`group by是对结果集分组`，而不是查询字段分组。**  
&emsp; **<font color = "red">Group By含有去重效果。</font>**  
1. 普通Limit语句需要全表扫描。  
&emsp; 建立主键或唯一索引，利用索引：`SELECT * FROM 表名称 WHERE id_pk > (pageNum*10) LIMIT M`  
&emsp; 基于索引再排序：`SELECT * FROM 表名称 WHERE id_pk > (pageNum*10) ORDER BY id_pk ASC LIMIT M`
2. **<font color = "blue">ORDER BY与limit（分页再加排序）</font>**  
&emsp; ORDER BY排序后，用LIMIT取前几条，发现返回的结果集的顺序与预期的不一样。    
&emsp; 如果order by的列有相同的值时，`MySQL会随机选取这些行`，`为了保证每次都返回的顺序一致可以额外增加一个排序字段（比如：id），用两个字段来尽可能减少重复的概率。`  

#### 1.5.1.2. 连接查询
1. **关键字in：**  
&emsp; **<font color = "clime">in查询里面的数量最大只能1000。</font>**  
&emsp; **<font color = "red">确定给定的值是否与子查询或列表中的值相匹配。in在查询的时候，首先查询子查询的表，然后将内表和外表做一个笛卡尔积，然后按照条件进行筛选。所以</font><font color = "clime">相对内表比较小的时候，in的速度较快。</font>**  
2. exists指定一个子查询，检测行的存在。<font color = "clime">遍历循环外表，然后看外表中的记录有没有和内表的数据一样的。匹配上就将结果放入结果集中。</font><font color = "red">exists内层查询语句不返回查询的记录，而是返回一个真假值。</font>  
&emsp; **<font color = "clime">in和exists的区别：</font><font color = "red">如果子查询得出的结果集记录较少，主查询中的表较大且又有索引时应该用in，反之如果外层的主查询记录较少，子查询中的表大，又有索引时使用exists。</font>**  
3. **UNION与UNION ALL：** 默认地，UNION 操作符选取不同的值。如果允许重复的值，请使用UNION ALL。  

#### 1.5.1.3. ~~高级查询~~


### 1.5.2. MySql优化
&emsp; <font color = "red">MySql性能由综合因素决定，抛开业务复杂度，影响程度依次是硬件配置、MySQL配置、数据表设计、索引优化。</font>  
1. SQL语句的优化。  
    &emsp; `对查询语句的监控、分析、优化是SQL优化的一般步骤。`常规调优思路：  
    1. 查看慢查询日志slowlog，分析slowlog，分析出查询慢的语句。  
    2. 按照一定优先级，进行一个一个的排查所有慢语句。  
    3. 分析top sql，进行explain调试，查看语句执行时间。  
    4. 调整[索引](/docs/SQL/7.index.md)或语句本身。 
2. 存储数据量较大： **<font color = "red">单库单表无法满足时，可以拆分表结构（主从复制、分库分表），或者使用ES搜索引擎。</font>**  
3. 服务器的优化。  

#### 1.5.2.1. SQL分析
1. **<font color = "clime">SQL分析语句有EXPLAIN与explain extended、show warnings、proceduer analyse、profiling、trace。</font>**  
2. <font color = "red">用explain extended查看执行计划会比explain多一列filtered。filtered列给出了一个百分比的值，这个百分比值和rows列的值一起使用，可以估计出那些将要和explain中的前一个表进行连接的行的数目。前一个表就是指explain的id列的值比当前表的id小的表。</font>  
&emsp; mysql中有一个explain 命令可以用来分析select 语句的运行效果，例如explain可以获得select语句使用的索引情况、排序的情况等等。除此以外，explain 的extended 扩展能够在原本explain的基础上额外的提供一些查询优化的信息，这些信息可以通过mysql的show warnings命令得到。  
3. profiling  
&emsp; 使用profiling命令可以了解SQL语句消耗资源的详细信息（每个执行步骤的开销）。可以清楚了解到SQL到底慢在哪个环节。   
4. trace  
&emsp; 查看优化器如何选择执行计划，获取每个可能的索引选择的代价。  

##### 1.5.2.1.1. Expain
&emsp; expain信息列分别是id、select_type、table、partitions、`type`、possible_keys、`key`、`key_len`、ref、`rows`、filtered、 `Extra`。  
* **<font color = "clime">`type，单表的访问方法。`单表查询类型要达到range级别（只检索给定范围的行，使用一个索引来选择行，非全表扫描）。</font>**  
* key_len表示使用的索引长度，key_len可以衡量索引的好坏。key_len越小，索引效果越好。 **<font color = "blue">可以根据key_len来判断联合索引是否生效。</font>**  
* **<font color = "red">extra：额外的信息，该列包含MySQL解决查询的详细信息。注意，常见的不太友好的值，如Using filesort（额外排序）、Using temporary（使用了临时表），意思MYSQL根本不能使用索引，常出现在使用order by。</font>**  

#### 1.5.2.2. SQL优化
1. 基本查询优化：  
2. 子查询优化：使用连接（JOIN）来代替子查询（Sub-Queries）。  
2. 关联查询优化：使用索引、 **<font color = "bllue">驱动表选择、`条件谓词下推`</font>** ......  
&emsp; 谓词下推，就是在将过滤条件下推到离数据源更近的地方，最好就是在table_scan时就能过滤掉不需要的数据。  

#### 1.5.2.3. 索引优化
1. 创建索引：为了使索引的使用效率更高，在创建索引时，需要考虑在哪些字段上创建索引和创建什么类型的索引。  
    * 多表连接的字段、where条件字段、分组字段、排序字段、联合UNION字段、去重distinct字段上建立索引。  
    * 尽量选择区分度高的列作为索引。  
    * ...  
2. 索引失效：进行null值运算、进行运算、隐式转换、对索引列使用函数，导致索引失效，进行模糊查询like时可能使索引失效(以%开头)，不满足联合索引最左前缀匹配原则。   
3. 索引条件下推：  
&emsp; 索引下推简而言之就是在复合索引由于某些条件（比如 like %aa）失效的情况下，当存在失效的过滤字段在索引覆盖范围内，使用比较的方式在不回表的情况下进一步缩小查询的范围。其实就是对索引失效的进一步修复。  
&emsp; **<font color = "clime">~~MySQL 5.6 引入了「索引下推优化」，可以在索引遍历过程中，对索引中包含的字段先做判断，直接过滤掉不满足条件的记录，减少回表次数。~~</font>**  
    * 关闭ICP：索引 ---> 回表 ---> 条件过滤。  
    * 开启ICP：索引 ---> 条件过滤 ---> 回表。</font>在支持ICP后，`MySQL在取出索引数据的同时，判断是否可以进行where条件过滤，`<font color = "blue">将where的部分过滤操作放在存储引擎层提前过滤掉不必要的数据，</font>`减少了不必要数据被扫描带来的IO开销。`  

#### 1.5.2.4. 碎片优化


### 1.5.3. 数据库分布式
#### 1.5.3.1. 大数据量操作

#### 1.5.3.2. MySql瓶颈
1. MySql性能
	* 最大并发数：并发数是指同一时刻数据库能处理多少个请求，由max_connections和max_user_connections决定。max_connections是指MySQL实例的最大连接数，上限值是16384，max_user_connections是指每个数据库用户的最大连接数。  
	* 查询耗时0.5秒，0.5秒是个经验值。  
	* 最大数据量：《阿里巴巴Java开发手册》提出单表行数超过500万行或者单表容量超过2GB，才推荐分库分表。  
2. 数据库瓶颈  
	&emsp; <font color = "clime">`不管是IO瓶颈，还是CPU瓶颈，最终都会导致数据库的活跃连接数增加，进而逼近甚至达到数据库可承载活跃连接数的阈值。在业务Service来看就是，可用数据库连接少甚至无连接可用。`</font>   
	1. IO瓶颈：  
	&emsp; 第一种：磁盘读IO瓶颈，热点数据太多，数据库缓存放不下，每次查询时会产生大量的IO，降低查询速度。 解决方案：分库和垂直分表。  
	&emsp; 第二种：网络IO瓶颈，请求的数据太多（MySql一般并发数200～5000），网络带宽不够。 解决方案：分库。  
	2. CPU瓶颈：  
	&emsp; 第一种：SQL问题，如SQL中包含join，group by，order by，非索引字段条件查询等，增加CPU运算的操作。 解决方案：SQL优化，建立合适的索引，在业务Service层进行业务计算。  
	&emsp; 第二种：单表数据量太大（达到1000W或100G以后），查询时扫描的行太多，SQL效率低，CPU率先出现瓶颈。 解决方案：水平分表。  

#### 1.5.3.3. 数据库分布式
&emsp; **`数据库拆分过程基本遵循的顺序是：`1).垂直拆分(业务拆分) ---> 2).读写分离 ---> 3).分库分表(水平拆分)。每个拆分过程都能解决业务上的一些问题，但同时也面临了一些挑战。**  
1. **分库分表与读写分离：**   
    &emsp; `读写分离实现了数据库读能力的水平扩展，分库分表实现了写能力的水平扩展。`  
    1. 存储能力的水平扩展：在读写分离的情况下，每个集群中的master和slave基本上数据是完全一致的，从存储能力来说，存在海量数据的情况下，可能由于磁盘空间的限制，无法存储所有的数据。而在分库分表的情况下，可以搭建多个mysql主从复制集群，每个集群只存储部分分片的数据，实现存储能力的水平扩展。  
    2. 写能力的水平扩展：在读写分离的情况下，由于每个集群只有一个master，所有的写操作压力都集中在这一个节点上，在写入并发非常高的情况下，这里会成为整个系统的瓶颈。而在分库分表的情况下，每个分片所属的集群都有一个master节点，都可以执行写入操作，实现写能力的水平扩展。此外减小建立索引开销，降低写操作的锁操作耗时等，都会带来很多显然的好处。  
2. 分表和分区  
    1.  **分表和分区的区别：**  
        1. 实现方式上：
            * mysql的分表是真正的分表，一张表分成很多表后，每一个小表都是完整的一张表，都对应三个文件，一个.MYD数据文件，.MYI索引文件，.frm表结构文件。  
            * 分区不一样，一张大表进行分区后，还是一张表，不会变成多张表，但是存放数据的区块变多了。  
        2. 数据处理上： 
            * 分表后，数据都是存放在分表里，总表只是一个外壳，存取数据发生在一个一个的分表里面。  
            * 分区不存在分表的概念，分区只不过把存放数据的文件分成了许多小块，分区后的表还是一张表。数据处理还是由自己来完成。  
    2. **分表和分区的联系：**  
        1. 都能提高mysql的性能，在高并发状态下都有一个良好的表面。 
        2. **<font color = "clime">分表和分区不矛盾，可以相互配合。</font>**  
            * 对于那些大访问量，并且表数据比较多的表，可以`采取分表和分区结合的方式`（如果merge这种分表方式，不能和分区配合的话，可以用其他的分表试）。  
            * `访问量不大，但是表数据很多的表，可以采取分区的方式等。`  

#### 1.5.3.4. 主从复制
##### 1.5.3.4.1. 主从复制原理  
1. 对于每一个主从复制的连接，都有三个线程。  
    * 拥有多个从库的主库为每一个连接到主库的从库创建一个binlog输出线程。  
    * 每一个从库都有它自己的I/O线程和SQL线程。  
        * `I/O线程与主库进行连接，请求主库的binlog。接收到binlog后，会存储到relay log中（中继日志）。`  
        * SQL线程会解析中继日志，并在从库上进行应用。  
2. 同步方式可以划分为：异步、半同步和同步。`在MySQL5.7中，带来了全新的多线程复制技术。`  
3. 复制类型有三种：基于行的复制、基于语句的复制、混合模式复制。  
    * 并非所有修改数据的语句都可以使用基于语句的复制进行复制。使用基于语句的复制时，任何非确定性行为都难以复制。  
    * 基于行的复制会产生大量的日志。  
    &emsp; 注：Mysql到Elasticsearch实时增量同步，多采用基于行复制。    
    * MySQL5.1及其以后的版本推荐使用混合模式的复制，它是<font color = "clime">根据事件的类型实时的改变binlog的格式。当设置为混合模式时，默认为基于语句的格式，但在特定的情况下它会自动转变为基于行的模式。</font>  

##### 1.5.3.4.2. 主从复制实现


##### 1.5.3.4.3. 主从复制问题
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

##### 1.5.3.4.4. 高可用实现


##### 1.5.3.4.5. 读写分离实现
&emsp; 读写分离的实现，可以在应用层解决，也可以通过中间件实现。  
1. 应用层解决方案：  
    1. 驱动实现
        * com.mysql.jdbc.ReplicationDriver
        * Sharding-jdbc
    2. MyBatis plugin(sqlType: select,update,insert)  
    3. SpringAOP + mybatis plugin + 注解
    4. Spring动态数据源 + mybatis plugin
2. 常见代理中间件有MyCat...  

#### 1.5.3.5. 分区

#### 1.5.3.6. 分库分表
##### 1.5.3.6.1. 分库分表
1. 数据切分方式：  
    * 垂直分库，一般根据业务维度拆分，分布式项目中单项目单库。  
    * **<font color = "clime">`水平分库`主要根据`用户属性（如地市）`拆分物理数据库。一种常见的方式是将全省划分为多个大区。`可以复合分片字段拆分，即按照用户属性（如地市）拆分后，再按照时间拆分。`</font>**  
    * 垂直分表，基于列字段进行的。一般是表中的字段较多，将不常用的，数据较大，长度较长（比如text类型字段）的拆分到“扩展表”。  
    * ~~水平分表：针对数据量比较大的单张表。~~ **<font color = "red">MySql水平分表必须使用MyISAM引擎。</font>**  
2. 水平分库无论怎么分，只要能通过拆分字段和分片策略，找到具体的库就可以。  

##### 1.5.3.6.2. 分库分表查询
1. `非partition key的查询 / 分库分表多维度查询`  
	* 冗余法
	* 基因法  
    &emsp; 如果拆分成16张表，则需要截取二进制订单id的最后LOG(16,2)=4位，作为分库/分表基因，订单id的最后4位采用从用户id那边获取的4位基因。这样就满足按照订单号和用户（买家、卖家）id查询。   
	* 映射法
	* NoSQL法：ES、Hbase等。  
    
    <font color = "blue">B2B模式（有买家、卖家），订单表采用`冗余法（买家库和卖家库）和基因法`结合。</font>  
2. `跨分片的分组group by以及聚合count等函数`  
&emsp; 这些是一类问题，因为它们<font color = "red">都需要基于全部数据集合进行计算。多数的代理都不会自动处理合并工作，部分支持聚合函数MAX、MIN、COUNT、SUM。</font>  
&emsp; **<font color = "red">解决方案：分别在各个节点上执行相应的函数处理得到结果后，在应用程序端进行合并。</font>** 每个结点的查询可以并行执行，因此很多时候它的速度要比单一大表快很多。但如果结果集很大，对应用程序内存的消耗是一个问题。  
3. `跨分片的排序分页`  
&emsp; <font color = "red">一般来讲，分页时需要按照指定字段进行排序。`当排序字段是分片字段时，通过分片规则可以比较容易定位到指定的分片；`而当排序字段非分片字段时，情况就会变得比较复杂了。</font>为了最终结果的准确性，需要在不同的分片节点中将数据进行排序并返回，并将不同分片返回的结果集进行汇总和再次排序，最后再返回给用户。  
4. `跨节点Join的问题`  
    tddl、MyCAT等都支持跨分片join。如果中间不支持，跨库Join的几种解决思路：  
	* `在程序中进行拼装。`  
	* 全局表
	* 字段冗余 
5. ~~**<font color = "blue">小结：分库分表分片键设计</font>**~~  
&emsp; ~~分库分表的分片键设计多数参考查询场景。因此分库分表时设计拆分字段考虑因素：1). 是否有必要按照地区、时间拆分表；2)参考B2B模式（有买家、卖家），订单表采用`冗余法（买家库和卖家库）和基因法`结合。~~  

##### 1.5.3.6.3. 跨分片的排序分页
&emsp; 常见的分片策略有随机分片和连续分片这两种。“跨库分页”的四种方案。    
2. 全局视野法
	1. 流程  
        &emsp; （1）将order by time offset X limit Y，改写成order by time offset 0 limit X+Y  
        &emsp; （2）服务层对得到的N*(X+Y)条数据进行内存排序，内存排序后再取偏移量X后的Y条记录   
		1. 如果要获取第N页的数据(每页S条数据)，则将每一个子库的前N页(offset 0,limit N*S)的所有数据都先查出来(有筛选条件或排序规则的话都包含)。  
		2. 然后将各个子库的结果合并起来之后，再做一次分页查询（可不用带上相同的筛选条件，但还要带上排序规则)即可得出最终结果，这种方式类似es分页的逻辑。  
	2. 优点: 数据准确，可以跳页  
	3. 缺点：  
	（1）每个分库需要返回更多的数据，增大了网络传输量（耗网络）；  
	（2）服务层还需要进行二次排序，增大了服务层的计算量（耗CPU）；   	
    （3）最致命的，这个算法随着页码的增大，性能会急剧下降，这是因为SQL改写后每个分库要返回X+Y行数据：返回第3页，offset中的X=200；假如要返回第100页，offset中的X=9900，即每个分库要返回100页数据，数据量和排序量都将大增，性能平方级下降。     
3. 方法二：业务折衷法-禁止跳页查询(对应es中的scroll方法)   
	1. 流程：  
        &emsp; （1）用正常的方法取得第一页数据，并得到第一页记录的time_max  
        &emsp; （2）每次翻页，将order by time offset X limit Y，改写成order by time where time>$time_max limit Y  
		1. 如果要获取第N页的数据，第一页时，是和全局视野法一致。  
		2. 但第二页开始后，需要在每一个子库查询时，加上可以排除上一页的过滤条件(如按时间排序时，获取上一页的最大时间后，需要加上time > ${maxTime_lastPage}的条件，然后再limit S。即可获取各个子库的结果。  
		3. 之后再合并后top S即可得到最终结果。  
	2. 优点: 数据准确，性能良好  
	3. 缺点: 不能跳页    
4. 方法三：业务折衷法-允许模糊数据  
	1. 前提：数据库分库-数据均衡原理  
	使用patition key进行分库，在数据量较大，数据分布足够随机的情况下，各分库所有非patition key属性，在各个分库上的数据分布，统计概率情况是一致的。  
	2. 流程：将order by time offset X limit Y，改写成order by time offset X/N limit Y/N    
	3. 优点: 性能良好，可以跳页
	4. 缺点: 数据不准确
5. 终极武器-二次查询法  
    &emsp; （1）将order by time offset X limit Y，改写成order by time offset X/N limit Y   
    &emsp; （2）找到最小值time_min   
    &emsp; （3）between二次查询，order by time between $time_min and $time_i_max    
    &emsp; （4）设置虚拟time_min，找到time_min在各个分库的offset，从而得到time_min在全局的offset    
    &emsp; （5）得到了time_min在全局的offset，自然得到了全局的offset X limit Y    

------------------
查询二次改写   
这也是"业界难题-跨库分页”中提到的一个方法，大致思路如下：在某 1 页的数据均摊到各分表的前提下(注：这个前提很重要，也就是说不会有一个分表的数据特别多或特别少)，换句话说：这个方案不适用分段法。    
第一次改写的SQL语句是select * from T order by time offset 333 limit 5  
第二次要改写成一个between语句，between的起点是time_min，between的终点是原来每个分库各自返回数据的最大值：  
第一个分库，第一次返回数据的最大值是1487501523  
所以查询改写为select * from T order by time where time between time_min and 1487501523  

第二个分库，第一次返回数据的最大值是1487501323  
所以查询改写为select * from T order by time where time between time_min and 1487501323  

第三个分库，第一次返回数据的最大值是1487501553  
所以查询改写为select * from T order by time where time between time_min and 1487501553  

相对第一次查询，第二次查询条件放宽了，故第二次查询会返回比第一次查询结果集更多的数据  


#### 1.5.3.7. 数据迁移
1. 现在有一个未分库分表的系统，未来要分库分表，如何设计才可以让系统从未分库分表**动态切换**到分库分表上？
    * 停机迁移方案
    * 双写迁移方案 

### 1.5.4. 索引事物锁
#### 1.5.4.1. 索引底层原理 
1. **<font color = "clime">评价一个数据结构作为索引的优劣最重要的指标就是在查找过程中`磁盘I/O`操作次数的渐进复杂度。</font>**  
&emsp; 操作系统中以页这种结构作为读写的基本单位。操作系统IO消耗：<font color = "red">一般来说，索引本身也很大，不可能全部存储在内存中，因此索引往往以索引文件的形式存储的磁盘上。</font>这样的话，索引查找过程中就要产生磁盘I/O消耗，相对于内存存取，I/O存取的消耗要高几个数量级，所以 **<font color = "clime">评价一个数据结构作为索引的优劣最重要的指标就是在查找过程中磁盘I/O操作次数的渐进复杂度。</font>** 换句话说，索引的结构组织要尽量减少查找过程中磁盘I/O的存取次数。  
2. InnoDB使用的数据结构选择：  
    * Hash索引适合精确查找，但是范围查找不适合。  
    * 二叉查找树，可能退化成单链表，相当于全表扫描。  
    * 二叉搜索树为什么不适合用作数据库索引？    
    &emsp; （1）当数据量大的时候，树的高度会比较高，数据量大的时候，查询会比较慢；  
    &emsp; （2）每个节点只存储一个记录，可能导致一次查询有很多次磁盘IO；    
    * 平衡二叉树的不足：  
        ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-93.png)  
        &emsp; 如果上图中平衡二叉树保存的是id索引，现在要查找id = 8的数据，过程如下：  

        1. 把根节点加载进内存，用8和10进行比较，发现8比10小，继续加载10的左子树。
        2. 把5加载进内存，用8和5比较，同理，加载5节点的右子树。
        3. 此时发现命中，则读取id为8的索引对应的数据。

        索引保存数据的方式一般有两种：

        * 数据区保存id 对应行数据的所有数据具体内容。  
        * 数据区保存的是真正保存数据的磁盘地址。  

        &emsp; 到这里，平衡二叉树解决了存在线性链表的问题，数据查询的效率好像也还可以，基本能达到O(log2(n))， 那为什么mysql不选择平衡二叉树作为索引存储结构，他又存在什么样的问题呢？    

        1. 搜索效率不足。一般来说，在树结构中，数据所处的深度，决定了搜索时的IO次数（MySql中将每个节点大小设置为一页大小，一次IO读取一页 / 一个节点）。如上图中搜索id = 8的数据，需要进行3次IO。当数据量到达几百万的时候，树的高度就会很恐怖。
        2. 查询不不稳定。如果查询的数据落在根节点，只需要一次IO，如果是叶子节点或者是支节点，会需要多次IO才可以。
        3. 存储的数据内容太少。没有很好利用操作系统和磁盘数据交换特性，也没有利用好磁盘IO的预读能力。因为操作系统和磁盘之间一次数据交换是以页为单位的，一页大小为 4K，即每次IO操作系统会将4K数据加载进内存。但是，在二叉树每个节点的结构只保存一个关键字，一个数据区，两个子节点的引用，并不能够填满4K的内容。幸幸苦苦做了一次的IO操作，却只加载了一个关键字。在树的高度很高，恰好又搜索的关键字位于叶子节点或者支节点的时候，取一个关键字要做很多次的IO。
    * B树：
        1. B树中每个节点中不仅包含数据的key值，还有data值。 **<font color = "red">而每一个页的存储空间是有限的，如果data数据较大时将会导致每个节点（即一个页）能存储的key的数量很小。当存储的数据量很大时同样会导致B树的深度较大，</font>** 增大查询时的磁盘I/O次数进而影响查询效率。  
        2. `范围查询，磁盘I/O高。`
    * B+树  
        1. B+Tree中间节点不存储数据，因此B+Tree能够在同样大小的节点中，存储更多的key。
        2. `叶子节点之间会有个指针指向，这个也是B+树的核心点，可以大大提升范围查询效率，也方便遍历整个树。`  
        3. `B+tree的查询效率更加稳定。`  
3. `InnoDB一棵B+树可以存放多少行数据？约2千万。`~~待总结~~  
3. **<font color = "red">联合索引底层还是使用B+树索引，并且还是只有一棵树，只是此时的排序：首先按照第一个索引排序，在第一个索引相同的情况下，再按第二个索引排序，依此类推。</font>**  
4. 无索引时的数据查询：查询数据时从磁盘中依次加载数据页到InnoDB的缓冲池中，然后对缓冲池中缓存页的每行数据，通过数据页的单向链表一个一个去遍历查找，如果没有找到，那么就会顺着数据页的双向链表数据结构，依次遍历加载磁盘中的其他数据页到缓冲池中遍历查询。 

#### 1.5.4.2. ~~各种索引~~（还需要总结）
&emsp; <font color = "red">InnoDB索引类型可以分为主键索引（聚簇索引）和辅助索引（非聚簇索引/非主键索引）。</font>  

#### 1.5.4.3. MySql事务（还需要总结）  
1. 事务的四大特性（ACID）：原子性（Atomicity）、一致性（Consistency）、隔离性（Isolation）、持久性（Durability）。  
2. 并发事务处理带来的问题：脏读、丢失修改、不可重复读、幻读。  
    * 脏`读`：一个事务读了另一个事务未提交的数据。
    * 丢失`修改`：一个事务覆盖了另一个事务的数据。  
    * 不可重复读：一个事务多次读，另一事务中间修改了数据。  
    * 幻读：一个事务多次读，另一事务中间新增了数据。  
3. SQL标准定义了四个隔离级别（隔离性）：读取未提交、读取已提交、可重复读（可以阻止脏读和不可重复读，幻读仍有可能发生，但MySql的可重复读解决了幻读）、可串行化。  
4. Innodb事务实现原理：
    * 原子性的实现：采用回滚日志[undo log](/docs/SQL/undoLog.md)实现。  
    * 持久性的实现：采用重做日志[redo log](/docs/SQL/redoLog.md)实现。  
    * 隔离性（事务的隔离级别）的实现  
        &emsp; 在MySQL中，默认的隔离级别是REPEATABLE-READ（可重复读），阻止脏读和不可重复读，并且解决了幻读问题。  
        &emsp; 隔离性（事务的隔离级别）的实现，利用的是锁和MVCC机制。 
        * **<font color = "blue">快照读：</font>**    
        &emsp; 生成一个事务快照（ReadView），之后都从这个快照获取数据。普通select语句就是快照读。  
        &emsp; <font color = "blue">对于快照读，MVCC因为从ReadView读取，所以必然不会看到新插入的行，所以天然就解决了幻读的问题。</font>  
        * **<font color = "clime">当前读：</font>**   
        &emsp; 读取数据的最新版本。常见的update/insert/delete、还有 select ... for update、select ... lock in share mode都是当前读。  
        &emsp; 对于当前读的幻读，MVCC是无法解决的。需要使用Gap Lock或Next-Key Lock（Gap Lock + Record Lock）来解决。  
    * 一致性的实现  
        &emsp; Mysql怎么保证一致性的？这个问题分为两个层面来说。  
        1. 从数据库层面，数据库通过原子性、隔离性、持久性来保证一致性。也就是说ACID四大特性之中，C(一致性)是目的，A(原子性)、I(隔离性)、D(持久性)是手段，是为了保证一致性，数据库提供的手段。数据库必须要实现AID三大特性，才有可能实现一致性。例如，原子性无法保证，显然一致性也无法保证。  
        2. 从应用层面，通过代码判断数据库数据是否有效，然后决定回滚还是提交数据！如果在事务里故意写出违反约束的代码，一致性还是无法保证的。

#### 1.5.4.4. MVCC
1. **<font color = "clime">多版本并发控制（MVCC）是一种用来解决读-写冲突的无锁并发控制。</font>**  
2. <font color = "clime">`MVCC与锁：MVCC主要解决读写问题，锁解决写写问题。`两者结合才能更好的控制数据库隔离性，保证事务正确提交。</font>  
2. **<font color = "clime">InnoDB有两个非常重要的模块来实现MVCC。</font>**   
    * 一个是undo log，用于记录数据的变化轨迹（版本链），用于数据回滚。  
    &emsp; 版本链的生成：在数据库中的每一条记录实际都会存在三个隐藏列：事务ID、行ID、回滚指针，指向undo log记录。  
    *  另外一个是Read View，用于判断一个session对哪些数据可见，哪些不可见。  
    &emsp; **<font color = "red">Read View是用来判断每一个读取语句有资格读取版本链中的哪个记录。所以在读取之前，都会生成一个Read View。然后根据生成的Read View再去读取记录。</font>**  
3. Read View判断可见性的规则：  
    &emsp; Read view 的几个重要属性：   

    * m_ids:当前系统中那些活跃(未提交)的读写事务ID, 它数据结构为一个List。  
    * min_limit_id:表示在生成Read View时，当前系统中活跃的读写事务中最小的事务id，即m_ids中的最小值。  
    * max_limit_id:表示生成Read View时，系统中应该分配给下一个事务的id值。  
    * creator_trx_id: 创建当前Read View的事务ID  

    &emsp; Read view 匹配条件规则如下：
    &emsp; 如果被访问版本的trx_id小于ReadView中的up_limit_id值，表明生成该版本的事务在当前事务生成ReadView前已经提交，所以该版本可以被当前事务访问。  
    &emsp; <font color = "red">如果被访问版本的trx_id属性值在ReadView的up_limit_id和low_limit_id之间，那就需要判断一下trx_id属性值是不是在trx_ids列表中。</font>如果在，说明创建ReadView时生成该版本的事务还是活跃的，该版本不可以被访问；<font color = "clime">如果不在，说明创建ReadView时生成该版本的事务已经被提交，该版本可以被访问。</font>  
    * 如果数据事务ID trx_id < min_limit_id，表明生成该版本的事务在生成Read View前，已经提交(因为事务ID是递增的)，所以该版本可以被当前事务访问。  
    * 如果trx_id>= max_limit_id，表明生成该版本的事务在生成ReadView后才生成，所以该版本不可以被当前事务访问。  
    * 如果 min_limit_id =<trx_id< max_limit_id，需要分3种情况讨论  
        * （1）.如果m_ids包含trx_id,则代表Read View生成时刻，这个事务还未提交，但是如果数据的trx_id等于creator_trx_id的话，表明数据是自己生成的，因此是可见的。  
        * （2）如果m_ids包含trx_id，并且trx_id不等于creator_trx_id，则Read   View生成时，事务未提交，并且不是自己生产的，所以当前事务也是看不见的；
        * （3）.如果m_ids不包含trx_id，则说明你这个事务在Read View生成之前就已经提交了，修改的结果，当前事务是能看见的。
4. 在读取已提交、可重复读两种隔离级别下会使用MVCC。  
    * 读取已提交READ COMMITTED是在`每次执行select操作时`都会生成一次Read View。所以解决不了幻读问题。 
    * 可重复读REPEATABLE READ只有在第一次执行select操作时才会生成Read View，后续的select操作都将使用第一次生成的Read View。
5. MVCC解决了幻读没有？  
        当前读:select...lock in share mode; select...for update;
        当前读:update、insert、delete
    &emsp; 对于当前读的幻读，MVCC是无法解决的。需要使用 Gap Lock 或 Next-Key Lock（Gap Lock + Record Lock）来解决。</font>其实原理也很简单，用上面的例子稍微修改下以触发当前读：select * from user where id < 10 for update。`若只有MVCC，当事务1执行第二次查询时，操作的数据集已经发生变化，所以结果也会错误；`当使用了Gap Lock时，Gap锁会锁住id < 10的整个范围，因此其他事务无法插入id < 10的数据，从而防止了幻读。  

#### 1.5.4.5. MySql锁
1. 数据库锁  
    &emsp; **锁的分类：**  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-42.png)  

    * 按使用方式：乐观锁、悲观锁。  
    * 锁类别：有共享锁(读锁)和排他锁(写锁)。锁类别取决于存储引擎执行的sql语句。  
        ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-47.png)  
    * 按粒度：锁的粒度的不同可以分为表锁、页锁、行锁。  
2. InnoDB共有七种类型的锁：共享/排它锁、意向锁、记录锁（Record lock）、间隙锁（Gap lock）、临键锁（Next-key lock）、插入意向锁、自增锁。  
3. 意向锁（`表级锁`）  
	&emsp; InnoDB 存储引擎表锁：当没有对数据表中的索引数据进行查询时，会执行表锁操作。采用两种意向锁(Intention Locks)。  

	* 意向共享锁(IS)：事务打算给数据行加行共享锁，事务在给一个数据行加共享锁前，必须先取得该表的 IS 锁。  
	* 意向排他锁(IX)：事务打算给数据行加行排他锁，事务在给一个数据行加排他锁前，必须先取得该表的 IX 锁。  
4. **<font color = "red">InnoDB存储引擎的锁的算法有三种：</font>**  
    1. Record lock：单个行记录上的锁。  
    2. Gap lock：间隙锁，锁定一个范围，不包括记录本身。  
    &emsp; **<font color = "red">当使用范围条件（> 、< 、between...）检索数据，InnoDB会给符合条件的已有数据记录的索引项加锁。对于键值在条件范围内但并不存在的记录，叫做“间隙（GAP）”，InnoDB也会对这个“间隙”加锁，这就是间隙锁。</font>**  
    &emsp; **<font color = "red">InnoDB除了通过范围条件加锁时使用间隙锁外，如果使用相等条件请求给一个不存在的记录加锁，InnoDB 也会使用间隙锁。</font>**  
    3. Next-key lock：record+gap锁定一个范围，包含记录本身。  
    &emsp; 临键锁，是记录锁与间隙锁的组合，它的封锁范围，既包含索引记录，又包含索引区间。  
    &emsp; <font color = "red">默认情况下，innodb使用next-key locks来锁定记录。</font><font color = "clime">但当查询的索引含有唯一属性的时候，Next-Key Lock会进行优化，将其降级为Record Lock，即仅锁住索引本身，不是范围。</font>  
5. 插入意向锁  
    &emsp; 对已有数据行的修改与删除，必须加强互斥锁(X锁)，那么对于数据的插入，是否还需要加这么强的锁，来实施互斥呢？插入意向锁，孕育而生。  
    &emsp; 插入意向锁，是间隙锁(Gap Locks)的一种(所以，也是实施在索引上的)，它是专门针对insert操作的。多个事务，在同一个索引，同一个范围区间插入记录时，如果插入的位置不冲突，不会阻塞彼此。  

#### 1.5.4.6. MySql死锁和锁表
&emsp; ~~胡扯，死锁，mysql检测后，回滚一条事务，抛出异常。~~  
1. 为什么发生死锁？  
2. 发生死锁时，服务器报错：`Deadlock found when trying to get to lock; try restarting transaction`。  
3. **<font color = "clime"> 死锁发生了如何解决，MySQL有没有提供什么机制去解决死锁？</font>**  
    1. 发起死锁检测，主动回滚其中一条事务，让其他事务继续执行。  
    2. 设置超时时间，超时后自动释放。  
    &emsp; `在涉及外部锁，或涉及表锁的情况下，InnoDB并不能完全自动检测到死锁，`这需要通过设置锁等待超时参数 innodb_lock_wait_timeout来解决。</font>   
4. **<font color = "clime">如果出现死锁</font>** ，<font color = "clime">可以用`show engine innodb status;`命令来确定最后一个死锁产生的原因。</font>  


### 1.5.5. MySql架构
#### 1.5.5.1. MySql运行流程
1. MySQL整个查询执行过程，总的来说分为5个步骤：  
    1. 客户端请求 ---> 连接器（验证用户身份，给予权限）；  
    2. 查询缓存（存在缓存则直接返回，不存在则执行后续操作）；
    3. 分析器（对SQL进行词法分析和语法分析操作）  ---> 优化器（主要对执行的sql优化选择最优的执行方案方法）；  
    4. 执行器（执行时会先看用户是否有执行权限，有才去使用这个引擎提供的接口）；  
    5. 去引擎层获取数据返回（如果开启查询缓存则会缓存查询结果）。   
2. **<font color = "clime">MySQL服务器主要分为Server层和存储引擎层。</font>**  
	1. <font color = "red">Server层包括连接器、查询缓存、分析器、优化器、执行器等。</font>涵盖MySQL的大多数核心服务功能，以及所有的内置函数（如日期、时间、数学和加密函数等），所有跨存储引擎的功能都在这一层实现，比如存储过程、触发器、视图等，还有 **<font color = "clime">一个通用的日志模块binglog日志模块。</font>**   
	2. `存储引擎：主要负责数据的存储和读取，`采用可以替换的插件式架构，支持 InnoDB、MyISAM、Memory等多个存储引擎，其中InnoDB引擎有自有的日志模块redolog模块。  
3. MySQL更新路程：  
    1. 事务提交前 --- 内存操作：  
        1. 数据加载到缓冲池buffer poll；  
        2. `写回滚日志undo log；`  
        3. 更新缓冲池数据；  
        4. 写redo log buffer。  
    2. 事务提交：`redo log与bin log两阶段提交。`  
    3. 事务提交后：后台线程将buffer poll中数据落盘。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-174.png)  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-183.png)  

#### 1.5.5.2. Server层之binLog日志  
1. **<font color = "clime">binlog是mysql的逻辑日志，并且由Server层进行记录，使用任何存储引擎的mysql数据库都会记录binlog日志。</font>**  
2. 在实际应用中，主要用在两个场景：主从复制和数据恢复。  
3. 写入流程：SQL修改语句先写Binlog Buffer，事务提交时，按照一定的格式刷到磁盘中。  
&emsp; binlog刷盘时机：对于InnoDB存储引擎而言，mysql通过sync_binlog参数控制binlog的刷盘时机。  

#### 1.5.5.3. 存储引擎层
1. **<font color = "red">InnoDB的特性：</font>**    
    * [支持事务](/docs/SQL/transaction.md)  
    * [支持行锁](/docs/SQL/lock.md)，采用[MVCC](/docs/SQL/MVCC.md)来支持高并发  
    * 支持外键  
    * 支持崩溃后的安全恢复  
    * 不支持全文索引  
    * InnoDB 不保存表的具体行数，执行`select count(*) from table`时需要全表扫描。  

#### 1.5.5.4. InnoDB体系结构
&emsp; Innodb体系结构包含后台线程、内存池和磁盘上的结构。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-147.png)  
1. `如果从内存上来看，Change Buffer和Adaptive Hash Index占用的内存都属于Buffer Pool`；redo Log Buffer占用的内存与Buffer Pool独立。`即InnoDB内存主要有两大部分：缓冲池、重做日志缓冲。`  
2. `Buffer Pool有Changer Buffer；Redo Log有Double Write。`  


##### 1.5.5.4.1. InnoDB内存结构-性能
&emsp; 内存中的结构主要包括Buffer Pool，Change Buffer、Adaptive Hash Index以及redo Log Buffer四部分。 **<font color = "blue">如果从内存上来看，[Change Buffer](/docs/SQL/ChangeBuffer.md)和[Adaptive Hash Index](/docs/SQL/AdaptiveHashIndex.md)占用的内存都属于Buffer Pool，redo Log Buffer占用的内存与 [Buffer Pool](/docs/SQL/bufferPoolNew.md)独立。</font>** `即InnoDB内存主要有两大部分：缓冲池、重做日志缓冲。`  

&emsp; 内存数据落盘整体思路分析：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-173.png)  
&emsp; `InnoDB内存缓冲池中的数据page要完成持久化的话，是通过两个流程来完成的，一个是脏页落盘；一个是预写redo log日志。`  

###### 1.5.5.4.1.1. BufferPool
1. 缓冲池是主内存中的一个区域，在InnoDB访问表和索引数据时会在其中进行高速缓存。**在专用服务器上，通常将多达80％的物理内存分配给缓冲池。**  
1. **预读：**   
&emsp; 数据访问，通常都遵循“集中读写”的原则，使用一些数据，大概率会使用附近的数据，这就是所谓的“局部性原理”，它表明提前加载是有效的，确实能够减少磁盘IO。  
&emsp; **<font color = "clime">预读机制能把一些“可能要访问”的页提前加入缓冲池，避免未来的磁盘IO操作；</font>**  
2. **预读失效与缓存污染：**    
&emsp; 预读失效：读取连续的缓存页，将lru链表尾部经常被访问的页清除了。缓存污染：当执行一条 SQL 语句时，如果扫描了大量数据或是进行了全表扫描，从而将缓冲池中已存在的所有页替换出去。  
3. **读操作：改进的lru算法：**    
&emsp; **<font color = "clime">为了提高缓存命中率，InnoDB 在传统 Lru 算法的基础上做了优化，解决了两个问题：1、预读失效 2、缓存池污染。</font>**   
&emsp; `将LRU链表分为两部分，一部分为热数据区域，一部分为冷数据区域。`当数据页第一次被加载到缓冲池中的时候，先将其放到冷数据区域的链表头部，1s（由 innodb_old_blocks_time 参数控制） 后该缓存页被访问了再将其移至热数据区域的链表头部。  
5. **写操作：**    
&emsp; **Buffer pool 另一个主要的功能是「加速写」，即当需要修改一个页面的时候，先将这个页面在缓冲池中进行修改，记下相关的重做日志，这个页面的修改就算已经完成了。**  


###### 1.5.5.4.1.2. 写缓冲ChangeBuffer
1. 在「非唯一」「普通」索引页（即非聚集索引）不在缓冲池中，对页进行了写操作， 1). 并不会立刻将磁盘页加载到缓冲池，而仅仅记录缓冲变更， 2).`等未来数据被读取时，再将数据合并(merge)恢复到缓冲池中`的技术。  
2. **~~<font color = "red">如果辅助索引页已经在缓冲区了，则直接修改即可；如果不在，则先将修改保存到 Change Buffer。</font><font color = "blue">Change Buffer的数据在对应辅助索引页读取到缓冲区时合并到真正的辅助索引页中。Change Buffer 内部实现也是使用的 B+ 树。</font>~~**  

###### 1.5.5.4.1.3. AdaptiveHashIndex
&emsp;对于InnoDB的哈希索引，确切的应该这么说：  
&emsp;(1)InnoDB用户无法手动创建哈希索引，这一层上说，InnoDB确实不支持哈希索引；  
&emsp;(2)InnoDB会自调优(self-tuning)，如果判定建立自适应哈希索引(Adaptive Hash Index, AHI)，能够提升查询效率，InnoDB自己会建立相关哈希索引，这一层上说，InnoDB又是支持哈希索引的。  

##### 1.5.5.4.2. InnoDB磁盘结构-可靠性
###### 1.5.5.4.2.1. BufferPool落盘表空间
1. 从InnoDb存储引擎的逻辑存储结构看，所有数据都被逻辑地存放在一个空间中，称之为表空间tablespace。表空间又由段segment，区extent，页page组成。  
2. **<font color = "clime">相比较之下，使用独占表空间的效率以及性能会更高一点。</font>**  
3. **<font color = "clime">在InnoDB存储引擎中，默认每个页的大小为16KB（在操作系统中默认页大小是4KB）。</font>**  

###### 1.5.5.4.2.2. undoLog
1. **<font color = "clime">Undo log，回滚日志，是`逻辑日记`。undo log解决了事务原子性。</font>** 主要有两个作用，事务回滚和MVCC（Mutil-Version Concurrency Control）。      
2. undo log主要记录了数据的逻辑变化，比如一条INSERT语句，对应一条DELETE的undo log，对于每个UPDATE语句，对应一条相反的UPDATE的undo log，这样在发生错误时，就能回滚到事务之前的数据状态。
3. 事务开始之前，将当前的版本生成undo log。

###### 1.5.5.4.2.3. redoLog
1. redo log，物理格式的日志，记录的是物理数据页面的修改的信息。 **<font color = "red">`redo log实际上记录数据页的变更，而这种变更记录是没必要全部保存，`因此redo log实现上采用了大小固定，`循环写入`的方式，当写到结尾时，会回到开头循环写日志。</font>**    
2. 解决事务的一致性，持久化数据。  
3. 写入流程：`(Write-Ahead Logging，‘日志’先行)`   
&emsp; 在计算机体系中，CPU处理速度和硬盘的速度，是不在同一个数量级上的，为了让它们速度匹配，从而催生了内存模块，但是内存有一个特点，就是掉电之后，数据就会丢失，不是持久的，我们需要持久化的数据，最后都需要存储到硬盘上。InnoDB引擎设计者也利用了类似的设计思想。   
&emsp; 当有一条记录需要更新的时候，InnoDB引擎就会先把记录写到redo log(redolog buffer)里面，并更新内存(buffer pool)，这个时候更新就算完成了。`同时，InnoDB引擎会在适当的时候，`将这个redoLog操作记录更新到磁盘里面（刷脏页）。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-184.png)  
4. 刷盘时机：重做日志的写盘，并不一定是随着事务的提交才写入重做日志文件的，而是随着事务的开始，逐步开始的。先写入redo log buffer。  

###### 1.5.5.4.2.4. DoubleWrite
&emsp; double write：<font color = "blue">如果说写缓冲change buffer带给InnoDB存储引擎的是性能，那么两次写Double Write带给InnoDB存储引擎的是数据的可靠性。</font>  
1. MySQL将buffer中一页数据刷入磁盘，要写4个文件系统里的页。  
2. 在应用(apply)重做日志(redo log)前，需要一个页的副本，当`写入失效发生时`，`先通过页的副本来还原该页，再进行重做`，这就是doublewrite。即doublewrite是页的副本。  
    1. 在异常崩溃时，如果不出现“页数据损坏”，能够通过redo恢复数据；
    2. 在出现“页数据损坏”时，能够通过double write buffer恢复页数据； 
3. doublewrite分为内存和磁盘的两层架构。当有页数据要刷盘时：  
    1. 第一步：页数据先memcopy到doublewrite buffer的内存里；
    2. 第二步：doublewrite buffe的内存里，会先刷到doublewrite buffe的磁盘上；
    3. 第三步：doublewrite buffe的内存里，再刷到数据磁盘存储上； 

##### 1.5.5.4.3. ~~两阶段提交和崩溃恢复~~
1. 两阶段提交
    1. **<font color = "clime">redo log和binlog都可以用于表示事务的提交状态，而两阶段提交就是让这两个状态保持逻辑上的一致。两阶段提交保证解决binlog和redo log的数据一致性。</font>**    
    2. `两阶段提交是很典型的分布式事务场景，因为redolog和binlog两者本身就是两个独立的个体，`要想保持一致，就必须使用分布式事务的解决方案来处理。 **<font color = "blue">而将redolog分成了两步，其实就是使用了两阶段提交协议(Two-phaseCommit，2PC)。</font>**  
    &emsp; 事务的提交过程有两个阶段，就是将redolog的写入拆成了两个步骤：prepare和commit，中间再穿插写入binlog。  
        1. 记录redolog，InnoDB事务进入prepare状态；
        2. 写入binlog；
        3. 将redolog这个事务相关的记录状态设置为commit状态。
2. 崩溃恢复： **<font color = "red">当重启数据库实例的时候，数据库做2个阶段性操作：redo log处理，undo log及binlog 处理。在崩溃恢复中还需要回滚没有提交的事务，提交没有提交成功的事务。由于回滚操作需要undo日志的支持，undo日志的完整性和可靠性需要redo日志来保证，所以崩溃恢复先做redo前滚，然后做undo回滚。</font>**    
