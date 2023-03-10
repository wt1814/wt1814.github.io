

# 分布式事务场景   
<!-- 
https://blog.csdn.net/qq_36700462/article/details/125789325

-->

场景   
1、单体服务多数据源，数据分部在不同的数据库实例，同时操作不同的数据库连接进行数据操作，跨数据库实例产生分布式事务。  

2、多服务访问同一个数据库，不同服务各自持有数据库连接实例，跨JVM进程，产生分布式事务。  

3、微服务场景下，多个服务拥有各自的数据库实例，通过服务间远程调用，操作数据库完成业务场景，产生分布式事务。  



