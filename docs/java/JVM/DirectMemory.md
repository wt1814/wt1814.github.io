

<!-- TOC -->

- [1. 直接内存](#1-直接内存)
    - [1.1. 直接内存](#11-直接内存)
    - [1.2. 直接内存的回收](#12-直接内存的回收)

<!-- /TOC -->


# 1. 直接内存  
<!-- 

https://blog.csdn.net/Ethan_199402/article/details/110431404
-->

## 1.1. 直接内存  
&emsp; 直接内存：直接内存主要被 Java NIO 使用，某种程度上也就是指DirectByteBuffer对象占用的堆外内存。  


## 1.2. 直接内存的回收
&emsp; 需注意堆外内存并不直接控制于JVM，这些内存只有在DirectByteBuffer回收掉之后才有机会被回收，而 Young GC 的时候只会将年轻代里不可达的DirectByteBuffer对象及其直接内存回收，如果这些对象大部分都晋升到了年老代，那么只能等到Full GC的时候才能彻底地回收DirectByteBuffer对象及其关联的堆外内存。因此，堆外内存的回收依赖于 Full GC。  

