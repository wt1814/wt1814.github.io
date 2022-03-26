
<!-- TOC -->

- [1. 基本查询](#1-基本查询)
    - [1.1. match分词匹配](#11-match分词匹配)
    - [1.2. term精确匹配](#12-term精确匹配)
    - [1.3. phrase search(短语搜索)](#13-phrase-search短语搜索)
    - [1.4. prefix 前缀匹配](#14-prefix-前缀匹配)
    - [1.5. wildcard模糊匹配](#15-wildcard模糊匹配)
    - [1.6. query_string类型](#16-query_string类型)
    - [1.7. 查询结果过滤](#17-查询结果过滤)

<!-- /TOC -->

![image](http://www.wt1814.com/static/view/images/ES/es-79.png)  


# 1. 基本查询

## 1.1. match分词匹配  
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

&emsp; ④multi_match：根据多个字段查询一个关键词，name和desc中包含“nfc”的doc  

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

&emsp; ③sort：按照价格倒序排序  

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


## 1.2. term精确匹配  
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


## 1.3. phrase search(短语搜索)
<!-- 
https://mp.weixin.qq.com/s/Fc5LhiLJIeCtstl9OFeqdQ
-->
&emsp; 短语搜索，和match相反，match会将输入的搜索串拆解开来，去倒排索引里面一一匹配，只要能匹配上任意一个拆解后的单词，就可以作为结果返回。phrase search，要求输入的搜索串必须在指定的字段文本中完全包含一模一样的，才可以算匹配，才能作为结果返回。  
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

## 1.4. prefix 前缀匹配  
* 核心功能：前缀匹配。  
* 应用场景：前缀自动补全的业务场景。  
![image](http://www.wt1814.com/static/view/images/ES/es-78.png)  
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

## 1.5. wildcard模糊匹配  
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

## 1.6. query_string类型

* 核心功能：支持与或非表达式+其他N多配置参数。
* 应用场景：业务系统需要支持自定义表达式检索。
* 适用类型：text。

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

## 1.7. 查询结果过滤 
......

