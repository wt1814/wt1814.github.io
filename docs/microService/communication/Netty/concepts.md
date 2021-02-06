
<!-- TOC -->

- [1. netty核心概念](#1-netty核心概念)
    - [1.1. Netty简介](#11-netty简介)
    - [1.2. Netty的整体架构](#12-netty的整体架构)
    - [1.3. Netty逻辑架构](#13-netty逻辑架构)
    - [1.4. Netty项目架构](#14-netty项目架构)

<!-- /TOC -->

# 1. netty核心概念  
<!-- 
45 张图深度解析 Netty 架构与原理 
https://mp.weixin.qq.com/s/insjE_EJRoCOM-1GqgZP9A
你要的Netty常见面试题总结，敖丙搞来了！
https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA
Netty网络框架
https://mp.weixin.qq.com/s?__biz=MzIxNTAwNjA4OQ==&mid=2247486074&idx=2&sn=7f7cf296f4f920f251a66e1857db7f04&chksm=979fa49ca0e82d8a628a4e638dfa89a24d74ce6924ab2f9fe0a8582fea33a4a7a2cd75ada4e0&mpshare=1&scene=1&srcid=&sharer_sharetime=1575464797228&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=ba91437029ffb265b462be29eed847d121d1877a58bddf202764fdbcd61e94a41419579d5bf5bf789bbd3bc854452600fcd9e7cafe71703e577957ee7731613da98d69b2581744c5f666bc4318028a01&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=AVVqSkDHGCNLIElG3PjpXco%3D&pass_ticket=UIzvXMBOSWKDgIz4M7cQoxQ548Mbvo9Oik9jB6kaYK60loRzg3FsHZUpAHYbC4%2By
掌握Netty，面试怎么回答Netty？ 
 https://mp.weixin.qq.com/s/36jmjy8YoIwULF-UlnwGtg
-->

## 1.1. Netty简介  
<!-- 
为什么要使用Netty而不直接使用JAVA中的NIO
1.Netty支持三种IO模型同时支持三种Reactor模式。
2.Netty支持很多应用层的协议，提供了很多decoder和encoder。
3.Netty能够解决TCP长连接所带来的缺陷（粘包、半包等）
4.Netty支持应用层的KeepAlive。
5.Netty规避了JAVA NIO中的很多BUG，性能更好。
-->

&emsp; 为什么要用 Netty？  
&emsp; **Netty是由JBoss开发，基于Java NIO的一个高性能通信框架。**在实际的网络开发中，其实很少使用Java NIO原生的API。主要有以下原因：  

* NIO的类库和API繁杂，使用麻烦，需要熟练掌握Selector、ServerSocketChannek、SockctChannek、ByteBuffer等。  
* 原生API使用单线程模型，不能很好利用多核优势；  
* 原生API是直接使用的IO数据，没有做任何封装处理，对数据的编解码、TCP的粘包和拆包、客户端断连、网络的可靠性和安全性方面没有做处理；  
* **<fong color = "red">JDK NIO的BUG，例如臭名昭著的epoll bug，它会导致Selector空轮询，最终导致CPU100%。官方声称在JDK1.6版本的update18修复了该问题，但是直到JDK 1.7版本该问题仍旧存在，只不过该BUG发生概率降低了一些而已，它并没有得到根本性解决。该BUG以及与该BUG相关的问题单可以参见以下链接内容。</font>**  
    * http://bugs.java.com/bugdatabase/viewbug.do?bug_id=6403933  
    * http://bugs.java.com/bugdalabase/viewbug.do?bug_id=21477l9  

<!-- 
&emsp; (1)NIO的类库和API繁杂，使用麻烦，需要熟练掌握Selector、ServerSocketChannek、SockctChannek、ByteBuffer等。  
&emsp; (2)需要貝备其他的额外技能做铺垫，例如熟悉Java多线程编程。这是因为NIO编程涉 及到Reactor模式，必须对多线程和网路编程非常熟悉，才能编写出高质量的NIO程序。  
&emsp; (3)可靠性能力补齐，工作量和难度都非常大。例如客户端面临断连重连、网络闪断、 半包读写、失败缓存、网络拥塞和异常码流的处理等问题，NIO编程的特点是功能开发相对容易，但是可靠性能力补齐的工作量和难度都非常大。  
&emsp; **<fong color = "red">(4)JDKNIO的BUG,例如見名昭著的epoll bug，它会导致Selector空轮询，最终导致CPU100%。官方声称在JDK 1.6版本的update18修复了该问题,但是直到JDK 1.7版本该问题仍旧存在，只不过该BUG发生概率降低了一些而已，它并没有得到根本性解决。该BUG以及与该BUG相关的问题单可以参见以下链接内容。**</font>  
-->
&emsp; Netty主要用来做网络通信：  

1. 作为RPC框架的网络通信工具：在分布式系统中，不同服务节点之间经常需要相互调用，这个时候就需要RPC框架了。不同服务节点之间的通信是如何做的呢？可以使用 Netty 来做。比如调用另外一个节点的方法的话，至少是要让对方知道调用的是哪个类中的哪个方法以及相关参数吧！  
2. 实现一个自己的HTTP服务器：通过Netty可以自己实现一个简单的HTTP服务器，这个大家应该不陌生。说到HTTP服务器的话，作为Java后端开发，一般使用Tomcat比较多。一个最基本的HTTP服务器可要以处理常见的HTTP Method的请求，比如POST请求、GET请求等等。  
3. 实现一个即时通讯系统：使用Netty可以实现一个可以聊天类似微信的即时通讯系统，这方面的开源项目还蛮多的，可以自行去Github找一找。  
4. 实现消息推送系统：市面上有很多消息推送系统都是基于Netty来做的。  
5. ......
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-18.png)  

## 1.2. Netty的整体架构  
&emsp; 官方文档：https://netty.io/3.8/guide/#architecture.5   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-25.png)  

* Core ：核心部分，是底层的网络通用抽象和部分实现。
    * Extensible Event Model：可拓展的事件模型。Netty是基于事件模型的网络应用框架。
    * Universal Communication API：通用的通信API层。Netty定义了一套抽象的通用通信层的API。  
    * Zero-Copy-Capable Rich Byte Buffer：支持零拷贝特性的Byte Buffer 实现。
* Transport Services ：传输(通信)服务，具体的网络传输的定义与实现。
    * Socket & Datagram：TCP和UDP的传输实现。
    * HTTP Tunnel：HTTP通道的传输实现。
    * In-VM Piple：JVM内部的传输实现。  
* Protocol Support：协议支持。Netty对于一些通用协议的编解码实现。例如：HTTP、Redis、DNS等等。

## 1.3. Netty逻辑架构  
<!-- 
《Netty权威指南》第20章
-->
&emsp; Netty采用了典型的三层网络架构进行设计和开发，逻辑架构如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-27.png)  

* Reactor通信调度层  
&emsp; 它由一系列辅助类完成，包括Reactor线程NioEvenlLoop及其父类，NioSocketChannel/NioServerSocketChannel及其父类，ByteBuffer以及由其衍生出来的各种Buffer，Unsafe以及其衍生出的各种内部类等。该层的主要职责就是监听网络的读写和连接操作，负责将网络层的数据读取到内存缓冲区中，然后触发务种网络事件，例如连接创建、连接激活、 读申件、写事件等，将这些少件触发到PipeLine中，由PipeLine管理的职责链来进行后续的处理。  
* 职责链ChannelPipeline  
&emsp; 它负责事件在职责链中的有序传播，同时负责动态地编排职责链。职责链可以选择监听和处理自己关心的事件，它可以拦截处理和向后/向前传播事件。不同应用的Handler用于消息的编解码，它可以将外部的协议消息转换成内部的POJO对象，这样上层业务则只需要关心处理业务逻辑即可，不需要感知底层的协议差异和线程模型差异，实现了架构层面的分层隔离。  
* 业务逻辑编排层(Service ChannelHandler)  
&emsp; 业务逻辑编排层通常有两类：一类是纯粹的业务逻辑编排，还有一类是其他的应用层协议插件，用于特定协议相关的会话和链路管理。例如CMPP协议，用于管理和中国移动短信系统的对接。  

&emsp; 架构的不同层面，需要关心和处理的对象都不同，通常情况下，对于业务开发者，只需要关心职责链的拦截和业务Handler的编排。因为应用层协议栈往往是开发一次，到处运行，所以实际上对于业务开发者来说，只需要关心服务层的业务逻辑开发即可。各种应用协议以插件的形式提供，只有协议开发人员需要关注协议插件，对于其他业务开发人员来说，只需关心业务逻辑定制。这种分层的架构设计理念实现了NIO框架各层之间的解耦，便于上层业务协议栈的开发和业务逻辑的定制。  

## 1.4. Netty项目架构  
&emsp; **Netty的项目结构：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-26.png)  

&emsp; **各个项目作用：**  

* common 项目，该项目是一个通用的工具类项目，几乎被所有的其它项目依赖使用，它提供了一些数据类型处理工具类，并发编程以及多线程的扩展，计数器等等通用的工具类。  
* buffer 项目，该项目下是Netty自行实现的一个Byte Buffer字节缓冲区。该包的实现相对于JDK自带的ByteBuffer有很多优点：无论是 API 的功能，使用体验，性能都要更加优秀。它提供了一系列(多种)的抽象定义以及实现，以满足不同场景下的需要。  

        该项目实现了Netty架构图中的Zero-Copy-Capable Rich Byte Buffer。

* transport项目，该项目是网络传输通道的抽象和实现。它定义通信的统一通信API，统一了JDK 的OIO、NIO(不包括AIO)等多种编程接口。  
&emsp; 另外，它提供了多个子项目，实现不同的传输类型。例如：transport-native-epoll、transport-native-kqueue、transport-rxtx、transport-udt和transport-sctp等等。  

        该项是核心项目，实现了 Netty 架构图中Transport Services、Universal Communication API 和 Extensible Event Model 等多部分内容。

* codec项目，该项目是协议编解码的抽象与部分实现：JSON、Google Protocol、Base64、XML等等。  
&emsp; 另外，它提供了多个子项目，实现不同协议的编解码。例如：codec-dns、codec-haproxy、codec-http、codec-http2、codec-mqtt、codec-redis、codec-memcached、codec-smtp、codec-socks、codec-stomp、codec-xml等等。  

        该项目实现了Netty架构图中的Protocol Support。

* handler 项目，该项目是提供内置的连接通道处理器(ChannelHandler)实现类。例如：SSL 处理器、日志处理器等等。  
&emsp; 另外，它提供了一个子项目handler-proxy，实现对HTTP、Socks4、Socks5的代理转发。   
* example项目，该项目是提供各种Netty使用示例。  

&emsp; **Netty中开发者最经常打交道的五个组件：ByteBuf，Channel，pipeline，ChannelHandler、EventLoop。**  

