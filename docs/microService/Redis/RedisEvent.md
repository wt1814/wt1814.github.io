

<!-- TOC -->

- [1. ~~Redis的事件与Reactor~~](#1-redis的事件与reactor)
    - [1.1. 概述](#11-概述)
    - [1.2. 文件事件](#12-文件事件)
        - [1.2.1. 文件事件处理器的构成](#121-文件事件处理器的构成)
        - [1.2.2. I/O多路复用程序的实现](#122-io多路复用程序的实现)
        - [1.2.3. 事件的类型](#123-事件的类型)
        - [1.2.4. 文件事件的处理器](#124-文件事件的处理器)
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
&emsp; Redis服务器是一个事件驱动程序，服务器需要处理以下两类事件：  

* 文件事件（file event）: Redis服务器通过套接字与客户端（或者其他Redis服务器） 进行连接，而文件事件就是服务器对套接字操作的抽象。服务器与客户端（或者其他服务器）的通信会产生相应的文件事件，而服务器则通过监听并处理这些事件来 完成一系列网络通信操作。  
* 时间事件（time event） : Redis服务器中的一些操作（比如serverCron函数）需 要在给定的时间点执行，而时间事件就是服务器对这类定时操作的抽象。    

&emsp; Redis没有使用第三方的libevent等网络库，而是基于Reactor模式开发了一个单线程的Reactor模型的事件处理模型。称为文件事件处理器，其使用I/O多路复用，同时监听多个套接字，根据套接字执行的任务来为套接字关联不同的事件处理器。  
&emsp; Redis在主循环中统一处理文件事件和时间事件，信号事件则由专门的handler来处理。  


## 1.2. 文件事件
<!-- 
&emsp; 服务器通过套接字与客户端或者其它服务器进行通信，文件事件就是对套接字操作的抽象。  
&emsp; **<font color = "clime">Redis基于`Reactor模式`开发了自己的网络事件处理器，使用 I/O 多路复用程序来同时监听多个套接字，并将到达的事件传送给文件事件分派器，分派器会根据套接字产生的事件类型调用相应的事件处理器。</font>**  

-->
&emsp; Redis基于Reactor模式开发了自己的网络事件处理器：这个处理器被称为文件事件处理器（file event handler）：  

* 文件事件处理器使用I/O多路复用（multiplexing）程序来同时监听多个套接字，并 根据套接字目前执行的任务来为套接字关联不同的事件处理器。  
* 当被监听的套接字准备好执行连接应答（accept）、读取（read）、写入（write）、关 闭（close）等操作时，与操作相对应的文件事件就会产生，这时文件事件处理器就 会调用套接字之前关联好的事件处理器来处理这些事件。  

&emsp; 虽然文件事件处理器以单线程方式运行，但通过使用I/O多路复用程序来监听多个套接 字，文件事件处理器既实现了高性能的网络通信模型，又可以很好地与Redis服务器中其他 同样以单线程方式运行的模块进行对接，这保持了 Redis内部单线程设计的简单性。  

### 1.2.1. 文件事件处理器的构成
&emsp; 下图展示了文件事件处理器的四个组成部分，它们分别是套接字、I/O多路复用程序、文件事件分派器(dispatcher)，以及 事件处理器。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-56.png)  
&emsp; 文件事件是对套接字操作的抽象, 每当一个套接字准备好执行连接应答 (accept)、写入、读取、关闭等操作时, 就会产生一个文件事件。因为一个服务器 通常会连接多个套接字，所以多个文件事 件有可能会并发地出现。  
&emsp; I/O多路复用程序负责监听多个套接字, 并向文件事件分派器传送那些产生了事件的套接字。  
&emsp; 尽管多个文件事件可能会并发地出 现，但I/O多路复用程序总是会将所有产 生事件的套接字都放到一个队列里面，然后通过这个队列，以有序(sequentially).同步 (synchronously )、每次一个套接字的方式向文件事件分派器传送套接字。当上一个套接字产 生的事件被处理完毕之后(该套接字为事件所关联的事件处理器执行完毕)，I/O多路复用 程序才会继续向文件事件分派器传送下一个套接字，如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-120.png)  
&emsp; 文件事件分派器接收I/O多路复用程序传来的套接字，并根据套接字产生的事件的类 型，调用相应的事件处理器。  
&emsp; 服务器会为执行不同任务的套接字关联不同的事件处理器，这些处理器是一个个函数, 它们定义了某个事件发生时，服务器应该执行的动作。  

### 1.2.2. I/O多路复用程序的实现
&emsp; ...  

### 1.2.3. 事件的类型
&emsp; ...  

### 1.2.4. 文件事件的处理器
&emsp; ...  

## 1.3. 时间事件（定时任务）
&emsp; Redis的时间事件分为以下两类：  

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

 
