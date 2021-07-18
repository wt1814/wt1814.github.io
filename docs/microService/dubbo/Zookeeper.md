
<!-- TOC -->

- [1. Zookeeper](#1-zookeeper)
    - [1.1. Zookeeper是什么](#11-zookeeper是什么)
    - [1.2. ZooKeeper分层命名空间(逻辑存储结构)](#12-zookeeper分层命名空间逻辑存储结构)
    - [1.3. ★★★Zookeeper的运行原理](#13-★★★zookeeper的运行原理)
        - [1.3.2. C/S之间的Watcher机制](#132-cs之间的watcher机制)
        - [1.3.3. ★★★服务端通过ZAB协议，保证主从节点数据一致性](#133-★★★服务端通过zab协议保证主从节点数据一致性)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "clime">Zookeeper是一个分布式协调服务的开源框架。主要用来解决分布式集群中应用系统的一致性问题。</font>**  
3. ~~C/S之间的Watcher机制~~  
&emsp; ~~特性：~~  
    * 异步发送
    * 一次性触发  
    &emsp; Watcher通知是一次性的， **<font color = "clime">即一旦触发一次通知后，该Watcher就失效了，因此客户端需要反复注册Watcher。</font>** 但是在获取watch事件和设置新的watch事件之间有延迟。延迟为毫秒级别，理论上会出现不能观察到节点的每一次变化。  
    &emsp; `不支持用持久Watcher的原因：如果Watcher的注册是持久的，那么必然导致服务端的每次数据更新都会通知到客户端。这在数据变更非常频繁且监听客户端特别多的场景下，ZooKeeper无法保证性能。`  
    * 有序性：`⚠️（两阶段）客户端先得到watch通知，才可查看节点变化结果`。  
4. ZK服务端通过ZAB协议保证数据顺序一致性。  
    1. ZAB协议：
        1. **<font color = "clime">崩溃恢复</font>**  
            * 服务器启动时的leader选举：每个server发出投票，投票信息包含(myid, ZXID,epoch)；接受投票；处理投票(epoch>ZXID>myid)；统计投票；改变服务器状态。</font>  
            * 运行过程中的leader选举：变更状态 ---> 发出投票 ---> 处理投票 ---> 统计投票 ---> 改变服务器的状态。
        2. **<font color = "clime">`消息广播（数据读写流程，读写流程）：`</font>**  
            &emsp; 在zookeeper中，客户端会随机连接到zookeeper集群中的一个节点。    
            * 如果是读请求，就直接从当前节点中读取数据。  
            * 如果是写请求，那么请求会被转发给 leader 提交事务，然后leader会广播事务，只要有超过半数节点写入成功，那么写请求就会被提交。 
            &emsp; ⚠️注：leader向follower写数据详细流程：类2pc(两阶段提交)。  
    2.  数据一致性  
        &emsp; **<font color = "red">Zookeeper保证的是CP，即一致性(Consistency)和分区容错性(Partition-Tolerance)，而牺牲了部分可用性(Available)。</font>**  
        * 为什么不满足AP模型？<font color = "red">zookeeper在选举leader时，会停止服务，直到选举成功之后才会再次对外提供服务。</font>
        * Zookeeper的CP模型：非强一致性， **<font color = "clime">而是单调一致性/顺序一致性。</font>**  
            1. <font color = "clime">假设有2n+1个server，在同步流程中，leader向follower同步数据，当同步完成的follower数量大于n+1时同步流程结束，系统可接受client的连接请求。</font><font color = "red">如果client连接的并非同步完成的follower，那么得到的并非最新数据，但可以保证单调性。</font> 未同步数据的情况，Zookeeper提供了同步机制（可选型），类似回调。   
            2. follower接收写请求后，转发给leader处理；leader完成两阶段提交的机制。向所有server发起提案，当提案获得超过半数(n+1)的server认同后，将对整个集群进行同步，超过半数(n+1)的server同步完成后，该写请求完成。如果client连接的并非同步完成follower，那么得到的并非最新数据，但可以保证单调性。  
5. 服务端脑裂：过半机制，要求集群内的节点数量为2N+1。  

# 1. Zookeeper
&emsp; Zookeeper官网文档：https://zookeeper.apache.org/doc/current
<!--
消息广播、Zab 与 Paxos 算法的联系与区别
https://www.cnblogs.com/zz-ksw/p/12786067.html
Zookeeper数据一致性中顺序一致性和最终一致性的区别
https://blog.csdn.net/weixin_47727457/article/details/106439452

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

## 1.1. Zookeeper是什么  
&emsp; **<font color = "clime">Zookeeper是一个分布式协调服务的开源框架。主要用来解决分布式集群中应用系统的一致性问题，</font>** 例如怎样避免同时操作同一数据造成脏读的问题。  
&emsp; **Zookeeper集群特性：**  

* 全局数据一致：每个server保存一份相同的数据副本，client无论连接到哪个server，展示的数据都是一致的，这是最重要的特征。  
* 可靠性：如果消息被其中一台服务器接受，那么将被所有的服务器接受。  
* **<font color = "red">顺序性：</font>** 包括全局有序和偏序两种。全局有序是指如果在一台服务器上消息a在消息b前发布，则在所有Server上消息a都将在消息b前被发布；偏序是指如果一个消息b在消息a后被同一个发送者发布，a必将排在b前面。  
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


### 1.3.2. C/S之间的Watcher机制  
&emsp; [Watcher](/docs/microService/dubbo/Watcher.md)  

### 1.3.3. ★★★服务端通过ZAB协议，保证主从节点数据一致性
&emsp; [ZAB](/docs/microService/dubbo/ZAB.md)  