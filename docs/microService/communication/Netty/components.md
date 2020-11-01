<!-- TOC -->

- [1. Netty核心组件](#1-netty核心组件)
    - [1.1. Bootstrap & ServerBootstrap](#11-bootstrap--serverbootstrap)
    - [1.2. EventLoopGroup && EventLoop](#12-eventloopgroup--eventloop)
        - [1.2.1. EventLoopGroup && EventLoop && Channel](#121-eventloopgroup--eventloop--channel)
        - [1.2.2. EventLoopGroup && Reactor](#122-eventloopgroup--reactor)
    - [1.3. ByteBuf](#13-bytebuf)
    - [1.4. channel](#14-channel)
        - [1.4.1. ChannelFuture && ChannelPromise](#141-channelfuture--channelpromise)
    - [1.5. ChannelHandler](#15-channelhandler)
        - [1.5.1. ChannelInboundHandler](#151-channelinboundhandler)
        - [1.5.2. ChannelOutboundHandler](#152-channeloutboundhandler)
        - [1.5.3. ChannelHandlerAdapter](#153-channelhandleradapter)
    - [1.6. ChannelPipeline](#16-channelpipeline)
    - [1.7. ChannelHandlerContext](#17-channelhandlercontext)
        - [1.7.1. 与ChannelHandler、ChannelPipeline的关联使用](#171-与channelhandlerchannelpipeline的关联使用)
    - [1.8. 核心组件之间的关系](#18-核心组件之间的关系)

<!-- /TOC -->

# 1. Netty核心组件 
<!-- 
Netty之旅二：口口相传的高性能Netty到底是什么？ 
https://mp.weixin.qq.com/s?__biz=Mzg5ODA5NDIyNQ==&mid=2247484812&idx=1&sn=52d38717da60683d671136f50009f4fd&chksm=c0668072f71109643fb4697d2ddcec1a1983544dc4e3497f2c2a6aff525c292d3d9f2bd30be0&scene=178&cur_album_id=1486063894363242498#rd

-->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-44.png)  

## 1.1. Bootstrap & ServerBootstrap  
一看到BootStrap大家就应该想到启动类、引导类这样的词汇，之前分析过EurekaServer项目启动类时介绍过EurekaBootstrap， 他的作用就是上下文初始化、配置初始化。  
在Netty中我们也有类似的类，Bootstrap和ServerBootstrap它们都是Netty程序的引导类，主要用于配置各种参数，并启动整个Netty服务。    

## 1.2. EventLoopGroup && EventLoop  
&emsp; EventLoop（事件循环）接口可以说是 Netty 中最核心的概念了！  
&emsp; EventLoop 定义了 Netty 的核心抽象，用于处理连接的生命周期中所发生的事件。  
&emsp; EventLoop 的主要作用实际就是负责监听网络事件并调用事件处理器进行相关 I/O 操作的处理。

&emsp; Channel和EventLoop之间的联系：  
&emsp; Channel为Netty 网络操作(读写等操作)抽象类，EventLoop 负责处理注册到其上的Channel 处理 I/O 操作，两者配合参与 I/O 操作。 

### 1.2.1. EventLoopGroup && EventLoop && Channel  


### 1.2.2. EventLoopGroup && Reactor  


## 1.3. ByteBuf  


## 1.4. channel  
&emsp; Channel接口是Netty对网络操作抽象类，它除了包括基本的I/O 操作，如 bind()、connect()、read()、write()等。  
&emsp; 比较常用的Channel接口实现类是NioServerSocketChannel（服务端）和NioSocketChannel（客户端），这两个 Channel 可以和 BIO 编程模型中的ServerSocket以及Socket两个概念对应上。Netty 的 Channel 接口所提供的 API，大大地降低了直接使用 Socket 类的复杂性。  

 

### 1.4.1. ChannelFuture && ChannelPromise 
&emsp; Netty 是异步非阻塞的，所有的 I/O 操作都为异步的。因此，不能立刻得到操作是否执行成功，但是，可以通过 ChannelFuture 接口的 addListener() 方法注册一个 ChannelFutureListener，当操作执行成功或者失败时，监听就会自动触发返回结果。  
&emsp; 并且，还可以通过ChannelFuture 的 channel() 方法获取关联的Channel。  

```java
public interface ChannelFuture extends Future<Void> {
    Channel channel();
    ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> var1);
    ChannelFuture sync() throws InterruptedException;
}
```
&emsp; 另外，还可以通过 ChannelFuture 接口的 sync()方法让异步的操作变成同步的。  

<!-- 
ChannelHandler 和 ChannelPipeline  
&emsp; 下面这段代码指定了序列化编解码器以及自定义的 ChannelHandler 处理消息。  

```java
b.group(eventLoopGroup)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                    ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                    ch.pipeline().addLast(new KryoClientHandler());
                }
            });
```
&emsp; ChannelHandler 是消息的具体处理器。他负责处理读写操作、客户端连接等事情。  
&emsp; ChannelPipeline 为 ChannelHandler 的链，提供了一个容器并定义了用于沿着链传播入站和出站事件流的 API 。当 Channel 被创建时，它会被自动地分配到它专属的 ChannelPipeline。  
&emsp; 可以在 ChannelPipeline 上通过 addLast() 方法添加一个或者多个ChannelHandler ，因为一个数据或者事件可能会被多个 Handler 处理。当一个 ChannelHandler 处理完之后就将数据交给下一个 ChannelHandler 。  
-->

## 1.5. ChannelHandler  


### 1.5.1. ChannelInboundHandler  

### 1.5.2. ChannelOutboundHandler   


### 1.5.3. ChannelHandlerAdapter  


## 1.6. ChannelPipeline  


## 1.7. ChannelHandlerContext  

### 1.7.1. 与ChannelHandler、ChannelPipeline的关联使用  


## 1.8. 核心组件之间的关系  

* 一个 Channel对应一个 ChannelPipeline
* 一个 ChannelPipeline 包含一条双向的 ChannelHandlerContext链
* 一个 ChannelHandlerContext中包含一个ChannelHandler
* 一个 Channel会绑定到一个EventLoop上
* 一个 NioEventLoop 维护了一个 Selector（使用的是 Java 原生的 Selector）
* 一个 NioEventLoop 相当于一个线程
