


# 资源限制  
&emsp; 并发高，但是服务器资源有限，可以采取哪些方案？  
1. [缓存](/docs/cache/Cache.md)、[限流](/docs/microService/thinking/CurrentLimiting.md)、[降级](/docs/microService/thinking/Demotion.md)；
2. [mq消峰](/docs/microService/mq/mq.md)；
3. 将请求放入队列中（包括但不限于阻塞队列、高效队列、redis队列...）；  
4. 使用服务器请求队列。例如tomcat的server.xml中增大acceptCount值。  
&emsp; acceptCount：当tomcat请求处理线程池中的所有线程都处于忙碌状态时，此时新建的链接将会被放入到pending队列，acceptCount即是此队列的容量，如果队列已满，此后所有的建立链接的请求(accept)，都将被拒绝。默认为100。在高并发/短链接较多的环境中，可以适当增大此值；当长链接较多的场景中，可以将此值设置为0。  
