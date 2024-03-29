
<!-- TOC -->

- [1. 分布式](#1-分布式)
    - [1.1. 分布式理论CAP](#11-分布式理论cap)
    - [1.2. 分布式ID](#12-分布式id)
    - [1.3. 分布式事务](#13-分布式事务)
        - [1.3.1. 事务模型DTP及XA](#131-事务模型dtp及xa)
        - [1.3.2. TCC](#132-tcc)
            - [1.3.2.1. TCC流程](#1321-tcc流程)
            - [1.3.2.2. TCC问题](#1322-tcc问题)
            - [1.3.2.3. TCC问题2](#1323-tcc问题2)
        - [1.3.3. Saga](#133-saga)
        - [1.3.4. 消息模式](#134-消息模式)
        - [1.3.5. 分布式事务的选型](#135-分布式事务的选型)
        - [1.3.6. 分布式事务框架Seata](#136-分布式事务框架seata)
            - [1.3.6.1. Seata四种模式的区别](#1361-seata四种模式的区别)
            - [1.3.6.2. AT模式详解](#1362-at模式详解)
                - [1.3.6.2.1. AT模式流程](#13621-at模式流程)
                - [1.3.6.2.2. AT缺点](#13622-at缺点)
    - [1.4. 分布式锁](#14-分布式锁)
        - [1.4.1. 分布式锁介绍](#141-分布式锁介绍)
        - [1.4.2. Redis分布锁](#142-redis分布锁)
            - [1.4.2.1. RedisLock](#1421-redislock)
            - [1.4.2.2. 使用redis分布式锁的注意点](#1422-使用redis分布式锁的注意点)
            - [1.4.2.3. Redisson](#1423-redisson)
        - [1.4.3. ZK分布式锁](#143-zk分布式锁)
        - [1.4.4. MySql分布式锁](#144-mysql分布式锁)
        - [1.4.5. 分布式锁选型（各种锁的对比）](#145-分布式锁选型各种锁的对比)

<!-- /TOC -->


# 1. 分布式  
&emsp; `分布式带来数据一致性的问题。` 解决方案：采用事务、锁，也可以使用补偿方案。   

## 1.1. 分布式理论CAP
1. CAP：一致性(Consistency)、可用性(Availability)、分区容错性(Partition tolerance)。  
&emsp; 一致性模型：强一致性、弱一致性、最终一致性、单调一致性/顺序一致性、会话一致性。  
2. BASE：**Basically Available(基本可用)、Soft state(软状态)和Eventually consistent(最终一致性)三个短语的缩写。<font color = "red">`BASE是对CAP中AP的一个扩展`，是对CAP中一致性和可用性权衡的结果。</font>**  
3. **<font color = "blue">无处不在的CAP的C</font>**  
&emsp; `只要是分布式或集群`，甚至一个接口中处在不同事务的调用，`都会有数据一致性的问题`。 例如Mysql主从复制、binlog和redolog的两阶段提交......  

## 1.2. 分布式ID
1. 4类分布式ID的生成方式：1.UUID；2.数据库方式（主键自增、序列号、<font color = "clime">号段模式</font>）；<font color = "red">3.redis、ZK等中间件；4.雪花算法。</font>  
    * 数据库Oracle中有序列SEQUENCE；在Mysql中可以建一张伪序列号表。  
    * 号段模式可以理解为从数据库批量的获取自增ID，每次从数据库取出一个号段范围。  
2. snowflake算法：`结构、特点、缺点、解决方案`    
    1. snowflake算法所生成的ID`结构`：正数位(占1比特)+ 时间戳(占41比特)+ 机器ID(占5比特)+ 数据中心(占5比特)+ `自增值(占12比特)`，总共64比特组成的一个Long类型。可以根据自身业务特性分配bit位，非常灵活。
    2. `特点`：ID呈趋势递增。  
    3. `缺点：` <font color="red">依赖于系统时钟的一致性。如果某台机器的系统时钟回拨，有可能造成ID冲突，或者ID乱序。</font>  
    &emsp; 百度UidGenerator如果时间有任何的回拨，那么直接抛出异常。此外UidGenerator通过消费未来时间克服了雪花算法的并发限制。   
    4. **<font color = "clime">时钟回拨问题解决方案：</font>**    
    &emsp; **<font color = "red">雪花算法中，第53-64的bit位：这12位表示序列号，也就是单节点1毫秒内可以生成2^12=4096个不同的ID。发生时钟回拨：</font>**  
        1. 抛异常。  
        2. `可以对给定的基础序列号稍加修改，后面每发生一次时钟回拨就将基础序列号加上指定的步长，`例如开始时是从0递增，发生一次时钟回拨后从1024开始递增，再发生一次时钟回拨则从2048递增，这样还能够满足3次的时钟回拨到同一时间点（发生这种操作就有点扯了）。  
        3. 当业务不是很繁忙，可以将12位序列号预留两位。2位的扩展位允许有3次大的时钟回拨，如果其超过三次可以打印异常。  

## 1.3. 分布式事务
### 1.3.1. 事务模型DTP及XA
1. X/Open DTP中的角色：
    * 应用程序
    * 事务管理器（协调者），<font color = "clime">管理全局事务，协调事务的提交或者回滚，并协调故障恢复。</font>常见的事务管理器(TM)是交易中间件。  
    * 资源管理器（参与者），能够提供单数据源的事务能力，它们通过XA接口，将本数据库的提交、回滚等能力提供给事务管理器调用。
    * XA接口  
    &emsp; XA是DTP模型定义的接口，指的是模型中TM(事务管理器)和RM(资源管理器)之间进行通信的接口，用于向事务管理器提供该资源管理器(该数据库)的提交、回滚等能力。目前大多数实现XA的都是数据库或者MQ，所以提起XA往往多指基于资源层的底层分布式事务解决方案。其实现在也有些数据分片框架或者中间件也支持XA协议，毕竟它的兼容性、普遍性更好。  
2. **2PC：**  
   &emsp; <font color = "clime">二阶段提交将分布式事务的提交拆分为2个阶段：准备阶段prepare和提交阶段commit/rollback。</font>  
   &emsp; 尽量(不能100%)保证了数据的强一致，适合对数据强一致要求很高的关键领域。  
   &emsp; **XA二阶段问题：**   
   0. **<font color = "red">小结：</font>**【阻塞、(网络)数据不一致、宕机(单点故障、协调者和参与者同时宕机)】  
   1. 同步阻塞问题。执行过程中，所有参与节点都是事务阻塞型的。`当参与者占有公共资源时，其他第三方节点访问公共资源不得不处于阻塞状态。` 
   2. 丢失消息导致的数据不一致问题。在二阶段提交的阶段二中，<font color = "clime">当协调者向参与者发送commit请求之后，发生了`局部网络异常`或者在发送commit请求过程中协调者发生了故障，这会导致只有一部分参与者接受到了commit请求。</font>  
   3. 单点故障问题。  
   4. 二阶段无法解决的问题：协调者和参与者同时宕机。  
3. **3PC：**  
    1. 3PC也就是多了一个阶段(一个询问的阶段)，分别是准备、预提交和提交这三个阶段。  
        1. 准备阶段单纯就是协调者去访问参与者，类似于“你还好吗？能接请求不”。  
        2. 预提交其实就是2PC的准备阶段，除了事务的提交啥都干了。  
        3. 提交阶段和2PC的提交一致。
    2. XA三阶段特点：多一个阶段  
        1. 降低阻塞    
        &emsp; <font color = "clime">3PC的引入降低了参与者的阻塞范围，解决协调者和参与者同时挂掉的问题，减少了数据不一致的情况。</font>  
        &emsp; <font color = "clime">超时机制也降低了参与者的阻塞范围，因为参与者不会一直持有事务资源并处于阻塞状态。</font>  
        3. <font color = "red">数据不一致问题依然存在。</font>当参与者收到preCommit请求后等待do commite指令时，此时如果协调者请求`中断事务`，而协调者无法与参与者正常通信，会导致参与者继续提交事务，造成数据不一致。  
        ~~3PC 虽然解决了 Coordinator 与参与者都异常情况下导致数据不一致的问题，3PC 依然带来其他问题：比如，网络分区问题，在 preCommit 消息发送后突然两个机房断开，这时候 Coordinator 所在机房会 abort, 另外剩余参与者的机房则会 commit。~~  
        3. `解决单点故障问题`  
        &emsp; ~~引入超时机制。**<font color = "red">同时在协调者和参与者中都引入超时机制。一旦事物参与者迟迟没有接到协调者的commit请求，`会自动进行本地commit`。3PC追求的是最终一致性。这样也有效解决了协调者单点故障的问题。</font>**~~  
        &emsp; 2pc协议在协调者和执行者同时宕机时(协调者和执行者不同时宕机时，都能确定事务状态)，选出协调者之后 无法确定事务状态，会等待宕机者恢复才会继续执行(无法利用定时器来做超时处理，超时后也不知道事务状态，无法处理，强制处理会导致数据不一致)，这段时间这个事务是阻塞的，其占用的资源不会被释放。  
        &emsp; 3pc感知事物状态。只要有一个事务进入PreCommit，说明各执行节点的状态都是canCommit。  
4. 2pc和3pc区别：  
&emsp; 相比较2PC而言，3PC对于协调者（Coordinator）和参与者（Partcipant）都设置了超时时间，而2PC只有协调者才拥有超时机制。这解决了一个什么问题呢？   
&emsp; 这个优化点，主要是避免了参与者在长时间无法与协调者节点通讯（协调者挂掉了）的情况下，无法释放资源的问题，因为参与者自身拥有超时机制会在超时后，自动进行本地commit从而进行释放资源。而这种机制也侧面降低了整个事务的阻塞时间和范围。  
&emsp; ~~3PC对于协调者和参与者都设置了超时时间，而2PC只有协调者才拥有超时机制。这个优化点，避免了参与者在长时间无法与协调者节点通讯（协调者挂掉了）的情况下，无法释放资源的问题。~~  
&emsp; ~~而且3pc多设置了一个缓冲阶段保证了在最后提交阶段之前各参与节点的状态是一致的。~~

### 1.3.2. TCC
#### 1.3.2.1. TCC流程
1. 介绍：**<font color = "red">TCC是一种`业务层面或者是应用层`的`两阶段、补偿型`的事务。</font>**  
2. 流程：TCC是`Try（检测及资源锁定或者预留）`、Commit（确认）、Cancel（取消）的缩写，业务层面需要写对应的三个方法。  
3. TCC与二阶段比较  
&emsp; 使用2PC机制时，以提交为例，一个完整的事务生命周期是：begin -> 业务逻辑 -> prepare -> commit。  
&emsp; 使用TCC机制时，以提交为例，一个完整的事务生命周期是：begin -> 业务逻辑(try业务) -> commit(comfirm业务)。  
&emsp; 综上，可以从执行的阶段上将二者一一对应起来：  
&emsp; 1、2PC机制的业务阶段 等价于 TCC机制的try业务阶段；  
&emsp; 2、2PC机制的提交阶段（prepare & commit） 等价于 TCC机制的提交阶段（confirm）；  
&emsp; 3、2PC机制的回滚阶段（rollback） 等价于 TCC机制的回滚阶段（cancel）。  
&emsp; 因此，可以看出，虽然TCC机制中有两个阶段都存在业务逻辑的执行，但其中 `try业务阶段其实是与全局事务处理无关的`。认清了这一点，当再比较TCC和2PC时，就会很容易地发现，`TCC不是两阶段提交，而只是它对事务的提交/回滚是通过执行一段confirm/cancel业务逻辑来实现，仅此而已。`  


#### 1.3.2.2. TCC问题
1.  **<font color = "clime">TCC的数据最终一致性：基于Confirm和Cancel的`幂等性`，保证事务最终完成确认或者取消，保证数据的一致性。</font>**  
2. TCC的问题：  
    &emsp; **<font color = "clime">TCC两个阶段都由业务代码控制，由于网络阻塞等原因，可能导致两个阶段执行顺序相反，</font>** 引发问题：  
    1. `【允许】空回滚`（Try方法由于网络问题没收到超时了，此时事务管理器就会发出Cancel命令。需要支持Cancel在未执行Try的情况下能正常的Cancel）；  
    2. `【防止】资源悬挂`（TC回滚事务调用二阶段完成空回滚后，一阶段try请求又到了）。

&emsp; **<font color = "clime">解决方案：使用一张事务状态控制表（包含全局事务ID、分支事务ID、执行状态）。</font>**  
1. 什么是空回滚？空回滚就是对于一个分布式事务，在没有调用 TCC 资源 Try 方法的情况下，调用了二阶段的 Cancel 方法，Cancel 方法需要识别出这是一个空回滚，然后直接返回成功。  
&emsp; 思路很简单就是需要知道一阶段是否执行，如果执行了，那就是正常回滚；如果没执行，那就是空回滚。因此，需要一张额外的事务控制表，其中有分布式事务 ID 和分支事务 ID，第一阶段 Try 方法里会插入一条记录，表示一阶段执行了。Cancel 接口里读取该记录，如果该记录存在，则正常回滚；如果该记录不存在，则是空回滚。  
2. 怎么解决重复执行的幂等问题呢？一个简单的思路就是记录每个分支事务的执行状态。在执行前状态，如果已执行，那就不再执行；否则，正常执行。前面在讲空回滚的时候，已经有一张事务控制表了，事务控制表的每条记录关联一个分支事务，那我们完全可以在这张事务控制表上加一个状态字段，用来记录每个分支事务的执行状态。  
&emsp; 该状态字段有三个值，分别是初始化、已提交、已回滚。Try 方法插入时，是初始化状态。二阶段 Confirm 和 Cancel 方法执行后修改为已提交或已回滚状态。当重复调用二阶段接口时，先获取该事务控制表对应记录，检查状态，如果已执行，则直接返回成功；否则正常执行。  
3. 怎么实现才能做到防悬挂呢？根据悬挂出现的条件先来分析下，悬挂是指二阶段 Cancel 执行完后，一阶段才执行。也就是说，为了避免悬挂，如果二阶段执行完成，那一阶段就不能再继续执行。因此，当一阶段执行时，需要先检查二阶段是否已经执行完成，如果已经执行，则一阶段不再执行；否则可以正常执行。那怎么检查二阶段是否已经执行呢？大家是否想到了刚才解决空回滚和幂等时用到的事务控制表，可以在二阶段执行时插入一条事务控制记录，状态为已回滚，这样当一阶段执行时，先读取该记录，如果记录存在，就认为二阶段已经执行；否则二阶段没执行。  


#### 1.3.2.3. TCC问题2
&emsp; 在微服务架构下，很有可能出现网络超时、重发，机器宕机等一系列的异常情况。一旦遇到这些情况，就会导致分布式事务执行过程出现异常。  
* 接口幂等
	* 产生
	* 解决方案：在事务控制表上加一个状态字段，用来记录每个分支事务的执行状态。
* 【允许】空回滚
	* 产生：注册分支事务是在调用 RPC 时，Seata 框架的切面会拦截到该次调用请求， **<font color = "red">先向 TC 注册一个分支事务，然后才去执行 RPC 调用逻辑。如果 RPC 调用逻辑有问题，比如调用方机器宕机、网络异常，都会造成 RPC 调用失败，即未执行 Try 方法。但是分布式事务已经开启了，需要推进到终态，因此，TC 会回调参与者二阶段 Cancel 接口，从而形成空回滚。</font>**  
	* 解决方案： **<font color = "clime">需要一张额外的事务控制表，其中有分布式事务 ID 和分支事务 ID，第一阶段 Try 方法里会插入一条记录，表示一阶段执行了。Cancel 接口里读取该记录，如果该记录存在，则正常回滚；如果该记录不存在，则是空回滚。</font>**  
* 【防止】资源悬挂
	* 产生： **<font color = "clime">因为允许空回滚的原因，Cancel 接口认为 Try 接口没执行，空回滚直接返回成功，对于 Seata 框架来说，认为分布式事务的二阶段接口已经执行成功，整个分布式事务就结束了。但是这之后 Try 方法才真正开始执行，预留业务资源，前面提到事务并发控制的业务加锁，对于一个 Try 方法预留的业务资源，只有该分布式事务才能使用，然而 Seata 框架认为该分布式事务已经结束，也就是说，当出现这种情况时，该分布式事务第一阶段预留的业务资源就再也没有人能够处理了，对于这种情况，就称为悬挂，即业务资源预留后没法继续处理。</font>**    
	&emsp; **<font color = "clime">什么样的情况会造成悬挂呢？按照前面所讲，在 RPC 调用时，先注册分支事务，再执行 RPC 调用，如果此时 RPC 调用的网络发生拥堵，通常 RPC 调用是有超时时间的，RPC 超时以后，发起方就会通知 TC 回滚该分布式事务，`可能回滚完成后，RPC 请求才到达参与者，真正执行`，从而造成悬挂。</font>**  
	* 解决方案：`可以在二阶段执行时插入一条事务控制记录，状态为已回滚，这样当一阶段执行时，先读取该记录，如果记录存在，就认为二阶段已经执行；否则二阶段没执行。`   



### 1.3.3. Saga
1. Saga是一种解决长事务的分布式事务方案。Saga模型将一个分布式事务拆分为多个本地事务，也是一种`二阶段补偿性`事务（⚠️`注：二阶段提交和二阶段补偿的区别`）。  
2. Saga执行流程：  
    1. Saga有两种执行方式：  
        * 编排（无协调器）：每个服务产生并聆听其他服务的事件，并决定是否应采取行动。  
        &emsp; 该实现第一个服务执行一个事务，然后发布一个事件。该事件被一个或多个服务进行监听，这些服务再执行本地事务并发布(或不发布)新的事件，当最后一个服务执行本地事务并且不发布任何事件时，意味着分布式事务结束，或者它发布的事件没有被任何Saga参与者听到都意味着事务结束。  
        * 控制（有协调器）：saga协调器orchestrator以命令/回复的方式与每项服务进行通信，告诉服务应该执行哪些操作。  
    2. 有两种恢复策略：  
        * <font color = "red">backward recovery，向后恢复，补偿所有已完成的事务。</font>  
        * <font color = "red">forward recovery，向前恢复，重试失败的事务，假设每个子事务最终都会成功。</font>  
3. **<font color = "blue">Saga和TCC比较：</font>**  
    1. **<font color = "red">Saga没有“预留”Try行为，每个子事务(本地事务)依次执行提交阶段，所以会留下原始事务操作的痕迹，</font>** Cancel属于不完美补偿，需要考虑对业务上的影响。  
    2. Saga和TCC一样需要注意3个问题：1)保持幂等性；2)允许空补偿；3)防止资源悬挂。


### 1.3.4. 消息模式
&emsp; `XA、TCC、Saga等都属于同步方式。还有基于消息的异步方式。`  
0. 依据是否保证投递到订阅者，分为可靠消息（本地消息表、事务消息）及最大努力交付消息。  
1. **本地消息表：**  
&emsp; 基本思想：调用方存储业务表和消息表；调用其他服务，如果成功修改消息表状态， **<font color = "clime">如果失败发起重试（重试方式可以基于定时任务、mq等）。</font>**    
2. **事务消息：** ~~相比于最终一致性方案可靠性高，但也非强一致性。~~   
    1. 调用方开启消息事务；
    2. 发送消息；
    3. 调用方执行本地事务；  
    4. 调用方提交或回滚事务消息。
3. **最大努力通知方案和可靠消息最终一致性方案的区别：**  
&emsp; 可靠消息最终一致性方案可以保证的是只要系统A的事务完成，通过不停（无限次）重试来保证系统B的事务总会完成。但是最大努力方案就不同，如果系统B本地事务执行失败了，那么它会重试N次后就不再重试，系统B的本地事务可能就不会完成了。  

### 1.3.5. 分布式事务的选型
1. 多种分布式事务比较  
    1. 协调器、sage编排模式、mq消息  
2. 分布式事务的选型：(`~~判断事务发起方与事务被调用方的关系、数据一致的要求、性能~~`)  
    * **<font color = "clime">事务被调用方`跟随`事务发起方，使用`最终一致性的消息事务`；(基于消息实现的事务适用于分布式事务的提交或回滚只取决于事务发起方的业务需求)</font>** `例如消费长积分`  
    * **<font color = "clime">事务被调用方与事务发起方`协助完成`功能，使用`补偿性事务`；</font>** `例如库存服务和支付服务`   
        * 事务被调用方与事务发起方`协助完成`功能，事务被调用方与事务发起方的数据保持一致性，使用强一致性的TCC；  
        * SAGA可以看做一个异步的、利用队列实现的补偿事务。<font color = "red">适用于不需要同步返回发起方执行最终结果、可以进行补偿、对性能要求较高、不介意额外编码的业务场景。</font>  
    * **<font color = "clime">单体服务，多数据源，使用XA协议的服务；</font>**  
3. 具体使用：  
    1. mq  
    2. 本地消息表。  

### 1.3.6. 分布式事务框架Seata
#### 1.3.6.1. Seata四种模式的区别
&emsp; 四种分布式事务模式，分别在不同的时间被提出，每种模式都有它的适用场景：  

* AT 模式是无侵入的分布式事务解决方案，适用于不希望对业务进行改造的场景，几乎0学习成本。  
* TCC 模式是高性能分布式事务解决方案，适用于核心系统等对性能有很高要求的场景。  
* Saga 模式是长事务解决方案，适用于业务流程长且需要保证事务最终一致性的业务系统， **<font color = "red">Saga 模式一阶段就会提交本地事务，无锁，长流程情况下可以保证性能，</font>** `多用于渠道层、集成层业务系统。事务参与者可能是其它公司的服务或者是遗留系统的服务，无法进行改造和提供 TCC 要求的接口，也可以使用 Saga 模式。`  
* XA模式是分布式强一致性的解决方案，但性能低而使用较少。  



#### 1.3.6.2. AT模式详解
##### 1.3.6.2.1. AT模式流程
1. Seata AT模式是二阶段提交协议的演变。  
    1. 一阶段：执行用户SQL。业务数据和回滚日志记录在同一个本地事务中提交，释放本地锁和连接资源。  
        1. 解析SQL  
        2. 查询前镜像  
        3. 执行业务SQL  
        4. 查询后镜像  
        5. 插入回滚日志  
        6. 提交前，向 TC 注册分支：申请 product 表中，主键值等于 1 的记录的 全局锁 。  
        7. 本地事务提交：业务数据的更新和前面步骤中生成的 UNDO LOG 一并提交。  
        8. 将本地事务提交的结果上报给 TC。  
    2. 二阶段：Seata框架自动生成。commit异步化快速完成；rollback通过一阶段的回滚日志进行反向补偿。    

2. AT全局锁与读写分离  


##### 1.3.6.2.2. AT缺点



## 1.4. 分布式锁
### 1.4.1. 分布式锁介绍
1. 分布式锁使用场景（什么是分布式锁？）：
    1. 避免不同节点重复相同的工作，实现幂等。  
    2. 避免破坏数据的正确性：在分布式环境下解决多实例对数据的访问一致性。如果多个节点在同一条数据上同时进行操作，可能会造成数据错误或不一致的情况出现。  
2. 实现分布式锁的细节（特点）：  
    * **<font color = "blue">确保互斥：在同一时刻，必须保证锁至多只能被一个客户端持有。</font>**  
    * **<font color = "clime">`不能死锁：`在一个客户端在持有锁的期间崩溃而没有主动解锁情况下，`也能保证后续其他客户端能加锁`。</font>**    
    * **<font color = "clime">`避免活锁：`在获取锁失败的情况下，`反复进行重试操作，占用CPU资源，影响性能。`</font>**    
    * 实现更多锁特性：锁中断、锁重入、锁超时等。确保客户端只能解锁自己持有的锁。  
3. 分布式锁选型（各种锁的对比）：  
&emsp; ~~CAP只能满足其二、数据一致性、性能。~~  
&emsp; 分布式中间件的选型无非就是AP、CP模型（CAP原理）中的一种。针对常用的分布式锁，redis是AP模型、zookeeper是CP模型。  
&emsp; ★★★具体选择哪一种看使用场景对数据一致性的要求。`解决幂等时，一般情况下使用redis分布式锁，但是分布式锁编码问题、中间件部署问题都有可能影响分布式锁的使用。所以数据一致性要求高的，要结合数据库乐观锁（状态控制）。`    

### 1.4.2. Redis分布锁
#### 1.4.2.1. RedisLock
1. Redis分布式锁`设计`：    
    * 如何避免死锁？设置锁过期时间。  
    * `锁过期。 增大冗余时间。`  
    * 释放别人的锁。唯一标识，例如UUID。  
2. Redis分布式锁`实现方案`：  
    * 方案一：SETNX + EXPIRE  
    * 方案二：SETNX + value值是（系统时间+过时时间）  
    * `方案三：使用Lua脚本(包含SETNX + EXPIRE两条指令) ` 
    * 方案四：SET的扩展命令（SET EX PX NX）  
    * `方案五：SET EX PX NX + 校验惟一随机值，再删除释放`  
    * `方案六: 开源框架: Redisson（通过lua脚本实现）`  
    * `方案七：多机实现的分布式锁Redlock`   
3. `方案五：SET EX PX NX + 校验惟一随机值，再删除释放（先查询再删除，非原子操作，需要使用lua脚本保证其原子性）。`  
&emsp; 在这里，判断是否是当前线程加的锁和释放锁不是一个原子操做。若是调用jedis.del()释放锁的时候，可能这把锁已经不属于当前客户端，会解除他人加的锁。  
![image](http://182.92.69.8:8081/img/microService/problems/problem-61.png)  
&emsp; 为了更严谨，通常也是用lua脚本代替。lua脚本以下：  
    ```text
    if redis.call('get',KEYS[1]) == ARGV[1] then 
    return redis.call('del',KEYS[1]) 
    else
    return 0
    end;
    ```
4. RedLock红锁：  
    1. RedLock：当前线程尝试给每个Master节点`顺序`加锁。要在多数节点上加锁，并且加锁时间小于超时时间，则加锁成功；加锁失败时，依次删除节点上的锁。  
    2. ~~RedLock“顺序加锁”：确保互斥。在同一时刻，必须保证锁至多只能被一个客户端持有。~~   
5. Redis分布式锁`缺点`：   
&emsp; 采用Master-Slave模式，加锁的时候只对一个节点加锁，即使通过Sentinel做了高可用，但是<font color="clime">如果Master节点故障了，发生主从切换，此时就会有可能出现`锁丢失`的问题，`可能导致多个客户端同时完成加锁`</font>。  
&emsp; `在解决接口幂等问题中，采用redis分布式锁可以。但是如果涉及支付等对数据一致性要求严格的场景，需要结合数据库锁，建议使用基于状态机的乐观锁。`  


#### 1.4.2.2. 使用redis分布式锁的注意点
1. 使用redis分布式锁要注意的问题：  
    1. 采用Master-Slave模式，加锁的时候只对一个节点加锁，即使通过Sentinel做了高可用，但是<font color="clime">如果Master节点故障了，发生主从切换，此时就会有可能出现`锁丢失`的问题，`可能导致多个客户端同时完成加锁`</font>。  
    &emsp; `在解决接口幂等问题中，采用redis分布式锁可以。但是如果涉及支付等对数据一致性要求严格的场景，需要结合数据库锁，建议使用基于状态机的乐观锁。`  
    2. 编码问题：  
        * key：释放别人的锁。唯一标识，例如UUID。  
        * 如何避免死锁？设置锁过期时间。  
        * `锁过期。 增大冗余时间。`  

![image](http://182.92.69.8:8081/img/microService/problems/problem-69.png)  

&emsp; 使用redis分布式锁的注意点：  

1. 非原子操作  
2. 忘了释放锁  
3. 释放了别人的锁  
4. `大量失败请求`  
&emsp; 如果有1万的请求同时去竞争那把锁，可能只有一个请求是成功的，其余的9999个请求都会失败。  
&emsp; 在秒杀场景下，会有什么问题？  
&emsp; 答：每1万个请求，有1个成功。再1万个请求，有1个成功。如此下去，直到库存不足。这就变成均匀分布的秒杀了，跟我们想象中的不一样。  
&emsp; `使用自旋锁。`  
5. 锁重入问题  
&emsp; 递归调用，使用可重入锁。 
6. 锁竞争问题  
&emsp; 如果有大量需要写入数据的业务场景，使用普通的redis分布式锁是没有问题的。  
&emsp; 但如果有些业务场景，写入的操作比较少，反而有大量读取的操作。这样直接使用普通的redis分布式锁，会不会有点浪费性能？  
&emsp; 我们都知道，锁的粒度越粗，多个线程抢锁时竞争就越激烈，造成多个线程锁等待的时间也就越长，性能也就越差。   
&emsp; 所以，提升redis分布式锁性能的第一步，就是要把锁的粒度变细。  
&emsp; 可以：1. `读写锁；`2. `锁分段`。
7. 锁超时问题  
&emsp; 自动续期  
8. 主从复制问题  
9. ~~正确使用分布式锁~~ 

#### 1.4.2.3. Redisson
1.  **<font color = "clime">RedissonLock解决客户端死锁问题（自动延期）：</font>**  
    1. 什么是死锁？因为业务不知道要执行多久才能结束，所以这个key一般不会设置过期时间。这样如果在执行业务的过程中，业务机器宕机，unlock操作不会执行，所以这个锁不会被释放，`其他机器拿不到锁，从而形成了死锁。`  
    2. ~~Redission解决死锁：(**要点：30s和10s**)~~
        1. `未设置加锁时间，自动设置加锁时间：`当业务方调用加锁操作的时候，`未设置加锁时间`，默认的leaseTime是-1，所以会取watch dog的时间作为锁的持有时间，默认是30s，这个时候即使`发生了宕机现象`，因为这个锁不是永不过期的，所以30s后就会释放，不会产生死锁。 
        2. `异步续租、递归调用：`另一方面，它还能解决`当锁内逻辑超过30s的时候锁会失效`的问题，因为当leaseTime是-1的时候，`客户端会启动一个异步任务（watch dog）`，会每隔10秒检查一下，如果客户端1还持有锁key，在业务方释放锁之前，会一直不停的增加这个锁的生命周期时间，保证在业务执行完毕之前，这个锁一直不会因为redis的超时而被释放。
2. Redisson实现了多种锁：重入锁、公平锁、联锁、红锁、读写锁、信号量Semaphore 和 CountDownLatch...  
3. **Redisson重入锁：**  
    1. Redisson重入锁加锁流程：  
        1. 执行lock.lock()代码时，<font color = "red">如果该客户端面对的是一个redis cluster集群，首先会根据hash节点选择一台机器。</font>  
        2. 然后发送一段`lua脚本`，带有三个参数：一个是锁的名字(在代码里指定的)、一个是锁的时常(默认30秒)、一个是加锁的客户端id(每个客户端对应一个id)。<font color = "red">然后脚本会判断是否有该名字的锁，如果没有就往数据结构中加入该锁的客户端id。</font>  

            * 锁不存在(exists)，则加锁(hset)，并设置(pexpire)锁的过期时间；  
            * 锁存在，检测(hexists)是当前线程持有锁，锁重入(hincrby)，并且重新设置(pexpire)该锁的有效时间；
            * 锁存在，但不是当前线程的，返回(pttl)锁的过期时间。 
    2. **<font color = "red">Redisson重入锁缺陷：</font>** 在哨兵模式或者主从模式下，如果master实例宕机的时候，可能导致多个客户端同时完成加锁。  


### 1.4.3. ZK分布式锁
1. **<font color = "clime">对于ZK来说，实现分布式锁的核心是临时顺序节点和监听机制。</font>** ZK实现分布式锁要注意[羊群效应](/docs/microService/dubbo/ZookeeperProblem.md)    
2. **ZooKeeper分布式锁的缺点：** 1). 需要依赖zookeeper；2). 性能低。频繁地“写”zookeeper。集群节点数越多，同步越慢，获取锁的过程越慢。  
3. 基于ZooKeeper可以实现分布式的独占锁和读写锁。  
    1. **使用ZK实现分布式独占锁：**<font color="red">在某一节点下，建立临时顺序节点。最小节点获取到锁。非最小节点监听上一节点，上一节点释放锁，唤醒当前节点。</font>  
    2. **~~使用ZK实现分布式读写锁：~~<font color = "red">客户端从 Zookeeper 端获取 /share_lock下所有的子节点，并判断自己能否获取锁。</font>**  
        1. **<font color = "red">如果客户端创建的是读锁节点，获取锁的条件（满足其中一个即可）如下：</font>**  

            * 自己创建的节点序号排在所有其他子节点前面  
            * 自己创建的节点前面无写锁节点  
            
        2. **<font color = "red">如果客户端创建的是写锁节点，</font>** 由于写锁具有排他性，所以获取锁的条件要简单一些，只需确定自己创建的锁节点是否排在其他子节点前面即可。  
4. ~~ZK的羊群效应~~  


### 1.4.4. MySql分布式锁
1. 基于表记录实现
2. 基于排他锁/悲观锁(for update)实现
3. 乐观锁实现
	* 一般是通过为数据库表添加一个version字段来实现读取出数据时，将此版本号一同读出。
	* 基于状态机的乐观锁


### 1.4.5. 分布式锁选型（各种锁的对比）   
&emsp; ~~CAP只能满足其二、数据一致性、性能。~~  
&emsp; 分布式中间件的选型无非就是AP、CP模型（CAP原理）中的一种。针对常用的分布式锁，redis是AP模型、zookeeper是CP模型。  
&emsp; 具体选择哪一种看使用场景对数据一致性的要求。`解决幂等时一般情况下，使用redis分布式锁，但是分布式锁编码问题、中间件部署问题都有可能影响分布式锁的使用。所以数据一致性要求高的，要结合数据库乐观锁（状态控制）。` 

