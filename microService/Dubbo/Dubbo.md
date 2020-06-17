---
title: Dubbo
date: 2020-05-19 00:00:00
tags:
    - Dubbo
---

<!-- TOC -->

- [1. Dubbo工作流程：](#1-dubbo工作流程)
    - [1.1. Dubbo需要Web容器吗？内置了哪几种服务容器？](#11-dubbo需要web容器吗内置了哪几种服务容器)
    - [1.2. Dubbo有些哪些注册中心？](#12-dubbo有些哪些注册中心)
    - [1.3. Dubbo支持哪些序列化方式？](#13-dubbo支持哪些序列化方式)
    - [1.4. 通信协议](#14-通信协议)
    - [1.5. 服务提供者能实现失效踢出是什么原理？](#15-服务提供者能实现失效踢出是什么原理)
    - [1.6. Dubbo服务之间的调用是阻塞的吗？](#16-dubbo服务之间的调用是阻塞的吗)
    - [1.7. ※※※负载均衡](#17-※※※负载均衡)
    - [1.8. ※※※集群容错策略](#18-※※※集群容错策略)
    - [1.9. 服务降级](#19-服务降级)
- [2. Dubbo和Spring Cloud](#2-dubbo和spring-cloud)
- [3. Dubbo中的SPI](#3-dubbo中的spi)
- [4. Dubbo对Spring的扩展](#4-dubbo对spring的扩展)

<!-- /TOC -->


![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo.png)   

# 1. Dubbo工作流程： 

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-11.png)   
&emsp; Dubbo中5个角色：Provider、Consumer、Registry、Monitor、Container。  

* Provider暴露服务方称之为“服务提供者”。服务提供者向注册中心注册其提供的服务，并汇报调用时间到监控中心，此时间不包含网络开销。  
* Consumer调用远程服务方称之为“服务消费者”。服务消费者向注册中心获取服务提供者地址列表，并根据负载算法直接调用提供者，同时汇报调用时间到监控中心，此时间包含网络开销。  
* Registry服务注册与发现的中心目录服务称之为“服务注册中心”。注册中心负责服务地址的注册与查找，相当于目录服务。服务提供者和消费者只在启动时与注册中心交互，注册中心不转发请求，压力较小。  
* Monitor统计服务的调用次数和调用时间的日志服务称之为“服务监控中心”。监控中心负责统计各服务调用次数，调用时间等，统计先在内存汇总后每分钟一次发送到监控中心服务器，并以报表展示。  
* Container服务运行容器。 

&emsp; ***调用关系：***  
0. (start)服务容器Container负责启动，加载，运行服务提供者。  
1. (register)服务提供者Provider在启动时，向注册中心注册服务。  
2. (subscribe)服务消费者Consumer在启动时，向注册中心订阅服务。  
3. <font color = "red">(notify)注册中心Registry返回服务提供者地址列表给消费者，如果有变更，注册中心将基于长连接推送变更数据给消费者。</font>  
4. <font color = "red">(invoke)服务消费者Consumer，从提供者地址列表中，基于软负载均衡算法，选一台提供者进行调用，如果调用失败，再选另一台调用。</font>  
5. (count)服务消费者Consumer和提供者Provider，在内存中累计调用次数和调用时间，定时每分钟发送一次统计数据到监控中心。  

## 1.1. Dubbo需要Web容器吗？内置了哪几种服务容器？  
&emsp; 不需要，如果强制使用Web容器，只会增加复杂性，也浪费资源。  
&emsp; 内置了Spring Container、Jetty Container、Log4j Container。   
&emsp; Dubbo 的服务容器只是一个简单的 Main 方法，并加载一个简单的 Spring 容器，用于暴露服务。  

## 1.2. Dubbo有些哪些注册中心？  

* Multicast注册中心：Multicast注册中心不需要任何中心节点，只要广播地址，就能进行服务注册和发现。基于网络中组播传输实现；  
* Zookeeper注册中心：基于分布式协调系统Zookeeper实现，采用Zookeeper的watch机制实现数据变更；  
*  redis注册中心：基于redis实现，采用key/Map存储，住key存储服务名和类型，Map中key存储服务URL，value服务过期时间。基于redis的发布/订阅模式通知数据变更；  
* Simple注册中心。  

## 1.3. Dubbo支持哪些序列化方式？  
&emsp; 默认使用Hessian序列化，还有Duddo、FastJson、Java自带序列化。   

## 1.4. 通信协议  
&emsp; 不同服务在性能上适用不同协议进行传输，比如大数据用短连接协议，小数据大并发用长连接协议。  

|协议名称	|实现描述	|连接	|适用范围	|使用场景|
|---|---|---|---|---|
|dubbo	|传输：mina、netty、grizzy <br/>序列化：dubbo、hessian2、java、json|dubbo缺省，采用单一长连接和NIO异步通讯，传输协议TCP|1.传入传出参数数据包较小<br/>2.消费者比提供者多<br/>3.常规远程服务方法调用<br/>4.不适合传送大数据量的服务，比如文件、传视频|	常规远程服务方法调用|
|rmi	|传输：java  rmi<br/>序列化：java 标准序列化（实现ser接口）	|1.连接个数：多连接<br/>2.连接方式：短连接<br/>3.传输协议：TCP/IP<br/>4.传输方式：BIO	|1.常规RPC调用<br/>2.与原RMI客户端互操作<br/>3.可传文件<br/>4.不支持防火墙穿|	常规远程服务方法调用，与原生RMI服务互操作|
|hessian	|传输：Serverlet容器<br/>序列化：hessian二进制序列化|1.连接个数：多连接<br/>2.连接方式：短连接<br/>3.传输协议：HTTP<br/>4.传输方式：同步传输|	1.提供者比消费者多<br/>2.可传文件<br/>3.跨语言传输|	需同时给应用程序和浏览器JS使用的服务。|
|http	|传输：Servlet容器<br/>序列化：表单序列化|	1.连接个数：多连接<br/>2.连接方式：短连接<br/>3.传输协议：HTTP<br/>4.传输方式：同步传输|	1.提供者多余消费者<br/>2.数据包混合	|需同时给应用程序和浏览器JS使用的服务。|
|webservice	|传输：HTTP<br/>序列化：SOAP文件序列化	|1.连接个数：多连接<br/>2.连接方式：短连接<br/>3.传输协议：HTTP<br/>4.传输方式：同步传输|	1.系统集成<br/>2.跨语言调用	|系统集成，跨语言调用|
|thrift	|与thrift RPC实现集成，并在基础上修改了报文头 |长连接、NIO异步传输 |||	
|Redis|||||  	

## 1.5. 服务提供者能实现失效踢出是什么原理？  
&emsp; 服务失效踢出基于zookeeper的临时节点原理。  

## 1.6. Dubbo服务之间的调用是阻塞的吗？  
&emsp; 默认是同步等待结果阻塞的，支持异步调用。Dubbo的异步调用是基于NIO的非阻塞实现并行调用，客户端不需要启动多线程即可完成并行调用多个远程服务，相对多线程开销较小，异步调用会返回一个 Future 对象。  
&emsp; 异步调用流程图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-12.png)   

## 1.7. ※※※负载均衡  
* Random（缺省）  
&emsp; 随机，按权重设置随机概率。  
&emsp; 在一个截面上碰撞的概率高，但调用量越大分布越均匀，而且按概率使用权重后也比较均匀，有利于动态调整提供者权重。  
* RoundRobin  
&emsp; 轮循，按公约后的权重设置轮循比率。  
&emsp; 轮询负载均衡算法的不足：存在慢的提供者累积请求的问题，比如：第二台机器很慢，但没挂，当请求调到第二台时就卡在那，久而久之，所有请求都卡在调到第二台上。  
* LeastActive  
&emsp; 最少活跃调用数，相同活跃数的随机，活跃数指调用前后计数差。使慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大。  
* ConsistentHash  
&emsp; 分布式一致性 Hash算法。相同参数的请求总是发到同一提供者；当某一台提供者崩溃时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动。  

    * 缺省只对第一个参数Hash，如果要修改，请配置<dubbo:parameter key="hash.arguments" value="0,1" /\>  
    * 缺省用160份虚拟节点，如果要修改，请配置<dubbo:parameter key="hash.nodes" value="320" /\>  

## 1.8. ※※※集群容错策略  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-13.png)   

&emsp; 在集群调用失败时，Dubbo 提供了多种容错方案，缺省为 failover 重试。下面列举dubbo支持的容错策略：  

* Failover（默认） - 失败自动切换，当出现失败，重试其它服务器。通常用于读操作，但重试会带来更长延迟。可通过 retries="2" 来设置重试次数(不含第一次)  
* Failfast - 快速失败，只发起一次调用，失败立即报错。通常用于非幂等性的写操作，比如新增记录。
* Failsafe - 失败安全，出现异常时，直接忽略。通常用于写入审计日志等操作。  
* Failback - 失败自动恢复，后台记录失败请求，定时重发。通常用于消息通知操作。  
* Forking - 并行调用多个服务器，只要一个成功即返回。通常用于实时性要求较高的读操作，但需要浪费更多服务资源。可通过 forks="2" 来设置最大并行数。  
* Broadcast - 广播调用所有提供者，逐个调用，任意一台报错则报错。通常用于通知所有提供者更新缓存或日志等本地资源信息。  

## 1.9. 服务降级  
&emsp; 当服务器压力过大时，可以通过服务降级来使某些非关键服务的调用变得简单；可以对其直接进行屏蔽，即客户端不发送请求直接返回null；也可以正常发送请求当请求超时或不可达时再返回null。
&emsp; 服务降级的相关配置可以直接在dubbo-admin的监控页面进行配置；通常是基于消费者来配置的,在dubbo-admin找到对应的消费者想要降级的服务，点击其后面的屏蔽或容错按钮即可生效；其中,屏蔽按钮点击表示放弃远程调用直接返回空，而容错按钮点击表示继续尝试进行远程调用当调用失败时再返回空。  

# 2. Dubbo和Spring Cloud  
&emsp; Dubbo是SOA时代的产物，它的关注点主要在于服务的调用，流量分发、流量监控和熔断。  
&emsp; Spring Cloud诞生于微服务架构时代，考虑的是微服务治理的方方面面，另外由于依托了 Spirng、Spirng Boot的优势之上。  

* 两个框架在开始目标就不一致：Dubbo 定位服务治理；Spirng Cloud 是一个生态。  
* Dubbo底层是使用Netty这样的NIO框架，是基于TCP协议传输的，配合以Hession序列化完成RPC通信。而SpringCloud是基于Http协议+Rest接口调用远程过程的通信，相对来说，Http请求会有更大的报文，占的带宽也会更多。但是REST相比RPC更为灵活，服务提供方和调用方的依赖只依靠一纸契约，不存在代码级别的强依赖，这在强调快速演化的微服务环境下，显得更为合适，至于注重通信速度还是方便灵活性，具体情况具体考虑。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-14.png)  


# 3. Dubbo中的SPI  
&emsp; SPI，Serviceproviderinterface，服务提供发现接口。  
&emsp; dubbo在JDK的spi基础上主要有以下的改变：
1. 配置文件采用键值对配置的方式，使用起来更加灵活和简单 
2. 增强了原本SPI的功能，使得SPI具备ioc和aop的功能，这在原本的java中spi是不支持的。dubbo的spi是通过ExtensionLoader来解析的，通过ExtensionLoader来加载指定的实现类，配置文件的路径在META-INF/dubbo路径下。  


# 4. Dubbo对Spring的扩展  
......


