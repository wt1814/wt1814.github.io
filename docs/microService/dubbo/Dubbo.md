

<!-- TOC -->

- [1. Dubbo](#1-dubbo)
    - [1.1. Dubbo介绍](#11-dubbo介绍)
        - [1.1.1. Dubbo工作流程](#111-dubbo工作流程)
        - [1.1.2. Dubbo需要Web容器吗？内置了哪几种服务容器？](#112-dubbo需要web容器吗内置了哪几种服务容器)
        - [1.1.3. Dubbo有哪些注册中心？](#113-dubbo有哪些注册中心)
        - [1.1.4. Dubbo支持哪些序列化方式？](#114-dubbo支持哪些序列化方式)
        - [1.1.5. 通信协议](#115-通信协议)
        - [1.1.6. 服务提供者能实现失效踢出是什么原理？](#116-服务提供者能实现失效踢出是什么原理)
        - [1.1.7. Dubbo服务之间的调用是阻塞的吗？](#117-dubbo服务之间的调用是阻塞的吗)
        - [1.1.8. ※※※负载均衡](#118-※※※负载均衡)
        - [1.1.9. ※※※集群容错策略](#119-※※※集群容错策略)
        - [1.1.10. 服务降级](#1110-服务降级)
    - [1.2. Dubbo和Spring Cloud](#12-dubbo和spring-cloud)
    - [1.3. Dubbo生态](#13-dubbo生态)
        - [1.3.1. Dubbo与分布式事务](#131-dubbo与分布式事务)

<!-- /TOC -->


![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo.png)   

# 1. Dubbo  
## 1.1. Dubbo介绍
### 1.1.1. Dubbo工作流程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-11.png)   
&emsp; Dubbo中5个角色：Provider、Consumer、Registry、Monitor、Container。  

* Provider，暴露服务方称之为“服务提供者”。服务提供者向注册中心注册其提供的服务，并汇报调用时间到监控中心，此时间不包含网络开销。  
* Consumer，调用远程服务方称之为“服务消费者”。服务消费者向注册中心获取服务提供者地址列表，并根据负载算法直接调用提供者，同时汇报调用时间到监控中心，此时间包含网络开销。  
* Registry，服务注册与发现的中心目录服务称之为“服务注册中心”。注册中心负责服务地址的注册与查找，相当于目录服务。服务提供者和消费者只在启动时与注册中心交互，注册中心不转发请求，压力较小。  
* Monitor，统计服务的调用次数和调用时间的日志服务称之为“服务监控中心”。监控中心负责统计各服务调用次数，调用时间等，统计先在内存汇总后每分钟一次发送到监控中心服务器，并以报表展示。  
* Container，服务运行容器。 

&emsp; **调用关系：**  
1. (start)服务容器Container负责启动，加载，运行服务提供者。  
2. (register)服务提供者Provider在启动时，向注册中心注册服务。  
3. (subscribe)服务消费者Consumer在启动时，向注册中心订阅服务。  
4. <font color = "red">(notify)注册中心Registry返回服务提供者地址列表给消费者，</font><font color = "lime">如果有变更，注册中心将基于长连接推送变更数据给消费者。</font>  
5. <font color = "red">(invoke)服务消费者Consumer，从提供者地址列表中，基于软负载均衡算法，选一台提供者进行调用，如果调用失败，再选另一台调用。</font>  
6. (count)服务消费者Consumer和提供者Provider，在内存中累计调用次数和调用时间，定时每分钟发送一次统计数据到监控中心。  

&emsp; Dubbo提供了3个关键功能：  

* 基于接口的远程调用  
* 容错和负载均衡  
* 自动服务注册和发现 

-----
&emsp; Dubbo的工作原理：  
1. 服务启动的时候，provider和consumer根据配置信息，连接到注册中心register，分别向注册中心注册和订阅服务。  
2. register根据服务订阅关系，返回provider信息到consumer，同时consumer会把provider信息缓存到本地。如果信息有变更，consumer会收到来自register的推送。  
3. consumer生成代理对象，同时根据负载均衡策略，选择一台provider，同时定时向monitor记录接口的调用次数和时间信息。  
4. 拿到代理对象之后，consumer通过代理对象发起接口调用。  
5. provider收到请求后对数据进行反序列化，然后通过代理调用具体的接口实现。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-52.png)   

&emsp; **<font color = "red">为什么要通过代理对象通信？</font>**    
&emsp; 主要是为了实现接口的透明代理，封装调用细节，让用户可以像调用本地方法一样调用远程方法，同时还可以通过代理实现一些其他的策略，比如：  

1. 调用的负载均衡策略  
2. 调用失败、超时、降级和容错机制  
3. 做一些过滤操作，比如加入缓存、mock数据  
4. 接口调用数据统计  

### 1.1.2. Dubbo需要Web容器吗？内置了哪几种服务容器？  
&emsp; 不需要，如果强制使用Web容器，只会增加复杂性，也浪费资源。  
&emsp; Dubbo内置了Spring Container、Jetty Container、Log4j Container。   
&emsp; Dubbo的服务容器只是一个简单的Main方法，并加载一个简单的Spring容器，用于暴露服务。  

### 1.1.3. Dubbo有哪些注册中心？  

* Multicast注册中心：Multicast注册中心不需要任何中心节点，只要广播地址，就能进行服务注册和发现。基于网络中组播传输实现；  
* Zookeeper注册中心：基于分布式协调系统Zookeeper实现，采用Zookeeper的watch机制实现数据变更；  
* redis注册中心：基于redis实现，采用key/Map存储，key中存储服务名和类型，Map中key存储服务URL，value服务过期时间。基于redis的发布/订阅模式通知数据变更；  
* Simple注册中心。  

### 1.1.4. Dubbo支持哪些序列化方式？  
&emsp; 默认使用Hessian序列化，还有Duddo、FastJson、Java自带序列化。   

### 1.1.5. 通信协议  
&emsp; 不同服务在性能上适用不同协议进行传输，比如大数据用短连接协议，小数据大并发用长连接协议。  

|协议名称|实现描述|连接|适用范围|使用场景|
|---|---|---|---|---|
|dubbo	|传输：mina、netty、grizzy <br/>序列化：dubbo、hessian2、java、json|dubbo缺省，采用单一长连接和NIO异步通讯，传输协议TCP|1.传入传出参数数据包较小<br/>2.消费者比提供者多<br/>3.常规远程服务方法调用<br/>4.不适合传送大数据量的服务，比如文件、传视频|常规远程服务方法调用|
|rmi|传输：java  rmi<br/>序列化：java 标准序列化(实现ser接口)|1.连接个数：多连接<br/>2.连接方式：短连接<br/>3.传输协议：TCP/IP<br/>4.传输方式：BIO|1.常规RPC调用<br/>2.与原RMI客户端互操作<br/>3.可传文件<br/>4.不支持防火墙穿|常规远程服务方法调用，与原生RMI服务互操作|
|hessian|传输：Serverlet容器<br/>序列化：hessian二进制序列化|1.连接个数：多连接<br/>2.连接方式：短连接<br/>3.传输协议：HTTP<br/>4.传输方式：同步传输|1.提供者比消费者多<br/>2.可传文件<br/>3.跨语言传输| 需同时给应用程序和浏览器JS使用的服务。|
|http|传输：Servlet容器<br/>序列化：表单序列化|	1.连接个数：多连接<br/>2.连接方式：短连接<br/>3.传输协议：HTTP<br/>4.传输方式：同步传输|1.提供者多余消费者<br/>2.数据包混合	|需同时给应用程序和浏览器JS使用的服务。|
|webservice	|传输：HTTP<br/>序列化：SOAP文件序列化|1.连接个数：多连接<br/>2.连接方式：短连接<br/>3.传输协议：HTTP<br/>4.传输方式：同步传输|	1.系统集成<br/>2.跨语言调用|系统集成，跨语言调用|
|thrift	|与thrift RPC实现集成，并在基础上修改了报文头 |长连接、NIO异步传输 |||	
|Redis|||||  	

### 1.1.6. 服务提供者能实现失效踢出是什么原理？  
&emsp; 服务失效踢出基于zookeeper的临时节点原理。  

### 1.1.7. Dubbo服务之间的调用是阻塞的吗？  
&emsp; 默认是同步等待结果，阻塞的，支持异步调用。Dubbo的异步调用是基于NIO的非阻塞实现并行调用，客户端不需要启动多线程即可完成并行调用多个远程服务，相对多线程开销较小，异步调用会返回一个Future对象。  
&emsp; 异步调用流程图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-12.png)   

### 1.1.8. ※※※负载均衡  
<!-- https://mp.weixin.qq.com/s/xkwwAUV9ziabPNUMEr5DPQ -->
* <font color = "red">Random(缺省)，随机，按权重设置随机概率。</font>在一个截面上碰撞的概率高，但调用量越大分布越均匀，而且按概率使用权重后也比较均匀，有利于动态调整提供者权重。  
* <font color = "red">RoundRobin，轮循，按公约后的权重设置轮循比率。</font>  
&emsp; 轮询负载均衡算法的不足：存在慢的提供者累积请求的问题，比如：第二台机器很慢，但没挂，当请求调到第二台时就卡在那，久而久之，所有请求都卡在调到第二台上。  
* <font color = "red">LeastActive，最少活跃调用数，活跃数指调用前后计数差。</font>相同活跃数的随机。使慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大。  
* <font color = "lime">ConsistentHash，[分布式一致性哈希算法](/docs/microService/thinking/分布式算法-consistent.md)。</font>相同参数的请求总是发到同一提供者；当某一台提供者崩溃时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动。  

    * 缺省只对第一个参数Hash，如果要修改，请配置<dubbo:parameter key="hash.arguments" value="0,1" /\>  
    * 缺省用160份虚拟节点，如果要修改，请配置<dubbo:parameter key="hash.nodes" value="320" /\>  

### 1.1.9. ※※※集群容错策略  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-13.png)   
&emsp; <font color = "red">在集群调用失败时，Dubbo 提供了多种容错方案，缺省为 failover 重试。</font>下面列举dubbo支持的容错策略：  

* Failover(默认) - 失败自动切换，当出现失败，重试其它服务器。通常用于读操作，但重试会带来更长延迟。可通过 retries="2" 来设置重试次数(不含第一次)。  
* Failfast - 快速失败，只发起一次调用，失败立即报错。通常用于非幂等性的写操作，比如新增记录。
* Failsafe - 失败安全，出现异常时，直接忽略。通常用于写入审计日志等操作。  
* Failback - 失败自动恢复，后台记录失败请求，定时重发。通常用于消息通知操作。  
* Forking - 并行调用多个服务器，只要一个成功即返回。通常用于实时性要求较高的读操作，但需要浪费更多服务资源。可通过 forks="2" 来设置最大并行数。  
* Broadcast - 广播调用所有提供者，逐个调用，任意一台报错则报错。通常用于通知所有提供者更新缓存或日志等本地资源信息。  

<!-- 
Failover Cluster失败自动切换：dubbo的默认容错方案，当调用失败时自动切换到其他可用的节点，具体的重试次数和间隔时间可用通过引用服务的时候配置，默认重试次数为1也就是只调用一次。
Failback Cluster快速失败：在调用失败，记录日志和调用信息，然后返回空结果给consumer，并且通过定时任务每隔5秒对失败的调用进行重试

Failfast Cluster失败自动恢复：只会调用一次，失败后立刻抛出异常

Failsafe Cluster失败安全：调用出现异常，记录日志不抛出，返回空结果

Forking Cluster并行调用多个服务提供者：通过线程池创建多个线程，并发调用多个provider，结果保存到阻塞队列，只要有一个provider成功返回了结果，就会立刻返回结果

Broadcast Cluster广播模式：逐个调用每个provider，如果其中一台报错，在循环调用结束后，抛出异常。
-->
### 1.1.10. 服务降级  
&emsp; 当服务器压力过大时，可以通过服务降级来使某些非关键服务的调用变得简单；可以对其直接进行屏蔽，即客户端不发送请求直接返回null；也可以正常发送请求当请求超时或不可达时再返回null。  
&emsp; 服务降级的相关配置可以直接在dubbo-admin的监控页面进行配置；通常是基于消费者来配置的，在dubbo-admin找到对应的消费者想要降级的服务，点击其后面的屏蔽或容错按钮即可生效；其中，屏蔽按钮点击表示放弃远程调用直接返回空，而容错按钮点击表示继续尝试进行远程调用当调用失败时再返回空。  

------

## 1.2. Dubbo和Spring Cloud  
&emsp; Dubbo是SOA时代的产物，它的关注点主要在于服务的调用，流量分发、流量监控和熔断。  
&emsp; Spring Cloud诞生于微服务架构时代，考虑的是微服务治理的方方面面，另外由于依托了Spirng、Spirng Boot的优势之上。  

* 两个框架在开始目标就不一致：<font color = "red">Dubbo定位服务治理；Spirng Cloud是一个生态。</font>  
* <font color = "red">Dubbo底层是使用Netty这样的NIO框架，是基于TCP协议传输的，配合以Hession序列化完成RPC通信。</font><font color = "lime">而SpringCloud是基于Http协议+Rest接口调用远程过程的通信，</font>相对来说，Http请求会有更大的报文，占的带宽也会更多。但是REST相比RPC更为灵活，服务提供方和调用方的依赖只依靠一纸契约，不存在代码级别的强依赖，这在强调快速演化的微服务环境下，显得更为合适，至于注重通信速度还是方便灵活性，具体情况具体考虑。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-14.png)  

## 1.3. Dubbo生态
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo生态.png)   

### 1.3.1. Dubbo与分布式事务  
&emsp; Dubbo支持分布式事务吗？   
&emsp; 目前暂时不支持，可与通过tcc-transaction框架实现。TCC-Transaction通过Dubbo隐式传参的功能，避免自己对业务代码的入侵。 
