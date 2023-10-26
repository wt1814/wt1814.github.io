



# 进程管理   
## top
<!-- 
 10分钟教会你看懂top 
 https://mp.weixin.qq.com/s/R9w9891uvHNm6tXTCvfeJg

Linux 守护进程的启动方法
https://www.ruanyifeng.com/blog/2016/02/linux-daemon.html

**** https://zhuanlan.zhihu.com/p/562569361

-->
&emsp; 它包含了很多方面的数据，例如CPU，内存，系统的任务等等数据。  

&emsp; 运行结果可以分为两部分：  
&emsp; 第一部分是前5行，是系统整体的统计信息；  
&emsp; 第二部分是第8行开始的进程信息，我们从上往下逐行依次进行说明。  

1. 整体统计信息
第二行：
Tasks：当前有多少进程。  
running：正在运行的进程数。  
sleeping：正在休眠的进程数。  
stopped：停止的进程数。  
zombie：僵尸进程数。  


第三行：
us：用户空间占CPU的百分比  
sy：内核空间占CPU的百分比  
sy：内核空间占CPU的百分比  

第四行：  
total：物理内存总量。  
free：空闲内存量。  
used：使用的内存量。  
buffer/cache：用作内核缓存的内存量。  


第五行：

total：交换区内存总量。
free：空闲交换区总量。
used：使用的交换区总量。
buffer/cache：缓冲的交换区总量。  

第四第五行分别是内存信息和swap信息，所有程序的运行都是在内存中进行的，所以内存的性能对与服务器来说非常重要。  

2. 进程信息  
默认情况下仅显示比较重要的 PID、USER、PR、NI、VIRT、RES、SHR、S、%CPU、%MEM、TIME+、COMMAND 列，还有一些参数  




