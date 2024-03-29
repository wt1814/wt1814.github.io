
<!-- TOC -->

- [1. Zookeeper使用](#1-zookeeper使用)
    - [1.1. Zookeeper应用场景](#11-zookeeper应用场景)
        - [1.1.1. 统一命名服务，生成分布式ID](#111-统一命名服务生成分布式id)
        - [1.1.2. 分布式锁](#112-分布式锁)
        - [1.1.3. 队列管理](#113-队列管理)
        - [1.1.4. 元数据/配置信息管理，数据发布/订阅](#114-元数据配置信息管理数据发布订阅)
        - [1.1.5. 分布式协调](#115-分布式协调)
        - [1.1.6. 集群管理，HA高可用性](#116-集群管理ha高可用性)
    - [1.2. Zookeeper的API](#12-zookeeper的api)
        - [1.2.1. 原生API](#121-原生api)
        - [1.2.2. ZkClient](#122-zkclient)
        - [1.2.3. Curator](#123-curator)
    - [1.3. Zookeeper监控](#13-zookeeper监控)
        - [1.3.1. Zookeeper的四字命令](#131-zookeeper的四字命令)
        - [1.3.2. IDEA zookeeper插件的使用](#132-idea-zookeeper插件的使用)
        - [1.3.3. JMX](#133-jmx)
    - [1.4. 部署](#14-部署)

<!-- /TOC -->

# 1. Zookeeper使用  
<!-- 
acl权限设置 
https://mp.weixin.qq.com/s/PZPSQcxJEeBFYgN2lJoSRQ
-->

## 1.1. Zookeeper应用场景  
<!-- 

*** https://www.cnblogs.com/leesf456/p/6036548.html
-->
&emsp; **<font color = "clime">Zookeeper的原理包含有目录树结构、watcher机制...</font>**

### 1.1.1. 统一命名服务，生成分布式ID  
&emsp; 分布式应用中，通常需要一套完整的命名规则，即能保证唯一又便于人识别和记住，通常采用树形的名称结构。  
&emsp; 在zookeeper的文件系统里创建一个目录，即有唯一的path，调用create接口即可建一个目录节点。  

### 1.1.2. 分布式锁  
&emsp; 有了zookeeper的一致性文件系统，锁的问题变得容易。锁服务可以分为两类，一个是保持独占，另一个是控制时序。  
&emsp; 对于第一类，将zookeeper上的一个znode看作是一把锁，通过createznode的方式来实现。所有客户端都去创建/distribute_lock节点，最终成功创建的那个客户端也即拥有了这把锁。用完删除掉自己创建的distribute_lock节点就释放出锁。  
&emsp; 对于第二类，/distribute_lock已经预先存在，所有客户端在它下面创建临时顺序编号目录节点，和选master一样，编号最小的获得锁，用完删除，依次方便。  

### 1.1.3. 队列管理
&emsp; 两种类型的队列：  
1. 同步队列，当一个队列的成员都聚齐时，这个队列才可用，否则一直等待所有成员到达。  
2. 队列按照FIFO方式进行入队和出队操作。 
 
&emsp; 第一类，在约定目录下创建临时目录节点，监听节点数目是否是要求的数目。  
&emsp; 第二类，和分布式锁服务中的控制时序场景基本原理一致，入列有编号，出列按编号。  

------------------

### 1.1.4. 元数据/配置信息管理，数据发布/订阅
&emsp; 配置的分布式管理，相同的配置需要应用到多台机器上。这种配置完全可以由zookeeper来管理，将配置信息保存在某个目录节点中，然后将所有的应用机器监控配置信息的状态，一旦发生变化，就可以从zookeeper中获取最新的配置信息，并应用到系统中。  
&emsp; 发布与订阅模型，即所谓的配置中心，顾名思义就是发布者将数据发布到ZK节点上，供订阅者动态获取数据，实现配置信息的集中式管理和动态更新。例如全局的配置信息，服务式服务框架的服务地址列表等就非常合适用。  


### 1.1.5. 分布式协调
<!-- 
ZooKeeper的典型应用场景之分布式协调/通知
https://blog.csdn.net/en_joker/article/details/78799737
-->  
&emsp; 分布式协调/通知服务是分布式系统中不可缺少的一个环节，是将不同的分布式组件有机结合起来的关键所在。 **<font color = "clime">对于一个在多台机器上部署运行的应用而言，通常需要一个协调者(Coordinator)来控制整个系统的运行流程，例如分布式事务的处理、机器间的互相协调等。</font>** 同时，引入这样一个协调者，便于将分布式协调的职责从应用中分离出来，从而可以大大减少系统之间的耦合性，而且能够显著提高系统的可扩展性。  
&emsp; <font color = "clime">ZooKeeper中特有的Watcher注册与异步通知机制，能够很好的实现分布式环境下不同机器，甚至是不同系统之间的协调与通知，从而实现对数据变更的实时处理。基于ZooKeeper实现分布式协调与通知功能，从而实现对数据变更的实时处理。</font><font color = "red">基于ZooKeeper实现分布式协调与通知功能，通常的做法是不同的客户端都对ZooKeeper上同一个数据节点进行Watcher注册，监听数据节点的变化(包括数据节点本身及其子节点)，如果数据节点发生变化，那么所有订阅的客户端都能够接收到相应的Watcher通知，并做出相应的处理。</font>  

&emsp; 这个其实是zookeeper很经典的一个用法，简单来说，就好比，A系统发送个请求到mq，然后B系统消息消费之后处理了。那A系统如何知道B系统的处理结果？用zookeeper就可以实现分布式系统之间的协调工作。A系统发送请求之后可以在zookeeper上对某个节点的值注册个监听器，一旦B系统处理完了就修改zookeeper那个节点的值，A系统立马就可以收到通知，完美解决。  

### 1.1.6. 集群管理，HA高可用性
![image](http://182.92.69.8:8081/img/microService/Redis/redis-29.png) 

&emsp; 所谓集群管理无在乎两点：是否有机器退出和加入、选举master。  
&emsp; 对于第一点，所有机器约定在父目录GroupMembers下创建临时目录节点，然后监听父目录节点的子节点变化消息。一旦有机器挂掉，该机器与zookeeper的连接断开，其所创建的临时目录节点被删除，所有其他机器都收到通知。新机器加入也是类似，所有机器收到通知。  
&emsp; 对于第二点，稍微改变一下，所有机器创建临时顺序编号目录节点，每次选取编号最小的机器作为master就好。  
&emsp; 集群启动、集群同步、集群FastPaxos  

&emsp; hadoop、hdfs、yarn等很多大数据系统，都选择基于zookeeper来开发HA高可用机制，就是一个重要进程一般会做主备两个，主进程挂了立马通过 zookeeper 感知到切换到备用进程。



## 1.2. Zookeeper的API  
### 1.2.1. 原生API  
......  

### 1.2.2. ZkClient  
&emsp; Zkclient是由Datameer的工程师开发的开源客户端，对Zookeeper的原生API进行了包装，实现了超时重连，Watcher反复注册等功能。  

### 1.2.3. Curator  
&emsp; Curator是Netflix公司开源的一个Zookeeper客户端，与Zookeeper提供的原生客户端相比，Curator的抽象层次更高，简化了Zookeeper客户端的开发量。  

## 1.3. Zookeeper监控  
<!-- 
taokeeper
-->
### 1.3.1. Zookeeper的四字命令  
.......  

### 1.3.2. IDEA zookeeper插件的使用  
......  

### 1.3.3. JMX  
......


## 1.4. 部署
&emsp; Windows 下的启动脚本是 zkServer.cmd。  

&emsp; ZooKeeper的三种部署方式：  

* 单机模式，即部署在单台机器上的一个 ZK 服务，适用于学习、了解 ZK 基础功能；
* 伪分布模式，即部署在一台机器上的多个（原则上大于3个）ZK 服务，伪集群，适用于学习、开发和测试；
* 全分布式模式（复制模式），即在多台机器上部署服务，真正的集群模式，生产环境中使用。


