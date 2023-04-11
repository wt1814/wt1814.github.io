

&emsp; [初始化源码解析](/docs/microService/dubbo/dubboSpring.md)  
&emsp; [服务暴露源码解析](/docs/microService/dubbo/export.md)  
&emsp; [服务引用源码解析](/docs/microService/dubbo/introduce.md)  
&emsp; [服务调用源码解析](/docs/microService/dubbo/call.md)  
&emsp; [再次理解dubbo-rpc包](/docs/microService/dubbo/dubboRPC.md)  


&emsp; [时间轮算法](/docs/microService/dubbo/timeWheel.md)  

<!-- 

妹妹问我：Dubbo集群容错负载均衡 
https://mp.weixin.qq.com/s/-IkHNAM4B0R_j50LkQunig

芋道源码
http://www.iocoder.cn/Dubbo/good-collection/

Dubbo原理简述四：服务暴露、服务引用和调用流程
https://blog.csdn.net/qq_33404395/article/details/86498060

-->

&emsp;  模块分包  
<!-- 
http://svip.iocoder.cn/Dubbo/intro/
-->
![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-17.png)   
&emsp; 模块说明：

* dubbo-common公共逻辑模块：包括 Util 类和通用模型。
* dubbo-remoting远程通讯模块：相当于Dubbo协议的实现，如果RPC用RMI协议则不需要使用此包。
* dubbo-rpc远程调用模块：抽象各种协议，以及动态代理，只包含一对一的调用，不关心集群的管理。
* dubbo-cluster集群模块：将多个服务提供方伪装为一个提供方，包括：负载均衡, 容错，路由等，集群的地址列表可以是静态配置的，也可以是由注册中心下发。
* dubbo-registry注册中心模块：基于注册中心下发地址的集群方式，以及对各种注册中心的抽象。
* dubbo-monitor监控模块：统计服务调用次数，调用时间的，调用链跟踪的服务。
* dubbo-config配置模块：是Dubbo对外的API，用户通过Config使用Dubbo，隐藏Dubbo所有细节。
* dubbo-container容器模块：是一个Standlone的容器，以简单的Main加载Spring启动，因为服务通常不需要Tomcat/JBoss等Web容器的特性，没必要用Web容器去加载服务。

&emsp; 整体上按照分层结构进行分包，与分层的不同点在于：  

* container 为服务容器，用于部署运行服务，没有在层中画出。
* protocol 层和 proxy 层都放在 rpc 模块中，这两层是 rpc 的核心，在不需要集群也就是只有一个提供者时，可以只使用这两层完成 rpc 调用。
* transport 层和 exchange 层都放在 remoting 模块中，为 rpc 调用的通讯基础。
* serialize 层放在 common 模块中，以便更大程度复用。  


-------------

1. 调用链  
&emsp; 展开总设计图的红色调用链，如下：  
![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-19.png)   

2. 暴露服务时序  
&emsp; 展开总设计图左边服务提供方暴露服务的蓝色初始化链，时序图如下：  
![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-20.png)   

3. 引用服务时序  
&emsp; 展开总设计图右边服务消费方引用服务的蓝色初始化链，时序图如下：  
![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-21.png)   

