<!-- TOC -->

- [1. DockerFile](#1-dockerfile)
    - [1.1. 基于容器构建镜像(docker commit)](#11-基于容器构建镜像docker-commit)
    - [1.2. 基于本地模板导入(docker import)](#12-基于本地模板导入docker-import)
    - [1.3. 基于Dockerfile构建](#13-基于dockerfile构建)
        - [1.3.1. Dockerfile](#131-dockerfile)
        - [1.3.2. 构建过程](#132-构建过程)
    - [1.4. 附录：构建jdk的镜像](#14-附录构建jdk的镜像)

<!-- /TOC -->

# 1. DockerFile
<!--
 Docker系列教程04-Docker构建镜像的三种方式 
https://mp.weixin.qq.com/s/06w1rsz6c_fLhDe2FYUlJg
-->

&emsp; 创建镜像的方法主要有三种：基于已有镜像的容器创建、基于本地模板导入、基于DockerFile创建。  

## 1.1. 基于容器构建镜像(docker commit)
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
&emsp; 通过docker images命令可以看到新增了centos/jdk标签为2.0的镜像。  

## 1.2. 基于本地模板导入(docker import)  
&emsp; 用户也可以从模板文件中导入镜像，主要使用docker import命令。  


## 1.3. 基于Dockerfile构建
<!-- 
镜像的定制实际上就是定制每一层所添加的配置、文件。我们可以把每一层修改、安装、构建、操作的命令都写入一个脚本，这个脚本就是Dockerfile。  
Dockerfile是一个文本文件，其内包含了一条条的指令，每一条指令构建一层，因此每一条指令的内容，就是描述该层应当如何构建。 
-->

&emsp; 将应用整合到容器中并且运行起来的这个过程，称为“容器化”(Containerizing )。一般来说，完成的应用容器化过程主要分为以下几个步骤：  

1. 编写应用代码；
2. 创建一个Dockerfile，其中包括当前应用的描述、依赖以及该如何运行这个应用；
3. 对该Dockerfile执行docker image build命令
4. 等待 Docker 将应用程序和依赖等构建到Docker镜像中。

&emsp; 一旦应用及其依赖被打包成一个Docker镜像，就能以镜像的形式交付并以容器的方式运行了。当然还可以将镜像推送到镜像仓库服务。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-27.png)  


&emsp; 应用容器化过程中最基础的是创建一个 Dockerfile。Dockerfile是一个包含用于组合镜像的命令的文本文档(必须要写成 Dockerfile)，它主要有两个用途，一是对当前应用的描述，二是指导Docker完成镜像的构建。Dockerfile对当前的应用及其依赖有一个清晰准确的描述，并且非常容易阅读和理解。另外，Dockerfile也通常被放在构建上下文的根目录下（在Docker当中，包含应用文件的目录通常被称为构建上下文（Build Context），在构建镜像时构建上下文和该上下文中的文件和目录都会被上传到 Docker daemon，这样 daemon 可以直接访问你想在镜像中存储的任何代码、文件或者其他数据）。  

### 1.3.1. Dockerfile  
<!-- 
https://mp.weixin.qq.com/s/xq9lrHqBOWjQ65-V4Jrttg
-->
&emsp; Dockerfile 中的注释行都是以 # 开始的，除注释之外，每一行都是一条指令（使用基本的基于 DSL 语法的指令），指令及其使用的参数格式如下。指令是不区分大小写的，但是通常都采用大写的方式（可读性会更高）。  

&emsp; **<font color = "red">组成部分</font>**    
&emsp; **<font color = "lime">Dockerfile一般分为：基础镜像、镜像元信息、镜像操作指令和容器启动时执行指令，# 为 Dockerfile中的注释。</font>**  

|部分|命令|
|---|---|
|基础镜像信息|FROM|
|维护者信息|MAINTAINER|
|镜像操作指令|RUN、COPY、ADD、EXPOSE、WORKDIR、ONBUILD、USER、VOLUME等|
|容器启动时执行指令|CMD、ENTRYPOINT|

![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-26.png)  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-9.png)  


<!-- 
https://mp.weixin.qq.com/s/whWxIflM807JCLLzQl726g
-->

1. FROM命令  
    &emsp; 定制的镜像都是基于FROM的镜像，所谓定制镜像，一定是以一个镜像为基础，在其上进行定制。基础镜像是必须指定的，而FROM就是指定基础镜像，因此一个Dockerfile中FROM是必备的指令，并且必须是第一条指令。在Docker Hub上有非常多的高质量的官方镜像，有可以直接拿来使用的服务类的镜像，如nginx、redis、mysql、tomcat等；可以在其中寻找一个最符合最终目标的镜像为基础镜像进行定制。    

    &emsp; FROM格式： 

        FROM <image>:<tag>  

    &emsp; 其中tag或digest是可选的，如果不使用这两个值时，会使用latest版本的基础镜像。  
    &emsp; 示例：FROM mysql:5.6  

2. MAINTAINER指令  
    &emsp; MAINTAINER用来声明维护者信息，该命令已经过期，推荐使用 LABEL ，格式：  

        MAINTAINER <name>  

3. LABEL指令  
    &emsp; LABEL：用于为镜像添加元数据,多用于声明构建信息，作者、机构、组织等。格式：  

        LABEL <key>=<value> <key>=<value> <key>=<value> ...  

    &emsp; 示例： LABEL version="1.0" description="felord.cn" by="Felordcn"  
    &emsp; 使用LABEL 指定元数据时，一条LABEL指定可以指定一或多条元数据，指定多条元数据时不同元数据之间通过空格分隔。推荐将所有的元数据通过一条LABEL指令指定，以免生成过多的中间镜像。  

4. RUN命令  
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

5. ENV命令  
    &emsp; 设置环境变量，定义了环境变量，那么在后续的指令中，就可以使用这个环境变量：

        ENV <key> <value> # 之后的所有内容均会被视为其的组成部分，因此，一次只能设置一个变量  
        ENV <key>=<value> ... #可以设置多个变量，每个变量为一个"="的键值对，如果中包含空格，可以使用\来进行转义，也可以通过""来进行标识；另外，\ 也可以用于续行  

    &emsp; 示例： ENV version 1.0.0 或者 ENV version=1.0.0  
    &emsp; 可以通过 ${key} 在其它指令中来引用变量，如 ${version} 。也可以通过 docker run 中的 -e \<ENV> 来动态赋值  

    <!-- 
    这个指令很简单，就是设置环境变量而已，无论是后面的其它指令，如RUN，还是运行时的应用，都可以直接使用这里定义的环境变量。通过${key1}方式引用即可。  
    -->

6. ARG指令  
    &emsp; 构建参数和ENV的效果一样，都是设置环境变量。所不同的是， **<font color = "red">ARG所设置的构建环境的环境变量，用于指定传递给构建运行时的变量，在将来容器运行时是不会存在这些环境变量的。</font>**  

    &emsp; 格式：  

        ARG <参数名>[=<默认值>]  

    &emsp; 通过docker run中的 --build-arg \<key>=\<value> 来动态赋值，不指定将使用其默认值。    

7. VOLUME指令  
    &emsp; 容器运行时应该尽量保持容器存储层不发生写操作，对于数据库需要保存动态数据的应用，其数据库文件应该保存于卷(volume)中，为了防止运行时用户忘记将动态文件所保存目录挂载为卷，在Dockerfile中，可以事先指定某些目录挂载为匿名卷，这样在运行时如果用户不指定挂载，其应用也可以正常运行，不会向容器存储层写入大量数据：  

    ```text
    VOLUME /data
    ```
    &emsp; 这里的/data目录就会在运行时自动挂载为匿名卷，任何向/data中写入的信息都不会记录进容器存储层，从而保证了容器存储层的无状态化。当然，运行时可以覆盖这个挂载设置。  

    ```text
    比如：docker run -d -v mydata:/data xxxx
    ```
    &emsp; 在这行命令中，就使用了mydata这个命名卷挂载到了/data这个位置，替代了Dockerfile中定义的匿名卷的挂载配置。  

8. COPY命令  
    &emsp; 复制指令，从上下文目录中复制文件或者目录到容器里指定路径：  

        COPY <源路径>...<目标路径> COPY ["<源路径1>",..."<目标路径>"]  

        COPY /usr/local/s.jar /usr/local/b.jar

    &emsp; 注意：需要复制的目录一定要放在Dockerfile文件的同级目录下  
    &emsp; 原因：  

        因为构建环境将会上传到Docker守护进程，而复制是在Docker守护进程中进行的。任何位于构建环境之外的东西都是不可用的。COPY指令的目的的位置则必须是容器内部的一个绝对路径。
        ---《THE DOCKER BOOK》

9. ADD命令  
    &emsp; ADD指令和COPY的格式和性质基本一致。但是在COPY基础上增加了一些功能。比如<源路径>可以是一个URL,这种情况下，Docker引擎会试图去下载这个链接的文件放到<目标路径>去。  

        在Docker官方的Dockerfile最佳实践文档中要求，尽可能的使用COPY，因为COPY的语义很明确，就是复制文件而已，没有必要使用ADD高级的命令。

10. EXPOSE命令  
    &emsp; EXPOSE指令是声明运行时容器提供服务端口，这只是一个声明，在运行时并不会因为这个声明应该就会开启这个端口的服务。  

    &emsp; EXPOSE <端口1> [<端口2>...]

    * 帮助镜像使用者理解这个镜像服务的守护端口，以方便配置映射。  
    * 运行时使用随机端口映射时，也就是docker run -P时，会自动随机映射EXPOSE的端口。  

11. WORKDIR命令  
    &emsp; 在构建镜像时，WORKDIR指定镜像的工作目录(或者称为当前目录)，RUN、CMD、ENTRYPOINT、ADD、COPY等命令都会在该目录下执行。如果不存在，则会创建目录。类似于通常使用的cd 命令，格式：  

        WORKDIR <PATH>  

    &emsp; 在使用docker run运行容器时，可以通过 -w 参数覆盖构建时所设置的工作目录。    

    &emsp; 之前提到一些初学者常犯的错误是把Dockerfile等同于Shell脚本来书写，这种错误的理解还可能会导致出现下面这样的错误：  

        RUN cd /app

    &emsp; RUN echo "hello">world.txt 如果将这个Dockerfile进行构建镜像运行后，会发现找不到/app/world.txt文件。  
    &emsp; 原因：在Shell中，连续两行是同一个进程执行环境，因此前一个命令修改的内存状态，会直接影响后一个命令。  
    &emsp; 而在Dockerfile中，这两行RUN命令的执行环境根本不同，是两个完全不同的容器。这就是对Dockerfile构建分层存储的概念不了解导致的错误。  

        每一个RUN都是启动一个容器、执行命令、然后提交存储层文件变量。第一层RUN cd /app的执行仅仅是当前进程的工作目录变量，一个内存上的变化而已，其结果不会造成任何文件变更。而到第二层的时候，启动的是一个全新的容器，跟第一层的容器更完全没关系，自然不可能继承前一层构建过程中的内存变化。  

    &emsp; 因此如果需要改变以后各层的工作目录的位置，那么应该使用WORKIDR指令。  

12. CMD命令  
    &emsp; 作用：为启动的容器指定默认要运行的程序。CMD指令指定的程序可被docker run命令行参数中指定要运行的程序所覆盖。 **<font color = "lime">注意：如果 Dockerfile 中如果存在多个 CMD 指令，仅最后一个生效。</font>    

        CMD命令是容器运行时执行的命令，命令和run有本质的区别：CMD 用于指定在容器启动docker run时所要执行的命令，而RUN用于指定镜像构建docker build时所要执行的命令。     

    格式：  

    <!-- 
    CMD ["<可执行文件或命令>","","",...] CMD ["","",...] # 写法是为 ENTRYPOINT指令指定的程序提供默认参数  
    -->
    ```
    \# 执行可执行文件，优先执行
    CMD ["<可执行文件或命令>","param1","param2"]
    \# 设置了 ENTRYPOINT，则直接调用ENTRYPOINT添加参数
    CMD ["param1","param2"]
    \# 执行shell命令
    CMD command param1 param2
    ```
    示例： CMD ["/usr/bin/bash","--help"]  
13. ENTRYPOINT命令  
    &emsp; ENTRYPOINT用来配置容器，使其可执行化。配合 CMD可省去 application，只使用参数。docker run 命令中指定的任何参数都会被当做参数再次传递给 ENTRYPOINT 指令。Dockerfile 中只有最后一个 ENTRYPOINT 命令起作用，也就是说如果指定多个ENTRYPOINT，只执行最后的 ENTRYPOINT 指令。  
    &emsp; 当指定了ENTRYPOINT后，CMD的含义就发生了改变*，不再是直接的运行*其命令，而是将CMD的内容作为参数传给ENTRYPOINT指令，换句话说实际执行时，将变为：  

        <ENTRYPOINT>"<CMD>"

    &emsp; 上面的意思就是CMD命令会被ENTRYPOINT覆盖掉；但有个特殊的情况，如果CMD的格式是如下格式：  

        CMD ["","",...]

    &emsp; 就是把CMD的参数，当作参数传给ENTRYPOINT命令。  
14. ONBUILD指令   
    &emsp; ONBUILD 作用是其当所构建的镜像被用做其它镜像的基础镜像，该镜像中的 ONBUILD 中的命令就会触发，格式：  

        ONBUILD [INSTRUCTION]  

    &emsp; 示例：  
    
        ONBUILD ADD . /application/src
        ONBUILD RUN /usr/local/bin/python-build --dir /app/src

### 1.3.2. 构建过程  
&emsp; 上述的 Dockerfile 编写完成之后，使用docker image build 指令，会解析 Dockerfile 中的指令并顺序执行，构建过程是，运行临时容器->在该容器中运行 Dockerfile 中的指令->将指令运行结果保存为一个新的镜像层（执行类似 docker commit 的操作）->删除容器。  

```text
# 构建出一个叫 web:latest 的镜像，.（点）表示将当前目录作为构建上下文并且当前目录需要包含 Dockerfile
# 如果没有指定标签的话，那么会默认设置一个 latest 标签
docker image build -t web:latest . 
# 可以通过 docker image build 的输出内容了解镜像的构建过程，而构建过程的最终结果是返回了新镜像的 ID。其实，构建的每一步都会返回一个镜像的 ID。
```

1. 可以使用 docker image history 来查看构建镜像的过程中都执行了哪些指令。也可以使用 docker image inspect 来确认一共创建了几层。
2. 如果构建过程中失败了的话，也将会得到一个可以使用的镜像。可以基于这个镜像，执行失败的指令查看具体原因。
3. 如果在构建上下文的根目录存在以 .dockerignore 命名的文件的话，那么该文件中匹配的文件不会被上传到构建上下文中去。该文件中模式匹配规则采用了 Go 语言的 filepath。

&emsp; **构建镜像的过程中会利用缓存**  
&emsp; Docker 构建镜像的过程中会利用缓存机制。对于每一条指令，Docker 都会检查缓存中是否检查已经有与该指令对应的镜像层。如果有，即为缓存命中，并且使用这个镜像层；如果没有，则是缓存未命中，Docker 会基于该指令构建新的镜像层。缓存命中能显著加快构建过程。  
&emsp; 比如，示例中使用的 Dockerfile。第一条指令告诉 Docker 使用 apline:latest 作为基础镜像。如果主机中已经存在这个镜像，那么构建时就会直接跳转到下一条指令；如果镜像不存在，则会从 Docker Hub 中拉取。  
&emsp; 下一条指令 RUN apk add --update nodejs nodejs-npm ，如果缓存中存在基于同一基础镜像并且执行了相同指令的镜像层的话（本例就是缓存中存在一个基于 alpine:latest 镜像并且在它之上执行了 RUN apk add --update nodejs nodejs-npm 指令构建而成的镜像层的话），那么则跳过这条指令，并链接到这个已存在的镜像层，然后继续构建；如果没有找到该镜像层，则设置缓存无效并构建该镜像层。设置缓存无效之后，接下来的指令将全部执行而不用再去检查缓存是否存在了。  
&emsp; 如果上一条指令击中了，并假设生成的镜像层 ID 是 AAA。那么在执行 COPY . /src 指令的时候，Docker 会检查缓存中是否存在一个镜像层也是基于 AAA 镜像层并执行了 COPY . /src 命令的。如果缓存中存在，那么则会链接到这个缓存的镜像层并继续执行后续指令；如果没有，则构建镜像层，并对后续的构建操作设置缓存无效。需要注意的是，这个阶段中，如果 COPY . /src 的指令没有变化，但是被复制的内容发生了变化，那么 Docker 会计算每一个被复制文件的 Checksum 值，并与缓存镜像层中同一个文件的 Checksum 值进行比较。如果不匹配同样设置缓存无效并构建新的镜像层。  
&emsp; 以此类推，需要注意的是一旦有指令在缓存中未击中，也就是没有该指令对应的镜像层，则后续的整个构建过程都将不再使用缓存。因此，在编写 Dokcerfile 的时候需要将易于发生变化的指令至于 Dockerfile 文件的后方执行。这样缓存未命中的情况将直到构建的后期才会出现，从而能够从缓存中获得更大的收益。  

&emsp; 如果在构建过程中，有一条指令使用了 apt-get update，结果缓存也有找到该指令执行过的镜像层，那么 Docker 不会刷新 APT 包的缓存，而是直接使用该缓存。此时，假如你需要每个包的最新版本的话，那么需要略过缓存的功能。比如缓存中有基于 Ubuntu16.04 并且执行了 apt-get update && apt-get install  nginx，那么当 Dockerfile 中也是基于 Ubuntu16.04 执行 apt-get update && apt-get install  nginx 的话，那么不会刷新 APT 包的缓存。想要禁止使用缓存，可使用 --no-cache。  

&emsp; **构建时可以合并镜像，即构建出只含一个镜像层的镜像**  
&emsp; 合并镜像也就是将所有的镜像层合并到一个镜像层中。这种方式在利弊参半，但是当镜像中的层数太多时，合并是一个不错的优化方式。  

&emsp; 比如，创建一个新的基础镜像时，以便基于这个基础镜像来构建其他镜像时，这个基础镜像最好是被合并为一层。但是合并之后的镜像将无法共享镜像层，在 push 和 pull 的时候镜像体积会更大。如图所示，原本在 push 的时候，左边的那个基础镜像层已经在 Docker Hub 中了，那么只需要 push 其他三层没有的镜像层即可。但是，合并的镜像却需要全都被 push。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-28.png)  

&emsp; **多阶段构建---优化构建出来的镜像**  
&emsp; 对于 Docker 镜像来说，过大的体积并不好。越大的体积意味着更难使用而且也容器遭受攻击，因此 Docker 镜像应该尽量小，将镜像缩小到仅包含应用所必须的内容即可。比如 RUN 指令被执行时，可能会拉取一些构建工具，这些工具其实也可以删除。这时候 Dockerfile 的写法对镜像的大小会产生显著影响，下面介绍一下多阶段构建的方式来优化产生的镜像大小。  

&emsp; 多阶段构建的方式即使用一个 Dockerfile 文件，但其中包含多个 FROM 指令。每一个 FROM 指令都是一个新的构建阶段，并且可以后面的构建阶段可以复制之前构建阶段产生的内容。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-29.png)  
&emsp; 比如，一个 Dockerfile 文件的内容如上所示。上面的内容显示了 3 个 FROM 指令，每个 FROM 指令构成一个单独的构建阶段，各个阶段在内部从 0 开始编号。不过，示例中针对每个阶段都定义了一个名字。  

* 阶段  0 叫做 storefront
* 阶段 1 叫做 appserver
* 阶段 2 叫做 production

&emsp; 阶段 0 即storefront阶段将会拉取大小超过600MB的node:latest镜像，然后设置工作目录，复制一些应用代码进去，然后使用2个RUN指令来执行npm操作，这会显著增加镜像大小，而其中包含了许多构建工具。  
&emsp; 阶段1即appserver 阶段将会拉取大小超过 700MB 的 maven:latest 镜像。然后设置工作目录等操作，这个阶段同样会构建出一个非常大的包含许多构建工具的镜像。  
&emsp; 阶段 3 即 production 阶段拉取 java:8-jdk-alpine 镜像，这个镜像大约 150MB。这个阶段会先创建一个用户，然后设置工作目录并将 storefront 阶段生成的镜像中的一些应用代码复制过来；之后再设置工作目录，将 appserver阶段生成的镜像中的一些应用代码复制过来。最后设置当前应用程序为容器启动时的主程序。这个阶段的重点在于 COPY --from指令，这个执行将从之前阶段构建的镜像中仅仅复制生产环境相关的应用代码，或者说仅需的应用代码，而不会产生生产环境不需要的构建工具。  
&emsp; 之后使用同样的命令（如docker image build -t multi:stage）构建镜像即可。当查看构建出来的镜像时，你会看到其他两个FROM阶段，即storefront阶段和appserver阶段构建出来的镜像，但是这些镜像是没有 REPO 和 TAG 的。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-30.png)  

## 1.4. 附录：构建jdk的镜像 
&emsp; 例：构建一个带有jdk的centos7镜像  

```text
[root@localhost Dockerfile]# mkdir Dockerfile
[root@localhost Dockerfile]# cd Dockerfile
```

&emsp; 编写Dockerfile：  

```text
FROM centos:7
MAINTAINER  Java-Road "Java-Road@qq.com"

RUN mkdir /usr/local/jdk
COPY jdk-8u171-linux-x64.rpm /usr/local/jdk/
RUN rpm -ivh /usr/local/jdk/jdk-8u171-linux-x64.rpm
```
&emsp; 执行如下指令：  

```text
[root@localhost Dockerfile]# docker build -t centos/jdk .
```

&emsp; 运行结果如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-6.png)  

&emsp; docker images可以看到新生成的centos/jdk镜像。  
