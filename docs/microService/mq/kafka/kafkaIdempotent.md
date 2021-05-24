

<!-- TOC -->

- [1. 幂等性](#1-幂等性)
    - [1.1. 幂等性介绍](#11-幂等性介绍)
    - [1.2. 幂等性实现原理](#12-幂等性实现原理)
        - [1.2.1. ~~幂等性引入之前的问题？~~](#121-幂等性引入之前的问题)
        - [1.2.2. ~~幂等性引入之后解决了什么问题？~~](#122-幂等性引入之后解决了什么问题)
        - [1.2.3. ProducerID是如何生成的？](#123-producerid是如何生成的)
    - [1.3. 幂等性的应用实例](#13-幂等性的应用实例)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. Kafka幂等是针对生产者角度的特性。kafka只保证producer单个会话中的单个分区幂等。  
2. **<font color = "red">Kafka幂等性实现机制：（分区消息维护一个序列号，进行比较）</font>**  
    1. 每一个producer在初始化时会生成一个producer_id，并为每个目标partition维护一个"一个序列号"；
    2. producer每发送一条消息，会将 \<producer_id,分区\> 对应的“序列号”加1；  
    3. broker端会为每一对 \<producer_id,分区\> 维护一个序列号，对于每收到的一条消息，会判断服务端的SN_old和接收到的消息中的SN_new进行对比：  

        * 如果SN_old < SN_new+1，说明是重复写入的数据，直接丢弃。    
        * 如果SN_old > SN_new+1，说明中间有数据尚未写入，或者是发生了乱序，或者是数据丢失，将抛出严重异常：OutOfOrderSeqenceException。 


# 1. 幂等性   
<!-- 
https://blog.csdn.net/BeiisBei/article/details/104737298
-->
&emsp; Kafka的幂等性和事务是比较重要的特性，特别是在数据丢失和数据重复的问题上非常重要。  

## 1.1. 幂等性介绍
&emsp; **幂等又称为exactly once（精确传递一次。消息被处理且只会被处理一次。不丢失不重复就一次）。**Kafka在0.11.0.0之前的版本中只支持At Least Once和At Most Once语义，尚不支持Exactly Once语义。  
&emsp; 但是在很多要求严格的场景下，如使用Kafka处理交易数据，Exactly Once语义是必须的。可以通过让下游系统具有幂等性来配合Kafka的At Least Once语义来间接实现Exactly Once。但是：  

* 该方案要求下游系统支持幂等操作，限制了Kafka的适用场景。
* 实现门槛相对较高，需要用户对Kafka的工作机制非常了解。
* 对于Kafka Stream而言，Kafka本身即是自己的下游系统，但Kafka在0.11.0.0版本之前不具有幂等发送能力。

&emsp; 因此，Kafka本身对Exactly Once语义的支持就非常必要。  
&emsp; ~~**影响Kafka幂等性的因素：**在分布式系统中，一些不可控因素有很多，比如网络、OOM、FullGC等。在Kafka Broker确认Ack时，出现网络异常、FullGC、OOM等问题时导致Ack超时，Producer会进行重复发送。可能出现的情况如下：~~  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-99.png)  
<!-- 
&emsp; 生产者进行retry重试，会重复产生消息。Kafka在0.11版本引入幂等性，brocker只持久化一条。  
&emsp; 幂等性结合At Least Once语义，就构成了Kafka的Exactily Once语义，即：At Least Once + 幂等性 = Exactly Once。  
-->
  
## 1.2. 幂等性实现原理  
&emsp; **<font color = "clime">Kafka幂等是针对生产者角度的特性。幂等可以保证生产者发送的消息，不会丢失，而且不会重复。</font>** **<font color = "red">实现幂等的关键点就是服务端可以区分请求是否重复，过滤掉重复的请求。</font>** 要区分请求是否重复的有两点：  

* 唯一标识：要想区分请求是否重复，请求中就得有唯一标识。例如支付请求中，订单号就是唯一标识。  
* 记录下已处理过的请求标识：光有唯一标识还不够，还需要记录下那些请求是已经处理过的，这样当收到新的请求时，用新请求中的标识和处理记录进行比较，如果处理记录中有相同的标识，说明是重复记录，拒绝掉。  

&emsp; **<font color = "clime">Kafka为了实现幂等性，它在底层设计架构中引入了ProducerID和SequenceNumber：</font>**  

* ProducerID：在每个新的Producer初始化时，会被分配一个唯一的ProducerID，这个ProducerID对客户端使用者是不可见的。  
* SequenceNumber：对于每个ProducerID，Producer发送数据的每个Topic和Partition都对应一个从0开始单调递增的SequenceNumber值。  

&emsp; **<font color = "red">Kafka幂等性实现机制：(分区消息维护一个序列号，进行比较)</font>**  

1. 每一个producer在初始化时会生成一个producer_id，并为每个目标partition维护一个"一个序列号"；
2. producer每发送一条消息，会将 \<producer_id，分区\> 对应的“序列号”加1；  
3. broker端会为每一对 \<producer_id，分区\> 维护一个序列号，对于每收到的一条消息，会判断服务端的SN_old和接收到的消息中的SN_new进行对比：  

    * 如果SN_old < SN_new+1，说明是重复写入的数据，直接丢弃。    
    * 如果SN_old > SN_new+1，说明中间有数据尚未写入，或者是发生了乱序，或者是数据丢失，将抛出严重异常：OutOfOrderSeqenceException。 


&emsp; **<font color = "red">注意：kafka只保证producer单个会话中的单个分区幂等。</font>**    

### 1.2.1. ~~幂等性引入之前的问题？~~  
&emsp; Kafka在引入幂等性之前，Producer向Broker发送消息，然后Broker将消息追加到消息流中后给Producer返回Ack信号值。实现流程如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-100.png)  
&emsp; 上图的实现流程是一种理想状态下的消息发送情况，但是实际情况中，会出现各种不确定的因素，比如在Producer在发送给Broker的时候出现网络异常。比如以下这种异常情况的出现：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-101.png)  
&emsp; 上图这种情况，当Producer第一次发送消息给Broker时，Broker将消息(x2,y2)追加到了消息流中，但是在返回Ack信号给Producer时失败了（比如网络异常） 。此时，Producer端触发重试机制，将消息(x2,y2)重新发送给Broker，Broker接收到消息后，再次将该消息追加到消息流中，然后成功返回Ack信号给Producer。这样下来，消息流中就被重复追加了两条相同的(x2,y2)的消息。  

### 1.2.2. ~~幂等性引入之后解决了什么问题？~~  
&emsp; 面对这样的问题，Kafka引入了幂等性。那么幂等性是如何解决这类重复发送消息的问题的呢？下面先来看看流程图：
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-102.png)  
&emsp; 同样，这是一种理想状态下的发送流程。实际情况下，会有很多不确定的因素，比如Broker在发送Ack信号给Producer时出现网络异常，导致发送失败。异常情况如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-103.png)  
&emsp; 当Producer发送消息(x2,y2)给Broker时，Broker接收到消息并将其追加到消息流中。此时，Broker返回Ack信号给Producer时，发生异常导致Producer接收Ack信号失败。对于Producer来说，会触发重试机制，将消息(x2,y2)再次发送，但是，由于引入了幂等性，在每条消息中附带了PID（ProducerID）和SequenceNumber。相同的PID和SequenceNumber发送给Broker，而之前Broker缓存过之前发送的相同的消息，那么在消息流中的消息就只有一条(x2,y2)，不会出现重复发送的情况。  

### 1.2.3. ProducerID是如何生成的？  
<!-- 
&emsp; 客户端在生成Producer时，会实例化如下代码：

// 实例化一个Producer对象
Producer<String, String> producer = new KafkaProducer<>(props);
-->
&emsp; 在org.apache.kafka.clients.producer.internals.Sender类中，在run()中有一个maybeWaitForPid()方法，用来生成一个ProducerID，实现代码如下：  

```java
private void maybeWaitForPid() {
    if (transactionState == null)
        return;

    while (!transactionState.hasPid()) {
        try {
            Node node = awaitLeastLoadedNodeReady(requestTimeout);
            if (node != null) {
                ClientResponse response = sendAndAwaitInitPidRequest(node);
                if (response.hasResponse() && (response.responseBody() instanceof InitPidResponse)) {
                    InitPidResponse initPidResponse = (InitPidResponse) response.responseBody();
                    transactionState.setPidAndEpoch(initPidResponse.producerId(), initPidResponse.epoch());
                } else {
                    log.error("Received an unexpected response type for an InitPidRequest from {}. " +
                            "We will back off and try again.", node);
                }
            } else {
                log.debug("Could not find an available broker to send InitPidRequest to. " +
                        "We will back off and try again.");
            }
        } catch (Exception e) {
            log.warn("Received an exception while trying to get a pid. Will back off and retry.", e);
        }
        log.trace("Retry InitPidRequest in {}ms.", retryBackoffMs);
        time.sleep(retryBackoffMs);
        metadata.requestUpdate();
    }
}
```

## 1.3. 幂等性的应用实例  
&emsp; **开启幂等性：** 要启用幂等性，只需要将Producer的参数中enable.idompotence设置为true即可。此时会默认把acks设置为all，所以不需要再设置acks属性。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-116.png)  

&emsp; **编码示例：**  

```java
private Producer buildIdempotProducer(){
    // create instance for properties to access producer configs
    Properties props = new Properties();
    // bootstrap.servers是Kafka集群的IP地址。多个时,使用逗号隔开
    props.put("bootstrap.servers", "localhost:9092");
    props.put("enable.idempotence",true);
    //If the request fails, the producer can automatically retry,
    props.put("retries", 3);
    //Reduce the no of requests less than 0
    props.put("linger.ms", 1);
    //The buffer.memory controls the total amount of memory available to the producer for buffering.
    props.put("buffer.memory", 33554432);
    // Kafka消息是以键值对的形式发送,需要设置key和value类型序列化器
    props.put("key.serializer",
            "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer",
            "org.apache.kafka.common.serialization.StringSerializer");
    Producer<String, String> producer = new KafkaProducer<String, String>(props);
    return producer;
}
```

