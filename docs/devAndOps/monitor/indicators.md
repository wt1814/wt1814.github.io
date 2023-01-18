
<!-- TOC -->

- [1. 如何快速深入理解监控知识？](#1-如何快速深入理解监控知识)
    - [1.1. Linux监控介绍](#11-linux监控介绍)
        - [1.1.1. 监控方法](#111-监控方法)
        - [1.1.2. 监控核心](#112-监控核心)
        - [1.1.3. 监控工具](#113-监控工具)
            - [1.1.3.1. 王牌监控](#1131-王牌监控)
    - [1.2. 监控系统](#12-监控系统)
    - [1.3. 监控报警及处理](#13-监控报警及处理)

<!-- /TOC -->

# 1. 如何快速深入理解监控知识？ 
<!-- 

大型分布式网站架构设计与实践 第4.2章  

如何快速深入理解监控知识？
https://mp.weixin.qq.com/s/q4QxJi5KZKNNIvWMjfdM1A

Linux 常用监控指标总结 
https://forum.huawei.com/enterprise/zh/thread-663521.html

全面解析微服务系统监控分层，啃透服务治理核心！
https://mp.weixin.qq.com/s/5xCL7KkMpnsfB6ivG4-0MQ


某生鲜电商平台的监控模块设计
https://mp.weixin.qq.com/s/m9tTCrOYrbuMbsiGpzsJHw

想监控主机性能的话，个人建议这本《SystemsPerformance》就足够了。

Java业务监控中间件_不得不知道的25个中间件监控指标
https://blog.csdn.net/weixin_35172715/article/details/114824837

-->


<!-- 

SkyWalking
https://mp.weixin.qq.com/s/Z7dRtmj2T7F09Q8etoK3hg
用了3年CAT，这次我想选择SkyWalking，老板反手就是一个赞！ 
https://mp.weixin.qq.com/s/foYoz8qjalO0AhlBcM0BpA
-->


## 1.1. Linux监控介绍 
### 1.1.1. 监控方法  


### 1.1.2. 监控核心

### 1.1.3. 监控工具

#### 1.1.3.1. 王牌监控
&emsp; Zabbix是一个分布式监控系统，支持多种采集方式和采集客户端，有专用的Agent代理，也支持SNMP、IPMI、JMX、Telnet、SSH等多种协议，它将采集到的数据存放到数据库，然后对其进行分析整理，达到条件触发告警。其灵活的扩展性和丰富的功能是其他监控系统所不能比的。相对来说，它的总体功能做的非常优秀。从以上各种监控系统的对比来看，Zabbix都是具有优势的，其丰富的功能、可扩展的能力、二次开发的能力和简单易用的特点，读者只要稍加学习，即可构建自己的监控系统。  

&emsp; prometheus  
&emsp; Prometheus 是一套开源的系统监控报警框架。它启发于Google的borgmon 监控系统，由工作在SoundCloud 的 google 前员工在 2012 年创建，作为社区开源项目进行开发，并于 2015 年正式发布。Prometheus是最近几年开始流行的一个新兴监控告警工具，特别是kubernetes的流行带动了prometheus的应用。  

&emsp; 小米的监控系统：open-falcon。open-falcon的目标是做最开放、最好用的互联网企业级监控产品。  

## 1.2. 监控系统
&emsp; [监控系统](/docs/devAndOps/monitor/monitor.md)  

## 1.3. 监控报警及处理    
&emsp; 故障报警通知的方式有很多种，当然我们最常用的还是短信，邮件  
![image](http://182.92.69.8:8081/img/monitor/monitor-8.png)  

&emsp; **报警处理：**  
&emsp; 一般报警后我们故障如何处理，首先，我们可以通过告警升级机制先自动处理，比如nginx服务down了，可以设置告警升级自动启动nginx。  
&emsp; 但是如果一般业务出现了严重故障，我们通常根据故障的级别，故障的业务，来指派不同的运维人员进行处理。  
&emsp; 当然不同业务形态、不同架构、不同服务可能采用的方式都不同，这个没有一个固定的模式套用。  
![image](http://182.92.69.8:8081/img/monitor/monitor-9.png)  

