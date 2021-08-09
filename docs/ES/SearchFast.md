
<!-- TOC -->

- [1. Elasticsearch搜索为什么那么快？](#1-elasticsearch搜索为什么那么快)
    - [1.1. 架构](#11-架构)
    - [1.2. 资源争夺，乐观锁](#12-资源争夺乐观锁)
    - [1.3. 读写原理](#13-读写原理)

<!-- /TOC -->

# 1. Elasticsearch搜索为什么那么快？  
<!-- 
Elasticsearch 搜索为什么那么快？
https://www.jianshu.com/p/9c7d4bb3b093
Elasticsearch：ES 倒排索引为什么查询速度会这么快
https://www.jianshu.com/p/addefe15f3e9
-->

<!-- 
ES和lucene
https://blog.csdn.net/universsky2015/article/details/108465744
https://blog.csdn.net/mijichui2153/article/details/107303579

-->

## 1.1. 架构
&emsp; ES是天然的分布式架构，由多台机器组成。  

## 1.2. 资源争夺，乐观锁  
&emsp; ES采用乐观并发控制，避免了加锁导致的正在尝试的操作的blocked，通过_version字段来保证并发情况下的正确性。  
&emsp; 同时，ES本身基于的Lucene库在底层将数据分为segment，几乎避免了锁的出现，大大的提升了读写效率。  

## 1.3. 读写原理  

