

<!-- TOC -->

- [目录](#目录)
    - [杂记](#杂记)
    - [Java](#java)
        - [并发编程](#并发编程)
        - [JVM](#jvm)
        - [设计模式](#设计模式)
    - [数据库](#数据库)
    - [SSM](#ssm)
        - [Spring](#spring)
        - [MyBatis](#mybatis)
    - [微服务](#微服务)
        - [SpringBoot](#springboot)
        - [Spring Cloud Netflix](#spring-cloud-netflix)
        - [Dubbo](#dubbo)
        - [Redis](#redis)
        - [分布式理论](#分布式理论)
        - [分布式通信](#分布式通信)
    - [Elaticsearch](#elaticsearch)
    - [项目构建基础](#项目构建基础)
    - [Error](#error)
    - [服务器](#服务器)
        - [Linux](#linux)
        - [Nginx](#nginx)
    - [计算机网络](#计算机网络)
    - [算法](#算法)
    - [项目管理](#项目管理)
    - [开发软件](#开发软件)
    - [其他工具](#其他工具)

<!-- /TOC -->

# 目录  

欢迎关注个人公众号：**<font color = "red">王猴卖瓜</font>**  
GitHub： https://github.com/wt1814/  
Gitee： https://gitee.com/wt1814/  
联系我：**wangtao1814@163.com**  

## 杂记  
<font color="red">多问几个为什么，怎么办，怎么做。</font>  
[我自己的学习之路]  

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
[IO](/docs/java/basis/JavaIO.md)  
[SPI](/docs/java/basis/SPI.md)  

### 并发编程  
[线程基本概念](/docs/java/concurrent/1.Thread.md)  
&emsp; [wait](/docs/java/concurrent/wait.md)  
[JMM](/docs/java/concurrent/2.Multithread.md)  
&emsp; [JMM](/docs/java/concurrent/JMM.md)  
&emsp; [线程安全](/docs/java/concurrent/线程安全.md)  
&emsp; &emsp; [Synchronized](/docs/java/concurrent/Synchronized.md)  
&emsp; &emsp; [Volatile](/docs/java/concurrent/Volatile.md)  
&emsp; &emsp; [ThreadLocal](/docs/java/concurrent/ThreadLocal.md)  
&emsp; [线程通信](/docs/java/concurrent/线程通信.md)  
[线程池](/docs/java/concurrent/3.ThreadPool.md)  
&emsp; [ThreadPoolExecutor](/docs/java/concurrent/ThreadPoolExecutor.md)  
&emsp; [ForkJoinPool](/docs/java/concurrent/ForkJoinPool.md)  
[J.U.C包](/docs/java/concurrent/4.ConcurrentPackage.md)  
&emsp; [Lock](/docs/java/concurrent/8.Lock.md)  
&emsp; [Atmoic](/docs/java/concurrent/9.Atmoic.md)  
&emsp; &emsp; [AtomicStampedReference](/docs/java/concurrent/6.AtomicStampedReference.md)  
&emsp; [Collections](/docs/java/concurrent/10.Collections.md)  
&emsp; &emsp; [ConcurrentHashMap](/docs/java/concurrent/5.ConcurrentHashMap.md)  
&emsp; &emsp; [BlockingQueue](/docs/java/concurrent/BlockingQueue.md)  
&emsp; [tools](/docs/java/concurrent/7.tools.md)  

### JVM  
[JDK、JRE、JVM](/docs/java/JVM/1.JDK、JRE、JVM三者间的关系.md)  
[Class文件]   
[JVM类的加载](/docs/java/JVM/2.JVM类的加载.md)  
[JVM内存结构](/docs/java/JVM/3.JVM内存结构.md)  

[GC垃圾回收](/docs/java/JVM/5.GC垃圾回收.md)  
[JVM调优-基础](/docs/java/JVM/6.JVM调优-基础.md)  
[JVM调优](/docs/java/JVM/7.JVM调优.md)  

### 设计模式  
[七大设计原则](/docs/java/Design/1.principles.md)  
[Java设计模式](/docs/java/Design/2.design.md)  
[创建型设计模式](/docs/java/Design/3.establish.md)  
[结构型设计模式](/docs/java/Design/4.structure.md)  
&emsp; [动态代理](/docs/java/Design/6.proxy.md)   
[行为型设计模式](/docs/java/Design/5.behavior.md)  


## 数据库  
[SQL语句](/docs/SQL/1.SQL语句.md)  
&emsp; [基本查询语句](/docs/SQL/9.basicSelect.md)  
&emsp; [连接查询](/docs/SQL/10.joinSelect.md)  
&emsp; [行列转换](/docs/SQL/11.trans.md)  
[MySql函数](/docs/SQL/MySQLFunction.md)  
[数据库对象](/docs/SQL/DatabaseObxject.md)  
[SQL优化](/docs/SQL/2.SQL优化.md)  
&emsp; [索引优化](/docs/SQL/7.index.md)  
&emsp; [SQL语句优化](/docs/SQL/SQL语句优化.md)  
&emsp; [优化案例](/docs/SQL/12.case.md)  
[MySql架构](/docs/SQL/8.MySql架构.md)  
[MySql日志](/docs/SQL/log.md)  
[MySql原理]  
&emsp; [MySql存储引擎](/docs/SQL/13.MySqlStorage.md)  
&emsp; &emsp; [InnoDB](/docs/SQL/InnoDB.md)  
&emsp; [MySql事务](/docs/SQL/14.transaction.md)  
&emsp; [MySql锁](/docs/SQL/15.lock.md)  
&emsp; [MySqlMVCC](/docs/SQL/MVCC.md)  
&emsp; [崩溃恢复](/docs/SQL/崩溃恢复.md)  
[分布式数据库](/docs/SQL/4.分布式数据库.md)  
&emsp; [主从复制](/docs/SQL/16.replication.md)  
&emsp; [分区](/docs/SQL/17.partition.md)  
&emsp; [分库分表-1](/docs/SQL/18.sub.md)  
&emsp; [数据迁移](/docs/projectImplement/implementation.md)  
[MyCat中间件](/docs/SQL/5.MyCat中间件.md)  
[AOP多数据源动态切换](/docs/SQL/6.multiDataSource.md)  


## SSM  
### Spring  
[Spring-1](/docs/SSM/Spring/1.Spring.md)  
[对于学习Spring源码的感悟](/docs/SSM/Spring/thinking.md)  
[SpringIOC解析](/docs/SSM/Spring/2.SpringIOC.md)  
&emsp; [容器初始化详解](/docs/SSM/Spring/容器初始化详解.md)  
[SpringDI解析](/docs/SSM/Spring/3.SpringDI.md)  
[SpringBean生命周期](/docs/SSM/Spring/SpringBean.md)  
[容器相关特性]  
&emsp; [可二次开发常用接口](/docs/SSM/Spring/feature/可二次开发常用接口.md)  
&emsp; &emsp; [Aware接口](/docs/SSM/Spring/feature/Aware.md)  
&emsp; &emsp; [后置处理器](/docs/SSM/Spring/feature/BeanFactoryPostProcessor.md)  
&emsp; &emsp; [InitializingBean](/docs/SSM/Spring/feature/InitializingBean.md)  
&emsp; &emsp; [事件](/docs/SSM/Spring/feature/Event.md)  
&emsp; [FactoryBean](/docs/SSM/Spring/feature/FactoryBean.md)  
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
[MyBatis插件解析-1](/docs/SSM/MyBatis/MybatisPlugins.md)  
[Spring和MyBatis整合-1](/docs/SSM/MyBatis/SpringMybatis.md)  
[MyBatis中的设计模式](/docs/SSM/MyBatis/MybatisDesign.md)  


## 微服务  
### SpringBoot  
[SpringBoot](/docs/microService/SpringBoot/SpringBoot.md)  
[SpringBoot启动过程-SpringApplication初始化](/docs/microService/SpringBoot/SpringApplication初始化.md)  
[SpringBoot启动过程-run()方法运行过程](/docs/microService/SpringBoot/run方法运行过程.md)  
[SpringBoot自动配置原理-@SpringBootApplication](/docs/microService/SpringBoot/自动配置@SpringBootApplication.md)  
[SpringBoot自动配置原理-运行流程解析](/docs/microService/SpringBoot/自动配置运行流程解析.md)  
[SpringBoot事件监听机制](/docs/microService/SpringBoot/3.SpringBootEvent.md)  
[SpringBoot内置生命周期事件详解](/docs/microService/SpringBoot/4.SpringBootEvent.md)  
[自定义strater](/docs/microService/SpringBoot/SpringBootStarter.md)  

### Spring Cloud Netflix   
[Spring Cloud Netflix](/docs/microService/SpringCloudNetflix/0.Netflix.md)  
[Spring Cloud Eureka](/docs/microService/SpringCloudNetflix/1.Eureka.md)  
[Spring Cloud Ribbon](/docs/microService/SpringCloudNetflix/2.Ribbon.md)  
[Spring Cloud Hytrix](/docs/microService/SpringCloudNetflix/3.Hytrix.md)  
[Spring Cloud Feign](/docs/microService/SpringCloudNetflix/4.Feign.md)  
[Spring Cloud Zuul](/docs/microService/SpringCloudNetflix/5.Zuul.md)  
[Spring Cloud Sleuth](/docs/microService/SpringCloudNetflix/6.Sleuth.md)  
<!-- 
[Spring Cloud Config]  
[Spring Cloud Bus]  
[Spring Cloud Security]  
-->
[Spring Boot Admin](/docs/microService/SpringCloudNetflix/7.SpringBootAdmin.md)  

### Dubbo  
[Dubbo](/docs/microService/Dubbo/Dubbo.md)   
[Dubbo使用教程](/docs/microService/Dubbo/Dubbo使用教程.md)  
[Zookeeper](/docs/microService/Dubbo/Zookeeper.md)  

### Redis
[Redis基本数据类型](/docs/microService/Redis/Redis数据结构.md)  
&emsp; [Redis的API](/docs/microService/Redis/RedisAPI.md)  
&emsp; [Redis高级数据类型](/docs/microService/Redis/Redis高级数据类型.md)  
&emsp; [Redis底层实现](/docs/microService/Redis/Redis底层实现.md)   
[Redis配置文件](/docs/microService/Redis/RedisConf.md)  
[Redis开发规范](/docs/microService/Redis/RedisStandard.md)  
[Redis原理](/docs/microService/Redis/Redis原理.md)  
&emsp; [Redis持久化](/docs/microService/Redis/Redis持久化.md)  
&emsp; [Redis淘汰策略](/docs/microService/Redis/Redis淘汰.md)  
&emsp; [Redis事务](/docs/microService/Redis/Redis事务.md)  
[Redis部署](/docs/microService/Redis/Redis集群.md)  
&emsp; [Redis主从复制](/docs/microService/Redis/Redis主从复制.md)  
&emsp; [Redis哨兵模式](/docs/microService/Redis/Redis哨兵模式.md)  
&emsp; [Redis分片模式](/docs/microService/Redis/Redis分片模式.md)  
[Redis运维](/docs/microService/Redis/Redis运维.md)  
[Redis6.0](/docs/microService/Redis/Redis6.0.md)  

### 分布式理论  
[分布式算法](/docs/microService/thinking/分布式算法-consistent.md)  
[分布式理论](/docs/microService/thinking/DistributedTheory.md)  
[分布式事务](/docs/microService/thinking/分布式事务.md)  
&emsp; [DTP](/docs/microService/thinking/分布式事务-1.md)    
&emsp; [消息、事件模式](/docs/microService/thinking/分布式事务-2.md)   
&emsp; [TCC模式](/docs/microService/thinking/分布式事务-3.md)   
&emsp; [分布式事务的选型](/docs/microService/thinking/分布式事务的选型.md)  
[分布式锁](/docs/microService/thinking/分布式锁.md)  
&emsp; [Redis分布式锁](/docs/microService/thinking/redis分布式锁.md)  
&emsp; [ZK分布式锁](/docs/microService/thinking/ZK分布式锁.md)  
&emsp; [使用分布式锁的思考](/docs/microService/thinking/useLock.md)  
[分布式缓存](/docs/microService/thinking/分布式缓存.md)  
[分布式限流](/docs/microService/thinking/分布式限流.md)   
[服务降级](/docs/microService/thinking/服务降级.md)  
[分布式ID](/docs/microService/thinking/分布式ID.md)  

<!-- 
[分布式Session](/docs/microService/thinking/分布式Session.md)  
-->

### 分布式通信  
[序列化](/docs/microService/communication/serializbale.md)  
[NIO](/docs/microService/communication/NIO.md)  
&emsp; [NIO Channel](/docs/microService/communication/NIO/Channel.md)  
&emsp; [NIO Buffer](/docs/microService/communication/NIO/Buffer.md)  
&emsp; [NIO Selector](/docs/microService/communication/NIO/Selector.md)  
[Netty]  

## Elaticsearch  
[ES基本概念](/docs/ES/1.basicConcepts.md)  
[ES集群](/docs/ES/3.colony.md)  
[ES使用教程](/docs/ES/2.useTutorials.md)  
[ES搭建](/docs/ES/7.build.md)  
[ES原理](/docs/ES/4.principle.md)  
[ES优化-1](/docs/ES/5.optimization.md)  
[ELK与EFK](/docs/ES/6.ELK与EFK.md)    

---
## 项目构建基础    
[接口幂等](/docs/web/idempotent.md)   
[日志系统](/docs/web/log.md)   
[Http重试](/docs/web/httpRetry.md)   
[生成二维码](/docs/web/QRCode.md)  
[反爬虫](/docs/web/reptile.md)  
[JavaBean](/docs/web/JavaBean.md)  
[加密算法](/docs/web/encryption.md)  
[参数校验](/docs/web/Validation.md)
[源码安全](/docs/web/codeSecurity.md)  
[其他](/docs/web/other.md)

---
## Error  
[多线程串线了](/docs/Error/ThreadCrossed.md)  
[接口响应时间](/docs/Error/responseTime.md)  
[熔断降级处理](/docs/Error/hystrix.md)  

---
## 服务器  
### Linux  
[Linux命令]  

### Nginx  
[Nginx](/docs/Linux/Nginx/1.nginx.md)  
[Nginx使用](/docs/Linux/Nginx/2.nginx使用场景.md)   
[Nginx运维](/docs/Linux/Nginx/3.nginx运维.md)  


## 计算机网络  
[OSI七层网络模型](/docs/network/0.OSI.md)  
[HTTP](/docs/network/1.HTTP.md)  
[HTTPS](/docs/network/2.HTTPS.md)  
[DNS](docs/network/3.DNS.md)  
[TCP](/docs/network/4.TCP.md)  



---
## 算法  
[算法基本概念](/docs/java/function/1.notion.md)  
[基本数据结构]  
&emsp; [链表](/docs/java/function/structure/array.md)  
&emsp; [栈](/docs/java/function/structure/stack.md)  
&emsp; [哈希](/docs/java/function/structure/hash.md)  
&emsp; [树](/docs/java/function/structure/tree.md)  
[其他数据结构](/docs/java/function/3.otherStructure.md)  
[排序算法](/docs/java/function/4.sort.md)  
&emsp; [比较排序](/docs/java/function/sort/compare.md)  
&emsp; [非比较排序](/docs/java/function/sort/noCompare.md)  
[查找算法](/docs/java/function/5.search.md)  
[算法思想题型](/docs/java/function/6.algorithmicIdea.md)  


---

## 项目管理  
[git](/docs/projectManage/git/command.md)   

## 开发软件  
[idea](/docs/software/idea/idea.md)  

## 其他工具  
[tools](/docs/software/tools/tools.md)  


----

[部署](/docs/DevOps/deploy.md)  
[监控](/docs/DevOps/monitor.md)  

 
