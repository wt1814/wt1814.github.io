
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


一文学会Maven私服的搭建
https://mp.weixin.qq.com/s?__biz=MzAxODcyNjEzNQ==&mid=2247488309&idx=2&sn=4ad5f2c7d27d823d0abcfce3b07082e0&chksm=9bd0beadaca737bbd1bab88f9b6fc169b24c634514c4174b0df9d102fcce5d3b2c33f2868bf2&mpshare=1&scene=1&srcid=&sharer_sharetime=1567989584402&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=2a4ff15fdd846346f881eb5c43aea593b2d0c447204596c6fe86adcd93c23ca17edf4437b9fa9b163db57756e777513e8f296542caccca4f26d9cea9e65ef0f28be15bb7b930a3bcf497b2035f7052d3&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=5RLB7cEjWtQ4V9twg%2FeYkye%2B%2Bz6%2FeAWlIuRf1Ac7hDK%2FajceQqYc2ZVspQ9y8Xh%2F

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

