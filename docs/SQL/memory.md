

# InnoDB内存结构
&emsp; 内存中的结构主要包括Buffer Pool，Change Buffer、Adaptive Hash Index以及redo Log Buffer四部分。 **<font color = "blue">如果从内存上来看，[Change Buffer](/docs/SQL/ChangeBuffer.md)和[Adaptive Hash Index](/docs/SQL/AdaptiveHashIndex.md)占用的内存都属于Buffer Pool，redo Log Buffer占用的内存与 [Buffer Pool](/docs/SQL/bufferPoolNew.md)独立。</font>** `即InnoDB内存主要有两大部分：缓冲池、重做日志缓冲。`  

&emsp; 内存数据落盘整体思路分析：  
![image](http://www.wt1814.com/static/view/images/SQL/sql-173.png)  
&emsp; `InnoDB内存缓冲池中的数据page要完成持久化的话，是通过两个流程来完成的，一个是脏页落盘；一个是预写redo log日志。`  

<!-- 
&emsp; 保证数据的持久性：  
&emsp; 当缓冲池中的页的版本比磁盘要新时，数据库需要将新版本的页从缓冲池刷新到磁盘。但是如果每次一个页发送变化，就进行刷新，那么性能开发是非常大的，于是InnoDB采用了Write Ahead Log（WAL）策略和Force Log at Commit机制实现事务级别下数据的持久性。  

&emsp; WAL要求数据的变更写入到磁盘前，首先必须将内存中的日志写入到磁盘；  
&emsp; Force-log-at-commit要求当一个事务提交时，所有产生的日志都必须刷新到磁盘上，如果日志刷新成功后，缓冲池中的数据刷新到磁盘前数据库发生了宕机，那么重启时，数据库可以从日志中 恢复数据。  
&emsp; 为了确保每次日志都写入到重做日志文件，在每次将重做日志缓冲写入重做日志后，必须调用一次fsync操作，将缓冲文件从文件系统缓存中真正写入磁盘。 可以通过innodb_flush_log_at_trx_commit来控制重做日志刷新到磁盘的策略。  

-->

