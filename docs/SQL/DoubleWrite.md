

<!-- TOC -->

- [1. 两次写Double Write](#1-两次写double-write)
    - [1.1. 部分写失效](#11-部分写失效)
    - [1.2. doublewrite架构及流程](#12-doublewrite架构及流程)
    - [1.3. 性能](#13-性能)
    - [1.4. 相关参数](#14-相关参数)
    - [1.5. 总结](#15-总结)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; doublewrite：<font color = "blue">如果说写缓冲change buffer带给InnoDB存储引擎的是性能，那么两次写Double Write带给InnoDB存储引擎的是数据的可靠性。</font>  
1. MySQL将buffer中一页数据刷入磁盘，要写4个文件系统里的页。  
2. 在应用(apply)重做日志(redo log)前，需要一个页的副本，当写入失效发生时，先通过页的副本来还原该页，再进行重做，这就是doublewrite。即doublewrite是页的副本。  
    1. 在异常崩溃时，如果不出现“页数据损坏”，能够通过redo恢复数据；
    2. 在出现“页数据损坏”时，能够通过double write buffer恢复页数据； 
3. doublewrite分为内存和磁盘的两层架构。当有页数据要刷盘时：  
    1. 第一步：页数据先memcopy到doublewrite buffer的内存里；
    2. 第二步：doublewrite buffe的内存里，会先刷到doublewrite buffe的磁盘上；
    3. 第三步：doublewrite buffe的内存里，再刷到数据磁盘存储上； 


# 1. 两次写Double Write  
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

## 1.1. 部分写失效  
&emsp; <font color = "red">当数据库宕机时，可能发生数据库正在写一个页面，而这个页只写了一部分(比如16K的页，只写前4K的页)的情况，称之为部分写失效(partial page write)。</font>在InnoDB存储引擎未使用double write技术前，曾出现过因为部分写失效而导致数据丢失的情况。  

&emsp; MySQL的buffer一页的大小是16K，文件系统一页的大小是4K，也就是说，<font color = "clime">MySQL将buffer中一页数据刷入磁盘，要写4个文件系统里的页。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-118.png)  
&emsp; 如上图所示，MySQL里page=1的页，物理上对应磁盘上的1+2+3+4四个格。  
&emsp; 那么，问题来了，这个操作并非原子，如果执行到一半断电，会不会出现问题呢？  
&emsp; 会，这就是所谓的“页数据损坏”。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-120.png)  
&emsp; 如上图所示，MySQL内page=1的页准备刷入磁盘，才刷了3个文件系统里的页，掉电了，则会出现：重启后，page=1的页，物理上对应磁盘上的1+2+3+4四个格，数据完整性被破坏。  

&emsp; 有人也许会想，如果发生写失效，可以通过重做日志进行恢复。这是一个办法。但是必须清楚的是，重做日志中记录的是对页的物理操作，如偏移量800，写'aaaa'记录。如果这个页本身已经损坏，再对其进行重做是没有意义的。 **<font color = "clime">因此，在应用(apply)重做日志前，需要一个页的副本，当写入失效发生时，先通过页的副本来还原该页，再进行重做，这就是doublewrite。即doublewrite是页的副本。</font>**  

## 1.2. doublewrite架构及流程
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

## 1.3. 性能  
&emsp; 能够通过DWB保证页数据的完整性，但毕竟DWB要写两次磁盘， **<font color = "red">会不会导致数据库性能急剧降低呢？</font>**   
&emsp; 在这个过程中，因为doublewrite页是连续的，因此这个过程是顺序写的，开销并不是很大。在完成doublewrite页的写入后，再将doublewrite buffer中的页写入各个表空间文件中，此时的写入则是离散的。  

&emsp; **<font color = "clime">分析DWB执行的三个步骤：</font>**  
1. 第一步，页数据memcopy到DWB的内存，速度很快；  
2. 第二步，DWB的内存fsync刷到DWB的磁盘，属于<font color = "red">顺序追加写，</font>速度也很快；  
3. 第三步，刷磁盘，随机写，本来就需要进行，不属于额外操作；  


&emsp; 另外，128页(每页16K)2M的DWB，会分两次刷入磁盘，每次最多64页，即1M的数据，执行也是非常之快的。  
&emsp; 综上，性能会有所影响，但影响并不大。

## 1.4. 相关参数
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

## 1.5. 总结
&emsp; **总结：**  
&emsp; MySQL有很强的数据安全性机制：  
1. 在异常崩溃时，如果不出现“页数据损坏”，能够通过redo恢复数据；  
2. <font color = "clime">在出现“页数据损坏”时，能够通过double write buffer恢复页数据；</font>  
 
&emsp; double write buffer：  
&emsp; (1)不是一个内存buffer，是一个内存/磁盘两层的结构，是InnoDB里On-Disk架构里很重要的一部分；  
&emsp; (2)是一个通过写两次，保证页完整性的机制；  
