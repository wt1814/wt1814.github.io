<!-- TOC -->

- [1. Nginx搭建](#1-nginx搭建)
    - [1.1. Nginx搭建](#11-nginx搭建)
    - [1.2. Nginx重启](#12-nginx重启)
    - [1.3. Nginx日志查看](#13-nginx日志查看)
    - [1.4. 搭建图片服务器](#14-搭建图片服务器)

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


## 1.2. Nginx重启  
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


## 1.4. 搭建图片服务器
<!-- 

nginx搭建图片服务器的过程详解(root和alias的区别)
https://www.yingsoo.com/news/servers/45786.html
Nginx中alias与root的区别
https://blog.csdn.net/zouyang920/article/details/122863369
-->

