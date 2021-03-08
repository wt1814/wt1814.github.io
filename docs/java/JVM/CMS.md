
<!-- TOC -->

- [1. CMS](#1-cms)
    - [1.1. 简介](#11-简介)
    - [1.2. ~~回收流程~~](#12-回收流程)
        - [1.2.1. 《深入理解Java虚拟机》](#121-深入理解java虚拟机)
        - [1.2.2. 《实战JAVA虚拟机  JVM故障诊断与性能优化》](#122-实战java虚拟机--jvm故障诊断与性能优化)
    - [1.3. 优点与缺点](#13-优点与缺点)
    - [1.4. ~~concurrent mode failure & promotion failed问题~~](#14-concurrent-mode-failure--promotion-failed问题)
        - [1.4.1. 简介](#141-简介)
        - [1.4.2. 可能原因及解决方案](#142-可能原因及解决方案)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; **<font color = "clime">CMS在某些阶段是并发，即CMS GC时并不是全部并发执行。大部分并发，但也有停顿(STW)，只是停顿时间更少。因为CMS是并发收集器，为了不影响用户线程使用，所以采用标记-清除算法。</font>**   
&emsp; CMS GC执行流程：初始标记(标记GCRoots能直接关联到的对象) ---> 并发标记(进行GCRoots Tracing(可达性分析)过程，GC与用户线程并发执行) ---> 重新标记(修正并发标记期间，因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录) ---> 并发清除(并发、标记-清除，GC与用户线程并发执行)。  
&emsp; CMS缺点：1).吞吐量低。并发执行，线程切换。2).并发执行，产生浮动垃圾。3).使用"标记-清除"算法，产生碎片空间。  

&emsp; 晋升失败(promotion failed)：当新生代发生垃圾回收，老年代有足够的空间可以容纳晋升的对象，但是由于空闲空间的碎片化，导致晋升失败。此时会触发单线程且带压缩动作的Full GC。  
&emsp; 并发模式失败(concurrent mode failure)：当CMS在执行回收时，新生代发生垃圾回收，同时老年代又没有足够的空间容纳晋升的对象时。CMS垃圾回收会退化成单线程的Full GC。所有的应用线程都会被暂停，老年代中所有的无效对象都被回收。  


# 1. CMS  
<!--
CMS GC
https://mp.weixin.qq.com/s/WqfzZRlk2NMkNc5a_Yjpdw
好视频推荐：   
https://www.bilibili.com/video/BV1Jy4y127tb?from=search&seid=14273060492345757864
-->
## 1.1. 简介
&emsp; CMS(Conrrurent并发，Mark Sweep标记删除)收集器是以 **<font color = "clime">获取最短回收停顿时间为目标</font>** 的收集器。  
&emsp; **<font color = "clime">CMS在某些阶段是并发，即CMS GC时并不是全部并发执行。大部分并发，但也有停顿(STW)，只是停顿时间更少。因为CMS是并发收集器，为了不影响用户线程使用，所以采用标记-清除算法。</font>**   

&emsp; 参数控制：  

    -XX:+UseConcMarkSweepGC，使用CMS收集器
    -XX:+ UseCMSCompactAtFullCollection，Full GC后，进行一次碎片整理；整理过程是独占的，会引起停顿时间变长
    -XX:+CMSFullGCsBeforeCompaction，设置进行几次Full GC后，进行一次碎片整理-XX:ParallelCMSThreads设定CMS的线程数量(一般情况约等于可用CPU数量)

## 1.2. ~~回收流程~~
&emsp; CMS回收老年代，能与CMS搭配使用的新生代垃圾收集器有Serial收集器和ParNew收集器。  

### 1.2.1. 《深入理解Java虚拟机》
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-122.png)  
&emsp; 使用标记-清除算法，收集过程分为如下四步：  
1. 初始标记，标记GCRoots能直接关联到的对象，时间很短。  
2. 并发标记，进行GCRoots Tracing(可达性分析)过程，过程耗时较长但是不需要停顿用户线程，可以与垃圾收集线程一起并发运行。  
3. 重新标记，**修正并发标记期间，因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录(漏标或错标)，** 停顿时间通常会比初始标记阶段稍长一些，但也远比并发标记阶段的时间短。  
4. 并发清除，删除掉标记阶段判断的已经死亡的对象，由于不需要移动存活对象，所以这个阶段也是可以与用户线程同时并发的。  

&emsp; **<font color = "red">由于在整个过程中耗时最长的并发标记和并发清除阶段中，垃圾收集器线程都可以与用户线程一起工作，所以从总体上来说，CMS收集器的内存回收过程是与用户线程一起并发执行的。</font>**  

### 1.2.2. 《实战JAVA虚拟机  JVM故障诊断与性能优化》
<!-- 
https://blog.csdn.net/zqz_zqz/article/details/70568819
https://www.jianshu.com/p/86e358afdf17

https://www.bilibili.com/read/cv6830986/


https://segmentfault.com/a/1190000020625913?utm_source=tag-newest
https://zhuanlan.zhihu.com/p/139785849
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-126.png)  
CMS垃圾回收过程：  

初始化标记(CMS-initial-mark)，标记root，会导致stw；  
并发标记(CMS-concurrent-mark)，与用户线程同时运行；  
预清理（CMS-concurrent-preclean），与用户线程同时运行；  
重新标记(CMS-remark)，会导致stw；  
并发清除(CMS-concurrent-sweep)，与用户线程同时运行；  
调整堆大小，设置CMS在清理之后进行内存压缩，目的是清理内存中的碎片；  
并发重置状态等待下次CMS的触发(CMS-concurrent-reset)，与用户线程同时运行；  



## 1.3. 优点与缺点  
&emsp; CMS是一款优秀的收集器，它最主要的优点在名字上已经体现出来： **<font color = "clime">并发收集、低停顿。</font>** 但是也有以下 **<font color = "red">三个明显的缺点：</font>**  

* **<font color = "clime">吞吐量低</font>**    
&emsp; <font color = "red">由于CMS在垃圾收集过程使用用户线程和GC线程并行执行，从而 **【线程切换】** 会有额外开销，</font>因此CPU吞吐量就不如在GC过程中停止一切用户线程的方式来的高。
* **<font color = "clime">无法处理浮动垃圾，导致频繁Full GC</font>**  
&emsp; <font color = "red">由于垃圾清除过程中，用户线程和GC线程并发执行，也就是用户线程仍在执行，那么在执行过程中会产生垃圾，这些垃圾称为"浮动垃圾"。</font>  
&emsp; 如果CMS在垃圾清理过程中，用户线程需要在老年代中分配内存时发现空间不足，就需再次发起Full GC，而此时CMS正在进行清除工作，因此此时只能由Serial Old临时对老年代进行一次Full GC。  
* **<font color = "clime">使用"标记-清除"算法，产生碎片空间</font>**  
&emsp; 由于CMS使用了"标记-清除"算法，因此清除之后会产生大量的碎片空间，不利于空间利用率。不过CMS提供了应对策略：开启-XX:+UseCMSCompactAtFullCollection，开启该参数后，每次FullGC完成后都会进行一次内存压缩整理，将零散在各处的对象整理到一块儿。但每次都整理效率不高，因此提供了另外一个参数，设置参数-XX:CMSFullGCsBeforeCompaction，本参数告诉CMS，经过了N次Full GC过后再进行一次内存整理。  

## 1.4. ~~concurrent mode failure & promotion failed问题~~
<!-- 
https://www.jianshu.com/p/ca1b0d4107c5
https://www.cnblogs.com/fswhq/p/11767439.html
https://my.oschina.net/hosee/blog/674181

concurrent mode failure影响
老年代的垃圾收集器从CMS退化为Serial Old，所有应用线程被暂停，停顿时间变长。
-->
### 1.4.1. 简介
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-124.png)  
&emsp; 晋升失败：当新生代发生垃圾回收，老年代有足够的空间可以容纳晋升的对象，但是由于空闲空间的碎片化，导致晋升失败。此时会触发单线程且带压缩动作的Full GC。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-123.png)  
&emsp; 并发模式失败：当CMS在执行回收时，新生代发生垃圾回收，同时老年代又没有足够的空间容纳晋升的对象时。CMS 垃圾回收会退化成单线程的Full GC。所有的应用线程都会被暂停，老年代中所有的无效对象都被回收。  

### 1.4.2. 可能原因及解决方案
<!-- 
方法一是降低触发CMS的阀值，即参数-XX:CMSInitiatingOccupancyFraction的值，默认值是68，所以这里调低到50，让CMS GC尽早执行，以保证有足够的空间

并发模式失败和晋升失败都会导致长时间的停顿，常见解决思路如下：

    降低触发CMS GC的阈值，即参数-XX:CMSInitiatingOccupancyFraction的值，让CMS GC尽早执行，以保证有足够的空间
    增加CMS线程数，即参数-XX:ConcGCThreads，
    增大老年代空间
    让对象尽量在新生代回收，避免进入老年代
-->

* 原因1：CMS触发太晚  
&emsp; 方案：降低触发CMS GC的阈值，即参数-XX:CMSInitiatingOccupancyFraction的值，让CMS GC尽早执行，以保证有足够的空间；  
* 原因2：空间碎片太多  
&emsp; 方案：开启空间碎片整理，并将空间碎片整理周期设置在合理范围；  
&emsp; -XX:+UseCMSCompactAtFullCollection（空间碎片整理）  
&emsp; -XX:CMSFullGCsBeforeCompaction=n  
* 原因3：垃圾产生速度超过清理速度  
&emsp; 晋升阈值过小；  
&emsp; Survivor空间过小；  
&emsp; Eden区过小，导致晋升速率提高；  
&emsp; 存在大对象；  