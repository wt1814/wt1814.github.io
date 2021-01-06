
<!-- TOC -->

- [1. SQL语句优化](#1-sql语句优化)
    - [1.1. 基本查询优化](#11-基本查询优化)
        - [1.1.1. 查询结果集优化](#111-查询结果集优化)
        - [1.1.2. group by优化](#112-group-by优化)
        - [1.1.3. Order by优化](#113-order-by优化)
        - [1.1.4. like优化](#114-like优化)
    - [1.2. 子查询优化](#12-子查询优化)
    - [1.3. 关联查询优化](#13-关联查询优化)
    - [1.4. 组合查询优化](#14-组合查询优化)
    - [1.5. INSERT的优化](#15-insert的优化)
    - [1.6. 快速删除大量（百万级）数据](#16-快速删除大量百万级数据)

<!-- /TOC -->

# 1. SQL语句优化
&emsp; MySql官网提供的优化方案：https://dev.mysql.com/doc/refman/5.7/en/optimization.html  

## 1.1. 基本查询优化  
### 1.1.1. 查询结果集优化  
&emsp; 避免使用select \*。count(1)或count(列)代替count(*)。  

    count(*) 和 count(1)和count(列名)区别：  
    1. 执行效果上： 
        count(*)包括了所有的列，相当于行数，在统计结果的时候，不会忽略列值为NULL  
        count(1)忽略所有列，用1代表代码行，在统计结果的时候，不会忽略列值为NULL  
        count(列名)只包括列名那一列，在统计结果的时候，会忽略列值为空（这里的空不是只空字符串或者0，而是表示null）的计数，即某个字段值为NULL时，不统计。
    
    2. 执行效率上：  
        列名为主键，count(列名)会比count(1)快  
        列名不为主键，count(1)会比count(列名)快  
        如果表多个列并且没有主键，则 count（1） 的执行效率优于 count（*）  
        如果有主键，则 select count（主键）的执行效率是最优的  
        如果表只有一个字段，则 select count（*）最优。

### 1.1.2. group by优化 
1. 优化GROUP BY: 提高GROUP BY语句的效率，<font color = "red">可以通过将不需要的记录在GROUP BY之前过滤掉。即联合使用where子句和having子句。</font>  
2. 在默认情况下，MySQL中的GROUP BY语句会对其后出现的字段进行默认排序（非主键情况），就好比使用ORDER BY col1,col2,col3…所以在后面跟上具有相同列（与GROUP BY后出现的col1,col2,col3…相同）ORDER BY子句并没有影响该SQL的实际执行性能。  

&emsp; 那么就会有这样的情况出现，对查询到的结果是否已经排序不在乎时，可以使用ORDER BY NULL禁止排序达到优化目的。下面使用EXPLAIN命令分析SQL。  
&emsp; 在user_1中执行select id, sum(money) form user_1 group by name时，会默认排序（注意group by后的column是非index才会体现group by的排序，如果是primary key，那之前说过了InnoDB默认是按照主键index排好序的）  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-53.png)  
&emsp; 不禁止排序，即不使用ORDER BY NULL时：有明显的Using filesort。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-54.png)  
&emsp; 当使用ORDER BY NULL禁止排序后，Using filesort不存在  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-55.png)  

### 1.1.3. Order by优化 
<!-- 
MySQL中order by语句的实现原理以及优化手段 
https://mp.weixin.qq.com/s/FykC_mfqJH5oics3wIzBQA
-->
&emsp; MySQL可以使用一个索引来满足ORDER BY子句的排序，而不需要额外的排序，但是需要满足以下几个条件：  
1. WHERE 条件和OREDR BY使用相同的索引：即key_part1与key_part2是复合索引，where中使用复合索引中的key_part1  

    SELECT*FROM user WHERE key_part1=1 ORDER BY key_part1 DESC, key_part2 DESC;
2. 而且ORDER BY顺序和索引顺序相同：  

    SELECT*FROM user ORDER BY key_part1, key_part2;
3. 并且要么都是升序要么都是降序：  

    SELECT*FROM user ORDER BY key_part1 DESC, key_part2 DESC;

&emsp; 但<font color = "lime">以下几种情况则不使用索引</font>：  
1. ORDER BY中混合ASC和DESC：  

    SELECT*FROM user ORDER BY key_part1 DESC, key_part2 ASC;
2. 查询行的关键字与ORDER BY所使用的不相同，即WHERE 后的字段与ORDER BY 后的字段是不一样的  

    SELECT*FROM user WHERE key2 = ‘xxx’ ORDER BY key1;
3. ORDER BY对不同的关键字使用，即ORDER BY后的关键字不相同  
    
    SELECT*FROM user ORDER BY key1, key2;

### 1.1.4. like优化  
<!-- 
like %%怎么优化
https://mp.weixin.qq.com/s/ygvuP35B_sJAlBHuuEJhfg
-->

## 1.2. 子查询优化  
&emsp; <font color = "red">使用子查询有时候可以使用更有效的JOIN连接代替，这是因为MySQL中不需要在内存中创建临时表完成SELECT子查询与主查询两部分查询工作。但是并不是所有的时候都成立，最好是在on关键字后面的列有索引的话，效果会更好！</font>  
&emsp; 比如在表major中major_id是有索引的：  

```sql
select * from student u left join major m on u.major_id=m.major_id where m.major_id is null;
```

&emsp; 而通过子查询时，在内存中创建临时表完成SELECT子查询与主查询两部分查询工作，会有一定的消耗  

```sql
select * from student u where major_id not in (select major_id from major);
```

## 1.3. 关联查询优化  
1. 在进行多表关联时，多用Where语句把单个表的结果集最小化，多用聚合函数汇总结果集后再与其它表做关联，以使结果集数据量最小化  
......

## 1.4. 组合查询优化  
1. MySQL处理UNION的策略是先创建临时表，然后再把各个查询结果插入到临时表中，最后再来做查询。因此很多优化策略在UNION查询中都没有办法很好的时候。经常需要手动将WHERE、LIMIT、ORDER BY等字句“下推”到各个子查询中，以便优化器可以充分利用这些条件先优化。  
2. 如果结果集允许重复的话,使用UNION ALL代替UNION。  

## 1.5. INSERT的优化  
1. 尽量使用多个值表的 INSERT 语句，这种方式将大大缩减客户端与数据库之间的连接、关闭等消耗。（同一客户的情况下），即：  

    INSERT INTO tablename values(1,2),(1,3),(1,4)  
2. 如果在不同客户端插入很多行，可使用INSERT DELAYED语句得到更高的速度，DELLAYED含义是让INSERT语句马上执行，其实数据都被放在内存的队列中。并没有真正写入磁盘。LOW_PRIORITY刚好相反。  
3. 将索引文件和数据文件分在不同的磁盘上存放（InnoDB引擎是在同一个表空间的）。  
4. 如果批量插入，则可以增加bluk_insert_buffer_size变量值提供速度（只对MyISAM有用）  
5. 当从一个文本文件装载一个表时，使用LOAD DATA INFILE，通常比INSERT语句快20倍。  

## 1.6. 快速删除大量（百万级）数据  

<!-- 
https://jingyan.baidu.com/article/48b37f8d2e0cad1a65648879.html
-->
