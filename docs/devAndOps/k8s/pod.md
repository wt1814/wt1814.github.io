

<!-- TOC -->

- [1. Pod详解](#1-pod详解)
    - [1.1. 创建与管理pod/ReplicaSet](#11-创建与管理podreplicaset)
    - [1.2. pod容器共享Volume](#12-pod容器共享volume)
    - [1.3. Pod的升级和回滚](#13-pod的升级和回滚)
        - [1.3.1. kubernetes多种发布方式](#131-kubernetes多种发布方式)
    - [1.4. Pod的扩容和缩容](#14-pod的扩容和缩容)

<!-- /TOC -->

# 1. Pod详解  
## 1.1. 创建与管理pod/ReplicaSet
&emsp; ......
<!-- 

k8s笔记二(pod资源的创建与管理)
https://blog.csdn.net/dayi_123/article/details/88683870
k8s创建资源的两种方式、访问pod
https://blog.csdn.net/PpikachuP/article/details/89674578
Kubernetes学习笔记——k8s创建Pod和ReplicaSet的工作流程
https://blog.csdn.net/weixin_38070561/article/details/82706973
K8S：创建pod资源两种方式： kubectl命令 && yaml文件
https://blog.csdn.net/weixin_45691464/article/details/106006125
-->

## 1.2. pod容器共享Volume
&emsp; ......

## 1.3. Pod的升级和回滚  
&emsp; 当集群中的某个服务需要升级时，需要停止目前与该服务相关的所有Pod，然后下载新版本镜像并创建新的Pod。如果集群规模比较大，则这个工作就变成了一个挑战， **<font color = "red">而且先全部停止，然后逐步升级的方式会导致较长时间的服务不可用。</font>** **<font color = "clime">Kubemetes提供了滚动升级功能来解决上述问题。</font>**   
&emsp; 如果Pod是通过Deployment创建的，则用户可以在运行时修改Deployment的Pod定义 (spec.template)或镜像名称，并应用到Deployment对象上，系统即可完成Deployment的自动更新操作。如果在更新过程中发生了错误，则还可以通过回滚(Rollback)操作恢复Pod的版本。  

### 1.3.1. kubernetes多种发布方式  
<!-- 
k8s中蓝绿部署、金丝雀发布、滚动更新汇总 
https://mp.weixin.qq.com/s?__biz=MzU0NjEwMTg4Mg==&mid=2247484195&idx=1&sn=b841f2ea305acfa2996a667d4ff4d99e&chksm=fb638c36cc140520e6905db5923afe163d7babb5d9eb6c5e8045a795c37b33a2a2e5541e3efd&scene=21#wechat_redirect
-->
[多种发布方式](/docs/system/publishe.md)  

&emsp; kubernetes支持金丝雀发布(又称灰度发布、灰度更新)、滚动更新。  
&emsp; **<font coclor = "clime">Kubernetes不支持内置的蓝/绿部署。</font>** 目前最好的方式是创建新的部署，然后更新应用程序的服务(如service)以指向新的部署；蓝绿部署是不停老版本，部署新版本然后进行测试，确认OK后将流量逐步切到新版本。蓝绿部署无需停机，并且风险较小。  


* 金丝雀发布(又称灰度发布、灰度更新)：  
&emsp; 金丝雀发布一般是先发1台机器，或者一个小比例，例如2%的服务器，主要做流量验证用，也称为金丝雀 (Canary) 测试，国内常称灰度测试。以前旷工下矿前，会先放一只金丝雀进去用于探测洞里是否有有毒气体，看金丝雀能否活下来，金丝雀发布由此得名。简单的金丝雀测试一般通过手工测试验证，复杂的金丝雀测试需要比较完善的监控基础设施配合，通过监控指标反馈，观察金丝雀的健康状况，作为后续发布或回退的依据。如果金丝测试通过，则把剩余的 V1 版本全部升级为 V2 版本。如果金丝雀测试失败，则直接回退金丝雀，发布失败。  
* 滚动更新：  
&emsp; 在金丝雀发布基础上的进一步优化改进，是一种自动化程度较高的发布方式，用户体验比较平滑，是目前成熟型技术组织所采用的主流发布方式。<font color = "red">一次滚动式发布一般由若干个发布批次组成，每批的数量一般是可以配置的(可以通过发布模板定义)。例如，第一批1台(金丝雀)，第二批10%，第三批 50%，第四批100%。</font>每个批次之间留观察间隔，通过手工验证或监控反馈确保没有问题再发下一批次，所以总体上滚动式发布过程是比较缓慢的 (其中金丝雀的时间一般会比后续批次更长，比如金丝雀10 分钟，后续间隔 2分钟)。  
* 蓝绿部署：  
&emsp; <font color = "clime">一些应用程序只需要部署一个新版本，并需要立即切到这个版本。因此，需要执行蓝/绿部署。</font><font color = "red">在进行蓝/绿部署时，应用程序的一个新副本(绿)将与现有版本(蓝)一起部署。然后更新应用程序的入口/路由器以切换到新版本(绿)。然后，需要等待旧(蓝)版本来完成所有发送给它的请求，但是大多数情况下，应用程序的流量将一次更改为新版本</font>   


## 1.4. Pod的扩容和缩容  
&emsp; 在实际生产系统中，经常会遇到某个服务需要扩容的场景，也可能会遇到由于资源紧张或者工作负载降低而需要减少服务实例数量的场景。此时可以利用Deployment/RC的Scale机制来完成这些工作。  
&emsp; **<font color = "clime">Kubernetes对Pod的扩容和缩容操作提供了手动和自动两种模式。</font>** 手动模式通过执行kubectl scale命令对一个Deployment/RC进行Pod副本数量的设置，即可一键完成。自动模式则需要用户根据某个性能指标或者自定义业务指标，并指定Pod副本数量的范围，系统将自动在这个范围内根据性能指标的变化进行调整。  