

<!-- TOC -->

- [1. ~~Redis的事件与Reactor~~](#1-redis的事件与reactor)
    - [1.1. 概述](#11-概述)
    - [1.2. 文件事件](#12-文件事件)
    - [1.3. 时间事件（定时任务）](#13-时间事件定时任务)
    - [1.4. 事件的调度与执行](#14-事件的调度与执行)

<!-- /TOC -->


# 1. ~~Redis的事件与Reactor~~  
&emsp; 参考《Redis设计与实现》第12章  
<!--
Redis基于Reactor模式开发了网络事件处理
https://blog.csdn.net/weixin_45931215/article/details/118728932

https://blog.csdn.net/shandadadada/article/details/106175386
https://www.cnblogs.com/harvyxu/p/7499396.html
-->

## 1.1. 概述
&emsp; Redis服务器是一个事件驱动程序。  
&emsp; Redis没有使用第三方的libevent等网络库，而是基于Reactor模式开发了一个单线程的Reactor模型的事件处理模型。称为文件事件处理器，其使用I/O多路复用，同时监听多个套接字，根据套接字执行的任务来为套接字关联不同的事件处理器。  
&emsp; Redis在主循环中统一处理文件事件和时间事件，信号事件则由专门的handler来处理。  


## 1.2. 文件事件
&emsp; 服务器通过套接字与客户端或者其它服务器进行通信，文件事件就是对套接字操作的抽象。  
&emsp; **<font color = "clime">Redis基于`Reactor模式`开发了自己的网络事件处理器，使用 I/O 多路复用程序来同时监听多个套接字，并将到达的事件传送给文件事件分派器，分派器会根据套接字产生的事件类型调用相应的事件处理器。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-56.png)  

## 1.3. 时间事件（定时任务）
&emsp; 服务器有一些操作需要在给定的时间点执行，时间事件是对这类定时操作的抽象。  
&emsp; 时间事件又分为：  

* 定时事件：是让一段程序在指定的时间之内执行一次；  
* 周期性事件：是让一段程序每隔指定时间就执行一次。Redis将所有时间事件都放在一个无序链表中，通过遍历整个链表查找出已到达的时间事件，并调用相应的事件处理器。  

## 1.4. 事件的调度与执行
&emsp; 服务器需要不断监听文件事件的套接字才能得到待处理的文件事件，但是不能一直监听，否则时间事件无法在规定的时间内执行，因此监听时间应该根据距离现在最近的时间事件来决定。  
&emsp; 事件调度与执行由 aeProcessEvents 函数负责，伪代码如下：  

```
def aeProcessEvents():
    # 获取到达时间离当前时间最接近的时间事件
    time_event = aeSearchNearestTimer()
    # 计算最接近的时间事件距离到达还有多少毫秒
    remaind_ms = time_event.when - unix_ts_now()
    # 如果事件已到达，那么 remaind_ms 的值可能为负数，将它设为 0
    if remaind_ms < 0:
        remaind_ms = 0
    # 根据 remaind_ms 的值，创建 timeval
    timeval = create_timeval_with_ms(remaind_ms)
    # 阻塞并等待文件事件产生，最大阻塞时间由传入的 timeval 决定
    aeApiPoll(timeval)
    # 处理所有已产生的文件事件
    procesFileEvents()
    # 处理所有已到达的时间事件
    processTimeEvents()
```
&emsp; 将 aeProcessEvents 函数置于一个循环里面，加上初始化和清理函数，就构成了 Redis 服务器的主函数，伪代码如下：  

```
def main():
    # 初始化服务器
    init_server()
    # 一直处理事件，直到服务器关闭为止
    while server_is_not_shutdown():
        aeProcessEvents()
    # 服务器关闭，执行清理操作
    clean_server()
```
&emsp; 从事件处理的角度来看，服务器运行流程如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-57.png)  

 
