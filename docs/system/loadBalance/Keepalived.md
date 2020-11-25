
# Keepalived  

<!-- 
http://www.yunweipai.com/35361.html

https://baike.baidu.com/item/Keepalived/10346758?fr=aladdin

https://mp.weixin.qq.com/s/TfXjON0mwfJzO9gGXws1Lg
https://mp.weixin.qq.com/s/Y843YN5apWF27GxYIGYqmQ

-->

## 介绍  
&emsp; Keepalived起初是为LVS设计的，专门用来监控集群系统中各个服务节点的状态，它根据TCP/IP参考模型的第三、第四层、第五层交换机制检测每个服务节点的状态，如果某个服务器节点出现异常，或者工作出现故障，Keepalived将检测到，并将出现的故障的服务器节点从集群系统中剔除，这些工作全部是自动完成的，不需要人工干涉，需要人工完成的只是修复出现故障的服务节点。  
&emsp; 后来Keepalived又加入了VRRP的功能，VRRP（VritrualRouterRedundancyProtocol,虚拟路由冗余协议)出现的目的是解决静态路由出现的单点故障问题，通过VRRP可以实现网络不间断稳定运行，因此Keepalvied一方面具有服务器状态检测和故障隔离功能，另外一方面也有HAcluster功能。  
&emsp; 健康检查和失败切换是keepalived的两大核心功能。所谓的健康检查，就是采用tcp三次握手，icmp请求，http请求，udp echo请求等方式对负载均衡器后面的实际的服务器(通常是承载真实业务的服务器)进行保活；而失败切换主要是应用于配置了主备模式的负载均衡器，利用VRRP维持主备负载均衡器的心跳，当主负载均衡器出现问题时，由备负载均衡器承载对应的业务，从而在最大限度上减少流量损失，并提供服务的稳定性。  

## VRRP  
https://blog.csdn.net/qq_24336773/article/details/82143367?utm_medium=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromBaidu-1.not_use_machine_learn_pai&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromBaidu-1.not_use_machine_learn_pai
https://mp.weixin.qq.com/s/TfXjON0mwfJzO9gGXws1Lg
&emsp; 在现实的网络环境中。主机之间的通信都是通过配置静态路由或者(默认网关)来完成的，而主机之间的路由器一旦发生故障，通信就会失效，因此这种通信模式当中，路由器就成了一个单点瓶颈，为了解决这个问题，就引入了VRRP协议。  
&emsp; VRRP协议是一种容错的主备模式的协议，保证当主机的下一跳路由出现故障时，由另一台路由器来代替出现故障的路由器进行工作，通过VRRP可以在网络发生故障时透明的进行设备切换而不影响主机之间的数据通信。   

## Keepalived原理  
https://baike.baidu.com/item/Keepalived/10346758?fr=aladdin
https://blog.csdn.net/qq_24336773/article/details/82143367?utm_medium=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromBaidu-1.not_use_machine_learn_pai&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromBaidu-1.not_use_machine_learn_pai



## 配置文件详解  
https://www.jianshu.com/p/a6b5ab36292a
https://blog.csdn.net/benpaobagzb/article/details/48062891


## 脑裂  
https://www.jianshu.com/p/a6b5ab36292a


