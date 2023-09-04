

<!-- TOC -->

- [1. 网络IO/分布式通信](#1-网络io分布式通信)
    - [1.1. 通信基础](#11-通信基础)
    - [1.2. webSocket协议](#12-websocket协议)
    - [1.3. NIO](#13-nio)
    - [1.4. Netty通信框架](#14-netty通信框架)

<!-- /TOC -->

# 1. 网络IO/分布式通信
## 1.1. 通信基础
[通信基础](/docs/microService/communication/Netty/basics.md)   
&emsp; [序列化](/docs/microService/communication/serializbale.md)  
&emsp; [【网络IO】](/docs/microService/communication/NetworkIO.md)  
&emsp; &emsp; [服务器处理连接的架构演变](/docs/microService/communication/ProcessingLinks.md)  
&emsp; &emsp; [五种I/O模型](/docs/microService/communication/IO.md)  
&emsp; &emsp; [I/O多路复用epoll](/docs/microService/communication/Netty/epoll.md)  
&emsp; &emsp; [多路复用之Reactor模式](/docs/microService/communication/Netty/Reactor.md)  
&emsp; &emsp; [IO性能优化之零拷贝](/docs/microService/communication/Netty/zeroCopy.md)  
&emsp; &emsp; [IO性能优化之零拷贝重制](/docs/microService/communication/Netty/zeroCopyRemake.md)  
&emsp; [请求合并](/docs/webSocket/RequestMerge.md)  


## 1.2. webSocket协议  
[webSocket协议](/docs/webSocket/Summary.md)  
&emsp; [Socket编程](/docs/microService/communication/Socket.md)  
&emsp; [4种Web端即时通信](/docs/webSocket/LongPolling.md)  
&emsp; &emsp; [配置中心使用长轮询推送](/docs/webSocket/Configuration.md)  
&emsp; [WebSocket协议](/docs/webSocket/WebSocket.md)  
&emsp; [WebSocket编码](/docs/webSocket/WebSocketCode.md)  
&emsp; [IM系统](/docs/webSocket/IM.md)  


## 1.3. NIO 
[NIO](/docs/microService/communication/NIO.md)  
&emsp; [NIO Channel](/docs/microService/communication/NIO/Channel.md)  
&emsp; [NIO Buffer](/docs/microService/communication/NIO/Buffer.md)  
&emsp; [Java中的零拷贝](/docs/microService/communication/NIO/JavaZeroCopy.md)  
&emsp; [NIO Selector](/docs/microService/communication/NIO/Selector.md)  

## 1.4. Netty通信框架
[Netty总结](/docs/microService/communication/Netty/netty.md)   
[Netty介绍](/docs/microService/communication/Netty/concepts.md)  
[Netty运行流程介绍](/docs/microService/communication/Netty/operation.md)   
[Netty核心组件](/docs/microService/communication/Netty/components.md)   
&emsp; [Channel相关](/docs/microService/communication/Netty/channel.md)   
[Netty逻辑架构](/docs/microService/communication/Netty/Architecture.md)   
[Netty高性能](/docs/microService/communication/Netty/performance.md)  
&emsp; [Reactor与EventLoop](/docs/microService/communication/Netty/NettyReactor.md)  
&emsp; [~~Netty中的零拷贝~~](/docs/microService/communication/Netty/nettyZeroCopy.md)  
[Netty开发](/docs/microService/communication/Netty/Development.md)  
&emsp; [TCP粘拆包与Netty编解码](/docs/microService/communication/Netty/Decoder.md)  
&emsp; [Netty实战](/docs/microService/communication/Netty/actualCombat.md)  
&emsp; [Netty多协议开发](/docs/microService/communication/Netty/MultiProtocol.md)  
[Netty源码](/docs/microService/communication/Netty/source.md)    
&emsp; [Netty源码搭建](/docs/microService/communication/Netty/build.md)  
&emsp; [Netty服务端创建](/docs/microService/communication/Netty/principle.md)  
&emsp; [Netty客户端创建](/docs/microService/communication/Netty/customer.md)  
&emsp; [NioEventLoop](/docs/microService/communication/Netty/NioEventLoop.md)  
&emsp; &emsp; [NioEventLoop的启动](/docs/microService/communication/Netty/NioEventLoopStart.md)  
&emsp; &emsp; [NioEventLoop的执行](/docs/microService/communication/Netty/NioEventLoopRun.md)  
&emsp; [内存分配-ByteBuf](/docs/microService/communication/Netty/byteBuf.md)    
&emsp; &emsp; [内存分配-ByteBuf](/docs/microService/communication/Netty/byteBufIntro.md)    
&emsp; &emsp; [内存分配-分配器ByteBufAllocator](/docs/microService/communication/Netty/ByteBufAllocator.md)    
&emsp; &emsp; [内存分配-非池化内存分配](/docs/microService/communication/Netty/Unpooled.md)    
&emsp; &emsp; [~~内存分配-池化内存分配~~](/docs/microService/communication/Netty/Pooled.md)    
&emsp; &emsp; [池化内存分配](/docs/microService/communication/Netty/byteBufTwo.md)    

