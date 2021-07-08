<!-- TOC -->

- [1. 检索操作](#1-检索操作)
    - [1.1. 搜索两种基本方式](#11-搜索两种基本方式)
    - [1.2. 回顾：sql查询](#12-回顾sql查询)
    - [1.3. 检索分类](#13-检索分类)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 结构化检索
	1. 精确值检索
		1. 单个精确值检索 term query
		2. 布尔过滤器 bool filter
		3. 多个精确值检索 terms query
	2. 范围检索 range query
	3. 存在与否检索 exist query
	4. 前缀检索 prefix query
	5. 通配符检索 wildcard query
		1. ? 代表单个字符
		2. * 代表0到多个字符
	6. 正则表达式检索 regexp query
	7. 模糊检索 fuzzy query
	8. 类型检索 type query 检索索引下对应type的数据
	9. ID检索 ids query 检索指定id的数据
2. 全文检索
	1. 匹配检索 match query
	2. 匹配解析检索 match_phrase query
	3. 匹配解析前缀检索 match_phrase_prefix query
	4. 多字段匹配检索 multi_match query 支持字段模糊匹配
	5. 字符串检测 query_string
	6. 简化字符串检索 simple_query_string
3. 复合检索
4. Joining检索
5. GEO检索


# 1. 检索操作  
<!-- 

重要  fingerprint filter 插件——Elasticsearch 去重必备利器 
https://www.baidu.com/index.php?tn=monline_3_dg
-->

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
&emsp; **<font color = "red">部分参考《Elasticsearch: 权威指南》</font>**  
  
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
1. 基本查询，SELECT语句有哪几部分构成？作用分别是什么？  
    1. SELECT关键字；  
    2. 谓词：DISTINCT，TOP n；  
    3. 查询字段：*或用逗号分隔的字段列表；  
    4. FROM子句：用逗号分隔的表或视图列表；  
    5. WHERE子句：查询条件；  
    6. GROUP BY子句：分组字段；  
    7. HAVING子句：针对分组字段的查询条件；  
    8. ORDER BY子句：排序字段列表；  
    9. limit子句：分页。  
2. 复杂查询，有关联查询。  

## 1.3. 检索分类  
&emsp; &emsp; [结构化检索](/docs/ES/Structured.md)  
&emsp; &emsp; [全文检索](/docs/ES/fullText.md)  
&emsp; &emsp; [相关度/排序/评分机制](/docs/ES/score.md)  
&emsp; &emsp; [多字段搜索](/docs/ES/MultiField.md)  
&emsp; &emsp; [聚合查询](/docs/ES/togetherSearch.md)  
&emsp; &emsp; [分页查询](/docs/ES/limitSearch.md)  

-----

&emsp; &emsp; [多表关联](/docs/ES/multiTable.md)  

-----

&emsp; &emsp; [高亮显示](/docs/ES/highLight.md)  
&emsp; &emsp; [评分机制](/docs/ES/score.md)  

-----

&emsp; &emsp; [检索模版](/docs/ES/searchTemplate.md)  

-----

![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-83.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-87.png)  


