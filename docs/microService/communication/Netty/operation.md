


# 运行流程  
&emsp; **Netty服务器执行流程概述：**  
1. 创建服务器启动辅助类，服务端是ServerBootstrap。需要设置事件循环组EventLoopGroup，如果使用reactor主从模式，需要创建2个：创建boss线程组用于服务端接受客户端的连接；创建worker线程组用于进行 SocketChannel的数据读写。  
2. 对ServerBootstrap进行配置，配置项有channel,handler,option。  
3. 绑定服务器端口并启动服务器，同步等待服务器启动完毕。  
4. 阻塞启动线程，并同步等待服务器关闭，因为如果不阻塞启动线程，则会在finally块中执行优雅关闭，导致服务器也会被关闭了。  

&emsp; **Netty客户端执行流程概述：(与服务端类似)**  
1. 创建服务端启动辅助类Bootstrap。需要设置事件循环组EventLoopGroup。  
2. 对Bootstrap进行配置，配置项有channel,handler,option。  
3. 绑定端口并启动服务端，同步等待服务器启动完毕。  
4. 阻塞启动线程，并同步等待服务器关闭，因为如果不阻塞启动线程，则会在finally块中执行优雅关闭，导致服务器也会被关闭了。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-87.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-44.png)  
