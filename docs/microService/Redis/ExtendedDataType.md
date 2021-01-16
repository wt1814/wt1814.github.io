

<!-- TOC -->

- [1. Redis扩展数据类型](#1-redis扩展数据类型)
    - [1.1. 前言：网页流量统计里的PV、UV](#11-前言网页流量统计里的pvuv)
    - [1.2. Bitmap，位图](#12-bitmap位图)
        - [1.2.1. 位图介绍](#121-位图介绍)
        - [1.2.2. Redis Bitmap命令](#122-redis-bitmap命令)
        - [1.2.3. 应用场景](#123-应用场景)
            - [1.2.3.1. 使用场景一：用户签到](#1231-使用场景一用户签到)
            - [1.2.3.2. 使用场景二：统计活跃用户](#1232-使用场景二统计活跃用户)
            - [1.2.3.3. 使用场景三：用户在线状态](#1233-使用场景三用户在线状态)
    - [1.3. HyperLogLog，基数统计](#13-hyperloglog基数统计)
        - [1.3.2. 基数统计](#132-基数统计)
        - [1.3.3. Redis中的基数统计方式](#133-redis中的基数统计方式)
        - [1.3.4. HyperLogLog用作基数统计](#134-hyperloglog用作基数统计)
        - [1.3.5. Redis中HyperLogLog的使用](#135-redis中hyperloglog的使用)
            - [1.3.5.1. HyperLogLog操作命令](#1351-hyperloglog操作命令)
            - [1.3.5.2. Redis中的HyperLogLog原理](#1352-redis中的hyperloglog原理)
    - [1.4. Geospatial地图](#14-geospatial地图)
    - [1.5. Streams消息队列](#15-streams消息队列)
    - [1.6. Redis中的布隆过滤器](#16-redis中的布隆过滤器)
    - [1.7. 小结](#17-小结)

<!-- /TOC -->

&emsp; **<font color = "lime">总结：</font>**  
1. <font color = "lime">Bitmap、HyperLogLog都是作为Redis的Value值。</font>  
2. <font color = "lime">Redis中的Bitmap：key可以为某一天、某一ID，Bitmap中bit可以存储用户的任意信息。所以Redis Bitmap可以用作统计信息。</font>  
3. <font color = "lime">HyperLogLog用于基数统计。</font>  
    * 基数统计是指找出集合中不重复元素，用于去重。  
    * 使用Redis统计集合的基数一般有三种方法，分别是使用Redis的 HashMap，BitMap和HyperLogLog。  
    * HyperLogLog内存空间消耗少，但存在误差0.81%。  
    
# 1. Redis扩展数据类型  
<!-- 

-->
<!-- 
~~
RedisTimeSeries
https://www.yuque.com/happy-coder/qka0of/ekdfzb
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-73.png)  

&emsp; Redis提供了一些扩展数据类型和时序数据库模块：  

* bitmap：基于bit位的存储，每一个bit存储0或1，一般用来进行海量数据的精准判重。  
* HyperLogLog：一般用来对海量数据进行基于概率的基数统计，比如说网站的独立访客数、独立IP数等。  
* Geo：基于地理空间的数据存储，常应用在那些基于位置服务，也就是常说的LBS(Location-Based Service)的应用，比如说打车等生活服务类应用。  
* Stream：消息流，Redis自5.0版本之后，引入了消息队列的机制，也就是Pub/Sub（发布/订阅）机制。  
* RedisTimeSeries：时间数据库模块，主要存储一些跟时间戳相关，需要范围查询，聚合计算等场景的数据集。    

## 1.1. 前言：网页流量统计里的PV、UV
&emsp; PV（Page View）访问量, 即页面浏览量或点击量，衡量网站用户访问的网页数量；在一定统计周期内用户每打开或刷新一个页面就记录1次，多次打开或刷新同一页面则浏览量累计。  
&emsp; UV（Unique Visitor）独立访客，统计1天内访问某站点的用户数(以cookie为依据)；访问网站的一台电脑客户端为一个访客。可以理解成访问某网站的电脑的数量。网站判断来访电脑的身份是通过来访电脑的cookies实现的。如果更换了IP后但不清除cookies，再访问相同网站，该网站的统计中UV数是不变的。如果用户不保存cookies访问、清除了cookies或者更换设备访问，计数会加1。00:00-24:00内相同的客户端多次访问只计为1个访客。 
 
--------------
## 1.2. Bitmap，位图
&emsp; **二值状态统计：**  
&emsp; 二值状态指的是取值0或者1两种；在签到打卡的场景中，只需要记录签到（1）和未签到（0）两种状态，这就是典型的二值状态统计。  
&emsp; 二值状态的统计可以使用Redis的扩展数据类型Bitmap。  

### 1.2.1. 位图介绍    
&emsp; Bitmaps是在字符串类型上面定义的位操作。一个字节由8个二进制位组成。每个二进制位只能存储0或1。   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-97.png)  

&emsp; **优点与缺点：**  
&emsp; Bitmaps的最大优点就是存储信息时可以节省大量的空间。例如在一个系统中，不同的用户被一个增长的用户ID表示。40亿（2^32≈40亿）用户只需要512M内存就能记住某种信息，例如用户是否登录过。  

### 1.2.2. Redis Bitmap命令

&emsp; bit操作被分为两组：  

* 恒定时间的单个bit操作，例如把某个bit设置为0或者1。或者获取某bit的值。  
* 对一组bit的操作。例如给定范围内bit统计（例如人口统计）。  

1. Bits命令：Bits设置和获取通过SETBIT和GETBIT命令。用法如下：  

    SETBIT key offset value #设置或者清空key的value(字符串)在offset处的bit值(只能只0或者1)  
    GETBIT key offset  

    &emsp; 使用实例：  

        127.0.0.1:6380> setbit dupcheck 10 1  
        (integer) 0  
        127.0.0.1:6380> getbit dupcheck 10   
        (integer) 1  

    * SETBIT命令第一个参数是位编号，第二个参数是这个位的值，只能是0或者1。如果bit地址超过当前string长度，会自动增大string。  
    * GETBIT命令指示返回指定位置bit的值。超过范围（寻址地址在目标key的string长度以外的位）的GETBIT总是返回0。  

2. 操作bits组的命令如下：  
    * BITOP执行两个不同string的位操作，包括AND，OR，XOR和NOT。
    * BITCOUNT统计位的值为1的数量。
    * BITPOS寻址第一个为0或者1的bit的位置（寻址第一个为1的bit的位置：bitpos dupcheck 1；寻址第一个为0的bit的位置：bitpos dupcheck 0）。  

### 1.2.3. 应用场景
<!-- 
实际项目开发中有很多业务都适合采用redis的bit来实现。
用户签到场景

每天的日期字符串作为一个key，用户Id作为offset，统计每天用户的签到情况，总的用户签到数
活跃用户数统计

用户日活、月活、留存率等均可以用redis位数组来存储，还是以每天的日期作为key，用户活跃了就写入offset为用户id的位值1。

同理月活也是如此。
用户是否在线以及总在线人数统计

同样是使用一个位数组，用户的id映射偏移量，在线标识为1，下线标识为0。即可实现用户上下线查询和总在线人数的统计
APP内用户的全局消息提示小红点

现在大多数的APP里都有站内信的功能，当有消息的时候，则提示一个小红点，代表用户有新的消息。
-->

* <font color = "red">各种实时分析，例如在线用户统计。</font>
* <font color = "red">用户访问统计。</font>

#### 1.2.3.1. 使用场景一：用户签到  
<!-- 
实现一个签到功能
https://mp.weixin.qq.com/s/pd_wRas4yyx5PsTpIdimgA
-->
&emsp; 很多网站都提供了签到功能(这里不考虑数据落地事宜)，并且需要展示最近一个月的签到情况，可以使用Bitmap。  

    ```php
    <?php
    $redis = new Redis();
    $redis->connect('127.0.0.1');


    //用户uid
    $uid = 1;

    //记录有uid的key
    $cacheKey = sprintf("sign_%d", $uid);

    //开始有签到功能的日期
    $startDate = '2017-01-01';

    //今天的日期
    $todayDate = '2017-01-21';

    //计算offset
    $startTime = strtotime($startDate);
    $todayTime = strtotime($todayDate);
    $offset = floor(($todayTime - $startTime) / 86400);

    echo "今天是第{$offset}天" . PHP_EOL;

    //签到
    //一年一个用户会占用多少空间呢？大约365/8=45.625个字节，好小，有木有被惊呆？
    $redis->setBit($cacheKey, $offset, 1);

    //查询签到情况
    $bitStatus = $redis->getBit($cacheKey, $offset);
    echo 1 == $bitStatus ? '今天已经签到啦' : '还没有签到呢';
    echo PHP_EOL;

    //计算总签到次数
    echo $redis->bitCount($cacheKey) . PHP_EOL;

    /**
    * 计算某段时间内的签到次数
    * 很不幸啊,bitCount虽然提供了start和end参数，但是这个说的是字符串的位置，而不是对应"位"的位置
    * 幸运的是我们可以通过get命令将value取出来，自己解析。并且这个value不会太大，上面计算过一年一个用户只需要45个字节
    * 给我们的网站定一个小目标，运行30年，那么一共需要1.31KB(就问你屌不屌？)
    */
    //这是个错误的计算方式
    echo $redis->bitCount($cacheKey, 0, 20) . PHP_EOL;
    ```

#### 1.2.3.2. 使用场景二：统计活跃用户  
&emsp; 使用时间作为cacheKey，然后用户ID为offset，如果当日活跃过就设置为1  
&emsp; 那么如果计算某几天/月/年的活跃用户呢(暂且约定，统计时间内只有有一天在线就称为活跃)，使用redis的命令   
&emsp; 命令 BITOP operation destkey key [key ...]  
&emsp; 说明：对一个或多个保存二进制位的字符串 key 进行位元操作，并将结果保存到 destkey 上。  
&emsp; 说明：BITOP 命令支持 AND 、 OR 、 NOT 、 XOR 这四种操作中的任意一种参数  

    ```php
    //日期对应的活跃用户
    $data = array(
    '2017-01-10' => array(1,2,3,4,5,6,7,8,9,10),
    '2017-01-11' => array(1,2,3,4,5,6,7,8),
    '2017-01-12' => array(1,2,3,4,5,6),
    '2017-01-13' => array(1,2,3,4),
    '2017-01-14' => array(1,2)
    );

    //批量设置活跃状态
    foreach($data as $date=>$uids) {
    $cacheKey = sprintf("stat_%s", $date);
    foreach($uids as $uid) {
    $redis->setBit($cacheKey, $uid, 1);
    }
    }

    $redis->bitOp('AND', 'stat', 'stat_2017-01-10', 'stat_2017-01-11', 'stat_2017-01-12') . PHP_EOL;
    //总活跃用户：6
    echo "总活跃用户：" . $redis->bitCount('stat') . PHP_EOL;

    $redis->bitOp('AND', 'stat1', 'stat_2017-01-10', 'stat_2017-01-11', 'stat_2017-01-14') . PHP_EOL;
    //总活跃用户：2
    echo "总活跃用户：" . $redis->bitCount('stat1') . PHP_EOL;

    $redis->bitOp('AND', 'stat2', 'stat_2017-01-10', 'stat_2017-01-11') . PHP_EOL;
    //总活跃用户：8
    echo "总活跃用户：" . $redis->bitCount('stat2') . PHP_EOL;
    ```

#### 1.2.3.3. 使用场景三：用户在线状态  
&emsp; 前段时间开发一个项目，对方提供了一个查询当前用户是否在线的接口。不了解对方是怎么做的，自己考虑了一下，使用bitmap是一个节约空间效率又高的一种方法，只需要一个key，然后用户ID为offset，如果在线就设置为1，不在线就设置为0，和上面的场景一样，5000W用户只需要6MB的空间。  

    ```php
    //批量设置在线状态
    $uids = range(1, 500000);
    foreach($uids as $uid) {
    $redis->setBit('online', $uid, $uid % 2);
    }
    //一个一个获取状态
    $uids = range(1, 500000);
    $startTime = microtime(true);
    foreach($uids as $uid) {
    echo $redis->getBit('online', $uid) . PHP_EOL;
    }
    $endTime = microtime(true);
    //在我的电脑上，获取50W个用户的状态需要25秒
    echo "total:" . ($endTime - $startTime) . "s";

    /**
    * 对于批量的获取，上面是一种效率低的办法，实际可以通过get获取到value，然后自己计算
    * 具体计算方法改天再写吧，之前写的代码找不见了。。。
    */
    ```

## 1.3. HyperLogLog，基数统计  
<!-- 
&emsp; <font color = "lime">如果统计 PV(浏览量，用户没点一次记录一次)，给每个页面配置一个独立的Redis计数器就可以了，把这个计数器的key后缀加上当天的日期。</font>这样每来一个请求，就执行INCRBY指令一次，最终就可以统计出所有的PV数据了。  
&emsp; 但是UV不同，它要去重，<font color = "lime">UV要求同一个用户一天之内的多次访问请求只能计数一次。</font>这就要求了每一个网页请求都需要带上用户的ID，无论是登录用户还是未登录的用户，都需要一个唯一ID来标识。<font color = "lime">对于统计UV数据需要基数统计。</font>  
-->
&emsp; HyperLogLog是一种概率数据结构，用来估算数据的基数。    
&emsp; 精确的计算数据集的基数需要消耗大量的内存来存储数据集。在遍历数据集时，判断当前遍历值是否已经存在唯一方法就是将这个值与已经遍历过的值进行一一对比。当数据集的数量越来越大，内存消耗就无法忽视，甚至成了问题的关键。

### 1.3.2. 基数统计  
&emsp; 什么是基数?  
&emsp; 比如数据集 {1, 3, 5, 7, 5, 7, 8}，那么这个数据集的基数集为 {1, 3, 5 ,7, 8}，基数(不重复元素)为5。基数估计就是在误差可接受的范围内，快速计算基数。  
&emsp; **<font color = "clime">基数统计(Cardinality Counting) 通常是用来统计一个集合中不重复的元素个数。</font>** 例如： **<font color = "red">统计每个网页的UV(独立访客，每个用户每天只记录一次，需要对每天对浏览去重) 。</font>**   
<!-- 
数据集可以是网站访客的 IP 地址，E-mail邮箱或者用户ID。
-->

### 1.3.3. Redis中的基数统计方式  
&emsp; 使用Redis统计集合的基数一般有三种方法，分别是使用 Redis 的 HashMap，BitMap 和 HyperLogLog。前两个数据结构在集合的数量级增长时，所消耗的内存会大大增加，但是 HyperLogLog 则不会。  
&emsp; Redis的HyperLogLog 通过牺牲准确率来减少内存空间的消耗，只需要12K内存，在标准误差0.81%的前提下，能够统计2^64个数据。所以 HyperLogLog 是否适合在比如统计日活月活此类的对精度要不不高的场景。

### 1.3.4. HyperLogLog用作基数统计  
&emsp; [HyperLogLog](/docs/java/function/otherStructure.md)可用于基数统计。Hyper指的是超级。 **<font color = "red">Hyperloglog提供不精确的去重计数功能，HyperLogLog适于做大规模数据的去重统计。</font>**   

&emsp; **HyperLogLog优点与缺点：**  

* 优点：占用内存极小，对于一个key，只需要12kb。  
* 缺点：查询指定用户的时候，可能会出错，毕竟存的不是具体的数据。总数也存在一定的误差。 
* 能够使用极少的内存来统计巨量的数据，在 Redis 中实现的 HyperLogLog，只需要12K内存就能统计2^64个数据。
* <font color = "red">计数存在一定的误差，误差率整体较低。标准误差为 0.81% 。</font>
* 误差可以被设置辅助计算因子进行降低。  

### 1.3.5. Redis中HyperLogLog的使用  
#### 1.3.5.1. HyperLogLog操作命令

&emsp; **操作命令：**  
&emsp; Redis Hyperloglog的三个命令：PFADD、PFCOUNT、PFMERGE。  

* PFADD命令用于添加一个新元素到统计中。pfadd key value，将key对应的一个value存入  
* PFCOUNT命令用于获取到目前为止通过PFADD命令添加的唯一元素个数近似值。pfcount key，统计key的value有多少个  
* PFMERGE命令执行多个HLL之间的联合操作。  

#### 1.3.5.2. Redis中的HyperLogLog原理  
<!--
https://www.jianshu.com/p/096b25cbc39c
https://www.cnblogs.com/linguanh/p/10460421.html
-->

        
## 1.4. Geospatial地图
&emsp; 可以用来保存地理位置，并作位置距离计算或者根据半径计算位置等。  

## 1.5. Streams消息队列  
&emsp; Redis5.0推出的数据类型。支持多播的可持久化的消息队列，用于实现发布订阅功能，借鉴了kafka的设计。  
<!-- 
https://www.cnblogs.com/williamjie/p/11201654.html
-->

## 1.6. Redis中的布隆过滤器  
&emsp; 之前的布隆过滤器可以使用Redis中的位图操作实现，直到Redis4.0版本提供了插件功能，Redis官方提供的布隆过滤器才正式登场。[布隆过滤器](/docs/java/function/otherStructure.md)作为一个插件加载到Redis Server中，就会给Redis提供了强大的布隆去重功能。  

<!-- 
详细解析Redis中的布隆过滤器及其应用
https://mp.weixin.qq.com/s/h7K7w9XBYRk7NApRV9evYA
Redis亿级数据过滤和布隆过滤器
https://mp.weixin.qq.com/s/3TcNbNNobn2QEJFat-f90A
-->


## 1.7. 小结  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-105.png)  

