
<!-- TOC -->

- [1. Docker命令](#1-docker命令)
    - [1.1. 镜像操作](#11-镜像操作)
        - [1.1.1. 常用命令](#111-常用命令)
        - [1.1.2. Docker镜像有哪些Tag](#112-docker镜像有哪些tag)
    - [1.2. 容器操作指令](#12-容器操作指令)
        - [1.2.1. docker run和docker start的区别](#121-docker-run和docker-start的区别)
        - [1.2.2. docker run命令详解](#122-docker-run命令详解)
    - [1.3. Docker自启动](#13-docker自启动)
        - [1.3.1. docker 服务启动启动](#131-docker-服务启动启动)
        - [1.3.2. docker容器的自启动](#132-docker容器的自启动)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "clime">镜像操作常用命令：pull(获取)、images(查看本地镜像)、inspect(查看镜像详细信息)、rmi(删除镜像)、commit(构建镜像)。</font>**  
2. **<font color = "clime">容器操作常用命令：run(创建并启动)、start(启动已有)、stop、exec(进入运行的容器)。</font>**  


# 1. Docker命令  
&emsp; Docker一般的使用流程：  
![image](http://182.92.69.8:8081/img/devops/docker/docker-39.png)  

## 1.1. 镜像操作
### 1.1.1. 常用命令
&emsp; **<font color = "clime">镜像操作常用命令：pull(获取)、images(查看本地镜像)、inspect(查看镜像详细信息)、rmi(删除镜像)、commit(构建镜像)。</font>**  

* 获取镜像：  
&emsp; docker pull centos (默认获取centos最新的镜像)  
&emsp; docker pull centos:7 (获取指定标签镜像)
* 查看本地镜像：  
&emsp; **docker images**  
* 查看镜像详细信息：  
&emsp; docker inspect centos:7  
* 查看镜像历史：  
&emsp; docker history centos:7  
* 删除镜像：  
    * 使用标签删除：docker rmi centos  
    * 使用ID删除：docker rimi
* 构建镜像：    
    * 使用docker commit命令  
    * 使用Dockerfile构建

![image](http://182.92.69.8:8081/img/devops/docker/docker-8.png)  

<!-- 

* docker pull 镜像名<:tags> - 从远程仓库抽取镜像   
    docker pull centos    (默认获取centos最新的镜像)
    docker pull centos:7 (获取指定标签镜像)
* docker images - 查看本地镜像  
* docker run 镜像名<:tags> - 创建容器，启动应用  
* docker ps - 查看正在运行中的镜像  
* docker rm <-f> 容器id - 删除容器  
* docker rmi <-f> 镜像名:<tags\> - 删除镜像  
-->

### 1.1.2. Docker镜像有哪些Tag
<!-- 

http://www.zztongyun.com/article/docker%20%E6%9F%A5%E7%9C%8B%E9%95%9C%E5%83%8F%E7%9A%84%E8%AF%A6%E7%BB%86%E4%BF%A1%E6%81%AF
-->


## 1.2. 容器操作指令
&emsp; **<font color = "clime">容器操作常用命令：run、start、stop、exec。</font>**  
* 查看有几个容器：  

        docker ps -a 
        
* 创建并启动容器：  

        [root@localhost ~]# docker run centos:7 /bin/echo'hello world'
        容器运行完后直接退出

* 交互形式创建启动容器  

        [root@localhost ~]# docker run -it centos:7 /bin/bash
        [root@802e3623e566 /]# ps
        PID TTY          TIME CMD
            1 ?        00:00:00 bash
            13 ?        00:00:00 ps
        [root@802e3623e566 /]# exit

        执行exit才能退出容器  

* 守护状态运行容器  

        [root@localhost ~]# docker run -d centos:7 /bin/sh -c "while true; do echo hello world; sleep 1; done"
        
* 启动已有的容器：

        docker start 容器ID
        例：[root@localhost ~]# docker start 802e3623e566

* 停止运行的容器：  

        docker stop 容器ID
        例：[root@localhost ~]# docker stop 802e3623e566

* 删除容器：  

        [root@localhost ~]# docker stop 89566e38c7fb
        [root@localhost ~]# docker rm 89566e38c7fb

* **<font color = "red">进入运行的容器：</font>**  

        [root@localhost ~]# docker exec -it cbd8b1f35dcc /bin/bash

* 导出容器：  

        导出容器cbd8b1f35dcc到centos_test.tar文件
        [root@localhost ~]# docker export -o centos_test.tar cbd8b1f35dcc
        导出的tar文件可以在其他机器上，通过导入来重新运行  

* 导入容器：

        把导出的文件centos_test.tar通过docker import导入变成镜像
        [root@localhost ~]# docker import centos_test.tar test/centos
        通过docker images命令可以看到增加了个test/centos镜像

* **进入容器**  

        docker exec -it 775c7c9ee1e1 /bin/bash

* **查看容器实例日志**  

        docker logs -f -t --tail 行数 容器id

![image](http://182.92.69.8:8081/img/devops/docker/docker-7.png)  

### 1.2.1. docker run和docker start的区别
<!-- 
docker run和docker start的区别
https://blog.csdn.net/weixin_44455388/article/details/120947771
-->
1. docker run ：创建一个新的容器并运行一个命令，是将镜像放入容器并启动容器。  
2. docker start :启动一个或多个已经被停止的容器。类似用法的还有docker stop和docker restart。  


### 1.2.2. docker run命令详解  
<!-- 
https://blog.csdn.net/qq_38974638/article/details/121061590
-->
&emsp; docker run命令：-a、-d、-e、-h、-i、-m、-p、-t、-v、--cpuset、--dns、--env-file、--expose、--link、--name、--net  

&emsp; -P：随机端口映射，容器内部端口随机映射到主机的端口。




## 1.3. Docker自启动  
<!-- 
Docker 自动启动和容器自动启动
https://blog.csdn.net/m0_67392661/article/details/123732115
-->

### 1.3.1. docker 服务启动启动

```text
# 开启 docker 自启动
systemctl enable docker.service

# 关闭 docker 自启动
systemctl disable docker.service
```


### 1.3.2. docker容器的自启动  

```text
# 开启容器自启动
docker update --restart=always 【容器名】
例如：docker update --restart=always tracker


# 关闭容器自启动
docker update --restart=no【容器名】
例如：docker update --restart=no tracker

##### 相关配置解析
no：
    不要自动重启容器。（默认）

on-failure： 
    如果容器由于错误而退出，则重新启动容器，该错误表现为非零退出代码。

always：
    如果容器停止，请务必重启容器。如果手动停止，则仅在Docker守护程序重新启动或手动重新启动容器本身时才重新启动。（参见重启政策详情中列出的第二个项目）

unless-stopped：
    类似于always，除了当容器停止（手动或其他方式）时，即使在Docker守护程序重新启动后也不会重新启动容器。
```
