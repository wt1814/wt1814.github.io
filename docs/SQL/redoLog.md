
<!-- TOC -->

- [1.1. redo log(重做日志)，WAL技术](#11-redo-log重做日志wal技术)
    - [1.1.1. 为什么需要redo log](#111-为什么需要redo-log)
    - [1.1.2. redo log简介](#112-redo-log简介)
    - [1.1.3. redo log详解](#113-redo-log详解)
        - [1.1.3.1. 记录形式](#1131-记录形式)
        - [1.1.3.2. ★★★写入流程，Write-Ahead Logging](#1132-★★★写入流程write-ahead-logging)
        - [1.1.3.3. 刷盘时机](#1133-刷盘时机)
        - [1.1.3.4. 对应的物理文件](#1134-对应的物理文件)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. redo log，物理格式的日志，记录的是物理数据页面的修改的信息。 **<font color = "red">`redo log实际上记录数据页的变更，而这种变更记录是没必要全部保存，`因此redo log实现上采用了大小固定，循环写入的方式，当写到结尾时，会回到开头循环写日志。</font>**    
2. 解决事务的一致性，持久化数据。  
3. `写入流程(Write-Ahead Logging，‘日志’先行)：当有一条记录需要更新的时候，InnoDB引擎就会先把记录写到redo log(redolog buffer)里面，并更新内存(buffer pool)，这个时候更新就算完成了。`同时，InnoDB引擎会在适当的时候，`将这个redoLog操作记录更新到磁盘里面（刷脏页）`。
4. 刷盘时机：重做日志的写盘，并不一定是随着事务的提交才写入重做日志文件的，而是随着事务的开始，逐步开始的。先写入redo log buffer。  


# 1.1. redo log(重做日志)，WAL技术
<!-- 

https://mp.weixin.qq.com/s/oia-GM1xWQBrQcnkNAgSjA
讲讲 MySQL 中的 WAL 策略和 CheckPoint 技术
https://mp.weixin.qq.com/s/bZJkylcGYyn2-POfLKfFtA

redo log日志
https://mp.weixin.qq.com/s/qgHlSo0wFJvPsYoEsU-xIQ
讲讲 MySQL 中的 WAL 策略和 CheckPoint 技术
https://mp.weixin.qq.com/s/bZJkylcGYyn2-POfLKfFtA

redo log是什么？
*** https://mp.weixin.qq.com/s/-ejTflS8v8UJhOkPk3pucQ

https://mp.weixin.qq.com/s/KUWamAYrBxFJi7t46hh79g
Log Buffer 
https://mp.weixin.qq.com/s/-Hx2KKYMEQCcTC-ADEuwVA
https://mp.weixin.qq.com/s/mNfjT99qIbjKGraZLV8EIQ
https://mp.weixin.qq.com/s/Cdq5aVYXUGQqxUdsnLlA8w


重做日志缓冲(redo log buffer)
https://mp.weixin.qq.com/s/Cdq5aVYXUGQqxUdsnLlA8w

知识点：了解InnoDB的Checkpoint技术 
https://mp.weixin.qq.com/s/rQX3AFivFDNIYXE7-r9U_w
-->
<!-- 
redo 与检查点

InnoDB 使用日志先行策略，将数据修改先在内存中完成，并且将事务记录成重做日志(Redo Log)，转换为顺序IO高效的提交事务。
这里日志先行，说的是日志记录到数据库以后，对应的事务就可以返回给用户，表示事务完成。但是实际上，这个数据可能还只在内存中修改完，并没有刷到磁盘上去。内存是易失的，如果在数据落地前，机器挂了，那么这部分数据就丢失了。
InnoDB 通过 redo 日志来保证数据的一致性。如果保存所有的重做日志，显然可以在系统崩溃时根据日志重建数据。
当然记录所有的重做日志不太现实，所以 InnoDB 引入了检查点机制。即定期检查，保证检查点之前的日志都已经写到磁盘，则下次恢复只需要从检查点开始。
-->


## 1.1.1. 为什么需要redo log  
&emsp; 事务的四大特性里面有一个是一致性，具体来说就是只要事务提交成功，那么对数据库做的修改就被永久保存下来了，不可能因为任何原因再回到原来的状态。那么mysql是如何保证一致性的呢？最简单的做法是在每次事务提交的时候，将该事务涉及修改的数据页全部刷新到磁盘中。但是这么做会有严重的性能问题，主要体现在两个方面：  

* 因为Innodb是以页为单位进行磁盘交互的，而一个事务很可能只修改一个数据页里面的几个字节，这个时候将完整的数据页刷到磁盘的话，太浪费资源了！  
* 一个事务可能涉及修改多个数据页，并且这些数据页在物理上并不连续，使用随机IO写入性能太差！  

&emsp; 因此mysql设计了redo log，具体来说就是只记录事务对数据页做了哪些修改，这样就能完美地解决性能问题了(相对而言文件更小并且是顺序IO)。  

## 1.1.2. redo log简介  
<!--
* redo log(重做日志) 实现持久化  
&emsp; 在innoDB的存储引擎中，事务日志通过重做(redo)日志和innoDB存储引擎的日志缓冲(InnoDB Log Buffer)实现。<font color = "red">事务开启时，事务中的操作，都会先写入存储引擎的日志缓冲中，在事务提交之前，这些缓冲的日志都需要提前刷新到磁盘上持久化，</font>这就是DBA们口中常说的“日志先行”(Write-Ahead Logging)。<font color = "red">当事务提交之后，在Buffer Pool中映射的数据文件才会慢慢刷新到磁盘。</font>此时如果数据库崩溃或者宕机，那么当系统重启进行恢复时，就可以根据redo log中记录的日志，把数据库恢复到崩溃前的一个状态。未完成的事务，可以继续提交，也可以选择回滚，这基于恢复的策略而定。  
&emsp; 在系统启动的时候，就已经为redo log分配了一块连续的存储空间，以顺序追加的方式记录Redo Log，通过顺序IO来改善性能。所有的事务共享redo log的存储空间，它们的Redo Log按语句的执行顺序，依次交替的记录在一起。  
-->
&emsp; **物理格式的日志，记录的是物理数据页面的修改的信息，这个页 “做了什么改动”。如：add xx记录 to Page1，向数据页Page1增加一个记录。**    

<!-- 
&emsp; **作用：**  

* <font color = "red">确保事务的持久性。</font>防止在发生故障的时间点，尚有脏页未写入磁盘，在重启mysql服务的时候，根据redo log进行重做，从而达到事务的持久性这一特性。  
* 提高性能：先写Redo log记录更新。当等到有空闲线程、内存不足、Redo log满了时刷脏。写 Redo log是顺序写入，刷脏是随机写，所以性能得到提升。此技术称为WAL技术：Write-Ahead Logging，它的关键点就是先写日记磁盘，再写数据磁盘。  
-->

## 1.1.3. redo log详解
### 1.1.3.1. 记录形式
&emsp; **<font color = "red">redo log实际上记录数据页的变更，而这种变更记录是没必要全部保存，因此redo log实现上采用了大小固定，循环写入的方式，当写到结尾时，会回到开头循环写日志。</font>** 如下图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-143.png)  
&emsp; 在innodb中，既有redo log需要刷盘，还有数据页也需要刷盘，redo log存在的意义主要就是降低对数据页刷盘的要求。  
&emsp; 在上图中，write pos表示redo log当前记录的LSN(逻辑序列号)位置，check point表示数据页更改记录刷盘后对应redo log所处的LSN(逻辑序列号)位置。write pos到check point之间的部分是redo log空着的部分，用于记录新的记录；check point到write pos之间是redo log待落盘的数据页更改记录。当write pos追上check point时，会先推动check point向前移动，空出位置再记录新的日志。  

----

&emsp; <font color = "clime">redo log是循环写的，redo log不是记录数据页更新之后的状态，而是记录这个页做了什么改动。</font>  
&emsp; redo log是固定大小的，比如可以配置为一组4个文件，每个文件的大小是1GB，那么日志总共就可以记录4GB的操作。从头开始写，写到末尾就又回到开头循环写，如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-94.png)  
&emsp; 图中展示了一组4个文件的redo log日志，checkpoint是当前要擦除的位置，擦除记录前需要先把对应的数据落盘(更新内存页，等待刷脏页)。write pos到 checkpoint之间的部分可以用来记录新的操作，如果write pos和checkpoint 相遇，说明 redolog 已满，这个时候数据库停止进行数据库更新语句的执行，转而进行 redo log 日志同步到磁盘中。checkpoint 到 write pos 之间的部分等待落盘(先更新内存页，然后等待刷脏页)。  

### 1.1.3.2. ★★★写入流程，Write-Ahead Logging

&emsp; <font color = "clime">~~事务开始之后就产生redo log，redo log的落盘并不是随着事务的提交才写入的，而是在事务的执行过程中，便开始写入redo log文件中。~~</font>  


&emsp; 在 MySQL 中，如果每一次的更新操作都需要写进磁盘，然后磁盘也要找到对应的那条记录，然后再更新，整个过程 IO 成本、查找成本都很高。为了解决这个问题，MySQL 的设计者就采用了日志(redo log)来提升更新效率。  
&emsp; 而日志和磁盘配合的整个过程，其实就是 MySQL 里的 WAL 技术，WAL 的全称是 Write-Ahead Logging，它的关键点就是先写日志，再写磁盘。  
&emsp; 具体来说，<font color = "clime">当有一条记录需要更新的时候，InnoDB 引擎就会先把记录写到 redo log(redolog buffer)里面，并更新内存(buffer pool)，这个时候更新就算完成了。同时，InnoDB 引擎会在适当的时候，将这个操作记录更新到磁盘里面(刷脏页)。</font>  


-----------

在计算机体系中，CPU处理速度和硬盘的速度，是不在同一个数量级上的，为了让它们速度匹配，从而催生了我们的内存模块，但是内存有一个特点，就是掉电之后，数据就会丢失，不是持久的，我们需要持久化的数据，最后都需要存储到硬盘上。   

InnoDB引擎设计者也利用了类似的设计思想，先写内存，再写硬盘，这样就不会因为redo log写硬盘IO而导致数据库性能问题。在InnoDB中，这种技术有一个专业名称，叫做Write-Ahead Log（预先日志持久化）  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-184.png)  



### 1.1.3.3. 刷盘时机
&emsp; 1). 先将日志写入redo log buffer。  
&emsp; 2). 将redo log buffer写入redo log file。可以通过innodb_flush_log_at_trx_commit参数控制刷盘时机，各参数值含义如下：  

|参数值	|含义|
|---|---|
|0(延迟写)	|事务提交时不会将redo log buffer中日志写入到os buffer，而是每秒写入os buffer并调用fsync()写入到redo log file中。也就是说设置为0时是(大约)每秒刷新写入到磁盘中的，当系统崩溃，会丢失1秒钟的数据。|
|1(实时写，实时刷)	|事务每次提交都会将redo log buffer中的日志写入os buffer并调用fsync()刷到redo log file中。这种方式即使系统崩溃也不会丢失任何数据，但是因为每次提交都写入磁盘，IO的性能较差。|
|2(实时写，延迟刷)	|每次提交都仅写入到os buffer，然后是每秒调用fsync()将os buffer中的日志写入到redo log file。|

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-142.png)  

-----

&emsp; **<font color = "red">很重要一点，redo log是什么时候写盘的？前面说了是在事物开始之后逐步写盘的。</font>**  
&emsp; <font color = "clime">之所以说重做日志是在事务开始之后逐步写入重做日志文件，而不一定是事务提交才写入重做日志缓存，原因就是，重做日志有一个缓存区redo log buffer，Innodb存储引擎先将重做日志写入redo log buffer中。</font>redo log buffer的默认大小为8M(这里设置的16M)。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-80.png)  

&emsp; <font color = "clime">然后会通过以下三种方式将innodb日志缓冲区的日志刷新到磁盘。</font>  

* Master Thread 每秒一次执行刷新Innodb_log_buffer到重做日志文件。  
* 每个事务提交时会将重做日志刷新到重做日志文件。  
* 当重做日志缓存可用空间 少于一半时，重做日志缓存被刷新到重做日志文件。  

&emsp; 由此可以看出，重做日志通过不止一种方式写入到磁盘，尤其是对于第一种方式，Innodb_log_buffer到重做日志文件是Master Thread线程的定时任务。  
&emsp; 因此重做日志的写盘，并不一定是随着事务的提交才写入重做日志文件的，而是随着事务的开始，逐步开始的。  

### 1.1.3.4. 对应的物理文件  

* 默认情况下，对应的物理文件位于数据库的data目录下的ib_logfile1&ib_logfile2  
* innodb_log_group_home_dir 指定日志文件组所在的路径，默认./ ，表示在数据库的数据目录下。  
* innodb_log_files_in_group 指定重做日志文件组中文件的数量，默认2  

&emsp; 关于文件的大小和数量，由以下两个参数配置  

* innodb_log_file_size 重做日志文件的大小。  
* innodb_mirrored_log_groups 指定了日志镜像文件组的数量，默认1  
