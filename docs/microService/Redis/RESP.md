


# Redis协议


<!--
《Redis深度历险》 2.2章


-->

&emsp; 我们用过很多redis的客户端，有没有相过自己写一个redis客户端？ 其实很简单，基于socket，监听6379端口，解析数据就可以了。解析数据的过程主要依赖于redis的协议了。   


## 手写一个简单版的redis客户端  
<!-- 
redis通讯协议(RESP )是什么
https://juejin.cn/post/6844903955235864589

https://cloud.tencent.com/developer/article/1403344
-->

## Redis 的通信协议 RESP  
<!-- 
http://www.jwsblog.com/archives/74.html
-->
&emsp; Redis作者认为数据库系统瓶颈不在网络流量, 而在数据库自身逻辑处理上, 所以使用了浪费流量的文本协议, 来换取即可的访问性能。  
