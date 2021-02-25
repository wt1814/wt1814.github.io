

<!-- TOC -->

- [1. 使用ES中的一些问题](#1-使用es中的一些问题)
    - [1.1. cpu使用率过高](#11-cpu使用率过高)
    - [1.2. Elasticsearch OOM](#12-elasticsearch-oom)
    - [1.3. 聚合数据结果不精确](#13-聚合数据结果不精确)

<!-- /TOC -->


# 1. 使用ES中的一些问题  
## 1.1. cpu使用率过高  
&emsp; cpu使用率，该如何调节呢。cpu使用率高，有可能是写入导致的，也有可能是查询导致的，那要怎么查看呢？  
&emsp; 可以先通过 GET _nodes/{node}/hot_threads 查看线程栈，查看是哪个线程占用 cpu 高，如果是 elasticsearch[{node}][search][T#10] 则是查询导致的，如果是 elasticsearch[{node}][bulk][T#1] 则是数据写入导致的。  
&emsp; 在实际调优中，cpu 使用率很高，如果不是 SSD，建议把 index.merge.scheduler.max_thread_count: 1 索引 merge 最大线程数设置为 1 个，该参数可以有效调节写入的性能。因为在存储介质上并发写，由于寻址的原因，写入性能不会提升，只会降低。  


## 1.2. Elasticsearch OOM  
<!-- 
公司银子不多，遇到Elasticsearch OOM（内存溢出），除了瞪白眼，还能干啥... 
https://mp.weixin.qq.com/s/W3mSgShSgoqkGz6otZRE5Q
-->
......

## 1.3. 聚合数据结果不精确  
<!-- 
 Elasticsearch 聚合数据结果不精确，怎么破？ 
https://mp.weixin.qq.com/s/V4cGqvkQ7-DgeSvPSketgQ
-->