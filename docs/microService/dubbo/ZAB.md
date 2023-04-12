
<!-- TOC -->

- [1. 服务端的ZAB协议](#1-服务端的zab协议)
    - [1.1. ZAB协议概述](#11-zab协议概述)
    - [1.2. ~~ZAB协议中的ZXid~~](#12-zab协议中的zxid)
    - [1.3. ZAB协议](#13-zab协议)
        - [1.3.1. 选举流程，恢复模式](#131-选举流程恢复模式)
            - [1.3.1.1. 选举流程中几个重要参数](#1311-选举流程中几个重要参数)
            - [1.3.1.2. 服务器启动时的leader选举](#1312-服务器启动时的leader选举)
            - [1.3.1.3. 运行过程中的leader选举](#1313-运行过程中的leader选举)
        - [1.3.2. 服务端主从节点的数据同步，广播模式-数据读写流程(读写分离)](#132-服务端主从节点的数据同步广播模式-数据读写流程读写分离)
    - [1.4. Zookeeper的CP模型(保证读取数据顺序一致性)](#14-zookeeper的cp模型保证读取数据顺序一致性)
    - [1.5. ~~脑裂~~](#15-脑裂)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. ZK服务端通过`ZAB协议`保证`数据顺序一致性`。  
2. ZAB协议：
    1. Zookeeper集群角色：  
        * 领导者Leader：同一时间，集群只允许有一个Leader，提供对客户端的读写功能，负责将数据同步至各个节点；  
        * 跟随者Follower：提供对客户端读功能，写请求则转发给Leader处理，当Leader崩溃失联之后参与Leader选举；  
        * 观察者Observer：与Follower不同的是不参与Leader选举。  
    2. **<font color = "clime">崩溃恢复</font>**  
        * 服务器启动时的leader选举：每个server发出投票，投票信息包含(myid, ZXID,epoch)；接受投票；处理投票(epoch>ZXID>myid)；统计投票；改变服务器状态。</font>  
        * 运行过程中的leader选举：变更状态 ---> 发出投票 ---> 处理投票 ---> 统计投票 ---> 改变服务器的状态。
    3. **<font color = "clime">`消息广播（数据读写流程，读写流程）：`</font>**  
        &emsp; 在zookeeper中，客户端会随机连接到zookeeper集群中的一个节点。    
        * 如果是读请求，就直接从当前节点中读取数据。  
        * 如果是写请求，那么请求会被转发给 leader 提交事务，然后leader会广播事务，只要有超过半数节点写入成功，那么写请求就会被提交。   
        &emsp; ⚠️注：leader向follower写数据详细流程：类2pc(两阶段提交)。  
3.  数据一致性  
    &emsp; **<font color = "red">Zookeeper保证的是CP，即一致性(Consistency)和分区容错性(Partition-Tolerance)，而牺牲了部分可用性(Available)。</font>**  
    * 为什么不满足AP模型？<font color = "red">zookeeper在选举leader时，会停止服务，直到选举成功之后才会再次对外提供服务。</font>
    * Zookeeper的CP模型：非强一致性， **<font color = "clime">而是单调一致性/顺序一致性。</font>**  
        1. <font color = "clime">假设有2n+1个server，在同步流程中，leader向follower同步数据，当同步完成的follower数量大于n+1时同步流程结束，系统可接受client的连接请求。</font><font color = "red">如果client连接的并非同步完成的follower，那么得到的并非最新数据，但可以保证单调性。</font> 未同步数据的情况，Zookeeper提供了同步机制（可选型），类似回调。   
        2. follower接收写请求后，转发给leader处理；leader完成两阶段提交的机制。向所有server发起提案，当提案获得超过半数(n+1)的server认同后，将对整个集群进行同步，超过半数(n+1)的server同步完成后，该写请求完成。如果client连接的并非同步完成follower，那么得到的并非最新数据，但可以保证单调性。  


# 1. 服务端的ZAB协议
<!-- 
https://www.cnblogs.com/zz-ksw/p/12786067.html
-->

## 1.1. ZAB协议概述  
&emsp; Zab 协议的具体实现可以分为以下两部分：  

* 消息广播阶段：Leader节点接受事务提交，并且将新的Proposal请求广播给Follower节点，收集各个节点的反馈，决定是否进行Commit。  
* 崩溃恢复阶段：如果在同步过程中出现 Leader 节点宕机，会进入崩溃恢复阶段，重新进行 Leader 选举，崩溃恢复阶段还包含数据同步操作，同步集群中最新的数据，保持集群的数据一致性。   

&emsp; 整个ZooKeeper集群的一致性保证就是在上面两个状态之前切换，当 Leader 服务正常时，就是正常的消息广播模式；当 Leader 不可用时，则进入崩溃恢复模式，崩溃恢复阶段会进行数据同步，完成以后，重新进入消息广播阶段。  

## 1.2. ~~ZAB协议中的ZXid~~  
&emsp; 如何保证事务的顺序一致性的？  
&emsp; zookeeper采用了递增的事务Id来标识，所有的proposal（提议）都在被提出的时候加上了zxid。zxid实际上是一个64位的数字，高32位是epoch（时期; 纪元; 世; 新时代）用来标识leader是否发生改变，如果有新的leader产生出来，epoch会自增；低32位用来递增计数。当新产生proposal的时候，会依据数据库的两阶段过程，首先会向其他的server发出事务执行请求，如果超过半数的机器都能执行并且能够成功，那么就会开始执行。  

----

&emsp; Zxid 是 Zab 协议的一个事务编号，Zxid 是一个 64 位的数字，其中低 32 位是一个简单的单调递增计数器，针对客户端每一个事务请求，计数器加 1；而高 32 位则代表 Leader 周期年代的编号。  
&emsp; 这里 Leader 周期的英文是 epoch，可以理解为当前集群所处的年代或者周期  
![image](http://182.92.69.8:8081/img/microService/zookeeper/zk-1.png)  
&emsp; 每当有一个新的 Leader 选举出现时，就会从这个 Leader 服务器上取出其本地日志中最大事务的 Zxid，并从中读取 epoch 值，然后加 1，以此作为新的周期 ID。总结一下，高 32 位代表了每代 Leader 的唯一性，低 32 位则代表了每代 Leader 中事务的唯一性。  


## 1.3. ZAB协议
&emsp; ZAB(Zookeeper Atomic Broadcast)，崩溃可恢复的的原子消息广播协议。ZAB协议包括两种基本模式：崩溃恢复(选主)和消息广播(同步)。整个Zookeeper集群就是在这两个模式之间切换。  

&emsp; Zookeeper集群角色：  

* 领导者Leader：同一时间集群总只允许有一个Leader，提供对客户端的读写功能，负责将数据同步至各个节点；  
* 跟随者Follower：提供对客户端读功能，写请求则转发给Leader处理，当Leader崩溃失联之后参与Leader选举；  
* 观察者Observer：与Follower不同的是但不参与Leader选举。  

<!-- 
* 服务状态

    * LOOKING：当节点认为群集中没有Leader，服务器会进入LOOKING状态，目的是为了查找或者选举Leader；  
    * FOLLOWING：follower角色；  
    * LEADING：leader角色；  
    * OBSERVING：observer角色；  
-->

### 1.3.1. 选举流程，恢复模式  
&emsp; 当整个集群在启动时，或者当leader节点出现网络中断、崩溃等情况时，ZAB协议就会进入恢复模式并选举产生新的Leader，当leader服务器选举出来后，并且集群中有过半的机器和该leader节点完成数据同步后(同步指的是数据同步，用来保证集群中过半的机器能够和leader服务器的数据状态保持一致)，ZAB协议就会退出恢复模式。  

#### 1.3.1.1. 选举流程中几个重要参数 
&emsp; 服务器ID：即配置的myId。id越大，选举时权重越高。  
&emsp; <font color = "red">事务ID，zkid(Zookeeper Transaction Id)：服务器在运行时产生的数据id，即zkid，这里指本地最新snapshot的id。id越大说明数据越新，选举时权重越高。</font>  
&emsp; 选举轮数：Epoch，即逻辑时钟。随着选举的轮数++。  
&emsp; 选举状态：4种状态。LOOKING，竞选状态；FOLLOWING，随从状态，同步leader状态，参与投票；OBSERVING，观察状态，同步leader状态，不参与投票；LEADING，领导者状态。  
<!-- 
ZooKeeper状态的每次变化都接收一个ZXID(ZooKeeper事务id)形式的标记。ZXID是一个64位的数字，由Leader统一分配，全局唯一，不断递增。
ZXID展示了所有的ZooKeeper的变更顺序。每次变更会有一个唯一的zxid，如果zxid1小于zxid2说明zxid1在zxid2之前发生。
-->

#### 1.3.1.2. 服务器启动时的leader选举 
&emsp; 每个节点启动的时候状态都是LOOKING，处于观望状态，接下来就开始进行选主流程。  
&emsp; 若进行Leader选举，则至少需要两台机器，这里选取 3 台机器组成的服务器集群为例。在集群初始化阶段，当有一台服务器 Server1 启动时，其单独无法进行和完成Leader选举，当第二台服务器 Server2 启动时，此时两台机器可以相互通信，每台机器都试图找到Leader，于是进入Leader选举过程。选举过程如下：  
1. 每个 Server 发出一个投票。由于是初始情况， Server1 和 Server2 都会将自己作为 Leader 服务器来进行投票，每次投票会包含所推举的服务器的 myid 和 ZXID、 epoch，使用(myid, ZXID,epoch)来表示，此时 Server1 的投票为(1, 0)， Server2 的投票为(2, 0)，然后各自将这个投票发给集群中其他机器。  
2. 接受来自各个服务器的投票。集群的每个服务器收到投票后，首先判断该投票的有效性，如检查是否是本轮投票( epoch)、是否来自LOOKING 状态的服务器。  
3. 处理投票。针对每一个投票，服务器都需要将别人的投票和自己的投票进行PK，PK规则如下：  
&emsp; i. 优先比较 epoch  
&emsp; ii. 其次检查 ZXID。 ZXID 比较大的服务器优先作为 Leader  
&emsp; iii. 如果 ZXID 相同，那么就比较 myid。 myid 较大的服务器作为Leader 服务器。  
&emsp; 对于 Server1 而言，它的投票是(1, 0)，接收 Server2 的投票为(2, 0)，首先会比较两者的 ZXID，均为 0，再比较 myid，此时 Server2 的myid 最大，于是更新自己的投票为(2, 0)，然后重新投票，对于Server2 而言，其无须更新自己的投票，只是再次向集群中所有机器发出上一次投票信息即可。  
4. 统计投票。每次投票后，服务器都会统计投票信息，判断是否已经有过半机器接受到相同的投票信息，对于 Server1、 Server2 而言，都统计出集群中已经有两台机器接受了(2, 0)的投票信息，此时便认为已经选出了 Leader。  
5. 改变服务器状态。一旦确定了 Leader，每个服务器就会更新自己的状态，如果是 Follower，那么就变更为 FOLLOWING，如果是 Leader，就变更为 LEADING。  

&emsp; **<font color = "red">一句话概述：每个server发出投票，投票信息包含(myid, ZXID,epoch)；接受投票；处理投票(epoch>ZXID>myid)；统计投票；改变服务器状态。</font>**

#### 1.3.1.3. 运行过程中的leader选举  
&emsp; 当集群中的 leader 服务器出现宕机或者不可用的情况时，那么整个集群将无法对外提供服务，而是进入新一轮的 Leader 选举，服务器运行期间的 Leader 选举和启动时期的 Leader 选举基本过程是一致的。  
![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-3.png)  
1. 变更状态。 Leader挂后，余下的非Observer服务器都会将自己的服务器状态变更为LOOKING，然后开始进入Leader选举过程。  
2. 每个 Server 会发出一个投票。在运行期间，每个服务器上的 ZXID 可能不同，此时假定 Server1 的 ZXID 为 123，Server3 的 ZXID 为 122；在第一轮投票中，Server1 和 Server3 都会投自己，产生投票(1, 123)，(3, 122)，然后各自将投票发送给集群中所有机器。接收来自各个服务器的投票。与启动时过程相同。  
3. 处理投票。与启动时过程相同，此时， Server1 将会成为 Leader。  
4. 统计投票。与启动时过程相同。  
5. 改变服务器的状态。与启动时过程相同。  

### 1.3.2. 服务端主从节点的数据同步，广播模式-数据读写流程(读写分离) 

<!-- 

1.3.1.1. 读数据流程  
&emsp; 当Client向zookeeper发出读请求时，无论是Leader还是Follower，都直接返回查询结果。  
![image](http://182.92.69.8:8081/img/microService/zookeeper/zk-2.png)  
&emsp; 如果是对zk进行读取操作，读取到的数据可能是过期的旧数据，不是最新的数据。  

1.3.1.2. 写数据流程
&emsp; zookeeper写入操作分为两种情况，① 写入请求直接发送到leader节点，② 写入请求发送到Follower节点，这两种情况有略微的区别。  

&emsp; 写入请求直接发送到Leader节点时的操作步骤如下：  
1. Client向Leader发出写请求。
2. Leader将数据写入到本节点，并将数据发送到所有的Follower节点；
3. 等待Follower节点返回；
4. 当Leader接收到一半以上节点(包含自己)返回写成功的信息之后，返回写入成功消息给client;
![image](http://182.92.69.8:8081/img/microService/zookeeper/zk-3.png)  

&emsp; 写入请求发送到Follower节点的操作步骤如下：  
1. Client向Follower发出写请求
2. Follower节点将请求转发给Leader
3. Leader将数据写入到本节点，并将数据发送到所有的Follower节点
4. 等待Follower节点返回
5. 当Leader接收到一半以上节点(包含自己)返回写成功的信息之后，返回写入成功消息给原来的Follower
6. 原来的Follower返回写入成功消息给Client
![image](http://182.92.69.8:8081/img/microService/zookeeper/zk-4.png)  
-->

&emsp; 当集群中已经有过半的Follower节点完成了和Leader状态同步以后，那么整个集群就进入了消息广播模式。这个时候，在Leader节点正常工作时，启动一台新的服务器加入到集群，那这个服务器会直接进入数据恢复模式，和leader节点进行数据同步。同步完成后即可正常对外提供非事务请求的处理。  
&emsp; 注：leader节点可以处理事务请求和非事务请求，follower节点只能处理非事务请求，如果follower节点接收到非事务请求，会把这个请求转发给Leader服务器。  

&emsp; <font color = "clime">在zookeeper中，客户端会随机连接到zookeeper集群中的一个节点，如果是读请求，就直接从当前节点中读取数据，如果是写请求，那么请求会被转发给 leader 提交事务，</font>然后leader会广播事务，只要有超过半数节点写入成功，那么写请求就会被提交(类2PC事务)。  

&emsp; **消息广播流程：**  
1. leader接收到消息请求后，将消息赋予一个全局唯一的64位自增id，zxid，通过zxid的大小比较既可以实现因果有序这个特征。  
2. <font color = "red">leader 为每个follower准备了一个 FIFO 队列(通过 TCP协议来实现，以实现了全局有序这一个特点)将带有zxid的消息作为一个提案(proposal)分发给所有的 follower。</font>  
3. 当follower接收到proposal，先把proposal写到磁盘，写入成功以后再向leader回复一个ack。  
4. 当leader接收到合法数量(超过半数节点)的ACK后，leader就会向这些follower发送commit命令同时会在本地执行该消息。  
5. 当follower收到消息的commit命令以后，会提交该消息。  
![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-4.png)  

&emsp; 注：和完整的2pc事务不一样的地方在于，zab协议不能终止事务，follower节点要么ACK给leader，要么抛弃leader，只需要保证过半数的节点响应这个消息并提交了即可，虽然在某一个时刻follower节点和leader节点的状态会不一致，但是也是这个特性提升了集群的整体性能。当然这种数据不一致的问题，zab协议提供了一种恢复模式来进行数据恢复。  
&emsp; 这里需要注意的是leader的投票过程，不需要Observer的ack，也就是Observer不需要参与投票过程，但是Observer必须要同步Leader的数据从而在处理请求的时候保证数据的一致性。  

## 1.4. Zookeeper的CP模型(保证读取数据顺序一致性)  
<!-- 
Zookeeper写入是强一致性,读取是顺序一致性。
https://blog.csdn.net/weixin_47727457/article/details/106439452
-->

&emsp; **<font color = "red">Zookeeper保证的是CP，即一致性(Consistency)和分区容错性(Partition-Tolerance)，而牺牲了部分可用性(Available)。</font>**  

1. 为什么不满足AP模型？  

        高可用方案：  
            两台机器：  
                    主从复制 主备复制 主主复制 主从切换 主备切换  
            两台以上机器：
                集中式集群
                    一主多备一主多从
                分散式集群
                    数据分片到多节点

    &emsp; zookeeper采用的是集中式集群中的一主多从方案，为的是提升性能和可用性。zookeeper实现了高可用，为什么不是AP系统？  

    &emsp; <font color = "red">zookeeper在选举leader时，会停止服务，直到选举成功之后才会再次对外提供服务，</font>这个时候就说明了服务不可用，但是在选举成功之后，因为一主多从的结构，zookeeper在这时还是一个高可用注册中心，只是在优先保证一致性的前提下，zookeeper才会顾及到可用性

2. Zookeeper的CP模型：  
&emsp; 很多文章和博客里提到，zookeeper是一种提供强一致性的服务，在分区容错性和可用性上做了一定折中，这和CAP理论是吻合的。但实际上<font color = "clime">zookeeper提供的只是单调一致性/顺序一致性。</font>  
    1. <font color = "clime">假设有2n+1个server，在同步流程中，leader向follower同步数据，当同步完成的follower数量大于n+1时同步流程结束，系统可接受client的连接请求。</font><font color = "red">如果client连接的并非同步完成的follower，那么得到的并非最新数据，但可以保证单调性。</font>  
    2. follower接收写请求后，转发给leader处理；leader完成两阶段提交的机制。向所有server发起提案，当提案获得超过半数(n+1)的server认同后，将对整个集群进行同步，超过半数(n+1)的server同步完成后，该写请求完成。如果client连接的并非同步完成follower，那么得到的并非最新数据，但可以保证单调性。  

## 1.5. ~~脑裂~~  
&emsp; 过半机制，要求集群内的节点数量为2N+1。  
