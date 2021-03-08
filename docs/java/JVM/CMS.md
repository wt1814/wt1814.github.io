
<!-- TOC -->

- [1. CMS](#1-cms)
    - [1.1. 简介](#11-简介)
    - [1.2. ~~回收流程~~](#12-回收流程)
        - [1.2.1. 《深入理解Java虚拟机》](#121-深入理解java虚拟机)
        - [1.2.2. 《实战JAVA虚拟机  JVM故障诊断与性能优化》](#122-实战java虚拟机--jvm故障诊断与性能优化)
            - [1.2.2.1. 初始标记](#1221-初始标记)
            - [1.2.2.2. 并发标记](#1222-并发标记)
            - [1.2.2.3. 预清理阶段](#1223-预清理阶段)
            - [1.2.2.4. 可终止的预处理](#1224-可终止的预处理)
            - [1.2.2.5. 重新标记](#1225-重新标记)
            - [1.2.2.6. 并发清理](#1226-并发清理)
            - [1.2.2.7. 并发重置](#1227-并发重置)
    - [1.3. ~~优点与缺点~~](#13-优点与缺点)
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
https://www.bilibili.com/read/cv6830986/
https://segmentfault.com/a/1190000020625913?utm_source=tag-newest
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-126.png)  
&emsp; CMS垃圾回收过程：  

1. 初始化标记(CMS-initial-mark)，标记root，会导致stw；  
2. 并发标记(CMS-concurrent-mark)，与用户线程同时运行；  
3. 预清理（CMS-concurrent-preclean），与用户线程同时运行；  
4. 重新标记(CMS-remark)，会导致stw；  
5. 并发清除(CMS-concurrent-sweep)，与用户线程同时运行；  
6. 调整堆大小，设置CMS在清理之后进行内存压缩，目的是清理内存中的碎片；  
7. 并发重置状态等待下次CMS的触发(CMS-concurrent-reset)，与用户线程同时运行；  

#### 1.2.2.1. 初始标记  
&emsp; 这是CMS中两次stop-the-world事件中的一次。这一步的作用是标记存活的对象，有两部分：  
1. 标记老年代中所有的GC Roots对象，如下图节点1；  
2. 标记年轻代中活着的对象引用到的老年代的对象（指的是年轻带中还存活的引用类型对象，引用指向老年代中的对象）如下图节点2、3；  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-127.png)  

#### 1.2.2.2. 并发标记
&emsp; 从“初始标记”阶段标记的对象开始找出所有存活的对象;  
&emsp; 因为是并发运行的，在运行期间会发生新生代的对象晋升到老年代、或者是直接在老年代分配对象、或者更新老年代对象的引用关系等等，对于这些对象，都是需要进行重新标记的，否则有些对象就会被遗漏，发生漏标的情况。为了提高重新标记的效率，该阶段会把上述对象所在的Card标识为Dirty，后续只需扫描这些Dirty Card的对象，避免扫描整个老年代；  
&emsp; 并发标记阶段只负责将引用发生改变的Card标记为Dirty状态，不负责处理；  
&emsp; 如下图所示，也就是节点1、2、3，最终找到了节点4和5。并发标记的特点是和应用程序线程同时运行。并不是老年代的所有存活对象都会被标记，因为标记的同时应用程序会改变一些对象的引用等。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-128.png)  
&emsp; 这个阶段因为是并发的容易导致concurrent mode failure。  

#### 1.2.2.3. 预清理阶段
&emsp; 前一个阶段已经说明，不能标记出老年代全部的存活对象，是因为标记的同时应用程序会改变一些对象引用，这个阶段就是用来处理前一个阶段因为引用关系改变导致没有标记到的存活对象的，它会扫描所有标记为Direty的Card  
&emsp; 如下图所示，在并发清理阶段，节点3的引用指向了6；则会把节点3的card标记为Dirty；  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-129.png)  
&emsp; 最后将6标记为存活,如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-130.png)  

#### 1.2.2.4. 可终止的预处理
&emsp; 这个阶段尝试着去承担下一个阶段Final Remark阶段足够多的工作。这个阶段持续的时间依赖好多的因素，由于这个阶段是重复的做相同的事情直到发生aboart的条件（比如：重复的次数、多少量的工作、持续的时间等等）之一才会停止。  
&emsp; ps:此阶段最大持续时间为5秒，之所以可以持续5秒，另外一个原因也是为了期待这5秒内能够发生一次ygc，清理年轻带的引用，是的下个阶段的重新标记阶段，扫描年轻带指向老年代的引用的时间减少；  

#### 1.2.2.5. 重新标记
&emsp; 这个阶段会导致第二次stop the word，该阶段的任务是完成标记整个年老代的所有的存活对象。  
&emsp; 这个阶段，重新标记的内存范围是整个堆，包含_young_gen和_old_gen。为什么要扫描新生代呢，因为对于老年代中的对象，如果被新生代中的对象引用，那么就会被视为存活对象，即使新生代的对象已经不可达了，也会使用这些不可达的对象当做cms的“gc root”，来扫描老年代； 因此对于老年代来说，引用了老年代中对象的新生代的对象，也会被老年代视作“GC ROOTS”:当此阶段耗时较长的时候，可以加入参数-XX:+CMSScavengeBeforeRemark，在重新标记之前，先执行一次ygc，回收掉年轻带的对象无用的对象，并将对象放入幸存带或晋升到老年代，这样再进行年轻带扫描时，只需要扫描幸存区的对象即可，一般幸存带非常小，这大大减少了扫描时间。  
&emsp; 由于之前的预处理阶段是与用户线程并发执行的，这时候可能年轻带的对象对老年代的引用已经发生了很多改变，这个时候，remark阶段要花很多时间处理这些改变，会导致很长stop the word，所以通常CMS尽量运行Final Remark阶段在年轻代是足够干净的时候。  
&emsp; 另外，还可以开启并行收集：-XX:+CMSParallelRemarkEnabled  

#### 1.2.2.6. 并发清理
&emsp; 通过以上5个阶段的标记，老年代所有存活的对象已经被标记并且现在要通过Garbage Collector采用清扫的方式回收那些不能用的对象了。  
&emsp; 这个阶段主要是清除那些没有标记的对象并且回收空间；  
&emsp; 由于CMS并发清理阶段用户线程还在运行着，伴随程序运行自然就还会有新的垃圾不断产生，这一部分垃圾出现在标记过程之后，CMS无法在当次收集中处理掉它们，只好留待下一次GC时再清理掉。这一部分垃圾就称为“浮动垃圾”。  

#### 1.2.2.7. 并发重置
&emsp; 这个阶段并发执行，重新设置CMS算法内部的数据结构，准备下一个CMS生命周期的使用。  


## 1.3. ~~优点与缺点~~  
<!-- 
https://blog.csdn.net/zqz_zqz/article/details/70568819
https://www.jianshu.com/p/86e358afdf17
-->
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