
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
    - [Elaticsearch-1](#elaticsearch-1)
    - [服务器](#服务器)
        - [Linux](#linux)
        - [Nginx](#nginx)
    - [计算机网络](#计算机网络)
    - [项目构建基础](#项目构建基础)
    - [项目管理](#项目管理)
        - [Git](#git)
    - [开发软件](#开发软件)
    - [Error](#error)
    - [算法](#算法)

<!-- /TOC -->


# 目录  

欢迎关注个人博客：***<font color = "red">王猴卖瓜</font>***  
联系我：***wangtao1814@163.com***  

## 杂记  
<font color="red">多问几个为什么，怎么办，怎么做。</font>  
[我自己的学习之路]  

## Java  
[Java基础](java/basis/Java基础.md)  
&emsp; [符号](java/basis/mark.md)  
[Java基础数据类型](java/basis/数据类型.md)  
[Java集合框架](java/Collection/1.集合框架.md)  
&emsp; [Map](java/Collection/2.Map.md)  
&emsp; [Collection](java/Collection/3.Collection.md)  

[Java异常](java/basis/JavaException.md)  
[Java范型](java/basis/JavaParadigm.md)  
[Java反射](java/basis/Java反射.md)  
[IO](java/basis/JavaIO.md)  
[JDK1.8](java/JDK8/JDK8.md)  
&emsp; [Lambda](java/JDK8/Lambda.md)  
&emsp; [Stream](java/JDK8/Stream.md)  
&emsp; [Optional](java/JDK8/Optional.md)  
&emsp; [DateTime](java/JDK8/DateTime.md)  

### 并发编程  
[线程基本概念](java/concurrent/1.Thread.md)  
[多线程编程](java/concurrent/2.Multithread.md)  
[线程池](java/concurrent/3.ThreadPool.md)  
[J.U.C包](java/concurrent/4.ConcurrentPackage.md)  
&emsp; [Lock](java/concurrent/8.Lock.md)  
&emsp; [Atmoic](java/concurrent/9.Atmoic.md)  
&emsp; &emsp; [AtomicStampedReference](java/concurrent/6.AtomicStampedReference.md)  
&emsp; [Collections](java/concurrent/10.Collections.md)  
&emsp; &emsp; [ConcurrentHashMap](java/concurrent/5.ConcurrentHashMap.md)  
&emsp; [tools](java/concurrent/7.tools.md)

### JVM  
[JDK、JRE、JVM](java/JVM/1.JDK、JRE、JVM三者间的关系.md)  

[JVM类的加载](java/JVM/2.JVM类的加载.md)  
[JVM内存结构](java/JVM/3.JVM内存结构.md)  

[GC垃圾回收](java/JVM/5.GC垃圾回收.md)  
[JVM调优-基础](java/JVM/6.JVM调优-基础.md)  
[JVM调优](java/JVM/7.JVM调优.md)  

### 设计模式  
[七大设计原则](java/Design/1.principles.md)  
[Java设计模式](java/Design/2.design.md)  
[创建型设计模式](java/Design/3.establish.md)  
[结构型设计模式](java/Design/4.structure.md)  
&emsp; [动态代理](java/Design/6.proxy.md)   
[行为型设计模式](java/Design/5.behavior.md)  


## 数据库  
[SQL语句](/SQL/1.SQL语句.md)  
&emsp; [基本查询语句](/SQL/9.basicSelect.md)  
&emsp; [连接查询](/SQL/10.joinSelect.md)  
&emsp; [行列转换](/SQL/11.trans.md)  
[SQL优化-1](/SQL/2.SQL优化.md)  
&emsp; [索引优化](/SQL/7.index.md)  
&emsp; [优化案例](/SQL/12.case.md)  
[MySql原理]  
&emsp; [MySql存储引擎](/SQL/13.MySqlStorage.md)  
&emsp; [MySql事务](/SQL/14.transaction.md)  
&emsp; [MySql锁](/SQL/15.lock.md)  
[MySql架构](/SQL/8.MySql架构.md)  
[分布式数据库](/SQL/4.分布式数据库.md)  
&emsp; [主从复制](/SQL/16.replication.md)  
&emsp; [分区](/SQL/17.partition.md)  
&emsp; [分库分表-1](/SQL/18.sub.md)  
[MyCat中间件](/SQL/5.MyCat中间件.md)  
[AOP多数据源动态切换](/SQL/6.multiDataSource.md)  


## SSM  
### Spring  
[Spring-1](SSM/Spring/1.Spring.md)  
[SpringIOC解析](SSM/Spring/2.SpringIOC.md)  
&emsp; [SpringIOC解析](SSM/Spring/2.SpringIOC.md)  
&emsp; [容器初始化详解](SSM/Spring/容器初始化详解.md)  
[SpringDI解析](SSM/Spring/3.SpringDI.md)  
[SpringBean生命周期](SSM/Spring/SpringBean.md)  
[容器相关特性]  
&emsp; [FactoryBean](SSM/Spring/feature/FactoryBean.md)  
&emsp; [循环依赖](SSM/Spring/feature/循环依赖.md)  
&emsp; [可二次开发常用接口](SSM/Spring/feature/可二次开发常用接口.md)  
&emsp; [lazy-init](SSM/Spring/feature/lazy-init.md)  

[SpringAOP教程](SSM/Spring/5.SpringAOP.md)  
[SpringAOP解析](SSM/Spring/6.SpringAOP解析.md)  
[Spring事务](SSM/Spring/7.SpringTransaction.md)  

[SpringMVC使用教程](SSM/Spring/9.SpringMVCUse.md)  
[SpringMVC解析](SSM/Spring/10.SpringMVCAnalysis.md)    
[过滤器、拦截器、监听器](web/subassembly.md)  

### MyBatis  
[MyBatis教程](SSM/MyBatis/Mybatis.md)  

[MyBatis SQL执行解析](SSM/MyBatis/MybatisExecutor.md)  
[MyBatis缓存](SSM/MyBatis/MybatisCache.md)  
[MyBatis插件解析-1](SSM/MyBatis/MybatisPlugins.md)  
[Spring和MyBatis整合-1](SSM/MyBatis/SpringMybatis.md)  
[MyBatis中的设计模式](SSM/MyBatis/MybatisDesign.md)  


## 微服务  
### SpringBoot  
[SpringBoot](/microService/SpringBoot/SpringBoot.md)  

[SpringBoot自动配置原理](/microService/SpringBoot/1.自动配置原理.md)  
[SpringBoot启动过程-SpringApplication初始化](/microService/SpringBoot/SpringApplication初始化.md)  
[SpringBoot启动过程-run()方法运行过程](/microService/SpringBoot/run方法运行过程.md)  
[SpringBoot事件监听机制](/microService/SpringBoot/3.SpringBootEvent.md)  
[SpringBoot内置生命周期事件详解](/microService/SpringBoot/4.SpringBootEvent.md)  
<!--[自定义strater] -->

### Spring Cloud Netflix  
参考《SpringCloud微服务实战》  
[Spring Cloud Netflix](/microService/SpringCloudNetflix/0.Netflix.md)  
[Spring Cloud Eureka](/microService/SpringCloudNetflix/1.Eureka.md)  
[Spring Cloud Ribbon](/microService/SpringCloudNetflix/2.Ribbon.md)  
[Spring Cloud Hytrix-1](/microService/SpringCloudNetflix/3.Hytrix.md)  
[Spring Cloud Feign](/microService/SpringCloudNetflix/4.Feign.md)  
[Spring Cloud Zuul](/microService/SpringCloudNetflix/5.Zuul.md)  
[Spring Cloud Sleuth](/microService/SpringCloudNetflix/6.Sleuth.md)  
[Spring Boot Admin](/microService/SpringCloudNetflix/7.SpringBootAdmin.md)  

### Dubbo  
[Dubbo](/microService/Dubbo/Dubbo.md)   
[Dubbo使用教程](/microService/Dubbo/Dubbo使用教程.md)  
[Zookeeper](/microService/Dubbo/Zookeeper.md)  

### Redis
[Redis基本数据类型](/microService/Redis/Redis数据结构及API.md)  
&emsp; [Redis高级数据类型](/microService/Redis/Redis高级数据类型.md)  
&emsp; [Redis的API](/microService/Redis/RedisAPI.md)  
[Redis原理](/microService/Redis/Redis原理.md)  
&emsp; [Redis持久化](/microService/Redis/Redis持久化.md)  
&emsp; [Redis淘汰策略](/microService/Redis/Redis淘汰.md)  
&emsp; [Redis事务](/microService/Redis/Redis事务.md)  
[Redis集群](/microService/Redis/Redis集群.md)  
&emsp; [Redis主从复制](/microService/Redis/Redis主从复制.md)  
&emsp; [Redis哨兵模式](/microService/Redis/Redis哨兵模式.md)  
&emsp; [Redis分片模式](/microService/Redis/Redis分片模式.md)  


### 分布式理论  
[一致性哈希算法](/microService/thinking/分布式算法-consistent.md)  
[分布式ID](/microService/thinking/分布式ID.md)  

[分布式事务](/microService/thinking/分布式事务.md)  
&emsp; [DTP](/microService/thinking/分布式事务-1.md)    
&emsp; [消息、事件模式](/microService/thinking/分布式事务-2.md)   
&emsp; [TCC模式](/microService/thinking/分布式事务-3.md)   
[分布式锁](/microService/thinking/分布式锁.md)  
&emsp; [Redis分布式锁](/microService/thinking/redis分布式锁.md)  
&emsp; [ZK分布式锁](/microService/thinking/ZK分布式锁.md)  

[分布式缓存](/microService/thinking/分布式缓存.md)  
[分布式限流](/microService/thinking/分布式限流.md)   
[服务降级](/microService/thinking/服务降级.md)  

### 分布式通信  
[序列化](/microService/communication/serializbale.md)    
[NIO](/microService/communication/NIO.md)  
&emsp; [NIO Channel](/microService/communication/NIO/Channel.md)  
&emsp; [NIO Buffer](/microService/communication/NIO/Buffer.md)  
&emsp; [NIO Selector](/microService/communication/NIO/Selector.md)  

## Elaticsearch-1  
[ES基本概念](ES/1.basicConcepts.md)  
[ES使用教程](ES/2.useTutorials.md)  
[ES集群](ES/3.colony.md)  
[ES原理](ES/4.principle.md)  
[ES优化-1](ES/5.optimization.md)  
[ELK](ES/6.ELK.md)    


## 服务器  
### Linux  
[Linux命令]  

### Nginx  
[Nginx](Linux/Nginx/1.nginx.md)  
[Nginx使用场景](Linux/Nginx/2.nginx使用场景.md)   


## 计算机网络  
[OSI七层网络模型](/network/0.OSI.md)  
[HTTP](/network/1.HTTP.md)  
[HTTPS](/network/2.HTTPS.md)  
[DNS](network/3.DNS.md)  
[TCP](/network/4.TCP.md)  


---
## 项目构建基础    
[接口幂等](web/idempotent.md)   

## 项目管理  
### Git  
[git命令](/projectManage/git/command.md)   

## 开发软件  
[idea](software/idea/idea.md)  


## Error  
[※※※多线程串线了](/Error/ThreadCrossed.md)  
[CodeCC]  

---
## 算法  
[算法基本概念](java/function/1.notion.md)  
[基本数据结构-1]  
&emsp; [链表](java/function/structure/array.md)  
&emsp; [栈](java/function/structure/stack.md)  
&emsp; [哈希](java/function/structure/hash.md)  
&emsp; [树](java/function/structure/tree.md)  
[其他数据结构](java/function/3.otherStructure.md)  
[排序算法](java/function/4.sort.md)  
&emsp; [比较排序](java/function/sort/compare.md)  
&emsp; [非比较排序](java/function/sort/noCompare.md)  
[查找算法](java/function/5.search.md)  
[算法思想题型](java/function/6.algorithmicIdea.md)  



