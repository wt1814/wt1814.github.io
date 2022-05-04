

# elasticsearch安装使用

## mac系统
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


## windowns系统
1. 安装elasticsearch  
2. 启动elasticsearch
    1. 进入 G:\software\elasticsearch-7.10.0-windows-x86_64\elasticsearch-7.10.0\bin  
    2. 双击elasticsearch.bat  
3. 安装Kibana  
4. 启动Kibana 
    1. 进入G:\software\kibana-7.10.0-windows-x86_64\kibana-7.10.0-windows-x86_64\bin  
    2. 双击kibana.bat  
5. 访问Kibana：localhost:5601  



## 在本机启动多个项目启动多个节点  
<!-- 
https://blog.csdn.net/qq_35463719/article/details/121940803
-->

