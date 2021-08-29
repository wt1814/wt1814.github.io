
<!-- TOC -->

- [1. 索引维护](#1-索引维护)
    - [1.1. 索引配置](#11-索引配置)
        - [1.1.1. Open/Close Index打开/关闭索引](#111-openclose-index打开关闭索引)
        - [1.1.2. 获取配置](#112-获取配置)
        - [1.1.3. 更新索引配置](#113-更新索引配置)
        - [1.1.4. 索引状态管理](#114-索引状态管理)
    - [1.2. 索引监控](#12-索引监控)
        - [1.2.1. 索引统计](#121-索引统计)
        - [1.2.2. 索引分片](#122-索引分片)
        - [1.2.3. 索引恢复](#123-索引恢复)
        - [1.2.4. 索引分片存储](#124-索引分片存储)
    - [1.3. 重建索引](#13-重建索引)
        - [1.3.1. ★★★Shrink Index收缩索引](#131-★★★shrink-index收缩索引)
        - [1.3.2. Split Index拆分索引](#132-split-index拆分索引)

<!-- /TOC -->


# 1. 索引维护
<!-- 

开发前---开发中---维护
-->

## 1.1. 索引配置  
<!-- 
Elasticsearch技术解析与实战 第2.4章
-->
&emsp; 在Elasticsearch中索引有很多的配置参数，有些配置是可以在建好索引后重新进行设置和管理的，比如索引的副本数量、索引的分词等。  


### 1.1.1. Open/Close Index打开/关闭索引  

```text
POST /my_index/_close
POST /my_index/_open
```
&emsp; 说明：  
&emsp; 关闭的索引不能进行读写操作，几乎不占集群开销。  
&emsp; 关闭的索引可以打开，打开走的是正常的恢复流程。

### 1.1.2. 获取配置  

### 1.1.3. 更新索引配置  




### 1.1.4. 索引状态管理
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


## 1.2. 索引监控
### 1.2.1. 索引统计  
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

### 1.2.2. 索引分片

### 1.2.3. 索引恢复
&emsp; **查看索引恢复信息**  
&emsp; 官网链接：https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-recovery.html

&emsp; GET index1,index2/_recovery?human  
&emsp; GET /_recovery?human  

### 1.2.4. 索引分片存储
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





## 1.3. 重建索引   

### 1.3.1. ★★★Shrink Index收缩索引
&emsp; 索引的分片数是不可更改的，如要减少分片数可以通过收缩方式收缩为一个新的索引。新索引的分片数必须是原分片数的因子值，如原分片数是8，则新索引的分片数可以为4、2、1 。  
&emsp; 什么时候需要收缩索引呢?  
&emsp; 最初创建索引的时候分片数设置得太大，后面发现用不了那么多分片，这个时候就需要收缩了。  

&emsp; **收缩的流程：**  

1. 先把所有主分片都转移到一台主机上；
2. 在这台主机上创建一个新索引，分片数较小，其他设置和原索引一致；
3. 把原索引的所有分片，复制（或硬链接）到新索引的目录下；
4. 对新索引进行打开操作恢复分片数据；
5. (可选)重新把新索引的分片均衡到其他节点上。

&emsp; **收缩前的准备工作：**  
1. 将原索引设置为只读；
2. 将原索引各分片的一个副本重分配到同一个节点上，并且要是健康绿色状态。

```text
PUT /my_source_index/_settings
{
  "settings": {
    <!-- 指定进行收缩的节点的名称 -->
    "index.routing.allocation.require._name": "shrink_node_name",
    <!-- 阻止写，只读 -->
     "index.blocks.write": true
  }
}
```

&emsp; 进行收缩：  

```text
POST my_source_index/_shrink/my_target_index
{
  "settings": {
    "index.number_of_replicas": 1,
    "index.number_of_shards": 1,
    "index.codec": "best_compression"
  }}
```

&emsp; 监控收缩过程：  

```text
GET _cat/recovery?v
GET _cluster/health  
```

### 1.3.2. Split Index拆分索引
&emsp; **当索引的分片容量过大时，可以通过拆分操作将索引拆分为一个倍数分片数的新索引。** 能拆分为几倍由创建索引时指定的index.number_of_routing_shards路由分片数决定。这个路由分片数决定了根据一致性hash路由文档到分片的散列空间。  
&emsp; 如index.number_of_routing_shards = 30，指定的分片数是5，则可按如下倍数方式进行拆分：  

```text
5 → 10 → 30 (split by 2, then by 3)
5 → 15 → 30 (split by 3, then by 2)
5 → 30 (split by 6)
```

&emsp; 为什么需要拆分索引？  
&emsp; 当最初设置的索引的分片数不够用时就需要拆分索引了，和压缩索引相反。  
&emsp; 注意：只有在创建时指定了index.number_of_routing_shards的索引才可以进行拆分，ES7开始将不再有这个限制。  

&emsp; 和solr的区别是，solr是对一个分片进行拆分，es中是整个索引进行拆分。  
&emsp; 拆分步骤：  
&emsp; 准备一个索引来做拆分：  

```text
PUT my_source_index
{
    "settings": {
        "index.number_of_shards" : 1,
        <!-- 创建时需要指定路由分片数 -->
        "index.number_of_routing_shards" : 2
    }
}
```

&emsp; 先设置索引只读：  

```text
PUT /my_source_index/_settings
{
  "settings": {
    "index.blocks.write": true
  }
}
```

&emsp; 做拆分：  

```text
POST my_source_index/_split/my_target_index
{
  "settings": {
    <!--新索引的分片数需符合拆分规则-->
    "index.number_of_shards": 2
  }
}
```

&emsp; 监控拆分过程：  

```text
GET _cat/recovery?v
GET _cluster/health
```
