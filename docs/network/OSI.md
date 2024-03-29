


# 1. OSI七层网络模型  
<!-- 
三天两夜肝完这篇万字长文，终于拿下了TCP/IP 
https://mp.weixin.qq.com/s/-_wgv7Ipo4Ke3-gtlYcoIg
计算机网络基础知识总结 
https://mp.weixin.qq.com/s/8u0GTnLkj7pP_Lh67Z1UGQ
一文讲懂什么是vlan、三层交换机、网关、DNS、子网掩码、MAC地址 
https://mp.weixin.qq.com/s/KM-mWwd5TivJScBVCQyEBQ
如何系统学习计算机网络？ 
https://mp.weixin.qq.com/s/wL5YI5TZXl1zRY8qS--awQ


OSI七层网络模型
https://blog.csdn.net/qfikh/article/details/52661745
 一图了解，网络7层协议之间的关系 
https://mp.weixin.qq.com/s/CTt0E5-B3Z_xCYEU6K_OVA
 36张图详解网络基础知识 
https://mp.weixin.qq.com/s/7KqYI2CDIOKusrSsb4tEeQ
 75张图带你了解网络设备、网络地址规划、静态路由、实战演练 
https://mp.weixin.qq.com/s/cHVsxIDPo8wXPTqvvdnvXQ
-->


&emsp; 为了使不同计算机厂家生产的计算机能够相互通信，以便在更大的范围内建立计算机网络，国际标准化组织（ISO）在1978年提出了“开放系统互联参考模型”，即著名的OSI/RM模型（Open System Interconnection/Reference Model）。  
&emsp; 除了标准的OSI七层模型以外，常见的网络层次划分还有TCP/IP四层协议以及TCP/IP五层协议，它们之间的对应关系如下图所示：  
![image](http://182.92.69.8:8081/img/network/osi-1.png)  

----
&emsp; OSI七层网络模型：
![image](http://182.92.69.8:8081/img/network/osi-2.png)  

&emsp; 7. 应用层  
&emsp; 与其它计算机进行通讯的一个应用，它是为应用程序的通信服务的。  

&emsp; 6. 表示层  
&emsp; 这一层的主要功能是定义数据格式及加密。例如，FTP允许选择以二进制或ASCII格式传输。如果选择二进制，那么发送方和接收方不改变文件的内容。如果选择ASCII格式，发送方将把文本从发送方的字符集转换成标准的ASCII后发送数据。在接收方将标准的ASCII转换成接收方计算机的字符集。示例：加密，ASCII等。  

&emsp; 5. 会话层  
&emsp; 它定义了如何开始、控制和结束一个会话，包括对多个双向消息的控制和管理，以便在只完成连续消息的一部分时可以通知应用，从而使表示层看到的数据是连续的，在某些情况下，如果表示层收到了所有的数据，则用数据代表表示层。示例：RPC，SQL等。  

&emsp; 4. 传输层  
&emsp; 这层的功能包括是否选择差错恢复协议还是无差错恢复协议，及在同一主机上对不同应用的数据流的输入进行复用，还包括对收到的顺序不对的数据包的重新排序功能。示例：TCP，UDP，SPX。  

&emsp; 3. 网络层  
&emsp; 这层对端到端的包传输进行定义，它定义了能够标识所有结点的逻辑地址，还定义了路由实现的方式和学习的方式。为了适应最大传输单元长度小于包长度的传输介质，网络层还定义了如何将一个包分解成更小的包的分段方法。示例：IP，IPX等。  

&emsp; 2. 数据链路层  
&emsp; 它定义了在单个链路上如何传输数据。这些协议与被讨论的各种介质有关。示例：ATM，FDDI等。  

&emsp; 1. 物理层  
&emsp; OSI的物理层规范是有关传输介质的特性，这些规范通常也参考了其他组织制定的标准。连接头、帧、帧的使用、电流、编码及光调制等都属于各种物理层规范中的内容。物理层常用多个规范完成对所有细节的定义。示例：Rj45，802.3等。  
