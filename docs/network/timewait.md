
<!-- TOC -->

- [1. TIME_WAIT问题](#1-time_wait问题)
    - [1.1. TIME_WAIT过多](#11-time_wait过多)
    - [1.2. TIME_WAIT分析](#12-time_wait分析)
    - [1.3. 解决办法](#13-解决办法)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  

1. TIME_WAIT状态，该socket所占用的本地端口号将一直无法释放。TIME_WAIT过多，可能出现做为客户端的程序无法向服务端建立新的socket连接的情况。  
2. **大量的TIME_WAIT状态 TCP 连接存在，是因为大量的短连接存在。TIME_WAIT状态时socket还占用端口。** TIME_WAIT状态默认为2MSL。    
3. 解决办法：
    1. 客户端  
    &emsp; **HTTP请求的头部，connection 设置为 keep-alive，** 保持存活一段时间：现在的浏览器，一般都这么进行了。     
    2. 服务器端  
        * **允许time_wait状态的socket被重用。**
        * 缩减time_wait时间，设置为 1 MSL（即，2 mins）。


# 1. TIME_WAIT问题
<!-- 
timeawit主要是确保客户端发送的ack包能让服务端收到
https://www.jianshu.com/p/a2938fc35573


运维同学说：服务端 TCP 连接的 TIME_WAIT 问题，大家都“疯”了，该怎么办？ 
https://mp.weixin.qq.com/s/dXpcXa_DZgJ-0PwaKdWr3g

-->

## 1.1. TIME_WAIT过多  
&emsp; 在高并发的场景下，TIME_WAIT连接存在，属于正常现象。  
&emsp; 线上场景中，持续的高并发场景

* 一部分 TIME_WAIT 连接被回收，但新的 TIME_WAIT 连接产生；
* 一些极端情况下，会出现大量的 TIME_WAIT 连接。  


&emsp; TIME_WAIT过多的影响  
&emsp; **在socket的TIME_WAIT状态结束之前，该socket所占用的本地端口号将一直无法释放。** 高TCP并发并且采用短连接方式进行通讯的通讯系统在高并发高负载下运行一段时间后， **<font color = "clime">就常常会出现做为客户端的程序无法向服务端建立新的socket连接的情况。</font>** 此时用“netstat -tanlp”命令查看系统将会发现机器上存在大量处于TIME_WAIT状态的socket连接，并且占用大量的本地端口号。最后，当该机器上的可用本地端口号被占完（或者达到用户可使用的文件句柄上限），而旧的大量处于TIME_WAIT状态的socket尚未被系统回收时，就会出现无法向服务端创建新的socket连接的情况。此时系统几乎停转，空有再好的性能也发挥不出来。  
&emsp; 注：这里涉及到一个服务器最多可以达到多少连接上限的问题，由于TCP的要素--四元组（源地址，源端口，目的地址，目的端口）中有一个不同就可以连接起新的连接，所以理论上一个服务器能接受的TCP连接数量貌似是无上限的（不考虑存储连接信息的表资源被用光和其他资源耗尽的情况）  


## 1.2. TIME_WAIT分析  
&emsp; 大量的 TIME_WAIT 状态 TCP 连接存在，其本质原因是什么？  
&emsp; **<font color = "clime">大量的短连接存在</font>**  
&emsp; 特别是 HTTP 请求中，如果 connection 头部取值被设置为 close 时，基本都由「服务端」发起主动关闭连接。而，TCP 四次挥手关闭连接机制中，为了保证 ACK 重发和丢弃延迟数据，设置 time_wait 为 2 倍的 MSL（报文最大存活时间）。  

## 1.3. 解决办法  
&emsp; 一般解决办法：  
1. 客户端  
&emsp; HTTP 请求的头部，connection 设置为 keep-alive，保持存活一段时间：现在的浏览器，一般都这么进行了。     
2. 服务器端  
    * 允许 time_wait 状态的 socket 被重用
    * 缩减 time_wait 时间，设置为 1 MSL（即，2 mins）
