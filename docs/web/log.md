

# 日志系统
## 编码过程输出日志  
<!-- 
如何在12个小时，搞定日志监控？
https://mp.weixin.qq.com/s/6Bjr1PUAC98IIaxBFcECNw

LogBack的filter的应用
https://blog.csdn.net/wangzhan0123/article/details/81219789

Slf4j适配日志原理 
https://mp.weixin.qq.com/s/hGtkjhG1Wz9BwrLKU4ph-w

mybatis日志功能是如何设计的？ 
https://mp.weixin.qq.com/s/JkdszV7Oy9E9cITNebY2NA

日志打印的15个建议 
https://mp.weixin.qq.com/s/D7rye88cki8rXMg0v1-dVw

-->  
&emsp; 使用[]进行参数变量隔离  
&emsp; 如有参数变量，应该写成如下写法:  

```java
logger.debug("Processing trade with id:[{}] and symbol : [{}] ", id, symbol);
```
&emsp; 这样的格式写法，可读性更好，对于排查问题更有帮助。  

## Slf4j  
### log4j2
<!-- 

https://blog.csdn.net/qq_43842093/article/details/123027783
log4j2异步日志的使用
https://blog.csdn.net/qq_26323323/article/details/124741008
https://blog.csdn.net/w1047667241/article/details/115894754

-->

### logback

#### logback性能
&emsp; 修改logbcak配置，使用框架提供的异步输出代替同步输出。  

<!-- 

logback日志导致的性能问题
https://blog.csdn.net/qq_38536878/article/details/123821072

-->


