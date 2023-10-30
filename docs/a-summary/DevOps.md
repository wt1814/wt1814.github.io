
<!-- TOC -->

- [1. Linux和DevOps](#1-linux和devops)
    - [1.1. Linux](#11-linux)
    - [1.2. Devops](#12-devops)
        - [1.2.3. 从上往下学Docker](#123-从上往下学docker)
            - [1.2.3.1. Docker使用教程](#1231-docker使用教程)
            - [1.2.3.2. 镜像详解](#1232-镜像详解)
            - [1.2.3.3. 容器详解](#1233-容器详解)
        - [1.2.4. Kubernetes](#124-kubernetes)
            - [1.2.4.1. k8s架构](#1241-k8s架构)

<!-- /TOC -->


# 1. Linux和DevOps

## 1.1. Linux  



## 1.2. Devops
1. 文件夹：
2. 文件操作：cp、mv、find，查找文件  
3. 文件传输：
    1. ftp，在本地主机和远程主机之间或者在两个远程主机之间进行文件传输；  
    2. scp，在网络上的主机之间拷贝文件；
    3. curl，
4. 文件内容操作：
    1. 查看：cat、more、tail  
    2. 编辑：vim、grep、sed  
5. 进程管理：  
    1. ps，查找出进程的信息
    2. free，查看内存使用状况
    3. top，查看实时刷新的系统进程信息
6. 网络：  
    1. ping，检测网络连通性
    2. lsof，查看指定IP 和/或 端口的进程的当前运行情况  


---------------
&emsp; top命令：它包含了很多方面的数据，例如CPU，内存，系统的任务等等数据。  

&emsp; 运行结果可以分为两部分：   
&emsp; 第一部分是前5行，是系统整体的统计信息；   
&emsp; 第二部分是第8行开始的进程信息，我们从上往下逐行依次进行说明。   

1. 整体统计信息  
&emsp; 第二行：Tasks：当前有多少进程。running：正在运行的进程数。sleeping：正在休眠的进程数。stopped：停止的进程数。zombie：僵尸进程数。  
&emsp; 第三行：us：用户空间占CPU的百分比。sy：内核空间占CPU的百分比。sy：内核空间占CPU的百分比   
&emsp; 第四行：total：物理内存总量。free：空闲内存量。used：使用的内存量。buffer/cache：用作内核缓存的内存量。  
&emsp; 第五行：total：交换区内存总量。free：空闲交换区总量。used：使用的交换区总量。buffer/cache：缓冲的交换区总量。  
&emsp; 第四第五行分别是内存信息和swap信息，所有程序的运行都是在内存中进行的，所以内存的性能对与服务器来说非常重要。  

2. 进程信息  
&emsp; 默认情况下仅显示比较重要的 PID、USER、PR、NI、VIRT、RES、SHR、S、%CPU、%MEM、TIME+、COMMAND 列，还有一些参数  


### 1.2.3. 从上往下学Docker

#### 1.2.3.1. Docker使用教程
1. **<font color = "clime">镜像操作常用命令：pull(获取)、images(查看本地镜像)、inspect(查看镜像详细信息)、rmi(删除镜像)、commit(构建镜像)。</font>**  
2. **<font color = "clime">容器操作常用命令：run(创建并启动)、start(启动已有)、stop、exec(进入运行的容器)。</font>**  
3. **<font color = "clime">Dockerfile中包含：</font>** （# 为 Dockerfile中的注释）  
    * 基础镜像(FROM)    
    * 镜像元信息   
    * **<font color = "clime">镜像操作指令</font>** （RUN、COPY、ADD、EXPOSE、WORKDIR、ONBUILD、USER、VOLUME等）    
        * RUN命令：**run是在docker build构建镜像时，会执行的命令，** 比如安装一些软件、配置一些基础环境。  
    * **<font color = "clime">容器启动时执行指令</font>** （CMD、ENTRYPOINT）  
        * CMD命令： **cmd是在docker run启动容器时，会执行的命令，为启动的容器指定默认要运行的程序。** CMD指令指定的程序可被docker run命令行参数中指定要运行的程序所覆盖。 **<font color = "clime">注意：如果Dockerfile中如果存在多个CMD指令，仅最后一个生效。</font>**    
    ![image](http://182.92.69.8:8081/img/devops/docker/docker-9.png)  


#### 1.2.3.2. 镜像详解
1. Docker中镜像是分层的，最顶层是读写层（镜像与容器的区别），其底部依赖于Linux的UnionFS文件系统。  
2. **<font color = "red">利用联合文件系统UnionFS写时复制的特点，在启动一个容器时，Docker引擎实际上只是增加了一个可写层和构造了一个Linux容器。</font>**  

#### 1.2.3.3. 容器详解
1. 单个宿主机的多个容器是隔离的，其依赖于Linux的Namespaces、CGroups。  
2. 隔离的容器需要通信、文件共享（数据持久化）。  

### 1.2.4. Kubernetes
#### 1.2.4.1. k8s架构
1. 1). 一个容器或多个容器可以同属于一个Pod之中。 2). Pod是由Pod控制器进行管理控制，其代表性的Pod控制器有Deployment、StatefulSet等。 3). Pod组成的应用是通过Service或Ingress提供外部访问。  
2. **<font color = "red">每一个Kubernetes集群都由一组Master节点和一系列的Worker节点组成。</font>**  
    1. **<font color = "clime">Master的组件包括：API Server、controller-manager、scheduler和etcd等几个组件。</font>**  
        * **<font color = "red">API Server：K8S对外的唯一接口，提供HTTP/HTTPS RESTful API，即kubernetes API。</font>**  
        * **<font color = "red">controller-manager：负责管理集群各种资源，保证资源处于预期的状态。</font>** 
        * **<font color = "red">scheduler：资源调度，负责决定将Pod放到哪个Node上运行。</font>** 
    2. **<font color = "clime">Node节点主要由kubelet、kube-proxy、docker引擎等组件组成。</font>**  


