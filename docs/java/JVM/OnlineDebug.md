


# 线上Debug  
<!-- 
IDEA 轻松实现线上debug 调试
https://blog.csdn.net/weixin_44922964/article/details/127383757

服务端线上debug流程
http://wiki.6noble.net/pages/viewpage.action?pageId=16155067
-->

<!-- 
一台单独的机器， 不对外开放  
1. 用最新的jar包替换6nobles-admin-hapi.jar  （从prod_server1手动同步过来即可）
   在prod_server1  运行   rsync /home/work/data/www/6nobles-admin-hapi/online/6nobles-admin-hapi.jar  work@prod_server3:/home/work/data/www/6nobles-admin-hapi/online/ 
2. kill掉原来进程
3. 运行 startdebug.sh即可启动   :   ./startdebug.sh 
4. 远程debug： 本地采用postman或者浏览器去访问要debug的接口，域名换成：10.210.10.97:8098     
IDE里面，远程debug设置 host  : 10.210.10.97    debug端口8000 即可


startdebug.sh内容

project_name="$1"
app_name="$2"
max_memory="$3"
provider_flag="$4"
nohup_file="$5"

nohup java -jar  -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 -server -Xms$max_memory -Xmx$max_memory -XX:+UseG1GC  -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=50 -XX:+DisableExplicitGC -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/work/data/www/$project_name/online/logs/oom-error_8000.log -Xloggc:/home/work/data/www/$project_name/online/logs/gc_log_8000.log /home/work/data/www/$project_name/online/$project_name.jar --Dappname=$app_name >> /home/work/data/www/$project_name/online/$nohup_file &



-->



