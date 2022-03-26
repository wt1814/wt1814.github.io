
# Netty中的零拷贝  
<!-- 
视频
https://www.bilibili.com/video/BV17t41137su?p=38

netty源码书
https://segmentfault.com/a/1190000007560884

https://mp.weixin.qq.com/s/xY-hJl5qJv3QtttcMqHZRQ

-->

&emsp; 前言：[IO性能优化之零拷贝](/docs/microService/communication/Netty/zeroCopy.md)  

&emsp; Netty的零拷贝主要体现在如下几个方面  
&emsp; **(1)Netty接收和发送ByteBuffer采用DirectBuffer，使用堆外直接内存进行Socket读写，不需要进行字节缓冲区的二次拷贝。** 如果使用传统的堆存(Heap Buffer)进行Socket读写，那么JVM会将堆存拷贝一份到直接内存中，然后才写入Socket。相比于堆外直接内存，消息在发送过程中多了一次缓冲区的内存拷贝。  
&emsp; (2)Netty提供了组合Buffer对象，可以聚合多个ByteBuffer对象，用户可以像操作一个Buffer那样方便地对组合Buffer进行操作，避免了传统的通过内存拷贝的方式将几个小Buffer合并成一个大Buffer大烦琐操作。  
&emsp; **(3)Netty中文件传输采用了transferTo()方法，它可以直接将文件缓冲区的数据发送到目标Channel，** 避免了传统通过循环write()方式导致的内存拷贝问题。  

-----


* Netty 提供了 CompositeByteBuf 类， 它可以将多个 ByteBuf 合并为一个逻辑上的 ByteBuf， 避免了各个 ByteBuf 之间的拷贝。  
* ByteBuf 支持 slice 操作， 因此可以将 ByteBuf 分解为多个共享同一个存储区域的 ByteBuf， 避免了内存的拷贝。  
* 通过 wrap 操作， 可以将 byte[] 数组、ByteBuf、ByteBuffer等包装成一个 Netty ByteBuf 对象， 进而避免了拷贝操作。  
（ 通过 FileRegion 包装的FileChannel.tranferTo 实现文件传输， 可以直接将文件缓冲区的数据发送到目标 Channel， 避免了传统通过循环 write 方式导致的内存拷贝问题。  

----------
&emsp; netty提供了零拷贝的buffer，在传输数据时，最终处理的数据会需要对单个传输的报文，进行组合和拆分，Nio原生的ByteBuffer无法做到，netty通过提供的Composite(组合)和Slice(拆分)两种buffer来实现零拷贝；看下面一张图会比较清晰：  
![image](http://www.wt1814.com/static/view/images/microService/netty/netty-89.png)  
&emsp; TCP层HTTP报文被分成了两个ChannelBuffer，这两个Buffer对我们上层的逻辑(HTTP处理)是没有意义的。但是两个ChannelBuffer被组合起来，就成为了一个有意义的HTTP报文，这个报文对应的ChannelBuffer，才是能称之为”Message”的东西，这里用到了一个词“Virtual Buffer”。可以看一下netty提供的CompositeChannelBuffer源码：  

```javva
public class CompositeChannelBuffer extends AbstractChannelBuffer {

    private final ByteOrder order;
    private ChannelBuffer[] components;
    private int[] indices;
    private int lastAccessedComponentId;
    private final boolean gathering;
    
    public byte getByte(int index) {
        int componentId = componentId(index);
        return components[componentId].getByte(index - indices[componentId]);
    }
    //......
}
```
&emsp; components用来保存的就是所有接收到的buffer，indices记录每个buffer的起始位置，lastAccessedComponentId记录上一次访问的ComponentId；CompositeChannelBuffer并不会开辟新的内存并直接复制所有ChannelBuffer内容，而是直接保存了所有ChannelBuffer的引用，并在子ChannelBuffer里进行读写，实现了零拷贝。  

----------

Netty 的零拷贝主要包含三个方面：  

* Netty 的接收和发送 ByteBuffer 采用 DIRECT BUFFERS，使用堆外直接内存进行 Socket 读写，不需要进行字节缓冲区的二次拷贝。如果使用传统的堆内存（HEAP BUFFERS）进行 Socket 读写，JVM 会将堆内存 Buffer 拷贝一份到直接内存中，然后才写入 Socket 中。相比于堆外直接内存，消息在发送过程中多了一次缓冲区的内存拷贝。  
* Netty 提供了组合 Buffer 对象，可以聚合多个 ByteBuffer 对象，用户可以像操作一个 Buffer 那样方便的对组合 Buffer 进行操作，避免了传统通过内存拷贝的方式将几个小 Buffer 合并成一个大的 Buffer。  
* Netty 的文件传输采用了 transferTo 方法，它可以直接将文件缓冲区的数据发送到目标 Channel，避免了传统通过循环 write 方式导致的内存拷贝问题。

