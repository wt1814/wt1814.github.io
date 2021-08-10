


# 内存分配之分配器ByteBufAllocator
<!-- 
https://blog.csdn.net/u011212394/article/details/103984870
-->

&emsp; Netty中内存分配有一个最顶层的抽象就是ByteBufAllocator，负责分配所有ByteBuf 类型的内存。功能其实不是很多，主要有以下几个重要的API：  

```java
public interface ByteBufAllocator {
    /**分配一块内存，自动判断是否分配堆内内存或者堆外内存。
        * Allocate a {@link ByteBuf}. If it is a direct or heap buffer depends on the actual implementation.
        */
    ByteBuf buffer();/**尽可能地分配一块堆外直接内存，如果系统不支持则分配堆内内存。
        * Allocate a {@link ByteBuf}, preferably a direct buffer which is suitable for I/O.
        */
    ByteBuf ioBuffer();/**分配一块堆内内存。
        * Allocate a heap {@link ByteBuf}.
        */
    ByteBuf heapBuffer();/**分配一块堆外内存。
        * Allocate a direct {@link ByteBuf}.
        */
    ByteBuf directBuffer();/**组合分配，把多个ByteBuf 组合到一起变成一个整体。
        * Allocate a {@link CompositeByteBuf}.If it is a direct or heap buffer depends on the actual implementation.
        */
    CompositeByteBuf compositeBuffer();
}
```

&emsp; ByteBufAllocator的基本实现类AbstractByteBufAllocator。  
&emsp; 在AbstractByteBufAllocator#buffer()方法中做了判断，是否默认支持directBuffer，如果支持则分配directBuffer，否则分配heapBuffer。directBuffer()方法和heapBuffer()方法的实现逻辑几乎一致。  
&emsp; unsafe和非unsafe，Netty 自动判别，如果操作系统底层支持unsafe 那就采用unsafe读写，否则采用非unsafe读写。  

