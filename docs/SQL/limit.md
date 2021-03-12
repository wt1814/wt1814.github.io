
<!-- TOC -->

- [1. limit](#1-limit)
    - [1.1. limit分页](#11-limit分页)
    - [1.2. 神坑！MySQL中order by与limit一起用的问题](#12-神坑mysql中order-by与limit一起用的问题)

<!-- /TOC -->

# 1. limit
## 1.1. limit分页

<!-- 
~~
 多数人都曾遇到过的 limit 问题，深入浅出 MySQL 优先队列 
 https://mp.weixin.qq.com/s/ejZ4f828dQnXyNE6dcLxOw
-->
1. 直接使用数据库提供的SQL语句  
&emsp; 语句样式: MySQL中，可用如下方法: SELECT * FROM 表名称 LIMIT M,N  
&emsp; 适应场景: 适用于数据量较少的情况(元组百/千级)  
&emsp; 原因/缺点: 全表扫描，速度会很慢 且 有的数据库结果集返回不稳定(如某次返回1,2,3，另外的一次返回2,1,3)。Limit限制的是从结果集的M位置处取出N条输出，其余抛弃。  

2. <font color = "red">建立主键或唯一索引，利用索引</font>  
&emsp; 语句样式: MySQL中，可用如下方法: SELECT * FROM 表名称 WHERE id_pk > (pageNum*10) LIMIT M  
&emsp; 适应场景: 适用于数据量多的情况(元组数上万)  
&emsp; 原因: 索引扫描，速度会很快. 有朋友提出: 因为数据查询出来并不是按照pk_id排序的，所以会有漏掉数据的情况，只能方法3  

3. <font color = "red">基于索引再排序</font>  
&emsp; 语句样式: MySQL中，可用如下方法: SELECT * FROM 表名称 WHERE id_pk > (pageNum*10) ORDER BY id_pk ASC LIMIT M  
&emsp; 适应场景: 适用于数据量多的情况(元组数上万)。最好ORDER BY后的列对象是主键或唯一索引，使得ORDERBY操作能利用索引被消除但结果集是稳定的(稳定的含义，参见方法1)  
&emsp; 原因: 索引扫描，速度会很快. 但MySQL的排序操作，只有ASC没有DESC(DESC是假的，未来会做真正的DESC，期待...).  

4. 基于索引使用prepare  
&emsp; 第一个问号表示pageNum，第二个？表示每页元组数  
&emsp; 语句样式: MySQL中，可用如下方法: PREPARE stmt_name FROM SELECT * FROM 表名称 WHERE id_pk > (？* ？) ORDER BY id_pk ASC LIMIT M  
&emsp; 适应场景: 大数据量  
&emsp; 原因: 索引扫描，速度会很快。prepare语句又比一般的查询语句快一点。  

5. 利用MySQL支持ORDER操作可以利用索引快速定位部分元组，避免全表扫描  
&emsp; 比如: 读第1000到1019行元组(pk是主键/唯一键)。  
&emsp; SELECT * FROM your_table WHERE pk>=1000 ORDER BY pk ASC LIMIT 0,20  

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


## 1.2. 神坑！MySQL中order by与limit一起用的问题 
<!-- 
神坑！MySQL中order by与limit不要一起用！ 
https://mp.weixin.qq.com/s/93rBBFlfTx58OjD5S_OlAw

-->