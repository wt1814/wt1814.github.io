
<!-- TOC -->

- [1. Netty内存管理](#1-netty内存管理)
    - [1.1. ByteBuf](#11-bytebuf)
        - [1.1.1. ByteBuf的基本结构](#111-bytebuf的基本结构)
        - [1.1.2. ByteBuf的重要API](#112-bytebuf的重要api)
        - [1.1.3. ByteBuf的基本分类](#113-bytebuf的基本分类)
        - [1.1.4. ★★★~~内存使用~~](#114-★★★内存使用)
    - [1.2. ByteBufAllocator内存管理器](#12-bytebufallocator内存管理器)
    - [1.3. 非池化内存分配](#13-非池化内存分配)
    - [1.4. 池化内存的分配](#14-池化内存的分配)

<!-- /TOC -->


# 1. Netty内存管理
<!--

谈谈Netty内存管理 ！ 
https://mp.weixin.qq.com/s/wh9sBajNczrKWtexf1beUA
https://www.cnblogs.com/rickiyang/p/13100413.html
https://blog.csdn.net/ClarenceZero/article/details/112971237



https://blog.csdn.net/m0_37556444/article/details/107959226
https://www.cnblogs.com/rickiyang/p/13253203.html
https://blog.csdn.net/qq_41652863/article/details/99095769?utm_source=app&app_version=4.12.0&code=app_1562916241&uLinkId=usr1mkqgl919blen
https://blog.csdn.net/ClarenceZero/article/details/112999320?utm_medium=distribute.wap_relevant.none-task-blog-2~default~baidujs_title~default-6.essearch_wap_relevant
https://zhuanlan.zhihu.com/p/100239049


Netty内存管理
https://mp.weixin.qq.com/s/6RWwwnXwf4c84BQYy2_6fQ
Netty内存管理算法
https://blog.csdn.net/ClarenceZero/article/details/112971237


-->

&emsp; 参考《Netty4核心原理与手写RPC框架实战》  

## 1.1. ByteBuf
&emsp; ByteBuf是Netty整个结构里面最为底层的模块，主要负责把数据从底层I/O读到ByteBuf，然后传递给应用程序，应用程序处理完成之后再把数据封装成ByteBuf写回I/O。所以，ByteBuf是直接与底层打交道的一层抽象。  

### 1.1.1. ByteBuf的基本结构


### 1.1.2. ByteBuf的重要API
&emsp; ByteBuf的基本API主要包括read()、write()、set()以及mark()、reset()方法。  

### 1.1.3. ByteBuf的基本分类
<!-- 
https://mp.weixin.qq.com/s/qSiExDGODbj4NXJQNcKpdA
-->
&emsp; AbstractByteBuf 之下有众多子类，大致可以从三个维度来进行分类，分别如下：

* Pooled：池化内存，就是从预先分配好的内存空间中提取一段连续内存封装成一个ByteBuf分给应用程序使用。  
* Unsafe：是JDK 底层的一个负责IO 操作的对象，可以直接拿到对象的内存地址，基于内存地址进行读写操作。  
* Direct：堆外内存，是直接调用JDK 的底层API 进行物理内存分配，不在JVM 的堆内存中，需要手动释放。  


    堆外直接内存的优势：Java 网络程序中使用堆外直接内存进行内容发送（Socket读写操作），可以避免了字节缓冲区的二次拷贝；相反，如果使用传统的堆内存（Heap Memory，其实就是byte[]）进行Socket读写，JVM会将堆内存Buffer拷贝一份到堆外直接内存中，然后才写入Socket中。这样，相比于堆外直接内存，消息在发送过程中多了一次缓冲区的内存拷贝。

&emsp; 综上所述，其实ByteBuf 一共会有六种组合：Pooled 池化内存和Unpooled 非池化内存；Unsafe和非Unsafe；Heap堆内存和Direct 堆外内存。下图是ByteBuf 最重要的继承关系类结构图，通过命名就能一目了然：

&emsp; ByteBuf最基本的读写API 操作在AbstractByteBuf 中已经实现了，其众多子类采用不同的策略来分配内存空间，下面对重要的几个子类总结如下：  

* PooledHeapByteBuf：池化的堆内缓冲区
* PooledUnsafeHeapByteBuf：池化的Unsafe堆内缓冲区
* PooledDirectByteBuf：池化的直接(堆外)缓冲区
* PooledUnsafeDirectByteBuf：池化的Unsafe直接(堆外)缓冲区
* UnpooledHeapByteBuf：非池化的堆内缓冲区
* UnpooledUnsafeHeapByteBuf：非池化的Unsafe堆内缓冲区
* UnpooledDirectByteBuf：非池化的直接(堆外)缓冲区
* UnpooledUnsafeDirectByteBuf：非池化的Unsafe直接(堆外)缓冲区


### 1.1.4. ★★★~~内存使用~~
&emsp; **<font color = "clime">默认情况下，安卓平台使用非池实现，其他平台使用内存池实现。</font>**  

1. 堆外内存  
&emsp; 堆内内存 / 堆外内存的切换方式，指定参数：`io.netty.noPreferDirect = true / false`  
&emsp; 默认不使用堆内内存的，可以这样指定使用堆内内存
```java
ServerBootstrap b = new ServerBootstrap();
b.childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(false))
```
&emsp; Netty分配堆外内存的本质是调用JDK的`ByteBuffer.allocateDirect(initialCapacity);`方法，再往下就是JDK的Unsafe了。

2. 内存池  
&emsp; 内存池 / 非内存池 的切换方式，指定参数：`io.netty.allocator.type = unpooled / pooled`  
&emsp; 启动类中指定配置：  
```java
ServerBootstrap b = new ServerBootstrap();
b.childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);
```
&emsp; 或
```java
ServerBootstrap b = new ServerBootstrap();
b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
```

## 1.2. ByteBufAllocator内存管理器
[内存分配-分配器ByteBufAllocator](/docs/microService/communication/Netty/ByteBufAllocator.md)   

## 1.3. 非池化内存分配
[内存分配-非池化内存分配](/docs/microService/communication/Netty/Unpooled.md)    

## 1.4. 池化内存的分配  
[内存分配-非池化内存分配](/docs/microService/communication/Netty/Pooled.md)   



