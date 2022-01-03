
<!-- TOC -->

- [1. Linux的五种I/O模型](#1-linux的五种io模型)
    - [1.1. 阻塞IO](#11-阻塞io)
        - [1.1.1. 流程](#111-流程)
        - [1.1.2. 使用示例](#112-使用示例)
    - [1.2. 非阻塞IO](#12-非阻塞io)
        - [1.2.1. 流程](#121-流程)
        - [1.2.2. 使用示例](#122-使用示例)
    - [1.3. 多路复用IO](#13-多路复用io)
        - [1.3.1. 为什么要有多路复用IO](#131-为什么要有多路复用io)
        - [1.3.2. 多路复用IO流程？](#132-多路复用io流程)
        - [1.3.3. 使用示例](#133-使用示例)
    - [1.4. ~~信号驱动IO~~](#14-信号驱动io)
    - [1.5. ~~异步IO~~](#15-异步io)
    - [1.6. 同步/异步、阻塞/非阻塞](#16-同步异步阻塞非阻塞)
        - [1.6.1. 同步和异步](#161-同步和异步)
        - [1.6.2. 阻塞和非阻塞](#162-阻塞和非阻塞)
        - [1.6.3. 小结](#163-小结)
    - [1.7. ~~各I/O模型的对比与总结~~](#17-各io模型的对比与总结)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "red">网络IO的本质就是socket流的读取，通常一次IO读操作会涉及到两个对象和两个阶段。**</font>  
    * **<font color = "clime">两个对象：用户进程（线程）、内核对象（内核态和用户态）。</font><font color = "blue">用户进程请求内核。</font>**   
    * **<font color = "clime">`内核中涉及两个阶段：1. 等待数据准备；2. 数据从内核空间拷贝到用户空间。`</font>**  
    &emsp; `⚠️基于以上两个阶段就产生了五种不同的IO模式，`分别是：阻塞I/O模型、非阻塞I/O模型、多路复用I/O模型、信号驱动I/O模型、异步I/O模型。其中，前四种被称为同步I/O。  
2. **<font color = "blue">同步（等待结果）和阻塞（线程）：</font>**  
    * 异步和同步：对于请求结果的获取是客户端主动获取结果，还是由服务端来通知结果。    
    * 阻塞和非阻塞：在等待这个函数返回结果之前，当前的线程是处于挂起状态还是运行状态。 
3. 同步阻塞I/O：  
    1. 流程：  
        ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-1.png)  
        1. 用户进程发起recvfrom系统调用内核。用户进程【同步】等待结果；
        2. 内核等待I/O数据返回，此时用户进程处于【阻塞】，一直等待内核返回；
        3. I/O数据返回后，内核将数据从内核空间拷贝到用户空间；  
        4. 内核将数据返回给用户进程。  
    特点：两阶段都阻塞。  
    2. BIO采用多线程时，`大量的线程占用很大的内存空间，并且线程切换会带来很大的开销，10000个线程真正发生读写事件的线程数不会超过20%，每次accept都开一个线程也是一种资源浪费。`  
4. 同步非阻塞I/O：  
    1. 流程：  
        ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-2.png)  
        1. 用户进程发起recvfrom系统调用内核。用户进程【同步】等待结果；
        2. 内核等待I/O数据返回。无I/O数据返回时，内核返回给用户进程ewouldblock结果。`【非阻塞】用户进程，立马返回结果。`但 **<font color = "clime">用户进程要主动轮询查询结果。</font>**  
        3. I/O数据返回后，内核将数据从内核空间拷贝到用户空间；  
        4. 内核将数据返回给用户进程。  
    特点：`第一阶段不阻塞但要轮询，`第二阶段阻塞。  
    2. NIO`每次轮询所有fd（包括没有发生读写事件的fd）会很浪费cpu。`  
5. 多路复用I/O：（~~同步阻塞，又基于回调通知~~）  
    1. 为什么有多路复用？  
    &emsp; 如果一个I/O流进来，就开启一个进程处理这个I/O流。那么假设现在有一百万个I/O流进来，那就需要开启一百万个进程一一对应处理这些I/O流（——这就是传统意义下的多进程并发处理）。思考一下，一百万个进程，CPU占有率会多高，这个实现方式及其的不合理。所以人们提出了I/O多路复用这个模型，一个线程，通过记录I/O流的状态来同时管理多个I/O，可以提高服务器的吞吐能力。  
    2. `多路是指多个socket套接字，复用是指复用同一个进程。`  
    3. 流程：  
        ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-3.png)  
        1. 用户多进程或多线程发起select系统调用，复用器Selector会监听注册进来的进程事件。用户进程【同步】等待结果；
        2. 内核等待I/O数据返回，无数据返回时，select进程【阻塞】，进程也受阻于select调用；
        2. I/O数据返回后，内核将数据从内核空间拷贝到用户空间， **<font color = "clime">Selector`通知`哪个进程哪个事件；</font>**  
        4. 进程发起recvfrom系统调用。
    多路复用I/O模型和阻塞I/O模型并没有太大的不同，事实上，还更差一些，因为它需要使用两个系统调用(select和recvfrom)，而阻塞I/O模型只有一次系统调用(recvfrom)。但是Selector的优势在于它可以同时处理多个连接。   
    4. 多路复用`能支持更多的并发连接请求。`  
6. 信号驱动IO  
7. 异步IO

# 1. Linux的五种I/O模型  
&emsp; **<font color = "blue">学习本章节，需要对Linux操作系统有一定的了解。</font>**  
&emsp; 本章讨论的背景是Linux环境下的network IO。  
<!--
图解---》流程描述---》概况
-->
<!--
IO 模型
https://mp.weixin.qq.com/s/I-IL8W2Yx7udxe8b7FCbbw

*** https://mp.weixin.qq.com/s/sB5yi3fdfKsvTetND5cjVg

https://www.cnblogs.com/shoshana-kong/p/14062903.html
https://zhuanlan.zhihu.com/p/115220699

图很好 https://mp.weixin.qq.com/s/Lge9CelG9SuDidbdAarGCQ

-->

<!--
大白话详解5种网络IO模型 
https://mp.weixin.qq.com/s/Tdtn3r1u-dn-cLl2Vzurrg
-->

<!-- 

1.1. ~~概念说明~~  
&emsp; 在进行解释之前，首先要说明几个概念：  
- 用户空间和内核空间   
- 进程切换  
- 进程的阻塞  
- 文件描述符  
- 缓存 I/O  

1.1.1. ~~用户空间与内核空间~~
&emsp; 现在操作系统都是采用虚拟存储器，那么对32位操作系统而言，它的寻址空间(虚拟存储空间)为4G(2的32次方)。操作系统的核心是内核，独立于普通的应用程序，可以访问受保护的内存空间，也有访问底层硬件设备的所有权限。为了保证用户进程不能直接操作内核(kernel)，保证内核的安全，操心系统将虚拟空间划分为两部分，一部分为内核空间，一部分为用户空间。针对linux操作系统而言，将最高的1G字节(从虚拟地址0xC0000000到0xFFFFFFFF)，供内核使用，称为内核空间，而将较低的3G字节(从虚拟地址0x00000000到0xBFFFFFFF)，供各个进程使用，称为用户空间。  

1.1.2. ~~进程切换~~
&emsp; 为了控制进程的执行，内核必须有能力挂起正在CPU上运行的进程，并恢复以前挂起的某个进程的执行。这种行为被称为进程切换。因此可以说，任何进程都是在操作系统内核的支持下运行的，是与内核紧密相关的。  
&emsp; 从一个进程的运行转到另一个进程上运行，这个过程中经过下面这些变化：  
1. 保存处理机上下文，包括程序计数器和其他寄存器。
2. 更新PCB信息。
3. 把进程的PCB移入相应的队列，如就绪、在某事件阻塞等队列。
4. 选择另一个进程执行，并更新其PCB。
5. 更新内存管理的数据结构。
6. 恢复处理机上下文。  

1.1.3. ~~进程的阻塞~~
&emsp; 正在执行的进程，由于期待的某些事件未发生，如请求系统资源失败、等待某种操作的完成、新数据尚未到达或无新工作做等，则由系统自动执行阻塞原语(Block)，使自己由运行状态变为阻塞状态。可见，进程的阻塞是进程自身的一种主动行为，也因此只有处于运行态的进程(获得CPU)，才可能将其转为阻塞状态。当进程进入阻塞状态，是不占用CPU资源的。  

1.1.4. ~~文件描述符fd~~
&emsp; 文件描述符(File descriptor)是计算机科学中的一个术语，是一个用于表述指向文件的引用的抽象化概念。  
&emsp; 文件描述符在形式上是一个非负整数。实际上，它是一个索引值，指向内核为每一个进程所维护的该进程打开文件的记录表。当程序打开一个现有文件或者创建一个新文件时，内核向进程返回一个文件描述符。在程序设计中，一些涉及底层的程序编写往往会围绕着文件描述符展开。但是文件描述符这一概念往往只适用于UNIX、Linux这样的操作系统。  

1.1.5. ~~缓存I/O~~
&emsp; 缓存 I/O 又被称作标准I/O，大多数文件系统的默认I/O操作都是缓存I/O。在Linux的缓存 I/O 机制中，操作系统会将 I/O 的数据缓存在文件系统的页缓存( page cache )中，也就是说，数据会先被拷贝到操作系统内核的缓冲区中，然后才会从操作系统内核的缓冲区拷贝到应用程序的地址空间。  
&emsp; **缓存I/O的缺点：**  
&emsp; 数据在传输过程中需要在应用程序地址空间和内核进行多次数据拷贝操作，这些数据拷贝操作所带来的 CPU 以及内存开销是非常大的。  
-->

<!-- 
&emsp; I/O交换流程：在操作系统中，应用程序对于一次IO操作(以read举例)，数据会先拷贝到内核空间中，然后再从内核空间拷贝到用户空间中，所以<font color = "red">一次read操作，会经历两个阶段：1. 等待数据准备；2. 数据从内核空间拷贝到用户空间。   
-->

&emsp; **<font color = "clime">网络IO的本质就是socket流的读取，在操作系统中，应用程序对于一次IO操作(以read举例)，数据会先拷贝到内核空间中，然后再从内核空间拷贝到用户空间中。因此一次IO操作会涉及到两个对象和两个阶段。</font>**  

&emsp; 两个对象：  

* 用户进程(线程)
* 内核对象  

&emsp; 两个阶段：  

* 等待数据流准备。~~第一步通常涉及等待网络上的数据分组到达，然后被复制到内核的某个缓冲区。~~
* 从内核像进程复制数据。~~第二步把数据从内核缓冲区复制到进程缓冲区。~~

------

&emsp; 基于以上两个阶段就产生了五种不同的IO模式，分别是：阻塞I/O模型、非阻塞I/O模型、多路复用I/O模型、异步I/O模型。</font><font color= "clime">其中，前四种被称为同步I/O。</font>   
 

## 1.1. 阻塞IO  
### 1.1.1. 流程
&emsp; 在linux中，默认情况下所有的socket都是blocking。从进程发起IO操作，一直等待上述两个阶段完成。两阶段一起阻塞。  
&emsp; 一个典型的读操作流程大概是这样：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-1.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-47.png)  

1. 用户进程发起recvfrom系统调用内核。用户进程【同步】等待结果；
2. 内核等待I/O数据返回，此时用户进程处于【阻塞】，一直等待内核返回；
3. I/O数据返回后，内核将数据从内核空间拷贝到用户空间；  
4. 内核将数据返回给用户进程。


&emsp; 特点：两个阶段都阻塞。  

<!-- 
&emsp; 当用户进程调用了recvfrom这个系统调用，kernel就开始了IO的第一个阶段：准备数据(对于网络IO来说，很多时候数据在一开始还没有到达。比如，还没有收到一个完整的UDP包。这个时候kernel就要等待足够的数据到来)。这个过程需要等待，也就是说数据被拷贝到操作系统内核的缓冲区中是需要一个过程的。而在用户进程这边，整个进程会被阻塞(当然，是进程自己选择的阻塞)。当kernel一直等到数据准备好了，它就会将数据从kernel中拷贝到用户内存，然后kernel返回结果，用户进程才解除block的状态，重新运行起来。  
&emsp; 所以，blocking IO的特点就是在IO执行的两个阶段都被block了。  
-->

### 1.1.2. 使用示例
&emsp; 服务端采用单线程，当accept一个请求后，在recv或send调用阻塞时，将无法accept其他请求（必须等上一个请求处recv或send完），`无法处理并发`。 

```c
// 伪代码描述
while(1) {
  // accept阻塞
  client_fd = accept(listen_fd)
  fds.append(client_fd)
  for (fd in fds) {
    // recv阻塞（会影响上面的accept）
    if (recv(fd)) {
      // logic
    }
  }  
}
```

&emsp; 服务器端采用多线程，当accept一个请求后，开启线程进行recv，可以完成并发处理，但随着请求数增加需要增加系统线程，`大量的线程占用很大的内存空间，并且线程切换会带来很大的开销，10000个线程真正发生读写事件的线程数不会超过20%，每次accept都开一个线程也是一种资源浪费。`  

```c
// 伪代码描述
while(1) {
  // accept阻塞
  client_fd = accept(listen_fd)
  // 开启线程read数据（fd增多导致线程数增多）
  new Thread func() {
    // recv阻塞（多线程不影响上面的accept）
    if (recv(fd)) {
      // logic
    }
  }  
}
```

## 1.2. 非阻塞IO  
### 1.2.1. 流程
&emsp; Linux下，可以通过设置socket使其变为non-blocking。进程一直询问IO准备好了没有，准备好了再发起读取操作，这时才把数据从内核空间拷贝到用户空间。 **<font color = "red">第一阶段不阻塞但要轮询，第二阶段阻塞。</font>**  
&emsp; 当对一个non-blocking socket执行读操作时，流程是这个样子：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-2.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-48.png)  

1. 用户进程发起recvfrom系统调用内核。用户进程【同步】等待结果；
2. 内核等待I/O数据返回。无I/O数据返回时，内核返回给用户进程ewouldblock结果。【非阻塞】用户进程，但用户进程要【轮询查询】结果。
3. I/O数据返回后，内核将数据从内核空间拷贝到用户空间；  
4. 内核将数据返回给用户进程。 

&emsp; 特点：第一阶段不阻塞但要轮询，第二阶段阻塞。  

<!-- 
&emsp; 当用户进程发出read操作时，如果kernel中的数据还没有准备好，那么它并不会block用户进程，而是立刻返回一个error。从用户进程角度讲，它发起一个read操作后，并不需要等待，而是马上就得到了一个结果。 **用户进程判断结果是一个error时，它就知道数据还没有准备好，于是它可以再次发送read操作。一旦kernel中的数据准备好了，并且又再次收到了用户进程的system call，那么它马上就将数据拷贝到了用户内存，然后返回。**  
&emsp; 所以，nonblocking IO的特点是用户进程需要不断的主动询问kernel数据好了没有。  
-->

### 1.2.2. 使用示例
&emsp; 服务器端当accept一个请求后，加入fds集合，每次轮询一遍fds集合recv(非阻塞)数据，没有数据则立即返回错误，`每次轮询所有fd（包括没有发生读写事件的fd）会很浪费cpu。`  

```c
setNonblocking(listen_fd)
// 伪代码描述
while(1) {
  // accept非阻塞（cpu一直忙轮询）
  client_fd = accept(listen_fd)
  if (client_fd != null) {
    // 有人连接
    fds.append(client_fd)
  } else {
    // 无人连接
  }  
  for (fd in fds) {
    // recv非阻塞
    setNonblocking(client_fd)
    // recv 为非阻塞命令
    if (len = recv(fd) && len > 0) {
      // 有读写数据
      // logic
    } else {
       无读写数据
    }
  }  
}
```

## 1.3. 多路复用IO  
<!-- 
此模型用到select和poll函数，这两个函数也会使进程阻塞，select先阻塞，有活动套接字才返回，但是和阻塞I/O不同的是，这两个函数可以同时阻塞多个I/O操作，而且可以同时对多个读操作，多个写操作的I/O函数进行检测，直到有数据可读或可写（就是监听多个socket）。select被调用后，进程会被阻塞，内核监视所有select负责的socket，当有任何一个socket的数据准备好了，select就会返回套接字可读，我们就可以调用recvfrom处理数据。  
-->
### 1.3.1. 为什么要有多路复用IO
<!-- 
https://zhuanlan.zhihu.com/p/115220699
-->
&emsp; IO复用形成原因：  
&emsp; 如果一个I/O流进来，我们就开启一个进程处理这个I/O流。那么假设现在有一百万个I/O流进来，那就需要开启一百万个进程一一对应处理这些I/O流（——这就是传统意义下的多进程并发处理）。思考一下，一百万个进程，你的CPU占有率会多高，这个实现方式及其的不合理。所以人们提出了I/O多路复用这个模型，一个线程，通过记录I/O流的状态来同时管理多个I/O，可以提高服务器的吞吐能力。  


### 1.3.2. 多路复用IO流程？  
&emsp; `多路是指多个socket套接字，复用是指复用同一个进程。`   
&emsp; 多个连接使用同一个select去询问IO准备好了没有，如果有准备好了的，就返回有数据准备好了，然后对应的连接再发起读取操作，把数据从内核空间拷贝到用户空间。  

&emsp; 两阶段分开阻塞。  
&emsp; IO multiplexing就是指select，poll，epoll，有些也称这种IO方式为event driven IO。select/epoll的好处就在于单个process就可以同时处理多个网络连接的IO。它的基本原理就是select，poll，epoll这个function会不断的轮询所负责的所有socket，当某个socket有数据到达了，就通知用户进程。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-3.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-49.png)  


1. 用户多进程或多线程发起select系统调用，复用器Selector会监听注册进来的进程事件。用户进程【同步】等待结果；
2. 内核等待I/O数据返回，无数据返回时，select进程【阻塞】，进程也受阻于select调用；
2. I/O数据返回后，内核将数据从内核空间拷贝到用户空间，Selector通知哪个进程哪个事件；
4. 进程发起recvfrom系统调用。

<!-- 
&emsp; 当用户进程调用了select，那么整个进程会被block，而同时，kernel会“监视”所有select负责的socket，当任何一个socket中的数据准备好了，select就会返回。这个时候用户进程再调用read操作，将数据从kernel拷贝到用户进程。  
&emsp; 所以，I/O 多路复用的特点是通过一种机制一个进程能同时等待多个文件描述符，而这些文件描述符(套接字描述符)其中的任意一个进入读就绪状态，select()函数就可以返回。  
&emsp; 这个图和blocking IO的图其实并没有太大的不同，事实上，还更差一些。因为这里需要使用两个system call (select 和 recvfrom)，而blocking IO只调用了一个system call (recvfrom)。但是，用select的优势在于它可以同时处理多个connection。  
&emsp; 所以，如果处理的连接数不是很高的话，使用select/epoll的web server不一定比使用multi-threading + blocking IO的web server性能更好，可能延迟还更大。select/epoll的优势并不是对于单个连接能处理得更快，而是在于能处理更多的连接。)  
&emsp; 在IO multiplexing Model中，实际中，对于每一个socket，一般都设置成为non-blocking，但是，如上图所示，整个用户的process其实是一直被block的。只不过process是被select这个函数block，而不是被socket IO给block。  

------------

&emsp; 多个进程的I/O可以注册到一个复用器(Selector)上，当用户进程调用该Selector，Selector会监听注册进来的所有I/O，如果Selector监听的所有I/O在内核缓存区都没有可读数据，select调用进程会被阻塞，而当任一I/O在内核缓冲区中有可读数据时，select调用就会返回，而后select调用进程可以自己或通知另外的进程(注册进程)再次发起读取I/O，读取内核中准备好的数据，多个进程注册I/O后，只有一个select调用进程被阻塞。    
&emsp; 其实多路复用I/O模型和阻塞I/O模型并没有太大的不同，事实上，还更差一些，因为这里需要使用两个系统调用(select和recvfrom)，而阻塞I/O模型只有一次系统调用(recvfrom)。但是Selector的优势在于它可以同时处理多个连接。   
-->

### 1.3.3. 使用示例
&emsp; 服务器端采用单线程通过select/epoll等系统调用获取fd列表，遍历有事件的fd进行accept/recv/send，使其`能支持更多的并发连接请求。`  

```c
fds = [listen_fd]
// 伪代码描述
while(1) {
  // 通过内核获取有读写事件发生的fd，只要有一个则返回，无则阻塞
  // 整个过程只在调用select、poll、epoll这些调用的时候才会阻塞，accept/recv是不会阻塞
  for (fd in select(fds)) {
    if (fd == listen_fd) {
        client_fd = accept(listen_fd)
        fds.append(client_fd)
    } elseif (len = recv(fd) && len != -1) { 
      // logic
    }
  }  
}
```

## 1.4. ~~信号驱动IO~~  
<!-- 
https://blog.csdn.net/uestcprince/article/details/90734564
-->
&emsp; 进程发起读取操作会立即返回，当数据准备好了会以通知的形式告诉进程，进程再发起读取操作，把数据从内核空间拷贝到用户空间。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-4.png)  

&emsp; 特点：第一阶段不阻塞，第二阶段阻塞。  

## 1.5. ~~异步IO~~
&emsp; **进程发起读取操作会立即返回，等到数据准备好且已经拷贝到用户空间了再通知进程拿数据。两个阶段都不阻塞。**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-5.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-50.png)  
&emsp; 用户进程发起read操作之后，立刻就可以开始去做其它的事。而另一方面，从kernel的角度，当它接收到一个asynchronous read之后，首先它会立刻返回，所以不会对用户进程产生任何block。然后，kernel会等待数据准备完成，然后将数据拷贝到用户内存，当这一切都完成之后，kernel会给用户进程发送一个signal，告诉它read操作完成了。  

## 1.6. 同步/异步、阻塞/非阻塞  
### 1.6.1. 同步和异步
<!-- 
同步非同步的区别在于调用操作系统的recvfrom()的时候是否阻塞，可见除了最后的异步IO其它都是同步IO。
同步、异步  
&emsp; 同步请求，A调用B，B的处理是同步的，在处理完之前不会通知A，只有处理完之后才会明确的通知A。  
&emsp; 异步请求，A调用B，B的处理是异步的，B在接到请求后先告诉A已经接到请求了，然后异步去处理，处理完之后通过回调等方式再通知A。  
&emsp; 所以说，同步和异步最大的区别就是被调用方的执行方式和返回时机。同步指的是被调用方做完事情之后再返回，异步指的是被调用方先返回，然后再做事情，做完之后再想办法通知调用方。
-->
&emsp; 同步和异步其实是指获取CPU时间片到利用，**主要看请求发起方对消息结果的获取是主动发起的，还是被动通知的。**如下图所示，如果是请求方主动发起的，一直在等待应答结果(同步阻塞)，或者可以先去处理其他事情，但要不断轮询查看发起的请求是否由应答结果(同步非阻塞)，因为不管如何都要发起方主动获取消息结果，所以形式上还是同步操作。如果是由服务方通知的，也就是请求方发出请求后，要么一直等待通知(异步阻塞)，要么先去干自己的事(异步非阻塞)。当事情处理完成后，服务方会主动通知请求方，它的请求已经完成，这就是异步。异步通知的方式一般通过状态改变、消息通知或者回调函数来完成，大多数时候采用的都是回调函数。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-8.png)  

### 1.6.2. 阻塞和非阻塞  
<!-- 
阻塞、非阻塞  
&emsp; 阻塞请求，A调用B，A一直等着B的返回，别的事情什么也不干。  
&emsp; 非阻塞请求，A调用B，A不用一直等着B的返回，先去处理其他事情。  
&emsp; 所以说，阻塞非阻塞最大的区别就是在被调用方返回结果之前的这段时间内，调用方是否一直等待。阻塞指的是调用方一直等待别的事情什么都不做。非阻塞指的是调用方先去忙别的事情。
-->
&emsp; 阻塞和非阻塞在计算机的世界里，通常指针对I/O的操作，如网络I/O和磁盘I/O等。那么什么是阻塞和非阻塞呢？简单地说，就是调用一个函数后，**在等待这个函数返回结果之前，当前的线程是处于挂起状态还是运行状态。**如果是挂起状态，就意味着当前线程什么都不能干，就等着获取结果，这就是同步阻塞；如果仍然是运行状态，就意味着当前线程是可以继续处理其他任务的，但要时不时地看一下是否由结果来，这就是同步非阻塞。具体如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-9.png)  

### 1.6.3. 小结  
<!-- 
&emsp; **阻塞、非阻塞和同步、异步的区别：**  
&emsp; 阻塞、非阻塞和同步、异步其实针对的对象是不一样的。阻塞、非阻塞说的是调用者，同步、异步说的是被调用者。
-->
&emsp; 从上面的描述中，可以看到<font color = "red">阻塞和非阻塞通常是指在客户端发出请求后，在服务端处理这个请求的过程中，客户端本身是直接挂起等待结果，还是继续做其他的任务。而异步和同步则是对于请求结果的获取是客户端主动获取结果，还是由服务端来通知结果。</font>从这一点来看，<font color = "clime">同步和阻塞其实描述的是两个不同角度的事情，阻塞和非阻塞指的是客户端等待消息处理时本身的状态，是挂起还是继续干别的。同步和异步指的是对于消息结果是客户端主动获取的，还是由服务端间接推送的。</font>  

## 1.7. ~~各I/O模型的对比与总结~~  
&emsp; 前四种I/O模型都是同步I/O操作，它们的区别在于第一阶段，而第二阶段是一样的：在数据从内核拷贝到应用缓冲期间(用户空间)，进程阻塞于recvfrom调用。  
&emsp; 下图是各I/O 模型的阻塞状态对比：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-6.png)  
<!-- 

通过上面的图片，可以发现non-blocking IO和asynchronous IO的区别还是很明显的。在non-blocking IO中，虽然进程大部分时间都不会被block，但是它仍然要求进程去主动的check，并且当数据准备完成以后，也需要进程主动的再次调用recvfrom来将数据拷贝到用户内存。而asynchronous IO则完全不同。它就像是用户进程将整个IO操作交给了他人(kernel)完成，然后他人做完后发信号通知。在此期间，用户进程不需要去检查IO操作的状态，也不需要主动的去拷贝数据。
-->
&emsp; 从上图可以看出，阻塞程度：阻塞I/O > 非阻塞I/O > 多路复用I/O > 信号驱动I/O > 异步I/O，效率是由低到高到。  

<!-- 
最后，再看一下下表，从多维度总结了各I/O模型之间到差异。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-7.png)  
-->
