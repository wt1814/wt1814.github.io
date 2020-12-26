

<!-- TOC -->

- [1. JVM实践](#1-jvm实践)
    - [1.1. JVM调优(落地)](#11-jvm调优落地)
        - [1.1.1. 内存设置](#111-内存设置)
        - [1.1.2. GC调优](#112-gc调优)
            - [1.1.2.1. YGC、FGC优化](#1121-ygcfgc优化)
                - [1.1.2.1.1. ※※※FGC过高](#11211-※※※fgc过高)
            - [1.1.2.2. GC策略调整](#1122-gc策略调整)
    - [1.2. JVM问题排查](#12-jvm问题排查)
        - [1.2.1. 线上可能出现的问题](#121-线上可能出现的问题)
        - [1.2.2. CPU飚高](#122-cpu飚高)
            - [1.2.2.1. 现象](#1221-现象)
            - [1.2.2.2. 排查步骤](#1222-排查步骤)
                - [1.2.2.2.1. 情况一](#12221-情况一)
                - [1.2.2.2.2. 情况二](#12222-情况二)
            - [※※※jstack文件进行分析](#※※※jstack文件进行分析)
        - [1.2.3. 内存溢出排查实战](#123-内存溢出排查实战)
            - [1.2.3.1. 堆溢出演示](#1231-堆溢出演示)
            - [1.2.3.2. 内存溢出的解决方案](#1232-内存溢出的解决方案)
                - [1.2.3.2.1. 使用内存查看工具分析堆dump文件](#12321-使用内存查看工具分析堆dump文件)
                - [1.2.3.2.2. jvm内存快照dump文件太大，怎么分析](#12322-jvm内存快照dump文件太大怎么分析)
        - [1.2.4. 线程死锁](#124-线程死锁)
    - [1.3. 系统其他问题](#13-系统其他问题)
        - [1.3.1. qps飘高](#131-qps飘高)

<!-- /TOC -->

# 1. JVM实践  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-57.png)  

## 1.1. JVM调优(落地)  
&emsp; JVM的调优主要有两个方面：内存调优、垃圾回收策略调优。  

    获取堆内存日志，调整内存比例或者gc回收策略； 

### 1.1.1. 内存设置
&emsp; Jvm调优Oracle官网有一份指导说明：https://docs.oracle.com/middleware/11119/wls/PERFM/jvm_tuning.htm#i1146060  

&emsp; 各分区的大小对GC的性能影响很大。如何将各分区调整到合适的大小，分析活跃数据的大小是很好的切入点。  
&emsp; **活跃数据的大小是指，应用程序稳定运行时长期存活对象在堆中占用的空间大小，也就是Full GC后堆中老年代占用空间的大小。** 可以通过GC日志中Full GC之后老年代数据大小得出，比较准确的方法是在程序稳定后，多次获取GC数据，通过取平均值的方式计算活跃数据的大小。活跃数据和各分区之间的比例关系如下：  

|空间	|倍数|
|---|---|
|总大小	|3--4倍活跃数据的大小|
|新生代	|1--1.5倍活跃数据的大小|
|老年代	|2--3倍活跃数据的大小|
|永久代	|1.2--1.5倍Full GC后的永久代空间占用|

&emsp; 例如，根据GC日志获得老年代的活跃数据大小为300MB，那么各分区大小可以设为：  

```
总堆：1200MB = 300MB × 4
新生代：450MB = 300MB × 1.5
老年代：750MB = 1200MB - 450MB
```
&emsp; 这部分设置仅仅是堆大小的初始值，后面的优化中，可能会调整这些值，具体情况取决于应用程序的特性和需求。

### 1.1.2. GC调优  
#### 1.1.2.1. YGC、FGC优化  
&emsp; <font color = "red">GC性能指标有吞吐量、GC开销、时长、频率、堆空间、对象生命周期。</font><font color = "lime">GC 的优化主要有2个维度，一是频率，二是时长。</font>  

    Minor GC执行时间不到50ms，Minor GC执行频率约10秒一次；
    Full GC执行时间不到1s，Full GC执行频率不低于10分钟1次；

* YGC  
&emsp; YGC，首先看频率，如果YGC超过10秒一次，甚至更长，说明系统内存过大，应该缩小容量，如果频率很高，说明Eden区过小，可以将Eden区增大，但整个新生代的容量应该在堆的30% - 40%之间，eden、from 和to的比例应该在8\:1\:1左右，这个比例可根据对象晋升的大小进行调整。
如果YGC时间过长呢？YGC有2个过程，一个是扫描，一个是复制，通常扫描速度很快，复制速度相比而言要慢一些，如果每次都有大量对象要复制，就会将STW时间延长，还有一个情况就是StringTable ，这个数据结构中存储着String.intern方法返回的常连池的引用，YGC每次都会扫描这个数据结构（HashTable），如果这个数据结构很大，且没有经过FGC，那么也会拉长STW时长，还有一种情况就是操作系统的虚拟内存，当GC时正巧操作系统正在交换内存，也会拉长STW时长。  

* FGC  
&emsp; 实际上，FGC只能优化频率，无法优化时长，因为这个时长无法控制。  
&emsp; <font color = "red">对于Full GC较多的情况，其主要有如下两个特征： </font> 

    * <font color = "red">线上多个线程的CPU都超过了100%，通过jstack命令可以看到这些线程主要是垃圾回收线程。</font>  
    * 通过jstat命令监控GC情况，可以看到Full GC次数非常多，并且次数在不断增加。  

##### 1.1.2.1.1. ※※※FGC过高  
&emsp; 使用jstack来分析GC是不是太频繁， **<font color = "lime">使用jstat -gc pid 1000命令来对gc分代变化情况进行观察，</font>** 1000表示采样间隔(ms)，S0C/S1C、S0U/S1U、EC/EU、OC/OU、MC/MU分别代表两个Survivor区、Eden区、老年代、元数据区的容量和使用量。YGC/YGT、FGC/FGCT、GCT则代表YoungGc、FullGc的耗时和次数以及总耗时。如果看到gc比较频繁，再针对gc方面做进一步分析。   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-81.png)  


&emsp; **<font color = "lime">FGC过高可能是内存参数设置不合理，也有可能是代码中某个位置读取数据量较大导致系统内存耗尽。FGC过高可能导致CPU飚高。</font>**  

    FGC过高，会导致CPU飚高；但CPU飚高不一定是FGC过高。
&emsp; 解决思路：打印线程堆栈信息。查看线程堆栈是用户线程，还是GC线程。如果是GC线程，打印内存快照进行分析。  

1. 如果FGC后还有大量对象，说明Old区过小，应该扩大Old区；  
2. 如果FGC后效果很好，说明Old区存在了大量短命的对象，优化的点应该是让这些对象在新生代就被YGC掉，通常的做法是增大新生代；  
3. 如果有大而短命的对象，通过参数设置对象的大小，要让这些对象进入Old区，还需要检查晋升年龄是否过小；  
4. 如果YGC后，有大量对象因为无法进入Survivor区从而提前晋升，这时应该增大Survivor区，但不宜太大。  
5. 也可以直接增大内存。  

<!-- 
&emsp; <font color = "red">内存参数设置不合理，有以下策略：</font>  

* 策略1：将新对象预留在新生代，由于Full GC的成本远高于Minor GC，因此尽可能将对象分配在新生代是明智的做法，实际项目中根据GC日志分析新生代空间大小分配是否合理，适当通过“-Xmn”命令调节新生代大小，最大限度降低新对象直接进入老年代的情况。  
* 策略2：大对象进入老年代，虽然大部分情况下，将对象分配在新生代是合理的。但是对于大对象这种做法却值得商榷，大对象如果首次在新生代分配可能会出现空间不足导致很多年龄不够的小对象被分配的老年代，破坏新生代的对象结构，可能会出现频繁的full gc。因此，对于大对象，可以设置直接进入老年代（当然短命的大对象对于垃圾回收老说简直就是噩梦）。-XX:PretenureSizeThreshold 可以设置直接进入老年代的对象大小。  
* 策略3：合理设置进入老年代对象的年龄，-XX:MaxTenuringThreshold设置对象进入老年代的年龄大小，减少老年代的内存占用，降低full gc发生的频率。  
* 策略4：设置稳定的堆大小，堆大小设置有两个参数：-Xms初始化堆大小，-Xmx最大堆大小。  

&emsp; <font color = "red">FGC过高，内存参数设置不合理，一般是Old区内存不够导致 FGC。</font>  
-->





#### 1.1.2.2. GC策略调整  
&emsp; 当Java程序性能达不到既定目标，且其他优化手段都已经穷尽时，通常需要调整垃圾回收器来进一步提高性能，选择一款合适的垃圾回收器。  

<!--
&emsp; Java虚拟机的垃圾回收策略一般分为：串行收集器、并行收集器和并发收集器。  

* 串行收集器：  
    1. -XX:+UseSerialGC：代表垃圾回收策略为串行收集器，即在整个扫描和复制过程采用单线程的方式来进行，适用于单CPU、新生代空间较小及对暂停时间要求不是非常高的应用上，是client级别默认的GC方式，主要在JDK1.5之前的垃圾回收方式。  

* 并发收集器：
    1. -XX:+UseParallelGC：代表垃圾回收策略为并行收集器(吞吐量优先)，即在整个扫描和复制过程采用多线程的方式来进行，适用于多CPU、对暂停时间要求较短的应用上，是server级别默认采用的GC方式。此配置仅对年轻代有效。该配置只能让年轻代使用并发收集，而年老代仍旧使用串行收集。  
    2. -XX:ParallelGCThreads=4：配置并行收集器的线程数，即：同时多少个线程一起进行垃圾回收。此值最好配置与处理器数目相等。  
    3. -XX:+UseParallelOldGC：配置年老代垃圾收集方式为并行收集。JDK6.0支持对年老代并行收集。  
    4. -XX:MaxGCPauseMillis=100：设置每次年轻代垃圾回收的最长时间，如果无法满足此时间，JVM会自动调整年轻代大小，以满足此值。  
    5. -XX:+UseAdaptiveSizePolicy：设置此选项后，并行收集器会自动选择年轻代区大小和相应的Survivor区比例，以达到目标系统规定的最低相应时间或者收集频率等，此值建议使用并行收集器时，一直打开。  

* 并发收集器：  
    1. -XX:+UseConcMarkSweepGC:代表垃圾回收策略为并发收集器。  
 -->
 
## 1.2. JVM问题排查  

    获取线程stack快照，排查CPU飚高；  
    获取堆heap快照，排查内存溢出的问题；  

### 1.2.1. 线上可能出现的问题  
&emsp; <font color = "red">对于线上系统突然产生的运行缓慢问题，如果该问题导致线上系统不可用，</font><font color = "lime">那么首先需要做的就是，导出线程堆栈和内存信息，然后重启系统，尽快保证系统的可用性。</font>这种情况可能的原因主要有两种：  

* **<font color = "red">代码中某个位置读取数据量较大，导致系统内存耗尽，从而导致Full GC次数过多，系统缓慢；</font>**  
* 代码中有比较耗CPU的操作，导致CPU过高，系统运行缓慢；  

&emsp; 相对来说，这是出现频率最高的两种线上问题，而且它们会直接导致系统不可用。另外有几种情况也会导致某个功能运行缓慢，但是不至于导致系统不可用：  

* 代码某个位置有阻塞性的操作，导致该功能调用整体比较耗时，但出现是比较随机的；  
* 某个线程由于某种原因而进入WAITING状态，此时该功能整体不可用，但是无法复现；  
* 由于锁使用不当，导致多个线程进入死锁状态，从而导致系统整体比较缓慢。  

&emsp; 对于这三种情况，通过查看CPU和系统内存情况是无法查看出具体问题的，因为它们相对来说都是具有一定阻塞性操作，CPU和系统内存使用情况都不高，但是功能却很慢。  

### 1.2.2. CPU飚高  
<!-- 
https://www.cnblogs.com/klvchen/p/11089632.html

-->

#### 1.2.2.1. 现象   
&emsp; **<font color = "red">CPU过高可能是系统频繁的进行Full GC，导致系统缓慢。</font><font color = "lime">而平常也可能遇到比较耗时的计算，导致CPU过高的情况。</font>**

&emsp; **<font color = "lime">怎么区分导致CPU过高的原因具体是Full GC次数过多还是代码中有比较耗时的计算？</font>**  
&emsp; 如果是Full GC次数过多，那么通过jstack得到的线程信息会是类似于VM Thread之类的线程，而如果是代码中有比较耗时的计算，那么得到的就是一个线程的具体堆栈信息。如下是一个代码中有比较耗时的计算，导致CPU过高的线程信息：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-47.png)  
&emsp; 这里可以看到，在请求UserController的时候，由于该Controller进行了一个比较耗时的调用，导致该线程的CPU一直处于100%。可以根据堆栈信息，直接定位到UserController的34行，查看代码中具体是什么原因导致计算量如此之高。  

#### 1.2.2.2. 排查步骤  

##### 1.2.2.2.1. 情况一  
通过top命令查看CPU情况，如果CPU比较高。  

    1. 通过 top 命令找到 CPU 消耗最高的进程，并记住进程 ID。  
    2. 再次通过 top -Hp [进程 ID] 找到 CPU 消耗最高的线程 ID，并记住线程 ID。  
    3. 通过JDK提供的 jstack 工具 dump 线程堆栈信息到指定文件中。具体命令：jstack -l [进程 ID] >jstack.log。  
    4. 由于刚刚的线程 ID 是十进制的，而堆栈信息中的线程 ID 是16进制的，因此需要将10进制的转换成16进制的，并用这个线程 ID 在堆栈中查找。使用 printf "%x\n" [十进制数字] ，可以将10进制转换成16进制。  
    5. 通过刚刚转换的16进制数字从堆栈信息里找到对应的线程堆栈。就可以从该堆栈中看出端倪。  

1. 查找消耗cpu最高的进程PID  
&emsp; 执行top -c ，显示进程运行信息列表。按下P，进程按照cpu使用率排序。  
&emsp; 如下图所示，PID为3033的进程耗费cpu最高。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-42.png)  
2. 查找该进程下消耗cpu最高的线程号  
&emsp; 执行命令top -Hp 3033 ，显示一个进程的线程运行信息列表。按下P,进程按照cpu使用率排序。  
&emsp; 如下图所示，PID为3034的线程耗费cpu最高。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-43.png)  
&emsp; 这是十进制的数据，需转成十六进制为0xbda。  

3. 通过JDK提供的jstack工具dump线程堆栈信息到指定文件中。  
&emsp; jstack -l [进程 ID] >jstack.stack。 

    ```
    jstack -l 3033 > ./3033.stack
    ```
4. 转换进制  
&emsp; 由于刚刚的线程ID是十进制的，而堆栈信息中的线程ID是十六进制的，因此需要将10进制的转换成16进制的，并用这个线程ID在堆栈中查找。  
&emsp; 执行命令：printf "%x\n" [十进制数字] 。  
5. 过滤指定线程，查找对应的线程堆栈信息  
&emsp; 执行grep命令：  

    ```
    cat 3033.stack |grep 'bda' -C 8
    ```

    输出如下：  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-44.png)  


    注意：  
    这里又分为两种情况：  
    1: 如果是正常的用户线程，则通过该线程的堆栈信息查看其具体是在哪处用户代码处运行比较消耗CPU。  
    2: 如果该线程是VM Thread（VM Thread 指的就是垃圾回收的线程，一般前面会有 nid=0x.......，这里 nid 的意思就是操作系统线程 id），则通过 jstat -gcutil 命令监控当前系统的 GC 状况。然后通过 jmap dump:format=b,file= 导出系统当前的内存数据。导出之后将内存情况放到Mat 工具中进行分析即可得出内存中主要是什么对象比较消耗内存，进而可以处理相关代码。

##### 1.2.2.2.2. 情况二  
&emsp; 如果通过top命令看到CPU并不高，并且系统内存占用率也比较低。此时就可以考虑是否是由于另外三种情况导致的问题。  
&emsp; 具体的可以根据具体情况分析：  
1. 如果是接口调用比较耗时，并且是不定时出现，则可以通过压测的方式加大阻塞点出现的频率，从而通过 jstack 查看堆栈信息，找到阻塞点。
2. 如果是某个功能突然出现停滞的状况，这种情况也无法复现，此时可以通过多次导出 jstack 日志的方式对比哪些用户线程是一直都处于等待状态，这些线程就是可能存在问题的线程。
3. 如果通过 jstack 可以查看到死锁状态，则可以检查产生死锁的两个线程的具体阻塞点，从而处理相应的问题。  

#### ※※※jstack文件进行分析  
&emsp; 常见的是对整个jstack文件进行分析，通常比较关注WAITING和TIMED_WAITING的部分，BLOCKED就不用说了。我们可以使用命令cat jstack.log | grep "java.lang.Thread.State" | sort -nr | uniq -c来对jstack的状态有一个整体的把握，如果WAITING之类的特别多，那么多半是有问题啦。  






### 1.2.3. 内存溢出排查实战  

#### 1.2.3.1. 堆溢出演示  

```java
/**
 * VM Args：-Xms10m -Xmx10m -XX:+HeapDumpOnOutOfMemoryError
 */
public class HeapOOMTest {

    public static final int _1MB = 1024 * 1024;

    public static void main(String[] args) {
        List<byte[]> byteList = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            byte[] bytes = new byte[2 * _1MB];
            byteList.add(bytes);
        }
    }
}
```
&emsp; 输出  

```java
java.lang.OutOfMemoryError: Java heap space
Dumping heap to java_pid32372.hprof ...
Heap dump file created [7774077 bytes in 0.009 secs]
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at jvm.HeapOOMTest.main(HeapOOMTest.java:18)
```

#### 1.2.3.2. 内存溢出的解决方案  
1. **<font color = "lime">修改JVM启动参数，直接增加内存。</font>**  
2. 检查错误日志，查看“OutOfMemory”错误前是否有其它异常或错误。  
3. **<font color = "lime">对代码进行走查和分析，找出可能发生内存溢出的位置。</font>** 重点排查以下几点：  
    * 检查对数据库查询中，是否有一次获得全部数据的查询。一般来说，如果一次取十万条记录到内存，就可能引起内存溢出。这个问题比较隐蔽，在上线前，数据库中数据较少，不容易出问题，上线后，数据库中数据多了，一次查询就有可能引起内存溢出。对数据库查询尽量采用分页查询。  
    * 检查代码是否有死循环或递归调用。  
    * 检查是否有大循环重复产生新对象实体。  
    * 检查List、Map等集合对象是否有使用后，未清除的问题。List、Map等集合对象会始终存有对对象的引用，使得这些对象不能被GC回收。  
4. 使用内存查看工具动态查看内存快照。 

##### 1.2.3.2.1. 使用内存查看工具分析堆dump文件  
1. 保存内存快照（两种方法）：  
    1. 添加JVM参数，该参数作用是：在程序内存溢出时输出dump文件。参数：-XX:+HeapDumpOnOutOfMemoryError -Xms20m -Xmx20m    
    &emsp; 随后找到项目地址，会发现在Project本目录中出现了个hprof文件，至此就把堆内存快照保存下来了。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-45.png)  
    2. jmap命令：先运行对应的jar进程，jps找到该进程ID，然后jmap设置导出格式。  
    &emsp; **<font color = "lime">注：线上环境不能直接使用jmap命令。找到未进行GC的一个节点，从线上环境摘除。然后再使用jmap命令。</font>**    

        ``
        jps
        jmap -dump:format=b,file=heap.hprof <pid>
        ``

2. 使用内存工具分析  
&emsp; 有了dump文件，就可以通过dump分析工具进行分析，比如常用的MAT，Jprofile，jvisualvm等工具都可以分析，这些工具都能够看出到底是哪里溢出，哪里创建了大量的对象等等信息。  

##### 1.2.3.2.2. jvm内存快照dump文件太大，怎么分析  

<!-- 
https://www.cnblogs.com/liangzs/p/8489321.html
-->
&emsp; dump文件太大，使用linux下的mat，既Memory Analyzer Tools。   

### 1.2.4. 线程死锁  
&emsp; 死循环、死锁、阻塞、页面打开慢等问题，打印线程dump是最好的解决问题的途径。  
<!-- 
一文学会Java死锁和CPU 100% 问题的排查技巧
https://club.perfma.com/article/2073508
-->

## 1.3. 系统其他问题  
### 1.3.1. qps飘高  
&emsp; 查看[秒杀系统设计](/docs/system/seckill.md)  


