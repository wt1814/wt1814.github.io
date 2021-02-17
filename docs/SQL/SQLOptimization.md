


<!-- TOC -->

- [1. SQL优化](#1-sql优化)
    - [1.1. MySql性能(最大数据量、最大并发数、查询耗时)](#11-mysql性能最大数据量最大并发数查询耗时)
    - [1.2. ※※※SQL优化维度](#12-※※※sql优化维度)
    - [1.3. 慢查询(监控)](#13-慢查询监控)
        - [1.3.1. 慢查询简介](#131-慢查询简介)
        - [1.3.2. 慢查询使用](#132-慢查询使用)
        - [1.3.3. 慢查询工具](#133-慢查询工具)
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
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-52.png)  

## 1.1. MySql性能(最大数据量、最大并发数、查询耗时)

* 最大并发数：  
&emsp; 并发数是指同一时刻数据库能处理多少个请求，由max_connections和max_user_connections决定。max_connections是指MySQL实例的最大连接数，上限值是16384，max_user_connections是指每个数据库用户的最大连接数。  
&emsp; MySQL会为每个连接提供缓冲区，意味着消耗更多的内存。如果连接数设置太高，硬件吃不消，太低又不能充分利用硬件。一般要求两者比值超过10%，计算方法如下：  
        
        max_used_connections / max_connections * 100% = 3/100 *100% ≈ 3%
 
    &emsp; 查看最大连接数与响应最大连接数：  

    ```sql
    show variables like '%max_connections%';
    show variables like '%max_user_connections%';
    ```

    &emsp; 在配置文件my.cnf中修改最大连接数  

        [mysqld]
        max_connections = 100
        max_used_connections = 20

* 查询耗时0.5秒：  
&emsp; 建议将单次查询耗时控制在0.5秒以内，0.5秒是个经验值，源于用户体验的3秒原则。如果用户的操作3秒内没有响应，将会厌烦甚至退出。响应时间=客户端UI渲染耗时+网络请求耗时+应用程序处理耗时+查询数据库耗时，0.5秒就是留给数据库1/6的处理时间。 

* 最大数据量：  
&emsp; MySQL没有限制单表最大记录数，它取决于操作系统对文件大小的限制。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-28.png)  
&emsp; <font color = "red">《阿里巴巴Java开发手册》提出单表行数超过500万行或者单表容量超过2GB，才推荐分库分表。</font>500万仅供参考，并非铁律。  

## 1.2. ※※※SQL优化维度  

1. 服务器的优化。  
2. 表结构设计： **<font color = "red">单库单表无法满足时，可以拆分表结构(主从复制、分库分表)，或者使用ES搜索引擎。</font>**  
3. SQL语句的优化。  
    &emsp; 对查询语句的监控、分析、优化是SQL优化的一般步骤。常规调优思路：  
    1. 查看slowlog，分析slowlog，分析出查询慢的语句。  
    2. 按照一定优先级，进行一个一个的排查所有慢语句。  
    3. 分析top sql，进行explain调试，查看语句执行时间。  
    4. 调整[索引](/docs/SQL/7.index.md)或语句本身。 

&emsp; <font color = "red">MySql性能由综合因素决定，抛开业务复杂度，影响程度依次是硬件配置、MySQL配置、数据表设计、索引优化。</font>  

 
----
----
----

## 1.3. 慢查询(监控)  
<!-- 
https://mp.weixin.qq.com/s/KG8xGeu1Sq_RhcCwl3AW5Q
-->
### 1.3.1. 慢查询简介  
&emsp; MySQL慢查询日志是MySQL提供的一种日志记录，用来记录在MySQL中响应时间超过阈值的语句，具体指运行时间超过long_query_time值(默认值为10，即10秒，通常设置为1秒)的SQL，则会被记录到慢查询日志中(日志可以写入文件或者数据库表，如果对性能要求高的话，建议写文件)。默认情况下，MySQL数据库是不开启慢查询日志的，如果不是调优需要的话，不建议启动该参数。  
&emsp; 一般来说，慢查询发生在大表(比如：一个表的数据量有几百万)，且查询条件的字段没有建立索引，此时，要匹配查询条件的字段会进行全表扫描，耗时超过long_query_time，则为慢查询语句。  

### 1.3.2. 慢查询使用  
1. 参数说明：  
    
        slow_query_log，慢查询开启状态。
        slow_query_log_file，慢查询日志存放的位置。
        long_query_time，查询超过多少秒才记录。
2. 设置步骤：  
    1. 查看慢查询相关参数。  

            #查看是否开启慢查询
            #slowquerylog = off，表示没有开启慢查询
            #slowquerylog_file 表示慢查询日志存放的目录
            show variables like 'slow_query%';
            show variables like 'long_query_time'; 
 
    2. 设置方法。
        方法一：全局变量设置。(即时性的，重启mysql之后失效，常用的)  

            --将 slow_query_log 全局变量设置为“ON”状态
            set global slow_query_log='ON';
            --设置慢查询日志存放的位置
            set global slow_query_log_file='/usr/local/mysql/data/slow.log';
            --查询超过1秒就记录
            set global long_query_time=1;  

        方法二：配置文件设置。修改配置文件my.cnf，在[mysqld]下的下方加入。  

            [mysqld]
            slow_query_log = ON
            slow_query_log_file = /usr/local/mysql/data/slow.log
            long_query_time = 1

    3. 重启MySQL服务。  

            service mysqld restart

    4. 查看设置后的参数。  

            show variables like 'slow_query%';
            show variables like 'long_query_time';  

### 1.3.3. 慢查询工具  
<!-- 
如何定位 MySQL 慢查询？ 
https://mp.weixin.qq.com/s/_SWewX-8nFam20Wcg6No1Q
-->
......

----
## 1.4. EXPLAIN、PROCEDURE ANALYSE(分析)  
&emsp; [SQL分析](/docs/SQL/Analysis.md)  

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

&emsp; 可以通过执行如下脚本监控 MySQL 服务器运行的状态值  

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
