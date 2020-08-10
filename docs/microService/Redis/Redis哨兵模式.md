
<!-- TOC -->

- [1. 哨兵模式](#1-哨兵模式)
    - [1.1. 架构-1](#11-架构-1)
    - [1.2. 部署及故障转移演示](#12-部署及故障转移演示)
    - [1.3. 哨兵原理](#13-哨兵原理)
        - [1.3.1. 心跳检查](#131-心跳检查)
        - [1.3.2. 主观下线、客观下线](#132-主观下线客观下线)
            - [1.3.2.1. 主观下线](#1321-主观下线)
            - [1.3.2.2. 客观下线](#1322-客观下线)
        - [1.3.3. Sentinel选举](#133-sentinel选举)
        - [1.3.4. 故障转移](#134-故障转移)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-75.png)  


# 1. 哨兵模式  
&emsp; 主从模式弊端：当Master宕机后，Redis集群将不能对外提供写入操作，需要手动将一个从节点晋升为主节点，同时需要修改应用方的主节点地址， 还需要命令其他从节点去复制新的主节点， 整个过程都需要人工干预。在 Redis 2.8 提供比较完善的解决方案：Redis Sentinel。Redis Sentinel 是一个能够自动完成故障发现和故障转移并通知应用方，从而实现真正的高可用的分布式架构。下面是Redis官方文档对于哨兵功能的描述：  
<!-- 哨兵，英文名 Sentinel，是一个分布式系统，用于对主从结构中的每一台服务器进行监控，当主节点出现故障后通过投票机制来挑选新的主节点，并且将所有的从节点连接到新的主节点上。-->

* 监控（Monitoring）：哨兵会不断地检查主节点和从节点是否运作正常。  
* 自动故障转移（Automatic failover）：当主节点不能正常工作时，哨兵会开始自动故障转移操作，它会将失效主节点的其中一个从节点升级为新的主节点，并让其他从节点改为复制新的主节点。  
* 配置提供者（Configuration provider）：客户端在初始化时，通过连接哨兵来获得当前Redis服务的主节点地址。  
* 通知（Notification）：哨兵可以将故障转移的结果发送给客户端。  

&emsp; <font color="red">监控和自动故障转移使得 Sentinel 能够完成主节点故障发现和自动转移，配置提供者和通知则是实现通知客户端主节点变更的关键。</font>  

## 1.1. 架构-1  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-28.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-29.png)  
&emsp; <font color = "red">Redis哨兵架构中主要包括两个部分：Redis Sentinel集群和Redis数据集群。</font>  

* 哨兵节点：哨兵系统由若干个哨兵节点组成。其实哨兵节点是一个特殊的 Redis 节点，<font color="red">哨兵节点不存储数据的和仅支持部分命令。配节点数量要满足2n+1（n>=1）的奇数个。</font>  
* 数据节点：由主节点和从节点组成的数据节点。  

## 1.2. 部署及故障转移演示    
......

## 1.3. 哨兵原理  
### 1.3.1. 心跳检查  
&emsp; Sentinel 通过三个定时任务来完成对各个节点的发现和监控，这是保证 Redis 高可用的重要机制。  
1. 每隔 10 秒，每个 Sentinel 节点会向已知的主从节点发送 info 命令获取最新的主从架构。下图是 info 命令的响应。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-46.png)  
&emsp; Sentinel 节点通过解析响应信息，就可以知道当前 Redis 数据节点的最新拓扑结构。如果是新增的节点，Sentinel 就会与其建立连接。  
2. 每隔 2 秒，Sentinel 节点都会向主从节点的 \_sentinel_:hello 频道发送自己的信息。目的有两个：  

    * 发现新的 Sentinel 节点  
    * Sentinel 节点之间交换主节点的状态，作为后面客观下线以及领导者选择的依据。  

    &emsp; 发送的消息内容格式为：  

        <哨兵地址>,<哨兵端口>,<哨兵的运行ID>,<哨兵的配置版本>,
        <主数据库的名称>,<主数据库的地址>,<主数据库的端口>,<主数据库的配置版本>

3. 每隔一秒，哨兵会每个主从节点、Sentinel 节点发送 PING 命令。该定时任务是哨兵心跳机制中的核心，它涉及到 Redis 数据节点的运行状态监控，哨兵领导者的选举等细节操作。当哨兵节点发送 PING 命令后，若超过 down-after-milliseconds 后，当前哨兵节点会认为该节点主观下线。  

### 1.3.2. 主观下线、客观下线  
#### 1.3.2.1. 主观下线  
&emsp; 在第三个定时任务中，每个一秒 Sentinel 节点会向每个 Redis 数据节点发送 PING 命令，若超过 down-after-milliseconds 设定的时间没有收到响应，则会对该节点做失败判定，这种行为叫做 主观下线。因为该行为是当前节点的一家之言，所以会存在误判的可能。  

#### 1.3.2.2. 客观下线  
&emsp; 当 Sentinel 节点判定一个主节点为主观下线后，则会通过 sentinelis-master-down-by-addr 命令询问其他 Sentinel 节点对该主节点的状态，当有超过 <quorunm\> 个 Sentinel 节点认为主节点存在问题，这时该 Sentinel节点会对主节点做客观下线的决定。  
&emsp; 这里有一点需要注意的是，客观下线是针对主机节点，如果主观下线的是从节点或者其他 Sentinel 节点，则不会进行后面的客观下线和故障转移了。  

### 1.3.3. Sentinel选举  
&emsp; 假如一个 Sentinel 节点完成了主节点的客观下线，那么是不是就可以立刻进行故障转移呢？显然不是，因为 Redis 的故障转移工作只需要一个 Sentinel 节点来完成，所以会有一个选举的过程，选举出来一个领导者来完成故障转移工作。Redis 节点采用 Raft 算法来完成领导者写选举。    

&emsp; Sentinel 选举的主要流程：
1. 每一个做主观下线的 Sentinel 节点都有成为领导者的可能，他们会想其他 Sentinel 节点发送 sentinelis-master-down-by-addr ，要求将它设置为领导者。    
2. 收到命令的 Sentinel 节点如果没有同意其他 Sentinel 节点发送的命令，则会同意该请求，否则拒绝。     
3. 如果该 Sentinel 节点发现自己得到的票数已经超过半数且超过 <quorum\>，那么它将成为领导者。     
4. 如果该过程有多个 Sentinel 成为领导者，那么将等待一段时间重新进行选择，直到有且只有一个 Sentinel 节点成为领导者为止。    

&emsp; 假如有 A、B、C、D 四个节点构成 Sentinel 集群。如果 A 率先完成客观下线，则 A 会向 B、C、D 发出成为领导者的申请，由于 B、C、D 没有同意过其他 Sentinel 节点，所以会将投票给 A，A 得到三票。B 则向 A、C、D 发起申请，由于 C、D 已经同意了 A，所以会拒绝，但是他会得到 A 的同意，所以 B 得到一票，同理 C、D 得到零票，如下图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-47.png)  
&emsp; 所以 A 会成为领导者，进行故障转移工作。一般来说，哨兵选择的过程很快，谁先完成客观下线，一般就能成为领导者。  

### 1.3.4. 故障转移  
&emsp; 当某个 Sentinel 节点通过选举成为了领导者，则它要承担起故障转移的工作，其具体步骤如下：  
1. 在从节点列表中选择一个节点作为新的主节点，选择的策略如下： 
    * 过滤掉不健康的节点（主观下线、断线），5 秒内没有回复过 Sentinel 节点 ping 响应、与主节点失联超过 down-after-milliseconds*10秒  
    * 选择优先级最高级别的节点，如果不存在则继续  
    * 选择复制偏移量最大的节点（数据最完整），存在则返回，不存在则继续  
    * 选择 runid 最小的节点  
2. 在新的主节点上执行 slaveofnoone，让其变成主节点  
3. 向剩余的从节点发送命令，让它们成为新主节点的从节点  

<!-- 
 3.4. Sentinel（哨兵）进程的工作方式：  
1. 每个Sentinel（哨兵）进程以每秒钟一次的频率向整个集群中的Master主服务器，Slave从服务器以及其他Sentinel（哨兵）进程发送一个 PING 命令。  
2. 如果一个实例（instance）距离最后一次有效回复 PING 命令的时间超过 down-after-milliseconds 选项所指定的值， 则这个实例会被 Sentinel（哨兵）进程标记为主观下线（SDOWN）。  
3. 如果一个Master主服务器被标记为主观下线（SDOWN），则正在监视这个Master主服务器的所有 Sentinel（哨兵）进程要以每秒一次的频率确认Master主服务器的确进入了主观下线状态。  
4. 当有足够数量的 Sentinel（哨兵）进程（大于等于配置文件指定的值）在指定的时间范围内确认Master主服务器进入了主观下线状态（SDOWN）， 则Master主服务器会被标记为客观下线（ODOWN）。  
5. 在一般情况下， 每个 Sentinel（哨兵）进程会以每 10 秒一次的频率向集群中的所有Master主服务器、Slave从服务器发送 INFO 命令。
6. 当Master主服务器被 Sentinel（哨兵）进程标记为客观下线（ODOWN）时，Sentinel（哨兵）进程向下线的 Master主服务器的所有 Slave从服务器发送 INFO 命令的频率会从 10 秒一次改为每秒一次。  
7. 若没有足够数量的 Sentinel（哨兵）进程同意 Master主服务器下线， Master主服务器的客观下线状态就会被移除。若 Master主服务器重新向 Sentinel（哨兵）进程发送 PING 命令返回有效回复，Master主服务器的主观下线状态就会被移除。  
-->

