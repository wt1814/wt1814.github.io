

<!-- TOC -->

- [1. 配置中心使用长轮询推送](#1-配置中心使用长轮询推送)
    - [1.1. 数据交互模式](#11-数据交互模式)
    - [1.2. 长轮询与轮询](#12-长轮询与轮询)
    - [1.3. 配置中心长轮询设计](#13-配置中心长轮询设计)

<!-- /TOC -->

1. push、pull、长轮询
    &emsp; push模型的好处是实时写新的数据到客户端。pull模型的好处是请求/响应模式，完成之后就断开，而不是像push模型一样，一直长连接不断开，如果每个连接都不断开，那么服务器连接数量很快会被耗尽。  
    &emsp; **<font color = "red">长轮询（Long Polling）和轮询（Polling）的区别，两者都是拉模式的实现。</font>**  
2. 配置中心使用长轮询推送
    &emsp; 客户端发起长轮询，如果服务端的数据没有发生变更，会 hold 住请求，直到服务端的数据发生变化，或者等待一定时间超时才会返回。返回后，客户端又会立即再次发起下一次长轮询。配置中心使用「长轮询」如何解决「轮询」遇到的问题也就显而易见了：
    * 推送延迟。服务端数据发生变更后，长轮询结束，立刻返回响应给客户端。
    * 服务端压力。长轮询的间隔期一般很长，例如 30s、60s，并且服务端 hold 住连接不会消耗太多服务端资源。



&emsp; 长轮询的好处是，既有push模型的服务器实时写数据到客户端，又有pull模型的避免一直长连接。  


# 1. 配置中心使用长轮询推送
<!-- 
https://zhuanlan.zhihu.com/p/351196920
-->
&emsp; 目前比较流行的两款配置中心：Nacos 和 Apollo 恰恰都没有使用长连接，而是使用的长轮询。  


## 1.1. 数据交互模式
&emsp; 众所周知，数据交互有两种模式：Push（推模式）和 Pull（拉模式）。  
&emsp; 推模式指的是客户端与服务端建立好网络长连接，服务方有相关数据，直接通过长连接通道推送到客户端。其优点是及时，一旦有数据变更，客户端立马能感知到；另外对客户端来说逻辑简单，不需要关心有无数据这些逻辑处理。缺点是不知道客户端的数据消费能力，可能导致数据积压在客户端，来不及处理。  
&emsp; 拉模式指的是客户端主动向服务端发出请求，拉取相关数据。其优点是此过程由客户端发起请求，故不存在推模式中数据积压的问题。缺点是可能不够及时，对客户端来说需要考虑数据拉取相关逻辑，何时去拉，拉的频率怎么控制等等。  

## 1.2. 长轮询与轮询  
&emsp; 在开头，重点介绍一下 **<font color = "red">长轮询（Long Polling）和轮询（Polling）的区别，两者都是拉模式的实现。</font>**   
&emsp; “轮询”是指不管服务端数据有无更新，客户端每隔定长时间请求拉取一次数据，可能有更新数据返回，也可能什么都没有。配置中心如果使用「轮询」实现动态推送，会有以下问题：  

* 推送延迟。客户端每隔 5s 拉取一次配置，若配置变更发生在第 6s，则配置推送的延迟会达到 4s。  
* 服务端压力。配置一般不会发生变化，频繁的轮询会给服务端造成很大的压力。  
* 推送延迟和服务端压力无法中和。降低轮询的间隔，延迟降低，压力增加；增加轮询的间隔，压力降低，延迟增高。

&emsp; “长轮询”则不存在上述的问题。客户端发起长轮询，如果服务端的数据没有发生变更，会 hold 住请求，直到服务端的数据发生变化，或者等待一定时间超时才会返回。返回后，客户端又会立即再次发起下一次长轮询。配置中心使用「长轮询」如何解决「轮询」遇到的问题也就显而易见了：  

* 推送延迟。服务端数据发生变更后，长轮询结束，立刻返回响应给客户端。  
* 服务端压力。长轮询的间隔期一般很长，例如 30s、60s，并且服务端 hold 住连接不会消耗太多服务端资源。

&emsp; 以Nacos为例的长轮询流程如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/websocket/websocket-3.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/websocket/websocket-4.png)  


## 1.3. 配置中心长轮询设计  
&emsp; 上文的图中，介绍了长轮询的流程，本节会详解配置中心长轮询的设计细节。  

&emsp; 客户端发起长轮询  
&emsp; 客户端发起一个 HTTP 请求，请求信息包含配置中心的地址，以及监听的 dataId（本文出于简化说明的考虑，认为 dataId 是定位配置的唯一键）。若配置没有发生变化，客户端与服务端之间一直处于连接状态。

&emsp; 服务端监听数据变化  
&emsp; 服务端会维护 dataId 和长轮询的映射关系，如果配置发生变化，服务端会找到对应的连接，为响应写入更新后的配置内容。如果超时内配置未发生变化，服务端找到对应的超时长轮询连接，写入 304 响应。  

    304 在 HTTP 响应码中代表“未改变”，并不代表错误。比较契合长轮询时，配置未发生变更的场景。

&emsp; 客户端接收长轮询响应  

&emsp; 首先查看响应码是 200 还是 304，以判断配置是否变更，做出相应的回调。之后再次发起下一次长轮询。  

&emsp; 服务端设置配置写入的接入点  
&emsp; 主要用配置控制台和 client 发布配置，触发配置变更。  
&emsp; 这几点便是配置中心实现长轮询的核心步骤，也是指导下面章节代码实现的关键。但在编码之前，仍有一些其他的注意点需要实现阐明。  

&emsp; 配置中心往往是为分布式的集群提供服务的，而每个机器上部署的应用，又会有多个 dataId 需要监听，实例级别 * 配置数是一个不小的数字，配置中心服务端维护这些 dataId 的长轮询连接显然不能用线程一一对应，否则会导致服务端线程数爆炸式增长。一个 Tomcat 也就 200 个线程，长轮询也不应该阻塞 Tomcat 的业务线程，所以需要配置中心在实现长轮询时，往往采用异步响应的方式来实现。而比较方便实现异步 HTTP 的常见手段便是 Servlet3.0 提供的AsyncContext 机制。  

    Servlet3.0 并不是一个特别新的规范，它跟 Java 6 是同一时期的产物。例如 SpringBoot 内嵌的 Tomcat 很早就支持了 Servlet3.0，你无需担心 AsyncContext 机制不起作用。

&emsp; SpringMVC 实现了 DeferredResult 和 Servlet3.0 提供的 AsyncContext 其实没有多大区别，我并没有深入研究过两个实现背后的源码，但从使用层面上来看，AsyncContext 更加的灵活，例如其可以自定义响应码，而 DeferredResult 在上层做了封装，可以快速的帮助开发者实现一个异步响应，但没法细粒度地控制响应。所以下文的示例中，我选择了 AsyncContext。  