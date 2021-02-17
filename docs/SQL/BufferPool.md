<!-- TOC -->

- [1. 缓冲池(buffer pool)](#1-缓冲池buffer-pool)
    - [1.1. 简介](#11-简介)
    - [1.2. 原理](#12-原理)
        - [1.2.1. 前言：预读](#121-前言预读)
        - [1.2.2. LRU算法](#122-lru算法)
            - [1.2.2.1. 预读失效](#1221-预读失效)
            - [1.2.2.2. 缓冲池污染](#1222-缓冲池污染)
    - [1.3. 相关参数](#13-相关参数)
    - [1.4. 总结](#14-总结)

<!-- /TOC -->

# 1. 缓冲池(buffer pool)  
<!-- 
什么是数据库的 “缓存池” ？
https://mp.weixin.qq.com/s/XbvSZ3X6c-tToe-FKsEafA

能说一说Mysql缓存池吗？ 
https://mp.weixin.qq.com/s/AbiYMbDUJsMWK-_g0mFNwg

了解InnoDB存储引擎的内存池 
https://mp.weixin.qq.com/s/Cdq5aVYXUGQqxUdsnLlA8w
MySQL 缓冲池 是什么？ 
https://mp.weixin.qq.com/s/rCTJDh3_WbEAbGLZdZiQzg
-->

## 1.1. 简介
<!-- 
&emsp; InnoDB 会将那些热点数据和一些 InnoDB 认为即将访问到的数据存在 Buffer Pool 中，以提升数据的读取性能。  
&emsp; InnoDB 在修改数据时，如果数据的页在 Buffer Pool 中，则会直接修改 Buffer Pool，此时称这个页为脏页，InnoDB 会以一定的频率将脏页刷新到磁盘，这样可以尽量减少磁盘I/O，提升性能。 
-->
&emsp; 缓冲池是主内存中的一个区域，在InnoDB访问表和索引数据时会在其中进行高速缓存。**在专用服务器上，通常将多达80％的物理内存分配给缓冲池。**  
&emsp; 为了提高大容量读取操作的效率，缓冲池被分为多个页面，这些页面可能包含多个行。为了提高缓存管理的效率，缓冲池被实现为页面的链接列表。使用LRU算法的变体将很少使用的数据从缓存中老化掉。  
&emsp; **缓冲池允许直接从内存中处理经常使用的数据，从而加快了处理速度。**  

* 读数据操作：
    * 首先将从磁盘读到的页存放在缓冲池中，这个过程称为将页“FIX”在缓冲池中
    * 下一次再读相同的页时，首先判断该页是否在缓冲池中
    * 若在缓冲池中，称该页在缓冲池中被命中，直接读取该页。否则，读取磁盘上的页
* 写数据操作：
    * 如果数据的页在 Buffer Pool 中，首先修改在缓冲池中的页，此时称这个页为脏页。然后再以一定的频率刷新到磁盘上
    * 页从缓冲池刷新回磁盘的操作并不是在每次页发生更新时触发
    * 通过一种称为Checkpoint的机制刷新回磁盘


## 1.2. 原理
&emsp; 如何管理与淘汰缓冲池，使得性能最大化呢？在介绍具体细节之前，先介绍下“预读”的概念。  
<!-- 
预读(read ahead)  
&emsp; InnoDB 在 I/O 的优化上有个比较重要的特性为预读，<font color = "red">当 InnoDB 预计某些 page 可能很快就会需要用到时，它会异步地将这些 page 提前读取到缓冲池(buffer pool)中，</font>这其实有点像空间局部性的概念。  
&emsp; 空间局部性(spatial locality)：如果一个数据项被访问，那么与它的址相邻的数据项也可能很快被访问。  
&emsp; InnoDB使用两种预读算法来提高I/O性能：线性预读(linear read-ahead)和随机预读(randomread-ahead)。  
&emsp; 其中，线性预读以 extent(块，1个 extent 等于64个 page)为单位，而随机预读放到以 extent 中的 page 为单位。线性预读着眼于将下一个extent 提前读取到 buffer pool 中，而随机预读着眼于将当前 extent 中的剩余的 page 提前读取到 buffer pool 中。  
&emsp; 线性预读(Linear read-ahead)：线性预读方式有一个很重要的变量 innodb_read_ahead_threshold，可以控制 Innodb 执行预读操作的触发阈值。如果一个 extent 中的被顺序读取的 page 超过或者等于该参数变量时，Innodb将会异步的将下一个 extent 读取到 buffer pool中，innodb_read_ahead_threshold 可以设置为0-64(一个 extend 上限就是64页)的任何值，默认值为56，值越高，访问模式检查越严格。  
&emsp; 随机预读(Random read-ahead): 随机预读方式则是表示当同一个 extent 中的一些 page 在 buffer pool 中发现时，Innodb 会将该 extent 中的剩余 page 一并读到 buffer pool中，由于随机预读方式给 Innodb code 带来了一些不必要的复杂性，同时在性能也存在不稳定性，在5.5中已经将这种预读方式废弃。要启用此功能，请将配置变量设置 innodb_random_read_ahead 为ON。  
-->

### 1.2.1. 前言：预读  
&emsp; 什么是预读？  
&emsp; 磁盘读写，并不是按需读取，而是按页读取， **<font color = "lime">一次至少读一页数据(一般是4K)，如果未来要读取的数据就在页中，就能够省去后续的磁盘IO，提高效率。</font>**  

&emsp; 预读为什么有效？  
&emsp; 数据访问，通常都遵循“集中读写”的原则，使用一些数据，大概率会使用附近的数据，这就是所谓的“局部性原理”，它表明提前加载是有效的，确实能够减少磁盘IO。  

&emsp; 按页(4K)读取，和InnoDB的缓冲池设计有什么关系？  
&emsp; (1)磁盘访问按页读取能够提高性能，所以缓冲池一般也是按页缓存数据；  
&emsp; (2) **<font color = "lime">预读机制能把一些“可能要访问”的页提前加入缓冲池，避免未来的磁盘IO操作；</font>**  

&emsp; InnoDB使用两种预读算法来提高I/O性能：线性预读(linear read-ahead)和随机预读(randomread-ahead)。其中，线性预读以 extent(块，1个 extent 等于64个 page)为单位，而随机预读放到以 extent 中的 page 为单位。线性预读着眼于将下一个extent 提前读取到 buffer pool 中，而随机预读着眼于将当前 extent 中的剩余的 page 提前读取到 buffer pool 中。  
&emsp; 线性预读(Linear read-ahead)：线性预读方式有一个很重要的变量 innodb_read_ahead_threshold，可以控制 Innodb 执行预读操作的触发阈值。如果一个 extent 中的被顺序读取的 page 超过或者等于该参数变量时，Innodb将会异步的将下一个 extent 读取到 buffer pool中，innodb_read_ahead_threshold 可以设置为0-64(一个 extend 上限就是64页)的任何值，默认值为56，值越高，访问模式检查越严格。  
&emsp; 随机预读(Random read-ahead): 随机预读方式则是表示当同一个extent中的一些page在buffer pool中发现时，Innodb 会将该 extent 中的剩余page一并读到 buffer pool中，由于随机预读方式给Innodb code带来了一些不必要的复杂性，同时在性能也存在不稳定性，在5.5中已经将这种预读方式废弃。要启用此功能，请将配置变量设置 innodb_random_read_ahead 为ON。 

### 1.2.2. LRU算法  
&emsp; InnoDB是以什么算法，来管理这些缓冲页呢？  
&emsp; memcache，OS都会用LRU来进行页置换管理，但MySQL并没有直接使用LRU算法。  

&emsp; 传统的LRU是如何进行缓冲页管理？  
&emsp; 最常见的是，把入缓冲池的页放到LRU的头部，作为最近访问的元素，从而最晚被淘汰。这里又分两种情况：  
&emsp; (1)页已经在缓冲池里，那就只做“移至”LRU头部的动作，而没有页被淘汰；  
&emsp; (2)页不在缓冲池里，除了做“放入”LRU头部的动作，还要做“淘汰”LRU尾部页的动作；  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-106.png)  
&emsp; 如上图，假如管理缓冲池的LRU长度为10，缓冲了页号为1，3，5…，40，7的页。  
&emsp; 假如，接下来要访问的数据在页号为4的页中：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-107.png)  
&emsp; (1)页号为4的页，本来就在缓冲池里；  
&emsp; (2)把页号为4的页，放到LRU的头部即可，没有页被淘汰；  
&emsp; 假如，再接下来要访问的数据在页号为50的页中：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-108.png)  
&emsp; (1)页号为50的页，原来不在缓冲池里；  
&emsp; (2)把页号为50的页，放到LRU头部，同时淘汰尾部页号为7的页；  
&emsp; 传统的LRU缓冲池算法十分直观，OS，memcache等很多软件都在用，MySQL为什么不能直接用呢？  
&emsp; 这里有两个问题：  
&emsp; (1)预读失效；  
&emsp; (2)缓冲池污染；  

#### 1.2.2.1. 预读失效  
&emsp; 什么是预读失效？  
&emsp; <font color = "lime">由于预读(Read-Ahead)，提前把页放入了缓冲池，但最终MySQL并没有从页中读取数据，称为预读失效。</font>  

&emsp; 如何对预读失效进行优化？  
&emsp; 要优化预读失效，思路是：  
&emsp; (1)让预读失败的页，停留在缓冲池LRU里的时间尽可能短；  
&emsp; (2)让真正被读取的页，才挪到缓冲池LRU的头部；  
&emsp; 以保证，真正被读取的热数据留在缓冲池里的时间尽可能长。  

&emsp; **<font color = "lime">预读失效进行优化的具体方法是：</font>**  
1. 将LRU分为两个部分：新生代(new sublist) 和 老生代(old sublist)。  
2. **<font color = "red">新老生首尾相连，即：新生代的尾(tail)连接着老生代的头(head)；</font>**  
3. 新页(例如被预读的页)加入缓冲池时，只加入到老生代头部：  
    * **<font color = "red">如果数据真正被读取(预读成功)，才会加入到新生代的头部</font>**  
    * **<font color = "red">如果数据没有被读取，则会比新生代里的“热数据页”更早被淘汰出缓冲池</font>**   

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-109.png)  
&emsp; 举个例子，整个缓冲池LRU如上图：  
&emsp; (1)整个LRU长度是10；  
&emsp; (2)前70%是新生代；  
&emsp; (3)后30%是老生代；  
&emsp; (4)新老生代首尾相连；  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-110.png)  
&emsp; 假如有一个页号为50的新页被预读加入缓冲池：  
&emsp; (1)50只会从老生代头部插入，老生代尾部(也是整体尾部)的页会被淘汰掉；  
&emsp; (2)假设50这一页不会被真正读取，即预读失败，它将比新生代的数据更早淘汰出缓冲池；  
&emsp; 改进版缓冲池LRU能够很好的解决“预读失败”的问题。不要因为害怕预读失败而取消预读策略，大部分情况下，局部性原理是成立的，预读是有效的。  
&emsp; 新老生代改进版LRU仍然解决不了缓冲池污染的问题。  

#### 1.2.2.2. 缓冲池污染  
&emsp; <font color = "red">当某一个SQL语句，要批量扫描大量数据时(例如like语句)，可能导致把缓冲池的所有页都替换出去，导致大量热数据被换出，MySQL性能急剧下降，这种情况叫缓冲池污染。</font>  
&emsp; 例如，有一个数据量较大的用户表，当执行：  

```sql
select * from user where name like "%shenjian%";  
```
&emsp; 虽然结果集可能只有少量数据，但这类like不能命中索引，必须全表扫描，就需要访问大量的页：  
&emsp; (1)把页加到缓冲池(插入老生代头部)；  
&emsp; (2)从页里读出相关的row(插入新生代头部)；  
&emsp; (3)row里的name字段和字符串shenjian进行比较，如果符合条件，加入到结果集中；  
&emsp; (4)…直到扫描完所有页中的所有row…  

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

## 1.3. 相关参数  
&emsp; 有三个比较重要的参数。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-105.png)  
&emsp; 参数：innodb_buffer_pool_size  
&emsp; 介绍：配置缓冲池的大小，在内存允许的情况下，DBA往往会建议调大这个参数，越多数据和索引放到内存里，数据库的性能会越好。  

&emsp; 参数：innodb_old_blocks_pct  
&emsp; 介绍：老生代占整个LRU链长度的比例，默认是37，即整个LRU中新生代与老生代长度比例是63:37。如果把这个参数设为100，就退化为普通LRU了。  

&emsp; 参数：innodb_old_blocks_time  
&emsp; 介绍：老生代停留时间窗口，单位是毫秒，默认是1000，即同时满足“被访问”与“在老生代停留时间超过1秒”两个条件，才会被插入到新生代头部。  

## 1.4. 总结  
1. 缓冲池(buffer pool)是一种常见的降低磁盘访问的机制；  
2. 缓冲池通常以页(page)为单位缓存数据；  
3. 缓冲池的常见管理算法是LRU，memcache，OS，InnoDB都使用了这种算法；  
4. **<font color = "lime">InnoDB对普通LRU进行了优化：</font>**  
    * **<font color = "lime">将缓冲池分为老生代和新生代，入缓冲池的页，优先进入老生代，页被访问，才进入新生代，以解决预读失效的问题</font>**  
    * **<font color = "lime">页被访问，且在老生代停留时间超过配置阈值的，才进入新生代，以解决批量数据访问，大量热数据淘汰的问题</font>**  
