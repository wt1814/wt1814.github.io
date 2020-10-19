
<!-- TOC -->

- [1. netty核心](#1-netty核心)
    - [1.1. 种I/O模型](#11-种io模型)
    - [1.2. Netty核心篇](#12-netty核心篇)
        - [1.2.1. Netty高性能](#121-netty高性能)
            - [线程模型](#线程模型)
            - [零拷贝](#零拷贝)

<!-- /TOC -->



# 1. netty核心概念  
<!-- 

-->


为什么要用 Netty？  
&emsp; Netty是由JBoss开发，基于Java NIO的一个高性能通信框架。之前几篇文章介绍了Java NIO的一些基本的概念和API。但在实际的网络开发中，其实很少使用Java NIO原生的API。主要有以下原因：  

* 原生API使用单线程模型，不能很好利用多核优势；  
* 原生API是直接使用的IO数据，没有做任何封装处理，对数据的编解码、TCP的粘包和拆包、客户端断连、网络的可靠性和安全性方面没有做处理；  

<!-- 

因为 Netty 具有下面这些优点，并且相比于直接使用 JDK 自带的 NIO 相关的 API 来说更加易用。

    统一的 API，支持多种传输类型，阻塞和非阻塞的。
    简单而强大的线程模型。
    自带编解码器解决 TCP 粘包/拆包问题。
    自带各种协议栈。
    真正的无连接数据包套接字支持。
    比直接使用 Java 核心 API 有更高的吞吐量、更低的延迟、更低的资源消耗和更少的内存复制。
    安全性不错，有完整的 SSL/TLS 以及 StartTLS 支持。
    社区活跃
    成熟稳定，经历了大型项目的使用和考验，而且很多开源项目都使用到了 Netty， 比如我们经常接触的 Dubbo、RocketMQ 等等。
    ......
-->

## Socket  


## 1.1. 五种I/O模型  

<!-- 
四图，读懂 BIO、NIO、AIO、多路复用 IO 的区别 
https://mp.weixin.qq.com/s/CRd3-vRD7xwoexqv7xyHRw
彤哥说netty系列之IO的五种模型
https://mp.weixin.qq.com/s?__biz=Mzg2ODA0ODM0Nw==&mid=2247484080&idx=1&sn=54d451db27af1067365ed1fef94a0b2d&chksm=ceb30e04f9c48712bcc13ecb14014fd3b244385881d1aabd66e794b14429ce938b8296f54297&mpshare=1&scene=1&srcid=&sharer_sharetime=1573694075606&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=0fd7b4fa2fb2f076f6b32bb04fcdff32f38e3e711297c12c2ac01f3cda80a8dbf8e95fe381e01b6d0fb0124c2b23cde0c2d17b5f5363615e42acd8ef9d1dd60a86ac6cf94adacae356330adbe943613b&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=9PZBgG0W8u5aIQH8JwuoebfJbcWXVv%2F8Jwpab0URWoWCafXeDrv6e7zaSa2n%2B7Oa
-->



## 1.2. Netty核心篇  

<!-- 
你要的Netty常见面试题总结，敖丙搞来了！
https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA
-->

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

netty初识
https://blog.csdn.net/yxf15732625262/article/details/81302162
-->


#### 线程模型  

<!--
Netty系列文章之Netty线程模型
https://juejin.im/post/5dac6ef75188252bc1657ead

说说Netty的线程模型 
https://mp.weixin.qq.com/s?__biz=MzAxNjM2MTk0Ng==&mid=2247488256&idx=3&sn=253eb6ba1f500d545bd8c836adaf1980&chksm=9bf4a3b5ac832aa3bb05595fac709334dd318698e577fa00b16d696a0fe235d3dee24cee3c75&mpshare=1&scene=1&srcid=&sharer_sharetime=1566173423019&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=5ead8116cc3d877610998f2c6fdc157f31c27badb458427f3cab67f312240f562e06a1819f6ac147c195e43f2d840d672dd0cf1f80fdb1dac6e8bd0157492bfe8b87c145bb2fe49422115139efca9e03&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=dj0rerrTmP1viAq%2FqHfGf12HB9AUM6AWfIt3Bw3twmsR0CedhQsJ3IHhoWnQJOqn

Netty 线程模型
https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA
-->