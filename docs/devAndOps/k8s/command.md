
<!-- TOC -->

- [1. Kubemetes实践](#1-kubemetes实践)
    - [1.1. Kubernetes的安装](#11-kubernetes的安装)
    - [1.2. Kubernetes的配置](#12-kubernetes的配置)
        - [1.2.1. Kubernetes集群的安全设置](#121-kubernetes集群的安全设置)
        - [1.2.2. Kubernetes集群的网络配置](#122-kubernetes集群的网络配置)
        - [1.2.3. 基于NFS文件集群共享](#123-基于nfs文件集群共享)
        - [1.2.4. 内网中搭建私有仓库](#124-内网中搭建私有仓库)
    - [1.3. kubectl命令行工具](#13-kubectl命令行工具)
        - [1.3.1. kubectl用法概述](#131-kubectl用法概述)
        - [1.3.2. kubectl常用命令](#132-kubectl常用命令)
    - [1.4. IDE插件](#14-ide插件)

<!-- /TOC -->

# 1. Kubemetes实践  
<!-- 
k8s的快速使用手册
https://www.cnblogs.com/linu/p/10955823.html

K8s自动扩缩容工具KEDA发布2.0版本，全面升级应用扩展能力 
https://mp.weixin.qq.com/s/KZlNqFRb6_N56oE-OKvFBA
-->
  
<!-- 
k8s上部署Redis三主三从集群
k8s 上部署 Redis 三主三从 集群
https://www.cnblogs.com/winstom/p/11881882.html
在K8s上部署Redis 集群
https://blog.csdn.net/zhutongcloud/article/details/90768390

-->

<!-- 
k8s微服务 
微服务交付至kubernetes流程
https://www.cnblogs.com/jasonminghao/p/12617313.html
-->

&emsp; <font color = "lime">整体参考《Kubernetes权威指南》</font>  

## 1.1. Kubernetes的安装
<!-- 
Centos7搭建k8s环境教程
https://mp.weixin.qq.com/s/4zsGwYBLoiZx0l68NQPPMA
https://mp.weixin.qq.com/s/LA1w7pZAyIpeP3sfQIWqpw
-->
<!-- 
https://www.cnblogs.com/xiaoyuxixi/p/12142218.html
https://blog.csdn.net/qq_46595591/article/details/107520114?utm_medium=distribute.wap_relevant.none-task-blog-title-4
-->
&emsp; CentOS Linux 7默认启动了防火墙服务(firewalld)，而Kubernetes的Master与工作Node之间会有大量的网络通信，安全的做法是在防火墙上配置各组件需要相互通信的端口号，具体要配置的端口号详见「内网中的Kubemetes相关配置」节中各服务监听的端口号说明。在一个安全的内部网络环境中可以关闭防火墙服务：  

```text
#systemctl disable firewalld
#systemctl stop firewalld
```
&emsp; 另外，建议在主机上禁用SELinux，目的是让容器可以读取主机文件系统：  
```
#setenforce 0
```
&emsp; 或修改系统文件/etc/sysconfig/selinux，将SELINUX=enfbrcing修改成SELINUX=disabled，然后重启Linux。  

&emsp; **Kubernetes的安装方式：**  

* **(推荐)使用<font color = "red">kubeadmin</font>通过离线镜像安装**  
* 使用阿里公有云平台k8s  
* 通过yum官方仓库安装  
* 二进制包的形式进行安装，kubeasz (github)  


        kubeadm/kubelet/kubectl区别？  
        kubeadm是kubernetes集群快速构建工具。
        kubelet运行在所有节点上，负责启动POD和容器，以系统服务形式出现。
        kubectl：kubectl是kubenetes命令行工具，提供指令。

## 1.2. Kubernetes的配置
### 1.2.1. Kubernetes集群的安全设置

1. 基于CA签名的双向数字证书认证方式
2. 基于HTTP BASE TOKEN 的简单认证方式  

### 1.2.2. Kubernetes集群的网络配置
<!--
 Kubernetes中容器到容器通信 
 https://mp.weixin.qq.com/s/P-xKd6HeOGxyt-YXCnZjmQ

kubernetes集群网络
https://www.cnblogs.com/yuezhimi/p/13042037.html

-->
&emsp; 在多个Node组成的Kubernetes集群内，跨主机的容器间网络互通是Kubernetes集群能够正常工作的前提条件。Kubernetes本身并不会对跨主机的容器网络进行设置，这需要额外的工具来实现。除了谷歌公有云GCE平台提供的网络设置，一些开源的工具包括Flannel, Open vSwitch. Weave> Calico等都能够实现跨主机的容器间网络互通。  

### 1.2.3. 基于NFS文件集群共享  
<!-- 
Kubernetes 集群部署NFS网络存储
https://blog.csdn.net/zuozewei/article/details/108165523
-->
&emsp; Kubernetes对Pod进行调度时，以当时集群中各节点的可用资源作为主要依据，自动选择某一个可用的节点，并将Pod分配到该节点上。在这种情况下，Pod中容器数据的持久化如果存储在所在节点的磁盘上，就会产生不可预知的问题，例如，当Pod出现故障，Kubernetes 重新调度之后，Pod 所在的新节点上，并不存在上一次 Pod 运行时所在节点上的数据。  
&emsp; 为了使 Pod 在任何节点上都能够使用同一份持久化存储数据，需要使用网络存储的解决方案为Pod提供数据卷。常用的网络存储方案有：NFS/cephfs/glusterfs。  

&emsp; **Kubernetes集群网络存储与Pod挂载点的区别：**  
&emsp; <font color = "lime">Kubernetes集群网络存储是不同宿主机实现文件共享；Pod挂载点是容器与宿主机实现文件共享。</font>  


### 1.2.4. 内网中搭建私有仓库  
......
<!-- 
从私有仓库拉取镜像
https://kubernetes.io/zh/docs/tasks/configure-pod-container/pull-image-private-registry/
-->


<!--   
1. Docker Private Registry (私有 Docker 镜像库)  
&emsp; 使用Docker提供的Registry镜像创建一个私有镜像仓库。  
&emsp; 详细的安装步骤请参考Docker的官方文档 https://docs.docker.eom/registry/deploying/o  
2. kubelet配置  
    &emsp; 由于在Kubemetes中是以Pod而不是以Docker容器为管理单元的，在kubelet创建Pod时，还通过启动一个名为ger.io/google_containers/pause的镜像来实现Pod的概念。  
    &emsp; 该镜像存在于谷歌镜像库http://gcr.io 中，需要通过一台能够连上Internet的服务器将其下载，导出文件，再push到私有Docker Registry中。  
    &emsp; 之后，可以给每台Node的kubelet服务加上启动参数-pod-infra-container-image，指定为私有Docker Registry中pause镜像的地址。例如：  

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
-->


## 1.3. kubectl命令行工具  
&emsp; <font color = "lime">kubectl作为客户端CLI工具，可以让用户通过命令行的方式对Kubernetes集群进行操作。</font>  

### 1.3.1. kubectl用法概述  
&emsp; kubectl命令行的语法如下：  

```text
$ kubectl [command] [TYPE] [NAME] [flags]
```
&emsp; 其中，command、TYPE、NAME、flags的含义如下。  
1. command：子命令，用于操作Kubemetes集群资源对象的命令，例如create、delete、describe、get、apply等。  
2. TYPE：资源对象的类型，区分大小写，能以单数形式、复数形式或者简写形式表示。例如以下3种TYPE是等价的。  

    ```text
    $ kubectl get pod podl  
    $ kubectl get pods podl  
    $ kubectl get po podl
    ```
3. NAME：资源对象的名称，区分大小写。如果不指定名称，则系统将返回属于TYPE的全部对象的列表，例如$kubectl get pods将返回所有Pod的列表。
4. flags：kubectl子命令的可选参数，例如使用"-s”指定apiserver的URL地址而不用默认值。  

### 1.3.2. kubectl常用命令
&emsp; [kubectl命令表](http://docs.kubernetes.org.cn/683.html)  
1. 创建资源对象  
    &emsp; 根据yaml配置文件一次性创建service和rc：  

    ```text
    $ kubectl create -f my-service.yaml -f my-rc.yaml 
    ``` 
    &emsp; 根据<directory>目录下所有.yaml、.yml、.json文件的定义进行创建操作: 
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
    $ kubectl describe pods <pod-name> 
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

    ```text
    $ kubectl delete pods,services -1 name=<label-name>  
    ```
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
    &emsp; 通过bash获得Pod中某个容器的TTY，相当于登录容器：  

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

## 1.4. IDE插件  
<!-- 
IDE 插件
https://mp.weixin.qq.com/s/KbcUxGJ3JK7ANtuDRvPzZQ
-->
