<!-- TOC -->

- [1. Kafka副本机制](#1-kafka副本机制)
    - [1.1. Kafka副本简介](#11-kafka副本简介)
    - [1.2. 客户端数据请求](#12-客户端数据请求)
    - [1.3. 服务端Leader的选举(ISR副本)](#13-服务端leader的选举isr副本)
        - [1.3.1. ISR副本](#131-isr副本)
        - [1.3.2. 服务端Unclear Leader选举](#132-服务端unclear-leader选举)
    - [1.4. ~~服务端副本消息的同步(LEO和HW)~~](#14-服务端副本消息的同步leo和hw)
        - [1.4.1. LEO和HW概念](#141-leo和hw概念)
        - [1.4.2. ★★★~~副本上LEO和HW的更新~~](#142-★★★副本上leo和hw的更新)
            - [1.4.2.1. Follower副本上LEO和HW的更新](#1421-follower副本上leo和hw的更新)
            - [1.4.2.2. Leader副本上LEO和HW的更新](#1422-leader副本上leo和hw的更新)
        - [1.4.3. 数据丢失和数据不一致场景](#143-数据丢失和数据不一致场景)
        - [1.4.4. Leader Epoch](#144-leader-epoch)
            - [1.4.4.1. 简介](#1441-简介)
            - [1.4.4.2. 工作流程](#1442-工作流程)
            - [1.4.4.3. 如何解决数据丢失和数据不一致](#1443-如何解决数据丢失和数据不一致)
                - [1.4.4.3.1. 数据丢失](#14431-数据丢失)
                - [1.4.4.3.2. 数据不一致](#14432-数据不一致)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "blue">Kafka副本中只有Leader可以和客户端交互，进行读写，其他副本是只能同步，不能分担读写压力。</font>**  
2. 客户端数据请求：  
&emsp; 集群中的每个broker都会缓存所有主题的分区副本信息，客户端会定期发送元数据请求，然后将获取的集群元数据信息进行缓存。  
3. 服务端Leader的选举：从ISR（保持同步的副本）集合副本中选取。  
4. 服务端副本消息的同步：  
&emsp; LEO，低水位，记录了日志的下一条消息偏移量，即当前最新消息的偏移量加一；HW，高水位，界定了消费者可见的消息，是ISR队列中最小的LEO。  
    1. Follower副本更新LEO和HW：  
    &emsp; 更新LEO和HW的时机： **<font color = "clime">Follower向Leader拉取了消息之后。(⚠注意：Follower副本只和Leader副本交互。)</font>**  
    &emsp; **<font color = "red">会用获取的偏移量加1来更新LEO，并且用Leader的HW值和当前LEO的最小值来更新HW。</font>**  
    2. Leader副本上LEO和HW的更新：  
        * 正常情况下Leader副本的更新时机有两个：一、收到生产者的消息；二、被Follower拉取消息。(⚠注意：Leader副本即和Leader副本交互，也和生产者交互。)  
            * 当收到生产者消息时，会用当前偏移量加1来更新LEO，然后取LEO和远程ISR副本中LEO的最小值更新HW。 
            * 当Follower拉取消息时，会更新Leader上存储的Follower副本LEO，然后判断是否需要更新HW，更新的方式和上述相同。 
        * 除了这两种正常情况，当发生故障时，例如Leader宕机，Follower被选为新的Leader，会尝试更新HW。还有副本被踢出ISR时，也会尝试更新HW。 
5. 在服务端Leader切换时，会存在数据丢失和数据不一致的问题。  
    1. 主从切换，数据不一致的情况如下：
    &emsp; A作为Leader，A已写入m0、m1两条消息，且HW为2，而B作为Follower，只有m0消息，且HW为1。若A、B同时宕机，且B重启时，A还未恢复，则B被选为Leader。 
    &emsp; 在B重启作为Leader之后，收到消息m2。A宕机重启后向成为Leader的B发送Fetch请求，发现自己的HW和B的HW一致，都是2，因此不会进行消息截断，而这也造成了数据不一致。 
    2. 引入Leader Epoch机制：
    &emsp; **<font color = "blue">为了解决HW可能造成的数据丢失和数据不一致问题，Kafka引入了Leader Epoch机制。</font>** 在每个副本日志目录下都有一个leader-epoch-checkpoint文件，用于保存Leader Epoch信息。  
    &emsp; Leader Epoch，分为两部分，前者Epoch，表示Leader版本号，是一个单调递增的正整数，每当Leader变更时，都会加1；后者StartOffset，为每一代Leader写入的第一条消息的位移。 


# 1. Kafka副本机制  
<!--~~ 
Kafka中副本机制的设计和原理 
https://mp.weixin.qq.com/s/yIPIABpAzaHJvGoJ6pv0kg
-->
## 1.1. Kafka副本简介
&emsp; 在分布式数据系统中，通常使用分区来提高系统的处理能力，通过副本来保证数据的高可用性。  
&emsp; 副本机制的使用在计算机的世界里是很常见的，比如MySQL、ZooKeeper、CDN等都有使用副本机制。使用副本机制所能带来的好处有以下几种：  

* 提供数据冗余，提高可用性；
* 提供扩展性，增加读操作吞吐量；
* 改善数据局部，降低系统延时。

&emsp; 但并不是每个好处都能获得，这还是和具体的设计有关，比如Kafka只具有第一个好处，即提高可用性。这是因为 **<font color = "blue">Kafka副本中只有Leader可以和客户端交互，进行读写，其他副本是只能同步，不能分担读写压力。</font>**  

&emsp; 副本的定义是在分区(Partition)层下定义的，每个分区有多个副本。 **副本可分布于多台机器上。**  
&emsp; **Kafka中副本分为领导者副本(Leader Replica) & 追随者副本(Follower Replica)。每个 Partition创建时都要选举一个副本，称为 Leader Replica，其余副本为 Follower Replica。<font color = "clime">只有Leader副本会读写数据。其他则作为Follower副本，负责同步Leader的数据，当Leader宕机时，从Follower选举出新的Leader，从而解决分区单点问题。</font>**   

&emsp; 这种副本机制设计的优势：

* 方便实现 Read-your-writes
    * Read-your-writes：当使用Producer API写消息后，马上使用Consumer API去消费。
    * 如果允许Follower对外提供服务，由于异步，因此不能实现Read-your-writes。
* 方便实现单调读(Monotonic Reads)
    * 单调读：对于一个 Consumer，多次消费时，不会看到某条消息一会存在一会不存在。
    * 问题案例
        * 如果允许Follower提供服务，假设有两个Follower F1、F2。
        * 如果F1拉取了最新消息而F2还没有。
        * 对于Consumer第一次消费时从F1看到的消息，第二次从F2则可能看不到。
        * 这种场景是非单调读。
        * 所有读请求通过Leader则可以实现单调读。

<!-- 
多分区意味着并发处理的能力，这多个副本中，只有一个是 leader，而其他的都是 follower 副本。仅有 leader 副本可以对外提供服务。多个 follower 副本通常存放在和 leader 副本不同的 broker 中。通过这样的机制实现了高可用，当某台机器挂掉后，其他 follower 副本也能迅速”转正“，开始对外提供服务。
-->
<!--
https://mp.weixin.qq.com/s/yIPIABpAzaHJvGoJ6pv0kg

&emsp; 为什么 follower 副本不提供读服务？
这个问题本质上是对性能和一致性的取舍。试想一下，如果 follower 副本也对外提供服务那会怎么样呢？首先，性能是肯定会有所提升的。但同时，会出现一系列问题。类似数据库事务中的幻读，脏读。比如你现在写入一条数据到 kafka 主题 a，消费者 b 从主题 a 消费数据，却发现消费不到，因为消费者 b 去读取的那个分区副本中，最新消息还没写入。而这个时候，另一个消费者 c 却可以消费到最新那条数据，因为它消费了 leader 副本。Kafka 通过 WH 和 Offset 的管理来决定 Consumer 可以消费哪些数据，已经当前写入的数据。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-30.png)  
-->

## 1.2. 客户端数据请求  
&emsp; 在所有副本中，只有领导副本才能进行消息的读写处理。 **<font color = "red">由于不同分区的领导副本可能在不同的broker上，如果某个broker收到了一个分区请求，但是该分区的领导副本并不在该broker上，那么它就会向客户端返回一个Not a Leader for Partition的错误响应。为了解决这个问题，Kafka提供了元数据请求机制。</font>**  
&emsp; **<font color = "red">首先集群中的每个broker都会缓存所有主题的分区副本信息，客户端会定期发送元数据请求，然后将获取的集群元数据信息进行缓存。</font>** 定时刷新元数据的时间间隔可以通过为客户端配置metadata.max.age.ms来进行指定。有了元数据信息后，客户端就知道了领导副本所在的broker，之后直接将读写请求发送给对应的broker即可。  
&emsp; 如果在定时请求的时间间隔内发生的分区副本的选举，则意味着原来缓存的信息可能已经过时了，此时还有可能会收到Not a Leader  for Partition的错误响应，这种情况下客户端会再次求发出元数据请求，然后刷新本地缓存，之后再去正确的broker上执行对应的操作，过程如下图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-94.png)  

## 1.3. 服务端Leader的选举(ISR副本)
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-117.png)  

### 1.3.1. ISR副本
<!-- 
只有 Leader 可以对外提供读服务，那如何选举 Leader

kafka 会将与 leader 副本保持同步的副本放到 ISR 副本集合中。当然，leader 副本是一直存在于 ISR 副本集合中的，在某些特殊情况下，ISR 副本中甚至只有 leader 一个副本。当 leader 挂掉时，kakfa 通过 zookeeper 感知到这一情况，在 ISR 副本中选取新的副本成为 leader，对外提供服务。但这样还有一个问题，前面提到过，有可能 ISR 副本集合中，只有 leader，当 leader 副本挂掉后，ISR 集合就为空，这时候怎么办呢？这时候如果设置 unclean.leader.election.enable 参数为 true，那么 kafka 会在非同步，也就是不在 ISR 副本集合中的副本中，选取出副本成为 leader。  


In-sync Replicas(ISR)

    对于 Follower 存在与 Leader 不同步的风险
    Kafka 要明确 Follower 在什么条件下算与 Leader 同步，因此引入 ISR 副本集合
    Q：什么副本算作 ISR？
    A：
        Leader 天然在 ISR 中，某些情况 ISR 中只有 Leader
        Kafka 判断 Follower 和 Leader 同步的标准基于 Broker 端参数 replica.lag.time.max.ms，i.e. Follower Replica 能够落后 Leader Replica 的最长时间间隔，默认值是 10s
        如果一个 Follower 落后 Leader 不超过 10s，则认为该 Follower 是同步的，即该 Follower 被认为是 ISR
    ISR 是动态调整的
-->
&emsp; 当领导者副本宕机了，Kafka依托于ZooKeeper提供的监控功能能够实时感知到，并立即开启新一轮的领导者选举，从追随者副本中选一个作为新的领导者。老Leader副本重启回来后，只能作为追随者副本加入到集群中。  
&emsp; **当Leader宕机时，要从Follower中选举出新的Leader，但并不是所有的Follower都有资格参与选举。因为有的Follower的同步情况滞后，如果让它成为Leader，将会导致消息丢失。**   

0. 什么是滞后副本？  
&emsp; 默认情况下(注意只是默认)，只有被认定为是实时同步的Follower副本，才可能被选举成Leader。一个副本与leader失去实时同步的原因有很多，比如：  

    * 慢副本(Slow replica)：follower replica在一段时间内一直无法赶上 leader 的写进度。造成这种情况的最常见原因之一是follower replica 上的 I/O瓶颈，导致它持久化日志的时间比它从leader消费消息的时间要长；  
    * 卡住副本(Stuck replica)：follower replica在很长一段时间内停止从leader获取消息。这可能是发生GC停顿，或者副本出现故障；  
    * 刚启动副本(Bootstrapping replica)：当用户给某个主题增加副本因子时，新的 follower replicas是不同步的，直到它跟上leader的日志。  
1. **引入ISR**  
&emsp; **<font color = "clime">为了避免将旧副本选举为Leader，Kafka引入了ISR（In-Sync Replica，保持同步的副本）的概念，这是一个集合，里面存放的是和Leader保持同步的副本并含有Leader。这是一个动态调整的集合，当副本由同步变为滞后时会从集合中剔除，而当副本由滞后变为同步时又会加入到集合中。</font>**  

<!--
https://juejin.cn/post/6844903950009794567#heading-3
每个分区都有一个 ISR(in-sync Replica) 列表，用于维护所有同步的、可用的副本。首领副本必然是同步副本，而对于跟随者副本来说，它需要满足以下条件才能被认为是同步副本：

与 Zookeeper 之间有一个活跃的会话，即必须定时向 Zookeeper 发送心跳；
在规定的时间内从首领副本那里低延迟地获取过消息。

如果副本不满足上面条件的话，就会被从 ISR 列表中移除，直到满足条件才会被再次加入。
副本的存在就会出现副本同步问题

Kafka 在所有分配的副本 (AR) 中维护一个可用的副本列表 (ISR)，Producer 向 Broker 发送消息时会根据ack配置来确定需要等待几个副本已经同步了消息才相应成功，Broker 内部会ReplicaManager服务来管理 flower 与 leader 之间的数据同步。
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-31.png)  
-->

2. **副本是否滞后的设置**  
&emsp; 那么如何判断一个副本是同步还是滞后呢？Kafka在0.9版本之前，是根据replica.lag.max.messages参数来判断，其含义是同步副本所能落后的最大消息数，当Follower上的最大偏移量落后Leader大于replica.lag.max.messages时，就认为该副本是不同步的了，会从ISR中移除。  
&emsp; 如果ISR的值设置得过小，会导致Follower经常被踢出ISR，而如果设置过大，则当Leader宕机时，会造成较多消息的丢失。  
&emsp; 在实际使用时，很难给出一个合理值，这是因为当生产者为了提高吞吐量而调大batch.size时，会发送更多的消息到Leader上，这时候如果不增大replica.lag.max.messages，则会有Follower频繁被踢出ISR的现象。而当Follower发生Fetch请求同步后，又被加入到ISR中，ISR将频繁变动。  
&emsp; 鉴于该参数难以设定，**Kafka在0.9版本引入了一个新的参数replica.lag.time.max.ms，默认10s，含义是当Follower超过10s没发送Fetch请求同步Leader时，就会认为不同步而被踢出ISR。**从时间维度来考量，能够很好地避免生产者发送大量消息到Leader副本导致分区ISR频繁收缩和扩张的问题。  

### 1.3.2. 服务端Unclear Leader选举  
<!-- 
    由于 ISR 是动态调整的，可能出现 ISR 为空，即 Leader 宕机，Follower 都不同步
    ISR 为空时，如何选举新 Leader？
        非同步副本：Kafka 把所有不在 ISR 中的存活副本称为非同步副本
    Broker 参数 unclean.leader.election.enable 控制是否允许 Unclean Leader Election
    即如果参数为 true，ISR 为空是，会从非同步副本中选举 Leader

unclean领导者选举。再回去看看刚刚我们说Leader挂了怎么办，有句话重点标粗，"默认情况下(注意只是默认)，只有被认定为是实时同步的Follower副本，才可能被选举成Leader"。

这是由Broker 端参数 unclean.leader.election.enable 控制的。默认为false，即只有被认定为是实时同步的Follower副本(在ISR中的)，才可能被选举成Leader。如果你设置为true,则所有副本都可以被选举。

开启 Unclean 领导者选举可能会造成数据丢失，但好处是，它使得分区 Leader 副本一直存在，不至于停止对外提供服务，因此提升了高可用性。反之，禁止 Unclean 领导者选举的好处在于维护了数据的一致性，避免了消息丢失，但牺牲了高可用性。

如果你听说过 CAP 理论的话，你一定知道，一个分布式系统通常只能同时满足一致性(Consistency)、可用性(Availability)、分区容错性(Partition tolerance)中的两个。显然，在这个问题上，Kafka 赋予你选择 C 或 A 的权利。

你可以根据你的实际业务场景决定是否开启 Unclean 领导者选举。不过，我强烈建议你不要开启它，毕竟我们还可以通过其他的方式来提升高可用性。如果为了这点儿高可用性的改善，牺牲了数据一致性，那就非常不值当了。

对于副本机制，在 broker 级别有一个可选的配置参数 unclean.leader.election.enable，默认值为 fasle，代表禁止不完全的首领选举。这是针对当首领副本挂掉且 ISR 中没有其他可用副本时，是否允许某个不完全同步的副本成为首领副本，这可能会导致数据丢失或者数据不一致，在某些对数据一致性要求较高的场景 (如金融领域)，这可能无法容忍的，所以其默认值为 false，如果你能够允许部分数据不一致的话，可以配置为 true。

-->
&emsp; ISR是一个动态调整的集合，而非静态不变的。当ISR集合为空时，即没有同步副本(Leader也挂了)，无法选出下一个Leader，Kafka集群将会失效。 **<font color = "red">而为了提高可用性，Kafka提供了unclean.leader.election.enable参数，当设置为true且ISR集合为空时，会进行Unclear Leader选举，允许在非同步副本中选出新的Leader，从而提高Kafka集群的可用性，但这样会造成消息丢失。在允许消息丢失的场景中，是可以开启此参数来提高可用性的。</font>** 而其他情况，则不建议开启，而是通过其他手段来提高可用性。  

## 1.4. ~~服务端副本消息的同步(LEO和HW)~~ 
<!-- 
kafka数据一致性，通过HW来保证  
&emsp; 由于并不能保证 Kafka 集群中每时每刻 follower 的长度都和 leader 一致(即数据同步是有时延的)，那么当leader 挂掉选举某个 follower 为新的 leader 的时候(原先挂掉的 leader 恢复了成为了 follower)，可能会出现leader 的数据比 follower 还少的情况。为了解决这种数据量不一致带来的混乱情况，Kafka 提出了以下概念：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-27.png)  

* LEO(Log End Offset)：指的是每个副本最后一个offset；  
* HW(High Wather)：指的是消费者能见到的最大的 offset，ISR 队列中最小的 LEO。  

&emsp; 消费者和 leader 通信时，只能消费 HW 之前的数据，HW 之后的数据对消费者不可见。  

&emsp; 针对这个规则：  

* 当follower发生故障时：follower 发生故障后会被临时踢出 ISR，待该 follower 恢复后，follower 会读取本地磁盘记录的上次的 HW，并将 log 文件高于 HW 的部分截取掉，从 HW 开始向 leader 进行同步。等该 follower 的 LEO 大于等于该 Partition 的 HW，即 follower 追上 leader 之后，就可以重新加入 ISR 了。
* 当leader发生故障时：leader 发生故障之后，会从 ISR 中选出一个新的 leader，之后，为保证多个副本之间的数据一致性，其余的 follower 会先将各自的 log 文件高于 HW 的部分截掉，然后从新的 leader 同步数据。

&emsp; 所以数据一致性并不能保证数据不丢失或者不重复，这是由 ack 控制的。HW 规则只能保证副本之间的数据一致性！

-->
### 1.4.1. LEO和HW概念
<!-- 
&emsp; ~~下面介绍下HW的概念，其可翻译为高水位或高水印，这一概念通常用于在流式处理领域(如Flink、Spark等)，流式系统将保证在HW为t时刻时，创建时间小于等于t时刻的所有事件都已经到达或可被观测到。而在Kafka中，HW的概念和时间无关，而是和偏移量有关，主要目的是为了保证一致性。~~  
&emsp; ~~试想如果一个消息到达了Leader，而Follower副本还未来得及同步，但该消息能已被消费者消费了，这时候Leader宕机，Follower副本中选出新的Leader，消息将丢失，出现不一致的现象。所以Kafka引入HW的概念，当消息被同步副本同步完成时，才让消息可被消费。~~  
-->
&emsp; 副本的本质其实是一个消息日志，为了让副本正常同步，需要通过一些变量记录副本的状态，如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-75.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-112.png)  
&emsp; **其中LEO(Last End Offset，低水位)记录了日志的下一条消息偏移量，即当前最新消息的偏移量加一。<font color = "red">**  
&emsp; **而HW（High Watermark，高水位）界定了消费者可见的消息，消费者可以消费小于HW的消息，而大于等于HW的消息将无法消费。</font>** 是ISR队列中最小的LEO。  

### 1.4.2. ★★★~~副本上LEO和HW的更新~~
&emsp; 上述即是LEO和HW的基本概念，下面看下具体是如何工作的。  
&emsp; 在每个副本中都存有LEO和HW，而 **<font color = "clime">Leader副本中除了存有自身的LEO和HW，还存储了其他Follower副本的LEO和HW值，</font>** 为了区分把Leader上存储的Follower副本的LEO和HW值叫做远程副本的LEO和HW值，如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-76.png)  
&emsp; 之所以这么设计，是为了HW的更新，Leader需保证HW是ISR副本集合中LEO的最小值。关于具体的更新，分为Follower副本和Leader副本来看。  

#### 1.4.2.1. Follower副本上LEO和HW的更新
1. Follower副本更新LEO和HW的时机： **<font color = "clime">Follower向Leader拉取了消息之后。(⚠注意：Follower副本只和Leader副本交互。)</font>**  
2. **<font color = "red">会用获取的偏移量加1来更新LEO，并且用Leader的HW值和当前LEO的最小值来更新HW。</font>**  

        CurrentOffset + 1 -> LEO
        min(LEO, LeaderHW) -> HW

&emsp; LEO的更新，很好理解。那为什么HW要取LEO和LeaderHW的最小值，为什么不直接取LeaderHW，LeaderHW不是一定大于LEO吗？LeaderHW是根据同步副本来决定，所以LeaderHW一定小于所有同步副本的LEO，而并不一定小于非同步副本的LEO，所以如果一个非同步副本在拉取消息，那LEO是会小于LeaderHW的，则应用当前LEO值来更新HW。  

#### 1.4.2.2. Leader副本上LEO和HW的更新
1. **<font color = "clime">正常情况下Leader副本的更新时机有两个：一、收到生产者的消息；二、被Follower拉取消息。(⚠注意：Leader副本即和Leader副本交互，也和生产者交互。)</font>**  
    1. 当收到生产者消息时，会用当前偏移量加1来更新LEO，然后取LEO和远程ISR副本中LEO的最小值更新HW。  

        CurrentOffset + 1 -> LEO
        min(LEO, RemoteIsrLEO) -> HW

    2. 当Follower拉取消息时，follower同步过来的数据会带上LEO的值，会更新Leader上存储的Follower副本LEO，然后判断是否需要更新HW，更新的方式和上述相同。  

        FollowerLEO -> RemoteLEO
        min(LEO, RemoteIsrLEO) -> HW

2. 除了这两种正常情况，当发生故障时，例如Leader宕机，Follower被选为新的Leader，会尝试更新HW。还有副本被踢出ISR时，也会尝试更新HW。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-113.png)  

### 1.4.3. 数据丢失和数据不一致场景
<!-- 
https://my.oschina.net/u/3379856/blog/4388538
-->
&emsp; 假设分区中有两个副本，min.insync.replica=1。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-77.png)  
&emsp; 从上述过程中，可以看到remoteLEO、LeaderHW和FollowerHW的更新发生于Follower更新LEO后的第二轮Fetch请求，而这也意味着，更新需要额外一次Fetch请求。 **而这也将导致在Leader切换时，会存在数据丢失和数据不一致的问题。**  
&emsp; 下面是数据丢失的示例：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-78.png)  
&emsp; 当B作为Follower已经Fetch了最新的消息，但是在发送第二轮Fetch时，未来得及处理响应，宕机了。当重启时，会根据HW更新LEO，将发生日志截断，消息m1被丢弃。  
&emsp; 这时再发送Fetch请求给A，A宕机了，则B未能同步到消息m1，同时B被选为Leader，而当A重启时，作为Follower同步B的消息时，会根据A的HW值更新HW和LEO，因此由2变成了1，也将发生日志截断，而已发送成功的消息m1将永久丢失。  
---
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-114.png)  

---

&emsp; 数据不一致的情况如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-79.png)  
&emsp; A作为Leader，A已写入m0、m1两条消息，且HW为2，而B作为Follower，只有m0消息，且HW为1。若A、B同时宕机，且B重启时，A还未恢复，则B被选为Leader。  

    集群处于上述这种状态有两种情况可能导致，一、宕机前，B不在ISR中，因此A未待B同步，即更新了HW，且unclear leader为true，允许B成为Leader；二、宕机前，B同步了消息m1，且发送了第二轮Fetch请求，Leader更新HW，但B未将消息m1落地到磁盘，宕机了，当再重启时，消息m1丢失，只剩m0。

&emsp; 在B重启作为Leader之后，收到消息m2。A宕机重启后向成为Leader的B发送Fetch请求，发现自己的HW和B的HW一致，都是2，因此不会进行消息截断，而这也造成了数据不一致。  

---

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-115.png)  

### 1.4.4. Leader Epoch 
#### 1.4.4.1. 简介
&emsp; **<font color = "blue">为了解决HW可能造成的数据丢失和数据不一致问题，Kafka引入了Leader Epoch机制。</font>** 在每个副本日志目录下都有一个leader-epoch-checkpoint文件，用于保存Leader Epoch信息，其内容示例如下：  

    0 0
    1 300
    2 500

&emsp; 上面每一行为一个Leader Epoch，分为两部分，前者Epoch，表示Leader版本号，是一个单调递增的正整数，每当Leader变更时，都会加1；后者StartOffset，为每一代Leader写入的第一条消息的位移。  

&emsp; 例如第0代Leader写的第一条消息位移为0，而第1代Leader写的第一条消息位移为300，也意味着第0代Leader在写了0-299号消息后挂了，重新选出了新的Leader。  

#### 1.4.4.2. 工作流程
&emsp; 下面看下Leader Epoch如何工作：  
1. 当副本成为Leader时：  
&emsp; 当收到生产者发来的第一条消息时，会将新的epoch和当前LEO添加到leader-epoch-checkpoint文件中。  
2. 当副本成为Follower时：
    1. 向Leader发送LeaderEpochRequest请求，请求内容中含有Follower当前本地的最新Epoch；
    2. Leader将返回给Follower的响应中含有一个LastOffset，其取值规则为：
        * 若FollowerLastEpoch = LeaderLastEpoch，则取Leader LEO；
        * 否则，取大于FollowerLastEpoch的第一个Leader Epoch中的StartOffset。
    3. Follower在拿到LastOffset后，若LastOffset < LEO，将截断日志；
    4. Follower开始正常工作，发送Fetch请求；

#### 1.4.4.3. 如何解决数据丢失和数据不一致
##### 1.4.4.3.1. 数据丢失
&emsp; 再回顾看下数据丢失和数据不一致的场景，在应用了LeaderEpoch后发生什么改变：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-80.png)  
&emsp; 当B作为Follower已经Fetch了最新的消息，但是发送第二轮Fetch时，未来得及处理响应，宕机了。 **<font color = "red">当重启时，会向A发送LeaderEpochRequest请求。</font>** 如果A没宕机，由于 FollowerLastEpoch = LeaderLastEpoch，所以将LeaderLEO，即2作为LastOffset给A，又因为LastOffset=LEO，所以不会截断日志。这种情况比较简单。  

&emsp; 而图中所画的情况是A宕机的情况，没返回LeaderEpochRequest的响应的情况。这时候B会被选作Leader，将当前LEO和新的Epoch写进leader-epoch-checkpoint文件中。  
&emsp; 当A作为Follower重启后，发送LeaderEpochRequest请求，包含最新的epoch值0，当B收到请求后，由于FollowerLastEpoch < LeaderLastEpoch，所以会取大于FollowerLastEpoch的第一个Leader Epoch中的StartOffset，即2。当A收到响应时，由于LEO = LastOffset，所以不会发生日志截断，也就不会丢失数据。  

##### 1.4.4.3.2. 数据不一致
&emsp; 下面是数据不一致情况：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-81.png)  
&emsp; A作为Leader，A已写入m0、m1两条消息，且HW为2，而B作为Follower，只有消息m0，且HW为1，A、B同时宕机。B重启，被选为Leader，将写入新的LeaderEpoch(1, 1)。B开始工作，收到消息m2时。这时A重启，将作为Follower将发送LeaderEpochRequert(FollowerLastEpoch=0)，B返回大于FollowerLastEpoch的第一个LeaderEpoch的StartOffset，即1，小于当前LEO值，所以将发生日志截断，并发送Fetch请求，同步消息m2，避免了消息不一致问题。  
&emsp; 但是此时m2消息会丢失，这种情况发生的根本原因在于min.insync.replicas的值设置为1，即没有任何其他副本同步的情况下，就认为m2消息为已提交状态。LeaderEpoch不能解决min.insync.replicas为1带来的数据丢失问题，但是可以解决其所带来的数据不一致问题。而之前所说能解决的数据丢失问题，是指消息已经成功同步到Follower上，但因HW未及时更新引起的数据丢失问题。  