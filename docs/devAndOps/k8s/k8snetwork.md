
<!-- TOC -->

- [1. Kubemetes网络原理](#1-kubemetes网络原理)
    - [1.1. Kubemetes网络模型](#11-kubemetes网络模型)
    - [1.2. Docker的网络基础和网络实现](#12-docker的网络基础和网络实现)
    - [1.3. Kubemetes 的网络实现](#13-kubemetes-的网络实现)
    - [1.4. Kubernetes 网络策略](#14-kubernetes-网络策略)
    - [1.5. 开源的网络组件](#15-开源的网络组件)

<!-- /TOC -->

# 1. Kubemetes网络原理  
<!--

权威指南  第3.7章  

** https://blog.csdn.net/Tiger_lin1/article/details/132297929

kubernetes集群网络
https://www.cnblogs.com/yuezhimi/p/13042037.html
深入解析k8s网络
https://blog.csdn.net/weixin_36755535/article/details/130389839
https://zhuanlan.zhihu.com/p/61677445?utm_id=0
-->


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


&emsp; K8S的网络中主要存在4种类型的通信：  

* ①同一Pod内的容器间通信  
* ②各个Pod彼此间的通信  
* ③Pod和Service间的通信  
* ④集群外部流量和Service之间的通信  


----------------------------
&emsp; 关于 Pod 如何接入网络这件事情，Kubernetes 做出了明确的选择。具体来说，Kubernetes 要求所有的网络插件实现必须满足如下要求：  

&emsp; 所有的 Pod 可以与任何其他 Pod 直接通信，无需使用 NAT 映射（network address translation）  
&emsp; 所有节点可以与所有 Pod 直接通信，无需使用 NAT 映射  
&emsp; Pod 内部获取到的 IP 地址与其他 Pod 或节点与其通信时的 IP 地址是同一个  

&emsp; 在这些限制条件下，需要解决如下四种完全不同的网络使用场景的问题：  

* Container-to-Container的网络
* Pod与Pod之间的网络  
* Pod与Service之间的网络  
* Internet-to-Service 的网络（集群外部与内部组件之间的通信）




## 1.2. Docker的网络基础和网络实现  
&emsp; 参考[网络：容器间通信](/docs/devAndOps/docker/network.md)    
&emsp; docker run创建Docker容器时，可以用 --net 选项指定容器的网络模式，Docker 有以下 4 种网络模式：

* host模式，使用 --net=host 指定。
* container模式，使用 --net=container:NAMEorID 指定。
* none模式，使用 --net=none 指定。
* bridge模式，使用 --net=bridge 指定，默认设置。


## 1.3. Kubemetes 的网络实现
&emsp; 在实际的业务场景中，业务组件之间的关系十分复杂，特别是随着微服务理念逐步深入人心，应用部署的粒度更加细小和灵活为了支持业务应用组件的通信联系，Kubemetes网络的设计主要致力于解决以下场景。
* 容器到容器之间的直接通信
* 抽象的 Pod 之间的通信。
* od erv ce 之间的通信
* 集群外部与内部组件之间的通信。


## 1.4. Kubernetes 网络策略

## 1.5. 开源的网络组件



