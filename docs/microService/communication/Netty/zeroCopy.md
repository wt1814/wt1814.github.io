
<!-- TOC -->

- [1. IO性能优化之零拷贝](#1-io性能优化之零拷贝)
    - [1.1. 传统Linux I/O中数据拷贝过程](#11-传统linux-io中数据拷贝过程)
        - [1.1.1. 仅CPU方式读数据read流程](#111-仅cpu方式读数据read流程)
        - [1.1.2. CPU&DMA方式](#112-cpudma方式)
            - [1.1.2.1. DMA介绍](#1121-dma介绍)
            - [1.1.2.2. 加入DMA后的读数据read流程](#1122-加入dma后的读数据read流程)
            - [1.1.2.3. ★★★一次读取数据read、传输数据write交互详解](#1123-★★★一次读取数据read传输数据write交互详解)
        - [1.2. CPU&DMA-2](#12-cpudma-2)
    - [1.3. 零拷贝技术](#13-零拷贝技术)
        - [1.3.1. 零拷贝简介](#131-零拷贝简介)
        - [1.3.2. 零拷贝技术实现技术](#132-零拷贝技术实现技术)
            - [1.3.2.1. mmap+write方式，内存映射](#1321-mmapwrite方式内存映射)
            - [1.3.2.2. sendfile方式](#1322-sendfile方式)
            - [1.3.2.3. ~~sendfile+DMA收集，零拷贝~~](#1323-sendfiledma收集零拷贝)
            - [1.3.2.4. splice方式](#1324-splice方式)
    - [1.4. 零拷贝实现](#14-零拷贝实现)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  

1. 比较常见的I/O流程是读取磁盘文件传输到网络中。  
2. **<font color = "clime">I/O传输中的一些基本概念：</font>**  
    * 状态切换：内核态和用户态之间的切换。  
    * CPU拷贝：内核态和用户态之间的复制。 **零拷贝："零"更多的是指在用户态和内核态之间的复制是0次。**   
    * DMA拷贝：设备(或网络)和内核态之间的复制。  
3. 多种I/O传输方式： 
    1. 仅CPU方式：  
    &emsp; 仅CPU方式有4次状态切换，4次CPU拷贝。  
    1. **CPU&DMA方式：**    
        * 读过程涉及2次空间切换(需要CPU参与)、1次DMA拷贝、1次CPU拷贝；
        * 写过程涉及2次空间切换、1次DMA拷贝、1次CPU拷贝；

    CPU&DMA与仅CPU方式相比少了2次CPU拷贝，多了2次DMA拷贝。  

    2. mmap+write方式：  
    &emsp; **<font color = "clime">mmap是Linux提供的一种内存映射文件的机制，它实现了将内核中读缓冲区地址与用户空间缓冲区地址进行映射，从而实现内核缓冲区与用户缓冲区的共享。</font>**  
    &emsp; 使用mmap+write方式相比CPU&DMA方式又少了一次CPU拷贝。即进行了4次用户空间与内核空间的上下文切换，以及3次数据拷贝；其中3次数据拷贝中包括了2次DMA拷贝和1次CPU拷贝。  

    3. sendfile方式：  
    &emsp; **<font color = "red">它建立了两个文件之间的传输通道。</font>**  
    &emsp; 应用程序只需要调用sendfile函数即可完成。数据不经过用户缓冲区，该数据无法被修改。但减少了2次状态切换，即只有2次状态切换、1次CPU拷贝、2次DMA拷贝。   
    4. sendfile+DMA：  
    &emsp; 升级后的sendfile将内核空间缓冲区中对应的数据描述信息（文件描述符、地址偏移量等信息）记录到socket缓冲区中。  
    &emsp; DMA控制器根据socket缓冲区中的地址和偏移量将数据从内核缓冲区拷贝到网卡中，从而省去了内核空间中仅剩的1次CPU拷贝。  
    &emsp; 这种方式又减少了1次CPU拷贝，即有2次状态切换、0次CPU拷贝、2次DMA拷贝。  
    &emsp; 但是仍然无法对数据进行修改，并且需要硬件层面DMA的支持， **并且sendfile只能将文件数据拷贝到socket描述符上，有一定的局限性。**   
    5. splice方式：  
    &emsp; splice系统调用是Linux在2.6版本引入的，其不需要硬件支持，并且不再限定于socket上，实现两个普通文件之间的数据零拷贝。  
    &emsp; splice系统调用可以在内核缓冲区和socket缓冲区之间建立管道来传输数据，避免了两者之间的CPU拷贝操作。  
    &emsp; **<font color = "clime">splice也有一些局限，它的两个文件描述符参数中有一个必须是管道设备。</font>**  


# 1. IO性能优化之零拷贝
<!-- 
想理解好零拷贝，重点还在于理解为什么需要拷贝，以及不同零拷贝技术的对比。想理解好 I/O 原理，必须先弄清楚数据结构。  
零拷贝
https://www.jianshu.com/p/2581342317ce
http://baijiahao.baidu.com/s?id=1664128784220450138&wfr=spider&for=pc
https://mp.weixin.qq.com/s/mWPjFbCVzvuAW3Y9lEQbGg
https://blog.csdn.net/wufaliang003/article/details/106195984
-->

&emsp; I/O传输中的一些基本概念：  

* 状态切换：内核态和用户态之间的切换。  
* CPU拷贝：内核态和用户态之间的复制。  
* DMA拷贝：设备(或网络)和内核态之间的复制。  

## 1.1. 传统Linux I/O中数据拷贝过程
<!-- 
3、操作系统中谁负责IO拷贝？
DMA 负责内核间的 IO 传输，CPU 负责内核和应用间的 IO 传输。
两种拷贝类型：
(1)CPU COPY
通过计算机的组成原理我们知道， 内存的读写操作是需要 CPU 的协调数据总线，地址总线和控制总线来完成的因此在"拷贝"发生的时候，往往需要 CPU 暂停现有的处理逻辑，来协助内存的读写，这种我们称为 CPU COPY。CPU COPY 不但占用了 CPU 资源，还占用了总线的带宽。
(2)DMA COPY
DMA(DIRECT MEMORY ACCESS) 是现代计算机的重要功能，它有一个重要特点：当需要与外设进行数据交换时， CPU 只需要初始化这个动作便可以继续执行其他指令，剩下的数据传输的动作完全由DMA来完成可以看到 DMA COPY 是可以避免大量的 CPU 中断的。
4、拷贝过程中会发生什么？
从内核态到用户态时会发生上下文切换，上下文切换时指由用户态切换到内核态， 以及由内核态切换到用户态。
-->

&emsp; 在Linux系统内部，缓存和内存容量都是有限的，更多的数据都是存储在磁盘中。对于Web服务器来说，经常需要从磁盘中读取read数据到内存，然后再通过网卡传输write给用户。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-65.png)  
&emsp; 上述数据流转只是概述，接下来看看几种模式。

### 1.1.1. 仅CPU方式读数据read流程
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-66.png)  
&emsp; **仅CPU方式读数据read流程：**  

* 当应用程序需要读取磁盘数据时，调用read()从用户态陷入内核态，read()这个系统调用最终由CPU来完成；
* CPU向磁盘发起I/O请求，磁盘收到之后开始准备数据；
* 磁盘将数据放到磁盘缓冲区之后，向CPU发起I/O中断，报告CPU数据已经Ready了；
* CPU收到磁盘控制器的I/O中断之后，开始拷贝数据，完成之后read()返回，再从内核态切换到用户态；  

&emsp; ~~缺点：~~ CPU操作数据与磁盘操作数据的速度不是一个量级。 **CPU是很珍贵的资源。**  

### 1.1.2. CPU&DMA方式
#### 1.1.2.1. DMA介绍
&emsp; 直接内存访问(Direct Memory Access，DMA)：DMA允许外设设备和内存存储器之间直接进行IO数据传输，其过程不需要CPU的参与。

<!-- 
CPU的时间宝贵，让它做杂活就是浪费资源。

直接内存访问(Direct Memory Access)，是一种硬件设备绕开CPU独立直接访问内存的机制。所以DMA在一定程度上解放了CPU，把之前CPU的杂活让硬件直接自己做了，提高了CPU效率。

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-63.png)  
-->

&emsp; 目前支持DMA的硬件包括：网卡、声卡、显卡、磁盘控制器等。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-67.png)  

#### 1.1.2.2. 加入DMA后的读数据read流程
&emsp; 有了DMA的参与之后的流程发生了一些变化：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-68.png)    
&emsp; **<font color = "red">最主要的变化是，CPU不再和磁盘直接交互，而是DMA和磁盘交互并且将数据从磁盘缓冲区拷贝到内核缓冲区，之后的过程类似。</font>**


#### 1.1.2.3. ★★★一次读取数据read、传输数据write交互详解
&emsp; 继续思考Web服务器读取本地磁盘文件数据再通过网络传输给用户的详细过程。  
&emsp; 一次完成的数据交互包括几个部分：系统调用syscall、CPU、DMA、网卡、磁盘等。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-69.png)   
&emsp; **来看下完整的数据拷贝过程简图：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-71.png)  
-----
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-99.png)  
&emsp; **应用程序读数据read流程：**

* 应用程序要读取磁盘数据，调用read()函数从而实现用户态切换内核态，这是第1次状态切换；
* DMA控制器将数据从磁盘拷贝到内核缓冲区，这是第1次DMA拷贝；
* CPU将数据从内核缓冲区复制到用户缓冲区，这是第1次CPU拷贝；
* CPU完成拷贝之后，read()函数返回实现用户态切换用户态，这是第2次状态切换；

&emsp; **应用程序写数据write流程：**

* 应用程序要向网卡写数据，调用write()函数实现用户态切换内核态，这是第1次切换；
* CPU将用户缓冲区数据拷贝到内核缓冲区，这是第1次CPU拷贝；
* DMA控制器将数据从内核缓冲区复制到socket缓冲区，这是第1次DMA拷贝；
* 完成拷贝之后，write()函数返回实现内核态切换用户态，这是第2次切换；

&emsp; 综上所述：

* 读过程涉及2次空间切换(需要CPU参与)、1次DMA拷贝、1次CPU拷贝；
* 写过程涉及2次空间切换、1次DMA拷贝、1次CPU拷贝；


&emsp; CPU&DMA与仅CPU方式少了2次CPU拷贝，多了2次DMA拷贝。  
&emsp; **注：无论从仅CPU方式和DMA&CPU方式，都存在多次冗余数据拷贝和内核态&用户态的切换。**  


### 1.2. CPU&DMA-2 
&emsp;总结所有系统中，不管是WEB应用服务器，FTP服务器，数据库服务器，静态文件服务器等等，所有涉及到数据传输的场景，无非就一种：——从硬盘上读取文件数据, 发送到网络上去。  

&emsp;这个场景简化为一个模型：  

    File.read(fileDesc, buf, len); 
    Socket.send(socket, buf, len);

&emsp; 为了方便描述，上面这两行代码，给它起个名字: read-send模型。  

&emsp; **<font color = "red">操作系统在实现这个read-send模型时，需要有以下步骤：</font>**  
1. 应用程序开始读文件的操作；
2. 应用程序发起系统调用, 从用户态切换到内核态(第一次上下文切换)；
3. 内核态中把数据从硬盘文件读取到内核中间缓冲区(kernel buf)；
4. 数据从内核中间缓冲区(kernel buf)复制到(用户态)应用程序缓冲区(app buf)，从内核态切换回到用户态(第二次上下文切换)；
5. 应用程序开始发送数据到网络上；
6. 应用程序发起系统调用，从用户态切换到内核态(第三次上下文切换)；
7. 内核中把数据从应用程序(app buf)的缓冲区复制到socket的缓冲区(socket)；
8. 内核中再把数据从socket的缓冲区(socket buf)发送的网卡的缓冲区(NIC buf)上；
9. 从内核态切换回到用户态(第四次上下文切换)。

&emsp;如下图表示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-98.png)  
&emsp; 由上图可以很清晰地看到, 一次 read-send 涉及到了四次拷贝：  

* 硬盘拷贝到内核缓冲区(DMA COPY)；
* 内核缓冲区拷贝到应用程序缓冲区(CPU COPY)；
* 应用程序缓冲区拷贝到socket缓冲区(CPU COPY)；
* socket buf拷贝到网卡的buf(DMA COPY)。

&emsp; 其中涉及到2次CPU中断, 还有4次的上下文切换。很明显,第2次和第3次的的copy只是把数据复制到app buffer又原封不动的复制回来, 为此带来了两次的CPU COPY和两次上下文切换, 是完全没有必要的。

&emsp; Linux的零拷贝技术就是为了优化掉这两次不必要的拷贝。


## 1.3. 零拷贝技术
### 1.3.1. 零拷贝简介
&emsp; **零拷贝是什么？**    
&emsp; "零拷贝"中的"拷贝"是指操作系统在I/O操作中，将数据从一个内存区域复制到另外一个内存区域， **<font color = "red">而"零"并不是指0次复制，更多的是指在用户态和内核态之间的复制是0次。</font>**

&emsp; **零拷贝的好处：**

* 减少甚至完全避免不必要的CPU拷贝，从而让CPU解脱出来去执行其他的任务；
* 减少内存的占用；
* 通常零拷贝技术还能够减少用户空间和操作系统内核空间之间的上下文切换。

<!-- 
3、操作系统中谁负责IO拷贝？

DMA 负责内核间的 IO 传输，CPU 负责内核和应用间的 IO 传输。

两种拷贝类型：
（1）CPU COPY

通过计算机的组成原理我们知道, 内存的读写操作是需要 CPU 的协调数据总线,地址总线和控制总线来完成的因此在"拷贝"发生的时候,往往需要 CPU 暂停现有的处理逻辑,来协助内存的读写，这种我们称为 CPU COPY。CPU COPY 不但占用了 CPU 资源,还占用了总线的带宽。


（2）DMA COPY

DMA(DIRECT MEMORY ACCESS) 是现代计算机的重要功能，它有一个重要特点：当需要与外设进行数据交换时, CPU 只需要初始化这个动作便可以继续执行其他指令,剩下的数据传输的动作完全由DMA来完成可以看到 DMA COPY 是可以避免大量的 CPU 中断的。


4、拷贝过程中会发生什么？

从内核态到用户态时会发生上下文切换，上下文切换时指由用户态切换到内核态, 以及由内核态切换到用户态。
-->

### 1.3.2. 零拷贝技术实现技术
&emsp; 目前，零拷贝技术的几个实现手段包括：mmap+write、sendfile、sendfile+DMA收集、splice等。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-72.png)

#### 1.3.2.1. mmap+write方式，内存映射
&emsp; **<font color = "clime">mmap是Linux提供的一种内存映射文件的机制，它实现了将内核中读缓冲区地址与用户空间缓冲区地址进行映射，从而实现内核缓冲区与用户缓冲区的共享。</font>**  
&emsp; 使用mmap+write方式代替原来的read+write方式，mmap是一种内存映射文件的方法，即将一个文件或者其它对象映射到进程的地址空间，实现文件磁盘地址和进程虚拟地址空间中一段虚拟地址的一一对映关系；这样就可以省掉原来内核read缓冲区copy数据到用户缓冲区，但是还是需要内核read缓冲区将数据copy到内核socket缓冲区。大致如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-100.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-73.png)   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-30.png)  
&emsp; 这样就减少了一次用户态和内核态的CPU拷贝，但是在内核空间内仍然有一次CPU拷贝。  
&emsp; **<font color = "clime">「通过mmap实现的零拷贝I/O进行了4次用户空间与内核空间的上下文切换，以及3次数据拷贝；其中3次数据拷贝中包括了2次DMA拷贝和1次CPU拷贝」。</font>**  

&emsp; mmap对大文件传输有一定优势，但是小文件可能出现碎片，并且在多个进程同时操作文件时可能产生引发coredump的signal。  

<!-- 
include <sys/mman.h>
void *mmap(void *start， size_t length， int prot， int flags， int fd， off_t offset)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-75.png)   
    1)发出mmap系统调用，导致用户空间到内核空间的上下文切换。然后通过DMA引擎将磁盘文件中的数据复制到内核空间缓冲区
    2)mmap系统调用返回，导致内核空间到用户空间的上下文切换
    3)这里不需要将数据从内核空间复制到用户空间，因为用户空间和内核空间共享了这个缓冲区
    4)发出write系统调用，导致用户空间到内核空间的上下文切换。将数据从内核空间缓冲区复制到内核空间socket缓冲区；write系统调用返回，导致内核空间到用户空间的上下文切换
    5)异步，DMA引擎将socket缓冲区中的数据copy到网卡
