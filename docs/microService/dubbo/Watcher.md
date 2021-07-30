

1. ~~C/S之间的Watcher机制~~  
&emsp; ~~特性：~~  
    * 异步发送
    * 一次性触发  
    &emsp; Watcher通知是一次性的， **<font color = "clime">即一旦触发一次通知后，该Watcher就失效了，因此客户端需要反复注册Watcher。</font>** 但是在获取watch事件和设置新的watch事件之间有延迟。延迟为毫秒级别，理论上会出现不能观察到节点的每一次变化。  
    &emsp; `不支持用持久Watcher的原因：如果Watcher的注册是持久的，那么必然导致服务端的每次数据更新都会通知到客户端。这在数据变更非常频繁且监听客户端特别多的场景下，ZooKeeper无法保证性能。`  
    * 有序性：`⚠️（两阶段）客户端先得到watch通知，才可查看节点变化结果`。  

# Watcher机制
&emsp; 在ZooKeeper中，引入Watcher机制来实现分布式数据的发布/订阅功能。Zookeeper客户端向服务端的某个Znode注册一个Watcher监听，当服务端的一些指定事件触发了这个Watcher，服务端会向指定客户端发送一个事件通知来实现分布式的通知功能，然后客户端根据Watcher通知状态和事件类型做出业务上的改变。  
&emsp; 触发watch事件种类很多，如：节点创建，节点删除，节点改变，子节点改变等。  

&emsp; <font color = "red">Watcher机制运行流程：客户端注册Watcher、服务器处理Watcher和客户端回调Watcher。</font>  

&emsp; **watch的重要特性：**  

* 一次性触发：  
&emsp; Watcher通知是一次性的，即一旦触发一次通知后，该Watcher就失效了，因此客户端需要反复注册Watcher。但是在获取watch事件和设置新的watch事件之间有延迟。延迟为毫秒级别，理论上会出现不能观察到节点的每一次变化。  
&emsp; `不支持用持久Watcher的原因：如果Watcher的注册是持久的，那么必然导致服务端的每次数据更新都会通知到客户端。这在数据变更非常频繁且监听客户端特别多的场景下，ZooKeeper无法保证性能。`  
* 有序性：  
&emsp; 客户端先得到watch通知才可查看节点变化结果。  

