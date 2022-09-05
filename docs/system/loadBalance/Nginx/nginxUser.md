

<!-- TOC -->

- [1. Nginx使用](#1-nginx使用)
    - [1.1. 基于配置文件的Nginx处理请求流程](#11-基于配置文件的nginx处理请求流程)
        - [1.1.1. IP和域名部分的处理](#111-ip和域名部分的处理)
        - [1.1.2. Location详解](#112-location详解)
    - [1.2. Nginx使用场景](#12-nginx使用场景)
        - [1.2.1. ***反向代理](#121-反向代理)
        - [1.2.2. ***静态资源WEB服务](#122-静态资源web服务)
        - [1.2.3. ***HTTPS反向代理配置](#123-https反向代理配置)
        - [1.2.4. ***动静分离](#124-动静分离)
        - [1.2.5. ***跨域解决](#125-跨域解决)
        - [1.2.6. 地址重定向，Rewrite](#126-地址重定向rewrite)
        - [1.2.7. 虚拟主机](#127-虚拟主机)
        - [1.2.8. 多个webapp的配置](#128-多个webapp的配置)
        - [1.2.9. 其他功能](#129-其他功能)
            - [1.2.9.1. 缓存](#1291-缓存)
            - [1.2.9.2. 限流](#1292-限流)
            - [1.2.9.3. 黑白名单](#1293-黑白名单)
            - [1.2.9.4. 防盗链](#1294-防盗链)
            - [1.2.9.5. 流量复制](#1295-流量复制)
            - [1.2.9.6. 正向代理](#1296-正向代理)

<!-- /TOC -->


<!-- 

*** 彻底搞懂 Nginx 的五大应用场景
*** https://cloud.tencent.com/developer/article/1845315
https://www.zhihu.com/question/470370584



https://mp.weixin.qq.com/s/trUWEfsxLWVASZckDOwqPQ
https://mp.weixin.qq.com/s/kIIGCq_oN66nt4MMYaCJpQ

 Nginx系列：配置跳转的常用方式 
 https://mp.weixin.qq.com/s/XEN_6Xz8Ayk1Z3qKugePjA

-->
&emsp; **<font color = "red">总结：</font>**  
1. <font color = "red">Nginx服务器处理一个请求是按照两部分进行的。第一部分是IP和域名，由listen和server_name指令匹配server模块；第二部分是URL，匹配server模块里的location；最后就是location里的具体处理。</font>  
2. <font color = "red">Nginx使用场景：反向代理、虚拟主机、静态资源WEB服务、缓存、限流、黑白名单、防盗链、流量复制...</font>  
3. 负载均衡：  
    1. **<font color = "red">Nginx反向代理通过proxy_pass来配置；负载均衡使用Upstream模块实现。</font>**  
    2. **<font color = "red">Nginx支持的负载均衡调度算法方式如下：</font>**  
        * **<font color = "red">轮询（默认）</font>** 
        * **<font color = "red">weight：</font>** 指定权重。  
        * **<font color = "red">ip_hash</font>**  
        * **<font color = "red">url_hash（第三方）</font>**  
        * **<font color = "red">fair（第三方）：</font>** 智能调整调度算法，动态的根据后端服务器的请求处理到响应的时间进行均衡分配。  

# 1. Nginx使用  
## 1.1. 基于配置文件的Nginx处理请求流程  
&emsp; <font color = "red">Nginx服务器处理一个请求是按照两部分进行的。第一部分是IP和域名，由listen和server_name指令匹配server模块；第二部分是URL，匹配server模块里的location；最后就是location里的具体处理。</font>  

### 1.1.1. IP和域名部分的处理  

&emsp; **基于名称的虚拟服务器**  
```
server {
    listen      80;
    server_name example.org www.example.org;
    ...
}

server {
    listen      80;
    server_name example.net www.example.net;
    ...
}

server {
    listen      80;
    server_name example.com www.example.com;
    ...
}
```
&emsp; 若是这种配置，nginx判断使用那个服务器的依据是request头部中的Host，Host 匹配到哪个server_name就使用哪个服务器。如果都不匹配，则使用默认的服务器。在未显示指定默认服务器的情况下，比如上面的这个例子，nginx认为第一个server为默认服务器。如需显示指定，如下。

```
# 0.8.21 版本以前使用的是default
server {
    listen      80 default_server; #default_server 是监听端口的一个属性
    server_name example.net www.example.net;
    ...
}
```

&emsp; **如何处理不含Host头部的请求**  

```
server {
    listen      80;
    # 使用空字符串匹配未定义的Host
    server_name "";
    # 返回一个特殊状态码，并关闭连接
    return      444;
}
```
&emsp; 注意：0.8.48版本以后，""是server_name的默认值，所以此处可以省略server_name ""，在更早的版本，server_name的默认值为主机的hostname。

&emsp; **基于名称和IP的混合虚拟服务器**  
&emsp; 前提知识：一台主机可以有多个IP(多网卡)，一个IP可以绑定多个域名。  

```
server {
    listen      192.168.1.1:80;
    server_name example.org www.example.org;
    ...
}

server {
    listen      192.168.1.1:80;
    server_name example.net www.example.net;
    ...
}

server {
    listen      192.168.1.2:80;
    server_name example.com www.example.com;
    ...
}
```
&emsp; nginx首先匹配监听IP地址和端口，匹配成功之后，再匹配相应的server_name，这个通过之后就选择使用该server处理请求，否则使用默认的server。正如前面所说，default_server 是监听端口的一个属性，所以不同的默认服务器应该有且只有一个定义在不同的IP:端口，如下。  

```
server {
    listen      192.168.1.1:80;
    server_name example.org www.example.org;
    ...
}

server {
    listen      192.168.1.1:80 default_server;
    server_name example.net www.example.net;
    ...
}

server {
    listen      192.168.1.2:80 default_server;
    server_name example.com www.example.com;
    ...
}
```

### 1.1.2. Location详解  
<!-- 
Nginx 实践｜location 路径匹配
https://mp.weixin.qq.com/s/qchaaVoOSJOqnRBlBIU--g
-->
&emsp; URL部分处理通过location实现。location指令的作用是根据用户请求的URI来执行不同的应用，也就是根据用户请求的网站URL进行匹配，匹配成功即进行相关的操作。    


## 1.2. Nginx使用场景  
&emsp; Nginx基本功能：作为http server、虚拟主机、反向代理服务器(负载均衡)、电子邮件(IMAP/POP3)代理服务器。  

### 1.2.1. ***反向代理  
&emsp; [Nginx负载](/docs/system/loadBalance/Nginx/nginxLoad.md)   


### 1.2.2. ***静态资源WEB服务 
1. 静态资源类型  
&emsp; 非服务器动态运行生成的文件，换句话说，就是可以直接在服务器上找到对应文件的请求。  
    * 浏览器端渲染：HTML,CSS,JS
    * 图片：JPEG,GIF,PNG
    * 视频：FLV,MPEG
    * 文件：TXT，任意下载文件

### 1.2.3. ***HTTPS反向代理配置  
<!-- 

https://mp.weixin.qq.com/s?__biz=MzA4MTk3MjI0Mw==&mid=2247489372&idx=1&sn=096fcc379a6304a63049567a244af1c1&chksm=9f8d8a20a8fa0336329104640f983b481a4e970a84177df735b02aeeee49fa713d4f155975a3&scene=178&cur_album_id=1474377728786300933#rd
-->
&emsp; Nginx静态资源服务器开启https配置及http rewrite到https。  
......

### 1.2.4. ***动静分离    
&emsp; Nginx是一个http服务器，可以独立提供http服务，可以做网页静态服务器。  
......

### 1.2.5. ***跨域解决  
<!-- 
https://mp.weixin.qq.com/s/oTBEK0tp2jANosVtRRj1lQ
-->
......


### 1.2.6. 地址重定向，Rewrite  
&emsp; Rewrite 是 Nginx 服务器提供的一个重要的功能，它可以实现 URL 重写和重定向功能。  
......


### 1.2.7. 虚拟主机  
<!-- 
 如何在服务器上添加虚拟IP
 https://mp.weixin.qq.com/s/K5r0UPCzG_FALD_H8Dibyg
-->
......

### 1.2.8. 多个webapp的配置  
......

### 1.2.9. 其他功能  
#### 1.2.9.1. 缓存  
......

#### 1.2.9.2. 限流  
......

#### 1.2.9.3. 黑白名单  
......

#### 1.2.9.4. 防盗链  
......

#### 1.2.9.5. 流量复制  
......

#### 1.2.9.6. 正向代理  
......

