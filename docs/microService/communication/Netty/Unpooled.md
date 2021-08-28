



# 非池化内存分配  
<!-- 
https://blog.csdn.net/gaoliang1719/article/details/113787703
-->
&emsp; 非池化内存的分配由UnpooledByteBufAllocator负责，本文梳理下由其负责分配的堆内存和堆外内存如何实现的。  
&emsp; Netty在非池化堆内存分配上Java9与Java8以下版本有啥不同呢？Netty堆外内存回收默认机制使用JDK提供的Cleaner吗？  


## 堆内内存分配
&emsp; 下面这小段代码摘自UnpooledByteBufAllocator#newHeapBuffer，通过此方法分析非池化堆内存的分配。  

```java
@Override
protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
    return PlatformDependent.hasUnsafe() ?
    new InstrumentedUnpooledUnsafeHeapByteBuf(this, initialCapacity, maxCapacity) :
    new InstrumentedUnpooledHeapByteBuf(this, initialCapacity, maxCapacity);
}
```
&emsp; 解读：堆内内存分配由newHeapBuffer方法负责，如果平台支持Unsafe则创建InstrumentedUnpooledUnsafeHeapByteBuf，否则创建InstrumentedUnpooledHeapByteBuf，下图为非池化相关类图，分别从两个类UnpooledDirectByteBuf和UnpooledHeapByteBuf延伸开来。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-126.png)  
&emsp; 还是聚集到堆内存的分配上来，主要分析上图中红色部分。InstrumentedUnpooledUnsafeHeapByteBuf和InstrumentedUnpooledHeapByteBuf有啥区别？  

### InstrumentedUnpooledUnsafeHeapByteBuf  
&emsp; 下面看下InstrumentedUnpooledUnsafeHeapByteBuf其内存分配的行为allocateArray()。   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-127.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-128.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-129.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-130.png)  
&emsp; 注解@1 调用了父类UnpooledUnsafeHeapByteBuf的allocateArray()  
&emsp; 注解@2 父类UnpooledUnsafeHeapByteBuf调用了PlatformDependent#allocateUninitializedArray  
&emsp; 注解@3/@4  Java9以上版本：如果待分配的内存小于1K使用堆内存，待分配的内存大于等于1K使用堆外内存。  
&emsp; Java8以及以下版本全部在堆内存分配  
&emsp; 小结：使用InstrumentedUnpooledUnsafeHeapByteBuf进行内存分配时：  
&emsp; Java9以及以上版本：如果待分配的内存小于1K使用堆内存；待分配的内存大于等于1K使用堆外内存（调用底层PlatformDependent#allocateUninitializedArray）。  
&emsp; Java8以及以下版本：使用堆内存分配。  


### InstrumentedUnpooledHeapByteBuf  
&emsp; 下面为InstrumentedUnpooledHeapByteBuf的内存分配allocateArray().   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-131.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-132.png)  
&emsp; 注解@1 调用父类 UnpooledHeapByteBuf的内存分配  
&emsp; 注解@2 UnpooledHeapByteBuf的通过new byte直接在堆内存分配  
&emsp; 小结：InstrumentedUnpooledHeapByteBuf直接在堆内存分配空间。  


### 数据获取方式  
&emsp; UnpooledUnsafeHeapByteBuf数据获取    
&emsp; UnpooledUnsafeHeapByteBuf的数据获取方式getByte()  

```java
    @Override
    protected byte _getByte(int index) {
    return UnsafeByteBufUtil.getByte(array, index);
    }
```

&emsp; 该方法调用UnsafeByteBufUtil的getByte，跟进去看下  

```java
static byte getByte(byte[] array, int index) {
    return PlatformDependent.getByte(array, index);
}
```

&emsp; 底层通过UNSAFE.getByte这种地址+偏移量的方式获取内存中的数据。  

```java
static byte getByte(byte[] data, int index) {
    return UNSAFE.getByte(data, BYTE_ARRAY_BASE_OFFSET + index);
}
```

&emsp; UnpooledHeapByteBuf数据获取  
&emsp; UnpooledHeapByteBuf数据获取方式_getByte()  

```java
@Override
protected byte _getByte(int index) {
    return HeapByteBufUtil.getByte(array, index);
}
```
&emsp; 该方法调用HeapByteBufUtil.getByte，跟进去看下，即直接从数组中获取数据。  

```java
static byte getByte(byte[] memory, int index) {
        return memory[index];
    }
```
&emsp; 小结：UnpooledUnsafeHeapByteBuf通过UNSAFE.getByte这种地址+偏移量的方式获取内存中的数据；UnpooledHeapByteBuf通过数组直接从堆内存获取。  

## 堆外内存分配  

