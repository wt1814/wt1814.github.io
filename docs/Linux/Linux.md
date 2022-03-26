

# Linux
<!-- 
为什么 Linux 需要 Swapping 
https://mp.weixin.qq.com/s/XTx6VITeSnGlrRE0_bWH6Q
Linux 系统故障排查，怕了怕了！ 
https://mp.weixin.qq.com/s/110HKE16ABxvPM_Ja7igjws

讲讲用户空间和内核空间
https://mp.weixin.qq.com/s/dK_at5_VSWP2oiIBWowXFQ

Linux内存、Swap、Cache、Buffer详细解析 
https://mp.weixin.qq.com/s/263OGw93GoWs5Jlqv4VrkA

-->


## 1.1. Linux操作系统基本概念


### 1.1.1. 内核空间和用户空间
![image](http://www.wt1814.com/static/view/images/microService/netty/netty-61.png)
* 内核空间：Linux自身使用的空间；主要提供进程调度、内存分配、连接硬件资源等功能
* 用户空间：提供给各个程序进程的空间； **用户空间不具有访问内核空间资源的权限，如果应用程序需要使用到内核空间的资源，则需要通过系统调用来完成：从用户空间切换到内核空间，完成相关操作后再从内核空间切换回用户空间。**

### 1.1.2. 系统调用  
&emsp; 系统调用syscall是应用程序和内核交互的桥梁， **<font color = "clime">每次进行调用/返回就会产生两次切换：</font>**  

* 调用syscall，从用户态切换到内核态
* syscall返回，从内核态切换到用户态

![image](http://www.wt1814.com/static/view/images/microService/netty/netty-70.png)

### 1.1.3. 缓冲区
&emsp; 缓冲区是所有I/O的基础，I/O讲的无非就是把数据移进或移出缓冲区；进程执行I/O操作，就是向操作系统发出请求，让它要么把缓冲区的数据排干(写)，要么填充缓冲区(读)。

### 1.1.4. 虚拟内存
&emsp; 所有现代操作系统都使用虚拟内存，使用虚拟的地址取代物理地址，这样做的好处是：1.一个以上的虚拟地址可以指向同一个物理内存地址， 2.虚拟内存空间可大于实际可用的物理地址。
<!--
&emsp; 利用第一点特性可以把内核空间地址和用户空间的虚拟地址映射到同一个物理地址，这样DMA就可以填充(读写)对内核和用户空间进程同时可见的缓冲区了；大致如下  
![image](http://www.wt1814.com/static/view/images/microService/netty/netty-64.png)  
 -->