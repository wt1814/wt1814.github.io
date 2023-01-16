

<!-- TOC -->

- [1. Nginx使用](#1-nginx使用)
    - [1.1. 基于配置文件的Nginx处理请求流程](#11-基于配置文件的nginx处理请求流程)
        - [1.1.1. IP和域名部分的处理](#111-ip和域名部分的处理)
        - [1.1.2. Location详解](#112-location详解)
    - [1.2. Nginx使用场景](#12-nginx使用场景)
        - [1.2.1. 前端](#121-前端)
            - [1.2.1.1. ***静态资源WEB服务](#1211-静态资源web服务)
            - [1.2.1.2. ***跨域解决](#1212-跨域解决)
            - [1.2.1.3. ***HTTPS](#1213-https)
            - [1.2.1.4. ***缓存](#1214-缓存)
            - [1.2.1.5. ~~动静分离~~](#1215-动静分离)
        - [1.2.2. ***反向代理和负载均衡](#122-反向代理和负载均衡)
        - [1.2.3. 其他](#123-其他)
            - [1.2.3.1. 地址重定向，Rewrite](#1231-地址重定向rewrite)
            - [1.2.3.2. 虚拟主机](#1232-虚拟主机)
            - [1.2.3.3. 多个webapp的配置](#1233-多个webapp的配置)
        - [1.2.4. 其他功能](#124-其他功能)
            - [1.2.4.1. 限流](#1241-限流)
            - [1.2.4.2. 黑白名单](#1242-黑白名单)
            - [1.2.4.3. 防盗链](#1243-防盗链)
            - [1.2.4.4. 流量复制](#1244-流量复制)
            - [1.2.4.5. 正向代理](#1245-正向代理)

<!-- /TOC -->


<!--
Nginx中文文档：http://www.nginx.cn/doc/
在线一键生成Nginx配置：https://nginxconfig.io/

Nginx如何处理一个请求
http://tengine.taobao.org/nginx_docs/cn/docs/http/request_processing.html

实用篇-无处不在的Location
https://mp.weixin.qq.com/s/ipGtkjTaiTMfUalaTASnCQ

*** 彻底搞懂 Nginx 的五大应用场景
*** https://cloud.tencent.com/developer/article/1845315
https://www.zhihu.com/question/470370584


https://mp.weixin.qq.com/s/trUWEfsxLWVASZckDOwqPQ
https://mp.weixin.qq.com/s/kIIGCq_oN66nt4MMYaCJpQ

 Nginx系列：配置跳转的常用方式 
 https://mp.weixin.qq.com/s/XEN_6Xz8Ayk1Z3qKugePjA

【Nginx】实现负载均衡、限流、缓存、黑白名单和灰度发布，这是最全的一篇了！
https://mp.weixin.qq.com/s/FdspSZ4_ETePyVALO_rNdg

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


### 1.2.1. 前端 
#### 1.2.1.1. ***静态资源WEB服务 
1. 静态资源类型  
&emsp; 非服务器动态运行生成的文件，换句话说，就是可以直接在服务器上找到对应文件的请求。  
    * 浏览器端渲染：HTML,CSS,JS
    * 图片：JPEG,GIF,PNG
    * 视频：FLV,MPEG
    * 文件：TXT，任意下载文件

#### 1.2.1.2. ***跨域解决  
<!-- 
https://mp.weixin.qq.com/s/oTBEK0tp2jANosVtRRj1lQ

前后端分离部署怎么解决跨域问题？
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247486541&idx=1&sn=40cec96a57eb1f4e70a033b24d8b184b&chksm=e9c35e2ddeb4d73bffe34324781916788eee61d6740fb9a91561846f68366fcc030e8d561215&mpshare=1&scene=1&srcid=&sharer_sharetime=1569342187668&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=2a4ff15fdd846346bbd73870dc7974f76f264c07959c9a7e0c24282e0a89ecadb89dcd58c963cc3023b3a52cc0769bec3e9aae6ed97b88ad8135632e00d0f94154e189553c317e60f4fbd95d4e1f75af&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=%2BeyejsSYMk60ZUY%2FpK4YBKveSPIi8GUzrQvyveBksIipCvi8KhPkraBA4Eyx2jY%2F

使用Nginx 实现纯前端跨域
https://blog.csdn.net/u012302552/article/details/81738185

-->
......

#### 1.2.1.3. ***HTTPS 
<!-- 

https://mp.weixin.qq.com/s?__biz=MzA4MTk3MjI0Mw==&mid=2247489372&idx=1&sn=096fcc379a6304a63049567a244af1c1&chksm=9f8d8a20a8fa0336329104640f983b481a4e970a84177df735b02aeeee49fa713d4f155975a3&scene=178&cur_album_id=1474377728786300933#rd

同时支持http和https
https://mp.weixin.qq.com/s?__biz=MzI3NzE0NjcwMg==&mid=2650124844&idx=2&sn=3750ae78f70526b6b9a2019ff07f574e&chksm=f36baf0dc41c261b73849a1a5b1dae819a5557c96ef2df594b8c06325dc818d0ea1a661b1225&mpshare=1&scene=1&srcid=&sharer_sharetime=1569688488710&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=6f23511bf9e1c01f58335462c0a5228e1a6d038723e752971fcd5a604eeeb6acf563bd6181e5e68ea0607c06d81fa3ae6ab713ff7e24671e92e90bfc568613c60b81d582abef7404408842a166371164&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=ZL%2FeHfhqz1QMMFsEMfVJBwZJ4lL9DTl1Z1M7e%2B%2FErhZY%2FUzuHIeMnYBVzJS6sOGw


Nginx静态资源服务器开启https配置及http rewrite到https：
https://blog.csdn.net/lhc1105/article/details/78774067

-->
&emsp; Nginx静态资源服务器开启https配置及http rewrite到https。  
......

#### 1.2.1.4. ***缓存  
&emsp; 通常对于静态资源，即较少经常更新的资源，如图片，css或js等进行缓存，从而在每次刷新浏览器的时候，不用重新请求，而是从缓存里面读取，这样就可以减轻服务器的压力。  
&emsp; Nginx设置缓存有两种方式：  

    proxy_cache_path和proxy_cache
    Cache-Control和Pragma

&emsp; 对于站点中不经常修改的静态内容（如图片，JS，CSS），可以在服务器中设置expires过期时间，控制浏览器缓存，达到有效减小带宽流量，降低服务器压力的目的。  



#### 1.2.1.5. ~~动静分离~~    
<!-- 
静态服务器
https://mp.weixin.qq.com/s/wfaveQ5qhiGNFbrktw6uYg
https://mp.weixin.qq.com/s?__biz=MzIwNTk5NjEzNw==&mid=2247489052&idx=2&sn=c0c2a6369c58adac7ae93e64c7d616b6&chksm=97293f1aa05eb60cc17b5ebe42da6d659c29f03c8628efe17b9775ad0c5b43b134d65a941dae&mpshare=1&scene=1&srcid=&sharer_sharetime=1576754715335&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=e2a6a5ccea4b8ce425348ae2f33a47e4980610c921b3ec6454b8654afb1afa0614e428065a262e73c57c2aea95f428cc1428db437a8deed4edcb0a7e41a3eeaf669d64c1d19684e7cf4f6df800ea485a&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=AYiLjNlCt%2FZDqUGto2Ql71c%3D&pass_ticket=k6uZZByTo2fQFzVvUtvPyhmI5ViFLGhQCjyvg5cJhfo3p1d4O0tHI6%2F00fVufJCm
-->

&emsp; Nginx是一个http服务器，可以独立提供http服务，可以做网页静态服务器。  
......



### 1.2.2. ***反向代理和负载均衡  
&emsp; [Nginx负载](/docs/system/loadBalance/Nginx/nginxLoad.md)   


### 1.2.3. 其他

#### 1.2.3.1. 地址重定向，Rewrite  
<!-- 
https://mp.weixin.qq.com/s?__biz=MjM5ODI5Njc2MA==&mid=2655828756&idx=1&sn=1e6e4203d84e85d4523073e5fb529656&chksm=bd74f6c38a037fd5c0812a775bd494c190395ee0d45b32e0349457e1488943ebc2d67f084dcc&mpshare=1&scene=1&srcid=&sharer_sharetime=1577187299959&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=7e51d7974732a32b94932fb1a25c33aa5e5fd789fb5c2d7129b5d1325b7946e8aa851c05f0c651ceb11382c510d18f69559b4b719cf1972c0e22d7f2de97c998b623ecd699f94c7d515d028f38457c01&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=AZfEi%2BIGtr%2FYmmJm5h2zfz8%3D&pass_ticket=fjo0TPQ4TftdXiH325uINjkxmTSYWN5xsY7SY8CPXJ8L70Z%2B9nqwLCPhjc61tfer
-->

&emsp; Rewrite 是 Nginx 服务器提供的一个重要的功能，它可以实现 URL 重写和重定向功能。  
......


#### 1.2.3.2. 虚拟主机  
<!-- 
 如何在服务器上添加虚拟IP
 https://mp.weixin.qq.com/s/K5r0UPCzG_FALD_H8Dibyg
-->
......

#### 1.2.3.3. 多个webapp的配置  
在一个端口号下部署多个应用  

<!-- 

https://blog.csdn.net/ManGooo0/article/details/124594170
-->


### 1.2.4. 其他功能  

#### 1.2.4.1. 限流  
<!--
图解Nginx限流配置 
https://mp.weixin.qq.com/s?__biz=MzIxNTAwNjA4OQ==&mid=2247485536&idx=1&sn=4eba961c92666a085d911b7b529aed84&chksm=979fa686a0e82f90a305471d46b52b1d66d321fe426d146dfb1b2655faab76856be81ed24b04&mpshare=1&scene=1&srcid=&sharer_sharetime=1567728523123&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=a1704a04d6cad8d0f19f8cf1b8875657e11dc85e1c91fc452db725245627087d7478cd8beef54caac40aece6ee6fa6afea6eee18b51858bd7f036c83fd81c5060ae5f2e8b21debb0671f01e1a9df24c1&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=z%2FwwCKX1VxUZIrB%2Fyurv7H3KYJfYtfEOC4f%2FPWwa0CuRWkVVV4TiFJiODLrgMPtY
如何优雅地使用 Nginx 限流
https://mp.weixin.qq.com/s/3rX8bm_giHUDMtXIZ7OguQ

-->

......

#### 1.2.4.2. 黑白名单  
......

#### 1.2.4.3. 防盗链  
......

#### 1.2.4.4. 流量复制  
<!-- 

Nginx流量复制 
https://mp.weixin.qq.com/s/1DX1E_j5eXo4-afXOow4vw
-->

......

#### 1.2.4.5. 正向代理  
......

