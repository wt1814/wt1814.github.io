
<!-- TOC -->

- [1. Docker架构](#1-docker架构)
    - [1.1. Docker体系结构](#11-docker体系结构)
        - [1.1.1. C/S模式](#111-cs模式)
        - [1.1.2. Docker组件](#112-docker组件)
    - [1.2. ~~Docker运行流程~~](#12-docker运行流程)

<!-- /TOC -->

# 1. Docker架构
&emsp; **官网：** https://docs.docker.com/get-started/overview/#docker-architecture  

## 1.1. Docker体系结构  
<!-- 
https://mp.weixin.qq.com/s/RvURRnoSFPywtR8Af7IZ-g
-->

<!-- 
Docker 客户端只需要向 Docker 服务器或守护进程发出请求，服务器或守护进程将完成所有工作并返回结果。  
Docker 提供了一个命令行工具和一整套 RESTful API。可以在同一台宿主机上运行 Docker 守护进程和客户端，也可以从本地的 Docker 客户端连接到运行在另一台宿主机上的远程 Docker 守护进程。Docker 以 root 权限运行它的守护进程，来处理普通用户无法完成的操作(如挂载文件系统)。Docker 程序是 Docker 守护进程的客户端程序，同样也需要以 root 身份运行。  
-->
<!-- 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-18.png)  

* distribution 负责与docker registry交互，上传镜像以及管理registry有关的源数据
* registry负责docker registry有关的身份认证、镜像查找、镜像验证以及管理registry mirror等交互操作
* image 负责与镜像源数据有关的存储、查找，镜像层的索引、查找以及镜像tar包有关的导入、导出操作
* reference负责存储本地所有镜像的repository和tag名，并维护与镜像id之间的映射关系
* layer模块负责与镜像层和容器层源数据有关的增删改查，并负责将镜像层的增删改查映射到实际存储镜像层文件的graphdriver模块
* graghdriver是所有与容器镜像相关操作的执行者
-->
### 1.1.1. C/S模式
&emsp; Docker使用客户端-服务器(client-server)架构模式。通过下面这个图可以简单清晰看出Server/Client通信。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-45.png)  
<center>Docker架构图一</center>  

&emsp; **<font color = "clime">Docker整体可以大致分为三部分：</font>**

* 外层：Client通过命令行界面(CLI)向Docker Server发起请求。
* 中间层：REST API，使用HTTP协议建立Client与Server之间的通信。
* 内层：Server，可以是本地的也可以是远程的，接收并分发Client端发起的请求。

### 1.1.2. Docker组件
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-16.png) 
<center>Docker架构图二</center> 

* docker cli用来管理容器和镜像，客户端提供一个只读镜像，然后通过镜像可以创建多个容器，这些容器可以只是一个RFS(Root file system根文件系统)，也可以是一个包含了用户应用的RFS，容器在docker client中只是要给进程，两个进程之间互不可见。  
&emsp; 用户不能与server直接交互，但可以通过与容器这个桥梁来交互，由于是操作系统级别的虚拟技术，中间的损耗几乎可以不计。  
* Docker Daemon 是Docker架构中一个常驻在后台的系统进程，主要功能是在后台启动一个Server接收处理Client发送的请求。Server负责接受Client发送的请求通过路由分发调度，找到相应的Handler来执行请求。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-46.png)  
* 镜像(Image)
    * 一个只读的模板，镜像可以用来创建Docker容器。
    * 用户基于镜像来运行自己的容器。镜像是基于Union文件系统的层式结构。
    * 可以简单创建或更新现有镜像，或者直接下载使用其他人的。可以理解为生成容器的『源代码』。
* 容器(Container)
    * 容器是从镜像创建的运行实例，在启动的时候创建一层可写层作为最上层(因为镜像是只读的)。
    * 可以被启动、开始、停止、删除。每个容器都是相互隔离的、保证安全的平台。
* 仓库(Registry)
    * 集中存放镜像文件的场所，可以是公有的，也可以是私有的。
    * 最大的公开仓库是Docker Hub。
    * 国内的公开仓库包括Docker Pool等。
    * 当用户创建了自己的镜像之后就可以使用push命令将它上传到公有或者私有仓库，这样下次在另外一台机器上使用这个镜像时候，只需要从仓库上 pull 下来就可以了。
    * Docker仓库的概念跟Git类似，注册服务器可以理解为GitHub这样的托管服务。

<!-- 
&emsp; **Docker基本概念**  
* 宿主机：运行引擎的操作系统所在服务器。  

Docker Daemon守护进程：用于接收client的请求并处理请求。  
&emsp; Docker Daemon(或者Docker 服务器)用来监听 Docker API 的请求和管理 Docker 对象，比如镜像、容器、网络和卷。默认情况 docker 客户端和 docker daemon 位于同一主机，此时 daemon 监听 /var/run/docker.sock 这个 Unix 套接字文件，来获取来自客户端的 Docker 请求。当然通过配置，也可以借助网络来实现 Docker Client 和 daemon 之间的通信，默认非 TLS 端口为 2375，TLS 默认端口为 2376。  
-->

## 1.2. ~~Docker运行流程~~  
<!-- 
https://blog.csdn.net/qq_20817327/article/details/108627035
-->
