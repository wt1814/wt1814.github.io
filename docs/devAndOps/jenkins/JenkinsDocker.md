

<!-- TOC -->

- [1. Jenkins和Docker](#1-jenkins和docker)
    - [1.1. docker-compose搭建Jenkins](#11-docker-compose搭建jenkins)
    - [1.2. jenkins+docker自动化部署SpringBoot项目](#12-jenkinsdocker自动化部署springboot项目)

<!-- /TOC -->


# 1. Jenkins和Docker

<!-- 



jenkins流水线+sonar检查多模块maven项目
https://www.jianshu.com/p/1a4b8bdf12f8
Jenkins流水线--部署多模块maven项目（推荐）
https://www.jianshu.com/p/600b9601820f




-->

## 1.1. docker-compose搭建Jenkins  
<!-- 

**** docker-compose安装jenkins
https://www.jianshu.com/p/42e2771dcc94

XXX docker使用dockerFile自定义Jenkins
使用docker来启动jenkins才发现里面有一大堆坑，每次都要安装maven、jdk太麻烦。于是写了个dockerfile，一键生成装有maven、jdk1.8、jenkins的镜像。  
https://blog.csdn.net/qq_35031494/article/details/125426380

docker安装jenkins
https://blog.csdn.net/aiwangtingyun/article/details/123523669
访问时出现无法访问，点击叉号  

docker run -d \
    -p 8888:8080 \
    -p 50000:50000 \
    -v /usr/work/dockerMount/jenkins:/var/jenkins_home \
    -v /etc/localtime:/etc/localtime \
    --restart=always \
    --name=jenkins \
    jenkins/jenkins

-->


&emsp; 使用docker来启动jenkins才发现里面有一大堆坑，每次都要安装maven、jdk太麻烦。于是写了个dockerfile，一键生成装有maven、jdk1.8、jenkins的镜像。   

1. docker-compose安装jenkins  

2. 访问：
    1. ip:映射端口
    2. 进入容器docker exec查看密码

2. 配置全局环境  


3. 编译  


## 1.2. jenkins+docker自动化部署SpringBoot项目  
<!-- 

https://blog.csdn.net/shayopron/article/details/121167066


Jenkins +Docker+Git 实现自动部署
https://www.cnblogs.com/seanRay/p/15126859.html

3. 编译
Jenkins+Docker 一键自动化部署 SpringBoot 项目 
https://mp.weixin.qq.com/s/C7o0SDNW-rajE0FywGGbTQ

云服务器中安装docker+jenkins部署接口自动化测试(java)
https://blog.csdn.net/m0_50026910/article/details/124114199
-->

