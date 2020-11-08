<!-- TOC -->

- [1. kafka生产者](#1-kafka生产者)
    - [1.1. 消息发送流程概述](#11-消息发送流程概述)
    - [1.2. 消息发送示例](#12-消息发送示例)
    - [1.3. 源码分析Kafka消息发送流程](#13-源码分析kafka消息发送流程)
        - [1.3.1. Kafka消息发送流程](#131-kafka消息发送流程)
            - [1.3.1.1. doSend](#1311-dosend)
            - [1.3.1.2. RecordAccumulator append 方法详解](#1312-recordaccumulator-append-方法详解)
            - [1.3.1.3. ProducerBatch tryAppend方法详解](#1313-producerbatch-tryappend方法详解)
            - [1.3.1.4. Kafka 消息追加流程图与总结](#1314-kafka-消息追加流程图与总结)
        - [1.3.2. Sender 线程详解](#132-sender-线程详解)
            - [1.3.2.1. Sender#run 方法详解](#1321-senderrun-方法详解)
            - [1.3.2.2. runOnce 详解](#1322-runonce-详解)
                - [1.3.2.2.1. sendProducerData](#13221-sendproducerdata)
                - [1.3.2.2.2. NetworkClient 的 poll 方法](#13222-networkclient-的-poll-方法)
            - [1.3.2.3. run 方法流程图](#1323-run-方法流程图)
        - [1.3.3. RecordAccumulator 核心方法详解](#133-recordaccumulator-核心方法详解)
            - [1.3.3.1. RecordAccumulator 的 ready 方法详解](#1331-recordaccumulator-的-ready-方法详解)
            - [1.3.3.2. RecordAccumulator 的 drain方法详解](#1332-recordaccumulator-的-drain方法详解)

<!-- /TOC -->


# 1. kafka生产者
## 1.1. 消息发送流程概述    
&emsp; 在消息发送的过程中，涉及到了两个线程—-main线程和Sender线程，以及一个线程共享变量——RecordAccumulator。 main 线程将消息发送给RecordAccumulator，Sender线程不断从RecordAccumulator中拉取消息发送到Kafka broker。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-7.png)  

&emsp; Producer 发送消息的过程如下图所示，需要经过拦截器，序列化器和分区器，最终由累加器批量发送至 Broker。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-8.png)  

## 1.2. 消息发送示例  
&emsp; 消息发送示例：  

```java
package persistent.prestige.demo.kafka;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import java.util.Properties;
import java.util.concurrent.Future;
public class KafkaProducerTest {
    public static void main(String[] args){
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092,localhost:9082,localhost:9072,");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<>(props);
        try {
            for (int i = 0; i < 100; i++) {
                Future<RecordMetadata>  future = producer.send(new ProducerRecord<String, String>("TOPIC_ORDER", Integer.toString(i), Integer.toString(i)));
                RecordMetadata recordMetadata = future.get();
                System.out.printf("offset:" + recordMetadata.offset());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }
}
```

&emsp; **<font color = "red">Kafka的生产者有如下三个必选的属性：</font>**  

* bootstrap.servers，指定broker的地址清单
* key.serializer（key 序列化器），必须是一个实现org.apache.kafka.common.serialization.Serializer接口的类，将key序列化成字节数组。注意：key.serializer必须被设置，即使消息中没有指定key。  
* value.serializer（value 序列化器），将value序列化成字节数组

&emsp; **同步方式：**  
&emsp; send()方法，创建完生产者与消息之后就可以发送了，发送消息分为三种：  

* 发送并忘记（send and forget）：producer.send()，默认为异步发送，并不关心消息是否达到服务端，会存在消息丢失的问题。
* 同步：producer.send()返回一个Future对象，调用get()方法变回进行同步等待，就知道消息是否发送成功。
* 异步发送：如果消息都进行同步发送，要发送这次的消息需要等到上次的消息成功发送到服务端，这样整个消息发送的效率就很低了。kafka支持producer.send()传入一个回调函数，消息不管成功或者失败都会调用这个回调函数，这样就算是异步发送，我们也知道消息的发送情况，然后再回调函数中选择记录日志还是重试都取决于调用方。Future\<RecordMetadata> send(ProducerRecord\<K, V> record, Callback callback);

&emsp; **分区策略**  
<!-- https://mp.weixin.qq.com/s/OB-ZVy70vHClCtep43gr_A 
&emsp; kafka会将一个topic划分成n个分区，那么在生产者发送消息的时候是怎么知道要发给哪个分区的呢。上面说过生产者会拿到整个集群的信息，所以生产者知道每个topic下面有一个分区，基于此可以有些常见的消息分区策略：  

* 轮询：依次将消息发送该topic下的所有分区，如果在创建消息的时候key为null，kafka默认采用这种策略。  
* key指定分区：在创建消息是key不为空，并且使用默认分区器，kafka会将key进行hash，然后根据hash值隐射到指定的分区上。这样的好处是key相同的消息会在一个分区下，kafka并不能保证全局有序，但是在每个分区下的消息是有序的，按照顺序存储，按照顺序消费。在保证同一个key的消息是有序的，这样基本能满足消息的顺序性的需求。  
* 自定义策略：实现Partitioner接口就能自定义分区策略。  
-->
&emsp; producer 发送消息到 broker 时，会根据分区算法选择将其存储到哪一个 partition。其路由机制为：  
1. 指定了 patition，则直接使用；  
2. 未指定 patition 但指定 key，通过对 key 的 value 进行hash 选出一个 patition；  
3. patition 和 key 都未指定，使用轮询选出一个 patition。  

&emsp; 附上 java 客户端分区源码，一目了然：  

```java
//创建消息实例
public ProducerRecord(String topic, Integer partition, Long timestamp, K key, V value) {
     if (topic == null)
          throw new IllegalArgumentException("Topic cannot be null");
     if (timestamp != null && timestamp < 0)
          throw new IllegalArgumentException("Invalid timestamp " + timestamp);
     this.topic = topic;
     this.partition = partition;
     this.key = key;
     this.value = value;
     this.timestamp = timestamp;
}

//计算 patition，如果指定了 patition 则直接使用，否则使用 key 计算
private int partition(ProducerRecord<K, V> record, byte[] serializedKey , byte[] serializedValue, Cluster cluster) {
     Integer partition = record.partition();
     if (partition != null) {
          List<PartitionInfo> partitions = cluster.partitionsForTopic(record.topic());
          int lastPartition = partitions.size() - 1;
          if (partition < 0 || partition > lastPartition) {
               throw new IllegalArgumentException(String.format("Invalid partition given with record: %d is not in the range [0...%d].", partition, lastPartition));
          }
          return partition;
     }
     return this.partitioner.partition(record.topic(), record.key(), serializedKey, record.value(), serializedValue, cluster);
}

// 使用 key 选取 patition
public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
     List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
     int numPartitions = partitions.size();
     if (keyBytes == null) {
          int nextValue = counter.getAndIncrement();
          List<PartitionInfo> availablePartitions = cluster.availablePartitionsForTopic(topic);
          if (availablePartitions.size() > 0) {
               int part = DefaultPartitioner.toPositive(nextValue) % availablePartitions.size();
               return availablePartitions.get(part).partition();
          } else {
               return DefaultPartitioner.toPositive(nextValue) % numPartitions;
          }
     } else {
          //对 keyBytes 进行 hash 选出一个 patition
          return DefaultPartitioner.toPositive(Utils.murmur2(keyBytes)) % numPartitions;
     }
}
```

## 1.3. 源码分析Kafka消息发送流程  
### 1.3.1. Kafka消息发送流程  
&emsp; 可以通过 KafkaProducer 的 send 方法发送消息，send 方法的声明如下：

```java
Future<RecordMetadata> send(ProducerRecord<K, V> record)
Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback)
```
&emsp; 从上面的 API 可以得知，用户在使用 KafkaProducer 发送消息时，首先需要将待发送的消息封装成 ProducerRecord，返回的是一个 Future 对象，典型的 Future 设计模式。在发送时也可以指定一个 Callable 接口用来执行消息发送的回调。  

&emsp; **Kafka消息追加流程**  
&emsp; KafkaProducer 的 send 方法，并不会直接向 broker 发送消息，kafka 将消息发送异步化，即分解成两个步骤，send 方法的职责是将消息追加到内存中(分区的缓存队列中)，然后会由专门的 Send 线程异步将缓存中的消息批量发送到 Kafka Broker 中。

&emsp; 消息追加入口为 KafkaProducer#send

```java
public Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback) {  
    // intercept the record, which can be potentially modified; this method does not throw exceptions
    ProducerRecord<K, V> interceptedRecord = this.interceptors.onSend(record);   // @1
    return doSend(interceptedRecord, callback);    // @2
}
```

&emsp; 代码@1：首先执行消息发送拦截器，拦截器通过 interceptor.classes 指定，类型为 List< String >，每一个元素为拦截器的全类路径限定名。  
&emsp; 代码@2：执行 doSend 方法，后续需要留意一下 Callback 的调用时机。  

#### 1.3.1.1. doSend  
&emsp; KafkaProducer#doSend  

```java
ClusterAndWaitTime clusterAndWaitTime;
try {
    clusterAndWaitTime = waitOnMetadata(record.topic(), record.partition(), maxBlockTimeMs);
} catch (KafkaException e) {
    if (metadata.isClosed())
        throw new KafkaException("Producer closed while send in progress", e);
	throw e;
}
long remainingWaitMs = Math.max(0, maxBlockTimeMs - clusterAndWaitTime.waitedOnMetadataMs);
```
&emsp; Step1：获取 topic 的分区列表，如果本地没有该topic的分区信息，则需要向远端 broker 获取，该方法会返回拉取元数据所耗费的时间。在消息发送时的最大等待时间时会扣除该部分损耗的时间。  

&emsp; KafkaProducer#doSend  

```java
byte[] serializedKey;
try {
    serializedKey = keySerializer.serialize(record.topic(), record.headers(), record.key());
} catch (ClassCastException cce) {
    throw new SerializationException("Can't convert key of class " + record.key().getClass().getName() +
                        " to class " + producerConfig.getClass(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG).getName() +
                        " specified in key.serializer", cce);
}
```
&emsp; Step2：序列化 key。注意：序列化方法虽然有传入 topic、Headers 这两个属性，但参与序列化的只是 key 。

&emsp; KafkaProducer#doSend  

```java
byte[] serializedValue;
try {
    serializedValue = valueSerializer.serialize(record.topic(), record.headers(), record.value());
} catch (ClassCastException cce) {
    throw new SerializationException("Can't convert value of class " + record.value().getClass().getName() +
                        " to class " + producerConfig.getClass(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG).getName() +
                        " specified in value.serializer", cce);
}
```
&emsp; Step3：对消息体内容进行序列化。

&emsp; KafkaProducer#doSend

```java
int partition = partition(record, serializedKey, serializedValue, cluster);
tp = new TopicPartition(record.topic(), partition);
```

&emsp; Step4：根据分区负载算法计算本次消息发送该发往的分区。其默认实现类为 DefaultPartitioner，路由算法如下：

        如果指定了 key ，则使用 key 的 hashcode 与分区数取模。
        如果未指定 key，则轮询所有的分区。

&emsp; KafkaProducer#doSend  

```java
setReadOnly(record.headers());
Header[] headers = record.headers().toArray();
```
&emsp; Step5：如果是消息头信息(RecordHeaders)，则设置为只读。

KafkaProducer#doSend

int serializedSize = AbstractRecords.estimateSizeInBytesUpperBound(apiVersions.maxUsableProduceMagic(),
                    compressionType, serializedKey, serializedValue, headers);
ensureValidRecordSize(serializedSize);



Step5：根据使用的版本号，按照消息协议来计算消息的长度，并是否超过指定长度，如果超过则抛出异常。

KafkaProducer#doSend

long timestamp = record.timestamp() == null ? time.milliseconds() : record.timestamp();
log.trace("Sending record {} with callback {} to topic {} partition {}", record, callback, record.topic(), partition);
Callback interceptCallback = new InterceptorCallback<>(callback, this.interceptors, tp);


Step6：先初始化消息时间戳，并对传入的 Callable(回调函数) 加入到拦截器链中。
KafkaProducer#doSend

if (transactionManager != null && transactionManager.isTransactional())
    transactionManager.maybeAddPartitionToTransaction(tp);



Step7：如果事务处理器不为空，执行事务管理相关的，本节不考虑事务消息相关的实现细节，后续估计会有对应的文章进行解析。

KafkaProducer#doSend

RecordAccumulator.RecordAppendResult result = accumulator.append(tp, timestamp, serializedKey, serializedValue, headers, interceptCallback, remainingWaitMs);
if (result.batchIsFull || result.newBatchCreated) {
    log.trace("Waking up the sender since topic {} partition {} is either full or getting a new batch", record.topic(), partition);
                this.sender.wakeup();
}
return result.future;


Step8：将消息追加到缓存区，这将是本文重点需要探讨的。如果当前缓存区已写满或创建了一个新的缓存区，则唤醒 Sender(消息发送线程)，将缓存区中的消息发送到 broker 服务器，最终返回 future。这里是经典的 Future 设计模式，从这里也能得知，doSend 方法执行完成后，此时消息还不一定成功发送到 broker。

KafkaProducer#doSend  
} catch (ApiException e) {
    log.debug("Exception occurred during message send:", e);
    if (callback != null)
        callback.onCompletion(null, e);
        
	this.errors.record();
    this.interceptors.onSendError(record, tp, e);
        return new FutureFailure(e);
} catch (InterruptedException e) {
    this.errors.record();
    this.interceptors.onSendError(record, tp, e);
    throw new InterruptException(e);
} catch (BufferExhaustedException e) {
    this.errors.record();
    this.metrics.sensor("buffer-exhausted-records").record();
    this.interceptors.onSendError(record, tp, e);
    throw e;
} catch (KafkaException e) {
    this.errors.record();
    this.interceptors.onSendError(record, tp, e);
    throw e;
} catch (Exception e) {
    // we notify interceptor about all exceptions, since onSend is called before anything else in this method
    this.interceptors.onSendError(record, tp, e);
    throw e;
}


Step9：针对各种异常，进行相关信息的收集。

接下来将重点介绍如何将消息追加到生产者的发送缓存区，其实现类为：RecordAccumulator。  

#### 1.3.1.2. RecordAccumulator append 方法详解  
RecordAccumulator#append  

```java
public RecordAppendResult append(TopicPartition tp,
                                     long timestamp,
                                     byte[] key,
                                     byte[] value,
                                     Header[] headers,
                                     Callback callback,
                                     long maxTimeToBlock) throws InterruptedException {
```
在介绍该方法之前，我们首先来看一下该方法的参数。

* TopicPartition tp
    topic 与分区信息，即发送到哪个 topic 的那个分区。
* long timestamp
    客户端发送时的时间戳。
    byte[] key
    消息的 key。
    byte[] value
    消息体。
    Header[] headers
    消息头，可以理解为额外消息属性。
    Callback callback
    回调方法。
* long maxTimeToBlock
    消息追加超时时间。

RecordAccumulator#append

```java
Deque<ProducerBatch> dq = getOrCreateDeque(tp);
synchronized (dq) {
    if (closed)
        throw new KafkaException("Producer closed while send in progress");
    RecordAppendResult appendResult = tryAppend(timestamp, key, value, headers, callback, dq);
    if (appendResult != null)
        return appendResult;
}
```
Step1：尝试根据 topic与分区在 kafka 中获取一个双端队列，如果不存在，则创建一个，然后调用 tryAppend 方法将消息追加到缓存中。Kafka 会为每一个 topic 的每一个分区创建一个消息缓存区，消息先追加到缓存中，然后消息发送 API 立即返回，然后由单独的线程 Sender 将缓存区中的消息定时发送到 broker 。这里的缓存区的实现使用的是 ArrayQeque。然后调用 tryAppend 方法尝试将消息追加到其缓存区，如果追加成功，则返回结果。

在讲解下一个流程之前，我们先来看一下 Kafka 双端队列的存储结构：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-11.png)  
RecordAccumulator#append  

```java
int size = Math.max(this.batchSize, AbstractRecords.estimateSizeInBytesUpperBound(maxUsableMagic, compression, key, value, headers));
log.trace("Allocating a new {} byte message buffer for topic {} partition {}", size, tp.topic(), tp.partition());
buffer = free.allocate(size, maxTimeToBlock);
```
Step2：如果第一步未追加成功，说明当前没有可用的 ProducerBatch，则需要创建一个 ProducerBatch，故先从 BufferPool 中申请 batch.size 的内存空间，为创建 ProducerBatch 做准备，如果由于 BufferPool 中未有剩余内存，则最多等待 maxTimeToBlock ，如果在指定时间内未申请到内存，则抛出异常。  

RecordAccumulator#append  

```java
synchronized (dq) {
    // Need to check if producer is closed again after grabbing the dequeue lock.
    if (closed)
        throw new KafkaException("Producer closed while send in progress");
    // 省略部分代码
    MemoryRecordsBuilder recordsBuilder = recordsBuilder(buffer, maxUsableMagic);
    ProducerBatch batch = new ProducerBatch(tp, recordsBuilder, time.milliseconds());
    FutureRecordMetadata future = Utils.notNull(batch.tryAppend(timestamp, key, value, headers, callback, time.milliseconds()));
    dq.addLast(batch);
    incomplete.add(batch);
    // Don't deallocate this buffer in the finally block as it's being used in the record batch
    buffer = null;
    return new RecordAppendResult(future, dq.size() > 1 || batch.isFull(), true);
}
```
Step3：创建一个新的批次 ProducerBatch，并将消息写入到该批次中，并返回追加结果，这里有如下几个关键点：

* 创建 ProducerBatch ，其内部持有一个 MemoryRecordsBuilder对象，该对象负责将消息写入到内存中，即写入到 ProducerBatch 内部持有的内存，大小等于 batch.size。
* 将消息追加到 ProducerBatch 中。
* 将新创建的 ProducerBatch 添加到双端队列的末尾。
* 将该批次加入到 incomplete 容器中，该容器存放未完成发送到 broker 服务器中的消息批次，当 Sender 线程将消息发送到 broker 服务端后，会将其移除并释放所占内存。
返回追加结果。  

纵观 RecordAccumulator append 的流程，基本上就是从双端队列获取一个未填充完毕的 ProducerBatch（消息批次），然后尝试将其写入到该批次中（缓存、内存中），如果追加失败，则尝试创建一个新的 ProducerBatch 然后继续追加。  

接下来我们继续探究如何向 ProducerBatch 中写入消息。  

#### 1.3.1.3. ProducerBatch tryAppend方法详解  
ProducerBatch #tryAppend  

```java
public FutureRecordMetadata tryAppend(long timestamp, byte[] key, byte[] value, Header[] headers, Callback callback, long now) {
    if (!recordsBuilder.hasRoomFor(timestamp, key, value, headers)) {    // @1
        return null;
    } else {
        Long checksum = this.recordsBuilder.append(timestamp, key, value, headers);    // @2
        this.maxRecordSize = Math.max(this.maxRecordSize, AbstractRecords.estimateSizeInBytesUpperBound(magic(),
                    recordsBuilder.compressionType(), key, value, headers));     // @3
        this.lastAppendTime = now;   //                                                     
        FutureRecordMetadata future = new FutureRecordMetadata(this.produceFuture, this.recordCount,
                                                                   timestamp, checksum,
                                                                   key == null ? -1 : key.length,
                                                                   value == null ? -1 : value.length,
                                                                   Time.SYSTEM);   // @4
        // we have to keep every future returned to the users in case the batch needs to be
        // split to several new batches and resent.
        thunks.add(new Thunk(callback, future));    // @5
        this.recordCount++;
        return future;                                                                            
    }
}
```
代码@1：首先判断 ProducerBatch 是否还能容纳当前消息，如果剩余内存不足，将直接返回 null。如果返回 null ，会尝试再创建一个新的ProducerBatch。  
代码@2：通过 MemoryRecordsBuilder 将消息写入按照 Kafka 消息格式写入到内存中，即写入到 在创建 ProducerBatch 时申请的 ByteBuffer 中。本文先不详细介绍 Kafka 各个版本的消息格式，后续会专门写一篇文章介绍 Kafka 各个版本的消息格式。  
代码@3：更新 ProducerBatch 的 maxRecordSize、lastAppendTime 属性，分别表示该批次中最大的消息长度与最后一次追加消息的时间。  
代码@4：构建 FutureRecordMetadata 对象，这里是典型的 Future模式，里面主要包含了该条消息对应的批次的 produceFuture、消息在该批消息的下标，key 的长度、消息体的长度以及当前的系统时间。  
代码@5：将 callback 、本条消息的凭证(Future) 加入到该批次的 thunks 中，该集合存储了 一个批次中所有消息的发送回执。  
流程执行到这里，KafkaProducer 的 send 方法就执行完毕了，返回给调用方的就是一个 FutureRecordMetadata 对象。  
源码的阅读比较枯燥，接下来用一个流程图简单的阐述一下消息追加的关键要素，重点关注一下各个 Future。代码@1：首先判断 ProducerBatch 是否还能容纳当前消息，如果剩余内存不足，将直接返回 null。如果返回 null ，会尝试再创建一个新的ProducerBatch。  
代码@2：通过 MemoryRecordsBuilder 将消息写入按照 Kafka 消息格式写入到内存中，即写入到 在创建 ProducerBatch 时申请的 ByteBuffer 中。本文先不详细介绍 Kafka 各个版本的消息格式，后续会专门写一篇文章介绍 Kafka 各个版本的消息格式。  
代码@3：更新 ProducerBatch 的 maxRecordSize、lastAppendTime 属性，分别表示该批次中最大的消息长度与最后一次追加消息的时间。  
代码@4：构建 FutureRecordMetadata 对象，这里是典型的 Future模式，里面主要包含了该条消息对应的批次的 produceFuture、消息在该批消息的下标，key 的长度、消息体的长度以及当前的系统时间。  
代码@5：将 callback 、本条消息的凭证(Future) 加入到该批次的 thunks 中，该集合存储了 一个批次中所有消息的发送回执。  
流程执行到这里，KafkaProducer 的 send 方法就执行完毕了，返回给调用方的就是一个 FutureRecordMetadata 对象。  
源码的阅读比较枯燥，接下来用一个流程图简单的阐述一下消息追加的关键要素，重点关注一下各个 Future。  

#### 1.3.1.4. Kafka 消息追加流程图与总结  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-12.png)  
上面的消息发送，其实用消息追加来表达更加贴切，因为 Kafka 的 send 方法，并不会直接向 broker 发送消息，而是首先先追加到生产者的内存缓存中，其内存存储结构如下：ConcurrentMap< TopicPartition, Deque< ProducerBatch>> batches，那我们自然而然的可以得知，Kafka 的生产者为会每一个 topic 的每一个 分区单独维护一个队列，即 ArrayDeque，内部存放的元素为 ProducerBatch，即代表一个批次，即 Kafka 消息发送是按批发送的。其缓存结果图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-13.png)  
KafkaProducer 的 send 方法最终返回的 FutureRecordMetadata ，是 Future 的子类，即 Future 模式。那 kafka 的消息发送怎么实现异步发送、同步发送的呢？  
其实答案也就蕴含在 send 方法的返回值，如果项目方需要使用同步发送的方式，只需要拿到 send 方法的返回结果后，调用其 get() 方法，此时如果消息还未发送到 Broker 上，该方法会被阻塞，等到 broker 返回消息发送结果后该方法会被唤醒并得到消息发送结果。如果需要异步发送，则建议使用 send(ProducerRecord< K, V > record, Callback callback),但不能调用 get 方法即可。Callback 会在收到 broker 的响应结果后被调用，并且支持拦截器。  
消息追加流程就介绍到这里了，消息被追加到缓存区后，什么是会被发送到 broker 端呢？将在下一篇文章中详细介绍。  


### 1.3.2. Sender 线程详解  
KafkaProducer send 方法的流程，该方法只是将消息追加到 KafKaProducer 的缓存中，并未真正的向 broker 发送消息，本节将探讨 Kafka 的 Sender 线程。  

在 KafkaProducer 中会启动一个单独的线程，其名称为 “kafka-producer-network-thread | clientID”，其中 clientID 为生产者的 id 。   

#### 1.3.2.1. Sender#run 方法详解
Sender#run  

```java
public void run() {
    log.debug("Starting Kafka producer I/O thread.");
    while (running) {   
        try {
            runOnce();    // @1
        } catch (Exception e) {
            log.error("Uncaught error in kafka producer I/O thread: ", e);
        }
    }
    log.debug("Beginning shutdown of Kafka producer I/O thread, sending remaining records.");
    while (!forceClose && (this.accumulator.hasUndrained() || this.client.inFlightRequestCount() > 0)) {    // @2
        try {
            runOnce();
        } catch (Exception e) {
            log.error("Uncaught error in kafka producer I/O thread: ", e);
        }
    }
    if (forceClose) {                                                                                                                                     // @3
        log.debug("Aborting incomplete batches due to forced shutdown");
        this.accumulator.abortIncompleteBatches();
    }
    try {
        this.client.close();                                                                                                                               // @4
    } catch (Exception e) {
        log.error("Failed to close network client", e);
    }
    log.debug("Shutdown of Kafka producer I/O thread has completed.");
}
```
代码@1：Sender 线程在运行状态下主要的业务处理方法，将消息缓存区中的消息向 broker 发送。  
代码@2：如果主动关闭 Sender 线程，如果不是强制关闭，则如果缓存区还有消息待发送，再次调用 runOnce 方法将剩余的消息发送完毕后再退出。  
代码@3：如果强制关闭 Sender 线程，则拒绝未完成提交的消息。  
代码@4：关闭 Kafka Client 即网络通信对象。  

接下来将分别探讨其上述方法的实现细节。  

#### 1.3.2.2. runOnce 详解  
Sender#runOnce  

```java
void runOnce() {
	// 此处省略与事务消息相关的逻辑
    long currentTimeMs = time.milliseconds();
    long pollTimeout = sendProducerData(currentTimeMs);   // @1
    client.poll(pollTimeout, currentTimeMs);   // @2
}
```
本文不关注事务消息的实现原理，故省略了该部分的代码。  
代码@1：调用 sendProducerData 方法发送消息。  
代码@2：调用这个方法的作用？  

接下来分别对上述两个方法进行深入探究。  

##### 1.3.2.2.1. sendProducerData
接下来将详细分析其实现步骤。  
Sender#sendProducerData  

```java
Cluster cluster = metadata.fetch();
// get the list of partitions with data ready to send
RecordAccumulator.ReadyCheckResult result = this.accumulator.ready(cluster, now);
```
Step1：首先根据当前时间，根据缓存队列中的数据判断哪些 topic 的 哪些分区已经达到发送条件。达到可发送的条件将在 2.1.1.1 节详细分析。

Sender#sendProducerData

if (!result.unknownLeaderTopics.isEmpty()) {
    for (String topic : result.unknownLeaderTopics)
        this.metadata.add(topic);
    
    log.debug("Requesting metadata update due to unknown leader topics from the batched records: {}",
                result.unknownLeaderTopics);
    this.metadata.requestUpdate();
}


Step2：如果在待发送的消息未找到其路由信息，则需要首先去 broker 服务器拉取对应的路由信息(分区的 leader 节点信息)。
Sender#sendProducerData

long notReadyTimeout = Long.MAX_VALUE;
while (iter.hasNext()) {
    Node node = iter.next();
    if (!this.client.ready(node, now)) {
        iter.remove();
        notReadyTimeout = Math.min(notReadyTimeout, this.client.pollDelayMs(node, now));
    }
}


Step3：移除在网络层面没有准备好的分区，并且计算在接下来多久的时间间隔内，该分区都将处于未准备状态。
1. 在网络环节没有准备好的标准如下：  
    * 分区没有未完成的更新元素数据请求(metadata)。
    * 当前生产者与对端 broker 已建立连接并完成了 TCP 的三次握手。
    * 如果启用 SSL、ACL 等机制，相关状态都已就绪。
    * 该分区对应的连接正在处理中的请求数时是否超过设定值，默认为 5，可通过属性 max.in.flight.requests.per.connection 来设置。
2. client pollDelayMs 预估分区在接下来多久的时间间隔内都将处于未转变好状态(not ready)，其标准如下：
    * 如果已与对端的 TCP 连接已创建好，并处于已连接状态，此时如果没有触发限流，则返回0，如果有触发限流，则返回限流等待时间。
    * 如果还位于对端建立 TCP 连接，则返回 Long.MAX_VALUE，因为连接建立好后，会唤醒发送线程的。

Sender#sendProducerData

// create produce requests
Map<Integer, List<ProducerBatch>> batches = this.accumulator.drain(cluster, result.readyNodes, this.maxRequestSize, now);


Step4：根据已准备的分区，从缓存区中抽取待发送的消息批次(ProducerBatch)，并且按照 nodeId:List 组织，注意，抽取后的 ProducerBatch 将不能再追加消息了，就算还有剩余空间可用，具体抽取将在下文在详细介绍。

Sender#sendProducerData

addToInflightBatches(batches);
public void addToInflightBatches(Map<Integer, List<ProducerBatch>> batches) {
    for (List<ProducerBatch> batchList : batches.values()) {
        addToInflightBatches(batchList);
    }
}
private void addToInflightBatches(List<ProducerBatch> batches) {
    for (ProducerBatch batch : batches) {
        List<ProducerBatch> inflightBatchList = inFlightBatches.get(batch.topicPartition);
        if (inflightBatchList == null) {
            inflightBatchList = new ArrayList<>();
            inFlightBatches.put(batch.topicPartition, inflightBatchList);
        }
        inflightBatchList.add(batch);
    }
}

Step5：将抽取的 ProducerBatch 加入到 inFlightBatches 数据结构，该属性的声明如下：Map<TopicPartition, List< ProducerBatch >> inFlightBatches，即按照 topic-分区 为键，存放已抽取的 ProducerBatch，这个属性的含义就是存储待发送的消息批次。可以根据该数据结构得知在消息发送时以分区为维度反馈 Sender 线程的“积压情况”，max.in.flight.requests.per.connection 就是来控制积压的最大数量，如果积压达到这个数值，针对该队列的消息发送会限流。

Sender#sendProducerData
```java
accumulator.resetNextBatchExpiryTime();
List<ProducerBatch> expiredInflightBatches = getExpiredInflightBatches(now);
List<ProducerBatch> expiredBatches = this.accumulator.expiredBatches(now);
expiredBatches.addAll(expiredInflightBatches);
```

Step6：从 inflightBatches 与 batches 中查找已过期的消息批次(ProducerBatch)，判断是否过期的标准是系统当前时间与 ProducerBatch 创建时间之差是否超过120s，过期时间可以通过参数 delivery.timeout.ms 设置。

Sender#sendProducerData

if (!expiredBatches.isEmpty())
    log.trace("Expired {} batches in accumulator", expiredBatches.size());
for (ProducerBatch expiredBatch : expiredBatches) {
    String errorMessage = "Expiring " + expiredBatch.recordCount + " record(s) for " + expiredBatch.topicPartition
                + ":" + (now - expiredBatch.createdMs) + " ms has passed since batch creation";
    failBatch(expiredBatch, -1, NO_TIMESTAMP, new TimeoutException(errorMessage), false);
    if (transactionManager != null && expiredBatch.inRetry()) {
        // This ensures that no new batches are drained until the current in flight batches are fully resolved.
        transactionManager.markSequenceUnresolved(expiredBatch.topicPartition);
    }
}

Step7：处理已超时的消息批次，通知该批消息发送失败，即通过设置 KafkaProducer#send 方法返回的凭证中的 FutureRecordMetadata 中的 ProduceRequestResult result，使之调用其 get 方法不会阻塞。  

Sender#sendProducerData

sensors.updateProduceRequestMetrics(batches);

Step8：收集统计指标，本文不打算详细分析，但后续会专门对 Kafka 的 Metrics 设计进行一个深入的探讨与学习。

Sender#sendProducerData

long pollTimeout = Math.min(result.nextReadyCheckDelayMs, notReadyTimeout);
pollTimeout = Math.min(pollTimeout, this.accumulator.nextExpiryTimeMs() - now);
pollTimeout = Math.max(pollTimeout, 0);
if (!result.readyNodes.isEmpty()) {
    log.trace("Nodes with data ready to send: {}", result.readyNodes);
    pollTimeout = 0;
}


Step9：设置下一次的发送延时，待补充详细分析。
Sender#sendProducerData

sendProduceRequests(batches, now);
private void sendProduceRequests(Map<Integer, List<ProducerBatch>> collated, long now) {
    for (Map.Entry<Integer, List<ProducerBatch>> entry : collated.entrySet())
        sendProduceRequest(now, entry.getKey(), acks, requestTimeoutMs, entry.getValue());
}

Step10：该步骤按照 brokerId 分别构建发送请求，即每一个 broker 会将多个 ProducerBatch 一起封装成一个请求进行发送，同一时间，每一个 与 broker 连接只会只能发送一个请求，注意，这里只是构建请求，并最终会通过 NetworkClient#send 方法，将该批数据设置到 NetworkClient 的待发送数据中，此时并没有触发真正的网络调用。

sendProducerData 方法就介绍到这里了，既然这里还没有进行真正的网络请求，那在什么时候触发呢？

我们继续回到 runOnce 方法。  

##### 1.3.2.2.2. NetworkClient 的 poll 方法  

```java
 public List<ClientResponse> poll(long timeout, long now) {
    ensureActive();

    if (!abortedSends.isEmpty()) {
        // If there are aborted sends because of unsupported version exceptions or disconnects,
        // handle them immediately without waiting for Selector#poll.
        List<ClientResponse> responses = new ArrayList<>();
        handleAbortedSends(responses);
        completeResponses(responses);
        return responses;
    }

    long metadataTimeout = metadataUpdater.maybeUpdate(now);   // @1
    try {
        this.selector.poll(Utils.min(timeout, metadataTimeout, defaultRequestTimeoutMs));  // @2
    } catch (IOException e) {
        log.error("Unexpected error during I/O", e);
    }

    // process completed actions
    long updatedNow = this.time.milliseconds();
    List<ClientResponse> responses = new ArrayList<>();   // @3
    handleCompletedSends(responses, updatedNow);
    handleCompletedReceives(responses, updatedNow);
    handleDisconnections(responses, updatedNow);
    handleConnections();
    handleInitiateApiVersionRequests(updatedNow);
    handleTimedOutRequests(responses, updatedNow);
    completeResponses(responses);   // @4
    return responses;
}
```
本文并不会详细深入探讨其网络实现部分，Kafka 的 网络通讯后续我会专门详细的介绍，在这里先点出其关键点。  
代码@1：尝试更新云数据。  
代码@2：触发真正的网络通讯，该方法中会通过收到调用 NIO 中的 Selector#select() 方法，对通道的读写就绪事件进行处理，当写事件就绪后，就会将通道中的消息发送到远端的 broker。  
代码@3：然后会消息发送，消息接收、断开连接、API版本，超时等结果进行收集。  
代码@4：并依次对结果进行唤醒，此时会将响应结果设置到 KafkaProducer#send 方法返回的凭证中，从而唤醒发送客户端，完成一次完整的消息发送流程。  

Sender 发送线程的流程就介绍到这里了，接下来首先给出一张流程图，然后对上述流程中一些关键的方法再补充深入探讨一下。  

#### 1.3.2.3. run 方法流程图  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-14.png)  
根据上面的源码分析得出上述流程图，图中对重点步骤也详细标注了其关键点。下面我们对上述流程图中 Sender 线程依赖的相关类的核心方法进行解读，以便加深 Sender 线程的理解。  

由于在讲解 Sender 发送流程中，大部分都是调用 RecordAccumulator 方法来实现其特定逻辑，故接下来重点对上述涉及到RecordAccumulator 的方法进行一个详细剖析，加强对 Sender 流程的理解。  

### 1.3.3. RecordAccumulator 核心方法详解  
#### 1.3.3.1. RecordAccumulator 的 ready 方法详解  
该方法主要就是根据缓存区中的消息，判断哪些分区已经达到发送条件。  
RecordAccumulator#ready  

```java
public ReadyCheckResult ready(Cluster cluster, long nowMs) {
    Set<Node> readyNodes = new HashSet<>();
    long nextReadyCheckDelayMs = Long.MAX_VALUE;
    Set<String> unknownLeaderTopics = new HashSet<>();

    boolean exhausted = this.free.queued() > 0;
    for (Map.Entry<TopicPartition, Deque<ProducerBatch>> entry : this.batches.entrySet()) {   // @1
        TopicPartition part = entry.getKey();
        Deque<ProducerBatch> deque = entry.getValue();

        Node leader = cluster.leaderFor(part);   // @2
        synchronized (deque) {
            if (leader == null && !deque.isEmpty()) {   // @3
                // This is a partition for which leader is not known, but messages are available to send.
                // Note that entries are currently not removed from batches when deque is empty.
                unknownLeaderTopics.add(part.topic());
            } else if (!readyNodes.contains(leader) && !isMuted(part, nowMs)) {    // @4
                ProducerBatch batch = deque.peekFirst();
                if (batch != null) {
                    long waitedTimeMs = batch.waitedTimeMs(nowMs);
                    boolean backingOff = batch.attempts() > 0 && waitedTimeMs < retryBackoffMs;
                    long timeToWaitMs = backingOff ? retryBackoffMs : lingerMs;
                    boolean full = deque.size() > 1 || batch.isFull();
                    boolean expired = waitedTimeMs >= timeToWaitMs;
                    boolean sendable = full || expired || exhausted || closed || flushInProgress();
                    if (sendable && !backingOff) {   // @5
                        readyNodes.add(leader);
                    } else {
                        long timeLeftMs = Math.max(timeToWaitMs - waitedTimeMs, 0);
                        // Note that this results in a conservative estimate since an un-sendable partition may have
                        // a leader that will later be found to have sendable data. However, this is good enough
                        // since we'll just wake up and then sleep again for the remaining time.
                        nextReadyCheckDelayMs = Math.min(timeLeftMs, nextReadyCheckDelayMs);   
                    }
                }
            }
        }
    }
    return new ReadyCheckResult(readyNodes, nextReadyCheckDelayMs, unknownLeaderTopics);
}
```

代码@1：对生产者缓存区 ConcurrentHashMap\<TopicPartition, Deque\< ProducerBatch>> batches 遍历，从中挑选已准备好的消息批次。  
代码@2：从生产者元数据缓存中尝试查找分区(TopicPartition) 的 leader 信息，如果不存在，当将该 topic 添加到 unknownLeaderTopics (代码@3)，稍后会发送元数据更新请求去 broker 端查找分区的路由信息。  
代码@4：如果不在 readyNodes 中就需要判断是否满足条件，isMuted 与顺序消息有关，本文暂时不关注，在后面的顺序消息部分会重点探讨。  
代码@5：这里就是判断是否准备好的条件，先一个一个来解读局部变量的含义。  

long waitedTimeMs
该 ProducerBatch 已等待的时长，等于当前时间戳 与 ProducerBatch 的 lastAttemptMs 之差，在 ProducerBatch 创建时或需要重试时会将当前的时间赋值给lastAttemptMs。
retryBackoffMs
当发生异常时发起重试之前的等待时间，默认为 100ms，可通过属性 retry.backoff.ms 配置。
batch.attempts()
该批次当前已重试的次数。
backingOff
后台发送是否关闭，即如果需要重试并且等待时间小于 retryBackoffMs ，则 backingOff = true，也意味着该批次未准备好。
timeToWaitMs
send 线程发送消息需要的等待时间，如果 backingOff 为 true，表示该批次是在重试，并且等待时间小于系统设置的需要等待时间，这种情况下 timeToWaitMs = retryBackoffMs 。否则需要等待的时间为 lingerMs。
boolean full
该批次是否已满，如果两个条件中的任意一个满足即为 true。

    Deque< ProducerBatch> 该队列的个数大于1，表示肯定有一个 ProducerBatch 已写满。
    ProducerBatch 已写满。

boolean expired
是否过期，等于已经等待的时间是否大于需要等待的时间，如果把发送看成定时发送的话，expired 为 true 表示定时器已到达触发点，即需要执行。
boolean exhausted
当前生产者缓存已不够，创建新的 ProducerBatch 时阻塞在申请缓存空间的线程大于0，此时应立即将缓存区中的消息立即发送到服务器。
boolean sendable 
是否可发送。其满足下面的任意一个条件即可：

    该批次已写满。(full = true)。
    已等待系统规定的时长。（expired = true）
    发送者内部缓存区已耗尽并且有新的线程需要申请(exhausted = true)。
    该发送者的 close 方法被调用(close = true)。
    该发送者的 flush 方法被调用。  

#### 1.3.3.2. RecordAccumulator 的 drain方法详解  
RecordAccumulator#drain  

```java
public Map<Integer, List<ProducerBatch>> drain(Cluster cluster, Set<Node> nodes, int maxSize, long now) { // @1
    if (nodes.isEmpty())
        return Collections.emptyMap();

    Map<Integer, List<ProducerBatch>> batches = new HashMap<>();
    for (Node node : nodes) {                                                                                                                              
        List<ProducerBatch> ready = drainBatchesForOneNode(cluster, node, maxSize, now);    // @2
        batches.put(node.id(), ready);
    }
    return batches;
}
```
代码@1：我们首先来介绍该方法的参数：

    Cluster cluster
    集群信息。
    Set< Node> nodes
    已准备好的节点集合。
    int maxSize
    一次请求最大的字节数。
    long now
    当前时间。

代码@2：遍历所有节点，调用 drainBatchesForOneNode 方法抽取数据，组装成 Map\<Integer /** brokerId */, List< ProducerBatch>> batches。

接下来重点来看一下 drainBatchesForOneNode。
RecordAccumulator#drainBatchesForOneNode  

```java
private List<ProducerBatch> drainBatchesForOneNode(Cluster cluster, Node node, int maxSize, long now) {
    int size = 0;
    List<PartitionInfo> parts = cluster.partitionsForNode(node.id());   // @1
    List<ProducerBatch> ready = new ArrayList<>();
    int start = drainIndex = drainIndex % parts.size();   // @2
    do {    // @3 
        PartitionInfo part = parts.get(drainIndex);
        TopicPartition tp = new TopicPartition(part.topic(), part.partition()); 
        this.drainIndex = (this.drainIndex + 1) % parts.size();                     
            
        if (isMuted(tp, now))
            continue;

        Deque<ProducerBatch> deque = getDeque(tp);    // @4
        if (deque == null)
            continue;

        synchronized (deque) {
            // invariant: !isMuted(tp,now) && deque != null
            ProducerBatch first = deque.peekFirst();    // @5
            if (first == null)
                continue;

            // first != null
            boolean backoff = first.attempts() > 0 && first.waitedTimeMs(now) < retryBackoffMs;   // @6
            // Only drain the batch if it is not during backoff period.
            if (backoff)                                                                                     
                continue;

            if (size + first.estimatedSizeInBytes() > maxSize && !ready.isEmpty()) {    // @7
                break;
            } else {
                if (shouldStopDrainBatchesForPartition(first, tp))                                  
                    break;

                // 这里省略与事务消息相关的代码，后续会重点学习。
                batch.close();   // @8
                size += batch.records().sizeInBytes();
                ready.add(batch);                                                                            

                batch.drained(now);                                                                             
            }
        }
    } while (start != drainIndex);
    return ready;
}
```
代码@1：根据 brokerId 获取该 broker 上的所有主分区。
代码@2：初始化 start。这里首先来阐述一下 start 与 drainIndex 。

    start 当前开始遍历的分区序号。
    drainIndex 上次抽取的队列索引后，这里主要是为了每个队列都是从零号分区开始抽取。

代码@3：循环从缓存区抽取对应分区中累积的数据。
代码@4：根据 topic + 分区号从生产者发送缓存区中获取已累积的双端Queue。
代码@5：从双端队列的头部获取一个元素。（消息追加时是追加到队列尾部）。
代码@6：如果当前批次是重试，并且还未到阻塞时间，则跳过该分区。
代码@7：如果当前已抽取的消息总大小 加上新的消息已超过 maxRequestSize，则结束抽取。
代码@8：将当前批次加入到已准备集合中，并关闭该批次，即不在允许向该批次中追加消息。  

关于消息发送就介绍到这里，NetworkClient 的 poll 方法内部会调用 Selector 执行就绪事件的选择，并将抽取的消息通过网络发送到 Broker 服务器，关于网络后面的具体实现，将在后续文章中单独介绍。  
