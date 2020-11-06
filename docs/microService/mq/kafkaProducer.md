
# kafka生产者
<!-- 

https://mp.weixin.qq.com/s/OB-ZVy70vHClCtep43gr_A


https://mp.weixin.qq.com/s/ITLN-DHxYc5w6qrlFD8HWQ
-->
&emsp; 在消息发送的过程中，涉及到了 两个线程——main 线程和 Sender 线程，以及一个线程共享变量——RecordAccumulator。 main 线程将消息发送给 RecordAccumulator，Sender 线程不断从 RecordAccumulator 中拉取 消息发送到 Kafka broker。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-7.png)  

Producer 发送消息的过程如下图所示，需要经过拦截器，序列化器和分区器，最终由累加器批量发送至 Broker。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-8.png)  





## kafka如何保证数据可靠性呢？通过ack来保证  

<!-- 
https://mp.weixin.qq.com/s/nSa2CPjbMFdOsYB2Dt0kYg
https://mp.weixin.qq.com/s/ITLN-DHxYc5w6qrlFD8HWQ
-->







