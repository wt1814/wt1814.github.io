

<!-- TOC -->

- [1. 分区保持顺序](#1-分区保持顺序)
    - [1.1. 前言](#11-前言)
    - [1.2. “严格的顺序消费”有多么困难](#12-严格的顺序消费有多么困难)
    - [1.3. 顺序消费](#13-顺序消费)
        - [1.3.1. 全局有序](#131-全局有序)
        - [1.3.2. 局部有序](#132-局部有序)
        - [1.3.3. 注意事项](#133-注意事项)
            - [1.3.3.1. 消息重试对顺序消息的影响](#1331-消息重试对顺序消息的影响)
            - [1.3.3.2. 消息producer发送逻辑的控制](#1332-消息producer发送逻辑的控制)
    - [1.4. 小结](#14-小结)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. Apache Kafka官方保证了partition内部的数据有效性（追加写、offset读）。为了提高Topic的并发吞吐能力，可以提高Topic的partition数，并通过设置partition的replica来保证数据高可靠。但是在多个Partition时，不能保证Topic级别的数据有序性。  
2. 顺序消费
    1. 前提：[消息分区](/docs/microService/mq/kafka/topic.md)  
    2. 针对消息有序的业务需求，还分为全局有序和局部有序：  
        * 全局有序：一个Topic下的所有消息都需要按照生产顺序消费。
        * 局部有序：一个Topic下的消息，只需要满足同一业务字段的要按照生产顺序消费。例如：Topic消息是订单的流水表，包含订单orderId，业务要求同一个orderId的消息需要按照生产顺序进行消费。
3. 全局有序：  
&emsp; 一个生产者、一个分区、一个消费者（或使用分布式锁），并严格到一个消费线程。   
4. 局部有序：    
&emsp; 多分区时，要满足局部有序，只需要在发消息的时候指定Partition Key，Kafka对其进行Hash计算，根据计算结果决定放入哪个Partition。这样Partition Key相同的消息会放在同一个Partition。此时，Partition的数量仍然可以设置多个，提升Topic的整体吞吐量。   
5. ~~注意事项：~~  
    1. 消息重试对顺序消息，无影响。   

# 1. 分区保持顺序
<!-- 
https://cloud.tencent.com/developer/article/1839597

https://www.cnblogs.com/sddai/p/11340870.html
*** https://sa.sogou.com/sgsearch/sgs_tc_news.php?req=nxiYB_RqrtR_RCBzn7Mur64FYacCUx0frbgmBhIDEUQ=&user_type=1

-->
<!-- 
~~
https://cloud.tencent.com/developer/article/1682566

https://objcoding.com/2020/05/01/mq-sequential-consumption/
-->
## 1.1. 前言
&emsp; Apache Kafka官方保证了partition内部的数据有效性（追加写、offset读）。为了提高Topic的并发吞吐能力，可以提高Topic的partition数，并通过设置partition的replica来保证数据高可靠。但是在多个Partition时，不能保证Topic级别的数据有序性。  

<!-- 
&emsp; 如果将Topic设置成单分区，该Topic的所有的消息都只在一个分区内读写，保证全局的顺序性，但将丧失Kafka多分区带来的高吞吐量和负载均衡的性能优势。  
&emsp; 多分区消息保序的方法是按消息键保序策略，根据业务提取出需要保序的消息的逻辑主体，并建立消息标志位ID，对标志位设定专门的分区策略，保证同一标志位的所有消息都发送到同一分区，既可以保证分区内的消息顺序，也可以享受到多分区带来的搞吞吐量。  
&emsp; 说明：消息重试只是简单将消息重新发送到原来的分区，不会重新选择分区。  
-->

## 1.2. “严格的顺序消费”有多么困难
<!-- 
https://cloud.tencent.com/developer/article/1682566
-->
&emsp; 下面就从3个方面来分析一下，对于一个消息中间件来说，”严格的顺序消费”有多么困难，或者说不可能。  

&emsp; **发送端**  
&emsp; 发送端不能异步发送，异步发送在发送失败的情况下，就没办法保证消息顺序。  
&emsp; 比如你连续发了1，2，3。 过了一会，返回结果1失败，2, 3成功。你把1再重新发送1遍，这个时候顺序就乱掉了。  

&emsp; **存储端**  
&emsp; 对于存储端，要保证消息顺序，会有以下几个问题：  
&emsp; （1）消息不能分区。也就是1个topic，只能有1个队列。在Kafka中，它叫做partition；在RocketMQ中，它叫做queue。 如果你有多个队列，那同1个topic的消息，会分散到多个分区里面，自然不能保证顺序。  
&emsp; （2）即使只有1个队列的情况下，会有第2个问题。该机器挂了之后，能否切换到其他机器？也就是高可用问题。  
&emsp; 比如你当前的机器挂了，上面还有消息没有消费完。此时切换到其他机器，可用性保证了。但消息顺序就乱掉了。  
&emsp; 要想保证，一方面要同步复制，不能异步复制；另1方面得保证，切机器之前，挂掉的机器上面，所有消息必须消费完了，不能有残留。很明显，这个很难！！！  

&emsp; **接收端**  
&emsp; 对于接收端，不能并行消费，也即不能开多线程或者多个客户端消费同1个队列。  

&emsp; **总结**  
&emsp; 从上面的分析可以看出，要保证消息的严格有序，有多么困难！  
&emsp; 发送端和接收端的问题，还好解决一点，限制异步发送，限制并行消费。但对于存储端，机器挂了之后，切换的问题，就很难解决了。  
&emsp; 切换了，可能消息就会乱；不切换，那就暂时不可用。这2者之间，就需要权衡了。  

## 1.3. 顺序消费
&emsp; 前提：[消息分区](/docs/microService/mq/kafka/topic.md)    
&emsp; ~~增加分区时，Partition内的消息不会重新进行分配，随着数据继续写入，新分区才会参与再平衡。~~  
<!-- 
&emsp; kafka只能保证分区内有序，无法保证分区间有序，所以消费时，数据是相对有序的。

&emsp; 消息路由策略

&emsp; 在通过API方式发布消息时，生产者是以Record为消息进行发布的。Record中包含key与value，value才是消息本身，而key用于路由消息所要存放Partition。消息要写入到哪个Partition并不是随机的，而是由路由策略决定。  

* 指定Partition，直接写入指定Partition。
* 没有指定Partition但指定了key，则通过对key的hash值与Partition数量取模，结果就是要选出的Partition索引。
* Partition和key都未指定，则使用轮询算法选出一个Partition。

&emsp; 增加分区时，Partition内的消息不会重新进行分配，随着数据继续写入，新分区才会参与再平衡。  

&emsp; 消息生产过程

&emsp; Producer先通过分区策略确定数据录入的partition，再从Zookeeper中找到Partition的Leader  
&emsp; Producer将消息发送给分区的Leader。  
&emsp; Leader将消息接入本地的Log，并通知ISR（In-sync Replicas，副本同步列表）的Followers。  
&emsp; ISR中的Followers从Leader中pull消息，写入本地Log后向Leader发送ACK（消息发送确认机制）。  
&emsp; Leader收到所有ISR中的Followers的ACK后，增加HW（high watermark，最后commit 的offset）并向Producer发送ACK，表示消息写入成功。  
-->
&emsp; 针对消息有序的业务需求，还分为全局有序和局部有序：  

* 全局有序：一个Topic下的所有消息都需要按照生产顺序消费。
* 局部有序：一个Topic下的消息，只需要满足同一业务字段的要按照生产顺序消费。例如：Topic消息是订单的流水表，包含订单orderId，业务要求同一个orderId的消息需要按照生产顺序进行消费。


### 1.3.1. 全局有序
<!-- 
https://cloud.tencent.com/developer/article/1839597
https://www.cnblogs.com/sddai/p/11340870.html
-->

&emsp; 全局顺序就目前的应用范围来讲，可以列举出来的也就限于binlog日志传输，如mysql binlog日志传输要求全局的顺序，不能有任何的乱序。这种的解决办法通常是最为保守的方式：  

1. 全局使用一个生产者  
2. 全局使用一个消费者（并严格到一个消费线程）  
3. 全局使用一个分区（当然不同的表可以使用不同的分区或者topic实现隔离与扩展）  

-------------

由于Kafka的一个Topic可以分为了多个Partition，Producer发送消息的时候，是分散在不同 Partition的。当Producer按顺序发消息给Broker，但进入Kafka之后，这些消息就不一定进到哪个Partition，会导致顺序是乱的。  

因此要满足全局有序，需要1个Topic只能对应1个Partition。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-126.png)  
而且对应的consumer也要使用单线程或者保证消费顺序的线程模型，否则会出现下图所示，消费端造成的消费乱序。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-127.png)  


### 1.3.2. 局部有序
其实在大部分业务场景下，只需要保证消息局部有序即可，什么是局部有序？局部有序是指在某个业务功能场景下保证消息的发送和接收顺序是一致的。如：订单场景，要求订单的创建、付款、发货、收货、完成消息在同一订单下是有序发生的，即消费者在接收消息时需要保证在接收到订单发货前一定收到了订单创建和付款消息。  

针对这种场景的处理思路是：针对部分消息有序（message.key相同的message要保证消费顺序）场景，可以在producer往kafka插入数据时控制，同一key分发到同一partition上面。因为每个partition是固定分配给某个消费者线程进行消费的，所以对于在同一个分区的消息来说，是严格有序的（在kafka 0.10.x以前的版本中，kafka因消费者重启或者宕机可能会导致分区的重新分配消费，可能会导致乱序的发生，0.10.x版本进行了优化，减少重新分配的可能性）。  

------------

要满足局部有序，只需要在发消息的时候指定Partition Key，Kafka对其进行Hash计算，根据计算结果决定放入哪个Partition。这样Partition Key相同的消息会放在同一个Partition。此时，Partition的数量仍然可以设置多个，提升Topic的整体吞吐量。  
如下图所示，在不增加partition数量的情况下想提高消费速度，可以考虑再次hash唯一标识（例如订单orderId）到不同的线程上，多个消费者线程并发处理消息（依旧可以保证局部有序）。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-128.png)  



### 1.3.3. 注意事项
<!-- 
https://cloud.tencent.com/developer/article/1839597
https://www.cnblogs.com/sddai/p/11340870.html
-->

#### 1.3.3.1. 消息重试对顺序消息的影响

对于一个有着先后顺序的消息A、B，正常情况下应该是A先发送完成后再发送B，但是在异常情况下，在A发送失败的情况下，B发送成功，而A由于重试机制在B发送完成之后重试发送成功了。这时对于本身顺序为AB的消息顺序变成了BA。  

针对这种问题，严格的顺序消费还需要max.in.flight.requests.per.connection参数的支持。  

该参数指定了生产者在收到服务器响应之前可以发送多少个消息。它的值越高，就会占用越多的内存，同时也会提升吞吐量。把它设为1就可以保证消息是按照发送的顺序写入服务器的。  

此外，对于某些业务场景，设置max.in.flight.requests.per.connection=1会严重降低吞吐量，如果放弃使用这种同步重试机制，则可以考虑在消费端增加失败标记的记录，然后用定时任务轮询去重试这些失败的消息并做好监控报警。  

-------------

消息重试对顺序消息的影响  

对于一个有着先后顺序的消息A、B，正常情况下应该是A先发送完成后再发送B，但是在异常情况下，在A发送失败的情况下，B发送成功，而A由于重试机制在B发送完成之后重试发送成功了。
这时对于本身顺序为AB的消息顺序变成了BA  

#### 1.3.3.2. 消息producer发送逻辑的控制

消息producer在发送消息的时候，对于同一个broker连接是存在多个未确认的消息在同时发送的，也就是存在上面场景说到的情况，虽然A和B消息是顺序的，但是由于存在未知的确认关系，有可能存在A发送失败，B发送成功，A需要重试的时候顺序关系就变成了BA。简之一句就是在发送B时A的发送状态是未知的。  
针对以上的问题，严格的顺序消费还需要以下参数支持：max.in.flight.requests.per.connection
这个参数官方文档的解释是：  

    The maximum number of unacknowledged requests the client will send on a single connection before blocking. Note that if this setting is set to be greater than 1 and there are failed sends, there is a risk of message re-ordering due to retries (i.e., if retries are enabled).

大体意思是：

    在发送阻塞前对于每个连接，正在发送但是发送状态未知的最大消息数量。如果设置大于1，那么就有可能存在有发送失败的情况下，因为重试发送导致的消息乱序问题。
    所以我们应该将其设置为1，保证在后一条消息发送前，前一条的消息状态已经是可知的。



## 1.4. 小结  

&emsp; Kafka无法做到消息全局有序，只能做到Partition维度的有序。所以如果想要消息有序，就需要从Partition维度入手。一般有两种解决方案：

* 单Partition，单Consumer。通过此种方案强制消息全部写入同一个Partition内，但是同时也牺牲掉了Kafka高吞吐的特性了，所以一般不会采用此方案。  
* **多Partition，多Consumer，`指定key使用特定的Hash策略，使其消息落入指定的Partition 中`，`从而保证相同的key对应的消息是有序的`。** 此方案也是有一些弊端，比如当Partition个数发生变化时，相同的key对应的消息会落入到其他的Partition上，所以一旦确定Partition个数后就不能再修改Partition个数了。  


