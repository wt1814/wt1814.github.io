
<!-- TOC -->

- [1. nacos](#1-nacos)
    - [1.1. 宕机](#11-宕机)
        - [1.1.1. 注册中心集群全部宕机后正确的处理方式](#111-注册中心集群全部宕机后正确的处理方式)
        - [1.1.2. Nacos本地缓存配置](#112-nacos本地缓存配置)

<!-- /TOC -->

# 1. nacos  
<!-- 

https://github.com/alibaba/nacos
*** https://nacos.io/zh-cn/docs/what-is-nacos.html


修改nacos启动时的占用内存
https://blog.csdn.net/weixin_48016395/article/details/124239230

-->

&emsp; nacos  

应用内/外：属于外部应用，侵入性小  
ACP原则：通知遵循CP原则（一致性+分离容忍） 和AP原则（可用性+分离容忍）  
版本迭代：目前仍然进行版本迭代  
集成支持：支持Dubbo 、SpringCloud、K8S集成  
访问协议：HTTP/动态DNS/UDP  
雪崩保护：支持雪崩保护  
界面：中文界面，符合国人习惯  
上手：极易，中文文档，案例，社区活跃  



## 1.1. 宕机 


### 1.1.1. 注册中心集群全部宕机后正确的处理方式
<!-- 
注册中心集群全部宕机后正确的处理方式
https://blog.csdn.net/qq_46514118/article/details/121471593
-->


### 1.1.2. Nacos本地缓存配置
<!-- 
https://zhuanlan.zhihu.com/p/659882213
-->