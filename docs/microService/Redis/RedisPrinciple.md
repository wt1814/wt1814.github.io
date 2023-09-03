
<!-- 
5. 缺点  
上面介绍了单线程可以达到如此高的性能，并不是说它就没有缺点了。

单线程处理最大的缺点就是，如果前一个请求发生耗时比较久的操作，那么整个Redis就会阻塞住，其他请求也无法进来，直到这个耗时久的操作处理完成并返回，其他请求才能被处理到。  
我们平时遇到Redis变慢或长时间阻塞的问题，90%也都是因为Redis处理请求是单线程这个原因导致的。  

所以，我们在使用Redis时，一定要避免非常耗时的操作，例如使用时间复杂度过高的方式获取数据、一次性获取过多的数据、大量key集中过期导致Redis淘汰key压力变大等等，这些场景都会阻塞住整个处理线程，直到它们处理完成，势必会影响业务的访问。  

-->

<!-- 
Redis为什么这么快？ 
https://mp.weixin.qq.com/s/v4ORkYyjfLxYVNhzaJH8tw
-->

&emsp; 从`内存、磁盘、网络IO、CPU`分析。  

&emsp; Redis的性能非常之高，每秒可以承受10W+的QPS，它如此优秀的性能主要取决于以下几个方面：  

1. 内存  
    * [Redis虚拟内存机制](/docs/microService/Redis/RedisVM.md)  
    * [Redis内存淘汰](/docs/microService/Redis/RedisEliminate.md)    
    * [Redis过期键删除](/docs/microService/Redis/Keydel.md)  

2. 磁盘I/O：  
    * 合理的数据编码  
    * [Redis持久化](/docs/microService/Redis/RedisPersistence.md)  
        * [AOF重写阻塞](/docs/microService/Redis/Rewrite.md)  

3. 网络I/O：  
    * [使用IO多路复用技术](/docs/microService/Redis/RedisEvent.md) 
    * [Redis事件/Reactor](/docs/microService/Redis/RedisEvent.md)   
    * [Redis多线程模型](/docs/microService/Redis/RedisMultiThread.md)   
    * [简单快速的Redis协议](/docs/microService/Redis/RESP.md)  

4. ......



