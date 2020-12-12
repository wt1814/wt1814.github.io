
<!-- TOC -->

- [1. Docker工具](#1-docker工具)
    - [1.1. Docker环境管理UI](#11-docker环境管理ui)
    - [1.2. Docker容器的日志处理](#12-docker容器的日志处理)
        - [Docker引擎日志](#docker引擎日志)
        - [容器日志](#容器日志)
    - [1.3. Docker容器故障排查工具](#13-docker容器故障排查工具)
    - [1.4. Docker Compose](#14-docker-compose)

<!-- /TOC -->

# 1. Docker工具  
<!-- 
推荐 5 款超好用的开源 Docker 工具！
https://mp.weixin.qq.com/s/jxMuLZNsMacKDXoVmooS-A

-->

## 1.1. Docker环境管理UI   
&emsp; docker图形页面管理工具常用的有三种：DockerUI、Portainer、Shipyard。  
&emsp; DockerUI 是 Portainer 的前身，这三个工具通过docker api来获取管理的资源信息。平时常常对着shell对着这些命令行客户端，审美会很疲劳，如果有漂亮的图形化界面可以直观查看docker资源信息，也是非常方便的。这三种图形化管理工具以Portainer最为受欢迎。 

<!--
～～
https://mp.weixin.qq.com/s?__biz=MzI5ODQ2MzI3NQ==&mid=2247488015&idx=1&sn=f7640dc2aab0d95245ba8b521adc5eb3&chksm=eca42b4bdbd3a25d75ac2c30fdaa25a38a0db560b0e85f0794c56a07d537f75163dd42788c50&mpshare=1&scene=1&srcid=&sharer_sharetime=1569341646799&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=2a4ff15fdd846346cca6724726c97c2197535a0af73ab946d789ddcb5bca832c59fc3b05e664926e093344c971b6894e335e858ff18a4493851dba74507020c7655f3aa9bb719e55759c5086086dea2f&ascene=1&uin=MTE1MTYxNzY2MQ==&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=+eyejsSYMk60ZUY/pK4YBKveSPIi8GUzrQvyveBksIipCvi8KhPkraBA4Eyx2jY/

https://mp.weixin.qq.com/s/Z_1xX1q5CDhD46b4KeK0bg

Docker 图形化页面管理工具使用 
https://mp.weixin.qq.com/s/wy99yOT3S6g0QLUIDf4DSQ
-->
## 1.2. Docker容器的日志处理  

<!-- 
docker容器日志管理
https://www.cnblogs.com/caibao666/p/12084928.html
Docker容器的日志处理
https://mp.weixin.qq.com/s/1CT1K9UPWN8k2G_JkK3EwA、

-->
docker容器日志分为两类：docker引擎日志（Docker本身运行的日志）和容器日志（各个容器内产生的日志）。  

### Docker引擎日志  

### 容器日志  


## 1.3. Docker容器故障排查工具  
&emsp; Docker-debug  
<!-- 
Docker 容器故障排查工具
https://mp.weixin.qq.com/s/8lOTyNogYRFsqLpE45217w
-->


## 1.4. Docker Compose  
&emsp; 跨越多台机器，docker compose就无能为例了：比如如何进行跨越多台机器的增删改查，比如不同机器之间的container如何通信等等。于是开始使用K8s来进行容器的管理维护了。  
<!-- 
Docker从入门到掉坑(三)：容器太多，操作好麻烦
https://mp.weixin.qq.com/s?__biz=MzI4Njc5NjM1NQ==&mid=2247490521&idx=1&sn=c5a1cdf7a574ea3f65fd497a6fde26be&chksm=ebd624f5dca1ade329637e7b93a145d1cc647e06dcc0949037bec468d2a454cd3a0784688e65&mpshare=1&scene=1&srcid=&sharer_sharetime=1574655285118&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=2459be73db906624b163fbe0e185474595ab24a36758f15ae0dead160e9db848b8329969966c7c73f110561fa6ba54f1bc940f337a7513baa1301b89953f53c135c5965279b2e798642d23b75fa34c22&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=iTkKo8IrxoXV%2FQ1p1Nm%2FqrCiHRjPcPjIl4dp2uE1rTwZVCMYw8j8RixABOBKh894
-->