-->

#### 1.3.2.2. sendfile方式
&emsp; mmap+write方式有一定改进，但是由系统调用引起的状态切换并没有减少。  
&emsp; sendfile系统调用是在Linux内核2.1版本中被引入， **<font color = "red">它建立了两个文件之间的传输通道。</font>**  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-31.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-32.png)  
1. 发出sendfile系统调用，导致用户空间到内核空间的上下文切换，然后通过DMA引擎将磁盘文件中的内容复制到内核空间缓冲区中，接着再将数据从内核空间缓冲区复制到socket相关的缓冲区。    
2. sendfile系统调用返回，导致内核空间到用户空间的上下文切换。DMA异步将内核空间socket缓冲区中的数据传递到网卡。    


&emsp; sendfile方式中，应用程序只需要调用sendfile函数即可完成。数据不经过用户缓冲区，该数据无法被修改。但减少了2次状态切换，即只有2次状态切换、1次CPU拷贝、2次DMA拷贝。    
&emsp; 但是sendfile在内核缓冲区和socket缓冲区仍然存在一次CPU拷贝，或许这个还可以优化。

<!-- 
sendfile系统调用在内核版本2.1中被引入，目的是简化通过网络在两个通道之间进行的数据传输过程。sendfile系统调用的引入，不仅减少了数据复制，还减少了上下文切换的次数，大致如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-74.png)  
数据传送只发生在内核空间，所以减少了一次上下文切换；但是还是存在一次copy，能不能把这一次copy也省略掉，Linux2.4内核中做了改进，将Kernel buffer中对应的数据描述信息(内存地址，偏移量)记录到相应的socket缓冲区当中，这样连内核空间中的一次cpu copy也省掉了；  

