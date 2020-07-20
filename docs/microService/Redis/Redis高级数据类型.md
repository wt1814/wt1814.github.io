

<!-- TOC -->

- [1. 前言：网页流量统计里的PV、UV](#1-前言网页流量统计里的pvuv)
- [2. Bitmaps，位图](#2-bitmaps位图)
    - [2.1. 应用场景](#21-应用场景)
    - [2.2. 操作命令](#22-操作命令)
- [3. HyperLogLog](#3-hyperloglog)
    - [3.1. HyperLogLog用作基数统计](#31-hyperloglog用作基数统计)
    - [3.2. HyperLogLog原理](#32-hyperloglog原理)
    - [3.3. HyperLogLog的使用](#33-hyperloglog的使用)
- [4. Geospatial](#4-geospatial)
- [5. Streams](#5-streams)
- [6. Redis中的布隆过滤器](#6-redis中的布隆过滤器)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-73.png)  


# 1. 前言：网页流量统计里的PV、UV
&emsp; PV（Page View）访问量, 即页面浏览量或点击量，衡量网站用户访问的网页数量；在一定统计周期内用户每打开或刷新一个页面就记录1次，多次打开或刷新同一页面则浏览量累计。  
&emsp; UV（Unique Visitor）独立访客，统计1天内访问某站点的用户数(以cookie为依据)；访问网站的一台电脑客户端为一个访客。可以理解成访问某网站的电脑的数量。网站判断来访电脑的身份是通过来访电脑的cookies实现的。如果更换了IP后但不清除cookies，再访问相同网站，该网站的统计中UV数是不变的。如果用户不保存cookies访问、清除了cookies或者更换设备访问，计数会加1。00:00-24:00内相同的客户端多次访问只计为1个访客。 
    
  
 
--------------
# 2. Bitmaps，位图  
&emsp; Bitmaps 是在字符串类型上面定义的位操作。一个字节由 8 个二进制位组成。每个二进制位只能存储0或1。   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-71.png)  

## 2.1. 应用场景

* <font color = "red">各种实时分析，例如在线用户统计。</font>
* <font color = "red">用户访问统计。</font>

<!-- 
如果统计 PV(浏览量，用户每点一次记录一次)，那非常好办，给每个页面配置一个独立的 Redis 计数器就可以了，把这个计数器的 key 后缀加上当天的日期。这样每来一个请求，就执行 INCRBY 指令一次，最终就可以统计出所有的 PV 数据了。
-->
<!-- 
存储与对象ID关联的布尔信息。  
&emsp; 例如，记录访问网站的用户的最长连续时间。开始计算从0开始的天数，就是网站公开的那天，每次用户访问网站时通过SETBIT命令设置bit为1，可以简单的用当前时间减去初始时间并除以3600*24（结果就是网站公开的第几天）当做这个bit的位置。  
&emsp; 这种方法对于每个用户，都有存储每天的访问信息的一个很小的string字符串。通过BITCOUN就能轻易统计某个用户历史访问网站的天数。另外通过调用BITPOS命令，或者客户端获取并分析这个bitmap，就能计算出最长停留时间。  
-->

&emsp; **优点与缺点：**  
&emsp; Bitmaps的最大优点就是存储信息时可以节省大量的空间。例如在一个系统中，不同的用户被一个增长的用户ID表示。40亿（2^32≈40亿）用户只需要512M内存就能记住某种信息，例如用户是否登录过。  

## 2.2. 操作命令
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

# 3. HyperLogLog  
<!-- 
https://mp.weixin.qq.com/s/EF1cgBlJB3U37oZm3KgQvQ
-->

## 3.1. HyperLogLog用作基数统计  
&emsp; 基数统计(Cardinality Counting) 通常是用来统计一个集合中不重复的元素个数。例如：**<font color = "red">统计每个网页的 UV(独立访客，每个用户每天只记录一次，需要对每天对浏览去重) 。</font>**  
&emsp; HyperLogLog可用于基数统计。Hyper指的是超级。**<font color = "red">Hyperloglog提供不精确的去重计数功能，HyperLogLog适于做大规模数据的去重统计。</font>**     

&emsp; **HyperLogLog优点与缺点：**  

* 优点：占用内存极小，对于一个key，只需要12kb。  
* 缺点：查询指定用户的时候，可能会出错，毕竟存的不是具体的数据。总数也存在一定的误差。 

## 3.2. HyperLogLog原理  
&emsp; HyperLogLog 的算法和思想  
&emsp; Redis 中的 HyperLogLog 实现  

## 3.3. HyperLogLog的使用  
&emsp; **操作命令：**  
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

        
# 4. Geospatial
&emsp; 可以用来保存地理位置，并作位置距离计算或者根据半径计算位置等。  

# 5. Streams  
&emsp; Redis5.0 推出的数据类型。支持多播的可持久化的消息队列，用于实现发布订阅功能，借 鉴了 kafka 的设计。  

# 6. Redis中的布隆过滤器  
&emsp; 之前的布隆过滤器可以使用Redis中的位图操作实现，直到Redis4.0版本提供了插件功能，Redis官方提供的布隆过滤器才正式登场。布隆过滤器作为一个插件加载到Redis Server中，就会给Redis提供了强大的布隆去重功能。  

<!-- 
详细解析Redis中的布隆过滤器及其应用
https://mp.weixin.qq.com/s/h7K7w9XBYRk7NApRV9evYA
Redis亿级数据过滤和布隆过滤器
https://mp.weixin.qq.com/s/3TcNbNNobn2QEJFat-f90A
-->

