

<!-- TOC -->

- [1.1. Docker搭建Jenkins](#11-docker搭建jenkins)
- [1.2. jenkins+docker自动化部署SpringBoot项目](#12-jenkinsdocker自动化部署springboot项目)

<!-- /TOC -->

<!-- 
**** docker安装jenkins
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


Jenkins +Docker+Git 实现自动部署
https://www.cnblogs.com/seanRay/p/15126859.html

3. 编译
Jenkins+Docker 一键自动化部署 SpringBoot 项目 
https://mp.weixin.qq.com/s/C7o0SDNW-rajE0FywGGbTQ

云服务器中安装docker+jenkins部署接口自动化测试(java)
https://blog.csdn.net/m0_50026910/article/details/124114199


docker-compose安装jenkins
https://www.jianshu.com/p/42e2771dcc94
jenkins流水线+sonar检查多模块maven项目
https://www.jianshu.com/p/1a4b8bdf12f8
Jenkins流水线--部署多模块maven项目（推荐）
https://www.jianshu.com/p/600b9601820f

-->



<!-- 
********  
1. 搭建 2. 配置全局环境
docker使用dockerFile自定义Jenkins
使用docker来启动jenkins才发现里面有一大堆坑，每次都要安装maven、jdk太麻烦。于是写了个dockerfile，一键生成装有maven、jdk1.8、jenkins的镜像。  
https://blog.csdn.net/qq_35031494/article/details/125426380

-->

## 1.1. Docker搭建Jenkins  
&emsp; 使用docker来启动jenkins才发现里面有一大堆坑，每次都要安装maven、jdk太麻烦。于是写了个dockerfile，一键生成装有maven、jdk1.8、jenkins的镜像。   

1. 搭建  


2. 配置全局环境  


3. 编译  


## 1.2. jenkins+docker自动化部署SpringBoot项目  
<!-- 

https://blog.csdn.net/shayopron/article/details/121167066
-->

