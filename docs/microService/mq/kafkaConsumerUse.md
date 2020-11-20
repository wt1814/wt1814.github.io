
<!-- TOC -->

- [1. kafka消费者开发](#1-kafka消费者开发)
    - [1.1. consumer概览](#11-consumer概览)
        - [1.1.1. 消费者分组](#111-消费者分组)
        - [1.1.2. 位移(offset)](#112-位移offset)
        - [1.1.3. 消费者组重平衡](#113-消费者组重平衡)
        - [1.1.4. 消费方式](#114-消费方式)
    - [1.2. 构建consumer](#12-构建consumer)
        - [1.2.1. consumer程序实例](#121-consumer程序实例)
        - [1.2.2. consumer参数](#122-consumer参数)
    - [1.3. 订阅topic](#13-订阅topic)
        - [1.3.1. 订阅topic列表](#131-订阅topic列表)
        - [1.3.2. 基于正则表达订阅topic](#132-基于正则表达订阅topic)
    - [1.4. 消息轮询](#14-消息轮询)
    - [1.5. 位移管理](#15-位移管理)
    - [1.6. 消费者组重平衡(rebalance)](#16-消费者组重平衡rebalance)
    - [1.7. 多线程消费实例](#17-多线程消费实例)

<!-- /TOC -->

# 1. kafka消费者开发
&emsp; **<font color = "lime">参考《kafka实战》</font>**  

<!-- 
https://blog.csdn.net/lwglwg32719/article/details/86510029
-->

## 1.1. consumer概览  

### 1.1.1. 消费者分组  
&emsp; kafka消费者分为两类：  

* 消费者组(consumer group)：由多个消费者实例构成一个整体进行消费
* 独立消费者(standalone consumer)：单独执行消费操作

&emsp; **消费者组的特点：**

* 对于同一个group而言，topic的每条消息只能发送到group下一个consumer实例上  
* topic消息可以发送到多个group中  

&emsp; Kafka通过消费者组，可以实现基于队列和基于发布/订阅的两种消息引擎

* 实现基于队列的模型：所有consumer实例都属于相同的group，每条消息只会被一个consumer实例处理。  
* 实现基于发布/订阅模型：consumer实例都属于不同group，这样kafka消息会被广播到所有consumer实例上。  


<!-- 
&emsp; 消费者使用一个消费者组名group.id来标记自己，topic的每条消息都只会被发送到每个订阅它的消费者组的一个消费者实例上。  

&emsp; kafka同时支持基于队列和基于发布/订阅的两种消息引擎模型：

* 实现基于队列的模型：所有consumer实例都属于相同的group，每条消息只会被一个consumer实例处理。  
* 实现基于发布/订阅模型：consumer实例都属于不同group，这样kafka消息会被广播到所有consumer实例上。  

&emsp; 每个消费者只能消费所分配到的分区的消息，每一个分区只能被一个消费组中的一个消费者所消费，所以同一个消费组中消费者的数量如果超过了分区的数量，将会出现有些消费者分配不到消费的分区。消费组与消费者关系如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-32.png)  

&emsp; consumer group是用于高伸缩性、高容错性的consumer机制。组内多个consumer实例可以同时读取Kafka消息，而且一旦有某个consumer挂掉，consumer group会立即将已崩溃consumer负责的分区转交给其他consumer来负责，从而保证不丢数据，这也成为重平衡。  
&emsp; Kafka目前只提供单个分区内的消息顺序，而不会维护全局的消息顺序，因此用户如果要实现topic全局的消息顺序读取，就只能通过让每个consumer group下只包含一个consumer实例的方式来实现。 
-->

### 1.1.2. 位移(offset)  
&emsp; 需要明确指出的是，这里的offset指代的是consumer端的offset，与分区日志中的offset是不同的含义。每个consumer实例都会为它消费的分区维护属于自己的位置信息来记录当前 消费了多少条消息。这在Kafka中有一个特有的术语：位移(offset)。kafka consumer在内部使用一个map来保存其订阅topic所属分区的offset，key是group.id、topic和分区的元组，value就是位移值。  
&emsp; Kafka同时还引入了检查点机制定期对位移进行持久化。  

&emsp; consumer客户端需要定期地向Kafka集群汇报自己消费数据的进度，这一过程被称为位移提交(offset commit)。位移提交这件事情对于consumer而言非常重要，它不仅表征了 consumer端的消费进度，同时也直接决定了consumer端的消费语义保证。    
&emsp; consumer把位移提交到kafka的一个内部topic（__consumer_offsets）上，通常不能直接操作该topic，特别注意不要擅自删除或搬移该topic的日志文件。  

### 1.1.3. 消费者组重平衡   
&emsp; 假设组内某个实例挂掉了，Kafka能够自动检测到，然后把这个Failed实例之前负责的分区转移给其他活着的消费者，这个过程称之为重平衡(Rebalance)。可以保障高可用性。  
&emsp; 除此之外，它协调着消费组中的消费者分配和订阅 topic 分区，比如某个 Group 下有 20 个 Consumer 实例，它订阅了一个具有 100 个分区的 Topic。正常情况下，Kafka 平均会为每个 Consumer 分配 5 个分区。这个分配的过程也叫 Rebalance。再比如此刻新增了消费者，得分一些分区给它吧，这样才可以负载均衡以及利用好资源，那么这个过程也是 Rebalance 来完成的。  

### 1.1.4. 消费方式  
&emsp; consumer 采用 pull（拉）模式从 broker 中读取数据。  
&emsp; push（推）模式很难适应消费速率不同的消费者，因为消息发送速率是由 broker 决定的。 它的目标是尽可能以最快速度传递消息，但是这样很容易造成 consumer 来不及处理消息，典型的表现就是拒绝服务以及网络拥塞。而 pull 模式则可以根据 consumer 的消费能力以适当的速率消费消息。  
&emsp; 对于 Kafka 而言，pull 模式更合适，它可简化 broker 的设计，consumer 可自主控制消费消息的速率，同时consumer可以自己控制消费方式——即可批量消费也可逐条消费，同时还能选择不同的提交方式从而实现不同的传输语义。  
&emsp; pull 模式不足之处是，如果 kafka 没有数据，消费者可能会陷入循环中，一直等待数据到达。为了避免这种情况，在拉取请求中有参数，允许消费者请求在等待数据到达 的“长轮询”中进行阻塞（并且可选地等待到给定的字节数，以确保大的传输大小）。  

## 1.2. 构建consumer
### 1.2.1. consumer程序实例
```java
public class ConsumerDemo {
    public static void main(String[] args) {
        String topicName = "test-topic";
        String groupId = "test-group";
        Properties props = new Properties();
        props.put("bootstrap.servers", "locahost:9092");
        //必须指定
        props.put("group.id", groupId);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        //从最早的消息开始读取
        props.put("auto.offset.reset", "earliest");
        //必须指定
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        //必须指定
        props.put("value.seserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<Object, Object> consumer = new KafkaConsumer<>(props);
        //订阅不是增量式的，多次订阅会覆盖
        consumer.subscribe(Collections.singleton(topicName));
        try {

            while (true) {
                //使用了和selector类似的机制，需要用户轮询
                //1000是超时时间，控制最大阻塞时间
                ConsumerRecords<Object, Object> records = consumer.poll(1000);
                for (ConsumerRecord record : records) {
                    System.out.println(record.key() + ":" + record.value());
                }
            }
        }finally {
            //关闭并最多等待30s
            consumer.close();
        }
    }
}
```
&emsp; 构造consumer需要下面6个步骤：  
1. 构造一个 java.util.Properties 对象，至少指定 bootstrap.serversn、key.deserializer、value.deserializer和group.id的值，即上面代码中显示指明必须指定带注释的4个参数。
2. 使用上一步创建的Properties实例构造KafkaConsumer对象。
3. 调用KafkaConsumer.subscribe方法订阅consumer group感兴趣的topic列表。
4. 循环调用KafkaConsumer.poll方法获取封装在ConsumerRecord的topic消息。
5. 处理获取到的ConsumerRecord对象。
6. 关闭 KafkaConsumer。 

### 1.2.2. consumer参数
&emsp; 关于consumer的常见参数：  

* bootstrap.servers：连接 broker 地址，host：port 格式。
* group.id：消费者隶属的消费组。
* key.deserializer：与生产者的key.serializer对应，key 的反序列化方式。
* value.deserializer：与生产者的value.serializer对应，value 的反序列化方式。
* session.timeout.ms：coordinator 检测失败的时间。默认 10s 该参数是 Consumer Group 主动检测 （组内成员 comsummer) 崩溃的时间间隔，类似于心跳过期时间。
* auto.offset.reset：该属性指定了消费者在读取一个没有偏移量后者偏移量无效（消费者长时间失效当前的偏移量已经过时并且被删除了）的分区的情况下，应该作何处理，默认值是 latest，也就是从最新记录读取数据（消费者启动之后生成的记录），另一个值是 earliest，意思是在偏移量无效的情况下，消费者从起始位置开始读取数据。
* enable.auto.commit：否自动提交位移，如果为false，则需要在程序中手动提交位移。对于精确到一次的语义，最好手动提交位移
* fetch.max.bytes：单次拉取数据的最大字节数量
* max.poll.records：单次 poll 调用返回的最大消息数，如果处理逻辑很轻量，可以适当提高该值。但是max.poll.records条数据需要在在 session.timeout.ms 这个时间内处理完 。默认值为500
* request.timeout.ms：一次请求响应的最长等待时间。如果在超时时间内未得到响应，kafka 要么重发这条消息，要么超过重试次数的情况下直接置为失败。

## 1.3. 订阅topic
&emsp; 两种订阅topic的方式：直接订阅topic列表，基于正则表达订阅topic。  

### 1.3.1. 订阅topic列表
1. 消费者组订阅topic列表

    ```java
    consumer.subscribe(Arrays.asList("topic1","topic2","topic3"))
    ```
2. 单独消费者订阅topic列表

    ```java
    TopicPartition tp1= new TopicPartition("topic1",0)
    TopicPartition tp2= new TopicPartition("topic2",1)
    consumer.assign(Arrays.asList(tp1,tp2))
    ```

    * 如果发生多次assign调用，最后一次assign调用的分配生效，之前的都会被覆盖掉。  
    * assign和subscribe一定不要混用，即不能在一个consumer应用中同时使用consumer group和独立consumer。  
    * 独立consumer使用情况：  
        * 进程自己维护分区的状态，它就可以固定消费某些分区而不用担心消费状态丢失的问题。
        * 进程本身就是高可用且能够自动重启恢复错误（比如使用YARN和Mesos等容器调度框架），就不需要让kafka来帮它完成错误检测和状态恢复。

### 1.3.2. 基于正则表达订阅topic
&emsp; 基于正则表达式订阅topic
* enable.auto.commit=true
    ```java
    consumer.subscribe(Pattern.compile("kafka-.*"),new NoOpConsumerRebalanceListener())
    ```
* enable.auto.commit=false
    ```java
    consumer.subscribe(Pattern.compile("kafka-.*"),new ConsumerRebalanceListener()..)
    //ConsumerRebalanceListener是一个回调接口，用户需要通过实现这个接口来实现consumer分区分配方案发生变更时的逻辑。
    ```

## 1.4. 消息轮询
&emsp; kafka采用类似于Linux I/O模型的poll或select等，使用一个线程来同时管理多个socket连接，即同时与多个broker通信实现消息的并行读取。  
&emsp; 一旦consumer订阅了topic，所有的消费逻辑包括coordinator的协调、消费者组的rebalance以及数据的获取都会在主逻辑poll方法的一次调用中被执行。这样用户可以很容易使用一个线程来管理所有的consumer I/O操作。  
&emsp; 新版本Java consumer是一个双线程的Java进程：创建KafkaConsumer的线程被称为用户主线程，同时consumer在后台会创建一个心跳线程。KafkaConsumer的poll方法在用户主线程中运行。    
&emsp; poll方法根据当前consumer的消费位移返回消息集合。poll方法中有个超时的参数，为了让consumer有机会在等待kafka消息的同时还能够定期执行其他任务。   
&emsp; poll方法返回满足以下任意一个条件即可返回：  
    
* 要么获取了足够多的可用数据  
* 要么等待时间超过了指定的超时设置  

&emsp; poll的使用方法：  

* consumer需要定期执行其他子任务：推荐poll(较小超时时间) + 运行标识布尔变量while(true)的方式  
* consumer不需要定期执行子任务：推荐poll(MAX_VALUE) + 捕获WakeupException的方式  

&emsp; poll(MAX_VALUE)让consumer程序在未获取到足够多数据时无限等待，然后通过捕获WakeupException异常来判断consumer是否结束。  
&emsp; 使用这种方式调用poll，那么需要在另一个线程中调用consumer.wakeup()方法来触发consumer的关闭。  
&emsp; KafkaConsumer不是线程安全的，但是有一个例外：用户可以安全地在另一个线程中调用consumer.wakeup()。注意，只有wakeup方法是特例，其他KafkaConsumer方法都不能同时在多线程中使用。  

## 1.5. 消费者端位移管理
<!-- 
kakfa消费位移
https://blog.csdn.net/haogenmin/article/details/109488571

https://www.kancloud.cn/nicefo71/kafka/1471593
-->

&emsp; consumer会在kafka集群的所有broker中选择一个broker作为consumer group的coordinator（协调者），用于实现组成员管理、消费分配方案制定以及提交位移等。  
&emsp; 当consumer运行了一段时间之后，它必须要提交自己的位移值。consumer提交位移的主要机制是通过向所属的coordinator发送位移提交请求来实现的。每个位移提交请求都会往内部topic（__consumer_offsets）对应分区上追加写入一条消息。  
&emsp; 消息的key是group.id、topic和分区的元组，value就是位移值。  

**位移提交：**  
&emsp; **<font color = "red">位移提交有两种方式：</font><font color = "lime">自动提交、手动提交。</font>**  
* 自动提交  
    &emsp; 使用方法：默认不用配置，或者显示配置enable.auto.commit=true，用auto.commit.interval.ms参数控制自动提交的间隔。  
    &emsp; 使用场景：对消息交付语义无需求，容忍一定的消息丢失。自动提交的问题是用户不能细粒度地处理位移的提交，特别是在有较强的精确一次的处理语义。  
* 手动提交  
    &emsp; 使用方法：设置enable.auto.commmit=false;手动调用KafkaConsumer.commitSync或KafkaConsumer.commitAsync提交位移  
    &emsp; 使用场景：消息处理逻辑重，不允许消息丢失，至少要求“最少一次”处理语义  
    &emsp; commitSync：同步手动提交，用户程序会等待位移提交结束才执行下一条语句命令。  
    &emsp; commitAsync：异步手动提交，这里的异步不是开启一个线程提交，而是指不会阻塞，consumer在后续poll调用时轮询该位移提交的结果。  
    &emsp; commitSync和commitAsync都有带参数的重载方法，目的是实现更加细粒度化的位移提交策略，指定一个Map显示地告诉kafka为哪些分区提交位移，consumer.commitSync(Collections.singletonMap(partition,new OffsetAndMetadata(lastOffset + 1)))。   

## 1.6. 消费者组重平衡(rebalance)
<!-- 
kakfa消费组和重平衡
https://blog.csdn.net/haogenmin/article/details/109489704
消费者组重平衡全流程解析
https://www.kancloud.cn/nicefo71/kafka/1473378
-->

&emsp; rebalance本质上是一组协议，它规定了一个consumer group是如何达成一致来分配订阅topic的所有分区的。coordinator负责对组执行rebalance操作。  
&emsp; 消费者组rebalance触发的条件，满足其一即可：  

1. 组成员发生变更，比如新consumer加入组，或已有consumer主动离开组，再或是已有consumer崩溃时则触rebalance。（consumer崩溃的情况，有可能是consumer进程“挂掉”或consumer进程所在的机器宕机，也有可能是consumer无法在指定的时间内完成消息的处理。）
2. 组订阅topic数发生变更，比如使用基于正则表达式的订阅，当匹配正则表达式的新topic被创建时则会触发rebalance。
3. 组订阅topic的分区数发生变更，比如使用命令行脚本增加了订阅topic的分区数。

&emsp; **如何避免 Rebalances？**  
&emsp; 由于目前一次rebalance操作的开销很大，生产环境中用户一定要结合自身业务特点仔细调优consumer参数：request.timeout.ms、max.poll.records和max.poll.interval.ms，以避免不必要的rebalance出现。  

&emsp; **分配策略**  
&emsp; kafka新版本consumer默认提供了3种分配策略，分别是range策略、round-robin策略和sticky策略。

* range策略：基于范围的思想，将单个topic的所有分区按照顺序排列，然后把这些分区划分成固定大小的分区段并依次分配给每个consumer。
* round-robin策略：把所有topic的所有分区顺序排开，然后轮询式地分配给各个consumer。
* sticky策略：有效地避免了上述两种策略完全无视历史分配方案的缺陷，采用了“有黏性”的策略对所有consumer实例进行分配，可以规避极端情况下的数据倾斜而且在两次rebalance间最大限度地维持了之前的分配方案。

&emsp; 如果group下所有consumer实例的订阅是相同的，那么使用round-robin会带来更公平的分配方案，否则使用range策略的效果更好。  
&emsp; 新版本consumer默认的分配策略是range，用户根据consumer参数partition.assingment.strategy来进行配置。另外kafka支持自定义的分配策略，用户可以创建自己的consumer分配器assignor。  

&emsp; **rebalance generation**  
&emsp; rebalance generation：用于标识某次rebalance,每个consumer group进行rebalance后，generation就会加1，表示group进入一个新的版本，generation从0开始。
&emsp; consumer group可以执行任意次rebalance，generation是为了防止无效offset提交，延迟的offset提交携带的是旧的generation信息，这次提交就会被consumer group拒绝。  

&emsp; **rebalance流程**  
1. 指定协调器：计算groupI的哈希值%分区数量(默认是50)的值，寻找__consumer_offsets分区为该值的leader副本所在的broker，该broker即为这个group的协调器  
2. 成功连接协调器之后便可以执行rebalance操作， 目前rebalance主要分为两步：加入组和同步更新分配方案  

        加入组：协调器group中选择一个consumer担任leader，并把所有成员信息以及它们的订阅信息发送给leader
        同步更新分配方案：leader在这一步开始制定分配方案，即根据前面提到的分配策略决定每个consumer都负责那些topic的哪些分区，一旦分配完成，leader会把这个分配方案封装进SyncGroup请求并发送给协调器。注意组内所有成员都会发送SyncGroup请求，不过只有leader发送的SyncGroup请求中包含分配方案。协调器接收到分配方案后把属于每个consumer的方案单独抽取出来作为SyncGroup请求的response返还给给自的consumer

3. consumer group分配方案是在consumer端执行的

&emsp; **rebalance监听器**  
&emsp; rebalance监听器：最常见的用法是手动提交位移到第三方存储（比如数据库中）以及在rebalance前后执行一些必要的审计操作。有一个主要的接口回调类ConsumerRebalanceListener，里面就两个方法onParitionsRevoked和onPartitionAssigned。在coordinator开启新一轮rebalance前onParitionsRevoked方法会被调用，而rebalance完成后会调用onPartitionAssigned方法。  
&emsp; 使用rebalance监听器的前提是用户使用consumer group。如果使用的是独立consumer或是直接手动分配分区，那么rebalance监听器是无效的。  

## 1.7. 多线程消费实例  

<!-- 
KafkaConsumer是非线程安全的，那么怎么样实现多线程消费？
https://www.cnblogs.com/luozhiyun/p/11811835.html
-->

<!-- 
~~
Kafka Consumer多线程实例
https://blog.csdn.net/matrix_google/article/details/80035222?utm_source=blogxgwz8
-->

&emsp; Kafka Consumer不是线程安全的，所以实现多线程时通常由两种实现方法：  
1. 每个线程维护一个KafkaConsumer  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-59.png)  
2. 维护一个或多个KafkaConsumer，同时维护多个事件处理线程(worker thread)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-60.png)  

&emsp; 两种方法的优缺点：   

|  | 优点| 缺点| 
|--- |--- |--- | 
|方法1(每个线程维护一个KafkaConsumer)|方便实现</br>速度较快，因为不需要任何线程间交互</br>易于维护分区内的消息顺序	|更多的TCP连接开销(每个线程都要维护若干个TCP连接)</br>consumer数受限于topic分区数，扩展性差</br>频繁请求导致吞吐量下降</br>线程自己处理消费到的消息可能会导致超时，从而造成rebalance|
|方法2 (单个(或多个)consumer，多个worker线程)|可独立扩展consumer数和worker数，伸缩性好| 实现麻烦</br>通常难于维护分区内的消息顺序</br>处理链路变长，导致难以保证提交位移的语义正确性 |

