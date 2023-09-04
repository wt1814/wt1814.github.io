


# Redis读写分离  
<!-- 
 Redis 生产架构选型解决方案 
 https://mp.weixin.qq.com/s/4d-zeaVbQFn7qT4DWagjOg

 https://baijiahao.baidu.com/s?id=1713333940467580300&wfr=spider&for=pc

哨兵机制的集群已经基本可以实现高可用，读写分离，但是在这种模式下，每台Redis服务器都存储了相同的数据，非常浪费内存，所以在Redis3.0上加入了Cluster集群模式，实现了Redis的分布式存储，即每台Redis节点上存储了不同的内容

-->

&emsp; 读写分离是一种使用方式，可以基于主从复制、哨兵模式实现。  

