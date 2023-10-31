
<!-- TOC -->

- [1. Linux和DevOps](#1-linux和devops)
    - [1.1. Linux](#11-linux)
    - [1.2. Devops](#12-devops)
        - [1.2.1. 从上往下学Docker](#121-从上往下学docker)
            - [1.2.1.1. Docker使用教程](#1211-docker使用教程)
            - [1.2.1.2. 镜像详解](#1212-镜像详解)
            - [1.2.1.3. 容器详解](#1213-容器详解)
            - [1.2.1.4. Docker的网络实现：Docker四种网络模式](#1214-docker的网络实现docker四种网络模式)
        - [1.2.2. Kubernetes](#122-kubernetes)
            - [1.2.2.1. k8s架构](#1221-k8s架构)
            - [1.2.2.2. k8s常用命令](#1222-k8s常用命令)
            - [1.2.2.3. 组件详解](#1223-组件详解)
                - [1.2.2.3.1. Pod](#12231-pod)
                    - [1.2.2.3.1.1. 资源限制](#122311-资源限制)
                    - [1.2.2.3.1.2. 零停机滚动更新](#122312-零停机滚动更新)
                    - [1.2.2.3.1.3. 自动扩缩容](#122313-自动扩缩容)
                    - [1.2.2.3.1.4. pod容器共享Volume（持久化）](#122314-pod容器共享volume持久化)
                - [1.2.2.3.2. Service](#12232-service)
                - [1.2.2.3.3. k8s网络原理](#12233-k8s网络原理)
                    - [1.2.2.3.3.1. Kubemetes网络模型](#122331-kubemetes网络模型)
                    - [1.2.2.3.3.2. # 网络模型](#122332--网络模型)
                    - [1.2.2.3.3.3. # 网络模Container-to-Container的网络型](#122333--网络模container-to-container的网络型)
                    - [1.2.2.3.3.4. kube-proxy](#122334-kube-proxy)
                - [1.2.2.3.4. 共享存储原理](#12234-共享存储原理)
    - [1.3. jenkins + Docker](#13-jenkins--docker)
    - [1.4. jenkins + Kubernetes](#14-jenkins--kubernetes)

<!-- /TOC -->


# 1. Linux和DevOps

## 1.1. Linux  
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


## 1.2. Devops

### 1.2.1. 从上往下学Docker

#### 1.2.1.1. Docker使用教程
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


#### 1.2.1.2. 镜像详解
1. Docker中镜像是分层的，最顶层是读写层（镜像与容器的区别），其底部依赖于Linux的UnionFS文件系统。  
2. **<font color = "red">利用联合文件系统UnionFS写时复制的特点，在启动一个容器时，Docker引擎实际上只是增加了一个可写层和构造了一个Linux容器。</font>**  

#### 1.2.1.3. 容器详解
1. 单个宿主机的多个容器是隔离的，其依赖于Linux的Namespaces、CGroups。  
2. 隔离的容器需要通信、文件共享（数据持久化）。  

#### 1.2.1.4. Docker的网络实现：Docker四种网络模式
&emsp; docker run创建Docker容器时，可以用 --net 选项指定容器的网络模式，Docker有以下4种网络模式：  

* host模式，使用 --net=host 指定。  
* container模式，使用 --net=container:NAMEorID 指定。  
* none模式，使用 --net=none 指定。  
* bridge模式，使用 --net=bridge 指定，默认设置。  


### 1.2.2. Kubernetes
#### 1.2.2.1. k8s架构
1. 1). 一个容器或多个容器可以同属于一个Pod之中。 2). Pod是由Pod控制器进行管理控制，其代表性的Pod控制器有Deployment、StatefulSet等。 3). Pod组成的应用是通过Service或Ingress提供外部访问。  
2. **<font color = "red">每一个Kubernetes集群都由一组Master节点和一系列的Worker节点组成。</font>**  
    1. **<font color = "clime">Master的组件包括：API Server、controller-manager、scheduler和etcd等几个组件。</font>**  
        * **<font color = "red">API Server：K8S对外的唯一接口，提供HTTP/HTTPS RESTful API，即kubernetes API。</font>**  
        * **<font color = "red">controller-manager：负责管理集群各种资源，保证资源处于预期的状态。</font>** 
        * **<font color = "red">scheduler：资源调度，负责决定将Pod放到哪个Node上运行。</font>** 
    2. **<font color = "clime">Node节点主要由kubelet、kube-proxy、docker引擎等组件组成。</font>**  

#### 1.2.2.2. k8s常用命令
1. k8s常用资源  
&emsp; Namespace、Pod、Label、Deployment、Service...  

2. kubectl命令摘要  
&emsp; `get`      #显示一个或多个资源  
&emsp; `describe`  #显示资源详情  
&emsp; create    #从文件或标准输入创建资源  
&emsp; edit   #从文件或标准输入更新资源  
&emsp; `delete`   #通过文件名、标准输入、资源名或者 label 删除资源  
&emsp; `log`       #输出 pod 中一个容器的日志  
&emsp; exec  #在容器内部执行命令  
&emsp; run     #在集群中使用指定镜像启动容器  
&emsp; help         #显示各个命令的帮助信息  

3. 常用命令  
    1. 获取资源信息  
    &emsp; 获取所有 Pod：kubectl get pods  
    &emsp; 获取所有 Service：kubectl get services  
    &emsp; 获取特定 Namespace 中的 Deployment：kubectl get deployment -n <namespace>  
    2. 查看资源详细信息  
    &emsp; 查看 Pod 的详细信息：kubectl describe pod <pod-name>  
    &emsp; 查看 Service 的详细信息：kubectl describe service <service-name>  
    3. 创建资源  
    &emsp; 创建一个 Pod：kubectl create -f pod.yaml  
    &emsp; 创建一个 Service：kubectl create -f service.yaml  
    4. 应用或更新资源配置  
    &emsp; 应用或更新一个 Deployment：kubectl apply -f deployment.yaml  
    &emsp; 应用或更新一个 ConfigMap：kubectl apply -f configmap.yaml  
    5. 删除资源  
    &emsp; 删除一个 Pod：kubectl delete pod <pod-name>  
    &emsp; 删除一个 Service：kubectl delete service <service-name>  
    6. 在容器内执行命令  
    &emsp; 在 Pod 内的一个容器中执行命令：kubectl exec -it <pod-name> --container <container-name> -- /bin/sh  
    7. 获取容器日志  
    &emsp; 查看 Pod 内容器的日志：kubectl logs <pod-name>  

#### 1.2.2.3. 组件详解  
##### 1.2.2.3.1. Pod
###### 1.2.2.3.1.1. 资源限制


###### 1.2.2.3.1.2. 零停机滚动更新

###### 1.2.2.3.1.3. 自动扩缩容 
<!-- 
https://blog.csdn.net/lvjianzhaoa/article/details/103278045
https://blog.csdn.net/u014034049/article/details/110387604
-->


###### 1.2.2.3.1.4. pod容器共享Volume（持久化）


##### 1.2.2.3.2. Service



##### 1.2.2.3.3. k8s网络原理  

###### 1.2.2.3.3.1. Kubemetes网络模型
###### 1.2.2.3.3.2. # 网络模型  
&emsp; 关于 Pod 如何接入网络这件事情，Kubernetes 做出了明确的选择。具体来说，Kubernetes要求所有的网络插件实现必须满足如下要求：  

&emsp; 所有的 Pod 可以与任何其他 Pod 直接通信，无需使用 NAT 映射（network address translation）  
&emsp; 所有节点可以与所有 Pod 直接通信，无需使用 NAT 映射  
&emsp; Pod 内部获取到的 IP 地址与其他 Pod 或节点与其通信时的 IP 地址是同一个  

&emsp; 在这些限制条件下，需要解决如下四种完全不同的网络使用场景的问题：  

* Container-to-Container的网络
* Pod与Pod之间的网络  
* Pod与Service之间的网络  
* Internet-to-Service 的网络（集群外部与内部组件之间的通信）



###### 1.2.2.3.3.3. # 网络模Container-to-Container的网络型 



###### 1.2.2.3.3.4. kube-proxy
问题：详述kube-proxy原理，一个请求是如何经过层层转发落到某个pod上的整个过程。请求可能来自pod也可能来自外部。

kube-proxy部署在每个Node节点上，通过监听集群状态变更，并对本机iptables做修改，从而实现网络路由。 而其中的负载均衡，也是通过iptables的特性实现的。

另外我们需要了解k8s中的网络配置类型，有如下几种：

hostNetwork Pod使用宿主机上的网络，此时可能端口冲突。
hostPort 宿主机上的端口与Pod的目标端口映射。
NodePort 通过Service访问Pod，并给Service分配一个ClusterIP。



##### 1.2.2.3.4. 共享存储原理

## 1.3. jenkins + Docker  
&emsp; jenkins推送jar包到docker服务器，docker容器启动。  

## 1.4. jenkins + Kubernetes    
&emsp; jenkins推送镜像到Harbor镜像服务器，Kubernetes容器启动。

