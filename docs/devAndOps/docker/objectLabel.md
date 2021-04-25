
<!-- TOC -->

- [1. 对象标签](#1-对象标签)
    - [1.1. 管理对象上的标签](#11-管理对象上的标签)
    - [1.2. 镜像标签使用](#12-镜像标签使用)
        - [1.2.1. tag的生成](#121-tag的生成)
        - [1.2.2. 运行镜像](#122-运行镜像)
        - [1.2.3. docker镜像的版本控制](#123-docker镜像的版本控制)

<!-- /TOC -->


# 1. 对象标签  
<!-- 
将自定义元数据应用对象
https://docs.docker.com/config/labels-custom-metadata/

Docker对象标签
https://docs.docker.com/config/labels-custom-metadata/
Docker对象标签(将自定义元数据应用于对象)
-->

## 1.1. 管理对象上的标签
&emsp; 支持标签的每种类型的对象都具有添加和管理标签的机制，并可以在该类型对象上使用它们。这些链接提供了一个开始学习如何在 Docker 部署中使用标签的好地方。  
&emsp; 镜像、容器、本地守护进程、卷和网络上的标签在对象的生命周期内是静态的。必须要重新创建对象才能改变这些标签。swarm节点和服务上的标签可以动态更新。  


## 1.2. 镜像标签使用  
<!-- 
https://blog.csdn.net/yangshangwei/article/details/52799675
-->

&emsp; git的tag功能是为了将代码的某个状态打上一个戳，通过tag可以很轻易的找到对应的提交。  
&emsp; docker的tag更加灵活，docker将文件等信息的变动抽象为一次次的commit，每一次commit以后可能走向不同的分支，当完成dockerfile的构建后，会生成一串无规则的字符串代表此次生成的ID，此时，tag的作用就是为他创建一个友好的NAME，方便对镜像库的管理。  

### 1.2.1. tag的生成  


### 1.2.2. 运行镜像  


### 1.2.3. docker镜像的版本控制  
&emsp; 如果需要升级某个docker镜像，可以这样做。  

1. 给每个新生成的镜像都打上相应版本的tag。此时可能存在image:latest、image:v1、image:v2等。  
2. 如果要从v1升级到v2，首先将导入的v2镜像强制重命名为image:latest，命令为docker tag -f image:v2 image:latest  
3. docker stop之前正在运行的容器  
4. 启用docker run image，此时image的等价镜像image:latest就是最新的V2镜像。  

&emsp; 总结下步骤：load/tag/stop/run  
