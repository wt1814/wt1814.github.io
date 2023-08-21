


&emsp; ~~架构的方方面面： 1).架构模式、2).性能/系统瓶颈。~~   

1. 功能、流程  
2. 性能/系统瓶颈：CPU、磁盘IO、内存、网络IO  


&emsp; 应用型框架，Spring、Mybatis  
&emsp; 功能性框架，redis、mq，一般都是分布式、高并发系统。  

* 分布式带来的问题：  
* 高并发系统的三高：  

<!-- 

 顶级架构师究竟有多牛？这个方法论带你跨越鸿沟！ 
 https://mp.weixin.qq.com/s/xzQe2Efy9I7nzNJftBWvLQ

数据中台
https://mp.weixin.qq.com/s/MrpIVY-u4jL7WDc8UvWaIQ


访问安全，鉴权
微服务如何实现「访问安全」？
在微服务架构下，有以下三种方案可以选择：
1).网关鉴权模式（API Gateway）
2).服务自主鉴权模式
3).API Token模式（OAuth2.0）
https://mp.weixin.qq.com/s?__biz=MzI2MTExOTA3Nw==&mid=2650502408&idx=1&sn=9184e8875e677c1651f14330bb8ec57b&chksm=f2509802c52711147f75de1e4300cb5f5c2bf9f6391a08db6c111b50360fa9ed216e12d768ab&mpshare=1&scene=1&srcid=&sharer_sharetime=1566698948555&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=a98b434d6faae616e90e97e51e408067b6a1eb2364b9e5e688e929903ae005c46b0b0fe4ea2406105a3af407304376eeeb8c57136ec96ba4afb4f9f00ea12385b765f4c9b4f38512b8cb68506685c0df&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=UdkKHp3zAdf78LuWubTqEqGIMoAcdSBRnjNG1p8%2FPFavkcsnnf0KgfvCZT5R%2B314

鉴权作用：
https://baike.baidu.com/item/%E9%89%B4%E6%9D%83/10857773?fr=aladdin&ms=1&rid=8512730653002098236
https://www.cnblogs.com/huangjianping/p/7911750.html
https://www.jianshu.com/p/4a00c0c3bf1d
https://blog.csdn.net/wang839305939/article/details/78713124/

-->



<!-- 

《大型网站系统与Java中间件实践》
《深入分析Java web技术内幕》
《大型网站技术架构：核心原理与案例分析》
《分布式系统原理介绍.pdf》
《微服务架构设计模式》书籍下载


架构整体设计：
高可用架构设计之硬件篇

DNS篇
通过DNS轮询实现机房间的负载均衡

全方位认识DNS（实践篇） 
https://mp.weixin.qq.com/s/vQt9Bq5prxvySBSW-y6XJw

CDN篇
如何给女朋友解释什么是CDN？
https://mp.weixin.qq.com/s?__biz=MjM5NzA1MTcyMA==&mid=2651169969&idx=2&sn=55094857c22f19c478f9ad623f1defd0&chksm=bd2ee8968a596180e4fb2e31b2ea3eb90a72fc81d333efab06fa866c4fc8997c649d21960f25&mpshare=1&scene=1&srcid=&sharer_sharetime=1566720760994&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=58e504541863490ebf8808ebda20e08fe5999bffcd0571a7d2c2d680855595b5872503855216bbed77b198987c2ee0791b677c50d62f826a97aab28621093ee06f3ee5ae3d78f3245e3b83143941a9b8&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=UdkKHp3zAdf78LuWubTqEqGIMoAcdSBRnjNG1p8%2FPFavkcsnnf0KgfvCZT5R%2B314

API网关：
API网关在微服务架构中的应用
https://mp.weixin.qq.com/s/iEpXeSjb_N4ImOJEuhCGDQ


LB负载

接入层篇
服务治理：熔断、降级、限流

一、服务器降级：
服务降级，当服务器压力剧增的情况下，根据当前业务情况及流量对一些服务和页面有策略的降级，以此释放服务器资源以保证核心任务的正常运行。也就是说，当服务器负载达到饱和时，将一些无关轻重、可有可无的服务停掉，以此来达到主功能的完整执行。

二、服务器熔断：
在互联网系统中，当下游服务因访问压力过大而响应变慢或失败，上游服务为了保护系统整体的可用性，可以暂时切断对下游服务的调用。
这种牺牲局部，保全整体的措施就叫做熔断。
降级与熔断使用场景不同。二者不可混淆。熔断是由于下游服务发生异常，而将下游服务停掉的做法；降级则是为了更好的完成主服务的执行而将一些服务停掉。

三、服务雪崩
1、服务血崩：当系统中的某个基础服务不可用时，导致整个系统不可用的情况。这种现象被称为服务雪崩效应. 为了应对服务雪崩, 一种常见的做法是手动服务降级. 而Hystrix的出现,给我们提供了另一种选择.

服务隔离：

服务隔离，指将系统按照一定的原则划分为若干个服务模块，各个模块之间相对独立，无强依赖。当有故障发生时，能将问题和影响隔离在某个模块内部，而不扩散风险，不波及其它模块，不影响整体的系统服务。
服务隔离的设计模式能降低依赖服务对整个系统的影响，保护有限的资源不被耗尽，提高了整个系统的可用性。
服务隔离应该怎么做？
那在实际项目中，一般通过什么方法去做服务隔离呢？主要有以下两种：
按服务/功能做隔离
按用户分类隔离

服务隔离的注意事项
在做服务隔离的时候，还是有一些原则和事项需要注意的：
不可越界：能在隔离模块内完成的逻辑，就尽量不要跨模块调用，减少依赖。
不可共享：数据和资源能独享的就尽量不要共享，不然很容易造成隔离失效。
考虑效率：设计隔离模块的时候，要根据业务情况而定，充分的考虑到未来的拓补结构，减少调用效率的损失。
考虑颗粒度：隔离模块设计的大小问题，过大和过小都不合适，需充分考虑。
服务的全面监控：既然服务或用户进行隔离了，那么系统的复杂度肯定是比之前要高了，那么针对多服务的全链路监控是必不可少的。

服务限流：

服务限流的注意事项
我们在做服务限流的时候，还是有一些原则和事项需要注意的：

实时监控：系统必须要做好全链路的实时监控，才能保证限流的及时检测和处理。
手动开关：除系统自动限流以外，还需要有能手动控制的开关，以保证随时都可以人工介入。
限流的性能：限流的功能理论上是会在一定程度影响到业务正常性能的，因此需要做到限流的性能优化和控制。



服务熔断：



业务逻辑层篇
查看《工程设计》章节

数据存储层篇
一般来说某个系统内部如果出现跨多个库的这么一个操作，是不合规的。现在微服务，一个大的系统分成几百个服务，几十个服务。一般来说，要求每个服务只能操作自己对应的一个数据库。如果要操作别的服务对应的库，不允许直连别的服务的库，违反微服务架构的规范。

 

分布式缓存篇
查看《分布式缓存》章节

性能评估&扩容篇

软件质量保证篇

监控篇
https://mp.weixin.qq.com/s/Gs-AlLJpDGXXjRpZgzfolg

安全篇

基于堆栈分析篇

高性能数据提交与存储篇





-->
