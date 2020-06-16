---
title: Redis数据结构及API
date: 2020-05-15 00:00:00
tags:
    - Redis
---
<!-- TOC -->

- [1. Redis简介](#1-redis简介)
- [2. Redis的基本数据类型：](#2-redis的基本数据类型)
- [3. Redis的其他数据类型](#3-redis的其他数据类型)
    - [3.1. bitmaps位图，SETBIT命令](#31-bitmaps位图setbit命令)
    - [3.2. HyperLogLog](#32-hyperloglog)
    - [3.3. Geospatial，地理](#33-geospatial地理)
- [4. Redis的基础数据结构和对象系统](#4-redis的基础数据结构和对象系统)
    - [4.1. 六种基础数据结构](#41-六种基础数据结构)
        - [4.1.1. SDS动态字符串](#411-sds动态字符串)
        - [4.1.2. linkedlist双端链表](#412-linkedlist双端链表)
        - [4.1.3. Hash字典](#413-hash字典)
        - [4.1.4. skipList跳跃表](#414-skiplist跳跃表)
        - [4.1.5. intset整数集合](#415-intset整数集合)
        - [4.1.6. ziplist压缩列表](#416-ziplist压缩列表)
    - [4.2. 对象系统，redisObject及5种基本类型](#42-对象系统redisobject及5种基本类型)
        - [4.2.1. redisObject](#421-redisobject)
        - [4.2.2. String](#422-string)
        - [4.2.3. List](#423-list)
        - [4.2.4. Hash](#424-hash)
        - [4.2.5. Set](#425-set)
        - [4.2.6. ZSet](#426-zset)
- [5. Redis的API](#5-redis的api)
- [6. Redis命令参考](#6-redis命令参考)

<!-- /TOC -->

# 1. Redis简介  
&emsp; Redis是一个开源，内存存储的数据结构服务器，可用作数据库，高速缓存和消息队列代理。  
&emsp; 它支持字符串、哈希表、列表、集合、有序集合，位图，hyperloglogs等丰富的数据类型。  
&emsp; 提供Lua脚本、LRU收回、事务以及不同级别磁盘持久化功能。  
&emsp; 同时通过Redis Sentinel提供高可用，通过Redis Cluster提供自动分区。  

# 2. Redis的基本数据类型：  
&emsp; Redis属于\<key,value\>形式的数据结构。  
&emsp; Redis的key是字符串类型，但是key中不能包括边界字符，由于key不是binary safe的字符串，所以key不能空格和换行。  
&emsp; Redis的value支持五种数据类型：String（字符串），Hash（哈希），List（列表），Set（集合）及Zset(sorted set，有序集合)。每个数据类型最多能处理2^32个key。  
&emsp; 注：Zset每个元素都会关联一个double类型的分数score。redis通过分数score来为Zset的成员进行从小到大的排序。Zset用来保存需要排序的数据。

|数据类型	|可以存储的值	|操作	|使用场景|
|---|---|---|---|
|String	|字符串、整数或者浮点数	|对整个字符串或者字符串的其中一部分执行操作；对整数和浮点数执行自增或者自减操作；	|1.缓存功能，如存放序列化后的用户信息 <br/>2.计数 <br/>3.共享session <br/>4.限速，如限制用户每分钟获取验证码的速率|
|Hash	|键值对(无序散列表)	|添加、获取、移除单个键值对；获取所有键值对；检查某个键是否存在；|	1.缓存功能，如存放用户信息，相较String可减少内存空间使用|
|List	|链表 |从两端压入或者弹出元素；读取单个或者多个元素；进行修剪，只保留一个范围内对元素；	|1.消息队列，lpush+brpop实现阻塞队列<br/> 2.文章列表 <br/>3.栈：lpush+lpop = Stack <br/>4.队列：lpush+lpop = Queue|
|Set	|无序集合|添加、获取、移除单个元素； 检查一个元素是否存在于集合中； 计算交集、并集、差集；从集合里面随机获取元素；|	1.标签(Tag) <br/>2.社交|
|Zset	|有序集合 | 添加、获取、删除元素；根据分值范围或者成员来获取元素； 计算一个键对排名；|1.排行榜系统，比如点赞排名 <br/>2.社交|

-----------
# 3. Redis的其他数据类型  
&emsp; bitmaps、HyperLogLog、geo  

## 3.1. bitmaps位图，SETBIT命令
&emsp; bitmaps位图为一个以位为单位的数组，数组的每个单元只能存储0和1。  
&emsp; bit操作被分为两组：  
* 恒定时间的单个bit操作，例如把某个bit设置为0或者1。或者获取某bit的值。  
* 对一组bit的操作。例如给定范围内bit统计（例如人口统计）。  

&emsp; Bitmaps的最大优点就是存储信息时可以节省大量的空间。例如在一个系统中，不同的用户被一个增长的用户ID表示。40亿（2^32=4*1024*1024*1024≈40亿）用户只需要512M内存就能记住某种信息，例如用户是否登录过。  
&emsp; 使用场景：  
* 各种实时分析；  
*  存储与对象ID关联的布尔信息。用来实现布隆过滤器（BloomFilter）；  
&emsp; 例如，记录访问网站的用户的最长连续时间。开始计算从0开始的天数，就是网站公开的那天，每次用户访问网站时通过SETBIT命令设置bit为1，可以简单的用当前时间减去初始时间并除以3600*24（结果就是网站公开的第几天）当做这个bit的位置。  
&emsp; 这种方法对于每个用户，都有存储每天的访问信息的一个很小的string字符串。通过BITCOUN就能轻易统计某个用户历史访问网站的天数。另外通过调用BITPOS命令，或者客户端获取并分析这个bitmap，就能计算出最长停留时间。  

&emsp; Bits命令：Bits设置和获取通过SETBIT和GETBIT命令。用法如下：  

        SETBIT key offset value  
        GETBIT key offset  

&emsp; 使用实例：  

    127.0.0.1:6380> setbit dupcheck 10 1  
    (integer) 0  
    127.0.0.1:6380> getbit dupcheck 10   
    (integer) 1  

* SETBIT命令第一个参数是位编号，第二个参数是这个位的值，只能是0或者1。如果bit地址超过当前string长度，会自动增大string。  

* GETBIT命令指示返回指定位置bit的值。超过范围（寻址地址在目标key的string长度以外的位）的GETBIT总是返回0。三个操作bits组的命令如下：  
    * BITOP执行两个不同string的位操作.，包括AND，OR，XOR和NOT。
    * BITCOUNT统计位的值为1的数量。
    * BITPOS寻址第一个为0或者1的bit的位置（寻址第一个为1的bit的位置：bitpos dupcheck 1；寻址第一个为0的bit的位置：bitpos dupcheck 0）。  

## 3.2. HyperLogLog  
&emsp; Hyper指的是超级。Hyperloglog供不精确的去重计数功能，适于做大规模数据的去重统计，例如统计 UV。  

    网页流量统计里的UV，pv：
    UV指的是独立访客。
    pv是page view的缩写，即页面浏览量。

&emsp; 优点：占用内存极小，对于一个key，只需要12kb。  
&emsp; 缺点：查询指定用户的时候，可能会出错，毕竟存的不是具体的数据。总数也存在一定的误差。  
&emsp; Redis Hyperloglog的三个命令：PFADD、PFCOUNT、PFMERGE。  
* PFADD命令用于添加一个新元素到统计中。  
* PFCOUNT命令用于获取到目前为止通过PFADD命令添加的唯一元素个数近似值。  
* PFMERGE命令执行多个HLL之间的联合操作。  

        127.0.0.1:6380> PFADD hll a b c d d c
        (integer) 1
        127.0.0.1:6380> PFCOUNT hll
        (integer) 4
        127.0.0.1:6380> PFADD hll e
        (integer) 1
        127.0.0.1:6380> PFCOUNT hll
        (integer) 5

## 3.3. Geospatial，地理
&emsp; 可以用来保存地理位置，并作位置距离计算或者根据半径计算位置等。主要有以下六组命令：  
* geoadd：增加某个地理位置的坐标。  
* geopos：获取某个地理位置的坐标。  
* geodist：获取两个地理位置的距离。  
* georadius：根据给定地理位置坐标获取指定范围内的地理位置集合。  
* georadiusbymember：根据给定地理位置获取指定范围内的地理位置集合。  
* geohash：获取某个地理位置的geohash值。  

--------------
# 4. Redis的基础数据结构和对象系统  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-1.png)  
* 首先介绍六种基础数据结构：动态字符串，链表，字典，跳跃表，整数集合和压缩列表。  
* 其次介绍Redis的对象系统中的字符串对象（String）、列表对象（List）、哈希对象（Hash）、集合对象（Set）和有序集合对象（ZSet）。  
* 最后介绍Redis的键空间和过期键( expire )实现。  

## 4.1. 六种基础数据结构  

### 4.1.1. SDS动态字符串  
&emsp; Redis使用动态字符串SDS来表示字符串值。 SDS结构：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-2.png)  
* len: 表示字符串的真正长度（不包含NULL结束符在内）。  
* alloc: 表示字符串的最大容量（不包含最后多余的那个字节）。  
* flags: 总是占用一个字节。其中的最低3个bit用来表示header的类型。  
* buf: 字符数组。  
 
```
//结构源码
struct sdshdr{
    //记录buf数组中已使用字节的数量
    //等于 SDS 保存字符串的长度
    int len;
    //记录 buf 数组中未使用字节的数量
    int free;
    //字节数组，用于保存字符串
    char buf[];
}
```

&emsp; 它的优点：  
* 开发者不用担心字符串变更造成的内存溢出问题。  
* 常数时间复杂度获取字符串长度len字段。  
* 空间预分配free字段，会默认留够一定的空间防止多次重分配内存。  

&emsp; SDS的结构可以减少修改字符串时带来的内存重分配的次数，这依赖于内存预分配和惰性空间释放两大机制。  
&emsp; 当SDS需要被修改，并且要对SDS进行空间扩展时，Redis不仅会为SDS分配修改所必须要的空间，还会为SDS分配额外的未使用的空间。  
* 如果修改后，SDS的长度(也就是len属性的值)将小于1MB，那么Redis预分配和len属性相同大小的未使用空间。  
* 如果修改后，SDS的长度将大于1MB，那么Redis会分配1MB的未使用空间。  
&emsp; 比如说，进行修改后SDS的len长度为20字节，小于1MB，那么Redis会预先再分配20字节的空间， SDS的buf数组的实际长度(除去最后一字节)变为20 + 20 = 40字节。当SDS的len长度大于1MB时，则只会再多分配1MB的空间。  
&emsp; 类似的，当SDS缩短其保存的字符串长度时，并不会立即释放多出来的字节，而是等待之后使用。  

### 4.1.2. linkedlist双端链表  
&emsp; Redis的链表在双向链表上扩展了头、尾节点、元素数等属性。Redis的链表结构如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-3.png)  
&emsp; 从图中可以看出Redis的linkedlist双端链表有以下特性：节点带有prev、next指针、head指针和tail指针，获取前置节点、后置节点、表头节点和表尾节点的复杂度都是O（1）。len属性获取节点数量也为O（1）。  
&emsp; 与双端链表相比，压缩列表可以节省内存空间，但是进行修改或增删操作时，复杂度较高；因此当节点数量较少时，可以使用压缩列表；但是节点数量多时，还是使用双端链表划算。  

&emsp; Redis的链表结构的dup、free和match成员属性是用于实现多态链表所需的类型特定函数：  
* dup函数用于复制链表节点所保存的值，用于深度拷贝。  
* free函数用于释放链表节点所保存的值。  
* match函数则用于对比链表节点所保存的值和另一个输入值是否相等。  

&emsp; ListNode节点数据结构：  

```
typedef  struct listNode{
    //前置节点
    struct listNode *prev;
    //后置节点
    struct listNode *next;
    //节点的值
    void *value;
}listNode
```
&emsp; 链表数据结构：  

```
typedef struct list{
    //表头节点
    listNode *head;
    //表尾节点
    listNode *tail;
    //链表所包含的节点数量
    unsigned long len;
    //节点值复制函数
    void (*free) (void *ptr);
    //节点值释放函数
    void (*free) (void *ptr);
    //节点值对比函数
    int (*match) (void *ptr,void *key);
}list;
```

### 4.1.3. Hash字典  
&emsp; Hash结构图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-4.png)  
&emsp; Redis的Hash，是在数组+链表的基础上，进行了一些rehash优化等。Redis Hash使用MurmurHash2算法来计算键的哈希值，并且使用链地址法来解决键冲突，被分配到同一个索引的多个键值对会连接成一个单向链表。  

&emsp; 数据结构源码：  
&emsp; 哈希表：  

```
typedef struct dictht {
    // 哈希表数组
    dictEntry **table;
    // 哈希表大小
    unsigned long size;
    // 哈希表大小掩码，用于计算索引值
    // 总是等于 size - 1
    unsigned long sizemask;
    // 该哈希表已有节点的数量
    unsigned long used;
} dictht; 
```
&emsp; Hash表节点：  

```
typedef struct dictEntry {
    // 键
    void *key;
    // 值
    union {
    void *val;
    uint64_t u64;
    int64_t s64;
    } v;
    // 指向下个哈希表节点，形成链表
    struct dictEntry *next;  // 单链表结构
} dictEntry;
```
字典：  
```
typedef struct dict {
    // 类型特定函数
    dictType *type;
    // 私有数据
    void *privdata;
    // 哈希表
    dictht ht[2];
    // rehash 索引
    // 当 rehash 不在进行时，值为 -1
    int rehashidx; /* rehashing not in progress if rehashidx == -1 */
} dict;
```

### 4.1.4. skipList跳跃表  
&emsp; Redis使用跳跃表作为有序集合对象的底层实现之一。它以有序的方式在层次化的链表中保存元素，效率和平衡树媲美 —— 查找、删除、添加等操作都可以在对数期望时间下完成，并且比起平衡树来说，跳跃表的实现要简单直观得多。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-5.png)  
&emsp; 如示意图所示，zskiplistNode是跳跃表的节点，其ele是保持的元素值，score 是分值，节点按照其score值进行有序排列，而level数组就是其所谓的层次化链表的体现。  
&emsp; 每个node的level数组大小都不同，level数组中的值是指向下一个node的指针和跨度值 (span)，跨度值是两个节点的score的差值。越高层的level数组值的跨度值就越大，底层的level数组值的跨度值越小。    
&emsp; level数组就像是不同刻度的尺子。度量长度时，先用大刻度估计范围，再不断地用缩小刻度，进行精确逼近。  
&emsp; 当在跳跃表中查询一个元素值时，都先从第一个节点的最顶层的level开始。比如说，在上图的跳表中查询o2元素时，先从o1的节点开始，因为zskiplist的header指针指向它。  

&emsp; 先从其level[3]开始查询，发现其跨度是2，o1节点的score是1.0，所以加起来为3.0，大于o2的score值2.0。所以，可以知道o2节点在o1和o3节点之间。这时，就改用小刻度的尺子了。就用level[1]的指针，顺利找到o2节点。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-6.png)  
&emsp; 它和平衡树性能很相似，但为什么不用平衡树而用skipList呢? skipList & AVL之间的选择：  
* 从算法实现难度上来比较，skiplist比平衡树要简单得多。  
* 平衡树的插入和删除操作可能引发子树的调整，逻辑复杂，而skiplist的插入和删除只需要修改相邻节点的指针，操作简单又快速。  
* 查找单个key，skiplist和平衡树的时间复杂度都为O(log n)，大体相当。  
* 在做范围查找的时候，平衡树比skiplist操作要复杂。  
* skiplist和各种平衡树（如AVL、红黑树等）的元素是有序排列的。  

&emsp; 可以看到，skipList中的元素是有序的，所以跳跃表在redis中用在有序集合键、集群节点内部数据结构。  
&emsp; 源码：  
&emsp; 跳跃表节点：  

```
typedef struct zskiplistNode {
    // 后退指针
    struct zskiplistNode *backward;
    // 分值
    double score;
    // 成员对象
    robj *obj;
    // 层
    struct zskiplistLevel {
    // 前进指针
    struct zskiplistNode *forward;
    // 跨度
    unsigned int span;
    } level[];
} zskiplistNode;  
```

&emsp; 跳跃表：  

```
typedef struct zskiplist {
    // 表头节点和表尾节点
    struct zskiplistNode *header, *tail;
    // 表中节点的数量
    unsigned long length;
    // 表中层数最大的节点的层数
    int level;
} zskiplist;
```
&emsp; 它有几个概念：
* 层(level[])  
&emsp; 层，也就是level[]字段，层的数量越多，访问节点速度越快。(因为它相当于是索引，层数越多，它索引就越细，就能很快找到索引值)  
* 前进指针(forward)  
&emsp; 层中有一个forward字段，用于从表头向表尾方向访问。  
* 跨度(span)  
&emsp; 用于记录两个节点之间的距离  
* 后退指针(backward)  
&emsp; 用于从表尾向表头方向访问。  

### 4.1.5. intset整数集合  
&emsp; 整数集合intset是集合对象的底层实现之一，当一个集合只包含整数值元素，并且这个集合的元素数量不多时，Redis就会使用整数集合作为集合对象的底层实现。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-7.png)  
&emsp; 如上图所示，整数集合的encoding表示它的类型，有int16t，int32t或者int64_t。其每个元素都是contents数组的一个数组项，各个项在数组中按值的大小从小到大有序的排列，并且数组中不包含任何重复项。length属性就是整数集合包含的元素数量。  

&emsp; Reids对整数存储专门作了优化，intset就是redis用于保存整数值的集合数据结构。当一个结合中只包含整数元素，redis就会用这个来存储。  


&emsp; 源码：  
&emsp; intset数据结构：  

```
typedef struct intset {
    // 编码方式
    uint32_t encoding;
    // 集合包含的元素数量
    uint32_t length;
    // 保存元素的数组
    int8_t contents[];
} intset;
```

### 4.1.6. ziplist压缩列表  
&emsp; 压缩队列ziplist是列表对象和哈希对象的底层实现之一。当满足一定条件时，列表对象和哈希对象都会以压缩队列为底层实现。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-8.png)  
&emsp; 压缩队列是Redis为了节约内存而开发的，是由一系列特殊编码的连续内存块组成的顺序型数据结构。它的属性值有：  
* zlbytes : 长度为4字节，记录整个压缩数组的内存字节数。  
* zltail : 长度为4字节，记录压缩队列表尾节点距离压缩队列的起始地址有多少字节，通过该属性可以直接确定尾节点的地址。  
* zllen : 长度为2字节，包含的节点数。当属性值小于 INT16_MAX时，该值就是节点总数，否则需要遍历整个队列才能确定总数。  
* zlend : 长度为1字节，特殊值，用于标记压缩队列的末端。  

&emsp; 中间每个节点entry由三部分组成：  
* previous_entry_length : 压缩列表中前一个节点的长度，和当前的地址进行指针运算，计算出前一个节点的起始地址。
* encoding：节点保存数据的类型和长度
* content：节点值，可以为一个字节数组或者整数。

&emsp; 当一个列表键只包含少量列表项，且是小整数值或长度比较短的字符串时，那么redis就使用ziplist（压缩列表）来做列表键的底层实现。  
&emsp; ziplist是Redis为了节约内存而开发的，是由一系列特殊编码的连续内存块(而不是像双端链表一样每个节点是指针)组成的顺序型数据结构；  

## 4.2. 对象系统，redisObject及5种基本类型  
### 4.2.1. redisObject  
&emsp; 当执行set hello world命令时，会有以下数据模型：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-9.png)  
* dictEntry：Redis给每个key-value键值对分配一个dictEntry，里面有着key和val的指针，next指向下一个dictEntry形成链表，这个指针可以将多个哈希值相同的键值对链接在一起，由此来解决哈希冲突问题(链地址法)。  
* sds：键key“hello”是以SDS（简单动态字符串）存储。  
* redisObject：值val“world”存储在redisObject中。实际上，redis常用5种类型都是以redisObject来存储的；而redisObject中的type字段指明了Value对象的类型，ptr字段则指向对象所在的地址。  

&emsp; redisObject对象非常重要，Redis对象的类型、内部编码、内存回收、共享对象等功能，都需要redisObject支持。这样设计的好处是，可以针对不同的使用场景，对5种常用类型设置多种不同的数据结构实现，从而优化对象在不同场景下的使用效率。  

&emsp; Redis内部使用一个redisObject对象来表示所有的key和value。  
&emsp;   上面介绍了6种底层数据结构，Redis并没有直接使用这些数据结构来实现键值数据库，而是基于这些数据结构创建了一个对象系统，这个系统包含字符串对象、列表对象、哈希对象、集合对象和有序集合这五种类型的对象，每个对象都使用到了至少一种前边讲的底层数据结构。  
&emsp; Redis根据不同的使用场景和内容大小来判断对象使用哪种数据结构，从而优化对象在不同场景下的使用效率和内存占用。  
&emsp; Redis的redisObject结构的定义如下所示。  

```
typedef struct redisObject {
    unsigned type:4;
    unsigned encoding:4;
    unsigned lru:LRU_BITS;
    int refcount;
    void *ptr;
} robj;
```
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-9.png)  
&emsp; Type：是对象类型，代表一个value对象具体是何种数据类型，包括REDISSTRING, REDISLIST, REDISHASH, REDISSET和REDIS_ZSET。  
&emsp; encoding是指对象使用的数据结构，是不同数据类型在redis内部的存储方式，比如：type=string代表value存储的是一个普通字符串，那么对应的encoding可以是raw或者是int，如果是 int 则代表实际redis内部是按数值型类存储和表示这个字符串的，当然前提是这个字符串本身可以用数值表示，比如："123" "456"这样的字符串。全集如下。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-10.png)  
redis所有的数据结构类型如下（重要，后面会用）：
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-11.png)  
&emsp; Redis数据结构底层实现：  

|Redis数据结构	|底层数据结构|
|---|---|
|String	|int、raw、embstr（即SDS）|
|Hash	|ziplist（压缩列表）或者hashtable（字典或者也叫哈希表）|
|List	|quicklist（快速列表，是ziplist压缩列表和linkedlist双端链表的组合）|
|Set	|intset（整数集合）或者hashtable（字典或者也叫哈希表）|
|ZSet	|ziplist（压缩列表）或者skiplist（跳跃表）|

### 4.2.2. String  
&emsp; 字符串对象的底层实现可以是int、raw、embstr（即SDS）。embstr编码是通过调用一次内存分配函数来分配一块连续的空间，而raw需要调用两次。  
&emsp; int编码字符串对象和embstr编码字符串对象在一定条件下会转化为raw编码字符串对象。  
&emsp; embstr：<=39字节的字符串。int：8个字节的长整型。raw：大于39个字节的字符串。

### 4.2.3. List  
&emsp; List对象的底层实现是quicklist（快速列表，是ziplist压缩列表和linkedlist双端链表的组合）。  
&emsp; Redis中的列表支持两端插入和弹出，并可以获得指定位置（或范围）的元素，可以充当数组、队列、栈等。  

```
typedef struct listNode {
    // 前置节点
    struct listNode *prev;
    // 后置节点
    struct listNode *next;
    // 节点的值
    void *value;
} listNode;
```

```
typedef struct list {
    // 表头节点
    listNode *head;
    // 表尾节点
    listNode *tail;
    // 节点值复制函数
    void *(*dup)(void *ptr);
    // 节点值释放函数
    void (*free)(void *ptr);
    // 节点值对比函数
    int (*match)(void *ptr, void *key);
    // 链表所包含的节点数量
    unsigned long len;
} list;
```
&emsp; quickList是zipList和linkedList的混合体。它将linkedList按段切分，每一段使用zipList来紧凑存储，多个zipList之间使用双向指针串接起来。因为链表的附加空间相对太高，prev和next指针就要占去16个字节 (64bit系统的指针是8个字节)，另外每个节点的内存都是单独分配，会加剧内存的碎片化，影响内存管理效率。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-13.png)  
&emsp; quicklist默认的压缩深度是0，也就是不压缩。为了支持快速的push/pop操作，quicklist的首尾两个ziplist不压缩，此时深度就是1。为了进一步节约空间，Redis还会对ziplist进行压缩存储，使用LZF算法压缩。  

### 4.2.4. Hash  
&emsp; Hash对象的底层实现可以是ziplist（压缩列表）或者hashtable（字典或者也叫哈希表）。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-14.png)  
&emsp; Hash对象只有同时满足下面两个条件时，才会使用ziplist（压缩列表）：1.哈希中元素数量小于512个；2.哈希中所有键值对的键和值字符串长度都小于64字节。  

### 4.2.5. Set  
&emsp; Set集合对象的底层实现可以是intset（整数集合）或者hashtable（字典或者也叫哈希表）。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-15.png)  
&emsp; intset（整数集合）当一个集合只含有整数，并且元素不多时会使用intset（整数集合）作为Set集合对象的底层实现。  

### 4.2.6. ZSet  
ZSet有序集合对象底层实现可以是ziplist（压缩列表）或者skiplist（跳跃表）。
&emsp; ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-16.png)    
&emsp; 当一个有序集合的元素数量比较多或者成员是比较长的字符串时，Redis就使用skiplist（跳跃表）作为ZSet对象的底层实现。  

-----------
# 5. Redis的API
&emsp; 官网推荐的 Java 客户端有 3 个 Jedis，Redisson 和 Luttuce。  

* Jedis，轻量，简洁，便于集成和改造。  
* Redisson  
&emsp; 与 Jedis 相比，Lettuce 则完全克服了其线程不安全的缺点：Lettuce 是一个可伸缩 的线程安全的 Redis 客户端，支持同步、异步和响应式模式（Reactive）。多个线程可 以共享一个连接实例，而不必担心多线程并发问题。  
&emsp; 同步调用：com.gupaoedu.lettuce.LettuceSyncTest。  
&emsp; 异步的结果使用 RedisFuture 包装，提供了大量回调的方法。  
&emsp; 异步调用：com.gupaoedu.lettuce.LettuceASyncTest。   

    &emsp; 它基于 Netty 框架构建，支持 Redis 的高级功能，如 Pipeline、发布订阅，事务、 Sentinel，集群，支持连接池。  

* Redisson  
&emsp; Redisson 是一个在 Redis 的基础上实现的 Java 驻内存数据网格（In-Memory Data Grid），提供了分布式和可扩展的 Java 数据结构。  
&emsp; 特点：  
    * 基于 Netty 实现，采用非阻塞 IO，性能高。  
    * 支持异步请求。  
    * 支持连接池、pipeline、LUA Scripting、Redis Sentinel、Redis Cluster。  
    * 不支持事务，官方建议以 LUA Scripting 代替事务。  
    * 主从、哨兵、集群都支持。Spring 也可以配置和注入 RedissonClient。 

# 6. Redis命令参考  
&emsp; http://doc.redisfans.com/ 

