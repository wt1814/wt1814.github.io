

<!-- TOC -->

- [1. Elasticsearch REST](#1-elasticsearch-rest)
    - [1.1. Elasticsearch REST语法](#11-elasticsearch-rest语法)
        - [1.1.1. REST URL和选项概述](#111-rest-url和选项概述)
        - [1.1.2. 常用选项](#112-常用选项)
    - [1.1.3. 状态 & 统计相关命令清单](#113-状态--统计相关命令清单)

<!-- /TOC -->

# 1. Elasticsearch REST  
<!-- 
干货 | Elasticsearch 开发实战常用命令清单 
https://mp.weixin.qq.com/s/5vdAd8_x056HGYvRjsdzcQ
-->
&emsp; Elasticsearch具有一组丰富的易于理解的REST API，这些API均可如下几种方式通过HTTP调用进行访问。  

* Curl
* Postman
* head插件
* cerebro工具
* kibana  

&emsp; 开发实战环节，推荐使用：kibana Dev-tools。  
![image](http://182.92.69.8:8081/img/ES/es-108.png)  
![image](http://182.92.69.8:8081/img/ES/es-22.png)  

## 1.1. Elasticsearch REST语法   
### 1.1.1. REST URL和选项概述  
&emsp; 示例：  

```text
POST products/_search?size=0
{
  "aggs": {
    "product_aggs": {
      "terms": {
        "field":"name.keyword"
      }
    }
  }
}
```

&emsp; 调用REST API很容易，除了实际的主机名/ IP和端口外，它还包含四个部分：  

* 动词  
&emsp; REST调用的 GET，PUT，POST和DELETE部分。在Kibana中，可以直接指定这些名称，对于cURL，请使用-X选项。  
&emsp; 对应示例中的：POST  
* 路径  
&emsp; API和路径。例如：/_cluster/health或/logstash-cron-2020.07.03/_mapping-路径的第一部分通常是索引名称，除非它以_开头。  
&emsp; 对应示例中的：products/_search，其中products是索引。  
* 参数  
&emsp; 后面的各种选项。例如?h或?v    
&emsp; 对应示例中的：?size=0。参数设定部分。  
* 正文  
&emsp; 某些调用需要JSON正文(例如设置选项)，并将包含在{}中。  
&emsp; 对应示例中的：检索语句部分。  

### 1.1.2. 常用选项  
&emsp; 有一些通用选项适用于许多(不是全部)URL。这些是：  

* ?help——帮助选项。  
&emsp; 将在列表中提供 API 可用的字段，其中包含短名称和长名称、说明等。  
&emsp; 举例：GET _cat/indices?help  
* ?h =-“ h”——使用上方“帮助”显示中的短名称或长名称指定要包括在结果中的字段。这些用逗号分隔，没有空格。  
&emsp; 举例：GET _cat/indices?h=docs.count,store.size  
* ?v——'v'在回复的顶部包括字段名称。  
&emsp; GET _cat/indices?h=docs.count,store.size&v  
&emsp; -v 参数的妙处主要体现在：加上了表头，直观交互呈现。  
* ?s——'s'用于排序，使用列出的字段作为排序键。  
&emsp; 如下所示：可能会看到节点列表。包括：返回字段名称，字段名称要显示并按名称name排序：  
&emsp; GET /_cat/nodes?v&h=heap.percent,diskUsedPercent,cpu,master,name&s=name


## 1.1.3. 状态 & 统计相关命令清单  
&emsp; 最有用的 API 调用通常与集群的运行状况，状态和统计信息有关，例如：  

* 获取版本和集群名称等信息。  
&emsp; GET /   
* 获取集群健康状态等信息，包括集群名称、节点数、数据节点数、分片等的一些统计信息。  
&emsp; GET /_cluster/health  
* 获取节点列表信息。显示了堆内存、磁盘使用情况，CPU 、负载和主机角色。  
&emsp; 用途：用来监视负载和磁盘使用情况以及主机角色。  
&emsp; GET /_cat/nodes?v&h=heap.percent,diskUsedPercent,cpu,load_1m,master,name&s=name  
* Index Level 索引层面健康  
&emsp; GET /_cluster/health?level=indices&pretty  
* Shard Level 分片层面健康  
&emsp; GET /_cluster/health?level=shards&pretty  
* 获取索引，文档，缓存，段，节点等的集群统计信息的更深入概述。  
&emsp; 用途：有助于基本故障排除。  
&emsp; GET /_cluster/stats   
* 获取节点级别的更多统计信息，包括堆使用情况等。  
&emsp; GET /_nodes/stats   
