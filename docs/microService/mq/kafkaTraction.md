

# kafka事务  

<!-- 
Kafka Exactly Once 语义与事务机制原理
https://blog.csdn.net/BeiisBei/article/details/104737298


https://www.cnblogs.com/wangzhuxing/p/10125437.html
https://www.cnblogs.com/middleware/p/9477133.html
-->
Apache Kafka 从 0.11.0 开始，支持了一个非常大的 feature，就是对事务性的支持，在 Kafka 中关于事务性，是有三种层面上的含义：一是幂等性的支持；二是事务性的支持；三是 Kafka Streams 的 exactly once 的实现。  

&emsp; **消息交付语义**    
&emsp; Kafka 在 producer 和 consumer 之间提供的语义保证。显然，Kafka可以提供的消息交付语义保证有多种：  

* At most once——消息可能会丢失但绝不重传。
* At least once——消息可以重传但绝不丢失。
* Exactly once——这正是人们想要的, 每一条消息只被传递一次。

&emsp; 值得注意的是，这个问题被分成了两部分：发布消息的持久性保证和消费消息的保证。  


## 幂等性，Exactly Once  
<!-- 
http://matt33.com/2018/10/24/kafka-idempotent/

https://mp.weixin.qq.com/s?__biz=MzIzNTIzNzYyNw==&mid=2247484078&idx=1&sn=3b0e754674804d61bde1df498f0bd092&chksm=e8eb7b3adf9cf22c370346b0eb2aa516f517daf4d0fa5977f41412d64f22cfe86f8eec90c58c&scene=178&cur_album_id=1338677566013800448#rd


**精准一次（幂等性），保证数据不重复**  
Ack设置为-1，则可以保证数据不丢失，但是会出现数据重复（at least once）

Ack设置为0，则可以保证数据不重复，但是不能保证数据不丢失（at most once）

但是如果鱼和熊掌兼得，该怎么办？这个时候就就引入了Exactl once（精准一次）

 

在0.11版本后，引入幂等性解决kakfa集群内部的数据重复，在0.11版本之前，在消费者处自己做处理

如果启用了幂等性，则ack默认就是-1，kafka就会为每个生产者分配一个pid，并未每条消息分配seqnumber，如果pid、partition、seqnumber三者一样，则kafka认为是重复数据，就不会落盘保存；但是如果生产者挂掉后，也会出现有数据重复的现象；所以幂等性解决在单次会话的单个分区的数据重复，但是在分区间或者跨会话的是数据重复的是无法解决的
-->

将服务器的 ACK 级别设置为 -1，可以保证 Producer 到 Server 之间不会丢失数据，即 At Least Once 语义。相对的，将服务器 ACK 级别设置为 0，可以保证生产者每条消息只会被发送一次，即 At Most Once语义。  
At Least Once 可以保证数据不丢失，但是不能保证数据不重复。相对的，At Most Once 可以保证数据不重复，但是不能保证数据不丢失。但是，对于一些非常重要的信息，比如说交易数据，下游数据消费者要求数据既不重复也不丢失，即 Exactly Once 语义。在 0.11 版本以前的 Kafka，对此是无能为力的，只能保证数据不丢失，再在下游消费者对数据做全局去重。对于多个下游应用的情况，每个都需要单独做全局去重，这就对性能造成了很大的影响。  
0.11 版本的 Kafka，引入了一项重大特性：幂等性。所谓的幂等性就是指 Producer 不论向 Server 发送多少次重复数据。Server 端都会只持久化一条，幂等性结合 At Least Once 语义，就构成了 Kafka 的 Exactily Once 语义，即：At Least Once + 幂等性 = Exactly Once  
要启用幂等性，只需要将 Producer 的参数中 enable.idompotence 设置为 true 即可。Kafka 的幂等性实现其实就是将原来下游需要做的去重放在了数据上游。开启幂等性的 Producer 在初始化的时候会被分配一个 PID，发往同一 Partition 的消息会附带 Sequence Number。而 Broker 端会对\<PID,Partition,SeqNumber> 做缓存，当具有相同主键的消息提交时，Broker 只会持久化一条。  

    但是 PID 重启就会变化，同时不同的 Partition 也具有不同主键，所以幂等性无法保证跨分区会话的 Exactly Once。


幂等性的实现是事务性实现的基础。  



## 事务性  
<!-- 
http://matt33.com/2018/11/04/kafka-transaction/

https://www.jianshu.com/p/64c93065473e
https://blog.csdn.net/mlljava1111/article/details/81180351
-->

Kafka提供事务主要是为了实现精确一次处理语义(exactly-once semantics, EOS)的，而EOS是实现流处理系统正确性(correctness)的基石，故Kafka事务被大量应用于Kafka Streams之中。  

Kafka 从 0.11 版本开始引入了事务支持。事务可以保证 Kafka 在 Exactly Once 语义的基础上，生产和消费可以跨分区和会话，要么全部成功，要么全部失败。  
**Producer事务**  
为了了实现跨分区跨会话的事务，需要引入一个全局唯一的 TransactionID，并将 Producer 获得的 PID 和Transaction ID 绑定。这样当 Producer 重启后就可以通过正在进行的 TransactionID 获得原来的 PID。  
为了管理 Transaction，Kafka 引入了一个新的组件 Transaction Coordinator。Producer 就是通过和 Transaction Coordinator 交互获得 Transaction ID 对应的任务状态。Transaction Coordinator 还负责将事务所有写入 Kafka 的一个内部 Topic，这样即使整个服务重启，由于事务状态得到保存，进行中的事务状态可以得到恢复，从而继续进行。  

**Consumer事务**  
对 Consumer 而言，事务的保证就会相对较弱，尤其是无法保证 Commit 的消息被准确消费。这是由于Consumer 可以通过 offset 访问任意信息，而且不同的 SegmentFile 生命周期不同，同一事务的消息可能会出现重启后被删除的情况。

