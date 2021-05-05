

<!--
一文把Redis主从复制、哨兵、Cluster三种模式摸透 
https://mp.weixin.qq.com/s/GlqoafdmC4Xjf7DACN3srQ

Redis官方的高可用性解决方案 
https://mp.weixin.qq.com/s/8JPBNMGhBsq2jfW9v-H5vQ
-->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-51.png)  

&emsp; Redis部署方式： **<font color = "red">单机、主从复制模式、哨兵模式、分片模式（包含客户端分片、代理分片、服务器分片即Redis Cluster）。</font>**  
1. Redis单机：  
    &emsp; Redis单机部署一般存在如下几个问题：  

    * 机器故障，导致Redis不可用，数据丢失。  
    * 容量瓶颈：容量不能水平扩展。  
    * QPS 瓶颈：一台机器的处理能力、网络宽带总是有限的，如果能够划分一些流量到其他机器，可以有效解决QPS问题。 
1. [主从模式](/docs/microService/Redis/Redis主从复制.md) ：读写分离，备份，一个Master可以有多个Slaves。  
2. [哨兵sentinel](/docs/microService/Redis/Redis哨兵模式.md)：监控，自动转移，哨兵发现主服务器挂了后，就会从slave中重新选举一个主服务器。  
3. [分片](/docs/microService/Redis/Redis分片模式.md)：为了解决单机Redis容量有限的问题，将数据按一定的规则分配到多台机器，内存/QPS不受限于单机，可受益于分布式集群高扩展性。Redis Cluster是官方的集群。  
&emsp; **<font color = "clime">注：redis集群模式很耗费资源。</font>**  

&emsp; <font color="red">参考：《Redis开发与运维》</font>  
