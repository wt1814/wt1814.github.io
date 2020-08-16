
# Redis总结  

1. Redis有5种基本数据类型，每种数据类型都有不同的数据结构；
2. Redis有3(Bitmaps位图、HyperLogLog基数统计、Geospatial地图)+1(Streams消息队列)+1(Redis中的布隆过滤器)种高级数据类型。  
3. Redis有持久化、内存淘汰、事务等特性。  
4. Redis有多种部署架构。单机、哨兵、集群。  
    * 哨兵
        * 作用：监控（心跳检查通过3次定时任务）、自动故障转移。  
        * **<font color = "lime">心跳检查：Sentinel通过三个定时任务来完成对各个节点的发现和监控。</font>**
        * **<font color = "lime">主观下线和客观下线：首先单个Sentinel节点认为数据节点主观下线，询问其他Sentinel节点， Sentinel多数节点认为主节点存在问题，这时该 Sentinel节点会对主节点做客观下线的决定。</font>**
        * **<font color = "lime">故障转移。</font>**    
        * **<font color = "lime">Sentinel选举：Sentinel集群是集中式架构，基于raft算法。</font>**    
    * 集群
        * 高性能（异步复制、去中心化）、高可用（自动故障转移）、高可扩展（集群伸缩）
        * 服务端：数据分槽、故障转移
        * 客户端和服务端通信：请求重定向、<font color = "red">ASK重定向</font>
5. Redis还有发布订阅、管道、Lua脚本...
