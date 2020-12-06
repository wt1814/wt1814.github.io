

# kafka如何保证消息队列不丢失?
<!-- 

Kafka消息中间件到底会不会丢消息 
https://mp.weixin.qq.com/s/uxYUEJRTEIULeObRIl209A
Kafka 无消息丢失配置
https://www.kancloud.cn/nicefo71/kafka/1471586

-->
&emsp; Kafka存在丢消息的问题，消息丢失会发生在Broker，Producer和Consumer三种。  

## 1.3.1. Broker端丢失消息
&emsp; Broker丢失消息是由于Kafka本身的原因造成的，kafka为了得到更高的性能和吞吐量，将数据异步批量的存储在磁盘中。消息的刷盘过程，为了提高性能，减少刷盘次数，kafka采用了批量刷盘的做法。即，按照一定的消息量，和时间间隔进行刷盘。这种机制也是由于linux操作系统决定的。将数据存储到linux操作系统种，会先存储到页缓存（Page cache）中，按照时间或者其他条件进行刷盘（从page cache到file），或者通过fsync命令强制刷盘。数据在page cache中时，如果系统挂掉，数据会丢失。  
&emsp; 上图简述了broker写数据以及同步的一个过程。broker写数据只写到PageCache中，而pageCache位于内存。这部分数据在断电后是会丢失的。pageCache的数据通过linux的flusher程序进行刷盘。刷盘触发条件有三：  

* 主动调用sync或fsync函数
* 可用内存低于阀值
* dirty data时间达到阀值。dirty是pagecache的一个标识位，当有数据写入到pageCache时，pagecache被标注为dirty，数据刷盘以后，dirty标志清除。

&emsp; Broker配置刷盘机制，是通过调用fsync函数接管了刷盘动作。从单个Broker来看，pageCache的数据会丢失。  
&emsp; Kafka没有提供同步刷盘的方式。要完全让kafka保证单个broker不丢失消息是做不到的，只能通过调整刷盘机制的参数缓解该情况。比如，减少刷盘间隔，减少刷盘数据量大小。时间越短，性能越差，可靠性越好（尽可能可靠）。这是一个选择题。  
&emsp; 为了解决该问题，kafka通过producer和broker协同处理单个broker丢失参数的情况。一旦producer发现broker消息丢失，即可自动进行retry。除非retry次数超过阀值（可配置），消息才会丢失。此时需要生产者客户端手动处理该情况。那么producer是如何检测到数据丢失的呢？是通过ack机制。  

### 1.3.1.1. ACK机制与ISR列表  
&emsp; kafka为了保证高可用性，采用了副本机制。  
&emsp; **a) 副本数据同步策略主要有如下两种**  

|方案	|优点	|缺点|
|---|---|---|
|半数以上完成同步，就发送ack|延迟低	|选举新的 leader 时，容忍n台节点的故障，需要2n+1个副本|
|全部完成同步，才发送ack|选举新的 leader 时，容忍n台节点的故障，需要 n+1 个副本|延迟高|

&emsp; Kafka 选择了第二种方案，原因如下：

* 同样为了容忍 n 台节点的故障，第一种方案需要的副本数相对较多，而 Kafka 的每个分区都有大量的数据，第一种方案会造成大量的数据冗余；
* 虽然第二种方案的网络延迟会比较高，但网络延迟对 Kafka 的影响较小。  

&emsp; **b) ISR**   
&emsp; 采用第二种方案之后，设想一下情景：leader 收到数据，所有 follower 都开始同步数据，但有一个 follower 挂了，迟迟不能与 leader 保持同步，那 leader 就要一直等下去，直到它完成同步，才能发送 ack，这个问题怎么解决呢？  
&emsp; leader 维护了一个动态的 in-sync replica set(ISR，保持同步的副本)，意为和 leader 保持同步的 follower 集合。当 ISR 中的follower 完成数据的同步之后，leader 就会给 follower 发送 ack。如果 follower 长时间未向 leader 同步数据，则该 follower 将会被踢出 ISR，该时间阈值由 replica.lag.time.max.ms 参数设定。leader 发生故障之后，就会从 ISR 中选举新的 leader。（之前还有另一个参数，0.9 版本之后 replica.lag.max.messages 参数被移除了）  

