
<!-- TOC -->

- [1. 慢查询](#1-慢查询)
    - [1.1. 慢查询简介](#11-慢查询简介)
    - [1.2. 慢查询使用](#12-慢查询使用)
    - [1.3. 慢查询工具](#13-慢查询工具)

<!-- /TOC -->


# 1. 慢查询  

<!-- 
https://mp.weixin.qq.com/s/KG8xGeu1Sq_RhcCwl3AW5Q
-->


## 1.1. 慢查询简介  
&emsp; MySQL慢查询日志是MySQL提供的一种日志记录，用来记录在MySQL中响应时间超过阈值的语句，具体指运行时间超过long_query_time值(默认值为10，即10秒，通常设置为1秒)的SQL，则会被记录到慢查询日志中(日志可以写入文件或者数据库表，如果对性能要求高的话，建议写文件)。默认情况下，MySQL数据库是不开启慢查询日志的，如果不是调优需要的话，不建议启动该参数。  
&emsp; 一般来说，慢查询发生在大表(比如：一个表的数据量有几百万)，且查询条件的字段没有建立索引，此时，要匹配查询条件的字段会进行全表扫描，耗时超过long_query_time，则为慢查询语句。  

## 1.2. 慢查询使用  
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

## 1.3. 慢查询工具  
<!-- 
如何定位 MySQL 慢查询？ 
https://mp.weixin.qq.com/s/_SWewX-8nFam20Wcg6No1Q
-->
&emsp; ......
