
<!-- TOC -->

- [1. Caffeine+Redis二级缓存](#1-caffeineredis二级缓存)
    - [1.1. 为什么使用？](#11-为什么使用)
    - [1.2. 数据一致性，广播机制（Pub/Sub，发布订阅），本地缓存数据同步](#12-数据一致性广播机制pubsub发布订阅本地缓存数据同步)
    - [1.3. 使用](#13-使用)
        - [1.3.1. 缓存提供者](#131-缓存提供者)
        - [1.3.2. ★★★缓存更新](#132-★★★缓存更新)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. J2Cache是一个两级缓存框架，第1级为JVM堆内缓存（通常选用caffeine），第2级为堆外缓存（Redis）。   
2. 缓存更新：  
&emsp; active:主动清除，二级缓存过期主动通知各节点清除，优点在于所有节点可以同时收到缓存清除  
&emsp; passive:被动清除，一级缓存过期进行通知各节点清除一二级缓存  

&emsp; redis里面并不会存太多的数据，但是访问量会比较高，所以可能会出现，redis机器内存占用不高，但是带宽满了的情况。而如果要解决这个问题，就需要用redis集群，让请求分散到不同的redis节点，但是这样很明显就需要更多的redis机器，提高了成本。  


&emsp; j2cache还提供了缓存过期、广播等机制，能实现数据过期、本地缓存数据同步等功能。

# 1. Caffeine+Redis二级缓存    
<!-- 
本地缓存组件 Guava cache 详解 
https://mp.weixin.qq.com/s/bMOaUEjnW5e2pSEb_tGqJQ
万字详解本地缓存之王 Caffeine 
https://mp.weixin.qq.com/s/aLN5pxs2MuHd5EFHkiBmBQ
-->

<!-- 
https://blog.csdn.net/Trunks2009/article/details/123982910
https://blog.csdn.net/Trunks2009/article/details/123786175
-->

## 1.1. 为什么使用？  
&emsp; redis里面并不会存太多的数据，但是访问量会比较高，所以可能会出现，redis机器内存占用不高，但是带宽满了的情况。而如果要解决这个问题，就需要用redis集群，让请求分散到不同的redis节点，但是这样很明显就需要更多的redis机器，提高了成本。  


## 1.2. 数据一致性，广播机制（Pub/Sub，发布订阅），本地缓存数据同步
&emsp; 缓存更新：  
&emsp; active:主动清除，二级缓存过期主动通知各节点清除，优点在于所有节点可以同时收到缓存清除  
&emsp; passive:被动清除，一级缓存过期进行通知各节点清除一二级缓存  

-------
&emsp; 当消息发送到客户端订阅的频道（channel）时，这个消息就会被订阅的所有未故障的客户端接接收到


&emsp; J2Cache 从 1.3.0 版本开始支持 JGroups 和 `【Redis Pub/Sub】` 两种方式进行缓存事件的通知。在某些云平台上可能无法使用 JGroups 组播方式，可以采用 Redis 发布订阅的方式。详情请看 j2cache.properties 配置文件的说明。  


## 1.3. 使用  
&emsp; J2Cache是一个两级缓存框架，第1级为JVM堆内缓存（通常选用caffeine），第2级为堆外缓存（Redis）。  
&emsp; J2Cache能自动进行堆内堆外缓存的协调使用（基于消息通知方式）。  

1. 添加依赖  
&emsp; 在pom.xml中添加如下依赖  

```text
<dependency>
	<groupId>net.oschina.j2cache</groupId>
	<artifactId>j2cache-core</artifactId>
	<version>${j2cache-version}</version>
	<exclusions>
		<exclusion>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-simple</artifactId>
		</exclusion>
	</exclusions>
</dependency>
<dependency>
	<groupId>net.oschina.j2cache</groupId>
	<artifactId>j2cache-spring-boot2-starter</artifactId>
	<version>${j2cache.version}</version>
	<exclusions>
		<exclusion>
			<groupId>com.alibaba</groupId>
			<artifactId>fastjson</artifactId>
		</exclusion>
	</exclusions>
</dependency>
```

2. 配置  
&emsp; 以下参数为默认值，如需修改，请将相应参数配置在application.properties或application.yml中  

```text
# 是否开启springcache
j2cache.open-spring-cache=true
# springcache类型(不可修改)
spring.cache.type=GENERIC
# 是否允许存放null值
j2cache.allow-null-values=true
#########################################
# 缓存清除模式
# active:主动清除，二级缓存过期主动通知各节点清除，优点在于所有节点可以同时收到缓存清除
# passive:被动清除，一级缓存过期进行通知各节点清除一二级缓存
# blend:两种模式一起运作，对于各个节点缓存准确性以及及时性要求高的可以使用(推荐使用前面两种模式中一种)
#########################################
j2cache.cache-clean-mode=passive
# 一级缓存的提供者
j2cache.L1.provider_class=caffeine
# 二级缓存的配置项前缀
j2cache.L2.config_section=redis
# 二级缓存的提供者
j2cache.L2.provider_class=net.oschina.j2cache.cache.support.redis.SpringRedisProvider
# 是否开启二级缓存
j2cache.l2-cache-open=true
# redis使用的客户端
j2cache.redis-client=jedis
# 缓存组播的提供类
j2cache.broadcast=net.oschina.j2cache.cache.support.redis.SpringRedisPubSubPolicy
# 一级、二级缓存的过期时间是否同步，false-二级缓存数据永不过期
j2cache.sync_ttl_to_redis=true
# 是否允许存放null对象
j2cache.default_cache_null_object=true
#########################################
# 缓存的序列化
# values:
# fst -> using fast-serialization (recommend)
# kyro -> using kyro serialization
# json -> using fst's json serialization (testing) 出现无法转换的Bug
# fastjson -> using fastjson serialization (embed non-static class not support)
# java -> java standard
# [classname implements Serializer]
#########################################
j2cache.serialization=fst
# 一级缓存作用域的大小(个数)及过期时间
caffeine.region.default=1000,1h
#########################################
# Redis参数配置
# single -> single redis server
# sentinel -> master-slaves servers
# cluster -> cluster servers (数据库配置无效，使用 database = 0）
# sharded -> sharded servers  (密码、数据库必须在 hosts 中指定，且连接池配置无效 ; redis://user:password@127.0.0.1:6379/0）
#########################################
redis.mode=single
#redis storage mode (generic|hash)
redis.storage=generic
## redis pub/sub channel name
redis.channel=j2cache
## redis pub/sub server (using redis.hosts when empty)
redis.channel.host=
#cluster name just for sharded
redis.cluster_name=j2cache
## redis cache namespace optional, default[empty]
redis.namespace=
## connection
# Separate multiple redis nodes with commas, such as 192.168.0.10:6379,192.168.0.11:6379,192.168.0.12:6379
redis.hosts=127.0.0.1:6379
redis.timeout=2000
redis.password=
redis.database=0
## Redis连接池参数
redis.maxTotal=100
redis.maxIdle=10
redis.maxWaitMillis=5000
redis.minEvictableIdleTimeMillis=60000
redis.minIdle=1
redis.numTestsPerEvictionRun=10
redis.lifo=false
redis.softMinEvictableIdleTimeMillis=10
redis.testOnBorrow=true
redis.testOnReturn=false
redis.testWhileIdle=true
redis.timeBetweenEvictionRunsMillis=300000
redis.blockWhenExhausted=false
redis.jmxEnabled=false
```

### 1.3.1. 缓存提供者  

```text
# 一级缓存的提供者
j2cache.L1.provider_class=caffeine
# 二级缓存的配置项前缀
j2cache.L2.config_section=redis
# 二级缓存的提供者
j2cache.L2.provider_class=net.oschina.j2cache.cache.support.redis.SpringRedisProvider
```


### 1.3.2. ★★★缓存更新  
&emsp; active:主动清除，二级缓存过期主动通知各节点清除，优点在于所有节点可以同时收到缓存清除  
&emsp; passive:被动清除，一级缓存过期进行通知各节点清除一二级缓存  

