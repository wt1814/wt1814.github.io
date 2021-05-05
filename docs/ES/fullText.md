

<!-- TOC -->

- [1. 全文检索](#1-全文检索)
    - [1.1. 全文检索](#11-全文检索)
    - [1.2. ★★★分词结果分析](#12-★★★分词结果分析)
    - [1.3. 匹配检索（match query）](#13-匹配检索match-query)
    - [1.4. 匹配解析检索 match_phrase query](#14-匹配解析检索-match_phrase-query)
    - [1.5. 匹配解析前缀检索（match_phrase_prefix）](#15-匹配解析前缀检索match_phrase_prefix)
    - [1.6. 多字段匹配检索（ multi_match query）](#16-多字段匹配检索-multi_match-query)
    - [1.7. 字符串检索(query_string）](#17-字符串检索query_string)
    - [1.8. 简化字符串检索（simple_query_string）](#18-简化字符串检索simple_query_string)

<!-- /TOC -->

# 1. 全文检索
&emsp; 高级全文查询通常用于在全文本字段（如电子邮件正文）上运行全文查询。它们了解如何对被查询的字段进行分析，并在执行前将每个字段的分析器（或search_analyzer）应用于查询字符串。 

## 1.1. 全文检索
&emsp; **<font color = "clime">全文搜索两个最重要的方面是：</font>**  
* 相关性（Relevance）  
&emsp; 它是评价查询与其结果间的相关程度，并根据这种相关程度对结果排名的一种能力，这种计算方式可以是TF/IDF方法（参见 相关性的介绍）、地理位置邻近、模糊相似，或其他的某些算法。  
* 分析（Analysis）  
&emsp; 它是将文本块转换为有区别的、规范化的 token 的一个过程，目的是为了创建倒排索引以及查询倒排索引。  

&emsp; 一旦谈论相关性或分析这两个方面的问题时，所处的语境是关于查询的而不是过滤。  


## 1.2. ★★★分词结果分析  
<!-- 
https://bigmaning.blog.csdn.net/article/details/110204549
-->


## 1.3. 匹配检索（match query）
&emsp; 匹配查询接受文本/数字/日期类型，分析它们，并构造查询。  
&emsp; 1）匹配查询的类型为boolean。 这意味着分析所提供的文本，并且分析过程从提供的文本构造一个布尔查询，可以将运算符标志设置为或以控制布尔子句（默认为或）；  
&emsp; 2）文本分析取决于mapping中设定的analyzer（中文分词，默认选择ik分词器）；  
&emsp; 3） fuzziness——模糊性允许基于被查询的字段的类型进行模糊匹配；  
&emsp; 4）”operator”: “and”——匹配与操作（默认或操作）；  
&emsp; 5） “minimum_should_match”: “75%”——可以指定必须匹配的词项数用来表示一个文档是否相关。  
&emsp; 举例：  

```
GET /_search
{
    "query": {
        "match" : {
            "message" : {
                "query" : "this is a test",
                "operator" : "and"
            }
        }
    }
}
```

## 1.4. 匹配解析检索 match_phrase query
&emsp; match_phrase查询分析文本，并从分析文本中创建短语查询。  
&emsp; 类似 match 查询， match_phrase 查询首先将查询字符串解析成一个词项列表，然后对这些词项进行搜索，但只保留那些包含 全部 搜索词项，且 位置 与搜索词项相同的文档。  
&emsp; 举例如下：对于 quick fox 的短语搜索可能不会匹配到任何文档，因为没有文档包含的 quick 词之后紧跟着 fox 。

```
GET /my_index/my_type/_search
{
  "query": {
  "match_phrase": {
  "title": "quick brown fox"
  }
  }
}
```

## 1.5. 匹配解析前缀检索（match_phrase_prefix）
&emsp; **<font color = "clime">用户已经渐渐习惯在输完查询内容之前，就展现搜索结果，这就是所谓的 即时搜索（instant search） 或 输入即搜索（search-as-you-type）。</font>**  
&emsp; 不仅用户能在更短的时间内得到搜索结果，系统也能引导用户搜索索引中真实存在的结果。  
&emsp; 例如，如果用户输入 johnnie walker bl ，希望在它们完成输入搜索条件前就能得到：  

```text
Johnnie Walker Black Label 和 Johnnie Walker Blue Label 。
```

&emsp; match_phrase_prefix与match_phrase相同，除了它允许文本中最后一个术语的前缀匹配。  
&emsp; 举例：

```
GET / _search
{
    “query”：{
        “match_phrase_prefix”：{
            “message”：“quick brown f”
        }
    }
}
```

## 1.6. 多字段匹配检索（ multi_match query）
&emsp; multi_match 查询为能在多个字段上反复执行相同查询提供了一种便捷方式。  
&emsp; 默认情况下，查询的类型是 best_fields， 这表示它会为每个字段生成一个 match 查询。  
&emsp; 举例1：`fields”: “*_title`  
&emsp; ——任何与模糊模式正则匹配的字段都会被包括在搜索条件中， 例如可以左侧的方式同时匹配 book_title 、 chapter_title 和 section_title （书名、章名、节名）这三个字段。  
&emsp; 举例2： “fields”: [ “*_title”, “chapter_title^2” ]  
&emsp; ——可以使用 ^ 字符语法为单个字段提升权重，在字段名称的末尾添加 ^boost ， 其中 boost 是一个浮点数。  
&emsp; 举例3：”fields”: [ “first_name”, “last_name” ],“operator”: “and”   
&emsp; ——两个字段必须都包含。

```
GET /_search
{
  "query": {
  "multi_match" : {
  "query": "this is a test",
  "fields": [ "subject", "message" ]
  }
  }
}
```


## 1.7. 字符串检索(query_string）
&emsp; 一个使用查询解析器解析其内容的查询。  
&emsp; query_string查询提供了以简明的简写语法执行多匹配查询 multi_match queries ，布尔查询 bool queries ，提升得分 boosting ，模糊匹配 fuzzy matching ，通配符 wildcards ，正则表达式 regexp 和范围查询 range queries 的方式。  
&emsp; 支持参数达10几种。

```
GET /_search
{
  "query": {
  "query_string" : {
  "default_field" : "content",
  "query" : "this AND that OR thus"
  }
  }
}
```

## 1.8. 简化字符串检索（simple_query_string）
&emsp; 一个使用SimpleQueryParser解析其上下文的查询。 与常规query_string查询不同，simple_query_string查询永远不会抛出异常，并丢弃查询的无效部分。  
&emsp; 举例：  

```
GET /_search
{
    "query": {
        "simple_query_string" : {
            "fields" : ["content"],
            "query" : "foo bar -baz"
        }
    }
}
```

&emsp; 支持的操作如下：  
&emsp; 1）+表示AND操作  
&emsp; 2）| 表示OR操作  
&emsp; 3）- 否定操作  
&emsp; 4）*在术语结束时表示前缀查询  
&emsp; 5）（和）表示优先  
