<!-- TOC -->

- [1. Kubernetes重要对象](#1-kubernetes重要对象)
    - [1.1. 深入掌握Pod](#11-深入掌握pod)
        - [1.1.1. 创建与管理pod/ReplicaSet](#111-创建与管理podreplicaset)
        - [pod容器共享Volume](#pod容器共享volume)
        - [1.1.2. Pod的升级和回滚](#112-pod的升级和回滚)
            - [1.1.2.1. kubernetes多种发布方式](#1121-kubernetes多种发布方式)
        - [1.1.3. Pod的扩容和缩容](#113-pod的扩容和缩容)
    - [1.2. 深入掌握Service](#12-深入掌握service)
        - [1.2.1. Service基本用法](#121-service基本用法)
            - [1.2.1.1. 外部Service](#1211-外部service)
        - [1.2.2. 集群外部访问Pod或Service](#122-集群外部访问pod或service)
        - [1.2.3. DNS服务搭建指南](#123-dns服务搭建指南)
        - [1.2.4. 自定义DNS和上游DNS服务器](#124-自定义dns和上游dns服务器)
        - [1.2.5. Ingress：HTTP 7层路由机制](#125-ingresshttp-7层路由机制)

<!-- /TOC -->

# 1. Kubernetes重要对象
## 1.1. 深入掌握Pod  
### 1.1.1. 创建与管理pod/ReplicaSet
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

### pod容器共享Volume

### 1.1.2. Pod的升级和回滚  
&emsp; 当集群中的某个服务需要升级时，需要停止目前与该服务相关的所有Pod，然后下载新版本镜像并创建新的Pod。如果集群规模比较大，则这个工作就变成了一个挑战，而且先全部停止然后逐步升级的方式会导致较长时间的服务不可用。Kubemetes提供了滚动升级功能来解决上述问题。  
&emsp; 如果Pod是通过Deployment创建的，则用户可以在运行时修改Deployment的Pod定义 (spec.template)或镜像名称，并应用到Deployment对象上，系统即可完成Deployment的自动更新操作。如果在更新过程中发生了错误，则还可以通过回滚(Rollback)操作恢复Pod的版本。  

#### 1.1.2.1. kubernetes多种发布方式  
<!-- 

 k8s中蓝绿部署、金丝雀发布、滚动更新汇总 
 https://mp.weixin.qq.com/s?__biz=MzU0NjEwMTg4Mg==&mid=2247484195&idx=1&sn=b841f2ea305acfa2996a667d4ff4d99e&chksm=fb638c36cc140520e6905db5923afe163d7babb5d9eb6c5e8045a795c37b33a2a2e5541e3efd&scene=21#wechat_redirect
-->

**Kubernetes蓝绿部署，金丝雀发布，滚动更新的介绍**  

* 金丝雀发布（又称灰度发布、灰度更新）：  
&emsp; 金丝雀发布一般是先发1台机器，或者一个小比例，例如2%的服务器，主要做流量验证用，也称为金丝雀 (Canary) 测试，国内常称灰度测试。以前旷工下矿前，会先放一只金丝雀进去用于探测洞里是否有有毒气体，看金丝雀能否活下来，金丝雀发布由此得名。简单的金丝雀测试一般通过手工测试验证，复杂的金丝雀测试需要比较完善的监控基础设施配合，通过监控指标反馈，观察金丝雀的健康状况，作为后续发布或回退的依据。如果金丝测试通过，则把剩余的 V1 版本全部升级为 V2 版本。如果金丝雀测试失败，则直接回退金丝雀，发布失败。  
* 滚动更新：  
&emsp; 在金丝雀发布基础上的进一步优化改进，是一种自动化程度较高的发布方式，用户体验比较平滑，是目前成熟型技术组织所采用的主流发布方式。<font color = "red">一次滚动式发布一般由若干个发布批次组成，每批的数量一般是可以配置的（可以通过发布模板定义）。例如，第一批1台（金丝雀），第二批10%，第三批 50%，第四批100%。</font>每个批次之间留观察间隔，通过手工验证或监控反馈确保没有问题再发下一批次，所以总体上滚动式发布过程是比较缓慢的 (其中金丝雀的时间一般会比后续批次更长，比如金丝雀10 分钟，后续间隔 2分钟)。  
* 蓝绿部署：  
&emsp; <font color = "lime">一些应用程序只需要部署一个新版本，并需要立即切到这个版本。因此，需要执行蓝/绿部署。</font><font color = "red">在进行蓝/绿部署时，应用程序的一个新副本（绿）将与现有版本（蓝）一起部署。然后更新应用程序的入口/路由器以切换到新版本（绿）。然后，需要等待旧（蓝）版本来完成所有发送给它的请求，但是大多数情况下，应用程序的流量将一次更改为新版本；Kubernetes不支持内置的蓝/绿部署。</font>目前最好的方式是创建新的部署，然后更新应用程序的服务（如service）以指向新的部署；蓝绿部署是不停老版本，部署新版本然后进行测试，确认OK后将流量逐步切到新版本。蓝绿部署无需停机，并且风险较小。  

### 1.1.3. Pod的扩容和缩容  
&emsp; 在实际生产系统中，经常会遇到某个服务需要扩容的场景，也可能会遇到由于资源紧张或者工作负载降低而需要减少服务实例数量的场景。此时可以利用Deployment/RC的Scale机制来完成这些工作。  
&emsp; Kubernetes对Pod的扩容和缩容操作提供了手动和自动两种模式，手动模式通过执行kubectl scale命令对一个Deployment/RC进行Pod副本数量的设置，即可一键完成。自动模式则需要用户根据某个性能指标或者自定义业务指标，并指定Pod副本数量的范围，系统将自动在这个范围内根据性能指标的变化进行调整。  

## 1.2. 深入掌握Service  
<!-- 
https://blog.csdn.net/PpikachuP/article/details/89674578
-->
&emsp; Service是Kubernetes最核心的概念，通过创建Service，可以为一组具有相同功能的容器应用提供一个统一的入口地址，并且将请求负载分发到后端的各个容器应用上。本节对Service的使用进行详细说明，包括Service的负载均衡、外网访问、DNS服务的搭建、Ingress7层路由机制等。  

### 1.2.1. Service基本用法  
#### 1.2.1.1. 外部Service  
&emsp; 在某些环境中，应用系统需要将一个外部数据库作为后端服务进行连接，或将另一个集群或Namespace中的服务作为服务的后端，这时可以通过创建一个无Label Selector的Service来实现。  
 
### 1.2.2. 集群外部访问Pod或Service  
<!-- 
从外部访问K8s中Pod的五种方式
https://blog.csdn.net/qq_23348071/article/details/87185025

-->
&emsp; <font color = "red">由于Pod和Service是Kubernetes集群范围内的虚拟概念，所以集群外的客户端系统无法通过Pod的IP地址或者Service的虚拟IP地址和虚拟端口号访问到它们。</font><font color = "lime">为了让外部客户端可以访问这些服务，可以将Pod或Service的端口号映射到宿主机，以使得客户端应用能够通过物理机访问容器应用。</font>  

1. 将容器应用的端口号映射到物理机  
2. 将Service的端口号映射到物理机  

### 1.2.3. DNS服务搭建指南  
&emsp; 作为服务发现机制的基本功能，在集群内需要能够通过服务名对服务进行访问，这就需要一个集群范围的DNS服务来完成服务名到ClusterIP的解析。  

### 1.2.4. 自定义DNS和上游DNS服务器  
&emsp; 在实际环境中，很多用户都有自己的私有域名区域，并且希望能够集成到Kubernetes DNS 的命名空间中，例如混合云用户可能希望能在集群内解析其内部的".corp"域名；用户也可能己存在一个未被Kubernetes管理的服务发现系统（例如Consul）来完成域名解析。从Kubernetes vl.6版本开始，用户可以在Kubernetes集群内配置私有DNS区域（通常称为存根域Stub Domain）和外部的上游域名服务了。  

### 1.2.5. Ingress：HTTP 7层路由机制  
&emsp; Service的表现形式为IP:Port，即工作在TCP/IP 层。而对于基于HTTP的服务来说，不同的URL地址经常对应到不同的后端服务或者虚拟服务 器（Virtual Host），这些应用层的转发机制仅通过Kubernetes的Service机制是无法实现的。从 Kubernetes vl.l版本开始新增Ingress资源对象，用于将不同URL的访问请求转发到后端不同 的Service，以实现HTTP层的业务路由机制。Kubernetes使用一个Ingress策略定义和一个具体的Ingress Controller，两者结合并实现了一个完整的Ingress负载均衡器。  
&emsp; **使用Ingress进行负载分发时，Ingress Controller将基于Ingress规则将客户端请求直接转发到Service对应的后端Endpoint（即Pod）上，这样会跳过kube-proxy的转发功能，kube-proxy不再起作用。**如果Ingress Controller提供的是对外服务，则实际上实现的是边缘路由器的功能。  

