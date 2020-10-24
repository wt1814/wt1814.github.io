
<!-- TOC -->

- [1. netty核心概念](#1-netty核心概念)
    - [1.1. 零拷贝](#11-零拷贝)
    - [1.2. Reactor线程模型](#12-reactor线程模型)
        - [1.2.1. Reactor线程模型](#121-reactor线程模型)
            - [1.2.1.1. 单线程模型](#1211-单线程模型)
            - [1.2.1.2. 多线程模型](#1212-多线程模型)
            - [1.2.1.3. 主从多线程模型](#1213-主从多线程模型)
        - [1.2.2. Netty中的线程模型与Reactor的联系](#122-netty中的线程模型与reactor的联系)
            - [1.2.2.1. 单线程模型](#1221-单线程模型)
            - [1.2.2.2. 多线程模型](#1222-多线程模型)
            - [1.2.2.3. 主从多线程模型 (最常使用)](#1223-主从多线程模型-最常使用)

<!-- /TOC -->



# 1. netty核心概念  

<!-- 
你要的Netty常见面试题总结，敖丙搞来了！
https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA
-->


&emsp; 为什么要用 Netty？  
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



&emsp; Netty高性能  

* 异步非阻塞通信  
* 零拷贝  
* 内存池
* 高效的Reactor线程模型  
* 无锁化的串行设计理念  
* 高效的并发编程  
* 对高性能对的序列化框架支持
* 灵活的TCP参数配置能力


## 1.1. 零拷贝  
<!-- 

原来 8 张图，就可以搞懂「零拷贝」了
https://mp.weixin.qq.com/s/P0IP6c_qFhuebwdwD8HM7w

netty初识
https://blog.csdn.net/yxf15732625262/article/details/81302162
-->


## 1.2. Reactor线程模型  

<!--
说说Netty的线程模型 
https://mp.weixin.qq.com/s?__biz=MzAxNjM2MTk0Ng==&mid=2247488256&idx=3&sn=253eb6ba1f500d545bd8c836adaf1980&chksm=9bf4a3b5ac832aa3bb05595fac709334dd318698e577fa00b16d696a0fe235d3dee24cee3c75&mpshare=1&scene=1&srcid=&sharer_sharetime=1566173423019&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=5ead8116cc3d877610998f2c6fdc157f31c27badb458427f3cab67f312240f562e06a1819f6ac147c195e43f2d840d672dd0cf1f80fdb1dac6e8bd0157492bfe8b87c145bb2fe49422115139efca9e03&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=dj0rerrTmP1viAq%2FqHfGf12HB9AUM6AWfIt3Bw3twmsR0CedhQsJ3IHhoWnQJOqn

-->

&emsp; 大部分网络框架都是基于 Reactor 模式设计开发的。  

### 1.2.1. Reactor线程模型  
&emsp; Reactor模式是基于事件驱动开发的，核心组成部分包括Reactor和线程池，其中Reactor负责监听和分配事件，线程池负责处理事件，而根据Reactor的数量和线程池的数量，又将Reactor分为三种模型:

* 单线程模型 (单Reactor单线程)  
* 多线程模型 (单Reactor多线程)  
* 主从多线程模型 (多Reactor多线程)  

#### 1.2.1.1. 单线程模型  
&emsp; 一个线程需要执行处理所有的 accept、read、decode、process、encode、send 事件。对于高负载、高并发，并且对性能要求比较高的场景不适用。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-10.png)  

* Reactor内部通过selector 监控连接事件，收到事件后通过dispatch进行分发，如果是连接建立的事件，则由Acceptor处理，Acceptor通过accept接受连接，并创建一个Handler来处理连接后续的各种事件,如果是读写事件，直接调用连接对应的Handler来处理。  
* Handler完成read->(decode->compute->encode)->send的业务流程。  
* 这种模型好处是简单，坏处却很明显，当某个Handler阻塞时，会导致其他客户端的handler和accpetor都得不到执行，无法做到高性能，只适用于业务处理非常快速的场景。  

#### 1.2.1.2. 多线程模型
&emsp; 一个 Acceptor 线程只负责监听客户端的连接，一个 NIO 线程池负责具体处理：accept、read、decode、process、encode、send 事件。满足绝大部分应用场景，并发连接量不大的时候没啥问题，但是遇到并发连接大的时候就可能会出现问题，成为性能瓶颈。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-11.png)  

* 主线程中，Reactor对象通过selector监控连接事件,收到事件后通过dispatch进行分发，如果是连接建立事件，则由Acceptor处理，Acceptor通过accept接收连接，并创建一个Handler来处理后续事件，而Handler只负责响应事件，不进行业务操作，也就是只进行read读取数据和write写出数据，业务处理交给一个线程池进行处理
* 线程池分配一个线程完成真正的业务处理，然后将响应结果交给主进程的Handler处理，Handler将结果send给client (下面是核心代码)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-13.png)  

&emsp; 单Reactor承当所有事件的监听和响应,而当我们的服务端遇到大量的客户端同时进行连接，或者在请求连接时执行一些耗时操作，比如身份认证，权限检查等，这种瞬时的高并发就容易成为性能瓶颈  

#### 1.2.1.3. 主从多线程模型  
&emsp; 从一个 主线程 NIO 线程池中选择一个线程作为 Acceptor 线程，绑定监听端口，接收客户端连接的连接，其他线程负责后续的接入认证等工作。连接建立完成后，Sub NIO 线程池负责具体处理 I/O 读写。如果多线程模型无法满足你的需求的时候，可以考虑使用主从多线程模型 。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-12.png)  

* 存在多个Reactor，每个Reactor都有自己的selector选择器，线程和dispatch
* 主线程中的mainReactor通过自己的selector监控连接建立事件，收到事件后通过Accpetor接收，将新的连接分配给某个子线程
* 子线程中的subReactor将mainReactor分配的连接加入连接队列中通过自己的selector进行监听，并创建一个Handler用于处理后续事件
* Handler完成read->业务处理->send的完整业务流程
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-14.png)  

### 1.2.2. Netty中的线程模型与Reactor的联系  
<!-- 
https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA

 Netty运用Reactor模式到极致 
 https://mp.weixin.qq.com/s/rqzzHAhntBJpEHpzz1o5HA
-->
&emsp; Netty主要靠NioEventLoopGroup线程池来实现具体的线程模型的。  

#### 1.2.2.1. 单线程模型  
&emsp; 单线程模型就是只指定一个线程执行客户端连接和读写操作，也就是在一个Reactor中完成，对应在Netty中的实现就是将NioEventLoopGroup线程数设置为1，核心代码是：  

```java
 NioEventLoopGroup group = new NioEventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ServerHandlerInitializer());
```
&emsp; 它的工作流程大致如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-15.png)  
&emsp; 上述单线程模型就对应了Reactor的单线程模型  

#### 1.2.2.2. 多线程模型  
&emsp; 多线程模型就是在一个单Reactor中进行客户端连接处理，然后业务处理交给线程池，核心代码如下：  

```java
NioEventLoopGroup eventGroup = new NioEventLoopGroup();
ServerBootstrap bootstrap = new ServerBootstrap();
bootstrap.group(eventGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.SO_BACKLOG, 1024)
        .childHandler(new ServerHandlerInitializer());
```
&emsp; 走进group方法可以发现我们平时设置的bossGroup和workerGroup就是使用了同一个group  

```java
@Override
public ServerBootstrap group(EventLoopGroup group) {
    return group(group, group);
}
```
&emsp; 工作流程如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-16.png)  

#### 1.2.2.3. 主从多线程模型 (最常使用)
&emsp; 主从多线程模型是有多个Reactor，也就是存在多个selector，所以我们定义一个bossGroup和一个workGroup，核心代码如下：  

```java
// 1.bossGroup 用于接收连接，workerGroup 用于具体的处理
NioEventLoopGroup bossGroup = new NioEventLoopGroup();
NioEventLoopGroup workerGroup = new NioEventLoopGroup();
//2.创建服务端启动引导/辅助类：ServerBootstrap
ServerBootstrap bootstrap = new ServerBootstrap();
//3.给引导类配置两大线程组,确定了线程模型
bootstrap.group(bossGroup,workerGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.SO_BACKLOG, 1024)
        .childHandler(new ServerHandlerInitializer());
```
&emsp; 工作流程如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-17.png)  
&emsp; **注意：其实在Netty中，bossGroup线程池最终还是只会随机选择一个线程用于处理客户端连接，与此同时，NioServerSocetChannel绑定到bossGroup的线程中，NioSocketChannel绑定到workGroup的线程中**  



