
<!-- TOC -->

- [1. Redis实现消息队列](#1-redis实现消息队列)
    - [1.1. 实现消息队列](#11-实现消息队列)
    - [1.2. 实现延迟队列](#12-实现延迟队列)

<!-- /TOC -->


# 1. Redis实现消息队列  
<!-- 

https://mp.weixin.qq.com/s/C3RyQMLLnABPcchV4_C8-A
-->

## 1.1. 实现消息队列
&emsp; redis中实现消息队列的几种方案：  

* 基于List的 LPUSH+BRPOP 的实现
* PUB/SUB，订阅/发布模式
* 基于Sorted-Set的实现
* 基于Stream类型的实现


## 1.2. 实现延迟队列
&emsp; 参考[延时队列](/docs/frame/delayQueue.md)  

