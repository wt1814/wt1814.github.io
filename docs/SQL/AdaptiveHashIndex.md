


# 自适应哈希索引  
<!-- 
 InnoDB到底支不支持哈希索引，为啥不同的人说的不一样？ 
 https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651962875&idx=1&sn=c6b3e7dc8a41609cfe070026bd27b71d&chksm=bd2d08278a5a813108b1f4116341ff31170574b9098e2708cbc212b008a1fac8dfd1ffeabc6b&scene=21#wechat_redirect
-->
&emsp;对于InnoDB的哈希索引，确切的应该这么说：  
&emsp;(1)InnoDB用户无法手动创建哈希索引，这一层上说，InnoDB确实不支持哈希索引；  
&emsp;(2)InnoDB会自调优(self-tuning)，如果判定建立自适应哈希索引(Adaptive Hash Index, AHI)，能够提升查询效率，InnoDB自己会建立相关哈希索引，这一层上说，InnoDB又是支持哈希索引的。  


&emsp; 哈希(hash)是一种非常快的查找方法，一般情况下查找的时间复杂度为O(1)。常用于连接(join)操作，如SQL Server和Oracle中的哈希连接(hash join)。但是SQL Server和Oracle等常见的数据库并不支持哈希索引(hash index)。MySQL的Heap存储引擎默认的索引类型为哈希，而InnoDB存储引擎提出了另一种实现方法，自适应哈希索引(adaptive hash index)。  
&emsp; <font color = "red">InnoDB存储引擎会监控对表上索引的查找，如果观察到建立哈希索引可以带来速度的提升，则建立哈希索引，所以称之为自适应(adaptive)的。</font>自适应哈希索引通过缓冲池的B+树构造而来，因此建立的速度很快。而且不需要将整个表都建哈希索引，InnoDB存储引擎会自动根据访问的频率和模式来为某些页建立哈希索引。  
&emsp; 根据InnoDB的官方文档显示，启用自适应哈希索引后，读取和写入速度可以提高2倍；对于辅助索引的连接操作，性能可以提高5倍。自适应哈希索引是非常好的优化模式，其设计思想是数据库自优化(self-tuning)，即无需DBA对数据库进行调整。  
&emsp; 查看当前自适应哈希索引的使用状况：show engine innodb status\G  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-88.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-89.png)  
&emsp; 现在可以看到自适应哈希索引的使用信息了，包括自适应哈希索引的大小、使用情况、每秒使用自适应哈希索引搜索的情况。值得注意的是，哈希索引只能用来搜索等值的查询，如select * from table where index_col='xxx'，而对于其他查找类型，如范围查找，是不能使用的。因此，这里出现了non-hash searches/s的情况。用hash searches:non-hash searches命令可以大概了解使用哈希索引后的效率。  
&emsp; 由于自适应哈希索引是由InnoDB存储引擎控制的，所以这里的信息只供参考。不过可以通过参数innodb_adaptive_hash_index来禁用或启动此特性，默认为开启。  

<!-- 
&emsp; **<font color = "red">自适应哈希索引：</font>**  
&emsp; InnoDB引擎中默认使用的是B+树索引，它会实时监控表上索引的使用情况。如果认为建立哈希索引可以提高查询效率，则自动在内存中的“自适应哈希索引缓冲区”建立哈希索引(在InnoDB中默认开启自适应哈希索引)。  
&emsp; 通过观察搜索模式，MySQL会利用index key的前缀建立哈希索引，如果一个表几乎大部分都在缓冲池中，那么建立一个哈希索引能够加快等值查询。  
&emsp; 注意：在某些工作负载下，通过哈希索引查找带来的性能提升远大于额外的监控索引搜索情况和保持这个哈希表结构所带来的开销。  
&emsp; 但某些时候，在负载高的情况下，自适应哈希索引中添加的read/write锁也会带来竞争，比如高并发的join操作。like操作和%的通配符操作也不适用于自适应哈希索引，可能要关闭自适应哈希索引。  
-->