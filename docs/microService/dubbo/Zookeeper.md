
<!-- TOC -->

- [1. Zookeeper](#1-zookeeper)
    - [1.1. Zookeeper是什么](#11-zookeeper是什么)
    - [1.2. ZooKeeper分层命名空间(逻辑存储结构)](#12-zookeeper分层命名空间逻辑存储结构)
    - [1.3. Zookeeper的运行原理](#13-zookeeper的运行原理)
        - [1.3.1. 服务端通过ZAB协议，保证主从节点数据一致性](#131-服务端通过zab协议保证主从节点数据一致性)
        - [1.3.2. C/S之间的Watcher机制](#132-cs之间的watcher机制)
    - [1.4. ~~ZK的弊端~~](#14-zk的弊端)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "clime">Zookeeper是一个分布式协调服务的开源框架。主要用来解决分布式集群中应用系统的一致性问题。</font>**  
2. `ZK服务端`通过`ZAB协议`保证`数据顺序一致性`。  
3. 服务端脑裂：过半机制，要求集群内的节点数量为2N+1。  
4. zookeeper引入了`watcher机制`来实现`客户端和服务端`的发布/订阅功能。  
5. ZK的弊端：
	1. 服务端从节点多，主从同步慢。  
	2. 客户端多，`网络风暴`。~~watcher机制中，回调流程，只有主节点参与？~~  



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

## 1.3. Zookeeper的运行原理  
~~架构图~~
### 1.3.1. 服务端通过ZAB协议，保证主从节点数据一致性
&emsp; [ZAB](/docs/microService/dubbo/ZAB.md)  

### 1.3.2. C/S之间的Watcher机制  
&emsp; [Watcher](/docs/microService/dubbo/Watcher.md)  


## 1.4. ~~ZK的弊端~~
<!-- 
https://blog.csdn.net/wwwsq/article/details/7644445
https://zhuanlan.zhihu.com/p/37894143
-->

1. 服务端从节点多，主从同步慢。  
2. 客户端多，`网络风暴`。~~watcher机制中，回调流程，只有主节点参与？~~    
