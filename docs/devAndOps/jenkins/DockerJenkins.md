

<!-- TOC -->

- [1. Dockerfile搭建Jenkins](#1-dockerfile搭建jenkins)
    - [1.1. Dockerfile搭建Jenkins](#11-dockerfile搭建jenkins)
    - [1.2. Dockerfile使用jenkins.war包构建jenkins](#12-dockerfile使用jenkinswar包构建jenkins)
    - [1.3. 使用rpm包安装jenkins](#13-使用rpm包安装jenkins)
    - [1.4. 小结](#14-小结)

<!-- /TOC -->


# 1. Dockerfile搭建Jenkins  

## 1.1. Dockerfile搭建Jenkins
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


<!-- 
jenkins官网：https://www.jenkinschina.com/doc/book/installing/  
建议使用的Docker映像是jenkinsci/blueocean image(来自 the Docker Hub repository)。  

-->

&emsp; 使用docker来启动jenkins才发现里面有一大堆坑，每次都要安装maven、jdk太麻烦。于是写了个dockerfile，一键生成装有maven、jdk1.8、jenkins的镜像。   

1. DockerFile安装jenkins  

2. 访问：
    1. ip:映射端口
    2. 进入容器docker exec查看密码

2. 配置全局环境jdk和maven  
    1. 进入容器
    2. cd /ect -> cat profile 查看系统环境变量 

3. 安装插件  
    1. 安装maven插件Maven Integration
    2. Publish Over SSH      


## 1.2. Dockerfile使用jenkins.war包构建jenkins

<!-- 

用最新jenkins.war包 构建jenkins
https://blog.csdn.net/whh18254122507/article/details/81783430
https://www.cnblogs.com/namedgx/p/15420711.html
https://www.bbsmax.com/A/gVdnMXBN5W/


dockerfile构建jenkins流水线
https://blog.csdn.net/weixin_44663310/article/details/125565946

-->


## 1.3. 使用rpm包安装jenkins
<!-- 

https://blog.csdn.net/weixin_43895083/article/details/127226397
-->


## 1.4. 小结
<!-- 
根据下面文章自己写Dockerfile

https://blog.csdn.net/qq_35031494/article/details/125426380 
https://blog.csdn.net/weixin_43895083/article/details/127226397

shell脚本修改文件
https://blog.51cto.com/u_12660945/5161654
-->


```text
FROM  centos:centos7.9.2009

MAINTAINER wangtap1814@163.com

# wget和建目录
RUN yum install wget lrzsz -y
RUN yum install -y git 
RUN mkdir -p /usr/local/java
RUN mkdir -p /usr/local/maven
RUN mkdir -p /usr/local/jenkins

# 安装jdk
RUN cd /usr/local/java && wget https://repo.huaweicloud.com/java/jdk/8u201-b09/jdk-8u201-linux-x64.tar.gz && tar -zxvf jdk-8u201-linux-x64.tar.gz
# 配置jdk环境
RUN echo 'JAVA_HOME=/usr/local/java/jdk1.8.0_201'>>/etc/profile &&  echo 'CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/'>>/etc/profile &&  echo 'PATH=$PATH:$JAVA_HOME/bin'>>/etc/profile &&  echo 'PATH=$PATH:$JAVA_HOME/bin'>>/etc/profile &&  echo 'export PATH JAVA_HOME CLASSPATH'>>/etc/profile
#
RUN /bin/bash -c "source /etc/profile"

# 安装maven
RUN cd /usr/local/maven && wget https://mirrors.aliyun.com/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz && tar -zxvf apache-maven-3.6.3-bin.tar.gz
# 配置maven环境变量
RUN echo "export MAVEN_HOME=/usr/local/maven/apache-maven-3.6.3">>/etc/profile && echo 'export PATH=$PATH:$MAVEN_HOME/bin'>>/etc/profile && ln -s /usr/local/maven/apache-maven-3.6.3/bin/mvn /usr/bin/
# 
RUN /bin/bash -c "source /etc/profile"

# 安装openjdk
RUN yum install -y java-1.8.0-openjdk

# 安装jenkins
RUN cd /usr/local/jenkins
RUN wget --no-check-certificate https://mirrors.jenkins.io/war-stable/2.346.3/jenkins.war  
# cmd nohup java -jar jenkins.war --httpPort=8097 &
ENTRYPOINT ["java","-jar","jenkins.war","--httpPort=8097"]

EXPOSE 8097
```