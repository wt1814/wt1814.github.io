
<!-- TOC -->

- [1. Kubernetes运维指南](#1-kubernetes运维指南)
    - [1.1. Kubernetes集群管理指南](#11-kubernetes集群管理指南)
        - [1.1.1. 使用WebUI(Dashboard)管理集群](#111-使用webuidashboard管理集群)
        - [1.1.2. 集群统一日志管理](#112-集群统一日志管理)
        - [1.1.3. Node的管理](#113-node的管理)
        - [1.1.4. 更新资源对象的Label](#114-更新资源对象的label)
        - [1.1.5. Namespace：集群环境共享与隔离](#115-namespace集群环境共享与隔离)
        - [1.1.6. Kubemetes集群的高可用部署方案](#116-kubemetes集群的高可用部署方案)
        - [1.1.7. Kubemetes集群监控](#117-kubemetes集群监控)
        - [1.1.8. Helm：Kubemetes应用包管理工具](#118-helmkubemetes应用包管理工具)
    - [1.2. Trouble Shooting指导](#12-trouble-shooting指导)
        - [1.2.1. 査看系统Event事件](#121-査看系统event事件)
        - [1.2.2. 查看容器日志](#122-查看容器日志)
        - [1.2.3. 査看 Kubemetes服务日志](#123-査看-kubemetes服务日志)

<!-- /TOC -->

# 1. Kubernetes运维指南  

&emsp; <font color = "red">整体参考《Kubernetes权威指南》</font>  

## 1.1. Kubernetes集群管理指南  
&emsp; 本节将从Node的管理、Label的管理、Namespace资源共享、资源配额管理、集群Master高可用及集群监控等方面，对Kubernetes集群本身的运维管理进行详细说明。  

### 1.1.1. 使用WebUI(Dashboard)管理集群  
<!-- 
kubernetes部署dashboard可视化插件
https://blog.csdn.net/networken/article/details/85607593?utm_medium=distribute.wap_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.wap_blog_relevant_pic&depth_1-utm_source=distribute.wap_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.wap_blog_relevant_pic
-->
&emsp; Kubernetes的可视化UI界面有官方提供的插件kubemetes-dashboard。使用kubeadm安装kubernetes-dashboard。  

### 1.1.2. 集群统一日志管理  
<!-- 
Kubernetes日志的6个最佳实践 
https://mp.weixin.qq.com/s/aPLE5N6Re-cUgTJR3MrICQ

kubernetes搭建EFK日志管理系统
https://mp.weixin.qq.com/s/sXl4KkoweCkSYL7k5G0ycQ
 kubernetes集群中部署EFK日志管理系统
https://mp.weixin.qq.com/s/oCOKYOgak3PjmHnFiAin7g

 2020年Kubernetes中7个最佳日志管理工具 
 https://mp.weixin.qq.com/s/hHOoXhq1Xcj5QD0D5XekhA  
-->

&emsp; 在Kubemetes集群环境中，一个完整的应用或服务都会涉及为数众多的组件运行，各组件所在的Node及实例数量都是可变的。日志子系统如果不做集中化管理，则会给系统的运维支撑造成很大的困难，因此有必要在集群层面对日志进行统一的收集和检索等工作。  
&emsp; <font color = "red">容器中输出到控制台的日志，都会以*-json.log的命名方式保存在/var/lib/docker/containers/目录下，这样就有了进行日志釆集和后续处理的基础。</font>  
&emsp; <font color = "cclime">Kubemetes推荐釆用Elasticsearch+Fluentd+Kibana完成对系统和容器日志的釆集、査询和展现工作。</font>  
&emsp; 在部署统一日志管理系统之前，需要以下两个前提条件。  

* API Server正确配置了CA证书。  
* DNS服务启动运行。  

&emsp; 系统的逻辑架构如下图所示：  
![image](http://www.wt1814.com/static/view/images/devops/k8s/k8s-4.png)  

&emsp; 在各Node上运行一个Fluentd容器，釆集本节点/var/log和/var/lib/docker/containers两个目录下的日志进程，然后汇总到Elasticsearch集群，最终通过Kibana完成和用户的交互工作。  
&emsp; 这里有一个特殊的需求，Fluentd必须在每个Node运行一份，为了满足这一需要，有以下几种不同的方式来部署Fluentdo：  

* 直接在Node主机上部署Fluentdo；  
* 利用kubelet的—config参数，为每个Node加载Fluentd Pod；  
* 利用DaemonSet来让Fluentd Pod在每个Node上运行。  

&emsp; 目前官方推荐的包括Fluentd > Logstash等日志或者监控类的Pod的运行方式就是DaemonSet方式。

### 1.1.3. Node的管理  
1. Node的隔离与恢复  
&emsp; 在硬件升级、硬件维护等情况下，需要将某些Node进行隔离，脱离Kubernetes集群的调度范围。Kubernetes提供了一种机制，既可以将Node纳入调度范围，也可以将Node脱离调度范围。  
2. Node的扩容  
&emsp; 在实际生产系统中会经常遇到服务器容量不足的情况，这时就需要购买新的服务器，然后将应用系统进行水平扩展来完成对系统的扩容。  
&emsp; 在Kubernetes集群中，一个新Node的加入是非常简单的。在新的Node节点上安装Docker、kubelet和kube-proxy服务，然后配置kubelet和kube-proxy的启动参数，将Master URL指定为当前Kubernetes集群Master的地址，最后启动这些服务。通过kubelet默认的自动注册机制，新的Node将会自动加入现有的Kubemetes集群中。    

&emsp; [k8s自动伸缩](/docs/devAndOps/k8s/Stretch.md)  

### 1.1.4. 更新资源对象的Label  
&emsp; <font color = "clime">Label(标签)作为用户可灵活定义的对象属性，在正在运行的资源对象上，仍然可以随时通过kubectl label命令对其进行增加、修改、删除等操作。</font>  

### 1.1.5. Namespace：集群环境共享与隔离  
&emsp; <font color = "clime">在一个组织内部，不同的工作组可以在同一个Kubemetes集群中工作，Kubemetes通过命名空间和Context的设置来对不同的工作组进行区分，使得它们既可以共享同一个Kubemetes集群的服务，也能够互不干扰，</font>如下图所示。  
![image](http://www.wt1814.com/static/view/images/devops/k8s/k8s-2.png)  
&emsp; 假设有两个工作组：开发组和生产运维组。开发组在Kubemetes集群中需要不断创建、修改、删除各种Pod、RC. Service等资源对象，以便实现敏捷开发的过程。而生产运维组则需要使用严格的权限设置来确保生产系统中的Pod、RC、Service处于正常运行状态且不会被误操作。  

### 1.1.6. Kubemetes集群的高可用部署方案  
<!-- 
 Kubernetes 高可用方案 
 https://mp.weixin.qq.com/s/yUynU3hagPxuyPtP9N2jxA
-->

&emsp; Kubemetes作为容器应用的管理平台，通过对Pod的运行状况进行监控，并且根据主机或容器失效的状态将新的Pod调度到其他Node上，实现了应用层的高可用性。针对Kubernetes集群，高可用性还应包含以下两个层面的考虑：etcd数据存储的高可用性和Kubernetes Master组件的高可用性。  

### 1.1.7. Kubemetes集群监控  
&emsp; [k8s监控](/docs/devAndOps/k8s/Monitor.md)  

### 1.1.8. Helm：Kubemetes应用包管理工具
&emsp; 随着容器技术逐渐被企业接受，简单的应用在Kubernetes上己经能够便捷部署。但对于复杂的应用中间件，在Kubemetes上进行容器化部署并非易事，通常需要先研究Docker镜像的运行需求、环境变量等内容，并能为这些容器定制存储、网络等设置，最后设计和编写Deployment. Configmap, Service及Ingress等相关yaml配置文件，再提交给Kubemetes进行部署。这些复杂的过程将逐步被Helm应用包管理工具实现。  

## 1.2. Trouble Shooting指导  

<!-- 
Kubernetes 问题定位技巧：容器内抓包
https://mp.weixin.qq.com/s/JlC8yCj-WOOCNOPo3V4_sQ

-->

&emsp; 本节将对Kubernetes集群中常见问题的排査方法进行说明。  
&emsp; 为了跟踪和发现Kubernetes集群中运行的容器应用出现的问题，常用的査错方法如下：  

* 首先，査看Kubernetes对象的当前运行时信息，特别是与对象关联的Event事件。这些事件记录了相关主题、发生时间、最近发生时间、发生次数及事件原因等，对排査故障非常有价值。此外，通过査看对象的运行时数据，还可以发现参数错误、关联错误、状态异常等明显问题。由于Kubernetes中多种对象相互关联，因此，这一步可能会涉及多个相关对象的排査问题。
* 其次，对于服务、容器的问题，则可能需要深入容器内部进行故障诊断，此时可以通过査看容器的运行日志来定位具体问题。  
* 最后，对于某些复杂问题，比如Pod调度这种全局性的问题，可能需要结合集群中每个节点上的Kubernetes服务日志来排查。比如搜集Master上kube-apiserver> kube-schedule> kube-controler-manager服务的日志，以及各个Node节点上的kubelet、kube-proxy服务的日志, 综合判断各种信息，就能找到问题的成因并解决问题。  

### 1.2.1. 査看系统Event事件  
&emsp; 在Kubernetes集群中创建了Pod之后，可以通过kubectl get pods命令査看Pod列表，但该命令能够显示的信息很有限。Kubernetes提供了kubectl describe pod命令来査看一个Pod的详细信息。  

### 1.2.2. 查看容器日志  
&emsp; 在需要排查容器内部应用程序生成的日志时，可以使用kubectl logs \<pod_name>命令。

### 1.2.3. 査看 Kubemetes服务日志  
&emsp; 如果在Linux系统上进行安装，并且使用systemd系统来管理Kubernetes服务，那么systemd 的journal系统会接管服务程序的输出日志。在这种环境中，可以通过使用systemd status或joumalctl工具来查看系统服务的日志。  
