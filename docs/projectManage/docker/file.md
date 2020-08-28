

# DockerFile

<!-- 
https://mp.weixin.qq.com/s?__biz=MzU0MzQ5MDA0Mw==&mid=2247486479&idx=3&sn=c1fbb2084fb251242d28232a199dcc32&chksm=fb0be69bcc7c6f8d43cacac86fc72b2c48d0264cdff59f992b1bdd3f34847283c596efdd524e&mpshare=1&scene=1&srcid=&sharer_sharetime=1564706539160&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=dd204f3b2a2710ede816adfd8719abab5e616cb2bfaa745f0c27e1497097ef87868dd4e09b2f4cf7f9356029742e5eca3f090d149bf596e7cdde74af20c9a2075fda7b37e40c5fbc75534666bf2183f9&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060834&lang=zh_CN&pass_ticket=J89DTwjzapl6QMdBj7AAiEYLyOJjEXJXaq6zx%2Fd594ed2uDLQjTlRiDqWumTTR0m

https://mp.weixin.qq.com/s/2poLYm-MgAEJxCYiRZDnQw

Dockerfile使用详解以及CMD、ENTRYPOINT的区别
https://mp.weixin.qq.com/s/sVnO59GEMomZYBlBGUJJTQ
面试官：你说你精通 Docker，那你来详细说说 Dockerfile 吧
https://mp.weixin.qq.com/s/gli_JAXRWMfZgUWZWXu8UQ
如何编写最佳的Dockerfile
https://mp.weixin.qq.com/s/D8aH9e85ym034e4qZSLOBQ
-->

## 使用docker commit

## 使用Dockerfile构建

<!-- 
http://www.imooc.com/article/277891
-->

&emsp; Dockerfile是一个包含用于组合镜像的命令的文本文档。  
&emsp; Docker通过读取Dockerfile中的指令按步自动生成镜像。  
&emsp; docker build -t 机构/镜像名<:tags> Dockerfile目录。   

```text
FROM scratch #制作base image 基础镜像，尽量使用官方的image作为base image
FROM centos #使用base image
FROM ubuntu:14.04 #带有tag的base image

LABEL version=“1.0” #容器元信息，帮助信息，Metadata，类似于代码注释
LABEL maintainer=“yc_uuu@163.com"

#对于复杂的RUN命令，避免无用的分层，多条命令用反斜线换行，合成一条命令！
RUN yum update && yum install -y vim 
    Python-dev #反斜线换行
RUN /bin/bash -c "source $HOME/.bashrc;echo $HOME”

WORKDIR /root #相当于linux的cd命令，改变目录，尽量使用绝对路径！！！不要用RUN cd
WORKDIR /test # 如果没有就自动创建
WORKDIR demo # 再进入demo文件夹
RUN pwd     # 打印结果应该是/test/demo

ADD and COPY 
ADD hello /  # 把本地文件添加到镜像中，吧本地的hello可执行文件拷贝到镜像的/目录
ADD test.tar.gz /  # 添加到根目录并解压

WORKDIR /root
ADD hello test/  # 进入/root/ 添加hello可执行命令到test目录下，也就是/root/test/hello 一个绝对路径
COPY hello test/  # 等同于上述ADD效果

ADD与COPY
   - 优先使用COPY命令
    -ADD除了COPY功能还有解压功能
添加远程文件/目录使用curl或wget

ENV # 环境变量，尽可能使用ENV增加可维护性
ENV MYSQL_VERSION 5.6 # 设置一个mysql常量
RUN yum install -y mysql-server=“${MYSQL_VERSION}” 
```

进阶知识(了解)  
```text
VOLUME and EXPOSE 
存储和网络

RUN and CMD and ENTRYPOINT
RUN：执行命令并创建新的Image Layer
CMD：设置容器启动后默认执行的命令和参数
ENTRYPOINT：设置容器启动时运行的命令

Shell格式和Exec格式
RUN yum install -y vim
CMD echo ”hello docker”
ENTRYPOINT echo “hello docker”

Exec格式
RUN [“apt-get”,”install”,”-y”,”vim”]
CMD [“/bin/echo”,”hello docker”]
ENTRYPOINT [“/bin/echo”,”hello docker”]


通过shell格式去运行命令，会读取$name指令，而exec格式是仅仅的执行一个命令，而不是shell指令
cat Dockerfile
    FROM centos
    ENV name Docker
    ENTRYPOINT [“/bin/echo”,”hello $name”]#这个仅仅是执行echo命令，读取不了shell变量
    ENTRYPOINT  [“/bin/bash”,”-c”,”echo hello $name"]

CMD
容器启动时默认执行的命令
如果docker run指定了其他命令(docker run -it [image] /bin/bash )，CMD命令被忽略
如果定义多个CMD，只有最后一个执行

ENTRYPOINT
让容器以应用程序或服务形式运行
不会被忽略，一定会执行
最佳实践：写一个shell脚本作为entrypoint
COPY docker-entrypoint.sh /usr/local/bin
ENTRYPOINT [“docker-entrypoint.sh]
EXPOSE 27017
CMD [“mongod”]

[root@master home]# more Dockerfile
FROm centos
ENV name Docker
#CMD ["/bin/bash","-c","echo hello $name"]
ENTRYPOINT ["/bin/bash","-c","echo hello $name”]
```



