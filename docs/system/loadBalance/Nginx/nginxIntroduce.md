

<!-- TOC -->

- [1. Nginx](#1-nginx)
    - [1.4. 模块化设计](#14-模块化设计)
        - [1.4.1. 核心模块](#141-核心模块)
        - [1.4.2. 标准HTTP模块](#142-标准http模块)
        - [1.4.3. 可选HTTP模块](#143-可选http模块)
        - [1.4.4. 邮件服务模块](#144-邮件服务模块)
        - [1.4.5. 第三方模块](#145-第三方模块)
    - [1.5. Nginx常见的优化配置](#15-nginx常见的优化配置)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. Nginx是一个高性能的Web服务器。<font color = "red">Nginx工作在4层或7层。</font>  
2. **多进程：** Nginx启动时，会生成两种类型的进程，一个主进程master，一个（windows版本的目前只有一个）或多个工作进程worker。  
    * 主进程并不处理网络请求，主要负责调度工作进程：加载配置、启动工作进程、非停升级。  
    * **<font color = "red">`一般推荐worker进程数与CPU内核数一致，这样一来不存在大量的子进程生成和管理任务，避免了进程之间竞争CPU资源和进程切换的开销。`</font>**  

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
&emsp; Nginx是一个高性能的Web服务器。<font color = "red">Nginx工作在在4层或7层。</font>  

* 同时处理大量的并发请求（可以处理2-3万并发连接数，官方监测能支持5万并发）。
* 内存消耗小：开启10个nginx才占150M内存 ，nginx处理静态文件好，耗费内存少。  
* 节省宽带：支持GZIP压缩，可以添加浏览器本地缓存。
* <font color = "red">Nginx原理：两种进程、多进程单线程、基于异步非阻塞的事件驱动模型、模块化设计。</font>



---
## 1.4. 模块化设计  
&emsp; Nginx的worker进程，包括核心和功能性模块。高度模块化的设计是 Nginx 的架构基础。Nginx 服务器被分解为多个模块 ，每个模块就是一个功能模块 ，只负责自身的功能，模块之间严格遵循 “高内聚，低耦合” 的原则。  

* 核心模块负责维持一个运行循环 ( run-loop )，执行网络请求处理的 不同阶段 的模块功能。比如：网络读写、存储读写、内容传输、外出过滤，以及将请求发往上游服务器等。  
* 而其代码的模块化设计，也使得开发人员可以根据需要对功能模块进行适当的选择和修改，编译成具有特定功能的服务器。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/Linux/Nginx/nginx-8.png)  

### 1.4.1. 核心模块  
&emsp; 核心模块是 Nginx 服务器正常运行 必不可少的模块，提供错误日志记录 、 配置文件解析 、 事件驱动机制 、 进程管理 等核心功能。  

### 1.4.2. 标准HTTP模块  
&emsp; 标准HTTP模块提供HTTP协议解析相关的功能，比如：端口配置、网页编码设置、HTTP响应头设置等等。  

### 1.4.3. 可选HTTP模块  
&emsp; 可选 HTTP 模块主要用于 扩展 标准的 HTTP 功能，让 Nginx 能处理一些特殊的服务，比如：Flash 多媒体传输 、解析 GeoIP 请求、 网络传输压缩 、 安全协议 SSL 支持等。  

### 1.4.4. 邮件服务模块  
&emsp; 邮件服务模块主要用于支持 Nginx 的 邮件服务 ，包括对 POP3 协议、 IMAP 协议和 SMTP协议的支持。  

### 1.4.5. 第三方模块  
&emsp; 第三方模块是为了扩展 Nginx 服务器应用，完成开发者自定义功能，比如：Json 支持、 Lua 支持等。  

## 1.5. Nginx常见的优化配置
  
* 调整 worker_processes：指 Nginx 要生成的 Worker 数量，最佳实践是每个 CPU 运行 1 个工作进程。  
* 最大化 worker_connections。  
* 启用 Gzip 压缩：压缩文件大小，减少了客户端 HTTP 的传输带宽，因此提高了页面加载速度。  
* 为静态文件启用缓存。  
* 禁用 access_logs：访问日志记录，它记录每个 Nginx 请求，因此消耗了大量 CPU 资源，从而降低了 Nginx 性能。  



