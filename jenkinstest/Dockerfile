#基础镜像
FROM java:8
#镜像作者
MAINTAINER wangtao1814@163.com
#将项目 jar包 加入并且重命名
ADD target/jenkinstest.jar app.jar
#启动工程
ENTRYPOINT ["java","-jar","/app.jar","--httpPort=8089"]
#暴露端口
EXPOSE 8089


# 注意Dockerfile放到根目录下，否则【ADD target/jenkinstest.jar app.jar】问题不好解决

# 构建镜像，进入Dockerfile目录  docker build -t jenkinstest .

# 运行容器 docker run -d -p 8089:8089 jenkinstest

