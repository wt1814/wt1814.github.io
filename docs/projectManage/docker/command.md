
<!-- TOC -->

- [1. Docker使用教程](#1-docker使用教程)
    - [1.1. Docker安装及使用阿里云Doker镜像加速](#11-docker安装及使用阿里云doker镜像加速)
    - [1.2. Docker命令](#12-docker命令)
        - [1.2.1. 镜像操作命令](#121-镜像操作命令)
        - [1.2.2. 容器操作指令](#122-容器操作指令)
    - [1.3. Docker部署](#13-docker部署)
    - [1.4. idea中docker插件](#14-idea中docker插件)

<!-- /TOC -->

# 1. Docker使用教程  

## 1.1. Docker安装及使用阿里云Doker镜像加速  
......

## 1.2. Docker命令  


### 1.2.1. 镜像操作命令  
&emsp; **<font color = "red">镜像操作常用命令：pull、images、inspect、rmi</font>**  

* 获取镜像：  
&emsp; docker pull centos    (默认获取centos最新的镜像)
&emsp; docker pull centos:7 (获取指定标签镜像)
* 查看本地镜像：
&emsp; docker images
* 查看镜像详细信息：
&emsp; docker inspect centos:7 
* 查看镜像历史：
&emsp; docker history centos:7
* 删除镜像：
&emsp; A:使用标签删除：docker rmi centos
&emsp; B:使用ID删除：docker rimi
* 构建镜像：  
&emsp; A:使用docker commit命令
&emsp; B:使用Dockerfile构建

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

### 1.2.2. 容器操作指令
&emsp; **<font color = "red">容器操作常用命令：run、start、stop、exec</font>**  

* 创建启动容器：  

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
        例：  
        [root@localhost ~]# docker start 802e3623e566

* 停止运行的容器：  

        docker stop 容器ID
        例：
        [root@localhost ~]# docker stop 802e3623e566

* 删除容器：  

        [root@localhost ~]# docker stop 89566e38c7fb
        [root@localhost ~]# docker rm 89566e38c7fb

* 进入运行的容器：  

        [root@localhost ~]# docker exec -it cbd8b1f35dcc /bin/bash

* 导出容器：  

        导出容器cbd8b1f35dcc到centos_test.tar文件
        [root@localhost ~]# docker export -o centos_test.tar cbd8b1f35dcc
        导出的tar文件可以在其他机器上，通过导入来重新运行  

* 导入容器：

        把导出的文件centos_test.tar通过docker import导入变成镜像
        [root@localhost ~]# docker import centos_test.tar test/centos
        通过docker images命令可以看到增加了个test/centos镜像

## 1.3. Docker部署  
......


## 1.4. idea中docker插件
......
<!-- 
https://mp.weixin.qq.com/s?__biz=MzAxNDMwMTMwMw==&mid=2247492037&idx=1&sn=5568994f8c801f56170b14f2d21df31c&chksm=9b97c0ddace049cb57d4396eabc3965532eb128ab83a55d3c8d7dea3d8be675dc8c0c8ebfba3&mpshare=1&scene=1&srcid=&sharer_sharetime=1565841354339&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=a98b434d6faae616ed91d3ea273cb1b2030141b502c3589cff178a48e66b895a407b58f1d6b6ffafcf8c3ced4828833e6652a8869d6d35edddf1f192fe618738afaaa152a55d00024b42ce09a67b0b99&ascene=1&uin=MTE1MTYxNzY2MQ==&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=SNTjToR4G4GRQcv6vbTgQCeljdugS8QdOBuRNyGrRTrVOdRDMoEgnHo3VlytJ0fv

使用 Docker 部署 Spring Boot 项目
https://mp.weixin.qq.com/s?__biz=MzI4NDY5Mjc1Mg==&mid=2247489662&idx=2&sn=8227bfd4b0b68ddc002dfe451a661688&chksm=ebf6c001dc8149172341b190c9b72700fed794261d67b8648edd2a596019212a742c7b241b90&mpshare=1&scene=1&srcid=&sharer_sharetime=1572833030053&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=6f23511bf9e1c01fc8b70c3e81dbcf33c57a5d08ea0ef51caa9a619fffb3e59ba744ac23ec082bfc8791797c1917e1e4f0290dff6475d1b71f64d8252bf92952180c025b0121995474ac59fe778892a9&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=Lu%2FLBuTxuGaOTLq0CL9dO0ss3p9k%2BNlDhrOCgfGfCUsKTPyuc12lccq3vmkXvxfb

-->

