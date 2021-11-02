
<!-- TOC -->

- [1. 垃圾回收器](#1-垃圾回收器)
    - [1.1. 收集器发展过程](#11-收集器发展过程)
    - [1.2. 收集器分类](#12-收集器分类)
    - [1.3. 收集器详解](#13-收集器详解)
        - [1.3.1. 新生代收集器](#131-新生代收集器)
            - [1.3.1.1. Serial收集器](#1311-serial收集器)
            - [1.3.1.2. ParNew收集器](#1312-parnew收集器)
            - [1.3.1.3. Parallel Scavenge收集器](#1313-parallel-scavenge收集器)
        - [1.3.2. 老年代收集器](#132-老年代收集器)
            - [1.3.2.1. Serial Old收集器](#1321-serial-old收集器)
                - [1.3.2.1.1. Parallel Old收集器](#13211-parallel-old收集器)
        - [1.3.3. CMS收集器](#133-cms收集器)
        - [1.3.4. 常用的收集器组合](#134-常用的收集器组合)
        - [1.3.5. G1收集器](#135-g1收集器)
        - [1.3.6. ZGC](#136-zgc)
        - [1.3.7. Epsilon](#137-epsilon)
        - [1.3.8. Shenandoah](#138-shenandoah)
    - [1.4. 选择合适的垃圾收集器](#14-选择合适的垃圾收集器)
    - [1.5. 垃圾收集器常用参数](#15-垃圾收集器常用参数)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. 根据收集器的指标（性能考虑因素）分类（`两个关键指标，停顿时间和吞吐量`）：  
    * **<font color = "clime">吞吐量：运行用户代码时间/(运行用户代码时间+垃圾收集时间)。</font>**  
    * 停顿时间：执行垃圾收集时，程序的工作线程被暂停的时间。  
    * 内存占有（堆空间）：Java堆区所占的内存大小。  
    * 垃圾收集开销：吞吐量的补数，垃圾收集器所占时间与总时间的比例。  
    * 收集频率：相对于应用程序的执行，收集操作发生的频率。  
    * 快速：一个对象从诞生到被回收所经历的时间。  

    &emsp; <font color  = "red">其中内存占用、吞吐量和停顿时间，三者共同构成了一个“不可能三角”。</font>    
    &emsp; 停顿时间越短就越适合需要和用户交互的程序，良好的响应速度能提升用户体验；  
    &emsp; 高吞吐量则可以高效地利用CPU时间，尽快完成程序的运算任务，主要适合在后台运算而不需要太多交互的任务。  
2. 根据运行时，线程执行方式分类：  
    * 串行收集器 -> Serial和Serial Old  
    &emsp; **<font color = "red">只能有一个垃圾回收线程执行，用户线程暂停。</font>** 适用于内存比较小的嵌入式设备 。  
    * 并行收集器【吞吐量优先】 -> Parallel Scanvenge、Parallel Old  
    &emsp; **<font color = "red">多条垃圾收集线程并行工作，但此时用户线程仍然处于等待状态。</font>** 适用于科学计算、后台处理等交互场景 。  
    * 并发收集器【停顿时间优先】 -> CMS、G1  
    &emsp; **<font color = "red">用户线程和垃圾收集线程同时执行</font><font color = "blue">（但并不一定是并行的，可能是交替执行的），</font><font color = "red">垃圾收集线程在执行的时候不会停顿用户线程的运行。</font>** 适用于相对时间有要求的场景，比如Web。  
3. `JDK 7u4后的7和JDK8默认使用的都是ParallelScavenge+ParallelOld。`  

    
<!-- 

PLAB
https://mp.weixin.qq.com/s/WVGZIBXsIVYPMfhkqToh_Q
-->



# 1. 垃圾回收器  
&emsp; 垃圾收集算法是内存回收的理论基础，而垃圾收集器就是内存回收的具体实现。   

## 1.1. 收集器发展过程
&emsp; 可以把收集器发展简单划分成以下几个阶段：  
1. 单线程阶段，对应收集器：Serial、Serial Old；
2. 并行阶段，多条收集器线程，对应收集器：ParNew、Parallel Scavenge、Parallel Old；
3. 并发阶段，收集器线程与用户线程同时运行，对应收集器：CMS(Concurrent Mark Sweep)；
4. 并行+并发+分区阶段，堆内存划分成多个小块进行收集，对应收集器：G1(Garbage First)；
5. .....

&emsp; GC过程一定会发生STW(Stop The World)，而一旦发生STW必然会影响用户使用，所以GC的发展都是在围绕减少STW时间这一目的。通过并行与并发已经极大的减少了STW的时间，但是STW的时间还是会因为各种原因不可控，G1提供的一个最大功能就是可控的STW时间。  

## 1.2. 收集器分类  
<font color = "blue">1. 根据收集器的指标分类（`两个关键指标，停顿时间和吞吐量`）：</font>  
&emsp; 收集器性能考虑因素：  

* **<font color = "clime">吞吐量：运行用户代码时间/(运行用户代码时间+垃圾收集时间)。</font>**  
* 停顿时间：执行垃圾收集时，程序的工作线程被暂停的时间。  
* 内存占有（堆空间）：Java堆区所占的内存大小。  
* 垃圾收集开销：吞吐量的补数，垃圾收集器所占时间与总时间的比例。  
* 收集频率：相对于应用程序的执行，收集操作发生的频率。  
* 快速：一个对象从诞生到被回收所经历的时间。  

&emsp; <font color  = "red">其中内存占用、吞吐量和停顿时间，三者共同构成了一个“不可能三角”。</font>    
&emsp; 停顿时间越短就越适合需要和用户交互的程序，良好的响应速度能提升用户体验；  
&emsp; 高吞吐量则可以高效地利用CPU时间，尽快完成程序的运算任务，主要适合在后台运算而不需要太多交互的任务。  

<font color = "clime">2. 根据运行时，线程执行方式分类：</font>  

* 串行收集器 -> Serial和Serial Old  
&emsp; **<font color = "red">只能有一个垃圾回收线程执行，用户线程暂停。</font>** 适用于内存比较小的嵌入式设备 。  
* 并行收集器【吞吐量优先】 -> Parallel Scanvenge、Parallel Old  
&emsp; **<font color = "red">多条垃圾收集线程并行工作，但此时用户线程仍然处于等待状态。</font>** 适用于科学计算、后台处理等交互场景 。  
* 并发收集器【停顿时间优先】 -> CMS、G1  
&emsp; **<font color = "red">用户线程和垃圾收集线程同时执行</font><font color = "blue">（但并不一定是并行的，可能是交替执行的），</font><font color = "red">垃圾收集线程在执行的时候不会停顿用户线程的运行。</font>** 适用于相对时间有要求的场景，比如Web。  

## 1.3. 收集器详解
&emsp; HotSpot虚拟机所包含的所有收集器如图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-55.png)  
&emsp; 上图展示了多种作用于不同分代的收集器。如果两个收集器之间存在连线，那说明它们可以搭配使用。虚拟机所处的区域说明它是属于新生代收集器还是老年代收集器。选择对具体应用最合适的收集器。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-67.png)  

* Serial串行收集器：新生代收集器、最基本、发展历史最久(jdk1.3之前)、单线程、基于复制算法。  
* Serial Old串行老年代收集器：老年代版本的Serial收集器、单线程、基于标记-整理算法。  
* ParNew收集器：Serial的多线程版本、新生代收集器、多线程、基于复制算法、关注用户停顿时间。  
* Parallel Scavenge收集器：新生代收集器，基于复制算法，并行的多线程、关注吞吐量。  
* Parallel Old收集器：Parallel Scavenge的老年代版本，使用多线程和“标记-整理”算法。  
* CMS(Conturrent Mark Sweep)收集器：并发、基于标记-清除算法。  
* G1(Garbage-First)收集器：并行与并发、分代收集、空间整合。  
* Shenandoah：支持并发的整理算法、基于读写屏障、旋转指针。  
* ZGC：支持并发收集、基于动态Region、染色指针、虚拟内存映射。  
* Epsilon垃圾收集器：没有操作的垃圾收集器、处理内存分配但不实现任何实际内存回收机制的GC。  


&emsp; 在JDK 7U4之前UserParallelGC用的是ParallelScavenge+SerialOld，在这个版本后Parallel已经很成熟了，所以直接替换了旧的收集器，所以 **<font color = "clime">JDK 7u4后的7和JDK8默认使用的都是ParallelScavenge+ParallelOld。</font>**  

<!-- 
https://blog.csdn.net/wszb2012/article/details/109075575?utm_medium=distribute.pc_relevant_download.none-task-blog-baidujs-1.nonecase&depth_1-utm_source=distribute.pc_relevant_download.none-task-blog-baidujs-1.nonecase
-->

### 1.3.1. 新生代收集器  
#### 1.3.1.1. Serial收集器  
&emsp; 最基本、发展历史最久的收集器，这个收集器是一个采用复制算法的单线程的收集器。  
&emsp; 迄今为止，Serial收集器依然是虚拟机运行在Client模式下的默认新生代收集器，因为它简单而高效。用户桌面应用场景中，分配给虚拟机管理的内存一般来说不会很大，收集几十兆甚至一两百兆的新生代停顿时间在几十毫秒最多一百毫秒，只要不是频繁发生，这点停顿是完全可以接受的。  
&emsp; 参数控制：
  
    -XX:+UseSerialGC 串行收集器
    
&emsp; Serial收集器运行过程如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-29.png)  
<!-- &emsp; 说明：1.需要STW(Stop The World)，停顿时间长。2.简单高效，对于单个CPU环境而言，Serial收集器由于没有线程交互开销，可以获取最高的单线程收集效率。  -->

&emsp; **<font color = "red">一句话概括：Serial收集器，采用复制算法的单线程的收集器，运行在Client模式下的默认新生代收集器，适用于用户桌面应用中。</font>**

#### 1.3.1.2. ParNew收集器  
&emsp; (相同)ParNew收集器是Serial收集器的多线程版本，除了使用多条线程进行垃圾收集外，其余行为和Serial收集器完全一样，包括使用的也是复制算法。  
&emsp; (不同)ParNew收集器除了多线程以外和Serial收集器并没有太多创新的地方，但是它却是JDK7之前Server模式下的虚拟机首选的新生代收集器，其中有一个很重要的和性能无关的原因是，除了Serial收集器外，目前只有它能与CMS收集器配合工作。  
&emsp; (对比)ParNew收集器在单CPU的环境中绝对不会有比Serial收集器更好的效果，甚至由于线程交互的开销，该收集器在两个CPU的环境中都不能百分之百保证可以超越Serial收集器。当然，随着可用CPU数量的增加，它对于GC时系统资源的有效利用还是很有好处的。它默认开启的收集线程数与CPU数量相同，在CPU数量非常多的情况下，可以使用-XX:ParallelGCThreads参数来限制垃圾收集的线程数。  
&emsp; 参数控制：  

    -XX:+UseParNewGC ParNew收集器
    -XX:ParallelGCThreads 限制线程数量
    
&emsp; ParNew收集器运行过程如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-30.png)  

&emsp; **<font color = "red">一句话概括：Serial收集器的多线程版本，降低停顿时间，JDK7之前Server模式下的虚拟机首选的新生代收集器，能与CMS收集器配合。</font>**

#### 1.3.1.3. Parallel Scavenge收集器  
&emsp; Parallel Scavenge收集器也是一个新生代收集器，也是用复制算法的收集器，也是并行的多线程收集器。Parallel Scavenge收集器是虚拟机运行在Server模式下的默认垃圾收集器。   
&emsp; 它的特点是它的关注点和其他收集器不同。<font color = "clime">Parallel Scavenge收集器的目标则是达到一个可控制的吞吐量(吞吐量=运行用户代码时间/(运行用户代码时间+垃圾收集时间)))。</font> 高吞吐量可以最高效率地利用 CPU 时间，尽快地完成程序的运算任务，主要适用于在后台运算而不需要太多交互的任务。<font color = "red">自适应调节策略也是 ParallelScavenge 收集器与 ParNew 收集器的一个重要区别。</font>   
&emsp; 参数控制：    

        -XX:+UseParallelGC 使用Parallel收集器+ 老年代串行。  
        Parallel Scavenge收集器提供了两个参数用于精确控制吞吐量，分别是控制最大垃圾收集停顿时间的-XX：MaxGCPauseMillis参数以及直接设置吞吐量大小的-XX：GCTimeRatio参数。  
        Parallel Scavenge收集器还有一个参数-XX：+UseAdaptiveSizePolicy值得我们关注。这是一个开关参数，当这个参数被激活之后，就不需要人工指定新生代的大小(-Xmn)、Eden与Survivor区的比例(-XX：SurvivorRatio)、晋升老年代对象大小(-XX：PretenureSizeThreshold)等细节参数 了，虚拟机会根据当前系统的运行情况收集性能监控信息，动态调整这些参数以提供最合适的停顿时间或者最大的吞吐量。这种调节方式称为垃圾收集的自适应的调节策略(GC Ergonomics)。  

&emsp; **<font color = "red"> Parallel Scavenge收集器，也是采用复制算法的并行的多线程收集器，Server模式下的默认垃圾收集器</font>，<font color = "clime">目标是达到一个可控制的吞吐量。</font>**

### 1.3.2. 老年代收集器  
#### 1.3.2.1. Serial Old收集器  
&emsp; Serial收集器的老年代版本，同样是一个单线程收集器，使用“标记-整理算法”，这个收集器的主要意义也是在于给Client模式下的虚拟机使用。 

##### 1.3.2.1.1. Parallel Old收集器  
&emsp; Parallel Scavenge收集器的老年代版本，使用多线程和“标记-整理”算法。这个收集器在JDK 1.6之后的出现，“吞吐量优先收集器”终于有了比较名副其实的应用组合，在注重吞吐量以及CPU资源敏感的场合，都可以优先考虑Parallel Scavenge收集器+Parallel Old收集器的组合。  
&emsp; 参数控制：  

    -XX:+UseParallelOldGC 使用Parallel收集器+ 老年代并行

&emsp; 运行过程如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-31.png)  


### 1.3.3. CMS收集器  
&emsp; [CMS回收器](/docs/java/JVM/CMS.md)  
  
### 1.3.4. 常用的收集器组合  

| |新生代GC策略	|老年代GC策略	|说明|
|---|---|---|---|
|组合1|Serial|Serial Old|Serial和Serial Old都是单线程进行GC，特点就是GC时暂停所有应用线程。|
|组合2|Serial|CMS+Serial Old|CMS(Concurrent Mark Sweep)是并发GC，实现GC线程和应用线程并发工作，不需要暂停所有应用线程。另外，当CMS进行GC失败时，会自动使用Serial Old策略进行GC。|
|组合3|ParNew|CMS|使用-XX:+UseParNewGC选项来开启。ParNew是Serial的并行版本，可以指定GC线程数，默认GC线程数为CPU的数量。可以使用-XX:ParallelGCThreads选项指定GC的线程数。如果指定了选项-XX:+UseConcMarkSweepGC选项，则新生代默认使用ParNew GC策略。|
|组合4|ParNew|Serial Old|使用-XX:+UseParNewGC选项来开启。新生代使用ParNew GC策略，年老代默认使用Serial Old GC策略。|
|组合5|Parallel Scavenge|Serial Old	|Parallel Scavenge策略主要是关注一个可控的吞吐量：应用程序运行时间 / (应用程序运行时间 + GC时间)，可见这会使得CPU的利用率尽可能的高，适用于后台持久运行的应用程序，而不适用于交互较多的应用程序。|
|组合6|Parallel Scavenge|Parallel Old|Parallel Old是Serial Old的并行版本|

### 1.3.5. G1收集器  
&emsp; [G1回收器](/docs/java/JVM/G1.md)  

### 1.3.6. ZGC  
&emsp; 一款由Oracle公司研发的，以低延迟为首要目标的一款垃圾收集器。它是基于动态Region内存布局，(暂时)不设年龄分代，使用了读屏障、染色指针和内存多重映射等技术来实现可并发的标记-整理算法的收集器。在JDK 11新加入，还在实验阶段，主要特点是：回收TB级内存(最大4T)，停顿时间不超过10ms  

* 优点：低停顿，高吞吐量，ZGC收集过程中额外耗费的内存小  
* 缺点：浮动垃圾  

&emsp; ZGC目前只在Linux/x64上可用，如果有足够的需求，将来可能会增加对其他平台的支持  
&emsp; 启动参数: -XX:+UnlockExperimentalVMOptions -XX:+UseZGC -Xmx10g -Xlog:gc  
<!-- 
 新一代垃圾回收器ZGC的探索与实践 
 https://mp.weixin.qq.com/s/ag5u2EPObx7bZr7hkcrOTg
 ZGC
https://mp.weixin.qq.com/s/5trCK-KlwikKO-R6kaTEAg
一文读懂Java 11的ZGC为何如此高效 
https://mp.weixin.qq.com/s/nAjPKSj6rqB_eaqWtoJsgw
深入理解JVM - ZGC垃圾收集器 
https://mp.weixin.qq.com/s/q4JeoI47eWBaViNDBCfnuQ

【241期】面试官：你了解JVM中的ZGC垃圾收集器吗？ 
https://mp.weixin.qq.com/s/r9TSqka8y1qV4QIiD3qXKw

&emsp; 一款由Oracle公司研发的，以低延迟为首要目标的一款垃圾收集器。它是基于动态Region内存布局，(暂时)不设年龄分代，使用了读屏障、染色指针和内存多重映射等技术来实现可并发的标记-整理算法的收集器。在JDK 11新加入，还在实验阶段，主要特点是：回收TB级内存(最大4T)，停顿时间不超过10ms  

* 优点：低停顿，高吞吐量，ZGC收集过程中额外耗费的内存小  
* 缺点：浮动垃圾  

&emsp; ZGC目前只在Linux/x64上可用，如果有足够的需求，将来可能会增加对其他平台的支持  
&emsp; 启动参数: -XX:+UnlockExperimentalVMOptions -XX:+UseZGC -Xmx10g -Xlog:gc  

&emsp; 1. ZGC收集器之动态Region  

* 小型Region(Small Region)：容量固定为2MB，用于放置小于256KB的小对象  
* 中型Region(Medium Region)：容量固定为32MB，用于放置大于等于256KB但小于4MB的对象  
* 大型Region(Large Region)：容量不固定，可以动态变化，但必须为2MB的整数倍，用于放置4MB或以上的大对象。每个大型Region中只会存放一个大对象，最小容量可低至4MB，所有大型Region可能小于中型Region  
* 大型Region在ZGC的实现中是不会被重分配的，因为复制一个大对象的代价非常高昂  

&emsp; 2. ZGC收集器之染色指针  
&emsp; HotSpot虚拟机的标记实现方案有如下几种：  

* 把标记直接记录在对象头上(如Serial收集器)；
* 把标记记录在与对象相互独立的数据结构上(如G1、Shenandoah使用了一种相当于堆内存的1/64大小的，称为BitMap的结构来记录标记信息)
* 直接把标记信息记在引用对象的指针上(如ZGC)

&emsp; 3. 染色指针是一种直接将少量额外的信息存储在指针上的技术  
&emsp; 目前在Linux下64位的操作系统中高18位是不能用来寻址的，但是剩余的46为却可以支持64T的空间，到目前为止我们几乎还用不到这么多内存。于是ZGC将46位中的高4位取出，用来存储4个标志位，剩余的42位可以支持4T的内存  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-62.png)  

&emsp; 4. ZGC收集器之三色标记  
&emsp; 在并发的可达性分析算法中我们使用三色标记(Tri-color Marking)来标记对象是否被收集器访问过  

* 白色：表示对象尚未被垃圾收集器访问过。显然在可达性分析刚刚开始的阶段，所有的对象都是白色的，若在分析结束的阶段，仍然是白色的对象，即代表不可达
* 黑色：表示对象已经被垃圾收集器访问过，且这个对象的所有引用都已经扫描过。黑色的对象代表已经扫描过，它是安全存活的，如果有其他对象引用指向了黑色对象，无须重新扫描一遍。黑色对象不可能直接(不经过灰色对象)指向某个白色对象。
* 灰色：表示对象已经被垃圾收集器访问过，但这个对象上至少存在一个引用还没有被扫描过

&emsp; 5. ZGC收集器之读屏障  
&emsp; 当对象从堆中加载的时候，就会使用到读屏障(Load Barrier)。这里使用读屏障的主要作用就是检查指针上的三色标记位，根据标记位判断出对象是否被移动过，如果没有可以直接访问，如果移动过就需要进行“自愈”(对象访问会变慢，但也只会有一次变慢)，当“自愈”完成后，后续访问就不会变慢  
读写屏障可以理解成对象访问的“AOP”操作  

&emsp; 6. ZGC收集器之内存多重映射  
&emsp; ZGC使用了内存多重映射(Multi-Mapping)将多个不同的虚拟内存地址映射到同一个物理内存地址上，这是一种多对一映射，意味着ZGC在虚拟内存中看到的地址空间要比实际的堆内存容量来得更大。把染色指针中的标志位看作是地址的分段符，那只要将这些不同的地址段都映射到同一个物理内存空间，经过多重映射转换后，就可以使用染色指针正常进行寻址了  

&emsp; 7. ZGC收集器运作过程  

* 并发标记：与G1、Shenandoah一样，并发标记是遍历对象图做可达性分析的阶段，它的初始标记和最终标记也会出现短暂的停顿，整个标记阶段只会更新染色指针中的Marked 0、Marked 1标志位  
* 并发预备重分配：这个阶段需要根据特定的查询条件统计得出本次收集过程要清理哪些Region，将这些Region组成重分配集(Relocation Set)。ZGC每次回收都会扫描所有的Region，用范围更大的扫描成本换取省去G1中记忆集的维护成本  
* 并发重分配：重分配是ZGC执行过程中的核心阶段，这个过程要把重分配集中的存活对象复制到新的Region上，并为重分配集中的每个Region维护一个转发表(Forward Table)，记录从旧对象到新对象的转向关系。ZGC收集器能仅从引用上就明确得知一个对象是否处于重分配集之中，如果用户线程此时并发访问了位于重分配集中的对象，这次访问将会被预置的内存屏障所截获，然后立即根据Region上的转发表记录将访问转发到新复制的对象上，并同时修正更新该引用的值，使其直接指向新对象，ZGC将这种行为称为指针的“自愈”(Self-Healing)能力  
* 并发重映射(Concurrent Remap)：重映射所做的就是修正整个堆中指向重分配集中旧对象的所有引用，但是ZGC中对象引用存在“自愈”功能，所以这个重映射操作并不是很迫切。ZGC很巧妙地把并发重映射阶段要做的工作，合并到了下一次垃圾收集循环中的并发标记阶段里去完成，反正它们都是要遍历所有对象的，这样合并就节省了一次遍历对象图的开销
-->


### 1.3.7. Epsilon  
&emsp; Epsilon(A No-Op Garbage Collector)垃圾回收器控制内存分配，但是不执行任何垃圾回收工作。一旦java的堆被耗尽，jvm就直接关闭。设计的目的是提供一个完全消极的GC实现，分配有限的内存分配，最大限度降低消费内存占用量和内存吞吐时的延迟时间。一个好的实现是隔离代码变化，不影响其他GC，最小限度的改变其他的JVM代码  
&emsp; 适用场景:  

* Performance testing，什么都不执行的GC非常适合用于差异性分析
* 在测试java代码时，确定分配内存的阈值有助于设置内存压力常量值。这时no-op就很有用，它可以简单地接受一个分配的内存分配上限，当内存超限时就失败。例如：测试需要分配小于1G的内存，就使用-Xmx1g参数来配置no-op GC，然后当内存耗尽的时候就直接crash

&emsp; 相关启动参数

    UnlockExperimentalVMOptions：解锁隐藏的虚拟机参数
    -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -Xms100m -Xmx100m

### 1.3.8. Shenandoah  
&emsp; 一款只有OpenJDK才会包含的收集器，最开始由RedHat公司独立发展后来贡献给了OpenJDK  
&emsp; Shenandoah与G1类似，也是使用基于Region的堆内存布局，同样有着用于存放大对象的Humongous Region，默认的回收策略也同样是优先处理回收价值最大的Region  
&emsp; 但是管理堆内存方面，与G1至少有三个明显的不同之处：  
1. Shenandoah 支持并发的整理算法；G1支持并行整理算法。
2. Shenandoah(目前)是默认不使用分代收集的；G1有专门的新生代Region或者老年代Region的存在;
3. Shenandoah摒弃了在G1中耗费大量内存和计算资源去维护的记忆集，改用名为“连接矩阵”(Connection Matrix)的全局数据结构来记录跨Region的引用关系，降低了处理跨代指针时的记忆集维护消耗，也降低了伪共享问题的发生概率

* 优点：延迟低
* 缺点：高运行负担使得吞吐量下降；使用大量的读写屏障，尤其是读屏障，增大了系统的性能开销；

&emsp; 开启参数: -XX:+UnlockExperimentalVMOptions  -XX:+UseShenandoahGC

&emsp; Shenandoah 收集器之连接矩阵  
&emsp; 连接矩阵可以简单理解为一张二维表格，如果Region N有对象指向RegionM，就在表格的N行M列中打上一个标记，如右图所示，如果Region 5中的对象Baz引用了Region 3的Foo，Foo又引用了Region 1的Bar，那连接矩阵中的5行3列、3行1列就应该被打上标记。在回收时通过这张表格就可以得出哪些Region之间产生了跨代引用  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-64.png)  

&emsp; Shenandoah 收集器之转发指针  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-65.png)  
&emsp; 转发指针(Forwarding Pointer，也常被称为Indirection Pointer)来实现对象移动与用户程序并发的一种解决方案  
&emsp; Brooks提出的新方案不需要用到内存保护陷阱，而是在原有对象布局结构的最前面统一增加一个新的引用字段，在正常不处于并发移动的情况下，该引用指向对象自己。从结构上来看，Brooks提出的转发指针与某些早期Java虚拟机使用过的句柄定位，有一些相似之处，两者都是一种间接性的对象访问方式，差别是句柄通常会统一存储在专门的句柄池中，而转发指针是分散存放在每一个对象头前面  

&emsp; Shenandoah 收集器之读写屏障  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-66.png)  
&emsp; Brooks形式的转发指针在设计上决定了它是必然会出现多线程竞争问题的，如果收集器线程与用户线程发生的只是并发读取，那无论读到旧对象还是新对象上的字段，返回的结果都应该是一样的，这个场景还可以有一些“偷懒”的处理余地；但如果发生的是并发写入，就一定必须保证写操作只能发生在新复制的对象上，而不是写入旧对象的内存中  
&emsp; 解决方案：Shenandoah不得不同时设置读、写屏障去拦截  

## 1.4. 选择合适的垃圾收集器  
&emsp; 官网：https://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/collectors.html#sthref28

* 优先调整堆的大小让服务器自己来选择  
* 如果内存小于100M，使用串行收集器  
* 如果是单核，并且没有停顿时间要求，使用串行或JVM自己选  
* 如果允许停顿时间超过1秒，选择并行或JVM自己选  
* 如果响应时间最重要，并且不能超过1秒，使用并发收集器  
* 对于G1收集  

<!-- 
    如果你的堆大小不是很大(比如 100MB)，选择串行收集器一般是效率最高的。

    参数：-XX:+UseSerialGC。

    如果你的应用运行在单核的机器上，或者你的虚拟机核数只有单核，选择串行收集器依然是合适的，这时候启用一些并行收集器没有任何收益。

    参数：-XX:+UseSerialGC。

    如果你的应用是“吞吐量”优先的，并且对较长时间的停顿没有什么特别的要求。选择并行收集器是比较好的。

    参数：-XX:+UseParallelGC。

    如果你的应用对响应时间要求较高，想要较少的停顿。甚至 1 秒的停顿都会引起大量的请求失败，那么选择G1、ZGC、CMS都是合理的。虽然这些收集器的 GC 停顿通常都比较短，但它需要一些额外的资源去处理这些工作，通常吞吐量会低一些。

    参数：

    -XX:+UseConcMarkSweepGC、

    -XX:+UseG1GC、

    -XX:+UseZGC 等。

从上面这些出发点来看，我们平常的 Web 服务器，都是对响应性要求非常高的。选择性其实就集中在 CMS、G1、ZGC上。而对于某些定时任务，使用并行收集器，是一个比较好的选择。
-->

## 1.5. 垃圾收集器常用参数  
&emsp; -XX:+UseSerialGC：在新生代和老年代使用串行收集器  
&emsp; -XX:+UseParNewGC：在新生代使用并行收集器  
&emsp; -XX:+UseParallelGC：新生代使用并行回收收集器，更加关注吞吐量  
&emsp; -XX:+UseParallelOldGC：老年代使用并行回收收集器  
&emsp; -XX:ParallelGCThreads：设置用于垃圾回收的线程数  
&emsp; -XX:+UseConcMarkSweepGC：新生代使用并行收集器，老年代使用CMS+串行收集器  
&emsp; -XX:ParallelCMSThreads：设定CMS的线程数量  
&emsp; -XX:+UseG1GC：启用G1垃圾回收器  