include <sys/sendfile.h>
ssize_t sendfile(int out_fd， int in_fd， off_t *offset， size_t count);
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-76.png)  
    1)发出sendfile系统调用，导致用户空间到内核空间的上下文切换，然后通过DMA引擎将磁盘文件中的内容复制到内核空间缓冲区中，接着再将数据从内核空间缓冲区复制到socket相关的缓冲区  
    2)sendfile系统调用返回，导致内核空间到用户空间的上下文切换。DMA异步将内核空间socket缓冲区中的数据传递到网卡  

「通过sendfile实现的零拷贝I/O使用了2次用户空间与内核空间的上下文切换，以及3次数据的拷贝。其中3次数据拷贝中包括了2次DMA拷贝和1次CPU拷贝」
-->

#### 1.3.2.3. ~~sendfile+DMA收集，零拷贝~~
&emsp; Linux 2.4内核对sendfile系统调用进行优化，但是需要硬件DMA控制器的配合。  
&emsp; 升级后的sendfile将内核空间缓冲区中对应的数据描述信息(文件描述符、地址偏移量等信息)记录到socket缓冲区中。  
&emsp; DMA控制器根据socket缓冲区中的地址和偏移量将数据从内核缓冲区拷贝到网卡中，从而省去了内核空间中仅剩的1次CPU拷贝。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-33.png)  
&emsp; 这种方式又减少了1次CPU拷贝，即有2次状态切换、0次CPU拷贝、2次DMA拷贝。  
&emsp; 但是仍然无法对数据进行修改，并且需要硬件层面DMA的支持，并且sendfile只能将文件数据拷贝到socket描述符上，有一定的局限性。

