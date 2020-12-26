<!-- TOC -->

- [1. 检索操作](#1-检索操作)
    - [1.1. 检索操作](#11-检索操作)
        - [1.1.1. Searchtimeout](#111-searchtimeout)
        - [1.1.2. query string search](#112-query-string-search)
        - [1.1.3. match分词匹配](#113-match分词匹配)
            - [1.1.3.1. SQL代替DSL](#1131-sql代替dsl)
        - [1.1.4. term精确匹配](#114-term精确匹配)
        - [1.1.5. phrase search(短语搜索)](#115-phrase-search短语搜索)
        - [1.1.6. prefix 前缀匹配](#116-prefix-前缀匹配)
        - [1.1.7. wildcard模糊匹配](#117-wildcard模糊匹配)
        - [1.1.8. query_string 类型](#118-query_string-类型)
        - [1.1.9. Query and filter(查询和过滤)](#119-query-and-filter查询和过滤)
        - [1.1.10. Compound queries(组合查询)](#1110-compound-queries组合查询)
        - [1.1.11. highlight search](#1111-highlight-search)
        - [1.1.12. 小结](#1112-小结)
    - [避免深度分页](#避免深度分页)
    - [1.2. 检索模板](#12-检索模板)
        - [1.2.1. template入门案例](#121-template入门案例)
            - [1.2.1.1. 标准形式](#1211-标准形式)
            - [1.2.1.2. toJson形式](#1212-tojson形式)
            - [1.2.1.3. join方法传参](#1213-join方法传参)
            - [1.2.1.4. default value形式](#1214-default-value形式)
        - [1.2.2. 记忆template并实现重复调用](#122-记忆template并实现重复调用)
            - [1.2.2.1. Elasticsearch保存template](#1221-elasticsearch保存template)
            - [1.2.2.2. 调用template](#1222-调用template)
            - [1.2.2.3. 查询已定义的template](#1223-查询已定义的template)
            - [1.2.2.4. 删除已定义的template](#1224-删除已定义的template)

<!-- /TOC -->

# 1. 检索操作  
<!-- 
ElasticSearch 搜索入门 
https://mp.weixin.qq.com/s/WVInd3kCciTVa1nzOgeEAQ

检索类型如何选型呢？
https://mp.weixin.qq.com/s/Fc5LhiLJIeCtstl9OFeqdQ

fuzzy query
https://mp.weixin.qq.com/s/ReiCivwDINsE8S5kwUWb5w

ElasticSearch 复合查询，理解 Es 中的文档评分策略！ 
https://mp.weixin.qq.com/s/59D8ouXbTMlh4swb6eY2zA

ElasticSearch 如何像 MySQL 一样做多表联合查询？ 
https://mp.weixin.qq.com/s/SDRI7GmZmmO7bLvhCgUDCg
ElasticSearch 搜索高亮与排序 
https://mp.weixin.qq.com/s/pxHjq0ejT0Fy9v7-dpg0Tw
Elasticsearch之评分机制
https://www.jianshu.com/p/2624f61f1d02
-->

## 1.1. 检索操作  
&emsp; 搜索分为两个过程：  

* 当向索引中保存文档时，默认情况下，es 会保存两份内容，一份是 _source  中的数据，另一份则是通过分词、排序等一系列过程生成的倒排索引文件，倒排索引中保存了词项和文档之间的对应关系。  
* 搜索时，当 es 接收到用户的搜索请求之后，就会去倒排索引中查询，通过的倒排索引中维护的倒排记录表找到关键词对应的文档集合，然后对文档进行评分、排序、高亮等处理，处理完成后返回文档。  

&emsp; 5.X 版本之后，string 类型不再存在，取代的是text和keyword类型。  
* text 类型作用：分词，将大段的文字根据分词器切分成独立的词或者词组，以便全文检索。  
    * 适用于：email 内容、某产品的描述等需要分词全文检索的字段；
    * 不适用：排序或聚合（Significant Terms 聚合例外）
* keyword 类型：无需分词、整段完整精确匹配。  
    * 适用于：email 地址、住址、状态码、分类 tags。  

&emsp; elasticsearch的搜索方式：  
1. query string search。  
2. query DSL，DSL(Domain Specific Language特定领域语言)以JSON请求体的形式出现。  

&emsp; DSL：Domain Specified Language，特定领域的语言。  
&emsp; http request body：请求体，可以用json的格式来构建查询语法，比较方便，可以构建各种复杂的语法。  

### 1.1.1. Searchtimeout  
&emsp; (1)设置：默认没有timeout，如果设置了timeout，那么会执行timeout机制。  
&emsp; (2)Timeout机制：假设用户查询结果有1W条数据，但是需要10″才能查询完毕，但是用户设置了1″的timeout，那么不管当前一共查询到了多少数据，都会在1″后ES讲停止查询，并返回当前数据。  
&emsp; (3)用法：GET /_search?timeout=1s/ms/m  

### 1.1.2. query string search
&emsp; 示例：  
&emsp; ①查询所有：GET /product/_search  
&emsp; ②带参数：GET /product/_search?q=name:xiaomi  
&emsp; ③分页：GET /product/_search?from=0&size=2&sort=price:asc  

### 1.1.3. match分词匹配  
* 核心功能：全文检索，分词词项匹配。  
* 应用场景：实际业务中较少使用，原因：匹配范围太宽泛，不够准确。  
* 适用类型：text。  

&emsp; 示例：  
&emsp; ①match_all：匹配所有  

```text
GET /product/_search
{
  "query":{
    "match_all": {}
  }
}
```
&emsp; ②match：name中包含“nfc”  

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
&emsp; ③sort：按照价格倒序排序  

&emsp; multi_match 多组匹配   

* 核心功能：match query 针对多字段的升级版本。
* 应用场景：多字段检索。
* 适用类型：text。

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
&emsp; ④multi_match：根据多个字段查询一个关键词，name和desc中包含“nfc”的doc  

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
&emsp; ⑤_source 元数据：想要查询多个字段，例子中为只查询“name”和“price”字段。  

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
&emsp; ⑥分页（deep-paging）：查询第一页（每页两条数据）  

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

#### 1.1.3.1. SQL代替DSL  
<!-- 
用SQL代替DSL查询ElasticSearch怎样？ 
https://mp.weixin.qq.com/s/CJkS3vu2BjUWfWrciwNVJg
如何用你最熟悉的 SQL 来查询 Elasticsearch 中的数据？ 
https://mp.weixin.qq.com/s/QQh0M85YqI-sHPnYy3pkBg
-->



### 1.1.4. term精确匹配  
<!-- 
ES搜索 term与match区别 bool查询
https://blog.csdn.net/qq_35240226/article/details/105275789
-->
* 核心功能：不受到分词器的影响，属于完整的精确匹配。  
* 应用场景：精确、精准匹配。  
* 适用类型：keyword。  

&emsp; ①query-term：不会被分词，  
term是代表完全匹配，也就是精确查询，搜索前不会再对搜索词进行分词拆解。  
term属于精确匹配，只能查单个词。想用term匹配多个词，可以使用terms来：  
terms里的[ ] 多个是或者的关系，只要满足其中一个词就可以。想要通知满足两个词的话，就得使用bool的must来做。  

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


### 1.1.5. phrase search(短语搜索)
<!-- 
https://mp.weixin.qq.com/s/Fc5LhiLJIeCtstl9OFeqdQ
-->
&emsp; 短语搜索，和match相反，match会将输入的搜索串拆解开来，去倒排索引里面一一匹配，只要能匹配上任意一个拆解后的单词，就可以作为结果返回，phrase search，要求输入的搜索串必须在指定的字段文本中完全包含一模一样的，才可以算匹配，才能作为结果返回。  
&emsp; “nfc phone”会作为一个短语去检索。    

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

### 1.1.6. prefix 前缀匹配  
* 核心功能：前缀匹配。  
* 应用场景：前缀自动补全的业务场景。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-78.png)  
* 适用类型：keyword。  

&emsp; 如下能匹配到文档 id 为 1 的文章。  

```text
POST zz_test/_search
{
  "query": {
    "prefix": {
      "title.keyword": "锤子加湿器"
    }
  }
}
```

### 1.1.7. wildcard模糊匹配  
* 核心功能：匹配具有匹配通配符表达式 keyword 类型的文档。支持的通配符：*，它匹配任何字符序列（包括空字符序列）；？，它匹配任何单个字符。  
* 应用场景：请注意，选型务必要慎重！此查询可能很慢多组关键次的情况下可能会导致宕机，因为它需要遍历多个术语。为了防止非常慢的通配符查询，通配符不能以任何一个通配符*或？开头。  
* 适用类型：keyword。  

&emsp; 如下匹配，类似 MySQL 中的通配符匹配，能匹配所有包含加湿器的文章。

```text
POST zz_test/_search
{
  "query": {
    "wildcard": {
      "title.keyword": "*加湿器*"
    }
  }
}
```
### 1.1.8. query_string 类型

    核心功能：支持与或非表达式+其他N多配置参数。
    应用场景：业务系统需要支持自定义表达式检索。
    适用类型：text。

```text
POST zz_test/_search
{
  "query": {
    "query_string": {
      "default_field": "title",
      "query": "(锤子 AND 加湿器) OR (官方 AND 道歉)"
    }
  }
}
```

### 1.1.9. Query and filter(查询和过滤)
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

### 1.1.10. Compound queries(组合查询)
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

### 1.1.11. highlight search
<!-- 
搜索模板、映射模板、高亮搜索和地理位置的简单玩法
https://mp.weixin.qq.com/s/BY0f47p6YETCVpQQDzG-dA
-->

```text
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
```

### 1.1.12. 小结  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-79.png)  

## 避免深度分页  
<!-- 
https://www.cnblogs.com/hello-shf/p/11543453.html
-->


## 1.2. 检索模板  
<!-- 
https://blog.csdn.net/miaomiao19971215/article/details/106322234
-->
&emsp; 什么是搜索模板？  
&emsp; 检索模板（search template），在实战业务场景中：每次业务请求都要构造 DSL，比如：这次查title、下次查content，除此之外的 DSL 部分 都一样，但两次请求：后端代码那里就要有相应的修改和适配。有没有不修改、拼接DSL使用检索的方案？这就引出了搜索模板。  
&emsp; 搜索模板与关系数据库中的存储过程非常相似。可以将常用查询定义为模板，并且使用 Elasticsearch 的应用程序可以简单地通过其 ID 引用查询。  
&emsp; 模板接受在运行时指定参数。搜索模板存储在服务器端，可以在不更改客户端代码的情况下进行修改。  
&emsp; 模板使用Mustache模板引擎表示。关于 Mustache 可以访问：http://mustache.github.io/mustache.5.html。

### 1.2.1. template入门案例  
&emsp; 本入门案例中定义的搜索模板仅在一次查询调用时生效，没有让Elsticsearch保存(记忆)搜索模板。  
&emsp; “source” 代表搜索模板，内含要执行的search语句  
&emsp; "params"是需要向模板中传递的变量。  
&emsp; {{变量名}}用于定义模板中的变量参数。  

#### 1.2.1.1. 标准形式  
&emsp; 可以发现，如果去掉"source"标签，实际上这就是一个标准的搜索条件语句。  

```text
GET /index_name/_search/template
{
  "source": {
    "query": {
      "match": {
        "remark": "{{kw}}"
      }
    },
    "size": "{{size}}"
  },
  "params": {
    "kw": "真正的数值",
    "size": 100
  }
}
```

#### 1.2.1.2. toJson形式
toJSON形式的特点在于，source使用字符串来定义。注意: source内需要用到转义字符。  

```text
GET cars/_search/template
{
  "source": "{ \"query\": { \"match\": {{#toJson}}parameter{{/toJson}} }}",
  "params": {
    "parameter" : {
      "remark" : "真正的数值"
    }
  }
}
```

#### 1.2.1.3. join方法传参  
join方式传入的是数组，让Elasticsearch来进行数据的拼接。  

```text
GET index_name/_search/template
{
  "source" : {
    "query" : {
      "match" : {
        "remark" : "{{#join delimiter=' '}}kw{{/join delimiter=' '}}"
      }
    }
  },
  "params": {
    "kw" : ["大众", "标致", "奔驰", "宝马"]
  }
}
```

等价于"remark": “大众 标志 奔驰 宝马”。  

#### 1.2.1.4. default value形式  
以下语句中，^end表示为end这个参数设置了默认值。如果在params中传递了end，则使用传递的数值，如果没有传递end，则使用默认的数值。  

```text
GET index_name/_search/template
{
  "source" : {
    "query" : {
      "range" : {
        "price" : {
          "gte" : "{{start}}",
          "lte" : "{{end}}{{^end}}200000{{/end}}"
        }
      }
    }
  },
  "params": {
    "start" : 100000
  }
}
```

### 1.2.2. 记忆template并实现重复调用  
#### 1.2.2.1. Elasticsearch保存template  
必须指定template的名称，方便后续反复调用。  

```text
POST _scripts/my_test_template
{
  "script": {
    "lang": "mustache",
    "source": {
      "query": {
        "match": {
          "field_name": "{{kw}" 
        }
      }
    }
  }
}
```

#### 1.2.2.2. 调用template  
调用template时，通过id来指定具体的template。  

```text
GET index_name/_search/template
{
  "id": "my_test_template",
  "params": {
    "kw": "自定义template的参数值"
  }
}
```

#### 1.2.2.3. 查询已定义的template  
```text
GET _scripts/template_name
```

#### 1.2.2.4. 删除已定义的template  

```text
DELETE _scripts/template_name
```

