
<!-- TOC -->

- [1. redo log(重做日志)和两次写](#1-redo-log重做日志和两次写)
    - [1.1. redo log](#11-redo-log)
        - [1.1.1. 为什么需要redo log](#111-为什么需要redo-log)
        - [1.1.2. redo log简介](#112-redo-log简介)
        - [1.1.3. redo log详解](#113-redo-log详解)
            - [1.1.3.1. 记录形式](#1131-记录形式)
            - [1.1.3.2. 写入流程，Write-Ahead Logging](#1132-写入流程write-ahead-logging)
            - [1.1.3.3. 刷盘时机](#1133-刷盘时机)
            - [1.1.3.4. 对应的物理文件](#1134-对应的物理文件)
    - [1.2. 两次写Double Write](#12-两次写double-write)
        - [1.2.1. 部分写失效](#121-部分写失效)
        - [1.2.2. doublewrite架构及流程](#122-doublewrite架构及流程)
        - [1.2.3. 性能](#123-性能)
        - [1.2.4. 相关参数](#124-相关参数)
        - [1.2.5. 总结](#125-总结)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. **redo log：**  
    1. 物理格式的日志，记录的是物理数据页面的修改的信息。  
    2. 解决事务的一致性，持久化数据。  
    3. 写入流程：当有一条记录需要更新的时候，InnoDB 引擎就会先把记录写到 redo log(redolog buffer)里面，并更新内存(buffer pool)，这个时候更新就算完成了。同时，InnoDB 引擎会在适当的时候，将这个操作记录更新到磁盘里面(刷脏页)。
    4. 刷盘时机：重做日志的写盘，并不一定是随着事务的提交才写入重做日志文件的，而是随着事务的开始，逐步开始的。先写入redo log buffer。  
2. doublewrite：<font color = "blue">如果说写缓冲change buffer带给InnoDB存储引擎的是性能，那么两次写Double Write带给InnoDB存储引擎的是数据的可靠性。</font>  
    1. MySQL将buffer中一页数据刷入磁盘，要写4个文件系统里的页。  
    2.  在应用(apply)重做日志(redo log)前，需要一个页的副本，当写入失效发生时，先通过页的副本来还原该页，再进行重做，这就是doublewrite。即doublewrite是页的副本。  
        1. 在异常崩溃时，如果不出现“页数据损坏”，能够通过redo恢复数据；
        2. 在出现“页数据损坏”时，能够通过double write buffer恢复页数据； 
    3. doublewrite分为内存和磁盘的两层架构。当有页数据要刷盘时：  
        1. 第一步：页数据先memcopy到doublewrite buffer的内存里；
        2. 第二步：doublewrite buffe的内存里，会先刷到doublewrite buffe的磁盘上；
        3. 第三步：doublewrite buffe的内存里，再刷到数据磁盘存储上； 


# 1. redo log(重做日志)和两次写
<!-- 
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

## 1.1. redo log
### 1.1.1. 为什么需要redo log  
&emsp; 事务的四大特性里面有一个是一致性，具体来说就是只要事务提交成功，那么对数据库做的修改就被永久保存下来了，不可能因为任何原因再回到原来的状态。那么mysql是如何保证一致性的呢？最简单的做法是在每次事务提交的时候，将该事务涉及修改的数据页全部刷新到磁盘中。但是这么做会有严重的性能问题，主要体现在两个方面：  

* 因为Innodb是以页为单位进行磁盘交互的，而一个事务很可能只修改一个数据页里面的几个字节，这个时候将完整的数据页刷到磁盘的话，太浪费资源了！  
* 一个事务可能涉及修改多个数据页，并且这些数据页在物理上并不连续，使用随机IO写入性能太差！  

&emsp; 因此mysql设计了redo log，具体来说就是只记录事务对数据页做了哪些修改，这样就能完美地解决性能问题了(相对而言文件更小并且是顺序IO)。  

### 1.1.2. redo log简介  
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

### 1.1.3. redo log详解
#### 1.1.3.1. 记录形式
&emsp; **<font color = "red">redo log实际上记录数据页的变更，而这种变更记录是没必要全部保存，因此redo log实现上采用了大小固定，循环写入的方式，当写到结尾时，会回到开头循环写日志。</font>** 如下图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-143.png)  
&emsp; 在innodb中，既有redo log需要刷盘，还有数据页也需要刷盘，redo log存在的意义主要就是降低对数据页刷盘的要求。  
&emsp; 在上图中，write pos表示redo log当前记录的LSN(逻辑序列号)位置，check point表示数据页更改记录刷盘后对应redo log所处的LSN(逻辑序列号)位置。write pos到check point之间的部分是redo log空着的部分，用于记录新的记录；check point到write pos之间是redo log待落盘的数据页更改记录。当write pos追上check point时，会先推动check point向前移动，空出位置再记录新的日志。  

----

&emsp; <font color = "clime">redo log是循环写的，redo log不是记录数据页更新之后的状态，而是记录这个页做了什么改动。</font>  
&emsp; redo log是固定大小的，比如可以配置为一组4个文件，每个文件的大小是1GB，那么日志总共就可以记录4GB的操作。从头开始写，写到末尾就又回到开头循环写，如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-94.png)  
&emsp; 图中展示了一组4个文件的redo log日志，checkpoint是当前要擦除的位置，擦除记录前需要先把对应的数据落盘(更新内存页，等待刷脏页)。write pos到 checkpoint之间的部分可以用来记录新的操作，如果write pos和checkpoint 相遇，说明 redolog 已满，这个时候数据库停止进行数据库更新语句的执行，转而进行 redo log 日志同步到磁盘中。checkpoint 到 write pos 之间的部分等待落盘(先更新内存页，然后等待刷脏页)。  

#### 1.1.3.2. 写入流程，Write-Ahead Logging

&emsp; <font color = "clime">~~事务开始之后就产生redo log，redo log的落盘并不是随着事务的提交才写入的，而是在事务的执行过程中，便开始写入redo log文件中。~~</font>  


&emsp; 在 MySQL 中，如果每一次的更新操作都需要写进磁盘，然后磁盘也要找到对应的那条记录，然后再更新，整个过程 IO 成本、查找成本都很高。为了解决这个问题，MySQL 的设计者就采用了日志(redo log)来提升更新效率。  
&emsp; 而日志和磁盘配合的整个过程，其实就是 MySQL 里的 WAL 技术，WAL 的全称是 Write-Ahead Logging，它的关键点就是先写日志，再写磁盘。  
&emsp; 具体来说，<font color = "clime">当有一条记录需要更新的时候，InnoDB 引擎就会先把记录写到 redo log(redolog buffer)里面，并更新内存(buffer pool)，这个时候更新就算完成了。同时，InnoDB 引擎会在适当的时候，将这个操作记录更新到磁盘里面(刷脏页)。</font>  


#### 1.1.3.3. 刷盘时机
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

#### 1.1.3.4. 对应的物理文件  

* 默认情况下，对应的物理文件位于数据库的data目录下的ib_logfile1&ib_logfile2  
* innodb_log_group_home_dir 指定日志文件组所在的路径，默认./ ，表示在数据库的数据目录下。  
* innodb_log_files_in_group 指定重做日志文件组中文件的数量，默认2  

&emsp; 关于文件的大小和数量，由以下两个参数配置  

* innodb_log_file_size 重做日志文件的大小。  
* innodb_mirrored_log_groups 指定了日志镜像文件组的数量，默认1  

## 1.2. 两次写Double Write  
<!-- 
InnoDB的Double Write
https://mp.weixin.qq.com/s?__biz=MzI0MjE4NTM5Mg==&mid=2648976025&idx=1&sn=3ee3d20a3f22528f9ba600dbbd338a64&chksm=f110af46c6672650cca073fd7f6ebd1a87944f98ba40843cdcfaca6ceff8745f1c079555af69&scene=178&cur_album_id=1536468200543027201#rd
double write buffer，你居然没听过？ 
https://mp.weixin.qq.com/s/bkoQ9g4cIcFFZBnpVh8ERQ
-->
<!-- 
脏页刷盘风险：InnoDB 的 page size一般是16KB，操作系统写文件是以4KB作为单位，那么每写一个 InnoDB 的 page 到磁盘上，操作系统需要写4个块。于是可能出现16K的数据，写入4K 时，发生了系统断电或系统崩溃，只有一部分写是成功的，这就是 partial page write(部分页写入)问题。这时会出现数据不完整的问题。
这时是无法通过 redo log 恢复的，因为 redo log 记录的是对页的物理修改，如果页本身已经损坏，重做日志也无能为力。

doublewrite 就是用来解决该问题的。doublewrite 由两部分组成，一部分为内存中的 doublewrite buffer，其大小为2MB，另一部分是磁盘上共享表空间中连续的128个页，即2个区(extent)，大小也是2M。
为了解决 partial page write 问题，当 MySQL 将脏数据刷新到磁盘的时候，会进行以下操作：
1)先将脏数据复制到内存中的 doublewrite buffer
2)之后通过 doublewrite buffer 再分2次，每次1MB写入到共享表空间的磁盘上(顺序写，性能很高)
3)完成第二步之后，马上调用 fsync 函数，将doublewrite buffer中的脏页数据写入实际的各个表空间文件(离散写)。

如果操作系统在将页写入磁盘的过程中发生崩溃，InnoDB 再次启动后，发现了一个 page 数据已经损坏，InnoDB 存储引擎可以从共享表空间的 doublewrite 中找到该页的一个最近的副本，用于进行数据恢复了。
-->
&emsp; <font color = "blue">如果说写缓冲change buffer带给InnoDB存储引擎的是性能，那么两次写Double Write带给InnoDB存储引擎的是数据的可靠性。</font>  

### 1.2.1. 部分写失效  
&emsp; <font color = "red">当数据库宕机时，可能发生数据库正在写一个页面，而这个页只写了一部分(比如16K的页，只写前4K的页)的情况，称之为部分写失效(partial page write)。</font>在InnoDB存储引擎未使用double write技术前，曾出现过因为部分写失效而导致数据丢失的情况。  

&emsp; MySQL的buffer一页的大小是16K，文件系统一页的大小是4K，也就是说，<font color = "clime">MySQL将buffer中一页数据刷入磁盘，要写4个文件系统里的页。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-118.png)  
&emsp; 如上图所示，MySQL里page=1的页，物理上对应磁盘上的1+2+3+4四个格。  
&emsp; 那么，问题来了，这个操作并非原子，如果执行到一半断电，会不会出现问题呢？  
&emsp; 会，这就是所谓的“页数据损坏”。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-120.png)  
&emsp; 如上图所示，MySQL内page=1的页准备刷入磁盘，才刷了3个文件系统里的页，掉电了，则会出现：重启后，page=1的页，物理上对应磁盘上的1+2+3+4四个格，数据完整性被破坏。  

&emsp; 有人也许会想，如果发生写失效，可以通过重做日志进行恢复。这是一个办法。但是必须清楚的是，重做日志中记录的是对页的物理操作，如偏移量800，写'aaaa'记录。如果这个页本身已经损坏，再对其进行重做是没有意义的。 **<font color = "clime">因此，在应用(apply)重做日志前，需要一个页的副本，当写入失效发生时，先通过页的副本来还原该页，再进行重做，这就是doublewrite。即doublewrite是页的副本。</font>**  

### 1.2.2. doublewrite架构及流程
&emsp; InnoDB存储引擎doublewrite的体系架构如下图所示  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-90.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-117.png)  
&emsp; <font color = "red">doublewrite分为内存和磁盘的两层架构：</font>一部分是内存中的doublewrite buffer，大小为2MB；另一部分是物理磁盘上共享表空间中连续的128个页，即两个区(extent)，大小同样为2MB(页的副本)。    

&emsp; 如上图所示，当有页数据要刷盘时：  
1. 第一步：页数据先memcopy到doublewrite buffer的内存里；  
2. 第二步：doublewrite buffe的内存里，会先刷到doublewrite buffe的磁盘上；  
3. 第三步：doublewrite buffe的内存里，再刷到数据磁盘存储上；  

&emsp; **DWB为什么能解决“页数据损坏”问题呢？**  
&emsp; 假设步骤2掉电，磁盘里依然是1+2+3+4的完整数据。只要有页数据完整，就能通过redo还原数据；假如步骤3掉电，doublewrite buffe里存储着完整的数据。所以，一定不会出现“页数据损坏”问题。  
&emsp; 写了2次，总有一个地方的数据是OK的。  

<!-- 
**<font color = "clime">1. 当缓冲池的脏页刷新时，并不直接写磁盘，而是会通过memcpy函数将脏页先拷贝到内存中的doublewrite buffer，</font>**     
2. 之后通过doublewrite buffer再分两次，每次写入1MB到共享表空间的物理磁盘上，  
3. 然后马上调用fsync函数，同步磁盘，避免缓冲写带来的问题。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-115.png)  

&emsp; 再看redo log写入关系，可以用下图演示  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-116.png)  
-->

### 1.2.3. 性能  
&emsp; 能够通过DWB保证页数据的完整性，但毕竟DWB要写两次磁盘， **<font color = "red">会不会导致数据库性能急剧降低呢？</font>**   
&emsp; 在这个过程中，因为doublewrite页是连续的，因此这个过程是顺序写的，开销并不是很大。在完成doublewrite页的写入后，再将doublewrite buffer中的页写入各个表空间文件中，此时的写入则是离散的。  

&emsp; **<font color = "clime">分析DWB执行的三个步骤：</font>**  
1. 第一步，页数据memcopy到DWB的内存，速度很快；  
2. 第二步，DWB的内存fsync刷到DWB的磁盘，属于<font color = "red">顺序追加写，</font>速度也很快；  
3. 第三步，刷磁盘，随机写，本来就需要进行，不属于额外操作；  


&emsp; 另外，128页(每页16K)2M的DWB，会分两次刷入磁盘，每次最多64页，即1M的数据，执行也是非常之快的。  
&emsp; 综上，性能会有所影响，但影响并不大。

### 1.2.4. 相关参数
&emsp; **InnoDB里有两个变量可以查看double write buffer相关的情况：**  
&emsp; Innodb_dblwr_pages_written  
&emsp; 记录写入DWB中页的数量。  
 
&emsp; Innodb_dblwr_writes  
&emsp; 记录DWB写操作的次数。  

&emsp; 可以通过以下命令观察到doublewrite运行的情况：show global status like 'innodb_dblwr%'\G  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-87.png)  
&emsp; doublewrite一共写了18 445个页，但实际的写入次数为434，(42:1)   基本上符合64:1。  
&emsp; 如果发现系统在高峰时Innodb_dblwr_pages_written:Innodb_dblwr_writes远小于64:1，那么说明系统写入压力并不是很高。  
&emsp; 如果操作系统在将页写入磁盘的过程中崩溃了，在恢复过程中，InnoDB存储引擎可以从共享表空间中的doublewrite中找到该页的一个副本，将其拷贝到表空间文件，再应用重做日志。下面显示了由doublewrite进行恢复的一种情况：  

&emsp; 参数skip_innodb_doublewrite可以禁止使用两次写功能，这时可能会发生前面提及的写失效问题。不过，如果有多台从服务器(slave server)，需要提供较快的性能(如slave上做的是RAID0)，也许启用这个参数是一个办法。不过，在需要提供数据高可靠性的主服务器(master server)上，任何时候都应确保开启两次写功能。  
&emsp; 注意：有些文件系统本身就提供了部分写失效的防范机制，如ZFS文件系统。在这种情况下，就不要启用doublewrite了。  

### 1.2.5. 总结
&emsp; **总结：**  
&emsp; MySQL有很强的数据安全性机制：  
1. 在异常崩溃时，如果不出现“页数据损坏”，能够通过redo恢复数据；  
2. <font color = "clime">在出现“页数据损坏”时，能够通过double write buffer恢复页数据；</font>  
 
&emsp; double write buffer：  
&emsp; (1)不是一个内存buffer，是一个内存/磁盘两层的结构，是InnoDB里On-Disk架构里很重要的一部分；  
&emsp; (2)是一个通过写两次，保证页完整性的机制；  
