
<!-- TOC -->

- [1. kubemetes安装](#1-kubemetes安装)
    - [1.1. 卸载k8s](#11-卸载k8s)
    - [1.2. kubemetes-dashboard仪表盘安装](#12-kubemetes-dashboard仪表盘安装)

<!-- /TOC -->


# 1. kubemetes安装  

## 1.1. 卸载k8s

<!-- 
https://blog.csdn.net/weixin_47752736/article/details/124855784
-->


## 1.2. kubemetes-dashboard仪表盘安装

<!-- 
kubernetes dashboard 开启http 免密登陆
https://www.jianshu.com/p/acf1e15e9200?utm_campaign=maleskine...&utm_content=note&utm_medium=seo_notes&utm_source=recommendation
kubernetes dashboard支持所有浏览器访问
https://blog.51cto.com/u_2837193/4926706
https://blog.csdn.net/wuchenlhy/article/details/128578633
K8s v1.25版本下可视化管理工具dashboardv2.7.0部署实践
https://baijiahao.baidu.com/s?id=1747656931703581518&wfr=spider&for=pc

-->

<!-- 
安装
k8s入门：kubernetes-dashboard 安装
https://blog.csdn.net/qq_41538097/article/details/125561769

*** 配置用户
http://www.manongjc.com/detail/62-twpjlhgearkhued.html
https://www.soulchild.cn/post/2945


Dashboard 认证 - 配置登录权限
https://blog.csdn.net/qq_41619571/article/details/127217339

查看用户列表
https://blog.csdn.net/weixin_42350212/article/details/125460396

-->

 kubectl describe secrets cluster-admin-dashboard-wt

<!-- 

彻底搞懂 K8S Pod Pending 故障原因及解决方案
https://blog.csdn.net/xcbeyond/article/details/124580730

主节点无法调度 0/1 nodes are available: 1 node(s) had untolerated taint {node.kubernetes.io/not-ready: }
https://blog.csdn.net/hzwy23/article/details/128111446
https://cloud.tencent.com/developer/article/2090770

【Kubernetes系列】Kubernetes常见报错
https://blog.csdn.net/u012069313/article/details/125264651


*** k8s 1.24 taint污点修改
https://i4t.com/5471.html

查日志命令  k8s启动Pod遇到CrashLoopBackOff的解决方法
https://www.jianshu.com/p/bcc05427990d

-->

