
<!-- TOC -->

- [1. Redis为什么这么快？](#1-redis为什么这么快)
- [2. Redis的事件](#2-redis的事件)
    - [2.1. 文件事件](#21-文件事件)
    - [2.2. 时间事件](#22-时间事件)
    - [2.3. 事件的调度与执行](#23-事件的调度与执行)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-55.png)  


# 1. Redis为什么这么快？  

<!-- 
 面试时说Redis是单线程的，被喷惨了！ 
 https://mp.weixin.qq.com/s/ucJ8nVwnbWvMOg0hQIJlAg


https://mp.weixin.qq.com/s/5Kdz3-Xx-tMPbhKMGundfw

https://mp.weixin.qq.com/s/PMGYoySBrOMVZvRZIyTwXg

Redis 6.0 新特性-多线程连环13问！ 
https://mp.weixin.qq.com/s/FZu3acwK6zrCBZQ_3HoUgw
-->

Redis是单线程的，为什么采用单线程的Redis也会如此之快呢？  
严格来说，Redis Server是多线程的，只是它的请求处理整个流程是单线程处理的。  

Redis的性能非常之高，每秒可以承受10W+的QPS，它如此优秀的性能主要取决于以下几个方面：  

* 纯内存操作
* 使用IO多路复用技术
* 非CPU密集型任务
* 单线程的优势

1. 内存  
&emsp; KV 结构的内存数据库，时间复杂度 O(1)。  

2. 单线程的优势  
    &emsp; 单线程的好处：   
    * 没有创建线程、销毁线程带来的消耗  
    * 避免了上线文切换导致的 CPU 消耗  
    * 避免了线程之间带来的竞争问题，例如加锁释放锁死锁等 

3. 异步非阻塞  
&emsp; 异步非阻塞 I/O，多路复用处理并发连接。 

4. 多线程优化  
Redis Server本身是多线程的，除了请求处理流程是单线程处理之外，Redis内部还有其他工作线程在后台执行，它负责异步执行某些比较耗时的任务，例如AOF每秒刷盘、AOF文件重写都是在另一个线程中完成的。  

而在Redis 4.0之后，Redis引入了lazyfree的机制，提供了unlink、flushall aysc、flushdb async等命令和lazyfree-lazy-eviction、lazyfree-lazy-expire等机制来异步释放内存，它主要是为了解决在释放大内存数据导致整个redis阻塞的性能问题。  

在删除大key时，释放内存往往都比较耗时，所以Redis提供异步释放内存的方式，让这些耗时的操作放到另一个线程中异步去处理，从而不影响主线程的执行，提高性能。  

到了Redis 6.0，Redis又引入了多线程来完成请求数据的协议解析，进一步提升性能。它主要是解决高并发场景下，单线程解析请求数据协议带来的压力。请求数据的协议解析由多线程完成之后，后面的请求处理阶段依旧还是单线程排队处理。  

可见，Redis并不是保守地认为单线程有多好，也不是为了使用多线程而引入多线程。Redis作者很清楚单线程和多线程的使用场景，针对性地优化，这是非常值得我们学习的。  


5. 缺点  
上面介绍了单线程可以达到如此高的性能，并不是说它就没有缺点了。

单线程处理最大的缺点就是，如果前一个请求发生耗时比较久的操作，那么整个Redis就会阻塞住，其他请求也无法进来，直到这个耗时久的操作处理完成并返回，其他请求才能被处理到。  
我们平时遇到Redis变慢或长时间阻塞的问题，90%也都是因为Redis处理请求是单线程这个原因导致的。  

所以，我们在使用Redis时，一定要避免非常耗时的操作，例如使用时间复杂度过高的方式获取数据、一次性获取过多的数据、大量key集中过期导致Redis淘汰key压力变大等等，这些场景都会阻塞住整个处理线程，直到它们处理完成，势必会影响业务的访问。  


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

 





