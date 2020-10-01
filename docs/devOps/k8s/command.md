
<!-- TOC -->

- [1. Kubemetes实践](#1-kubemetes实践)
    - [1.1. Kubemetes的安装与配置](#11-kubemetes的安装与配置)
        - [1.1.1. Kubernetes的安装](#111-kubernetes的安装)
        - [1.1.2. Kubernetes集群的安全设置](#112-kubernetes集群的安全设置)
        - [1.1.3. Kubernetes集群的网络配置](#113-kubernetes集群的网络配置)
        - [1.1.4. 内网中的Kubernetes相关配置](#114-内网中的kubernetes相关配置)
        - [1.1.5. Kubemetes的版本升级](#115-kubemetes的版本升级)
    - [1.2. kubectl命令行工具](#12-kubectl命令行工具)
        - [1.2.1. kubectl用法概述](#121-kubectl用法概述)
        - [1.2.2. kubectl常用命令](#122-kubectl常用命令)
    - [1.3. 深入掌握Pod](#13-深入掌握pod)
        - [1.3.1. 创建与管理pod/ReplicaSet](#131-创建与管理podreplicaset)
        - [1.3.2. Pod的升级和回滚](#132-pod的升级和回滚)
        - [1.3.3. Pod的扩容和缩容](#133-pod的扩容和缩容)
    - [1.4. 深入掌握Service](#14-深入掌握service)
        - [1.4.1. Service基本用法](#141-service基本用法)
            - [1.4.1.1. 外部Service](#1411-外部service)
        - [1.4.2. 集群外部访问Pod或Service](#142-集群外部访问pod或service)
        - [1.4.3. DNS服务搭建指南](#143-dns服务搭建指南)
        - [1.4.4. 自定义DNS和上游DNS服务器](#144-自定义dns和上游dns服务器)
        - [1.4.5. Ingress：HTTP 7层路由机制](#145-ingresshttp-7层路由机制)
    - [1.5. 基于NFS文件集群共享](#15-基于nfs文件集群共享)
    - [1.6. IDE插件](#16-ide插件)
    - [1.7. k8s上部署Redis三主三从集群](#17-k8s上部署redis三主三从集群)
    - [1.8. k8s微服务](#18-k8s微服务)

<!-- /TOC -->

# 1. Kubemetes实践  
<!-- 
k8s的快速使用手册
https://www.cnblogs.com/linu/p/10955823.html
-->


&emsp; <font color = "lime">整体参考《Kubernetes权威指南》</font>  

## 1.1. Kubemetes的安装与配置
<!-- 
Centos7搭建k8s环境教程
https://mp.weixin.qq.com/s/4zsGwYBLoiZx0l68NQPPMA
https://mp.weixin.qq.com/s/LA1w7pZAyIpeP3sfQIWqpw
-->

&emsp; CentOS Linux 7默认启动了防火墙服务（firewalld），而Kubernetes的Master与工作Node之间会有大量的网络通信，安全的做法是在防火墙上配置各组件需要相互通信的端口号，具体要配置的端口号详见「内网中的Kubemetes相关配置」节中各服务监听的端口号说明。在一个安全的内部网络环境中可以关闭防火墙服务：  

```
#systemctl disable firewalld
#systemctl stop firewalld
```
&emsp; 另外，建议在主机上禁用SELinux,目的是让容器可以读取主机文件系统：  
```
#setenforce 0
```
&emsp; 或修改系统文件/etc/sysconfig/selinux，将SELINUX=enfbrcing修改成SELINUX=disabled，然后重启Linux。  


### 1.1.1. Kubernetes的安装
<!-- 
https://www.cnblogs.com/xiaoyuxixi/p/12142218.html
https://blog.csdn.net/qq_46595591/article/details/107520114?utm_medium=distribute.wap_relevant.none-task-blog-title-4
-->

* （推荐）使用kubeadmin通过离线镜像安装  
* 使用阿里公有云平台k8s  
* 通过yum官方仓库安装  
* 二进制包的形式进行安装，kubeasz (github)  

### 1.1.2. Kubernetes集群的安全设置

1. 基于CA签名的双向数字证书认证万式
2. 基于HTTP BASE TOKEN 的简单认证后式  

### 1.1.3. Kubernetes集群的网络配置
&emsp; 在多个Node组成的Kubernetes集群内，跨主机的容器间网络互通是Kubernetes集群能够正常工作的前提条件。Kubernetes本身并不会对跨主机的容器网络进行设置，这需要额外的工 具来实现。除了谷歌公有云GCE平台提供的网络设置，一些开源的工具包括Flannel, Open vSwitch. Weave> Calico等都能够实现跨主机的容器间网络互通。随着CNI网络模型的逐渐成 熟，Kubernetes将优先使用CNI网络插件打通跨主机的容器网络。具体的网络原理和流行开源 网络工具配置详见第3章的说明。  

### 1.1.4. 内网中的Kubernetes相关配置  
1. Docker Private Registry （私有 Docker 镜像库）  
&emsp; 使用Docker提供的Registry镜像创建一个私有镜像仓库。  
&emsp; 详细的安装步骤请参考 Docker 的官方文档 https://docs.docker.eom/registry/deploying/o  
2. kubelet配置  
    &emsp; 由于在Kubemetes中是以Pod而不是以Docker容器为管理单元的，在kubelet创建Pod时，还通过启动一个名为 ger.io/google_containers/pause 的镜像来实现Pod的概念。  
    &emsp; 该镜像存在于谷歌镜像库http://gcr.io 中，需要通过一台能够连上Internet的服务器将其下载，导出文件，再push到私有Docker Registry中。  
    &emsp; 之后，可以给每台Node的kubelet服务加上启动参数-pod-infra-container-image,指定为私 有Docker Registry中pause镜像的地址。例如：  

    ```text
    #cat /etc/kubemetes/kubelet
    KUBELET_ARGS="--api-servers=http://192.168.18.3:8080
    一一hostname-override=l92.168.18.3 一一log-dir=/var/log/kubemetes 一一v=2
    --pod-infra-container-image=gcr.io/google_containers/pause-amd64:3.0"
    ```
    &emsp; 如果该镜像无法从gcr.io下载，则也可以从Docker Hub上进行下载：  

    ```text
    #docker pull kubeguide/pause-amd64:3.0
    ```
    &emsp; 修改kubelet配置文件中的-pod_infra_container_image参数：  

    ```
    --pod-infra-container-image=kubeguide/pause-amd64:3.0
    ```
    &emsp; 然后重启kubelet服务：  

    ```
    #systemctl restart kubelet
    ```
    &emsp; 通过以上设置就在内网环境中搭建了一个企业内部的私有容器云平台。  

### 1.1.5. Kubemetes的版本升级  
&emsp; Kubemetes的版本升级需要考虑到不要让当前集群中正在运行的容器受到影响。应对集群中的各Node逐个进行隔离，然后等待在其上运行的容器全部执行完成，再更新该Node上的 kubelet和kube-proxy服务，将全部Node都更新完成后，最后更新Master的服务。  

* 通过官网获取最新版本的二进制包kubemetes.tar.gz，解压缩后提取服务的二进制文件。  
* 逐个隔离Node，等待在其上运行的全部容器工作完成后，更新kubelet和kube-proxy服务文件，然后重启这两个服务。  
* 更新Master的kube-apiserver、kube-controller-manager、kube-scheduler服务文件并重启。  


## 1.2. kubectl命令行工具  
&emsp; kubectl作为客户端CLI工具，可以让用户通过命令行的方式对Kubernetes集群进行操作。  
&emsp; kubeadm/kubelet/kubectl区别？  

* kubeadm是kubernetes集群快速构建工具
* kubelet运行在所有节点上，负责启动POD和容器，以系统服务形式出现
* kubectl：kubectl是kubenetes命令行工具，提供指令

### 1.2.1. kubectl用法概述  
&emsp; kubectl命令行的语法如下：  

```text
$ kubectl [command] [TYPE] [NAME] [flags]
```
&emsp; 其中，command、TYPE、NAME> flags的含义如下。  
1. command：子命令，用于操作Kubemetes集群资源对象的命令，例如create> delete> describe > get> apply等。  
2. TYPE：资源对象的类型，区分大小写，能以单数形式、复数形式或者简写形式表示。例如以下3种TYPE是等价的。  

    ```text
    $ kubectl get pod podl  
    $ kubectl get pods podl  
    $ kubectl get po podl
    ```
3. NAME：资源对象的名称，区分大小写。如果不指定名称，则系统将返回属于TYPE的全部对象的列表，例如$kubectl get pods将返回所有Pod的列表。
4. flags：kubectl子命令的可选参数，例如使用"-s”指定apiserver的URL地址而不用默认值。  

### 1.2.2. kubectl常用命令

1. 创建资源对象  
    &emsp; 根据yaml配置文件一次性创建service和rc：  

    ```text
    $ kubectl create -f my-service.yaml -f my-rc.yaml 
    ``` 
    &emsp; 根据\<directory>目录下所有.yaml、.yml、.json文件的定义进行创建操作: 
    ```text 
    $ kubectl create -f <directory>  
    ```
2. 查看资源对象  
    &emsp; 查看所有Pod列表：  
    ```text
    $ kubectl get pods 
    ``` 
    &emsp; 查看rc和service列表：  
    ```text
    $ kubectl get rc,service 
    ``` 
3. 描述资源对象  
    &emsp; 显示Node的详细信息：  

    ```text
    $ kubectl describe nodes <node-name>  
    ```
    &emsp; 显示Pod的详细信息：  

    ```text
    $ kubectl describe pods/<pod-name> 
    ``` 
    &emsp; 显示由RC管理的Pod的信息：  

    ```text
    $ kubectl describe pods <rc-name> 
    ``` 
4. 删除资源对象  
    &emsp; 基于pod.yaml定义的名称删除Pod：  

    ```text
    $ kubectl delete -f pod.yaml  
    ```
    &emsp; 删除所有包含某个label的Pod和service：  
    $ kubectl delete pods,services -1 name=\<label-name>  
    &emsp; 删除所有Pod:  

    ```text
    $ kubectl delete pods --all  
    ```
5. 执行容器的命令  
    &emsp; 执行Pod的date命令，默认使用Pod中的第1个容器执行：  

    ```text
    $ kubectl exec <pod-name> date 
    ``` 
    &emsp; 指定Pod中某个容器执行date命令：  

    ```text
    $ kubectl exec <pod-name> -c <container-name> date 
    ``` 
    &emsp; 通过bash获得Pod中某个容器的TTY,相当于登录容器：  

    ```text
    $ kubectl exec -ti <pod-name> -c <container-name> /bin/bash  
    ```
6. 查看容器的日志  
    &emsp; 查看容器输出到stdout的日志：  

    ```text
    $ kubectl logs <pod-name>
    ```  
    &emsp; 跟踪查看容器的日志，相当于tail-f命令的结果：  

    ```text
    $ kubectl logs -f <pod-name> -c <container-name> 
    ``` 

----

## 1.3. 深入掌握Pod  

### 1.3.1. 创建与管理pod/ReplicaSet
<!-- 

k8s笔记二（pod资源的创建与管理）
https://blog.csdn.net/dayi_123/article/details/88683870

k8s创建资源的两种方式、访问pod
https://blog.csdn.net/PpikachuP/article/details/89674578

Kubernetes学习笔记——k8s创建Pod和ReplicaSet的工作流程
https://blog.csdn.net/weixin_38070561/article/details/82706973


K8S：创建pod资源两种方式： kubectl命令 && yaml文件
https://blog.csdn.net/weixin_45691464/article/details/106006125

-->

### 1.3.2. Pod的升级和回滚  
&emsp; 当集群中的某个服务需要升级时，我们需要停止目前与该服务相关的所有Pod,然后下载 新版本镜像并创建新的Pod。如果集群规模比较大，则这个工作就变成了一个挑战，而且先全 部停止然后逐步升级的方式会导致较长时间的服务不可用。Kubemetes提供了滚动升级功能来解决上述问题。  
&emsp; 如果Pod是通过Deployment创建的，则用户可以在运行时修改Deployment的Pod定义 (spec.template)或镜像名称，并应用到D eployment对象上，系统即可完成D eployment的自 动更新操作。如果在更新过程中发生了错误，则还可以通过回滚(Rollback)操作恢复Pod 的版本。  

### 1.3.3. Pod的扩容和缩容  
&emsp; 在实际生产系统中，经常会遇到某个服务需要扩容的场景，也可能会遇到由于资源紧 张或者工作负载降低而需要减少服务实例数量的场景。此时我们可以利用Deployment/RC的 Scale机制来完成这些工作。  
&emsp; Kubernetes对Pod的扩容和缩容操作提供了手动和自动两种模式，手动模式通过执行kubectl scale命令对一个Deployment/RC进行Pod副本数量的设置，即可一键完成。自动模式则需要用 户根据某个性能指标或者自定义业务指标，并指定Pod副本数量的范围，系统将自动在这个范 围内根据性能指标的变化进行调整。  

## 1.4. 深入掌握Service  

&emsp; Service是Kubernetes最核心的概念，通过创建Service，可以为一组具有相同功能的容器应用提供一个统一的入口地址，并且将请求负载分发到后端的各个容器应用上。本节对Service的使用进行详细说明，包括Service的负载均衡、外网访问、DNS服务的搭建、Ingress7层路由机制等。  

### 1.4.1. Service基本用法  

#### 1.4.1.1. 外部Service  
&emsp; 在某些环境中，应用系统需要将一个外部数据库作为后端服务进行连接，或将另一个集群或Namespace中的服务作为服务的后端，这时可以通过创建一个无Label Selector的Service来实现。  

 
### 1.4.2. 集群外部访问Pod或Service  
<!-- 
从外部访问K8s中Pod的五种方式
https://blog.csdn.net/qq_23348071/article/details/87185025

-->
&emsp; 由于Pod和Service是Kubernetes集群范围内的虚拟概念，所以集群外的客户端系统无法通过Pod的IP地址或者Service的虚拟IP地址和虚拟端口号访问到它们。为了让外部客户端可以访问这些服务，可以将Pod或Service的端口号映射到宿主机，以使得客户端应用能够通过物理机访问容器应用。  

1. 将容器应用的端口号映射到物理机  
2. 将Service的端口号映射到物理机  

### 1.4.3. DNS服务搭建指南  
&emsp; 作为服务发现机制的基本功能，在集群内需要能够通过服务名对服务进行访问，这就需要一个集群范围的DNS服务来完成服务名到ClusterIP的解析。本节将对如何搭建DNS服务进行详细说明。  

### 1.4.4. 自定义DNS和上游DNS服务器  
&emsp; 在实际环境中，很多用户都有自己的私有域名区域，并且希望能够集成到Kubernetes DNS 的命名空间中，例如混合云用户可能希望能在集群内解析其内部的".corp"域名；用户也可能己存在一个未被Kubernetes管理的服务发现系统（例如Consul）来完成域名解析。从Kubernetes vl.6版本开始，用户可以在Kubernetes集群内配置私有DNS区域（通常称为存根域Stub Domain） 和外部的上游域名服务了。  


### 1.4.5. Ingress：HTTP 7层路由机制  
&emsp; 根据前面对Service的使用说明，我们知道Service的表现形式为IP:Port,即工作在TCP/IP 层。而对于基于HTTP的服务来说，不同的URL地址经常对应到不同的后端服务或者虚拟服务 器（Virtual Host）,这些应用层的转发机制仅通过Kubernetes的Service机制是无法实现的。从 Kubernetes vl.l版本开始新增Ingress资源对象，用于将不同URL的访问请求转发到后端不同 的Service,以实现HTTP层的业务路由机制。Kubernetes使用一个Ingress策略定义和一个具体 的Ingress Controller,两者结合并实现了一个完整的Ingress负载均衡器。  
&emsp; 使用Ingress进行负载分发时，Ingress Controller将基于Ingress规则将客户端请求直接转 发到Service对应的后端Endpoint（即Pod）上，这样会跳过kube-proxy的转发功能，kube-proxy 不再起作用。如果Ingress Controller提供的是对外服务，则实际上实现的是边缘路由器的功能。  



----

## 1.5. 基于NFS文件集群共享  




## 1.6. IDE插件  
<!-- 
IDE 插件
https://mp.weixin.qq.com/s/KbcUxGJ3JK7ANtuDRvPzZQ

-->

----

## 1.7. k8s上部署Redis三主三从集群  
<!-- 
k8s 上部署 Redis 三主三从 集群
https://www.cnblogs.com/winstom/p/11881882.html


在K8s上部署Redis 集群
https://blog.csdn.net/zhutongcloud/article/details/90768390

-->

## 1.8. k8s微服务 
<!-- 
微服务交付至kubernetes流程
https://www.cnblogs.com/jasonminghao/p/12617313.html

-->