&emsp; **c) ack应答机制**  
&emsp; 对于某些不太重要的数据，对数据的可靠性要求不是很高，能够容忍数据的少量丢失，所以没必要等 ISR 中的follower全部接收成功。kafka的request.required.acks 可设置为 1、0、-1 三种情况。  
<!-- 
    acks=0，producer不等待broker的响应，效率最高，但是消息很可能会丢。
    acks=1，leader broker收到消息后，不等待其他follower的响应，即返回ack。也可以理解为ack数为1。此时，如果follower还没有收到leader同步的消息leader就挂了，那么消息会丢失。按照上图中的例子，如果leader收到消息，成功写入PageCache后，会返回ack，此时producer认为消息发送成功。但此时，按照上图，数据还没有被同步到follower。如果此时leader断电，数据会丢失。
    acks=-1，leader broker收到消息后，挂起，等待所有ISR列表中的follower返回结果后，再返回ack。-1等效与all。这种配置下，只有leader写入数据到pagecache是不会返回ack的，还需要所有的ISR返回“成功”才会触发ack。如果此时断电，producer可以知道消息没有被发送成功，将会重新发送。如果在follower收到数据以后，成功返回ack，leader断电，数据将存在于原来的follower中。在重新选举以后，新的leader会持有该部分数据。数据从leader同步到follower，需要2步：
        数据从pageCache被刷盘到disk。因为只有disk中的数据才能被同步到replica。
        数据同步到replica，并且replica成功将数据写入PageCache。在producer得到ack后，哪怕是所有机器都停电，数据也至少会存在于leader的磁盘内。
-->
    
    Kafka 的交付语义？交付语义一般有at least once、at most once和exactly once。kafka 通过 ack 的配置来实现前两种。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-61.png)  
&emsp; 设置为 1 时代表当 Leader 状态的 Partition 接收到消息并持久化时就认为消息发送成功，如果 ISR 列表的 Replica 还没来得及同步消息，Leader 状态的 Partition 对应的 Broker 宕机，则消息有可能丢失。    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-62.png)  
&emsp; 设置为 0 时代表 Producer 发送消息后就认为成功，消息有可能丢失。    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-63.png)  
&emsp; 设置为-1 时，代表 ISR 列表中的所有 Replica 将消息同步完成后才认为消息发送成功；但是如果只存在主 Partition 的时候，Broker 异常时同样会导致消息丢失。所以此时就需要min.insync.replicas参数的配合，该参数需要设定值大于等于 2，当 Partition 的个数小于设定的值时，Producer 发送消息会直接报错。  

&emsp; 上面这个过程看似已经很完美了，但是假设如果消息在同步到部分从Partition 上时，主 Partition 宕机，此时消息会重传，虽然消息不会丢失，但是会造成同一条消息会存储多次。在新版本中 Kafka 提出了幂等性的概念，通过给每条消息设置一个唯一 ID，并且该 ID 可以唯一映射到 Partition 的一个固定位置，从而避免消息重复存储的问题。  

## 1.3.2. Producer端丢失消息  
&emsp; Producer丢失消息，发生在生产者客户端。  
&emsp; 为了提升效率，减少IO，producer在发送数据时可以将多个请求进行合并后发送。被合并的请求咋发送一线缓存在本地buffer中。缓存的方式和前文提到的刷盘类似，producer可以将请求打包成“块”或者按照时间间隔，将buffer中的数据发出。通过buffer可以将生产者改造为异步的方式，而这可以提升发送效率。  
&emsp; 但是，buffer中的数据就是危险的。在正常情况下，客户端的异步调用可以通过callback来处理消息发送失败或者超时的情况，但是，一旦producer被非法的停止了，那么buffer中的数据将丢失，broker将无法收到该部分数据。又或者，当Producer客户端内存不够时，如果采取的策略是丢弃消息（另一种策略是block阻塞），消息也会被丢失。抑或，消息产生（异步产生）过快，导致挂起线程过多，内存不足，导致程序崩溃，消息丢失。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-73.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-73.png)  
&emsp; 根据上图，可以想到几个解决的思路：  

* 异步发送消息改为同步发送消。或者service产生消息时，使用阻塞的线程池，并且线程数有一定上限。整体思路是控制消息产生速度。  
* 扩大Buffer的容量配置。这种方式可以缓解该情况的出现，但不能杜绝。  
* service不直接将消息发送到buffer（内存），而是将消息写到本地的磁盘中（数据库或者文件），由另一个（或少量）生产线程进行消息发送。相当于是在buffer和service之间又加了一层空间更加富裕的缓冲层。  

## 1.3.3. Consumer 端丢失消息
&emsp; Consumer消费消息有下面几个步骤：  

* 接收消息
* 处理消息
* 反馈“处理完毕”（commited）

&emsp; Consumer的消费方式主要分为两种：  

* 自动提交offset，Automatic Offset Committing
* 手动提交offset，Manual Offset Control

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

&emsp; 上面的示例是自动提交的例子。如果此时，insertIntoDB(record)发生异常，消息将会出现丢失。接下来是手动提交的例子：  

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
&emsp; 将提交类型改为手动以后，可以保证消息“至少被消费一次”(at least once)。但此时可能出现重复消费的情况，重复消费不属于本篇讨论范围。  
&emsp; 上面两个例子，是直接使用Consumer的High level API，客户端对于offset等控制是透明的。也可以采用Low level API的方式，手动控制offset，也可以保证消息不丢，不过会更加复杂。  

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

