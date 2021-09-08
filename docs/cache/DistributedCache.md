

<!-- TOC -->

- [1. 分布式缓存](#1-分布式缓存)
    - [1.1. 缓存穿透](#11-缓存穿透)
        - [1.1.1. 问题](#111-问题)
        - [1.1.2. 解决方案](#112-解决方案)
            - [1.1.2.1. Springboot在redis中使用BloomFilter布隆过滤器机制](#1121-springboot在redis中使用bloomfilter布隆过滤器机制)
    - [1.2. 缓存击穿](#12-缓存击穿)
        - [1.2.1. 问题](#121-问题)
        - [1.2.2. 解决方案](#122-解决方案)
            - [1.2.2.1. 互斥锁方案详解](#1221-互斥锁方案详解)
            - [1.2.2.2. 双缓存](#1222-双缓存)
    - [1.3. 缓存雪崩](#13-缓存雪崩)
        - [1.3.1. 问题](#131-问题)
        - [1.3.2. 解决方案](#132-解决方案)
    - [1.4. 缓存更新（保证缓存与数据库的双写一致性）](#14-缓存更新保证缓存与数据库的双写一致性)
    - [1.5. 缓存预热](#15-缓存预热)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. **缓存穿透、缓存击穿和缓存雪崩：**  
&emsp; <font color="red">缓存穿透、缓存击穿和缓存雪崩都是缓存失效导致大量请求直接访问数据库而出现的情况。</font>  
&emsp; <font color="red">不同的是缓存穿透是数据库和缓存都不存在相关数据；而缓存击穿和缓存雪崩是缓存和数据库都存在相应数据，</font><font color = "clime">只是缓存失效了而已。</font>  
2. 缓存穿透：  
&emsp; 缓存穿透是指，请求访问的数据在缓存中没有命中，到数据库中查询也没有，导致此次查询失败；当大量请求针对此类数据时，由于缓存不能命中，请求直接穿透缓存，直击数据库，给数据库造成巨大的访问压力。    
&emsp; 解决方案：空值缓存，`设置布隆过滤器`。  
3. 缓存`击穿`(`热点缓存`)：  
&emsp; 当缓存中不存在但是数据库中存在的数据（一般来说指缓存失效），在短时间内针对这种数据产生大量的请求，由于缓存不能命中，直击数据库，给数据库造成较大压力。  
&emsp; **<font color = "clime">解决方案：key永不过期，使用互斥锁或队列，双缓存。</font>**   
4. 缓存雪崩：  
&emsp; 缓存雪崩是指某一时间段内缓存中数据大批量过期失效，但是查询数据量巨大，引起数据库压力过大甚至宕机。和缓存击穿不同的是，缓存击穿指并发查同一条数据，缓存雪崩是不同数据都过期了，导致大量请求直达数据库。缓存雪崩有两种情况：  
    * 缓存批量过期：缓存批量过期这样的雪崩只是对数据库产生周期性的压力，数据还是扛得住的。解决方案：`key随机值，` **<font color = "clime">key永不过期，使用互斥锁或队列，双缓存。</font>**
    * 缓存服务器宕机：缓存服务器的某个节点宕机或断网，对数据库产生的压力是致命的。解决方案：服务器高可用。  
5. 缓存预热

# 1. 分布式缓存  
<!--
******
https://blog.csdn.net/weixin_39782500/article/details/111344759
-->
<!-- 
~~
缓存与数据库一致性问题深度剖析
https://mp.weixin.qq.com/s/NUgsE7PYcZTujsEhJtMdIQ
-->
&emsp; 使用缓存需要考虑的问题：缓存雪崩、缓存穿透、缓存更新和缓存与数据的一致性问题、热点数据集中失效、缓存并发、缓存无底洞、缓存淘汰......  


## 1.1. 缓存穿透  
### 1.1.1. 问题  
&emsp; 缓存穿透是指查询一个一定不存在的数据，因为缓存中无该数据的信息，则会直接去数据库层进行查询，从系统层面来看像是穿透了缓存层直接达到数据库，从而称为缓存穿透。  

### 1.1.2. 解决方案  
1. 空值缓存或缓存特殊字符串，比如&&：  
&emsp; 一种比较简单的解决办法，在第一次查询完不存在的数据后，将该key与对应的空值也放入缓存中，只不过设定为较短的失效时间，例如几分钟。这样则可以应对短时间的大量的该key攻击，设置为较短的失效时间是因为该值可能业务无关，存在意义不大，且该次的查询也未必是攻击者发起，无过久存储的必要，故可以早点失效。    
2. 设置布隆过滤器：  
&emsp; [布隆过滤器介绍](/docs/function/otherStructure.md)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-50.png)  
&emsp; 设置布隆过滤器，预先将所有值哈希到一个足够大的BitMap中，每次请求都会经过BitMap的拦截，如果Key不存在，直接返回异常。这样就避免了对缓存以及底层数据库的查询压力。  

#### 1.1.2.1. Springboot在redis中使用BloomFilter布隆过滤器机制
<!-- 
https://blog.csdn.net/qq_35387940/article/details/105700615
-->


## 1.2. 缓存击穿  
### 1.2.1. 问题  
&emsp; 缓存击穿指的是一个key的访问量非常大，比如某秒杀活动，有1w/s的并发量。这个key在某一时刻过期，那这些大量的请求就会一瞬间到数据库，数据库可能会直接崩溃。  
&emsp; <font color="red">缓存击穿实际上是缓存雪崩的一个特例，缓存被“击穿”和缓存雪崩的区别在于这里针对某一key缓存，前者则是很多key。</font>  

### 1.2.2. 解决方案 
* 对于热点数据，慎重考虑过期时间，确保热点期间key不会过期，甚至有些可以设置永不过期。  
* 使用互斥锁或队列。加锁有多种方式。  
* 双缓存：对于热点数据进行二级缓存，并对于不同级别的缓存设定不同的失效时间，则请求不会直接击穿缓存层到达数据库。  

#### 1.2.2.1. 互斥锁方案详解  
&emsp; 第一种方法：整个方法是synchronized 的，这样做虽然可以防止大量请求落到数据库上，但是就算是缓存没有失效，需要从数据库中查询数据也需要排队，无疑是降低了系统的吞吐量。  

```java
public synchronized String getCacheData() {
      String cacheData = "";
      //读 Redis
      cacheData = getDataFromRedis();
      if (cacheData.isEmpty()) {
          //读数据库
          cacheData = getDataFromDB();
          //写 Redis
          setDataToCache(cacheData);
      }
      return cacheData;
}
```
&emsp; 第二种方法：当缓存失效时，只对查询数据库的操作进行加锁，这样对于缓存没有失效的情况也非常友好，但是查询操作这里加锁，也只是会阻塞掉其他调用，第一其他线程要等待，对调用方不友好，第二这些被阻塞的请求最终还是会落到数据库上的。  

```java
static Object lock = new Object();
  
public String getCacheData() {
      String cacheData = "";
      // 读 Redis
      cacheData = getDataFromRedis();
      if (cacheData.isEmpty()) {
          synchronized (lock) {
              //读数据库
           cacheData = getDataFromDB();
              //写 Redis
              setDataToCache(cacheData);
          }
      }
      return cacheData;
 }
```
&emsp; <font color="red">第三种方法：使用互斥锁，</font> **<font color="clime">抢到锁的线程读数据库并写入缓存，</font>**<font color="red">抢不到线程的话也不阻塞，而是直接去读缓存，如果缓存中依然读不到数据（抢到锁的可能还没有将缓存写入成功），就等一会再试试读缓存。</font>  

```java
public String getCacheData(){
    String result = "";
    //读 Redis
    result = getDataFromRedis();
    if (result.isEmpty()) {
        if (reenLock.tryLock()) {
            try {
                //读数据库
                result = getDataFromDB();
                //写Redis
                setDataToCache(result);
            }catch(Exception e){
                //...
            }finally {
                reenLock.unlock();//释放锁
            }
        } else {
            //抢不到锁的去查询二级缓存
            //读 Redis
            result = getDataFromRedis();
            if (result.isEmpty()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //...
                }
                return getCacheData();
            }
        }
    }
    return result;
}
```

#### 1.2.2.2. 双缓存  
&emsp; [双缓存](/docs/cache/DoubleCache.md)  

## 1.3. 缓存雪崩  
### 1.3.1. 问题  
&emsp; **缓存雪崩是指在某一个时间段，缓存集中过期失效。** 此刻无数的请求直接绕开缓存，直接请求数据库。  
&emsp; 造成缓存雪崩的原因，有两种：1.多个key同时失效；2.reids宕机。  

### 1.3.2. 解决方案  
1. **<font color = "clime">多个key同时失效：</font>**  
    * **<font color = "clime">不设置缓存过期时间。</font>**  
    * **<font color = "clime">设置随机过期时间，这样就会大幅度的减少缓存在同一时间过期。</font>**  
    * **<font color = "clime">使用互斥锁或队列：</font><font color = "red">在缓存失效后，通过互斥锁或者队列，控制读数据库和写缓存的线程数量；不过这样会导致系统的吞吐量下降。</font>**  
    * **<font color = "clime">双缓存：</font><font color = "red">设置一级缓存和二级缓存，一级缓存过期时间短，二级缓存过期时间长或者不过期，一级缓存失效后访问二级缓存，同时刷新一级缓存。</font>**  

2. 缓存服务器宕机：对于“Redis挂掉了，请求全部走数据库”这种情况，可以有以下的思路：  
    * 事发前：实现Redis的高可用(主从架构+Sentinel 或者Redis Cluster)，尽量避免Redis挂掉这种情况发生。  
    * 事发中：万一Redis真的挂了，可以设置本地缓存(ehcache)+限流(hystrix)，尽量避免数据库被干掉(起码能保证服务还是能正常工作的)。  
    * 事发后：redis持久化，重启后自动从磁盘上加载数据，快速恢复缓存数据。  



## 1.4. 缓存更新（保证缓存与数据库的双写一致性）  
&emsp; 参考[缓存更新](/docs/cache/CacheUpdate.md)  


## 1.5. 缓存预热
&emsp; 所谓缓存预热就是将一些可能经常使用数据在系统启动的时候预先设置到缓存中，这样可以避免在使用到的时候先去数据库中查询。  
&emsp; 还有一种方式就是添加一个缓存刷新页，这样通过人工干预的方式将一些可能为热点的key添加到缓存中。  