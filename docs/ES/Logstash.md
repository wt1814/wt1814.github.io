
<!-- TOC -->

- [1. FileBeat和Logstash](#1-filebeat和logstash)
    - [1.1. 多种不同中间件日志格式](#11-多种不同中间件日志格式)
    - [1.2. FileBeat](#12-filebeat)
        - [1.2.1. 模板](#121-模板)
        - [1.2.2. 输入](#122-输入)
        - [1.2.3. 输出](#123-输出)
    - [1.3. ***Logstash](#13-logstash)

<!-- /TOC -->

# 1. FileBeat和Logstash  
## 1.1. 多种不同中间件日志格式  


## 1.2. FileBeat
官方配置：https://www.elastic.co/guide/en/beats/filebeat/current/configuring-howto-filebeat.html  

### 1.2.1. 模板

### 1.2.2. 输入  
https://www.elastic.co/guide/en/beats/filebeat/current/configuration-filebeat-options.html  



### 1.2.3. 输出  
https://www.elastic.co/guide/en/beats/filebeat/current/configuring-output.html  
https://www.elastic.co/guide/en/beats/filebeat/current/logstash-output.html



## 1.3. ***Logstash  
http://doc.yonyoucloud.com/doc/logstash-best-practice-cn/output/elasticsearch.html  


logstash6.8.12动态生成elasticsearch的index的正确方法
https://baijiahao.baidu.com/s?id=1676803444228068371&wfr=spider&for=pc

通过filter插件，先把message转成json格式，这一步很重要，很多文章里都不提这个；  


