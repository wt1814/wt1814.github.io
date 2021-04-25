
<!-- TOC -->

- [1. 分布式事务](#1-分布式事务)
    - [1.1. 分布式事务产生的原因](#11-分布式事务产生的原因)
    - [1.2. 分布式事务解决方案](#12-分布式事务解决方案)
    - [1.3. 分布式事务具体实现](#13-分布式事务具体实现)
        - [1.3.1. JTA](#131-jta)
        - [1.3.2. TCC框架](#132-tcc框架)
        - [1.3.3. LCN分布式事务框架](#133-lcn分布式事务框架)
        - [1.3.4. Seata](#134-seata)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-33.png)  




# 1. 分布式事务  
<!--
如何选择分布式事务解决方案？
https://mp.weixin.qq.com/s/2AL3uJ5BG2X3Y2Vxg0XqnQ
聊一下分布式事务
https://mp.weixin.qq.com/s/2cmZTDTtFDBogxlUFKtpLQ

分布式事务解决方案
https://mp.weixin.qq.com/s/mSxUXHgcHo5a7VAhvjIpCQ
微服务架构下分布式事务解决方案 
https://mp.weixin.qq.com/s/T4oGKrOXONl2AuhhKtd_9g
分布式事务场景  
https://blog.csdn.net/lyb9292/article/details/106526156
-->

<!--
～～～～～～～～～～～～
分布式事务 
https://mp.weixin.qq.com/s/XknegP66mnYboiBx556Kzw

https://mp.weixin.qq.com/s?__biz=MzI5ODQ2MzI3NQ==&mid=2247487531&idx=1&sn=b3fbc4dee7cea4a78db062a4a656afdf&chksm=eca4296fdbd3a079a8e328ec7946ced7d1f94c0f105463743a8bee569bae6da00bf2133c3e1a&mpshare=1&scene=1&srcid=&sharer_sharetime=1564202929646&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=ecc4386bb884a7b134f7967009b30d8850e84095233bdb465a9d85c893c9d20f24ac5d5c020310846ccee37aa2e8173504c6cfc1df58512d821d0e4576cf5551069f7159d6583c1ffafa2c3922d85c13&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060834&lang=zh_CN&pass_ticket=FpawTdCfFbNulIqKIET55TinFCVk8qXp4EKE58T1l6zm9idpTXvh4%2BicV3hbPZAB
~~
-->

## 1.1. 分布式事务产生的原因  
* 数据库分库分表：如果一个操作既访问01库，又访问02库，而且要保证数据的一致性，那么就要用到分布式事务。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-1.png)  
* 应用SOA化：所谓的SOA化，就是业务的服务化。将一个整体的系统拆分为多个子系统，每个子系统都有自己的数据库，为了保证数据一致性，就需要用到分布式事务。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-2.png)  


## 1.2. 分布式事务解决方案  

* [DTP及XA](/docs/microService/thinking/DTPAndXA.md)     
    * 一致性协议-XA两阶段提交
    * 一致性协议-XA三阶段提交
* [TCC模式](/docs/microService/thinking/TCC.md)-强一致性
* [事件溯源模式(Event Sourcing)](/docs/microService/thinking/news.md)，Saga事务模型-最终一致性 
* [消息驱动模式(Message Driven)](/docs/microService/thinking/news.md) 
    * 本地消息表（异步确保）-强一致性
    * 事务消息-最终一致性
    * 最大努力通知-最终一致性

## 1.3. 分布式事务具体实现  
### 1.3.1. JTA  

### 1.3.2. TCC框架  
&emsp; ByteTCC、TCC-transaction、EasyTransaction  

### 1.3.3. LCN分布式事务框架  
<!-- 
https://mp.weixin.qq.com/s/xe0M5GsmtNWbWEygIFFfig
-->
&emsp; ......

### 1.3.4. Seata  
<!-- 
https://seata.io/zh-cn/docs/overview/what-is-seata.html
两天，我把分布式事务搞完了 
https://mp.weixin.qq.com/s/amuBimPo7lnfsfo5Pyzc-w
-->
&emsp; Seata一个框架集成了AT、TCC、Saga、XA四种模式。  
