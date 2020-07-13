---
title: Redis集群
date: 2020-05-17 00:00:00
tags:
    - Redis
---


![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-51.png)  

&emsp; Redis部署方式：单机、主从复制模式、哨兵模式、分片模式（包含客户端分片、代理分片、服务器分片即Redis Cluster）。    
&emsp; <font color="red">参考《Redis开发与运维》</font>  

# 1. 单机  
&emsp; Redis 单机部署一般存在如下几个问题：  

* 机器故障，导致 Redis 不可用，数据丢失  
* 容量瓶颈：容量不能水平扩展  
* QPS 瓶颈：一台机器的处理能力、网络宽带总是有限的，如果能够划分一些流量到其他机器，可以有效解决 QPS 问题  

