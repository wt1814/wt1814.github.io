

# 秒杀系统设计  


## 什么是秒杀系统  


<!-- 

qps飘高  
我是如何将系统QPS从300提升到6000的
https://blog.csdn.net/u012562943/article/details/100879341


高并发架构系列：什么是流量削峰？如何解决秒杀业务的削峰场景
https://blog.csdn.net/m0_37125796/article/details/88833419

-->


## 秒杀系统设计  


<!-- 
 
秒杀系统的架构分析与实战
https://mp.weixin.qq.com/s/CUTG32SaLST9nmhBP1PgkA
经验：一个秒杀系统的设计思考
https://mp.weixin.qq.com/s/cyR59SLxOqpC5my8vl8VnQ
秒杀架构模型设计，怎么搞？ 
https://mp.weixin.qq.com/s/ZMoLa1dlgDEPG7kAqS79tg
这一次，彻底弄懂“秒杀系统” 
https://mp.weixin.qq.com/s?__biz=MjM5ODI5Njc2MA==&mid=2655826732&idx=1&sn=2ff974a229fa8276646e4343990e8556&chksm=bd74fefb8a0377ed814522413f5a5a4cd98272f2e1a7f09dabe25d1797c5b406d12d91eecd87&mpshare=1&scene=1&srcid=&sharer_sharetime=1568201541445&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=2a4ff15fdd84634657a339ecc4fdb0102ba0a10162fa7605c55832121ed738b7f126631b73f50506cd4b8837d56d64dcca233432973943b22a4658512020508fc3611c27ffb677dd0c1b272049e052d2&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=itx1gApiSjQ3hWB5NxczIuCswqlR4CHjqy8rNSbMiIlPrLAnYQ1%2BCdb6ALXoRgGH
《吊打面试官》系列-秒杀系统设计
https://juejin.im/post/5dd09f5af265da0be72aacbd
实战 Spring Cloud 微服务架构下的“秒杀”（含代码） 
https://mp.weixin.qq.com/s?__biz=MzI4ODQ3NjE2OA==&mid=2247485875&idx=1&sn=0ff0a0c4ea9c5a36334d80de83f1084c&chksm=ec3c94d4db4b1dc29283aae847140827bf8db0a8d4113a5a8f6f15d1f9daf2207ce8f36221c8&mpshare=1&scene=1&srcid=&sharer_sharetime=1574610504784&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=0414aa86a61cc65d5075224c9bbe07dc0ec18127df5a5dec0b896c957b5c02e89ace501c4a5805f1846578d33bb68bc07855abfe1d7425e5bf5ee862303da6da1ac182a521552200e8715143232cc369&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=NUAXVXOtx23t%2B2qP0pIU2igFgFZyp%2BSpoLm3b%2FFpPXb%2FFprtG9Q72sqp35P17oGU
使用 Redis 搭建电商秒杀系统
https://mp.weixin.qq.com/s/qgGS7ODqdQIHFtKnVlvIDQ
秒杀系统设计～亿级用户
https://mp.weixin.qq.com/s/tdpht9QeGZlqYOu2vidHLg
 进阶：秒杀系统是如何设计的？ 
https://mp.weixin.qq.com/s/4jYEvq7tIlbLOLGvXI4prg
秒杀系统设计的 5 个要点：前端三板斧＋后端两条路！ 
https://mp.weixin.qq.com/s/8TWZG0rkTuvBqw3abPDE2w
还不知道【秒杀系统】如何设计？ 
https://mp.weixin.qq.com/s/CYa_YGVGCnozus2K9UTfNA

-->



