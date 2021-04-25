<!-- TOC -->

- [1. Dubbo框架设计](#1-dubbo框架设计)
    - [1.1. 整体设计](#11-整体设计)
    - [1.2. 各层说明](#12-各层说明)
    - [1.3. 关系说明](#13-关系说明)
    - [1.4. 模块分包](#14-模块分包)
    - [1.5. 依赖关系](#15-依赖关系)
    - [1.6. ~~Dubbo服务调用~~](#16-dubbo服务调用)
        - [1.6.1. 调用链](#161-调用链)
        - [1.6.2. 暴露服务时序](#162-暴露服务时序)
        - [1.6.3. 引用服务时序](#163-引用服务时序)
    - [1.7. DDD领域模型](#17-ddd领域模型)

<!-- /TOC -->

# 1. Dubbo框架设计  
<!-- 
官网
http://dubbo.apache.org/zh/docs/v2.7/dev/design/
-->
&emsp; **<font color = "red">官网：http://dubbo.apache.org/</font>**  
&emsp; **本节参考官方文档：http://dubbo.apache.org/zh/docs/v2.7/dev/design/**  

## 1.1. 整体设计  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-16.png)   
&emsp; 图例说明：  

* <font color = "red">图中左边淡蓝背景的为服务消费方使用的接口，右边淡绿色背景的为服务提供方使用的接口，位于中轴线上的为双方都用到的接口。</font>  
* 图中从下至上分为十层，各层均为单向依赖，右边的黑色箭头代表层之间的依赖关系，每一层都可以剥离上层被复用，其中，Service 和 Config 层为 API，其它各层均为 SPI。  
* 图中绿色小块的为扩展接口，蓝色小块为实现类，图中只显示用于关联各层的实现类。  
* 图中蓝色虚线为初始化过程，即启动时组装链，红色实线为方法调用过程，即运行时调时链，紫色三角箭头为继承，可以把子类看作父类的同一个节点，线上的文字为调用的方法。  

## 1.2. 各层说明  
&emsp; 从大的范围来说，dubbo分为三层，business业务逻辑层由开发人员来提供接口和实现还有一些配置信息，RPC层就是真正的RPC调用的核心层，封装整个RPC的调用过程、负载均衡、集群容错、代理，remoting则是对网络传输协议和数据转换的封装。  
&emsp; 划分到更细的层面，就是图中的10层模式，整个分层依赖由上至下，除开business业务逻辑之外，其他的几层都是SPI机制。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-51.png)  

&emsp; **各层说明：**  

* service 业务逻辑层。  
* config 配置层：对外配置接口，以ServiceConfig, ReferenceConfig为中心，可以直接初始化配置类，也可以通过spring解析配置生成配置类。
* proxy 服务代理层：服务接口透明代理，生成服务的客户端Stub和服务器端Skeleton, 以ServiceProxy为中心，扩展接口为ProxyFactory。
* registry 注册中心层：封装服务地址的注册与发现，以服务URL为中心，扩展接口为RegistryFactory, Registry, RegistryService。
* cluster 路由层：封装多个提供者的路由及负载均衡，并桥接注册中心，以Invoker为中心，扩展接口为Cluster, Directory, Router, LoadBalance。
* monitor 监控层：RPC 调用次数和调用时间监控，以 Statistics 为中心，扩展接口为 MonitorFactory, Monitor, MonitorService。
* **<font color = "red">protocol 远程调用层：封装 RPC 调用，以 Invocation, Result 为中心，扩展接口为 Protocol, Invoker, Exporter。</font>**
* exchange 信息交换层：封装请求响应模式，同步转异步，以 Request, Response 为中心，扩展接口为 Exchanger, ExchangeChannel, ExchangeClient, ExchangeServer。
* transport 网络传输层：抽象 mina 和 netty 为统一接口，以 Message 为中心，扩展接口为 Channel, Transporter, Client, Server, Codec。
* serialize 数据序列化层：可复用的一些工具，扩展接口为 Serialization, ObjectInput, ObjectOutput, ThreadPool。

## 1.3. 关系说明
&emsp; **关系说明：**  

* 在 RPC 中，Protocol是核心层，也就是只要有 Protocol + Invoker + Exporter 就可以完成非透明的 RPC 调用，然后在 Invoker 的主过程上 Filter 拦截点。
* 图中的 Consumer 和 Provider 是抽象概念，只是想让看图者更直观的了解哪些类分属于客户端与服务器端，不用Client和Server的原因是Dubbo在很多场景下都使用 Provider, Consumer, Registry, Monitor划分逻辑拓普节点，保持统一概念。
* 而 Cluster 是外围概念，所以 Cluster 的目的是将多个 Invoker 伪装成一个 Invoker，这样其它人只要关注 Protocol 层 Invoker 即可，加上 Cluster 或者去掉 Cluster 对其它层都不会造成影响，因为只有一个提供者时，是不需要 Cluster 的。  
* Proxy 层封装了所有接口的透明化代理，而在其它层都以 Invoker 为中心，只有到了暴露给用户使用时，才用 Proxy 将 Invoker 转成接口，或将接口实现转成 Invoker，也就是去掉 Proxy 层 RPC 是可以 Run 的，只是不那么透明，不那么看起来像调本地服务一样调远程服务。  
&emsp; 而Remoting实现是Dubbo协议的实现，如果选择RMI协议，整个Remoting都不会用上，Remoting内部再划为Transport传输层和Exchange信息交换层，Transport 层只负责单向消息传输，是对 Mina, Netty, Grizzly 的抽象，它也可以扩展 UDP 传输，而 Exchange 层是在传输层之上封装了 Request-Response 语义。
* Registry 和 Monitor 实际上不算一层，而是一个独立的节点，只是为了全局概览，用层的方式画在一起。  

## 1.4. 模块分包  
<!-- 
http://svip.iocoder.cn/Dubbo/intro/
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-17.png)   
&emsp; 模块说明：

* dubbo-common公共逻辑模块：包括 Util 类和通用模型。
* dubbo-remoting远程通讯模块：相当于Dubbo协议的实现，如果RPC用RMI协议则不需要使用此包。
* dubbo-rpc远程调用模块：抽象各种协议，以及动态代理，只包含一对一的调用，不关心集群的管理。
* dubbo-cluster集群模块：将多个服务提供方伪装为一个提供方，包括：负载均衡, 容错，路由等，集群的地址列表可以是静态配置的，也可以是由注册中心下发。
* dubbo-registry 注册中心模块：基于注册中心下发地址的集群方式，以及对各种注册中心的抽象。
* dubbo-monitor监控模块：统计服务调用次数，调用时间的，调用链跟踪的服务。
* dubbo-config配置模块：是Dubbo对外的API，用户通过Config使用Dubbo，隐藏Dubbo所有细节。
* dubbo-container容器模块：是一个Standlone的容器，以简单的Main加载Spring启动，因为服务通常不需要Tomcat/JBoss等Web容器的特性，没必要用Web容器去加载服务。

&emsp; 整体上按照分层结构进行分包，与分层的不同点在于：  

* container 为服务容器，用于部署运行服务，没有在层中画出。
* protocol 层和 proxy 层都放在 rpc 模块中，这两层是 rpc 的核心，在不需要集群也就是只有一个提供者时，可以只使用这两层完成 rpc 调用。
* transport 层和 exchange 层都放在 remoting 模块中，为 rpc 调用的通讯基础。
* serialize 层放在 common 模块中，以便更大程度复用。  

## 1.5. 依赖关系  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-18.png)   
&emsp; 图例说明：  

* 图中小方块 Protocol, Cluster, Proxy, Service, Container, Registry, Monitor 代表层或模块，蓝色的表示与业务有交互，绿色的表示只对 Dubbo 内部交互。
* 图中背景方块 Consumer, Provider, Registry, Monitor代表部署逻辑拓扑节点。
* 图中蓝色虚线为初始化时调用，红色虚线为运行时异步调用，红色实线为运行时同步调用。
* 图中只包含 RPC 的层，不包含 Remoting 的层，Remoting 整体都隐含在 Protocol 中。

## 1.6. ~~Dubbo服务调用~~
### 1.6.1. 调用链  
&emsp; 展开总设计图的红色调用链，如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-19.png)   

### 1.6.2. 暴露服务时序  
&emsp; 展开总设计图左边服务提供方暴露服务的蓝色初始化链，时序图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-20.png)   

### 1.6.3. 引用服务时序
&emsp; 展开总设计图右边服务消费方引用服务的蓝色初始化链，时序图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-21.png)   

## 1.7. DDD领域模型
&emsp; 在Dubbo的核心领域模型中：  

* Protocol是服务域，它是 Invoker 暴露和引用的主功能入口，它负责 Invoker 的生命周期管理。
* Invoker是实体域，它是 Dubbo 的核心模型，其它模型都向它靠扰，或转换成它，它代表一个可执行体，可向它发起invoke调用，它有可能是一个本地的实现，也可能是一个远程的实现，也可能一个集群实现。
* Invocation 是会话域，它持有调用过程中的变量，比如方法名，参数等。
