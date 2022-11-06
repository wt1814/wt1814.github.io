
<!-- TOC -->

- [1. 日志系统](#1-日志系统)
    - [1.1. 编码过程输出日志](#11-编码过程输出日志)
    - [1.2. Slf4j](#12-slf4j)
        - [1.2.1. log4j2](#121-log4j2)
        - [1.2.2. logback](#122-logback)
            - [1.2.2.1. logback性能](#1221-logback性能)

<!-- /TOC -->

# 1. 日志系统

<!-- 

日志门面slf4j
https://mp.weixin.qq.com/s?__biz=Mzg3NzU5NTIwNg==&mid=2247488033&idx=1&sn=decbb336691a95825ef2abeda277c997&source=41#wechat_redirect
https://mp.weixin.qq.com/s?__biz=Mzg2MjEwMjI1Mg==&mid=2247490130&idx=3&sn=899dc48623a00ad1fb204a63958045b0&chksm=ce0dadd1f97a24c7a54c8ba4049e3f011e79eda09acf0a3481daa10be433ff6e5b36a0d24ff3&mpshare=1&scene=1&srcid=&sharer_sharetime=1572953697601&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=f8a21a8df9909cbb19ae7eb4ea2a8e233043aacc6fde197e66e73e2fa167b5eeedf76b3ed4a7b357e627ad337b25ed67b836d2bdc741d9be20c72296d5cd695a9d0087eaae4312d7c1c124b0cb12be48&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=oO0JeKFHPK4ifYXARHWcz%2ByO29zMw9sI%2FiH99TTESnd8Wbv3LNtJVGdX%2BPzdWpL6


log4j2
https://www.jianshu.com/p/62c0ef1cc699
https://mp.weixin.qq.com/s?__biz=Mzg2MDYzODI5Nw==&mid=2247493856&idx=1&sn=fcde3677771e69ed0e429a684681a1e7&source=41#wechat_redirect


logback
logback.xml配置如何按天输出日志文件  
https://my.oschina.net/bootstrap/blog/414079
java 中使用logback日志，并实现日志按天分类压缩保存
https://www.cnblogs.com/devise/p/9974662.html
java 中使用logback日志，并实现日志按天分类压缩保存。
https://www.cnblogs.com/devise/p/9974662.html
一文带你了解logback的一些常用配置
http://www.justdojava.com/2019/09/12/logback-basic/


日志收集
SpringBoot使用Graylog日志收集 
https://mp.weixin.qq.com/s/mVQ9YJL10_VDobumsj-PqA
日志系统新贵 Loki，真香！！ 
https://mp.weixin.qq.com/s/Hq1VGdHDP3o_bmYLFfbHIw



 ***MDC工具类：快速过滤出一次请求的所有日志


https://mp.weixin.qq.com/s?__biz=MzU2MTI4MjI0MQ==&mid=2247486475&idx=2&sn=5f94e533860c3977c1b2a410e849d70b&chksm=fc7a61a5cb0de8b3b5b58955b7fba2b9ad36a9ad888ce1ccf6765fcb4292a1f1247773e847e5&mpshare=1&scene=1&srcid=&key=00a8e91eefd868fc4b3eda558cb3a40f5375515011aa4e1850228ff13cf347d94b072b0f41ef9e1620233af5f9ddfe00e3895782b5a28c290b6780212035ecc8e490bcbee13d428b7f7cedd0fc4a2262&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060833&lang=zh_CN&pass_ticket=WvQRNfXTbzo8YbNsaaP3bvOrF4WWy2nhzya3QiHsgSx6qD6EFNaOiTxgL7MHqDsT

logback日志与MDC机制 
https://www.iteye.com/blog/shift-alt-ctrl-2345272
slf4j中的MDC
https://www.cnblogs.com/sealedbook/p/6227452.html



logback自定义变量
logback自定义变量获取hostname
https://blog.csdn.net/weixin_33981932/article/details/92531278

logback自定义PatternLayout
ogback 实现 （ASNI）彩色日志（还原Spring boot 彩色日志）
https://blog.csdn.net/u012693119/article/details/79716306

Log4j写入日志到数据库
https://blog.csdn.net/ziruobing/article/details/3919501

正确的打日志姿势！
https://mp.weixin.qq.com/s/jNVgfjg4edl6aWy6FsffEg

-->



## 1.1. 编码过程输出日志  
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

## 1.2. Slf4j  


### 1.2.1. log4j2
&emsp; 参考[log4j2](/docs/web/log4j2.md)  


### 1.2.2. logback

#### 1.2.2.1. logback性能
&emsp; 修改logbcak配置，使用框架提供的异步输出代替同步输出。  

<!-- 

logback日志导致的性能问题
https://blog.csdn.net/qq_38536878/article/details/123821072

-->


