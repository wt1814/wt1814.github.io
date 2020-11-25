<!-- TOC -->

- [wt1814-note](#wt1814-note)
    - [Java](#java)
        - [设计模式](#设计模式)
        - [并发编程](#并发编程)
        - [JVM](#jvm)
    - [数据库](#数据库)
    - [SSM](#ssm)
        - [Spring](#spring)
        - [MyBatis](#mybatis)
    - [分布式](#分布式)
        - [SpringBoot](#springboot)
        - [SpringCloud](#springcloud)
        - [微服务](#微服务)
        - [Dubbo](#dubbo)
        - [Zookeeper](#zookeeper)
        - [分布式理论](#分布式理论)
        - [分布式缓存](#分布式缓存)
            - [Redis](#redis)
            - [Ehcache](#ehcache)
        - [限流降级](#限流降级)
        - [分布式消息队列](#分布式消息队列)
            - [kafka](#kafka)
        - [分布式ID](#分布式id)
        - [分布式事务](#分布式事务)
        - [分布式锁](#分布式锁)
        - [分布式搜索引擎](#分布式搜索引擎)
        - [分布式调度](#分布式调度)
        - [分布式通信](#分布式通信)
    - [其他框架](#其他框架)
    - [项目构建基础](#项目构建基础)
    - [架构设计](#架构设计)
    - [负载均衡](#负载均衡)
        - [Nginx](#nginx)
    - [网络安全](#网络安全)
    - [Linux](#linux)
    - [DevOps](#devops)
    - [算法](#算法)
    - [计算机网络](#计算机网络)
    - [开发常用工具](#开发常用工具)
    - [其他工具](#其他工具)
    - [Error](#error)

<!-- /TOC -->

# wt1814-note  
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
[SPI](/docs/java/basis/SPI.md)  

### 设计模式  
[七大设计原则](/docs/java/Design/1.principles.md)  
[Java设计模式](/docs/java/Design/2.design.md)  
&emsp; [创建型设计模式](/docs/java/Design/3.establish.md)  
&emsp; [结构型设计模式](/docs/java/Design/4.structure.md)  
&emsp; &emsp; [动态代理](/docs/java/Design/6.proxy.md)   
&emsp; [行为型设计模式](/docs/java/Design/5.behavior.md)  
[设计模式讨论](/docs/java/Design/discuss.md)  


### 并发编程  
[并发编程总结](/docs/java/concurrent/summary.md)  
[线程基本概念](/docs/java/concurrent/1.Thread.md)  
&emsp; [wait](/docs/java/concurrent/wait.md)  
[并发问题](/docs/java/concurrent/2.Multithread.md)  
&emsp; [JMM](/docs/java/concurrent/JMM.md)  
&emsp; [线程安全](/docs/java/concurrent/线程安全.md)  
&emsp; &emsp; [Synchronized](/docs/java/concurrent/Synchronized.md)  
&emsp; &emsp; [Volatile](/docs/java/concurrent/Volatile.md)  
&emsp; &emsp; [ThreadLocal](/docs/java/concurrent/ThreadLocal.md)  
&emsp; [线程通信](/docs/java/concurrent/线程通信.md)  
&emsp; [线程活跃性](/docs/java/concurrent/Activity.md)  
[线程池](/docs/java/concurrent/3.ThreadPool.md)  
&emsp; [ThreadPoolExecutor](/docs/java/concurrent/ThreadPoolExecutor.md)  
&emsp; [ForkJoinPool](/docs/java/concurrent/ForkJoinPool.md)  
&emsp; [CompletableFuture](/docs/java/concurrent/CompletableFuture.md)  
[J.U.C包](/docs/java/concurrent/4.ConcurrentPackage.md)  
&emsp; [Lock](/docs/java/concurrent/8.Lock.md)  
&emsp; &emsp; [LocLockSupportk](/docs/java/concurrent/LockSupport.md)         
&emsp; [Atmoic](/docs/java/concurrent/9.Atmoic.md)  
&emsp; &emsp; [AtomicStampedReference](/docs/java/concurrent/6.AtomicStampedReference.md)  
&emsp; [Collections](/docs/java/concurrent/10.Collections.md)  
&emsp; &emsp; [ConcurrentHashMap](/docs/java/concurrent/5.ConcurrentHashMap.md)  
&emsp; &emsp; [BlockingQueue](/docs/java/concurrent/BlockingQueue.md)  
&emsp; [tools](/docs/java/concurrent/7.tools.md)  
[并发框架Disruptor](/docs/java/concurrent/disruptor.md)  
[多线程处理大数据量](/docs/java/concurrent/bigData.md)  

### JVM  
[JVM总结](/docs/java/JVM/summary.md)  
[JDK、JRE、JVM](/docs/java/JVM/1.JDK、JRE、JVM三者间的关系.md)   
[字节码文件](/docs/java/JVM/Class.md)  
[JVM类的加载](/docs/java/JVM/2.JVM类的加载.md)  
[JVM内存结构](/docs/java/JVM/3.JVM内存结构.md)  
[GC垃圾回收](/docs/java/JVM/5.GC垃圾回收.md)  
[JVM调优-基础](/docs/java/JVM/6.JVM调优-基础.md)  
[JVM调优](/docs/java/JVM/7.JVM调优.md)  
&emsp; [JVM排查案例](/docs/java/JVM/case.md)  
[JAVA线上故障排查](/docs/Linux/problem.md)  
[Arthas](/docs/java/JVM/Arthas.md)  
[JMH](/docs/java/JVM/JMH.md)  

## 数据库  
[学习SQL的总结](/docs/SQL/学习SQL的总结.md)  
[建模](/docs/SQL/modeling.md)    
[SQL语句](/docs/SQL/1.SQL语句.md)  
&emsp; [基本查询语句](/docs/SQL/9.basicSelect.md)  
&emsp; &emsp; [orderLimit](/docs/SQL/orderLimit.md)  
&emsp; [连接查询](/docs/SQL/10.joinSelect.md)  
&emsp; [行列转换](/docs/SQL/11.trans.md)  
[MySql函数](/docs/SQL/MySQLFunction.md)  
[数据库对象](/docs/SQL/DatabaseObject.md)  
[SQL优化](/docs/SQL/2.SQL优化.md)  
&emsp; [索引优化](/docs/SQL/7.index.md)  
&emsp; [优化案例](/docs/SQL/12.case.md)  
[MySql架构](/docs/SQL/8.MySql架构.md)  
[MySql原理]  
&emsp; [MySql存储引擎](/docs/SQL/13.MySqlStorage.md)  
&emsp; &emsp; [InnoDB](/docs/SQL/InnoDB.md)  
&emsp; [MySql事务](/docs/SQL/14.transaction.md)  
&emsp; [MySql锁](/docs/SQL/15.lock.md)  
&emsp; [MySqlMVCC](/docs/SQL/MVCC.md)  
&emsp; [MySql事务日志](/docs/SQL/log.md)  
[数据库分布式](/docs/SQL/4.分布式数据库.md)  
&emsp; [主从复制](/docs/SQL/16.replication.md)  
&emsp; &emsp; [主从复制的高可用](/docs/SQL/replicationAvailability.md)   
&emsp; &emsp; [主从复制的问题](/docs/SQL/replicationProblem.md)  
&emsp; &emsp; [读写分离](/docs/SQL/SeparationReade.md)  
&emsp; [分区](/docs/SQL/17.partition.md)  
&emsp; [分库分表](/docs/SQL/18.sub.md)  
&emsp; [数据库分布式实现](/docs/SQL/subRealize.md)  
&emsp; &emsp; [MyCat中间件](/docs/SQL/5.MyCat中间件.md)  
[数据迁移](/docs/projectImplement/implementation.md)  
[大数据量操作](/docs/SQL/largeData.md)  

[数据库连接池](/docs/SQL/connectionPool.md)  
&emsp; [HikariCPConnectionPool]   
&emsp; &emsp; [HikariCP原理](/docs/SQL/HikariCPPrinciple.md)  
&emsp; &emsp; [HikariCP监控与故障排查](/docs/SQL/HikariCPMonitor.md)  

## SSM  
### Spring  
[学习Spring源码的感悟](/docs/SSM/Spring/thinking.md)  
[Spring](/docs/SSM/Spring/1.Spring.md)  
[SpringIOC解析](/docs/SSM/Spring/2.SpringIOC.md)  
&emsp; [容器初始化详解](/docs/SSM/Spring/容器初始化详解.md)  
[SpringDI解析](/docs/SSM/Spring/3.SpringDI.md)  
[SpringBean生命周期](/docs/SSM/Spring/SpringBean.md)  
[容器相关特性]  
&emsp; [FactoryBean](/docs/SSM/Spring/feature/FactoryBean.md)  
&emsp; [可二次开发常用接口](/docs/SSM/Spring/feature/可二次开发常用接口.md)  
&emsp; &emsp; [Aware接口](/docs/SSM/Spring/feature/Aware.md)  
&emsp; &emsp; [后置处理器](/docs/SSM/Spring/feature/BeanFactoryPostProcessor.md)  
&emsp; &emsp; [InitializingBean](/docs/SSM/Spring/feature/InitializingBean.md)  
&emsp; &emsp; [事件](/docs/SSM/Spring/feature/Event.md)  
&emsp; [循环依赖](/docs/SSM/Spring/feature/循环依赖.md)  
&emsp; [lazy-init](/docs/SSM/Spring/feature/lazy-init.md)  

[SpringAOP教程](/docs/SSM/Spring/5.SpringAOP.md)  
[SpringAOP解析](/docs/SSM/Spring/6.SpringAOP解析.md)  
[Spring事务](/docs/SSM/Spring/7.SpringTransaction.md)  

[SpringMVC使用教程](/docs/SSM/Spring/9.SpringMVCUse.md)  
[SpringMVC解析](/docs/SSM/Spring/10.SpringMVCAnalysis.md)    
[过滤器、拦截器、监听器](docs/web/subassembly.md)  

### MyBatis  
[MyBatis教程](/docs/SSM/MyBatis/Mybatis.md)  
[MyBatis SQL执行解析](/docs/SSM/MyBatis/MybatisExecutor.md)  
[MyBatis缓存](/docs/SSM/MyBatis/MybatisCache.md)  
[MyBatis插件解析](/docs/SSM/MyBatis/MybatisPlugins.md)  
[PageHelper](/docs/SSM/MyBatis/PageHelper.md)  
[Spring和MyBatis整合](/docs/SSM/MyBatis/SpringMybatis.md)  
[MyBatis中的设计模式](/docs/SSM/MyBatis/MybatisDesign.md)  

----

## 分布式  
### SpringBoot  
<!-- 
 非常有必要了解的Springboot启动扩展点 
 https://mp.weixin.qq.com/s/H9hcQHZUNhuRodEPiVOHfQ
 https://mp.weixin.qq.com/s/Z5meCbbfgUmnLnnWjeEeVw
-->
[学习SpringBoot源码的感悟](/docs/microService/SpringBoot/thinking.md)  
[SpringBoot](/docs/microService/SpringBoot/SpringBoot.md)  
[SpringBoot启动过程-SpringApplication初始化](/docs/microService/SpringBoot/SpringApplication初始化.md)  
[SpringBoot启动过程-run()方法运行过程](/docs/microService/SpringBoot/run方法运行过程.md)  
&emsp; [SpringBoot事件回调机制](/docs/microService/SpringBoot/eventCallback.md)  
&emsp; [SpringBoot事件监听](/docs/microService/SpringBoot/3.SpringBootEvent.md)  
&emsp; [SpringBoot内置生命周期事件详解](/docs/microService/SpringBoot/4.SpringBootEvent.md)  
[SpringBoot自动配置原理-@SpringBootApplication](/docs/microService/SpringBoot/自动配置@SpringBootApplication.md)  
[SpringBoot自动配置原理-运行流程解析](/docs/microService/SpringBoot/自动配置运行流程解析.md)  
[内置Tomcat]()  
<!-- [内置Tomcat]()   -->
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
<!-- 
[Spring Cloud Config]  
[Spring Cloud Bus]  
[Spring Cloud Security]  
-->
[Spring Cloud Admin](/docs/microService/SpringCloudNetflix/7.SpringBootAdmin.md)  


### 微服务  
[RPC](/docs/microService/RPC.md)  
[微服务架构设计](/docs/microService/microservices/microservices.md)  
[Gateway](/docs/microService/microservices/Gateway.md)  


### Dubbo  
[Dubbo](/docs/microService/Dubbo/Dubbo.md)   
[Dubbo使用教程](/docs/microService/Dubbo/Dubbo使用教程.md)  
[Dubbo生态](/docs/microService/Dubbo/ecology.md)  

[Dubbo框架设计](/docs/microService/Dubbo/design.md)  
[扩展点加载(SPI)](/docs/microService/Dubbo/SPI.md)  
&emsp; [获得指定拓展对象](/docs/microService/Dubbo/getExtension.md)  
&emsp; [获得自适应的拓展对象](/docs/microService/Dubbo/getAdaptiveExtension.md)  
[Dubbo实现细节](/docs/microService/Dubbo/realization.md)  
&emsp; [Dubbo初始化](/docs/microService/Dubbo/dubboSpring.md)  
&emsp; [服务暴露](/docs/microService/Dubbo/export.md)  
&emsp; [服务引用](/docs/microService/Dubbo/introduce.md)  
<!-- 
Dubbo集群容错  
https://www.cnblogs.com/caoxb/p/13140347.html
-->
&emsp; [服务调用](/docs/microService/Dubbo/call.md)  
[时间轮算法](/docs/microService/Dubbo/timeWheel.md)  


### Zookeeper
[Zookeeper](/docs/microService/Dubbo/Zookeeper.md)  

### 分布式理论  
[分布式和集群](/docs/system/distributed.md)   
[分布式和集中式](/docs/system/deploy.md)  
[分布式算法](/docs/microService/thinking/分布式算法.md)  
&emsp; [一致性哈希](/docs/microService/thinking/分布式算法-consistent.md)  
[分布式理论](/docs/microService/thinking/DistributedTheory.md)  

### 分布式缓存  
[分布式缓存](/docs/microService/thinking/分布式缓存.md)  

#### Redis
[学习Redis的总结](/docs/microService/Redis/RedisSummary.md)  
[Redis基本数据类型](/docs/microService/Redis/Redis数据结构.md)  
&emsp; [Redis的API](/docs/microService/Redis/RedisAPI.md)  
&emsp; [Redisson](/docs/microService/Redis/Redisson.md)  
&emsp; [Redis高级数据类型](/docs/microService/Redis/Redis高级数据类型.md)  
&emsp; [Redis底层实现](/docs/microService/Redis/Redis底层实现.md)  
[Redis其他功能]  
&emsp; [Redis发布订阅](/docs/microService/Redis/pub.md)  
&emsp; [Redis和lua](/docs/microService/Redis/pub.md)  
[Redis配置文件](/docs/microService/Redis/RedisConf.md)  
[Redis开发规范](/docs/microService/Redis/RedisStandard.md)  
[Redis原理](/docs/microService/Redis/Redis原理.md)  
<!-- 
Redis的虚拟内存
https://mp.weixin.qq.com/s/CmfUSVfMss8TeQkLrE8GGQ
-->
&emsp; [Redis持久化](/docs/microService/Redis/Redis持久化.md)  
&emsp; [Redis淘汰策略](/docs/microService/Redis/Redis淘汰.md)  
&emsp; [Redis事务](/docs/microService/Redis/Redis事务.md)  
[Redis部署](/docs/microService/Redis/Redis部署.md)  
&emsp; [Redis主从复制](/docs/microService/Redis/Redis主从复制.md)  
&emsp; [Redis哨兵模式](/docs/microService/Redis/Redis哨兵模式.md)  
&emsp; [Redis集群模式](/docs/microService/Redis/Redis集群模式.md)  
[Redis运维](/docs/microService/Redis/Redis运维.md)  
[Redis6.0](/docs/microService/Redis/Redis6.0.md)  
[Redis问题及排查](/docs/microService/Redis/problem.md)  

#### Ehcache 

### 限流降级
[分布式限流](/docs/microService/thinking/分布式限流.md)   
&emsp; [Sentinel](/docs/microService/thinking/Sentinel.md)  
[服务降级](/docs/microService/thinking/服务降级.md)  

### 分布式消息队列  
[mq](/docs/microService/mq/mq.md)  

#### kafka   
[kafka基本概念](/docs/microService/mq/kafka/kafka.md)  
[kafka安装](/docs/microService/mq/kafka/kafkaInstall.md)  
[kafka生产者](/docs/microService/mq/kafka/kafkaProducerUse.md)  
[kafka消费者](/docs/microService/mq/kafka/kafkaConsumerUse.md)  
[主题与分区](/docs/microService/mq/kafka/topic.md)  
[kafka特性](/docs/microService/mq/kafka/kafka特性.md)  
&emsp; [kafka事务](/docs/microService/mq/kafka/kafkaTraction.md)  
[kafkaStreams](/docs/microService/mq/kafka/kafkaStreams.md)  
[kafka服务端](/docs/microService/mq/kafka/kafkaServer.md)  
[kafka集群管理](/docs/microService/mq/kafka/kafkaUse.md)  
[kafka集群监控与调优](/docs/microService/mq/kafka/kafkaMonitor.md)  
[kafka-SpringBoot](/docs/microService/mq/kafka/kafkaSpringBoot.md)  
[kafka与Spark集成](/docs/microService/mq/kafka/kafkaSpark.md)  
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

### 分布式搜索引擎  
[ES基本概念](/docs/ES/1.basicConcepts.md)  
[ES集群](/docs/ES/3.colony.md)  
[ES使用教程](/docs/ES/2.useTutorials.md)  
[ES搭建](/docs/ES/7.build.md)  
[ES原理](/docs/ES/4.principle.md)  
[ES优化-1](/docs/ES/5.optimization.md)  
[ELK与EFK](/docs/ES/6.ELK与EFK.md)    
&emsp; [Kibana 用户手册](/docs/ES/Kibana.md)  

### 分布式调度
[延迟队列](/docs/frame/delayQueue.md)  
[StopWatch](/docs/frame/StopWatch.md)  
[分布式调度](/docs/frame/taskSheduling.md)  
&emsp; [XXL-JOB](/docs/frame/XXL-JOB.md) 

### 分布式通信  
[序列化](/docs/microService/communication/serializbale.md)  
[NIO](/docs/microService/communication/NIO.md)  
&emsp; [NIO Channel](/docs/microService/communication/NIO/Channel.md)  
&emsp; [NIO Buffer](/docs/microService/communication/NIO/Buffer.md)  
&emsp; [NIO Selector](/docs/microService/communication/NIO/Selector.md)  
[Netty]  
&emsp; [分布式通信基础](/docs/microService/communication/Netty/basics.md)   
&emsp; &emsp; [多路复用详解（select poll epoll）](/docs/microService/communication/Netty/epoll.md)  
&emsp; [Netty介绍及架构剖析](/docs/microService/communication/Netty/concepts.md)  
&emsp; [Netty核心组件](/docs/microService/communication/Netty/components.md)    
&emsp; [启动器Bootstrap]  
&emsp; &emsp; [Netty服务端创建](/docs/microService/communication/Netty/principle.md)  
&emsp; &emsp; [Netty客户端创建](/docs/microService/communication/Netty/customer.md)  
&emsp; [内存分配byteBuf](/docs/microService/communication/Netty/byteBuf.md)    
&emsp; [Netty高性能](/docs/microService/communication/Netty/performance.md)  
&emsp; &emsp; [零拷贝](/docs/microService/communication/Netty/DMA.md)  
&emsp; &emsp; [Reactor与EventLoop](/docs/microService/communication/Netty/Reactor.md)  
&emsp; [TCP粘拆包与Netty编解码](/docs/microService/communication/Netty/Decoder.md)  
&emsp; [Netty实战](/docs/microService/communication/Netty/actualCombat.md)  


## 其他框架  
[Security](/docs/frame/Security/Security.md)  

---

## 项目构建基础  
[日志系统](/docs/web/log.md)   
[SpringTest](/docs/web/test.md)  
[乱码](/docs/web/garbled.md)  
[Assert处理异常](/docs/web/Assert.md)  

[统一日志记录]()  
<!-- [统一日志记录]()   -->
[统一格式返回](/docs/web/UnifiedFormat.md)  
[统一异常处理](/docs/web/ExceptionHandler.md)  

[API接口设计](/docs/web/API.md)    
&emsp; [RESTful](/docs/microService/RESTful.md)  

&emsp; [接口幂等](/docs/web/idempotent.md)  
<!-- 
&emsp; [接口防刷]()  
&emsp; [接口安全]()  
-->
&emsp; [接口超时](/docs/web/timeout.md)  

[JavaBean](/docs/web/JavaBean.md)  
&emsp; [POJO](/docs/web/POJO.md)  
&emsp; [BeanUtils](/docs/web/BeanUtils.md)  
&emsp; [参数校验](/docs/web/Validation.md)  

[Http重试](/docs/web/httpRetry.md)   
[敏感词汇](/docs/web/sensitive.md)  
[生成二维码](/docs/web/QRCode.md)  
[反爬虫](/docs/web/reptile.md)  
[加密算法](/docs/web/encryption.md)  
[源码安全](/docs/web/codeSecurity.md)  
[其他](/docs/web/other.md)  

## 架构设计  
<!-- 

[系统稳定性建设]()  
10个有意思的架构问题！
https://mp.weixin.qq.com/s/0IyRzXGzgSKyBBclB96xww

 一个复杂系统的拆分改造实践！ 
 https://mp.weixin.qq.com/s/OSAxKO6tddY4TucRADqOiQ
-->
[架构图](/docs/system/diagram.md)  
&emsp; [设计文档]()  
&emsp; [接口文档](/docs/system/document/API.md)  
[性能指标](/docs/system/performance.md)  
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

## 网络安全  
[网络安全]( /docs/system/network/security.md)  

----

## Linux  
[Linux命令](/docs/Linux/Linux命令.md)  
&emsp; [文本处理](/docs/Linux/文本处理.md)  
&emsp; [网络通讯](/docs/Linux/网络通讯.md)  
[shell编程](/docs/Linux/shell.md)  
[堡垒机](/docs/Linux/baolei.md)  

## DevOps  
[DevOps与CI/CD](/docs/devOps/devOps.md)  
[DevOps搭建](/docs/devOps/devOpsPractice.md)  

[docker]  
&emsp; [docker总结](/docs/devOps/docker/summary.md)  
&emsp; [docker架构](/docs/devOps/docker/principle.md)  
&emsp; [docker核心技术与实现原理](/docs/devOps/docker/theory.md)  
&emsp; [镜像容器详解](/docs/devOps/docker/image.md)  
&emsp; [docker使用](/docs/devOps/docker/command.md)  
&emsp; [dockerFile](/docs/devOps/docker/file.md)  
&emsp; [docker工具](/docs/devOps/docker/tools.md)   
[Kubernetes]  
&emsp; [k8s原理](/docs/devOps/k8s/principle.md)  
&emsp; [k8s实践](/docs/devOps/k8s/command.md)  
&emsp; &emsp; [滚动更新](/docs/devOps/k8s/rollingUpdate.md)  
&emsp; [k8s运维](/docs/devOps/k8s/tools.md)  
&emsp; [Kuboard介绍](/docs/devOps/k8s/kuboard.md)  

[Jenkins](/docs/devOps/Jenkins.md)  
[Maven](/docs/devOps/maven.md)  
[git](/docs/devOps/git/command.md)  

[灰度发布](/docs/system/grayscalePublishe.md)  
&emsp; [网关灰度发布](/docs/system/gatewayGrayscale.md)  
[监控](/docs/devOps/monitor.md)  

---

## 算法  
[总结](/docs/java/function/summary.md)  
[算法基本概念](/docs/java/function/1.notion.md)  
[基本数据结构]  
&emsp; [链表](/docs/java/function/structure/array.md)  
&emsp; [栈](/docs/java/function/structure/stack.md)  
&emsp; [哈希](/docs/java/function/structure/hash.md)  
&emsp; [树](/docs/java/function/structure/tree.md)  
[排序算法](/docs/java/function/4.sort.md)  
&emsp; [比较排序](/docs/java/function/sort/compare.md)  
&emsp; [非比较排序](/docs/java/function/sort/noCompare.md)  
[外部排序](/docs/java/function/sort/externalSort.md)  
[查找算法](/docs/java/function/5.search.md)  
[算法思想题型](/docs/java/function/6.algorithmicIdea.md)  
[其他数据结构]  
&emsp; [其他数据结构](/docs/java/function/3.otherStructure.md)  
&emsp; [海量数据应用](/docs/java/function/bigdata.md)  

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


---

[个人笔录](/docs/note/record.md)  

