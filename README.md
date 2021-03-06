<!-- TOC -->

- [wt1814-note](#wt1814-note)
    - [侃侃Java](#侃侃java)
    - [Java](#java)
        - [Java基础](#java基础)
        - [设计模式](#设计模式)
        - [JVM](#jvm)
        - [并发编程](#并发编程)
    - [数据库](#数据库)
    - [SSM](#ssm)
        - [Spring](#spring)
        - [MyBatis](#mybatis)
    - [分布式](#分布式)
        - [SpringBoot](#springboot)
        - [SpringCloud](#springcloud)
        - [Dubbo](#dubbo)
        - [Zookeeper](#zookeeper)
        - [分布式缓存](#分布式缓存)
            - [Redis](#redis)
        - [限流降级](#限流降级)
        - [分布式消息队列](#分布式消息队列)
            - [kafka](#kafka)
        - [分布式高并发](#分布式高并发)
        - [分布式理论](#分布式理论)
        - [分布式ID](#分布式id)
        - [分布式事务](#分布式事务)
        - [分布式锁](#分布式锁)
        - [任务调度](#任务调度)
        - [分布式搜索引擎](#分布式搜索引擎)
        - [分布式通信](#分布式通信)
    - [~~软件工程~~](#软件工程)
    - [项目构建基础](#项目构建基础)
    - [架构设计](#架构设计)
    - [DDD](#ddd)
    - [负载均衡](#负载均衡)
    - [Linux](#linux)
    - [DevOps](#devops)
    - [计算机网络](#计算机网络)
    - [测试工具](#测试工具)
    - [常用工具](#常用工具)
    - [Error](#error)
    - [项目总结](#项目总结)
    - [算法](#算法)

<!-- /TOC -->

# wt1814-note  

## 侃侃Java  
[知识点总结](/docs/java/KnowledgePoints.md)  
[学习不要学的片面](/docs/life/oneSided.md)  

## Java  
### Java基础
[Java基础](/docs/java/basis/JavaBasic.md)  
[Java基本数据类型](/docs/java/basis/DataType.md)  
&emsp; [Object](/docs/java/basis/Object.md)  
&emsp; [String](/docs/java/basis/String.md)  
&emsp; [Java基础数据类型](/docs/java/basis/BasicsDataType.md)  
&emsp; [Java对象大小](/docs/java/basis/ObjectSize.md)  
[Java符号](/docs/java/basis/mark.md)  
[Java集合框架](/docs/java/Collection/CollectionFramework.md)  
&emsp; [HashMap](/docs/java/Collection/HashMap.md)  
&emsp; [Collection](/docs/java/Collection/Collection.md)  
[JDK1.8](/docs/java/JDK8/JDK8.md)  
&emsp; [Lambda](/docs/java/JDK8/Lambda.md)  
&emsp; [Stream](/docs/java/JDK8/Stream.md)  
&emsp; [Optional](/docs/java/JDK8/Optional.md)  
&emsp; [DateTime](/docs/java/JDK8/DateTime.md)  
[Java异常](/docs/java/basis/JavaException.md)  
[Java范型](/docs/java/basis/JavaParadigm.md)  
[Java反射](/docs/java/basis/JavaReflex.md)  
[自定义注解](/docs/java/basis/annotation.md)  
[IO](/docs/java/basis/JavaIO.md)  
[SPI](/docs/java/basis/SPI.md)  
[SDK](/docs/java/basis/SDK.md)  


### 设计模式  
[七大设计原则](/docs/java/Design/principles.md)  
&emsp; [面向抽象和面向接口](/docs/java/Design/abstract.md)  
&emsp; [继承和组合/复用规则](/docs/java/Design/compose.md)  
[Java设计模式](/docs/java/Design/design.md)  
&emsp; [创建型设计模式](/docs/java/Design/establish.md)  
&emsp; &emsp; [单例模式](/docs/java/Design/singleton.md)  
&emsp; &emsp; [简单工厂模式](/docs/java/Design/factory.md)  
&emsp; &emsp; [抽象工厂模式](/docs/java/Design/AbstractFactory.md)  
&emsp; &emsp; [建造者模式](/docs/java/Design/build.md)  
&emsp; &emsp; [原型模式](/docs/java/Design/prototype.md)  
&emsp; [结构型设计模式](/docs/java/Design/structure.md)  
&emsp; &emsp; [代理模式](/docs/java/Design/proxy.md)   
&emsp; &emsp; [JDK动态代理](/docs/java/Design/DynamicProxy.md)   
&emsp; &emsp; [CGLIB代理](/docs/java/Design/CGLIB.md)   
&emsp; &emsp; [装饰者模式](/docs/java/Design/decorator.md)   
&emsp; &emsp; [适配器模式](/docs/java/Design/adapter.md)   
&emsp; &emsp; [门面模式](/docs/java/Design/facade.md)   
&emsp; [行为型设计模式](/docs/java/Design/behavior.md)  
&emsp; &emsp; [策略模式](/docs/java/Design/strategy.md)   
&emsp; &emsp; [模板方法模式](/docs/java/Design/template.md)   
&emsp; &emsp; [观察者模式](/docs/java/Design/observer.md)   
&emsp; &emsp; [责任链模式](/docs/java/Design/chain.md)   
[设计模式大讨论](/docs/java/Design/discuss.md)  

### JVM  
[JVM总结](/docs/java/JVM/summary.md)  
[JDK、JRE、JVM](/docs/java/JVM/JDK、JRE、JVM.md)   
[字节码文件](/docs/java/JVM/Class.md)  
[类加载](/docs/java/JVM/classLoading.md)  
&emsp; [JVM类的加载](/docs/java/JVM/classLoad.md)  
&emsp; [JVM类加载器](/docs/java/JVM/classLoader.md)  
&emsp; [对象的生命周期](/docs/java/JVM/ObjectPeriod.md)  
[内存结构](/docs/java/JVM/Memory.md)  
&emsp; [JVM内存结构](/docs/java/JVM/JVMMemory.md)  
&emsp; [常量池详解](/docs/java/JVM/ConstantPool.md)  
&emsp; [逃逸分析](/docs/java/JVM/escape.md)  
&emsp; [内存中的对象](/docs/java/JVM/MemoryObject.md)  
&emsp; [内存泄露](/docs/java/JVM/MemoryLeak.md)  
[编译执行](/docs/java/JVM/run.md)  
[GC](/docs/java/JVM/GC.md)  
&emsp; [GC-回收对象](/docs/java/JVM/GCProject.md)   
&emsp; [GC-回收位置/安全点](/docs/java/JVM/safePoint.md)  
&emsp; [回收算法与分代回收](/docs/java/JVM/generationRecovery.md)  
&emsp; [GC-垃圾回收器](/docs/java/JVM/GCReclaimer.md)  
&emsp; [CMS回收器](/docs/java/JVM/CMS.md)  
&emsp; [G1回收器](/docs/java/JVM/G1.md)  
&emsp; [三色标记](/docs/java/JVM/TriMark.md)  
[调优](/docs/java/JVM/tuning.md)  
&emsp; [JVM调优-基础](/docs/java/JVM/TuningBasic.md)  
&emsp; [JVM调优](/docs/java/JVM/tuningProblem.md)  
&emsp; [JVM问题排查](/docs/java/JVM/TroubleShooting.md)  
&emsp; [Arthas工具](/docs/java/JVM/Arthas.md)  
&emsp; [JAVA线上故障排查](/docs/Linux/problem.md)  


<!-- 
&emsp; [JVM排查案例](/docs/java/JVM/case.md)  
-->

### 并发编程  
[并发编程总结](/docs/java/concurrent/summary.md)  
[线程Thread](/docs/java/concurrent/thread.md)  
&emsp; [线程基本知识](/docs/java/concurrent/threadConcepts.md)  
&emsp; [Thread类详解](/docs/java/concurrent/threadClass.md)  
&emsp; &emsp; [线程停止与中断](/docs/java/concurrent/interrupt.md)  
&emsp; [线程基本操作](/docs/java/concurrent/threadOperation.md)  
&emsp; [CompletableFuture](/docs/java/concurrent/CompletableFuture.md)  
&emsp; [CompletionService](/docs/java/concurrent/CompletionService.md)  
[并发编程](/docs/java/concurrent/MultiThread.md)  
&emsp; [并发编程原理](/docs/java/concurrent/ConcurrentPrinciple.md)  
&emsp; &emsp; [CPU缓存及JMM](/docs/java/concurrent/JMM.md)  
&emsp; &emsp; [并发安全问题产生原因](/docs/java/concurrent/ConcurrencyProblem.md)  
&emsp; &emsp; [并发安全解决底层](/docs/java/concurrent/ConcurrencySolve.md)  
&emsp; &emsp; [伪共享问题](/docs/java/concurrent/PseudoSharing.md)  
&emsp; [线程安全解决方案](/docs/java/concurrent/ThreadSafety.md)  
&emsp; &emsp; [Synchronized应用](/docs/java/concurrent/SynApply.md)  
&emsp; &emsp; [Synchronized底层原理](/docs/java/concurrent/SynBottom.md)  
&emsp; &emsp; [Synchronized优化](/docs/java/concurrent/SynOptimize.md)  
&emsp; &emsp; [Volatile](/docs/java/concurrent/Volatile.md)  
&emsp; &emsp; [ThreadLocal原理](/docs/java/concurrent/ThreadLocal.md)  
&emsp; &emsp; [ThreadLocal应用](/docs/java/concurrent/ThreadLocalUse.md)  
&emsp; [线程通信(生产者消费者问题)](/docs/java/concurrent/ThreadCommunication.md)  
&emsp; [线程活跃性](/docs/java/concurrent/Activity.md)  
[线程池](/docs/java/concurrent/ThreadPool.md)  
&emsp; [ThreadPoolExecutor](/docs/java/concurrent/ThreadPoolExecutor.md)  
&emsp; [ForkJoinPool](/docs/java/concurrent/ForkJoinPool.md)  
&emsp; [线程池的正确使用](/docs/java/concurrent/ThreadPoolUse.md)  
[J.U.C包](/docs/java/concurrent/ConcurrentPackage.md)  
&emsp; [CAS](/docs/java/concurrent/CAS.md)  
&emsp; [AQS](/docs/java/concurrent/AQS.md)  
&emsp; &emsp; [LockSupport](/docs/java/concurrent/LockSupport.md)  
&emsp; [Lock](/docs/java/concurrent/Lock.md)  
&emsp; &emsp; [ReentrantLock](/docs/java/concurrent/ReentrantLock.md)  
&emsp; &emsp; [Condition](/docs/java/concurrent/Condition.md)  
&emsp; &emsp; [ReadWriteLock](/docs/java/concurrent/ReadWriteLock.md)  
&emsp; [Atmoic](/docs/java/concurrent/Atmoic.md)  
&emsp; &emsp; [AtomicStampedReference与AtomicMarkableReference](/docs/java/concurrent/AtomicStampedReference.md)  
&emsp; &emsp; [LongAdder](/docs/java/concurrent/LongAdder.md)  
&emsp; [Collections](/docs/java/concurrent/jihe.md)  
&emsp; &emsp; [ConcurrentHashMap](/docs/java/concurrent/ConcurrentHashMap.md)  
&emsp; &emsp; [BlockingQueue](/docs/java/concurrent/BlockingQueue.md)  
&emsp; [tools](/docs/java/concurrent/tools.md)  
&emsp; &emsp; [CountDownLatch](/docs/java/concurrent/CountDownLatch.md)  
&emsp; &emsp; [CyclicBarrier](/docs/java/concurrent/CyclicBarrier.md)  
&emsp; &emsp; [Semaphore](/docs/java/concurrent/Semaphore.md)  
&emsp; &emsp; [Exchanger](/docs/java/concurrent/Exchanger.md)  
[获取多线程执行结果](/docs/java/concurrent/execResult.md)  
<!-- 
[并发框架Disruptor](/docs/java/concurrent/disruptor.md)  
-->

## 数据库  
[学习SQL的总结](/docs/SQL/SQLSummary.md)  
[数据建模](/docs/SQL/modeling.md)  
[SQL语句](/docs/SQL/SQLSentence.md)  
&emsp; [基本查询语句](/docs/SQL/basicSelect.md)  
&emsp; &emsp; [limit](/docs/SQL/limit.md)  
&emsp; [连接查询](/docs/SQL/joinSelect.md)  
&emsp; [高级查询](/docs/SQL/trans.md)  
[MySql函数](/docs/SQL/MySQLFunction.md)  
[数据库对象](/docs/SQL/DatabaseObject.md)  
[SQL优化](/docs/SQL/SQLOptimization.md)  
&emsp; [SQL分析](/docs/SQL/Analysis.md)  
&emsp; &emsp; [explain](/docs/SQL/explain.md)  
&emsp; [~~SQL语句优化~~](/docs/SQL/SQLStatement.md)  
&emsp; [索引优化](/docs/SQL/index.md)  
&emsp; [优化案例](/docs/SQL/case.md)  
&emsp; [大数据量操作](/docs/SQL/largeData.md)  
&emsp; [碎片优化](/docs/SQL/Fragment.md)  
[数据库分布式](/docs/SQL/DistributedDatabase.md)  
&emsp; [主从复制](/docs/SQL/replication.md)  
&emsp; &emsp; [主从复制的问题](/docs/SQL/replicationProblem.md)  
&emsp; [分区](/docs/SQL/partition.md)  
&emsp; [分库分表](/docs/SQL/sub.md)  
&emsp; &emsp; [分库分表带来的问题](/docs/SQL/subProblem.md)  
&emsp; &emsp; [分库分表查询](/docs/SQL/subSelect.md)  
&emsp; [数据迁移](/docs/projectImplement/implementation.md)  
&emsp; [亿级订单系统](/docs/SQL/million.md)  
&emsp; [数据库分布式实现](/docs/SQL/subRealize.md)  
&emsp; [MyCat中间件](/docs/SQL/MyCat.md)  
&emsp; [ShardingSphere](/docs/SQL/ShardingSphere.md)  
&emsp; [Sharding-JDBC](/docs/SQL/Sharding-JDBC.md)  
[数据库连接池](/docs/SQL/connectionPool.md)  
[MySQL运维](/docs/SQL/MySqlMonitor.md)  

[MySql原理](/docs/SQL/MySqlPrinciple.md)   
&emsp; [MySql架构](/docs/SQL/Framework.md)   
&emsp; [MySql存储引擎](/docs/SQL/MySqlStorage.md)  
&emsp; [InnoDB体系结构](/docs/SQL/InnoDB.md)  
&emsp; [InnoDB内存结构-性能](/docs/SQL/memory.md)  
&emsp; &emsp; [BufferPool](/docs/SQL/bufferPoolNew.md)  
&emsp; &emsp; [脏页落盘，CheckPoint](/docs/SQL/CheckPoint.md)  
&emsp; &emsp; [ChangeBuffer](/docs/SQL/ChangeBuffer.md)  
&emsp; &emsp; [AdaptiveHashIndex](/docs/SQL/AdaptiveHashIndex.md)  
&emsp; [InnoDB磁盘结构-可靠性](/docs/SQL/disk.md)  
&emsp; &emsp; [表空间](/docs/SQL/TableSpace.md)  
&emsp; &emsp; [MySql事务日志](/docs/SQL/log.md)  
&emsp; &emsp; &emsp; [undoLog和binLog](/docs/SQL/undoLog.md)  
&emsp; &emsp; &emsp; [redoLog与DoubleWrite](/docs/SQL/redoLog.md)  
&emsp; &emsp; &emsp; [binLog使用](/docs/SQL/binLogUse.md)  
&emsp; &emsp; [两阶段提交和崩溃恢复](/docs/SQL/CrashRecovery.md)  
&emsp; [小结：insert插入流程](/docs/SQL/insert.md)  

&emsp; [索引底层原理](/docs/SQL/IndexPrinciple.md)  
&emsp; [各种索引](/docs/SQL/IndexKnowledge.md)  
&emsp; [MySql事务](/docs/SQL/transaction.md)  
&emsp; [MySql-MVCC](/docs/SQL/MVCC.md)  
&emsp; [MySql锁](/docs/SQL/lock.md)  
&emsp; [MySql死锁和锁表](/docs/SQL/LockProblem.md)  

<!-- 
&emsp; [HikariCP原理](/docs/SQL/HikariCPPrinciple.md)  
&emsp; [HikariCP监控与故障排查](/docs/SQL/HikariCPMonitor.md)  
-->

## SSM  
### Spring  
[Spring](/docs/SSM/Spring/Spring.md)  
[学习Spring源码的感悟](/docs/SSM/Spring/thinking.md)  
[SpringIOC解析](/docs/SSM/Spring/SpringIOC.md)  
&emsp; [容器初始化详解](/docs/SSM/Spring/ContainerInit.md)  
[SpringDI解析](/docs/SSM/Spring/SpringDI.md)  
&emsp; [循环依赖](/docs/SSM/Spring/feature/CircularDepend.md)  
[SpringBean生命周期](/docs/SSM/Spring/SpringBean.md)  
[容器相关特性](/docs/SSM/Spring/feature/ContainerFeature.md)  
&emsp; [FactoryBean](/docs/SSM/Spring/feature/FactoryBean.md)  
&emsp; [可二次开发常用接口](/docs/SSM/Spring/feature/SecendDeve.md)  
&emsp; &emsp; [事件多播器](/docs/SSM/Spring/feature/EventMulticaster.md)  
&emsp; &emsp; [事件](/docs/SSM/Spring/feature/Event.md)  
&emsp; &emsp; [Aware接口](/docs/SSM/Spring/feature/Aware.md)  
&emsp; &emsp; [后置处理器](/docs/SSM/Spring/feature/BeanFactoryPostProcessor.md)  
&emsp; &emsp; [InitializingBean](/docs/SSM/Spring/feature/InitializingBean.md)  
&emsp; [自定义XML schema扩展](/docs/SSM/Spring/feature/XMLSchema.md)  

[AOP基本概念](/docs/SSM/Spring/AOP.md)  
[SpringAOP教程](/docs/SSM/Spring/SpringAOP.md)  
[SpringAOP解析](/docs/SSM/Spring/SpringAOPAnalysis.md)  
[Spring事务](/docs/SSM/Spring/SpringTransaction.md)  

[SpringMVC使用教程](/docs/SSM/Spring/SpringMVCUse.md)  
[SpringMVC解析](/docs/SSM/Spring/SpringMVCAnalysis.md)    
[过滤器、拦截器、监听器](docs/web/subassembly.md)  

### MyBatis  
[MyBatis使用](/docs/SSM/MyBatis/MybatisUse.md)  
&emsp; [MyBatis使用教程](/docs/SSM/MyBatis/Mybatis.md)  
&emsp; [MyBatis高级使用](/docs/SSM/MyBatis/MybatisSenior.md)  
&emsp; [PageHelper](/docs/SSM/MyBatis/PageHelper.md)  
[MyBatis解析](/docs/SSM/MyBatis/MybatisAnalysis.md)  
&emsp; [MyBatis架构](/docs/SSM/MyBatis/MybatisFramework.md)  
&emsp; [MyBatis SQL执行解析](/docs/SSM/MyBatis/MybatisExecutor.md)  
&emsp; [Spring整合MyBatis原理](/docs/SSM/MyBatis/SpringMybatisPrinciple.md)  
&emsp; [MyBatis缓存](/docs/SSM/MyBatis/MybatisCache.md)  
&emsp; [MyBatis插件解析](/docs/SSM/MyBatis/MybatisPlugins.md)  
&emsp; [MyBatis日志体系](/docs/SSM/MyBatis/MybatisLog.md)   

<!-- 
&emsp; [MyBatis中的设计模式](/docs/SSM/MyBatis/MybatisDesign.md)  
-->

----

## 分布式  
### SpringBoot  
[学习SpringBoot源码的感悟](/docs/microService/SpringBoot/thinking.md)  
[SpringBoot](/docs/microService/SpringBoot/SpringBoot.md)  
[SpringBoot源码](/docs/microService/SpringBoot/SpringBootSource.md)  
&emsp; [SpringBoot启动过程](/docs/microService/SpringBoot/SpringBootRun.md)  
&emsp; &emsp; [SpringApplication初始化](/docs/microService/SpringBoot/SpringApplicationInit.md)  
&emsp; &emsp; [run()方法运行过程](/docs/microService/SpringBoot/runProcess.md)  
&emsp; &emsp; [SpringBoot事件监听](/docs/microService/SpringBoot/EventListeners.md)  
&emsp; &emsp; [SpringBoot内置生命周期事件详解](/docs/microService/SpringBoot/SpringBootEvent.md)  
&emsp; &emsp; [SpringBoot事件回调机制](/docs/microService/SpringBoot/eventCallback.md)  
&emsp; [SpringBoot自动配置](/docs/microService/SpringBoot/AutomaticAssembly.md)  
&emsp; &emsp; [注解@SpringBootApplication](/docs/microService/SpringBoot/SpringBootApplication.md)  
&emsp; &emsp; [加载自动配置流程](/docs/microService/SpringBoot/ApplicationProcess.md)  
&emsp; &emsp; [内置Tomcat](/docs/microService/SpringBoot/Tomcat.md)  
[自定义strater](/docs/microService/SpringBoot/SpringBootStarter.md)  

### SpringCloud    
[Spring Cloud Netflix](/docs/microService/SpringCloudNetflix/Netflix.md)  
[Spring Cloud Eureka](/docs/microService/SpringCloudNetflix/Eureka.md)  
[Spring Cloud Ribbon](/docs/microService/SpringCloudNetflix/Ribbon.md)  
[Spring Cloud Hytrix](/docs/microService/SpringCloudNetflix/Hytrix.md)  
[Spring Cloud Feign](/docs/microService/SpringCloudNetflix/Feign.md)  
[Spring Cloud Zuul](/docs/microService/SpringCloudNetflix/Zuul.md)  
[Spring Cloud Sleuth](/docs/microService/SpringCloudNetflix/Sleuth.md)  
[Spring Cloud Admin](/docs/microService/SpringCloudNetflix/SpringBootAdmin.md)  

### Dubbo  
[RPC](/docs/microService/RPC.md)  
[Dubbo](/docs/microService/dubbo/Dubbo.md)   
[Dubbo使用教程](/docs/microService/dubbo/DubboUse.md)  
&emsp; [Dubbo实战](/docs/microService/dubbo/DubboActualCombat.md)  
[Dubbo详解](/docs/microService/dubbo/DubboExplanate.md)  
&emsp; [Dubbo框架设计](/docs/microService/dubbo/design.md)  
&emsp; [Dubbo实现细节](/docs/microService/dubbo/realization.md)  
&emsp; [扩展点加载(SPI)](/docs/microService/dubbo/SPI.md)  
&emsp; &emsp; [获得指定拓展对象](/docs/microService/dubbo/getExtension.md)  
&emsp; &emsp; [获得自适应的拓展对象](/docs/microService/dubbo/getAdaptiveExtension.md)  
&emsp; [Dubbo协议长连接心跳](/docs/microService/dubbo/LongConnection.md)  

[Dubbo运行流程源码解析](/docs/microService/dubbo/DubboSource.md)  
&emsp; [初始化源码解析](/docs/microService/dubbo/dubboSpring.md)  
&emsp; [服务暴露源码解析](/docs/microService/dubbo/export.md)  
&emsp; [服务引用源码解析](/docs/microService/dubbo/introduce.md)  
&emsp; [服务调用源码解析](/docs/microService/dubbo/call.md)  
&emsp; [再次理解dubbo-rpc包](/docs/microService/dubbo/dubboRPC.md)  
[Dubbo常见问题](/docs/microService/dubbo/problem.md)  

<!-- 
[Dubbo集群容错源码解析](/docs/microService/dubbo/DubboColonySource.md)  
&emsp; [服务目录源码解析](/docs/microService/dubbo/Directory.md)  
&emsp; [服务路由源码解析](/docs/microService/dubbo/Router.md)  
&emsp; [集群源码解析](/docs/microService/dubbo/Cluster.md)  
&emsp; [负载均衡源码解析](/docs/microService/dubbo/LoadBalance.md)  
-->

### Zookeeper
[如何理解分布式协调技术](/docs/microService/dubbo/coordinate.md)  
[Zookeeper原理](/docs/microService/dubbo/Zookeeper.md)  
&emsp; [Watcher](/docs/microService/dubbo/Watcher.md)  
&emsp; [ZAB](/docs/microService/dubbo/ZAB.md)  
[Zookeeper使用](/docs/microService/dubbo/ZookeeperUse.md)  
<!-- 
[Zookeeper问题](/docs/microService/dubbo/ZookeeperProblem.md)  
-->

### 分布式缓存  
[分布式缓存问题](/docs/cache/DistributedCache.md)  
[双缓存](/docs/cache/DoubleCache.md)  
[缓存算法](/docs/cache/CacheAlgorithm.md)  
<!-- 
[缓存](/docs/cache/Cache.md)  
本地缓存
https://mp.weixin.qq.com/s/JdawZKAeVzDnZ8ZQF0XVuA
-->

#### Redis
[学习Redis的总结](/docs/microService/Redis/RedisSummary.md)  
[Redis数据类型](/docs/microService/Redis/RedisStructure.md)  
&emsp; [Redis基本数据类型](/docs/microService/Redis/RedisBasicStructure.md)  
&emsp; [Redis扩展数据类型](/docs/microService/Redis/ExtendedDataType.md)  
&emsp; [Redis底层实现](/docs/microService/Redis/RedisBottom.md)  
&emsp; &emsp; [SDS](/docs/microService/Redis/SDS.md)  
&emsp; &emsp; [Dictht](/docs/microService/Redis/Dictht.md)  
[Redis客户端使用及开发规范](/docs/microService/Redis/RedisStandard.md)  
&emsp; [BigKey](/docs/microService/Redis/BigKey.md)  
[Redis配置文件介绍](/docs/microService/Redis/RedisConf.md)  
[Redis内置功能](/docs/microService/Redis/BuiltIn.md)    
&emsp; [Redis事务](/docs/microService/Redis/RedisTransaction.md)  
&emsp; [RedisPipeline/批处理](/docs/microService/Redis/RedisPipeline.md)  
&emsp; [Redis和Lua](/docs/microService/Redis/lua.md)  
&emsp; [Redis持久化](/docs/microService/Redis/RedisPersistence.md)  
&emsp; &emsp; [AOF重写阻塞](/docs/microService/Redis/Rewrite.md)  
&emsp; [Redis内存](/docs/microService/Redis/RedisEliminate.md)  
&emsp; [Redis实现消息队列](/docs/microService/Redis/Message.md)  
&emsp; &emsp; [发布订阅](/docs/microService/Redis/pub.md)  
&emsp; [Redis实现延迟队列](/docs/microService/Redis/delay.md)  
&emsp; [Redis多线程](/docs/microService/Redis/RedisMultiThread.md)  
[Redis高可用](/docs/microService/Redis/RedisDeploy.md)  
&emsp; [Redis主从复制](/docs/microService/Redis/RedisMasterSlave.md)  
&emsp; [Redis哨兵模式](/docs/microService/Redis/RedisSentry.md)  
&emsp; [Redis集群模式](/docs/microService/Redis/RedisCluster.md)  
[Redis运维](/docs/microService/Redis/RedisOperation.md)  
[Redis常见问题与优化](/docs/microService/Redis/problem.md)  
<!-- [Redis原理](/docs/microService/Redis/RedisPrinciple.md)  -->

### 限流降级
[分布式限流](/docs/microService/thinking/CurrentLimiting.md)   
&emsp; [限流Sentinel](/docs/microService/thinking/Sentinel.md)  
[服务降级](/docs/microService/thinking/Demotion.md)  

### 分布式消息队列  
[mq](/docs/microService/mq/mq.md)  

#### kafka  
[kafka总结](/docs/microService/mq/kafka/kafka.md)   
[kafka安装](/docs/microService/mq/kafka/kafkaInstall.md)  
[kafka基本概念](/docs/microService/mq/kafka/kafkaConcepts.md)  
&emsp; [kafka生产者](/docs/microService/mq/kafka/kafkaProducerUse.md)  
&emsp; [消息分区](/docs/microService/mq/kafka/topic.md)  
&emsp; [kafka消费者](/docs/microService/mq/kafka/kafkaConsumerUse.md)  
&emsp; [kafka服务端](/docs/microService/mq/kafka/kafkaServer.md)  
[kafka特性](/docs/microService/mq/kafka/kafkaCharacteristic.md)  
&emsp; [kafka持久化](/docs/microService/mq/kafka/kafkaPersistence.md)  
&emsp; [kafka副本机制](/docs/microService/mq/kafka/kafkaReplica.md)  
&emsp; [kafka如何保证消息队列不丢失?](/docs/microService/mq/kafka/kafkaReliability.md)  
&emsp; [kafka幂等性](/docs/microService/mq/kafka/kafkaIdempotent.md)  
&emsp; [kafka事务](/docs/microService/mq/kafka/kafkaTraction.md)  
[kafka集群管理](/docs/microService/mq/kafka/kafkaUse.md)  
[kafka-SpringBoot](/docs/microService/mq/kafka/kafkaSpringBoot.md)  
[kafka高级应用](/docs/microService/mq/kafka/advanced.md)  
[kafka源码](/docs/microService/mq/kafka/kafkaSource.md)  
&emsp; [kafka生产者](/docs/microService/mq/kafka/kafkaProducer.md)  
&emsp; [kafka消费者](/docs/microService/mq/kafka/kafkaConsumer.md)  
<!-- 
[kafkaStreams](/docs/microService/mq/kafka/kafkaStreams.md)  
-->

### 分布式高并发
[分布式高并发](/docs/system/DistributedHighConcurrency.md)   
&emsp; [分布式与微服务](/docs/system/serviceSplit.md)  
&emsp; [性能指标](/docs/system/performance.md)  
&emsp; [并发系统三高](/docs/system/threeHigh.md)  
&emsp; [高可用](/docs/system/highAvailability.md)  
&emsp; [秒杀系统设计](/docs/system/seckill.md)  
&emsp; [资源限制](/docs/system/ResourceConstraints.md)  

### 分布式理论  
[分布式和集群](/docs/system/distributed.md)   
[分布式算法](/docs/microService/thinking/DistributedAlgorithm.md)  
&emsp; [一致性哈希](/docs/microService/thinking/consistent.md)  
[分布式理论](/docs/microService/thinking/DistributedTheory.md)  

### 分布式ID
[分布式ID常见解决方案](/docs/microService/thinking/DistributedID.md)  

### 分布式事务
[分布式事务](/docs/microService/thinking/DistriTransaction.md)  
&emsp; [DTP及XA](/docs/microService/thinking/DTPAndXA.md)  
&emsp; [TCC](/docs/microService/thinking/TCC.md)   
&emsp; [TCC问题](/docs/microService/thinking/TCCProblem.md)   
&emsp; [Saga](/docs/microService/thinking/Event.md)  
&emsp; [消息模式](/docs/microService/thinking/news.md)   
&emsp; [分布式事务的选型](/docs/microService/thinking/DistributedTransactionSelection.md)  

### 分布式锁
[分布式锁](/docs/microService/thinking/Lock.md)  
&emsp; [RedisLock](/docs/microService/thinking/redisLock.md)  
&emsp; [Redisson](/docs/microService/thinking/Redisson.md)  
&emsp; [ZK分布式锁](/docs/microService/thinking/ZKLock.md)  
&emsp; [MySql分布式锁](/docs/microService/thinking/MySqlLock.md)  

### 任务调度
[延时队列](/docs/frame/delayQueue.md)  
[时间轮算法](/docs/microService/dubbo/timeWheel.md)  
[分布式调度](/docs/frame/taskSheduling.md)  
&emsp; [XXL-JOB](/docs/frame/XXL-JOB.md)  

### 分布式搜索引擎  
<!-- &emsp; &emsp; [基本查询](/docs/ES/basicSearch.md)   -->
[ES搭建](/docs/ES/build.md)  
&emsp; [ES配置文件](/docs/ES/configure.md)  
&emsp; [内存设置](/docs/ES/heap.md)  
[ES基本概念](/docs/ES/basicConcepts.md)  
[ES集群基本概念](/docs/ES/ClusterConcept.md)  
[ES使用](/docs/ES/useTutorials.md)  
&emsp; [ES建模](/docs/ES/modeling.md)  
&emsp; [PB级别的大索引如何设计？](/docs/ES/IndexDesign.md)  
&emsp; [ElasticsearchREST](/docs/ES/ElasticsearchREST.md)  
&emsp; [索引基本操作](/docs/ES/index.md)  
&emsp; [索引管理](/docs/ES/indexMaintain.md)  
&emsp; [映射详解](/docs/ES/mapping.md)  
&emsp; [文档操作](/docs/ES/document.md)  
&emsp; [检索操作](/docs/ES/search.md)  
&emsp; &emsp; [结构化检索](/docs/ES/Structured.md)  
&emsp; &emsp; [全文检索](/docs/ES/fullText.md)  
&emsp; &emsp; [排序/相关度/评分机制](/docs/ES/score.md)  
&emsp; &emsp; [多字段搜索](/docs/ES/MultiField.md)  
&emsp; &emsp; [聚合查询](/docs/ES/togetherSearch.md)  
&emsp; &emsp; [分页查询](/docs/ES/limitSearch.md)  
&emsp; &emsp; [多表关联](/docs/ES/multiTable.md)  
&emsp; &emsp; [高亮显示](/docs/ES/highLight.md)  
&emsp; &emsp; [检索模版](/docs/ES/searchTemplate.md)  
&emsp; [Java客户端](/docs/ES/JavaRestClient.md)  
[ES原理](/docs/ES/principle.md)  
&emsp; [ES集群运行原理](/docs/ES/ClusterPrinciple.md)  
&emsp; [ES增删改原理](/docs/ES/write.md)  
[ES运维](/docs/ES/Operation.md)  
&emsp; [ES集群操作](/docs/ES/ClusterOperation.md)  
&emsp; [ES监控](/docs/ES/monitor.md)  
&emsp; [ES优化](/docs/ES/optimization.md)  
&emsp; [使用ES中的一些问题](/docs/ES/problem.md)  
[ELK/EFK](/docs/ES/EFK.md)    
&emsp; [EFK介绍](/docs/ES/EFKIntroduce.md)  
&emsp; [EFK使用](/docs/ES/EFKUse.md)  
&emsp; [Kibana用户手册](/docs/ES/Kibana.md)  
&emsp; [Kibana查询](/docs/ES/KibanaQuery.md)  
<!-- 
logstash同步mysql数据到Elasticsearch实战,主要实现删除
https://blog.csdn.net/Giggle1994/article/details/111194763
-->

### 分布式通信  
[分布式通信基础](/docs/microService/communication/Netty/basics.md)   
&emsp; [序列化](/docs/microService/communication/serializbale.md)  
&emsp; [Socket](/docs/microService/communication/Socket.md)  
&emsp; [五种I/O模型](/docs/microService/communication/IO.md)  
&emsp; &emsp; [I/O多路复用详解](/docs/microService/communication/Netty/epoll.md)  
&emsp; [IO性能优化之零拷贝](/docs/microService/communication/Netty/zeroCopy.md)  
[NIO](/docs/microService/communication/NIO.md)  
&emsp; [NIO Channel](/docs/microService/communication/NIO/Channel.md)  
&emsp; [NIO Buffer](/docs/microService/communication/NIO/Buffer.md)  
&emsp; [Java中的零拷贝](/docs/microService/communication/NIO/JavaZeroCopy.md)  
&emsp; [NIO Selector](/docs/microService/communication/NIO/Selector.md)  
[Netty](/docs/microService/communication/Netty/netty.md)   
&emsp; [Netty介绍及架构剖析](/docs/microService/communication/Netty/concepts.md)  
&emsp; [Netty运行流程](/docs/microService/communication/Netty/operation.md)   
&emsp; [Netty核心组件](/docs/microService/communication/Netty/components.md)   
&emsp; [Netty高性能](/docs/microService/communication/Netty/performance.md)  
&emsp; &emsp; [~~Netty中的零拷贝~~](/docs/microService/communication/Netty/nettyZeroCopy.md)  
&emsp; &emsp; [Reactor与EventLoop](/docs/microService/communication/Netty/Reactor.md)  
&emsp; [TCP粘拆包与Netty编解码](/docs/microService/communication/Netty/Decoder.md)  
&emsp; [Netty实战](/docs/microService/communication/Netty/actualCombat.md)  
&emsp; [Netty源码](/docs/microService/communication/Netty/source.md)    
&emsp; &emsp; [Netty服务端创建](/docs/microService/communication/Netty/principle.md)  
&emsp; &emsp; [Netty客户端创建](/docs/microService/communication/Netty/customer.md)  
&emsp; &emsp; [NioEventLoop](/docs/microService/communication/Netty/NioEventLoop.md)  
&emsp; &emsp; &emsp; [NioEventLoop的启动](/docs/microService/communication/Netty/NioEventLoopStart.md)  
&emsp; &emsp; &emsp; [NioEventLoop的执行](/docs/microService/communication/Netty/NioEventLoopRun.md)  
&emsp; &emsp; [内存分配-ByteBuf](/docs/microService/communication/Netty/byteBuf.md)    
&emsp; &emsp; &emsp; [内存分配-分配器ByteBufAllocator](/docs/microService/communication/Netty/ByteBufAllocator.md)    
&emsp; &emsp; &emsp; [内存分配-非池化内存分配](/docs/microService/communication/Netty/Unpooled.md)    
&emsp; &emsp; &emsp; [~~内存分配-池化内存分配~~](/docs/microService/communication/Netty/Pooled.md)    

---

## ~~软件工程~~

## 项目构建基础  
[构建基础](/docs/web/BuildFoundation.md)  
&emsp; [日志系统](/docs/web/log.md)  
&emsp; [SpringTest](/docs/web/test.md)  
&emsp; [乱码](/docs/web/garbled.md)  
&emsp; [统一格式返回](/docs/web/UnifiedFormat.md)  
&emsp; [统一异常处理](/docs/web/ExceptionHandler.md)  
&emsp; [统一日志记录](/docs/web/unifiedLog.md)  
&emsp; [工具类hutool](/docs/web/hutool.md)  
[API接口设计](/docs/web/API.md)    
&emsp; [RESTful风格](/docs/web/interface/RESTful.md)  
&emsp; [接口幂等](/docs/web/interface/idempotent.md)  
&emsp; [接口防刷/反爬虫](/docs/web/interface/brush.md)  
&emsp; [接口安全](/docs/web/interface/security.md)  
&emsp; [★★★接口响应时间问题](/docs/web/interface/timeout.md)  
&emsp; [接口预警](/docs/web/interface/EarlyWarn.md)  
[JavaBean](/docs/web/JavaBean.md)  
&emsp; [POJO](/docs/web/POJO.md)  
&emsp; [BeanUtils](/docs/web/BeanUtils.md)  
&emsp; [参数校验](/docs/web/Validation.md)  
&emsp; [Lombok](/docs/web/Lombok.md)  
[Http](/docs/web/http.md)   
&emsp; [RestTemplate](/docs/web/Resttemplate.md)  
&emsp; [Http重试](/docs/web/httpRetry.md)   
[数据相关](/docs/web/Data.md)  
&emsp; [格式化](/docs/web/Format.md)  
&emsp; [数据脱敏](/docs/web/sensitive.md)  
&emsp; [加密算法](/docs/web/encryption.md)  
[源码安全](/docs/web/codeSecurity.md)   
[其他](/docs/web/other.md)  

## 架构设计  
[架构图](/docs/system/diagram.md)  
[软件架构设计模式](/docs/system/designPattern.md)  
[OpenAPI](/docs/system/OpenAPI.md)  
&emsp; [容灾和备份](/docs/system/backups.md)  
<!-- 
[Gateway](/docs/microService/microservices/Gateway.md)  

软件工程

## DDD
[DDD](/docs/system/DDD.md)  
-->

----

## 负载均衡  
[负载均衡](/docs/system/loadBalance/loadBalance.md)  
[DNS轮询](/docs/system/loadBalance/DNS.md)  
[CDN](/docs/system/loadBalance/CDN.md)   
[http重定向](/docs/system/loadBalance/redirect.md)  
[LVS](/docs/system/loadBalance/LVS.md)  
[Keepalived](/docs/system/loadBalance/Keepalived.md)  
[Nginx](/docs/system/loadBalance/Nginx/nginx.md)  
[Nginx使用](/docs/system/loadBalance/Nginx/nginxUser.md)   
[Nginx运维](/docs/system/loadBalance/Nginx/nginxOperation.md)  

----

## Linux  
[Linux命令](/docs/Linux/LinuxCommand.md)  
&emsp; [文本处理](/docs/Linux/textProcessing.md)  
&emsp; [网络通讯](/docs/Linux/NetworkCommunication.md)  
&emsp; [进程管理](/docs/Linux/ProcessManagement.md)  
[shell编程](/docs/Linux/shell.md)  
[堡垒机](/docs/Linux/baolei.md)  
[Linux系统](/docs/Linux/Linux.md)  

------

## DevOps  
[DevOps与CI/CD](/docs/devAndOps/devOps.md)  
[DevOps搭建](/docs/devAndOps/devOpsPractice.md)  
[GIT](/docs/devAndOps/git/command.md)  
[Maven](/docs/devAndOps/maven.md)  
[Jenkins](/docs/devAndOps/Jenkins.md)  
[从上往下学Docker](/docs/devAndOps/docker/summary.md)  
&emsp; [容器化Docker](/docs/devAndOps/docker/introduce.md)  
&emsp; [Docker架构](/docs/devAndOps/docker/principle.md)  
&emsp; [Docker使用教程](/docs/devAndOps/docker/use.md)  
&emsp; &emsp; [Docker安装及常用命令](/docs/devAndOps/docker/command.md)  
&emsp; &emsp; [DockerFile](/docs/devAndOps/docker/file.md)  
&emsp; &emsp; [对象标签使用](/docs/devAndOps/docker/objectLabel.md)  
&emsp; [镜像详解](/docs/devAndOps/docker/image.md)  
&emsp; [容器详解](/docs/devAndOps/docker/container.md)  
&emsp; [Docker工具](/docs/devAndOps/docker/tools.md)  
[Kubernetes](/docs/devAndOps/k8s/Kubernetes.md)      
&emsp; [k8s使用教程](/docs/devAndOps/k8s/use.md)  
&emsp; &emsp; [k8s安装及常用命令](/docs/devAndOps/k8s/command.md)  
&emsp; &emsp; [Yaml文件配置](/docs/devAndOps/k8s/yaml.md)  
&emsp; [k8s架构](/docs/devAndOps/k8s/principle.md)  
&emsp; &emsp; [Pod详解](/docs/devAndOps/k8s/pod.md)  
&emsp; &emsp; [Service详解](/docs/devAndOps/k8s/service.md)  
&emsp; [k8s运维](/docs/devAndOps/k8s/tools.md)  
&emsp; [Kuboard介绍](/docs/devAndOps/k8s/kuboard.md)  
[多种发布方式](/docs/system/publishe.md)  
&emsp; [灰度发布](/docs/system/grayscalePublishe.md)  
[监控](/docs/devAndOps/monitor.md)  

----

## 计算机网络  
[网络的性能指标](/docs/network/standard.md)  
[OSI七层网络模型](/docs/network/OSI.md)  
[HTTP](/docs/network/HTTP.md)  
[HTTPS](/docs/network/HTTPS.md)  
[TCP](/docs/network/TCP.md)  
&emsp; [TIME_WAIT问题](/docs/network/timewait.md)  
&emsp; [TCP粘包](/docs/network/TCPSticking.md)   
[DNS](docs/network/DNS.md)  
[长/短连接](/docs/network/connection.md)   
[网络抓包](/docs/network/wireshark.md)  


----

## 测试工具  
[Jmeter](/docs/devAndOps/Jmeter.md)  
[JMH](/docs/java/JVM/JMH.md)  

## 常用工具  
[idea](/docs/software/idea/idea.md)  
[抓包Charles](/docs/software/charles.md)  
[markdown](/docs/software/markdown.md)  

---
## Error  
[多线程串线了](/docs/Error/ThreadCrossed.md)  
[熔断降级处理](/docs/Error/hystrix.md)  
[Redis高并发](/docs/Error/redisConcurrent.md)  
[Redis内存增长异常排查](/docs/Error/RedisMemoryGrowth.md)  
[redis scan命令](/docs/Error/redisScan.md)  
<!-- [雪花利用ZK生成workId]()   -->

## 项目总结  
<!-- 
[支付项目](/docs/project/payment.md)  
[二清项目](/docs/project/erqing.md)  
-->

## 算法  
[红黑树](/docs/function/redBlack.md)  
[复杂数据结构](/docs/function/otherStructure.md)  
&emsp; [跳跃表](/docs/function/SkipList.md)  
&emsp; [位图](/docs/function/BitMap.md)  
&emsp; [BloomFilter](/docs/function/BloomFilter.md)  
&emsp; [HyperLogLog](/docs/function/HyperLogLog.md)  
&emsp; [Trie](/docs/function/Trie.md)  
[大数据和空间限制](/docs/function/bigdata.md)  

-------------------

<!-- 
[技术管理](/docs/Administration/TechnologyAdmin.md)  
-->

## 面试技巧
[总体流程](/docs/recruit/1resumeCreate.md)  
[死命题](/docs/recruit/2askedProblem.md)  
[问面试官](/docs/recruit/3askProblem.md)  
[难点](/docs/recruit/4difficultProblem.md)  