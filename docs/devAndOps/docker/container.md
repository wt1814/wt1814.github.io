

<!-- TOC -->

- [1. 容器详解](#1-容器详解)
    - [1.1. 容器生命周期](#11-容器生命周期)
        - [1.1.1. 创建和运行](#111-创建和运行)
        - [1.1.2. 休眠和销毁](#112-休眠和销毁)
        - [1.1.3. 重启策略](#113-重启策略)
    - [1.2. 容器隔离原理](#12-容器隔离原理)
        - [1.2.1. Namespaces，命名空间](#121-namespaces命名空间)
        - [1.2.2. CGroups](#122-cgroups)
    - [1.3. 容器的文件](#13-容器的文件)
        - [1.3.1. 容器的文件系统](#131-容器的文件系统)
        - [1.3.2. 数据卷(Volume)](#132-数据卷volume)
            - [1.3.2.1. 数据卷](#1321-数据卷)
            - [1.3.2.2. 数据卷容器](#1322-数据卷容器)
    - [1.4. 容器的通信](#14-容器的通信)
        - [1.4.1. Docker宿主机与容器通信(端口映射)](#141-docker宿主机与容器通信端口映射)
            - [1.4.1.1. 自动映射端口](#1411-自动映射端口)
            - [1.4.1.2. 绑定端口到指定接口](#1412-绑定端口到指定接口)
            - [1.4.1.3. 示例](#1413-示例)
        - [1.4.2. 容器间通信](#142-容器间通信)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 单个宿主机的多个容器是隔离的，其依赖于Linux的Namespaces、CGroups。  
2. 隔离的容器需要通信、文件共享（数据持久化）。  

# 1. 容器详解  
<!-- 
容器
https://docs.docker.com/config/containers/start-containers-automatically/
-->
&emsp; 容器是基于镜像启动起来的，是镜像的一个运行实例，容器中可以运行一个或多个进程。同时，用户可以从单个镜像上启动一个或多个容器。  
&emsp; 一个容器内的一个或多个进程的运行是依靠容器所在宿主机的内核，但是这些进程属于自己的独立的命名空间，拥有自己的root文件系统、自己的网络配置、自己的进程空间和自己的用户ID等等，所以容器内的进程相当于运行在一个隔离的环境里，就相当于运行在一个新的操作系统中一样。容器使用root文件系统大部分是由镜像提供的，还有一些是由Docker为Docker容器生成的。  
&emsp; 运行的容器是共享宿主机内核的，也就是相当于容器在执行时还是依靠主机的内核代码的。这也就意味着一个基于Windows的容器化应用在Linux主机上无法运行的。也可以简单地理解为Windows容器需要运行在Windows宿主机之上，Linux容器需要运行在Linux宿主机上。Docker推荐单个容器只运行一个应用程序或进程，但是其实也可以运行多个应用程序。  

## 1.1. 容器生命周期  
![image](http://182.92.69.8:8081/img/devops/docker/docker-4.png)  
&emsp; <font color = "clime">容器的生命周期大致可分为4个：创建、运行、休眠和销毁。</font>  

### 1.1.1. 创建和运行
&emsp; 容器的创建和运行主要使用docker container run命名，比如下面的命令会从Ubuntu:latest这个镜像中启动/bin/bash这个程序。那么/bin/bash成为了容器中唯一运行的进程。  

    docker container run -it ubuntu:latest /bin/bash

&emsp; 当运行上述的命令之后，Docker客户端会选择合适的API来调用Docker daemon接收到命令并搜索Docker本地缓存，观察是否有命令所请求的镜像。如果没有，就去查询Docker Hub是否存在相应镜像。找到镜像后，就将其拉取到本地，并存储在本地。一旦镜像拉取到本地之后，Docker daemon就会创建容器并在其中运行指定应用。  

&emsp; ps -elf命令可以看到会显示两个，那么其中一个是运行ps -elf产生的。  
![image](http://182.92.69.8:8081/img/devops/docker/docker-22.png)  

&emsp; 假如，此时输入exit退出Bash Shell之后，那么容器也会退出(休眠)。因为容器如果不运行任何进程则无法存在，上面将容器的唯一进程杀死之后，容器也就没了。其实，当把进程的PID为1的进程杀死就会杀死容器。  

### 1.1.2. 休眠和销毁
&emsp; docker container stop命令可以让容器进入休眠状态，使用docker container rm可以删除容器。删除容器的最佳方式就是先停止容器，然后再删除容器，这样可以给容器中运行的应用/进程一个停止运行并清理残留数据的机会。因为先stop的话，docker container stop命令会像容器内的PID 1进程发送SIGTERM信号，这样会给进程预留一个清理并优雅停止的机会。如果进程在10s的时间内没有终止，那么会发送SIGKILL信号强制停止该容器。但是docker container rm命令不会友好地发送 SIGTERM，而是直接发送SIGKILL信号。  

### 1.1.3. 重启策略
&emsp; 容器还可以配置重启策略，这是容器的一种自我修复能力，可以在指定事件或者错误后重启来完成自我修复。  
&emsp; 配置重启策略有两种方式，一种是命令中直接传入参数，另一种是在Compose文件中声明。下面阐述命令中传入参数的方式，也就是在命令中加入--restart标志，该标志会检查容器的退出代码，并根据退出码已经重启策略来决定。  
&emsp; Docker支持的重启策略包括always、unless-stopped和on-failed四种。

* always策略会一直尝试重启处于停止状态的容器，除非通过docker container stop命令明确将容器停止。另外，当daemon重启的时候，被docker container stop停止的设置了 always策略的容器也会被重启。

        $ docker container run --it --restart always apline sh 
        # 过几秒之后，在终端中输入 exit，过几秒之后再来看一下。照理来说应该会处于stop状态，但是会发现又处于运行状态了。

* unless-stopped策略和always策略是差不多的，最大的区别是，docker container stop停止的容器在daemon重启之后不会被重启。
* on-failure策略会在退出容器并且返回值不会0的时候，重启容器。如果容器处于stopped状态，那么daemon重启的时候也会被重启。另外，on-failure还接受一个可选的重启次数参数，如--restart=on-failure:5表示最多重启5次。

## 1.2. 容器隔离原理  
### 1.2.1. Namespaces，命名空间  
&emsp; <font color = "clime">命名空间(namespaces)是Linux提供的用于分离进程树、网络接口、挂载点以及进程间通信等资源的方法。</font>在日常使用Linux或者macOS时，并没有运行多个完全分离的服务器的需要，但是如果在服务器上启动了多个服务，这些服务其实会相互影响的，每一个服务都能看到其他服务的进程，也可以访问宿主机器上的任意文件，这是很多时候不愿意看到的，更希望运行在同一台机器上的不同服务能做到完全隔离，就像运行在多台不同的机器上一样。  
![image](http://182.92.69.8:8081/img/devops/docker/docker-34.png)  
&emsp; 在这种情况下，一旦服务器上的某一个服务被入侵，那么入侵者就能够访问当前机器上的所有服务和文件，这也是不想看到的，而Docker其实就通过Linux的Namespaces对不同的容器实现了隔离。  
&emsp; Linux 的命名空间机制提供了以下七种不同的命名空间，包括CLONE_NEWCGROUP、CLONE_NEWIPC、CLONE_NEWNET、CLONE_NEWNS、CLONE_NEWPID、CLONE_NEWUSER和CLONE_NEWUTS，通过这七个选项能在创建新的进程时设置新进程应该在哪些资源上与宿主机器进行隔离。  

### 1.2.2. CGroups  
&emsp; Linux的命名空间为新创建的进程隔离了文件系统、网络并与宿主机器之间的进程相互隔离，但是命名空间并不能够提供物理资源上的隔离，比如CPU或者内存，如果在同一台机器上运行了多个对彼此以及宿主机器一无所知的『容器』，这些容器却共同占用了宿主机器的物理资源。  
![image](http://182.92.69.8:8081/img/devops/docker/docker-35.png)  
&emsp; 如果其中的某一个容器正在执行CPU密集型的任务，那么就会影响其他容器中任务的性能与执行效率，导致多个容器相互影响并且抢占资源。如何对多个容器的资源使用进行限制就成了解决进程虚拟资源隔离之后的主要问题，<font color = "red">而Control Groups(简称 CGroups)就是能够隔离宿主机器上的物理资源，例如 CPU、内存、磁盘I/O和网络带宽。</font>  
&emsp; 每一个CGroup都是一组被相同的标准和参数限制的进程，不同的CGroup之间是有层级关系的，也就是说它们之间可以从父类继承一些用于限制资源使用的标准和参数。  
![image](http://182.92.69.8:8081/img/devops/docker/docker-36.png)  
&emsp; Linux的CGroup能够为一组进程分配资源，也就是在上面提到的CPU、内存、网络带宽等资源，通过对资源的分配，CGroup能够提供以下的几种功能：  
![image](http://182.92.69.8:8081/img/devops/docker/docker-37.png)  

    在CGroup中，所有的任务就是一个系统的一个进程，而CGroup就是一组按照某种标准划分的进程，在CGroup这种机制中，所有的资源控制都是以CGroup作为单位实现的，每一个进程都可以随时加入一个CGroup也可以随时退出一个CGroup。

## 1.3. 容器的文件
### 1.3.1. 容器的文件系统   
&emsp; 容器会共享其所在主机的操作系统/内核（容器执行使用共享主机的内核代码），<font color = "red">但是容器内运行的进程所使用的是容器自己的文件系统，也就是容器内部的进程访问数据时访问的是容器的文件系统。</font>当从一个镜像启动容器的时候，除了把镜像当成容器的文件系统一部分之外，Docker还会在该镜像的最顶层加载一个可读写文件系统，容器中运行的程序就是在这个读写层中执行的。  
&emsp; 使用Docker，启动容器，会新建两层内容。这两层分别为Docker容器的初始层(Init Layer)与可读写层(Read-Write Layer)：  

* 初始层中大多是初始化容器环境时，与容器相关的环境信息，如容器主机名，主机host信息以及域名服务文件等。  
* 再来看可读写层，这一层的作用非常大，Docker的镜像层以及顶上的两层加起来，Docker容器内的进程只对可读写层拥有写权限，其他层对进程而言都是只读的(Read-Only)。比如想修改一个文件，这个文件会从该读写层下面的只读层复制到该读写层，该文件的只读版本仍然存在，但是已经被读写层中的该文件副本所隐藏了。这种机制被称为写时复制(copy on write)(在 AUFS 等文件系统下，写下层镜像内容就会涉及COW(Copy-on-Write)技术)。另外，关于VOLUME以及容器的hosts、hostname、resolv.conf文件等都会挂载到这里。需要额外注意的是，虽然Docker容器有能力在可读写层看到VOLUME以及hosts文件等内容，但那都仅仅是挂载点，真实内容位于宿主机上。  

&emsp; 在运行阶段时，容器产生的新文件、文件的修改都会在可读写层上，当停止容器(stop)运行之后并不会被损毁，但是删除容器会丢弃其中的数据。  
![image](http://182.92.69.8:8081/img/devops/docker/docker-23.png)  

### 1.3.2. 数据卷(Volume)
<!--
https://www.docker.org.cn/dockerppt/117.html
Docker容器数据持久化
https://blog.csdn.net/dkfajsldfsdfsd/article/details/88789360
https://www.baidu.com/index.php?tn=monline_3_dg
https://blog.csdn.net/lucky_ykcul/article/details/96474854

容器间数据共享的原理，是在宿主机上开辟一块空间，该空间会被其他容器同时共享。我们可以把页面文件存放一份到宿主机的这块磁盘空间中，其他容器挂载这个文件即可。以后更新所有的web服务的页面，我们只需要更改宿主机的这份页面文件即可。

容器间共享数据-挂载

方法一、docker run --name 容器名 -v 宿主机路径:容器内挂载路径 镜像名。例如docker run --name myweb01 -v /usr/webapps:/usr/local/tomcat/webapps tomcat。

方法二、通过设置挂载点，我们可以创建一个用于挂载的容器，其他容器设置参照该容器的挂载信息，进行挂载。例如我们创建一个webpage的容器，设置挂载信息，无需运行该容器：  
docker create --name webpage -v /webapps:/tomcat/webapps tomcat /bin/true，接着我们创建其他容器参照该容器的挂载信息进行挂载：docker run --volumes-from webpage --name myweb02 -d tomcat,我们创建的myweb02的容器的挂载信息，就和webpage的挂载信息相同。如果需要修改挂载点，只需要修改webpage的挂载信息即可。  
-->
&emsp; docker通过数据卷(Volume)来进行数据持久化、文件共享。有数据卷和数据卷容器两种方式。  

#### 1.3.2.1. 数据卷
&emsp; 数据卷是一个或多个容器专门指定绕过Union File System的目录，为持续性或共享数据提供一些有用的功能：  

* 数据卷可以在容器间共享和重用。
* 数据卷数据改变是直接修改的。
* 数据卷数据改变不会被包括在容器中。
* 数据卷是持续性的，直到没有容器使用它们。

#### 1.3.2.2. 数据卷容器  
&emsp; 通过设置挂载点，可以创建一个用于挂载的容器，其他容器设置参照该容器的挂载信息，进行挂载。  
&emsp; 例如创建一个webpage的容器，设置挂载信息，无需运行该容器：`docker create --name webpage -v /webapps:/tomcat/webapps tomcat /bin/true`，接着创建其他容器参照该容器的挂载信息进行挂载：`docker run --volumes-from webpage --name myweb02 -d tomcat`，创建的myweb02的容器的挂载信息，就和webpage的挂载信息相同。如果需要修改挂载点，只需要修改webpage的挂载信息即可。 

## 1.4. 容器的通信
### 1.4.1. Docker宿主机与容器通信(端口映射)  
<!-- 
~~
Docker学习笔记：Docker 端口映射 
https://www.docker.org.cn/dockerppt/110.html
-->
&emsp; **容器运行在宿主机上，如果外网能够访问容器，才能够使用它提供的服务。Docker容器与宿主机进行通信可以通过映射容器的端口到宿主机上。**  

#### 1.4.1.1. 自动映射端口  
&emsp; -P使用时需要指定--expose选项，指定需要对外提供服务的端口

```text
$ sudo docker run -t -P --expose 22 --name server  ubuntu:14.04
```
&emsp; 使用docker run -P自动绑定所有对外提供服务的容器端口，映射的端口将会从没有使用的端口池中 (49000..49900) 自动选择，可以通过docker ps、docker inspect \<container_id>或者docker port \<container_id> \<port>确定具体的绑定信息。 

#### 1.4.1.2. 绑定端口到指定接口  
&emsp; 基本语法  

```text
$ sudo docker run -p [([<host_interface>:[host_port]])|(<host_port>):]<container_port>[/udp] <image> <cmd>
```
&emsp; 默认不指定绑定ip则监听所有网络接口。  

&emsp; 绑定TCP端口  

```text
# Bind TCP port 8080 of the container to TCP port 80 on 127.0.0.1 of the host machine. $ sudo docker run -p 127.0.0.1:80:8080 <image> <cmd> # Bind TCP port 8080 of the container to a dynamically allocated TCP port on 127.0.0.1 of the host machine. $ sudo docker run -p 127.0.0.1::8080 <image> <cmd> # Bind TCP port 8080 of the container to TCP port 80 on all available interfaces of the host machine. $ sudo docker run -p 80:8080 <image> <cmd> # Bind TCP port 8080 of the container to a dynamically allocated TCP port on all available interfaces $ sudo docker run -p 8080 <image> <cmd>
```

&emsp; **绑定UDP端口**  

```text
# Bind UDP port 5353 of the container to UDP port 53 on 127.0.0.1 of the host machine. $ sudo docker run -p 127.0.0.1:53:5353/udp <image> <cmd>
```

#### 1.4.1.3. 示例  
&emsp; 例如，使用如下命令启动一个容器  

    docker run -p 8080:80 --name test nginx  

&emsp; 使用-p参数将容器的80端口映射到宿主机的8080端口，这样就可以通过curl localhost:8080访问到容器上80端口的服务了。另外一个参数-P可以将容器的端口映射到宿主机的高位随机端口上，而不需要手动指定。  

### 1.4.2. 容器间通信
&emsp; [网络：容器间通信](/docs/devAndOps/docker/network.md)  

