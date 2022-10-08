
<!-- TOC -->

- [1. Linux启动脚本](#1-linux启动脚本)
    - [1.1. 第一种：配置/etc/rc.local](#11-第一种配置etcrclocal)
        - [1.1.1. 使用](#111-使用)
        - [1.1.2. rc.local配置的启动项未生效原因总结](#112-rclocal配置的启动项未生效原因总结)
    - [1.2. /etc/init.d目录下添加自启动脚本](#12-etcinitd目录下添加自启动脚本)
    - [1.3. ***第二种：添加system启动文件](#13-第二种添加system启动文件)

<!-- /TOC -->

# 1. Linux启动脚本  

<!--
三种方式  
Linux系统下如何设置开机自动运行脚本？
https://baijiahao.baidu.com/s?id=1722174560616569543&wfr=spider&for=pc

linux开机启动脚本
https://blog.csdn.net/User_bie/article/details/120226581


-->



## 1.1. 第一种：配置/etc/rc.local  
### 1.1.1. 使用
<!-- 
https://www.yisu.com/zixun/481840.html
https://blog.csdn.net/weixin_50518271/article/details/122712171
-->

### 1.1.2. rc.local配置的启动项未生效原因总结  
<!-- 

https://www.cnblogs.com/baihh/p/16435572.html
-->



## 1.2. /etc/init.d目录下添加自启动脚本  
<!-- 

https://m.php.cn/article/480523.html
-->




## 1.3. ***第二种：添加system启动文件  
<!-- 
https://blog.51cto.com/u_10473224/4286697
-->
&emsp; 过去Linux采用的是init.d的服务启动管理方式，新版的Linux采用systemd服务启动管理方式。  

&emsp; 设置开机自启动  

```text
systemctl enable nfs-server.service
```

