

<!-- TOC -->

- [1. prometheus介绍](#1-prometheus介绍)
    - [1.1. 架构图](#11-架构图)
    - [1.2. 各组件运行流程](#12-各组件运行流程)

<!-- /TOC -->


# 1. prometheus介绍
<!--
Prometheus
https://mp.weixin.qq.com/s/W38FcwGmwPj1tp_87FVC1A

搭建Prometheus+Grafana的云平台监控系统
https://www.jianshu.com/p/268489bf5756?utm_campaign=haruki&utm_content=note&utm_medium=reader_share&utm_source=weixin

-->

1. 怎么采集监控数据？  
&emsp; 要采集目标（主机或服务）的监控数据，首先就要在被采集目标上安装采集组件，这种采集组件被称为Exporter。prometheus.io官网上有很多这种exporter，比如：  

    Consul exporter (official)
    Memcached exporter (official)
    MySQL server exporter (official)
    Node/system metrics exporter (official)
    HAProxy exporter (official)
    RabbitMQ exporter
    Grok exporter
    InfluxDB exporter (official)

&emsp; 这些exporter能为我们采集目标的监控数据，然后传输给普罗米修斯。这时候，exporter会暴露一个http接口，普罗米修斯通过HTTP协议使用Pull的方式周期性拉取相应的数据。  

&emsp; 输出被监控组件信息的HTTP接口被叫做exporter。目前互联网公司常用的组件大部分都有exporter可以直接使用，比如Varnish、Haproxy、Nginx、MySQL、Linux 系统信息 (包括磁盘、内存、CPU、网络等等)，具体支持的源看：https://github.com/prometheus。  

--------------------

&emsp; Prometheus基本原理是通过HTTP协议周期性抓取被监控组件的状态，这样做的好处是任意组件只要提供HTTP接口就可以接入监控系统，不需要任何SDK或者其他的集成过程。这样做非常适合虚拟化环境比如VM或者Docker 。

&emsp; 输出被监控组件信息的HTTP接口被叫做exporter。目前互联网公司常用的组件大部分都有exporter可以直接使用，比如Varnish、Haproxy、Nginx、MySQL、Linux 系统信息 (包括磁盘、内存、CPU、网络等等)，具体支持的源看：https://github.com/prometheus。




## 1.1. 架构图  
&emsp; Prometheus提供了从指标暴露，到指标抓取、存储和可视化，以及最后的监控告警等一系列组件。  
![image](http://182.92.69.8:8081/img/devops/prometheus/prometheus-2.png)  

![image](http://182.92.69.8:8081/img/devops/prometheus/prometheus-1.png)  
Prometheus  Server: 收集指标和存储时间序列数据，并提供查询接口  
ClientLibrary:客户端库  
Push Gateway: 短期存储指标数据。主要用于临时性的任务  
**Exporters:采集已有的第三方服务监控指标并暴露metrics**    
Alertmanager:告警  
Web  UI :简单的web控制台  


## 1.2. 各组件运行流程  


