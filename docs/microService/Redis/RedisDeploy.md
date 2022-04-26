
<!-- TOC -->

- [1. Redis高可用](#1-redis高可用)
    - [1.1. 多种架构方式](#11-多种架构方式)
    - [1.2. Redis高可用建设考虑因素](#12-redis高可用建设考虑因素)
    - [1.3. 支持xxx万的QPS](#13-支持xxx万的qps)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; `小型公司，可以采用哨兵，主从复制-单副本模式。`  

# 1. Redis高可用
<!--
一文把Redis主从复制、哨兵、Cluster三种模式摸透 
https://mp.weixin.qq.com/s/GlqoafdmC4Xjf7DACN3srQ

Redis官方的高可用性解决方案 
https://mp.weixin.qq.com/s/8JPBNMGhBsq2jfW9v-H5vQ
***Redis中主、从库宕机如何恢复？
 https://mp.weixin.qq.com/s/pO23ASPrc46BoPkRnQsPXQ
 Redis主从、哨兵、 Cluster集群一锅端！ 
 https://mp.weixin.qq.com/s/u31mfJfY9yUAJp-vQ9us2w

-->

<!-- 
~~
***Redis 生产架构选型解决方案 
https://mp.weixin.qq.com/s/4d-zeaVbQFn7qT4DWagjOg
-->

## 1.1. 多种架构方式 
&emsp; Redis部署方式： **<font color = "red">单机、主从复制模式、哨兵模式、分片模式（包含客户端分片、代理分片、服务器分片即Redis Cluster）。</font>**  
1. Redis单机：  
    &emsp; Redis单机部署一般存在如下几个问题：  
    * 机器故障，导致Redis不可用，数据丢失。  
    * 容量瓶颈：容量不能水平扩展。  
    * QPS 瓶颈：一台机器的处理能力、网络宽带总是有限的，如果能够划分一些流量到其他机器，可以有效解决QPS问题。 
1. [主从模式](/docs/microService/Redis/RedisMasterSlave.md) ：读写分离，备份，一个Master可以有多个Slaves。  
2. [Redis读写分离](/docs/microService/Redis/RedisWriteRead.md)  
3. [哨兵sentinel](/docs/microService/Redis/RedisSentry.md)：监控，自动转移，哨兵发现主服务器挂了后，就会从slave中重新选举一个主服务器。  
4. [分片](/docs/microService/Redis/RedisCluster.md)：为了解决单机Redis容量有限的问题，将数据按一定的规则分配到多台机器，内存/QPS不受限于单机，可受益于分布式集群高扩展性。Redis Cluster是官方的集群。  
&emsp; **<font color = "clime">注：redis集群模式很耗费资源。</font>**  

&emsp; <font color="red">参考：《Redis开发与运维》</font>  


## 1.2. Redis高可用建设考虑因素
1. **<font color = "clime">考虑资源：</font>**    
&emsp; Redis集群最少6个节点，每个节点20G，总共120G。因此Redis集群比较耗资源。小型公司可以采用哨兵模式。    
2. **<font color = "clime">考虑QPS：</font>**  
&emsp; **单机的redis一般是支持上万甚至几万，具体的性能取决于数据操作的复杂性，如果仅仅是简单的kv操作的话，可以达到数万，如果是运行复杂的lua脚本的话，就可能只能到一万左右。**  
&emsp; 缓存一般是用来支撑读的高并发，一般比较少用来支撑读的操作，一般读的操作是比较频繁的，甚至达到几万几十万，但是写的操作每秒才几千，这就需要读写分离了。  

&emsp; `小型公司，可以采用哨兵，主从复制-单副本模式。`  


## 1.3. 支持xxx万的QPS  
<!-- 
https://www.zhihu.com/question/263771630
-->
&emsp; 使用中间件代理的分片模式。同时要考虑网络延迟、数据倾斜等等问题。  
