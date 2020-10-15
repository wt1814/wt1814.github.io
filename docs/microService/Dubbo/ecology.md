


# Dubbo生态
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo生态.png)   

## Dubbo与分布式事务  

Dubbo 支持分布式事务吗？   
目前暂时不支持， 可与通过 tcc-transaction 框架实现   
介绍： tcc-transaction 是开源的 TCC 补偿性分布式事务框架  
Git 地址： https://github.com/changmingxie/tcc-transaction  
TCC-Transaction 通过 Dubbo 隐式传参的功能， 避免自己对业务代码的入侵。 


