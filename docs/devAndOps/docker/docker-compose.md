

<!-- TOC -->

- [1. docker-compose](#1-docker-compose)
    - [1.1. 是什么？](#11-是什么)
    - [1.2. 二进制包安装](#12-二进制包安装)
    - [1.3. 基础操作](#13-基础操作)

<!-- /TOC -->

# 1. docker-compose  

## 1.1. 是什么？  
<!-- 

容器技术Docker、Docker-Compose、k8s的演变
https://www.jianshu.com/p/63feae362a8c

应该主动学习Docker-Compose还是k8s
https://blog.csdn.net/weixin_40814247/article/details/102942360

-->


## 1.2. 二进制包安装
https://www.cnblogs.com/zccoming/p/15424237.html     
二进制包docker-compose-Linux-x86_64下载：  https://github.com/docker/compose/releases

linux安装docker-compose
https://www.cnblogs.com/demoduan/p/15434532.html


## 1.3. 基础操作

<!-- 
docker-compose命令通过指定文件运行
https://blog.csdn.net/weixin_40959890/article/details/125524383

-->

启动服务
docker-compose -f docker-compose_wws.yml up -d

停止服务
docker-compose -f docker-compose_wws.yml stop

停止并删除服务(可能会切断容器间的网络)
docker-compose -f docker-compose_jenkins.yml down

