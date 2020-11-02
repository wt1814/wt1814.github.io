

<!-- 
从面试角度来看一看 Kafka 
https://mp.weixin.qq.com/s/kguKr_k-BrcQz4G5gag8gg
Kafka 面试必问：聊聊 acks 参数对消息持久化的影响！ 
https://mp.weixin.qq.com/s/PePsJzuKEIfQpCH1KbxrCg

http://blog.51cto.com/littledevil

Kafka基本原理 
https://mp.weixin.qq.com/s?__biz=MzI3MjUxNzkxMw==&mid=2247484070&idx=1&sn=640f99a7d918ba47baea8f0358503ade&chksm=eb301cd0dc4795c6756b0d825e579208a4a2018335ddb927aa28763ff6bb3a5740aaaa86aef5&mpshare=1&scene=1&srcid=&sharer_sharetime=1569341085780&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=2a4ff15fdd84634661f14e77d0fed49aff1e1a252cc01c8b57354637cdb78093b41e57707bef951c7687776cda9d4ba2f25d2d80585a3877d0897dec4b6d3924126a672d8fa881f3f59dd6f03391d67a&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=%2BeyejsSYMk60ZUY%2FpK4YBKveSPIi8GUzrQvyveBksIipCvi8KhPkraBA4Eyx2jY%2F


https://mp.weixin.qq.com/s?__biz=MjM5ODI5Njc2MA==&mid=2655826006&idx=1&sn=40260eb24d57c7dc651e864e2c1bc522&chksm=bd74fd818a037497fc7630776314f7b3b7ae34d93ec3390ed94eabb691978175d56d2c2581b5&mpshare=1&scene=1&srcid=&sharer_sharetime=1565613484904&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=ecc4386bb884a7b11f67eb046d1cfb51287e0b646d7dd5b2a83d57aff04863855b0c470a00d68836a0037bb73afb0da3faead154742a453e19c3bc9f3761889c6a136ee40c2cd73673f9301952f69a42&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=ZNpwaCm7lig8GxObuYnsLOy5YLlTGrfL0TUgNX%2BGTylF4nuPDQtCUafUJSn7768P

 kafka概念扫盲
https://mp.weixin.qq.com/s/nSa2CPjbMFdOsYB2Dt0kYg
 Kafka基本架构及原理
https://mp.weixin.qq.com/s/OB-ZVy70vHClCtep43gr_A

 Java人应该知道的SpringBoot For Kafka (上) 
https://mp.weixin.qq.com/s/2U2jSgA95-D0_N4HxwOnWA
 Java人应该知道的SpringBoot For Kafka (下) 
https://mp.weixin.qq.com/s/JB660Pgypr-PvkkdGOlhag


 一文讲清 Kafka 工作流程和存储机制 
https://mp.weixin.qq.com/s/ITLN-DHxYc5w6qrlFD8HWQ




全网最通俗易懂的 Kafka 入门
https://mp.weixin.qq.com/s?__biz=Mzg2MjEwMjI1Mg==&mid=2247490770&idx=2&sn=1008bcdaed680ed1413e2ead6320bec0&chksm=ce0dab51f97a224771a468245ed4f99f338a51a97505f2e78790cc8a6360f4f22c5e4f07cca2&mpshare=1&scene=1&srcid=&sharer_sharetime=1575464964037&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=e2a6a5ccea4b8ce41e290743b191d123754ef664941f31b9abdbdf28c289f875664f750548bc9da8bbbbabbeaa6a6d5fbb9efc00d2f33e693de36420dd87f9348fb89d058eb4d5ccbcfd806790431b8e&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=AQTbt4i1KPDzS6vieYS4x5I%3D&pass_ticket=UIzvXMBOSWKDgIz4M7cQoxQ548Mbvo9Oik9jB6kaYK60loRzg3FsHZUpAHYbC4%2By



Kafka 基本原理（8000 字小结） 
https://mp.weixin.qq.com/s?__biz=MzI5MzYzMDAwNw==&mid=2247487577&idx=1&sn=606b2899af6a7dcee0b4f300d4b2d687&chksm=ec6e6901db19e0173de830526e65c1961021edde2796ec71a4df8fff572343fbed63abb3234b&mpshare=1&scene=1&srcid=&sharer_sharetime=1574207521875&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=0fd7b4fa2fb2f076f8be28496819994efb11697d02a5a8cfe02eb4e2e6de8c8e7a41ac7edaacfa6b76a5bb560aaf552eb9084e00190995da861de152d8a80b66145866dcce98ab54339a06974524e8bc&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=WfLUVSCdR759nVfSaPrEbDJ5pQBJLzYUdmc8DOJ9hHJHHUoxvw5vHgy0hzLZZoMW

-->

<!-- 
草捏子
Kafka中副本机制的设计和原理 
https://mp.weixin.qq.com/s/yIPIABpAzaHJvGoJ6pv0kg
Kafka 消费者的使用和原理 
https://mp.weixin.qq.com/s/cmDRWi2tmw0reHoUf5UriQ

Kafka中的再均衡 
https://mp.weixin.qq.com/s/UiSpj3WctvdcdXXAwjcI-Q

-->


<!-- 

小赵

 Kafka系列第7篇：你必须要知道集群内部工作原理的一些事！
https://mp.weixin.qq.com/s/5uTiunLJZvNqly6xdMjbzw

-->



## 基本概念

<!-- 

实战1.4章
kafka源码解析 第2章
https://mp.weixin.qq.com/s?__biz=MzUzMzQ2MDIyMA==&mid=2247484112&idx=2&sn=1d95e4c272a5624d3579a625c87a4df9&chksm=faa2e743cdd56e556b6efa4aa4f63ee715217feca0e93c712ebd465bef06e532278adbef4f3e&mpshare=1&scene=1&srcid=&sharer_sharetime=1569340211913&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=96490f73691e56d5c0353e13c3e42a73489ef38851702f058571bedb4bcec99278bdd2e860d2497ad5a18183c832e82d940e77646c56a89509966ce2799af708db94a174c28d9562bc1b56f425e21a12&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=%2BeyejsSYMk60ZUY%2FpK4YBKveSPIi8GUzrQvyveBksIipCvi8KhPkraBA4Eyx2jY%2F

https://mp.weixin.qq.com/s/0lvmttozjIn9RL6eofQjww
-->




## 使用场景  
<!-- 
1.4章
-->



## 高性能

<!-- 
 师兄大厂面试遇到面试官的 Kafka 暴击三连问，快面哭了！ 
https://mp.weixin.qq.com/s/ejZBAGI7qLE_QYSe-AqipA
-->
