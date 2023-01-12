

<!-- TOC -->

- [1. Nginx](#1-nginx)
    - [1.1. Nginx介绍](#11-nginx介绍)
    - [1.2. 模块化设计](#12-模块化设计)
        - [1.2.1. 核心模块](#121-核心模块)
        - [1.2.2. 标准HTTP模块](#122-标准http模块)
        - [1.2.3. 可选HTTP模块](#123-可选http模块)
        - [1.2.4. 邮件服务模块](#124-邮件服务模块)
        - [1.2.5. 第三方模块](#125-第三方模块)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. Nginx是一个高性能的Web服务器。<font color = "red">Nginx工作在4层或7层。</font>  


# 1. Nginx  
<!--
nginx四层和七层负载均衡的区别
https://blog.csdn.net/friends99/article/details/79803638
nginx4层是哪四层
https://m.php.cn/nginx/434192.html


Nginx 的配置文件
https://mp.weixin.qq.com/s/iYvNHkWaQ8CyuPQJQbidlA
Nginx如何工作？
https://mp.weixin.qq.com/s/pmS-9Z-RAkVatdwlyNuFaQ
-->

## 1.1. Nginx介绍  

&emsp; Nginx是一个高性能的Web服务器。<font color = "red">Nginx工作在在4层或7层。</font>  

* 同时处理大量的并发请求（可以处理2-3万并发连接数，官方监测能支持5万并发）。
* 内存消耗小：开启10个nginx才占150M内存 ，nginx处理静态文件好，耗费内存少。  
* 节省宽带：支持GZIP压缩，可以添加浏览器本地缓存。
* <font color = "red">Nginx原理：两种进程、多进程单线程、基于异步非阻塞的事件驱动模型、模块化设计。</font>



---
## 1.2. 模块化设计  
&emsp; Nginx的worker进程，包括核心和功能性模块。高度模块化的设计是 Nginx 的架构基础。Nginx 服务器被分解为多个模块 ，每个模块就是一个功能模块 ，只负责自身的功能，模块之间严格遵循 “高内聚，低耦合” 的原则。  

* 核心模块负责维持一个运行循环 ( run-loop )，执行网络请求处理的 不同阶段 的模块功能。比如：网络读写、存储读写、内容传输、外出过滤，以及将请求发往上游服务器等。  
* 而其代码的模块化设计，也使得开发人员可以根据需要对功能模块进行适当的选择和修改，编译成具有特定功能的服务器。  

![image](http://182.92.69.8:8081/img/Linux/Nginx/nginx-8.png)  

### 1.2.1. 核心模块  
&emsp; 核心模块是 Nginx 服务器正常运行 必不可少的模块，提供错误日志记录 、 配置文件解析 、 事件驱动机制 、 进程管理 等核心功能。  

### 1.2.2. 标准HTTP模块  
&emsp; 标准HTTP模块提供HTTP协议解析相关的功能，比如：端口配置、网页编码设置、HTTP响应头设置等等。  

### 1.2.3. 可选HTTP模块  
&emsp; 可选 HTTP 模块主要用于 扩展 标准的 HTTP 功能，让 Nginx 能处理一些特殊的服务，比如：Flash 多媒体传输 、解析 GeoIP 请求、 网络传输压缩 、 安全协议 SSL 支持等。  

### 1.2.4. 邮件服务模块  
&emsp; 邮件服务模块主要用于支持 Nginx 的 邮件服务 ，包括对 POP3 协议、 IMAP 协议和 SMTP协议的支持。  

### 1.2.5. 第三方模块  
&emsp; 第三方模块是为了扩展 Nginx 服务器应用，完成开发者自定义功能，比如：Json 支持、 Lua 支持等。  

