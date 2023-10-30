
<!-- TOC -->

- [1. shardingsphere](#1-shardingsphere)
    - [1.1. ★★★ShardingSphere](#11-★★★shardingsphere)
    - [1.2. Sharding-JDBC](#12-sharding-jdbc)
        - [1.2.1. Sharding-JDBC 适用场景](#121-sharding-jdbc-适用场景)
        - [1.2.2. Sharding-JDBC 不合适的场景](#122-sharding-jdbc-不合适的场景)
        - [1.2.3. sharding-jdbc支持读写分离](#123-sharding-jdbc支持读写分离)
    - [1.3. ★★★Sharding-Proxy](#13-★★★sharding-proxy)
    - [1.4. sharding-jdbc与sharding-proxy比较](#14-sharding-jdbc与sharding-proxy比较)
    - [1.5. shardingsphere分布式事务](#15-shardingsphere分布式事务)

<!-- /TOC -->


# 1. shardingsphere
<!-- 
系列文章
https://blog.csdn.net/linzhefeng89/category_10701422.html?utm_source=BWXQ_bottombtn&spm=1001.2101.3001.4225
ShardingSphere内核原理 
https://mp.weixin.qq.com/s/hPJHCKcptRYvKQPZRz6Tdg
-->


https://www.jianshu.com/p/0238ff53c6bb

## 1.1. ★★★ShardingSphere
&emsp; 官网：https://shardingsphere.apache.org/document/current/cn/features/   
&emsp; ShardingSphere是一套开源的分布式数据库中间件解决方案组成的生态圈。   
&emsp; 它由Sharding-JDBC、Sharding-Proxy和Sharding-Sidecar（计划中）这3款相互独立的产品组成，shardingSphere定位为关系型数据库中间件。  

开源分布式数据库中间件解决方案，主要有Sharding-JDBC和Sharding-Proxy   
客户端分库分表：以jar包形式放在java应用里，通过Sharding-JDBC去找对应数据，主要作用数据分片，读写分离   
服务端分库分表：利用Sharding-Proxy伪装成数据库，分库分表由Sharding-Proxy实现   

## 1.2. Sharding-JDBC

<!-- 
sharding-jdbc与Sharding-Proxy
https://www.jianshu.com/p/20c0d4114632
-->
Sharding-JDBC是Sharding-Sphere的第一个产品，也是Sharding-Sphere的前身，是当当网开源的一个产品。定位为轻量级的Java框架，在Java的JDBC层提供额外服务。 它使用客户端直连数据库，以jar包形式提供服务，无需额外部署和依赖，可理解为增强版的JDBC驱动，完全兼容JDBC和各种ORM框架。  

Sharding-JDBC 采用在 JDBC 层扩展分库分表，支持读写分离，是一个以 jar 形式提供服务的轻量级组件，其核心思路是小而美地完成最核心的事情，基于 JDBC 层进行分片的好处是轻量、简单、兼容性好以及无需额外的运维工作。缺点是无法跨语言，目前仅支持 Java。


### 1.2.1. Sharding-JDBC 适用场景
对于关系型数据库数据量很大的情况，需要进行水平拆库和拆表（即分库和分表），这种场景很适合使用 Sharding-JDBC。
举例说明：假设有一亿数据的用户库，放在 MySQL 数据库里查询性能会比较低，而采用水平拆库，将其分为 10 个库，根据用户的 ID 模10，这样数据就能比较平均的分在 10 个库中，每个库只有 1000w 记录，查询性能会大大提升。分片策略类型非常多，大致分为 Hash +Mod、Range、Tag 等。
Sharding-JDBC 还提供了读写分离的能力，用于减轻写库的压力。


### 1.2.2. Sharding-JDBC 不合适的场景
不适合 OLAP 的场景。虽然 Sharding-JDBC 也能做聚合分组查询，但大量的 OLAP 场景，仍然会比较慢，而且复杂的SQL（如子查询等）目前还没有支持。这种查询不太适合大数据和高并发的互联网 online 数据库，建议使用合理的 OLTP 查询(OLTP:联机事务处理(Online Transaction Processing))。
不适合事务强一致的要求。目前 Sharding-JDBC 的事务支持两种，一种是弱 XA，另一种是柔性事务（BASE）。因为 XA的两阶段或三阶段提交其性能较低，因此互联网公司基本不会采用。而无论是弱 XA还是柔性事务，都无法保证事务在任意时间段完全保证一致，其中柔性事务能保证数据的最终一致性，但达到最终一致性的时间仍然不可控。因此对于对跨库事务强一致要求很高的场景，需要从设计方面去考虑数据库schema 的合理性。
对于 JTA 事务，目前 Shariding-JDBC 没有实现 JTA 的标准。而且由于在互联网场景下使用 JTA 比较少见，因此暂时不支持 JTA 事务。



### 1.2.3. sharding-jdbc支持读写分离
Sharding-JDBC 从 1.3.0 开始支持读写分离。其功能包括：

根据配置区分写库和多个读库，目前暂时只有轮训策略选取读库，可以配合分库分表使用。
通过 Hint 强制指定某次查询走写库。
如果在同一线程且同一数据库连接中有发现 DML 语句，则该 DML 之后的查询都从写库查询，DML 之前的 DQL 语句不受影响，仍然查询读库。其目的是保持同一用户线程的数据一致性。
但限于 Sharding-JDBC 本身设计的考虑，数据库层面的主从切换以及主从数据同步，Sharding-JDBC并不负责。Sharding-JDBC 定位仍然是轻量级的增强版数据库驱动。因此由于主库和从库同步延迟导致的数据不一致，并不是Sharding-JDBC 的处理范畴。
官网参考：http://shardingsphere.io/document/current/en/quick-start/


## 1.3. ★★★Sharding-Proxy
Sharding-Proxy是Sharding-Sphere的第二个产品。 它定位为透明化的数据库代理端，提供封装了数据库二进制协议的服务端版本，用于完成对异构语言的支持。 Sharding-Proxy屏蔽了底层的分库分表，可以像使用一个简单的数据库一样来操作分库分表的数据。  

Sharding-Proxy的定位是透明化的数据库代理，它封装了数据库二进制协议，用于完成对异构语言的支持。目前兼容MySQL和PG，可以使用任何兼容MySQL或PG协议的客户端进行访问，比如MySQL命令行、MySQL Workbench、Navicat等等，对DBA更加友好。
整个架构可以分为前端、后端和核心组件三部分。
前端负责与客户端进行网络通信，采用的是基于NIO的客户端/服务器框架，在Windows和Mac操作系统下采用NIO 模型，Linux系统自动适配为Epoll模型。通信的过程中完成对MySQL协议的编解码。核心组件得到解码的MySQL命令后，开始调用Sharding-Core对SQL进行解析、路由、改写、结果归并等核心功能。后端与真实数据库的交互目前借助于Hikari连接池。

## 1.4. sharding-jdbc与sharding-proxy比较
Sharding-JDBC是Sharding-Sphere的第一个产品，也是Sharding-Sphere的前身。  
它定位为轻量级Java框架，在Java的JDBC层提供分库分表、读写分离、数据库治理、柔性事务等服务。它使用客户端直连数据库，以jar包形式提供服务，无需额外部署和依赖，可理解为增强版的JDBC驱动，完全兼容JDBC和各种ORM框架。  

Sharding-Proxy是Sharding-Sphere的第二个产品。  
它定位为透明化的数据库代理端，提供封装了数据库二进制协议的服务端版本，用于完成对异构语言的支持。
Sharding-Proxy屏蔽了底层的分库分表，您可以像使用一个简单的数据库一样来操作分库分表的数据。目前提供MySQL版本，它可以使用任何兼容MySQL协议的访问客户端(如：MySQL Command Client, MySQLWorkbench等)来访问Sharding-Proxy，进而进行DDL/DML等操作来变更数据，对DBA更加友好。  

ShardingJDBC是一个开源的分布式数据库中间件，它提供了分库分表、读写分离、分布式事务等功能，可以帮助开发者轻松地实现数据库的水平扩展。    
而Proxy是一个数据库代理工具，可以将数据库请求转发到后端的多个数据库节点上，实现负载均衡和故障转移等功能。    
两者的主要区别在于ShardingJDBC更加注重数据库的分片和分布式事务，而Proxy更加注重数据库的负载均衡和故障转移。  


## 1.5. shardingsphere分布式事务  

<!--
https://shardingsphere.apache.org/document/legacy/4.x/document/cn/manual/sharding-jdbc/usage/transaction/#%E9%85%8D%E7%BD%AEspring-boot%E7%9A%84%E4%BA%8B%E5%8A%A1%E7%AE%A1%E7%90%86%E5%99%A8
-->





