

<!-- TOC -->

- [1. Linux问题排查](#1-linux问题排查)
    - [1.1. OS诊断](#11-os诊断)
        - [1.1.1. CPU](#111-cpu)
        - [1.1.2. 内存占用](#112-内存占用)
        - [1.1.3. I/O](#113-io)
    - [1.2. Linux下性能分析工具总结](#12-linux下性能分析工具总结)
    - [Linux服务器指标](#linux服务器指标)

<!-- /TOC -->

# 1. Linux问题排查
&emsp; 针对Java应用，性能诊断工具主要分为两层：OS层面和Java应用层面（包括应用代码诊断和GC诊断）。  

## 1.1. OS诊断  
&emsp; OS的诊断主要关注的是CPU、Memory、I/O三个方面。很多时候服务出现问题，在这三者上会体现出现，比如CPU飙升，内存不足发生OOM等，这时候需要使用对应的工具，来对性能进行监控，对问题进行定位。  

### 1.1.1. CPU  
&emsp; 对于CPU主要关注平均负载（Load Average），CPU使用率，上下文切换次数（Context Switch）。  
&emsp; 通过top命令可以查看系统平均负载和CPU使用率。  

&emsp; PID进程在内核调用情况。  
* 如果是Java应用可通过 jstack 进程号 | grep 16进制线程号 -A 10 命令找到 CPU 消耗最多的线程方法堆栈。
* 是非 Java 应用可使用 perf  
    perf top -p 7574  
    如果提示perf: command not found，使用yum install perf安装。

### 1.1.2. 内存占用  
<!-- 
https://www.linuxidc.com/Linux/2020-05/163174.htm
-->  
1. free查看内存情况
2. top查看内存占用，shift+m按MEN占用降序

### 1.1.3. I/O  

&emsp; I/O包括磁盘I/O和网络I/O，一般情况下磁盘更容易出现I/O 瓶颈。通过iostat可以查看磁盘的读写情况，通过CPU的I/O wait可以看出磁盘I/O是否正常。  
&emsp; 如果磁盘I/O一直处于很高的状态，说明磁盘太慢或故障，成为了性能瓶颈，需要进行应用优化或者磁盘更换。  


## 1.2. Linux下性能分析工具总结  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/Linux/Linux/linux-1.png) 



## Linux服务器指标  

......

