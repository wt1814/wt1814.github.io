

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

 
<!-- 
很好的视频
https://www.bilibili.com/video/BV1ft4y1B74d?p=1

深入分析Buffer 
https://mp.weixin.qq.com/s/4zOLqsARGbrxOXXh24B_MQ

netty系列文章
https://www.cnblogs.com/dafanjoy/category/1306477.html

45 张图深度解析 Netty 架构与原理 
https://mp.weixin.qq.com/s/insjE_EJRoCOM-1GqgZP9A
Netty之旅二：口口相传的高性能Netty到底是什么？ 
https://mp.weixin.qq.com/s?__biz=Mzg5ODA5NDIyNQ==&mid=2247484812&idx=1&sn=52d38717da60683d671136f50009f4fd&chksm=c0668072f71109643fb4697d2ddcec1a1983544dc4e3497f2c2a6aff525c292d3d9f2bd30be0&scene=178&cur_album_id=1486063894363242498#rd
-->

<!-- 
服务的心跳机制与断线重连，Netty底层是怎么实现
https://mp.weixin.qq.com/s/TC7gQnxBoxvFh-iaQg28YQ


-->



------


9.Netty 发送消息有几种方式？  

Netty 有两种发送消息的方式：  

    直接写入 Channel 中，消息从 ChannelPipeline 当中尾部开始移动；  
    写入和 ChannelHandler 绑定的 ChannelHandlerContext 中，消息从 ChannelPipeline 中的下一个 ChannelHandler 中移动。  

10.默认情况 Netty 起多少线程？何时启动？  

Netty 默认是 CPU 处理器数的两倍，bind 完之后启动。  
