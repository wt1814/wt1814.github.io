

<!-- TOC -->

- [1. Redis Pipeline](#1-redis-pipeline)
    - [1.1. 批处理](#11-批处理)
        - [1.1.1. 为什么需要批处理](#111-为什么需要批处理)
        - [1.1.2. redis中批处理方式](#112-redis中批处理方式)
    - [1.2. Redis Pipeline](#12-redis-pipeline)

<!-- /TOC -->

# 1. Redis Pipeline

<!-- 
Redis pipeline
https://mp.weixin.qq.com/s/54n1Q3_Zvyxr9Sj2Fqzhew
批量处理
https://www.jianshu.com/p/75137d23ae4a

https://blog.csdn.net/w1lgy/article/details/84455579

redis通过pipeline提升吞吐量的方法
https://www.jb51.net/article/134489.htm
-->

## 1.1. 批处理  

### 1.1.1. 为什么需要批处理  
&emsp; ......

### 1.1.2. redis中批处理方式  
&emsp; Redis主要提供了以下几种批量操作方式：  

* 批量get/set(multi get/set)。⚠️注意：Redis中有删除单个Key的指令DEL，但没有批量删除 Key 的指令。  
* 管道(pipelining)
* 事务(transaction)
* 基于事务的管道(transaction in pipelining)


&emsp; 批量get/set(multi get/set)与管道：  
1. 原生批命令（mset, mget）是原子性，pipeline是非原子性。  
2. 原生批命令一命令多个key，但pipeline支持多命令（存在事务），非原子性。  
3. 原生批命令是服务端实现，而pipeline需要服务端与客户端共同完成。  

## 1.2. Redis Pipeline
&emsp; Pipeline指的是管道技术，指的是客户端允许将多个请求依次发给服务器，过程中而不需要等待请求的回复，在最后再一并读取结果即可。  
