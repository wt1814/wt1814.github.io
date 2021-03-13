
<!-- TOC -->

- [1. InnoDB体系结构](#1-innodb体系结构)
    - [1.1. Innodb后台线程](#11-innodb后台线程)
    - [1.2. InnoDB内存中的结构](#12-innodb内存中的结构)
    - [1.3. InnoDB磁盘上的结构](#13-innodb磁盘上的结构)

<!-- /TOC -->

# 1. InnoDB体系结构  
<!-- 
InnoDB 原理
https://mp.weixin.qq.com/s/nrb0OaiD_QRtPGREpUr0HA
-->
&emsp; Innodb体系结构图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-131.png)  
&emsp; Innodb体系结构包含后台线程、内存池和磁盘上的结构。  

&emsp; InnoDB架构图二(来自官方文档 https://dev.mysql.com/doc/refman/5.7/en/innodb-architecture.html )
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-132.png)  

## 1.1. Innodb后台线程 
<!-- 
了解InnoDB的后台线程 
https://mp.weixin.qq.com/s/2dUIAot8OKHiWar44qRi-A
-->
&emsp; 后台线程主要分为4个部分：  

* Master Thread   
&emsp; 主要负责将缓冲池中的数据异步刷新到磁盘，保证数据的一致性，包括脏页的刷新、合并插入缓冲(INSERT BUFFER)、UNDO页的回收等等。
* IO Thread  
&emsp; InnoDB存储引擎中大量使用AIO(Async IO)来处理写IO的请求，而IO Thread的工作主要就是负责这些IO请求的回调。  
&emsp; **InnoDB 1.0版本之前一共有4个IO Thread，分别是write、read、insert buffer和log IO thread。** 从1.0.x版本开始 ，read和write增加到4个，一共10个IO Thread，阿里云的RDS就是这10个IO Thread。  
* Purge Thread  
&emsp; 事务被提交后，其所使用的undolog可能不再需要了，purge thread来回收已经使用并分配的undo页。在InnoDB 1.1版本之前undo页的回收也就是purge操作都是在master thread中进行的，InnoDB1.1版本之后，就分配到单独的线程中执行，也就是purge thread，尽管现阶段使用的阿里云的RDS是基于mysql 5.5版本的，5.5对应的InnoDB就是1.1.x版本，但是配置的purge thread是为0的，目测应该是master thread进行purge操作来回收已经分配并使用的undo页。  
* Page Cleaner Thread  
&emsp; page cleaner thread是在InnoDB1.2.x版本之后加入的，将原本放在master thread中进行的脏页刷新操作放到了单独的线程中来完成。  

## 1.2. InnoDB内存中的结构  
<!-- 
https://mp.weixin.qq.com/s/nrb0OaiD_QRtPGREpUr0HA
-->
&emsp; 官网：https://dev.mysql.com/doc/refman/5.7/en/innodb-in-memory-structures.html  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-141.png)  

&emsp; 内存中的结构主要包括Buffer Pool，Change Buffer、Adaptive Hash Index以及Log Buffer四部分。  
&emsp; **<font color = "clime">如果从内存上来看，Change Buffer和Adaptive Hash Index占用的内存都属于Buffer Pool，Log Buffer占用的内存与 Buffer Pool独立。**</font>  

* [Buffer Pool](/docs/SQL/BufferPool.md)  
* [Change Buffer](/docs/SQL/ChangeBuffer.md)  
* [Adaptive Hash Index](/docs/SQL/AdaptiveHashIndex.md)  

---
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-147.png)  
&emsp; 目前可以看出，InnoDB内存主要有两大部分：缓冲池、重做日志缓冲。  

## 1.3. InnoDB磁盘上的结构  
&emsp; 官方文档：https://dev.mysql.com/doc/refman/5.7/en/innodb-on-disk-structures.html  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-133.png)  

* [表空间](/docs/SQL/TableSpace.md) 
* [Double Write](/docs/SQL/DoubleWrite.md)  
* [MySql事务日志](/docs/SQL/log.md)  
