
<!-- TOC -->

- [1. 重试请求](#1-重试请求)
    - [1.1. Spring Retry](#11-spring-retry)
    - [1.2. RestTemplate自动重试](#12-resttemplate自动重试)
    - [1.3. Spring Cloud Ribbon](#13-spring-cloud-ribbon)

<!-- /TOC -->


# 1. 重试请求
<!-- 

Spring-Retry重试实现原理 
https://mp.weixin.qq.com/s/_qFK-ki7-mL4Nnv4zEfYrQ
https://mp.weixin.qq.com/s/Tc7USvs_xZ6jPyyeR2jx1Q
https://mp.weixin.qq.com/s/_Np5AX5CFU3eUhIHt9FVdQ

-->

## 1.1. Spring Retry

## 1.2. RestTemplate自动重试  
<!-- 
https://www.hangge.com/blog/cache/detail_2522.html
-->


## 1.3. Spring Cloud Ribbon  
&emsp; Spring Cloud整合Spring Retry来增强RestTemplate的重试能力。通过RestTemplate实现的服务访问就会自动根据配置来实现重试机制。  

```yaml
spring:
  cloud:
    loadbalancer:
      retry:
        enabled: true #开启重试机制
#ribbon配置,key-value配置类:CommonClientConfigKey
#服务名
eureka-provider:
  ribbon:
    ConnectTimeout: 250 #单位ms,请求连接超时时间
    ReadTimeout: 1000 #单位ms,请求处理的超时时间
    OkToRetryOnAllOperations: true #对所有操作请求都进行重试
    MaxAutoRetriesNextServer: 2 #切换实例的重试次数
    MaxAutoRetries: 1 #对当前实例的重试次数
```


