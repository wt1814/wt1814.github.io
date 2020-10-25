
<!-- TOC -->

- [1. netty核心概念](#1-netty核心概念)
    - [1.1. 零拷贝](#11-零拷贝)
        - [为什么要有 DMA 技术?](#为什么要有-dma-技术)
        - [传统的文件传输有多糟糕？](#传统的文件传输有多糟糕)
        - [如何优化文件传输的性能？](#如何优化文件传输的性能)
        - [如何实现零拷贝？](#如何实现零拷贝)
            - [mmap + write](#mmap--write)
            - [sendfile](#sendfile)
    - [1.2. Reactor线程模型](#12-reactor线程模型)
        - [1.2.1. Reactor线程模型](#121-reactor线程模型)
            - [1.2.1.1. 单线程模型](#1211-单线程模型)
            - [1.2.1.2. 多线程模型](#1212-多线程模型)
            - [1.2.1.3. 主从多线程模型](#1213-主从多线程模型)
        - [1.2.2. Netty中的线程模型与Reactor的联系](#122-netty中的线程模型与reactor的联系)
            - [1.2.2.1. 单线程模型](#1221-单线程模型)
            - [1.2.2.2. 多线程模型](#1222-多线程模型)
            - [1.2.2.3. 主从多线程模型 (最常使用)](#1223-主从多线程模型-最常使用)

<!-- /TOC -->

# 1. netty核心概念  

<!-- 
你要的Netty常见面试题总结，敖丙搞来了！
https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA
-->

&emsp; 为什么要用 Netty？  
&emsp; Netty是由JBoss开发，基于Java NIO的一个高性能通信框架。之前几篇文章介绍了Java NIO的一些基本的概念和API。但在实际的网络开发中，其实很少使用Java NIO原生的API。主要有以下原因：  

* 原生API使用单线程模型，不能很好利用多核优势；  
* 原生API是直接使用的IO数据，没有做任何封装处理，对数据的编解码、TCP的粘包和拆包、客户端断连、网络的可靠性和安全性方面没有做处理；  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-18.png)  

<!-- 

因为 Netty 具有下面这些优点，并且相比于直接使用 JDK 自带的 NIO 相关的 API 来说更加易用。

    统一的 API，支持多种传输类型，阻塞和非阻塞的。
    简单而强大的线程模型。
    自带编解码器解决 TCP 粘包/拆包问题。
    自带各种协议栈。
    真正的无连接数据包套接字支持。
    比直接使用 Java 核心 API 有更高的吞吐量、更低的延迟、更低的资源消耗和更少的内存复制。
    安全性不错，有完整的 SSL/TLS 以及 StartTLS 支持。
    社区活跃
    成熟稳定，经历了大型项目的使用和考验，而且很多开源项目都使用到了 Netty， 比如我们经常接触的 Dubbo、RocketMQ 等等。
    ......
-->



&emsp; Netty高性能  

* 异步非阻塞通信  
* 零拷贝  
* 内存池
* 高效的Reactor线程模型  
* 无锁化的串行设计理念  
* 高效的并发编程  
* 对高性能对的序列化框架支持
* 灵活的TCP参数配置能力


## 1.1. 零拷贝  
<!-- 

原来 8 张图，就可以搞懂「零拷贝」了
https://mp.weixin.qq.com/s/P0IP6c_qFhuebwdwD8HM7w
-->

### 为什么要有 DMA 技术?  
&emsp; 在没有 DMA 技术前，I/O 的过程是这样的：  

* CPU 发出对应的指令给磁盘控制器，然后返回；  
* 磁盘控制器收到指令后，于是就开始准备数据，会把数据放入到磁盘控制器的内部缓冲区中，然后产生一个中断；  
* CPU 收到中断信号后，停下手头的工作，接着把磁盘控制器的缓冲区的数据一次一个字节地读进自己的寄存器，然后再把寄存器里的数据写入到内存，而在数据传输的期间 CPU 是无法执行其他任务的。  

&emsp; 流程图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-19.png)  
&emsp; 可以看到，整个数据的传输过程，都要需要 CPU 亲自参与搬运数据的过程，而且这个过程，CPU 是不能做其他事情的。  
&emsp; 简单的搬运几个字符数据那没问题，但是如果我们用千兆网卡或者硬盘传输大量数据的时候，都用 CPU 来搬运的话，肯定忙不过来。  
&emsp; 计算机科学家们发现了事情的严重性后，于是就发明了 DMA 技术，也就是直接内存访问（Direct Memory Access） 技术。  
&emsp; 什么是 DMA 技术？简单理解就是，在进行 I/O 设备和内存的数据传输的时候，数据搬运的工作全部交给 DMA 控制器，而 CPU 不再参与任何与数据搬运相关的事情，这样 CPU 就可以去处理别的事务。  

使用 DMA 控制器进行数据传输的过程如下图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-20.png)  

&emsp; 具体过程：

* 用户进程调用 read 方法，向操作系统发出 I/O 请求，请求读取数据到自己的内存缓冲区中，进程进入阻塞状态；  
* 操作系统收到请求后，进一步将 I/O 请求发送 DMA，然后让 CPU 执行其他任务；
* DMA 进一步将 I/O 请求发送给磁盘；
* 磁盘收到 DMA 的 I/O 请求，把数据从磁盘读取到磁盘控制器的缓冲区中，当磁盘控制器的缓冲区被读满后，向 DMA 发起中断信号，告知自己缓冲区已满；
* DMA 收到磁盘的信号，将磁盘控制器缓冲区中的数据拷贝到内核缓冲区中，此时不占用 CPU，CPU 可以执行其他任务；
* 当 DMA 读取了足够多的数据，就会发送中断信号给 CPU；
* CPU 收到 DMA 的信号，知道数据已经准备好，于是将数据从内核拷贝到用户空间，系统调用返回；

&emsp; 可以看到， 整个数据传输的过程，CPU 不再参与数据搬运的工作，而是全程由 DMA 完成，但是 CPU 在这个过程中也是必不可少的，因为传输什么数据，从哪里传输到哪里，都需要 CPU 来告诉 DMA 控制器。  
&emsp; 早期 DMA 只存在在主板上，如今由于 I/O 设备越来越多，数据传输的需求也不尽相同，所以每个 I/O 设备里面都有自己的 DMA 控制器。  

### 传统的文件传输有多糟糕？  
&emsp; 如果服务端要提供文件传输的功能，我们能想到的最简单的方式是：将磁盘上的文件读取出来，然后通过网络协议发送给客户端。  
&emsp; 传统 I/O 的工作方式是，数据读取和写入是从用户空间到内核空间来回复制，而内核空间的数据是通过操作系统层面的 I/O 接口从磁盘读取或写入。  
&emsp; 代码通常如下，一般会需要两个系统调用：  

```text
read(file, tmp_buf, len);
write(socket, tmp_buf, len);
```
&emsp; 代码很简单，虽然就两行代码，但是这里面发生了不少的事情。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-21.png)  

&emsp; 首先，期间共发生了 4 次用户态与内核态的上下文切换，因为发生了两次系统调用，一次是 read() ，一次是 write()，每次系统调用都得先从用户态切换到内核态，等内核完成任务后，再从内核态切换回用户态。

&emsp; 上下文切换到成本并不小，一次切换需要耗时几十纳秒到几微秒，虽然时间看上去很短，但是在高并发的场景下，这类时间容易被累积和放大，从而影响系统的性能。

&emsp; 其次，还发生了 4 次数据拷贝，其中两次是 DMA 的拷贝，另外两次则是通过 CPU 拷贝的，下面说一下这个过程：  

* 第一次拷贝，把磁盘上的数据拷贝到操作系统内核的缓冲区里，这个拷贝的过程是通过 DMA 搬运的。
* 第二次拷贝，把内核缓冲区的数据拷贝到用户的缓冲区里，于是我们应用程序就可以使用这部分数据了，这个拷贝到过程是由 CPU 完成的。
* 第三次拷贝，把刚才拷贝到用户的缓冲区里的数据，再拷贝到内核的 socket 的缓冲区里，这个过程依然还是由 CPU 搬运的。
* 第四次拷贝，把内核的 socket 缓冲区里的数据，拷贝到网卡的缓冲区里，这个过程又是由 DMA 搬运的。

&emsp; 回过头看这个文件传输的过程，我们只是搬运一份数据，结果却搬运了 4 次，过多的数据拷贝无疑会消耗 CPU 资源，大大降低了系统性能。  
&emsp; 这种简单又传统的文件传输方式，存在冗余的上文切换和数据拷贝，在高并发系统里是非常糟糕的，多了很多不必要的开销，会严重影响系统性能。  
&emsp; 所以，要想提高文件传输的性能，就需要减少「用户态与内核态的上下文切换」和「内存拷贝」的次数。  

### 如何优化文件传输的性能？  
&emsp; **先来看看，如何减少「用户态与内核态的上下文切换」的次数呢？**  
&emsp; 读取磁盘数据的时候，之所以要发生上下文切换，这是因为用户空间没有权限操作磁盘或网卡，内核的权限最高，这些操作设备的过程都需要交由操作系统内核来完成，所以一般要通过内核去完成某些任务的时候，就需要使用操作系统提供的系统调用函数。  
&emsp; 而一次系统调用必然会发生 2 次上下文切换：首先从用户态切换到内核态，当内核执行完任务后，再切换回用户态交由进程代码执行。  
&emsp; 所以，要想减少上下文切换到次数，就要减少系统调用的次数。  

&emsp; **再来看看，如何减少「数据拷贝」的次数？**  
&emsp; 在前面已经知道了，传统的文件传输方式会历经 4 次数据拷贝，而且这里面，「从内核的读缓冲区拷贝到用户的缓冲区里，再从用户的缓冲区里拷贝到 socket 的缓冲区里」，这个过程是没有必要的。  
&emsp; 因为文件传输的应用场景中，在用户空间我们并不会对数据「再加工」，所以数据实际上可以不用搬运到用户空间，因此用户的缓冲区是没有必要存在的。  

### 如何实现零拷贝？  
&emsp; 零拷贝技术实现的方式通常有 2 种：  

* mmap + write
* sendfile

&emsp; 下面就谈一谈，它们是如何减少「上下文切换」和「数据拷贝」的次数。  

#### mmap + write  
&emsp; 在前面已经知道，read() 系统调用的过程中会把内核缓冲区的数据拷贝到用户的缓冲区里，于是为了减少这一步开销，我们可以用 mmap() 替换 read() 系统调用函数。  

```text
buf = mmap(file, len);
write(sockfd, buf, len);
```
&emsp; mmap() 系统调用函数会直接把内核缓冲区里的数据「映射」到用户空间，这样，操作系统内核与用户空间就不需要再进行任何的数据拷贝操作。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-22.png)  
&emsp; 具体过程如下：  

* 应用进程调用了 mmap() 后，DMA 会把磁盘的数据拷贝到内核的缓冲区里。接着，应用进程跟操作系统内核「共享」这个缓冲区；
* 应用进程再调用 write()，操作系统直接将内核缓冲区的数据拷贝到 socket 缓冲区中，这一切都发生在内核态，由 CPU 来搬运数据；
* 最后，把内核的 socket 缓冲区里的数据，拷贝到网卡的缓冲区里，这个过程是由 DMA 搬运的。

&emsp; 我们可以得知，通过使用 mmap() 来代替 read()， 可以减少一次数据拷贝的过程。  
&emsp; 但这还不是最理想的零拷贝，因为仍然需要通过 CPU 把内核缓冲区的数据拷贝到 socket 缓冲区里，而且仍然需要 4 次上下文切换，因为系统调用还是 2 次。  

#### sendfile  
&emsp; 在 Linux 内核版本 2.1 中，提供了一个专门发送文件的系统调用函数 sendfile()，函数形式如下：

```text
#include <sys/socket.h>
ssize_t sendfile(int out_fd, int in_fd, off_t *offset, size_t count);
```
&emsp; 它的前两个参数分别是目的端和源端的文件描述符，后面两个参数是源端的偏移量和复制数据的长度，返回值是实际复制数据的长度。  
&emsp; 首先，它可以替代前面的 read() 和 write() 这两个系统调用，这样就可以减少一次系统调用，也就减少了 2 次上下文切换的开销。  
&emsp; 其次，该系统调用，可以直接把内核缓冲区里的数据拷贝到 socket 缓冲区里，不再拷贝到用户态，这样就只有 2 次上下文切换，和 3 次数据拷贝。如下图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-23.png)  
&emsp; 但是这还不是真正的零拷贝技术，如果网卡支持 SG-DMA（The Scatter-Gather Direct Memory Access）技术（和普通的 DMA 有所不同），我们可以进一步减少通过 CPU 把内核缓冲区里的数据拷贝到 socket 缓冲区的过程。  
&emsp; 你可以在你的 Linux 系统通过下面这个命令，查看网卡是否支持 scatter-gather 特性：  

```text
$ ethtool -k eth0 | grep scatter-gather
scatter-gather: on
```

&emsp; 于是，从 Linux 内核 2.4 版本开始起，对于支持网卡支持 SG-DMA 技术的情况下， sendfile() 系统调用的过程发生了点变化，具体过程如下：  

* 第一步，通过 DMA 将磁盘上的数据拷贝到内核缓冲区里；
* 第二步，缓冲区描述符和数据长度传到 socket 缓冲区，这样网卡的 SG-DMA 控制器就可以直接将内核缓存中的数据拷贝到网卡的缓冲区里，此过程不需要将数据从操作系统内核缓冲区拷贝到 socket 缓冲区中，这样就减少了一次数据拷贝；

&emsp; 所以，这个过程之中，只进行了 2 次数据拷贝，如下图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-24.png)  

&emsp; 这就是所谓的零拷贝（Zero-copy）技术，因为我们没有在内存层面去拷贝数据，也就是说全程没有通过 CPU 来搬运数据，所有的数据都是通过 DMA 来进行传输的。  
&emsp; 零拷贝技术的文件传输方式相比传统文件传输的方式，减少了 2 次上下文切换和数据拷贝次数，只需要 2 次上下文切换和数据拷贝次数，就可以完成文件的传输，而且 2 次的数据拷贝过程，都不需要通过 CPU，2 次都是由 DMA 来搬运。  
&emsp; 所以，总体来看，零拷贝技术可以把文件传输的性能提高至少一倍以上。  

## 1.2. Reactor线程模型  

<!--
说说Netty的线程模型 
https://mp.weixin.qq.com/s?__biz=MzAxNjM2MTk0Ng==&mid=2247488256&idx=3&sn=253eb6ba1f500d545bd8c836adaf1980&chksm=9bf4a3b5ac832aa3bb05595fac709334dd318698e577fa00b16d696a0fe235d3dee24cee3c75&mpshare=1&scene=1&srcid=&sharer_sharetime=1566173423019&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=5ead8116cc3d877610998f2c6fdc157f31c27badb458427f3cab67f312240f562e06a1819f6ac147c195e43f2d840d672dd0cf1f80fdb1dac6e8bd0157492bfe8b87c145bb2fe49422115139efca9e03&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=dj0rerrTmP1viAq%2FqHfGf12HB9AUM6AWfIt3Bw3twmsR0CedhQsJ3IHhoWnQJOqn

-->

&emsp; 大部分网络框架都是基于 Reactor 模式设计开发的。  

### 1.2.1. Reactor线程模型  
&emsp; Reactor模式是基于事件驱动开发的，核心组成部分包括Reactor和线程池，其中Reactor负责监听和分配事件，线程池负责处理事件，而根据Reactor的数量和线程池的数量，又将Reactor分为三种模型:

* 单线程模型 (单Reactor单线程)  
* 多线程模型 (单Reactor多线程)  
* 主从多线程模型 (多Reactor多线程)  

#### 1.2.1.1. 单线程模型  
&emsp; 一个线程需要执行处理所有的 accept、read、decode、process、encode、send 事件。对于高负载、高并发，并且对性能要求比较高的场景不适用。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-10.png)  

* Reactor内部通过selector 监控连接事件，收到事件后通过dispatch进行分发，如果是连接建立的事件，则由Acceptor处理，Acceptor通过accept接受连接，并创建一个Handler来处理连接后续的各种事件,如果是读写事件，直接调用连接对应的Handler来处理。  
* Handler完成read->(decode->compute->encode)->send的业务流程。  
* 这种模型好处是简单，坏处却很明显，当某个Handler阻塞时，会导致其他客户端的handler和accpetor都得不到执行，无法做到高性能，只适用于业务处理非常快速的场景。  

#### 1.2.1.2. 多线程模型
&emsp; 一个 Acceptor 线程只负责监听客户端的连接，一个 NIO 线程池负责具体处理：accept、read、decode、process、encode、send 事件。满足绝大部分应用场景，并发连接量不大的时候没啥问题，但是遇到并发连接大的时候就可能会出现问题，成为性能瓶颈。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-11.png)  

* 主线程中，Reactor对象通过selector监控连接事件,收到事件后通过dispatch进行分发，如果是连接建立事件，则由Acceptor处理，Acceptor通过accept接收连接，并创建一个Handler来处理后续事件，而Handler只负责响应事件，不进行业务操作，也就是只进行read读取数据和write写出数据，业务处理交给一个线程池进行处理
* 线程池分配一个线程完成真正的业务处理，然后将响应结果交给主进程的Handler处理，Handler将结果send给client (下面是核心代码)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-13.png)  

&emsp; 单Reactor承当所有事件的监听和响应,而当我们的服务端遇到大量的客户端同时进行连接，或者在请求连接时执行一些耗时操作，比如身份认证，权限检查等，这种瞬时的高并发就容易成为性能瓶颈  

#### 1.2.1.3. 主从多线程模型  
&emsp; 从一个 主线程 NIO 线程池中选择一个线程作为 Acceptor 线程，绑定监听端口，接收客户端连接的连接，其他线程负责后续的接入认证等工作。连接建立完成后，Sub NIO 线程池负责具体处理 I/O 读写。如果多线程模型无法满足你的需求的时候，可以考虑使用主从多线程模型 。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-12.png)  

* 存在多个Reactor，每个Reactor都有自己的selector选择器，线程和dispatch
* 主线程中的mainReactor通过自己的selector监控连接建立事件，收到事件后通过Accpetor接收，将新的连接分配给某个子线程
* 子线程中的subReactor将mainReactor分配的连接加入连接队列中通过自己的selector进行监听，并创建一个Handler用于处理后续事件
* Handler完成read->业务处理->send的完整业务流程
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-14.png)  

### 1.2.2. Netty中的线程模型与Reactor的联系  
<!-- 
https://mp.weixin.qq.com/s/eJ-dAtOYsxylGL7pBv7VVA

 Netty运用Reactor模式到极致 
 https://mp.weixin.qq.com/s/rqzzHAhntBJpEHpzz1o5HA
-->
&emsp; Netty主要靠NioEventLoopGroup线程池来实现具体的线程模型的。  

#### 1.2.2.1. 单线程模型  
&emsp; 单线程模型就是只指定一个线程执行客户端连接和读写操作，也就是在一个Reactor中完成，对应在Netty中的实现就是将NioEventLoopGroup线程数设置为1，核心代码是：  

```java
 NioEventLoopGroup group = new NioEventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ServerHandlerInitializer());
```
&emsp; 它的工作流程大致如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-15.png)  
&emsp; 上述单线程模型就对应了Reactor的单线程模型  

#### 1.2.2.2. 多线程模型  
&emsp; 多线程模型就是在一个单Reactor中进行客户端连接处理，然后业务处理交给线程池，核心代码如下：  

```java
NioEventLoopGroup eventGroup = new NioEventLoopGroup();
ServerBootstrap bootstrap = new ServerBootstrap();
bootstrap.group(eventGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.SO_BACKLOG, 1024)
        .childHandler(new ServerHandlerInitializer());
```
&emsp; 走进group方法可以发现我们平时设置的bossGroup和workerGroup就是使用了同一个group  

```java
@Override
public ServerBootstrap group(EventLoopGroup group) {
    return group(group, group);
}
```
&emsp; 工作流程如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-16.png)  

#### 1.2.2.3. 主从多线程模型 (最常使用)
&emsp; 主从多线程模型是有多个Reactor，也就是存在多个selector，所以我们定义一个bossGroup和一个workGroup，核心代码如下：  

```java
// 1.bossGroup 用于接收连接，workerGroup 用于具体的处理
NioEventLoopGroup bossGroup = new NioEventLoopGroup();
NioEventLoopGroup workerGroup = new NioEventLoopGroup();
//2.创建服务端启动引导/辅助类：ServerBootstrap
ServerBootstrap bootstrap = new ServerBootstrap();
//3.给引导类配置两大线程组,确定了线程模型
bootstrap.group(bossGroup,workerGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.SO_BACKLOG, 1024)
        .childHandler(new ServerHandlerInitializer());
```
&emsp; 工作流程如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-17.png)  
&emsp; **注意：其实在Netty中，bossGroup线程池最终还是只会随机选择一个线程用于处理客户端连接，与此同时，NioServerSocetChannel绑定到bossGroup的线程中，NioSocketChannel绑定到workGroup的线程中**  



