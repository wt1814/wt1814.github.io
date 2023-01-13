
<!-- TOC -->

- [1. Docker部署SpringBoot](#1-docker部署springboot)
    - [1.1. 方式一：Dockerfile部署Jar包](#11-方式一dockerfile部署jar包)
        - [1.1.1. 编写Dockerfile](#111-编写dockerfile)
        - [1.1.2. 制作镜像](#112-制作镜像)
        - [1.1.3. 启动容器](#113-启动容器)
        - [1.1.4. 踩炕](#114-踩炕)
    - [1.2. 方式二：maven插件](#12-方式二maven插件)
    - [1.3. DockerFile部署war包](#13-dockerfile部署war包)

<!-- /TOC -->

# 1. Docker部署SpringBoot
<!-- 

http://www.3qphp.com/java/framework/3717.html
整体流程： https://zhuanlan.zhihu.com/p/534014249

https://zhuanlan.zhihu.com/p/534014249



Docker：使用dockerFile创建镜像（演示war包和jar包）
https://blog.csdn.net/weixin_60670696/article/details/125442933?spm=1001.2101.3001.6650.11&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-11-125442933-blog-125921262.pc_relevant_recovery_v2&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-11-125442933-blog-125921262.pc_relevant_recovery_v2&utm_relevant_index=15
Docker 部署war包项目
https://blog.51cto.com/u_15127559/4720269


-->

将docker项目包和Dockerfile一起上上传到服务器同一个目录下，进入后构建镜像  


## 1.1. 方式一：Dockerfile部署Jar包    



### 1.1.1. 编写Dockerfile  
**注意Dockerfile放到根目录下，否则【ADD target/jenkinstest.jar app.jar】问题不好解决**  
  

```text
#基础镜像
FROM java:8
EXPOSE 8081
VOLUME /usr/local/work/dockerTemp/wt1814Note
#将项目 springboot-docker.jar 加入并且重命名
ADD /target/wt1814-note-0.0.1-SNAPSHOT.jar wt1814-note.jar
#启动工程，指定端口
ENTRYPOINT ["java","-jar","/wt1814-note.jar","--server.port=8081"]
```


### 1.1.2. 制作镜像  
1. cd Dockerfile目录
2. docker build -t 别名 .
3. 报错：Use ‘docker scan‘ to run Snyk tests against images to find vulnerabilities and learn how to fix th： https://blog.csdn.net/W_317/article/details/127164024  
     

### 1.1.3. 启动容器
1. doker ps查看容器id  
2. docker run -d -p 【外部端口】:【内部端口】 【容器id】


### 1.1.4. 踩炕
&emsp; java -jar命令启动jar包默认8080端口，Dockerfile中指定端口。    

```text
#启动工程，指定端口
ENTRYPOINT ["java","-jar","/wt1814-note.jar","--server.port=8081"]
```

## 1.2. 方式二：maven插件  



## 1.3. DockerFile部署war包   
<!-- 

https://blog.51cto.com/u_15127559/4720269
-->
