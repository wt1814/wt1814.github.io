---
title: Redis集群
date: 2020-05-17 00:00:00
tags:
    - Redis
---

Redis部署方式：单机、分片模式、主从复制模式、哨兵模式、集群模式。  

# 分片模式  
&emsp; 分片(sharding)是将数据拆分到多个Redis实例的过程，这样每个实例将只包含所有键的子集，这种方法在解决某些问题时可以获得线性级别的性能提升。  
&emsp; 假设有4个Redis实例R0, R1, R2, R3, 还有很多表示用户的键user:1, user:2, … , 有不同的方式来选择一个指定的键存储在哪个实例中。  
&emsp; 最简单的是范围分片，例如用户id从0 ~ 1000的存储到实例R0中，用户id从1001 ~ 2000的存储到实例R1中，等等。但是这样需要维护一张映射范围表，维护操作代价高。  
&emsp; 还有一种是哈希分片。使用CRC32哈希函数将键转换为一个数字，再对实例数量求模就能知道存储的实例。  
&emsp; 根据执行分片的位置，可以分为三种分片方式：  
* 客户端分片：客户端使用一致性哈希等算法决定应当分布到哪个节点。
* 代理分片：将客户端的请求发送到代理上，由代理转发到正确的节点上。
* 服务器分片：Redis Cluster。

&emsp; 基于客户端分片  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-18.png)  
&emsp; Redis Sharding是Redis Cluster出来之前，业界普遍使用的多Redis实例集群方法。其主要思想是基于哈希算法，根据Redis数据的key的哈希值对数据进行分片，将数据映射到各自节点上。  
&emsp; 优点在于实现简单，缺点在于当Redis集群调整，每个客户端都需要更新调整。  
&emsp; ***在redis3.0版本之前的版本，可以通过redis客户端做sharding分片，比如jedis实现的ShardedJedisPool。***  
&emsp; 基于代理服务器分片
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-19.png)  
&emsp; 客户端发送请求到独立部署代理组件，代理组件解析客户端的数据，并将请求转发至正确的节点，最后将结果回复给客户端。  
&emsp; 优点在于透明接入，容易集群扩展，缺点在于多了一层代理转发，性能有所损耗。  


# 主从复制模式：  









