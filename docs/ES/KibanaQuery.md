
<!-- TOC -->

- [1. Kibana查询](#1-kibana查询)
    - [1.1. 单词查询](#11-单词查询)
    - [1.2. 通配符查询](#12-通配符查询)
    - [1.3. 模糊查询](#13-模糊查询)
    - [1.4. 近似查询](#14-近似查询)
    - [1.5. 范围查询](#15-范围查询)
    - [1.6. 优先级查询](#16-优先级查询)
    - [1.7. 逻辑操作](#17-逻辑操作)
    - [1.8. 括号分组](#18-括号分组)
    - [1.9. 转义特殊字符](#19-转义特殊字符)

<!-- /TOC -->

# 1. Kibana查询
<!-- 

https://www.cnblogs.com/chenqionghe/p/12501218.html
-->

&emsp; Lucene是目前最为流行的开源全文搜索引擎工具包，提供了完整的查询引擎和索引引擎，部分文本分析引擎。平时使用kibana、阿里云的日志查询或者其他一些lucene二次开发的产品，几乎都支持lucene语法。下面给大家演示各种查询方式，更多请参考 [Apache Lucene - Query Parser Syntax](https://lucene.apache.org/core/2_9_4/queryparsersyntax.html)  

## 1.1. 单词查询
&emsp; 直接使用单词，例如chenqionghe  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-89.png)  

&emsp; 多个单词，可以用逗号或者空格隔开，例如chenqionghe,活动  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-90.png)  

&emsp; 可以指定字段:空格来查询，例如page: 18、content:"sport"  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-91.png)  


## 1.2. 通配符查询
* ?匹配单个字符
* *匹配0或多个字符

&emsp; 例如muscle?能匹配到muscles  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-92.png)  

&emsp; 搜索hi*er
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-93.png)  

&emsp; 搜索*er
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-94.png)  


## 1.3. 模糊查询
&emsp; ~：在一个单词后面加上~启用模糊搜索，可以搜到一些拼写错误的单词  
&emsp; 例如first~能匹配到错误的单词frist  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-95.png)  

&emsp; 可以在~后面添加模糊系数，例如first~0.8，模糊系数\[0-1]，越靠近1表示越相近,默认模糊系数为0.5。  

## 1.4. 近似查询
&emsp; 在短语后面加上~，可以搜到被隔开或顺序不同的单词  
&emsp; "life movement"~2表示life和movement之间可以隔开2两个词  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-96.png)  

## 1.5. 范围查询

* page: [2 TO 8]
* page: {2 TO 8}

&emsp; []表示端点数值包含在范围内，{}表示端点不包含在范围内  
&emsp; 搜索第2到第8页，包含两端点page: [2 TO 8]  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-97.png)  

&emsp; 搜索第2到第8页，不包含两端点page: {2 TO 8}  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-98.png)  

&emsp; 搜索第2到第8页，包含起始不包含末端page: [2 TO 8}  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-99.png)  

## 1.6. 优先级查询
&emsp; 如果单词的匹配度很高，一个文档中或者一个字段中可以匹配多次，那么可以提升该词的相关度。使用符号^提高相关度。
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-100.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-101.png)  

&emsp; 默认为1，可以为0~1之间的浮点数，来降低优先级

## 1.7. 逻辑操作

* AND：逻辑与，也可以用&&代替
* OR：逻辑或，也可以使用||代替
* NOT：逻辑非，也可以使用!代替
* +：必须包含
* -：不能包含

&emsp; 如muscle AND easy，muscle和easy必须同时存在  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-102.png)  

&emsp; muscle NOT easy，muscle存在easy不存在  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-103.png)  

&emsp; muscle OR easy，muscle或easy存在  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-104.png)  

&emsp; 例如+life -lies：必须包含life，不包含lies  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-105.png)  

## 1.8. 括号分组
&emsp; 可以使用小括号对子句进行分组，构造更复杂的查询逻辑  
&emsp; chenqionghe OR (生命 AND 运动)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-106.png)  

&emsp; 同时，也可以在字段中使用小括号分组，例如content:(+chenqionghe +"muscle")  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-107.png)  


## 1.9. 转义特殊字符
&emsp; + - && || ! ( ) { } [ ] ^ " ~ * ? : \
&emsp; 这些字符需要转义，例如\(1\+1\)\:2用来查询(1+1):2。  
