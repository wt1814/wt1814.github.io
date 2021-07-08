
<!-- TOC -->

- [1. 索引维护](#1-索引维护)
    - [1.1. 索引监控](#11-索引监控)
        - [1.1.1. 索引统计](#111-索引统计)
        - [1.1.2. 索引分片](#112-索引分片)
        - [1.1.3. 索引恢复](#113-索引恢复)
        - [1.1.4. 索引分片存储](#114-索引分片存储)
    - [1.2. 索引状态管理](#12-索引状态管理)

<!-- /TOC -->


# 1. 索引维护
## 1.1. 索引监控
### 1.1.1. 索引统计  
&emsp; **查看索引状态信息**  
&emsp; 官网链接：https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-stats.html  

&emsp; 查看所有的索引状态：  

```text
GET /_stats  
```

&emsp; 查看指定索引的状态信息：  

```text
GET /index1,index2/_stats  
```

&emsp; **查看索引段信息**  
&emsp; 官网链接：https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-segments.html  

```text
GET /test/_segments 
GET /index1,index2/_segments
GET /_segments
```

### 1.1.2. 索引分片

### 1.1.3. 索引恢复
&emsp; **查看索引恢复信息**  
&emsp; 官网链接：https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-recovery.html

&emsp; GET index1,index2/_recovery?human  
&emsp; GET /_recovery?human  

### 1.1.4. 索引分片存储
&emsp; **查看索引分片的存储信息**  
&emsp; 官网链接：https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-shards-stores.html

```text
# return information of only index test
GET /test/_shard_stores
# return information of only test1 and test2 indices
GET /test1,test2/_shard_stores
# return information of all indices
GET /_shard_stores
GET /_shard_stores?status=green
```

## 1.2. 索引状态管理
&emsp; Clear Cache清理缓存  

```text
POST /twitter/_cache/clear  
```

&emsp; 默认会清理所有缓存，可指定清理query, fielddata or request缓存  

```text
POST /kimchy,elasticsearch/_cache/clear
POST /_cache/clear
```

&emsp; Refresh，重新打开读取索引  

```text
POST /kimchy,elasticsearch/_refresh
POST /_refresh
```

&emsp; Flush，将缓存在内存中的索引数据刷新到持久存储中  

```text
POST twitter/_flush
```

&emsp; Force merge 强制段合并  

```text
POST /kimchy/_forcemerge?only_expunge_deletes=false&max_num_segments=100&flush=true
```

&emsp; 可选参数说明：  
&emsp; max_num_segments 合并为几个段，默认1  
&emsp; only_expunge_deletes 是否只合并含有删除文档的段，默认false  
&emsp; flush 合并后是否刷新，默认true  

```text
POST /kimchy,elasticsearch/_forcemerge
POST /_forcemerge
```
