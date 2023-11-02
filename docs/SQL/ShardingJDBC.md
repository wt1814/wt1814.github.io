<!-- TOC -->

- [1. Sharding-JDBC](#1-sharding-jdbc)
    - [1.1. 概述](#11-概述)
    - [1.2. 使用](#12-使用)
        - [1.2.1. 水平分表](#121-水平分表)
        - [1.2.2. 水平分库](#122-水平分库)
        - [1.2.3. 垂直分库](#123-垂直分库)
        - [1.2.4. 公共表 — 广播](#124-公共表--广播)
        - [1.2.5. 主从复制 - 读写分离](#125-主从复制---读写分离)
    - [1.3. 分布式事务](#13-分布式事务)
        - [1.3.1. 介绍：支持3种分布式事务](#131-介绍支持3种分布式事务)
        - [1.3.2. 支持4个事务框架](#132-支持4个事务框架)
        - [1.3.3. 实现两阶段提交](#133-实现两阶段提交)
        - [1.3.4. 实现柔性事务](#134-实现柔性事务)

<!-- /TOC -->

# 1. Sharding-JDBC

<!-- 
***ShardingSphere-JDBC5.1.0读写分离配置示例（Java Config）
https://blog.csdn.net/qq_31226223/article/details/123815551

*** https://blog.csdn.net/Bitter_Li/article/details/122394199

https://blog.csdn.net/yx444535180/article/details/123377956
-->


## 1.1. 概述


## 1.2. 使用
### 1.2.1. 水平分表
<!-- 
https://blog.csdn.net/u013473447/article/details/121868284
-->
在order_db中创建t_order_1、t_order_2表。人工创建两张表，t_order_1和t_order_2，这两张表是订单表拆分后的表，通过Sharding-Jdbc向订单表插入数据， 按照一定的分片规则，主键为偶数的进入t_order_1，另一部分数据进入t_order_2，通过Sharding-Jdbc 查询数据  

```xml
# 以下是分片规则配置
# 定义数据源
spring.shardingsphere.datasource.names = m1
spring.shardingsphere.datasource.m1.type = com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.m1.driver‐class‐name = com.mysql.jdbc.Driver
spring.shardingsphere.datasource.m1.url = jdbc:mysql://localhost:3306/order_db?useUnicode=true
spring.shardingsphere.datasource.m1.username = root
spring.shardingsphere.datasource.m1.password = root
# 指定t_order表的数据分布情况，配置数据节点
spring.shardingsphere.sharding.tables.t_order.actual‐data‐nodes = m1.t_order_$‐>{1..2}
# 指定t_order表的主键生成策略为SNOWFLAKE
spring.shardingsphere.sharding.tables.t_order.key‐generator.column=order_id
spring.shardingsphere.sharding.tables.t_order.key‐generator.type=SNOWFLAKE
 
# 指定t_order表的分片策略，分片策略包括分片键和分片算法
spring.shardingsphere.sharding.tables.t_order.table‐strategy.inline.sharding‐column = order_id
spring.shardingsphere.sharding.tables.t_order.table‐strategy.inline.algorithm‐expression =t_order_$‐>{order_id % 2 + 1}

```


### 1.2.2. 水平分库


### 1.2.3. 垂直分库

### 1.2.4. 公共表 — 广播

### 1.2.5. 主从复制 - 读写分离


## 1.3. 分布式事务  
<!-- 
https://www.cnblogs.com/ppku/p/17023127.html
*** https://blog.csdn.net/qq_41432730/article/details/122373098

-->
&emsp; sharding-jdbc分布式事务支持：官网https://shardingsphere.apache.org/document/current/cn/features/transaction/  

### 1.3.1. 介绍：支持3种分布式事务  
&emsp; ShardingJDBC支持的分布式事务方式有三种 LOCAL, XA , BASE，这三种事务实现方式都是采用的对代码无侵入的方式实现的。  

1. 本地事务
&emsp; 在不开启任何分布式事务管理器的前提下，让每个数据节点各自管理自己的事务。 它们之间没有协调以及通信的能力，也并不互相知晓其他数据节点事务的成功与否。 本地事务在性能方面无任何损耗，但在强一致性以及最终一致性方面则力不从心。  
​&emsp; 这种方式实际上是将事务交由数据库自行管理，可以用Spring的@Transaction注解来配置。这种方式不具备分布式事务的特性。  

2. 两阶段提交：  
&emsp; XA协议最早的分布式事务模型是由 X/Open 国际联盟提出的 X/Open Distributed Transaction Processing (DTP) 模型，简称 XA 协议。  
&emsp; 基于XA协议实现的分布式事务对业务侵入很小。 它最大的优势就是对使用方透明，用户可以像使用本地事务一样使用基于XA协议的分布式事务。 XA协议能够严格保障事务 ACID 特性。  
&emsp; 严格保障事务 ACID 特性是一把双刃剑。 事务执行在过程中需要将所需资源全部锁定，它更加适用于执行时间确定的短事务。 对于长事务来说，整个事务进行期间对数据的独占，将导致对热点数据依赖的业务系统并发性能衰退明显。 因此，在高并发的性能至上场景中，基于XA协议的分布式事务并不是最佳选择。   

3. 柔性事务:
&emsp; 如果将实现了 ACID 的事务要素的事务称为刚性事务的话，那么基于 BASE 事务要素的事务则称为柔性事务。 BASE 是基本可用、柔性状态和最终一致性这三个要素的缩写。   
&emsp; 在 ACID 事务中对隔离性的要求很高，在事务执行过程中，必须将所有的资源锁定。 柔性事务的理念则是通过业务逻辑将互斥锁操作从资源层面上移至业务层面。通过放宽对强一致性要求，来换取系统吞吐量的提升。  


### 1.3.2. 支持4个事务框架  
&emsp; Sharding-JDBC支持以下四种事务模型，实际上这些分布式事务模式都是集成开源的事务组件做的集成。
* Atomikos事务
* Narayana事务
* Bitronix事务
* Seata事务

### 1.3.3. 实现两阶段提交  
&emsp; Apache ShardingSphere 默认的 XA 事务管理器为 Atomikos。  
&emsp; Sharding-JDBC下实现强一致分布式事务需要导入以下依赖：  

```xml
<!--xa分布式事务-->
<dependency>
　　<groupId>io.shardingsphere</groupId>
　　<artifactId>sharding-transaction-2pc-xa</artifactId>
　　<version>3.1.0</version>
</dependency>

<dependency>
　　<groupId>io.shardingsphere</groupId>
　　<artifactId>sharding-transaction-spring-boot-starter</artifactId>
　　<version>3.1.0</version>
</dependency>
```

&emsp; 默认是用 atomikos 实现的。在 Service 类上加上注解：  
```java
@ShardingTransactionType(TransactionType.XA)
@Transactional(rollbackFor = Exception.class)
```


### 1.3.4. 实现柔性事务  
&emsp; 这种模式，是由Seata作为事务协调者，来进行协调。使用方式需要先部署seata服务。官方建议是使用seata配合nacos作为配置中心来使用。实际上是使用的seata的AT模式进行两阶段提交。  


