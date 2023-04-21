

<!-- TOC -->

- [1. MVCC](#1-mvcc)
    - [1.1. 什么是当前读和快照读？](#11-什么是当前读和快照读)
    - [1.2. ~~MVCC定义~~](#12-mvcc定义)
    - [1.3. MVCC的实现](#13-mvcc的实现)
        - [1.3.1. 版本链的生成](#131-版本链的生成)
        - [1.3.2. Read View，一致读快照](#132-read-view一致读快照)
        - [1.3.3. 不同隔离级别下的MVCC](#133-不同隔离级别下的mvcc)
            - [1.3.3.1. READ UNCOMMITTED](#1331-read-uncommitted)
            - [1.3.3.2. READ COMMITTED，读取已提交](#1332-read-committed读取已提交)
            - [1.3.3.3. REPEATABLE READ，可重复读](#1333-repeatable-read可重复读)
            - [1.3.3.4. SERIALIZABLE](#1334-serializable)
            - [1.3.3.5. 总结](#1335-总结)
        - [1.3.4. ~~MVCC解决了幻读了没有？~~](#134-mvcc解决了幻读了没有)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "clime">多版本并发控制（MVCC）是一种用来解决读-写冲突的无锁并发控制。</font>**  
&emsp; <font color = "clime">MVCC与锁：MVCC主要解决读写问题，锁解决写写问题。两者结合才能更好的控制数据库隔离性，保证事务正确提交。</font>  
2. **<font color = "clime">InnoDB有两个非常重要的模块来实现MVCC。一个是undo log，用于记录数据的变化轨迹（版本链），用于数据回滚；另外一个是Read View，用于判断一个session对哪些数据可见，哪些不可见。</font>**   
    * 版本链的生成：在数据库中的每一条记录实际都会存在三个隐藏列：事务ID、行ID、回滚指针，指向undo log记录。  
    *  **<font color = "red">Read View是用来判断每一个读取语句有资格读取版本链中的哪个记录。所以在读取之前，都会生成一个Read View。然后根据生成的Read View再去读取记录。</font>**  
3. ~~Read View判断：~~  
&emsp; 如果被访问版本的trx_id小于ReadView中的up_limit_id值，表明生成该版本的事务在当前事务生成ReadView前已经提交，所以该版本可以被当前事务访问。  
&emsp; <font color = "red">如果被访问版本的trx_id属性值在ReadView的up_limit_id和low_limit_id之间，那就需要判断一下trx_id属性值是不是在trx_ids列表中。</font>如果在，说明创建ReadView时生成该版本的事务还是活跃的，该版本不可以被访问；<font color = "clime">如果不在，说明创建ReadView时生成该版本的事务已经被提交，该版本可以被访问。</font>  
4. 在读取已提交、可重复读两种隔离级别下会使用MVCC。  
    * 读取已提交READ COMMITTED是在`每次执行select操作时`都会生成一次Read View。所以解决不了幻读问题。 
    * 可重复读REPEATABLE READ只有在第一次执行select操作时才会生成Read View，后续的select操作都将使用第一次生成的Read View。
5. MVCC解决了幻读没有？  
        当前读：select...lock in share mode; select...for update;
        当前读：update、insert、delete
&emsp; 对于当前读的幻读，MVCC是无法解决的。需要使用 Gap Lock 或 Next-Key Lock（Gap Lock + Record Lock）来解决。</font>其实原理也很简单，用上面的例子稍微修改下以触发当前读：select * from user where id < 10 for update。`若只有MVCC，当事务1执行第二次查询时，操作的数据集已经发生变化，所以结果也会错误；`当使用了Gap Lock时，Gap锁会锁住id < 10的整个范围，因此其他事务无法插入id < 10的数据，从而防止了幻读。  


# 1. MVCC
<!--
三分钟图解 MVCC
https://mp.weixin.qq.com/s/TZhgLRKQsppaRMOWIewYOg

https://segmentfault.com/a/1190000037557620
https://www.jianshu.com/p/f692d4f8a53e

MVCC原理详解 
https://mp.weixin.qq.com/s/7kBh6vH8X8a9PwTVUp0yfg

MVCC原理详解 
https://mp.weixin.qq.com/s/7kBh6vH8X8a9PwTVUp0yfg

https://mp.weixin.qq.com/s/B-2AN3ryX8IJWUoxLRPp_w

MySQL面试三连杀：如何实现可重复读、又为什么会出现幻读、是否解决了幻读问题
https://mp.weixin.qq.com/s/D9hL0W1dcSmzDd2joHgy5A

https://www.codercto.com/a/88775.html
https://zhuanlan.zhihu.com/p/147372839
https://www.jianshu.com/p/8845ddca3b23
https://baijiahao.baidu.com/s?id=1629409989970483292&wfr=spider&for=pc&searchword=mvcc
https://blog.csdn.net/weixin_35554306/article/details/113432616
https://www.cnblogs.com/jmliao/p/13204946.html
https://baike.baidu.com/item/MVCC/6298019?ivk_sa=1022817p


MVCC机制
https://mp.weixin.qq.com/s/hrbnpPqNNJszObizYq0Q1w
MySQL事务与MVCC如何实现的隔离级别 
https://mp.weixin.qq.com/s/CZHuGT4sKs_QHD_bv3BfAQ
https://blog.csdn.net/SnailMann/article/details/94724197
 关于MVCC
 https://mp.weixin.qq.com/s/VJlBQTg_MHWXnw_z0i8F1A

 MVCC机制了解吗 
 https://mp.weixin.qq.com/s/hrbnpPqNNJszObizYq0Q1w
-->

<!-- 
~~
MVCC是如何实现的？ 
https://mp.weixin.qq.com/s/JJd0SoZ--JKW0LEtkZONUA
-->
<!-- 
MCVV这种读取历史数据的方式称为快照读(snapshot read)，而读取数据库当前版本数据的方式，叫当前读(current read)。
-->

## 1.1. 什么是当前读和快照读？  
![image](http://182.92.69.8:8081/img/SQL/sql-145.png)  
&emsp; 在学习MVCC多版本并发控制之前，必须先了解一下，什么是MySQL InnoDB下的当前读和快照读?  
* 当前读  
&emsp; 像select lock in share mode(共享锁)，select for update；update，insert，delete(排他锁)这些操作都是一种当前读，为什么叫当前读？就是它读取的是记录的最新版本，读取时还要保证其他并发事务不能修改当前记录，会对读取的记录进行加锁。  
* 快照读  
&emsp; 像不加锁的select操作就是快照读，即不加锁的非阻塞读；快照读的前提是隔离级别不是串行级别，串行级别下的快照读会退化成当前读；之所以出现快照读的情况，是基于提高并发性能的考虑，快照读的实现是基于多版本并发控制，即MVCC，可以认为MVCC是行锁的一个变种，但它在很多情况下，避免了加锁操作，降低了开销；既然是基于多版本，即快照读可能读到的并不一定是数据的最新版本，而有可能是之前的历史版本。

<!-- 
&emsp; 谈到幻读，首先要引入“当前读”和“快照读”的概念：  
* 快照读：生成一个事务快照(ReadView)，之后都从这个快照获取数据。普通 select 语句就是快照读。  
* 当前读：读取数据的最新版本。常见的 update/insert/delete、还有 select ... for update、select ... lock in share mode 都是当前读。  

&emsp; 当前读，快照读和MVCC的关系？  
&emsp; 准确的说，MVCC多版本并发控制指的是 “维持一个数据的多个版本，使得读写操作没有冲突” 这么一个概念。仅仅是一个理想概念。  
&emsp; 而在MySQL中，实现这么一个MVCC理想概念，我们就需要MySQL提供具体的功能去实现它，而快照读就是MySQL为我们实现MVCC理想模型的其中一个具体非阻塞读功能。而相对而言，当前读就是悲观锁的具体功能实现。  
&emsp; 要说的再细致一些，快照读本身也是一个抽象概念，再深入研究。MVCC模型在MySQL中的具体实现则是由 3个隐式字段，undo日志 ，Read View 等去完成的，具体可以看下面的MVCC实现原理。
-->

## 1.2. ~~MVCC定义~~
&emsp; Multi-Version Concurrency Control，多版本并发控制。<font color = "red">MVCC是一种并发控制的方法，一般在数据库管理系统中，实现对数据库的并发访问。MVCC是无锁操作的一种实现方式。</font>  

&emsp; 数据库并发场景有三种，分别为：

    读-读：不存在任何问题，也不需要并发控制
    读-写：有线程安全问题，可能会造成事务隔离性问题，可能遇到脏读，幻读，不可重复读
    写-写：有线程安全问题，可能会存在更新丢失问题，比如第一类更新丢失，第二类更新丢失

&emsp; MVCC带来的好处是？  
&emsp; **<font color = "clime">多版本并发控制(MVCC)是一种用来解决读-写冲突的无锁并发控制，</font>** 也就是为事务分配单向增长的时间戳，为每个修改保存一个版本，版本与事务时间戳关联，读操作只读该事务开始前的数据库的快照。 所以MVCC可以为数据库解决以下问题：  
1. 在并发读写数据库时，可以做到在读操作时不用阻塞写操作，写操作也不用阻塞读操作，提高了数据库并发读写的性能。
2. 同时还可以解决脏读，幻读，不可重复读等事务隔离问题，但不能解决更新丢失问题。

&emsp; <font color = "clime">MVCC与锁：MVCC主要解决读写问题，锁解决写写问题。两者结合才能更好的控制数据库隔离性，保证事务正确提交。</font>  

&emsp; <font color = "red">MVCC就是为了实现读-写冲突不加锁，而这个读指的就是快照读，而非当前读，当前读实际上是一种加锁的操作，是悲观锁的实现。</font>  

<!-- 
![image](http://182.92.69.8:8081/img/SQL/sql-77.png)  

&emsp; 回顾并发存在哪些可能冲突的情况：  

1. 读-读(没任何问题)  
2. 读-写(隔离性、读未提交、幻读)  
3. 写-写(数据丢失)  
-->

## 1.3. MVCC的实现
&emsp; **<font color = "clime">InnoDB有两个非常重要的模块来实现MVCC，一个是undo log，用于记录数据的变化轨迹(版本链)，用于数据回滚；另外一个是Read View，用于判断一个session对哪些数据可见，哪些不可见。</font>**   

### 1.3.1. 版本链的生成  
&emsp; 在数据库中的每一条记录实际都会存在三个隐藏列：  
<!-- 
trx_id：用来标识最近一次对本行记录做修改(insert|update)的事务的标识符, 即最后一次修改(insert|update)本行记录的事务id。  
roll_pointer：每次有修改的时候，都会把老版本写入undo日志中。这个roll_pointer就是存了一个指针，它指向这条聚簇索引记录的上一个版本的位置，通过它来获得上一个版本的记录信息。(注意插入操作的undo日志没有这个属性，因为它没有老版本)。  
-->

* DB_TRX_ID：该列表示此记录的事务ID。  
* DB_ROW_ID：行ID，如果有指定主键，那么该值就是主键。如果没有主键，那么就会使用定义的第一个唯一索引。如果没有唯一索引，那么就会默认生成一个值。  
* DB_ROLL_PTR：回滚指针，指向undo log记录。每次对某条记录进行改动时，该列会存一个指针，可以通过这个指针找到该记录修改前的信息 。<font color = "clime">当某条记录被多次修改时，该行记录会存在多个版本，通过回滚指针DB_ROLL_PTR链接形成一个类似版本链的概念。</font>    

&emsp; 执行sql：  

    start transaction;
    update person set age = 22 where id = 1;
    update person set name = 'out' where id = 1;
    commit;

&emsp; 当执行完上面两条语句之后，但是还没有提交事务之前，它的版本链如下图所示：  
![image](http://182.92.69.8:8081/img/SQL/sql-74.png)  

### 1.3.2. Read View，一致读快照  
<!-- 
https://mp.weixin.qq.com/s?__biz=MzU0MDEwMjgwNA==&mid=2247489840&idx=1&sn=77800cbea0eeaa61e1bd8334c8ad42c4&chksm=fb3f00cbcc4889dd6e8aed60812a7cd7d0c9e7fc6ec5a2aec2803ba05b26de34cd79ff109f06&scene=21#wechat_redirect
-->
<!-- 
~~
面试官灵魂的一击：你懂MySQL事务吗？ 
https://mp.weixin.qq.com/s/N5nK7q0vUD9Ouqdi5EYdSw
-->
&emsp; **<font color = "red">Read View是用来判断每一个读取语句有资格读取版本链中的哪个记录。所以在读取之前，都会生成一个Read View。然后根据生成的Read View再去读取记录。</font>**  
    
    在事务中，只有执行插入、更新、删除操作时才会分配到一个事务id。如果事务只是一个单纯的读取事务，那么它的事务 id 就是默认的 0。

&emsp; Read View的结构如下：  
![image](http://182.92.69.8:8081/img/SQL/sql-75.png)  
* rw_trx_ids：表示在生成 Read View 时，<font color = "red">当前活跃的读写事务数组。</font>
* up_limit_id：表示在生成 Read View 时，当前已提交的事务号 + 1，也就是在 rw_trx_ids 中的最小事务号。<font color = "clime">trx_id 小于该值都能看到。</font>
* low_limit_id：表示在生成 Read View 时，当前已分配的事务号 + 1，也就是将要分配给下一个事务的事务号。<font color = "clime">trx_id 大于等于该值都不能看到。</font>
* curr_trx_id：创建 Read View 的当前事务id。

&emsp; <font color = "red">有了ReadView，在访问某条记录时，MySQL会根据以下规则来判断版本链中的哪个版本(记录)是在本次事务中可见的：</font>  

![image](http://182.92.69.8:8081/img/SQL/sql-85.png)  
![image](http://182.92.69.8:8081/img/SQL/sql-86.png)  
![image](http://182.92.69.8:8081/img/SQL/sql-154.png)  

1. 如果被访问版本的trx_id与ReadView中的creator_trx_id值相同，意味着当前事务在访问它自己修改过的记录，所以该版本可以被当前事务访问。  
2. 如果被访问版本的trx_id小于ReadView中的up_limit_id值，表明生成该版本的事务在当前事务生成ReadView前已经提交，所以该版本可以被当前事务访问。  
3. 如果被访问版本的trx_id大于ReadView中的low_limit_id值，表明生成该版本的事务在当前事务生成ReadView后才开启，所以该版本不可以被当前事务访问。  
4. <font color = "red">如果被访问版本的trx_id属性值在ReadView的up_limit_id和low_limit_id之间，那就需要判断一下trx_id属性值是不是在trx_ids列表中。</font>如果在，说明创建ReadView时生成该版本的事务还是活跃的，该版本不可以被访问；<font color = "clime">如果不在，说明创建ReadView时生成该版本的事务已经被提交，该版本可以被访问。</font>  

&emsp; 在进行判断时，首先会拿记录的最新版本来比较， **<font color = "clime">如果该版本无法被当前事务看到，则通过记录的DB_ROLL_PTR找到上一个版本，重新进行比较，直到找到一个能被当前事务看到的版本。</font>**   
&emsp; 而对于删除，其实就是一种特殊的更新，InnoDB用一个额外的标记位delete_bit标识是否删除。在进行判断时，会检查下delete_bit是否被标记，如果是，则跳过该版本，通过 DB_ROLL_PTR拿到下一个版本进行判断。

<!-- 
&emsp; Read View遵循一个可见性算法，主要是将要被修改的数据的最新记录中的DB_TRX_ID(即当前事务ID)取出来，与系统当前其他活跃事务的ID去对比(由Read View维护)，如果DB_TRX_ID跟Read View的属性做了某些比较，不符合可见性，那就通过DB_ROLL_PTR回滚指针去取出Undo Log中的DB_TRX_ID再比较，即遍历链表的DB_TRX_ID(从链首到链尾，即从最近的一次修改查起)，直到找到满足特定条件的DB_TRX_ID, 那么这个DB_TRX_ID所在的旧记录就是当前事务能看见的最新老版本。 

1. MySQL事务开始的时候，会根据当前活跃的事务构造出一个事务列表(Read View)。
2. 当读取一行记录时会根据行记录上的TRX_ID与Read View中的最大TRX_ID和最小TRX_ID比较来判断是否可见。
3. 首先会比较TRX_ID是否小于Read View列表中最小的TRX_ID，如果小于，则说明此事务早于Read View中的所有事务结束，则可以直接返回。
4. 如果TRX_ID大于Read View列表中最小的TRX_ID，则判断TRX_ID是否大于Read View列表中最大的TRX_ID，如果是，则根据行上的回滚指针找到回滚段中的对应undo log记录取出TRX_ID赋值给当前的TRX_ID重新进行比较(递归)。
5. 如果TRX_ID在Read View列表中最小TRX_ID和最大TRX_ID之间，判断TRX_ID是否在Read View中，如果在，则根据行上的回滚指针找到回滚段中的对应undo log记录返回，否则直接返回。
-->
<!--
![image](http://182.92.69.8:8081/img/SQL/sql-76.png)  
当执行查询sql时会生成一致性视图read-view，它由执行查询时所有未提交事务id数组(数组里最小的id为min_id)和已创建的最大事务id(max_id)组成，查询的数据结果需要跟read-view做比对从而得到快走结果。  

版本链比对规则：  
1.如果落在绿色部分(trx_id< min_id)，表示这个版本是已提交的事务生成的，这个数据是可见的；  
2.如果落在红色部分(trx_id>max_id)，表示这个版本是由将来启动的事务生成的，是肯定不可见的；  
3.如果落在黄色部分(min_id<=trx_id<=max_id)，那就包括两种情况  
  a. 若row的trx_id在数组中，表示这个版本是由还没提交的事务生成的，可不见，当前自己的事务是可见的。  
  b.若row的trx_id不在数组中，表示这个版本是已经提交了的事务生成的，可见。  

对于删除的情况可以认为是update的特色情况，会将版本链上最新的数据复制一份，然后将trx_id修改成删除操作的trx_id，同时在该条记录的头信息(record header)里的(deleted_flag)标记位写上true，来表示当前记录已经被删除，在查询时按照上面的规则查到对应的记录，如果delete_flag标记为true，意味着记录已经被删除，则不返回数据。   
-->
---
![image](http://182.92.69.8:8081/img/SQL/sql-146.png)  
&emsp; 执行过程如下：  
1. 如果被访问版本的trx_id=creator_id，意味着当前事务在访问它自己修改过的记录，所以该版本可以被当前事务访问。  
2. 如果被访问版本的trx_id\<min_trx_id，表明生成该版本的事务在当前事务生成ReadView前已经提交，所以该版本可以被当前事务访问。
3. 被访问版本的trx_id>=max_trx_id，表明生成该版本的事务在当前事务生成ReadView后才开启，该版本不可以被当前事务访问。  
4. 被访问版本的trx_id是否在m_ids列表中。
    1. 是，创建ReadView时，该版本还是活跃的，该版本不可以被访问。顺着版本链找下一个版本的数据，继续执行上面的步骤判断可见性，如果最后一个版本还不可见，意味着记录对当前事务完全不可见。  
    2. 否，创建ReadView时，生成该版本的事务已经被提交，该版本可以被访问。  

----

&emsp; 如下，它是一段MySQL判断可见性的一段源码  
![image](http://182.92.69.8:8081/img/SQL/sql-97.png)  

&emsp; 查看数据库中正在执行的事务：  

```sql
mysql> set autocommit = 0; --设置手动提交事务
Query OK, 0 rows affected (0.00 sec)

mysql> insert into user(username, password) values ('a', 'a'); --插入一条数据

--information_schema.innodb_trx表中可以查到正在执行的事务信息，trx_id为事务id
mysql> select * from information_schema.innodb_trx\G
*************************** 1. row ***************************
                    trx_id: 1288
                 trx_state: RUNNING
               trx_started: 2018-01-13 17:04:44
     trx_requested_lock_id: NULL
          trx_wait_started: NULL
                trx_weight: 2
       trx_mysql_thread_id: 3
                 trx_query: select * from information_schema.innodb_trx
       trx_operation_state: NULL
         trx_tables_in_use: 0
         trx_tables_locked: 1
          trx_lock_structs: 1
     trx_lock_memory_bytes: 1136
           trx_rows_locked: 0
         trx_rows_modified: 1
   trx_concurrency_tickets: 0
       trx_isolation_level: REPEATABLE READ
         trx_unique_checks: 1
    trx_foreign_key_checks: 1
trx_last_foreign_key_error: NULL
 trx_adaptive_hash_latched: 0
 trx_adaptive_hash_timeout: 0
          trx_is_read_only: 0
trx_autocommit_non_locking: 0
1 row in set (0.00 sec)

mysql> commit; --提交

--再次执行已经没有活跃事务
mysql> select * from information_schema.innodb_trx\G
Empty set (0.00 sec)
```

### 1.3.3. 不同隔离级别下的MVCC  
#### 1.3.3.1. READ UNCOMMITTED
&emsp; 该隔离级别不会使用 MVCC。它只要执行 select，那么就会获取 B+ 树上最新的记录。而不管该记录的事务是否已经提交。  

#### 1.3.3.2. READ COMMITTED，读取已提交  
&emsp; 在READ COMMITTED隔离级别下，会使用 MVCC。在开启一个读取事务之后，它会在每一个 select 操作之前都生成一个Read View。  

#### 1.3.3.3. REPEATABLE READ，可重复读  
&emsp; 实际上，REPEATABLE READ 与 READ COMMITTED 的区别只有在生成 Read View 的时机上。  
&emsp; READ COMMITTED(读已提交) 是在每次执行 select 操作时，都会生成一个新的 Read View。而 REPEATABLE READ (可重复读)只会在第一次执行 select 操作时生成一个 Read View，直到该事务提交之前，所有的 select 操作都是使用第一次生成的 Read View。  

<!-- 
那经常有人说 Repeatable Read 解决了幻读是什么情况？
SQL 标准中规定的 RR 并不能消除幻读，但是 MySQL 的 RR 可以，靠的就是 Gap 锁。在 RR 级别下，Gap 锁是默认开启的，而在 RC 级别下，Gap 锁是关闭的。

MVCC在MySQL InnoDB中的实现主要是为了提高数据库并发性能，用更好的方式去处理读-写冲突，做到即使有读写冲突时，也能做到不加锁，非阻塞并发读
-->

#### 1.3.3.4. SERIALIZABLE
&emsp; 该隔离级别不会使用 MVCC。如果使用的是普通的 select 语句，它会在该语句后面加上 lock in share mode，变为一致性锁定读。假设一个事务读取一条记录，其他事务对该记录的更改都会被阻塞。假设一个事务在更改一条记录，其他事务对该记录的读取都会被阻塞。  
&emsp; 在该隔离级别下，读写操作变为了串行操作。  

#### 1.3.3.5. 总结
&emsp; 在 READ COMMITTED 和 REPEATABLE READ 隔离等级之下才会使用 MVCC。  
&emsp; 但是 READ COMMITTED 和 REPEATABLE READ 使用MVCC的方式各不相同：  

* 读取已提交READ COMMITTED 是在每次执行 select 操作时都会生成一次 Read View。
* 可重复读REPEATABLE READ 只有在第一次执行 select 操作时才会生成 Read View，后续的 select 操作都将使用第一次生成的 Read View。

&emsp; 而 READ UNCOMMITTED 和 SERIALIZABLE 隔离级别不会使用 MVCC。它们的读取操作也不相同：  

* READ UNCOMMITTED 每次执行 select 都会去读最新的记录。  
* SERIALIZABLE 每次执行 select 操作都会在该语句后面加上 lock in share mode，使 select 变为一致性锁定读，将读写进行串行化。  

### 1.3.4. ~~MVCC解决了幻读了没有？~~  
<!-- 
https://blog.csdn.net/MarkusZhang/article/details/107335259
-->

&emsp; 回答这个问题前，先要了解下什么是快照读、什么是当前读。  

    当前读:select...lock in share mode; select...for update;
    当前读:update、insert、delete
    快照读:不加锁的非阻塞读，select

&emsp; 幻读：在一个事务中使用相同的 SQL 两次读取，第二次读取到了其他事务新插入的行，则称为发生了幻读。  
&emsp; 例如：  
1. 事务1第一次查询：select * from user where id < 10 时查到了 id = 1 的数据；  
2. 事务2插入了 id = 2 的数据；  
3. 事务1使用同样的语句第二次查询时，查到了 id = 1、id = 2 的数据，出现了幻读。  

&emsp; MVCC解决了快照读的幻读：  
&emsp; <font color = "clime">对于快照读，MVCC 因为从 ReadView读取，所以必然不会看到新插入的行，所以天然就解决了幻读的问题。</font>  
&emsp; <font color = "clime">而对于当前读的幻读，MVCC是无法解决的。需要使用 Gap Lock 或 Next-Key Lock(Gap Lock + Record Lock)来解决。</font>其实原理也很简单，用上面的例子稍微修改下以触发当前读：select * from user where id < 10 for update。`若只有MVCC，当事务1执行第二次查询时，操作的数据集已经发生变化，所以结果也会错误；`当使用了 Gap Lock 时，Gap 锁会锁住 id < 10 的整个范围，因此其他事务无法插入 id < 10 的数据，从而防止了幻读。  

<!-- 

1. 每种存储引擎对MVCC的实现方式不同。<font color = "clime">InnoDB 的 MVCC，是通过在每行记录后面保存两个隐藏的列来实现。</font>这两个列，一个保存了行的创建时间，一个保存行的过期时间(删除时间)。当然存储的并不是真实的时间，而是系统版本号(system version number)。每开始一个新的事务，系统版本号都会自动递增。事务开始时刻的系统版本号会作为事务的版本号，用来和查询到的每行记录的版本号进行比较。  
2. InnoDB有两个非常重要的模块来实现MVCC，一个是undo日志，用于记录数据的变化轨迹，用于数据回滚，另外一个是Read View，用于判断一个session对哪些数据可见，哪些不可见。  
&emsp; Read View：它用于控制数据的可见性。Read View是事务开启时，当前所有事务的一个集合，这个数据结构中存储了当前Read View中最大的ID及最小的ID。    
&emsp; 在InnoDB中，只有读查询才会去构建ReadView视图，对于类似DML这样的数据更改，无需判断可见性，而是单纯的发现事务锁冲突，直接堵塞操作。  
3. **<font color = "red">MVCC 只在 COMMITTED READ(读提交)和REPEATABLE READ(可重复读)两种隔离级别下工作。</font>**  

    &emsp; <font color = "red">REPEATABLE READ(可重读)隔离级别下MVCC如何工作：</font>  

    * SELECT  
    &emsp; InnoDB会根据以下两个条件检查每行记录(只有符合这两个条件的才会被查询出来)：  
        * InnoDB只查找版本早于当前事务版本的数据行，这样可以确保事务读取的行，要么是在开始事务之前已经存在要么是事务自身插入或者修改过的  
        * 行的删除版本号要么未定义，要么大于当前事务版本号，这样可以确保事务读取到的行在事务开始之前未被删除  
    * INSERT：InnoDB为新插入的每一行保存当前系统版本号作为行版本号  
    * DELETE：InnoDB为删除的每一行保存当前系统版本号作为行删除标识  
    * UPDATE：InnoDB为插入的一行新纪录保存当前系统版本号作为行版本号，同时保存当前系统版本号到原来的行作为删除标识  

    &emsp; 保存这两个额外系统版本号，使大多数操作都不用加锁。使数据操作简单，性能很好，并且也能保证只会读取到符合要求的行。不足之处是每行记录都需要额外的存储空间，需要做更多的行检查工作和一些额外的维护工作。  

    -->
    