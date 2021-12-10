
<!-- TOC -->

- [1. 零拷贝](#1-零拷贝)
    - [1.1. 前言](#11-前言)
    - [1.2. CPU](#12-cpu)
    - [1.3. CPU + DMA(直接内存访问)](#13-cpu--dma直接内存访问)
        - [1.3.1. 流程](#131-流程)
    - [1.4. 零拷贝技术](#14-零拷贝技术)
    - [1.5. mmap(内存映射) + write](#15-mmap内存映射--write)
        - [1.5.1. 简介](#151-简介)
        - [1.5.2. 流程](#152-流程)
        - [1.5.3. 小结](#153-小结)
    - [1.6. sendfile](#16-sendfile)
        - [1.6.1. 简介](#161-简介)
        - [1.6.2. 流程](#162-流程)
        - [1.6.3. 小结](#163-小结)
    - [1.7. sendfile+DMA收集，零拷贝](#17-sendfiledma收集零拷贝)

<!-- /TOC -->


# 1. 零拷贝
<!-- 
零拷贝实现原理与使用 
https://mp.weixin.qq.com/s/16QtgkJiPSxK--QdFwf0vA
-->

## 1.1. 前言


## 1.2. CPU


## 1.3. CPU + DMA(直接内存访问)  
&emsp; 直接内存访问(Direct Memory Access，DMA)：DMA允许外设设备和内存存储器之间直接进行IO数据传输，其过程不需要CPU的参与。  
&emsp; 有了DMA的参与之后的流程发生了一些变化：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-68.png)    
&emsp; **<font color = "red">最主要的变化是，CPU不再和磁盘直接交互，而是DMA和磁盘交互并且将数据从磁盘缓冲区拷贝到内核缓冲区，之后的过程类似。</font>**



### 1.3.1. 流程

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-71.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-140.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-99.png)  


<!-- 
第一步:操作系统通过DMA传输将硬盘中的数据复制到内核缓冲区  
第二步:操作系统执行read方法将内核缓冲区的数据复制到用户空间  
第三步:操作系统执行write方法将用户空间的数据复制到内核socket缓冲区  
第四步:操作系统通过DMA传输将内核socket缓冲区数据复制给网卡发送数据  
-->


------
1. **应用程序读数据read流程：**
    1. 用户进程通过read()方法向操作系统发起调用，此时上下文从用户态转向内核态  
    2. DMA控制器把数据从硬盘中拷贝到读缓冲区  
    3. CPU把（内核态）读缓冲区数据拷贝到（用户态）应用缓冲区，上下文从内核态转为用户态，read()返回  
2. **应用程序写数据write流程：**
    4. 用户进程通过write()方法发起调用，上下文从用户态转为内核态  
    5. CPU将应用缓冲区中数据拷贝到socket缓冲区  
    6. DMA控制器把数据从socket缓冲区拷贝到网卡，上下文从内核态切换回用户态，write()返回  

-------

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

----------------

&emsp; 综上所述：

* 读过程涉及2次空间切换(需要CPU参与)、1次DMA拷贝、1次CPU拷贝；
* 写过程涉及2次空间切换、1次DMA拷贝、1次CPU拷贝；

&emsp; CPU&DMA与仅CPU方式少了2次CPU拷贝，多了2次DMA拷贝。  
&emsp; **注：无论从仅CPU方式和DMA&CPU方式，都存在多次冗余数据拷贝和内核态&用户态的切换。**  


## 1.4. 零拷贝技术
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

&emsp; 目前，`零拷贝技术的几个实现手段包括：mmap+write、sendfile、sendfile+DMA收集、splice等。`  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-72.png)


## 1.5. mmap(内存映射) + write
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

### 1.5.1. 简介
&emsp; 使用mmap+write方式代替原来的read+write方式，mmap是一种内存映射文件的的机制，它实现了将内核中读缓冲区地址与用户空间缓冲区地址进行映射，从而实现内核缓冲区与用户缓冲区的共享，从而减少了从读缓冲区到用户缓冲区的一次CPU拷贝。  
&emsp; 即将一个文件或者其它对象映射到进程的地址空间，实现文件磁盘地址和进程虚拟地址空间中一段虚拟地址的一一对映关系；这样就可以省掉原来内核read缓冲区copy数据到用户缓冲区，但是还是需要内核read缓冲区将数据copy到内核socket缓冲区。  

### 1.5.2. 流程
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-73.png)   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-142.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-30.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-100.png)  

&emsp; 整个过程发生了4次用户态和内核态的上下文切换和3次拷贝，具体流程如下：  

1. 用户进程通过mmap()方法向操作系统发起调用，上下文从用户态转向内核态
2. DMA控制器把数据从硬盘中拷贝到读缓冲区
3. 上下文从内核态转为用户态，mmap调用返回
4. 用户进程通过write()方法发起调用，上下文从用户态转为内核态
5. CPU将读缓冲区中数据拷贝到socket缓冲区
6. DMA控制器把数据从socket缓冲区拷贝到网卡，上下文从内核态切换回用户态，write()返回

&emsp; mmap函数的作用相当于是内存共享,将内核空间的内存区域和用户空间共享,这样就避免了将内核空间的数据拷贝到用户空间的步骤,通过mmap函数发送数据时上述的步骤如下:  

&emsp; 第一步:操作系统通过DMA传输将硬盘中的数据复制到内核缓冲区,执行了mmap函数之后,拷贝到内核缓冲区的数据会和用户空间进行共享,所以不需要进行拷贝  
&emsp; 第二步:CPU将内核缓冲区的数据拷贝到内核空间socket缓冲区  
&emsp; 第三步:操作系统通过DMA传输将内核socket缓冲区数据拷贝给网卡发送数据  




### 1.5.3. 小结
&emsp; mmap的方式节省了一次CPU拷贝，同时由于用户进程中的内存是虚拟的，只是映射到内核的读缓冲区，所以可以节省一半的内存空间，比较适合大文件的传输。  

&emsp; 这样就减少了一次用户态和内核态的CPU拷贝，但是在内核空间内仍然有一次CPU拷贝。  
&emsp; **<font color = "clime">「通过mmap实现的零拷贝I/O进行了4次用户空间与内核空间的上下文切换，以及3次数据拷贝；其中3次数据拷贝中包括了2次DMA拷贝和1次CPU拷贝」。</font>**  

&emsp; mmap对大文件传输有一定优势，但是小文件可能出现碎片，并且在多个进程同时操作文件时可能产生引发coredump的signal。  


&emsp; 整个流程中：DMA拷贝2次、CPU拷贝1次、用户空间和内核空间切换4次  

&emsp; 可以发现此种方案避免了内核空间和用户空间之间数据的拷贝工作,但是在内核空间内部还是会有一次数据拷贝过程,而且CPU还是会有从内核空间和用户空间的切换过程  

## 1.6. sendfile
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


### 1.6.1. 简介
&emsp; 相比mmap来说，sendfile同样减少了一次CPU拷贝，而且还减少了2次上下文切换。  
&emsp; sendfile是Linux2.1内核版本后引入的一个系统调用函数：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-147.png)  
&emsp; **<font color = "red">它建立了两个文件之间的传输通道。</font>** 通过使用sendfile数据可以直接在内核空间进行传输，因此避免了用户空间和内核空间的拷贝，同时由于使用sendfile替代了read+write从而节省了一次系统调用，也就是2次上下文切换。  
&emsp; senfile函数的作用是将一个文件描述符的内容发送给另一个文件描述符。而用户空间是不需要关心文件描述符的,所以整个的拷贝过程只会在内核空间操作,相当于减少了内核空间和用户空间之间数据的拷贝过程,而且还避免了CPU在内核空间和用户空间之间的来回切换过程。  


### 1.6.2. 流程 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-148.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-32.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-31.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-149.png)  



&emsp; 整个过程发生了2次用户态和内核态的上下文切换和3次拷贝，具体流程如下：  

1. 用户进程通过sendfile()方法向操作系统发起调用，上下文从用户态转向内核态
2. DMA控制器把数据从硬盘中拷贝到读缓冲区
3. CPU将读缓冲区中数据拷贝到socket缓冲区
4. DMA控制器把数据从socket缓冲区拷贝到网卡，上下文从内核态切换回用户态，sendfile调用返回  

&emsp; 第一步：通过DMA传输将硬盘中的数据复制到内核页缓冲区  
&emsp; 第二步：通过sendfile函数将页缓冲区的数据通过CPU拷贝给socket缓冲区  
&emsp; 第三步：网卡通过DMA传输将socket缓冲区的数据拷贝走并发送数据  

1. 发出sendfile系统调用，导致用户空间到内核空间的上下文切换，然后通过DMA引擎将磁盘文件中的内容复制到内核空间缓冲区中，接着再将数据从内核空间缓冲区复制到socket相关的缓冲区。    
2. sendfile系统调用返回，导致内核空间到用户空间的上下文切换。DMA异步将内核空间socket缓冲区中的数据传递到网卡。    


### 1.6.3. 小结
&emsp; sendfile方式中，应用程序只需要调用sendfile函数即可完成。数据不经过用户缓冲区，该数据无法被修改。但减少了2次状态切换，即只有2次状态切换、1次CPU拷贝、2次DMA拷贝。    

&emsp; sendfile方法IO数据对用户空间完全不可见，所以只能适用于完全不需要用户空间处理的情况，比如静态文件服务器。  

&emsp; 可以看出通过sendfile函数时只会有一次CPU拷贝过程，而且全程都是在内核空间实现的，所以整个过程都不会使得CPU在内核空间和用户空间进行来回切换的操作，性能相比于mmap而言要更好  

&emsp; 但是sendfile在内核缓冲区和socket缓冲区仍然存在一次CPU拷贝，或许这个还可以优化。

## 1.7. sendfile+DMA收集，零拷贝


