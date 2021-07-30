
<!-- TOC -->

- [1. ~~Dubbo协议长连接心跳~~](#1-dubbo协议长连接心跳)
    - [1.1. 前言：长连接](#11-前言长连接)
    - [1.2. Dubbo协议长连接](#12-dubbo协议长连接)
    - [1.3. Dubbo心跳机制](#13-dubbo心跳机制)

<!-- /TOC -->

# 1. ~~Dubbo协议长连接心跳~~  
<!-- 


dubbo是长连接还是短连接_聊聊 TCP 长连接和心跳那些事
https://blog.csdn.net/weixin_39850699/article/details/109903631

rpc、dubbo和http的区别
https://blog.csdn.net/qq_27184497/article/details/117265654


https://blog.csdn.net/weixin_39850699/article/details/109903631
https://blog.csdn.net/YAOQINGGG/article/details/91649589

dubbo心跳检测机制
https://blog.csdn.net/forget_me_not1991/article/details/80676181?utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromMachineLearnPai2%7Edefault-1.baidujs&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromMachineLearnPai2%7Edefault-1.baidujs
-->

&emsp; Dubbo缺省协议采用单一长连接和NIO异步通讯，适合于小数据量大并发的服务调用，以及服务消费者机器数远大于服务提供者机器数的情况。  
&emsp; 注意：Dubbo缺省协议不适合传送大数据量的服务，比如传文件，传视频等，除非请求量很低。  

## 1.1. 前言：长连接  
&emsp; 参考[长/短连接](/docs/network/connection.md)  

## 1.2. Dubbo协议长连接
&emsp; Dubbo协议采用长连接，还可以防止注册中心宕机风险。  

## 1.3. Dubbo心跳机制  

