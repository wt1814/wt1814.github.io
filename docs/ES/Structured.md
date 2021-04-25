

<!-- TOC -->

- [1. 结构化检索](#1-结构化检索)
    - [1.1. 什么是结构化检索](#11-什么是结构化检索)
    - [1.2. 精确值查找](#12-精确值查找)
        - [1.2.1. 单个精确值查找（term query）](#121-单个精确值查找term-query)
        - [1.2.2. 布尔过滤器(复合查询)](#122-布尔过滤器复合查询)
        - [1.2.3. 多个值精确查找（terms query）](#123-多个值精确查找terms-query)
    - [1.3. 范围检索（range query）](#13-范围检索range-query)
    - [1.4. 存在与否检索（exist query）](#14-存在与否检索exist-query)
    - [1.5. 前缀检索（ Prefix Query ）](#15-前缀检索-prefix-query-)
    - [1.6. 通配符检索( wildcard query)](#16-通配符检索-wildcard-query)
    - [1.7. 正则表达式检索（regexp query）](#17-正则表达式检索regexp-query)
    - [1.8. 模糊检索(fuzzy query)](#18-模糊检索fuzzy-query)
    - [1.9. 类型检索（type query）](#19-类型检索type-query)
    - [1.10. Ids检索（ids query）](#110-ids检索ids-query)

<!-- /TOC -->

# 1. 结构化检索

## 1.1. 什么是结构化检索  
<!-- 
https://www.elastic.co/guide/cn/elasticsearch/guide/current/structured-search.html
-->
&emsp; 针对字段类型： 日期、时间、数字类型，以及精确的文本匹配。  
&emsp; 结构化检索特点：  
* 1） **结构化查询，得到的结果总是 非是即否，** 要么存于集合之中，要么存在集合之外。  
* 2）结构化查询不关心文件的相关度或评分；它简单的对文档包括或排除处理。  

## 1.2. 精确值查找
### 1.2.1. 单个精确值查找（term query）
&emsp; term 查询会查找指定的精确值。term查询是简单的，它接受一个字段名以及希望查找的数值。  
&emsp; 想要类似mysql中如下sql语句的查询操作：

```sql
SELECT document FROM products WHERE price = 20;
```

&emsp; DSL写法：  

```
GET /my_store/products/_search
{
  "query" : {
  "term" : {
  "price" : 20
  }
  }
}
```

&emsp; 当进行精确值查找时，会使用过滤器（filters）。过滤器很重要，因为它们执行速度非常快，不会计算相关度（直接跳过了整个评分阶段）而且很容易被缓存。如下： 使用 constant_score 查询以非评分模式来执行 term 查询并以一作为统一评分。  

```
GET /my_store/products/_search
{
  "query" : {
  "constant_score" : {
  "filter" : {
  "term" : {
  "price" : 20
  }
  }
  }
  }
}
```

&emsp; 注意：5.xES中，对于字符串类型，要进行精确值匹配。需要讲类型设置为text和keyword两种类型。mapping设置如下：  

```
POST testindex/testtype/_mapping
{
   "testtype ":{
  "properties":{
  "title":{
  "type":"text",
  "analyzer":"ik_max_word",
  "search_analyzer":"ik_max_word",
  "fields":{
  "keyword":{
  "type":"keyword"
  }
  }
  }
}
}
```

&emsp; 精确值java api jest使用方法：  

```java
searchSourceBuilder.query(QueryBuilders.termQuery(“text.keyword”, “来自新华社的报道”));
```

### 1.2.2. 布尔过滤器(复合查询)
&emsp; 一个 bool 过滤器由三部分组成：  

```
{
   "bool" : {
      "must" :     [],
      "should" :   [],
      "must_not" : [],
      "filter":    []
   }
}
```

* must ——所有的语句都 必须（must） 匹配，与 AND 等价。
* must_not ——所有的语句都 不能（must not） 匹配，与 NOT 等价。
* should ——至少有一个语句要匹配，与 OR 等价。
* filter——必须匹配，运行在非评分&过滤模式。

&emsp; 当需要多个过滤器时，只须将它们置入 bool 过滤器的不同部分即可。举例：  
```
GET /my_store/products/_search
{
  "query" : {
  "filtered" : {
  "filter" : {
  "bool" : {
  "should" : [
  { "term" : {"price" : 20}},

  { "term" : {"productID" : "XHDK-A-1293-#fJ3"}}

  ],
  "must_not" : {
  "term" : {"price" : 30}

  }
  }
  }
  }
  }
}
```


<!-- 
ElasticSearch 复合查询，理解 Es 中的文档评分策略！ 
https://mp.weixin.qq.com/s/59D8ouXbTMlh4swb6eY2zA



&emsp; ①想要一台带NFC功能的 或者 小米的手机 但是不要耳机  

```sql
SELECT * from product 
where (`name` like "%xiaomi%" or `name` like '%nfc%')
AND `name` not LIKE '%erji%'
```
```text
GET /product/_search
{
  "query": {
    "constant_score":{
      "filter": {
        "bool": {
          "should":[
            {"term":{"name":"xiaomi"}},
            {"term":{"name":"nfc"}}
            ],
          "must_not":[
            {"term":{"name":"erji"}}
            ]
        }
      },
      "boost": 1.2
    }
  }
}
```
&emsp; ②搜索一台xiaomi nfc phone或者一台满足 是一台手机 并且 价格小于等于2999  

```sql
SELECT * FROM product 
WHERE NAME LIKE '%xiaomi nfc phone%' 
OR (
NAME LIKE '%erji%' 
AND price > 399 
AND price <=999);
```
```text
GET /product/_search
{
  "query": {
    "constant_score": {
      "filter": { 
        "bool":{
          "should":[
            {"match_phrase":{"name":"xiaomi nfc phone"}},
            {
              "bool":{
                "must":[
                  {"term":{"name":"phone"}},
                  {"range":{"price":{"lte":"2999"}}}
                  ]
              }
            }
          ]
        }
      }
    }
  }
}
```

--------
&emsp; bool 组合匹配  

* 核心功能：多条件组合综合查询。
* 应用场景：支持多条件组合查询的场景。
* 适用类型：text 或者 keyword。一个 bool 过滤器由三部分组成：

```text
{
   "bool" : {
      "must" :     [],
      "should" :   [],
      "must_not" : [],
      "filter":    []
   }
}
```
&emsp; must ——所有的语句都 必须（must） 匹配，与 AND 等价。  
&emsp; must_not ——所有的语句都 不能（must not） 匹配，与 NOT 等价。  
&emsp; should ——至少有一个语句要匹配，与 OR 等价。  
&emsp; filter——必须匹配，运行在非评分&过滤模式。  


&emsp; ①bool：可以组合多个查询条件，bool查询也是采用more_matches_is_better的机制，因此满足must和should子句的文档将会合并起来计算分值。  
&emsp; 1)must：必须满足  
&emsp; 子句（查询）必须出现在匹配的文档中，并将有助于得分。  
&emsp; 2)filter：过滤器 不计算相关度分数，cache☆  
&emsp; 子句（查询）必须出现在匹配的文档中。但是不像 must查询的分数将被忽略。Filter子句在filter上下文中执行，这意味着计分被忽略，并且子句被考虑用于缓存。  
&emsp; 3)should：可能满足 or  
&emsp; 子句（查询）应出现在匹配的文档中。  
&emsp; 4)must_not：必须不满足 不计算相关度分数   not   
&emsp; 子句（查询）不得出现在匹配的文档中。子句在过滤器上下文中执行，这意味着计分被忽略，并且子句被视为用于缓存。由于忽略计分，0因此将返回所有文档的分数。  
&emsp; 5)minimum_should_match  
&emsp; ②案例：  
&emsp; 1)demo案例  

```text
#首先筛选name包含“xiaomi phone”并且价格大于1999的数据（不排序），
#然后搜索name包含“xiaomi”and desc 包含“shouji”

GET /product/_search
{
  "query": {
    "bool":{
      "must": [
        {"match": { "name": "xiaomi"}},
        {"match": {"desc": "shouji"}}
      ],
      "filter": [
        {"match_phrase":{"name":"xiaomi phone"}},
        {"range": {
          "price": {
            "gt": 1999
          }
        }}
      ]
    }
  }
}
```
&emsp; 2)bool多条件 name包含xiaomi 不包含erji 描述里包不包含nfc都可以，价钱要大于等于4999  

```text
GET /product/_search
{
  "query": {
    "bool":{
          #name中必须不能包含“erji”
          "must": [
            {"match": { "name": "xiaomi"}}
          ],
          #name中必须包含“xiaomi”
          "must_not": [
            {"match": { "name": "erji"}}
          ],
          #should中至少满足0个条件，参见下面的minimum_should_match的解释
          "should": [
            {"match": {
              "desc": "nfc"
            }}
          ], 
          #筛选价格大于4999的doc
          "filter": [		
            {"range": {
              "price": {
                "gt": 4999   
              }
            }}
          ]
        }
  }
}
```
&emsp; ③嵌套查询：  
&emsp; 1)minimum_should_match：参数指定should返回的文档必须匹配的子句的数量或百分比。如果bool查询包含至少一个should子句，而没有must或 filter子句，则默认值为1。否则，默认值为0  

```text
GET /product/_search
{
  "query": {
    "bool":{
      "must": [
        {"match": { "name": "nfc"}}
      ],
      "should": [
        {"range": {
          "price": {"gt":1999}
        }},
         {"range": {
          "price": {"gt":3999}
        }}
      ],
      "minimum_should_match": 1
    }
  }
}
```
&emsp; 2)案例：  

```text
GET /product/_search
{
  "query": {
    "bool": {
      "filter": {
        "bool": {
          "should": [
            { "range": {"price": {"gt": 1999}}},
            { "range": {"price": {"gt": 3999}}}
          ],
          "must": [
            { "match": {"name": "nfc"}}
          ]
        }
      }
    }
  }
}
```
 

-->



### 1.2.3. 多个值精确查找（terms query）

```
{
  "terms" : {
  "price" : [20, 30]
  }
}
```

&emsp; 如上，terms是包含的意思，包含20或者包含30。  
&emsp; 如下实现严格意义的精确值检索， tag_count代表必须匹配的次数为1。  

```
GET /my_index/my_type/_search
{
  "query": {
  "constant_score" : {
  "filter" : {
  "bool" : {
  "must" : [
  { "term" : { "tags" : "search" } },
  { "term" : { "tag_count" : 1 } }
  ]
  }
  }
  }
  }
}
```

## 1.3. 范围检索（range query）
&emsp; range 查询可同时提供包含（inclusive）和不包含（exclusive）这两种范围表达式，可供组合的选项如下：  

* gt: > 大于（greater than）
* lt: < 小于（less than）
* gte: >= 大于或等于（greater than or equal to）
* lte: <= 小于或等于（less than or equal to）


&emsp; 类似Mysql中的范围查询：  

```sql
SELECT document
FROM   products
WHERE  price BETWEEN 20 AND 40
```

&emsp; ES中对应的DSL如下：  

```
GET /my_store/products/_search
{
  "query" : {
  "constant_score" : {
  "filter" : {
  "range" : {
  "price" : {
  "gte" : 20,
  "lt" : 40
  }
  }
  }
  }
  }
}
```

## 1.4. 存在与否检索（exist query）
&emsp; mysql中，有如下sql：  

```sql
SELECT tags FROM posts WHERE tags IS NOT NULL;
```

&emsp; ES中，exist查询某个字段是否存在：  

```
GET /my_index/posts/_search
{
    "query" : {
        "constant_score" : {
            "filter" : {
                "exists" : { "field" : "tags" }
            }
        }
    }
}
```

&emsp; 若想要exist查询能匹配null类型，需要设置mapping：  

```
"user": {
  "type": "keyword",
  "null_value": "_null_"
  }
```

&emsp; missing查询在5.x版本已经不存在，改成如下的判定形式：  

```
GET /_search
{
    "query": {
        "bool": {
            "must_not": {
                "exists": {
                    "field": "user"
                }
            }
        }
    }
}
```


## 1.5. 前缀检索（ Prefix Query ）
&emsp; 匹配包含 not analyzed 的前缀字符：  

```
GET /_search
{ "query": {
  "prefix" : { "user" : "ki" }
  }
}
```

## 1.6. 通配符检索( wildcard query)
&emsp; 匹配具有匹配通配符表达式（ (not analyzed ）的字段的文档。 支持的通配符：  
&emsp; 1）\*，它匹配任何字符序列（包括空字符序列）；  
&emsp; 2）？，它匹配任何单个字符。  
&emsp; 请注意，此查询可能很慢，因为它需要遍历多个术语。  
&emsp; 为了防止非常慢的通配符查询，通配符不能以任何一个通配符*或？开头。  
&emsp; 举例：  

```
GET /_search
{
    "query": {
        "wildcard" : { "user" : "ki*y" }
    }
}
```


## 1.7. 正则表达式检索（regexp query）
&emsp; 正则表达式查询允许您使用正则表达式术语查询。  
&emsp; 举例如下：  

```
GET /_search
{
  "query": {
  "regexp":{
  "name.first": "s.*y"
  }
  }
}
```

&emsp; 注意：\*的匹配会非常慢，需要使用一个长的前缀，通常类似.\*?+通配符查询的正则检索性能会非常低。  

## 1.8. 模糊检索(fuzzy query)
&emsp; 模糊查询查找在模糊度中指定的最大编辑距离内的所有可能的匹配项，然后检查术语字典，以找出在索引中实际存在待检索的关键词。  
&emsp; 举例如下：  

```
GET /_search
{
  "query": {
  "fuzzy" : { "user" : "ki" }
  }
}
```

## 1.9. 类型检索（type query）
&emsp; 举例：  

```
GET /my_index/_search
{
  "query": {
  "type" : {
  "value" : "xext"
  }
  }
}
```

&emsp; 已验证，检索索引my_index中，type为xext的全部信息。  

## 1.10. Ids检索（ids query）
&emsp; 返回指定id的全部信息。  

```
GET /my_index/_search
{
  "query": {
  "ids" : {
  "type" : "xext",
  "values" : ["2", "4", "100"]
  }
  }
}
```