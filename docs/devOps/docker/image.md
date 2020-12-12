<!-- TOC -->

- [1. Docker组件详解](#1-docker组件详解)
    - [1.1. 镜像详解](#11-镜像详解)
        - [1.1.1. 分层构建](#111-分层构建)
        - [1.1.2. 读写层](#112-读写层)
        - [1.1.3. 联合文件系统UnionFS](#113-联合文件系统unionfs)
    - [1.2. 容器详解](#12-容器详解)
        - [1.2.1. 容器生命周期](#121-容器生命周期)
            - [1.2.1.1. 创建和运行](#1211-创建和运行)
            - [1.2.1.2. 休眠和销毁](#1212-休眠和销毁)
            - [1.2.1.3. 重启策略](#1213-重启策略)
        - [1.2.2. 容器隔离原理](#122-容器隔离原理)
            - [1.2.2.1. Namespaces](#1221-namespaces)
            - [1.2.2.2. CGroups](#1222-cgroups)
        - [1.2.3. 容器的文件系统](#123-容器的文件系统)
        - [1.2.4. 数据卷（Volume）](#124-数据卷volume)
            - [1.2.4.1. 数据卷](#1241-数据卷)
            - [1.2.4.2. 数据卷容器](#1242-数据卷容器)
        - [1.2.5. Docker宿主机与容器通信(端口映射)](#125-docker宿主机与容器通信端口映射)
            - [1.2.5.1. 自动映射端口](#1251-自动映射端口)
            - [1.2.5.2. 绑定端口到指定接口](#1252-绑定端口到指定接口)
            - [1.2.5.3. 示例](#1253-示例)
        - [1.2.6. 容器间通信](#126-容器间通信)
            - [1.2.6.1. Docker四种网络模式](#1261-docker四种网络模式)
            - [1.2.6.2. 容器间单向通信（Link）](#1262-容器间单向通信link)
            - [1.2.6.3. 容器间双向通信（bridge）](#1263-容器间双向通信bridge)
                - [1.2.6.3.1. Docker中的虚拟网桥](#12631-docker中的虚拟网桥)
                - [1.2.6.3.2. 借助网桥进行容器间通信](#12632-借助网桥进行容器间通信)
                - [1.2.6.3.3. 网桥通信原理](#12633-网桥通信原理)
            - [1.2.6.4. 不同主机间容器通信](#1264-不同主机间容器通信)

<!-- /TOC -->

# 1. Docker组件详解

<!-- 
https://mp.weixin.qq.com/s/xq9lrHqBOWjQ65-V4Jrttg

-->

<!-- 
&emsp; docker本质就是宿主机的一个进程，docker是通过namespace实现资源隔离，通过cgroup实现资源限制，通过写时复制技术（copy-on-write）实现了高效的文件操作（类似虚拟机的磁盘比如分配500g并不是实际占用物理磁盘500g）  
&emsp; 1）namespaces 名称空间  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-18.png)  
&emsp; 2）control Group 控制组  
cgroup的特点是：  　　　

* cgroup的api以一个伪文件系统的实现方式，用户的程序可以通过文件系统实现cgroup的组件管理
* cgroup的组件管理操作单元可以细粒度到线程级别，另外用户可以创建和销毁cgroup，从而实现资源载分配和再利用
* 所有资源管理的功能都以子系统的方式实现，接口统一子任务创建之初与其父任务处于同一个cgroup的控制组


&emsp; Docker虚拟化技术需要以下核心技术的支撑：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-33.png)  

&emsp; 首先，Docker 的出现一定是因为目前的后端在开发和运维阶段确实需要一种虚拟化技术解决开发环境和生产环境环境一致的问题，通过 Docker 可以将程序运行的环境也纳入到版本控制中，排除因为环境造成不同运行结果的可能。但是上述需求虽然推动了虚拟化技术的产生，但是如果没有合适的底层技术支撑，那么仍然得不到一个完美的产品。本文剩下的内容会介绍几种 Docker 使用的核心技术，如果了解它们的使用方法和原理，就能清楚 Docker 的实现原理。  
-->
&emsp; Docker中镜像是分层的，最顶层是读写层（镜像与容器的区别），其底部依赖于Linux的UnionFS文件系统；单个宿主机的多个容器是隔离的，其依赖于Linux的Namespaces、CGroups，隔离的容器需要通信、文件共享（数据持久化）。  

## 1.1. 镜像详解
### 1.1.1. 分层构建  
&emsp; **Docker镜像是分层构建的，Dockerfile中每条指令都会新建一层。**例如以下Dockerfile：  

```text
FROM ubuntu:18.04
COPY . /app
RUN make /app
CMD python /app/app.py
```
&emsp; 以上四条指令会创建四层，分别对应基础镜像、复制文件、编译文件以及入口文件，每层只记录本层所做的更改，而这些层都是只读层。**当启动一个容器，Docker会在最顶部添加读写层，在容器内做的所有更改，如写日志、修改、删除文件等，都保存到了读写层内，一般称该层为容器层，**如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-15.png)  
&emsp; 每个镜像可依赖其他镜像进行构建，每一层的镜像可被多个镜像引用，下图的镜像依赖关系，K8S镜像其实是CentOS+GCC+GO+K8S这四个软件结合的镜像。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-19.png)  

### 1.1.2. 读写层  
&emsp; 下图形象的表现出了镜像和容器的关系：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-20.png)  
&emsp; 上图中Apache应用基于emacs镜像构建，emacs基于Debian系统镜像构建，在启动为容器时，在Apache镜像层之上构造了一个可写层，对容器本身的修改操作都在可写层中进行。Debian是该镜像的基础镜像（Base Image），它提供了内核Kernel的更高级的封装。同时其他的镜像也是基于同一个内核来构建的。  
&emsp; 事实上，<font color = "lime">容器（container）和镜像（image）的最主要区别就是容器加上了顶层的读写层。</font>所有对容器的修改都发生在此层，镜像并不会被修改。容器需要读取某个文件时，直接从底部只读层去读即可，而如果需要修改某文件，则将该文件拷贝到顶部读写层进行修改，只读层保持不变。  
&emsp; **每个容器都有自己的读写层，因此多个容器可以使用同一个镜像，**另外容器被删除时，其对应的读写层也会被删除（如果希望多个容器共享或者持久化数据，可以使用Docker volume）。  
&emsp; 最后，执行命令 docker ps -s，可以看到最后有两列 size 和 virtual size。其中 size就是容器读写层占用的磁盘空间，而 virtual size 就是读写层加上对应只读层所占用的磁盘空间。如果两个容器是从同一个镜像创建，那么只读层就是100%共享，即使不是从同一镜像创建，其镜像仍然可能共享部分只读层（如一个镜像是基于另一个创建）。因此，docker 实际占用的磁盘空间远远小于virtual size 的总和。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-14.png)  
&emsp; **这种分层结构能充分共享镜像层，能大大减少镜像仓库占用的空间。**  

### 1.1.3. 联合文件系统UnionFS  
&emsp; Docker镜像分层的交互、管理需要存储驱动程序，也即联合文件系统（UnionFS）。**Docker利用UnionFS（联合文件系统）把相关镜像层的目录“联合”到同一个挂载点，呈现出一个整体。**Docker可使用多种驱动，如目前已经合并入Linux 内核、官方推荐的overlay等等，需要根据Docker以及宿主机系统的版本，进行合适的选择。  
&emsp; UnionFS有很多种，其中Docker中常用的是AUFS，这是UnionFS的升级版，除此之外还有DeviceMapper、Overlay2、ZFS和 VFS等。Docker镜像的每一层默认存放在/var/lib/docker/aufs/diff目录中，当用户启动一个容器时，Docker引擎首先在/var/lib/docker/aufs/diff中新建一个可读写层目录，然后使用UnionFS把该可读写层目录和指定镜像的各层目录联合挂载到/var/lib/docker/aufs/mnt里的一个目录中（其中指定镜像的各层目录都以只读方式挂载），通过LXC等技术进行环境隔离和资源控制，使容器里的应用仅依赖mnt目录中对应的挂载目录和文件运行起来。  
&emsp; 利用UnionFS写时复制的特点，在启动一个容器时，Docker引擎实际上只是增加了一个可写层和构造了一个Linux容器，这两者都几乎不消耗系统资源，因此Docker容器能够做到秒级启动，一台服务器上能够启动上千个Docker容器，而传统虚拟机在一台服务器上启动几十个就已经非常吃力了，而且虚拟机启动很慢，这是Docker相比于传统虚拟机的两个巨大的优势。  
&emsp; 当应用只是直接调用了内核功能来运作的情况下，应用本身就能直接作为最底层的层来构建镜像，但因为容器本身会隔绝环境，因此容器内部是无法访问宿主机里文件的（除非指定了某些目录或文件映射到容器内），这种情况下应用代码就只能使用内核的功能。  
&emsp; 但是Linux内核仅提供了进程管理、内存管理、文件系统管理等一些基础且底层的管理功能，在实际的场景中，几乎所有软件都是基于操作系统来开发的，因此往往都需要依赖操作系统的软件和运行库等，如果这些应用的下一层直接是内核，那么应用将无法运行。所以实际上应用镜像往往底层都是基于一个操作系统镜像来补足运行依赖的。  
&emsp; Docker中的操作系统镜像，与平常安装系统时用的ISO镜像不同。ISO镜像里包含了操作系统内核及该发行版系统包含的所有目录和软件，而Docker中的操作系统镜像，不包含系统内核，仅包含系统必备的一些目录（如/etc /proc等）和常用的软件和运行库等，可把操作系统镜像看作内核之上的一个应用，一个封装了内核功能，并为用户编写的应用提供运行环境的工具。   
&emsp; 应用基于这样的镜像构建，就能够利用上相应操作系统的各种软件的功能和运行库，此外，由于应用是基于操作系统镜像来构建的，就算换到另外的服务器，只要操作系统镜像中被应用使用到的功能能适配宿主机的内核，应用就能正常运行，这就是一次构建到处运行的原因。  

<!-- 

&emsp; Linux 的命名空间和控制组分别解决了不同资源隔离的问题，前者解决了进程、网络以及文件系统的隔离，后者实现了 CPU、内存等资源的隔离，但是在 Docker 中还有另一个非常重要的问题需要解决 - 也就是镜像。  
&emsp; 镜像到底是什么，它又是如何组成和组织的是作者使用Docker以来的一段时间内一直比较让作者感到困惑的问题，可以使用docker run非常轻松地从远程下载Docker的镜像并在本地运行。  
&emsp; Docker 镜像其实本质就是一个压缩包，可以使用下面的命令将一个Docker镜像中的文件导出：  

```text
$ docker export $(docker create busybox) | tar -C rootfs -xvf -
$ ls
bin  dev  etc  home proc root sys  tmp  usr  var
```
&emsp; 可以看到这个busybox镜像中的目录结构与Linux操作系统的根目录中的内容并没有太多的区别，可以说Docker镜像就是一个文件。 
-->

## 1.2. 容器详解  
&emsp; 容器是基于镜像启动起来的，是镜像的一个运行实例，容器中可以运行一个或多个进程。同时，用户可以从单个镜像上启动一个或多个容器。  
&emsp; 一个容器内的一个或多个进程的运行是依靠容器所在宿主机的内核，但是这些进程属于自己的独立的命名空间，拥有自己的root文件系统、自己的网络配置、自己的进程空间和自己的用户ID等等，所以容器内的进程相当于运行在一个隔离的环境里，就相当于运行在一个新的操作系统中一样。容器使用root文件系统大部分是由镜像提供的，还有一些是由Docker为Docker容器生成的。  

&emsp; 运行的容器是共享宿主机内核的，也就是相当于容器在执行时还是依靠主机的内核代码的。这也就意味着一个基于 Windows 的容器化应用在 Linux 主机上无法运行的。也可以简单地理解为 Windows 容器需要运行在 Windows 宿主机之上，Linux 容器需要运行在 Linux 宿主机上。Docker 推荐单个容器只运行一个应用程序或进程，但是其实也可以运行多个应用程序。  

### 1.2.1. 容器生命周期  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-4.png)  
&emsp; <font color = "lime">容器的生命周期大致可分为 4 个：创建、运行、休眠和销毁。</font>  

#### 1.2.1.1. 创建和运行
&emsp; 容器的创建和运行主要使用 docker container run 命名，比如下面的命令会从 Ubuntu:latest 这个镜像中启动 /bin/bash 这个程序。那么 /bin/bash 成为了容器中唯一运行的进程。  

    docker container run -it ubuntu:latest /bin/bash

&emsp; 当运行上述的命令之后，Docker 客户端会选择合适的 API 来调用 Docker daemon 接收到命令并搜索 Docker 本地缓存，观察是否有命令所请求的镜像。如果没有，就去查询 Docker Hub 是否存在相应镜像。找到镜像后，就将其拉取到本地，并存储在本地。一旦镜像拉取到本地之后，Docker daemon 就会创建容器并在其中运行指定应用。  

&emsp; ps -elf命令可以看到会显示两个，那么其中一个是运行ps -elf 产生的。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-22.png)  

&emsp; 假如，此时输入exit退出Bash Shell之后，那么容器也会退出（休眠）。因为容器如果不运行任何进程则无法存在，上面将容器的唯一进程杀死之后，容器也就没了。其实，当把进程的PID为1的进程杀死就会杀死容器。  

#### 1.2.1.2. 休眠和销毁
&emsp; docker container stop命令可以让容器进入休眠状态，使用docker container rm可以删除容器。删除容器的最佳方式就是先停止容器，然后再删除容器，这样可以给容器中运行的应用/进程一个停止运行并清理残留数据的机会。因为先stop的话，docker container stop命令会像容器内的PID 1进程发送 SIGTERM 信号，这样会给进程预留一个清理并优雅停止的机会。如果进程在10s的时间内没有终止，那么会发送SIGKILL信号强制停止该容器。但是docker container rm命令不会友好地发送 SIGTERM，而是直接发送SIGKILL信号。

#### 1.2.1.3. 重启策略
&emsp; 容器还可以配置重启策略，这是容器的一种自我修复能力，可以在指定事件或者错误后重启来完成自我修复。配置重启策略有两种方式，一种是命令中直接传入参数，另一种是在 Compose 文件中声明。下面阐述命令中传入参数的方式，也就是在命令中加入 --restart 标志，该标志会检查容器的退出代码，并根据退出码已经重启策略来决定。Docker 支持的重启策略包括 always、unless-stopped 和 on-failed 四种。

* always策略会一直尝试重启处于停止状态的容器，除非通过docker container stop命令明确将容器停止。另外，当daemon重启的时候，被docker container stop停止的设置了 always策略的容器也会被重启。

        $ docker container run --it --restart always apline sh 
        # 过几秒之后，在终端中输入 exit，过几秒之后再来看一下。照理来说应该会处于stop状态，但是会发现又处于运行状态了。

* unless-stopped 策略和 always 策略是差不多的，最大的区别是，docker container stop停止的容器在daemon重启之后不会被重启。
* on-failure 策略会在退出容器并且返回值不会 0 的时候，重启容器。如果容器处于 stopped 状态，那么 daemon 重启的时候也会被重启。另外，on-failure 还接受一个可选的重启次数参数，如--restart=on-failure:5 表示最多重启 5 次。

### 1.2.2. 容器隔离原理  
#### 1.2.2.1. Namespaces  
&emsp; <font color = "lime">命名空间（namespaces）是 Linux 提供的用于分离进程树、网络接口、挂载点以及进程间通信等资源的方法。</font>在日常使用 Linux 或者 macOS 时，并没有运行多个完全分离的服务器的需要，但是如果在服务器上启动了多个服务，这些服务其实会相互影响的，每一个服务都能看到其他服务的进程，也可以访问宿主机器上的任意文件，这是很多时候不愿意看到的，更希望运行在同一台机器上的不同服务能做到完全隔离，就像运行在多台不同的机器上一样。 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-34.png)  
&emsp; 在这种情况下，一旦服务器上的某一个服务被入侵，那么入侵者就能够访问当前机器上的所有服务和文件，这也是不想看到的，而 Docker 其实就通过 Linux 的 Namespaces 对不同的容器实现了隔离。  
&emsp; Linux 的命名空间机制提供了以下七种不同的命名空间，包括 CLONE_NEWCGROUP、CLONE_NEWIPC、CLONE_NEWNET、CLONE_NEWNS、CLONE_NEWPID、CLONE_NEWUSER 和 CLONE_NEWUTS，通过这七个选项能在创建新的进程时设置新进程应该在哪些资源上与宿主机器进行隔离。  

#### 1.2.2.2. CGroups  
&emsp; Linux的命名空间为新创建的进程隔离了文件系统、网络并与宿主机器之间的进程相互隔离，但是命名空间并不能够提供物理资源上的隔离，比如 CPU 或者内存，如果在同一台机器上运行了多个对彼此以及宿主机器一无所知的『容器』，这些容器却共同占用了宿主机器的物理资源。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-35.png)  
&emsp; 如果其中的某一个容器正在执行 CPU 密集型的任务，那么就会影响其他容器中任务的性能与执行效率，导致多个容器相互影响并且抢占资源。如何对多个容器的资源使用进行限制就成了解决进程虚拟资源隔离之后的主要问题，<font color = "red">而Control Groups（简称 CGroups）就是能够隔离宿主机器上的物理资源，例如 CPU、内存、磁盘 I/O 和网络带宽。</font>  
&emsp; 每一个CGroup都是一组被相同的标准和参数限制的进程，不同的CGroup之间是有层级关系的，也就是说它们之间可以从父类继承一些用于限制资源使用的标准和参数。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-36.png)  
&emsp; Linux 的 CGroup 能够为一组进程分配资源，也就是在上面提到的 CPU、内存、网络带宽等资源，通过对资源的分配，CGroup 能够提供以下的几种功能：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-37.png)  

    在CGroup中，所有的任务就是一个系统的一个进程，而CGroup就是一组按照某种标准划分的进程，在CGroup这种机制中，所有的资源控制都是以CGroup作为单位实现的，每一个进程都可以随时加入一个CGroup也可以随时退出一个CGroup。

### 1.2.3. 容器的文件系统   
&emsp; 容器会共享其所在主机的操作系统/内核（容器执行使用共享主机的内核代码），<font color = "red">但是容器内运行的进程所使用的是容器自己的文件系统，也就是容器内部的进程访问数据时访问的是容器的文件系统。</font>当从一个镜像启动容器的时候，除了把镜像当成容器的文件系统一部分之外，Docker还会在该镜像的最顶层加载一个可读写文件系统，容器中运行的程序就是在这个读写层中执行的。  
&emsp; 使用Docker，启动容器，会新建两层内容。这两层分别为 Docker 容器的初始层（Init Layer）与可读写层（Read-Write Layer）：  

* 初始层中大多是初始化容器环境时，与容器相关的环境信息，如容器主机名，主机 host 信息以及域名服务文件等。  
* 再来看可读写层，这一层的作用非常大，Docker 的镜像层以及顶上的两层加起来，Docker 容器内的进程只对可读写层拥有写权限，其他层对进程而言都是只读的（Read-Only）。比如想修改一个文件，这个文件会从该读写层下面的只读层复制到该读写层，该文件的只读版本仍然存在，但是已经被读写层中的该文件副本所隐藏了。这种机制被称为写时复制（copy on write）（在 AUFS 等文件系统下，写下层镜像内容就会涉及 COW （Copy-on-Write）技术）。另外，关于 VOLUME 以及容器的 hosts、hostname 、resolv.conf 文件等都会挂载到这里。需要额外注意的是，虽然Docker容器有能力在可读写层看到 VOLUME 以及 hosts 文件等内容，但那都仅仅是挂载点，真实内容位于宿主机上。  

&emsp; 在运行阶段时，容器产生的新文件、文件的修改都会在可读写层上，当停止容器（stop）运行之后并不会被损毁，但是删除容器会丢弃其中的数据。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-23.png)  

### 1.2.4. 数据卷（Volume）
<!--
https://www.docker.org.cn/dockerppt/117.html


容器间数据共享的原理，是在宿主机上开辟一块空间，该空间会被其他容器同时共享。我们可以把页面文件存放一份到宿主机的这块磁盘空间中，其他容器挂载这个文件即可。以后更新所有的web服务的页面，我们只需要更改宿主机的这份页面文件即可。

容器间共享数据-挂载

方法一、docker run --name 容器名 -v 宿主机路径:容器内挂载路径 镜像名。例如docker run --name myweb01 -v /usr/webapps:/usr/local/tomcat/webapps tomcat。

方法二、通过设置挂载点，我们可以创建一个用于挂载的容器，其他容器设置参照该容器的挂载信息，进行挂载。例如我们创建一个webpage的容器，设置挂载信息，无需运行该容器：  
docker create --name webpage -v /webapps:/tomcat/webapps tomcat /bin/true，接着我们创建其他容器参照该容器的挂载信息进行挂载：docker run --volumes-from webpage --name myweb02 -d tomcat,我们创建的myweb02的容器的挂载信息，就和webpage的挂载信息相同。如果需要修改挂载点，只需要修改webpage的挂载信息即可。  
-->
&emsp; docker通过数据卷（Volume）来进行数据持久化、文件共享。有数据卷和数据卷容器两种方式。  

#### 1.2.4.1. 数据卷
&emsp; 数据卷是一个或多个容器专门指定绕过Union File System的目录，为持续性或共享数据提供一些有用的功能：  

* 数据卷可以在容器间共享和重用
* 数据卷数据改变是直接修改的
* 数据卷数据改变不会被包括在容器中
* 数据卷是持续性的，直到没有容器使用它们

#### 1.2.4.2. 数据卷容器  
&emsp; 通过设置挂载点，可以创建一个用于挂载的容器，其他容器设置参照该容器的挂载信息，进行挂载。  
&emsp; 例如创建一个webpage的容器，设置挂载信息，无需运行该容器：  
docker create --name webpage -v /webapps:/tomcat/webapps tomcat /bin/true，接着创建其他容器参照该容器的挂载信息进行挂载：docker run --volumes-from webpage --name myweb02 -d tomcat，创建的myweb02的容器的挂载信息，就和webpage的挂载信息相同。如果需要修改挂载点，只需要修改webpage的挂载信息即可。 
 
### 1.2.5. Docker宿主机与容器通信(端口映射)  
<!-- 
~~
Docker学习笔记：Docker 端口映射 
https://www.docker.org.cn/dockerppt/110.html
-->
&emsp; 容器运行在宿主机上，如果外网能够访问容器，才能够使用它提供的服务。Docker容器与宿主机进行通信可以通过映射容器的端口到宿主机上。  

#### 1.2.5.1. 自动映射端口  
&emsp; -P使用时需要指定--expose选项，指定需要对外提供服务的端口

```text
$ sudo docker run -t -P --expose 22 --name server  ubuntu:14.04
```
&emsp; 使用docker run -P自动绑定所有对外提供服务的容器端口，映射的端口将会从没有使用的端口池中 (49000..49900) 自动选择，你可以通过docker ps、docker inspect \<container_id>或者docker port \<container_id> \<port>确定具体的绑定信息。 

#### 1.2.5.2. 绑定端口到指定接口  
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

#### 1.2.5.3. 示例  
&emsp; 例如，使用如下命令启动一个容器  

    docker run -p 8080:80 --name test nginx  

&emsp; 使用-p参数将容器的80端口映射到宿主机的8080端口，这样就可以通过curl localhost:8080访问到容器上80端口的服务了。另外一个参数-P可以将容器的端口映射到宿主机的高位随机端口上，而不需要手动指定。  

### 1.2.6. 容器间通信  
<!-- 

Docker容器网络
http://www.itmuch.com/docker/15-docker-network/
network命令
http://www.itmuch.com/docker/16-docker-network-command/
默认bridge网络中配置DNS
http://www.itmuch.com/docker/17-docker-bridge-dns/
用户定义网络中的内嵌DNS服务器
http://www.itmuch.com/docker/18-docker-user-network-embeded-dns/
-->

#### 1.2.6.1. Docker四种网络模式  
<!-- 
初探Docker的网络模式 
https://www.docker.org.cn/docker/187.html
-->
&emsp; docker run 创建 Docker 容器时，可以用 --net 选项指定容器的网络模式，Docker 有以下 4 种网络模式：

* host 模式，使用 --net=host 指定。
* container 模式，使用 --net=container:NAMEorID 指定。
* none 模式，使用 --net=none 指定。
* bridge 模式，使用 --net=bridge 指定，默认设置。

&emsp; 可以使用docker network ls查看当前主机的网络模式。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-44.png)  

* host 模式  
&emsp; 如果启动容器的时候使用 host 模式，那么这个容器将不会获得一个独立的 Network Namespace，而是和宿主机共用一个 Network Namespace。容器将不会虚拟出自己的网卡，配置自己的 IP 等，而是使用宿主机的 IP 和端口。  
&emsp; 例如，在 10.10.101.105/24 的机器上用 host 模式启动一个含有 web 应用的 Docker 容器，监听 tcp 80 端口。当容器中执行任何类似 ifconfig 命令查看网络环境时，看到的都是宿主机上的信息。而外界访问容器中的应用，则直接使用 10.10.101.105:80 即可，不用任何 NAT 转换，就如直接跑在宿主机中一样。但是，容器的其他方面，如文件系统、进程列表等还是和宿主机隔离的。  
* container 模式  
&emsp; 这个模式指定新创建的容器和已经存在的一个容器共享一个 Network Namespace，而不是和宿主机共享。新创建的容器不会创建自己的网卡，配置自己的 IP，而是和一个指定的容器共享 IP、端口范围等。同样，两个容器除了网络方面，其他的如文件系统、进程列表等还是隔离的。两个容器的进程可以通过 lo 网卡设备通信。  
* none模式  
&emsp; 这个模式和前两个不同。在这种模式下，Docker 容器拥有自己的 Network Namespace，但是，并不为 Docker容器进行任何网络配置。也就是说，这个Docker容器没有网卡、IP、路由等信息。需要为Docker容器添加网卡、配置IP等。  
* bridge模式  
&emsp; bridge 模式是 Docker 默认的网络设置，此模式会为每一个容器分配 Network Namespace、设置 IP 等，并将一个主机上的 Docker 容器连接到一个虚拟网桥上。当 Docker server 启动时，会在主机上创建一个名为 docker0 的虚拟网桥，此主机上启动的 Docker 容器会连接到这个虚拟网桥上。虚拟网桥的工作方式和物理交换机类似，这样主机上的所有容器就通过交换机连在了一个二层网络中。接下来就要为容器分配 IP 了，Docker 会从 RFC1918 所定义的私有 IP 网段中，选择一个和宿主机不同的IP地址和子网分配给 docker0，连接到 docker0 的容器就从这个子网中选择一个未占用的 IP 使用。如一般 Docker 会使用 172.17.0.0/16 这个网段，并将 172.17.42.1/16 分配给 docker0 网桥（在主机上使用 ifconfig 命令是可以看到 docker0 的，可以认为它是网桥的管理接口，在宿主机上作为一块虚拟网卡使用）   

#### 1.2.6.2. 容器间单向通信（Link）  
&emsp; 在docker环境下，容器创建后，都会默认分配一个虚拟ip，该ip无法从外界直接访问，但是在docker环境下各个ip直接是互通互联的。下图假设Tomcat分配的虚拟ip为107.1.31.22，Mysql分配的虚拟ip为107.1.31.24。  
&emsp; 虽然在Docker环境下可以通过虚拟ip互相访问，但是局限性很大，原因是容器是随时可能宕机，重启的，宕机重启后会为容器重新分配ip，这样原来直接通信的ip就消失了。所以容器间通信不建议通过ip进行通信。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-42.png)  
&emsp; 可以通过为容器命名，通过名称通信，这样无论该容器重启多少次，ip如何改变，都不会存在通信不可用的情形。  

* 通过docker run -d --name myweb tomcat命令，使用tomcat镜像运行一个名称为myweb的容器。通过docker run -d --name mydatabases mysql命令，使用mysql镜像运行一个名称为mydatabases的容器  
* 通过docker inspect myweb查看myweb容器的详细配置。其中NetworkSettings下的IPAddress的信息即为docker为该容器分配的虚拟ip地址  

&emsp; 记录mydatabases容器的ip地址，进入myweb容器，用ping命令访问mydatabases的ip。发现是没问题的，说明docker之间运用虚拟ip地址可以直接互相访问。  

&emsp; **配置容器间通过名称访问**

* 把之前的myweb容器。运用docker rm myweb移除掉，保留mydatabases容器。通过docker run -d --name myweb --link mydatabases tomcat来运行容器。其中--link是指定该容器需要和名称为databases的容器通信。  
* 进入myweb容器docker exec -it myweb sh,运行命令ping mydatabases，即可通信。配置mysql连接的时候，把ip换成mydatabases即可，docker会自动维护mydatabases和该容器ip的映射，即使该容器的ip改变也不会影响访问。  

#### 1.2.6.3. 容器间双向通信（bridge）  
&emsp; 通过上文，配置容器间互相link，也是可以实现双向通信的，但是有点麻烦。Docker提供了网桥，用来快速实现容器间双向通信。  

##### 1.2.6.3.1. Docker中的虚拟网桥  
&emsp; docker网桥组件概览图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-41.png)  
&emsp; Docker中的网桥也是虚拟出来的，网桥的主要用途是用来实现docker环境和外部的通信。我们在某个容器内部ping外部的ip例如百度。是可以ping通的。实际上是容器发出的数据包，通过虚拟网桥，发送给宿主机的物理网卡，实际上是借助物理网卡与外界通信的，反之物理网卡也会通过虚拟网桥把相应的数据包送回给相应的容器。  

##### 1.2.6.3.2. 借助网桥进行容器间通信
&emsp; 可以借助网桥实现容器间的双向通信。docker虚拟网桥的另一个作用是为容器在网络层面上进行分组，把不同容器都绑定到网桥上，这样容器间就可以天然互联互通。  

* docker run -d --name myweb tomcat运行myweb容器；docker run -d -it --name mydatabases centos sh交互模式挂起一个databases容器（这里用一个linux容器模拟数据库服务器）。由于没配置网桥，此时容器间无法通过名称互相通信  
* docker network ls列出docker网络服务明细，其中bridge的条目就是docker默认的网桥。接着我们新建立一个网桥docker network create -d bridge my-bridge命名为my-bridge  
* 把容器与新建立的my-bridge网桥进行绑定: docker network connect my-bridge myweb,同理: docker network connect my-bridge mydatabases。需要让哪些容器互联互通，就把容器绑定到该网桥上,用来标识这些容器同处在my-bridge网桥的分组中。至此容器间可以通过名称互相访问  

```text
~/ docker network ls
NETWORK ID          NAME                DRIVER              SCOPE
a31f18ae4b97        bridge              bridge              local
9e311308c3ce        host                host                local
2c5f89509739        none                null                local
~/ docker network create -d bridge my-bridge
5e678ed577b120f0d95e87ce43d44bab8e15e47f4002428168cad61120c54cc7
~/ docker network ls
NETWORK ID          NAME                DRIVER              SCOPE
a31f18ae4b97        bridge              bridge              local
9e311308c3ce        host                host                local
5e678ed577b1        my-bridge           bridge              local
2c5f89509739        none                null                local
~/ docker network connect my-bridge myweb
~/ docker network connect my-bridge mydatabases
~/ docker ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
92888ef080a9        centos              "sh"                2 minutes ago       Up 2 minutes                            mydatabases
03fc2187d4ef        tomcat              "catalina.sh run"   43 minutes ago      Up 43 minutes       8080/tcp            myweb
~/ docker exec -it 03fc2187d4ef sh
# ping mydatabases
PING mydatabases (172.18.0.3) 56(84) bytes of data.
64 bytes from mydatabases.my-bridge (172.18.0.3): icmp_seq=1 ttl=64 time=0.278 ms
64 bytes from mydatabases.my-bridge (172.18.0.3): icmp_seq=2 ttl=64 time=0.196 ms
64 bytes from mydatabases.my-bridge (172.18.0.3): icmp_seq=3 ttl=64 time=0.417 ms
^C
--- mydatabases ping statistics ---
3 packets transmitted, 3 received, 0% packet loss, time 55ms
rtt min/avg/max/mdev = 0.196/0.297/0.417/0.091 ms
# exit
```
##### 1.2.6.3.3. 网桥通信原理  
&emsp; 网桥为什么可以实现容器间的互联互通？实际上，每当创建一个网桥，docker都会在宿主机上安装一个虚拟网卡，该虚拟网卡也充当了一个网关的作用。与该网桥（虚拟网关）绑定的容器，相当于处在一个局域网，所以可以通信。虚拟网卡毕竟是虚拟的，如果容器想要和外部通信，仍然需要借助外部（宿主机）的物理网卡。虚拟网卡的数据包进行地址转换，转换为物理网卡的数据包发送出去，反之外部和内部容器通信，也需要进行数据包地址转换。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-44.png)  

#### 1.2.6.4. 不同主机间容器通信  
<!-- 
docker同宿主机容器和不同宿主机容器之间怎么通信？
https://blog.51cto.com/2367685/2349762
Docker学习笔记：Docker 网络配置 
https://www.docker.org.cn/dockerppt/111.html
-->

