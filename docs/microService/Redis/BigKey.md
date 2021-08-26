<!-- TOC -->

- [1. BigKey](#1-bigkey)
    - [1.1. BigKey定义](#11-bigkey定义)
    - [1.2. 怎么产生的？](#12-怎么产生的)
    - [1.3. 危害](#13-危害)
    - [1.4. ~~如何发现~~](#14-如何发现)
        - [1.4.1. redis-cli --bigkeys](#141-redis-cli---bigkeys)
        - [1.4.2. debug object](#142-debug-object)
        - [1.4.3. memory usage](#143-memory-usage)
        - [1.4.4. 客户端](#144-客户端)
        - [1.4.5. 监控报警](#145-监控报警)
    - [1.5. 如何删除](#15-如何删除)
    - [1.6. 如何优化](#16-如何优化)
        - [1.6.1. 拆分](#161-拆分)
        - [1.6.2. 本地缓存](#162-本地缓存)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. bigKey如何发现？
2. 如何删除：Redis4.0支持异步删除。  
3. 如何优化：1). 拆分；2). 本地缓存。  

# 1. BigKey
<!-- 
https://blog.csdn.net/yangshangwei/article/details/104958460
https://blog.csdn.net/weixin_47531845/article/details/108821372

 ***** 一文详解Redis中BigKey、HotKey的发现与处理 
 https://mp.weixin.qq.com/s/FPYE1B839_8Yk1-YSiW-1Q
-->

## 1.1. BigKey定义
&emsp; 在Redis中，一个字符串最大512MB，一个二级数据结构（例如hash、list、set、zset）可以存储大约40亿个(2^32-1)个元素，但实际上，如果是下面两种情况，可以认为它是bigkey。

* 字符串类型：它的big体现在单个value值很大，一般认为超过10KB就是bigkey。  
* 非字符串类型：哈希、列表、集合、有序集合，它们的big体现在元素个数太多。  

## 1.2. 怎么产生的？  
&emsp; 一般来说，bigkey的产生都是由于程序设计不当，或者对于数据规模预料不清楚造成的，来看几个：  

* 社交类：粉丝列表，如果某些明星或者大v不精心设计下，必是bigkey。  
* 统计类：例如按天存储某项功能或者网站的用户集合，除非没几个人用，否则必是bigkey。  
* 缓存类：将数据从数据库load出来序列化放到Redis里，这个方式非常常用，但有两个地方需要注意:  
    * 第一，是不是有必要把所有字段都缓存  
    * 第二，有没有相关关联的数据  

## 1.3. 危害  
1. 内存空间不均匀  
&emsp; 这样会不利于集群对内存的统一管理，存在丢失数据的隐患。
2. 超时阻塞  
&emsp; 操作bigkey通常比较耗时，也就意味着阻塞Redis可能性越大，这样会造成客户端阻塞或者引起故障切换，它们通常出现在慢查询中。  
3. 网络拥塞   
&emsp; bigkey也就意味着每次获取要产生的网络流量较大，假设一个bigkey为1MB，客户端每秒访问量为1000，那么每秒产生1000MB的流量，对于普通的千兆网卡(按照字节算是128MB/s)的服务器来说简直是灭顶之灾，而且一般服务器会采用单机多实例的方式来部署，也就是说一个bigkey可能会对其他实例造成影响，其后果不堪设想。
4. 过期删除   
&emsp; 有个bigkey，它安分守己（只执行简单的命令，例如hget、lpop、zscore等），但它设置了过期时间，当它过期后，会被删除，如果没有使用Redis 4.0的过期异步删除(lazyfree-lazy-expire yes)，就会存在阻塞Redis的可能性，而且这个过期删除不会从主节点的慢查询发现（因为这个删除不是客户端产生的，是内部循环事件，可以从latency命令中获取或者从slave节点慢查询发现）。  
5. 迁移困难  
&emsp; 当需要对bigkey进行迁移（例如Redis cluster的迁移slot），实际上是通过migrate命令来完成的，migrate实际上是通过dump + restore + del三个命令组合成原子命令完成，如果是bigkey，可能会使迁移失败，而且较慢的migrate会阻塞Redis。  

## 1.4. ~~如何发现~~
### 1.4.1. redis-cli --bigkeys  
&emsp; redis-cli提供了--bigkeys来查找bigkey，例如下面就是一次执行结果：  

```text
-------- summary -------
Biggest string found 'user:1' has 5 bytes
Biggest list found 'taskflow:175448' has 97478 items
Biggest set found 'redisServerSelect:set:11597' has 49 members
Biggest hash found 'loginUser:t:20180905' has 863 fields
Biggest zset found 'hotkey:scan:instance:zset' has 3431 members
40 strings with 200 bytes (00.00% of keys, avg size 5.00)
2747619 lists with 14680289 items (99.86% of keys, avg size 5.34)
2855 sets with 10305 members (00.10% of keys, avg size 3.61)
13 hashs with 2433 fields (00.00% of keys, avg size 187.15)
830 zsets with 14098 members (00.03% of keys, avg size 16.99)
```

&emsp; 可以看到--bigkeys给出了每种数据结构的top 1 bigkey，同时给出了每种数据类型的键值个数以及平均大小。  

&emsp; bigkeys对问题的排查非常方便，但是在使用它时候也有几点需要注意:  

* 建议在从节点执行，因为--bigkeys也是通过scan完成的。  
* 建议在节点本机执行，这样可以减少网络开销。  
* 如果没有从节点，可以使用--i参数，例如(--i 0.1 代表100毫秒执行一次)  
* --bigkeys只能计算每种数据结构的top1，如果有些数据结构非常多的bigkey，也搞不定  


### 1.4.2. debug object

### 1.4.3. memory usage


### 1.4.4. 客户端

### 1.4.5. 监控报警


## 1.5. 如何删除  
&emsp; Redis4.0支持异步删除：如果无法避免存储 bigkey，那么建议开启 Redis 的 lazy-free 机制（4.0+版本支持）。当开启这个机制后，Redis 在删除一个 bigkey 时，释放内存的耗时操作，将会放到后台线程中去执行，这样可以在最大程度上，避免对主线程的影响。  


## 1.6. 如何优化
### 1.6.1. 拆分
&emsp; big list： list1、list2、...listN
&emsp; big hash：可以做二次的hash，例如hash%100
&emsp; 日期类：key20190320、key20190321、key_20190322。

### 1.6.2. 本地缓存
&emsp; 减少访问redis次数，降低危害，但是要注意这里有可能因此本地的一些开销（例如使用堆外内存会涉及序列化，bigkey对序列化的开销也不小）
