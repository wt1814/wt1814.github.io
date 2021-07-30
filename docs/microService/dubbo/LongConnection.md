
<!-- TOC -->

- [1. Dubbo协议长连接心跳](#1-dubbo协议长连接心跳)
    - [1.1. 前言：长连接](#11-前言长连接)
    - [1.2. dubbo协议与http协议比较](#12-dubbo协议与http协议比较)
    - [1.3. Dubbo心跳检测机制](#13-dubbo心跳检测机制)

<!-- /TOC -->

# 1. Dubbo协议长连接心跳  
&emsp; Dubbo缺省协议采用单一长连接和NIO异步通讯，适合于小数据量大并发的服务调用，以及服务消费者机器数远大于服务提供者机器数的情况。  
&emsp; 注意：Dubbo缺省协议不适合传送大数据量的服务，比如传文件，传视频等，除非请求量很低。  
&emsp; Dubbo协议采用长连接，还可以防止注册中心宕机风险。  

## 1.1. 前言：长连接  
&emsp; 参考[长短连接](/docs/network/connection.md)  

## 1.2. dubbo协议与http协议比较
<!-- 
rpc、dubbo和http的区别
https://blog.csdn.net/qq_27184497/article/details/117265654

-->
&emsp; dubbo协议传输层还是TCP。所以Dubbo协议与HTTP、FTP，SMTP这些应用层协议是并列的概念。除了默认的Dubbo协议，Dubbo框架还支持RMI、Hessian、HTTP等协议。  



## 1.3. Dubbo心跳检测机制  
<!--
dubbo心跳检测机制
https://blog.csdn.net/forget_me_not1991/article/details/80676181?utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromMachineLearnPai2%7Edefault-1.baidujs&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromMachineLearnPai2%7Edefault-1.baidujs

https://blog.csdn.net/YAOQINGGG/article/details/91649589

https://blog.csdn.net/weixin_39850699/article/details/109903631
-->

&emsp; 目的：维持provider和consumer之间的长连接
&emsp; 实现：dubbo心跳时间heartbeat默认是60s，超过heartbeat时间没有收到消息，就发送心跳消息(provider，consumer一样)，如果连着3次(heartbeatTimeout为heartbeat*3)没有收到心跳响应，provider会关闭channel，而consumer会进行重连；不论是provider还是consumer的心跳检测都是通过启动定时任务的方式实现。    
