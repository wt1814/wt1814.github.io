

<!-- TOC -->

- [1. Redis内存淘汰](#1-redis内存淘汰)
    - [1.1. Redis过期键删除策略](#11-redis过期键删除策略)
        - [1.1.1. Key生存期](#111-key生存期)
        - [1.1.2. 常见的删除策略](#112-常见的删除策略)
            - [1.1.2.1. 定时删除策略（主动淘汰）](#1121-定时删除策略主动淘汰)
            - [1.1.2.2. 惰性删除策略（被动淘汰）](#1122-惰性删除策略被动淘汰)
            - [1.1.2.3. 定期删除策略（主动淘汰）](#1123-定期删除策略主动淘汰)
        - [1.1.3. Redis使用的过期键删除策略](#113-redis使用的过期键删除策略)
    - [1.2. Redis内存](#12-redis内存)
        - [1.2.1. 内存设置](#121-内存设置)
        - [1.2.2. 内存淘汰策略](#122-内存淘汰策略)
            - [1.2.2.1. redis内存淘汰使用的算法（淘汰机制）](#1221-redis内存淘汰使用的算法淘汰机制)
                - [1.2.2.1.1. TTL](#12211-ttl)
                - [1.2.2.1.2. Redis中的LRU算法](#12212-redis中的lru算法)
                - [1.2.2.1.3. Redis中的LFU算法](#12213-redis中的lfu算法)
            - [1.2.2.2. 内存淘汰策略](#1222-内存淘汰策略)
                - [1.2.2.2.1. ※※※内存淘汰策略选择](#12221-※※※内存淘汰策略选择)

<!-- /TOC -->

# 1. Redis内存淘汰  
&emsp; **<font color = "lime">一句话概述：redis的key有3种删除策略；内存淘汰有4种算法、8种淘汰策略。注意内存淘汰策略的选择。</font>**  
&emsp; **<font color = "lime">注：volatile和allkeys规定了是对已设置过期时间的key淘汰数据还是从全部key淘汰数据。</font>**  

## 1.1. Redis过期键删除策略
### 1.1.1. Key生存期  
&emsp; 在Redis当中，有生存期的key被称为volatile。在创建缓存时，要为给定的key设置生存期，当key过期的时候（生存期为0），它可能会被删除。  
1. 影响生存时间的一些操作：  
&emsp; 生存时间可以通过使用DEL命令来删除整个key来移除，或者被SET和GETSET命令覆盖原来的数据。也就是说，修改key对应value和使用另外相同key和value来覆盖以后，当前数据的生存时间不同。  
&emsp; 比如说，对一个key执行INCR命令，对一个列表进行LPUSH命令，或者对一个哈希表执行HSET命令，这类操作都不会修改key 本身的生存时间。另一方面，如果使用RENAME对一个key进行改名，那么改名后的key的生存时间和改名前一样。  
&emsp; RENAME命令的另一种可能是，尝试将一个带生存时间的key改名成另一个带生存时间的another_key，这时旧的another_key(以及它的生存时间)会被删除，然后旧的key会改名为another_key，因此，新的another_key的生存时间也和原本的key一样。使用PERSIST命令可以在不删除key的情况下，移除key的生存时间，让key重新成为一个persistent key。  

2. 如何更新生存时间：  
&emsp; 可以对一个已经带有生存时间的key执行EXPIRE命令，新指定的生存时间会取代旧的生存时间。过期时间的精度已经被控制在1ms之内，主键失效的时间复杂度是O(1)，EXPIRE和TTL命令搭配使用，TTL可以查看key的当前生存时间。设置成功返回1；当key不存在或者不能为key设置生存时间时，返回0。  

### 1.1.2. 常见的删除策略  
&emsp; 常见的删除策略有3种：定时删除、惰性删除、定期删除。  

#### 1.1.2.1. 定时删除策略（主动淘汰）  
&emsp; <font color = "red">在设置键的过期时间的同时，创建一个定时器，让定时器在键的过期时间来临时，立即执行对键的删除操作。</font>  

&emsp; 优点：对内存非常友好。  
&emsp; 缺点：对CPU时间非常不友好，会占用大量的 CPU 资源去处理过期的 数据，从而影响缓存的响应时间和吞吐量。  

&emsp; 举个例子，如果有大量的命令请求等待服务器处理，并且服务器当前不缺少内存，如果服务器将大量的CPU时间用来删除过期键，那么服务器的响应时间和吞吐量就会受到影响。  
&emsp; 也就是说，如果服务器创建大量的定时器，服务器处理命令请求的性能就会降低，因此Redis目前并没有使用定时删除策略。  

#### 1.1.2.2. 惰性删除策略（被动淘汰）  
&emsp; <font color = "red">只有当访问一个key时，才会判断该key是否已过期，过期则清除。</font>  

&emsp; 优点：对CPU时间非常友好，可以最大化地节省 CPU 资源。  
&emsp; 缺点：<font color = "red">对内存非常不友好，极端情况可能出现大量的过期key没有再次被访问，从而不会被清除，占用大量内存。</font>  

&emsp; 举个例子，如果数据库有很多的过期键，而这些过期键又恰好一直没有被访问到，那这些过期键就会一直占用着宝贵的内存资源，造成资源浪费。  

#### 1.1.2.3. 定期删除策略（主动淘汰）  
&emsp; 定期删除策略是定时删除策略和惰性删除策略的一种整合折中方案。  
&emsp; <font color = "red"> **定期删除策略每隔一段时间执行一次删除过期键操作**</font>，并通过<font color = "lime">限制删除操作执行的时长和频率来减少删除操作对CPU时间的影响</font>，同时，通过定期删除过期键，也有效地减少了因为过期键而带来的内存浪费。  

<!-- 
每隔一定的时间，会扫描一定数量的数据库的 expires 字典中一定数量的 key，并清 除其中已过期的 key。该策略是前两者的一个折中方案。通过调整定时扫描的时间间隔和 每次扫描的限定耗时，可以在不同情况下使得 CPU 和内存资源达到最优的平衡效果。
-->

### 1.1.3. Redis使用的过期键删除策略  
&emsp; <font color = "red">Redis服务器使用的是惰性删除策略和定期删除策略。</font>  

## 1.2. Redis内存
&emsp; <font color = "red">如果大量非过期key堆积在内存里，导致redis内存块耗尽了。redis会采用内存淘汰机制。</font>Redis的内存淘汰策略，是指当内存使用达到最大内存极限时，需要使用淘汰算法来决定清理掉哪些数据，以保证新数据的存入。  

### 1.2.1. 内存设置  
&emsp; 默认情况下，在32位OS中，Redis最大使用3GB的内存，在64位OS中则没有限制。  
&emsp; 在使用Redis时，应该对数据占用的最大空间有一个基本准确的预估，并为Redis设定最大使用的内存。否则在64位OS中Redis会无限制地占用内存（当物理内存被占满后会使用swap空间），容易引发各种各样的问题。  

&emsp; 在redis中，允许用户设置最大使用内存大小server.maxmemory。默认为0，没有指定最大缓存，如果有新的数据添加，超过最大内存，则会使redis崩溃，所以一定要设置。  

&emsp; Redis是基于内存的key-value数据库，因为系统的内存大小有限，所以在使用Redis的时候可以配置Redis能使用的最大的内存大小。  

1. 通过配置文件配置  
&emsp; 通过在Redis安装目录下面的redis.conf配置文件中添加以下配置设置内存大小  

        //设置Redis最大占用内存大小为100M
        maxmemory 100mb
    &emsp; redis的配置文件不一定使用的是安装目录下面的redis.conf文件，启动redis服务的时候是可以传一个参数指定redis的配置文件的  

2. 通过命令修改  
&emsp; Redis支持运行时通过命令动态修改内存大小  

        //设置Redis最大占用内存大小为100M  
        127.0.0.1:6379> config set maxmemory 100mb  
        //获取设置的Redis能使用的最大内存大小  
        127.0.0.1:6379> config get maxmemory

    &emsp; 如果不设置最大内存大小或者设置最大内存大小为0，在64位操作系统下不限制内存大小，在32位操作系统下最多使用3GB内存。  

### 1.2.2. 内存淘汰策略  
&emsp; 官网描述：https://redis.io/topics/lru-cache  

#### 1.2.2.1. redis内存淘汰使用的算法（淘汰机制） 
<!--
Redis内存淘汰策略，看这一篇就够了！
https://www.jianshu.com/p/b1b4eeccc140
https://www.jianshu.com/p/c8aeb3eee6bc
https://stor.51cto.com/art/201904/594773.htm
--> 
&emsp; redis内存淘汰使用的算法有：  
* random，随机删除。  
* TTL
* <font color = "lime">LRU，Least Recently Used：最近最少使用。</font>判断最近被使用的时间，离目前最远的数据优先被淘汰。  
* <font color = "lime">LFU，Least Frequently Used，最不常用，4.0版本新增。</font>  


##### 1.2.2.1.1. TTL  
&emsp; Redis数据集数据结构中保存了键值对过期时间的表。与LRU数据淘汰机制类似，TTL数据淘汰机制中会先从过期时间的表中随机挑选几个键值对，取出其中ttl ***的键值对淘汰。同样，TTL淘汰策略并不是面向所有过期时间的表中最快过期的键值对，而只是随机挑选的几个键值对。  

##### 1.2.2.1.2. Redis中的LRU算法  
&emsp; 如果基于传统LRU算法实现，Redis LRU会有什么问题？需要额外的数据结构存储，消耗内存。  
&emsp; <font color = "red">Redis LRU对传统的LRU算法进行了改良，通过随机采样来调整算法的精度。</font>如果淘汰策略是LRU，则根据配置的采样值maxmemory_samples（默认是 5 个），随机从数据库中选择m个key，淘汰其中热度最低的key对应的缓存数据。所以采样参数m配置的数值越大，就越能精确的查找到待淘汰的缓存数据，但是也消耗更多的CPU计算，执行效率降低。  

&emsp; 如何找出热度最低的数据？  
&emsp; Redis中所有对象结构都有一个lru字段, 且使用了unsigned的低24位，这个字段用来记录对象的热度。对象被创建时会记录lru值。在被访问的时候也会更新lru的值。但是不是获取系统当前的时间戳，而是设置为全局变量server.lruclock的值。  


##### 1.2.2.1.3. Redis中的LFU算法  
&emsp; LFU算法是Redis4.0里面新加的一种淘汰策略。它的全称是Least Frequently Used最不长用。它的核心思想是根据key的最近被访问的频率进行淘汰，很少被访问的优先被淘汰，被访问的多的则被留下来。  
&emsp; LFU算法能更好的表示一个key被访问的热度。假如使用的是LRU算法，一个key很久没有被访问到，只刚刚是偶尔被访问了一次，那么它就被认为是热点数据，不会被淘汰，而有些key将来是很有可能被访问到的则被淘汰了。  
&emsp; 如果使用LFU算法则不会出现这种情况，因为使用一次并不会使一个key成为热点数据。  


<!--- 
https://stor.51cto.com/art/201904/594773.htm
-->

#### 1.2.2.2. 内存淘汰策略 
&emsp; Redis3.0版本支持的淘汰策略有6种。Redis4.0新增LFU算法支持的2种。    

* volatile-lru：从已设置过期时间的key中挑选最近最少使用的数据淘汰。  
* volatile-ttl：从已设置过期时间的key中挑选将要过期的数据淘汰。  
* volatile-random：从已设置过期时间的key中任意选择数据淘汰。  
* volatile-lfu：在设置了过期时间的key中使用LFU算法淘汰key  

* allkeys-lru：在所有的key中挑选最近最少使用的数据淘汰。  
* allkeys-random：在所有的key中任意选择数据淘汰。  
* allkeys-lfu：在所有的key中挑选最不常用用的数据淘汰  

* no-enviction（驱逐）：禁止驱逐数据，永不回收。redis默认不采用no-enviction，直接返回错误。  

|策略 |含义|
|---|---|
|volatile-lru |根据LRU算法删除设置了超时属性（expire）的键，直到腾出足够内存为止。如果没有可删除的键对象，回退到noeviction策略。|
|allkeys-lru|根据LRU算法删除键，不管数据有没有设置超时属性，直到腾出足够内存为止。|
|volatile-lfu|在带有过期时间的键中选择最不常用的。| 
|allkeys-lfu|在所有的键中选择最不常用的，不管数据有没有设置超时属性。| 
|volatile-random|在带有过期时间的键中随机选择。 allkeys-random 随机删除所有键，直到腾出足够内存为止。| 
|volatile-ttl|根据键值对象的ttl属性，删除最近将要过期数据。如果没有，回退到 noeviction 策略。| 
|noeviction |默认策略，不会删除任何数据，拒绝所有写入操作并返回客户端错误信息（error）OOM command not allowed when used memory，此时Redis只响应读操作。| 
 
&emsp; **<font color = "lime">注：volatile和allkeys规定了是对已设置过期时间的key淘汰数据还是从全部key淘汰数据。</font>**  

##### 1.2.2.2.1. ※※※内存淘汰策略选择  
&emsp; **使用策略规则：**  
1. 如果数据呈现幂律分布，也就是一部分数据访问频率高，一部分数据访问频率低，或者无法预测数据的使用频率时，则使用allkeys-lru/allkeys-lfu。  
2. 如果数据呈现平等分布，也就是所有的数据访问频率都相同，则使用allkeys-random。
3. 如果研发者需要通过设置不同的ttl来判断数据过期的先后顺序，此时可以选择volatile-ttl策略。
4. 如果希望一些数据能长期被保存，而一些数据可以被淘汰掉，选择volatile-lru/volatile-lfu或volatile-random都是比较不错的。
5. 由于设置expire会消耗额外的内存，如果计划避免Redis内存在此项上的浪费，可以选用allkeys-lru/volatile-lfu策略，这样就可以不再设置过期时间，高效利用内存了。  

<!-- 
&emsp; **Redis中设置置换策略：**  
&emsp; 在redis.conf配置文件中或通过CONFIG SET动态修改最大缓存maxmemory、置换策略maxmemory-policy。  
-->
&emsp; **如何获取及设置内存淘汰策略**  
&emsp; 获取当前内存淘汰策略：  

    127.0.0.1:6379> config get maxmemory-policy

&emsp; 通过配置文件设置淘汰策略（修改redis.conf文件）：  

    maxmemory-policy allkeys-lru
    
&emsp; 通过命令修改淘汰策略：  

    127.0.0.1:6379> config set maxmemory-policy allkeys-lru


