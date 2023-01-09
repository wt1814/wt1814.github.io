<!-- TOC -->

- [1. Nginx搭建](#1-nginx搭建)
    - [1.1. Nginx搭建](#11-nginx搭建)
    - [1.2. Nginx启动](#12-nginx启动)
    - [1.3. Nginx日志查看](#13-nginx日志查看)
    - [1.4. 搭建图片服务器/静态服务器](#14-搭建图片服务器静态服务器)
    - [1.5. HTTP服务器](#15-http服务器)
    - [1.6. 反向代理](#16-反向代理)
    - [1.7. 负载均衡](#17-负载均衡)
    - [1.8. 动静分离](#18-动静分离)

<!-- /TOC -->


# 1. Nginx搭建  
<!-- 

基于Docker实现nginx+keepalived实现web高可用web系统集群视频教程
https://mp.weixin.qq.com/s/UaGeAXiTqiG_wstxke5N4Q

Nginx 高可用集群解决方案 Nginx + Keepalived
https://mp.weixin.qq.com/s/0OqutgVQuBiCcjuZNL8vFw
-->


## 1.1. Nginx搭建  
<!-- 
https://blog.csdn.net/weixin_43424481/article/details/124236742

-->
&emsp; ./configure --prefix=/usr/local/nginx 。 **安装后的目录为/usr/local/nginx。**  
&emsp; 注：nginx的安装目录和解压目录非一个目录，并且 **都有conf配置**。    


## 1.2. Nginx启动  
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
 

## 1.3. Nginx日志查看  
&emsp; 日志目录：/usr/local/nginx/logs  


## 1.4. 搭建图片服务器/静态服务器
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

## 1.5. HTTP服务器


## 1.6. 反向代理

## 1.7. 负载均衡 


## 1.8. 动静分离  

