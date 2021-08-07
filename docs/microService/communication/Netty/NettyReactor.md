



<!-- TOC -->

- [1. Netty与Reactor](#1-netty与reactor)
    - [1.1. Reactor是什么](#11-reactor是什么)
    - [1.2. Netty中的线程模型与Reactor的联系](#12-netty中的线程模型与reactor的联系)
        - [1.2.1. 单线程模型](#121-单线程模型)
        - [1.2.2. 多线程模型](#122-多线程模型)
        - [1.2.3. 主从多线程模型 (最常使用)](#123-主从多线程模型-最常使用)

<!-- /TOC -->
<!-- 
Reactor的一般流程、3种线程模型、Netty中的Reactor
-->

&emsp; **<font color = "red">总结：</font>**  
&emsp; Netty的线程模型并不是一成不变的，它实际取决于用户的启动参数配置。<font color = "red">通过设置不同的启动参数，Netty可以同时支持Reactor单线程模型、多线程模型和主从Reactor多线层模型。</font><font color = "clime">Netty主要靠NioEventLoopGroup线程池来实现具体的线程模型的。</font>  


# 1. Netty与Reactor  

<!-- 

https://zhuanlan.zhihu.com/p/264355987?utm_source=wechat_session
https://mp.weixin.qq.com/s/7CvrYsuLSFF7eCAQ3svihw
-->

## 1.1. Reactor是什么
&emsp; 参考[Reactor](/docs/microService/communication/Netty/Reactor.md)    


## 1.2. Netty中的线程模型与Reactor的联系  
<!-- 
https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA
http://www.uml.org.cn/j2ee/201909123.asp
https://mp.weixin.qq.com/s?__biz=MzAxNjM2MTk0Ng==&mid=2247488256&idx=3&sn=253eb6ba1f500d545bd8c836adaf1980&chksm=9bf4a3b5ac832aa3bb05595fac709334dd318698e577fa00b16d696a0fe235d3dee24cee3c75&mpshare=1&scene=1&srcid=&sharer_sharetime=1566173423019&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=5ead8116cc3d877610998f2c6fdc157f31c27badb458427f3cab67f312240f562e06a1819f6ac147c195e43f2d840d672dd0cf1f80fdb1dac6e8bd0157492bfe8b87c145bb2fe49422115139efca9e03&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=dj0rerrTmP1viAq%2FqHfGf12HB9AUM6AWfIt3Bw3twmsR0CedhQsJ3IHhoWnQJOqn

Netty运用Reactor模式到极致 
https://mp.weixin.qq.com/s/rqzzHAhntBJpEHpzz1o5HA
《Netty权威指南》第18章
-->
&emsp; Netty框架的主要线程就是I/O线程，线程模型设计的好坏，决定了系统的吞吐量、并发性和安全性等架构质量属性。  
&emsp; Netty的线程模型并不是一成不变的，它实际取决于用户的启动参数配置。<font color = "red">通过设置不同的启动参数，Netty可以同时支持Reactor单线程模型、多线程模型和主从Reactor多线层模型。</font><font color = "clime">Netty主要靠NioEventLoopGroup线程池来实现具体的线程模型的。</font>  

### 1.2.1. 单线程模型  
&emsp; 单线程模型就是只指定一个线程执行客户端连接和读写操作，也就是在一个Reactor中完成，对应在Netty中的实现就是将NioEventLoopGroup线程数设置为1，核心代码是：  

```java
 NioEventLoopGroup group = new NioEventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY， true)
                .option(ChannelOption.SO_BACKLOG， 1024)
                .childHandler(new ServerHandlerInitializer());
```
&emsp; 它的工作流程大致如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-15.png)  

### 1.2.2. 多线程模型  
&emsp; 多线程模型就是在一个单Reactor中进行客户端连接处理，然后业务处理交给线程池，核心代码如下：  

```java
NioEventLoopGroup eventGroup = new NioEventLoopGroup();
ServerBootstrap bootstrap = new ServerBootstrap();
bootstrap.group(eventGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY， true)
        .option(ChannelOption.SO_BACKLOG， 1024)
        .childHandler(new ServerHandlerInitializer());
```
&emsp; 走进group方法可以发现设置的bossGroup和workerGroup就是使用了同一个group  

```java
@Override
public ServerBootstrap group(EventLoopGroup group) {
    return group(group， group);
}
```
&emsp; 工作流程如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-16.png)  

### 1.2.3. 主从多线程模型 (最常使用)
&emsp; 主从多线程模型是有多个Reactor，也就是存在多个selector，所以定义一个bossGroup和一个workGroup，核心代码如下：  

```java
// 1.bossGroup 用于接收连接，workerGroup 用于具体的处理
NioEventLoopGroup bossGroup = new NioEventLoopGroup();
NioEventLoopGroup workerGroup = new NioEventLoopGroup();
//2.创建服务端启动引导/辅助类：ServerBootstrap
ServerBootstrap bootstrap = new ServerBootstrap();
//3.给引导类配置两大线程组，确定了线程模型
bootstrap.group(bossGroup，workerGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY， true)
        .option(ChannelOption.SO_BACKLOG， 1024)
        .childHandler(new ServerHandlerInitializer());
```
&emsp; 工作流程如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-17.png)  
&emsp; **注意：其实在Netty中，bossGroup线程池最终还是只会随机选择一个线程用于处理客户端连接，与此同时，NioServerSocetChannel绑定到bossGroup的线程中，NioSocketChannel绑定到workGroup的线程中。**  
