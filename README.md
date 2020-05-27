

<!-- TOC -->

- [1. 目录](#1-目录)
    - [1.1. Java](#11-java)
        - [1.1.1. 集合框架](#111-集合框架)
        - [1.1.2. JVM](#112-jvm)
        - [1.1.3. 并发编程](#113-并发编程)
        - [1.1.4. 设计模式](#114-设计模式)
        - [1.1.5. 算法](#115-算法)
    - [1.2. 数据库](#12-数据库)
    - [1.3. SSM](#13-ssm)
        - [1.3.1. Spring](#131-spring)
        - [1.3.2. MyBatis](#132-mybatis)
    - [1.4. 微服务](#14-微服务)
        - [1.4.1. SpringBoot](#141-springboot)
        - [1.4.2. Spring Cloud Netflix](#142-spring-cloud-netflix)
        - [1.4.3. Dubbo](#143-dubbo)
        - [1.4.4. Redis](#144-redis)
        - [1.4.5. 分布式理论](#145-分布式理论)
        - [1.4.6. 分布式通信](#146-分布式通信)
    - [1.5. web](#15-web)
    - [1.6. 框架](#16-框架)
    - [1.7. linux服务器](#17-linux服务器)
        - [1.7.1. Linux](#171-linux)
        - [1.7.2. Nginx](#172-nginx)
    - [1.8. 计算机网络](#18-计算机网络)

<!-- /TOC -->


# 1. 目录  

***联系我：wangtao1814@163.com***  


## 1.1. Java  
[Java数据类型](java/数据类型.md)  
[Java基础](java/Java基础.md)  
[Java反射](java/Java反射.md)  
[IO](java/JavaIO.md)  
[JDK1.8](java/JDK8.md)  

### 1.1.1. 集合框架  
[集合框架](java/Collection/1.集合框架.md)  
[Map](java/Collection/2.Map.md)  
[Collection](java/Collection/3.Collection.md)  

### 1.1.2. JVM  
[JDK、JRE、JVM三者间的关系](java/JVM/1.JDK、JRE、JVM三者间的关系.md)  
[JVM类的加载](java/JVM/2.JVM类的加载.md)  
[JVM内存结构](java/JVM/3.JVM内存结构.md)  
[JVM类的运行机制](java/JVM/4.JVM类的运行机制.md)  
[GC垃圾回收](java/JVM/5.GC垃圾回收.md)  
[JVM调优-基础](java/JVM/6.JVM调优-基础.md)  
[JVM调优](java/JVM/7.JVM调优.md)  

### 1.1.3. 并发编程  
[线程基本概念](java/concurrent/1.Thread.md)  
[多线程编程](java/concurrent/2.Multithread.md)  
[线程池](java/concurrent/3.ThreadPool.md)  
[J.U.C.包](java/concurrent/4.ConcurrentPackage.md)  

### 1.1.4. 设计模式  
[七大设计原则](java/Design/1.principles.md)  
[Java设计模式](java/Design/2.design.md)    
[创建型设计模式](java/Design/3.establish.md)  
[结构型设计模式](java/Design/4.structure.md)  
[行为型设计模式](java/Design/5.behavior.md)  

### 1.1.5. 算法  
[算法基本概念](java/function/1.notion.md)  
[~~基本数据结构~~](java/function/2.structure.md)  
[其他数据结构](java/function/3.otherStructure.md)  
[排序算法](java/function/4.sort.md)  
[查找算法](java/function/5.search.md)  
[算法思想题型](java/function/6.algorithmicIdea.md)  


## 1.2. 数据库  
[SQL语句](SQL/1.SQL语句.md)  
[SQL优化](SQL/2.SQL优化.md)  
[MySql深入](SQL/3.MySql深入.md)  
[~~分布式数据库~~](SQL/4.分布式数据库.md)  
[~~MyCat中间件~~](SQL/5.MyCat中间件.md)  



## 1.3. SSM  
### 1.3.1. Spring  
[Spring](SSM/Spring/1.Spring.md)  
[SpringIOC解析](SSM/Spring/2.SpringIOC.md)  
[SpringDI解析](SSM/Spring/3.SpringDI.md)  
[~~容器相关特性~~](SSM/Spring/4.SpringFeature.md)  
[SpringAOP教程]    
[SpringAOP解析]  
[Spring事务](SSM/Spring/7.SpringTransaction.md)  
[Spring涉及的涉及模式]  
[SpringMVC使用教程](SSM/Spring/9.SpringMVCUse.md)  
[SpringMVC解析](SSM/Spring/10.SpringMVCAnalysis.md)    

### 1.3.2. MyBatis  
[MyBatis教程]  
[Mybatis原理]  


## 1.4. 微服务  
### 1.4.1. SpringBoot  
[自动配置原理](microService/SpringBoot/1.自动配置原理.md)  
[Spring Boot 2.2.0启动全过程解析](microService/SpringBoot/2.SpringBoot2.2.0启动全过程源码分析.md)  
[自定义strater]

### 1.4.2. Spring Cloud Netflix  
[Spring Cloud Netflix](microService/SpringCloudNetflix/0.Netflix.md)  
[Spring Cloud Eureka](microService/SpringCloudNetflix/1.Eureka.md)  
[Spring Cloud Ribbon](microService/SpringCloudNetflix/2.Ribbon.md)  
[Spring Cloud Hytrix](microService/SpringCloudNetflix/3.Hytrix.md)  
[Spring Cloud Feign](microService/SpringCloudNetflix/4.Feign.md)  
[Spring Cloud Zuul](microService/SpringCloudNetflix/5.Zuul.md)  
[Spring Cloud Sleuth](microService/SpringCloudNetflix/6.Sleuth.md)  
[Spring Boot Admin](microService/SpringCloudNetflix/7.SpringBootAdmin.md)  

### 1.4.3. Dubbo  
[Dubbo](microService/Dubbo/Dubbo.md)   
[Dubbo使用教程](microService/Dubbo/Dubbo使用教程.md)  
[Zookeeper](microService/Dubbo/Zookeeper.md)  

### 1.4.4. Redis
[redis数据结构及API](microService/Redis/Redis数据结构及API.md)  
[redis原理](microService/Redis/Redis原理.md)  
[redis集群](microService/Redis/Redis集群.md)  

### 1.4.5. 分布式理论  
[一致性哈希算法](microService/分布式算法-consistent.md)  
[分布式事务](microService/分布式事务.md)  
[分布式锁](microService/分布式锁.md)  
[分布式缓存](microService/分布式缓存.md)  
[分布式ID](microService/分布式ID.md)  
[分布式限流](microService/分布式限流.md)   

### 1.4.6. 分布式通信  



## 1.5. web  
[过滤器、拦截器、监听器](web/subassembly.md)   



## 1.6. 框架  
[ElasticSearch]  



## 1.7. linux服务器  
### 1.7.1. Linux  
[Linux命令]  

### 1.7.2. Nginx  
[Nginx](Linux/Nginx/1.nginx.md)  
[Nginx使用场景](Linux/Nginx/2.nginx使用场景.md)   
[Nginx配置参数](Linux/Nginx/3.nginx配置参数.md)     



## 1.8. 计算机网络  

[HTTP](/network/1.HTTP.md)  
[HTTPS](/network/2.HTTPS.md)  
[DNS](network/3.DNS.md)  
[TCP](/network/4.TCP.md)  


