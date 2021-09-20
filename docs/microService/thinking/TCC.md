
<!-- TOC -->

- [1. xxxTCC模式-强一致性xxx](#1-xxxtcc模式-强一致性xxx)
    - [1.1. ~~实现流程~~](#11-实现流程)
        - [实现流程一](#实现流程一)
        - [★★★实现流程二](#★★★实现流程二)
        - [实现流程三](#实现流程三)
    - [1.2. 特点](#12-特点)
    - [1.3. TCC与二阶段比较](#13-tcc与二阶段比较)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "red">TCC是一种`业务层面或者是应用层`的`两阶段、补偿型`的事务。</font>**  
&emsp; TCC是`Try（检测及资源锁定或者预留）`、Commit（确认）、Cancel（取消）的缩写，业务层面需要写对应的三个方法。  
2. TCC与二阶段比较  
&emsp; 使用2PC机制时————以提交为例————一个完整的事务生命周期是：begin -> 业务逻辑 -> prepare -> commit。  
&emsp; 使用TCC机制时————以提交为例————一个完整的事务生命周期是：begin -> 业务逻辑(try业务) -> commit(comfirm业务)。  
&emsp; 综上，可以从执行的阶段上将二者一一对应起来：  
&emsp; 1、2PC机制的业务阶段 等价于 TCC机制的try业务阶段；  
&emsp; 2、2PC机制的提交阶段（prepare & commit） 等价于 TCC机制的提交阶段（confirm）；  
&emsp; 3、2PC机制的回滚阶段（rollback） 等价于 TCC机制的回滚阶段（cancel）。  
&emsp; 因此，可以看出，虽然TCC机制中有两个阶段都存在业务逻辑的执行，但其中 `try业务阶段其实是与全局事务处理无关的`。认清了这一点，当再比较TCC和2PC时，就会很容易地发现，`TCC不是两阶段提交，而只是它对事务的提交/回滚是通过执行一段confirm/cancel业务逻辑来实现，仅此而已。`  


# 1. xxxTCC模式-强一致性xxx 
<!-- 
分布式事务 Seata TCC 模式深度解析 | SOFAChannel#4 直播整理 
https://www.sofastack.tech/blog/sofa-channel-4-retrospect/

-->
&emsp; 不管是 2PC 还是 3PC 都是依赖于数据库的事务提交和回滚。  
&emsp; 而有时候一些业务它不仅仅涉及到数据库，可能是发送一条短信，也可能是上传一张图片。所以说事务的提交和回滚就得提升到业务层面而不是数据库层面了，  **<font color = "red">而TCC是一种`业务层面或者是应用层`的`两阶段、补偿型`的事务。</font>**   

&emsp; TCC是Try（检测及资源锁定或者预留）、Commit（确认）、Cancel（取消）的缩写，业务层面需要写对应的三个方法。主要用于跨数据库、跨服务的业务操作的数据一致性问题。    

## 1.1. ~~实现流程~~  
<!-- 
*** https://www.cnblogs.com/jajian/p/10014145.html
https://www.sofastack.tech/blog/sofa-channel-4-retrospect/
https://www.cnblogs.com/rjzheng/p/10164667.html
-->

### 实现流程一
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-9.png)  
&emsp; TCC是两阶段型、补偿型的事务。TCC采用的补偿机制，其逻辑模式类似于XA两阶段提交。其核心思想是：<font color = "red">针对每个操作，都要注册一个与其对应的确认和补偿(撤销)操作。</font>TCC模型是把锁的粒度完全交给业务处理。业务实现TCC服务之后，该TCC服务将作为分布式事务的其中一个资源，参与到整个分布式事务中；<font color = "clime">事务管理器分两阶段协调的TCC服务，第一阶段调用所有TCC服务的Try方法，在第二阶段执行所有TCC服务的Confirm或者Cancel方法。</font>  
&emsp; TCC模型中有个事务管理者的角色，用来记录TCC全局事务状态并提交或者回滚事务。  

1. Try阶段主要是对业务系统做 **<font color = "red">检测及资源锁定或者预留。</font>** 这个阶段主要完成：  
    1. 完成所有业务检查(一致性)。  
    2. 预留必须业务资源(准隔离性)。  
    3. Try尝试执行业务。
2. Confirm(确认)阶段主要是对业务系统做确认提交，不做业务检查。Try阶段执行成功并开始执行Confirm阶段时，默认Confirm阶段是不会出错的。即：只要Try成功，Confirm一定成功。  
3. Cancel(取消)阶段主要是在业务执行错误，需要回滚的状态下，执行的业务取消，预留资源释放。  

&emsp; 其实从思想上看和2PC差不多，都是先试探性的执行，如果都可以，那就真正的执行；如果不行，就回滚。比如说一个事务要执行A、B、C三个操作，那么先对三个操作执行预留动作。如果都预留成功了那么就执行确认操作，如果有一个预留失败那就都执行撤销动作。  

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

### ★★★实现流程二
<!-- 
https://www.cnblogs.com/jajian/p/10014145.html
-->

1. try阶段：
    * 支付服务：在 pay() 那个方法里，你别直接把订单状态修改为已支付啊！你先把订单状态修改为 UPDATING，也就是修改中的意思。  
    * 库存服务：库存进行冻结。  




### 实现流程三



## 1.2. 特点  
* 优点：  
    * 性能提升：具体业务来实现控制资源锁的粒度变小，不会锁定整个资源。  
    * **<font color = "clime">数据最终一致性：基于Confirm和Cancel的幂等性，保证事务最终完成确认或者取消，保证数据的一致性。</font>**  
    * 可靠性：解决了XA协议的协调者单点故障问题，由主业务方发起并控制整个业务活动，业务活动管理器也变成多点，引入集群。  
* 缺点：  
    * TCC的Try、Confirm和Cancel操作功能要按具体业务来实现。  


## 1.3. TCC与二阶段比较  
<!-- 
https://blog.csdn.net/Saintyyu/article/details/100862449
-->

&emsp; 当讨论2PC时，我们只专注于事务处理阶段，因而只讨论prepare和commit，所以，可能很多人都忘了，使用2PC事务管理机制时也是有业务逻辑阶段的。正是因为业务逻辑的执行，发起了全局事务，这才有其后的事务处理阶段。实际上， **<font color = "clime">使用2PC机制时————以提交为例————一个完整的事务生命周期是：begin -> 业务逻辑 -> prepare -> commit。</font>**  

&emsp; 再看TCC，也不外乎如此。我们要发起全局事务，同样也必须通过执行一段业务逻辑来实现。该业务逻辑一来通过执行触发TCC全局事务的创建；二来也需要执行部分数据写操作；此外，还要通过执行来向TCC全局事务注册自己，以便后续TCC全局事务commit/rollback时回调其相应的confirm/cancel业务逻辑。所以， **<font color = "clime">使用TCC机制时————以提交为例————一个完整的事务生命周期是：begin -> 业务逻辑(try业务) -> commit(comfirm业务)。</font>**  

&emsp; 综上，可以从执行的阶段上将二者一一对应起来：  
&emsp; 1、 2PC机制的业务阶段 等价于 TCC机制的try业务阶段；  
&emsp; 2、 2PC机制的提交阶段（prepare & commit） 等价于 TCC机制的提交阶段（confirm）；  
&emsp; 3、 2PC机制的回滚阶段（rollback） 等价于 TCC机制的回滚阶段（cancel）。  

&emsp; 因此，可以看出，虽然TCC机制中有两个阶段都存在业务逻辑的执行，但其中try业务阶段其实是与全局事务处理无关的。认清了这一点，当再比较TCC和2PC时，就会很容易地发现，`TCC不是两阶段提交，而只是它对事务的提交/回滚是通过执行一段confirm/cancel业务逻辑来实现，仅此而已。`  

