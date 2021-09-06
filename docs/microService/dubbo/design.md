<!-- TOC -->

- [1. Dubbo框架设计](#1-dubbo框架设计)
    - [1.1. 分层架构](#11-分层架构)
    - [1.2. Dubbo总体调用过程](#12-dubbo总体调用过程)
    - [1.3. ~~关系说明~~](#13-关系说明)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 从大的范围来说，dubbo分为三层：
    * business业务逻辑层由开发人员来提供接口和实现还有一些配置信息。
    * RPC层就是真正的RPC调用的核心层，封装整个RPC的调用过程、负载均衡、集群容错、代理。
    * remoting则是对网络传输协议和数据转换的封装。  
2. RPC层：config，配置层、proxy，代理层、register，服务注册层、cluster，路由层、monitor，监控层、protocol，远程调用层。    
    1. **<font color = "red">proxy，服务代理层：服务接口透明代理，生成服务的客户端Stub和服务器端Skeleton，以ServiceProxy为中心，扩展接口为ProxyFactory。</font>**  
    &emsp; **<font color = "red">Proxy层封装了所有接口的透明化代理，而在其它层都以Invoker为中心，</font><font color = "blue">只有到了暴露给用户使用时，才用Proxy将Invoker转成接口，或将接口实现转成 Invoker，也就是去掉Proxy层RPC是可以Run的，只是不那么透明，不那么看起来像调本地服务一样调远程服务。</font>**  
    &emsp; dubbo实现接口的透明代理，封装调用细节，让用户可以像调用本地方法一样调用远程方法，同时还可以通过代理实现一些其他的策略，比如：负载、降级......  
    2. **<font color = "red">protocol，远程调用层：封装RPC调用，以Invocation, Result为中心，扩展接口为Protocol, Invoker, Exporter。</font>**
3. remoting层：  
    1. 网络传输层：抽象mina和netty为统一接口，以Message为中心，扩展接口为Channel, Transporter, Client, Server, Codec。  
    2. 数据序列化层：可复用的一些工具，扩展接口为Serialization, ObjectInput, ObjectOutput, ThreadPool。  



# 1. Dubbo框架设计  
<!-- 
官网
http://dubbo.apache.org/zh/docs/v2.7/dev/design/
-->
&emsp; **<font color = "red">官网：http://dubbo.apache.org/</font>**  
&emsp; **本节参考官方文档：http://dubbo.apache.org/zh/docs/v2.7/dev/design/**  

## 1.1. 分层架构  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-16.png)   
&emsp; 图例说明：  

* <font color = "red">图中左边淡蓝背景的为服务消费方使用的接口，右边淡绿色背景的为服务提供方使用的接口，位于中轴线上的为双方都用到的接口。</font>  
* 图中从下至上分为十层，各层均为单向依赖，右边的黑色箭头代表层之间的依赖关系，每一层都可以剥离上层被复用，其中，Service 和 Config 层为 API，其它各层均为 SPI。  
* 图中绿色小块的为扩展接口，蓝色小块为实现类，图中只显示用于关联各层的实现类。  
* 图中蓝色虚线为初始化过程，即启动时组装链，红色实线为方法调用过程，即运行时调时链，紫色三角箭头为继承，可以把子类看作父类的同一个节点，线上的文字为调用的方法。  


-------------
 
<!-- 
&emsp; 划分到更细的层面，就是图中的10层模式，整个分层依赖由上至下，除开business业务逻辑之外，其他的几层都是SPI机制。  
-->


&emsp; Dubbo的总体分为业务层(Biz)、RPC层、Remote层。business业务逻辑层由开发人员来提供接口和实现还有一些配置信息，RPC层就是真正的RPC调用的核心层，封装整个RPC的调用过程、负载均衡、集群容错、代理，remoting则是对网络传输协议和数据转换的封装。  
&emsp; 如果把每一层继续做细分，那么一共可以分为十层。其中，Monitor层在最新的官方PPT中并不再作为单独的一层。整个分层依赖由上至下，除开business业务逻辑之外，其他的几层都是SPI机制。如图1-7所示，图中左边是具体的分层，右边是该层中比较重要的接口。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-61.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-51.png)  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-62.png)  

&emsp; **各层说明：**  

* service 业务逻辑层。  
* config 配置层：对外配置接口，以ServiceConfig, ReferenceConfig为中心，可以直接初始化配置类，也可以通过spring解析配置生成配置类。
* **<font color = "clime">proxy 服务代理层：服务接口透明代理，生成服务的客户端Stub和服务器端Skeleton, 以ServiceProxy为中心，扩展接口为ProxyFactory。</font>**  
* registry 注册中心层：封装服务地址的注册与发现，以服务URL为中心，扩展接口为RegistryFactory, Registry, RegistryService。
* cluster 路由层：封装多个提供者的路由及负载均衡，并桥接注册中心，以Invoker为中心，扩展接口为Cluster, Directory, Router, LoadBalance。
* monitor 监控层：RPC 调用次数和调用时间监控，以 Statistics 为中心，扩展接口为 MonitorFactory, Monitor, MonitorService。
* **<font color = "clime">protocol 远程调用层：封装 RPC 调用，以 Invocation, Result 为中心，扩展接口为 Protocol, Invoker, Exporter。</font>**
* exchange 信息交换层：封装请求响应模式，同步转异步，以 Request, Response 为中心，扩展接口为 Exchanger, ExchangeChannel, ExchangeClient, ExchangeServer。
* transport 网络传输层：抽象 mina 和 netty 为统一接口，以 Message 为中心，扩展接口为 Channel, Transporter, Client, Server, Codec。
* serialize 数据序列化层：可复用的一些工具，扩展接口为 Serialization, ObjectInput, ObjectOutput, ThreadPool。  


&emsp; Service和Config两层可以认为是API层，主要提供给API使用者，使用者无须关心底层的实现，只需要配置和完成业务代码即可；后面所有的层级合在一起，可以认为是SPI层，主要提供给扩展者使用，即用户可以基于Dubb。框架做定制性的二次开发，扩展其功能。Dubbo的扩展能力非常强，这也是Dubbo一直广受欢迎的原因之一。  
&emsp; 每一层都会有比较核心的接口来支撑整个层次的逻辑，后续如果读者需要阅读源码，则可以从这些核心接口开始，梳理整个逻辑过程。在后面的章节中，我们会围绕不同的层次对其原理进行讲解。  


## 1.2. Dubbo总体调用过程
&emsp; 或许有读者目前还不能理解整个组件串起来的工作过程，因此先介绍一下服务的暴露过程。首先，服务器端（服务提供者）在框架启动时，会初始化服务实例，通过Proxy组件调用具体协议（Protocol ），把服务端要暴露的接口封装成Invoker（真实类型是AbstractProxylnvoker ，然后转换成Exporter，这个时候框架会打开服务端口等并记录服务实例到内存中，最后通过Registry把服务元数据注册到注册中心。这就是服务端（服务提供者）整个接口暴露的过程。读者可能对里面的各种组件还不清楚，下面就讲解组件的含义：  

* Proxy组件：Dubbo中只需要引用一个接口就可以调用远程的服务，并且只需要像调用本地方法一样调用即可。其实是Dubbo框架生成了代理类，调用的方法其实是Proxy组件生成的代理方法，会自动发起远程/本地调用，并返回结果，整个过程对用户完全透明。 
* Protocol：顾名思义，协议就是对数据格式的一种约定。它可以把我们对接口的配置，根据不同的协议转换成不同的Invoker对象。例如：用DubboProtocol可以把XML文件中一个远程接口的配置转换成一个Dubbolnvokero  
* Exporter：用于暴露到注册中心的对象，它的内部属性持有了 Invoker对象，可以认为它在Invoker上包了 一层。 
* Registry：把Exporter注册到注册中心。

&emsp; 以上就是整个服务暴露的过程，消费方在启动时会通过Registry在注册中心订阅服务端的元数据（包括IP和端口）。这样就可以得到刚才暴露的服务了。  

&emsp; 下面来看一下消费者调用服务提供者的总体流程，此处只介绍远程调用，本地调用是远程调用的子集，因此不在此展开。Dubbo组件调用总体流程如图1-8所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-63.png)  
&emsp; 首先，调用过程也是从一个Proxy开始的，Proxy持有了一个Invoker对象。然后触发invoke调用。在invoke调用过程中，需要使用Cluster, Cluster负责容错，如调用失败的重试。Cluster在调用之前会通过Directory获取所有可以调用的远程服务Invoker列表（一个接口可能有多个节点提供服务）。由于可以调用的远程服务有很多，此时如果用户配置了路由规则（如指定某些方法只能调用某个节点），那么还会根据路由规则将Invoker列表过滤一遍。  
&emsp; 然后，存活下来的Invoker可能还会有很多，此时要调用哪一个呢？于是会继续通过LoadBalance方法做负载均衡，最终选出一个可以调用的Invokero这个Invoker在调用之前又会经过一个过滤器链，这个过滤器链通常是处理上下文、限流、计数等。  
&emsp; 接着，会使用Client做数据传输，如常见的Netty Client等。传输之前肯定要做一些私有协议的构造，此时就会用到Codec接口。构造完成后，就对数据包做序列化（Serialization) ，然后传输到服务提供者端。服务提供者收到数据包，也会使用Codec处理协议头及一些半包、粘包等。处理完成后再对完整的数据报文做反序列化处理。

