
<!-- TOC -->

- [1. ~~dubbo-rpc模块简介~~](#1-dubbo-rpc模块简介)
    - [1.1. dubbo-­rpc-­api模块](#11-dubbo-­rpc-­api模块)
    - [1.2. ~~dubbo­-rpc­-default模块~~](#12-dubbo­-rpc­-default模块)

<!-- /TOC -->


# 1. ~~dubbo-rpc模块简介~~  
## 1.1. dubbo-­rpc-­api模块  
&emsp; dubbo­-rpc-­api模块是dubbo最为核心的一个模块，它定义了dubbo作为一个rpc框架最核心的一些接口和抽象实现。  

&emsp; **简化的类图**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-38.png)  
&emsp; 该图是经过简化后的rpc-­api模块的类图，去除了一些非关键的属性和方法定义，也去除了一些非核心的类和接口，只是一个简化了的的示意图。  

**核心类说明**    
* Protocol  
&emsp; 服务协议。这是rpc模块中最核心的一个类，它定义了rpc的最主要的两个行为即：1、provider暴露远程服务，即将调用信息发布到服务器上的某个URL上去，可以供消费者连接调用，一般是将某个service类的全部方法整体发布到服务器上。2、consumer引用远程服务，即根据service的服务类和provider发布服务的URL转化为一个Invoker对象，消费者可以通过该对象调用provider发布的远程服务。这其实概括了rpc的最为核心的职责，提供了多级抽象的实现、包装器实现等。  
* AbstractProtocol  
&emsp; Protocol的顶层抽象实现类，它定义了这些属性：1、exporterMap表示发布过的serviceKey和Exporter（远程服务发布的引用）的映射表；2、invokers是一个Invoker对象的集合，表示层级暴露过远程服务的服务执行体对象集合。还提供了一个通用的服务发布销毁方法destroy，该方法是一个通用方法，它清空了两个集合属性，调用了所有invoker的destroy方法，也调用所有exporter对象的unexport方法。
* AbstractProxyProtocol  
&emsp; 继承自AbstractProtoco的一个抽象代理协议类。它聚合了代理工厂ProxyFactory对象来实现服务的暴露和引用。  
* ProtocolFilterWrapper  
&emsp; 一个Protocol的支持过滤器的装饰器。通过该装饰器的对原始对象的包装使得Protocol支持可扩展的过滤器链，已经支持的包括ExceptionFilter、ExecuteLimitFilter和TimeoutFilter等多种支持不同特性的过滤器。
* ProtocolListenerWrapper  
&emsp; 一个支持监听器特性的Protocal的包装器。支持两种监听器的功能扩展，分别是：ExporterListener是远程服务发布监听器，可以监听服务发布和取消发布两个事件点；InvokerListener是服务消费者引用调用器的监听器，可以监听引用和销毁两个事件方法。支持可扩展的事件监听模型，目前只提供了一些适配器InvokerListenerAdapter、ExporterListenerAdapter以及简单的过期服务调用监听器DeprecatedInvokerListener。开发者可自行扩展自己的监听器。  
* ProxyFactory  
&emsp; dubbo的代理工厂。定义了两个接口分别是：getProxy根据invoker目标接口的代理对象，一般是消费者获得代理对象触发远程调用；getInvoker方法将代理对象proxy、接口类type和远程服务的URL获取执行对象Invoker，往往是提供者获得目标执行对象执行目标实现调用。AbstractProxyFactory是其抽象实现，提供了getProxy的模版方法实现，使得可以支持多接口的映射。dubbo最终内置了两种动态代理的实现，分别是jdkproxy和javassist。默认的实现使用javassist。  
* Invoker  
&emsp; 该接口是服务的执行体。它有获取服务发布的URL，服务的接口类等关键属性的行为；还有核心的服务执行方法invoke，执行该方法后返回执行结果Result，而传递的参数是调用信息Invocation。该接口有大量的抽象和具体实现类。AbstractProxyInvoker是基于代理的执行器抽象实现，AbstractInvoker是通用的抽象实现。  

## 1.2. ~~dubbo­-rpc­-default模块~~  
&emsp; dubbo-­rpc-­default模块是dubbo­-rpc-­api模块的默认实现，提供了默认的dubbo协议的实现，它是所有模块中最为复杂的一个模块，因为底层的协议都是它自己实现的。  
&emsp; **简化类图**    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-39.png)  
&emsp; 从图中可以看出该模块下的类主要是实现了dubbo­-rpc-­api和dubbo­-remoting-­api两个模块中定义的一些接口和抽象类。扩展了一种duubo框架自定义的dubbo协议，包括编解码和方法调用处理等。  
&emsp; **核心类说明**    

* DubboProtocol  
&emsp; 该类是抽象协议实现类AbstractProtocol的具体的dubbo协议的实现。  
* DubboInvoker  
&emsp; 该类是消费者dubbo协议的执行器。它处理了dubbo协议在客户端调用远程接口的逻辑实现。  