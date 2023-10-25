

&emsp; [Spring Cloud Eureka](/docs/microService/SpringCloudNetflix/Eureka.md)  
&emsp; [nacos](/docs/microService/SpringCloudNetflix/nacos.md)  

<!-- 
https://blog.csdn.net/wr_java/article/details/119977368
-->


配置中心  
由于每个服务都需要必要的配置信息才能运行，所以一套集中式的、动态的配置管理设施是必不可少的。  

注册中心
解决了服务之间的自动发现。  
在没有注册中心时候，服务间调用需要知道被调方的地址或者代理地址。当服务更换部署地址，就不得不修改调用当中指定的地址或者修改代理配置。  
而有了注册中心之后，每个服务在调用别人的时候只需要知道服务名称就好，继续地址都会通过注册中心同步过来。  



