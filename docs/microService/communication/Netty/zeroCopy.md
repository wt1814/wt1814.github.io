
<!-- TOC -->

- [1. IO性能优化之零拷贝](#1-io性能优化之零拷贝)
    - [1.1. 传统Linux I/O中数据拷贝过程](#11-传统linux-io中数据拷贝过程)
        - [1.1.1. 仅CPU方式读数据read流程](#111-仅cpu方式读数据read流程)
        - [1.1.2. CPU&DMA方式](#112-cpudma方式)
            - [1.1.2.1. DMA，直接内存访问(Direct Memory Access)](#1121-dma直接内存访问direct-memory-access)
            - [1.1.2.2. 加入DMA后的读数据read流程](#1122-加入dma后的读数据read流程)
            - [1.1.2.3. ★★★一次读取数据read、传输数据write交互详解](#1123-★★★一次读取数据read传输数据write交互详解)
        - [1.1.3. CPU&DMA-2](#113-cpudma-2)
    - [1.2. 零拷贝技术](#12-零拷贝技术)
        - [1.2.1. sendfile方式](#121-sendfile方式)
        - [1.2.2. ~~sendfile+DMA收集，零拷贝~~](#122-sendfiledma收集零拷贝)
        - [1.2.3. splice方式](#123-splice方式)
    - [1.3. 小结](#13-小结)
    - [1.4. 零拷贝实现](#14-零拷贝实现)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. 比较常见的I/O流程是读取磁盘文件传输到网络中。一次完整的网络I/O流程包括读写2个过程。    
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

    `读写过程仅CPU方式有4次状态切换，4次CPU拷贝。`    

4. CPU & DMA（Direct Memory Access，直接内存访问）方式   
	![image](http://182.92.69.8:8081/img/microService/netty/netty-68.png)    
	&emsp; **<font color = "red">最主要的变化是，CPU不再和磁盘直接交互，而是DMA和磁盘交互并且将数据从磁盘缓冲区拷贝到内核缓冲区，因此减少了2次CPU拷贝。共2次CPU拷贝，2次DMA拷贝，4次状态切换。之后的过程类似。</font>**  

	* 读过程涉及2次空间切换(需要CPU参与)、1次DMA拷贝、1次CPU拷贝；
	* 写过程涉及2次空间切换、1次DMA拷贝、1次CPU拷贝；  
5. `零拷贝技术的几个实现手段包括：mmap+write、sendfile、sendfile+DMA收集、splice等。`  
6. **<font color = "blue">mmap（内存映射）：</font>**   
    &emsp; **<font color = "clime">mmap是Linux提供的一种内存映射文件的机制，它实现了将内核中读缓冲区地址与用户空间缓冲区地址进行映射，从而实现内核缓冲区与用户缓冲区的共享，</font>** 又减少了一次cpu拷贝。总共包含1次cpu拷贝，2次DMA拷贝，4次状态切换。此流程中，cpu拷贝从4次减少到1次，但状态切换还是4次。   
    ![image](http://182.92.69.8:8081/img/microService/netty/netty-100.png)  
7. sendfile（函数调用）：  
    1. 升级后的sendfile将内核空间缓冲区中对应的数据描述信息（文件描述符、地址偏移量等信息）记录到socket缓冲区中。  
    2. DMA控制器根据socket缓冲区中的地址和偏移量将数据从内核缓冲区拷贝到网卡中，从而省去了内核空间中仅剩的1次CPU拷贝。  

&emsp; `数据不经过用户缓冲区，这种方式又减少了1次CPU拷贝。`即有2次状态切换、0次CPU拷贝、2次DMA拷贝。  
&emsp; 但是`仍然无法对数据进行修改`，并且需要硬件层面DMA的支持， **并且sendfile只能将文件数据拷贝到socket描述符上，有一定的局限性。**   
![image](http://182.92.69.8:8081/img/microService/netty/netty-32.png)  
8. sendfile+DMA收集  
9. splice方式  
&emsp; splice系统调用是Linux在2.6版本引入的，其不需要硬件支持，并且不再限定于socket上，实现两个普通文件之间的数据零拷贝。  
&emsp; splice系统调用可以在内核缓冲区和socket缓冲区之间建立管道来传输数据，避免了两者之间的CPU拷贝操作。  
&emsp; **<font color = "clime">splice也有一些局限，它的两个文件描述符参数中有一个必须是管道设备。</font>**  
![image](http://182.92.69.8:8081/img/microService/netty/netty-35.png)  


<!-- 
★★★阿里二面：什么是mmap？ 
https://mp.weixin.qq.com/s/sG0rviJlhVtHzGfd5NoqDQ
★★★零拷贝流程
https://mp.weixin.qq.com/s/xY-hJl5qJv3QtttcMqHZRQ

https://mp.weixin.qq.com/s/mOGtDgcc826MhGP7OWwjyg
深入理解零拷贝技术 
https://blog.csdn.net/wufaliang003/article/details/106195984

-->

# 1. IO性能优化之零拷贝
<!-- 
想理解好零拷贝，重点还在于理解为什么需要拷贝，以及不同零拷贝技术的对比。想理解好 I/O 原理，必须先弄清楚数据结构。  

-->

<!-- 
https://mp.weixin.qq.com/s/mWPjFbCVzvuAW3Y9lEQbGg
~~
https://www.jianshu.com/p/2581342317ce
http://baijiahao.baidu.com/s?id=1664128784220450138&wfr=spider&for=pc
-->

&emsp; I/O传输中的一些基本概念：  

* 状态切换：内核态和用户态之间的切换。  
&emsp; 这里指的用户态、内核态指的是什么？上下文切换又是什么？  
&emsp; 简单来说，用户空间指的就是用户进程的运行空间，内核空间就是内核的运行空间。   
&emsp; 如果进程运行在内核空间就是内核态，运行在用户空间就是用户态。  
&emsp; 为了安全起见，他们之间是互相隔离的，而在用户态和内核态之间的上下文切换也是比较耗时的。  
* CPU拷贝：内核态和用户态之间的复制。  
* DMA拷贝：设备（或网络）和内核态之间的复制。  
&emsp; 因为对于一个IO操作而言，都是通过CPU发出对应的指令来完成，但是相比CPU来说，IO的速度太慢了，CPU有大量的时间处于等待IO的状态。  
&emsp; 因此就产生了DMA（Direct Memory Access）直接内存访问技术，本质上来说他就是一块主板上独立的芯片，通过它来进行内存和IO设备的数据传输，从而减少CPU的等待时间。  
&emsp; 但是无论谁来拷贝，频繁的拷贝耗时也是对性能的影响。  

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

![image](http://182.92.69.8:8081/img/microService/netty/netty-65.png)  
&emsp; 上述数据流转只是概述，接下来看看几种模式。

### 1.1.1. 仅CPU方式读数据read流程
&emsp; 基于传统的IO方式，底层实际上通过调用read()和write()来实现。 **<font color = "red">通过read()把数据从硬盘读取到内核缓冲区，再复制到用户缓冲区；然后再通过write()写入到socket缓冲区，最后写入网卡设备。</font>**  

```text
File.read(file, buf, len);
Socket.send(socket, buf, len);
```

&emsp; 整个过程发生了4次用户态和内核态的上下文切换和4次拷贝。  

![image](http://182.92.69.8:8081/img/microService/netty/netty-66.png)  


&emsp; **仅CPU方式读数据read流程，具体流程如下：**  

* 当应用程序需要读取磁盘数据时，调用read()从用户态陷入内核态，read()这个系统调用最终由CPU来完成；
* CPU向磁盘发起I/O请求，磁盘收到之后开始准备数据；
* 磁盘将数据放到磁盘缓冲区之后，向CPU发起I/O中断，报告CPU数据已经Ready了；
* CPU收到磁盘控制器的I/O中断之后，开始拷贝数据，完成之后read()返回，再从内核态切换到用户态；  

&emsp; ~~缺点：~~ CPU操作数据与磁盘操作数据的速度不是一个量级。 **CPU是很珍贵的资源。**  

-------

![image](http://182.92.69.8:8081/img/microService/netty/netty-139.png)  

&emsp; 4 次 copy：  

* CPU 负责将数据从磁盘搬运到内核空间的 Page Cache 中；  
* CPU 负责将数据从内核空间的 Socket 缓冲区搬运到的网络中；  
* CPU 负责将数据从内核空间的 Page Cache 搬运到用户空间的缓冲区；  
* CPU 负责将数据从用户空间的缓冲区搬运到内核空间的 Socket 缓冲区中；  


&emsp; 4 次上下文切换：

* read 系统调用时：用户态切换到内核态；
* read 系统调用完毕：内核态切换回用户态；
* write 系统调用时：用户态切换到内核态；
* write 系统调用完毕：内核态切换回用户态；



### 1.1.2. CPU&DMA方式
#### 1.1.2.1. DMA，直接内存访问(Direct Memory Access)
&emsp; 直接内存访问(Direct Memory Access，DMA)：DMA允许外设设备和内存存储器之间直接进行IO数据传输，其过程不需要CPU的参与。

<!-- 
CPU的时间宝贵，让它做杂活就是浪费资源。

直接内存访问(Direct Memory Access)，是一种硬件设备绕开CPU独立直接访问内存的机制。所以DMA在一定程度上解放了CPU，把之前CPU的杂活让硬件直接自己做了，提高了CPU效率。

![image](http://182.92.69.8:8081/img/microService/netty/netty-63.png)  
-->

&emsp; 目前支持DMA的硬件包括：网卡、声卡、显卡、磁盘控制器等。  
![image](http://182.92.69.8:8081/img/microService/netty/netty-67.png)  

#### 1.1.2.2. 加入DMA后的读数据read流程
&emsp; 有了DMA的参与之后的流程发生了一些变化：  
![image](http://182.92.69.8:8081/img/microService/netty/netty-68.png)    
&emsp; **<font color = "red">最主要的变化是，CPU不再和磁盘直接交互，而是DMA和磁盘交互并且将数据从磁盘缓冲区拷贝到内核缓冲区，之后的过程类似。</font>**

<!-- 
用户进程通过read()方法向操作系统发起调用，此时上下文从用户态转向内核态
DMA控制器把数据从硬盘中拷贝到读缓冲区
CPU把读缓冲区数据拷贝到应用缓冲区，上下文从内核态转为用户态，read()返回
用户进程通过write()方法发起调用，上下文从用户态转为内核态
CPU将应用缓冲区中数据拷贝到socket缓冲区
DMA控制器把数据从socket缓冲区拷贝到网卡，上下文从内核态切换回用户态，write()返回

-->

#### 1.1.2.3. ★★★一次读取数据read、传输数据write交互详解




### 1.1.3. CPU&DMA-2 
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
![image](http://182.92.69.8:8081/img/microService/netty/netty-98.png)  
&emsp; 由上图可以很清晰地看到，一次 read-send 涉及到了四次拷贝：  

* 硬盘拷贝到内核缓冲区(DMA COPY)；
* 内核缓冲区拷贝到应用程序缓冲区(CPU COPY)；
* 应用程序缓冲区拷贝到socket缓冲区(CPU COPY)；
* socket buf拷贝到网卡的buf(DMA COPY)。

&emsp; 其中涉及到2次CPU中断, 还有4次的上下文切换。很明显,第2次和第3次的的copy只是把数据复制到app buffer又原封不动的复制回来, 为此带来了两次的CPU COPY和两次上下文切换, 是完全没有必要的。

&emsp; Linux的零拷贝技术就是为了优化掉这两次不必要的拷贝。


## 1.2. 零拷贝技术

### 1.2.1. sendfile方式


### 1.2.2. ~~sendfile+DMA收集，零拷贝~~
&emsp; Linux 2.4内核对sendfile系统调用进行优化，但是需要硬件DMA控制器的配合。  
&emsp; 升级后的sendfile将内核空间缓冲区中对应的数据描述信息（文件描述符、地址偏移量等信息）记录到socket缓冲区中。  
&emsp; DMA控制器根据socket缓冲区中的地址和偏移量将数据从内核缓冲区拷贝到网卡中，从而省去了内核空间中仅剩的1次CPU拷贝。  
![image](http://182.92.69.8:8081/img/microService/netty/netty-33.png)  
&emsp; 这种方式又减少了1次CPU拷贝，即有2次状态切换、0次CPU拷贝、2次DMA拷贝。  
&emsp; 但是仍然无法对数据进行修改，并且需要硬件层面DMA的支持，并且sendfile只能将文件数据拷贝到socket描述符上，有一定的局限性。

<!-- 
带有DMA收集拷贝功能的sendfile实现的零拷贝  
从Linux 2.4版本开始，操作系统提供scatter和gather的SG-DMA方式，直接从内核空间缓冲区中将数据读取到网卡，无需将内核空间缓冲区的数据再复制一份到socket缓冲区  
![image](http://182.92.69.8:8081/img/microService/netty/netty-77.png)  
    1)发出sendfile系统调用，导致用户空间到内核空间的上下文切换。通过DMA引擎将磁盘文件中的内容复制到内核空间缓冲区
    2)这里没把数据复制到socket缓冲区；取而代之的是，相应的描述符信息被复制到socket缓冲区。该描述符包含了两种的信息：A)内核缓冲区的内存地址、B)内核缓冲区的偏移量
    3)sendfile系统调用返回，导致内核空间到用户空间的上下文切换。DMA根据socket缓冲区的描述符提供的地址和偏移量直接将内核缓冲区中的数据复制到网卡

「带有DMA收集拷贝功能的sendfile实现的I/O使用了2次用户空间与内核空间的上下文切换，以及2次数据的拷贝，而且这2次的数据拷贝都是非CPU拷贝。这样一来我们就实现了最理想的零拷贝I/O传输了，不需要任何一次的CPU拷贝，以及最少的上下文切换」
-->

----------

另外如果硬件支持的化,sendfile函数还可以直接将文件描述符和数据长度发送给socket缓冲区,然后直接通过DMA传输将页缓冲区的数据拷贝给网卡进行发送即可,这样就避免了CPU在内核空间内的拷贝过程，流程如下：  

第一步：通过DMA传输将硬盘中的数据复制到内核页缓冲区  
第二步：通过sendfile函数将页缓冲区数据的文件描述符和数据长度发送给socket缓冲区  
第三步：网卡通过DMA传输根据文件描述符和文件长度直接从页缓冲区拷贝数据  

如下图示：  
![image](http://182.92.69.8:8081/img/microService/netty/netty-144.png)  
整个过程中：DMA拷贝2次、CPU拷贝0次、内核空间和用户空间切换0次  
所以整个过程都是没有CPU拷贝的过程的，实现了真正的CPU零拷贝机制  

------------------

Linux2.4内核版本之后对sendfile做了进一步优化，通过引入新的硬件支持，这个方式叫做DMA Scatter/Gather 分散/收集功能。  
它将读缓冲区中的数据描述信息--内存地址和偏移量记录到socket缓冲区，由 DMA 根据这些将数据从读缓冲区拷贝到网卡，相比之前版本减少了一次CPU拷贝的过程  
![image](http://182.92.69.8:8081/img/microService/netty/netty-145.png)  

整个过程发生了2次用户态和内核态的上下文切换和2次拷贝，其中更重要的是完全没有CPU拷贝，具体流程如下：  

1. 用户进程通过sendfile()方法向操作系统发起调用，上下文从用户态转向内核态  
2. DMA控制器利用scatter把数据从硬盘中拷贝到读缓冲区离散存储  
3. CPU把读缓冲区中的文件描述符和数据长度发送到socket缓冲区  
4. DMA控制器根据文件描述符和数据长度，使用scatter/gather把数据从内核缓冲区拷贝到网卡  
5. sendfile()调用返回，上下文从内核态切换回用户态  

DMA gather和sendfile一样数据对用户空间不可见，而且需要硬件支持，同时输入文件描述符只能是文件，但是过程中完全没有CPU拷贝过程，极大提升了性能。  

### 1.2.3. splice方式  
&emsp; splice系统调用是Linux在2.6版本引入的，其不需要硬件支持，并且不再限定于socket上，实现两个普通文件之间的数据零拷贝。  
&emsp; splice系统调用可以在内核缓冲区和socket缓冲区之间建立管道来传输数据，避免了两者之间的CPU拷贝操作。  
![image](http://182.92.69.8:8081/img/microService/netty/netty-35.png)  
&emsp; **<font color = "clime">splice也有一些局限，它的两个文件描述符参数中有一个必须是管道设备。</font>**  

--------

splice函数的作用是将两个文件描述符之间建立一个管道,然后将文件描述符的引用传递过去,这样在使用到数据的时候就可以直接通过引用指针访问到具体数据。过程如下：  

第一步:通过DMA传输将文件复制到内核页缓冲区  
第二步:通过splice函数在页缓冲区和socket缓冲区之间建立管道,并将文件描述符的引用指针发送给socket缓冲区  
第三步:网卡通过DMA传输根据文件描述符的指针直接访问数据  

如下图示：  
![image](http://182.92.69.8:8081/img/microService/netty/netty-146.png)  

整个过程中：DMA拷贝2次、CPU拷贝0次、内核空间和用户空间切换0次  
可以看出通过slice函数传输数据时同样可以实现CPU的零拷贝，且不需要CPU在内核空间和用户空间之间来回切换  

## 1.3. 小结  
由于CPU和IO速度的差异问题，产生了DMA技术，通过DMA搬运来减少CPU的等待时间。  
传统的IOread+write方式会产生2次DMA拷贝+2次CPU拷贝，同时有4次上下文切换。  
而通过mmap+write方式则产生2次DMA拷贝+1次CPU拷贝，4次上下文切换，通过内存映射减少了一次CPU拷贝，可以减少内存使用，适合大文件的传输。  
sendfile方式是新增的一个系统调用函数，产生2次DMA拷贝+1次CPU拷贝，但是只有2次上下文切换。因为只有一次调用，减少了上下文的切换，但是用户空间对IO数据不可见，适用于静态文件服务器。  
sendfile+DMA gather方式产生2次DMA拷贝，没有CPU拷贝，而且也只有2次上下文切换。虽然极大地提升了性能，但是需要依赖新的硬件设备支持。  


## 1.4. 零拷贝实现  

&emsp; [Java中的零拷贝](/docs/microService/communication/NIO/JavaZeroCopy.md)  