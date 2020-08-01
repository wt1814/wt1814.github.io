
<!-- TOC -->

- [1. Redis底层实现](#1-redis底层实现)
    - [1.1. 对象系统RedisObject](#11-对象系统redisobject)
    - [1.2. String内部编码](#12-string内部编码)
    - [1.3. Hash内部编码](#13-hash内部编码)
        - [1.3.1. hashtable（dict），字典](#131-hashtabledict字典)
        - [1.3.2. ziplist，压缩列表](#132-ziplist压缩列表)
    - [1.4. List内部编码](#14-list内部编码)
        - [1.4.1. linkedlist，双向链表](#141-linkedlist双向链表)
        - [1.4.2. quicklist，快速列表](#142-quicklist快速列表)
    - [1.5. Set内部编码](#15-set内部编码)
        - [1.5.1. inset](#151-inset)
    - [1.6. Zset内部编码](#16-zset内部编码)
        - [1.6.1. skiplist，跳跃表](#161-skiplist跳跃表)

<!-- /TOC -->

# 1. Redis底层实现  
<!-- 
万字长文的Redis五种数据结构详解（理论+实战），建议收藏。 
https://mp.weixin.qq.com/s/ipP35Zho9STAgu_lFT79rQ

https://mp.weixin.qq.com/s/zcWvzZTwUAm2NfAQhxMqeQ
-->

## 1.1. 对象系统RedisObject  
&emsp; **<font color = "lime">很重要的思想：redis设计比较复杂的对象系统，都是为了缩减内存占有！！！</font>**  
&emsp; Redis并没有直接使用数据结构来实现数据类型，而是基于这些数据结构创建了一个对象系统RedisObject，每个对象都使用到了至少一种底层数据结构。**<font color = "lime">Redis根据不同的使用场景和内容大小来判断对象使用哪种数据结构，从而优化对象在不同场景下的使用效率和内存占用。</font>**  

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
* Type：是对象类型，代表一个value对象具体是何种数据类型，包括REDISSTRING, REDISLIST, REDISHASH, REDISSET和REDIS_ZSET。  
* encoding是指对象使用的数据结构，是不同数据类型在redis内部的存储方式。<font color = "red">目前有8种数据结构：int、raw、embstr、ziplist、hashtable、quicklist、intset、skiplist。</font>  

&emsp; <font color = "lime">Redis数据类型的底层实现如下：</font>  
|Redis数据结构	|底层数据结构|
|---|---|
|String	|int、raw、embstr（即SDS）|
|Hash	|ziplist（压缩列表）或者hashtable（字典或者也叫哈希表）|
|List	|quicklist（快速列表，是ziplist压缩列表和linkedlist双端链表的组合）|
|Set	|intset（整数集合）或者hashtable（字典或者也叫哈希表）|
|ZSet	|ziplist（压缩列表）或者skiplist（跳跃表）|

## 1.2. String内部编码  
&emsp; **<font color = "red">字符串类型的内部编码有三种：</font>**  

*  int，存储 8 个字节的长整型（long，2^63-1）。   
*  embstr, 代表 embstr 格式的 SDS（Simple Dynamic String 简单动态字符串）， 存储小于 44 个字节的字符串。   
*  raw，存储大于 44 个字节的字符串（3.2 版本之前是 39 字节）。  

&emsp; <font color = "red">Redis会根据当前值的类型和长度决定使用哪种内部编码实现。</font>  

1. [SDS](/docs/microService/Redis/SDS.md)  

2. embstr 和 raw 的区别？  
&emsp; embstr 的使用只分配一次内存空间（因为 RedisObject 和 SDS 是连续的），而 raw 需要分配两次内存空间（分别为 RedisObject 和 SDS 分配空间）。 因此与 raw 相比，embstr 的好处在于创建时少分配一次空间，删除时少释放一次 空间，以及对象的所有数据连在一起，寻找方便。 而 embstr 的坏处也很明显，如果字符串的长度增加需要重新分配内存时，整个 RedisObject 和 SDS 都需要重新分配空间，因此 Redis 中的 embstr 实现为只读。  

3. int 和 embstr 什么时候转化为 raw?  
&emsp; 当 int 数 据 不 再 是 整 数 ， 或 大 小 超 过 了 long 的 范 围 （2^63-1=9223372036854775807）时，自动转化为 embstr。  

4. embstr没有超过阈值，为什么变成 raw 了？  
&emsp; 对于 embstr，由于其实现是只读的，因此在对 embstr 对象进行修改时，都会先 转化为 raw 再进行修改。 因此，只要是修改 embstr 对象，修改后的对象一定是 raw 的，无论是否达到了 44 个字节。  

5. 当长度小于阈值时，会还原吗？  
&emsp; 关于 Redis 内部编码的转换，都符合以下规律：编码转换在 Redis 写入数据时完 成，且转换过程不可逆，只能从小内存编码向大内存编码转换（但是不包括重新 set）。  

## 1.3. Hash内部编码  
&emsp; Redis 的 Hash 本身也是一个 KV 的结构，类似于 Java 中的 HashMap。外层的哈希（Redis KV 的实现）只用到了 hashtable。当存储 hash 数据类型时，把它叫做内层的哈希。内层的哈希底层可以使用两种数据结构实现：ziplist、hashtable，会由ziplist转换为hashtable。  


### 1.3.1. hashtable（dict），字典  
&emsp; 在 Redis 中，hashtable 被称为字典（dictionary），它是一个数组+链表的结构。Redis Hash使用MurmurHash2算法来计算键的哈希值，并且使用链地址法来解决键冲突，进行了一些rehash优化等。结构如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-4.png)  


&emsp; 字典类型的底层就是hashtable实现的，明白了字典的底层实现原理也就是明白了hashtable的实现原理，hashtable的实现原理可以与HashMap的是底层原理相类比。  
&emsp; 两者在新增时都会通过key计算出数组下标，不同的是计算法方式不同，HashMap中是以hash函数的方式，而hashtable中计算出hash值后，还要通过sizemask 属性和哈希值再次得到数组下标。  
hash表最大的问题就是hash冲突，为了解决hash冲突，假如hashtable中不同的key通过计算得到同一个index，就会形成单向链表（「链地址法」），如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-81.png)  

**rehash：**  
&emsp; 在字典的底层实现中，value对象以每一个dictEntry的对象进行存储，当hash表中的存放的键值对不断的增加或者减少时，需要对hash表进行一个扩展或者收缩。  
&emsp; 这里就会和HashMap一样也会就进行rehash操作，进行重新散列排布。从上图中可以看到有ht[0]和ht[1]两个对象，先来看看对象中的属性是干嘛用的。  
&emsp; 在hash表结构定义中有四个属性分别是dictEntry **table、unsigned long size、unsigned long sizemask、unsigned long used，分别表示的含义就是「哈希表数组、hash表大小、用于计算索引值，总是等于size-1、hash表中已有的节点数」。  
&emsp; ht[0]是用来最开始存储数据的，当要进行扩展或者收缩时，ht[0]的大小就决定了ht[1]的大小，ht[0]中的所有的键值对就会重新散列到ht[1]中。  
&emsp; 扩展操作：ht[1]扩展的大小是比当前 ht[0].used 值的二倍大的第一个 2 的整数幂；收缩操作：ht[0].used 的第一个大于等于的 2 的整数幂。  
&emsp; 当ht[0]上的所有的键值对都rehash到ht[1]中，会重新计算所有的数组下标值，当数据迁移完后ht[0]就会被释放，然后将ht[1]改为ht[0]，并新创建ht[1]，为下一次的扩展和收缩做准备。  

**渐进式rehash：**  
&emsp; 假如在rehash的过程中数据量非常大，Redis不是一次性把全部数据rehash成功，这样会导致Redis对外服务停止，Redis内部为了处理这种情况采用「渐进式的rehash」。  
&emsp; Redis将所有的rehash的操作分成多步进行，直到都rehash完成，具体的实现与对象中的rehashindex属性相关，「若是rehashindex 表示为-1表示没有rehash操作」。  
&emsp; 当rehash操作开始时会将该值改成0，在渐进式rehash的过程「更新、删除、查询会在ht[0]和ht[1]中都进行」，比如更新一个值先更新ht[0]，然后再更新ht[1]。  
&emsp; 而新增操作直接就新增到ht[1]表中，ht[0]不会新增任何的数据，这样保证「ht[0]只减不增，直到最后的某一个时刻变成空表」，这样rehash操作完成。  

### 1.3.2. ziplist，压缩列表  
&emsp; ziplist是一组连续内存块组成的顺序的数据结构，是一个经过特殊编码的双向链表，它不存储指向上一个链表节点和指向下一 个链表节点的指针，而是存储上一个节点长度和当前节点长度，通过牺牲部分读写性能，来换取高效的内存空间利用率，节省空间，是一种时间换空间的思想。只用在字段个数少，字段值小的场景面。  

&emsp; 什么时候使用 ziplist 存储？  
&emsp; 当 hash 对象同时满足以下两个条件的时候，使用 ziplist 编码：  
1. 所有的键值对的健和值的字符串长度都小于等于 64byte（一个英文字母 一个字节）；  
2. 哈希对象保存的键值对数量小于 512 个。  

&emsp; 一个哈希对象超过配置的阈值（键和值的长度有>64byte，键值对个数>512 个）时， 会转换成哈希表hashtable。  

&emsp; 压缩列表的内存结构图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-79.png)  
&emsp; 压缩列表中每一个节点表示的含义如下所示：
1. zlbytes：4个字节的大小，记录压缩列表占用内存的字节数。
2. zltail：4个字节大小，记录表尾节点距离起始地址的偏移量，用于快速定位到尾节点的地址。
3. zllen：2个字节的大小，记录压缩列表中的节点数。
4. entry：表示列表中的每一个节点。
5. zlend：表示压缩列表的特殊结束符号'0xFF'。

&emsp; 在压缩列表中每一个entry节点又有三部分组成，包括previous_entry_ength、encoding、content。  
1. previous_entry_ength表示前一个节点entry的长度，可用于计算前一个节点的其实地址，因为他们的地址是连续的。
2. encoding：这里保存的是content的内容类型和长度。
3. content：content保存的是每一个节点的内容。
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-80.png)  


## 1.4. List内部编码   
&emsp; Redis中的列表在3.2之前的版本是使用ziplist和linkedlist进行实现的，<font color = "red">数据量较小时用ziplist存储，达到临界值时转换为linkedlist进行存储，</font><font color = "lime">双向链表占用的内存比压缩列表的要多。</font>Redis3.2 版本之后，统一用quicklist来存储。   

### 1.4.1. linkedlist，双向链表  
&emsp; Redis的链表在双向链表上扩展了头、尾节点、元素数等属性。Redis的链表结构如下：
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-62.png)  

&emsp; Redis中链表的特性：  
1. 每一个节点都有指向前一个节点和后一个节点的指针。  
2. 头节点和尾节点的prev和next指针指向为null，所以链表是无环的。  
3. 链表有自己长度的信息，获取长度的时间复杂度为O(1)。  

<!-- 
&emsp; 从图中可以看出Redis的linkedlist双端链表有以下特性：节点带有prev、next指针、head指针和tail指针，获取前置节点、后置节点、表头节点和表尾节点的复杂度都是O(1)。len属性获取节点数量也为O(1)。 
-->
 

### 1.4.2. quicklist，快速列表
&emsp; quicklist（快速列表）是ziplist和linkedlist的结合体。quicklist 存储了一个双向链表，每个节点 都是一个ziplist。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-63.png)  

## 1.5. Set内部编码   
&emsp; Redis中列表和集合都可以用来存储字符串，但是「Set是不可重复的集合，而List列表可以存储相同的字符串」，「Set是一个特殊的value为空的Hash」，Set集合是无序的这个和后面讲的ZSet有序集合相对。  

&emsp; Redis 用intset或hashtable存储set。<font color = "red">如果元素都是整数类型，就用 inset 存储；如果不是整数类型，就用 hashtable。</font>  

&emsp; KV 怎么存储 set 的元素？  
&emsp; key 就是元素的值，value 为 null。  
&emsp; 如果元素个数超过 512 个，也会用 hashtable 存储。  

### 1.5.1. inset  
&emsp; inset的数据结构：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-7.png)  
&emsp; inset也叫做整数集合，用于保存整数值的数据结构类型，它可以保存int16_t、int32_t 或者int64_t 的整数值。  
&emsp; 在整数集合中，有三个属性值encoding、length、contents[]，分别表示编码方式、整数集合的长度、以及元素内容，length就是记录contents里面的大小。  

&emsp; 在整数集合新增元素的时候，若是超出了原集合的长度大小，就会对集合进行升级，具体的升级过程如下：  
1. 首先扩展底层数组的大小，并且数组的类型为新元素的类型。  
2. 然后将原来的数组中的元素转为新元素的类型，并放到扩展后数组对应的位置。  
3. 整数集合升级后就不会再降级，编码会一直保持升级后的状态。  


## 1.6. Zset内部编码   
&emsp; ZSet的底层实现是ziplist和skiplist实现的。同时满足以下条件时使用 ziplist 编码：1. 元素数量小于 128 个；2. 所有 member 的长度都小于 64 字节。超过阈值之后，使用 skiplist和hashtable存储。  

### 1.6.1. skiplist，跳跃表  
&emsp; skiplist也叫做「跳跃表」，跳跃表是一种有序的数据结构，它通过每一个节点维持多个指向其它节点的指针，从而达到快速访问的目的。  

&emsp; skiplist有如下几个特点：  
1. 有很多层组成，由上到下节点数逐渐密集，最上层的节点最稀疏，跨度也最大。  
2. 每一层都是一个有序链表，至少包含两个节点，头节点和尾节点。  
3. 每一层的每一个每一个节点都含有指向同一层下一个节点和下一层同一个位置节点的指针。  
4. 如果一个节点在某一层出现，那么该以下的所有链表同一个位置都会出现该节点。  

&emsp; 具体实现的结构图如下所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-82.png)  

&emsp; 在跳跃表的结构中有head和tail表示指向头节点和尾节点的指针，能快速的实现定位。level表示层数，len表示跳跃表的长度，BW表示后退指针，在从尾向前遍历的时候使用。  
&emsp; BW下面还有两个值分别表示分值（score）和成员对象（各个节点保存的成员对象）。  
&emsp; 跳跃表的实现中，除了最底层的一层保存的是原始链表的完整数据，上层的节点数会越来越少，并且跨度会越来越大。  
&emsp; 跳跃表的上面层就相当于索引层，都是为了找到最后的数据而服务的，数据量越大，条表所体现的查询的效率就越高，和平衡树的查询效率相差无几。  

&emsp; 如下图所示，红线是查找10的过程：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-83.png)  

&emsp; **<font color = "red">如何借助Sorted set实现多维排序? </font>** 
&emsp; Sorted Set默认情况下只能根据一个因子score进行排序。如此一来，局限性就很大，举个栗子：热门排行榜需要按照下载量&最近更新时间排序，即类似数据库中的ORDER BY download_count, update_time DESC。那这样的需求如果用Redis的Sorted Set实现呢？  
&emsp; 事实上很简单，思路就是<font color = "red">将涉及排序的多个维度的列通过一定的方式转换成一个特殊的列</font>，即result = function(x, y, z)，即x，y，z是三个排序因子，例如下载量、时间等，通过自定义函数function()计算得到result，将result作为Sorted Set中的score的值，就能实现任意维度的排序需求了。  



