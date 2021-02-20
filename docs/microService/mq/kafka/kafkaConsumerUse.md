
<!-- TOC -->

- [1. kafka消费者开发](#1-kafka消费者开发)
    - [1.1. 构建consumer](#11-构建consumer)
        - [1.1.1. consumer程序实例](#111-consumer程序实例)
        - [1.1.2. consumer配置](#112-consumer配置)
    - [1.2. 消费者与消费组](#12-消费者与消费组)
    - [1.3. ※※※消费者组重平衡(rebalance)](#13-※※※消费者组重平衡rebalance)
        - [1.3.1. 重平衡简介](#131-重平衡简介)
        - [1.3.2. 重平衡触发条件](#132-重平衡触发条件)
        - [1.3.3. 重平衡流程](#133-重平衡流程)
            - [1.3.3.1. 协调者](#1331-协调者)
            - [1.3.3.2. 交互方式](#1332-交互方式)
            - [1.3.3.3. 流程](#1333-流程)
            - [1.3.3.4. 分配策略](#1334-分配策略)
        - [1.3.4. 重平衡劣势](#134-重平衡劣势)
        - [1.3.5. 如何避免重平衡？](#135-如何避免重平衡)
    - [1.4. ※※※消费者位移(offset)管理](#14-※※※消费者位移offset管理)
        - [1.4.1. 位移提交方式](#141-位移提交方式)
        - [1.4.2. 位移主题(__consumer_offsets) ，(位移提交地址)](#142-位移主题__consumer_offsets-位移提交地址)
        - [1.4.3. 消费组消费进度查看的命令](#143-消费组消费进度查看的命令)
    - [1.5. 订阅topic](#15-订阅topic)
        - [1.5.1. 订阅topic列表](#151-订阅topic列表)
        - [1.5.2. 基于正则表达订阅topic](#152-基于正则表达订阅topic)
    - [1.6. 消息轮询](#16-消息轮询)
    - [1.7. 消费语义](#17-消费语义)
    - [1.8. 多线程消费实例](#18-多线程消费实例)

<!-- /TOC -->

&emsp; 总结：  
&emsp; **消费者组重平衡：**
2. **<font color = "red">消费者在收到提交偏移量成功的响应后，再发送JoinGroup请求，重新申请加入组，请求中会含有订阅的主题信息；</font>**  
3. **<font color = "red">当协调者收到第一个JoinGroup请求时，会把发出请求的消费者指定为Leader消费者，</font>**

&emsp; **消费者位移提交：** Kafka消费者通过自动提交/手动提交位移信息到位移主题(__consumer_offsets)。  

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
1. 构造一个java.util.Properties对象，至少指定 bootstrap.serversn、key.deserializer、value.deserializer和group.id的值，即上面代码中显示指明必须指定带注释的4个参数。
2. 使用上一步创建的Properties实例构造KafkaConsumer对象。
3. 调用KafkaConsumer.subscribe方法订阅consumer group感兴趣的topic列表。
4. 循环调用KafkaConsumer.poll方法获取封装在ConsumerRecord的topic消息。
5. 处理获取到的ConsumerRecord对象。
6. 关闭 KafkaConsumer。 

### 1.1.2. consumer配置
&emsp; Kafka为Java消费者提供的[配置](https://kafka.apachecn.org/documentation.html#configuration)如下：  

## 1.2. 消费者与消费组  
&emsp; **<font color = "red">kafka消费者分为两类：</font>**  

* 消费者组(consumer group)：由多个消费者实例构成一个整体进行消费
* 独立消费者(standalone consumer)：单独执行消费操作

&emsp; **<font color = "blue">Kafka消费端确保一个Partition在一个消费者组内只能被一个消费者消费。</font>   

* 在同一个消费者组内，一个Partition只能被一个消费者消费。
* 在同一个消费者组内，所有消费者组合起来必定可以消费一个Topic下的所有 Partition。
* 在同一个消费组内，一个消费者可以消费多个Partition的信息。
* 在不同消费者组内，同一个分区可以被多个消费者消费。
* 每个消费者组一定会完整消费一个Topic下的所有Partition。  

<!-- 
&emsp; <font color = "red">消费者组的特点：</font>

* 对于同一个group而言，topic的每条消息只能发送到group下一个consumer实例上  
* topic消息可以发送到多个group中  
-->

&emsp; **消费组示例：**  
&emsp; 如下图所示，某个主题中共有4个分区（Partition）：PO、Pl、P2、P3。有两个消费组A和B都订阅了这个主题，消费组A中有4个消费者（CO、Cl、C2和C3），消费组B中有2个消费者（C4和C5）。按照Kafka默认的规则，最后的分配结果是消费组A中的每一个消费者分配到1个分区，消费组B中的每一个消费者分配到2个分区，两个消费组之间互不影响。每个消费者只能消费所分配到的分区中的消息。换言之，每一个分区只能被一个消费组中的一个消费者所消费。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-65.png)  
&emsp; 再来看一下消费组内的消费者个数变化时所对应的分区分配的演变。假设目前某消费组内只有一个消费者CO，订阅了一个主题，这个主题包含7个分区：PO、Pl、P2、P3、P4、 P5、P6。也就是说，这个消费者CO订阅了7个分区，具体分配情形参考下图。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-66.png)  
&emsp; 此时消费组内又加入了一个新的消费者C1，按照既定的逻辑，需要将原来消费者CO的部分分区分配给消费者C1消费，如下图所示。消费者CO和Cl各自负责消费所分配到的分区，彼此之间并无逻辑上的干扰。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-67.png)  
&emsp; 紧接着消费组内又加入了一个新的消费者C2，消费者CO、Cl和C2按照下图中的方式 各自负责消费所分配到的分区。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-68.png)  
&emsp; 消费者与消费组这种模型可以让整体的消费能力具备横向伸缩性，可以增加（或减少）消费者的个数来提高（或降低）整体的消费能力。 **<font color = "red">对于分区数固定的情况，一味地增加消费者并不会让消费能力一直得到提升，如果消费者过多，出现了消费者的个数大于分区个数的情况, 就会有消费者分配不到任何分区。</font>** 参考下图，一共有8个消费者，7个分区，那么最后的消费者C7由于分配不到任何分区而无法消费任何消息。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-69.png)  

<!-- 
* 实现基于队列的模型：所有consumer实例都属于相同的group，每条消息只会被一个consumer实例处理。  
* 实现基于发布/订阅模型：consumer实例都属于不同group，这样kafka消息会被广播到所有consumer实例上。  
-->

## 1.3. ※※※消费者组重平衡(rebalance)
<!-- 
消费者组重平衡全流程解析
https://www.kancloud.cn/nicefo71/kafka/1473378
-->
### 1.3.1. 重平衡简介  
&emsp; **什么是重平衡？**  
&emsp; **<font color = "red">假设组内某个实例挂掉了，Kafka能够自动检测到，然后把这个Failed实例之前负责的分区转移给其他活着的消费者，这个过程称之为重平衡(Rebalance)。</font>** 可以保障高可用性。  
&emsp; 除此之外，它协调着消费组中的消费者分配和订阅topic分区，比如某个Group下有20个Consumer实例，它订阅了一个具有100个分区的Topic。正常情况下，Kafka平均会为每个Consumer分配5个分区。这个分配的过程也叫 Rebalance。再比如此刻新增了消费者，得分一些分区给它吧，这样才可以负载均衡以及利用好资源，那么这个过程也是Rebalance来完成的。  

&emsp; rebalance本质上是一组协议，它规定了一个consumer group是如何达成一致来分配订阅topic的所有分区的。在Rebalance过程中，所有Consumer实例共同参与，在协调者组件（Coordinator，专门为Consumer Group服务，负责为Group执行Rebalance以及提供位移管理和组成员管理）的帮助下，完成订阅主题分区的分配。  

### 1.3.2. 重平衡触发条件  
&emsp; **消费者组rebalance触发的条件，满足其一即可：**  
1. 组成员发生变更，比如新consumer加入组，或已有consumer主动离开组，再或是已有consumer崩溃时则触rebalance。（consumer崩溃的情况，有可能是consumer进程“挂掉”或consumer进程所在的机器宕机，也有可能是consumer无法在指定的时间内完成消息的处理。）
    * 消费组内某消费者宕机，触发 Repartition 操作，如下图所示。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-91.png)    
    * 消费组内新增消费者，触发 Repartition 操作，如下图所示。一般这种情况是为了提高消费端的消费能力，从而加快消费进度。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-92.png)   
2. 组订阅topic数发生变更，比如使用基于正则表达式的订阅，当匹配正则表达式的新topic被创建时则会触发rebalance。
3. 组订阅topic的分区数发生变更，比如使用命令行脚本增加了订阅topic的分区数。如下图所示。一般这种调整 Partition 个数的情况也是为了提高消费端消费速度的，因为当消费者个数大于等于 Partition 个数时，在增加消费者个数是没有用的（原因是：在一个消费组内，消费者:Partition = 1:N，当 N 小于 1 时，相当于消费者过剩了），所以一方面增加 Partition 个数同时增加消费者个数可以提高消费端的消费速度。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-90.png)    

### 1.3.3. 重平衡流程
<!-- 
https://mp.weixin.qq.com/s/UiSpj3WctvdcdXXAwjcI-Q
-->
#### 1.3.3.1. 协调者
&emsp; 重均衡，将分区所属权分配给消费者。因此需要和所有消费者通信，这就需要引进一个协调者的概念，由协调者为消费组服务，为消费者们做好协调工作。在Kafka中，每一台Broker上都有一个协调者组件，负责组成员管理、再均衡和提交位移管理等工作。如果有N台Broker，那就有N个协调者组件，而一个消费组只需一个协调者进行服务，那该**由哪个Broker为其服务？** 确定Broker需要两步：  
1. 计算分区号  
&emsp;partition = Math.abs(groupID.hashCode() % offsetsTopicPartitionCount)  
&emsp;根据groupID的哈希值，取余offsetsTopicPartitionCount（内部主题__consumer_offsets的分区数，默认50）的绝对值，其意思就是把消费组哈希散列到内部主题__consumer_offsets的一个分区上。确定协调者为什么要和内部主题扯上关系。这就跟协调者的作用有关了。协调者不仅是负责组成员管理和再均衡，在协调者中还需要负责处理消费者的偏移量提交，而偏移量提交则正是提交到__consumer_offsets的一个分区上。所以这里需要取余offsetsTopicPartitionCount来确定偏移量提交的分区。  
2. 找出分区Leader副本所在的Broker  
&emsp;确定了分区就简单了，分区Leader副本所在的Broker上的协调者，就是要找的。  

&emsp;这个算法通常用于帮助定位问题。当一个消费组出现问题时，可以先确定协调者的Broker，然后查看Broker端的日志来定位问题。  

#### 1.3.3.2. 交互方式  
&emsp; 协调者和消费者之间是如何交互的？协调者如何掌握消费者的状态，又如何通知再均衡。这里使用了心跳机制。在消费者端有一个专门的心跳线程负责以heartbeat.interval.ms的间隔频率发送心跳给协调者，告诉协调者自己还活着。同时协调者会返回一个响应。而当需要开始再均衡时，协调者则会在响应中加入REBALANCE_IN_PROGRESS，当消费者收到响应时，便能知道再均衡要开始了。  
&emsp; 由于再平衡的开始依赖于心跳的响应，所以heartbeat.interval.ms除了决定心跳的频率，也决定了再均衡的通知频率。  

#### 1.3.3.3. 流程  
1. 当消费者收到协调者的再均衡开始通知时，需要立即提交偏移量；  
2. **<font color = "red">消费者在收到提交偏移量成功的响应后，再发送JoinGroup请求，重新申请加入组，请求中会含有订阅的主题信息；</font>**  
3. **<font color = "red">当协调者收到第一个JoinGroup请求时，会把发出请求的消费者指定为Leader消费者，</font>** 同时等待rebalance.timeout.ms，在收集其他消费者的JoinGroup请求中的订阅信息后，将订阅信息放在JoinGroup响应中发送给Leader消费者，并告知它成为了Leader，同时也会发送成功入组的JoinGroup响应给其他消费者；  
4. Leader消费者收到JoinGroup响应后，根据消费者的订阅信息制定分配方案，把方案放在SyncGroup请求中，发送给协调者。普通消费者在收到响应后，则直接发送SyncGroup请求，等待Leader的分配方案；  
5. 协调者收到分配方案后，再通过SyncGroup响应把分配方案发给所有消费组。  
6. 当所有消费者收到分配方案后，就意味着再均衡的结束，可以正常开始消费工作了。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-70.png)  

#### 1.3.3.4. 分配策略  
&emsp; kafka新版本consumer默认提供了3种分配策略，分别是range策略、round-robin策略和sticky策略。

* range策略：基于范围的思想，将单个topic的所有分区按照顺序排列，然后把这些分区划分成固定大小的分区段并依次分配给每个consumer。
* round-robin策略：把所有topic的所有分区顺序排开，然后轮询式地分配给各个consumer。
* sticky策略：有效地避免了上述两种策略完全无视历史分配方案的缺陷，采用了“有黏性”的策略对所有consumer实例进行分配，可以规避极端情况下的数据倾斜而且在两次rebalance间最大限度地维持了之前的分配方案。

&emsp; 如果group下所有consumer实例的订阅是相同的，那么使用round-robin会带来更公平的分配方案，否则使用range策略的效果更好。  
&emsp; 新版本consumer默认的分配策略是range，用户根据consumer参数partition.assingment.strategy来进行配置。另外kafka支持自定义的分配策略，用户可以创建自己的consumer分配器assignor。  

### 1.3.4. 重平衡劣势
&emsp; **重平衡的劣势：**  
&emsp; 第一：Rebalance影响Consumer端TPS，对Consumer Group消费过程有极大的影响。类似JVM的stop the world，在Rebalance期间，Consumer会停下手头的事情，什么也干不了。  
&emsp; 第二：Rebalance很慢。如果Group下成员很多，就一定会有这样的痛点。某真实案例：Group 下有几百个Consumer实例，Rebalance 一次要几个小时。  
&emsp; 为什么会这么慢呢？因为目前Rebalance的设计是让所有Consumer实例共同参与，全部重新分配所有分区。其实应该尽量保持之前的分配，目前 kafka 社区也在对此进行优化，在0.11版本提出了StickyAssignor，即有粘性的分区分配策略。所谓的有粘性，是指每次Rebalance时，该策略会尽可能地保留之前的分配方案。不过不够完善，有bug，暂时不建议使用。  

### 1.3.5. 如何避免重平衡？
<!-- 
&emsp; **如何避免 Rebalances？**  
&emsp; 由于目前一次rebalance操作的开销很大，生产环境中用户一定要结合自身业务特点仔细调优consumer参数：request.timeout.ms、max.poll.records和max.poll.interval.ms，以避免不必要的rebalance出现。  
-->
&emsp; 刚刚说到三个触发机制，后面两者一般是用户主动操作，这不可避免，所以应该重点关注第一个场景，当然消费实例增加也是出于伸缩性的需求，所以其实只需要避免实例减少的情况就行了。  
&emsp; 实际中，不是主动kill消费成员，或者机器宕机那种情况，才算是被踢出组，消费时间过长，也是会被踢的。不仅如此，某些情况会让Coordinator错误地认为 Consumer 实例“已停止”从而被“踢出”Group。  
&emsp; 以下情景会让协调者认为消费者实例已经死亡并把它们踢出组。  
1. 未能及时发送心跳  
&emsp; Consumer实例未能及时发送心跳，导致 Coordinator 误认为它已经死亡。  
&emsp; 心跳机制都有阈值和频率的概念。阈值是 Coordinator 最长能接受的心跳间隔，默认10s，即超过10s还没收到心跳才认定consumer死亡，从而将其从Group中移除，然后开启新一轮Rebalance。频率是指Consumer发送心跳的频率。它俩对应的参数分为叫做session.timeout.ms，heartbeat.interval.ms，因此需要合理的设置这两个参数。  
&emsp; 千万不要无脑的觉得把频率调高点，阈值也调高点，比如1s 发一次心跳，并设置超过 1 分钟才可以认定为死亡，就完美避免了未能及时收到心跳请求而误认为死亡。发送心跳的目的就是为了及时通知协调者自己是否健康。所以session.timeout.ms这个参数，不宜最长。heartbeat.interval.ms这个值也不宜过短，频繁地发送心跳请求会额外消耗带宽资源。  

* 推荐设置 session.timeout.ms = 6s。
* 推荐设置 heartbeat.interval.ms = 2s。

&emsp; 保证Consumer实例在被判定为“dead”之前，能够发送至少 3 轮的心跳请求，因此上面推荐的配置是一个三倍的关系。  

2. 消费时间过长    
&emsp; Consumer因为处理消息的时间太长而引发Rebalance 。
&emsp; Consumer端有一个参数，用于控制Consumer实际消费能力对Rebalance的影响，即 max.poll.interval.ms 参数。它限定了Consumer 端应用程序两次调用 poll 方法的最大时间间隔。它的默认值是 5 分钟，表示Consumer 程序如果在 5 分钟之内无法消费完 poll 方法返回的消息，那么 Consumer 会主动发起“离开组”的请求，Coordinator 也会开启新一轮 Rebalance。因此你最好将该参数值设置得大一点，比下游最大处理时间稍长一点。还可以配置一个参数max.poll.records，它代表批量消费的 size，如果一次性 poll 的数据量过多，导致一次 poll 的处理无法在指定时间内完成，则会 Rebalance。因此，需要预估你的业务处理时间，并正确的设置这两个参数。  

## 1.4. ※※※消费者位移(offset)管理
<!-- 
-->
&emsp; 需要明确指出的是，这里的offset指代的是consumer端的offset，与分区日志中的offset是不同的含义。每个consumer实例都会为它消费的分区维护属于自己的位置信息来记录当前消费了多少条消息。这在Kafka中有一个特有的术语：位移(offset)。  
&emsp; kafka consumer在内部使用一个map来保存其订阅topic所属分区的offset，key是group.id、topic和分区的元组，value就是位移值。Kafka同时还引入了检查点机制定期对位移进行持久化。  

### 1.4.1. 位移提交方式  
<!-- 
https://blog.csdn.net/haogenmin/article/details/109488571
https://www.kancloud.cn/nicefo71/kafka/1471593
-->
&emsp; consumer客户端需要定期地向Kafka集群汇报自己消费数据的进度，这一过程被称为位移提交(offset commit)。位移提交这件事情对于consumer而言非常重要，它不仅保证了consumer端的消费进度，同时也直接决定了consumer端的消费语义保证。  
&emsp; **<font color = "red">位移提交有两种方式：</font><font color = "lime">自动提交、手动提交。</font>**  
* 自动提交  
    &emsp; 使用方法：默认不用配置，或者显示配置enable.auto.commit=true，用auto.commit.interval.ms参数控制自动提交的间隔。  
    &emsp; 使用场景：对消息交付语义无需求，容忍一定的消息丢失。自动提交的问题是用户不能细粒度地处理位移的提交，特别是在有较强的精确一次的处理语义。  
* 手动提交  
    &emsp; 使用方法：设置enable.auto.commmit=false；手动调用KafkaConsumer.commitSync或KafkaConsumer.commitAsync提交位移  
    &emsp; 使用场景：消息处理逻辑重，不允许消息丢失，至少要求“最少一次”处理语义  
    &emsp; commitSync：同步手动提交，用户程序会等待位移提交结束才执行下一条语句命令。  
    &emsp; commitAsync：异步手动提交，这里的异步不是开启一个线程提交，而是指不会阻塞，consumer在后续poll调用时轮询该位移提交的结果。  
    &emsp; commitSync和commitAsync都有带参数的重载方法，目的是实现更加细粒度化的位移提交策略，指定一个Map显示地告诉kafka为哪些分区提交位移，consumer.commitSync(Collections.singletonMap(partition,new OffsetAndMetadata(lastOffset + 1)))。  

### 1.4.2. 位移主题(__consumer_offsets) ，(位移提交地址) 
<!-- 
https://www.kancloud.cn/nicefo71/kafka/1471591
-->

    协调者：consumer会在kafka集群的所有broker中选择一个broker作为consumer group的coordinator（协调者），用于实现组成员管理、消费分配方案制定以及提交位移等。  

&emsp; 当consumer运行了一段时间之后，它必须要提交自己的位移值。consumer提交位移的主要机制是通过向所属的coordinator发送位移提交请求来实现的。**consumer把位移提交到kafka的一个内部topic（__consumer_offsets）上。**通常不能直接操作该topic，特别注意不要擅自删除或搬移该topic的日志文件。每个位移提交请求都会往内部topic（__consumer_offsets）对应分区上追加写入一条消息。  
&emsp; 消息的key是group.id、topic和分区的元组，value就是位移值。  

### 1.4.3. 消费组消费进度查看的命令
&emsp; 通过消费组消费进度查看的命令，可以知道当前消费组消费了哪些 topic，每个分区的消费进度如何，以及每个分区是由哪个消费者进行消费的。从下面的信息也可以得出，一个分区只会被一个消费者消费。  

```text
> bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group test
 
 
TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                     HOST            CLIENT-ID
kafka_test      2          7290            7292            2               consumer-3-7edccfc6-ab05-4871-b1ea-ad06ed5038c1 /192.168.109.1  consumer-3
kafka_test      1          7258            7258            0               consumer-2-50840b10-5550-4b9e-a357-f3667520af08 /192.168.109.1  consumer-2
kafka_test      3          7437            7437            0               consumer-4-0a30fa36-5a3c-40b6-84b8-c3f67553b27c /192.168.109.1  consumer-4
kafka_test      0          7399            7399            0               consumer-1-696cc434-d9f0-4f95-8ac5-2202dee19d2d /192.168.109.1  consumer-1
```

--------------
## 1.5. 订阅topic
&emsp; **Kafka通过消费者组，可以实现基于队列和基于发布/订阅的两种消息引擎：**  

* 如果所有的消费者都隶属于同一个消费组，那么所有的消息都会被均衡地投递给每一 个消费者，即每条消息只会被一个消费者处理，这就相当于点对点模式的应用。  
* 如果所有的消费者都隶属于不同的消费组，那么所有的消息都会被广播给所有的消费者，即每条消息会被所有的消费者处理，这就相当于发布/订阅模式的应用。


&emsp; **两种订阅topic的方式：直接订阅topic列表，基于正则表达订阅topic。**  

### 1.5.1. 订阅topic列表
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

### 1.5.2. 基于正则表达订阅topic
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

## 1.6. 消息轮询
&emsp; **consumer 采用 pull（拉）模式从 broker 中读取数据。**  
&emsp; push（推）模式很难适应消费速率不同的消费者，因为消息发送速率是由 broker 决定的。 它的目标是尽可能以最快速度传递消息，但是这样很容易造成 consumer 来不及处理消息，典型的表现就是拒绝服务以及网络拥塞。而 pull 模式则可以根据 consumer 的消费能力以适当的速率消费消息。  
&emsp; 对于 Kafka 而言，pull 模式更合适，它可简化 broker 的设计，consumer 可自主控制消费消息的速率，同时consumer可以自己控制消费方式——即可批量消费也可逐条消费，同时还能选择不同的提交方式从而实现不同的传输语义。  
&emsp; pull 模式不足之处是，如果 kafka 没有数据，消费者可能会陷入循环中，一直等待数据到达。为了避免这种情况，在拉取请求中有参数，允许消费者请求在等待数据到达 的“长轮询”中进行阻塞（并且可选地等待到给定的字节数，以确保大的传输大小）。  

## 1.7. 消费语义  
&emsp; 消费者从服务端的 Partition 上拉取到消息，消费消息有三种情况，分别如下：  

* 至少一次。即一条消息至少被消费一次，消息不可能丢失，但是可能会被重复消费。  
* 至多一次。即一条消息最多可以被消费一次，消息不可能被重复消费，但是消息有可能丢失。  
* 正好一次。即一条消息正好被消费一次，消息不可能丢失也不可能被重复消费。  

1. 至少一次  
&emsp; 消费者读取消息，先处理消息，在保存消费进度。消费者拉取到消息，先消费消息，然后在保存偏移量，当消费者消费消息后还没来得及保存偏移量，则会造成消息被重复消费。如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-87.png)  
<center>先消费后保存消费进度</center>
2. 至多一次  
&emsp; 消费者读取消息，先保存消费进度，在处理消息。消费者拉取到消息，先保存了偏移量，当保存了偏移量后还没消费完消息，消费者挂了，则会造成未消费的消息丢失。如下图所示：
&emsp; 先保存消费进度后消费消息  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-88.png)  
<center>先保存消费进度后消费消息</center>
3. 正好一次  
&emsp; 正好消费一次的办法可以通过将消费者的消费进度和消息处理结果保存在一起。只要能保证两个操作是一个原子操作，就能达到正好消费一次的目的。通常可以将两个操作保存在一起，比如 HDFS 中。正好消费一次流程如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-89.png)  
<center>正好消费一次</center>

## 1.8. 多线程消费实例  
<!-- 
多线程 Consumer Instance
https://www.kancloud.cn/nicefo71/kafka/1471595
线程封闭，即为每个线程实例化一个 KafkaConsumer 对象
一个线程对应一个 KafkaConsumer 实例，我们可以称之为消费线程。一个消费线程可以消费一个或多个分区中的消息，所有的消费线程都隶属于同一个消费组。

消费者程序使用单或多线程获取消息，同时创建多个消费线程执行消息处理逻辑。
获取消息的线程可以是一个，也可以是多个，每个线程维护专属的 KafkaConsumer 实例，处理消息则交由特定的线程池来做，从而实现消息获取与消息处理的真正解耦。具体架构如下图所示：

维护一个或多个KafkaConsumer，同时维护多个事件处理线程(worker thread) 
-->
<!-- 
~~
Kafka Consumer多线程实例
https://blog.csdn.net/matrix_google/article/details/80035222?utm_source=blogxgwz8
-->
&emsp; KafkaConsumer类不是线程安全的，所有的网络IO处理都是发生在用户主线程中。不能在多个线程中共享同一个 KafkaConsumer 实例，可以使用 KafkaConsumer.wakeup() 在其他线程中唤醒 Consumer。  
&emsp; 实现多线程时通常由两种实现方法：  
1. 消费者程序启动多个线程，每个线程维护专属的KafkaConsumer Instance，负责完整的消息获取、消息处理流程。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-59.png)  
2. 消费者程序使用单或多线程获取消息，同时创建多个消费线程执行消息处理逻辑
    * 处理消息交由特定的线程池来做
    * 将消息获取与处理解耦

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-60.png)  

&emsp; 两种方法的优缺点：   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-93.png)  

|  | 优点| 缺点| 
|--- |--- |--- | 
|方法1(每个线程维护一个KafkaConsumer)|方便实现</br>速度较快，因为不需要任何线程间交互</br>易于维护分区内的消息顺序	|更多的TCP连接开销(每个线程都要维护若干个TCP连接)</br>consumer数受限于topic分区数，扩展性差</br>频繁请求导致吞吐量下降</br>线程自己处理消费到的消息可能会导致超时，从而造成rebalance|
|方法2 (单个(或多个)consumer，多个worker线程)|可独立扩展consumer数和worker数，伸缩性好| 实现麻烦</br>通常难于维护分区内的消息顺序</br>处理链路变长，导致难以保证提交位移的语义正确性 |

&emsp; **示例代码：**  
&emsp; **方案一：**  

```java
public class KafkaConsumerRunner implements Runnable {
     private final AtomicBoolean closed = new AtomicBoolean(false);
     private final KafkaConsumer consumer;


     public void run() {
         try {
             consumer.subscribe(Arrays.asList("topic"));
             while (!closed.get()) {
      ConsumerRecords records = 
        consumer.poll(Duration.ofMillis(10000));
                 //  执行消息处理逻辑
             }
         } catch (WakeupException e) {
             // Ignore exception if closing
             if (!closed.get()) throw e;
         } finally {
             consumer.close();
         }
     }


     // Shutdown hook which can be called from a separate thread
     public void shutdown() {
         closed.set(true);
         consumer.wakeup();
     }
}
```

&emsp; **方案二：**
```java
private final KafkaConsumer<String, String> consumer;
private ExecutorService executors;
//...


private int workerNum = ...;
executors = new ThreadPoolExecutor(
  workerNum, workerNum, 0L, TimeUnit.MILLISECONDS,
  new ArrayBlockingQueue<>(1000), 
  new ThreadPoolExecutor.CallerRunsPolicy());


//...
while (true)  {
  ConsumerRecords<String, String> records = 
    consumer.poll(Duration.ofSeconds(1));
  for (final ConsumerRecord record : records) {
    executors.submit(new Worker(record));
  }
}
//..
```