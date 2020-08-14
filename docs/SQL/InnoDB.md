
<!-- TOC -->

- [1. InnoDB](#1-innodb)
    - [1.1. 关键特性](#11-关键特性)
        - [1.1.1. 插入缓冲](#111-插入缓冲)
        - [1.1.2. 两次写](#112-两次写)
        - [1.1.3. 自适应哈希索引](#113-自适应哈希索引)
        - [1.1.4. 预读（read ahead）](#114-预读read-ahead)
    - [1.2. 数据恢复](#12-数据恢复)
    - [1.3. 表](#13-表)
        - [1.3.1. InnoDB的逻辑存储结构](#131-innodb的逻辑存储结构)

<!-- /TOC -->

# 1. InnoDB  
## 1.1. 关键特性  
**<font color = "red">《MySQL技术内幕：InnoDB存储引擎》</font>**  

&emsp; InnoDB存储引擎的关键特性包括插入缓冲、两次写（double write）、自适应哈希索引（adaptive hash index）、预读。  

### 1.1.1. 插入缓冲
&emsp; <font color = "red">InnoDB存储引擎开创性地设计了插入缓冲，</font>对于非聚集索引的插入或更新操作，<font color = "red">不是每一次直接插入索引页中，而是先判断插入的非聚集索引页是否在缓冲池中。如果在，则直接插入；如果不在，则先放入一个插入缓冲区中，</font>好似欺骗数据库这个非聚集的索引已经插到叶子节点了，<font color = "red">然后再以一定的频率执行插入缓冲和非聚集索引页子节点的合并操作，</font>这时通常能将多个插入合并到一个操作中（因为在一个索引页中），这就大大提高了对非聚集索引执行插入和修改操作的性能。

&emsp; <font color = "lime">插入缓冲的使用需要满足以下两个条件：</font>  

1. 索引是辅助索引。
2. 索引不是唯一的。  

&emsp; 当满足以上两个条件时，InnoDB存储引擎会使用插入缓冲，这样就能提高性能了。不过考虑一种情况，应用程序执行大量的插入和更新操作，这些操作都涉及了不唯一的非聚集索引，如果在这个过程中数据库发生了宕机，这时候会有大量的插入缓冲并没有合并到实际的非聚集索引中。如果是这样，恢复可能需要很长的时间，极端情况下甚至需要几个小时来执行合并恢复操作。  

&emsp; 辅助索引不能是唯一的，因为在把它插入到插入缓冲时，并不去查找索引页的情况。如果去查找肯定又会出现离散读的情况，插入缓冲就失去了意义。  

&emsp; 查看插入缓冲的信息：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-84.png)  
&emsp; seg size显示了当前插入缓冲的大小为2*16KB，free list len代表了空闲列表的长度，size代表了已经合并记录页的数量。  

&emsp; 下面一行可能是真正要关心的，因为它显示了提高性能了。inserts代表插入的记录数，merged recs代表合并的页的数量，merges代表合并的次数。  
&emsp; merged recs:merges大约为3:1，代表插入缓冲将对于非聚集索引页的IO请求大约降低了3倍。  

&emsp; **问题：**  
&emsp; 目前插入缓冲存在一个问题是，在写密集的情况下，插入缓冲会占用过多的缓冲池内存，默认情况下最大可以占用1/2的缓冲池内存。Percona已发布一些patch来修正插入缓冲占用太多缓冲池内存的问题，具体的可以到http//www.percona.com/percona-lab.html 查找。简单来说，修改IBUF_POOL_SIZE_PER_MAX_SIZE就可以对插入缓冲的大小进行控制，例如，将IBUF_POOL_SIZE_PER_MAX_SIZE改为3，则最大只能使用1/3的缓冲池内存。  

### 1.1.2. 两次写  
&emsp; <font color = "lime">如果说插入缓冲带给InnoDB存储引擎的是性能，那么两次写带给InnoDB存储引擎的是数据的可靠性。</font><font color = "red">当数据库宕机时，可能发生数据库正在写一个页面，而这个页只写了一部分（比如16K的页，只写前4K的页）的情况，称之为部分写失效（partial page write）。</font>在InnoDB存储引擎未使用double write技术前，曾出现过因为部分写失效而导致数据丢失的情况。  

&emsp; 有人也许会想，如果发生写失效，可以通过重做日志进行恢复。这是一个办法。但是必须清楚的是，重做日志中记录的是对页的物理操作，如偏移量800，写'aaaa'记录。如果这个页本身已经损坏，再对其进行重做是没有意义的。这就是说，<font color = "red">在应用（apply）重做日志前，需要一个页的副本，当写入失效发生时，先通过页的副本来还原该页，再进行重做，这就是doublewrite。</font>  
&emsp; InnoDB存储引擎doublewrite的体系架构如下图所示  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-90.png)  

&emsp; doublewrite由两部分组成：一部分是内存中的doublewrite buffer，大小为2MB；另一部分是物理磁盘上共享表空间中连续的128个页，即两个区（extent），大小同样为2MB(页的副本)。当缓冲池的脏页刷新时，并不直接写磁盘，而是会通过memcpy函数将脏页先拷贝到内存中的doublewrite buffer，之后通过doublewrite buffer再分两次，每次写入1MB到共享表空间的物理磁盘上，然后马上调用fsync函数，同步磁盘，避免缓冲写带来的问题。在这个过程中，因为doublewrite页是连续的，因此这个过程是顺序写的，开销并不是很大。在完成doublewrite页的写入后，再将doublewrite buffer中的页写入各个表空间文件中，此时的写入则是离散的。  
&emsp; 可以通过以下命令观察到doublewrite运行的情况： show global status like 'innodb_dblwr%'\G  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-87.png)  
&emsp; doublewrite一共写了18 445个页，但实际的写入次数为434，(42:1)   基本上符合64:1。  
&emsp; 如果发现系统在高峰时Innodb_dblwr_pages_written:Innodb_dblwr_writes远小于64:1，那么说明系统写入压力并不是很高。  
&emsp; 如果操作系统在将页写入磁盘的过程中崩溃了，在恢复过程中，InnoDB存储引擎可以从共享表空间中的doublewrite中找到改页的一个副本，将其拷贝到表空间文件，再应用重做日志。下面显示了由doublewrite进行恢复的一种情况：  

```text
090924 11：36：32 mysqld restarted
090924 11：36：33 InnoDB：Database was not shut down normally！
InnoDB：Starting crash recovery.
InnoDB：Reading tablespace information from the.ibd files……
InnoDB：Error：space id in fsp header 0，but in the page header 4294967295
InnoDB：Error：tablespace id 4294967295 in file./test/t.ibd is not sensible
InnoDB：Error：tablespace id 0 in file./test/t2.ibd is not sensible
090924 11：36：33 InnoDB：Operating system error number 40 in a file operation.
InnoDB：Error number 40 means'Too many levels of symbolic links'.
InnoDB：Some operating system error numbers are described at
InnoDB：http：//dev.mysql.com/doc/refman/5.0/en/operating-system-error-codes.html
InnoDB：File name./now/member
InnoDB：File operation call：'stat'.
InnoDB：Error：os_file_readdir_next_file（）returned-1 in
InnoDB：directory./nowInnoDB：Crash recovery may have failed for some.ibd files！
InnoDB：Restoring possible half-written data pages from the doublewrite
InnoDB：buffer……
```
&emsp; 参数skip_innodb_doublewrite可以禁止使用两次写功能，这时可能会发生前面提及的写失效问题。不过，如果有多台从服务器（slave server），需要提供较快的性能（如slave上做的是RAID0），也许启用这个参数是一个办法。不过，在需要提供数据高可靠性的主服务器（master server）上，任何时候我们都应确保开启两次写功能。  
&emsp; 注意：有些文件系统本身就提供了部分写失效的防范机制，如ZFS文件系统。在这种情况下，就不要启用doublewrite了。  

### 1.1.3. 自适应哈希索引
&emsp; 哈希（hash）是一种非常快的查找方法，一般情况下查找的时间复杂度为O(1)。常用于连接（join）操作，如SQL Server和Oracle中的哈希连接（hash join）。但是SQL Server和Oracle等常见的数据库并不支持哈希索引（hash index）。MySQL的Heap存储引擎默认的索引类型为哈希，而InnoDB存储引擎提出了另一种实现方法，自适应哈希索引（adaptive hash index）。  
&emsp; <font color = "red">InnoDB存储引擎会监控对表上索引的查找，如果观察到建立哈希索引可以带来速度的提升，则建立哈希索引，所以称之为自适应（adaptive）的。</font>自适应哈希索引通过缓冲池的B+树构造而来，因此建立的速度很快。而且不需要将整个表都建哈希索引，InnoDB存储引擎会自动根据访问的频率和模式来为某些页建立哈希索引。  
&emsp; 根据InnoDB的官方文档显示，启用自适应哈希索引后，读取和写入速度可以提高2倍；对于辅助索引的连接操作，性能可以提高5倍。自适应哈希索引是非常好的优化模式，其设计思想是数据库自优化（self-tuning），即无需DBA对数据库进行调整。  
&emsp; 查看当前自适应哈希索引的使用状况：show engine innodb status\G  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-88.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-89.png)  
&emsp; 现在可以看到自适应哈希索引的使用信息了，包括自适应哈希索引的大小、使用情况、每秒使用自适应哈希索引搜索的情况。值得注意的是，哈希索引只能用来搜索等值的查询，如select * from table where index_col='xxx'，而对于其他查找类型，如范围查找，是不能使用的。因此，这里出现了non-hash searches/s的情况。用hash searches:non-hash searches命令可以大概了解使用哈希索引后的效率。  
&emsp; 由于自适应哈希索引是由InnoDB存储引擎控制的，所以这里的信息只供参考。不过可以通过参数innodb_adaptive_hash_index来禁用或启动此特性，默认为开启。  

<!-- 
&emsp; **<font color = "red">自适应哈希索引：</font>**  
&emsp; InnoDB引擎中默认使用的是B+树索引，它会实时监控表上索引的使用情况。如果认为建立哈希索引可以提高查询效率，则自动在内存中的“自适应哈希索引缓冲区”建立哈希索引（在InnoDB中默认开启自适应哈希索引）。  
&emsp; 通过观察搜索模式，MySQL会利用index key的前缀建立哈希索引，如果一个表几乎大部分都在缓冲池中，那么建立一个哈希索引能够加快等值查询。  
&emsp; 注意：在某些工作负载下，通过哈希索引查找带来的性能提升远大于额外的监控索引搜索情况和保持这个哈希表结构所带来的开销。  
&emsp; 但某些时候，在负载高的情况下，自适应哈希索引中添加的read/write锁也会带来竞争，比如高并发的join操作。like操作和%的通配符操作也不适用于自适应哈希索引，可能要关闭自适应哈希索引。  
-->

### 1.1.4. 预读（read ahead）  
&emsp; InnoDB 在 I/O 的优化上有个比较重要的特性为预读，<font color = "red">当 InnoDB 预计某些 page 可能很快就会需要用到时，它会异步地将这些 page 提前读取到缓冲池（buffer pool）中，</font>这其实有点像空间局部性的概念。  
&emsp; 空间局部性（spatial locality）：如果一个数据项被访问，那么与它的址相邻的数据项也可能很快被访问。  
&emsp; InnoDB使用两种预读算法来提高I/O性能：线性预读（linear read-ahead）和随机预读（randomread-ahead）。  
&emsp; 其中，线性预读以 extent（块，1个 extent 等于64个 page）为单位，而随机预读放到以 extent 中的 page 为单位。线性预读着眼于将下一个extent 提前读取到 buffer pool 中，而随机预读着眼于将当前 extent 中的剩余的 page 提前读取到 buffer pool 中。  
&emsp; 线性预读（Linear read-ahead）：线性预读方式有一个很重要的变量 innodb_read_ahead_threshold，可以控制 Innodb 执行预读操作的触发阈值。如果一个 extent 中的被顺序读取的 page 超过或者等于该参数变量时，Innodb将会异步的将下一个 extent 读取到 buffer pool中，innodb_read_ahead_threshold 可以设置为0-64（一个 extend 上限就是64页）的任何值，默认值为56，值越高，访问模式检查越严格。  
&emsp; 随机预读（Random read-ahead）: 随机预读方式则是表示当同一个 extent 中的一些 page 在 buffer pool 中发现时，Innodb 会将该 extent 中的剩余 page 一并读到 buffer pool中，由于随机预读方式给 Innodb code 带来了一些不必要的复杂性，同时在性能也存在不稳定性，在5.5中已经将这种预读方式废弃。要启用此功能，请将配置变量设置 innodb_random_read_ahead 为ON。  


## 1.2. 数据恢复
&emsp; 数据库关闭只有2种情况，正常关闭，非正常关闭（包括数据库实例crash及服务器crash）。正常关闭情况，所有buffer pool里边的脏页都会都会刷新一遍到磁盘，同时记录最新LSN到ibdata文件的第一个page中。而非正常关闭来不及做这些操作，也就是没及时把脏数据flush到磁盘，也没有记录最新LSN到ibdata file。  
&emsp; 当重启数据库实例的时候，数据库做2个阶段性操作：redo log处理，undo log及binlog 处理。(在崩溃恢复中还需要回滚没有提交的事务，提交没有提交成功的事务。<font color = "red">由于回滚操作需要undo日志的支持，undo日志的完整性和可靠性需要redo日志来保证，所以崩溃恢复先做redo前滚，然后做undo回滚。</font>)

<!-- 
怎么进行数据恢复？

binlog 会记录所有的逻辑操作，并且是采用追加写的形式。当需要恢复到指定的某一秒时，比如今天下午二点发现中午十二点有一次误删表，需要找回数据，那你可以这么做：

•首先，找到最近的一次全量备份，从这个备份恢复到临时库•然后，从备份的时间点开始，将备份的 binlog 依次取出来，重放到中午误删表之前的那个时刻。

这样你的临时库就跟误删之前的线上库一样了，然后你可以把表数据从临时库取出来，按需要恢复到线上库去。
-->

## 1.3. 表
### 1.3.1. InnoDB的逻辑存储结构  
<!-- 
https://zhuanlan.zhihu.com/p/111958646
-->

&emsp; 从InnoDb存储引擎的逻辑存储结构看，所有数据都被逻辑地存放在一个空间中，称之为表空间（tablespace）。表空间又由段（segment），区（extent），页（page）组成。页在一些文档中有时候也称为块（block）。InnoDb逻辑存储结构图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-41.png)  

* 表空间（tablespace）：  
&emsp; 表空间是Innodb存储引擎逻辑的最高层，所有的数据都存放在表空间中。  
&emsp; 默认情况下，Innodb存储引擎有一个共享表空间ibdata1，即所有数据都存放在这个表空间中内。  
&emsp; 如果启用了innodbfileper_table参数，需要注意的是每张表的表空间内存放的只是数据、索引、和插入缓冲Bitmap，其他类的数据，比如回滚(undo)信息、插入缓冲检索页、系统事物信息，二次写缓冲等还是放在原来的共享表内的。  
* 段（segment）：  
&emsp; 表空间由段组成，常见的段有数据段、索引段、回滚段等。  
&emsp; InnoDB存储引擎表是索引组织的，因此数据即索引，索引即数据。数据段即为B+树的叶子结点，索引段即为B+树的非索引结点。  
&emsp; 在InnoDB存储引擎中对段的管理都是由引擎自身所完成，DBA不能也没必要对其进行控制。  
* 区（extent）：  
&emsp; 区是由连续页组成的空间，在任何情况下每个区的大小都为1MB。  
&emsp; 为了保证区中页的连续性，InnoDB存储引擎一次从磁盘申请4~5个区。  
&emsp; 默认情况下，InnoDB存储引擎页的大小为16KB，一个区中一共64个连续的区。  
* 页（page）：  
&emsp; 页是InnoDB磁盘管理的最小单位。在InnoDB存储引擎中，默认每个页的大小为16KB。  
&emsp; 从InnoDB1.2.x版本开始，可以通过参数innodbpagesize将页的大小设置为4K，8K，16K。  
&emsp; InnoDB存储引擎中，常见的页类型有：数据页，undo页，系统页，事务数据页，插入缓冲位图页，插入缓冲空闲列表页等。 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-78.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-79.png)  


