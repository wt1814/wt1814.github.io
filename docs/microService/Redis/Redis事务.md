---
title: Redis事务
date: 2020-05-16 00:00:00
tags:
    - Redis
---

<!-- TOC -->

- [1. Redis事务](#1-redis事务)
    - [1.1. Redis事务的使用](#11-redis事务的使用)
    - [1.2. Redis事务中的错误](#12-redis事务中的错误)
    - [1.3. Redis的乐观锁Watch](#13-redis的乐观锁watch)

<!-- /TOC -->

# 1. Redis事务  
&emsp; Redis 的事务有两个特点：  
1. 按进入队列的顺序执行。  
2. 不会受到其他客户端的请求的影响。  

## 1.1. Redis事务的使用  
&emsp; Redis 的事务涉及到四个命令：multi（开启事务），exec（执行事务），discard （取消事务），watch（监视）。  
1. 使用Multi命令表示开启一个事务；  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-36.png)  
2. 开启一个事务过后中间输入的所有命令都不会被立即执行，而是被加入到队列中缓存起来，当收到Exec命令的时候Redis服务会按入队顺序依次执行命令。  
&emsp; 在multi命令后输入的命令不会被立即执行，而是被加入的队列中，并且加入成功redis会返回QUEUED，表示加入队列成功，如果这里的命令输入错误了，或者命令参数不对，Redis会返回ERR 如下图，并且此次事务无法继续执行了。这里需要注意的是在 Redis 2.6.5 版本后是会取消事务的执行，但是在 2.6.5 之前Redis是会执行所有成功加入队列的命令。详细信息可以看官方文档。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-37.png)  
3. 输入exec命令后会依次执行加入到队列中的命令。  

## 1.2. Redis事务中的错误  

1. 在Redis的事务中，命令在加入队列的时候如果出错，那么此次事务是会被取消执行的。这种错误在执行exec命令前Redis服务就可以探测到。  
2. 在 Redis 事务中还有一种错误，那就是所有命令都加入队列成功了，但是在执行exec命令的过程中出现了错误，这种错误 Redis 是无法提前探测到的，那么这种情况下 Redis 的事务是怎么处理的呢？  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-38.png)  
&emsp; 上面测试的过程是先通过命令get a获取a的值为 5，然后开启一个事务，在事务中执行两个动作，第一个是自增a的值，另一个是通过命令hset a b 3来设置a中b的值，可以看到这里a的类型是字符串，但是第二个命令也成功的加入到了队列，Redis并没有报错。但是最后在执行exec命令的时候，第一条命令执行成功了，看到返回结果是6，第二条命令执行失败了，提示的错误信息表示类型不对。  
&emsp; 然后再通过get a命令发现a的值已经被改变了，不再是之前的5了，说明虽然事务失败了但是命令执行的结果并没有回滚！  

&emsp; **<font color = "red">Redis为什么不支持事务回滚？</font>**  
1. 在开发环境中就能避免掉语法错误或者类型不匹配的情况，在生产上是不会出现的；  
2. Redis的内部是简单的快速的，所以不需要支持回滚的能力。  

## 1.3. Redis的乐观锁Watch  
&emsp; 在 Redis 中提供了一个 watch 命令，它可以为 Redis 事务提供 CAS 乐观锁行为（Check and Set / Compare and Swap），也就是多个线程更新变量的时候，会跟原值做比较，只有它没有被其他线程修 改的情况下，才更新成新的值。  

&emsp; Watch会在事务开始之前盯住1个或多个关键变量。  
&emsp; 当事务执行时，也就是服务器收到了exec指令要顺序执行缓存的事务队列时，Redis会检查关键变量自Watch 之后，是否被修改了。  
&emsp; 如果开启事务之后，至少有一个被监视 key 键在 exec 执行之前被修改了， 那么整个事务都会被取消（key 提前过期除外）。  
&emsp; 可以用 unwatch 取消。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-39.png)  
