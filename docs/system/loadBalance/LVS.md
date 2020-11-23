<!-- TOC -->

- [1. LVS](#1-lvs)
    - [1.1. LVS 的组成及作用](#11-lvs-的组成及作用)
        - [ipvsadm用法](#ipvsadm用法)
    - [1.2. LVS的体系结构](#12-lvs的体系结构)
    - [1.3. 三种工作模式](#13-三种工作模式)
        - [1.3.1. NAT（地址转换）](#131-nat地址转换)
        - [1.3.2. TUN（隧道）](#132-tun隧道)
        - [1.3.3. DR（直接路由）](#133-dr直接路由)
    - [1.4. LVS的十种负载调度算法](#14-lvs的十种负载调度算法)

<!-- /TOC -->

# 1. LVS

<!-- 



 超详细！一文带你了解 LVS 负载均衡集群！
https://mp.weixin.qq.com/s/3Ahb299iBScC3Znrc7NUNQ



官网：http://zh.linuxvirtualserver.org/




LVS
https://blog.csdn.net/qq_37165604/article/details/79802390
https://www.cnblogs.com/MacoLee/p/5856858.html
https://www.jianshu.com/p/7a063123d1f1
https://www.cnblogs.com/cq146637/p/8517818.html
https://blog.csdn.net/ghost_leader/article/details/55827729
-->


&emsp; LVS是Linux Virtual Server的简写，意即Linux虚拟服务器，是一个虚拟的服务器集群系统。  


## 1.1. LVS 的组成及作用  
<!-- 
https://mp.weixin.qq.com/s/3Ahb299iBScC3Znrc7NUNQ
-->

### ipvsadm用法  
https://blog.csdn.net/qq_37165604/article/details/79802390

## 1.2. LVS的体系结构  

https://mp.weixin.qq.com/s/3Ahb299iBScC3Znrc7NUNQ
https://mp.weixin.qq.com/s/IsVBnld4yYXyQ_6xNlAjLA
https://mp.weixin.qq.com/s/cjDSNb9H6HO1DYup7223_g

## 1.3. 三种工作模式
LVS 是四层负载均衡，也就是说建立在 OSI 模型的第四层——传输层之上，LVS 支持 TCP/UDP 的负载均衡。  
LVS 的转发主要通过修改 IP 地址（NAT 模式，分为源地址修改 SNAT 和目标地址修改 DNAT）、修改目标 MAC（DR 模式）来实现。  

https://mp.weixin.qq.com/s/HtOH8-tmCo6bjFoDBhqK_g

### 1.3.1. NAT（地址转换）  


### 1.3.2. TUN（隧道）  


### 1.3.3. DR（直接路由）  


## 1.4. LVS的十种负载调度算法  
https://mp.weixin.qq.com/s/3Ahb299iBScC3Znrc7NUNQ
https://blog.csdn.net/qq_37165604/article/details/79802390