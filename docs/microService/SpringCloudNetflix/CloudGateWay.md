
<!-- TOC -->

- [1. GateWay](#1-gateway)
    - [1.1. 路由](#11-路由)
    - [1.2. 断言](#12-断言)
    - [1.3. 过滤器](#13-过滤器)
        - [1.3.1. 过滤器](#131-过滤器)
        - [1.3.2. GatewayFilter（局部过滤器）](#132-gatewayfilter局部过滤器)
        - [1.3.3. GlobalFilter（全局过滤器）](#133-globalfilter全局过滤器)
        - [1.3.4. 自定义局部过滤器](#134-自定义局部过滤器)
    - [1.4. 集成注册中心](#14-集成注册中心)
        - [1.4.1. 实现动态路由？](#141-实现动态路由)
    - [1.5. 自定义全局异常处理](#15-自定义全局异常处理)

<!-- /TOC -->


# 1. GateWay
<!-- 


*** Spring Cloud Gateway夺命连环10问？
https://mp.weixin.qq.com/s/YdMQTVH8vqKnWXyRXxTmag


 这样讲 API 网关，你应该能明白了吧！ 
 https://mp.weixin.qq.com/s/IpUlh54BkvfTkrjf20mE9Q
 
 这可能是全网Spring Cloud Gateway限流最完整的方案了！ 
 https://mp.weixin.qq.com/s/fBmHhX3TomnFcIgeBDPeGw

-->

<!-- 

网关Spring Cloud Gateway科普 
https://mp.weixin.qq.com/s/NIc6bredbMF2jpabagv2lg
-->


&emsp; Spring Cloud Gateway 具有如下特性：

* 基于Spring Framework 5、Project Reactor 和 Spring Boot 2.0 进行构建；
* 动态路由：能够匹配任何请求属性；
* 可以对路由指定 Predicate（断言）和 Filter（过滤器）；
* 集成Hystrix的断路器功能；
* 集成 Spring Cloud 服务发现功能；
* 易于编写的 Predicate（断言）和 Filter（过滤器）；
* 请求限流功能；
* 支持路径重写。  

-----------

统一入口

    所有请求通过网关路由到内部其他服务。

断言(Predicates)和过滤器(filters)特定路由。

    断言是根据具体的请求的规则由route去处理；

    过滤器用来对请求做各种判断和修改。

Hystrix 熔断机制。

    Hystrix是 spring cloud gateway中是以filter的形式使用的。

请求限流

    防止大规模请求对业务数据造成破坏。

路径重写

    自定义路由转发规则。

------------
Spring Cloud Gateway几个必知的术语？  
路由（route）：gateway的基本构建模块。它由ID、目标URI、断言集合和过滤器集合组成。如果聚合断言结果为真，则匹配到该路由。  
断言（Predicate ）：参照Java8的新特性Predicate，允许开发人员匹配HTTP请求中的任何内容，比如头或参数。  
过滤器（filter）：可以在返回请求之前或之后修改请求和响应的内容。  


## 1.1. 路由  



## 1.2. 断言  


## 1.3. 过滤器
### 1.3.1. 过滤器  
&emsp; 过滤器这个概念很熟悉，在Spring mvc 就接触过，Gateway的过滤器的作用以及生命周期都是类似的。  

&emsp; Gateway的生命周期：

* PRE：这种过滤器在请求被路由之前调用。我们可利用这种过滤器实现身份验证、在集群中选择 请求的微服务、记录调试信息等。  
* POST：这种过滤器在路由到微服务以后执行。这种过滤器可用来为响应添加标准的HTTP Header、收集统计信息和指标、将响应从微服务发送给客户端等。  

&emsp; Gateway 的Filter从作用范围可分为两种:

* GatewayFilter：应用到单个路由或者一个分组的路由上（需要在配置文件中配置）。  
* GlobalFilter：应用到所有的路由上（无需配置，全局生效）  



### 1.3.2. GatewayFilter（局部过滤器）
Spring Cloud Gateway中内置了许多的局部过滤器，如下图：  


### 1.3.3. GlobalFilter（全局过滤器）  



### 1.3.4. 自定义局部过滤器  
<!-- 
Gateway：自定义过滤器 
https://mp.weixin.qq.com/s/1QQYCM4dTjzbkF9FJjB5qw

-->

## 1.4. 集成注册中心  



### 1.4.1. 实现动态路由？  
<!-- 

https://mp.weixin.qq.com/s/bFEIYPZOhisg8skjMDvdJw
-->


## 1.5. 自定义全局异常处理