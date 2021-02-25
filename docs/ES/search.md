<!-- TOC -->

- [1. 检索操作](#1-检索操作)
    - [1.1. 检索操作](#11-检索操作)
        - [1.1.1. 搜索两种基本方式](#111-搜索两种基本方式)
        - [1.1.2. 回顾：sql查询](#112-回顾sql查询)
        - [1.1.3. 检索操作](#113-检索操作)

<!-- /TOC -->

# 1. 检索操作  
<!-- 
检索类型如何选型呢？
https://mp.weixin.qq.com/s/Fc5LhiLJIeCtstl9OFeqdQ
Elasticsearch之评分机制
https://www.jianshu.com/p/2624f61f1d02
-->
<!-- 
ElasticSearch 搜索入门 
https://mp.weixin.qq.com/s/WVInd3kCciTVa1nzOgeEAQ
fuzzy query
https://mp.weixin.qq.com/s/ReiCivwDINsE8S5kwUWb5w
-->
<!-- 
SQL代替DSL
用SQL代替DSL查询ElasticSearch怎样？ 
https://mp.weixin.qq.com/s/CJkS3vu2BjUWfWrciwNVJg
如何用你最熟悉的 SQL 来查询 Elasticsearch 中的数据？ 
https://mp.weixin.qq.com/s/QQh0M85YqI-sHPnYy3pkBg
-->

## 1.1. 检索操作  
&emsp; 搜索分为两个过程：  

* 当向索引中保存文档时，默认情况下，es 会保存两份内容，一份是 _source  中的数据，另一份则是通过分词、排序等一系列过程生成的倒排索引文件，倒排索引中保存了词项和文档之间的对应关系。  
* 搜索时，当 es 接收到用户的搜索请求之后，就会去倒排索引中查询，通过的倒排索引中维护的倒排记录表找到关键词对应的文档集合，然后对文档进行评分、排序、高亮等处理，处理完成后返回文档。  
  
### 1.1.1. 搜索两种基本方式  
<!-- 
ES运行检索两种基本方式
https://www.bblog.vip/article_detail/1559295979215
ES实战九、全文检索-ElasticSearch-进阶-两种查询方式
https://tech.souyunku.com/?p=37521

https://haokan.baidu.com/v?pd=wisenatural&vid=12730932323983835698
-->

&emsp; elasticsearch的搜索方式：  
1. query string search。  
2. query DSL，DSL(Domain Specific Language特定领域语言)以JSON请求体的形式出现。  

&emsp; DSL：Domain Specified Language，特定领域的语言。  
&emsp; http request body：请求体，可以用json的格式来构建查询语法，比较方便，可以构建各种复杂的语法。  

### 1.1.2. 回顾：sql查询  
&emsp; 基本查询，SELECT语句有哪几部分构成？作用分别是什么？  
1. SELECT关键字；  
2. 谓词：DISTINCT，TOP n；  
3. 查询字段：*或用逗号分隔的字段列表；  
4. FROM子句：用，分隔的表或视图列表；  
5. WHERE子句：查询条件；  
6. GROUP BY子句：分组字段；  
7. HAVING子句：针对分组字段的查询条件；  
8. ORDER BY子句：排序字段列表；  
9. limit子句：分页。  

&emsp; 复杂查询，有关联查询。  

### 1.1.3. 检索操作  
&emsp; [基本查询](/docs/ES/basicSearch.md)  
&emsp; [聚合查询](/docs/ES/togetherSearch.md)  
&emsp; [分页查询](/docs/ES/limitSearch.md)  
&emsp; [复合查询](/docs/ES/compoundQuery.md)  
&emsp; [多表关联](/docs/ES/multiTable.md)  
&emsp; [高亮显示](/docs/ES/highLight.md)  
&emsp; [检索模版](/docs/ES/searchTemplate.md)  