
<!-- TOC -->

- [1. Dubbo和Zookeeper](#1-dubbo和zookeeper)
    - [1.1. Dubbo](#11-dubbo)
        - [1.1.1. 分布式服务治理](#111-分布式服务治理)
            - [1.1.1.1. Dubbo和Spring Cloud](#1111-dubbo和spring-cloud)
            - [1.1.1.2. Spring Cloud Alibaba介绍](#1112-spring-cloud-alibaba介绍)
        - [1.1.2. RPC介绍](#112-rpc介绍)
        - [1.1.3. Dubbo介绍](#113-dubbo介绍)
        - [1.1.4. Dubbo框架设计](#114-dubbo框架设计)
        - [1.1.5. Dubbo初始化（Dubbo和Spring）](#115-dubbo初始化dubbo和spring)
        - [1.1.6. 暴露和引用服务（实际类 ---> invoker ---> ）](#116-暴露和引用服务实际类-----invoker-----)
            - [1.1.6.1. Dubbo序列化和协议](#1161-dubbo序列化和协议)
                - [1.1.6.1.1. Dubbo协议长连接和心跳](#11611-dubbo协议长连接和心跳)
                    - [1.1.6.1.1.1. 协议长链接](#116111-协议长链接)
            - [1.1.6.2. Dubbo心跳机制](#1162-dubbo心跳机制)
        - [1.1.7. 服务调用](#117-服务调用)
            - [1.1.7.1. 服务调用介绍](#1171-服务调用介绍)
            - [1.1.7.2. Dubbo集群容错](#1172-dubbo集群容错)
        - [1.1.8. ~~扩展点加载(SPI)~~](#118-扩展点加载spi)
        - [1.1.9. Dubbo和Netty](#119-dubbo和netty)
        - [1.1.10. Dubbo总结](#1110-dubbo总结)
    - [1.2. Zookeeper](#12-zookeeper)
        - [1.2.1. ZK服务端](#121-zk服务端)
        - [1.2.2. ZK客户端](#122-zk客户端)
        - [1.2.3. ZK应用场景和弊端](#123-zk应用场景和弊端)

<!-- /TOC -->



# 1. Dubbo和Zookeeper

## 1.1. Dubbo
### 1.1.1. 分布式服务治理
#### 1.1.1.1. Dubbo和Spring Cloud
1. 两个框架在开始目标就不一致：<font color = "red">Dubbo定位服务治理；Spirng Cloud是一个生态。</font>  
    * Dubbo是SOA时代的产物，它的关注点主要在于服务的调用，流量分发、流量监控和熔断。  
    * Spring Cloud诞生于微服务架构时代，考虑的是微服务治理的方方面面，另外由于依托了Spirng、Spirng Boot的优势之上。  
2. <font color = "red">Dubbo底层是使用Netty这样的NIO框架，是基于TCP协议传输的，配合以Hession序列化完成RPC通信。</font><font color = "clime">而SpringCloud是基于Http协议+Rest接口调用远程过程的通信，</font>相对来说，Http请求会有更大的报文，占的带宽也会更多。但是`REST相比RPC更为灵活`，服务提供方和调用方的依赖只依靠一纸契约，不存在代码级别的强依赖，这在强调快速演化的微服务环境下，显得更为合适，`至于注重通信速度还是方便灵活性，具体情况具体考虑。`  

#### 1.1.1.2. Spring Cloud Alibaba介绍  
1. Spring Cloud与Dubbo的比较本身是不公平的，主要前者是一套较为完整的架构方案，而Dubbo只是服务治理与RPC实现方案。Spring Cloud Alibaba是阿里巴巴提供的微服务开发一站式解决方案，是阿里巴巴开源中间件与 Spring Cloud体系的融合。   
2. 集成 Spring Cloud 组件： **<font color = "clime">Spring Cloud Alibaba作为整套的微服务解决组件，只依靠目前阿里的开源组件是不够的，更多的是集成当前的社区组件，所以 Spring Cloud Alibaba 可以集成 Zuul，OpenFeign等网关，也支持 Spring Cloud Stream消息组件。</font>**  
3. **<font color = "clime">使用@DubboTransported注解可将底层的Rest协议无缝切换成Dubbo RPC协议，进行RPC调用。</font>**  
4. Spring Cloud Alibaba 基于 Nacos 提供 spring-cloud-alibaba-starter-nacos-discovery & spring-cloud-alibaba-starter-nacos-config 实现了服务注册 & 配置管理功能。  
&emsp; 使用 Seata 解决微服务场景下面临的分布式事务问题。  

### 1.1.2. RPC介绍
&emsp; ......  


### 1.1.3. Dubbo介绍
1. Dubbo的工作原理：  
    1. 服务启动的时候，provider和consumer根据配置信息，连接到注册中心register，分别向注册中心注册和订阅服务。  
    2. register根据服务订阅关系，返回provider信息到consumer，同时 **<font color = "clime">consumer会把provider信息缓存到本地。如果信息有变更，consumer会收到来自register的推送。</font>**  
    3. consumer生成代理对象，同时根据负载均衡策略，选择一台provider，同时定时向monitor记录接口的调用次数和时间信息。  
    4. **<font color = "clime">拿到代理对象之后，consumer通过`代理对象`发起接口调用。</font>**  
    5. provider收到请求后对数据进行反序列化，然后通过代理调用具体的接口实现。  
    ![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-52.png)   

&emsp; &emsp; [Dubbo负载、容错、降级](/docs/microService/dubbo/Load.md)  
&emsp; &emsp; [Dubbo协议和序列化](/docs/microService/dubbo/Agreement.md)  

### 1.1.4. Dubbo框架设计
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

### 1.1.5. Dubbo初始化（Dubbo和Spring）  


### 1.1.6. 暴露和引用服务（实际类 ---> invoker ---> ）
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


#### 1.1.6.1. Dubbo序列化和协议
1. 不同服务在性能上适用不同协议进行传输，比如`大数据用短连接协议`，`小数据大并发用长连接协议`。  
2. 默认使用Hessian序列化（跨语言），还有Duddo、FastJson、Java自带序列化。   

##### 1.1.6.1.1. Dubbo协议长连接和心跳
###### 1.1.6.1.1.1. 协议长链接
&emsp; Dubbo缺省协议采用单一长连接和NIO异步通讯，适合于小数据量大并发的服务调用，以及服务消费者机器数远大于服务提供者机器数的情况。  
&emsp; 注意：Dubbo缺省协议不适合传送大数据量的服务，比如传文件，传视频等，除非请求量很低。  
&emsp; Dubbo协议采用长连接，还可以防止注册中心宕机风险。  

#### 1.1.6.2. Dubbo心跳机制  
&emsp; `主流的RPC框架都会追求性能选择使用长连接`，所以如何`保活连接`就是一个重要的话题。如何确保连接的有效性呢，在TCP中用到了KeepAlive机制。有了KeepAlive机制往往是不够用的，还需要配合心跳机制来一起使用。  
&emsp; 何为心跳机制，简单来讲就是客户端启动一个定时器用来定时发送请求，服务端接到请求进行响应，如果多次没有接受到响应，那么客户端认为连接已经断开，可以断开半打开的连接或者进行重连处理。   
&emsp; `Dubbo的心跳方案：Dubbo对于建立的每一个连接，同时在客户端和服务端开启了2个定时器，一个用于定时发送心跳，一个用于定时重连、断连，执行的频率均为各自检测周期的 1/3。`定时发送心跳的任务负责在连接空闲时，向对端发送心跳包。定时重连、断连的任务负责检测 lastRead 是否在超时周期内仍未被更新，如果判定为超时，客户端处理的逻辑是重连，服务端则采取断连的措施。  

### 1.1.7. 服务调用
#### 1.1.7.1. 服务调用介绍


#### 1.1.7.2. Dubbo集群容错
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


### 1.1.8. ~~扩展点加载(SPI)~~  
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


### 1.1.9. Dubbo和Netty  


### 1.1.10. Dubbo总结  
1. dubbo初始化：Dubbo和Spring  
2. dubbo的十层架构  
3. dubbo和netty  

## 1.2. Zookeeper
&emsp; **<font color = "clime">Zookeeper是一个分布式协调服务的开源框架。主要用来解决分布式集群中应用系统的一致性问题。</font>**  

### 1.2.1. ZK服务端
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

### 1.2.2. ZK客户端
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

### 1.2.3. ZK应用场景和弊端
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



