


# Dubbo常见问题

## DUBBO线程池打满
<!-- 
https://mp.weixin.qq.com/s/3EX7rnIrj_lESoReKf00FQ
 为什么一段看似正确的代码会导致DUBBO线程池被打满 
 https://mp.weixin.qq.com/s/OW6cSK3xl1fZfQwCjWWqqg
-->
![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-60.png)   

## 先启动客户端再启动服务端，注入消费者为null
<!-- 
https://mp.weixin.qq.com/s/xdhXF8UzUmAhdCsJjDXtag
-->
1. 先启动producer，再启动consumer，正常调用
2. 先启动consumer（check=true），再启动producer，代理对象为空，完美复现
3. 先启动consumer（check=false），再启动producer，正常调用


