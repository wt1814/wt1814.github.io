---
   title: redis分布式锁
   date: 2020-05-14 00:00:00
   tags:
       - 分布式
---

<!-- TOC -->

- [1. Redis分布式锁](#1-redis分布式锁)
    - [1.1. 使用Redis分布式锁的中的问题](#11-使用redis分布式锁的中的问题)
    - [1.2. Redis 分布式锁实现](#12-redis-分布式锁实现)
        - [1.2.1. Redis Client原生API](#121-redis-client原生api)
            - [1.2.1.1. 单实例redis实现分布式锁](#1211-单实例redis实现分布式锁)
                - [1.2.1.1.1. 加锁](#12111-加锁)
                - [1.2.1.1.2. 解锁](#12112-解锁)
            - [1.2.1.2. ※※※集群redlock算法实现分布式锁](#1212-※※※集群redlock算法实现分布式锁)
        - [1.2.2. Redisson实现redis分布式锁-1](#122-redisson实现redis分布式锁-1)
            - [RedissonLock解析](#redissonlock解析)
                - [获取锁tryLock](#获取锁trylock)
                - [解锁unlock](#解锁unlock)
                - [强制解锁forceUnlock](#强制解锁forceunlock)

<!-- /TOC -->


# 1. Redis分布式锁  
## 1.1. 使用Redis分布式锁的中的问题  
1. 超时问题。  
2. 部署问题：除了要考虑客户端要怎么实现分布式锁之外，还需要考虑Redis的部署问题。Redis有多种部署方式：单机模式；Master-Slave+Sentinel选举模式；Redis Cluster模式。  
    * 如果采用单机部署模式，会存在单点问题。只要 Redis 故障了，加锁就不行了。  
    * 采用Master-Slave 模式，加锁的时候只对一个节点加锁，即使通过 Sentinel做了高可用，但是<font color="red">如果Master节点故障了，发生主从切换，此时就会有可能出现锁丢失的问题</font>。  

## 1.2. Redis 分布式锁实现  
### 1.2.1. Redis Client原生API  
#### 1.2.1.1. 单实例redis实现分布式锁  

##### 1.2.1.1.1. 加锁  
&emsp; **加锁代码：**  

```java
public class RedisTool {
    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    /**
     * 尝试获取分布式锁
     * @param jedis Redis客户端
     * @param lockKey 锁
     * @param requestId 请求标识
     * @param expireTime 超期时间
     * @return 是否获取成功
     */
    public static boolean tryGetDistributedLock(Jedis jedis, String lockKey, String requestId, int expireTime) {
        String result = jedis.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        if (LOCK_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }
}
```
&emsp; 加锁就一行代码：jedis.set(String key, String value, String nxxx, String expx, int time)，这个set()方法一共有五个形参：  

* key，使用key来当锁，因为key是唯一的。  
* value，传参requestId，代表执行的具体线程。  
* **<font color = "red">nxxx，传参NX，意思是SET IF NOT EXIST，即当key不存在时，进行set操作；若key已经存在，则不做任何操作。</font>**  
* **<font color = "red">expx，传参PX，即给这个key加一个过期的设置，具体时间由第五个参数决定。</font>**  
* time，与第四个参数相呼应，代表key的过期时间。  

&emsp; 执行上面的set()方法就只会导致两种结果：1. 锁不存在，进行加锁操作，并对锁设置个有效期，同时value表示加锁的客户端； 2. 锁存在，不做任何操作。  

&emsp; 加锁中使用了redis的set命令。加锁涉及获取锁、加锁两步操作**最初分布式锁借助于setnx和expire命令**，但是这两个命令不是原子操作，如果执行setnx之后获取锁，但是此时客户端挂掉，这样无法执行expire设置过期时间就导致锁一直无法被释放，因此**在2.8版本中Antirez为setnx增加了参数扩展，使得setnx和expire具备原子操作性**。  

```
SET KEY value [EX seconds] [PX milliseconds] [NX|XX]
``` 
* EX second:设置键的过期时间为second秒。  
* PX millisecond:设置键的过期时间为millisecond毫秒。  
* NX：只在键不存在时，才对键进行设置操作。  
* XX：只在键已经存在时，才对键进行设置操作。  

##### 1.2.1.1.2. 解锁  
&emsp; 解锁也涉及获取锁、删除锁两步操作，采用redis和lua脚本实现。lua脚本执行命令具有原子性。  
&emsp; **解锁代码：**  

```java
public class RedisTool {
    private static final Long RELEASE_SUCCESS = 1L;
    /**
     * 释放分布式锁
     * @param jedis Redis客户端
     * @param lockKey 锁
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public static boolean releaseDistributedLock(Jedis jedis, String lockKey, String requestId) {
        //Lua脚本代码
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        if (RELEASE_SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }
}
```

#### 1.2.1.2. ※※※集群redlock算法实现分布式锁  
&emsp; Redis分布式锁官网中文地址：http://redis.cn/topics/distlock.html 。 

&emsp; RedLock算法描述：假设Redis的部署模式是Redis Cluster，总共有5个Master节点。客户端通过以下步骤获取一把锁。  
1. 获取当前时间戳，单位是毫秒。  
2. 轮流尝试在每个Master节点上创建锁，过期时间设置较短，一般就几十毫秒。  
3. 尝试在大多数节点上建立一个锁，比如 5 个节点就要求是3个节点（n / 2 +1）。  
4. 客户端计算建立好锁的时间，如果建立锁的时间小于超时时间，就算建立成功了。  
5. 要是锁建立失败了，那么就依次删除这个锁。  
6. 只要别的线程建立了一把分布式锁，当前线程就得不断轮询去尝试获取锁。  

&emsp; <font color="red">一句话概述：当前线程尝试给每个Master节点加锁。要在多数节点上加锁，并且加锁时间小于超时时间，则加锁成功；加锁失败时，依次删除节点上的锁。</font>  

### 1.2.2. Redisson实现redis分布式锁-1  
&emsp; 基于redis的分布式锁实现客户端Redisson，官方网址：https://redisson.org/ 。Redisson支持redis单实例、redis哨兵、redis cluster、redis master-slave等各种部署架构，都可以完美实现。  
<!-- 
https://mp.weixin.qq.com/s?__biz=MzI5OTIyMjQxMA==&mid=2247485439&idx=1&sn=5eba3992109fadf245d7b56ff4a45635&chksm=ec98931adbef1a0c0ad4a4c309bfc3000bc6271e1739f3ecb79361ecbe18d399adb3ca628c56&scene=21#wechat_redirect

https://mp.weixin.qq.com/s/LLi_7DedZfSsZyDr2OdOrg

https://mp.weixin.qq.com/s/fuaUXuJbskqcFgsrwnPhZQ

https://mp.weixin.qq.com/s/J6sYbLa_BMFHHVwgFEbQ4Q
-->

#### RedissonLock解析
##### 获取锁tryLock  
&emsp; **<font color = "lime">RedissonLock锁互斥、锁时间自动延迟、可重入加锁。</font>**  

```java
Future<Long> tryLockInnerAsync(long leaseTime, TimeUnit unit, long threadId) {
    internalLockLeaseTime = unit.toMillis(leaseTime);
    return commandExecutor.evalWriteAsync(getName(), LongCodec.INSTANCE, RedisCommands.EVAL_LONG,
                "if (redis.call('exists', KEYS[1]) == 0) then " +
                    "redis.call('hset', KEYS[1], ARGV[2], 1); " +
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                    "return nil; " +
                "end; " +
                "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                    "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                    "return nil; " +
                "end; " +
                "return redis.call('pttl', KEYS[1]);",
                Collections.<Object>singletonList(getName()), internalLockLeaseTime, getLockName(threadId));
}
```

* KEYS[1] 表示的是 getName() ，代表的是锁名 test_lock  
* ARGV[1] 表示的是 internalLockLeaseTime 默认值是30s  
* ARGV[2] 表示的是 getLockName(threadId) 代表的是 id:threadId 用锁对象id+线程id， 表示当前访问线程，用于区分不同服务器上的线程。  

&emsp; 逐句分析：

```java
if (redis.call('exists', KEYS[1]) == 0) then 
         redis.call('hset', KEYS[1], ARGV[2], 1); 
         redis.call('pexpire', KEYS[1], ARGV[1]); 
         return nil;
         end;if (redis.call('exists', KEYS[1]) == 0) then 
         redis.call('hset', KEYS[1], ARGV[2], 1); 
         redis.call('pexpire', KEYS[1], ARGV[1]); 
         return nil;
         end;
```
&emsp; if (redis.call(‘exists’, KEYS[1]) == 0) <font color = "red">如果锁名称不存在</font>  
&emsp; then redis.call(‘hset’, KEYS[1], ARGV[2],1) 则向redis中添加一个key为test_lock的set，并且向set中添加一个field为线程id，值=1的键值对，表示此线程的重入次数为1  
&emsp; redis.call(‘pexpire’, KEYS[1], ARGV[1]) 设置set的过期时间，防止当前服务器出问题后导致死锁，return nil; end;返回nil 结束  

```java
if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then 
         redis.call('hincrby', KEYS[1], ARGV[2], 1); 
         redis.call('pexpire', KEYS[1], ARGV[1]);
         return nil; 
         end;
```
&emsp; if (redis.call(‘hexists’, KEYS[1], ARGV[2]) == 1) <font color = "red">如果锁是存在的，检测是否是当前线程持有锁，如果是当前线程持有锁</font>  
&emsp; then redis.call(‘hincrby’, KEYS[1], ARGV[2], 1)则将该线程重入的次数++  
&emsp; redis.call(‘pexpire’, KEYS[1], ARGV[1]) 并且重新设置该锁的有效时间  
&emsp; return nil; end;返回nil，结束  

```java
return redis.call('pttl', KEYS[1]);
```
&emsp; <font color = "red">锁存在, 但不是当前线程加的锁，则返回锁的过期时间。</font>  

##### 解锁unlock  
......

##### 强制解锁forceUnlock 
...... 

