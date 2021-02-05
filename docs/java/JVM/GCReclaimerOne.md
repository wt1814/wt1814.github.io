
<!-- TOC -->

- [1. 重要的垃圾收集器](#1-重要的垃圾收集器)
    - [1.1. CMS](#11-cms)
        - [1.1.1. 回收流程](#111-回收流程)
        - [1.1.2. 优点与缺点](#112-优点与缺点)
        - [1.1.3. CMS使用](#113-cms使用)
    - [1.2. G1](#12-g1)
        - [1.2.1. G1的内存布局](#121-g1的内存布局)
        - [1.2.2. 回收流程](#122-回收流程)
        - [1.2.3. G1优缺点](#123-g1优缺点)
        - [1.2.4. 使用G1](#124-使用g1)

<!-- /TOC -->

# 1. 重要的垃圾收集器  

## 1.1. CMS  
&emsp; CMS（Conrrurent Mark Sweep）收集器是以 **<font color = "lime">获取最短回收停顿时间为目标</font>** 的收集器。  

### 1.1.1. 回收流程  
&emsp; 使用标记-清除算法，收集过程分为如下四步：  
1. 初始标记，标记GCRoots能直接关联到的对象，时间很短。  
2. 并发标记，进行GCRoots Tracing（可达性分析）过程，过程耗时较长但是不需要停顿用户线程，可以与垃圾收集线程一起并发运行。  
3. 重新标记，修正并发标记期间，因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录，停顿时间通常会比初始标记阶段稍长一些，但也远比并发标记阶段的时间短。  
4. 并发清除，理删除掉标记阶段判断的已经死亡的对象，由于不需要移动存活对象，所以这个阶段也是可以与用户线程同时并发的。  

&emsp; **<font color = "red">由于在整个过程中耗时最长的并发标记和并发清除阶段中，垃圾收集器线程都可以与用户线程一起工作，所以从总体上来说，CMS收集器的内存回收过程是与用户线程一起并发执行的。</font>**  

### 1.1.2. 优点与缺点  
&emsp; CMS是一款优秀的收集器，它最主要的优点在名字上已经体现出来： **<font color = "clime">并发收集、低停顿。</font>** 但是也有以下 **<font color = "red">三个明显的缺点：</font>**  

* **<font color = "lime">吞吐量低</font>**    
&emsp; <font color = "red">由于CMS在垃圾收集过程使用用户线程和GC线程并行执行，从而线程切换会有额外开销，</font>因此CPU吞吐量就不如在GC过程中停止一切用户线程的方式来的高。
* **<font color = "lime">无法处理浮动垃圾，导致频繁Full GC</font>**  
&emsp; <font color = "red">由于垃圾清除过程中，用户线程和GC线程并发执行，也就是用户线程仍在执行，那么在执行过程中会产生垃圾，这些垃圾称为"浮动垃圾"。</font>  
&emsp; 如果CMS在垃圾清理过程中，用户线程需要在老年代中分配内存时发现空间不足，就需再次发起Full GC，而此时CMS正在进行清除工作，因此此时只能由Serial Old临时对老年代进行一次Full GC。  
* **<font color = "lime">使用"标记-清除"算法，产生碎片空间</font>**  
&emsp; 由于CMS使用了"标记-清除"算法, 因此清除之后会产生大量的碎片空间，不利于空间利用率。不过CMS提供了应对策略：开启-XX:+UseCMSCompactAtFullCollection，开启该参数后，每次FullGC完成后都会进行一次内存压缩整理，将零散在各处的对象整理到一块儿。但每次都整理效率不高，因此提供了另外一个参数，设置参数-XX:CMSFullGCsBeforeCompaction，本参数告诉CMS，经过了N次Full GC过后再进行一次内存整理。  

### 1.1.3. CMS使用
&emsp; 参数控制：  

    -XX:+UseConcMarkSweepGC 使用CMS收集器
    -XX:+ UseCMSCompactAtFullCollection Full GC后，进行一次碎片整理；整理过程是独占的，会引起停顿时间变长
    -XX:+CMSFullGCsBeforeCompaction 设置进行几次Full GC后，进行一次碎片整理-XX:ParallelCMSThreads 设定CMS的线程数量（一般情况约等于可用CPU数量）

## 1.2. G1  
<!-- 
https://www.cnblogs.com/cuizhiquan/articles/10961354.html
https://mp.weixin.qq.com/s/dWg5S7m-LUQhxUofHfqb3g
|组合7|G1GC|G1GC|-XX:+UnlockExperimentalVMOptions -XX:+UseG1GC  <br/>#开启  <br/>-XX:MaxGCPauseMillis =50  #暂停时间目标  <br/>-XX:GCPauseIntervalMillis =200  #暂停间隔目标  <br/>-XX:+G1YoungGenSize=512m  #年轻代大小  <br/>-XX:SurvivorRatio=6  #幸存区比例|

-->
&emsp; G1(Garbage first)是目前技术发展的最前沿成果之一，HotSpot开发团队赋予它的使命是未来可以替换掉JDK1.5中发布的CMS收集器。  
&emsp; G1是一款而向服务端应用的垃圾收集器。G1回收器在jdk1.9后成为了JVM的默认垃圾回收器。通过把Java堆分成大小相等的多个独立区域，回收时计算出每个区域回收所获得的空间以及所需时间的经验值，根据记录两个值来判断哪个区域最具有回收价值，所以叫 **<font color = "lime">Garbage First（垃圾优先）</font>** 。

### 1.2.1. G1的内存布局  
<!-- 深入理解Java虚拟机 第3版 -->
&emsp; **<font color = "red">G1的内存布局：</font>** 在G1之前的垃圾收集器，收集的范围都是整个新生代或者老年代，而G1不再是这样。使用G1收集器时，Java堆的内存布局与其他收集器有很大差别，它<font color = "red">将整个Java堆划分为多个大小相等的独立区域（Region）</font>，虽然还保留有新生代和老年代的概念，但新生代和老年代不再是物理隔离的了，它们都是一部分（可以不连续）Region的集合。  
&emsp; **<font color = "red">G1收集器能建立可预测的停顿时间模型，</font>** 是因为它可以有计划地避免在整个Java堆中进行全区域的垃圾收集。<font color = "red">G1跟踪各个Region里面的垃圾堆积的价值大小（回收所获得的空间大小以及回收所需时间的经验值），在后台维护一个优先列表，</font><font color = "lime">每次根据允许的收集时间，优先回收价值最大的Region/这也就是Garbage-First名称的来由）。</font>这种使用Region划分内存空间以及有优先级的区域回收方式，保证了G1收集器在有限的时间内可以获取尽可能高的收集效率。  

        G1收集器避免全区域垃圾收集，它把堆内存划分为大小固定的几个独立区域，并且跟踪这些区域的垃圾收集进度，同时在后台维护一个优先级列表，每次根据所允许的收集时间，优先回收垃圾最多的区域。区域划分和优先级区域回收机制，确保G1收集器可以在有限时间获得最高的垃圾收集效率。 
    
&emsp; Region不可能是孤立的。一个对象分配在某个Region中，它并非只能被本Region中的其他对象引用，而是可以与整个Java堆任意的对象发生引用关系。  
&emsp; 在G1收集器中，Region之间的对象引用以及其他收集器中的新生代与老年代之间的对象引用，<font color = "red">虚拟机是使用Remembered Set 来避免全堆扫描的</font>。G1中每个Region都有一个与之对应的Remembered Set，虚拟机发现程序在对Reference类型的数据进行写操作时，会产生一个Write Barrier暂时中断写操作，检查Reference引用的对象是否处于不同的Region之中（在分代的例子中就是检查是否老年代中的对象引用了新生代中的对象），如果是，便通过CardTable 把相关引用信息记录到被引用对象所属的Region的Remembered Set之中。当进行内存回收时，在GC根节点的枚举范围中加入Remembered Set即可保证不对全堆扫描也不会有遗漏。  

### 1.2.2. 回收流程  
<!-- https://baijiahao.baidu.com/s?id=1663956888745443356&wfr=spider&for=pc-->
&emsp; 不去计算用户线程运行过程中的动作（如使用写屏障维护记忆集的操作），G1收集器的运作过程大致可划分为以下四个步骤：  

* 初始标记（Initial Marking）：仅仅只是标记一下GC Roots能直接关联到的对象，并且修改TAMS 指针的值，让下一阶段用户线程并发运行时，能正确地在可用的Region中分配新对象。这个阶段需要停顿线程，但耗时很短，而且是借用进行Minor GC的时候同步完成的，所以G1收集器在这个阶段实际并没有额外的停顿。    
* 并发标记（Concurrent Marking）：从GC Root开始对堆中对象进行可达性分析，递归扫描整个堆里的对象图，找出要回收的对象，这阶段耗时较长，但可与用户程序并发执行。当对象图扫描完成以后，还要重新处理SATB记录下的在并发时有引用变动的对象。  
* 最终标记（Final Marking）：对用户线程做另一个短暂的暂停，用于处理并发阶段结束后仍遗留下来的最后那少量的SATB记录。   
* 筛选回收（Live Data Counting and Evacuation）：负责更新Region的统计数据，对各个Region的回收价值和成本进行排序，根据用户所期望的停顿时间来制定回收计划，可以自由选择任意多个Region 构成回收集，然后把决定回收的那一部分Region的存活对象复制到空的Region中，再清理掉整个旧Region的全部空间。这里的操作涉及存活对象的移动，是必须暂停用户线程，由多条收集器线程并行完成的。    

&emsp; G1收集器除了并发标记外，其余阶段也是要完全暂停用户线程的， 换言之，它并非纯粹地追求低延迟， **<font color = "lime">官方给它设定的目标是在延迟可控的情况下获得尽可能高的吞吐量，</font>** 所以才能担当起“全功能收集器”的重任与期望。  

-----
&emsp; 收集过程：  
&emsp; ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-95.png)  
&emsp; G1的回收过程主要分为3类：  
&emsp; （1）G1“年轻代”的垃圾回收，同样叫Minor G1，这个过程和我们前面描述的类似，发生时机就是Eden区满的时候。  
（2）老年代的垃圾收集，严格上来说其实不算是收集，它是一个“并发标记”的过程，顺便清理了一点点对象。  
&emsp; （3）真正的清理，发生在“混合模式”，它不止清理年轻代，还会将老年代的一部分区域进行清理。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-96.png)  

### 1.2.3. G1优缺点  
&emsp; G1收集器有以下特点：  

* **<font color = "lime">并行和并发：</font>** 使用多个CPU来缩短Stop The World停顿时间，与用户线程并发执行。  
* 分代收集：虽然G1可以不需要其他收集器配合就能独立管理整个GC堆，但是还是保留了分代的概念。它能够采用不同的方式去处理新创建的对象和已经存活了一段时间，熬过多次GC的旧对象以获取更好的收集效果。  
* **<font color = "lime">空间整合(不产生内存碎片)：</font>** 与CMS的“标记--清理”算法不同，<font color = "red">G1从整体来看是基于“标记整理”算法实现的收集器；从局部上来看是基于“复制”算法实现的。</font>这两种算法都意味着<font color = "lime">G1运作期不会产生内存空间碎片</font>，收集后能提供规整的可用内存。这种特性有利于程序长时间运行，分配大对象吋不会因为无法找到连续内存空而提前触发下一次GC。  
* **<font color = "lime">可预测的停顿：</font>** 这是G1相对于CMS的另一个大优势，<font color = "red">降低停顿时间是G1和CMS共同的关注点，但G1除了追求低停顿外，还能建立可预测的停顿时间模型，</font>能让使用者明确指定在一个长度为M毫秒的时间片段内，消耗在垃圾收集上的时间不得超过N毫秒。  

&emsp; 比起CMS，G1的弱项也可以列举出不少，如在用户程序运行过程中，G1无论是为了垃圾收集产生的内存占用（Footprint）还是程序运行时的额外执行负载
（Overload）都要比CMS要高。 

### 1.2.4. 使用G1  
&emsp; **开启G1：** 在JDK9之前，JDK7和JDK8默认都是ParallelGC垃圾回收。到了JDK9，G1才是默认的垃圾回收器。所以如果JDK7或者JDK8需要使用G1的话，需要通过参数（-XX:+UseG1GC）显示执行垃圾回收器。而JDK9以后的版本，不需要任何JVM参数，默认就是G1垃圾回收模式，显示指定G1运行一个Demo程序如下：  

```java
java -Xmx1g -Xms1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar demo.jar
```



