
<!-- TOC -->

- [1. Kibana用户手册](#1-kibana用户手册)
    - [1.1. Kibana权限验证](#11-kibana权限验证)
    - [1.2. kibana查询与过滤](#12-kibana查询与过滤)
        - [1.2.1. 单词查询](#121-单词查询)
        - [1.2.2. 通配符查询](#122-通配符查询)
        - [1.2.3. 模糊查询](#123-模糊查询)
        - [1.2.4. 近似查询](#124-近似查询)
        - [1.2.5. 范围查询](#125-范围查询)
        - [1.2.6. 优先级查询](#126-优先级查询)
        - [1.2.7. 逻辑操作](#127-逻辑操作)
        - [1.2.8. 括号分组](#128-括号分组)
        - [1.2.9. 转义特殊字符](#129-转义特殊字符)
    - [1.3. 日志检索](#13-日志检索)

<!-- /TOC -->

# 1. Kibana用户手册  
<!-- 
快用 Kibana 吧
https://mp.weixin.qq.com/s/Ky51TVhvDP0Mv1FlhNGycg
kibana设置中文
https://www.cnblogs.com/gaohanghang/p/12099614.html
在 Kibana 中显示图片及 Binary 字段类型介绍 
https://mp.weixin.qq.com/s/c1myzUz7fJhcG_ZjM9wguw


如何使用 Kibana 可视化地理位置数据 
https://mp.weixin.qq.com/s/96TBRzABHol27KrMncUkTQ

-->

&emsp; **<font color = "clime">Kibana文档：https://www.elastic.co/guide/cn/kibana/current/index.html</font>**  
## 1.1. Kibana权限验证  

<!-- 
Kibana验证  
https://www.lmlphp.com/user/18641/article/item/475053/

-->


## 1.2. kibana查询与过滤  
<!-- 
kibana查询与过滤
https://blog.csdn.net/weixin_39754616/article/details/110803809
-->
&emsp; [Kibana查询](/docs/ES/KibanaQuery.md)  

<!-- 

https://www.cnblogs.com/chenqionghe/p/12501218.html
-->

&emsp; Lucene是目前最为流行的开源全文搜索引擎工具包，提供了完整的查询引擎和索引引擎，部分文本分析引擎。平时使用kibana、阿里云的日志查询或者其他一些lucene二次开发的产品，几乎都支持lucene语法。下面给大家演示各种查询方式，更多请参考 [Apache Lucene - Query Parser Syntax](https://lucene.apache.org/core/2_9_4/queryparsersyntax.html)  

### 1.2.1. 单词查询
&emsp; 直接使用单词，例如chenqionghe  
![image](http://www.wt1814.com/static/view/images/ES/es-89.png)  

&emsp; 多个单词，可以用逗号或者空格隔开，例如chenqionghe,活动  
![image](http://www.wt1814.com/static/view/images/ES/es-90.png)  

&emsp; 可以指定字段:空格来查询，例如page: 18、content:"sport"  
![image](http://www.wt1814.com/static/view/images/ES/es-91.png)  


### 1.2.2. 通配符查询
* ?匹配单个字符
* *匹配0或多个字符

&emsp; 例如muscle?能匹配到muscles  
![image](http://www.wt1814.com/static/view/images/ES/es-92.png)  

&emsp; 搜索hi*er
![image](http://www.wt1814.com/static/view/images/ES/es-93.png)  

&emsp; 搜索*er
![image](http://www.wt1814.com/static/view/images/ES/es-94.png)  


### 1.2.3. 模糊查询
&emsp; ~：在一个单词后面加上~启用模糊搜索，可以搜到一些拼写错误的单词  
&emsp; 例如first~能匹配到错误的单词frist  
![image](http://www.wt1814.com/static/view/images/ES/es-95.png)  

&emsp; 可以在~后面添加模糊系数，例如first~0.8，模糊系数\[0-1]，越靠近1表示越相近,默认模糊系数为0.5。  

### 1.2.4. 近似查询
&emsp; 在短语后面加上~，可以搜到被隔开或顺序不同的单词  
&emsp; "life movement"~2表示life和movement之间可以隔开2两个词  
![image](http://www.wt1814.com/static/view/images/ES/es-96.png)  

### 1.2.5. 范围查询

* page: [2 TO 8]
* page: {2 TO 8}

&emsp; []表示端点数值包含在范围内，{}表示端点不包含在范围内  
&emsp; 搜索第2到第8页，包含两端点page: [2 TO 8]  
![image](http://www.wt1814.com/static/view/images/ES/es-97.png)  

&emsp; 搜索第2到第8页，不包含两端点page: {2 TO 8}  
![image](http://www.wt1814.com/static/view/images/ES/es-98.png)  

&emsp; 搜索第2到第8页，包含起始不包含末端page: [2 TO 8}  
![image](http://www.wt1814.com/static/view/images/ES/es-99.png)  

### 1.2.6. 优先级查询
&emsp; 如果单词的匹配度很高，一个文档中或者一个字段中可以匹配多次，那么可以提升该词的相关度。使用符号^提高相关度。
![image](http://www.wt1814.com/static/view/images/ES/es-100.png)  
![image](http://www.wt1814.com/static/view/images/ES/es-101.png)  

&emsp; 默认为1，可以为0~1之间的浮点数，来降低优先级

### 1.2.7. 逻辑操作

* AND：逻辑与，也可以用&&代替
* OR：逻辑或，也可以使用||代替
* NOT：逻辑非，也可以使用!代替
* +：必须包含
* -：不能包含

&emsp; 如muscle AND easy，muscle和easy必须同时存在  
![image](http://www.wt1814.com/static/view/images/ES/es-102.png)  

&emsp; muscle NOT easy，muscle存在easy不存在  
![image](http://www.wt1814.com/static/view/images/ES/es-103.png)  

&emsp; muscle OR easy，muscle或easy存在  
![image](http://www.wt1814.com/static/view/images/ES/es-104.png)  

&emsp; 例如+life -lies：必须包含life，不包含lies  
![image](http://www.wt1814.com/static/view/images/ES/es-105.png)  

### 1.2.8. 括号分组
&emsp; 可以使用小括号对子句进行分组，构造更复杂的查询逻辑  
&emsp; chenqionghe OR (生命 AND 运动)  
![image](http://www.wt1814.com/static/view/images/ES/es-106.png)  

&emsp; 同时，也可以在字段中使用小括号分组，例如content:(+chenqionghe +"muscle")  
![image](http://www.wt1814.com/static/view/images/ES/es-107.png)  


### 1.2.9. 转义特殊字符
&emsp; + - && || ! ( ) { } [ ] ^ " ~ * ? : \
&emsp; 这些字符需要转义，例如\(1\+1\)\:2用来查询(1+1):2。  


## 1.3. 日志检索  
* 登录后界面  
&emsp; 如图所示，检索日志需点击红框内的菜单  
![image](http://www.wt1814.com/static/view/images/ES/es-52.png)  
* 日志搜索检索  
&emsp; 如图所示：   
![image](http://www.wt1814.com/static/view/images/ES/es-53.png)  

* 日志索引选定  
&emsp; 点击下拉选项，可弹出全部索引列表  
![image](http://www.wt1814.com/static/view/images/ES/es-54.png)  

* 查询时间范围选定   
&emsp; 点击箭头处可打开日期选取界面，下方红框为设置自动刷新获取日志的时间间隔  
![image](http://www.wt1814.com/static/view/images/ES/es-55.png)  

* 日志内容搜索  
&emsp; 在箭头指向处可以搜索指定文本，搜索后在日志文本区匹配文本会高亮显示  
![image](http://www.wt1814.com/static/view/images/ES/es-56.png)  
![image](http://www.wt1814.com/static/view/images/ES/es-57.png)  
&emsp; 如果搜索字段为已切割字段，则可以键入切割字段名称，搜索框下方会弹出提示框，类似于Key:Value的使用：  
![image](http://www.wt1814.com/static/view/images/ES/es-58.png)  

* 日志已切割字段的使用  
&emsp; 鼠标移动到字段后方时，会出现添加按钮，点击后，右侧日志文本区会发生变化  
![image](http://www.wt1814.com/static/view/images/ES/es-59.png)  
&emsp; 日志会以选定字段展示，如果想恢复原状则可以在选定字段区域点击移除按钮  
![image](http://www.wt1814.com/static/view/images/ES/es-60.png)  
&emsp; 也可以将鼠标移动到字段名称上方，会出现是否继续选取该字段以及调整字段顺序的选项  
![image](http://www.wt1814.com/static/view/images/ES/es-61.png)  
* 日志正文查看  
&emsp; 默认切割字段内容和日志原文一同显示在页面中，如果需要查看详情可点击红框内箭头  
![image](http://www.wt1814.com/static/view/images/ES/es-62.png)  
![image](http://www.wt1814.com/static/view/images/ES/es-63.png)  
* 仪表板的使用  
&emsp; 选取菜单栏中仪表板  
![image](http://www.wt1814.com/static/view/images/ES/es-64.png)  
&emsp; 会出现已配置好的仪表板，点击即可进入该仪表板界面，使用方式与日志检索界面相同  
![image](http://www.wt1814.com/static/view/images/ES/es-65.png)  
