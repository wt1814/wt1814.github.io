
<!-- TOC -->

- [1. 检索模板](#1-检索模板)
    - [1.1. template入门案例](#11-template入门案例)
        - [1.1.1. 标准形式](#111-标准形式)
        - [1.1.2. toJson形式](#112-tojson形式)
        - [1.1.3. join方法传参](#113-join方法传参)
        - [1.1.4. default value形式](#114-default-value形式)
    - [1.2. 记忆template并实现重复调用](#12-记忆template并实现重复调用)
        - [1.2.1. Elasticsearch保存template](#121-elasticsearch保存template)
        - [1.2.2. 调用template](#122-调用template)
        - [1.2.3. 查询已定义的template](#123-查询已定义的template)
        - [1.2.4. 删除已定义的template](#124-删除已定义的template)

<!-- /TOC -->

# 1. 检索模板  
<!-- 
https://blog.csdn.net/miaomiao19971215/article/details/106322234
-->
&emsp; 什么是搜索模板？  
&emsp; 检索模板(search template)，在实战业务场景中：每次业务请求都要构造 DSL，比如：这次查title、下次查content，除此之外的DSL部分都一样，但两次请求：后端代码那里就要有相应的修改和适配。有没有不修改、拼接DSL使用检索的方案？这就引出了搜索模板。  
&emsp; 搜索模板与关系数据库中的存储过程非常相似。可以将常用查询定义为模板，并且使用Elasticsearch的应用程序可以简单地通过其ID引用查询。  
&emsp; 模板接受在运行时指定参数。搜索模板存储在服务器端，可以在不更改客户端代码的情况下进行修改。  
&emsp; 模板使用Mustache模板引擎表示。关于Mustache可以访问：http://mustache.github.io/mustache.5.html。

## 1.1. template入门案例  
&emsp; 本入门案例中定义的搜索模板仅在一次查询调用时生效，没有让Elsticsearch保存(记忆)搜索模板。  
&emsp; “source” 代表搜索模板，内含要执行的search语句  
&emsp; "params"是需要向模板中传递的变量。  
&emsp; {{变量名}}用于定义模板中的变量参数。  

### 1.1.1. 标准形式  
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

### 1.1.2. toJson形式
&emsp; toJSON形式的特点在于，source使用字符串来定义。注意: source内需要用到转义字符。  

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

### 1.1.3. join方法传参  
&emsp; join方式传入的是数组，让Elasticsearch来进行数据的拼接。  

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

&emsp; 等价于"remark": “大众 标志 奔驰 宝马”。  

### 1.1.4. default value形式  
&emsp; 以下语句中，^end表示为end这个参数设置了默认值。如果在params中传递了end，则使用传递的数值，如果没有传递end，则使用默认的数值。  

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

## 1.2. 记忆template并实现重复调用  
### 1.2.1. Elasticsearch保存template  
&emsp; 必须指定template的名称，方便后续反复调用。  

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

### 1.2.2. 调用template  
&emsp; 调用template时，通过id来指定具体的template。  

```text
GET index_name/_search/template
{
  "id": "my_test_template",
  "params": {
    "kw": "自定义template的参数值"
  }
}
```

### 1.2.3. 查询已定义的template  
```text
GET _scripts/template_name
```

### 1.2.4. 删除已定义的template  

```text
DELETE _scripts/template_name
```

