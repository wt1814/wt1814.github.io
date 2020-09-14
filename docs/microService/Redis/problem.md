

# Redis

## Redis服务器 can not get resource from pool
&emsp; <font color = "lime">Redis连接池值比较小；也有可能是未释放连接数。</font>  

&emsp; 1000个线程并发还能跑，5000个线程的时候出现这种问题，查后台debug日志，发现redis 线程池不够。刚开始设置的是：  

&emsp; 等待时间10s改为300s  
&emsp; maxTotal 资源池中最大连接数 默认值8 建议值  

```xml
# redis 配置文件
#redis
redis.host=127.0.0.1
redis.port=6379
redis.timeout=300        等待时间  10s改为300s
redis.password=123456
redis.poolMaxTotal=1000   连接数，刚开始最大连接数 设置为100.
redis.poolMaxIdle=500      最大空闲连接数  100改成500
redis.poolMaxWait=300      
```

## Redis连接超时
<!-- 
【95期】面试官：你遇到 Redis 线上连接超时一般如何处理？ 
https://mp.weixin.qq.com/s/LqeeCviPW84ykfPhluTMlQ

-->
&emsp; redis响应变慢，查看日志，发现大量 TimeoutException。  


## Redis内存耗尽  
<!-- 
一次生产环境redis内存占用居高不下问题排查
https://blog.csdn.net/eene894777/article/details/102820565?utm_medium=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param

https://blog.csdn.net/Zhenxue_Xu/article/details/90727983
https://www.cnblogs.com/yinliang/p/7498529.html
https://blog.csdn.net/weixin_41507324/article/details/90742075?utm_medium=distribute.pc_relevant.none-task-blog-title-5&spm=1001.2101.3001.4242


记录一次生产环境中Redis内存增长异常排查全流程！ 
https://mp.weixin.qq.com/s/1Rqzn4juKMqlNK9sBzlGig
-->

## Redis延迟

<!-- 
 Redis为什么变慢了？常见延迟问题定位与分析 
 https://mp.weixin.qq.com/s/tmMlDy3ESq6-5sOnsGbAWQ
-->

