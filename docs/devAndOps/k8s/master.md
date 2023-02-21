
<!-- TOC -->

- [1. Kubemetes Master安装](#1-kubemetes-master安装)
    - [1.1. 单机安装](#11-单机安装)
        - [1.1.1. kubemetes安装](#111-kubemetes安装)
    - [1.2. 安装flannel组件](#12-安装flannel组件)
    - [1.3. kubemetes-dashboard仪表盘安装](#13-kubemetes-dashboard仪表盘安装)
    - [1.4. 集群安装](#14-集群安装)
    - [1.5. 配置](#15-配置)
        - [1.5.1. Kubernetes集群的安全设置](#151-kubernetes集群的安全设置)
        - [1.5.2. Kubernetes集群的网络配置](#152-kubernetes集群的网络配置)
        - [1.5.3. 基于NFS文件集群共享](#153-基于nfs文件集群共享)
        - [1.5.4. 内网中搭建私有仓库](#154-内网中搭建私有仓库)

<!-- /TOC -->


# 1. Kubemetes Master安装  

|节点|组件|
|---|---|
|master|etcd <br> kube-apiserver <br> kube-controller-manager <br> kube-scheduler|
|node|kubelet <br> kube-proxy <br> docker|


## 1.1. 单机安装

### 1.1.1. kubemetes安装
<!--
kubernetes(k8s) 集群 安装总流程
http://blog.51yip.com/cloud/2399.html

kubeadm安装单机k8s
*** https://blog.csdn.net/zjcjava/article/details/99317569
centos7安装kubernetes
https://blog.csdn.net/sumengnan/article/details/120932201

*** 本质是第一步的报错，failed to pull image \"k8s.gcr.io/pause:3.6\"
https://ceshiren.com/t/topic/22431



-->

<!-- 
Unable to register node with API server
https://blog.csdn.net/hawk199/article/details/125058030

*** 问题 使用kubeadm创建集群失败报Unable to register node with API server
https://blog.csdn.net/hawk199/article/details/125058030
The connection to the server localhost:8080 was refused - did you specify the right host or port?解决
https://blog.csdn.net/CEVERY/article/details/108753379

failed to get sandbox image “k8s.gcr.io/pause:3.6“: failed to pull image “k8s.gcr.io/pause:3.6“
https://blog.csdn.net/Haskei/article/details/128474534
https://blog.csdn.net/hawk199/article/details/125058030

-->

![image](http://182.92.69.8:8081/img/devops/k8s/k8s-20.png)  
1. 安装数据库etcd  
1. master节点安装组件：在 Kubemetes 的 Master 节点上需要部署的服务包括 etcd 、 kube-apiserver 、kube-controller-manager 和 kube-scheduler。    
2. node节点安装组件：在工作节点 (Worker Node ) 上需要部署的服务包括 docker 、 kubelet 和 kube-proxy 。    


安装kubelt的时候，已经安装了etcd数据库

## 1.2. 安装flannel组件
<!-- 
安装flannel组件
https://blog.csdn.net/weixin_45067241/article/details/126531465
-->


## 1.3. kubemetes-dashboard仪表盘安装  
<!-- 
安装
k8s入门：kubernetes-dashboard 安装
https://blog.csdn.net/qq_41538097/article/details/125561769

*** 配置用户
http://www.manongjc.com/detail/62-twpjlhgearkhued.html
https://www.soulchild.cn/post/2945


Dashboard 认证 - 配置登录权限
https://blog.csdn.net/qq_41619571/article/details/127217339

查看用户列表
https://blog.csdn.net/weixin_42350212/article/details/125460396

-->

 kubectl describe secrets cluster-admin-dashboard-wt
 

<!-- 

彻底搞懂 K8S Pod Pending 故障原因及解决方案
https://blog.csdn.net/xcbeyond/article/details/124580730

主节点无法调度 0/1 nodes are available: 1 node(s) had untolerated taint {node.kubernetes.io/not-ready: }
https://blog.csdn.net/hzwy23/article/details/128111446
https://cloud.tencent.com/developer/article/2090770

【Kubernetes系列】Kubernetes常见报错
https://blog.csdn.net/u012069313/article/details/125264651


*** k8s 1.24 taint污点修改
https://i4t.com/5471.html

查日志命令  k8s启动Pod遇到CrashLoopBackOff的解决方法
https://www.jianshu.com/p/bcc05427990d

-->


## 1.4. 集群安装
<!-- 
https://mp.weixin.qq.com/s/4zsGwYBLoiZx0l68NQPPMA
https://blog.csdn.net/qq_46595591/article/details/107520114?utm_medium=distribute.wap_relevant.none-task-blog-title-4
-->
&emsp; <font color = "red">整体参考《Kubernetes权威指南》</font>  


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

* **(推荐)使用<font color = "red">kubeadmin</font>通过离线镜像安装；**  
* 使用阿里公有云平台k8s；  
* 通过yum官方仓库安装；  
* 【二进制包的形式进行安装，kubeasz (github)；】  

&emsp; **通过kubeadm能够快速部署一个Kubernetes集群，但是如果需要精细调整Kubernetes各组件服务的参数及安全设置、高可用模式等，管理员就可以使用Kubernetes二进制文件进行部署。**  


## 1.5. 配置

### 1.5.1. Kubernetes集群的安全设置

1. 基于CA签名的双向数字证书认证方式
2. 基于HTTP BASE TOKEN的简单认证方式  

### 1.5.2. Kubernetes集群的网络配置
&emsp; [k8s网络配置](/docs/devAndOps/k8s/k8snetwork.md)  

### 1.5.3. 基于NFS文件集群共享  
<!-- 
Kubernetes 集群部署NFS网络存储
https://blog.csdn.net/zuozewei/article/details/108165523
-->
&emsp; Kubernetes对Pod进行调度时，以当时集群中各节点的可用资源作为主要依据，自动选择某一个可用的节点，并将Pod分配到该节点上。在这种情况下，Pod中容器数据的持久化如果存储在所在节点的磁盘上，就会产生不可预知的问题，例如，当Pod出现故障，Kubernetes 重新调度之后，Pod 所在的新节点上，并不存在上一次 Pod 运行时所在节点上的数据。  
&emsp; 为了使 Pod 在任何节点上都能够使用同一份持久化存储数据，需要使用网络存储的解决方案为Pod提供数据卷。常用的网络存储方案有：NFS/cephfs/glusterfs。  

&emsp; **Kubernetes集群网络存储与Pod挂载点的区别：**  
&emsp; <font color = "clime">Kubernetes集群网络存储是不同宿主机实现文件共享；Pod挂载点是容器与宿主机实现文件共享。</font>  

### 1.5.4. 内网中搭建私有仓库  
&emsp; ......
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
