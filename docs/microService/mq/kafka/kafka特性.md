

<!-- TOC -->

- [1. kafka特性](#1-kafka特性)
    - [1.1. Kafka的高性能（读写机制）](#11-kafka的高性能读写机制)
        - [1.1.1. 顺序读写](#111-顺序读写)
            - [1.1.1.1. 顺序写入](#1111-顺序写入)
            - [1.1.1.2. Memory Mapped Files](#1112-memory-mapped-files)
        - [1.1.2. 基于 Sendfile 实现零拷贝（Zero Copy）](#112-基于-sendfile-实现零拷贝zero-copy)
    - [1.2. kafka高可用性（副本机制）](#12-kafka高可用性副本机制)
        - [Kafka副本简介](#kafka副本简介)
        - [1.2.1. 物理分区分配](#121-物理分区分配)
        - [1.2.2. Leader的选举(ISR副本)](#122-leader的选举isr副本)
            - [1.2.2.1. Unclear Leader选举](#1221-unclear-leader选举)
        - [1.2.3. 副本的同步(LEO和HW)](#123-副本的同步leo和hw)
            - [1.2.3.1. Leader Epoch](#1231-leader-epoch)
    - [1.3. kafka可靠性（如何保证消息队列不丢失?）](#13-kafka可靠性如何保证消息队列不丢失)
        - [1.3.1. Broker端丢失消息](#131-broker端丢失消息)
            - [1.3.1.1. ACK机制与ISR列表](#1311-ack机制与isr列表)
        - [1.3.2. Producer端丢失消息](#132-producer端丢失消息)
        - [1.3.3. Consumer 端丢失消息](#133-consumer-端丢失消息)
    - [1.4. kafka数据一致性，通过HW来保证](#14-kafka数据一致性通过hw来保证)
    - [1.5. 如何让 Kafka 的消息有序？](#15-如何让-kafka-的消息有序)
    - [1.6. 消息重复消费](#16-消息重复消费)

<!-- /TOC -->


# 1. kafka特性
<!-- 

面试官：说说Kafka处理请求的全流程 
https://mp.weixin.qq.com/s/LEmybNmD5XwkBtcTPHcaEA
-->

## 1.1. Kafka的高性能（读写机制）  
&emsp; Kafka 的消息是保存或缓存在磁盘上的，一般认为在磁盘上读写数据是会降低性能的，因为寻址会比较消耗时间，但是实际上，Kafka 的特性之一就是高吞吐率。Kafka 之所以能这么快，是因为：「顺序写磁盘、大量使用内存页、零拷贝技术的使用」..  

### 1.1.1. 顺序读写  
&emsp; 数据写入Kafka会把收到的消息都写入到硬盘中。为了优化写入速度，Kafka使用了：顺序写入和Memory Mapped File 。

#### 1.1.1.1. 顺序写入  
&emsp; kafka的消息是不断追加到文件中的，这个特性使kafka可以充分利用磁盘的顺序读写性能。Kafka会将数据顺序插入到文件末尾，消费者端通过控制偏移量来读取消息，这样做会导致数据无法删除，时间一长，磁盘空间会满，kafka提供了2种策略来删除数据：基于时间删除和基于partition文件的大小删除。  

    顺序读写不需要硬盘磁头的寻道时间，只需很少的扇区旋转时间，所以速度远快于随机读写。

#### 1.1.1.2. Memory Mapped Files  
&emsp; 这个和Java NIO中的内存映射基本相同，mmf （Memory Mapped Files）直接利用操作系统的Page来实现文件到物理内存的映射，完成之后对物理内存的操作会直接同步到硬盘。**mmf通过内存映射的方式大大提高了IO速率，省去了用户空间到内核空间的复制。它的缺点显而易见，不可靠，当发生宕机而数据未同步到硬盘时，数据会丢失。**  
<!-- Kafka 提供了produce.type参数来控制是否主动的进行刷新，如果 Kafka 写入到 mmf 后立即flush再返回给生产者则为同步模式，反之为异步模式。 --> 
&emsp; Kafka 提供了一个参数 producer.type 来控制是不是主动 Flush：  

* 如果 Kafka 写入到 mmf 之后就立即 Flush，然后再返回 Producer 叫同步 (Sync)。  
* 如果 Kafka 写入 mmf 之后立即返回 Producer 不调用 Flush 叫异步 (Async)。  

### 1.1.2. 基于 Sendfile 实现零拷贝（Zero Copy）  
<!-- 
在这之前先来了解一下零拷贝(直接让操作系统的 Cache 中的数据发送到网卡后传输给下游的消费者)：平时从服务器读取静态文件时，服务器先将文件从复制到内核空间，再复制到用户空间，最后再复制到内核空间并通过网卡发送出去，而零拷贝则是直接从内核到内核再到网卡，省去了用户空间的复制。

Kafka把所有的消息存放到一个文件中，当消费者需要数据的时候直接将文件发送给消费者，比如10W的消息共10M，全部发送给消费者，10M的消息在内网中传输是非常快的，假如需要1s，那么kafka的tps就是10w。Zero copy对应的是Linux中sendfile函数，这个函数会接受一个offsize来确定从哪里开始读取。现实中，不可能将整个文件全部发给消费者，他通过消费者传递过来的偏移量来使用零拷贝读取指定内容的数据返回给消费者。

在Linux kernel2.2 之后出现了一种叫做"零拷贝(zero-copy)"系统调用机制，就是跳过“用户缓冲区”的拷贝，建立一个磁盘空间和内存的直接映射，数据不再复制到“用户态缓冲区”，系统上下文切换减少为2次，可以提升一倍的性能。
-->
&emsp; 作为一个消息系统，不可避免的便是消息的拷贝，常规的操作，一条消息，需要从创建者的socket到应用，再到操作系统内核，然后才能落盘。同样，一条消息发送给消费者也要从磁盘到内核到应用再到接收者的socket，中间经过了多次不是很有必要的拷贝。  
&emsp; 传统 Read/Write 方式进行网络文件传输，在传输过程中，文件数据实际上是经过了四次 Copy 操作，其具体流程细节如下：  

* 调用 Read 函数，文件数据被 Copy 到内核缓冲区。  
* Read 函数返回，文件数据从内核缓冲区 Copy 到用户缓冲区。
* Write 函数调用，将文件数据从用户缓冲区 Copy 到内核与 Socket 相关的缓冲区。
* 数据从 Socket 缓冲区 Copy 到相关协议引擎。

    硬盘—>内核 buf—>用户 buf—>Socket 相关缓冲区—>协议引擎  
    
&emsp; 而 Sendfile 系统调用则提供了一种减少以上多次 Copy，提升文件传输性能的方法。<font color = "red">在内核版本 2.1 中，引入了 Sendfile 系统调用，以简化网络上和两个本地文件之间的数据传输。</font>Sendfile 的引入不仅减少了数据复制，还减少了上下文切换。相较传统 Read/Write 方式，2.1 版本内核引进的 Sendfile 已经减少了内核缓冲区到 User 缓冲区，再由 User 缓冲区到 Socket 相关缓冲区的文件 Copy。而在内核版本 2.4 之后，文件描述符结果被改变，Sendfile 实现了更简单的方式，再次减少了一次 Copy 操作。  
&emsp; Kafka 把所有的消息都存放在一个一个的文件中，当消费者需要数据的时候 Kafka 直接把文件发送给消费者，配合 mmap 作为文件读写方式，直接把它传给 Sendfile。  

## 1.2. kafka高可用性（副本机制）
[kafka副本机制](/docs/microService/mq/kafka/kafkaReplica.md)  

## 1.3. kafka可靠性（如何保证消息队列不丢失?）
<!-- 

Kafka消息中间件到底会不会丢消息 
https://mp.weixin.qq.com/s/uxYUEJRTEIULeObRIl209A
Kafka 无消息丢失配置
https://www.kancloud.cn/nicefo71/kafka/1471586

-->
&emsp; Kafka存在丢消息的问题，消息丢失会发生在Broker，Producer和Consumer三种。  

### 1.3.1. Broker端丢失消息
&emsp; Broker丢失消息是由于Kafka本身的原因造成的，kafka为了得到更高的性能和吞吐量，将数据异步批量的存储在磁盘中。消息的刷盘过程，为了提高性能，减少刷盘次数，kafka采用了批量刷盘的做法。即，按照一定的消息量，和时间间隔进行刷盘。这种机制也是由于linux操作系统决定的。将数据存储到linux操作系统种，会先存储到页缓存（Page cache）中，按照时间或者其他条件进行刷盘（从page cache到file），或者通过fsync命令强制刷盘。数据在page cache中时，如果系统挂掉，数据会丢失。  
&emsp; 上图简述了broker写数据以及同步的一个过程。broker写数据只写到PageCache中，而pageCache位于内存。这部分数据在断电后是会丢失的。pageCache的数据通过linux的flusher程序进行刷盘。刷盘触发条件有三：  

* 主动调用sync或fsync函数
* 可用内存低于阀值
* dirty data时间达到阀值。dirty是pagecache的一个标识位，当有数据写入到pageCache时，pagecache被标注为dirty，数据刷盘以后，dirty标志清除。

&emsp; Broker配置刷盘机制，是通过调用fsync函数接管了刷盘动作。从单个Broker来看，pageCache的数据会丢失。  
&emsp; Kafka没有提供同步刷盘的方式。要完全让kafka保证单个broker不丢失消息是做不到的，只能通过调整刷盘机制的参数缓解该情况。比如，减少刷盘间隔，减少刷盘数据量大小。时间越短，性能越差，可靠性越好（尽可能可靠）。这是一个选择题。  
&emsp; 为了解决该问题，kafka通过producer和broker协同处理单个broker丢失参数的情况。一旦producer发现broker消息丢失，即可自动进行retry。除非retry次数超过阀值（可配置），消息才会丢失。此时需要生产者客户端手动处理该情况。那么producer是如何检测到数据丢失的呢？是通过ack机制。  

#### 1.3.1.1. ACK机制与ISR列表  
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

### 1.3.2. Producer端丢失消息  
&emsp; Producer丢失消息，发生在生产者客户端。  
&emsp; 为了提升效率，减少IO，producer在发送数据时可以将多个请求进行合并后发送。被合并的请求咋发送一线缓存在本地buffer中。缓存的方式和前文提到的刷盘类似，producer可以将请求打包成“块”或者按照时间间隔，将buffer中的数据发出。通过buffer可以将生产者改造为异步的方式，而这可以提升发送效率。  
&emsp; 但是，buffer中的数据就是危险的。在正常情况下，客户端的异步调用可以通过callback来处理消息发送失败或者超时的情况，但是，一旦producer被非法的停止了，那么buffer中的数据将丢失，broker将无法收到该部分数据。又或者，当Producer客户端内存不够时，如果采取的策略是丢弃消息（另一种策略是block阻塞），消息也会被丢失。抑或，消息产生（异步产生）过快，导致挂起线程过多，内存不足，导致程序崩溃，消息丢失。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-73.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-73.png)  
&emsp; 根据上图，可以想到几个解决的思路：  

* 异步发送消息改为同步发送消。或者service产生消息时，使用阻塞的线程池，并且线程数有一定上限。整体思路是控制消息产生速度。  
* 扩大Buffer的容量配置。这种方式可以缓解该情况的出现，但不能杜绝。  
* service不直接将消息发送到buffer（内存），而是将消息写到本地的磁盘中（数据库或者文件），由另一个（或少量）生产线程进行消息发送。相当于是在buffer和service之间又加了一层空间更加富裕的缓冲层。  

### 1.3.3. Consumer 端丢失消息
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

## 1.4. kafka数据一致性，通过HW来保证  
&emsp; 由于并不能保证 Kafka 集群中每时每刻 follower 的长度都和 leader 一致（即数据同步是有时延的），那么当leader 挂掉选举某个 follower 为新的 leader 的时候（原先挂掉的 leader 恢复了成为了 follower），可能会出现leader 的数据比 follower 还少的情况。为了解决这种数据量不一致带来的混乱情况，Kafka 提出了以下概念：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-27.png)  

* LEO（Log End Offset）：指的是每个副本最后一个offset；  
* HW（High Wather）：指的是消费者能见到的最大的 offset，ISR 队列中最小的 LEO。  

&emsp; 消费者和 leader 通信时，只能消费 HW 之前的数据，HW 之后的数据对消费者不可见。  

&emsp; 针对这个规则：  

* 当follower发生故障时：follower 发生故障后会被临时踢出 ISR，待该 follower 恢复后，follower 会读取本地磁盘记录的上次的 HW，并将 log 文件高于 HW 的部分截取掉，从 HW 开始向 leader 进行同步。等该 follower 的 LEO 大于等于该 Partition 的 HW，即 follower 追上 leader 之后，就可以重新加入 ISR 了。
* 当leader发生故障时：leader 发生故障之后，会从 ISR 中选出一个新的 leader，之后，为保证多个副本之间的数据一致性，其余的 follower 会先将各自的 log 文件高于 HW 的部分截掉，然后从新的 leader 同步数据。

&emsp; 所以数据一致性并不能保证数据不丢失或者不重复，这是由 ack 控制的。HW 规则只能保证副本之间的数据一致性！



## 1.5. 如何让 Kafka 的消息有序？  
&emsp; Kafka 无法做到消息全局有序，只能做到 Partition 维度的有序。所以如果想要消息有序，就需要从 Partition 维度入手。一般有两种解决方案。

* 单 Partition，单 Consumer。通过此种方案强制消息全部写入同一个 Partition 内，但是同时也牺牲掉了 Kafka 高吞吐的特性了，所以一般不会采用此方案。  
* 多 Partition，多 Consumer，指定 key 使用特定的 Hash 策略，使其消息落入指定的 Partition 中，从而保证相同的 key 对应的消息是有序的。此方案也是有一些弊端，比如当 Partition 个数发生变化时，相同的 key 对应的消息会落入到其他的 Partition 上，所以一旦确定 Partition 个数后就不能在修改 Partition 个数了。  


## 1.6. 消息重复消费
<!-- 

有哪些情形会造成重复消费？
Rebalance
一个consumer正在消费一个分区的一条消息，还没有消费完，发生了rebalance(加入了一个consumer)，从而导致这条消息没有消费成功，rebalance后，另一个consumer又把这条消息消费一遍。
消费者端手动提交
如果先消费消息，再更新offset位置，导致消息重复消费。
消费者端自动提交
设置offset为自动提交，关闭kafka时，如果在close之前，调用 consumer.unsubscribe() 则有可能部分offset没提交，下次重启会重复消费。
生产者端
生产者因为业务问题导致的宕机，在重启之后可能数据会重发

-->

&emsp; 有遇到过消息重复消费的情况吗？是怎么解决的？  
&emsp; 有，发生过两次重复消费的情况。发现用户的"xx"计数偶现大于实际情况，排查日志发现大概意思是心跳检测异常导致 commit 还没有来得及提交，对应的 Partition 被重新分配给其他的 Consumer 消费导致消息被重复消费。   

1. 解决方式 1：调整降低消费端的消费速率、提高心跳检测周期。  
&emsp; 通过方案 1 调整参数后，还是会出现重复消费的情况，只是出现的概率降低了。  
2. 解决方案 2：在业务层增加 Redis，在一定周期内，相同 key 对应的消息认为是同一条，如果 Redis 内不存在则正常消费消费，反之直接抛弃。  

