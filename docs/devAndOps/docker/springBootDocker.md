
<!-- TOC -->

- [1. Docker部署SpringBoot](#1-docker部署springboot)
    - [1.1. 方式一：Dockerfile文件](#11-方式一dockerfile文件)
        - [1.1.1. 编写Dockerfile](#111-编写dockerfile)
        - [1.1.2. 制作镜像](#112-制作镜像)
        - [1.1.3. 启动容器](#113-启动容器)
        - [1.1.4. 踩炕](#114-踩炕)
    - [1.2. 方式二：maven插件](#12-方式二maven插件)

<!-- /TOC -->

# 1. Docker部署SpringBoot
<!-- 

http://www.3qphp.com/java/framework/3717.html
https://zhuanlan.zhihu.com/p/534014249
-->


## 1.1. 方式一：Dockerfile文件  
### 1.1.1. 编写Dockerfile  

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



