
<!-- TOC -->

- [1. 索引基本操作](#1-索引基本操作)
    - [1.1. 索引增删改查](#11-索引增删改查)
    - [1.2. Open/Close Index打开/关闭索引](#12-openclose-index打开关闭索引)
    - [1.3. 索引映射管理](#13-索引映射管理)
    - [1.4. 索引别名](#14-索引别名)
        - [1.4.1. 索引别名操作](#141-索引别名操作)
        - [1.4.2. ★★★Rollover Index别名滚动指向新创建的索引](#142-★★★rollover-index别名滚动指向新创建的索引)
    - [1.5. 索引配置](#15-索引配置)
        - [1.5.1. 更新索引配置](#151-更新索引配置)
        - [1.5.2. 获取配置](#152-获取配置)
        - [1.5.3. 索引分析](#153-索引分析)
        - [1.5.4. ★★★索引模板](#154-★★★索引模板)
        - [1.5.5. 重建索引](#155-重建索引)
            - [1.5.5.1. ★★★Shrink Index收缩索引](#1551-★★★shrink-index收缩索引)
            - [1.5.5.2. Split Index拆分索引](#1552-split-index拆分索引)

<!-- /TOC -->


&emsp; **<font color = "clime">ES的rollover index API，可以根据满足指定的条件（时间、文档数量、索引大小）创建新的索引，并把别名滚动指向新的索引。</font>**   



# 1. 索引基本操作
<!-- 
~~
ElasticSearch 索引基本操作～ 
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247490712&idx=1&sn=b10f393152120d2e985b5e550850ad30&scene=21#wechat_redirect
索引管理  
https://mp.weixin.qq.com/s/gi9Dxt23chmEgDK9ZWfHLw
-->
&emsp; **<font color = "red">部分参考《Elasticsearch技术解析与实战》</font>**  


## 1.1. 索引增删改查  
&emsp; 创建一个名为twitter的索引，设置索引的分片数为3，备份数为2。  

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
&emsp; 默认的分片数是5到1024。默认的备份数是1。  
&emsp; 索引的名称必须是小写的，不可重名。  

&emsp; 创建结果：  
&emsp; ![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-25.png)  

&emsp; 创建的命令还可以简写为：  

```text
PUT twitter
{
    "settings" : {
        "number_of_shards" : 3,
        "number_of_replicas" : 2
    }
}
```

&emsp; **创建mapping映射：**  
&emsp; `注意：在ES中创建一个mapping映射类似于在数据库中定义表结构，即表里面有哪些字段、字段是什么类型、字段的默认值等。`  

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

&emsp; **创建索引时加入别名定义：**  

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

&emsp; 创建索引时返回的结果说明：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-26.png)  

&emsp; **Get Index查看索引的定义信息**  
&emsp; GET /twitter，可以一次获取多个索引(以逗号间隔)，获取所有索引_all或用通配符 *

```text
GET /twitter/_settings
GET /twitter/_mapping
```
 
&emsp; **删除索引**

```text
DELETE /twitter
```

&emsp; 说明：可以一次删除多个索引(以逗号间隔)，删除所有索引_all或通配符 *

&emsp; **判断索引是否存在**  

```text
HEAD twitter
```

&emsp; HTTP status code表示结果：404 不存在，200 存在

&emsp; **修改索引的settings信息**  
&emsp; 索引的设置信息分为静态信息和动态信息两部分。静态信息不可更改，如索引的分片数。动态信息可以修改。  
&emsp; REST访问端点：/_settings，更新所有索引的settings信息； {index}/_settings更新一个或多个索引的settings。  
&emsp; 详细的设置项请参考：https://www.elastic.co/guide/en/elasticsearch/reference/current/index-modules.html#index-modules-settings

&emsp; **修改备份数**

```text
PUT /twitter/_settings
{
    "index" : {
        "number_of_replicas" : 2
    }
}
```

&emsp; **设置回默认值，用null**  

```text
PUT /twitter/_settings
{
    "index" : {
        "refresh_interval" : null
    }
}
```

&emsp; **设置索引的读写**  

```text
index.blocks.read_only：设为true,则索引以及索引的元数据只可读
index.blocks.read_only_allow_delete：设为true，只读时允许删除。
index.blocks.read：设为true，则不可读。
index.blocks.write：设为true，则不可写。
index.blocks.metadata：设为true，则索引元数据不可读写。
```


## 1.2. Open/Close Index打开/关闭索引  

```text
POST /my_index/_close
POST /my_index/_open
```
&emsp; 说明：  
&emsp; 关闭的索引不能进行读写操作，几乎不占集群开销。  
&emsp; 关闭的索引可以打开，打开走的是正常的恢复流程。

## 1.3. 索引映射管理  
&emsp; ......

## 1.4. 索引别名  
### 1.4.1. 索引别名操作

### 1.4.2. ★★★Rollover Index别名滚动指向新创建的索引
&emsp; **对于有时效性的索引数据，如日志，过一定时间后，老的索引数据就没有用了。可以像数据库中根据时间创建表来存放不同时段的数据一样，在ES中也可用建多个索引的方式来分开存放不同时段的数据。比数据库中更方便的是ES中可以通过别名滚动指向最新的索引的方式，通过别名来操作时总是操作的最新的索引。**  
&emsp; **<font color = "clime">ES的rollover index API，可以根据满足指定的条件（时间、文档数量、索引大小）创建新的索引，并把别名滚动指向新的索引。</font>**  
&emsp; 注意：这时的别名只能是一个索引的别名。  

&emsp; Rollover Index示例：  
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
&emsp; 如果别名logs_write指向的索引是7天前（含）创建的或索引的文档数>=1000或索引的大小>= 5gb，则会创建一个新索引 logs-000002，并把别名logs_writer指向新创建的logs-000002索引。  
&emsp; Rollover Index新建索引的命名规则：  
&emsp; 如果索引的名称是-数字结尾，如logs-000001，则新建索引的名称也会是这个模式，数值增1。  
&emsp; 如果索引的名称不是-数值结尾，则在请求rollover api时需指定新索引的名称。

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
&emsp; 注意：rollover是请求它才会进行操作，并不是自动在后台进行的。可以周期性地去请求它。  

## 1.5. 索引配置  
<!-- 
Elasticsearch技术解析与实战 第2.4章
-->
&emsp; 在Elasticsearch中索引有很多的配置参数，有些配置是可以在建好索引后重新进行设置和管理的，比如索引的副本数量、索引的分词等。  

### 1.5.1. 更新索引配置  

### 1.5.2. 获取配置  

### 1.5.3. 索引分析  


### 1.5.4. ★★★索引模板
<!-- 
https://www.cnblogs.com/shoufeng/p/10641560.html
-->
&emsp; 在创建索引时，为每个索引写定义信息可能是一件繁琐的事情，ES提供了索引模板功能，可以定义一个索引模板， **模板中定义好settings、mapping、以及一个模式定义来匹配创建的索引。**  
&emsp; <font color = "clime">注意：模板只在索引创建时被参考，修改模板不会影响已创建的索引。</font>  

&emsp; 新增/修改名为tempae_1的模板，匹配名称为te\*或bar\*的索引创建：  

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

------------

&emsp; 索引模版Templates——允许定义在创建新索引时自动应用模板。模板包括settings和Mappings以及控制是否应将模板应用于新索引。  
&emsp; 注意：模板仅在索引创建时应用。更改模板不会对现有索引产生影响。  
&emsp; 针对大索引，使用模板是必须的。核心需要设置的setting(仅列举了实战中最常用、可以动态修改的)如下：  

* index.numberofreplicas 每个主分片具有的副本数。默认为 1(7.X 版本，低于 7.X 为 5)。
* index.maxresultwindow 深度分页 from + size 的最大值—— 默认为 10000。
* index.refresh_interval 默认 1s：代表最快 1s 搜索可见；

&emsp; 写入时候建议设置为 -1，提高写入性能；  
&emsp; 实战业务如果对实时性要求不高，建议设置为 30s 或者更高。  

&emsp; **包含Mapping的template设计万能模板**  
&emsp; 以下模板已经在 7.2 验证 ok，可以直接拷贝修改后实战项目中使用。  

```text
PUT _template/test_template
{
  "index_patterns": [
    "test_index_*",
    "test_*"
  ],
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 1,
    "max_result_window": 100000,
    "refresh_interval": "30s"
  },
  "mappings": {
    "properties": {
      "id": {
        "type": "long"
      },
      "title": {
        "type": "keyword"
      },
      "content": {
        "analyzer": "ik_max_word",
        "type": "text",
        "fields": {
          "keyword": {
            "ignore_above": 256,
            "type": "keyword"
          }
        }
      },
      "available": {
        "type": "boolean"
      },
      "review": {
        "type": "nested",
        "properties": {
          "nickname": {
            "type": "text"
          },
          "text": {
            "type": "text"
          },
          "stars": {
            "type": "integer"
          }
        }
      },
      "publish_time": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
      },
      "expected_attendees": {
        "type": "integer_range"
      },
      "ip_addr": {
        "type": "ip"
      },
      "suggest": {
        "type": "completion"
      }
    }
  }
}
```

----
&emsp; 以下的索引Mapping中，_source设置为false，同时各个字段的store根据需求设置了true和false。  
&emsp; url的doc_values设置为false，该字段url不用于聚合和排序操作。  

```text
PUT blog_index
{
  "mappings": {
    "doc": {
      "_source": {
        "enabled": false
      },
      "properties": {
        "title": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 100
            }
          },
          "store": true
        },
        "publish_date": {
          "type": "date",
          "store": true
        },
        "author": {
          "type": "keyword",
          "ignore_above": 100, 
          "store": true
        },
        "abstract": {
          "type": "text",
          "store": true
        },
        "content": {
          "type": "text",
          "store": true
        },
        "url": {
          "type": "keyword",
          "doc_values":false,
          "norms":false,
          "ignore_above": 100, 
          "store": true
        }
      }
    }
  }
}
```


### 1.5.5. 重建索引   

#### 1.5.5.1. ★★★Shrink Index收缩索引
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

#### 1.5.5.2. Split Index拆分索引
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
