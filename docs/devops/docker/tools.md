
<!-- TOC -->

- [1. Docker工具](#1-docker工具)
    - [1.1. Idea中Docker插件](#11-idea中docker插件)
    - [1.2. Docker环境管理UI](#12-docker环境管理ui)
    - [1.3. Docker容器的日志处理](#13-docker容器的日志处理)
        - [1.3.1. Docker引擎日志](#131-docker引擎日志)
        - [1.3.2. 容器日志](#132-容器日志)
    - [1.4. Docker容器故障排查工具](#14-docker容器故障排查工具)
    - [1.5. Docker Compose](#15-docker-compose)

<!-- /TOC -->

# 1. Docker工具  
<!-- 
推荐 5 款超好用的开源 Docker 工具！
https://mp.weixin.qq.com/s/jxMuLZNsMacKDXoVmooS-A
使用Prometheus收集Docker指标
https://docs.docker.com/config/daemon/prometheus/
-->

## 1.1. Idea中Docker插件  
<!-- 
https://mp.weixin.qq.com/s?__biz=MzAxNDMwMTMwMw==&mid=2247492037&idx=1&sn=5568994f8c801f56170b14f2d21df31c&chksm=9b97c0ddace049cb57d4396eabc3965532eb128ab83a55d3c8d7dea3d8be675dc8c0c8ebfba3&mpshare=1&scene=1&srcid=&sharer_sharetime=1565841354339&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=a98b434d6faae616ed91d3ea273cb1b2030141b502c3589cff178a48e66b895a407b58f1d6b6ffafcf8c3ced4828833e6652a8869d6d35edddf1f192fe618738afaaa152a55d00024b42ce09a67b0b99&ascene=1&uin=MTE1MTYxNzY2MQ==&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=SNTjToR4G4GRQcv6vbTgQCeljdugS8QdOBuRNyGrRTrVOdRDMoEgnHo3VlytJ0fv

使用 Docker 部署 Spring Boot 项目
https://mp.weixin.qq.com/s?__biz=MzI4NDY5Mjc1Mg==&mid=2247489662&idx=2&sn=8227bfd4b0b68ddc002dfe451a661688&chksm=ebf6c001dc8149172341b190c9b72700fed794261d67b8648edd2a596019212a742c7b241b90&mpshare=1&scene=1&srcid=&sharer_sharetime=1572833030053&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=6f23511bf9e1c01fc8b70c3e81dbcf33c57a5d08ea0ef51caa9a619fffb3e59ba744ac23ec082bfc8791797c1917e1e4f0290dff6475d1b71f64d8252bf92952180c025b0121995474ac59fe778892a9&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=Lu%2FLBuTxuGaOTLq0CL9dO0ss3p9k%2BNlDhrOCgfGfCUsKTPyuc12lccq3vmkXvxfb

提升10倍生产力：IDEA远程一键部署SpringBoot 
https://mp.weixin.qq.com/s?__biz=MzAxNDMwMTMwMw==&mid=2247492037&idx=1&sn=5568994f8c801f56170b14f2d21df31c&chksm=9b97c0ddace049cb57d4396eabc3965532eb128ab83a55d3c8d7dea3d8be675dc8c0c8ebfba3&mpshare=1&scene=1&srcid=&sharer_sharetime=1565841354339&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=a98b434d6faae616ed91d3ea273cb1b2030141b502c3589cff178a48e66b895a407b58f1d6b6ffafcf8c3ced4828833e6652a8869d6d35edddf1f192fe618738afaaa152a55d00024b42ce09a67b0b99&ascene=1&uin=MTE1MTYxNzY2MQ==&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=SNTjToR4G4GRQcv6vbTgQCeljdugS8QdOBuRNyGrRTrVOdRDMoEgnHo3VlytJ0fv
-->


## 1.2. Docker环境管理UI   
&emsp; docker图形页面管理工具常用的有三种：DockerUI、Portainer、Shipyard。  
&emsp; DockerUI 是 Portainer 的前身，这三个工具通过docker api来获取管理的资源信息。平时常常对着shell对着这些命令行客户端，审美会很疲劳，如果有漂亮的图形化界面可以直观查看docker资源信息，也是非常方便的。这三种图形化管理工具以Portainer最为受欢迎。 

<!--
～～
https://mp.weixin.qq.com/s?__biz=MzI5ODQ2MzI3NQ==&mid=2247488015&idx=1&sn=f7640dc2aab0d95245ba8b521adc5eb3&chksm=eca42b4bdbd3a25d75ac2c30fdaa25a38a0db560b0e85f0794c56a07d537f75163dd42788c50&mpshare=1&scene=1&srcid=&sharer_sharetime=1569341646799&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=2a4ff15fdd846346cca6724726c97c2197535a0af73ab946d789ddcb5bca832c59fc3b05e664926e093344c971b6894e335e858ff18a4493851dba74507020c7655f3aa9bb719e55759c5086086dea2f&ascene=1&uin=MTE1MTYxNzY2MQ==&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=+eyejsSYMk60ZUY/pK4YBKveSPIi8GUzrQvyveBksIipCvi8KhPkraBA4Eyx2jY/

https://mp.weixin.qq.com/s/Z_1xX1q5CDhD46b4KeK0bg

Docker 图形化页面管理工具使用 
https://mp.weixin.qq.com/s/wy99yOT3S6g0QLUIDf4DSQ
-->
## 1.3. Docker容器的日志处理  
<!-- 
https://docs.docker.com/config/formatting/
docker容器日志管理
https://www.cnblogs.com/caibao666/p/12084928.html
Docker容器的日志处理
https://mp.weixin.qq.com/s/1CT1K9UPWN8k2G_JkK3EwA、

-->
&emsp; **<font color = "red">docker容器日志分为两类：docker引擎日志(Docker本身运行的日志)和容器日志(各个容器内产生的日志)。**</font>  

### 1.3.1. Docker引擎日志  

### 1.3.2. 容器日志  


## 1.4. Docker容器故障排查工具  
&emsp; Docker-debug  
<!-- 
Docker 容器故障排查工具
https://mp.weixin.qq.com/s/8lOTyNogYRFsqLpE45217w
-->

## 1.5. Docker Compose  
&emsp; 跨越多台机器，docker compose就无能为例了：比如如何进行跨越多台机器的增删改查，比如不同机器之间的container如何通信等等。于是开始使用K8s来进行容器的管理维护了。  
<!-- 
Docker从入门到掉坑(三)：容器太多，操作好麻烦
https://mp.weixin.qq.com/s?__biz=MzI4Njc5NjM1NQ==&mid=2247490521&idx=1&sn=c5a1cdf7a574ea3f65fd497a6fde26be&chksm=ebd624f5dca1ade329637e7b93a145d1cc647e06dcc0949037bec468d2a454cd3a0784688e65&mpshare=1&scene=1&srcid=&sharer_sharetime=1574655285118&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=2459be73db906624b163fbe0e185474595ab24a36758f15ae0dead160e9db848b8329969966c7c73f110561fa6ba54f1bc940f337a7513baa1301b89953f53c135c5965279b2e798642d23b75fa34c22&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=iTkKo8IrxoXV%2FQ1p1Nm%2FqrCiHRjPcPjIl4dp2uE1rTwZVCMYw8j8RixABOBKh894
-->