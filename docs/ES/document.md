
<!-- TOC -->

- [1. 文档操作](#1-文档操作)
    - [1.1. 文档基本操作](#11-文档基本操作)
        - [1.1.1. 新建文档](#111-新建文档)
        - [1.1.2. 获取文档](#112-获取文档)
        - [1.1.3. 文档更新](#113-文档更新)
            - [1.1.3.1. 普通更新](#1131-普通更新)
            - [1.1.3.2. 查询更新](#1132-查询更新)
        - [1.1.4. 删除文档](#114-删除文档)
    - [1.2. 批量操作](#12-批量操作)
    - [1.3. 索引词频率](#13-索引词频率)

<!-- /TOC -->

&emsp; **<font color = "blue">★★★注：文档元数据（有哪些字段）由映射Mapping决定。</font>**    


# 1. 文档操作  
&emsp; **<font color = "red">部分参考《Elasticsearch技术解析与实战》</font>**  

<!-- 
~~
ElasticSearch 文档的添加、获取以及更新 
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247490740&idx=1&sn=ba34fcdb40b82361aab3c738bcea2aa9&scene=21#wechat_redirect
ElasticSearch 文档的删除和批量操作 
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247490840&idx=2&sn=3bf45591fb8d383c06b49b16331482b3&scene=21#wechat_redirect
-->
&emsp; 文档是具体的数据，一个文档有点像数据库中的一条记录，文档必须包含在一个索引中。  
&emsp; **<font color = "blue">★★★注：文档元数据（有哪些字段）由映射Mapping决定。</font>**    

## 1.1. 文档基本操作 
### 1.1.1. 新建文档
&emsp; 首先新建一个索引。  
&emsp; 然后向索引中添加一个文档： 1 表示新建文档的 id。  

```text
PUT blog/_doc/1
{
  "title":"ElasticSearch 文档基本操作",
  "date":"2020-11-05",
  "content":"首先新建一个索引。"
}
```

&emsp; 添加成功后，响应的json如下：  

```text
{
  "_index" : "blog",
  "_type" : "_doc",
  "_id" : "1",
  "_version" : 1,
  "result" : "created",
  "_shards" : {
    "total" : 2,
    "successful" : 2,
    "failed" : 0
  },
  "_seq_no" : 0,
  "_primary_term" : 1
}
```
    _index 表示文档索引。
    _type 表示文档的类型。
    _id 表示文档的 id。
    _version 表示文档的版本(更新文档，版本会自动加 1，针对一个文档的)。
    result 表示执行结果。
    _shards 表示分片信息。
    _seq_no 和 _primary_term 这两个也是版本控制用的(针对当前 index)。

&emsp; 添加成功后，可以查看添加的文档：  
![image](http://www.wt1814.com/static/view/images/ES/es-27.png)  
&emsp; 当然，添加文档时，也可以不指定id，此时系统会默认给出一个id，如果不指定id，则需要使用POST请求，而不能使用PUT请求。  

```text
POST blog/_doc
{
  "title":"666",
  "date":"2020-11-05",
  "content":"微信公众号**江南一点雨**后台回复 **elasticsearch06** 下载本笔记。首先新建一个索引。"
}
```

### 1.1.2. 获取文档
&emsp; Es 中提供了 GET API 来查看存储在 es 中的文档。使用方式如下：下面这个命令表示获取一个id为RuWrl3UByGJWB5WucKtP的文档。   
  
    GET blog/_doc/RuWrl3UByGJWB5WucKtP  

&emsp; 如果获取不存在的文档，会返回如下信息：  

```text
{
  "_index" : "blog",
  "_type" : "_doc",
  "_id" : "2",
  "found" : false
}
```
&emsp; 如果仅仅只是想探测某一个文档是否存在，可以使用 head 请求：  
&emsp; 如果文档不存在，响应如下：  
![image](http://www.wt1814.com/static/view/images/ES/es-28.png)  
&emsp; 如果文档存在，响应如下：  
![image](http://www.wt1814.com/static/view/images/ES/es-29.png)  
&emsp; 当然也可以批量获取文档。  

```text
GET blog/_mget
{
  "ids":["1","RuWrl3UByGJWB5WucKtP"]
}
``` 

### 1.1.3. 文档更新
#### 1.1.3.1. 普通更新
&emsp; 注意，文档更新一次，version 就会自增 1。  
&emsp; 可以直接更新整个文档：  

```text
PUT blog/_doc/RuWrl3UByGJWB5WucKtP
{
  "title":"666"
}
```
&emsp; 这种方式，更新的文档会覆盖掉原文档。  
&emsp; 大多数时候，只是想更新文档字段，这个可以通过脚本来实现。  

```text
POST blog/_update/1
{
  "script": {
    "lang": "painless",
    "source":"ctx._source.title=params.title",
    "params": {
      "title":"666666"
    }
  }
}
```
&emsp; 更新的请求格式：POST {index}/_update/{id}  
&emsp; 在脚本中，lang 表示脚本语言，painless 是 es 内置的一种脚本语言。source 表示具体执行的脚本，ctx 是一个上下文对象，通过 ctx 可以访问到_source、_title 等。   
&emsp; 也可以向文档中添加字段：    

```text
POST blog/_update/1
{
  "script": {
    "lang": "painless",
    "source":"ctx._source.tags=[\"java\",\"php\"]"
  }
}
```
&emsp; 添加成功后的文档如下：   
![image](http://www.wt1814.com/static/view/images/ES/es-30.png)  
&emsp; 通过脚本语言，也可以修改数组。例如再增加一个 tag：  

```text
POST blog/_update/1
{
  "script":{
    "lang": "painless",
    "source":"ctx._source.tags.add(\"js\")"
  }
}
```
&emsp; 当然，也可以使用 if else 构造稍微复杂一点的逻辑。  

```text
POST blog/_update/1
{
  "script": {
    "lang": "painless",
    "source": "if (ctx._source.tags.contains(\"java\")){ctx.op=\"delete\"}else{ctx.op=\"none\"}"
  }
}
```

#### 1.1.3.2. 查询更新
&emsp; 通过条件查询找到文档，然后再去更新。  
&emsp; 例如将 title 中包含 666 的文档的 content 修改为 888。  

```text
POST blog/_update_by_query
{
  "script": {
    "source": "ctx._source.content=\"888\"",
    "lang": "painless"
  },
  "query": {
    "term": {
      "title":"666"
    }
  }
}
```

### 1.1.4. 删除文档
&emsp; 根据 id 删除，从索引中删除一个文档。  
&emsp; 删除一个 id 为 TuUpmHUByGJWB5WuMasV 的文档。  

```text
DELETE blog/_doc/TuUpmHUByGJWB5WuMasV
```
&emsp; 如果在添加文档时指定了路由，则删除文档时也需要指定路由，否则删除失败。  

&emsp; 查询删除  
&emsp; 例如删除 title 中包含 666 的文档：  

```text
POST blog/_delete_by_query
{
  "query":{
    "term":{
      "title":"666"
    }
  }
}
```

&emsp; 也可以删除某一个索引下的所有文档：  

```text
POST blog/_delete_by_query
{
  "query":{
    "match_all":{
      
    }
  }
}
```

## 1.2. 批量操作
&emsp; es 中通过Bulk API可以执行批量索引、批量删除、批量更新等操作。  
&emsp; 首先需要将所有的批量操作写入一个JSON文件中，然后通过POST请求将该JSON文件上传并执行。    
&emsp; 例如新建一个名为 aaa.json的文件，内容如下：  
![image](http://www.wt1814.com/static/view/images/ES/es-31.png)  
&emsp; 首先第一行：index表示要执行一个索引操作(这个表示一个action，其他的action还有create，delete，update)。_index定义了索引名称，这里表示要创建一个名为 user的索引，_id表示新建文档的id为666。  
&emsp; 第二行是第一行操作的参数。  
&emsp; 第三行的 update 则表示要更新。  
&emsp; 第四行是第三行的参数。  
&emsp; 注意，结尾要空出一行。  

&emsp; aaa.json文件创建成功后，在该目录下，执行请求命令，如下：  

```text
curl -XPOST "http://localhost:9200/user/_bulk" -H "content-type:application/json" --data-binary @aaa.json
```
&emsp; 执行完成后，就会创建一个名为user的索引，同时向该索引中添加一条记录，再修改该记录，最终结果如下：  
![image](http://www.wt1814.com/static/view/images/ES/es-32.png)  

## 1.3. 索引词频率  
&emsp; term vector是在Lucene中的一个概念，就是对于文档的某一列，如title、body这种文本类型的建立词频的多维向量空间，每一个词就是一个维度，这个维度的值就是这个词在这个列中的频率。在Elasticsearch中termvectors返回在索引中特定文档字段的统计信息，termvectors在Elasticsearch中是实时分析的，如果要想不实时分析，可以设置realtime参数为falseo默认情况下索引词频率统计是关闭的，需要在建索引的时候手工打开。  
