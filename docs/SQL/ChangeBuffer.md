
<!-- TOC -->

- [1. 写缓冲(change buffer)](#1-写缓冲change-buffer)

<!-- /TOC -->

<!-- 
Mysql-Innodb特性之插入缓存 
https://mp.weixin.qq.com/s/bjKbi0jKXjHUitGuCh-kbg
InnoDB的插入缓冲 
https://mp.weixin.qq.com/s/6t0_XByG8-yuyB0YaLuuBA
https://mp.weixin.qq.com/s/PF21mUtpM8-pcEhDN4dOIw
-->

# 1. 写缓冲(change buffer)
&emsp; **<font color = "clime">总结：当使用普通索引，修改数据时，没有命中缓冲池时，会使用到写缓冲（写缓冲是缓冲池的一部分）。在写缓冲中记录这个操作，一次内存操作；写入redo log，一次磁盘顺序写操作。</font>**  

<!-- 
在进行数据插入时必然会引起索引的变化，聚集索引不必说，一般都是递增有序的。而非聚集索引就不一定是什么数据了，其离散性导致了在插入时结构的不断变化，从而导致插入性能降低。
所以为了解决非聚集索引插入性能的问题，InnoDB引擎 创造了Insert Buffer。
-->
&emsp; 通常来说，InnoDB辅助索引不同于聚集索引的顺序插入，如果每次修改二级索引都直接写入磁盘，则会有大量频繁的随机IO。Change buffer 的主要目的是将对 非唯一 辅助索引页的操作缓存下来，以此减少辅助索引的随机IO，并达到操作合并的效果。它会占用部分Buffer Pool 的内存空间。  
&emsp; 如果辅助索引页已经在缓冲区了，则直接修改即可；如果不在，则先将修改保存到 Change Buffer。Change Buffer的数据在对应辅助索引页读取到缓冲区时合并到真正的辅助索引页中。Change Buffer 内部实现也是使用的 B+ 树。  
---
&emsp; <font color = "lime">change buffer是一种应用在非唯一普通索引页不在缓冲池中，</font><font color = "red">对页进行了写操作，并不会立刻将磁盘页加载到缓冲池，而仅仅记录缓冲变更，等未来数据被读取时，再将数据合并(merge)恢复到缓冲池中的技术。</font>写缓冲的目的是降低写操作的磁盘IO，提升数据库性能。  
&emsp; 什么时候缓冲池中的页，会刷到磁盘上呢？定期刷磁盘，而不是每次刷磁盘，能够降低磁盘IO，提升MySQL的性能。  


<!-- 
索引是存储在磁盘上的，所以对于索引的操作需要涉及磁盘操作。如果我们使用自增主键，那么在插入主键索引（聚簇索引）时，只需不断追加即可，不需要磁盘的随机 I/O。但是如果我们使用的是普通索引，大概率是无序的，此时就涉及到磁盘的随机 I/O，而随机I/O的性能是比较差的（Kafka 官方数据：磁盘顺序I/O的性能是磁盘随机I/O的4000~5000倍）。

因此，InnoDB 存储引擎设计了 Insert Buffer ，对于非聚集索引的插入或更新操作，不是每一次直接插入到索引页中，而是先判断插入的非聚集索引页是否在缓冲池（Buffer pool）中，若在，则直接插入；若不在，则先放入到一个 Insert Buffer 对象中，然后再以一定的频率和情况进行 Insert Buffer 和辅助索引页子节点的 merge（合并）操作，这时通常能将多个插入合并到一个操作中（因为在一个索引页中），这就大大提高了对于非聚集索引插入的性能。
插入缓冲的使用需要满足以下两个条件：1）索引是辅助索引；2）索引不是唯一的。

因为在插入缓冲时，数据库不会去查找索引页来判断插入的记录的唯一性。如果去查找肯定又会有随机读取的情况发生，从而导致 Insert Buffer 失去了意义。
-->

&emsp; 在MySQL5.5之前，叫插入缓冲(insert buffer)，只针对insert做了优化；现在对delete和update也有效，叫做写缓冲(change buffer)。  

&emsp; <font color = "lime">change buffer是一种应用在非唯一普通索引页不在缓冲池中，对页进行了写操作，并不会立刻将磁盘页加载到缓冲池，而仅仅记录缓冲变更，等未来数据被读取时，再将数据合并(merge)恢复到缓冲池中的技术。</font>写缓冲的目的是降低写操作的磁盘IO，提升数据库性能。  

&emsp; <font color = "lime">写缓冲的使用需要满足以下两个条件：</font>1. 索引是辅助索引。2. <font color = "lime">索引不是唯一的。</font>  

    辅助索引不能是唯一的，因为在把它写到插入缓冲时，并不去查找索引页的情况。如果去查找肯定又会出现离散读的情况，写缓冲就失去了意义。  
    
&emsp; **写缓冲的使用：**  
&emsp; 对于数据库的写请求。  
&emsp; 情况一  
&emsp; 假如要修改页号为4的索引页，而这个页正好在缓冲池内。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-101.png)  
&emsp; 如上图序号1-2：  
&emsp; （1）直接修改缓冲池中的页，一次内存操作；  
&emsp; （2）写入redo log，一次磁盘顺序写操作；  
&emsp; 这样的效率是最高的。  
&emsp; 是否会出现一致性问题呢？  
&emsp; 并不会。  
&emsp; （1）读取，会命中缓冲池的页；  
&emsp; （2）缓冲池LRU数据淘汰，会将“脏页”刷回磁盘；  
&emsp; （3）数据库异常奔溃，能够从redo log中恢复数据；  

