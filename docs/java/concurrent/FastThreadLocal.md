

# FastThreadLocal  
<!--
https://cloud.tencent.com/developer/article/1877813

https://blog.csdn.net/w13485673086/article/details/106529908

 FastThreadLocal 是什么鬼？吊打 ThreadLocal 的存在！！ 
 https://mp.weixin.qq.com/s/aItosqUu1aMvWqJ2ZMqy5Q
-->



## FastThreadLocal 为什么快?  
&emsp; 在 FastThreadLocal 内部，使用了索引常量代替了 Hash Code 和哈希表，源代码如下：  

```java
private final int index;

public FastThreadLocal() {
    index = InternalThreadLocalMap.nextVariableIndex();
}
```

```java
public static int nextVariableIndex() {
    int index = nextIndex.getAndIncrement();
    if (index < 0) {
        nextIndex.decrementAndGet();
        throw new IllegalStateException("too many thread-local indexed variables");
    }
    return index;
}
```
&emsp; FastThreadLocal 内部维护了一个索引常量 index，该常量在每次创建 FastThreadLocal 中都会自动+1，从而保证了下标的不重复性。  
&emsp; 这要做虽然会产生大量的 index，但避免了在 ThreadLocal  中计算索引下标位置以及处理 hash 冲突带来的损耗，所以在操作数组时使用固定下标要比使用计算哈希下标有一定的性能优势，特别是在频繁使用时会非常显著，用空间换时间，这就是高性能 Netty 的巧妙之处。  



&emsp; 要利用 FastThreadLocal 带来的性能优势，就必须结合使用 FastThreadLocalThread 线程类或其子类，因为 FastThreadLocalThread 线程类会存储必要的状态，如果使用了非 FastThreadLocalThread 线程类则会回到常规 ThreadLocal。  
&emsp; Netty 提供了继承类和实现接口的线程类：  




## FastThreadLocal 实战  


## 总结  
&emsp; Netty 中的 FastThreadLocal 在大量频繁读写操作时效率要高于 ThreadLocal，但要注意结合 Netty 自带的线程类使用，这可能就是 Netty 为什么高性能的奥妙之一吧！  
&emsp; 如果没有大量频繁读写操作的场景，JDK 自带的 ThreadLocal 足矣，并且性能还要优于 FastThreadLocal。  

