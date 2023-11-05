

<!-- TOC -->

- [1. ES](#1-es)
    - [1.1. MySQL数据同步到ES的4种解决方案](#11-mysql数据同步到es的4种解决方案)
    - [1.2. 分页3种方案](#12-分页3种方案)
    - [1.3. 使用ES中的一些问题](#13-使用es中的一些问题)
        - [1.3.1. cpu使用率过高](#131-cpu使用率过高)
        - [1.3.2. Elasticsearch OOM](#132-elasticsearch-oom)
        - [1.3.3. 聚合数据结果不精确](#133-聚合数据结果不精确)

<!-- /TOC -->


# 1. ES  

## 1.1. MySQL数据同步到ES的4种解决方案

* 同步双写  
* 异步双写  
&emsp; 异步双写是指在主库上进行数据修改操作时，异步将数据写入到备库中。这种方式可以降低主库的写入延迟，并且备库出现问题时不会影响主库的性能，但是可能会存在主备数据不一致的情况。  

* 定时任务  
&emsp; 定时任务是指在固定的时间点或时间间隔内将主库中的数据同步到备库中。这种方式可以避免主库的写入延迟，同时保证备库中的数据与主库中的数据一致，但是可能会存在备库中数据的滞后问题。  

* 数据订阅  
&emsp; 数据订阅是指使用MySQL的复制功能，将主库的数据实时复制到备库中。这种方式可以保证备库中的数据与主库中的数据实时一致，但是会增加主库的读取压力，并且可能存在网络延迟等问题。  

## 1.2. 分页3种方案

* from/size方案的优点是简单，缺点是在深度分页的场景下系统开销比较大，占用较多内存。  
* scroll方案也很高效，但是它基于快照，不能用在实时性高的业务场景，建议用在类似报表导出，或者ES内部的reindex等场景。  
* search after基于ES内部排序好的游标，可以实时高效的进行分页查询，但是它只能做下一页这样的查询场景，不能随机的指定页数查询。  

```java
//1. 创建SearchRequest
SearchRequest request = new SearchRequest(index);
request.types(type);

//2. 指定scroll信息
request.scroll(TimeValue.timeValueMinutes(1L));

//3. 指定查询条件
SearchSourceBuilder builder = new SearchSourceBuilder();
builder.size(2);
builder.sort("fee", SortOrder.DESC);
builder.query(QueryBuilders.matchAllQuery());

request.source(builder);

//4. 获取返回结果scrollId，source
SearchResponse resp = client.search(request, RequestOptions.DEFAULT);

```


## 1.3. 使用ES中的一些问题  
### 1.3.1. cpu使用率过高  
&emsp; cpu使用率，该如何调节呢。cpu使用率高，有可能是写入导致的，也有可能是查询导致的，那要怎么查看呢？  
&emsp; 可以先通过 GET \_nodes/{node}/hot\_threads 查看线程栈，查看是哪个线程占用 cpu 高，如果是 elasticsearch[\{node}][search][T#10] 则是查询导致的，如果是 elasticsearch[\{node}][bulk][T#1] 则是数据写入导致的。  
&emsp; 在实际调优中，cpu 使用率很高，如果不是 SSD，建议把 index.merge.scheduler.max_thread_count: 1 索引 merge 最大线程数设置为 1 个，该参数可以有效调节写入的性能。因为在存储介质上并发写，由于寻址的原因，写入性能不会提升，只会降低。  


### 1.3.2. Elasticsearch OOM  
<!-- 
公司银子不多，遇到Elasticsearch OOM（内存溢出），除了瞪白眼，还能干啥... 
https://mp.weixin.qq.com/s/W3mSgShSgoqkGz6otZRE5Q
-->
&emsp; ......

### 1.3.3. 聚合数据结果不精确  
<!-- 
 Elasticsearch 聚合数据结果不精确，怎么破？ 
https://mp.weixin.qq.com/s/V4cGqvkQ7-DgeSvPSketgQ
-->