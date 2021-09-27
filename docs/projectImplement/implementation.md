

<!-- TOC -->

- [1. 数据迁移](#1-数据迁移)
    - [1.1. 从未分库分表动态切换到分库分表](#11-从未分库分表动态切换到分库分表)
        - [1.1.1. 停机迁移方案](#111-停机迁移方案)
        - [1.1.2. 双写迁移方案](#112-双写迁移方案)
    - [1.2. MySql现有的未分区表进行分区](#12-mysql现有的未分区表进行分区)

<!-- /TOC -->


# 1. 数据迁移  

<!-- 
数据库迁移神器——Flyway 
https://mp.weixin.qq.com/s/QJFY46Ku66MwQwkb7AQpNQ
数据库迁移搞炸了！没用这款开源神器的锅？ 
https://mp.weixin.qq.com/s/_LJWWHk9E07i1hNQ_D4R9A
服务化带来的问题---之数据迁移经历 
https://mp.weixin.qq.com/s?__biz=MzU5MTIyODk1Mg==&mid=2247484005&amp;idx=1&amp;sn=8957f419dfcffc85523aadd0a496ebec&source=41#wechat_redirect

现在有一个未分库分表的系统，你怎么分？ 
https://mp.weixin.qq.com/s/gJy3N5UtSdb3FhJ9HUlO6A
-->
## 1.1. 从未分库分表动态切换到分库分表
&emsp; 现在有一个未分库分表的系统，未来要分库分表，如何设计才可以让系统从未分库分表**动态切换**到分库分表上？

* 停机迁移方案
* 双写迁移方案  

### 1.1.1. 停机迁移方案
&emsp; 先说一个最 low 的方案，就是很简单，大家伙儿凌晨 12 点开始运维，网站或者 app 挂个公告，说 0 点到早上 6 点进行运维，无法访问。  
&emsp; 接着到 0 点停机，系统停掉，没有流量写入了，此时老的单库单表数据库静止了。然后你之前得写好一个**导数的一次性工具**，此时直接跑起来，然后将单库单表的数据哗哗哗读出来，写到分库分表里面去。  
&emsp; 导数完了之后，就 ok 了，修改系统的数据库连接配置啥的，包括可能代码和 SQL 也许有修改，那你就用最新的代码，然后直接启动连到新的分库分表上去。  
&emsp; 验证一下，ok了，完美，大家伸个懒腰，看看看凌晨4点钟的北京夜景，打个滴滴回家吧。  
&emsp; 但是这个方案比较 low，谁都能干，我们来看看高大上一点的方案。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-127.png)  

### 1.1.2. 双写迁移方案
&emsp; 这个是常用的一种迁移方案，比较靠谱一些，不用停机，不用看北京凌晨 4 点的风景。  
&emsp; 简单来说，就是在线上系统里面，之前所有写库的地方，增删改操作， **<font color = "clime">除了对老库增删改，都加上对新库的增删改，这就是所谓的双写，</font><font color = "blue">同时写俩库，老库和新库。</font>**  
&emsp; 然后**系统部署**之后，新库数据差太远，用之前说的导数工具，跑起来读老库数据写新库，写的时候要根据 gmt_modified 这类字段判断这条数据最后修改的时间，除非是读出来的数据在新库里没有，或者是比新库的数据新才会写。简单来说，就是不允许用老数据覆盖新数据。  
&emsp; 导完一轮之后，有可能数据还是存在不一致，那么就程序自动做一轮校验，比对新老库每个表的每条数据，接着如果有不一样的，就针对那些不一样的，从老库读数据再次写。反复循环，直到两个库每个表的数据都完全一致为止。  
&emsp; 接着当数据完全一致了，就 ok 了，基于仅仅使用分库分表的最新代码，重新部署一次，不就仅仅基于分库分表在操作了么，还没有几个小时的停机时间，很稳。所以现在基本玩儿数据迁移之类的，都是这么干的。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-128.png)  

## 1.2. MySql现有的未分区表进行分区
<!-- 

https://blog.csdn.net/sunvince/article/details/7752662

Mysql 未分区表分区
https://www.cnblogs.com/shenqilun/articles/11136766.html

针对TIMESTAMP的分区方案
https://www.cnblogs.com/ivictor/p/5032793.html

如何对现有的未分区表进行分区
https://qastack.cn/dba/48011/how-to-partition-an-existing-non-partitioned-table

-->
&emsp; mysql在已有无分区表增加分区，mysql5.5才有，可以是innodb_file_per_table关闭状态。  
