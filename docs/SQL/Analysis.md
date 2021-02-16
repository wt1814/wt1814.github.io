<!-- TOC -->

- [1. EXPLAIN、PROCEDURE ANALYSE(分析)](#1-explainprocedure-analyse分析)
    - [1.1. explain与explain extended](#11-explain与explain-extended)
        - [1.1.1. explain extended](#111-explain-extended)
    - [1.2. show warnings](#12-show-warnings)
    - [1.3. ~~proceduer analyse()取得建议~~](#13-proceduer-analyse取得建议)
    - [1.4. profiling](#14-profiling)
        - [1.4.1. 查看profile开启情况](#141-查看profile开启情况)
        - [1.4.2. 启用profile](#142-启用profile)
        - [1.4.3. 查看执行的SQL列表](#143-查看执行的sql列表)
        - [1.4.4. 查询指定ID的执行详细信息](#144-查询指定id的执行详细信息)
        - [1.4.5. 获取CPU、Block IO等信息](#145-获取cpublock-io等信息)

<!-- /TOC -->

&emsp; **<font color = "lime">explain：type单表查询类型要达到range级别（只检索给定范围的行，使用一个索引来选择行，非全表扫描），extra包含不在其他属性显示，但是又非常重要的信息，常见的不太友好的值，如下：Using filesort，Using temporary。其他重要字段：key、key_len</font>**  

# 1. EXPLAIN、PROCEDURE ANALYSE(分析)  

```meimaid
graph LR
A[SQL分析] --> B(explain)
A --> C(proceduer analyse)
A --> D(profiling)
```

&emsp; **<font color = "lime">SQL分析语句有EXPLAIN与explain extended、show warnings、proceduer analyse、profiling。</font>**  

## 1.1. explain与explain extended
<!-- 
~~
https://mp.weixin.qq.com/s/eJ_ConoGHP6az3IKNe6L2g
https://mp.weixin.qq.com/s?__biz=MzAxODcyNjEzNQ==&mid=2247487641&idx=1&sn=3551d8f82bf8b503041e079b6ce704ce&chksm=9bd0bd01aca734172fff2cda5c4a46bce8f1dabc1b0d44334794510b34b665e3cf8b1f1abced&mpshare=1&scene=1&srcid=&key=00a8e91eefd868fcd64be6325594939523bc619318b02b06053cd6a26de9a9f6490cd967c97a822819178ab39d2507e2b41ba0694bcac89b80ab27e7518e7df3f17aa0d224992a132b90164c45e889c2&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060833&lang=zh_CN&pass_ticket=A8TVciY05jxe73%2ByAqBufT%2F39WMw2DS5UIeWy9gagHorTGRPzk0IoQC5RsOCwRL0
-->

```sql
EXPLAIN SELECT column_name FROM table_name;  
```
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-29.png)  
&emsp; expain出来的信息列分别是id、select_type、table、partitions、 **<font color = "red">type</font>** 、possible_keys、 **<font color = "red">key</font>** 、 **<font color = "red">key_len</font>** 、ref、rows、filtered、 **<font color = "red">Extra</font>** 。  

&emsp; 标注(1,2,3,4,5)是要重点关注的数据  

* type列，连接类型。一个好的sql语句至少要达到range级别。杜绝出现all级别  
* key列，使用到的索引名。如果没有选择索引，值是NULL。可以采取强制索引方式  
* key_len列，索引长度  
* rows列，扫描行数。该值是个预估值  
* extra列，详细说明。注意常见的不太友好的值有：Using filesort, Using temporary  

|列名|用途|
|---|---|
|id	|每一个SELECT关键字查询语句都对应一个唯一id|
|select_type|SELECT关键字对应的查询类型|
|table|表名|
|partitions	|匹配的分区信息|
|type|单表的访问方法|
|possible_keys|可能用到的索引|
|key|实际使用到的索引|
|key_len|实际使用到的索引长度|
|ref|当使用索引列等值查询时，与索引列进行等值匹配的对象信息|
|rows|预估需要读取的记录条数|
|filtered|某个表经过条件过滤后剩余的记录条数百分比|
|Extra|额外的一些信息|

<!--
id相同，执行顺序从上往下
id全不同，如果是子查询，id的序号会递增，id值越大优先级越高，越先被执行
id部分相同，执行顺序是先按照数字大的先执行，然后数字相同的按照从上往下的顺序执行
-->

* id：SELECT识别符。这是SELECT的查询序列号。  
    * id值相同  
    &emsp; id值相同一般出现在多表关联的场景，访问表的顺序是从上到下。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-30.png)  
    &emsp; 两个id 都为1，先访问b表然后访问a表。  
    * id值不同  
    &emsp; id 值不同的情况，从大到小执行，值越大越先开始执行或者被访问。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-31.png)  
    &emsp; 从结果来看，id为2那一行的子查询先被执行。然后再去访问id=1中a表。  
    * id包含了相同和不同的情况。  
    &emsp; 该情况一般是先有2个表或者子查询和表join，然后再和第三个表关联查询。比如  
    
        ```sql
        EXPLAIN SELECT t2.* FROM(SELECT t3.id FROM t3 WHERE t3.other_column = '') s1,t2 WHERE s1.id = t2.id;
        ```
        ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-32.png)  
        &emsp; 分析结果可看出，先走id最大的2，也就是先走括号里面的查t3表的语句。走完查t3后，顺序执行，有一个，derived是衍生的意思，意思是在执行完t3查询后的s1虚表基础上，结果中的2，就是id为2的。最后执行的查t2表。  

* select_type：常见的有如下6种SIMPLE、PRIMARY、SUBQUERY、DERIVED、UNION、UNION RESULT，指出查询的类型：普通查询、联合查询、子查询等复杂的查询。  
    * SIMPLE：最简单的查询，查询中不包含子查询或者UNION。  
    * PRIMARY：查询中若包含任何复杂的子查询，最外层查询则被标记为PRIMARY，也就是最后被执行的语句。  
    * SUBQUERY：在SELECT from或者WHERE列表中包含了子查询  
    * DERIVED：导出表的SELECT(FROM子句的子查询)；在FROM列表中包含的子查询被标记为DERIVED（衍生）MySQL会递归执行这些子查询，把结果放在临时表里。  
    * UNION：UNION中的第二个或后面的SELECT语句；若第二个SELECT出现在UNION之后，则被标记为UNION；若UNION包含在FROM子句的子查询中，外层SELECT将被标记为DERIVED  
    * UNION RESULT：UNION的结果；  
    * DEPENDENT SUBQUERY：子查询中的第一个 SELECT，取决于外面的查询. 即子查询依赖于外层查询的结果. 出现该值的时候一定要特别注意，可能需要使用join的方式优化子查询。  
    * DEPENDENT UNION：UNION中的第二个或后面的SELECT语句，取决于外面的查询；  

* table：（查询涉及的表或衍生表）  
&emsp; 其值为表名或者表的别名，表示访问哪一个表。  
&emsp; 当from中有子查询的时候，表名是derivedN的形式，其中N指向子查询，也就是explain结果中的下一列  
&emsp; 当有union result的时候，表名是union 1,2等的形式，1,2表示参与union的query id  
&emsp; 注意MySQL对待这些表和普通表一样，但是这些临时表是没有任何索引的。数据量大的情况下可能会有性能问题。  

* **<font color = "red">type：联接类型，表示访问表的方式。</font>**  
&emsp; 从最好到最差的结果依次如下:system > const > eq_ref > ref > range > index > ALL。<font color = "red">一个好的SQL语句至少要达到range级别。杜绝出现all级别。</font>下面给出各种联接类型，按照从最佳类型到最坏类型进行排序：  
    * System：表仅有一行(=系统表)。这是const联接类型的一个特例。  
    * Const：表最多有一个匹配行，它将在查询开始时被读取。因为仅有一行，在这行的列值可被优化器剩余部分认为是常数。const表很快，因为它们只读取一次!  
    * eq_ref：对于每个来自于前面的表的行组合，从该表中读取一行。这可能是最好的联接类型，除了const类型。  
    * Ref：对于每个来自于前面的表的行组合，所有有匹配索引值的行将从这张表中读取。  
    * ref_or_null：该联接类型如同ref，但是添加了MySQL可以专门搜索包含NULL值的行。  
    * index_merge：该联接类型表示使用了索引合并优化方法。  
    * unique_subquery：该类型替换了下面形式的IN子查询的ref: value IN (SELECT primary_key FROM single_table WHERE some_expr) unique_subquery是一个索引查找函数,可以完全替换子查询,效率更高。  
    * index_subquery：该联接类型类似于unique_subquery。可以替换IN子查询，但只适合下列形式的子查询中的非唯一索引: value IN (SELECT key_column FROM single_table WHERE some_expr)  
    * <font color = "red">Range：只检索给定范围的行，使用一个索引来选择行。</font>  
    * Index：该联接类型与ALL相同，除了只有索引树被扫描。这通常比ALL快，因为索引文件通常比数据文件小。  
    * ALL：对于每个来自于先前的表的行组合，进行完整的表扫描。  
* possible_keys：指出MySQL能使用哪个索引在该表中找到行。  
* **<font color = "red">key：显示MySQL实际决定使用的键(索引)。如果没有选择索引，键是NULL。</font><font color = "lime">很少的情况下，MYSQL会选择优化不足的索引。这种情况下，可以在SELECT语句中使用USE INDEX（indexname）来强制使用一个索引或者用IGNORE INDEX（indexname）来强制MYSQL忽略索引。</font>**  
* **<font color = "red">key_len：索引长度，显示MySQL决定使用的键长度。如果键是NULL，则长度为NULL。</font>**  
* ref：显示使用哪个列或常数与key一起从表中选择行。  
* rows：扫描行数。该值是个预估值。显示MySQL认为它执行查询时必须检查的行数。多行之间的数据相乘可以估算要处理的行数。  
* filtered：显示了通过条件过滤出的行数的百分比估计值。  
* **<font color = "red">extra：该列包含MySQL解决查询的详细信息。注意，常见的不太友好的值，如下：Using filesort，Using temporary，意思MYSQL根本不能使用索引，常出现在使用order by。</font>**  
    * Distinct：MySQL发现第1个匹配行后，停止为当前的行组合搜索更多的行。  
    * Not exists：MySQL能够对查询进行LEFT JOIN优化，发现1个匹配LEFT JOIN标准的行后，不再为前面的的行组合在该表内检查更多的行。  
    * range checked for each record (index map: #):MySQL没有发现好的可以使用的索引，但发现如果来自前面的表的列值已知，可能部分索引可以使用。  
    * Using filesort：额外排序。MySQL需要额外的一次传递，以找出如何按排序顺序检索行。  
    * Using index:从只使用索引树中的信息而不需要进一步搜索读取实际的行来检索表中的列信息。  
    * Using temporary：使用了临时表。为了解决查询,MySQL需要创建一个临时表来容纳结果。这通常发生在对不同的列集进行ORDER BY上，而不是GROUP BY上。  
    * Using where:WHERE 子句用于限制哪一个行匹配下一个表或发送到客户。  
    * Using sort_union(...), Using union(...), Using intersect(...):这些函数说明如何为index_merge联接类型合并索引扫描。  
    * Using index for group-by:类似于访问表的Using index方式，Using index for group-by表示MySQL发现了一个索引，可以用来查询GROUP BY或DISTINCT查询的所有列,而不要额外搜索硬盘访问实际的表。  

### 1.1.1. explain extended
&emsp; <font color = "red">用explain extended查看执行计划会比explain多一列 filtered。filtered列给出了一个百分比的值，这个百分比值和rows列的值一起使用，可以估计出那些将要和explain中的前一个表进行连接的行的数目。前一个表就是指explain 的 id列的值比当前表的id小的表。</font>  

## 1.2. show warnings
&emsp; SHOW WARNINGS显示有关由于当前会话中执行最新的非诊断性语句而导致的条件的信息。  
&emsp; 使用mysql show warnings避免一些隐式转换。  
<!-- 
explain extended + show warnings
阿里的程序员也不过如此，竟被一个简单的SQL查询难住 
https://mp.weixin.qq.com/s/6jjLv2SIBmh2kjHunxJVYA
-->

## 1.3. ~~proceduer analyse()取得建议~~  
&emsp; PROCEDURE ANALYSE()，在优化表结构时可以辅助参考分析语句。  
&emsp; 使用proceduer analyse()对当前已有应用的表类型的判断，该函数可以对数据表中的列的数据类型（字段类型）提出优化建议（Optimal_fieldtype）。  

```sql
SELECT column_name FROM table_name PROCEDURE ANALYSE();
```

## 1.4. profiling  
&emsp; 使用profiling命令可以了解SQL语句消耗资源的详细信息（每个执行步骤的开销）。  

### 1.4.1. 查看profile开启情况  
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

### 1.4.2. 启用profile  
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
&emsp; 在连接关闭后，profiling 状态自动设置为关闭状态。  

### 1.4.3. 查看执行的SQL列表  
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

### 1.4.4. 查询指定ID的执行详细信息  

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

### 1.4.5. 获取CPU、Block IO等信息  

```sql
show profile block io,cpu for query Query_ID;
show profile cpu,block io,memory,swaps,context switches,source for query Query_ID;
show profile all for query Query_ID;
```