<!-- TOC -->

- [1. 检索操作](#1-检索操作)
    - [1.1. 检索操作](#11-检索操作)
        - [Searchtimeout](#searchtimeout)
        - [1.1.1. Query_string](#111-query_string)
        - [1.1.2. DSL](#112-dsl)
            - [1.1.2.1. SQL代替DSL](#1121-sql代替dsl)
        - [1.1.4. full-text search(全文检索)](#114-full-text-search全文检索)
        - [1.1.5. phrase search(短语搜索)](#115-phrase-search短语搜索)
        - [1.1.3. Query and filter(查询和过滤)](#113-query-and-filter查询和过滤)
        - [Compound queries(组合查询)](#compound-queries组合查询)
        - [1.1.6. highlight search](#116-highlight-search)
    - [1.2. 检索模板](#12-检索模板)

<!-- /TOC -->

# 1. 检索操作  
<!-- 
检索类型如何选型呢？
https://mp.weixin.qq.com/s/Fc5LhiLJIeCtstl9OFeqdQ

-->

## 1.1. 检索操作  
<!-- 
https://blog.csdn.net/xiaos76/article/details/105883617
https://blog.csdn.net/jiaojiao521765146514/article/details/82746192
-->
&emsp; 5.X 版本之后，string 类型不再存在，取代的是text和keyword类型。  
* text 类型作用：分词，将大段的文字根据分词器切分成独立的词或者词组，以便全文检索。  
    * 适用于：email 内容、某产品的描述等需要分词全文检索的字段；
    * 不适用：排序或聚合（Significant Terms 聚合例外）
* keyword 类型：无需分词、整段完整精确匹配。  
    * 适用于：email 地址、住址、状态码、分类 tags。  

&emsp; elasticsearch有六种搜索方式：  
1. query string search。  
2. query DSL，DSL(Domain Specific Language特定领域语言)以JSON请求体的形式出现。  
3. query filter，对数据进行过滤。  
4. full-text search，全文检索。  
5. phrase search，短语搜索。  
6. highlight search，高亮搜索。


### Searchtimeout  
&emsp; (1)设置：默认没有timeout，如果设置了timeout，那么会执行timeout机制。  
&emsp; (2)Timeout机制：假设用户查询结果有1W条数据，但是需要10″才能查询完毕，但是用户设置了1″的timeout，那么不管当前一共查询到了多少数据，都会在1″后ES讲停止查询，并返回当前数据。  
&emsp; (3)用法：GET /_search?timeout=1s/ms/m  

### 1.1.1. Query_string
<!-- 
https://blog.csdn.net/qq_33746789/article/details/83782932
--> 

&emsp; 示例：  
&emsp; ①查询所有：GET /product/_search  
&emsp; ②带参数：GET /product/_search?q=name:xiaomi  
&emsp; ③分页：GET /product/_search?from=0&size=2&sort=price:asc  

### 1.1.2. DSL  


&emsp; 示例：  
①match_all：匹配所有  

```text
GET /product/_search
{
  "query":{
    "match_all": {}
  }
}
```
②match：name中包含“nfc”  

```text
GET /product/_search
{
  "query": {
    "match": {
      "name": "nfc"
    }
  }
}
```
③sort：按照价格倒序排序  

```text
GET /product/_search
{
  "query": {
    "multi_match": {
      "query": "nfc",
      "fields": ["name","desc"]
    }
  },
  "sort": [
    {
      "price": "desc"
    }
  ]
}
```
④multi_match：根据多个字段查询一个关键词，name和desc中包含“nfc”的doc  

```text
GET /product/_search
{
  "query": {
    "multi_match": {
      "query": "nfc",
      "fields": ["name","desc"]
    }
  },
  "sort": [
    {
      "price": "desc"
    }
  ]
}
```
⑤_source 元数据：想要查询多个字段，例子中为只查询“name”和“price”字段。  

```text
GET /product/_search
{
  "query":{
    "match": {
      "name": "nfc"
    }
  },
  "_source": ["name","price"]
}
```
⑥分页（deep-paging）：查询第一页（每页两条数据）  

```text
GET /product/_search
{
  "query":{
    "match_all": {}
  },
  "sort": [
    {
      "price": "asc"
    }
  ], 
  "from": 0,
  "size": 2
}
```

#### 1.1.2.1. SQL代替DSL  
<!-- 
用SQL代替DSL查询ElasticSearch怎样？ 
https://mp.weixin.qq.com/s/CJkS3vu2BjUWfWrciwNVJg
如何用你最熟悉的 SQL 来查询 Elasticsearch 中的数据？ 
https://mp.weixin.qq.com/s/QQh0M85YqI-sHPnYy3pkBg
-->



### 1.1.4. full-text search(全文检索)  
<!-- 
ES搜索 term与match区别 bool查询
https://blog.csdn.net/qq_35240226/article/details/105275789
-->

①query-term：不会被分词，  

```text
GET /product/_search
{
  "query": {
    "term": {
      "name": "nfc"
    }
  }
}
```
②match和term区别：  
```text
GET /product/_search
{
  "query": {
    "term": {
      "name": "nfc phone" 这里因为没有分词，所以查询没有结果
    }
  }
}
GET /product/_search
{
  "query": {
    "bool": {
      "must": [
        {"term":{"name":"nfc"}},
        {"term":{"name":"phone"}}
      ]
    }
  }
}
GET /product/_search
{
  "query": {
    "terms": {
      "name":["nfc","phone"]
    }
  }
}
GET /product/_search
{
  "query": {
    "match": {
      "name": "nfc phone" 
    }
  }
}
```
③☆全文检索：  
```text
GET /product/_search
{
  "query": {
    "match": {
      "name": "xiaomi nfc zhineng phone"
    }
  }
}
#验证分词
GET /_analyze
{
  "analyzer": "standard",
  "text":"xiaomi nfc zhineng phone"
}
```

### 1.1.5. phrase search(短语搜索)
短语搜索，和全文检索相反，“nfc phone”会作为一个短语去检索    

```text
GET /product/_search
{
  "query": {
    "match_phrase": {
      "name": "nfc phone"
    }
  }
}
```

### 1.1.3. Query and filter(查询和过滤)
①bool：可以组合多个查询条件，bool查询也是采用more_matches_is_better的机制，因此满足must和should子句的文档将会合并起来计算分值。  
1)must：必须满足  
子句（查询）必须出现在匹配的文档中，并将有助于得分。  
2)filter：过滤器 不计算相关度分数，cache☆  
子句（查询）必须出现在匹配的文档中。但是不像 must查询的分数将被忽略。Filter子句在filter上下文中执行，这意味着计分被忽略，并且子句被考虑用于缓存。  
3)should：可能满足 or  
子句（查询）应出现在匹配的文档中。  
4)must_not：必须不满足 不计算相关度分数   not   
子句（查询）不得出现在匹配的文档中。子句在过滤器上下文中执行，这意味着计分被忽略，并且子句被视为用于缓存。由于忽略计分，0因此将返回所有文档的分数。  
5)minimum_should_match  
②案例：  
1)demo案例  
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
2)bool多条件 name包含xiaomi 不包含erji 描述里包不包含nfc都可以，价钱要大于等于4999  

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
③嵌套查询：  
1)minimum_should_match：参数指定should返回的文档必须匹配的子句的数量或百分比。如果bool查询包含至少一个should子句，而没有must或 filter子句，则默认值为1。否则，默认值为0  

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
2)案例：  

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

### Compound queries(组合查询)
①想要一台带NFC功能的 或者 小米的手机 但是不要耳机  

```text
SELECT * from product 
where (`name` like "%xiaomi%" or `name` like '%nfc%')
AND `name` not LIKE '%erji%'
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
②搜索一台xiaomi nfc phone或者一台满足 是一台手机 并且 价格小于等于2999  

```text
SELECT * FROM product 
WHERE NAME LIKE '%xiaomi nfc phone%' 
OR (
NAME LIKE '%erji%' 
AND price > 399 
AND price <=999);
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

### 1.1.6. highlight search
<!-- 
搜索模板、映射模板、高亮搜索和地理位置的简单玩法
https://mp.weixin.qq.com/s/BY0f47p6YETCVpQQDzG-dA
-->

GET /product/_search
{
    "query" : {
        "match_phrase" : {
            "name" : "nfc phone"
        }
    },
    "highlight":{
      "fields":{
         "name":{}
      }
    }
}








## 1.2. 检索模板  
<!-- 
https://www.cnblogs.com/Henry-pan/p/7242600.html
索引模版、检索模板问题 
https://mp.weixin.qq.com/s/gARgAKgqmUzfeHujK0n71g
https://blog.csdn.net/miaomiao19971215/article/details/106322234
-->
&emsp; 什么是搜索模板？  
&emsp; 检索模板（search template），在实战业务场景中：每次业务请求都要构造 DSL，比如：这次查title、下次查content，除此之外的 DSL 部分 都一样，但两次请求：后端代码那里就要有相应的修改和适配。有没有不修改、拼接DSL使用检索的方案？这就引出了搜索模板。  
&emsp; 搜索模板与关系数据库中的存储过程非常相似。可以将常用查询定义为模板，并且使用 Elasticsearch 的应用程序可以简单地通过其 ID 引用查询。  
&emsp; 模板接受在运行时指定参数。搜索模板存储在服务器端，可以在不更改客户端代码的情况下进行修改。  
&emsp; 模板使用Mustache模板引擎表示。关于 Mustache 可以访问：http://mustache.github.io/mustache.5.html。





