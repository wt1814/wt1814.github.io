
<!-- TOC -->

- [1. Jenkins构建Java项目](#1-jenkins构建java项目)
    - [1.1. Jenkins通过jar包启动项目](#11-jenkins通过jar包启动项目)
        - [1.1.1. 打包](#111-打包)
        - [1.1.2. 推送服务器](#112-推送服务器)
        - [1.1.3. 启动脚本restart.sh](#113-启动脚本restartsh)
    - [1.2. jenkins通过docker启动项目](#12-jenkins通过docker启动项目)

<!-- /TOC -->


# 1. Jenkins构建Java项目  

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
将workspace下的jar和docker一起推送到远程同一目录下，然后执行shell脚本。  


cd /root/.jenkins/workspace/jenkinstestdocker/jenkinstest
docker stop jenkinstest || true
docker rm jenkinstest || true
docker rmi jenkinstest || true
docker build -t jenkinstest .
docker run -d -p 8089:8089 jenkinstest

