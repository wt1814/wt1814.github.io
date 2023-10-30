

<!-- TOC -->

- [1. 网关](#1-网关)
    - [1.1. 为什么需要网关？](#11-为什么需要网关)
    - [1.2. 网关的基本功能](#12-网关的基本功能)
    - [1.3. GateWay与Zuul](#13-gateway与zuul)

<!-- /TOC -->



<!-- 
https://mp.weixin.qq.com/s/IpUlh54BkvfTkrjf20mE9Q  

****网关的基本功能？https://mp.weixin.qq.com/s/YdMQTVH8vqKnWXyRXxTmag  
-->
***

# 1. 网关

## 1.1. 为什么需要网关？  
&emsp; 传统的单体架构中只有一个服务开放给客户端调用，但是微服务架构中是将一个系统拆分成多个微服务，那么作为客户端如何去调用这些微服务呢？如果没有网关的存在，只能在本地记录每个微服务的调用地址。  
&emsp; 无网关的微服务架构往往存在以下问题：  
* 客户端多次请求不同的微服务，增加客户端代码或配置编写的复杂性。
* 认证复杂，每个服务都需要独立认证。
* 存在跨域请求，在一定场景下处理相对复杂。

## 1.2. 网关的基本功能  
&emsp; 网关是所有微服务的门户，路由转发仅仅是最基本的功能，除此之外还有其他的一些功能，比如：认证、鉴权、熔断、限流、日志监控等等.........  

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

## 1.3. GateWay与Zuul
&emsp; [Spring Cloud GateWay](/docs/microService/SpringCloudNetflix/CloudGateWay.md)  
&emsp; [Spring Cloud Zuul](/docs/microService/SpringCloudNetflix/Zuul.md)  

