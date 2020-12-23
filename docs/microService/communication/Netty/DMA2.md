
<!-- TOC -->

- [1. 零拷贝](#1-零拷贝)
    - [1.1. Linux操作系统基本概念](#11-linux操作系统基本概念)
        - [1.1.1. 内核空间和用户空间](#111-内核空间和用户空间)
        - [1.1.2. 缓冲区](#112-缓冲区)
        - [1.1.3. 虚拟内存](#113-虚拟内存)
    - [1.2. 数据拷贝过程](#12-数据拷贝过程)
        - [1.2.1. 仅CPU方式](#121-仅cpu方式)
        - [1.2.2. CPU&DMA方式](#122-cpudma方式)
            - [1.2.2.1. DMA介绍](#1221-dma介绍)
            - [1.2.2.2. 普通模式数据交互详解](#1222-普通模式数据交互详解)
    - [1.3. 零拷贝技术](#13-零拷贝技术)
        - [1.3.1. 出现原因](#131-出现原因)
        - [1.3.2. 零拷贝技术实现方式](#132-零拷贝技术实现方式)

<!-- /TOC -->

# 1. 零拷贝  
<!-- 

小白也能秒懂的Linux零拷贝原理 
https://mp.weixin.qq.com/s/SKuNuC3kSGGor0xwArzvcg
关于零拷贝的一点认识 
https://mp.weixin.qq.com/s/KD2cpeUviLcEE3wrfdnrlQ

零拷贝
https://mp.weixin.qq.com/s/mWPjFbCVzvuAW3Y9lEQbGg
https://mp.weixin.qq.com/s/LAWUHrRSnxKKicHz1FiGVw
-->


## 1.1. Linux操作系统基本概念  

### 1.1.1. 内核空间和用户空间  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-61.png)  
* 内核空间：Linux自身使用的空间；主要提供进程调度、内存分配、连接硬件资源等功能  
* 用户空间：提供给各个程序进程的空间；用户空间不具有访问内核空间资源的权限，如果应用程序需要使用到内核空间的资源，则需要通过系统调用来完成：从用户空间切换到内核空间，完成相关操作后再从内核空间切换回用户空间  

### 1.1.2. 缓冲区  
&emsp; 缓冲区是所有I/O的基础，I/O讲的无非就是把数据移进或移出缓冲区；进程执行I/O操作，就是向操作系统发出请求，让它要么把缓冲区的数据排干(写)，要么填充缓冲区(读)。  

### 1.1.3. 虚拟内存  
&emsp; 所有现代操作系统都使用虚拟内存，使用虚拟的地址取代物理地址，这样做的好处是：1.一个以上的虚拟地址可以指向同一个物理内存地址， 2.虚拟内存空间可大于实际可用的物理地址。  
<!--
&emsp; 利用第一点特性可以把内核空间地址和用户空间的虚拟地址映射到同一个物理地址，这样DMA就可以填充(读写)对内核和用户空间进程同时可见的缓冲区了；大致如下  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-64.png)  
 -->

## 1.2. 数据拷贝过程  
&emsp; 在Linux系统内部缓存和内存容量都是有限的，更多的数据都是存储在磁盘中。对于Web服务器来说，经常需要从磁盘中读取数据到内存，然后再通过网卡传输给用户  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-65.png)  
&emsp; 上述数据流转只是概述，接下来看看几种模式。  

### 1.2.1. 仅CPU方式  
* 当应用程序需要读取磁盘数据时，调用read()从用户态陷入内核态，read()这个系统调用最终由CPU来完成；
* CPU向磁盘发起I/O请求，磁盘收到之后开始准备数据；
* 磁盘将数据放到磁盘缓冲区之后，向CPU发起I/O中断，报告CPU数据已经Ready了；
* CPU收到磁盘控制器的I/O中断之后，开始拷贝数据，完成之后read()返回，再从内核态切换到用户态；  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-66.png)  

### 1.2.2. CPU&DMA方式  
#### 1.2.2.1. DMA介绍
&emsp; 直接内存访问（Direct Memory Access）（DMA）：DMA允许外设设备和内存存储器之间直接进行IO数据传输，其过程不需要CPU的参与。  

<!-- 
CPU的时间宝贵，让它做杂活就是浪费资源。

直接内存访问（Direct Memory Access），是一种硬件设备绕开CPU独立直接访问内存的机制。所以DMA在一定程度上解放了CPU，把之前CPU的杂活让硬件直接自己做了，提高了CPU效率。

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-63.png)  
-->

&emsp; 目前支持DMA的硬件包括：网卡、声卡、显卡、磁盘控制器等。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-67.png)  
&emsp; 有了DMA的参与之后的流程发生了一些变化：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-68.png)    
&emsp; 最主要的变化是，CPU不再和磁盘直接交互，而是DMA和磁盘交互并且将数据从磁盘缓冲区拷贝到内核缓冲区，之后的过程类似。  

&emsp; **注：无论从仅CPU方式和DMA&CPU方式，都存在多次冗余数据拷贝和内核态&用户态的切换。**  
&emsp; 继续思考Web服务器读取本地磁盘文件数据再通过网络传输给用户的详细过程。  

#### 1.2.2.2. 普通模式数据交互详解  
&emsp; 一次完成的数据交互包括几个部分：系统调用syscall、CPU、DMA、网卡、磁盘等。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-69.png)   
&emsp; 系统调用syscall是应用程序和内核交互的桥梁，每次进行调用/返回就会产生两次切换：  

* 调用syscall 从用户态切换到内核态
* syscall返回 从内核态切换到用户态  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-70.png)   

&emsp; 来看下完整的数据拷贝过程简图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-71.png)   

&emsp; 读数据过程：  

* 应用程序要读取磁盘数据，调用read()函数从而实现用户态切换内核态，这是第1次状态切换；
* DMA控制器将数据从磁盘拷贝到内核缓冲区，这是第1次DMA拷贝；
* CPU将数据从内核缓冲区复制到用户缓冲区，这是第1次CPU拷贝；
* CPU完成拷贝之后，read()函数返回实现用户态切换用户态，这是第2次状态切换；

&emsp; 写数据过程：  

* 应用程序要向网卡写数据，调用write()函数实现用户态切换内核态，这是第1次切换；
* CPU将用户缓冲区数据拷贝到内核缓冲区，这是第1次CPU拷贝；
* DMA控制器将数据从内核缓冲区复制到socket缓冲区，这是第1次DMA拷贝；
* 完成拷贝之后，write()函数返回实现内核态切换用户态，这是第2次切换；

&emsp; 综上所述：

* 读过程涉及2次空间切换、1次DMA拷贝、1次CPU拷贝；
* 写过程涉及2次空间切换、1次DMA拷贝、1次CPU拷贝；

&emsp; 可见传统模式下，涉及多次空间切换和数据冗余拷贝，效率并不高  

## 1.3. 零拷贝技术  

### 1.3.1. 出现原因
&emsp; 如果应用程序不对数据做修改，从内核缓冲区到用户缓冲区，再从用户缓冲区到内核缓冲区。两次数据拷贝都需要CPU的参与，并且涉及用户态与内核态的多次切换，加重了CPU负担。  
&emsp; 需要降低冗余数据拷贝、解放CPU，这也就是零拷贝Zero-Copy技术。   

### 1.3.2. 零拷贝技术实现方式  
&emsp; 目前，零拷贝技术的几个实现手段包括：mmap+write、sendfile、sendfile+DMA收集、splice等。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-72.png)   


