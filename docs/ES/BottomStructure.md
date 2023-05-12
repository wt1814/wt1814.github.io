
<!-- TOC -->

- [1. ES底层数据结构](#1-es底层数据结构)
    - [1.1. ES与lucene的关联](#11-es与lucene的关联)
    - [1.2. Lucene索引的总体架构](#12-lucene索引的总体架构)
    - [1.3. 倒排索引](#13-倒排索引)
        - [1.3.1. 示例](#131-示例)
        - [1.3.2. 核心组成](#132-核心组成)
        - [1.3.3. 倒排索引是怎么工作的？](#133-倒排索引是怎么工作的)
            - [1.3.3.1. 创建倒排索引](#1331-创建倒排索引)
            - [1.3.3.2. 倒排索引搜索](#1332-倒排索引搜索)
        - [1.3.4. 分析(建索引与检索)](#134-分析建索引与检索)
            - [1.3.4.1. ~~索引与查询~~](#1341-索引与查询)
            - [1.3.4.2. ik中文分词器](#1342-ik中文分词器)

<!-- /TOC -->

# 1. ES底层数据结构  
<!-- 

https://blog.csdn.net/weixin_42128977/article/details/127705236

https://blog.csdn.net/a934079371/article/details/108505474
https://zhuanlan.zhihu.com/p/344550528

视频  
https://www.bilibili.com/video/BV17S4y1U7xb?spm_id_from=333.337.search-card.all.click

-->

## 1.1. ES与lucene的关联
<!-- 
https://blog.csdn.net/yinni11/article/details/92798993
-->

## 1.2. Lucene索引的总体架构  
![image](http://182.92.69.8:8081/img/ES/BottomStructure/BottomStructure-1.jpg)  



## 1.3. 倒排索引  
&emsp; <font color = "red">在Elasticsearch中倒排索引也是非常重要的“索引”结构。</font>Elasticsearch将写入索引的所有信息组织成倒排索引的结构。该结构是一种<font color = "clime">将文档单词到文档ID的数据结构，其工作方式与传统的关系数据库不同，可以认为倒排索引是面向词项而不是面向文档的。</font>

### 1.3.1. 示例  
&emsp; 例如，针对专栏文章，平时在各大平台根据关键词检索时，使用到的技术就有“倒排序索引”。  

|ID	|作者	|文章题目	|内容|
|---|---|---|---|
|1	|wt	|系统学习es|	系统学习es，倒排序索引|
|2	|wt	|学习mysql	|学习mysql，正向索引|

&emsp; 假设文章的储存结果如上，对于关系型数据库mysql来说，普通的索引结构就是“id->题目->内容”，在搜索的时候，如果知道id或者题目，那么检索效率是很高效的，因为“id”、“题目”是很方便创建索引的。  

&emsp; **正向索引**  

|索引	|内容|
|---|---|
|1	|系统学习es，倒排序索引|
|系统学习es	|系统学习es，倒排序索引|
|2	|学习mysql，正向索引|
|学习mysql|学习mysql，正向索引|

&emsp; 但是当只有一个检索关键词，比如需求是搜索到与“倒排序索引”相关的文章时，在索引结构是“id->题目->内容”时，就只能对“题目”和“内容”进行全文扫描了，当数量级上去后，效率是没办法接受的！对于这类的搜索，关系型数据库的索引就很难应付了，适合使用全文搜索的倒排索引。  
&emsp; 那么倒排序索引的结构是怎样的呢？简单来讲就是<font color="clime">“以内容的关键词”建立索引，映射关系为“内容的关键词->文档ID”。这样只需要在“关键词”中进行检索，效率肯定更快。</font>  

&emsp; **倒排序索引**  

|Token	|文档id=1	|文档id=2|
|---|---|---|
|系统	|true|	|
|学习	|true	|true|
|es	|true|	|
|mysql	|	|true|
|倒排序	|true|	|
|正向	|	|true|
|索引|	true|	true|

### 1.3.2. 核心组成  
<!-- 

一般来说，倒排索引分为两个部分：

    单词词典(记录所有的文档词项，以及词项到倒排列表的关联关系)
    倒排列表(记录单词与对应的关系，由一系列倒排索引项组成，倒排索引项指：文档 id、词频(TF)(词项在文档中出现的次数，评分时使用)、位置(Position，词项在文档中分词的位置)、偏移(记录词项开始和结束的位置))

当我们去索引一个文档时，就回建立倒排索引，搜索时，直接根据倒排索引搜索。
-->

&emsp; **<font color = "red">倒排序索引包含两个部分：单词词典和倒排列表。</font>**  

* **<font color = "red">单词词典：记录所有文档单词；记录单词到倒排列表的关联关系。</font>**
* **<font color = "red">倒排列表：记录单词与对应文档结合，由倒排索引项组成。</font>**  
&emsp; 倒排索引项：  
  * 文档   
  * 词频 TF - 单词在文档中出现的次数，用于相关性评分。  
  * 位置(Position)- 单词在文档中分词的位置，用于phrase query。  
  * 偏移(Offset)- 记录单词开始结束的位置，实现高亮显示。  

&emsp; 举个简单例子，理解下“倒排索引项”：以 Token“学习”为例：  

  |单词	|doc Id 	|TF	|Position	|Offset|
  |---|---|---|---|---|
  |学习|	1|	1|	1|	<2,4>|
  |学习|	2|	1|	0|	<0,2>|

### 1.3.3. 倒排索引是怎么工作的？  
&emsp; **<font color = "red">倒排索引的工作流程主要包括2个过程：</font>**  
1. 创建倒排索引；  
2. 倒排索引搜索；  

#### 1.3.3.1. 创建倒排索引  
&emsp; 还是使用上面的例子。<font color = "red">先对文档的内容进行分词，形成一个个的token，也就是单词，然后保存这些token与文档的对应关系。</font>结果如下：  
&emsp; **倒排序索引**  

|Token|文档id=1	|文档id=2|
|---|---|---|
|系统	|true|	|
|学习|	true|	true|
|es	|true|	|
|mysql|		|true|
|倒排序	|true|	|
|正向|		|true|
|索引	|true	|true|

#### 1.3.3.2. 倒排索引搜索   

&emsp; 搜索示例1：“学习索引”  
&emsp; <font color = "red">先分词，得到两个Token：“学习”、“索引”；然后去倒排索引中进行匹配。</font>  
&emsp; 这2个Token在2个文档中都匹配，所以2个文档都会返回，而且分数相同。  

&emsp; 搜索示例2：“学习es”  
&emsp; 同样，2个文档都匹配，都会返回。但是文档1的相关性评分会高于文档2，因为文档1匹配了两个Token，而文档2只匹配了一个Token【学习】。  

### 1.3.4. 分析(建索引与检索)  
<!-- 
ElasticSearch 23 种映射参数详解 
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247491016&idx=1&sn=843b7e0a94f15d60efee27c4a8efbdc7&scene=21#wechat_redirect


1. 索引词(term)  
在Elasticsearch中索引词(term)是一个能够被索引的精确值。foo、Foo、F00几个单词是不同的索引词索引词(term)是可以通过term查询进行准确的搜索。  
2. 文本(text)  
文本是一段普通的非结构化文字。通常，文本会被分析成一个个的索引词，存储在Elasticsearch的索引库中。为了让文本能够进行搜索，文本字段需要事先进行分析；当对文 本中的关键词进行査询的时候，搜索引擎应该根据搜索条件搜索岀原文本。  
3. 分析(analysis)
分析是将文本转换为索引词的过程，分析的结果依赖于分词器。比如：FOOBAR、 Foo-Bar和foo bar这几个单词有可能会被分析成相同的索引词foo和bar，这些索引词存储 在Elasticsearch的索引库中。当用FoO.bAR进行全文搜索的时候，搜索引擎根据匹配计算也能在索引库中搜索出之前的内容。这就是Elasticsearch的搜索分析。  
-->


&emsp; <font color = "red">文档中的数据是如何转化为倒排索引的，而查询串又是如何转换为可用于搜索的文档单词的？这个转换过程称为分析(Analysis)。</font><font color = "clime">文本分析由分析器来执行，而分析器由分词器、过滤器和字符映射器组成。</font>  

#### 1.3.4.1. ~~索引与查询~~  
&emsp; Elasticsearch是如何控制索引和查询操作的。在索引期，Elasticsearch会使用选择的分析器来处理文档中的内容，并可以对不同的字段使用不同的分析器。  
&emsp; 在检索时，如果使用了某个查询分析器，那么查询串就会被分析。当然，也可以选择不进行查询分析。 **<font color = "clime">有一点需要牢记，Elasticsearch有些查询会被分析，而有些则不会。例如，前缀查询不会被分析，而匹配查询会被分析。</font>**  
&emsp; 索引期与检索期的文本分析要采用同样的分析器，只有查询分析出来的文档与索引中文档能匹配上，才会返回预期的文档集。  

-----

&emsp; Analysis：即文本分析，是把全文本转化为一系列单词(term/token)的过程，也叫分词；在Elasticsearch中可通过内置分词器实现分词，也可以按需定制分词器。Elasticsearch中的内置分词器：  

* Standard Analyzer：默认分词器，按词切分，小写处理  
* Simple Analyzer：按照非字母切分(符号被过滤)，小写处理  
* Stop Analyzer：小写处理，停用词过滤(the, a, is)  
* Whitespace Analyzer：按照空格切分，不转小写  
* Keyword Analyzer：不分词，直接将输入当做输出  
* Patter Analyzer：正则表达式，默认 \W+ (非字符分割)  
* Language：提供了30多种常见语言的分词器  
* Customer Analyzer：自定义分词器  

&emsp; **Analyzer由三部分组成：**  

* Character Filters：原始文本处理，如去除html。  
* Tokenizer：按照规则切分为单词。  
* Token Filters：对切分单词加工、小写、删除stopwords，增加同义词。  

&emsp; **Analyzer分词过程简介**  
1. 字符过滤器  character filter  
&emsp; 首先，字符串按顺序通过每个字符过滤器 。它们的任务是在分词前整理字符串。一个字符过滤器可以用来去掉HTML，或者将 & 转化成and。  
2. 分词器 tokenizer  
&emsp; 其次，字符串被分词器分为单个的词条。一个whitespace的分词器遇到空格和标点的时候，可能会将文本拆分成词条。  
![image](http://182.92.69.8:8081/img/ES/es-3.png)  
3. 令牌过滤器token filter  
&emsp; 最后，词条按顺序通过每个token过滤器。这个过程可能会改变词条，例如，lowercase token filter小写化(将ES转为es)、stop token filter删除词条(例如，像a，and，the等无用词)，或者synonym token filter增加词条(例如，像jump和leap这种同义词)。  

#### 1.3.4.2. ik中文分词器  
<!--
中文分词器
https://mp.weixin.qq.com/s/wpLeWIgq6yQEolKcv_P8hA
-->




