


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




如果加入集群的token忘了，可以使用如下的命令获取最新的加入命令token

[root@k8scloude1 ~]# kubeadm token create --print-join-command
kubeadm join 192.168.110.130:6443 --token 8e3haz.m1wrpuf357g72k1u --discovery-token-ca-cert-hash sha256:9add1314177ac5660d9674dab8c13aa996520028514246c4cd103cf08a211cc8 
