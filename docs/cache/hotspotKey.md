
<!-- TOC -->

- [1. Redis热点key](#1-redis热点key)
    - [1.1. 怎么发现热key](#11-怎么发现热key)
    - [1.2. 如何发现](#12-如何发现)
    - [1.3. 业内方案](#13-业内方案)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 发现热key
	1. 方法一:凭借业务经验，进行预估哪些是热key
	2. 方法二:在客户端进行收集
	3. 方法三:在Proxy层做收集  
	4. 方法四:用redis自带命令
	5. 方法五:自己抓包评估
2. 处理方案
	1. 二级缓存
	2. 备份热key  
    &emsp; 这个方案也很简单。不要让key走到同一台redis上不就行了。把这个key，在多个redis上都存一份不就好了。接下来，有热key请求进来的时候，我们就在有备份的redis上随机选取一台，进行访问取值，返回数据。  
    &emsp; 假设redis的集群数量为N，步骤如下图所示  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-71.png)  
    &emsp; 注:不一定是2N，你想取3N，4N都可以，看要求。  
    &emsp; 伪代码如下  

    ```text
    const M = N * 2
    //生成随机数
    random = GenRandom(0, M)
    //构造备份新key
    bakHotKey = hotKey + “_” + random
    data = redis.GET(bakHotKey)
    if data == NULL {
        data = GetFromDB()
        redis.SET(bakHotKey, expireTime + GenRandom(0,5))
    }
    ```


# 1. Redis热点key
<!-- 
https://www.cnblogs.com/rjzheng/p/10874537.html
-->

&emsp; 上面提到，所谓热key问题就是，突然有几十万的请求去访问redis上的某个特定key。那么，这样会造成流量过于集中，达到物理网卡上限，从而导致这台redis的服务器宕机。  
&emsp; 那接下来这个key的请求，就会直接怼到你的数据库上，导致你的服务不可用。  

## 1.1. 怎么发现热key
&emsp; 方法一:凭借业务经验，进行预估哪些是热key  
&emsp; 其实这个方法还是挺有可行性的。比如某商品在做秒杀，那这个商品的key就可以判断出是热key。缺点很明显，并非所有业务都能预估出哪些key是热key。  
&emsp; 方法二:在客户端进行收集  
&emsp; 这个方式就是在操作redis之前，加入一行代码进行数据统计。那么这个数据统计的方式有很多种，也可以是给外部的通讯系统发送一个通知信息。缺点就是对客户端代码造成入侵。  
&emsp; 方法三:在Proxy层做收集  
&emsp; 有些集群架构是下面这样的，Proxy可以是Twemproxy，是统一的入口。可以在Proxy层做收集上报，但是缺点很明显，并非所有的redis集群架构都有proxy。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-70.png)  
&emsp; 方法四:用redis自带命令  
&emsp; (1)monitor命令，该命令可以实时抓取出redis服务器接收到的命令，然后写代码统计出热key是啥。当然，也有现成的分析工具可以给你使用，比如redis-faina。但是该命令在高并发的条件下，有内存增暴增的隐患，还会降低redis的性能。  
&emsp; (2)hotkeys参数，redis 4.0.3提供了redis-cli的热点key发现功能，执行redis-cli时加上–hotkeys选项即可。但是该参数在执行的时候，如果key比较多，执行起来比较慢。  
&emsp; 方法五:自己抓包评估  
&emsp; Redis客户端使用TCP协议与服务端进行交互，通信协议采用的是RESP。自己写程序监听端口，按照RESP协议规则解析数据，进行分析。缺点就是开发成本高，维护困难，有丢包可能性。  

&emsp; 以上五种方案，各有优缺点。根据自己业务场景进行抉择即可。那么发现热key后，如何解决呢？  

## 1.2. 如何发现
&emsp; (1)利用二级缓存  
&emsp; 比如利用ehcache，或者一个HashMap都可以。在你发现热key以后，把热key加载到系统的JVM中。  
&emsp; 针对这种热key请求，会直接从jvm中取，而不会走到redis层。  
&emsp; 假设此时有十万个针对同一个key的请求过来,如果没有本地缓存，这十万个请求就直接怼到同一台redis上了。  
&emsp; 现在假设，你的应用层有50台机器，OK，你也有jvm缓存了。这十万个请求平均分散开来，每个机器有2000个请求，会从JVM中取到value值，然后返回数据。避免了十万个请求怼到同一台redis上的情形。  
&emsp; (2)备份热key  
&emsp; 这个方案也很简单。不要让key走到同一台redis上不就行了。把这个key，在多个redis上都存一份不就好了。接下来，有热key请求进来的时候，我们就在有备份的redis上随机选取一台，进行访问取值，返回数据。  
&emsp; 假设redis的集群数量为N，步骤如下图所示  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-71.png)  
&emsp; 注:不一定是2N，你想取3N，4N都可以，看要求。  
&emsp; 伪代码如下  

```text
const M = N * 2
//生成随机数
random = GenRandom(0, M)
//构造备份新key
bakHotKey = hotKey + “_” + random
data = redis.GET(bakHotKey)
if data == NULL {
    data = GetFromDB()
    redis.SET(bakHotKey, expireTime + GenRandom(0,5))
}
```

## 1.3. 业内方案


