


# 1.2.2. Kubernetes集群的网络配置
<!--
 Kubernetes中容器到容器通信 
 https://mp.weixin.qq.com/s/P-xKd6HeOGxyt-YXCnZjmQ

kubernetes集群网络
https://www.cnblogs.com/yuezhimi/p/13042037.html

-->
&emsp; 在多个Node组成的Kubernetes集群内，跨主机的容器间网络互通是Kubernetes集群能够正常工作的前提条件。Kubernetes本身并不会对跨主机的容器网络进行设置，这需要额外的工具来实现。除了谷歌公有云GCE平台提供的网络设置，一些开源的工具包括Flannel, Open vSwitch. Weave> Calico等都能够实现跨主机的容器间网络互通。  
