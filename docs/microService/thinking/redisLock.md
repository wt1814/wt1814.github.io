

<!-- TOC -->

- [1. RedisLock](#1-redislock)
    - [1.1. Redis部署方式](#11-redis部署方式)
    - [1.2. Redis分布式锁实现方案](#12-redis分布式锁实现方案)
        - [1.2.1. 单实例redis实现分布式锁](#121-单实例redis实现分布式锁)
            - [1.2.1.1. 加锁](#1211-加锁)
            - [1.2.1.2. ~~加锁的注意点~~](#1212-加锁的注意点)
            - [1.2.1.3. 解锁](#1213-解锁)
        - [1.2.2. lua脚本实现分布式锁](#122-lua脚本实现分布式锁)
        - [1.2.3. SET EX PX NX + 校验惟一随机值，再删除](#123-set-ex-px-nx--校验惟一随机值再删除)
        - [1.2.4. 集群RedLock算法实现分布式锁](#124-集群redlock算法实现分布式锁)
    - [1.3. Redisson实现redis分布式锁](#13-redisson实现redis分布式锁)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. Redis分布式锁`设计`：    
    * 如何避免死锁？设置锁过期时间。  
    * `锁过期。 增大冗余时间。`  
    * 释放别人的锁。唯一标识，例如UUID。  
2. Redis分布式锁`实现方案`：  
    * 方案一：SETNX + EXPIRE  
    * 方案二：SETNX + value值是（系统时间+过时时间）  
    * `方案三：使用Lua脚本(包含SETNX + EXPIRE两条指令) ` 
    * 方案四：SET的扩展命令（SET EX PX NX）  
    * `方案五：SET EX PX NX + 校验惟一随机值，再删除释放`  
    * `方案六: 开源框架: Redisson（通过lua脚本实现）`  
    * `方案七：多机实现的分布式锁Redlock`   
3. `方案五：SET EX PX NX + 校验惟一随机值，再删除释放（先查询再删除，非原子操作，需要使用lua脚本保证其原子性）。`  
&emsp; 在这里，判断是否是当前线程加的锁和释放锁不是一个原子操做。若是调用jedis.del()释放锁的时候，可能这把锁已经不属于当前客户端，会解除他人加的锁。  
![image](http://182.92.69.8:8081/img/microService/problems/problem-61.png)  
&emsp; 为了更严谨，通常也是用lua脚本代替。lua脚本以下：  
    ```text
    if redis.call('get',KEYS[1]) == ARGV[1] then 
    return redis.call('del',KEYS[1]) 
    else
    return 0
    end;
    ```
4. RedLock红锁：  
    1. RedLock：当前线程尝试给每个Master节点`顺序`加锁。要在多数节点上加锁，并且加锁时间小于超时时间，则加锁成功；加锁失败时，依次删除节点上的锁。  
    2. ~~RedLock“顺序加锁”：确保互斥。在同一时刻，必须保证锁至多只能被一个客户端持有。~~   
5. Redis分布式锁`缺点`：   
&emsp; 采用Master-Slave模式，加锁的时候只对一个节点加锁，即使通过Sentinel做了高可用，但是<font color="clime">如果Master节点故障了，发生主从切换，此时就会有可能出现`锁丢失`的问题，`可能导致多个客户端同时完成加锁`</font>。  
&emsp; `在解决接口幂等问题中，采用redis分布式锁可以。但是如果涉及支付等对数据一致性要求严格的场景，需要结合数据库锁，建议使用基于状态机的乐观锁。`  



# 1. RedisLock 
<!-- 

★★★重要Redis分布式锁到底安全吗？ 
https://mp.weixin.qq.com/s/jDR1uH-4ACRCZFm4EdNBEg
-->

## 1.1. Redis部署方式 
&emsp; 使用Redis分布式锁需要考虑Redis的部署问题。Redis有多种部署方式：单机模式；Master-Slave+Sentinel选举模式；Redis Cluster模式。  
* 如果采用单机部署模式，会存在单点问题。 **<font color = "clime">只要Redis故障了，可能存在`死锁`问题。</font>**  
* 采用Master-Slave模式，加锁的时候只对一个节点加锁，即使通过Sentinel做了高可用，但是<font color="clime">如果Master节点故障了，发生主从切换，此时就会有可能出现锁丢失的问题，可能导致多个客户端同时完成加锁</font>。  

## 1.2. Redis分布式锁实现方案  
<!-- 
七种方案！探讨Redis分布式锁的正确使用姿式
https://www.shangmayuan.com/a/c3bd01caa98044f28d5584dc.html
-->  

&emsp; redis实现分布式锁方案：  
* 方案一：SETNX + EXPIRE  
* 方案二：SETNX + value值是（系统时间+过时时间）  
* `方案三：使用Lua脚本(包含SETNX + EXPIRE两条指令) ` 
* 方案四：SET的扩展命令（SET EX PX NX）  
* 方案五：SET EX PX NX + 校验惟一随机值,再删除释放  
* 方案六: 开源框架: Redisson  
* 方案七：多机实现的分布式锁Redlock  

### 1.2.1. 单实例redis实现分布式锁  
#### 1.2.1.1. 加锁  
&emsp; 加锁中使用了redis的set命令。加锁涉及获取锁、加锁两步操作，**最初分布式锁借助于setnx和expire命令**，但是这两个命令不是原子操作，如果执行setnx之后获取锁，但是此时客户端挂掉，这样无法执行expire设置过期时间，就导致锁一直无法被释放，因此**在2.8版本中Antirez为setnx增加了参数扩展，使得setnx和expire具备原子操作性**。  

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

```
SET KEY value [EX seconds] [PX milliseconds] [NX|XX]
``` 
* EX second：设置键的过期时间为second秒。  
* PX millisecond：设置键的过期时间为millisecond毫秒。  
* NX：只在键不存在时，才对键进行设置操作。  
* XX：只在键已经存在时，才对键进行设置操作。  


#### 1.2.1.2. ~~加锁的注意点~~
<!-- 
深度剖析：Redis分布式锁到底安全吗？看完这篇文章彻底懂了！ 
https://mp.weixin.qq.com/s/ymU8m6rco2GZqDzRSGp3iA
-->

* 如何避免死锁？设置锁过期时间。  
* 锁过期。 增大冗余时间。  
* 释放别人的锁。 唯一标识，例如线程ID或UUID。  

#### 1.2.1.3. 解锁  
&emsp; <font color = "blue">解锁涉及获取锁、删除锁两步操作，采用redis和lua脚本实现。</font>lua脚本执行命令具有原子性。  
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

### 1.2.2. lua脚本实现分布式锁  

### 1.2.3. SET EX PX NX + 校验惟一随机值，再删除  
&emsp; 既然锁可能被别的线程误删，那咱们给value值设置一个标记当前线程惟一的随机数，在删除的时候，校验一下，不就OK了嘛。伪代码以下：  

```text
if（jedis.set(key_resource_id, uni_request_id, "NX", "EX", 100s) == 1）{ //加锁
    try {
        do something  //业务处理
    }catch(){
　　}
　　finally {
       //判断是否是当前线程加的锁,是才释放
       if (uni_request_id.equals(jedis.get(key_resource_id))) {
        jedis.del(lockKey); //释放锁
        }
    }
}
```

&emsp; 在这里，判断是否是当前线程加的锁和释放锁不是一个原子操做。若是调用jedis.del()释放锁的时候，可能这把锁已经不属于当前客户端，会解除他人加的锁。  
![image](http://182.92.69.8:8081/img/microService/problems/problem-61.png)  

&emsp; 为了更严谨，通常也是用lua脚本代替。lua脚本以下：  

```text
if redis.call('get',KEYS[1]) == ARGV[1] then 
   return redis.call('del',KEYS[1]) 
else
   return 0
end;
```


### 1.2.4. 集群RedLock算法实现分布式锁  
&emsp; Redis分布式锁官网中文地址：http://redis.cn/topics/distlock.html 。 

&emsp; RedLock算法描述：假设Redis的部署模式是Redis Cluster，总共有5个Master节点。客户端通过以下步骤获取一把锁。  
1. 获取当前时间戳，单位是毫秒。  
2. 轮流尝试在每个Master节点上创建锁，过期时间设置较短，一般就几十毫秒。  
3. 尝试在大多数节点上建立一个锁，比如 5 个节点就要求是3个节点(n / 2 +1)。  
4. 客户端计算建立好锁的时间，如果建立锁的时间小于超时时间，就算建立成功了。  
5. 要是锁建立失败了，那么就依次删除这个锁。  
6. 只要别的线程建立了一把分布式锁，当前线程就得不断轮询去尝试获取锁。  

&emsp; **<font color="red">一句话概述：当前线程尝试给每个Master节点`顺序`加锁。要在多数节点上加锁，并且加锁时间小于超时时间，则加锁成功；加锁失败时，依次删除节点上的锁。</font>**  
&emsp; ~~**<font color="blue">RedLock“顺序加锁”：确保互斥。在同一时刻，必须保证锁至多只能被一个客户端持有。</font>**~~   
<!-- 
https://mp.weixin.qq.com/s/UjK9fhRElLzllqHWz9wxJg
为了取到锁，客户端应该执行以下操作:

    获取当前Unix时间，以毫秒为单位。

    依次尝试从N个Master实例使用相同的key和随机值获取锁（假设这个key是LOCK_KEY）。当向Redis设置锁时，客户端应该设置一个网络连接和响应超时时间，这个超时时间应该小于锁的失效时间。例如你的锁自动失效时间为10秒，则超时时间应该在5-50毫秒之间。这样可以避免服务器端Redis已经挂掉的情况下，客户端还在死死地等待响应结果。如果服务器端没有在规定时间内响应，客户端应该尽快尝试另外一个Redis实例。

    客户端使用当前时间减去开始获取锁时间（步骤1记录的时间）就得到获取锁使用的时间。当且仅当从大多数的Redis节点都取到锁，并且使用的时间小于锁失效时间时，锁才算获取成功。

    如果取到了锁，key的真正有效时间等于有效时间减去获取锁所使用的时间（步骤3计算的结果）。

    如果因为某些原因，获取锁失败（没有在至少N/2+1个Redis实例取到锁或者取锁时间已经超过了有效时间），客户端应该在所有的Redis实例上进行解锁（即便某些Redis实例根本就没有加锁成功）。
-->


## 1.3. Redisson实现redis分布式锁  
&emsp; [Redisson](/docs/microService/thinking/Redisson.md)  
