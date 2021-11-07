
<!-- TOC -->

- [1. 数据结构](#1-数据结构)
    - [1.1. Redis源码阅读之环境搭建及准备](#11-redis源码阅读之环境搭建及准备)
    - [1.2. 对象系统RedisObject](#12-对象系统redisobject)
    - [1.3. 数据结构介绍](#13-数据结构介绍)
        - [1.3.1. SDS字符串](#131-sds字符串)
        - [1.3.2. 链表](#132-链表)
            - [1.3.2.1. 双端链表LinkedList](#1321-双端链表linkedlist)
            - [1.3.2.2. 压缩列表Ziplist](#1322-压缩列表ziplist)
            - [1.3.2.3. 快速列表Quicklist](#1323-快速列表quicklist)
        - [1.3.3. 字典Dictht](#133-字典dictht)
        - [1.3.4. 整数集合inset](#134-整数集合inset)
        - [1.3.5. 跳跃表SkipList](#135-跳跃表skiplist)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 很重要的思想：redis设计比较复杂的对象系统，都是为了缩减内存占有！！！  
2. redis底层8种数据结构：int、raw、embstr(SDS)、ziplist、hashtable、quicklist、intset、skiplist。  
3. 3种链表：  
    * 双端链表LinkedList  
        &emsp; Redis的链表在双向链表上扩展了头、尾节点、元素数等属性。Redis的链表结构如下：
        ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-62.png)  
    * 压缩列表Ziplist  
        &emsp; 在双端链表中，如果在一个链表节点中存储一个小数据，比如一个字节。那么对应的就要保存头节点，前后指针等额外的数据。这样就浪费了空间，同时由于反复申请与释放也容易导致内存碎片化。这样内存的使用效率就太低了。  
        &emsp; Redis设计了压缩列表  
        ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-110.png)  
        &emsp; ziplist是一组连续内存块组成的顺序的数据结构， **<font color = "red">是一个经过特殊编码的双向链表，它不存储指向上一个链表节点和指向下一个链表节点的指针，而是存储上一个节点长度和当前节点长度，通过牺牲部分读写性能，来换取高效的内存空间利用率，节省空间，是一种时间换空间的思想。</font>** 只用在字段个数少，字段值小的场景里。  
    * 快速列表Quicklist  
        QuickList其实就是结合了ZipList和LinkedList的优点设计出来的。quicklist存储了一个双向链表，每个节点都是一个ziplist。  
        ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-63.png)  
4. 整数集合inset  
&emsp; inset的数据结构：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-7.png)  
&emsp; inset也叫做整数集合，用于保存整数值的数据结构类型，它可以保存int16_t、int32_t 或者int64_t 的整数值。  
&emsp; 在整数集合中，有三个属性值encoding、length、contents[]，分别表示编码方式、整数集合的长度、以及元素内容，length就是记录contents里面的大小。  
5. 跳跃表SkipList  
&emsp; skiplist也叫做「跳跃表」，跳跃表是一种有序的数据结构，它通过每一个节点维持多个指向其它节点的指针，从而达到快速访问的目的。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-85.png)  
&emsp; SkipList分为两部分，dict部分是由字典实现，Zset部分使用跳跃表实现，从图中可以看出，dict和跳跃表都存储了数据，实际上dict和跳跃表最终使用指针都指向了同一份数据，即数据是被两部分共享的，为了方便表达将同一份数据展示在两个地方。  



# 1. 数据结构
<!--
https://pdai.tech/md/db/nosql-redis/db-redis-x-redis-ds.html

全面阐释Redis常见对象类型的底层数据结构 
https://mp.weixin.qq.com/s/QVxwJb6F99E17ZaGQlhVTQ

https://mp.weixin.qq.com/s/h9V2a-KMLB-eeYliUksYFw
-->

## 1.1. Redis源码阅读之环境搭建及准备  
&emsp; ......
<!-- 
Redis源码阅读之: 环境搭建及准备
https://blog.csdn.net/u014563989/article/details/81066074?utm_medium=distribute.pc_relevant_download.none-task-blog-baidujs-2.nonecase&depth_1-utm_source=distribute.pc_relevant_download.none-task-blog-baidujs-2.nonecase
-->

## 1.2. 对象系统RedisObject  
&emsp; **<font color = "clime">(很重要的思想：redis设计比较复杂的对象系统，都是为了缩减内存占有！！！)</font>**  
&emsp; Redis并没有直接使用数据结构来实现数据类型，而是基于这些数据结构创建了一个对象系统RedisObject，每个对象都使用到了至少一种底层数据结构。 **<font color = "clime">Redis根据不同的使用场景和内容大小来判断对象使用哪种数据结构，从而优化对象在不同场景下的使用效率和内存占用。</font>**   

<!-- 
Redis这样设计有两个好处：
可以偷偷的改进内部编码，而对外的数据结构和命令没有影响，这样一旦开发出更优秀的内部编码，无需改动对外数据结构和命令。
多种内部编码实现可以在不同场景下发挥各自的优势。例如ziplist比较节省内存，但是在列表元素比较多的情况下，性能会有所下降。这时候Redis会根据配置选项将列表类型的内部实现转换为linkedlist。
-->

&emsp; redisObject的源代码在redis.h中，使用c语言编写。redisObject结构的定义如下所示：  

```c
typedef struct redisObject {
    unsigned type:4;
    unsigned encoding:4;
    unsigned lru:LRU_BITS;
    int refcount;
    void *ptr;
} robj;
```
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-10.png)  
* Type：是对象类型，代表一个value对象具体是何种数据类型，包括REDISSTRING，REDISLIST，REDISHASH，REDISSET和REDIS_ZSET。  
* encoding是指对象使用的数据结构，是不同数据类型在redis内部的存储方式。<font color = "red">目前有8种数据结构：int、raw、embstr(SDS)、ziplist、hashtable、quicklist、intset、skiplist。</font>  

&emsp; **<font color = "clime">Redis数据类型的底层实现如下：</font>**  

|Redis数据结构|底层数据结构|
|---|---|
|String	|int、embstr(即SDS)、raw|
|Hash	|ziplist(压缩列表)或者dictht(字典)|
|List	|quicklist(快速列表，是ziplist压缩列表和linkedlist双端链表的组合)|
|Set	|intset(整数集合)或者dictht(字典)|
|ZSet	|skiplist(跳跃表)或者ziplist(压缩列表)|


    String和Hash都是存储的字符串，Hash由ziplist(压缩列表)或者dictht(字典)组成；  
    List，「有序」可重复集合，由ziplist压缩列表和linkedlist双端链表的组成，在 3.2 之后采用QuickList；  
    Set，无序不可重复集合，是特殊的Hash结构(value为null)，由intset(整数集合)或者dictht(字典)组成；
    ZSet，「有序」不可重复集合，由skiplist(跳跃表)或者ziplist(压缩列表)组成。  

## 1.3. 数据结构介绍  
<!-- 
https://mp.weixin.qq.com/s/PMGYoySBrOMVZvRZIyTwXg
-->
### 1.3.1. SDS字符串  
&emsp; [SDS](/docs/microService/Redis/SDS.md)  

### 1.3.2. 链表
#### 1.3.2.1. 双端链表LinkedList  
&emsp; Redis的链表在双向链表上扩展了头、尾节点、元素数等属性。Redis的链表结构如下：
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-62.png)  

&emsp; 各部分作用说明：  

* head：表示 List 的头结点；通过其可以找到 List 的头节点。  
* tail：表示 List 的尾节点；通过其可以找到 List 的尾节点。  
* len：表示 List 存储的元素个数。  
* dup：表示用于复制元素的函数。  
* free：表示用于释放元素的函数。  
* match：表示用于对比元素的函数。  

&emsp; Redis中链表的特性：  
1. 每一个节点都有指向前一个节点和后一个节点的指针。  
2. 头节点和尾节点的prev和next指针指向为null，所以链表是无环的。  
3. 链表有自己长度的信息，获取长度的时间复杂度为O(1)。  

<!-- 
&emsp; 从图中可以看出Redis的linkedlist双端链表有以下特性：节点带有prev、next指针、head指针和tail指针，获取前置节点、后置节点、表头节点和表尾节点的复杂度都是O(1)。len属性获取节点数量也为O(1)。 
-->

#### 1.3.2.2. 压缩列表Ziplist
&emsp; 在双端链表中，如果在一个链表节点中存储一个小数据，比如一个字节。那么对应的就要保存头节点，前后指针等额外的数据。这样就浪费了空间，同时由于反复申请与释放也容易导致内存碎片化。这样内存的使用效率就太低了。  
&emsp; Redis设计了压缩列表  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-110.png)  

&emsp; 它是经过特殊编码，专门为了提升内存使用效率设计的。所有的操作都是通过指针与解码出来的偏移量进行的。  
&emsp; 并且压缩列表的内存是连续分配的，遍历的速度很快。  

---
&emsp; ziplist是一组连续内存块组成的顺序的数据结构， **<font color = "red">是一个经过特殊编码的双向链表，它不存储指向上一个链表节点和指向下一个链表节点的指针，而是存储上一个节点长度和当前节点长度，通过牺牲部分读写性能，来换取高效的内存空间利用率，节省空间，是一种时间换空间的思想。</font>** 只用在字段个数少，字段值小的场景里。  

&emsp; 压缩列表的内存结构图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-79.png)  
&emsp; 从图中可以看出ZipList没有前后指针。压缩列表中每一个节点表示的含义如下所示：
1. zlbytes：4个字节的大小，记录压缩列表占用内存的字节数。
2. zltail：4个字节大小，记录表尾节点距离起始地址的偏移量，用于快速定位到尾节点的地址。
3. zllen：2个字节的大小，记录压缩列表中的节点数。
4. entry：表示列表中的每一个节点。
5. zlend：表示压缩列表的特殊结束符号'0xFF'。

&emsp; 在压缩列表中每一个entry节点又有三部分组成，包括previous_entry_ength、encoding、content。  
1. previous_entry_ength表示前一个节点entry的长度，可用于计算前一个节点的其实地址，因为它们的地址是连续的。  
2. encoding：这里保存的是content的内容类型和长度。  
3. content：content保存的是每一个节点的内容。  

&emsp; ZipList 的优缺点比较：  

* 优点：<font color = "red">内存地址连续，省去了每个元素的头尾节点指针占用的内存。</font>  
* 缺点：对于删除和插入操作比较可能会触发连锁更新反应，比如在 list 中间插入删除一个元素时，在插入或删除位置后面的元素可能都需要发生相应的移动操作。 

#### 1.3.2.3. 快速列表Quicklist
&emsp; 在 Redis3.2 版本之后，Redis集合采用了QuickList作为List的底层实现，QuickList其实就是结合了ZipList和LinkedList的优点设计出来的。quicklist存储了一个双向链表，每个节点都是一个ziplist。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-63.png)  

&emsp; 各部分作用说明：  

* 每个listNode 存储一个指向 ZipList 的指针，ZipList 用来真正存储元素的数据。
* ZipList 中存储的元素数据总大小超过 8kb(默认大小，通过 list-max-ziplist-size 参数可以进行配置)的时候，就会重新创建出来一个 ListNode 和 ZipList，然后将其通过指针关联起来。


### 1.3.3. 字典Dictht  
&emsp; [Dictht](/docs/microService/Redis/Dictht.md)  

### 1.3.4. 整数集合inset  
&emsp; inset的数据结构：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-7.png)  
&emsp; inset也叫做整数集合，用于保存整数值的数据结构类型，它可以保存int16_t、int32_t 或者int64_t 的整数值。  
&emsp; 在整数集合中，有三个属性值encoding、length、contents[]，分别表示编码方式、整数集合的长度、以及元素内容，length就是记录contents里面的大小。  

&emsp; 在整数集合新增元素的时候，若是超出了原集合的长度大小，就会对集合进行升级，具体的升级过程如下：  
1. 首先扩展底层数组的大小，并且数组的类型为新元素的类型。  
2. 然后将原来的数组中的元素转为新元素的类型，并放到扩展后数组对应的位置。  
3. 整数集合升级后就不会再降级，编码会一直保持升级后的状态。  

### 1.3.5. 跳跃表SkipList  
<!-- 
&emsp; 具体实现的结构图如下所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-82.png)  
-->
&emsp; skiplist也叫做「跳跃表」，跳跃表是一种有序的数据结构，它通过每一个节点维持多个指向其它节点的指针，从而达到快速访问的目的。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-85.png)  
&emsp; SkipList分为两部分，dict部分是由字典实现，Zset部分使用跳跃表实现，从图中可以看出，dict和跳跃表都存储了数据，实际上dict和跳跃表最终使用指针都指向了同一份数据，即数据是被两部分共享的，为了方便表达将同一份数据展示在两个地方。  

&emsp; 在跳跃表的结构中有head和tail表示指向头节点和尾节点的指针，能快速的实现定位。level表示层数，len表示跳跃表的长度，BW表示后退指针，在从尾向前遍历的时候使用。BW下面还有两个值分别表示分值(score)和成员对象(各个节点保存的成员对象)。  

&emsp; skiplist有如下几个特点：  
1. 有很多层组成，由上到下节点数逐渐密集，最上层的节点最稀疏，跨度也最大。  
2. 每一层都是一个有序链表，至少包含两个节点，头节点和尾节点。  
3. 每一层的每一个节点都含有指向同一层下一个节点和下一层同一个位置节点的指针。  
4. 如果一个节点在某一层出现，那么该以下的所有链表同一个位置都会出现该节点。  

&emsp; 跳跃表的上面层就相当于索引层，都是为了找到最后的数据而服务的，数据量越大，条表所体现的查询的效率就越高，和平衡树的查询效率相差无几。  

&emsp; 如下图所示，红线是查找10的过程：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-83.png)  

