
<!-- TOC -->

- [1. 网络IO、Netty、WebSocket](#1-网络ionettywebsocket)
    - [1.1. ~~服务器处理连接~~](#11-服务器处理连接)
    - [1.2. 通信基础](#12-通信基础)
    - [1.3. 网络IO](#13-网络io)
        - [1.3.1. 五种I/O模型](#131-五种io模型)
        - [1.3.2. I/O多路复用详解](#132-io多路复用详解)
        - [1.3.3. 多路复用之Reactor模式](#133-多路复用之reactor模式)
        - [1.3.4. IO性能优化之零拷贝](#134-io性能优化之零拷贝)
    - [1.4. Socket编程](#14-socket编程)
    - [1.5. NIO](#15-nio)
    - [1.6. Netty](#16-netty)
        - [1.6.1. Netty简介](#161-netty简介)
        - [1.6.2. Netty运行流程](#162-netty运行流程)
        - [1.6.3. Netty核心组件](#163-netty核心组件)
        - [1.6.4. Netty逻辑架构](#164-netty逻辑架构)
        - [1.6.5. Netty高性能](#165-netty高性能)
            - [1.6.5.1. Netty的Reactor线程模型](#1651-netty的reactor线程模型)
        - [1.6.6. Netty开发](#166-netty开发)
            - [1.6.6.1. Netty应用场景，](#1661-netty应用场景)
            - [1.6.6.2. TCP粘拆包与Netty编解码](#1662-tcp粘拆包与netty编解码)
            - [1.6.6.3. Netty实战](#1663-netty实战)
            - [1.6.6.4. Netty多协议开发](#1664-netty多协议开发)
        - [1.6.7. Netty源码](#167-netty源码)
    - [1.7. WebSocket](#17-websocket)
        - [1.7.1. 4种Web端即时通信](#171-4种web端即时通信)
        - [1.7.2. 配置中心使用长轮询推送](#172-配置中心使用长轮询推送)
        - [1.7.3. WebSocket](#173-websocket)

<!-- /TOC -->



# 1. 网络IO、Netty、WebSocket


## 1.1. ~~服务器处理连接~~
&emsp; 多线程模式和多进程模式是类似的，也是分为下面几种：  

1. 主进程accept，创建子线程处理
2. 子线程accept
3. 线程池


## 1.2. 通信基础
&emsp; 1. 序列化。  

## 1.3. 网络IO
&emsp; 前言：`一次网络I/O事件，包含网络连接（IO多路复用） -> 读写数据（零拷贝） -> 处理事件。`  

### 1.3.1. 五种I/O模型
1. **<font color = "red">网络IO的本质就是socket流的读取，通常一次IO读操作会涉及到两个对象和两个阶段。**</font>  
    * **<font color = "clime">两个对象：用户进程（线程）、内核对象（内核态和用户态）。</font><font color = "blue">用户进程请求内核。</font>**   
    * **<font color = "clime">`内核中涉及两个阶段：1. 等待数据准备；2. 数据从内核空间拷贝到用户空间。`</font>**  
    &emsp; `⚠️基于以上两个阶段就产生了五种不同的IO模式，`分别是：阻塞I/O模型、非阻塞I/O模型、多路复用I/O模型、信号驱动I/O模型、异步I/O模型。其中，前四种被称为同步I/O。  
2. **<font color = "blue">同步（等待结果）和阻塞（线程）：</font>**  
    * 异步和同步：对于请求结果的获取是客户端主动获取结果，还是由服务端来通知结果。    
    * 阻塞和非阻塞：在等待这个函数返回结果之前，当前的线程是处于挂起状态还是运行状态。 
3. 同步阻塞I/O：  
    1. 流程：  
        ![image](http://182.92.69.8:8081/img/microService/netty/netty-1.png)  
        1. 用户进程发起recvfrom系统调用内核。用户进程【同步】等待结果；
        2. 内核等待I/O数据返回，此时用户进程处于【阻塞】，一直等待内核返回；
        3. I/O数据返回后，内核将数据从内核空间拷贝到用户空间；  
        4. 内核将数据返回给用户进程。  
    特点：两阶段都阻塞。  
    2. BIO采用多线程时，大量的线程占用很大的内存空间，并且线程切换会带来很大的开销，10000个线程真正发生读写事件的线程数不会超过20%，`每次accept都开一个线程也是一种资源浪费。`  
4. 同步 非阻塞（轮询）I/O：  
    1. 流程：  
        ![image](http://182.92.69.8:8081/img/microService/netty/netty-2.png)  
        1. 用户进程发起recvfrom系统调用内核。用户进程【同步】等待结果；
        2. 内核等待I/O数据返回。无I/O数据返回时，内核返回给用户进程ewouldblock结果。`【`非阻塞`】用户进程，立马返回结果。`但 **<font color = "clime">用户进程要`【主动轮询】`查询结果。</font>**  
        3. I/O数据返回后，内核将数据从内核空间拷贝到用户空间；  
        4. 内核将数据返回给用户进程。  
    特点：`第一阶段不阻塞但要轮询，`第二阶段阻塞。  
    2. NIO`每次轮询所有fd（包括没有发生读写事件的fd）会很浪费cpu。`  
5. 多路复用I/O：（~~同步阻塞，又基于回调通知~~）  
    1. 为什么有多路复用？  
    &emsp; 如果一个I/O流进来，就开启一个进程处理这个I/O流。那么假设现在有一百万个I/O流进来，那就需要开启一百万个进程一一对应处理这些I/O流（这就是传统意义下的多进程并发处理）。思考一下，一百万个进程，CPU占有率会多高，这个实现方式及其的不合理。所以人们提出了 **<font color = "red">I/O多路复用这个模型，一个线程，通过记录I/O流的状态来同时管理多个I/O，可以提高服务器的吞吐能力。</font>**  
    &emsp; `多路是指多个socket套接字，复用是指复用同一个进程。`  
    2. 流程：  
        ![image](http://182.92.69.8:8081/img/microService/netty/netty-3.png)  
        1. 用户多进程或多线程发起select系统调用，复用器Selector会监听注册进来的进程事件。用户进程【同步】等待结果；
        2. 内核等待I/O数据返回，无数据返回时，进程【阻塞】于select调用；
        2. I/O数据返回后，内核将数据从内核空间拷贝到用户空间， **<font color = "clime">Selector`通知`哪个进程哪个事件；</font>**  
        4. 进程发起recvfrom系统调用。  
    3. 多路复用`能支持更多的并发连接请求。`  
    &emsp; 多路复用I/O模型和阻塞I/O模型并没有太大的不同，事实上，还更差一些，因为它需要使用两个系统调用(select和recvfrom)，而阻塞I/O模型只有一次系统调用(recvfrom)。但是Selector的优势在于它可以同时处理多个连接。   
6. 信号驱动IO  
7. 异步IO

### 1.3.2. I/O多路复用详解
1. **<font color = "clime">`~~select,poll,epoll只是I/O多路复用模型中第一阶段，即获取网络数据、用户态和内核态之间的拷贝。~~`</font>** 此阶段会阻塞线程。  
2. **select()：**  
    1. **select运行流程：**  
        &emsp; **<font color = "red">select()运行时会`将fd_set（文件句柄集合）从用户态拷贝到内核态`。</font>** 在内核态中线性扫描socket，即采用轮询。如果有事件返回，会将内核态的数组相应的FD置位。最后再将内核态的数据返回用户态。  
    2. **select机制的问题：（拷贝、两次轮询、FD置位）**  
        * 为了减少数据拷贝带来的性能损坏，内核对被监控的fd_set集合大小做了限制，并且这个是通过宏控制的，大小不可改变（限制为1024）。  
        * 每次调用select， **<font color = "red">1)需要把fd_set集合从用户态拷贝到内核态，</font>** **<font color = "clime">2)需要在内核遍历传递进来的所有fd_set（对socket进行扫描时是线性扫描，即采用轮询的方法，效率较低），</font>** **<font color = "red">3)如果有数据返回还需要从内核态拷贝到用户态。</font>** 如果fd_set集合很大时，开销比较大。 
        * 由于运行时，需要将FD置位，导致fd_set集合不可重用。  
        * **<font color = "clime">select()函数返回后，</font>** 调用函数并不知道是哪几个流（可能有一个，多个，甚至全部）， **<font color = "clime">还得再次遍历fd_set集合处理数据，即采用`无差别轮询`。</font>**   
        * ~~惊群~~   
3. **poll()：** 运行机制与select()相似。将fd_set数组改为采用链表方式pollfds，没有连接数的限制，并且pollfds可重用。   
4. **epoll()：**   
    1. **epoll的三个函数：**  
        <!-- 
        * 调用epoll_create，会在内核cache里建个红黑树，同时也会再建立一个rdllist双向链表。 
        * epoll_ctl将被监听的描述符添加到红黑树或从红黑树中删除或者对监听事件进行修改。
        * 双向链表，用于存储准备就绪的事件，当epoll_wait调用时，仅查看这个rdllist双向链表数据即可。epoll_wait阻塞等待注册的事件发生，返回事件的数目，并将触发的事件写入events数组中。   
        -->
        ![image](http://182.92.69.8:8081/img/microService/netty/netty-119.png)  
        1. 执行epoll_create()时，创建了红黑树和就绪链表（rdllist双向链表）；
        2. 执行epoll_ctl()时，如果增加socket句柄，则检查在红黑树中是否存在，存在立即返回，不存在则添加到树干上，然后向内核注册回调函数，用于当中断事件来临时向准备就绪链表中插入数据；
        3. 执行epoll_wait()时立刻返回准备就绪链表里的数据即可。  
        
        -----------
        1. 首先epoll_create创建一个epoll文件描述符，底层同时创建一个红黑树，和一个就绪链表；红黑树存储所监控的文件描述符的节点数据，就绪链表存储就绪的文件描述符的节点数据；  
        2. epoll_ctl将会添加新的描述符，首先判断是红黑树上是否有此文件描述符节点，如果有，则立即返回。如果没有， 则在树干上插入新的节点，并且告知内核注册回调函数。`【当接收到某个文件描述符过来数据时，那么内核将该节点插入到就绪链表里面。】`  
        3. epoll_wait将会接收到消息，并且将数据拷贝到用户空间，清空链表。对于LT模式epoll_wait清空就绪链表之后会检查该文件描述符是哪一种模式，`如果为LT模式，且必须该节点确实有事件未处理，那么就会把该节点重新放入到刚刚删除掉的且刚准备好的就绪链表，`epoll_wait马上返回。ET模式不会检查，只会调用一次。  
    2. **epoll机制的工作模式：**  
        * LT模式（默认，水平触发，level trigger）：当epoll_wait检测到某描述符事件就绪并通知应用程序时，应用程序可以不立即处理该事件； **<font color = "clime">下次调用epoll_wait时，会再次响应应用程序并通知此事件。</font>**    
        * ET模式（边缘触发，edge trigger）：当epoll_wait检测到某描述符事件就绪并通知应用程序时，应用程序必须立即处理该事件。如果不处理，下次调用epoll_wait时，不会再次响应应用程序并通知此事件。（直到做了某些操作导致该描述符变成未就绪状态了，也就是说 **<font color = "clime">边缘触发只在状态由未就绪变为就绪时只通知一次。</font>** ）   

        &emsp; 由此可见：ET模式的效率比LT模式的效率要高很多。只是如果使用ET模式，就要保证每次进行数据处理时，要将其处理完，不能造成数据丢失，这样对编写代码的人要求就比较高。  
        &emsp; 注意：ET模式只支持非阻塞的读写：为了保证数据的完整性。  

    3. **epoll机制的优点：**  
        * 调用epoll_ctl时拷贝进内核并保存，之后每次epoll_wait不拷贝。  
        * epoll()函数返回后，调用函数以O(1)复杂度遍历。  
5. 两种IO多路复用模式：[Reactor和Proactor](/docs/microService/communication/Netty/Reactor.md)  

### 1.3.3. 多路复用之Reactor模式
1. `Reactor，是网络编程中基于IO多路复用的一种设计模式，是【event-driven architecture】的一种实现方式，处理多个客户端并发的向服务端请求服务的场景。`    
2. **<font color = "red">Reactor模式核心组成部分包括Reactor线程和worker线程池，</font> <font color = "clime">而根据Reactor的数量和线程池的数量，又将Reactor分为三种模型。</font>**  
    * `Reactor负责监听（建立连接）和处理请求（分发事件、读写）。`  
    * 线程池负责处理事件。  
3. **单线程模型（单Reactor单线程）**  
![image](http://182.92.69.8:8081/img/microService/netty/netty-91.png)  
&emsp; ~~这是最基本的单Reactor单线程模型。其中Reactor线程，负责多路分离套接字，有新连接到来触发connect事件之后，交由Acceptor进行处理，有IO读写事件之后交给hanlder处理。~~  
&emsp; ~~Acceptor主要任务就是构建handler，在获取到和client相关的SocketChannel之后，绑定到相应的hanlder上，对应的SocketChannel有读写事件之后，基于racotor分发，hanlder就可以处理了（所有的IO事件都绑定到selector上，有Reactor分发）。~~  
&emsp; **<font color = "red">Reactor单线程模型，指的是所有的IO操作都在同一个NIO线程上面完成。</font>** 单个NIO线程会成为系统瓶颈，并且会有节点故障问题。   
4. **多线程模型（单Reactor多线程）**  
![image](http://182.92.69.8:8081/img/microService/netty/netty-92.png)  
&emsp; ~~相对于第一种单线程的模式来说，在处理业务逻辑，也就是获取到IO的读写事件之后，交由线程池来处理，这样可以减小主reactor的性能开销，从而更专注的做事件分发工作了，从而提升整个应用的吞吐。~~  
&emsp; Rector多线程模型与单线程模型最大的区别就是有一组NIO线程处理IO操作。在极个别特殊场景中，一个NIO线程（Acceptor线程）负责监听和处理所有的客户端连接可能会存在性能问题。    
5. **主从多线程模型（多Reactor多线程）**    
![image](http://182.92.69.8:8081/img/microService/netty/netty-93.png)  
&emsp; 主从Reactor多线程模型中，Reactor线程拆分为mainReactor和subReactor两个部分， **<font color = "clime">`mainReactor只处理连接事件`，`读写事件交给subReactor来处理`。</font>** 业务逻辑还是由线程池来处理。  
&emsp; mainRactor只处理连接事件，用一个线程来处理就好。处理读写事件的subReactor个数一般和CPU数量相等，一个subReactor对应一个线程。  
&emsp; ~~第三种模型比起第二种模型，是将Reactor分成两部分：~~  
&emsp; ~~mainReactor负责监听server socket，用来处理新连接的建立，将建立的socketChannel指定注册给subReactor。~~  
&emsp; ~~subReactor维护自己的selector，基于mainReactor注册的socketChannel多路分离IO读写事件，读写网络数据，对业务处理的功能，另其扔给worker线程池来完成。~~  
&emsp; Reactor主从多线程模型中，一个连接accept专门用一个线程处理。  
&emsp; 主从Reactor线程模型的特点是：服务端用于接收客户端连接的不再是1个单独的NIO线程，而是一个独立的NIO线程池。Acceptor接收到客户端TCP连接请求处理完成后（可能包含接入认证等），将新创建的SocketChannel注册到IO线程池（sub reactor线程池）的某个IO线程上，由它负责SocketChannel的读写和编解码工作。Acceptor线程池仅仅只用于客户端的登陆、握手和安全认证，一旦链路建立成功，就将链路注册到后端subReactor线程池的IO线程上，由IO线程负责后续的IO操作。  
&emsp; 利用主从NIO线程模型，可以解决1个服务端监听线程无法有效处理所有客户端连接的性能不足问题。  

### 1.3.4. IO性能优化之零拷贝
1. 比较常见的I/O流程是读取磁盘文件传输到网络中。  
2. **<font color = "clime">I/O传输中的一些基本概念：</font>**  
    * 状态切换：内核态和用户态之间的切换。  
    * CPU拷贝：内核态和用户态之间的复制。 **零拷贝："零"更多的是指在用户态和内核态之间的复制是0次。**   
    * DMA拷贝：设备（或网络）和内核态之间的复制。  
3. 仅CPU方式：  
	![image](http://182.92.69.8:8081/img/microService/netty/netty-66.png)  
	&emsp; **仅CPU方式读数据read流程：**  

	* 当应用程序需要读取磁盘数据时，调用read()从用户态陷入内核态，read()这个系统调用最终由CPU来完成；
	* CPU向磁盘发起I/O请求，磁盘收到之后开始准备数据；
	* 磁盘将数据放到磁盘缓冲区之后，向CPU发起I/O中断，报告CPU数据已经Ready了；
	* CPU收到磁盘控制器的I/O中断之后，开始拷贝数据，完成之后read()返回，再从内核态切换到用户态；  

    `仅CPU方式有4次状态切换，4次CPU拷贝。`    

4. CPU & DMA（Direct Memory Access，直接内存访问）方式   
	![image](http://182.92.69.8:8081/img/microService/netty/netty-68.png)    
	&emsp; **<font color = "red">最主要的变化是，CPU不再和磁盘直接交互，而是DMA和磁盘交互并且将数据从磁盘缓冲区拷贝到内核缓冲区，因此减少了2次CPU拷贝。共2次CPU拷贝，2次DMA拷贝，4次状态切换。之后的过程类似。</font>**  

	* 读过程涉及2次空间切换（需要CPU参与）、1次DMA拷贝、1次CPU拷贝；
	* 写过程涉及2次空间切换、1次DMA拷贝、1次CPU拷贝；  
5. `零拷贝技术的几个实现手段包括：mmap+write、sendfile、sendfile+DMA收集、splice等。`  
6. **<font color = "blue">mmap（内存映射）：</font>**   
    &emsp; **<font color = "clime">mmap是Linux提供的一种内存映射文件的机制，它实现了将内核中读缓冲区地址与用户空间缓冲区地址进行映射，从而实现内核缓冲区与用户缓冲区的共享，</font>** 又减少了一次cpu拷贝。总共包含1次cpu拷贝，2次DMA拷贝，4次状态切换。此流程中，cpu拷贝从4次减少到1次，但状态切换还是4次。   
    &emsp; mmap+write简单来说就是使用`mmap替换了read+write中的read操作`，减少了一次CPU的拷贝。  
    &emsp; `mmap主要实现方式是将读缓冲区的地址和用户缓冲区的地址进行映射，内核缓冲区和应用缓冲区共享，从而减少了从读缓冲区到用户缓冲区的一次CPU拷贝。`  

    ![image](http://182.92.69.8:8081/img/microService/netty/netty-142.png)  

    &emsp; 整个过程发生了4次用户态和内核态的上下文切换和3次拷贝，具体流程如下：  

    1. 用户进程通过mmap()方法向操作系统发起调用，上下文从用户态转向内核态。
    2. DMA控制器把数据从硬盘中拷贝到读缓冲区。
    3. 上下文从内核态转为用户态，mmap调用返回。
    4. 用户进程通过write()方法发起调用，上下文从用户态转为内核态。
    5. CPU将读缓冲区中数据拷贝到socket缓冲区。
    6. DMA控制器把数据从socket缓冲区拷贝到网卡，上下文从内核态切换回用户态，write()返回。

    &emsp; mmap的方式节省了一次CPU拷贝，同时由于用户进程中的内存是虚拟的，`只是映射到内核的读缓冲区，所以可以节省一半的内存空间，比较适合大文件的传输。`  
7. sendfile（函数调用）：  
    1. **<font color = "red">sendfile建立了两个文件之间的传输通道。</font>** `通过使用【sendfile数据可以直接在内核空间进行传输，】因此避免了用户空间和内核空间的拷贝，`同时由于使用sendfile替代了read+write从而节省了一次系统调用，也就是2次上下文切换。   
    2. 流程：  
        ![image](http://182.92.69.8:8081/img/microService/netty/netty-148.png)  
        &emsp; 整个过程发生了2次用户态和内核态的上下文切换和3次拷贝，具体流程如下：  
        1. 用户进程通过sendfile()方法向操作系统发起调用，上下文从用户态转向内核态。
        2. DMA控制器把数据从硬盘中拷贝到读缓冲区。
        3. CPU将读缓冲区中数据拷贝到socket缓冲区。
        4. DMA控制器把数据从socket缓冲区拷贝到网卡，上下文从内核态切换回用户态，sendfile调用返回。  
    3. sendfile方式中，应用程序只需要调用sendfile函数即可完成。数据不经过用户缓冲区，该数据无法被修改。但减少了2次状态切换，即只有2次状态切换、1次CPU拷贝、2次DMA拷贝。  
8. sendfile+DMA收集  
9. splice方式  
&emsp; splice系统调用是Linux在2.6版本引入的，其不需要硬件支持，并且不再限定于socket上，实现两个普通文件之间的数据零拷贝。  
&emsp; splice系统调用可以在内核缓冲区和socket缓冲区之间建立管道来传输数据，避免了两者之间的CPU拷贝操作。  
&emsp; **<font color = "clime">splice也有一些局限，它的两个文件描述符参数中有一个必须是管道设备。</font>**  
![image](http://182.92.69.8:8081/img/microService/netty/netty-35.png)  

## 1.4. Socket编程
&emsp; `Socket是对TCP/IP协议的封装。`Socket只是个接口不是协议，通过Socket才能使用TCP/IP协议，除了TCP，也可以使用UDP协议来传递数据。  

## 1.5. NIO
&emsp; **BIO即Block I/O，同步并阻塞的IO。**  
&emsp; **<font color = "red">NIO，同步非阻塞I/O，基于io多路复用模型，即select，poll，epoll。</font>**  
&emsp; **<font color = "red">AIO，异步非阻塞的IO。</font>**  

&emsp; BIO方式适用于连接数目比较小且固定的架构，这种方式对服务器资源要求比较高，并发局限于应用中，JDK1.4以前的唯一选择，但程序直观简单易理解。  
&emsp; NIO方式适用于连接数目多且连接比较短（轻操作）的架构，比如聊天服务器，并发局限于应用中，编程比较复杂，JDK1.4开始支持。  
&emsp; AIO方式适用于连接数目多且连接比较长（重操作）的架构，比如相册服务器，充分调用OS参与并发操作，编程比较复杂，JDK7开始支持。  

## 1.6. Netty
### 1.6.1. Netty简介
1. Netty是一个`非阻塞I/O` <font color = "red">客户端-服务器框架</font>，主要用于开发Java网络应用程序，如协议服务器和客户端。`异步事件驱动`的网络应用程序框架和工具用于简化网络编程，例如TCP和UDP套接字服务器。Netty包括了`反应器编程模式`的实现。  
&emsp; 除了作为异步网络应用程序框架，Netty还包括了对HTTP、HTTP2、DNS及其他协议的支持，涵盖了在Servlet容器内运行的能力、`对WebSockets的支持`、与Google Protocol Buffers的集成、对SSL/TLS的支持以及对用于SPDY协议和消息压缩的支持。  
2. **<font color = "clime">为什么要用Netty？</font>**  
    &emsp; 在实际的网络开发中，其实很少使用Java NIO原生的API。主要有以下原因：  

    * NIO的类库和API繁杂，使用麻烦，需要熟练掌握Selector、ServerSocketChannel、SockctChannel、ByteBuffer等。  
    * `原生API使用单线程模型，不能很好利用多核优势；`  
    * 原生API是直接使用的IO数据，没有做任何封装处理，对数据的编解码、TCP的粘包和拆包、客户端断连、网络的可靠性和安全性方面没有做处理；  
    * **<font color = "red">JDK NIO的BUG，例如臭名昭著的epoll bug，它会导致Selector空轮询，最终导致CPU100%。</font>官方声称在JDK1.6版本的update18修复了该问题，但是直到JDK 1.7版本该问题仍旧存在，只不过该BUG发生概率降低了一些而已，它并没有得到根本性解决。该BUG以及与该BUG相关的问题单可以参见以下链接内容。** 
        * http://bugs.java.com/bugdatabase/viewbug.do?bug_id=6403933  
        * http://bugs.java.com/bugdalabase/viewbug.do?bug_id=21477l9  

### 1.6.2. Netty运行流程
&emsp; [Netty运行流程](/docs/microService/communication/Netty/operation.md)   

&emsp; **<font color = "clime">一般来说，使用Bootstrap创建启动器的步骤可分为以下几步：</font>**  
![image](http://182.92.69.8:8081/img/microService/netty/netty-151.png)  
1. 创建服务器启动辅助类，服务端是ServerBootstrap。  
    &emsp; 需要设置事件循环组EventLoopGroup，如果使用reactor主从模式，需要创建2个：   
    * <font color = "clime">`创建boss线程组（EventLoopGroup bossGroup）`用于服务端接受客户端的连接；  
    * `创建worker线程组（EventLoopGroup workerGroup）`用于进行SocketChannel的数据读写。</font>  
2. 对ServerBootstrap进行配置，配置项有channel,handler,option。  
3. 绑定服务器端口并启动服务器，同步等待服务器启动完毕。  
4. 阻塞启动线程，并同步等待服务器关闭，因为如果不阻塞启动线程，则会在finally块中执行优雅关闭，导致服务器也会被关闭了。  

![image](http://182.92.69.8:8081/img/microService/netty/netty-150.png)  
![image](http://182.92.69.8:8081/img/microService/netty/netty-44.png)  

&emsp; Netty整体运行流程：  
![image](http://182.92.69.8:8081/img/microService/netty/netty-87.png) 

1. `Netty基于Reactor，parentGroup用于处理连接，childGroup用于处理数据读写。`  
2. 当一个连接到达时，Netty就会创建一个Channel，然后`从EventLoopGroup中分配一个EventLoop来给这个Channel绑定上`，在该Channel的整个生命周期中都是由这个绑定的EventLoop来服务的。  
3. 职责链ChannelPipeline，负责事件在职责链中的有序传播，同时负责动态地编排职责链。职责链可以选择监听和处理自己关心的事件，它可以拦截处理和向后/向前传播事件。 ChannelHandlerContext代表了ChannelHandler和ChannelPipeline之间的绑定。  

### 1.6.3. Netty核心组件
1. `由netty运行流程可以看出Netty核心组件有Bootstrap、channel相关、EventLoop、byteBuf...`  
2. Bootstrap和ServerBootstrap是针对于Client和Server端定义的引导类，主要用于配置各种参数，并启动整个Netty服务。  
3. `EventLoop线程模型`  
    1. **EventLoop定义了Netty的核心抽象，用于处理连接（一个channel）的生命周期中所发生的事件。<font color = "clime">EventLoop的主要作用实际就是负责监听网络事件并调用事件处理器进行相关I/O操作的处理。</font>**  
    2. **<font color = "red">Channel与EventLoop：</font>**  
    &emsp; 当一个连接到达时，Netty就会创建一个Channel，然后从EventLoopGroup中分配一个EventLoop来给这个Channel绑定上，在该Channel的整个生命周期中都是由这个绑定的EventLoop来服务的。  
    3. **<font color = "red">EventLoopGroup与EventLoop：</font>**  
    &emsp; EventLoopGroup是EventLoop的集合，一个EventLoopGroup包含一个或者多个EventLoop。可以将EventLoop看做EventLoopGroup线程池中的一个工作线程。  
4. Channel  
    1. **在Netty中，Channel是一个Socket连接的抽象，它为用户提供了关于底层Socket状态（是否是连接还是断开）以及对Socket的读写等操作。**  
    2. ChannelHandler  
    &emsp; **ChannelHandler主要用来处理各种事件，这里的事件很广泛，比如可以是连接、数据接收、异常、数据转换等。**  
    3. ChannelPipeline  
    &emsp; Netty的ChannelHandler为处理器提供了基本的抽象，目前可以认为每个ChannelHandler的实例都类似于一种为了响应特定事件而被执行的回调。从应用程序开发人员的角度来看，它充当了所有处理入站和出站数据的应用程序逻辑的拦截载体。ChannelPipeline提供了ChannelHandler链的容器，并定义了用于在该链上传播入站和出站事件流的API。当Channel被创建时，它会被自动地分配到它专属的ChannelPipeline。  
    4. ChannelHandlerContext  
    &emsp; 当ChannelHandler被添加到ChannelPipeline时，它将会被分配一个ChannelHandlerContext， **<font color = "clime">它代表了ChannelHandler和ChannelPipeline之间的绑定。</font>** ChannelHandlerContext的主要功能是管理它所关联的ChannelHandler和在同一个ChannelPipeline中的其他ChannelHandler之间的交互。  

5. 小结：  
    1. Netty基于Reactor，parentGroup用于处理连接，childGroup用于处理数据读写。  
    2. 当一个连接到达时，Netty就会创建一个Channel，然后从EventLoopGroup中分配一个EventLoop来给这个Channel绑定上，在该Channel的整个生命周期中都是由这个绑定的EventLoop来服务的。  
    3. 职责链ChannelPipeline，负责事件在职责链中的有序传播，同时负责动态地编排职责链。职责链可以选择监听和处理自己关心的事件，它可以拦截处理和向后/向前传播事件。 ChannelHandlerContext代表了ChannelHandler和ChannelPipeline之间的绑定。  


### 1.6.4. Netty逻辑架构
&emsp; **<font color = "red">Netty采用了典型的三层网络架构进行设计和开发，逻辑架构如下图所示：</font>**  
![image](http://182.92.69.8:8081/img/microService/netty/netty-134.png)  
1. 业务逻辑编排层 Service ChannelHandler：  
&emsp; 业务逻辑编排层通常有两类，一类是纯粹的业务逻辑编排，一类是应用层协议插件，用于特定协议相关的会话和链路管理。由于应用层协议栈往往是开发一次到处运行，并且变动较小，故而将应用协议到 POJO 的转变和上层业务放到不同的 ChannelHandler 中，就可以实现协议层和业务逻辑层的隔离，实现架构层面的分层隔离。  
2. 职责链 ChannelPipeline：  
&emsp; 它负责事件在职责链中的有序传播，同时负责动态地编排职责链。职责链可以选择监听和处理自己关心的事件，它可以拦截处理和向后/向前传播事件。不同应用的Handler用于消息的编解码，它可以将外部的协议消息转换成内部的POJO对象，这样上层业务则只需要关心处理业务逻辑即可，不需要感知底层的协议差异和线程模型差异，实现了架构层面的分层隔离。  
3. 通信调度层 Reactor：  
&emsp; 由一系列辅助类组成，包括 Reactor 线程 NioEventLoop 及其父类，NioSocketChannel 和 NioServerSocketChannel 等等。该层的职责就是监听网络的读写和连接操作，负责将网络层的数据读到内存缓冲区，然后触发各自网络事件，例如连接创建、连接激活、读事件、写事件等。将这些事件触发到 pipeline 中，由 pipeline 管理的职责链来进行后续的处理。  

&emsp; 架构的不同层面，需要关心和处理的对象都不同，通常情况下，对于业务开发者，只需要关心职责链的拦截和业务Handler的编排。因为应用层协议栈往往是开发一次，到处运行，所以实际上对于业务开发者来说，只需要关心服务层的业务逻辑开发即可。各种应用协议以插件的形式提供，只有协议开发人员需要关注协议插件，对于其他业务开发人员来说，只需关心业务逻辑定制。这种分层的架构设计理念实现了NIO框架各层之间的解耦，便于上层业务协议栈的开发和业务逻辑的定制。  

### 1.6.5. Netty高性能
&emsp; Netty高性能：    
1. 内存池设计：申请的内存可以重用，主要指直接内存。内部实现是用一颗二叉查找树管理内存分配情况。  
1. 网络I/O  
    * IO 线程模型：异步非阻塞通信。  
    * [高效的Reactor线程模型](/docs/microService/communication/Netty/Reactor.md) 
    * [零拷贝](/docs/microService/communication/Netty/nettyZeroCopy.md)  
    * 支持 protobuf 等高性能序列化协议。
3. 串形化处理读写：避免使用锁带来的性能开销。以及高效的并发编程。  

#### 1.6.5.1. Netty的Reactor线程模型
1. Netty的线程模型并不是一成不变的，它实际取决于用户的启动参数配置。<font color = "red">通过设置不同的启动参数，Netty可以同时支持Reactor单线程模型、多线程模型和主从Reactor多线层模型。</font><font color = "clime">Netty主要靠NioEventLoopGroup线程池来实现具体的线程模型。</font>  
2. Netty主从Reactor多线程模型，内部实现了两个线程池，boss线程池和work线程池，其中boss线程池的线程负责处理请求的accept事件，当接收到accept事件的请求时，把对应的socket封装到一个NioSocketChannel中，并交给work线程池，其中work线程池负责请求的read和write事件，由对应的Handler处理。

### 1.6.6. Netty开发
#### 1.6.6.1. Netty应用场景，  
&emsp; Netty主要用来做网络通信：   

* 作为 RPC 框架的网络通信工具：在分布式系统中，不同服务节点之间经常需要相互调用，这个时候就需要 RPC 框架了。不同服务节点之间的通信是如何做的呢？可以使用 Netty 来做。  
* 实现一个自己的 HTTP 服务器：通过 Netty 可以实现一个简单的 HTTP 服务器。  
* 实现一个即时通讯系统：使用 Netty 可以实现一个可以聊天类似微信的即时通讯系统，这方面的开源项目还蛮多的，可以自行去 Github 找一找。  
* 实现消息推送系统：市面上有很多消息推送系统都是基于 Netty 来做的。  
* ...  

#### 1.6.6.2. TCP粘拆包与Netty编解码  
1. [TCP的粘包和拆包问题描述](/docs/network/TCPSticking.md)  
2. **<font color = "clime">Netty对半包或者粘包的处理：</font>** **每个Handler都是和Channel唯一绑定的，一个Handler只对应一个Channel，<font color = "red">所以Channel中的数据读取的时候经过解析，如果不是一个完整的数据包，则解析失败，将这个数据包进行保存，等下次解析时再和这个数据包进行组装解析，直到解析到完整的数据包，才会将数据包向下传递。</font>** 
3. Netty默认提供了多种解码器来解决，可以进行分包操作。  
    * 固定长度的拆包器 FixedLengthFrameDecoder
    * 行拆包器 LineBasedFrameDecoder
    * 分隔符拆包器 DelimiterBasedFrameDecoder
    * 基于数据包长度的拆包器 LengthFieldBasedFrameDecoder  

#### 1.6.6.3. Netty实战
&emsp; ...  

#### 1.6.6.4. Netty多协议开发

* Http协议开发应用
* WebSocket协议开发  
&emsp; WebSocket是基于TCP的应用层协议，用于在C/S架构的应用中实现双向通信，关于WebSocket协议的详细规范和定义参见rfc6455。  
* 私有协议栈开发  

### 1.6.7. Netty源码


## 1.7. WebSocket
### 1.7.1. 4种Web端即时通信
1. `Web端即时通讯技术：服务器端可以即时地将数据的更新或变化反应到客户端，例如消息即时推送等功能都是通过这种技术实现的。`但是在Web中，由于浏览器的限制，实现即时通讯需要借助一些方法。这种限制出现的主要原因是，一般的Web通信都是浏览器先发送请求到服务器，服务器再进行响应完成数据的显示更新。  
&emsp; 实现Web端即时通讯的方法：`实现即时通讯主要有四种方式，它们分别是轮询、长轮询(comet)、长连接(SSE)、WebSocket。` **<font color = "blue">它们大体可以分为两类，一种是在HTTP基础上实现的，包括短轮询、comet和SSE；另一种不是在HTTP基础上实现，即WebSocket。</font>**    
2. 短轮询：平时写的忙轮询/无差别轮询。  
3. 长轮询：  
	&emsp; 客户端发送请求后服务器端不会立即返回数据， **<font color = "red">服务器端会阻塞请求连接不会立即断开，`直到服务器端有数据更新或者是连接超时才返回`，</font>** `客户端才再次发出请求新建连接、如此反复从而获取最新数据。`  
	* 优点：长轮询和短轮询比起来，明显减少了很多不必要的http请求次数，相比之下节约了资源。  
	* 缺点：`连接挂起也会导致资源的浪费。`  
4. 长连接（SSE）  
&emsp; SSE是HTML5新增的功能，全称为Server-Sent Events。它可以`允许服务推送数据到客户端`。 **<font color = "clime">SSE在本质上就与之前的长轮询、短轮询不同，虽然都是基于http协议的，但是轮询需要客户端先发送请求。</font>** 而SSE`最大的特点就是不需要客户端发送请求`，`可以实现只要服务器端数据有更新，就可以马上发送到客户端`。  
&emsp; SSE的优势很明显，`它不需要建立或保持大量的客户端发往服务器端的请求，`节约了很多资源，提升应用性能。并且后面会介绍道，SSE的实现非常简单，并且不需要依赖其他插件。  
5. WebSocket  
&emsp; WebSocket是Html5定义的一个新协议，与传统的http协议不同，该协议可以实现服务器与客户端之间全双工通信。简单来说，首先需要在客户端和服务器端建立起一个连接，这部分需要http。连接一旦建立，客户端和服务器端就处于平等的地位，可以相互发送数据，不存在请求和响应的区别。  
&emsp; WebSocket的优点是实现了双向通信，缺点是服务器端的逻辑非常复杂。现在针对不同的后台语言有不同的插件可以使用。  

### 1.7.2. 配置中心使用长轮询推送
1. push、pull、长轮询  
    &emsp; push模型的好处是实时写新的数据到客户端。pull模型的好处是请求/响应模式，完成之后就断开，而不是像push模型一样，一直长连接不断开，如果每个连接都不断开，那么服务器连接数量很快会被耗尽。  
    &emsp; **<font color = "red">长轮询（Long Polling）和轮询（Polling）的区别，两者都是拉模式的实现。</font>**  
2. 配置中心使用长轮询推送  
    &emsp; 客户端发起长轮询，如果服务端的数据没有发生变更，会 hold 住请求，直到服务端的数据发生变化，或者等待一定时间超时才会返回。返回后，客户端又会立即再次发起下一次长轮询。配置中心使用「长轮询」如何解决「轮询」遇到的问题也就显而易见了：
    * 推送延迟。服务端数据发生变更后，长轮询结束，立刻返回响应给客户端。
    * 服务端压力。长轮询的间隔期一般很长，例如 30s、60s，并且服务端 hold 住连接不会消耗太多服务端资源。

### 1.7.3. WebSocket
1. WebSocket是应用层协议  
&emsp; WebSocket是基于TCP的应用层协议，用于在C/S架构的应用中实现双向通信。虽然WebSocket协议在建立连接时会使用HTTP协议，但这并不意味着WebSocket协议是基于HTTP协议实现的。  
2. WebSocket与Http的区别  
&emsp; 通信方式不同：WebSocket是双向通信模式，客户端与服务器之间只有在握手阶段是使用HTTP协议的“请求-响应”模式交互，而一旦连接建立之后的通信则使用双向模式交互，不论是客户端还是服务端都可以随时将数据发送给对方；而HTTP协议则至始至终都采用“请求-响应”模式进行通信。也正因为如此，HTTP协议的通信效率没有WebSocket高。  

