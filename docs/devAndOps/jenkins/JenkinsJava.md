
<!-- TOC -->

- [1. Jenkins构建Java项目](#1-jenkins构建java项目)
    - [1.1. 打包](#11-打包)
    - [1.2. 推送服务器](#12-推送服务器)

<!-- /TOC -->


# 1. Jenkins构建Java项目  

## 1.1. 打包
多模块项目打包：  
![image](http://182.92.69.8:8081/img/devops/jenkins/jenkins-1.png)    


## 1.2. 推送服务器  
<!-- 
Jenkins部署springboot项目至远程服务器
https://blog.csdn.net/HIM2014/article/details/126579634

解决SSH: Transferred 0 file(s)
https://www.jianshu.com/p/ef6a4022b7b5
-->


源服务器相关设置
Send files or execute commands over SSH  
![image](http://182.92.69.8:8081/img/devops/jenkins/jenkins-2.png)    
![image](http://182.92.69.8:8081/img/devops/jenkins/jenkins-3.png)    

目标服务器设置  
1. 系统设置  
![image](http://182.92.69.8:8081/img/devops/jenkins/jenkins-4.png)    
2. Send files or execute commands over SSH  
![image](http://182.92.69.8:8081/img/devops/jenkins/jenkins-5.png)    

