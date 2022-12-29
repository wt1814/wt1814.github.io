

<!-- TOC -->

- [1. Dockerfile搭建Jenkins](#1-dockerfile搭建jenkins)

<!-- /TOC -->


# 1. Dockerfile搭建Jenkins  
<!-- 
XXX docker使用dockerFile自定义Jenkins
使用docker来启动jenkins才发现里面有一大堆坑，每次都要安装maven、jdk太麻烦。于是写了个dockerfile，一键生成装有maven、jdk1.8、jenkins的镜像。  
https://blog.csdn.net/qq_35031494/article/details/125426380  文档中dockerfile文件maven目录有问题  
按照这份文档配置jdk和maven时， cd /etc -> cat profile 查看系统环境变量
查看数据卷信息 https://blog.csdn.net/m0_64284147/article/details/126571316

docker run -d \
    -p 8888:8080 \
    -p 50000:50000 \
    -v /usr/work/dockerMount/jenkins:/var/jenkins_home \
    -v /etc/localtime:/etc/localtime \
    --restart=always \
    --name=jenkins \
    jenkins/jenkins


启动成功后，无法下载插件
访问时出现无法访问，点击叉号  
1. hudson.model.UpdateCenter.xml文件位于容器内 ./var/jenkins_home/hudson.model.UpdateCenter.xml
2. 或者直接跳过插件下载步骤， admin的密码为首次登录密码

/var/jenkins_home已经挂载到/data/jenkins/
-->

&emsp; 使用docker来启动jenkins才发现里面有一大堆坑，每次都要安装maven、jdk太麻烦。于是写了个dockerfile，一键生成装有maven、jdk1.8、jenkins的镜像。   

1. DockerFile安装jenkins  

2. 访问：
    1. ip:映射端口
    2. 进入容器docker exec查看密码

2. 配置全局环境  
    1. 进入容器
    2. cd /ect -> cat profile 查看系统环境变量 

3. 编译  
    1. 安装maven插件  
    


