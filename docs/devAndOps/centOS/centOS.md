

<!-- TOC -->

- [1. centOS](#1-centos)
    - [1.1. 下载](#11-下载)
    - [1.2. 安装](#12-安装)
        - [1.2.1. 安装包类型](#121-安装包类型)
        - [1.2.2. 软件安装目录](#122-软件安装目录)
        - [1.2.3. yum](#123-yum)
            - [1.2.3.1. 更换yum源](#1231-更换yum源)
            - [1.2.3.2. yum重装](#1232-yum重装)
    - [1.3. 环境变量](#13-环境变量)

<!-- /TOC -->

# 1. centOS

## 1.1. 下载  


* 上传
&emsp; scp G:\nexus-3.31.1-01-unix.tar.gz root@182.92.69.8:/usr/local  

## 1.2. 安装
### 1.2.1. 安装包类型  
<!-- 


linux安装包类型,Linux安装包类型
https://blog.csdn.net/weixin_39608988/article/details/116876796
rpm和yum命令安装软件的区别
https://blog.csdn.net/qq_47346664/article/details/120277985
-->

### 1.2.2. 软件安装目录  
<!-- 
https://www.csdn.net/tags/MtzaIg0sNDA5NTEtYmxvZwO0O0OO0O0O.html

https://blog.csdn.net/Acx77/article/details/121702959?spm=1001.2101.3001.6650.13&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7Edefault-13-121702959-blog-116792066.pc_relevant_aa&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7Edefault-13-121702959-blog-116792066.pc_relevant_aa&utm_relevant_index=18
-->

/usr：系统级的目录，可以理解为C:/Windows/，/usr/lib理解为C:/Windows/System32。  
/usr/local：用户级的程序目录，可以理解为C:/Progrem Files/。用户自己编译的软件默认会安装到这个目录下。  
/opt：用户级的程序目录，可以理解为D:/Software，opt有可选的意思，这里可以用于放置第三方大型软件（或游戏），当你不需要时，直接rm -rf掉即可。在硬盘容量不够时，也可将/opt单独挂载到其他磁盘上使用。  

源码放哪里？  
/usr/src：系统级的源码目录。  
/usr/local/src：用户级的源码目录  

### 1.2.3. yum

#### 1.2.3.1. 更换yum源
<!--


yum没有被启用的仓库
https://blog.csdn.net/Ennis_Tongji/article/details/119638020


Centos8更改国内源
https://blog.csdn.net/qq_41233709/article/details/122509655

centos8更换国内源及 Status code: 404 for https:// 问题
https://blog.csdn.net/zhuxiyulu/article/details/122974560


linux配置yum源的三种方法：
https://www.cnblogs.com/helong-123/p/16054732.html

https://help.aliyun.com/document_detail/405635.htm?spm=a2c4g.11186623.0.0.68c94eb7uP6Rki#task-2182261

-->

#### 1.2.3.2. yum重装  
<!-- 
centos8 解决yum重装
https://blog.csdn.net/JineD/article/details/111396902
-->


## 1.3. 环境变量  
<!-- 

https://blog.csdn.net/weixin_39270987/article/details/123181105?utm_medium=distribute.pc_relevant.none-task-blog-2~default~baidujs_baidulandingword~default-0-123181105-blog-118450790.pc_relevant_antiscanv4&spm=1001.2101.3001.4242.1&utm_relevant_index=3
-->

