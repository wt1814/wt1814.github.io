
<!-- TOC -->

- [1. JVM问题排查](#1-jvm问题排查)
    - [1.1. 快速恢复业务](#11-快速恢复业务)
    - [1.2. 系统缓慢可能的原因](#12-系统缓慢可能的原因)
    - [1.3. FGC过高](#13-fgc过高)
    - [1.4. CPU飚高](#14-cpu飚高)
        - [1.4.1. 现象](#141-现象)
        - [1.4.2. 排查步骤](#142-排查步骤)
    - [1.5. ★★★CPU高，查看所有进程占用率要远小于100](#15-★★★cpu高查看所有进程占用率要远小于100)
    - [1.6. 内存溢出排查实战](#16-内存溢出排查实战)
        - [1.6.1. 堆溢出演示](#161-堆溢出演示)
        - [1.6.2. 内存溢出的解决方案](#162-内存溢出的解决方案)
            - [1.6.2.1. 可以使用的解决方案](#1621-可以使用的解决方案)
            - [1.6.2.2. 使用内存查看工具分析堆dump文件](#1622-使用内存查看工具分析堆dump文件)
            - [1.6.2.3. ★★★jvm内存快照dump文件太大，怎么分析](#1623-★★★jvm内存快照dump文件太大怎么分析)
                - [1.6.2.3.1. ★★★jmap中live参数](#16231-★★★jmap中live参数)
                - [1.6.2.3.2. jhat指定最大堆内存](#16232-jhat指定最大堆内存)
                - [1.6.2.3.3. linux下的mat](#16233-linux下的mat)
    - [1.7. 线程死锁](#17-线程死锁)
    - [1.8. 其他情况](#18-其他情况)
    - [1.9. JAVA线上故障排查](#19-java线上故障排查)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 快速恢复业务：隔离故障服务器。  
2. FGC过高  
&emsp; **<font color = "clime">FGC过高可能是内存参数设置不合理，也有可能是代码中某个位置读取数据量较大导致系统内存耗尽。FGC过高可能导致CPU飚高。</font>**  
&emsp; **<font color = "clime">解决思路（`FGC过高参考CPU飚高`）：FGC过高一般会导致CPU过高，打印线程堆栈信息。查看线程堆栈是用户线程，还是GC线程。如果是GC线程，打印内存快照进行分析（`查看内存溢出`）。</font>**  
3. CPU飚高  
&emsp; **<font color = "red">CPU过高可能是系统频繁的进行Full GC，导致系统缓慢。</font><font color = "clime">而平常也可能遇到比较耗时的计算，导致CPU过高的情况。</font>**  
&emsp; **<font color = "clime">怎么区分导致CPU过高的原因具体是Full GC次数过多还是代码中有比较耗时的计算？</font>** `如果是Full GC次数过多，那么通过jstack得到的线程信息会是类似于VM Thread之类的线程`；而`如果是代码中有比较耗时的计算，那么得到的就是一个线程的具体堆栈信息。` 

        1. 通过top命令找到CPU消耗最高的进程，并记住进程ID。  
        2. 再次通过top -Hp [进程 ID]找到CPU消耗最高的线程ID，并记住线程ID。  
        3. 通过JDK提供的jstack工具dump线程堆栈信息到指定文件中。具体命令：jstack -l [进程 ID] >jstack.log。  
        4. 由于刚刚的线程ID是十进制的，而堆栈信息中的线程ID是16进制的，因此需要将10进制的转换成16进制的，并用这个线程ID在堆栈中查找。使用printf "%x\n" [十进制数字] ，可以将10进制转换成16进制。  
        5. 通过刚刚转换的16进制数字从堆栈信息里找到对应的线程堆栈。就可以从该堆栈中看出端倪。      
4. **<font color = "blue">★★★CPU高，查看所有进程占用率要远小于100。</font>**
    1. 可能多个线程执行同一方法，每个线程占有不高，但总和比较大。  
    2. 可以使用arthas工具的thread -n -i分析。
5. 内存溢出OOM  
	1. 解决方案：
		1. 修改JVM启动参数，直接增加内存。  
		2. 检查错误日志，查看“OutOfMemory”错误前是否有其它异常或错误。  
		3. 对代码进行走查和分析，找出可能发生内存溢出的位置。 
		4. 使用内存查看工具动态查看内存快照。 
	2. 使用内存查看工具分析堆dump文件
    3. jvm内存快照dump文件太大： 
	    * **<font color = "clime">live参数表示需要抓取目前在生命周期内的内存对象，也就是说GC收不走的对象，然后绝大部分情况下，需要的看的就是这些内存。</font>**   
		* 如果Dump文件太大，可能需要加上-J-Xmx512m这种参数指定最大堆内存，即jhat -J-Xmx512m -port 9998 /tmp/dump.dat。
		* 如果dump文件太大，使用linux下的mat，既Memory Analyzer Tools。   



# 1. JVM问题排查  
<!-- 
★★★JVM故障分析及性能优化系列之五
https://blog.csdn.net/z69183787/article/details/103955420
-->


    获取线程stack快照，排查CPU飚高；  
    获取堆heap快照，排查内存溢出的问题；  

## 1.1. 快速恢复业务  
&emsp; 通常线上的故障会对业务造成重大影响，影响用户体验，故如果线上服务器出现故障，应规避对业务造成影响，但不能简单的重启服务器，因为需要尽可能保留现场，为后续的问题分析打下基础。  
&emsp; 那如何快速规避对业务的影响，并能保留现场呢？  
&emsp; 通常的做法是隔离故障服务器。  
&emsp; 通常线上服务器是集群部署，一个好的分布式负载方案会自动剔除故障的机器，从而实现高可用架构，但如果未被剔除，则需要运维人员将故障服务器进行剔除，保留现场进行分析。  


## 1.2. 系统缓慢可能的原因  
&emsp; 对于线上系统突然产生的运行缓慢问题，可能的原因主要有两种：  

* **<font color = "red">代码中某个位置读取数据量较大，导致系统内存耗尽，从而导致Full GC次数过多，系统缓慢；</font>**  
* 代码中有比较耗CPU的操作，导致CPU过高，系统运行缓慢；  

&emsp; 相对来说，这是出现频率最高的两种线上问题，而且它们会直接导致系统不可用。另外有几种情况也会导致某个功能运行缓慢，但是不至于导致系统不可用：  

* 代码某个位置有阻塞性的操作，导致该功能调用整体比较耗时，但出现是比较随机的；  
* 某个线程由于某种原因而进入WAITING状态，此时该功能整体不可用，但是无法复现；  
* 由于锁使用不当，导致多个线程进入死锁状态，从而导致系统整体比较缓慢。  

&emsp; 对于这三种情况，通过查看CPU和系统内存情况是无法查看出具体问题的，因为它们相对来说都是具有一定阻塞性操作，CPU和系统内存使用情况都不高，但是功能却很慢。  



## 1.3. FGC过高  
&emsp; ~~使用jstack来分析GC是不是太频繁， **<font color = "clime">使用jstat -gc pid 1000命令来对gc分代变化情况进行观察，</font>** 1000表示采样间隔(ms)，S0C/S1C、S0U/S1U、EC/EU、OC/OU、MC/MU分别代表两个Survivor区、Eden区、老年代、元数据区的容量和使用量。YGC/YGT、FGC/FGCT、GCT则代表YoungGc、FullGc的耗时和次数以及总耗时。如果看到gc比较频繁，再针对gc方面做进一步分析。~~   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-81.png)  

&emsp; **<font color = "clime">FGC过高 1)可能是内存参数设置不合理，2)也有可能是代码中某个位置读取数据量较大导致系统内存耗尽。FGC过高可能导致CPU飚高。</font>** ⚠注意：FGC过高，会导致CPU飚高；但CPU飚高不一定是FGC过高。  
&emsp; **<font color = "clime">解决思路：打印线程堆栈信息。查看线程堆栈是用户线程，还是GC线程。如果是GC线程，打印内存快照进行分析。</font>**  

1. 如果FGC后还有大量对象，说明Old区过小，应该扩大Old区；  
2. 如果FGC后效果很好，说明Old区存在了大量短命的对象，优化的点应该是让这些对象在新生代就被YGC掉，通常的做法是增大新生代；  
3. 如果有大而短命的对象，通过参数设置对象的大小，要让这些对象进入Old区，还需要检查晋升年龄是否过小；  
4. 如果YGC后，有大量对象因为无法进入Survivor区从而提前晋升，这时应该增大Survivor区，但不宜太大。  
5. 也可以直接增大内存。  

<!-- 
&emsp; <font color = "red">内存参数设置不合理，有以下策略：</font>  

* 策略1：将新对象预留在新生代，由于Full GC的成本远高于Minor GC，因此尽可能将对象分配在新生代是明智的做法，实际项目中根据GC日志分析新生代空间大小分配是否合理，适当通过“-Xmn”命令调节新生代大小，最大限度降低新对象直接进入老年代的情况。  
* 策略2：大对象进入老年代，虽然大部分情况下，将对象分配在新生代是合理的。但是对于大对象这种做法却值得商榷，大对象如果首次在新生代分配可能会出现空间不足导致很多年龄不够的小对象被分配的老年代，破坏新生代的对象结构，可能会出现频繁的full gc。因此，对于大对象，可以设置直接进入老年代(当然短命的大对象对于垃圾回收老说简直就是噩梦)。-XX:PretenureSizeThreshold 可以设置直接进入老年代的对象大小。  
* 策略3：合理设置进入老年代对象的年龄，-XX:MaxTenuringThreshold设置对象进入老年代的年龄大小，减少老年代的内存占用，降低full gc发生的频率。  
* 策略4：设置稳定的堆大小，堆大小设置有两个参数：-Xms初始化堆大小，-Xmx最大堆大小。  

&emsp; <font color = "red">FGC过高，内存参数设置不合理，一般是Old区内存不够导致 FGC。</font>  
-->



## 1.4. CPU飚高  
<!-- 
https://www.cnblogs.com/klvchen/p/11089632.html
-->

### 1.4.1. 现象   
&emsp; **<font color = "red">CPU过高可能是系统频繁的进行Full GC，导致系统缓慢。</font><font color = "clime">而平常也可能遇到比较耗时的计算，导致CPU过高的情况。</font>**

&emsp; **<font color = "clime">怎么区分导致CPU过高的原因具体是Full GC次数过多还是代码中有比较耗时的计算？</font>**  
&emsp; 如果是Full GC次数过多，那么通过jstack得到的线程信息会是类似于VM Thread之类的线程，而如果是代码中有比较耗时的计算，那么得到的就是一个线程的具体堆栈信息。如下是一个代码中有比较耗时的计算，导致CPU过高的线程信息：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-47.png)  
&emsp; 这里可以看到，在请求UserController的时候，由于该Controller进行了一个比较耗时的调用，导致该线程的CPU一直处于100%。可以根据堆栈信息，直接定位到UserController的34行，查看代码中具体是什么原因导致计算量如此之高。  

### 1.4.2. 排查步骤  

    1. 通过top命令找到CPU消耗最高的进程，并记住进程ID。  
    2. 再次通过top -Hp [进程 ID]找到CPU消耗最高的线程ID，并记住线程ID。  
    3. 通过JDK提供的jstack工具dump线程堆栈信息到指定文件中。具体命令：jstack -l [进程 ID] >jstack.log。  
    4. 由于刚刚的线程ID是十进制的，而堆栈信息中的线程ID是16进制的，因此需要将10进制的转换成16进制的，并用这个线程ID在堆栈中查找。使用 printf "%x\n" [十进制数字] ，可以将10进制转换成16进制。  
    5. 通过刚刚转换的16进制数字从堆栈信息里找到对应的线程堆栈。就可以从该堆栈中看出端倪。  

1. 查找消耗cpu最高的进程PID  
&emsp; 执行top -c，显示进程运行信息列表。按下P，进程按照cpu使用率排序。  
&emsp; 如下图所示，PID为3033的进程耗费cpu最高。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-42.png)  
2. 查找该进程下消耗cpu最高的线程号  
&emsp; 执行命令top -Hp 3033 ，显示一个进程的线程运行信息列表。按下P，进程按照cpu使用率排序。  
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

&emsp; 输出如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-44.png)  


&emsp; **<font color = "cclime">注意，这里又分为两种情况：</font>**    

* 如果是正常的用户线程，则通过该线程的堆栈信息查看其具体是在哪处用户代码处运行比较消耗CPU。  
* 如果该线程是VM Thread(VM Thread 指的就是垃圾回收的线程，一般前面会有 nid=0x.......，这里 nid 的意思就是操作系统线程 id)，则通过 jstat -gcutil 命令监控当前系统的 GC 状况。然后通过 jmap dump:format=b,file= 导出系统当前的内存数据。导出之后将内存情况放到Mat工具中进行分析即可得出内存中主要是什么对象比较消耗内存，进而可以处理相关代码。


## 1.5. ★★★CPU高，查看所有进程占用率要远小于100
<!-- 
https://www.cnblogs.com/arnoldlu/p/12112225.html
https://www.liaochuntao.cn/2020/02/23/java-web-59/
https://blog.csdn.net/weixin_35078004/article/details/113707562
-->
1. 可能多个线程执行同一方法，每个线程占有不高，但总和比较大。  
2. ......

&emsp; 解决方案：
1. 可以使用arthas工具的thread -n -i分析。
2. 

## 1.6. 内存溢出排查实战  
### 1.6.1. 堆溢出演示  

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

### 1.6.2. 内存溢出的解决方案  
<!-- 
https://blog.csdn.net/prestigeding/article/details/89075661
-->
#### 1.6.2.1. 可以使用的解决方案
1. **<font color = "clime">修改JVM启动参数，直接增加内存。</font>**  
2. 检查错误日志，查看“OutOfMemory”错误前是否有其它异常或错误。  
3. **<font color = "clime">对代码进行走查和分析，找出可能发生内存溢出的位置。</font>** 重点排查以下几点：  
    * 检查对数据库查询中，是否有一次获得全部数据的查询。一般来说，如果一次取十万条记录到内存，就可能引起内存溢出。这个问题比较隐蔽，在上线前，数据库中数据较少，不容易出问题，上线后，数据库中数据多了，一次查询就有可能引起内存溢出。对数据库查询尽量采用分页查询。  
    * 检查代码是否有死循环或递归调用。  
    * 检查是否有大循环重复产生新对象实体。  
    * 检查List、Map等集合对象是否有使用后，未清除的问题。List、Map等集合对象会始终存有对对象的引用，使得这些对象不能被GC回收。  
4. **<font color = "clime">使用内存查看工具动态查看内存快照。</font>**  

#### 1.6.2.2. 使用内存查看工具分析堆dump文件  
1. 保存内存快照(两种方法)：  
    1. 添加JVM参数，该参数作用是：在程序内存溢出时输出dump文件。参数：-XX:+HeapDumpOnOutOfMemoryError -Xms20m -Xmx20m    
    &emsp; 随后找到项目地址，会发现在Project本目录中出现了个hprof文件，至此就把堆内存快照保存下来了。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-45.png)  
    2. jmap命令：先运行对应的jar进程，jps找到该进程ID，然后jmap设置导出格式。  
    &emsp; **<font color = "clime">注：线上环境不能直接使用jmap命令。找到未进行GC的一个节点，从线上环境摘除。然后再使用jmap命令。</font>**    

        ``
        jps
        jmap -dump:format=b,file=heap.hprof <pid>
        ``

2. 使用内存工具分析  
&emsp; 有了dump文件，就可以通过dump分析工具进行分析，比如常用的MAT，Jprofile，jvisualvm等工具都可以分析，这些工具都能够看出到底是哪里溢出，哪里创建了大量的对象等等信息。  


#### 1.6.2.3. ★★★jvm内存快照dump文件太大，怎么分析  
<!-- 
https://www.cnblogs.com/liangzs/p/8489321.html
jhat -J-Xmx512M a1.log
https://www.cnblogs.com/baihuitestsoftware/articles/6406271.html
-->

##### 1.6.2.3.1. ★★★jmap中live参数  
&emsp; 开发人员经常需要查看内存中的一些变量的值，来定位生产环境的问题。一般会使用jmap来抓dump，在抓dump的时候，会把堆全部扒下来：  

```text
jmap -dump:format=b,file=path pid
```

&emsp; 然后会生成一个几百M的包，让运维人员从生产环境拖下来再传给你，然后用jvisualvm打开，等打开这个dump的时候，看到想看的内存的时候，基本上半天时间已经过去了。  
&emsp; 其实丢了一个很重要的参数： **<font color = "clime">live，这个参数表示需要抓取目前在生命周期内的内存对象，也就是说GC收不走的对象，然后绝大部分情况下，需要的看的就是这些内存。</font>** 如果把这个参数加上：  

```text
jmap -dump:live,format=b,file=path pid  
```

&emsp; 那么抓下来的dump会减少一个数量级，在几十M左右，这样我们传输，打开这个dump的时间将大大减少，为解决故障赢取了宝贵的时间。   

##### 1.6.2.3.2. jhat指定最大堆内存
&emsp; 如果Dump文件太大，可能需要加上-J-Xmx512m这种参数指定最大堆内存，即jhat -J-Xmx512m -port 9998 /tmp/dump.dat。

##### 1.6.2.3.3. linux下的mat
&emsp; 如果dump文件太大，使用linux下的mat，既Memory Analyzer Tools。   

## 1.7. 线程死锁  
&emsp; 死循环、死锁、阻塞、页面打开慢等问题，打印线程堆栈是最好的解决问题的途径。  
<!-- 
一文学会Java死锁和CPU 100% 问题的排查技巧
https://club.perfma.com/article/2073508
-->


## 1.8. 其他情况  
&emsp; 如果通过top命令看到CPU并不高，并且系统内存占用率也比较低。此时就可以考虑是否是由于另外三种情况导致的问题。  
&emsp; 具体的可以根据具体情况分析：  
1. 如果是接口调用比较耗时，并且是不定时出现，则可以通过压测的方式加大阻塞点出现的频率，从而通过 jstack 查看堆栈信息，找到阻塞点。
2. 如果是某个功能突然出现停滞的状况，这种情况也无法复现，此时可以通过多次导出 jstack 日志的方式对比哪些用户线程是一直都处于等待状态，这些线程就是可能存在问题的线程。
3. 如果通过 jstack 可以查看到死锁状态，则可以检查产生死锁的两个线程的具体阻塞点，从而处理相应的问题。  

<!-- 
 ※※※jstack文件进行分析  
&emsp; 常见的是对整个jstack文件进行分析，通常比较关注WAITING和TIMED_WAITING的部分，BLOCKED就不用说了。我们可以使用命令cat jstack.log | grep "java.lang.Thread.State" | sort -nr | uniq -c来对jstack的状态有一个整体的把握，如果WAITING之类的特别多，那么多半是有问题啦。  
-->


## 1.9. JAVA线上故障排查
&emsp; 参考[JAVA线上故障排查](/docs/Linux/problem.md)  

