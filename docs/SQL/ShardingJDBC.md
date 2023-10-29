

# Sharding-JDBC

<!-- 
***ShardingSphere-JDBC5.1.0读写分离配置示例（Java Config）
https://blog.csdn.net/qq_31226223/article/details/123815551

*** https://blog.csdn.net/Bitter_Li/article/details/122394199

https://blog.csdn.net/yx444535180/article/details/123377956
-->


## 概述


## 水平分表
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


## 水平分库


## 垂直分库

## 公共表 — 广播

## 主从复制 - 读写分离

