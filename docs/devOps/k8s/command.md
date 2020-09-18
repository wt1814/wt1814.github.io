


# k8s使用  

## 安装k8s

* （推荐）使用kubeadmin通过离线镜像安装  
* 使用阿里公有云平台k8s  
* 通过yum官方仓库安装  
* 二进制包的形式进行安装，kubeasz (github)  

<!-- 
https://www.cnblogs.com/xiaoyuxixi/p/12142218.html
-->

## kubectl  
&emsp; kubeadm/kubelet/kubectl区别？  

* kubeadm是kubernetes集群快速构建工具
* kubelet运行在所有节点上，负责启动POD和容器，以系统服务形式出现
* kubectl：kubectl是kubenetes命令行工具，提供指令




Dashboard部署Tomcat集群

## Deployment（部署）  
Deployment脚本部署Tomcat集群

部署是指Kubernetes向Node节点发送指令，创建容器的过程  
Kubernetes支持yml格式的部署脚本  
kubectl create -f 部署yml文件 #创建部署  

## 利用Rinetd实现Service负载均衡  


## 外部访问Tomcat集群  



## 基于NFS文件集群共享  



## Web UI Dashboard  
<!-- 
Kubernetes的三种可视化UI界面
https://blog.51cto.com/14157628/2473866

kubernetes部署dashboard可视化插件
https://blog.csdn.net/networken/article/details/85607593?utm_medium=distribute.wap_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.wap_blog_relevant_pic&depth_1-utm_source=distribute.wap_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.wap_blog_relevant_pic


配置kubernetes UI图形化界面
https://blog.csdn.net/iouczp/article/details/80299775

-->


## IDE插件  
<!-- 
IDE 插件
https://mp.weixin.qq.com/s/KbcUxGJ3JK7ANtuDRvPzZQ

-->

