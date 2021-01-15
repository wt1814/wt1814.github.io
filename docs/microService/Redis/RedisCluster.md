

<!-- TOC -->

- [1. Redis Cluster](#1-redis-cluster)
    - [1.1. 分片模式介绍](#11-分片模式介绍)
        - [1.1.1. 基于客户端分片](#111-基于客户端分片)
        - [1.1.2. 基于代理服务器分片](#112-基于代理服务器分片)
    - [1.2. Redis Cluster集群](#12-redis-cluster集群)
        - [Redis生产集群的部署架构](#redis生产集群的部署架构)
        - [1.2.1. 架构](#121-架构)
        - [1.2.2. 部署](#122-部署)
        - [1.2.3. 原理](#123-原理)
            - [1.2.3.1. Redis集群服务端](#1231-redis集群服务端)
                - [1.2.3.1.1. 数据分布](#12311-数据分布)
                    - [1.2.3.1.1.1. Redis数据分区](#123111-redis数据分区)
                    - [1.2.3.1.1.2. 集群功能限制](#123112-集群功能限制)
                - [1.2.3.1.2. 节点通信流程](#12312-节点通信流程)
                - [1.2.3.1.3. 集群伸缩](#12313-集群伸缩)
                    - [1.2.3.1.3.1. 伸缩原理](#123131-伸缩原理)
                    - [1.2.3.1.3.2. 扩容集群](#123132-扩容集群)
                    - [1.2.3.1.3.3. 收缩集群](#123133-收缩集群)
                - [1.2.3.1.4. 故障转移](#12314-故障转移)
                    - [1.2.3.1.4.1. 故障发现](#123141-故障发现)
                    - [1.2.3.1.4.2. 故障恢复](#123142-故障恢复)
            - [1.2.3.2. 服务端和客户端](#1232-服务端和客户端)
                - [1.2.3.2.1. 请求路由，客户端重定向](#12321-请求路由客户端重定向)
                    - [1.2.3.2.1.1. 请求重定向](#123211-请求重定向)
                    - [1.2.3.2.1.2. Smart客户端](#123212-smart客户端)
                    - [1.2.3.2.1.3. ASK重定向](#123213-ask重定向)
        - [1.2.4. 集群运维](#124-集群运维)
            - [1.2.4.1. 集群倾斜](#1241-集群倾斜)

<!-- /TOC -->

&emsp; **<font color = "red">参考《Redis开发与运维》</font>**
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-74.png)  

# 1. Redis Cluster  
<!-- 

一万字详解 Redis Cluster Gossip 协议 
https://mp.weixin.qq.com/s/aL5IEnGuq-9SuDcnmBEu_g

 Redis集群环境搭建实践 
 https://mp.weixin.qq.com/s/KT8kGgzn6TQwBGCUkMw_4g
-->

## 1.1. 分片模式介绍  
&emsp; **<font color = "lime">分片(sharding)是将数据拆分到多个Redis实例的过程，这样每个实例将只包含所有键的子集，</font>** 这种方法在解决某些问题时可以获得线性级别的性能提升。  
&emsp; **<font color = "red">根据执行分片的位置，可以分为三种分片方式：</font>**  

* 客户端分片：在客户端实现相关的逻辑，例如用取模或者一致性哈希对 key 进行分片，查询和修改都先判断 key 的路由。  
* 代理分片：把做分片处理的逻辑抽取出来，运行一个独立的代理服务，客户端连接到这个代理服务，代理服务做请求的转发。  
* 服务器分片：官方Redis Cluster。  

### 1.1.1. 基于客户端分片  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-18.png)  
&emsp; Redis Sharding是Redis Cluster出来之前，业界普遍使用的多Redis实例集群方法。其主要思想是基于哈希算法，根据Redis数据的key的哈希值对数据进行分片，将数据映射到各自节点上。  
&emsp; 优点在于实现简单，缺点在于当Redis集群调整，每个客户端都需要更新调整。  
&emsp; **在redis3.0版本之前的版本，可以通过redis客户端做sharding分片，比如jedis实现的ShardedJedisPool。**  

### 1.1.2. 基于代理服务器分片
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-19.png)  
&emsp; 客户端发送请求到独立部署代理组件，代理组件解析客户端的数据，并将请求转发至正确的节点，最后将结果回复给客户端。  
&emsp; 优点在于透明接入，容易集群扩展，缺点在于多了一层代理转发，性能有所损耗。  
&emsp; 典型的代理分区方案有 Twitter 开源的 Twemproxy 和国内的豌豆荚开源的 Codis。  

## 1.2. Redis Cluster集群  
&emsp; Redis Cluster是在3.0版本正式推出的高可用集群方案。  
&emsp; <font color = "red">Redis Cluster相比Redis Sentinel，Redis Cluster方案不需要额外部署Sentinel集群，而是通过集群内部通信实现集群监控，故障时主从切换；同时，支持内部基于哈希槽实现数据分片，支持动态水平扩容。</font>  
&emsp; Redis Cluster相比Codis，它是去中心化的，客户端可以连接到任意一个可用节点。  

&emsp; 特点：  
*  高性能  
    * **<font color = "lime">采用了异步复制机制，</font>** 向某个节点写入数据时，无需等待其它节点的写数据响应。  
    * **<font color = "lime">去中心化，无中心代理节点，每个节点保存数据和整个集群状态，每个节点都和其他所有节点连接，将客户端直接重定向到拥有数据的节点。</font>**  
    * 对于N个Master节点的Cluster，整体性能理论上相当于单个Redis的性能的N倍。  
* 高可用
    * **<font color = "lime">采用了主从复制的机制，Master节点失效时Slave节点自动提升为Master节点。如果Cluster中有N个Master节点，每个Master拥有1个Slave节点，那么这个Cluster的失效概率为1/(2*N-1)，可用概率为 1-1/(2*N-1)。</font>**  
* 高可扩展  
    * **<font color = "lime">可支持多达1000个服务节点。随时可以向 Cluster 中添加新节点，或者删除现有节点。Cluster中每个节点都与其它节点建立了相互连接。</font>**  

----
&emsp; 优势：  
1. 无中心架构。  
2. 数据按照slot存储分布在多个节点，节点间数据共享，可动态调整数据分布。  
3. 可扩展性，可线性扩展到1000个节点（官方推荐不超过1000个），节点可动态添加或删除。  
4. 高可用性，部分节点不可用时，集群仍可用。通过增加Slave做standby数据副本，能够实现故障自动failover，节点之间通过gossip协议交换状态信息，用投票机制完成Slave到Master的角色提升。   
5. 降低运维成本，提高系统的扩展性和可用性。  

&emsp; 不足：  
1. Client实现复杂，驱动要求实现Smart Client，缓存 slots mapping 信息并及时更新，提高了开发难度，客户端的不成熟影响业务的稳定性。   
2. 节点会因为某些原因发生阻塞（阻塞时间大于 clutser-node-timeout），被判断下线，这种failover是没有必要的。   
3. 数据通过异步复制，不保证数据的强一致性。  
4. 多个业务使用同一套集群时，无法根据统计区分冷热数据，资源隔离性较差，容易出现相互影响的情况。  

### Redis生产集群的部署架构  
<!-- 
https://mp.weixin.qq.com/s/dfj-_qYdK3emt965hj9_jw
-->
redis cluster，10 台机器，5 台机器部署了 redis 主实例，另外 5 台机器部署了 redis 的从实例， 每个主实例挂了一个从实例，5 个节点对外提供读写服务，每个节点的读写高峰 qps 可能可以达到每秒 5 万，5 台机器最多是 25 万读写请求/s。  
机器是什么配置？32G 内存+ 8 核 CPU + 1T 磁盘，但是分配给 redis 进程的是 10g 内存，一般线上生产环境，redis 的内存尽量不要超过 10g，超过 10g 可能会有问题。5 台机器对外提供读写，一共有 50g 内存。  
因为每个主实例都挂了一个从实例，所以是高可用的，任何一个主实例宕机，都会自动故障迁移，redis 从实例会自动变成主实例继续提供读写服务。  
你往内存里写的是什么数据？每条数据的大小是多少？商品数据，每条数据是 10kb。100 条数据是 1mb，10 万条数据是 1g。常驻内存的是 200 万条商品数据，占用内存是 20g，仅仅不到总内存的 50%。目前高峰期每秒就是 3500 左右的请求量。  


### 1.2.1. 架构  
&emsp; 服务器节点：3主3从，最少6个节点。其 Redis Cluster架构图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-30.png)  

### 1.2.2. 部署  
...

### 1.2.3. 原理  

#### 1.2.3.1. Redis集群服务端
##### 1.2.3.1.1. 数据分布  
###### 1.2.3.1.1.1. Redis数据分区  
&emsp; Redis集群并没有使用传统的一致性哈希来分配数据，而是采用哈希槽(hash slot)的方式来分配数据。Redis Cluster默认分配了16384（2∧14）个slot，set一个key时，redis采用CRC16算法进行键-槽（key->slot）之间的映射：HASH_SLOT（key）= CRC16(key) % 16384。其中 CRC16(key) 语句用于计算键key的CRC16校验和。key经过公式计算后得到所对应的哈希槽，而哈希槽被某个主节点管理，从而确定key在哪个主节点上存取。  

&emsp; **<font color = "lime">分区优点：RedisCluster将一个哈希槽从一个节点移动到另一个节点不会造成节点阻塞，所以无论是添加新节点还是移除已存在节点，又或者改变某个节点包含的哈希槽数量，都不会造成集群下线。</font>** 从而保证集群的可用性，使得用户可以很容易地向集群中添加或者删除节点。  
&emsp; 示例：现在三个主节点分别是：A, B, C三个节点，它们可以是一台机器上的三个端口，也可以是三台不同的服务器。那么，采用哈希槽 (hash slot)的方式来分配16384个slot 的话，它们三个节点分别承担的slot区间是：节点A覆盖0－5460；节点B覆盖5461－10922；节点C覆盖10923－16383。  
&emsp; 获取数据：如果存入一个值，按照redis cluster哈希槽的算法CRC16('key')384 = 6782。 那么就会把这个key 的存储分配到B上了。同样，当连接(A,B,C)任何一个节点想获取'key'这个key时，也会这样的算法，然后内部跳转到B节点上获取数据。  
&emsp; 新增一个主节点：新增一个节点D，redis cluster的这种做法是从各个节点的前面各拿取一部分slot到D上。大致就会变成这样：节点A覆盖1365-5460；节点B覆盖6827-10922；节点C覆盖12288-16383；节点D覆盖0-1364,5461-6826,10923-12287。  
&emsp; 同样删除一个节点也是类似，移动完成后就可以删除这个节点了。  

###### 1.2.3.1.1.2. 集群功能限制  
&emsp; **<font color = "red">Redis集群相对单机在功能上存在一些限制，</font>** 需要开发人员提前了解，在使用时做好规避。限制如下：  
1. <font color = "red">key批量操作支持有限。</font>如mset、mget，目前只支持具有相同slot值的 key执行批量操作。对于映射为不同slot值的key由于执行mget、mget等操作可能存在于多个节点上因此不被支持。  
2. key事务操作支持有限。同理只支持多key在同一节点上的事务操作，当多个key分布在不同的节点上时无法使用事务功能。   
3. key作为数据分区的最小粒度，因此不能将一个大的键值对象如hash、list等映射到不同的节点。   
4. 不支持多数据库空间。单机下的Redis可以支持16个数据库，集群模式下只能使用一个数据库空间，即db0。  
5. 复制结构只支持一层，从节点只能复制主节点，不支持嵌套树状复制结构。  

##### 1.2.3.1.2. 节点通信流程  
<!-- 

https://mp.weixin.qq.com/s/dfj-_qYdK3emt965hj9_jw
-->

&emsp; 在分布式存储中需要提供维护节点元数据信息的机制，所谓元数据是 指：节点负责哪些数据，是否出现故障等状态信息。常见的元数据维护方式分为：集中式和P2P方式。Redis集群采用P2P的Gossip（流言）协议，Gossip协议工作原理就是节点彼此不断通信交换信息，一段时间后所有的节点都会知道集群完整的信息，这种方式类似流言传播，如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-87.png)  
&emsp; 通信过程说明：  
1. <font color = "lime">集群中的每个节点都会单独开辟一个TCP通道，用于节点之间彼此通信，通信端口号在基础端口上加10000。</font>  
2. 每个节点在固定周期内通过特定规则选择几个节点发送ping消息。  
3. 接收到ping消息的节点用pong消息作为响应。  

&emsp; 集群中每个节点通过一定规则挑选要通信的节点，每个节点可能知道全 部节点，也可能仅知道部分节点，只要这些节点彼此可以正常通信，最终它们会达到一致的状态。当节点出故障、新节点加入、主从角色变化、槽信息变更等事件发生时，通过不断的ping/pong消息通信，经过一段时间后所有的节点都会知道整个集群全部节点的最新状态，从而达到集群状态同步的目的。  

##### 1.2.3.1.3. 集群伸缩  
###### 1.2.3.1.3.1. 伸缩原理  
&emsp; Redis集群提供了灵活的节点扩容和收缩方案。 **<font color = "lime">在不影响集群对外服务的情况下，可以为集群添加节点进行扩容也可以下线部分节点进行缩容。</font>**  
&emsp; Redis集群可以实现对节点的灵活上下线控制。其中原理可抽象为槽和对应数据在不同节点之间灵活移动。    

###### 1.2.3.1.3.2. 扩容集群  
&emsp; 扩容是分布式存储最常见的需求，Redis集群扩容操作可分为如下步骤：
1. 准备新节点。 
2. 加入集群。 
3. 迁移槽和数据。

###### 1.2.3.1.3.3. 收缩集群  
......

##### 1.2.3.1.4. 故障转移  
###### 1.2.3.1.4.1. 故障发现  
&emsp; 当集群内某个节点出现问题时，需要通过一种健壮的方式保证识别出节点是否发生了故障。<font color = "red">Redis集群内节点通过ping/pong消息实现节点通信，消息不但可以传播节点槽信息，还可以传播其他状态如：主从状态、节点故障等。</font>因此故障发现也是通过消息传播机制实现的， **<font color = "lime">主要环节包括：主观下线（pfail）和客观下线（fail）。</font>**   

* 主观下线：指某个节点认为另一个节点不可用，即下线状态，这个状态并不是最终的故障判定，只能代表一个节点的意见，可能存在误判情况。   
* 客观下线： **<font color = "red">当某个节点判断另一个节点主观下线后，该节点的下线报告会通过Gossip消息传播。当接收节点发现消息体中含有主观下线的节点，其会尝试对该节点进行客观下线，</font>** 依据下线报告是否在有效期内(如果在cluster-node-timeout*2时间内无法收集到一半以上槽节点的下线报告，那么之前的下线报告会过期)，且数量大于槽节点总数的一半。若是，则将该节点更新为客观下线，并向集群广播下线节点的fail消息。  

<!-- 
* 客观下线：指标记一个节点真正的下线，集群内多个节点都认为该节点不可用，从而达成共识的结果。如果是持有槽的主节点故障，需要为该节点进行故障转移。  
-->

###### 1.2.3.1.4.2. 故障恢复  
&emsp; **<font color = "lime">一句话概述：当从节点通过内部定时任务发现自身复制的主节点进入客观下线；从节点发起选举；其余主节点选举投票。</font>**  

&emsp; 故障节点变为客观下线后，如果下线节点是持有槽的主节点则需要在它的从节点中选出一个替换它，从而保证集群的高可用。下线主节点的所有从节点承担故障恢复的义务，<font color = "red">当从节点通过内部定时任务发现自身复制的主节点进入客观下线时，将会触发故障恢复流程。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-50.png)  

1. 资格检查  
&emsp; 每个从节点都要检查最后与主节点断线时间，判断是否有资格替换故障的主节点。如果从节点与主节点断线时间超过cluster-node-timeout*cluster-slave-validity-factor，则当前从节点不具备故障转移资格。  
2. 准备选举时间  
&emsp; 从节点符合故障转移资格后，更新触发故障选举时间，只有到达该时间才能执行后续流程。采用延迟触发机制，主要是对多个从节点使用不同的延迟选举时间来支持优先级。复制偏移量越大说明从节点延迟越低，那么它应该具有更高的优先级。  
3. 发起选举  
&emsp; 当从节点到达故障选举时间后，会触发选举流程：  
    1. 更新配置纪元  
    &emsp; 配置纪元是一个只增不减的整数，每个主节点自身维护一个配置纪元，标示当前主节点的版本，所有主节点的配置纪元都不相等，从节点会复制主节点的配置纪元。整个集群又维护一个全局的配置纪元，用于记录集群内所有主节点配置纪元的最大版本。每次集群发生重大事件，如新加入主节点或由从节点转换而来，从节点竞争选举，都会递增集群全局配置纪元并赋值给相关主节点，用于记录这一关键事件。  
    2. 广播选举消息  
    &emsp; 在集群内广播选举消息，并记录已发送过消息的状态，保证该从节点在一个配置纪元内只能发起一次选举。  
4. 选举投票  
&emsp; <font color = "red">只有持有槽的主节点才会处理故障选举消息，</font>每个持有槽的节点在一个配置纪元内都有唯一的一张选票，当接到第一个请求投票的从节点消息，回复消息作为投票，之后相同配置纪元内其它从节点的选举消息将忽略。投票过程其实是一个领导者选举的过程。  
&emsp; 每个配置纪元代表了一次选举周期，如果在开始投票后的cluster-node-timeout*2时间内从节点没有获取足够数量的投票，则本次选举作废。从节点对配置纪元自增并发起下一轮投票，直到选举成功为止。  
5. 替换主节点  
&emsp; 当前从节点取消复制变为主节点，撤销故障主节点负责的槽，把这些槽委派给自己，并向集群广播告知所有节点当前从节点变为主节点。

-----

#### 1.2.3.2. 服务端和客户端  
##### 1.2.3.2.1. 请求路由，客户端重定向  
&emsp; Redis集群对客户端通信协议做了比较大的修改， 为了追求性能最大化，并没有采用代理的方式而是采用客户端直连节点的方式。  

###### 1.2.3.2.1.1. 请求重定向  
&emsp; **<font color = "lime">在集群模式下，Redis接收任何键相关命令时首先计算键对应的槽，再根据槽找出所对应的节点，如果节点是自身，则处理键命令；否则回复MOVED重定向错误，通知客户端请求正确的节点。这个过程称为MOVED重定向。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-48.png)  

###### 1.2.3.2.1.2. Smart客户端  
&emsp; 大多数开发语言的Redis客户端都采用Smart客户端支持集群协议，客户端如何选择见：http://redis.io/clients ，从中找出符合自己要求的客户端类 库。Smart客户端通过在内部维护slot→node的映射关系，本地就可实现键到 节点的查找，从而保证IO效率的最大化，而MOVED重定向负责协助Smart客户端更新slot→node映射。  
&emsp; Jedis为Redis Cluster提供了Smart客户端，对应的类是JedisCluster。  

###### 1.2.3.2.1.3. ASK重定向  
&emsp; **<font color = "lime">Redis集群支持在线迁移槽（slot）和数据来完成水平伸缩，当slot对应的数据从源节点到目标节点迁移过程中，客户端需要做到智能识别，保证键命令可正常执行。例如当一个slot数据从源节点迁移到目标节点时，期间可 能出现一部分数据在源节点，而另一部分在目标节点。</font>**   
&emsp; 当出现上述情况时，客户端键命令执行流程将发生变化，如下所示： 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-49.png)   
1. 客户端根据本地slots缓存发送命令到源节点，如果存在键对象则直 接执行并返回结果给客户端。  
2. **<font color = "lime">如果键对象不存在，则可能存在于目标节点，这时源节点会回复ASK重定向异常。格式如下：（error）ASK{slot}{targetIP}：{targetPort}。</font>**   
3. 客户端从ASK重定向异常提取出目标节点信息，发送asking命令到目 标节点打开客户端连接标识，再执行键命令。如果存在则执行，不存在则返回不存在信息。   

&emsp; ASK与MOVED虽然都是对客户端的重定向控制，但是有着本质区别。ASK重定向说明集群正在进行slot数据迁移，客户端无法知道什么时候迁移完成，因此只能是临时性的重定向，客户端不会更新slots缓存。但是MOVED重定向说明键对应的槽已经明确指定到新的节点，因此需要更新slots缓存。  



### 1.2.4. 集群运维  
#### 1.2.4.1. 集群倾斜  

&emsp; 集群倾斜指不同节点之间数据量和请求量出现明显差异，这种情况将加大负载均衡和开发运维的难度。因此需要理解哪些原因会造成集群倾斜，从而避免这一问题。   
1. 数据倾斜  
&emsp; 数据倾斜主要分为以下几种： 

    * 节点和槽分配严重不均。 
    * 不同槽对应键数量差异过大。 
    * 集合对象包含大量元素。 
    * 内存相关配置不一致。

2. 请求倾斜  
&emsp; 集群内特定节点请求量/流量过大将导致节点之间负载不均，影响集群均衡和运维成本。常出现在热点键场景，当键命令消耗较低时如小对象的get、set、incr等，即使请求量差异较大一般也不会产生负载严重不均。但是当热点键对应高算法复杂度的命令或者是大对象操作如hgetall、smembers等，会导致对应节点负载过高的情况。避免方式如下：  

    * 合理设计键，热点大集合对象做拆分或使用hmget替代hgetall避免整体读取。
    * 不要使用热键作为hash_tag，避免映射到同一槽。 
    * 对于一致性要求不高的场景，客户端可使用本地缓存减少热键调用。   



<!-- 

 5. 集群优雅扩容  
&emsp; 程序如果能够知道Redis集群地址产生了变化，重新设置一下jedis客户端的连接配置。现在的问题就是如何知道Redis集群地址发生了改变？
可以采用把Redis的集群地址配置在zookeeper中，应用在启动的时候，获取zk上的集群地址的值，进行初始化。如果想要改变集群地址，要在zk上面进行设置。  
&emsp; zk重要的特性就是监听特性，节点发生变化，就会立刻把变化发送给应用，从而应用获取到值，重新设置jedis客户端连接。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-26.png)  
-->


