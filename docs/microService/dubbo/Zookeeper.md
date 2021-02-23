
<!-- TOC -->

- [1. Zookeeper](#1-zookeeper)
    - [1.1. Zookeeper是什么](#11-zookeeper是什么)
    - [1.2. ZooKeeper分层命名空间(逻辑存储结构)](#12-zookeeper分层命名空间逻辑存储结构)
    - [1.3. ★★★Zookeeper的运行原理](#13-★★★zookeeper的运行原理)
        - [1.3.1. C/S之间的Watcher机制](#131-cs之间的watcher机制)
        - [1.3.2. ※※※服务端通过ZAB协议，保证主从节点数据一致性](#132-※※※服务端通过zab协议保证主从节点数据一致性)
            - [1.3.2.1. ZAB协议概述](#1321-zab协议概述)
            - [1.3.2.2. ZAB协议中的ZXid](#1322-zab协议中的zxid)
            - [1.3.2.3. ZAB协议](#1323-zab协议)
                - [1.3.2.3.1. 选举流程，恢复模式](#13231-选举流程恢复模式)
                    - [1.3.2.3.1.1. 选举流程中几个重要参数](#132311-选举流程中几个重要参数)
                    - [1.3.2.3.1.2. 服务器启动时的leader选举](#132312-服务器启动时的leader选举)
                    - [1.3.2.3.1.3. 运行过程中的leader选举](#132313-运行过程中的leader选举)
                - [1.3.2.3.2. 数据同步，广播模式](#13232-数据同步广播模式)
            - [1.3.2.4. Zookeeper的CP模型(保证数据顺序一致性)](#1324-zookeeper的cp模型保证数据顺序一致性)
            - [1.3.2.5. 脑裂](#1325-脑裂)
    - [1.4. Zookeeper应用场景](#14-zookeeper应用场景)
        - [1.4.1. 集群管理，HA高可用性](#141-集群管理ha高可用性)
        - [1.4.2. 元数据/配置信息管理](#142-元数据配置信息管理)
        - [1.4.3. 分布式锁](#143-分布式锁)
        - [1.4.4. 分布式协调](#144-分布式协调)
        - [1.4.5. 统一命名服务](#145-统一命名服务)
        - [1.4.6. 队列管理](#146-队列管理)

<!-- /TOC -->

<!-- 
Zookeeper工作原理(详细)
https://blog.csdn.net/yanxilou/article/details/84400562

Zookeeper ZAB协议中的zxid
https://blog.csdn.net/xu505928168/article/details/105383859


Zookeeper 怎么保证分布式事务的最终一致性？ 
https://mp.weixin.qq.com/s/n0UKU7BxPT1r4OCPPKHGaA

ZooKeeper的十二连问，你顶得了嘛？ 
https://mp.weixin.qq.com/s/dp8jFlTsxTvGuhSAnct1jA
讲解 Zookeeper 的五个核心知识点 
https://mp.weixin.qq.com/s/W6QgmFTpXQ8EL-dVvLWsyg
ZooKeeper的顺序一致性[1] 
https://time.geekbang.org/column/article/239261
ZooKeeper = 文件系统 + 监听通知机制。
-->

&emsp; **<font color = "red">总结：</font>**  

&emsp; **<font color = "blue">Zookeeper是一个分布式协调服务的开源框架。主要用来解决分布式集群中应用系统的一致性问题。</font>**  
1. C/S之间的Watcher机制。
2. ZK服务端通过ZAB协议保证数据顺序一致性。  
    1. ZAB协议：消息广播、崩溃恢复
        * 服务器启动时的leader选举：每个server发出投票，投票信息包含(myid, ZXID,epoch)；接受投票；处理投票(epoch>ZXID>myid)；统计投票；改变服务器状态。</font>**  
        * 运行过程中的leader选举：变更状态 ---> 发出投票 ---> 处理投票 ---> 统计投票 ---> 改变服务器的状态
    2.  数据一致性
        * 为什么不满足AP模型？<font color = "red">zookeeper在选举leader时，会停止服务，直到选举成功之后才会再次对外提供服务。</font>。
        * Zookeeper的CP模型：非强一致性、而是单调一致性/顺序一致性。  


# 1. Zookeeper
&emsp; Zookeeper官网文档：https://zookeeper.apache.org/doc/current
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-2.png)  

## 1.1. Zookeeper是什么  
&emsp; **<font color = "blue">Zookeeper是一个分布式协调服务的开源框架。主要用来解决分布式集群中应用系统的一致性问题，</font>** 例如怎样避免同时操作同一数据造成脏读的问题。  
&emsp; **Zookeeper集群特性：**  

* 全局数据一致：每个server保存一份相同的数据副本，client无论连接到哪个server，展示的数据都是一致的，这是最重要的特征。  
* 可靠性：如果消息被其中一台服务器接受，那么将被所有的服务器接受。  
* **<font color = "blue">顺序性：</font>** 包括全局有序和偏序两种。全局有序是指如果在一台服务器上消息a在消息b前发布，则在所有Server上消息a都将在消息b前被发布；偏序是指如果一个消息b在消息a后被同一个发送者发布，a必将排在b前面。  
* 数据更新原子性：一次数据更新要么成功(半数以上节点成功)，要么失败，不存在中间状态。  
* 实时性：Zookeeper保证客户端将在一个时间间隔范围内获得服务器的更新信息，或者服务器失效的信息。  

## 1.2. ZooKeeper分层命名空间(逻辑存储结构)  
&emsp; Zookeeper提供一个多层级的节点命名空间(节点称为znode)，这些节点都可以设置关联的数据。Zookeeper为了保证高吞吐和低延迟，在内存中维护了这个树状的目录结构，这种特性使得Zookeeper不能用于存放大量的数据，每个节点的存放数据上限为1M。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-1.png)  
&emsp; znode的类型在创建时确定，并且之后不能再修改。  

* 临时非顺序节点：客户端与zookeeper断开连接后，该节点被删除。  
* 临时顺序节点：客户端与zookeeper断开连接后，该节点被删除，节点名称会追加一个单调递增的数字。  
* 持久非顺序节点：客户端与zookeeper断开连接后，该节点依旧存在，直到有删除操作主动清除该节点。  
* 持久顺序节点：客户端与zookeeper断开连接后，该节点依旧存在，直到有删除操作主动清除该节点，节点名称会追加一个单调递增的数字。   

&emsp; **Zookeeper节点的持久性：**  

* 临时节点并不会在客户端断开的瞬间就被删除，而是等到会话结束时，zookeeper会将该短暂znode删除，短暂znode不可以有子节点。  
* 持久节点不依赖于客户端会话，只有当客户端明确要删除该持久znode时才会被删除。  

&emsp; **Zookeeper节点的顺序性：**  

* 创建带自增序列znode时设置顺序标识，znode名称后会附加一个值。  
* 顺序号是一个单调递增的计数器，由父节点维护。  
* 在分布式系统中，顺序号可以被用于为所有的事件进行全局排序，这样客户端可以通过顺序号推断事件的顺序。  

## 1.3. ★★★Zookeeper的运行原理  
~~架构图~~

### 1.3.1. C/S之间的Watcher机制  
&emsp; 在ZooKeeper中，引入Watcher机制来实现分布式数据的发布/订阅功能。Zookeeper客户端向服务端的某个Znode注册一个Watcher监听，当服务端的一些指定事件触发了这个Watcher，服务端会向指定客户端发送一个事件通知来实现分布式的通知功能，然后客户端根据Watcher通知状态和事件类型做出业务上的改变。  
&emsp; 触发watch事件种类很多，如：节点创建，节点删除，节点改变，子节点改变等。  

&emsp; <font color = "red">Watcher机制运行流程：客户端注册 Watcher、服务器处理Watcher和客户端回调Watcher。</font>  

&emsp; **watch的重要特性：**  

* 一次性触发：  
&emsp; Watcher通知是一次性的，即一旦触发一次通知后，该 Watcher 就失效了，因此客户端需要反复注册Watcher。但是在获取watch事件和设置新的watch事件之间有延迟。延迟为毫秒级别，理论上会出现不能观察到节点的每一次变化。  
&emsp; 不支持用持久Watcher的原因：如果Watcher的注册是持久的，那么必然导致服务端的每次数据更新都会通知到客户端。这在数据变更非常频繁且监听客户端特别多的场景下，ZooKeeper无法保证性能。  
* 有序性：  
&emsp; 客户端先得到watch通知才可查看节点变化结果。  

### 1.3.2. ※※※服务端通过ZAB协议，保证主从节点数据一致性
<!-- 

https://www.cnblogs.com/zz-ksw/p/12786067.html
-->

#### 1.3.2.1. ZAB协议概述  
&emsp; Zab 协议的具体实现可以分为以下两部分：  

* 消息广播阶段：Leader节点接受事务提交，并且将新的Proposal请求广播给Follower节点，收集各个节点的反馈，决定是否进行Commit。  
* 崩溃恢复阶段：如果在同步过程中出现 Leader 节点宕机，会进入崩溃恢复阶段，重新进行 Leader 选举，崩溃恢复阶段还包含数据同步操作，同步集群中最新的数据，保持集群的数据一致性。   

&emsp; 整个ZooKeeper集群的一致性保证就是在上面两个状态之前切换，当 Leader 服务正常时，就是正常的消息广播模式；当 Leader 不可用时，则进入崩溃恢复模式，崩溃恢复阶段会进行数据同步，完成以后，重新进入消息广播阶段。  

#### 1.3.2.2. ZAB协议中的ZXid  


#### 1.3.2.3. ZAB协议
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

##### 1.3.2.3.1. 选举流程，恢复模式  
&emsp; 当整个集群在启动时，或者当leader节点出现网络中断、崩溃等情况时，ZAB协议就会进入恢复模式并选举产生新的 Leader，当leader服务器选举出来后，并且集群中有过半的机器和该 leader 节点完成数据同步后(同步指的是数据同步，用来保证集群中过半的机器能够和leader服务器的数据状态保持一致)，ZAB协议就会退出恢复模式。  

###### 1.3.2.3.1.1. 选举流程中几个重要参数 
&emsp; 服务器ID：即配置的myId。id越大，选举时权重越高。  
&emsp; <font color = "red">事务ID，zkid(Zookeeper Transaction Id)：服务器在运行时产生的数据id，即zkid，这里指本地最新snapshot的id。id越大说明数据越新，选举时权重越高。</font>  
&emsp; 选举轮数：Epoch，即逻辑时钟。随着选举的轮数++。  
&emsp; 选举状态：4种状态。LOOKING，竞选状态；FOLLOWING，随从状态，同步leader状态，参与投票；OBSERVING，观察状态，同步leader状态，不参与投票；LEADING，领导者状态。  
<!-- 
ZooKeeper状态的每次变化都接收一个ZXID(ZooKeeper事务id)形式的标记。ZXID是一个64位的数字，由Leader统一分配，全局唯一，不断递增。
ZXID展示了所有的ZooKeeper的变更顺序。每次变更会有一个唯一的zxid，如果zxid1小于zxid2说明zxid1在zxid2之前发生。
-->

###### 1.3.2.3.1.2. 服务器启动时的leader选举 
&emsp; 每个节点启动的时候状态都是LOOKING，处于观望状态，接下来就开始进行选主流程。  
&emsp; 若进行 Leader 选举，则至少需要两台机器，这里选取 3 台机器组成的服务器集群为例。在集群初始化阶段，当有一台服务器 Server1 启动时，其单独无法进行和完成 Leader 选举，当第二台服务器 Server2 启动时，此时两台机器可以相互通信，每台机器都试图找到 Leader，于是进入 Leader选举过程。选举过程如下：  
1. 每个 Server 发出一个投票。由于是初始情况， Server1 和 Server2 都会将自己作为 Leader 服务器来进行投票，每次投票会包含所推举的服务器的 myid 和 ZXID、 epoch，使用(myid, ZXID,epoch)来表示，此时 Server1 的投票为(1, 0)， Server2 的投票为(2, 0)，然后各自将这个投票发给集群中其他机器。  
2. 接受来自各个服务器的投票。集群的每个服务器收到投票后，首先判断该投票的有效性，如检查是否是本轮投票( epoch)、是否来自LOOKING 状态的服务器。  
3. 处理投票。针对每一个投票，服务器都需要将别人的投票和自己的投票进行 PK， PK 规则如下：  
&emsp; i. 优先比较 epoch  
&emsp; ii. 其次检查 ZXID。 ZXID 比较大的服务器优先作为 Leader  
&emsp; iii. 如果 ZXID 相同，那么就比较 myid。 myid 较大的服务器作为Leader 服务器。  
&emsp; 对于 Server1 而言，它的投票是(1, 0)，接收 Server2 的投票为(2, 0)，首先会比较两者的 ZXID，均为 0，再比较 myid，此时 Server2 的myid 最大，于是更新自己的投票为(2, 0)，然后重新投票，对于Server2 而言，其无须更新自己的投票，只是再次向集群中所有机器发出上一次投票信息即可。  
4. 统计投票。每次投票后，服务器都会统计投票信息，判断是否已经有过半机器接受到相同的投票信息，对于 Server1、 Server2 而言，都统计出集群中已经有两台机器接受了(2, 0)的投票信息，此时便认为已经选出了 Leader。  
5. 改变服务器状态。一旦确定了 Leader，每个服务器就会更新自己的状态，如果是 Follower，那么就变更为 FOLLOWING，如果是 Leader，就变更为 LEADING。  

&emsp; **<font color = "red">一句话概述：每个server发出投票，投票信息包含(myid, ZXID,epoch)；接受投票；处理投票(epoch>ZXID>myid)；统计投票；改变服务器状态。</font>**

###### 1.3.2.3.1.3. 运行过程中的leader选举  
&emsp; 当集群中的 leader 服务器出现宕机或者不可用的情况时，那么整个集群将无法对外提供服务，而是进入新一轮的 Leader 选举，服务器运行期间的 Leader 选举和启动时期的 Leader 选举基本过程是一致的。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-3.png)  
1. 变更状态。 Leader挂后，余下的非Observer服务器都会将自己的服务器状态变更为LOOKING，然后开始进入Leader选举过程。  
2. 每个 Server 会发出一个投票。在运行期间，每个服务器上的 ZXID 可能不同，此时假定 Server1 的 ZXID 为 123，Server3 的 ZXID 为 122；在第一轮投票中，Server1 和 Server3 都会投自己，产生投票(1, 123)，(3, 122)，然后各自将投票发送给集群中所有机器。接收来自各个服务器的投票。与启动时过程相同。  
3. 处理投票。与启动时过程相同，此时， Server1 将会成为 Leader。  
4. 统计投票。与启动时过程相同。  
5. 改变服务器的状态。与启动时过程相同。  

##### 1.3.2.3.2. 数据同步，广播模式  
&emsp; 当集群中已经有过半的 Follower 节点完成了和 Leader 状态同步以后，那么整个集群就进入了消息广播模式。这个时候，在 Leader 节点正常工作时，启动一台新的服务器加入到集群，那这个服务器会直接进入数据恢复模式，和leader 节点进行数据同步。同步完成后即可正常对外提供非事务请求的处理。  
&emsp; 注：leader节点可以处理事务请求和非事务请求， follower 节点只能处理非事务请求，如果 follower 节点接收到非事务请求，会把这个请求转发给 Leader 服务器。  

&emsp; <font color = "lime">在zookeeper中，客户端会随机连接到zookeeper集群中的一个节点，如果是读请求，就直接从当前节点中读取数据，如果是写请求，那么请求会被转发给 leader 提交事务，</font>然后leader会广播事务，只要有超过半数节点写入成功，那么写请求就会被提交(类2PC事务)。  

&emsp; **消息广播流程：**  
1. leader接收到消息请求后，将消息赋予一个全局唯一的64 位自增 id，zxid，通过 zxid 的大小比较既可以实现因果有序这个特征。  
2. <font color = "red">leader 为每个 follower 准备了一个 FIFO 队列(通过 TCP协议来实现，以实现了全局有序这一个特点)将带有 zxid的消息作为一个提案( proposal)分发给所有的 follower。</font>  
3. 当 follower 接收到 proposal，先把 proposal 写到磁盘，写入成功以后再向 leader 回复一个 ack。  
4. 当 leader 接收到合法数量(超过半数节点)的 ACK 后，leader 就会向这些 follower 发送 commit 命令，同时会在本地执行该消息。  
5. 当 follower 收到消息的 commit 命令以后，会提交该消息。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-4.png)  

&emsp; 注：和完整的2pc事务不一样的地方在于，zab协议不能终止事务，follower节点要么ACK给leader，要么抛弃leader，只需要保证过半数的节点响应这个消息并提交了即可，虽然在某一个时刻follower节点和leader节点的状态会不一致，但是也是这个特性提升了集群的整体性能。当然这种数据不一致的问题，zab协议提供了一种恢复模式来进行数据恢复。  
&emsp; 这里需要注意的是leader的投票过程，不需要Observer的ack，也就是Observer不需要参与投票过程，但是Observer必须要同步Leader的数据从而在处理请求的时候保证数据的一致性。  


#### 1.3.2.4. Zookeeper的CP模型(保证数据顺序一致性)  

1. 为什么不满足AP模型？  

        高可用方案：  
            两台机器：  
                    主从复制主备复制主主复制主从切换主备切换  
            两台以上机器：
                集中式集群
                    一主多备一主多从
                分散式集群
                    数据分片到多节点

    &emsp; zookeeper采用的是集中式集群中的一主多从方案，为的是提升性能和可用性。zookeeper实现了高可用，为什么不是AP系统？  

    &emsp; <font color = "red">zookeeper在选举leader时，会停止服务，直到选举成功之后才会再次对外提供服务，</font>这个时候就说明了服务不可用，但是在选举成功之后，因为一主多从的结构，zookeeper在这时还是一个高可用注册中心，只是在优先保证一致性的前提下，zookeeper才会顾及到可用性

2. Zookeeper的CP模型：  
&emsp; 很多文章和博客里提到，zookeeper是一种提供强一致性的服务，在分区容错性和可用性上做了一定折中，这和CAP理论是吻合的。但实际上<font color = "lime">zookeeper提供的只是单调一致性/顺序一致性。</font>  
    1. 假设有2n+1个server，在同步流程中，leader向follower同步数据，当同步完成的follower数量大于n+1时同步流程结束，系统可接受client的连接请求。<font color = "red">如果client连接的并非同步完成的follower，那么得到的并非最新数据，但可以保证单调性。</font>  
    2. follower接收写请求后，转发给leader处理；leader完成两阶段提交的机制。向所有server发起提案，当提案获得超过半数(n+1)的server认同后，将对整个集群进行同步，超过半数(n+1)的server同步完成后，该写请求完成。如果client连接的并非同步完成follower，那么得到的并非最新数据，但可以保证单调性。  

#### 1.3.2.5. 脑裂  
&emsp; 脑裂问题是集群部署必须考虑的一点，比如在Hadoop跟Spark集群中。而ZAB为解决脑裂问题，要求集群内的节点数量为2N+1。当网络分裂后，始终有一个集群的节点数量过半数，而另一个节点数量小于N+1, 因为选举Leader需要过半数的节点同意，所以可以得出如下结论：  
&emsp; 有了过半机制，对于一个Zookeeper集群，要么没有Leader，要没只有1个Leader，这样就避免了脑裂问题  

----

## 1.4. Zookeeper应用场景  
### 1.4.1. 集群管理，HA高可用性
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-29.png) 

&emsp; 所谓集群管理无在乎两点：是否有机器退出和加入、选举master。  
&emsp; 对于第一点，所有机器约定在父目录GroupMembers下创建临时目录节点，然后监听父目录节点的子节点变化消息。一旦有机器挂掉，该机器与zookeeper的连接断开，其所创建的临时目录节点被删除，所有其他机器都收到通知。新机器加入也是类似，所有机器收到通知。  
&emsp; 对于第二点，稍微改变一下，所有机器创建临时顺序编号目录节点，每次选取编号最小的机器作为master就好。  
&emsp; 集群启动、集群同步、集群FastPaxos  

&emsp; hadoop、hdfs、yarn等很多大数据系统，都选择基于zookeeper来开发HA高可用机制，就是一个重要进程一般会做主备两个，主进程挂了立马通过 zookeeper 感知到切换到备用进程。

### 1.4.2. 元数据/配置信息管理
&emsp; 配置的分布式管理，相同的配置需要应用到多台机器上。这种配置完全可以由zookeeper来管理，将配置信息保存在某个目录节点中，然后将所有的应用机器监控配置信息的状态，一旦发生变化，就可以从zookeeper中获取最新的配置信息，并应用到系统中。  
&emsp; 发布与订阅模型，即所谓的配置中心，顾名思义就是发布者将数据发布到ZK节点上，供订阅者动态获取数据，实现配置信息的集中式管理和动态更新。例如全局的配置信息，服务式服务框架的服务地址列表等就非常合适用。  


### 1.4.3. 分布式锁  
&emsp; 有了zookeeper的一致性文件系统，锁的问题变得容易。锁服务可以分为两类，一个是保持独占，另一个是控制时序。  
&emsp; 对于第一类，将zookeeper上的一个znode看作是一把锁，通过createznode的方式来实现。所有客户端都去创建/distribute_lock节点，最终成功创建的那个客户端也即拥有了这把锁。用完删除掉自己创建的distribute_lock节点就释放出锁。  
&emsp; 对于第二类，/distribute_lock已经预先存在，所有客户端在它下面创建临时顺序编号目录节点，和选master一样，编号最小的获得锁，用完删除，依次方便。  


### 1.4.4. 分布式协调
<!-- 
ZooKeeper的典型应用场景之分布式协调/通知
https://blog.csdn.net/en_joker/article/details/78799737
-->  
&emsp; 分布式协调/通知服务是分布式系统中不可缺少的一个环节，是将不同的分布式组件有机结合起来的关键所在。对于一个在多台机器上部署运行的应用而言，通常需要一个协调者(Coordinator)来控制整个系统的运行流程，例如分布式事务的处理、机器间的互相协调等。同时，引入这样一个协调者，便于将分布式协调的职责从应用中分离出来，从而可以大大减少系统之间的耦合性，而且能够显著提高系统的可扩展性。  
&emsp; <font color = "lime">ZooKeeper中特有的Watcher注册与异步通知机制，能够很好的实现分布式环境下不同机器，甚至是不同系统之间的协调与通知，从而实现对数据变更的实时处理。基于ZooKeeper实现分布式协调与通知功能，从而实现对数据变更的实时处理。</font><font color = "red">基于ZooKeeper实现分布式协调与通知功能，通常的做法是不同的客户端都对ZooKeeper上同一个数据节点进行Watcher注册，监听数据节点的变化(包括数据节点本身及其子节点)，如果数据节点发生变化，那么所有订阅的客户端都能够接收到相应的Watcher通知，并做出相应的处理。</font>  

&emsp; 这个其实是zookeeper很经典的一个用法，简单来说，就好比，A系统发送个请求到mq，然后B系统消息消费之后处理了。那A系统如何知道B系统的处理结果？用zookeeper就可以实现分布式系统之间的协调工作。A系统发送请求之后可以在zookeeper上对某个节点的值注册个监听器，一旦B系统处理完了就修改zookeeper那个节点的值，A系统立马就可以收到通知，完美解决。  

### 1.4.5. 统一命名服务  
&emsp; 分布式应用中，通常需要一套完整的命名规则，即能保证唯一又便于人识别和记住，通常采用树形的名称结构。  
&emsp; 在zookeeper的文件系统里创建一个目录，即有唯一的path，调用create接口即可建一个目录节点。  

### 1.4.6. 队列管理
&emsp; 两种类型的队列：  
1. 同步队列，当一个队列的成员都聚齐时，这个队列才可用，否则一直等待所有成员到达。  
2. 队列按照FIFO方式进行入队和出队操作。 
 
&emsp; 第一类，在约定目录下创建临时目录节点，监听节点数目是否是要求的数目。  
&emsp; 第二类，和分布式锁服务中的控制时序场景基本原理一致，入列有编号，出列按编号。  
