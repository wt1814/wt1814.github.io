
<!-- TOC -->

- [1. 分布式锁](#1-分布式锁)
    - [1.1. 分布式锁使用场景](#11-分布式锁使用场景)
    - [1.2. 实现分布式锁需要关注哪些细节呢？](#12-实现分布式锁需要关注哪些细节呢)
    - [1.3. 分布式锁实现](#13-分布式锁实现)
        - [1.3.2. Spring Integration](#132-spring-integration)

<!-- /TOC -->

# 1. 分布式锁  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-29.png)  

## 1.1. 分布式锁使用场景  
<!-- 
***分布式锁使用场景
https://www.cnblogs.com/aoshicangqiong/p/12173550.html
-->
&emsp; **为什么使用分布式锁？**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-11.png)  
&emsp; 如上图，在分布式系统中，订单模块为了迎战高并发，订单服务被横向拆分，拆分成了不同的进程，就像上图，两个人同时访问订单服务，然后订单系统1和订单系统2共用一个Mysql当成数据库，经过查询发现仅有一件商品，所以两个系统都认为可以下单。如果不加锁限制，可能会出现库存减为负数的情况。  
&emsp; 解决方案：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-12.png)  
&emsp; 如上图。mysql自带行级锁，可以考虑使用它的行级锁，可以保证数据的安全。不足之处：使用MySql的行级锁，系统的压力全部集中在mysql，那么mysql就是系统吞吐量的瓶颈了，系统的吞吐量也会受到mysql的限制。  
&emsp; 可以使用分布式锁。如下图，分布式锁将系统的压力从mysql上面转移到自身上来。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-13.png)  

&emsp; **分布式锁的使用场景：**  
1. 避免不同节点重复相同的工作。  

        比如用户执行了某个操作有可能不同节点会发送多封邮件。  
        比如多台机器都可以定时执行某个任务，如果限制任务每次只能被一台机器执行，不能重复执行，就可以用分布式锁来做标记。

2. 避免破坏数据的正确性：在分布式环境下解决多实例对数据的访问一致性。如果多个节点在同一条数据上同时进行操作，可能会造成数据错误或不一致的情况出现。  

        比较敏感的数据比如金额修改，同一时间只能有一个人操作，如果2个人同时修改金额，一个加金额一个减金额，为了防止同时操作造成数据不一致，需要锁，如果是数据库需要的就是行锁或表锁，如果是在集群里，多个客户端同时修改一个共享的数据就需要分布式锁。  
        比如秒杀场景，要求并发量很高，那么同一件商品只能被一个用户抢到，那么就可以使用分布式锁实现。 


## 1.2. 实现分布式锁需要关注哪些细节呢？  

* 确保互斥：在同一时刻，必须保证锁至多只能被一个客户端持有。  
* **不能死锁：在一个客户端在持有锁的期间崩溃而没有主动解锁情况下，也能保证后续其他客户端能加锁。**    
* **避免活锁：在获取锁失败的情况下，反复进行重试操作，占用Cpu资源，影响性能。**    
* 实现更多锁特性：锁中断、锁重入、锁超时等。确保客户端只能解锁自己持有的锁。  


## 1.3. 分布式锁实现  
&emsp; 分布式锁实现的三个核心要素：1.加锁；2.解锁；3.锁超时。  
&emsp; 分布式锁一般有三种基础的实现方式：1.数据库悲观锁、乐观锁；2.[基于缓存(redis，memcached，tair)的分布式锁](/docs/microService/thinking/redisLock.md)；3.[基于ZooKeeper临时顺序节点的分布式锁](/docs/microService/thinking/ZKLock.md)。  
&emsp; <font color="red">基于Redis的分布式锁是AP模型，基于Zookeeper的分布式锁是CP模型的。</font> 


### 1.3.2. Spring Integration  
&emsp; Spring Integration提供的全局锁目前为如下存储提供了实现：Gemfire、JDBC、Redis、Zookeeper。它们使用相同的API抽象。即不论使用哪种存储，编码体验是一样的，若想更换实现，只需要修改依赖和配置，无需修改代码。  

