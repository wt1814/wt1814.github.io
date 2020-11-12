
<!-- TOC -->

- [1. kafka生产者](#1-kafka生产者)
    - [1.1. kafka生产者概览](#11-kafka生产者概览)
    - [1.2. 构造Producer](#12-构造producer)
        - [1.2.1. Producer程序实例](#121-producer程序实例)
        - [1.2.2. Producer主要参数](#122-producer主要参数)
    - [1.3. producer拦截器](#13-producer拦截器)
    - [1.4. 消息序列化](#14-消息序列化)
    - [1.5. 消息分区机制](#15-消息分区机制)
    - [1.6. 无消息丢失配置](#16-无消息丢失配置)
    - [1.7. 消息压缩](#17-消息压缩)
    - [1.8. 多线程处理](#18-多线程处理)

<!-- /TOC -->


# 1. kafka生产者
&emsp; **<font color = "lime">参考《kafka实战》</font>**  

## 1.1. kafka生产者概览  
&emsp; Producer发送消息的过程如下图所示，需要经过拦截器，序列化器和分区器，最终由累加器批量发送至 Broker。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-7.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-8.png)  


## 1.2. 构造Producer

### 1.2.1. Producer程序实例
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

&emsp; 构造一个producer实例大致需要以下5个步骤。  
1. 构造一个 java.utiLProperties 对象，然后至少指定 bootstrap.serverskey.serializer 和 value.serializer这3个属性，它们没有默认值。  
    * bootstrap.servers，指定broker的地址清单
    * key.serializer（key 序列化器），必须是一个实现org.apache.kafka.common.serialization.Serializer接口的类，将key序列化成字节数组。注意：key.serializer必须被设置，即使消息中没有指定key。  
    * value.serializer（value 序列化器），将value序列化成字节数组
2. 使用上一步中创建的Properties实例构造KafkaProducer对象。
3. 构造待发送的消息对象ProducerRecord,指定消息要被发送到的topic、分区以及对 应的key和valueO注意，分区和key信息可以不用指定，由Kafka自行确定目标分区。
4. 调用KafkaProducer的send方法发送消息。
5. 关闭 KafkaProducero。

### 1.2.2. Producer主要参数  
&emsp; 除了前面的3个参数：bootstrap.servers、key.serializer 和 value.serializer 之外，Java 版本 producer还提供了很多其他很重要的参数。常见参数：  

* batch.num.messages

        默认值：200，每次批量消息的数量，只对 asyc 起作用。

* request.required.acks

        默认值：0，0 表示 producer 毋须等待 leader 的确认，1 代表需要 leader 确认写入它的本地 log 并立即确认，-1 代表所有的备份都完成后确认。只对 async 模式起作用，这个参数的调整是数据不丢失和发送效率的 tradeoff，如果对数据丢失不敏感而在乎效率的场景可以考虑设置为 0，这样可以大大提高 producer 发送数据的效率。

* request.timeout.ms

        默认值：10000，确认超时时间。

* partitioner.class

        默认值：kafka.producer.DefaultPartitioner，必须实现 kafka.producer.Partitioner，根据 Key 提供一个分区策略。有时候我们需要相同类型的消息必须顺序处理，这样我们就必须自定义分配策略，从而将相同类型的数据分配到同一个分区中。

* producer.type

        默认值：sync，指定消息发送是同步还是异步。异步 asyc 成批发送用 kafka.producer.AyncProducer， 同步 sync 用 kafka.producer.SyncProducer。同步和异步发送也会影响消息生产的效率。

* compression.topic

        默认值：none，消息压缩，默认不压缩。其余压缩方式还有，"gzip"、"snappy"和"lz4"。对消息的压缩可以极大地减少网络传输量、降低网络 IO，从而提高整体性能。

* compressed.topics

        默认值：null，在设置了压缩的情况下，可以指定特定的 topic 压缩，未指定则全部压缩。

* message.send.max.retries

        默认值：3，消息发送最大尝试次数。

* retry.backoff.ms

        默认值：300，每次尝试增加的额外的间隔时间。

* topic.metadata.refresh.interval.ms

        默认值：600000，定期的获取元数据的时间。当分区丢失，leader 不可用时 producer 也会主动获取元数据，如果为 0，则每次发送完消息就获取元数据，不推荐。如果为负值，则只有在失败的情况下获取元数据。

* queue.buffering.max.ms

        默认值：5000，在 producer queue 的缓存的数据最大时间，仅仅 for asyc。

* queue.buffering.max.message

        默认值：10000，producer 缓存的消息的最大数量，仅仅 for asyc。

* queue.enqueue.timeout.ms

        默认值：-1，0 当 queue 满时丢掉，负值是 queue 满时 block, 正值是 queue 满时 block 相应的时间，仅仅 for asyc。

## 1.3. producer拦截器  
&emsp; 主要用于实现clients端的定制化控制逻辑。interceptor使得用户在消息发送前以及producer回调逻辑前有机会对消息做一些定制化需求，比如修改消息等。同时，producer允许用户指定多个interceptor按序作用于同一条消息从而形成一个拦截链interceptor chain。interceptor的实现接口是org.apahce.kafka.clients.producer.ProducerInterceptor，方法是onSend(ProducerRecord),onAcknowledgement(RecordMetadata,Exception),close。  
&emsp; 若指定多个interceptor，则producer将按照指定顺序调用它们，同时把每个interceptor中捕获的异常记录到错误日志中而不是向上传递。  

## 1.4. 消息序列化  
&emsp; 序列化器serializer：将消息转换成字节数组ByteArray。  
&emsp; 可自定义序列化：1.定义数据对象格式；2.创建自定义序列化类，实现org.apache.kafka.common.serialization.Serializer接口，在serializer方法中实现序列化逻辑；3.在用于构建KafkaProducer的Properties对象中设置key.serializer或value.serializer，取决于是为消息key还是value做自定义序列化。  

## 1.5. 消息分区机制  
......

## 1.6. 无消息丢失配置  
1. 采用同步发送，但是性能会很差，并不推荐在实际场景中使用。因此最好能有一份配置，既使用异步方式还能有效地避免数据丢失, 即使出现producer崩溃的情况也不会有问题。 
2. 做producer端的无消息丢失配置  
    * producer端配置

            max.block.ms=3000     控制block的时长,当buffer空间不够或者metadata丢失时产生block
            acks=all or -1   所有follower都响应了发送消息才能认为提交成功
            retries=Integer.MAX_VALUE  producer开启无限重试，只会重试那些可恢复的异常情况
            max.in.flight.requests.per.connection=1  限制了producer在单个broker连接上能够发送的未响应请求的数量，为了防止topic同分区下的消息乱序问题
            使用带回调机制的send发送消息，即KafkaProducer.send(record,callback)   
            会返回消息发送的结果信息Callback的失败处理逻辑中显式地立即关闭producer，使用close(0)。目的是为了处理消息的乱序问题，将不允许将未完成的消息发送出去

    * broker端配置

            unclean.leader.election.enable=false    不允许非ISR中的副本被选举为leader
            replication.factor=3       强调一定要使用多个副本来保存分区的消息
            min.insync.replicas=2   控制某条消息至少被写入到ISR中的多少个副本才算成功，只有在producer端的acks设置成all或-1时，这个参数才有意义
            确保replication.factor>min.insync.replicas   

    * consumer端配置

            enable.auto.commit=false   设置不能自动提交位移，需要用户手动提交位移

## 1.7. 消息压缩  
&emsp; 数据压缩显著地降低了磁盘占用或带宽占用，从而有效地提升了I/O密集型应用的性能。不过引用压缩同时会消耗额外的CPU时钟周期，因此压缩是I/O性能和CPU资源的平衡。  
&emsp; kafka自0.7.x版本便开始支持压缩特性-producer端能够将一批消息压缩成一条消息发送，而broker端将这条压缩消息写入本地日志文件，consumer端获取到这条压缩消息时会自动对消息进行解压缩。即producer端压缩，broker保持，consumer解压缩。  
&emsp; 如果有些前置条件不满足，比如需要进行消息格式的转换等，那么broker端就需要对消息进行解压缩然后再重新压缩。  
&emsp; kafka支持三种压缩算法：GZIP、Snappy和LZ4，性能LZ4>> Snappy>>GZIP，batch越大，压缩时间越长。
假定要设置使用Snappy压缩算法，则设置方法如下：  

```java
props .put (ncompressiont. typ,"snappy");
//或者
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");  
```
&emsp; 如果发现压缩很慢，说明系统的瓶颈在用户主线程而不是I/O发送线程，因此可以考虑增加多个用户线程同时发送消息，这样通常能显著地提升producer吞吐量。

## 1.8. 多线程处理  
&emsp; 实际环境中只使用一个用户主线程通常无法满足所需的吞吐量目标，因此需要构造多个线程或多个进程来同时给Kafka集群发送消息。这样在使用过程中就存在着两种基本的使用方法。  

* 多线程单KafkaProducer实例  
&emsp; 顾名思义，这种方法就是在全局构造一个KafkaProducer实例，然后在多个线程中共享使 用。由于KafkaProducer是线程安全的，所以这种使用方式也是线程安全的。  
* 多线程多KafkaProducer实例  
&emsp; 除了上面的用法，还可以在每个producer主线程中都构造一个KafkaProducer实例，并且 保证此实例在该线程中封闭（thread confinement,线程封闭是实现线程安全的重要手段之一）。   
    
&emsp; 显然，这两种方式各有优劣，如下表所示。   

||说 明	|优 势	|劣 势|
|---|---|---|---|
|单 KafkaProducer 实例|	所有线程共享一个KafkaProducer 实例|实现简单，性能好|①所有线程共享一个内存缓冲区，可 能需要较多内存；</br>②一旦producer某个线程崩溃导致 KafkaProducer实例被“破坏”,则所 有用户线程都无法工作|
|多 KafkaProducer 实例|	每个线程维护自己专属的 KafkaProducer 实例|①每个用户线程拥有专属的 KafkaProducer 实例、缓冲 区空间及一组对应的配置参 数，可以进行细粒度的调 优；</br>②单个 KafkaProducer崩溃不会影响其他producer线程工作	|需要较大的内存分配开销|

&emsp; 如果是对分区数不多的Kafka集群而言，比较推荐使用第一种方法，即在多个 producer用户线程中共享一个KafkaProducer实例。若是对那些拥有超多分区的集群而言，釆 用第二种方法具有较高的可控性，方便producer的后续管理。  

