
<!-- TOC -->

- [1. 接口响应时间](#1-接口响应时间)
    - [1.1. 接口耗时统计，StopWatch](#11-接口耗时统计stopwatch)
    - [1.2. 降低接口响应时间的方案](#12-降低接口响应时间的方案)
    - [1.3. 接口超时](#13-接口超时)

<!-- /TOC -->

# 1. 接口响应时间

## 1.1. 接口耗时统计，StopWatch
<!-- 
Spring计时器StopWatch使用
https://blog.csdn.net/gxs1688/article/details/87185030

这样统计代码执行耗时，才足够优雅！ 
https://mp.weixin.qq.com/s/SeAxuZ3Ytg1SzQTQLnYjCg
-->


## 1.2. 降低接口响应时间的方案

&emsp; 接口的响应时间过长，你会怎么办？（此处只针对最简单的场景，抛开STW那些复杂的问题。）以下是我目前想到的：  
1. 异步化（Runnable、Future）  
2. 缓存  
3. 并行（ForkJoinPool、CyclicBarrier）  
4. 干掉锁（空间换时间）  

&emsp; 一句话总结：xxxxx，删代码，删到只有Controller，然后拼个返回值。一步搞定响应时间问题。  


## 1.3. 接口超时

<!-- 
 如何优雅地处理后端接口超时问题？ 
 https://mp.weixin.qq.com/s/vkGDvhbgXpTB229xE-M3Lw
-->


