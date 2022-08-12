
<!-- TOC -->

- [1. elasticsearch安装使用](#1-elasticsearch安装使用)
    - [1.1. Linux](#11-linux)
        - [1.1.1. Elasticsearch](#111-elasticsearch)
        - [1.1.2. Kinaba](#112-kinaba)
        - [1.1.3. Logstash](#113-logstash)
            - [1.1.3.1. 安装](#1131-安装)
            - [1.1.3.2. 整合SpringBoot](#1132-整合springboot)
            - [1.1.3.3. 整合MySql](#1133-整合mysql)
    - [1.2. Docker部署](#12-docker部署)
    - [1.3. mac系统](#13-mac系统)
    - [1.4. windowns系统](#14-windowns系统)
    - [1.5. 在本机启动多个项目启动多个节点](#15-在本机启动多个项目启动多个节点)

<!-- /TOC -->


# 1. elasticsearch安装使用

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
-->

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



## 1.2. Docker部署  
<!-- 
Docker部署多机单节点ELK集群【ES + Logstash + Kibana + IK】
https://mp.weixin.qq.com/s/lXvBTja_B6l-z0oUgiLETQ

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

