<!-- TOC -->

- [1. Docker](#1-docker)
    - [1.1. 容器化技术](#11-容器化技术)
    - [1.2. Docker简介](#12-docker简介)
    - [1.3. Dokcer底层原理](#13-dokcer底层原理)
    - [1.4. Docker体系结构](#14-docker体系结构)
    - [1.5. Docker基本概念](#15-docker基本概念)
    - [1.6. 镜像详解](#16-镜像详解)
    - [1.7. 容器详解](#17-容器详解)
        - [1.7.1. 容器生命周期](#171-容器生命周期)
        - [1.7.2. 容器数据卷](#172-容器数据卷)
        - [1.7.3. 容器通信](#173-容器通信)
            - [1.7.3.1. Docker宿主机与容器通信](#1731-docker宿主机与容器通信)
            - [1.7.3.2. Docker同宿主机容器和不同宿主机容器之间怎么通信？](#1732-docker同宿主机容器和不同宿主机容器之间怎么通信)
                - [1.7.3.2.1. 同宿主机容器通信](#17321-同宿主机容器通信)
                - [1.7.3.2.2. 容器连接](#17322-容器连接)
                - [1.7.3.2.3. 不同宿主机容器通信](#17323-不同宿主机容器通信)

<!-- /TOC -->



# 1. Docker

<!-- 
https://mp.weixin.qq.com/s/xq9lrHqBOWjQ65-V4Jrttg
-->

## 1.1. 容器化技术  
&emsp; 容器和虚拟机  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-11.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-12.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-13.png)  
&emsp; <font color = "red">传统虚拟化是在硬件层面实现虚拟化，需要有额外的虚拟机管理应用和虚拟机操作系统层；而Docker容器是在操作系统层面实现虚拟化，直接复用本地主机操作系统，更加轻量级。</font>  
&emsp; 虚拟机运行的是一个完成的操作系统，通过虚拟机管理程序对主机资源进行虚拟访问，相比之下需要的资源更多。  
&emsp; 容器是在本机运行，并与其他容器共享主机的内核，它运行的一个独立的进程，不占用其他任何可执行文件的内存，非常轻量。  

1. 虚拟机：虚拟化硬件 虚拟机 Virtual Machine 指通过软件模拟的具有完整硬件系统功能的、运行在一个完全隔离环境中的完整计算机系统。在实体计算机中能够完成的工作在虚拟机中都能够实现。  
&emsp; 在计算机中创建虚拟机时，需要将实体机的部分硬盘和内存容量作为虚拟机的硬盘和内存容量。每个虚拟机都有独立的 CMOS、硬盘和操作系统，可以像使用实体机一样对虚拟机进行操作。在容器技术之前，业界的网红是虚拟机。  
&emsp; 虚拟机技术的代表，是 VMWare 和 OpenStack。更多请参看百科虚拟机。  
2. 容器：将操作系统层虚拟化，是一个标准的软件单元  
&emsp; 随处运行：容器可以将代码与配置文件和相关依赖库进行打包，从而确保在任何环境下的运行都是一致的。  
&emsp; 高资源利用率：容器提供进程级的隔离，因此可以更加精细地设置 CPU 和内存的使用率，进而更好地利用服务器的计算资源。  
&emsp; 快速扩展：每个容器都可作为单独的进程予以运行，并且可以共享底层操作系统的系统资源，这样一来可以加快容器的启动和停止效率。  
3. 区别与联系  
&emsp; 虚拟机虽然可以隔离出很多「子电脑」，但占用空间更大，启动更慢。虚拟机软件可能还要花钱，例如VMWare；  
&emsp; 容器技术不需要虚拟出整个操作系统，只需要虚拟一个小规模的环境，类似「沙箱」；  
&emsp; 运行空间，虚拟机一般要几 GB 到 几十 GB 的空间，而容器只需要 MB 级甚至 KB 级；  

&emsp; 来看一下对比数据：  

|特性	|虚拟机	|容器|
|---|---|----|
|隔离级别	|操作系统级	|进程|
|隔离策略|	Hypervisor（虚拟机监控器）|	Cgroups（控制组群）|
|系统资源	|5 ～ 15%	|0 ～ 5%|
|启动时间	|分钟级	|秒级|
|镜像存储	|GB - TB	|KB - MB|
|集群规模	|上百	|上万|
|高可用策略	|备份、容灾、迁移	|弹性、负载、动态|

&emsp; 与虚拟机相比，容器更轻量且速度更快，因为它利用了 Linux 底层操作系统在隔离的环境中运行。虚拟机的 Hypervisor 创建了一个非常牢固的边界，以防止应用程序突破它，而容器的边界不那么强大。
&emsp; 物理机部署不能充分利用资源，造成资源浪费。虚拟机方式部署，虚拟机本身会占用大量资源，导致资源浪费，另外虚拟机性能也很差。而容器化部署比较灵活，且轻量级，性能较好。
&emsp; 虚拟机属于虚拟化技术，而 Docker 这样的容器技术，属于轻量级的虚拟化。


## 1.2. Docker简介  

&emsp; Docker是提供应用打包部署与运行应用的容器化平台。 

* 开源的应用容器引擎，基于Go语言开发  
* 容器是完全使用沙箱机制，容器开销极低  
* Docker就是容器化技术的代名词	
* Docker也具备一定虚拟化职能

![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-1.png)  

<!-- 
https://mp.weixin.qq.com/s/RvURRnoSFPywtR8Af7IZ-g
https://mp.weixin.qq.com/s/PM6K3j8bqBbbwtt4S4uyEw
-->

## 1.3. Dokcer底层原理  
<!-- 
https://www.jianshu.com/p/e1f7b8d5184c
http://dockone.io/article/2941
-->


## 1.4. Docker体系结构  
<!-- 
https://mp.weixin.qq.com/s/RvURRnoSFPywtR8Af7IZ-g
-->
&emsp; 一个完整的Docker基本架构由如下几个部分构成：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-16.png)  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-2.png)  

&emsp; Docker 是一个客户-服务器（C/S）架构的程序。Docker 客户端只需要向 Docker 服务器或守护进程发出请求，服务器或守护进程将完成所有工作并返回结果。Docker 提供了一个命令行工具和一整套 RESTful API。你可以在同一台宿主机上运行 Docker 守护进程和客户端，也可以从本地的 Docker 客户端连接到运行在另一台宿主机上的远程 Docker 守护进程。Docker 以 root 权限运行它的守护进程，来处理普通用户无法完成的操作（如挂载文件系统）。Docker 程序是 Docker 守护进程的客户端程序，同样也需要以 root 身份运行。  


## 1.5. Docker基本概念  

* Docker客户端：也就是在窗口中执行的命令，都是客户端。    
* Docker Daemon守护进程：用于去接受client的请求并处理请求。  
&emsp; Docker Daemon（或者Docker 服务器）用来监听 Docker API 的请求和管理 Docker 对象，比如镜像、容器、网络和卷。默认情况 docker 客户端和 docker daemon 位于同一主机，此时 daemon 监听 /var/run/docker.sock 这个 Unix 套接字文件，来获取来自客户端的 Docker 请求。当然通过配置，也可以借助网络来实现 Docker Client 和 daemon 之间的通信，默认非 TLS 端口为 2375，TLS 默认端口为 2376。  

* 引擎：创建和管理容器的工具，通过读取镜像来生成容器，并负责从仓库拉取镜像或提交镜像到仓库中；  
* 镜像(Image)
    * 一个只读的模板，镜像可以用来创建 Docker 容器
    * 用户基于镜像来运行自己的容器。镜像是基于 Union 文件系统的层式结构
    * 可以简单创建或更新现有镜像，或者直接下载使用其他人的。可以理解为生成容器的『源代码』

* 容器(Container)
    * 容器是从镜像创建的运行实例，在启动的时候创建一层可写层作为最上层（因为镜像是只读的）
    * 可以被启动、开始、停止、删除。每个容器都是相互隔离的、保证安全的平台
    * 可以把容器看做是一个简易版的 Linux 环境（包括root用户权限、进程空间、用户空间和网络空间等）和运行在其中的应用程序

* 仓库(Registry)
    * 集中存放镜像文件的场所，可以是公有的，也可以是私有的
    * 最大的公开仓库是 Docker Hub
    * 国内的公开仓库包括 Docker Pool 等
    * 当用户创建了自己的镜像之后就可以使用 push 命令将它上传到公有或者私有仓库，这样下次在另外一台机器上使用这个镜像时候，只需要从仓库上 pull 下来就可以了
    * Docker 仓库的概念跟 Git 类似，注册服务器可以理解为 GitHub 这样的托管服务
* 宿主机：运行引擎的操作系统所在服务器。  

&emsp; 另外Docker采用的是客户端/服务器架构，客户端只需要向 Docker 服务器或守护进程发出请求即可完成各类操作。  


## 1.6. 镜像详解

<!-- 
https://mp.weixin.qq.com/s/PM6K3j8bqBbbwtt4S4uyEw
-->
&emsp; Docker镜像是分层构建的，Dockerfile 中每条指令都会新建一层。例如以下 Dockerfile：  

```text
FROM ubuntu:18.04
COPY . /app
RUN make /app
CMD python /app/app.py
```
&emsp; 以上四条指令会创建四层，分别对应基础镜像、复制文件、编译文件以及入口文件，每层只记录本层所做的更改，而这些层都是只读层。当启动一个容器，Docker 会在最顶部添加读写层，在容器内做的所有更改，如写日志、修改、删除文件等，都保存到了读写层内，一般称该层为容器层，如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-15.png)  
&emsp; 事实上，容器（container）和镜像（image）的最主要区别就是容器加上了顶层的读写层。所有对容器的修改都发生在此层，镜像并不会被修改，也即前面说的 COW(copy-on-write)技术。容器需要读取某个文件时，直接从底部只读层去读即可，而如果需要修改某文件，则将该文件拷贝到顶部读写层进行修改，只读层保持不变。  
&emsp; 每个容器都有自己的读写层，因此多个容器可以使用同一个镜像，另外容器被删除时，其对应的读写层也会被删除（如果你希望多个容器共享或者持久化数据，可以使用 Docker volume）。  
&emsp; 最后，执行命令 docker ps -s，可以看到最后有两列 size 和 virtual size。其中 size就是容器读写层占用的磁盘空间，而 virtual size 就是读写层加上对应只读层所占用的磁盘空间。如果两个容器是从同一个镜像创建，那么只读层就是 100%共享，即使不是从同一镜像创建，其镜像仍然可能共享部分只读层（如一个镜像是基于另一个创建）。因此，docker 实际占用的磁盘空间远远小于 virtual size 的总和。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-14.png)  

&emsp; 以上就是Docker镜像分层的主要内容，至于这些层的交互、管理就需要存储驱动程序，也即联合文件系统（UnionFS）。Docker 可使用多种驱动，如目前已经合并入 Linux 内核、官方推荐的overlay， 曾在 Ubuntu、Debian等发行版中得到广泛使用的 AUFS，以及devicemapper、zfs等等，需要根据 Docker以及宿主机系统的版本，进行合适的选择。  

## 1.7. 容器详解
### 1.7.1. 容器生命周期  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-4.png)  

### 1.7.2. 容器数据卷
&emsp; 容器数据卷：持久化。docker运行产生的数据持久化.

### 1.7.3. 容器通信  


#### 1.7.3.1. Docker宿主机与容器通信  
&emsp; 容器运行在宿主机上，如果外网能够访问容器，才能够使用它提供的服务。  

&emsp; Docker容器与宿主机进行通信可以通过映射容器的端口到宿主机上。例如，使用如下命令启动一个容器  

    docker run -p 8080:80 --name test nginx  

&emsp; 使用 -p 参数将容器的 80 端口映射到宿主机的 8080 端口，这样就可以通过curl localhost:8080访问到容器上 80 端口的服务了。另外一个参数 -P 可以将容器的端口映射到宿主机的高位随机端口上，而不需要手动指定。  

#### 1.7.3.2. Docker同宿主机容器和不同宿主机容器之间怎么通信？

<!-- 
docker同宿主机容器和不同宿主机容器之间怎么通信？
https://blog.51cto.com/2367685/2349762
--> 

##### 1.7.3.2.1. 同宿主机容器通信  
......


##### 1.7.3.2.2. 容器连接  
......

##### 1.7.3.2.3. 不同宿主机容器通信
......




