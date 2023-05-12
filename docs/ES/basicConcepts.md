
<!-- TOC -->

- [1. ES基本概念](#1-es基本概念)
    - [1.1. 什么是ES？](#11-什么是es)
    - [1.2. ~~ES数据架构~~](#12-es数据架构)
        - [1.2.1. 索引(Index)](#121-索引index)
        - [1.2.2. ~~类型(Type)~~](#122-类型type)
        - [1.2.3. 映射(mapping)](#123-映射mapping)
        - [1.2.4. 文档(document)](#124-文档document)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. ES是一个`分布式`的`搜索`框架。  
1. **<font color = "blue">ES数据结构</font>**  
&emsp; 索引Index（数据库） ---> 映射Mapping（表结构） ---> 文档Fields（一行数据）  
&emsp; **<font color = "clime">★★★注：文档的元数据（有哪些字段）是由映射Mapping决定的。</font>**    
&emsp; **~~ES中索引的元数据包含Mapping信息和Setting信息。Mapping定义文档字段的类型；Setting定义不同的数据分布。~~**  
&emsp; **~~每个索引都有一个映射类型，它决定了文档的索引方式。~~**  
2. 倒排索引的结构  
&emsp; 那么倒排序索引的结构是怎样的呢？简单来讲就是<font color="clime">“以内容的关键词”建立索引，映射关系为“内容的关键词->文档ID”。`这样只需要在“关键词”中进行检索，效率肯定更快。`</font>  
&emsp; **<font color = "red">倒排序索引包含两个部分：单词词典和倒排列表。</font>** 单词词典：记录所有文档单词；记录单词到倒排列表的关联关系。倒排列表：记录单词与对应文档结合，由倒排索引项组成。  
3. 分析（建索引与检索）  
&emsp; 文本分析由分析器来执行，而分析器由分词器、过滤器和字符映射器组成。  
&emsp; **<font color = "clime">有一点需要牢记，Elasticsearch有些查询会被分析，而有些则不会。例如，前缀查询不会被分析，而匹配查询会被分析。</font>**     

# 1. ES基本概念  
<!--
Elasticsearch 为什么能做到快速检索？— 倒排索引的秘密 
https://mp.weixin.qq.com/s/vwTrRSfgJ-7bWQUJWhfaHQ

核心概念介绍
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247490655&idx=3&sn=c988118763c13d263dfabe745e43cebe&scene=21#wechat_redirect
一文讲透Elasticsearch倒排索引与分词 
https://mp.weixin.qq.com/s/81wHAF9b96r08uBCH3TodA
ES倒排索引结构设计太牛逼
https://mp.weixin.qq.com/s/4ssGyiK_J2NXwZe4JqnKHQ

&emsp; 特点和优势：  

* 分布式实时文件存储，可将每一个字段存入索引，使其可以被检索到。  
* 近乎实时分析的分布式搜索引擎。   
* 分布式：索引分拆成多个分片，每个分片可有零个或多个副本。集群中的每个数据节点都可承载一个或多个分片，并且协调和处理各种操作。  
* 负载再平衡和路由在大多数情况下自动完成。   
* 可以扩展到上百台服务器，处理PB级别的结构化或非结构化数据(官网是这么说的)。也可以运行在单台PC上(已测试)。   
* 支持插件机制，分词插件、同步插件、Hadoop插件、可视化插件等。
--> 
ElasticSearch java API：https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/index.html  
文档：https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html
官网：https://www.elastic.co/cn/
中文社区：https://elasticsearch.cn/

## 1.1. 什么是ES？  
&emsp; Elasticsearch（ES）是一个基于Lucene构建的开源、分布式、RESTful接口的全文搜索引擎。  
&emsp; Elasticsearch还是一个分布式文档数据库，其中每个字段均可被索引，而且每个字段的数据均可被搜索，ES能够横向扩展至数以百计的服务器存储以及处理PB级的数据。可以在极短的时间内存储、搜索和分析大量的数据。通常作为具有复杂搜索场景情况下的核心发动机。  

&emsp; 什么是全文检索？  

## 1.2. ~~ES数据架构~~  
&emsp; **与mysql的对比**  
![image](http://182.92.69.8:8081/img/ES/es-2.png)  
![image](http://182.92.69.8:8081/img/ES/es-82.png)  

|关系型数据库|ES|
|---|---|
|数据库|索引|
|表|类型|
|行|文档|
|列|字段|
|表结构|映射(Mapping)|



<!-- 
11.索引(index)  
索引是具有相同结构的文档集合。例如，可以有一个客户信息的索引，包括一个产品目录的索引，一个订单数据的索引。在系统上索引的名字全部小写，通过这个名字可以用来执 行索引、搜索、更新和删除操作等。在单个集群中，可以定义多个你想要的索引。索引结构 参见图1-2。  
12.类型(type)  
在索引中，可以定义一个或多个类型，类型是索引的逻辑分区。在一般情况下，一种类 型被定义为具有一组公共字段的文档。例如，让我们假设你运行一个博客平台，并把所有的 数据存储在一个索引中。在这个索引中，你可以定义一种类型为用户数据，一种类型为博客 数据，另一种类型为评论数据。  
13.文档(document)  
文档是存储在Elasticsearch中的一个JSON格式的字符串。它就像在关系数据库中表的一行。每个存储在索引中的一个文档都有一个类型和一个ID,每个文档都是一个JSON对象，存储了零个或者多个字段，或者键值对。原始的JSON文档被存储在一个叫作_source 的字段中。当搜索文档的时候默认返回的就是这个字段。   
14.映射(mapping)  
映射像关系数据库中的表结构，每一个索引都有一个映射，它定义了索引中的每一个字段类型，以及一个索引范围内的设置。一个映射可以事先被定义，或者在第一次存储文档的 时候自动识别。  
15.字段(field)
文档中包含零个或者多个字段，字段可以是一个简单的值(例如字符串、整数、日期), 也可以是一个数组或对象的嵌套结构。字段类似于关系数据库中表的列。每个字段都对应一 个字段类型，例如整数、字符串、对象等。字段还可以指定如何分析该字段的值。  
16.来源字段(source field )  
默认情况下，原文档将被存储在.source这个字段中，当査询的时候也是返回这个字段。这可以从搜索结果中访问原始的对象，这个对象返回一个精确的JSON字符串，这个对象不显示索引分析后的其他任何数据。  
17.主键(ID)  
ID是一个文件的唯一标识，如果在存库的时候没有提供ID,系统会自动生成一个ID, 文档的index/type/id必须是唯一的。  
-->

### 1.2.1. 索引(Index)  
&emsp; ES将数据存储于一个或多个索引中，索引是具有类似特性的文档的集合。  
&emsp; **<font color = "red">索引的元数据包含Mapping信息和Setting信息。Mapping定义文档字段的类型；Setting定义不同的数据分布。**</font>  

### 1.2.2. ~~类型(Type)~~  
&emsp; 类型是索引内部的逻辑分区(category/partition)，然而其意义完全取决于用户需求。因此，一个索引内部可定义一个或多个类型(type)。一般来说，类型就是为那些拥有相同的域的文档做的预定义。  
&emsp; 注：在Elasticsearch 6.0.0或更高版本中创建的索引只能包含一个映射类型。在5.x中创建的具有多种映射类型的索引将继续像在Elasticsearch 6.x中一样工作。类型在Elasticsearch 7.0.0中的API中弃用，并在8.0.0中完全删除。  

### 1.2.3. 映射(mapping)  
<!-- 
&emsp; 注意：在ES中创建一个mapping映射类似于在数据库中定义表结构，即表里面有哪些字段、字段是什么类型、字段的默认值等；也类似于solr里面的模式schema的定义。 --> 
&emsp; 映射是定义文档及其包含的字段如何存储和索引的过程。例如，使用映射来定义：  

* 哪些字符串字段应该被视为全文字段。
* 哪些字段包含数字、日期或地理位置。
* 文档中所有字段的值是否应该被索引到catch-all _all字段中。
* 日期值的格式。
* 用于控制动态添加字段的映射的自定义规则。

&emsp; **<font color = "red">每个索引都有一个映射类型，它决定了文档的索引方式。</font>**  
&emsp;ES中的数据类型：    
![image](http://182.92.69.8:8081/img/ES/es-1.png)  

### 1.2.4. 文档(document)  
&emsp; 文档是可搜索数据的最小单位。  
&emsp; 文档都是JSON格式的。  

&emsp; **<font color = "clime">★★★注：文档的元数据（有哪些字段）是由映射Mapping决定的。</font>**    
<!-- 
元数据（_index、_type、_id、_score、_source）
https://blog.csdn.net/qq_42513284/article/details/90678516
-->

  
