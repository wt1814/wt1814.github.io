
<!-- TOC -->

- [1. Seata分布式事务框架](#1-seata分布式事务框架)
    - [1.1. AT模式](#11-at模式)
        - [1.1.1. ※※※架构及模块组成](#111-※※※架构及模块组成)
        - [1.1.2. 工作流程示例](#112-工作流程示例)
        - [1.1.3. 全局锁与读写隔离](#113-全局锁与读写隔离)
            - [1.1.3.1. 读写隔离和全局锁](#1131-读写隔离和全局锁)
            - [1.1.3.2. 全局锁实现](#1132-全局锁实现)
            - [1.1.3.3. 防止脏读和脏写](#1133-防止脏读和脏写)
        - [1.1.4. AT模式解析](#114-at模式解析)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
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



# 1. Seata分布式事务框架
<!-- 
https://seata.io/zh-cn/docs/overview/what-is-seata.html
两天，我把分布式事务搞完了 
https://mp.weixin.qq.com/s/amuBimPo7lnfsfo5Pyzc-w
-->

&emsp; Seata 是一款开源的分布式事务解决方案，致力于提供高性能和简单易用的分布式事务服务。Seata 将为用户提供了 AT、TCC、SAGA 和 XA 事务模式，为用户打造一站式的分布式解决方案。  

## 1.1. AT模式
&emsp; AT模式的前提是基于支持本地 ACID 事务的关系型数据库和Java应用基于JDBC访问数据库。AT模式是二阶段提交协议的演变。  


### 1.1.1. ※※※架构及模块组成
<!-- 
*** Seata AT模式
https://blog.csdn.net/a315157973/article/details/103113483

https://baijiahao.baidu.com/s?id=1751062475741532450&wfr=spider&for=pc&searchword=seata%20at%20%E9%9A%94%E7%A6%BB%E7%BA%A7%E5%88%AB
-->
![image](http://182.92.69.8:8081/img/microService/problems/problem-73.png)  
&emsp; 三大组件  
&emsp; TC：事务协调者。即Transaction Coordinator，维护全局和分支事务的状态，驱动全局事务提交或回滚。  
&emsp; TM：事务管理器。即Transaction Manager，定义全局事务的范围，开始事务、提交事务，回滚事务。  
&emsp; RM：资源管理器。即Resource Manager，管理分支事务处理的资源，向TC注册分支事务，报告分支事务的状态，驱动分支事务提交或回滚。

&emsp; 基础交互：TC是需要独立部署的服务，TM和RM是集成在服务中，三大组件相互协作，共同完成分布事务的管理；

------------

&emsp; 1）TM：事务发起者。定义事务的边界，负责告知 TC，分布式事务的开始，提交，回滚。  
&emsp; 2）RM：资源管理者。管理每个分支事务的资源，每一个 RM 都会作为一个分支事务注册在 TC。  
&emsp; 3）TC ：事务协调者。负责事务ID的生成，事务注册、提交、回滚等。  
&emsp; 在Seata的AT模式中，TM和RM都作为SDK的一部分和业务服务在一起，可以认为是Client。TC是一个独立的服务，通过服务的注册、发现将自己暴露给Client。  


&emsp; 从宏观上梳理一下 Seata 的工作过程：  
![image](http://182.92.69.8:8081/img/microService/problems/problem-72.png)  
&emsp; TM 请求 TC，开始一个新的全局事务，TC 会为这个全局事务生成一个 XID。  
&emsp; XID 通过微服务的调用链传递到其他微服务。  
&emsp; RM 把本地事务作为这个XID的分支事务注册到TC。  
&emsp; TM 请求 TC 对这个 XID 进行提交或回滚。  
&emsp; TC 指挥这个 XID 下面的所有分支事务进行提交、回滚。  


### 1.1.2. 工作流程示例
&emsp; AT模式两个阶段：

1. 一阶段：执行用户SQL。业务数据和回滚日志记录在同一个本地事务中提交，释放本地锁和连接资源。    
2. 二阶段：Seata框架自动生成。commit异步化快速完成；rollback通过一阶段的回滚日志进行反向补偿。    

--------------
&emsp; 下面通过一个分支事务的执行过程来了解 Seata 的工作流程。  
&emsp; 例如有一个业务表 product(id,name)，分支事务的业务逻辑：  

```sql
update product set name = 'GTS' where name = 'TXC';
```

1. 一阶段  
    1. 解析SQL：得到 SQL 的类型（UPDATE），表（product），条件（where name = 'TXC'）等相关的信息。  
    2. 查询前镜像：根据解析得到的条件信息，生成查询语句，定位数据。  

    ```sql
    select id, name from product where name = 'TXC';  
    ```
    &emsp; 得到前镜像：  
    
    |id|name|
    |---|---|
    |1|TXC|

    3. 执行业务SQL：执行自己的业务逻辑。  
    ```sql  
    update product set name = 'GTS' where name = 'TXC';  
    ```
    &emsp; 把 name 改为了 GTS。  
    4. 查询后镜像：根据前镜像的结果，通过 主键 定位数据。  

    ```sql
    select id, name from product where id = 1;
    ```
    &emsp; 得到后镜像：
    |id|name|
    |---|---|
    |1|GTS|

    --------------
    5. 插入回滚日志：把前后镜像数据以及业务 SQL 相关的信息组成一条回滚日志记录，插入到 UNDO_LOG 表中。  
    6. 提交前，向 TC 注册分支：申请 product 表中，主键值等于 1 的记录的 全局锁 。  
    7. 本地事务提交：业务数据的更新和前面步骤中生成的 UNDO LOG 一并提交。  
    8. 将本地事务提交的结果上报给 TC。  
2. 二阶段 - 提交  
    1. 收到 TC 的分支提交请求，把请求放入一个异步任务的队列中，马上返回提交成功的结果给 TC。  
    2. 异步任务阶段的分支提交请求，将异步和批量地删除相应 UNDO LOG 记录。  
3. 二阶段 - 回滚  
    1. 收到 TC 的分支回滚请求，开启一个本地事务，执行如下操作。  
    2. 通过 XID 和 Branch ID 查找到相应的 UNDO LOG 记录。  
    3. 数据校验  
    &emsp; 拿 UNDO LOG 中的后镜与当前数据进行比较，根据校验结果决定是否做回滚。  
    4. 根据 UNDO LOG 中的前镜像和业务 SQL 的相关信息生成并执行回滚的语句：  
    ```sql
    update product set name = 'TXC' where id = 1;
    ```
    5. 提交本地事务  
    &emsp; 并把本地事务的执行结果（即分支事务回滚的结果）上报给 TC。  

----------------

&emsp; 用一个比较简单的业务场景来描述一下Seata AT模式的工作过程。  
&emsp; 有个充值业务，现在有两个服务，一个负责管理用户的余额，另外一个负责管理用户的积分。  
&emsp; 当用户充值的时候，首先增加用户账户上的余额，然后增加用户的积分。  
&emsp; AT流程分为两阶段，主要逻辑全部在第一阶段，第二阶段主要做回滚或日志清理的工作。其中，第一阶段流程如下：  
![image](http://182.92.69.8:8081/img/microService/problems/problem-68.png)  
&emsp; 积分服务中也有TM，但是由于没有用到，因此直接可以忽略。  
&emsp; 1）余额服务中的TM，向TC申请开启一个全局事务，TC会返回一个全局的事务ID。  
&emsp; 2）余额服务在执行本地业务之前，RM会先向TC注册分支事务。  
&emsp; 3）余额服务依次生成undo log、执行本地事务、生成redo log，最后直接提交本地事务。  
&emsp; 4）余额服务的RM向TC汇报，事务状态是成功的。  
&emsp; 5）余额服务发起远程调用，把事务ID传给积分服务。   
&emsp; 6）积分服务在执行本地业务之前，也会先向TC注册分支事务。  
&emsp; 7）积分服务次生成undo log、执行本地事务、生成redo log，最后直接提交本地事务。  
&emsp; 8）积分服务的RM向TC汇报，事务状态是成功的。  
&emsp; 9）积分服务返回远程调用成功给余额服务。  
&emsp; 10）余额服务的TM向TC申请全局事务的提交/回滚。  

&emsp; 如果使用Spring框架的注解式事务，远程调用会在本地事务提交之前发生。先发起远程调用还是先提交本地事务，这个其实没有任何影响。  
&emsp; 第二阶段的逻辑就比较简单了。Client和TC之间是有长连接的，如果是正常全局提交，则TC通知多个RM异步清理掉本地的redo和undo log即可。如果是回滚，则TC通知每个RM回滚数据即可。  
&emsp; 这里就会引出一个问题，由于本地事务都是自己直接提交了，后面如何回滚，由于在操作本地业务操作的前后，做记录了undo和redo log，因此可以通过undo log进行回滚。  
&emsp; 由于undo和redo log和业务操作在同一个事务中，因此肯定会同时成功或同时失败。  
&emsp; 但是还会存在一个问题，因为每个事务从本地提交到通知回滚这段时间里，可能这条数据已经被别的事务修改，如果直接用undo log回滚，会导致数据不一致的情况。  
&emsp; 此时，RM会用redo log进行校验，对比数据是否一样，从而得知数据是否有别的事务修改过。注意：undo log是被修改前的数据，可以用于回滚；redo log是被修改后的数据，用于回滚校验。  
&emsp; 如果数据未被其他事务修改过，则可以直接回滚；如果是脏数据，再根据不同策略处理。  


------------

* 一阶段：
    1. 解析 SQL：得到 SQL 的类型（UPDATE），表（product），条件（where name = 'TXC'）等相关的信息。
    2. 查询前镜像：根据解析得到的条件信息，生成查询语句，定位数据。
    3. 执行业务 SQL：执行业务更新SQL。
    4. 查询后镜像：根据前镜像的结果，通过 主键 定位数据。
    5. 插入回滚日志：把前后镜像数据以及业务 SQL 相关的信息组成一条回滚日志记录，插入到 UNDO_LOG 表中。
    6. 提交前，向 TC 注册分支：申请 product 表中，主键值等于 1 的记录的 全局锁 。
    7. 本地事务提交：业务数据的更新和前面步骤中生成的 UNDO LOG 一并提交。
    8. 将本地事务提交的结果上报给 TC。

    一阶段在分支事务提交前向TC注册分支，进行一次通信。

* 二阶段-回滚：  
    1. 收到 TC 的分支回滚请求，开启一个本地事务，执行如下操作。
    2. 通过 XID 和 Branch ID 查找到相应的 UNDO LOG 记录。
    3. 数据校验：拿 UNDO LOG 中的后镜与当前数据进行比较，如果有不同，说明数据被当前全局事务之外的动作做了修改。这种情况，需要根据配置策略来做处理，详细的说明在另外的文档中介绍。
    4. 根据 UNDO LOG 中的前镜像和业务 SQL 的相关信息生成并执行回滚的语句。
    5. 提交本地事务。并把本地事务的执行结果（即分支事务回滚的结果）上报给 TC。

* 二阶段-提交：  
    1. 收到 TC 的分支提交请求，把请求放入一个异步任务的队列中，马上返回提交成功的结果给 TC。异步任务阶段的分支提交请求将异步和批量地删除相应 UNDO LOG 记录。
    2. 既然说到了undolog，seata中是如何记录的呢？

    UNDO_LOG Table，MySQL示例如下：    

        ```sql
        DROP TABLE IF EXISTS `undo_log`;
        -- 注意此处0.3.0+ 增加唯一索引 ux_undo_log
        CREATE TABLE `undo_log` (
        `id` bigint(20) NOT NULL AUTO_INCREMENT,
        `branch_id` bigint(20) NOT NULL,
        `xid` varchar(100) NOT NULL,
        `context` varchar(128) NOT NULL,
        `rollback_info` longblob NOT NULL,
        `log_status` int(11) NOT NULL,
        `log_created` datetime NOT NULL,
        `log_modified` datetime NOT NULL,
        PRIMARY KEY (`id`),
        UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
        ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
        ```

### 1.1.3. 全局锁与读写隔离    
<!--
***Seata AT 模式事务隔离级别与全局锁设计
https://blog.csdn.net/hundan_520520/article/details/128833977

seata AT模式是如何实现的
https://blog.csdn.net/zzti_erlie/article/details/120939588


Seata AT 模式代码级详解
https://zhuanlan.zhihu.com/p/566683139?utm_id=0
seata分布式事务
https://blog.csdn.net/weixin_42420663/article/details/126276049

-->
#### 1.1.3.1. 读写隔离和全局锁
&emsp; Seata AT模式的事务隔离是建立在分支事务的本地隔离级别基础之上的，在数据库本地隔离级别读已提交或以上的前提下，Seata 设计了由事务协调器维护的全局写排他锁，来保证事务间的写隔离，同时，将全局事务默认定义在读未提交的隔离级别上。  

&emsp; 脏读脏写： Seata 的事务是一个全局事务，它包含了若干个分支本地事务，在全局事务执行过程中（全局事务还没执行完），某个本地事务提交了，如果 Seata 没有采取任务措施，则会导致已提交的本地事务被读取，造成脏读，如果数据在全局事务提交前已提交的本地事务被修改，则会造成脏写。  

&emsp; 传统意义的脏读是读到了未提交的数据，Seata 脏读是读到了全局事务下未提交的数据，全局事务可能包含多个本地事务，某个本地事务提交了不代表全局事务提交了。  
&emsp; 在绝大部分应用在读已提交的隔离级别下工作是没有问题的，而实际上，这当中又有绝大多数的应用场景，实际上工作在读未提交的隔离级别下同样没有问题。  
&emsp; 在极端场景下，应用如果需要达到全局的读已提交，Seata 也提供了全局锁机制实现全局事务读已提交。但是默认情况下，Seata 的全局事务是工作在读未提交隔离级别的，保证绝大多数场景的高效性。  


#### 1.1.3.2. 全局锁实现


#### 1.1.3.3. 防止脏读和脏写  



### 1.1.4. AT模式解析
<!-- 
seata AT模式是如何实现的
https://blog.csdn.net/zzti_erlie/article/details/120939588
分布式事务Seata——AT模式解析
https://www.jianshu.com/p/ea454a710908
-->


