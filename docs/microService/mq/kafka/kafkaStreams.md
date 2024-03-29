<!-- TOC -->

- [1. kafka Streams](#1-kafka-streams)
    - [1.1. 前言](#11-前言)
    - [1.2. 流处理简介](#12-流处理简介)
    - [1.3. 流处理的架构](#13-流处理的架构)
        - [1.3.1. 流分区与任务](#131-流分区与任务)
        - [1.3.2. 线程模型](#132-线程模型)
        - [1.3.3. 本地状态存储](#133-本地状态存储)
        - [1.3.4. 容错性（Failover）](#134-容错性failover)
    - [1.4. 操作KStream和KTable](#14-操作kstream和ktable)
        - [1.4.1. 流处理的核心概念](#141-流处理的核心概念)
        - [1.4.2. 具体操作](#142-具体操作)
            - [1.4.2.1. 窗口操作](#1421-窗口操作)
            - [1.4.2.2. 连接操作](#1422-连接操作)
            - [1.4.2.3. 转换操作](#1423-转换操作)
            - [1.4.2.4. 聚合操作](#1424-聚合操作)

<!-- /TOC -->

# 1. kafka Streams
&emsp; **<font color = "red">参考《Kafka并不难学》第7章</font>**  

## 1.1. 前言  
&emsp; Kafka有四个核心的API:  

* The Producer API，允许一个应用程序发布一串流式的数据到一个或者多个Kafka topic。
* The Consumer API，允许一个应用程序订阅一个或多个topic，并且对发布给他们的流式数据进行处理。
* The Streams API，允许一个应用程序作为一个流处理器，消费一个或者多个topic产生的输入流，然后生产一个输出流到一个或多个topic中去，在输入输出流中进行有效的转换。
* The Connector API，允许构建并运行可重用的生产者或者消费者，将Kafka topics连接到已存在的应用程序或者数据系统。比如，连接到一个关系型数据库，捕捉表（table）的所有变更内容。  

## 1.2. 流处理简介  
&emsp; 流处理是一种用来处理无穷数据集的数据处理引擎，通常无穷数据集具有以下几个特点：  

* 无穷数据：持续产生的数据，它们通常会被称为流数据 

        提示：流数据与批次数据的区别在于——数据边界是否有限。在大数据应用场景中，无穷数据通常用来表示流数据，有穷数据用来表示批次数据。
* 低延时：流数据通常都是实时处理，数据实时产生，然后流处理引擎实时处理流数据，因此延时很短  

&emsp; **什么是流式计算？**  
&emsp; 通常情况下，流式计算与批处理计算会放在一起做比较分析。   
1. **在流式计算模型中，数据的输入是持续不断的，这意味着永远不知道数据的上限是多少，因此，计算产生的结果也是持续输出的，**流程如下图所示。流式计算一般对实时性要求较高，为了提升计算效率，通常会采用增量计算来替代全量计算。  
![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-42.png)  
2. 在批处理计算模型中，通常数据上限是可知的，一般会先定义计算逻辑，然后进行全量计算，并将计算结果一次性输出。流程如下图所示。  
![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-43.png)  

## 1.3. 流处理的架构  
![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-44.png)  
&emsp; <font color = "red">Kafka流处理输入的数据来源于Kafka系统中的业务主题，处理后的结果会存储到Kafka系统中新的业务主题中。</font>   
&emsp; 上图中，消费者程序和生产者程序并不需要用户在应用程序中显式地实例化，而是通过Kafka流处理根据参数来隐式地实例化和管理，这让操作变得更加简单。用户只需关心核心业务逻辑（即上图中的任务模块）的编写，其他的都交由Kafka流处理来实现。  

### 1.3.1. 流分区与任务  
&emsp; Kafka流处理通过流分区来处理数据，内容包含存储和传输数据。 **Kafka流处理使用分区和任务概念来作为并发模型中的逻辑单元。**在并发环境中，通过分区数据来保证灵活性、可扩展性、高效性和容错性。  
1. 流分区的作用  
&emsp; 在Kafka流处理中，每个流分区是完全而有序的数据队列。这些有序数据记录会映射到Kafka系统主题分区中。  
&emsp; 流数据中映射的消息数据来自于Kafka系统主题。消息数据中键（Key）值是Kafka和Kafka流处理的关键，它决定了数据如何被路由到指定分区。  
2. 任务的作用  
&emsp; 一个应用程序可以被拆分成多个任务来执行。Kafka流处理会根据输入流分区来创建固定数量的任务，每个任务分配一个输入流分区列表。  
&emsp; 任务是应用程序并行执行时的固定单元，因此分区对任务的分配不会造成影响。任务可以基于分配到的分区来实现相关处理流程，同时为每个分配到的分区保留一个缓冲区，并从这些缓冲区逐一地处理消息，这样可以让Kafka流处理的任务自动并行处理。  

        提示：  
        在一个子处理流程中，如果一个Kafka流处理应用程序指定了多个处理流程，则每个任务只实例化一个处理对象。  
        另外，一个处理流程可以被拆分为多个独立的子处理流程，只要保证子处理流程与其他子处理流程没有交集即可。通过这种方式可以让任务之间保持负载均衡。  

&emsp; 下图展示了不同主题的两个分区的流处理过程。(不同主题的两个分区的流处理过程)  
![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-45.png)  

    提示：  
    Kafka流处理不是一个资源管理器，而是一个类库。   
    Kafka 流处理可以运行在任何流处理应用程序中。应用程序的多个实例可以运行在同一台主机上，也可以分发到不同的主机进行执行。如果一个应用程序实例发生异常导致不可用，则该任务将会自动地在其他的实例上重新创建，并从相同的分区中继续读取数据。  

### 1.3.2. 线程模型  
&emsp; Kafka流处理允许用户配置多个线程，并通过多线程来均衡应用程序中的任务数。每个线程的处理流程可以执行一个或者多个任务。  
1. 线程模型的处理流程  
&emsp; 下图所示是使用一个线程来处理多个流任务。  
![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-46.png)  
&emsp; 启动多个流线程或更多的应用程序，只需要复制执行流程即可。比如，将一份业务逻辑处理代码复制到不同的主机上进行执行。这样做的好处是，通过多线程来并行处理不同Kafka系统主题分区中的数据，能提升处理效率。   
    
        提示：这些线程之间的状态是不共享的。因此，不需要线程间的协作。这使得运行一个多并发的处理流程实例变得非常简单。

2. 线程模型的优点  
&emsp; （1）易扩展：对Kafka流处理应用程序进行扩展是很容易的，只需要运行应用程序的实例即可。  
&emsp; （2）自动分配分区：Kafka流处理会监听任务数量的变化，并自动给任务分配分区。  
&emsp; （3）多线程：用户在启动应用程序时，可以根据Kafka系统输入主题中的分区数来设置线程数。每个线程至少对应一个分区。  

3. 实例：多线程并发场景  
    &emsp; 假设有一个Kafka流处理应用程序，它从两个业务主题（A和B）中读取数据，每个业务主题都有3个分区。如果用户在一台主机上启动一 个应用程序实例，并设置线程数为2，则最终会出现两个Kafka流处理线程。  
    &emsp; 由于输入主题A和输入主题B的最大分区数均为3，所以Kafka流处理会默认将其拆分为3个任务，然后将合计的6个分区（主题A和主题B 的分区总和）均匀地分布到3个任务中。  
    &emsp; 这种情况下，每个任务会同时从两个分区中读取数据。最终，这3个任务会被均匀地分布在两个线程当中。两个线程的作用分别如下。  

    * 线程1包含两个任务，从四个分区（主题A的两个分区和主题B的两个分区）中读取数据； 
    * 线程2包含一个任务，从两个分区（主题A的一个分区和主题B的一个分区）中读取数据。  

    具体实现流程如下图所示。(多线程多任务执行流程)  
    ![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-47.png)  
    随着业务数据量的增加，需要对现有的应用程序进行拓展。实施的 具体方案是，在另外一台主机上启动该应用程序，并设置线程数为1。具体实现流程如下图(新增应用程序和线程数)所示。  
    当前总分区数为6个，线程总数为3个。当任务被重新分配时，相同的分区、任务和存储状态，都会被移到新的线程中，从而使应用程序达到负载均衡。  
    ![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-48.png)  

### 1.3.3. 本地状态存储  
&emsp; Kafka流处理提供的状态存储机制，可用来保存和查询应用程序产生的状态数据。例如，在执行连接、聚合操作时，Kafka流处理会自动创建和管理这些状态存储。  
&emsp; 下图（流处理任务本地状态存储）展示了两个流处理任务，以及它们专用的本地状态存储。  
&emsp; 在Kafka流处理应用程序中，每个流任务可以集成一个或多个本地状态存储，这些本地状态存储可以通过应用接口来进行存储和查询。同时，Kafka 流处理也为本地状态存储提供了容错机制和自动恢复功能。  
![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-49.png)  

### 1.3.4. 容错性（Failover）  
&emsp; Kafka流处理继承了Kafka系统主题分区的两大能力——高可用能力、副本故障自动转移能力。因而，在流数据持久化到Kafka系统主题时，即使应用程序失败也会自动重新处理。  
&emsp; Kafka流处理中的任务利用Kafka系统客户端提供的容错机制来处理异常问题。如果某个任务在发生故障的主机上执行，则Kafka流处理会自动在应用程序的其他运行实例中重新启动该任务。  
&emsp; 每个状态存储都会维护一个更改日志主题的副本，用来跟踪状态的更新。这些更改日志主题也进行了分区，以便每个本地状态存储实例和访问存储的任务都有其专属的更改日志主题分区。在更该日志主题上启用日志压缩，可以方便、安全地清理无用数据，避免主题数据无限增长。  
&emsp; 如果任务在一台主机上运行失败，之后在另一台主机上重新启动，则Kafka流处理在恢复对新启动的任务之前，通过回滚机制将关联的状态存储恢复到故障之前，整个过程对于用户来说是完全透明的。  
&emsp; 需要注意的是，任务初始化所耗费的时间，通常取决于回滚机制恢复状态存储所用的时间。为了缩短恢复时间，用户可以将应用程序配置为本地状态的备用副本。当发生任务迁移时，Kafka流处理会尝试将任务分配给已存在备用副本的应用程序实例，以减少任务初始化所耗费的时间。  

    提示： 可以通过设置属性num.standby.replicas来分配每个任务的备用副本数。  

## 1.4. 操作KStream和KTable  
&emsp; **<font color = "red">Kafka流处理中定义了以下三个抽象概念：</font>**  

* KStream：它是一个消息流的抽象，消息记录由“键-值”对构成，其中的每条消息记录代表无界数据集中的一条消息记录；  
* KTable：它是一个变更日志流的抽象，其中每条消息记录代表一次更新。如果消息记录中的键不存在，则更新操作将被认为是创建；  
* GlobalKTable：它和KTable类似，也是一个变更日志流的抽象，其中每条消息记录代表一次更新。  

&emsp; 无论是消息流抽象，还是变更日志流抽象，都能通过一个或者多个 Kafka系统主题来进行创建。  
&emsp; 在Kafka流处理中，KStream和KTable都提供了一系列的转换操作，每个操作可以产生一个或多个KStream和KTable对象，所有这些转换的函数连接在一起，则形成了一个复杂的处理流程。  

    提示： 因为KStream和KTable都属于强类型，所以这些转换操作都被定义成通用函数。因此，用户在使用时，需要显性指定输入和输出的数据类型。  

### 1.4.1. 流处理的核心概念  
&emsp; Kafka流处理是一个客户端类库，用于处理和分析Kafka系统主题中的数据。  
&emsp; Kafka流处理能够准确区分事件时间和处理时间，另外，它支持窗口操作，能够简单而有效地管理和查询应用程序的状态。  
&emsp; **<font color = "red">Kafka流处理有以下几个核心概念：时间、状态、处理保障。</font>**
1. 时间  
    &emsp; 时间（Time）是Kafka流处理中一个比较重要的概念，它定义了Kafka流处理如何进行建模和集成。例如，窗口操作就是基于时间边界来定义的。  
    &emsp; 目前Kafka流处理定义了以下三种通用的时间类型。  

    * 事件时间：事件或消息记录产生的时间，都包含在消息记录中。 产生的时间由生产者实例在写入数据时指定一个时间戳字段。  
    * 处理时间：Kafka流处理应用程序开始处理事件的时间点，即消息记录存入Kafka代理节点的时间。一般情况下，处理时间迟于事件时间，它们之间的时间间隔单位可能是毫秒、秒或小时。  
    * 摄取时间：消息被处理后存储到Kafka系统主题的时间点。如果 消息没有被处理，而是直接存储到Kafka系统主题中，则这个阶段没有处理时间，只有存储时间。  

    &emsp; 事件时间和摄取时间的选择，实际上是通过配置Kafka来完成的。从Kafka 0.10.x开始，每条消息记录会自动附带一个时间戳。通过配置Kafka代理节点或主题可以设置消息记录的时间戳类型。例如，在 Kafka 系统主题中配置 message.timestamp.type属性，该属性可选值包括 CreateTime和LogAppendTime。  
    &emsp; 在Kafka流处理中，可以通过TimestampExtractor接口给每条消息记录附加一个时间戳，用户可以根据实际的业务需求来实现。  

2. 状态  
    &emsp; 某些流处理应用程序是不需要状态信息的，这意味着，每条消息记录的处理都是互相独立的。而某些流处理应用程序是需要保存一些状态信息的，例如，聚合操作需要状态信息来随时保存当前的聚合结果。  
    &emsp; Kafka流处理引入“状态存储”的概念，流处理应用程序可以对状态存储进行读/写。每个流处理任务都可以集成一个或多个状态存储。状态存储支持持久化键值对存储、内存哈希表存储或者其他形式的存储。  
    &emsp; 交互式查询允许进程内部和外部的代码对状态存储进行只读操作。 另外，Kafka流处理还为本地状态存储提供了容错和自动恢复功能。  

3. 处理保障  
    &emsp; 在流处理中，常见的问题之一是“在处理过程中遇到异常，流处理是否能够保障每条记录只被处理一次”。  
    &emsp; 在0.11.0.0版本之前，Kafka流处理系统不能完全保证每条记录只被处理一次。  
    &emsp; 从0.11.0.0版本开始，Kafka系统开始支持生产者实例以事物性和幂等性向主题分区发送消息数据。通过这一特性，Kafka流处理可以准确地支持一次性处理。  

        提示：幂等性是指，用户对同一操作发起的一次或多次请求的结果是一致的，不会因为多次请求而产生副作用。  

    &emsp; Kafka流处理框架和其他实时处理框架不同之处是，Kafka流处理和底层的Kafka存储机制紧密集成，确保是原子性操作，其操作内容包括：对主题偏移量的提交、对状态存储的更新、对输出主题的写入等。  
    &emsp; 如果要启用一次性处理保障，则需要配置属性processing.guarantee的值为exactly_once。  

### 1.4.2. 具体操作
#### 1.4.2.1. 窗口操作  
&emsp; 流数据属于时间无界的数据集。而在某些特定的场景下需要处理有界的数据集，这时可以通过窗口来实现。  
&emsp; 在处理流数据时，会把消息记录按照时间进行分组，每一组由一个窗口构成。Kafka目前支持四种窗口，下面分别介绍。  
1. 翻滚时间窗口翻滚时间窗口是大小固定、不重叠、无间隙的一类窗口模型。窗口的间隔是由窗口大小来决定的，这样可以有效地保证窗口之间不会发生重叠现象，一条消息记录也仅属于一个窗口。  
&emsp; 下图(翻滚时间窗口)是一个窗口大小为5min的翻滚时间窗口。  
![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-50.png)  
&emsp; 上图中，颜色相同的方块代表消息记录的键是相同的，而窗口的创建是根据每条消息记录的键来实现的。  
&emsp; 需要注意的是，翻滚时间窗口的取值范围是闭区间到开区间，即，低位是闭区间，高位是开区间。而翻滚时间窗口规定，第一个窗口的取值必须从0开始。例如，窗口大小为5000ms的滚动时间窗口取值为[0, 5000)、[5000, 10000)等，并不是[1000,6000)、[6000,11000)或者一些其他随机的数字。 
 
        提示：  
        闭区间是指区间边界的两个值都包含在内，例如[0, 1]。  
        开区间是指区间边界的两个值都不包含在内，例如(0, 1)。  
&emsp; 【实例】实现一个滚动时间窗口，大小为5min。 具体内容见下述代码（实现滚动时间窗口大小为5min）。    
![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-51.png)  

2. 跳跃时间窗口  
&emsp; 跳跃时间窗口是基于时间间隔的窗口模型，是一个大小固定、可能会出现重叠的窗口模型。  
&emsp; “跳跃”现象由窗口大小和时间间隔两个属性决定。如果时间间隔小于窗口大小，则会出现这种现象。  
&emsp; 例如，配置一个窗口大小为5min，时间间隔为1min的跳跃时间窗口。由于跳跃时间窗口会出现重叠，所以消息记录可能属于多个窗口， 如下图（跳跃时间窗口）所示。  
&emsp; 下图中，颜色相同的方块代表消息记录的键是相同的，而窗口的创建是根据每条消息记录的键来实现的。  
        
        提示： 在 Kafka 流处理中，时间单位是ms，如果是每隔5min标记一 次，那换算成 ms 就是300000ms。  
&emsp; 需要注意的是，跳跃时间窗口的取值范围是闭区间到开区间。即，低位是闭区间，高位是开区间。  
&emsp; 另外，翻滚时间窗口规定第一个窗口的取值必须从0开始。  
![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-52.png)  
&emsp; 例如，窗口大小为5000ms，时间间隔为3000ms的跳跃时间窗口取值为[0, 5000)、[3000, 8000)等，并不是[1000,6000)、[4000,9000)或一些 其他随机的数字。  
&emsp; 【实例】实现一个跳跃时间窗口，大小为5min，时间间隔为1min。具体实现见下述代码（设置跳跃时间窗口，大小为5min，时间间隔为1min）。     
![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-53.png)  

3. 滑动时间窗口  
&emsp; 滑动时间窗口与翻滚时间窗口、跳跃时间窗口不同，它是大小固定、沿着时间轴连续滑动的窗口模型。  
&emsp; 如果两条消息记录的时间戳之差在窗口大小范围之内，则这两条消 息记录属于同一个窗口。因此，滑动时间窗口不会和某个时间点对齐， 而是和消息记录的时间戳进行对齐，它的窗口大小取值范围为闭区间到 闭区间。  

        提示：  
        在Kafka流处理中，滑动时间窗口只有在连接操作时才会用到，即，在执行KStream连接操作时用到连接类JoinWindows。  
        例如，滑动时间窗口的大小为10ms，参与连接操作的两个KStream 中，消息数据的时间戳之差小于10ms的消息数据会被认为在同一个窗口中，从而进行连接操作。  

4. 会话窗口  
&emsp; 会话窗口需要先对消息记录的键做分组，然后根据实际的业务需求为分组后的数据定义一个起始点和结束点的窗口。  
&emsp; 例如，使用会话窗口统计用户登录游戏的时长。用户在登录游戏时会产生一条唯一的消息记录，此时会话窗口生成一个起始点，当用户退出游戏或者当前会话自动超时时，该用户的会话窗口会在结束后生成一个结束点。  
&emsp; 下图(会话窗口)中有三条消息记录，会话窗口大小为5min。  
![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-54.png)  
&emsp; 上图中，颜色相同的方块代表消息记录的键是相同的，而窗口的创建是根据每条消息记录的键来实现的。由于黑色方块的两条消息记录时间差超过5min，所以，黑色方块所代表的消息记录会产生两个会话窗口，白色方块所代表的消息记录会产生一个会话窗口。  
&emsp; 如果此时探测到新的消息记录，则会话窗口会发生相应的变化，如下图（添加新的消息记录）所示。  
![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-55.png)  
&emsp; 由于新增的灰色方块所代表的消息记录与之前黑色方块所代表的消息记录时间差小于5min，所以会话窗口会发生合并，形成一个会话窗口。   

    * 新增的第一个白色方块所代表的消息记录和之前的消息记录时间 差小于5min，所以会在同一个会话窗口，  
    * 新增的第二个白色方块所
    
    代表的消息记录和已有的消息记录时间 差大于5min，所以会另起一个会话窗口。  


&emsp; 【实例】实现一个会话窗口大小为5min。具体见代码9-3。 代码9-3 设置会话窗口大小为5min
![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-56.png)  

#### 1.4.2.2. 连接操作  
&emsp; 连接操作是通过消息记录键将两个流数据记录进行合并，生成一个新的流数据。  
&emsp; Kafka流处理中将流抽象为KStream、KTable、GlobalKTable三种类型，因此连接操作也是在这三类流之间进行互相操作，即：  

* KStream和KStream； 
* KTable和KTable； 
* KStream和KTable； 
* KStream和GlobalKTable； 
* KTable和GlobalKTable。  

&emsp; Kafka流处理提供了三种连接操作——内连接、左连接、外连接，对应的函数分别是join()、leftJoin()、outerJoin()。    
&emsp; 三种类型与三种连接操作的支持关系见下表。
![image](http://www.wt1814.com/static/view/images/microService/mq/kafka/kafka-57.png)  
  

#### 1.4.2.3. 转换操作  
&emsp; 在Kafka流处理中，KStream和KTable支持一系列的转换操作。这些转换操作都可以被转换为一个或者多个连接，这些转换后的连接组合在一起形成一个复制的流处理关系。  
&emsp; 由于KStream和KTable都是强类型，因此这些转换操作都以泛型的方式进行定义。  

    提示：  
    泛型是程序设计语言的一种特性，用于实现一个通用的标准容器库。  
&emsp; 对于KStream转换操作来说，某些KStream可以产生一个或多个 KStream对象，另外一些则产生一个KTable对象。  
&emsp; 而对于KTable来说，所有的转换操作都只能产生一个KTable对象。 1. 无状态转换 这种转换不依赖任何状态（如过滤、分组等）就可以完成，不要求Kafka流处理器去关联状态存储。  

#### 1.4.2.4. 聚合操作  
&emsp; 在Kafka流处理中，聚合操作是一种有状态的转换，通过Aggregator类的apply()函数来实现聚合功能。聚合操作是基于键来完成的，这意味着，操作的流数据会先按照相同的键来进行分组，然后对消息记录进行聚合，而在执行聚合操作时可以选择使用窗口或不使用窗口。  
