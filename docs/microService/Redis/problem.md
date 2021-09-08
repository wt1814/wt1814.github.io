<!-- TOC -->

- [1. Redis常见问题与优化](#1-redis常见问题与优化)
    - [1.1. 客户端常见异常](#11-客户端常见异常)
        - [1.1.1. 无法从连接池获取到连接](#111-无法从连接池获取到连接)
        - [1.1.2. 客户端读写超时](#112-客户端读写超时)
        - [1.1.3. 客户端连接超时](#113-客户端连接超时)
        - [1.1.4. Redis使用的内存超过maxmemory配置](#114-redis使用的内存超过maxmemory配置)
        - [1.1.5. 客户端连接数过大](#115-客户端连接数过大)
    - [1.2. 客户端案例分析](#12-客户端案例分析)
        - [1.2.1. Redis内存陡增](#121-redis内存陡增)
        - [1.2.2. 客户端周期性的超时](#122-客户端周期性的超时)
    - [1.3. Redis的噩梦：阻塞](#13-redis的噩梦阻塞)
    - [1.4. 持久化问题定位与优化](#14-持久化问题定位与优化)
    - [1.5. 内存优化](#15-内存优化)
    - [1.6. redis内存中的key分析](#16-redis内存中的key分析)

<!-- /TOC -->
&emsp; **<font color = "red">总结：</font>**  
1. 连接问题：
2. 内存问题：


# 1. Redis常见问题与优化  
&emsp; **参考《Redis开发与运维》**  


<!--



线上Redis高并发性能调优实践 
https://mp.weixin.qq.com/s/JFNqQWS5GrCW5Q2kmeNVrw
【95期】面试官：你遇到 Redis 线上连接超时一般如何处理？ 
https://mp.weixin.qq.com/s/LqeeCviPW84ykfPhluTMlQ

记录一次生产环境中Redis内存增长异常排查全流程！ 
https://mp.weixin.qq.com/s/1Rqzn4juKMqlNK9sBzlGig

**内存占用率,内存碎片
https://mp.weixin.qq.com/s/insu_PySCOo4SWAUB0Nopg


一次生产环境redis内存占用居高不下问题排查
https://blog.csdn.net/eene894777/article/details/102820565?utm_medium=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param
https://blog.csdn.net/Zhenxue_Xu/article/details/90727983
-->

<!-- 

Redis 越来越慢？常见延迟问题定位与分析 
https://mp.weixin.qq.com/s/Abb2muE0GaVRYswqwxfJCw

~~
 一次线上 Redis 高负载排查经历，步步惊心！ 
 https://mp.weixin.qq.com/s/a4KhrwLAClnH4ElSlUkt2Q

 ~~
-->


## 1.1. 客户端常见异常  
### 1.1.1. 无法从连接池获取到连接
&emsp; JedisPool中的Jedis对象个数是有限的，默认是8个。这里假设使用的默认配置，如果有8个Jedis对象被占用，并且没有归还，此时调用者还要从JedisPool中借用Jedis，就需要进行等待（例如设置了maxWaitMillis>0），如果在maxWaitMillis时间内仍然无法获取到Jedis对象就会抛出如下异常：  

```text
redis.clients.jedis.exceptions.JedisConnectionException: Could not get a resource from the pool …   
Caused by: java.util.NoSuchElementException: Timeout waiting for idle object at org.apache.commons.pool2.impl.GenericObjectPool.borrowObject(GenericObjectPool. java:449)  
```

&emsp; 还有一种情况，就是设置了blockWhenExhausted=false，那么调用者发现池子中没有资源时，会立即抛出异常不进行等待，下面的异常就是blockWhenExhausted=false时的效果：  

```text
redis.clients.jedis.exceptions.JedisConnectionException: Could not get a resource from the pool …   
Caused by: java.util.NoSuchElementException: Pool exhausted at org.apache.commons.pool2.impl.GenericObjectPool.borrowObject(GenericObjectPool. java:464)  
```

&emsp; 对于这个问题， **<font color = "blue">需要重点讨论的是为什么连接池没有资源了，造成没有资源的原因非常多，可能如下：</font>**  

* 客户端：高并发下连接池设置过小，出现供不应求，所以会出现上面的错误，但是正常情况下只要比默认的最大连接数（8个）多一些即可，因为正常情况下JedisPool以及Jedis的处理效率足够高。  
* 客户端：没有正确使用连接池，比如没有进行释放。  
* 客户端：存在慢查询操作，这些慢查询持有的Jedis对象归还速度会比较慢，造成池子满了。  
* 服务端：客户端是正常的，但是Redis服务端由于一些原因造成了客户端命令执行过程的阻塞，也会使得客户端抛出这种异常。  


&emsp; 可以看到造成这个异常的原因是多个方面的，不要被异常的表象所迷惑，而且并不存在万能钥匙解决所有问题，开发和运维只能不断加强对于Redis的理解，顺藤摸瓜逐渐找到问题所在。


### 1.1.2. 客户端读写超时  
&emsp; Jedis在调用Redis时，如果出现了读写超时后，会出现下面的异常：  

```text
redis.clients.jedis.exceptions.JedisConnectionException: 
java.net.SocketTimeoutException: Read timed out 
```

&emsp; 造成该异常的原因也有以下几种：  

* 读写超时间设置得过短。   
* 命令本身就比较慢。   
* 客户端与服务端网络不正常。   
* Redis自身发生阻塞。  


### 1.1.3. 客户端连接超时  
&emsp; Jedis在调用Redis时，如果出现了连接超时后，会出现下面的异常：  

```text
redis.clients.jedis.exceptions.JedisConnectionException: 
java.net.SocketTimeoutException: connect timed out
```

&emsp; 造成该异常的原因也有以下几种：  

&emsp; 1）连接超时设置得过短，可以通过下面代码进行设置：  

```text
// 毫秒 
jedis.getClient().setConnectionTimeout(time);
```

&emsp; 2）Redis发生阻塞，造成tcp-backlog已满，造成新的连接失败。  
&emsp; 3）客户端与服务端网络不正常。  

### 1.1.4. Redis使用的内存超过maxmemory配置  
&emsp; Jedis执行写操作时，如果Redis的使用内存大于maxmemory的设置，会收到下面的异常，此时应该调整maxmemory并找到造成内存增长的原因：  

```text
redis.clients.jedis.exceptions.JedisDataException:  
OOM command not allowed when used memory > 'maxmemory'.
```

### 1.1.5. 客户端连接数过大
&emsp; 如果客户端连接数超过了maxclients，新申请的连接就会出现如下异常：  

```text
redis.clients.jedis.exceptions.JedisDataException: ERR max number of clients reached
```

&emsp; 此时新的客户端连接执行任何命令，返回结果都是如下：  

```text
127.0.0.1:6379> get hello 
(error) ERR max number of clients reached
```

&emsp; 这个问题可能会比较棘手，因为此时无法执行Redis命令进行问题修复，一般来说可以从两个方面进行着手解决：  

* 客户端：如果maxclients参数不是很小的话，应用方的客户端连接数基本不会超过maxclients，通常来看是由于应用方对于Redis客户端使用不当造成的。此时如果应用方是分布式结构的话，可以通过下线部分应用节点（例如占用连接较多的节点），使得Redis的连接数先降下来。从而让绝大部分节点可以正常运行，此时再通过查找程序bug或者调整maxclients进行问题的修复。  
* 服务端：如果此时客户端无法处理，而当前Redis为高可用模式（例如Redis Sentinel和Redis Cluster），可以考虑将当前Redis做故障转移。 

&emsp; 此问题不存在确定的解决方式，但是无论从哪个方面进行处理，故障的快速恢复极为重要，当然更为重要的是找到问题的所在，否则一段时间后客户端连接数依然会超过maxclients。

## 1.2. 客户端案例分析  
### 1.2.1. Redis内存陡增  
1. 现象   
&emsp; 服务端现象：Redis主节点内存陡增，几乎用满maxmemory，而从节点内存并没有变化（正常情况下主从节点内存使用量基本相同），如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-112.png)  
<center>主从节点内存不一致，主节点内存陡增</center>
&emsp; 客户端现象：客户端产生了OOM异常，也就是Redis主节点使用的内存已经超过了maxmemory的设置，无法写入新的数据：  

    ```text
    redis.clients.jedis.exceptions.JedisDataException: OOM command not allowed when used memory > 'maxmemory'
    ```
2. 分析原因  
&emsp; 从现象看，可能的原因有两个。  
&emsp; 1）确实有大量写入，但是主从复制出现问题：查询了Redis复制的相关 信息，复制是正常的，主从数据基本一致。  
&emsp; 主节点的键个数： 

    ```text
    127.0.0.1:6379> dbsize 
    (integer) 2126870 
    ```
&emsp; 从节点的键个数： 

    ```text
    127.0.0.1:6380> dbsize 
    (integer) 2126870 
    ```
&emsp; 2）其他原因造成主节点内存使用过大：排查是否由客户端缓冲区造成 主节点内存陡增，使用info clients命令查询相关信息如下：  

    ```text
    127.0.0.1:6379> info clients
    # Clients 
    connected_clients:1891 
    client_longest_output_list:225698 
    client_biggest_input_buf:0 
    blocked_clients:0
    ```
&emsp; 很明显输出缓冲区不太正常，最大的客户端输出缓冲区队列已经超过了20万个对象，于是需要通过client list命令找到omem不正常的连接，一般来说大部分客户端的omem为0（因为处理速度会足够快），于是执行如下代码，找到omem非零的客户端连接：  

    ```text
    redis-cli client list | grep -v "omem=0"
    ```
&emsp; 找到了如下一条记录： 

    ```text
    id=7 addr=10.10.xx.78:56358 fd=6 name= age=91 idle=0 flags=O db=0 sub=0 psub=0 
    multi=-1 qbuf=0 qbuf-free=0 obl=0 oll=224869 omem=2129300608 events=rw cmd=monitor 
    ```
&emsp; 已经很明显是因为有客户端在执行monitor命令造成的。  
3. 处理方法和后期处理   
&emsp; 对这个问题处理的方法相对简单，只要使用client kill命令杀掉这个连接，让其他客户端恢复正常写数据即可。但是更为重要的是在日后如何及时 发现和避免这种问题的发生，基本有三点：  
    * 从运维层面禁止monitor命令，例如使用rename-command命令重置 monitor命令为一个随机字符串，除此之外，如果monitor没有做rename- command，也可以对monitor命令进行相应的监控（例如client list）。 
    * 从开发层面进行培训，禁止在生产环境中使用monitor命令，因为有时 候monitor命令在测试的时候还是比较有用的，完全禁止也不太现实。 
    * 限制输出缓冲区的大小。  

&emsp; 使用专业的Redis运维工具，例如CacheCloud，上述问题在Cachecloud中会收到相应的报警，快速发现和定位问题。


### 1.2.2. 客户端周期性的超时  
......

## 1.3. Redis的噩梦：阻塞  
......

## 1.4. 持久化问题定位与优化
......

## 1.5. 内存优化  
......


## 1.6. redis内存中的key分析

<!-- 

Redis爆了？快速定位Redis占用最高的key有哪些
https://blog.csdn.net/weixin_34199405/article/details/92266571



redis key占用内存量分析
https://blog.csdn.net/u013282159/article/details/85780942
-->

