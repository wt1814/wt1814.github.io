

<!-- TOC -->

- [1. ~~Redis内存~~](#1-redis内存)
    - [1.1. 内存设置](#11-内存设置)
    - [1.2. 内存淘汰策略](#12-内存淘汰策略)
        - [1.2.1. Redis内存淘汰使用的算法(淘汰机制)](#121-redis内存淘汰使用的算法淘汰机制)
            - [1.2.1.1. TTL](#1211-ttl)
            - [1.2.1.2. Redis中的LRU算法](#1212-redis中的lru算法)
            - [1.2.1.3. Redis中的LFU算法](#1213-redis中的lfu算法)
        - [1.2.2. 内存淘汰策略](#122-内存淘汰策略)
            - [1.2.2.1. ★★★内存淘汰策略选择](#1221-★★★内存淘汰策略选择)
            - [1.2.2.2. 获取及设置内存淘汰策略](#1222-获取及设置内存淘汰策略)
    - [1.3. 内存优化](#13-内存优化)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 内存设置：默认情况下，在32位OS中，Redis最大使用3GB的内存；在64位OS中则没有限制。  
&emsp; 生产上内存设置：一般推荐redis设置内存为最大物理内存的四分之三；  
1. **Redis内存淘汰使用的算法：**  
&emsp; Redis内存淘汰使用的算法有4种： 
    * random，随机删除。  
    * TTL，删除过期时间最少的键。  
    * <font color = "clime">LRU，Least Recently Used：最近最少使用（访问时间）。</font>判断最近被使用的时间，离目前最远的数据优先被淘汰。  
    &emsp; **<font color = "red">`如果基于传统LRU算法实现，Redis LRU会有什么问题？需要额外的数据结构存储，消耗内存。`</font>**  
    &emsp; **<font color = "blue">Redis LRU对传统的LRU算法进行了改良，通过`随机采样`来调整算法的精度。</font>** 如果淘汰策略是LRU，则根据配置的采样值maxmemory_samples(默认是 5 个)，随机从数据库中选择m个key，淘汰其中热度最低的key对应的缓存数据。所以采样参数m配置的数值越大，就越能精确的查找到待淘汰的缓存数据，但是也消耗更多的CPU计算，执行效率降低。  
    * <font color = "clime">LFU，Least Frequently Used，最不常用（访问频率），4.0版本新增。</font>  
2. **~~内存淘汰策略选择：~~**  
&emsp; **<font color = "clime">volatile和allkeys规定了是对已设置过期时间的key淘汰数据还是从全部key淘汰数据。volatile-xxx策略只会针对带过期时间的key进行淘汰，allkeys-xxx策略会对所有的key进行淘汰。</font>**  
    * `noeviction（默认策略）`： 不会删除任何数据，拒绝所有写入操作并返回客户端错误消息（error）OOM command not allowed when used memory，此时 Redis 只响应删和读操作。  
    * `如果只是拿Redis做缓存，那应该使用allkeys-xxx`，客户端写缓存时不必携带过期时间。  
    * 如果还想同时使用Redis的`持久化`功能，那就使用`volatile-xxx`策略，这样可以保留没有设置过期时间的key，它们是永久的key不会被LRU算法淘汰。`  

    1. `如果数据呈现幂律分布，也就是一部分数据访问频率高，一部分数据访问频率低，或者无法预测数据的使用频率时，则使用allkeys-lru/allkeys-lfu。` 
    2. 如果数据呈现平等分布，也就是所有的数据访问频率都相同，则使用allkeys-random。  
    5. 由于设置expire会消耗额外的内存，如果计划避免Redis内存在此项上的浪费，可以选用allkeys-lru/volatile-lfu策略，这样就可以不再设置过期时间，高效利用内存了。  
    3. 如果研发者需要通过设置不同的ttl来判断数据过期的先后顺序，此时可以选择volatile-ttl策略。
    4. 如果希望一些数据能长期被保存，而一些数据可以被淘汰掉，选择volatile-lru/volatile-lfu或volatile-random都是比较不错的。

<!-- 

http://www.inspinia.net/a/26471.html?action=onClick
-->


# 1. ~~Redis内存~~  
## 1.1. 内存设置  
&emsp; <font color = "red">如果大量非过期key堆积在内存里，导致redis内存块耗尽了。redis会采用内存淘汰机制。</font>Redis的内存淘汰策略，是指当内存使用达到最大内存极限时，需要使用淘汰算法来决定清理掉哪些数据，以保证新数据的存入。  
&emsp; **<font color = "clime">默认情况下，在32位OS中，Redis最大使用3GB的内存；在64位OS中则没有限制。</font>**  
&emsp; **在使用Redis时，应该对数据占用的最大空间有一个基本准确的预估，并为Redis设定最大使用的内存。否则在64位OS中Redis会无限制地占用内存(当物理内存被占满后会使用swap空间)，容易引发各种各样的问题。**  

&emsp; **Redis内存设置：**  
1. 通过配置文件配置：  
&emsp; 通过在Redis安装目录下面的redis.conf配置文件中添加以下配置设置内存大小  

        //设置Redis最大占用内存大小为100M
        maxmemory 100mb
    &emsp; redis的配置文件不一定使用的是安装目录下面的redis.conf文件，启动redis服务的时候是可以传一个参数指定redis的配置文件的  

2. 通过命令修改：  
&emsp; Redis支持运行时通过命令动态修改内存大小  

        //设置Redis最大占用内存大小为100M  
        127.0.0.1:6379> config set maxmemory 100mb  
        //获取设置的Redis能使用的最大内存大小  
        127.0.0.1:6379> config get maxmemory

## 1.2. 内存淘汰策略  
&emsp; 官网描述：https://redis.io/topics/lru-cache  

### 1.2.1. Redis内存淘汰使用的算法(淘汰机制) 
&emsp; Redis内存淘汰使用的算法有：  
* random，随机删除。  
* TTL，删除过期时间最少的键。  
* <font color = "clime">LRU，Least Recently Used：最近最少使用。</font>判断最近被使用的时间，离目前最远的数据优先被淘汰。  
* <font color = "clime">LFU，Least Frequently Used，最不常用，4.0版本新增。</font>  

#### 1.2.1.1. TTL  
&emsp; Redis数据集数据结构中保存了键值对过期时间的表。与LRU数据淘汰机制类似，<font color = "red">TTL数据淘汰机制中会先从过期时间的表中随机挑选几个键值对，取出其中ttl ***的键值对淘汰。</font>同样，<font color = "clime">TTL淘汰策略并不是面向所有过期时间的表中最快过期的键值对，而只是随机挑选的几个键值对。</font>  

#### 1.2.1.2. Redis中的LRU算法  
&emsp; **<font color = "clime">如果基于传统LRU算法实现，Redis LRU会有什么问题？需要额外的数据结构存储，消耗内存。</font>**  
&emsp; <font color = "red">Redis LRU对传统的LRU算法进行了改良，通过随机采样来调整算法的精度。</font>如果淘汰策略是LRU，则根据配置的采样值maxmemory_samples(默认是 5 个)，随机从数据库中选择m个key，淘汰其中热度最低的key对应的缓存数据。所以采样参数m配置的数值越大，就越能精确的查找到待淘汰的缓存数据，但是也消耗更多的CPU计算，执行效率降低。  

&emsp; 如何找出热度最低的数据？  
&emsp; Redis中所有对象结构都有一个lru字段，且使用了unsigned的低24位，这个字段用来记录对象的热度。对象被创建时会记录lru值。在被访问的时候也会更新lru的值。但是不是获取系统当前的时间戳，而是设置为全局变量server.lruclock的值。  

#### 1.2.1.3. Redis中的LFU算法  
&emsp; LFU算法是Redis4.0里面新加的一种淘汰策略。 **<font color = "red">它的全称是Least Frequently Used最不长用。它的核心思想是根据key的最近被访问的频率进行淘汰，很少被访问的优先被淘汰，被访问的多的则被留下来。</font>**  
&emsp; LFU算法能更好的表示一个key被访问的热度。假如使用的是LRU算法，一个key很久没有被访问到，只刚刚是偶尔被访问了一次，那么它就被认为是热点数据，不会被淘汰，而有些key将来是很有可能被访问到的则被淘汰了。  
&emsp; 如果使用LFU算法则不会出现这种情况，因为使用一次并不会使一个key成为热点数据。  

<!--- 
https://stor.51cto.com/art/201904/594773.htm
-->

### 1.2.2. 内存淘汰策略 
&emsp; Redis3.0版本支持的淘汰策略有6种。Redis4.0新增LFU算法支持的2种。    

* volatile-lru：从已设置过期时间的key中挑选最近最少使用的数据淘汰。  
* volatile-ttl：从已设置过期时间的key中挑选将要过期的数据淘汰。  
* volatile-random：从已设置过期时间的key中任意选择数据淘汰。  
* volatile-lfu：在设置了过期时间的key中使用LFU算法淘汰key  

* allkeys-lru：在所有的key中挑选最近最少使用的数据淘汰。  
* allkeys-random：在所有的key中任意选择数据淘汰。  
* allkeys-lfu：在所有的key中挑选最不常用用的数据淘汰  

* no-enviction(驱逐)：禁止驱逐数据，永不回收。redis默认不采用no-enviction，直接返回错误。  

|策略 |含义|
|---|---|
|volatile-lru |根据LRU算法删除设置了超时属性(expire)的键，直到腾出足够内存为止。如果没有可删除的键对象，回退到noeviction策略。|
|allkeys-lru|根据LRU算法删除键，不管数据有没有设置超时属性，直到腾出足够内存为止。|
|volatile-lfu|在带有过期时间的键中选择最不常用的。| 
|allkeys-lfu|在所有的键中选择最不常用的，不管数据有没有设置超时属性。| 
|volatile-random|在带有过期时间的键中随机选择。 allkeys-random 随机删除所有键，直到腾出足够内存为止。| 
|volatile-ttl|根据键值对象的ttl属性，删除最近将要过期数据。如果没有，回退到 noeviction 策略。| 
|noeviction |默认策略，不会删除任何数据，拒绝所有写入操作并返回客户端错误信息(error)OOM command not allowed when used memory，此时Redis只响应读操作。|


&emsp; **<font color = "clime">注：volatile和allkeys规定了是对已设置过期时间的key淘汰数据还是从全部key淘汰数据。</font>**  

#### 1.2.2.1. ★★★内存淘汰策略选择  
&emsp; **内存淘汰策略选择：**  
1. 如果数据呈现幂律分布，也就是一部分数据访问频率高，一部分数据访问频率低，或者 **无法预测数据的使用频率时，则使用allkeys-lru/allkeys-lfu。**  
2. 如果数据呈现平等分布，也就是所有的数据访问频率都相同，则使用allkeys-random。
3. 如果研发者需要通过设置不同的ttl来判断数据过期的先后顺序，此时可以选择volatile-ttl策略。
4. 如果希望一些数据能长期被保存，而一些数据可以被淘汰掉，选择volatile-lru/volatile-lfu或volatile-random都是比较不错的。
5. 由于设置expire会消耗额外的内存，如果计划避免Redis内存在此项上的浪费，可以选用allkeys-lru/volatile-lfu策略，这样就可以不再设置过期时间，高效利用内存了。  

&emsp; **<font color = "bule">volatile-xxx策略只会针对带过期时间的key进行淘汰，allkeys-xxx策略会对所有的key进行淘汰。</font>**  

* **<font color = "bule">如果只是拿Redis做缓存，那应该使用allkeys-xxx，客户端写缓存时不必携带过期时间。</font>**  
* **<font color = "bule">如果还想同时使用Redis的持久化功能，那就使用volatile-xxx策略，这样可以保留没有设置过期时间的key，它们是永久的key不会被LRU算法淘汰。</font>**  

<!-- 
&emsp; **Redis中设置置换策略：**  
&emsp; 在redis.conf配置文件中或通过CONFIG SET动态修改最大缓存maxmemory、置换策略maxmemory-policy。  
-->
#### 1.2.2.2. 获取及设置内存淘汰策略  
&emsp; 获取当前内存淘汰策略：  

    127.0.0.1:6379> config get maxmemory-policy

&emsp; 通过配置文件设置淘汰策略(修改redis.conf文件)：  

    maxmemory-policy allkeys-lru
    
&emsp; 通过命令修改淘汰策略：  

    127.0.0.1:6379> config set maxmemory-policy allkeys-lru

## 1.3. 内存优化  
&emsp; 参考《Redis开发与运维》第8.3章  