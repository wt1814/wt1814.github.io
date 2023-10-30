

&emsp; [Linux命令-实操](/docs/Linux/operation.md)  
&emsp; [Linux命令](/docs/Linux/LinuxCommand.md)  
&emsp; &emsp; [文本处理](/docs/Linux/textProcessing.md)  
&emsp; &emsp; [网络通讯](/docs/Linux/NetworkCommunication.md)  
&emsp; &emsp; [进程管理](/docs/Linux/ProcessManagement.md)  
&emsp; [shell编程](/docs/Linux/shell.md)  

1. 文件夹：
2. 文件操作：cp、mv、find，查找文件  
3. 文件传输：
    1. ftp，在本地主机和远程主机之间或者在两个远程主机之间进行文件传输；  
    2. scp，在网络上的主机之间拷贝文件；
    3. curl，
4. 文件内容操作：
    1. 查看：cat、more、tail  
    2. 编辑：vim、grep、sed  
5. 进程管理：  
    1. ps，查找出进程的信息
    2. free，查看内存使用状况
    3. top，查看实时刷新的系统进程信息
6. 网络：  
    1. ping，检测网络连通性
    2. lsof，查看指定IP 和/或 端口的进程的当前运行情况  


---------------
&emsp; top命令：它包含了很多方面的数据，例如CPU，内存，系统的任务等等数据。  

&emsp; 运行结果可以分为两部分：   
&emsp; 第一部分是前5行，是系统整体的统计信息；   
&emsp; 第二部分是第8行开始的进程信息，我们从上往下逐行依次进行说明。   

1. 整体统计信息  
&emsp; 第二行：Tasks：当前有多少进程。running：正在运行的进程数。sleeping：正在休眠的进程数。stopped：停止的进程数。zombie：僵尸进程数。  
&emsp; 第三行：us：用户空间占CPU的百分比。sy：内核空间占CPU的百分比。sy：内核空间占CPU的百分比   
&emsp; 第四行：total：物理内存总量。free：空闲内存量。used：使用的内存量。buffer/cache：用作内核缓存的内存量。  
&emsp; 第五行：total：交换区内存总量。free：空闲交换区总量。used：使用的交换区总量。buffer/cache：缓冲的交换区总量。  
&emsp; 第四第五行分别是内存信息和swap信息，所有程序的运行都是在内存中进行的，所以内存的性能对与服务器来说非常重要。  

2. 进程信息  
&emsp; 默认情况下仅显示比较重要的 PID、USER、PR、NI、VIRT、RES、SHR、S、%CPU、%MEM、TIME+、COMMAND 列，还有一些参数  

