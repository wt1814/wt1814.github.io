

&emsp; **<font color = "red">总结：</font>**  
1. **SpringCloud子项目：**  
![image](http://182.92.69.8:8081/img/microService/SpringCloudNetflix/cloud-27.png)  
&emsp; Spring Cloud是一个微服务框架，相比Dubbo等RPC框架，Spring Cloud提供全套的分布式系统解决方案。Spring Cloud为微服务架构开发涉及的配置管理，服务治理，熔断机制，智能路由，微代理，控制总线，一次性token，全局一致性锁，leader选举，分布式session，集群状态管理等操作提供了一种简单的开发方式。   
&emsp; Spring Cloud对微服务基础框架Netflix的多个开源组件进行了封装，同时又实现了和云端平台以及和Spring Boot开发框架的集成。   

2. ★★★各组件作用  
    1. 注册中心，解决了服务之间的自动发现。  
    &emsp; 在没有注册中心时候，服务间调用需要知道被调方的地址或者代理地址。当服务更换部署地址，就不得不修改调用当中指定的地址或者修改代理配置。  
    &emsp; 而有了注册中心之后，每个服务在调用别人的时候只需要知道服务名称就好，继续地址都会通过注册中心同步过来。    
    2. 配置中心，每个服务都需要必要的配置信息才能运行，所以一套集中式的、动态的配置管理设施是必不可少的。  
    2. 服务分发(Ribbon)：客户端负载均衡，Ribbon中提供了多种负载均衡策略：轮询、随机、重试、权重等。  
    3. 声明式调用(OpenFeign)
    3. 服务熔断(Hystrix)，解决服务雪崩问题。    
    4. 链路监控(Sleuth)：如何快速定位请求异常；如何快速定位性能瓶颈；如何快速定位不合理调用。  
    5. 服务网关

3. ★★★**<font color = "clime">Spring Cloud各组件运行流程：</font>**  
    1. 外部或者内部的非Spring Cloud项目都统一通过微服务网关(Zuul)来访问内部服务。客户端的请求首先经过负载均衡(Ngnix)，再到达服务网关(Zuul集群)；  
    2. 网关接收到请求后，从注册中心(Eureka)获取可用服务；  
    3. 由Ribbon进行负载均衡后，分发到后端的具体实例；  
    4. 微服务之间也可通过Feign进行通信处理业务；  
    5. Hystrix负责处理服务超时熔断；Hystrix dashboard，Turbine负责监控Hystrix的熔断情况，并给予图形化的展示；  
    6. 服务的所有的配置文件由配置服务管理，配置服务的配置文件放在git仓库，方便开发人员随时改配置。  


[Spring Cloud Eureka](/docs/microService/SpringCloudNetflix/Eureka.md)  
[Spring Cloud Ribbon](/docs/microService/SpringCloudNetflix/Ribbon.md)  
[Spring Cloud Hytrix](/docs/microService/SpringCloudNetflix/Hytrix.md)  
[Spring Cloud Feign](/docs/microService/SpringCloudNetflix/Feign.md)  
[Gateway](/docs/microService/microservices/Gateway.md)  
[Spring Cloud Zuul](/docs/microService/SpringCloudNetflix/Zuul.md)  
[Spring Cloud Sleuth](/docs/microService/SpringCloudNetflix/Sleuth.md)  
&emsp; [SpringMVC、dubbo集成zipkin](/docs/microService/SpringCloudNetflix/zipkin.md)  
[Spring Cloud Admin](/docs/microService/SpringCloudNetflix/SpringBootAdmin.md)  
<!-- 
[Spring Cloud Config]  
[Spring Cloud Bus]  
[Spring Cloud Security]  
-->

<!-- 
Spring CLoud系列
https://mp.weixin.qq.com/mp/appmsgalbum?__biz=MzkwMzE3MDY0Ng==&action=getalbum&album_id=1571227588654645250&scene=173&from_msgid=2247486840&from_itemidx=1&count=3#wechat_redirect

 一文搞懂火遍大厂的ServiceMesh模式 
 https://mp.weixin.qq.com/s/P4SNEVYV1mZQ3qHQqjDF3g


-->
