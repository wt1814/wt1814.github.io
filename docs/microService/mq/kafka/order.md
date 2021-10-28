

<!-- TOC -->

- [1. 分区后保持顺序](#1-分区后保持顺序)
    - [1.1. 存储消息保序](#11-存储消息保序)
    - [1.2. 消费消息保序](#12-消费消息保序)

<!-- /TOC -->


# 1. 分区后保持顺序
<!-- 

https://cloud.tencent.com/developer/article/1682566

https://www.cnblogs.com/sddai/p/11340870.html
https://cloud.tencent.com/developer/article/1839597

https://objcoding.com/2020/05/01/mq-sequential-consumption/

https://sa.sogou.com/sgsearch/sgs_tc_news.php?req=nxiYB_RqrtR_RCBzn7Mur64FYacCUx0frbgmBhIDEUQ=&user_type=1

-->

&emsp; Kafka无法做到消息全局有序，只能做到Partition维度的有序。所以如果想要消息有序，就需要从Partition维度入手。一般有两种解决方案：

* 单Partition，单Consumer。通过此种方案强制消息全部写入同一个Partition内，但是同时也牺牲掉了Kafka高吞吐的特性了，所以一般不会采用此方案。  
* **多Partition，多Consumer，指定key使用特定的Hash策略，使其消息落入指定的Partition 中，从而保证相同的key对应的消息是有序的。** 此方案也是有一些弊端，比如当Partition个数发生变化时，相同的key对应的消息会落入到其他的Partition上，所以一旦确定Partition个数后就不能再修改Partition个数了。  




## 1.1. 存储消息保序

    如果将Topic设置成单分区，该Topic的所有的消息都只在一个分区内读写，保证全局的顺序性，但将丧失Kafka多分区带来的高吞吐量和负载均衡的性能优势。
    多分区消息保序的方法是按消息键保序策略，根据业务提取出需要保序的消息的逻辑主体，并建立消息标志位ID，，对标志位设定专门的分区策略，保证同一标志位的所有消息都发送到同一分区，既可以保证分区内的消息顺序，也可以享受到多分区带来的搞吞吐量。
    说明：消息重试只是简单将消息重新发送到原来的分区，不会重新选择分区。

## 1.2. 消费消息保序

    kafka只能保证分区内有序，无法保证分区间有序，所以消费时，数据是相对有序的。

消息路由策略

    在通过API方式发布消息时，生产者是以Record为消息进行发布的。Record中包含key与value，value才是消息本身，而key用于路由消息所要存放Partition。消息要写入到哪个Partition并不是随机的，而是由路由策略决定。
        指定Partition，直接写入指定Partition。
        没有指定Partition但指定了key，则通过对key的hash值与Partition数量取模，结果就是要选出的Partition索引。
        Partition和key都未指定，则使用轮询算法选出一个Partition。
    增加分区时，Partition内的消息不会重新进行分配，随着数据继续写入，新分区才会参与再平衡。

消息生产过程

    Producer先通过分区策略确定数据录入的partition，再从Zookeeper中找到Partition的Leader
    Producer将消息发送给分区的Leader。
    Leader将消息接入本地的Log，并通知ISR（In-sync Replicas，副本同步列表）的Followers。
    ISR中的Followers从Leader中pull消息，写入本地Log后向Leader发送ACK（消息发送确认机制）。
    Leader收到所有ISR中的Followers的ACK后，增加HW（high watermark，最后commit 的offset）并向Producer发送ACK，表示消息写入成功。



