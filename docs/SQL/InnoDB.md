
<!-- TOC -->

- [1. InnoDB](#1-innodb)
    - [1.1. 关键特性](#11-关键特性)
        - [1.1.1. 缓冲池(buffer pool)](#111-缓冲池buffer-pool)
            - [1.1.1.1. 前言：预读](#1111-前言预读)
            - [1.1.1.2. LRU算法](#1112-lru算法)
                - [1.1.1.2.1. 预读失效](#11121-预读失效)
                - [1.1.1.2.2. 缓冲池污染](#11122-缓冲池污染)
            - [1.1.1.3. 相关参数](#1113-相关参数)
            - [1.1.1.4. 总结](#1114-总结)
        - [1.1.2. 写缓冲(change buffer)](#112-写缓冲change-buffer)
        - [1.1.3. 两次写](#113-两次写)
            - [1.1.3.1. 部分写失效](#1131-部分写失效)
            - [1.1.3.2. doublewrite架构及流程](#1132-doublewrite架构及流程)
            - [1.1.3.3. 性能](#1133-性能)
            - [1.1.3.4. 相关参数](#1134-相关参数)
            - [1.1.3.5. 总结](#1135-总结)
        - [1.1.4. 自适应哈希索引](#114-自适应哈希索引)
    - [1.2. 数据恢复](#12-数据恢复)
    - [1.3. 表](#13-表)
        - [1.3.1. InnoDB的逻辑存储结构](#131-innodb的逻辑存储结构)

<!-- /TOC -->

# 1. InnoDB  
**<font color = "red">《MySQL技术内幕：InnoDB存储引擎》</font>** 

## 1.1. 关键特性  
&emsp; InnoDB存储引擎的关键特性包括缓冲池、写缓冲、两次写（double write）、自适应哈希索引（adaptive hash index）。  

### 1.1.1. 缓冲池(buffer pool)  

<!-- 
https://mp.weixin.qq.com/s/nA6UHBh87U774vu4VvGhyw

&emsp; Buffer Pool 是 InnoDB 维护的一个缓存区域，用来缓存数据和索引在内存中，主要用来加速数据的读写，如果 Buffer Pool 越大，那么 MySQL 就越像一个内存数据库，默认大小为 128M。  
&emsp; InnoDB 会将那些热点数据和一些 InnoDB 认为即将访问到的数据存在 Buffer Pool 中，以提升数据的读取性能。  
&emsp; InnoDB 在修改数据时，如果数据的页在 Buffer Pool 中，则会直接修改 Buffer Pool，此时称这个页为脏页，InnoDB 会以一定的频率将脏页刷新到磁盘，这样可以尽量减少磁盘I/O，提升性能。 
-->

&emsp; MySQL作为一个存储系统，具有缓冲池(buffer pool)机制，<font color = "red">缓存表数据与索引数据，把磁盘上的数据加载到缓冲池，以避免每次查询数据都进行磁盘IO。缓冲池提高了读能力。</font>  

&emsp; 如何管理与淘汰缓冲池，使得性能最大化呢？在介绍具体细节之前，先介绍下“预读”的概念。  

#### 1.1.1.1. 前言：预读  
<!-- 
预读（read ahead）  
&emsp; InnoDB 在 I/O 的优化上有个比较重要的特性为预读，<font color = "red">当 InnoDB 预计某些 page 可能很快就会需要用到时，它会异步地将这些 page 提前读取到缓冲池（buffer pool）中，</font>这其实有点像空间局部性的概念。  
&emsp; 空间局部性（spatial locality）：如果一个数据项被访问，那么与它的址相邻的数据项也可能很快被访问。  
&emsp; InnoDB使用两种预读算法来提高I/O性能：线性预读（linear read-ahead）和随机预读（randomread-ahead）。  
&emsp; 其中，线性预读以 extent（块，1个 extent 等于64个 page）为单位，而随机预读放到以 extent 中的 page 为单位。线性预读着眼于将下一个extent 提前读取到 buffer pool 中，而随机预读着眼于将当前 extent 中的剩余的 page 提前读取到 buffer pool 中。  
&emsp; 线性预读（Linear read-ahead）：线性预读方式有一个很重要的变量 innodb_read_ahead_threshold，可以控制 Innodb 执行预读操作的触发阈值。如果一个 extent 中的被顺序读取的 page 超过或者等于该参数变量时，Innodb将会异步的将下一个 extent 读取到 buffer pool中，innodb_read_ahead_threshold 可以设置为0-64（一个 extend 上限就是64页）的任何值，默认值为56，值越高，访问模式检查越严格。  
&emsp; 随机预读（Random read-ahead）: 随机预读方式则是表示当同一个 extent 中的一些 page 在 buffer pool 中发现时，Innodb 会将该 extent 中的剩余 page 一并读到 buffer pool中，由于随机预读方式给 Innodb code 带来了一些不必要的复杂性，同时在性能也存在不稳定性，在5.5中已经将这种预读方式废弃。要启用此功能，请将配置变量设置 innodb_random_read_ahead 为ON。  
-->

&emsp; 什么是预读？  
&emsp; 磁盘读写，并不是按需读取，而是按页读取，**<font color = "lime">一次至少读一页数据（一般是4K），如果未来要读取的数据就在页中，就能够省去后续的磁盘IO，提高效率。</font>**  

&emsp; 预读为什么有效？  
&emsp; 数据访问，通常都遵循“集中读写”的原则，使用一些数据，大概率会使用附近的数据，这就是所谓的“局部性原理”，它表明提前加载是有效的，确实能够减少磁盘IO。  

&emsp; 按页(4K)读取，和InnoDB的缓冲池设计有什么关系？  
&emsp; （1）磁盘访问按页读取能够提高性能，所以缓冲池一般也是按页缓存数据；  
&emsp; （2）**<font color = "lime">预读机制能把一些“可能要访问”的页提前加入缓冲池，避免未来的磁盘IO操作；</font>**  

&emsp; InnoDB使用两种预读算法来提高I/O性能：线性预读（linear read-ahead）和随机预读（randomread-ahead）。  
&emsp; 其中，线性预读以 extent（块，1个 extent 等于64个 page）为单位，而随机预读放到以 extent 中的 page 为单位。线性预读着眼于将下一个extent 提前读取到 buffer pool 中，而随机预读着眼于将当前 extent 中的剩余的 page 提前读取到 buffer pool 中。  
&emsp; 线性预读（Linear read-ahead）：线性预读方式有一个很重要的变量 innodb_read_ahead_threshold，可以控制 Innodb 执行预读操作的触发阈值。如果一个 extent 中的被顺序读取的 page 超过或者等于该参数变量时，Innodb将会异步的将下一个 extent 读取到 buffer pool中，innodb_read_ahead_threshold 可以设置为0-64（一个 extend 上限就是64页）的任何值，默认值为56，值越高，访问模式检查越严格。  
&emsp; 随机预读（Random read-ahead）: 随机预读方式则是表示当同一个extent中的一些page在buffer pool中发现时，Innodb 会将该 extent 中的剩余page一并读到 buffer pool中，由于随机预读方式给Innodb code带来了一些不必要的复杂性，同时在性能也存在不稳定性，在5.5中已经将这种预读方式废弃。要启用此功能，请将配置变量设置 innodb_random_read_ahead 为ON。 

#### 1.1.1.2. LRU算法  
&emsp; InnoDB是以什么算法，来管理这些缓冲页呢？  
&emsp; memcache，OS都会用LRU来进行页置换管理，但MySQL并没有直接使用LRU算法。  

&emsp; 传统的LRU是如何进行缓冲页管理？  
&emsp; 最常见的是，把入缓冲池的页放到LRU的头部，作为最近访问的元素，从而最晚被淘汰。这里又分两种情况：  
&emsp; （1）页已经在缓冲池里，那就只做“移至”LRU头部的动作，而没有页被淘汰；  
&emsp; （2）页不在缓冲池里，除了做“放入”LRU头部的动作，还要做“淘汰”LRU尾部页的动作；  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-106.png)  
&emsp; 如上图，假如管理缓冲池的LRU长度为10，缓冲了页号为1，3，5…，40，7的页。  
&emsp; 假如，接下来要访问的数据在页号为4的页中：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-107.png)  
&emsp; （1）页号为4的页，本来就在缓冲池里；  
&emsp; （2）把页号为4的页，放到LRU的头部即可，没有页被淘汰；  
&emsp; 假如，再接下来要访问的数据在页号为50的页中：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-108.png)  
&emsp; （1）页号为50的页，原来不在缓冲池里；  
&emsp; （2）把页号为50的页，放到LRU头部，同时淘汰尾部页号为7的页；  
&emsp; 传统的LRU缓冲池算法十分直观，OS，memcache等很多软件都在用，MySQL为什么不能直接用呢？  
&emsp; 这里有两个问题：  
&emsp; （1）预读失效；  
&emsp; （2）缓冲池污染；  

##### 1.1.1.2.1. 预读失效  
&emsp; 什么是预读失效？  
&emsp; <font color = "lime">由于预读(Read-Ahead)，提前把页放入了缓冲池，但最终MySQL并没有从页中读取数据，称为预读失效。</font>  

&emsp; 如何对预读失效进行优化？  
&emsp; 要优化预读失效，思路是：  
&emsp; （1）让预读失败的页，停留在缓冲池LRU里的时间尽可能短；  
&emsp; （2）让真正被读取的页，才挪到缓冲池LRU的头部；  
&emsp; 以保证，真正被读取的热数据留在缓冲池里的时间尽可能长。  

&emsp; **<font color = "lime">预读失效进行优化的具体方法是：</font>**  
1. 将LRU分为两个部分：新生代(new sublist) 和 老生代(old sublist)。  
2. **<font color = "red">新老生首尾相连，即：新生代的尾(tail)连接着老生代的头(head)；</font>**  
3. 新页（例如被预读的页）加入缓冲池时，只加入到老生代头部：  
    * **<font color = "red">如果数据真正被读取（预读成功），才会加入到新生代的头部</font>**  
    * **<font color = "red">如果数据没有被读取，则会比新生代里的“热数据页”更早被淘汰出缓冲池</font>**   

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-109.png)  
&emsp; 举个例子，整个缓冲池LRU如上图：  
&emsp; （1）整个LRU长度是10；  
&emsp; （2）前70%是新生代；  
&emsp; （3）后30%是老生代；  
&emsp; （4）新老生代首尾相连；  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-110.png)  
&emsp; 假如有一个页号为50的新页被预读加入缓冲池：  
&emsp; （1）50只会从老生代头部插入，老生代尾部（也是整体尾部）的页会被淘汰掉；  
&emsp; （2）假设50这一页不会被真正读取，即预读失败，它将比新生代的数据更早淘汰出缓冲池；  
&emsp; 改进版缓冲池LRU能够很好的解决“预读失败”的问题。不要因为害怕预读失败而取消预读策略，大部分情况下，局部性原理是成立的，预读是有效的。  
&emsp; 新老生代改进版LRU仍然解决不了缓冲池污染的问题。  

##### 1.1.1.2.2. 缓冲池污染  
&emsp; <font color = "red">当某一个SQL语句，要批量扫描大量数据时(例如like语句)，可能导致把缓冲池的所有页都替换出去，导致大量热数据被换出，MySQL性能急剧下降，这种情况叫缓冲池污染。</font>  
&emsp; 例如，有一个数据量较大的用户表，当执行：  
&emsp; select * from user where name like "%shenjian%";  
&emsp; 虽然结果集可能只有少量数据，但这类like不能命中索引，必须全表扫描，就需要访问大量的页：  
&emsp; （1）把页加到缓冲池（插入老生代头部）；  
&emsp; （2）从页里读出相关的row（插入新生代头部）；  
&emsp; （3）row里的name字段和字符串shenjian进行比较，如果符合条件，加入到结果集中；  
&emsp; （4）…直到扫描完所有页中的所有row…  

&emsp; 如此一来，所有的数据页都会被加载到新生代的头部，但只会访问一次，真正的热数据被大量换出。  
&emsp; <font color = "red">怎么解决这类扫码读取大量数据导致的缓冲池污染问题呢？</font>  
&emsp; **<font color = "lime">MySQL缓冲池加入了一个“老生代停留时间窗口”的机制：</font>**  
1. 假设T=老生代停留时间窗口；  
2. <font color = "red">插入老生代头部的页，即使立刻被访问，并不会立刻放入新生代头部；</font>  
3. <font color = "red">只有满足“被访问”并且“在老生代停留时间”大于T，才会被放入新生代头部；</font>  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-111.png)  
&emsp; 继续举例，假如批量数据扫描，有51，52，53，54，55等五个页面将要依次被访问。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-112.png)  
&emsp; 如果没有“老生代停留时间窗口”的策略，这些批量被访问的页面，会换出大量热数据。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-113.png)  
&emsp; 加入“老生代停留时间窗口”策略后，短时间内被大量加载的页，并不会立刻插入新生代头部，而是优先淘汰那些，短期内仅仅访问了一次的页。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-114.png)  
&emsp; 而只有在老生代呆的时间足够久，停留时间大于T，才会被插入新生代头部。  

#### 1.1.1.3. 相关参数  
&emsp; 有三个比较重要的参数。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-105.png)  
&emsp; 参数：innodb_buffer_pool_size  
&emsp; 介绍：配置缓冲池的大小，在内存允许的情况下，DBA往往会建议调大这个参数，越多数据和索引放到内存里，数据库的性能会越好。  

&emsp; 参数：innodb_old_blocks_pct  
&emsp; 介绍：老生代占整个LRU链长度的比例，默认是37，即整个LRU中新生代与老生代长度比例是63:37。如果把这个参数设为100，就退化为普通LRU了。  

&emsp; 参数：innodb_old_blocks_time  
&emsp; 介绍：老生代停留时间窗口，单位是毫秒，默认是1000，即同时满足“被访问”与“在老生代停留时间超过1秒”两个条件，才会被插入到新生代头部。  

#### 1.1.1.4. 总结  
1. 缓冲池(buffer pool)是一种常见的降低磁盘访问的机制；  
2. 缓冲池通常以页(page)为单位缓存数据；  
3. 缓冲池的常见管理算法是LRU，memcache，OS，InnoDB都使用了这种算法；  
4. **<font color = "lime">InnoDB对普通LRU进行了优化：</font>**  
    * **<font color = "lime">将缓冲池分为老生代和新生代，入缓冲池的页，优先进入老生代，页被访问，才进入新生代，以解决预读失效的问题</font>**  
    * **<font color = "lime">页被访问，且在老生代停留时间超过配置阈值的，才进入新生代，以解决批量数据访问，大量热数据淘汰的问题</font>**  

### 1.1.2. 写缓冲(change buffer)
&emsp; **<font color = "lime">一句话概述：当使用普通索引，修改数据时，没有命中缓冲池时，会使用到写缓冲（写缓冲是缓冲池的一部分）。在写缓冲中记录这个操作，一次内存操作；写入redo log，一次磁盘顺序写操作。</font>**

<!--
https://www.cnblogs.com/wangchunli-blogs/p/10416046.html
https://mp.weixin.qq.com/s/PF21mUtpM8-pcEhDN4dOIw
-->

<!-- 
&emsp; <font color = "red">InnoDB存储引擎开创性地设计了插入缓冲，</font>对于非聚集索引的插入或更新操作，<font color = "red">不是每一次直接插入索引页中，而是先判断插入的非聚集索引页是否在缓冲池中。如果在，则直接插入；如果不在，则先放入一个插入缓冲区中，</font>好似欺骗数据库这个非聚集的索引已经插到叶子节点了，<font color = "red">然后再以一定的频率执行插入缓冲和非聚集索引页子节点的合并操作，</font>这时通常能将多个插入合并到一个操作中（因为在一个索引页中），这就大大提高了对非聚集索引执行插入和修改操作的性能。
 
-->

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
&emsp; 可以看到，40这一页，在真正被读取时，才会被加载到缓冲池中。  

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


### 1.1.3. 两次写  
<!-- 
 double write buffer，你居然没听过？ 
 https://mp.weixin.qq.com/s/bkoQ9g4cIcFFZBnpVh8ERQ
-->

<!-- 

脏页刷盘风险：InnoDB 的 page size一般是16KB，操作系统写文件是以4KB作为单位，那么每写一个 InnoDB 的 page 到磁盘上，操作系统需要写4个块。于是可能出现16K的数据，写入4K 时，发生了系统断电或系统崩溃，只有一部分写是成功的，这就是 partial page write（部分页写入）问题。这时会出现数据不完整的问题。
这时是无法通过 redo log 恢复的，因为 redo log 记录的是对页的物理修改，如果页本身已经损坏，重做日志也无能为力。

doublewrite 就是用来解决该问题的。doublewrite 由两部分组成，一部分为内存中的 doublewrite buffer，其大小为2MB，另一部分是磁盘上共享表空间中连续的128个页，即2个区(extent)，大小也是2M。
为了解决 partial page write 问题，当 MySQL 将脏数据刷新到磁盘的时候，会进行以下操作：
1）先将脏数据复制到内存中的 doublewrite buffer
2）之后通过 doublewrite buffer 再分2次，每次1MB写入到共享表空间的磁盘上（顺序写，性能很高）
3）完成第二步之后，马上调用 fsync 函数，将doublewrite buffer中的脏页数据写入实际的各个表空间文件（离散写）。

如果操作系统在将页写入磁盘的过程中发生崩溃，InnoDB 再次启动后，发现了一个 page 数据已经损坏，InnoDB 存储引擎可以从共享表空间的 doublewrite 中找到该页的一个最近的副本，用于进行数据恢复了。


-->

#### 1.1.3.1. 部分写失效  
 
&emsp; <font color = "lime">如果说写缓冲带给InnoDB存储引擎的是性能，那么两次写带给InnoDB存储引擎的是数据的可靠性。</font><font color = "red">当数据库宕机时，可能发生数据库正在写一个页面，而这个页只写了一部分（比如16K的页，只写前4K的页）的情况，称之为部分写失效（partial page write）。</font>在InnoDB存储引擎未使用double write技术前，曾出现过因为部分写失效而导致数据丢失的情况。  

&emsp; MySQL的buffer一页的大小是16K，文件系统一页的大小是4K，也就是说，MySQL将buffer中一页数据刷入磁盘，要写4个文件系统里的页。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-118.png)  
&emsp; 如上图所示，MySQL里page=1的页，物理上对应磁盘上的1+2+3+4四个格。  
&emsp; 那么，问题来了，这个操作并非原子，如果执行到一半断电，会不会出现问题呢？  
&emsp; 会，这就是所谓的“页数据损坏”。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-120.png)  
&emsp; 如上图所示，MySQL内page=1的页准备刷入磁盘，才刷了3个文件系统里的页，掉电了，则会出现：重启后，page=1的页，物理上对应磁盘上的1+2+3+4四个格，数据完整性被破坏。  

&emsp; 有人也许会想，如果发生写失效，可以通过重做日志进行恢复。这是一个办法。但是必须清楚的是，重做日志中记录的是对页的物理操作，如偏移量800，写'aaaa'记录。如果这个页本身已经损坏，再对其进行重做是没有意义的。这就是说，**<font color = "lime">在应用（apply）重做日志前，需要一个页的副本，当写入失效发生时，先通过页的副本来还原该页，再进行重做，这就是doublewrite。即doublewrite是页的副本。</font>**  

#### 1.1.3.2. doublewrite架构及流程
**doublewrite：**  
&emsp; InnoDB存储引擎doublewrite的体系架构如下图所示  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-90.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-117.png)  
&emsp; <font color = "red">doublewrite分为内存和磁盘的两层架构：</font>一部分是内存中的doublewrite buffer，大小为2MB；另一部分是物理磁盘上共享表空间中连续的128个页，即两个区（extent），大小同样为2MB(页的副本)。    

&emsp; 如上图所示，当有页数据要刷盘时：  
1. 第一步：页数据先memcopy到DWB的内存里；  
2. 第二步：DWB的内存里，会先刷到DWB的磁盘上；  
3. 第三步：DWB的内存里，再刷到数据磁盘存储上；  

&emsp; **DWB为什么能解决“页数据损坏”问题呢？**  
&emsp; 假设步骤2掉电，磁盘里依然是1+2+3+4的完整数据。  
&emsp; &emsp; 只要有页数据完整，就能通过redo还原数据。  
&emsp; 假如步骤3掉电，DWB里存储着完整的数据。  
&emsp; 所以，一定不会出现“页数据损坏”问题。  
&emsp; &emsp; 写了2次，总有一个地方的数据是OK的。  

<!-- 
**<font color = "lime">1. 当缓冲池的脏页刷新时，并不直接写磁盘，而是会通过memcpy函数将脏页先拷贝到内存中的doublewrite buffer，</font>**     
2. 之后通过doublewrite buffer再分两次，每次写入1MB到共享表空间的物理磁盘上，  
3. 然后马上调用fsync函数，同步磁盘，避免缓冲写带来的问题。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-115.png)  

&emsp; 再看redo log写入关系，可以用下图演示  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-116.png)  
-->

#### 1.1.3.3. 性能  
&emsp; 能够通过DWB保证页数据的完整性，但毕竟DWB要写两次磁盘，**<font color = "red">会不会导致数据库性能急剧降低呢？</font>**  
&emsp; 在这个过程中，因为doublewrite页是连续的，因此这个过程是顺序写的，开销并不是很大。在完成doublewrite页的写入后，再将doublewrite buffer中的页写入各个表空间文件中，此时的写入则是离散的。  

&emsp; 分析DWB执行的三个步骤：  
1. 第一步，页数据memcopy到DWB的内存，速度很快；  
2. 第二步，DWB的内存fsync刷到DWB的磁盘，属于<font color = "red">顺序追加写，</font>速度也很快；  
3. 第三步，刷磁盘，随机写，本来就需要进行，不属于额外操作；  
 
&emsp; 另外，128页（每页16K）2M的DWB，会分两次刷入磁盘，每次最多64页，即1M的数据，执行也是非常之快的。  
&emsp; 综上，性能会有所影响，但影响并不大。

#### 1.1.3.4. 相关参数
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

&emsp; 参数skip_innodb_doublewrite可以禁止使用两次写功能，这时可能会发生前面提及的写失效问题。不过，如果有多台从服务器（slave server），需要提供较快的性能（如slave上做的是RAID0），也许启用这个参数是一个办法。不过，在需要提供数据高可靠性的主服务器（master server）上，任何时候都应确保开启两次写功能。  
&emsp; 注意：有些文件系统本身就提供了部分写失效的防范机制，如ZFS文件系统。在这种情况下，就不要启用doublewrite了。  

#### 1.1.3.5. 总结
&emsp; **总结：**  
&emsp; MySQL有很强的数据安全性机制：  
&emsp; （1）在异常崩溃时，如果不出现“页数据损坏”，能够通过redo恢复数据；  
&emsp; （2）在出现“页数据损坏”时，能够通过double write buffer恢复页数据；  
 
&emsp; double write buffer：  
&emsp; （1）不是一个内存buffer，是一个内存/磁盘两层的结构，是InnoDB里On-Disk架构里很重要的一部分；  
&emsp; （2）是一个通过写两次，保证页完整性的机制；  

### 1.1.4. 自适应哈希索引  
<!-- 
 InnoDB到底支不支持哈希索引，为啥不同的人说的不一样？ 
 https://mp.weixin.qq.com/s?__biz=MjM5ODYxMDA5OQ==&mid=2651962875&idx=1&sn=c6b3e7dc8a41609cfe070026bd27b71d&chksm=bd2d08278a5a813108b1f4116341ff31170574b9098e2708cbc212b008a1fac8dfd1ffeabc6b&scene=21#wechat_redirect
-->

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


