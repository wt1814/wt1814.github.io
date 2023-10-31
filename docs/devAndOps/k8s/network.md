
<!-- TOC -->

- [1. k8s网络](#1-k8s网络)
    - [1.1. Kubernetes的网络模型](#11-kubernetes的网络模型)
    - [1.2. kube-proxy负载](#12-kube-proxy负载)

<!-- /TOC -->

# 1. k8s网络  
## 1.1. Kubernetes的网络模型  
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

## 1.2. kube-proxy负载 



