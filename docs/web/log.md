


<!-- 


LogBack的filter的应用
https://blog.csdn.net/wangzhan0123/article/details/81219789

-->

# 编码过程输出日志    
&emsp; 使用[]进行参数变量隔离  
&emsp; 如有参数变量，应该写成如下写法:  

```java
logger.debug("Processing trade with id:[{}] and symbol : [{}] ", id, symbol);
```
&emsp; 这样的格式写法，可读性更好，对于排查问题更有帮助。  

# 日志配置文件  


