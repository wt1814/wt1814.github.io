<!-- TOC -->

- [1. kafka服务端](#1-kafka服务端)
    - [1.1. Kafka控制器](#11-kafka控制器)
        - [1.1.1. 控制器的选举](#111-控制器的选举)
        - [1.1.2. 控制器的异常选举](#112-控制器的异常选举)
        - [1.1.3. 控制器的功能](#113-控制器的功能)
            - [1.1.3.1. kafka在ZK的目录分布](#1131-kafka在zk的目录分布)
            - [1.1.3.2. 控制器功能](#1132-控制器功能)
        - [1.1.4. 控制器设计原理](#114-控制器设计原理)
        - [1.1.5. 优雅关闭](#115-优雅关闭)
    - [1.2. broker请求处理](#12-broker请求处理)
    - [1.3. 时间轮与延时操作](#13-时间轮与延时操作)

<!-- /TOC -->


# 1. kafka服务端
<!-- 
zookeeper
https://blog.csdn.net/lwglwg32719/article/details/86510029
https://www.cnblogs.com/yogoup/p/12000545.html
https://www.cnblogs.com/frankdeng/p/9310713.html
-->

## 1.1. Kafka控制器  
&emsp; 参考《深入理解kafka》第6章
<!-- 
Kafka Controller
https://www.kancloud.cn/nicefo71/kafka/1473379
-->

&emsp; 在Kafka集群中会有一个或多个broker，其中有一个broker会被选举为控制器(Kafka Controller)。 **<font color = "red">控制器负责管理整个集群中所有分区和副本的状态。</font>** 当某个分区的leader副本出现故障时，由控制器负责为该分区选举新的leader副本。当检测到某个分区的ISR集合发生变化时，由控制器负责通知所有broker更新其元数据信息。当使用kafka-topics.sh脚本为某个topic增加分区数量时，同样还是由控制器负责分区的重新分配。  

### 1.1.1. 控制器的选举  
&emsp; Kafka中的控制器选举工作依赖于ZooKeeper，成功竞选为控制器的broker会在ZooKeeper中创建/controller这个临时(EPHEMERAL)节点，此临时节点的内容参考如下：  
    
    {”version”：1,"brokerid":0,Htimestampn:n1529210278988n}
&emsp; 其中version固定为1, brokerid表示成为控制器的broker的id编号，timestamp表示竞选成为控制器时的时间戳。   
&emsp; 在任意时刻，集群中有且仅有一个控制器。每个broker启动的时候会去尝试读取/controller节点的brokerid的值，如果读取到brokerid的值不为-1，则表示己经有其他broker节点成功竞选为控制器，所以当前broker就会放弃竞选；如果ZooKeeper中不存在/controller节点，或者这个节点中的数据异常，那么就会尝试去创建/controller节点。当前broker去创建节点的时候，也有可能其他broker同时去尝试创建这个节点，只有创建成功的那个broker才会成为控制器，而创建失败的broker竞选失败。每个broker都会在内存中保存当前控制器的brokerid值，这个值可以标识为activeControllerId。  

&emsp; ZooKeeper中还有一个与控制器有关的/controller_epoch节点，这个节点是持久 (PERSISTENT)节点，节点中存放的是一个整型的controller_epoch值。controller_ epoch用于记录控制器发生变更的次数，即记录当前的控制器是第几代控制器，也可以称之为“控制器的纪元”。  
&emsp; controller_epoch的初始值为1，即集群中第一个控制器的纪元为1，当控制器发生变更时，每选出一个新的控制器就将该字段值加1。每个和控制器交互的请求都会携带controller epoch这个字段，如果请求的controller_epoch值小于内存中的controller_epoch值，则认为这个请求是向己经过期的控制器所发送的请求，那么这个请求会被认定为无效的请求。如果请求的controller_epoch值大于内存中的controller_epoch值，那么说明已经有新的控制器当选了。由此可见，Kafka通过controller_epoch来保证控制器的唯一性，进而保证相关操作的一致性。 

### 1.1.2. 控制器的异常选举  
&emsp; 当/controller节点的数据发生变化时，每个broker都会更新自身内存中保存的activeControllerldo。如果broker在数据变更前是控制器，在数据变更后自身的brokerid值与新的activeControllerld值不一致，那么就需要“退位”，关闭相应的资源，比如关闭状态机、注销相应的监听器等。有可能控制器由于异常而下线，造成/controller这个临时节点被自动删除；也有可能是其他原因将此节点删除了。   
&emsp; 当/controller节点被删除时，每个broker都会进行选举，如果broker在节点被删除前是控制器，那么在选举前还需要有一个“退位”的动作。如果有特殊需要，则可以手动删除/controller节点来触发新一轮的选举。当然关闭控制器所对应的broker，以及手动向/controller节点写入新的brokerid的所对应的数据，同样可以触发新一轮的选举。 

### 1.1.3. 控制器的功能  
#### 1.1.3.1. kafka在ZK的目录分布  
<!-- 
Zookeeper 是 Kafka 用来负责集群元数据管理、控制器选举等操作的。Producer 是负责将消息发送到 Broker 的，Broker 负责将消息持久化到磁盘，而 Consumer 是负责从Broker 订阅并消费消息。
-->
&emsp; kafka在zookeeper中的存储结构如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-21.png)  
&emsp; kafka中Zookeeper作用：集群管理，元数据管理。  
&emsp; 注意：producer不在zk中注册，消费者在zk中注册。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-4.png)  
* Broker 注册：Broker 是分布式部署并且之间相互独立，Zookeeper 用来管理注册到集群的所有 Broker 节点。
* Topic 注册：在 Kafka 中，同一个 Topic 的消息会被分成多个分区并将其分布在多个 Broker 上，这些分区信息及与 Broker 的对应关系也都是由Zookeeper在维护
* 生产者负载均衡：由于同一个 Topic 消息会被分区并将其分布在多个 Broker 上，因此，生产者需要将消息合理地发送到这些分布式的Broker上。
* 消费者负载均衡：与生产者类似，Kafka 中的消费者同样需要进行负载均衡来实现多个消费者合理地从对应的 Broker 服务器上接收消息，每个消费者分组包含若干消费者，每条消息都只会发送给分组中的一个消费者，不同的消费者分组消费自己特定的 Topic 下面的消息，互不干扰。  

#### 1.1.3.2. 控制器功能  
&emsp; **<font color =red>具备控制器身份的broker需要比其他普通的broker多一份职责，具体细节如下：</font>**  

* 监听分区相关的变化。为ZooKeeper中的/admin/reassign_partitions节点注册PartitionReassignmentHandler，用来处理分区重分配的动作。为ZooKeeper中的/isr_change_notification节点注册 IsrChangeNotificetionHandler，用来处理ISR集合变更的动作。为ZooKeeper中的admin/preferred-replica-election节点添加PreferredReplicaElectionHandler，用来处理优先副本的选举动作。
* 监听主题相关的变化。为ZooKeeper中的/brokers/topics节点添加TopicChangeHandler，用来处理主题增减的变化；为ZooKeeper中的/admin/delete_topics节点添加TopicDeletionHandler，用来处理删除主题的动作。
* 监听broker相关的变化。为ZooKeeper中的/brokers/ids节点添加BrokerChangeHandler, 用来处理broker增减的变化。
* 从ZooKeeper中读取获取当前所有与主题、分区及broker有关的信息并进行相应的管理。对所有主题对应的ZooKeeper中的/brokers/topics/\<topic>节点添加PartitionModificationsHandler，用来监听主题中的分区分配变化。
* 启动并管理分区状态机和副本状态机。
* 更新集群的元数据信息。
* 如果参数auto.leader.rebalance.enable设置为true，则还会开启一个名为“auto-leader-rebalance-task”的定时任务来负责维护分区的优先副本的均衡。  

### 1.1.4. 控制器设计原理
&emsp; 控制器在选举成功之后会读取ZooKeeper中各个节点的数据来初始化上下文信息 （Controllercontext），并且需要管理这些上下文信息。比如为某个主题增加了若干分区，控制器在负责创建这些分区的同时要更新上下文信息，并且需要将这些变更信息同步到其他普通的broker节点中。不管是监听器触发的事件，还是定时任务触发的事件，或者是其他事件（比如ControlledShutdown）都会读取或更新控制器中的上下文信息，那么这样就会涉及多线程间的同步。如果单纯使用锁机制来实现，那么整体的性能会大打折扣。针对这一现象，Kafka的控制器使用单线程基于事件队列的模型，将每个事件都做一层封装，然后按照事件发生的先后顺序暂存到LinkedBlockingQueue中，最后使用一个专用的线程（ControllerEventThread）按照FIFO（First Input First Output，先入先出）的原则顺序处理各个事件，这样不需要锁机制就可以在多线程间维护线程安全。  
&emsp; 在Kafka的早期版本中，并没有采用Kafka Controller这样一个概念来对分区和副本的状态进行管理，而是依赖于ZooKeeper，每个broker都会在ZooKeeper上为分区和副本注册大量的监听器（Watcher）。当分区或副本状态变化时，会唤醒很多不必要的监听器，这种严重依赖ZooKeeper的设计会有脑裂、羊群效应，以及造成ZooKeeper过载的隐患(旧版的消费者客户端存在同样的问题)。  
&emsp; 在目前的新版本的设计中，只有Kafka Controller在ZooKeeper上注册相应的监听器，其他的broker极少需要再监听ZooKeeper中的数据变化， 这样省去了很多不必要的麻烦。不过每个broker还是会对/controller节点添加监听器，以此来监听此节点的数据变化(ControllerChangeHandler)。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-71.png)  

### 1.1.5. 优雅关闭  
&emsp; 可以直接修改 kafka-server-step.sh 脚本的内容，将其中的第一行命令修改如下：  

    PIDS=$(ps ax | grep - i 'kafka' | grep java | grep - v grep | awk  '{print $1 )' )  

&emsp; 即把“\.Kafka”去掉，这样在绝大多数情况下是可以奏效的。如果有极端情况，即使这样也不能闭，那么需要按照以下两个步骤就可以优雅地关闭Kafka的服务进程：
1. 获取 Kafka 的服务进程号 PIDS 可以使用 Java 中的 jps 命令或使用 Linux 系统中的ps 命令来查看。
2. 使用 kill - s TERM PIDS kill -l5 PIDS 的方式来关闭进程，注：千万不要使用kill -9的方式。

## 1.2. broker请求处理  
《kafka实战》第6章  

## 1.3. 时间轮与延时操作    
《深入理解kafka》第6章   


