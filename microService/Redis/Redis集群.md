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

# 哨兵模式  
&emsp; 主从模式弊端：当Master宕机后，Redis集群将不能对外提供写入操作，需要手动将一个从节点晋升为主节点，同时需要修改应用方的主节点地址， 还需要命令其他从节点去复制新的主节点， 整个过程都需要人工干预。Redis Sentinel可解决这一问题。    
&emsp; Redis Sentinel中的概念：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-27.png)  

## Sentinel（哨兵）进程的作用：  
* 自动故障迁移：主从切换（在Master宕机后，将其中一个Slave转为Master，其他的Slave从该节点同步数据）。
当一个Master不能正常工作时，哨兵(sentinel) 会开始一次自动故障迁移操作，它会将失效Master的其中一个Slave升级为新的Master, 并让失效Master的其他Slave改为复制新的Master；当客户端试图连接失效的Master时，集群也会向客户端返回新Master的地址，使得集群可以使用现在的Master替换失效Master。Master和Slave服务器切换后，Master的redis.conf、Slave的redis.conf和sentinel.conf的配置文件的内容都会发生相应的改变，即，Master主服务器的redis.conf配置文件中会多一行slaveof的配置，sentinel.conf的监控目标会随之调换。  
* 监控：检查主从服务器是否运行正常；  
* 提醒：通过API向管理员或者其它应用程序发送故障通知；  

## Sentinel（哨兵）进程的工作方式：  
1. 每个Sentinel（哨兵）进程以每秒钟一次的频率向整个集群中的Master主服务器，Slave从服务器以及其他Sentinel（哨兵）进程发送一个 PING 命令。  
2. 如果一个实例（instance）距离最后一次有效回复 PING 命令的时间超过 down-after-milliseconds 选项所指定的值， 则这个实例会被 Sentinel（哨兵）进程标记为主观下线（SDOWN）。  
3. 如果一个Master主服务器被标记为主观下线（SDOWN），则正在监视这个Master主服务器的所有 Sentinel（哨兵）进程要以每秒一次的频率确认Master主服务器的确进入了主观下线状态。  
4. 当有足够数量的 Sentinel（哨兵）进程（大于等于配置文件指定的值）在指定的时间范围内确认Master主服务器进入了主观下线状态（SDOWN）， 则Master主服务器会被标记为客观下线（ODOWN）。  
5. 在一般情况下， 每个 Sentinel（哨兵）进程会以每 10 秒一次的频率向集群中的所有Master主服务器、Slave从服务器发送 INFO 命令。
6. 当Master主服务器被 Sentinel（哨兵）进程标记为客观下线（ODOWN）时，Sentinel（哨兵）进程向下线的 Master主服务器的所有 Slave从服务器发送 INFO 命令的频率会从 10 秒一次改为每秒一次。  
7. 若没有足够数量的 Sentinel（哨兵）进程同意 Master主服务器下线， Master主服务器的客观下线状态就会被移除。若 Master主服务器重新向 Sentinel（哨兵）进程发送 PING 命令返回有效回复，Master主服务器的主观下线状态就会被移除。  

## Sentinel部署架构  
R&emsp; edis Sentinel部署架构主要包括两部分：Redis Sentinel集群和Redis数据集群。  
&emsp; 其中Redis Sentinel集群是由若干Sentinel节点组成的分布式集群，可以实现故障发现、故障自动转移、配置中心和客户端通知。Redis Sentinel的节点数量要满足2n+1（n>=1）的奇数个。   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-28.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-29.png)  

# Redis Cluster集群  
&emsp; Redis Cluster是在3.0版本正式推出的高可用集群方案，相比Redis Sentinel，Redis Cluster方案不需要额外部署Sentinel集群，而是通过集群内部通信实现集群监控，故障时主从切换；同时，支持内部基于哈希实现数据分片，支持动态水平扩容。  

## 结构设计  
&emsp; Redis Cluster采用无中心结构，每个节点保存数据和整个集群状态，每个节点都和其他所有节点连接。服务器节点：3主3从，最少6个节点。其 Redis Cluster架构图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-30.png)  
*  高性能  
    * 采用了异步复制机制，向某个节点写入数据时，无需等待其它节点的写数据响应。  
    * 无中心代理节点，而是将客户端直接重定向到拥有数据的节点。  
    * 对于N个Master节点的Cluster ，整体性能理论上相当于单个Redis的性能的N倍。  

* 高可用
    * 采用了主从复制的机制，Master节点失效时Slave节点自动提升为Master节点。如果Cluster中有N个Master节点，每个Master拥有1个Slave节点，那么这个Cluster的失效概率为1/(2*N-1)，可用概率为 1-1/(2*N-1)。  
* 高可扩展  
    * 可支持多达1000个服务节点。随时可以向 Cluster 中添加新节点，或者删除现有节点。Cluster中每个节点都与其它节点建立了相互连接  

## 集群主节点分配  
&emsp; Redis集群并没有使用传统的一致性哈希来分配数据，而是采用哈希槽(hash slot)的方式来分配数据。Redis Cluster默认分配了16384（2∧14）个slot，set一个key时，redis采用CRC16算法进行键-槽（key->slot）之间的映射。  
&emsp; HASH_SLOT（key）= CRC16(key) % 16384  
&emsp; 其中 CRC16(key) 语句用于计算键key的CRC16校验和。key经过公式计算后得到所对应的哈希槽，而哈希槽被某个主节点管理，从而确定key在哪个主节点上存取。  
&emsp; 分区好处：RedisCluster将一个哈希槽从一个节点移动到另一个节点不会造成节点阻塞，所以无论是添加新节点还是移除已存在节点，又或者改变某个节点包含的哈希槽数量，都不会造成集群下线。从而保证集群的可用性，使得用户可以很容易地向集群中添加或者删除节点。  
&emsp; 示例：现在三个主节点分别是：A, B, C三个节点，它们可以是一台机器上的三个端口，也可以是三台不同的服务器。那么，采用哈希槽 (hash slot)的方式来分配16384个slot 的话，它们三个节点分别承担的slot区间是：节点A覆盖0－5460；节点B覆盖5461－10922；节点C覆盖10923－16383。  
&emsp; 获取数据：如果存入一个值，按照redis cluster哈希槽的算法CRC16('key')384 = 6782。 那么就会把这个key 的存储分配到B上了。同样，当连接(A,B,C)任何一个节点想获取'key'这个key时，也会这样的算法，然后内部跳转到B节点上获取数据。       
&emsp; 新增一个主节点：新增一个节点D，redis cluster的这种做法是从各个节点的前面各拿取一部分slot到D上，我会在接下来的实践中实验。大致就会变成这样：节点A覆盖1365-5460；节点B覆盖6827-10922；节点C覆盖12288-16383；节点D覆盖0-1364,5461-6826,10923-12287。  
&emsp; 同样删除一个节点也是类似，移动完成后就可以删除这个节点了。  


## 请求路由  
&emsp; 参考《Redis开发与运维》  

## 故障转移  
&emsp; 参考《Redis开发与运维》  


# 集群优雅扩容  
&emsp; 程序如果能够知道Redis集群地址产生了变化，重新设置一下jedis客户端的连接配置。现在的问题就是如何知道Redis集群地址发生了改变？
可以采用把Redis的集群地址配置在zookeeper中，应用在启动的时候，获取zk上的集群地址的值，进行初始化。如果想要改变集群地址，要在zk上面进行设置。  
&emsp; zk重要的特性就是监听特性，节点发生变化，就会立刻把变化发送给应用，从而应用获取到值，重新设置jedis客户端连接。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-26.png)  








