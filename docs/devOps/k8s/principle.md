


# Kubernetes  


<!-- 

https://www.cnblogs.com/linuxk/category/1248289.html

-->
<!-- 
k8s中文文档
https://www.kubernetes.org.cn/k8s

K8S组件运行原理详解总结
https://www.cnblogs.com/linuxk/p/10291178.html
Kubernetes关键组件及运行流程
https://juejin.im/post/6844903863443521550

-->

## 走进K8S

<!-- 

-->
Kubemetes的一些基本知识  
在Kubemetes中，Service (服务)是分布式集群架构的核心，一个Service对象拥有如下关 键特征。  
。拥有一个唯一指定的名字(比如mysql-server)。  
◎ 拥有一个虚拟 IP (Cluster IP, Service IP 或 VIP)和端口号。  
◎能够提供某种远程服务能力。  
◎被映射到了提供这种服务能力的一组容器应用上。  

Service的服务进程目前都基于Socket通信方式对外提供服务，比如Redis、Memcache> MySQL、Web Server,或者是实现了某个具体业务的一个特定的TCP Server进程。虽然一个 Service通常由多个相关的服务进程来提供服务，每个服务进程都有一个独立的Endpoint (IP+Port)访问点，但 Kubemetes 能够让我们通过 Service (虚拟 Cluster IP +Service Port)连接 到指定的Service上。有了 Kubemetes内建的透明负载均衡和故障恢复机制，不管后端有多少服 务进程，也不管某个服务进程是否会由于发生故障而重新部署到其他机器，都不会影响到我们 对服务的正常调用。更重要的是这个Service本身一旦创建就不再变化，这意味着在Kubemetes 集群中，我们再也不用为了服务的IP地址变来变去的问题而头疼了。  
容器提供了强大的隔离功能，所以有必要把为Service提供服务的这组进程放入容器中进行 隔离。为此，Kubemetes设计了 Pod对象，将每个服务进程包装到相应的Pod中，使其成为Pod 中运行的一个容器(Container)。为了建立Service和Pod间的关联关系，Kubemetes首先给每 个Pod贴上一个标签(Label),给运行MySQL的Pod贴上name=mysql标签，给运行PHP的 Pod贴上name=php标签，然后给相应的Service定义标签选择器（Label Selector）,比如MySQL Service的标签选择器的选择条件为name=mysql,意为该Service要作用于所有包含name=mysql Label的Pod上。这样一来，就巧妙地解决了 Service与Pod的关联问题。  
说到Pod,我们这里先简单介绍其概念。首先，Pod运行在一个我们称之为节点（Node） 的环境中，这个节点既可以是物理机，也可以是私有云或者公有云中的一个虚拟机，通常在一 个节点上运行几百个Pod；其次，每个Pod里运行着一个特殊的被称之为Pause的容器，其他 容器则为业务容器，这些业务容器共享Pause容器的网络栈和M）lume挂载卷，因此它们之间的 通信和数据交换更为高效，在设计时我们可以充分利用这一特性将一组密切相关的服务进程放 入同一个Pod中；最后，需要注意的是，并不是每个Pod和它里面运行的容器都能''映射”到 一个Service上，只有那些提供服务（无论是对内还是对外）的一组Pod才会被"映射”成一个 服务。
在集群管理方面,Kubemetes将集群中的机器划分为一个Master节点和一群工作节点（Node ）。 其中，在Master节点上运行着集群管理相关的一组进程kube-apiserver、kube-controller-manager 和kube-scheduler,这些进程实现了整个集群的资源管理、Pod调度、弹性伸缩、安全控制、系 统监控和纠错等管理功能，并且都是全自动完成的。Node作为集群中的工作节点，运行真正的 应用程序，在Node上Kubemetes管理的最小运行单元是Pod。Node上运行着Kubemetes的 kubelet, kube-proxy服务进程，这些服务进程负责Pod的创建、启动、监控、重启、销毁，以 及实现软件模式的负载均衡器。  
最后，我们再来看看传统的IT系统中服务扩容和服务升级这两个难题，以及Kubemetes 所提供的全新解决思路。服务的扩容涉及资源分配（选择哪个节点进行扩容）、实例部署和启动 等环节，在一个复杂的业务系统中，这两个问题基本上靠人工一步步操作才得以完成，费时费 力又难以保证实施质量。  
在Kubemetes集群中，你只需为需要扩容的Service关联的Pod创建一个RC （Replication Controller）,则该Service的扩容以至于后来的Service升级等头疼问题都迎刃而解。在一个RC 定义文件中包括以下3个关键信息。  
◎目标Pod的定义。  
。目标Pod需要运行的副本数量（Replicas）。  
◎要监控的目标Pod的标签（Label）o  
在创建好RC （系统将自动创建好Pod）后，Kubemetes会通过RC中定义的Label筛选出 对应的Pod实例并实时监控其状态和数量，如果实例数量少于定义的副本数量（Replicas）,则 会根据RC中定义的Pod模板来创建一个新的Pod,然后将此Pod调度到合适的Node上启动运 行，直到Pod实例的数量达到预定目标。这个过程完全是自动化的，无须人工干预。有了 RC, 服务的扩容就变成了一个纯粹的简单数字游戏了，只要修改RC中的副本数量即可。  

