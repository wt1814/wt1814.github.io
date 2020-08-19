

# 数据库连接池

## 大小设置  

<!-- 
https://www.cnblogs.com/rickiyang/p/12239907.html
 数据库连接池到底应该设多大？
https://mp.weixin.qq.com/s/UmdgJgsGKQT0J2L6NnfiYA
 别再乱改数据库连接池的大小
https://mp.weixin.qq.com/s?__biz=MzA4NjgxMjQ5Mg==&mid=2665762002&idx=1&sn=1266ebadc04c480daf8f3dbd8a452b7d&chksm=84d21ef1b3a597e72f14142c22c02346416d336b97efa77a9469b8bfc7df8f1e4c7a3e94af28&mpshare=1&scene=1&srcid=&key=2ab8a62e312555a14e02c63e4ce11ef2e7e82d7d18a0a28c31ea9f5a1d208e3142140a7277c1848149819f8d3fe8655fc30bbd4333e0dcf6ee0784a8d643b95761883f07a5761b316ae5b2bab9bca8f1&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060739&lang=zh_CN&pass_ticket=ds1EjNwEBMC5I7yCgScTd0rhXp5zbUIu%2F5Dt6%2BtjWzMDDkLhTdTTznf3w%2FxRZdH%2F
-->
&emsp; 找最合适的连接数大小：连接数 = ((核心数 * 2) + 有效磁盘数)   
&emsp; 按照这个公式，如果服务器CPU是4核i7的，连接池大小应该为 ((4*2)+1)=9。  

&emsp; 结论：<font color = "red">需要的是一个小连接池，和一个等待连接的线程队列。</font>  
&emsp; 假设说有10000个并发访问，仅仅需要一个大小为 10 数据库连接池，然后让剩下的业务线程都在队列里等待就可以了。  
&emsp; 连接池中的连接数量大小应该设置成：数据库能够有效同时进行的查询任务数（通常情况下来说不会高于 2*CPU核心数）。  


## 数据库连接池内存泄漏问题  

<!-- 

线上问题分析系列：数据库连接池内存泄漏问题的分析和解决方案
https://mp.weixin.qq.com/s/N1xZuejPIbE3R3iG-3VbMA
-->

## 几种数据库连接池
### HikariCP  
####  HikariCP常用监控指标与故障排查实战 
<!-- 
https://mp.weixin.qq.com/s?__biz=MzI4NTA1MDEwNg==&mid=2650782637&idx=1&sn=4a0e769b80ceb393ab456717773cc8ee&chksm=f3f90e38c48e872e010a37e269e4d15c4fea90bba2b16ab2f2a5c89fba931c27050a327c56ba&mpshare=1&scene=1&srcid=&sharer_sharetime=1572797187831&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=f394366f6bc7d2c5925fc9be1714c68521356777fbed54568f1a4c8d94875e120004108b37de180f3b6b1f53ecb8e2297ca7cf84bf1c25102b0dcffaff1c0663d6616f88410ddf95105e99cba5d29825&ascene=1&uin=MTE1MTYxNzY2MQ==&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=Lu/LBuTxuGaOTLq0CL9dO0ss3p9k+NlDhrOCgfGfCUsKTPyuc12lccq3vmkXvxfb
-->


## Druid  
...