
<!-- TOC -->

- [1. LongAdder](#1-longadder)
    - [1.1. Volatile、AtomicInteger、LongAdder比较](#11-volatileatomicintegerlongadder比较)
    - [1.2. LongAdder和AtomicLong性能测试](#12-longadder和atomiclong性能测试)
    - [1.3. LongAdder为什么这么快？](#13-longadder为什么这么快)
    - [1.4. ~~源码分析~~](#14-源码分析)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; **<font color = "clime">LongAdder有一个全局变量volatile long base值，当并发不高的情况下都是通过CAS来直接操作base值，如果CAS失败，则针对LongAdder中的Cell[]数组中的Cell进行CAS操作，减少失败的概率。</font>**  
&emsp; 在 LongAdder 的父类 Striped64 中存在一个 volatile Cell[] cells; 数组，其长度是2 的幂次方，每个Cell都使用 @Contended 注解进行修饰，而@Contended注解可以进行缓存行填充，从而解决伪共享问题。  

# 1. LongAdder 
## 1.1. Volatile、AtomicInteger、LongAdder比较
&emsp; 阿里《Java开发手册》嵩山版：    
&emsp; 【参考】volatile 解决多线程内存不可见问题。对于一写多读，是可以解决变量同步问题，但是如果多写，同样无法解决线程安全问题。  
&emsp; 说明：如果是count++操作，使用如下类实现：AtomicInteger count = new AtomicInteger(); count.addAndGet(1); 如果是JDK8，推荐使用 LongAdder对象，比AtomicLong性能更好(减少乐观锁的重试次数)。  


&emsp; **Volatile、AtomicInteger、LongAdder：**  
1. volatile在多写环境下是非线程安全的。  
2. AtomicInteger循环时间长开销大。  
3. <font color = "clime">LongAdder会将这个原子变量分离成一个Cell数组，每个线程通过Hash获取到自己数组，这样就减少了乐观锁的重试次数，从而在高竞争下获得优势；</font>而在低竞争下表现的又不是很好，可能是因为自己本身机制的执行时间大于了锁竞争的自旋时间，因此在低竞争下表现性能不如AtomicInteger。  
&emsp; <font color = "clime">同时LongAdder也解决了[伪共享问题](/docs/java/concurrent/PseudoSharing.md)。</font>  

## 1.2. LongAdder和AtomicLong性能测试  
<!-- 
阿里为什么推荐使用LongAdder，而不是volatile？ 
https://mp.weixin.qq.com/s/lpk5l4m0oFpPDDf6fl8mmQ
-->

## 1.3. LongAdder为什么这么快？ 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-35.png)   
1. 设计思想上，LongAdder采用"分段"的方式降低CAS失败的频次。(以空间换时间)  
&emsp; 这里先简单的说下LongAdder的思路，后面还会详述LongAdder的原理。  
&emsp; AtomicLong中有个内部变量value保存着实际的long值，所有的操作都是针对该变量进行。也就是说，高并发环境下，value变量其实是一个热点数据，也就是N个线程竞争一个热点。  
&emsp; LongAdder的基本思路就是分散热点，将value值的新增操作分散到一个数组中，不同线程会命中到数组的不同槽中，各个线程只对自己槽中的那个value值进行CAS操作，这样热点就被分散了，冲突的概率就小很多。  
&emsp; **<font color = "clime">LongAdder有一个全局变量volatile long base值，当并发不高的情况下都是通过CAS来直接操作base值，如果CAS失败，则针对LongAdder中的Cell[]数组中的Cell进行CAS操作，减少失败的概率。</font>**  
&emsp; 例如当前类中base = 10，有三个线程进行CAS原子性的+1操作，线程一执行成功，此时base=11，线程二、线程三执行失败后开始针对于Cell[]数组中的Cell元素进行+1操作，同样也是CAS操作，此时数组index=1和index=2中Cell的value都被设置为了1。  
&emsp; 执行完成后，统计累加数据：sum = 11 + 1 + 1 = 13，利用LongAdder进行累加的操作就执行完了，流程图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-36.png)   
2. 使用Contended注解来消除伪共享  
&emsp; 在 LongAdder 的父类 Striped64 中存在一个 volatile Cell[] cells; 数组，其长度是2 的幂次方，每个Cell都使用 @Contended 注解进行修饰，而@Contended注解可以进行缓存行填充，从而解决伪共享问题。伪共享会导致缓存行失效，缓存一致性开销变大。  

```java
@sun.misc.Contended static final class Cell {

}
```
3. 惰性求值  
&emsp; LongAdder只有在使用longValue()获取当前累加值时才会真正的去结算计数的数据，longValue()方法底层就是调用sum()方法，对base和Cell数组的数据累加然后返回，做到数据写入和读取分离。  
&emsp; 而AtomicLong使用incrementAndGet()每次都会返回long类型的计数值，每次递增后还会伴随着数据返回，增加了额外的开销。  

<!-- 
&emsp; 即LongAdder每个线程拥有自己的槽，各个线程一般只对自己槽中的那个值进行CAS操作。  
&emsp; 比如有三个线程同时对value增加1，那么value = 1 + 1 + 1 = 3  
&emsp; 但是对于LongAdder来说，内部有一个base变量，一个Cell[]数组。  

    * base变量：非竞争条件下，直接累加到该变量上  
    * Cell[\]数组：竞争条件下，累加个各个线程自己的槽Cell[i]中  

&emsp;最终结果的计算是下面这个形式：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-37.png)   
-->

## 1.4. ~~源码分析~~  
<!--
https://www.bilibili.com/video/BV1KE411K7Ts?p=2&spm_id_from=pageDriver
https://www.bilibili.com/video/BV1KE411K7Ts?p=3&spm_id_from=pageDriver
比AtomicLong更优秀的LongAdder确定不来了解一下吗？ 
https://mp.weixin.qq.com/s/rJAIoZLe9lnEcTj3SmgIZw
https://blog.csdn.net/jiangtianjiao/article/details/103844801/
-->
