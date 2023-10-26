
<!-- TOC -->

- [1. 分页查询(避免深度分页)](#1-分页查询避免深度分页)
    - [1.1. 深度分页问题](#11-深度分页问题)
    - [1.2. 分页3种方案](#12-分页3种方案)

<!-- /TOC -->


# 1. 分页查询(避免深度分页)  

<!--
https://mp.weixin.qq.com/s/BmgvAHJBoLVPlRDbDPppdg
https://www.cnblogs.com/jpfss/p/10815206.html

https://blog.csdn.net/pony_maggie/article/details/105478557
https://www.cnblogs.com/hello-shf/p/11543453.html
https://mp.weixin.qq.com/s/RhkLwO1X28gIbrXGH_Cl5g

三种分页方式
https://mp.weixin.qq.com/s/3sJajydXPmfLP3KXMs0Gmw
*****https://mp.weixin.qq.com/s/YDewi8XUjff7knzkBRjnrg

-->

## 1.1. 深度分页问题
```
org.elasticsearch.ElasticsearchStatusException: Elasticsearch exception [type=search_phase_execution_exception, reason=all shards failed]
        at org.elasticsearch.rest.BytesRestResponse.errorFromXContent(BytesRestResponse.java:177)
        at org.elasticsearch.client.RestHighLevelClient.parseEntity(RestHighLevelClient.java:618)
        at org.elasticsearch.client.RestHighLevelClient.parseResponseException(RestHighLevelClient.java:594)
        at org.elasticsearch.client.RestHighLevelClient.performRequest(RestHighLevelClient.java:501)
        at org.elasticsearch.client.RestHighLevelClient.performRequestAndParseEntity(RestHighLevelClient.java:474)
        at org.elasticsearch.client.RestHighLevelClient.search(RestHighLevelClient.java:391)
        at com.xxxx.assets.service.es.factory.rest.EsHighClientService.queryByPage(EsHighClientService.java:82)
        ... 21 common frames omitted
        Suppressed: org.elasticsearch.client.ResponseException: method [POST], host [http://abc.xxxx.com:9900], URI [/blood_relation_index/blood_relation/_search?typed_keys=true&ignore_unavailable=false&expand_wildcards=open&allow_no_indices=true&search_type=dfs_query_then_fetch&batched_reduce_size=512], status line [HTTP/1.1 500 Internal Server Error]{"error":{"root_cause":[{"type":"query_phase_execution_exception","reason":"Result window is too large, from + size must be less than or equal to: [10000] but was [20000]. See the scroll api for a more efficient way to request large data sets. This limit can be set by changing the [index.max_result_window] index level setting."}],"type":"search_phase_execution_exception","reason":"all shards failed","phase":"query","grouped":true,"failed_shards":[{"shard":0,"index":"blood_relation_index","node":"RKah0wB7RDeQMmmawJqMHA","reason":{"type":"query_phase_execution_exception","reason":"Result window is too large, from + size must be less than or equal to: [10000] but was [20000]. See the scroll api for a more efficient way to request large data sets. This limit can be set by changing the [index.max_result_window] index level setting."}}]},"status":500}
```

&emsp; **<font color = "red">分页查询中from+size 必须<=\[10000]。</font>**  
&emsp; 请求大数据集的更有效的方式可参阅scroll(游标)或search after。也可通过更改\[index.max_result_window] 进行设置。  

## 1.2. 分页3种方案

* from/size方案的优点是简单，缺点是在深度分页的场景下系统开销比较大，占用较多内存。  
* scroll方案也很高效，但是它基于快照，不能用在实时性高的业务场景，建议用在类似报表导出，或者ES内部的reindex等场景。  
* search after基于ES内部排序好的游标，可以实时高效的进行分页查询，但是它只能做下一页这样的查询场景，不能随机的指定页数查询。  