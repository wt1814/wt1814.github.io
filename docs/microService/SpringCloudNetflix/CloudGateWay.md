
<!-- TOC -->

- [1. Spring Cloud Gateway](#1-spring-cloud-gateway)
    - [1.1. Gateway简介（Gateway的特性/Gateway VS zuul）](#11-gateway简介gateway的特性gateway-vs-zuul)
    - [1.2. 路由](#12-路由)
        - [1.2.1. ★★★路由实现](#121-★★★路由实现)
            - [1.2.1.1. 配置实践](#1211-配置实践)
            - [1.2.1.2. 路由规则](#1212-路由规则)
        - [1.2.2. 集成注册中心实现动态路由](#122-集成注册中心实现动态路由)
    - [1.3. 断言](#13-断言)
    - [1.4. 过滤器](#14-过滤器)
        - [1.4.1. 过滤器](#141-过滤器)
        - [1.4.2. GatewayFilter（局部过滤器）](#142-gatewayfilter局部过滤器)
        - [1.4.3. GlobalFilter（全局过滤器）](#143-globalfilter全局过滤器)
        - [1.4.4. 自定义局部过滤器](#144-自定义局部过滤器)
    - [1.5. 自定义全局异常处理](#15-自定义全局异常处理)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. Gateway的特性/Gateway VS zuul  



# 1. Spring Cloud Gateway
<!-- 
*** Spring Cloud Gateway夺命连环10问？
https://mp.weixin.qq.com/s/YdMQTVH8vqKnWXyRXxTmag
网关Spring Cloud Gateway科普 
https://mp.weixin.qq.com/s/NIc6bredbMF2jpabagv2lg


https://baijiahao.baidu.com/s?id=1698546896997623218&wfr=spider&for=pc
-->


## 1.1. Gateway简介（Gateway的特性/Gateway VS zuul）  

1. 为什么选择Spring cloud Gateway？  
在1.x版本中都是采用的Zuul网关；但在2.x版本中，zuul的升级一直跳票，Spring Cloud最后自己研发了一个网关替代Zuul，那就是Spring Cloud Gateway。  
它的很多思想都是借鉴zuul，所谓青出于蓝而胜于蓝，功能和性能肯定是优于zuul。  

Spring Cloud Gateway 逐渐崭露头角，它基于 Spring 5.0、Spring Boot 2.0 和 Project Reactor 等技术开发，不仅支持响应式和无阻塞式的 API，而且支持 WebSocket，和 Spring 框架紧密集成  


&emsp; Spring Cloud Gateway 具有如下特性：

* 基于Spring Framework 5、Project Reactor 和 Spring Boot 2.0 进行构建；
* 动态路由：能够匹配任何请求属性；  
* 集成 Spring Cloud 服务发现功能；
* 可以对路由指定 Predicate（断言）和 Filter（过滤器）；
* 易于编写的 Predicate（断言）和 Filter（过滤器）；
* 集成Hystrix的断路器功能；
* 请求限流功能；
* 支持路径重写。  



------------
1. Spring Cloud Gateway重要概念？  
&emsp; 路由（route）：gateway的基本构建模块。它由ID、目标URI、断言集合和过滤器集合组成。如果聚合断言结果为真，则匹配到该路由。  
&emsp; 断言（Predicate ）：参照Java8的新特性Predicate，允许开发人员匹配HTTP请求中的任何内容，比如头或参数。  
&emsp; 过滤器（filter）：可以在返回请求之前或之后修改请求和响应的内容。  


## 1.2. 路由  

### 1.2.1. ★★★路由实现  
#### 1.2.1.1. 配置实践  
1. pom配置依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

2. 配置路由  
```text
spring:
 cloud:
  gateway:
   routes:
   - id: product-service
     uri:http://127.0.0.1:9002
     predicates:
     - Path=/product/**
```

&emsp; 1）id：我们自定义的路由 ID，保持唯一  
&emsp; 2）uri：目标服务地址  
&emsp; 3）predicates：路由条件，Predicate 接受一个输入参数，返回一个布尔值结果。该接口包含多种默认方法来将 Predicate 组合成其他复杂的逻辑（比如：与，或，非）。  
&emsp; 4）filters：过滤规则，暂时没用。  
&emsp; 上面这段配置的意思是，配置了一个id为product-service的路由规则，当访问网关请求地址以product开头时，会自动转发到地址：http://127.0.0.1:9002/。  


#### 1.2.1.2. 路由规则  
&emsp; SpringCloudGateway的功能很强大，前面我们只是使用了predicates进行了简单的条件匹配，其实SpringCloudGataway帮我们内置了很多Predicates功能。在SpringCloudGateway中Spring利用Predicate的特性实现了各种路由匹配规则，有通过Header、请求参数等不同的条件来进行作为条件匹配到对应的路由。  

![image](http://182.92.69.8:8081/img/microService/SpringCloudNetflix/cloud-46.png)  

### 1.2.2. 集成注册中心实现动态路由  
<!-- 
https://baijiahao.baidu.com/s?id=1698546896997623218&wfr=spider&for=pc
https://mp.weixin.qq.com/s/bFEIYPZOhisg8skjMDvdJw
-->
&emsp; 动态路由：是与静态路由相对的一个概念，指路由器能够根据路由器之间的交换的特定路由信息自动地建立自己的路由表，并且能够根据链路和节点的变化适时地进行自动调整。当网络中节点或节点间的链路发生故障，或存在其它可用路由时，动态路由可以自行选择最佳的可用路由并继续转发报文  


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


## 1.5. 自定义全局异常处理


