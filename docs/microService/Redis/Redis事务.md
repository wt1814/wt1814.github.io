

<!-- TOC -->

- [1. Redis事务](#1-redis事务)
    - [1.1. Redis事务简介](#11-redis事务简介)
    - [1.2. Redis事务使用案例](#12-redis事务使用案例)
    - [1.3. Redis事务总结](#13-redis事务总结)

<!-- /TOC -->

# 1. Redis事务  

<!-- 
不支持原子性的 Redis 事务也敢叫事务？ 
https://mp.weixin.qq.com/s/v2Ob6gZjmoJW1cEKb1Om0Q
-->

《Redis深度历险 核心原理与应用实践》  

## 1.1. Redis事务简介  
&emsp; **Redis事务的概念：**   
&emsp; **<font color = "lime">Redis事务的本质是一组命令的集合。</font>** 事务支持一次执行多个命令，一个事务中所有命令都会被序列化。在事务执行过程，会按照顺序串行化执行队列中的命令，其他客户端提交的命令请求不会插入到事务执行命令序列中。   
&emsp; 总结说： **<font color = "red">redis事务就是一次性、顺序性、排他性的执行一个队列中的一系列命令。</font>**　　

&emsp; **Redis事务没有隔离级别的概念：**  
&emsp; 批量操作在发送EXEC命令前被放入队列缓存，并不会被实际执行，也就不存在事务内的查询要看到事务里的更新，事务外查询不能看到。  

&emsp; **Redis不保证原子性：**  
&emsp; Redis中，单条命令是原子性执行的，但事务不保证原子性，且没有回滚。事务中任意命令执行失败，其余的命令仍会被执行。  

&emsp; **<font color = "lime">Redis事务的三个阶段：</font>**  

* 开始事务：以MULTI开启一个事务   
* 命令入队：将多个命令入队到事务中，接到这些命令不会立即执行，而是放到等待执行的事务队列里面    
* 执行事务：由EXEC命令触发事务  

&emsp; **Redis事务相关命令：**  

* multi: 标记一个事务块的开始（queued）
* exec: 执行所有事务块的命令（一旦执行exec后，之前加的监控锁都会被取消掉 ）　
* discard: 取消事务，放弃事务块中的所有命令
* watch key1 key2 ...: <font color = "red">监视一或多个key，如果在事务执行之前，被监视的key被其他命令改动，则事务被打断（类似乐观锁）</font>
* unwatch: 取消watch对所有key的监控
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-98.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-99.png)  

## 1.2. Redis事务使用案例  
&emsp; （1）正常执行。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-91.png)  
&emsp; （2）放弃事务。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-92.png)  
&emsp; （3）<font color = "lime">若在事务队列中存在命令性错误（类似于java编译性错误），</font><font color = "red">则执行EXEC命令时，所有命令都不会执行。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-93.png)  
&emsp; （4）<font color = "lime">若在事务队列中存在语法性错误（类似于java的1/0的运行时异常），</font><font color = "red">则执行EXEC命令时，其他正确命令会被执行，错误命令抛出异常。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-94.png)  
&emsp; （5）使用watch  
&emsp; 案例一：使用watch检测balance，事务期间balance数据未变动，事务执行成功  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-95.png)  
&emsp; 案例二：使用watch检测balance，在开启事务后（标注1处），在新窗口执行标注2中的操作，更改balance的值，模拟其他客户端在事务执行期间更改watch监控的数据，然后再执行标注1后命令，执行EXEC后，事务未成功执行。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-96.png)  
&emsp; 一但执行EXEC开启事务的执行后，无论事务使用执行成功，WARCH对变量的监控都将被取消。  
&emsp; 故当事务执行失败后，需重新执行WATCH命令对变量进行监控，并开启新的事务进行操作。  


## 1.3. Redis事务总结  
&emsp; watch指令类似于乐观锁，在事务提交时，<font color = "red">如果watch监控的多个KEY中任何KEY的值已经被其他客户端更改，则使用EXEC执行事务时，</font>事务队列将不会被执行，同时返回Nullmulti-bulk应答以通知调用者事务执行失败。  


<!-- 
&emsp; Redis 的事务有两个特点：  
1. 按进入队列的顺序执行。  
2. 不会受到其他客户端的请求影响。  

1.1. Redis事务的使用  
&emsp; Redis 的事务涉及到四个命令：multi（开启事务），exec（执行事务），discard （取消事务），watch（监视）。  
1. 使用Multi命令表示开启一个事务；  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-36.png)  
2. 开启一个事务过后中间输入的所有命令都不会被立即执行，而是被加入到队列中缓存起来，当收到Exec命令的时候Redis服务会按入队顺序依次执行命令。  
&emsp; 在multi命令后输入的命令不会被立即执行，而是被加入的队列中，并且加入成功redis会返回QUEUED，表示加入队列成功，如果这里的命令输入错误了，或者命令参数不对，Redis会返回ERR 如下图，并且此次事务无法继续执行了。这里需要注意的是在 Redis 2.6.5 版本后是会取消事务的执行，但是在 2.6.5 之前Redis是会执行所有成功加入队列的命令。详细信息可以看官方文档。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-37.png)  
3. 输入exec命令后会依次执行加入到队列中的命令。  

1.2. Redis事务中的错误  

1. 在Redis的事务中，命令在加入队列的时候如果出错，那么此次事务是会被取消执行的。这种错误在执行exec命令前Redis服务就可以探测到。  
2. 在 Redis 事务中还有一种错误，那就是所有命令都加入队列成功了，但是在执行exec命令的过程中出现了错误，这种错误 Redis 是无法提前探测到的，那么这种情况下 Redis 的事务是怎么处理的呢？  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-38.png)  
&emsp; 上面测试的过程是先通过命令get a获取a的值为 5，然后开启一个事务，在事务中执行两个动作，第一个是自增a的值，另一个是通过命令hset a b 3来设置a中b的值，可以看到这里a的类型是字符串，但是第二个命令也成功的加入到了队列，Redis并没有报错。但是最后在执行exec命令的时候，第一条命令执行成功了，看到返回结果是6，第二条命令执行失败了，提示的错误信息表示类型不对。  
&emsp; 然后再通过get a命令发现a的值已经被改变了，不再是之前的5了，说明虽然事务失败了但是命令执行的结果并没有回滚！  

&emsp; **<font color = "red">Redis为什么不支持事务回滚？</font>**  
1. 在开发环境中就能避免掉语法错误或者类型不匹配的情况，在生产上是不会出现的；  
2. Redis的内部是简单的快速的，所以不需要支持回滚的能力。  

1.3. Redis的乐观锁Watch  
&emsp; 在 Redis 中提供了一个 watch 命令，它可以为 Redis 事务提供 CAS 乐观锁行为（Check and Set / Compare and Swap），也就是多个线程更新变量的时候，会跟原值做比较，只有它没有被其他线程修 改的情况下，才更新成新的值。  

&emsp; Watch会在事务开始之前盯住1个或多个关键变量。  
&emsp; 当事务执行时，也就是服务器收到了exec指令要顺序执行缓存的事务队列时，Redis会检查关键变量自Watch 之后，是否被修改了。  
&emsp; 如果开启事务之后，至少有一个被监视 key 键在 exec 执行之前被修改了， 那么整个事务都会被取消（key 提前过期除外）。  
&emsp; 可以用 unwatch 取消。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-39.png)  
-->



