
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


<!-- 

k8s 给dashboard配置自定义证书
https://www.cnblogs.com/RRecal/p/15747097.html

kubernetes dashboard 2.x 配置https证书
https://blog.csdn.net/weixin_35306450/article/details/112805986

kubernetes学习笔记之十一：kubernetes dashboard认证及分级授权
https://www.cnblogs.com/panwenbin-logs/p/10052554.html
https://www.sklinux.com/posts/devops/k8s%E9%9D%A2%E6%9D%BFhttps%E4%BF%AE%E7%90%86/

-->
k8s 给dashboard配置自定义证书
https://www.cnblogs.com/RRecal/p/15747097.html  

1. 下载recommended.yaml
2. 修改nodePort
3. 修改jar包地址
4. 修改http证书  
    1. kubectl create namespace kubernetes-dashboard
	2. kubectl create secret tls mycentos-cert --key 9161214_www.wt1814.com.key --cert 9161214_www.wt1814.com.pem -n kubernetes-dashboard 
    3. 查看证书内容  kubectl describe secrets/证书名称  
5. 查看token




