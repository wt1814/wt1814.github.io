

# Netty高性能  
<!-- 
关键架构属性
《Netty权威指南》第20章
-->
&emsp; Netty高性能  

* 异步非阻塞通信  
* [高效的Reactor线程模型](/docs/microService/communication/Netty/Reactor.md) 
* [零拷贝](/docs/microService/communication/Netty/nettyZeroCopy.md)  
* 灵活的TCP参数配置能力
* 内存池
* 无锁化的串行设计理念  
* 高效的并发编程  
* 对高性能对的序列化框架支持


------

Netty 高性能表现在哪些方面？  

IO 线程模型：同步非阻塞，用最少的资源做更多的事。  
内存零拷贝：尽量减少不必要的内存拷贝，实现了更高效率的传输。  
内存池设计：申请的内存可以重用，主要指直接内存。内部实现是用一颗二叉查找树管理内存分配情况。  
串形化处理读写：避免使用锁带来的性能开销。  
高性能序列化协议：支持 protobuf 等高性能序列化协议。
