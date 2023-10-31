
<!-- TOC -->

- [1. Kubernetes集群的网络配置](#1-kubernetes集群的网络配置)
    - [1.1. Kubemetes网络模型](#11-kubemetes网络模型)
    - [1.2. Docker的网络基础](#12-docker的网络基础)
    - [1.3. Docker的网络实现](#13-docker的网络实现)
    - [1.4. Kubemetes 的网络实现](#14-kubemetes-的网络实现)
    - [1.5. kube-proxy负载](#15-kube-proxy负载)
    - [1.6. 网络](#16-网络)
        - [1.6.1. 集群外部访问Pod或Service](#161-集群外部访问pod或service)
            - [1.6.1.1. 利用Rinetd实现Service负载均衡](#1611-利用rinetd实现service负载均衡)

<!-- /TOC -->

# 1. Kubernetes集群的网络配置
<!--

权威指南  第3.7章  

Kubernetes中容器到容器通信 
https://mp.weixin.qq.com/s/P-xKd6HeOGxyt-YXCnZjmQ

kubernetes集群网络
https://www.cnblogs.com/yuezhimi/p/13042037.html
-->

&emsp; 在多个Node组成的Kubernetes集群内，跨主机的容器间网络互通是Kubernetes集群能够正常工作的前提条件。Kubernetes本身并不会对跨主机的容器网络进行设置，这需要额外的工具来实现。除了谷歌公有云GCE平台提供的网络设置，一些开源的工具包括Flannel, Open vSwitch. Weave> Calico等都能够实现跨主机的容器间网络互通。  


## 1.1. Kubemetes网络模型
&emsp; K8S为Pod和Service资源对象分别使用了各自的专有网络，Pod网络由K8S的网络插件配置实现，而Service网络则由K8S集群进行指定。如下图：  
![image](http://182.92.69.8:8081/img/devops/k8s/k8s-14.png)  
&emsp; K8S使用的网络插件需要为每个Pod配置至少一个特定的地址，即Pod IP。Pod IP地址实际存在于某个网卡(可以是虚拟机设备)上。  
&emsp; <font color = "clime">而Service的地址却是一个虚拟IP地址，没有任何网络接口配置在此地址上，它由Kube-proxy借助iptables规则或ipvs规则重定向到本地端口，再将其调度到后端的Pod对象。</font>Service的IP地址是集群提供服务的接口，也称为Cluster IP。  
&emsp; Pod网络和IP由K8S的网络插件负责配置和管理，具体使用的网络地址可以在管理配置网络插件时进行指定，如10.244.0.0/16网络。而Cluster网络和IP是由K8S集群负责配置和管理，如10.96.0.0/12网络。  
&emsp; 从上图进行总结起来， **<font color = "clime">一个K8S集群包含三个网络：</font>**  

* 节点网络：各主机(Master、Node、ETCD等)自身所属的网络，地址配置在主机的网络接口，用于各主机之间的通信，又称为节点网络。  
* Pod网络：专用于Pod资源对象的网络，它是一个虚拟网络，用于为各Pod对象设定IP地址等网络参数，其地址配置在Pod中容器的网络接口上。Pod网络需要借助kubenet插件或CNI插件实现。  
* Service网络：专用于Service资源对象的网络，它也是一个虚拟网络，用于为K8S集群之中的Service配置IP地址，但是该地址不会配置在任何主机或容器的网络接口上，而是通过Node上的kube-proxy配置为iptables或ipvs规则，从而将发往该地址的所有流量调度到后端的各Pod对象之上。  

<!-- 
&emsp; K8S的网络中主要存在4种类型的通信：  

* ①同一Pod内的容器间通信  
* ②各个Pod彼此间的通信  
* ③Pod和Service间的通信  
* ④集群外部流量和Service之间的通信  
-->

## 1.2. Docker的网络基础


## 1.3. Docker的网络实现

## 1.4. Kubemetes 的网络实现



## 1.5. kube-proxy负载 


## 1.6. 网络
### 1.6.1. 集群外部访问Pod或Service  
<!-- 
从外部访问K8s中Pod的五种方式
https://blog.csdn.net/qq_23348071/article/details/87185025
-->
&emsp; <font color = "red">由于Pod和Service是Kubernetes集群范围内的虚拟概念，所以集群外的客户端系统无法通过Pod的IP地址或者Service的虚拟IP地址和虚拟端口号访问到它们。</font><font color = "clime">为了让外部客户端可以访问这些服务，可以将Pod或Service的端口号映射到宿主机，以使得客户端应用能够通过物理机访问容器应用。</font>  

1. 将容器应用的端口号映射到物理机。  
2. **将Service的端口号映射到物理机。**  

#### 1.6.1.1. 利用Rinetd实现Service负载均衡  
&emsp; 端口转发工具Rineted：Rineted是Linux操作系统中为重定向传输控制协议工具。可将源IP端口数据转发至目标IP端口。在Kubernetes中用于将service服务对外暴露。  



