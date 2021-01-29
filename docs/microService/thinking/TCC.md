
<!-- TOC -->

- [1. TCC模式-强一致性](#1-tcc模式-强一致性)
    - [1.1. 实现流程](#11-实现流程)
    - [1.2. 特点](#12-特点)
    - [1.3. TCC 的注意点](#13-tcc-的注意点)

<!-- /TOC -->

# 1. TCC模式-强一致性 
&emsp; 不管是 2PC 还是 3PC 都是依赖于数据库的事务提交和回滚。  
&emsp; 而有时候一些业务它不仅仅涉及到数据库，可能是发送一条短信，也可能是上传一张图片。  
&emsp; 所以说事务的提交和回滚就得提升到业务层面而不是数据库层面了，而TCC就是一种业务层面或者是应用层的两阶段提交。  

&emsp; TCC是Try、Commit、Cancel的缩写。也就是业务层面需要写对应的三个方法，主要用于跨数据库、跨服务的业务操作的数据一致性问题。    
&emsp; TTCC是两阶段型、补偿型的事务。TCC采用的补偿机制，其逻辑模式类似于XA两阶段提交。其核心思想是：<font color = "red">针对每个操作，都要注册一个与其对应的确认和补偿(撤销)操作。</font>TCC模型是把锁的粒度完全交给业务处理。业务实现TCC服务之后，该TCC服务将作为分布式事务的其中一个资源，参与到整个分布式事务中；<font color = "lime">事务管理器分两阶段协调的TCC服务，第一阶段调用所有TCC服务的Try方法，在第二阶段执行所有TCC服务的Confirm或者Cancel方法。</font>  

## 1.1. 实现流程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-9.png)  
* 条件：  

        需要实现确认和补偿逻辑
        需要支持幂等

1. Try阶段主要是对业务系统做检测及资源锁定或者预留。这个阶段主要完成：  
    1. 完成所有业务检查( 一致性 ) 。  
    2. 预留必须业务资源( 准隔离性 ) 。  
    3. Try尝试执行业务。
2. Confirm(确认)阶段主要是对业务系统做确认提交，不做业务检查。Try阶段执行成功并开始执行Confirm阶段时，默认 Confirm阶段是不会出错的。即：只要Try成功，Confirm一定成功。  
3. Cancel(取消)阶段主要是在业务执行错误，需要回滚的状态下，执行的业务取消，预留资源释放。  

&emsp; 其实从思想上看和2PC差不多，都是先试探性的执行，如果都可以那就真正的执行，如果不行就回滚。  
&emsp; 比如说一个事务要执行A、B、C三个操作，那么先对三个操作执行预留动作。如果都预留成功了那么就执行确认操作，如果有一个预留失败那就都执行撤销动作。  
&emsp; TCC模型中还有个事务管理者的角色，用来记录TCC全局事务状态并提交或者回滚事务。  

&emsp; 示例：TCC模式下，A账户往B账户汇款100元为例子，对业务的改造进行详细的分析：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-40.png)  
&emsp; 汇款服务和收款服务分别需要实现，Try-Confirm-Cancel接口，并在业务初始化阶段将其注入到TCC事务管理器中。  

```text
[汇款服务]
Try：
    检查A账户有效性，即查看A账户的状态是否为“转帐中”或者“冻结”；
    检查A账户余额是否充足；
    从A账户中扣减100元，并将状态置为“转账中”；
    预留扣减资源，将从A往B账户转账100元这个事件存入消息或者日志中；
Confirm：
    不做任何操作；
Cancel：
    A账户增加100元；
    从日志或者消息中，释放扣减资源。
[收款服务]
Try：
    检查B账户账户是否有效；
Confirm：
    读取日志或者消息，B账户增加100元；
    从日志或者消息中，释放扣减资源；
Cancel：
    不做任何操作。
```

## 1.2. 特点  
* 优点：  
    * 性能提升：具体业务来实现控制资源锁的粒度变小，不会锁定整个资源。  
    * **<font color = "lime">数据最终一致性：基于Confirm和Cancel的幂等性，保证事务最终完成确认或者取消，保证数据的一致性。</font>**  
    * 可靠性：解决了XA协议的协调者单点故障问题，由主业务方发起并控制整个业务活动，业务活动管理器也变成多点，引入集群。  
* 缺点：  
    * TCC的Try、Confirm和Cancel操作功能要按具体业务来实现。  

## 1.3. TCC 的注意点  

* 幂等问题，因为网络调用无法保证请求一定能到达，所以都会有重调机制，因此对于Try、Confirm、Cancel三个方法都需要幂等实现，避免重复执行产生错误。  
* 空回滚问题，指的是Try方法由于网络问题没收到超时了，此时事务管理器就会发出Cancel命令，那么需要支持Cancel在未执行Try的情况下能正常的Cancel。  
* 悬挂问题，这个问题也是指 Try 方法由于网络阻塞超时触发了事务管理器发出了Cancel命令，但是执行了Cancel命令之后Try请求到了。  
&emsp; 已经执行Cancel 了，又来个 Try请求，对于事务管理器来说这时候事务已经是结束了的，这冻结操作就被“悬挂”了，所以空回滚之后还得记录一下，防止 Try 的再调用。