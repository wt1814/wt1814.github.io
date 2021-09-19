
<!-- TOC -->

- [1. Dubbo和Spring Cloud](#1-dubbo和spring-cloud)
    - [1.1. Dubbo和Spring Cloud](#11-dubbo和spring-cloud)
    - [1.2. Dubbo生态](#12-dubbo生态)
        - [1.2.1. Dubbo与分布式事务](#121-dubbo与分布式事务)

<!-- /TOC -->

# 1. Dubbo和Spring Cloud
## 1.1. Dubbo和Spring Cloud  
&emsp; Dubbo是SOA时代的产物，它的关注点主要在于服务的调用，流量分发、流量监控和熔断。  
&emsp; Spring Cloud诞生于微服务架构时代，考虑的是微服务治理的方方面面，另外由于依托了Spirng、Spirng Boot的优势之上。  

* 两个框架在开始目标就不一致：<font color = "red">Dubbo定位服务治理；Spirng Cloud是一个生态。</font>  
* <font color = "red">Dubbo底层是使用Netty这样的NIO框架，是基于TCP协议传输的，配合以Hession序列化完成RPC通信。</font><font color = "clime">而SpringCloud是基于Http协议+Rest接口调用远程过程的通信，</font>相对来说，Http请求会有更大的报文，占的带宽也会更多。但是REST相比RPC更为灵活，服务提供方和调用方的依赖只依靠一纸契约，不存在代码级别的强依赖，这在强调快速演化的微服务环境下，显得更为合适，至于注重通信速度还是方便灵活性，具体情况具体考虑。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-14.png)  

## 1.2. Dubbo生态
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-65.png)   

### 1.2.1. Dubbo与分布式事务  
&emsp; Dubbo支持分布式事务吗？   
&emsp; 目前暂时不支持，可与通过tcc-transaction框架实现。TCC-Transaction通过Dubbo隐式传参的功能，避免自己对业务代码的入侵。 
