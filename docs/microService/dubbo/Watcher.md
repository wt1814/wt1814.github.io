
<!-- TOC -->

- [1. ~~客户端和服务端的Watcher机制~~](#1-客户端和服务端的watcher机制)
    - [1.1. Watcher机制运行流程](#11-watcher机制运行流程)
        - [1.1.1. Watcher接口](#111-watcher接口)
        - [1.1.2. 流程详解](#112-流程详解)
            - [1.1.2.1. 客户端注册 Watcher](#1121-客户端注册-watcher)
            - [1.1.2.2. 服务端处理 Watcher](#1122-服务端处理-watcher)
            - [1.1.2.3. 客户端回调 Watcher](#1123-客户端回调-watcher)
    - [1.2. Watcher的重要特性](#12-watcher的重要特性)

<!-- /TOC -->

# 1. ~~客户端和服务端的Watcher机制~~
<!-- 
https://blog.nowcoder.net/n/16f13a7d72b2496c8ff4da080f777a5a
https://segmentfault.com/a/1190000022856773
https://lvqiushi.github.io/2020/01/22/zookeeper-2/
https://cloud.tencent.com/developer/article/1648640

-->
&emsp; 在ZooKeeper中，引入Watcher机制来实现分布式数据的发布/订阅功能。

## 1.1. Watcher机制运行流程
1. Watcher机制运行流程：Zookeeper客户端向服务端的某个Znode注册一个Watcher监听，当服务端的一些指定事件触发了这个Watcher，服务端会向指定客户端发送一个事件通知来实现分布式的通知功能，然后客户端根据Watcher通知状态和事件类型做出业务上的改变。  
&emsp; 触发watch事件种类很多，如：节点创建，节点删除，节点改变，子节点改变等。  
&emsp; 概括可以分为三个过程：1. 客户端注册 Watcher；2. 服务端处理 Watcher；3. 客户端回调 Watcher。  
![image](http://182.92.69.8:8081/img/microService/zookeeper/zk-5.png)  
![image](http://182.92.69.8:8081/img/microService/zookeeper/zk-6.png)  
&emsp; 大致流程就是 Client 向ZK中注册 Watcher，如果注册成功的话，会将对应的 Watcher 存储在本地。当ZK服务器端触发 Watcher 事件之后，会向客户端发送通知，客户端会从 ClientWatchManager 中取出对应的 Watcher 进行回调。  

### 1.1.1. Watcher接口
<!-- 
https://segmentfault.com/a/1190000022856773
-->

### 1.1.2. 流程详解
&emsp; 概括可以分为三个过程：  

1. 客户端注册 Watcher
2. 服务端处理 Watcher
3. 客户端回调 Watcher

#### 1.1.2.1. 客户端注册 Watcher
<!-- 
https://segmentfault.com/a/1190000022856773
https://blog.nowcoder.net/n/16f13a7d72b2496c8ff4da080f777a5a
-->


#### 1.1.2.2. 服务端处理 Watcher

#### 1.1.2.3. 客户端回调 Watcher



## 1.2. Watcher的重要特性
1.  **watch的重要特性：**  
    * 异步发送
    * 一次性触发：  
    &emsp; Watcher通知是一次性的， **<font color = "clime">即一旦触发一次通知后，该Watcher就失效了，因此客户端需要反复注册Watcher。</font>** 但是在获取watch事件和设置新的watch事件之间有延迟。延迟为毫秒级别，理论上会出现不能观察到节点的每一次变化。  
    &emsp; `不支持用持久Watcher的原因：如果Watcher的注册是持久的，那么必然导致服务端的每次数据更新都会通知到客户端。这在数据变更非常频繁且监听客户端特别多的场景下，ZooKeeper无法保证性能。`  
    * 有序性：  
    &emsp; 客户端先得到watch通知才可查看节点变化结果。  
2. 客户端过多，会引发网络风暴。  