&emsp; **<font color = "lime">什么时候缓冲池中的页，会刷到磁盘上呢？</font>**  
&emsp; **<font color = "red">定期刷磁盘，而不是每次刷磁盘，能够降低磁盘IO，提升MySQL的性能。</font>**

&emsp; 情况二  
&emsp; 假如要修改页号为40的索引页，而这个页正好不在缓冲池内。
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-102.png)  
此时麻烦一点，如上图需要1-3：  
&emsp; （1）先把需要为40的索引页，从磁盘加载到缓冲池，一次磁盘随机读操作；  
&emsp; （2）修改缓冲池中的页，一次内存操作；  
&emsp; （3）写入redo log，一次磁盘顺序写操作；  

&emsp; **<font color = "lime">没有命中缓冲池的时候，至少产生一次磁盘IO，</font>** 对于写多读少的业务场景，是否还有优化的空间呢？  
&emsp; 针对此情况，InnoDB采用写缓冲。  

&emsp; InnoDB加入写缓冲优化，上文“情况二，没有命中缓冲池时”流程会有什么变化？  
&emsp; 假如要修改页号为40的索引页，而这个页正好不在缓冲池内。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-103.png)  
&emsp; **<font color = "lime">加入写缓冲优化后，流程优化为：</font>**  
&emsp; **<font color = "lime">（1）在写缓冲中记录这个操作，一次内存操作；</font>**  
&emsp; **<font color = "lime">（2）写入redo log，一次磁盘顺序写操作；</font>**  
&emsp; 其性能与，这个索引页在缓冲池中，相近。  

&emsp; 是否会出现一致性问题呢？  
&emsp; 也不会。  
&emsp; （1）数据库异常奔溃，能够从redo log中恢复数据；  
&emsp; （2）写缓冲不只是一个内存结构，它也会被定期刷盘到写缓冲系统表空间；  
&emsp; （3）数据读取时，有另外的流程，将数据合并到缓冲池；  

&emsp; 不妨设，稍后的一个时间，有请求查询索引页40的数据。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-104.png)  
&emsp; 此时的流程如序号1-3：  
&emsp; （1）载入索引页，缓冲池未命中，这次磁盘IO不可避免；  
&emsp; （2）从写缓冲读取相关信息；  
&emsp; （3）恢复索引页，放到缓冲池LRU里；  
&emsp; <font color = "lime">可以看到，40这一页，在真正被读取时，才会被加载到缓冲池中。</font>  

&emsp; **什么业务场景，适合开启InnoDB的写缓冲机制？**  
* 不适合使用写缓冲  
&emsp; （1）数据库都是唯一索引；  
&emsp; （2）或者，写入一个数据后，会立刻读取它；  
&emsp; 这两类场景，在写操作进行时（进行后），本来就要进行进行页读取，本来相应页面就要入缓冲池，此时写缓存反倒成了负担，增加了复杂度。
* 适合使用写缓冲  
&emsp; （1）数据库大部分是非唯一索引；  
&emsp; （2）业务是写多读少，或者不是写后立刻读取；  
&emsp; 可以使用写缓冲，将原本每次写入都需要进行磁盘IO的SQL，优化定期批量写磁盘。  

&emsp; **有关写缓冲的参数：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-100.png)  
&emsp; 参数：innodb_change_buffer_max_size  
&emsp; 介绍：配置写缓冲的大小，占整个缓冲池的比例，默认值是25%，最大值是50%。  
&emsp; 参数：innodb_change_buffering  
&emsp; 介绍：配置哪些写操作启用写缓冲，可以设置成all/none/inserts/deletes等。  