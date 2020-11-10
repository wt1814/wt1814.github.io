
<!-- TOC -->

- [1. kafka消费者开发](#1-kafka消费者开发)
    - [1.1. consumer概览](#11-consumer概览)
        - [1.1.1. 消费者分组](#111-消费者分组)
        - [1.1.2. 位移(offset)](#112-位移offset)
        - [1.1.3. 消费者重平衡](#113-消费者重平衡)
    - [1.2. 构建consumer](#12-构建consumer)
        - [1.2.1. Kafka consumer 参数](#121-kafka-consumer-参数)
    - [1.3. 订阅topic](#13-订阅topic)
        - [1.3.1. 订阅topic列表](#131-订阅topic列表)
        - [1.3.2. 基于正则表达订阅topic](#132-基于正则表达订阅topic)
    - [1.4. 消息轮询](#14-消息轮询)
    - [1.5. 位移管理](#15-位移管理)
    - [1.6. 重平衡(rebalance)](#16-重平衡rebalance)
    - [1.7. 多线程消费实例](#17-多线程消费实例)

<!-- /TOC -->

# 1. kafka消费者开发
&emsp; **<font color = "lime">参考《kafka实战》</font>**  
<!-- 
https://mp.weixin.qq.com/s/kguKr_k-BrcQz4G5gag8gg
-->
## 1.1. consumer概览  

### 1.1.1. 消费者分组  
&emsp; kafka消费者分为两类：  

* 消费者组(consumer group)：单独执行消费操作
* 独立消费者(standalone consumer)：由多个消费者实例构成一个整体进行消费

消费者使用一个消费者组名group.id来标记自己，topic的每条消息都只会被发送到每个订阅它的消费者组的一个消费者实例上。  
&emsp; kafka同时支持基于队列和基于发布/订阅的两种消息引擎模型：

* 实现基于队列的模型：所有consumer实例都属于相同的group，每条消息只会被一个consumer实例处理。  
* 实现基于发布/订阅模型：consumer实例都属于不同group，这样kafka消息会被广播到所有consumer实例上。  

消费者组好处：consumer group用于实现高伸缩性、高容错性的consumer机制。组内多个consumer实例可以同时读取kafka消息，而且一旦有某个consumer挂了，consumer group会立即将已崩溃consumer负责的分区转交给其他consumer来负责，从而保证整个group可以继续操作，不会丢失数据，这个过程被称为重平衡rebalance。  
kafka在为consumer group成员分配分区时可以做到公平的分配。  
由于目前kafka只提供单个分区内的消息顺序，而不会维护全局的消息顺序，因此如果用户要实现topic全局的消息读取顺序，就只能通过让每个consumer group下只包含一个consumer实例的方式来间接实现。  
&emsp; 每个消费者只能消费所分配到的分区的消息，每一个分区只能被一个消费组中的一个消费者所消费，所以同一个消费组中消费者的数量如果超过了分区的数量，将会出现有些消费者分配不到消费的分区。消费组与消费者关系如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-32.png)  

### 1.1.2. 位移(offset)  
&emsp; 需要明确指出的是，这里的offset指代的是consumer端的offset,与分区日志中的offset是不同的含义。每个consumer实例都会为它消费的分区维护属于自己的位置信息来记录当前 消费了多少条消息。这在Kafka中有一个特有的术语：位移(offset)。
&emsp; consumer客户端需要定期地向Kafka集群汇报自己消费数据的进度，这一过程被称为位移提交(offset commit)。位移提交这件事情对于consumer而言非常重要，它不仅表征了 consumer端的消费进度，同时也直接决定了 consumer端的消费语义保证。  

kafka让consumer group保存offset，保存成了一个长整型数据就行，同时kafka consumer还引入了检查点机制checkpointing定期对offset进行持久化，从而简化了应答机制的实现。  
kafka consumer在内部使用一个map来保存其订阅topic所属分区的offset，key是group.id、topic和分区的元组，value就是位移值。  
consumer把位移提交到kafka的一个内部topic（__consumer_offsets）上，通常不能直接操作该topic，特别注意不要擅自删除或搬移该topic的日志文件。  

### 1.1.3. 消费者重平衡  
&emsp; 标题中特意强调了 consumer group0如果用户使用的是standalone consumer,则压根就没 有 rebalance 的概念，即 rebalance 只对 consumer group有效。
&emsp; 何为rebalance？它本质上是一种协议，规定了一个consumer group下所有consumer如何达成一致来分配订阅topic的所有分区。举个例子，假设有一个consumer group，它有20个consumer实例。该group订阅了一个具有100个分区的topic。那么正常情况下，consumer group平均会为每个consumer分配5个分区，即每个consumer负责读取5个分区的数据。这 个分配过程就被称作rebalance。  

## 1.2. 构建consumer

### 1.2.1. Kafka consumer 参数

* bootstrap.servers：连接 broker 地址，host：port 格式。
* group.id：消费者隶属的消费组。
* key.deserializer：与生产者的key.serializer对应，key 的反序列化方式。
* value.deserializer：与生产者的value.serializer对应，value 的反序列化方式。
* session.timeout.ms：coordinator 检测失败的时间。默认 10s 该参数是 Consumer Group 主动检测 （组内成员 comsummer) 崩溃的时间间隔，类似于心跳过期时间。
* auto.offset.reset：该属性指定了消费者在读取一个没有偏移量后者偏移量无效（消费者长时间失效当前的偏移量已经过时并且被删除了）的分区的情况下，应该作何处理，默认值是 latest，也就是从最新记录读取数据（消费者启动之后生成的记录），另一个值是 earliest，意思是在偏移量无效的情况下，消费者从起始位置开始读取数据。
* enable.auto.commit：否自动提交位移，如果为false，则需要在程序中手动提交位移。对于精确到一次的语义，最好手动提交位移
* fetch.max.bytes：单次拉取数据的最大字节数量
* max.poll.records：单次 poll 调用返回的最大消息数，如果处理逻辑很轻量，可以适当提高该值。但是max.poll.records条数据需要在在 session.timeout.ms 这个时间内处理完 。默认值为 500
* request.timeout.ms：一次请求响应的最长等待时间。如果在超时时间内未得到响应，kafka 要么重发这条消息，要么超过重试次数的情况下直接置为失败。

## 1.3. 订阅topic

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
3. 基于正则表达式订阅topic
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
-kafka采用类似于Linux I/O模型的poll或select等，使用一个线程来同时管理多个socket连接，即同时与多个broker通信实现消息的并行读取。

-一旦consumer订阅了topic，所有的消费逻辑包括coordinator的协调、消费者组的rebalance以及数据的获取都会在主逻辑poll方法的一次调用中被执行。这样用户可以很容易使用一个线程来管理所有的consumer I/O操作。

-新版本Java consumer是一个双线程的Java进程：创建KafkaConsumer的线程被称为用户主线程，同时consumer在后台会创建一个心跳线程，该线程被称为后台心跳线程。KafkaConsumer的poll方法在用户主线程中运行。  

poll方法根据当前consumer的消费位移返回消息集合。poll方法中有个超时的参数，为了让consumer有机会在等待kafka消息的同时还能够定期执行其他任务。  

poll方法返回满足以下任意一个条件即可返回：  

-要么获取了足够多的可用数据  

-要么等待时间超过了指定的超时设置  

poll的使用方法：  

-consumer需要定期执行其他子任务：推荐poll(较小超时时间) + 运行标识布尔变量while(true)的方式  

-consumer不需要定期执行子任务：推荐poll(MAX_VALUE) + 捕获WakeupException的方式  

poll(MAX_VALUE)让consumer程序在未获取到足够多数据时无限等待，然后通过捕获WakeupException异常来判断consumer是否结束。  

使用这种方式调用poll，那么需要在另一个线程中调用consumer.wakeup()方法来触发consumer的关闭。  

KafkaConsumer不是线程安全的，但是有一个例外：用户可以安全地在另一个线程中调用consumer.wakeup()。注意，只有wakeup方法是特例，其他KafkaConsumer方法都不能同时在多线程中使用。  


## 1.5. 位移管理
consumer会在kafka集群的所有broker中选择一个broker作为consumer group的coordinator，用于实现组成员管理、消费分配方案制定以及提交位移等。  

当consumer运行了一段时间之后，它必须要提交自己的位移值。consumer提交位移的主要机制是通过向所属的coordinator发送位移提交请求来实现的。每个位移提交请求都会往内部topic（__consumer_offsets）对应分区上追加写入一条消息。  

消息的key是group.id、topic和分区的元组，value就是位移值。  

-自动提交  

使用方法：默认不用配置，或者显示配置enable.auto.commit=true，用auto.commit.interval.ms参数控制自动提交的间隔。  

使用场景：对消息交付语义无需求，容忍一定的消息丢失。  

-手动提交  

使用方法：设置enable.auto.commmit=false;手动调用KafkaConsumer.commitSync或KafkaConsumer.commitAsync提交位移  

使用场景：消息处理逻辑重，不允许消息丢失，至少要求“最少一次”处理语义  

commitSync：同步手动提交，用户程序会等待位移提交结束才执行下一条语句命令。  

commitAsync：异步手动提交，就是异步非阻塞，consumer在后续poll调用时轮询该位移提交的结果。  

commitSync和commitAsync都有带参数的重载方法，目的是实现更加细粒度化的位移提交策略，指定一个Map显示地告诉kafka为哪些分区提交位移，consumer.commitSync(Collections.singletonMap(partition,new OffsetAndMetadata(lastOffset + 1)))。  

提交的位移一定是consumer下一条待读消息的位移。  
## 1.6. 重平衡(rebalance)

什么时候 rebalance？

这也是经常被提及的一个问题。rebalance 的触发条件有三种：

    组成员发生变更（新 consumer 加入组、已有 consumer 主动离开组或已有 consumer 崩溃了——这两者的区别后面会谈到）
    订阅主题数发生变更
    订阅主题的分区数发生变更

如何进行组内分区分配？

Kafka 默认提供了两种分配策略：Range 和 Round-Robin。当然 Kafka 采用了可插拔式的分配策略，你可以创建自己的分配器以实现不同的分配策略。

---
rebalance本质上是一组协议，它规定了一个consumer group是如何达成一致来分配订阅topic的所有分区的。coordinator负责对组执行rebalance操作。

组rebalance触发的条件，满足其一即可：

1.组成员发生变更，比如新consumer加入组，或已有consumer主动离开组，再或是已有consumer崩溃时则触rebalance。

2.组订阅topic数发生变更，比如使用基于正则表达式的订阅，当匹配正则表达式的新topic被创建时则会触发rebalance。

3.组订阅topic的分区数发生变更，比如使用命令行脚本增加了订阅topic的分区数。

consumer崩溃的情况，有可能是consumer进程“挂掉”或consumer进程所在的机器宕机，也有可能是consumer无法在指定的时间内完成消息的处理。

由于目前一次rebalance操作的开销很大，生产环境中用户一定要结合自身业务特点仔细调优consumer参数：request.timeout.ms、max.poll.records和max.poll.interval.ms，以避免不必要的rebalance出现。

kafka新版本consumer默认提供了3种分配策略，分别是range策略、round-robin策略和sticky策略。

-range策略：基于范围的思想，将单个topic的所有分区按照顺序排列，然后把这些分区划分成固定大小的分区段并依次分配给每个consumer。

-round-robin策略：把所有topic的所有分区顺序排开，然后轮询式地分配给各个consumer。

-sticky策略：有效地避免了上述两种策略完全无视历史分配方案的缺陷，采用了“有黏性”的策略对所有consumer实例进行分配，可以规避极端情况下的数据倾斜而且在两次rebalance间最大限度地维持了之前的分配方案。

如果group下所有consumer实例的订阅是相同的，那么使用round-robin会带来更公平的分配方案，否则使用range策略的效果更好。

新版本consumer默认的分配策略是range，用户根据consumer参数partition.assingment.strategy来进行配置。另外kafka支持自定义的分配策略，用户可以创建自己的consumer分配器assignor。

rebalance generation：用于标识某次rebalance,每个consumer group进行rebalance后，generation就会加1，表示group进入一个新的版本，generation从0开始。

consumer group可以执行任意次rebalance，generation是为了防止无效offset提交，延迟的offset提交携带的是旧的generation信息，这次提交就会被consumer group拒绝。

rebalance监听器：最常见的用法是手动提交位移到第三方存储（比如数据库中）以及在rebalance前后执行一些必要的审计操作。有一个主要的接口回调类ConsumerRebalanceListener，里面就两个方法onParitionsRevoked和onPartitionAssigned。在coordinator开启新一轮rebalance前onParitionsRevoked方法会被调用，而rebalance完成后会调用onPartitionAssigned方法。

使用rebalance监听器的前提是用户使用consumer group。如果使用的是独立consumer或是直接手动分配分区，那么rebalance监听器是无效的。

## 1.7. 多线程消费实例  
多线程消费---两种实现方式

 	方法1.每个线程维护专属KafkaConsumer	方法2.全局consumer+多worker线程
offset提交方式	自动提交	手动提交
优点	
实现简单，速度快，因为没有线程间交互开销，

方便位移管理，易于维护

分区间的消息消费顺序

消息获取与处理解耦，

可独立扩展consumer数和worker数，

伸缩性好

缺点	
socket连接开销大；

consumer数受限于topic分区数，扩展性差；

broker端处理负载高，因为发往broker的请求数多；

rebalance可能性增大

实现负载；

难于维护分区内的消息顺序；

处理链路变长，导致位移管理困难；

worker线程异常可能导致消费数据丢失


