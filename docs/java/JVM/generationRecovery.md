
<!-- TOC -->

- [1. GC算法与分代回收](#1-gc算法与分代回收)
    - [1.1. GC算法](#11-gc算法)
        - [1.1.1. 标记-清除(Mark-Sweep)算法](#111-标记-清除mark-sweep算法)
        - [1.1.2. 标记-复制(Copying)算法](#112-标记-复制copying算法)
        - [1.1.3. 标记-整理(Mark-Compact)算法](#113-标记-整理mark-compact算法)
    - [1.2. 分代收集](#12-分代收集)
        - [1.2.1. ~~新生代和老年代采用的算法~~](#121-新生代和老年代采用的算法)
        - [1.2.2. 分代收集理论](#122-分代收集理论)
    - [1.3. HotSpot GC分类](#13-hotspot-gc分类)
        - [1.3.1. Yong GC](#131-yong-gc)
            - [1.3.1.1. ~~YGC触发时机~~](#1311-ygc触发时机)
            - [1.3.1.2. YGC执行流程](#1312-ygc执行流程)
        - [1.3.2. Major GC](#132-major-gc)
        - [1.3.3. Full GC](#133-full-gc)
            - [1.3.3.1. FGC的触发时机](#1331-fgc的触发时机)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**   
1. 思想：算法  
    1. 各种GC算法：  
        * **<font color = "clime">标记-清除算法分为两个阶段：标记阶段和清除阶段。</font>** 不足：清除过程中，`扫描两次`，效率不高；清除后，产生`空间碎片`。  
        * `复制：1).（非标记-复制）只扫描一次；` 2). 没有碎片，空间连续； 3). 50%的内存空间始终空闲浪费。  
        * 标记-整理：1). 没有碎片，空间连续； 2). 不会产生内存减半； 3). 扫描两次，指针需要调整(移动对象)，效率偏低。  
        &emsp; **<font color = "clime">标记-清除和标记-整理都需要扫描两次。</font>**   
    2. 分代回收：  
        * 新生代采用复制算法，新生代存活率低，存活对象少。
        * 老年代采用标记-整理算法。 
            * 标记-清除算法效率高，搬运对象也比较少。`  
            * **<font color = "clime">注意：CMS回收老年代，但采用标记-清除算法；CMS收集器也会在内存空间的碎片化程度已经大到影响对象分配时（晋升失败(promotion failed) 或 并发模式失败(concurrent mode failure)），采用标记-整理算法收集一次，以获得规整的内存空间。</font>**    
2. 分代回收流程：各种GC（YGC、Full GC）执行流程   
    * `Partial GC(局部 GC)：并不收集整个 GC 堆的模式。`  
        * Young GC：只收集 Young Gen 的 GC，Young GC 还有种说法就叫做 Minor GC。  
        * Old GC：只收集 old gen 的 GC，只有垃圾收集器 CMS 的 concurrent collection 是这个模式。  
        * Mixed GC：收集整个 Young Gen 以及部分 old gen 的 GC，只有垃圾收集器 G1 有这个模式。  
    * `Full GC：收集整个堆，包括新生代，老年代，永久代(在 JDK 1.8 及以后，永久代被移除，换为 metaspace 元空间)等所有部分的模式。`  
3. YGC触发时机：eden区快要被占满的时候；在full gc前会让先执行以下young gc。  
4. Full GC  
&emsp; **<font color = "red">Full GC的触发时机：（老年代或永久代不足 ---> 老年代不满足年轻代晋升 ---> 回收器(例如CMS)---> 系统调用 ）</font>**   
    1. 老年代或永久代的`不足`
        1. 老年代空间不足(92%)  
        &emsp; 老年代空间不足的常见场景为大对象直接进入老年代、长期存活的对象进入老年代等。  
        &emsp; 为了避免以上原因引起的Full GC，应当尽量不要创建过大的对象以及数组。除此之外，可以通过-Xmn虚拟机参数调大新生代的大小，让对象尽量在新生代被回收掉，不进入老年代。还可以通过 -XX:MaxTenuringThreshold调大对象进入老年代的年龄，让对象在新生代多存活一段时间。  
        2. JDK 1.7及以前的永久代空间不足  
        &emsp; 为避免以上原因引起的Full GC，可采用的方法为增大永久代空间或转为使用CMS GC。  
    2. 老年代`不满足`年轻代晋升  
        1. 统计得到的Minor GC晋升到旧生代的`平均大小`大于旧生代的剩余空间  
        &emsp; Hotspot为了避免由于新生代对象晋升到旧生代导致旧生代空间不足的现象，在进行Minor GC时，做了一个判断，如果之前统计所得到的Minor GC晋升到旧生代的平均大小大于旧生代的剩余空间，那么就直接触发Full GC。  
        2. 空间分配担保失败  
        &emsp; **<font color = "clime">JVM在发生Minor GC之前，虚拟机会检查老年代最大可用的`连续空间`是否大于新生代所有对象的`总空间`，</font>** 如果大于，则此次Minor GC是安全的；如果小于，则虚拟机会查看HandlePromotionFailure设置项的值是否允许担保失败。如果HandlePromotionFailure=true，那么会继续检查老年代最大可用连续空间是否大于历次晋升到老年代的对象的平均大小，如果大于则尝试进行一次Minor GC，但这次Minor GC依然是有风险的；如果小于或者HandlePromotionFailure=false，则改为进行一次Full GC。   
    3. CMS GC时出现promotion failed（晋升失败）和concurrent mode failure（并发模式失败）  
    &emsp; 执行CMS GC的过程中同时有对象要放入老年代，而此时老年代空间不足（可能是GC过程中浮动垃圾过多导致暂时性的空间不足），便会报Concurrent Mode Failure错误，并触发Full GC。  
    4. <font color = "red">系统调用System.gc()</font>  
    &emsp; 只是建议虚拟机执行Full GC，但是虚拟机不一定真正去执行。不建议使用这种方式，而是让虚拟机管理内存。  


# 1. GC算法与分代回收
<!-- 
Stop The World 是何时发生的？ 
https://mp.weixin.qq.com/s/AdF--fvDq63z0Inr2-JSHw
https://mp.weixin.qq.com/s/UrJ3cu8GvlOSg5CagrX64g

https://mp.weixin.qq.com/s/PodAB7knKJm6qjLfPEFIaw

-->

## 1.1. GC算法  
<!-- 

-->
&emsp; GC常用的算法：标记-清除(Mark-Sweep)、复制(Copying)、标记-整理(Mark-Compact)、分代收集(新生用复制，老年用标记-整理)。  

### 1.1.1. 标记-清除(Mark-Sweep)算法  
1. <font color = "red">标记-清除算法是最基础的收集算法，是因为后续的收集算法大多都是以标记-清除算法为基础，对其缺点进行改进而得到的。</font>  
2. **<font color = "clime">标记-清除算法分为两个阶段：标记阶段和清除阶段。</font>** 标记阶段是标记出所有需要被回收的对象，清除阶段就是回收被标记的对象所占用的空间。  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-73.png)  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-76.png)  
3. 特点：  
    * 优点：  
        1. 算法相对简单
        2. 存活对象比较多的情况下效率比较高
    * 缺点：
        1. **<font color = "red">执行过程中：</font>** 效率偏低，两遍扫描，标记和清除都比较耗时。执行效率不稳定，如果Java堆中包含大量对象，而且其中大部分是需要被回收的，这时必须进行大量标记和清除的动作，导致标记和清除两个过程的执行效率都随对象数量增长而降低；  
        2. **<font color = "red">执行后：</font>**（位置不连续，产生碎片）<font color = "clime">内存空间的碎片化问题，</font>清除后产生大量不连续的内存碎片。如果有大对象会出现空间不够的现象，从而不得不提前触发另一次垃圾收集动作。 

### 1.1.2. 标记-复制(Copying)算法 
1. 标记-复制算法常被简称为复制算法。<font color = "red">为了解决标记-清除算法面对大量可回收对象时执行效率低的问题。</font>  
2. 标记-复制算法的执行过程：  
&emsp; 将可用内存按容量划分为大小相等的两块，每次只使用其中的一块。当这一块的内存用完了，就将还存活着的对象复制到另外一块上面，然后再把已使用过的内存空间一次清理掉。  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-74.png)  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-77.png)  
&emsp; <font color = "red">如果内存中多数对象都是存活的，这种算法将会产生大量的内存间复制的开销</font>， **<font color = "clime">但对于多数对象都是可回收的情况，算法需要复制的就是占少数的存活对象，而且每次都是针对整个半区进行内存回收，分配内存时也就不用考虑有空间碎片的复杂情况，只要移动堆顶指针，按顺序分配即可。</font>** 这样实现简单，运行高效，不过其缺陷也显而易见，这种复制回收算法的代价是<font color = "red">将可用内存缩小为了原来的一半</font>，空间浪费多了一点。 
3. 特点：  
    * 适用于存活对象较少的情况。  
    * 优点：  
        1. 只扫描一次，效率提高。  
        2. 没有碎片，空间连续。
    * 缺点：  
        1. 移动复制对象，需要调整对象引用。  
        2. 50%的内存空间始终空闲浪费，存活对象越多效率越低。

### 1.1.3. 标记-整理(Mark-Compact)算法  
1. 为了解决标记-复制算法的缺陷，充分利用内存空间，提出了标记-整理算法。标记-整理算法的标记过程仍然与“标记-清除”算法一样，但后续步骤不是直接对可回收对象进行清理，而是让所有存活的对象都向内存空间一端移动，然后直接清理掉边界以外的内存。标记-整理算法的执行过程：  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-75.png)  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-78.png)  

3. 特点：  
    * 优点：
        1. 没有碎片，空间连续，方便对象分配。  
        2. 不会产生内存减半
    * 缺点：<font color = "red">扫描两次，指针需要调整(移动对象)，效率偏低。</font>  


&emsp; **<font color = "clime">标记清除和标记整理都需要扫描两次。</font>**  

## 1.2. 分代收集 
<!--
卡表与Remembered Set
★★★ https://blog.csdn.net/Sqdmn/article/details/103978643/
https://mp.weixin.qq.com/s/3YHHtuPENiV_2ZXfHHuD4A


https://mp.weixin.qq.com/s/dWg5S7m-LUQhxUofHfqb3g

视频
https://www.bilibili.com/video/BV1Jy4y1p7t8

分代收集理论

当前商业虚拟机的垃圾收集器大多数都遵循了“分代收集”的设计理论，分代收集理论其实是一套符合大多数程序运行实际情况的经验法则，主要建立在两个分代假说之上：

    弱分代假说：绝大多数对象都是朝生夕灭的

    强分代假说：熬过越多次垃圾收集过程的对象就越难以消亡

这两个分代假说共同奠定了多款常用垃圾收集器的一致设计原则：收集器应该将 Java 堆划分出不同的区域，将回收对象依据年龄(即对象熬过垃圾收集过程的次数)分配到不同的区域之中存储，把存活时间短的对象集中在一起，每次回收只关注如何保留少量存活的对象，即新生代(Young Generation)；把难以消亡的对象集中在一起，虚拟机就可以使用较低的频率来回收这个区域，即老年代(Old Generation)

正因为划出了不同的区域，垃圾收集器才可以每次只回收其中一个或多个区域，因此才有了“Minor GC”、“Major GC”、“Full GC”这样的回收类型划分，也才能够针对不同的区域采用不同的垃圾收集算法，因而有了“标记-复制”算法、“标记-清除”算法、“标记-整理”算法
-->

<!-- https://mp.weixin.qq.com/s/dWg5S7m-LUQhxUofHfqb3g -->

### 1.2.1. ~~新生代和老年代采用的算法~~
<!-- 
年轻代使用复制算法
https://blog.csdn.net/weixin_38118016/article/details/115354336
为何新生代和老年代使用的算法不一样？
https://www.kuangstudy.com/bbs/1327624514502131713
https://www.jianshu.com/p/1d6c44415dbc
为何新生代和老年代使用的算法不一？
https://blog.csdn.net/qq_43644398/article/details/109173054
-->

### 1.2.2. 分代收集理论
&emsp; 分代收集算法(Generational Collection)严格来说并不是一种思想或理论，是融合上述3种基础的算法思想，产生的针对不同情况所采用不同算法的一套组合。  
&emsp; 大多数对象都是朝生夕死的，所以把堆分为了新生代、老年代，以及永生代(JDK8 里面叫做元空间)，方便按照不同的代进行不同的垃圾回收。新生代又被进一步划分为Eden(伊甸园)和 Survivor(幸存者)区，它们的比例是8：1：1。  
&emsp; 新生代采用复制算法；老年代采用标记-整理算法。 **<font color = "clime">注意：CMS回收老年代，但采用标记-清除算法；CMS收集器也会在内存空间的碎片化程度已经大到影响对象分配时，采用标记-整理算法收集一次，以获得规整的内存空间。</font>**  

 

## 1.3. HotSpot GC分类  
<!-- 
其实 GC 分为两大类，分别是 Partial GC 和 Full GC。

Partial GC 即部分收集，分为 young gc、old gc、mixed gc。

    young gc：指的是单单收集年轻代的 GC。
    old gc：指的是单单收集老年代的 GC。
    mixed gc：这个是 G1 收集器特有的，指的是收集整个年轻代和部分老年代的 GC。

Full GC 即整堆回收，指的是收取整个堆，包括年轻代、老年代，如果有永久代的话还包括永久代。

其实还有 Major GC 这个名词，在《深入理解Java虚拟机》中这个名词指代的是单单老年代的 GC，也就是和 old gc 等价的，不过也有很多资料认为其是和 full gc 等价的。

还有 Minor GC，其指的就是年轻代的 gc。
young gc 触发条件是什么？

大致上可以认为在年轻代的 eden 快要被占满的时候会触发 young gc。

为什么要说大致上呢？因为有一些收集器的回收实现是在 full gc 前会让先执行以下 young gc。

比如 Parallel Scavenge，不过有参数可以调整让其不进行 young gc。

可能还有别的实现也有这种操作，不过正常情况下就当做 eden 区快满了即可。

eden 快满的触发因素有两个，一个是为对象分配内存不够，一个是为 TLAB 分配内存不够。
full gc 触发条件有哪些？

这个触发条件稍微有点多，我们来看下。

    在要进行 young gc 的时候，根据之前统计数据发现年轻代平均晋升大小比现在老年代剩余空间要大，那就会触发 full gc。
    有永久代的话如果永久代满了也会触发 full gc。
    老年代空间不足，大对象直接在老年代申请分配，如果此时老年代空间不足则会触发 full gc。
    担保失败即 promotion failure，新生代的 to 区放不下从 eden 和 from 拷贝过来对象，或者新生代对象 gc 年龄到达阈值需要晋升这两种情况，老年代如果放不下的话都会触发 full gc。
    执行 System.gc()、jmap -dump 等命令会触发 full gc。
-->

<!-- 
https://cloud.tencent.com/developer/article/1582661
https://www.zhihu.com/question/41922036

-->

![image](http://182.92.69.8:8081/img/java/JVM/JVM-98.png)  
&emsp; 针对HotSpot VM的实现，它里面的GC其实准确分类只有两大种：  
* Partial GC：并不收集整个GC堆的模式  
    * Young GC：只收集young gen的GC  
    * Old GC：只收集old gen的GC。只有CMS的concurrent collection是这个模式  
    * Mixed GC：收集整个young gen以及部分old gen的GC。只有G1有这个模式  
* Full GC：收集整个堆，包括young gen、old gen、perm gen(如果存在的话)等所有部分的模式。  
&emsp; Major GC通常是跟full GC是等价的，收集整个GC堆。但因为HotSpot VM发展了这么多年，外界对各种名词的解读已经完全混乱了，当有人说“major GC”的时候一定要问清楚指的是上面的full GC还是old GC。

### 1.3.1. Yong GC  
<!-- 
https://www.cnblogs.com/williamjie/p/9516367.html
-->
![image](http://182.92.69.8:8081/img/java/JVM/JVM-99.png)  

#### 1.3.1.1. ~~YGC触发时机~~
<!-- 

***** https://blog.csdn.net/weixin_28901327/article/details/114427714
-->

1. Eden区空间满；<font color = "red">Survivor区中From和To区域默认使用率都是50%，Survivor区使用率设置使用命令-XX:TargetSurvivorRatio=80。</font>  
2. 空间分配担保成功


young gc 触发条件是什么？  
大致上可以认为在年轻代的 eden 快要被占满的时候会触发 young gc。  
为什么要说大致上呢？因为有一些收集器的回收实现是在 full gc 前会让先执行以下 young gc。  
比如 Parallel Scavenge，不过有参数可以调整让其不进行 young gc。  
可能还有别的实现也有这种操作，不过正常情况下就当做 eden 区快满了即可。  
eden 快满的触发因素有两个，一个是为对象分配内存不够，一个是为 TLAB 分配内存不够。  

#### 1.3.1.2. YGC执行流程
&emsp; **YGC执行流程：(young GC中有部分存活对象会晋升到old gen，所以young GC后old gen的占用量通常会有所升高)**  
1. 大部分对象在Eden区中生成。当Eden占用完时，垃圾回收器进行回收。  
2. 回收时先将eden区存活对象复制到一个survivor0区，然后清空eden区。   
3. 当这个survivor0区也存放满了时，则将eden区和survivor0区（使用的survivor中的对象也可能失去引用）存活对象复制到另一个survivor1区，然后清空eden和这个survivor0区，此时survivor0区是空的，然后将survivor0区和survivor1区交换，即保持survivor1区为空， 如此往复。  
4. 每经过一次YGC，对象年龄加1，当对象寿命超过阈值时，会晋升至老年代，最大寿命15(4bit)。  

### 1.3.2. Major GC  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-100.png)  

### 1.3.3. Full GC  
<!-- 
https://zhidao.baidu.com/question/717236418134267765.html
https://blog.csdn.net/qq_38384440/article/details/81710887
-->
![image](http://182.92.69.8:8081/img/java/JVM/JVM-101.png)  

#### 1.3.3.1. FGC的触发时机
&emsp; **<font color = "red">Full GC的触发时机：( 系统调用--->  老年代或永久代不足 ---> 执行GC时，老年代或永久的不足 ---> 回收器(例如CMS))</font>**  
1. <font color = "red">系统调用System.gc()</font>  
&emsp; 只是建议虚拟机执行Full GC，但是虚拟机不一定真正去执行。不建议使用这种方式，而是让虚拟机管理内存。  
2. 老年代或永久的不足
    1. 老年代空间不足(92%)  
    &emsp; 老年代空间不足的常见场景为大对象直接进入老年代、长期存活的对象进入老年代等。  
    &emsp; 为了避免以上原因引起的Full GC，应当尽量不要创建过大的对象以及数组。除此之外，可以通过-Xmn虚拟机参数调大新生代的大小，让对象尽量在新生代被回收掉，不进入老年代。还可以通过 -XX:MaxTenuringThreshold调大对象进入老年代的年龄，让对象在新生代多存活一段时间。  
    2. JDK 1.7及以前的永久代空间不足  
    &emsp; 为避免以上原因引起的Full GC，可采用的方法为增大永久代空间或转为使用CMS GC。  
3. 老年代不满足年轻代的晋升  
    1. 统计得到的Minor GC晋升到老年代的平均大小大于旧生代的剩余空间  
    &emsp; Hotspot为了避免由于新生代对象晋升到旧生代导致老年代空间不足的现象，在进行Minor GC时，做了一个判断，如果之前统计所得到的Minor GC晋升到旧生代的平均大小大于旧生代的剩余空间，那么就直接触发Full GC。  
    2. 空间分配担保失败  
    &emsp; **<font color = "clime">JVM在发生Minor GC之前，虚拟机会检查老年代最大可用的连续空间是否大于新生代所有对象的总空间，</font>** 如果大于，则此次Minor GC是安全的；如果小于，则虚拟机会查看HandlePromotionFailure设置项的值是否允许担保失败。如果HandlePromotionFailure=true，那么会继续检查老年代最大可用连续空间是否大于历次晋升到老年代的对象的平均大小，如果大于则尝试进行一次Minor GC，但这次Minor GC依然是有风险的；如果小于或者HandlePromotionFailure=false，则改为进行一次Full GC。    
4. CMS GC时出现promotion failed和concurrent mode failure  
&emsp; 执行CMS GC的过程中同时有对象要放入老年代，而此时老年代空间不足(可能是GC过程中浮动垃圾过多导致暂时性的空间不足)，便会报Concurrent Mode Failure错误，并触发Full GC。  
