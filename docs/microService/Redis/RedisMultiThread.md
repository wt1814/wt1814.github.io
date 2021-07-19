
<!-- TOC -->

- [1. Redis多线程](#1-redis多线程)
    - [1.1. 为什么Redis一开始使用单线程？](#11-为什么redis一开始使用单线程)
    - [1.2. ~~为什么引入多线程？~~](#12-为什么引入多线程)
    - [1.3. Redis 6.0 默认是否开启了多线程？](#13-redis-60-默认是否开启了多线程)
    - [1.4. Redis 6.0 多线程的实现机制？](#14-redis-60-多线程的实现机制)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 为什么Redis一开始使用单线程？  
&emsp; 基于内存而且使用多路复用技术，单线程速度很快，又保证了多线程的特点。因此没有必要使用多线程。  
2. 为什么引入多线程？  
&emsp; **<font color = "clime">因为读写网络的read/write系统调用在Redis执行期间占用了大部分CPU时间，如果把网络读写做成多线程的方式对性能会有很大提升。</font>**  
&emsp; **<font color = "clime">Redis的多线程部分只是用来处理网络数据的读写和协议解析，执行命令仍然是单线程。</font>** 
3. 官方建议：4核的机器建议设置为2或3个线程，8核的建议设置为6个线程， **<font color = "clime">线程数一定要小于机器核数，尽量不超过8个。</font>**   


# 1. Redis多线程
<!-- 
重要  ★★★Redis 多线程网络模型全面揭秘 
https://mp.weixin.qq.com/s/-s5BaFx2IV5xbyjgZWk-5A

https://baijiahao.baidu.com/s?id=1664285811566919896&wfr=spider&for=pc&searchword=redis%E5%A4%9A%E7%BA%BF%E7%A8%8B
https://www.cnblogs.com/gz666666/p/12901507.html

https://www.hollischuang.com/archives/6198
-->

## 1.1. 为什么Redis一开始使用单线程？  
&emsp; 不管是单线程或者是多线程都是为了提升Redis的开发效率，因为Redis是一个基于内存的数据库，还要处理大量的外部的网络请求，这就不可避免的要进行多次IO。好在Redis使用了很多优秀的机制来保证了它的高效率。那么为什么Redis要设计成单线程模式的呢？可以总结如下：  

&emsp; （1）IO多路复用
&emsp; 看一下Redis顶层设计。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-115.png)  
&emsp; FD是一个文件描述符，意思是表示当前文件处于可读、可写还是异常状态。使用 I/O 多路复用机制同时监听多个文件描述符的可读和可写状态。  
&emsp; 一旦受到网络请求就会在内存中快速处理，由于绝大多数的操作都是纯内存的，所以处理的速度会非常地快。也就是说在单线程模式下，即使连接的网络处理很多，因为有IO多路复用，依然可以在高速的内存处理中得到忽略。  

&emsp; （2）可维护性高  
&emsp; 多线程模型虽然在某些方面表现优异，但是它却引入了程序执行顺序的不确定性，带来了并发读写的一系列问题。单线程模式下，可以方便地进行调试和测试。  

&emsp; （3）基于内存，单线程状态下效率依然高  
&emsp; 多线程能够充分利用CPU的资源，但对于Redis来说，由于基于内存速度那是相当的高，能达到在一秒内处理10万个用户请求，如果一秒十万还不能满足，就可以使用Redis分片的技术来交给不同的Redis服务器。这样的做饭避免了在同一个 Redis 服务中引入大量的多线程操作。  
&emsp; 而且基于内存，除非是要进行AOF备份，否则基本上不会涉及任何的 I/O 操作。这些数据的读写由于只发生在内存中，所以处理速度是非常快的；用多线程模型处理全部的外部请求可能不是一个好的方案。  

&emsp; 基本上可以总结成两句话，基于内存而且使用多路复用技术，单线程速度很快，又保证了多线程的特点。因为没有必要使用多线程。  

## 1.2. ~~为什么引入多线程？~~
&emsp; 刚刚说了一堆使用单线程的好处，现在话锋一转，又要说为什么要引入多线程，别不适应。引入多线程说明Redis在有些方面，单线程已经不具有优势了。  
&emsp; **<font color = "clime">因为读写网络的read/write系统调用在Redis执行期间占用了大部分CPU时间，如果把网络读写做成多线程的方式对性能会有很大提升。</font>**  
&emsp; **<font color = "clime">Redis的多线程部分只是用来处理网络数据的读写和协议解析，执行命令仍然是单线程。</font>** 之所以这么设计是不想 Redis 因为多线程而变得复杂，需要去控制 key、lua、事务，LPUSH/LPOP 等等的并发问题。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-119.png)  
&emsp; 单线程执行两个命令需要6个时间维度，多线程执行只需要4个时间维度。  


&emsp; Redis 在最新的几个版本中加入了一些可以被其他线程异步处理的删除操作，如UNLINK、FLUSHALL ASYNC 和 FLUSHDB ASYNC，为什么会需要这些删除操作，而它们为什么需要通过多线程的方式异步处理？  
&emsp; Redis可以使用del命令删除一个元素，如果这个元素非常大，可能占据了几十兆或者是几百兆，那么在短时间内是不能完成的，这样一来就需要多线程的异步支持。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-116.png)  
&emsp; 现在删除工作可以在后台进行。  

## 1.3. Redis 6.0 默认是否开启了多线程？
&emsp; 否，在conf文件进行配置  

```text
io-threads-do-reads yes  
io-threads 线程数
```
&emsp; 官方建议：4 核的机器建议设置为 2 或 3 个线程，8 核的建议设置为 6 个线程， **<font color = "clime">线程数一定要小于机器核数，尽量不超过8个。</font>**   

## 1.4. Redis 6.0 多线程的实现机制？  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-117.png)  
&emsp; 流程简述如下：  

* 主线程负责接收建立连接请求，获取 Socket 放入全局等待读处理队列。  
* 主线程处理完读事件之后，通过 RR（Round Robin）将这些连接分配给这些 IO 线程。  
* 主线程阻塞等待 IO 线程读取 Socket 完毕。  
* 主线程通过单线程的方式执行请求命令，请求数据读取并解析完成，但并不执行。 
* 主线程阻塞等待 IO 线程将数据回写 Socket 完毕。  
* 解除绑定，清空等待队列。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-118.png)  

&emsp; 该设计有如下特点：  

* IO 线程要么同时在读 Socket，要么同时在写，不会同时读或写。  
* IO 线程只负责读写 Socket 解析命令，不负责命令处理。

&emsp; **开启多线程后，是否会存在线程并发安全问题？**  
&emsp; 不会，Redis 的多线程部分只是用来处理网络数据的读写和协议解析，执行命令仍然是单线程顺序执行。  
