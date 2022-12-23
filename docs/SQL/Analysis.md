<!-- TOC -->

- [1. EXPLAIN、PROCEDURE ANALYSE(分析)](#1-explainprocedure-analyse分析)
    - [1.1. explain与explain extended](#11-explain与explain-extended)
        - [1.1.1. explain](#111-explain)
        - [1.1.2. explain extended](#112-explain-extended)
    - [1.2. profiling](#12-profiling)
        - [1.2.1. 查看profile开启情况](#121-查看profile开启情况)
        - [1.2.2. 启用profile](#122-启用profile)
        - [1.2.3. 查看执行的SQL列表](#123-查看执行的sql列表)
        - [1.2.4. 查询指定ID的执行详细信息](#124-查询指定id的执行详细信息)
        - [1.2.5. 获取CPU、Block IO等信息](#125-获取cpublock-io等信息)
    - [1.3. trace工具](#13-trace工具)
    - [1.4. 小结](#14-小结)
    - [1.5. show warnings](#15-show-warnings)
    - [1.6. ~~proceduer analyse()取得建议~~](#16-proceduer-analyse取得建议)
    - [1.7. sql分析工具](#17-sql分析工具)

<!-- /TOC -->

&emsp; **<font color = "red">总结</font>**  
1. **<font color = "clime">SQL分析语句有EXPLAIN与explain extended、show warnings、proceduer analyse、profiling、trace。</font>**  
2. <font color = "red">用explain extended查看执行计划会比explain多一列filtered。filtered列给出了一个百分比的值，这个百分比值和rows列的值一起使用，可以估计出那些将要和explain中的前一个表进行连接的行的数目。前一个表就是指explain的id列的值比当前表的id小的表。</font>  
&emsp; mysql中有一个explain 命令可以用来分析select 语句的运行效果，例如explain可以获得select语句使用的索引情况、排序的情况等等。除此以外，explain 的extended 扩展能够在原本explain的基础上额外的提供一些查询优化的信息，这些信息可以通过mysql的show warnings命令得到。  
3. profiling  
&emsp; 使用profiling命令可以了解SQL语句消耗资源的详细信息（每个执行步骤的开销）。可以清楚了解到SQL到底慢在哪个环节。   
4. trace  
&emsp; 查看优化器如何选择执行计划，获取每个可能的索引选择的代价。  

# 1. EXPLAIN、PROCEDURE ANALYSE(分析)  

```meimaid
graph LR
A[SQL分析] --> B(explain)
A --> C(proceduer analyse)
A --> D(profiling)
```

&emsp; **<font color = "clime">SQL分析语句有EXPLAIN与explain extended、show warnings、proceduer analyse、profiling。</font>**  

## 1.1. explain与explain extended
### 1.1.1. explain
&emsp; 查看[explain](/docs/SQL/explain.md)  

### 1.1.2. explain extended
&emsp; <font color = "red">用explain extended查看执行计划会比explain多一列filtered。filtered列给出了一个百分比的值，这个百分比值和rows列的值一起使用，可以估计出那些将要和explain中的前一个表进行连接的行的数目。前一个表就是指explain的id列的值比当前表的id小的表。</font>  
&emsp; mysql中有一个explain 命令可以用来分析select 语句的运行效果，例如explain可以获得select语句使用的索引情况、排序的情况等等。除此以外，explain 的extended 扩展能够在原本explain的基础上额外的提供一些查询优化的信息，这些信息可以通过mysql的show warnings命令得到。  

## 1.2. profiling  
&emsp; 使用profiling命令可以了解SQL语句消耗资源的详细信息(每个执行步骤的开销)。  

### 1.2.1. 查看profile开启情况  
```
select @@profiling;
```
&emsp; 返回结果：  

```
mysql> select @@profiling;
+-------------+
| @@profiling |
+-------------+
|           0 |
+-------------+
1 row in set, 1 warning (0.00 sec)
```
&emsp; 0 表示关闭状态，1表示开启  

### 1.2.2. 启用profile  
```
set profiling = 1;  
```
&emsp; 返回结果：  

```
mysql> set profiling = 1;  
Query OK, 0 rows affected, 1 warning (0.00 sec)

mysql> select @@profiling;
+-------------+
| @@profiling |
+-------------+
|           1 |
+-------------+
1 row in set, 1 warning (0.00 sec)
```
&emsp; 在连接关闭后，profiling状态自动设置为关闭状态。  

### 1.2.3. 查看执行的SQL列表  
```
show profiles;  
```
&emsp; 返回结果：  
```
mysql> show profiles;
+----------+------------+------------------------------+
| Query_ID | Duration   | Query                        |
+----------+------------+------------------------------+
|        1 | 0.00062925 | select @@profiling           |
|        2 | 0.00094150 | show tables                  |
|        3 | 0.00119125 | show databases               |
|        4 | 0.00029750 | SELECT DATABASE()            |
|        5 | 0.00025975 | show databases               |
|        6 | 0.00023050 | show tables                  |
|        7 | 0.00042000 | show tables                  |
|        8 | 0.00260675 | desc role                    |
|        9 | 0.00074900 | select name,is_key from role |
+----------+------------+------------------------------+
9 rows in set, 1 warning (0.00 sec)
```
&emsp; 该命令执行之前，需要执行其他 SQL 语句才有记录。  

### 1.2.4. 查询指定ID的执行详细信息  

```sql
show profile for query Query_ID;
```
&emsp; 返回结果：  

```
mysql> show profile for query 9;
+----------------------+----------+
| Status               | Duration |
+----------------------+----------+
| starting             | 0.000207 |
| checking permissions | 0.000010 |
| Opening tables       | 0.000042 |
| init                 | 0.000050 |
| System lock          | 0.000012 |
| optimizing           | 0.000003 |
| statistics           | 0.000011 |
| preparing            | 0.000011 |
| executing            | 0.000002 |
| Sending data         | 0.000362 |
| end                  | 0.000006 |
| query end            | 0.000006 |
| closing tables       | 0.000006 |
| freeing items        | 0.000011 |
| cleaning up          | 0.000013 |
+----------------------+----------+
15 rows in set, 1 warning (0.00 sec)
```
&emsp; 每行都是状态变化的过程以及它们持续的时间。Status这一列和show processlist的 State是一致的。因此，需要优化的注意点与上文描述的一样。  

### 1.2.5. 获取CPU、Block IO等信息  

```sql
show profile block io,cpu for query Query_ID;
show profile cpu,block io,memory,swaps,context switches,source for query Query_ID;
show profile all for query Query_ID;
```


## 1.3. trace工具
<!-- 
MySQL中trace工具的使用
https://blog.csdn.net/weixin_43576564/article/details/90202501

https://baijiahao.baidu.com/s?id=1644815785057748879&wfr=spider&for=pc
-->
&emsp; explain可以查看SQL执行计划，但是无法知道它为什么做这个决策，如果想确定多种索引方案之间是如何选择的或者排序时选择的是哪种排序模式，有什么好的办法吗？  
&emsp; 从MySQL 5.6开始，可以使用trace查看优化器如何选择执行计划。  
&emsp; **通过trace，能够进一步了解为什么优化器选择A执行计划而不是选择B执行计划，或者知道某个排序使用的排序模式，更好地理解优化器行为。**  
&emsp; 如果需要使用，先开启trace，设置格式为JSON，再执行需要分析的SQL，最后查看trace分析结果（在 information_schema.OPTIMIZER_TRACE 中）。  
&emsp; 注意⚠️：开启该功能，会对 MySQL 性能有所影响，因此只建议分析问题时临时开启。  

&emsp; 示例：在test_table中除了d字段，abc字段都有索引。执行如下sql：  

```sql
explain select * from test_table where a=90000 and b=90000 order by a;
```
![image](http://182.92.69.8:8081/img/SQL/sql-171.png)  
&emsp; 通过上面执行计划中 key 这个字段可以看出，该语句使用的是 b 字段的索引 idx_a。实际表 t1 中，a、b 两个字段都有索引，为什么条件中有这两个索引字段却偏偏选了a字段的索引呢？这时就可以使用trace进行分析。  

&emsp; 大致步骤如下：  

1. 开启trace分析器  
2. 执行要查询的sql  
3. 查看分析结果  
4. 关闭trace分析器

        NO.1 开启trace分析器
        MySQL [test]> set session optimizer_trace="enabled=on";
        NO.2 执行要查询的SQL
        MySQL [test]> select * from test_table where a=90000 and b=90000 order by a;
        NO.3 查询分析结果
        MySQL [test]> SELECT * FROM information_schema.OPTIMIZER_TRACE\G
        注意：在返回的steps数组中可以查看详细mysql都干了什么。
        NO.4 关闭trace分析器
        mysql> set session optimizer_trace="enabled=off";


&emsp; TRACE字段中整个文本大致分为三个过程：  

1. 准备阶段：对应文本中的 join_preparation  
2. 优化阶段：对应文本中的 join_optimization  
3. 执行阶段：对应文本中的 join_execution  

&emsp; 使用时，重点关注优化阶段和执行阶段。  


## 1.4. 小结
&emsp; 对比一下三种分析 SQL 方法的特点：

* explain：获取 MySQL 中 SQL 语句的执行计划，比如语句是否使用了关联查询、是否使用了索引、扫描行数等；
* profile：可以清楚了解到SQL到底慢在哪个环节；
* trace：查看优化器如何选择执行计划，获取每个可能的索引选择的代价。



## 1.5. show warnings
&emsp; SHOW WARNINGS显示有关由于当前会话中执行最新的非诊断性语句而导致的条件的信息。  
&emsp; 可以使用mysql show warnings避免一些隐式转换等。  
<!-- 
explain extended + show warnings
阿里的程序员也不过如此，竟被一个简单的SQL查询难住 
https://mp.weixin.qq.com/s/6jjLv2SIBmh2kjHunxJVYA
-->

## 1.6. ~~proceduer analyse()取得建议~~  
&emsp; PROCEDURE ANALYSE()，在优化表结构时可以辅助参考分析语句。  
&emsp; 使用proceduer analyse()对当前已有应用的表类型的判断，该函数可以对数据表中的列的数据类型(字段类型)提出优化建议(Optimal_fieldtype)。  

```sql
SELECT column_name FROM table_name PROCEDURE ANALYSE();
```

## 1.7. sql分析工具
<!-- 
4 款 MySQL 调优工具 
https://mp.weixin.qq.com/s/X5Ik_cpKdk95IDLG-vZg4w
-->

&emsp; 小米开源的sql分析工具：soar-web  