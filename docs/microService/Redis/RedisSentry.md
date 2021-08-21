
<!-- TOC -->

- [1. 哨兵模式](#1-哨兵模式)
    - [1.1. 架构](#11-架构)
    - [1.2. 哨兵原理](#12-哨兵原理)
        - [1.2.1. 心跳检查](#121-心跳检查)
            - [1.2.1.1. 定时任务一](#1211-定时任务一)
            - [1.2.1.2. 定时任务二](#1212-定时任务二)
            - [1.2.1.3. 定时任务三](#1213-定时任务三)
        - [1.2.2. 主观下线、客观下线](#122-主观下线客观下线)
        - [1.2.3. 故障转移(主节点选举)](#123-故障转移主节点选举)
        - [1.2.4. Sentinel选举](#124-sentinel选举)
    - [1.3. 部署及故障转移演示](#13-部署及故障转移演示)

<!-- /TOC -->

<!-- 
***Redis中主、从库宕机如何恢复？
 https://mp.weixin.qq.com/s/pO23ASPrc46BoPkRnQsPXQ

~~
Redis如何实现故障自动恢复？ 
https://mp.weixin.qq.com/s/uUNIdeRLDZb-Unx_HmxL9g
-->


&emsp; **<font color = "red">总结：</font>**  
1. <font color="clime">监控和自动故障转移使得Sentinel能够完成主节点故障发现和自动转移，配置提供者和通知则是实现通知客户端主节点变更的关键。</font>  
2. <font color = "clime">Redis哨兵架构中主要包括两个部分：Redis Sentinel集群和Redis数据集群。</font>  
3. **<font color = "clime">哨兵原理：</font>**  
    * **<font color = "red">心跳检查：Sentinel通过三个定时任务来完成对各个节点的发现和监控，这是保证Redis高可用的重要机制。</font>**  
        * 每隔10秒，每个Sentinel节点会向主节点和从节点发送`info命令` **<font color = "clime">获取最新的拓扑结构。</font>**   
        * 每隔2秒，每个Sentinel节点会向Redis数据节点的`__sentinel__：hello频道`上发送该Sentinel节点对于主节点的判断以及当前Sentinel节点的信息，同时每个Sentinel节点也会订阅该频道， **<font color = "clime">了解其他Sentinel节点以及它们对主节点的判断。</font>**  
        * 每隔1秒，每个Sentinel节点会向主节点、从节点、其余Sentinel节点发送一条`ping命令` **<font color = "clime">做一次心跳检测。</font>**  
    * **<font color = "red">主观下线和客观下线：</font>** 首先单个Sentinel节点认为数据节点主观下线，询问其他Sentinel节点，Sentinel多数节点认为主节点存在问题，这时该 Sentinel节点会对主节点做客观下线的决定。
    * **<font color = "red">故障转移/主节点选举：</font>** Sentinel节点的领导者根据策略在从节点中选择主节点。    
    * **<font color = "red">Sentinel选举：</font>** Sentinel集群是集中式架构，基于raft算法。  

# 1. 哨兵模式  
&emsp; **<font color = "red">参考《Redis开发与运维》</font>**

&emsp; 主从模式弊端：当Master宕机后，Redis集群将不能对外提供写入操作，需要手动将一个从节点晋升为主节点，同时需要修改应用方的主节点地址，还需要命令其他从节点去复制新的主节点，整个过程都需要人工干预。在Redis 2.8提供比较完善的解决方案：Redis Sentinel。  
&emsp; Redis Sentinel是一个能够自动完成故障发现和故障转移并通知应用方，从而实现真正的高可用的分布式架构。下面是Redis官方文档对于哨兵功能的描述：  
<!-- 哨兵，英文名 Sentinel，是一个分布式系统，用于对主从结构中的每一台服务器进行监控，当主节点出现故障后通过投票机制来挑选新的主节点，并且将所有的从节点连接到新的主节点上。
哨兵是Redis高可用的解决方案，它是一个管理多个Redis实例的服务工具，可以实现对Redis实例的监控、通知、自动故障转移。
-->

* **监控(Monitoring)：** 哨兵会不断地检查主节点和从节点是否运作正常。  
* **自动故障转移(Automatic failover)：** 当主节点不能正常工作时，哨兵会开始自动故障转移操作，它会将失效主节点的其中一个从节点升级为新的主节点，并让其他从节点改为复制新的主节点。  
* 配置提供者(Configuration provider)：客户端在初始化时，通过连接哨兵来获得当前Redis服务的主节点地址。  
* 通知(Notification)：哨兵可以将故障转移的结果发送给客户端。  

&emsp; <font color="clime">监控和自动故障转移使得Sentinel能够完成主节点故障发现和自动转移，配置提供者和通知则是实现通知客户端主节点变更的关键。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-89.png)  

## 1.1. 架构  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-28.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-29.png)  
&emsp; <font color = "red">Redis哨兵架构中主要包括两个部分：Redis Sentinel集群和Redis数据集群。</font>  

* 哨兵节点：哨兵系统由若干个哨兵节点组成。其实哨兵节点是一个特殊的 Redis 节点，<font color="red">哨兵节点不存储数据的和仅支持部分命令。配节点数量要满足2n+1(n>=1)的奇数个。</font>  
* 数据节点：由主节点和从节点组成的数据节点。  

## 1.2. 哨兵原理  
### 1.2.1. 心跳检查  
&emsp; **<font color = "red">Sentinel通过三个定时任务来完成对各个节点的发现和监控，这是保证Redis高可用的重要机制。</font>**  

#### 1.2.1.1. 定时任务一
&emsp; 每隔10秒，每个Sentinel节点会向主节点和从节点发送info命令获取最新的拓扑结构，如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-107.png)  
<center>Sentinel节点定时执行info命令</center>  

&emsp; 下图是info 命令的响应。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-46.png)
&emsp; Sentinel节点通过对上述结果进行解析就可以找到相应的从节点。  
&emsp; 这个定时任务的作用具体可以表现在三个方面：  

* 通过向主节点执行info命令，获取从节点的信息，这也是为什么 Sentinel节点不需要显式配置监控从节点。  
* 当有新的从节点加入时都可以立刻感知出来。   
* 节点不可达或者故障转移后，可以通过info命令实时更新节点拓扑信息。

#### 1.2.1.2. 定时任务二  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-108.png)  
<center>Sentinel节点发布和订阅__sentinel__hello频道</center>  
&emsp; 每隔2秒，每个Sentinel节点会向Redis数据节点的__sentinel__：hello 频道上发送该Sentinel节点对于主节点的判断以及当前Sentinel节点的信息，同时每个Sentinel节点也会订阅该频道，来了解其他 Sentinel节点以及它们对主节点的判断，所以这个定时任务可以完成以下两个工作：  

* 发现新的Sentinel节点。  
&emsp; 通过订阅主节点的__sentinel__：hello了解其他的Sentinel节点信息，如果是新加入的Sentinel节点，将该Sentinel节点信息保 存起来，并与该Sentinel节点创建连接
* Sentinel节点之间交换主节点的状态，作为后面客观下线以及领导者选举的依据。  


&emsp; 发送的消息内容格式为：  

    <哨兵地址>,<哨兵端口>,<哨兵的运行ID>,<哨兵的配置版本>,
    <主数据库的名称>,<主数据库的地址>,<主数据库的端口>,<主数据库的配置版本>

#### 1.2.1.3. 定时任务三  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-109.png)  
<center>Sentinel节点向其余节点发送ping命令</center>  
&emsp; 每隔1秒，每个Sentinel节点会向主节点、从节点、其余Sentinel节点 发送一条ping命令 **<font color = "clime">做一次心跳检测</font>** ，来确认这些节点当前是否可达。如上图所示。通过上面的定时任务，Sentinel节点对主节点、从节点、其余 Sentinel节点都建立起连接，实现了对每个节点的监控，这个定时任务是节点失败判定的重要依据。  

### 1.2.2. 主观下线、客观下线  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-88.png)  

* 主观下线  
&emsp; 在第三个定时任务(Sentinel通过三个定时任务来完成对各个节点的发现和监控)中，每隔一秒Sentinel节点会向每个Redis数据节点发送PING命令，若超过down-after-milliseconds设定的时间没有收到响应，<font color = "clime">则会对该节点做失败判定，这种行为叫做主观下线。因为该行为是当前节点的一家之言，所以会存在误判的可能。</font>  
* 客观下线  
&emsp; <font color = "red">当Sentinel节点判定一个主节点为主观下线后，则会通过sentinelis-master-down-by-addr命令询问其他Sentinel节点对该主节点的状态，当有超过<quorunm\>个Sentinel节点认为主节点存在问题，这时该 Sentinel节点会对主节点做客观下线的决定。</font>  
&emsp; 这里有一点需要注意的是，<font color = "clime">客观下线是针对主机节点，如果主观下线的是从节点或者其他Sentinel节点，则不会进行后面的客观下线和故障转移了。</font>  

### 1.2.3. 故障转移(主节点选举)  
&emsp; 当某个Sentinel节点通过选举成为了领导者，则它要承担起故障转移的工作，其具体步骤如下：  
1. <font color = "red">在从节点列表中选择一个节点作为新的主节点，选择的策略如下：</font> 
    * 过滤掉不健康的节点(主观下线、断线)，5秒内没有回复过Sentinel节点ping响应、与主节点失联超过down-after-milliseconds*10秒  
    * 选择优先级最高级别的节点，如果不存在则继续  
    * 选择复制偏移量最大的节点(数据最完整)，存在则返回，不存在则继续  
    * 选择 runid 最小的节点  
2. <font color = "red">在新的主节点上执行slaveofnoone，让其变成主节点。</font>  
3. <font color = "red">向剩余的从节点发送命令，让它们成为新主节点的从节点。</font>  

<!-- 
 3.4. Sentinel(哨兵)进程的工作方式：  
1. 每个Sentinel(哨兵)进程以每秒钟一次的频率向整个集群中的Master主服务器，Slave从服务器以及其他Sentinel(哨兵)进程发送一个 PING 命令。  
2. 如果一个实例(instance)距离最后一次有效回复 PING 命令的时间超过 down-after-milliseconds 选项所指定的值， 则这个实例会被 Sentinel(哨兵)进程标记为主观下线(SDOWN)。  
3. 如果一个Master主服务器被标记为主观下线(SDOWN)，则正在监视这个Master主服务器的所有 Sentinel(哨兵)进程要以每秒一次的频率确认Master主服务器的确进入了主观下线状态。  
4. 当有足够数量的 Sentinel(哨兵)进程(大于等于配置文件指定的值)在指定的时间范围内确认Master主服务器进入了主观下线状态(SDOWN)， 则Master主服务器会被标记为客观下线(ODOWN)。  
5. 在一般情况下， 每个 Sentinel(哨兵)进程会以每 10 秒一次的频率向集群中的所有Master主服务器、Slave从服务器发送 INFO 命令。
6. 当Master主服务器被 Sentinel(哨兵)进程标记为客观下线(ODOWN)时，Sentinel(哨兵)进程向下线的 Master主服务器的所有 Slave从服务器发送 INFO 命令的频率会从 10 秒一次改为每秒一次。  
7. 若没有足够数量的 Sentinel(哨兵)进程同意 Master主服务器下线， Master主服务器的客观下线状态就会被移除。若 Master主服务器重新向 Sentinel(哨兵)进程发送 PING 命令返回有效回复，Master主服务器的主观下线状态就会被移除。  
-->

### 1.2.4. Sentinel选举  
&emsp; 假如一个 Sentinel 节点完成了主节点的客观下线，那么是不是就可以立刻进行故障转移呢？显然不是，因为 **<font color = "red">Redis的故障转移工作只需要一个Sentinel节点来完成，所以会有一个选举的过程，选举出来一个领导者来完成故障转移工作。Redis节点采用[Raft算法]()来完成领导者写选举。</font>**    

&emsp; Sentinel 选举的主要流程：
1. 每一个做主观下线的Sentinel节点都有成为领导者的可能，它们会向其他Sentinel节点发送sentinelis-master-down-by-addr，要求将它设置为领导者。    
2. 收到命令的Sentinel节点如果没有同意其他Sentinel节点发送的命令，则会同意该请求，否则拒绝。     
3. 如果该Sentinel节点发现自己得到的票数已经超过半数且超过<quorum\>，那么它将成为领导者。     
4. 如果该过程有多个Sentinel成为领导者，那么将等待一段时间重新进行选择，直到有且只有一个Sentinel节点成为领导者为止。    

&emsp; 假如有A、B、C、D四个节点构成Sentinel集群。如果A率先完成客观下线，则A会向B、C、D发出成为领导者的申请，由于B、C、D没有同意过其他Sentinel节点，所以会将投票给A，A得到三票。B 则向A、C、D发起申请，由于C、D已经同意了A，所以会拒绝，但是它会得到A的同意，所以B得到一票，同理C、D得到零票，如下图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-47.png)  
&emsp; 所以A会成为领导者，进行故障转移工作。一般来说，哨兵选择的过程很快，谁先完成客观下线，一般就能成为领导者。  

## 1.3. 部署及故障转移演示    
......
