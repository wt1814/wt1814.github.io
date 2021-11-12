
<!-- TOC -->

- [1. Netty核心概念](#1-netty核心概念)
    - [1.1. Netty简介](#11-netty简介)
    - [1.2. Netty的整体架构](#12-netty的整体架构)
    - [1.3. Netty项目架构](#13-netty项目架构)

<!-- /TOC -->


# 1. Netty核心概念  

## 1.1. Netty简介  
<!-- 
为什么要使用Netty而不直接使用JAVA中的NIO
1.Netty支持三种IO模型同时支持三种Reactor模式。
2.Netty支持很多应用层的协议，提供了很多decoder和encoder。
3.Netty能够解决TCP长连接所带来的缺陷（粘包、半包等）
4.Netty支持应用层的KeepAlive。
5.Netty规避了JAVA NIO中的很多BUG，性能更好。
-->
&emsp; Netty是一个`非阻塞I/O` **<font color = "red">客户端-服务器框架</font>，主要用于开发Java网络应用程序，如协议服务器和客户端。`异步事件驱动`的网络应用程序框架和工具用于简化网络编程，例如TCP和UDP套接字服务器。Netty包括了`反应器编程模式`的实现。  
&emsp; 除了作为异步网络应用程序框架，Netty还包括了对HTTP、HTTP2、DNS及其他协议的支持，涵盖了在Servlet容器内运行的能力、`对WebSockets的支持`、与Google Protocol Buffers的集成、对SSL/TLS的支持以及对用于SPDY协议和消息压缩的支持。  

&emsp; **<font color = "clime">为什么要用Netty？</font>**  
&emsp; 在实际的网络开发中，其实很少使用Java NIO原生的API。主要有以下原因：  

* NIO的类库和API繁杂，使用麻烦，需要熟练掌握Selector、ServerSocketChannek、SockctChannek、ByteBuffer等。  
* **原生API使用单线程模型，不能很好利用多核优势；**  
* 原生API是直接使用的IO数据，没有做任何封装处理，对数据的编解码、TCP的粘包和拆包、客户端断连、网络的可靠性和安全性方面没有做处理；  
* **<font color = "red">JDK NIO的BUG，例如臭名昭著的epoll bug，它会导致Selector空轮询，最终导致CPU100%。</font>官方声称在JDK1.6版本的update18修复了该问题，但是直到JDK 1.7版本该问题仍旧存在，只不过该BUG发生概率降低了一些而已，它并没有得到根本性解决。该BUG以及与该BUG相关的问题单可以参见以下链接内容。** 
    * http://bugs.java.com/bugdatabase/viewbug.do?bug_id=6403933  
    * http://bugs.java.com/bugdalabase/viewbug.do?bug_id=21477l9  

<!-- 
&emsp; (1)NIO的类库和API繁杂，使用麻烦，需要熟练掌握Selector、ServerSocketChannek、SockctChannek、ByteBuffer等。  
&emsp; (2)需要貝备其他的额外技能做铺垫，例如熟悉Java多线程编程。这是因为NIO编程涉 及到Reactor模式，必须对多线程和网路编程非常熟悉，才能编写出高质量的NIO程序。  
&emsp; (3)可靠性能力补齐，工作量和难度都非常大。例如客户端面临断连重连、网络闪断、 半包读写、失败缓存、网络拥塞和异常码流的处理等问题，NIO编程的特点是功能开发相对容易，但是可靠性能力补齐的工作量和难度都非常大。  
&emsp; **<fong color = "red">(4)JDKNIO的BUG,例如見名昭著的epoll bug，它会导致Selector空轮询，最终导致CPU100%。官方声称在JDK 1.6版本的update18修复了该问题,但是直到JDK 1.7版本该问题仍旧存在，只不过该BUG发生概率降低了一些而已，它并没有得到根本性解决。该BUG以及与该BUG相关的问题单可以参见以下链接内容。**</font>  
-->
&emsp; Netty主要用来做网络通信：  

1. 作为RPC框架的网络通信工具：在分布式系统中，不同服务节点之间经常需要相互调用，这个时候就需要RPC框架了。不同服务节点之间的通信是如何做的呢？可以使用Netty来做。比如调用另外一个节点的方法的话，至少是要让对方知道调用的是哪个类中的哪个方法以及相关参数。  
2. 实现一个自己的HTTP服务器：通过Netty可以自己实现一个简单的HTTP服务器，这个大家应该不陌生。说到HTTP服务器的话，作为Java后端开发，一般使用Tomcat比较多。一个最基本的HTTP服务器可要以处理常见的HTTP Method的请求，比如POST请求、GET请求等等。  
3. 实现一个即时通讯系统：使用Netty可以实现一个可以聊天类似微信的即时通讯系统，这方面的开源项目还蛮多的，可以自行去Github找一找。  
4. 实现消息推送系统：市面上有很多消息推送系统都是基于Netty来做的。  
5. ......

## 1.2. Netty的整体架构  
&emsp; 官方文档：https://netty.io/3.8/guide/#architecture.5   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-25.png)  

* Core：核心部分，是底层的网络通用抽象和部分实现。
    * Extensible Event Model：可拓展的事件模型。Netty是基于事件模型的网络应用框架。
    * Universal Communication API：通用的通信API层。Netty定义了一套抽象的通用通信层的API。  
    * Zero-Copy-Capable Rich Byte Buffer：支持零拷贝特性的Byte Buffer实现。
* Transport Services：传输(通信)服务，具体的网络传输的定义与实现。
    * Socket & Datagram：TCP和UDP的传输实现。
    * HTTP Tunnel：HTTP通道的传输实现。
    * In-VM Piple：JVM内部的传输实现。  
* Protocol Support：协议支持。Netty对于一些通用协议的编解码实现。例如：HTTP、Redis、DNS等等。


## 1.3. Netty项目架构  
&emsp; **Netty的项目结构：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-26.png)  

&emsp; **各个项目作用：**  

* common项目，该项目是一个通用的工具类项目，几乎被所有的其它项目依赖使用，它提供了一些数据类型处理工具类，并发编程以及多线程的扩展，计数器等等通用的工具类。  
* buffer项目，该项目下是Netty自行实现的一个Byte Buffer字节缓冲区。该包的实现相对于JDK自带的ByteBuffer有很多优点：无论是 API 的功能，使用体验，性能都要更加优秀。它提供了一系列(多种)的抽象定义以及实现，以满足不同场景下的需要。  

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

