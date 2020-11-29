
<!-- TOC -->

- [1. 索引详解](#1-索引详解)
    - [1.1. 索引操作](#11-索引操作)
        - [1.1.1. 索引增删改查](#111-索引增删改查)
        - [1.1.2. 索引模板](#112-索引模板)
        - [1.1.3. Open/Close Index打开/关闭索引](#113-openclose-index打开关闭索引)
        - [1.1.4. Shrink Index 收缩索引](#114-shrink-index-收缩索引)
        - [1.1.5. Split Index 拆分索引](#115-split-index-拆分索引)
        - [1.1.6. Rollover Index别名滚动指向新创建的索引](#116-rollover-index别名滚动指向新创建的索引)
    - [1.2. 索引监控](#12-索引监控)
    - [1.3. 索引状态管理](#13-索引状态管理)

<!-- /TOC -->

# 1. 索引详解  
<!-- 
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247490712&idx=1&sn=b10f393152120d2e985b5e550850ad30&scene=21#wechat_redirect 
索引管理  
https://mp.weixin.qq.com/s/gi9Dxt23chmEgDK9ZWfHLw
-->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-23.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-24.png)  

## 1.1. 索引操作
### 1.1.1. 索引增删改查  
&emsp; 创建一个名为twitter的索引，设置索引的分片数为3，备份数为2。注意：在ES中创建一个索引类似于在数据库中建立一个数据库(ES6.0之后类似于创建一个表)  

```text
PUT twitter
{
    "settings" : {
        "index" : {
            "number_of_shards" : 3,
            "number_of_replicas" : 2
        }
    }
}
```
&emsp; 说明：  
&emsp; 默认的分片数是5到1024  
&emsp; 默认的备份数是1  
&emsp; 索引的名称必须是小写的，不可重名  

&emsp; 创建结果：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-25.png)  

&emsp; 创建的命令还可以简写为  

```text
PUT twitter
{
    "settings" : {
        "number_of_shards" : 3,
        "number_of_replicas" : 2
    }
}
```

&emsp; 创建mapping映射  
&emsp; 注意：在ES中创建一个mapping映射类似于在数据库中定义表结构，即表里面有哪些字段、字段是什么类型、字段的默认值等；也类似于solr里面的模式schema的定义  

```text
PUT twitter
{
    "settings" : {
        "index" : {
            "number_of_shards" : 3,
            "number_of_replicas" : 2
        }
    },
   "mappings" : {
        "type1" : {
            "properties" : {
                "field1" : { "type" : "text" }
            }
        }
    }
}
```

&emsp; 创建索引时加入别名定义

```text
PUT twitter
{
    "aliases" : {
        "alias_1" : {},
        "alias_2" : {
            "filter" : {
                "term" : {"user" : "kimchy" }
            },
            "routing" : "kimchy"
        }
    }
}
```

&emsp; 创建索引时返回的结果说明  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-26.png)  

&emsp; Get Index查看索引的定义信息
&emsp; GET /twitter，可以一次获取多个索引（以逗号间隔） 获取所有索引 _all 或 用通配符*

```text
GET /twitter/_settings
GET /twitter/_mapping
```
 
&emsp; 删除索引

```text
DELETE /twitter
```

&emsp; 说明：  
&emsp; 可以一次删除多个索引（以逗号间隔） 删除所有索引 _all 或 通配符 *

&emsp; 判断索引是否存在  

```text
HEAD twitter
```

&emsp; HTTP status code 表示结果 404 不存在 ， 200 存在

&emsp; 修改索引的settings信息  
&emsp; 索引的设置信息分为静态信息和动态信息两部分。静态信息不可更改，如索引的分片数。动态信息可以修改。  
&emsp; REST 访问端点：  
&emsp; /_settings 更新所有索引的。  
&emsp; {index}/_settings 更新一个或多个索引的settings。  
&emsp; 详细的设置项请参考：https://www.elastic.co/guide/en/elasticsearch/reference/current/index-modules.html#index-modules-settings

&emsp; 修改备份数

```text
PUT /twitter/_settings
{
    "index" : {
        "number_of_replicas" : 2
    }
}
```

&emsp; 设置回默认值，用null  

```text
PUT /twitter/_settings
{
    "index" : {
        "refresh_interval" : null
    }
}
```

&emsp; 设置索引的读写  

```text
index.blocks.read_only：设为true,则索引以及索引的元数据只可读
index.blocks.read_only_allow_delete：设为true，只读时允许删除。
index.blocks.read：设为true，则不可读。
index.blocks.write：设为true，则不可写。
index.blocks.metadata：设为true，则索引元数据不可读写。
```

### 1.1.2. 索引模板
&emsp; 在创建索引时，为每个索引写定义信息可能是一件繁琐的事情，ES提供了索引模板功能，让你可以定义一个索引模板，模板中定义好settings、mapping、以及一个模式定义来匹配创建的索引。  
&emsp; 注意：模板只在索引创建时被参考，修改模板不会影响已创建的索引

&emsp; 新增/修改名为tempae_1的模板，匹配名称为te* 或 bar*的索引创建：  

```text
PUT _template/template_1
{
  "index_patterns": ["te*", "bar*"],
  "settings": {
    "number_of_shards": 1
  },
  "mappings": {
    "type1": {
      "_source": {
        "enabled": false
      },
      "properties": {
        "host_name": {
          "type": "keyword"
        },
        "created_at": {
          "type": "date",
          "format": "EEE MMM dd HH:mm:ss Z YYYY"
        }
      }
    }
  }
}
```

&emsp; 查看索引模板  

```text
GET /_template/template_1
GET /_template/temp* 
GET /_template/template_1,template_2
GET /_template
```

&emsp; 删除模板  

```text
DELETE /_template/template_1
```

### 1.1.3. Open/Close Index打开/关闭索引  

```text
POST /my_index/_close
POST /my_index/_open
```
&emsp; 说明：  
&emsp; 关闭的索引不能进行读写操作，几乎不占集群开销。  
&emsp; 关闭的索引可以打开，打开走的是正常的恢复流程。

### 1.1.4. Shrink Index 收缩索引
&emsp; 索引的分片数是不可更改的，如要减少分片数可以通过收缩方式收缩为一个新的索引。新索引的分片数必须是原分片数的因子值，如原分片数是8，则新索引的分片数可以为4、2、1 。  
&emsp; 什么时候需要收缩索引呢?  
&emsp; 最初创建索引的时候分片数设置得太大，后面发现用不了那么多分片，这个时候就需要收缩了  

&emsp; 收缩的流程：  

1. 先把所有主分片都转移到一台主机上；
2. 在这台主机上创建一个新索引，分片数较小，其他设置和原索引一致；
3. 把原索引的所有分片，复制（或硬链接）到新索引的目录下；
4. 对新索引进行打开操作恢复分片数据；
5. (可选)重新把新索引的分片均衡到其他节点上。

&emsp; 收缩前的准备工作：  
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

### 1.1.5. Split Index 拆分索引
&emsp; 当索引的分片容量过大时，可以通过拆分操作将索引拆分为一个倍数分片数的新索引。能拆分为几倍由创建索引时指定的index.number_of_routing_shards 路由分片数决定。这个路由分片数决定了根据一致性hash路由文档到分片的散列空间。  
&emsp; 如index.number_of_routing_shards = 30 ，指定的分片数是5，则可按如下倍数方式进行拆分：  

```text
5 → 10 → 30 (split by 2, then by 3)
5 → 15 → 30 (split by 3, then by 2)
5 → 30 (split by 6)
```

&emsp; 为什么需要拆分索引？  
&emsp; 当最初设置的索引的分片数不够用时就需要拆分索引了，和压缩索引相反  
&emsp; 注意：只有在创建时指定了index.number_of_routing_shards 的索引才可以进行拆分，ES7开始将不再有这个限制。  

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

### 1.1.6. Rollover Index别名滚动指向新创建的索引
&emsp; 对于有时效性的索引数据，如日志，过一定时间后，老的索引数据就没有用了。我们可以像数据库中根据时间创建表来存放不同时段的数据一样，在ES中也可用建多个索引的方式来分开存放不同时段的数据。比数据库中更方便的是ES中可以通过别名滚动指向最新的索引的方式，让你通过别名来操作时总是操作的最新的索引。  
&emsp; ES的rollover index API 让我们可以根据满足指定的条件（时间、文档数量、索引大小）创建新的索引，并把别名滚动指向新的索引。  
&emsp; 注意：这时的别名只能是一个索引的别名。  

&emsp; Rollover Index 示例：  
&emsp; 创建一个名字为logs-0000001 、别名为logs_write 的索引：  

```text
PUT /logs-000001
{
  "aliases": {
    "logs_write": {}
  }
}
```

&emsp; 添加1000个文档到索引logs-000001，然后设置别名滚动的条件  

```text
POST /logs_write/_rollover
{
  "conditions": {
    "max_age":   "7d",
    "max_docs":  1000,
    "max_size":  "5gb"
  }
}
```
&emsp; 说明：  
&emsp; 如果别名logs_write指向的索引是7天前（含）创建的或索引的文档数>=1000或索引的大小>= 5gb，则会创建一个新索引 logs-000002，并把别名logs_writer指向新创建的logs-000002索引  
&emsp; Rollover Index 新建索引的命名规则：  
&emsp; 如果索引的名称是-数字结尾，如logs-000001，则新建索引的名称也会是这个模式，数值增1。  
&emsp; 如果索引的名称不是-数值结尾，则在请求rollover api时需指定新索引的名称

```text
POST /my_alias/_rollover/my_new_index_name
{
  "conditions": {
    "max_age":   "7d",
    "max_docs":  1000,
    "max_size": "5gb"
  }
}
```

&emsp; 在名称中使用Date math（时间表达式）  
&emsp; 如果希望生成的索引名称中带有日期，如logstash-2016.02.03-1 ，则可以在创建索引时采用时间表达式来命名：  

```text
# PUT /<logs-{now/d}-1> with URI encoding:
PUT /%3Clogs-%7Bnow%2Fd%7D-1%3E
{
  "aliases": {
    "logs_write": {}
  }
}
PUT logs_write/_doc/1
{
  "message": "a dummy log"
} 
POST logs_write/_refresh
# Wait for a day to pass
POST /logs_write/_rollover
{
  "conditions": {
    "max_docs":   "1"
  }
}
```

&emsp; Rollover时可对新的索引作定义：  

```text
PUT /logs-000001
{
  "aliases": {
    "logs_write": {}
  }
}
POST /logs_write/_rollover
{
  "conditions" : {
    "max_age": "7d",
    "max_docs": 1000,
    "max_size": "5gb"
  },
  "settings": {
    "index.number_of_shards": 2
  }
}
```

&emsp; Dry run实际操作前先测试是否达到条件：  

```text
POST /logs_write/_rollover?dry_run
{
  "conditions" : {
    "max_age": "7d",
    "max_docs": 1000,
    "max_size": "5gb"
  }
}
```

&emsp; 说明：  
&emsp; 测试不会创建索引，只是检测条件是否满足  
&emsp; 注意：rollover是你请求它才会进行操作，并不是自动在后台进行的。你可以周期性地去请求它。  

## 1.2. 索引监控
&emsp; 查看索引状态信息  
&emsp; 官网链接：  
https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-stats.html  

&emsp; 查看所有的索引状态：  

```text
GET /_stats  
```

&emsp; 查看指定索引的状态信息：  

```text
GET /index1,index2/_stats  
```

&emsp; 查看索引段信息  

&emsp; 官网链接：
https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-segments.html  

```text
GET /test/_segments 
GET /index1,index2/_segments
GET /_segments
```

&emsp; 查看索引恢复信息  
&emsp; 官网链接：
https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-recovery.html

&emsp; GET index1,index2/_recovery?human  
&emsp; GET /_recovery?human  

&emsp; 查看索引分片的存储信息  
&emsp; 官网链接：
https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-shards-stores.html

```text
# return information of only index test
GET /test/_shard_stores
# return information of only test1 and test2 indices
GET /test1,test2/_shard_stores
# return information of all indices
GET /_shard_stores
  GET /_shard_stores?status=green
```

## 1.3. 索引状态管理
&emsp; Clear Cache 清理缓存  

```text
POST /twitter/_cache/clear  
```

&emsp; 默认会清理所有缓存，可指定清理query, fielddata or request 缓存  

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