## 设计架构


## 核心技术概念和API对象
<!-- 
https://www.kubernetes.org.cn/kubernetes%e8%ae%be%e8%ae%a1%e7%90%86%e5%bf%b5
-->

## 集群组件  
<!-- 
k8s各个服务和执行流程介绍
https://blog.csdn.net/wang725/article/details/90138704
-->

## 运行流程  
<!-- 

整体访问流程
用户执行kubectl/userClient向apiserver发起一个命令，经过认证授权后，经过scheduler的各种策略，得到一个目标node，然后告诉apiserver，apiserver 会请求相关node的kubelet，通过kubelet把pod运行起来，apiserver还会将pod的信息保存在etcd；pod运行起来后，controllermanager就会负责管理pod的状态，如，若pod挂了，controllermanager就会重新创建一个一样的pod，或者像扩缩容等；pod有一个独立的ip地址，但pod的IP是易变的，如异常重启，或服务升级的时候，IP都会变，这就有了service；完成service工作的具体模块是kube-proxy；在每个node上都会有一个kube-proxy，在任何一个节点上访问一个service的虚拟ip，都可以访问到pod；service的IP可以在集群内部访问到，在集群外呢？service可以把服务端口暴露在当前的node上，外面的请求直接访问到node上的端口就可以访问到service了；
-->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/k8s/k8s-1.png)  

1. 准备好一个包含应用程序的Deployment的yml文件，然后通过kubectl客户端工具发送给ApiServer。  
2. ApiServer接收到客户端的请求并将资源内容存储到数据库(etcd)中。  
3. Controller组件(包括scheduler、replication、endpoint)监控资源变化并作出反应。  
4. ReplicaSet检查数据库变化，创建期望数量的pod实例。  
5. Scheduler再次检查数据库变化，发现尚未被分配到具体执行节点(node)的Pod，然后根据一组相关规则将pod分配到可以运行它们的节点上，并更新数据库，记录pod分配情况。  
6. Kubelete监控数据库变化，管理后续pod的生命周期，发现被分配到它所在的节点上运行的那些pod。如果找到新pod，则会在该节点上运行这个新pod。  
7. kuberproxy运行在集群各个主机上，管理网络通信，如服务发现、负载均衡。例如当有数据发送到主机时，将其路由到正确的pod或容器。对于从主机上发出的数据，它可以基于请求地址发现远程服务器，并将数据正确路由，在某些情况下会使用轮训调度算法(Round-robin)将请求发送到集群中的多个实例。  


### Service
 

## K8S的网络模型  





