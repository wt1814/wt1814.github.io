<!-- TOC -->

- [1. DockerFile](#1-dockerfile)
    - [1.1. 使用docker commit](#11-使用docker-commit)
    - [1.2. Dockerfile详解](#12-dockerfile详解)
        - [1.2.1. FROM命令](#121-from命令)
        - [1.2.2. MAINTAINER指令](#122-maintainer指令)
        - [1.2.3. LABEL指令](#123-label指令)
        - [1.2.4. RUN命令](#124-run命令)
        - [1.2.5. ENV命令](#125-env命令)
        - [1.2.6. ARG指令](#126-arg指令)
        - [1.2.7. VOLUME指令](#127-volume指令)
        - [1.2.8. COPY命令](#128-copy命令)
        - [1.2.9. ADD命令](#129-add命令)
        - [1.2.10. EXPOSE命令](#1210-expose命令)
        - [1.2.11. WORKDIR命令](#1211-workdir命令)
        - [1.2.12. CMD命令](#1212-cmd命令)

<!-- /TOC -->


# 1. DockerFile
<!--
https://mp.weixin.qq.com/s/2poLYm-MgAEJxCYiRZDnQw
Dockerfile使用详解以及CMD、ENTRYPOINT的区别
https://mp.weixin.qq.com/s/sVnO59GEMomZYBlBGUJJTQ
面试官：你说你精通 Docker，那你来详细说说 Dockerfile 吧
https://mp.weixin.qq.com/s/gli_JAXRWMfZgUWZWXu8UQ
如何编写最佳的Dockerfile
http://www.imooc.com/article/277891
-->


## 1.1. 使用docker commit

&emsp; 例：构建一个带有jdk的镜像  

```text
[root@localhost ~]# docker run -it centos:7 /bin/bash
[root@060793baf536 /]# yum install wget
[root@060793baf536 /]# wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u131-b11/d54c1d3a095b4ff2b6607d096fa80163/jdk-8u131-linux-x64.rpm

[root@060793baf536 /]# rpm -ivh jdk-8u131-linux-x64.rpm
Preparing...                          ################################# [100%]
Updating / installing...
   1:jdk1.8.0_131-2000:1.8.0_131-fcs  ################################# [100%]
Unpacking JAR files...
        tools.jar...
        plugin.jar...
        javaws.jar...
        deploy.jar...
        rt.jar...
        jsse.jar...
        charsets.jar...
        localedata.jar...
[root@060793baf536 /]# exit
[root@localhost ~]# docker commit 060793baf536 centos/jdk:2.0
```
&emsp; 通过docker images命令可以看到新增了centos/jdk标签为2.0的镜像  


## 1.2. Dockerfile详解

<!-- 
镜像的定制实际上就是定制每一层所添加的配置、文件。我们可以把每一层修改、安装、构建、操作的命令都写入一个脚本，这个脚本就是Dockerfile。  

Dockerfile是一个文本文件，其内包含了一条条的指令，每一条指令构建一层，因此每一条指令的内容，就是描述该层应当如何构建。 
-->
&emsp; Dockerfile中文名叫镜像描述文件，是一个包含用于组合镜像的命令的文本文档，也可以叫“脚本”。通过读取Dockerfile中的指令安装步骤自动生成镜像。    
&emsp; 通过docker build命令用于从Dockerfile文件构建镜像。docker build -t 机构/镜像名<:tags> Dockerfile目录。   

&emsp; **<font color = "red">组成部分</font>**    
&emsp; **<font color = "lime">Dockerfile一般分为：基础镜像、镜像元信息、镜像操作指令和容器启动时执行指令，# 为 Dockerfile中的注释。</font>**  

|部分|命令|
|---|---|
|基础镜像信息|FROM|
|维护者信息|MAINTAINER|
|镜像操作指令|RUN、COPY、ADD、EXPOSE、WORKDIR、ONBUILD、USER、VOLUME等|
|容器启动时执行指令|CMD、ENTRYPOINT|

![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-9.png)  


### 1.2.1. FROM命令  
&emsp; 定制的镜像都是基于FROM的镜像，所谓定制镜像，一定是以一个镜像为基础，在其上进行定制。基础镜像是必须指定的，而FROM就是指定基础镜像，因此一个Dockerfile中FROM是必备的指令，并且必须是第一条指令。在Docker Hub上有非常多的高质量的官方镜像，有可以直接拿来使用的服务类的镜像，如nginx、redis、mysql、tomcat等；可以在其中寻找一个最符合最终目标的镜像为基础镜像进行定制。    

&emsp; FROM格式： 

    FROM \<image>:\<tag>  

&emsp; 其中tag或digest是可选的，如果不使用这两个值时，会使用latest版本的基础镜像。  
&emsp; 示例：FROM mysql:5.6  

### 1.2.2. MAINTAINER指令  
&emsp; MAINTAINER用来声明维护者信息，该命令已经过期，推荐使用 LABEL ，格式：  

    MAINTAINER \<name>  

### 1.2.3. LABEL指令  
&emsp; LABEL：用于为镜像添加元数据,多用于声明构建信息，作者、机构、组织等。格式：  

    LABEL <key>=<value> <key>=<value> <key>=<value> ...  

&emsp; 示例： LABEL version="1.0" description="felord.cn" by="Felordcn"  
&emsp; 使用LABEL 指定元数据时，一条LABEL指定可以指定一或多条元数据，指定多条元数据时不同元数据之间通过空格分隔。推荐将所有的元数据通过一条LABEL指令指定，以免生成过多的中间镜像。  

### 1.2.4. RUN命令
&emsp; RUN命令是在新镜像内部执行的命令，比如安装一些软件、配置一些基础环境。其格式有两种：

1. shell 格式：RUN <命令行命令>  

```text
RUN echo helloworld
RUN yum install wget
RUN tar -xvf xxx.tar.gz
```

2. exec 格式：RUN ["可执行文件",“参数1”,“参数2”]  

```text
RUN ["echo", "helloworld"]
```

### 1.2.5. ENV命令  
ADD指令和COPY的格式和性质基本一致。但是在COPY基础上增加了一些功能。比如<源路径>可以是一个URL,这种情况下，Docker引擎会试图去下载这个链接的文件放到<目标路径>去。  
    
    在Docker官方的Dockerfile最佳实践文档中要求，尽可能的使用COPY，因此COPY的语义很明确，就是复制文件而已，没有必要使用ADD高级的命令  

ENV 用来设置环境变量，格式：  

ENV <key> <value> # 之后的所有内容均会被视为其的组成部分，因此，一次只能设置一个变量  

ENV <key>=<value> ... #可以设置多个变量，每个变量为一个"="的键值对，如果中包含空格，可以使用\来进行转义，也可以通过""来进行标识；另外，\ 也可以用于续行  

示例： ENV version 1.0.0 或者 ENV version=1.0.0  

可以通过 ${key} 在其它指令中来引用变量，如 ${version} 。我们也可以通过 docker run 中的 -e <ENV> 来动态赋值  

### 1.2.6. ARG指令  
构建参数和ENV的效果一样，都是设置环境变量。所不同的是，ARG所设置的构建环境的环境变量，在将来容器运行时是不会存在这些环境变量的。  

格式：  

    ARG <参数名>[=<默认值>]  

构建命令 docker build 中可以用 --build-arg <参数名>=<值> 来覆盖。  


### 1.2.7. VOLUME指令  
容器运行时应该尽量保持容器存储层不发生写操作，对于数据库需要保存动态数据的应用，其数据库文件应该保存于卷(volume)中，为了防止运行时用户忘记将动态文件所保存目录挂载为卷，在Dockerfile中,我们可以事先指定某些目录挂载为匿名卷，这样在运行时如果用户不指定挂载，其应用也可以正常运行，不会向容器存储层写入大量数据：  

```text
VOLUME /data
```
这里的/data目录就会在运行时自动挂载为匿名卷，任何向/data中写入的信息都不会记录进容器存储层，从而保证了容器存储层的无状态化。当然，运行时可以覆盖这个挂载设置。  

```text
比如：docker run -d -v mydata:/data xxxx
```
在这行命令中，就使用了mydata这个命名卷挂载到了/data这个位置，替代了Dockerfile中定义的匿名卷的挂载配置。  

### 1.2.8. COPY命令  
COPY 的功能类似于 ADD，但是不会自动解压文件，也不能访问网络资源  
复制指令，从上下文目录中复制文件或者目录到容器里指定路径：  

    COPY <源路径>...<目标路径> COPY ["<源路径1>",..."<目标路径>"]  

    COPY /usr/local/s.jar /usr/local/b.jar


注意：需要复制的目录一定要放在Dockerfile文件的同级目录下  
原因：  

    因为构建环境将会上传到Docker守护进程，而复制是在Docker守护进程中进行的。任何位于构建环境之外的东西都是不可用的。COPY指令的目的的位置则必须是容器内部的一个绝对路径。
    ---《THE DOCKER BOOK》

### 1.2.9. ADD命令  
ADD指令和COPY的格式和性质基本一致。但是在COPY基础上增加了一些功能。比如<源路径>可以是一个URL,这种情况下，Docker引擎会试图去下载这个链接的文件放到<目标路径>去。  

    在Docker官方的Dockerfile最佳实践文档中要求，尽可能的使用COPY，因此COPY的语义很明确，就是复制文件而已，没有必要使用ADD高级的命令


### 1.2.10. EXPOSE命令  
EXPOSE指令是声明运行时容器提供服务端口，这只是一个声明，在运行时并不会因为这个声明应该就会开启这个端口的服务。  

EXPOSE <端口1> [<端口2>...]

* 帮助镜像使用者理解这个镜像服务的守护端口，以方便配置映射。  
* 运行时使用随机端口映射时，也就是 docker run -P 时，会自动随机映射 EXPOSE 的端口。  


### 1.2.11. WORKDIR命令  
在构建镜像时，指定镜像的工作目录，之后的命令都是基于此工作目录，如果不存在，则会创建目录。  

WORKDIR 用来指定工作目录，类似于我们通常使用的cd 命令，格式：  

WORKDIR <PATH>  

通过 WORKDIR 设置工作目录，Dockerfile 中的其它指令 RUN、CMD、ENTRYPOINT、ADD、COPY等命令都会在该目录下执行。在使用 docker run 运行容器时，可以通过 -w 参数覆盖构建时所设置的工作目录。  

使用WORKDIR指令可以来指定工作目录(或者称为当前目录),以后各层的当前目录就被改为指定的目录，如该目录不存在，WORKDIR会帮你建立目录。  

之前提到一些初学者常犯的错误是把Dockerfile等同于Shell脚本来书写，这种错误的理解还可能会导致出现下面这样的错误：  

    RUN cd /app

RUN echo "hello">world.txt 如果将这个Dockerfile进行构建镜像运行后，会发现找不到/app/world.txt文件。  

原因：在Shell中，连续两行是同一个进程执行环境，因此前一个命令修改的内存状态，会直接影响后一个命令。  

而在Dockerfile中，这两行RUN命令的执行环境根本不同，是两个完全不同的容器。这就是对Dockerfile构建分层存储的概念不了解导致的错误。  

    每一个RUN都是启动一个容器、执行命令、然后提交存储层文件变量。第一层RUN cd /app的执行仅仅是当前进程的工作目录变量，一个内存上的变化而已，其结果不会造成任何文件变更。而到第二层的时候，启动的是一个全新的容器，跟第一层的容器更完全没关系，自然不可能继承前一层构建过程中的内存变化。  

因此如果需要改变以后各层的工作目录的位置，那么应该使用WORKIDR指令。  

### 1.2.12. CMD命令  
是容器运行时执行的命令，命令和run有本质的区别：  

    CMD 在docker run 时运行。RUN 是在 docker build。  

作用：为启动的容器指定默认要运行的程序，程序运行结束，容器也就结束。CMD 指令指定的程序可被 docker run 命令行参数中指定要运行的程序所覆盖。  

    注意：如果 Dockerfile 中如果存在多个 CMD 指令，仅最后一个生效。    

格式：  

    CMD CMD ["<可执行文件或命令>","","",...] CMD ["","",...] # 写法是为 ENTRYPOINT指令指定的程序提供默认参数  


CMD 构建容器后执行的命令，也就是在容器启动时才执行的命令。格式：  

```
 \# 执行可执行文件，优先执行
 CMD ["executable","param1","param2"]
 \# 设置了 ENTRYPOINT，则直接调用ENTRYPOINT添加参数  参见 CMD 讲解
 CMD ["param1","param2"]
 \# 执行shell命令
 CMD command param1 param2
 ```
 示例： CMD ["/usr/bin/bash","--help"]  

CMD 不同于 RUN，CMD 用于指定在容器启动时所要执行的命令，而RUN用于指定镜像构建时所要执行的命令。  


### ENTRYPOINT命令  
ENTRYPOINT的目的和CMD一样，都是在指定容器启动程序及参数。ENTRYPOINT在运行也可以替代，不过比CMD要略显繁琐，需要通过docker run的参数 --entrypoint来指定。  
当指定了ENTRYPOINT后，CMD的含义就发生了改变*，不再是直接的运行*其命令，而是将CMD的内容作为参数传给ENTRYPOINT指令，换句话说实际执行时，将变为：  

    <ENTRYPOINT>"<CMD>"

上面的意思就是CMD命令会被ENTRYPOINT覆盖掉；但有个特殊的情况，如果CMD的格式是如下格式：  

    CMD ["","",...]

就是把CMD的参数，当作参数传给ENTRYPOINT命令。  


ENTRYPOINT 用来配置容器，使其可执行化。配合 CMD可省去 application，只使用参数。格式：  

    \#可执行文件, 优先
    ENTRYPOINT ["executable", "param1", "param2"]
    \# shell内部命令
    ENTRYPOINT command param1 param2

示例：  

    FROM ubuntu
    ENTRYPOINT ["top", "-b"]
    CMD ["-c"]

ENTRYPOINT 与 CMD 非常类似，不同的是通过 docker run 执行的命令不会覆盖 ENTRYPOINT ，而 docker run 命令中指定的任何参数都会被当做参数再次传递给 ENTRYPOINT 指令。Dockerfile 中只有最后一个 ENTRYPOINT 命令起作用，也就是说如果你指定多个ENTRYPOINT,只执行最后的 ENTRYPOINT 指令。   

### ONBUILD指令   
ONBUILD 作用是其当所构建的镜像被用做其它镜像的基础镜像，该镜像中的 ONBUILD 中的命令就会触发，格式：  

    ONBUILD [INSTRUCTION]  

示例：  
 
    ONBUILD ADD . /application/src
    ONBUILD RUN /usr/local/bin/python-build --dir /app/src

## 附: Spring Boot Dockerfile  



```text
\# 使用 aws 的java jdk 8
FROM amazoncorretto:8
\# 作者等相关的元信息
LABEL AUTHOR=Felordcn OG=felord.cn
\# 挂载卷
VOLUME ["/tmp","/logs"]
\# 时区
ENV TZ=Asia/Shanghai
\# 启用配置文件 默认为 application.yml
ENV ACTIVE=defualt
\# 设置镜像时区
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
\# 修改为打包后的jar文件名称
ADD /target/flyway-spring-boot-1.0.0.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=${ACTIVE}","-jar","app.jar"]
```


例：构建一个带有jdk的centos7镜像  

```text
[root@localhost Dockerfile]# mkdir Dockerfile
[root@localhost Dockerfile]# cd Dockerfile
```
编写Dockerfile：  

```text
FROM centos:7
MAINTAINER  Java-Road "Java-Road@qq.com"

RUN mkdir /usr/local/jdk
COPY jdk-8u171-linux-x64.rpm /usr/local/jdk/
RUN rpm -ivh /usr/local/jdk/jdk-8u171-linux-x64.rpm
```
执行如下指令：  

```text
[root@localhost Dockerfile]# docker build -t centos/jdk .
```

运行结果如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-6.png)  

docker images可以看到新生成的centos/jdk镜像。  


<!-- 
```text
FROM scratch #制作base image 基础镜像，尽量使用官方的image作为base image
FROM centos #使用base image
FROM ubuntu:14.04 #带有tag的base image

LABEL version=“1.0” #容器元信息，帮助信息，Metadata，类似于代码注释
LABEL maintainer=“yc_uuu@163.com"

\# 2. 对于复杂的RUN命令，避免无用的分层，多条命令用反斜线换行，合成一条命令！
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
\# 3. CMD ["/bin/bash","-c","echo hello $name"]
ENTRYPOINT ["/bin/bash","-c","echo hello $name”]
```
-->


