

# 开发中遇到的难题  
## 接口响应时间问题  


## pom修改后，代码报错  
&emsp; 看错误到源码级别，看不出来。  

## json格式化  
&emsp; 先xml转json，再json转json。  

## mysql
### 死锁  
<!-- 
https://mp.weixin.qq.com/s/1mO8q-RJrxx1OTtM8dR4Ng
-->


## Redis
### Redis服务器 can not get resource from pool
&emsp; 1000个线程并发还能跑，5000个线程的时候出现这种问题，查后台debug日志，发现redis 线程池不够。刚开始设置的是：  

&emsp; 等待时间  10s改为300s
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

### redis内存飘升  


## mq  



