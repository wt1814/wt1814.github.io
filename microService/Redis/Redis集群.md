---
title: Redis集群
date: 2020-05-17 00:00:00
tags:
    - Redis
---

Redis部署方式：单机、分片模式、主从复制模式、哨兵模式、集群模式。  

# 分片模式  
&emsp; 分片(sharding)是将数据拆分到多个Redis实例的过程，这样每个实例将只包含所有键的子集，这种方法在解决某些问题时可以获得线性级别的性能提升。  
&emsp; 假设有4个Redis实例R0, R1, R2, R3, 还有很多表示用户的键user:1, user:2, … , 有不同的方式来选择一个指定的键存储在哪个实例中。  
&emsp; 最简单的是范围分片，例如用户id从0 ~ 1000的存储到实例R0中，用户id从1001 ~ 2000的存储到实例R1中，等等。但是这样需要维护一张映射范围表，维护操作代价高。  
&emsp; 还有一种是哈希分片。使用CRC32哈希函数将键转换为一个数字，再对实例数量求模就能知道存储的实例。  
&emsp; 根据执行分片的位置，可以分为三种分片方式：  
* 客户端分片：客户端使用一致性哈希等算法决定应当分布到哪个节点。
* 代理分片：将客户端的请求发送到代理上，由代理转发到正确的节点上。
* 服务器分片：Redis Cluster。

&emsp; 基于客户端分片  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-18.png)  
&emsp; Redis Sharding是Redis Cluster出来之前，业界普遍使用的多Redis实例集群方法。其主要思想是基于哈希算法，根据Redis数据的key的哈希值对数据进行分片，将数据映射到各自节点上。  
&emsp; 优点在于实现简单，缺点在于当Redis集群调整，每个客户端都需要更新调整。  
&emsp; ***在redis3.0版本之前的版本，可以通过redis客户端做sharding分片，比如jedis实现的ShardedJedisPool。***  
&emsp; 基于代理服务器分片
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-19.png)  
&emsp; 客户端发送请求到独立部署代理组件，代理组件解析客户端的数据，并将请求转发至正确的节点，最后将结果回复给客户端。  
&emsp; 优点在于透明接入，容易集群扩展，缺点在于多了一层代理转发，性能有所损耗。  

# 主从复制模式：  

## 主从复制的作用  
* 数据冗余：主从复制实现了数据的热备份，是持久化之外的一种数据冗余方式。  
* 故障恢复：当主节点出现问题时，可以由从节点提供服务，实现快速的故障恢复；实际上是一种服务的冗余。  
* 负载均衡：在主从复制的基础上，配合读写分离，可以由主节点提供写服务，由从节点提供读服务（即写Redis数据时应用连接主节点，读Redis数据时应用连接从节点），分担服务器负载；尤其是在写少读多的场景下，通过多个从节点分担读负载，可以大大提高Redis服务器的并发量。  
* 读写分离：可以用于实现读写分离，主库写、从库读，读写分离不仅可以提高服务器的负载能力，同时可根据需求的变化，改变从库的数量；  
* 高可用基石：除了上述作用以外，主从复制还是哨兵和集群能够实施的基础，因此说主从复制是Redis高可用的基础。  

## 主从复制启用：  
&emsp; 临时配置：redis-cli进入redis从节点后，使用 --slaveof [masterIP] [masterPort]  
&emsp; 永久配置：进入从节点的配置文件redis.conf，增加slaveof [masterIP] [masterPort]  

## 复制流程  
&emsp; 主从复制过程大体可以分为3个阶段：连接建立阶段（即准备阶段）、数据同步阶段、命令传播阶段。  
&emsp; 在从节点执行 slaveof 命令后，复制过程便开始运作，下面图示大概可以看到，从图中可以看出复制过程大致分为6个过程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-20.png)  
&emsp; 主从配置之后的日志记录也可以看出这个流程  
&emsp; 1）保存主节点（master）信息。  
&emsp; 执行 slaveof 后 Redis 会打印如下日志：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-21.png)  

&emsp; 2）从节点（slave）内部通过每秒运行的定时任务维护复制相关逻辑，当定时任务发现存在新的主节点后，会尝试与该节点建立网络连接  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-22.png)  
&emsp; 从节点与主节点建立网络连接  
&emsp; 从节点会建立一个 socket 套接字，从节点建立了一个端口为51234的套接字，专门用于接受主节点发送的复制命令。从节点连接成功后打印如下日志：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-23.png)  

&emsp; 如果从节点无法建立连接，定时任务会无限重试直到连接成功或者执行 slaveof no one 取消复制  

&emsp; 关于连接失败，可以在从节点执行 info replication 查看 master_link_down_since_seconds 指标，它会记录与主节点连接失败的系统时间。从节点连接主节点失败时也会每秒打印如下日志，方便发现问题：  

```
# Error condition on socket for SYNC: {socket_error_reason}
```

&emsp; 3）发送 ping 命令。  
&emsp; 连接建立成功后从节点发送 ping 请求进行首次通信，ping 请求主要目的如下：  
* 检测主从之间网络套接字是否可用。  
* 检测主节点当前是否可接受处理命令。  

&emsp; 如果发送 ping 命令后，从节点没有收到主节点的 pong 回复或者超时，比如网络超时或者主节点正在阻塞无法响应命令，从节点会断开复制连接，下次定时任务会发起重连。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-24.png)  
&emsp; 从节点发送的 ping 命令成功返回，Redis 打印如下日志，并继续后续复制流程：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-25.png)  
&emsp; 4）权限验证。如果主节点设置了 requirepass 参数，则需要密码验证，从节点必须配置 masterauth 参数保证与主节点相同的密码才能通过验证；如果验证失败复制将终止，从节点重新发起复制流程。  
&emsp; 5）同步数据集。主从复制连接正常通信后，对于首次建立复制的场景，主节点会把持有的数据全部发送给从节点，这部分操作是耗时最长的步骤。  
&emsp; 6）命令持续复制。当主节点把当前的数据同步给从节点后，便完成了复制的建立流程。接下来主节点会持续地把写命令发送给从节点，保证主从数据一致性。  

### 数据同步步骤详解：  
&emsp; redis 2.8之前使用sync [runId] [offset]同步命令，redis2.8之后使用psync [runId] [offset]命令。两者不同在于，sync命令仅支持全量复制过程，psync支持全量和部分复制；  
&emsp; Psync命令具有完整重同步和部分重同步两种模式：  
* 完整同步用于处理初次复制情况：  
&emsp; 完整重同步的执行步骤和 Sync 命令执行步骤一致，都是通过让主服务器创建并发送 RDB 文件，以及向从服务器发送保存在缓冲区的写命令来进行同步。  
* 部分重同步是用于处理断线后重复制情况：  
&emsp; 当从服务器在断线后重新连接主服务器时，主服务可以将主从服务器连接断开期间执行的写命令发送给从服务器，从服务器只要接收并执行这些写命令，就可以将数据库更新至主服务器当前所处的状态。  

# 哨兵模式（监控）  





# 集群优雅扩容  
&emsp; 程序如果能够知道Redis集群地址产生了变化，重新设置一下jedis客户端的连接配置。现在的问题就是如何知道Redis集群地址发生了改变？
可以采用把Redis的集群地址配置在zookeeper中，应用在启动的时候，获取zk上的集群地址的值，进行初始化。如果想要改变集群地址，要在zk上面进行设置。  
&emsp; zk重要的特性就是监听特性，节点发生变化，就会立刻把变化发送给应用，从而应用获取到值，重新设置jedis客户端连接。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-26.png)  








