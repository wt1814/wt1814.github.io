
<!-- TOC -->

- [1. netty核心概念](#1-netty核心概念)
    - [1.1. Netty简介](#11-netty简介)
    - [1.2. Netty项目架构](#12-netty项目架构)
    - [1.3. Netty逻辑架构](#13-netty逻辑架构)
    - [1.4. Netty核心组件](#14-netty核心组件)

<!-- /TOC -->

# 1. netty核心概念  

<!-- 
你要的Netty常见面试题总结，敖丙搞来了！
https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA
-->

## 1.1. Netty简介  

&emsp; 为什么要用 Netty？  
&emsp; Netty是由JBoss开发，基于Java NIO的一个高性能通信框架。之前几篇文章介绍了Java NIO的一些基本的概念和API。但在实际的网络开发中，其实很少使用Java NIO原生的API。主要有以下原因：  

* 原生API使用单线程模型，不能很好利用多核优势；  
* 原生API是直接使用的IO数据，没有做任何封装处理，对数据的编解码、TCP的粘包和拆包、客户端断连、网络的可靠性和安全性方面没有做处理；  

&emsp; (1)NIO的类库和API繁杂，使用麻烦，需要熟练掌握Selector、ServerSocketChannek、SockctChannek、ByteBuffer等。  
&emsp; (2)需要貝备其他的额外技能做铺垫，例如熟悉Java多线程编程。这是因为NIO编程涉 及到Reactor模式，必须对多线程和网路编程非常熟悉，才能编写出高质量的NIO程序。  
&emsp; (3)可靠性能力补齐，工作量和难度都非常大。例如客户端面临断连重连、网络闪断、 半包读写、失败缓存、网络拥塞和异常码流的处理等问题，NIO编程的特点是功能开发相对容易，但是可靠性能力补齐的工作量和难度都非常大。  
&emsp; **<fong color = "red">(4)JDKNIO的BUG,例如見名昭著的epoll bug，它会导致Selector空轮询，最终导致CPU100%。官方声称在JDK 1.6版本的update18修复了该问题,但是直到JDK 1.7版本该问题仍旧存在，只不过该BUG发生概率降低了一些而已，它并没有得到根本性解决。该BUG以及与该BUG相关的问题单可以参见以下链接内容。**</font>  

* http://bugs.java.com/bugdatabase/viewbug.do?bug_id=6403933  
* http://bugs.java.com/bugdalabase/viewbug.do?bug_id=21477l9  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-18.png)  

## 1.2. Netty项目架构  
&emsp; **Netty的项目结构：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-26.png)  

&emsp; **Netty的整体架构图：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-25.png)  

* Core ：核心部分，是底层的网络通用抽象和部分实现。
    * Extensible Event Model ：可拓展的事件模型。Netty 是基于事件模型的网络应用框架。
    * Universal Communication API ：通用的通信 API 层。Netty 定义了一套抽象的通用通信层的 API 。
    * Zero-Copy-Capable Rich Byte Buffer ：支持零拷贝特性的 Byte Buffer 实现。
* Transport Services ：传输( 通信 )服务，具体的网络传输的定义与实现。
    * Socket & Datagram ：TCP 和 UDP 的传输实现。
    * HTTP Tunnel ：HTTP通道的传输实现。
    * In-VM Piple ：JVM内部的传输实现。  
* Protocol Support ：协议支持。Netty对于一些通用协议的编解码实现。例如：HTTP、Redis、DNS等等。

&emsp; **各个项目作用：**  

* common 项目，该项目是一个通用的工具类项目，几乎被所有的其它项目依赖使用，它提供了一些数据类型处理工具类，并发编程以及多线程的扩展，计数器等等通用的工具类。  
* buffer 项目，该项目下是 Netty 自行实现的一个 Byte Buffer 字节缓冲区。该包的实现相对于 JDK 自带的 ByteBuffer 有很多优点：无论是 API 的功能，使用体验，性能都要更加优秀。它提供了一系列( 多种 )的抽象定义以及实现，以满足不同场景下的需要。  

        该项目实现了 Netty 架构图中的 Zero-Copy-Capable Rich Byte Buffer 。

* transport 项目，该项目是网络传输通道的抽象和实现。它定义通信的统一通信 API ，统一了 JDK 的 OIO、NIO ( 不包括 AIO )等多种编程接口。  
&emsp; 另外，它提供了多个子项目，实现不同的传输类型。例如：transport-native-epoll、transport-native-kqueue、transport-rxtx、transport-udt 和 transport-sctp 等等。  

        该项是核心项目，实现了 Netty 架构图中 Transport Services、Universal Communication API 和 Extensible Event Model 等多部分内容。

* codec 项目，该项目是协议编解码的抽象与部分实现：JSON、Google Protocol、Base64、XML等等。  
&emsp; 另外，它提供了多个子项目，实现不同协议的编解码。例如：codec-dns、codec-haproxy、codec-http、codec-http2、codec-mqtt、codec-redis、codec-memcached、codec-smtp、codec-socks、codec-stomp、codec-xml 等等。  

        该项目实现了Netty 架构图中的 Protocol Support 。

* handler 项目，该项目是提供内置的连接通道处理器( ChannelHandler )实现类。例如：SSL 处理器、日志处理器等等。  
&emsp; 另外，它提供了一个子项目 handler-proxy ，实现对 HTTP、Socks 4、Socks 5 的代理转发。   
* example 项目，该项目是提供各种 Netty 使用示例。  

## 1.3. Netty逻辑架构  
<!-- 

《Netty权威指南》第20章
-->

&emsp; Netty采用了典型的三层网络架构进行设计和开发，逻辑架构如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-27.png)  

* Reactor通信调度层  
&emsp; 它由一系列辅助类完成，包括Reactor线程NioEvenlLoop及其父类，NioSocketChannel/ NioServerSocketChannel 及其父类，ByteBuffer 以及由其衍生出来的各种 Buffer. Unsafe 以及其衍生出的*种内部类等。该层的主要职责就是监听网络的读写和连接操作，负责将 网络层的数据读取到内存缓冲区中，然后触发务种网络爭件，例如连接创建、连接激活、 读申件、写事件等，将这些少件触发到PipeLine ill PipeLine管理的职责链来进行后续 的处理。  
* 职责ChannelPipeline  
它负責事件在职责链中的冇序传播，同时负责动态地编排职责链。职责链可以选拝监 听和处理自己关心的事件，它可以拦截处理和向后/向前传播事件。不同应用的Handler W 点的功能也不同，通常情况下，往往会开发编解Pl Hanlder用于消息的编解码，它可以将 外部的协议消息转换成内部的POJO对象，这样上层业务则只需要关心处理业务逻辑即可， 小需要感知底层的协议差异和线程模型差异，实现了架构层面的分层隔离。  
* 业务逻辑编排层(Service ChannelHandler)  
&emsp; 业务逻辑编排层通常有两类：一类是纯粹的业务逻辑编排，还有一类是其他的应用层 协议插件，用于特定协议相关的会话和链路管理。例如CMPP协议，用于管理和中国移动 短信系统的对接。  
&emsp; 架构的不同层面，需要关心和处理的对象都不同，通常情况下，对于业务开发者，只 需要关心职责链的拦截和业务Handler的编排。因为应用层协议栈往往是开发一次，到处 运行，所以实际上对于业务开发者来说，只需要关心服务层的业务逻辑开发即可。各种应 用协议以插件的形式提供，只有协议开发人员需要关注协议插件，对于其他业务开发人员 来说，只需关心业务辺辑定制。这种分层的架构设计理念实现了 NIO框架各层之间的解耦， 便于上层业务协议栈的开发和业务逻辑的定制。  

## 1.4. Netty核心组件 
<!-- 

https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA
深入剖析 Netty 的核心组件
https://mp.weixin.qq.com/s?__biz=MzA4Mzc0NjkwNA==&mid=2650789476&idx=1&sn=2e80b93d77d981545ec0675daadb6a19&chksm=87fabf53b08d3645b3360ebcbe76e3f4ed997c864207b9346a9f966dd87bd66fc5a25e7b93b6&mpshare=1&scene=1&srcid=&sharer_sharetime=1573692167895&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=19d3af0bf483adc6682923d492b7787944532c9ccc243b9542acda07d977289c60fd04548bf7f65ebbbfea992b3f7312e4e8a1be71ca9e2a2aa14344514466ae32bee79145286966d23d4da3a7badb6b&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=9PZBgG0W8u5aIQH8JwuoebfJbcWXVv%2F8Jwpab0URWoWCafXeDrv6e7zaSa2n%2B7Oa

-->

