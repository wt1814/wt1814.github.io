
<!-- TOC -->

- [1. MySql锁造成的问题](#1-mysql锁造成的问题)
    - [1.1. 死锁](#11-死锁)
        - [1.1.1. 死锁产生](#111-死锁产生)
        - [1.1.2. 死锁检测](#112-死锁检测)
        - [1.1.3. 避免死锁](#113-避免死锁)
        - [1.1.4. 解决死锁](#114-解决死锁)
    - [1.2. 生产问题-死锁解决](#12-生产问题-死锁解决)
        - [1.2.1. 前提](#121-前提)
        - [1.2.2. 死锁场景复现](#122-死锁场景复现)
        - [1.2.3. 导致死锁的原因](#123-导致死锁的原因)
        - [1.2.4. 解决方案](#124-解决方案)
        - [1.2.5. 小结](#125-小结)
    - [1.3. 锁表](#13-锁表)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; **<font color = "clime">1. MySql如何处理死锁？1). 发起死锁检测，主动回滚其中一条事务，让其他事务继续执行。2). 设置超时时间，超时后自动释放。</font>**   
&emsp; **<font color = "clime">2. 如果出现死锁？除了以上两种方案外，开发人员还需要检查代码。</font>**


# 1. MySql锁造成的问题

## 1.1. 死锁  
<!-- 
★★★mysql死锁的情况问题分析 gap间隙锁
https://blog.csdn.net/u010325193/article/details/88366707
MySQL间隙锁问题
https://www.cnblogs.com/jing99/p/11489510.html
-->
<!-- 
MySQL死锁产生原因和解决方法 
https://mp.weixin.qq.com/s/F3IPSiKzabuDd8S5UKI-WQ
死锁的发生与否，并不在于事务中有多少条SQl语句，死锁的关键在于：两个(或以上)的Session加锁的顺序不一致。而使用本文上面提到的，分析MySQL每条SQL语句的加锁规则，分析出每条语句的加锁顺序，然后检查多个并发SQL间是否存在相反的顺序加锁的情况，就可以分析出各种潜在的死锁情况，也可以分析出线上死锁发生的原因。 
-->

&emsp; 服务器报错：Deadlock found when trying to get to lock; try restarting transaction。  
&emsp; 死锁发生了如何解决，MySQL 有没有提供什么机制去解决死锁。
  
### 1.1.1. 死锁产生  
&emsp; 死锁是指两个或多个事务在同一资源上相互占用，并请求锁定对方占用的资源，从而导致恶性循环。  
&emsp; 当事务试图以不同的顺序锁定资源时，就可能产生死锁。多个事务同时锁定同一个资源时也可能会产生死锁。  
&emsp; 锁的行为和顺序和存储引擎相关。以同样的顺序执行语句，有些存储引擎会产生死锁有些不会——死锁有双重原因：真正的数据冲突；存储引擎的实现方式。  

### 1.1.2. 死锁检测 
&emsp; **<font color = "red">检测死锁</font>** ：数据库系统实现了各种死锁检测和死锁超时的机制。InnoDB存储引擎能检测到死锁的循环依赖并立即返回一个错误。  

&emsp; **<font color = "red">死锁恢复</font>** ：死锁发生以后，只有部分或完全回滚其中一个事务，才能打破死锁，InnoDB目前处理死锁的方法是，将持有最少行级排他锁的事务进行回滚。所以事务型应用程序在设计时必须考虑如何处理死锁，多数情况下只需要重新执行因死锁回滚的事务即可。  

&emsp; **<font color = "red">外部锁的死锁检测</font>** ：发生死锁后，InnoDB 一般都能自动检测到，并使一个事务释放锁并回退，另一个事务获得锁，继续完成事务。 **<font color = "clime">但在涉及外部锁，或涉及表锁的情况下，InnoDB 并不能完全自动检测到死锁， 这需要通过设置锁等待超时参数 innodb_lock_wait_timeout 来解决。</font>**  

---
&emsp; **<font color = "red">死锁影响性能</font>** ：死锁会影响性能而不是会产生严重错误，因为InnoDB会自动检测死锁状况并回滚其中一个受影响的事务。在高并发系统上，当许多线程等待同一个锁时，死锁检测可能导致速度变慢。有时当发生死锁时，禁用死锁检测(使用innodb_deadlock_detect配置选项)可能会更有效，这时可以依赖innodb_lock_wait_timeout设置进行事务回滚。  

### 1.1.3. 避免死锁 
&emsp; **<font color = "red">MyISAM避免死锁：</font>**  在自动加锁的情况下，MyISAM总是一次获得SQL语句所需要的全部锁，所以 MyISAM 表不会出现死锁。  

&emsp; **<font color = "red">InnoDB避免死锁：</font>**   

* 为了在单个InnoDB表上执行多个并发写入操作时避免死锁，可以在事务开始时通过为预期要修改的每个元祖(行)使用SELECT ... FOR UPDATE语句来获取必要的锁，即使这些行的更改语句是在之后才执行的。  
* 在事务中，如果要更新记录，应该直接申请足够级别的锁，即排他锁，而不应先申请共享锁、更新时再申请排他锁，因为这时候当用户再申请排他锁时，其他事务可能又已经获得了相同记录的共享锁，从而造成锁冲突，甚至死锁  
* 如果事务需要修改或锁定多个表，则应在每个事务中以相同的顺序使用加锁语句。在应用中，如果不同的程序会并发存取多个表，应尽量约定以相同的顺序来访问表，这样可以大大降低产生死锁的机会  
* 通过SELECT ... LOCK IN SHARE MODE获取行的读锁后，如果当前事务再需要对该记录进行更新操作，则很有可能造成死锁。  
* 改变事务隔离级别  

### 1.1.4. 解决死锁
&emsp; **<font color = "clime">如果出现死锁</font>** ，<font color = "clime">可以用show engine innodb status;命令来确定最后一个死锁产生的原因。</font>返回结果中包括死锁相关事务的详细信息，如引发死锁的SQL语句，事务已经获得的锁，正在等待什么锁，以及被回滚的事务等。据此可以分析死锁产生的原因和改进措施。  

&emsp; **<font color = "clime">死锁的解决方案：</font>**   
1. 首先在程序的设计上，当发现程序有高并发的访问某一个表时，尽量对该表的执行操作串行化，或者锁升级，一次性获取所有的锁资源。  

        编码中，insert、delete、update这些操作，独占锁占用事务时间过长，是会锁表的。  
        解决方案：操作要进行拆分，重整数据库策略，比如限制处理1000条。删除操作，删除数据的速度和创建的索引数量是成正比的。所以在超大型数据库中，删除时处理好索引关系非常重要。推荐的折中方法：在删除数据之前删除表中索引，然后删除其中无用数据，删除完成后重新创建索引。  
        
        死锁的避免
        1. 顺序访问
        2. 数据排序
        3. 申请足够级别的锁
        4. 避免没有where条件的操作
        5. 大事务分解成小事务
        6. 使用等值查询而不是范围查询
         
2. 然后也可以在服务器上设置参数innodb_lock_wait_timeout(超时时间)，并且将参数innodb_deadlock_detect 打开，当发现死锁的时候，自动回滚其中的某一个事务。  

## 1.2. 生产问题-死锁解决
<!--
~~ 
MySQL死锁如何处理？ 
https://mp.weixin.qq.com/s/1mO8q-RJrxx1OTtM8dR4Ng
~~
-->
### 1.2.1. 前提  
&emsp; 笔者负责的一个系统最近有新功能上线后突然在预警模块不定时报出MySQL死锁导致事务回滚。幸亏，上游系统采用了异步推送和同步查询结合的方式，感知到推送失败及时进行了补偿。  

### 1.2.2. 死锁场景复现  
&emsp; 首先，MySQL的服务端版本是5.7(小版本可以基本忽略)，使用了InnoDB。有一张用户数据表的schema设计如下(无关字段已经屏蔽掉)：  

```sql
CREATE TABLE `t_user_data`
(
    id      BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    data_id VARCHAR(50)     NOT NULL COMMENT '数据ID',
    INDEX idx_user_id (user_id),
    INDEX idx_data_id (data_id)
) COMMENT '用户数据表';
```

&emsp; 业务代码中发生死锁的伪代码如下：  

```java
process_method(dataId,userDataDtoList){
    start transaction:
    userDataDao.deleteByDataId(dataId);
    for dto in userDataDtoList:
        UserData userData = convert(dto);
        userDataDao.insert(dto);
    commit;
}
```

&emsp; 这里的逻辑是，如果已经存在对应dataId的数据要先进行删除，然后写入新的用户数据。  
&emsp; 尝试用两个Session提交两个事务重现死锁问题：  

|时间序列|Tx-Session-1|Tx-Session-2|
|---|---|---|
|T1	START TRANSACTION;| |	
|T2	| |START TRANSACTION;|
|T3	|DELETE FROM t_user_data WHERE data_id = ‘xxxxx’;|  |	
|T4	| |DELETE FROM t_user_data WHERE data_id = ‘yyyyy’;|
|T5	|INSERT INTO t_user_data(USER_ID, DATA_ID) VALUES (1, ‘xxxxx’);	| |
|T6	| |INSERT INTO t_user_data(USER_ID, DATA_ID) VALUES (2, ‘yyyyy’);|
|T7	| |Deadlock found when trying to get lock; try restarting transaction(Rollback)|
|T8	|COMMIT;|	|

&emsp; 这里会出现两个现象：  

1. Tx-Session-2会话T4执行完毕之后，Tx-Session-1会话T5执行的时候，Tx-Session-1会话客户端会处于阻塞状态。
2. Tx-Session-2会话T6执行完毕之后，MySQL提示死锁事务被回滚，此时，Tx-Session-1会话客户端会解除阻塞。


### 1.2.3. 导致死锁的原因  
&emsp; 直接排查InnoDB的死锁日志：  

    mysql> show engine innodb status;

&emsp; 输出的死锁日志如下：  

```text
------------------------
LATEST DETECTED DEADLOCK
------------------------
2019-05-11 19:16:04 0x5804
*** (1) TRANSACTION:
TRANSACTION 3882, ACTIVE 13 sec inserting
mysql tables in use 1, locked 1
LOCK WAIT 3 lock struct(s), heap size 1136, 2 row lock(s), undo log entries 1
MySQL thread id 32, OS thread handle 9876, query id 358 localhost ::1 doge update
INSERT INTO t_user_data(USER_ID, DATA_ID) VALUES (1, 'xxxxx')
*** (1) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 33 page no 6 n bits 72 index idx_data_id of table `test`.`t_user_data` trx id 3882 lock_mode X insert intention waiting
Record lock, heap no 1 PHYSICAL RECORD: n_fields 1; compact format; info bits 0
 0: len 8; hex 73757072656d756d; asc supremum;;

*** (2) TRANSACTION:
TRANSACTION 3883, ACTIVE 9 sec inserting, thread declared inside InnoDB 5000
mysql tables in use 1, locked 1
3 lock struct(s), heap size 1136, 2 row lock(s), undo log entries 1
MySQL thread id 11, OS thread handle 22532, query id 359 localhost ::1 doge update
INSERT INTO t_user_data(USER_ID, DATA_ID) VALUES (2, 'yyyyy')
*** (2) HOLDS THE LOCK(S):
RECORD LOCKS space id 33 page no 6 n bits 72 index idx_data_id of table `test`.`t_user_data` trx id 3883 lock_mode X
Record lock, heap no 1 PHYSICAL RECORD: n_fields 1; compact format; info bits 0
 0: len 8; hex 73757072656d756d; asc supremum;;

*** (2) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 33 page no 6 n bits 72 index idx_data_id of table `test`.`t_user_data` trx id 3883 lock_mode X insert intention waiting
Record lock, heap no 1 PHYSICAL RECORD: n_fields 1; compact format; info bits 0
 0: len 8; hex 73757072656d756d; asc supremum;;

*** WE ROLL BACK TRANSACTION (2)
```

&emsp; 这里要参考MySQL关于InnoDB锁的关于next-key锁描述那一节，注意死锁日志关键字supremum的意义：  

    next-key锁将gap锁定在索引中最大值之上，而supremum伪记录的值高于索引中实际的任何值。supremum不是真正的索引记录，因此，实际上，此next-key锁仅锁定最大索引值之后的间隙。  

&emsp; <font color = "red">两个事务的锁属性可以通过select * from information_schema.innodb_locks;进行查询，</font>数据如下表：  

|lock_id	|lock_tx_id	|lock_mode	|lock_type	|lock_table	|lock_index	|lock_space|lock_page|lock_rec|lock_data|
|---|---|---|---|---|---|---|---|---|---|
|3882:33:6:1	|3882|	X|	RECORD|	test.t_user_data|	idx_data_id	|33|	6	|1|	supremum pseudo-record|
|3883:33:6:1	3883	|X	|RECORD	|test.t_user_data	|idx_data_id	|33	|6	|1	|supremum pseudo-record|

    DELETE FROM t_user_data WHERE data_id = '不存在的索引值';   

&emsp; 上面的SQL执行时候，如果条件刚好是索引列，并且查询的值是当前表(索引)中不存在的数据，根据next-key锁的描述和死锁日志中的asc supremum关键字，执行该DELETE语句的时候，会锁定目标值和高于目标值的任何值，如果条件是"xxxxx"，那么相当于锁定区间为(“xxxxx”,最大上界]。  

&emsp; next-key锁是索引记录上的记录锁(Record Lock)和索引记录之前的间隙上的间隙锁(Gap Lock)定的组合。间隙锁有两个特点：  
1. 两个事务即使锁定的区间一致(或者有部分重合)，不会影响它们之间获取到锁(可以参考行锁的兼容性矩阵)。  
2. 间隙锁G会阻止非持有G的其他事务向锁定的区间中插入数据，以避免产生冲突数据。  

&emsp; 分析到这里，就很好解释上面出现死锁的执行时序：  
1. 两个事务的DELETE语句都可以正确执行，这个时候，两者的间隙锁锁定的区域分别是(‘xxxxx’,最大上界]和(‘yyyyy’,最大上界]。
2. 事务1执行INSERT语句的时候阻塞，是因为事务2的间隙锁不允许事务1插入索引值’xxxxx’。
3. 事务2执行INSERT语句的时候阻塞，是因为事务1的间隙锁不允许事务1插入索引值’yyyyy’，执行到这一步，MySQL的死锁检查模块应该起效了，因为两个事务依赖的锁资源已经成环(或者成有向图)。
4. 事务2的优先级比较低，于是抛出死锁异常并且被回滚了。  

### 1.2.4. 解决方案  
&emsp; 参考MySQL的文档，解决方案有两个：  

1. 方案一：降低数据库的事务隔离级别，需要降低到READ COMMITED，这样子可以关闭间隙锁的扫描。(<== 并不推荐这种做法，修改事务隔离级别有可能出现新的问题)
2. 方案二：针对对应的原因修改业务代码。

&emsp; 这里方案二只需要把伪代码逻辑修改如下：  

```java
process_method(dataId,userDataDtoList){
    List<UserData> userDataList = userDataDao.selectByDataId(dataId);
    start transaction:
    if userDataList is not empty: 
       List<Long> ids = collectIdList(userDataList);
       userDataDao.deleteByIds(ids);       
    for dto in userDataDtoList:
        UserData userData = convert(dto);
        userDataDao.insert(dto);
    commit;
}
```

&emsp; 就是先根据dataId进行查询，如果存在数据，聚合主键列表，通过主键列表进行删除，然后再进行数据插入。  

### 1.2.5. 小结  
&emsp; InnoDB提供的死锁日志其实并没有提供完整的事务提交的SQL，所以对于复杂的场景需要细致结合代码和死锁日志进行排查，很多时候对应的代码逻辑是多处的。这里列举一下处理死锁问题的一些步骤：  
1. 及时止损，如果可以回滚导致死锁的代码，那么最好果敢地回滚；如果重试可以解决问题并且出现死锁问题的规模不大，可以尝试短时间内进行问题排查。  
2. 通过业务系统日志迅速定位到发生死锁的代码块，JVM应用一般底层是依赖JDBC，出现死锁的时候会抛出一个SQLException的子类，异常栈的信息中带有"Deadlock"字样。  
3. 分析InnoDB的死锁日志，一般会列出竞争锁的多个事务的相对详细的信息，这些信息是排查死锁问题的第一手资料。  
4. 修复问题上线后注意做好监控和预警，确定问题彻底解决。


## 1.3. 锁表  
<!--

https://blog.csdn.net/cai519678181/article/details/105475504
数据库为什么会锁表
http://www.360doc.com/content/18/0414/07/48169514_745495475.shtml
-->

1. 锁表发生在insert、update、delete中。  
2. 锁表的原理是数据库使用独占式封锁机制，当执行上面的语句时，对表进行锁住，直到发生commite 或者 回滚 或者 退出数据库用户。  
3. 锁表的原因  
&emsp; 第一、 A程序执行了对 tableA 的 insert ，并还未 commite时，B程序也对tableA 进行insert 则此时会发生资源正忙的异常 就是锁表  
&emsp; 第二、锁表常发生于并发而不是并行(并行时，一个线程操作数据库时，另一个线程是不能操作数据库的，cpu 和 i/o 分配原则)  

&emsp; (1)字段不加索引：在执行事务的时候，如果表中没有索引，会执行全表扫描，如果这时候有其他的事务过来，就会发生锁表！  
&emsp; (2)事务处理时间长：事务处理时间较长，当越来越多事务堆积的时候，会发生锁表！  
&emsp; (3)关联操作太多：涉及到很多张表的修改等，在并发量大的时候，会造成大量表数据被锁！  

4. 减少锁表的概率  
&emsp; 减少insert 、update 、delete 语句执行 到 commite 之间的时间。具体点批量执行改为单个执行、优化sql自身的非执行速度  
&emsp; 如果异常对事物进行回滚  


&emsp; 出现锁表的解决方法有：  

1. 通过相关的sql语句可以查出是否被锁定，和被锁定的数据！  
2. 为加锁进行时间限定，防止无限死锁！  
3. 加索引，避免全表扫描！  
4. 尽量顺序操作数据！  
5. 根据引擎选择合理的锁粒度！  
6. 事务中的处理时间尽量短！ 