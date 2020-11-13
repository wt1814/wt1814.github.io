


<!-- TOC -->

- [1. 零拷贝](#1-零拷贝)
    - [1.1. DMA介绍](#11-dma介绍)
        - [1.1.1. 为什么要有 DMA 技术?](#111-为什么要有-dma-技术)
        - [1.1.2. 传统的文件传输有多糟糕？](#112-传统的文件传输有多糟糕)
        - [1.1.3. 如何优化文件传输的性能？](#113-如何优化文件传输的性能)
        - [1.1.4. 如何实现零拷贝？](#114-如何实现零拷贝)
            - [1.1.4.1. mmap + write](#1141-mmap--write)
            - [1.1.4.2. sendfile](#1142-sendfile)
        - [1.1.5. sendfile+DMA收集](#115-sendfiledma收集)
        - [1.1.6. plice方式](#116-plice方式)
    - [1.2. Netty中的零拷贝](#12-netty中的零拷贝)

<!-- /TOC -->

# 1. 零拷贝  

<!-- 
框架篇：小白也能秒懂的Linux零拷贝原理 
https://juejin.im/post/6887469050515947528

关于零拷贝的一点认识 
https://mp.weixin.qq.com/s/KD2cpeUviLcEE3wrfdnrlQ
-->

## 1.1. DMA介绍
### 1.1.1. 为什么要有 DMA 技术?  
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

&emsp; 使用 DMA 控制器进行数据传输的过程如下图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-20.png)  

    最主要的变化是，CPU不再和磁盘直接交互，而是DMA和磁盘交互并且将数据从磁盘缓冲区拷贝到内核缓冲区，之后的过程类似。  

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

    直接内存访问（Direct Memory Access），是一种硬件设备绕开CPU独立直接访问内存的机制。所以DMA在一定程度上解放了CPU，把之前CPU的杂活让硬件直接自己做了，提高了CPU效率。  

### 1.1.2. 传统的文件传输有多糟糕？  
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

### 1.1.3. 如何优化文件传输的性能？  
&emsp; **先来看看，如何减少「用户态与内核态的上下文切换」的次数呢？**  
&emsp; 读取磁盘数据的时候，之所以要发生上下文切换，这是因为用户空间没有权限操作磁盘或网卡，内核的权限最高，这些操作设备的过程都需要交由操作系统内核来完成，所以一般要通过内核去完成某些任务的时候，就需要使用操作系统提供的系统调用函数。  
&emsp; 而一次系统调用必然会发生 2 次上下文切换：首先从用户态切换到内核态，当内核执行完任务后，再切换回用户态交由进程代码执行。  
&emsp; 所以，要想减少上下文切换到次数，就要减少系统调用的次数。  

&emsp; **再来看看，如何减少「数据拷贝」的次数？**  
&emsp; 在前面已经知道了，传统的文件传输方式会历经 4 次数据拷贝，而且这里面，「从内核的读缓冲区拷贝到用户的缓冲区里，再从用户的缓冲区里拷贝到 socket 的缓冲区里」，这个过程是没有必要的。  
&emsp; 因为文件传输的应用场景中，在用户空间我们并不会对数据「再加工」，所以数据实际上可以不用搬运到用户空间，因此用户的缓冲区是没有必要存在的。  

### 1.1.4. 如何实现零拷贝？  
<!--零拷贝技术的几个实现手段包括：mmap+write、sendfile、sendfile+DMA收集、splice等。
 -->
&emsp; 零拷贝技术实现的方式通常有 2 种：  

* mmap + write
* sendfile

&emsp; 下面就谈一谈，它们是如何减少「上下文切换」和「数据拷贝」的次数。  

#### 1.1.4.1. mmap + write  
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

-----
&emsp; mmap是Linux提供的一种内存映射文件的机制，它实现了将内核中读缓冲区地址与用户空间缓冲区地址进行映射，从而实现内核缓冲区与用户缓冲区的共享。  
&emsp; 这样就减少了一次用户态和内核态的CPU拷贝，但是在内核空间内仍然有一次CPU拷贝。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-30.png)  
&emsp; mmap对大文件传输有一定优势，但是小文件可能出现碎片，并且在多个进程同时操作文件时可能产生引发coredump的signal。  

#### 1.1.4.2. sendfile  
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

-----
&emsp; mmap+write方式有一定改进，但是由系统调用引起的状态切换并没有减少。  
&emsp; sendfile系统调用是在 Linux 内核2.1版本中被引入，它建立了两个文件之间的传输通道。  
&emsp; sendfile方式只使用一个函数就可以完成之前的read+write 和 mmap+write的功能，这样就少了2次状态切换，由于数据不经过用户缓冲区，因此该数据无法被修改。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-31.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-32.png)  
&emsp; 从图中可以看到，应用程序只需要调用sendfile函数即可完成，只有2次状态切换、1次CPU拷贝、2次DMA拷贝。  
&emsp; 但是sendfile在内核缓冲区和socket缓冲区仍然存在一次CPU拷贝，或许这个还可以优化。  

### 1.1.5. sendfile+DMA收集  
&emsp; Linux 2.4 内核对 sendfile 系统调用进行优化，但是需要硬件DMA控制器的配合。  
&emsp; 升级后的sendfile将内核空间缓冲区中对应的数据描述信息（文件描述符、地址偏移量等信息）记录到socket缓冲区中。  
&emsp; DMA控制器根据socket缓冲区中的地址和偏移量将数据从内核缓冲区拷贝到网卡中，从而省去了内核空间中仅剩1次CPU拷贝。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-33.png)  
&emsp; 这种方式有2次状态切换、0次CPU拷贝、2次DMA拷贝，但是仍然无法对数据进行修改，并且需要硬件层面DMA的支持，并且sendfile只能将文件数据拷贝到socket描述符上，有一定的局限性。  

### 1.1.6. plice方式  
&emsp; splice系统调用是Linux 在 2.6 版本引入的，其不需要硬件支持，并且不再限定于socket上，实现两个普通文件之间的数据零拷贝。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-34.png)  
&emsp; splice 系统调用可以在内核缓冲区和socket缓冲区之间建立管道来传输数据，避免了两者之间的 CPU 拷贝操作。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-35.png)  
&emsp; splice也有一些局限，它的两个文件描述符参数中有一个必须是管道设备。  

## 1.2. Netty中的零拷贝  
&emsp; Netty的零拷贝主要体现在如下三个方面  
&emsp; （1）Netty接收和发送ByteBuffer采用DirectBuffer，使用堆外直接内存进行Socket读写，不需要进行字节缓冲区的二次拷贝。如果使用传统的堆存（Heap Buffer）进行Socket读写，那么JVM会将堆存拷贝一份到直接内存中，然后才写入Socket。相比于堆外直接内存，消息在发送过程中多了一次缓冲区的内存拷贝。  
&emsp; （2）Netty提供了组合Buffer对象，可以聚合多个ByteBuffer对象，用户可以像操作一个Buffer那样方便地对组合Buffer进行操作，避免了传统的通过内存拷贝的方式将几个小Buffer合并成一个大Buffer大烦琐操作。  
&emsp; （3）Netty中文件传输采用了transferTo()方法，它可以直接将文件缓冲区的数据发送到目标Channel，避免了传统通过循环write()方式导致的内存拷贝问题。  
