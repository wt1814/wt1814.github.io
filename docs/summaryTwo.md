
<!-- TOC -->

- [1. 总结](#1-总结)
    - [1.1. 项目构建](#11-项目构建)
        - [1.1.1. 接口幂等](#111-接口幂等)
        - [1.1.2. 接口响应时间](#112-接口响应时间)
        - [1.1.3. 接口预警](#113-接口预警)
    - [1.2. 架构设计](#12-架构设计)
        - [1.2.1. 架构质量属性](#121-架构质量属性)
        - [1.2.2. 系统瓶颈](#122-系统瓶颈)
    - [1.3. Spring](#13-spring)
        - [1.3.1. Spring基础](#131-spring基础)
        - [1.3.2. Spring IOC](#132-spring-ioc)
        - [1.3.3. Spring DI](#133-spring-di)
            - [1.3.3.1. Bean的生命周期](#1331-bean的生命周期)
            - [1.3.3.2. Spring DI中循环依赖](#1332-spring-di中循环依赖)
        - [1.3.4. IOC容器扩展](#134-ioc容器扩展)
            - [1.3.4.2. Spring可二次开发常用接口（扩展性）](#1342-spring可二次开发常用接口扩展性)
                - [1.3.4.1. FactoryBean](#1341-factorybean)
                - [1.3.4.2.1. 事件](#13421-事件)
                - [1.3.4.2.2. Aware接口](#13422-aware接口)
                - [1.3.4.2.3. 后置处理器](#13423-后置处理器)
                - [1.3.4.2.4. InitializingBean](#13424-initializingbean)
        - [1.3.5. SpringAOP教程](#135-springaop教程)
        - [1.3.6. SpringAOP解析](#136-springaop解析)
        - [1.3.7. Spring事务](#137-spring事务)
            - [1.3.7.1. Spring事务使用](#1371-spring事务使用)
            - [1.3.7.2. Spring事务问题](#1372-spring事务问题)
        - [1.3.8. SpringMVC解析](#138-springmvc解析)
        - [1.3.9. 过滤器、拦截器、监听器](#139-过滤器拦截器监听器)
    - [1.4. MyBatis](#14-mybatis)
        - [1.4.1. MyBatis大数据量查询](#141-mybatis大数据量查询)
        - [1.4.2. MyBatis架构](#142-mybatis架构)
        - [1.4.3. MyBatis SQL执行解析](#143-mybatis-sql执行解析)
        - [SqlSession详解](#sqlsession详解)
        - [1.4.4. Spring集成Mybatis](#144-spring集成mybatis)
        - [1.4.5. MyBatis缓存](#145-mybatis缓存)
        - [1.4.6. MyBatis插件解析](#146-mybatis插件解析)
    - [1.5. 分布微服务和集群](#15-分布微服务和集群)
    - [1.6. SpringBoot](#16-springboot)
        - [1.6.1. SpringBoot基础知识](#161-springboot基础知识)
        - [1.6.2. SpringBoot启动过程](#162-springboot启动过程)
            - [1.6.2.1. SpringApplication初始化](#1621-springapplication初始化)
            - [1.6.2.2. run()方法运行过程](#1622-run方法运行过程)
            - [1.6.2.3. SpringBoot事件监听机制](#1623-springboot事件监听机制)
                - [1.6.2.3.1. 事件监听步骤](#16231-事件监听步骤)
                - [1.6.2.3.2. 内置生命周期事件](#16232-内置生命周期事件)
            - [1.6.2.4. SpringBoot事件回调](#1624-springboot事件回调)
        - [1.6.3. SpringBoot自动配置（获取 ---> 注入 ---> 装配）](#163-springboot自动配置获取-----注入-----装配)
            - [1.6.3.1. 获取自动配置，注解@SpringBootApplication](#1631-获取自动配置注解springbootapplication)
            - [1.6.3.2. 加载自动配置流程](#1632-加载自动配置流程)
            - [1.6.3.3. 内置Tomcat](#1633-内置tomcat)
        - [1.6.4. 自定义strater](#164-自定义strater)
    - [1.7. SpringCloud](#17-springcloud)
        - [1.7.1. Eureka](#171-eureka)
        - [1.7.2. Ribbon](#172-ribbon)
        - [1.7.3. Feign](#173-feign)
        - [1.7.4. Zuul](#174-zuul)
        - [1.7.5. Hytrix](#175-hytrix)
        - [1.7.6. Sleuth](#176-sleuth)
        - [1.7.7. Admin](#177-admin)
    - [1.8. Dubbo](#18-dubbo)
        - [1.8.1. 分布式服务治理](#181-分布式服务治理)
            - [1.8.1.1. Dubbo和Spring Cloud](#1811-dubbo和spring-cloud)
            - [1.8.1.2. Spring Cloud Alibaba介绍](#1812-spring-cloud-alibaba介绍)
        - [1.8.2. RPC介绍](#182-rpc介绍)
        - [1.8.3. Dubbo介绍](#183-dubbo介绍)
        - [1.8.4. Dubbo框架设计](#184-dubbo框架设计)
        - [1.8.5. Dubbo初始化（Dubbo和Spring）](#185-dubbo初始化dubbo和spring)
        - [1.8.6. 暴露和引用服务（实际类 ---> invoker ---> ）](#186-暴露和引用服务实际类-----invoker-----)
            - [1.8.6.1. Dubbo序列化和协议](#1861-dubbo序列化和协议)
                - [1.8.6.1.1. Dubbo协议长连接和心跳](#18611-dubbo协议长连接和心跳)
                    - [1.8.6.1.1.1. 协议长链接](#186111-协议长链接)
            - [1.8.6.2. Dubbo心跳机制](#1862-dubbo心跳机制)
        - [1.8.7. 服务调用](#187-服务调用)
            - [1.8.7.1. 服务调用介绍](#1871-服务调用介绍)
            - [1.8.7.2. Dubbo集群容错](#1872-dubbo集群容错)
        - [1.8.8. ~~扩展点加载(SPI)~~](#188-扩展点加载spi)
        - [1.8.9. Dubbo和Netty](#189-dubbo和netty)
        - [Dubbo总结](#dubbo总结)
    - [1.9. Zookeeper](#19-zookeeper)
        - [1.9.1. ZK服务端](#191-zk服务端)
        - [1.9.2. ZK客户端](#192-zk客户端)
        - [1.9.3. ZK应用场景和弊端](#193-zk应用场景和弊端)
    - [1.10. 分布式](#110-分布式)
        - [1.10.1. 分布式理论CAP](#1101-分布式理论cap)
        - [1.10.2. 分布式ID](#1102-分布式id)
        - [1.10.3. 分布式事务](#1103-分布式事务)
            - [1.10.3.1. 事务模型DTP及XA](#11031-事务模型dtp及xa)
            - [1.10.3.2. TCC](#11032-tcc)
                - [1.10.3.2.1. TCC流程](#110321-tcc流程)
                - [1.10.3.2.2. TCC问题](#110322-tcc问题)
                - [1.10.3.2.3. TCC优化](#110323-tcc优化)
            - [1.10.3.3. Saga](#11033-saga)
            - [1.10.3.4. ~~消息模式，异步方式~~](#11034-消息模式异步方式)
            - [1.10.3.5. 分布式事务框架Seata](#11035-分布式事务框架seata)
                - [1.10.3.5.1. AT模式详解](#110351-at模式详解)
                    - [1.10.3.5.1.1. AT模式流程](#1103511-at模式流程)
                    - [1.10.3.5.1.2. AT缺点](#1103512-at缺点)
                - [1.10.3.5.2. Seata四种模式的区别](#110352-seata四种模式的区别)
            - [1.10.3.6. 分布式事务的选型](#11036-分布式事务的选型)
        - [1.10.4. 分布式锁](#1104-分布式锁)
            - [1.10.4.1. 分布式锁介绍](#11041-分布式锁介绍)
            - [1.10.4.2. RedisLock](#11042-redislock)
            - [1.10.4.3. 使用redis分布式锁的注意点](#11043-使用redis分布式锁的注意点)
            - [1.10.4.4. Redisson](#11044-redisson)
            - [1.10.4.5. ZK分布式锁](#11045-zk分布式锁)
            - [1.10.4.6. MySql分布式锁](#11046-mysql分布式锁)
            - [1.10.4.7. 分布式锁选型（各种锁的对比）](#11047-分布式锁选型各种锁的对比)
    - [1.11. 高并发](#111-高并发)
        - [1.11.1. 系统性能指标](#1111-系统性能指标)
        - [1.11.2. 并发系统三高](#1112-并发系统三高)
            - [1.11.2.1. 高可用建设](#11121-高可用建设)
            - [1.11.2.2. 秒杀系统设计](#11122-秒杀系统设计)
        - [1.11.3. 资源限制](#1113-资源限制)
    - [1.12. 缓存](#112-缓存)
        - [1.12.1. ~~分布式缓存问题~~](#1121-分布式缓存问题)
        - [1.12.2. 数据结构：bitMap、布隆、布谷鸟](#1122-数据结构bitmap布隆布谷鸟)
            - [1.12.2.1. bitMap](#11221-bitmap)
            - [1.12.2.2. 布隆](#11222-布隆)
            - [1.12.2.3. 计数布隆过滤器](#11223-计数布隆过滤器)
            - [1.12.2.4. 布谷鸟](#11224-布谷鸟)
            - [1.12.2.5. Redis热点key](#11225-redis热点key)
        - [1.12.3. Redis](#1123-redis)
            - [1.12.3.1. Redis数据类型](#11231-redis数据类型)
                - [1.12.3.1.1. Redis基本数据类型](#112311-redis基本数据类型)
                - [1.12.3.1.2. Redis扩展数据类型](#112312-redis扩展数据类型)
                - [1.12.3.1.3. redis使用：bigKey](#112313-redis使用bigkey)
                - [1.12.3.1.4. ~~Redis底层实现~~](#112314-redis底层实现)
                    - [1.12.3.1.4.1. 数据结构](#1123141-数据结构)
                    - [1.12.3.1.4.2. SDS详解](#1123142-sds详解)
                    - [1.12.3.1.4.3. Dictht](#1123143-dictht)
                    - [1.12.3.1.4.4. 数据类型](#1123144-数据类型)
            - [1.12.3.2. Redis原理](#11232-redis原理)
                - [1.12.3.2.1. Redis为什么那么快？](#112321-redis为什么那么快)
                - [1.12.3.2.2. Redis【虚拟内存】机制](#112322-redis虚拟内存机制)
                - [1.12.3.2.3. Redis事件/Reactor](#112323-redis事件reactor)
                - [1.12.3.2.4. Redis多线程模型](#112324-redis多线程模型)
                - [1.12.3.2.5. Redis协议](#112325-redis协议)
                - [1.12.3.2.6. Redis内存操作](#112326-redis内存操作)
                    - [1.12.3.2.6.1. Redis过期键删除](#1123261-redis过期键删除)
                    - [1.12.3.2.6.2. Redis内存淘汰](#1123262-redis内存淘汰)
                - [1.12.3.2.7. Redis持久化，磁盘操作](#112327-redis持久化磁盘操作)
                    - [1.12.3.2.7.1. ~~AOF重写阻塞~~](#1123271-aof重写阻塞)
            - [1.12.3.3. Redis内置功能](#11233-redis内置功能)
                - [1.12.3.3.1. RedisPipeline/批处理](#112331-redispipeline批处理)
                - [1.12.3.3.2. Redis事务](#112332-redis事务)
                - [1.12.3.3.3. Redis和Lua](#112333-redis和lua)
                - [1.12.3.3.4. Redis实现消息队列](#112334-redis实现消息队列)
            - [1.12.3.4. Redis高可用](#11234-redis高可用)
                - [1.12.3.4.1. Redis高可用方案](#112341-redis高可用方案)
                - [1.12.3.4.2. Redis主从复制](#112342-redis主从复制)
                - [1.12.3.4.3. Redis读写分离](#112343-redis读写分离)
                - [1.12.3.4.4. Redis哨兵模式](#112344-redis哨兵模式)
                - [1.12.3.4.5. Redis集群模式](#112345-redis集群模式)
            - [1.12.3.5. Redis常见问题与优化](#11235-redis常见问题与优化)
        - [1.12.4. 分布式限流](#1124-分布式限流)
        - [1.12.5. 服务降级](#1125-服务降级)
    - [1.13. 分布式消息队列](#113-分布式消息队列)
        - [1.13.1. mq](#1131-mq)
        - [1.13.2. Kafka](#1132-kafka)
            - [1.13.2.1. kafka基本概念](#11321-kafka基本概念)
                - [1.13.2.1.1. kafka生产者](#113211-kafka生产者)
                - [1.13.2.1.2. 消息分区](#113212-消息分区)
                - [1.13.2.1.3. kafka消费者](#113213-kafka消费者)
                - [1.13.2.1.4. kafka服务端](#113214-kafka服务端)
            - [1.13.2.2. kafka特性](#11322-kafka特性)
                - [1.13.2.2.1. 【1】高性能(读写机制) ，Kafka为什么吞吐量大、速度快？](#113221-1高性能读写机制-kafka为什么吞吐量大速度快)
                    - [1.13.2.2.1.1. 内存](#1132211-内存)
                    - [1.13.2.2.1.2. 持久化 / 磁盘IO - 顺序读写](#1132212-持久化--磁盘io---顺序读写)
                    - [1.13.2.2.1.3. 网络IO优化](#1132213-网络io优化)
                - [1.13.2.2.2. 【2】高可用与数据一致性(副本机制)](#113222-2高可用与数据一致性副本机制)
                - [1.13.2.2.3. ~~可靠性~~](#113223-可靠性)
                    - [1.13.2.2.3.1. 如何保证消息队列不丢失?](#1132231-如何保证消息队列不丢失)
                    - [1.13.2.2.3.2. 分区保证消费顺序](#1132232-分区保证消费顺序)
                    - [1.13.2.2.3.3. 消费语义介绍](#1132233-消费语义介绍)
                    - [1.13.2.2.3.4. 幂等（重复消费）](#1132234-幂等重复消费)
                    - [1.13.2.2.3.5. 事务](#1132235-事务)
    - [1.14. 网络IO](#114-网络io)
        - [1.14.1. ~~服务器处理连接~~](#1141-服务器处理连接)
        - [1.14.2. 通信基础](#1142-通信基础)
        - [1.14.3. 网络IO](#1143-网络io)
            - [1.14.3.1. 五种I/O模型](#11431-五种io模型)
            - [1.14.3.2. I/O多路复用详解](#11432-io多路复用详解)
            - [1.14.3.3. 多路复用之Reactor模式](#11433-多路复用之reactor模式)
            - [1.14.3.4. IO性能优化之零拷贝](#11434-io性能优化之零拷贝)
        - [1.14.4. Socket编程](#1144-socket编程)
        - [1.14.5. NIO](#1145-nio)
        - [1.14.6. Netty](#1146-netty)
            - [1.14.6.1. Netty简介](#11461-netty简介)
            - [1.14.6.2. Netty运行流程](#11462-netty运行流程)
            - [1.14.6.3. Netty核心组件](#11463-netty核心组件)
            - [1.14.6.4. Netty逻辑架构](#11464-netty逻辑架构)
            - [1.14.6.5. Netty高性能](#11465-netty高性能)
                - [1.14.6.5.1. Netty的Reactor线程模型](#114651-netty的reactor线程模型)
            - [1.14.6.6. Netty开发](#11466-netty开发)
                - [1.14.6.6.1. Netty应用场景，](#114661-netty应用场景)
                - [1.14.6.6.2. TCP粘拆包与Netty编解码](#114662-tcp粘拆包与netty编解码)
                - [1.14.6.6.3. Netty实战](#114663-netty实战)
                - [1.14.6.6.4. Netty多协议开发](#114664-netty多协议开发)
            - [1.14.6.7. Netty源码](#11467-netty源码)
    - [1.15. WebSocket](#115-websocket)
        - [1.15.1. 4种Web端即时通信](#1151-4种web端即时通信)
        - [1.15.2. 配置中心使用长轮询推送](#1152-配置中心使用长轮询推送)
        - [1.15.3. WebSocket](#1153-websocket)
    - [1.16. 磁盘IO](#116-磁盘io)
    - [1.17. 内存优化](#117-内存优化)
    - [1.18. 计算机网络](#118-计算机网络)
        - [1.18.1. OSI七层网络模型](#1181-osi七层网络模型)
        - [1.18.2. 应用层](#1182-应用层)
            - [1.18.2.1. DNS](#11821-dns)
            - [1.18.2.2. HTTP](#11822-http)
            - [1.18.2.3. HTTPS](#11823-https)
        - [1.18.3. 传输层](#1183-传输层)
            - [1.18.3.1. TCP](#11831-tcp)
                - [1.18.3.1.1. 连接建立阶段](#118311-连接建立阶段)
                    - [1.18.3.1.1.1. 连接建立](#1183111-连接建立)
                    - [1.18.3.1.1.2. ★★★Http长短链接](#1183112-★★★http长短链接)
                    - [1.18.3.1.1.3. TCP粘包](#1183113-tcp粘包)
                - [1.18.3.1.2. 数据传输阶段](#118312-数据传输阶段)
        - [1.18.4. 网络的性能指标](#1184-网络的性能指标)
    - [1.19. 负载均衡](#119-负载均衡)
        - [1.19.1. 负载均衡解决方案](#1191-负载均衡解决方案)
        - [1.19.2. Nginx](#1192-nginx)
            - [1.19.2.1. Nginx介绍](#11921-nginx介绍)
            - [1.19.2.2. Nginx使用](#11922-nginx使用)
    - [1.20. Devops](#120-devops)
        - [1.20.1. CI/CD](#1201-cicd)
        - [1.20.2. DevOps](#1202-devops)
        - [1.20.3. 从上往下学Docker](#1203-从上往下学docker)
            - [1.20.3.1. Docker使用教程](#12031-docker使用教程)
            - [1.20.3.2. 镜像详解](#12032-镜像详解)
            - [1.20.3.3. 容器详解](#12033-容器详解)
        - [1.20.4. Kubernetes](#1204-kubernetes)
            - [1.20.4.1. k8s架构](#12041-k8s架构)
    - [1.21. 数据结构和算法](#121-数据结构和算法)

<!-- /TOC -->

# 1. 总结  
## 1.1. 项目构建
### 1.1.1. 接口幂等
&emsp; 接口幂等常用解决方案：  
* 分布式锁
* DB锁
    * 1).select+insert，insert前先select，该方案可能不适用于并发场景，在并发场景中，要配合其他方案一起使用，否则同样会产生重复数据 
    * 2). 状态机
    * 3). 乐观锁（新增version字段）  

&emsp; **<font color = "blue">小结：</font>**  
&emsp; 一般场景直接使用redis分布式锁解决。可是redis分布式锁可能因编码、部署等，出现一些问题。    
&emsp; 对数据要求高的场景，使用分布式锁 + db锁，db锁一般采用状态机幂等。  
&emsp; 对于将商品数量放在redis中，扣减库存采用lua脚本，支付时反查订单系统，防止超卖问题。    

### 1.1.2. 接口响应时间
1. `链路追踪，`查询耗时情况。  
2. 接口的响应时间过长，你会怎么办？（此处只针对最简单的场景，抛开STW那些复杂的问题。）以下是我目前想到的：  
    1. 异步化（Runnable、Future）  
    2. 缓存  
    3. 并行（ForkJoinPool、CyclicBarrier）  
    4. 干掉锁（空间换时间）  

### 1.1.3. 接口预警
1. 应用主动发送：
    1. 钉钉应用开放平台
    2. logback添加error预警
2. 中间件：  
    1. Filebeat+Logstash发送Email告警日志

## 1.2. 架构设计
&emsp; ~~架构的方方面面： 1).架构模式、2).性能/系统瓶颈。~~   

&emsp; 功能、流程、性能。   
&emsp; 分布式（数据一致）、高并发（高性能、高可用、高扩展）  

1. 功能、流程
2. 性能/系统瓶颈：CPU、磁盘IO、内存、网络IO


&emsp; 应用型框架，Spring、Mybatis  
&emsp; 功能型框架，redis、mq，一般都是分布式、高并发系统。  

* 分布式带来的问题：  
* 高并发系统的三高：  


### 1.2.1. 架构质量属性
&emsp; 高性能、可靠性、可定制性、可扩展性...  
&emsp; 架构属性一般包括如下方面：性能，伸缩性，可用性，安全性，容错性，灾难恢复，可访问性，可运维，管理，灵活性，可扩展性，可维护性，国际化，本地化。还有法律法规，成本，人员等对上面架构属性的影响。 

--------

1. 架构自身：
2. 架构延伸：
3. 架构对外：


### 1.2.2. 系统瓶颈
1. 网络I/O
2. 磁盘I/O
3. 内存
4. CPU

## 1.3. Spring
### 1.3.1. Spring基础
1. **@Autowired和@Resource之间的区别：**  
    1. @Autowired默认是按照类型装配注入的，默认情况下它要求依赖对象必须存在（可以设置它的required属性为false）。
    2. @Resource默认是按照名称来装配注入的，只有当找不到与名称匹配的bean才会按照类型来装配注入。  

### 1.3.2. Spring IOC
1. Spring容器就是个Map映射, IOC底层就是反射机制，AOP底层是动态代理。  
&emsp; Spring中的IoC的实现原理就是工厂模式加反射机制。  
1. BeanFactory与ApplicationContext
    * BeanFactory作为最顶层的一个接口类，定义了IOC容器的基本功能规范。
    * <font color = "clime">ApplicationContext接口是BeanFactory的扩展，它除了具备BeanFactory接口所拥有的全部功能外，还有应用程序上下文的一层含义</font>，主要包括：  
        1. 继承自ListableBeanFactory接口，<font color = "clime">可以访问Bean工厂上下文的组件；</font>  
        2. 继承自ResourceLoader接口，以通用的方式加载文件资源；  
        3. 继承自ApplicationContextPublisher接口，<font color = "clime">拥有发布事件注册监听的能力；</font>  
        4. 继承自 MessageSource 接口，解析消息支持国际化。  
2. BeanDefinition： **<font color = "red">BeanDefinition中保存了Bean信息，比如这个Bean指向的是哪个类、是否是单例的、是否懒加载、这个Bean依赖了哪些Bean等。</font>**  
3. Spring bean容器刷新的核心，12个步骤完成IoC容器的创建及初始化工作：  
    
    **<font color = "blue">（⚠`利用工厂和反射创建Bean。主要包含3部分：1).容器本身--创建容器、2).容器扩展--预处理、后置处理器、3).事件，子容器，★★★实例化Bean。`）</font>**     
    1. 刷新前的准备工作。  
    2. **<font color = "red">创建IoC容器(DefaultListableBeanFactory)，加载和注册BeanDefinition对象。</font>** <font color = "blue">`个人理解：此处仅仅相当于创建Spring Bean的类，实例化是在Spring DI里。`</font>   
        &emsp; **<font color = "clime">DefaultListableBeanFactory中使用一个HashMap的集合对象存放IOC容器中注册解析的BeanDefinition。</font>**  
        ```java
        private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
        ```
    -----------
    3. **<font color = "red">对IoC容器进行一些预处理。</font>** 为BeanFactory配置容器特性，`例如设置BeanFactory的类加载器，`配置了BeanPostProcessor，注册了三个默认bean实例，分别是“environment”、“systemProperties”、“systemEnvironment”。  
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


### 1.3.3. Spring DI
1. 加载时机：SpringBean默认单例，非懒加载，即容器启动时就加载。  
2. 加载流程：  
    1. doCreateBean()创建Bean有三个关键步骤：2.createBeanInstance()实例化、5.populateBean()属性填充、6.initializeBean()初始化。  

#### 1.3.3.1. Bean的生命周期
![image](http://182.92.69.8:8081/img/SSM/Spring/spring-10.png)  
&emsp; SpringBean的生命周期主要有4个阶段：  
1. 实例化（Instantiation），可以理解为new一个对象；
2. 属性赋值（Populate），可以理解为调用setter方法完成属性注入；
3. 初始化（Initialization），包含：  
    * 激活Aware方法  
    * 【前置处理】  
    * 激活自定义的init方法 
    * 【后置处理】 
4. 销毁（Destruction）---注册Destruction回调函数。  

------
&emsp; Spring Bean的生命周期管理的基本思路是：在Bean出现之前，先准备操作Bean的BeanFactory，然后操作完Bean，所有的Bean也还会交给BeanFactory进行管理。再所有Bean操作准备BeanPostProcessor作为回调。  
![image](http://182.92.69.8:8081/img/SSM/Spring/spring-23.png)  

#### 1.3.3.2. Spring DI中循环依赖
1. Spring循环依赖的场景：均采用setter方法（属性注入）注入方式，可被解决；采用构造器和setter方法（属性注入）混合注入方式可能被解决。
2. **<font color = "red">Spring通过3级缓存解决：</font>**  
    ![image](http://182.92.69.8:8081/img/SSM/Spring/spring-20.png)  
    * 三级缓存: Map<String,ObjectFactory<?>> singletonFactories，早期曝光对象工厂，用于保存bean创建工厂，以便于后面扩展有机会创建代理对象。  
    * 二级缓存: Map<String,Object> earlySingletonObjects， **<font color = "blue">早期曝光对象</font>** ，`二级缓存，用于存放已经被创建，但是尚未初始化完成的Bean。`尚未经历了完整的Spring Bean初始化生命周期。
    * 一级缓存: Map<String,Object> singletonObjects，单例对象池，用于保存实例化、注入、初始化完成的bean实例。经历了完整的Spring Bean初始化生命周期。  
3. 未发生依赖  
    ![image](http://182.92.69.8:8081/img/SSM/Spring/spring-21.png)  
4. **<font color = "clime">单例模式下Spring解决循环依赖的流程：</font>**  
    ![image](http://182.92.69.8:8081/img/SSM/Spring/spring-22.png)  
    ![image](http://182.92.69.8:8081/img/SSM/Spring/spring-17.png)  
    ![image](http://182.92.69.8:8081/img/SSM/Spring/spring-16.png)  
    1. Spring创建bean主要分为两个步骤，`创建原始bean对象`，`接着去填充对象属性和初始化`。  
    2. 每次创建bean之前，都会从缓存中查下有没有该bean，因为是单例，只能有一个。  
    3. `当创建beanA的原始对象后，并把它放到三级缓存中，`接下来就该填充对象属性了，这时候发现依赖了beanB，接着就又去创建 beanB，同样的流程，创建完beanB填充属性时又发现它依赖了beanA，又是同样的流程，不同的是，这时候可以在三级缓存中查到刚放进去的原始对象beanA，所以不需要继续创建，用它注入beanB，完成beanB的创建。`★★★此时会将beanA从三级缓存删除，放到二级缓存。`   
    4. 既然 beanB 创建好了，所以 beanA 就可以完成填充属性的步骤了，接着执行剩下的逻辑，闭环完成。  
    ---
    &emsp; 当A、B两个类发生循环引用时，在A完成实例化后，就使用实例化后的对象去创建一个对象工厂，并添加到三级缓存中。 **<font color = "blue">`如果A被AOP代理，那么通过这个工厂获取到的就是A代理后的对象，如果A没有被AOP代理，那么这个工厂获取到的就是A实例化的对象。`</font>** 当A进行属性注入时，会去创建B，同时B又依赖了A，所以创建B的同时又会去调用getBean(a)来获取需要的依赖，此时的getBean(a)会从缓存中获取：  

    * 第一步，先获取到三级缓存中的工厂。  
    * 第二步，调用对象工厂的getObject方法来获取到对应的对象，得到这个对象后将其注入到B中。紧接着B会走完它的生命周期流程，包括初始化、后置处理器等。  

    当B创建完后，会将B再注入到A中，此时A再完成它的整个生命周期。  
5. 常见问题
    1. 二级缓存能解决循环依赖嘛？  
    &emsp; 二级缓存可以解决循环依赖。如果创建的Bean有对应的代理，那其他对象注入时，注入的应该是对应的代理对象。  
    &emsp; 但是Spring无法提前知道这个对象是不是有循环依赖的情况，而正常情况下（没有循环依赖情况），Spring都是在创建好完成品Bean之后才创建对应的代理。这时候Spring有两个选择：

        * 方案一：不管有没有循环依赖，都提前创建好代理对象，并将代理对象放入缓存，出现循环依赖时，其他对象直接就可以取到代理对象并注入。
        * 方案二：不提前创建好代理对象，在出现循环依赖被其他对象注入时，才实时生成代理对象。这样在没有循环依赖的情况下，Bean就可以按着Spring设计原则的步骤来创建。  

    &emsp; `如果使用二级缓存解决循环依赖，即采用方案一，意味着所有Bean在实例化后就要完成AOP代理，这样违背了Spring设计的原则，`Spring在设计之初就是通过AnnotationAwareAspectJAutoProxyCreator这个后置处理器来在Bean生命周期的最后一步来完成AOP代理，而不是在实例化后就立马进行AOP代理。   
    &emsp; **怎么做到提前曝光对象而又不生成代理呢？**   
    &emsp; Spring就是在对象外面包一层ObjectFactory（三级缓存存放），提前曝光的是ObjectFactory对象，在被注入时才在ObjectFactory.getObject方式内实时生成代理对象，并将生成好的代理对象放入到第二级缓存Map\<String, Object> earlySingletonObjects。  

### 1.3.4. IOC容器扩展 
&emsp; Spring的扩展点有IOC容器扩展、AOP扩展...  

#### 1.3.4.2. Spring可二次开发常用接口（扩展性）
&emsp; Spring为了用户的开发方便和特性支持，开放了一些特殊接口和类，用户可进行实现或者继承，常见的有：  

&emsp; FactoryBean  

&emsp; **Spring IOC阶段：**  
&emsp; [事件](/docs/SSM/Spring/feature/Event.md)  

&emsp; **Spring DI阶段：**  
&emsp; [Aware接口](/docs/SSM/Spring/feature/Aware.md)  
&emsp; [后置处理器](/docs/SSM/Spring/feature/BeanFactoryPostProcessor.md)  
&emsp; [InitializingBean](/docs/SSM/Spring/feature/InitializingBean.md)  

##### 1.3.4.1. FactoryBean
1. BeanFactory  
&emsp; BeanFactory是个Factory，也就是IOC容器或对象工厂；FactoryBean是个Bean，也由BeanFactory管理。  
2. FactoryBean：`⚠️FactoryBean，工厂Bean，首先是个Bean，其次再加上工厂模式。`  
&emsp; 一般情况下，Spring通过`反射机制`利用\<bean\>的class属性指定实现类实例化Bean。 **<font color = "red">在某些情况下，实例化Bean过程比较复杂，</font>** 如果按照传统的方式，则需要在\<bean>中提供大量的配置信息。配置方式的灵活性是受限的，这时采用编码的方式可能会得到一个简单的方案。 **<font color = "red">Spring为此提供了一个org.springframework.bean.factory.FactoryBean的`工厂类接口，用户可以通过实现该接口定制实例化Bean的逻辑。`</font>**  
&emsp; **<font color = "red">FactoryBean接口的一些实现类，如Spring自身提供的ProxyFactoryBean、JndiObjectFactoryBean，还有Mybatis中的SqlSessionFactoryBean，</font>** 用于生产一些复杂的Bean。  


##### 1.3.4.2.1. 事件
&emsp; **<font color = "clime">★★★Spring事件机制的流程：</font>**   
1. **<font color = "clime">事件机制的核心是事件。</font>** Spring中的事件是ApplicationEvent。Spring提供了5个标准事件，此外还可以自定义事件（继承ApplicationEvent）。  
2. **<font color = "clime">确定事件后，要把事件发布出去。</font>** 在事件发布类的业务代码中调用ApplicationEventPublisher#publishEvent方法（或调用ApplicationEventPublisher的子类，例如调用ApplicationContext#publishEvent）。  
3. **<font color = "blue">`发布完成之后，启动监听器，自动监听。`</font>** 在监听器类中覆盖ApplicationListener#onApplicationEvent方法。  
4. 最后，就是实际场景中触发事件发布，完成一系列任务。  


&emsp; **<font color = "clime">5个标准事件：</font>**   

* 上下文更新事件（ContextRefreshedEvent）：在调用ConfigurableApplicationContext接口中的refresh()方法时被触发。  
* 上下文开始事件（ContextStartedEvent）：当容器调用ConfigurableApplicationContext的Start()方法开始/重新开始容器时触发该事件。  
* 上下文停止事件（ContextStoppedEvent）：当容器调用ConfigurableApplicationContext的Stop()方法停止容器时触发该事件。  
* 上下文关闭事件（ContextClosedEvent）：当ApplicationContext被关闭时触发该事件。容器被关闭时，其管理的所有单例Bean都被销毁。  
* 请求处理事件（RequestHandledEvent）：在Web应用中，当一个http请求（request）结束触发该事件。如果一个bean实现了ApplicationListener接口，当一个ApplicationEvent被发布以后，bean会自动被通知。  

##### 1.3.4.2.2. Aware接口
&emsp; **<font color = "clime">容器管理的Bean一般不需要了解容器的状态和直接使用容器，但在某些情况下，是需要在Bean中直接对IOC容器进行操作的，这时候，就需要在Bean中设定对容器的感知。Spring IOC容器也提供了该功能，它是通过特定的aware接口来完成的。</font>** aware接口有以下这些：

* BeanNameAware，可以在Bean中得到它在IOC容器中的Bean实例名称。  
* BeanFactoryAware，可以在Bean中得到Bean所在的IOC容器，从而直接在Bean中使用IOC容器的服务。  
* ApplicationContextAware，可以在Bean中得到Bean所在的应用上下文，从而直接在 Bean中使用应用上下文的服务。  
* MessageSourceAware，在Bean中可以得到消息源。  
* ApplicationEventPublisherAware，在Bean中可以得到应用上下文的事件发布器，从而可以在Bean中发布应用上下文的事件。  
* ResourceLoaderAware，在Bean中可以得到ResourceLoader，从而在Bean中使用ResourceLoader加载外部对应的Resource资源。</font>  

&emsp; 在设置Bean的属性之后，调用初始化回调方法之前，Spring会调用aware接口中的setter方法。  

##### 1.3.4.2.3. 后置处理器
1. <font color = "clime">实现BeanFactoryPostProcessor接口，可以`在spring的bean创建之前，修改bean的定义属性（BeanDefinition）`。</font>  
2. <font color = "red">实现BeanPostProcessor接口，</font><font color = "blue">可以在spring容器实例化bean之后，`在执行bean的初始化方法前后，`添加一些自己的处理逻辑。</font>  

##### 1.3.4.2.4. InitializingBean
&emsp; ......  

### 1.3.5. SpringAOP教程
1. SpringAOP的主要功能是：日志记录，性能统计，安全控制，事务处理，异常处理等。 
    * 慢请求记录  
    * 使用aop + redis + Lua接口限流
2. `SpringAOP失效：`  
&emsp; 参考[Spring事务失效](/docs/SSM/Spring/SpringTransactionInvalid.md)  
&emsp; <font color = "red">同一对象内部方法嵌套调用，慎用this来调用被@Async、@Transactional、@Cacheable等注解标注的方法，this下注解可能不生效。</font>async方法中的this不是动态代理的子类对象，而是原始的对象，故this调用无法通过动态代理来增强。 
3. **<font color = "red">过滤器，拦截器和aop的区别：</font>** 过滤器拦截的是URL；拦截器拦截的是URL；Spring AOP只能拦截Spring管理Bean的访问（业务层Service）。  

### 1.3.6. SpringAOP解析
1. **<font color = "blue">自动代理触发的时机：AspectJAnnotationAutoProxyCreator是一个【后置处理器BeanPostProcessor】，</font>** 因此Spring AOP是在这一步，进行代理增强！  
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

### 1.3.7. Spring事务
#### 1.3.7.1. Spring事务使用  
1. `@Transactional(rollbackFor = Exception.class) `，Transactional`默认只回滚RuntimeException，`但是可以指定要回滚的异常类型。    
2. **<font color = "red">Spring事务属性通常由事务的传播行为、事务的隔离级别、事务的超时值、事务只读标志组成。</font>**  
    * 事务的传播行为主要分为支持当前事务和不支持当前事务。  
        &emsp; <font color = "red">PROPAGATION_REQUIRED：如果当前存在事务，则加入该事务，合并成一个事务；如果当前没有事务，则创建一个新的事务。这是默认值。</font>  
        &emsp; 下面的类型都是针对于被调用方法来说的，理解起来要想象成两个service 方法的调用才可以。  
        &emsp; **支持当前事务的情况：**  
        &emsp; 1. <font color = "red">PROPAGATION_REQUIRED：如果当前存在事务，则加入该事务，合并成一个事务；如果当前没有事务，则创建一个新的事务。这是默认值。</font>  
        &emsp; 2. PROPAGATION_SUPPORTS：如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。  
        &emsp; 3. PROPAGATION_MANDATORY：如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常，即父级方法必须有事务。  

        &emsp; **不支持当前事务的情况：**  
        &emsp; 4. PROPAGATION_REQUIRES_NEW：创建一个新的事务，如果当前存在事务，则把当前事务挂起。这个方法会独立提交事务，不受调用者的事务影响，父级异常，它也是正常提交。  
        &emsp; 5. PROPAGATION_NOT_SUPPORTED：以非事务方式运行，如果当前存在事务，则把当前事务挂起。  
        &emsp; 6. PROPAGATION_NEVER：以非事务方式运行，如果当前存在事务，则抛出异常，即父级方法必须无事务。  

        &emsp; **其他情况：**  
        &emsp; 7. PROPAGATION_NESTED：如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于PROPAGATION_REQUIRED。  
        &emsp; 嵌套事务是外部事务的一部分，只有外部事务结束后它才会被提交。由此可见，PROPAGATION_REQUIRES_NEW和PROPAGATION_NESTED的最大区别在于：PROPAGATION_REQUIRES_NEW完全是一个新的事务，而PROPAGATION_NESTED则是外部事务的子事务，如果外部事务commit，嵌套事务也会被commit， 这个规则同样适用于roll back。  
    * 事务的隔离级别，包含数据库的4种隔离级别，默认使用底层数据库的默认隔离级别。  
    * 事务只读，相当于将数据库设置成只读数据库，此时若要进行写的操作，会出现错误。  

#### 1.3.7.2. Spring事务问题
1. 事务失效
    1. <font color = "red">同一个类中方法调用。</font>  
    &emsp; 因为spring声明式事务是基于AOP实现的，是使用动态代理来达到事务管理的目的，当前类调用的方法上面加@Transactional 这个是没有任何作用的，因为 **<font color = "clime">调用这个方法的是this，没有经过 Spring 的代理类。</font>**  
    2. 方法不是public的。    
    &emsp; @Transactional 只能用于 public 的方法上，否则事务不会失效，如果要用在非 public 方法上，可以开启 AspectJ 代理模式。  
    3. 抛出的异常不支持回滚。捕获了异常，未再抛出。  
2. 大事务问题：将修改库的代码聚合在一起。  

### 1.3.8. SpringMVC解析
1. **SpringMVC的工作流程：**  
    1. 找到处理器：前端控制器DispatcherServlet ---> **<font color = "red">处理器映射器HandlerMapping</font>** ---> 找到处理器Handler；  
    2. 处理器处理：前端控制器DispatcherServlet ---> **<font color = "red">处理器适配器HandlerAdapter</font>** ---> 处理器Handler ---> 执行具体的处理器Controller（也叫后端控制器） ---> Controller执行完成返回ModelAndView；  
    &emsp; 1. 处理器映射器HandlerMapping：根据请求的url查找Handler即处理器（Controller）。  
    &emsp; 2. **<font color = "blue">处理器适配器HandlAdapter：按照特定规则（HandlerAdapter要求的规则）去执行Handler。通过HandlerAdapter对处理器进行执行，这是适配器模式的应用，通过扩展适配器可以对更多类型的处理器进行执行。</font>**  
    &emsp; 3. 处理器Handler和controller区别：
    3. 返回前端控制器DispatcherServlet ---> 视图解析器ViewReslover。  
2. **SpringMVC解析：**  
    1. 在SpringMVC.xml中定义一个DispatcherServlet和一个监听器ContextLoaderListener。  
    2. 上下文在web容器中的启动：<font color = "red">由ContextLoaderListener启动的上下文为根上下文。在根上下文的基础上，还有一个与Web MVC相关的上下文用来保存控制器（DispatcherServlet）需要的MVC对象，作为根上下文的子上下文，构成一个层次化的上下文体系。</font>  
    3. **<font color = "red">`DispatcherServlet初始化和使用：`</font>**     
        1. 初始化阶段。DispatcherServlet的初始化在HttpServletBean#init()方法中。 **<font color = "red">`完成Spring MVC的组件的初始化。`</font>**    
        2. 调用阶段。这一步是由请求触发的。入口为DispatcherServlet#doService() ---> DispatcherServlet#doDispatch()。 **<font color = "blue">`逻辑即为SpringMVC处理流程。`</font>**   


### 1.3.9. 过滤器、拦截器、监听器
&emsp; 过滤前-拦截前-action执行-拦截后-过滤后  

## 1.4. MyBatis

### 1.4.1. MyBatis大数据量查询
1. `流式查询（针对查询结果集比较大）`  
&emsp; 流式查询指的是查询成功后不是返回一个集合而是返回一个迭代器，应用每次从迭代器取一条查询结果。流式查询的好处是能够降低内存使用。  
&emsp; **<font color = "clime">如果没有流式查询，想要从数据库取 1000 万条记录而又没有足够的内存时，就不得不分页查询，而分页查询效率取决于表设计，如果设计的不好，就无法执行高效的分页查询。因此流式查询是一个数据库访问框架必须具备的功能。</font>**  
&emsp; 流式查询的过程当中，数据库连接是保持打开状态的，因此要注意的是： **<font color = "clime">执行一个流式查询后，数据库访问框架就不负责关闭数据库连接了，需要应用在取完数据后自己关闭。</font>**  

### 1.4.2. MyBatis架构
&emsp; **<font color = "red">Mybatis的功能架构分为三层：</font>**  

* API接口层：提供给外部使用的接口API，开发人员通过这些本地API来操纵数据库。接口层一接收到调用请求就会调用核心处理层来完成具体的数据处理。  
* 核心处理层：负责具体的SQL查找、SQL解析、SQL执行和执行结果映射处理等。它主要的目的是根据调用的请求完成一次数据库操作。  
* 基础支持层：负责最基础的功能支撑，包括连接管理、事务管理、配置加载和缓存处理，这些都是共用的东西，将它们抽取出来作为最基础的组件。为上层的数据处理层提供最基础的支撑。  

### 1.4.3. MyBatis SQL执行解析
1. Mybatis Sql执行流程：   
    1. 读取核心配置文件并返回InputStream流对象。
    2. 根据InputStream流对象解析出Configuration对象，然后创建SqlSessionFactory工厂对象。
    3. 根据一系列属性从SqlSessionFactory工厂中创建SqlSession。
    4. 从SqlSession中调用Executor执行数据库操作和生成具体SQL指令。
    5. 对执行结果进行二次封装。
    6. 提交与事务。      
2. **<font color = "clime">Mapper接口动态代理类的生成：</font>** 
    * 生成代理工厂类：  
    &emsp; **<font color = "blue">解析配置文件生成sqlSessionFactory时，</font>** 会调用bindMapperForNamespace() ---> addMapper()方法， **<font color = "blue">根据mapper文件中的namespace属性值，`将接口生成动态代理类的工厂，存储在MapperRegistry对象中`。</font>** （MapperRegistry内部维护一个映射关系，每个接口对应一个`MapperProxyFactory（生成动态代理工厂类）`。）      
    * 生成对应Mapper的代理类：    
    &emsp; 在调用getMapper，根据type类型，从MapperRegistry对象中的knownMappers获取到当前类型对应的代理工厂类，然后通过代理工厂类使用`jdk自带的动态代理`生成对应Mapper的代理类。  
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

### SqlSession详解  


### 1.4.4. Spring集成Mybatis  
0. MyBatis运行原理：1.创建SqlSessionFacory；2.从SqlSessionFactory对象中获取SqlSession对象；3.获取Mapper；4.执行操作；    
1. 创建SqlSessionFacory  
&emsp; MyBatis-Spring中创建SqlSessionFacory是由SqlSessionFactoryBean完成的。实现了InitializingBean接口、FactoryBean接口、ApplicationListener接口。  
2. 创建SqlSession   
&emsp; 在Spring中并没有直接使用DefaultSqlSession，DefaultSqlSession是线程不安全的。Spring对SqlSession 进行了一个封装，这个SqlSession的实现类就是SqlSessionTemplate。SqlSessionTemplate是线程安全的。SqlSessionTemplate通过动态代理的方式来保证DefaultSqlSession操作的线程安全性。  
3. 接口的扫描注册  
&emsp; MapperScannerConfigurer 实现了BeanDefinitionRegistryPostProcessor接口， BeanDefinitionRegistryPostProcessor 是 BeanFactoryPostProcessor的子类。  
4. 接口注入使用  
&emsp; MapperFactoryBean，因为实现了 FactoryBean 接口，同样是调用getObject()方法。  


### 1.4.5. MyBatis缓存
&emsp; ......  

### 1.4.6. MyBatis插件解析
1. **<font color="clime">Mybaits插件的实现主要用了拦截器、责任链和动态代理。</font>** `动态代理可以对SQL语句执行过程中的某一点进行拦截`，`当配置多个插件时，责任链模式可以进行多次拦截`。  
2. **<font color = "clime">mybatis扩展性很强，基于插件机制，基本上可以控制SQL执行的各个阶段，如执行器阶段，参数处理阶段，语法构建阶段，结果集处理阶段，具体可以根据项目业务来实现对应业务逻辑。</font>**   
    * 执行器Executor（update、query、commit、rollback等方法）；  
    * 参数处理器ParameterHandler（getParameterObject、setParameters方法）；  
    * 结果集处理器ResultSetHandler（handleResultSets、handleOutputParameters等方法）；  
    * SQL语法构建器StatementHandler（prepare、parameterize、batch、update、query等方法）；    

----------------------------

## 1.5. 分布微服务和集群  
&emsp; `分布式带来数据一致性的问题。` 解决方案：采用事务、锁，也可以使用补偿方案。   

## 1.6. SpringBoot

### 1.6.1. SpringBoot基础知识
&emsp; SpringBoot基本上是Spring框架的扩展，它消除了设置Spring应用程序所需的XML配置，为更快，更高效的开发生态系统铺平了道路。  

* SpringBoot简化了Spring的配置；
* SpringBoot提供了起步依赖、自动配置；
* SpringBoot内嵌了Tomcat、 Jetty、 Undertow容器（无需部署war文件）；
* 提供生产指标，例如指标、健壮检查和外部化配置。  

### 1.6.2. SpringBoot启动过程
&emsp; SpringApplication.run()中首先new SpringApplication对象，然后调用该对象的run方法。<font color = "red">`即run()方法主要包括两大步骤：`</font>  
1. 创建SpringApplication对象；  
2. 运行run()方法。  

#### 1.6.2.1. SpringApplication初始化
1. **<font color = "blue">构造过程一般是对构造函数的一些成员属性赋值，做一些初始化工作。</font><font color = "blue">SpringApplication有6个属性：`资源加载器`、资源类集合、应用类型、`初始化器`、`监听器`、`包含main函数的主类`。</font>**  
	1. 给resourceLoader属性赋值，resourceLoader属性，资源加载器，此时传入的resourceLoader参数为null；  
	2. **<font color = "clime">初始化资源类集合并去重。</font>** 给primarySources属性赋值，primarySources属性即`SpringApplication.run(MainApplication.class,args);`中传入的MainApplication.class，该类为SpringBoot项目的启动类，主要通过该类来扫描Configuration类加载bean；
	3. **<font color = "clime">判断当前是否是一个 Web 应用。</font>** 给webApplicationType属性赋值，webApplicationType属性，代表应用类型，根据classpath存在的相应Application类来判断。因为后面要根据webApplicationType来确定创建哪种Environment对象和创建哪种ApplicationContext；
	4. **<font color = "blue">设置应用上下文初始化器。</font>** 给initializers属性赋值，initializers属性为List<ApplicationContextInitializer<?\>>集合，利用SpringBoot的SPI机制从spring.factories配置文件中加载，后面`在初始化容器的时候会应用这些初始化器来执行一些初始化工作`。`因为SpringBoot自己实现的SPI机制比较重要；`    
    &emsp; loadSpringFactories方法主要做的事情就是利用之前获取的线程上下文类加载器将classpath中的所有spring.factories配置文件中所有SPI接口的所有扩展实现类给加载出来，然后放入缓存中。注意，这里是一次性加载所有的SPI扩展实现类，所以之后根据SPI接口就直接从缓存中获取SPI扩展类了，就不用再次去spring.factories配置文件中获取SPI接口对应的扩展实现类了。`比如之后的获取ApplicationListener,FailureAnalyzer和EnableAutoConfiguration接口的扩展实现类都直接从缓存中获取即可。`  
	5. **<font color = "blue">设置监听器。</font>** 给listeners属性赋值，listeners属性为List<ApplicationListener<?\>>集合，同样利用SpringBoot的SPI机制从spring.factories配置文件中加载。因为SpringBoot启动过程中会在不同的阶段发射一些事件，所以这些加载的监听器们就是来监听SpringBoot启动过程中的一些生命周期事件的；
	6. **<font color = "clime">推断主入口应用类。</font>** 给mainApplicationClass属性赋值，mainApplicationClass属性表示包含main函数的类，即这里要推断哪个类调用了main函数，然后把这个类的全限定名赋值给mainApplicationClass属性，用于后面启动流程中打印一些日志。

2. **<font color = "clime">SpringApplication初始化中第4步和第5步都是利用SpringBoot的[SPI机制](/docs/java/basis/SPI.md)来加载扩展实现类。`SpringBoot通过以下步骤实现自己的SPI机制：`</font>**  
	1. 首先获取线程上下文类加载器;  
    ![image](http://182.92.69.8:8081/img/sourceCode/springBoot/boot-10.png) 
	2. 然后利用上下文类加载器从spring.factories配置文件中加载所有的SPI扩展实现类并放入缓存中；  
	3. 根据SPI接口从缓存中取出相应的SPI扩展实现类；  
	4. 实例化从缓存中取出的SPI扩展实现类并返回。  
    ![image](http://182.92.69.8:8081/img/microService/boot/boot-11.png)  

#### 1.6.2.2. run()方法运行过程
1. **<font color = "clime">运行流程，分3步：</font>**  
    ![image](http://182.92.69.8:8081/img/microService/boot/boot-9.png)  

    ```java
    // SpringApplication.java
    public ConfigurableApplicationContext run(String... args) {
        // 创建并启动计时监控类。new 一个StopWatch用于统计run启动过程花了多少时间
        StopWatch stopWatch = new StopWatch();
        // 开始计时，首先记录了当前任务的名称，默认为空字符串，然后记录当前Spring Boot应用启动的开始时间
        stopWatch.start();
        ConfigurableApplicationContext context = null;
        // exceptionReporters集合用来存储异常报告器，用来报告SpringBoot启动过程的异常
        Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
        // 配置系统属性headless，即“java.awt.headless”属性，默认为ture
        // 其实是想设置该应用程序,即使没有检测到显示器,也允许其启动.对于服务器来说,是不需要显示器的,所以要这样设置.
        configureHeadlessProperty();
        // 【1】创建所有 Spring 运行监听器并发布应用启动事件
        //从spring.factories配置文件中加载到EventPublishingRunListener对象并赋值给SpringApplicationRunListeners
        // EventPublishingRunListener对象主要用来发布SpringBoot启动过程中内置的一些生命周期事件，标志每个不同启动阶段
        SpringApplicationRunListeners listeners = getRunListeners(args);
        // 启动SpringApplicationRunListener的监听，表示SpringApplication开始启动。
        // 》》》》》发布【ApplicationStartingEvent】事件
        listeners.starting();
        try {
            // 初始化默认应用参数类，封装命令行参数
            // 创建ApplicationArguments对象，封装了args参数
            ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
            // 【2】准备环境变量，包括系统变量，环境变量，命令行参数，默认变量，servlet相关配置变量，随机值，
            // JNDI属性值，以及配置文件（比如application.properties）等，注意这些环境变量是有优先级的
            // 》》》》》发布【ApplicationEnvironmentPreparedEvent】事件
            ConfigurableEnvironment environment = prepareEnvironment(listeners,applicationArguments);
            // 配置spring.beaninfo.ignore属性，默认为true，即跳过搜索BeanInfo classes.
            configureIgnoreBeanInfo(environment);
            // 【3】控制台打印SpringBoot的bannner标志
            Banner printedBanner = printBanner(environment);
            // 【4】创建容器
            // 根据不同类型创建不同类型的spring applicationcontext容器
            // 因为这里是servlet环境，所以创建的是AnnotationConfigServletWebServerApplicationContext容器对象
            context = createApplicationContext();
            // 【5】准备异常报告器
            // 从spring.factories配置文件中加载异常报告期实例，这里加载的是FailureAnalyzers
            // 注意FailureAnalyzers的构造器要传入ConfigurableApplicationContext，因为要从context中获取beanFactory和environment
            exceptionReporters = getSpringFactoriesInstances(
                    SpringBootExceptionReporter.class,
                    new Class[] { ConfigurableApplicationContext.class }, context); // ConfigurableApplicationContext是AnnotationConfigServletWebServerApplicationContext的父接口
            // 【6】容器准备
            //为刚创建的AnnotationConfigServletWebServerApplicationContext容器对象做一些初始化工作，准备一些容器属性值等
            // 1）为AnnotationConfigServletWebServerApplicationContext的属性AnnotatedBeanDefinitionReader和ClassPathBeanDefinitionScanner设置environgment属性
            // 2）根据情况对ApplicationContext应用一些相关的后置处理，比如设置resourceLoader属性等
            // 3）在容器刷新前调用各个ApplicationContextInitializer的初始化方法，ApplicationContextInitializer是在构建SpringApplication对象时从spring.factories中加载的
            // 4）》》》》》发布【ApplicationContextInitializedEvent】事件，标志context容器被创建且已准备好
            // 5）从context容器中获取beanFactory，并向beanFactory中注册一些单例bean，比如applicationArguments，printedBanner
            // 6）TODO 加载bean到application context，注意这里只是加载了部分bean比如mainApplication这个bean，大部分bean应该是在AbstractApplicationContext.refresh方法中被加载？这里留个疑问先
            // 7）》》》》》发布【ApplicationPreparedEvent】事件，标志Context容器已经准备完成
            prepareContext(context, environment, listeners, applicationArguments,printedBanner);
            // 【7】刷新容器，IOC 容器初始化（如果是 Web 应用还会创建嵌入式的 Tomcat），扫描、创建、加载所有组件
            // 1）在context刷新前做一些准备工作，比如初始化一些属性设置，属性合法性校验和保存容器中的一些早期事件等；
            // 2）让子类刷新其内部bean factory,注意SpringBoot和Spring启动的情况执行逻辑不一样
            // 3）对bean factory进行配置，比如配置bean factory的类加载器，后置处理器等
            // 4）完成bean factory的准备工作后，此时执行一些后置处理逻辑，子类通过重写这个方法来在BeanFactory创建并预准备完成以后做进一步的设置
            // 在这一步，所有的bean definitions将会被加载，但此时bean还不会被实例化
            // 5）执行BeanFactoryPostProcessor的方法即调用bean factory的后置处理器：
            // BeanDefinitionRegistryPostProcessor（触发时机：bean定义注册之前）和BeanFactoryPostProcessor（触发时机：bean定义注册之后bean实例化之前）
            // 6）注册bean的后置处理器BeanPostProcessor，注意不同接口类型的BeanPostProcessor；在Bean创建前后的执行时机是不一样的
            // 7）初始化国际化MessageSource相关的组件，比如消息绑定，消息解析等
            // 8）初始化事件广播器，如果bean factory没有包含事件广播器，那么new一个SimpleApplicationEventMulticaster广播器对象并注册到bean factory中
            // 9）AbstractApplicationContext定义了一个模板方法onRefresh，留给子类覆写，比如ServletWebServerApplicationContext覆写了该方法来创建内嵌的tomcat容器
            // 10）注册实现了ApplicationListener接口的监听器，之前已经有了事件广播器，此时就可以派发一些early application events
            // 11）完成容器bean factory的初始化，并初始化所有剩余的单例bean。这一步非常重要，一些bean postprocessor会在这里调用。
            // 12）完成容器的刷新工作，并且调用生命周期处理器的onRefresh()方法，并且发布ContextRefreshedEvent事件
            refreshContext(context);
            // 【8】应用上下文刷新后置处理，从 IOC 容器中获取所有的 ApplicationRunner 和 CommandLineRunner 进行回调
            afterRefresh(context, applicationArguments);
            // 停止stopWatch计时
            stopWatch.stop();
            // 输出日志记录执行主类名、时间信息
            if (this.logStartupInfo) {
                new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
            }
            // 》》》》》发布【ApplicationStartedEvent】事件，标志spring容器已经刷新，此时所有的bean实例都已经加载完毕
            listeners.started(context);
            // 【9】调用ApplicationRunner和CommandLineRunner的run方法，实现spring容器启动后需要做的一些东西比如加载一些业务数据等
            callRunners(context, applicationArguments);
        }
        // 【10】若启动过程中抛出异常，此时用FailureAnalyzers来报告异常
        // 并》》》》》发布【ApplicationFailedEvent】事件，标志SpringBoot启动失败
        catch (Throwable ex) {
            handleRunFailure(context, ex, exceptionReporters, listeners);
            throw new IllegalStateException(ex);
        }

        try {
            // 发布应用上下文就绪事件，触发所有SpringApplicationRunListener 监听器的running事件方法。
            // 》》》》》发布【ApplicationReadyEvent】事件，标志SpringApplication已经正在运行即已经成功启动，可以接收服务请求了。
            listeners.running(context);
        }
        // 若出现异常，此时仅仅报告异常，而不会发布任何事件
        catch (Throwable ex) {
            handleRunFailure(context, ex, exceptionReporters, null);
            throw new IllegalStateException(ex);
        }
        // 【11】最终返回容器
        return context;
    }
    ```
2. **<font color = "clime">`内置生命周期事件：`</font>** <font color = "red">在SpringBoot启动过程中，每个不同的启动阶段会分别发布不同的内置生命周期事件。</font>  
3. **<font color = "clime">`事件回调机制：`</font>** <font color = "red">run()阶段涉及了比较重要的[事件回调机制](/docs/microService/SpringBoot/eventCallback.md)，回调4个监听器（ApplicationContextInitializer、ApplicationRunner、CommandLineRunner、SpringApplicationRunListener）中的方法与加载项目中组件到IOC容器中。</font>  

#### 1.6.2.3. SpringBoot事件监听机制
##### 1.6.2.3.1. 事件监听步骤
&emsp; **<font color = "clime">SpringBoot启动时广播生命周期事件步骤：</font>**    
1. 为广播SpringBoot内置生命周期事件做前期准备：    
    1. 首先加载ApplicationListener监听器实现类；  
    2. 其次加载SPI扩展类EventPublishingRunListener。  
2. SpringBoot启动时利用EventPublishingRunListener广播生命周期事件，然后ApplicationListener监听器实现类监听相应的生命周期事件执行一些初始化逻辑的工作。  
    1. 构建SpringApplication对象时<font color = "red">根据ApplicationListener接口去spring.factories配置文件中加载并实例化监听器。</font>  
    2. 在SpringBoot的启动过程中首先从spring.factories配置文件中加载并实例化EventPublishingRunListener对象。  
    3. 在SpringBoot的启动过程中会发布一系列SpringBoot内置的生命周期事件。  

##### 1.6.2.3.2. 内置生命周期事件
&emsp; **<font color = "clime">SpringBoot内置的7种生命周期事件</font>**  

|发布顺序|事件|用途|
|---|---|---|
|1|ApplicationStartingEvent|在SpringApplication`启动时`但在环境变量Environment或容器ApplicationContext创建前触发，标志SpringApplication开始启动。|
|2|ApplicationEnvironmentPreparedEvent|在SpringApplication`已经开始启动`且环境变量Environment已经准备好时触发，标志环境变量已经准备好。|
|3|ApplicationContextInitializedEvent|ApplicationContextInitilizers的初始化方法已经被调用即从spring.factories中加载的initializers已经执行ApplicationContext初始化逻辑但在bean定义加载前触发，标志Application已经初始化完毕。|
|4|ApplicationPreparedEvent|在Spring容器`刷新refresh前`触发。|
|5|ApplicationStaredEvent|在Spring`容器刷新后`触发，但在调用ApplicationRunner和CommandLineRunner的run方法调用前触发，标志Spring容器已经刷新，此时所有的bean实例等都已经加载了。|
|6|ApplicationReadyEvent|只要SpringApplication可以接收服务请求时即调用完ApplicationRunner和CommandLineRunner的run方法后触发，此时标志SpringApplication已经正在运行，即成功启动。|
|7|ApplicationFailedEvent|若SpringApplicaton`未能成功启动时`则会catch住异常发布ApplicationFailedEvent事件，标志ApplicationFailedEvent启动失败。|


#### 1.6.2.4. SpringBoot事件回调
&emsp; **<font color = "clime">SpringBoot事件回调：</font>**  

* **<font color = "red">ApplicationContextInitializer，IOC容器初始化时被回调；</font>**  
* **<font color = "red">SpringApplicationRunListener，SpringBoot启动过程中多次被回调；</font>**  
* **<font color = "red">ApplicationRunner，容器启动完成后被回调；</font>**  
* **<font color = "red">CommandLineRunner，ApplicationRunner之后被回调。</font>**  

### 1.6.3. SpringBoot自动配置（获取 ---> 注入 ---> 装配）
&emsp; 包含两部分：1. 注解@SpringBootApplication，获取自动配置；2. 加载自动配置流程。

#### 1.6.3.1. 获取自动配置，注解@SpringBootApplication
1. @SpringBootApplication  
    * @ComponentScan  
    * @SpringBootConfiguration  
    * @EnableAutoConfiguration  
2. `@EnableAutoConfiguration`使用@Import将所有符合自动配置条件的bean定义加载到IOC容器。   
    1. `@Import({AutoConfigurationImportSelector.class})`，开启自动配置，导入了AutoConfigurationImportSelector类。  
    2. `AutoConfigurationImportSelector#getCandidateConfigurations()`方法获取配置文件spring.factories所有候选的配置，剔除重复部分，再剔除@SpringbootApplication注解里exclude的配置，才得到最终的配置类名集合。  


#### 1.6.3.2. 加载自动配置流程
1. `启动对象的注入`：在SpringBoot启动流程的`容器准备阶段`prepareContext()会将@SpringBootApplication--->@Component对象注册到容器中。  
2. `自动装配入口`，从SpringBoot启动流程的`刷新容器阶段`refresh()开始。 

#### 1.6.3.3. 内置Tomcat
1. SpringBoot内置Tomcat，可以对比SpringBoot自动配置运行流程了解。  
2. Tomcat的自动装配：自动装配过程中，Web容器所对应的自动配置类为ServletWebServerFactoryAutoConfiguration，该类导入了EmbeddedTomcat，EmbeddedJetty，EmbeddedUndertow三个类，可以根据用户的需求去选择使用哪一个web服务器，默认情况下使用的是tomcat。  
3. Tomcat的启动：在容器刷新refreshContext(context)步骤完成。  

### 1.6.4. 自定义strater
1. 第一步，创建maven项目  
	1. 命名潜规则  
	&emsp; spring-boot-starter-XX是springboot官方的starter；XX-spring-boot-starter是第三方扩展的starter。
	2. 项目pom文件
2. `第二步，写自动配置逻辑`
	1. 编写业务逻辑  
	2. 定义配置文件对应类  
    	* @ConfigurationProperties 配置属性文件，需要指定前缀prefix。
    	* @EnableConfigurationProperties 启用配置，需要指定启用的配置类。
    	* @NestedConfigurationProperty 当一个类中引用了外部类，需要在该属性上加该注解。
	3. 定义自动配置类，自动暴露功能接口。  
        ```java
        @ConditionalOnProperty(prefix = "xxxxx", name = "enable",havingValue = "true", matchIfMissing = true)
        ```
3. 第三步，应用加载starter的配置，有两种方式：主动加载、被动加载。  


## 1.7. SpringCloud
&emsp; **<font color = "clime">Spring Cloud各组件运行流程：</font>**  
1. 外部或者内部的非Spring Cloud项目都统一通过微服务网关（Zuul）来访问内部服务。客户端的请求首先经过负载均衡（Ngnix），再到达服务网关（Zuul集群）；  
2. 网关接收到请求后，从注册中心（Eureka）获取可用服务；  
3. 由Ribbon进行负载均衡后，分发到后端的具体实例；  
4. 微服务之间也可通过Feign进行通信处理业务；  
5. Hystrix负责处理服务超时熔断；Hystrix dashboard，Turbine负责监控Hystrix的熔断情况，并给予图形化的展示；  
6. 服务的所有的配置文件由配置服务管理，配置服务的配置文件放在git仓库，方便开发人员随时改配置。  

### 1.7.1. Eureka
1. 服务治理框架和产品都围绕着服务注册与服务发现机制来完成对微服务应用实例的自动化管理。  
2. Spring Cloud Eureka，使用Netflix Eureka来实现服务注册与发现，它既包含了服务端组件，也包含了客户端组件。  
3. Eureka服务治理体系包含三个核心角色：服务注册中心、服务提供者以及服务消费者。  
    * 服务提供者  
    &emsp; 服务同步、服务续约。
    * 服务消费者    
    &emsp; 荻取服务、服务调用、服务下线。  
    &emsp; 服务下线：在系统运行过程中必然会面临关闭或重启服务的某个实例的情况，在服务关闭期间，不希望客户端会继续调用关闭了的实例。 所以<font color = "red">在客户端程序中，当服务实例进行正常的关闭操作时，它会触发一个服务下线的REST请求给Eurka Server，告诉服务注册中心：“我要下线了”。服务端在接收到请求之后，将该服务状态置为下线(DOWN)，并把该下线事件传播出去。</font>  
    * 服务注册中心  
        * 失效剔除  
        * 自我保护：  
        &emsp; 开启自我保护后，Eureka Server在运行期间，会统计心跳失败的比例在15分钟之内是否低于85%，如果出现低于的情况（在单机调试的时候很容易满足，实际在生产环境上通常是由于网络不稳定导致），Eureka Server会将当前的实例注册信息保护起来，让这些实例不会过期，尽可能保护这些注册信息。  
        &emsp; Eureka Server进入自我保护状态后，客户端很容易拿到实际已经不存在的服务实例，会出现调用失败的清况。  
4. <font color = "red">高可用注册中心/AP模型：EurekaServer的高可用实际上就是将自己作为服务向其他服务注册中心注册自己，这样就可以形成一组互相注册的服务注册中心，以实现服务清单的互相同步，达到高可用的效果。</font>  


### 1.7.2. Ribbon
&emsp; 负载均衡大体可以分为两类：集中式、进程内。前者也被称为服务端负载均衡，其一般位于服务集群的前端，统一接收、处理、转发客户端的请求。典型的包括F5硬件、LVS、Nginx等技术方案；而后者也被称为客户端负载均衡，其是在客户端侧根据某种策略选择合适的服务实例直接进行请求，其典型代表有Ribbon。  

### 1.7.3. Feign


### 1.7.4. Zuul
1. Spring Cloud Zuul，微服务网关，包含hystrix、ribbon、actuator。主要有路由转发、请求过滤功能。  
2. **<font color = "red">Zuul提供了四种过滤器的API，分别为前置（Pre）、路由（Route）、后置（Post）和错误（Error）四种处理方式。</font>**  
3. 动态加载：  
&emsp; 作为最外部的网关，它必须具备动态更新内部逻辑的能力，比如动态修改路由规则、动态添加／删除过滤器等。  
&emsp; 通过Zuul实现的API网关服务具备了动态路由和动态过滤器能力，可以在不重启API网关服务的前提下为其动态修改路由规则和添加或删除过滤器。   

### 1.7.5. Hytrix
1. 服务雪崩：在微服务架构中，存在着那么多的服务单元，若一个单元出现故障，就很容易因依赖关系而引发故障的蔓延，最终导致整个系统的瘫痪。  
2. Hystrix工作流程：1. 包装请求 ---> 2. 发起请求 ---> 3. 缓存处理 ---> 4. 判断断路器是否打开（熔断） ---> 5. 判断是否进行业务请求（请求是否需要隔离或降级） ---> 6. 执行业务请求 ---> 7. 健康监测 ---> 8. fallback处理或返回成功的响应。  
3. 熔断是一种[降级](/docs/microService/thinking/Demotion.md)策略。
    1. <font color = "clime">熔断的对象是服务之间的请求；`1).熔断策略会根据请求的数量（资源）分为信号量和线程池，2).还有请求的时间（即超时熔断），3).请求错误率（即熔断触发降级）。`</font>  
    &emsp; ~~Hystrix中的降级方案：熔断触发降级、请求超时触发降级、【资源】（信号量、线程池）隔离触发降级 / 依赖隔离。~~  
    2. 线程池隔离与信号量隔离  
        1. 线程池隔离：  
            1. `请求线程和【每个服务】单独用线程池。`比如现在有3个业务调用分别是查询订单、查询商品、查询用户，且这三个业务请求都是依赖第三方服务-订单服务、商品服务、用户服务。`为每一个服务接口单独开辟一个线程池，`保持与其他服务接口线程的隔离，提高该服务接口的独立性和高可用。    
            2. 优点：  
                1. 使用线程池隔离可以完全隔离依赖的服务，请求线程可以快速放回。  
                2. 当线程池出现问题时，线程池隔离是独立的，不会影响其他服务和接口。  
                3. 当失败的服务再次变得可用时，线程池将清理并可立即恢复，而不需要一个长时间的恢复。  
                4. 独立的线程池提高了并发性。    
            3. 缺点：  
                1.  **<font color = "clime">线程池隔离的主要缺点是它们增加计算开销（CPU）。每个命令的执行涉及到排队、调度和上下文切换都是在一个单独的线程上运行的。</font>**    
            1. ~~线程池隔离：~~  
                1. 调用线程和hystrixCommand线程不是同一个线程，并发请求数受到线程池（不是容器tomcat的线程池，而是hystrixCommand所属于线程组的线程池）中的线程数限制，默认是10。  
                2. 这个是默认的隔离机制。
                3. hystrixCommand线程无法获取到调用线程中的ThreadLocal中的值。
        2. 信号量隔离：
            1. 调用线程和hystrixCommand线程是同一个线程，默认最大并发请求数是10。  
            2. 调用速度快，开销小，由于和调用线程是处于同一个线程，所以必须确保调用的微服务可用性足够高并且返回快才用。  
        3. 什么情况下使用线程池隔离/信号量隔离？  
            * 请求并发量大，并且耗时长（请求耗时长一般是计算量大，或读数据库）：采用线程池隔离策略，这样的话，可以保证大量的容器（tomcat）线程可用，不会由于服务原因，一直处于阻塞或等待状态，快速失败返回。  
            * 请求并发量大，并且耗时短（请求耗时长一般是计算量大，或读缓存）：采用信号量隔离策略，因为这类服务的返回通常会非常的快，不会占用容器线程太长时间，而且也减少了线程切换的一些开销，提高了缓存服务的效率。  
4. <font color = "clime">微服务集群中，Hystrix的度量信息通过`Turbine`来汇集监控信息，并将聚合后的信息提供给Hystrix Dashboard来集中展示和监控。</font> 


### 1.7.6. Sleuth
1. 分布式调用链追踪系统，可以解决的问题：  
    **<font color = "red">(1) `如何快速定位请求异常；`</font>**    
    **<font color = "red">(2) `如何快速定位性能瓶颈；`</font>**  
    **<font color = "red">(3) 如何快速定位不合理调用；</font>**  
2. **<font color = "red">埋点日志通常包含：traceId、spanId、调用的开始时间，协议类型、调用方ip和端口，请求的服务名、调用耗时，调用结果，异常信息等，同时预留可扩展字段，为下一步扩展做准备；</font>**  
3. spring-cloud-starter-sleuth功能点：
    1. 有关traceId：获取当前traceId、日志获取traceId、传递traceId到异步线程池、子线程或线程池中获取Zipkin traceId并打印。  
    2. sleuth自定义信息。增加自定义属性、添加自定义Tag标签。  
    3. 监控本地方法：异步执行和远程调用都会新开启一个Span，如果想监控本地的方法耗时时间，可以采用埋点的方式监控本地方法，也就是开启一个新的Span。  
    4. ...  

### 1.7.7. Admin
1. spring-boot-starter-actuator依赖中已经实现的一些原生端点。根据端点的作用，<font color = "clime">可以将原生端点分为以下三大类：</font>  
    * 应用配置类：获取应用程序中加载的应用配置、环境变量、自动化配置报告等与Spring Boot应用密切相关的配置类信息。  
    * 度量指标类：获取应用程序运行过程中用于监控的度量指标，比如内存信息、线程池信息、HTTP请求统计等。  
    * 操作控制类：提供了对应用的关闭等操作类功能。  
2. 监控通知
3. 集成spring security

## 1.8. Dubbo
### 1.8.1. 分布式服务治理
#### 1.8.1.1. Dubbo和Spring Cloud
1. 两个框架在开始目标就不一致：<font color = "red">Dubbo定位服务治理；Spirng Cloud是一个生态。</font>  
    * Dubbo是SOA时代的产物，它的关注点主要在于服务的调用，流量分发、流量监控和熔断。  
    * Spring Cloud诞生于微服务架构时代，考虑的是微服务治理的方方面面，另外由于依托了Spirng、Spirng Boot的优势之上。  
2. <font color = "red">Dubbo底层是使用Netty这样的NIO框架，是基于TCP协议传输的，配合以Hession序列化完成RPC通信。</font><font color = "clime">而SpringCloud是基于Http协议+Rest接口调用远程过程的通信，</font>相对来说，Http请求会有更大的报文，占的带宽也会更多。但是`REST相比RPC更为灵活`，服务提供方和调用方的依赖只依靠一纸契约，不存在代码级别的强依赖，这在强调快速演化的微服务环境下，显得更为合适，`至于注重通信速度还是方便灵活性，具体情况具体考虑。`  

#### 1.8.1.2. Spring Cloud Alibaba介绍  
1. Spring Cloud与Dubbo的比较本身是不公平的，主要前者是一套较为完整的架构方案，而Dubbo只是服务治理与RPC实现方案。Spring Cloud Alibaba是阿里巴巴提供的微服务开发一站式解决方案，是阿里巴巴开源中间件与 Spring Cloud体系的融合。   
2. 集成 Spring Cloud 组件： **<font color = "clime">Spring Cloud Alibaba作为整套的微服务解决组件，只依靠目前阿里的开源组件是不够的，更多的是集成当前的社区组件，所以 Spring Cloud Alibaba 可以集成 Zuul，OpenFeign等网关，也支持 Spring Cloud Stream消息组件。</font>**  
3. **<font color = "clime">使用@DubboTransported注解可将底层的Rest协议无缝切换成Dubbo RPC协议，进行RPC调用。</font>**  
4. Spring Cloud Alibaba 基于 Nacos 提供 spring-cloud-alibaba-starter-nacos-discovery & spring-cloud-alibaba-starter-nacos-config 实现了服务注册 & 配置管理功能。  
&emsp; 使用 Seata 解决微服务场景下面临的分布式事务问题。  

### 1.8.2. RPC介绍
&emsp; ......  


### 1.8.3. Dubbo介绍
1. Dubbo的工作原理：  
    1. 服务启动的时候，provider和consumer根据配置信息，连接到注册中心register，分别向注册中心注册和订阅服务。  
    2. register根据服务订阅关系，返回provider信息到consumer，同时 **<font color = "clime">consumer会把provider信息缓存到本地。如果信息有变更，consumer会收到来自register的推送。</font>**  
    3. consumer生成代理对象，同时根据负载均衡策略，选择一台provider，同时定时向monitor记录接口的调用次数和时间信息。  
    4. **<font color = "clime">拿到代理对象之后，consumer通过`代理对象`发起接口调用。</font>**  
    5. provider收到请求后对数据进行反序列化，然后通过代理调用具体的接口实现。  
    ![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-52.png)   

&emsp; &emsp; [Dubbo负载、容错、降级](/docs/microService/dubbo/Load.md)  
&emsp; &emsp; [Dubbo协议和序列化](/docs/microService/dubbo/Agreement.md)  

### 1.8.4. Dubbo框架设计
1. 分层架构设计  
    ![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-51.png)  
    1. 从大的范围来说，dubbo分为三层：
        * business业务逻辑层由开发人员来提供接口和实现，还有一些配置信息。
        * `RPC层就是真正的RPC调用的核心层，封装整个RPC的调用过程、负载均衡、集群容错、代理。`
        * remoting则是对网络传输协议和数据转换的封装。  
    2. RPC层包含配置层config、代理层proxy、服务注册层register、路由层cluster、监控层monitor、远程调用层protocol。`⚠️dubbo远程调用可以分为2步：1.Invoker的生成；2.代理的生成。`      
        1. **<font color = "red">`服务代理层proxy`：服务接口透明代理，生成服务的客户端Stub和服务器端Skeleton，以ServiceProxy为中心，扩展接口为ProxyFactory。</font>**  
        &emsp; **<font color = "red">Proxy层封装了所有接口的透明化代理，而在其它层都以Invoker为中心，</font><font color = "blue">只有到了暴露给用户使用时，才用Proxy将Invoker转成接口，或将接口实现转成 Invoker，也就是去掉Proxy层RPC是可以Run的，只是不那么透明，不那么看起来像调本地服务一样调远程服务。</font>**  
        &emsp; dubbo实现接口的透明代理，封装调用细节，让用户可以像调用本地方法一样调用远程方法，同时还可以通过代理实现一些其他的策略，比如：负载、降级......  
        2. **<font color = "red">`远程调用层protocol`：封装RPC调用，以Invocation, Result为中心，扩展接口为Protocol, Invoker, Exporter。</font>**  
    3. remoting层：  
        1. 网络传输层：抽象mina和netty为统一接口，以Message为中心，扩展接口为Channel, Transporter, Client, Server, Codec。  
        2. 数据序列化层：可复用的一些工具，扩展接口为Serialization, ObjectInput, ObjectOutput, ThreadPool。  
2. ~~总体调用过程~~  

2. **<font color = "red">为什么要通过代理对象通信？</font>**    
    &emsp; dubbo实现接口的`透明代理，封装调用细节，让用户可以像调用本地方法一样调用远程方法`，同时还可以通过代理实现一些其他的策略，比如：  
    1. 调用的负载均衡策略  
    2. 调用失败、超时、降级和容错机制  
    3. 做一些过滤操作，比如加入缓存、mock数据  
    4. 接口调用数据统计  

### 1.8.5. Dubbo初始化（Dubbo和Spring）  


### 1.8.6. 暴露和引用服务（实际类 ---> invoker ---> ）
1. 解析服务：  
&emsp; **<font color = "clime">基于dubbo.jar内的META-INF/spring.handlers配置，Spring在遇到dubbo名称空间时，会回调DubboNamespaceHandler。所有dubbo的标签，都统一用DubboBeanDefinitionParser进行解析，基于一对一属性映射，将XML标签解析为Bean对象。</font>**  
&emsp; ⚠️注：`在暴露服务ServiceConfig.export()或引用服务ReferenceConfig.get()时，会将Bean对象转换URL格式，所有Bean属性转成URL的参数。`然后将URL传给协议扩展点，基于扩展点的扩展点自适应机制，根据URL的协议头，进行不同协议的服务暴露或引用。  
2. **服务提供者暴露服务的主过程：** `参考dubbo架构分层`，`主要分4步`。    
    ![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-29.png)   
    ![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-53.png)   
    1. ServiceConfig将Bean对象解析成URL格式。  
    2. `服务代理层proxy`：通过ProxyFactory类的getInvoker方法使用ref实际类生成一个AbstractProxyInvoker实例。`ProxyFactory #getInvoker(T proxy, Class<T> type, URL url)`  
    3. `远程调用层protocol`：通过Protocol协议类的export方法暴露服务。`DubboProtocol #export(Invoker<T> invoker)`  
        1. 本地各种协议暴露。  
        2. 注册中心暴露。  
    4. 如果通过注册中心暴露服务，RegistryProtocol保存URL地址和invoker的映射关系，同时注册到服务中心。  
3. **服务消费者引用服务的主过程：** `与服务暴露相反`  
    ![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-30.png)   
    ![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-54.png)   
    1. ReferenceConfig解析引用的服务。  
    2. ReferenceConfig类的init方法调用Protocol的refer方法生成Invoker实例。  
    3. 把Invoker转换为客户端需要的接口。  
4. Dubbo和ZK  
&emsp; Dubbo在ZK上的节点 /dubbo/xxxService 节点是持久节点。  


#### 1.8.6.1. Dubbo序列化和协议
1. 不同服务在性能上适用不同协议进行传输，比如`大数据用短连接协议`，`小数据大并发用长连接协议`。  
2. 默认使用Hessian序列化（跨语言），还有Duddo、FastJson、Java自带序列化。   

##### 1.8.6.1.1. Dubbo协议长连接和心跳
###### 1.8.6.1.1.1. 协议长链接
&emsp; Dubbo缺省协议采用单一长连接和NIO异步通讯，适合于小数据量大并发的服务调用，以及服务消费者机器数远大于服务提供者机器数的情况。  
&emsp; 注意：Dubbo缺省协议不适合传送大数据量的服务，比如传文件，传视频等，除非请求量很低。  
&emsp; Dubbo协议采用长连接，还可以防止注册中心宕机风险。  

#### 1.8.6.2. Dubbo心跳机制  
&emsp; `主流的RPC框架都会追求性能选择使用长连接`，所以如何`保活连接`就是一个重要的话题。如何确保连接的有效性呢，在TCP中用到了KeepAlive机制。有了KeepAlive机制往往是不够用的，还需要配合心跳机制来一起使用。  
&emsp; 何为心跳机制，简单来讲就是客户端启动一个定时器用来定时发送请求，服务端接到请求进行响应，如果多次没有接受到响应，那么客户端认为连接已经断开，可以断开半打开的连接或者进行重连处理。   
&emsp; `Dubbo的心跳方案：Dubbo对于建立的每一个连接，同时在客户端和服务端开启了2个定时器，一个用于定时发送心跳，一个用于定时重连、断连，执行的频率均为各自检测周期的 1/3。`定时发送心跳的任务负责在连接空闲时，向对端发送心跳包。定时重连、断连的任务负责检测 lastRead 是否在超时周期内仍未被更新，如果判定为超时，客户端处理的逻辑是重连，服务端则采取断连的措施。  

### 1.8.7. 服务调用
#### 1.8.7.1. 服务调用介绍


#### 1.8.7.2. Dubbo集群容错
1. 服务降级  
    &emsp; 最主要的两种形式是：  
    &emsp; 1） mock='force:return+null'表示消费对该服务的方法调用都直接返回null值，不发起远程调用。用来屏蔽不重要服务不可用时对调用方的影响。  
    &emsp; 2） 还可以改为mock=fail:return+null表示消费方对该服务的方法调用在失败后，再返回null。用来容忍不重要服务不稳定时对调用方的影响。  
2. 集群容错策略  
    &emsp; 下面列举dubbo支持的容错策略：  

    * Failover(默认) - 失败自动切换，当出现失败，重试其它服务器。通常用于读操作，但重试会带来更长延迟。可通过 retries="2" 来设置重试次数(不含第一次)。  
    * Failfast - 快速失败，只发起一次调用，失败立即报错。通常用于非幂等性的写操作，比如新增记录。
    * Failsafe - 失败安全，出现异常时，直接忽略。通常用于写入审计日志等操作。  
    * Failback - 失败自动恢复，后台记录失败请求，定时重发。通常用于消息通知操作。  
    * Forking - 并行调用多个服务器，只要一个成功即返回。通常用于实时性要求较高的读操作，但需要浪费更多服务资源。可通过 forks="2" 来设置最大并行数。  
    * Broadcast - 广播调用所有提供者，逐个调用，任意一台报错则报错。通常用于通知所有提供者更新缓存或日志等本地资源信息。  
3. 负载均衡  
    * <font color = "red">Random(缺省)，随机，按权重设置随机概率。</font>在一个截面上碰撞的概率高，但调用量越大分布越均匀，而且按概率使用权重后也比较均匀，有利于动态调整提供者权重。  
    * <font color = "red">RoundRobin，轮循，按公约后的权重设置轮循比率。</font>  
    &emsp; 轮询负载均衡算法的不足：存在慢的提供者累积请求的问题，比如：第二台机器很慢，但没挂，当请求调到第二台时就卡在那，久而久之，所有请求都卡在调到第二台上。  
    * <font color = "red">LeastActive，最少活跃调用数，活跃数指调用前后计数差。</font>相同活跃数的随机。使慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大。  
    * <font color = "clime">ConsistentHash，[分布式一致性哈希算法](/docs/microService/thinking/分布式算法-consistent.md)。</font>相同参数的请求总是发到同一提供者；当某一台提供者崩溃时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动。  
    * 缺省只对第一个参数Hash，如果要修改，请配置`<dubbo:parameter key="hash.arguments" value="0,1" />`  
    * 缺省用160份虚拟节点，如果要修改，请配置`<dubbo:parameter key="hash.nodes" value="320" />`  


### 1.8.8. ~~扩展点加载(SPI)~~  
1. JDK SPI的缺点：  
&emsp; JDK标准的SPI会一次性实例化扩展点所有实现。如果扩展点加载失败，连扩展点的名称都拿不到了。   
&emsp; 而Dubbo的SPI不会一次性全部加载并实例化，它会按需加载（指定、自适应、自动激活）。  

    1. JDK标准的SPI会一次性实例化扩展点的所有实现。而Dubbo SPI能实现按需加载
    2. Dubbo SPI增加了对扩展点Ioc和Aop的支持

2. 扩展点自适应  
&emsp; 使用@Adaptive注解，动态的通过URL中的参数来确定要使用哪个具体的实现类。  
3. 扩展点自动激活  
&emsp; 使用@Activate注解，可以标记对应的扩展点默认被激活使用，可以通过指定group或者value，在不同条件下获取自动激活的扩展点。  
&emsp; Dubbo的激活扩展是指根据分组和url参数中的key，结合扩展类上的注解Activate，生成符合匹配条件的扩展实例，得到一个实例集合。  
&emsp; 激活扩展在Dubbo中一个典型的应用场景就是过滤器(Filter), 在服务端收到请求之后，经过一系列过滤器去拦截请求，做一些处理工作，然后在真正去调用实现类。  


----------

0. ExtensionLoader是Dubbo SPI中用来加载扩展类的，有如下三个重要方法，搞懂这3个方法基本上就搞懂Dubbo SPI了。加载扩展类的三种方法如下
    * getExtension()，获取普通扩展类
    * getAdaptiveExtension()，获取自适应扩展类
    * getActivateExtension()，获取自动激活的扩展类
2. 扩展点特性
    * 扩展点自动包装，Wrapper机制
    * 扩展点自动装配
    * 扩展点自适应
    * 扩展点自动激活
1. **<font color = "clime">~~Dubbo改进了JDK标准的SPI的以下问题：~~</font>**  
    * JDK标准的SPI会一次性实例化扩展点所有实现。 将该实现类直接作为默认实现，不再自动生成代码 标记在方法上：生成接口对应的Adaptive类，通过url中的参数来确定最终的实现类。    
    * 如果扩展点加载失败，连扩展点的名称都拿不到了。  
    * 增加了对扩展点IoC和AOP的支持，一个扩展点可以直接setter注入其它扩展点。  
2. dubbo的spi有如下几个概念：  
&emsp; （1）扩展点：一个接口。  
&emsp; （2）扩展：扩展（接口）的实现。  
&emsp; （3）扩展自适应实例：其实就是一个Extension的代理，它实现了扩展点接口。在调用扩展点的接口方法时，会根据实际的参数来决定要使用哪个扩展。dubbo会根据接口中的参数，自动地决定选择哪个实现。  
&emsp; （4）@SPI：该注解作用于扩展点的接口上，表明该接口是一个扩展点。  
&emsp; （5）@Adaptive：@Adaptive注解用在扩展接口的方法上。表示该方法是一个自适应方法。Dubbo在为扩展点生成自适应实例时，如果方法有@Adaptive注解，会为该方法生成对应的代码。   
3. Dubbo中的扩展点有哪些？  
&emsp; [Dubbo中已经实现的扩展](https://dubbo.apache.org/zh/docs/v2.7/dev/impls/)：  
&emsp; 协议扩展 、调用拦截扩展 、引用监听扩展 、暴露监听扩展 、集群扩展 、路由扩展 、负载均衡扩展 、合并结果扩展 、注册中心扩展 、监控中心扩展 、扩展点加载扩展 、动态代理扩展 、编译器扩展 、Dubbo 配置中心扩展 、消息派发扩展 、线程池扩展 、序列化扩展 、网络传输扩展 、信息交换扩展 、组网扩展 、Telnet 命令扩展 、状态检查扩展 、容器扩展 、缓存扩展 、验证扩展 、日志适配扩展。  

--------

2. **<font color = "clime">SpringApplication初始化中第4步和第5步都是利用SpringBoot的[SPI机制](/docs/java/basis/SPI.md)来加载扩展实现类。SpringBoot通过以下步骤实现自己的SPI机制：</font>**  
	1. 首先获取线程上下文类加载器;  
	2. 然后利用上下文类加载器从spring.factories配置文件中加载所有的SPI扩展实现类并放入缓存中；  
	3. 根据SPI接口从缓存中取出相应的SPI扩展实现类；  
	4. 实例化从缓存中取出的SPI扩展实现类并返回。  


### 1.8.9. Dubbo和Netty  


### Dubbo总结  
1. dubbo初始化：Dubbo和Spring  
2. dubbo的十层架构  
3. dubbo和netty  

## 1.9. Zookeeper
&emsp; **<font color = "clime">Zookeeper是一个分布式协调服务的开源框架。主要用来解决分布式集群中应用系统的一致性问题。</font>**  

### 1.9.1. ZK服务端
1. `ZK服务端`通过`ZAB协议`保证`数据顺序一致性`。  
3. **<font color = "clime">`消息广播（数据读写流程）：`</font>**  
    &emsp; 在zookeeper中，客户端会随机连接到zookeeper集群中的一个节点。    
    * 如果是读请求，就直接从当前节点中读取数据。  
    * 如果是写请求，那么请求会被转发给 leader 提交事务，然后leader会广播事务，只要有超过半数节点写入成功，那么写请求就会被提交。   
    &emsp; ⚠️注：leader向follower写数据详细流程：类2pc(两阶段提交)。  
2. 数据一致性  
    &emsp; **<font color = "red">Zookeeper保证的是CP，即一致性（Consistency）和分区容错性（Partition-Tolerance），而牺牲了部分可用性（Available）。</font>**  
    * 为什么不满足AP模型？<font color = "red">zookeeper在选举leader时，会停止服务，直到选举成功之后才会再次对外提供服务。</font>
    * Zookeeper的CP模型：非强一致性， **<font color = "clime">而是单调一致性/顺序一致性。</font>**  
        1. <font color = "clime">假设有2n+1个server，在同步流程中，leader向follower同步数据，`当同步完成的follower数量大于n+1时同步流程结束，系统可接受client的连接请求。`</font><font color = "red">`如果client连接的并非同步完成的follower，那么得到的并非最新数据，但可以保证单调性。`</font> 未同步数据的情况，Zookeeper提供了同步机制（可选型），类似回调。   
        2. follower接收写请求后，转发给leader处理；leader完成两阶段提交的机制。向所有server发起提案，当提案获得超过半数(n+1)的server认同后，将对整个集群进行同步，超过半数(n+1)的server同步完成后，该写请求完成。如果client连接的并非同步完成follower，那么得到的并非最新数据，但可以保证单调性。  
1. Zookeeper集群角色：  
    * 领导者Leader：同一时间，集群只允许有一个Leader，提供对客户端的读写功能，负责将数据同步至各个节点；  
    * 跟随者Follower：提供对客户端读功能，写请求则转发给Leader处理，当Leader崩溃失联之后参与Leader选举；  
    * 观察者Observer：与Follower不同的是不参与Leader选举。  
2. **<font color = "clime">崩溃恢复</font>**  
    * 服务器启动时的leader选举：每个server发出投票，投票信息包含(myid, ZXID,epoch) ---> 接受投票 ---> 处理投票(epoch>ZXID>myid) ---> 统计投票 ---> 改变服务器状态。</font>  
        4. 统计投票。每次投票后，服务器都会统计投票信息，判断是否已经有过半机器接受到相同的投票信息，对于 Server1、Server2 而言，都统计出集群中已经有两台机器接受了(2, 0)的投票信息，此时便认为已经选出了 Leader。  
        5. 改变服务器状态。一旦确定了 Leader，每个服务器就会更新自己的状态，如果是 Follower，那么就变更为 FOLLOWING，如果是 Leader，就变更为 LEADING。  
    * 运行过程中的leader选举：变更状态 ---> 发出投票 ---> 处理投票 ---> 统计投票 ---> 改变服务器的状态。
2. 服务端脑裂：过半机制，要求集群内的节点数量为2N+1。  

### 1.9.2. ZK客户端
&emsp; zookeeper引入了`watcher机制`来实现`客户端和服务端`的发布/订阅功能。  
1. ~~Watcher机制运行流程：Zookeeper客户端向服务端的某个Znode注册一个Watcher监听，当服务端的一些指定事件触发了这个Watcher，服务端会向指定客户端发送一个事件通知来实现分布式的通知功能，然后客户端根据Watcher通知状态和事件类型做出业务上的改变。  
&emsp; 触发watch事件种类很多，如：节点创建，节点删除，节点改变，子节点改变等。~~  
&emsp; 概括可以分为三个过程：1. 客户端注册 Watcher；2. 服务端处理 Watcher；3. 客户端回调 Watcher。  
![image](http://182.92.69.8:8081/img/microService/zookeeper/zk-5.png)  
![image](http://182.92.69.8:8081/img/microService/zookeeper/zk-6.png)  
&emsp; 大致流程就是 Client 向ZK中注册 Watcher，如果注册成功的话，会将对应的 Watcher 存储在本地。当ZK服务器端触发 Watcher 事件之后，会向客户端发送通知，`客户端会从 ClientWatchManager 中取出对应的 Watcher 进行回调。`  
2.  **watch的重要特性：**  
    * 异步发送
    * 一次性触发：  
    &emsp; Watcher通知是一次性的， **<font color = "clime">即一旦触发一次通知后，该Watcher就失效了，因此客户端需要反复注册Watcher。</font>** 但是在获取watch事件和设置新的watch事件之间有延迟。延迟为毫秒级别，理论上会出现不能观察到节点的每一次变化。  
    &emsp; `不支持用持久Watcher的原因：`如果Watcher的注册是持久的，那么必然导致`服务端的每次数据更新都会通知到客户端。这在数据变更非常频繁且监听客户端特别多的场景下，ZooKeeper无法保证性能。`  
    * 有序性：  
    &emsp; 客户端先得到watch通知才可查看节点变化结果。  
3. 客户端过多，会引发网络风暴。  

### 1.9.3. ZK应用场景和弊端
1. Zookeeper应用场景：统一命名服务，生成分布式ID、分布式锁、队列管理、元数据/配置信息管理，数据发布/订阅、分布式协调、集群管理，HA高可用性。  
2. ZK的弊端：
	1. 服务端从节点多，主从同步慢。  
	2. 客户端多，`网络风暴`。~~watcher机制中，回调流程，只有主节点参与？~~  
3. `ZK羊群效应`
    1. 什么是羊群效应？  
    &emsp; 羊群效应理论（The Effect of Sheep Flock），也称羊群行为（Herd Behavior）、从众心理。 羊群是一种很散乱的组织，平时在一起也是盲目地左冲右撞，但一旦有一只头羊动起来，其他的羊也会不假思索地一哄而上，全然不顾旁边可能有的狼和不远处更好的草。  
    &emsp; 当多个客户端请求获取zk创建临时节点来进行加锁的时候，会进行竞争，因为zk独有的一个特性：即watch机制。啥意思呢？就是当A获取锁并加锁的时候，B会监听A的结点变化，当A创建的临时结点被删除的时候，B会去竞争锁。懂了没？  
    &emsp; 那么问题来了？如果同时有1000个客户端发起请求并创建临时节点，都会去监听A结点的变化，然后A删除节点的时候会通知其他节点，这样是否会太影响并耗费资源了？  
    2. 解决方案  
        &emsp; 在使用ZK时，要尽量避免出现羊群效应。但是如果出现了该怎么解决？  
        1. 如果ZK是用于实现分布式锁，使用临时顺序节点。 ~~未获取到锁的客户端给自己的上一个临时有序节点添加监听~~    
        2. 如果ZK用于其他用途，则分析出现羊群效应的问题，从根本上解决问题或提供其他替代ZK的方案。  

## 1.10. 分布式
### 1.10.1. 分布式理论CAP
1. CAP：一致性(Consistency)、可用性(Availability)、分区容错性(Partition tolerance)。  
&emsp; 一致性模型：强一致性、弱一致性、最终一致性、单调一致性/顺序一致性、会话一致性。  
2. BASE：**是Basically Available(基本可用)、Soft state(软状态)和Eventually consistent(最终一致性)三个短语的缩写。<font color = "red">`BASE是对CAP中AP的一个扩展`，是对CAP中一致性和可用性权衡的结果。</font>**  
3. **<font color = "blue">无处不在的CAP的C</font>**  
&emsp; 只要是分布式或集群，甚至一个接口中处在不同事务的调用，都会有数据一致性的问题。 例如MySql主从复制、binlog和redolog的两阶段提交......  

### 1.10.2. 分布式ID
1. 分布式ID的基本生成方式有：UUID，数据库方式（主键自增、序列号、<font color = "clime">号段模式</font>），<font color = "red">redis、ZK等中间件，雪花算法。</font>  
    * 数据库Oracle中有序列SEQUENCE；在Mysql中可以建一张伪序列号表。  
    * 号段模式可以理解为从数据库批量的获取自增ID，每次从数据库取出一个号段范围。  
2. snowflake算法：  
    1. snowflake算法所生成的ID结构：正数位(占1比特)+ 时间戳(占41比特)+ 机器ID(占5比特)+ 数据中心(占5比特)+ `自增值(占12比特)`，总共64比特组成的一个Long类型。可以根据自身业务特性分配bit位，非常灵活。
    2. ID呈趋势递增。  
    3. **snowflake算法缺点：** 
        1. <font color="red">依赖于系统时钟的一致性。如果某台机器的系统时钟回拨，有可能造成ID冲突，或者ID乱序。</font>  
        2. 百度UidGenerator如果时间有任何的回拨，那么直接抛出异常。此外UidGenerator通过消费未来时间克服了雪花算法的并发限制。   
        3. **<font color = "clime">时钟回拨问题解决方案：</font>**    
        &emsp; **<font color = "red">雪花算法中，第53-64的bit位：这12位表示序列号，也就是单节点1毫秒内可以生成2^12=4096个不同的ID。发生时钟回拨：</font>**  
            1. 抛异常。  
            2. `可以对给定的基础序列号稍加修改，后面每发生一次时钟回拨就将基础序列号加上指定的步长，`例如开始时是从0递增，发生一次时钟回拨后从1024开始递增，再发生一次时钟回拨则从2048递增，这样还能够满足3次的时钟回拨到同一时间点（发生这种操作就有点扯了）。  
            3. 当业务不是很繁忙，可以将12位序列号预留两位。2位的扩展位允许有3次大的时钟回拨，如果其超过三次可以打印异常。  

### 1.10.3. 分布式事务
#### 1.10.3.1. 事务模型DTP及XA
1. X/Open DTP中的角色：
    * 应用程序
    * 事务管理器（协调者），<font color = "clime">管理全局事务，协调事务的提交或者回滚，并协调故障恢复。</font>常见的事务管理器(TM)是交易中间件。  
    * 资源管理器（参与者），能够提供单数据源的事务能力，它们通过XA接口，将本数据库的提交、回滚等能力提供给事务管理器调用。
    * XA接口  
    &emsp; XA是DTP模型定义的接口，指的是模型中TM（事务管理器）和RM（资源管理器）之间进行通信的接口，用于向事务管理器提供该资源管理器（该数据库）的提交、回滚等能力。目前大多数实现XA的都是数据库或者MQ，所以提起`XA往往多指基于资源层的底层分布式事务解决方案`。其实现在也有些数据分片框架或者中间件也支持XA协议，毕竟它的兼容性、普遍性更好。  
2. **2PC：** 
    1. 流程：  
        ![image](http://182.92.69.8:8081/img/microService/problems/problem-4.png)  
        &emsp; <font color = "clime">二阶段提交引入协调者，将`事务的提交`拆分为2个阶段：准备阶段prepare和提交阶段commit/rollback。</font>  
        &emsp; 尽量（不能100%）保证了数据的强一致，适合对数据强一致要求很高的关键领域。  
    2. **XA二阶段问题：** 
        1. `正常运行：`同步阻塞问题。执行过程中，所有参与节点都是事务阻塞型的。`当参与者占有公共资源时，其他第三方节点访问公共资源不得不处于阻塞状态。`   
        3. 丢失消息导致的数据不一致问题。在二阶段提交的阶段二中，<font color = "clime">当协调者向参与者发送commit请求之后，发生了局部网络异常或者在发送commit请求过程中协调者发生了故障，这会导致只有一部分参与者接受到了commit请求。</font>  
        1. 单点故障问题。  
        &emsp; 参与者发生故障。协调者需要给每个参与者额外指定超时机制，超时后整个事务失败。  
        &emsp; 协调者发生故障。参与者会一直阻塞下去。需要额外的备机进行容错。  
        4. 二阶段无法解决的问题：协调者和参与者同时宕机。  
3. **~~3PC：解决2PC的问题。~~**  
    1. 3PC也就是多了一个阶段（一个询问的阶段），分别是准备、预提交和提交这三个阶段。  
        ![image](http://182.92.69.8:8081/img/microService/problems/problem-5.png)  
        1. 准备阶段单纯就是协调者去访问参与者，类似于“你还好吗？能接请求不”。  
        2. 预提交其实就是2PC的准备阶段，除了事务的提交啥都干了。  
        3. 提交阶段和2PC的提交一致。
    2. **XA三阶段特点：多一个阶段、  
        &emsp; <font color = "clime">3PC解决2PC的问题：降低了参与者的阻塞范围，~~解决单点故障问题~~，解决协调者和参与者同时挂掉的问题，减少了数据不一致的情况。</font>**  
        1. <font color = "clime">【超时机制】也降低了参与者的阻塞范围，因为参与者不会一直持有事务资源并处于阻塞状态。</font>  
        &emsp; 相比较2PC而言，3PC对于协调者（Coordinator）和参与者（Partcipant）都设置了超时时间，而2PC只有协调者才拥有超时机制。这解决了一个什么问题呢？   
        &emsp; 这个优化点，主要是避免了参与者在长时间无法与协调者节点通讯（协调者挂掉了）的情况下，无法释放资源的问题，因为参与者自身拥有超时机制会在超时后，自动进行本地commit从而进行释放资源。而这种机制也侧面降低了整个事务的阻塞时间和范围。  
        1. `解决单点故障问题`  
        &emsp; ~~引入超时机制。**<font color = "red">同时在协调者和参与者中都引入超时机制。一旦事物参与者迟迟没有接到协调者的commit请求，`会自动进行本地commit`。3PC追求的是最终一致性。这样也有效解决了协调者单点故障的问题。</font>**~~  
        &emsp; 2pc协议在协调者和执行者同时宕机时（协调者和执行者不同时宕机时，都能确定事务状态），选出协调者之后无法确定事务状态，会等待宕机者恢复才会继续执行（无法利用定时器来做超时处理，超时后也不知道事务状态，无法处理，强制处理会导致数据不一致），这段时间这个事务是阻塞的，其占用的资源不会被释放。  
        &emsp; `3pc感知事物状态。` **<font color = "clime">只要有一个事务进入PreCommit，说明各执行节点的状态都是canCommit。</font>**  
        3. <font color = "red">数据不一致问题依然存在。</font>当参与者收到preCommit请求后等待do commite指令时，此时如果协调者请求`中断事务`，而协调者无法与参与者正常通信，会导致参与者继续提交事务，造成数据不一致。  
        ~~3PC 虽然解决了 Coordinator 与参与者都异常情况下导致数据不一致的问题，3PC 依然带来其他问题：比如，网络分区问题，在 preCommit 消息发送后突然两个机房断开，这时候 Coordinator 所在机房会 abort, 另外剩余参与者的机房则会 commit。~~  

#### 1.10.3.2. TCC
##### 1.10.3.2.1. TCC流程
1. **<font color = "red">TCC是一种`业务层面或者是应用层`的`两阶段、【补偿型】`的事务。</font>**  
2. TCC是`Try（检测及资源锁定或者预留）`、Commit（确认）、Cancel（取消）的缩写，业务层面需要写对应的三个方法。  
![image](http://182.92.69.8:8081/img/microService/problems/problem-9.png)  
3. 一个支付订单的场景：    
    * Try
        * 订单服务，订单状态修改为UPDATING，也就是修改中的意思
        * 库存服务，别直接扣减库存，可以冻结掉库存。
        * 积分服务，别直接给用户增加会员积分，可以先在积分表里的一个预增加积分字段加入积分。
    * Confirm/Cancel
        * 订单服务，
        * 库存服务，
        * 积分服务，
3. TCC与二阶段比较  
&emsp; 使用2PC机制时，以提交为例，一个完整的事务生命周期是：begin -> 业务逻辑 -> prepare -> commit。  
&emsp; 使用TCC机制时，以提交为例，一个完整的事务生命周期是：begin -> 业务逻辑(try业务) -> commit(comfirm业务)。  
&emsp; 综上，可以从执行的阶段上将二者一一对应起来：  
&emsp; 1、2PC机制的业务阶段 等价于 TCC机制的try业务阶段；  
&emsp; 2、2PC机制的提交阶段（prepare & commit） 等价于 TCC机制的提交阶段（confirm）；  
&emsp; 3、2PC机制的回滚阶段（rollback） 等价于 TCC机制的回滚阶段（cancel）。  
&emsp; 因此，可以看出，虽然TCC机制中有两个阶段都存在业务逻辑的执行，但其中 `try业务阶段其实是与全局事务处理无关的`。认清了这一点，当再比较TCC和2PC时，就会很容易地发现，`TCC不是两阶段提交，而只是它对事务的提交/回滚是通过执行一段confirm/cancel业务逻辑来实现，仅此而已。`  

##### 1.10.3.2.2. TCC问题
&emsp; 在微服务架构下，很有可能出现网络超时、重发，机器宕机等一系列的异常情况。一旦遇到这些情况，就会导致分布式事务执行过程出现异常。  

* 允许空回滚
	* 产生：注册分支事务是在调用 RPC 时，Seata 框架的切面会拦截到该次调用请求， **<font color = "red">先向 TC 注册一个分支事务，然后才去执行 RPC 调用逻辑。如果 RPC 调用逻辑有问题，`比如调用方机器宕机、网络异常，都会造成 RPC 调用失败，即【未执行 Try 方法】。但是分布式事务已经开启了，需要推进到终态，因此，TC 会回调参与者二阶段 Cancel 接口，从而形成空回滚。`</font>**  
	* 解决方案： **<font color = "clime">需要一张额外的事务控制表，其中有分布式事务 ID 和分支事务 ID，第一阶段 Try 方法里会插入一条记录，表示一阶段执行了。Cancel 接口里读取该记录，如果该记录存在，则正常回滚；`如果该记录不存在，则是空回滚。`</font>**  
* 接口幂等
	* 产生
	* 解决方案：在事务控制表上加一个状态字段，用来记录每个分支事务的执行状态。
* 防止资源悬挂
	* 产生： **<font color = "clime">因为允许空回滚的原因，Cancel 接口认为 Try 接口没执行，空回滚直接返回成功，对于 Seata 框架来说，认为分布式事务的二阶段接口已经执行成功，整个分布式事务就结束了。但是这之后 Try 方法才真正开始执行，预留业务资源，前面提到事务并发控制的业务加锁，对于一个 Try 方法预留的业务资源，只有该分布式事务才能使用，然而 Seata 框架认为该分布式事务已经结束，也就是说，当出现这种情况时，该分布式事务第一阶段预留的业务资源就再也没有人能够处理了，对于这种情况，就称为悬挂，即业务资源预留后没法继续处理。</font>**    
	&emsp; **<font color = "clime">什么样的情况会造成悬挂呢？按照前面所讲，在 RPC 调用时，先注册分支事务，再执行 RPC 调用，如果此时 RPC 调用的网络发生拥堵，通常 RPC 调用是有超时时间的，RPC 超时以后，发起方就会通知 TC 回滚该分布式事务，`可能回滚完成后，RPC 请求才到达参与者，真正执行，从而造成悬挂。`</font>**  
	* 解决方案：`可以在二阶段执行时插入一条事务控制记录，状态为已回滚，这样当一阶段执行时，先读取该记录，如果记录存在，就认为二阶段已经执行；否则二阶段没执行。`   

##### 1.10.3.2.3. TCC优化
&emsp; ......

#### 1.10.3.3. Saga
1. Saga是一种解决长事务的分布式事务方案。Saga模型将一个分布式事务拆分为多个本地事务，也是一种二阶段补偿性事务（⚠️`注：二阶段提交和二阶段补偿的区别`）。  
2. Saga执行流程：  
    1. Saga有两种执行方式：  
        * 编排（Choreography）：每个服务产生并聆听其他服务的事件，并决定是否应采取行动。  
        &emsp; `该实现第一个服务执行一个事务，然后发布一个事件。` **<font color = "red">该事件被一个或多个服务进行监听，</font>** 这些服务再执行本地事务并发布(或不发布)新的事件，`当最后一个服务执行本地事务并且不发布任何事件时，意味着分布式事务结束，或者它发布的事件没有被任何Saga参与者听到都意味着事务结束。`  
        * 控制（Orchestration）：saga协调器orchestrator以命令/回复的方式与每项服务进行通信，告诉服务应该执行哪些操作。  
    2. 有两种恢复策略：  
        * <font color = "red">backward recovery，向后恢复，补偿所有已完成的事务。</font>  
        * <font color = "red">forward recovery，向前恢复，重试失败的事务，假设每个子事务最终都会成功。</font>  
3. **<font color = "blue">Saga和TCC比较：</font>**  
    1. **<font color = "red">Saga没有“预留”Try行为（`直接进入终态`），每个子事务（本地事务）依次执行提交阶段，所以会留下原始事务操作的痕迹，</font>** Cancel属于不完美补偿，需要考虑对业务上的影响。  
    2. Saga和TCC一样需要注意3个问题：1)保持幂等性；2)允许空补偿；3)防止资源悬挂。

#### 1.10.3.4. ~~消息模式，异步方式~~

&emsp; `XA、TCC、Saga等都属于同步方式。还有基于消息的异步方式。`  
1. 依据是否保证投递到订阅者，分为可靠消息及最大努力交付消息。`可靠消息的两种实现方案：基于本地消息表、基于事务消息。`    
1. 可靠消息
    1. **基于本地消息表：**  
    &emsp; 基本思想：调用方存储业务表和消息表；调用其他服务，如果成功修改消息表状态， **<font color = "clime">如果失败发起重试（重试方式可以基于定时任务、mq等）。</font>**    
    2. **基于mq提供的事务消息：** ~~相比于最终一致性方案可靠性高，但也非强一致性。~~   

        ![image](http://182.92.69.8:8081/img/microService/problems/problem-48.png)  
        1. 调用方开启消息事务；
        2. 发送消息；
        3. 调用方执行本地事务；  
        4. 调用方提交或回滚事务消息。
3. **~~最大努力通知方案和可靠消息最终一致性方案：~~**  
&emsp; 最大努力通知，即mq的一般使用；可靠消息，即保证消息不丢失。  
&emsp; 可靠消息最终一致性方案可以保证的是只要系统A的事务完成，通过不停（无限次）重试来保证系统B的事务总会完成。但是最大努力方案就不同，如果系统B本地事务执行失败了，那么它会重试N次后就不再重试，系统B的本地事务可能就不会完成了。 


#### 1.10.3.5. 分布式事务框架Seata
##### 1.10.3.5.1. AT模式详解
###### 1.10.3.5.1.1. AT模式流程
&emsp; AT 模式分为两个阶段：

1. 一阶段：执行用户SQL。`业务数据和回滚日志记录在同一个本地事务中提交，`释放本地锁和连接资源。    
2. 二阶段：Seata框架自动生成。commit异步化快速完成；rollback通过一阶段的回滚日志进行反向补偿。    

![image](http://182.92.69.8:8081/img/microService/problems/problem-68.png)  
&emsp; 下面通过一个分支事务的执行过程来了解 Seata 的工作流程。  
&emsp; 例如有一个业务表 product(id,name)，分支事务的业务逻辑：  

```sql
update product set name = 'GTS' where name = 'TXC';
```

1. 一阶段  
    （1）解析 SQL  
    &emsp; 得到 SQL 的类型（UPDATE），表（product），条件（where name = 'TXC'）等相关的信息。  
    （2）查询前镜像  
    &emsp; 根据解析得到的条件信息，生成查询语句，定位数据。  

    ```sql
    select id, name from product where name = 'TXC';  
    ```
    &emsp; 得到前镜像：  

    |id|name|
    |---|---|
    |1|TXC|

    （3）执行业务 SQL  
    &emsp; 执行自己的业务逻辑：  
    ```sql  
    update product set name = 'GTS' where name = 'TXC';  
    ```
    &emsp; 把 name 改为了 GTS。  
    （4）查询后镜像  
    &emsp; 根据前镜像的结果，通过 主键 定位数据。  

    ```sql
    select id, name from product where id = 1;
    ```
    &emsp; 得到后镜像：

    |id|name|
    |---|---|
    |1|GTS|

    （5）插入回滚日志  
    &emsp; 把前后镜像数据以及业务 SQL 相关的信息组成一条回滚日志记录，插入到 UNDO_LOG 表中。  
    （6）提交前，向 TC 注册分支：申请 product 表中，主键值等于 1 的记录的 全局锁 。  
    （7）本地事务提交：业务数据的更新和前面步骤中生成的 UNDO LOG 一并提交。  
    （8）将本地事务提交的结果上报给 TC。  
2. 二阶段 - 提交  
（1）收到 TC 的分支提交请求，把请求放入一个异步任务的队列中，马上返回提交成功的结果给 TC。  
（2）异步任务阶段的分支提交请求，将异步和批量地删除相应 UNDO LOG 记录。  
3. 二阶段 - 回滚  
（1）收到 TC 的分支回滚请求，开启一个本地事务，执行如下操作。  
（2）通过 XID 和 Branch ID 查找到相应的 UNDO LOG 记录。  
（3）数据校验  
&emsp; 拿 UNDO LOG 中的后镜与当前数据进行比较，根据校验结果决定是否做回滚。  
（4）根据 UNDO LOG 中的前镜像和业务 SQL 的相关信息生成并执行回滚的语句：  
```sql
update product set name = 'TXC' where id = 1;
```
（5）提交本地事务  
&emsp; 并把本地事务的执行结果（即分支事务回滚的结果）上报给 TC。  

###### 1.10.3.5.1.2. AT缺点


##### 1.10.3.5.2. Seata四种模式的区别
&emsp; 四种分布式事务模式，分别在不同的时间被提出，每种模式都有它的适用场景：  

* AT 模式是无侵入的分布式事务解决方案，适用于不希望对业务进行改造的场景，几乎0学习成本。  
* TCC 模式是高性能分布式事务解决方案，适用于核心系统等对性能有很高要求的场景。  
* Saga 模式是长事务解决方案，适用于业务流程长且需要保证事务最终一致性的业务系统， **<font color = "red">Saga 模式一阶段就会提交本地事务，无锁，长流程情况下可以保证性能，</font>** `多用于渠道层、集成层业务系统。事务参与者可能是其它公司的服务或者是遗留系统的服务，无法进行改造和提供 TCC 要求的接口，也可以使用 Saga 模式。`  
* XA模式是分布式强一致性的解决方案，但性能低而使用较少。  


#### 1.10.3.6. 分布式事务的选型
&emsp; 分布式事务的选型：(`~~判断事务发起方与事务被调用方的关系、数据一致的要求、性能~~`)  
* **<font color = "clime">`事务被调用方跟随事务发起方，使用最终一致性的消息事务；`(基于消息实现的事务适用于分布式事务的提交或回滚只取决于事务发起方的业务需求)</font>** `例如消费长积分`  
* **<font color = "clime">`事务被调用方与事务发起方协助完成功能，使用补偿性事务；`</font>** `例如库存服务和支付服务` 
    * 事务被调用方与事务发起方`协助完成`功能，事务被调用方与事务发起方的数据保持一致性，使用强一致性的TCC；  
    * SAGA可以看做一个异步的、利用队列实现的补偿事务。<font color = "red">适用于不需要同步返回发起方执行最终结果、可以进行补偿、对性能要求较高、不介意额外编码的业务场景。</font>  
* **<font color = "clime">单体服务，多数据源，使用XA协议的服务；</font>**  

### 1.10.4. 分布式锁
#### 1.10.4.1. 分布式锁介绍
1. 分布式锁使用场景
    1. 避免不同节点重复相同的工作，实现幂等。  
    2. 避免破坏数据的正确性：在分布式环境下解决多实例对数据的访问一致性。如果多个节点在同一条数据上同时进行操作，可能会造成数据错误或不一致的情况出现。  
2. 实现分布式锁的细节：  
    * **<font color = "blue">确保互斥：在同一时刻，必须保证锁至多只能被一个客户端持有。</font>**  
    * **<font color = "clime">`不能死锁：`在一个客户端在持有锁的期间崩溃而没有主动解锁情况下，`也能保证后续其他客户端能加锁`。</font>**    
    * **<font color = "clime">`避免活锁：`在获取锁失败的情况下，`反复进行重试操作，占用CPU资源，影响性能。`</font>**    
    * 实现更多锁特性：锁中断、锁重入、锁超时等。确保客户端只能解锁自己持有的锁。  
 

#### 1.10.4.2. RedisLock
2. redis实现分布式锁方案：  
    * 方案一：SETNX + EXPIRE  
    * 方案二：SETNX + value值是（系统时间+过时时间）  
    * `方案三：使用Lua脚本（包含SETNX + EXPIRE两条指令）` 
    * 方案四：SET的扩展命令（SET EX PX NX）  
    * `方案五：SET EX PX NX + 校验惟一随机值，再删除释放`  
    * `方案六: 开源框架: Redisson`  
    * `方案七：多机实现的分布式锁Redlock`   
3. `方案五：使用SET EX PX NX + 校验惟一随机值加锁；再删除释放（先查询再删除，非原子操作，需要使用lua脚本保证其原子性）。`  
&emsp; 在这里，判断是否是当前线程加的锁和释放锁不是一个原子操做。若是调用jedis.del()释放锁的时候，可能这把锁已经不属于当前客户端，会解除他人加的锁。  
![image](http://182.92.69.8:8081/img/microService/problems/problem-61.png)  
&emsp; 为了更严谨，通常也是用lua脚本代替。lua脚本以下：  
    ```text
    if redis.call('get',KEYS[1]) == ARGV[1] then 
    return redis.call('del',KEYS[1]) 
    else
    return 0
    end;
    ```
4. RedLock红锁：  
    1. RedLock：当前线程尝试给每个Master节点`顺序`加锁。要在多数节点上加锁，并且加锁时间小于超时时间，则加锁成功；加锁失败时，依次删除节点上的锁。  
    2. ~~RedLock“顺序加锁”：确保互斥。在同一时刻，必须保证锁至多只能被一个客户端持有。~~   

#### 1.10.4.3. 使用redis分布式锁的注意点
1. 使用redis分布式锁要注意的问题：  
    1. 采用Master-Slave模式，加锁的时候只对一个节点加锁，即使通过Sentinel做了高可用，但是<font color="clime">如果Master节点故障了，发生主从切换，此时就会有可能出现`锁丢失`的问题，`可能导致多个客户端同时完成加锁`</font>。  
    &emsp; `在解决接口幂等问题中，采用redis分布式锁可以。但是如果涉及支付等对数据一致性要求严格的场景，需要结合数据库锁，建议使用基于状态机的乐观锁。`  
    2. 编码问题：  
        * key：释放别人的锁。唯一标识，例如UUID。  
        * 如何避免死锁？设置锁过期时间。  
        * `锁过期。 增大冗余时间。`  

![image](http://182.92.69.8:8081/img/microService/problems/problem-69.png)  

&emsp; 使用redis分布式锁的注意点：  

1. 非原子操作  
2. 忘了释放锁  
3. 释放了别人的锁  
4. `大量失败请求`  
&emsp; 如果有1万的请求同时去竞争那把锁，可能只有一个请求是成功的，其余的9999个请求都会失败。  
&emsp; 在秒杀场景下，会有什么问题？  
&emsp; 答：每1万个请求，有1个成功。再1万个请求，有1个成功。如此下去，直到库存不足。这就变成均匀分布的秒杀了，跟我们想象中的不一样。  
&emsp; `使用自旋锁。`  
5. 锁重入问题  
&emsp; 递归调用，使用可重入锁。 
6. 锁竞争问题  
&emsp; 如果有大量需要写入数据的业务场景，使用普通的redis分布式锁是没有问题的。  
&emsp; 但如果有些业务场景，写入的操作比较少，反而有大量读取的操作。这样直接使用普通的redis分布式锁，会不会有点浪费性能？  
&emsp; 我们都知道，锁的粒度越粗，多个线程抢锁时竞争就越激烈，造成多个线程锁等待的时间也就越长，性能也就越差。   
&emsp; 所以，提升redis分布式锁性能的第一步，就是要把锁的粒度变细。  
&emsp; 可以：1. `读写锁；`2. `锁分段`。
7. 锁超时问题  
&emsp; 自动续期  
8. 主从复制问题  
9. ~~正确使用分布式锁~~ 

#### 1.10.4.4. Redisson
1.  **<font color = "clime">RedissonLock解决客户端死锁问题（自动延期）：</font>**  
    1. 什么是死锁？因为业务不知道要执行多久才能结束，所以这个key一般不会设置过期时间。这样如果在执行业务的过程中，业务机器宕机，unlock操作不会执行，所以这个锁不会被释放，`其他机器拿不到锁，从而形成了死锁。`  
    2. ~~Redission解决死锁：(**要点：30s和10s**)~~
        1. `未设置加锁时间，自动设置加锁时间：`当业务方调用加锁操作的时候，`未设置加锁时间`，默认的leaseTime是-1，所以会取watch dog的时间作为锁的持有时间，默认是30s，这个时候即使发生了宕机现象，因为这个锁不是永不过期的，所以30s后就会释放，不会产生死锁。 
        2. `异步续租、递归调用：`另一方面，它还能解决当锁内逻辑超过30s的时候锁会失效的问题，因为当leaseTime是-1的时候，`客户端会启动一个异步任务（watch dog）`，会每隔10秒检查一下，如果客户端1还持有锁key，在业务方释放锁之前，会一直不停的增加这个锁的生命周期时间，保证在业务执行完毕之前，这个锁一直不会因为redis的超时而被释放。
2. Redisson实现了多种锁：重入锁、公平锁、联锁、红锁、读写锁、信号量Semaphore 和 CountDownLatch...  
3. **Redisson重入锁：**  
    1. Redisson重入锁加锁流程：  
        1. 执行lock.lock()代码时，<font color = "red">如果该客户端面对的是一个redis cluster集群，首先会根据hash节点选择一台机器。</font>  
        2. 然后发送一段lua脚本，带有三个参数：一个是锁的名字（在代码里指定的）、一个是锁的时常（默认30秒）、一个是加锁的客户端id（每个客户端对应一个id）。<font color = "red">然后脚本会判断是否有该名字的锁，如果没有就往数据结构中加入该锁的客户端id。</font>  

            * 锁不存在(exists)，则加锁(hset)，并设置(pexpire)锁的过期时间；  
            * 锁存在，检测(hexists)是当前线程持有锁，锁重入(hincrby)，并且重新设置(pexpire)该锁的有效时间；
            * 锁存在，但不是当前线程的，返回(pttl)锁的过期时间。 
    2. **<font color = "red">Redisson重入锁缺陷：</font>** 在哨兵模式或者主从模式下，如果master实例宕机的时候，可能导致多个客户端同时完成加锁。  


#### 1.10.4.5. ZK分布式锁
1. **<font color = "clime">对于ZK来说，实现分布式锁的核心是临时顺序节点和监听机制。</font>** ZK实现分布式锁要注意[羊群效应](/docs/microService/dubbo/ZookeeperProblem.md)    
2. **ZooKeeper分布式锁的缺点：** 1). 需要依赖zookeeper；2). 性能低。频繁地“写”zookeeper。集群节点数越多，同步越慢，获取锁的过程越慢。  
3. 基于ZooKeeper可以实现分布式的独占锁和读写锁。  
    1. **使用ZK实现分布式独占锁：**<font color="red">在某一节点下，建立临时顺序节点。最小节点获取到锁。非最小节点监听上一节点，上一节点释放锁，唤醒当前节点。</font>  
    2. **~~使用ZK实现分布式读写锁：~~<font color = "red">客户端从 Zookeeper 端获取 /share_lock下所有的子节点，并判断自己能否获取锁。</font>**  
        1. **<font color = "red">如果客户端创建的是读锁节点，获取锁的条件（满足其中一个即可）如下：</font>**  

            * 自己创建的节点序号排在所有其他子节点前面  
            * 自己创建的节点前面无写锁节点  
            
        2. **<font color = "red">如果客户端创建的是写锁节点，</font>** 由于写锁具有排他性，所以获取锁的条件要简单一些，只需确定自己创建的锁节点是否排在其他子节点前面即可。  
4. ~~ZK的羊群效应~~  


#### 1.10.4.6. MySql分布式锁
1. 基于表记录实现
2. 基于排他锁/悲观锁(for update)实现
3. 乐观锁实现
	* 一般是通过为数据库表添加一个version字段来实现读取出数据时，将此版本号一同读出。
	* 基于状态机的乐观锁


#### 1.10.4.7. 分布式锁选型（各种锁的对比）   
&emsp; ~~CAP只能满足其二、数据一致性、性能。~~  
&emsp; 分布式中间件的选型无非就是AP、CP模型（CAP原理）中的一种。针对常用的分布式锁，redis是AP模型、zookeeper是CP模型。  
&emsp; 具体选择哪一种看使用场景对数据一致性的要求。`解决幂等时一般情况下，使用redis分布式锁，但是分布式锁编码问题、中间件部署问题都有可能影响分布式锁的使用。所以数据一致性要求高的，要结合数据库乐观锁（状态控制）。`   

----------------------------


## 1.11. 高并发
### 1.11.1. 系统性能指标
1. 吞吐量  
&emsp; 一般来说， **<font color = "clime">系统吞吐量指的是系统的抗压、负载能力，代表一个系统每秒钟能承受的最大用户访问量。</font>**   
&emsp; **<font color = "clime">`一个系统的吞吐量通常由qps（tps）、并发数来决定，`每个系统对这两个值都有一个相对极限值，只要某一项达到最大值，系统的吞吐量就上不去了。</font>**  
2. QPS  
&emsp; Queries Per Second，每秒查询数，即是每秒能够响应的查询次数，注意这里的查询是指用户发出请求到服务器做出响应成功的次数，简单理解可以认为查询=请求request。`qps=每秒钟request数量`  
3. TPS  
&emsp; Transactions Per Second 的缩写，每秒处理的事务数。一个事务是指一个客户机向服务器发送请求然后服务器做出反应的过程。客户机在发送请求时开始计时，收到服务器响应后结束计时，以此来计算使用的时间和完成的事务个数。  
&emsp; 针对单接口而言，TPS可以认为是等价于QPS的，比如访问一个页面/index.html，是一个TPS，而访问/index.html页面可能请求了3次服务器比如css、js、index接口，产生了3个QPS。  
4. 并发数  
&emsp; 简而言之，系统能同时处理的请求/事务数量。  
&emsp; 计算方式：QPS = 并发数 / RT 或者 并发数= QPS * RT  

### 1.11.2. 并发系统三高
&emsp; 高并发绝不意味着只追求高性能，这是很多人片面的理解。从宏观角度看，高并发系统设计的目标有三个：高性能、高可用，以及高可扩展。  
1. **<font color = "red">`高性能`：性能体现了系统的并行处理能力，在有限的硬件投入下，提高性能意味着节省成本。**</font> 同时，性能也反映了用户体验，响应时间分别是100毫秒和1秒，给用户的感受是完全不同的。  
2. **<font color = "clime">`高可用`：表示系统可以正常服务的时间。</font>** 一个全年不停机、无故障；另一个隔三差五出线上事故、宕机，用户肯定选择前者。另外，如果系统只能做到90%可用，也会大大拖累业务。  
3. **<font color = "clime">`高扩展`：表示系统的扩展能力，流量高峰时能否在短时间内完成扩容，更平稳地承接峰值流量，比如双11活动、明星离婚等热点事件。</font>**  

#### 1.11.2.1. 高可用建设


#### 1.11.2.2. 秒杀系统设计
1. 什么是秒杀系统？定时开始、库存有限...  
2. 秒杀应该考虑哪些问题？  
&emsp; 链接暴露、恶意请求（接口防刷）、突然增加的网络及服务器带宽、超卖问题、数据库设计...  
3. 秒杀系统设计思考：  
    &emsp; 从架构视角来看，<font color = "red">`★★★秒杀系统本质是一个（分布式）高一致、（高并发）高性能、（高并发）高可用的三高系统。`</font>  
    * 高一致。从业界通用的几种减库存方案切入。减库存一般有以下几个方式：下单减库存、付款减库存、<font color = "clime">`预扣库存（买家下单后，库存为其保留一定的时间（如15分钟），超过这段时间，库存自动释放，释放后其他买家可以购买）。业界最为常见的是预扣库存。`</font>    
	* 高性能。动静分离、热点优化以及服务端性能优化。  
        * **<font color = "clime">热点数据的处理三步走，一是热点识别，二是热点隔离，三是热点优化。</font>**   
            1. 热点隔离：1. 业务隔离；2. 部署隔离；3. 数据隔离。  
            2. 热点优化：缓存和限流。  
	* 高可用。流量削峰(答题、mq)、高可用建设。  
4. 秒杀业务完整流程梳理：  
    * 后端service服务层。  
        * **<font color = "clime">使用缓存(Redis、Memchched)：将读多写少的业务数据放入缓存，如秒杀业务中可以将更新频繁的商品库存信息放入Redis缓存处理。</font>**  
            &emsp; 注：`库存信息放入Redis缓存的时候最好分为多份放入不同key的缓存中`，如库存为10万可以分为10份分别放入不同key的缓存中，这样将数据分散操作可以达到更高的读写性能。
        * 使用队列处理：将请求放入队列排队处理，以可控的速度来访问底层DB。
        * **<font color = "clime">异步处理：如将秒杀成功的订单通知信息通过消息队列（RabbitMQ、Kafka）来异步处理。</font>**    


### 1.11.3. 资源限制
&emsp; 并发高，但是服务器资源有限，可以采取哪些方案？具体案例可以参考[秒杀系统设计](/docs/system/seckill.md)。    
1. 高并发3大利器：[缓存](/docs/cache/Cache.md)、[限流](/docs/microService/thinking/CurrentLimiting.md)、[降级](/docs/microService/thinking/Demotion.md)；
2. 削峰：[mq消峰](/docs/microService/mq/mq.md)、流量削峰；
3. 将请求放入队列中（包括但不限于阻塞队列、高效队列、redis队列）；  
4. 使用服务器请求队列。例如tomcat的server.xml中增大acceptCount值。  
&emsp; acceptCount：当tomcat请求处理线程池中的所有线程都处于忙碌状态时，此时新建的链接将会被放入到pending队列，acceptCount即是此队列的容量，如果队列已满，此后所有的建立链接的请求(accept)，都将被拒绝。默认为100。在高并发/短链接较多的环境中，可以适当增大此值；当长链接较多的场景中，可以将此值设置为0。  
5. 池化技术。各种池化技术的使用和池大小的设置，包括HTTP请求池、线程池（考虑CPU密集型还是IO密集型设置核心参数）、数据库和Redis连接池等。    

## 1.12. 缓存
### 1.12.1. ~~分布式缓存问题~~
1. `对数据的操作：curd`。`同时操作数据库和缓存，存在分布式即数据一致问题`。    
2. 缓存预热/新增缓存
3. **查询缓存/（缓存穿透、缓存击穿和缓存雪崩）：**  
&emsp; <font color="red">缓存穿透、缓存击穿和缓存雪崩都是缓存失效导致大量请求直接访问数据库而出现的情况。</font>  
&emsp; <font color="red">不同的是缓存穿透是数据库和缓存都不存在相关数据；而缓存击穿和缓存雪崩是缓存和数据库都存在相应数据，</font><font color = "clime">只是缓存失效了而已。</font>  

        穿透和击穿的区别：没有障碍物的直接穿透，有障碍物的打击穿透。  

4. 缓存穿透：  
&emsp; 缓存穿透是指，请求访问的数据在缓存中没有命中，到数据库中查询也没有，导致此次查询失败；当大量请求针对此类数据时，由于缓存不能命中，请求直接穿透缓存，直击数据库，给数据库造成巨大的访问压力。    
&emsp; 解决方案：空值缓存；`设置布隆过滤器`，若布隆过滤器中无，则直接返回，若存在，则查找缓存redis。  
5. 缓存`击穿`(`热点缓存`)：  
    1. 当缓存中不存在但是数据库中存在数据（`一般来说指缓存失效`），在短时间内针对这种数据产生大量的请求，由于缓存不能命中，直击数据库，给数据库造成较大压力。  
    2. **<font color = "clime">解决方案：key永不过期，使用互斥锁或队列，双缓存。</font>**   
    3. ⚠️互斥锁方案：单机使用互斥锁，集群使用分布式锁。  
    &emsp; 上面的现象是多个线程同时去查询数据库的这条数据，那么可以在第一个查询数据的请求上使用一个互斥锁来锁住它。  
    &emsp; 其他的线程走到这一步拿不到锁就等着，等第一个线程查询到了数据，然后做缓存。后面的线程进来发现已经有缓存了，就直接走缓存。  
6. 缓存雪崩：  
    &emsp; 缓存雪崩是指某一时间段内缓存中数据大批量过期失效，但是查询数据量巨大，引起数据库压力过大甚至宕机。和缓存击穿不同的是，缓存击穿指并发查同一条数据，缓存雪崩是不同数据都过期了，导致大量请求直达数据库。缓存雪崩有两种情况：  
    * 缓存批量过期：缓存批量过期这样的雪崩只是对数据库产生周期性的压力，数据还是扛得住的。  
    &emsp; 解决方案：`key随机值，` **<font color = "clime">key永不过期，使用互斥锁或队列，双缓存。</font>**
    * 缓存服务器宕机：缓存服务器的某个节点宕机或断网，对数据库产生的压力是致命的。  
    &emsp; 解决方案：服务器高可用。   
7. 更新缓存  
    1. 「先更新数据库，再删缓存」的策略，原因是这个策略即使在并发读写时，也能最大程度保证数据一致性。  
    2. `先更新数据库，再删除缓存，非原子操作。可以采用双删延迟策略，进行兜底，补偿方案。` **<font color = "clime">第二次删除前线程休眠冗余的读写时间，如果读从库，再加上延迟时间。</font>**  
    3. `采用`延时删除`策略中的问题：`  
        * **<font color = "clime">同步方式会降低吞吐量，可以采用异步，即异步延时删除。</font>**  
        * **<font color = "clime">第二次删除可能失败，提供一个保障的重试机制。</font>** 方案一：采用消息队列，缺点对业务线代码造成大量的侵入；方案二：订阅binlog，订阅程序提取出所需要的数据以及key，另起一段非业务代码，获得该信息，尝试删除缓存操作，发现删除失败，将这些信息发送至消息队列，重新从消息队列中获得该数据，重试操作。  

### 1.12.2. 数据结构：bitMap、布隆、布谷鸟
#### 1.12.2.1. bitMap
&emsp; [BitMap实现签到](/docs/microService/Redis/BitMap.md)  

#### 1.12.2.2. 布隆
&emsp; BloomFilter是由一个固定大小的二进制向量或者位图(bitmap)和一系列(通常好几个)映射函数组成的。  
1. 布隆过滤器的原理：   
    &emsp; **<font color = "red">当一个变量被加入集合时，通过K个映射函数将这个变量映射成位图中的 K 个点，把它们置为1。</font>**  
    ![image](http://www.wt1814.com/static/view/algorithm/function-2.png)  

    &emsp; 查询某个变量的时候，只要看看这些点是不是都是1，就可以大概率知道集合中有没有它了。  

    * 如果这些点有任何一个0，则被查询变量一定不在；
    * 如果都是1，则被查询变量很可能在。  

    &emsp; 注意，这里是<font color = "clime">可能存在，而不是一定存在！</font>  
2. 布隆过滤器的特点：  
    * 优点：占用内存少，新增、查询效率高。  
    * 缺点： **<font color = "red">误判率和不能删除。</font>**  

            布隆过滤器的误判是指多个输入经过哈希之后在相同的bit位置1了，这样就无法判断究竟是哪个输入产生的，因此误判的根源在于相同的bit位被多次映射且置1。  
            这种情况也造成了布隆过滤器的删除问题，因为布隆过滤器的每一个bit并不是独占的，很有可能多个元素共享了某一位。如果直接删除这一位的话，会影响其他的元素。  

    * 特点总结：  
        * **<font color = "clime">一个元素如果判断结果为存在的时候元素不一定存在（可能存在），但是判断结果为不存在的时候则一定不存在。</font>**  
        * **<font color = "red">布隆过滤器可以添加元素，但是不能删除元素。</font><font color = "clime">因为删掉元素会导致误判率增加。</font>**  

3. 布隆过滤器的使用场景：布隆过滤器适合于一些需要去重，但不一定要完全精确的场景。比如：  
    &emsp; 1. 黑名单 2. URL去重 3. 单词拼写检查 4. Key-Value缓存系统的Key校验 5. ID校验，比如订单系统查询某个订单ID是否存在，如果不存在就直接返回。
3. bitmap和布隆过滤器的区别：  
&emsp; bitmap虽然好用，可是对于不少实际状况下的大数据处理它仍是远远不够的， **<font color = "clime">例如若是要进行64bit的long型数据去重，那咱们须要含有2^61个byte的byte数组来存储，这显然是不现实的。</font>** 那咱们如何来优化呢，很明显假如咱们申请了这么大的byte数组来标记数据，可想而知其空间利用率是极地的。布隆过滤器正是经过`提升空间利用率`来进行标记的。 

#### 1.12.2.3. 计数布隆过滤器


#### 1.12.2.4. 布谷鸟
1. 布谷鸟哈希  
&emsp; 谷鸟哈希算法会帮这些受害者（被挤走的蛋）寻找其它的窝。因为每一个元素都可以放在两个位置，只要任意一个有空位置，就可以塞进去。所以这个伤心的被挤走的蛋会看看自己的另一个位置有没有空，如果空了，自己挪过去也就皆大欢喜了。但是如果这个位置也被别人占了呢？好，那么它会再来一次「鸠占鹊巢」，将受害者的角色转嫁给别人。然后这个新的受害者还会重复这个过程直到所有的蛋都找到了自己的巢为止。  
&emsp; 但是会遇到一个问题，那就是如果数组太拥挤了，连续踢来踢去几百次还没有停下来，这时候会严重影响插入效率。这时候布谷鸟哈希会设置一个阈值，当连续占巢行为超出了某个阈值，就认为这个数组已经几乎满了。这时候就需要对它进行扩容，重新放置所有元素。  
2. 布谷鸟算法  
	1. 基本结构  
	&emsp; 哈希表由一个桶数组组成，其中一个桶可以有多个条目（比如上述图c中有四个条目）。而每个桶中有四个指纹位置，意味着一次哈希计算后布谷鸟有四个“巢“可用，而且四个巢是连续位置，可以更好的利用cpu高速缓存。也就是说每个桶的大小是4*8bits。  


#### 1.12.2.5. Redis热点key
1. 发现热key
	1. 方法一：凭借业务经验，进行预估哪些是热key
	2. 方法二：在客户端进行收集
	3. 方法三：在Proxy层做收集  
	4. 方法四：用redis自带命令
	5. 方法五：自己抓包评估
2. 处理方案
	1. 二级缓存
	2. 备份热key  
    &emsp; 这个方案也很简单。不要让key走到同一台redis上不就行了。把这个key，在多个redis上都存一份不就好了。接下来，有热key请求进来的时候，就在有备份的redis上随机选取一台，进行访问取值，返回数据。  
    &emsp; 假设redis的集群数量为N，步骤如下图所示  
    ![image](http://182.92.69.8:8081/img/microService/problems/problem-71.png)  
    &emsp; 注：不一定是2N，你想取3N，4N都可以，看要求。  
    &emsp; 伪代码如下  

    ```text
    const M = N * 2
    //生成随机数
    random = GenRandom(0, M)
    //构造备份新key
    bakHotKey = hotKey + “_” + random
    data = redis.GET(bakHotKey)
    if data == NULL {
        data = GetFromDB()
        redis.SET(bakHotKey, expireTime + GenRandom(0,5))
    }
    ``` 

### 1.12.3. Redis
#### 1.12.3.1. Redis数据类型
##### 1.12.3.1.1. Redis基本数据类型
1. Key操作命令：expire，为给定key设置生存时间；TTL key，以秒为单位，返回给定key的剩余生存时间（TTL, time to live）。  
2.  **<font color = "clime">Redis各个数据类型的使用场景：分析存储类型和可用的操作。</font>**  
    * 有序列表list：  
    &emsp; `列表不但是有序的，同时支持按照索引范围获取元素。` 可以用作栈、文章列表。  
    * 无序集合set：
        * 集合内操作，可以用作标签、点赞、签到；
        * 集合间操作，可以用作社交需求； 
        * spop/srandmember命令生成随机数。   
    * 有序集合ZSet：  
    &emsp; 有序的集合，每个元素有个 score。  
    &emsp; 可以用作排行榜、延迟队列。  
3. **ZSet实现多维排序：**  
&emsp; <font color = "red">将涉及排序的多个维度的列通过一定的方式转换成一个特殊的列</font>，即result = function(x, y, z)，即x，y，z是三个排序因子，例如下载量、时间等，通过自定义函数function()计算得到result，将result作为ZSet中的score的值，就能实现任意维度的排序需求了。 

##### 1.12.3.1.2. Redis扩展数据类型
1. <font color = "clime">Bitmap、HyperLogLog都是作为Redis的Value值。</font>  
2. <font color = "clime">`Bitmap：二值状态统计。`Redis中的Bitmap，`key可以为某一天或某一ID，Bitmap中bit可以存储用户的任意信息，所以Redis Bitmap可以用作统计信息。`常用场景：用户签到、统计活跃用户、用户在线状态。</font>  
    1. 基于Redis BitMap实现用户签到功能： **<font color = "clime">考虑到每月初需要重置连续签到次数，最简单的方式是按用户每月存一条签到数据（也可以每年存一条数据）。`Key的格式为u :sign :uid :yyyyMM`，`Value则采用长度为4个字节（32位）的位图（最大月份只有31天）。位图的每一位代表一天的签到，1表示已签，0表示未签。`</font>**  
3. <font color = "clime">`HyperLogLog用于基数统计，例如UV（独立访客数）。`</font>  
    * `基数统计是指找出集合中不重复元素，用于去重。`  
    * **<font color = "clime">使用Redis统计集合的基数一般有三种方法，分别是使用Redis的Hash，BitMap和HyperLogLog。</font>**  
    * HyperLogLog内存空间消耗少，但存在误差0.81%。  
4. Streams消息队列：支持多播的可持久化的消息队列，用于实现发布订阅功能，借鉴了kafka的设计。 
5. [布隆过滤器](/docs/function/otherStructure.md)作为一个插件加载到Redis Server中，就会给Redis提供了强大的布隆去重功能。  

##### 1.12.3.1.3. redis使用：bigKey

##### 1.12.3.1.4. ~~Redis底层实现~~
1. 很重要的思想：redis设计比较复杂的对象系统，都是为了缩减内存占有！！！  

###### 1.12.3.1.4.1. 数据结构
1. 很重要的思想：redis设计比较复杂的对象系统，都是为了缩减内存占有！！！  
2. ~~redis底层8种数据结构：int、raw、embstr(SDS)、ziplist、hashtable、quicklist、intset、skiplist。~~  
3. 3种链表：  
    * 双端链表LinkedList  
        &emsp; Redis的链表在双向链表上扩展了头、尾节点、元素数等属性。Redis的链表结构如下：
        ![image](http://182.92.69.8:8081/img/microService/Redis/redis-62.png)  
    * 压缩列表Ziplist  
        &emsp; 在双端链表中，如果在一个链表节点中存储一个小数据，比如一个字节。那么对应的就要保存头节点，前后指针等额外的数据。这样就浪费了空间，同时由于反复申请与释放也容易导致内存碎片化。这样内存的使用效率就太低了。  
        &emsp; Redis设计了压缩列表：  
        ![image](http://182.92.69.8:8081/img/microService/Redis/redis-110.png)  
        &emsp; ziplist是一组连续内存块组成的顺序的数据结构， **<font color = "red">是一个经过特殊编码的双向链表，它不存储指向上一个链表节点和指向下一个链表节点的指针，而是存储上一个节点长度和当前节点长度，通过牺牲部分读写性能，来换取高效的内存空间利用率，节省空间，是一种时间换空间的思想。</font>** 只用在字段个数少，字段值小的场景里。  
    * 快速列表Quicklist  
        &emsp; QuickList其实就是结合了LinkedList和ZipList的优点设计出来的。quicklist存储了一个双向链表，每个节点都是一个ziplist。  
        ![image](http://182.92.69.8:8081/img/microService/Redis/redis-63.png)  
4. 整数集合inset  
&emsp; inset的数据结构：  
![image](http://182.92.69.8:8081/img/microService/Redis/redis-7.png)  
&emsp; inset也叫做整数集合，用于保存整数值的数据结构类型，它可以保存int16_t、int32_t 或者int64_t 的整数值。  
&emsp; 在整数集合中，有三个属性值encoding、length、contents[]，分别表示编码方式、整数集合的长度、以及元素内容，length就是记录contents里面的大小。  
5. 跳跃表SkipList  
&emsp; skiplist也叫做「跳跃表」，跳跃表是一种有序的数据结构，它通过每一个节点维持多个指向其它节点的指针，从而达到快速访问的目的。  
![image](http://182.92.69.8:8081/img/microService/Redis/redis-85.png)  
&emsp; SkipList分为两部分，dict部分是由字典实现，Zset部分使用跳跃表实现，从图中可以看出，dict和跳跃表都存储了数据，实际上dict和跳跃表最终使用指针都指向了同一份数据，即数据是被两部分共享的，为了方便表达将同一份数据展示在两个地方。  

&emsp; 二叉搜索算法能够高效的查询数据，但是需要一块连续的内存，而且增删改效率很低。  
&emsp; 跳表，是基于链表实现的一种类似“二分”的算法。它可以快速的实现增，删，改，查操作。    

###### 1.12.3.1.4.2. SDS详解
1. **<font color = "clime">对于SDS中的定义在Redis的源码中有的三个属性int len、int free、char buf[]。</font>**  
    ![image](http://182.92.69.8:8081/img/microService/Redis/redis-77.png)  
    * len保存了字符串的长度；
    * free表示buf数组中未使用的字节数量；
    * buf数组则是保存字符串的每一个字符元素。  
2. Redis字符串追加会做以下三个操作：  
    1. 计算出大小是否足够；  
    2. 开辟空间至满足所需大小；  
    3. **<font color = "red">如果len < 1M，开辟与已使用大小len相同长度的空闲free空间；如果len >= 1M，开辟1M长度的空闲free空间。</font>**  
3. **Redis字符串的性能优势：**  
    * 动态扩展：拼接字符串时，计算出大小是否足够，开辟空间至满足所需大小。  
    * 避免缓冲区溢出。「c语言」中两个字符串拼接，若是没有分配足够长度的内存空间就「会出现缓冲区溢出的情况」。  
    * （`内存分配优化`）降低空间分配次数，提升内存使用效率。 **<font color = "blue">`空间预分配和惰性空间回收`。</font>** 
        * 空间预分配：对于追加操作来说，Redis不仅会开辟空间至够用，<font color = "red">而且还会预分配未使用的空间(free)来用于下一次操作。</font>  
        * 惰性空间回收：与上面情况相反，<font color = "red">惰性空间回收适用于字符串缩减操作。</font>比如有个字符串s1="hello world"，对s1进行sdstrim(s1," world")操作，<font color = "red">执行完该操作之后Redis不会立即回收减少的部分，而是会分配给下一个需要内存的程序。</font>  
    * 快速获取字符串长度。
    * 二进制安全。

###### 1.12.3.1.4.3. Dictht
1. **<font color = "red">rehash：</font>**  
&emsp; dictEntry有ht[0]和ht[1]两个对象。  
&emsp; 扩展操作：ht[1]扩展的大小是比当前 ht[0].used 值的二倍大的第一个2的整数幂；收缩操作：ht[0].used 的第一个大于等于的 2 的整数幂。  
&emsp; **<font color = "clime">当ht[0]上的所有的键值对都rehash到ht[1]中，会重新计算所有的数组下标值，当数据迁移完后，ht[0]就会被释放，然后将ht[1]改为ht[0]，并新创建ht[1]，为下一次的扩展和收缩做准备。</font>**  
2. **<font color = "red">渐进式rehash：</font>**  
&emsp; **<font color = "clime">Redis将所有的`rehash操作分成多步进行`，直到都rehash完成。</font>**  
&emsp; **<font color = "red">在渐进式rehash的过程「更新、删除、查询会在ht[0]和ht[1]中都进行」，比如更新一个值先更新ht[0]，然后再更新ht[1]。</font>**   
&emsp; **<font color = "clime">而新增操作直接就新增到ht[1]表中，ht[0]不会新增任何的数据，</font><font color = "red">这样保证`「ht[0]只减不增`，直到最后的某一个时刻变成空表」，这样rehash操作完成。</font>**  


###### 1.12.3.1.4.4. 数据类型  
&emsp; **<font color = "clime">Redis根据不同的使用场景和内容大小来判断对象使用哪种数据结构，从而优化对象在不同场景下的使用效率和内存占用。</font>**   
![image](http://182.92.69.8:8081/img/microService/Redis/redis-106.png)  

* String字符串类型的内部编码有三种：
    1. int，存储8个字节的长整型（long，2^63-1）。当int数据不再是整数，或大小超过了long的范围（2^63-1 = 9223372036854775807）时，自动转化为embstr。  
    2. embstr，代表 embstr 格式的 SDS（Simple Dynamic String简单动态字符串），存储小于44个字节的字符串。  
    3. raw，存储大于44个字节的字符串（3.2 版本之前是 39 字节）。  
* Hash由ziplist（压缩列表）或者dictht（字典）组成；  
* List，「有序」「可重复」集合，由ziplist压缩列表和linkedlist双端链表的组成，在 3.2 之后采用QuickList；  
* Set，「无序」「不可重复」集合， **<font color = "clime">是特殊的Hash结构（value为null），</font>** 由intset（整数集合）或者dictht（字典）组成；
* ZSet，「有序」「不可重复」集合，由skiplist（跳跃表）或者ziplist（压缩列表）组成。  
    &emsp; ZSet为什么不使用红黑树？  
    1. zset有个核心操作：范围查找，跳表效率比红黑树高。  
    2. 跳表的实现比红黑树简单。可以有效的控制跳表层级，来控制内存的消耗。    

#### 1.12.3.2. Redis原理
&emsp; 从`CPU、内存、磁盘、网络IO`分析。  

##### 1.12.3.2.1. Redis为什么那么快？
&emsp; Redis的性能非常之高，每秒可以承受10W+的QPS，它如此优秀的性能主要取决于以下几个方面：  

1. 磁盘I/O：
    * 纯内存操作
    * [虚拟内存机制](/docs/microService/Redis/RedisVM.md)  
    * 合理的数据编码
2. ~~网络I/O：~~  
    * [使用IO多路复用技术](/docs/microService/Redis/RedisEvent.md)  
    * [合理的线程模型](/docs/microService/Redis/RedisMultiThread.md)   
    * [简单快速的Redis协议](/docs/microService/Redis/RESP.md)  
3. ......

##### 1.12.3.2.2. Redis【虚拟内存】机制
&emsp; **<font color = "clime">通过VM功能可以实现冷热数据分离，使热数据仍在内存中、冷数据保存到磁盘。这样就可以避免因为内存不足而造成访问速度下降的问题。</font>**  
&emsp; 使用虚拟内存把那些不经常访问的数据交换到磁盘上。需要特别注意的是Redis并没有使用OS提供的Swap，而是自己实现。  
&emsp; **<font color = "clime">Redis为了保证查找的速度，只会将value交换出去，而在内存中保留所有的Key。</font>**  

##### 1.12.3.2.3. Redis事件/Reactor
&emsp; 参考[Redis事件/Reactor](/docs/microService/Redis/RedisEvent.md)  
&emsp; Redis基于Reactor模式开发了自己的网络事件处理器：这个处理器被称为文件事件处理器（file event handler）：文件事件处理器使用I/O多路复用（multiplexing）程序来同时监听多个套接字，并根据套接字目前执行的任务来为套接字关联不同的事件处理器。  
&emsp; 下图展示了文件事件处理器的四个组成部分，它们分别是套接字、I/O多路复用程序、文件事件分派器（dispatcher），以及事件处理器。  
![image](http://182.92.69.8:8081/img/microService/Redis/redis-56.png)    

##### 1.12.3.2.4. Redis多线程模型
1. 为什么Redis一开始使用单线程？  
&emsp; 基于内存而且使用多路复用技术，单线程速度很快，又保证了多线程的特点。因此没有必要使用多线程。  
2. 为什么引入多线程？  
&emsp; **<font color = "clime">因为读写网络的read/write系统调用（网络I/O）在Redis执行期间占用了大部分CPU时间，如果把网络读写做成多线程的方式对性能会有很大提升。</font>**  
&emsp; **<font color = "clime">Redis的多线程部分只是用来处理网络数据的读写和协议解析，执行命令仍然是单线程。</font>** 
3. 官方建议：4核的机器建议设置为2或3个线程，8核的建议设置为6个线程， **<font color = "clime">`线程数一定要小于机器核数，尽量不超过8个。`</font>**   

##### 1.12.3.2.5. Redis协议
&emsp; RESP是Redis Serialization Protocol的简称，也就是专门为redis设计的一套序列化协议。这个协议其实在redis的1.2版本时就已经出现了，但是到了redis2.0才最终成为redis通讯协议的标准。  
&emsp; 这个序列化协议听起来很高大上， 但实际上就是一个文本协议。根据官方的说法，这个协议是基于以下几点(而妥协)设计的：  
1. 实现简单。可以减低客户端出现bug的机率。  
2. `解析速度快。`由于RESP能知道返回数据的固定长度，所以不用像json那样扫描整个payload去解析，所以它的性能是能跟解析二进制数据的性能相媲美的。  
3. 可读性好。  


---------------

##### 1.12.3.2.6. Redis内存操作
###### 1.12.3.2.6.1. Redis过期键删除
1. 过期键常见的删除策略有3种：定时删除(主动)、惰性删除(被动)、定期删除(主动)。<font color = "red">Redis服务器使用的是惰性删除策略和定期删除策略。</font>  
    * 定时删除策略，在设置键的过期时间的同时，创建一个定时器，让定时器在键的过期时间来临时，立即执行对键的删除操作。  
    * <font color = "clime">惰性删除策略，只有当访问一个key时，才会判断该key是否已过期，过期则清除。</font>  
    * <font color = "red">`定期删除策略，每隔一段时间执行一次删除过期键操作`</font>，并通过<font color = "clime">`限制删除操作执行的时长和频率来减少删除操作对CPU时间的影响`</font>，同时，通过定期删除过期键，也有效地减少了因为过期键而带来的内存浪费。  


###### 1.12.3.2.6.2. Redis内存淘汰
1. **Redis内存淘汰使用的算法有4种：**  
    * random，随机删除。  
    * TTL，删除过期时间最少的键。  
    * <font color = "clime">LRU，Least Recently Used：最近最少使用（访问时间）。</font>判断最近被使用的时间，离目前最远的数据优先被淘汰。  
    &emsp; **<font color = "red">`如果基于传统LRU算法实现，Redis LRU会有什么问题？需要额外的数据结构存储，消耗内存。`</font>**  
    &emsp; **<font color = "blue">Redis LRU对传统的LRU算法进行了改良，通过`随机采样`来调整算法的精度。</font>** 如果淘汰策略是LRU，则根据配置的采样值maxmemory_samples(默认是 5 个)，随机从数据库中选择m个key，淘汰其中热度最低的key对应的缓存数据。所以采样参数m配置的数值越大，就越能精确的查找到待淘汰的缓存数据，但是也消耗更多的CPU计算，执行效率降低。  
    * <font color = "clime">LFU，Least Frequently Used，最不常用（`访问频率`），4.0版本新增。</font>  
2. **~~内存淘汰策略选择：~~**  
&emsp; **<font color = "clime">volatile和allkeys规定了是对已设置过期时间的key淘汰数据还是从全部key淘汰数据。volatile-xxx策略只会针对带过期时间的key进行淘汰，allkeys-xxx策略会对所有的key进行淘汰。</font>**  
    * 如果只是拿Redis做缓存，那应该使用allkeys-xxx，客户端写缓存时不必携带过期时间。  
    * `如果还想同时使用Redis的持久化功能，那就使用volatile-xxx策略，这样可以保留没有设置过期时间的key，它们是永久的key不会被LRU算法淘汰。`  

    1. 如果数据呈现幂律分布，也就是一部分数据访问频率高，一部分数据访问频率低，或者无法预测数据的使用频率时，则使用allkeys-lru/allkeys-lfu。  
    2. 如果数据呈现平等分布，也就是所有的数据访问频率都相同，则使用allkeys-random。
    3. 如果研发者需要通过设置不同的ttl来判断数据过期的先后顺序，此时可以选择volatile-ttl策略。
    4. `如果希望一些数据能长期被保存，而一些数据可以被淘汰掉，选择volatile-lru/volatile-lfu或volatile-random都是比较不错的。`
    5. 由于设置expire会消耗额外的内存，如果计划避免Redis内存在此项上的浪费，可以选用allkeys-lru/volatile-lfu策略，这样就可以不再设置过期时间，高效利用内存了。 

##### 1.12.3.2.7. Redis持久化，磁盘操作
1. RDB，快照；保存某一时刻的全部数据；缺点是间隔长（配置文件中默认最少60s）。  
&emsp; Redis 提供了两个命令来生成 RDB 快照文件，分别是 save 和 bgsave。save 命令在主线程中执行，会导致阻塞。而 bgsave 命令则会创建一个子进程，用于写入 RDB 文件的操作，避免了对主线程的阻塞，这也是 Redis RDB 的默认配置。fork子进程也会造成阻塞。    
2. AOF，文件追加；记录所有操作命令；优点是默认间隔1s，丢失数据少；缺点是文件比较大，通过重写机制来压缩文件体积。  
    1. `AOF采用的是【写后日志】的方式`，Redis先执行命令把数据写入内存，然后再记录日志到文件中。AOF日志记录的是操作命令，不是实际的数据，如果采用AOF方法做故障恢复时需要将全量日志都执行一遍。  
        ![image](http://182.92.69.8:8081/img/microService/Redis/redis-121.png)  

        &emsp; `平时用的MySQL则采用的是 “写前日志”，`那 Redis为什么要先执行命令，再把数据写入日志呢？  

        &emsp; 这个主要是由于Redis在写入日志之前，不对命令进行语法检查，所以只记录执行成功的命令，避免出现记录错误命令的情况，而且在命令执行后再写日志不会阻塞当前的写操作。  

        &emsp; 后写日志主要有两个风险可能会发生：  

        * 数据可能会丢失：如果 Redis 刚执行完命令，此时发生故障宕机，会导致这条命令存在丢失的风险。  
        * 可能阻塞其他操作：AOF 日志其实也是在主线程中执行，所以当 Redis 把日志文件写入磁盘的时候，还是会阻塞后续的操作无法执行。
    1. **<font color = "clime">重写后的AOF文件为什么可以变小？有如下原因：</font>**  
        1. <font color = "red">进程内已经超时的数据不再写入文件。</font>   
        2. <font color = "red">旧的AOF文件含有无效命令，</font>如del key1、hdel key2、srem keys、set a111、set a222等。重写使用进程内数据直接生成，这样新的AOF文件只保留最终数据的写入命令。  
        3. <font color = "red">多条写命令可以合并为一个，</font>如：lpush list a、lpush list b、lpush list c可以转化为：lpush list a b c。为了防止单条命令过大造成客户端缓冲区溢出，对于list、set、hash、zset等类型操作，以64个元素为界拆分为多条。  
    2. **<font color = "red">AOF重写降低了文件占用空间，除此之外，另一个目的是：更小的AOF 文件可以更快地被Redis加载。</font>**  
    3. 在写入AOF日志文件时，如果Redis服务器宕机，则AOF日志文件文件会出格式错误。在重启Redis服务器时，Redis服务器会拒绝载入这个AOF文件，可以修复AOF 并恢复数据。 
3. Redis4.0混合持久化，先RDB，后AOF。  
4. ~~**<font color = "clime">RDB方式bgsave指令中fork子进程、AOF方式重写bgrewriteaof都会造成阻塞。</font>**~~  
  

###### 1.12.3.2.7.1. ~~AOF重写阻塞~~
1. AOF重写阻塞：
    1. **<font color = "clime">当Redis执行完一个写命令之后，它会`同时将这个写命令发送给AOF缓冲区和AOF重写缓冲区`。</font>**  
	2. **<font color = "clime">当子进程完成AOF重写工作之后，它会向父进程发送一个信号，父进程在接收到该信号之后，`会调用一个信号处理函数，并执行相应工作：将AOF重写缓冲区中的所有内容写入到新的AOF文件中。`</font>**  
	3. **<font color = "clime">在整个AOF后台重写过程中，`只有信号处理函数执行时会对Redis主进程造成阻塞，`在其他时候，AOF后台重写都不会阻塞主进程。</font>** 如果信号处理函数执行时间较长，即造成AOF阻塞时间长，就会对性能有影响。  
2. 解决方案：  
	* **<font color = "red">将no-appendfsync-on-rewrite设置为yes。</font>** 
	* master节点关闭AOF。  
    
    &emsp; 可以采取比较折中的方式：  
    * 在master节点设置将no-appendfsync-on-rewrite设置为yes（`表示在日志重写时，不进行命令追加操作，而只是将命令放在重写缓冲区里，避免与命令的追加造成磁盘IO上的冲突`），同时auto-aof-rewrite-percentage参数设置为0关闭主动重写。  
    * 在重写时为了避免硬盘空间不足或者IO使用率高影响重写功能，还添加了硬盘空间报警和IO使用率报警保障重写的正常进行。
4. 虽然在everysec配置下aof的fsync是由子线程进行操作的，但是主线程会监控fsync的执行进度。  
&emsp; **<font color = "clime">主线程在执行时候如果发现上一次的fsync操作还没有返回，那么主线程就会阻塞。</font>**  


#### 1.12.3.3. Redis内置功能
##### 1.12.3.3.1. RedisPipeline/批处理
1. Redis主要提供了以下几种批量操作方式：  
    * 批量get/set(multi get/set)。⚠️注意：Redis中有删除单个Key的指令DEL，但没有批量删除 Key 的指令。  
    * 管道(pipelining)
    * 事务(transaction)
    * 基于事务的管道(transaction in pipelining)
2. 批量get/set(multi get/set)与管道：  
    1. 原生批命令（mset, mget）是原子性，pipeline是非原子性。  
    2. 原生批命令一命令多个key，但pipeline支持多命令（存在事务），非原子性。  
    3. 原生批命令是服务端实现，而pipeline需要服务端与客户端共同完成。  
3. Pipeline指的是管道技术，指的是客户端允许将多个请求依次发给服务器，过程中而不需要等待请求的回复，在最后再一并读取结果即可。  


##### 1.12.3.3.2. Redis事务
1. **<font color = "clime">Redis事务的三个阶段：</font>**  
    * 开始事务：以MULTI开启一个事务。   
    * **<font color = "clime">命令入队：将多个命令入队到事务中，接到这些命令不会立即执行，而是放到等待执行的事务队列里。</font>**    
    * 执行事务(exec)或取消事务(discard)：由EXEC/DISCARD命令触发事务。  
2. **使用Redis事务的时候，可能会遇上以下两种错误：**  
    * **<font color = "red">（类似于Java中的编译错误）事务在执行EXEC之前，入队的命令可能会出错。</font>** 比如说，命令可能会产生语法错误（参数数量错误，参数名错误等等），或者其他更严重的错误，比如内存不足（如果服务器使用maxmemory设置了最大内存限制的话）。  
    * **<font color = "red">（类似于Java中的运行错误）命令可能在 EXEC 调用之后失败。</font>** 举个例子，事务中的命令可能处理了错误类型的键，比如将列表命令用在了字符串键上面，诸如此类。  

    1. Redis 针对如上两种错误采用了不同的处理策略，对于发生在 EXEC 执行之前的错误，服务器会对命令入队失败的情况进行记录，并在客户端调用 EXEC 命令时，拒绝执行并自动放弃这个事务（Redis 2.6.5 之前的做法是检查命令入队所得的返回值：如果命令入队时返回 QUEUED ，那么入队成功；否则，就是入队失败）  
    2. 对于那些在 EXEC 命令执行之后所产生的错误，并没有对它们进行特别处理：即使事务中有某个/某些命令在执行时产生了错误，事务中的其他命令仍然会继续执行。 
3. **带Watch的事务（CAS）：**  
&emsp; Redis Watch 命令给事务提供check-and-set (CAS) 机制。被Watch的Key被持续监控，如果key在Exec命令执行前有改变，那么整个事务被取消。   
&emsp; WATCH命令用于在事务开始之前监视任意数量的键：当调用EXEC命令执行事务时，如果任意一个被监视的键已经被其他客户端修改了，那么整个事务将被打断，不再执行，直接返回失败。 

##### 1.12.3.3.3. Redis和Lua



##### 1.12.3.3.4. Redis实现消息队列
&emsp; redis中实现消息队列的几种方案：  

* 基于List的 LPUSH+BRPOP 的实现
* PUB/SUB，订阅/发布模式
* 基于Sorted-Set的实现
* 基于Stream类型的实现


#### 1.12.3.4. Redis高可用
##### 1.12.3.4.1. Redis高可用方案
1. **<font color = "clime">考虑资源：</font>**    
&emsp; Redis集群最少6个节点，每个节点20G，总共120G。因此Redis集群比较耗资源。小型公司可以采用哨兵模式。    
2. **<font color = "clime">考虑QPS：</font>**  
&emsp; **单机的redis一般是支持上万甚至几万，具体的性能取决于数据操作的复杂性，如果仅仅是简单的kv操作的话，可以达到数万，如果是运行复杂的lua脚本的话，就可能只能到一万左右。**  
&emsp; 缓存一般是用来支撑读的高并发，一般比较少用来支撑读的操作，一般读的操作是比较频繁的，甚至达到几万几十万，但是写的操作每秒才几千，这就需要读写分离了。  

&emsp; `小型公司，可以采用哨兵，主从复制-单副本模式。`  


##### 1.12.3.4.2. Redis主从复制
1. **<font color = "red">Redis主从复制架构常见的是`单副本`、双副本模式。</font>**  
2. <font color = "red">主从复制过程大体可以分为3个阶段：连接建立阶段（即准备阶段）、数据同步阶段、命令传播阶段。</font>  
&emsp; 一、连接建立阶段：1. 保存主节点（master）信息。2. 从节点（slave）内部通过每秒运行的定时任务维护复制相关逻辑，当定时任务发现存在新的主节点后，会尝试与该节点建立网络连接。</font>3. 发送ping命令。  
&emsp; 二、数据同步阶段：5. 同步数据集。有两种复制方式：全量复制和部分复制。  
&emsp; 三、命令传播阶段：6. 命令持续复制。  
3. redis 2.8之前使用sync [runId] [offset]同步命令，redis2.8之后使用psync [runId] [offset]命令。两者不同在于，sync命令仅支持全量复制过程，psync支持全量和部分复制。    
4. **主从复制应用与问题：**
    * **<font color = "red">传输延迟，提供了repl-disable-tcp-nodelay参数用于控制是否关闭TCP_NODELAY，默认关闭。</font>**    
        * `当关闭时，主节点产生的命令数据无论大小都会及时地发送给从节点，这样主从之间延迟会变小，但增加了网络带宽的消耗。` **<font color = "blue">适用于主从之间的网络环境良好的场景，如同机架或同机房部署。</font>** 
        * **<font color = "blue">当开启时，主节点会合并较小的TCP数据包从而节省带宽。</font>** 默认发送时间间隔取决于Linux的内核，一般默认为40毫秒。这种配置节省了带宽但增大主从之间的延迟。 **<font color = "blue">适用于主从网络环境复杂或带宽紧张的场景，如跨机房部署。</font>**  
    * 规避全量复制
        * 节点运行ID不匹配。提供故障转移的功能；如果修改了主节点的配置，需要重启才能够生效，可以选择安全重启的方式（debug reload）。  
        * 复制偏移量offset不在复制积压缓冲区中。需要根据中断时长来调整复制积压缓冲区的大小。  

##### 1.12.3.4.3. Redis读写分离


##### 1.12.3.4.4. Redis哨兵模式
1. <font color="clime">监控和自动故障转移使得Sentinel能够完成主节点故障发现和自动转移，配置提供者和通知则是实现通知客户端主节点变更的关键。</font>  
2. <font color = "clime">Redis哨兵架构中主要包括两个部分：Redis Sentinel集群和Redis数据集群。</font>  
3. **<font color = "clime">哨兵原理：</font>**  
    * **<font color = "red">心跳检查：Sentinel通过三个定时任务来完成对各个节点的发现和监控，这是保证Redis高可用的重要机制。</font>**  
        * 每隔10秒，每个Sentinel节点会向主节点和从节点发送`info命令` **<font color = "clime">获取最新的拓扑结构。</font>**   
        * 每隔2秒，每个Sentinel节点会向Redis数据节点的`__sentinel__：hello频道`上发送该Sentinel节点对于主节点的判断以及当前Sentinel节点的信息，同时每个Sentinel节点也会订阅该频道， **<font color = "clime">了解其他Sentinel节点以及它们对主节点的判断。</font>**  
        * 每隔1秒，每个Sentinel节点会向主节点、从节点、其余Sentinel节点发送一条`ping命令` **<font color = "clime">做一次心跳检测。</font>**  
    * **<font color = "red">主观下线和客观下线：</font>** 首先单个Sentinel节点认为数据节点主观下线，询问其他Sentinel节点，Sentinel多数节点认为主节点存在问题，这时该 Sentinel节点会对主节点做客观下线的决定。
    * **<font color = "red">故障转移/主节点选举：</font>** Sentinel节点的领导者根据策略在从节点中选择主节点。    
    * **<font color = "red">Sentinel选举：</font>** Sentinel集群是集中式架构，基于raft算法。  


##### 1.12.3.4.5. Redis集群模式
1. **<font color = "red">根据执行分片的位置，可以分为三种分片方式：</font>** 客户端分片、代理分片、服务器分片：官方Redis Cluster。  
2. **<font color = "clime">Redis集群的服务端：</font>**  
    * 数据分布
        * Redis数据分区
        * 集群限制
            1. <font color = "red">key批量操作支持有限。</font>如mset、mget，目前只支持具有相同slot值的key执行批量操作。对于映射为不同slot值的key由于执行mset、mget等操作可能存在于多个节点上因此不被支持。   
            2. key事务操作支持有限。同理只支持多key在同一节点上的事务操作，当多个key分布在不同的节点上时无法使用事务功能。     
            5. 复制结构只支持一层，从节点只能复制主节点，不支持嵌套树状复制结构。  
    * 故障转移：
        * 故障发现：主观下线(pfail)和客观下线(fail)。  
        * 故障恢复：当从节点通过内部定时任务发现自身复制的主节点进入客观下线； **<font color = "red">从节点发起选举；其余主节点选举投票。</font>**  
3. **<font color = "clime">Redis集群的客户端：</font>**  
&emsp; Redis集群对客户端通信协议做了比较大的修改，为了追求性能最大化，并没有采用代理的方式，而是采用客户端直连节点的方式。    
    * 请求重定向：在集群模式下，Redis接收任何键相关命令时首先计算键对应的槽，再根据槽找出所对应的节点，如果节点是自身，则处理键命令；否则回复MOVED重定向错误，通知客户端请求正确的节点。这个过程称为MOVED重定向。  
    * ASK重定向：Redis集群支持在线迁移槽(slot)和数据来完成水平伸缩，当slot对应的数据从源节点到目标节点迁移过程中，客户端需要做到智能识别，保证键命令可正常执行。例如当一个slot数据从源节点迁移到目标节点时，期间可能出现一部分数据在源节点，而另一部分在目标节点。  
        1. 客户端根据本地slots缓存发送命令到源节点，如果存在键对象则直接执行并返回结果给客户端。  
        2. **<font color = "clime">`如果键对象不存在，则可能存在于目标节点，这时源节点会回复ASK重定向异常。格式如下：(error)ASK{slot}{targetIP}：{targetPort}。`</font>**   
        3. `客户端从ASK重定向异常提取出目标节点信息，`发送asking命令到目标节点打开客户端连接标识，再执行键命令。如果存在则执行，不存在则返回不存在信息。   

    &emsp; **<font color = "clime">ASK与MOVED虽然都是对客户端的重定向控制，但是有着本质区别。ASK重定向说明集群正在进行slot数据迁移，客户端无法知道什么时候迁移完成，因此只能是临时性的重定向，客户端不会更新slots缓存。但是MOVED重定向说明键对应的槽已经明确指定到新的节点，因此需要更新slots缓存。</font>**  

#### 1.12.3.5. Redis常见问题与优化


### 1.12.4. 分布式限流
&emsp; 限流的地方、怎么限流？  
1. **<font color = "clime">一个限流系统的设计要考虑限流对象、限流算法、限流方式、限流设计的要点。</font>**  
2. 限流的位置：网关、系统还是接口？  
3. `限流对象分类：基于请求限流、基于资源限流。` 阿里Sentinel是针对`qps和线程数`进行限流。   
4. 限流算法：  
    * 固定窗口算法，有时会让通过请求量允许为限制的两倍。  
    ![image](http://182.92.69.8:8081/img/microService/problems/problem-24.png)  
    * 滑动窗口算法， **<font color = "clime">`避免了固定窗口计数器带来的双倍突发请求。`</font>** 但时间区间的精度越高，算法所需的空间容量就越大。  
    * 漏桶算法，实现流量整形和流量控制。漏洞底部的设计大小固定，水流速度固定。漏桶算法的缺陷也很明显，当短时间内有大量的突发请求时，即便此时服务器没有任何负载，每个请求也都得在队列中等待一段时间才能被响应。  
    * 令牌桶算法，
        1. 漏桶算法和令牌桶算法在设计上的区别：`漏桶算法中“水滴”代表请求，令牌桶中“水滴”代表请求令牌。`   
        2. **<font color = "blue">`只要令牌桶中存在令牌，那么就允许突发地传输数据直到达到用户配置的门限，`所以它适合于具有突发特性的流量。</font>** 
5. 限流方式有服务降级、服务拒绝。 
5. 限流解决方案：    
    * Java单机限流可以使用AtomicInteger、Semaphore或Guava的RateLimiter来实现，但是上述方案都不支持集群限流。  
    * 集群限流的应用场景有两个，一个是网关，常用的方案有Nginx限流和Spring Cloud Gateway，另一个场景是与外部或者下游服务接口的交互，可以使用redis+lua实现。阿里巴巴的开源限流系统Sentinel也可以针对接口限流。  

### 1.12.5. 服务降级
![image](http://182.92.69.8:8081/img/microService/problems/problem-36.png)  


## 1.13. 分布式消息队列
### 1.13.1. mq
1. 为什么使用mq？  
    * 优点：解耦（调用多个系统，非同步调用）、异步、削锋（削qps）  
2. 消息的推拉机制  
    1. **<font color = "red">一般说的推拉模式指的是broker和consumer之间的，producer和broker之间的模式是推的模式，也就是每次producer生产了消息，会主动推给broker。</font>** 下面的所讲都是基于消费者。    
    2. 推模式以及优缺点
        1. 优点
            1. **<font color = "clime">一个优点就是延迟小，实时性比较好，</font>** broker接收到消息之后就会立刻推送到Consumer。  
            2. 还有一个优点其实就是简化了Consumer端的逻辑，消费端不需要自己去处理这个拉取的逻辑，只需要监听这个消息的topic，然后去专心的处理这个消息的业务逻辑即可。  
        2. 缺点
            1. 第二点简化了Consumer消费端的逻辑的同时，也就复杂化了broker端的逻辑，这其实也不算是优点或者缺点，算是这个模式的一个特点，需要根据场景来选择自己合适的模式。
            2. **<font color = "clime">最大的一个缺点就是推送的速率和消费的速率不好去匹配。</font>** 如果broker拿到消息就推给Consumer，不在乎Consumer的消费能力如何，就往Consumer直接扔，那Consumer有可能会崩溃。
            3. **<font color = "clime">还有一个缺点就是消息推出去之后，无法保证消息发送成功，</font>** push采用的是广播模式，也就是只有服务端和客户端都在同一个频道的时候，推模式才可以成功的将消息推到消费者。
    3. 拉模式以及优缺点  
        1. 优点
            1. **<font color = "clime">最大的优点就是主动权掌握在Consumer这边了，每个消费者的消费能力可能不一样，消费者可以根据自身的情况来拉取消息的请求，如果消费者真的出现那种忙不过来的情况下，可以根据一定的策略去暂停拉取。</font>** 
            2. **<font color = "clime">拉模式也更适合批量消息的发送，推模式是来一个消息就推一个，</font>** 当然也可以缓存一部分消息再推送，但是无法确定Consumer是否能够处理这批推送的消息，拉模式则是Consumer主动来告诉broker，这样broker也可以更好的决定缓存多少消息用于批量发送。
        2. 缺点  
            1. 拉模式需要Consumer对于服务端有一定的了解， **<font color = "clime">`主要的缺点就是实时性较差，`</font>** 针对于服务器端的实时更新的信息，客户端还是难以获取实时的信息。  
            &emsp; **<font color = "clime">不能频繁的去拉取，这样也耗费性能，因此就必须降低请求的频率，请求间隔时间也就意味着消息的延迟。</font>**   
    4. 常见MQ的选择  
    &emsp; RocketMQ最终决定的拉模式，kafka也是如此。  
3. 消息队列选型  
    * RabbitMQ  
    * Kafka  
    * RocketMQ  
4. 消息队列使用事项
    1. 保障高可用  
    2. 消息队列丢失消息有3种情况：生产者丢失数据、mq客户端丢失数据、消费者丢失数据。提供两种方案解决。    
    2. 重复消费  
        消费接口幂等性。  
    3. 顺序消费  
    4. **<font color = "red">~~消息积压~~</font>**  
        `修复消费者问题` ---> ~~将原topic下的消息迁移到一个新的扩容的topic下（分区/队列扩容）~~ ---> 扩容消费者  
        &emsp; 一般这种比较着急的问题，`最好的办法就是临时扩容，用更快的速度来消费数据。`  
        0. 修复消费者问题。  
        1. 临时建立一个新的Topic，然后调整queue的数量为原来的10倍或者20倍，根据堆积情况来决定。  
        2. 然后写一个临时分发消息的consumer程序，这个程序部署上去，消费积压的消息，消费的就是刚刚新建的Topic，消费之后不做耗时的处理，只需要直接均匀的轮询将这些消息轮询的写入到临时创建的queue里面即可。  
        3. 然后增加相应倍数的机器来部署真正的consumer消费，注意这里的Topic，然后让这些consumer去真正的消费这些临时的queue里面的消息。  
  
    &emsp; 一个topic堵住了，新建一个topic去进行分流，临时将queue资源和consumer资源扩大10倍，将消息平均分配到这些新增的queue资源和consumer资源上，以正常10倍的速度来消费消息，等到这些堆积的消息消费完了，便可以恢复到原来的部署架构。  
    &emsp; 这种只是用于临时解决一些异常情况导致的消息堆积的处理，如果消息经常出现堵塞的情况，那该考虑一下彻底增强系统的部署架构了。  


### 1.13.2. Kafka

#### 1.13.2.1. kafka基本概念
&emsp; Apache Kafka是一个分布式流处理平台。支持百万级TPS。    

##### 1.13.2.1.1. kafka生产者
1. Producer发送消息的过程：需要经过拦截器，序列化器和分区器，最终由累加器批量发送至Broker。  
&emsp; Kafka分区策略查看[消息分区](/docs/microService/mq/kafka/topic.md)。  
2. **<font color = "clime">如何提升Producer的性能？异步，批量，压缩。</font>**  
3. 多线程处理：  
&emsp; 多线程单KafkaProducer实例（可以理解为单例模式）、多线程多KafkaProducer实例（可以理解为多例，原型模式）。  
&emsp; **<font color = "clime">如果是对分区数不多的Kafka集群而言，比较推荐使用第一种方法，即在多个producer用户线程中共享一个KafkaProducer实例。若是对那些拥有超多分区的集群而言，釆用第二种方法具有较高的可控性，方便producer的后续管理。</font>**   

##### 1.13.2.1.2. 消息分区
1. 分区说明  
&emsp; 分区（Partition）的作用就是提供负载均衡的能力，单个topic的不同分区可存储在相同或不同节点机上，为实现系统的高伸缩性（Scalability），`不同的分区被放置到不同节点的机器上，`各节点机独立地执行各自分区的读写任务，如果性能不足，可通过添加新的节点机器来增加整体系统的吞吐量。  
2. 服务端物理分区分配
    1. ★★★分区策略  
    在所有broker上均匀地分配分区副本； **<font color = " red">确保分区的每个副本分布在不同的broker上。</font>**  
    2. ~~分区存储数据~~
3. 客户端怎么分区？
    1. 生产者
        1. 分区策略  
        &emsp; 如果没有指定分区，但是 `消息的key不为空，则基于key的哈希值来选择一个分区；`  
        &emsp; 如果既没有指定分区，且 `消息的key也是空，则用轮询的方式选择一个分区。`
    2. 消费者  
        1. 消费者`分组消费`。同一时刻，`一条消息只能被组中的一个消费者实例消费。`  
        2. 消费者消费分区策略
            1. 轮询。  
            2. range策略。 
4. 分区数设置
5. 分区后保持有序，查看顺序消费。    

##### 1.13.2.1.3. kafka消费者
1. 消费者/消费者组/消费者组重平衡  
    1. `消费者组：Kafka消费端确保一个Partition在一个消费者组内只能被一个消费者消费。`  
    2. **<font color = "red">消费者组重平衡：</font>**  
    &emsp; **什么是重平衡？ 假设组内某个实例挂掉了，Kafka能够自动检测到，然后把这个`Failed实例之前负责的分区转移给其他活着的消费者`，这个过程称之为重平衡(Rebalance)。**  
    &emsp; 重平衡触发条件：组成员发生变更、组订阅topic数发生变更、组订阅topic的分区数发生变更。
    3.  **重平衡流程：**  
    &emsp; 引入协调者（每一台Broker上都有一个协调者组件），由协调者为消费组服务，为消费者们做好协调工作。一个消费组只需一个协调者进行服务。  
    &emsp; 1. 当消费者收到协调者的再均衡开始通知时，需要立即提交偏移量；  
    &emsp; **2. 消费者在收到提交偏移量成功的响应后，再发送JoinGroup请求，重新申请加入组，请求中会含有订阅的主题信息；**  
    &emsp; **<font color = "red">3. 当协调者收到第一个JoinGroup请求时，会把发出请求的消费者指定为Leader消费者，</font>**  
    &emsp; **<font color = "red">4. Leader消费者收到JoinGroup响应后，根据消费者的订阅信息制定分配方案，把方案放在SyncGroup请求中，发送给协调者。</font>**  
    &emsp; **<font color = "red">5. 协调者收到分配方案后，再通过SyncGroup响应把分配方案发给所有消费组。</font>**  
    4. **如何避免重平衡？**  
    &emsp; **<font color = "clime">其实只需要避免实例减少的情况就行了。</font>** ~~消费时间过长~~  
2. 消费者位移管理  
    &emsp; **<font color = "red">位移提交有两种方式：</font><font color = "clime">自动提交、手动提交。</font>**  
3. 怎样消费  
    * 消费者分区分配策略：轮询RoundRobin、range策略。
    * 消费语义：至少一次、至多一次、正好一次。

##### 1.13.2.1.4. kafka服务端


#### 1.13.2.2. kafka特性
* 高并发：支持百万级TPS。    
    * 高性能：磁盘I/O-顺序读写、基于Sendfile实现零拷贝。  
    * 高可用：Kafka副本机制。  
* 分布式：  
    * 可靠性：副本的一致性保证、可以保证消息队列不丢失、幂等（重复消费）和事务。  
    * 如何让Kafka的消息有序？  


##### 1.13.2.2.1. 【1】高性能(读写机制) ，Kafka为什么吞吐量大、速度快？
&emsp; Kafka的消息是保存或缓存在磁盘上的，一般认为在磁盘上读写数据是会降低性能的，因为寻址会比较消耗时间，但是实际上，Kafka的特性之一就是高吞吐率。 **Kafka之所以能这么快，是因为：「`顺序写磁盘、【大量使用内存页】`、零拷贝技术的使用」..**  

###### 1.13.2.2.1.1. 内存
&emsp; 为了优化读写性能，`Kafka利用了操作系统本身的Page Cache，就是利用操作系统自身的内存而不是JVM空间内存。`  

###### 1.13.2.2.1.2. 持久化 / 磁盘IO - 顺序读写
&emsp; kafka的消息是不断追加到文件中的，这个特性使kafka可以充分利用磁盘的顺序读写性能。  

###### 1.13.2.2.1.3. 网络IO优化
1. Kafka 的数据传输通过TransportLayer来完成，其子类 PlaintextTransportLayer 通过Java NIO的FileChannel的transferTo和transferFrom方法实现零拷贝。  
2. 批量读写、批量压缩。  

##### 1.13.2.2.2. 【2】高可用与数据一致性(副本机制)
1. **<font color = "blue">`Kafka副本中只有Leader可以和客户端交互，进行读写，`其他副本是只能同步，不能分担读写压力。</font>**  
2. 服务端Leader的选举：从ISR（保持同步的副本）集合副本中选取。  
3. 服务端副本消息的同步：  
&emsp; LEO，低水位，记录了日志的下一条消息偏移量，即当前最新消息的偏移量加一；HW，高水位，界定了消费者可见的消息，是ISR队列中最小的LEO。  
    1. Follower副本更新LEO和HW：  
    &emsp; 更新LEO和HW的时机： **<font color = "clime">Follower向Leader拉取了消息之后。(⚠️注意：Follower副本只和Leader副本交互。)</font>**  
    &emsp; **<font color = "red">会用获取的偏移量加1来更新LEO，并且用Leader的HW值和当前LEO的最小值来更新HW。</font>**  
    2. Leader副本上LEO和HW的更新：  
        * 正常情况下Leader副本的更新时机有两个：一、收到生产者的消息；二、被Follower拉取消息。(⚠️注意：Leader副本即和Follower副本交互，也和生产者交互。)  
            * 当收到生产者消息时，会用当前偏移量加1来更新LEO，然后取LEO和远程ISR副本中LEO的最小值更新HW。 
            * 当Follower拉取消息时，会更新Leader上存储的Follower副本LEO，然后判断是否需要更新HW，更新的方式和上述相同。 
        * 除了这两种正常情况，当发生故障时，例如Leader宕机，Follower被选为新的Leader，会尝试更新HW。还有副本被踢出ISR时，也会尝试更新HW。 
4. 在服务端Leader切换时，会存在数据丢失和数据不一致的问题。  
    1. 主从切换，数据不一致的情况如下：  
    &emsp; A作为Leader，A已写入m0、m1两条消息，且HW为2，而B作为Follower，只有m0消息，且HW为1。若A、B同时宕机，且B重启时，A还未恢复，则B被选为Leader。  
    &emsp; 在B重启作为Leader之后，收到消息m2。A宕机重启后，向成为Leader的B发送Fetch请求，发现自己的HW和B的HW一致，都是2，因此不会进行消息截断，而这也造成了数据不一致。  
    2. 引入Leader Epoch机制：  
    &emsp; **<font color = "blue">为了解决HW可能造成的数据丢失和数据不一致问题，Kafka引入了Leader Epoch机制。</font>** 在每个副本日志目录下都有一个leader-epoch-checkpoint文件，用于保存Leader Epoch信息。  
    &emsp; Leader Epoch，分为两部分，前者Epoch，表示Leader版本号，是一个单调递增的正整数，每当Leader变更时，都会加1；`后者StartOffset，为每一代Leader写入的第一条消息的位移。`   
5. 客户端数据请求：  
&emsp; 集群中的每个broker都会缓存所有主题的分区副本信息，客户端会定期发送元数据请求，然后将获取的集群元数据信息进行缓存。  

##### 1.13.2.2.3. ~~可靠性~~
&emsp; Kafka作为一个商业级消息中间件，消息可靠性的重要性可想而知。 **<font color = "clime">如何确保消息的`精确传输`？如何确保消息的`准确存储`？如何确保消息的`正确消费`？</font>** 这些都是需要考虑的问题。  

&emsp; 可靠性保证：确保系统在各种不同的环境下能够发生一致的行为。  
&emsp; Kafka的保证：  

* 保证分区消息的顺序
	* 如果使用同一个生产者往同一个分区写入消息，而且消息B在消息A之后写入。
	* 那么Kafka可以保证消息B的偏移量比消息A的偏移量大，而且消费者会先读取消息A再读取消息B。
* 只有当消息被写入分区的所有同步副本时（文件系统缓存），它才被认为是已提交。
	* 生产者可以选择接收不同类型的确认，控制参数acks。
* 只要还有一个副本是活跃的，那么“已提交的消息就不会丢失”。  
* 消费者只能读取已经提交的消息。  


###### 1.13.2.2.3.1. 如何保证消息队列不丢失?  
1. 在Producer端、Broker端、Consumer端都有可能丢失消息。  
2. Producer端：  
    1. 为防止Producer端丢失消息， **<font color = "red">除了将ack设置为all，表明所有副本 Broker 都要接收到消息，才算“已提交”。</font>**  
    2. `还可以使用带有回调通知的发送API，即producer.send(msg, callback)`。  
3. Broker端:  
&emsp; ★★★Kafka`没有`提供`同步`刷盘的方式。要完全让kafka保证单个broker不丢失消息是做不到的，只能通过调整刷盘机制的参数缓解该情况。  
&emsp; 为了解决该问题，kafka通过producer和broker协同处理单个broker丢失参数的情况。 **<font color = "red">`一旦producer发现broker消息丢失，即可自动进行retry。`</font>** 除非retry次数超过阀值（可配置），消息才会丢失。此时需要生产者客户端手动处理该情况。  
4. ~~Consumer端：~~  
&emsp; 采用手动提交位移。  
5. `★★★方案二：使用补偿机制`  
&emsp; 服务端丢失消息处理：建立消息表，发送消息前保存表记录，发送后更新表记录。  
&emsp; 客户端丢失消息处理：服务端提供查询接口。 

###### 1.13.2.2.3.2. 分区保证消费顺序  
1. Apache Kafka官方保证了partition内部的数据有效性（追加写、offset读）。为了提高Topic的并发吞吐能力，可以提高Topic的partition数，并通过设置partition的replica来保证数据高可靠。但是在多个Partition时，不能保证Topic级别的数据有序性。  
2. 顺序消费
    1. 前提：[消息分区](/docs/microService/mq/kafka/topic.md)  
    2. **<font color = "clime">针对消息有序的业务需求，可以分为全局有序和局部有序：</font>**  
        * 全局有序：一个Topic下的所有消息都需要按照生产顺序消费。
        * 局部有序：一个Topic下的消息，只需要满足同一业务字段的要按照生产顺序消费。例如：Topic消息是订单的流水表，包含订单orderId，业务要求同一个orderId的消息需要按照生产顺序进行消费。
3. 全局有序：  
&emsp; 一个生产者、一个分区、一个消费者（或使用分布式锁），并严格到一个消费线程。   
&emsp; ~~而且对应的consumer也要使用单线程或者保证消费顺序的线程模型，否则会出现消费端造成的消费乱序。~~  
4. 局部有序：    
&emsp; 多分区时，要满足局部有序，只需要在发消息的时候指定Partition Key，Kafka对其进行Hash计算，根据计算结果决定放入哪个Partition。这样Partition Key相同的消息会放在同一个Partition。此时，Partition的数量仍然可以设置多个，提升Topic的整体吞吐量。   
&emsp; **<font color = "clime">在不增加分区、消费者情况下，每个partition是固定分配给某个消费者线程进行消费的，</font>** 所以对于在同一个分区的消息来说，是严格有序的。  
5. ~~注意事项：~~  
    1. 消息重试对顺序消息，无影响。   
6. `业务上实现有序消费（⚠️★★★着重看看）`  
    &emsp; 除了消息队列自身的顺序消费机制，可以合理地对消息进行改造，从业务上实现有序的目的。具体的方式有以下几种：  
    1. 根据不同的业务场景，以发送端或者消费端时间戳为准  
    &emsp; 比如在电商大促的秒杀场景中，如果要对秒杀的请求进行排队，就可以使用秒杀提交时服务端的时间戳，虽然服务端不一定保证时钟一致，但是在这个场景下，不需要保证绝对的有序。  
    2. 每次消息发送时生成唯一递增的 ID  
    &emsp; 在每次写入消息时，可以考虑添加一个单调递增的序列 ID，在消费端进行消费时，缓存最大的序列 ID，只消费超过当前最大的序列 ID 的消息。这个方案和分布式算法中的 Paxos 很像，虽然无法实现绝对的有序，但是可以保证每次只处理最新的数据，避免一些业务上的不一致问题。  
    3. 通过缓存时间戳的方式  
    &emsp; 这种方式的机制和递增 ID 是一致的，即当生产者在发送消息时，添加一个时间戳，消费端在处理消息时，通过缓存时间戳的方式，判断消息产生的时间是否最新，如果不是则丢弃，否则执行下一步。  


###### 1.13.2.2.3.3. 消费语义介绍 
&emsp; 消息传递语义message delivery semantic，简单说就是消息传递过程中消息传递的保证性。主要分为三种：  

* at most once：最多一次。消息可能丢失也可能被处理，但最多只会被处理一次。  
* at least once：至少一次。消息不会丢失，但可能被处理多次。可能重复，不会丢失。  
* exactly once：精确传递一次。消息被处理且只会被处理一次。不丢失不重复就一次。  


&emsp; at most once，最多一次，可以理解为可能发生消息丢失；at least once，至少一次，可以理解为可能发生重复消费。kafka通过ack的配置来实现这两种。  
&emsp; 理想情况下肯定是希望系统的消息传递是严格exactly once，也就是保证不丢失、只会被处理一次，但是很难做到。 **exactly once也被称为幂等性。**  

![image](http://182.92.69.8:8081/img/microService/mq/kafka/kafka-119.png)  

###### 1.13.2.2.3.4. 幂等（重复消费）
1. **幂等又称为exactly once（精确传递一次。消息被处理且只会被处理一次。不丢失不重复就一次）。**  
2. `Kafka幂等是针对生产者角度的特性。`kafka只保证producer单个会话中的单个分区幂等。  
3. **<font color = "red">Kafka幂等性实现机制：（`producer_id和序列号，进行比较`）</font>**  
    1. `每一个producer在初始化时会生成一个producer_id，并为每个目标partition维护一个"序列号"；`
    2. producer每发送一条消息，会将 \<producer_id,分区\> 对应的“序列号”加1；  
    3. broker端会为每一对 \<producer_id,分区\> 维护一个序列号，对于每收到的一条消息，会判断服务端的SN_old和接收到的消息中的SN_new进行对比：  

        * 如果SN_old < SN_new+1，说明是重复写入的数据，直接丢弃。    
        * 如果SN_old > SN_new+1，说明中间有数据尚未写入，或者是发生了乱序，或者是数据丢失，将抛出严重异常：OutOfOrderSeqenceException。 
4. `★★★Kafka消费者的幂等性（kafka怎样保证消息仅被消费一次？）`  
    &emsp; 在使用kafka时，大多数场景对于数据少量的不一致（重复或者丢失）并不关注，比如日志，因为不会影响最终的使用或者分析，但是在某些应用场景（比如业务数据），需要对任何一条消息都要做到精确一次的消费，才能保证系统的正确性，`kafka并不提供准确一致的消费API，需要在实际使用时借用外部的一些手段来保证消费的精确性。`    
    &emsp; **<font color = "clime">当消费者消费到了重复的数据的时候，消费者需要去过滤这部分的数据。主要有以下两种思路：</font>**  
    1. 将消息的offset存在消费者应用中或者第三方存储的地方  
    &emsp; 可以将这个数据存放在redis或者是内存中，消费消息时，如果有这条数据的话，就不会去做后续操作。  
    2. `数据落库的时候，根据主键去过滤`  
    &emsp; 在落库时，如果不不在这条数据，则去新增，如果存在则去修改。  

###### 1.13.2.2.3.5. 事务


## 1.14. 网络IO

### 1.14.1. ~~服务器处理连接~~
&emsp; 多线程模式和多进程模式是类似的，也是分为下面几种：  

1. 主进程accept，创建子线程处理
2. 子线程accept
3. 线程池


### 1.14.2. 通信基础
&emsp; 1. 序列化。  

### 1.14.3. 网络IO
&emsp; 前言：`一次网络I/O事件，包含网络连接（IO多路复用） -> 读写数据（零拷贝） -> 处理事件。`  

#### 1.14.3.1. 五种I/O模型
1. **<font color = "red">网络IO的本质就是socket流的读取，通常一次IO读操作会涉及到两个对象和两个阶段。**</font>  
    * **<font color = "clime">两个对象：用户进程（线程）、内核对象（内核态和用户态）。</font><font color = "blue">用户进程请求内核。</font>**   
    * **<font color = "clime">`内核中涉及两个阶段：1. 等待数据准备；2. 数据从内核空间拷贝到用户空间。`</font>**  
    &emsp; `⚠️基于以上两个阶段就产生了五种不同的IO模式，`分别是：阻塞I/O模型、非阻塞I/O模型、多路复用I/O模型、信号驱动I/O模型、异步I/O模型。其中，前四种被称为同步I/O。  
2. **<font color = "blue">同步（等待结果）和阻塞（线程）：</font>**  
    * 异步和同步：对于请求结果的获取是客户端主动获取结果，还是由服务端来通知结果。    
    * 阻塞和非阻塞：在等待这个函数返回结果之前，当前的线程是处于挂起状态还是运行状态。 
3. 同步阻塞I/O：  
    1. 流程：  
        ![image](http://182.92.69.8:8081/img/microService/netty/netty-1.png)  
        1. 用户进程发起recvfrom系统调用内核。用户进程【同步】等待结果；
        2. 内核等待I/O数据返回，此时用户进程处于【阻塞】，一直等待内核返回；
        3. I/O数据返回后，内核将数据从内核空间拷贝到用户空间；  
        4. 内核将数据返回给用户进程。  
    特点：两阶段都阻塞。  
    2. BIO采用多线程时，大量的线程占用很大的内存空间，并且线程切换会带来很大的开销，10000个线程真正发生读写事件的线程数不会超过20%，`每次accept都开一个线程也是一种资源浪费。`  
4. 同步 非阻塞（轮询）I/O：  
    1. 流程：  
        ![image](http://182.92.69.8:8081/img/microService/netty/netty-2.png)  
        1. 用户进程发起recvfrom系统调用内核。用户进程【同步】等待结果；
        2. 内核等待I/O数据返回。无I/O数据返回时，内核返回给用户进程ewouldblock结果。`【`非阻塞`】用户进程，立马返回结果。`但 **<font color = "clime">用户进程要`【主动轮询】`查询结果。</font>**  
        3. I/O数据返回后，内核将数据从内核空间拷贝到用户空间；  
        4. 内核将数据返回给用户进程。  
    特点：`第一阶段不阻塞但要轮询，`第二阶段阻塞。  
    2. NIO`每次轮询所有fd（包括没有发生读写事件的fd）会很浪费cpu。`  
5. 多路复用I/O：（~~同步阻塞，又基于回调通知~~）  
    1. 为什么有多路复用？  
    &emsp; 如果一个I/O流进来，就开启一个进程处理这个I/O流。那么假设现在有一百万个I/O流进来，那就需要开启一百万个进程一一对应处理这些I/O流（这就是传统意义下的多进程并发处理）。思考一下，一百万个进程，CPU占有率会多高，这个实现方式及其的不合理。所以人们提出了 **<font color = "red">I/O多路复用这个模型，一个线程，通过记录I/O流的状态来同时管理多个I/O，可以提高服务器的吞吐能力。</font>**  
    &emsp; `多路是指多个socket套接字，复用是指复用同一个进程。`  
    2. 流程：  
        ![image](http://182.92.69.8:8081/img/microService/netty/netty-3.png)  
        1. 用户多进程或多线程发起select系统调用，复用器Selector会监听注册进来的进程事件。用户进程【同步】等待结果；
        2. 内核等待I/O数据返回，无数据返回时，进程【阻塞】于select调用；
        2. I/O数据返回后，内核将数据从内核空间拷贝到用户空间， **<font color = "clime">Selector`通知`哪个进程哪个事件；</font>**  
        4. 进程发起recvfrom系统调用。  
    3. 多路复用`能支持更多的并发连接请求。`  
    &emsp; 多路复用I/O模型和阻塞I/O模型并没有太大的不同，事实上，还更差一些，因为它需要使用两个系统调用(select和recvfrom)，而阻塞I/O模型只有一次系统调用(recvfrom)。但是Selector的优势在于它可以同时处理多个连接。   
6. 信号驱动IO  
7. 异步IO

#### 1.14.3.2. I/O多路复用详解
1. **<font color = "clime">`~~select,poll,epoll只是I/O多路复用模型中第一阶段，即获取网络数据、用户态和内核态之间的拷贝。~~`</font>** 此阶段会阻塞线程。  
2. **select()：**  
    1. **select运行流程：**  
        &emsp; **<font color = "red">select()运行时会`将fd_set（文件句柄集合）从用户态拷贝到内核态`。</font>** 在内核态中线性扫描socket，即采用轮询。如果有事件返回，会将内核态的数组相应的FD置位。最后再将内核态的数据返回用户态。  
    2. **select机制的问题：（拷贝、两次轮询、FD置位）**  
        * 为了减少数据拷贝带来的性能损坏，内核对被监控的fd_set集合大小做了限制，并且这个是通过宏控制的，大小不可改变（限制为1024）。  
        * 每次调用select， **<font color = "red">1)需要把fd_set集合从用户态拷贝到内核态，</font>** **<font color = "clime">2)需要在内核遍历传递进来的所有fd_set（对socket进行扫描时是线性扫描，即采用轮询的方法，效率较低），</font>** **<font color = "red">3)如果有数据返回还需要从内核态拷贝到用户态。</font>** 如果fd_set集合很大时，开销比较大。 
        * 由于运行时，需要将FD置位，导致fd_set集合不可重用。  
        * **<font color = "clime">select()函数返回后，</font>** 调用函数并不知道是哪几个流（可能有一个，多个，甚至全部）， **<font color = "clime">还得再次遍历fd_set集合处理数据，即采用`无差别轮询`。</font>**   
        * ~~惊群~~   
3. **poll()：** 运行机制与select()相似。将fd_set数组改为采用链表方式pollfds，没有连接数的限制，并且pollfds可重用。   
4. **epoll()：**   
    1. **epoll的三个函数：**  
        <!-- 
        * 调用epoll_create，会在内核cache里建个红黑树，同时也会再建立一个rdllist双向链表。 
        * epoll_ctl将被监听的描述符添加到红黑树或从红黑树中删除或者对监听事件进行修改。
        * 双向链表，用于存储准备就绪的事件，当epoll_wait调用时，仅查看这个rdllist双向链表数据即可。epoll_wait阻塞等待注册的事件发生，返回事件的数目，并将触发的事件写入events数组中。   
        -->
        ![image](http://182.92.69.8:8081/img/microService/netty/netty-119.png)  
        1. 执行epoll_create()时，创建了红黑树和就绪链表（rdllist双向链表）；
        2. 执行epoll_ctl()时，如果增加socket句柄，则检查在红黑树中是否存在，存在立即返回，不存在则添加到树干上，然后向内核注册回调函数，用于当中断事件来临时向准备就绪链表中插入数据；
        3. 执行epoll_wait()时立刻返回准备就绪链表里的数据即可。  
        
        -----------
        1. 首先epoll_create创建一个epoll文件描述符，底层同时创建一个红黑树，和一个就绪链表；红黑树存储所监控的文件描述符的节点数据，就绪链表存储就绪的文件描述符的节点数据；  
        2. epoll_ctl将会添加新的描述符，首先判断是红黑树上是否有此文件描述符节点，如果有，则立即返回。如果没有， 则在树干上插入新的节点，并且告知内核注册回调函数。`【当接收到某个文件描述符过来数据时，那么内核将该节点插入到就绪链表里面。】`  
        3. epoll_wait将会接收到消息，并且将数据拷贝到用户空间，清空链表。对于LT模式epoll_wait清空就绪链表之后会检查该文件描述符是哪一种模式，`如果为LT模式，且必须该节点确实有事件未处理，那么就会把该节点重新放入到刚刚删除掉的且刚准备好的就绪链表，`epoll_wait马上返回。ET模式不会检查，只会调用一次。  
    2. **epoll机制的工作模式：**  
        * LT模式（默认，水平触发，level trigger）：当epoll_wait检测到某描述符事件就绪并通知应用程序时，应用程序可以不立即处理该事件； **<font color = "clime">下次调用epoll_wait时，会再次响应应用程序并通知此事件。</font>**    
        * ET模式（边缘触发，edge trigger）：当epoll_wait检测到某描述符事件就绪并通知应用程序时，应用程序必须立即处理该事件。如果不处理，下次调用epoll_wait时，不会再次响应应用程序并通知此事件。（直到做了某些操作导致该描述符变成未就绪状态了，也就是说 **<font color = "clime">边缘触发只在状态由未就绪变为就绪时只通知一次。</font>** ）   

        &emsp; 由此可见：ET模式的效率比LT模式的效率要高很多。只是如果使用ET模式，就要保证每次进行数据处理时，要将其处理完，不能造成数据丢失，这样对编写代码的人要求就比较高。  
        &emsp; 注意：ET模式只支持非阻塞的读写：为了保证数据的完整性。  

    3. **epoll机制的优点：**  
        * 调用epoll_ctl时拷贝进内核并保存，之后每次epoll_wait不拷贝。  
        * epoll()函数返回后，调用函数以O(1)复杂度遍历。  
5. 两种IO多路复用模式：[Reactor和Proactor](/docs/microService/communication/Netty/Reactor.md)  

#### 1.14.3.3. 多路复用之Reactor模式
1. `Reactor，是网络编程中基于IO多路复用的一种设计模式，是【event-driven architecture】的一种实现方式，处理多个客户端并发的向服务端请求服务的场景。`    
2. **<font color = "red">Reactor模式核心组成部分包括Reactor线程和worker线程池，</font> <font color = "clime">而根据Reactor的数量和线程池的数量，又将Reactor分为三种模型。</font>**  
    * `Reactor负责监听（建立连接）和处理请求（分发事件、读写）。`  
    * 线程池负责处理事件。  
3. **单线程模型（单Reactor单线程）**  
![image](http://182.92.69.8:8081/img/microService/netty/netty-91.png)  
&emsp; ~~这是最基本的单Reactor单线程模型。其中Reactor线程，负责多路分离套接字，有新连接到来触发connect事件之后，交由Acceptor进行处理，有IO读写事件之后交给hanlder处理。~~  
&emsp; ~~Acceptor主要任务就是构建handler，在获取到和client相关的SocketChannel之后，绑定到相应的hanlder上，对应的SocketChannel有读写事件之后，基于racotor分发，hanlder就可以处理了（所有的IO事件都绑定到selector上，有Reactor分发）。~~  
&emsp; **<font color = "red">Reactor单线程模型，指的是所有的IO操作都在同一个NIO线程上面完成。</font>** 单个NIO线程会成为系统瓶颈，并且会有节点故障问题。   
4. **多线程模型（单Reactor多线程）**  
![image](http://182.92.69.8:8081/img/microService/netty/netty-92.png)  
&emsp; ~~相对于第一种单线程的模式来说，在处理业务逻辑，也就是获取到IO的读写事件之后，交由线程池来处理，这样可以减小主reactor的性能开销，从而更专注的做事件分发工作了，从而提升整个应用的吞吐。~~  
&emsp; Rector多线程模型与单线程模型最大的区别就是有一组NIO线程处理IO操作。在极个别特殊场景中，一个NIO线程（Acceptor线程）负责监听和处理所有的客户端连接可能会存在性能问题。    
5. **主从多线程模型（多Reactor多线程）**    
![image](http://182.92.69.8:8081/img/microService/netty/netty-93.png)  
&emsp; 主从Reactor多线程模型中，Reactor线程拆分为mainReactor和subReactor两个部分， **<font color = "clime">`mainReactor只处理连接事件`，`读写事件交给subReactor来处理`。</font>** 业务逻辑还是由线程池来处理。  
&emsp; mainRactor只处理连接事件，用一个线程来处理就好。处理读写事件的subReactor个数一般和CPU数量相等，一个subReactor对应一个线程。  
&emsp; ~~第三种模型比起第二种模型，是将Reactor分成两部分：~~  
&emsp; ~~mainReactor负责监听server socket，用来处理新连接的建立，将建立的socketChannel指定注册给subReactor。~~  
&emsp; ~~subReactor维护自己的selector，基于mainReactor注册的socketChannel多路分离IO读写事件，读写网络数据，对业务处理的功能，另其扔给worker线程池来完成。~~  
&emsp; Reactor主从多线程模型中，一个连接accept专门用一个线程处理。  
&emsp; 主从Reactor线程模型的特点是：服务端用于接收客户端连接的不再是1个单独的NIO线程，而是一个独立的NIO线程池。Acceptor接收到客户端TCP连接请求处理完成后（可能包含接入认证等），将新创建的SocketChannel注册到IO线程池（sub reactor线程池）的某个IO线程上，由它负责SocketChannel的读写和编解码工作。Acceptor线程池仅仅只用于客户端的登陆、握手和安全认证，一旦链路建立成功，就将链路注册到后端subReactor线程池的IO线程上，由IO线程负责后续的IO操作。  
&emsp; 利用主从NIO线程模型，可以解决1个服务端监听线程无法有效处理所有客户端连接的性能不足问题。  

#### 1.14.3.4. IO性能优化之零拷贝
1. 比较常见的I/O流程是读取磁盘文件传输到网络中。  
2. **<font color = "clime">I/O传输中的一些基本概念：</font>**  
    * 状态切换：内核态和用户态之间的切换。  
    * CPU拷贝：内核态和用户态之间的复制。 **零拷贝："零"更多的是指在用户态和内核态之间的复制是0次。**   
    * DMA拷贝：设备（或网络）和内核态之间的复制。  
3. 仅CPU方式：  
	![image](http://182.92.69.8:8081/img/microService/netty/netty-66.png)  
	&emsp; **仅CPU方式读数据read流程：**  

	* 当应用程序需要读取磁盘数据时，调用read()从用户态陷入内核态，read()这个系统调用最终由CPU来完成；
	* CPU向磁盘发起I/O请求，磁盘收到之后开始准备数据；
	* 磁盘将数据放到磁盘缓冲区之后，向CPU发起I/O中断，报告CPU数据已经Ready了；
	* CPU收到磁盘控制器的I/O中断之后，开始拷贝数据，完成之后read()返回，再从内核态切换到用户态；  

    `仅CPU方式有4次状态切换，4次CPU拷贝。`    

4. CPU & DMA（Direct Memory Access，直接内存访问）方式   
	![image](http://182.92.69.8:8081/img/microService/netty/netty-68.png)    
	&emsp; **<font color = "red">最主要的变化是，CPU不再和磁盘直接交互，而是DMA和磁盘交互并且将数据从磁盘缓冲区拷贝到内核缓冲区，因此减少了2次CPU拷贝。共2次CPU拷贝，2次DMA拷贝，4次状态切换。之后的过程类似。</font>**  

	* 读过程涉及2次空间切换（需要CPU参与）、1次DMA拷贝、1次CPU拷贝；
	* 写过程涉及2次空间切换、1次DMA拷贝、1次CPU拷贝；  
5. `零拷贝技术的几个实现手段包括：mmap+write、sendfile、sendfile+DMA收集、splice等。`  
6. **<font color = "blue">mmap（内存映射）：</font>**   
    &emsp; **<font color = "clime">mmap是Linux提供的一种内存映射文件的机制，它实现了将内核中读缓冲区地址与用户空间缓冲区地址进行映射，从而实现内核缓冲区与用户缓冲区的共享，</font>** 又减少了一次cpu拷贝。总共包含1次cpu拷贝，2次DMA拷贝，4次状态切换。此流程中，cpu拷贝从4次减少到1次，但状态切换还是4次。   
    &emsp; mmap+write简单来说就是使用`mmap替换了read+write中的read操作`，减少了一次CPU的拷贝。  
    &emsp; `mmap主要实现方式是将读缓冲区的地址和用户缓冲区的地址进行映射，内核缓冲区和应用缓冲区共享，从而减少了从读缓冲区到用户缓冲区的一次CPU拷贝。`  

    ![image](http://182.92.69.8:8081/img/microService/netty/netty-142.png)  

    &emsp; 整个过程发生了4次用户态和内核态的上下文切换和3次拷贝，具体流程如下：  

    1. 用户进程通过mmap()方法向操作系统发起调用，上下文从用户态转向内核态。
    2. DMA控制器把数据从硬盘中拷贝到读缓冲区。
    3. 上下文从内核态转为用户态，mmap调用返回。
    4. 用户进程通过write()方法发起调用，上下文从用户态转为内核态。
    5. CPU将读缓冲区中数据拷贝到socket缓冲区。
    6. DMA控制器把数据从socket缓冲区拷贝到网卡，上下文从内核态切换回用户态，write()返回。

    &emsp; mmap的方式节省了一次CPU拷贝，同时由于用户进程中的内存是虚拟的，`只是映射到内核的读缓冲区，所以可以节省一半的内存空间，比较适合大文件的传输。`  
7. sendfile（函数调用）：  
    1. **<font color = "red">sendfile建立了两个文件之间的传输通道。</font>** `通过使用【sendfile数据可以直接在内核空间进行传输，】因此避免了用户空间和内核空间的拷贝，`同时由于使用sendfile替代了read+write从而节省了一次系统调用，也就是2次上下文切换。   
    2. 流程：  
        ![image](http://182.92.69.8:8081/img/microService/netty/netty-148.png)  
        &emsp; 整个过程发生了2次用户态和内核态的上下文切换和3次拷贝，具体流程如下：  
        1. 用户进程通过sendfile()方法向操作系统发起调用，上下文从用户态转向内核态。
        2. DMA控制器把数据从硬盘中拷贝到读缓冲区。
        3. CPU将读缓冲区中数据拷贝到socket缓冲区。
        4. DMA控制器把数据从socket缓冲区拷贝到网卡，上下文从内核态切换回用户态，sendfile调用返回。  
    3. sendfile方式中，应用程序只需要调用sendfile函数即可完成。数据不经过用户缓冲区，该数据无法被修改。但减少了2次状态切换，即只有2次状态切换、1次CPU拷贝、2次DMA拷贝。  
8. sendfile+DMA收集  
9. splice方式  
&emsp; splice系统调用是Linux在2.6版本引入的，其不需要硬件支持，并且不再限定于socket上，实现两个普通文件之间的数据零拷贝。  
&emsp; splice系统调用可以在内核缓冲区和socket缓冲区之间建立管道来传输数据，避免了两者之间的CPU拷贝操作。  
&emsp; **<font color = "clime">splice也有一些局限，它的两个文件描述符参数中有一个必须是管道设备。</font>**  
![image](http://182.92.69.8:8081/img/microService/netty/netty-35.png)  

### 1.14.4. Socket编程
&emsp; `Socket是对TCP/IP协议的封装。`Socket只是个接口不是协议，通过Socket才能使用TCP/IP协议，除了TCP，也可以使用UDP协议来传递数据。  

### 1.14.5. NIO
&emsp; **BIO即Block I/O，同步并阻塞的IO。**  
&emsp; **<font color = "red">NIO，同步非阻塞I/O，基于io多路复用模型，即select，poll，epoll。</font>**  
&emsp; **<font color = "red">AIO，异步非阻塞的IO。</font>**  

&emsp; BIO方式适用于连接数目比较小且固定的架构，这种方式对服务器资源要求比较高，并发局限于应用中，JDK1.4以前的唯一选择，但程序直观简单易理解。  
&emsp; NIO方式适用于连接数目多且连接比较短（轻操作）的架构，比如聊天服务器，并发局限于应用中，编程比较复杂，JDK1.4开始支持。  
&emsp; AIO方式适用于连接数目多且连接比较长（重操作）的架构，比如相册服务器，充分调用OS参与并发操作，编程比较复杂，JDK7开始支持。  

### 1.14.6. Netty
#### 1.14.6.1. Netty简介
1. Netty是一个`非阻塞I/O` <font color = "red">客户端-服务器框架</font>，主要用于开发Java网络应用程序，如协议服务器和客户端。`异步事件驱动`的网络应用程序框架和工具用于简化网络编程，例如TCP和UDP套接字服务器。Netty包括了`反应器编程模式`的实现。  
&emsp; 除了作为异步网络应用程序框架，Netty还包括了对HTTP、HTTP2、DNS及其他协议的支持，涵盖了在Servlet容器内运行的能力、`对WebSockets的支持`、与Google Protocol Buffers的集成、对SSL/TLS的支持以及对用于SPDY协议和消息压缩的支持。  
2. **<font color = "clime">为什么要用Netty？</font>**  
    &emsp; 在实际的网络开发中，其实很少使用Java NIO原生的API。主要有以下原因：  

    * NIO的类库和API繁杂，使用麻烦，需要熟练掌握Selector、ServerSocketChannel、SockctChannel、ByteBuffer等。  
    * `原生API使用单线程模型，不能很好利用多核优势；`  
    * 原生API是直接使用的IO数据，没有做任何封装处理，对数据的编解码、TCP的粘包和拆包、客户端断连、网络的可靠性和安全性方面没有做处理；  
    * **<font color = "red">JDK NIO的BUG，例如臭名昭著的epoll bug，它会导致Selector空轮询，最终导致CPU100%。</font>官方声称在JDK1.6版本的update18修复了该问题，但是直到JDK 1.7版本该问题仍旧存在，只不过该BUG发生概率降低了一些而已，它并没有得到根本性解决。该BUG以及与该BUG相关的问题单可以参见以下链接内容。** 
        * http://bugs.java.com/bugdatabase/viewbug.do?bug_id=6403933  
        * http://bugs.java.com/bugdalabase/viewbug.do?bug_id=21477l9  

#### 1.14.6.2. Netty运行流程
&emsp; [Netty运行流程](/docs/microService/communication/Netty/operation.md)   

&emsp; **<font color = "clime">一般来说，使用Bootstrap创建启动器的步骤可分为以下几步：</font>**  
![image](http://182.92.69.8:8081/img/microService/netty/netty-151.png)  
1. 创建服务器启动辅助类，服务端是ServerBootstrap。  
    &emsp; 需要设置事件循环组EventLoopGroup，如果使用reactor主从模式，需要创建2个：   
    * <font color = "clime">`创建boss线程组（EventLoopGroup bossGroup）`用于服务端接受客户端的连接；  
    * `创建worker线程组（EventLoopGroup workerGroup）`用于进行SocketChannel的数据读写。</font>  
2. 对ServerBootstrap进行配置，配置项有channel,handler,option。  
3. 绑定服务器端口并启动服务器，同步等待服务器启动完毕。  
4. 阻塞启动线程，并同步等待服务器关闭，因为如果不阻塞启动线程，则会在finally块中执行优雅关闭，导致服务器也会被关闭了。  

![image](http://182.92.69.8:8081/img/microService/netty/netty-150.png)  
![image](http://182.92.69.8:8081/img/microService/netty/netty-44.png)  

&emsp; Netty整体运行流程：  
![image](http://182.92.69.8:8081/img/microService/netty/netty-87.png) 

1. `Netty基于Reactor，parentGroup用于处理连接，childGroup用于处理数据读写。`  
2. 当一个连接到达时，Netty就会创建一个Channel，然后`从EventLoopGroup中分配一个EventLoop来给这个Channel绑定上`，在该Channel的整个生命周期中都是由这个绑定的EventLoop来服务的。  
3. 职责链ChannelPipeline，负责事件在职责链中的有序传播，同时负责动态地编排职责链。职责链可以选择监听和处理自己关心的事件，它可以拦截处理和向后/向前传播事件。 ChannelHandlerContext代表了ChannelHandler和ChannelPipeline之间的绑定。  

#### 1.14.6.3. Netty核心组件
1. `由netty运行流程可以看出Netty核心组件有Bootstrap、channel相关、EventLoop、byteBuf...`  
2. Bootstrap和ServerBootstrap是针对于Client和Server端定义的引导类，主要用于配置各种参数，并启动整个Netty服务。  
3. `EventLoop线程模型`  
    1. **EventLoop定义了Netty的核心抽象，用于处理连接（一个channel）的生命周期中所发生的事件。<font color = "clime">EventLoop的主要作用实际就是负责监听网络事件并调用事件处理器进行相关I/O操作的处理。</font>**  
    2. **<font color = "red">Channel与EventLoop：</font>**  
    &emsp; 当一个连接到达时，Netty就会创建一个Channel，然后从EventLoopGroup中分配一个EventLoop来给这个Channel绑定上，在该Channel的整个生命周期中都是由这个绑定的EventLoop来服务的。  
    3. **<font color = "red">EventLoopGroup与EventLoop：</font>**  
    &emsp; EventLoopGroup是EventLoop的集合，一个EventLoopGroup包含一个或者多个EventLoop。可以将EventLoop看做EventLoopGroup线程池中的一个工作线程。  
4. Channel  
    1. **在Netty中，Channel是一个Socket连接的抽象，它为用户提供了关于底层Socket状态（是否是连接还是断开）以及对Socket的读写等操作。**  
    2. ChannelHandler  
    &emsp; **ChannelHandler主要用来处理各种事件，这里的事件很广泛，比如可以是连接、数据接收、异常、数据转换等。**  
    3. ChannelPipeline  
    &emsp; Netty的ChannelHandler为处理器提供了基本的抽象，目前可以认为每个ChannelHandler的实例都类似于一种为了响应特定事件而被执行的回调。从应用程序开发人员的角度来看，它充当了所有处理入站和出站数据的应用程序逻辑的拦截载体。ChannelPipeline提供了ChannelHandler链的容器，并定义了用于在该链上传播入站和出站事件流的API。当Channel被创建时，它会被自动地分配到它专属的ChannelPipeline。  
    4. ChannelHandlerContext  
    &emsp; 当ChannelHandler被添加到ChannelPipeline时，它将会被分配一个ChannelHandlerContext， **<font color = "clime">它代表了ChannelHandler和ChannelPipeline之间的绑定。</font>** ChannelHandlerContext的主要功能是管理它所关联的ChannelHandler和在同一个ChannelPipeline中的其他ChannelHandler之间的交互。  

5. 小结：  
    1. Netty基于Reactor，parentGroup用于处理连接，childGroup用于处理数据读写。  
    2. 当一个连接到达时，Netty就会创建一个Channel，然后从EventLoopGroup中分配一个EventLoop来给这个Channel绑定上，在该Channel的整个生命周期中都是由这个绑定的EventLoop来服务的。  
    3. 职责链ChannelPipeline，负责事件在职责链中的有序传播，同时负责动态地编排职责链。职责链可以选择监听和处理自己关心的事件，它可以拦截处理和向后/向前传播事件。 ChannelHandlerContext代表了ChannelHandler和ChannelPipeline之间的绑定。  


#### 1.14.6.4. Netty逻辑架构
&emsp; **<font color = "red">Netty采用了典型的三层网络架构进行设计和开发，逻辑架构如下图所示：</font>**  
![image](http://182.92.69.8:8081/img/microService/netty/netty-134.png)  
1. 业务逻辑编排层 Service ChannelHandler：  
&emsp; 业务逻辑编排层通常有两类，一类是纯粹的业务逻辑编排，一类是应用层协议插件，用于特定协议相关的会话和链路管理。由于应用层协议栈往往是开发一次到处运行，并且变动较小，故而将应用协议到 POJO 的转变和上层业务放到不同的 ChannelHandler 中，就可以实现协议层和业务逻辑层的隔离，实现架构层面的分层隔离。  
2. 职责链 ChannelPipeline：  
&emsp; 它负责事件在职责链中的有序传播，同时负责动态地编排职责链。职责链可以选择监听和处理自己关心的事件，它可以拦截处理和向后/向前传播事件。不同应用的Handler用于消息的编解码，它可以将外部的协议消息转换成内部的POJO对象，这样上层业务则只需要关心处理业务逻辑即可，不需要感知底层的协议差异和线程模型差异，实现了架构层面的分层隔离。  
3. 通信调度层 Reactor：  
&emsp; 由一系列辅助类组成，包括 Reactor 线程 NioEventLoop 及其父类，NioSocketChannel 和 NioServerSocketChannel 等等。该层的职责就是监听网络的读写和连接操作，负责将网络层的数据读到内存缓冲区，然后触发各自网络事件，例如连接创建、连接激活、读事件、写事件等。将这些事件触发到 pipeline 中，由 pipeline 管理的职责链来进行后续的处理。  

&emsp; 架构的不同层面，需要关心和处理的对象都不同，通常情况下，对于业务开发者，只需要关心职责链的拦截和业务Handler的编排。因为应用层协议栈往往是开发一次，到处运行，所以实际上对于业务开发者来说，只需要关心服务层的业务逻辑开发即可。各种应用协议以插件的形式提供，只有协议开发人员需要关注协议插件，对于其他业务开发人员来说，只需关心业务逻辑定制。这种分层的架构设计理念实现了NIO框架各层之间的解耦，便于上层业务协议栈的开发和业务逻辑的定制。  

#### 1.14.6.5. Netty高性能
&emsp; Netty高性能：    
1. 内存池设计：申请的内存可以重用，主要指直接内存。内部实现是用一颗二叉查找树管理内存分配情况。  
1. 网络I/O  
    * IO 线程模型：异步非阻塞通信。  
    * [高效的Reactor线程模型](/docs/microService/communication/Netty/Reactor.md) 
    * [零拷贝](/docs/microService/communication/Netty/nettyZeroCopy.md)  
    * 支持 protobuf 等高性能序列化协议。
3. 串形化处理读写：避免使用锁带来的性能开销。以及高效的并发编程。  

##### 1.14.6.5.1. Netty的Reactor线程模型
1. Netty的线程模型并不是一成不变的，它实际取决于用户的启动参数配置。<font color = "red">通过设置不同的启动参数，Netty可以同时支持Reactor单线程模型、多线程模型和主从Reactor多线层模型。</font><font color = "clime">Netty主要靠NioEventLoopGroup线程池来实现具体的线程模型。</font>  
2. Netty主从Reactor多线程模型，内部实现了两个线程池，boss线程池和work线程池，其中boss线程池的线程负责处理请求的accept事件，当接收到accept事件的请求时，把对应的socket封装到一个NioSocketChannel中，并交给work线程池，其中work线程池负责请求的read和write事件，由对应的Handler处理。

#### 1.14.6.6. Netty开发
##### 1.14.6.6.1. Netty应用场景，  
&emsp; Netty主要用来做网络通信：   

* 作为 RPC 框架的网络通信工具：在分布式系统中，不同服务节点之间经常需要相互调用，这个时候就需要 RPC 框架了。不同服务节点之间的通信是如何做的呢？可以使用 Netty 来做。  
* 实现一个自己的 HTTP 服务器：通过 Netty 可以实现一个简单的 HTTP 服务器。  
* 实现一个即时通讯系统：使用 Netty 可以实现一个可以聊天类似微信的即时通讯系统，这方面的开源项目还蛮多的，可以自行去 Github 找一找。  
* 实现消息推送系统：市面上有很多消息推送系统都是基于 Netty 来做的。  
* ...  

##### 1.14.6.6.2. TCP粘拆包与Netty编解码  
1. [TCP的粘包和拆包问题描述](/docs/network/TCPSticking.md)  
2. **<font color = "clime">Netty对半包或者粘包的处理：</font>** **每个Handler都是和Channel唯一绑定的，一个Handler只对应一个Channel，<font color = "red">所以Channel中的数据读取的时候经过解析，如果不是一个完整的数据包，则解析失败，将这个数据包进行保存，等下次解析时再和这个数据包进行组装解析，直到解析到完整的数据包，才会将数据包向下传递。</font>** 
3. Netty默认提供了多种解码器来解决，可以进行分包操作。  
    * 固定长度的拆包器 FixedLengthFrameDecoder
    * 行拆包器 LineBasedFrameDecoder
    * 分隔符拆包器 DelimiterBasedFrameDecoder
    * 基于数据包长度的拆包器 LengthFieldBasedFrameDecoder  

##### 1.14.6.6.3. Netty实战
&emsp; ...  

##### 1.14.6.6.4. Netty多协议开发

* Http协议开发应用
* WebSocket协议开发  
&emsp; WebSocket是基于TCP的应用层协议，用于在C/S架构的应用中实现双向通信，关于WebSocket协议的详细规范和定义参见rfc6455。  
* 私有协议栈开发  

#### 1.14.6.7. Netty源码


## 1.15. WebSocket
### 1.15.1. 4种Web端即时通信
1. `Web端即时通讯技术：服务器端可以即时地将数据的更新或变化反应到客户端，例如消息即时推送等功能都是通过这种技术实现的。`但是在Web中，由于浏览器的限制，实现即时通讯需要借助一些方法。这种限制出现的主要原因是，一般的Web通信都是浏览器先发送请求到服务器，服务器再进行响应完成数据的显示更新。  
&emsp; 实现Web端即时通讯的方法：`实现即时通讯主要有四种方式，它们分别是轮询、长轮询(comet)、长连接(SSE)、WebSocket。` **<font color = "blue">它们大体可以分为两类，一种是在HTTP基础上实现的，包括短轮询、comet和SSE；另一种不是在HTTP基础上实现，即WebSocket。</font>**    
2. 短轮询：平时写的忙轮询/无差别轮询。  
3. 长轮询：  
	&emsp; 客户端发送请求后服务器端不会立即返回数据， **<font color = "red">服务器端会阻塞请求连接不会立即断开，`直到服务器端有数据更新或者是连接超时才返回`，</font>** `客户端才再次发出请求新建连接、如此反复从而获取最新数据。`  
	* 优点：长轮询和短轮询比起来，明显减少了很多不必要的http请求次数，相比之下节约了资源。  
	* 缺点：`连接挂起也会导致资源的浪费。`  
4. 长连接（SSE）  
&emsp; SSE是HTML5新增的功能，全称为Server-Sent Events。它可以`允许服务推送数据到客户端`。 **<font color = "clime">SSE在本质上就与之前的长轮询、短轮询不同，虽然都是基于http协议的，但是轮询需要客户端先发送请求。</font>** 而SSE`最大的特点就是不需要客户端发送请求`，`可以实现只要服务器端数据有更新，就可以马上发送到客户端`。  
&emsp; SSE的优势很明显，`它不需要建立或保持大量的客户端发往服务器端的请求，`节约了很多资源，提升应用性能。并且后面会介绍道，SSE的实现非常简单，并且不需要依赖其他插件。  
5. WebSocket  
&emsp; WebSocket是Html5定义的一个新协议，与传统的http协议不同，该协议可以实现服务器与客户端之间全双工通信。简单来说，首先需要在客户端和服务器端建立起一个连接，这部分需要http。连接一旦建立，客户端和服务器端就处于平等的地位，可以相互发送数据，不存在请求和响应的区别。  
&emsp; WebSocket的优点是实现了双向通信，缺点是服务器端的逻辑非常复杂。现在针对不同的后台语言有不同的插件可以使用。  

### 1.15.2. 配置中心使用长轮询推送
1. push、pull、长轮询  
    &emsp; push模型的好处是实时写新的数据到客户端。pull模型的好处是请求/响应模式，完成之后就断开，而不是像push模型一样，一直长连接不断开，如果每个连接都不断开，那么服务器连接数量很快会被耗尽。  
    &emsp; **<font color = "red">长轮询（Long Polling）和轮询（Polling）的区别，两者都是拉模式的实现。</font>**  
2. 配置中心使用长轮询推送  
    &emsp; 客户端发起长轮询，如果服务端的数据没有发生变更，会 hold 住请求，直到服务端的数据发生变化，或者等待一定时间超时才会返回。返回后，客户端又会立即再次发起下一次长轮询。配置中心使用「长轮询」如何解决「轮询」遇到的问题也就显而易见了：
    * 推送延迟。服务端数据发生变更后，长轮询结束，立刻返回响应给客户端。
    * 服务端压力。长轮询的间隔期一般很长，例如 30s、60s，并且服务端 hold 住连接不会消耗太多服务端资源。

### 1.15.3. WebSocket
1. WebSocket是应用层协议  
&emsp; WebSocket是基于TCP的应用层协议，用于在C/S架构的应用中实现双向通信。虽然WebSocket协议在建立连接时会使用HTTP协议，但这并不意味着WebSocket协议是基于HTTP协议实现的。  
2. WebSocket与Http的区别  
&emsp; 通信方式不同：WebSocket是双向通信模式，客户端与服务器之间只有在握手阶段是使用HTTP协议的“请求-响应”模式交互，而一旦连接建立之后的通信则使用双向模式交互，不论是客户端还是服务端都可以随时将数据发送给对方；而HTTP协议则至始至终都采用“请求-响应”模式进行通信。也正因为如此，HTTP协议的通信效率没有WebSocket高。  


## 1.16. 磁盘IO
&emsp; 顺序读写磁盘  

## 1.17. 内存优化  
&emsp; 虚拟内存  



------------------

## 1.18. 计算机网络
### 1.18.1. OSI七层网络模型
![image](http://182.92.69.8:8081/img/network/osi-2.png)  

### 1.18.2. 应用层
#### 1.18.2.1. DNS
1. **<font color = "clime">DNS解析流程：</font>** ...  
2. DNS将域名解析成ip，网络通信也即 ip:端口 间的通信。  

#### 1.18.2.2. HTTP
&emsp; 在浏览器地址栏键入URL，按下回车之后经历的流程：URL解析 ---> DNS解析 ---> TCP连接 ---> 发送HTTP请求 ---> 服务器处理请求并返回HTTP报文 ---> 浏览器解析渲染页面 ---> 连接结束。  

#### 1.18.2.3. HTTPS
1. **<font color = "clime">HTTPS的整体过程分为证书验证、协商密钥、数据传输阶段。</font>**  
    ![image](http://182.92.69.8:8081/img/network/https-5.png)  
    1. 证书验证阶段，一次交互，客户端发送请求，获取到服务端的证书，并进行校验。若不合法，进行警告。  
    2. 协商密钥阶段，采取非对称加密。`客户端用公钥对随机数进行加密和传输`；`服务端用私钥解密随机数`，获取到随机数，完成连接。 **<font color = "clime">~~注：非对称加密的目的就是让服务端得到这个随机值，以后客户端和服务端的通信就可以通过这个随机值来进行加密解密。~~</font>**  
    3. 完成连接后，采用对称加密进行数据传输。  
2. 本地随机数会被窃取嘛？  
&emsp; HTTPS并不包含对随机数的安全保证，HTTPS保证的只是传输过程安全，<font color = "red">而随机数存储于本地，本地的安全属于另一安全范畴。</font>  

### 1.18.3. 传输层
#### 1.18.3.1. TCP
##### 1.18.3.1.1. 连接建立阶段
###### 1.18.3.1.1.1. 连接建立
1. 连接建立
    1. 三次握手  
        ![image](http://182.92.69.8:8081/img/network/TCP-1.png)  
        1. 为什么只有三次握手才能确认双方的接受与发送能力是否正常，而两次却不可以？  
        &emsp; <font color = "clime">第三次握手中，客户端向服务器发送确认包ACK，`防止了服务器端的一直等待而浪费资源`。</font>例如：已失效的连接请求报文突然又传送到了服务器，从而会产生错误。  
    2. 四次挥手  
        ![image](http://182.92.69.8:8081/img/network/TCP-2.png)  
        1. **<font color = "clime">Client收到服务端F1N后，Client进入TIME_WAIT状态。</font>** 2MSL后自动关闭。 
        2. 为什么客户端最后还要等待2MSL？  
        &emsp; <font color = "red">保证客户端发送的最后一个`ACK报文`能够到达服务器，因为这个ACK报文可能丢失。</font>  
        &emsp; `站在服务器的角度看来，服务端已经发送了FIN+ACK报文请求断开了，客户端还没有给我回应，应该是服务器发送的请求断开报文它没有收到，于是服务器又会重新发送一次。而客户端就能在这个2MSL时间段内收到这个重传的报文，接着给出回应报文，并且会重启2MSL计时器。`  
    &emsp;如果客户端收到服务端的FIN+ACK报文后，发送一个ACK给服务端之后就“自私”地立马进入CLOSED状态，可能会导致服务端无法确认收到最后的ACK指令，也就无法进入CLOSED状态，这是客户端不负责任的表现。  
    3. **<font color = "blue">`如果已经建立了连接，但是客户端突然出现故障了怎么办？`</font>**   
    &emsp; 客户端如果出现故障，服务器不能一直等下去，白白浪费资源。`在TCP设有一个保活计时器。`<font color = "clime">服务器每收到一次客户端的请求后都会重新复位这个计时器，时间通常是设置为2小时，若两小时还没有收到客户端的任何数据，服务器就会发送一个探测报文段，以后每隔75分钟发送一次。若一连发送10个探测报文仍然没反应，服务器就认为客户端出了故障，接着就关闭连接。</font>  
2. TIME_WAIT问题
    1. TIME_WAIT状态，该socket所占用的本地端口号将一直无法释放。TIME_WAIT过多，可能出现做为客户端的程序无法向服务端建立新的socket连接的情况。  
    2. **大量的TIME_WAIT状态TCP连接存在，是因为大量的短连接存在。TIME_WAIT状态时socket还占用端口。** TIME_WAIT状态默认为2MSL。    
    3. 解决办法：
        1. 客户端  
        &emsp; **HTTP请求的头部，connection 设置为 keep-alive，** 保持存活一段时间：现在的浏览器，一般都这么进行了。     
        2. 服务器端  
            * **<font color = "red">允许time_wait状态的socket被重用。</font>**
            * 缩减time_wait时间，设置为 1 MSL（即2mins）。


###### 1.18.3.1.1.2. ★★★Http长短链接
1. TCP长连接  
	1. **<font color = "red">长连接，指在一个连接上可以连续发送多个数据包，在连接保持期间，如果没有数据包发送，需要双方发链路检测包。</font>**   
	&emsp; `长连接没有条件能够判断读写什么时候结束，所以必须要加报文头的长度。`读函数先是读取报文头的长度，再根据这个长度去读相应长度的报文。  
	2. 流程：TCP连接→数据包传输→保持连接(心跳)→数据传输→保持连接(心跳)→……→关闭连接（一个TCP连接通道有多个读写通信）。  
	![image](http://182.92.69.8:8081/img/network/TCP-4.png)   
	3. 优点：  
	&emsp; 可以省去较多的TCP建立和关闭的操作，减少浪费，节约时间。 
	4. 缺点：
	    * **<font color = "red">服务器端有很多TCP连接时，会降低服务器的性能。</font>**  
	    * 需要管理所有的TCP连接，`检测是否是无用的连接（长时间没有读写事件），并进行关闭等操作`。  
	&emsp; 解决方案：如关闭一些长时间没有读写事件发生的连接，这样可以避免一些恶意连接导致server端服务受损；如果条件再允许就可以以客户端机器为粒度，限制每个客户。
2. TCP短链接 
	1. 短连接是指通信双方有数据交互时，就建立一个TCP连接，数据发送完成后，则断开此TCP连接（管理起来比较简单，存在的连接都是有用的连接，不需要额外的控制手段）。  
	2. 流程：连接→数据传输→关闭连接→连接→数据传输→关闭连接。  
	![image](http://182.92.69.8:8081/img/network/TCP-5.png)   
	3. 优点：对于服务器来说管理较为简单，存在的连接都是有用的连接，不需要额外的控制手段。  
	4. 缺点：但如果客户请求频繁，将在TCP的建立和关闭操作上浪费较多时间和带宽。  
3. TCP长短连接使用场景：  
    1. **<font color = "red">长连接多用于操作频繁，点对点的通讯，而且连接数不能太多情况。</font>** 每个TCP连接都需要三次握手，这需要时间，如果每个操作都是短连接，再操作的话那么处理速度会降低很多，所以每个操作完后都不断开，下次处理时直接发送数据包就OK了，不用建立TCP连接。例如：数据库的连接用长连接，如果用短连接频繁的通信会造成socket错误，而且频繁的socket 创建也是对资源的浪费。  
    2. 像WEB网站的http服务一般都用短链接，因为长连接对于服务端来说会耗费一定的资源，像WEB网站这么频繁的成千上万甚至上亿客户端的连接用短连接会更省一些资源，如果用长连接，而且同时有成千上万的用户，如果每个用户都占用一个连接的话，那可想而知吧。所以并发量大，但每个用户无需频繁操作情况下需用短连好。  
4. HTTP长短连接  
&emsp; HTTP的长连接和短连接本质上是TCP长连接和短连接。  

###### 1.18.3.1.1.3. TCP粘包
1. **TCP是基于流传输的协议，请求数据在其传输的过程中是没有界限区分，所以在读取请求的时候，不一定能获取到一个完整的数据包。**  
2. **<font color = "red">TCP粘包/拆包常见解决方案：</font>** 
    * 每个包都固定长度。
    * 每个包的末尾使用固定的分隔符。
    * 将消息分为头部和消息体，在头部中保存有当前整个消息的长度。
    * 通过自定义协议进行粘包和拆包的处理。   

##### 1.18.3.1.2. 数据传输阶段

### 1.18.4. 网络的性能指标
&emsp; **<font color = "red">通常是以4个指标来衡量网络的性能，分别是`带宽、延时、吞吐率、PPS(Packet Per Second)`。</font>**  

## 1.19. 负载均衡
&emsp; 要想理解负载均衡，首先需要清楚OSI七层网络模型。  

### 1.19.1. 负载均衡解决方案
&emsp; **<font color = "clime">★★★负载均衡方案选择：</font>**  
* 小于3000万pv的，DNS轮询+监控；  
* **3000万以上的，nginx+监控；**  
&emsp; 5000万PV的，HAProxy+Keepalived,nginx，HAPROXY负责TCP的负载均衡，nginx负责7层调度；  
* **1亿以上的，LVS-DR+keepalived,nginx，LVS-DR负责TCP的负载均衡，nginx负责7层调度。**  

### 1.19.2. Nginx
#### 1.19.2.1. Nginx介绍
1. Nginx是一个高性能的Web服务器。同时处理大量的并发请求（可以处理2-3万并发连接数，官方监测能支持5万并发）。   
&emsp; <font color = "red">Nginx工作在4层或7层。</font> 
2. 多进程 
    1. Nginx启动时，会生成两种类型的进程，一个主进程master，一个（windows版本的目前只有一个）或多个工作进程worker。  
        * 主进程并不处理网络请求，主要负责调度工作进程：加载配置、启动工作进程、非停升级。  
        * **<font color = "red">`一般推荐worker进程数与CPU内核数一致，这样一来不存在大量的子进程生成和管理任务，避免了进程之间竞争CPU资源和进程切换的开销。`</font>**  
    2. <font color = "red">多进程的优缺点：</font>  
        1. 使用进程的好处是各个进程之间相互独立，不需要加锁，减少了使用锁对性能造成影响，同时降低编程的复杂度，降低开发成本。  
        2. 采用独立的进程，`可以让进程互相之间不会影响`，如果一个进程发生异常退出时，其它进程正常工作，master进程则很快启动新的worker进程，确保服务不会中断，从而将风险降到最低。     
        3. 缺点是操作系统生成一个子进程需要进行内存复制等操作，在资源和时间上会产生一定的开销。当有大量请求 时，会导致系统性能下降。   
3. 多路复用，Reactor

#### 1.19.2.2. Nginx使用
1. <font color = "red">Nginx服务器处理一个请求是按照两部分进行的。第一部分是IP和域名，由listen和server_name指令匹配server模块；第二部分是URL，匹配server模块里的location；最后就是location里的具体处理。</font>  
2. <font color = "red">Nginx使用场景：反向代理、虚拟主机、静态资源WEB服务、缓存、限流、黑白名单、防盗链、流量复制...</font>  
3. 负载均衡：  
    1. **<font color = "red">Nginx反向代理通过proxy_pass来配置；负载均衡使用Upstream模块实现。</font>**  
    2. **<font color = "red">Nginx支持的负载均衡调度算法方式如下：</font>**  
        * **<font color = "red">轮询（默认）</font>** 
        * **<font color = "red">weight：</font>** 指定权重。  
        * **<font color = "red">ip_hash</font>**  
        * **<font color = "red">url_hash（第三方）</font>**  
        * **<font color = "red">fair（第三方）：</font>** 智能调整调度算法，动态的根据后端服务器的请求处理到响应的时间进行均衡分配。  

## 1.20. Devops
### 1.20.1. CI/CD
&emsp; `CI/CD是两个独立过程的组合：持续集成和持续部署。`  
1. Continuous Integration（持续集成）  
&emsp; 持续集成（CI）是构建软件和完成初始测试的过程。  
2. Continuous Delivery（持续交付）  
3. Continuous Deployment（持续部署）  
&emsp; 持续部署（CD）是将代码与基础设施相结合的过程，确保完成所有测试并遵循策略，然后将代码部署到预期环境中。  

### 1.20.2. DevOps
1. DevOps框架  
&emsp; 以下是一个DevOps框架。这个框架只指出那些被认可的概念和它们在某种程度上的关系。
![image](http://182.92.69.8:8081/img/devops/devops/devops-8.png)  
&emsp; **<font color = "clime">敏捷开发指的是在 DevOps 中采用敏捷思想进行软件开发，敏捷宣言无疑是很重要的一项。有多种敏捷方法可以采用，比如Scrum、看板和极限编程。</font>**  
&emsp; **<font color = "clime">持续集成提供了让多个程序员可以同时运行应用程序的最佳实践，可以频繁合并源代码、验证代码（静态测试用例）、编译和测试代码（动态测试用例）。</font>**  
&emsp; **<font color = "clime">持续交忖关注从开发、测试、验收到生产环境的高频生产能力。基于高度的自动化，极端的发布上线时间可以达到分钟级。</font>**  
2. DevOps流程  
&emsp; 下图显示了一个DevOps流程。它不是DevOps流程的正式定义，而是表述了在大多数组织机构中，为了实现一个服务而会被循环执行的合乎逻辑顺序的一系列阶段。  
&emsp; 深色部分表示开发流程，浅色部分表示运维流程。这两个流程构成了DevOps方法的核心。  
![image](http://182.92.69.8:8081/img/devops/devops/devops-1.png)  
3. 工具集  
&emsp; **<font color = "clime">DevOps一般包括版本控制&协作开发工具、自动化构建和测试工具、持续集成&交付工具、部署工具、维护工具、监控，警告&分析工具等。</font>**  
![image](http://182.92.69.8:8081/img/devops/devops/devops-3.png)  

### 1.20.3. 从上往下学Docker

#### 1.20.3.1. Docker使用教程
1. **<font color = "clime">镜像操作常用命令：pull(获取)、images(查看本地镜像)、inspect(查看镜像详细信息)、rmi(删除镜像)、commit(构建镜像)。</font>**  
2. **<font color = "clime">容器操作常用命令：run(创建并启动)、start(启动已有)、stop、exec(进入运行的容器)。</font>**  
3. **<font color = "clime">Dockerfile中包含：</font>** （# 为 Dockerfile中的注释）  
    * 基础镜像(FROM)    
    * 镜像元信息   
    * **<font color = "clime">镜像操作指令</font>** （RUN、COPY、ADD、EXPOSE、WORKDIR、ONBUILD、USER、VOLUME等）    
        * RUN命令：**run是在docker build构建镜像时，会执行的命令，** 比如安装一些软件、配置一些基础环境。  
    * **<font color = "clime">容器启动时执行指令</font>** （CMD、ENTRYPOINT）  
        * CMD命令： **cmd是在docker run启动容器时，会执行的命令，为启动的容器指定默认要运行的程序。** CMD指令指定的程序可被docker run命令行参数中指定要运行的程序所覆盖。 **<font color = "clime">注意：如果Dockerfile中如果存在多个CMD指令，仅最后一个生效。</font>**    
    ![image](http://182.92.69.8:8081/img/devops/docker/docker-9.png)  


#### 1.20.3.2. 镜像详解
1. Docker中镜像是分层的，最顶层是读写层（镜像与容器的区别），其底部依赖于Linux的UnionFS文件系统。  
2. **<font color = "red">利用联合文件系统UnionFS写时复制的特点，在启动一个容器时，Docker引擎实际上只是增加了一个可写层和构造了一个Linux容器。</font>**  

#### 1.20.3.3. 容器详解
1. 单个宿主机的多个容器是隔离的，其依赖于Linux的Namespaces、CGroups。  
2. 隔离的容器需要通信、文件共享（数据持久化）。  

### 1.20.4. Kubernetes
#### 1.20.4.1. k8s架构
1. 1). 一个容器或多个容器可以同属于一个Pod之中。 2). Pod是由Pod控制器进行管理控制，其代表性的Pod控制器有Deployment、StatefulSet等。 3). Pod组成的应用是通过Service或Ingress提供外部访问。  
2. **<font color = "red">每一个Kubernetes集群都由一组Master节点和一系列的Worker节点组成。</font>**  
    1. **<font color = "clime">Master的组件包括：API Server、controller-manager、scheduler和etcd等几个组件。</font>**  
        * **<font color = "red">API Server：K8S对外的唯一接口，提供HTTP/HTTPS RESTful API，即kubernetes API。</font>**  
        * **<font color = "red">controller-manager：负责管理集群各种资源，保证资源处于预期的状态。</font>** 
        * **<font color = "red">scheduler：资源调度，负责决定将Pod放到哪个Node上运行。</font>** 
    2. **<font color = "clime">Node节点主要由kubelet、kube-proxy、docker引擎等组件组成。</font>**  


## 1.21. 数据结构和算法  


