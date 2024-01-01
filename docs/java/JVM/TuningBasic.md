
<!-- TOC -->

- [1. JVM调优基础](#1-jvm调优基础)
    - [★★★每天100w次登陆请求, 8G 内存该如何设置JVM参数？](#★★★每天100w次登陆请求-8g-内存该如何设置jvm参数)
    - [1.1. 性能指标](#11-性能指标)
    - [1.2. JVM参数](#12-jvm参数)
        - [1.2.1. JVM参数分类](#121-jvm参数分类)
        - [1.2.2. 需要掌握的JVM参数](#122-需要掌握的jvm参数)
            - [1.2.2.1. 设置内存参数](#1221-设置内存参数)
            - [1.2.2.2. GC打印参数](#1222-gc打印参数)
            - [1.2.2.3. 垃圾回收器设置参数](#1223-垃圾回收器设置参数)
    - [1.3. GC日志分析](#13-gc日志分析)
        - [1.3.1. GC日志详解](#131-gc日志详解)
        - [1.3.2. GC日志分析工具](#132-gc日志分析工具)
    - [1.4. JVM调优工具](#14-jvm调优工具)
        - [1.4.1. Jdk命令行调优工具](#141-jdk命令行调优工具)
            - [1.4.1.1. Jps：虚拟机进程状况工具](#1411-jps虚拟机进程状况工具)
            - [1.4.1.2. Jstack：java线程堆栈跟踪工具](#1412-jstackjava线程堆栈跟踪工具)
            - [1.4.1.3. Jmap：java内存映像工具](#1413-jmapjava内存映像工具)
                - [1.4.1.3.1. ★★★jmap的几个操作要慎用](#14131-★★★jmap的几个操作要慎用)
                - [1.4.1.3.2. ★★★live参数](#14132-★★★live参数)
            - [1.4.1.4. Jhat：虚拟机堆转储快照分析工具](#1414-jhat虚拟机堆转储快照分析工具)
            - [1.4.1.5. ★★★Jstat：虚拟机统计信息监视工具](#1415-★★★jstat虚拟机统计信息监视工具)
            - [1.4.1.6. Jinfo：java配置信息工具](#1416-jinfojava配置信息工具)
        - [1.4.2. 可视化调优工具](#142-可视化调优工具)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. JVM参数：

|参数|描述|
|---|---|
|-Xms|用于在JVM启动时设置初始堆大小|
|-Xmx|用于设置最大堆大小|
|-Xmn|设置新生区的大小，剩下的空间用于老年区|
|-XX：PermGen|用于设置永久区存初始大小|
|-XX：MaxPermGen|用于设置Perm Gen的最大尺寸|
|-XX：SurvivorRatio|提供Eden区域的比例|
|-XX：NewRatio|用于提供老年代/新生代大小的比例，默认值为2|
    
2. JVM命令行调优工具：  
    * Jps：虚拟机进程状况工具。  
    * Jstack：java线程堆栈跟踪工具。  
    &emsp; **<font color = "clime">生成线程快照的主要目的是定位线程出现长时间停顿的原因，如线程间死锁、死循环、请求外部资源导致的长时间等待等都是导致线程长时间停顿的常见原因。</font>**  
    &emsp; **`线程出现停顿的时候，通过jstack来查看各个线程的调用堆栈，就可以知道没有响应的线程到底在后台做什么事情，或者等待什么。`**  
    * Jmap：java内存映像工具。  
    &emsp; <font color = "red">jmap（JVM Memory Map）命令用于生成heap dump文件。如果不使用这个命令，</font> **<font color = "red">还可以使用-XX:+HeapDumpOnOutOfMemoryError参数来让虚拟机出现OOM的时候自动生成dump文件。</font>**   
    &emsp; jmap -dump:live,format=b,file=path pid。 **<font color = "blue">参数lime表示需要抓取目前在生命周期内的内存对象。</font>**   
    * Jhat：虚拟机堆转储快照分析工具。  
    * Jstat：虚拟机统计信息监视工具。  
    * Jinfo：java配置信息工具。  

# 1. JVM调优基础  

<!-- 

★★★jvm指令与工具jstat/jstack/jmap/jconsole/jps/visualVM
https://www.bilibili.com/video/BV1QJ411P78Q?spm_id_from=333.999.0.0

6 款 Java 8 自带工具，轻松分析定位 JVM 问题！ 
https://mp.weixin.qq.com/s/YuML5GfzRhq5YBfIPXgbYg

jstack 命令解读
https://blog.csdn.net/qq_19922839/article/details/115379649

 6 款 Java 8 自带工具，轻松分析定位 JVM 问题！
 https://mp.weixin.qq.com/s/xo6Cp37u3xl7mlzBNYKa5A

-->

## ★★★每天100w次登陆请求, 8G 内存该如何设置JVM参数？  
https://mp.weixin.qq.com/s/vOwsUnpoZcznkwP_jvhpww  



## 1.1. 性能指标  
&emsp; 参考[性能指标](/docs/system/performance.md)  

## 1.2. JVM参数  
&emsp; **<font color = "red">设置参数的方式：</font>**  

* 开发工具中设置，比如IDEA，eclipse。  
* 运行jar包的时候：java -XX:+UseG1GC xxx.jar。  
* web容器比如tomcat，可以在脚本中进行设置。  
* 通过jinfo实时调整某个java进程的参数（参数只有被标记为manageable的flags可以被实时修改）。  

&emsp; **<font color = "red">平时⼯作⽤过的JVM常⽤基本配置参数：</font>**  

    -Xms128m-Xmx4096m-Xss1024K-XX:MetaspaceSize=512m-XX:+PrintCommandLineFlags-XX:+PrintGCDetails-XX:+UseSerialGC  

1. -Xms。初始⼤⼩内存，默认为物理内存1/64，等价于-XX:InitialHeapSize。
2. -Xmx。最⼤分配内存，默认物理内存1/4，等价于-XX:MaxHeapSize。
3. -Xss。设置单个线程栈的⼤⼩，默认542K~1024K ，等价于-XX:ThreadStackSize。
4. -Xmn。设置年轻代的⼤⼩。  
5. -XX:MetaspaceSize。设置元空间⼤⼩元空间的本质和永久代类似，都是对JVM规范中⽅法区的实现，不过元空间与永久代最⼤的区别在于：==元空间并不在虚拟机中，⽽是在本地内存中。==因此，默认元空间的⼤⼩仅受本地内存限制。  
6. -XX:+PrintGCDetails。输出详细GC收集⽇志信息\[名称：GC前内存占⽤->GC后内存占⽤(该区内存总⼤⼩)]。  
7. -XX:SurvivorRatio。设置新⽣代中Eden和S0/S1空间的⽐例，默认-XX:SurvivorRatio=8，Eden :S0 :S1=8 :1 :1。  
8. -XX:NewRatio。设置年轻代与⽼年代在堆结构的占⽐。默认-XX:NewRatio=2 新⽣代在1，⽼年代2，年轻代占整个堆的1/3。NewRatio值⼏句诗设置⽼年代的占⽐，剩下的1给新⽣代。  
9. -XX:MaxTenuringThreshold。设置垃圾的最⼤年龄默认-XX:MaxTenuringThreshold=15。如果设置为0，年轻代对象不经过Survivor区，直接进⼊年⽼代。对于年⽼代⽐较多的应⽤，可以提⾼效率。如果将此值设置为⼀个较⼤的值，则年轻代对象回在Survivor区进⾏多次复制，这样可以增加对对象在年轻代的存活时间，增加在年轻代即被回收的概率。  
10. -XX:+UseSerialGC。串⾏垃圾回收器。
11. -XX:+UseParallelGC。并⾏垃圾回收器。  

### 1.2.1. JVM参数分类  
&emsp; JVM参数主要可以分为以下三类：  

* 标准参数(-)，所有的JVM实现都必须实现这些参数的功能，而且向后兼容。例如 -verbose:gc(输出每次GC的相关情况)。  
* 非标准参数(-X)，默认JVM实现这些参数的功能，但是并不保证所有JVM实现都满足，且不保证向后兼容。栈，堆大小的设置都是通过这个参数来配置的，用得最多的如下。   

|参数	|描述|
|---|---|
|-Xms512m|JVM 启动时设置的初始堆大小为 512M|
|-Xmx512m|	JVM 可分配的最大堆大小为 512M|
|-Xmn200m|	设置的年轻代大小为 200M|
|-Xss128k|	设置每个线程的栈大小为 128k|

* 非标准参数(-XX)，此类参数各个JVM实现会有所不同，将来可能会随时取消，需要慎重使用。
    * 分Boolean类型和非Boolean类型：  
        * Boolean类型  

                格式：-XX:[+-]<name>，+或-表示启用或者禁用name属性  
                比如：-XX:+UseConcMarkSweepGC，表示启用CMS类型的垃圾回收器  
                     -XX:+UseG1GC，表示启用G1类型的垃圾回收器  
        * 非Boolean类型  
        
                格式：-XX<name>=<value>，表示name属性的值是value  
                比如：-XX:MaxGCPauseMillis=500  
       
    * 分行为参数、性能调优、调试参数：  
        * 行为参数(Behavioral Options)：用于改变JVM的一些基础行为，如启用串行/并行 GC。    
            
        |参数	|描述|
        |---|---|
        |-XX:-DisableExplicitGC	|禁止调用System.gc()；但jvm的gc仍然有效|
        |-XX:-UseConcMarkSweepGC|对老生代采用并发标记交换算法进行GC|
        |-XX:-UseParallelGC	|启用并行GC|
        |-XX:-UseParallelOldGC	|对Full GC启用并行，当-XX:-UseParallelGC启用时该项自动启用|
        |-XX:-UseSerialGC	|启用串行GC|
        
        * 性能调优(Performance Tuning)：用于jvm的性能调优，如设置新老生代内存容量比例。    
            
        |参数|描述|
        |---|---|
        |-XX:MaxHeapFreeRatio=70	|GC后java堆中空闲量占的最大比例|
        |-XX:NewRatio=2	|新生代内存容量与老生代内存容量的比例|
        |-XX:NewSize=2.125m	|新生代对象生成时占用内存的默认值|
        |-XX:ReservedCodeCacheSize=32m	|保留代码占用的内存容量|
        |-XX:ThreadStackSize=512	|设置线程栈大小，若为0则使用系统默认值|
        
        * 调试参数(Debugging Options)：一般用于打开跟踪、打印、输出等JVM参数，用于显示JVM更加详细的信息。    

        |参数	|描述|
        |---|---|
        |-XX:HeapDumpPath=./java_pid.hprof	|指定导出堆信息时的路径或文件名|
        |-XX:-HeapDumpOnOutOfMemoryError	|当首次遭遇OOM时导出此时堆中相关信息|
        |-XX:-PrintGC	|每次GC时打印相关信息|
        |-XX:-PrintGC Details	|每次GC时打印详细信息|


### 1.2.2. 需要掌握的JVM参数  
#### 1.2.2.1. 设置内存参数  

|参数|描述|
|---|---|
|-Xms|用于在JVM启动时设置初始堆大小|
|-Xmx|用于设置最大堆大小|
|-Xmn|设置新生区的大小，剩下的空间用于老年区|
|-XX：PermGen|用于设置永久区存初始大小|
|-XX：MaxPermGen|用于设置Perm Gen的最大尺寸|
|-XX：SurvivorRatio|提供Eden区域的比例|
|-XX：NewRatio|用于提供老年代/新生代大小的比例，默认值为2|

&emsp; 将-Xms和-Xmx设置为相同值，会提高JVM性能。  

&emsp; 元空间是将存储JVM的元数据定义(例如类定义，方法定义)的区域。默认情况下，可用于存储此元数据信息的内存量是无限的(即受容器或计算机的RAM大小的限制)。需要使用-XX：MaxMetaspaceSize参数来指定可用于存储元数据信息的内存量的上限。  
&emsp; -XX:MaxMetaspaceSize=256m  

&emsp; -Xss，设置栈的大小。栈都是每个线程独有一个，所有一般都是几百k的大小。  

#### 1.2.2.2. GC打印参数  

|参数|描述|
|---|---|
|-verbose:gc|开启输出JVM GC日志|
|-verbose:class |查看类加载信息明细|
|-Xloggc:./gc.log|指定GC日志目录和文件名|
|-XX:+PrintGCDetails|GC日志打印详细信息|
|-XX:+PrintGCDateStamps|GC日志打印时间戳信息|
|-XX:+PrintHeapAtGC	|在GC前后打印GC日志|
|-XX:+PrintGCApplicationStoppedTime	|打印应用暂停时间|
|-XX:+PrintGCApplicationConcurrentTime	|打印每次垃圾回收前，程序未中断的执行时间|
| | |
|-XX:+HeapDumpOnOutOfMemoryError|当发生OOM(OutOfMemory)时，自动转储堆内存快照，缺省情况未指定目录时，JVM会创建一个名称为java_pidPID.hprof的堆dump文件在JVM的工作目录下|
|-XX:HeapDumpPath=/data/log/gc/dump/|指定OOM时堆内存转储快照位置|
|-XX:+PrintClassHistogramBeforeFullGC、-XX:+PrintClassHistogramAfterFullGC|Full GC前后打印跟踪类视图|
|-XX:+PrintTenuringDistribution	|打印Young GC各个年龄段的对象分布|
|-XX:+PrintTLAB|打印TLAB(线程本地分配缓存区)空间使用情况|

#### 1.2.2.3. 垃圾回收器设置参数  

|参数	|描述|
|---|---|
|-XX:+UseSerialGC|在新生代和老年代使用串行收集器|
|-XX:+UseParNewGC|在新生代使用并行收集器|
|-XX:+UseParallelGC|新生代使用并行回收收集器，更加关注吞吐量|
|-XX:+UseParallelOldGC|老年代使用并行回收收集器|
|-XX:ParallelGCThreads|设置用于垃圾回收的线程数|
|-XX:+UseConcMarkSweepGC|新生代使用并行收集器，老年代使用CMS+串行收集器|
|-XX:ParallelCMSThreads|设定CMS的线程数量|
|-XX:+UseG1GC|启用G1垃圾回收器|

&emsp; 未明确指定GC算法，那么JVM将选择默认算法。在Java 8之前，Parallel GC是默认的GC算法。从Java 9开始，G1 GC是默认的GC算法。  

----
## 1.3. GC日志分析  
<!-- 

 收藏，手把手教你Java GC算法日志解读与分析
 https://mp.weixin.qq.com/s/ODKOXFUJmekjM3ykkML68w
-->


### 1.3.1. GC日志详解  
&emsp; 在某个应用中，配置`-XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:D:/gc.log`。启动后打印如下 GC 日志：  
&emsp; YongGC  

```
2019-04-18T14:52:06.790+0800: 2.653: [GC (Allocation Failure) [PSYoungGen: 33280K->5113K(38400K)] 33280K->5848K(125952K), 0.0095764 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
```
&emsp; 含义：  

```
2019-04-18T14:52:06.790+0800(当前时间戳): 2.653(应用启动基准时间): [GC (Allocation Failure) [PSYoungGen(表示 Young GC): 33280K(年轻代回收前大小)->5113K(年轻代回收后大小)(38400K(年轻代总大小))] 33280K(整个堆回收前大小)->5848K(整个堆回收后大小)(125952K(堆总大小)), 0.0095764(耗时) secs] [Times: user=0.00(用户耗时) sys=0.00(系统耗时), real=0.01(实际耗时) secs]
```
&emsp; Full GC  

```
2019-04-18T14:52:15.359+0800: 11.222: [Full GC (Metadata GC Threshold) [PSYoungGen: 6129K->0K(143360K)] [ParOldGen: 13088K->13236K(55808K)] 19218K->13236K(199168K), [Metaspace: 20856K->20856K(1069056K)], 0.1216713 secs] [Times: user=0.44 sys=0.02, real=0.12 secs]
```
&emsp; 含义：  

```
2019-04-18T14:52:15.359+0800(当前时间戳): 11.222(应用启动基准时间): [Full GC (Metadata GC Threshold) [PSYoungGen: 6129K(年轻代回收前大小)->0K(年轻代回收后大小)(143360K(年轻代总大小))] [ParOldGen: 13088K(老年代回收前大小)->13236K(老年代回收后大小)(55808K(老年代总大小))] 19218K(整个堆回收前大小)->13236K(整个堆回收后大小)(199168K(堆总大小)), [Metaspace: 20856K(持久代回收前大小)->20856K(持久代回收后大小)(1069056K(持久代总大小))], 0.1216713(耗时) secs] [Times: user=0.44(用户耗时) sys=0.02(系统耗时), real=0.12(实际耗时) secs]
```
&emsp; **GC日志格式详解：**  
&emsp; 每种收集器的日志形式都是由它们自身的实现所决定的，换言之，每种收集器的日志格式都可以不一样。不过虚拟机为了方便用户阅读，将各个收集器的日志都维持了一定的共性。例如上面一段GC日志：  
1. 日志的开头“GC”、“Full GC”表示这次垃圾收集的停顿类型，而不是用来区分新生代GC还是老年代GC的。如果有Full，则说明本次GC停止了其他所有工作线程(Stop-The-World)。Full GC，一般是因为出现了分配担保失败之类的问题，所以才导致STW。如果是调用System.gc()方法所触发的收集，将显示“Full GC(System)”。  
2. “GC”中接下来的“[DefNew”、“[Tenured”、“[Prem”表示GC发生的区域，这里显示的区域名称与使用的GC收集器是密切相关的，例如上面样例所使用的Serial收集器中的新生代名为“Default New Generation”，所以显示的是“[DefNew”。如果是ParNew收集器，新生代名称就会变为“[ParNew”，意为“Parallel New Generation”。如果采用Parallel Scavenge收集器，那它配套的新生代称为“PSYoungGen”，老年代和永久代同理，名称也是由收集器决定的。  
3. 后面方括号内部的“310K->194K(2368K)”、“2242K->0K(2368K)”，指的是该区域已使用的容量->GC后该内存区域已使用的容量(该内存区总容量)。方括号外面的“310K->194K(7680K)”、“2242K->2241K(7680K)”则指的是GC前Java堆已使用的容量->GC后Java堆已使用的容量(Java堆总容量)。  
4. 再往后“0.0269163 secs”表示该内存区域GC所占用的时间，单位是秒。有的收集器会给出更具体的时间数据，如“[Times: user=0.00 sys=0.00 real=0.03 secs]”，这里面的user、sys和real与Linux的time命令所输出的时间含义一致，分别表示用户态消耗的CPU时间、内核态消耗的CPU时间、操作从开始到结束经过的墙钟时间。CPU时间与墙钟时间的区别是，墙钟时间包括各种非运算的等待消耗，比如等待磁盘I/O、等待线程阻塞，而CPU时间不包括这些耗时，但当系统有多CPU或者多核的话，多线程操作会叠加这些CPU时间，所以如果看到user或sys时间超过real时间是完全正常的。  
5. “Heap”后面就列举出堆内存目前各个年代的区域的内存情况。  

### 1.3.2. GC日志分析工具  
&emsp; GC日志分析工具有GCHisto、GCLogViewer、HPjmeter、garbagecat，还有Jvisualvm插件visualGC。  

-----
## 1.4. JVM调优工具  
### 1.4.1. Jdk命令行调优工具  
<!-- 
JVM 性能调优监控工具 jps、jstack、jmap、jhat、jstat、hprof 使用详解
https://mp.weixin.qq.com/s/XBB2IJf8ODkcjZiU423J4Q
JVM 常用命令行工具 
https://mp.weixin.qq.com/s/MC2y6JAbZyjIVp7yTxT7fQ
-->
&emsp; **<font color = "clime">~~注意如果Dump文件太大，可能需要加上-J-Xmx512m这种参数指定最大堆内存，即jhat -J-Xmx512m -port 9998 /tmp/dump.dat。~~</font>**  

|名称|主要作用|
|---|---|
|jps|JVM Process Status Tool，显示指定系统内所有的HotSpot虚拟机进程。|
|jstack|Stack Trace for Java，显示虚拟机的线程快照。
|jmap|JVM Memory Map，生成虚拟机的内存转储快照(heap dump文件)。|
|jhat|JVM Heap Analysis Tool命令是与jmap搭配使用，用来分析jmap生成的dump，jhat内置了一个微型的HTTP/HTML服务器，生成dump的分析结果后，可以在浏览器中查看。|
|jstat|JVM statistics Monitoring Tool，用于收集虚拟机各方面的运行数据，它可以显示出虚拟机进程中的类装载、内存、垃圾收集、JIT编译等运行数据。|
|jinfo|JVM Configuration info，显示虚拟机配置信息。|  

&emsp; **<font color = "red">快照</font>：** 系统运行到某一时刻的一个定格。在进行调优的时候，依赖快照功能，可以进行系统两个不同运行时刻，对比对象(或类、线程等)的不同，以便快速找到问题。  

#### 1.4.1.1. Jps：虚拟机进程状况工具  
&emsp; JVM Process Status Tool，显示指定系统内所有的**HotSpot虚拟机进程** 。  
&emsp; 命令格式：jps [options] [hostid]，如果不指定hostid就默认为当前主机或服务器。 命令行参数选项说明如下：  

* -q 不输出类名、Jar名和传入main方法的参数  
* -l 输出main类或Jar的全限名  
* -m 输出传入main方法的参数  
* -v 输出传入JVM的参数  

#### 1.4.1.2. Jstack：java线程堆栈跟踪工具  
&emsp; jstack用于生成java虚拟机当前时刻的线程快照。线程快照是当前java虚拟机内每一条线程正在执行的方法堆栈的集合。 **<font color = "clime">生成线程快照的主要目的是定位线程出现长时间停顿的原因，如线程间死锁、死循环、请求外部资源导致的长时间等待等都是导致线程长时间停顿的常见原因。</font>**  
&emsp; **线程出现停顿的时候，通过jstack来查看各个线程的调用堆栈，就可以知道没有响应的线程到底在后台做什么事情，或者等待什么** 如果java程序崩溃生成core文件，jstack工具可以用来获得core文件的java stack和native stack的信息，从而可以轻松地知道java程序是如何崩溃和在程序何处发生问题。  
&emsp; 另外，jstack工具还可以附属到正在运行的java程序中，看到当时运行的java程序的java stack和native stack的信息，如果现在运行的java程序呈现hung的状态，jstack是非常有用的。  
&emsp; 命令格式：jstack [option] PID。option参数：  

* -F : 当正常输出请求不被响应时，强制输出线程堆栈
* -l : 除堆栈外，显示关于锁的附加信息
* -m : 如果调用到本地方法的话，可以显示C/C++的堆栈

![image](http://182.92.69.8:8081/img/java/JVM/JVM-35.png)  

#### 1.4.1.3. Jmap：java内存映像工具  
&emsp; <font color = "red">jmap(JVM Memory Map)命令用于生成heap dump文件，如果不使用这个命令，</font>**<font color = "red">还可以使用-XX:+HeapDumpOnOutOfMemoryError参数来让虚拟机出现OOM的时候自动生成dump文件。</font>**    
&emsp; jmap不仅能生成dump文件，还可以查询finalize执行队列、Java堆和永久代的详细信息，如当前使用率、当前使用的是哪种收集器等。  
&emsp; 命令格式：jmap [option]  PID。option参数： 

* dump：生成堆转储快照
* finalizerinfo：显示在F-Queue队列等待Finalizer线程执行finalizer方法的对象
* heap：显示Java堆详细信息
* histo：显示堆中对象的统计信息
* permstat：to print permanent generation statistics
* F：当-dump没有响应时，强制生成dump快照  


&emsp; jmap -dump:format=b,file=heap.hprof PID。  
&emsp; format指定输出格式，live指明是活着的对象，file指定文件名。  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-37.png)  

##### 1.4.1.3.1. ★★★jmap的几个操作要慎用  
&emsp; 生产环境中最主要的危险操作是下面这三种：
1. jmap -dump。这个命令执行，JVM会将整个heap的信息dump写入到一个文件，heap如果比较大的话，就会导致这个过程比较耗时，并且执行的过程中为了保证dump的信息是可靠的，所以会暂停应用。  
2. jmap -permstat。这个命令执行，JVM会去统计perm区的状况，这整个过程也会比较的耗时，并且同样也会暂停应用。  
3. jmap -histo:live。这个命令执行，JVM会先触发gc，然后再统计信息。  

&emsp; 上面的这三个操作都将对应用的执行产生影响，所以建议如果不是很有必要的话，不要去执行。

##### 1.4.1.3.2. ★★★live参数  
<!-- 

https://blog.csdn.net/shenzhenhair/article/details/8607366
-->

#### 1.4.1.4. Jhat：虚拟机堆转储快照分析工具  
&emsp; **<font color = "clime">jhat(JVM Heap Analysis Tool)命令是与jmap搭配使用，用来分析jmap生成的dump，</font>** jhat内置了一个微型的HTTP/HTML服务器，生成dump的分析结果后，可以在浏览器中查看。在此要注意，一般不会直接在服务器上进行分析，因为jhat是一个耗时并且耗费硬件资源的过程，一般把服务器生成的dump文件复制到本地或其他机器上进行分析。    

#### 1.4.1.5. ★★★Jstat：虚拟机统计信息监视工具  
<!-- 

-->
&emsp; jstat的主要作用就是对Java应用程序的资源和性能进行实时监控的命令行工具，主要包括GC情况和Heap Size资源使用情况。  
&emsp; jstat(JVM statistics Monitoring)是用于监视虚拟机运行时状态信息的命令，它可以显示出虚拟机进程中的类装载、内存、垃圾收集、JIT编译等运行数据。  
&emsp; 命令格式：jstat [option] PID [interval] [count]。参数：  

* [option] : 操作参数  
* LVMID : 本地虚拟机进程ID  
* [interval] : 连续输出的时间间隔  
* [count] : 连续输出的次数  

1. 查看类装载信息  
&emsp; jstat -class PID 1000 10查看某个java进程的类装载信息，每1000毫秒输出一次，共输出10次  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-38.png)  
2. 查看垃圾收集信息  
&emsp; jstat -gc PID 1000 10  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-39.png)  

#### 1.4.1.6. Jinfo：java配置信息工具
&emsp; jinfo(JVM Configuration info)，实时查看和调整虚拟机运行参数。之前的jps -v口令只能查看到显示指定的参数，如果想要查看未被显示指定的参数的值就要使用jinfo口令。  
&emsp; 命令格式：jinfo [option] [args] PID。option参数： 

* -flag : 输出指定args参数的值  
* -flags : 不需要args参数，输出所有JVM参数的值  
* -sysprops : 输出系统属性，等同于System.getProperties()  

1. 查看  
jinfo -flag name PID查看某个java进程的name属性的值  
jinfo -flag MaxHeapSize PID  
jinfo -flag UseG1GC PID  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-40.png)  

2. 修改  
参数只有被标记为manageable的flags可以被实时修改  
jinfo -flag [+|-] PID  
jinfo -flag = PID  

3. 查看曾经赋过值的一些参数  
jinfo -flags PID  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-41.png)  

----
### 1.4.2. 可视化调优工具  
1. JConsole(JDK自带)  
2. JVisualVM(JDK自带)  
3. 第三方分析内存日志工具  
4. MAT分析堆内存快照工具  
5. Arthas  



<!-- 
HSDB（Hotspot Debugger），JDK自带的工具，用于查看JVM运行时的状态。  

HSDB入门介绍
https://blog.csdn.net/weixin_37152234/article/details/99775593
-->

