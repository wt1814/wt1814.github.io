<!-- TOC -->

- [1. Nginx搭建](#1-nginx搭建)
    - [1.1. Ngingx操作](#11-ngingx操作)
        - [1.1.1. Nginx搭建](#111-nginx搭建)
        - [1.1.2. Nginx启动](#112-nginx启动)
        - [1.1.3. Nginx日志查看](#113-nginx日志查看)
    - [1.2. Nginx使用](#12-nginx使用)
        - [1.2.1. 前端](#121-前端)
            - [1.2.1.1. 搭建图片服务器/静态服务器](#1211-搭建图片服务器静态服务器)
            - [1.2.1.2. HTTP服务器(部署前端)](#1212-http服务器部署前端)
                - [1.2.1.2.1. nginx的index选项设置默认网页](#12121-nginx的index选项设置默认网页)
            - [1.2.1.3. 跨域](#1213-跨域)
            - [1.2.1.4. ***配置域名和https](#1214-配置域名和https)
            - [1.2.1.5. 缓存](#1215-缓存)
            - [1.2.1.6. Gzip压缩](#1216-gzip压缩)
        - [1.2.2. 反向代理和负载均衡](#122-反向代理和负载均衡)

<!-- /TOC -->


# 1. Nginx搭建  
<!-- 
基于Docker实现nginx+keepalived实现web高可用web系统集群视频教程
https://mp.weixin.qq.com/s/UaGeAXiTqiG_wstxke5N4Q
Nginx 高可用集群解决方案 Nginx + Keepalived
https://mp.weixin.qq.com/s/0OqutgVQuBiCcjuZNL8vFw
-->

## 1.1. Ngingx操作
### 1.1.1. Nginx搭建  
<!-- 
https://blog.csdn.net/weixin_43424481/article/details/124236742

-->
&emsp; ./configure --prefix=/usr/local/nginx。 **安装后的目录为/usr/local/nginx。**  
&emsp; 注：nginx的安装目录和解压目录非一个目录，并且 **都有conf配置**。    


### 1.1.2. Nginx启动  
<!-- 

Linux中nginx如何重启、启动与停止/设置开机自启动
https://blog.csdn.net/qq_39715000/article/details/119919823
-->
1. 启动： ​​​​​​​/usr/local/nginx/sbin/nginx -c /usr/local/nginx/conf/nginx.conf    

    cd /usr/local/nginx/sbin
    ./nginx

    查看是否启动成功命令：ps -ef | grep nginx

2. 重启  

    ./nginx -s reload  
 

### 1.1.3. Nginx日志查看  
&emsp; 日志目录：/usr/local/nginx/logs  

## 1.2. Nginx使用  

### 1.2.1. 前端
#### 1.2.1.1. 搭建图片服务器/静态服务器
<!-- 

nginx搭建图片服务器的过程详解(root和alias的区别)
https://www.yingsoo.com/news/servers/45786.html
Nginx中alias与root的区别
https://blog.csdn.net/zouyang920/article/details/122863369
-->

```text
server {
	listen       8081;
	server_name  localhost;
	
	location / {
		root   html;
		index  index.html index.htm;
	}

   location = /favicon.ico {
	   log_not_found off;
	   access_log off;
   }

   # 搭建图片服务器  
   location  /img/ {
		alias  /usr/work/workspace/pic-host/images/;
		autoindex on;
	}

	error_page   500 502 503 504  /50x.html;
	location = /50x.html {
		root   html;
	}

}
```

访问：http://ip:8081/img/ES/es-20.png  

#### 1.2.1.2. HTTP服务器(部署前端)  

```text
location /pc {
    alias   /usr/share/nginx/html/dev/6noble-pc-customer;
        index  index.html index.htm;
}
```

```text
location / {  
    root   html；                   # 站点的根目录，相当于Nginx的安装目录
    index  index.html index.htm；           # 默认的首页文件，多个用空格分开
} 
```

##### 1.2.1.2.1. nginx的index选项设置默认网页
<!-- 
nginx的index选项设置默认网页
https://blog.csdn.net/m0_47174483/article/details/109056036
-->

#### 1.2.1.3. 跨域
<!-- 
为什么会出现跨域？
关于跨域
https://baijiahao.baidu.com/s?id=1725513524016226208&wfr=spider&for=pc

前后端分离了，部署方式可以分开部署。前端和后端跑在不同的服务器，访问地址肯定不同。就会产生跨域。
也可以部署在一起，前端的HTML页面，也通过后端的服务器进行渲染，访问才用相对路径 / ，不会产生跨域。
-->
<!-- 

https://www.cnblogs.com/shanhubei/p/16918852.html
https://blog.csdn.net/CrazyQiQi/article/details/126137047
-->
```text
location /resources/{            
    add_header Access-control-allow-origin *;
    alias /home/work/data/resources_dev/;
}
```


#### 1.2.1.4. ***配置域名和https
<!-- 
在阿里云域名https配置(nginx为例)
http://t.zoukankan.com/leungUwah-p-10344178.html

Linux实例如何在Web服务中绑定域名
https://help.aliyun.com/document_detail/41091.html?spm=5176.22414175.sslink.1.ce5772a7qD4WLD

Nginx设置HTTPS的方法步骤
https://jiuaidu.com/jianzhan/666939/
-->



#### 1.2.1.5. 缓存  


#### 1.2.1.6. Gzip压缩  


### 1.2.2. 反向代理和负载均衡  



