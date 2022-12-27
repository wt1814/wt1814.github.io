


# docker-compose搭建Jenkins  


<!-- 

**** docker-compose安装jenkins
https://www.jianshu.com/p/42e2771dcc94

docker-compose命令通过指定文件运行
https://blog.csdn.net/weixin_40959890/article/details/125524383


XXX docker使用dockerFile自定义Jenkins
使用docker来启动jenkins才发现里面有一大堆坑，每次都要安装maven、jdk太麻烦。于是写了个dockerfile，一键生成装有maven、jdk1.8、jenkins的镜像。  
https://blog.csdn.net/qq_35031494/article/details/125426380

docker安装jenkins
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

-->


&emsp; 使用docker来启动jenkins才发现里面有一大堆坑，每次都要安装maven、jdk太麻烦。于是写了个dockerfile，一键生成装有maven、jdk1.8、jenkins的镜像。   

1. docker-compose安装jenkins  

2. 访问：
    1. ip:映射端口
    2. 进入容器docker exec查看密码

2. 配置全局环境  


3. 编译  

