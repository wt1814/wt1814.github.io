

<!-- TOC -->

- [1. TCP](#1-tcp)
    - [1.1. TCP简介](#11-tcp简介)
    - [1.2. TCP头部格式](#12-tcp头部格式)
    - [1.3. 工作方式](#13-工作方式)
        - [1.3.1. 连接、释放阶段](#131-连接释放阶段)
            - [1.3.1.1. 三次握手，建立连接](#1311-三次握手建立连接)
                - [1.3.1.1.1. 流程](#13111-流程)
                - [1.3.1.1.2. 常见问题](#13112-常见问题)
            - [1.3.1.2. 四次挥手，连接终止](#1312-四次挥手连接终止)
                - [1.3.1.2.1. 流程](#13121-流程)
                - [1.3.1.2.2. ~~常见问题~~](#13122-常见问题)
        - [1.3.2. socket的状态小结](#132-socket的状态小结)
        - [1.3.3. ~~传输阶段~~](#133-传输阶段)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. TCP头部格式  
    ![image](http://182.92.69.8:8081/img/network/TCP-3.png)   

    * <font color = "red">序列号seq</font>：在建立连接时由计算机生成的随机数作为其初始值，通过 SYN 包传给接收端主机，每发送一次数据，就「累加」一次该「数据字节数」的大小。用来解决网络包乱序问题。  
    * <font color = "red">确认应答号ack</font>：指下一次「期望」收到的数据的序列号，发送端收到这个确认应答以后可以认为在这个序号以前的数据都已经被正常接收。用来解决不丢包的问题。  
    * 控制位：  
        * URG(紧急)：指示报文中有紧急数据，应尽快传送(相当于高优先级的数据)。
        * <font color = "red">ACK(确认)</font>：确认序号标志，为1时表示确认号有效，为0表示报文中不含确认信息，忽略确认号字段。
        * PSH(传送)：为1表示是带有push标志的数据，指示接收方在接收到该报文段以后，应尽快将这个报文段交给应用程序，而不是在缓冲区排队。
        * RST(重置)：TCP连接中出现严重差错(如主机崩溃)，必须释放连接，在重新建立连接。
        * <font color = "red">SYN(连接)</font>：同步序号，用于建立连接过程。(Synchronize Sequence Numbers)
        * <font color = "red">FIN(结束)</font>：发送端已完成数据传输，请求释放连接。 

2. 三次握手  
    ![image](http://182.92.69.8:8081/img/network/TCP-1.png)  
    1. 为什么只有三次握手才能确认双方的接受与发送能力是否正常，而两次却不可以？  
    &emsp; <font color = "clime">第三次握手中，客户端向服务器发送确认包ACK，防止了服务器端的一直等待而浪费资源。</font>例如：已失效的连接请求报文突然又传送到了服务器，从而会产生错误。  

3. 四次挥手  
    ![image](http://182.92.69.8:8081/img/network/TCP-2.png)  
    1. **<font color = "clime">Client收到服务端F1N后，Client进入TIME_WAIT状态。</font>** 2MSL后自动关闭。 
    2. 为什么客户端最后还要等待2MSL？  
    &emsp; <font color = "red">保证客户端发送的最后一个`ACK报文`能够到达服务器，因为这个ACK报文可能丢失。</font>  
    &emsp; `站在服务器的角度看来，服务端已经发送了FIN+ACK报文请求断开了，客户端还没有给回应，应该是服务器发送的请求断开报文它没有收到，于是服务器又会重新发送一次。而客户端就能在这个2MSL时间段内收到这个重传的报文，接着给出回应报文，`**<font color = "clime">并且会重启2MSL计时器。</font>**  
&emsp;如果客户端收到服务端的FIN+ACK报文后，发送一个ACK给服务端之后就“自私”地立马进入CLOSED状态，可能会导致服务端无法确认收到最后的ACK指令，也就无法进入CLOSED状态，这是客户端不负责任的表现。  

4. **<font color = "blue">`如果已经建立了连接，但是客户端突然出现故障了怎么办？`</font>**   
&emsp; 客户端如果出现故障，服务器不能一直等下去，白白浪费资源。`在TCP设有一个保活计时器。`<font color = "clime">服务器每收到一次客户端的请求后都会重新复位这个计时器，时间通常是设置为2小时，若两小时还没有收到客户端的任何数据，服务器就会发送一个探测报文段，以后每隔75分钟发送一次。若一连发送10个探测报文仍然没反应，服务器就认为客户端出了故障，接着就关闭连接。</font>  

# 1. TCP  
<!--
***** TCP
https://mp.weixin.qq.com/s/-t6fS_Hif9jDPsuMlWWeyQ

面试再不怕问TCP了
https://blog.csdn.net/a159357445566/article/details/106411680/
-->

## 1.1. TCP简介  
&emsp; 传输控制协议(TCP，Transmission Control Protocol)是一种面向<font color = "red">连接的、可靠的、基于字节流的</font>传输层通信协议。  

## 1.2. TCP头部格式  
![image](http://182.92.69.8:8081/img/network/TCP-3.png)   

* <font color = "red">序列号seq</font>：在建立连接时由计算机生成的随机数作为其初始值，通过 SYN 包传给接收端主机，每发送一次数据，就「累加」一次该「数据字节数」的大小。用来解决网络包乱序问题。  
* <font color = "red">确认应答号ack</font>：指下一次「期望」收到的数据的序列号，发送端收到这个确认应答以后可以认为在这个序号以前的数据都已经被正常接收。用来解决不丢包的问题。  
* 控制位：  
    * URG(紧急)：指示报文中有紧急数据，应尽快传送(相当于高优先级的数据)。
    * <font color = "red">ACK(确认)</font>：确认序号标志，为1时表示确认号有效，为0表示报文中不含确认信息，忽略确认号字段。
    * PSH(传送)：为1表示是带有push标志的数据，指示接收方在接收到该报文段以后，应尽快将这个报文段交给应用程序，而不是在缓冲区排队。
    * RST(重置)：TCP连接中出现严重差错(如主机崩溃)，必须释放连接，在重新建立连接。
    * <font color = "red">SYN(连接)</font>：同步序号，用于建立连接过程。(Synchronize Sequence Numbers)
    * <font color = "red">FIN(结束)</font>：发送端已完成数据传输，请求释放连接。 

## 1.3. 工作方式 
### 1.3.1. 连接、释放阶段   
#### 1.3.1.1. 三次握手，建立连接  
&emsp; 在TCP/IP协议中，TCP协议提供可靠的连接服务，采用三次握手建立一个连接。 **<font color = "clime">三次握手主要目的是：信息对等和防止超时。</font>**  
&emsp; TCP三次握手的过程掌握最重要的两点就是客户端和服务端状态的变化和三次握手过程标志信息的变化。  

##### 1.3.1.1.1. 流程  
![image](http://182.92.69.8:8081/img/network/TCP-1.png)  
&emsp; 初始状态：客户端处于closed(关闭) 状态，服务端处于listen(监听) 状态。  
&emsp; 第一次握手：建立连接时，客户端发送SYN包(SYN=1，sql=x)到服务器。并进入SYN_SEND发送等待状态，等待服务器确认；  
&emsp; 第二次握手： **<font color = "red">服务器收到SYN包，必须确认客户的SYN(ACK=1，ack+1)，**</font> 同时自己也发送一个SYN包(SYN=1，sql=Y)，即SYN+ACK包，此时服务器进入SYN_RECV确认接收状态；  
&emsp; 第三次握手：客户端收到服务器的SYN+ACK包，向服务器发送确认包ACK(ACK=1，ack=y+l)，此包发送完毕，客户端和服务器进入ESTABLISHED确认连接状态，完成三次握手。  

##### 1.3.1.1.2. 常见问题  
1. **为什么只有三次握手才能确认双方的接受与发送能力是否正常，而两次却不可以？**  
&emsp; <font color = "clime">第三次握手中，客户端向服务器发送确认包ACK。防止了服务器端的一直等待而浪费资源。</font>例如：已失效的连接请求报文突然又传送到了服务器，从而会产生错误。  
&emsp; 如果使用的是两次握手建立连接，假设有这样一种场景，客户端发送了第一个请求连接并且没有丢失，只是因为在网络结点中滞留的时间太长了，由于TCP的客户端迟迟没有收到确认报文，<font color = "red">以为服务器没有收到，此时重新向服务器发送这条报文</font>，此后客户端和服务器经过两次握手完成连接，传输数据，然后关闭连接。<font color = "red">此时此前滞留的那一次请求连接，网络通畅了到达了服务器，这个报文本该是失效的，但是，两次握手的机制将会让客户端和服务器再次建立连接，这将导致不必要的错误和资源的浪费。</font>  
&emsp; 如果采用的是三次握手，就算是那一次失效的报文传送过来了，服务端接受到了那条失效报文并且回复了确认报文，但是客户端不会再次发出确认。由于服务器收不到确认，就知道客户端并没有请求连接。  

2. **三次握手过程中可以携带数据吗？**  
&emsp; <font color = "clime">第三次握手的时候，是可以携带数据的。</font>但是，第一次、第二次握手不可以携带数据。  
&emsp; 假如第一次握手可以携带数据的话，如果有人要恶意攻击服务器，那攻击者每次都在第一次握手中的 SYN 报文中放入大量的数据。因为攻击者根本就不理服务器的接收、发送能力是否正常，然后疯狂着重复发 SYN 报文的话，这会让服务器花费很多时间、内存空间来接收这些报文。  
&emsp; 也就是说，第一次握手不可以放数据，其中一个简单的原因就是会让服务器更加容易受到攻击了。<font color = "red">而对于第三次的话，此时客户端已经处于 ESTABLISHED 状态。客户端已经建立起连接了，并且也已经知道服务器的接收、发送能力是正常的了，所以能携带数据。</font>  

#### 1.3.1.2. 四次挥手，连接终止  
##### 1.3.1.2.1. 流程  
![image](http://182.92.69.8:8081/img/network/TCP-2.png)  
&emsp; TCP采用四次挥手来释放连接：刚开始双方都处于establised状态。  
1. 第一次挥手：第一次挥手无论是客户端还是服务端都可以发起，因为TCP是全双工的。假如是客户端先发起关闭请求，Client发送一个FIN，用来关闭Client到Server的数据传送，Client进入FIN_WAIT_1状态；  
2. 第二次挥手：Server收到FIN后，发送一个ACK给Client，确认序号为收到序号+1 (与SYN相同，一个FIN占用一个序号)，<font color = "red">Server进入CLOSE_WAIT状态；</font>  
3. 第三次挥手：Server发送一个FIN，用来关闭Server到Client的数据传送，Server进入LAST_ACK状态；  
4. 第四次挥手： **<font color = "clime">Client收到服务端F1N后，Client进入TIME_WAIT状态，</font>** 接着发送一个ACK给Server，确认序号为收到序号+1，Server进入CLOSED状态，完成四次挥手。  
&emsp; **<font color = "clime">客户端会在2MSL后，会自动关闭。</font>**  

##### 1.3.1.2.2. ~~常见问题~~  
**<font color = "clime">1. 为什么建立连接是三次握手，关闭连接确是四次挥手呢？</font>**  
&emsp; <font color = "clime">全双工半关闭：</font>TCP连接是全双工的(即数据可在两个方向上同时传递)，而进行关闭时每个方向上都单独进行关闭。  
&emsp; 关闭连接时，服务器收到对方的FIN报文时，仅仅表示对方不再发送数据了但是还能接收数据，而自己也未必全部数据都发送给对方了，所以己方可以立即关闭，也可以发送一些数据给对方后，再发送FIN报文给对方来表示同意现在关闭连接，因此，己方ACK和FIN一般都会分开发送，从而导致多了一次。    

**<font color = "clime">2. 为什么客户端最后还要等待2MSL？</font>**  
&emsp; MSL是Maximum Segment Lifetime的英文缩写，可译为“最长报文段寿命”，它是任何报文在网络上存在的最长时间，超过这个时间报文将被丢弃。2MSL等待状态也称为TIME_WAIT状态。   

&emsp; `为什么客户端最后还要等待2MSL？`  
1. <font color = "red">保证客户端发送的最后一个ACK报文能够到达服务器，因为这个ACK报文可能丢失。</font>  
&emsp; 站在服务器的角度看来，服务端已经发送了FIN+ACK报文请求断开了，客户端还没有给回应，应该是服务器发送的请求断开报文它没有收到，于是服务器又会重新发送一次。而客户端就能在这个2MSL时间段内收到这个重传的报文，接着给出回应报文， **<font color = "clime">并且会重启2MSL计时器。</font>**  
&emsp;如果客户端收到服务端的FIN+ACK报文后，发送一个ACK给服务端之后就“自私”地立马进入CLOSED状态，可能会导致服务端无法确认收到最后的ACK指令，也就无法进入CLOSED状态，这是客户端不负责任的表现。  
2. <font color = "red">~~防止失效请求。防止类似与“三次握手”中提到了的“已经失效的连接请求报文段”出现在本连接中。~~</font>   
&emsp; 客户端发送完最后一个确认报文后，在这个2MSL时间中，就可以使本连接持续的时间内所产生的所有报文段都从网络中消失。这样新的连接中不会出现旧连接的请求报文。  

**<font color = "clime">3. 如果已经建立了连接，但是客户端突然出现故障了怎么办？</font>**  
&emsp; 客户端如果出现故障，服务器不能一直等下去，白白浪费资源。<font color = "red">在TCP设有一个保活计时器。服务器每收到一次客户端的请求后都会重新复位这个计时器，时间通常是设置为2小时，若两小时还没有收到客户端的任何数据，服务器就会发送一个探测报文段，以后每隔75分钟发送一次。若一连发送10个探测报文仍然没反应，服务器就认为客户端出了故障，接着就关闭连接。</font>  


### 1.3.2. socket的状态小结  
<!-- 
https://blog.csdn.net/zzhongcy/article/details/38851271
-->

### 1.3.3. ~~传输阶段~~  
&emsp; [TCP传输阶段](/docs/network/TCPTransfer.md)  

