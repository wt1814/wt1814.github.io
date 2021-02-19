
<!-- TOC -->

- [1. Redis](#1-redis)
    - [1.1. Redis简介](#11-redis简介)
    - [1.2. Redis的数据类型](#12-redis的数据类型)
        - [1.2.1. Key操作命令](#121-key操作命令)
            - [1.2.1.1. expire命令和ttl命令](#1211-expire命令和ttl命令)
            - [1.2.1.2. 使用scan代替keys指令](#1212-使用scan代替keys指令)
            - [1.2.1.3. Redis中的批量删除数据库中的Key](#1213-redis中的批量删除数据库中的key)
        - [1.2.2. String字符串](#122-string字符串)
            - [1.2.2.1. 常用操作](#1221-常用操作)
            - [1.2.2.2. 使用场景](#1222-使用场景)
        - [1.2.3. Hash哈希](#123-hash哈希)
            - [1.2.3.1. 常用操作](#1231-常用操作)
            - [1.2.3.2. 使用场景](#1232-使用场景)
        - [1.2.4. List列表](#124-list列表)
            - [1.2.4.1. 常用操作](#1241-常用操作)
            - [1.2.4.2. 使用场景](#1242-使用场景)
        - [1.2.5. Set集合](#125-set集合)
            - [1.2.5.1. 常用操作](#1251-常用操作)
            - [1.2.5.2. 使用场景](#1252-使用场景)
        - [1.2.6. ZSet有序集合](#126-zset有序集合)
            - [1.2.6.1. 常用操作](#1261-常用操作)
            - [1.2.6.2. 使用场景](#1262-使用场景)
            - [1.2.6.3. 实现多维排序](#1263-实现多维排序)
        - [1.2.7. 数据结构对比](#127-数据结构对比)

<!-- /TOC -->

<!--
 

-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-72.png)  

# 1. Redis  
&emsp; <font color="red">整体参考《Redis开发与运维》，数据结构参考《Redis深度历险：核心原理和应用实践》</font>  

## 1.1. Redis简介  
&emsp; Redis是一个开源，内存存储的数据结构服务器，可用作数据库，高速缓存和消息队列。  
1. 支持字符串、哈希表、列表、集合、有序集合，位图，hyperloglogs等丰富的数据类型。  
2. 提供Lua脚本、LRU收回、事务以及不同级别磁盘持久化功能。  
3. 同时通过Redis Sentinel提供高可用，通过Redis Cluster提供自动分区。  

## 1.2. Redis的数据类型  
&emsp; Redis属于<key,value\>形式的数据结构。key和value的最大长度限制是512M。  
1. Redis的key是字符串类型，但是key中不能包括边界字符，不能空格和换行。  
2. Redis的value支持五种基本数据类型<font color = "lime">(注意是数据类型不是数据结构)</font>：String(字符串)，Hash(哈希)，List(列表)，Set(集合)及Zset(sorted set，有序集合)。每个数据类型最多能处理2^32个key。  
3. Redis还有几种高级数据类型：bitmaps、HyperLogLog、geo、Streams(5.0最新版本数据结构)。  
4. Redis提供插件功能使用布隆过滤器。  
5. Redis内部采用对象系统RedisObject构建数据类型。  
6. RedisObject对象系统内部采用多种数据结构构建数据类型。数据结构有：int、raw、embstr(SDS)、linkedlist、ziplist、skiplist、hashtable、inset。  

### 1.2.1. Key操作命令 
#### 1.2.1.1. expire命令和ttl命令
1. expire  
&emsp; **EXPIRE key seconds(单位/秒)**   
&emsp; **<font color = "red">为给定key设置生存时间，当key过期时(生存时间为 0 )，它会被自动删除。</font>**  
&emsp; 在Redis中，带有生存时间的 key 被称为『易失的』(volatile)。  
&emsp; 生存时间可以通过使用DEL命令来删除整个key来移除，或者被SET和GETSET命令覆写(overwrite)，这意味着，如果一个命令只是修改(alter)一个带生存时间的 key的值而不是用一个新的key值来代替(replace)它的话，那么生存时间不会被改变。  
&emsp; 比如说，对一个key执行INCR命令，对一个列表进行 LPUSH 命令，或者对一个哈希表执行 HSET 命令，这类操作都不会修改 key 本身的生存时间。  
&emsp; 另一方面，如果使用 RENAME 对一个 key 进行改名，那么改名后的 key 的生存时间和改名前一样。  
&emsp; RENAME 命令的另一种可能是，尝试将一个带生存时间的 key 改名成另一个带生存时间的 another_key ，这时旧的 another_key (以及它的生存时间)会被删除，然后旧的key会改名为another_key ，因此，新的another_key的生存时间也和原本的key一样。  
&emsp; 使用 PERSIST命令可以在不删除key的情况下，移除 key 的生存时间，让 key 重新成为一个『持久的』(persistent) key 。  
&emsp; **更新生存时间**   
&emsp; 可以对一个已经带有生存时间的 key 执行 EXPIRE 命令，新指定的生存时间会取代旧的生存时间。    
&emsp; **过期时间的精确度**   
&emsp; 在 Redis 2.4 版本中，过期时间的延迟在 1 秒钟之内 —— 也即是，就算 key 已经过期，但它还是可能在过期之后一秒钟之内被访问到，而在新的 Redis 2.6 版本中，延迟被降低到 1 毫秒之内。   

2. ttl  
&emsp; **TTL key，<font color = "red">以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live)。</font>**  

&emsp; **返回值：**  
&emsp; 当 key 不存在时，返回 -2 。  
&emsp; 当 key 存在但没有设置剩余生存时间时，返回 -1 。  
&emsp; 否则，以秒为单位，返回 key 的剩余生存时间。  

#### 1.2.1.2. 使用scan代替keys指令  
......
<!-- 
在RedisTemplate中使用scan代替keys指令 
https://mp.weixin.qq.com/s/8hBrUb1Tn6cuSzQITCDReQ
-->

#### 1.2.1.3. Redis中的批量删除数据库中的Key  
&emsp; Redis中有删除单个Key的指令 DEL，但似乎没有批量删除 Key 的指令，不过可以借助 Linux 的 xargs 指令来完成这个动作。  
<!-- 
https://www.cnblogs.com/DreamDrive/p/5772198.html
 熬了一个通宵终于把Key删完了 
 https://mp.weixin.qq.com/s/xb6USb3FLIDDloUPoqBnMw
-->

### 1.2.2. String字符串
&emsp; String可以用来存储字符串、整数、浮点数。最大值不能超过512MB。  

#### 1.2.2.1. 常用操作

|命令|描述|
|---|---|
|SET key value    |设置指定key的值|
|GET key |   获取指定key的值|
|GETRANGE key start end| 返回key中字符串值的子字符|
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
|INCRBY key increment |将key所储存的值加上给定的增量值(increment)。|
|INCRBYFLOAT key increment |将key所储存的值加上给定的浮点增量值(increment)。|
|DECR key   |将key中储存的数字值减一。|
|DECRBY key decrement |key所储存的值减去给定的减量值(decrement)。|
|APPEND key value   |如果key已经存在并且是一个字符串， APPEND命令将指定value 追加到改key原来的值(value)的末尾。|

#### 1.2.2.2. 使用场景
&emsp; String使用场景: (参考《Redis开发与运维》，书中有使用案例。)  

* 缓存功能  
&emsp; 在web服务中，使用MySQL作为数据库，Redis作为缓存。由于Redis具有支撑高并发的特性，通常能起到加速读写和降低后端压力的作用。web端的大多数请求都是从Redis中获取的数据，如果Redis中没有需要的数据，则会从MySQL中去获取，并将获取到的数据写入redis。  
* 共享Session  
&emsp; 在分布式系统中，用户的每次请求会访问到不同的服务器，这就会导致session不同步的问题，假如一个用来获取用户信息的请求落在A服务器上，获取到用户信息后存入session。下一个请求落在B服务器上，想要从session中获取用户信息就不能正常获取了，因为用户信息的session在服务器A上，为了解决这个问题，使用redis集中管理这些session，将session存入redis，使用的时候直接从redis中获取就可以了。  
* 全局ID
* 计数  
&emsp; Redis中有一个字符串相关的命令incr key，incr命令对值做自增操作，返回结果分为以下三种情况：  

    * 值不是整数，返回错误
    * 值是整数，返回自增后的结果
    * key不存在，默认键为0，返回1
&emsp; 比如文章的阅读量，视频的播放量等等都会使用redis来计数，每播放一次，对应的播放量就会加1，同时将这些数据异步存储到数据库中达到持久化的目的。  
* 限速  
&emsp; 为了安全考虑，有些网站会对IP进行限制，限制同一IP在一定时间内访问次数不能超过n次。  

### 1.2.3. Hash哈希
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-58.png)  
&emsp; Hash存储键值对的无序散列表。  

&emsp; <font color = "lime">Hash与String同样是存储字符串(存储单个字符串时使用String；存储对象时使用Hash，勿将对象序列化后存String类型)，它们的主要区别：</font>  
1. 把所有相关的值聚集到一个 key 中，节省内存空间  
2. 只使用一个 key，减少 key 冲突  
3. 当需要批量获取值的时候，只需要使用一个命令，减少内存/IO/CPU 的消耗  

&emsp; **不适合的场景：** 
1. Field 不能单独设置过期时间  
2. 没有bit操作  
3. 需要考虑数据量分布的问题(value 值非常大的时候，无法分布到多个节点)  

#### 1.2.3.1. 常用操作
&emsp; ......

#### 1.2.3.2. 使用场景
&emsp; **使用场景：**  
* 存储对象类型的数据  
&emsp; 比如对象或者一张表的数据，比String节省了更多key的空间，也更加便于集中管理。  
* 购物车功能  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-60.png)  
&emsp; key：用户 id；field：商品 id；value：商品数量。  
&emsp; +1：hincr。-1：hdecr。删除：hdel。全选：hgetall。商品数：hlen。  

### 1.2.4. List列表  
&emsp; 存储有序的字符串(从左到右)，元素可以重复。一个列表最多可以存储2^32-1个元素，列表的两端都可以插入和弹出元素，可以充当队列和栈的角色。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-61.png)  

#### 1.2.4.1. 常用操作
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-64.png)  
&emsp; List中分页查询命令LRANGE  


#### 1.2.4.2. 使用场景
&emsp; 使用场景：  

* 消息队列  
&emsp; 列表用来存储多个有序的字符串，既然是有序的，那么就满足消息队列的特点。使用lpush+rpop或者rpush+lpop实现消息队列。除此之外，redis支持阻塞操作，在弹出元素的时候使用阻塞命令来实现阻塞队列。  

<!-- 
&emsp; List 提供了两个阻塞的弹出操作：BLPOP/BRPOP，可以设置超时时间。  

        BLPOP：BLPOP key1 timeout 移出并获取列表的第一个元素， 如果列表没有元素 会阻塞列表直到等待超时或发现可弹出元素为止。  
        BRPOP：BRPOP key1 timeout 移出并获取列表的最后一个元素， 如果列表没有元 素会阻塞列表直到等待超时或发现可弹出元素为止。  

    &emsp; 队列：先进先出：rpush blpop，左头右尾，右边进入队列，左边出队列。  
    &emsp; 栈：先进后出：rpush brpop   
-->

* 栈  
&emsp; 由于列表存储的是有序字符串，满足队列的特点，也就能满足栈先进后出的特点，使用lpush+lpop或者rpush+rpop实现栈。  
* 文章列表  
&emsp; <font color = "lime">每个用户有属于自己的文章列表，现需要分页展示文章列表。此时可以考虑使用列表，因为列表不但是有序的，同时支持按照索引范围获取元素。</font>  
&emsp; 使用列表类型保存和获取文章列表会存在两个问题。第一，如果每次分页获取的文章个数较多，需要执行多次hgetall操作，此时可以考虑使用Pipeline批量获取，或者考虑将文章数据序列化为字符串类型，使用mget批量获取。第二，分页获取文章列表时，lrange命令在列表两端性能较好，但是如果列表较大，获取列表中间范围的元素性能会变差，此时可以考虑将列表做二级拆分，或者使用Redis3.2的quicklist内部编码实现，它结合ziplist和linkedlist的特点，获取列表中间范围的元素时也可以高效完成。  

### 1.2.5. Set集合
&emsp; 集合类型也可以保存多个字符串元素，与列表不同的是，**集合中不允许有重复元素并且集合中的元素是无序的。**一个集合最多可以存储2^32-1个元素。    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-65.png)  


&emsp; **Set集合的交差并的计算复杂度很高，如果数据量很大的情况下，可能会造成Redis的阻塞。**  
&emsp; 那么如何规避阻塞呢？建议如下：  

* 在Redis集群中选一个从库专门负责聚合统计，这样就不会阻塞主库和其他的从库了
* 将数据交给客户端，由客户端进行聚合统计。


#### 1.2.5.1. 常用操作
&emsp; 可以分为集合内操作、集合间操作。  

#### 1.2.5.2. 使用场景
&emsp; **使用场景：**  

* 抽奖(随机数)  
&emsp; 集合有两个命令支持获取随机数，分别是：

    * 随机获取count个元素，集合元素个数不变  
    &emsp; srandmember key [count]  

    * 随机弹出count个元素，元素从集合弹出，集合元素个数改变  
    &emsp; spop key [count]  
    
    &emsp; 用户点击抽奖按钮，参数抽奖，将用户编号放入集合，然后抽奖，分别抽一等奖、二等奖，如果已经抽中一等奖的用户不能参数抽二等奖则使用spop，反之使用srandmember。  
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
&emsp; 获取交集(intersection ) sinter set1 set2  
&emsp; 获取并集 sunion set1 set2  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-68.png)  
&emsp; iPhone11 上市了。 sadd brand:apple iPhone11  
&emsp; sadd brand:ios iPhone11  
&emsp; sad screensize:6.0-6.24 iPhone11  
&emsp; sad screentype:lcd iPhone11  

    &emsp; 筛选商品，苹果的，iOS的，屏幕在6.0-6.24之间的，屏幕材质是LCD屏幕  
    &emsp; sinter brand:apple brand:ios screensize:6.0-6.24 screentype:lcd  
* 用户关注、推荐模型  

### 1.2.6. ZSet有序集合  
<!-- 
&emsp; 命令：  
Redis 数据类型及应用场景——zset
https://www.jianshu.com/p/0cccf031da00
-->
&emsp; sorted set，有序的集合，每个元素有个 score。有序集合中的元素不能重复。可以排序，它给每个元素设置一个score作为排序的依据。最多可以存储2^32-1个元素。  
&emsp; score可以重复。score 相同时，按照 key 的 ASCII 码排序。  
&emsp; 有序集合(sort set)是在集合类型的基础上为每个元素关联一个分数，不仅可以完成插入，删除和判断元素是否存在等集合操作，还能够获得分数最高(或最低)的前N个元素，获得指定分数范围内的元素等分数有关的操作。虽然集合中每个元素都是不相同的，但是分数可以相同。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-70.png)  

#### 1.2.6.1. 常用操作
&emsp; 可以分为集合内操作、集合间操作。  
&emsp; Sorted Set分页查询命令ZRANGEBYSCORE。  


#### 1.2.6.2. 使用场景
&emsp; 使用场景  
<!-- 
读懂才会用：Redis ZSet 的几种使用场景
https://zhuanlan.zhihu.com/p/147912757
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-104.png)  

* 排行榜  
&emsp; 用户发布了n篇文章，其他人看到文章后给喜欢的文章点赞，使用score来记录点赞数，有序集合会根据score排行。流程如下   

&emsp; 用户发布一篇文章，初始点赞数为0，即score为0  

    zadd user:article 0 a  

&emsp; 有人给文章a点赞，递增1  

    zincrby user:article 1 a  

&emsp; 查询点赞前三篇文章  

    zrevrangebyscore user:article 0 2  

&emsp; 查询点赞后三篇文章  

    zrangebyscore user:article 0 2  

* 延迟消息队列  
&emsp; 下单系统，下单后需要在15分钟内进行支付，如果15分钟未支付则自动取消订单。将下单后的十五分钟后时间作为score，订单作为value存入redis，消费者轮询去消费，如果消费的大于等于这笔记录的score，则将这笔记录移除队列，取消订单。  

#### 1.2.6.3. 实现多维排序  
&emsp; 排行榜榜单的维度可能是多个方面的：按照时间、按照播 放数量、按照获得的赞数。  
&emsp; **<font color = "lime">如何借助ZSet实现多维排序? </font>**  
&emsp; ZSet默认情况下只能根据一个因子score进行排序。如此一来，局限性就很大，举个例子：热门排行榜需要按照下载量&最近更新时间排序，即类似数据库中的ORDER BY download_count, update_time DESC。那这样的需求如果用Redis的ZSet实现呢？  
&emsp; 事实上很简单，思路就是<font color = "red">将涉及排序的多个维度的列通过一定的方式转换成一个特殊的列</font>，即result = function(x, y, z)，即x，y，z是三个排序因子，例如下载量、时间等，通过自定义函数function()计算得到result，将result作为ZSet中的score的值，就能实现任意维度的排序需求了。  


### 1.2.7. 数据结构对比
&emsp; **数据结构对比：**  

|数据结构 |是否允许重复元素 |是否有序 |有序实现方式| 
|---|---|---|---|
|列表 list| 是 |是 |<font color = "red">索引下标</font>| 
|集合 set |否 |否| 无 |
|有序集合 zset |否 |是 |分值 score|