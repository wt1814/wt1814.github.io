
&emsp; `Socket是对TCP/IP 协议的封装。`Socket 只是个接口不是协议，通过 Socket 才能使用 TCP/IP 协议，除了 TCP，也可以使用 UDP 协议来传递数据。  


# Socket编程  
<!-- 

Socket是tcp、udp的封装。  

HTTP 和 Socket 的区别
https://www.cnblogs.com/zhuleixiao/p/9218121.html

HTTP 和 Socket 的区别
https://www.cnblogs.com/zhuleixiao/p/9218121.html

HTTP 和 Socket 的区别
https://www.cnblogs.com/meier1205/p/5971313.html

socket编程
https://www.cnblogs.com/mingforyou/p/3258418.html
socket技术详解（看清socket编程）
https://blog.csdn.net/weixin_39634961/article/details/80236161
https://www.zhihu.com/question/29637351
https://blog.csdn.net/zhoujn90/article/details/44955137


-->

&emsp; 要想明白 Socket，必须要理解 TCP 连接。  

&emsp; TCP 三次握手：握手过程中并不传输数据，在握手后服务器与客户端才开始传输数据，理想状态下，TCP 连接一旦建立，在通讯双方中的任何一方主动断开连接之前 TCP 连接会一直保持下去。  

&emsp; Socket 是对 TCP/IP 协议的封装，Socket 只是个接口不是协议，通过 Socket 才能使用 TCP/IP 协议，除了 TCP，也可以使用 UDP 协议来传递数据。  

&emsp; 创建 Socket 连接的时候，可以指定传输层协议，可以是 TCP 或者 UDP，当用 TCP 连接，该Socket就是个TCP连接，反之。  

&emsp; Socket 原理  

&emsp; Socket 连接,至少需要一对套接字，分为 clientSocket，serverSocket 连接分为3个步骤:  

&emsp; (1) 服务器监听:服务器并不定位具体客户端的套接字，而是时刻处于监听状态；  

&emsp; (2) 客户端请求:客户端的套接字要描述它要连接的服务器的套接字，提供地址和端口号，然后向服务器套接字提出连接请求；  

&emsp; (3) 连接确认:当服务器套接字收到客户端套接字发来的请求后，就响应客户端套接字的请求,并建立一个新的线程,把服务器端的套接字的描述发给客户端。一旦客户端确认了此描述，就正式建立连接。而服务器套接字继续处于监听状态，继续接收其他客户端套接字的连接请求.  

&emsp; Socket为长连接：通常情况下Socket 连接就是 TCP 连接，因此 Socket 连接一旦建立,通讯双方开始互发数据内容，直到双方断开连接。在实际应用中，由于网络节点过多，在传输过程中，会被节点断开连接，因此要通过轮询高速网络，该节点处于活跃状态。  

 

&emsp; 很多情况下，都是需要服务器端向客户端主动推送数据，保持客户端与服务端的实时同步。  

&emsp; 若双方是 Socket 连接，可以由服务器直接向客户端发送数据。  

&emsp; 若双方是 HTTP 连接，则服务器需要等客户端发送请求后，才能将数据回传给客户端。  

&emsp; 因此，客户端定时向服务器端发送请求，不仅可以保持在线，同时也询问服务器是否有新数据，如果有就将数据传给客户端。  

----------

&emsp; Socket本身有“插座”的意思，不是Java中特有的概念，而是一个语言无关的标准，任何可以实现网络编程的编程语言都有Socket。在Linux环境下，用于表示进程间网络通信的特殊文件类型，其本质为内核借助缓冲区形成的伪文件。既然是文件，那么理所当然的，可以使用文件描述符引用套接字。   
&emsp; 与管道类似的，Linux系统将其封装成文件的目的是为了统一接口，使得读写套接字和读写文件的操作一致。区别是管道主要应用于本地进程间通信，而套接字多应用于网络进程间数据的传递。  
&emsp; 可以这么理解：Socket就是网络上的两个应用程序通过一个双向通信连接实现数据交换的编程接口API。  

&emsp; Socket通信的基本流程具体步骤如下所示：  
1. 服务端通过Listen开启监听，等待客户端接入。
2. 客户端的套接字通过Connect连接服务器端的套接字，服务端通过Accept接收客户端连接。在connect-accept过程中，操作系统将会进行三次握手。
3. 客户端和服务端通过write和read发送和接收数据，操作系统将会完成TCP数据的确认、重发等步骤。
4. 通过close关闭连接，操作系统会进行四次挥手。

&emsp; 针对Java编程语言，java.net包是网络编程的基础类库。其中ServerSocket和Socket是网络编程的基础类型。  
&emsp; SeverSocket是服务端应用类型。Socket是建立连接的类型。当连接建立成功后，服务器和客户端都会有一个Socket对象示例，可以通过这个Socket对象示例，完成会话的所有操作。对于一个完整的网络连接来说，Socket是平等的，没有服务器客户端分级情况。  

&emsp; 一般很少直接使用socket来编程，使用框架比较多，而netty就是其中一种框架。  
