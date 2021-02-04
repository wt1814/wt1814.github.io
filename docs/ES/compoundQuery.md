


# 1. ~~复合查询~~ 
<!-- 
ElasticSearch 复合查询，理解 Es 中的文档评分策略！ 
https://mp.weixin.qq.com/s/59D8ouXbTMlh4swb6eY2zA
-->

## 1.1. Query and filter(查询和过滤)
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
 
## 1.2. Compound queries(组合查询)
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


