
<!-- TOC -->

- [1. netty核心](#1-netty核心)
    - [1.1. 种I/O模型](#11-种io模型)
    - [1.2. Netty核心篇](#12-netty核心篇)
        - [1.2.1. Netty高性能](#121-netty高性能)
            - [线程模型](#线程模型)
            - [零拷贝](#零拷贝)

<!-- /TOC -->



# 1. netty核心  
<!-- 


-->

## 1.1. 种I/O模型  

<!-- 
四图，读懂 BIO、NIO、AIO、多路复用 IO 的区别 
https://mp.weixin.qq.com/s/CRd3-vRD7xwoexqv7xyHRw
-->

## 1.2. Netty核心篇  

### 1.2.1. Netty高性能  

* 异步非阻塞通信  
* 零拷贝  
* 内存池
* 高效的Reactor线程模型  
* 无锁化的串行设计理念  
* 高效的并发编程  
* 对高性能对的序列化框架支持
* 灵活的TCP参数配置能力


#### 零拷贝  
<!-- 

原来 8 张图，就可以搞懂「零拷贝」了
https://mp.weixin.qq.com/s/P0IP6c_qFhuebwdwD8HM7w
-->


#### 线程模型  

<!--
Netty系列文章之Netty线程模型
https://juejin.im/post/5dac6ef75188252bc1657ead

说说Netty的线程模型 
https://mp.weixin.qq.com/s?__biz=MzAxNjM2MTk0Ng==&mid=2247488256&idx=3&sn=253eb6ba1f500d545bd8c836adaf1980&chksm=9bf4a3b5ac832aa3bb05595fac709334dd318698e577fa00b16d696a0fe235d3dee24cee3c75&mpshare=1&scene=1&srcid=&sharer_sharetime=1566173423019&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=5ead8116cc3d877610998f2c6fdc157f31c27badb458427f3cab67f312240f562e06a1819f6ac147c195e43f2d840d672dd0cf1f80fdb1dac6e8bd0157492bfe8b87c145bb2fe49422115139efca9e03&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=dj0rerrTmP1viAq%2FqHfGf12HB9AUM6AWfIt3Bw3twmsR0CedhQsJ3IHhoWnQJOqn
-->