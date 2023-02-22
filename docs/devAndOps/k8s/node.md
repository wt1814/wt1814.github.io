


# Kubemetes Node安装

kubelet  
kube-proxy，kube-proxy是集群中每个节点上运行的网络代理，实现Kubernetes服务（Service）概念的一部分。    
docker  

1. 安装 kubeadm、kubelet 和 kubectl，每个节点都需要安装  
2. 初始化主节点（只在master节点上执行）kubeadm init  


<!-- 
kubelet启动失败：  

升级containerd   https://blog.csdn.net/weixin_46476452/article/details/127837404

-->


sudo kubelet --container-runtime-endpoint=unix:///run/containerd/containerd.sock





