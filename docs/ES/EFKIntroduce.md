
<!-- TOC -->

- [1. EFK介绍](#1-efk介绍)
    - [1.1. ELK/EFK简介](#11-elkefk简介)
    - [Elastic Stack](#elastic-stack)
    - [1.2. EFK日志架构的演进](#12-efk日志架构的演进)
        - [1.2.1. Demo版](#121-demo版)
        - [1.2.2. 初级版](#122-初级版)
        - [1.2.3. 中级版](#123-中级版)
        - [1.2.4. 高级版](#124-高级版)
    - [1.3. EFK原理](#13-efk原理)
        - [1.3.1. Filebeat工作原理](#131-filebeat工作原理)
        - [1.3.2. Logstash工作原理](#132-logstash工作原理)

<!-- /TOC -->

# 1. EFK介绍  

## 1.1. ELK/EFK简介
<!-- 

详解日志采集工具--Logstash、Filebeat、Fluentd、Logagent对比
https://developer.51cto.com/art/201904/595529.htm

https://www.cnblogs.com/xianglei_/p/12047315.html
1. Elasticsearch 存储数据
是一个实时的分布式搜索和分析引擎，它可以用于全文搜索，结构化搜索以及分析。它是一个建立在全文搜索引擎 Apache Lucene 基础上的搜索引擎，使用 Java 语言编写，能对大容量的数据进行接近实时的存储、搜索和分析操作。

2. Logstash  收集数据
数据收集引擎。它支持动态的从各种数据源搜集数据，并对数据进行过滤、分析、丰富、统一格式等操作，然后存储到用户指定的位置。

3. Kibana  展示数据
数据分析和可视化平台。通常与 Elasticsearch 配合使用，对其中数据进行搜索、分析和以统计图表的方式展示。
-->
&emsp; EFK是目前最受欢迎的日志管理系统。 **<font color = "red">EFK是ELK日志分析系统的一个变种，加入了filebeat可以更好的收集到资源日志来为日志分析做好准备工作。</font>** EFK组件：  

* Filebeat：   
&emsp; **轻量级的开源日志文件数据搜集器。**通常在需要采集数据的客户端安装Filebeat，并指定目录与日志格式，Filebeat就能快速收集数据，并发送给logstash进行解析，或是直接发给Elasticsearch存储。  

        常见的日志采集工具有Logstash、Filebeat、Fluentd、Logagent、rsyslog等等。  
* Logstash：收集数据  
&emsp; **数据收集引擎。**它支持动态的的从各种数据源获取数据，并对数据进行过滤，分析，丰富，统一格式等操作，然后存储到用户指定的位置。 
* Elasticsearch：存储数据  
&emsp; 能对大容量的数据进行接近实时的存储，搜索和分析操作。 
* Kibana：展示数据  
&emsp; 数据分析与可视化平台，对Elasticsearch存储的数据进行可视化分析，通过表格的形式展现出来。  

&emsp; **<font color = "red">EFK的流程：</font>** `Filebeat->【LogstashShipper ---> 缓冲中间件Kafka ---> LogstashIndexer】->【Elasticsearch <-> Kibana】`。由程序产生出日志，由Filebeat进行处理，将日志数据输出到Logstash中，Logstash再将数据输出到Elasticsearch中，Elasticsearch再与Kibana相结合展示给用户。

## Elastic Stack
<!-- 

什么是Elastic Stack
https://blog.csdn.net/qq_41106844/article/details/106577851
-->


## 1.2. EFK日志架构的演进  
<!-- 
从ELK到EFK演进
https://www.cnblogs.com/panchanggui/p/10697548.html

ELK日志架构的演进
https://mp.weixin.qq.com/s/u5hqNTgZ7P235NQA0SIJiA
https://www.cnblogs.com/aresxin/p/8035137.html

-->
### 1.2.1. Demo版  
&emsp; 这版算是Demo版，各位开发可以在自己电脑上搭建练练手，如下图所示：   
![image](http://www.wt1814.com/static/view/images/ES/es-15.png)  
&emsp; 这种架构下把Logstash实例与Elasticsearch实例直接相连。程序App将日志写入Log，然后Logstash将Log读出，进行过滤，写入Elasticsearch。最后浏览器访问Kibana，提供一个可视化输出。  

&emsp; 该版的<font color = "red">缺点</font>主要是两个：  

* 在大并发情况下，日志传输峰值比较大。如果直接写入ES，ES的HTTP API处理能力有限，在日志写入频繁的情况下可能会超时、丢失，所以需要一个缓冲中间件。  
* 注意了，Logstash将Log读出、过滤、输出都是在应用服务器上进行的，这势必会造成服务器上占用系统资源较高，性能不佳，需要进行拆分。  

### 1.2.2. 初级版  
&emsp; 在这版中， **<font color = "red">加入一个缓冲中间件Kafka</font>** 。另外对Logstash拆分为Shipper和Indexer。先说一下，LogStash自身没有什么角色，只是根据不同的功能、不同的配置给出不同的称呼而已。Shipper来进行日志收集，Indexer从缓冲中间件接收日志，过滤输出到Elasticsearch。具体如下图所示：  
![image](http://www.wt1814.com/static/view/images/ES/es-16.png)  
&emsp; 说一下，这个缓冲中间件的选择。  
&emsp; 早期的博客，都是推荐使用redis。因为这是ELK Stack 官网建议使用 Redis 来做消息队列，但是很多大佬已经通过实践证明使用Kafka更加优秀。原因如下:  

* Redis无法保证消息的可靠性，这点Kafka可以做到
* Kafka的吞吐量和集群模式都比Redis更优秀  
* Redis受限于机器内存，当内存达到Max，数据就会抛弃。如果加大内存，在Redis中内存越大，触发持久化的操作阻塞主线程的时间越长。相比之下，Kafka的数据是堆积在硬盘中，不存在这个问题。  

&emsp; 因此，综上所述，这个缓存中间件，选择使用Kafka。  

&emsp; 主要`缺点`还是两个：  
* Logstash Shipper是jvm跑的，非常占用JAVA内存！据《ELK系统使用filebeat替代logstash进行日志采集》这篇文章说明，8线程8GB内存下，Logstash常驻内存660M（JAVA）。因此，这么一个巨无霸部署在应用服务器端就不大合适了，需要一个更加轻量级的日志采集组件。  
* 上述架构如果部署成集群，所有业务放在一个大集群中相互影响。一个业务系统出问题了，就会拖垮整个日志系统。因此，需要进行业务隔离！  

### 1.2.3. 中级版  
&emsp; 这版引入组件Filebeat。当年，Logstash的作者用golang写了一个功能较少但是资源消耗也小的轻量级的Logstash-forwarder。后来加入Elasticsearch后，以logstash-forwarder为基础，研发了一个新项目就叫Filebeat。  
&emsp; 相比于Logstash，Filebeat更轻量，占用资源更少，所占系统的 CPU 和内存几乎可以忽略不计。毕竟只是一个二进制文件。那么，这一版的架构图如下，直接画集群版。  
![image](http://www.wt1814.com/static/view/images/ES/es-17.png)  
&emsp; 至于这个Tribe Node，中文翻译为部落结点，它是一个特殊的客户端，它可以连接多个集群，在所有连接的集群上执行搜索和其他操作。在这里呢，负责将请求路由到正确的后端ES集群上。  
&emsp; `缺点：`  
&emsp; 这套架构的缺点在于对日志没有进行冷热分离。因为一般来说，对一个星期内的日志，查询的最多。以7天作为界限，区分冷热数据，可以大大的优化查询速度。  

### 1.2.4. 高级版  
&emsp; 这一版，对数据进行冷热分离。每个业务准备两个Elasticsearch集群，可以理解为冷热集群。7天以内的数据，存入热集群，以SSD存储索引。超过7天，就进入冷集群，以SATA存储索引。这么一改动，性能又得到提升，这一版架构图如下(为了方便画图，只画了两个业务Elasticsearch集群)  
![image](http://www.wt1814.com/static/view/images/ES/es-18.png)  
&emsp; **隐患:** 这个高级版，非要说有什么隐患，就是敏感数据没有进行处理，就直接写入日志了。关于这点，其实现在JAVA这边，现成的日志组件，比如log4j都有提供这种日志过滤功能，可以将敏感信息进行脱敏后，再记录日志。  


## 1.3. EFK原理  
&emsp; `官方文档：`  
&emsp; `Filebeat：`  

    https://www.elastic.co/cn/products/beats/filebeat
    https://www.elastic.co/guide/en/beats/filebeat/5.6/index.html

&emsp; `Logstash：`  

    https://www.elastic.co/cn/products/logstash
    https://www.elastic.co/guide/en/logstash/5.6/index.html

&emsp; `Kibana:`  

    https://www.elastic.co/cn/products/kibana
    https://www.elastic.co/guide/en/kibana/5.5/index.html

&emsp; `Elasticsearch：`  

    https://www.elastic.co/cn/products/elasticsearch
    https://www.elastic.co/guide/en/elasticsearch/reference/5.6/index.html
    elasticsearch中文社区：https://elasticsearch.cn/

### 1.3.1. Filebeat工作原理  
&emsp; Filebeat由两个主要组件组成：prospectors 和 harvesters。这两个组件协同工作将文件变动发送到指定的输出中。    
![image](http://www.wt1814.com/static/view/images/ES/es-19.png)  
&emsp; Harvester（收割机）：负责读取单个文件内容。每个文件会启动一个Harvester，每个Harvester会逐行读取各个文件，并将文件内容发送到制定输出中。Harvester负责打开和关闭文件，意味在Harvester运行的时候，文件描述符处于打开状态，如果文件在收集中被重命名或者被删除，Filebeat会继续读取此文件。所以在Harvester关闭之前，磁盘不会被释放。默认情况filebeat会保持文件打开的状态，直到达到close_inactive（如果此选项开启，filebeat会在指定时间内将不再更新的文件句柄关闭，时间从harvester读取最后一行的时间开始计时。若文件句柄被关闭后，文件发生变化，则会启动一个新的harvester。关闭文件句柄的时间不取决于文件的修改时间，若此参数配置不当，则可能发生日志不实时的情况，由scan_frequency参数决定，默认10s。Harvester使用内部时间戳来记录文件最后被收集的时间。例如：设置5m，则在Harvester读取文件的最后一行之后，开始倒计时5分钟，若5分钟内文件无变化，则关闭文件句柄。默认5m）。  

&emsp; Prospector（勘测者）：负责管理Harvester并找到所有读取源。    

    filebeat.prospectors:
    - input_type: log
    paths:
        - /apps/logs/*/info.log

&emsp; Prospector会找到/apps/logs/*目录下的所有info.log文件，并为每个文件启动一个Harvester。Prospector会检查每个文件，看Harvester是否已经启动，是否需要启动，或者文件是否可以忽略。若Harvester关闭，只有在文件大小发生变化的时候Prospector才会执行检查。只能检测本地的文件。  

&emsp; `Filebeat如何记录文件状态：`  
&emsp; 将文件状态记录在文件中（默认在/var/lib/filebeat/registry）。此状态可以记住Harvester收集文件的偏移量。若连接不上输出设备，如ES等，filebeat会记录发送前的最后一行，并再可以连接的时候继续发送。Filebeat在运行的时候，Prospector状态会被记录在内存中。Filebeat重启的时候，利用registry记录的状态来进行重建，用来还原到重启之前的状态。每个Prospector会为每个找到的文件记录一个状态，对于每个文件，Filebeat存储唯一标识符以检测文件是否先前被收集。  

&emsp; `Filebeat如何保证事件至少被输出一次：`    
&emsp; Filebeat之所以能保证事件至少被传递到配置的输出一次，没有数据丢失，是因为filebeat将每个事件的传递状态保存在文件中。在未得到输出方确认时，filebeat会尝试一直发送，直到得到回应。若filebeat在传输过程中被关闭，则不会再关闭之前确认所有时事件。任何在filebeat关闭之前为确认的时间，都会在filebeat重启之后重新发送。这可确保至少发送一次，但有可能会重复。可通过设置shutdown_timeout 参数来设置关闭之前的等待事件回应的时间（默认禁用）。   

### 1.3.2. Logstash工作原理  
&emsp; Logstash事件处理有三个阶段：inputs → filters → outputs。是一个接收，处理，转发日志的工具。支持系统日志，webserver日志，错误日志，应用日志，总之包括所有可以抛出来的日志类型。  
![image](http://www.wt1814.com/static/view/images/ES/es-20.png)  

&emsp; `Input：输入数据到logstash。`  
&emsp; 一些常用的输入为：  

* file：从文件系统的文件中读取，类似于tail -f命令  
* syslog：在514端口上监听系统日志消息，并根据RFC3164标准进行解析  
* redis：从redis service中读取  
* beats：从filebeat中读取  

&emsp; `Filters：数据中间处理，对数据进行操作。`    
&emsp; 一些常用的过滤器为：  

* grok：解析任意文本数据，Grok 是 Logstash 最重要的插件。它的主要作用就是将文本格式的字符串，转换成为具体的结构化的数据，配合正则表达式使用。内置120多个解析语法。  

        官方提供的grok表达式：https://github.com/logstash-plugins/logstash-patterns-core/tree/master/patterns
        grok在线调试：https://grokdebug.herokuapp.com/

* mutate：对字段进行转换。例如对字段进行删除、替换、修改、重命名等。  
* drop：丢弃一部分events不进行处理。  
* clone：拷贝 event，这个过程中也可以添加或移除字段。  
* geoip：添加地理信息(为前台kibana图形化展示使用)  

&emsp; `Outputs：outputs是logstash处理管道的最末端组件。`一个event可以在处理过程中经过多重输出，但是一旦所有的outputs都执行结束，这个event也就完成生命周期。  
&emsp; 一些常见的outputs为：  
&emsp; elasticsearch：可以高效的保存数据，并且能够方便和简单的进行查询。  
&emsp; file：将event数据保存到文件中。  
&emsp; graphite：将event数据发送到图形化组件中，一个很流行的开源存储图形化展示的组件。   

&emsp; `Codecs：codecs 是基于数据流的过滤器，它可以作为input，output的一部分配置。`Codecs可以帮助你轻松的分割发送过来已经被序列化的数据。  
&emsp; 一些常见的codecs：  
&emsp; json：使用json格式对数据进行编码/解码。   
&emsp; multiline：将汇多个事件中数据汇总为一个单一的行。比如：java异常信息和堆栈信息。  

