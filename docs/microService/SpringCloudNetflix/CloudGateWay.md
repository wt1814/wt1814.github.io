
<!-- TOC -->

- [1. Spring Cloud Gateway](#1-spring-cloud-gateway)
    - [1.1. Gateway简介（Gateway的特性/Gateway VS zuul）](#11-gateway简介gateway的特性gateway-vs-zuul)
    - [1.2. 路由](#12-路由)
    - [1.3. 断言](#13-断言)
    - [1.4. 过滤器](#14-过滤器)
        - [1.4.1. 过滤器](#141-过滤器)
        - [1.4.2. GatewayFilter（局部过滤器）](#142-gatewayfilter局部过滤器)
        - [1.4.3. GlobalFilter（全局过滤器）](#143-globalfilter全局过滤器)
        - [1.4.4. 自定义局部过滤器](#144-自定义局部过滤器)
    - [1.5. 集成注册中心](#15-集成注册中心)
        - [1.5.1. 实现动态路由？](#151-实现动态路由)
    - [1.6. 自定义全局异常处理](#16-自定义全局异常处理)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. Gateway的特性/Gateway VS zuul  



# 1. Spring Cloud Gateway
<!-- 

*** Spring Cloud Gateway夺命连环10问？
https://mp.weixin.qq.com/s/YdMQTVH8vqKnWXyRXxTmag
网关Spring Cloud Gateway科普 
https://mp.weixin.qq.com/s/NIc6bredbMF2jpabagv2lg

-->



## 1.1. Gateway简介（Gateway的特性/Gateway VS zuul）  

1. 为什么选择Spring cloud Gateway？  
在1.x版本中都是采用的Zuul网关；但在2.x版本中，zuul的升级一直跳票，Spring Cloud最后自己研发了一个网关替代Zuul，那就是Spring Cloud Gateway。  
它的很多思想都是借鉴zuul，所谓青出于蓝而胜于蓝，功能和性能肯定是优于zuul。  

Spring Cloud Gateway 逐渐崭露头角，它基于 Spring 5.0、Spring Boot 2.0 和 Project Reactor 等技术开发，不仅支持响应式和无阻塞式的 API，而且支持 WebSocket，和 Spring 框架紧密集成  


&emsp; Spring Cloud Gateway 具有如下特性：

* 基于Spring Framework 5、Project Reactor 和 Spring Boot 2.0 进行构建；
* 动态路由：能够匹配任何请求属性；
* 可以对路由指定 Predicate（断言）和 Filter（过滤器）；
* 集成Hystrix的断路器功能；
* 集成 Spring Cloud 服务发现功能；
* 易于编写的 Predicate（断言）和 Filter（过滤器）；
* 请求限流功能；
* 支持路径重写。  



------------
1. Spring Cloud Gateway几个必知的术语？  
&emsp; 路由（route）：gateway的基本构建模块。它由ID、目标URI、断言集合和过滤器集合组成。如果聚合断言结果为真，则匹配到该路由。  
&emsp; 断言（Predicate ）：参照Java8的新特性Predicate，允许开发人员匹配HTTP请求中的任何内容，比如头或参数。  
&emsp; 过滤器（filter）：可以在返回请求之前或之后修改请求和响应的内容。  


## 1.2. 路由  



## 1.3. 断言  


## 1.4. 过滤器
### 1.4.1. 过滤器  
&emsp; 过滤器这个概念很熟悉，在Spring mvc 就接触过，Gateway的过滤器的作用以及生命周期都是类似的。  

&emsp; Gateway的生命周期：

* PRE：这种过滤器在请求被路由之前调用。我们可利用这种过滤器实现身份验证、在集群中选择 请求的微服务、记录调试信息等。  
* POST：这种过滤器在路由到微服务以后执行。这种过滤器可用来为响应添加标准的HTTP Header、收集统计信息和指标、将响应从微服务发送给客户端等。  

&emsp; Gateway 的Filter从作用范围可分为两种:

* GatewayFilter：应用到单个路由或者一个分组的路由上（需要在配置文件中配置）。  
* GlobalFilter：应用到所有的路由上（无需配置，全局生效）  



### 1.4.2. GatewayFilter（局部过滤器）
Spring Cloud Gateway中内置了许多的局部过滤器，如下图：  


### 1.4.3. GlobalFilter（全局过滤器）  



### 1.4.4. 自定义局部过滤器  
<!-- 
Gateway：自定义过滤器 
https://mp.weixin.qq.com/s/1QQYCM4dTjzbkF9FJjB5qw

-->

## 1.5. 集成注册中心  



### 1.5.1. 实现动态路由？  
<!-- 

https://mp.weixin.qq.com/s/bFEIYPZOhisg8skjMDvdJw
-->


## 1.6. 自定义全局异常处理