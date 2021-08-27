
<!-- TOC -->

- [1. 高可用实现](#1-高可用实现)
    - [1.1. MMM架构](#11-mmm架构)
    - [1.2. MHA架构](#12-mha架构)

<!-- /TOC -->


# 1. 高可用实现

<!-- 
五大常见的MySQL高可用方案
https://www.douban.com/note/706714492/
https://blog.csdn.net/yzj5208/article/details/81288436
https://blog.csdn.net/qq_39720208/article/details/102758662
https://mp.weixin.qq.com/s/RZ7b8IKC7N3E87DaZhWaVA


mysql高可用方案对比
https://blog.csdn.net/yzj5208/article/details/81288436

-->
## 1.1. MMM架构  
&emsp; Multi_Master Replication Manager，就是mysql**多主复制管理器的简称，它是由一套perl语言开发的用于管理mysql**主主同步架构的工具集，主要作用是监控和管理mysql主主复制拓扑，并在当前的主服务器失效时，进行主和主备服务器之间的主从切换和故障转移等工作。  

## 1.2. MHA架构  
&emsp; <font color = "red">Mha(master high Avaliability )，是由perl脚本开发的，用于管理mysql主从复制或者实现mysql高可用的一套相对比较成熟的工具套装。</font>从名称可以看出，MHA主要关注的是mysql集群的主DB，其主要功能是在mysql中主从复制架构下完成故障切换和在众多的从服务器中自动选举出新的从服务器，并将其他的从服务器和新选出的主数据库进行同步切换，在mysql的切换过程中，MHA可以做到完成高效的主从切换。基本可以保证在30s内完成所有的切换操作。并且在切换的过程中可以最大程度的保证数据的一致性。以避免丢失的事务，达到真正意义上的高可用。  


