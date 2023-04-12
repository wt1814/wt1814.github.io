
<!-- TOC -->

- [1. 使用ZK的问题](#1-使用zk的问题)
    - [1.1. 羊群效应/惊群现象](#11-羊群效应惊群现象)
        - [1.1.1. 什么是羊群效应？](#111-什么是羊群效应)
        - [1.1.2. 解决方案](#112-解决方案)

<!-- /TOC -->


# 1. 使用ZK的问题

广播风暴  
服务端节点数比较多  
客户端连接比较多  
服务端节点下的字节点比较多  
某个节点的注册者表多  

<!-- 
https://blog.csdn.net/zh521zh/article/details/51731818
https://blog.csdn.net/weixin_33716154/article/details/92614922?utm_term=zookeeper%E8%BF%9E%E6%8E%A5%E8%BF%87%E5%A4%9A&utm_medium=distribute.pc_aggpage_search_result.none-task-blog-2~all~sobaiduweb~default-0-92614922&spm=3001.4430
https://blog.csdn.net/yuanwangliu/article/details/50680802
-->



## 1.1. 羊群效应/惊群现象
<!-- 
https://www.shuzhiduo.com/A/MyJx9OMe5n/
https://cloud.tencent.com/developer/article/1678078
-->


### 1.1.1. 什么是羊群效应？  
&emsp; 羊群效应理论（The Effect of Sheep Flock），也称羊群行为（Herd Behavior）、从众心理。 羊群是一种很散乱的组织，平时在一起也是盲目地左冲右撞，但一旦有一只头羊动起来，其他的羊也会不假思索地一哄而上，全然不顾旁边可能有的狼和不远处更好的草。看到这里，就应该知道了，当多个客户端请求获取zk创建临时节点来进行加锁的时候，会进行竞争，因为zk独有的一个特性：即watch机制。啥意思呢？就是当A获取锁并加锁的时候，B会监听A的结点变化，当A创建的临时结点被删除的时候，B会去竞争锁。懂了没？  
&emsp; 那么问题来了？如果同时有1000个客户端发起请求并创建临时节点，都会去监听A结点的变化，然后A删除节点的时候会通知其他节点，这样是否会太影响并耗费资源了？  


### 1.1.2. 解决方案  
&emsp; 在使用ZK时，要尽量避免出现羊群效应。但是如果出现了该怎么解决？  
1. 如果ZK是用于实现分布式锁，使用临时顺序节点。 ~~未获取到锁的客户端给自己的上一个临时有序节点添加监听~~    
2. 如果ZK用于其他用途，则分析出现羊群效应的问题，从根本上解决问题或提供其他替代ZK的方案。  
