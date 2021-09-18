
<!-- TOC -->

- [1. 两阶段提交和崩溃恢复](#1-两阶段提交和崩溃恢复)
    - [1.1. ~~redo log与binlog的区别~~](#11-redo-log与binlog的区别)
    - [1.2. 两阶段提交](#12-两阶段提交)
    - [1.3. ~~MySQL中更新一条语句的流程~~](#13-mysql中更新一条语句的流程)
    - [1.4. ~~两阶段提交中，MySQL异常重启（crash），是如何保证数据完整性的？~~](#14-两阶段提交中mysql异常重启crash是如何保证数据完整性的)
    - [1.5. 组提交](#15-组提交)
    - [1.6. ~~恢复~~](#16-恢复)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. 两阶段提交
    1. **<font color = "clime">redo log和binlog都可以用于表示事务的提交状态，而两阶段提交就是让这两个状态保持逻辑上的一致。两阶段提交保证解决binlog和redo log的数据一致性。</font>**    
    2. `两阶段提交是很典型的分布式事务场景，因为redolog和binlog两者本身就是两个独立的个体，`要想保持一致，就必须使用分布式事务的解决方案来处理。 **<font color = "blue">而将redolog分成了两步，其实就是使用了两阶段提交协议(Two-phaseCommit，2PC)。</font>**  
    &emsp; 事务的提交过程有两个阶段，就是将redolog的写入拆成了两个步骤：prepare和commit，中间再穿插写入binlog。  
        1. 记录redolog，InnoDB事务进入prepare状态；
        2. 写入binlog；
        3. 将redolog这个事务相关的记录状态设置为commit状态。
2. 崩溃恢复： **<font color = "red">当重启数据库实例的时候，数据库做2个阶段性操作：redo log处理，undo log及binlog 处理。在崩溃恢复中还需要回滚没有提交的事务，提交没有提交成功的事务。由于回滚操作需要undo日志的支持，undo日志的完整性和可靠性需要redo日志来保证，所以崩溃恢复先做redo前滚，然后做undo回滚。</font>**    


# 1. 两阶段提交和崩溃恢复
<!-- 
https://blog.csdn.net/ab1024249403/article/details/110099571

redo log与binlog间
https://mp.weixin.qq.com/s/XPXvx9fE1632uzo1SGn4SA

redo log与binlog间的破事 
https://mp.weixin.qq.com/s/jG9_Iqb4Ig2eUAyIgfeyVQ
5. redo log 为什么可以保证crash safe机制呢？ 
https://mp.weixin.qq.com/s/tmlk9p3UOnxzMwjggCtC2g

-->

## 1.1. ~~redo log与binlog的区别~~
&emsp; <font color = "red">二进制日志的作用之一是还原数据库的，这与redo log很类似，</font>很多人混淆过，但是两者有本质的不同  

* 作用不同：redo log是保证事务的持久性的，是事务层面的，binlog作为还原的功能，是数据库层面的(当然也可以精确到事务层面的)，虽然都有还原的意思，但是其保护数据的层次是不一样的。
* 内容不同：<font color = "clime">redo log是物理日志，是数据页面的修改之后的物理记录，binlog是逻辑日志，可以简单认为记录的就是sql语句。</font>
* 另外，两者日志产生的时间，可以释放的时间，在可释放的情况下清理机制，都是完全不同的。
* 恢复数据时候的效率，基于物理日志的redo log恢复数据的效率要高于语句逻辑日志的binlog。

&emsp; 关于事务提交时，redo log和binlog的写入顺序，为了保证主从复制时候的主从一致(当然也包括使用binlog进行基于时间点还原的情况)，是要严格一致的，MySQL通过两阶段提交过程来完成事务的一致性的，也即redo log和binlog的一致性的，理论上是先写redo log，再写binlog，两个日志都提交成功(刷入磁盘)，事务才算真正的完成。

<!-- 
&emsp; redo log 是 InnoDB 引擎特有的；binlog是MySQL的Server层实现的，所有引擎都可以使用。  
&emsp; redo log 是物理日志，记录的是在某个数据页上做了什么修改；binlog 是逻辑日志，记录的是DDL和DML操作语句。  
&emsp; redo log 是循环写的，空间固定会用完；binlog 是可以追加写入的。追加写是指binlog 文件写到一定大小后会切换到下一个，并不会覆盖以前的日志。  



1. redo log是在InnoDB存储引擎层产生，而binlog是MySQL数据库的上层产生的，并且二进制日志不仅仅针对INNODB存储引擎，MySQL数据库中的任何存储引擎对于数据库的更改都会产生二进制日志。  
2. 两种日志记录的内容形式不同。MySQL的binlog是逻辑日志，其记录是对应的SQL语句。而innodb存储引擎层面的重做日志是物理日志。  
3. 两种日志与记录写入磁盘的时间点不同，二进制日志只在事务提交完成后进行一次写入。而innodb存储引擎的重做日志在事务进行中不断地被写入，并日志不是随事务提交的顺序进行写入的。  
&emsp; 二进制日志仅在事务提交时记录，并且对于每一个事务，仅在事务提交时记录，并且对于每一个事务，仅包含对应事务的一个日志。而对于innodb存储引擎的重做日志，由于其记录是物理操作日志，因此每个事务对应多个日志条目，并且事务的重做日志写入是并发的，并非在事务提交时写入，其在文件中记录的顺序并非是事务开始的顺序。  
4. binlog不是循环使用，在写满或者重启之后，会生成新的binlog文件，redo log是循环使用。  
5. binlog可以作为恢复数据使用，主从复制搭建，redo log作为异常宕机或者介质故障后的数据恢复使用。  
-->

&emsp; redo log 和 binlog 是怎么关联起来的？  
&emsp; redo log 和 binlog 有一个共同的数据字段，叫XID。崩溃恢复的时候，会按顺序扫描 redo log：  

* 如果碰到既有 prepare、又有commit的redo log，就直接提交；  
* 如果碰到只有 parepare、而没有commit 的 redo log，就拿着 XID 去 binlog 找对应的事务。  

-----

| |redo log|binlog|
|---|---|---|
|文件大小|redo log的大小是固定的。	|binlog可通过配置参数max_binlog_size设置每个binlog文件的大小。|
|实现方式|redo log是InnoDB引擎层实现的，并不是所有引擎都有。	|binlog是Server层实现的，所有引擎都可以使用 binlog日志|
|记录方式|redo log 采用循环写的方式记录，当写到结尾时，会回到开头循环写日志。|binlog 通过追加的方式记录，当文件大小大于给定值后，后续的日志会记录到新的文件上|
|适用场景|redo log适用于崩溃恢复(crash-safe)	|binlog适用于主从复制和数据恢复|

&emsp; 由binlog和redo log的区别可知：binlog日志只用于归档，只依靠binlog是没有crash-safe能力的。但只有redo log也不行，因为redo log是InnoDB特有的，且日志上的记录落盘后会被覆盖掉。因此需要binlog和redo log二者同时记录，才能保证当数据库发生宕机重启时，数据不会丢失。  



## 1.2. 两阶段提交  
<!-- 
Redo log 两阶段提交
更新内存后引擎层写 Redo log 将状态改成 prepare 为预提交第一阶段，Server 层写 Binlog，将状态改成 commit为提交第二阶段。两阶段提交可以确保 Binlog 和 Redo log 数据一致性。  
-->

&emsp; 问题：为什么redolog要分两步写，中间再穿插写binlog呢？  
&emsp; 从上面可以看出，因为redolog影响主库的数据，binlog影响从库的数据，所以redolog和binlog必须保持一致才能保证主从数据一致，这是前提。  
&emsp; 相信很多有过开发经验的同学都知道分布式事务， **<font color = "clime">这里的redolog和binlog其实就是很典型的分布式事务场景，因为两者本身就是两个独立的个体，要想保持一致，就必须使用分布式事务的解决方案来处理。而将redolog分成了两步，其实就是使用了两阶段提交协议(Two-phaseCommit，2PC)。</font>**  
&emsp; 下面对更新语句的执行流程进行简化，看一下MySQL的两阶段提交是如何实现的：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-167.png)  
&emsp; 从图中可看出，事务的提交过程有两个阶段，就是将redolog的写入拆成了两个步骤：prepare和commit，中间再穿插写入binlog。  
&emsp; 如果这时候你很疑惑，为什么一定要用两阶段提交呢，如果不用两阶段提交会出现什么情况，比如先写redolog，再写binlog或者先写binlog，再写redolog不行吗？下面我们用反证法来进行论证。  
&emsp; 我们继续用`update T set c=c+1 where id=2`这个例子，假设id=2这一条数据的c初始值为0。那么在redolog写完，binlog还没有写完的时候，MySQL进程异常重启。由于redolog已经写完了，系统重启后会通过redolog将数据恢复回来，所以恢复后这一？c的值是1。但是由于binlog没写完就crash？，这时候binlog？面就没有记录这个语？。因此，不管是现在的从库还是之后通过这份binlog还原临时库都没有这一次更新，c的值还是0，与原库的值不同。  
&emsp; 同理，如果先写binlog，再写redolog，中途系统crash了，也会导致主从不一致，这里就不再详述。  
&emsp; 所以将redolog分成两步写，即两阶段提交，才能保证redolog和binlog内容一致，从而保证主从数据一致。  
&emsp; 两阶段提交虽然能够保证单事务两个日志的内容一致，但在多事务的情况下，却不能保证两者的提交顺序一致，比如下面这个例子，假设现在有3个事务同时提交：  
&emsp; T1(--prepare--binlog---------------------commit)T2(-----prepare-----binlog----commit)T3(--------prepare-------binlog------commit)解析：redologprepare的顺序：T1--》T2--》T3binlog的写入顺序：T1--》T2--》T3redologcommit的顺序：T2--》T3--》T1  
&emsp; 结论：由于binlog写入的顺序和redolog提交结束的顺序不一致，导致binlog和redolog所记录的事务提交结束的顺序不一样，最终导致的结果就是主从数据不一致。  
&emsp; 因此，在两阶段提交的流程基础上，还需要加一个锁来保证提交的原子性，从而保证多事务的情况下，两个日志的提交顺序一致。所以在早期的MySQL版本中，通过使用prepare_commit_mutex锁来保证事务提交的顺序，在一个事务获取到锁时才能进入prepare，一直到commit结束才能释放锁，下个事务才可以继续进行prepare操作。通过加锁虽然完美地解决了顺序一致性的问题，但在并发量较大的时候，就会导致对锁的争用，性能不佳。除了锁的争用会影响到性能之外，还有一个对性能影响更大的点，就是每个事务提交都会进行两次fsync(写磁盘)，一次是redolog落盘，另一次是binlog落盘。大家都知道，写磁盘是昂贵的操作，对于普通磁盘，每秒的QPS大概也就是几百。  


-------------------------

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-144.png)  

&emsp; **<font color = "clime">MySQL使用两阶段提交主要解决binlog 和 redo log的数据一致性的问题。</font>**  
&emsp; **<font color = "clime">redo log和binlog都可以用于表示事务的提交状态，而两阶段提交就是让这两个状态保持逻辑上的一致。</font>** 下图为MySQL两阶段提交简图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-96.png)  
&emsp; <font color = "clime">两阶段提交原理描：（redolog要分两步写，中间再穿插写binlog）</font> 

1. InnoDB redo log写盘，InnoDB事务进入prepare状态。  
2. 如果前面prepare成功，binlog写盘，那么再继续将事务日志持久化到binlog，如果持久化成功，那么InnoDB事务则进入commit状态(在redo log里面写一个commit记录)  

&emsp; 备注: 每个事务 binlog 的末尾，会记录一个 XID event，标志着事务是否提交成功，也就是说，recovery 过程中，binlog 最后一个 XID event 之后的内容都应该被 purge。


## 1.3. ~~MySQL中更新一条语句的流程~~
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-169.png)  
&emsp; 具体流程：  

1. server层中的执行器先找引擎取id=2这一行。id是主键，引擎直接用树搜索找到这一行。如果id=2这一行所在的数据页本来就在内存中，就直接返回给执行器；否则，需要先从磁盘读入内存，然后再返回；  
2. 执行器拿到引擎给的行数据，对相应的值进行处理（加1），再调用引擎接口写入这行新数据；  
3. 引擎将这行新数据更新到内存中，同时将这个更新操作记录到redo log里面，此时redo log处于prepare状态。然后通知执行器执行完成了，随时可以提交事务；  
4. 执行器生成这个操作的binlog，并把binlog写入磁盘；  
5. 执行器调用引擎的提交事务接口，引擎把刚刚写入的redo log改成提交（commit）状态，完成更新；  
6. （redo log的写入分成两个步骤prepare和commit，这就是两阶段提交)  



## 1.4. ~~两阶段提交中，MySQL异常重启（crash），是如何保证数据完整性的？~~  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-170.png)  

1. 在上图时刻A中，也就是写入redo log处于prepare阶段以后、写binlog之前，发生了崩溃（crash）：由于此时binlog还没写，redo log也还没提交（commit），所以崩溃恢复的时候，这个事务会回滚。这时候，binlog还没写，所以也不会传到备库，数据一致；  
2. 在上图时刻B中，也就是写完binlog之后，发生crash：如果redo log里面的事务有commit标识（事务是完整的）则直接提交；如果redo log里面的事务只有prepare没有commit，则判断对应的事务在binlog是否存在并完整，完整则提交事务，否则回滚事务；  
3. MySQL怎么知道binlog是完整的：一个事务的binlog是有完整格式的，statement格式（记录sql语句），最后会有个COMMIT; row格式（记录行的内容，记两条，更新前和更新后都有），最后会有个XID event  
4. redo log和binlog是怎么关联起来的：他们有一个共同的数据字段XID。崩溃恢复的时候，会按顺序扫描redo log；上述第二点中的崩溃恢复场景（redo log里面的事务只有完整的prepare而没有commit），那么mysql就会拿着XID去binlog找是否存在对应的完整事务；  



## 1.5. 组提交  
<!-- 
https://blog.csdn.net/weixin_39820173/article/details/113402536
-->


## 1.6. ~~恢复~~
<!-- 

https://blog.csdn.net/weixin_37692493/article/details/106970706


&emsp; 启动innodb的时候，不管上次是正常关闭还是异常关闭，总是会进行恢复操作。因为redo log记录的是数据页的物理变化，因此恢复的时候速度比逻辑日志(如binlog)要快很多。重启innodb时，首先会检查磁盘中数据页的LSN，如果数据页的LSN小于日志中的LSN，则会从checkpoint开始恢复。还有一种情况，在宕机前正处于checkpoint的刷盘过程，且数据页的刷盘进度超过了日志页的刷盘进度，此时会出现数据页中记录的LSN大于日志中的LSN，这时超出日志进度的部分将不会重做，因为这本身就表示已经做过的事情，无需再重做。  


&emsp; 有了 redo log 日志，那么在数据库进行异常重启的时候，可以根据 redo log 日志进行恢复，也就达到了 crash-safe。  
&emsp; redo log 用于保证 crash-safe 能力。innodb_flush_log_at_trx_commit 这个参数设置成 1 的时候，表示每次事务的 redo log 都直接持久化到磁盘。这个参数建议设置成 1，这样可以保证 MySQL 异常重启之后数据不丢失。  

https://www.jianshu.com/p/d0e16db410e4
Redo log 容灾恢复过程

MySQL的处理过程如下

    判断 redo log 是否完整，如果判断是完整(commit)的，直接用 Redo log 恢复
    如果 redo log 只是预提交 prepare 但不是 commit 状态，这个时候就会去判断 binlog 是否完整，如果完整就提交 Redo log，用 Redo log 恢复，不完整就回滚事务，丢弃数据。

只有在 redo log 状态为 prepare 时，才会去检查 binlog 是否存在，否则只校验 redo log 是否是 commit 就可以啦。怎么检查 binlog：一个完整事务 binlog 结尾有固定的格式。


&emsp; 启动innodb的时候，不管上次是正常关闭还是异常关闭，总是会进行恢复操作。因为redo log记录的是数据页的物理变化，因此恢复的时候速度比逻辑日志(如binlog)要快很多。重启innodb时，首先会检查磁盘中数据页的LSN，如果数据页的LSN小于日志中的LSN，则会从checkpoint开始恢复。还有一种情况，在宕机前正处于checkpoint的刷盘过程，且数据页的刷盘进度超过了日志页的刷盘进度，此时会出现数据页中记录的LSN大于日志中的LSN，这时超出日志进度的部分将不会重做，因为这本身就表示已经做过的事情，无需再重做。  
-->

    lsn: 可以理解为数据库从创建以来产生的redo日志量，这个值越大，说明数据库的更新越多，也可以理解为更新的时刻。此外，每个数据页上也有一个lsn，表示最后被修改时的lsn，值越大表示越晚被修改。比如，数据页A的lsn为100，数据页B的lsn为200，checkpoint lsn为150，系统lsn为300，表示当前系统已经更新到300，小于150的数据页已经被刷到磁盘上，因此数据页A的最新数据一定在磁盘上，而数据页B则不一定，有可能还在内存中。

&emsp; 数据库关闭只有2种情况，正常关闭，非正常关闭(包括数据库实例crash及服务器crash)。正常关闭情况，所有buffer pool里边的脏页都会刷新一遍到磁盘，同时记录最新LSN到ibdata文件的第一个page中。而非正常关闭来不及做这些操作，也就是没及时把脏数据flush到磁盘，也没有记录最新LSN到ibdata file。  

&emsp; 启动innodb的时候，不管上次是正常关闭还是异常关闭，总是会进行恢复操作。因为redo log记录的是数据页的物理变化，因此恢复的时候速度比逻辑日志(如binlog)要快很多。重启innodb时，会先检查数据页中的LSN，如果这个 LSN 小于 redo log 中的LSN，即write pos位置，说明在redo log上记录着数据页上尚未完成的操作，接着就会从最近的一个check point出发，开始同步数据。  

&emsp; 简单理解，比如：redo log的LSN是500，数据页的LSN是300，表明重启前有部分数据未完全刷入到磁盘中，那么系统则将redo log中LSN序号300到500的记录进行重放刷盘。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-168.png)  


-----------

&emsp; **<font color = "red">当重启数据库实例的时候，数据库做2个阶段性操作：redo log处理，undo log及binlog 处理。在崩溃恢复中还需要回滚没有提交的事务，提交没有提交成功的事务。由于回滚操作需要undo日志的支持，undo日志的完整性和可靠性需要redo日志来保证，所以崩溃恢复先做redo前滚，然后做undo回滚。</font>**  

<!-- 
怎么进行数据恢复？
binlog 会记录所有的逻辑操作，并且是采用追加写的形式。当需要恢复到指定的某一秒时，比如今天下午二点发现中午十二点有一次误删表，需要找回数据，那你可以这么做：
•首先，找到最近的一次全量备份，从这个备份恢复到临时库•然后，从备份的时间点开始，将备份的 binlog 依次取出来，重放到中午误删表之前的那个时刻。
这样你的临时库就跟误删之前的线上库一样了，然后你可以把表数据从临时库取出来，按需要恢复到线上库去。
-->


-----------------

