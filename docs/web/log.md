

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

## 日志配置文件  


