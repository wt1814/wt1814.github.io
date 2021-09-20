
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
            - [1.4.2.1. 并发编程原理](#1421-并发编程原理)
                - [1.4.2.1.1. CPU缓存及JMM](#14211-cpu缓存及jmm)
                - [1.4.2.1.2. 并发安全问题产生原因](#14212-并发安全问题产生原因)
                - [1.4.2.1.3. 并发安全解决底层](#14213-并发安全解决底层)
                - [1.4.2.1.4. 伪共享问题](#14214-伪共享问题)
            - [1.4.2.2. 线程安全解决](#1422-线程安全解决)
                - [1.4.2.2.1. 线程安全解决方案](#14221-线程安全解决方案)
                - [1.4.2.2.2. Synchronized](#14222-synchronized)
                    - [1.4.2.2.2.1. Synchronized介绍](#142221-synchronized介绍)
                    - [1.4.2.2.2.2. Synchronized使用](#142222-synchronized使用)
                - [1.4.2.2.3. Synchronized使用是否安全](#14223-synchronized使用是否安全)
                    - [1.4.2.2.3.1. Synchronized底层原理](#142231-synchronized底层原理)
                    - [1.4.2.2.3.2. Synchronized优化](#142232-synchronized优化)
                - [1.4.2.2.4. Volatile](#14224-volatile)
                - [1.4.2.2.5. ThreadLocal原理](#14225-threadlocal原理)
                    - [1.4.2.2.5.1. ThreadLocal原理](#142251-threadlocal原理)
                    - [1.4.2.2.5.2. ThreadLocal应用](#142252-threadlocal应用)
            - [1.4.2.3. 线程通信(生产者消费者问题)](#1423-线程通信生产者消费者问题)
            - [1.4.2.4. 线程活跃性](#1424-线程活跃性)
        - [1.4.3. 线程池](#143-线程池)
            - [1.4.3.1. 线程池框架](#1431-线程池框架)
            - [1.4.3.2. ThreadPoolExecutor详解](#1432-threadpoolexecutor详解)
            - [1.4.3.3. 线程池的正确使用](#1433-线程池的正确使用)
            - [1.4.3.4. ForkJoinPool详解](#1434-forkjoinpool详解)
            - [1.4.3.5. Future相关](#1435-future相关)
            - [1.4.3.6. ~~CompletionService~~](#1436-completionservice)
            - [1.4.3.7. ~~CompletableFuture~~](#1437-completablefuture)
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
        - [1.8.6. SpringAOP教程](#186-springaop教程)
        - [1.8.7. SpringAOP解析](#187-springaop解析)
        - [1.8.8. Spring事务](#188-spring事务)
        - [1.8.9. SpringMVC解析](#189-springmvc解析)
        - [1.8.10. 过滤器、拦截器、监听器](#1810-过滤器拦截器监听器)
    - [1.9. MyBatis](#19-mybatis)
        - [1.9.1. MyBatis大数据量查询](#191-mybatis大数据量查询)
        - [1.9.2. MyBatis架构](#192-mybatis架构)
        - [1.9.3. MyBatis SQL执行解析](#193-mybatis-sql执行解析)
        - [1.9.4. MyBatis缓存](#194-mybatis缓存)
        - [1.9.5. MyBatis插件解析](#195-mybatis插件解析)
    - [1.10. SpringBoot](#110-springboot)
        - [1.10.1. SpringBoot基础知识](#1101-springboot基础知识)
        - [1.10.2. SpringBoot启动过程](#1102-springboot启动过程)
            - [1.10.2.1. SpringApplication初始化](#11021-springapplication初始化)
            - [1.10.2.2. run()方法运行过程](#11022-run方法运行过程)
            - [1.10.2.3. SpringBoot事件监听机制](#11023-springboot事件监听机制)
                - [1.10.2.3.1. 事件监听步骤](#110231-事件监听步骤)
                - [1.10.2.3.2. 内置生命周期事件](#110232-内置生命周期事件)
            - [1.10.2.4. SpringBoot事件回调](#11024-springboot事件回调)
        - [1.10.3. SpringBoot自动配置](#1103-springboot自动配置)
            - [1.10.3.1. 注解@SpringBootApplication](#11031-注解springbootapplication)
            - [1.10.3.2. 加载自动配置流程](#11032-加载自动配置流程)
            - [1.10.3.3. 内置Tomcat](#11033-内置tomcat)
        - [1.10.4. 自定义strater](#1104-自定义strater)
    - [1.11. SpringCloud](#111-springcloud)
        - [1.11.1. Eureka](#1111-eureka)
        - [1.11.2. Ribbon](#1112-ribbon)
        - [1.11.3. Hytrix](#1113-hytrix)
        - [1.11.4. Feign](#1114-feign)
        - [1.11.5. Zuul](#1115-zuul)
        - [1.11.6. Sleuth](#1116-sleuth)
        - [1.11.7. Admin](#1117-admin)
    - [1.12. Dubbo](#112-dubbo)
        - [1.12.1. Dubbo和Spring Cloud](#1121-dubbo和spring-cloud)
        - [1.12.2. RPC介绍](#1122-rpc介绍)
        - [1.12.3. Dubbo介绍](#1123-dubbo介绍)
        - [1.12.4. Dubbo框架设计](#1124-dubbo框架设计)
        - [1.12.5. 暴露和引用服务](#1125-暴露和引用服务)
        - [1.12.6. 服务调用](#1126-服务调用)
            - [1.12.6.1. 服务调用介绍](#11261-服务调用介绍)
            - [1.12.6.2. Dubbo序列化和协议](#11262-dubbo序列化和协议)
                - [1.12.6.2.1. Dubbo协议长连接](#112621-dubbo协议长连接)
        - [1.12.7. Dubbo集群容错](#1127-dubbo集群容错)
        - [1.12.8. 扩展点加载(SPI)](#1128-扩展点加载spi)
    - [1.13. Zookeeper](#113-zookeeper)

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
3. **<font color = "clime">`多标/错标`，本应该回收 但是 没有回收掉的内存，被称之为“浮动垃圾”</font>** ，并不会影响垃圾回收的正确性，只是需要等到下一轮垃圾回收才被清除。  
4. **<font color = "clime">漏标：把本来应该存活的垃圾，标记为了死亡。这就会导致非常严重的错误。</font>**   
	1. 两个必要条件：1). 灰色指向白色的引用消失。2). 黑色重新指向白色；  
  &emsp; 新增对象不算漏标。  
	2. CMS采用增量更新（针对新增的引用，将其记录下来等待遍历）， **<font color = "clime">关注引用的增加（黑色重新指向白色），把黑色重写标记为灰色，下次重新扫描属性。</font>** 破坏了条件“黑指向白”。
	3. G1采用开始时快照技术SATB， **<font color = "clime">关注引用的删除（灰色指向白色的引用消失），当B->D消失时，要把这个引用推到GC的堆栈，保证D还能被GC扫描到。破坏了条件“灰指向白的引用消失”。</font>** 保存在GC堆栈中的删除引用，会在最终标记remark阶段处理。    
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
1. 线程状态：新建、就绪、阻塞（等待阻塞(o.wait)、同步阻塞(lock)、其他阻塞(sleep/join)）、等待、计时等待、终止。  
&emsp; 线程状态切换图示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/thread-1.png)  
2. yield()，线程让步。 yield会使当前线程让出CPU执行时间片，与其他线程一起重新竞争CPU时间片。  
3. thread.join()把指定的线程加入到当前线程，可以将两个交替执行的线程合并为顺序执行的线程。比如在线程B中调用了线程A的Join()方法，直到线程A执行完毕后，才会继续执行线程B。  
4. 中断  
    &emsp; **<font color = "red">线程在不同状态下对于中断所产生的反应：</font>**    
    * NEW和TERMINATED对于中断操作几乎是屏蔽的；  
    * RUNNABLE和BLOCKED类似， **<font color = "cclime">对于中断操作只是设置中断标志位并没有强制终止线程，对于线程的终止权利依然在程序手中；</font>**  
    * WAITING/TIMED_WAITING状态下的线程对于中断操作是敏感的，它们会抛出异常并清空中断标志位。  

### 1.4.2. 并发编程
#### 1.4.2.1. 并发编程原理
##### 1.4.2.1.1. CPU缓存及JMM
1. JMM
    1. JMM内存划分：线程对变量的所有操作都必须在工作内存进行，而不能直接读写主内存中的变量。    
    2. 单个线程操作时，8种内存间交换操作指令。  
    3. 线程之间的通信和同步。线程之间的通信过程：线程对变量的操作（读取赋值等）必须在工作内存中进行，首先要将变量从主内存拷贝到自己的工作内存空间，然后对变量进行操作，操作完成后再将变量写回主内存，不能直接操作主内存中的变量，</font>各个线程中的工作内存中存储着主内存中的变量副本拷贝，<font color = "red">因此不同的线程间无法访问对方的工作内存，线程间的通信（传值）必须通过主内存来完成。</font>    


##### 1.4.2.1.2. 并发安全问题产生原因
1. **并发安全的3个问题：**  

    * 原子性：线程切换带来的原子性问题；（[Volatile](/docs/java/concurrent/Volatile.md)不保证原子性）
    * 可见性：缓存不能及时刷新导致的可见性问题；
    * 有序性：编译优化带来的有序性问题  

    &emsp; **<font color = "clime">【编译器优化】和“缓存不能及时刷新”(【内存系统重排序】)都是重排序的一种。</font>**   

2. **重排序：**  
    * **<font color = "blue">重排序分类：1). 编译器优化；2). 指令重排序(CPU优化行为)；3). 内存系统重排序：内存系统没有重排序，但是由于有缓存的存在，使得程序整体上会表现出乱序的行为。</font>**     
        * 对于编译器，JMM的编译器重排序规则会禁止特定类型的编译器重排序（不是所有的编译器重排序都要禁止）。  
        * 对于处理器重排序，JMM的处理器重排序规则会要求Java编译器在生成指令序列时，插入特定类型的内存屏障指令， **<font color = "clime">通过内存屏障指令来禁止特定类型的处理器重排序</font>** (不是所有的处理器重排序都要禁止)。 

    * 重排序遵守的规则：重排序遵守数据依赖性、重排序遵守as-if-serial语义。  
    * 重排序对多线程的影响

##### 1.4.2.1.3. 并发安全解决底层
1. 缓存一致性协议  
    1. 怎么解决缓存一致性问题呢？使用总线锁或缓存锁。  
    &emsp; 缓存锁：只要保证多个CPU缓存的同一份数据是一致的就可以了，基于缓存一致性协议来实现。  
    2. MESI缓存一致性协议  
        1. 缓存一致性协议有很多种，MESI(Modified-Exclusive-Shared-Invalid)协议其实是目前使用很广泛的缓存一致性协议，x86处理器所使用的缓存一致性协议就是基于MESI的。  
        2. 其他cpu通过 总线嗅探机制 可以感知到数据的变化从而将自己缓存里的数据失效。总线嗅探，**<font color = "red">每个CPU不断嗅探总线上传播的数据来检查自己缓存值是否过期了，如果处理器发现自己的缓存行对应的内存地址被修改，就会将当前处理器的缓存行设置为无效状态，当处理器对这个数据进行修改操作的时候，会重新从内存中把数据读取到处理器缓存中。</font>**    
        2. 总线嗅探会带来总线风暴。  
2. 内存屏障  
&emsp; Java中如何保证底层操作的有序性和可见性？可以通过内存屏障。  
&emsp; 内存屏障，禁止处理器重排序，保障缓存一致性。
    1. 内存屏障的作用：（~~原子性~~、可见性、有序性）
        1.（`保障可见性`）它会强制将对缓存的修改操作立即写入主存； 如果是写操作，会触发总线嗅探机制(MESI)，会导致其他CPU中对应的缓存行无效，也有 [伪共享问题](/docs/java/concurrent/PseudoSharing.md)。 
        2.（`保障有序性`）阻止屏障两侧的指令重排序。 
3. JMM中的happens-before原则

##### 1.4.2.1.4. 伪共享问题
1. CPU具有多级缓存，越接近CPU的缓存越小也越快；CPU缓存中的数据是以缓存行为单位处理的；CPU缓存行（通常是64字节）能带来免费加载数据的好处，所以处理数组性能非常高。  
2. **CPU缓存行也带来了弊端，多线程处理不相干的变量时会相互影响，也就是伪共享。**  
&emsp; 设想如果有个long类型的变量a，它不是数组的一部分，而是一个单独的变量，并且还有另外一个long类型的变量b紧挨着它，那么当加载a的时候将免费加载b。  
&emsp; 看起来似乎没有什么毛病，但是如果一个CPU核心的线程在对a进行修改，另一个CPU核心的线程却在对b进行读取。  
3. 避免伪共享的主要思路就是让不相干的变量不要出现在同一个缓存行中；一是在两个long类型的变量之间再加7个long类型(字节填充)；二是创建自己的long类型，而不是用原生的；三是使用java8提供的注解。  
&emsp; 高性能原子类[LongAdder](/docs/java/concurrent/LongAdder.md)可以解决类伪共享问题。   

#### 1.4.2.2. 线程安全解决
##### 1.4.2.2.1. 线程安全解决方案
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

##### 1.4.2.2.2. Synchronized
###### 1.4.2.2.2.1. Synchronized介绍




###### 1.4.2.2.2.2. Synchronized使用
1. `类锁：当Synchronized修饰静态方法或Synchronized修饰代码块传入某个class对象（Synchronized (XXXX.class)）时被称为类锁。`
2. `对象锁：当Synchronized修饰非静态方法或Synchronized修饰代码块时传入非class对象（Synchronized this）时被称为对象锁。`


##### 1.4.2.2.3. Synchronized使用是否安全


###### 1.4.2.2.3.1. Synchronized底层原理
1. Synchronized底层实现：  
    * Synchronized方法同步：依靠的是方法修饰符上的ACC_Synchronized实现。  
    * Synchronized代码块同步：使用monitorenter和monitorexit指令实现。   
每一个对象都会和一个监视器monitor关联。监视器被占用时会被锁住，其他线程无法来获取该monitor。   
线程执行monitorenter指令时尝试获取对象的monitor的所有权，当monitor被占用时就会处于锁定状态。  
2. **<font color = "clime">Java对象头的MarkWord中除了存储锁状态标记外，还存有ptr_to_heavyweight_monitor（也称为管程或监视器锁）的起始地址，每个对象都存在着一个monitor与之关联。</font>**  
3. **<font color = "clime">在Java虚拟机（HotSpot）中，Monitor是基于C++实现的，在虚拟机的ObjectMonitor.hpp文件中。</font><font color = "blue">monitor运行的机制过程如下：(_EntryList队列、_Owner区域、_WaitSet队列)</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-55.png)  
    * `想要获取monitor的线程，首先会进入_EntryList队列。`  
    * `当某个线程获取到对象的monitor后，进入Owner区域，设置为当前线程，`同时计数器count加1。  
    * **如果线程调用了wait()方法，则会进入WaitSet队列。** 它会释放monitor锁，即将owner赋值为null，count自减1，进入WaitSet队列阻塞等待。  
    * 如果其他线程调用 notify() / notifyAll()，会唤醒WaitSet中的某个线程，该线程再次尝试获取monitor锁，成功即进入Owner区域。  
    * 同步方法执行完毕了，线程退出临界区，会将monitor的owner设为null，并释放监视锁。  
4. linux互斥锁nutex（内核态）  
&emsp; <font color = "clime">重量级锁是依赖对象内部的monitor锁来实现的，而monitor又依赖操作系统的MutexLock(互斥锁)来实现的，所以重量级锁也称为互斥锁。</font>  
&emsp; **<font color = "clime">为什么说重量级线程开销很大？</font>**  
&emsp; 当系统检查到锁是重量级锁之后，会把等待想要获得锁的线程进行阻塞，被阻塞的线程不会消耗cpu。 **<font color = "clime">`但是阻塞或者唤醒一个线程时，都需要操作系统来帮忙，这就需要从用户态转换到内核态(向内核申请)，而转换状态是需要消耗很多时间的，有可能比用户执行代码的时间还要长。`</font>**  



###### 1.4.2.2.3.2. Synchronized优化
1. **<font color = "clime">锁降级：</font>** <font color = "red">Hotspot在1.8开始有了锁降级。在STW期间JVM进入安全点时，如果发现有闲置的monitor（重量级锁对象），会进行锁降级。</font>   
2. 锁升级
    锁主要存在四种状态，依次是：无锁状态（普通对象）、偏向锁状态、轻量级锁状态、重量级锁状态，它们会随着竞争的激烈而逐渐升级。锁升级流程如下：   
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-79.png)   
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-80.png)   
	1. 偏向锁：  
        1.  **<font color = "bule">偏向锁状态</font>**  
            * **<font color = "clime">匿名偏向(Anonymously biased)</font>** 。在此状态下thread pointer为NULL(0)，意味着还没有线程偏向于这个锁对象。第一个试图获取该锁的线程将会面临这个情况，使用原子CAS指令可将该锁对象绑定于当前线程。这是允许偏向锁的类对象的初始状态。
            * **<font color = "clime">可重偏向(Rebiasable)</font>** 。在此状态下，偏向锁的epoch字段是无效的（与锁对象对应的class的mark_prototype的epoch值不匹配）。下一个试图获取锁对象的线程将会面临这个情况，使用原子CAS指令可将该锁对象绑定于当前线程。**在批量重偏向的操作中，未被持有的锁对象都被置于这个状态，以便允许被快速重偏向。**
            * **<font color = "clime">已偏向(Biased)</font>** 。这种状态下，thread pointer非空，且epoch为有效值——意味着其他线程正在持有这个锁对象。
        2. 偏向锁获取： 
            1. 判断是偏向锁时，检查对象头Mark Word中记录的Thread Id是否是当前线程ID。  
            2. 如果对象头Mark Word中Thread Id不是当前线程ID，则进行CAS操作，企图将当前线程ID替换进Mark Word。如果当前对象锁状态处于匿名偏向锁状态（可偏向未锁定），则会替换成功（ **<font color = "clime">将Mark Word中的Thread id由匿名0改成当前线程ID，</font>** 在当前线程栈中找到内存地址最高的可用Lock Record，将线程ID存入）。  
            3. 如果对象锁已经被其他线程占用，则会替换失败，开始进行偏向锁撤销，这也是偏向锁的特点，一旦出现线程竞争，就会撤销偏向锁； 
        3. 偏向锁撤销： 
            1. 等到安全点，检查持有偏向锁的`线程是否还存活`。如果线程还存活，则检查线程是否在执行同步代码块中的代码，如果是，则升级为轻量级锁，进行CAS竞争锁； 
            2. 如果持有偏向锁的线程未存活，或者持有偏向锁的线程未在执行同步代码块中的代码， **<font color = "red">则进行校验`是否允许重偏向`。</font>**   
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
			1. 获取轻量锁过程当中会在当前线程的虚拟机栈中创建一个Lock Record的内存区域去存储获取锁的记录DisplacedMarkWord，
			2. 然后使用CAS操作将锁对象的Mark Word更新成指向刚刚创建的Lock Record的内存区域DisplacedMarkWord的地址。  
		4. 已经获取轻量级锁的线程的解锁： **<font color = "red">轻量级锁的锁释放逻辑其实就是获得锁的逆向逻辑，通过CAS操作把线程栈帧中的LockRecord替换回到锁对象的MarkWord中。</font>** 
    3. 重量级锁  
    &emsp; **<font color = "clime">为什么有了自旋锁还需要重量级锁？</font>**  
    &emsp; 自旋是消耗CPU资源的，如果锁的时间长，或者自旋线程多，CPU会被大量消耗；重量级锁有等待队列，所有拿不到锁的线程进入等待队列，不需要消耗CPU资源。  
    &emsp; 偏向锁、自旋锁都是用户空间完成。重量级锁是需要向内核申请。  



##### 1.4.2.2.4. Volatile
1. **<font color = "clime">Volatile的特性：</font>**  
    1. 不支持原子性。<font color = "red">它只对Volatile变量的单次读/写具有原子性；</font><font color = "clime">但是对于类似i++这样的复合操作不能保证原子性。</font>    
    2. 实现了可见性。 **Volatile提供happens-before的保证，使变量在多个线程间可见。**  
    3. <font color = "red">实现了有序性，禁止进行指令重排序。</font>  
2. Volatile底层原理：  
    * **<font color = "clime">在Volatile写前插入写-写[屏障](/docs/java/concurrent/ConcurrencySolve.md)（禁止上面的普通写与下面的Volatile写重排序），在Volatile写后插入写-读屏障（禁止上面的Volatile写与下面可能有的Volatile读/写重排序）。</font>**  
    * **<font color = "clime">在Volatile读后插入读-读屏障（禁止下面的普通读操作与上面的Volatile读重排序）、读-写屏障（禁止下面所有的普通写操作和上面Volatile读重排序）。</font>**  
3. Volatile为什么不安全（不保证原子性，线程切换）？  
&emsp; 两个线程执行i++（i++的过程可以分为三步，首先获取i的值，其次对i的值进行加1，最后将得到的新值写回到缓存中），线程1获取i值后被挂起，线程2执行...  
4. DCL详解：  
	1. 为什么两次判断？ 线程1调用第一个if(singleton==null)，可能会被挂起。  
	2. 为什么要加volatile关键字？  
	&emsp; singleton = new Singleton()非原子性操作，包含3个步骤：分配内存 ---> 初始化对象 ---> 将singleton对象指向分配的内存空间(这步一旦执行了，那singleton对象就不等于null了)。  
	&emsp; **<font color = "clime">因为指令重排序，可能编程1->3->2。如果是这种顺序，会导致别的线程拿到半成品的实例。</font>**  


##### 1.4.2.2.5. ThreadLocal原理
###### 1.4.2.2.5.1. ThreadLocal原理
1. ThreadLocal源码/内存模型：  
    1. **<font color = "red">ThreadLocal#set()#getMap()方法：线程调用threadLocal对象的set(Object value)方法时，数据并不是存储在ThreadLocal对象中，</font><font color = "clime">而是将值存储在每个Thread实例的threadLocals属性中。</font>** 即，当前线程调用ThreadLocal类的set或get方法时，实际上调用的是ThreadLocalMap类对应的 get()、set()方法。  
    &emsp; ~~Thread ---> ThreadLocal.ThreadLocalMap~~
    2. **<font color = "clime">ThreadLocal.ThreadLocalMap，</font>Map结构中Entry继承WeakReference，所以Entry对应key的引用(ThreadLocal实例)是一个弱引用，Entry对Value的引用是强引用。<font color = "clime">`Key是一个ThreadLocal实例，Value是设置的值。`Entry的作用即是：为其属主线程建立起一个ThreadLocal实例与一个线程持有对象之间的对应关系。</font>**   
2. **ThreadLocal内存泄露：**  
&emsp; ThreadLocalMap使用ThreadLocal的弱引用作为key，<font color = "red">如果一个ThreadLocal不存在外部强引用时，Key(ThreadLocal实例)会被GC回收，这样就会导致ThreadLocalMap中key为null，而value还存在着强引用，只有thead线程退出以后，value的强引用链条才会断掉。</font>  
&emsp; **<font color = "clime">但如果当前线程迟迟不结束的话，这些key为null的Entry的value就会一直存在一条强引用链：Thread Ref -> Thread -> ThreaLocalMap -> Entry -> value。永远无法回收，造成内存泄漏。</font>**  
&emsp; 解决方案：`调用remove()方法`
3. **ThreadLocalMap的key被回收后，如何获取值？**  
&emsp; ThreadLocal#get() ---> setInitialValue() ---> ThreadLocalMap.set(this, value); 。  
&emsp; 通过nextIndex()不断获取table上的槽位，直到遇到第一个为null的地方，此处也将是存放具体entry的位置，在线性探测法的不断冲突中，如果遇到非空entry中的key为null，可以表明key的弱引用已经被回收，但是由于线程仍未结束生命周期被回收，而导致该entry仍未从table中被回收，那么则会在这里尝试通过replaceStaleEntry()方法，将null key的entry回收掉并set相应的值。  


###### 1.4.2.2.5.2. ThreadLocal应用
1. ThreadLocal使用场景：  
    1. 线程安全问题。
    2. 业务中变量传递。1)ThreadLocal实现同一线程下多个类之间的数据传递；2)ThreadLocal实现线程内的缓存，避免重复调用。
    3. ThreadLocal+MDC实现链路日志增强。
    4. ThreadLocal 实现数据库读写分离下强制读主库。
2. ThreadLocal无法在父子线程之间传递。使用类InheritableThreadLocal可以在子线程中取得父线程继承下来的值。   
3. ThreadLocal和线程池。TransmittableThreadLocal是阿里巴巴开源的专门解决InheritableThreadLocal的局限性，实现线程本地变量在线程池的执行过程中，能正常的访问父线程设置的线程变量。  
4. FastThreadLocal


#### 1.4.2.3. 线程通信(生产者消费者问题)

#### 1.4.2.4. 线程活跃性


### 1.4.3. 线程池
#### 1.4.3.1. 线程池框架
1. **线程池通过线程复用机制，并对线程进行统一管理，** 具有以下优点：  
    * 降低系统资源消耗。通过复用已存在的线程，降低线程创建和销毁造成的消耗；  
    * 提高响应速度。当有任务到达时，无需等待新线程的创建便能立即执行；  
    * 提高线程的可管理性。线程是稀缺资源，如果无限制的创建，不仅会消耗大量系统资源，还会降低系统的稳定性，使用线程池可以进行对线程进行统一的分配、调优和监控。  
2. 线程池框架Executor：  
&emsp; Executor：所有线程池的接口。  
&emsp; ExecutorService：扩展了Executor接口。添加了一些用来管理执行器生命周期和任务生命周期的方法。  
&emsp; ThreadPoolExecutor(创建线程池方式一)：线程池的具体实现类。  
&emsp; Executors(创建线程池方式二)：提供了一系列静态的工厂方法用于创建线程池，返回的线程池都实现了ExecutorService 接口。  
3. 根据返回的对象类型，创建线程池可以分为几类：ThreadPoolExecutor、ScheduleThreadPoolExecutor（任务调度线程池）、ForkJoinPool。  
4. **<font color = "clime">Executors返回线程池对象的弊端如下：</font>**  
	* SingleThreadExecutor（单线程）和FixedThreadPool（定长线程池，可控制线程最大并发数）：允许请求的队列长度为Integer.MAX_VALUE，可能堆积大量的请求，从而导致OOM。
	* CachedThreadPool和ScheduledThreadPool：允许创建的线程数量为Integer.MAX_VALUE，可能会创建大量线程，从而导致OOM。
5. 线程池执行，ExecutorService的API：execute()，提交不需要返回值的任务；`submit()，提交需要返回值的任务，返回值类型是Future`。    

#### 1.4.3.2. ThreadPoolExecutor详解
1. 理解构造函数中参数：核心线程数大小、最大线程数大小、空闲线程（超出corePoolSize的线程）的生存时间、参数keepAliveTime的单位、任务阻塞队列、创建线程的工厂（可以通过这个工厂来创建有业务意义的线程名字）。  
    * [阻塞队列](/docs/java/concurrent/BlockingQueue.md)，线程池所使用的缓冲队列，常用的是：SynchronousQueue、ArrayBlockingQueue、LinkedBlockingQueue。   
    * 拒绝策略，默认AbortPolicy（拒绝任务，抛异常）， **<font color = "clime">可以选用CallerRunsPolicy（任务队列满时，不进入线程池，由主线程执行）。</font>**  
2. 线程运行流程：查看execute方法。  
    &emsp; <font color = "clime">线程池创建时没有设置成预启动加载，首发线程数为0。</font><font color = "red">任务队列是作为参数传进来的。即使队列里面有任务，线程池也不会马上执行它们，而是创建线程。</font>当一个线程完成任务时，它会从队列中取下一个任务来执行。当调用execute()方法添加一个任务时，线程池会做如下判断：  
    1. 如果当前工作线程总数小于corePoolSize，则直接创建核心线程执行任务（任务实例会传入直接用于构造工作线程实例）。  
    2. 如果当前工作线程总数大于等于corePoolSize，判断线程池是否处于运行中状态，同时尝试用非阻塞方法向任务队列放入任务，这里会二次检查线程池运行状态，如果当前工作线程数量为0，则创建一个非核心线程并且传入的任务对象为null。  
    3. 如果向任务队列投放任务失败（任务队列已经满了），则会尝试创建非核心线程传入任务实例执行。  
    4. 如果创建非核心线程失败，此时需要拒绝执行任务，调用拒绝策略处理任务。  
3. 线程复用机制：    
&emsp; **线程池将线程和任务进行解耦，线程是线程，任务是任务，摆脱了之前通过Thread创建线程时的一个线程必须对应一个任务的限制。**  
&emsp; **<font color = "red">在线程池中，同一个线程可以从阻塞队列中不断获取新任务来执行，其核心原理在于线程池对Thread进行了封装（内部类Worker），并不是每次执行任务都会调用Thread.start() 来创建新线程，而是让每个线程去执行一个“循环任务”，在这个“循环任务”中不停的检查是否有任务需要被执行。</font>** 如果有则直接执行，也就是调用任务中的run方法，将run方法当成一个普通的方法执行，通过这种方式将只使用固定的线程就将所有任务的run方法串联起来。  
&emsp; 源码解析：runWorker()方法中，有任务时，while循环获取；没有任务时，清除空闲线程。  
4. 线程池保证核心线程不被销毁？  
&emsp; 获取任务getTask()方法里allowCoreThreadTimeOut值默认为true，线程take()会一直阻塞，等待任务的添加。   


#### 1.4.3.3. 线程池的正确使用
1. **<font color = "clime">线程池设置：</font>**   
    1. `使用自定义的线程池。`共享的问题在于会干扰，如果有一些异步操作的平均耗时是1秒，另外一些是100秒，这些操作放在一起共享一个线程池很可能会出现相互影响甚至饿死的问题。`建议根据异步业务类型，合理设置隔离的线程池。`  
    2. `确定线程池的大小（CPU可同时处理线程数量大部分是CPU核数的两倍）`  
        1. 线程数设置，`建议核心线程数core与最大线程数max一致`
            * 如果是CPU密集型应用（多线程处理复杂算法），则线程池大小设置为N+1。
            * 如果是IO密集型应用（多线程用于数据库数据交互、文件上传下载、网络数据传输等），则线程池大小设置为2N。
            * 如果是混合型，将任务分为CPU密集型和IO密集型，然后分别使用不同的线程池去处理，从而使每个线程池可以根据各自的工作负载来调整。  
        2. 阻塞队列设置  
        &emsp; `线程池的任务队列本来起缓冲作用，`但是如果设置的不合理会导致线程池无法扩容至max，这样无法发挥多线程的能力，导致一些服务响应变慢。队列长度要看具体使用场景，取决服务端处理能力以及客户端能容忍的超时时间等。队列长度要根据使用场景设置一个上限值，如果响应时间要求较高的系统可以设置为0。  
        &emsp; `队列大小200或500-1000。`  
    3. `线程池的优雅关闭：`处于SHUTDOWN的状态下的线程池依旧可以调用shutdownNow。所以可以结合shutdown，shutdownNow，awaitTermination，更加优雅关闭线程池。  
2. **<font color = "clime">线程池使用：</font>**   
    1. **<font color = "clime">线程池异常处理：</font>**  
    &emsp; ThreadPoolExecutor中将异常传递给afterExecute()方法，而afterExecute()没有做任何处理。这种处理方式能够保证提交的任务抛出了异常不会影响其他任务的执行，同时也不会对用来执行该任务的线程产生任何影响。然而afterExecute()没有做任何处理，所以如果任务抛出了异常，也无法立刻感知到。即使感知到了，也无法查看异常信息。  
    &emsp; 解决方案：在提交的任务中将异常捕获并处理，不抛给线程池；异常抛给线程池，但是要及时处理抛出的异常。如果提交任务的时候使用的方法是submit，那么该方法将返回一个Future对象，所有的异常以及处理结果都可以通过future对象获取。    
3. **<font color = "clime">线程池的监控：</font>**  
&emsp; 通过重写线程池的beforeExecute、afterExecute和shutdown等方式就可以实现对线程的监控。  
4. @Async方法没有执行的问题分析：  
&emsp; @Async异步方法默认使用Spring创建ThreadPoolTaskExecutor(参考TaskExecutionAutoConfiguration)，其中默认核心线程数为8，默认最大队列和默认最大线程数都是Integer.MAX_VALUE，队列使用LinkedBlockingQueue，容量是：Integet.MAX_VALUE，空闲线程保留时间：60s，线程池拒绝策略：AbortPolicy。创建新线程的条件是队列填满时，而这样的配置队列永远不会填满，如果有@Async注解标注的方法长期占用线程(比如HTTP长连接等待获取结果)，在核心8个线程数占用满了之后，新的调用就会进入队列，外部表现为没有执行。  


#### 1.4.3.4. ForkJoinPool详解
1. <font color = "clime">ForkJoinPool的两大核心是 分而治之和工作窃取 算法。</font>  
2. 分而治之：<font color = "red">ForkJoinPool的计算方式是大任务拆中任务，中任务拆小任务，最后再汇总。</font>  
3. 工作窃取算法  
&emsp; <font color = "clime">每个工作线程都有自己的工作队列WorkQueue。这是一个双端队列，它是线程私有的。</font>双端队列的操作：push、pop、poll。push/pop只能被队列的所有者线程调用，而poll是由其它线程窃取任务时调用的。  
    1. ForkJoinTask中fork的子任务，将放入运行该任务的工作线程的队头，工作线程将以LIFO的顺序来处理工作队列中的任务；  
    2. **<font color = "clime">为了最大化地利用CPU，空闲的线程将随机从其它线程的队列中“窃取”任务来执行。从工作队列的尾部窃取任务，以减少竞争；</font>**  
    3. **<font color = "clime">当只剩下最后一个任务时，还是会存在竞争，是通过CAS来实现的；</font>**    


#### 1.4.3.5. Future相关
1. **Future是一个接口，它可以对具体的Runnable或者Callable任务进行取消、判断任务是否已取消、查询任务是否完成、获取任务结果。**  
2. JDK1.5为Future接口提供了一个实现类FutureTask，表示一个可以取消的异步运算。它有启动和取消运算、查询运算是否完成和取回运算结果等方法。  


#### 1.4.3.6. ~~CompletionService~~


#### 1.4.3.7. ~~CompletableFuture~~


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
&emsp; ......  

### 1.7.2. 系统瓶颈
&emsp; ......  

## 1.8. Spring
### 1.8.1. Spring基础
1. **@Autowired和@Resource之间的区别：**  
    1. @Autowired默认是按照类型装配注入的，默认情况下它要求依赖对象必须存在(可以设置它的required属性为false)。
    2. @Resource默认是按照名称来装配注入的，只有当找不到与名称匹配的bean才会按照类型来装配注入。  

### 1.8.2. Spring IOC
1. BeanFactory与ApplicationContext
2. BeanDefinition： **<font color = "red">BeanDefinition中保存了Bean信息，比如这个Bean指向的是哪个类、是否是单例的、是否懒加载、这个Bean依赖了哪些Bean等。</font>**  
3. Spring容器刷新：  
    **<font color = "blue">（⚠`利用工厂和反射创建Bean。主要包含3部分：1).容器本身--创建容器、2).容器扩展--后置处理器、3).事件，子容器，实例化Bean。`）</font>**     
    **<font color = "red">Spring bean容器刷新的核心 12个步骤完成IoC容器的创建及初始化工作：</font>**  
    1. 刷新前的准备工作。  
    2. **<font color = "red">创建IoC容器(DefaultListableBeanFactory)，加载和注册BeanDefinition对象。</font>** <font color = "blue">`个人理解：此处仅仅相当于创建Spring Bean的类，实例化是在Spring DI里。`</font>   
        &emsp; **<font color = "clime">DefaultListableBeanFactory中使用一个HashMap的集合对象存放IOC容器中注册解析的BeanDefinition。</font>**  
        ```java
        private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
        ```
    -----------
    3. 对IoC容器进行一些预处理。 为BeanFactory配置容器特性，例如设置BeanFactory的类加载器，配置了BeanPostProcessor，注册了三个默认bean实例，分别是“environment”、“systemProperties”、“systemEnvironment”。  
    4. 允许在上下文子类中对bean工厂进行后处理。 本方法没有具体实现，是一个扩展点，开发人员可以根据自己的情况做具体的实现。  
    5. **<font color = "red">调用BeanFactoryPostProcessor后置处理器对BeanDefinition处理（修改BeanDefinition对象）。</font>**  
    6. **<font color = "red">注册BeanPostProcessor后置处理器。</font>**  
    7. 初始化一些消息源（比如处理国际化的i18n等消息源）。 
    ------------ 
    8. **<font color = "red">初始化应用[事件多播器](/docs/SSM/Spring/feature/EventMulticaster.md)。</font>**     
    9. **<font color = "red">`onRefresh()，典型的模板方法(钩子方法)。不同的Spring容器做不同的事情。`比如web程序的容器ServletWebServerApplicationContext中会调用createWebServer方法去创建内置的Servlet容器。</font>**  
    10. **<font color = "red">注册一些监听器到事件多播器上。</font>**  
    11. **<font color = "red">`实例化剩余的单例bean(非懒加载方式)。`</font><font color = "blue">`注意事项：Bean的IoC、DI和AOP都是发生在此步骤。`</font>**  
    12. **<font color = "red">完成刷新时，发布对应的事件。</font>**  


### 1.8.3. Spring DI
1. 加载时机：SpringBean默认单例，非懒加载，即容器启动时就加载。  
2. 加载流程：  
    1. doCreateBean()创建Bean有三个关键步骤：2.createBeanInstance()实例化、5.populateBean()属性填充、6.initializeBean()初始化。  



#### 1.8.3.1. 循环依赖
1. Spring循环依赖的场景：均采用setter方法（属性注入）注入方式，可被解决；采用构造器和setter方法（属性注入）混合注入方式可能被解决。
2. **<font color = "red">Spring通过3级缓存解决：</font>**  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Spring/spring-20.png)  
    * 三级缓存: Map<String,ObjectFactory<?>> singletonFactories，早期曝光对象工厂，用于保存bean创建工厂，以便于后面扩展有机会创建代理对象。  
    * 二级缓存: Map<String,Object> earlySingletonObjects， **<font color = "blue">早期曝光对象</font>** ，二级缓存，用于存放已经被创建，但是尚未初始化完成的Bean。尚未经历了完整的Spring Bean初始化生命周期。
    * 一级缓存: Map<String,Object> singletonObjects，单例对象池，用于保存实例化、注入、初始化完成的bean实例。经历了完整的Spring Bean初始化生命周期。
3. **<font color = "clime">单例模式下Spring解决循环依赖的流程：</font>**  
    1. Spring创建bean主要分为两个步骤，创建原始bean对象，接着去填充对象属性和初始化。  
    2. 每次创建bean之前，都会从缓存中查下有没有该bean，因为是单例，只能有一个。  
    3. 当创建beanA的原始对象后，并把它放到三级缓存中，接下来就该填充对象属性了，这时候发现依赖了beanB，接着就又去创建 beanB，同样的流程，创建完beanB填充属性时又发现它依赖了beanA，又是同样的流程，不同的是，这时候可以在三级缓存中查到刚放进去的原始对象beanA，所以不需要继续创建，用它注入beanB，完成beanB的创建。此时会将beanA从三级缓存删除，放到二级缓存。   
    4. 既然 beanB 创建好了，所以 beanA 就可以完成填充属性的步骤了，接着执行剩下的逻辑，闭环完成。  
    ---
    &emsp; 当A、B两个类发生循环引用时，在A完成实例化后，就使用实例化后的对象去创建一个对象工厂，并添加到三级缓存中。 **<font color = "blue">`如果A被AOP代理，那么通过这个工厂获取到的就是A代理后的对象，如果A没有被AOP代理，那么这个工厂获取到的就是A实例化的对象。`</font>** 当A进行属性注入时，会去创建B，同时B又依赖了A，所以创建B的同时又会去调用getBean(a)来获取需要的依赖，此时的getBean(a)会从缓存中获取：  

    * 第一步，先获取到三级缓存中的工厂。  
    * 第二步，调用对象工厂的getObject方法来获取到对应的对象，得到这个对象后将其注入到B中。紧接着B会走完它的生命周期流程，包括初始化、后置处理器等。  

    当B创建完后，会将B再注入到A中，此时A再完成它的整个生命周期。  
4. 常见问题
    1. 二级缓存能解决循环依赖嘛？  
    &emsp; 二级缓存可以解决循环依赖。  
    &emsp; 如果创建的Bean有对应的代理，那其他对象注入时，注入的应该是对应的代理对象；但是Spring无法提前知道这个对象是不是有循环依赖的情况，而正常情况下（没有循环依赖情况），Spring都是在创建好完成品Bean之后才创建对应的代理。这时候Spring有两个选择：

        * 方案一：不管有没有循环依赖，都提前创建好代理对象，并将代理对象放入缓存，出现循环依赖时，其他对象直接就可以取到代理对象并注入。
        * 方案二：不提前创建好代理对象，在出现循环依赖被其他对象注入时，才实时生成代理对象。这样在没有循环依赖的情况下，Bean就可以按着Spring设计原则的步骤来创建。  

    &emsp; 如果使用二级缓存解决循环依赖，即采用方案一，意味着所有Bean在实例化后就要完成AOP代理，这样违背了Spring设计的原则，Spring在设计之初就是通过AnnotationAwareAspectJAutoProxyCreator这个后置处理器来在Bean生命周期的最后一步来完成AOP代理，而不是在实例化后就立马进行AOP代理。   
    &emsp; **怎么做到提前曝光对象而又不生成代理呢？**   
    &emsp; Spring就是在对象外面包一层ObjectFactory（三级缓存存放），提前曝光的是ObjectFactory对象，在被注入时才在ObjectFactory.getObject方式内实时生成代理对象，并将生成好的代理对象放入到第二级缓存Map\<String, Object> earlySingletonObjects。  



### 1.8.4. Bean的生命周期
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Spring/spring-10.png)  
&emsp; SpringBean的生命周期主要有4个阶段：  
1. 实例化（Instantiation），可以理解为new一个对象；
2. 属性赋值（Populate），可以理解为调用setter方法完成属性注入；
3. 初始化（Initialization），包含：  
    * 激活Aware方法  
    * 前置处理  
    * 激活自定义的init方法 
    * 后置处理 
4. 销毁（Destruction）---注册Destruction回调函数。  


### 1.8.5. 容器相关特性
#### 1.8.5.1. FactoryBean
&emsp; BeanFactory是个Factory，也就是IOC容器或对象工厂；FactoryBean是个Bean，也由BeanFactory管理。  
&emsp; `一般情况下，Spring通过反射机制利用<bean>的class属性指定实现类实例化Bean。` **<font color = "red">在某些情况下，实例化Bean过程比较复杂，</font>** 如果按照传统的方式，则需要在\<bean>中提供大量的配置信息。配置方式的灵活性是受限的，这时采用编码的方式可能会得到一个简单的方案。 **<font color = "red">Spring为此提供了一个org.springframework.bean.factory.FactoryBean的`工厂类接口`，用户可以通过实现该接口定制实例化Bean的逻辑。</font>**  
&emsp; **<font color = "red">FactoryBean接口的一些实现类，如Spring自身提供的ProxyFactoryBean、JndiObjectFactoryBean，还有Mybatis中的SqlSessionFactoryBean，</font>** 用于生产一些复杂的Bean。  


#### 1.8.5.2. 可二次开发常用接口
&emsp; **<font color = "red">Spring可二次开发常用接口：</font>**  
&emsp; Spring为了用户的开发方便和特性支持，开放了一些特殊接口和类，用户可进行实现或者继承，常见的有：  

&emsp; **Spring IOC阶段：**  
&emsp; [事件多播器](/docs/SSM/Spring/feature/EventMulticaster.md)  
&emsp; [事件](/docs/SSM/Spring/feature/Event.md)  

&emsp; **Spring DI阶段：**  
&emsp; [Aware接口](/docs/SSM/Spring/feature/Aware.md)  
&emsp; [后置处理器](/docs/SSM/Spring/feature/BeanFactoryPostProcessor.md)  
&emsp; [InitializingBean](/docs/SSM/Spring/feature/InitializingBean.md)  

##### 1.8.5.2.1. 事件
&emsp; **<font color = "clime">★★★Spring事件机制的流程：</font>**   
1. **<font color = "clime">事件机制的核心是事件。</font>** Spring中的事件是ApplicationEvent。Spring提供了5个标准事件，此外还可以自定义事件(继承ApplicationEvent)。  
2. **<font color = "clime">确定事件后，要把事件发布出去。</font>** 在事件发布类的业务代码中调用ApplicationEventPublisher#publishEvent方法（或调用ApplicationEventPublisher的子类，例如调用ApplicationContext#publishEvent）。  
3. **<font color = "blue">发布完成之后，启动监听器，自动监听。</font>** 在监听器类中覆盖ApplicationListener#onApplicationEvent方法。  
4. 最后，就是实际场景中触发事件发布，完成一系列任务。  

##### 1.8.5.2.2. Aware接口
&emsp; **<font color = "clime">容器管理的Bean一般不需要了解容器的状态和直接使用容器，但在某些情况下，是需要在Bean中直接对IOC容器进行操作的，这时候，就需要在Bean中设定对容器的感知。Spring IOC容器也提供了该功能，它是通过特定的aware接口来完成的。</font>** <font color = "red">aware接口有以下这些：

* BeanNameAware，可以在Bean中得到它在IOC容器中的Bean实例名称。  
* BeanFactoryAware，可以在Bean中得到Bean所在的IOC容器，从而直接在Bean中使用IOC容器的服务。  
* ApplicationContextAware，可以在Bean中得到Bean所在的应用上下文，从而直接在 Bean中使用应用上下文的服务。  
* MessageSourceAware，在Bean中可以得到消息源。  
* ApplicationEventPublisherAware，在Bean中可以得到应用上下文的事件发布器，从而可以在Bean中发布应用上下文的事件。  
* ResourceLoaderAware，在Bean中可以得到ResourceLoader，从而在Bean中使用ResourceLoader加载外部对应的Resource资源。</font>  

&emsp; 在设置Bean的属性之后，调用初始化回调方法之前，Spring会调用aware接口中的setter方法。  


##### 1.8.5.2.3. 后置处理器
1. <font color = "clime">实现BeanFactoryPostProcessor接口，可以`在spring的bean创建之前，修改bean的定义属性（BeanDefinition）`。</font>  
2. <font color = "red">实现BeanPostProcessor接口，</font><font color = "blue">可以在spring容器实例化bean之后，`在执行bean的初始化方法前后，`添加一些自己的处理逻辑。</font>  


##### 1.8.5.2.4. InitializingBean
&emsp; ......  


### 1.8.6. SpringAOP教程
1. SpringAOP的主要功能是：日志记录，性能统计，安全控制，事务处理，异常处理等。 
    * 慢请求记录  
    * 使用aop + redis + Lua接口限流
2. **SpringAOP失效：**  
&emsp; <font color = "red">同一对象内部方法嵌套调用，慎用this来调用被@Async、@Transactional、@Cacheable等注解标注的方法，this下注解可能不生效。</font>async方法中的this不是动态代理的子类对象，而是原始的对象，故this调用无法通过动态代理来增强。 
3. **<font color = "red">过滤器，拦截器和aop的区别：</font>** 过滤器拦截的是URL；拦截器拦截的是URL；Spring AOP只能拦截Spring管理Bean的访问（业务层Service）。  


### 1.8.7. SpringAOP解析
1. **<font color = "blue">自动代理触发的时机：AspectJAnnotationAutoProxyCreator是一个BeanPostProcessor，</font>** 因此Spring AOP是在这一步，进行代理增强！  
2. **<font color = "clime">代理类的生成流程：1). `获取当前的Spring Bean适配的advisors；`2). `创建代理类`。</font>**   
    1. Spring AOP获取对应Bean适配的Advisors链的核心逻辑：
        1. 获取当前IoC容器中所有的Aspect类。
        2. 给每个Aspect类的advice方法创建一个Spring Advisor，这一步又能细分为： 
            1. 遍历所有 advice 方法。
            2. 解析方法的注解和pointcut。
            3. 实例化 Advisor 对象。
        3. 获取到候选的 Advisors，并且`缓存`起来，方便下一次直接获取。
        4. 从候选的Advisors中筛选出与目标类适配的Advisor。 
            1. 获取到Advisor的切入点pointcut。
            2. 获取到当前target类所有的public方法。
            3. 遍历方法，通过切入点的methodMatcher匹配当前方法，只要有一个匹配成功就相当于当前的Advisor适配。
        5. 对筛选之后的Advisor链进行排序。  
    2. 创建代理类
        1. 创建AopProxy。根据ProxyConfig 获取到了对应的AopProxy的实现类，分别是JdkDynamicAopProxy和ObjenesisCglibAopProxy。 
        2. 获取代理类。


### 1.8.8. Spring事务
1. `@Transactional(rollbackFor = Exception.class) `，Transactional`默认只回滚RuntimeException，`但是可以指定要回滚的异常类型。    
2. **<font color = "red">Spring事务属性通常由事务的传播行为、事务的隔离级别、事务的超时值、事务只读标志组成。</font>**  
    * 事务的传播行为主要分为支持当前事务和不支持当前事务。  
    &emsp; <font color = "red">PROPAGATION_REQUIRED：如果当前存在事务，则加入该事务，合并成一个事务；如果当前没有事务，则创建一个新的事务。这是默认值。</font>  
    * 事务的隔离级别，默认使用底层数据库的默认隔离级别。  
    * 事务只读，相当于将数据库设置成只读数据库，此时若要进行写的操作，会出现错误。  
3. Spring事务失效：  
    * 使用在了非public方法上。
    * 捕获了异常，未再抛出。
    * 同一个类中方法调用。
    * @Transactional的类注入失败。
    * 多数据源（静态配置）
    * 原始SSM项目，重复扫描导致事务失效  


### 1.8.9. SpringMVC解析
1. **SpringMVC的工作流程：**  
    1. 找到处理器：前端控制器DispatcherServlet ---> **<font color = "red">处理器映射器HandlerMapping</font>** ---> 找到处理器Handler；  
    2. 处理器处理：前端控制器DispatcherServlet ---> **<font color = "red">处理器适配器HandlerAdapter</font>** ---> 处理器Handler ---> 执行具体的处理器Controller（也叫后端控制器） ---> Controller执行完成返回ModelAndView；  
    &emsp; 1. 处理器映射器HandlerMapping：根据请求的url查找HandlerHandler即处理器（Controller）。  
    &emsp; 2. **<font color = "blue">处理器适配器HandlAdapter：按照特定规则（HandlerAdapter要求的规则）去执行Handler。通过HandlerAdapter对处理器进行执行，这是适配器模式的应用，通过扩展适配器可以对更多类型的处理器进行执行。</font>**  
    &emsp; 3. 处理器Handler和controller区别：
    3. 返回前端控制器DispatcherServlet ---> 视图解析器ViewReslover。  
2. **SpringMVC解析：**  
    1. 在SpringMVC.xml中定义一个DispatcherServlet和一个监听器ContextLoaderListener。  
    2. 上下文在web容器中的启动：<font color = "red">由ContextLoaderListener启动的上下文为根上下文。在根上下文的基础上，还有一个与Web MVC相关的上下文用来保存控制器（DispatcherServlet）需要的MVC对象，作为根上下文的子上下文，构成一个层次化的上下文体系。</font>  
    3. **<font color = "red">`DispatcherServlet初始化和使用：`</font>**     
        1. 初始化阶段。DispatcherServlet的初始化在HttpServletBean#init()方法中。 **<font color = "red">`完成Spring MVC的组件的初始化。`</font>**    
        2. 调用阶段。这一步是由请求触发的。入口为DispatcherServlet#doService() ---> DispatcherServlet#doDispatch()。 **<font color = "blue">`逻辑即为SpringMVC处理流程。`</font>**   


### 1.8.10. 过滤器、拦截器、监听器
&emsp; ......  

## 1.9. MyBatis

### 1.9.1. MyBatis大数据量查询
1. 流式查询  
&emsp; 流式查询指的是查询成功后不是返回一个集合而是返回一个迭代器，应用每次从迭代器取一条查询结果。流式查询的好处是能够降低内存使用。  
&emsp; **<font color = "clime">如果没有流式查询，想要从数据库取 1000 万条记录而又没有足够的内存时，就不得不分页查询，而分页查询效率取决于表设计，如果设计的不好，就无法执行高效的分页查询。因此流式查询是一个数据库访问框架必须具备的功能。</font>**  
&emsp; 流式查询的过程当中，数据库连接是保持打开状态的，因此要注意的是： **<font color = "clime">执行一个流式查询后，数据库访问框架就不负责关闭数据库连接了，需要应用在取完数据后自己关闭。</font>**  

### 1.9.2. MyBatis架构
&emsp; **<font color = "red">Mybatis的功能架构分为三层：</font>**  

* API接口层：提供给外部使用的接口API，开发人员通过这些本地API来操纵数据库。接口层一接收到调用请求就会调用核心处理层来完成具体的数据处理。  
* 核心处理层：负责具体的SQL查找、SQL解析、SQL执行和执行结果映射处理等。它主要的目的是根据调用的请求完成一次数据库操作。  
* 基础支持层：负责最基础的功能支撑，包括连接管理、事务管理、配置加载和缓存处理，这些都是共用的东西，将它们抽取出来作为最基础的组件。为上层的数据处理层提供最基础的支撑。  


### 1.9.3. MyBatis SQL执行解析
1. sql执行流程：   
    1. 读取核心配置文件并返回InputStream流对象。
    2. 根据InputStream流对象解析出Configuration对象，然后创建SqlSessionFactory工厂对象。
    3. 根据一系列属性从SqlSessionFactory工厂中创建SqlSession。
    4. 从SqlSession中调用Executor执行数据库操作和生成具体SQL指令。
    5. 对执行结果进行二次封装。
    6. 提交与事务。   
2. **<font color = "clime">Mapper接口动态代理类的生成：</font>** 
    * **<font color = "blue">解析配置文件生成sqlSessionFactory时，</font>** 会调用bindMapperForNamespace() ---> addMapper方法， **<font color = "blue">根据mapper文件中的namespace属性值，将接口生成动态代理类的`工厂`，存储在MapperRegistry对象中。</font>** MapperRegistry内部维护一个映射关系，每个接口对应一个`MapperProxyFactory（生成动态代理工厂类）`。      
    * 在调用getMapper，根据type类型，从MapperRegistry对象中的knownMappers获取到当前类型对应的代理工厂类，然后通过代理工厂类使用`jdk自带的动态代理`生成对应Mapper的代理类。  
    ```java
    //这里可以看到每次调用都会创建一个新的代理对象返回
    return mapperProxyFactory.newInstance(sqlSession);
    ```

    ```java
    public T newInstance(SqlSession sqlSession) {
        //首先会调用这个newInstance方法
        //动态代理逻辑在MapperProxy里面
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
        //通过这里调用下面的newInstance方法
        return newInstance(mapperProxy);
    }
    @SuppressWarnings("unchecked")
    protected T newInstance(MapperProxy<T> mapperProxy) {
        //jdk自带的动态代理
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
    }
    ```


### 1.9.4. MyBatis缓存
&emsp; ......  


### 1.9.5. MyBatis插件解析
1. **<font color="clime">Mybaits插件的实现主要用了拦截器、责任链和动态代理。</font>** 动态代理可以对SQL语句执行过程中的某一点进行拦截，当配置多个插件时，责任链模式可以进行多次拦截。  
2. **<font color = "clime">mybatis扩展性很强，基于插件机制，基本上可以控制SQL执行的各个阶段，如执行器阶段，参数处理阶段，语法构建阶段，结果集处理阶段，具体可以根据项目业务来实现对应业务逻辑。</font>**   
    * 执行器Executor（update、query、commit、rollback等方法）；  
    * 参数处理器ParameterHandler（getParameterObject、setParameters方法）；  
    * 结果集处理器ResultSetHandler（handleResultSets、handleOutputParameters等方法）；  
    * SQL语法构建器StatementHandler（prepare、parameterize、batch、update、query等方法）；    


## 1.10. SpringBoot

### 1.10.1. SpringBoot基础知识


### 1.10.2. SpringBoot启动过程
&emsp; SpringApplication.run()中首先new SpringApplication对象，然后调用该对象的run方法。<font color = "red">即run()方法主要包括两大步骤：</font>  
1. 创建SpringApplication对象；  
2. 运行run()方法。  

#### 1.10.2.1. SpringApplication初始化
1. **<font color = "blue">构造过程一般是对构造函数的一些成员属性赋值，做一些初始化工作。</font><font color = "blue">SpringApplication有6个属性：`资源加载器`、资源类集合、应用类型、`初始化器`、`监听器`、`包含main函数的主类`。</font>**  
	1. 给resourceLoader属性赋值，resourceLoader属性，资源加载器，此时传入的resourceLoader参数为null；  
	2. **<font color = "clime">初始化资源类集合并去重。</font>** 给primarySources属性赋值，primarySources属性即`SpringApplication.run(MainApplication.class,args);`中传入的MainApplication.class，该类为SpringBoot项目的启动类，主要通过该类来扫描Configuration类加载bean；
	3. **<font color = "clime">判断当前是否是一个 Web 应用。</font>** 给webApplicationType属性赋值，webApplicationType属性，代表应用类型，根据classpath存在的相应Application类来判断。因为后面要根据webApplicationType来确定创建哪种Environment对象和创建哪种ApplicationContext；
	4. **<font color = "blue">设置应用上下文初始化器。</font>** 给initializers属性赋值，initializers属性为List<ApplicationContextInitializer<?\>>集合，利用SpringBoot的SPI机制从spring.factories配置文件中加载，后面在初始化容器的时候会应用这些初始化器来执行一些初始化工作。因为SpringBoot自己实现的SPI机制比较重要；  
	5. **<font color = "blue">设置监听器。</font>** 给listeners属性赋值，listeners属性为List<ApplicationListener<?\>>集合，同样利用SpringBoot的SPI机制从spring.factories配置文件中加载。因为SpringBoot启动过程中会在不同的阶段发射一些事件，所以这些加载的监听器们就是来监听SpringBoot启动过程中的一些生命周期事件的；
	6. **<font color = "clime">推断主入口应用类。</font>** 给mainApplicationClass属性赋值，mainApplicationClass属性表示包含main函数的类，即这里要推断哪个类调用了main函数，然后把这个类的全限定名赋值给mainApplicationClass属性，用于后面启动流程中打印一些日志。

2. **<font color = "clime">SpringApplication初始化中第4步和第5步都是利用SpringBoot的[SPI机制](/docs/java/basis/SPI.md)来加载扩展实现类。SpringBoot通过以下步骤实现自己的SPI机制：</font>**  
	1. 首先获取线程上下文类加载器;  
	2. 然后利用上下文类加载器从spring.factories配置文件中加载所有的SPI扩展实现类并放入缓存中；  
	3. 根据SPI接口从缓存中取出相应的SPI扩展实现类；  
	4. 实例化从缓存中取出的SPI扩展实现类并返回。  

#### 1.10.2.2. run()方法运行过程
1. **<font color = "clime">运行流程：创建所有Spring运行监听器并发布应用启动事件、准备环境变量、创建容器 ---> 容器准备（为刚创建的容器对象做一些初始化工作，准备一些容器属性值等）、刷新容器 ---> 执行刷新容器后的后置处理逻辑、调用ApplicationRunner和CommandLineRunner的run方法。</font>**  
2. **<font color = "clime">`内置生命周期事件：`</font>** <font color = "red">在SpringBoot启动过程中，每个不同的启动阶段会分别发布不同的内置生命周期事件。</font>  
3. **<font color = "clime">`事件回调机制：`</font>** <font color = "red">run()阶段涉及了比较重要的[事件回调机制](/docs/microService/SpringBoot/eventCallback.md)，回调4个监听器（ApplicationContextInitializer、ApplicationRunner、CommandLineRunner、SpringApplicationRunListener）中的方法与加载项目中组件到IOC容器中。</font>  

#### 1.10.2.3. SpringBoot事件监听机制
##### 1.10.2.3.1. 事件监听步骤
&emsp; **<font color = "clime">SpringBoot启动时广播生命周期事件步骤：</font>**    
1. 为广播SpringBoot内置生命周期事件做前期准备：    
    1. 首先加载ApplicationListener监听器实现类；  
    2. 其次加载SPI扩展类EventPublishingRunListener。  
2. SpringBoot启动时利用EventPublishingRunListener广播生命周期事件，然后ApplicationListener监听器实现类监听相应的生命周期事件执行一些初始化逻辑的工作。  
    1. 构建SpringApplication对象时<font color = "red">根据ApplicationListener接口去spring.factories配置文件中加载并实例化监听器。</font>  
    2. 在SpringBoot的启动过程中首先从spring.factories配置文件中加载并实例化EventPublishingRunListener对象。  
    3. 在SpringBoot的启动过程中会发布一系列SpringBoot内置的生命周期事件。  

##### 1.10.2.3.2. 内置生命周期事件
&emsp; **<font color = "clime">SpringBoot内置的7种生命周期事件</font>**  

|发布顺序|事件|用途|
|---|---|---|
|1|ApplicationStartingEvent|在SpringApplication启动时但在环境变量Environment或容器ApplicationContext创建前触发，标志SpringApplication开始启动。|
|2|ApplicationEnvironmentPreparedEvent|在SpringApplication已经开始启动且环境变量Environment已经准备好时触发，标志环境变量已经准备好。|
|3|ApplicationContextInitializedEvent|ApplicationContextInitilizers的初始化方法已经被调用即从spring.factories中加载的initializers已经执行ApplicationContext初始化逻辑但在bean定义加载前触发，标志Application已经初始化完毕。|
|4|ApplicationPreparedEvent|在Spring容器刷新refresh前触发。|
|5|ApplicationStaredEvent|在Spring容器刷新后触发，但在调用ApplicationRunner和CommandLineRunner的run方法调用前触发，标志Spring容器已经刷新，此时所有的bean实例等都已经加载了。|
|6|ApplicationReadyEvent|只要SpringApplication可以接收服务请求时即调用完ApplicationRunner和CommandLineRunner的run方法后触发，此时标志SpringApplication已经正在运行，即成功启动。|
|7|ApplicationFailedEvent|若SpringApplicaton未能成功启动时则会catch住异常发布ApplicationFailedEvent事件，标志ApplicationFailedEvent启动失败。|


#### 1.10.2.4. SpringBoot事件回调
&emsp; **<font color = "clime">SpringBoot事件回调：</font>**  

* **<font color = "red">ApplicationContextInitializer，IOC容器初始化时被回调；</font>**  
* **<font color = "red">SpringApplicationRunListener，SpringBoot启动过程中多次被回调；</font>**  
* **<font color = "red">ApplicationRunner，容器启动完成后被回调；</font>**  
* **<font color = "red">CommandLineRunner，ApplicationRunner之后被回调。</font>**  

### 1.10.3. SpringBoot自动配置
&emsp; 包含两部分：1. 注解@SpringBootApplication；2. 加载自动配置流程。


#### 1.10.3.1. 注解@SpringBootApplication
1. @SpringBootApplication  
    * @ComponentScan  
    * @SpringBootConfiguration  
    * @EnableAutoConfiguration  
2. @EnableAutoConfiguration使用@Import将所有符合自动配置条件的bean定义加载到IOC容器。   
    1. @Import({AutoConfigurationImportSelector.class})，开启自动配置，导入了AutoConfigurationImportSelector类。  
    2. AutoConfigurationImportSelector#getCandidateConfigurations()方法获取配置文件spring.factories所有候选的配置，剔除重复部分，再剔除@SpringbootApplication注解里exclude的配置，才得到最终的配置类名集合。  


#### 1.10.3.2. 加载自动配置流程
1. 启动对象的注入：在SpringBoot启动流程的`容器准备阶段`prepareContext()会将@SpringBootApplication--->@Component对象注册到容器中。  
2. 自动装配入口，从SpringBoot启动流程的`刷新容器阶段`refresh()开始。 

#### 1.10.3.3. 内置Tomcat
1. SpringBoot内置Tomcat，可以对比SpringBoot自动配置运行流程了解。  
2. Tomcat的自动装配：自动装配过程中，Web容器所对应的自动配置类为ServletWebServerFactoryAutoConfiguration，该类导入了EmbeddedTomcat，EmbeddedJetty，EmbeddedUndertow三个类，可以根据用户的需求去选择使用哪一个web服务器，默认情况下使用的是tomcat。  
3. Tomcat的启动：在容器刷新refreshContext(context)步骤完成。  


### 1.10.4. 自定义strater

## 1.11. SpringCloud
&emsp; **<font color = "clime">Spring Cloud各组件运行流程：</font>**  
1. 外部或者内部的非Spring Cloud项目都统一通过微服务网关(Zuul)来访问内部服务。客户端的请求首先经过负载均衡(Ngnix)，再到达服务网关(Zuul集群)；  
2. 网关接收到请求后，从注册中心(Eureka)获取可用服务；  
3. 由Ribbon进行均衡负载后，分发到后端的具体实例；  
4. 微服务之间也可通过Feign进行通信处理业务；  
5. Hystrix负责处理服务超时熔断；Hystrix dashboard，Turbine负责监控Hystrix的熔断情况，并给予图形化的展示；  
6. Turbine监控服务间的调用和熔断相关指标；  
7. 服务的所有的配置文件由配置服务管理，配置服务的配置文件放在git仓库，方便开发人员随时改配置。  

### 1.11.1. Eureka
1. 服务治理框架和产品都围绕着服务注册与服务发现机制来完成对微服务应用实例的自动化管理。  
2. Spring Cloud Eureka，使用Netflix Eureka来实现服务注册与发现，它既包含了服务端组件，也包含了客户端组件。  
3. Eureka服务治理体系包含三个核心角色：服务注册中心、服务提供者以及服务消费者。  
    * 服务提供者  
    &emsp; 服务同步、服务续约。
    * 服务消费者    
    &emsp; 荻取服务、服务调用、服务下线。
    * 服务注册中心  
        * 失效剔除  
        * 自我保护：  
        &emsp; 开启自我保护后，Eureka Server在运行期间，会统计心跳失败的比例在15分钟之内是否低于85%，如果出现低于的情况（在单机调试的时候很容易满足，实际在生产环境上通常是由于网络不稳定导致），Eureka Server会将当前的实例注册信息保护起来，让这些实例不会过期，尽可能保护这些注册信息。  
        &emsp; Eureka Server进入自我保护状态后，客户端很容易拿到实际已经不存在的服务实例，会出现调用失败的清况。  
4. <font color = "red">高可用注册中心/AP模型：EurekaServer的高可用实际上就是将自己作为服务向其他服务注册中心注册自己，这样就可以形成一组互相注册的服务注册中心，以实现服务清单的互相同步，达到高可用的效果。</font>  


### 1.11.2. Ribbon

### 1.11.3. Hytrix
1. 服务雪崩：在微服务架构中，存在着那么多的服务单元，若一个单元出现故障，就很容易因依赖关系而引发故障的蔓延，最终导致整个系统的瘫痪。  
2. 熔断是一种[降级](/docs/microService/thinking/Demotion.md)策略。Hystrix中的降级方案：熔断触发降级、请求超时触发降级、资源（信号量、线程池）隔离触发降级 / 依赖隔离。  
&emsp; <font color = "clime">熔断的对象是服务之间的请求；`熔断策略有根据请求的数量分为信号量和线程池，还有请求的时间（即超时熔断），请求错误率（即熔断触发降级）。`</font>  
3. Hystrix工作流程：1. 包装请求 ---> 2. 发起请求 ---> 3. 缓存处理 ---> 4. 判断断路器是否打开（熔断） ---> 5. 判断是否进行业务请求（请求是否需要隔离或降级） ---> 6. 执行业务请求 ---> 7. 健康监测 ---> 8. fallback处理或返回成功的响应。  
4. <font color = "clime">微服务集群中，Hystrix的度量信息通过`Turbine`来汇集监控信息，并将聚合后的信息提供给Hystrix Dashboard来集中展示和监控。</font>  

### 1.11.4. Feign

### 1.11.5. Zuul
1. Spring Cloud Zuul，微服务网关，包含hystrix、ribbon、actuator。主要有路由转发、请求过滤功能。  
2. **<font color = "red">Zuul提供了四种过滤器的API，分别为前置（Pre）、路由（Route）、后置（Post）和错误（Error）四种处理方式。</font>**  
3. 动态加载：  
&emsp; 作为最外部的网关，它必须具备动态更新内部逻辑的能力，比如动态修改路由规则、动态添加／删除过滤器等。  
&emsp; 通过Zuul实现的API网关服务具备了动态路由和动态过滤的器能力，可以在不重启API网关服务的前提下为其动态修改路由规则和添加或删除过滤器。   


### 1.11.6. Sleuth
1. **<font color = "red">埋点日志通常包含：traceId、spanId、调用的开始时间，协议类型、调用方ip和端口，请求的服务名、调用耗时，调用结果，异常信息等，同时预留可扩展字段，为下一步扩展做准备；</font>**  
2. spring-cloud-starter-sleuth功能点：
    1. 有关traceId：获取当前traceId、日志获取traceId、传递traceId到异步线程池、子线程或线程池中获取Zipkin traceId并打印。  
    2. sleuth自定义信息。增加自定义属性、添加自定义Tag标签。  
    3. 监控本地方法：异步执行和远程调用都会新开启一个Span，如果想监控本地的方法耗时时间，可以采用埋点的方式监控本地方法，也就是开启一个新的Span。  
    4. ...  


### 1.11.7. Admin
1. spring-boot-starter-actuator依赖中已经实现的一些原生端点。根据端点的作用，<font color = "clime">可以将原生端点分为以下三大类：</font>  
    * 应用配置类：获取应用程序中加载的应用配置、环境变量、自动化配置报告等与Spring Boot应用密切相关的配置类信息。  
    * 度量指标类：获取应用程序运行过程中用于监控的度量指标，比如内存信息、线程池信息、HTTP请求统计等。  
    * 操作控制类：提供了对应用的关闭等操作类功能。  
2. 监控通知
3. 集成spring security


## 1.12. Dubbo
### 1.12.1. Dubbo和Spring Cloud
&emsp; Dubbo是SOA时代的产物，它的关注点主要在于服务的调用，流量分发、流量监控和熔断。  
&emsp; Spring Cloud诞生于微服务架构时代，考虑的是微服务治理的方方面面，另外由于依托了Spirng、Spirng Boot的优势之上。  

* 两个框架在开始目标就不一致：<font color = "red">Dubbo定位服务治理；Spirng Cloud是一个生态。</font>  
* <font color = "red">Dubbo底层是使用Netty这样的NIO框架，是基于TCP协议传输的，配合以Hession序列化完成RPC通信。</font><font color = "clime">而SpringCloud是基于Http协议+Rest接口调用远程过程的通信，</font>相对来说，Http请求会有更大的报文，占的带宽也会更多。但是REST相比RPC更为灵活，服务提供方和调用方的依赖只依靠一纸契约，不存在代码级别的强依赖，这在强调快速演化的微服务环境下，显得更为合适，至于注重通信速度还是方便灵活性，具体情况具体考虑。

### 1.12.2. RPC介绍
&emsp; ......  


### 1.12.3. Dubbo介绍
1. Dubbo的工作原理：  
    1. 服务启动的时候，provider和consumer根据配置信息，连接到注册中心register，分别向注册中心注册和订阅服务。  
    2. register根据服务订阅关系，返回provider信息到consumer，同时consumer会把provider信息缓存到本地。如果信息有变更，consumer会收到来自register的推送。  
    3. consumer生成代理对象，同时根据负载均衡策略，选择一台provider，同时定时向monitor记录接口的调用次数和时间信息。  
    4. 拿到代理对象之后，consumer通过`代理对象`发起接口调用。  
    5. provider收到请求后对数据进行反序列化，然后通过代理调用具体的接口实现。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-52.png)   

2. **<font color = "red">为什么要通过代理对象通信？</font>**    
    &emsp; dubbo实现接口的透明代理，封装调用细节，让用户可以像调用本地方法一样调用远程方法，同时还可以通过代理实现一些其他的策略，比如：负载、降级等。让用户可以像调用本地方法一样调用远程方法，同时还可以通过代理实现一些其他的策略，比如：  
    1. 调用的负载均衡策略  
    2. 调用失败、超时、降级和容错机制  
    3. 做一些过滤操作，比如加入缓存、mock数据  
    4. 接口调用数据统计  

### 1.12.4. Dubbo框架设计
1. 分层架构设计
    1. 从大的范围来说，dubbo分为三层：
        * business业务逻辑层由开发人员来提供接口和实现还有一些配置信息。
        * RPC层就是真正的RPC调用的核心层，封装整个RPC的调用过程、负载均衡、集群容错、代理。
        * remoting则是对网络传输协议和数据转换的封装。  
    2. RPC层包含config，配置层、proxy，代理层、register，服务注册层、cluster，路由层、monitor，监控层、protocol，远程调用层。    
        1. **<font color = "red">proxy，服务代理层：服务接口透明代理，生成服务的客户端Stub和服务器端Skeleton，以ServiceProxy为中心，扩展接口为ProxyFactory。</font>**  
        &emsp; **<font color = "red">Proxy层封装了所有接口的透明化代理，而在其它层都以Invoker为中心，</font><font color = "blue">只有到了暴露给用户使用时，才用Proxy将Invoker转成接口，或将接口实现转成 Invoker，也就是去掉Proxy层RPC是可以Run的，只是不那么透明，不那么看起来像调本地服务一样调远程服务。</font>**  
        &emsp; dubbo实现接口的透明代理，封装调用细节，让用户可以像调用本地方法一样调用远程方法，同时还可以通过代理实现一些其他的策略，比如：负载、降级......  
        2. **<font color = "red">protocol，远程调用层：封装RPC调用，以Invocation, Result为中心，扩展接口为Protocol, Invoker, Exporter。</font>**
    3. remoting层：  
        1. 网络传输层：抽象mina和netty为统一接口，以Message为中心，扩展接口为Channel, Transporter, Client, Server, Codec。  
        2. 数据序列化层：可复用的一些工具，扩展接口为Serialization, ObjectInput, ObjectOutput, ThreadPool。  
2. 总体调用过程  


### 1.12.5. 暴露和引用服务
1. 解析服务：  
&emsp; **<font color = "clime">基于dubbo.jar内的META-INF/spring.handlers配置，Spring在遇到dubbo名称空间时，会回调DubboNamespaceHandler。所有dubbo的标签，都统一用DubboBeanDefinitionParser进行解析，基于一对一属性映射，将XML标签解析为Bean对象。</font>**  
&emsp; ⚠️注：在暴露服务ServiceConfig.export()或引用服务ReferenceConfig.get()时，会将Bean对象转换URL格式，所有Bean属性转成URL的参数。然后将URL传给协议扩展点，基于扩展点的扩展点自适应机制，根据URL的协议头，进行不同协议的服务暴露或引用。  
2. **服务提供者暴露服务的主过程：**  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-29.png)   
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-53.png)   
    1. ServiceConfig将Bean对象解析成URL格式。  
    2. 通过ProxyFactory类的getInvoker方法使用ref(实际类)生成一个AbstractProxyInvoker实例。`ProxyFactory #getInvoker(T proxy, Class<T> type, URL url)`  
    3. 通过Protocol(协议)类的export方法暴露服务。`DubboProtocol #export(Invoker<T> invoker)`  
        1. 本地各种协议暴露。  
        2. 注册中心暴露。  
    4. 如果通过注册中心暴露服务，RegistryProtocol保存URL地址和invoker的映射关系，同时注册到服务中心。  
3. **服务消费者引用服务的主过程：**  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-30.png)   
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-54.png)   
    1. ReferenceConfig解析引用的服务。  
    2. ReferenceConfig类的init方法调用Protocol的refer方法生成Invoker实例。  
    3. 把Invoker转换为客户端需要的接口。  

### 1.12.6. 服务调用
#### 1.12.6.1. 服务调用介绍


#### 1.12.6.2. Dubbo序列化和协议
1. 不同服务在性能上适用不同协议进行传输，比如大数据用短连接协议，小数据大并发用长连接协议。  
2. 默认使用Hessian序列化，还有Duddo、FastJson、Java自带序列化。   

##### 1.12.6.2.1. Dubbo协议长连接
&emsp; Dubbo缺省协议采用单一长连接和NIO异步通讯，适合于小数据量大并发的服务调用，以及服务消费者机器数远大于服务提供者机器数的情况。  
&emsp; 注意：Dubbo缺省协议不适合传送大数据量的服务，比如传文件，传视频等，除非请求量很低。  
&emsp; Dubbo协议采用长连接，还可以防止注册中心宕机风险。  


### 1.12.7. Dubbo集群容错
1. 负载均衡
2. 集群容错策略
3. 服务降级

### 1.12.8. 扩展点加载(SPI)
1. **<font color = "clime">Dubbo改进了JDK标准的SPI的以下问题：</font>**  
    * JDK标准的SPI会一次性实例化扩展点所有实现。  
    * 如果扩展点加载失败，连扩展点的名称都拿不到了。  
    * 增加了对扩展点IoC和AOP的支持，一个扩展点可以直接setter注入其它扩展点。  
2. 扩展点特性
    * 扩展点自动包装，Wrapper机制
    * 扩展点自动装配
    * 扩展点自适应
    * 扩展点自动激活

## 1.13. Zookeeper

1. **<font color = "clime">Zookeeper是一个分布式协调服务的开源框架。主要用来解决分布式集群中应用系统的一致性问题。</font>**  
2. `ZK服务端`通过`ZAB协议`保证`数据顺序一致性`。  
    1. ZK服务端通过`ZAB协议`保证`数据顺序一致性`。  
    2. ZAB协议：
        1. **<font color = "clime">崩溃恢复</font>**  
            * 服务器启动时的leader选举：每个server发出投票，投票信息包含(myid, ZXID,epoch)；接受投票；处理投票(epoch>ZXID>myid)；统计投票；改变服务器状态。</font>  
            * 运行过程中的leader选举：变更状态 ---> 发出投票 ---> 处理投票 ---> 统计投票 ---> 改变服务器的状态。
        2. **<font color = "clime">`消息广播（数据读写流程，读写流程）：`</font>**  
            &emsp; 在zookeeper中，客户端会随机连接到zookeeper集群中的一个节点。    
            * 如果是读请求，就直接从当前节点中读取数据。  
            * 如果是写请求，那么请求会被转发给 leader 提交事务，然后leader会广播事务，只要有超过半数节点写入成功，那么写请求就会被提交。   
            &emsp; ⚠️注：leader向follower写数据详细流程：类2pc(两阶段提交)。  
    3.  数据一致性  
        &emsp; **<font color = "red">Zookeeper保证的是CP，即一致性(Consistency)和分区容错性(Partition-Tolerance)，而牺牲了部分可用性(Available)。</font>**  
        * 为什么不满足AP模型？<font color = "red">zookeeper在选举leader时，会停止服务，直到选举成功之后才会再次对外提供服务。</font>
        * Zookeeper的CP模型：非强一致性， **<font color = "clime">而是单调一致性/顺序一致性。</font>**  
            1. <font color = "clime">假设有2n+1个server，在同步流程中，leader向follower同步数据，当同步完成的follower数量大于n+1时同步流程结束，系统可接受client的连接请求。</font><font color = "red">如果client连接的并非同步完成的follower，那么得到的并非最新数据，但可以保证单调性。</font> 未同步数据的情况，Zookeeper提供了同步机制（可选型），类似回调。   
            2. follower接收写请求后，转发给leader处理；leader完成两阶段提交的机制。向所有server发起提案，当提案获得超过半数(n+1)的server认同后，将对整个集群进行同步，超过半数(n+1)的server同步完成后，该写请求完成。如果client连接的并非同步完成follower，那么得到的并非最新数据，但可以保证单调性。  
3. 服务端脑裂：过半机制，要求集群内的节点数量为2N+1。  
4. zookeeper引入了`watcher机制`来实现`客户端和服务端`的发布/订阅功能。  
    1. Watcher机制运行流程：Zookeeper客户端向服务端的某个Znode注册一个Watcher监听，当服务端的一些指定事件触发了这个Watcher，服务端会向指定客户端发送一个事件通知来实现分布式的通知功能，然后客户端根据Watcher通知状态和事件类型做出业务上的改变。  
    &emsp; 触发watch事件种类很多，如：节点创建，节点删除，节点改变，子节点改变等。  
    2.  **watch的重要特性：**  
        * 异步发送
        * 一次性触发：  
        &emsp; Watcher通知是一次性的， **<font color = "clime">即一旦触发一次通知后，该Watcher就失效了，因此客户端需要反复注册Watcher。</font>** 但是在获取watch事件和设置新的watch事件之间有延迟。延迟为毫秒级别，理论上会出现不能观察到节点的每一次变化。  
        &emsp; `不支持用持久Watcher的原因：如果Watcher的注册是持久的，那么必然导致服务端的每次数据更新都会通知到客户端。这在数据变更非常频繁且监听客户端特别多的场景下，ZooKeeper无法保证性能。`  
        * 有序性：  
        &emsp; 客户端先得到watch通知才可查看节点变化结果。  
    3. 客户端过多，会引发网络风暴。  
5. ZK的弊端：
	1. 服务端从节点多，主从同步慢。  
	2. 客户端多，`网络风暴`。~~watcher机制中，回调流程，只有主节点参与？~~  
6. Zookeeper应用场景：统一命名服务，生成分布式ID、分布式锁、队列管理、元数据/配置信息管理，数据发布/订阅、分布式协调、集群管理，HA高可用性。  




