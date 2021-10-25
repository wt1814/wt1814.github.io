
<!-- TOC -->

- [1. redis分布式锁的8大坑](#1-redis分布式锁的8大坑)
    - [1.1. 非原子操作](#11-非原子操作)
    - [1.2. 忘了释放锁](#12-忘了释放锁)
    - [1.3. 释放了别人的锁](#13-释放了别人的锁)
    - [1.4. 大量失败请求](#14-大量失败请求)
    - [1.5. 锁重入问题](#15-锁重入问题)
    - [1.6. 锁竞争问题](#16-锁竞争问题)
        - [1.6.1. 读写锁](#161-读写锁)
        - [1.6.2. 锁分段](#162-锁分段)
    - [1.7. 锁超时问题](#17-锁超时问题)
    - [1.8. 主从复制问题](#18-主从复制问题)
    - [1.9. ~~正确使用分布式锁~~](#19-正确使用分布式锁)

<!-- /TOC -->

# 1. redis分布式锁的8大坑
<!-- 
https://mp.weixin.qq.com/s/CJQKtGOEQLzuUOUjSCvEuA
-->

&emsp; 在分布式系统中，由于redis分布式锁相对于更简单和高效，成为了分布式锁的首先，被我们用到了很多实际业务场景当中。  
&emsp; 但不是说用了redis分布式锁，就可以高枕无忧了，如果没有用好或者用对，也会引来一些意想不到的问题。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-69.png)  


## 1.1. 非原子操作

## 1.2. 忘了释放锁

## 1.3. 释放了别人的锁

## 1.4. 大量失败请求
&emsp; 如果有1万的请求同时去竞争那把锁，可能只有一个请求是成功的，其余的9999个请求都会失败。  
&emsp; 在秒杀场景下，会有什么问题？  
&emsp; 答：每1万个请求，有1个成功。再1万个请求，有1个成功。如此下去，直到库存不足。这就变成均匀分布的秒杀了，跟我们想象中的不一样。  

&emsp; 使用自旋锁。  

```java
try {
  Long start = System.currentTimeMillis();
  while(true) {
     String result = jedis.set(lockKey, requestId, "NX", "PX", expireTime);
     if ("OK".equals(result)) {
        if(!exists(path)) {
           mkdir(path);
        }
        return true;
     }
     
     long time = System.currentTimeMillis() - start;
      if (time>=timeout) {
          return false;
      }
      try {
          Thread.sleep(50);
      } catch (InterruptedException e) {
          e.printStackTrace();
      }
  }
} finally{
    unlock(lockKey,requestId);
}  
return false;
```

&emsp; 在规定的时间，比如500毫秒内，自旋不断尝试加锁（说白了，就是在死循环中，不断尝试加锁），如果成功则直接返回。如果失败，则休眠50毫秒，再发起新一轮的尝试。如果到了超时时间，还未加锁成功，则直接返回失败。  

## 1.5. 锁重入问题
&emsp; 递归调用，使用可重入锁。  

## 1.6. 锁竞争问题
&emsp; 如果有大量需要写入数据的业务场景，使用普通的redis分布式锁是没有问题的。  
&emsp; 但如果有些业务场景，写入的操作比较少，反而有大量读取的操作。这样直接使用普通的redis分布式锁，会不会有点浪费性能？  
&emsp; 我们都知道，锁的粒度越粗，多个线程抢锁时竞争就越激烈，造成多个线程锁等待的时间也就越长，性能也就越差。   
&emsp; 所以，提升redis分布式锁性能的第一步，就是要把锁的粒度变细。  

### 1.6.1. 读写锁

### 1.6.2. 锁分段


## 1.7. 锁超时问题
&emsp; 自动续期  


## 1.8. 主从复制问题



## 1.9. ~~正确使用分布式锁~~  
<!-- 
记一次由Redis分布式锁造成的重大事故，避免以后踩坑！ 
https://mp.weixin.qq.com/s/70mS50S2hdN_qd-RD4rk2Q



&emsp; 之前跟同事讨论，redis锁是不是要加时间限制。其实redis锁要不要加时间，也就是释放锁的时机问题，最终演变成了finally里要不要释放锁。  
&emsp; 如果redis锁用于争抢资源(文本、数据库)，在finally是要释放锁的；如果redis锁用于幂等，建议还是不要在finally释放锁了，可能程序执行时间比你触发幂等的间隔短，那加不加锁，也就没意义了。  
-->
&emsp; 对于锁的使用，编码问题，小编认为可以参考DCL(双重校验锁)的使用。例如过期Token：  

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
