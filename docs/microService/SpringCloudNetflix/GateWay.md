

&emsp; [Spring Cloud GateWay](/docs/microService/SpringCloudNetflix/CloudGateWay.md)  
&emsp; [Spring Cloud Zuul](/docs/microService/SpringCloudNetflix/Zuul.md)  

https://mp.weixin.qq.com/s/IpUlh54BkvfTkrjf20mE9Q  

网关的基本功能？https://mp.weixin.qq.com/s/YdMQTVH8vqKnWXyRXxTmag  

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

