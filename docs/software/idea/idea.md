
<!-- TOC -->

- [1. Idea](#1-idea)
    - [1.1. 安装](#11-安装)
    - [1.2. 激活](#12-激活)
        - [1.2.1. ★★★License Server](#121-★★★license-server)
    - [1.3. Idea更新本地仓库maven构建索引](#13-idea更新本地仓库maven构建索引)
    - [1.4. springboot在idea的RunDashboard如何显示](#14-springboot在idea的rundashboard如何显示)
    - [1.5. iml文件](#15-iml文件)
    - [1.6. 文件夹类型](#16-文件夹类型)
    - [1.7. 插件](#17-插件)
        - [1.7.1. 代码质量检测插件](#171-代码质量检测插件)
        - [1.7.2. 查看字节码插件](#172-查看字节码插件)
        - [IDEA的Redis插件](#idea的redis插件)
    - [集成JIRA、UML类图插件、SSH、FTP、Database管理...](#集成jirauml类图插件sshftpdatabase管理)
    - [1.8. 快捷键](#18-快捷键)
        - [1.8.1. 批量修改变量名方法名](#181-批量修改变量名方法名)
    - [***高级调试](#高级调试)

<!-- /TOC -->


# 1. Idea
<!-- 
Idea在debug模式下修改的java后，Recopile（ctrl+shift+f9）热部署失效
https://blog.csdn.net/weixin_42170236/article/details/121637717
-->


## 1.1. 安装
<!-- 

Mac idea 打不开
https://blog.csdn.net/sanmi8276/article/details/108522676
-->


## 1.2. 激活
<!-- 
IntelliJ IDEA 2020.2.3永久激活教程
https://www.yuque.com/docs/share/23fc9e41-ad96-4343-aced-a35419117d89


-->

### 1.2.1. ★★★License Server

<!-- 
*** https://www.cnblogs.com/xiang--liu/p/13883523.html
https://www.cnblogs.com/jie-fang/p/10214170.html

https://blog.csdn.net/sanmi8276/article/details/108522676

https://www.jianshu.com/p/46ac89620c0a

-->


## 1.3. Idea更新本地仓库maven构建索引
<!-- 

Idea更新本地仓库maven构建索引
https://blog.csdn.net/weixin_42325659/article/details/105649218
-->

## 1.4. springboot在idea的RunDashboard如何显示
<!-- 
https://jingyan.baidu.com/article/ce4366495a1df73773afd3d3.html
-->

## 1.5. iml文件  

&emsp; IDEA中的.iml文件是项目标识文件，缺少了这个文件，IDEA就无法识别项目。跟Eclipse的.project文件性质是一样的。并且这些文件不同的设备上的内容也会有差异，所以在管理项目的时候，.project和.iml文件都需要忽略掉。  

&emsp; 在缺少.iml文件项目下运行mvn idea:module，完成后将自动生成.iml文件。  


## 1.6. 文件夹类型
<!-- 

https://blog.csdn.net/a772304419/article/details/79680775
-->

## 1.7. 插件  
<!-- 
 Stream Trace 
https://mp.weixin.qq.com/s/-IZ9jDMXUlL4kFt-OZ-qQw
https://mp.weixin.qq.com/s/jHSTmVR8NJtpaAEAxZayqw

-->

### 1.7.1. 代码质量检测插件
<!-- 
https://mp.weixin.qq.com/s/UwS0oGaHR5yV5PIAHx6QZg
-->

### 1.7.2. 查看字节码插件
<!-- 

IDEA查看字节码插件
https://blog.csdn.net/qq_38826019/article/details/119273641
--> 


### IDEA的Redis插件  
<!-- 

使用IDEA的Redis插件连接Redis服务器
https://blog.csdn.net/m0_47503416/article/details/121397584

-->


## 集成JIRA、UML类图插件、SSH、FTP、Database管理... 
<!-- 

https://mp.weixin.qq.com/s/UcNy1hybHz5u3_fOn3FhHw
-->


## 1.8. 快捷键  
<!-- 

-->

### 1.8.1. 批量修改变量名方法名  
<!-- 

https://www.pianshen.com/article/83811617038/
-->

&emsp; 选中要修改的然后按shift+F6 出现了红框后修改好点击回车就行了  



## ***高级调试   
<!--
 这几个 IntelliJ IDEA 高级调试技巧，用了都说爽！ 
https://mp.weixin.qq.com/s?__biz=Mzg2MjEwMjI1Mg==&mid=2247491131&idx=3&sn=d072a649696b5b07411642067d51367d&chksm=ce0da9b8f97a20ae787ac263de162bc8c1d1843902341e1f5c4a2db0ece02276b85c5b76cfc6&mpshare=1&scene=1&srcid=&sharer_sharetime=1577187607880&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=6f32c57123495b976074ebfd1a889da106f7c9e51dd9e4429295af6cf4ef294ca7377d41701d83de6900e2ad7a0d82fe20da55d14f325c1cd2296f52a3b45a7ac0ab989b3ae24465df8a53af9481378a&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=Af6pZ1ErrZndhXyF%2FrpKbKA%3D&pass_ticket=fjo0TPQ4TftdXiH325uINjkxmTSYWN5xsY7SY8CPXJ8L70Z%2B9nqwLCPhjc61tfer
-->


