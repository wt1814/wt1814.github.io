
<!-- TOC -->

- [1. Redis的API](#1-redis的api)
    - [1.1. Redis客户端](#11-redis客户端)
    - [1.2. Key操作命令](#12-key操作命令)
        - [1.2.1. 使用scan代替keys指令](#121-使用scan代替keys指令)
        - [1.2.2. Redis中的批量删除数据库中的Key](#122-redis中的批量删除数据库中的key)
    - [1.3. String操作命令](#13-string操作命令)
    - [1.4. Hash操作命令](#14-hash操作命令)
    - [1.5. List操作命令](#15-list操作命令)
    - [1.6. Set操作命令](#16-set操作命令)
    - [1.7. Zset操作命令](#17-zset操作命令)

<!-- /TOC -->

**《Redis开发与运维》**  

# 1. Redis的API

## 1.1. Redis客户端
&emsp; 官网推荐的 Java 客户端有 3 个 Jedis，Redisson 和 Luttuce。  

* Jedis，轻量，简洁，便于集成和改造。  
* Lettuce   
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


## 1.2. Key操作命令  
......

### 1.2.1. 使用scan代替keys指令  
......
<!-- 
在RedisTemplate中使用scan代替keys指令 
https://mp.weixin.qq.com/s/8hBrUb1Tn6cuSzQITCDReQ
-->

### 1.2.2. Redis中的批量删除数据库中的Key  
&emsp; Redis中有删除单个Key的指令 DEL，但似乎没有批量删除 Key 的指令，不过可以借助 Linux 的 xargs 指令来完成这个动作。  
<!-- 
https://www.cnblogs.com/DreamDrive/p/5772198.html
 熬了一个通宵终于把Key删完了 
 https://mp.weixin.qq.com/s/xb6USb3FLIDDloUPoqBnMw
-->

## 1.3. String操作命令  

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


## 1.4. Hash操作命令  
......


## 1.5. List操作命令  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-64.png)  


## 1.6. Set操作命令  
&emsp; 可以分为集合内操作、集合间操作。



## 1.7. Zset操作命令  
&emsp; 可以分为集合内操作、集合间操作。  




