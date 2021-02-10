<!-- TOC -->

- [1. 镜像详解](#1-镜像详解)
    - [1.1. 分层构建](#11-分层构建)
    - [1.2. 读写层](#12-读写层)
    - [1.3. 联合文件系统UnionFS](#13-联合文件系统unionfs)

<!-- /TOC -->

<!-- 
https://mp.weixin.qq.com/s/xq9lrHqBOWjQ65-V4Jrttg

-->

<!-- 
&emsp; docker本质就是宿主机的一个进程，docker是通过namespace实现资源隔离，通过cgroup实现资源限制，通过写时复制技术(copy-on-write)实现了高效的文件操作(类似虚拟机的磁盘比如分配500g并不是实际占用物理磁盘500g)  
&emsp; 1)namespaces 名称空间  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-18.png)  
&emsp; 2)control Group 控制组  
cgroup的特点是：  　　　

* cgroup的api以一个伪文件系统的实现方式，用户的程序可以通过文件系统实现cgroup的组件管理
* cgroup的组件管理操作单元可以细粒度到线程级别，另外用户可以创建和销毁cgroup，从而实现资源载分配和再利用
* 所有资源管理的功能都以子系统的方式实现，接口统一子任务创建之初与其父任务处于同一个cgroup的控制组


&emsp; Docker虚拟化技术需要以下核心技术的支撑：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-33.png)  

&emsp; 首先，Docker 的出现一定是因为目前的后端在开发和运维阶段确实需要一种虚拟化技术解决开发环境和生产环境环境一致的问题，通过 Docker 可以将程序运行的环境也纳入到版本控制中，排除因为环境造成不同运行结果的可能。但是上述需求虽然推动了虚拟化技术的产生，但是如果没有合适的底层技术支撑，那么仍然得不到一个完美的产品。本文剩下的内容会介绍几种 Docker 使用的核心技术，如果了解它们的使用方法和原理，就能清楚 Docker 的实现原理。  
-->
&emsp; Docker中镜像是分层的，最顶层是读写层(镜像与容器的区别)，其底部依赖于Linux的UnionFS文件系统；单个宿主机的多个容器是隔离的，其依赖于Linux的Namespaces、CGroups，隔离的容器需要通信、文件共享(数据持久化)。  

# 1. 镜像详解

## 1.1. 分层构建  
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

## 1.2. 读写层  
&emsp; 下图形象的表现出了镜像和容器的关系：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-20.png)  
&emsp; 上图中Apache应用基于emacs镜像构建，emacs基于Debian系统镜像构建，在启动为容器时，在Apache镜像层之上构造了一个可写层，对容器本身的修改操作都在可写层中进行。Debian是该镜像的基础镜像(Base Image)，它提供了内核Kernel的更高级的封装。同时其他的镜像也是基于同一个内核来构建的。  
&emsp; 事实上，<font color = "lime">容器(container)和镜像(image)的最主要区别就是容器加上了顶层的读写层。</font>所有对容器的修改都发生在此层，镜像并不会被修改。容器需要读取某个文件时，直接从底部只读层去读即可，而如果需要修改某文件，则将该文件拷贝到顶部读写层进行修改，只读层保持不变。  
&emsp; **每个容器都有自己的读写层，因此多个容器可以使用同一个镜像，**另外容器被删除时，其对应的读写层也会被删除(如果希望多个容器共享或者持久化数据，可以使用Docker volume)。  
&emsp; 最后，执行命令 docker ps -s，可以看到最后有两列 size 和 virtual size。其中 size就是容器读写层占用的磁盘空间，而 virtual size 就是读写层加上对应只读层所占用的磁盘空间。如果两个容器是从同一个镜像创建，那么只读层就是100%共享，即使不是从同一镜像创建，其镜像仍然可能共享部分只读层(如一个镜像是基于另一个创建)。因此，docker 实际占用的磁盘空间远远小于virtual size 的总和。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-14.png)  
&emsp; **这种分层结构能充分共享镜像层，能大大减少镜像仓库占用的空间。**  

## 1.3. 联合文件系统UnionFS  
&emsp; Docker镜像分层的交互、管理需要存储驱动程序，也即联合文件系统(UnionFS)。**Docker利用UnionFS(联合文件系统)把相关镜像层的目录“联合”到同一个挂载点，呈现出一个整体。**Docker可使用多种驱动，如目前已经合并入Linux 内核、官方推荐的overlay等等，需要根据Docker以及宿主机系统的版本，进行合适的选择。  
&emsp; UnionFS有很多种，其中Docker中常用的是AUFS，这是UnionFS的升级版，除此之外还有DeviceMapper、Overlay2、ZFS和 VFS等。Docker镜像的每一层默认存放在/var/lib/docker/aufs/diff目录中，当用户启动一个容器时，Docker引擎首先在/var/lib/docker/aufs/diff中新建一个可读写层目录，然后使用UnionFS把该可读写层目录和指定镜像的各层目录联合挂载到/var/lib/docker/aufs/mnt里的一个目录中(其中指定镜像的各层目录都以只读方式挂载)，通过LXC等技术进行环境隔离和资源控制，使容器里的应用仅依赖mnt目录中对应的挂载目录和文件运行起来。  
&emsp; 利用UnionFS写时复制的特点，在启动一个容器时，Docker引擎实际上只是增加了一个可写层和构造了一个Linux容器，这两者都几乎不消耗系统资源，因此Docker容器能够做到秒级启动，一台服务器上能够启动上千个Docker容器，而传统虚拟机在一台服务器上启动几十个就已经非常吃力了，而且虚拟机启动很慢，这是Docker相比于传统虚拟机的两个巨大的优势。  
&emsp; 当应用只是直接调用了内核功能来运作的情况下，应用本身就能直接作为最底层的层来构建镜像，但因为容器本身会隔绝环境，因此容器内部是无法访问宿主机里文件的(除非指定了某些目录或文件映射到容器内)，这种情况下应用代码就只能使用内核的功能。  
&emsp; 但是Linux内核仅提供了进程管理、内存管理、文件系统管理等一些基础且底层的管理功能，在实际的场景中，几乎所有软件都是基于操作系统来开发的，因此往往都需要依赖操作系统的软件和运行库等，如果这些应用的下一层直接是内核，那么应用将无法运行。所以实际上应用镜像往往底层都是基于一个操作系统镜像来补足运行依赖的。  
&emsp; Docker中的操作系统镜像，与平常安装系统时用的ISO镜像不同。ISO镜像里包含了操作系统内核及该发行版系统包含的所有目录和软件，而Docker中的操作系统镜像，不包含系统内核，仅包含系统必备的一些目录(如/etc /proc等)和常用的软件和运行库等，可把操作系统镜像看作内核之上的一个应用，一个封装了内核功能，并为用户编写的应用提供运行环境的工具。   
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

