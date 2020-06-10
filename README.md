

<!-- TOC -->

- [目录](#目录)
    - [杂记](#杂记)
    - [Java](#java)
        - [集合框架](#集合框架)
        - [JVM](#jvm)
        - [并发编程](#并发编程)
        - [设计模式](#设计模式)
        - [算法](#算法)
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
    - [服务器](#服务器)
        - [Linux](#linux)
        - [Nginx](#nginx)
    - [计算机网络](#计算机网络)
    - [项目构建基础](#项目构建基础)

<!-- /TOC -->


# 目录  

***联系我：wangtao1814@163.com***  

## 杂记  
[我自己的学习之路]  

## Java  
[Java数据类型](java/数据类型.md)  
[Java基础](java/Java基础.md)  
[Java异常](java/JavaException.md)  
[Java范型](java/JavaParadigm.md)  
[Java反射](java/Java反射.md)  
[IO](java/JavaIO.md)  
[JDK1.8](java/JDK8.md)  

### 集合框架  
[集合框架](java/Collection/1.集合框架.md)  
[Map](java/Collection/2.Map.md)  
[Collection](java/Collection/3.Collection.md)  

### JVM  
[JDK、JRE、JVM三者间的关系](java/JVM/1.JDK、JRE、JVM三者间的关系.md)  
[JVM类的加载](java/JVM/2.JVM类的加载.md)  
[JVM内存结构](java/JVM/3.JVM内存结构.md)  

[GC垃圾回收](java/JVM/5.GC垃圾回收.md)  
[JVM调优-基础](java/JVM/6.JVM调优-基础.md)  
[JVM调优](java/JVM/7.JVM调优.md)  

### 并发编程  
[线程基本概念](java/concurrent/1.Thread.md)  
[多线程编程](java/concurrent/2.Multithread.md)  
[线程池](java/concurrent/3.ThreadPool.md)  
[J.U.C.包](java/concurrent/4.ConcurrentPackage.md)  

### 设计模式  
[七大设计原则](java/Design/1.principles.md)  
[Java设计模式](java/Design/2.design.md)    
[创建型设计模式](java/Design/3.establish.md)  
[结构型设计模式](java/Design/4.structure.md)  
[行为型设计模式](java/Design/5.behavior.md)  

### 算法  
[算法基本概念](java/function/1.notion.md)  
[基本数据结构-1](java/function/2.structure.md)  
[其他数据结构](java/function/3.otherStructure.md)  
[排序算法](java/function/4.sort.md)  
[查找算法](java/function/5.search.md)  
[算法思想题型](java/function/6.algorithmicIdea.md)  


## 数据库  
[SQL语句](SQL/1.SQL语句.md)  
[SQL优化-1](SQL/2.SQL优化.md)  
[MySql深入](SQL/3.MySql深入.md)  
[分布式数据库](SQL/4.分布式数据库.md)  
[MyCat中间件](SQL/5.MyCat中间件.md)  
[AOP多数据源动态切换](SQL/6.multiDataSource.md)  


## SSM  
### Spring  
[Spring](SSM/Spring/1.Spring.md)  
[SpringIOC解析](SSM/Spring/2.SpringIOC.md)  
[SpringDI解析](SSM/Spring/3.SpringDI.md)  
[容器相关特性](SSM/Spring/4.SpringFeature.md)  
[SpringAOP教程](SSM/Spring/5.SpringAOP.md)  
[SpringAOP解析](SSM/Spring/6.SpringAOP解析.md)  
[Spring事务](SSM/Spring/7.SpringTransaction.md)  
[SpringMVC使用教程](SSM/Spring/9.SpringMVCUse.md)  
[SpringMVC解析](SSM/Spring/10.SpringMVCAnalysis.md)    

### MyBatis  
[MyBatis教程](SSM/MyBatis/Mybatis.md)    

## 微服务  
### SpringBoot  
[自动配置原理](microService/SpringBoot/1.自动配置原理.md)  
[Spring Boot 2.2.0启动全过程解析](microService/SpringBoot/2.SpringBoot2.2.0启动全过程源码分析.md)  
[自定义strater]

### Spring Cloud Netflix  
[Spring Cloud Netflix](microService/SpringCloudNetflix/0.Netflix.md)  
[Spring Cloud Eureka](microService/SpringCloudNetflix/1.Eureka.md)  
[Spring Cloud Ribbon](microService/SpringCloudNetflix/2.Ribbon.md)  
[Spring Cloud Hytrix](microService/SpringCloudNetflix/3.Hytrix.md)  
[Spring Cloud Feign](microService/SpringCloudNetflix/4.Feign.md)  
[Spring Cloud Zuul](microService/SpringCloudNetflix/5.Zuul.md)  
[Spring Cloud Sleuth](microService/SpringCloudNetflix/6.Sleuth.md)  
[Spring Boot Admin](microService/SpringCloudNetflix/7.SpringBootAdmin.md)  

### Dubbo  
[Dubbo](microService/Dubbo/Dubbo.md)   
[Dubbo使用教程](microService/Dubbo/Dubbo使用教程.md)  
[Zookeeper](microService/Dubbo/Zookeeper.md)  

### Redis
[redis数据结构及API](microService/Redis/Redis数据结构及API.md)  
[redis原理](microService/Redis/Redis原理.md)  
[redis集群](microService/Redis/Redis集群.md)  

### 分布式理论  
[一致性哈希算法](microService/分布式算法-consistent.md)  
[分布式ID](microService/分布式ID.md)  
[分布式事务](microService/分布式事务.md)  
[分布式锁](microService/分布式锁.md)    

--------- 
[分布式缓存](microService/分布式缓存.md)  
[分布式限流](microService/分布式限流.md)   
[~~服务降级~~](microService/服务降级.md)  

### 分布式通信  
[NIO](microService/communication/NIO.md)  


## Elaticsearch  
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

[HTTP](/network/1.HTTP.md)  
[HTTPS](/network/2.HTTPS.md)  
[DNS](network/3.DNS.md)  
[TCP](/network/4.TCP.md)  


-----

## 项目构建基础    
[过滤器、拦截器、监听器](web/subassembly.md)  
[～～接口幂等～～](web/idempotent.md)   
[～～接口安全～～](web/security.md)  



