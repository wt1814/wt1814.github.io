


# 内存
<!--
https://mp.weixin.qq.com/s/QIK1N-ePm6DQE4tMQ9N3Gw
kafka——高效读写数据
https://www.jianshu.com/p/ce8253609b6b
-->
&emsp; 为了优化读写性能，Kafka利用了操作系统本身的Page Cache，就是利用操作系统自身的内存而不是JVM空间内存。这样做的好处有：  

* 避免Object消耗：如果是使用 Java 堆，Java对象的内存消耗比较大，通常是所存储数据的两倍甚至更多。
* 避免GC问题：随着JVM中数据不断增多，垃圾回收将会变得复杂与缓慢，使用系统缓存就不会存在GC问题

&emsp; 相比于使用JVM或in-memory cache等数据结构，利用操作系统的Page Cache更加简单可靠。

&emsp; 首先，操作系统层面的缓存利用率会更高，因为存储的都是紧凑的字节结构而不是独立的对象。  
&emsp; 其次，操作系统本身也对于Page Cache做了大量优化，提供了 write-behind、read-ahead以及flush等多种机制。  
&emsp; 再者，即使服务进程重启，系统缓存依然不会消失，避免了in-process cache重建缓存的过程。  

&emsp; 通过操作系统的Page Cache，Kafka的读写操作基本上是基于内存的，读写速度得到了极大的提升。  

