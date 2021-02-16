
# Redis总结  
<!-- 

redis源码
https://mp.weixin.qq.com/mp/appmsgalbum?action=getalbum&__biz=MzAxODc1MzM5Nw==&scene=1&album_id=1340108909599883264&count=3#wechat_redirect
-->

1. Redis有5种基本数据类型，每种数据类型都有不同的数据结构；
2. Redis有3(Bitmaps位图、HyperLogLog基数统计、Geospatial地图)+1(Streams消息队列)+1(Redis中的布隆过滤器)种高级数据类型。  
3. Redis有持久化、内存淘汰、事务等特性。  
    * 内存淘汰具体选择哪种策略？
4. Redis有多种部署架构。单机、哨兵、集群。  
    * 哨兵
        * 作用：监控（心跳检查通过3次定时任务）、自动故障转移。  
        * **<font color = "lime">心跳检查：Sentinel通过三个定时任务来完成对各个节点的发现和监控。</font>**
        * **<font color = "lime">主观下线和客观下线：首先单个Sentinel节点认为数据节点主观下线，询问其他Sentinel节点，Sentinel多数节点认为主节点存在问题，这时该 Sentinel节点会对主节点做客观下线的决定。</font>**
        * **<font color = "lime">故障转移。</font>**    
        * **<font color = "lime">Sentinel选举：Sentinel集群是集中式架构，基于raft算法。</font>**    
    * 集群
        * 高性能（异步复制、去中心化）、高可用（自动故障转移）、高可扩展（集群伸缩）
        * 服务端：数据分槽、故障转移
        * 客户端和服务端通信：请求重定向、<font color = "red">ASK重定向</font>
5. Redis还有发布订阅、管道、Lua脚本...

<!--
Redis 竟然浪费了这么多内存！ 
https://mp.weixin.qq.com/s/2eCT5gaxLXEOK7Ax0BO_rQ
Redis的虚拟内存
https://mp.weixin.qq.com/s/CmfUSVfMss8TeQkLrE8GGQs



Pipeline 有什么好处，为什么要用pipeline？  
答：可以将多次 IO 往返的时间缩减为一次，前提是 pipeline 执行的指令之间没有因果相关性。使用 redis-benchmark 进行压测的时候可以发现影响 redis 的 QPS 峰值的一个重要因素是 pipeline 批次指令的数目。 


Redis 如何做内存优化？

尽可能使用散列表（ hashes）， 散列表（ 是说散列表里面存储的数少） 使用 的内存非常小， 所以你应该尽可能的将你的数据模型抽象到一个散列表里面。比如 你的 web 系统中有一个用户对象， 不要为这个用户的名称， 姓氏， 邮箱， 密码设置 单独的 key,而是应该把这个用户的所有信息存储到一张散列表里。  


假如 Redis 里面有 1 亿个key，其中有 10w 个key 是以某个固定的已知的前缀开头的，如果将它们全部找出来？
答： 使用 keys 指令可以扫出指定模式的 key 列表。  
对方接着追问： 如果这个 redis 正在给线上的业务提供服务， 那使用 keys 指令会有什么问题？  

这个时候你要回答 redis 关键的一个特性：redis 的单线程的。keys 指令会导致线程阻塞一段时间， 线上服务会停顿， 直到指令执行完毕， 服务才能恢复。这个时候可以使用scan 指令， scan 指令可以无阻塞的提取出指定模式的 key 列表， 但是会有一定的重复概率， 在客户端做一次去重就可以了， 但是整体所花费的时间会比直接用 keys 指令长。   


使用过 Redis 做异步队列么，你是怎么用的？

答：一般使用 list 结构作为队列，rpush 生产消息，lpop 消费消息。当 lpop 没有消息的时候， 要适当 sleep 一会再重试。

如果对方追问可不可以不用 sleep 呢？

list 还有个指令叫 blpop，在没有消息的时候，它会阻塞住直到消息到来。如果对方追问能不能生产一次消费多次呢？ 使用 pub/sub 主题订阅者模式， 可以实现1:N 的消息队列。

如果对方追问 pub/sub 有什么缺点？

在消费者下线的情况下，生产的消息会丢失，得使用专业的消息队列如 RabbitMQ 等。

如果对方追问 redis 如何实现延时队列？

我估计现在你很想把面试官一棒打死如果你手上有一根棒球棍的话， 怎么问的这么详细。但是你很克制，然后神态自若的回答道：使用 sortedset，拿时间戳作为score，消息内容作为 key 调用 zadd 来生产消息，消费者用 zrangebyscore 指令获取 N 秒之前的数据轮询进行处理。到这里， 面试官暗地里已经对你竖起了大拇指。但是他不知道的是此刻你却竖起了中指， 在椅子背后。
-->
