

<!-- TOC -->

- [1. 分区](#1-分区)
    - [1.1. 什么是分区](#11-什么是分区)
    - [1.2. 怎么分区](#12-怎么分区)
        - [1.2.1. 分区键](#121-分区键)
        - [1.2.2. 分区路由(MySql分区类型)](#122-分区路由mysql分区类型)
    - [1.3. 管理分区(增、删分区)](#13-管理分区增删分区)
    - [1.4. 分区表使用](#14-分区表使用)

<!-- /TOC -->

# 1. 分区  
## 1.1. 什么是分区  
&emsp; **<font color = "red">数据分区是一种物理数据库的设计技术，它的目的是为了在特定的SQL操作中减少数据读写的总量以缩减响应时间。</font>**  
&emsp; 分区把存放数据的文件分成了许多小块。例如mysql中myisam的一张表对应三个文件MYD、MYI、frm，根据一定的规则把数据文件(MYD)和索引文件(MYI)进行了分割。  
&emsp; 分区并不是生成新的数据表，而是将表的数据均衡分摊到不同的硬盘、系统或是不同服务器存储介子中。就访问数据库应用而言，逻辑上就只有一个表或者一个索引，但实际上这个表可能有N个物理分区对象组成，每个分区都是一个独立的对象，可以独立处理，可以作为表的一部分进行处理。分区对应用来说是完全透明的，不影响应用的业务逻辑。  

&emsp; **分区使用的场景(优点)：**  
1. 表非常大以至于无法全部都放在内存中，或者<font color = "clime">只在表的最后部分有热点数据，其他都是历史数据。</font>  
2. 分区表的数据容易维护，如：想批量删除数据可以清楚整个分区的方式，另外，还可以对一个独立分区进行优化、检查、修复等操作。  
3. 分区的数据可以分布在不同物理设备上，分区可以存储更多的数据，高效地利用多个硬件设备。  
4. 可以使用分区表来避免某些特殊的瓶颈，如InnoDB单个索引的互斥访问。  
5. 如果需要，还可以备份和恢复独立的分页去，这在非常大的数据集场景下效果非常有效。  
6. 优化查询，在where字句中包含分区列时，可以只使用必要的分区来提高查询效率，同时在涉及sum()和count()这类聚合函数的查询时，可以在每个分区上面并行处理，最终只需要汇总所有分区得到的结果。  

&emsp; **分区限制(缺点)：**  
1. 一个表最多只能有1024个分区(mysql5.6之后支持8192个分区)。  
2. 在mysql5.1中分区表达式必须是整数，或者返回整数表达式，在5.5之后，某些场景可以直接使用字符串和日期类型列进行分区。  
3. <font color = "clime">如果分区字段中有主键或者唯一索引列，那么所有主键列和唯一索引列都必须包含进来，如果表中有主键或唯一索引，那么分区键必须是主键或唯一索引。</font>  
4. 分区表中无法使用外键约束。  
5. <font color = "red">mysql数据库支持的分区类型为水平分区，并不支持垂直分区，因此，mysql数据库的分区中索引是局部分区索引，一个分区中既存放了数据又存放了索引，而全局分区是指的数据库放在各个分区中，但是所有的数据的索引放在另外一个对象中。</font>  
6. 目前mysql不支持空间类型和临时表类型进行分区。不支持全文索引。  

## 1.2. 怎么分区  
### 1.2.1. 分区键  
&emsp; <font color = "red">分区依据的字段必须是主键的一部分</font>，分区是为了快速定位数据，因此该字段的搜索频次较高应作为强检索字段，否则依照该字段分区毫无意义。  

### 1.2.2. 分区路由(MySql分区类型)  
&emsp; range分区、list分区、columns分区、hash分区、key分区、子分区。  

&emsp; **1. range分区**  
&emsp; 按照RANGE分区的表，每个分区包含那些分区表达式的值位于一个给定的连续区间内的行。一般使用这种分区方式大都是对连续的值进行分区，常见的如：按年份，日期，连续的数字进行分区。  

```sql
-- 语法
create table <table> (
   // 字段
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=1
partition by range (分区字段) (
  partition <分区名称> values less than (Value),
  partition <分区名称> values less than (Value),
  ...
  partition <分区名称> values less than maxvalue
);
```

    range：表示按范围分区；  
    分区字段：表示要按照哪个字段进行分区，可以是一个字段名，也可以是对某个字段进行表达式运算如year(create_time)，使用range最终的值必须是数字；  
    分区名称: 要保证不同，也可以采用p0、p1、p2 这样的分区名称；  
    less than : 表示小于；  
    Value : 表示要小于某个具体的值，如 less than (10) 那么分区字段的值小于10的都会被分到这个分区；  
    maxvalue: 表示一个最大的值；  

&emsp; 分区可以在创建表的时候进行分区，也可以在创建表之后进行分区。  

```sql
alter table <table> partition by RANGE(id) (
    PARTITION p0 VALUES LESS THAN (value1),
    PARTITION p1 VALUES LESS THAN (value2),
    PARTITION p4 VALUES LESS THAN MAXVALUE 
);
```
&emsp; 在创建分区的时候经常会遇到这个错误：A PRIMARY KEY must include all columns in the table’s partitioning function。意思是说分区的字段必须是要包含在主键当中。可以使用PRIMARY KEY (id,xxx)来将多个字段作为主键。在做分区表时，选择分区的依据字段时要谨慎，需要仔细斟酌这个字段拿来做为分区依据是否合适，这个字段加入到主键中做为复合主键是否适合。  
&emsp; 使用range分区时表结构要么没有主键，要么分区字段必须是主键。  

&emsp; **4. hash分区**  
&emsp; HASH分区主要用来分散热点读取，确保数据在预先确定数目的分区中平均分布，所要做的只是基于将要被哈希的列值指定一个列值或表达式，以及指定被分区的表将要被分割成的分区数量。一个表执行hash分区，mysql会对分区键应用一个散列函数，以此确定数据应该放在n个分区中的哪一个分区。  
&emsp; mysql支持两种hash分区：  

* 常规hash分区和线性hash分区(linear hash分区)，常规hash分区使用的是取模算法，对应一个表达式expr是可以计算出它被保存到哪个分区中，N = MOD(expr, num)  
* 线性hash分区使用的是一个线性的2的幂运算法则。  

&emsp; 常规hash分区方式，通过取模的方式来数据尽可能平均分布在每个分区，让每个分区管理的数据都减少，提高查询效率，可是当要增加分区时或者合并分区，会有问题，假设原来是5个常规hash分区，现在需要增加一个常规分区，原来的取模算法是MOD(expr, 5)，根据余数0~4分布在5个分区中，现在新增一个分区后，取模算法变成MOD(expr, 6)，根据余数0~6分区在6个分区中，原来5个分区的数据大部分都需要通过重新计算进行重新分区。  
&emsp; 常规hash分区在管理上带来了的代价太大，不适合需要灵活变动分区的需求。为了降低分区管理上的代价，mysql提供了线性hash分区，分区函数是一个线性的2的幂的运算法则。同样线性hash分区的记录被存在那个分区也是能被计算出来的。线性hash分区的优点是在分区维护(增加、删除、合并、拆分分区)时，mysql能够处理的更加迅速，缺点是：对比常规hash分区，线性hash各个分区之间数据的分布不太均衡。  

```sql
-- HASH
create table <table> (
   // 字段
) ENGINE=数据库引擎  DEFAULT CHARSET=utf8 AUTO_INCREMENT=1
PARTITION BY HASH(expr)
PARTITIONS <num>;
```
&emsp; hash分区只需要指定要分区的字段和要分成几个分区。expr是一个字段值或者基于某列值云散返回的一个整数，expr可以是mysql中有效的任何函数或者其它表达式，只要它们返回一个即非常熟也非随机数的整数。num表示分区数量。  

&emsp; **6.子分区**  
&emsp; 子分区(subpartition)：是分区表中对每个分区的再次分割，又被称为复合分区，支持对range和list进行子分区，子分区即可以使用hash分区也可以使用key分区。复合分区适用于保存非常大量的数据记录。  
1. 如果一个分区中创建了子分区，其他分区也要有子分区；  
2. 如果创建了了分区，每个分区中的子分区数必有相同；  
3. 同一分区内的子分区，名字不相同，不同分区内的子分区名子可以相同；  

```sql
-- 根据年进行分区
-- 再根据天数分区
-- 3个range分区(p0,p1,p2)又被进一步分成2个子分区，实际上整个分区被分成了 3 x 2 = 6个分区
create table ts (
   id int, 
   purchased date
) 
partition by range(year(purchased))
subpartition by hash(to_days(purchased)) subpartitions 2 
(
   partition p0 values less than (1990),
   partition p0 values less than (2000),
   partition p0 values less than maxvalue
);
```

## 1.3. 管理分区(增、删分区)  
&emsp; mysql不禁止在分区键值上使用null，分区键可能是一个字段或者一个用户定义的表达式，一般情况下，mysql的分区把null值当做零值或者一个最小值进行处理。range分区中，null值会被当做最小值来处理；list分区中null值必须出现在枚举列表中，否则不被接受；hash/key分区中，null值会被当做领值来处理。  
&emsp; mysql提供了添加、删除、重定义、合并、拆分分区的命令，这些操作都可以通过alter table命令来实现。  

```sql
-- 删除list或者range分区(同时删除分区对应的数据)
alter table <table> drop partition <分区名称>;
-- 新增分区
-- range添加新分区
alter table <table> add partition(partition p4 values less than MAXVALUE);
-- hash重新分区
alter table <table> add partition partitions 4;
```
&emsp; 删除分区：删除分区后，分区中原有的数据也会随之删除！  

```sql
alter table article_range drop PARTITION p201808
```

&emsp; 销毁分区：  

```sql
alter table article_key coalesce partition 6
```
&emsp; key/hash分区的管理不会删除数据，但是每一次调整(新增或销毁分区)都会将所有的数据重写分配到新的分区上。==效率极低==，最好在设计阶段就考虑好分区策略。  

## 1.4. 分区表使用  
&emsp; 在where条件带入分区键。  
