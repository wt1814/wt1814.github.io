


# RestTemplate自动重试  
<!-- 
https://www.hangge.com/blog/cache/detail_2522.html

-->


# Spring Cloud Ribbon  
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


