


# Kubernetes  


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

## K8S的网络模型  





