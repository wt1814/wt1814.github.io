
<!-- TOC -->

- [1. Jenkins构建Java项目](#1-jenkins构建java项目)
    - [1.1. Jenkins通过jar包启动项目](#11-jenkins通过jar包启动项目)
        - [1.1.1. 打包](#111-打包)
        - [1.1.2. 推送服务器](#112-推送服务器)
        - [1.1.3. 启动脚本restart.sh](#113-启动脚本restartsh)
    - [1.2. jenkins通过docker启动项目](#12-jenkins通过docker启动项目)
        - [1.2.1. 推送jar和Dockerfile到远程服务器](#121-推送jar和dockerfile到远程服务器)
        - [1.2.2. 上传镜像方式](#122-上传镜像方式)
    - [1.3. jenkins通过k8s构建项目](#13-jenkins通过k8s构建项目)

<!-- /TOC -->


# 1. Jenkins构建Java项目  
&emsp; 一句话总结步骤：1.配置git源码信息，将源码下载到jenkins的workspace目录下；2.在jenkins的workspace目录下执行编译（mvn clean package）；3.推送代码到远程服务器；4.执行远程服务器的启动脚本。     



## 1.1. Jenkins通过jar包启动项目
### 1.1.1. 打包
多模块项目打包：  
![image](http://182.92.69.8:8081/img/devops/jenkins/jenkins-1.png)    


### 1.1.2. 推送服务器  
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


### 1.1.3. 启动脚本restart.sh

```text
#!/bin/bash -ilex

# 启动命令 sh restartjar.sh jenkinstest jenkinstest-1 0.1g nohup.out


# 读取参数
project_name="$1"
app_name="$2"
max_memory="$3"
nohup_file="$4"

echo "System Information:"
echo "****************************"
echo "project_name=$project_name"
echo "app_name=$app_name"
echo "max_memory=$max_memory"
echo "nohup_file=$nohup_file"
echo "****************************"

# 搜索旧的进程号
application_pid=`ps -ef | grep $app_name | grep -v 'sudo ' | grep -v grep | grep -v restartjar.sh | awk '{print $2}'`
# 如果旧进程未关闭，关闭旧进程
if [ -n "$application_pid" ]; then
	sudo kill $application_pid
	for i in {1..20}
	do
		sleep 2
		echo "check kill result count : $i"
		application_pid=`ps -ef | grep $app_name | grep -v 'sudo ' | grep -v grep | grep -v restartjar.sh | awk '{print $2}'`
		if [ -z "$application_pid" ]
		then
				break
		fi
	done
fi


# 创建目录
mkdir -p /usr/work/$project_name/online/logs
mkdir -p /usr/work/$project_name/history
touch /usr/work/$project_name/online/$nohup_file

ps -ef | grep $app_name | grep -v grep | grep -v restartjar.sh | awk '{print $2}'  | sed -e "s/^/sudo kill -9 /g" | sh -  

sudo cp -rf /usr/work/$project_name/online/$project_name.jar  /usr/work/$project_name/history/$project_name.jar_$(date +%Y%m%d_%H%M)

cp -rf /usr/work/$project_name/$project_name.jar  /usr/work/$project_name/online/$project_name.jar

sleep 2

cat /dev/null | sudo tee /usr/work/$project_name/online/$nohup_file

# 多参数启动
# sudo nohup java -jar -Duser.timezone=GMT+08 -server -Xms$max_memory -Xmx$max_memory -XX:+UseG1GC  -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:G1MaxNewSizePercent=30 -XX:+DisableExplicitGC -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/usr/work/$project_name/online/logs/oom-error_8000.log -Xloggc:/usr/work/$project_name/online/logs/gc_log_8000.log /usr/work/$project_name/online/$project_name.jar --Dappname=$app_name  >> /usr/work/$project_name/online/$nohup_file &

sudo nohup java -jar -Duser.timezone=GMT+08 /usr/work/$project_name/online/$project_name.jar --Dappname=$app_name  >> /usr/work/$project_name/online/$nohup_file &

echo "System Stop"
```

## 1.2. jenkins通过docker启动项目  
<!-- 
docker in docker
https://juejin.cn/post/6855559520950452238

-->

### 1.2.1. 推送jar和Dockerfile到远程服务器  
<!-- 
Jenkins+Docker 一键自动化部署 SpringBoot 项目
https://blog.csdn.net/weixin_36380516/article/details/126326838
-->

将workspace下的jar和Dockerfile一起推送到远程同一目录下，然后执行shell脚本。  

推送Dockerfile到目标服务器  
![image](http://182.92.69.8:8081/img/devops/jenkins/jenkins-6.png)    

shell脚本  

```text
cd /usr/work/jenkinstest
# 删除容器
docker stop `docker ps -a| grep jenkinstest | awk '{print $1}' `
docker rm   `docker ps -a| grep jenkinstest | awk '{print $1}' `
# 删除旧镜像
docker rmi jenkinstest || true
# 构建新镜像
docker build -t jenkinstest .
# 启动容器
docker run -d -p 8090:8089 jenkinstest
```


### 1.2.2. 上传镜像方式  
<!-- 

在jenkins本机就把镜像构建好，上传镜像到harbor仓库后再去通知目标服务器去自动拉取镜像部署
https://blog.csdn.net/qq_42883074/article/details/126009573
-->


## 1.3. jenkins通过k8s构建项目  
<!-- 

采用 jenkins pipeline 实现自动构建并部署至 k8s
https://segmentfault.com/a/1190000039251002
-->