&emsp; 随后，这个Request会被分配到线程池ThreadPool中进行处理oServer会处理这些Request，根据请求查找对应的Exporter它内部持有了 Invoker 0 Invoker是被用装饰器模式一层一层套了非常多Filter的，因此在调用最终的实现类之前，又会经过一个服务提供者端的过滤器链。  
&emsp; 最终，得到了具体接口的真实实现并调用，再原路把结果返回。 至此，一个完整的远程调用过程就结束了。  


## 1.3. ~~关系说明~~
&emsp; **关系说明：**  

* 在 RPC 中，Protocol是核心层，也就是只要有 Protocol + Invoker + Exporter 就可以完成非透明的 RPC 调用，然后在 Invoker 的主过程上 Filter 拦截点。
* 图中的 Consumer 和 Provider 是抽象概念，只是想让看图者更直观的了解哪些类分属于客户端与服务器端，不用Client和Server的原因是Dubbo在很多场景下都使用 Provider, Consumer, Registry, Monitor划分逻辑拓普节点，保持统一概念。
* 而 Cluster 是外围概念，所以 Cluster 的目的是将多个 Invoker 伪装成一个 Invoker，这样其它人只要关注 Protocol 层 Invoker 即可，加上 Cluster 或者去掉 Cluster 对其它层都不会造成影响，因为只有一个提供者时，是不需要 Cluster 的。  
* **<font color = "clime">Proxy层封装了所有接口的透明化代理，而在其它层都以 Invoker 为中心，只有到了暴露给用户使用时，才用 Proxy 将 Invoker 转成接口，或将接口实现转成 Invoker，也就是去掉 Proxy 层 RPC 是可以 Run 的，只是不那么透明，不那么看起来像调本地服务一样调远程服务。</font>**  
&emsp; 而Remoting实现是Dubbo协议的实现，如果选择RMI协议，整个Remoting都不会用上，Remoting内部再划为Transport传输层和Exchange信息交换层，Transport 层只负责单向消息传输，是对 Mina, Netty, Grizzly 的抽象，它也可以扩展 UDP 传输，而 Exchange 层是在传输层之上封装了 Request-Response 语义。
* Registry 和 Monitor 实际上不算一层，而是一个独立的节点，只是为了全局概览，用层的方式画在一起。  
