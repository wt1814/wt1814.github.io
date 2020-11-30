<!-- TOC -->

- [1. ES建模](#1-es建模)
    - [1.1. PB级别的大索引如何设计？](#11-pb级别的大索引如何设计)
        - [1.1.1. 大索引的缺陷](#111-大索引的缺陷)
            - [1.1.1.1. 存储大小限制维度](#1111-存储大小限制维度)
            - [1.1.1.2. 性能维度](#1112-性能维度)
            - [1.1.1.3. 风险维度](#1113-风险维度)
        - [1.1.2. PB级索引设计实现](#112-pb级索引设计实现)
            - [1.1.2.1. 使用模板统一配置索引](#1121-使用模板统一配置索引)
            - [1.1.2.2. 使用Rollver增量管理索引](#1122-使用rollver增量管理索引)
            - [1.1.2.3. 索引增量更新原理](#1123-索引增量更新原理)
            - [1.1.2.4. 使用 curator 高效清理历史数据](#1124-使用-curator-高效清理历史数据)
    - [1.2. 分片数和副本数如何设计？](#12-分片数和副本数如何设计)
        - [1.2.1. 分片/副本认知](#121-分片副本认知)
        - [1.2.2. 分片和副本实战中设计](#122-分片和副本实战中设计)
            - [1.2.2.1. 问题 1：索引设置多少分片？](#1221-问题-1索引设置多少分片)
            - [1.2.2.2. 问题 2：索引设置多少副本？](#1222-问题-2索引设置多少副本)
    - [1.3. Mapping 如何设计？](#13-mapping-如何设计)
        - [1.3.1. Mapping 认知](#131-mapping-认知)
        - [1.3.2. ES Mapping设置](#132-es-mapping设置)
        - [1.3.3. 设计Mapping的注意事项](#133-设计mapping的注意事项)
        - [1.3.4. Mapping字段的设置流程](#134-mapping字段的设置流程)
        - [1.3.5. Mapping建议结合模板定义](#135-mapping建议结合模板定义)
        - [1.3.6. 包含Mapping的template设计万能模板](#136-包含mapping的template设计万能模板)
    - [1.4. 分词的选型](#14-分词的选型)
        - [1.4.1. 坑 1：分词选型](#141-坑-1分词选型)
        - [1.4.2. 坑 2：ik 要装集群的所有机器吗？](#142-坑-2ik-要装集群的所有机器吗)
        - [1.4.3. 坑 3：ik 匹配不到怎么办？](#143-坑-3ik-匹配不到怎么办)
    - [1.5. ES多表关联](#15-es多表关联)
    - [1.6. 检索类型如何选型呢？](#16-检索类型如何选型呢)
    - [1.7. 多表关联如何设计？](#17-多表关联如何设计)
        - [1.7.1. 为什么会有多表关联](#171-为什么会有多表关联)
        - [1.7.2. 多表关联如何实现](#172-多表关联如何实现)
    - [1.8. 实战中遇到过的坑](#18-实战中遇到过的坑)

<!-- /TOC -->

# 1. ES建模  
<!-- 
Elasticsearch 索引设计实战指南
https://mp.weixin.qq.com/s/Fc5LhiLJIeCtstl9OFeqdQ

https://destiny1020.blog.csdn.net/article/details/47710367
https://destiny1020.blog.csdn.net/article/details/47710591

Elasticsearch系列---数据建模实战
https://mp.weixin.qq.com/s/hTGqpCl4KYXlvD74fj8l5Q

* 每天几百 GB 增量实时数据的TB级甚至PB级别的大索引如何设计？
* 分片数和副本数大小如何设计，才能提升 ES 集群的性能？
* ES 的 Mapping 该如何设计，才能保证检索的高效？
* 检索类型 term/match/matchphrase/querystring /match_phrase _prefix /fuzzy 那么多，设计阶段如何选型呢？
* 分词该如何设计，才能满足复杂业务场景需求？
* 传统数据库中的多表关联在 ES 中如何设计？
* ......
-->

<!-- 
~~
从一个实战问题再谈 Elasticsearch 数据建模 
https://mp.weixin.qq.com/s/lGrNd-O_hmlEQcTc7OykmQ
干货 | 论Elasticsearch数据建模的重要性 
https://mp.weixin.qq.com/s?__biz=MzI2NDY1MTA3OQ==&mid=2247484159&idx=1&sn=731562a8bb89c9c81b4fd6a8e92e1a99&chksm=eaa82ad7dddfa3c11e5b63a41b0e8bc10d12f1b8439398e490086ddc6b4107b7864dbb9f891a&scene=21#wechat_redirect
-->


&emsp; 数据在 Elasticsearch 怎么建模？  
&emsp; Mapping 如何设置？  
&emsp; 哪些字段需要全文检索？需要分词？哪些不需要？  
&emsp; 哪些字段需要建索引？  
&emsp; 哪些字段不需要存储？  
&emsp; 类型选择：integer 还是 keyword？  
&emsp; 哪些需要做多表关联？使用：宽表冗余存储？还是 Array，Object，Nested，Join ？都需要深入考虑。  
&emsp; 哪些字段需要预处理？  
&emsp; 预处理是通过logstash filter 环节实现？还是 Mysql 视图实现？还是 ingest 管道实现？还是其他？  
&emsp; ......  


## 1.1. PB级别的大索引如何设计？  
&emsp; 单纯的普通数据索引，如果不考虑增量数据，基本上普通索引就能够满足性能要求。  
&emsp; 通常的操作就是：  

* 步骤 1：创建索引；
* 步骤 2：导入或者写入数据；
* 步骤 3：提供查询请求访问或者查询服务。  

### 1.1.1. 大索引的缺陷  
&emsp; 如果每天亿万+的实时增量数据呢，基于以下几点原因，单个索引是无法满足要求的。在 360 技术访谈中也提到了大索引的设计的困惑。  

#### 1.1.1.1. 存储大小限制维度
&emsp; 单个分片（Shard）实际是 Lucene 的索引，单分片能存储的最大文档数是：2,147,483,519 (= Integer.MAX_VALUE - 128)。如下命令能查看全部索引的分隔分片的文档大小：  

```text
GET _cat/shards
app_index                       2 p STARTED      9443   2.8mb 127.0.0.1 Hk9wFwU
app_index                       2 r UNASSIGNED                          
app_index                       3 p STARTED      9462   2.7mb 127.0.0.1 Hk9wFwU
app_index                       3 r UNASSIGNED                          
app_index                       4 p STARTED      9520   3.5mb 127.0.0.1 Hk9wFwU
app_index                       4 r UNASSIGNED                          
app_index                       1 p STARTED      9453   2.4mb 127.0.0.1 Hk9wFwU
app_index                       1 r UNASSIGNED                          
app_index                       0 p STARTED      9365   2.3mb 127.0.0.1 Hk9wFwU
app_index                       0 r UNASSIGNED
```

#### 1.1.1.2. 性能维度  
&emsp; 当然一个索引很大的话，数据写入和查询性能都会变差。  
&emsp; 而高效检索体现在：基于日期的检索可以直接检索对应日期的索引，无形中缩减了很大的数据规模。  
&emsp; 比如检索：“2019-02-01”号的数据，之前的检索会是在一个月甚至更大体量的索引中进行。  

&emsp; 现在直接检索"index_2019-02-01"的索引,效率提升好几倍。  

#### 1.1.1.3. 风险维度
&emsp; 一旦一个大索引出现故障，相关的数据都会受到影响。而分成滚动索引的话，相当于做了物理隔离。  

### 1.1.2. PB级索引设计实现
&emsp; 综上，结合实践经验，大索引设计建议：使用模板+Rollover+Curator动态创建索引。动态索引使用效果如下：  

```text
index_2019-01-01-000001
index_2019-01-02-000002
index_2019-01-03-000003
index_2019-01-04-000004
index_2019-01-05-000005
```

#### 1.1.2.1. 使用模板统一配置索引
&emsp; 目的：统一管理索引，相关索引字段完全一致。  

#### 1.1.2.2. 使用Rollver增量管理索引
&emsp; 目的：按照日期、文档数、文档存储大小三个维度进行更新索引。使用举例：  

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

#### 1.1.2.3. 索引增量更新原理  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-66.png)  
&emsp; 索引更新的时机是：当原始索引满足设置条件的三个中的一个的时候，就会更新为新的索引。为保证业务的全索引检索，一般采用别名机制。  
&emsp; 在索引模板设计阶段，模板定义一个全局别名：用途是全局检索，如图所示的别名：indexall。每次更新到新的索引后，新索引指向一个用于实时新数据写入的别名，如图所示的别名：indexlatest。同时将旧索引的别名 index_latest 移除。  

&emsp; 别名删除和新增操作举例：  

```text
POST /_aliases
{
  "actions" : [
      { "remove" : { "index" : "index_2019-01-01-000001", "alias" : "index_latest" } },
      { "add" : { "index" : "index_2019-01-02-000002", "alias" : "index_latest" } }
  ]
}
```
&emsp; 经过如上步骤，即可完成索引的更新操作。  

#### 1.1.2.4. 使用 curator 高效清理历史数据
&emsp; 目的：按照日期定期删除、归档历史数据。  
&emsp; 一个大索引的数据删除方式只能使用 delete_by_query，由于 ES 中使用更新版本机制。删除索引后，由于没有物理删除，磁盘存储信息会不减反增。有同学就反馈 500GB+ 的索引 delete_by_query 导致负载增高的情况。  
&emsp; 而按照日期划分索引后，不需要的历史数据可以做如下的处理。  

* 删除——对应 delete 索引操作。
* 压缩——对应 shrink 操作。
* 段合并——对应 force_merge 操作。

&emsp; 而这一切，可以借助：curator 工具通过简单的配置文件结合定义任务 crontab 一键实现。  
&emsp; 注意：7.X高版本借助iLM实现更为简单。  
&emsp; 举例，一键删除 30 天前的历史数据：  

```text
[root@localhost .curator]# cat action.yml 
actions:
    1:
    action: delete_indices
    description: >-
        Delete indices older than 30 days (based on index name), for logstash-
        prefixed indices. Ignore the error if the filter does not result in an
        actionable list of indices (ignore_empty_list) and exit cleanly.
    options:
        ignore_empty_list: True
        disable_action: False
    filters:
    - filtertype: pattern
        kind: prefix
        value: logs_
    - filtertype: age
        source: name
        direction: older
        timestring: '%Y.%m.%d'
        unit: days
        unit_count: 30
```

## 1.2. 分片数和副本数如何设计？  
### 1.2.1. 分片/副本认知
1. 分片：分片本身都是一个功能齐全且独立的“索引”，可以托管在集群中的任何节点上。
&emsp; 数据切分分片的主要目的：  
&emsp; （1）水平分割/缩放内容量 。  
&emsp; （2）跨分片（可能在多个节点上）分布和并行化操作，提高性能/吞吐量。   
&emsp; 注意：分片一旦创建，不可以修改大小。  
2. 副本：它在分片/节点出现故障时提供高可用性。  
&emsp; 副本的好处：因为可以在所有副本上并行执行搜索——因此扩展了搜索量/吞吐量。  
&emsp; 注意：副本分片与主分片存储在集群中不同的节点。副本的大小可以通过：number_of_replicas动态修改。  

### 1.2.2. 分片和副本实战中设计

&emsp; 最常见问题答疑  

#### 1.2.2.1. 问题 1：索引设置多少分片？
&emsp; Shard 大小官方推荐值为 20-40GB, 具体原理呢？Elasticsearch 员工 Medcl 曾经讨论如下：  
&emsp; Lucene 底层没有这个大小的限制，20-40GB 的这个区间范围本身就比较大，经验值有时候就是拍脑袋，不一定都好使。  
&emsp; Elasticsearch 对数据的隔离和迁移是以分片为单位进行的，分片太大，会加大迁移成本。  
&emsp; 一个分片就是一个 Lucene 的库，一个 Lucene 目录里面包含很多 Segment，每个 Segment 有文档数的上限，Segment 内部的文档 ID 目前使用的是 Java 的整型，也就是 2 的 31 次方，所以能够表示的总的文档数为Integer.MAXVALUE - 128 = 2^31 - 128 = 2147483647 - 1 = 2,147,483,519，也就是21.4亿条。  

&emsp; 同样，如果不 forcemerge 成一个 Segment，单个 shard 的文档数能超过这个数。  
&emsp; 单个 Lucene 越大，索引会越大，查询的操作成本自然要越高，IO 压力越大，自然会影响查询体验。  
&emsp; 具体一个分片多少数据合适，还是需要结合实际的业务数据和实际的查询来进行测试以进行评估。  
&emsp; 综合实战+网上各种经验分享，梳理如下：  

* 第一步：预估一下数据量的规模。一共要存储多久的数据，每天新增多少数据？两者的乘积就是总数据量。  
* 第二步：预估分多少个索引存储。索引的划分可以根据业务需要。  
* 第三步：考虑和衡量可扩展性，预估需要搭建几台机器的集群。存储主要看磁盘空间，假设每台机器2TB，可用：2TB0.85(磁盘实际利用率）0.85(ES 警戒水位线）。  
* 第四步：单分片的大小建议最大设置为 30GB。此处如果是增量索引，可以结合大索引的设计部分的实现一起规划。  

&emsp; 前三步能得出一个索引的大小。分片数考虑维度：  

* 1）分片数 = 索引大小/分片大小经验值 30GB 。
* 2）分片数建议和节点数一致。设计的时候1）、2）两者权衡考虑+rollover 动态更新索引结合。

&emsp; 每个 shard 大小是按照经验值 30G 到 50G，因为在这个范围内查询和写入性能较好。  

&emsp; 经验值的探推荐阅读： 
[Elasticsearch究竟要设置多少分片数？](https://mp.weixin.qq.com/s?__biz=MzI2NDY1MTA3OQ==&mid=2247483700&idx=1&sn=1549fc794f77da2d2194c991e1ce029b&chksm=eaa8291cdddfa00ae765d5fc5298e252a0f848197348123266afb84751d9fe8907aff65d7aea&scene=21#wechat_redirect)  
[探究 | Elasticsearch集群规模和容量规划的底层逻辑](https://mp.weixin.qq.com/s?__biz=MzI2NDY1MTA3OQ==&mid=2247484628&idx=1&sn=666e416ae28b93e42c26f26b208dea84&chksm=eaa82cfcdddfa5eacfcddb0cf54edcecb3ad86ca2cafd6f4f2d90cf8a4033d83eb16cb2a56f0&scene=21#wechat_redirect)  

#### 1.2.2.2. 问题 2：索引设置多少副本？
&emsp; 结合集群的规模，对于集群数据节点 >=2 的场景：建议副本至少设置为 1。  
&emsp; 之前有同学出现过：副本设置为 0，长久以后会出现——数据写入向指定机器倾斜的情况。  
&emsp; 注意：  
&emsp; 单节点的机器设置了副本也不会生效的。副本数的设计结合数据的安全需要。对于数据安全性要求非常高的业务场景，建议做好：增强备份（结合 ES 官方备份方案）。  

## 1.3. Mapping 如何设计？  
### 1.3.1. Mapping 认知
&emsp; Mapping 是定义文档及其包含的字段的存储和索引方式的过程。例如，使用映射来定义：  

* 应将哪些字符串字段定义为全文检索字段；  
* 哪些字段包含数字，日期或地理位置；  
* 定义日期值的格式（时间戳还是日期类型等）；  
* 用于控制动态添加字段的映射的自定义规则。  

### 1.3.2. ES Mapping设置  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-72.png)  

### 1.3.3. 设计Mapping的注意事项
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-67.png)  
&emsp; ES 支持增加字段 //新增字段  

```text
PUT new_index
  {
    "mappings": {
      "_doc": {
        "properties": {
          "status_code": {
            "type":       "keyword"
          }
        }
      }
    }
  }
```

* ES 不支持直接删除字段  
* ES 不支持直接修改字段  
* ES 不支持直接修改字段类型 如果非要做灵活设计，ES 有其他方案可以替换，借助reindex。但是数据量大会有性能问题，建议设计阶段综合权衡考虑。    

### 1.3.4. Mapping字段的设置流程
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-73.png)  
&emsp; 索引分为静态 Mapping（自定义字段）+动态 Mapping（ES 自动根据导入数据适配）。  
&emsp; 实战业务场景建议：选用静态 Mapping，根据业务类型自己定义字段类型。

&emsp; 好处：  

* 可控；
* 节省存储空间（默认 string 是 text+keyword，实际业务不一定需要）。

&emsp; 设置字段的时候，务必过一下如下图示的流程。根据实际业务需要，主要关注点：  

* 数据类型选型；
* 是否需要检索；
* 是否需要排序+聚合分析；
* 是否需要另行存储。

![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-68.png)  
&emsp; 核心参数的含义，梳理如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-69.png)  

### 1.3.5. Mapping建议结合模板定义
&emsp; 索引 Templates——索引模板允许您定义在创建新索引时自动应用的模板。模板包括settings和Mappings以及控制是否应将模板应用于新索引。  
&emsp; 注意：模板仅在索引创建时应用。更改模板不会对现有索引产生影响。  
&emsp; 第1部分也有说明，针对大索引，使用模板是必须的。核心需要设置的setting（仅列举了实战中最常用、可以动态修改的）如下：  

* index.numberofreplicas 每个主分片具有的副本数。默认为 1（7.X 版本，低于 7.X 为 5）。
* index.maxresultwindow 深度分页 rom + size 的最大值—— 默认为 10000。
* index.refresh_interval 默认 1s：代表最快 1s 搜索可见；

&emsp; 写入时候建议设置为 -1，提高写入性能；  
&emsp; 实战业务如果对实时性要求不高，建议设置为 30s 或者更高。  

### 1.3.6. 包含Mapping的template设计万能模板
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
&emsp; 以下的索引 Mapping中，_source设置为false，同时各个字段的store根据需求设置了true和false。  
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

## 1.4. 分词的选型
&emsp; 主要以 ik 来说明，最新版本的ik支持两种类型。ik_maxword 细粒度匹配，适用切分非常细的场景。ik_smart 粗粒度匹配，适用切分粗的场景。  

### 1.4.1. 坑 1：分词选型
&emsp; 实际业务中：建议适用ik_max_word分词 + match_phrase短语检索。  
&emsp; 原因：ik_smart有覆盖不全的情况，数据量大了以后，即便 reindex 能满足要求，但面对极大的索引的情况，reindex 的耗时我们承担不起。建议ik_max_word一步到位。  

### 1.4.2. 坑 2：ik 要装集群的所有机器吗？
&emsp; 建议：安装在集群的所有节点上。  

### 1.4.3. 坑 3：ik 匹配不到怎么办？  
* 方案1：扩充 ik 开源自带的词库+动态更新词库；原生的词库分词数量级很小，基础词库尽量更大更全，网上搜索一下“搜狗词库“。  
&emsp; 动态更新词库：可以结合 mysql+ik 自带的更新词库的方式动态更新词库。  
&emsp; 更新词库仅对新创建的索引生效，部分老数据索引建议使用 reindex 升级处理。  
* 方案2：采用字词混合索引的方式，避免“明明存在，但是检索不到的”场景。  
&emsp; [探究 | 明明存在，怎么搜索不出来呢？](https://mp.weixin.qq.com/s?__biz=MzI2NDY1MTA3OQ==&mid=2247484287&idx=1&sn=e1f4b24f61d37d556828bbcd211707ac&chksm=eaa82b57dddfa241570730dde38b74a3ff9c36927fd84513b0136e4dc3ee7af18154290a27b2&scene=21#wechat_redirect)  

## 1.5. ES多表关联  

    实际业务问题：多层数据结构，一对多关系，如何用一个查询查询所有的数据？  
    比如数据结构如下：帖子--帖子评论--评论用户  3层。  
    现在需要查询一条帖子，最好能查询到帖子下的评论，还有评论下面的用户数据，一个查询能搞定吗？目前两层我可以查询到，3层就不行了。  
    如果一次查询不到，那如何设计数据结构？又应该如何查询呢？  

&emsp; 目前ES主要有以下4种常用的方法来处理数据实体间的关联关系：  
&emsp; **（1）Application-side joins（服务端Join或客户端Join）**  
&emsp; 这种方式，索引之间完全独立（利于对数据进行标准化处理，如便于上述两种增量同步的实现），由应用端的多次查询来实现近似关联关系查询。  
&emsp; 这种方法<font color = "red">适用于第一个实体只有少量的文档记录的情况</font>（使用ES的terms查询具有上限，默认1024，具体可在elasticsearch.yml中修改），并且最好它们很少改变。这将允许应用程序对结果进行缓存，并避免经常运行第一次查询。  
&emsp; **（2）Data denormalization（数据的非规范化）**  
&emsp; 这种方式，通俗点就是通过<font color = "red">字段冗余</font>，以一张大宽表来实现粗粒度的index，这样可以充分发挥扁平化的优势。但是这是以牺牲索引性能及灵活度为代价的。  
&emsp; 使用的前提：冗余的字段应该是很少改变的；比较适合与一对少量关系的处理。当业务数据库并非采用非规范化设计时，这时要将数据同步到作为二级索引库的ES中，就很难使用上述增量同步方案，必须进行定制化开发，基于特定业务进行应用开发来处理join关联和实体拼接。  
&emsp; ps：宽表处理在处理一对多、多对多关系时，会有字段冗余问题，<font color = "red">适合“一对少量”且这个“一”更新不频繁的应用场景。</font>  
宽表化处理，在查询阶段如果只需要“一”这部分时，需要进行结果去重处理（可以使用ES5.x的字段折叠特性，但无法准确获取分页总数，产品设计上需采用上拉加载分页方式）  
&emsp; **（3）Nested objects（嵌套文档）**    
&emsp; 索引性能和查询性能二者不可兼得，必须进行取舍。嵌套文档将实体关系嵌套组合在单文档内部（类似与json的一对多层级结构），这种方式牺牲索引性能（文档内任一属性变化都需要重新索引该文档）来换取查询性能，可以同时返回关系实体，<font color = "red">比较适合于一对少量的关系处理。</font>   
&emsp; ps: 当使用嵌套文档时，使用通用的查询方式是无法访问到的，必须使用合适的查询方式（nested query、nested filter、nested facet等），很多场景下，使用嵌套文档的复杂度在于索引阶段对关联关系的组织拼装。  
&emsp; **（4）Parent/child relationships（父子文档）**  
&emsp; 父子文档牺牲了一定的查询性能来换取索引性能，<font color = "red">适用于一对多的关系处理</font>。其通过两种type的文档来表示父子实体，父子文档的索引是独立的。父-子文档ID映射存储在 Doc Values 中。当映射完全在内存中时， Doc Values 提供对映射的快速处理能力，另一方面当映射非常大时，可以通过溢出到磁盘提供足够的扩展能力。  
&emsp; 在查询parent-child替代方案时，发现了一种filter-terms的语法，要求某一字段里有关联实体的ID列表。基本的原理是在terms的时候，对于多项取值，如果在另外的index或者type里已知主键id的情况下，某一字段有这些值，可以直接嵌套查询。具体可参考官方文档的示例：通过用户里的粉丝关系，微博和用户的关系，来查询某个用户的粉丝发表的微博列表。  
&emsp; ps：父子文档相比嵌套文档较灵活，但只适用于“一对大量”且这个“一”不是海量的应用场景，该方式比较耗内存和CPU，<font color = "red">这种方式查询比嵌套方式慢5~10倍</font>，且需要使用特定的has_parent和has_child过滤器查询语法，查询结果不能同时返回父子文档（一次join查询只能返回一种类型的文档）。  
&emsp; 而受限于父子文档必须在同一分片上，ES父子文档在滚动索引、多索引场景下对父子关系存储和联合查询支持得不好，而且子文档type删除比较麻烦（子文档删除必须提供父文档ID）。  

&emsp; 如果业务端对查询性能要求很高的话，还是<font color = "red">建议使用宽表化处理的方式</font>，这样也可以比较好地应对聚合的需求。在索引阶段需要做join处理，查询阶段可能需要做去重处理，分页方式可能也得权衡考虑下。  

## 1.6. 检索类型如何选型呢？  


## 1.7. 多表关联如何设计？  
### 1.7.1. 为什么会有多表关联
&emsp; 主要原因：常规基于关系型数据库开发，多多少少都会遇到关联查询。而关系型数据库设计的思维很容易带到 ES 的设计中。  

### 1.7.2. 多表关联如何实现  
&emsp; **方案一：多表关联视图，视图同步 ES**  
&emsp; MySQL 宽表导入 ES，使用 ES 查询+检索。适用场景：基础业务都在 MySQL，存在几十张甚至几百张表，准备同步到 ES，使用 ES 做全文检索。  
&emsp; 将数据整合成一个宽表后写到 ES，宽表的实现可以借助关系型数据库的视图实现。  
&emsp; 宽表处理在处理一对多、多对多关系时，会有字段冗余问题，如果借助：  logstash_input_jdbc，关系型数据库如 MySQL 中的每一个字段都会自动帮你转成 ES 中对应索引下的对应 document 下的某个相同字段下的数据。  

* 步骤 1：提前关联好数据，将关联的表建立好视图，一个索引对应你的一个视图，并确认视图中数据的正确性。
* 步骤 2：ES 中针对每个视图定义好索引名称及 Mapping。
* 步骤 3：以视图为单位通过 logstash_input_jdbc 同步到 ES 中。

&emsp; **方案二：1 对 1 同步 ES**  
&emsp; MySQL+ES 结合，各取所长。适用场景：关系型数据库全量同步到 ES 存储，没有做冗余视图关联。  
&emsp; ES 擅长的是检索，而 MySQL 才擅长关系管理。  
&emsp; 所以可以考虑二者结合，使用 ES 多索引建立相同的别名，针对别名检索到对应 ID 后再回 MySQL 通过关联 ID join 出需要的数据。  

&emsp; **方案三：使用 Nested 做好关联**  
&emsp; 适用场景：1 对少量的场景。  
&emsp; 举例：有一个文档描述了一个帖子和一个包含帖子上所有评论的内部对象评论。可以借助 Nested 实现。  
&emsp; Nested 类型选型——如果需要索引对象数组并保持数组中每个对象的独立性，则应使用嵌套 Nested 数据类型而不是对象 Oject 数据类型。  
&emsp; 当使用嵌套文档时，使用通用的查询方式是无法访问到的，必须使用合适的查询方式（nested query、nested filter、nested facet等），很多场景下，使用嵌套文档的复杂度在于索引阶段对关联关系的组织拼装。  

&emsp; **方案四：使用 ES6.X+ 父子关系 Join 做关联**   
&emsp; 适用场景：1 对多量的场景。  
&emsp; 举例：1 个产品和供应商之间是1对N的关联关系。  
&emsp; Join 类型：join 数据类型是一个特殊字段，用于在同一索引的文档中创建父/子关系。关系部分定义文档中的一组可能关系，每个关系是父名称和子名称。  
&emsp; 当使用父子文档时，使用has_child 或者has_parent做父子关联查询。  

&emsp; **方案三、方案四选型对比：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-70.png)  
&emsp; 注意：方案三&方案四选型必须考虑性能问题。文档应该尽量通过合理的建模来提升检索效率。  
&emsp; Join 类型应该尽量避免使用。nested 类型检索使得检索效率慢几倍，父子Join 类型检索会使得检索效率慢几百倍。  
&emsp; 尽量将业务转化为没有关联关系的文档形式，在文档建模处多下功夫，以提升检索效率。  

&emsp; [干货 | 论Elasticsearch数据建模的重要性](https://mp.weixin.qq.com/s?__biz=MzI2NDY1MTA3OQ==&mid=2247484159&idx=1&sn=731562a8bb89c9c81b4fd6a8e92e1a99&chksm=eaa82ad7dddfa3c11e5b63a41b0e8bc10d12f1b8439398e490086ddc6b4107b7864dbb9f891a&scene=21#wechat_redirect)  
&emsp; [干货 | Elasticsearch多表关联设计指南](https://mp.weixin.qq.com/s?__biz=MzI2NDY1MTA3OQ==&mid=2247484382&idx=1&sn=da073a257575867b8d979dac850c3f8e&chksm=eaa82bf6dddfa2e0bf920f0a3a63cb635277be2ae286a2a6d3fff905ad913ebf1f43051609e8&scene=21#wechat_redirect)  

&emsp; 小结  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-71.png)  

## 1.8. 实战中遇到过的坑
&emsp; 如果能重来，我会如何设计 Elasticsearch 系统？  
&emsp; 来自累计近千万实战项目设计的思考。  

* 坑1: 数据清洗一定发生在写入 es 之前！而不是请求数据后处理，拿势必会降低请求速度和效率。
* 坑2：高亮不要重复造轮子，用原生就可以。
* 坑3：让 es 做他擅长的事，检索+不复杂的聚合，否则数据量+复杂的业务逻辑大会有性能问题。
* 坑4：设计的工作必须不要省！快了就是慢了，否则无休止的因设计缺陷引发的 bug 会增加团队的戳败感！
* 坑5：在给定时间的前提下，永远不会有完美的设计，必须相对合理的设计+重构结合，才会有相对靠谱的系统。
* 坑6：SSD 能提升性能，但如果系统业务逻辑非常负责，换了 SSD 未必达到预期。
* 坑7：由于 Elasticsearch 不支持事务 ACID 特性，数据库作为实时数据补充，对于实时数据要求严格的场景，必须同时采取双写或者同步的方式。这样，一旦实时数据出现不一致，可以通过数据库进行同步递增更新。

