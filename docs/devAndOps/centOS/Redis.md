
<!-- TOC -->

- [1. Redis](#1-redis)
    - [1.1. Redis各版本](#11-redis各版本)
    - [1.2. 搭建Redis](#12-搭建redis)
        - [redis哨兵](#redis哨兵)
        - [1.2.1. 搭建Redis伪集群](#121-搭建redis伪集群)
    - [1.3. 无法杀死Redis集群](#13-无法杀死redis集群)

<!-- /TOC -->


# 1. Redis  
<!-- 


-->

## 1.1. Redis各版本  
<!--
Redis各版本特性
https://blog.csdn.net/tianyu_yunlong_1/article/details/123006028
-->
1. Redis4.0(2017-07-15)

```text
①　提供了模块系统，方便第三方开发者拓展Redis的功能。
②　PSYNC2.0：优化了之前版本中，主从节点切换必然引起全量复制的问题。
③　提供了新的缓存剔除算法：LFU（Last Frequently Used），并对已有算法进行了优化。
④　提供了非阻塞del和flushall/flushdb功能，有效解决删除了bigkey可能造成的Redis阻塞。
⑤　提供了memory命令，实现对内存更为全面的监控统计。
⑥　提供了交互数据库功能，实现Redis内部数据库的数据置换。
⑦　提供了RDB-AOF混合持久化格式，充分利用了AOF和RDB各自优势。
⑧　Redis Cluster 兼容NAT和Docker。
```

2. Redis6.0(2020-08-27)

```text
①　许多新的模块API。
②　更好过期算法。
③　SSL支持。
④　ACL支持。
⑤　新的RESP3协议。
⑥　客户端缓存。
⑦　多线程I/O
⑧　副本的无盘复制。
⑨　redis-benchmark支持和redis-cli改进。
⑩　Systemd 支持重写。
⑪　redis集群代理的发布（还不稳定，不建议生产使用）。
⑫　disque模块的发布
```



## 1.2. 搭建Redis  

### redis哨兵  
<!-- 

https://www.jianshu.com/p/df4af68549a5
-->


### 1.2.1. 搭建Redis伪集群  
<!-- 
windows搭建redis伪集群
https://www.bianchengquan.com/article/438078.html
Windows的Redis5.0+集群搭建
https://blog.csdn.net/qq_37062156/article/details/121628324
在window下创建redis服务
https://blog.csdn.net/lejian/article/details/124339831

-->

## 1.3. 无法杀死Redis集群  
<!-- 

Linux上无法杀掉redis进程
https://blog.csdn.net/u011191042/article/details/84528502?spm=1001.2101.3001.6661.1&utm_medium=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-84528502-blog-121384286.pc_relevant_antiscanv2&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-1-84528502-blog-121384286.pc_relevant_antiscanv2&utm_relevant_index=1

-->

