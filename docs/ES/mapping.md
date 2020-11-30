
<!-- TOC -->

- [1. 映射详解](#1-映射详解)
    - [1.1. 定义映射](#11-定义映射)
    - [1.2. 动态映射](#12-动态映射)
        - [1.2.1. 字段动态映射规则(字段类型)](#121-字段动态映射规则字段类型)
        - [1.2.2. 自动识别日期类型](#122-自动识别日期类型)
        - [1.2.3. Numeric detection数值侦测](#123-numeric-detection数值侦测)
    - [1.3. 更新映射](#13-更新映射)
    - [1.4. 多字段](#14-多字段)
    - [1.5. 控制索引](#15-控制索引)
    - [1.6. 空值处理](#16-空值处理)
    - [1.7. 聚合多个字段](#17-聚合多个字段)

<!-- /TOC -->

# 1. 映射详解
<!--

ES的文档映射
https://www.cnblogs.com/laoyeye/p/13289153.html

-->

<!-- 
~~
ElasticSearch之映射常用操作 
https://mp.weixin.qq.com/s/er3SAyhMcHl5Ql7OuJ-keA
映射详解
https://mp.weixin.qq.com/s/gi9Dxt23chmEgDK9ZWfHLw
-->
&emsp; Mapping (映射)类似关系型数据库中的表的结构定义。将数据以 JSON 格式存入到 ElasticSearch 中后，在搜索引擎中 JSON 字段映射对应的类型，这时需要 mapping 来定义内容的类型。  

## 1.1. 定义映射  
&emsp; 在关系型数据库中，存储数据之前，会先创建表结构，给字段指定一个存在的类型。同样ElasticSearch在进行数据存储前，也可以先定义好存储数据的Mapping结构。先定义一个简单的person Mapping：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-33.png)  
&emsp; 上图中就是一个 Mapping 的定义，如果是在 ElasticSearch7之前，mappings 里还有 _type 属性。  

## 1.2. 动态映射  
<!-- 
动态映射与静态映射 
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247490923&idx=2&sn=ce49834bca5cc2c21ee8334c2eec5461&scene=21#wechat_redirect
-->
&emsp; 当没有事先定义好 Mapping，添加数据时，ElasticSearch 会自动根据字段进行换算出对应的类型，但是换算出来的类型并不一定是我们想要的字段类型，还是需要人为的干预进行修改成想要的 Mapping。  

&emsp; 动态映射：ES中提供的重要特性，让我们可以快速使用ES，而不需要先创建索引、定义映射。如直接向ES提交文档进行索引：  

```text
PUT data/_doc/1
{ "count": 5 }
```
&emsp; ES将自动为创建data索引、_doc 映射、类型为 long 的字段 count  
&emsp; 索引文档时，当有新字段时， ES将根据我们字段的json的数据类型为我们自动加人字段定义到mapping中。  

### 1.2.1. 字段动态映射规则(字段类型)  
<!--
字段类型定义了该如何索引存储字段值。ES中提供了丰富的字段类型定义，请查看官网链接详细了解每种类型的特点：
https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html

四种字段类型详解
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247490952&idx=1&sn=bd34ff8633b3a101eb0f6a7dfd9a2c94&scene=21#wechat_redirects
地理类型和特殊类型
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247490994&idx=1&sn=77ec1e677a9c77379956530808f30887&scene=21#wechat_redirect
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-34.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-35.png)  
&emsp; JSON 数据类型映射到 ElasticSearch 定义的类型，常用的简单类型有：  

JSON类型	ElasticSearch 类型  
文本类型	Text/Keyword  
整数类型	long/integer  
浮点类型	float/double  
时间类型	date  
布尔值	boolean  
数组	Text/Keyword  

&emsp; 上面要注意的是时间类型，JSON 中并没有时间类型，这里主要指时间格式数据的类型。  

### 1.2.2. 自动识别日期类型  
&emsp; Date detection 时间侦测  
&emsp; 所谓时间侦测是指我们往ES里面插入数据的时候会去自动检测我们的数据是不是日期格式的，是的话就会给我们自动转为设置的格式  
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

### 1.2.3. Numeric detection数值侦测
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

## 1.3. 更新映射  
&emsp; 使用 dynamic 控制映射是否可以被更新。  
* dynamic-true  
&emsp; 设置 dynamic 为 true是默认 dynamic 的默认值，新增字段数据可以写入，同时也可以被索引，Mapping 结构也会被更新。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-45.png)  
&emsp; 添加数据，同时多添加一个没被定义的 gender 字段。  

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
&emsp; 添加成功，搜索 gender 字段：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-46.png)  
&emsp; 查看 Mapping 结构：   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-47.png)  
&emsp; 新添加的字段值，在添加过程中 Mapping 已自动添加字段。  

* dynamic-false  
&emsp; 设置 dynamic 为 false时，新增字段数据可以写入，不可以被索引，Mapping 结构会被更新。同样先将 dynamic 设置为 false，然后向里面添加数据，其他步骤和上面 true 操作一样。定义 Mapping，添加数据。搜索 gender 字段： 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-48.png)   
&emsp; 此时新增字段数据无法被索引，但数据可以写入。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-49.png)  
&emsp; Mappnig 也不会添加新增的字段：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-50.png)  

* dynamic-strict  
&emsp; 设置 dynamic 为 strict时，从字面上意思也可以看出，对于动态映射是较严格的，新增字段数据不可以写入，不可以被索引，Mapping 结构不会被更新。只能按照定义好的 Mapping 结构添加数据。在添加新字段数据时，就马上会抛出异常：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-51.png)  

## 1.4. 多字段  
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
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-43.png)  
&emsp; 查询 address.keyword数据。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-44.png)  
&emsp; 通过 keyword检索时，由于不会建立分词索引，并没有获取到数据。  

## 1.5. 控制索引  
&emsp; 在字段中使用 index 指定当前字段索引是否能被搜索到。指定类型为 boolean 类型，false 为不可搜索到，true 为可以搜索到。先删除之前的 Mapping：  

```text
DELETE person
```
&emsp; 创建 Mapping，设置 name属性的 index 为 false。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-41.png)  
&emsp; 再次添加上面的数据后搜索 name字段：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-42.png)  
&emsp; 字段 index 设置 false 后，由于没有被索引，所以搜索无法获取到索引。  

## 1.6. 空值处理
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
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-36.png)  
&emsp; 搜索返回异常，默认是不被允许搜索NUll。这是需要在Mapping指定null_value属性，并且不能在text类型中声明。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-37.png)  
&emsp; 搜索address.keyword 为空的数据：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-38.png)  
&emsp; 设置 "null_value":"NULL" 后，空值可以处理搜索。  

## 1.7. 聚合多个字段  
&emsp; 聚合多个字段放到一个索引中，使用 copy_to 进行聚合。例如我们在多字段查询中，这是不需要对每个字段进行过滤筛选，只需对聚合字段即可。在使用 copy_to 时，是通过指定聚合的名称实现。  
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
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-39.png)  
&emsp; 查询 full_name 的值，会返回 name 和 address 相关的值的对象。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-40.png)  
&emsp; 从上面返回结果看到，_source 中的字段没有增加相应的 copy_to 字段名，所以 copy_to 只会拷贝字段内容至索引，并不会改变包含的字段。  

