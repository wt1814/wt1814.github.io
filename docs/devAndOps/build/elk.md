

<!-- TOC -->

- [1. ELK搭建](#1-elk搭建)
    - [1.1. ***Docker部署](#11-docker部署)
        - [1.1.1. 步骤一：Docker构建FileBeat](#111-步骤一docker构建filebeat)
        - [1.1.2. 步骤二，Docker-Compose部署ELK](#112-步骤二docker-compose部署elk)
            - [1.1.2.1. ***单机部署](#1121-单机部署)
            - [1.1.2.2. 集群部署](#1122-集群部署)
    - [1.2. Linux系统搭建](#12-linux系统搭建)
        - [1.2.1. Elasticsearch](#121-elasticsearch)
        - [1.2.2. Kinaba](#122-kinaba)
        - [1.2.3. Logstash](#123-logstash)
            - [1.2.3.1. 安装](#1231-安装)
            - [1.2.3.2. 整合SpringBoot](#1232-整合springboot)
            - [1.2.3.3. 整合MySql](#1233-整合mysql)
        - [1.2.4. FileBeat](#124-filebeat)
        - [1.2.5. 安装IK分词器](#125-安装ik分词器)
        - [1.2.6. ES可视化客户端](#126-es可视化客户端)
            - [1.2.6.1. ElasticHD](#1261-elastichd)
            - [1.2.6.2. cerebro](#1262-cerebro)

<!-- /TOC -->


# 1. ELK搭建
<!-- 
kibana设置中文
https://blog.csdn.net/qq_18671415/article/details/109690002

Kibana启动报错：[resource_already_exists_exception]
https://blog.csdn.net/m0_37710023/article/details/111357638
Windows安装ES的head
https://blog.csdn.net/qq_37554565/article/details/117250647

-->

<!-- 
使用Docker搭建Elasticsearch集群服务教程 
https://mp.weixin.qq.com/s/pxI-poDt5F8TbAL9Rr7A8g

ELK原理
*** https://www.cnblogs.com/aresxin/p/8035137.html

-->

## 1.1. ***Docker部署  

### 1.1.1. 步骤一：Docker构建FileBeat
<!-- 

-->

http://testingpai.com/article/1606896558221

docker run -d --name=filebeat -v g:\software\elkDocker\filebeat\filebeat.yml:/usr/share/filebeat/filebeat.yml -v g:\software\elkDocker\filebeat\log:/var/log/filebeat/  docker.elastic.co/beats/filebeat:7.2.0  


&emsp; filebeat调试：  

  1. 进入filebeat.yml目录  
  2. https://blog.csdn.net/qq_41712271/article/details/123384250  


1. ***收集【宿主机目录】日志，要收集的日志目录从外面挂进来。***  
https://blog.51cto.com/u_14834727/3012235  

2. ***错误日志：Exiting: error loading config file: config file ("/opt/filebeat/filebeat.yml") can only be writable by the owner but the permissions are "-rwxrwxrwx" (to fix the permissions use: 'chmod go-w /opt/filebeat/filebeat.yml')***

*******【解决方案：把宿主机的filebeat.yml改成只读权限，再docker run】


Filebeat 连接 Logstash 常见问题  Failed to connect to backoff(async(tcp://ip:5044)): dial tcp ip:5044:i/o timeout  
https://blog.csdn.net/xy707707/article/details/100073701  


----------------------------
1. 网络，新建elk inspect  
2. filebeat.yml。hosts: ["logstash"]，logstash为网络卷中的别称  

```
filebeat.inputs:
- type: log				##文本日志
  paths: 
      - /var/log/app/*.log
output:
  logstash:  			#输出到logstash
    hosts: ["logstash"]
```
3. 启动命令：1收集的日志目录从外面挂进来；2使用--network elk加入网络卷elk和--link logstash链接logstash。

  docker run -d --name=filebeat -v g:\software\elkDocker\filebeat\filebeat.yml:/usr/share/filebeat/filebeat.yml -v g:\software\elkDocker\filebeat\log\all.log:/var/log/filebeat/  -v G:\logs:/var/log/app  --network elk   --link logstash docker.elastic.co/beats/filebeat:7.2.0



### 1.1.2. 步骤二，Docker-Compose部署ELK  

<!-- 
使用Docker搭建ELK日志系统 
https://mp.weixin.qq.com/s?__biz=MzAxMjY5NDU2Ng==&mid=2651854010&idx=2&sn=46f1f62cf15a1788da042f38d83d8a5a&chksm=804951f3b73ed8e5563e1d00bb2267a9d45a639955beaff6c03cfedbffd9aeeddf19e6fbf36c&mpshare=1&scene=1&srcid=&sharer_sharetime=1565741061263&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=36a99a852770fa03398d6d83b32183afed3cd3d1a3b4efe37bc5a5a3adc65c68adf852bc5aac9742c6ff7cce3b13adbc7ae3f843113389531a972cfe419ab100ddb13dfa6c31159f348959b7f6dbbe01&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=JLiKeNv%2F4KR6gCtdLZGhUXVH7BONlSBEY%2FTbKINtAXs2YG8At3hpMApp1DgxUdHh

Docker核心技术-企业级容器多主机ELK部署 Docker网络架构+数据管理+镜像+Dockerfile
https://www.bilibili.com/video/BV1hS4y1s7FT/?p=49&vd_source=9a9cf49f6bf9bd6a6e6e556f641ae9cb

-->


#### 1.1.2.1. ***单机部署  

<!-- 
Docker-Compose部署ELK 
*************** https://www.cnblogs.com/xiaobaibuai/p/15662224.html

logstash6.8.12动态生成elasticsearch的index的正确方法  
https://baijiahao.baidu.com/s?id=1676803444228068371&wfr=spider&for=pc

视频  
https://www.bilibili.com/video/BV1Sd4y1m7iq/?spm_id_from=333.337.search-card.all.click&vd_source=9a9cf49f6bf9bd6a6e6e556f641ae9cb
Docker核心技术-企业级容器多主机ELK部署 Docker网络架构+数据管理+镜像+Dockerfile
https://www.bilibili.com/video/BV1hS4y1s7FT/?p=49&vd_source=9a9cf49f6bf9bd6a6e6e556f641ae9cb
-->

1. 网络，新建elk inspect


  docker-compose up -d  

&emsp; docker-compose.yml  

```text
version: '7.2'
services:
  elasticsearch:
    image: elasticsearch:7.2.0
    container_name: elasticsearch
    privileged: true
    user: root
    environment:
      #设置集群名称为elasticsearch
      - cluster.name=elasticsearch 
      #以单一节点模式启动
      - discovery.type=single-node 
      #设置使用jvm内存大小
      - ES_JAVA_OPTS=-Xms512m -Xmx512m 
    volumes:
      - G:\software\elkDocker\elasticsearch/plugins:/usr/share/elasticsearch/plugins
      - G:\software\elkDocker\elasticsearch/data:/usr/share/elasticsearch/data
    ports:
      - 9200:9200
      - 9300:9300

  logstash:
    image: logstash:7.2.0
    container_name: logstash
    ports:
       - 4560:4560
    privileged: true
    environment:
      - TZ=Asia/Shanghai
    volumes:
      #挂载logstash的配置文件
      - G:\software\elkDocker\logstash/logstash.conf:/usr/share/logstash/pipeline/logstash.conf 
    depends_on:
      - elasticsearch 
    links:
      #可以用es这个域名访问elasticsearch服务
      - elasticsearch:es 

  kibana:
    image: kibana:7.2.0
    container_name: kibana
    ports:
        - 5601:5601
    privileged: true
    links:
      #可以用es这个域名访问elasticsearch服务
      - elasticsearch:es 
    depends_on:
      - elasticsearch 
    environment:
      #设置访问elasticsearch的地址
      - elasticsearch.hosts=http://elasticsearch:9200

networks:  ### 网络，新建elk inspect
  default:
    external:
      name: elk
```


&emsp; logstash.conf  

```text
input {
    beats {
        port => "5044"
    }
}

output {
	elasticsearch {
		hosts => "elasticsearch:9200"
		index => "logstash-%{+YYYY.MM.dd}"
	}
	stdout { codec => rubydebug }
}
```


#### 1.1.2.2. 集群部署  
<!-- 

Docker部署多机单节点ELK【集群】【ES + Logstash + Kibana + IK】
https://mp.weixin.qq.com/s/lXvBTja_B6l-z0oUgiLETQ
-->


## 1.2. Linux系统搭建  
<!-- 

https://blog.csdn.net/ECHOZCL/article/details/122740053
https://blog.csdn.net/CX1544539968/article/details/120038113
-->

### 1.2.1. Elasticsearch
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
    问题：elasticsearch在启动过程中被自动killed方法， https://blog.csdn.net/weixin_39643007/article/details/110426519  
    3. 启动 bin目录下 ./elasticsearch
2. 验证、访问： ip:9200


### 1.2.2. Kinaba  
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


### 1.2.3. Logstash  
<!-- 

http://www.360doc.com/content/22/0727/17/10087950_1041609111.shtml
https://www.elastic.co/cn/downloads/past-releases#logstash
https://elasticstack.blog.csdn.net/article/details/99655350
https://blog.csdn.net/CX1544539968/article/details/120038113

https://www.cnblogs.com/zyb2016/p/14886589.html
https://blog.csdn.net/u014527058/article/details/70495595

-->

&emsp; 端口在配置文件里。  

#### 1.2.3.1. 安装
1. 安装：tar -zxvf logstash-x.x.x-linux-x86_64.tar.gz   
2. 启动：bin目录下，./logstash -e "input {stdin {}} output {stdout{}}"  


#### 1.2.3.2. 整合SpringBoot  
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


#### 1.2.3.3. 整合MySql  
<!-- 

https://blog.csdn.net/hanjun0612/article/details/123061711
-->


### 1.2.4. FileBeat  
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

### 1.2.5. 安装IK分词器  

<!-- 

Elasticsearch 6 启动成功后 创建类型报错 analyzer [ik_smart] not found for field [methodDesc] 的原因
https://blog.csdn.net/sdfadfsdf/article/details/107466784
-->
https://github.com/medcl/elasticsearch-analysis-ik/ 



### 1.2.6. ES可视化客户端  
&emsp; 可视化客户端选择：Kibana、ElasticHD、cerebro...    

#### 1.2.6.1. ElasticHD  
1. http://github.com/qax-os/ElasticHD  
2. 启动：cmd进入目录，执行命令./ElasticHD -p 127.0.0.1:9800（自定义端口9800）  
&emsp; Linux中执行./ElasticHD -p 0.0.0.0:9800。访问：8.142.23.42:9800  


#### 1.2.6.2. cerebro


