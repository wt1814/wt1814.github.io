---
title: Redis原理
date: 2020-05-16 00:00:00
tags:
    - Redis
---

<!-- TOC -->

- [1. Redis为什么这么快？](#1-redis为什么这么快)
- [2. Redis的事件](#2-redis的事件)
    - [2.1. 文件事件](#21-文件事件)
    - [2.2. 时间事件](#22-时间事件)
    - [2.3. 事件的调度与执行](#23-事件的调度与执行)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-55.png)  


# 1. Redis为什么这么快？  

1. 内存  
&emsp; KV 结构的内存数据库，时间复杂度 O(1)。  

2. 单线程  
    &emsp; 单线程的好处：   
    * 没有创建线程、销毁线程带来的消耗  
    * 避免了上线文切换导致的 CPU 消耗  
    * 避免了线程之间带来的竞争问题，例如加锁释放锁死锁等 

3. 异步非阻塞  
&emsp; 异步非阻塞 I/O，多路复用处理并发连接。 

# 2. Redis的事件  
&emsp; Redis 服务器是一个事件驱动程序。  

## 2.1. 文件事件
&emsp; 服务器通过套接字与客户端或者其它服务器进行通信，文件事件就是对套接字操作的抽象。  
&emsp; Redis 基于 Reactor 模式开发了自己的网络事件处理器，使用 I/O 多路复用程序来同时监听多个套接字，并将到达的事件传送给文件事件分派器，分派器会根据套接字产生的事件类型调用相应的事件处理器。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-56.png)  

## 2.2. 时间事件
&emsp; 服务器有一些操作需要在给定的时间点执行，时间事件是对这类定时操作的抽象。  
&emsp; 时间事件又分为：  

* 定时事件：是让一段程序在指定的时间之内执行一次；  
* 周期性事件：是让一段程序每隔指定时间就执行一次。Redis 将所有时间事件都放在一个无序链表中，通过遍历整个链表查找出已到达的时间事件，并调用相应的事件处理器。  

## 2.3. 事件的调度与执行
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

 





