
<!-- TOC -->

- [1. Docker使用教程](#1-docker使用教程)
    - [1.1. Docker安装及使用阿里云Doker镜像加速](#11-docker安装及使用阿里云doker镜像加速)
    - [1.2. 私有仓库搭建](#12-私有仓库搭建)
    - [1.3. Docker守护进程配置](#13-docker守护进程配置)
    - [1.4. Docker命令](#14-docker命令)
        - [1.4.1. 镜像操作命令](#141-镜像操作命令)
        - [1.4.2. 容器操作指令](#142-容器操作指令)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; **<font color = "clime">镜像操作常用命令：pull(获取)、images(查看本地镜像)、inspect(查看镜像详细信息)、rmi(删除镜像)、commit(构建镜像)</font>**  
&emsp; **<font color = "clime">容器操作常用命令：run、start、stop、exec(进入运行的容器)</font>**  

# 1. Docker使用教程  
<!-- 
Docker从入门到干活，看这一篇足矣 
https://mp.weixin.qq.com/s/QNo3pDlzxFjkmu0OEmcq3g
-->

## 1.1. Docker安装及使用阿里云Doker镜像加速  
......
<!-- 
Docker配置阿里云镜像仓库
https://mp.weixin.qq.com/s/qp3BX2oq5dULOEBFt5XTAQ
-->

## 1.2. 私有仓库搭建  
......
<!-- 
 Dockerfile构建镜像、registry私服搭建和阿里云的私服仓库构建
https://mp.weixin.qq.com/s/3Lz9CcgIZXjwtwkPdkkqsA

-->

## 1.3. Docker守护进程配置
<!-- 
bibil
https://docs.docker.com/config/daemon/
-->

## 1.4. Docker命令  
&emsp; Docker一般的使用流程：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-39.png)  

### 1.4.1. 镜像操作命令  
&emsp; **<font color = "clime">镜像操作常用命令：pull(获取)、images(查看本地镜像)、inspect(查看镜像详细信息)、rmi(删除镜像)、commit(构建镜像)</font>**  

* 获取镜像：  
&emsp; docker pull centos (默认获取centos最新的镜像)  
&emsp; docker pull centos:7 (获取指定标签镜像)
* 查看本地镜像：  
&emsp; docker images  
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

![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-8.png)  

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

### 1.4.2. 容器操作指令
&emsp; **<font color = "clime">容器操作常用命令：run、start、stop、exec</font>**  

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

![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-7.png)  
