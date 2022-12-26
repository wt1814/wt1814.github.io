
<!-- TOC -->

- [1. ~~映射详解~~](#1-映射详解)
    - [1.1. 动态映射](#11-动态映射)
        - [1.1.1. 字段动态映射规则(字段类型)](#111-字段动态映射规则字段类型)
        - [1.1.2. 自动识别日期类型](#112-自动识别日期类型)
        - [1.1.3. Numeric detection数值侦测](#113-numeric-detection数值侦测)
    - [1.2. 更新映射](#12-更新映射)
    - [1.3. 字段数据类型](#13-字段数据类型)
    - [1.4. 元字段](#14-元字段)
    - [1.5. 映射参数](#15-映射参数)
        - [1.5.1. 多字段](#151-多字段)
        - [1.5.2. 控制索引](#152-控制索引)
        - [1.5.3. ★★★空值处理](#153-★★★空值处理)
        - [1.5.4. 聚合多个字段](#154-聚合多个字段)

<!-- /TOC -->

&emsp; 空值处理：  
&emsp; 搜索address.keyword为空的数据，搜索返回异常，默认是不被允许搜索NUll。这时需要在Mapping指定null_value属性，并且不能在text类型中声明。  


# 1. ~~映射详解~~
&emsp; **<font color = "red">部分参考《Elasticsearch技术解析与实战》</font>**  

&emsp; Mapping (映射)类似关系型数据库中的表的结构定义。将数据以JSON格式存入到ElasticSearch后，在搜索引擎中JSON字段映射对应的类型，这时需要mapping来定义内容的类型。  

----
&emsp; **<font color = "red">映射是定义存储和索引的文档类型以及字段的过程。索引中的每一个文档都有一个类型，每种类型都有它自己的映射。一个映射定义了文档结构内每个字段的数据类型。映射通过配置来定义字段类型与该类型相关联的元数据的关系。</font>** 例如，可以通过映射来定义日期类型的格式、数字类型的格式或者文档中所有字段的值是否应该被_all字段索引等。本章将介绍映射的概念、参数，以及动态映射的使用等。  

## 1.1. 动态映射  
<!-- 
映射的分类

动态映射
    我们知道，在关系数据库中，需要事先创建数据库，然后在该数据库实例下创建数据表，然后才能在该数据表中插入数据。而ElasticSearch中不需要事先定义映射（Mapping），文档写入ElasticSearch时，会根据文档字段自动识别类型，这种机制称之为动态映射。

静态映射
    在ElasticSearch中也可以事先定义好映射，包含文档的各个字段及其类型等，这种方式称之为静态映射。

动态映射与静态映射 
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247490923&idx=2&sn=ce49834bca5cc2c21ee8334c2eec5461&scene=21#wechat_redirect
-->
<!-- 
~~
映射详解
ES中支持手动定义映射，动态映射两种方式。
https://mp.weixin.qq.com/s/gi9Dxt23chmEgDK9ZWfHLw
-->
&emsp; **<font color = "red">当没有事先定义好Mapping，添加数据时，ElasticSearch会自动根据字段进行换算出对应的类型，但是换算出来的类型并不一定是开发人员想要的字段类型，还是需要人为的干预进行修改成想要的Mapping。</font>**  

&emsp; 动态映射：ES中提供的重要特性，可以快速使用ES，而不需要先创建索引、定义映射。如直接向ES提交文档进行索引：  

```text
PUT data/_doc/1
{ "count": 5 }
```
&emsp; ES将自动为创建data索引、_doc映射、类型为long的字段count。  
&emsp; 索引文档时，当有新字段时ES将根据字段的json的数据类型自动加入字段定义到mapping中。  

### 1.1.1. 字段动态映射规则(字段类型)  

![image](http://182.92.69.8:8081/img/ES/es-34.png)  
&emsp; JSON数据类型映射到 ElasticSearch 定义的类型，常用的简单类型有：  

|JSON中的数据|自动推断出来的数据类型|
|---|---|
|null |没有字段被添加|  
|布尔值|	boolean  |
|浮点类型	|float/double  |
|整数类型	|long/integer  |
|数组|	数组中的第一个非空值来决定| 
|文本类型|	Text/Keyword| 

### 1.1.2. 自动识别日期类型  
&emsp; Date detection时间侦测：所谓时间侦测是指往ES里面插入数据的时候会去自动检测数据是不是日期格式的，是的话就会自动转为设置的格式。  
&emsp; date_detection 默认是开启的，默认的格式dynamic_date_formats为：  

```text
[ "strict_date_optional_time","yyyy/MM/dd HH:mm:ss Z||yyyy/MM/dd Z"]
PUT my_index/_doc/1
{
  "create_date": "2015/09/02"
}

GET my_index/_mapping
```

&emsp; 自定义时间格式：   

```text
PUT my_index
{
  "mappings": {
    "_doc": {
      "dynamic_date_formats": ["MM/dd/yyyy"]
    }
  }
}
```

&emsp; 禁用时间侦测：  

```text
PUT my_index
{
  "mappings": {
    "_doc": {
      "date_detection": false
    }
  }
}
```

### 1.1.3. Numeric detection数值侦测
&emsp; 开启数值侦测（默认是禁用的）

```text
PUT my_index
{
  "mappings": {
    "_doc": {
      "numeric_detection": true
    }
  }
}
PUT my_index/_doc/1
{
  "my_float":   "1.0",
  "my_integer": "1"
}
```


## 1.2. 更新映射  
&emsp; 使用dynamic控制映射是否可以被更新。  
* dynamic-true  
&emsp; 设置dynamic为true是默认dynamic的默认值，新增字段数据可以写入，同时也可以被索引，Mapping结构也会被更新。  
![image](http://182.92.69.8:8081/img/ES/es-45.png)  
&emsp; 添加数据，同时多添加一个没被定义的gender字段。  

  ```text
  # 向 person 中添加数据
  PUT person/_doc/1
  {
      "uId": 1,
      "name": "ytao",
      "age": 18,
      "address": "广东省珠海市",
      "birthday": "2020-01-15T12:00:00Z",
      "money": 108.2,
      "isStrong": true,
      "gender": "男"    # Mapping 中未定义的字段
  }
  ```

  &emsp; 添加成功，搜索gender字段：  
  ![image](http://182.92.69.8:8081/img/ES/es-46.png)  
  &emsp; 查看Mapping结构：   
  ![image](http://182.92.69.8:8081/img/ES/es-47.png)  
  &emsp; 新添加的字段值，在添加过程中Mapping已自动添加字段。  

* dynamic-false  
&emsp; 设置dynamic为false时，新增字段数据可以写入，不可以被索引，Mapping结构会被更新。同样先将dynamic设置为false，然后向里面添加数据，其他步骤和上面true操作一样。定义Mapping，添加数据。搜索gender字段：   
![image](http://182.92.69.8:8081/img/ES/es-48.png)   
&emsp; 此时新增字段数据无法被索引，但数据可以写入。  
![image](http://182.92.69.8:8081/img/ES/es-49.png)  
&emsp; Mappnig也不会添加新增的字段：  
![image](http://182.92.69.8:8081/img/ES/es-50.png)  

* dynamic-strict  
&emsp; 设置dynamic为strict时，从字面上意思也可以看出，对于动态映射是较严格的，新增字段数据不可以写入，不可以被索引，Mapping结构不会被更新。只能按照定义好的 Mapping结构添加数据。在添加新字段数据时，就马上会抛出异常：  
![image](http://182.92.69.8:8081/img/ES/es-51.png)  

## 1.3. 字段数据类型  
<!--
四种字段类型详解
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247490952&idx=1&sn=bd34ff8633b3a101eb0f6a7dfd9a2c94&scene=21#wechat_redirects
地理类型和特殊类型
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247490994&idx=1&sn=77ec1e677a9c77379956530808f30887&scene=21#wechat_redirect

Elasticsearch技术解析与实战 第3.2章
-->
&emsp; 字段类型定义了该如何索引存储字段值。ES中提供了丰富的字段类型定义，请查看官网链接详细了解每种类型的特点：  
https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html  

## 1.4. 元字段  
<!-- 
Elasticsearch技术解析与实战 第3.3章
-->


## 1.5. 映射参数  
<!-- 
ElasticSearch系列18：Mapping 设计指南 
https://mp.weixin.qq.com/s/Ti4V7sMUCZjqfYjQ7RjcFg
-->
### 1.5.1. 多字段  
&emsp; Mapping 中可以定义 fields 多字段属性，以满足不同场景下的实现。比如 address 定义为 text 类型，fields 里面又有定义 keyword 类型，这里主要是区分两个不同不同使用场景。  

* text 会建立分词倒排索引，用于全文检索。
* keyword 不会建立分词倒排索引，用于排序和聚合。

&emsp; 添加数据：  

```text
# 向 person 中添加数据
PUT person/_doc/1
{
    "uId": 1,
    "name": "ytao",
    "age": 18,
    "address": "广东省珠海市",
    "birthday": "2020-01-15T12:00:00Z",
    "money": 108.2,
    "isStrong": true
}
```
&emsp; 查询 address数据。  
![image](http://182.92.69.8:8081/img/ES/es-43.png)  
&emsp; 查询 address.keyword数据。  
![image](http://182.92.69.8:8081/img/ES/es-44.png)  
&emsp; 通过 keyword检索时，由于不会建立分词索引，并没有获取到数据。  

### 1.5.2. 控制索引  
&emsp; 在字段中使用 index 指定当前字段索引是否能被搜索到。指定类型为 boolean 类型，false 为不可搜索到，true 为可以搜索到。先删除之前的 Mapping：  

```text
DELETE person
```
&emsp; 创建 Mapping，设置 name属性的 index 为 false。  
![image](http://182.92.69.8:8081/img/ES/es-41.png)  
&emsp; 再次添加上面的数据后搜索 name字段：  
![image](http://182.92.69.8:8081/img/ES/es-42.png)  
&emsp; 字段 index 设置 false 后，由于没有被索引，所以搜索无法获取到索引。  

### 1.5.3. ★★★空值处理
&emsp; 现在向 ElasticSearch 中添加一条 address 为空的数据：  

```text
PUT person/_doc/2

{
    "uId": 2,
    "name": "Jack",
    "age": 22,
    "address": null,
    "birthday": "2020-01-15T12:00:00Z",
    "money": 68.7,
    "isStrong": true
}
```
&emsp; 搜索 address.keyword 为空的数据： 
![image](http://182.92.69.8:8081/img/ES/es-36.png)  
&emsp; 搜索返回异常，默认是不被允许搜索NUll。这是需要在Mapping指定null_value属性，并且不能在text类型中声明。  
![image](http://182.92.69.8:8081/img/ES/es-88.png)  
&emsp; 搜索address.keyword 为空的数据：  
![image](http://182.92.69.8:8081/img/ES/es-38.png)  
&emsp; 设置 "null_value":"NULL" 后，空值可以处理搜索。  

### 1.5.4. 聚合多个字段  
&emsp; 聚合多个字段放到一个索引中，使用 copy_to 进行聚合。例如在多字段查询中，这是不需要对每个字段进行过滤筛选，只需对聚合字段即可。在使用copy_to时，是通过指定聚合的名称实现。  
&emsp; 实际上，copy_to 不使用数组格式添加名称，也会自动转换成数据格式。  
&emsp; 添加两条数据，待校验搜索：  

```text
# 向 person 中添加数据
PUT person/_doc/1
{
    "uId": 1,
    "name": "ytao",
    "age": 18,
    "address": "广东省珠海市",
    "birthday": "2020-01-15T12:00:00Z",
    "money": 108.2,
    "isStrong": true
}

PUT person/_doc/2
{
    "uId": 2,
    "name": "杨广东",
    "age": 22,
    "address": null,
    "birthday": "2020-01-15T12:00:00Z",
    "money": 68.7,
    "isStrong": true
}
```
![image](http://182.92.69.8:8081/img/ES/es-39.png)  
&emsp; 查询 full_name 的值，会返回 name 和 address 相关的值的对象。  
![image](http://182.92.69.8:8081/img/ES/es-40.png)  
&emsp; 从上面返回结果看到，_source 中的字段没有增加相应的 copy_to 字段名，所以 copy_to 只会拷贝字段内容至索引，并不会改变包含的字段。  

