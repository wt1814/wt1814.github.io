

# 1. 使用分布式锁的思考  

<!-- 
 Redis——由分布式锁造成的重大事故 
 https://mp.weixin.qq.com/s/38YlgZnxRNX54esQmQoZ0w
记一次由Redis分布式锁造成的重大事故，避免以后踩坑！ 
https://mp.weixin.qq.com/s/70mS50S2hdN_qd-RD4rk2Q
-->

## 1.1. 分布式锁使用场景  
&emsp; 锁使用在不同线程共享资源。  
1. 单机环境中使用synchronized、Lock就可以了。现在大部分都是分布式、集群，线程处于不同JVM中，针对共享资源应该使用分布式锁了。  
2. 共享资源概念比较宽泛，共享资源可是是数据库、同一份文件....。  

&emsp; 一些具体的使用场景：  
1. 电商中的减库存，超卖问题。  
2. 调用外部系统，过期Token。如果不使用分布式锁，Token过期时，多个线程同时请求，会导致浪费了好多Token值。    

## 1.2. 分布式锁选型  
&emsp; 分布式中间件的选型无非就是AP、CP模型（CAP原理）中的一种。针对常用的分布式锁，redis是AP模型、zookeeper是CP模型。具体选择哪一种看使用场景对数据一致性的要求。  
&emsp; 现在针对这两种锁做一些具体分析：  
&emsp; 目前使用比较多的是redis锁，但是redis单机会有宕机风险；主从复制、Redisson实现锁，都有节点宕机，锁丢失，多个线程同时抢到锁的问题。  
&emsp; 由RedLock、ZK实现的锁，相对安全，但是又有性能问题。  
&emsp; 所以对于分布式锁的选型还是，针对具体业务、具体场景是要数据一致性好点，还是性能好点。  

## 1.3. 分布式锁使用  
&emsp; 对于锁的使用，编码问题，小编认为可以参考DCL（双重校验锁）的使用。例如过期Token：  

```java
private volatile static token;

//......
if(StringUtil.isBack(token)){
    lock.lock();
    if(StringUtil.isBack(token)){
        token = "xxx";
    }
    lock.unlock();
} 
```

&emsp; 当然还有一些其他问题考虑：锁重入、自动延期等等。  

&emsp; 如果不能正确使用分布式锁，还是会出一些问题的。例如下面这位哥们。  
https://mp.weixin.qq.com/s/70mS50S2hdN_qd-RD4rk2Q  






