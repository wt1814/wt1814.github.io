

<!-- TOC -->

- [1. EFK使用](#1-efk使用)
    - [1.1. EFK搭建](#11-efk搭建)
    - [1.2. 日志收集](#12-日志收集)
        - [1.2.1. 方式一：FileBeat采集Spring日志](#121-方式一filebeat采集spring日志)
        - [1.2.2. 方式二：Spring日志直接输出到logstash](#122-方式二spring日志直接输出到logstash)
    - [1.3. EFK监控](#13-efk监控)

<!-- /TOC -->

# 1. EFK使用  
<!-- 
https://blog.csdn.net/HuaZi_Myth/article/details/102770893?utm_medium=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromBaidu-1.control&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromBaidu-1.control

https://blog.csdn.net/ct1150/article/details/88058345
-->
&emsp; kubernetes可以实现efk的快速部署和使用，通过statefulset控制器部署elasticsearch组件，用来存储日志数据，还可通过volumenclaimtemplate动态生成pv实现es数据的持久化。通过deployment部署kibana组件，实现日志的可视化管理。通过daemonset控制器部署fluentd组件，来收集各节点和k8s集群的日志。  
&emsp; EFK可以收集Java服务、MySQL、Nginx等系统的日志。  
&emsp; Spring Boot整合ELK+Filebear构建日志系统：将Spring Boot应用与Filebeat部署在同一服务器上，使用相同的日志路径。  

## 1.1. EFK搭建  
......


## 1.2. 日志收集  
<!-- 

Filebeat、Logstash、Rsyslog 各种姿势采集Nginx日志 
https://mp.weixin.qq.com/s/TXmxWGvQ-gjTlpSXFkqMNQ
TB级微服务海量日志监控平台 
https://mp.weixin.qq.com/s/TcuXAOnqHWlKpceP1T4BpA
-->

### 1.2.1. 方式一：FileBeat采集Spring日志  
<!-- 
https://blog.csdn.net/zimou5581/article/details/90519307
--> 
&emsp; <font color = "red">无需修改项目工程文件。</font>  

### 1.2.2. 方式二：Spring日志直接输出到logstash  
&emsp; **无fileBeat组件时，日志需要直接输出到logstash。**

1. maven  

    ```xml
    <!--logback日志-->
    <dependency>
        <groupId>net.logstash.logback</groupId>
        <artifactId>logstash-logback-encoder</artifactId>
        <version>4.8</version>
    </dependency>
    ```

2. 修改logback.xml配置文件，添加logstash配置，并且使java应用打印出来的日志以json格式来显示，并且一个日志占用一行，这样对于elk处理非常简单方便高效。     

    ```xml
    <appender name="logstash" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <param name="Encoding" value="UTF-8"/>
        <remoteHost>192.168.1.102</remoteHost>
        <port>10514</port>
        <!-- <filter class="com.program.interceptor.ELKFilter"/>-->//引入过滤类
        <!-- encoder is required -->
        <encoder charset="UTF-8" class="net.logstash.logback.encoder.LogstashEncoder" >
            <customFields>{"appname":"${appName}"}</customFields> // 索引名
        </encoder>
    </appender>
    ```

## 1.3. EFK监控  
<!-- 
如何实现对ELK各组件的监控？试试Metricbeat 
https://mp.weixin.qq.com/s/Bt8_1TPxtKHStmYd_hQD0Q
-->
