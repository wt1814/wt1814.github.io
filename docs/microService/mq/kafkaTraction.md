<!-- TOC -->

- [1. kafka事务](#1-kafka事务)
    - [1.1. 幂等性](#11-幂等性)
        - [1.1.1. 消息交付语义](#111-消息交付语义)
        - [1.1.2. 幂等性介绍](#112-幂等性介绍)
        - [1.1.3. 幂等性实现](#113-幂等性实现)
    - [1.2. 事务性](#12-事务性)
        - [1.2.1. kafka事务介绍及使用场景](#121-kafka事务介绍及使用场景)
        - [Kafka事务特性](#kafka事务特性)
            - [原子写](#原子写)
            - [拒绝僵尸实例（Zombie fencing）](#拒绝僵尸实例zombie-fencing)
            - [读事务消息](#读事务消息)
        - [1.2.2. kafka事务使用](#122-kafka事务使用)
            - [1.2.2.1. 事务相关配置](#1221-事务相关配置)
            - [1.2.2.2. Java API](#1222-java-api)
                - [1.2.2.2.1. “只有写”应用程序示例](#12221-只有写应用程序示例)
                - [1.2.2.2.2. 消费-生产并存（consume-Transform-Produce）](#12222-消费-生产并存consume-transform-produce)
        - [1.2.3. kafka事务原理](#123-kafka事务原理)
            - [基本概念](#基本概念)
        - [幂等性和事务性的关系](#幂等性和事务性的关系)
            - [事务流程](#事务流程)
                - [查找事务协调者Tranaction Corordinator](#查找事务协调者tranaction-corordinator)
                - [初始化事务 initTransaction](#初始化事务-inittransaction)
                - [开始事务 beginTransaction](#开始事务-begintransaction)
                - [Consume-Transform-Produce](#consume-transform-produce)
                - [事务提交和事务终结(放弃事务)](#事务提交和事务终结放弃事务)
        - [总结](#总结)

<!-- /TOC -->

# 1. kafka事务  

## 1.1. 幂等性  

### 1.1.1. 消息交付语义    
&emsp; Kafka 在 producer 和 consumer 之间提供的语义保证。显然，Kafka可以提供的消息交付语义保证有多种：  

* At most once——消息可能会丢失但绝不重传。
* At least once——消息可以重传但绝不丢失。
* Exactly once——每一条消息只被传递一次。

&emsp; 值得注意的是，这个问题被分成了两部分：发布消息的持久性保证和消费消息的保证。  

### 1.1.2. 幂等性介绍
&emsp; 将服务器的 ACK 级别设置为 -1，可以保证 Producer 到 Server 之间不会丢失数据，即 At Least Once 语义。相对的，将服务器 ACK 级别设置为 0，可以保证生产者每条消息只会被发送一次，即 At Most Once语义。  
&emsp; At Least Once 可以保证数据不丢失，但是不能保证数据不重复。相对的，At Most Once 可以保证数据不重复，但是不能保证数据不丢失。但是，对于一些非常重要的信息，比如说交易数据，下游数据消费者要求数据既不重复也不丢失，即 Exactly Once 语义。在 0.11 版本以前的 Kafka，对此是无能为力的，只能保证数据不丢失，再在下游消费者对数据做全局去重。对于多个下游应用的情况，每个都需要单独做全局去重，这就对性能造成了很大的影响。  
&emsp; 0.11 版本的 Kafka，引入了一项重大特性：幂等性。所谓的幂等性就是指 Producer 不论向 Server 发送多少次重复数据。Server 端都会只持久化一条，幂等性结合 At Least Once 语义，就构成了 Kafka 的 Exactily Once 语义，即：At Least Once + 幂等性 = Exactly Once  

### 1.1.3. 幂等性实现  
&emsp; **开启幂等性**  
&emsp; 要启用幂等性，只需要将 Producer 的参数中 enable.idompotence 设置为 true 即可。  
&emsp; **幂等性实现原理**  

Kafka 的幂等性实现其实就是将原来下游需要做的去重放在了数据上游。开启幂等性的 Producer 在初始化的时候会被分配一个 PID，发往同一 Partition 的消息会附带 Sequence Number。而 Broker 端会对\<PID,Partition,SeqNumber> 做缓存，当具有相同主键的消息提交时，Broker 只会持久化一条。  

    但是 PID 重启就会变化，同时不同的 Partition 也具有不同主键，所以幂等性无法保证跨分区会话的 Exactly Once。

## 1.2. 事务性  

### 1.2.1. kafka事务介绍及使用场景  
&emsp; Kafka在0.11.0.0之前的版本中只支持At Least Once和At Most Once语义，尚不支持Exactly Once语义。  
&emsp; 但是在很多要求严格的场景下，如使用Kafka处理交易数据，Exactly Once语义是必须的。可以通过让下游系统具有幂等性来配合Kafka的At Least Once语义来间接实现Exactly Once。但是：  

* 该方案要求下游系统支持幂等操作，限制了Kafka的适用场景
* 实现门槛相对较高，需要用户对Kafka的工作机制非常了解
* 对于Kafka Stream而言，Kafka本身即是自己的下游系统，但Kafka在0.11.0.0版本之前不具有幂等发送能力

&emsp; 因此，Kafka本身对Exactly Once语义的支持就非常必要。即实现事务。    

&emsp; **事务场景**  

* 最简单的需求是producer发的多条消息组成一个事务这些消息需要对consumer同时可见或者同时不可见。  
* producer可能会给多个topic，多个partition发消息，这些消息也需要能放在一个事务里面，这就形成了一个典型的分布式事务。  
* kafka的应用场景经常是应用先消费一个topic，然后做处理再发到另一个topic，这个consume-transform-produce过程需要放到一个事务里面，比如在消息处理或者发送的过程中如果失败了，消费位点也不能提交。  
* producer或者producer所在的应用可能会挂掉，新的producer启动以后需要知道怎么处理之前未完成的事务。  
* 流式处理的拓扑可能会比较深，如果下游只有等上游消息事务提交以后才能读到，可能会导致rt非常长吞吐量也随之下降很多，所以需要实现read committed和read uncommitted两种事务隔离级别。

<!-- 
Kafka中的事务特性主要用于以下两种场景：

    生产者发送多条消息可以封装在一个事务中，形成一个原子操作。多条消息要么都发送成功，要么都发送失败。
    read-process-write模式：将消息消费和生产封装在一个事务中，形成一个原子操作。在一个流式处理的应用中，常常一个服务需要从上游接收消息，然后经过处理后送达到下游，这就对应着消息的消费和生成。

    当事务中仅仅存在Consumer消费消息的操作时，它和Consumer手动提交Offset并没有区别。因此单纯的消费消息并不是Kafka引入事务机制的原因，单纯的消费消息也没有必要存在于一个事务中。

-->

### Kafka事务特性  
&emsp; Kafka的事务特性本质上代表了三个功能：原子写操作，拒绝僵尸实例（Zombie fencing）和读事务消息。  

#### 原子写
&emsp; Kafka的事务特性本质上是支持了Kafka跨分区和Topic的原子写操作。在同一个事务中的消息要么同时写入成功，要么同时写入失败。我们知道，Kafka中的Offset信息存储在一个名为_consumed_offsets的Topic中，因此read-process-write模式，除了向目标Topic写入消息，还会向_consumed_offsets中写入已经消费的Offsets数据。因此read-process-write本质上就是跨分区和Topic的原子写操作。Kafka的事务特性就是要确保跨分区的多个写操作的原子性。   

#### 拒绝僵尸实例（Zombie fencing）
&emsp; 在分布式系统中，一个instance的宕机或失联，集群往往会自动启动一个新的实例来代替它的工作。此时若原实例恢复了，那么集群中就产生了两个具有相同职责的实例，此时前一个instance就被称为“僵尸实例（Zombie Instance）”。在Kafka中，两个相同的producer同时处理消息并生产出重复的消息（read-process-write模式），这样就严重违反了Exactly Once Processing的语义。这就是僵尸实例问题。  
&emsp; Kafka事务特性通过transaction-id属性来解决僵尸实例问题。所有具有相同transaction-id的Producer都会被分配相同的pid，同时每一个Producer还会被分配一个递增的epoch。Kafka收到事务提交请求时，如果检查当前事务提交者的epoch不是最新的，那么就会拒绝该Producer的请求。从而达成拒绝僵尸实例的目标。

#### 读事务消息  
&emsp; 为了保证事务特性，Consumer如果设置了isolation.level = read_committed，那么它只会读取已经提交了的消息。在Producer成功提交事务后，Kafka会将所有该事务中的消息的Transaction Marker从uncommitted标记为committed状态，从而所有的Consumer都能够消费。

### 1.2.2. kafka事务使用  
#### 1.2.2.1. 事务相关配置  
1. Broker configs
    1. transactional.id.timeout.ms：
    &emsp; 在ms中，事务协调器在生产者TransactionalId提前过期之前等待的最长时间，并且没有从该生产者TransactionalId接收到任何事务状态更新。默认是604800000(7天)。这允许每周一次的生产者作业维护它们的id
    2. max.transaction.timeout.ms
    &emsp; 事务允许的最大超时。如果客户端请求的事务时间超过此时间，broke将在InitPidRequest中返回InvalidTransactionTimeout错误。这可以防止客户机超时过大，从而导致用户无法从事务中包含的主题读取内容。
    &emsp; 默认值为900000(15分钟)。这是消息事务需要发送的时间的保守上限。
    3. transaction.state.log.replication.factor
    &emsp; 事务状态topic的副本数量。默认值:3
    4. transaction.state.log.num.partitions
    &emsp; 事务状态主题的分区数。默认值:50
    5. transaction.state.log.min.isr
    &emsp; 事务状态主题的每个分区ISR最小数量。默认值:2
    6. transaction.state.log.segment.bytes
    &emsp; 事务状态主题的segment大小。默认值:104857600字节
2. Producer configs
    1. enable.idempotence：开启幂等
    2. transaction.timeout.ms：事务超时时间  
    &emsp; 事务协调器在主动中止正在进行的事务之前等待生产者更新事务状态的最长时间。  
    &emsp; 这个配置值将与InitPidRequest一起发送到事务协调器。如果该值大于max.transaction.timeout。在broke中设置ms时，请求将失败，并出现InvalidTransactionTimeout错误。  
    &emsp; 默认是60000。这使得交易不会阻塞下游消费超过一分钟，这在实时应用程序中通常是允许的。  
    3. transactional.id
    &emsp; 用于事务性交付的TransactionalId。这支持跨多个生产者会话的可靠性语义，因为它允许客户端确保使用相同TransactionalId的事务在启动任何新事务之前已经完成。如果没有提供TransactionalId，则生产者仅限于幂等交付。  
3. Consumer configs
    1. isolation.level
    &emsp; read_uncommitted:以偏移顺序使用已提交和未提交的消息。  
    &emsp; read_committed:仅以偏移量顺序使用非事务性消息或已提交事务性消息。为了维护偏移排序，这个设置意味着我们必须在使用者中缓冲消息，直到看到给定事务中的所有消息。  

#### 1.2.2.2. Java API
<!-- 
https://blog.csdn.net/mlljava1111/article/details/81180351
-->
&emsp; producer提供了五个事务方法：  

* initTransactions
* beginTransaction
* sendOffsets
* commitTransaction
* abortTransaction

    消费者代码，将配置中的自动提交属性（auto.commit）进行关闭，而且在代码里面也不能使用手动提交commitSync( )或者commitAsync( )。  


##### 1.2.2.2.1. “只有写”应用程序示例  

```java
package com.example.demo.transaction;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.Future;

public class TransactionProducer {
    private static Properties getProps(){
        Properties props =  new Properties();
        props.put("bootstrap.servers", "47.52.199.53:9092");
        props.put("retries", 2); // 重试次数
        props.put("batch.size", 100); // 批量发送大小
        props.put("buffer.memory", 33554432); // 缓存大小，根据本机内存大小配置
        props.put("linger.ms", 1000); // 发送频率，满足任务一个条件发送
        props.put("client.id", "producer-syn-2"); // 发送端id,便于统计
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("transactional.id","producer-1"); // 每台机器唯一
        props.put("enable.idempotence",true); // 设置幂等性
        return props;
    }
    public static void main(String[] args) {
        　　KafkaProducer<String, String> producer = new KafkaProducer<>(getProps());
        　　// 初始化事务
        　　producer.initTransactions();try {
                    Thread.sleep(2000);
                    // 开启事务
                    producer.beginTransaction();
                    // 发送消息到producer-syn
                    producer.send(new ProducerRecord<String, String>("producer-syn","test3"));
                    // 发送消息到producer-asyn
                    Future<RecordMetadata> metadataFuture = producer.send(new ProducerRecord<String, String>("producer-asyn","test4"));
                    // 提交事务
                    producer.commitTransaction();
                }catch (Exception e){
                    e.printStackTrace();
                    // 终止事务
                    producer.abortTransaction();
                }
    }
}
```

##### 1.2.2.2.2. 消费-生产并存（consume-Transform-Produce）  
&emsp; 在一个事务中，既有生产消息操作又有消费消息操作，即常说的Consume-tansform-produce模式。如下实例代码  

```java
package com.example.demo.transaction;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

public class consumeTransformProduce {
    private static Properties getProducerProps(){
        Properties props =  new Properties();
        props.put("bootstrap.servers", "47.52.199.51:9092");
        props.put("retries", 3); // 重试次数
        props.put("batch.size", 100); // 批量发送大小
        props.put("buffer.memory", 33554432); // 缓存大小，根据本机内存大小配置
        props.put("linger.ms", 1000); // 发送频率，满足任务一个条件发送
        props.put("client.id", "producer-syn-2"); // 发送端id,便于统计
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("transactional.id","producer-2"); // 每台机器唯一
        props.put("enable.idempotence",true); // 设置幂等性
        return props;
    }

    private static Properties getConsumerProps(){
        Properties props =  new Properties();
        props.put("bootstrap.servers", "47.52.199.51:9092");
        props.put("group.id", "test_3");
        props.put("session.timeout.ms", 30000);       // 如果其超时，将会可能触发rebalance并认为已经死去，重新选举Leader
        props.put("enable.auto.commit", "false");      // 开启自动提交
        props.put("auto.commit.interval.ms", "1000"); // 自动提交时间
        props.put("auto.offset.reset","earliest"); // 从最早的offset开始拉取，latest:从最近的offset开始消费
        props.put("client.id", "producer-syn-1"); // 发送端id,便于统计
        props.put("max.poll.records","100"); // 每次批量拉取条数
        props.put("max.poll.interval.ms","1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("isolation.level","read_committed"); // 设置隔离级别
        return props;
    }
    public static void main(String[] args) {
        // 创建生产者
        KafkaProducer<String, String> producer = new KafkaProducer<>(getProducerProps());
        // 创建消费者
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(getConsumerProps());
        // 初始化事务
        producer.initTransactions();
        // 订阅主题
        consumer.subscribe(Arrays.asList("consumer-tran"));
        for(;;){
            // 开启事务
            producer.beginTransaction();
            // 接受消息
            ConsumerRecords<String, String> records = consumer.poll(500);
            // 处理逻辑
            try {
                Map<TopicPartition, OffsetAndMetadata> commits = new HashMap<>();
                for(ConsumerRecord record : records){
                    // 处理消息
                    System.out.printf("offset = %d, key = %s, value = %s\n", record.offset(), record.key(), record.value());
                    // 记录提交的偏移量
                    commits.put(new TopicPartition(record.topic(), record.partition()),new OffsetAndMetadata(record.offset()));
                    // 产生新消息
                    Future<RecordMetadata> metadataFuture = producer.send(new ProducerRecord<>("consumer-send",record.value()+"send"));
                }
                // 提交偏移量
                producer.sendOffsetsToTransaction(commits,"group0323");
                // 事务提交
                producer.commitTransaction();

            }catch (Exception e){
                e.printStackTrace();
                producer.abortTransaction();
            }
        }
    }
}
```

### 1.2.3. kafka事务原理  

#### 基本概念  
<!-- 
https://blog.csdn.net/mlljava1111/article/details/81180351

Transaction Marker与PID提供了识别消息是否应该被读取的能力，从而实现了事务的隔离性。
Offset的更新标记了消息是否被读取，从而将对读操作的事务处理转换成了对写（Offset）操作的事务处理。
Kafka事务的本质是，将一组写操作（如果有）对应的消息与一组读操作（如果有）对应的Offset的更新进行同样的标记（Transaction Marker）来实现事务中涉及的所有读写操作同时对外可见或同时对外不可见。
Kafka只提供对Kafka本身的读写操作的事务性，不提供包含外部系统的事务性。

-->

&emsp; 为了支持事务，Kafka 0.11.0版本引入以下概念：  
1. 事务协调者：类似于消费组负载均衡的协调者，每一个实现事务的生产端都被分配到一个事务协调者(Transaction Coordinator)。
2. 引入一个内部Kafka Topic作为事务Log：类似于消费管理Offset的Topic，事务Topic本身也是持久化的，日志信息记录事务状态信息，由事务协调者写入。
3. 引入控制消息(Control Messages)：这些消息是客户端产生的并写入到主题的特殊消息，但对于使用者来说不可见。它们是用来让broker告知消费者之前拉取的消息是否被原子性提交。
4. 引入TransactionId：不同生产实例使用同一个TransactionId表示是同一个事务，可以跨Session的数据幂等发送。当具有相同Transaction ID的新的Producer实例被创建且工作时，旧的且拥有相同Transaction ID的Producer将不再工作，避免事务僵死。
5. Producer ID：每个新的Producer在初始化的时候会被分配一个唯一的PID，这个PID对用户是不可见的。主要是为提供幂等性时引入的。
6. Sequence Numbler。（对于每个PID，该Producer发送数据的每个\<Topic, Partition>都对应一个从0开始单调递增的Sequence Number。
7. 每个生产者增加一个epoch：用于标识同一个事务Id在一次事务中的epoch，每次初始化事务时会递增，从而让服务端可以知道生产者请求是否旧的请求。
8. 幂等性：保证发送单个分区的消息只会发送一次，不会出现重复消息。增加一个幂等性的开关enable.idempotence，可以独立与事务使用，即可以只开启幂等但不开启事务。  

### 幂等性和事务性的关系  
<!-- 
https://blog.csdn.net/mlljava1111/article/details/81180351
https://blog.csdn.net/BeiisBei/article/details/104737298
-->


#### 事务流程  
<!-- 
https://blog.csdn.net/BeiisBei/article/details/104737298
https://blog.csdn.net/mlljava1111/article/details/81180351
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-64.png)  

##### 查找事务协调者Tranaction Corordinator  
&emsp; 由于Transaction Coordinator是分配PID和管理事务的核心，因此Producer要做的第一件事情就是通过向任意一个Broker发送FindCoordinator请求找到Transaction Coordinator的位置。  
&emsp; 注意：只有应用程序为Producer配置了Transaction ID时才可使用事务特性，也才需要这一步。另外，由于事务性要求Producer开启幂等特性，因此通过将transactional.id设置为非空从而开启事务特性的同时也需要通过将enable.idempotence设置为true来开启幂等特性。  


<!--
获取produce ID  
在知道事务协调者后，生产者需要往协调者发送初始化pid请求(initPidRequest)。这个请求分两种情况：  

* 不带transactionID
这种情况下直接生成一个新的produce ID即可，返回给客户端  
* 带transactionID
这种情况下，kafka根据transactionalId获取对应的PID，这个对应关系是保存在事务日志中（上图2a）。这样可以确保相同的TransactionId返回相同的PID，用于恢复或者终止之前未完成的事务。

---
找到Transaction Coordinator后，具有幂等特性的Producer必须发起InitPidRequest请求以获取PID。

注意：只要开启了幂等特性即必须执行该操作，而无须考虑该Producer是否开启了事务特性。

如果事务特性被开启
InitPidRequest会发送给Transaction Coordinator。如果Transaction Coordinator是第一次收到包含有该Transaction ID的InitPidRequest请求，它将会把该\<TransactionID, PID>存入Transaction Log，如上图中步骤2.1所示。这样可保证该对应关系被持久化，从而保证即使Transaction Coordinator宕机该对应关系也不会丢失。

除了返回PID外，InitPidRequest还会执行如下任务：

    增加该PID对应的epoch。具有相同PID但epoch小于该epoch的其它Producer（如果有）新开启的事务将被拒绝。
    恢复（Commit或Abort）之前的Producer未完成的事务（如果有）。

注意：InitPidRequest的处理过程是同步阻塞的。一旦该调用正确返回，Producer即可开始新的事务。

另外，如果事务特性未开启，InitPidRequest可发送至任意Broker，并且会得到一个全新的唯一的PID。该Producer将只能使用幂等特性以及单一Session内的事务特性，而不能使用跨Session的事务特性。
-->

##### 初始化事务 initTransaction  
&emsp; Producer发送InitpidRequest给事务协调器，获取一个Pid。InitpidRequest的处理过程是同步阻塞的，一旦该调用正确返回，Producer就可以开始新的事务。TranactionalId通过InitpidRequest发送给Tranciton Corordinator，然后在Tranaciton Log中记录这\<TranacionalId,pid>的映射关系。除了返回PID之外，还具有如下功能：  

    对PID对应的epoch进行递增，这样可以保证同一个app的不同实例对应的PID是一样的，但是epoch是不同的。
    回滚之前的Producer未完成的事务（如果有）。  

##### 开始事务 beginTransaction
&emsp; 执行Producer的beginTransacion()，它的作用是Producer在本地记录下这个transaction的状态为开始状态。这个操作并没有通知Transaction Coordinator，因为Transaction Coordinator只有在Producer发送第一条消息后才认为事务已经开启。

##### Consume-Transform-Produce  
&emsp; 这一阶段，包含了整个事务的数据处理过程，并且包含了多种请求。  

&emsp; **AddPartitionsToTxnRequest**  
&emsp; 一个Producer可能会给多个\<Topic, Partition>发送数据，给一个新的\<Topic, Partition>发送数据前，它需要先向Transaction Coordinator发送AddPartitionsToTxnRequest。  
&emsp; Transaction Coordinator会将该\<Transaction, Topic, Partition>存于Transaction Log内，并将其状态置为BEGIN，如上图中步骤4.1所示。有了该信息后，我们才可以在后续步骤中为每个Topic, Partition>设置COMMIT或者ABORT标记（如上图中步骤5.2所示）。  
&emsp; 另外，如果该\<Topic, Partition>为该事务中第一个\<Topic, Partition>，Transaction Coordinator还会启动对该事务的计时（每个事务都有自己的超时时间）。  

**ProduceRequest**  
&emsp; Producer通过一个或多个ProduceRequest发送一系列消息。除了应用数据外，该请求还包含了PID，epoch，和Sequence Number。该过程如上图中步骤4.2所示。  

**AddOffsetsToTxnRequest**
&emsp; 为了提供事务性，Producer新增了sendOffsetsToTransaction方法，该方法将多组消息的发送和消费放入同一批处理内。  
&emsp; 该方法先判断在当前事务中该方法是否已经被调用并传入了相同的Group ID。若是，直接跳到下一步；若不是，则向Transaction Coordinator发送AddOffsetsToTxnRequests请求，Transaction Coordinator将对应的所有<Topic, Partition>存于Transaction Log中，并将其状态记为BEGIN，如上图中步骤4.3所示。该方法会阻塞直到收到响应。    

&emsp; **TxnOffsetCommitRequest**  
&emsp; 作为sendOffsetsToTransaction方法的一部分，在处理完AddOffsetsToTxnRequest后，Producer也会发送TxnOffsetCommit请求给Consumer Coordinator从而将本事务包含的与读操作相关的各<Topic, Partition>的Offset持久化到内部的__consumer_offsets中，如上图步骤4.4所示。  
&emsp; 在此过程中，Consumer Coordinator会通过PID和对应的epoch来验证是否应该允许该Producer的该请求。  
&emsp; 这里需要注意：  

* 写入__consumer_offsets的Offset信息在当前事务Commit前对外是不可见的。也即在当前事务被Commit前，可认为该Offset尚未Commit，也即对应的消息尚未被完成处理。
* Consumer Coordinator并不会立即更新缓存中相应<Topic, Partition>的Offset，因为此时这些更新操作尚未被COMMIT或ABORT。  

##### 事务提交和事务终结(放弃事务)  
在Producer执行commitTransaction/abortTransaction时，Transaction Coordinator会执行一个两阶段提交：  

* 第一阶段，将Transaction Log内的该事务状态设置为PREPARE_COMMIT或PREPARE_ABORT
* 第二阶段，将Transaction Marker写入该事务涉及到的所有消息（即将消息标记为committed或aborted）。这一步骤Transaction Coordinator会发送给当前事务涉及到的每个\<Topic, Partition>的Leader，Broker收到该请求后，会将对应的Transaction Marker控制信息写入日志。

一旦Transaction Marker写入完成，Transaction Coordinator会将最终的COMPLETE_COMMIT或COMPLETE_ABORT状态写入Transaction Log中以标明该事务结束。  

----

&emsp; 一旦上述数据写入操作完成，应用程序必须调用KafkaProducer的commitTransaction方法或者abortTransaction方法以结束当前事务。
&emsp; **EndTxnRequest**  
&emsp; commitTransaction方法使得Producer写入的数据对下游Consumer可见。abortTransaction方法通过Transaction Marker将Producer写入的数据标记为Aborted状态。下游的Consumer如果将isolation.level设置为READ_COMMITTED，则它读到被Abort的消息后直接将其丢弃而不会返回给客户程序，也即被Abort的消息对应用程序不可见。  
&emsp; 无论是Commit还是Abort，Producer都会发送EndTxnRequest请求给Transaction Coordinator，并通过标志位标识是应该Commit还是Abort。    
&emsp; 收到该请求后，Transaction Coordinator会进行如下操作  

1. 将PREPARE_COMMIT或PREPARE_ABORT消息写入Transaction Log，如上图中步骤5.1所示
2. 通过WriteTxnMarker请求以Transaction Marker的形式将COMMIT或ABORT信息写入用户数据日志以及Offset Log中，如上图中步骤5.2所示
3. 最后将COMPLETE_COMMIT或COMPLETE_ABORT信息写入Transaction Log中，如上图中步骤5.3所示

&emsp; 补充说明：对于commitTransaction方法，它会在发送EndTxnRequest之前先调用flush方法以确保所有发送出去的数据都得到相应的ACK。对于abortTransaction方法，在发送EndTxnRequest之前直接将当前Buffer中的事务性消息（如果有）全部丢弃，但必须等待所有被发送但尚未收到ACK的消息发送完成。  
&emsp; 上述第二步是实现将一组读操作与写操作作为一个事务处理的关键。因为Producer写入的数据Topic以及记录Comsumer Offset的Topic会被写入相同的Transactin Marker，所以这一组读操作与写操作要么全部COMMIT要么全部ABORT。  

**WriteTxnMarkerRequest**  
&emsp; 上面提到的WriteTxnMarkerRequest由Transaction Coordinator发送给当前事务涉及到的每个\<Topic, Partition>的Leader。收到该请求后，对应的Leader会将对应的COMMIT(PID)或者ABORT(PID)控制信息写入日志，如上图中步骤5.2所示。  
&emsp; 该控制消息向Broker以及Consumer表明对应PID的消息被Commit了还是被Abort了。  
&emsp; 这里要注意，如果事务也涉及到__consumer_offsets，即该事务中有消费数据的操作且将该消费的Offset存于__consumer_offsets中，Transaction Coordinator也需要向该内部Topic的各Partition的Leader发送WriteTxnMarkerRequest从而写入COMMIT(PID)或COMMIT(PID)控制信息。  

**写入最终的COMPLETE_COMMIT或COMPLETE_ABORT消息**  
&emsp; 写完所有的Transaction Marker后，Transaction Coordinator会将最终的COMPLETE_COMMIT或COMPLETE_ABORT消息写入Transaction Log中以标明该事务结束，如上图中步骤5.3所示。  
&emsp; 此时，Transaction Log中所有关于该事务的消息全部可以移除。当然，由于Kafka内数据是Append Only的，不可直接更新和删除，这里说的移除只是将其标记为null从而在Log Compact时不再保留。  
&emsp; 另外，COMPLETE_COMMIT或COMPLETE_ABORT的写入并不需要得到所有Rreplica的ACK，因为如果该消息丢失，可以根据事务协议重发。  

&emsp; 补充说明，如果参与该事务的某些\<Topic, Partition>在被写入Transaction Marker前不可用，它对READ_COMMITTED的Consumer不可见，但不影响其它可用\<Topic, Partition>的COMMIT或ABORT。在该\<Topic, Partition>恢复可用后，Transaction Coordinator会重新根据PREPARE_COMMIT或PREPARE_ABORT向该\<Topic, Partition>发送Transaction Marker。  

### 总结  
* PID与Sequence Number的引入实现了写操作的幂等性
* 写操作的幂等性结合At Least Once语义实现了单一Session内的Exactly Once语义
* Transaction Marker与PID提供了识别消息是否应该被读取的能力，从而实现了事务的隔离性
* Offset的更新标记了消息是否被读取，从而将对读操作的事务处理转换成了对写（Offset）操作的事务处理
* Kafka事务的本质是，将一组写操作（如果有）对应的消息与一组读操作（如果有）对应的Offset的更新进行同样的标记（即Transaction Marker）来实现事务中涉及的所有读写操作同时对外可见或同时对外不可见
* Kafka只提供对Kafka本身的读写操作的事务性，不提供包含外部系统的事务性

