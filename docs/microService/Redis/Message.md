

# Redis实现消息队列  
<!-- 

https://mp.weixin.qq.com/s/C3RyQMLLnABPcchV4_C8-A
-->

&emsp; redis中实现消息队列的几种方案：  

* 基于List的 LPUSH+BRPOP 的实现
* PUB/SUB，订阅/发布模式
* 基于Sorted-Set的实现
* 基于Stream类型的实现
