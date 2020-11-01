
<!-- TOC -->

- [1. Redis](#1-redis)
    - [1.1. Redis简介](#11-redis简介)
    - [1.2. Redis的数据类型](#12-redis的数据类型)
        - [1.2.1. String](#121-string)
        - [1.2.2. Hash](#122-hash)
        - [1.2.3. List](#123-list)
        - [1.2.4. Set](#124-set)
        - [1.2.5. ZSet](#125-zset)
            - [1.2.5.1. 实现多维排序](#1251-实现多维排序)

<!-- /TOC -->

<!--

Pipeline 有什么好处，为什么要用pipeline？  
答：可以将多次 IO 往返的时间缩减为一次，前提是 pipeline 执行的指令之间没有因果相关性。使用 redis-benchmark 进行压测的时候可以发现影响 redis 的 QPS 峰值的一个重要因素是 pipeline 批次指令的数目。 


Redis 如何做内存优化？

尽可能使用散列表（ hashes）， 散列表（ 是说散列表里面存储的数少） 使用 的内存非常小， 所以你应该尽可能的将你的数据模型抽象到一个散列表里面。比如 你的 web 系统中有一个用户对象， 不要为这个用户的名称， 姓氏， 邮箱， 密码设置 单独的 key,而是应该把这个用户的所有信息存储到一张散列表里。  


假如 Redis 里面有 1 亿个key，其中有 10w 个key 是以某个固定的已知的前缀开头的，如果将它们全部找出来？
答： 使用 keys 指令可以扫出指定模式的 key 列表。  
对方接着追问： 如果这个 redis 正在给线上的业务提供服务， 那使用 keys 指令会有什么问题？  

这个时候你要回答 redis 关键的一个特性：redis 的单线程的。keys 指令会导致线程阻塞一段时间， 线上服务会停顿， 直到指令执行完毕， 服务才能恢复。这个时候可以使用scan 指令， scan 指令可以无阻塞的提取出指定模式的 key 列表， 但是会有一定的重复概率， 在客户端做一次去重就可以了， 但是整体所花费的时间会比直接用 keys 指令长。   


使用过 Redis 做异步队列么，你是怎么用的？

答：一般使用 list 结构作为队列，rpush 生产消息，lpop 消费消息。当 lpop 没有消息的时候， 要适当 sleep 一会再重试。

如果对方追问可不可以不用 sleep 呢？

list 还有个指令叫 blpop，在没有消息的时候，它会阻塞住直到消息到来。如果对方追问能不能生产一次消费多次呢？ 使用 pub/sub 主题订阅者模式， 可以实现1:N 的消息队列。

如果对方追问 pub/sub 有什么缺点？

在消费者下线的情况下，生产的消息会丢失，得使用专业的消息队列如 RabbitMQ 等。

如果对方追问 redis 如何实现延时队列？

我估计现在你很想把面试官一棒打死如果你手上有一根棒球棍的话， 怎么问的这么详细。但是你很克制，然后神态自若的回答道：使用 sortedset，拿时间戳作为score，消息内容作为 key 调用 zadd 来生产消息，消费者用 zrangebyscore 指令获取 N 秒之前的数据轮询进行处理。到这里， 面试官暗地里已经对你竖起了大拇指。但是他不知道的是此刻你却竖起了中指， 在椅子背后。


Redis五大数据类型使用场景 
https://mp.weixin.qq.com/s/QKFXW-Z4A6hj9yB_V1gb2A

-->



# 1. Redis  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-72.png)  
&emsp; <font color="red">整体参考《Redis开发与运维》，数据结构参考《Redis深度历险：核心原理和应用实践》</font>  

## 1.1. Redis简介  
&emsp; Redis是一个开源，内存存储的数据结构服务器，可用作数据库，高速缓存和消息队列代理。  
1. 支持字符串、哈希表、列表、集合、有序集合，位图，hyperloglogs等丰富的数据类型。  
2. 提供Lua脚本、LRU收回、事务以及不同级别磁盘持久化功能。  
3. 同时通过Redis Sentinel提供高可用，通过Redis Cluster提供自动分区。  

## 1.2. Redis的数据类型  
&emsp; Redis属于<key,value\>形式的数据结构。key和value的最大长度限制是512M。  
1. Redis的key是字符串类型，但是key中不能包括边界字符，不能空格和换行。  
2. Redis的value支持五种基本数据类型<font color = "lime">（注意是数据类型不是数据结构）</font>：String（字符串），Hash（哈希），List（列表），Set（集合）及Zset(sorted set，有序集合)。每个数据类型最多能处理2^32个key。  
3. Redis还有几种高级数据类型：bitmaps、HyperLogLog、geo、Streams（5.0最新版本数据结构）。  
4. Redis提供插件功能使用布隆过滤器。  
5. Redis内部采用对象系统RedisObject构建数据类型。  
6. RedisObject对象系统内部采用多种数据结构构建数据类型。数据结构有：int、raw、embstr（SDS）、linkedlist、ziplist、skiplist、hashtable、inset。  

&emsp; [5种基本类型的API使用](/docs/microService/Redis/RedisAPI.md)  

### 1.2.1. String

&emsp; String可以用来存储字符串、整数、浮点数。  
&emsp; String使用场景: （参考《Redis开发与运维》，书中有使用案例。）  

* 缓存功能
* 共享Session
* 全局ID
* 计数  
* 限速  

### 1.2.2. Hash
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-58.png)  
&emsp; Hash存储键值对的无序散列表。  

&emsp; <font color = "lime">Hash与String同样是存储字符串（存储单个字符串时使用String；存储对象时使用Hash，勿将对象序列化后存String类型），它们的主要区别：</font>  
1. 把所有相关的值聚集到一个 key 中，节省内存空间  
2. 只使用一个 key，减少 key 冲突  
3. 当需要批量获取值的时候，只需要使用一个命令，减少内存/IO/CPU 的消耗 Hash 

&emsp; **不适合的场景：** 
1. Field 不能单独设置过期时间  
2. 没有bit操作  
3. 需要考虑数据量分布的问题（value 值非常大的时候，无法分布到多个节点）  

&emsp; **使用场景：**  
* 存储对象类型的数据  
&emsp; 比如对象或者一张表的数据，比String节省了更多key的空间，也更加便于集中管理。  
* 购物车功能  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-60.png)  
&emsp; key：用户 id；field：商品 id；value：商品数量。  
&emsp; +1：hincr。-1：hdecr。删除：hdel。全选：hgetall。商品数：hlen。  

### 1.2.3. List  
 
&emsp; 存储有序的字符串（从左到右），元素可以重复。可以充当队列和栈的角色。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-61.png)  

&emsp; 使用场景：  

* 消息队列  
&emsp; List 提供了两个阻塞的弹出操作：BLPOP/BRPOP，可以设置超时时间。  

        BLPOP：BLPOP key1 timeout 移出并获取列表的第一个元素， 如果列表没有元素 会阻塞列表直到等待超时或发现可弹出元素为止。  
        BRPOP：BRPOP key1 timeout 移出并获取列表的最后一个元素， 如果列表没有元 素会阻塞列表直到等待超时或发现可弹出元素为止。  

    &emsp; 队列：先进先出：rpush blpop，左头右尾，右边进入队列，左边出队列。  
    &emsp; 栈：先进后出：rpush brpop   

* 文章列表  
&emsp; <font color = "lime">每个用户有属于自己的文章列表，现需要分页展示文章列表。此时可以考虑使用列表，因为列表不但是有序的，同时支持按照索引范围获取元素。</font>  
&emsp; 使用列表类型保存和获取文章列表会存在两个问题。第一，如果每次分页获取的文章个数较多，需要执行多次hgetall操作，此时可以考虑使用Pipeline批量获取，或者考虑将文章数据序列化为字符串类型，使用mget批量获取。第二，分页获取文章列表时，lrange命令在列表两端性能较好，但是如果列表较大，获取列表中间范围的元素性能会变差，此时可以考虑将列表做二级拆分，或者使用Redis3.2的quicklist内部编码实现，它结合ziplist和linkedlist的特点，获取列表中间范围的元素时也可以高效完成。  

### 1.2.4. Set
&emsp; 存储String类型的无序集合，最大存储数量2^32-1（40 亿左右）。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-65.png)  

&emsp; 使用场景：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-103.png)  

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

    &emsp; 筛选商品，苹果的，iOS的，屏幕在6.0-6.24之间的，屏幕材质是LCD屏幕  
    &emsp; sinter brand:apple brand:ios screensize:6.0-6.24 screentype:lcd  
* 用户关注、推荐模型  

### 1.2.5. ZSet  

<!-- 
Redis 数据类型及应用场景——zset
https://www.jianshu.com/p/0cccf031da00
读懂才会用：Redis ZSet 的几种使用场景
https://zhuanlan.zhihu.com/p/147912757
-->

&emsp; sorted set，有序的 set，每个元素有个 score。 有序集合中的元素不能重复，但是score可以重复。score 相同时，按照 key 的 ASCII 码排序。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-70.png)  

&emsp; 数据结构对比： 

|数据结构 |是否允许重复元素 |是否有序 |有序实现方式| 
|---|---|---|---|
|列表 list| 是 |是 |<font color = "red">索引下标</font>| 
|集合 set |否 |否| 无 |
|有序集合 zset |否 |是 |分值 score|

&emsp; 使用场景  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-104.png)  

* 排行榜  
&emsp; 排行榜榜单的维度可能是多个方面的：按照时间、按照播 放数量、按照获得的赞数。 

#### 1.2.5.1. 实现多维排序  
&emsp; **<font color = "lime">如何借助ZSet实现多维排序? </font>**  
&emsp; ZSet默认情况下只能根据一个因子score进行排序。如此一来，局限性就很大，举个例子：热门排行榜需要按照下载量&最近更新时间排序，即类似数据库中的ORDER BY download_count, update_time DESC。那这样的需求如果用Redis的ZSet实现呢？  
&emsp; 事实上很简单，思路就是<font color = "red">将涉及排序的多个维度的列通过一定的方式转换成一个特殊的列</font>，即result = function(x, y, z)，即x，y，z是三个排序因子，例如下载量、时间等，通过自定义函数function()计算得到result，将result作为ZSet中的score的值，就能实现任意维度的排序需求了。  

