
<!-- TOC -->

- [1. JVM问题排查](#1-jvm问题排查)
    - [1.1. 线上可能出现的问题](#11-线上可能出现的问题)
    - [1.2. CPU飚高](#12-cpu飚高)
        - [1.2.1. 现象](#121-现象)
        - [1.2.2. 排查步骤](#122-排查步骤)
            - [1.2.2.1. 情况一](#1221-情况一)
            - [1.2.2.2. 情况二](#1222-情况二)
    - [1.3. 内存溢出排查实战](#13-内存溢出排查实战)
        - [1.3.1. 堆溢出演示](#131-堆溢出演示)
        - [1.3.2. 内存溢出的解决方案](#132-内存溢出的解决方案)
            - [1.3.2.1. 使用内存查看工具分析堆dump文件](#1321-使用内存查看工具分析堆dump文件)
            - [1.3.2.2. jvm内存快照dump文件太大，怎么分析](#1322-jvm内存快照dump文件太大怎么分析)
    - [1.4. 线程死锁](#14-线程死锁)

<!-- /TOC -->

<!-- 
这轮面试，居然只有20%的人了解 MAT 神器 
https://mp.weixin.qq.com/s/_HsvTtjZiJe43FYYYtKWMA
-->

# 1. JVM问题排查  

    获取线程stack快照，排查CPU飚高；  
    获取堆heap快照，排查内存溢出的问题；  

## 1.1. 线上可能出现的问题  
&emsp; <font color = "red">对于线上系统突然产生的运行缓慢问题，如果该问题导致线上系统不可用，</font><font color = "lime">那么首先需要做的就是，导出线程堆栈和内存信息，然后重启系统，尽快保证系统的可用性。</font>这种情况可能的原因主要有两种：  

* **<font color = "red">代码中某个位置读取数据量较大，导致系统内存耗尽，从而导致Full GC次数过多，系统缓慢；</font>**  
* 代码中有比较耗CPU的操作，导致CPU过高，系统运行缓慢；  

&emsp; 相对来说，这是出现频率最高的两种线上问题，而且它们会直接导致系统不可用。另外有几种情况也会导致某个功能运行缓慢，但是不至于导致系统不可用：  

* 代码某个位置有阻塞性的操作，导致该功能调用整体比较耗时，但出现是比较随机的；  
* 某个线程由于某种原因而进入WAITING状态，此时该功能整体不可用，但是无法复现；  
* 由于锁使用不当，导致多个线程进入死锁状态，从而导致系统整体比较缓慢。  

&emsp; 对于这三种情况，通过查看CPU和系统内存情况是无法查看出具体问题的，因为它们相对来说都是具有一定阻塞性操作，CPU和系统内存使用情况都不高，但是功能却很慢。  

## 1.2. CPU飚高  
<!-- 
https://www.cnblogs.com/klvchen/p/11089632.html
-->

### 1.2.1. 现象   
&emsp; **<font color = "red">CPU过高可能是系统频繁的进行Full GC，导致系统缓慢。</font><font color = "lime">而平常也可能遇到比较耗时的计算，导致CPU过高的情况。</font>**

&emsp; **<font color = "lime">怎么区分导致CPU过高的原因具体是Full GC次数过多还是代码中有比较耗时的计算？</font>**  
&emsp; 如果是Full GC次数过多，那么通过jstack得到的线程信息会是类似于VM Thread之类的线程，而如果是代码中有比较耗时的计算，那么得到的就是一个线程的具体堆栈信息。如下是一个代码中有比较耗时的计算，导致CPU过高的线程信息：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-47.png)  
&emsp; 这里可以看到，在请求UserController的时候，由于该Controller进行了一个比较耗时的调用，导致该线程的CPU一直处于100%。可以根据堆栈信息，直接定位到UserController的34行，查看代码中具体是什么原因导致计算量如此之高。  

### 1.2.2. 排查步骤  

#### 1.2.2.1. 情况一  
&emsp; 通过top命令查看CPU情况，如果CPU比较高。  

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

    输出如下：  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-44.png)  


    注意：  
    这里又分为两种情况：  
    1: 如果是正常的用户线程，则通过该线程的堆栈信息查看其具体是在哪处用户代码处运行比较消耗CPU。  
    2: 如果该线程是VM Thread（VM Thread 指的就是垃圾回收的线程，一般前面会有 nid=0x.......，这里 nid 的意思就是操作系统线程 id），则通过 jstat -gcutil 命令监控当前系统的 GC 状况。然后通过 jmap dump:format=b,file= 导出系统当前的内存数据。导出之后将内存情况放到Mat 工具中进行分析即可得出内存中主要是什么对象比较消耗内存，进而可以处理相关代码。

#### 1.2.2.2. 情况二  
&emsp; 如果通过top命令看到CPU并不高，并且系统内存占用率也比较低。此时就可以考虑是否是由于另外三种情况导致的问题。  
&emsp; 具体的可以根据具体情况分析：  
1. 如果是接口调用比较耗时，并且是不定时出现，则可以通过压测的方式加大阻塞点出现的频率，从而通过 jstack 查看堆栈信息，找到阻塞点。
2. 如果是某个功能突然出现停滞的状况，这种情况也无法复现，此时可以通过多次导出 jstack 日志的方式对比哪些用户线程是一直都处于等待状态，这些线程就是可能存在问题的线程。
3. 如果通过 jstack 可以查看到死锁状态，则可以检查产生死锁的两个线程的具体阻塞点，从而处理相应的问题。  
<!-- 
 ※※※jstack文件进行分析  
&emsp; 常见的是对整个jstack文件进行分析，通常比较关注WAITING和TIMED_WAITING的部分，BLOCKED就不用说了。我们可以使用命令cat jstack.log | grep "java.lang.Thread.State" | sort -nr | uniq -c来对jstack的状态有一个整体的把握，如果WAITING之类的特别多，那么多半是有问题啦。  

-->

## 1.3. 内存溢出排查实战  
### 1.3.1. 堆溢出演示  

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

### 1.3.2. 内存溢出的解决方案  
1. **<font color = "lime">修改JVM启动参数，直接增加内存。</font>**  
2. 检查错误日志，查看“OutOfMemory”错误前是否有其它异常或错误。  
3. **<font color = "lime">对代码进行走查和分析，找出可能发生内存溢出的位置。</font>** 重点排查以下几点：  
    * 检查对数据库查询中，是否有一次获得全部数据的查询。一般来说，如果一次取十万条记录到内存，就可能引起内存溢出。这个问题比较隐蔽，在上线前，数据库中数据较少，不容易出问题，上线后，数据库中数据多了，一次查询就有可能引起内存溢出。对数据库查询尽量采用分页查询。  
    * 检查代码是否有死循环或递归调用。  
    * 检查是否有大循环重复产生新对象实体。  
    * 检查List、Map等集合对象是否有使用后，未清除的问题。List、Map等集合对象会始终存有对对象的引用，使得这些对象不能被GC回收。  
4. 使用内存查看工具动态查看内存快照。 

#### 1.3.2.1. 使用内存查看工具分析堆dump文件  
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

#### 1.3.2.2. jvm内存快照dump文件太大，怎么分析  
<!-- 
https://www.cnblogs.com/liangzs/p/8489321.html
-->
* 如果Dump文件太大，可能需要加上-J-Xmx512m这种参数指定最大堆内存，即jhat -J-Xmx512m -port 9998 /tmp/dump.dat。
* 如果dump文件太大，使用linux下的mat，既Memory Analyzer Tools。   

## 1.4. 线程死锁  
&emsp; 死循环、死锁、阻塞、页面打开慢等问题，打印线程dump是最好的解决问题的途径。  
<!-- 
一文学会Java死锁和CPU 100% 问题的排查技巧
https://club.perfma.com/article/2073508
-->
