

# 1. Redis的API
&emsp; 官网推荐的 Java 客户端有 3 个 Jedis，Redisson 和 Luttuce。  

* Jedis，轻量，简洁，便于集成和改造。  
* Lettuce   
&emsp; 与 Jedis 相比，Lettuce 则完全克服了其线程不安全的缺点：Lettuce 是一个可伸缩 的线程安全的 Redis 客户端，支持同步、异步和响应式模式（Reactive）。多个线程可 以共享一个连接实例，而不必担心多线程并发问题。  
&emsp; 同步调用：com.gupaoedu.lettuce.LettuceSyncTest。  
&emsp; 异步的结果使用 RedisFuture 包装，提供了大量回调的方法。  
&emsp; 异步调用：com.gupaoedu.lettuce.LettuceASyncTest。   

    &emsp; 它基于 Netty 框架构建，支持 Redis 的高级功能，如 Pipeline、发布订阅，事务、 Sentinel，集群，支持连接池。  

* Redisson  
&emsp; Redisson 是一个在 Redis 的基础上实现的 Java 驻内存数据网格（In-Memory Data Grid），提供了分布式和可扩展的 Java 数据结构。  
&emsp; 特点：  

    * 基于 Netty 实现，采用非阻塞 IO，性能高。  
    * 支持异步请求。  
    * 支持连接池、pipeline、LUA Scripting、Redis Sentinel、Redis Cluster。  
    * 不支持事务，官方建议以 LUA Scripting 代替事务。  
    * 主从、哨兵、集群都支持。Spring 也可以配置和注入 RedissonClient。  


## Redis中的批量删除数据库中的Key  
&emsp; Redis中有删除单个Key的指令 DEL，但似乎没有批量删除 Key 的指令，不过可以借助 Linux 的 xargs 指令来完成这个动作。  
<!-- 
https://www.cnblogs.com/DreamDrive/p/5772198.html
-->


