
# UDP

&emsp; UDP 的全称是 User Datagram Protocol，用户数据报协议。它不需要所谓的握手操作，从而加快了通信速度，允许网络上的其他主机在接收方同意通信之前进行数据传输。  
&emsp; UDP 的特点主要有：  
* UDP 能够支持容忍数据包丢失的带宽密集型应用程序
* UDP 具有低延迟的特点
* UDP 能够发送大量的数据包
* UDP 能够允许 DNS 查找，DNS 是建立在 UDP 之上的应用层协议。

&emsp; **TCP和UDP的区别：**  
* 报文方面：  
    * TCP：面向字节流(把应用层传下来的报文看成字节流，把字节流组织成大小不等的数据块)，首部 20 字节，全双工。  
    * UDP：面向报文(对于应用程序传下来的报文不合并也不拆分，只是添加 UDP 首部)，首部 8 字节。    
* 传输方面：  
    * TCP：只能是点对点，面向连接，提供可靠的服务，有流量控制，拥塞控制，无重复、无丢失、无差错，
    * UDP: 支持一对一、一对多、多对多，无连接，尽最大努力交付，没有拥塞控制， 
* 性能方面： 
    * TCP传输效率慢，所需资源多。
    * UDP传输效率快，所需资源少。
* 应用场景：  
    * TCP，要求通信数据可靠(如文件传输、邮件传输、远程登录)
    * UDP，要求通信速度高(如域名转换、即时通信，如：QQ，直播等)  

&emsp; **使用TCP的协议有哪些？使用UDP的协议有哪些？**  
&emsp; 使用TCP的协议：FTP(文件传输协议)、Telnet(远程登录协议)、SMTP(简单邮件传输协议)、POP3(和SMTP相对，用于接收邮件)、HTTP协议等。  
&emsp; 使用UDP协议包括：TFTP(简单文件传输协议)、SNMP(简单网络管理协议)、DNS(域名解析协议)、NFS、BOOTP。  
