

<!-- TOC -->

- [1. ELK搭建](#1-elk搭建)
    - [1.1. Linux](#11-linux)
        - [1.1.1. Elasticsearch](#111-elasticsearch)
        - [1.1.2. Kinaba](#112-kinaba)
        - [1.1.3. Logstash](#113-logstash)
            - [1.1.3.1. 安装](#1131-安装)
            - [1.1.3.2. 整合SpringBoot](#1132-整合springboot)
            - [1.1.3.3. 整合MySql](#1133-整合mysql)
        - [1.1.4. FileBeat](#114-filebeat)
        - [1.1.5. 安装IK分词器](#115-安装ik分词器)
        - [1.1.6. ES可视化客户端](#116-es可视化客户端)
            - [1.1.6.1. ElasticHD](#1161-elastichd)
            - [1.1.6.2. cerebro](#1162-cerebro)
    - [1.2. ***Docker部署](#12-docker部署)
    - [1.3. mac系统](#13-mac系统)
    - [1.4. windowns系统](#14-windowns系统)
    - [1.5. 在本机启动多个项目启动多个节点](#15-在本机启动多个项目启动多个节点)

<!-- /TOC -->


# 1. ELK搭建
<!-- 

使用Docker搭建Elasticsearch集群服务教程 
https://mp.weixin.qq.com/s/pxI-poDt5F8TbAL9Rr7A8g





ELK原理
*** https://www.cnblogs.com/aresxin/p/8035137.html

官方文档：
Filebeat：
https://www.elastic.co/cn/products/beats/filebeat
https://www.elastic.co/guide/en/beats/filebeat/5.6/index.html

Logstash：
https://www.elastic.co/cn/products/logstash
https://www.elastic.co/guide/en/logstash/5.6/index.html

Kibana:
https://www.elastic.co/cn/products/kibana
https://www.elastic.co/guide/en/kibana/5.5/index.html

Elasticsearch：
https://www.elastic.co/cn/products/elasticsearch
https://www.elastic.co/guide/en/elasticsearch/reference/5.6/index.html
elasticsearch中文社区：
https://elasticsearch.cn/

-->


## 1.1. Linux  
<!-- 

https://blog.csdn.net/ECHOZCL/article/details/122740053
https://blog.csdn.net/CX1544539968/article/details/120038113
-->

### 1.1.1. Elasticsearch
<!-- 

Linux环境下安装Elasticsearch，史上最详细的教程来啦~
https://blog.csdn.net/smilehappiness/article/details/118466378
JVM is using the client VM [Java HotSpot Client VM] but should be using a server VM for the best pe
https://blog.csdn.net/hnhroot/article/details/121497050
-->
1. 安装  
    1. 修改 /usr/local/elasticsearch-7.3.0/config/elasticsearch.yml 修改配置  

    ```text
    network.host: 0.0.0.0 
    xpack.ml.enabled: false

    bootstrap.memory_lock: false
    bootstrap.system_call_filter: false
    cluster.initial_master_nodes: ["node-1"]
    ```
    2. 修改内存大小，jvm.options文件。  
    问题：elasticsearch在启动过程中被自动killed方法，https://blog.csdn.net/weixin_39643007/article/details/110426519  
    3. 启动 bin目录下 ./elasticsearch
2. 验证、访问： ip:9200


### 1.1.2. Kinaba  
<!-- 
Linux版本Kibana安装教程
https://blog.csdn.net/qq_39706570/article/details/125293901
kibana启动时遇到的坑
https://blog.csdn.net/weixin_45495060/article/details/125183341
-->
1. 安装  
    1. 修改/usr/local/kibana-7.3.0-linux-x86_64/config/kibana.yml
    ```text
    server.port: 5601  #默认端口
    server.host: "0.0.0.0"
    i18n.locale: "zh-CN"
    elasticsearch.hosts: ["http://8.142.23.42:9200"]  #elasticsearch所在的IP+端口
    ```
    2. 启动：./kibana --allow-root
2. 验证、访问：ip:5601


### 1.1.3. Logstash  
<!-- 

http://www.360doc.com/content/22/0727/17/10087950_1041609111.shtml
https://www.elastic.co/cn/downloads/past-releases#logstash
https://elasticstack.blog.csdn.net/article/details/99655350
https://blog.csdn.net/CX1544539968/article/details/120038113

https://www.cnblogs.com/zyb2016/p/14886589.html
https://blog.csdn.net/u014527058/article/details/70495595

-->

&emsp; 端口在配置文件里。  

#### 1.1.3.1. 安装
1. 安装：tar -zxvf logstash-x.x.x-linux-x86_64.tar.gz   
2. 启动：bin目录下，./logstash -e "input {stdin {}} output {stdout{}}"  


#### 1.1.3.2. 整合SpringBoot  
1. 修改配置  
&emsp; 在 /usr/local/logstash-7.3.0/config 添加logstash-springboot.conf文件

```text
input {
  tcp {
    mode => "server"
    host => "0.0.0.0"
    port => 4560
    codec => json_lines
  }
}
output {
  elasticsearch {
    hosts => "8.142.23.42:9200"
    index => "springboot-logstash-%{+YYYY.MM.dd}"
  }
}
```

2. 启动：bin目录下执行 ./logstash -f ../config/logstash-springboot.conf  


#### 1.1.3.3. 整合MySql  
<!-- 

https://blog.csdn.net/hanjun0612/article/details/123061711
-->


### 1.1.4. FileBeat  
<!-- 

https://www.elastic.co/cn/downloads/beats/filebeat

rsyslog
https://www.jianshu.com/p/861091c71bcd

**** https://www.cnblogs.com/linjiqin/p/12106462.html


-->
1. 下载：https://www.elastic.co/cn/downloads/beats/filebeat  
2. 配置：  
    1. FileBeat是直接发送到ELS还是通过LogStash? https://www.it1352.com/1529867.html  
    2.   
3. 启动：
    1. windwos启动，https://blog.csdn.net/winsanity/article/details/120651928  
    cmd， .\filebeat -e -c filebeat.yml

### 1.1.5. 安装IK分词器  

<!-- 

Elasticsearch 6 启动成功后 创建类型报错 analyzer [ik_smart] not found for field [methodDesc] 的原因
https://blog.csdn.net/sdfadfsdf/article/details/107466784
-->
https://github.com/medcl/elasticsearch-analysis-ik/ 



### 1.1.6. ES可视化客户端  
&emsp; 可视化客户端选择：Kibana、ElasticHD、cerebro...    

#### 1.1.6.1. ElasticHD  
1. http://github.com/qax-os/ElasticHD  
2. 启动：cmd进入目录，执行命令./ElasticHD -p 127.0.0.1:9800（自定义端口9800） 
&emsp; Linux中执行./ElasticHD -p 0.0.0.0:9800。访问：8.142.23.42:9800  


#### 1.1.6.2. cerebro



## 1.2. ***Docker部署  
<!-- 


使用Docker搭建ELK日志系统 
https://mp.weixin.qq.com/s?__biz=MzAxMjY5NDU2Ng==&mid=2651854010&idx=2&sn=46f1f62cf15a1788da042f38d83d8a5a&chksm=804951f3b73ed8e5563e1d00bb2267a9d45a639955beaff6c03cfedbffd9aeeddf19e6fbf36c&mpshare=1&scene=1&srcid=&sharer_sharetime=1565741061263&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=36a99a852770fa03398d6d83b32183afed3cd3d1a3b4efe37bc5a5a3adc65c68adf852bc5aac9742c6ff7cce3b13adbc7ae3f843113389531a972cfe419ab100ddb13dfa6c31159f348959b7f6dbbe01&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=JLiKeNv%2F4KR6gCtdLZGhUXVH7BONlSBEY%2FTbKINtAXs2YG8At3hpMApp1DgxUdHh

Docker核心技术-企业级容器多主机ELK部署 Docker网络架构+数据管理+镜像+Dockerfile
https://www.bilibili.com/video/BV1hS4y1s7FT/?p=49&vd_source=9a9cf49f6bf9bd6a6e6e556f641ae9cb

-->

<!-- 
Docker-Compose部署ELK 
https://www.cnblogs.com/xiaobaibuai/p/15662224.html

Docker部署多机单节点ELK集群【ES + Logstash + Kibana + IK】
https://mp.weixin.qq.com/s/lXvBTja_B6l-z0oUgiLETQ


*** docker-compose安装ELK
https://www.jianshu.com/p/2d78ce6bc504
https://www.jianshu.com/p/50839769ffa3


https://www.bilibili.com/video/BV1Sd4y1m7iq/?spm_id_from=333.337.search-card.all.click&vd_source=9a9cf49f6bf9bd6a6e6e556f641ae9cb

Docker核心技术-企业级容器多主机ELK部署 Docker网络架构+数据管理+镜像+Dockerfile
https://www.bilibili.com/video/BV1hS4y1s7FT/?p=49&vd_source=9a9cf49f6bf9bd6a6e6e556f641ae9cb

-->


## 1.3. mac系统
cd /Users/wangtao/software/elk/elasticsearch-7.13.3
bin/elasticsearch

http://localhost:5601/app/dev_tools#/console
GET canal_product/_search



GET canal_product/_search



POST wt/_doc
{
    "mappings":{
        "_doc":{
            "properties":{
                "testid":{
                    "type":"long"
                },
                "name":{
                    "type":"text"
                }
            }
        }
    }
}


## 1.4. windowns系统
1. 安装elasticsearch  
2. 启动elasticsearch
    1. 进入 G:\software\elasticsearch-7.10.0-windows-x86_64\elasticsearch-7.10.0\bin  
    2. 双击elasticsearch.bat  
3. 访问elasticsearch：http://localhost:9200/
4. 安装Kibana  
5. 启动Kibana 
    1. 进入G:\software\kibana-7.10.0-windows-x86_64\kibana-7.10.0-windows-x86_64\bin  
    2. 双击kibana.bat  
6. 访问Kibana：http://localhost:5601/  
7. 安装head插件 
    1. 安装head插件环境nodejs  
    2. 安装grunt  
    3. 安装head插件  
    4. 启动head：  
        1. G:\software\elasticsearch-head-master\elasticsearch-head-master
        2. npm run start
4. 访问head：http://localhost:9100/

<!-- 
kibana设置中文
https://blog.csdn.net/qq_18671415/article/details/109690002

Kibana启动报错：[resource_already_exists_exception]
https://blog.csdn.net/m0_37710023/article/details/111357638
Windows安装ES的head
https://blog.csdn.net/qq_37554565/article/details/117250647

-->

## 1.5. 在本机启动多个项目启动多个节点  
<!-- 
https://blog.csdn.net/qq_35463719/article/details/121940803
-->
