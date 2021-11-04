
<!-- TOC -->

- [1. limit](#1-limit)
    - [1.1. 逻辑分页和物理分页](#11-逻辑分页和物理分页)
    - [1.2. limit物理分页](#12-limit物理分页)
    - [1.3. limit效率问题](#13-limit效率问题)
    - [1.4. 神坑！MySQL中order by与limit一起用的问题](#14-神坑mysql中order-by与limit一起用的问题)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 普通Limit语句需要全表扫描。  
&emsp; 建立主键或唯一索引，利用索引：`SELECT * FROM 表名称 WHERE id_pk > (pageNum*10) LIMIT M`  
&emsp; 基于索引再排序：`SELECT * FROM 表名称 WHERE id_pk > (pageNum*10) ORDER BY id_pk ASC LIMIT M`
2. **<font color = "blue">ORDER BY与limit（分页再加排序）</font>**  
&emsp; ORDER BY排序后，用LIMIT取前几条，发现返回的结果集的顺序与预期的不一样。    
&emsp; 如果order by的列有相同的值时，MySQL会随机选取这些行，`为了保证每次都返回的顺序一致可以额外增加一个排序字段（比如：id），用两个字段来尽可能减少重复的概率。`  

# 1. limit

<!-- 
面试官：谈谈MySQL的limit用法、逻辑分页和物理分页 
https://mp.weixin.qq.com/s/KcaLyboO0MltR6out67_DA

MySQL 千万数据量深分页优化, 拒绝线上故障！ 
https://mp.weixin.qq.com/s?__biz=Mzg4NDU0Mjk5OQ==&mid=2247491478&idx=1&sn=74ae760980211ac4e08f2182d217da0e&chksm=cfb7c4eef8c04df89d2805bc7a294ef97dad33c482f5f675ff5dcbf0213516a6c394c5cee7e6&scene=178&cur_album_id=1674476913974624262#rd
-->

## 1.1. 逻辑分页和物理分页  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-162.png)  

## 1.2. limit物理分页

<!-- 
~~
 多数人都曾遇到过的 limit 问题，深入浅出 MySQL 优先队列 
 https://mp.weixin.qq.com/s/ejZ4f828dQnXyNE6dcLxOw
-->
1. 直接使用数据库提供的SQL语句  
&emsp; 语句样式：MySQL中，可用如下方法：`SELECT * FROM 表名称 LIMIT M,N`  
&emsp; 适应场景：适用于数据量较少的情况(元组百/千级)。  
&emsp; 原因/缺点：全表扫描，速度会很慢 且 有的数据库结果集返回不稳定(如某次返回1,2,3，另外的一次返回2,1,3)。Limit限制的是从结果集的M位置处取出N条输出，其余抛弃。  

2. <font color = "clime">建立主键或唯一索引，利用索引</font>  
&emsp; 语句样式：MySQL中，可用如下方法：`SELECT * FROM 表名称 WHERE id_pk > (pageNum*10) LIMIT M`。  
&emsp; 适应场景：适用于数据量多的情况(元组数上万)。  
&emsp; 原因：索引扫描，速度会很快。 有朋友提出: 因为数据查询出来并不是按照pk_id排序的，所以会有漏掉数据的情况，只能方法3。  

3. <font color = "red">基于索引再排序</font>  
&emsp; 语句样式：MySQL中，可用如下方法：`SELECT * FROM 表名称 WHERE id_pk > (pageNum*10) ORDER BY id_pk ASC LIMIT M`。  
&emsp; 适应场景：适用于数据量多的情况(元组数上万)。最好ORDER BY后的列对象是主键或唯一索引，使得ORDERBY操作能利用索引被消除但结果集是稳定的(稳定的含义，参见方法1)。  
&emsp; 原因：索引扫描，速度会很快. 但MySQL的排序操作，只有ASC没有DESC(DESC是假的，未来会做真正的DESC，期待...)。 

4. 基于索引使用prepare  
&emsp; 第一个问号表示pageNum，第二个？表示每页元组数  
&emsp; 语句样式：MySQL中，可用如下方法：`PREPARE stmt_name FROM SELECT * FROM 表名称 WHERE id_pk > (？* ？) ORDER BY id_pk ASC LIMIT M`  
&emsp; 适应场景：大数据量  
&emsp; 原因：索引扫描，速度会很快。prepare语句又比一般的查询语句快一点。  

5. 利用MySQL支持ORDER操作可以利用索引快速定位部分元组，避免全表扫描。  
&emsp; 比如：读第1000到1019行元组(pk是主键/唯一键)。  
```sql
ELECT * FROM your_table WHERE pk>=1000 ORDER BY pk ASC LIMIT 0,20  
```
6. 利用"子查询/连接+索引"快速定位元组的位置，然后再读取元组。  
&emsp; 比如(id是主键/唯一键，蓝色字体时变量)  
&emsp; 利用子查询示例：  

    ```java
    SELECT * FROM your_table WHERE id <= 
    (SELECT id FROM your_table ORDER BY id desc LIMIT ($page-1)*$pagesize ORDER BY id desc 
    LIMIT $pagesize
    ```
    &emsp; 利用连接示例:    

    ```sql
    SELECT * FROM your_table AS t1 
    JOIN (SELECT id FROM your_table ORDER BY id desc LIMIT ($page-1)*$pagesize AS t2 
    WHERE t1.id <= t2.id ORDER BY t1.id desc LIMIT $pagesize;
    ```

## 1.3. limit效率问题  
&emsp; 有一个需求，就是从vote_record_memory表中查出3600000到3800000的数据，此时在id上加个索引，索引的类型是Normal，索引的方法是BTREE，分别用两种方法查询。  

```sql
-- 方法1
SELECT * FROM vote_record_memory vrm  LIMIT 3600000,20000 ;

-- 方法2
SELECT * FROM vote_record_memory vrm WHERE vrm.id >= 3600000 LIMIT 20000 
```

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-163.png)  
&emsp; 会发现，方法2的执行效率远比方法1的执行效率高，几乎是方法1的九分之一的时间。  

&emsp; 为什么方法1的效率低，而方法二的效率高呢？  

* 分析一、  
&emsp; 因为在方法1中，使用的单纯的limit。limit随着行偏移量的增大，当大到一定程度后，会出现效率下降。而方法2用上索引加where和limit，性能基本稳定，受偏移量和行数的影响不大。  
* 分析二、  
&emsp; 用explain来分析  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-164.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-165.png)  
&emsp; 可见，limit语句的执行效率未必很高，因为会进行全表扫描，这就是为什么方法1扫描的的行数是400万行的原因。方法2的扫描行数是47945行，这也是为什么方法2执行效率高的原因。尽量避免全表扫描查询，尤其是数据非常庞大，这张表仅有400万条数据，方法1和方法就有这么大差距，可想而知上千万条的数据呢。  


## 1.4. 神坑！MySQL中order by与limit一起用的问题 
<!-- 
神坑！MySQL中order by与limit不要一起用！ 
https://mp.weixin.qq.com/s/93rBBFlfTx58OjD5S_OlAw
-->
&emsp; ORDER BY排序后，用LIMIT取前几条，发现返回的结果集的顺序与预期的不一样。  

&emsp; 如果order by的列有相同的值时，MySQL 会随机选取这些行，为了保证每次都返回的顺序一致可以额外增加一个排序字段（比如：id），用两个字段来尽可能减少重复的概率。  
