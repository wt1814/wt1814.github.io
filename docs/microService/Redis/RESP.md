


# Redis协议
## RESP简介  
<!-- 
http://www.jwsblog.com/archives/74.html
https://blog.csdn.net/mango_love/article/details/106557576

-->
&emsp; Redis的作者认为数据库系统的瓶颈一般不在于网络流量，而是数据库自身内部逻辑处理上。所以即使Redis使用了浪费流量的文本协议，依然可以取得极高的访问性能。Redis将所有数据都放在内存，用一个单线程对外提供服务，单个节点在跑满一个CPU核心的情况下可以达到了10w/s的超高QPS。  
&emsp; Redis作者认为数据库系统瓶颈不在网络流量, 而在数据库自身逻辑处理上, 所以使用了浪费流量的文本协议, 来换取即可的访问性能。  

&emsp; RESP是REdis Serialization Protocol的简称,也就是专门为redis设计的一套序列化协议. 这个协议其实在redis的1.2版本时就已经出现了,但是到了redis2.0才最终成为redis通讯协议的标准  
&emsp; 这个序列化协议听起来很高大上, 但实际上就是一个文本协议.根据官方的说法, 这个协议是基于以下几点(而妥协)设计的:  
1. 实现简单.可以减低客户端出现bug的机率  
2. `解析速度快。`由于RESP能知道返回数据的固定长度,所以不用像json那样扫描整个payload去解析, 所以它的性能是能跟解析二进制数据的性能相媲美的.  
3. 可读性好.  

## RESP详解


## 手写一个简单版的redis客户端  
<!-- 
redis通讯协议(RESP )是什么
https://juejin.cn/post/6844903955235864589

https://cloud.tencent.com/developer/article/1403344
-->

&emsp; 我们用过很多redis的客户端，有没有相过自己写一个redis客户端？ 其实很简单，基于socket，监听6379端口，解析数据就可以了。解析数据的过程主要依赖于redis的协议了。   