


<!-- TOC -->

- [1. SQL优化](#1-sql优化)
    - [1.1. MySql性能和瓶颈](#11-mysql性能和瓶颈)
    - [1.2. ★★★多方面优化](#12-★★★多方面优化)
    - [1.3. 慢查询（监控）](#13-慢查询监控)
    - [1.4. EXPLAIN、PROCEDURE ANALYSE(分析)](#14-explainprocedure-analyse分析)
    - [1.5. Sql语句优化](#15-sql语句优化)
    - [1.6. 数据库表结构设计](#16-数据库表结构设计)
        - [1.6.1. 选择合适的数据类型](#161-选择合适的数据类型)
        - [1.6.2. 表的拆分](#162-表的拆分)
        - [1.6.3. 读写分离](#163-读写分离)
    - [1.7. 系统调优](#17-系统调优)
        - [1.7.1. 查看MySQL服务器运行的状态值](#171-查看mysql服务器运行的状态值)
        - [1.7.2. 服务器参数调优](#172-服务器参数调优)
            - [1.7.2.1. 内存相关](#1721-内存相关)
            - [1.7.2.2. IO相关](#1722-io相关)
            - [1.7.2.3. 安全相关](#1723-安全相关)
            - [1.7.2.4. 其他](#1724-其他)

<!-- /TOC -->


# 1. SQL优化  
<!--
你知道这 8 种 SQL 错误用法吗？ 
https://mp.weixin.qq.com/s/Ur-SWCwm-6yVF9vV2eLlHA
男朋友连模糊匹配like %%怎么优化都不知道 
https://mp.weixin.qq.com/s/ygvuP35B_sJAlBHuuEJhfg
8 种最坑的SQL错误用法
https://mp.weixin.qq.com/s/rfKgNDc7r6JlZY1ZGS1nvQ

-->

## 1.1. MySql性能和瓶颈
&emsp; 查看[数据库分布式](/docs/SQL/DistributedDatabase.md)  


## 1.2. ★★★多方面优化 
&emsp; <font color = "red">MySql性能由综合因素决定，抛开业务复杂度，影响程度依次是硬件配置、MySQL配置、数据表设计、索引优化。</font>  
1. SQL语句的优化。  
    &emsp; `对查询语句的监控、分析、优化是SQL优化的一般步骤。`常规调优思路：  
    1. 查看慢查询日志slowlog，分析slowlog，分析出查询慢的语句。  
    2. 按照一定优先级，进行一个一个的排查所有慢语句。  
    3. 分析top sql，进行explain调试，查看语句执行时间。  
    4. 调整[索引](/docs/SQL/7.index.md)或语句本身。 
2. 表结构设计： **<font color = "red">单库单表无法满足时，可以拆分表结构（主从复制、分库分表），或者使用ES搜索引擎。</font>**  
3. 服务器的优化。  


 
----
----
----

## 1.3. 慢查询（监控）  
&emsp; 查看[慢查询（监控）](/docs/SQL/Slowlog.md)  

## 1.4. EXPLAIN、PROCEDURE ANALYSE(分析)  
&emsp; 查看[SQL分析](/docs/SQL/Analysis.md)  

## 1.5. Sql语句优化  
&emsp; [SQL语句优化](/docs/SQL/SQLStatement.md)  

## 1.6. 数据库表结构设计  
### 1.6.1. 选择合适的数据类型  
<!-- 
https://mp.weixin.qq.com/s/IsZjLI7QAB6t7H7NyGscGg
-->
1. 使用可以存下数据最小的数据类型  
2. 使用简单的数据类型。int 要比 varchar 类型在mysql处理简单   
3. 尽量使用 tinyint、smallint、mediumint 作为整数类型而非 int  
4. 尽可能使用 not null 定义字段，因为 null 占用4字节空间  
5. 尽量少用 text 类型，非用不可时最好考虑分表  
6. 尽量使用 timestamp 而非 datetime  
7. 单表不要有太多字段，建议在 20 以内  

### 1.6.2. 表的拆分 
&emsp; 当数据库中的数据非常大时，查询优化方案也不能解决查询速度慢的问题时，可以考虑拆分表，让每张表的数据量变小，从而提高查询效率。  
1. 垂直拆分：将表中多个列分开放到不同的表中。例如用户表中一些字段经常被访问，将这些字段放在一张表中，另外一些不常用的字段放在另一张表中。插入数据时，使用事务确保两张表的数据一致性。  
2. 水平拆分：按照行进行拆分。例如用户表中，使用用户ID，对用户ID取10的余数，将用户数据均匀的分配到0~9的10个用户表中。查找时也按照这个规则查询数据。  

### 1.6.3. 读写分离  
&emsp; 一般情况下对数据库而言都是“读多写少”。换言之，数据库的压力多数是因为大量的读取数据的操作造成的。可以采用数据库集群的方案，使用一个库作为主库，负责写入数据；其他库为从库，负责读取数据。这样可以缓解对数据库的访问压力。  

## 1.7. 系统调优  

<!-- https://mp.weixin.qq.com/s/taJSS2QgLRWXgNnAW5WcsQ-->

### 1.7.1. 查看MySQL服务器运行的状态值  
&emsp; 执行命令：  

```
show status
```
&emsp; 由于返回结果太多，此处不贴出结果。其中，在返回的结果中，主要关注 “Queries”、“Threadsconnected” 和 “Threadsrunning” 的值，即查询次数、线程连接数和线程运行数。

&emsp; 可以通过执行如下脚本监控 MySQL 服务器运行的状态值：  

```
#!/bin/bash
while true
do
mysqladmin -uroot -p"密码" ext | awk '/Queries/{q=$4}/Threads_connected/{c=$4}/Threads_running/{r=$4}END{printf("%d %d %d\n",q,c,r)}' >> status.txt
sleep 1
done
```
&emsp; 执行该脚本 24 小时，获取 status.txt 里的内容，再次通过 awk 计算==每秒请求 MySQL 服务的次数==  

```
awk '{q=$1-last;last=$1}{printf("%d %d %d\n",q,$2,$3)}' status.txt
```
&emsp; 复制计算好的内容到 Excel 中生成图表观察数据周期性。  
&emsp; 如果观察的数据有周期性的变化，如上图的解释，需要修改缓存失效策略。  
&emsp; 例如：  
&emsp; 通过随机数在[3,6,9] 区间获取其中一个值作为缓存失效时间，这样分散了缓存失效时间，从而节省了一部分内存的消耗。  
&emsp; 当访问高峰期时，一部分请求分流到未失效的缓存，另一部分则访问 MySQL 数据库，这样减少了 MySQL 服务器的压力。  

### 1.7.2. 服务器参数调优  

#### 1.7.2.1. 内存相关
&emsp; sortbuffersize 排序缓冲区内存大小  
&emsp; joinbuffersize 使用连接缓冲区大小  
&emsp; readbuffersize 全表扫描时分配的缓冲区大小  

#### 1.7.2.2. IO相关
&emsp; Innodblogfile_size 事务日志大小  
&emsp; Innodblogfilesingroup 事务日志个数  
&emsp; Innodblogbuffer_size 事务日志缓冲区大小  
&emsp; Innodbflushlogattrx_commit 事务日志刷新策略 ，其值如下：  

    0：每秒进行一次 log 写入 cache，并 flush log 到磁盘
    1：在每次事务提交执行 log 写入 cache，并 flush log 到磁盘
    2：每次事务提交，执行 log 数据写到 cache，每秒执行一次 flush log 到磁盘

#### 1.7.2.3. 安全相关
&emsp; expirelogsdays 指定自动清理 binlog 的天数  
&emsp; maxallowedpacket 控制 MySQL 可以接收的包的大小  
&emsp; skipnameresolve 禁用 DNS 查找  
&emsp; read_only 禁止非 super 权限用户写权限  

#### 1.7.2.4. 其他
&emsp; max_connections 控制允许的最大连接数  
&emsp; tmptablesize 临时表大小  
&emsp; maxheaptable_size 最大内存表大小  
