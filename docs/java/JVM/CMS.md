
<!-- TOC -->

- [1. ~~CMS~~](#1-cms)
    - [1.1. 简介](#11-简介)
    - [1.2. ~~回收流程~~](#12-回收流程)
        - [1.2.1. 《深入理解Java虚拟机》](#121-深入理解java虚拟机)
        - [1.2.2. 《实战JAVA虚拟机  JVM故障诊断与性能优化》](#122-实战java虚拟机--jvm故障诊断与性能优化)
            - [1.2.2.1. 初始标记](#1221-初始标记)
            - [1.2.2.2. 并发标记](#1222-并发标记)
            - [1.2.2.3. 预处理阶段](#1223-预处理阶段)
                - [1.2.2.3.1. 预清理阶段](#12231-预清理阶段)
                - [1.2.2.3.2. 可终止的预处理](#12232-可终止的预处理)
            - [1.2.2.4. 重新标记](#1224-重新标记)
            - [1.2.2.5. 并发清理](#1225-并发清理)
            - [1.2.2.6. 并发重置](#1226-并发重置)
    - [1.3. ~~优点与问题~~](#13-优点与问题)
        - [1.3.1. 减少remark阶段停顿](#131-减少remark阶段停顿)
        - [1.3.2. ~~内存碎片问题解决~~](#132-内存碎片问题解决)
        - [1.3.3. ~~concurrent mode failure & promotion failed问题~~](#133-concurrent-mode-failure--promotion-failed问题)
            - [1.3.3.1. 简介](#1331-简介)
            - [1.3.3.2. 可能原因及解决方案](#1332-可能原因及解决方案)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "clime">CMS在某些阶段是并发，即CMS GC时并不是全部并发执行。大部分并发，但也有停顿(STW)，只是停顿时间更少。因为CMS是并发收集器，为了不影响用户线程使用，所以采用标记-清除算法。</font>**   
2. CMS GC执行流程：(**<font color = "clime">3次标记、2次清除</font>**)  
    1. 初始标记：标记GCRoots能直接关联到的对象。   
    2. 并发标记：进行GCRoots Tracing（可达性分析）过程，GC与用户线程并发执行。
    3. 预清理：（`三色标记法的漏标问题处理`） **<font color = "red">这个阶段是用来</font><font color = "blue">处理</font><font color = "clime">前一个并发标记阶段因为引用关系改变导致没有标记到的存活对象的。如果发现对象的引用发生变化，则JVM会标记堆的这个区域为Dirty Card。那些能够从Dirty Card到达的对象也被标记（标记为存活），当标记做完后，这个Dirty Card区域就会消失。</font>**  
    4. 可终止的预处理。这个阶段尝试着去承担下一个阶段Final Remark阶段足够多的工作。  
    5. 重新标记（remark）：修正并发标记期间，因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录。会触发stop the word。  
    6. 并发清除：并发、标记-清除，GC与用户线程并发执行。   
    7. 并发重置。
3. `CMS的特点：`  
    1. 划时代的并发收集器。`关注停顿时间。`    
    2. `吞吐量低。`并发执行，线程切换。  
    3. **<font color = "blue">并发执行，`产生浮动垃圾（参考三色标记法中“错标”）`。</font>**  
    4. 使用"标记-清除"算法，产生空间碎片。CMS GC在老生代回收时产生的内存碎片会导致老生代的利用率变低；或者可能在老生代总内存大小足够的情况下，却不能容纳新生代的晋升行为（由于没有连续的内存空间可用），导致触发FullGC。  
        &emsp; 针对这个问题，`Sun官方给出了以下解决内存碎片问题的方法：`  
        * 增大Xmx或者减少Xmn  
        * `在应用访问量最低的时候，在程序中主动调用System.gc()，比如每天凌晨。`  
        * 在应用启动并完成所有初始化工作后，主动调用System.gc()，它可以将初始化的数据压缩到一个单独的chunk中，以腾出更多的连续内存空间给新生代晋升使用。  
        * `降低-XX:CMSInitiatingOccupancyFraction参数（内存占用率，默认70%）以提早执行CMS GC动作，`虽然CMSGC不会进行内存碎片的压缩整理，但它会合并老生代中相邻的free空间。这样就可以容纳更多的新生代晋升行为。 
        * CMS收集器提供了一个-XX：+UseCMS-CompactAtFullCollection开关参数（默认是开启的，此参数从JDK 9开始废弃），用于在CMS收集器不得不进行Full GC时开启内存碎片的合并整理过程。`还提供了另外一个参数-XX：CMSFullGCsBefore-Compaction（此参数从JDK 9开始废弃），这个参数的作用是要求CMS收集器在执行过若干次（数量由参数值决定）不整理空间的Full GC之后，下一次进入Full GC前会先进行碎片整理（默认值为0，表示每次进入Full GC时都进行碎片整理）。`  
    5. `并发模式失败（CMS垃圾回收）与晋升失败（新生代垃圾回收）`：都会退化成单线程的Full GC。  
        * 并发模式失败(concurrent mode failure)：`当CMS在执行回收时`，新生代发生垃圾回收，同时老年代又`没有足够的空间`容纳晋升的对象时。CMS垃圾回收会退化成单线程的Full GC。所有的应用线程都会被暂停，老年代中所有的无效对象都被回收。  
        * 晋升失败(promotion failed)：`当新生代发生垃圾回收`， **老年代`有足够的空间`可以容纳晋升的对象，但是由于空闲空间的`碎片化`，导致晋升失败。** ~此时会触发单线程且带压缩动作的Full GC。~  
    6. 减少remark阶段停顿：在执行并发操作之前先做一次Young GC。  


# 1. ~~CMS~~  
<!--

*** https://www.jianshu.com/p/2a1b2f17d3e4
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
![image](http://182.92.69.8:8081/img/java/JVM/JVM-122.png)  
&emsp; 使用标记-清除算法，收集过程分为如下四步：  
1. 初始标记，标记GCRoots能直接关联到的对象，时间很短。  
2. 并发标记，进行GCRoots Tracing(可达性分析)过程，过程耗时较长但是不需要停顿用户线程，可以与垃圾收集线程一起并发运行。  
3. 重新标记，**修正并发标记期间，因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录(漏标或错标)，** 停顿时间通常会比初始标记阶段稍长一些，但也远比并发标记阶段的时间短。  
4. 并发清除，删除掉标记阶段判断的已经死亡的对象，由于不需要移动存活对象，所以这个阶段也是可以与用户线程同时并发的。  

&emsp; **<font color = "red">由于在整个过程中耗时最长的并发标记和并发清除阶段中，垃圾收集器线程都可以与用户线程一起工作，所以从总体上来说，CMS收集器的内存回收过程是与用户线程一起并发执行的。</font>**  

### 1.2.2. 《实战JAVA虚拟机  JVM故障诊断与性能优化》
<!-- 
*** https://www.bilibili.com/read/cv7696168/
https://www.bilibili.com/read/cv6830986/
https://segmentfault.com/a/1190000020625913?utm_source=tag-newest

-->
![image](http://182.92.69.8:8081/img/java/JVM/JVM-131.png)  
<center>CMS的整体流程</center>  

![image](http://182.92.69.8:8081/img/java/JVM/JVM-126.png)  
&emsp; CMS垃圾回收过程：  

1. 初始化标记(CMS-initial-mark)，标记root，会导致stw；  
2. 并发标记(CMS-concurrent-mark)，与用户线程同时运行；  
3. 预清理(CMS-concurrent-preclean)，与用户线程同时运行；  
4. 重新标记(CMS-remark)，会导致stw；  
5. 并发清除(CMS-concurrent-sweep)，与用户线程同时运行；  
6. 调整堆大小，设置CMS在清理之后进行内存压缩，目的是清理内存中的碎片；  
7. 并发重置状态等待下次CMS的触发(CMS-concurrent-reset)，与用户线程同时运行；  

#### 1.2.2.1. 初始标记  
&emsp; **<font color = "clime">这是CMS中两次stop-the-world事件中的一次。</font>** 这一步的作用是标记存活的对象，有两部分：  
1. 标记老年代中所有的GC Roots对象，如下图节点1；  
2. 标记年轻代中活着的对象引用到的老年代的对象（指的是年轻代中还存活的引用类型对象，引用指向老年代中的对象）如下图节点2、3；  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-127.png)  

#### 1.2.2.2. 并发标记
&emsp; 从“初始标记”阶段标记的对象开始找出所有存活的对象;  
&emsp; 因为是并发运行的，在运行期间会发生新生代的对象晋升到老年代、或者是直接在老年代分配对象、或者更新老年代对象的引用关系等等，对于这些对象，都是需要进行重新标记的，否则有些对象就会被遗漏，发生漏标的情况。为了提高重新标记的效率，该阶段会把上述对象所在的Card标识为Dirty，后续只需扫描这些Dirty Card的对象，避免扫描整个老年代；  
&emsp; **<font color = "clime">并发标记阶段只负责将引用发生改变的Card标记为Dirty状态，不负责处理；</font>**  
&emsp; 如下图所示，也就是节点1、2、3，最终找到了节点4和5。并发标记的特点是和应用程序线程同时运行。并不是老年代的所有存活对象都会被标记，因为标记的同时应用程序会改变一些对象的引用等。  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-128.png)  
&emsp; 这个阶段因为是并发的容易导致concurrent mode failure。  

#### 1.2.2.3. 预处理阶段
&emsp; **<font color = "clime">这个阶段的目的都是为了减轻后面的重新标记的压力，提前做一点重新标记阶段的工作。</font>**  
&emsp; 预处理阶段其实有2部分：预清理阶段、可终止的预处理。  

<!-- 

在整个CMS回收过程中，默认情况下，在并发标记之后，会有一个预清理的操作（也可 以关闭开关-XX:-CMSPredeaningEnabled,不进行预清理）。预清理是并发的，除了为正式清理 做准备和检査以外，预清理还会尝试控制一次停顿时间。由于重新标记是独占CPU的，如果新 生代GC发生后，立即触发一次重新标记，那么一次停顿时间可能很长。为了避免这种情况， 预处理时，会刻意等待一次新生代GC的发生，然后根据历史性能数据预测下一次新生代GC 可能发生的时间，然后在当前时间和预测时间的中间时刻，进行重新标记。这样，从最大程度 上避免新生代GC和重新标记重合，尽可能减少一次停顿时间。


1、首先，CMS是一个关注停顿时间，以回收停顿时间最短为目标的垃圾回收器。并发预处理阶段做的工作是标记，重标记需要STW（Stop The World），因此重标记的工作尽可能多的在并发阶段完成来减少STW的时间。此阶段标记从新生代晋升的对象、新分配到老年代的对象以及在并发阶段被修改了的对象。
2、并发可中断预清理(Concurrent precleaning)是标记在并发标记阶段引用发生变化的对象，如果发现对象的引用发生变化，则JVM会标记堆的这个区域为Dirty Card。那些能够从Dirty Card到达的对象也被标记（标记为存活），当标记做完后，这个Dirty Card区域就会消失。CMS有两个参数：CMSScheduleRemarkEdenSizeThreshold、CMSScheduleRemarkEdenPenetration，默认值分别是2M、50%。两个参数组合起来的意思是预清理后，eden空间使用超过2M时启动可中断的并发预清理（CMS-concurrent-abortable-preclean），直到eden空间使用率达到50%时中断，进入重新标记阶段。
-->

##### 1.2.2.3.1. 预清理阶段
&emsp; 前一个阶段已经说明，不能标记出老年代全部的存活对象，是因为标记的同时应用程序会改变一些对象引用， **<font color = "red">这个阶段就是用来</font><font color = "blue">处理</font><font color = "clime">前一个阶段因为引用关系改变导致没有标记到的存活对象的，它会扫描所有标记为Direty的Card。</font>**  
&emsp; 如下图所示，在并发清理阶段，节点3的引用指向了6；则会把节点3的card标记为Dirty；  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-129.png)  
&emsp; 最后将6标记为存活，如下图所示：  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-130.png)  

--------

&emsp; 通过参数CMSPrecleaningEnabled选择关闭该阶段，默认启用，主要做两件事情：  

* 处理新生代已经发现的引用，比如在并发阶段，在Eden区中分配了一个A对象，A对象引用了一个老年代对象B（这个B之前没有被标记），在这个阶段就会标记对象B为活跃对象。  
* 在并发标记阶段，如果老年代中有对象内部引用发生变化，会把所在的Card标记为Dirty（其实这里并非使用CardTable，而是一个类似的数据结构，叫ModUnionTalble），通过扫描这些Table，重新标记那些在并发标记阶段引用被更新的对象（晋升到老年代的对象、原本就在老年代的对象）  

---------
&emsp; 并发可中断预清理(Concurrent precleaning)是标记在并发标记阶段引用发生变化的对象，如果发现对象的引用发生变化，则JVM会标记堆的这个区域为Dirty Card。那些能够从Dirty Card到达的对象也被标记（标记为存活），当标记做完后，这个Dirty Card区域就会消失。CMS有两个参数：CMSScheduleRemarkEdenSizeThreshold、CMSScheduleRemarkEdenPenetration，默认值分别是2M、50%。两个参数组合起来的意思是预清理后，eden空间使用超过2M时启动可中断的并发预清理（CMS-concurrent-abortable-preclean），直到eden空间使用率达到50%时中断，进入重新标记阶段。  

##### 1.2.2.3.2. 可终止的预处理
&emsp; **这个阶段尝试着去承担下一个阶段Final Remark阶段足够多的工作。** 这个阶段持续的时间依赖好多的因素，由于这个阶段是重复的做相同的事情直到发生aboart的条件（比如：重复的次数、多少量的工作、持续的时间等等）之一才会停止。  
&emsp; ps：此阶段最大持续时间为5秒，之所以可以持续5秒，另外一个原因也是为了期待这5秒内能够发生一次ygc，清理年轻代的引用，是为下个阶段的重新标记阶段，扫描年轻带指向老年代的引用的时间减少；  

----------

&emsp; 该阶段发生的前提是，新生代Eden区的内存使用量大于参数CMSScheduleRemarkEdenSizeThreshold 默认是2M，如果新生代的对象太少，就没有必要执行该阶段，直接执行重新标记阶段。  

&emsp; 为什么需要这个阶段，存在的价值是什么？  

&emsp; 因为CMS GC的终极目标是降低垃圾回收时的暂停时间，所以在该阶段要尽最大的努力去处理那些在并发阶段被应用线程更新的老年代对象，这样在暂停的重新标记阶段就可以少处理一些，暂停时间也会相应的降低。  

&emsp; 在该阶段，主要循环的做两件事：  

* 处理 From 和 To 区的对象，标记可达的老年代对象  
* 和上一个阶段一样，扫描处理Dirty Card中的对象  

&emsp; 当然了，这个逻辑不会一直循环下去，打断这个循环的条件有三个：  

* 可以设置最多循环的次数 CMSMaxAbortablePrecleanLoops，默认是0，意思没有循环次数的限制。  
* 如果执行这个逻辑的时间达到了阈值CMSMaxAbortablePrecleanTime，默认是5s，会退出循环。  
* 如果新生代Eden区的内存使用率达到了阈值CMSScheduleRemarkEdenPenetration，默认50%，会退出循环。（这个条件能够成立的前提是，在进行Precleaning时，Eden区的使用率小于十分之一）  

&emsp; 如果在循环退出之前，发生了一次YGC，对于后面的Remark阶段来说，大大减轻了扫描年轻代的负担，但是发生YGC并非人为控制，所以只能祈祷这5s内可以来一次YGC。  

```text
...
1678.150: [CMS-concurrent-preclean-start]
1678.186: [CMS-concurrent-preclean: 0.044/0.055 secs]
1678.186: [CMS-concurrent-abortable-preclean-start]
1678.365: [GC 1678.465: [ParNew: 2080530K->1464K(2044544K), 0.0127340 secs] 
1389293K->306572K(2093120K), 
0.0167509 secs]
1680.093: [CMS-concurrent-abortable-preclean: 1.052/1.907 secs]  
....
```

&emsp; 在上面GC日志中，1678.186启动了AbortablePreclean阶段，在随后不到2s就发生了一次YGC。  


#### 1.2.2.4. 重新标记
&emsp; 这个阶段会导致第二次stop the word，该阶段的任务是完成标记整个年老代的所有的存活对象。  
&emsp; 这个阶段，重新标记的内存范围是整个堆，包含_young_gen和_old_gen。为什么要扫描新生代呢，因为对于老年代中的对象，如果被新生代中的对象引用，那么就会被视为存活对象，即使新生代的对象已经不可达了，也会使用这些不可达的对象当做cms的“gc root”，来扫描老年代； 因此对于老年代来说，引用了老年代中对象的新生代的对象，也会被老年代视作“GC ROOTS”：当此阶段耗时较长的时候，可以加入参数-XX:+CMSScavengeBeforeRemark，在重新标记之前，先执行一次ygc，回收掉年轻带的对象无用的对象，并将对象放入幸存带或晋升到老年代，这样再进行年轻带扫描时，只需要扫描幸存区的对象即可，一般幸存带非常小，这大大减少了扫描时间。  
&emsp; 由于之前的预处理阶段是与用户线程并发执行的，这时候可能年轻带的对象对老年代的引用已经发生了很多改变，这个时候，remark阶段要花很多时间处理这些改变，会导致很长stop the word，所以通常CMS尽量运行Final Remark阶段在年轻代是足够干净的时候。  
&emsp; 另外，还可以开启并行收集：-XX:+CMSParallelRemarkEnabled  

----------

&emsp; 该阶段并发执行，在之前的并行阶段（GC线程和应用线程同时执行，好比你妈在打扫房间，你还在扔纸屑），可能产生新的引用关系如下：  

* 老年代的新对象被GC Roots引用  
* 老年代的未标记对象被新生代对象引用  
* 老年代已标记的对象增加新引用指向老年代其它对象  
* 新生代对象指向老年代引用被删除  
* 也许还有其它情况..  

&emsp; 上述对象中可能有一些已经在Precleaning阶段和AbortablePreclean阶段被处理过，但总存在没来得及处理的，所以还有进行如下的处理：  

* 遍历新生代对象，重新标记  
* 根据GC Roots，重新标记  
* 遍历老年代的Dirty Card，重新标记，这里的Dirty Card大部分已经在clean阶段处理过  

&emsp; 在第一步骤中，需要遍历新生代的全部对象，如果新生代的使用率很高，需要遍历处理的对象也很多，这对于这个阶段的总耗时来说，是个灾难（因为可能大量的对象是暂时存活的，而且这些对象也可能引用大量的老年代对象，造成很多应该回收的老年代对象而没有被回收，遍历递归的次数也增加不少），如果在AbortablePreclean阶段中能够恰好的发生一次YGC，这样就可以避免扫描无效的对象。  

&emsp; 如果在AbortablePreclean阶段没来得及执行一次YGC，怎么办？  

&emsp; CMS算法中提供了一个参数：CMSScavengeBeforeRemark，默认并没有开启，如果开启该参数，在执行该阶段之前，会强制触发一次YGC，可以减少新生代对象的遍历时间，回收的也更彻底一点。  

&emsp; 不过，这种参数有利有弊，利是降低了Remark阶段的停顿时间，弊的是在新生代对象很少的情况下也多了一次YGC，最可怜的是在AbortablePreclean阶段已经发生了一次YGC，然后在该阶段又傻傻的触发一次。  

&emsp; 所以利弊需要把握。  


#### 1.2.2.5. 并发清理
&emsp; 通过以上5个阶段的标记，老年代所有存活的对象已经被标记，并且现在要通过Garbage Collector采用清扫的方式回收那些不能用的对象了。  
&emsp; 这个阶段主要是清除那些没有标记的对象并且回收空间；  
&emsp; 由于CMS并发清理阶段用户线程还在运行着，伴随程序运行自然就还会有新的垃圾不断产生，这一部分垃圾出现在标记过程之后，CMS无法在当次收集中处理掉它们，只好留待下一次GC时再清理掉。这一部分垃圾就称为“浮动垃圾”。  

#### 1.2.2.6. 并发重置
&emsp; 这个阶段并发执行，重新设置CMS算法内部的数据结构，准备下一个CMS生命周期的使用。  


## 1.3. ~~优点与问题~~  
<!-- 
https://blog.csdn.net/zqz_zqz/article/details/70568819
https://www.jianshu.com/p/86e358afdf17
线程资源被垃圾收集线程占用（cpu资源占用问题）
https://segmentfault.com/a/1190000040354999?utm_source=sf-similar-article

https://blog.csdn.net/m0_37989980/article/details/112794928
缺点：

    CMS 不会整理压缩堆空间，存在碎片。
    需要更多 CPU 资源：CMS 默认启动的回收线程数是（CPU 数量+3）/ 4，也就是当 CPU 在4个以上时，并发回收时垃圾收集线程不少于 25% 的 CPU 资源，并且随着 CPU 数量的增加而下降。但是当 CPU 不足4个（譬如2个）时，CMS 对用户程序的影响就可能变得很大。
    需要更多堆空间：由于在 GC 阶段用户线程还需要运行，那也就还需要预留有足够的内存空间给用户线程使用。

-->
&emsp; CMS是一款优秀的收集器，它最主要的优点在名字上已经体现出来： **<font color = "clime">并发收集、低停顿。</font>** 但是也有以下 **<font color = "red">三个明显的缺点：</font>**  

* **<font color = "clime">吞吐量低</font>**    
&emsp; <font color = "red">由于CMS在垃圾收集过程使用用户线程和GC线程并行执行，从而 **【线程切换】** 会有额外开销，</font>因此CPU吞吐量就不如在GC过程中停止一切用户线程的方式来的高。
* **<font color = "clime">无法处理浮动垃圾，导致频繁Full GC</font>**  
&emsp; <font color = "red">由于垃圾清除过程中，用户线程和GC线程并发执行，也就是用户线程仍在执行，那么在执行过程中会产生垃圾，这些垃圾称为"浮动垃圾"。</font>  
&emsp; 如果CMS在垃圾清理过程中，用户线程需要在老年代中分配内存时发现空间不足，就需再次发起Full GC，而此时CMS正在进行清除工作，因此此时只能由Serial Old临时对老年代进行一次Full GC。  
* **<font color = "clime">使用"标记-清除"算法，产生碎片空间</font>**  
&emsp; 由于CMS使用了"标记-清除"算法，因此清除之后会产生大量的碎片空间，不利于空间利用率。不过CMS提供了应对策略：开启-XX:+UseCMSCompactAtFullCollection，开启该参数后，每次FullGC完成后都会进行一次内存压缩整理，将零散在各处的对象整理到一块儿。但每次都整理效率不高，因此提供了另外一个参数，设置参数-XX:CMSFullGCsBeforeCompaction，本参数告诉CMS，经过了N次Full GC过后再进行一次内存整理。  

### 1.3.1. 减少remark阶段停顿
<!-- 
★★★ cms remark
https://mp.weixin.qq.com/s/5czSnGFi_PdOAsM-Iqqmww
-->

&emsp; 一般CMS的GC耗时80%都在remark阶段，如果发现remark阶段停顿时间很长，可以尝试添加该参数：`-XX:+CMSScavengeBeforeRemark`。  
&emsp; 在执行remark操作之前先做一次Young GC，目的在于减少年轻代对老年代的无效引用，降低remark时的开销。  


### 1.3.2. ~~内存碎片问题解决~~  
<!-- 

CMSGC造成内存碎片的解决方法
https://blog.csdn.net/weixin_34343689/article/details/86131696

CMS的碎片解决方案
https://blog.csdn.net/qq_40198004/article/details/109668921

CMS的CMSInitiatingOccupancyFraction解析
https://blog.csdn.net/insomsia/article/details/91802923
-->
&emsp; CMS是基于标记-清除算法的，CMS只会删除无用对象，不会对内存做压缩，会造成内存碎片，这时候需要用到这个参数：`-XX:CMSFullGCsBeforeCompaction=n`。  
&emsp;意思是说在上一次CMS并发GC执行过后，到底还要再执行多少次full GC才会做压缩。默认是0，也就是在默认配置下每次CMS GC顶不住了而要转入full GC的时候都会做压缩。 如果把CMSFullGCsBeforeCompaction配置为10，就会让上面说的第一个条件变成每隔10次真正的full GC才做一次压缩。  

-------------

&emsp; CMSGC在老生代回收时产生的内存碎片会导致老生代的利用率变低；或者可能在老生代总内存大小足够的情况下，却不能容纳新生代的晋升行为（由于没有连续的内存空间可用），导致触发FullGC。针对这个问题，Sun官方给出了以下的四种解决方法：

* 增大Xmx或者减少Xmn  
* 在应用访问量最低的时候，在程序中主动调用System.gc()，比如每天凌晨。  
* 在应用启动并完成所有初始化工作后，主动调用System.gc()，它可以将初始化的数据压缩到一个单独的chunk中，以腾出更多的连续内存空间给新生代晋升使用。  
* 降低-XX:CMSInitiatingOccupancyFraction参数（内存占用率，默认70%）以提早执行CMSGC动作，虽然CMSGC不会进行内存碎片的压缩整理，但它会合并老生代中相邻的free空间。这样就可以容纳更多的新生代晋升行为。   

----------

&emsp; CMS是一款基于“标记-清除”算法实现的收集器，如果读者对前面这部分介绍还有印象的话，就可能想到这意味着收集结束时会有大量空间碎片产生。空间碎片过多时，将会给大对象分配带来很大麻烦，往往会出现老年代还有很多剩余空间，但就是无法找到足够大的连续空间来分配当前对象，而不得不提前触发一次Full GC的情况。  
&emsp; 为了解决这个问题，CMS收集器提供了一个-XX：+UseCMS-CompactAtFullCollection开关参数（默认是开启的，此参数从JDK 9开始废弃），用于在CMS收集器不得不进行Full GC时开启内存碎片的合并整理过程，由于这个内存整理必须移动存活对象，（在Shenandoah和ZGC出现前）是无法并发的。  
&emsp; 这样空间碎片问题是解决了，但停顿时间又会变长，因此虚拟机设计者们还提供了另外一个参数-XX：CMSFullGCsBefore-Compaction（此参数从JDK 9开始废弃），这个参数的作用是要求CMS收集器在执行过若干次（数量由参数值决定）不整理空间的Full GC之后，下一次进入Full GC前会先进行碎片整理（默认值为0，表示每次进入Full GC时都进行碎片整理）。  



### 1.3.3. ~~concurrent mode failure & promotion failed问题~~
<!-- 
https://www.jianshu.com/p/ca1b0d4107c5
https://www.cnblogs.com/fswhq/p/11767439.html
https://my.oschina.net/hosee/blog/674181

concurrent mode failure影响
老年代的垃圾收集器从CMS退化为Serial Old，所有应用线程被暂停，停顿时间变长。
-->
#### 1.3.3.1. 简介
![image](http://182.92.69.8:8081/img/java/JVM/JVM-124.png)  
&emsp; 晋升失败：当新生代发生垃圾回收，老年代有足够的空间可以容纳晋升的对象，但是由于空闲空间的碎片化，导致晋升失败。此时会触发单线程且带压缩动作的Full GC。  

![image](http://182.92.69.8:8081/img/java/JVM/JVM-123.png)  
&emsp; 并发模式失败：当CMS在执行回收时，新生代发生垃圾回收，同时老年代又没有足够的空间容纳晋升的对象时。CMS 垃圾回收会退化成单线程的Full GC。所有的应用线程都会被暂停，老年代中所有的无效对象都被回收。  

#### 1.3.3.2. 可能原因及解决方案
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