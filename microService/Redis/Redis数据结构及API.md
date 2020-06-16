---
title: Redis数据结构及API
date: 2020-05-15 00:00:00
tags:
    - Redis
---

<!-- TOC -->

- [1. Redis简介](#1-redis简介)
- [2. Redis的数据类型](#2-redis的数据类型)
    - [2.1. 数据类型介绍](#21-数据类型介绍)
    - [2.2. 键管理](#22-键管理)
    - [2.3. 对象系统RedisObject](#23-对象系统redisobject)
    - [2.4. String](#24-string)
        - [2.4.1. 操作命令](#241-操作命令)
        - [2.4.2. 使用场景](#242-使用场景)
        - [2.4.3. 内部编码](#243-内部编码)
    - [2.5. Hash](#25-hash)
        - [2.5.1. 操作命令](#251-操作命令)
        - [2.5.2. 使用场景](#252-使用场景)
        - [2.5.3. 内部编码](#253-内部编码)
            - [2.5.3.1. ziplist 压缩列表](#2531-ziplist-压缩列表)
            - [2.5.3.2. hashtable（dict），字典](#2532-hashtabledict字典)
    - [2.6. List](#26-list)
        - [2.6.1. 操作命令](#261-操作命令)
        - [2.6.2. 使用场景](#262-使用场景)
        - [2.6.3. 内部编码](#263-内部编码)
            - [2.6.3.1. linkedlist](#2631-linkedlist)
            - [2.6.3.2. quicklist](#2632-quicklist)
    - [2.7. Set](#27-set)
        - [2.7.1. 操作命令](#271-操作命令)
        - [2.7.2. 使用场景](#272-使用场景)
        - [2.7.3. 内部编码](#273-内部编码)
            - [2.7.3.1. inset](#2731-inset)
    - [2.8. Zset](#28-zset)
        - [2.8.1. 操作命令](#281-操作命令)
        - [2.8.2. 使用场景](#282-使用场景)
        - [2.8.3. 内部编码](#283-内部编码)
    - [2.9. Bitmaps，位图](#29-bitmaps位图)
    - [2.10. HyperLogLog](#210-hyperloglog)
    - [2.11. Geospatial](#211-geospatial)
    - [2.12. Streams](#212-streams)
    - [2.13. Redis中的布隆过滤器](#213-redis中的布隆过滤器)
- [3. Redis的API](#3-redis的api)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-71.png)  

&emsp; <font color="red">整体参考《Redis开发与运维》，数据结构参考《Redis深度历险：核心原理和应用实践》</font>  

# 1. Redis简介  
&emsp; Redis是一个开源，内存存储的数据结构服务器，可用作数据库，高速缓存和消息队列代理。  
1. 支持字符串、哈希表、列表、集合、有序集合，位图，hyperloglogs等丰富的数据类型。  
2. 提供Lua脚本、LRU收回、事务以及不同级别磁盘持久化功能。  
3. 同时通过Redis Sentinel提供高可用，通过Redis Cluster提供自动分区。  

# 2. Redis的数据类型  
## 2.1. 数据类型介绍  
&emsp; Redis属于<key,value\>形式的数据结构。key和value的最大长度限制是512M。  
1. Redis的key是字符串类型，但是key中不能包括边界字符，不能空格和换行。  
2. Redis的value支持五种基本数据类型<font color = "red">（注意是数据类型不是数据结构）</font>：String（字符串），Hash（哈希），List（列表），Set（集合）及Zset(sorted set，有序集合)。每个数据类型最多能处理2^32个key。  
3. Redis还有几种高级数据类型：bitmaps、HyperLogLog、geo、Streams（5.0最新版本数据结构）。  
4. Redis提供插件功能使用布隆过滤器。  
5. Redis内部采用对象系统RedisObject构建数据类型。  
6. RedisObject对象系统内部采用多种数据结构构建数据类型。数据结构有：int、raw、embstr（SDS）、linkedlist、ziplist、skiplist、hashtable、inset。  

<!-- 
|数据类型	|可以存储的值	|操作	|使用场景|
|---|---|---|---|
|String	|字符串、整数或者浮点数	|对整个字符串或者字符串的其中一部分执行操作；对整数和浮点数执行自增或者自减操作；	|1.缓存功能，如存放序列化后的用户信息 <br/>2.计数 <br/>3.共享session <br/>4.限速，如限制用户每分钟获取验证码的速率|
|Hash	|键值对(无序散列表)	|添加、获取、移除单个键值对；获取所有键值对；检查某个键是否存在；|	1.缓存功能，如存放用户信息，相较String可减少内存空间使用|
|List	|链表 |从两端压入或者弹出元素；读取单个或者多个元素；进行修剪，只保留一个范围内对元素；	|1.消息队列，lpush+brpop实现阻塞队列<br/> 2.文章列表 <br/>3.栈：lpush+lpop = Stack <br/>4.队列：lpush+lpop = Queue|
|Set	|无序集合|添加、获取、移除单个元素； 检查一个元素是否存在于集合中； 计算交集、并集、差集；从集合里面随机获取元素；|	1.标签(Tag) <br/>2.社交|
|Zset	|有序集合 | 添加、获取、删除元素；根据分值范围或者成员来获取元素； 计算一个键对排名；|1.排行榜系统，比如点赞排名 <br/>2.社交|
-->
## 2.2. 键管理  
......

## 2.3. 对象系统RedisObject  
&emsp; Redis并没有直接使用数据结构来实现数据类型，而是基于这些数据结构创建了一个对象系统RedisObject，每个对象都使用到了至少一种底层数据结构。<font color = "red">Redis根据不同的使用场景和内容大小来判断对象使用哪种数据结构，从而优化对象在不同场景下的使用效率和内存占用。</font>  

&emsp; Redis的redisObject结构的定义如下所示：  

```
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
* encoding是指对象使用的数据结构，是不同数据类型在redis内部的存储方式。  

&emsp; Redis数据类型的底层实现：  

|Redis数据结构	|底层数据结构|
|---|---|
|String	|int、raw、embstr（即SDS）|
|Hash	|ziplist（压缩列表）或者hashtable（字典或者也叫哈希表）|
|List	|quicklist（快速列表，是ziplist压缩列表和linkedlist双端链表的组合）|
|Set	|intset（整数集合）或者hashtable（字典或者也叫哈希表）|
|ZSet	|ziplist（压缩列表）或者skiplist（跳跃表）|

## 2.4. String  
&emsp; 可以用来存储字符串、整数、浮点数。   

### 2.4.1. 操作命令  

|命令|描述|
|---|---|
|SET key value    |设置指定key的值|
|GET key |   获取指定key的值|
|GETRANGE key start end |   返回key中字符串值的子字符|
|GETSET key value   |将给定key的值设为value，并返回key的旧值(old value)。设新值，取旧值|
|GETBIT key offset  | 对key所储存的字符串值，获取指定偏移量上的位(bit)。|
|MGET key1 [key2..]   |获取所有(一个或多个)给定key的值。|
|SETBIT key offset value   |对key所储存的字符串值，设置或清除指定偏移量上的位(bit)。|
|SETEX key seconds value   |将值value关联到key，并将key的过期时间设为seconds(以秒为单位)。|
|SETNX key value   |只有在key不存在时设置key的值。|
|SETRANGE key offset value   |用value参数覆写给定 key 所储存的字符串值，从偏移量offset开始。|
|STRLEN key   |返回key所储存的字符串值的长度。|
|MSET key value [key value ...]   |同时设置一个或多个key-value对。  MSET key1 "Hello" key2 "World"|
|MSETNX key value [key value ...] |同时设置一个或多个key-value对，当且仅当所有给定key都不存在。|
|PSETEX key milliseconds value   |这个命令和SETEX命令相似，但它以毫秒为单位设置key的生存时间，而不是像 SETEX 命令那样，以秒为单位。|
|INCR key   |将key中储存的数字值增一。|
|INCRBY key increment |将key所储存的值加上给定的增量值（increment）。|
|INCRBYFLOAT key increment |将key所储存的值加上给定的浮点增量值（increment）。|
|DECR key   |将key中储存的数字值减一。|
|DECRBY key decrement |key所储存的值减去给定的减量值（decrement）。|
|APPEND key value   |如果key已经存在并且是一个字符串， APPEND命令将指定value 追加到改key原来的值（value）的末尾。|

### 2.4.2. 使用场景  

&emsp; 参考《Redis开发与运维》，书中有使用案例。  

* 缓存功能
* 共享Session
* 全局ID
* 计数  
* 限速  

### 2.4.3. 内部编码  
&emsp; 字符串类型的内部编码有三种：  

*  int，存储 8 个字节的长整型（long，2^63-1）。   
*  embstr, 代表 embstr 格式的 SDS（Simple Dynamic String 简单动态字符串）， 存储小于 44 个字节的字符串。   
*  raw，存储大于 44 个字节的字符串（3.2 版本之前是 39 字节）。  

&emsp; Redis会根据当前值的类型和长度决定使用哪种内部编码实现。  

1. 什么是 SDS？ Redis中字符串的实现。  
&emsp; 在 3.2 以后的版本中，SDS 又有多种结构（sds.h）：sdshdr5、sdshdr8、sdshdr16、sdshdr32、sdshdr64，用于存储不同的长度的字符串，分别代表 2^5=32byte， 2^8=256byte，2^16=65536byte=64KB，2^32byte=4GB。  

2. 为什么 Redis 要用 SDS 实现字符串？  
&emsp; C 语言本身没有字符串类型（只能用字符数组 char[]实现）。 
    1. 使用字符数组必须先给目标变量分配足够的空间，否则可能会溢出。  
    2. 如果要获取字符长度，必须遍历字符数组，时间复杂度是 O(n)。  
    3. C 字符串长度的变更会对字符数组做内存重分配。  
    4. 通过从字符串开始到结尾碰到的第一个'\0'来标记字符串的结束，因此不能保 存图片、音频、视频、压缩文件等二进制(bytes)保存的内容，二进制不安全。  

    &emsp; SDS的特点：  
    1. 不用担心内存溢出问题，如果需要会对 SDS 进行扩容。  
    2. 获取字符串长度时间复杂度为 O(1)，因为定义了 len 属性。  
    3. 通过“空间预分配”（ sdsMakeRoomFor）和“惰性空间释放”，防止多 次重分配内存。  
    4. 判断是否结束的标志是 len 属性（它同样以'\0'结尾是因为这样就可以使用 C语言中函数库操作字符串的函数了），可以包含'\0'。  

3. embstr 和 raw 的区别？  
&emsp; embstr 的使用只分配一次内存空间（因为 RedisObject 和 SDS 是连续的），而 raw 需要分配两次内存空间（分别为 RedisObject 和 SDS 分配空间）。 因此与 raw 相比，embstr 的好处在于创建时少分配一次空间，删除时少释放一次 空间，以及对象的所有数据连在一起，寻找方便。 而 embstr 的坏处也很明显，如果字符串的长度增加需要重新分配内存时，整个 RedisObject 和 SDS 都需要重新分配空间，因此 Redis 中的 embstr 实现为只读。  

4. int 和 embstr 什么时候转化为 raw?  
&emsp; 当 int 数 据 不 再 是 整 数 ， 或 大 小 超 过 了 long 的 范 围 （2^63-1=9223372036854775807）时，自动转化为 embstr。  

5. embstr没有超过阈值，为什么变成 raw 了？  
&emsp; 对于 embstr，由于其实现是只读的，因此在对 embstr 对象进行修改时，都会先 转化为 raw 再进行修改。 因此，只要是修改 embstr 对象，修改后的对象一定是 raw 的，无论是否达到了 44 个字节。  

6. 当长度小于阈值时，会还原吗？  
&emsp; 关于 Redis 内部编码的转换，都符合以下规律：编码转换在 Redis 写入数据时完 成，且转换过程不可逆，只能从小内存编码向大内存编码转换（但是不包括重新 set）。  

## 2.5. Hash  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-58.png)  
&emsp; 存储键值对的无序散列表。  
&emsp; 同样是存储字符串，Hash 与 String 的主要区别？  
1. 把所有相关的值聚集到一个 key 中，节省内存空间  
2. 只使用一个 key，减少 key 冲突  
3. 当需要批量获取值的时候，只需要使用一个命令，减少内存/IO/CPU 的消耗 Hash 

&emsp; 不适合的场景： 
1. Field 不能单独设置过期时间  
2. 没有 bit 操作  
3. 需要考虑数据量分布的问题（value 值非常大的时候，无法分布到多个节点）  

### 2.5.1. 操作命令  
......

### 2.5.2. 使用场景  

* 存储对象类型的数据  
&emsp; 比如对象或者一张表的数据，比 String 节省了更多 key 的空间，也更加便于集中管 理。  
* 购物车功能  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-60.png)  
&emsp; key：用户 id；field：商品 id；value：商品数量。  
&emsp; +1：hincr。-1：hdecr。删除：hdel。全选：hgetall。商品数：hlen。  

### 2.5.3. 内部编码  
&emsp; Redis 的 Hash 本身也是一个 KV 的结构，类似于 Java 中的 HashMap。外层的哈希（Redis KV 的实现）只用到了 hashtable。当存储 hash 数据类型时，把它叫做内层的哈希。内层的哈希底层可以使用两种数据结构实现：ziplist、hashtable。  

#### 2.5.3.1. ziplist 压缩列表  
&emsp; ziplist是一个经过特殊编码的双向链表，它不存储指向上一个链表节点和指向下一 个链表节点的指针，而是存储上一个节点长度和当前节点长度，通过牺牲部分读写性能，来换取高效的内存空间利用率，是一种时间换空间的思想。只用在字段个数少，字段值小的场景面。  

&emsp; 什么时候使用 ziplist 存储？  
&emsp; 当 hash 对象同时满足以下两个条件的时候，使用 ziplist 编码：  
1. 所有的键值对的健和值的字符串长度都小于等于 64byte（一个英文字母 一个字节）；  
2. 哈希对象保存的键值对数量小于 512 个。  

&emsp; 一个哈希对象超过配置的阈值（键和值的长度有>64byte，键值对个数>512 个）时， 会转换成哈希表hashtable。  

#### 2.5.3.2. hashtable（dict），字典  
&emsp; 在 Redis 中，hashtable 被称为字典（dictionary），它是一个数组+链表的结构。Redis Hash使用MurmurHash2算法来计算键的哈希值，并且使用链地址法来解决键冲突，进行了一些rehash优化等。结构如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-4.png)  


## 2.6. List  
&emsp; 存储有序的字符串（从左到右），元素可以重复。可以充当队列和栈的角色。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-61.png)  

### 2.6.1. 操作命令  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-64.png)  


### 2.6.2. 使用场景  

* 消息队列  
&emsp; List 提供了两个阻塞的弹出操作：BLPOP/BRPOP，可以设置超时时间。  

    * BLPOP：BLPOP key1 timeout 移出并获取列表的第一个元素， 如果列表没有元素 会阻塞列表直到等待超时或发现可弹出元素为止。  
    * BRPOP：BRPOP key1 timeout 移出并获取列表的最后一个元素， 如果列表没有元 素会阻塞列表直到等待超时或发现可弹出元素为止。  

    &emsp; 队列：先进先出：rpush blpop，左头右尾，右边进入队列，左边出队列。  
    &emsp; 栈：先进后出：rpush brpop   

* 文章列表  
&emsp; 每个用户有属于自己的文章列表，现需要分页展示文章列表。此时可以 考虑使用列表，因为列表不但是有序的，同时支持按照索引范围获取元素。  
&emsp; 使用列表类型保存和获取文章列表会存在两个问题。第一，如果每次分 页获取的文章个数较多，需要执行多次hgetall操作，此时可以考虑使用 Pipeline批量获取，或者考虑将文章数据序列化为字符串类 型，使用mget批量获取。第二，分页获取文章列表时，lrange命令在列表两 端性能较好，但是如果列表较大，获取列表中间范围的元素性能会变差，此 时可以考虑将列表做二级拆分，或者使用Redis3.2的quicklist内部编码实现， 它结合ziplist和linkedlist的特点，获取列表中间范围的元素时也可以高效完成。  


### 2.6.3. 内部编码   
&emsp; 在早期的版本中，数据量较小时用 ziplist 存储，达到临界值时转换为 linkedlist 进行存储。   
&emsp; Redis3.2 版本之后，统一用 quicklist 来存储。  

#### 2.6.3.1. linkedlist  
&emsp; Redis的链表在双向链表上扩展了头、尾节点、元素数等属性。Redis的链表结构如下：
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-62.png)  

&emsp; 从图中可以看出Redis的linkedlist双端链表有以下特性：节点带有prev、next指针、head指针和tail指针，获取前置节点、后置节点、表头节点和表尾节点的复杂度都是O（1）。len属性获取节点数量也为O（1）。

#### 2.6.3.2. quicklist
&emsp; quicklist（快速列表）是ziplist和linkedlist的结合体。quicklist 存储了一个双向链表，每个节点 都是一个ziplist。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-63.png)  


## 2.7. Set  
&emsp; 存储String 类型的无序集合，最大存储数量 2^32-1（40 亿左右）。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-65.png)  

### 2.7.1. 操作命令  
&emsp; 可以分为集合内操作、集合间操作。  

### 2.7.2. 使用场景  

* 抽奖  
&emsp; 随机获取元素，spop myset  
* 点赞、签到、打卡  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-66.png)  
&emsp; 这条微博的 ID 是 t1001，用户 ID 是 u3001。  
&emsp; 用 like:t1001 来维护 t1001 这条微博的所有点赞用户。   
&emsp; 点赞了这条微博：sadd like:t1001 u3001  
&emsp; 取消点赞：srem like:t1001 u3001  
&emsp; 是否点赞：sismember like:t1001 u3001  
&emsp; 点赞的所有用户：smembers like:t1001  
&emsp; 点赞数：scard like:t1001   
&emsp; 比关系型数据库简单许多。  
* 商品标签  
&emsp; 用 tags:i5001 来维护商品所有的标签。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-67.png)  
&emsp; sadd tags:i5001 画面清晰细腻  
&emsp; sadd tags:i5001 真彩清晰显示屏  
&emsp; sadd tags:i5001 流畅至极  
* 商品筛选  
&emsp; 获取差集 sdiff set1 set2   
&emsp; 获取交集（intersection ） sinter set1 set2  
&emsp; 获取并集 sunion set1 set2  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-68.png)  
&emsp; iPhone11 上市了。 sadd brand:apple iPhone11  
&emsp; sadd brand:ios iPhone11  
&emsp; sad screensize:6.0-6.24 iPhone11  
&emsp; sad screentype:lcd iPhone11  

&emsp; 筛选商品，苹果的，iOS 的，屏幕在 6.0-6.24 之间的，屏幕材质是 LCD 屏幕  
&emsp; sinter brand:apple brand:ios screensize:6.0-6.24 screentype:lcd  
* 用户关注、推荐模型  

### 2.7.3. 内部编码   
&emsp; Redis 用intset或hashtable存储set。<font color = "red">如果元素都是整数类型，就用 inset 存储。 如果不是整数类型，就用 hashtable（数组+链表的存来储结构）。</font>  

&emsp; KV 怎么存储 set 的元素？  
&emsp; key 就是元素的值，value 为 null。  
&emsp; 如果元素个数超过 512 个，也会用 hashtable 存储。  

#### 2.7.3.1. inset  
&emsp; inset的数据结构：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-7.png)  
&emsp; 整数集合的encoding表示它的类型，有int16t，int32t或者int64_t。其每个元素都是contents数组的一个数组项，各个项在数组中按值的大小从小到大有序的排列，并且数组中不包含任何重复项。length属性就是整数集合包含的元素数量。  

## 2.8. Zset  
&emsp; sorted set，有序的 set，每个元素有个 score。 有序集合中的元素不能重复，但是score可以重复。score 相同时，按照 key 的 ASCII 码排序。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-70.png)  

&emsp; 数据结构对比： 

|数据结构 |是否允许重复元素 |是否有序 |有序实现方式| 
|---|---|---|---|
|列表 list| 是 |是 |索引下标| 
|集合 set |否 |否| 无 |
|有序集合 zset |否 |是 |分值 score|

### 2.8.1. 操作命令  
&emsp; 可以分为集合内操作、集合间操作。  

### 2.8.2. 使用场景  

* 排行榜  
&emsp; 排行榜榜单的维度可能是多个方面的：按照时间、按照播 放数量、按照获得的赞数。  

### 2.8.3. 内部编码   
&emsp; 同时满足以下条件时使用 ziplist 编码：  

* 元素数量小于 128 个  
* 所有 member 的长度都小于 64 字节  

&emsp; 超过阈值之后，使用 skiplist和hashtable存储。  

## 2.9. Bitmaps，位图  
&emsp; Bitmaps 是在字符串类型上面定义的位操作。一个字节由 8 个二进制位组成。每个二进制位只能存储0或1。   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-71.png)  
&emsp; ***操作命令：***  
&emsp; bit操作被分为两组：  

* 恒定时间的单个bit操作，例如把某个bit设置为0或者1。或者获取某bit的值。  
* 对一组bit的操作。例如给定范围内bit统计（例如人口统计）。  

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

&emsp; ***应用场景：*** 

* 各种实时分析，例如在线用户统计。
* 用户访问统计。
<!-- 
存储与对象ID关联的布尔信息。  
&emsp; 例如，记录访问网站的用户的最长连续时间。开始计算从0开始的天数，就是网站公开的那天，每次用户访问网站时通过SETBIT命令设置bit为1，可以简单的用当前时间减去初始时间并除以3600*24（结果就是网站公开的第几天）当做这个bit的位置。  
&emsp; 这种方法对于每个用户，都有存储每天的访问信息的一个很小的string字符串。通过BITCOUN就能轻易统计某个用户历史访问网站的天数。另外通过调用BITPOS命令，或者客户端获取并分析这个bitmap，就能计算出最长停留时间。  
-->

&emsp; ***优点与缺点：***  
&emsp; Bitmaps的最大优点就是存储信息时可以节省大量的空间。例如在一个系统中，不同的用户被一个增长的用户ID表示。40亿（2^32≈40亿）用户只需要512M内存就能记住某种信息，例如用户是否登录过。  

## 2.10. HyperLogLog  
&emsp; Hyper指的是超级。Hyperloglog提供不精确的去重计数功能。  
&emsp; ***操作命令：***  
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

&emsp; ***应用场景：***  
&emsp; 适于做大规模数据的去重统计，例如统计 UV。  

    网页流量统计里的PV、UV：
    PV（Page View）访问量, 即页面浏览量或点击量，衡量网站用户访问的网页数量；在一定统计周期内用户每打开或刷新一个页面就记录1次，多次打开或刷新同一页面则浏览量累计。
    UV（Unique Visitor）独立访客，统计1天内访问某站点的用户数(以cookie为依据);访问网站的一台电脑客户端为一个访客。可以理解成访问某网站的电脑的数量。网站判断来访电脑的身份是通过来访电脑的cookies实现的。如果更换了IP后但不清除cookies，再访问相同网站，该网站的统计中UV数是不变的。如果用户不保存cookies访问、清除了cookies或者更换设备访问，计数会加1。00:00-24:00内相同的客户端多次访问只计为1个访客。  

&emsp; ***优点与缺点：***  

* 优点：占用内存极小，对于一个key，只需要12kb。  
* 缺点：查询指定用户的时候，可能会出错，毕竟存的不是具体的数据。总数也存在一定的误差。  
        
## 2.11. Geospatial
&emsp; 可以用来保存地理位置，并作位置距离计算或者根据半径计算位置等。  

## 2.12. Streams  
&emsp; Redis5.0 推出的数据类型。支持多播的可持久化的消息队列，用于实现发布订阅功能，借 鉴了 kafka 的设计。  

## 2.13. Redis中的布隆过滤器  
&emsp; 之前的布隆过滤器可以使用Redis中的位图操作实现，直到Redis4.0版本提供了插件功能，Redis官方提供的布隆过滤器才正式登场。布隆过滤器作为一个插件加载到Redis Server中，就会给Redis提供了强大的布隆去重功能。  

<!-- 
详细解析Redis中的布隆过滤器及其应用
https://mp.weixin.qq.com/s/h7K7w9XBYRk7NApRV9evYA
Redis亿级数据过滤和布隆过滤器
https://mp.weixin.qq.com/s/3TcNbNNobn2QEJFat-f90A
-->

# 3. Redis的API
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

