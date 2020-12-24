
<!-- TOC -->

- [1. Reactor线程模型](#1-reactor线程模型)
    - [1.1. Reactor是什么](#11-reactor是什么)
        - [1.1.1. thread-based architecture](#111-thread-based-architecture)
        - [1.1.2. event-driven architecture](#112-event-driven-architecture)
        - [1.1.3. Reactor（反应堆）](#113-reactor反应堆)
    - [1.2. Reactor线程模型详解](#12-reactor线程模型详解)
        - [1.2.1. 单线程模型](#121-单线程模型)
        - [1.2.2. 多线程模型](#122-多线程模型)
        - [1.2.3. 主从多线程模型](#123-主从多线程模型)
    - [1.3. Netty中的线程模型与Reactor的联系](#13-netty中的线程模型与reactor的联系)
        - [1.3.1. 单线程模型](#131-单线程模型)
        - [1.3.2. 多线程模型](#132-多线程模型)
        - [1.3.3. 主从多线程模型 (最常使用)](#133-主从多线程模型-最常使用)

<!-- /TOC -->


# 1. Reactor线程模型  

<!--
说说Netty的线程模型 
https://mp.weixin.qq.com/s?__biz=MzAxNjM2MTk0Ng==&mid=2247488256&idx=3&sn=253eb6ba1f500d545bd8c836adaf1980&chksm=9bf4a3b5ac832aa3bb05595fac709334dd318698e577fa00b16d696a0fe235d3dee24cee3c75&mpshare=1&scene=1&srcid=&sharer_sharetime=1566173423019&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=5ead8116cc3d877610998f2c6fdc157f31c27badb458427f3cab67f312240f562e06a1819f6ac147c195e43f2d840d672dd0cf1f80fdb1dac6e8bd0157492bfe8b87c145bb2fe49422115139efca9e03&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=dj0rerrTmP1viAq%2FqHfGf12HB9AUM6AWfIt3Bw3twmsR0CedhQsJ3IHhoWnQJOqn
linux高性能网络IO+Reactor模型 
https://mp.weixin.qq.com/s/JPcOKoWhBDW59GpO37Jq4w
-->
<!-- 

-->
## 1.1. Reactor是什么
&emsp; 在处理web请求时，通常有两种体系结构，分别为：thread-based architecture（基于线程）、event-driven architecture（事件驱动）。  

### 1.1.1. thread-based architecture
&emsp; 基于线程的体系结构通常会使用多线程来处理客户端的请求，每当接收到一个请求，便开启一个独立的线程来处理。这种方式虽然是直观的，但是仅适用于并发访问量不大的场景，因为线程需要占用一定的内存资源，且操作系统在线程之间的切换也需要一定的开销，当线程数过多时显然会降低web服务器的性能。并且，当线程在处理I/O操作，在等待输入的这段时间线程处于空闲的状态，同样也会造成cpu资源的浪费。一个典型的设计如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-36.png)  

### 1.1.2. event-driven architecture
&emsp; 事件驱动体系结构是目前比较广泛使用的一种。这种方式会定义一系列的事件处理器来响应事件的发生，并且将服务端接受连接与对事件的处理分离。其中，事件是一种状态的改变。比如，tcp中socket的new incoming connection、ready for read、ready for write。  

### 1.1.3. Reactor（反应堆）  
&emsp; Reactor设计模式是event-driven architecture的一种实现方式，处理多个客户端并发的向服务端请求服务的场景。每种服务在服务端可能由多个方法组成。reactor会解耦并发请求的服务并分发给对应的事件处理器来处理。目前，许多流行的开源框架都用到了reactor模式，如：netty、node.js等，包括java的nio。  

        维基百科上的定义：“反应堆设计模式是一种事件处理模式，用于处理由一个或多个输入同时发送的服务请求。然后，服务处理程序将传入的请求多路分解，并同步地将其分发到关联的请求处理程序。”。

&emsp; 总体图示如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-37.png)  
<!-- 
https://www.jianshu.com/p/eef7ebe28673
-->
&emsp; Reactor主要由以下几个角色构成：handle、Synchronous Event Demultiplexer、Initiation Dispatcher、Event Handler、Concrete Event Handler  

* Handle，handle，在linux中一般称为文件描述符，而在window称为句柄，两者的含义一样。handle是事件的发源地。比如一个网络socket、磁盘文件等。而发生在handle上的事件可以有connection、ready for read、ready for write等。  
* Synchronous Event Demultiplexer，同步事件分离器，本质上是系统调用。比如linux中的select、poll、epoll等。比如，select方法会一直阻塞直到handle上有事件发生时才会返回。  
* **Event Handler，事件处理器，**其会定义一些回调方法或者称为钩子函数，当handle上有事件发生时，回调方法便会执行，一种事件处理机制。  
* **Concrete Event Handler，具体的事件处理器，**实现了Event Handler。在回调方法中会实现具体的业务逻辑。  
* Initiation Dispatcher，初始分发器，也是reactor角色，提供了注册、删除与转发event handler的方法。当Synchronous Event Demultiplexer检测到handle上有事件发生时，便会通知initiation dispatcher调用特定的event handler的回调方法。  

&emsp; **处理流程：**  
1. 当应用向Initiation Dispatcher注册Concrete Event Handler时，应用会标识出该事件处理器希望Initiation Dispatcher在某种类型的事件发生发生时向其通知，事件与handle关联  
2. Initiation Dispatcher要求注册在其上面的Concrete Event Handler传递内部关联的handle，该handle会向操作系统标识
3. 当所有的Concrete Event Handler都注册到 Initiation Dispatcher上后，应用会调用handle_events方法来启动Initiation Dispatcher的事件循环，这时Initiation Dispatcher会将每个Concrete Event Handler关联的handle合并，并使用Synchronous Event Demultiplexer来等待这些handle上事件的发生
4. 当与某个事件源对应的handle变为ready时，Synchronous Event Demultiplexer便会通知 Initiation Dispatcher。比如tcp的socket变为ready for reading
5. Initiation Dispatcher会触发事件处理器的回调方法。当事件发生时， Initiation Dispatcher会将被一个“key”（表示一个激活的handle）定位和分发给特定的Event Handler的回调方法
6. Initiation Dispatcher调用特定的Concrete Event Handler的回调方法来响应其关联的handle上发生的事件

## 1.2. Reactor线程模型详解  
&emsp; 无论是C++ 还是 Java 编写的网络框架，大部分网络框架都是基于 Reactor模式设计开发的。Reactor模式核心组成部分包括Reactor和线程池，其中Reactor负责监听和分配事件，线程池负责处理事件，而根据Reactor的数量和线程池的数量，又将Reactor分为三种模型:

* 单线程模型 (单Reactor单线程)  
* 多线程模型 (单Reactor多线程)  
* 主从多线程模型 (多Reactor多线程)  

### 1.2.1. 单线程模型  
&emsp; 一个线程需要执行处理所有的 accept、read、decode、process、encode、send 事件。对于高负载、高并发，并且对性能要求比较高的场景不适用。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-10.png)  

* Reactor内部通过selector 监控连接事件，收到事件后通过dispatch进行分发，如果是连接建立的事件，则由Acceptor处理，Acceptor通过accept接受连接，并创建一个Handler来处理连接后续的各种事件,如果是读写事件，直接调用连接对应的Handler来处理。  
* Handler完成read->(decode->compute->encode)->send的业务流程。  
* 这种模型好处是简单，坏处却很明显，当某个Handler阻塞时，会导致其他客户端的handler和accpetor都得不到执行，无法做到高性能，只适用于业务处理非常快速的场景。  

### 1.2.2. 多线程模型
&emsp; 一个 Acceptor 线程只负责监听客户端的连接，一个 NIO 线程池负责具体处理：accept、read、decode、process、encode、send 事件。满足绝大部分应用场景，并发连接量不大的时候没啥问题，但是遇到并发连接大的时候就可能会出现问题，成为性能瓶颈。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-11.png)  

* 主线程中，Reactor对象通过selector监控连接事件，收到事件后通过dispatch进行分发，如果是连接建立事件，则由Acceptor处理，Acceptor通过accept接收连接，并创建一个Handler来处理后续事件，而Handler只负责响应事件，不进行业务操作，也就是只进行read读取数据和write写出数据，业务处理交给一个线程池进行处理
* 线程池分配一个线程完成真正的业务处理，然后将响应结果交给主进程的Handler处理，Handler将结果send给client (下面是核心代码)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-13.png)  

&emsp; 单Reactor承当所有事件的监听和响应，而当服务端遇到大量的客户端同时进行连接，或者在请求连接时执行一些耗时操作，比如身份认证，权限检查等，这种瞬时的高并发就容易成为性能瓶颈  

### 1.2.3. 主从多线程模型  
&emsp; 从一个 主线程 NIO 线程池中选择一个线程作为 Acceptor 线程，绑定监听端口，接收客户端连接的连接，其他线程负责后续的接入认证等工作。连接建立完成后，Sub NIO 线程池负责具体处理 I/O 读写。如果多线程模型无法满足需求的时候，可以考虑使用主从多线程模型 。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-12.png)  

* 存在多个Reactor，每个Reactor都有自己的selector选择器，线程和dispatch
* 主线程中的mainReactor通过自己的selector监控连接建立事件，收到事件后通过Accpetor接收，将新的连接分配给某个子线程
* 子线程中的subReactor将mainReactor分配的连接加入连接队列中通过自己的selector进行监听，并创建一个Handler用于处理后续事件
* Handler完成read->业务处理->send的完整业务流程
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-14.png)  

## 1.3. Netty中的线程模型与Reactor的联系  
<!-- 
https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA

 Netty运用Reactor模式到极致 
 https://mp.weixin.qq.com/s/rqzzHAhntBJpEHpzz1o5HA

《Netty权威指南》第18章
-->
&emsp; Netty框架的主要线程就是I/O线程，线程模型设计的好坏，决定了系统的吞吐量、并发性和安全性等架构质量属性。  
&emsp; Netty的线程模型并不是一成不变的，它实际取决于用户的启动参数配置。<font color = "red">通过设置不同的启动参数，Netty可以同时支持Reactor单线程模型、多线程模型和主从Reactor多线层模型。</font><font color = "lime">Netty主要靠NioEventLoopGroup线程池来实现具体的线程模型的。</font>  

### 1.3.1. 单线程模型  
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

### 1.3.2. 多线程模型  
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
&emsp; 走进group方法可以发现设置的bossGroup和workerGroup就是使用了同一个group  

```java
@Override
public ServerBootstrap group(EventLoopGroup group) {
    return group(group, group);
}
```
&emsp; 工作流程如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-16.png)  

### 1.3.3. 主从多线程模型 (最常使用)
&emsp; 主从多线程模型是有多个Reactor，也就是存在多个selector，所以定义一个bossGroup和一个workGroup，核心代码如下：  

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



