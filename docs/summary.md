
<!-- TOC -->

- [1. 总结](#1-总结)
    - [1.1. Java基础](#11-java基础)
    - [1.2. 设计模式](#12-设计模式)
    - [1.3. JVM](#13-jvm)
        - [1.3.1. JDK、JRE、JVM](#131-jdkjrejvm)
        - [1.3.2. 编译成Class字节码文件](#132-编译成class字节码文件)
        - [1.3.3. 类加载](#133-类加载)
            - [1.3.3.1. JVM类的加载](#1331-jvm类的加载)
            - [1.3.3.2. JVM类加载器](#1332-jvm类加载器)
        - [1.3.4. 内存结构](#134-内存结构)
            - [1.3.4.1. JVM内存结构](#1341-jvm内存结构)
                - [1.3.4.1.1. JVM内存结构](#13411-jvm内存结构)
                - [1.3.4.1.2. 常量池详解](#13412-常量池详解)
                - [1.3.4.1.3. 逃逸分析](#13413-逃逸分析)
            - [1.3.4.2. 内存中的对象](#1342-内存中的对象)
            - [1.3.4.3. 内存泄露](#1343-内存泄露)
        - [1.3.5. JVM执行](#135-jvm执行)
        - [1.3.6. GC](#136-gc)
            - [1.3.6.1. GC-回收对象](#1361-gc-回收对象)
            - [1.3.6.2. GC-回收位置/安全点](#1362-gc-回收位置安全点)
            - [1.3.6.3. 回收算法与分代回收](#1363-回收算法与分代回收)
            - [1.3.6.4. GC-垃圾回收器](#1364-gc-垃圾回收器)
                - [1.3.6.4.1. ~~垃圾回收器~~](#13641-垃圾回收器)
                - [1.3.6.4.2. CMS回收器](#13642-cms回收器)
                - [1.3.6.4.3. G1回收器](#13643-g1回收器)
                - [1.3.6.4.4. 三色标记](#13644-三色标记)
        - [1.3.7. JVM调优](#137-jvm调优)
            - [1.3.7.1. JVM调优-基础](#1371-jvm调优-基础)
            - [1.3.7.2. JVM调优](#1372-jvm调优)
            - [1.3.7.3. JVM问题排查](#1373-jvm问题排查)
            - [1.3.7.4. Arthas工具](#1374-arthas工具)
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
        - [1.5.5. 索引事物锁](#155-索引事物锁)
            - [1.5.5.1. 索引底层原理](#1551-索引底层原理)
            - [1.5.5.2. 各种索引](#1552-各种索引)
            - [1.5.5.3. MySql事务](#1553-mysql事务)
            - [1.5.5.4. MySql锁](#1554-mysql锁)
            - [1.5.5.5. MySql死锁和锁表](#1555-mysql死锁和锁表)
        - [1.5.6. MySql架构原理](#156-mysql架构原理)
            - [1.5.6.1. MySql架构](#1561-mysql架构)
            - [1.5.6.2. binLog日志](#1562-binlog日志)
            - [1.5.6.3. MySql存储引擎](#1563-mysql存储引擎)
            - [1.5.6.4. InnoDB体系结构](#1564-innodb体系结构)
                - [1.5.6.4.1. InnoDB内存结构-性能](#15641-innodb内存结构-性能)
                    - [1.5.6.4.1.1. BufferPool](#156411-bufferpool)
                    - [1.5.6.4.1.2. ChangeBuffer](#156412-changebuffer)
                    - [1.5.6.4.1.3. AdaptiveHashIndex](#156413-adaptivehashindex)
                - [1.5.6.4.2. InnoDB磁盘结构-可靠性](#15642-innodb磁盘结构-可靠性)
                    - [1.5.6.4.2.1. BufferPool落盘表空间](#156421-bufferpool落盘表空间)
                    - [1.5.6.4.2.2. undoLog](#156422-undolog)
                    - [1.5.6.4.2.3. redoLog](#156423-redolog)
                    - [1.5.6.4.2.4. DoubleWrite](#156424-doublewrite)
                - [1.5.6.4.3. 两阶段提交和崩溃恢复](#15643-两阶段提交和崩溃恢复)
    - [1.6. 项目构建](#16-项目构建)
        - [1.6.1. 接口幂等](#161-接口幂等)
        - [1.6.2. 接口响应时间](#162-接口响应时间)
        - [1.6.3. 接口预警](#163-接口预警)
    - [1.7. 架构设计](#17-架构设计)
        - [1.7.1. 架构质量属性](#171-架构质量属性)
        - [1.7.2. 系统瓶颈](#172-系统瓶颈)
    - [1.8. Spring](#18-spring)
        - [1.8.1. Spring基础](#181-spring基础)
        - [1.8.2. Spring IOC](#182-spring-ioc)
        - [1.8.3. Spring DI](#183-spring-di)
            - [1.8.3.1. 循环依赖](#1831-循环依赖)
        - [1.8.4. Bean的生命周期](#184-bean的生命周期)
        - [1.8.5. 容器相关特性](#185-容器相关特性)
            - [1.8.5.1. FactoryBean](#1851-factorybean)
            - [1.8.5.2. 可二次开发常用接口](#1852-可二次开发常用接口)
                - [1.8.5.2.1. 事件](#18521-事件)
                - [1.8.5.2.2. Aware接口](#18522-aware接口)
                - [1.8.5.2.3. 后置处理器](#18523-后置处理器)
                - [1.8.5.2.4. InitializingBean](#18524-initializingbean)
        - [1.8.6. AOP基本概念](#186-aop基本概念)
        - [1.8.7. SpringAOP解析](#187-springaop解析)
        - [1.8.8. Spring事务](#188-spring事务)
        - [1.8.9. SpringMVC解析](#189-springmvc解析)
        - [1.8.10. 过滤器、拦截器、监听器](#1810-过滤器拦截器监听器)
    - [1.9. MyBatis](#19-mybatis)
        - [1.9.1. MyBatis架构](#191-mybatis架构)
        - [1.9.2. MyBatis SQL执行解析](#192-mybatis-sql执行解析)
        - [1.9.3. MyBatis缓存](#193-mybatis缓存)
        - [1.9.4. MyBatis插件解析](#194-mybatis插件解析)

<!-- /TOC -->


# 1. 总结  

## 1.1. Java基础

## 1.2. 设计模式

## 1.3. JVM
### 1.3.1. JDK、JRE、JVM
1. <font color = "red">JVM由4大部分组成：类加载器ClassLoader，运行时数据区Runtime Data Area，执行引擎Execution Engine，本地方法调用Native Interface。</font>  
2. **<font color = "clime">JVM各组件的作用（JVM执行程序的过程）：</font>**   
    1. 首先通过类加载器（ClassLoader）会把Java代码转换成字节码；  
    2. 运行时数据区（Runtime Data Area）再把字节码加载到内存中；  
    3. <font color = "red">而字节码文件只是JVM的一套指令集规范，并不能直接交给底层操作系统去执行，因此需要特定的命令解析器执行引擎（Execution Engine），将字节码翻译成底层系统指令，再交由CPU去执行；</font>  
    4. 而这个过程中需要调用其他语言的本地库接口（Native Interface）来实现整个程序的功能。  


### 1.3.2. 编译成Class字节码文件

### 1.3.3. 类加载
#### 1.3.3.1. JVM类的加载
1. 类加载流程：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-5.png)  
2. 验证：确保被加载的类的正确性。验证阶段大致会完成4个阶段的检验动作：1. 文件格式验证、2. 元数据验证、3. 字节码验证、4. 符号引用验证。  
3. 准备(Preparation)  
&emsp; <font color = "red">为类的静态变量分配内存，并将其初始化为默认值，这些内存都将在方法区中分配。</font>对于该阶段有以下几点需要注意：  
    1. <font color = "red">这时候进行内存分配的仅包括类变量(static)，而不包括实例变量，实例变量会在对象实例化时随着对象一块分配在Java堆中。</font>  
    2. <font color = "red">这里所设置的初始值"通常情况"下是数据类型默认的零值(如0、0L、null、false等)，比如定义了public static int value=111 ，那么 value 变量在准备阶段的初始值就是 0 而不是111(初始化阶段才会复制)。</font>  
    * <font color = "red">特殊情况：比如给value变量加上了fianl关键字public static final int value=111，那么准备阶段value的值就被赋值为 111。</font>  
4. 解析(Resolution)： **<font color = "red">将常量池内的符号引用转换为直接引用</font>** ，得到类或者字段、方法在内存中的指针或者偏移量，确保类与类之间相互引用正确性，完成内存结构布局，以便直接调用该方法。  
&emsp; `为什么要用符号引用呢？` **<font color = "blue">这是因为类加载之前，javac会将源代码编译成.class文件，这个时候javac是不知道被编译的类中所引用的类、方法或者变量它们的引用地址在哪里，所以只能用符号引用来表示。</font>**  
&emsp; **<font color = "clime">解析过程在某些情况下可以在初始化阶段之后再开始，这是为了支持Java的动态绑定。</font>**   


#### 1.3.3.2. JVM类加载器
1. JVM默认提供三个类加载器：启动类加载器、扩展类加载器、应用类加载器。  
&emsp; 双亲委派模型中，类加载器之间的父子关系一般不会以继承（Inheritance）的关系来实现，而是使用组合（Composition）关系来复用父加载器的代码的。      
2. 双亲委派模型，一个类加载器首先将类加载请求转发到父类加载器，只有当父类加载器无法完成时才尝试自己加载。  
&emsp; 好处：避免类的重复加载；防止核心API被随意篡改。   
3. 破坏双亲委派模型的案例：  
    1. `双亲委派模型有一个问题：顶层ClassLoader无法加载底层ClassLoader的类，典型例子JNDI、JDBC。`
        * **<font color = "clime">JDBC是启动类加载器加载，但 mysql 驱动是应用类加载器。所以加入了线程上下文类加载器(Thread Context ClassLoader)，</font>** 可以通过Thread.setContextClassLoaser()设置该类加载器，然后顶层ClassLoader再使用Thread.getContextClassLoader()获得底层的ClassLoader进行加载。  
    2. Tomcat中使用了自定义ClassLoader，使得一个Tomcat中可以加载多个应用。一个Tomcat可以部署N个web应用，但是每个web应用都有自己的classloader，互不干扰。比如web1里面有com.test.A.class，web2里面也有com.test.A.class，如果没打破双亲委派模型的话，那么web1加载完后，web2再加载的话会冲突。    
    3. ......  

### 1.3.4. 内存结构
#### 1.3.4.1. JVM内存结构
##### 1.3.4.1.1. JVM内存结构
1. 运行时数据区。线程独享：程序计数器、JVM栈、本地方法栈；线程共享区：堆、方法区（元空间）。  
2. Java虚拟机栈是由一个个栈帧组成，每个栈帧中都拥有：局部变量表、操作数栈、动态链接、方法出口信息。局部变量表存储八大原始类型、对象引用、returnAddress。 
3. 堆  
    1. 1). 堆分为新生代、老年代，默认比例1: 2； 2). 新生代又按照8: 1: 1划分为Eden区和两个Survivor区。  
    2. **<font color = "blue">在Eden区中，JVM为每个线程分配了一个私有缓存区域[TLAB(Thread Local Allocation Buffer)](/docs/java/JVM/MemoryObject.md)。</font>**    
    3. 堆是分配对象存储的唯一选择吗？[逃逸分析](/docs/java/JVM/escape.md)  
4. <font color = "clime">方法区的演进：</font>  
    1. 为什么JDK1.8移除元空间  
        1. 由于PermGen内存经常会溢出，引发java.lang.OutOfMemoryError: PermGen，因此JVM的开发者希望这一块内存可以更灵活地被管理，不要再经常出现这样的OOM。  
        2. 移除PermGen可以促进HotSpot JVM与JRockit VM的融合，因为JRockit没有永久代。  
    2. 演进历程：  
        * jdk1.6及之前：有永久代(permanent generation)，静态变量存放在永久代上。  
        * jdk1.7：有永久代，但已经逐步“去永久代”，[字符串常量池](/docs/java/JVM/ConstantPool.md) <font color = "red">、静态变量</font>移除，保存在堆中。  
        * jdk1.8及之后：无永久代，类型信息、字段、方法、<font color = "red">常量</font>保存在本地内存的元空间，<font color = "clime">但字符串常量池、静态变量仍在堆。</font>  


##### 1.3.4.1.2. 常量池详解
&emsp; **<font color = "clime">常量池分为以下三种：class文件常量池、运行时常量池、全局字符串常量池。</font>**   

##### 1.3.4.1.3. 逃逸分析
1. <font color = "red">通过逃逸分析算法可以分析出某一个方法中的某个对象是否会被其它方法或者线程访问到。</font>如果分析结果显示某对象并不会被其他方法引用或被其它线程访问，则有可能在编译期间做一些深层次的优化。   
2. 对于NoEscape（ **<font color = "clime">没有逃逸</font>** ）状态的对象，则不一定，具体会有这种优化情况：   
    1. 对象可能分配在栈上。  
    2. 分离对象或标量替换。  
    &emsp; **<font color = "clime">在HotSpot中并没有真正的实现"栈"中分配对象的功能，取而代之的是一个叫做"标量替换"的折中办法。</font>**  
    &emsp; 什么是标量？标量，不可再分，基本数据类型；相对的是聚合量，可再分，引用类型。  
    &emsp; **当JVM通过逃逸分析，确定要将对象分配到栈上时，即时编译可以将对象打散，将对象替换为一个个很小的局部变量，将这个打散的过程叫做标量替换。** 
    3. 消除同步锁

#### 1.3.4.2. 内存中的对象
1. **<font color = "clime">对象创建过程：1. 检测类是否被加载；2. 为对象分配内存；3. 将分配内存空间的对象初始化零值；4. 对对象进行其他设置；5.执行init方法。</font>**   
2. 为对象分配内存：
    * 分配内存两种方式：指针碰撞（内存空间绝对规整）；空闲列表（内存空间是不连续的）。
        * 标记-整理或复制 ---> 空间规整 ---> 指针碰撞； 
        * 标记-清除 ---> 空间不规整 ---> 空闲列表。       
    * 线程安全问题：1).采用CAS； **<font color = "clime">2).线程本地分配缓冲（TLAB）。</font>**  
    * **<font color = "blue">TLAB详解：</font>**  
        * 线程本地分配缓存，这是一个线程专用的内存分配区域。可以加速对象的分配。TLAB是在堆中开辟的内存区域。默认情况下，TLAB空间的内存非常小，仅占有整个Eden空间的1%。  
        * **<font color = "blue">TLAB通常很小，所以放不下大对象。`JVM设置了最大浪费空间`。</font>**  
        &emsp; 当大对象申请内存时，当剩余的空间小于最大浪费空间，那该TLAB属于的线程在重新向Eden区申请一个TLAB空间。进行对象创建，还是空间不够，那这个对象太大了，去Eden区直接创建吧！  
        &emsp; 当剩余的空间大于最大浪费空间，那这个大对象直接去Eden区创建。剩余空间还需要使用。
    * **<font color = "blue">`内存分配全流程：`逃逸分析 ---> 没有逃逸，尝试栈上分配 ---> 是否满足直接进入老年代的条件 ---> 尝试TLAB分配 ---> Eden分配。</font>**  
3. 堆内存分配策略：  
&emsp; 分配策略有：对象优先在Eden分配、大对象直接进入老年代、长期存活的对象将进入老年代、动态对象年龄判定、空间分配担保。  
&emsp; 空间分配担保： **<font color = "clime">JVM在发生Minor GC之前，虚拟机会检查老年代最大可用的`连续空间`是否大于新生代所有对象的总空间。</font>**   

#### 1.3.4.3. 内存泄露
1. 内存溢出与内存泄露  
&emsp; **<font color = "red">内存溢出out of memory</font>** ，是指<font color = "red">程序在申请内存时，`没有足够的内存空间供其使用`</font>，出现out of memory。  
&emsp; **<font color = "blue">内存泄露memory leak</font>** ，是指<font color = "red">程序在申请内存后，`无法释放已申请的内存空间`</font>。一次内存泄露危害可以忽略，但内存泄露堆积后果很严重，无论多少内存，迟早会被占光。内存泄露，会导致频繁的Full GC。  
&emsp; 所以内存泄漏可能会导致内存溢出，但内存溢出并不完全都是因为内存泄漏，也有可能使用了太多的大对象导致。  
2. 内存溢出影响  
&emsp; **<font color = "clime">问题：`JVM堆内存溢出后，其他线程是否可继续工作？`</font>**  
&emsp; 当一个线程抛出OOM异常后，它所占据的内存资源会全部被释放掉，从而不会影响其他线程的运行！  
&emsp; **<font color = "red">其实发生OOM的线程一般情况下会死亡，也就是会被终结掉，该线程持有的对象占用的heap都会被gc了，释放内存。</font><font color = "clime">因为发生OOM之前要进行gc，就算其他线程能够正常工作，也会因为频繁gc产生较大的影响。</font>**  


### 1.3.5. JVM执行

### 1.3.6. GC
#### 1.3.6.1. GC-回收对象
1. 堆中对象的存活：  
	1. 存活标准
		1. 引用计数法、根可达性分析法  
			1. **<font color = "clime">不可回收对象包含 1). 方法区中，类静态属性(static)引用的对象； 2). 方法区中，常量(final static)引用的对象；</font>** 
			2. `由以上可得java 全局变量 不可被回收。`  
		2. 四种引用  
		&emsp; **<font color = "red">软引用：当堆使用率临近阈值时，才会去回收软引用的对象。</font>**  
		&emsp; **<font color = "red">弱引用：只要发现弱引用，不管系统堆空间是否足够，都会将对象进行回收。</font>**  
		&emsp; **软引用和弱引用的使用：**  
		&emsp; **<font color = "red">软引用，弱引用都非常适合来保存那些可有可无的缓存数据，如果这么做，当系统内存不足时，这些缓存数据会被回收，不会导致内存溢出。而当内存资源充足时，这些缓存数据又可以存在相当长的时间，从而起到加速系统的作用。</font>**  
		&emsp; **<font color = "clime">假如⼀个应⽤需要读取⼤量的本地图⽚，如果每次读取图⽚都从硬盘读取会严重影响性能，如果⼀次性全部加载到内存⼜可能造成内存溢出，这时可以⽤软引⽤解决这个问题。</font>**  
	2. 对象生存还是死亡？  
	&emsp; **<font color = "clime">如果有必要执行父类`Object#finalize()`方法，放入F-Queue队列；收集器将对F-Queue队列中的对象进行第二次小规模的标记；如果对象在执行finalize()方法时重新与引用链上的任何一个对象建立关联则逃脱死亡，否则执行死亡。</font>**  
2. null与GC：  
&emsp; 《深入理解Java虚拟机》作者的观点：在需要“不使用的对象应手动赋值为null”时大胆去用，但不应当对其有过多依赖，更不能当作是一个普遍规则来推广。  
&emsp; **<font color = "red">虽然代码片段已经离开了变量xxx的`作用域`，但在此之后，没有任何对运行时栈的读写，placeHolder所在的索引还没有被其他变量重用，所以GC判断其为存活。</font>**    
&emsp; 加上`int replacer = 1;`和将placeHolder赋值为null起到了同样的作用：断开堆中placeHolder和栈的联系，让GC判断placeHolder已经死亡。    
&emsp; “不使用的对象应手动赋值为null”的原理，一切根源都是来自于JVM的一个“bug”：代码离开变量作用域时，并不会自动切断其与堆的联系。    


#### 1.3.6.2. GC-回收位置/安全点
1. 安全点  
&emsp; **<font color = "clime">可达性分析算法必须是在一个确保一致性的内存快照中进行。</font>**   
&emsp; **<font color = "clime">安全点意味着在这个点时，所有工作线程的状态是确定的，JVM可以安全地执行GC。</font>**  
2. 安全区域  
&emsp; `在安全点上中断的是活跃运行的用户线程，对于已经挂起的线程该怎么处理呢？`  
&emsp; **<font color = "blue">已经挂起的线程会被认定为处在安全区域内，中断的时候不需要考虑安全区域中的线程。</font>**  
&emsp; 当前安全区域的线程要被唤醒离开安全区域时，先检查能否离开，如果GC完成了，那么线程可以离开，否则它必须等待直到收到安全离开的信号为止。  


#### 1.3.6.3. 回收算法与分代回收
1. GC算法  
    * **<font color = "clime">标记-清除算法分为两个阶段：标记阶段和清除阶段。</font>** 不足：清除过程中，扫描两次，效率不高；清除后，产生空间碎片。  
    * `复制：1).（非标记-复制）只扫描一次；` 2). 没有碎片，空间连续； 3). 50%的内存空间始终空闲浪费。  
    * 标记-整理：1). 没有碎片，空间连续； 2). 不会产生内存减半； 3). 扫描两次，指针需要调整(移动对象)，效率偏低。  
    &emsp; **<font color = "clime">标记-清除和标记-整理都需要扫描两次。</font>**   
2. 新生代采用复制算法；老年代采用标记-整理算法。 **<font color = "clime">注意：CMS回收老年代，但采用标记-清除算法；CMS收集器也会在内存空间的碎片化程度已经大到影响对象分配时，采用标记-整理算法收集一次（晋升失败(promotion failed) 或 并发模式失败(concurrent mode failure)），以获得规整的内存空间。</font>**    
3. 分代回收流程  
4. 跨代引用假说（跨代引用相对于同代引用仅占少数）  
&emsp; **既然跨代引用只是少数，那么就没必要去扫描整个老年代，也不必专门记录每一个对象是否存在哪些跨代引用，只需在新生代上建立一个全局的数据结构，称为记忆集(Remembered Set)，这个结构把老年代划分为若干个小块，标识出老年代的哪一块内存会存在跨代引用。此后当发生Minor GC时，只有包含了跨代引用的小块内存里的对象才会被加入GC Roots进行扫描。**  
&emsp; ~~跨代引用假说的具体解决办法是：在新生代上建立一个全局的数据结构(该结构被称为“记忆集”，Remembered Set)，这个结构把老年代划分成若干小块，标识出老年代的哪一块内存会存在跨代引用。此后当发生Minor GC时，只有包含了跨代引用的小块内存里的对象才会被加入到GC Roots进行扫描。~~  
5. Full GC  
&emsp; **<font color = "red">Full GC的触发时机：（老年代或永久代不足 ---> 老年代不满足年轻代晋升 ---> 回收器(例如CMS)---> 系统调用 ）</font>**   
    1. 老年代或永久的不足
        1. 老年代空间不足(92%)  
        &emsp; 老年代空间不足的常见场景为大对象直接进入老年代、长期存活的对象进入老年代等。  
        &emsp; 为了避免以上原因引起的Full GC，应当尽量不要创建过大的对象以及数组。除此之外，可以通过-Xmn虚拟机参数调大新生代的大小，让对象尽量在新生代被回收掉，不进入老年代。还可以通过 -XX:MaxTenuringThreshold调大对象进入老年代的年龄，让对象在新生代多存活一段时间。  
        2. JDK 1.7及以前的永久代空间不足  
        &emsp; 为避免以上原因引起的Full GC，可采用的方法为增大永久代空间或转为使用CMS GC。  
    2. 老年代不满足年轻代晋升  
        1. 统计得到的Minor GC晋升到旧生代的平均大小大于旧生代的剩余空间  
        &emsp; Hotspot为了避免由于新生代对象晋升到旧生代导致旧生代空间不足的现象，在进行Minor GC时，做了一个判断，如果之前统计所得到的Minor GC晋升到旧生代的平均大小大于旧生代的剩余空间，那么就直接触发Full GC。  
        2. 空间分配担保失败  
        &emsp; **<font color = "clime">JVM在发生Minor GC之前，虚拟机会检查老年代最大可用的`连续空间`是否大于新生代所有对象的总空间，</font>** 如果大于，则此次Minor GC是安全的；如果小于，则虚拟机会查看HandlePromotionFailure设置项的值是否允许担保失败。如果HandlePromotionFailure=true，那么会继续检查老年代最大可用连续空间是否大于历次晋升到老年代的对象的平均大小，如果大于则尝试进行一次Minor GC，但这次Minor GC依然是有风险的；如果小于或者HandlePromotionFailure=false，则改为进行一次Full GC。   
    3. CMS GC时出现promotion failed和concurrent mode failure  
    &emsp; 执行CMS GC的过程中同时有对象要放入老年代，而此时老年代空间不足（可能是GC过程中浮动垃圾过多导致暂时性的空间不足），便会报Concurrent Mode Failure错误，并触发Full GC。  
    4. <font color = "red">系统调用System.gc()</font>  
    &emsp; 只是建议虚拟机执行Full GC，但是虚拟机不一定真正去执行。不建议使用这种方式，而是让虚拟机管理内存。  



#### 1.3.6.4. GC-垃圾回收器
##### 1.3.6.4.1. ~~垃圾回收器~~


##### 1.3.6.4.2. CMS回收器
1. **<font color = "clime">CMS在某些阶段是并发，即CMS GC时并不是全部并发执行。大部分并发，但也有停顿(STW)，只是停顿时间更少。因为CMS是并发收集器，为了不影响用户线程使用，所以采用标记-清除算法。</font>**   
2. CMS GC执行流程：(**<font color = "clime">3次标记、2次清除</font>**)  
    1. 初始标记：标记GCRoots能直接关联到的对象。   
    2. 并发标记：进行GCRoots Tracing(可达性分析)过程，GC与用户线程并发执行。
    3. 预清理：（`三色标记法的漏标问题处理`） **<font color = "red">这个阶段是用来</font><font color = "blue">处理</font><font color = "clime">前一个并发标记阶段因为引用关系改变导致没有标记到的存活对象的。如果发现对象的引用发生变化，则JVM会标记堆的这个区域为Dirty Card。那些能够从Dirty Card到达的对象也被标记（标记为存活），当标记做完后，这个Dirty Card区域就会消失。</font>**  
    4. 可终止的预处理。这个阶段尝试着去承担下一个阶段Final Remark阶段足够多的工作。  
    5. 重新标记（remark）：修正并发标记期间，因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录。
    6. 并发清除：并发、标记-清除，GC与用户线程并发执行。   
    7. 并发重置。
3. `CMS的特点：`  
    1. 划时代的并发收集器。`关注停顿时间。`    
    2. `吞吐量低。`并发执行，线程切换。  
    3. **<font color = "blue">并发执行，`产生浮动垃圾（参考三色标记法中“错标”）`。</font>**  
    4. 使用"标记-清除"算法，产生空间碎片。CMS GC在老生代回收时产生的内存碎片会导致老生代的利用率变低；或者可能在老生代总内存大小足够的情况下，却不能容纳新生代的晋升行为（由于没有连续的内存空间可用），导致触发FullGC。针对这个问题，`Sun官方给出了以下解决内存碎片问题的方法：`  
        * 增大Xmx或者减少Xmn  
        * `在应用访问量最低的时候，在程序中主动调用System.gc()，比如每天凌晨。`  
        * 在应用启动并完成所有初始化工作后，主动调用System.gc()，它可以将初始化的数据压缩到一个单独的chunk中，以腾出更多的连续内存空间给新生代晋升使用。  
        * `降低-XX:CMSInitiatingOccupancyFraction参数（内存占用率，默认70%）以提早执行CMS GC动作，`虽然CMSGC不会进行内存碎片的压缩整理，但它会合并老生代中相邻的free空间。这样就可以容纳更多的新生代晋升行为。 
        * CMS收集器提供了一个-XX：+UseCMS-CompactAtFullCollection开关参数（默认是开启的，此参数从JDK 9开始废弃），用于在CMS收集器不得不进行Full GC时开启内存碎片的合并整理过程。`还提供了另外一个参数-XX：CMSFullGCsBefore-Compaction（此参数从JDK 9开始废弃），这个参数的作用是要求CMS收集器在执行过若干次（数量由参数值决定）不整理空间的Full GC之后，下一次进入Full GC前会先进行碎片整理（默认值为0，表示每次进入Full GC时都进行碎片整理）。`  
    5. 晋升失败与并发模式失败：都会退化成单线程的Full GC。  
        * 晋升失败(promotion failed)：当新生代发生垃圾回收， **老年代有足够的空间可以容纳晋升的对象，但是由于空闲空间的碎片化，导致晋升失败。** ~此时会触发单线程且带压缩动作的Full GC。~  
        * 并发模式失败(concurrent mode failure)：当CMS在执行回收时，新生代发生垃圾回收，同时老年代又没有足够的空间容纳晋升的对象时。CMS垃圾回收会退化成单线程的Full GC。所有的应用线程都会被暂停，老年代中所有的无效对象都被回收。  
    6. 减少remark阶段停顿：在执行并发操作之前先做一次Young GC。  


##### 1.3.6.4.3. G1回收器
1. G1，垃圾优先，可预测停顿模型。 **<font color = "clime">G1在大多数情况下可以实现指定的GC暂停时间，同时还能保持较高的吞吐量。</font>**    
2. G1逻辑分代但物理不分代，将整个Java堆划分为多个大小相等的独立区域(Region)。E区、S区、H区、O区。  
3. G1的收集过程可能有4个阶段：新生代GC、老年队并发标记周期、混合回收、如果需要可能会进行Full GC。   
4. 老年队并发标记周期  
&emsp; **<font color = "clime">当整个堆内存（包括老年代和新生代）被占满一定大小的时候（默认是45%，可以通过-XX:InitiatingHeapOccupancyPercent进行设置），老年代回收过程会被启动。</font>**  
&emsp; **<font color = "clime">老年队并发标记周期，回收百分之百为垃圾的内存分段，</font>** H区（本质是o区）Humongous对象会独占整个内存分段。  
5. 混合回收MixGC  
&emsp; 老年代并发标记过程结束以后，紧跟着就会开始混合回收过程。混合回收的意思是年轻代和老年代会同时被回收。  
&emsp; **<font color = "blue">步骤分2步：全局并发标记（global concurrent marking）、拷贝存活对象（evacuation）。</font>**  
    1. 全局并发标记  
        1. 初始标记
        2. 根区域扫描
        3. 并发标记
        4. 最终标记： **<font color = "blue">去处理剩下的SATB(开始快照)日志缓冲区和所有更新，找出所有未被访问的存活对象，同时安全完成存活数据计算。</font>**   
        5. 清除垃圾


##### 1.3.6.4.4. 三色标记
1. 三色：  
    * 黑色：本对象已访问过，而且本对象 引用到 的其他对象 也全部访问过了。  
    * 灰色：本对象已访问过，但是本对象 引用到 的其他对象 尚未全部访问完。全部访问后，会转换为黑色。  
    * 白色：尚未访问过。  
2. 三色标记流程： 1).根对象黑色... **<font color = "clime">如果标记结束后对象仍为白色，意味着已经“找不到”该对象在哪了，不可能会再被重新引用。</font>**  
3. **<font color = "clime">`多标/错标`，本应该回收 但是 没有回收到的内存，被称之为“浮动垃圾”</font>** ，并不会影响垃圾回收的正确性，只是需要等到下一轮垃圾回收才被清除。  
4. **<font color = "clime">漏标：把本来应该存活的垃圾，标记为了死亡。这就会导致非常严重的错误。</font>**   
	1. 两个必要条件：1). 灰色指向白色的引用消失。2). 黑色重新指向白色；  
  &emsp; 新增对象不算漏标。  
	2. CMS采用增量更新（针对新增的引用，将其记录下来等待遍历）， **<font color = "clime">关注引用的增加（黑色重新指向白色），把黑色重写标记为灰色，下次重新扫描属性。</font>** 破坏了条件“黑指向白”。
	3. G1采用开始时快照技术SATB， **<font color = "clime">关注引用的删除（灰色指向白色的引用消失），当B->D消失时，要把这个引用推到GC的堆栈，保证D还能被GC扫描到。破坏了条件“灰指向白的引用消失”。</font>** 保存在GC堆栈中的删除引用，会在最终标记remark阶段处理。    
	4. 使用SATB会大大减少扫描对象。  


### 1.3.7. JVM调优
#### 1.3.7.1. JVM调优-基础
1. JVM命令行调优工具：  
    * Jps：虚拟机进程状况工具。  
    * Jstack：java线程堆栈跟踪工具。  
    &emsp; **<font color = "clime">生成线程快照的主要目的是定位线程出现长时间停顿的原因，如线程间死锁、死循环、请求外部资源导致的长时间等待等都是导致线程长时间停顿的常见原因。</font>**  
    &emsp; **`线程出现停顿的时候，通过jstack来查看各个线程的调用堆栈，就可以知道没有响应的线程到底在后台做什么事情，或者等待什么。`**  
    * Jmap：java内存映像工具。  
    &emsp; <font color = "red">jmap(JVM Memory Map)命令用于生成heap dump文件，如果不使用这个命令，</font> **<font color = "red">还可以使用-XX:+HeapDumpOnOutOfMemoryError参数来让虚拟机出现OOM的时候自动生成dump文件。</font>**   
    &emsp; jmap -dump:live,format=b,file=path pid。 **<font color = "blue">参数lime表示需要抓取目前在生命周期内的内存对象。</font>**   
    * Jhat：虚拟机堆转储快照分析工具。  
    * Jstat：虚拟机统计信息监视工具。  
    * Jinfo：java配置信息工具。  


#### 1.3.7.2. JVM调优
1. 内存设置  
&emsp; 如何将各分区调整到合适的大小，分析活跃数据的大小是很好的切入点。  
&emsp; **活跃数据的大小是指，应用程序稳定运行时长期存活对象在堆中占用的空间大小，也就是Full GC后堆中老年代占用空间的大小。** 
2. GC调优
    1. <font color = "clime">GC的优化主要有2个维度，一是频率，二是时长。</font>  
    2. **<font color = "clime">如果满足下面的指标，则一般不需要进行GC调优：</font>**    
        * Minor GC执行时间不到50ms;
        * Minor GC执行不频繁，约10秒一次；
        * Full GC执行时间不到1s;
        * Full GC执行频率不算频繁，不低于10分钟1次。
    3. Young GC、Full GC优化策略 参考 1.2.3节。


#### 1.3.7.3. JVM问题排查
1. 快速恢复业务：隔离故障服务器。  
2. FGC过高  
&emsp; **<font color = "clime">FGC过高可能是内存参数设置不合理，也有可能是代码中某个位置读取数据量较大导致系统内存耗尽。FGC过高可能导致CPU飚高。</font>**  
&emsp; **<font color = "clime">解决思路（`FGC过高参考CPU飚高`）：FGC过高一般会导致CPU过高，打印线程堆栈信息。查看线程堆栈是用户线程，还是GC线程。如果是GC线程，打印内存快照进行分析（`查看内存溢出`）。</font>**  
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
	1. 解决方案：
		1. 修改JVM启动参数，直接增加内存。  
		2. 检查错误日志，查看“OutOfMemory”错误前是否有其它异常或错误。  
		3. 对代码进行走查和分析，找出可能发生内存溢出的位置。 
		4. 使用内存查看工具动态查看内存快照。 
	2. 使用内存查看工具分析堆dump文件
    3. jvm内存快照dump文件太大： 
	    * **<font color = "clime">live参数表示需要抓取目前在生命周期内的内存对象，也就是说GC收不走的对象，然后绝大部分情况下，需要的看的就是这些内存。</font>**   
		* 如果Dump文件太大，可能需要加上-J-Xmx512m这种参数指定最大堆内存，即jhat -J-Xmx512m -port 9998 /tmp/dump.dat。
		* 如果dump文件太大，使用linux下的mat，既Memory Analyzer Tools。   


#### 1.3.7.4. Arthas工具


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

### 1.5.5. 索引事物锁
#### 1.5.5.1. 索引底层原理 
1. **<font color = "clime">评价一个数据结构作为索引的优劣最重要的指标就是在查找过程中`磁盘I/O`操作次数的渐进复杂度。</font>**  
2. InnoDB使用的数据结构：  
    * B树：
        1. B树中每个节点中不仅包含数据的key值，还有data值。 **<font color = "red">而每一个页的存储空间是有限的，如果data数据较大时将会导致每个节点（即一个页）能存储的key的数量很小。当存储的数据量很大时同样会导致B树的深度较大，</font>** 增大查询时的磁盘I/O次数进而影响查询效率。  
        2. `范围查询，磁盘I/O高。`
    * B+树  
        1. B+Tree中间节点不存储数据，因此B+Tree能够在同样大小的节点中，存储更多的key。
        2. `叶子节点之间会有个指针指向，这个也是B+树的核心点，可以大大提升范围查询效率，也方便遍历整个树。`  
        3. `B+tree的查询效率更加稳定。`  
3. **<font color = "red">联合索引底层还是使用B+树索引，并且还是只有一棵树，只是此时的排序：首先按照第一个索引排序，在第一个索引相同的情况下，再按第二个索引排序，依此类推。</font>**  
4. 无索引时的数据查询：查询数据时从磁盘中依次加载数据页到InnoDB的缓冲池中，然后对缓冲池中缓存页的每行数据，通过数据页的单向链表一个一个去遍历查找，如果没有找到，那么就会顺着数据页的双向链表数据结构，依次遍历加载磁盘中的其他数据页到缓冲池中遍历查询。 

#### 1.5.5.2. 各种索引
&emsp; <font color = "red">InnoDB索引类型可以分为主键索引和辅助索引（非主键索引）。</font>  

#### 1.5.5.3. MySql事务  
1. 事务的四大特性（ACID）：原子性（Atomicity）、一致性（Consistency）、隔离性（Isolation）、持久性（Durability）。  
2. 并发事务处理带来的问题：脏读、丢失修改、不可重复读、幻读。  
&emsp; SQL标准定义了四个隔离级别：读取未提交、读取已提交、可重复读（可以阻止脏读和不可重复读，幻读仍有可能发生，但MySql的可重复读解决了幻读）、可串行化。  
3. 在MySQL中，默认的隔离级别是REPEATABLE-READ（可重复读），阻止脏读和不可重复读，并且解决了幻读问题。  
&emsp; 隔离性(事务的隔离级别)的实现，利用的是锁和MVCC机制。 
    * **<font color = "blue">快照读：生成一个事务快照（ReadView），之后都从这个快照获取数据。</font>** 普通select语句就是快照读。  
    &emsp; <font color = "blue">对于快照读，MVCC因为从ReadView读取，所以必然不会看到新插入的行，所以天然就解决了幻读的问题。</font>  
    * **<font color = "clime">当前读：读取数据的最新版本。</font>** 常见的update/insert/delete、还有 select ... for update、select ... lock in share mode都是当前读。  
    &emsp; 对于当前读的幻读，MVCC是无法解决的。需要使用Gap Lock或Next-Key Lock（Gap Lock + Record Lock）来解决。  


#### 1.5.5.4. MySql锁
1. InnoDB共有七种类型的锁：共享/排它锁、意向锁、记录锁（Record lock）、间隙锁（Gap lock）、临键锁（Next-key lock）、插入意向锁、自增锁。  
2. **<font color = "red">InnoDB存储引擎的锁的算法有三种：</font>**  
    1. Record lock：单个行记录上的锁。  
    2. Gap lock：间隙锁，锁定一个范围，不包括记录本身。  
    &emsp; **<font color = "red">当使用范围条件（> 、< 、between...）检索数据，InnoDB会给符合条件的已有数据记录的索引项加锁。对于键值在条件范围内但并不存在的记录，叫做“间隙（GAP）”，InnoDB也会对这个“间隙”加锁，这就是间隙锁。</font>**  
    &emsp; **<font color = "red">InnoDB除了通过范围条件加锁时使用间隙锁外，如果使用相等条件请求给一个不存在的记录加锁，InnoDB 也会使用间隙锁。</font>**  
    3. Next-key lock：record+gap锁定一个范围，包含记录本身。  
    &emsp; 临键锁，是记录锁与间隙锁的组合，它的封锁范围，既包含索引记录，又包含索引区间。  
    &emsp; <font color = "red">默认情况下，innodb使用next-key locks来锁定记录。</font><font color = "clime">但当查询的索引含有唯一属性的时候，Next-Key Lock会进行优化，将其降级为Record Lock，即仅锁住索引本身，不是范围。</font>  

#### 1.5.5.5. MySql死锁和锁表
&emsp; ~~胡扯，死锁，mysql检测后，回滚一条事务，抛出异常。~~  
1. 服务器报错：`Deadlock found when trying to get to lock; try restarting transaction`。  
2. **<font color = "clime"> 死锁发生了如何解决，MySQL有没有提供什么机制去解决死锁？</font>**  
    1. 发起死锁检测，主动回滚其中一条事务，让其他事务继续执行。  
    2. 设置超时时间，超时后自动释放。  
    &emsp; `在涉及外部锁，或涉及表锁的情况下，InnoDB并不能完全自动检测到死锁，`这需要通过设置锁等待超时参数 innodb_lock_wait_timeout来解决。</font>   
3. **<font color = "clime">如果出现死锁</font>** ，<font color = "clime">可以用`show engine innodb status;`命令来确定最后一个死锁产生的原因。</font>  


### 1.5.6. MySql架构原理
#### 1.5.6.1. MySql架构
1. MySQL整个查询执行过程，总的来说分为5个步骤：`1). 客户端请求 ---> 连接器（验证用户身份，给予权限）  ---> 2). 查询缓存（存在缓存则直接返回，不存在则执行后续操作） ---> 3). 分析器（对SQL进行词法分析和语法分析操作）  ---> 优化器（主要对执行的sql优化选择最优的执行方案方法）  ---> 4). 执行器（执行时会先看用户是否有执行权限，有才去使用这个引擎提供的接口） ---> 5). 去引擎层获取数据返回（如果开启查询缓存则会缓存查询结果）。`  
2. **<font color = "clime">MySQL服务器主要分为Server层和存储引擎层。</font>**  
	1. <font color = "red">Server层包括连接器、查询缓存、分析器、优化器、执行器等。</font>涵盖MySQL的大多数核心服务功能，以及所有的内置函数(如日期、时间、数学和加密函数等)，所有跨存储引擎的功能都在这一层实现，比如存储过程、触发器、视图等，还有 **<font color = "clime">一个通用的日志模块binglog日志模块。</font>**     
	2. 存储引擎：主要负责数据的存储和读取，采用可以替换的插件式架构，支持 InnoDB、MyISAM、Memory等多个存储引擎，其中InnoDB引擎有自有的日志模块redolog模块。  

#### 1.5.6.2. binLog日志  
1. **<font color = "clime">binlog是mysql的逻辑日志，并且由Server层进行记录，使用任何存储引擎的mysql数据库都会记录binlog日志。</font>**  
2. 在实际应用中，主要用在两个场景：主从复制和数据恢复。  
3. 写入流程：SQL修改语句先写Binlog Buffer，事务提交时，按照一定的格式刷到磁盘中。binlog刷盘时机：对于InnoDB存储引擎而言，mysql通过sync_binlog参数控制binlog的刷盘时机。  

#### 1.5.6.3. MySql存储引擎
1. **<font color = "red">InnoDB的特性：</font>**    
    * [支持事务](/docs/SQL/transaction.md)  
    * [支持行锁](/docs/SQL/lock.md)，采用[MVCC](/docs/SQL/MVCC.md)来支持高并发  
    * 支持外键  
    * 支持崩溃后的安全恢复  
    * 不支持全文索引  
    * InnoDB 不保存表的具体行数，执行`select count(*) from table`时需要全表扫描。  

#### 1.5.6.4. InnoDB体系结构
&emsp; Innodb体系结构包含后台线程、内存池和磁盘上的结构。
1. `如果从内存上来看，Change Buffer和Adaptive Hash Index占用的内存都属于Buffer Pool；Log Buffer占用的内存与Buffer Pool独立。即InnoDB内存主要有两大部分：缓冲池、重做日志缓冲。`  
2. `Buffer Pool有Changer Buffer；Redo Log有Double Write。`  


##### 1.5.6.4.1. InnoDB内存结构-性能
&emsp; 内存中的结构主要包括Buffer Pool，Change Buffer、Adaptive Hash Index以及Log Buffer四部分。 **<font color = "blue">如果从内存上来看，[Change Buffer](/docs/SQL/ChangeBuffer.md)和[Adaptive Hash Index](/docs/SQL/AdaptiveHashIndex.md)占用的内存都属于Buffer Pool，Log Buffer占用的内存与 [Buffer Pool](/docs/SQL/bufferPoolNew.md)独立。</font>** `即InnoDB内存主要有两大部分：缓冲池、重做日志缓冲。`  

&emsp; 内存数据落盘整体思路分析：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-173.png)  
&emsp; `InnoDB内存缓冲池中的数据page要完成持久化的话，是通过两个流程来完成的，一个是脏页落盘；一个是预写redo log日志。`  

###### 1.5.6.4.1.1. BufferPool
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


###### 1.5.6.4.1.2. ChangeBuffer
1. 在「非唯一」「普通」索引页（即非聚集索引）不在缓冲池中，对页进行了写操作， 1). 并不会立刻将磁盘页加载到缓冲池，而仅仅记录缓冲变更， 2).等未来数据被读取时，再将数据合并(merge)恢复到缓冲池中的技术。  
2. **~~<font color = "red">如果辅助索引页已经在缓冲区了，则直接修改即可；如果不在，则先将修改保存到 Change Buffer。</font><font color = "blue">Change Buffer的数据在对应辅助索引页读取到缓冲区时合并到真正的辅助索引页中。Change Buffer 内部实现也是使用的 B+ 树。</font>~~**  

###### 1.5.6.4.1.3. AdaptiveHashIndex
&emsp;对于InnoDB的哈希索引，确切的应该这么说：  
&emsp;(1)InnoDB用户无法手动创建哈希索引，这一层上说，InnoDB确实不支持哈希索引；  
&emsp;(2)InnoDB会自调优(self-tuning)，如果判定建立自适应哈希索引(Adaptive Hash Index, AHI)，能够提升查询效率，InnoDB自己会建立相关哈希索引，这一层上说，InnoDB又是支持哈希索引的；  


##### 1.5.6.4.2. InnoDB磁盘结构-可靠性
###### 1.5.6.4.2.1. BufferPool落盘表空间
1. 从InnoDb存储引擎的逻辑存储结构看，所有数据都被逻辑地存放在一个空间中，称之为表空间tablespace。表空间又由段segment，区extent，页page组成。  
2. **<font color = "clime">相比较之下，使用独占表空间的效率以及性能会更高一点。</font>**  
3. **<font color = "clime">在InnoDB存储引擎中，默认每个页的大小为16KB（在操作系统中默认页大小是4KB）。</font>**  

###### 1.5.6.4.2.2. undoLog
1. **<font color = "clime">Undo log，回滚日志，是逻辑日记。undo log解决了事务原子性。</font>**    
2. undo log主要记录了数据的逻辑变化，比如一条INSERT语句，对应一条DELETE的undo log，对于每个UPDATE语句，对应一条相反的UPDATE的undo log，这样在发生错误时，就能回滚到事务之前的数据状态。
3. 事务开始之前，将当前的版本生成undo log。

###### 1.5.6.4.2.3. redoLog
1. redo log，物理格式的日志，记录的是物理数据页面的修改的信息。 **<font color = "red">`redo log实际上记录数据页的变更，而这种变更记录是没必要全部保存，`因此redo log实现上采用了大小固定，循环写入的方式，当写到结尾时，会回到开头循环写日志。</font>**    
2. 解决事务的一致性，持久化数据。  
3. `写入流程(Write-Ahead Logging，‘日志’先行)：当有一条记录需要更新的时候，InnoDB引擎就会先把记录写到redo log(redolog buffer)里面，并更新内存(buffer pool)，这个时候更新就算完成了。`同时，InnoDB引擎会在适当的时候，将这个操作记录更新到磁盘里面(刷脏页)。
4. 刷盘时机：重做日志的写盘，并不一定是随着事务的提交才写入重做日志文件的，而是随着事务的开始，逐步开始的。先写入redo log buffer。  

###### 1.5.6.4.2.4. DoubleWrite
&emsp; doublewrite：<font color = "blue">如果说写缓冲change buffer带给InnoDB存储引擎的是性能，那么两次写Double Write带给InnoDB存储引擎的是数据的可靠性。</font>  
1. MySQL将buffer中一页数据刷入磁盘，要写4个文件系统里的页。  
2. 在应用(apply)重做日志(redo log)前，需要一个页的副本，当写入失效发生时，先通过页的副本来还原该页，再进行重做，这就是doublewrite。即doublewrite是页的副本。  
    1. 在异常崩溃时，如果不出现“页数据损坏”，能够通过redo恢复数据；
    2. 在出现“页数据损坏”时，能够通过double write buffer恢复页数据； 
3. doublewrite分为内存和磁盘的两层架构。当有页数据要刷盘时：  
    1. 第一步：页数据先memcopy到doublewrite buffer的内存里；
    2. 第二步：doublewrite buffe的内存里，会先刷到doublewrite buffe的磁盘上；
    3. 第三步：doublewrite buffe的内存里，再刷到数据磁盘存储上； 

##### 1.5.6.4.3. 两阶段提交和崩溃恢复
1. 两阶段提交
    1. **<font color = "clime">redo log和binlog都可以用于表示事务的提交状态，而两阶段提交就是让这两个状态保持逻辑上的一致。两阶段提交保证解决binlog和redo log的数据一致性。</font>**    
    2. `两阶段提交是很典型的分布式事务场景，因为redolog和binlog两者本身就是两个独立的个体，`要想保持一致，就必须使用分布式事务的解决方案来处理。 **<font color = "blue">而将redolog分成了两步，其实就是使用了两阶段提交协议(Two-phaseCommit，2PC)。</font>**  
    &emsp; 事务的提交过程有两个阶段，就是将redolog的写入拆成了两个步骤：prepare和commit，中间再穿插写入binlog。  
        1. 记录redolog，InnoDB事务进入prepare状态；
        2. 写入binlog；
        3. 将redolog这个事务相关的记录状态设置为commit状态。
2. 崩溃恢复： **<font color = "red">当重启数据库实例的时候，数据库做2个阶段性操作：redo log处理，undo log及binlog 处理。在崩溃恢复中还需要回滚没有提交的事务，提交没有提交成功的事务。由于回滚操作需要undo日志的支持，undo日志的完整性和可靠性需要redo日志来保证，所以崩溃恢复先做redo前滚，然后做undo回滚。</font>**    


## 1.6. 项目构建
### 1.6.1. 接口幂等
&emsp; 接口幂等xxx常用解决方案：分布式锁、DB锁<font color = "red">（ 1).select+insert，insert前先select，该方案可能不适用于并发场景，在并发场景中，要配合其他方案一起使用，否则同样会产生重复数据</font>、<font color = "clime"> 2). 状态机</font>、<font color = "red">3). 乐观锁（新增version字段））</font>....  

&emsp; **<font color = "blue">小结：</font>**  
&emsp; 一般场景直接使用redis分布式锁解决。可是redis分布式锁可能因编码、部署等，出现一些问题。    
&emsp; 对数据要求高的场景，使用分布式锁 + db锁，db锁一般采用状态机幂等。  
&emsp; 对于将商品数量放在redis中，扣减库存采用lua脚本，支付时反查订单系统，防止超卖问题。    


### 1.6.2. 接口响应时间
1. 链路追踪，查询耗时情况。  
2. 接口的响应时间过长，你会怎么办？（此处只针对最简单的场景，抛开STW那些复杂的问题。）以下是我目前想到的：  
    1. 异步化（Runnable、Future）  
    2. 缓存  
    3. 并行（ForkJoinPool、CyclicBarrier）  
    4. 干掉锁（空间换时间）  

### 1.6.3. 接口预警
1. logback添加error预警
2. Filebeat+Logstash发送Email告警日志
3. 钉钉应用开放平台

## 1.7. 架构设计
### 1.7.1. 架构质量属性


### 1.7.2. 系统瓶颈


## 1.8. Spring
### 1.8.1. Spring基础


### 1.8.2. Spring IOC


### 1.8.3. Spring DI

#### 1.8.3.1. 循环依赖

### 1.8.4. Bean的生命周期

### 1.8.5. 容器相关特性
#### 1.8.5.1. FactoryBean

#### 1.8.5.2. 可二次开发常用接口


##### 1.8.5.2.1. 事件


##### 1.8.5.2.2. Aware接口


##### 1.8.5.2.3. 后置处理器

##### 1.8.5.2.4. InitializingBean


### 1.8.6. AOP基本概念


### 1.8.7. SpringAOP解析

### 1.8.8. Spring事务


### 1.8.9. SpringMVC解析


### 1.8.10. 过滤器、拦截器、监听器


## 1.9. MyBatis
### 1.9.1. MyBatis架构

### 1.9.2. MyBatis SQL执行解析


### 1.9.3. MyBatis缓存

### 1.9.4. MyBatis插件解析


