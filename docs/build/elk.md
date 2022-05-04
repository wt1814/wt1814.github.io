
<!-- TOC -->

- [1. elasticsearch安装使用](#1-elasticsearch安装使用)
    - [1.1. mac系统](#11-mac系统)
    - [1.2. windowns系统](#12-windowns系统)
    - [1.3. 在本机启动多个项目启动多个节点](#13-在本机启动多个项目启动多个节点)

<!-- /TOC -->


# 1. elasticsearch安装使用

## 1.1. mac系统
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


## 1.2. windowns系统
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

## 1.3. 在本机启动多个项目启动多个节点  
<!-- 
https://blog.csdn.net/qq_35463719/article/details/121940803
-->

