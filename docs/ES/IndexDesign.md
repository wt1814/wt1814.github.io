

<!-- TOC -->

- [1. PB级别的大索引如何设计？](#1-pb级别的大索引如何设计)
    - [1.1. 大索引的缺陷](#11-大索引的缺陷)
        - [1.1.1. 存储大小限制维度](#111-存储大小限制维度)
        - [1.1.2. 性能维度](#112-性能维度)
        - [1.1.3. 风险维度](#113-风险维度)
    - [1.2. ※※※PB级索引设计实现(动态/滚动索引)](#12-※※※pb级索引设计实现动态滚动索引)
        - [1.2.1. 使用模板统一配置索引](#121-使用模板统一配置索引)
        - [1.2.2. 使用Rollver增量管理索引](#122-使用rollver增量管理索引)
            - [1.2.2.1. 索引增量更新原理](#1221-索引增量更新原理)
        - [1.2.3. 使用curator高效清理历史数据](#123-使用curator高效清理历史数据)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; **<font color = "clime">大索引设计建议：使用模板+滚动Rollover+Curator动态创建索引。</font>**  

* 使用Rollver增量管理索引  
* 使用curator高效清理历史数据  

# 1. PB级别的大索引如何设计？  
&emsp; 单纯的普通数据索引，如果不考虑增量数据，基本上普通索引就能够满足性能要求。  
&emsp; 通常的操作就是：  

* 步骤 1：创建索引；
* 步骤 2：导入或者写入数据；
* 步骤 3：提供查询请求访问或者查询服务。  

## 1.1. 大索引的缺陷  
&emsp; 如果每天亿万+的实时增量数据呢，基于以下几点原因，单个索引是无法满足要求的。  

### 1.1.1. 存储大小限制维度
&emsp; 单个分片(Shard)实际是Lucene的索引，单分片能存储的最大文档数是：2,147,483,519 (= Integer.MAX_VALUE - 128)。如下命令能查看全部索引的分隔分片的文档大小：  

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

### 1.1.2. 性能维度  
&emsp; 当然一个索引很大的话，数据写入和查询性能都会变差。而高效检索体现在：基于日期的检索可以直接检索对应日期的索引，无形中缩减了很大的数据规模。比如检索：“2019-02-01”号的数据，之前的检索会是在一个月甚至更大体量的索引中进行。  
&emsp; 现在直接检索"index_2019-02-01"的索引，效率提升好几倍。  

### 1.1.3. 风险维度
&emsp; **<font color = "red">一旦一个大索引出现故障，相关的数据都会受到影响。而分成滚动索引的话，相当于做了物理隔离。</font>**  

## 1.2. ※※※PB级索引设计实现(动态/滚动索引)
&emsp; 综上，结合实践经验， **<font color = "clime">大索引设计建议：使用模板+滚动Rollover+Curator动态创建索引。</font>** 动态索引使用效果如下：  

```text
index_2019-01-01-000001
index_2019-01-02-000002
index_2019-01-03-000003
index_2019-01-04-000004
index_2019-01-05-000005
```

### 1.2.1. 使用模板统一配置索引
&emsp; 目的：统一管理索引，相关索引字段完全一致。  

### 1.2.2. 使用Rollver增量管理索引
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

#### 1.2.2.1. 索引增量更新原理  
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

### 1.2.3. 使用curator高效清理历史数据
&emsp; 目的：按照日期定期删除、归档历史数据。  
&emsp; 一个大索引的数据删除方式只能使用delete_by_query，由于ES中使用更新版本机制。删除索引后，由于没有物理删除，磁盘存储信息会不减反增。  
&emsp; 而按照日期划分索引后，不需要的历史数据可以做如下的处理。  

* 删除——对应delete索引操作。
* 压缩——对应shrink操作。
* 段合并——对应force_merge操作。

&emsp; 而这一切，可以借助：curator 工具通过简单的配置文件结合定义任务crontab一键实现。(注意：7.X高版本借助iLM实现更为简单。)  
&emsp; 举例，一键删除30天前的历史数据：  

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