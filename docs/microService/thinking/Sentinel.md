
<!-- TOC -->

- [1. Sentinel](#1-sentinel)
    - [1.1. 接入Sentinel](#11-接入sentinel)
    - [gateway集成sentinel](#gateway集成sentinel)
    - [1.2. Sentinel控制台](#12-sentinel控制台)
        - [1.2.1. API 分组和route维度](#121-api-分组和route维度)
        - [1.2.2. 网关限流规则 GatewayFlowRule](#122-网关限流规则-gatewayflowrule)
        - [1.2.3. 熔断降级](#123-熔断降级)

<!-- /TOC -->

# 1. Sentinel
<!-- 


什么是Sentinel?它能做什么
https://blog.csdn.net/u012190514/article/details/81383698
很好？Sentinel 夺命连环 17 问
https://mp.weixin.qq.com/s/JBX3M-LrNwCoGl4Xzcg18Q


https://blog.csdn.net/lzq199528/article/details/109898038


1. 搭建  
&emsp; https://sentinelguard.io/zh-cn/docs/dashboard.html    
2. 集成  
    1. 官方使用文档：https://sentinelguard.io/zh-cn/docs/basic-api-resource-rule.html  
    2. 普通接口和feign集成Sentinel  
    &emsp; https://www.jb51.net/article/226839.htm  
    &emsp; https://www.jianshu.com/p/f5cabdef0de1  
    &emsp; ~~https://blog.csdn.net/MenBad/article/details/125118367~~  
    3. ~~dubbo之使用sentinel限流~~  
    &emsp; https://blog.csdn.net/wang0907/article/details/121356872  
    4. gateway集成Sentinel https://blog.51cto.com/u_15284359/4874743  
    5. dubbo之使用sentinel限流  https://blog.csdn.net/wang0907/article/details/121356872  

-->

## 1.1. 接入Sentinel
<!-- 
全局配置
异常处理 https://mp.weixin.qq.com/s?__biz=MzkwNzI0MzQ2NQ==&mid=2247489058&idx=3&sn=2a9abd84a257e49869689079bccfa733&source=41#wechat_redirect
-->
&emsp; 查看[官方文档](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)  

## gateway集成sentinel
<!--

https://blog.csdn.net/lzq199528/article/details/109898038

-->


## 1.2. Sentinel控制台
<!-- 
https://mp.weixin.qq.com/s/YRfDFeIcoFlIl5kE7A9Y0Q
-->
### 1.2.1. API 分组和route维度
![image](http://182.92.69.8:8081/img/microService/problems/problem-57.png)  


### 1.2.2. 网关限流规则 GatewayFlowRule
&emsp; **<font color = "clime">阈值类型：qps和线程数</font>**  
![image](http://182.92.69.8:8081/img/microService/problems/problem-58.png)  

![image](http://182.92.69.8:8081/img/microService/problems/problem-59.png)  



### 1.2.3. 熔断降级

