
<!-- TOC -->

- [1. Nexus3搭建](#1-nexus3搭建)
    - [1.1. 安装](#11-安装)
    - [1.2. 使用](#12-使用)
    - [1.3. maven配置仓库地址](#13-maven配置仓库地址)

<!-- /TOC -->

# 1. Nexus3搭建 
<!-- 
下载地址：http://www.sonatype.org/nexus/archived/

https://blog.csdn.net/qq_43100406/article/details/97156661

*** https://blog.csdn.net/qq_43100406/article/details/97156661
*** https://blog.csdn.net/blackoon88/article/details/124404129

https://mp.weixin.qq.com/mp/appmsgalbum?action=getalbum&__biz=MzA4MTk3MjI0Mw==&scene=1&album_id=2415325568729841667&count=3#wechat_redirect

-->



## 1.1. 安装
1. 下载
2. 配置  
3. 安装  
4. **启动：**  

    cd /usr/local/nexus-3.31.1-01/bin
    ./nexus start 或者./nexus run

## 1.2. 使用  

1. 访问：ip:8081  
2. 修改admin用户密码，需要重启nexus，使用admin登录。    


## 1.3. maven配置仓库地址  
1. setting.xml中配置  


2. 项目pom.xml中配置  