<!-- 
带有DMA收集拷贝功能的sendfile实现的零拷贝  
从Linux 2.4版本开始，操作系统提供scatter和gather的SG-DMA方式，直接从内核空间缓冲区中将数据读取到网卡，无需将内核空间缓冲区的数据再复制一份到socket缓冲区  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-77.png)  
    1)发出sendfile系统调用，导致用户空间到内核空间的上下文切换。通过DMA引擎将磁盘文件中的内容复制到内核空间缓冲区
    2)这里没把数据复制到socket缓冲区；取而代之的是，相应的描述符信息被复制到socket缓冲区。该描述符包含了两种的信息：A)内核缓冲区的内存地址、B)内核缓冲区的偏移量
    3)sendfile系统调用返回，导致内核空间到用户空间的上下文切换。DMA根据socket缓冲区的描述符提供的地址和偏移量直接将内核缓冲区中的数据复制到网卡

「带有DMA收集拷贝功能的sendfile实现的I/O使用了2次用户空间与内核空间的上下文切换，以及2次数据的拷贝，而且这2次的数据拷贝都是非CPU拷贝。这样一来我们就实现了最理想的零拷贝I/O传输了，不需要任何一次的CPU拷贝，以及最少的上下文切换」
-->

#### 1.3.2.4. splice方式  
&emsp; splice系统调用是Linux在2.6版本引入的，其不需要硬件支持，并且不再限定于socket上，实现两个普通文件之间的数据零拷贝。  
&emsp; splice系统调用可以在内核缓冲区和socket缓冲区之间建立管道来传输数据，避免了两者之间的CPU拷贝操作。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-35.png)  
&emsp; **<font color = "clime">splice也有一些局限，它的两个文件描述符参数中有一个必须是管道设备。</font>**  

## 1.4. 零拷贝实现  

&emsp; [Java中的零拷贝](/docs/microService/communication/NIO/JavaZeroCopy.md)  