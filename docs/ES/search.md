<!-- TOC -->

- [1. 检索操作](#1-检索操作)
    - [1.1. 搜索两种基本方式](#11-搜索两种基本方式)
    - [1.2. 回顾：sql查询](#12-回顾sql查询)
    - [1.3. 检索操作](#13-检索操作)

<!-- /TOC -->

# 1. 检索操作  

<!-- 
检索类型如何选型呢？
https://mp.weixin.qq.com/s/Fc5LhiLJIeCtstl9OFeqdQ

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

  
## 1.1. 搜索两种基本方式  
<!-- 
ES运行检索两种基本方式
https://www.bblog.vip/article_detail/1559295979215
ES实战九、全文检索-ElasticSearch-进阶-两种查询方式
https://tech.souyunku.com/?p=37521
https://haokan.baidu.com/v?pd=wisenatural&vid=12730932323983835698
-->
&emsp; elasticsearch的搜索方式：  
1. query string search。  
2. query DSL，DSL（Domain Specific Language特定领域语言），可以用json的格式来构建查询语法，比较方便，可以构建各种复杂的语法。   
 

## 1.2. 回顾：sql查询  
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

## 1.3. 检索操作  
&emsp; [基本查询](/docs/ES/basicSearch.md)  
&emsp; [聚合查询](/docs/ES/togetherSearch.md)  
&emsp; [分页查询](/docs/ES/limitSearch.md)  

---
&emsp; [复合查询](/docs/ES/compoundQuery.md)  
&emsp; [多表关联](/docs/ES/multiTable.md)  

---
&emsp; [高亮显示](/docs/ES/highLight.md)  
&emsp; [检索模版](/docs/ES/searchTemplate.md)  
