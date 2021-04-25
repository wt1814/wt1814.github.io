
<!-- TOC -->

- [1. MySql存储引擎](#1-mysql存储引擎)
    - [1.1. 存储引擎操作](#11-存储引擎操作)
    - [1.2. InnoDB存储引擎](#12-innodb存储引擎)
    - [1.3. MyISAM存储引擎](#13-myisam存储引擎)
    - [1.4. ~~InnoDB与MyISAM的区别总结~~](#14-innodb与myisam的区别总结)
    - [1.5. 选择合适的存储引擎](#15-选择合适的存储引擎)

<!-- /TOC -->

<!-- 
MySQL
https://www.cnblogs.com/geaozhang/category/1326927.html
-->

# 1. MySql存储引擎
1. MySQL常见的存储引擎有InnoDB、MyISAM、Memory、NDB。<font color = "red">InnoDB现在是MySQL默认的存储引擎，支持事务、行级锁定和外键。</font>  
2. MySQL的存储引擎是针对表的，不是针对库的。也就是说在一个数据库中可以使用不同的存储引擎。但是不建议这样做。 
3. MySQL是插件式的存储引擎，其中存储引擎分很多种。只要实现符合mysql存储引擎的接口，可以开发自己的存储引擎! 
4. **<font color = "red">所有跨存储引擎的功能都是在服务层实现的。</font>**  

## 1.1. 存储引擎操作  
&emsp; ......
<!-- 
https://mp.weixin.qq.com/s/MCFHNOQnTtJ6MGVjM3DP4A 
https://mp.weixin.qq.com/s/IsZjLI7QAB6t7H7NyGscGg
https://mp.weixin.qq.com/s/StjX9bi-YDANrMX21Pn_Uw
-->

## 1.2. InnoDB存储引擎  
&emsp; **参考：**  

* 《MySQL技术内幕：InnoDB存储引擎》  
* https://dev.mysql.com/doc/refman/5.7/en/  

&emsp; MySQL5.5及之后版本默认的存储引擎：InnoDB。  

&emsp; **<font color = "red">InnoDB的特性：</font>**    

* [支持事务](/docs/SQL/transaction.md)  
* [支持行锁](/docs/SQL/lock.md)，采用[MVCC](/docs/SQL/MVCC.md)来支持高并发  
* 支持外键  
* 支持崩溃后的安全恢复  
* 不支持全文索引  
* InnoDB 不保存表的具体行数，执行select count(*) from table时需要全表扫描。  

<!-- 
支持事务操作，具有事务 ACID 隔离特性，默认的隔离级别是可重复读(repetable-read)、通过MVCC(并发版本控制)来实现的。能够解决脏读和不可重复读的问题。
InnoDB 支持外键操作。
InnoDB 默认的锁粒度行级锁，并发性能比较好，会发生死锁的情况。
和 MyISAM 一样的是，InnoDB 存储引擎也有 .frm文件存储表结构 定义，但是不同的是，InnoDB 的表数据与索引数据是存储在一起的，都位于 B+ 数的叶子节点上，而 MyISAM 的表数据和索引数据是分开的。
InnoDB 有安全的日志文件，这个日志文件用于恢复因数据库崩溃或其他情况导致的数据丢失问题，保证数据的一致性。
InnoDB 和 MyISAM 支持的索引类型相同，但具体实现因为文件结构的不同有很大差异。
增删改查性能方面，果执行大量的增删改操作，推荐使用 InnoDB 存储引擎，它在删除操作时是对行删除，不会重建表。

InnoDB引擎有几个重点特性，为其带来了更好的性能和可靠性：

插入缓冲(Insert Buffer)
两次写(Double Write)
自适应哈希索引(Adaptive Hash Index)
异步IO(Async IO)
刷新邻接页(Flush Neighbor Page)
-->

&emsp; [InnoDB体系结构](/docs/SQL/InnoDB.md)  

## 1.3. MyISAM存储引擎  
&emsp; MyISAM上的索引结构，查看[索引](/docs/SQL/7.index.md)  

&emsp; MyISAM引擎是MySQL 5.1及之前版本的默认引擎，它的特性是：  

* 不支持事务  
* 不支持行锁，读取时对需要读到的所有表加锁，写入时则对表加排它锁  
* 不支持外键  
* 不支持崩溃后的安全恢复  
* 在表有读取查询的同时，支持往表中插入新纪录  
* 支持BLOB和TEXT的前500个字符索引，支持全文索引  
* 支持延迟更新索引，极大提升写入性能  
* 对于不会进行修改的表，支持压缩表，极大减少磁盘空间占用  
* MyISAM 用一个变量保存了整个表的行数，执行`select count(*) from tale`时只需要读出该变量即可，速度很快；  

&emsp; <font color = "red">一张表，里面有ID自增主键，当insert了17条记录之后，删除了第15,16,17条记录，再把Mysql重启，再insert一条记录，这条记录的ID是18还是15 ？</font>  
&emsp; 如果表的类型是MyISAM，那么是18。因为MyISAM表会把自增主键的最大ID记录到数据文件中，重启MySQL自增主键的最大ID也不会丢失；  
&emsp; 如果表的类型是InnoDB，那么是15。因为InnoDB 表只是把自增主键的最大ID记录到内存中，所以重启数据库或对表进行OPTION操作，都会导致最大ID丢失。  

&emsp; <font color = "red">哪个存储引擎执行select count(*) 更快，为什么? </font>   
&emsp; MyISAM更快，因为MyISAM内部维护了一个计数器，可以直接调取。  

* 在 MyISAM 存储引擎中，把表的总行数存储在磁盘上，当执行 select count(*) from t 时，直接返回总数据。  
* 在 InnoDB 存储引擎中，跟 MyISAM 不一样，没有将总行数存储在磁盘上，当执行 select count(*) from t 时，会先把数据读出来，一行一行的累加，最后返回总数量。  

&emsp; InnoDB 中 count(*) 语句是在执行的时候，全表扫描统计总数量，所以当数据越来越大时，语句就越来越耗时了，为什么 InnoDB 引擎不像 MyISAM 引擎一样，将总行数存储到磁盘上？这跟 InnoDB 的事务特性有关，由于多版本并发控制(MVCC)的原因，InnoDB 表“应该返回多少行”也是不确定的。  

## 1.4. ~~InnoDB与MyISAM的区别总结~~  
<!-- 
https://mp.weixin.qq.com/s/StjX9bi-YDANrMX21Pn_Uw
-->
1. InnoDB支持事务，MyISAM不支持，对于InnoDB每一条SQL语言都默认封装成事务，自动提交，这样会影响速度，所以最好把多条SQL语言放在begin和commit之间，组成一个事务；  
2. InnoDB支持外键，而MyISAM不支持。对一个包含外键的InnoDB表转为MYISAM会失败；  
3. InnoDB是聚集索引，数据文件是和索引绑在一起的，必须要有主键，通过主键索引效率很高。但是辅助索引需要两次查询，先查询到主键，然后再通过主键查询到数据。因此，主键不应该过大，因为主键太大，其他索引也都会很大。而MyISAM是非聚集索引，数据文件是分离的，索引保存的是数据文件的指针。主键索引和辅助索引是独立的。  
4. InnoDB不保存表的具体行数，执行select count(*) from table时需要全表扫描。而MyISAM用一个变量保存了整个表的行数，执行上述语句时只需要读出该变量即可，速度很快；  
5. Innodb不支持全文索引，而MyISAM支持全文索引，查询效率上MyISAM要高；  

## 1.5. 选择合适的存储引擎  

* 如果对数据一致性要求比较高，需要事务支持，可以选择 InnoDB。比如OA自动化办公系统。  
* 如果数据查询多更新少，对查询性能要求比较高，可以选择 MyISAM。比如博客系统、新闻门户网站。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-50.png)  

<!-- 
选择合适的存储引擎

在实际开发过程中，我们往往会根据应用特点选择合适的存储引擎。

    MyISAM：如果应用程序通常以检索为主，只有少量的插入、更新和删除操作，并且对事物的完整性、并发程度不是很高的话，通常建议选择 MyISAM 存储引擎。
    InnoDB：如果使用到外键、需要并发程度较高，数据一致性要求较高，那么通常选择 InnoDB 引擎，一般互联网大厂对并发和数据完整性要求较高，所以一般都使用 InnoDB 存储引擎。
    MEMORY：MEMORY 存储引擎将所有数据保存在内存中，在需要快速定位下能够提供及其迅速的访问。MEMORY 通常用于更新不太频繁的小表，用于快速访问取得结果。
    MERGE：MERGE 的内部是使用 MyISAM 表，MERGE 表的优点在于可以突破对单个 MyISAM 表大小的限制，并且通过将不同的表分布在多个磁盘上， 可以有效地改善 MERGE 表的访问效率。
-->
