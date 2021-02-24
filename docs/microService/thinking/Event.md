

<!-- TOC -->

- [1. ~~Saga~~](#1-saga)
    - [1.1. Saga简介](#11-saga简介)
    - [1.2. Saga组成](#12-saga组成)
    - [1.3. Saga相关实现](#13-saga相关实现)
    - [1.4. Saga协调](#14-saga协调)
        - [1.4.1. 编排（Choreography）](#141-编排choreography)
            - [1.4.1.1. 实现流程](#1411-实现流程)
            - [1.4.1.2. 特点](#1412-特点)
        - [1.4.2. 控制（Orchestration）](#142-控制orchestration)
            - [1.4.2.1. 实现流程](#1421-实现流程)
                - [1.4.2.1.1. 使用状态机建模SAGA ORCHESTRATORS](#14211-使用状态机建模saga-orchestrators)
                - [1.4.2.1.2. SAGA控制和事务消息](#14212-saga控制和事务消息)
            - [1.4.2.2. 特点](#1422-特点)
    - [1.5. Saga的使用条件](#15-saga的使用条件)

<!-- /TOC -->



# 1. ~~Saga~~
<!--
http://servicecomb.apache.org/cn/docs/distributed-transactions-saga-implementation/
https://zhuanlan.zhihu.com/p/95852045

Saga的核心就是补偿，一阶段就是服务的正常顺序调用（数据库事务正常提交），如果都执行成功，则第二阶段则什么都不做；但如果其中有执行发生异常，则依次调用其补偿服务（一般多逆序调用未已执行服务的反交易）来保证整个交易的一致性。应用实施成本一般。
TCC的特点在于业务资源检查与加锁，一阶段进行校验，资源锁定，如果第一阶段都成功，二阶段对锁定资源进行交易逻辑，否则，对锁定资源进行释放。应用实施成本较高。
基于可靠消息最终一致，一阶段服务正常调用，同时同事务记录消息表，二阶段则进行消息的投递，消费。应用实施成本较低

&emsp; Saga模式是一种分布式异步事务，一种最终一致性事务，是一种柔性事务。  
-->

<!--
~~
https://www.jianshu.com/p/e4b662407c66?from=timeline&isappinstalled=0
https://mp.weixin.qq.com/s/HDSWK2eCOtusroV3Elv1jA
~~
-->

## 1.1. Saga简介
&emsp; Saga模型起源于1987年Hector Garcia-Molina，Kenneth Salem发表的论文《Sagas》，是分布式事务相关概念最早出现的。  
&emsp; <font color = "red">Saga是一个长活事务可被分解成可以交错运行的子事务集合。其中每个子事务都是一个保持数据库一致性的真实事务。</font>  

    长事务(Long-Lived Transactions)，顾名思义，就是执行时间较长的事务。  

&emsp; <font color = "lime">Saga模型可以将一个分布式事务拆分为多个本地事务，每个本地事务都有相应的执行模块和补偿模块(对应TCC中的Confirm和Cancel)，当Saga事务中任意一个本地事务出错时，可以通过调用相关的补偿方法恢复之前的事务，达到事务最终一致性。</font>  
&emsp; **<font color = "red">注意：saga也是一种二阶段补偿性协议，每个子事务(本地事务)依次执行提交阶段。如果都提交成功，也就不会有第二阶段的产生。</font>**  

&emsp; ~~Saga的实现有很多种方式，其中最流行的两种方式是：~~  

* 基于事件的方式。这种方式没有协调中心，整个模式的工作方式就像舞蹈一样，各个舞蹈演员按照预先编排的动作和走位各自表演，最终形成一只舞蹈。处于当前Saga下的各个服务，会产生某类事件，或者监听其它服务产生的事件并决定是否需要针对监听到的事件做出响应。 
* 基于命令的方式。这种方式的工作形式就像一只乐队，由一个指挥家（协调中心）来协调大家的工作。协调中心来告诉Saga的参与方应该执行哪一个本地事务。  

## 1.2. Saga组成
&emsp; **Saga的组成：**  

* 每个Saga由一系列sub-transaction Ti组成  
* 每个Ti都有对应的补偿动作Ci，补偿动作用于撤销Ti造成的结果  

&emsp; 可以看到，<font color = "red">和TCC相比，Saga没有“预留”动作，它的Ti就是直接提交到库。</font>  

&emsp; **Saga的执行顺序有两种：**  

* T1, T2, T3, ..., Tn  
* T1, T2, ..., Tj, Cj,..., C2, C1，其中0 < j < n  

&emsp; **数据隔离性：**  

* 业务层控制并发
* 在应用层加锁
* 应用层预先冻结资源等

&emsp; **<font color = "clime">Saga定义了两种恢复策略：** </font>** 

* <font color = "red">backward recovery，向后恢复，补偿所有已完成的事务，如果任一子事务失败。</font>即上面提到的第二种执行顺序，其中j是发生错误的sub-transaction，这种做法的效果是撤销掉之前所有成功的sub-transation，使得整个Saga的执行结果撤销。  
* <font color = "red">forward recovery，向前恢复，重试失败的事务，假设每个子事务最终都会成功。</font><font color = "clime">适用于必须要成功的场景。</font> 执行顺序是类似于这样的：T1, T2, ..., Tj(失败), Tj(重试),..., Tn，其中j是发生错误的sub-transaction。该情况下不需要Ci。  

    &emsp; 显然，向前恢复没有必要提供补偿事务，如果业务中，子事务（最终）总会成功，或补偿事务难以定义或不可能，向前恢复更符合你的需求。  
    &emsp; 理论上补偿事务永不失败，然而，在分布式世界中，服务器可能会宕机，网络可能会失败，甚至数据中心也可能会停电。在这种情况下我们能做些什么？ 最后的手段是提供回退措施，比如人工干预。  

## 1.3. Saga相关实现
**Saga Log**  
&emsp; Saga保证所有的子事务都得以完成或补偿，但Saga系统本身也可能会崩溃。Saga崩溃时可能处于以下几个状态：  

* Saga收到事务请求，但尚未开始。因子事务对应的微服务状态未被Saga修改，什么也不需要做。
* 一些子事务已经完成。重启后，Saga必须接着上次完成的事务恢复。
* 子事务已开始，但尚未完成。由于远程服务可能已完成事务，也可能事务失败，甚至服务请求超时，saga只能重新发起之前未确认完成的子事务。这意味着子事务必须幂等。
* 子事务失败，其补偿事务尚未开始。Saga必须在重启后执行对应补偿事务。
* 补偿事务已开始但尚未完成。解决方案与上一个相同。这意味着补偿事务也必须是幂等的。
* 所有子事务或补偿事务均已完成，与第一种情况相同。  

&emsp; 为了恢复到上述状态，必须追踪子事务及补偿事务的每一步。可以通过事件的方式达到以上要求，并将以下事件保存在名为saga log的持久存储中：

* Saga started event 保存整个saga请求，其中包括多个事务/补偿请求
* Transaction started event 保存对应事务请求
* Transaction ended event 保存对应事务请求及其回复
* Transaction aborted event 保存对应事务请求和失败的原因
* Transaction compensated event 保存对应补偿请求及其回复
* Saga ended event 标志着saga事务请求的结束，不需要保存任何内容

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-43.png)  

&emsp; <font color = "red">通过将这些事件持久化在saga log中，可以将saga恢复到上述任何状态。</font>  

&emsp; 由于Saga只需要做事件的持久化，而事件内容以JSON的形式存储，Saga log的实现非常灵活，数据库（SQL或NoSQL），持久消息队列，甚至普通文件可以用作事件存储， 当然有些能更快得帮saga恢复状态。  

**注意事项**  
&emsp; 对于服务来说，实现Saga有以下这些要求：  
1. Ti和Ci是幂等的。
2. Ci必须是能够成功的，如果无法成功则需要人工介入。
3. Ti - Ci和Ci - Ti的执行结果必须是一样的：sub-transaction被撤销了。

&emsp; 第一点要求Ti和Ci是幂等的，举个例子，假设在执行Ti的时候超时了，此时是不知道执行结果的，如果采用forward recovery策略就会再次发送Ti，那么就有可能出现Ti被执行了两次，所以要求Ti幂等。如果采用backward recovery策略就会发送Ci，而如果Ci也超时了，就会尝试再次发送Ci，那么就有可能出现Ci被执行两次，所以要求Ci幂等。  
&emsp; 第二点要求Ci必须能够成功，这个很好理解，因为，如果Ci不能执行成功就意味着整个Saga无法完全撤销，这个是不允许的。但总会出现一些特殊情况比如Ci的代码有bug、服务长时间崩溃等，这个时候就需要人工介入了。  
&emsp; 第三点乍看起来比较奇怪，举例说明，还是考虑Ti执行超时的场景，采用了backward recovery，发送一个Ci，那么就会有三种情况：  

* Ti的请求丢失了，服务之前没有、之后也不会执行Ti
* Ti在Ci之前执行
* Ci在Ti之前执行

&emsp; 对于第1种情况，容易处理。对于第2、3种情况，则要求Ti和Ci是可交换的（commutative)，并且其最终结果都是sub-transaction被撤销。  

## 1.4. Saga协调  
&emsp; 协调saga：saga的实现包含协调saga步骤的逻辑。当系统命令启动saga时，协调逻辑必须选择并告知第一个saga参与者执行本地事务。一旦该事务完成，saga的排序协调选择并调用下一个saga参与者。这个过程一直持续到saga执行了所有步骤。如果任何本地事务失败，则saga必须以相反的顺序执行补偿事务。构建一个saga的协调逻辑有几种不同的方法：

* 编排（Choreography）：在saga参与者中分配决策和排序。saga参与者主要通过交换事件进行沟通。
* 控制（Orchestration）：在saga控制类中集中saga的协调逻辑。一个saga控制者向saga参与者发送命令消息，告诉sage参与者要执行哪些操作。

### 1.4.1. 编排（Choreography）
&emsp; <font color = "red">编排模式没有中央协调器（没有单点风险）时，每个服务产生并聆听其他服务的事件，并决定是否应采取行动。</font>  
&emsp; 该实现第一个服务执行一个事务，然后发布一个事件。该事件被一个或多个服务进行监听，这些服务再执行本地事务并发布（或不发布）新的事件，当最后一个服务执行本地事务并且不发布任何事件时，意味着分布式事务结束，或者它发布的事件没有被任何Saga参与者听到都意味着事务结束。 

#### 1.4.1.1. 实现流程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-30.png)  
1. 订单服务保存新订单，将状态设置为pengding挂起状态，并发布名为ORDER_CREATED_EVENT的事件。
2. 支付服务监听ORDER_CREATED_EVENT，并公布事件BILLED_ORDER_EVENT。
3. 库存服务监听BILLED_ORDER_EVENT，更新库存，并发布ORDER_PREPARED_EVENT。
4. 货运服务监听ORDER_PREPARED_EVENT，然后交付产品。最后，它发布ORDER_DELIVERED_EVENT。
5. 最后，订单服务侦听ORDER_DELIVERED_EVENT并设置订单的状态为concluded完成。

&emsp; 假设库存服务在事务过程中失败了。进行回滚：
1. 库存服务产生PRODUCT_OUT_OF_STOCK_EVENT
2. 订购服务和支付服务会监听到上面库存服务的这一事件：
    1. 支付服务会退款给客户。
    2. 订单服务将订单状态设置为失败。  

#### 1.4.1.2. 特点  
* 优点：事件/编排是实现Saga模式的自然方式; 它很简单，容易理解，不需要太多的努力来构建，所有参与者都是松散耦合的，因为它们彼此之间没有直接的耦合。如果事务涉及2至4个步骤，则可能是非常合适的。  

### 1.4.2. 控制（Orchestration）  
&emsp; 中央协调器负责集中处理事件的决策和业务逻辑排序。  
&emsp; saga协调器orchestrator以命令/回复的方式与每项服务进行通信，告诉服务应该执行哪些操作。  


#### 1.4.2.1. 实现流程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-31.png)  
1. 订单服务保存pending状态，并要求订单Saga协调器（简称OSO）开始启动订单事务。
2. OSO向收款服务发送执行收款命令，收款服务回复Payment Executed消息。
3. OSO向库存服务发送准备订单命令，库存服务将回复OrderPrepared消息。
4. OSO向货运服务发送订单发货命令，货运服务将回复Order Delivered消息。

&emsp; OSO订单Saga协调器必须事先知道执行“创建订单”事务所需的流程(通过读取BPM业务流程XML配置获得)。如果有任何失败，它还负责通过向每个参与者发送命令来撤销之前的操作来协调分布式的回滚。当有一个中央协调器协调一切时，回滚要容易得多，因为协调器默认是执行正向流程，回滚时只要执行反向流程即可。  

##### 1.4.2.1.1. 使用状态机建模SAGA ORCHESTRATORS  
&emsp; 建模saga orchestrator的好方法是作为状态机。状态机由一组状态和一组由事件触发的状态之间的转换组成。每个transition都可以有一个action，对于一个saga来说是一个saga参与者的调用。状态之间的转换由saga参与者执行的本地事务的完成触发。当前状态和本地事务的特定结果决定了状态转换以及执行的操作（如果有的话）。对状态机也有有效的测试策略。因此，使用状态机模型可以更轻松地设计、实施和测试。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-44.png)  

&emsp; 图显示了Create Order Saga的状态机模型。此状态机由多个状态组成，包括以下内容：

* Verifying Consumer：初始状态。当处于此状态时，该saga正在等待消费者服务部门验证消费者是否可以下订单。  
* Creating Ticket：该saga正在等待对创建票证命令的回复。  
* Authorizing Card：等待Accounting服务授权消费者的信用卡。  
* OrderApproved：表示saga成功完成的最终状态。  
* Order Rejected：最终状态表明该订单被其中一方参与者们拒绝。  

##### 1.4.2.1.2. SAGA控制和事务消息
&emsp; 基于业务流程的saga的每个步骤都包括更新数据库和发布消息的服务。例如，Order Service持久保存Order和Create Order Saga orchestrator，并向第一个saga参与者发送消息。一个saga参与者，例如Kitchen Service，通过更新其数据库并发送回复消息来处理命令消息。 Order Service通过更新saga协调器的状态并向下一个saga参与者发送命令消息来处理参与者的回复消息。服务必须使用事务性消息传递，以便自动更新数据库并发布消息。  


#### 1.4.2.2. 特点  
* 优点：
    * 避免服务之间的循环依赖关系，因为saga协调器会调用saga参与者，但参与者不会调用协调器。
    * 集中分布式事务的编排。
    * 只需要执行命令/回复(其实回复消息也是一种事件消息)，降低参与者的复杂性。
    * 在添加新步骤时，事务复杂性保持线性，回滚更容易管理。
    * 如果在第一笔交易还没有执行完，想改变有第二笔事务的目标对象，则可以轻松地将其暂停在协调器上，直到第一笔交易结束。
* 缺点：协调器中集中太多逻辑的风险。  

## 1.5. Saga的使用条件  
&emsp; 所有长活事务都可以使用Saga吗？这里有一些限制：  

* Saga只允许两个层次的嵌套，顶级的Saga和简单子事务
* 在外层，全原子性不能得到满足。也就是说，sagas可能会看到其他sagas的部分结果
* 每个子事务应该是独立的原子行为
* 在业务场景下，各个业务环境（如：航班预订、租车、酒店预订和付款）是自然独立的行为，而且每个事务都可以用对应服务的数据库保证原子操作。

&emsp; 补偿也有需考虑的事项：

* 补偿事务从语义角度撤消了事务Ti的行为，但未必能将数据库返回到执行Ti时的状态。（例如，如果事务触发导弹发射， 则可能无法撤消此操作）

&emsp; 但这对业务来说不是问题。其实难以撤消的行为也有可能被补偿。例如，发送电邮的事务可以通过发送解释问题的另一封电邮来补偿。  

&emsp; **对于ACID的保证：**    
&emsp; 从Saga模型的上述定义中，Saga 模型可以满足事务的三个特性：  

* 原子性：Saga 协调器协调事务链中的本地事务要么全部提交，要么全部回滚。
* 一致性：Saga 事务可以实现最终一致性。
* 持久性：基于本地事务，所以这个特性可以很好实现。

&emsp; 从数据隔离性上分析，可以发现Saga模型无法保证外部的原子性和隔离性，因为可以查看其他sagas的部分结果，论文中有对应的表述：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-42.png)  

&emsp; **<font color = "lime">注意事项：</font>**   
&emsp; <font color = "red">Saga 事务和 TCC 事务一样，都是强依靠业务改造，所以要求业务方在设计上要遵循三个策略：</font>

* 允许空补偿：网络异常导致事务的参与方只收到了补偿操作指令，因为没有执行过正常操作，因此要进行空补偿。  
* 保持幂等性：事务的正向操作和补偿操作都可能被重复触发，因此要保证操作的幂等性。  
* 防止资源悬挂：原因是网络异常导致事务的正向操作指令晚于补偿操作指令到达，则要丢弃本次正常操作，否则会出现资源悬挂问题。  

&emsp; **<font color = "lime">Saga和TCC对比：</font>**  
&emsp; 虽然 Saga 和 TCC 都是补偿事务，但是由于提交阶段不同，所以两者也是有不同的：  

* <font color = "red">Saga 没有Try行为，直接Commit，所以会留下原始事务操作的痕迹，Cancel属于不完美补偿，需要考虑对业务上的影响。</font>TCC Cancel是完美补偿的Rollback，补偿操作会彻底清理之前的原始事务操作，用户是感知不到事务取消之前的状态信息的。
* Saga 的补偿操作通常可以异步执行，TCC的Cancel和Confirm可以根据需要是否异步化。
* Saga 对业务侵入较小，只需要提供一个逆向操作的Cancel即可；而TCC需要对业务进行全局性的流程改造。
* TCC最少通信次数为2n，而Saga为n（n=子事务的数量）。 


