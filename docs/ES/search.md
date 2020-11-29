<!-- TOC -->

- [1. 检索操作](#1-检索操作)
    - [1.1. 检索操作](#11-检索操作)
        - [1.1.1. query string search](#111-query-string-search)
        - [1.1.2. DSL](#112-dsl)
            - [1.1.2.1. SQL代替DSL](#1121-sql代替dsl)
        - [1.1.3. query filter](#113-query-filter)
        - [1.1.4. full-text search](#114-full-text-search)
        - [1.1.5. phrase search](#115-phrase-search)
        - [1.1.6. highlight search](#116-highlight-search)
    - [1.2. 检索模板](#12-检索模板)

<!-- /TOC -->

# 1. 检索操作  

## 1.1. 检索操作  
<!-- 
https://blog.csdn.net/xiaos76/article/details/105883617
https://blog.csdn.net/jiaojiao521765146514/article/details/82746192
-->
&emsp; elasticsearch有六种搜索方式：  
1. query string search。  
2. query DSL，DSL(Domain Specific Language特定领域语言)以JSON请求体的形式出现。  
3. query filter，对数据进行过滤。  
4. full-text search，全文检索。  
5. phrase search，短语搜索。  
6. highlight search，高亮搜索。  

### 1.1.1. query string search

### 1.1.2. DSL


#### 1.1.2.1. SQL代替DSL  
<!-- 
用SQL代替DSL查询ElasticSearch怎样？ 
https://mp.weixin.qq.com/s/CJkS3vu2BjUWfWrciwNVJg
如何用你最熟悉的 SQL 来查询 Elasticsearch 中的数据？ 
https://mp.weixin.qq.com/s/QQh0M85YqI-sHPnYy3pkBg
-->

### 1.1.3. query filter

### 1.1.4. full-text search

### 1.1.5. phrase search

### 1.1.6. highlight search
<!-- 
搜索模板、映射模板、高亮搜索和地理位置的简单玩法
https://mp.weixin.qq.com/s/BY0f47p6YETCVpQQDzG-dA
-->

## 1.2. 检索模板  
<!-- 
https://www.cnblogs.com/Henry-pan/p/7242600.html
索引模版、检索模板问题 
https://mp.weixin.qq.com/s/gARgAKgqmUzfeHujK0n71g
https://blog.csdn.net/miaomiao19971215/article/details/106322234
-->
&emsp; 什么是搜索模板？  
&emsp; 检索模板（search template），在实战业务场景中：每次业务请求都要构造 DSL，比如：这次查title、下次查content，除此之外的 DSL 部分 都一样，但两次请求：后端代码那里就要有相应的修改和适配。有没有不修改、拼接DSL使用检索的方案？这就引出了搜索模板。  
&emsp; 搜索模板与关系数据库中的存储过程非常相似。可以将常用查询定义为模板，并且使用 Elasticsearch 的应用程序可以简单地通过其 ID 引用查询。  
&emsp; 模板接受在运行时指定参数。搜索模板存储在服务器端，可以在不更改客户端代码的情况下进行修改。  
&emsp; 模板使用Mustache模板引擎表示。关于 Mustache 可以访问：http://mustache.github.io/mustache.5.html。





