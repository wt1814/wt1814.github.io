<!-- TOC -->

- [wt1814-note](#wt1814-note)
    - [算法](#算法)
    - [Java](#java)
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
        - [分布式理论](#分布式理论)
        - [分布式缓存](#分布式缓存)
            - [Redis](#redis)
        - [分布式消息队列](#分布式消息队列)
            - [kafka](#kafka)
        - [分布式ID](#分布式id)
        - [分布式事务](#分布式事务)
        - [分布式锁](#分布式锁)
        - [限流降级](#限流降级)
        - [分布式调度](#分布式调度)
        - [分布式搜索引擎](#分布式搜索引擎)
        - [分布式通信](#分布式通信)
    - [其他框架](#其他框架)
    - [项目构建基础](#项目构建基础)
    - [架构设计](#架构设计)
    - [负载均衡](#负载均衡)
        - [Nginx](#nginx)
    - [Linux](#linux)
    - [DevOps](#devops)
    - [测试](#测试)
    - [计算机网络](#计算机网络)
    - [开发常用工具](#开发常用工具)
    - [其他工具](#其他工具)
    - [Error](#error)

<!-- /TOC -->

# wt1814-note  
## 算法  
[总结](/docs/java/function/summary.md)  
[算法基本概念](/docs/java/function/notion.md)  
[基本数据结构](/docs/java/function/structure/structure.md)  
&emsp; [数组和链表](/docs/java/function/structure/array.md)  
&emsp; [栈与队列](/docs/java/function/structure/stack.md)  
&emsp; [二叉树](/docs/java/function/structure/tree.md)  
&emsp; [二叉排序树](/docs/java/function/structure/binarySort.md)  
&emsp; [平衡二叉树](/docs/java/function/structure/AVL.md)  
&emsp; [红黑树](/docs/java/function/structure/redBlack.md)  
&emsp; [B树](/docs/java/function/structure/BTree.md)  
&emsp; [二叉堆](/docs/java/function/structure/binaryReactor.md)  
&emsp; [哈希](/docs/java/function/structure/hash.md)  
[排序算法](/docs/java/function/sort.md)  
&emsp; [比较排序](/docs/java/function/sort/compare.md)  
&emsp; [非比较排序](/docs/java/function/sort/noCompare.md)  
&emsp; [外部排序](/docs/java/function/sort/externalSort.md)  
[查找算法](/docs/java/function/search.md)  
[算法方法](/docs/java/function/algorithmicIdea.md)  
[其他数据结构](/docs/java/function/otherStructure.md)  
&emsp; [跳跃表](/docs/java/function/SkipList.md)  
&emsp; [位图](/docs/java/function/BitMap.md)  
&emsp; [BloomFilter](/docs/java/function/BloomFilter.md)  
&emsp; [HyperLogLog](/docs/java/function/HyperLogLog.md)  
&emsp; [Trie](/docs/java/function/Trie.md)  
[海量数据应用](/docs/java/function/bigdata.md)  

## Java  
[Java基础](/docs/java/basis/Java基础.md)  
&emsp; [符号](/docs/java/basis/mark.md)  
[Java基础数据类型](/docs/java/basis/数据类型.md)  
[Java集合框架](/docs/java/Collection/1.集合框架.md)  
&emsp; [Map](/docs/java/Collection/2.Map.md)  
&emsp; &emsp; [HashMap](/docs/java/Collection/HashMap.md)  
&emsp; &emsp; [LikedHashMap](/docs/java/Collection/LikedHashMap.md)  
&emsp; [Collection](/docs/java/Collection/3.Collection.md)  
[JDK1.8](/docs/java/JDK8/JDK8.md)  
&emsp; [Lambda](/docs/java/JDK8/Lambda.md)  
&emsp; [Stream](/docs/java/JDK8/Stream.md)  
&emsp; [Optional](/docs/java/JDK8/Optional.md)  
&emsp; [DateTime](/docs/java/JDK8/DateTime.md)  
[Java异常](/docs/java/basis/JavaException.md)  
[Java范型](/docs/java/basis/JavaParadigm.md)  
[Java反射](/docs/java/basis/Java反射.md)  
[自定义注解](/docs/java/basis/annotation.md)  
[IO](/docs/java/basis/JavaIO.md)  
&emsp; [断点续传](/docs/java/basis/breakpoint.md)  
[SPI](/docs/java/basis/SPI.md)  

### 设计模式  
[七大设计原则](/docs/java/Design/1.principles.md)  
[Java设计模式](/docs/java/Design/2.design.md)  
&emsp; [创建型设计模式](/docs/java/Design/3.establish.md)  
&emsp; [结构型设计模式](/docs/java/Design/4.structure.md)  
&emsp; &emsp; [动态代理](/docs/java/Design/6.proxy.md)   
&emsp; [行为型设计模式](/docs/java/Design/5.behavior.md)  
[设计模式讨论](/docs/java/Design/discuss.md)  

### JVM  
[JVM总结](/docs/java/JVM/summary.md)  
[JDK、JRE、JVM](/docs/java/JVM/JDK、JRE、JVM.md)   
[字节码文件](/docs/java/JVM/Class.md)  
[JVM类的加载](/docs/java/JVM/JVM类的加载.md)  
[JVM类加载器](/docs/java/JVM/classLoader.md)  
[JVM内存结构](/docs/java/JVM/JVM内存结构.md)  
[内存对象](/docs/java/JVM/MemoryObject.md)  
[内存泄露](/docs/java/JVM/MemoryLeak.md)  
[GC-回收对象](/docs/java/JVM/GCProject.md)  
[GC-回收器](/docs/java/JVM/GCReclaimer.md)  
[JVM调优-基础](/docs/java/JVM/TuningBasic.md)  
[JVM调优](/docs/java/JVM/tuning.md)  
[JVM问题排查](/docs/java/JVM/TroubleShooting.md)  
[JVM排查案例](/docs/java/JVM/case.md)  
[Arthas工具](/docs/java/JVM/Arthas.md)  
[性能指标](/docs/system/performance.md)  
[JAVA线上故障排查](/docs/Linux/problem.md)  

### 并发编程  
[并发编程总结](/docs/java/concurrent/summary.md)  
[线程基本概念](/docs/java/concurrent/1.Thread.md)  
[并发问题](/docs/java/concurrent/2.Multithread.md)  
&emsp; [JMM](/docs/java/concurrent/JMM.md)  
&emsp; [并发问题](/docs/java/concurrent/ConcurrencyProblem.md)  
&emsp; [线程安全解决](/docs/java/concurrent/ThreadSafety.md)  
&emsp; &emsp; [Synchronized应用](/docs/java/concurrent/SynApply.md)  
&emsp; &emsp; [Synchronized原理](/docs/java/concurrent/SynPrinciple.md)  
&emsp; &emsp; [Volatile](/docs/java/concurrent/Volatile.md)  
&emsp; &emsp; [ThreadLocal](/docs/java/concurrent/ThreadLocal.md)  
&emsp; [线程通信](/docs/java/concurrent/线程通信.md)  
&emsp; [线程活跃性](/docs/java/concurrent/Activity.md)  
[线程池](/docs/java/concurrent/3.ThreadPool.md)  
&emsp; [ThreadPoolExecutor](/docs/java/concurrent/ThreadPoolExecutor.md)  
&emsp; [ForkJoinPool](/docs/java/concurrent/ForkJoinPool.md)  
[J.U.C包](/docs/java/concurrent/4.ConcurrentPackage.md)  
&emsp; [CAS](/docs/java/concurrent/CAS.md)  
&emsp; [AQS](/docs/java/concurrent/AQS.md)  
&emsp; [Lock](/docs/java/concurrent/8.Lock.md)  
&emsp; &emsp; [ReentrantLock](/docs/java/concurrent/ReentrantLock.md)  
&emsp; &emsp; [ReadWriteLock](/docs/java/concurrent/ReadWriteLock.md)  
&emsp; &emsp; [LocLockSupport](/docs/java/concurrent/LockSupport.md)         
&emsp; [Atmoic](/docs/java/concurrent/9.Atmoic.md)  
&emsp; [Collections](/docs/java/concurrent/10.Collections.md)  
&emsp; &emsp; [ConcurrentHashMap](/docs/java/concurrent/5.ConcurrentHashMap.md)  
&emsp; &emsp; [BlockingQueue](/docs/java/concurrent/BlockingQueue.md)  
&emsp; [tools](/docs/java/concurrent/7.tools.md)  
[CompletableFuture](/docs/java/concurrent/CompletableFuture.md)  
[并发框架Disruptor](/docs/java/concurrent/disruptor.md)  
[多线程处理大数据量](/docs/java/concurrent/bigData.md)  

## 数据库  
[学习SQL的总结](/docs/SQL/学习SQL的总结.md)  
[数据建模](/docs/SQL/modeling.md)  
[数据类型](/docs/SQL/dataType.md)  
[SQL语句](/docs/SQL/1.SQL语句.md)  
&emsp; [基本查询语句](/docs/SQL/9.basicSelect.md)  
&emsp; [连接查询](/docs/SQL/10.joinSelect.md)  
&emsp; [行列转换](/docs/SQL/11.trans.md)  

[MySql函数](/docs/SQL/MySQLFunction.md)  
[数据库对象](/docs/SQL/DatabaseObject.md)  
[SQL优化](/docs/SQL/2.SQL优化.md)  
&emsp; [SQL分析](/docs/SQL/Analysis.md)  
&emsp; [SQL语句优化](/docs/SQL/SQLStatement.md)  
&emsp; [索引优化](/docs/SQL/7.index.md)  
&emsp; [优化案例](/docs/SQL/12.case.md)  
[数据库分布式](/docs/SQL/DistributedDatabase.md)  
&emsp; [主从复制](/docs/SQL/16.replication.md)  
&emsp; &emsp; [主从复制的问题](/docs/SQL/replicationProblem.md)  
&emsp; [分区](/docs/SQL/17.partition.md)  
&emsp; [分库分表](/docs/SQL/18.sub.md)  
&emsp; [数据库分布式实现](/docs/SQL/subRealize.md)  
&emsp; &emsp; [MyCat中间件](/docs/SQL/5.MyCat中间件.md)  
[MySQL监控](/docs/SQL/MySqlMonitor.md)  

[MySql原理](/docs/SQL/MySqlPrinciple.md)   
&emsp; [MySql架构](/docs/SQL/Framework.md)   
&emsp; [MySql存储引擎](/docs/SQL/13.MySqlStorage.md)  
&emsp; &emsp; [BufferPool](/docs/SQL/BufferPool.md)  
&emsp; &emsp; [ChangeBuffer](/docs/SQL/ChangeBuffer.md)  
&emsp; &emsp; [AdaptiveHashIndex](/docs/SQL/AdaptiveHashIndex.md)  
&emsp; &emsp; [表空间](/docs/SQL/TableSpace.md)  
&emsp; &emsp; [Double Write](/docs/SQL/DoubleWrite.md)  
&emsp; &emsp; [MySql事务日志](/docs/SQL/log.md)  
&emsp; &emsp; [BinLog使用](/docs/SQL/BinLog.md)  
&emsp; [索引底层原理](/docs/SQL/IndexPrinciple.md)  
&emsp; [各种索引](/docs/SQL/IndexKnowledge.md)  
&emsp; [MySql事务](/docs/SQL/14.transaction.md)  
&emsp; [MySql锁](/docs/SQL/15.lock.md)  
&emsp; [MySql锁造成的问题](/docs/SQL/LockProblem.md)  
&emsp; [MySql-MVCC](/docs/SQL/MVCC.md)  

[数据库连接池](/docs/SQL/connectionPool.md)  
&emsp; [HikariCPConnectionPool](/docs/SQL/HikariCP.md)   
&emsp; &emsp; [HikariCP原理](/docs/SQL/HikariCPPrinciple.md)  
&emsp; &emsp; [HikariCP监控与故障排查](/docs/SQL/HikariCPMonitor.md)  
[数据迁移](/docs/projectImplement/implementation.md)  
[大数据量操作](/docs/SQL/largeData.md)  

## SSM  
### Spring  
[Spring](/docs/SSM/Spring/Spring.md)  
[学习Spring源码的感悟](/docs/SSM/Spring/thinking.md)  
[SpringIOC解析](/docs/SSM/Spring/SpringIOC.md)  
&emsp; [容器初始化详解](/docs/SSM/Spring/ContainerInit.md)  
[SpringDI解析](/docs/SSM/Spring/SpringDI.md)  
[SpringBean生命周期](/docs/SSM/Spring/SpringBean.md)  
[容器相关特性](/docs/SSM/Spring/feature/ContainerFeature.md)  
&emsp; [FactoryBean](/docs/SSM/Spring/feature/FactoryBean.md)  
&emsp; [可二次开发常用接口](/docs/SSM/Spring/feature/SecendDeve.md)  
&emsp; &emsp; [事件](/docs/SSM/Spring/feature/Event.md)  
&emsp; &emsp; [Aware接口](/docs/SSM/Spring/feature/Aware.md)  
&emsp; &emsp; [后置处理器](/docs/SSM/Spring/feature/BeanFactoryPostProcessor.md)  
&emsp; &emsp; [InitializingBean](/docs/SSM/Spring/feature/InitializingBean.md)  
&emsp; [循环依赖](/docs/SSM/Spring/feature/CircularDepend.md)  
&emsp; [lazy-init](/docs/SSM/Spring/feature/lazy-init.md)  

[AOP基本概念](/docs/SSM/Spring/AOP.md)  
[SpringAOP教程](/docs/SSM/Spring/SpringAOP.md)  
[SpringAOP解析](/docs/SSM/Spring/SpringAOPAnalysis.md)  
[Spring事务](/docs/SSM/Spring/SpringTransaction.md)  

[SpringMVC使用教程](/docs/SSM/Spring/SpringMVCUse.md)  
[SpringMVC解析](/docs/SSM/Spring/SpringMVCAnalysis.md)    
[过滤器、拦截器、监听器](docs/web/subassembly.md)  

### MyBatis  
[MyBatis教程](/docs/SSM/MyBatis/Mybatis.md)  
&emsp; [MyBatis使用](/docs/SSM/MyBatis/MybatisSenior.md)  
&emsp; [PageHelper](/docs/SSM/MyBatis/PageHelper.md)  
&emsp; [Spring和MyBatis整合](/docs/SSM/MyBatis/SpringMybatis.md)  
[MyBatis解析](/docs/SSM/MyBatis/MybatisAnalysis.md)  
&emsp; [MyBatis架构](/docs/SSM/MyBatis/MybatisFramework.md)  
&emsp; [MyBatis SQL执行解析](/docs/SSM/MyBatis/MybatisExecutor.md)  
&emsp; [MyBatis缓存](/docs/SSM/MyBatis/MybatisCache.md)  
&emsp; [MyBatis插件解析](/docs/SSM/MyBatis/MybatisPlugins.md)  
&emsp; [MyBatis中的设计模式](/docs/SSM/MyBatis/MybatisDesign.md)  

----

## 分布式  
[RPC](/docs/microService/RPC.md)  
[Gateway](/docs/microService/microservices/Gateway.md)  

### SpringBoot  
[学习SpringBoot源码的感悟](/docs/microService/SpringBoot/thinking.md)  
[SpringBoot](/docs/microService/SpringBoot/SpringBoot.md)  
[SpringBoot启动过程-SpringApplication初始化](/docs/microService/SpringBoot/SpringApplication初始化.md)  
[SpringBoot启动过程-run()方法运行过程](/docs/microService/SpringBoot/run方法运行过程.md)  
&emsp; [SpringBoot事件回调机制](/docs/microService/SpringBoot/eventCallback.md)  
&emsp; [SpringBoot事件监听](/docs/microService/SpringBoot/3.SpringBootEvent.md)  
&emsp; [SpringBoot内置生命周期事件详解](/docs/microService/SpringBoot/4.SpringBootEvent.md)  
[SpringBoot自动配置原理-@SpringBootApplication](/docs/microService/SpringBoot/自动配置@SpringBootApplication.md)  
[SpringBoot自动配置原理-运行流程解析](/docs/microService/SpringBoot/自动配置运行流程解析.md)  
[内置Tomcat](/docs/microService/SpringBoot/内置Tomcat.md)  
[自定义strater](/docs/microService/SpringBoot/SpringBootStarter.md)  

### SpringCloud    
[Spring Cloud Netflix](/docs/microService/SpringCloudNetflix/0.Netflix.md)  
[Spring Cloud Eureka](/docs/microService/SpringCloudNetflix/1.Eureka.md)  
[Spring Cloud Ribbon](/docs/microService/SpringCloudNetflix/2.Ribbon.md)  
[Spring Cloud Hytrix](/docs/microService/SpringCloudNetflix/3.Hytrix.md)  
[Spring Cloud Feign](/docs/microService/SpringCloudNetflix/4.Feign.md)  
[Gateway](/docs/microService/microservices/Gateway.md)  
[Spring Cloud Zuul](/docs/microService/SpringCloudNetflix/5.Zuul.md)  
[Spring Cloud Sleuth](/docs/microService/SpringCloudNetflix/6.Sleuth.md)  
&emsp; [SpringMVC、dubbo集成zipkin](/docs/microService/SpringCloudNetflix/zipkin.md)  
[Spring Cloud Admin](/docs/microService/SpringCloudNetflix/7.SpringBootAdmin.md)  

### Dubbo  
[Dubbo](/docs/microService/Dubbo/Dubbo.md)   
[Dubbo使用教程](/docs/microService/Dubbo/Dubbo使用教程.md)  
[Dubbo框架设计](/docs/microService/Dubbo/design.md)  
[Dubbo源码](/docs/microService/Dubbo/DubboSource.md)  
&emsp; [扩展点加载(SPI)](/docs/microService/Dubbo/SPI.md)  
&emsp; &emsp; [获得指定拓展对象](/docs/microService/Dubbo/getExtension.md)  
&emsp; &emsp; [获得自适应的拓展对象](/docs/microService/Dubbo/getAdaptiveExtension.md)  
&emsp; [Dubbo运行流程](/docs/microService/Dubbo/realization.md)  
&emsp; &emsp; [Dubbo初始化](/docs/microService/Dubbo/dubboSpring.md)  
&emsp; &emsp; [服务暴露](/docs/microService/Dubbo/export.md)  
&emsp; &emsp; [服务引用](/docs/microService/Dubbo/introduce.md)  
&emsp; &emsp; [服务调用](/docs/microService/Dubbo/call.md)  

### Zookeeper
[Zookeeper](/docs/microService/Dubbo/Zookeeper.md)  

### 分布式理论  
[分布式和集群](/docs/system/distributed.md)   
[分布式和集中式](/docs/system/deploy.md)  
[分布式算法](/docs/microService/thinking/分布式算法.md)  
&emsp; [一致性哈希](/docs/microService/thinking/分布式算法-consistent.md)  
[分布式理论](/docs/microService/thinking/DistributedTheory.md)  

### 分布式缓存  
[缓存](/docs/cache/Cache.md)  
[分布式缓存](/docs/cache/DistributedCache.md)  
[缓存算法](/docs/cache/CacheAlgorithm.md)  

#### Redis
[学习Redis的总结](/docs/microService/Redis/RedisSummary.md)  
[Redis数据类型](/docs/microService/Redis/RedisStructure.md)  
&emsp; [Redis基本数据类型](/docs/microService/Redis/RedisBasicStructure.md)  
&emsp; [Redis的API](/docs/microService/Redis/RedisAPI.md)  
&emsp; [Redis扩展数据类型](/docs/microService/Redis/ExtendedDataType.md)  
&emsp; [Redis底层实现](/docs/microService/Redis/RedisBottom.md)  
[Redis开发规范](/docs/microService/Redis/RedisStandard.md)  
[Redis配置文件介绍](/docs/microService/Redis/RedisConf.md)  
[Redis内置功能](/docs/microService/Redis/BuiltIn.md)    
&emsp; [Redis发布订阅](/docs/microService/Redis/pub.md)  
&emsp; [Redis事务](/docs/microService/Redis/RedisTransaction.md)  
&emsp; [Redis和Lua](/docs/microService/Redis/lua.md)  
&emsp; [Redis持久化](/docs/microService/Redis/RedisPersistence.md)  
&emsp; [Redis淘汰策略](/docs/microService/Redis/RedisEliminate.md)  
[Redis部署](/docs/microService/Redis/RedisDeploy.md)  
&emsp; [Redis主从复制](/docs/microService/Redis/RedisMasterSlave.md)  
&emsp; [Redis哨兵模式](/docs/microService/Redis/RedisSentry.md)  
&emsp; [Redis集群模式](/docs/microService/Redis/RedisCluster.md)  
<!-- [Redis原理](/docs/microService/Redis/RedisPrinciple.md)  -->
[Redis运维](/docs/microService/Redis/RedisOperation.md)  
[Redis问题及排查](/docs/microService/Redis/problem.md)  

### 分布式消息队列  
[mq](/docs/microService/mq/mq.md)  

#### kafka   
[kafka安装](/docs/microService/mq/kafka/kafkaInstall.md)  
[kafka基本概念](/docs/microService/mq/kafka/kafka.md)  
[kafka生产者](/docs/microService/mq/kafka/kafkaProducerUse.md)  
[主题与分区](/docs/microService/mq/kafka/topic.md)  
[kafka消费者](/docs/microService/mq/kafka/kafkaConsumerUse.md)  
[kafka特性](/docs/microService/mq/kafka/kafka特性.md)  
&emsp; [kafka副本机制](/docs/microService/mq/kafka/kafkaReplica.md)  
&emsp; [kafka如何保证消息队列不丢失?](/docs/microService/mq/kafka/kafkaReliability.md)  
&emsp; [kafka幂等和事务](/docs/microService/mq/kafka/kafkaTraction.md)  
[kafka服务端](/docs/microService/mq/kafka/kafkaServer.md)  
[kafka集群管理](/docs/microService/mq/kafka/kafkaUse.md)  
[kafka集群监控与调优](/docs/microService/mq/kafka/kafkaMonitor.md)  
[kafka-SpringBoot](/docs/microService/mq/kafka/kafkaSpringBoot.md)  
[kafka高级应用](/docs/microService/mq/kafka/advanced.md)  
[kafkaStreams](/docs/microService/mq/kafka/kafkaStreams.md)  
[kafka源码]  
[kafka生产者](/docs/microService/mq/kafka/kafkaProducer.md)  
[kafka消费者](/docs/microService/mq/kafka/kafkaConsumer.md)  

### 分布式ID
[分布式ID](/docs/microService/thinking/分布式ID.md)  
[分库分表多维度查询](/docs/microService/thinking/分库分表多维度查询.md)  

### 分布式事务
[分布式事务](/docs/microService/thinking/分布式事务.md)  
&emsp; [DTP](/docs/microService/thinking/分布式事务-1.md)    
&emsp; [消息、事件模式](/docs/microService/thinking/分布式事务-2.md)   
&emsp; [TCC模式、事务状态表](/docs/microService/thinking/分布式事务-3.md)   
&emsp; [分布式事务的选型](/docs/microService/thinking/分布式事务的选型.md)  

### 分布式锁
[分布式锁](/docs/microService/thinking/分布式锁.md)  
&emsp; [Redis分布式锁](/docs/microService/thinking/redis分布式锁.md)  
&emsp; [ZK分布式锁](/docs/microService/thinking/ZK分布式锁.md)  
&emsp; [使用分布式锁的思考](/docs/microService/thinking/useLock.md)  

### 限流降级
[分布式限流](/docs/microService/thinking/分布式限流.md)   
&emsp; [Sentinel](/docs/microService/thinking/Sentinel.md)  
[服务降级](/docs/microService/thinking/服务降级.md)  

### 分布式调度
[延迟队列](/docs/frame/delayQueue.md)  
[StopWatch](/docs/frame/StopWatch.md)  
[时间轮算法](/docs/microService/Dubbo/timeWheel.md)  
[分布式调度](/docs/frame/taskSheduling.md)  
&emsp; [XXL-JOB](/docs/frame/XXL-JOB.md)  

### 分布式搜索引擎  
[ES搭建](/docs/ES/7.build.md)  
[ES基本概念](/docs/ES/1.basicConcepts.md)  
[ES集群](/docs/ES/3.colony.md)  
[ES使用](/docs/ES/2.useTutorials.md)  
&emsp; [ES建模](/docs/ES/modeling.md)  
&emsp; [索引操作](/docs/ES/index.md)  
&emsp; [文档操作](/docs/ES/document.md)  
&emsp; [映射详解](/docs/ES/mapping.md)  
&emsp; [检索操作](/docs/ES/search.md)  
&emsp; [多表关联](/docs/ES/multiTable.md)  
&emsp; [Java客户端](/docs/ES/JavaRestClient.md)  
[ES原理](/docs/ES/4.principle.md)  
[ES优化](/docs/ES/5.optimization.md)  
[ELK与EFK](/docs/ES/6.ELK与EFK.md)    
&emsp; [Kibana用户手册](/docs/ES/Kibana.md)  

### 分布式通信  
[序列化](/docs/microService/communication/serializbale.md)  
[NIO](/docs/microService/communication/NIO.md)  
&emsp; [NIO Channel](/docs/microService/communication/NIO/Channel.md)  
&emsp; [NIO Buffer](/docs/microService/communication/NIO/Buffer.md)  
&emsp; [NIO Selector](/docs/microService/communication/NIO/Selector.md)  
[Netty](/docs/microService/communication/Netty/netty.md)   
&emsp; [分布式通信基础](/docs/microService/communication/Netty/basics.md)   
&emsp; &emsp; [多路复用详解（select poll epoll）](/docs/microService/communication/Netty/epoll.md)  
&emsp; [Netty介绍及架构剖析](/docs/microService/communication/Netty/concepts.md)  
&emsp; [Netty核心组件](/docs/microService/communication/Netty/components.md)    
&emsp; [Netty高性能](/docs/microService/communication/Netty/performance.md)  
&emsp; &emsp; [零拷贝](/docs/microService/communication/Netty/zeroCopy.md)  
&emsp; &emsp; [Reactor与EventLoop](/docs/microService/communication/Netty/Reactor.md)  
&emsp; [TCP粘拆包与Netty编解码](/docs/microService/communication/Netty/Decoder.md)  
&emsp; [Netty实战](/docs/microService/communication/Netty/actualCombat.md)  
&emsp; [Netty源码](/docs/microService/communication/Netty/source.md)    
&emsp; &emsp; [Netty服务端创建](/docs/microService/communication/Netty/principle.md)  
&emsp; &emsp; [Netty客户端创建](/docs/microService/communication/Netty/customer.md)  
&emsp; &emsp; [内存分配byteBuf](/docs/microService/communication/Netty/byteBuf.md)    

## 其他框架  
[Security](/docs/frame/Security/Security.md)  

---

## 项目构建基础  
[日志系统](/docs/web/log.md)   
[SpringTest](/docs/web/test.md)  
[Assert处理异常](/docs/web/Assert.md)  
<!-- 
[乱码](/docs/web/garbled.md)  
-->
[统一格式返回](/docs/web/UnifiedFormat.md)  
[统一异常处理](/docs/web/ExceptionHandler.md)  
[统一日志记录](/docs/web/unifiedLog.md)  

[API接口设计](/docs/web/API.md)    
&emsp; [RESTful风格](/docs/web/interface/RESTful.md)  
&emsp; [接口幂等](/docs/web/interface/idempotent.md)  
&emsp; [接口防刷](/docs/web/interface/brush.md)  
&emsp; [接口安全](/docs/web/interface/security.md)  
&emsp; [接口超时](/docs/web/interface/timeout.md)  

[JavaBean](/docs/web/JavaBean.md)  
&emsp; [POJO](/docs/web/POJO.md)  
&emsp; [BeanUtils](/docs/web/BeanUtils.md)  
&emsp; [参数校验](/docs/web/Validation.md)  

[格式化](/docs/web/Format.md)  
[源码安全](/docs/web/codeSecurity.md)  
[Http重试](/docs/web/httpRetry.md)   
[数据脱敏](/docs/web/sensitive.md)  
[生成二维码](/docs/web/QRCode.md)  
[反爬虫](/docs/web/reptile.md)  
[加密算法](/docs/web/encryption.md)  
[其他](/docs/web/other.md)  
<!-- 
Httpclient4.3+ 连接池监控详细介绍
https://www.jianshu.com/p/2813af4eb0d3
-->

## 架构设计  
[架构图](/docs/system/diagram.md)  
&emsp; [接口文档](/docs/system/document/API.md)  
[软件架构质量属性](/docs/system/qualityAttributes.md)  
[软件架构设计模式](/docs/system/designPattern.md)  
[领域驱动模型](/docs/system/DDD.md)  

[分布式与微服务](/docs/system/serviceSplit.md)  
[并发系统三高](/docs/system/threeHigh.md)  
[高可用](/docs/system/highAvailability.md)  

[秒杀系统设计](/docs/system/seckill.md)  
[订单系统设计](/docs/system/order.md)  
[支付系统](/docs/system/payment.md)  
[个别场景](/docs/project/scene.md)  

[短链接](/docs/project/URL.md)  

## 负载均衡  
[负载均衡](/docs/system/loadBalance/loadBalance.md)  
[DNS轮询](/docs/system/loadBalance/DNS.md)  
[CDN](/docs/system/loadBalance/CDN.md)   
[LVS](/docs/system/loadBalance/LVS.md)  
[Keepalived](/docs/system/loadBalance/Keepalived.md)  

### Nginx  
[Nginx](/docs/system/loadBalance/Nginx/1.nginx.md)  
[Nginx使用](/docs/system/loadBalance/Nginx/2.nginx使用场景.md)   
[Nginx运维](/docs/system/loadBalance/Nginx/3.nginx运维.md)  

----

## Linux  
[Linux命令](/docs/Linux/Linux命令.md)  
&emsp; [文本处理](/docs/Linux/文本处理.md)  
&emsp; [网络通讯](/docs/Linux/网络通讯.md)  
[shell编程](/docs/Linux/shell.md)  
[堡垒机](/docs/Linux/baolei.md)  
[Linux系统](/docs/Linux/Linux.md)  

------

## DevOps  
[DevOps与CI/CD](/docs/devOps/devOps.md)  
[DevOps搭建](/docs/devOps/devOpsPractice.md)  
[Docker](/docs/devOps/docker/summary.md)  
&emsp; [Docker架构](/docs/devOps/docker/principle.md)  
&emsp; [Docker使用](/docs/devOps/docker/command.md)  
&emsp; [DockerFile](/docs/devOps/docker/file.md)  
&emsp; [镜像容器详解](/docs/devOps/docker/image.md)  
&emsp; [Docker工具](/docs/devOps/docker/tools.md)  
[Kubernetes](/docs/devOps/k8s/Kubernetes.md)      
&emsp; [k8s原理](/docs/devOps/k8s/principle.md)  
&emsp; [k8s实践](/docs/devOps/k8s/command.md)  
&emsp; [k8s重要对象](/docs/devOps/k8s/object.md)  
&emsp; [Yaml文件配置](/docs/devOps/k8s/yaml.md)  
&emsp; [k8s运维](/docs/devOps/k8s/tools.md)  
&emsp; [Kuboard介绍](/docs/devOps/k8s/kuboard.md)  
[Jenkins](/docs/devOps/Jenkins.md)  
[Maven](/docs/devOps/maven.md)  
[git](/docs/devOps/git/command.md)  

[灰度发布](/docs/system/grayscalePublishe.md)  
[监控](/docs/devOps/monitor.md)  

## 测试  
[Jmeter](/docs/devOps/Jmeter.md)  
[JMH](/docs/java/JVM/JMH.md)  

----

## 计算机网络  
[OSI七层网络模型](/docs/network/0.OSI.md)  
[HTTP](/docs/network/1.HTTP.md)  
[HTTPS](/docs/network/2.HTTPS.md)  
[DNS](docs/network/3.DNS.md)  
[TCP](/docs/network/4.TCP.md)  
[VPN](/docs/network/VPN.md)  
[衡量计算机网络的主要标准](/docs/network/standard.md)  

----

## 开发常用工具  
[idea](/docs/software/idea/idea.md)  
[抓包Charles](/docs/software/charles.md) 

## 其他工具  
[tools](/docs/software/tools/tools.md)  
[MarkDown](/docs/software/tools/MarkDown.md)    

---
## Error  
[多线程串线了](/docs/Error/ThreadCrossed.md)  
[接口响应时间](/docs/Error/responseTime.md)  
[熔断降级处理](/docs/Error/hystrix.md)  
[Redis高并发](/docs/Error/redisConcurrent.md)  
[Redis内存增长异常排查](/docs/Error/RedisMemoryGrowth.md)  
[redis scan命令](/docs/Error/redisScan.md)  
[雪花利用ZK生成workId]()  
<!-- [雪花利用ZK生成workId]()   -->

