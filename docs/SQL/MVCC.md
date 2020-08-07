

<!-- TOC -->

- [1. MVCC](#1-mvcc)
    - [1.1. MVCC定义](#11-mvcc定义)
    - [1.2. MVCC的实现](#12-mvcc的实现)
    - [1.3. 版本链的生成](#13-版本链的生成)
    - [1.4. Read View](#14-read-view)
    - [1.5. 不同隔离级别下的MVCC](#15-不同隔离级别下的mvcc)
        - [1.5.1. READ UNCOMMITTED](#151-read-uncommitted)
        - [1.5.2. READ COMMITTED](#152-read-committed)
        - [1.5.3. REPEATABLE READ，可重复读](#153-repeatable-read可重复读)
        - [1.5.4. SERIALIZABLE](#154-serializable)
        - [1.5.5. 总结](#155-总结)
    - [1.6. MVCC与锁](#16-mvcc与锁)

<!-- /TOC -->

<!-- 

https://blog.csdn.net/SnailMann/article/details/94724197
https://blog.csdn.net/heroqiang/article/details/79024333
-->

# 1. MVCC
&emsp; **<font color = "lime">一句话概述：MVCC使用无锁并发控制，解决数据库读写问题。数据库会根据事务ID，形成版本链；MVCC会根据Read View来决定读取版本链中的哪条记录。</font>**

## 1.1. MVCC定义
&emsp; Multi-Version Concurrency Control，多版本并发控制。<font color = "red">MVCC 是一种并发控制的方法，一般在数据库管理系统中，实现对数据库的并发访问。MVCC是无锁操作的一种实现方式。</font>  

## 1.2. MVCC的实现
&emsp; InnoDB有两个非常重要的模块来实现MVCC，<font color = "lime">一个是undo log，用于记录数据的变化轨迹，用于数据回滚，另外一个是Read View，用于判断一个session对哪些数据可见，哪些不可见。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-72.png)  

&emsp; MVCC的基本原理如下：  
1. 每行数据都存在一个版本，每次数据更新时都更新该版本。  
2. 修改时Copy出当前版本随意修改，各个事务之间无干扰。  
3. 保存时比较版本号，如果成功（commit），则覆盖原记录；失败则放弃copy（rollback）。  

&emsp; 使用基于锁的并发控制（Lock-Based Concurrency Control），开销是非常大的 ，而使用MVCC机制来做，能一定程度的代替行锁，可以做到读不加锁，读写不冲突，在读多写少的OLTP应用中，读写不冲突是非常重要的，极大的增加了系统的并发性能。  

## 1.3. 版本链的生成  
&emsp; 在数据库中的每一条记录实际都会存在三个隐藏列：  
<!-- 
trx_id：用来标识最近一次对本行记录做修改(insert|update)的事务的标识符, 即最后一次修改(insert|update)本行记录的事务id。  
roll_pointer：每次有修改的时候，都会把老版本写入undo日志中。这个roll_pointer就是存了一个指针，它指向这条聚簇索引记录的上一个版本的位置，通过它来获得上一个版本的记录信息。(注意插入操作的undo日志没有这个属性，因为它没有老版本)。  
-->

* DB_TRX_ID：该列表示此记录的事务 ID。  
* DB_ROLL_PTR：该列表示一个<font color = "red">指向回滚段的指针，实际就是指向该记录的一个版本链。</font>  
* DB_ROW_ID：记录的 ID，如果有指定主键，那么该值就是主键。如果没有主键，那么就会使用定义的第一个唯一索引。如果没有唯一索引，那么就会默认生成一个值。  

&emsp; 执行sql：  

    start transaction;
    update person set age = 22 where id = 1;
    update person set name = 'out' where id = 1;
    commit;

&emsp; 当执行完上面两条语句之后，但是还没有提交事务之前，它的版本链如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-74.png)  

## 1.4. Read View  
&emsp; Read View 是用来判断每一个读取语句有资格读取版本链中的哪个记录。所以在在读取之前，都会生成一个 Read View。然后根据生成的Read View再去读取记录。
    
    在事务中，只有执行插入、更新、删除操作时才会分配到一个事务 id。如果事务只是一个单纯的读取事务，那么它的事务 id 就是默认的 0。

&emsp; Read View的结构如下：  

* rw_trx_ids：表示在生成 Read View 时，<font color = "red">当前活跃的读写事务数组。</font>
* up_limit_id：表示在生成 Read View 时，当前已提交的事务号 + 1，也就是在 rw_trx_ids 中的最小事务号。
* low_limit_id：表示在生成 Read View 时，当前已分配的事务号 + 1，也就是将要分配给下一个事务的事务号。
* curr_trx_id：创建 Read View 的当前事务id。

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-75.png)  

&emsp; <font color = "red">MySQL会根据以下规则来判断版本链中的哪个版本（记录）是在事务中可见的：</font>  

    Read View遵循一个可见性算法，主要是将要被修改的数据的最新记录中的DB_TRX_ID（即当前事务ID）取出来，与系统当前其他活跃事务的ID去对比（由Read View维护），如果DB_TRX_ID跟Read View的属性做了某些比较，不符合可见性，那就通过DB_ROLL_PTR回滚指针去取出Undo Log中的DB_TRX_ID再比较，即遍历链表的DB_TRX_ID（从链首到链尾，即从最近的一次修改查起），直到找到满足特定条件的DB_TRX_ID, 那么这个DB_TRX_ID所在的旧记录就是当前事务能看见的最新老版本。

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-85.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-86.png)  

1. MySQL事务开始的时候，会根据当前活跃的事务构造出一个事务列表（Read View）。
2. 当读取一行记录时会根据行记录上的TRX_ID与Read View中的最大TRX_ID和最小TRX_ID比较来判断是否可见。
3. 首先会比较TRX_ID是否小于Read View列表中最小的TRX_ID，如果小于，则说明此事务早于Read View中的所有事务结束，则可以直接返回。
4. 如果TRX_ID大于Read View列表中最小的TRX_ID，则判断TRX_ID是否大于Read View列表中最大的TRX_ID，如果是，则根据行上的回滚指针找到回滚段中的对应undo log记录取出TRX_ID赋值给当前的TRX_ID重新进行比较（递归）。
5. 如果TRX_ID在Read View列表中最小TRX_ID和最大TRX_ID之间，判断TRX_ID是否在Read View中，如果在，则根据行上的回滚指针找到回滚段中的对应undo log记录返回，否则直接返回。

<!-- 

首先比较DB_TRX_ID < up_limit_id, 如果小于，则当前事务能看到DB_TRX_ID 所在的记录，如果大于等于进入下一个判断
接下来判断 DB_TRX_ID 大于等于 low_limit_id , 如果大于等于则代表DB_TRX_ID 所在的记录在Read View生成后才出现的，那对当前事务肯定不可见，如果小于则进入下一个判断
判断DB_TRX_ID 是否在活跃事务之中，trx_list.contains(DB_TRX_ID)，如果在，则代表我Read View生成时刻，你这个事务还在活跃，还没有Commit，你修改的数据，我当前事务也是看不见的；如果不在，则说明，你这个事务在Read View生成之前就已经Commit了，你修改的结果，我当前事务是能看见的

-->

<!--

* trx_id < min_trx_id，那么该记录则在当前事务可见，因为修改该版本记录的事务在当前事务生成 Read View 之前就已经提交。
* trx_id = curr_trx_id，那么该记录在当前事务可见，因为修改该版本记录的事务就是当前事务。
* trx_id in (rw_trx_ids)，那么该记录在当前事务不可见，因为需改该版本记录的事务在当前事务生成 Read View 之前还未提交。
* trx_id > max_trx_id，那么该记录在当前事务不可见，因为修改该版本记录的事务在当前事务生成 Read View 之前还未开启。
-->

<!--
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-76.png)  
当执行查询sql时会生成一致性视图read-view，它由执行查询时所有未提交事务id数组（数组里最小的id为min_id）和已创建的最大事务id（max_id）组成，查询的数据结果需要跟read-view做比对从而得到快走结果。  

版本链比对规则：  
1.如果落在绿色部分(trx_id< min_id)，表示这个版本是已提交的事务生成的，这个数据是可见的；  
2.如果落在红色部分(trx_id>max_id)，表示这个版本是由将来启动的事务生成的，是肯定不可见的；  
3.如果落在黄色部分(min_id<=trx_id<=max_id)，那就包括两种情况  
  a. 若row的trx_id在数组中，表示这个版本是由还没提交的事务生成的，可不见，当前自己的事务是可见的。  
  b.若row的trx_id不在数组中，表示这个版本是已经提交了的事务生成的，可见。  

对于删除的情况可以认为是update的特色情况，会将版本链上最新的数据复制一份，然后将trx_id修改成删除操作的trx_id，同时在该条记录的头信息(record header)里的(deleted_flag)标记位写上true，来表示当前记录已经被删除，在查询时按照上面的规则查到对应的记录，如果delete_flag标记为true，意味着记录已经被删除，则不返回数据。   
-->

## 1.5. 不同隔离级别下的MVCC  
### 1.5.1. READ UNCOMMITTED
&emsp; 该隔离级别不会使用 MVCC。它只要执行 select，那么就会获取 B+ 树上最新的记录。而不管该记录的事务是否已经提交。  

### 1.5.2. READ COMMITTED  
&emsp; 在READ COMMITTED隔离级别下，会使用 MVCC。在开启一个读取事务之后，它会在每一个 select 操作之前都生成一个Read View。  

### 1.5.3. REPEATABLE READ，可重复读  
&emsp; 实际上，REPEATABLE READ 与 READ COMMITTED 的区别只有在生成 Read View 的时机上。  
&emsp; READ COMMITTED（读已提交） 是在每次执行 select 操作时，都会生成一个新的 Read View。而 REPEATABLE READ （可重复读）只会在第一次执行 select 操作时生成一个 Read View，直到该事务提交之前，所有的 select 操作都是使用第一次生成的 Read View。  

### 1.5.4. SERIALIZABLE
&emsp; 该隔离级别不会使用 MVCC。如果使用的是普通的 select 语句，它会在该语句后面加上 lock in share mode，变为一致性锁定读。假设一个事务读取一条记录，其他事务对该记录的更改都会被阻塞。假设一个事务在更改一条记录，其他事务对该记录的读取都会被阻塞。  
&emsp; 在该隔离级别下，读写操作变为了串行操作。  

### 1.5.5. 总结
&emsp; 在 READ COMMITTED 和 REPEATABLE READ 隔离等级之下才会使用 MVCC。  
&emsp; 但是 READ COMMITTED 和 REPEATABLE READ 使用MVCC的方式各不相同：  

* READ COMMITTED 是在每次执行 select 操作时都会生成一次 Read View。
* REPEATABLE READ 只有在第一次执行 select 操作时才会生成 Read View，后续的 select 操作都将使用第一次生成的 Read View。

&emsp; 而 READ UNCOMMITTED 和 SERIALIZABLE 隔离级别不会使用 MVCC。  
&emsp; 它们的读取操作也不相同：  

* READ UNCOMMITTED 每次执行 select 都会去读最新的记录。  
* SERIALIZABLE 每次执行 select 操作都会在该语句后面加上 lock in share mode，使 select 变为一致性锁定读，将读写进行串行化。  


## 1.6. MVCC与锁  
<!-- 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-77.png)  
-->

&emsp; 回顾并发存在哪些可能冲突的情况：  

1. 读-读（没任何问题）  
2. 读-写（隔离性、读未提交、幻读）  
3. 写-写（数据丢失）  

&emsp; MVCC主要解决读写问题，锁解决写写问题。两者结合才能更好的控制数据库隔离性，保证事务正确提交。  


---
&emsp; 参考：  
https://www.bilibili.com/read/cv6580973  
https://www.jianshu.com/p/cfe3c269ad19  
bilibili视频理解  


<!-- 

1. 每种存储引擎对MVCC的实现方式不同。<font color = "lime">InnoDB 的 MVCC，是通过在每行记录后面保存两个隐藏的列来实现。</font>这两个列，一个保存了行的创建时间，一个保存行的过期时间（删除时间）。当然存储的并不是真实的时间，而是系统版本号（system version number）。每开始一个新的事务，系统版本号都会自动递增。事务开始时刻的系统版本号会作为事务的版本号，用来和查询到的每行记录的版本号进行比较。  
2. InnoDB有两个非常重要的模块来实现MVCC，一个是undo日志，用于记录数据的变化轨迹，用于数据回滚，另外一个是Read View，用于判断一个session对哪些数据可见，哪些不可见。  
&emsp; Read View：它用于控制数据的可见性。Read View是事务开启时，当前所有事务的一个集合，这个数据结构中存储了当前Read View中最大的ID及最小的ID。    
&emsp; 在InnoDB中，只有读查询才会去构建ReadView视图，对于类似DML这样的数据更改，无需判断可见性，而是单纯的发现事务锁冲突，直接堵塞操作。  
3. **<font color = "red">MVCC 只在 COMMITTED READ（读提交）和REPEATABLE READ（可重复读）两种隔离级别下工作。</font>**  

    &emsp; <font color = "red">REPEATABLE READ（可重读）隔离级别下MVCC如何工作：</font>  

    * SELECT  
    &emsp; InnoDB会根据以下两个条件检查每行记录（只有符合这两个条件的才会被查询出来）：  
        * InnoDB只查找版本早于当前事务版本的数据行，这样可以确保事务读取的行，要么是在开始事务之前已经存在要么是事务自身插入或者修改过的  
        * 行的删除版本号要么未定义，要么大于当前事务版本号，这样可以确保事务读取到的行在事务开始之前未被删除  
    * INSERT：InnoDB为新插入的每一行保存当前系统版本号作为行版本号  
    * DELETE：InnoDB为删除的每一行保存当前系统版本号作为行删除标识  
    * UPDATE：InnoDB为插入的一行新纪录保存当前系统版本号作为行版本号，同时保存当前系统版本号到原来的行作为删除标识  

    &emsp; 保存这两个额外系统版本号，使大多数操作都不用加锁。使数据操作简单，性能很好，并且也能保证只会读取到符合要求的行。不足之处是每行记录都需要额外的存储空间，需要做更多的行检查工作和一些额外的维护工作。  

    -->
    