
<!-- TOC -->

- [1. kafka消费者开发](#1-kafka消费者开发)
    - [1.1. 构建consumer](#11-构建consumer)
        - [1.1.1. consumer程序实例](#111-consumer程序实例)
        - [1.1.2. consumer参数](#112-consumer参数)
    - [1.2. 消费者与消费组](#12-消费者与消费组)
    - [1.3. 订阅topic](#13-订阅topic)
        - [1.3.1. 订阅topic列表](#131-订阅topic列表)
        - [1.3.2. 基于正则表达订阅topic](#132-基于正则表达订阅topic)
    - [1.4. 消息轮询](#14-消息轮询)
    - [1.5. 消费者位移(offset)管理](#15-消费者位移offset管理)
    - [1.6. 消费者组重平衡(rebalance)](#16-消费者组重平衡rebalance)
        - [1.6.1. 重平衡简介](#161-重平衡简介)
        - [1.6.2. 重平衡触发条件](#162-重平衡触发条件)
        - [1.6.3. 重平衡流程](#163-重平衡流程)
            - [1.6.3.1. 协调者](#1631-协调者)
            - [1.6.3.2. 交互方式](#1632-交互方式)
            - [1.6.3.3. 流程](#1633-流程)
            - [1.6.3.4. 分配策略](#1634-分配策略)
            - [1.6.3.5. rebalance generation](#1635-rebalance-generation)
            - [1.6.3.6. rebalance监听器](#1636-rebalance监听器)
        - [1.6.4. 重平衡劣势](#164-重平衡劣势)
        - [1.6.5. 如何避免重平衡？](#165-如何避免重平衡)
    - [1.7. 多线程消费实例](#17-多线程消费实例)

<!-- /TOC -->

# 1. kafka消费者开发
&emsp; **<font color = "lime">参考《kafka实战》</font>**  

<!-- 
https://blog.csdn.net/lwglwg32719/article/details/86510029
-->

## 1.1. 构建consumer
### 1.1.1. consumer程序实例
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

### 1.1.2. consumer参数
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


## 1.2. 消费者与消费组  
&emsp; kafka消费者分为两类：  

* 消费者组(consumer group)：由多个消费者实例构成一个整体进行消费
* 独立消费者(standalone consumer)：单独执行消费操作

&emsp; **<font color = "red">消费者组的特点：</font>**

* 对于同一个group而言，topic的每条消息只能发送到group下一个consumer实例上  
* topic消息可以发送到多个group中  

&emsp; 如下图所示，某个主题中共有4个分区（Partition） : PO、Pl、P2、P3。有两个消费组A 和B都订阅了这个主题，消费组A中有4个消费者（CO、Cl、C2和C3），消费组B中有2 个消费者（C4和C5）。按照Kafka默认的规则，最后的分配结果是消费组A中的每一个消费 者分配到1个分区，消费组B中的每一个消费者分配到2个分区，两个消费组之间互不影响。 每个消费者只能消费所分配到的分区中的消息。换言之，每一个分区只能被一个消费组中的一 个消费者所消费。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-65.png)  
&emsp; 再来看一下消费组内的消费者个数变化时所对应的分区分配的演变。假设目前某消费组内只有一个消费者CO，订阅了一个主题，这个主题包含7个分区：PO、Pl、P2、P3、P4、 P5、P6。也就是说，这个消费者CO订阅了7个分区，具体分配情形参考下图。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-66.png)  
&emsp; 此时消费组内又加入了一个新的消费者C1，按照既定的逻辑，需要将原来消费者CO的部分分区分配给消费者C1消费，如下图所示。消费者CO和Cl各自负责消费所分配到的分区，彼此之间并无逻辑上的干扰。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-67.png)  
&emsp; 紧接着消费组内又加入了一个新的消费者C2，消费者CO、Cl和C2按照下图中的方式 各自负责消费所分配到的分区。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-68.png)  
&emsp; 消费者与消费组这种模型可以让整体的消费能力具备横向伸缩性，可以增加（或减少）消费者的个数来提高（或降低）整体的消费能力。**<font color = "red">对于分区数固定的情况，一味地增加消费者 并不会让消费能力一直得到提升，如果消费者过多，出现了消费者的个数大于分区个数的情况, 就会有消费者分配不到任何分区。</font>** 参考下图，一共有8个消费者，7个分区，那么最后的消费者C7由于分配不到任何分区而无法消费任何消息。
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-69.png)  

&emsp; **Kafka通过消费者组，可以实现基于队列和基于发布/订阅的两种消息引擎：**  

* 如果所有的消费者都隶属于同一个消费组，那么所有的消息都会被均衡地投递给每一 个消费者，即每条消息只会被一个消费者处理，这就相当于点对点模式的应用。  
* 如果所有的消费者都隶属于不同的消费组，那么所有的消息都会被广播给所有的消费 者，即每条消息会被所有的消费者处理，这就相当于发布/订阅模式的应用。


<!-- 

* 实现基于队列的模型：所有consumer实例都属于相同的group，每条消息只会被一个consumer实例处理。  
* 实现基于发布/订阅模型：consumer实例都属于不同group，这样kafka消息会被广播到所有consumer实例上。  

-->

## 1.3. 订阅topic
&emsp; **两种订阅topic的方式：直接订阅topic列表，基于正则表达订阅topic。**  

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
&emsp; **consumer 采用 pull（拉）模式从 broker 中读取数据。**  
&emsp; push（推）模式很难适应消费速率不同的消费者，因为消息发送速率是由 broker 决定的。 它的目标是尽可能以最快速度传递消息，但是这样很容易造成 consumer 来不及处理消息，典型的表现就是拒绝服务以及网络拥塞。而 pull 模式则可以根据 consumer 的消费能力以适当的速率消费消息。  
&emsp; 对于 Kafka 而言，pull 模式更合适，它可简化 broker 的设计，consumer 可自主控制消费消息的速率，同时consumer可以自己控制消费方式——即可批量消费也可逐条消费，同时还能选择不同的提交方式从而实现不同的传输语义。  
&emsp; pull 模式不足之处是，如果 kafka 没有数据，消费者可能会陷入循环中，一直等待数据到达。为了避免这种情况，在拉取请求中有参数，允许消费者请求在等待数据到达 的“长轮询”中进行阻塞（并且可选地等待到给定的字节数，以确保大的传输大小）。  

&emsp; **kafka采用类似于Linux I/O模型的poll或select等，使用一个线程来同时管理多个socket连接，即同时与多个broker通信实现消息的并行读取。**  
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

## 1.5. 消费者位移(offset)管理
<!-- 
kakfa消费位移
https://blog.csdn.net/haogenmin/article/details/109488571

https://www.kancloud.cn/nicefo71/kafka/1471593
-->
&emsp; 需要明确指出的是，这里的offset指代的是consumer端的offset，与分区日志中的offset是不同的含义。每个consumer实例都会为它消费的分区维护属于自己的位置信息来记录当前 消费了多少条消息。这在Kafka中有一个特有的术语：位移(offset)。kafka consumer在内部使用一个map来保存其订阅topic所属分区的offset，key是group.id、topic和分区的元组，value就是位移值。Kafka同时还引入了检查点机制定期对位移进行持久化。  
&emsp; consumer客户端需要定期地向Kafka集群汇报自己消费数据的进度，这一过程被称为位移提交(offset commit)。位移提交这件事情对于consumer而言非常重要，它不仅表征了 consumer端的消费进度，同时也直接决定了consumer端的消费语义保证。    
 
&emsp; consumer会在kafka集群的所有broker中选择一个broker作为consumer group的coordinator（协调者），用于实现组成员管理、消费分配方案制定以及提交位移等。  
&emsp; 当consumer运行了一段时间之后，它必须要提交自己的位移值。consumer提交位移的主要机制是通过向所属的coordinator发送位移提交请求来实现的。**consumer把位移提交到kafka的一个内部topic（__consumer_offsets）上。**通常不能直接操作该topic，特别注意不要擅自删除或搬移该topic的日志文件。每个位移提交请求都会往内部topic（__consumer_offsets）对应分区上追加写入一条消息。  
&emsp; 消息的key是group.id、topic和分区的元组，value就是位移值。  
 
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
消费者组重平衡全流程解析
https://www.kancloud.cn/nicefo71/kafka/1473378
-->
### 1.6.1. 重平衡简介
&emsp; 假设组内某个实例挂掉了，Kafka能够自动检测到，然后把这个Failed实例之前负责的分区转移给其他活着的消费者，这个过程称之为重平衡(Rebalance)。可以保障高可用性。  
&emsp; 除此之外，它协调着消费组中的消费者分配和订阅topic分区，比如某个Group 下有 20 个 Consumer 实例，它订阅了一个具有 100 个分区的 Topic。正常情况下，Kafka 平均会为每个 Consumer 分配 5 个分区。这个分配的过程也叫 Rebalance。再比如此刻新增了消费者，得分一些分区给它吧，这样才可以负载均衡以及利用好资源，那么这个过程也是 Rebalance 来完成的。  

&emsp; rebalance本质上是一组协议，它规定了一个consumer group是如何达成一致来分配订阅topic的所有分区的。coordinator负责对组执行rebalance操作。  
&emsp; 在 Rebalance 过程中，所有 Consumer 实例共同参与，在协调者组件（Coordinator，专门为 Consumer Group 服务，负责为 Group 执行 Rebalance 以及提供位移管理和组成员管理）的帮助下，完成订阅主题分区的分配。  

### 1.6.2. 重平衡触发条件
&emsp; **消费者组rebalance触发的条件，满足其一即可：**  
1. 组成员发生变更，比如新consumer加入组，或已有consumer主动离开组，再或是已有consumer崩溃时则触rebalance。（consumer崩溃的情况，有可能是consumer进程“挂掉”或consumer进程所在的机器宕机，也有可能是consumer无法在指定的时间内完成消息的处理。）
2. 组订阅topic数发生变更，比如使用基于正则表达式的订阅，当匹配正则表达式的新topic被创建时则会触发rebalance。
3. 组订阅topic的分区数发生变更，比如使用命令行脚本增加了订阅topic的分区数。

### 1.6.3. 重平衡流程
<!-- 
https://mp.weixin.qq.com/s/UiSpj3WctvdcdXXAwjcI-Q
-->
#### 1.6.3.1. 协调者
&emsp;再均衡，将分区所属权分配给消费者。因此需要和所有消费者通信，这就需要引进一个协调者的概念，由协调者为消费组服务，为消费者们做好协调工作。在Kafka中，每一台Broker上都有一个协调者组件，负责组成员管理、再均衡和提交位移管理等工作。如果有N台Broker，那就有N个协调者组件，而一个消费组只需一个协调者进行服务，那该**由哪个Broker为其服务？** 确定Broker需要两步：  
1. 计算分区号
&emsp;partition = Math.abs(groupID.hashCode() % offsetsTopicPartitionCount)  
&emsp;根据groupID的哈希值，取余offsetsTopicPartitionCount（内部主题__consumer_offsets的分区数，默认50）的绝对值，其意思就是把消费组哈希散列到内部主题__consumer_offsets的一个分区上。确定协调者为什么要和内部主题扯上关系。这就跟协调者的作用有关了。协调者不仅是负责组成员管理和再均衡，在协调者中还需要负责处理消费者的偏移量提交，而偏移量提交则正是提交到__consumer_offsets的一个分区上。所以这里需要取余offsetsTopicPartitionCount来确定偏移量提交的分区。  
2. 找出分区Leader副本所在的Broker  
&emsp;确定了分区就简单了，分区Leader副本所在的Broker上的协调者，就是要找的。  

&emsp;这个算法通常用于帮助定位问题。当一个消费组出现问题时，可以先确定协调者的Broker，然后查看Broker端的日志来定位问题。  

#### 1.6.3.2. 交互方式  
&emsp; 协调者和消费者之间是如何交互的？协调者如何掌握消费者的状态，又如何通知再均衡。这里使用了心跳机制。在消费者端有一个专门的心跳线程负责以heartbeat.interval.ms的间隔频率发送心跳给协调者，告诉协调者自己还活着。同时协调者会返回一个响应。而当需要开始再均衡时，协调者则会在响应中加入REBALANCE_IN_PROGRESS，当消费者收到响应时，便能知道再均衡要开始了。  
&emsp; 由于再平衡的开始依赖于心跳的响应，所以heartbeat.interval.ms除了决定心跳的频率，也决定了再均衡的通知频率。  

#### 1.6.3.3. 流程  
1. 当消费者收到协调者的再均衡开始通知时，需要立即提交偏移量；  
2. 消费者在收到提交偏移量成功的响应后，再发送JoinGroup请求，重新申请加入组，请求中会含有订阅的主题信息；  
3. 当协调者收到第一个JoinGroup请求时，会把发出请求的消费者指定为Leader消费者，同时等待rebalance.timeout.ms，在收集其他消费者的JoinGroup请求中的订阅信息后，将订阅信息放在JoinGroup响应中发送给Leader消费者，并告知他成为了Leader，同时也会发送成功入组的JoinGroup响应给其他消费者；  
4. Leader消费者收到JoinGroup响应后，根据消费者的订阅信息制定分配方案，把方案放在SyncGroup请求中，发送给协调者。普通消费者在收到响应后，则直接发送SyncGroup请求，等待Leader的分配方案；  
5. 协调者收到分配方案后，再通过SyncGroup响应把分配方案发给所有消费组。  
6. 当所有消费者收到分配方案后，就意味着再均衡的结束，可以正常开始消费工作了。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-70.png)  

#### 1.6.3.4. 分配策略  
&emsp; kafka新版本consumer默认提供了3种分配策略，分别是range策略、round-robin策略和sticky策略。

* range策略：基于范围的思想，将单个topic的所有分区按照顺序排列，然后把这些分区划分成固定大小的分区段并依次分配给每个consumer。
* round-robin策略：把所有topic的所有分区顺序排开，然后轮询式地分配给各个consumer。
* sticky策略：有效地避免了上述两种策略完全无视历史分配方案的缺陷，采用了“有黏性”的策略对所有consumer实例进行分配，可以规避极端情况下的数据倾斜而且在两次rebalance间最大限度地维持了之前的分配方案。

&emsp; 如果group下所有consumer实例的订阅是相同的，那么使用round-robin会带来更公平的分配方案，否则使用range策略的效果更好。  
&emsp; 新版本consumer默认的分配策略是range，用户根据consumer参数partition.assingment.strategy来进行配置。另外kafka支持自定义的分配策略，用户可以创建自己的consumer分配器assignor。  


#### 1.6.3.5. rebalance generation  
&emsp; rebalance generation：用于标识某次rebalance,每个consumer group进行rebalance后，generation就会加1，表示group进入一个新的版本，generation从0开始。  
&emsp; consumer group可以执行任意次rebalance，generation是为了防止无效offset提交，延迟的offset提交携带的是旧的generation信息，这次提交就会被consumer group拒绝。  

#### 1.6.3.6. rebalance监听器  
&emsp; rebalance监听器：最常见的用法是手动提交位移到第三方存储（比如数据库中）以及在rebalance前后执行一些必要的审计操作。有一个主要的接口回调类ConsumerRebalanceListener，里面就两个方法onParitionsRevoked和onPartitionAssigned。在coordinator开启新一轮rebalance前onParitionsRevoked方法会被调用，而rebalance完成后会调用onPartitionAssigned方法。  
&emsp; 使用rebalance监听器的前提是用户使用consumer group。如果使用的是独立consumer或是直接手动分配分区，那么rebalance监听器是无效的。  


### 1.6.4. 重平衡劣势
&emsp; **重平衡的劣势：**  
&emsp; 第一：Rebalance 影响 Consumer 端 TPS，对 Consumer Group 消费过程有极大的影响。我们知道 JVM 的垃圾回收机制，那可怕的万物静止的收集方式，即 stop the world，所有应用线程都会停止工作，整个应用程序僵在那边一动不动。类似，在 Rebalance 期间，Consumer 会停下手头的事情，什么也干不了。  
&emsp; 第二：Rebalance 很慢。如果你的 Group 下成员很多，就一定会有这样的痛点。某真实案例：Group 下有几百个 Consumer 实例，Rebalance 一次要几个小时。万物静止几个小时是非常可怕的一件事了，老板可能要提大刀来相见了。  
&emsp; 为什么会这么慢呢？因为目前 Rebalance 的设计是让所有 Consumer 实例共同参与，全部重新分配所有分区。其实应该尽量保持之前的分配，目前 kafka 社区也在对此进行优化，在 0.11 版本提出了 StickyAssignor，即有粘性的分区分配策略。所谓的有粘性，是指每次 Rebalance 时，该策略会尽可能地保留之前的分配方案。不过不够完善，有 bug，暂时不建议使用。  

### 1.6.5. 如何避免重平衡？
&emsp; **如何避免 Rebalances？**  
<!-- 
https://blog.csdn.net/haogenmin/article/details/109489704
-->
&emsp; 由于目前一次rebalance操作的开销很大，生产环境中用户一定要结合自身业务特点仔细调优consumer参数：request.timeout.ms、max.poll.records和max.poll.interval.ms，以避免不必要的rebalance出现。  



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

