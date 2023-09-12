
<!-- TOC -->

- [1. 排序/相关度/评分机制](#1-排序相关度评分机制)
    - [1.1. 排序与相关性](#11-排序与相关性)
        - [1.1.1. 排序](#111-排序)
            - [1.1.1.1. 按照字段的值排序](#1111-按照字段的值排序)
            - [1.1.1.2. 多级排序](#1112-多级排序)
            - [1.1.1.3. 字段多值的排序](#1113-字段多值的排序)
        - [1.1.2. 字符串排序与多字段](#112-字符串排序与多字段)
    - [1.2. 相关性评分机制](#12-相关性评分机制)

<!-- /TOC -->


# 1. 排序/相关度/评分机制
<!-- 
https://blog.csdn.net/hellozhxy/article/details/82632007

相关度评分背后的理论
https://www.elastic.co/guide/cn/elasticsearch/guide/current/scoring-theory.html
Elasticsearch之评分机制
https://www.jianshu.com/p/2624f61f1d02

ES学习——ES评分简单介绍
https://blog.csdn.net/qq_25673113/article/details/88917161
-->


## 1.1. 排序与相关性
&emsp; **默认情况下，返回的结果是按照 相关性 进行排序的，最相关的文档排在最前。**   
<!-- 
elasticsearch排序(相关性排序score)
https://blog.csdn.net/wwd0501/article/details/78622204
-->

### 1.1.1. 排序
&emsp; 为了按照相关性来排序，需要将相关性表示为一个数值。在 Elasticsearch 中， 相关性得分 由一个浮点数进行表示，并在搜索结果中通过 _score 参数返回， 默认排序是 \_score 降序。  
&emsp; 有时，相关性评分并没有意义。例如，下面的查询返回所有 user\_id 字段包含 1 的结果：  

```text
GET /_search
{
    "query" : {
        "bool" : {
            "filter" : {
                "term" : {
                    "user_id" : 1
                }
            }
        }
    }
}
```
&emsp; 这里没有一个有意义的分数：因为使用的是 filter （过滤），这表明只希望获取匹配 user_id: 1的文档，并没有试图确定这些文档的相关性。 实际上文档将按照随机顺序返回，并且每个文档都会评为零分。  

#### 1.1.1.1. 按照字段的值排序
&emsp; 在这个案例中，通过时间来对 tweets 进行排序是有意义的，最新的 tweets 排在最前。 可以使用 sort 参数进行实现：  

```text
GET /_search
{
    "query" : {
        "bool" : {
            "filter" : { "term" : { "user_id" : 1 }}
        }
    },
    "sort": { "date": { "order": "desc" }}
}
```

&emsp; 会注意到结果中的两个不同点：  

```text
"hits" : {
    "total" :           6,
    "max_score" :       null, 
    "hits" : [ {
        "_index" :      "us",
        "_type" :       "tweet",
        "_id" :         "14",
        "_score" :      null, 
        "_source" :     {
             "date":    "2014-09-24",
             ...
        },
        "sort" :        [ 1411516800000 ] 
    },
    ...
}
```

&emsp; \_score 不被计算, 因为它并没有用于排序。  
&emsp; date 字段的值表示为自 epoch (January 1, 1970 00 :00 :00 UTC)以来的毫秒数，通过 sort 字段的值进行返回。  
&emsp; 首先在每个结果中有一个新的名为 sort 的元素，它包含了用于排序的值。 在这个案例中，按照 date 进行排序，在内部被索引为 自 epoch 以来的毫秒数。long 类型数 1411516800000 等价于日期字符串 2014-09-24 00 :00 :00 UTC 。  
&emsp; 其次 \_score 和 max\_score 字段都是 null 。 计算 \_score 的花销巨大，通常仅用于排序；并不根据相关性排序，所以记录 _score 是没有意义的。如果无论如何都要计算\_score，可以将track\_scores参数设置为true 。  

#### 1.1.1.2. 多级排序
&emsp; 假定想要结合使用 date 和 \_score 进行查询，并且匹配的结果首先按照日期排序，然后按照相关性排序：  

```text
GET /_search
{
    "query" : {
        "bool" : {
            "must":   { "match": { "tweet": "manage text search" }},
            "filter" : { "term" : { "user_id" : 2 }}
        }
    },
    "sort": [
        { "date":   { "order": "desc" }},
        { "_score": { "order": "desc" }}
    ]
}
```

&emsp; 排序条件的顺序是很重要的。结果首先按第一个条件排序，仅当结果集的第一个sort值完全相同时才会按照第二个条件进行排序，以此类推。  
&emsp; 多级排序并不一定包含 _score。可以根据一些不同的字段进行排序，如地理距离或是脚本计算的特定值。  

#### 1.1.1.3. 字段多值的排序
&emsp; 一种情形是字段有多个值的排序， 需要记住这些值并没有固有的顺序；一个多值的字段仅仅是多个值的包装，这时应该选择哪个进行排序呢？  
&emsp; 对于数字或日期，可以将多值字段减为单值，这可以通过使用min、max、avg或是sum排序模式。例如可以按照每个date字段中的最早日期进行排序，通过以下方法：  

```text
"sort": {
    "dates": {
        "order": "asc",
        "mode":  "min"
    }
}
```

### 1.1.2. 字符串排序与多字段
&emsp; 被解析的字符串字段也是多值字段， 但是很少会按照想要的方式进行排序。如果想分析一个字符串，如fine old art ，这包含3项。很可能想要按第一项的字母排序，然后按第二项的字母排序，诸如此类，但是Elasticsearch在排序过程中没有这样的信息。  
&emsp; 可以使用 min 和 max 排序模式（默认是 min ），但是这会导致排序以art或是old ，任何一个都不是所希望的。  
&emsp; 为了以字符串字段进行排序，这个字段应仅包含一项： 整个 not_analyzed 字符串。 但是仍需要 analyzed 字段，这样才能以全文进行查询  
&emsp; 一个简单的方法是用两种方式对同一个字符串进行索引，这将在文档中包括两个字段： analyzed 用于搜索， not_analyzed 用于排序  
&emsp; 但是保存相同的字符串两次在 _source 字段是浪费空间的。 真正想要做的是传递一个 单字段 但是却用两种方式索引它。所有的 _core_field 类型 (strings, numbers, Booleans, dates) 接收一个 fields 参数  
&emsp; 该参数允许转化一个简单的映射如：  

```text
"tweet": {
    "type":     "string",
    "analyzer": "english"
}
```
&emsp; 为一个多字段映射如：  
&emsp; 被解析的字符串字段也是多值字段， 但是很少会按照你想要的方式进行排序。如果你想分析一个字符串，如 fine old art ，这包含 3 项。很可能想要按第一项的字母排序，然后按第二项的字母排序，诸如此类，但是 Elasticsearch 在排序过程中没有这样的信息。  
&emsp; 你可以使用 min 和 max 排序模式（默认是 min ），但是这会导致排序以 art 或是 old ，任何一个都不是所希望的。  
&emsp; 为了以字符串字段进行排序，这个字段应仅包含一项： 整个 not_analyzed 字符串。 但是仍需要 analyzed 字段，这样才能以全文进行查询  
&emsp; 一个简单的方法是用两种方式对同一个字符串进行索引，这将在文档中包括两个字段：analyzed用于搜索，not_analyzed用于排序  
&emsp; 但是保存相同的字符串两次在 \_source字段是浪费空间的。真正想要做的是传递一个单字段但是却用两种方式索引它。所有的_core_field 类型 (strings, numbers, Booleans, dates) 接收一个fields参数  
&emsp; 该参数允许你转化一个简单的映射如：  

```text
"tweet": {
    "type":     "string",
    "analyzer": "english"
}
```
&emsp; 为一个多字段映射如：  

```text
"tweet": { 
    "type":     "string",
    "analyzer": "english",
    "fields": {
        "raw": { 
            "type":  "string",
            "index": "not_analyzed"
        }
    }
}
```
&emsp; tweet 主字段与之前的一样: 是一个 analyzed 全文字段。  
&emsp; 新的 tweet.raw 子字段是 not_analyzed.  
&emsp; 现在，至少只要重新索引了数据，使用 tweet 字段用于搜索，tweet.raw 字段用于排序：  

```text
GET /_search
{
    "query": {
        "match": {
            "tweet": "elasticsearch"
        }
    },
    "sort": "tweet.raw"
}
```

&emsp; 默认情况下，返回结果是按相关性倒序排列的。 但是什么是相关性？ 相关性如何计算？  
&emsp; 每个文档都有相关性评分，用一个正浮点数字段 _score 来表示 。 _score 的评分越高，相关性越高。  


## 1.2. 相关性评分机制
<!-- 
https://blog.csdn.net/hellozhxy/article/details/82632007
-->

<!-- 
Elasticsearch之评分机制
https://www.jianshu.com/p/2624f61f1d02

https://blog.csdn.net/qq_25673113/article/details/88917161

https://cloud.tencent.com/developer/article/1063292

ES学习——ES评分简单介绍
https://blog.csdn.net/qq_25673113/article/details/88917161
-->
