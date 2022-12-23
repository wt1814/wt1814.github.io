
<!-- TOC -->

- [1. 索引基本操作](#1-索引基本操作)
    - [1.1. 索引增删改查](#11-索引增删改查)
    - [1.2. 索引设计](#12-索引设计)
        - [1.2.1. 索引映射](#121-索引映射)
        - [1.2.2. ★★★索引模板](#122-★★★索引模板)
        - [1.2.3. 索引别名](#123-索引别名)
            - [1.2.3.1. 索引别名操作](#1231-索引别名操作)
            - [1.2.3.2. ★★★Rollover Index别名滚动指向新创建的索引](#1232-★★★rollover-index别名滚动指向新创建的索引)

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
&emsp; ![image](http://182.92.69.8:8081/img/ES/es-25.png)  

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
![image](http://182.92.69.8:8081/img/ES/es-26.png)  

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

## 1.2. 索引设计
### 1.2.1. 索引映射  
&emsp; ......






### 1.2.2. ★★★索引模板
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

### 1.2.3. 索引别名  
#### 1.2.3.1. 索引别名操作

#### 1.2.3.2. ★★★Rollover Index别名滚动指向新创建的索引
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
