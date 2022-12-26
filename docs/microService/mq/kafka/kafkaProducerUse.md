
<!-- TOC -->

- [1. kafka生产者](#1-kafka生产者)
    - [1.1. 构造Producer](#11-构造producer)
        - [1.1.1. Producer程序实例](#111-producer程序实例)
        - [1.1.2. Producer配置](#112-producer配置)
    - [1.2. kafka生产者发送消息过程](#12-kafka生产者发送消息过程)
        - [1.2.1. producer拦截器](#121-producer拦截器)
        - [1.2.2. 消息序列化](#122-消息序列化)
        - [1.2.3. 消息分区机制](#123-消息分区机制)
    - [1.3. 可靠性，Acks & Retries](#13-可靠性acks--retries)
    - [1.4. 提升性能](#14-提升性能)
        - [1.4.1. 同步/异步](#141-同步异步)
        - [1.4.2. ~~批量发送~~](#142-批量发送)
        - [1.4.3. 消息压缩](#143-消息压缩)
    - [1.5. 多线程处理](#15-多线程处理)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. Producer发送消息的过程：需要经过拦截器，序列化器和分区器，最终由累加器批量发送至Broker。  
&emsp; Kafka分区策略查看[消息分区](/docs/microService/mq/kafka/topic.md)。  
2. **<font color = "clime">如何提升Producer的性能？异步，批量，压缩。</font>**  
3. 多线程处理：   
&emsp; 多线程单KafkaProducer实例（可以理解为单例模式）、多线程多KafkaProducer实例（可以理解为多例，原型模式）。  
&emsp; **<font color = "clime">如果是对分区数不多的Kafka集群而言，比较推荐使用第一种方法，即在多个producer用户线程中共享一个KafkaProducer实例。若是对那些拥有超多分区的集群而言，釆用第二种方法具有较高的可控性，方便producer的后续管理。</font>**   

# 1. kafka生产者
&emsp; **<font color = "red">参考《kafka实战》</font>**  

## 1.1. 构造Producer
### 1.1.1. Producer程序实例
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

&emsp; 构造一个producer实例大致需要以下5个步骤：   
1. 构造一个java.utiLProperties对象，然后至少指定bootstrap.servers、key.serializer和value.serializer这3个属性，它们没有默认值。  
    * bootstrap.servers，指定broker的地址清单。
    * key.serializer（key序列化器），必须是一个实现org.apache.kafka.common.serialization.Serializer接口的类，将key序列化成字节数组。注意：key.serializer必须被设置，即使消息中没有指定key。  
    * value.serializer（value 序列化器），将value序列化成字节数组。
2. 使用上一步中创建的Properties实例构造KafkaProducer对象。
3. 构造待发送的消息对象ProducerRecord，指定消息要被发送到的topic、分区以及对应的key和value。注意，分区和key信息可以不用指定，由Kafka自行确定目标分区。
4. 调用KafkaProducer的send方法发送消息。
5. 关闭 KafkaProducer。

### 1.1.2. Producer配置   
&emsp; 除了前面的3个参数：bootstrap.servers、key.serializer 和 value.serializer 之外，Kafka还为JAVA生产者提供了[其他配置](https://kafka.apachecn.org/documentation.html#producerconfigs)。如下：  
  
|Name |	Description |	Type |	Default |	Valid Values |	Importance|
|---|---|----|---|---|----|
|bootstrap.servers	|这是一个用于建立初始连接到kafka集群的"主机/端口对"配置列表。不论这个参数配置了哪些服务器来初始化连接，客户端都是会均衡地与集群中的所有服务器建立连接。—配置的服务器清单仅用于初始化连接，以便找到集群中的所有服务器。配置格式： host1:port1,host2:port2,.... 由于这些主机是用于初始化连接，以获得整个集群（集群是会动态变化的），因此这个配置清单不需要包含整个集群的服务器。（当然，为了避免单节点风险，这个清单最好配置多台主机）。	|list	| | |	high|
|key.serializer	|关键字的序列化类，实现以下接口： org.apache.kafka.common.serialization.Serializer 接口。	|class	| | |high|
|value.serializer	|值的序列化类，实现以下接口： org.apache.kafka.common.serialization.Serializer 接口。	|class	| | |high|
|acks	|此配置是 Producer 在确认一个请求发送完成之前需要收到的反馈信息的数量。 这个参数是为了保证发送请求的可靠性。以下配置方式是允许的：</br>acks=0 如果设置为0，则 producer 不会等待服务器的反馈。该消息会被立刻添加到 socket buffer 中并认为已经发送完成。在这种情况下，服务器是否收到请求是没法保证的，并且参数retries也不会生效（因为客户端无法获得失败信息）。每个记录返回的 offset 总是被设置为-1。</br>acks=1 如果设置为1，leader节点会将记录写入本地日志，并且在所有 follower 节点反馈之前就先确认成功。在这种情况下，如果 leader 节点在接收记录之后，并且在 follower 节点复制数据完成之前产生错误，则这条记录会丢失。</br>acks=all 如果设置为all，这就意味着 leader 节点会等待所有同步中的副本确认之后再确认这条记录是否发送完成。只要至少有一个同步副本存在，记录就不会丢失。这种方式是对请求传递的最有效保证。acks=-1与acks=all是等效的。 |string	|1	|[all, -1, 0, 1]|	high|
|buffer.memory|	Producer 用来缓冲等待被发送到服务器的记录的总字节数。如果记录发送的速度比发送到服务器的速度快， Producer 就会阻塞，如果阻塞的时间超过 max.block.ms 配置的时长，则会抛出一个异常。</br>这个配置与 Producer 的可用总内存有一定的对应关系，但并不是完全等价的关系，因为 Producer 的可用内存并不是全部都用来缓存。一些额外的内存可能会用于压缩(如果启用了压缩)，以及维护正在运行的请求。|long	|33554432	|[0,...]|high|

## 1.2. kafka生产者发送消息过程  
&emsp; **<font color = "red">Producer发送消息的过程如下图所示（详情可参考kafka生产者源码部分），需要经过拦截器，序列化器和分区器，最终由累加器批量发送至Broker。</font>**  

![image](http://182.92.69.8:8081/img/microService/mq/kafka/kafka-7.png)  
![image](http://182.92.69.8:8081/img/microService/mq/kafka/kafka-8.png)  

### 1.2.1. producer拦截器  
&emsp; producer拦截器主要用于实现clients端的定制化控制逻辑。interceptor使得用户在消息发送前以及producer回调逻辑前有机会对消息做一些定制化需求，比如修改消息等。  
&emsp; 同时，producer允许用户指定多个interceptor按序作用于同一条消息从而形成一个拦截链interceptor chain。若指定多个interceptor，则producer将按照指定顺序调用它们，同时把每个interceptor中捕获的异常记录到错误日志中而不是向上传递。    
&emsp; interceptor的实现接口是org.apahce.kafka.clients.producer.ProducerInterceptor，方法是onSend(ProducerRecord)、onAcknowledgement(RecordMetadata,Exception)、close。  

### 1.2.2. 消息序列化  
&emsp; 序列化器serializer：将消息转换成字节数组ByteArray。  
&emsp; 可自定义序列化：  
1. 定义数据对象格式； 
2. 创建自定义序列化类，实现org.apache.kafka.common.serialization.Serializer接口，在serializer方法中实现序列化逻辑；  
3. 在用于构建KafkaProducer的Properties对象中设置key.serializer或value.serializer，取决于是为消息key还是value做自定义序列化。  

### 1.2.3. 消息分区机制  
&emsp; **Kafka提供了默认的分区策略（轮询、随机、按key顺序），同时支持自定义分区策略。**分区机制如下：    

```java
public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
    List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
    int numPartitions = partitions.size();
    if (keyBytes == null) {
        int nextValue = nextValue(topic);
        List<PartitionInfo> availablePartitions = cluster.availablePartitionsForTopic(topic);
        if (availablePartitions.size() > 0) {
            //轮询
            int part = Utils.toPositive(nextValue) % availablePartitions.size();
            return availablePartitions.get(part).partition();
        } else {
            // no partitions are available, give a non-available partition
            //随机
            return Utils.toPositive(nextValue) % numPartitions;
        }
    } else {
        // hash the keyBytes to choose a partition
        // 按key顺序
        return Utils.toPositive(Utils.murmur2(keyBytes)) % numPartitions;
    }
}
```
1. 首先kafka会判断有没有key，这里的key就是每条消息定义的消息键，发消息的时候在ProducerRecord(String topic, K key, V value)中指定的key。  
2. 如果没有key，会采用轮询策略，也称 Round-robin 策略，即顺序分配。比如一个主题下有 3 个分区，那么第一条消息被发送到分区 0，第二条被发送到分区 1，第三条被发送到分区 2，以此类推。当生产第 4 条消息时又会重新开始上述轮询。轮询策略有非常优秀的负载均衡表现，它总是能保证消息最大限度地被平均分配到所有分区上，故默认情况下它是最合理的分区策略。  
3. 如果有key，那么就按消息键策略，这样可以保证同一个 Key 的所有消息都进入到相同的分区里面，这样就保证了顺序性。  

## 1.3. 可靠性，Acks & Retries
&emsp; Kafka生产者在发送完一个的消息之后，要求Broker在规定的时间Ack应答，如果没有在规定时间内应答，Kafka生产者会尝试n次重新发送消息。  

    acks=1(默认) - Leader会将Record写到其本地日志中，但会在不等待所有Follower的完全确认的情况下做出响应。在这种情况下，如果Leader在确认记录后立即失败，但在Follower复制记录之前失败，则记录将丢失。
    acks=0 - 生产者根本不会等待服务器的任何确认。该记录将立即添加到套接字缓冲区中并视为已发送。在这种情况下，不能保证服务器已收到记录。
    acks=all - 这意味着Leader将等待全套同步副本确认记录。这保证了只要至少一个同步副本仍处于活动状态，记录就不会丢失。这是最有力的保证。这等效于acks = -1设置。

&emsp; 如果生产者在规定的时间内，并没有得到Kafka的Leader的Ack应答，Kafka可以开启reties机制。  

    request.timeout.ms = 30000  默认  
    retries = 2147483647 默认  

&emsp; Kafka的Retries机制会导致幂等性问题。 

## 1.4. 提升性能  
&emsp; **<font color = "clime">如何提升Producer的性能？异步，批量，压缩。</font>**  

### 1.4.1. 同步/异步  
<!-- 
https://blog.csdn.net/lwglwg32719/article/details/86510029
-->
&emsp; Kafka系统支持两种不同的发送方式----同步模式(sync)和异步模式(async)。  

### 1.4.2. ~~批量发送~~  
<!-- 
https://www.it610.com/article/1281259146699620352.htm
-->

&emsp; Kafka允许进行批量发送消息，producter发送消息的时候，可以将消息缓存在本地，等到了固定条件发送到Kafka 。  

* 等消息条数到固定条数。  
* 一段时间发送一次。  


### 1.4.3. 消息压缩
&emsp; **<font color = "red">数据压缩显著地降低了磁盘占用或带宽占用，从而有效地提升了I/O密集型应用的性能。不过引用压缩同时会消耗额外的CPU时钟周期，因此压缩是I/O性能和CPU资源的平衡。</font>**  
&emsp; kafka自0.7.x版本便开始支持压缩特性。producer端能够将一批消息压缩成一条消息发送，而broker端将这条压缩消息写入本地日志文件，consumer端获取到这条压缩消息时会自动对消息进行解压缩。即producer端压缩，broker保持，consumer解压缩。  
&emsp; 如果有些前置条件不满足，比如需要进行消息格式的转换等，那么broker端就需要对消息进行解压缩然后再重新压缩。  
&emsp; kafka支持三种压缩算法：GZIP、Snappy和LZ4，性能LZ4>> Snappy>>GZIP，batch越大，压缩时间越长。
假定要设置使用Snappy压缩算法，则设置方法如下：  

```java
props .put (ncompressiont. typ,"snappy");
//或者
props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");  
```
&emsp; 如果发现压缩很慢，说明系统的瓶颈在用户主线程而不是I/O发送线程，因此可以考虑增加多个用户线程同时发送消息，这样通常能显著地提升producer吞吐量。


## 1.5. 多线程处理  
&emsp; 实际环境中只使用一个用户主线程通常无法满足所需的吞吐量目标，因此需要构造多个线程或多个进程来同时给Kafka集群发送消息。这样在使用过程中就存在着两种基本的使用方法。  

* 多线程单KafkaProducer实例（可以理解为单例模式）  
&emsp; 这种方法就是在全局构造一个KafkaProducer实例，然后在多个线程中共享使用。由于KafkaProducer是线程安全的，所以这种使用方式也是线程安全的。  
* 多线程多KafkaProducer实例（可以理解为多例，原型模式）    
&emsp; 除了上面的用法，还可以在每个producer主线程中都构造一个KafkaProducer实例，并且保证此实例在该线程中封闭（thread confinement，线程封闭是实现线程安全的重要手段之一）。   


&emsp; 显然，这两种方式各有优劣，如下表所示：   


|说 明|优 势|劣 势|
|---|---|---|
|单 KafkaProducer 实例|所有线程共享一个KafkaProducer 实例|实现简单，性能好|①所有线程共享一个内存缓冲区，可能需要较多内存；</br>②一旦producer某个线程崩溃导致KafkaProducer实例被“破坏”，则所有用户线程都无法工作|
|多 KafkaProducer 实例|	每个线程维护自己专属的 KafkaProducer 实例|①每个用户线程拥有专属的KafkaProducer实例、缓冲 区空间及一组对应的配置参数，可以进行细粒度的调优；</br>②单个KafkaProducer崩溃不会影响其他producer线程工作 |需要较大的内存分配开销|

&emsp; **<font color = "clime">如果是对分区数不多的Kafka集群而言，比较推荐使用第一种方法，即在多个producer用户线程中共享一个KafkaProducer实例。若是对那些拥有超多分区的集群而言，釆用第二种方法具有较高的可控性，方便producer的后续管理。</font>**    
