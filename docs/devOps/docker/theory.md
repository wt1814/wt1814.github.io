<!-- TOC -->

- [1. Docker核心技术与实现原理](#1-docker核心技术与实现原理)
    - [1.1. Namespaces](#11-namespaces)
    - [1.2. CGroups](#12-cgroups)
    - [1.3. UnionFS](#13-unionfs)

<!-- /TOC -->

# 1. Docker核心技术与实现原理    

<!-- 
http://dockone.io/article/2941

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
-->

&emsp; Docker虚拟化技术需要以下核心技术的支撑：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-33.png)  

&emsp; 首先，Docker 的出现一定是因为目前的后端在开发和运维阶段确实需要一种虚拟化技术解决开发环境和生产环境环境一致的问题，通过 Docker 可以将程序运行的环境也纳入到版本控制中，排除因为环境造成不同运行结果的可能。但是上述需求虽然推动了虚拟化技术的产生，但是如果没有合适的底层技术支撑，那么仍然得不到一个完美的产品。本文剩下的内容会介绍几种 Docker 使用的核心技术，如果了解它们的使用方法和原理，就能清楚 Docker 的实现原理。  

## 1.1. Namespaces  
&emsp; <font color = "lime">命名空间（namespaces）是 Linux 提供的用于分离进程树、网络接口、挂载点以及进程间通信等资源的方法。</font>在日常使用 Linux 或者 macOS 时，并没有运行多个完全分离的服务器的需要，但是如果在服务器上启动了多个服务，这些服务其实会相互影响的，每一个服务都能看到其他服务的进程，也可以访问宿主机器上的任意文件，这是很多时候不愿意看到的，更希望运行在同一台机器上的不同服务能做到完全隔离，就像运行在多台不同的机器上一样。 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-34.png)  
&emsp; 在这种情况下，一旦服务器上的某一个服务被入侵，那么入侵者就能够访问当前机器上的所有服务和文件，这也是不想看到的，而 Docker 其实就通过 Linux 的 Namespaces 对不同的容器实现了隔离。  
&emsp; Linux 的命名空间机制提供了以下七种不同的命名空间，包括 CLONE_NEWCGROUP、CLONE_NEWIPC、CLONE_NEWNET、CLONE_NEWNS、CLONE_NEWPID、CLONE_NEWUSER 和 CLONE_NEWUTS，通过这七个选项能在创建新的进程时设置新进程应该在哪些资源上与宿主机器进行隔离。  

## 1.2. CGroups  
&emsp; Linux的命名空间为新创建的进程隔离了文件系统、网络并与宿主机器之间的进程相互隔离，但是命名空间并不能够提供物理资源上的隔离，比如 CPU 或者内存，如果在同一台机器上运行了多个对彼此以及宿主机器一无所知的『容器』，这些容器却共同占用了宿主机器的物理资源。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-35.png)  
&emsp; 如果其中的某一个容器正在执行 CPU 密集型的任务，那么就会影响其他容器中任务的性能与执行效率，导致多个容器相互影响并且抢占资源。如何对多个容器的资源使用进行限制就成了解决进程虚拟资源隔离之后的主要问题，而 Control Groups（简称 CGroups）就是能够隔离宿主机器上的物理资源，例如 CPU、内存、磁盘 I/O 和网络带宽。  
&emsp; 每一个 CGroup 都是一组被相同的标准和参数限制的进程，不同的 CGroup 之间是有层级关系的，也就是说它们之间可以从父类继承一些用于限制资源使用的标准和参数。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-36.png)  
&emsp; Linux 的 CGroup 能够为一组进程分配资源，也就是我们在上面提到的 CPU、内存、网络带宽等资源，通过对资源的分配，CGroup 能够提供以下的几种功能：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/docker/docker-37.png)  

    在 CGroup 中，所有的任务就是一个系统的一个进程，而 CGroup 就是一组按照某种标准划分的进程，在 CGroup 这种机制中，所有的资源控制都是以 CGroup 作为单位实现的，每一个进程都可以随时加入一个 CGroup 也可以随时退出一个 CGroup。  

## 1.3. UnionFS  
&emsp; Linux 的命名空间和控制组分别解决了不同资源隔离的问题，前者解决了进程、网络以及文件系统的隔离，后者实现了 CPU、内存等资源的隔离，但是在 Docker 中还有另一个非常重要的问题需要解决 - 也就是镜像。  
&emsp; 镜像到底是什么，它又是如何组成和组织的是作者使用 Docker 以来的一段时间内一直比较让作者感到困惑的问题，我们可以使用 docker run 非常轻松地从远程下载 Docker 的镜像并在本地运行。  
&emsp; Docker 镜像其实本质就是一个压缩包，我们可以使用下面的命令将一个 Docker 镜像中的文件导出：  

```text
$ docker export $(docker create busybox) | tar -C rootfs -xvf -
$ ls
bin  dev  etc  home proc root sys  tmp  usr  var
```
&emsp; 可以看到这个 busybox 镜像中的目录结构与 Linux 操作系统的根目录中的内容并没有太多的区别，可以说 Docker 镜像就是一个文件。  
