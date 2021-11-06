<!-- TOC -->

- [1. Dubbo运行流程](#1-dubbo运行流程)
    - [1.1. 解析服务](#11-解析服务)
    - [1.2. ~~暴露服务~~](#12-暴露服务)
    - [1.3. ~~引用服务~~](#13-引用服务)
    - [1.4. 拦截服务](#14-拦截服务)
    - [1.5. Invoker](#15-invoker)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. 解析服务：  
&emsp; **<font color = "clime">基于dubbo.jar内的META-INF/spring.handlers配置，Spring在遇到dubbo名称空间时，会回调DubboNamespaceHandler。所有dubbo的标签，都统一用DubboBeanDefinitionParser进行解析，基于一对一属性映射，将XML标签解析为Bean对象。</font>**  
&emsp; ⚠️注：`在暴露服务ServiceConfig.export()或引用服务ReferenceConfig.get()时，会将Bean对象转换URL格式，所有Bean属性转成URL的参数。`然后将URL传给协议扩展点，基于扩展点的扩展点自适应机制，根据URL的协议头，进行不同协议的服务暴露或引用。  
2. **服务提供者暴露服务的主过程：** `参考dubbo架构分层`，`主要分4步`。 
    1. ServiceConfig将Bean对象解析成URL格式。  
    2. 通过ProxyFactory类的getInvoker方法使用ref(实际类)生成一个AbstractProxyInvoker实例。`ProxyFactory #getInvoker(T proxy, Class<T> type, URL url)`  
    3. 通过Protocol(协议)类的export方法暴露服务。`DubboProtocol #export(Invoker<T> invoker)`  
        1. 本地各种协议暴露。  
        2. 注册中心暴露。  
    4. 如果通过注册中心暴露服务，RegistryProtocol保存URL地址和invoker的映射关系，同时注册到服务中心。  
3. **服务消费者引用服务的主过程：** `与服务暴露相反` 
    1. ReferenceConfig解析引用的服务。  
    2. ReferenceConfig类的init方法调用Protocol的refer方法生成Invoker实例。  
    3. 把Invoker转换为客户端需要的接口。  


# 1. Dubbo运行流程  


## 1.1. 解析服务 
<!-- 
URL统一模型的意义
https://mp.weixin.qq.com/s/YHFp58F1gbrWvZ91iX_WcA

--> 
&emsp; **<font color = "clime">基于dubbo.jar内的META-INF/spring.handlers配置，Spring在遇到dubbo名称空间时，会回调DubboNamespaceHandler。所有dubbo的标签，都统一用DubboBeanDefinitionParser进行解析，基于一对一属性映射，将XML标签解析为Bean对象。</font>**  

&emsp; ⚠️注：在暴露服务ServiceConfig.export()或引用服务ReferenceConfig.get()时，会将Bean对象转换URL格式，所有Bean属性转成URL的参数。然后将URL传给协议扩展点，基于扩展点的扩展点自适应机制，根据URL的协议头，进行不同协议的服务暴露或引用。  

&emsp; 注意：URL是[Dubbo公共契约](https://dubbo.apache.org/zh/docs/v2.7/dev/contract/)之一。    

## 1.2. ~~暴露服务~~  
&emsp; 详解请看[服务暴露源码解析](/docs/microService/dubbo/export.md)  

&emsp; 下图是服务提供者暴露服务的主过程：(包含3个步骤)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-29.png)   

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-53.png)   

1. 首先ServiceConfig类拿到对外提供服务的实际类 ref(如：HelloWorldImpl)，将Bean对象解析成URL格式； ~~在容器启动的时候，通过ServiceConfig解析标签，创建dubbo标签解析器来解析dubbo的标签，容器创建完成之后，触发ContextRefreshEvent事件回调开始暴露服务~~  
    * 在没有注册中心，直接暴露提供者的情况下，ServiceConfig解析出的URL的格式为：dubbo://service-host/com.foo.FooService?version=1.0.0。
    * 在有注册中心，需要注册提供者地址的情况下，ServiceConfig 解析出的URL的格式为: registry://registry-host/org.apache.dubbo.registry.RegistryService?export=URL.encode("dubbo://service-host/com.foo.FooService?version=1.0.0")。  
2. **然后通过ProxyFactory类的getInvoker方法使用ref生成一个AbstractProxyInvoker实例，** 到这一步就完成具体服务到Invoker的转化； ~~通过ProxyFactory获取到invoker，invoker包含了需要执行的方法的对象信息和具体的URL地址~~   
3. 接下来就是Invoker转换到Exporter的过程。通过Protocol类的export方法暴露服务：本地各种协议暴露、注册中心暴露。 ~~再通过DubboProtocol的实现把包装后的invoker转换成exporter，然后启动服务器server，监听端口~~  
    &emsp; Dubbo处理服务暴露的关键就在Invoker转换到Exporter的过程，上图中的红色部分。下面以Dubbo和RMI这两种典型协议的实现来进行说明：  

    * **Dubbo 的实现**  
    &emsp; Dubbo协议的Invoker转为Exporter发生在DubboProtocol 类的 export 方法，它主要是打开 socket 侦听服务，并接收客户端发来的各种请求，通讯细节由 Dubbo 自己实现。  
    * **RMI 的实现**  
    &emsp; RMI协议的Invoker转为Exporter发生在 RmiProtocol类的 export 方法，它通过 Spring 或 Dubbo 或 JDK 来实现 RMI 服务，通讯细节这一块由 JDK 底层来实现，这就省了不少工作量。  
4. **<font color = "clime">如果通过注册中心暴露服务，RegistryProtocol保存URL地址和invoker的映射关系，同时注册到服务中心。</font>**  
    &emsp; ~~基于扩展点自适应机制，通过URL的registry://协议头识别，就会调用 RegistryProtocol的export()方法，将export参数中的提供者URL，先注册到注册中心。~~  
    &emsp; ~~再重新传给Protocol扩展点进行暴露：dubbo://service-host/com.foo.FooService?version=1.0.0，然后基于扩展点自适应机制，通过提供者URL的dubbo://协议头识别，就会调用DubboProtocol的export()方法，打开服务端口。~~  


## 1.3. ~~引用服务~~
<!-- 
服务引用 时机、三种方式
https://mp.weixin.qq.com/s/Cqh-sxjDvdGt6r2XE6vjoQ
-->
&emsp; 详解请看[服务引用源码解析](/docs/microService/dubbo/introduce.md)  

&emsp; 服务暴露之后，客户端就要引用服务，最后才是调用的过程。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-30.png)   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-54.png)   

0. 客户端启动，订阅服务。  
1. ReferenceConfig 类的 init 方法调用 Protocol 的 refer 方法生成 Invoker 实例(如上图中的红色部分)，这是服务消费的关键。 ~~DubboProtocol根据订阅的得到provider地址和接口信息连接到服务端server，开启客户端client，然后创建invoker~~ 
2. 接下来把Invoker转换为客户端需要的接口(如：HelloWorld)。关于每种协议如 RMI/Dubbo/Web service 等它们在调用 refer 方法生成 Invoker 实例的细节和上一章节所描述的类似。~~invoker创建完成之后，通过invoker为服务接口生成代理对象，这个代理对象用于远程调用provider，服务的引用就完成了~~    

-----

1. 直连引用服务：  
&emsp; 在没有注册中心，直连提供者的情况下，ReferenceConfig解析出的URL的格式为：dubbo://service-host/com.foo.FooService?version=1.0.0。  
&emsp; 基于扩展点自适应机制，通过URL的dubbo://协议头识别，直接调用DubboProtocol的refer()方法，返回提供者引用。  
2. 从注册中心发现引用服务：  
&emsp; 在有注册中心，通过注册中心发现提供者地址的情况下，ReferenceConfig 解析出的URL的格式为： registry://registry-host/org.apache.dubbo.registry.RegistryService?refer=URL.encode("consumer://consumer-host/com.foo.FooService?version=1.0.0")。  
&emsp; 基于扩展点自适应机制，通过 URL 的 registry:// 协议头识别，就会调用RegistryProtocol的refer()方法，基于refer参数中的条件，查询提供者URL，如：dubbo://service-host/com.foo.FooService?version=1.0.0。  
&emsp; 基于扩展点自适应机制，通过提供者 URL 的 dubbo:// 协议头识别， **<font color = "clime">就会调用DubboProtocol的refer() 方法，得到提供者引用。</font>**  
&emsp; 然后 RegistryProtocol将多个提供者引用，通过 Cluster 扩展点，伪装成单个提供者引用返回。  

## 1.4. 拦截服务
&emsp; 基于扩展点自适应机制，所有的Protocol扩展点都会自动套上 Wrapper 类。  
&emsp; 基于ProtocolFilterWrapper类，将所有Filter组装成链，在链的最后一节调用真实的引用。  
&emsp; 基于 ProtocolListenerWrapper 类，将所有 InvokerListener 和 ExporterListener 组装集合，在暴露和引用前后，进行回调。  
&emsp; 包括监控在内，所有附加功能，全部通过 Filter 拦截实现。  

## 1.5. Invoker  
&emsp; Invoker是Dubbo的核心模型，代表一个可执行体。在服务提供方，Invoker用于调用服务提供类。在服务消费方，Invoker用于执行远程调用。Invoker是由Protocol实现类构建而来。  
&emsp; 下面用一个精简的图来说明<font color = "clime">最重要的两种Invoker：服务提供 Invoker和服务消费Invoker：</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-31.png)   
&emsp; 为了更好的解释上面这张图，结合服务消费和提供者的代码示例来进行说明：  
&emsp; 服务消费者代码：  

```java
public class DemoClientAction {
 
    private DemoService demoService;
 
    public void setDemoService(DemoService demoService) {
        this.demoService = demoService;
    }
 
    public void start() {
        String hello = demoService.sayHello("world");
    }
}
```
&emsp; 上面代码中的DemoService就是上图中服务消费端的proxy，用户代码通过这个proxy调用其对应的Invoker，而该Invoker实现了真正的远程服务调用。  
&emsp; 服务提供者代码：  

```java
public class DemoServiceImpl implements DemoService {
 
    public String sayHello(String name) throws RemoteException {
        return "Hello " + name;
    }
}
```
&emsp; 上面这个类会被封装成为一个AbstractProxyInvoker实例，并新生成一个Exporter实例。<font color = "red">这样当网络通讯层收到一个请求后，会找到对应的Exporter实例，并调用它所对应的AbstractProxyInvoker实例，从而真正调用了服务提供者的代码。Dubbo里还有一些其他的Invoker类，但上面两种是最重要的。</font>  
