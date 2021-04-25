<!-- TOC -->

- [1. Linux网络通讯](#1-linux网络通讯)
    - [1.1. 常见网络命令](#11-常见网络命令)
    - [1.2. Linux网络流量监控工具](#12-linux网络流量监控工具)
        - [1.2.1. iftop](#121-iftop)
        - [1.2.2. nethogs](#122-nethogs)

<!-- /TOC -->

# 1. Linux网络通讯

## 1.1. 常见网络命令  
<!-- 
面试官问我Linux下常见网络命令 
https://mp.weixin.qq.com/s/uMK6QnJKK2MFERkfx6b-QA
Linux 网络分析必备技能：tcpdump 实战详解 
https://mp.weixin.qq.com/s/Tgxdyt1PdVkaNdlZGIDdbA
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/Linux/Linux/linux-5.png)  

* ping命令  
&emsp; ping命令用来测试主机之间网络的连通性。执行ping指令会使用ICMP传输协议，发出要求回应的信息，若远端主机的网络功能没有问题，就会回应该信息，因而得知该主机运作正常。  
* **<font color = "clime">telnet</font>**  
&emsp; telnet命令用于登录远程主机，对远程主机进行管理。telnet因为采用明文传送报文，安全性不好，很多Linux服务器都不开放telnet服务，而改用更安全的ssh方式了。  
* netstat  
&emsp; netstat命令用来打印Linux中网络系统的状态信息，可得知整个Linux系统的网络情况。  
* ifconfig  
&emsp; ifconfig命令被用于配置和显示Linux内核中网络接口的网络参数。用ifconfig命令配置的网卡信息，在网卡重启后机器重启后，配置就不存在。要想将上述的配置信息永远的存的电脑里，那就要修改网卡的配置文件了。  
* route  
&emsp; route命令用来显示并设置Linux内核中的网络路由表，route命令设置的路由主要是静态路由。要注意的是，直接在命令行下执行route命令来添加路由，不会永久保存，当网卡重启或者机器重启之后，该路由就失效了；可以在*/etc/rc.local中添加route命令来保证该路由设置永久有效。  
* arp  
&emsp; arp命令用于操作主机arp缓冲区，可以显示arp缓冲区的所有条目、删除指定条目或增加静态IP地址与MAC地址的对应关系。  
* traceroute  
&emsp; traceroute命令用于追踪数据包在网络上传输时的全部路径，它默认发送的数据包大小是40字节通过traceroute，可以知道信息从计算机到互联网另一端的主机是走的什么路径。当然每次数据包由某一同样的出发点（source）到达某一同样的目的地(destination)走的路径可能会不一样，但基本上来说大部分时候所走的路由是相同的。traceroute通过发送小的数据包到目的设备直到其返回，来测量其需要多长时间。一条路径上的每个设备traceroute要测3次。输出结果中包括每次测试的时间(ms)和设备的名称（如有的话）及其ip地址。  
* host  
&emsp; host命令是常用的分析域名查询工具，可以检测域名系统工作是否正常。  
* tcpdump  
&emsp; tcpdump命令是一款抓取数据包的工具，它可以打印所有经过网络接口的数据包的头信息，也可以使用-w选项将数据包保存到文件中，方便以后分析。  


## 1.2. Linux网络流量监控工具  
<!--
Linux网络流量监控工具 
https://mp.weixin.qq.com/s?__biz=MzU0NjEwMTg4Mg==&mid=2247485997&idx=2&sn=ac694beaf60cbf18d133eb6ebbc7a345&chksm=fb638538cc140c2e581b8c673a9c529359042a2262ec112f8c3476c7408f25e04856f0b37bbd&scene=21#wechat_redirect
-->
&emsp; 几个比较好用的小工具，iftop和nethogs，用于排查linux机器的网络问题。  

### 1.2.1. iftop  
&emsp; iftop用于网卡机器级别的流量监控，可以实时显示当前机器和其他主机之间的网络流量。  

### 1.2.2. nethogs
&emsp; nethogs用于监控统计每个进程的带宽使用。  
