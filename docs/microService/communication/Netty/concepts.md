
<!-- TOC -->

- [1. netty核心概念](#1-netty核心概念)
    - [Netty简介](#netty简介)
    - [Netty项目架构](#netty项目架构)
    - [Netty逻辑架构](#netty逻辑架构)
    - [Netty核心组件](#netty核心组件)
    - [1.3. Reactor线程模型](#13-reactor线程模型)
        - [1.3.1. Reactor线程模型](#131-reactor线程模型)
            - [1.3.1.1. 单线程模型](#1311-单线程模型)
            - [1.3.1.2. 多线程模型](#1312-多线程模型)
            - [1.3.1.3. 主从多线程模型](#1313-主从多线程模型)
        - [1.3.2. Netty中的线程模型与Reactor的联系](#132-netty中的线程模型与reactor的联系)
            - [1.3.2.1. 单线程模型](#1321-单线程模型)
            - [1.3.2.2. 多线程模型](#1322-多线程模型)
            - [1.3.2.3. 主从多线程模型 (最常使用)](#1323-主从多线程模型-最常使用)

<!-- /TOC -->

# 1. netty核心概念  


<!-- 
你要的Netty常见面试题总结，敖丙搞来了！
https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA
-->

## Netty简介  

&emsp; 为什么要用 Netty？  
&emsp; Netty是由JBoss开发，基于Java NIO的一个高性能通信框架。之前几篇文章介绍了Java NIO的一些基本的概念和API。但在实际的网络开发中，其实很少使用Java NIO原生的API。主要有以下原因：  

* 原生API使用单线程模型，不能很好利用多核优势；  
* 原生API是直接使用的IO数据，没有做任何封装处理，对数据的编解码、TCP的粘包和拆包、客户端断连、网络的可靠性和安全性方面没有做处理；  

&emsp;(1)NIO的类库和API繁杂，使用麻烦，需要熟练掌握Selector、ServerSocketChannek、SockctChannek、ByteBuffer等。  
&emsp;(2)需要貝备其他的额外技能做铺垫，例如熟悉Java多线程编程。这是因为NIO编程涉 及到Reactor模式，必须对多线程和网路编程非常熟悉，才能编写出高质量的NIO程序。  
&emsp;(3)可靠性能力补齐，匸作量和难度都非常大。例如客户端面临断连重连、网络闪断、 半包读写、失败缓存、网络拥塞和异常码流的处理等问题，N1O编程的特点是功能开发相 对容易，但是可靠性能力补齐的工作吊:和难度都非常大。  
&emsp;(4)JDKNIO的BUG,例如見名昭著的叩。II bug,它会导致Selector空轮机 最终导 致CPU 100%。官方声称在JDK 1.6版本的update 18修复P该问题,但是直到JDK 1.7版 本该问题仍旧存在,只不过该BUG发生概率降低了 •些而已,它并没有得到根本性解决。 该BUG以及与该BUG相关的问题单可以参见以下链接内容。  
* http://bugs.java.com/bugdatabase/view bug.do?bug_id=6403933  
* http://bugs.java.com/bugdalabase/view bug.do?bug id=21477l9  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-18.png)  




## Netty项目架构  
<!-- 

http://svip.iocoder.cn/Netty/intro-1/#3-%E6%9E%B6%E6%9E%84%E5%9B%BE
-->

## Netty逻辑架构  
<!-- 

《Netty权威指南》第20章
-->

## Netty核心组件 
<!-- 

https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA
深入剖析 Netty 的核心组件
https://mp.weixin.qq.com/s?__biz=MzA4Mzc0NjkwNA==&mid=2650789476&idx=1&sn=2e80b93d77d981545ec0675daadb6a19&chksm=87fabf53b08d3645b3360ebcbe76e3f4ed997c864207b9346a9f966dd87bd66fc5a25e7b93b6&mpshare=1&scene=1&srcid=&sharer_sharetime=1573692167895&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=19d3af0bf483adc6682923d492b7787944532c9ccc243b9542acda07d977289c60fd04548bf7f65ebbbfea992b3f7312e4e8a1be71ca9e2a2aa14344514466ae32bee79145286966d23d4da3a7badb6b&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=9PZBgG0W8u5aIQH8JwuoebfJbcWXVv%2F8Jwpab0URWoWCafXeDrv6e7zaSa2n%2B7Oa

-->

