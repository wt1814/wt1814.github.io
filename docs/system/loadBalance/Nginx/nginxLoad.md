
<!-- TOC -->

- [1. 反向代理](#1-反向代理)
    - [1.1. 代理服务器简介](#11-代理服务器简介)
        - [1.1.1. 正向代理](#111-正向代理)
        - [1.1.2. 反向代理](#112-反向代理)
        - [1.1.3. 正向代理和反向代理对比](#113-正向代理和反向代理对比)
    - [1.2. ★★★~~Nginx负载均衡~~](#12-★★★nginx负载均衡)
    - [后端服务应用健康检测](#后端服务应用健康检测)

<!-- /TOC -->

# 1. 反向代理  
## 1.1. 代理服务器简介  
&emsp; **什么是代理服务器(Proxy Serve)？**  
&emsp; 提供代理服务的电脑系统或其它类型的网络终端，代替网络用户去取得网络信息。  
&emsp; **为什么使用代理服务器？**  
1. 提高访问速度：由于目标主机返回的数据会存放在代理服务器的硬盘中，因此下一次客户再访问相同的站点数据时，会直接从代理服务器的硬盘中读取，起到了缓存的作用，尤其对于热门网站能明显提高访问速度。  
2. 防火墙作用：由于所有的客户机请求都必须通过代理服务器访问远程站点，因此可以在代理服务器上设限，过滤掉某些不安全信息。同时正向代理中上网者可以隐藏自己的IP，免受攻击。  
3. 突破访问限制：互联网上有许多开发的代理服务器，客户机在访问受限时，可通过不受限的代理服务器访问目标站点，通俗说，使用的翻墙浏览器就是利用了代理服务器，可以直接访问外网。  

### 1.1.1. 正向代理  
![image](http://www.wt1814.com/static/view/images/Linux/Nginx/nginx-1.png)  
&emsp; 正向代理：内网服务器主动去请求外网的服务的一种行为。正向代理其实就是说客户端无法主动或者不打算完成主动去向某服务器发起请求，而是委托了nginx代理服务器去向服务器发起请求，并且获得处理结果，返回给客户端。  
&emsp; 正向代理主要应用于内网环境中只有某台特定服务器支持连接互联网，而其它同一局域网的服务器IP都不支持直接连接互联网，此时可以在支持连接公网的服务器配置nginx的正向代理，局域网内其它机器可通过此台服务器连接公网。  
&emsp; 如图，服务器①的IP没有访问公网的权限，nginx服务器同时连接了内网和公网，则服务器①可通过nginx服务器访问公网。  
![image](http://www.wt1814.com/static/view/images/Linux/Nginx/nginx-2.png)   
&emsp; (1)访问原来无法访问的资源，如google。  
&emsp; (2)可以做缓存，加速访问资源。  
&emsp; (3)对客户端访问授权，上网进行认证。  
&emsp; (4)代理可以记录用户访问记录(上网行为管理)，对外隐藏用户信息。  

### 1.1.2. 反向代理  
&emsp; 反向代理(Reverse Proxy)指以代理服务器来接受internet上的连接请求，然后将请求转发给内部网络上的服务器，并将从服务器上得到的结果返回给internet上请求连接的客户端，此时代理服务器对外就表现为一个反向代理服务器。  
&emsp; 对反向代理服务器的攻击并不会使得网页信息遭到破坏，这样就增强了Web服务器的安全性。这种方式通过降低了向WEB服务器的请求数从而降低了WEB服务器的负载。  
&emsp; 当网站的访问量达到一定程度后，单台服务器不能满足用户的请求时，需要用多台服务器集群可以使用nginx做反向代理。并且多台服务器可以平均分担负载，不会因为某台服务器负载高宕机而某台服务器闲置的情况。  
![image](http://www.wt1814.com/static/view/images/Linux/Nginx/nginx-3.png) 

&emsp; 反向代理的作用：  
* 保证内网的安全，可以使用反向代理提供WAF功能，阻止web攻击大型网站，通常将反向代理作为公网访问地址，Web服务器是内网。    
* 负载均衡，通过反向代理服务器来优化网站的负载。    

### 1.1.3. 正向代理和反向代理对比  
![image](http://www.wt1814.com/static/view/images/Linux/Nginx/nginx-4.png) 
![image](http://www.wt1814.com/static/view/images/Linux/Nginx/nginx-5.png) 

* 位置不同  

        正向代理，架设在客户机和目标主机之间； 
        反向代理，架设在服务器端；  

* 代理对象不同  

        正向代理，代理客户端，服务端不知道实际发起请求的客户端；   
        反向代理，代理服务端，客户端不知道实际提供服务的服务端；   
        备注：正向代理–HTTP代理为多个人提供翻墙服务；反向代理–百度外卖为多个商户提供平台给某个用户提供外卖服务。  

* 用途不同  

        正向代理，为在防火墙内的局域网客户端提供访问Internet的途径；  
        反向代理，将防火墙后面的服务器提供给Internet访问；  

* 安全性不同  

        正向代理允许客户端通过它访问任意网站并且隐藏客户端自身，因此必须采取安全措施以确保仅为授权的客户端提供服务； 
        反向代理都对外都是透明的，访问者并不知道自己访问的是哪一个代理。

&emsp; **正向代理的应用：**  
1. 访问原来无法访问的资源。   
2. 用作缓存，加速访问速度。   
3. 对客户端访问授权，上网进行认证。  
4. 代理可以记录用户访问记录(上网行为管理)，对外隐藏用户信息。  

&emsp; **反向代理的应用：**  
1. 保护内网安全 
2. 负载均衡 
3. 缓存，减少服务器的压力 

## 1.2. ★★★~~Nginx负载均衡~~  
<!-- 

讲讲Nginx如何实现四层负载均衡
https://blog.csdn.net/yihuliunian/article/details/108534708
-->

&emsp; 负载均衡功能即是反向代理的应用，只不过负载均衡是代理多台服务器，更注重其均衡转发功能。    
![image](http://www.wt1814.com/static/view/images/Linux/Nginx/nginx-5.png) 

&emsp; **<font color = "red">Nginx支持的负载均衡调度算法方式如下：</font>**  
* **<font color = "red">轮询(默认)</font>** ：接收到的请求按照顺序逐一分配到不同的后端服务器，即使在使用过程中，某一台后端服务器宕机，Nginx会自动将该服务器剔除出队列，请求受理情况不会受到任何影响。 
* **<font color = "red">weight</font>** ：指定权重。这种方式下，可以给不同的后端服务器设置一个权重值(weight)，用于调整不同的服务器上请求的分配率；权重数据越大，被分配到请求的几率越大；该权重值，主要是针对实际工作环境中不同的后端服务器硬件配置进行调整的。  
* **<font color = "red">ip_hash</font>** ：每个请求按照发起客户端的ip的hash结果进行匹配，这样的算法下一个固定ip地址的客户端总会访问到同一个后端服务器，这也在一定程度上解决了集群部署环境下session共享的问题。  
* **<font color = "red">fair(第三方)</font>** ：智能调整调度算法，动态的根据后端服务器的请求处理到响应的时间进行均衡分配，<font color = "red">响应时间短处理效率高的服务器分配到请求的概率高，响应时间长处理效率低的服务器分配到的请求少；</font>结合了前两者的优点的一种调度算法。但是需要注意的是Nginx默认不支持fair算法，如果要使用这种调度算法，请安装upstream_fair模块。  
* **<font color = "red">url_hash(第三方)</font>** ：按照访问的url的hash结果分配请求，每个请求的url会指向后端固定的某个服务器，可以在Nginx作为静态服务器的情况下提高缓存效率。同样要注意Nginx默认不支持这种调度算法，要使用的话需要安装Nginx的hash软件包。  

&emsp; **Nginx负载均衡配置：**    
&emsp; **<font color = "red">Nginx反向代理通过proxy_pass来配置。负载均衡使用Upstream模块实现。</font>**  
&emsp; 假设这样一个应用场景：将应用部署在192.168.1.11:80、192.168.1.12:80、192.168.1.13:80三台linux环境的服务器上。网站域名叫 www.helloworld.com ，公网IP为192.168.1.11。在公网IP所在的服务器上部署nginx，对所有请求做负载均衡处理。  
&emsp; nginx.conf 配置如下：  

```
http {
     #设定mime类型,类型由mime.type文件定义
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;
    #设定日志格式
    access_log    /var/log/nginx/access.log;

    #设定负载均衡的服务器列表
    upstream load_balance_server {
        #weigth参数表示权值，权值越高被分配到的几率越大
        server 192.168.1.11:80   weight=5;
        server 192.168.1.12:80   weight=1;
        server 192.168.1.13:80   weight=6;
    }

   #HTTP服务器
   server {
        #侦听80端口
        listen       80;

        #定义使用www.xx.com访问
        server_name  www.helloworld.com;

        #对所有请求进行负载均衡请求
        location / {
            root        /root;                 #定义服务器的默认网站根目录位置
            index       index.html index.htm;  #定义首页索引文件的名称
            proxy_pass  http://load_balance_server ;#请求转向load_balance_server 定义的服务器列表

            #以下是一些反向代理的配置(可选择性配置)
            #proxy_redirect off;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            #后端的Web服务器可以通过X-Forwarded-For获取用户真实IP
            proxy_set_header X-Forwarded-For $remote_addr;
            proxy_connect_timeout 90;          #nginx跟后端服务器连接超时时间(代理连接超时)
            proxy_send_timeout 90;             #后端服务器数据回传时间(代理发送超时)
            proxy_read_timeout 90;             #连接成功后，后端服务器响应时间(代理接收超时)
            proxy_buffer_size 4k;              #设置代理服务器(nginx)保存用户头信息的缓冲区大小
            proxy_buffers 4 32k;               #proxy_buffers缓冲区，网页平均在32k以下的话，这样设置
            proxy_busy_buffers_size 64k;       #高负荷下缓冲大小(proxy_buffers*2)
            proxy_temp_file_write_size 64k;    #设定缓存文件夹大小，大于这个值，将从upstream服务器传

            client_max_body_size 10m;          #允许客户端请求的最大单文件字节数
            client_body_buffer_size 128k;      #缓冲区代理缓冲用户端请求的最大字节数
        }
    }
}
```

## 后端服务应用健康检测
<!-- 

https://mp.weixin.qq.com/s?__biz=MzA4MTk3MjI0Mw==&mid=2247489170&idx=1&sn=7c26cc731d132cb9e4d1826fb7a96484&chksm=9f8d8beea8fa02f814eb7520deb698c7fbfa4db4a069f78738da6ca32ae33a731fff794ee50c&scene=178&cur_album_id=1474377728786300933#rd
-->
