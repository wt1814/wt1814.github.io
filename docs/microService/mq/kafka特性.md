




# kafka特性


<!-- 
Kafka 面试必问：聊聊 acks 参数对消息持久化的影响！ 
https://mp.weixin.qq.com/s/PePsJzuKEIfQpCH1KbxrCg
-->

<!-- 
https://mp.weixin.qq.com/s/kguKr_k-BrcQz4G5gag8gg
-->

## Kafka的高效读写机制  

<!-- 

https://mp.weixin.qq.com/s/nSa2CPjbMFdOsYB2Dt0kYg
https://mp.weixin.qq.com/s/ejZBAGI7qLE_QYSe-AqipA
如何实现高吞吐量
https://blog.csdn.net/BeiisBei/article/details/104360272

-->

## kafa高可用性

<!-- 

https://mp.weixin.qq.com/s/OB-ZVy70vHClCtep43gr_A
https://mp.weixin.qq.com/s/kguKr_k-BrcQz4G5gag8gg
-->

## kafka可靠性
<!-- 

https://mp.weixin.qq.com/s/nSa2CPjbMFdOsYB2Dt0kYg
https://mp.weixin.qq.com/s/ITLN-DHxYc5w6qrlFD8HWQ

-->


## kafka数据一致性  
<!-- 

https://mp.weixin.qq.com/s/nSa2CPjbMFdOsYB2Dt0kYg
https://mp.weixin.qq.com/s/ejZBAGI7qLE_QYSe-AqipA

-->

Kafka 的消息是否是有序的？  

    Topic 级别无序，Partition 有序  

Kafka 在 Topic 级别本身是无序的，只有 partition 上才有序，所以为了保证处理顺序，可以自定义分区器，将需顺序处理的数据发送到同一个 partition





