
<!-- TOC -->

- [1. binlog，二进制日志(归档日志)](#1-binlog二进制日志归档日志)
    - [1.1. MySql日志文件](#11-mysql日志文件)
    - [1.2. binlog简介](#12-binlog简介)
    - [1.3. 写入流程及刷盘时机](#13-写入流程及刷盘时机)
    - [1.4. binlog日志格式](#14-binlog日志格式)
    - [1.5. 物理文件](#15-物理文件)

<!-- /TOC -->


**<font color = "red">总结：</font>**  
1. **<font color = "clime">binlog是mysql的逻辑日志，并且由Server层进行记录，使用任何存储引擎的mysql数据库都会记录binlog日志。</font>**  
2. 在实际应用中，主要用在两个场景：主从复制和数据恢复。  
3. 写入流程：SQL修改语句先写Binlog Buffer，事务提交时，按照一定的格式刷到磁盘中。binlog刷盘时机：对于InnoDB存储引擎而言，mysql通过sync_binlog参数控制binlog的刷盘时机。  

# 1. binlog，二进制日志(归档日志)  
<!-- 
不会吧，不会吧，还有人不知道 binlog ？ 
https://mp.weixin.qq.com/s/W-u9l_As2pLUMlSQFTckCQ
-->

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

回顾下写流程
https://mp.weixin.qq.com/s/CYPARs7o_X9PnMlkGxtOcw

-->

## 1.1. MySql日志文件
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


## 1.2. binlog简介  
&emsp; binlog用于记录数据库执行的写入性操作(不包括查询)信息，以二进制的形式保存在磁盘中。 **<font color = "red">binlog是mysql的逻辑日志，并且由Server层进行记录，使用任何存储引擎的mysql数据库都会记录binlog日志。</font>**  
&emsp; binlog是通过追加的方式进行写入的，可以通过max_binlog_size参数设置每个binlog文件的大小，当文件大小达到给定值之后，会生成新的文件来保存日志。  

&emsp; **<font color = "red">作用：</font>**  
&emsp; **<font color = "blue">在实际应用中，主要用在两个场景：主从复制和数据恢复</font>**   

* 主从复制：在Master端开启binlog，然后将binlog发送到各个Slave端，Slave端重放binlog从而达到主从数据一致。  
* 数据恢复：通过使用mysqlbinlog工具来恢复数据。  

## 1.3. 写入流程及刷盘时机   
&emsp; **写入流程：** **<font color = "clime">SQL修改语句先写Binlog Buffer，事务提交时，按照一定的格式刷到磁盘中。</font>**  
&emsp; **binlog刷盘时机：** **<font color = "clime">对于InnoDB存储引擎而言，mysql通过sync_binlog参数控制biglog的刷盘时机，取值范围是0-N：</font>**  

    0：不去强制要求，由系统自行判断何时写入磁盘；
    1：每次commit的时候都要将binlog写入磁盘；
    N：每N个事务，才会将binlog写入磁盘。

&emsp; 从上面可以看出，sync_binlog最安全的是设置是1，这也是MySQL 5.7.7之后版本的默认值。但是设置一个大一些的值可以提升数据库性能，因此实际情况下也可以将值适当调大，牺牲一定的一致性来获取更好的性能。  

## 1.4. binlog日志格式  
&emsp; binlog日志有三种格式，分别为STATMENT、ROW和MIXED。在 MySQL 5.7.7之前，默认的格式是STATEMENT， **MySQL 5.7.7之后，默认值是ROW。** 日志格式通过binlog-format指定。

* STATMENT 基于SQL语句的复制(statement-based replication, SBR)，每一条会修改数据的sql语句会记录到binlog中。优点：不需要记录每一行的变化，减少了binlog日志量，节约了IO， 从而提高了性能；缺点：在某些情况下会导致主从数据不一致，比如执行sysdate()、slepp()等。  
* ROW 基于行的复制(row-based replication, RBR)，不记录每条sql语句的上下文信息，仅需记录哪条数据被修改了。优点：不会出现某些特定情况下的存储过程、或function、或trigger的调用和触发无法被正确复制的问题；缺点：会产生大量的日志，尤其是alter table的时候会让日志暴涨。  
* MIXED 基于STATMENT和ROW两种模式的混合复制(mixed-based replication, MBR)，一般的复制使用STATEMENT模式保存binlog，对于STATEMENT模式无法复制的操作使用ROW模式保存binlog。  

## 1.5. 物理文件  
&emsp; 配置文件的路径为log_bin_basename，binlog日志文件按照指定大小，当日志文件达到指定的最大的大小之后，进行滚动更新，生成新的日志文件。  
&emsp; 对于每个binlog日志文件，通过一个统一的index文件来组织。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-83.png)  

