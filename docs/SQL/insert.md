


# insert插入流程  
<!-- 
回顾下写流程
https://mp.weixin.qq.com/s/CYPARs7o_X9PnMlkGxtOcw
-->

## 事务提交前的日志文件写入  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-150.png)  

1. 首先 insert 进入 server 层后，会进行一些必要的检查，检查的过程中并不会涉及到磁盘的写入。
2. 检查没有问题之后，便进入引擎层开始正式的提交。我们知道 InnoDB 会将数据页缓存至内存中的 buffer pool，所以 insert 语句到了这里并不需要立刻将数据写入磁盘文件中，只需要修改 buffer pool 当中对应的数据页就可以了。

    buffer pool 中的数据页刷盘并不需要在事务提交前完成，其中的交互过程会在下一张图中分解。

4. 但仅仅写入内存的 buffer pool 并不能保证数据的持久化，如果 MySQL 宕机重启了，需要保证 insert 的数据不会丢失。redo log 因此而生，当 innodb_flush_log_at_trx_commit=1 时，每次事务提交都会触发一次 redo log 刷盘。（redo log 是顺序写入，相比直接修改数据文件，redo 的磁盘写入效率更加高效）
5. 如果开启了 binlog 日志，还需将事务逻辑数据写入binlog 文件，且为了保证复制安全，建议使用 sync_binlog=1 ，也就是每次事务提交时，都要将 binlog 日志的变更刷入磁盘。  

&emsp; 综上（在 InnoDB buffer pool 足够大且上述的两个参数设置为双一时），insert 语句成功提交时，真正发生磁盘数据写入的，并不是 MySQL 的数据文件，而是 redo log 和 binlog 文件。然而，InnoDB buffer pool 不可能无限大，redo log 也需要定期轮换，很难容下所有的数据，下面我们就来看看 buffer pool 与磁盘数据文件的交互方式。

## 事务提交后的数据文件写入  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-151.png)  
1. 当 buffer pool 中的数据页达到一定量的脏页或 InnoDB 的 IO 压力较小 时，都会触发脏页的刷盘操作。
2. 当开启 double write 时，InnoDB 刷脏页时首先会复制一份刷入 double write，在这个过程中，由于double write的页是连续的，对磁盘的写入也是顺序操作，性能消耗不大。
3. 无论是否经过 double write，脏页最终还是需要刷入表空间的数据文件。刷入完成后才能释放 buffer pool 当中的空间。
4. insert buffer 也是 buffer pool 中的一部分，当 buffer pool 空间不足需要交换出部分脏页时，有可能将 insert buffer 的数据页换出，刷入共享表空间中的 insert buffer 数据文件中。
5. 当 innodb_stats_persistent=ON 时，SQL 语句所涉及到的 InnoDB 统计信息也会被刷盘到 innodb_table_stats 和 innodb_index_stats 这两张系统表中，这样就不用每次再实时计算了。
6. 有一些情况下可以不经过 double write 直接刷盘

    * 关闭 double write  
    * 不需要 double write 保障，如 drop table 等操作  


&emsp; 汇总两张图，一条 insert 语句的所有涉及到的数据在磁盘上会依次写入 redo log，binlog，(double write，insert buffer) 共享表空间，最后在自己的用户表空间落定为安。