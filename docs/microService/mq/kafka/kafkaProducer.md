<!-- TOC -->

- [1. kafka生产者](#1-kafka生产者)
    - [1.1. 消息发送示例](#11-消息发送示例)
    - [1.2. 消息发送流程概述](#12-消息发送流程概述)
    - [1.3. main线程详解](#13-main线程详解)
        - [1.3.1. doSend](#131-dosend)
            - [1.3.1.1. RecordAccumulator#append方法详解](#1311-recordaccumulatorappend方法详解)
                - [1.3.1.1.1. ProducerBatch tryAppend方法详解](#13111-producerbatch-tryappend方法详解)
        - [1.3.2. Kafka 消息追加流程图与总结](#132-kafka-消息追加流程图与总结)
    - [1.4. Sender 线程详解](#14-sender-线程详解)
        - [1.4.1. Sender#run方法详解](#141-senderrun方法详解)
            - [1.4.1.1. runOnce 详解](#1411-runonce-详解)
                - [1.4.1.1.1. sendProducerData](#14111-sendproducerdata)
                    - [1.4.1.1.1.1. RecordAccumulator#ready](#141111-recordaccumulatorready)
                    - [1.4.1.1.1.2. RecordAccumulator#drain](#141112-recordaccumulatordrain)
                    - [1.4.1.1.1.3. Sender#sendProduceRequests](#141113-sendersendproducerequests)
                - [1.4.1.1.2. NetworkClient 的 poll 方法](#14112-networkclient-的-poll-方法)
        - [1.4.2. run方法流程图](#142-run方法流程图)

<!-- /TOC -->


# 1. kafka生产者
<!-- 
https://www.cnblogs.com/dennyzhangdd/p/7827564.html
-->
&emsp; 源码地址：https://kafka.apachecn.org/downloads.html  

## 1.1. 消息发送示例        
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

* bootstrap.servers，指定broker的地址清单。  
* key.serializer（key 序列化器），必须是一个实现org.apache.kafka.common.serialization.Serializer接口的类，将key序列化成字节数组。注意：key.serializer必须被设置，即使消息中没有指定key。  
* value.serializer（value 序列化器），将value序列化成字节数组。  

&emsp; **同步方式：**  
&emsp; send()方法，创建完生产者与消息之后就可以发送了，发送消息分为三种：  

* 发送并忘记（send and forget）：producer.send()，默认为异步发送，并不关心消息是否达到服务端，会存在消息丢失的问题。
* 同步：producer.send()返回一个Future对象，调用get()方法变回进行同步等待，就知道消息是否发送成功。
* 异步发送：如果消息都进行同步发送，要发送这次的消息需要等到上次的消息成功发送到服务端，这样整个消息发送的效率就很低了。kafka支持producer.send()传入一个回调函数，消息不管成功或者失败都会调用这个回调函数，这样就算是异步发送，也知道消息的发送情况，然后再回调函数中选择记录日志还是重试都取决于调用方。Future\<RecordMetadata> send(ProducerRecord\<K, V> record, Callback callback);  

## 1.2. 消息发送流程概述    
<!-- 
《深入理解kafka》2.2.1
-->

&emsp; **在消息发送的过程中，涉及到了两个线程：Main线程和Sender线程，以及一个线程共享变量RecordAccumulator（消息累加器）。 main线程将消息发送给RecordAccumulator，Sender线程不断从RecordAccumulator中拉取消息发送到Kafka broker。<font color = "red">其中主线程发送消息的过程如下图所示，需要经过拦截器，序列化器和分区器，最终由累加器批量发送至Broker。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-7.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-8.png)  

## 1.3. main线程详解   
&emsp; Kafka生产者示例中通过KafkaProducer的send方法发送消息，send方法的声明如下：

```java
Future<RecordMetadata> send(ProducerRecord<K, V> record)
Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback)
```
&emsp; 从上面的API可以得知，用户在使用KafkaProducer发送消息时，首先需要将待发送的消息封装成ProducerRecord，返回的是一个Future对象，典型的Future设计模式。在发送时也可以指定一个Callable接口用来执行消息发送的回调。  
<!-- 
&emsp; **Kafka消息追加流程**  
&emsp; KafkaProducer的send方法，并不会直接向broker发送消息，kafka将消息发送异步化，即分解成两个步骤，send方法的职责是将消息追加到内存中(分区的缓存队列中)，然后会由专门的Send线程异步将缓存中的消息批量发送到Kafka Broker中。
-->
```java
public Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback) {  
    // intercept the record, which can be potentially modified; this method does not throw exceptions
    // @1 首先执行消息发送拦截器，拦截器通过 interceptor.classes 指定，类型为 List< String >，每一个元素为拦截器的全类路径限定名。
    ProducerRecord<K, V> interceptedRecord = this.interceptors.onSend(record);   
    // @2 执行 doSend 方法，后续需要留意一下 Callback 的调用时机。
    return doSend(interceptedRecord, callback);    
}
```

### 1.3.1. doSend  
&emsp; KafkaProducer#doSend  

```java
/**
 * Implementation of asynchronously send a record to a topic.
 */
private Future<RecordMetadata> doSend(ProducerRecord<K, V> record, Callback callback) {
    TopicPartition tp = null;
    try {
        // first make sure the metadata for the topic is available
        //Step1：获取 topic 的分区列表，如果本地没有该topic的分区信息，则需要向远端 broker 获取，该方法会返回拉取元数据所耗费的时间。在消息发送时的最大等待时间时会扣除该部分损耗的时间。
        ClusterAndWaitTime clusterAndWaitTime = waitOnMetadata(record.topic(), record.partition(), maxBlockTimeMs);
        long remainingWaitMs = Math.max(0, maxBlockTimeMs - clusterAndWaitTime.waitedOnMetadataMs);
        Cluster cluster = clusterAndWaitTime.cluster;
        byte[] serializedKey;
        try {
            //Step2：序列化 key。注意：序列化方法虽然有传入 topic、Headers 这两个属性，但参与序列化的只是 key 。
            serializedKey = keySerializer.serialize(record.topic(), record.key());
        } catch (ClassCastException cce) {
            throw new SerializationException("Can't convert key of class " + record.key().getClass().getName() +
                    " to class " + producerConfig.getClass(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG).getName() +
                    " specified in key.serializer");
        }
        byte[] serializedValue;
        try {
            //Step3：对消息体内容进行序列化。
            serializedValue = valueSerializer.serialize(record.topic(), record.value());
        } catch (ClassCastException cce) {
            throw new SerializationException("Can't convert value of class " + record.value().getClass().getName() +
                    " to class " + producerConfig.getClass(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG).getName() +
                    " specified in value.serializer");
        }
        //Step4：根据分区负载算法计算本次消息发送该发往的分区。其默认实现类为 DefaultPartitioner，路由算法如下：
        //  如果指定了 key ，则使用 key 的 hashcode 与分区数取模。
        //  如果未指定 key，则轮询所有的分区。
        int partition = partition(record, serializedKey, serializedValue, cluster);
        int serializedSize = Records.LOG_OVERHEAD + Record.recordSize(serializedKey, serializedValue);
        ensureValidRecordSize(serializedSize);
        tp = new TopicPartition(record.topic(), partition);
        //Step5：根据使用的版本号，按照消息协议来计算消息的长度，并是否超过指定长度，如果超过则抛出异常。
        long timestamp = record.timestamp() == null ? time.milliseconds() : record.timestamp();
        log.trace("Sending record {} with callback {} to topic {} partition {}", record, callback, record.topic(), partition);
        // producer callback will make sure to call both 'callback' and interceptor callback
        //Step6：先初始化消息时间戳，并对传入的 Callable(回调函数) 加入到拦截器链中。
        Callback interceptCallback = this.interceptors == null ? callback : new InterceptorCallback<>(callback, this.interceptors, tp);

        //Step7：如果事务处理器不为空，执行事务管理相关的，本节不考虑事务消息相关的实现细节，后续估计会有对应的文章进行解析。
        if (transactionManager != null && transactionManager.isTransactional())
            transactionManager.maybeAddPartitionToTransaction(tp);
        //Step8：将消息追加到缓存区，这将是本文重点需要探讨的。如果当前缓存区已写满或创建了一个新的缓存区，则唤醒 Sender(消息发送线程)，将缓存区中的消息发送到 broker 服务器，最终返回 future。这里是经典的 Future 设计模式，从这里也能得知，doSend 方法执行完成后，此时消息还不一定成功发送到 broker。
        RecordAccumulator.RecordAppendResult result = accumulator.append(tp, timestamp, serializedKey, serializedValue, interceptCallback, remainingWaitMs);
        if (result.batchIsFull || result.newBatchCreated) {
            log.trace("Waking up the sender since topic {} partition {} is either full or getting a new batch", record.topic(), partition);
            this.sender.wakeup();
        }
        return result.future;
        // handling exceptions and record the errors;
        // for API exceptions return them in the future,
        // for other exceptions throw directly
    } catch (ApiException e) {
        //Step9：针对各种异常，进行相关信息的收集。
        log.debug("Exception occurred during message send:", e);
        if (callback != null)
            callback.onCompletion(null, e);
        this.errors.record();
        if (this.interceptors != null)
            this.interceptors.onSendError(record, tp, e);
        return new FutureFailure(e);
    } catch (InterruptedException e) {
        this.errors.record();
        if (this.interceptors != null)
            this.interceptors.onSendError(record, tp, e);
        throw new InterruptException(e);
    } catch (BufferExhaustedException e) {
        this.errors.record();
        this.metrics.sensor("buffer-exhausted-records").record();
        if (this.interceptors != null)
            this.interceptors.onSendError(record, tp, e);
        throw e;
    } catch (KafkaException e) {
        this.errors.record();
        if (this.interceptors != null)
            this.interceptors.onSendError(record, tp, e);
        throw e;
    } catch (Exception e) {
        // we notify interceptor about all exceptions, since onSend is called before anything else in this method
        if (this.interceptors != null)
            this.interceptors.onSendError(record, tp, e);
        throw e;
    }
}
```

#### 1.3.1.1. RecordAccumulator#append方法详解  
&emsp; 接下来将重点介绍step8如何将消息追加到生产者的发送缓存区，其实现类为：RecordAccumulator。  
&emsp; 纵观 RecordAccumulator append 的流程，基本上就是从双端队列获取一个未填充完毕的 ProducerBatch（消息批次），然后尝试将其写入到该批次中（缓存、内存中），如果追加失败，则尝试创建一个新的ProducerBatch然后继续追加。Kafka双端队列的存储结构：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-11.png)  

&emsp; RecordAccumulator#append  

```java
/**
 * Add a record to the accumulator, return the append result
 * <p>
 * The append result will contain the future metadata, and flag for whether the appended batch is full or a new batch is created
 * <p>
 *
 * @param tp topic 与分区信息，即发送到哪个 topic 的那个分区
 * @param timestamp 客户端发送时的时间戳
 * @param key 消息的 key
 * @param value 消息体
 * @param callback 回调方法
 * @param maxTimeToBlock 消息追加超时时间
 */
public RecordAppendResult append(TopicPartition tp,
                                 long timestamp,
                                 byte[] key,
                                 byte[] value,
                                 Callback callback,
                                 long maxTimeToBlock) throws InterruptedException {
    // Step1：尝试根据 topic与分区在 kafka 中获取一个双端队列，如果不存在，则创建一个，然后调用 tryAppend 方法将消息追加到缓存中。
    // Kafka 会为每一个 topic 的每一个分区创建一个消息缓存区，消息先追加到缓存中，然后消息发送 API 立即返回，然后由单独的线程 Sender 将缓存区中的消息定时发送到 broker 。
    // 这里的缓存区的实现使用的是 ArrayQeque。然后调用 tryAppend 方法尝试将消息追加到其缓存区，如果追加成功，则返回结果。
    appendsInProgress.incrementAndGet();
    try {
        // check if we have an in-progress batch
        Deque<RecordBatch> dq = getOrCreateDeque(tp);
        synchronized (dq) {
            if (closed)
                throw new IllegalStateException("Cannot send after the producer is closed.");
            RecordAppendResult appendResult = tryAppend(timestamp, key, value, callback, dq);
            if (appendResult != null)
                return appendResult;
        }

        //Step2：如果第一步未追加成功，说明当前没有可用的 ProducerBatch，则需要创建一个 ProducerBatch，故先从 BufferPool 中申请 batch.size 的内存空间，为创建 ProducerBatch 做准备，如果由于 BufferPool 中未有剩余内存，则最多等待 maxTimeToBlock ，如果在指定时间内未申请到内存，则抛出异常。
        int size = Math.max(this.batchSize, Records.LOG_OVERHEAD + Record.recordSize(key, value));
        log.trace("Allocating a new {} byte message buffer for topic {} partition {}", size, tp.topic(), tp.partition());
        ByteBuffer buffer = free.allocate(size, maxTimeToBlock);
        //Step3：创建一个新的批次 ProducerBatch，并将消息写入到该批次中，并返回追加结果，这里有如下几个关键点：
        //
        //* 创建 ProducerBatch ，其内部持有一个 MemoryRecordsBuilder对象，该对象负责将消息写入到内存中，即写入到 ProducerBatch 内部持有的内存，大小等于 batch.size。
        //* 将消息追加到 ProducerBatch 中。
        //* 将新创建的 ProducerBatch 添加到双端队列的末尾。
        //* 将该批次加入到 incomplete 容器中，该容器存放未完成发送到 broker 服务器中的消息批次，当 Sender 线程将消息发送到 broker 服务端后，会将其移除并释放所占内存。
        //返回追加结果。
        synchronized (dq) {
            // Need to check if producer is closed again after grabbing the dequeue lock.
            if (closed)
                throw new IllegalStateException("Cannot send after the producer is closed.");

            RecordAppendResult appendResult = tryAppend(timestamp, key, value, callback, dq);
            if (appendResult != null) {
                // Somebody else found us a batch, return the one we waited for! Hopefully this doesn't happen often...
                free.deallocate(buffer);
                return appendResult;
            }
            MemoryRecords records = MemoryRecords.emptyRecords(buffer, compression, this.batchSize);
            RecordBatch batch = new RecordBatch(tp, records, time.milliseconds());
            FutureRecordMetadata future = Utils.notNull(batch.tryAppend(timestamp, key, value, callback, time.milliseconds()));

            dq.addLast(batch);
            incomplete.add(batch);
            return new RecordAppendResult(future, dq.size() > 1 || batch.records.isFull(), true);
        }
    } finally {
        appendsInProgress.decrementAndGet();
    }
}
```

##### 1.3.1.1.1. ProducerBatch tryAppend方法详解  
&emsp; 接下来继续探究如何向 ProducerBatch 中写入消息。  
&emsp; ProducerBatch #tryAppend  

```java
public FutureRecordMetadata tryAppend(long timestamp, byte[] key, byte[] value, Header[] headers, Callback callback, long now) {
    //首先判断 ProducerBatch 是否还能容纳当前消息，如果剩余内存不足，将直接返回 null。如果返回 null ，会尝试再创建一个新的ProducerBatch。
    if (!recordsBuilder.hasRoomFor(timestamp, key, value, headers)) {    // @1
        return null;
    } else {
        //通过 MemoryRecordsBuilder 将消息写入按照 Kafka 消息格式写入到内存中，即写入到 在创建 ProducerBatch 时申请的 ByteBuffer 中。本文先不详细介绍 Kafka 各个版本的消息格式，后续会专门写一篇文章介绍 Kafka 各个版本的消息格式。
        Long checksum = this.recordsBuilder.append(timestamp, key, value, headers);    // @2
        //更新 ProducerBatch 的 maxRecordSize、lastAppendTime 属性，分别表示该批次中最大的消息长度与最后一次追加消息的时间。
        this.maxRecordSize = Math.max(this.maxRecordSize, AbstractRecords.estimateSizeInBytesUpperBound(magic(),
                recordsBuilder.compressionType(), key, value, headers));     // @3
        this.lastAppendTime = now;   //
        //构建 FutureRecordMetadata 对象，这里是典型的 Future模式，里面主要包含了该条消息对应的批次的 produceFuture、消息在该批消息的下标，key 的长度、消息体的长度以及当前的系统时间。
        FutureRecordMetadata future = new FutureRecordMetadata(this.produceFuture, this.recordCount,
                timestamp, checksum,
                key == null ? -1 : key.length,
                value == null ? -1 : value.length,
                Time.SYSTEM);   // @4
        // we have to keep every future returned to the users in case the batch needs to be
        // split to several new batches and resent.
        //将 callback 、本条消息的凭证(Future) 加入到该批次的 thunks 中，该集合存储了 一个批次中所有消息的发送回执。
        thunks.add(new Thunk(callback, future));    // @5
        this.recordCount++;
        return future;
    }
}
```
&emsp; 流程执行到这里，KafkaProducer 的 send 方法就执行完毕了，返回给调用方的就是一个 FutureRecordMetadata 对象。  
&emsp; 源码的阅读比较枯燥，接下来用一个流程图简单的阐述一下消息追加的关键要素，重点关注一下各个 Future。  

### 1.3.2. Kafka 消息追加流程图与总结  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-12.png)  
&emsp; 上面的消息发送，其实用消息追加来表达更加贴切，因为 Kafka 的 send 方法，并不会直接向 broker 发送消息，而是首先先追加到生产者的内存缓存中，其内存存储结构如下：ConcurrentMap< TopicPartition, Deque< ProducerBatch>> batches，Kafka 的生产者为会每一个 topic 的每一个 分区单独维护一个队列，即 ArrayDeque，内部存放的元素为 ProducerBatch，即代表一个批次，即 Kafka 消息发送是按批发送的。其缓存结果图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-13.png)  
&emsp; KafkaProducer 的 send 方法最终返回的 FutureRecordMetadata ，是 Future 的子类，即 Future 模式。那 kafka 的消息发送怎么实现异步发送、同步发送的呢？  
&emsp; 其实答案也就蕴含在 send 方法的返回值，如果项目方需要使用同步发送的方式，只需要拿到 send 方法的返回结果后，调用其 get() 方法，此时如果消息还未发送到 Broker 上，该方法会被阻塞，等到 broker 返回消息发送结果后该方法会被唤醒并得到消息发送结果。如果需要异步发送，则建议使用 send(ProducerRecord< K, V > record, Callback callback)，但不能调用 get 方法即可。Callback 会在收到 broker 的响应结果后被调用，并且支持拦截器。  
&emsp; 消息追加流程就介绍到这里了，消息被追加到缓存区后，什么是会被发送到 broker 端呢？将在下一节中详细介绍。  

## 1.4. Sender 线程详解  
&emsp; KafkaProducer send 方法的流程，该方法只是将消息追加到 KafKaProducer 的缓存中，并未真正的向 broker 发送消息，本节将探讨 Kafka 的 Sender 线程。  
&emsp; Sender 线程：在 KafkaProducer 中会启动一个单独的线程，其名称为 “kafka-producer-network-thread | clientID”，其中 clientID 为生产者的 id 。   

<!-- 
 属性含义  
KafkaClient client
kafka 网络通信客户端，主要封装与 broker 的网络通信。
RecordAccumulator accumulator
消息记录累积器，消息追加的入口(RecordAccumulator 的 append 方法)。
Metadata metadata
元数据管理器，即 topic 的路由分区信息。
boolean guaranteeMessageOrder
是否需要保证消息的顺序性。
int maxRequestSize
调用 send 方法发送的最大请求大小，包括 key、消息体序列化后的消息总大小不能超过该值。通过参数 max.request.size 来设置。
short acks
用来定义消息“已提交”的条件(标准)，就是 Broker 端向客户端承偌已提交的条件，可选值如下0、-1、1.
int retries
重试次数。
Time time
时间工具类。
boolean running
该线程状态，为 true 表示运行中。
boolean forceClose
是否强制关闭，此时会忽略正在发送中的消息。
SenderMetrics sensors
消息发送相关的统计指标收集器。
int requestTimeoutMs
请求的超时时间。
long retryBackoffMs
请求失败之在重试之前等待的时间。
ApiVersions apiVersions
API版本信息。
TransactionManager transactionManager
事务处理器。
Map< TopicPartition, List< ProducerBatch>> inFlightBatches
正在执行发送相关的消息批次。
-->

### 1.4.1. Sender#run方法详解
&emsp; Sender#run  

```java
public void run() {
    log.debug("Starting Kafka producer I/O thread.");
    while (running) {
        try {
            // @1 Sender 线程在运行状态下主要的业务处理方法，将消息缓存区中的消息向 broker 发送。
            runOnce();    
        } catch (Exception e) {
            log.error("Uncaught error in kafka producer I/O thread: ", e);
        }
    }
    log.debug("Beginning shutdown of Kafka producer I/O thread, sending remaining records.");
    // @2 如果主动关闭 Sender 线程，如果不是强制关闭，则如果缓存区还有消息待发送，再次调用 runOnce 方法将剩余的消息发送完毕后再退出。
    while (!forceClose && (this.accumulator.hasUndrained() || this.client.inFlightRequestCount() > 0)) {    
        try {
            runOnce();
        } catch (Exception e) {
            log.error("Uncaught error in kafka producer I/O thread: ", e);
        }
    }
    // @3 如果强制关闭 Sender 线程，则拒绝未完成提交的消息。
    if (forceClose) {  
        log.debug("Aborting incomplete batches due to forced shutdown");
        this.accumulator.abortIncompleteBatches();
    }
    try {
        // @4 关闭 Kafka Client 即网络通信对象。
        this.client.close();  
    } catch (Exception e) {
        log.error("Failed to close network client", e);
    }
    log.debug("Shutdown of Kafka producer I/O thread has completed.");
}
```

&emsp; 接下来将分别探讨其上述方法的实现细节。  

#### 1.4.1.1. runOnce 详解  
&emsp; Sender#runOnce(本文不关注事务消息的实现原理，故省略了该部分的代码。)  

```java
void runOnce() {
	// 此处省略与事务消息相关的逻辑
    long currentTimeMs = time.milliseconds();
    // @1 调用 sendProducerData 方法发送消息。
    long pollTimeout = sendProducerData(currentTimeMs);   
    // @2  
    client.poll(pollTimeout, currentTimeMs);   
}
```

&emsp; 接下来分别对上述两个方法进行深入探究。  

##### 1.4.1.1.1. sendProducerData
&emsp; sendProducerData把实际要发的消息封装好，放入KakfaNetworkClient中。  

```java
 private long sendProducerData(long now) {
    // 1. 计算需要以及可以向哪些节点发送请求
    Cluster cluster = metadata.fetch();
    // get the list of partitions with data ready to send
    // 计算需要向哪些节点发送请求
    RecordAccumulator.ReadyCheckResult result = this.accumulator.ready(cluster, now);
 
    // if there are any partitions whose leaders are not known yet, force metadata update
    // 2. 如果存在未知的 leader 副本对应的节点（对应的 topic 分区正在执行 leader 选举，或者对应的 topic 已经失效），
    // 标记需要更新缓存的集群元数据信息
    if (!result.unknownLeaderTopics.isEmpty()) {
        // The set of topics with unknown leader contains topics with leader election pending as well as
        // topics which may have expired. Add the topic again to metadata to ensure it is included
        // and request metadata update, since there are messages to send to the topic.
        for (String topic : result.unknownLeaderTopics)
            this.metadata.add(topic);
 
        log.debug("Requesting metadata update due to unknown leader topics from the batched records: {}",
            result.unknownLeaderTopics);
        this.metadata.requestUpdate();
    }
 
    // remove any nodes we aren't ready to send to
    // 3. 遍历处理待发送请求的目标节点，基于网络 IO 检查对应节点是否可用，对于不可用的节点则剔除
    Iterator<Node> iter = result.readyNodes.iterator();
    long notReadyTimeout = Long.MAX_VALUE;
    while (iter.hasNext()) {
        Node node = iter.next();
        // 检查目标节点是否准备好接收请求，如果未准备好但目标节点允许创建连接，则创建到目标节点的连接
        if (!this.client.ready(node, now)) {
            // 对于未准备好的节点，则从 ready 集合中删除
            iter.remove();
            notReadyTimeout = Math.min(notReadyTimeout, this.client.pollDelayMs(node, now));
        }
    }
 
    // create produce requests
    // 4. 获取每个节点待发送消息集合，其中 key 是目标 leader 副本所在节点 ID
    Map<Integer, List<ProducerBatch>> batches = this.accumulator.drain(cluster, result.readyNodes, this.maxRequestSize, now);
    addToInflightBatches(batches);
    if (guaranteeMessageOrder) {
        // 5. 如果需要保证消息的强顺序性，则缓存对应 topic 分区对象，防止同一时间往同一个 topic 分区发送多条处于未完成状态的消息
        // Mute all the partitions drained
        // 将所有 RecordBatch 的 topic 分区对象加入到 muted 集合中
        // 防止同一时间往同一个 topic 分区发送多条处于未完成状态的消息
        for (List<ProducerBatch> batchList : batches.values()) {
            for (ProducerBatch batch : batchList)
                this.accumulator.mutePartition(batch.topicPartition);
        }
    }
    // 6. 处理本地过期的消息，返回 TimeoutException，并释放空间
    accumulator.resetNextBatchExpiryTime();
    List<ProducerBatch> expiredInflightBatches = getExpiredInflightBatches(now);
    List<ProducerBatch> expiredBatches = this.accumulator.expiredBatches(now);
    expiredBatches.addAll(expiredInflightBatches);
 
    // Reset the producer id if an expired batch has previously been sent to the broker. Also update the metrics
    // for expired batches. see the documentation of @TransactionState.resetProducerId to understand why
    // we need to reset the producer id here.
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
    sensors.updateProduceRequestMetrics(batches);
 
    // If we have any nodes that are ready to send + have sendable data, poll with 0 timeout so this can immediately
    // loop and try sending more data. Otherwise, the timeout will be the smaller value between next batch expiry
    // time, and the delay time for checking data availability. Note that the nodes may have data that isn't yet
    // sendable due to lingering, backing off, etc. This specifically does not include nodes with sendable data
    // that aren't ready to send since they would cause busy looping.
    // 如果存在待发送的消息，则设置 pollTimeout 等于 0，这样可以立即发送请求，从而能够缩短剩余消息的缓存时间，避免堆积
    long pollTimeout = Math.min(result.nextReadyCheckDelayMs, notReadyTimeout);
    pollTimeout = Math.min(pollTimeout, this.accumulator.nextExpiryTimeMs() - now);
    pollTimeout = Math.max(pollTimeout, 0);
    if (!result.readyNodes.isEmpty()) {
        log.trace("Nodes with data ready to send: {}", result.readyNodes);
        // if some partitions are already ready to be sent, the select time would be 0;
        // otherwise if some partition already has some data accumulated but not ready yet,
        // the select time will be the time difference between now and its linger expiry time;
        // otherwise the select time will be the time difference between now and the metadata expiry time;
        pollTimeout = 0;
    }
    // 7. 发送请求到服务端，并处理服务端响应
    sendProduceRequests(batches, now);
    return pollTimeout;
}
```

###### 1.4.1.1.1.1. RecordAccumulator#ready
&emsp; 消息发送的过程（ 步骤 7 ），位于 Sender#sendProduceRequests 方法中：  
&emsp; 这一步主要逻辑就是创建客户端请求 ClientRequest 对象，并通过 NetworkClient#send 方法将请求加入到网络 I/O 通道（KafkaChannel）中。同时将该对象缓存到 InFlightRequests 中，等接收到服务端响应时会通过缓存的 ClientRequest 对象调用对应的 callback 方法。最后调用 NetworkClient#poll 方法执行具体的网络请求和响应。  

```java
private void sendProduceRequests(Map<Integer, List<ProducerBatch>> collated, long now) {
    // 遍历处理待发送消息集合，key 是目标节点 ID
    for (Map.Entry<Integer, List<ProducerBatch>> entry : collated.entrySet())
        sendProduceRequest(now, entry.getKey(), acks, requestTimeoutMs, entry.getValue());
}
```

```java
private void sendProduceRequest(long now, int destination, short acks, int timeout, List<ProducerBatch> batches) {
    if (batches.isEmpty())
        return;
 
    Map<TopicPartition, MemoryRecords> produceRecordsByPartition = new HashMap<>(batches.size());
    final Map<TopicPartition, ProducerBatch> recordsByPartition = new HashMap<>(batches.size());
 
    // find the minimum magic version used when creating the record sets
    byte minUsedMagic = apiVersions.maxUsableProduceMagic();
    for (ProducerBatch batch : batches) {
        if (batch.magic() < minUsedMagic)
            minUsedMagic = batch.magic();
    }
 
    // 遍历 RecordBatch 集合，整理成 produceRecordsByPartition 和 recordsByPartition
    for (ProducerBatch batch : batches) {
        TopicPartition tp = batch.topicPartition;
        MemoryRecords records = batch.records();
 
        // down convert if necessary to the minimum magic used. In general, there can be a delay between the time
        // that the producer starts building the batch and the time that we send the request, and we may have
        // chosen the message format based on out-dated metadata. In the worst case, we optimistically chose to use
        // the new message format, but found that the broker didn't support it, so we need to down-convert on the
        // client before sending. This is intended to handle edge cases around cluster upgrades where brokers may
        // not all support the same message format version. For example, if a partition migrates from a broker
        // which is supporting the new magic version to one which doesn't, then we will need to convert.
        if (!records.hasMatchingMagic(minUsedMagic))
            records = batch.records().downConvert(minUsedMagic, 0, time).records();
        produceRecordsByPartition.put(tp, records);
        recordsByPartition.put(tp, batch);
    }
 
    String transactionalId = null;
    if (transactionManager != null && transactionManager.isTransactional()) {
        transactionalId = transactionManager.transactionalId();
    }
    // 创建 ProduceRequest 请求构造器
    ProduceRequest.Builder requestBuilder = ProduceRequest.Builder.forMagic(minUsedMagic, acks, timeout,
            produceRecordsByPartition, transactionalId);
    // 创建回调对象，用于处理响应
    RequestCompletionHandler callback = new RequestCompletionHandler() {
        public void onComplete(ClientResponse response) {
            handleProduceResponse(response, recordsByPartition, time.milliseconds());
        }
    };
 
    String nodeId = Integer.toString(destination);
    // 创建 ClientRequest 请求对象，如果 acks 不等于 0 则表示期望获取服务端响应
    ClientRequest clientRequest = client.newClientRequest(nodeId, requestBuilder, now, acks != 0,
            requestTimeoutMs, callback);
    // 将请求加入到网络 I/O 通道（KafkaChannel）中。同时将该对象缓存到 InFlightRequests 中
    client.send(clientRequest, now);
    log.trace("Sent produce request to {}: {}", nodeId, requestBuilder);
}
```

###### 1.4.1.1.1.2. RecordAccumulator#drain       
&emsp; 知道了需要向哪些节点投递消息，接下来自然而然就需要获取发往每个节点的数据， 步骤 4 的实现位于 RecordAccumulator#drain 方法中：  

```java
public Map<Integer, List<ProducerBatch>> drain(Cluster cluster, Set<Node> nodes, int maxSize, long now) {
    if (nodes.isEmpty())
        return Collections.emptyMap();
    // 记录转换后的结果，key 是目标节点 ID
    Map<Integer, List<ProducerBatch>> batches = new HashMap<>();
    for (Node node : nodes) {
        List<ProducerBatch> ready = drainBatchesForOneNode(cluster, node, maxSize, now);
        batches.put(node.id(), ready);
    }
    return batches;
}
```

```java
private List<ProducerBatch> drainBatchesForOneNode(Cluster cluster, Node node, int maxSize, long now) {
    int size = 0;
    // 获取当前节点上的分区信息
    List<PartitionInfo> parts = cluster.partitionsForNode(node.id());
    // 记录待发往当前节点的 RecordBatch 集合
    List<ProducerBatch> ready = new ArrayList<>();
    /* to make starvation less likely this loop doesn't start at 0 */
    /*
     * drainIndex 用于记录上次发送停止的位置，本次继续从当前位置开始发送，
     * 如果每次都是从 0 位置开始，可能会导致排在后面的分区饿死，可以看做是一个简单的负载均衡策略
     */
    int start = drainIndex = drainIndex % parts.size();
    do {
        PartitionInfo part = parts.get(drainIndex);
        TopicPartition tp = new TopicPartition(part.topic(), part.partition());
 
        this.drainIndex = (this.drainIndex + 1) % parts.size();
 
        // Only proceed if the partition has no in-flight batches.
        // 如果需要保证消息强顺序性，则不应该同时存在多个发往目标分区的消息
        if (isMuted(tp, now))
            continue;
 
        Deque<ProducerBatch> deque = getDeque(tp);
        if (deque == null)
            continue;
 
        synchronized (deque) {
            // invariant: !isMuted(tp,now) && deque != null
            // 获取当前分区对应的 RecordBatch 集合
            ProducerBatch first = deque.peekFirst();
            if (first == null)
                continue;
 
            // first != null
            // 重试 && 重试时间间隔未达到阈值时间
            boolean backoff = first.attempts() > 0 && first.waitedTimeMs(now) < retryBackoffMs;
            // Only drain the batch if it is not during backoff period.
            if (backoff)
                continue;
 
            // 仅发送第一次发送，或重试等待时间较长的消息
            if (size + first.estimatedSizeInBytes() > maxSize && !ready.isEmpty()) {
                // there is a rare case that a single batch size is larger than the request size due to
                // compression; in this case we will still eventually send this batch in a single request
                // 单次消息数据量已达到上限，结束循环，一般对应一个请求的大小，防止请求消息过大
                break;
            } else {
                if (shouldStopDrainBatchesForPartition(first, tp))
                    break;
 
                boolean isTransactional = transactionManager != null && transactionManager.isTransactional();
                ProducerIdAndEpoch producerIdAndEpoch =
                    transactionManager != null ? transactionManager.producerIdAndEpoch() : null;
                // 每次仅获取第一个 RecordBatch，并放入 read 列表中，这样给每个分区一个机会，保证公平，防止饥饿
                ProducerBatch batch = deque.pollFirst();
                if (producerIdAndEpoch != null && !batch.hasSequence()) {
                    // If the batch already has an assigned sequence, then we should not change the producer id and
                    // sequence number, since this may introduce duplicates. In particular, the previous attempt
                    // may actually have been accepted, and if we change the producer id and sequence here, this
                    // attempt will also be accepted, causing a duplicate.
                    //
                    // Additionally, we update the next sequence number bound for the partition, and also have
                    // the transaction manager track the batch so as to ensure that sequence ordering is maintained
                    // even if we receive out of order responses.
                    batch.setProducerState(producerIdAndEpoch, transactionManager.sequenceNumber(batch.topicPartition), isTransactional);
                    transactionManager.incrementSequenceNumber(batch.topicPartition, batch.recordCount);
                    log.debug("Assigned producerId {} and producerEpoch {} to batch with base sequence " +
                            "{} being sent to partition {}", producerIdAndEpoch.producerId,
                        producerIdAndEpoch.epoch, batch.baseSequence(), tp);
 
                    transactionManager.addInFlightBatch(batch);
                }
                // 将当前 RecordBatch 设置为只读
                batch.close();
                size += batch.records().sizeInBytes();
                ready.add(batch);
                // 更新 drainedMs
                batch.drained(now);
            }
        }
    } while (start != drainIndex);
    return ready;
}
```

###### 1.4.1.1.1.3. Sender#sendProduceRequests
&emsp; 消息发送的过程（ 步骤 7 ），位于 Sender#sendProduceRequests 方法中：  
&emsp; 这一步主要逻辑就是创建客户端请求 ClientRequest 对象，并通过 NetworkClient#send 方法将请求加入到网络 I/O 通道（KafkaChannel）中。同时将该对象缓存到 InFlightRequests 中，等接收到服务端响应时会通过缓存的 ClientRequest 对象调用对应的 callback 方法。最后调用 NetworkClient#poll 方法执行具体的网络请求和响应。  

```java
private void sendProduceRequests(Map<Integer, List<ProducerBatch>> collated, long now) {
    // 遍历处理待发送消息集合，key 是目标节点 ID
    for (Map.Entry<Integer, List<ProducerBatch>> entry : collated.entrySet())
        sendProduceRequest(now, entry.getKey(), acks, requestTimeoutMs, entry.getValue());
}
```

```java
private void sendProduceRequest(long now, int destination, short acks, int timeout, List<ProducerBatch> batches) {
    if (batches.isEmpty())
        return;
 
    Map<TopicPartition, MemoryRecords> produceRecordsByPartition = new HashMap<>(batches.size());
    final Map<TopicPartition, ProducerBatch> recordsByPartition = new HashMap<>(batches.size());
 
    // find the minimum magic version used when creating the record sets
    byte minUsedMagic = apiVersions.maxUsableProduceMagic();
    for (ProducerBatch batch : batches) {
        if (batch.magic() < minUsedMagic)
            minUsedMagic = batch.magic();
    }
 
    // 遍历 RecordBatch 集合，整理成 produceRecordsByPartition 和 recordsByPartition
    for (ProducerBatch batch : batches) {
        TopicPartition tp = batch.topicPartition;
        MemoryRecords records = batch.records();
 
        // down convert if necessary to the minimum magic used. In general, there can be a delay between the time
        // that the producer starts building the batch and the time that we send the request, and we may have
        // chosen the message format based on out-dated metadata. In the worst case, we optimistically chose to use
        // the new message format, but found that the broker didn't support it, so we need to down-convert on the
        // client before sending. This is intended to handle edge cases around cluster upgrades where brokers may
        // not all support the same message format version. For example, if a partition migrates from a broker
        // which is supporting the new magic version to one which doesn't, then we will need to convert.
        if (!records.hasMatchingMagic(minUsedMagic))
            records = batch.records().downConvert(minUsedMagic, 0, time).records();
        produceRecordsByPartition.put(tp, records);
        recordsByPartition.put(tp, batch);
    }
 
    String transactionalId = null;
    if (transactionManager != null && transactionManager.isTransactional()) {
        transactionalId = transactionManager.transactionalId();
    }
    // 创建 ProduceRequest 请求构造器
    ProduceRequest.Builder requestBuilder = ProduceRequest.Builder.forMagic(minUsedMagic, acks, timeout,
            produceRecordsByPartition, transactionalId);
    // 创建回调对象，用于处理响应
    RequestCompletionHandler callback = new RequestCompletionHandler() {
        public void onComplete(ClientResponse response) {
            handleProduceResponse(response, recordsByPartition, time.milliseconds());
        }
    };
 
    String nodeId = Integer.toString(destination);
    // 创建 ClientRequest 请求对象，如果 acks 不等于 0 则表示期望获取服务端响应
    ClientRequest clientRequest = client.newClientRequest(nodeId, requestBuilder, now, acks != 0,
            requestTimeoutMs, callback);
    // 将请求加入到网络 I/O 通道（KafkaChannel）中。同时将该对象缓存到 InFlightRequests 中
    client.send(clientRequest, now);
    log.trace("Sent produce request to {}: {}", nodeId, requestBuilder);
}
```

##### 1.4.1.1.2. NetworkClient 的 poll 方法  

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
&emsp; 本文并不会详细深入探讨其网络实现部分，在这里先点出其关键点。  
&emsp; 代码@1：尝试更新云数据。  
&emsp; 代码@2：触发真正的网络通讯，该方法中会通过收到调用 NIO 中的 Selector#select() 方法，对通道的读写就绪事件进行处理，当写事件就绪后，就会将通道中的消息发送到远端的 broker。  
&emsp; 代码@3：然后会消息发送，消息接收、断开连接、API版本，超时等结果进行收集。  
&emsp; 代码@4：并依次对结果进行唤醒，此时会将响应结果设置到 KafkaProducer#send 方法返回的凭证中，从而唤醒发送客户端，完成一次完整的消息发送流程。  

&emsp; Sender 发送线程的流程就介绍到这里了，接下来首先给出一张流程图，然后对上述流程中一些关键的方法再补充深入探讨一下。  

### 1.4.2. run方法流程图  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-14.png)  
&emsp; 根据上面的源码分析得出上述流程图，图中对重点步骤也详细标注了其关键点。  

-------

<!-- 
 RecordAccumulator 核心方法详解  
 RecordAccumulator 的 ready 方法详解  
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


1.5.2. RecordAccumulator 的 drain方法详解  
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
-->
