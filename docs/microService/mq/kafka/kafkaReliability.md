<!-- TOC -->

- [1. kafka如何保证消息队列不丢失?](#1-kafka如何保证消息队列不丢失)
    - [1.1. Kafka消息传递过程概述](#11-kafka消息传递过程概述)
    - [1.2. Producer端丢失消息](#12-producer端丢失消息)
        - [1.2.1. 发送消息的流程](#121-发送消息的流程)
        - [1.2.2. Producer的ACK应答机制](#122-producer的ack应答机制)
        - [1.2.3. 丢失消息的情况及解决方案](#123-丢失消息的情况及解决方案)
    - [1.3. Broker端丢失消息](#13-broker端丢失消息)
        - [1.3.1. 消息持久化](#131-消息持久化)
        - [1.3.2. 丢失消息的情况及解决方案](#132-丢失消息的情况及解决方案)
    - [1.4. Consumer端丢失消息](#14-consumer端丢失消息)
        - [1.4.1. 消息消费流程](#141-消息消费流程)
        - [1.4.2. 丢失消息情况一（自动提交）](#142-丢失消息情况一自动提交)
        - [1.4.3. 丢失消息情况二（手动提交）](#143-丢失消息情况二手动提交)
        - [1.4.4. 丢失消息情况三（多线程消费消息）](#144-丢失消息情况三多线程消费消息)
        - [1.4.5. 丢失消息情况四（增加分区）](#145-丢失消息情况四增加分区)
        - [1.4.6. 使用Low level API，严格控制位移](#146-使用low-level-api严格控制位移)
    - [1.5. ~~最佳实践~~](#15-最佳实践)
    - [1.6. ★★★方案二：使用补偿机制](#16-★★★方案二使用补偿机制)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**
1. 在Producer端、Broker端、Consumer端都有可能丢失消息。  
2. Producer端：  
&emsp; 为防止Producer端丢失消息， **<font color = "red">除了将ack设置为all，表明所有副本 Broker 都要接收到消息，才算“已提交”。</font>**  
&emsp; `还可以使用带有回调通知的发送API，即producer.send(msg, callback)`。  
3. Broker端:  
&emsp; Kafka没有提供同步刷盘的方式。要完全让kafka保证单个broker不丢失消息是做不到的，只能通过调整刷盘机制的参数缓解该情况。  
&emsp; 为了解决该问题，kafka通过producer和broker协同处理单个broker丢失参数的情况。 **<font color = "red">`一旦producer发现broker消息丢失，即可自动进行retry。`</font>** 除非retry次数超过阀值（可配置），消息才会丢失。此时需要生产者客户端手动处理该情况。  
4. ~~Consumer端：~~  
&emsp; 采用手动提交位移。  
5. `方案二：使用补偿机制`  
&emsp; 服务端丢失消息处理：建立消息表，发送消息前保存表记录，发送后更新表记录。  
&emsp; 客户端丢失消息处理：服务端提供查询接口。 


# 1. kafka如何保证消息队列不丢失?
<!--
https://mp.weixin.qq.com/s/EpY_U1vIGs-F0PyEfpgNOw
Kafka消息中间件到底会不会丢消息 
https://mp.weixin.qq.com/s/uxYUEJRTEIULeObRIl209A
面试官问：Kafka 会不会丢消息？怎么处理的? 
https://mp.weixin.qq.com/s/0eotlFBTSl-HxInb4Xg1Iw


在车商模块存储的保证金和保证金模块的金额数据确实存在不一致的情况（数据采样分析，约有2%左右）。解决方案：
（1）调整两个模块的消息同步机制，解决消息丢失问题。5.11日之前完成；
（2）车商保证金数据批量同步，5.11日之前完成同步脚本开发测试。5.11日线上同步；
-->

## 1.1. Kafka消息传递过程概述  


&emsp; **Kafka有三次消息传递的过程：**  

* 生产者发消息给Kafka Broker。
* Kafka Broker消息同步和持久化。
* Kafka Broker将消息传递给消费者。

&emsp; **<font color = "blue">在这三步中每一步都有可能会丢失消息。</font>**    

## 1.2. Producer端丢失消息  
### 1.2.1. 发送消息的流程
&emsp; 生产者发送消息的一般流程（部分流程与具体配置项强相关，这里先忽略）：  

1. 生产者是与leader直接交互，所以先从集群获取topic对应分区的leader元数据；
2. 获取到leader分区元数据后直接将消息发给过去；
3. Kafka Broker对应的leader分区收到消息后写入文件持久化；
4. Follower拉取Leader消息与Leader的数据保持一致；
5. Follower消息拉取完毕需要给Leader回复ACK确认消息；
6. Kafka Leader和Follower分区同步完，Leader分区会给生产者回复ACK确认消息。

![image](http://182.92.69.8:8081/img/microService/mq/kafka/kafka-96.png)  

&emsp; 生产者采用push模式将数据发布到broker，每条消息追加到分区中，顺序写入磁盘。消息写入Leader后，Follower是主动与Leader进行同步。  
&emsp; Kafka消息发送有两种方式：同步（sync）和异步（async），默认是同步方式，可通过producer.type属性进行配置。  

### 1.2.2. Producer的ACK应答机制  
<!-- 
&emsp; kafka为了保证高可用性，采用了副本机制。当 ISR副本中的follower 完成数据的同步之后，leader 就会给 follower 发送 ack。如果 follower 长时间未向 leader 同步数据，则该 follower 将会被踢出 ISR，该时间阈值由 replica.lag.time.max.ms 参数设定。leader 发生故障之后，就会从 ISR 中选举新的 leader。（之前还有另一个参数，0.9 版本之后 replica.lag.max.messages 参数被移除了）  
&emsp; 对于某些不太重要的数据，对数据的可靠性要求不是很高，能够容忍数据的少量丢失，所以没必要等 ISR 中的follower全部接收成功。kafka的request.required.acks 可设置为 1、0、-1 三种情况。  

    acks=0，producer不等待broker的响应，效率最高，但是消息很可能会丢。
    acks=1，leader broker收到消息后，不等待其他follower的响应，即返回ack。也可以理解为ack数为1。此时，如果follower还没有收到leader同步的消息leader就挂了，那么消息会丢失。按照上图中的例子，如果leader收到消息，成功写入PageCache后，会返回ack，此时producer认为消息发送成功。但此时，按照上图，数据还没有被同步到follower。如果此时leader断电，数据会丢失。
    acks=-1，leader broker收到消息后，挂起，等待所有ISR列表中的follower返回结果后，再返回ack。-1等效与all。这种配置下，只有leader写入数据到pagecache是不会返回ack的，还需要所有的ISR返回“成功”才会触发ack。如果此时断电，producer可以知道消息没有被发送成功，将会重新发送。如果在follower收到数据以后，成功返回ack，leader断电，数据将存在于原来的follower中。在重新选举以后，新的leader会持有该部分数据。数据从leader同步到follower，需要2步：
        数据从pageCache被刷盘到disk。因为只有disk中的数据才能被同步到replica。
        数据同步到replica，并且replica成功将数据写入PageCache。在producer得到ack后，哪怕是所有机器都停电，数据也至少会存在于leader的磁盘内。

&emsp; 将服务器的ACK级别设置为-1，可以保证Producer到Server之间不会丢失数据，即 At Least Once 语义。相对的，将服务器ACK级别设置为0，可以保证生产者每条消息只会被发送一次，即At Most Once语义。  
&emsp; At Least Once可以保证数据不丢失，但是不能保证数据不重复。相对的，At Most Once 可以保证数据不重复，但是不能保证数据不丢失。但是，对于一些非常重要的信息，比如说交易数据，下游数据消费者要求数据既不重复也不丢失，即Exactly Once语义。在0.11版本以前的Kafka，对此是无能为力的，只能保证数据不丢失，再在下游消费者对数据做全局去重。对于多个下游应用的情况，每个都需要单独做全局去重，这就对性能造成了很大的影响。 
-->
&emsp; 消息发送到brocker后，Kafka会进行副本同步。对于某些不太重要的数据，对数据的可靠性要求不是很高，能够容忍数据的少量丢失，所以没必要等 ISR 中的follower全部接收成功。kafka的request.required.acks 可设置为 1、0、-1 三种情况。  
&emsp; Kafka通过配置request.required.acks属性来确认消息的生产：

* 0表示不进行消息接收是否成功的确认；不能保证消息是否发送成功，生成环境基本不会用。
* 1表示当Leader接收成功时确认；只要Leader存活就可以保证不丢失，保证了吞吐量。
* -1或者all表示Leader和Follower都接收成功时确认；可以最大限度保证消息不丢失，但是吞吐量低。

&emsp; kafka producer 的参数acks 的默认值为1，所以默认的producer级别是at least once，并不能exactly once。

![image](http://182.92.69.8:8081/img/microService/mq/kafka/kafka-61.png)  
&emsp; 设置为 1 时代表当 Leader 状态的 Partition 接收到消息并持久化时就认为消息发送成功，如果 ISR 列表的 Replica 还没来得及同步消息，Leader 状态的 Partition 对应的 Broker 宕机，则消息有可能丢失。    
![image](http://182.92.69.8:8081/img/microService/mq/kafka/kafka-62.png)  
&emsp; 设置为 0 时代表 Producer 发送消息后就认为成功，消息有可能丢失。    
![image](http://182.92.69.8:8081/img/microService/mq/kafka/kafka-63.png)  
&emsp; 设置为-1 时，代表 ISR 列表中的所有 Replica 将消息同步完成后才认为消息发送成功；但是如果只存在主Partition的时候，Broker异常时同样会导致消息丢失。所以此时就需要min.insync.replicas参数的配合，该参数需要设定值大于等于2，当Partition的个数小于设定的值时，Producer发送消息会直接报错。  

&emsp; 上面这个过程看似已经很完美了，但是假设如果消息在同步到部分从Partition 上时，主 Partition 宕机，此时消息会重传，虽然消息不会丢失，但是会造成同一条消息会存储多次。在新版本中 Kafka 提出了幂等性的概念，通过给每条消息设置一个唯一 ID，并且该 ID 可以唯一映射到 Partition 的一个固定位置，从而避免消息重复存储的问题。 

### 1.2.3. 丢失消息的情况及解决方案  

* 如果acks配置为0，发生网络抖动消息丢了，生产者不校验ACK自然就不知道丢了。
* 如果acks配置为1保证leader不丢，但是如果leader挂了，恰好选了一个没有ACK的follower，那也丢了。
* all：保证leader和follower不丢，但是如果网络拥塞，没有收到ACK，会有重复发的问题。

&emsp; 为防止Producer端丢失消息，除了将ack设置为all，还可以使用带有回调通知的发送API，即producer.send(msg, callback)。  

## 1.3. Broker端丢失消息
<!-- 
&emsp; Broker丢失消息是由于Kafka本身的原因造成的，kafka为了得到更高的性能和吞吐量，将数据异步批量的存储在磁盘中。消息的刷盘过程，为了提高性能，减少刷盘次数，kafka采用了批量刷盘的做法。即，按照一定的消息量，和时间间隔进行刷盘。这种机制也是由于linux操作系统决定的。将数据存储到linux操作系统种，会先存储到页缓存（Page cache）中，按照时间或者其他条件进行刷盘（从page cache到file），或者通过fsync命令强制刷盘。数据在page cache中时，如果系统挂掉，数据会丢失。  
![image](http://182.92.69.8:8081/img/microService/mq/kafka/kafka-95.png)  
&emsp; 上图简述了broker写数据以及同步的一个过程。broker写数据只写到PageCache中，而pageCache位于内存。这部分数据在断电后是会丢失的。pageCache的数据通过linux的flusher程序进行刷盘。刷盘触发条件有三：  

* 主动调用sync或fsync函数
* 可用内存低于阀值
* dirty data时间达到阀值。dirty是pagecache的一个标识位，当有数据写入到pageCache时，pagecache被标注为dirty，数据刷盘以后，dirty标志清除。

&emsp; Broker配置刷盘机制，是通过调用fsync函数接管了刷盘动作。从单个Broker来看，pageCache的数据会丢失。  
&emsp; Kafka没有提供同步刷盘的方式。要完全让kafka保证单个broker不丢失消息是做不到的，只能通过调整刷盘机制的参数缓解该情况。比如，减少刷盘间隔，减少刷盘数据量大小。时间越短，性能越差，可靠性越好（尽可能可靠）。这是一个选择题。  

&emsp; **为了解决该问题，**kafka通过producer和broker协同处理单个broker丢失参数的情况。一旦producer发现broker消息丢失，即可自动进行retry。除非retry次数超过阀值（可配置），消息才会丢失。此时需要生产者客户端手动处理该情况。那么producer是如何检测到数据丢失的呢？是通过ack机制。  
-->
<!-- 
&emsp; 为了提升效率，减少IO，producer在发送数据时可以将多个请求进行合并后发送。被合并的请求咋发送一线缓存在本地buffer中。缓存的方式和前文提到的刷盘类似，producer可以将请求打包成“块”或者按照时间间隔，将buffer中的数据发出。通过buffer可以将生产者改造为异步的方式，而这可以提升发送效率。  
&emsp; 但是，buffer中的数据就是危险的。在正常情况下，客户端的异步调用可以通过callback来处理消息发送失败或者超时的情况，但是，一旦producer被非法的停止了，那么buffer中的数据将丢失，broker将无法收到该部分数据。又或者，当Producer客户端内存不够时，如果采取的策略是丢弃消息（另一种策略是block阻塞），消息也会被丢失。抑或，消息产生（异步产生）过快，导致挂起线程过多，内存不足，导致程序崩溃，消息丢失。  
![image](http://182.92.69.8:8081/img/microService/mq/kafka/kafka-73.png)  
![image](http://182.92.69.8:8081/img/microService/mq/kafka/kafka-73.png)  

&emsp; **根据上图，可以想到几个解决的思路：**  

* 异步发送消息改为同步发送消。或者service产生消息时，使用阻塞的线程池，并且线程数有一定上限。整体思路是控制消息产生速度。  
* 扩大Buffer的容量配置。这种方式可以缓解该情况的出现，但不能杜绝。  
* service不直接将消息发送到buffer（内存），而是将消息写到本地的磁盘中（数据库或者文件），由另一个（或少量）生产线程进行消息发送。相当于是在buffer和service之间又加了一层空间更加富裕的缓冲层。  
-->

### 1.3.1. 消息持久化  
&emsp; Kafka Broker接收到数据后会将数据进行持久化存储。持久化流程： 
![image](http://182.92.69.8:8081/img/microService/mq/kafka/kafka-97.png)  
&emsp; 操作系统本身有一层缓存，叫做 Page Cache，当往磁盘文件写入的时候，系统会先将数据流写入缓存中，至于什么时候将缓存的数据写入文件中是由操作系统自行决定。  
&emsp; Kafka提供了一个参数 producer.type 来控制是不是主动flush，如果Kafka写入到mmap之后就立即 flush 然后再返回 Producer 叫同步 (sync)；写入mmap之后立即返回 Producer 不调用 flush 叫异步 (async)。  

### 1.3.2. 丢失消息的情况及解决方案   
&emsp; Kafka通过多分区多副本机制中已经能最大限度保证数据不会丢失，如果数据已经写入系统 cache 中，但是还没来得及刷入磁盘，此时突然机器宕机或者掉电那就丢了，当然这种情况很极端。  

&emsp; **<font color = "red">解决方案：</font>**  
&emsp; Kafka没有提供同步刷盘的方式。要完全让kafka保证单个broker不丢失消息是做不到的，只能通过调整刷盘机制的参数缓解该情况。  
&emsp; 为了解决该问题，kafka通过producer和broker协同处理单个broker丢失参数的情况。一旦producer发现broker消息丢失，即可自动进行retry。除非retry次数超过阀值（可配置），消息才会丢失。此时需要生产者客户端手动处理该情况。  

## 1.4. Consumer端丢失消息
### 1.4.1. 消息消费流程  
&emsp; Consumer消费消息有下面几个步骤：  

* 接收消息
* 处理消息
* 反馈“处理完毕”（commited）

&emsp; 消费者通过pull模式主动的去 kafka 集群拉取消息，与producer相同的是，消费者在拉取消息的时候也是找leader分区去拉取。  
&emsp; 多个消费者可以组成一个消费者组（consumer group），每个消费者组都有一个组id。同一个消费组者的消费者可以消费同一topic下不同分区的数据，但是不会出现多个消费者消费同一分区的数据。  
![image](http://182.92.69.8:8081/img/microService/mq/kafka/kafka-98.png)  
&emsp; 消费者消费的进度通过offset保存在kafka集群的__consumer_offsets这个topic中。  

&emsp; 消费消息的时候主要分为两个阶段：  
1. 标识消息已被消费，commit offset坐标；  
2. 处理消息。  

&emsp; Consumer的消费方式主要分为两种：  

* 自动提交offset，Automatic Offset Committing
* 手动提交offset，Manual Offset Control

<!-- 
敲黑板了，这里可能会丢消息的！

场景一：先commit再处理消息。如果在处理消息的时候异常了，但是offset 已经提交了，这条消息对于该消费者来说就是丢失了，再也不会消费到了。

场景二：先处理消息再commit。如果在commit之前发生异常，下次还会消费到该消息，重复消费的问题可以通过业务保证消息幂等性来解决。
-->

### 1.4.2. 丢失消息情况一（自动提交） 
&emsp; Consumer自动提交的机制是根据一定的时间间隔，将收到的消息进行commit。commit过程和消费消息的过程是异步的。也就是说，可能存在消费过程未成功（比如抛出异常），commit消息已经提交了。此时消息就丢失了。  

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("group.id", "test");
// 自动提交开关
props.put("enable.auto.commit", "true");
// 自动提交的时间间隔，此处是1s
props.put("auto.commit.interval.ms", "1000");
props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Arrays.asList("foo", "bar"));
while (true) {
        // 调用poll后，1000ms后，消息状态会被改为 committed
  ConsumerRecords<String, String> records = consumer.poll(100);
  for (ConsumerRecord<String, String> record : records)
    insertIntoDB(record); // 将消息入库，时间可能会超过1000ms
```

&emsp; 上面的示例是自动提交的例子。如果此时，insertIntoDB(record)发生异常，消息将会出现丢失。  

### 1.4.3. 丢失消息情况二（手动提交）  
&emsp; 接下来是手动提交的例子：  

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("group.id", "test");
// 关闭自动提交，改为手动提交
props.put("enable.auto.commit", "false");
props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Arrays.asList("foo", "bar"));
final int minBatchSize = 200;
List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
while (true) {
        // 调用poll后，不会进行auto commit
  ConsumerRecords<String, String> records = consumer.poll(100);
  for (ConsumerRecord<String, String> record : records) {
    buffer.add(record);
  }
  if (buffer.size() >= minBatchSize) {
    insertIntoDb(buffer);
                // 所有消息消费完毕以后，才进行commit操作
    consumer.commitSync();
    buffer.clear();
  }
```
&emsp; 将提交类型改为手动以后，可以保证消息“至少被消费一次”(at least once)。但此时可能出现重复消费的情况。重复消费的问题可以通过业务保证消息幂等性来解决。  

### 1.4.4. 丢失消息情况三（多线程消费消息）   
&emsp; Consumer 从 Kafka 获取到消息后开启多个线程异步处理。  
&emsp; Consumer 自动地向前更新 offset，如果某个线程失败，则该线程上消息丢失。  

### 1.4.5. 丢失消息情况四（增加分区）  
&emsp; 增加Topic Partition分区。在某段不巧的时间间隔后，Producer 先于 Consumer 感知到新增加的Partition，此时 Consumer 设置的是从最新位移处开始读取消息，因此在 Consumer 感知到新分区前，Producer 发送的这些消息就全部丢失了。

### 1.4.6. 使用Low level API，严格控制位移
&emsp; 开发中一般直接使用Consumer的High level API，客户端对于offset等控制是透明的。也可以采用Low level API的方式，手动控制offset，也可以保证消息不丢，不过会更加复杂。  

```java
 try {
     while(running) {
         ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
         for (TopicPartition partition : records.partitions()) {
             List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
             for (ConsumerRecord<String, String> record : partitionRecords) {
                 System.out.println(record.offset() + ": " + record.value());
             }
             long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
             // 精确控制offset
             consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
         }
     }
 } finally {
   consumer.close();
 }
```

## 1.5. ~~最佳实践~~  
* 不要使用 producer.send(msg)，而是 producer.send(msg, callback) 
* 设置 acks = all  
    * acks 是 Producer 的一个参数，代表了你对“已提交“消息的定义。  
    * 如果设置为 all，表明所有副本 Broker 都要接收到消息，才算“已提交”。这是最严谨的定义。  
* 设置 retries 为一个较大的值  
    * retries 是 Producer 的参数，能够让 Producer 自动重试  
* 设置 unclean.leader.election.enable = false  
    * 这是 Broker 端参数，控制哪些 Broker 有资格竞选分区的 Leader。  
    * 如果一个 Broker 落后原先的 Leader 太多，那么它一旦是新的 Leader，则会造成消息丢失。  
* 设置 replicationn.factor >= 3  
    * Broker 端参数  
    * 最好将消息多保存几份  
* 设置 min.insync.replicas > 1  
    * Broker 端参数  
    * 控制的是消息至少要被写入到几个副本才算“已提交”
    * 生产环境要设置大于 1  
* 确保 replication.factor > min.insync.replicas  
    * 如果两者相等，只要有一个副本挂机，整个分区无法正常工作  
    * 推荐配置 replication.factor = min.insync.replicas + 1  
* 确保消息消费完再提交  
    * Consumer 端参数 enable.auto.commit 设置成 false，采用手动提交位移  
    * 这对于单 Consumer 多线程处理很重要  


<!-- 


1. 采用同步发送，但是性能会很差，并不推荐在实际场景中使用。因此最好能有一份配置，既使用异步方式还能有效地避免数据丢失，即使出现producer崩溃的情况也不会有问题。 
2. **做producer端的无消息丢失配置**  
    * producer端配置

            max.block.ms=3000   控制block的时长,当buffer空间不够或者metadata丢失时产生block
            acks=all or -1   所有follower都响应了发送消息才能认为提交成功
            retries=Integer.MAX_VALUE   producer开启无限重试，只会重试那些可恢复的异常情况
            max.in.flight.requests.per.connection=1   限制了producer在单个broker连接上能够发送的未响应请求的数量，为了防止topic同分区下的消息乱序问题
            使用带回调机制的send发送消息，即KafkaProducer.send(record,callback)   
            会返回消息发送的结果信息Callback的失败处理逻辑中显式地立即关闭producer，使用close(0)。目的是为了处理消息的乱序问题，将不允许将未完成的消息发送出去

    * broker端配置

            unclean.leader.election.enable=false   不允许非ISR中的副本被选举为leader
            replication.factor=3   强调一定要使用多个副本来保存分区的消息
            min.insync.replicas=2   控制某条消息至少被写入到ISR中的多少个副本才算成功，只有在producer端的acks设置成all或-1时，这个参数才有意义
            确保replication.factor>min.insync.replicas   

    * consumer端配置

            enable.auto.commit=false   设置不能自动提交位移，需要用户手动提交位移
-->


## 1.6. ★★★方案二：使用补偿机制  
&emsp; 服务端丢失消息处理：建立消息表，发送消息前保存表记录，发送后更新表记录。  
&emsp; 客户端丢失消息处理：服务端提供查询接口。  

