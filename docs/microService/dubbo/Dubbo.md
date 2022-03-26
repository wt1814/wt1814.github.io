

<!-- TOC -->

- [1. Dubbo](#1-dubbo)
    - [1.1. Dubbo工作流程](#11-dubbo工作流程)
    - [1.2. dubbo相关知识点](#12-dubbo相关知识点)
        - [1.2.1. Dubbo需要Web容器吗？内置了哪几种服务容器？](#121-dubbo需要web容器吗内置了哪几种服务容器)
        - [1.2.2. Dubbo有哪些注册中心？](#122-dubbo有哪些注册中心)
        - [1.2.3. Dubbo序列化和协议](#123-dubbo序列化和协议)
        - [1.2.4. ★★★负载均衡](#124-★★★负载均衡)
        - [1.2.5. Dubbo服务之间的调用是阻塞的吗？](#125-dubbo服务之间的调用是阻塞的吗)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. Dubbo的工作原理：  
    1. 服务启动的时候，provider和consumer根据配置信息，连接到注册中心register，分别向注册中心注册和订阅服务。  
    2. register根据服务订阅关系，返回provider信息到consumer，同时consumer会把provider信息缓存到本地。如果信息有变更，consumer会收到来自register的推送。  
    3. consumer生成代理对象，同时根据负载均衡策略，选择一台provider，同时定时向monitor记录接口的调用次数和时间信息。  
    4. 拿到代理对象之后，consumer通过`代理对象`发起接口调用。  
    5. provider收到请求后对数据进行反序列化，然后通过代理调用具体的接口实现。  
    ![image](http://www.wt1814.com/static/view/images/microService/Dubbo/dubbo-52.png)   
2. **<font color = "red">为什么要通过代理对象通信？</font>**    
    &emsp; dubbo实现接口的透明代理，封装调用细节，让用户可以像调用本地方法一样调用远程方法，同时还可以通过代理实现一些其他的策略，比如：负载、降级等。让用户可以像调用本地方法一样调用远程方法，同时还可以通过代理实现一些其他的策略，比如：  
    1. 调用的负载均衡策略  
    2. 调用失败、超时、降级和容错机制  
    3. 做一些过滤操作，比如加入缓存、mock数据  
    4. 接口调用数据统计  

# 1. Dubbo  
<!--
面试官从Dubbo泛化调用问到设计模式，我们聊了三十分钟 
https://mp.weixin.qq.com/s/2Wm2SsRa1xOMX6pV9NyCrA
-->

## 1.1. Dubbo工作流程  
![image](http://www.wt1814.com/static/view/images/microService/Dubbo/dubbo-11.png)   
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
4. <font color = "red">(notify)注册中心Registry返回服务提供者地址列表给消费者，</font><font color = "clime">如果有变更，注册中心将基于长连接推送变更数据给消费者。</font>  
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
4. 拿到代理对象之后，consumer通过`代理对象`发起接口调用。  
5. provider收到请求后对数据进行反序列化，然后通过代理调用具体的接口实现。  

![image](http://www.wt1814.com/static/view/images/microService/Dubbo/dubbo-52.png)   

&emsp; **<font color = "red">为什么要通过代理对象通信？</font>**    
&emsp; 主要是为了实现接口的透明代理，封装调用细节，让用户可以像调用本地方法一样调用远程方法，同时还可以通过代理实现一些其他的策略，比如：  

1. 调用的负载均衡策略  
2. 调用失败、超时、降级和容错机制  
3. 做一些过滤操作，比如加入缓存、mock数据  
4. 接口调用数据统计  

## 1.2. dubbo相关知识点
### 1.2.1. Dubbo需要Web容器吗？内置了哪几种服务容器？  
&emsp; 不需要，如果强制使用Web容器，只会增加复杂性，也浪费资源。  
&emsp; Dubbo内置了Spring Container、Jetty Container、Log4j Container。   
&emsp; Dubbo的服务容器只是一个简单的Main方法，并加载一个简单的Spring容器，用于暴露服务。  

### 1.2.2. Dubbo有哪些注册中心？  

* Multicast注册中心：Multicast注册中心不需要任何中心节点，只要广播地址，就能进行服务注册和发现。基于网络中组播传输实现；  
* Zookeeper注册中心：基于分布式协调系统Zookeeper实现，采用Zookeeper的watch机制实现数据变更；  
* redis注册中心：基于redis实现，采用key/Map存储，key中存储服务名和类型，Map中key存储服务URL，value服务过期时间。基于redis的发布/订阅模式通知数据变更；  
* Simple注册中心。  

### 1.2.3. Dubbo序列化和协议
&emsp; [Dubbo协议和序列化](/docs/microService/dubbo/Agreement.md)  

### 1.2.4. ★★★负载均衡  
&emsp; [Dubbo负载、容错、降级](/docs/microService/dubbo/Load.md)   

### 1.2.5. Dubbo服务之间的调用是阻塞的吗？  
&emsp; 默认是同步等待结果，阻塞的，支持异步调用。Dubbo的异步调用是基于NIO的非阻塞实现并行调用，客户端不需要启动多线程即可完成并行调用多个远程服务，相对多线程开销较小，异步调用会返回一个Future对象。  
&emsp; 异步调用流程图如下：  
![image](http://www.wt1814.com/static/view/images/microService/Dubbo/dubbo-12.png)   

