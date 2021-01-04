

<!-- TOC -->

- [1. Linux问题排查](#1-linux问题排查)
    - [1.1. OS诊断](#11-os诊断)
        - [1.1.1. CPU](#111-cpu)
        - [1.1.2. 内存占用](#112-内存占用)
            - [堆内内存](#堆内内存)
            - [堆外内存](#堆外内存)
        - [1.1.3. I/O](#113-io)
            - [网络I/O](#网络io)
    - [1.2. Linux下性能分析工具总结](#12-linux下性能分析工具总结)
    - [Linux服务器指标](#linux服务器指标)

<!-- /TOC -->

# 1. Linux问题排查
<!-- 
能说几个常见的Linux性能调优命令吗能说几个常见的Linux性能调优命令吗
https://mp.weixin.qq.com/s/FlRdH3ejNE2VybBMeyiFHg

面试官：生产服务器变慢了，你能谈谈诊断思路吗 
https://mp.weixin.qq.com/s/wXeHrdoEAmLApeC497Sk3w

JAVA 线上故障排查完整套路，从 CPU、磁盘、内存、网络、GC 一条龙！ 
https://mp.weixin.qq.com/s/aqvXXdrJyslXuhXE6IqYfw

一整套线上故障排查技巧
https://mp.weixin.qq.com/s/znVGpJhtA52UKiekHWqD3w
 这些排查内存问题的命令，你用过多少？ 
 https://mp.weixin.qq.com/s/1mdFnmHXLK3j5x75ecdx1Q

-->

&emsp; 针对Java应用，性能诊断工具主要分为两层：OS层面和Java应用层面（包括应用代码诊断和GC诊断）。  

&emsp; <font color = "red">线上故障主要会包括cpu、磁盘、内存以及网络问题，而大多数故障可能会包含不止一个层面的问题，所以进行排查时候尽量四个方面依次排查一遍。</font>  
&emsp; <font color = "lime">基本上出问题就是df、free、top 三连，然后依次jstack、jmap伺候，具体问题具体分析即可。</font>  

## 1.1. OS诊断  
<!-- 
https://www.linuxidc.com/Linux/2020-05/163174.htm
-->  
&emsp; OS的诊断主要关注的是CPU、Memory、I/O三个方面。很多时候服务出现问题，在这三者上会体现出现，比如CPU飙升，内存不足发生OOM等，这时候需要使用对应的工具，来对性能进行监控，对问题进行定位。  

### 1.1.1. CPU  
<!-- 
一般来讲我们首先会排查cpu方面的问题。cpu异常往往还是比较好定位的。原因包括业务逻辑问题(死循环)、频繁gc以及上下文切换过多。而最常见的往往是业务逻辑(或者框架逻辑)导致的，可以使用jstack来分析对应的堆栈情况。
-->

&emsp; 对于CPU主要关注平均负载（Load Average），CPU使用率，上下文切换次数（Context Switch）。  
&emsp; 通过top命令可以查看系统平均负载和CPU使用率。  

&emsp; PID进程在内核调用情况。  
* 如果是Java应用可通过 jstack 进程号 | grep 16进制线程号 -A 10 命令找到 CPU 消耗最多的线程方法堆栈。
* <font color = "red">是非Java应用可使用 perf</font>  
    perf top -p 7574  
    如果提示perf: command not found，使用yum install perf安装。

### 1.1.2. 内存占用  

1. free查看内存情况。
2. top查看内存占用，shift+m按MEN占用降序。

&emsp; 内存问题排查起来相对比CPU麻烦一些，场景也比较多。主要包括OOM、GC问题和堆外内存。一般来讲，会先用free命令先来检查一发内存的各种情况。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-82.png)  

#### 堆内内存

&emsp; 内存问题大多还都是堆内内存问题。表象上主要分为OOM和StackOverflow。  

#### 堆外内存  
&emsp; 如果碰到堆外内存溢出，那可真是太不幸了。首先堆外内存溢出表现就是物理常驻内存增长快，报错的话视使用方式都不确定，如果由于使用Netty导致的，那错误日志里可能会出现OutOfDirectMemoryError错误，如果直接是DirectByteBuffer，那会报OutOfMemoryError: Direct buffer memory。  
&emsp; 堆外内存溢出往往是和NIO的使用相关，一般先通过pmap来查看下进程占用的内存情况pmap -x pid | sort -rn -k3 | head -30，这段意思是查看对应pid倒序前30大的内存段。这边可以再一段时间后再跑一次命令看看内存增长情况，或者和正常机器比较可疑的内存段在哪里。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-83.png)  
&emsp; 如果确定有可疑的内存端，需要通过gdb来分析gdb --batch --pid {pid} -ex "dump memory filename.dump {内存起始地址} {内存起始地址+内存块大小}"  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-84.png)  
&emsp; 获取dump文件后可用heaxdump进行查看hexdump -C filename | less，不过大多数看到的都是二进制乱码。  
&emsp; NMT是Java7U40引入的HotSpot新特性，配合jcmd命令我们就可以看到具体内存组成了。需要在启动参数中加入 -XX:NativeMemoryTracking=summary 或者 -XX:NativeMemoryTracking=detail，会有略微性能损耗。  
&emsp; 一般对于堆外内存缓慢增长直到爆炸的情况来说，可以先设一个基线jcmd pid VM.native_memory baseline。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-85.png)  
&emsp; 然后等放一段时间后再去看看内存增长的情况，通过jcmd pid VM.native_memory detail.diff(summary.diff)做一下summary或者detail级别的diff。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-86.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-87.png)  
&emsp; 可以看到jcmd分析出来的内存十分详细，包括堆内、线程以及gc(所以上述其他内存异常其实都可以用nmt来分析)，这边堆外内存我们重点关注Internal的内存增长，如果增长十分明显的话那就是有问题了。  
&emsp; detail级别的话还会有具体内存段的增长情况，如下图。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-88.png)  
&emsp; 此外在系统层面，我们还可以使用strace命令来监控内存分配 strace -f -e "brk,mmap,munmap" -p pid  
&emsp; 这边内存分配信息主要包括了pid和内存地址。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-89.png)  
&emsp; 不过其实上面那些操作也很难定位到具体的问题点，关键还是要看错误日志栈，找到可疑的对象，搞清楚它的回收机制，然后去分析对应的对象。比如DirectByteBuffer分配内存的话，是需要full GC或者手动system.gc来进行回收的(所以最好不要使用-XX:+DisableExplicitGC)。  
&emsp; 那么其实可以跟踪一下DirectByteBuffer对象的内存情况，通过jmap -histo:live pid手动触发fullGC来看看堆外内存有没有被回收。如果被回收了，那么大概率是堆外内存本身分配的太小了，通过-XX:MaxDirectMemorySize进行调整。如果没有什么变化，那就要使用jmap去分析那些不能被gc的对象，以及和DirectByteBuffer之间的引用关系了。  

### 1.1.3. I/O  
&emsp; I/O包括磁盘I/O和网络I/O，一般情况下磁盘更容易出现I/O 瓶颈。通过iostat可以查看磁盘的读写情况，通过CPU的I/O wait可以看出磁盘I/O是否正常。  
&emsp; 如果磁盘I/O一直处于很高的状态，说明磁盘太慢或故障，成为了性能瓶颈，需要进行应用优化或者磁盘更换。  

#### 网络I/O
&emsp; 涉及到网络I/O的问题一般都比较复杂，场景多，定位难，成为了大多数开发的噩梦，应该是最复杂的了。这里会举一些例子，并从tcp层、应用层以及工具的使用等方面进行阐述。  

## 1.2. Linux下性能分析工具总结  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/Linux/Linux/linux-1.png) 



## Linux服务器指标  

......

