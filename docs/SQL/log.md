

<!-- TOC -->

- [1. MySql日志文件](#1-mysql日志文件)
    - [1.1. MySql日志文件简介](#11-mysql日志文件简介)

<!-- /TOC -->


# 1. MySql日志文件  
&emsp; **<font color = "red">参考《MySQL技术内幕：InnoDB存储引擎》</font>**  
<!--

https://mp.weixin.qq.com/s/1FOL7E--rQ9_QUN-dO_4pQ
MySQL中的7种日志你都知道是干啥的吗
https://mp.weixin.qq.com/s/oeKTX-E6W40IjLy5TJewLg

https://mp.weixin.qq.com/s/voW6T241-00DAPpgrh-STw
Mysql 中写操作时保驾护航的三兄弟！ 
https://mp.weixin.qq.com/s/CYPARs7o_X9PnMlkGxtOcw

Redo log 与 Binlog 
https://mp.weixin.qq.com/s/XTpoYW--6PTqotcC8tpF2A
事务日志－－崩溃恢复
https://mp.weixin.qq.com/s/mSxUXHgcHo5a7VAhvjIpCQ

写入流程
https://mp.weixin.qq.com/s/CYPARs7o_X9PnMlkGxtOcw

-->


## 1.1. MySql日志文件简介  
&emsp; MySql日志文件有：  

* 错误日志(errorlog)：记录出错信息，也记录一些警告信息或者正确的信息。
* 一般查询日志(general log)：记录所有对数据库请求的信息，不论这些请求是否得到了正确的执行。
* 慢查询日志(slow query log)：设置一个阈值，将运行时间超过该值的所有SQL语句都记录到慢查询的日志文件中。
* 二进制日志(binlog)：记录对数据库执行更改的所有操作。
* 中继日志(relay log)：中继日志也是二进制日志，用来给slave库恢复。
* 重做日志(redo log)。
* 回滚日志(undo log)。

&emsp; 重点需要关注的是二进制日志(binlog)和事务日志(包括redo log和undo log)。  

&emsp; 此外日志可以分为<font color = "clime">逻辑日志和物理日志</font>。  
    
* 逻辑日志：可以简单理解为记录的就是sql语句。
* 物理日志：因为mysql数据最终是保存在数据页中的，物理日志记录的就是数据页变更。

<!-- 
WAL
-->
