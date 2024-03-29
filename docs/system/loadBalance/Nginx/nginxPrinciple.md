
<!-- TOC -->

- [1. nginx原理](#1-nginx原理)
    - [1.1. 进程模型](#11-进程模型)
    - [1.2. 多进程机制](#12-多进程机制)
    - [1.3. 基于异步及非阻塞的事件驱动模型](#13-基于异步及非阻塞的事件驱动模型)
        - [1.3.1. 异步非阻塞机制](#131-异步非阻塞机制)
        - [1.3.2. Nginx事件驱动模型](#132-nginx事件驱动模型)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 多进程 
    1. Nginx启动时，会生成两种类型的进程，一个主进程master，一个（windows版本的目前只有一个）或多个工作进程worker。  
        * 主进程并不处理网络请求，主要负责调度工作进程：加载配置、启动工作进程、非停升级。  
        * **<font color = "red">`一般推荐worker进程数与CPU内核数一致，这样一来不存在大量的子进程生成和管理任务，避免了进程之间竞争CPU资源和进程切换的开销。`</font>**  
    2. <font color = "red">多进程的优缺点：</font>  
        1. 使用进程的好处是各个进程之间相互独立，不需要加锁，减少了使用锁对性能造成影响，同时降低编程的复杂度，降低开发成本。  
        2. 采用独立的进程，`可以让进程互相之间不会影响`，如果一个进程发生异常退出时，其它进程正常工作，master进程则很快启动新的worker进程，确保服务不会中断，从而将风险降到最低。     
        3. 缺点是操作系统生成一个子进程需要进行内存复制等操作，在资源和时间上会产生一定的开销。当有大量请求时，会导致系统性能下降。   
2. 多路复用，Reactor


# 1. nginx原理  
<!-- 

https://m.php.cn/nginx/440281.html
https://blog.csdn.net/qq422431474/article/details/108244352

-->

## 1.1. 进程模型  
&emsp; Nginx启动时，会生成两种类型的进程，一个主进程master， 一个(windows版本的目前只有一个)或多个工作进程worker。因此，Nginx启动以后，查看操作系统的进程列表，就能看到至少有两个Nginx进程。  

* 主进程并不处理网络请求，主要负责调度工作进程：加载配置、启动工作进程、非停升级。  
* 服务器实际处理网络请求及响应的是工作进程worker，在类unix系统上，Nginx可以配置多个worker，而每个worker进程都可以同时处理数以千计的网络请求。  
![image](http://182.92.69.8:8081/img/Linux/Nginx/nginx-7.png) 

## 1.2. 多进程机制  
&emsp; 服务器每当收到一个客户端请求时，就有服务器主进程(master process)生成一个子进程(worker process)出来和客户端建立连接进行交互，直到连接断开，该子进程结束。  

&emsp; <font color = "red">多进程的优缺点：</font>  
1. 使用进程的好处是各个进程之间相互独立，不需要加锁，减少了使用锁对性能造成影响，同时降低编程的复杂度，降低开发成本。  
2. 采用独立的进程，可以让进程互相之间不会影响，如果一个进程发生异常退出时，其它进程正常工作，master进程则很快启动新的worker进程，确保服务不会中断，从而将风险降到最低。     
3. 缺点是操作系统生成一个子进程需要进行内存复制等操作，在资源和时间上会产生一定的开销。当有大量请求 时，会导致系统性能下降。   

&emsp; **<font color = "red">一般推荐worker进程数与CPU内核数一致，这样一来不存在大量的子进程生成和管理任务，避免了进程之间竞争CPU资源和进程切换的开销。</font>**  
&emsp; 而且Nginx为了更好的利用多核特性，提供了CPU亲缘性的绑定选项，可以将某一个进程绑定在某一个核上，这样就不会因为进程的切换带来Cache的失效。  

&emsp; 对于每个请求，有且只有一个 工作进程 对其处理。首先，每个 worker 进程都是从 master进程 fork 过来。在 master 进程里面，先建立好需要 listen 的 socket(listenfd) 之后，然后再 fork 出多个 worker 进程。  
&emsp; 所有 worker 进程的 listenfd 会在 新连接 到来时变得 可读 ，为保证只有一个进程处理该连接，所有 worker 进程在注册 listenfd 读事件前抢占 accept_mutex。   
&emsp; 抢到 互斥锁 的那个进程注册 listenfd 读事件 ，在读事件里调用 accept 接受该连接。  
&emsp; 当一个 worker 进程在 accept 这个连接之后，就开始读取请求 ， 解析请求 ， 处理请求，产生数据后，再返回给客户端 ，最后才断开连接 ，一个完整的请求就是这样。  
&emsp; 可以看到，一个请求，完全由 worker 进程来处理，而且只在一个 worker 进程中处理。  
&emsp; 如下图所示：
![image](http://182.92.69.8:8081/img/Linux/Nginx/nginx-10.png)  
&emsp; 在 Nginx 服务器的运行过程中， 主进程 和 工作进程 需要进程交互。交互依赖于 Socket 实现的管道来实现。  

## 1.3. 基于异步及非阻塞的事件驱动模型  
&emsp; 基于 异步及非阻塞的事件驱动模型 ，可以说是 Nginx 得以获得高并发、高性能的关键因素，同时也得益于对Linux、Solaris及类BSD等操作系统内核中 事件通知及I/O性能增强功能的采用，如kqueue、epoll及event ports。  

### 1.3.1. 异步非阻塞机制  
&emsp; <font color = "red">每个工作进程使用异步非阻塞方式，可以处理多个客户端请求。</font>  
&emsp; 当某个工作进程接收到客户端的请求以后，调用IO进行处理，如果不能立即得到结果，就去处理其他请求 (即为非阻塞 )，而客户端在此期间也无需等待响应，可以去处理其他事情（即为异步）。  
&emsp; 当 IO 返回时，就会通知此工作进程，该进程得到通知，暂时挂起当前处理的事务去响应客户端请求 。  

### 1.3.2. Nginx事件驱动模型  
&emsp; 在Nginx的异步非阻塞机制中，工作进程在调用IO后，就去处理其他的请求，当IO调用返回后，会通知该工作进程。  
&emsp; 对于这样的系统调用，主要使用Nginx服务器的事件驱动模型来实现。  
![image](http://182.92.69.8:8081/img/Linux/Nginx/nginx-9.png)   
&emsp; 如上图所示，<font color = "red">Nginx的事件驱动模型由事件收集器、事件发送器和事件处理器三部分基本单元组成。</font>  

* 事件收集器：负责收集 worker 进程的各种 IO 请求；  
* 事件发送器：负责将 IO 事件发送到 事件处理器 ；  
* 事件处理器：负责各种事件的 响应工作 。  

&emsp; 事件发送器将每个请求放入一个 待处理事件列表 ，使用非阻塞 I/O 方式调用 事件处理器来处理该请求。事件处理器的处理方式称为 “多路 IO 复用方法” ，常见的包括以下三种：select 模型、 poll模型、 epoll 模型。  

