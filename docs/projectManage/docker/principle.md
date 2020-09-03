



# Docker

<!-- 
https://mp.weixin.qq.com/s/xq9lrHqBOWjQ65-V4Jrttg

https://baike.baidu.com/item/Docker/13344470?anchor=1
http://dockone.io/article/2941
https://baijiahao.baidu.com/s?id=1641433332828402192&wfr=spider&for=pc
https://www.jianshu.com/p/e1f7b8d5184c
Docker Swarm概念与基本用法
https://mp.weixin.qq.com/s/xywXgXEgjWcYZ8TSLzSEVw
-->

## 容器化技术  
<!-- 
https://mp.weixin.qq.com/s/RvURRnoSFPywtR8Af7IZ-g
-->
......

## Docker简介  
<!-- 
https://mp.weixin.qq.com/s?__biz=MzU0MzQ5MDA0Mw==&mid=2247486479&idx=3&sn=c1fbb2084fb251242d28232a199dcc32&chksm=fb0be69bcc7c6f8d43cacac86fc72b2c48d0264cdff59f992b1bdd3f34847283c596efdd524e&mpshare=1&scene=1&srcid=&sharer_sharetime=1564706539160&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=dd204f3b2a2710ede816adfd8719abab5e616cb2bfaa745f0c27e1497097ef87868dd4e09b2f4cf7f9356029742e5eca3f090d149bf596e7cdde74af20c9a2075fda7b37e40c5fbc75534666bf2183f9&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060834&lang=zh_CN&pass_ticket=J89DTwjzapl6QMdBj7AAiEYLyOJjEXJXaq6zx%2Fd594ed2uDLQjTlRiDqWumTTR0m
-->

* 开源的应用容器引擎，基于Go语言开发  
* 容器是完全使用沙箱机制，容器开销极低  
* Docker就是容器化技术的代名词	
* Docker也具备一定虚拟化职能

&emsp; Docker是容器化平台。  
&emsp; Docker是提供应用打包部署与运行应用的容器化平台。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-1.png)  




## Docker基本概念  

<!-- 
* 镜像: 镜像是文件，是只读的，提供了运行程序完整的软硬件资源，是应用程序的"集装箱"。  
* 容器: 是镜像的实例，由Docker负责创建，容器之间彼此隔离。  

引擎：创建和管理容器的工具，通过读取镜像来生成容器，并负责从仓库拉取镜像或提交镜像到仓库中；

镜像：类似于虚拟机镜像，一般由一个基本操作系统环境和多个应用程序打包而成，是创建容器的模板；

容器：可看作一个简易版的Linxu系统环境（包括root用户权限、进程空间、用户空间和网络空间等）以及运行在其中的应用程序打包而成的盒子；

仓库：集中存放镜像文件的场所，分为公共仓库和私有仓库，目前最大的公共仓库是官方提供的Docker Hub，此外国内的阿里云、腾讯云等也提供了公共仓库；

宿主机：运行引擎的操作系统所在服务器。
-->

  

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


## Docker体系结构  
<!-- 
https://mp.weixin.qq.com/s/RvURRnoSFPywtR8Af7IZ-g
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-2.png)  

## Docker运行流程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-3.png)  

## 引擎  
<!-- 
https://mp.weixin.qq.com/s?__biz=MzI1NDQ3MjQxNA==&mid=2247488707&idx=1&sn=eab72ec6e0c6bcd0396f49ab67644f05&chksm=e9c5ed72deb264649c24f8c1f54a1ecb896abc11dee26373bcf7df0f2b6a1a28f1ce2050fe89&mpshare=1&scene=1&srcid=#rd
-->


## 镜像详解
### 镜像分层，镜像文件系统  
<!-- 
https://mp.weixin.qq.com/s/PM6K3j8bqBbbwtt4S4uyEw
-->

## 容器详解
### Docker宿主机与容器通信  

### 容器生命周期  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-4.png)  

### 容器数据卷
容器数据卷：持久化。docker运行产生的数据持久化

### 容器间通信  
#### 容器间Link单向通信  

#### Bridge网桥双向通信  

#### Volume容器间共享数据  


## Docker基础操作系统  
<!-- 
https://mp.weixin.qq.com/s/PM6K3j8bqBbbwtt4S4uyEw
-->


![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/docker/docker-10.png)  





