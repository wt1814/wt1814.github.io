


# 1. sharding-jdbc
<!-- 
ShardingSphere
https://mp.weixin.qq.com/s?__biz=MzAxNTM4NzAyNg==&mid=2247488500&idx=1&sn=108bf704a54b0a9638e84698deb3ce4c&chksm=9b858309acf20a1fc606f6d140e9638072405011829bb8decc906a648d3f2f75441c0adac869&token=1691474648&lang=zh_CN#rd



分片策略
https://segmentfault.com/a/1190000037706070
https://www.cnblogs.com/mr-yang-localhost/p/8313360.html
-->

## 1.1. 分片策略
### 1.1.1. 标准分片策略  
&emsp; 使用场景：SQL 语句中有>，>=, <=，<，=，IN 和 BETWEEN AND 操作符，都可以应用此分片策略。  
&emsp; 标准分片策略（StandardShardingStrategy），它只支持对单个分片健（字段）为依据的分库分表，并提供了两种分片算法 PreciseShardingAlgorithm（精准分片）和 RangeShardingAlgorithm（范围分片）。  
&emsp; 在使用标准分片策略时，精准分片算法是必须实现的算法，用于 SQL 含有 = 和 IN 的分片处理；范围分片算法是非必选的，用于处理含有 BETWEEN AND 的分片处理。  

    一旦我们没配置范围分片算法，而 SQL 中又用到 BETWEEN AND 或者 like等，那么 SQL 将按全库、表路由的方式逐一执行，查询性能会很差需要特别注意。

### 1.1.2. 范围分片算法
&emsp; 使用场景：当SQL中的分片健字段用到 BETWEEN AND操作符会使用到此算法，会根据 SQL中给出的分片健值范围值处理分库、分表逻辑。  

### 1.1.3. 复合分片策略
&emsp; 使用场景：SQL 语句中有>，>=, <=，<，=，IN 和 BETWEEN AND 等操作符，`不同的是复合分片策略支持对多个分片健操作。`  


### 1.1.4. 行表达式分片策略
&emsp; 行表达式分片策略（InlineShardingStrategy），在配置中使用 Groovy 表达式，提供对 SQL语句中的 = 和 IN 的分片操作支持，它只支持单分片健。  
&emsp; 行表达式分片策略适用于做简单的分片算法，无需自定义分片算法，省去了繁琐的代码开发，是几种分片策略中最为简单的。  
&emsp; 它的配置相当简洁，这种分片策略利用inline.algorithm-expression书写表达式。  
&emsp; 比如：ds-$->{order_id % 2} 表示对 order_id 做取模计算， $ 是个通配符用来承接取模结果，最终计算出分库ds-0 ··· ds-n，整体来说比较简单。  

```text
# 行表达式分片键
sharding.jdbc.config.sharding.tables.t_order.database-strategy.inline.sharding-column=order_id

# 表达式算法
sharding.jdbc.config.sharding.tables.t_order.database-strategy.inline.algorithm-expression=ds-$->{order_id % 2}
```

### 1.1.5. Hint分片策略
&emsp; Hint分片策略（HintShardingStrategy）相比于上面几种分片策略稍有不同，这种分片策略无需配置分片健，分片健值也不再从 SQL中解析，而是由外部指定分片信息，让 SQL在指定的分库、分表中执行。ShardingSphere 通过 Hint API实现指定操作，实际上就是把分片规则tablerule 、databaserule由集中配置变成了个性化配置。  
