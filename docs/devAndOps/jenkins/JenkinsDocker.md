

# Jenkins和Docker

<!-- 
**** docker安装jenkins
https://blog.csdn.net/aiwangtingyun/article/details/123523669
访问时出现无法访问，点击叉号  

docker run -d \
    -p 8888:8080 \
    -p 50000:50000 \
    -v /usr/work/dockerMount/jenkins:/var/jenkins_home \
    -v /etc/localtime:/etc/localtime \
    --restart=always \
    --name=jenkins \
    jenkins/jenkins


Jenkins +Docker+Git 实现自动部署
https://www.cnblogs.com/seanRay/p/15126859.html
Jenkins+Docker 一键自动化部署 SpringBoot 项目 
https://mp.weixin.qq.com/s/C7o0SDNW-rajE0FywGGbTQ

云服务器中安装docker+jenkins部署接口自动化测试(java)
https://blog.csdn.net/m0_50026910/article/details/124114199

-->



<!-- 
***  
docker使用dockerFile自定义Jenkins
使用docker来启动jenkins才发现里面有一大堆坑，每次都要安装maven、jdk太麻烦。于是写了个dockerfile，一键生成装有maven、jdk1.8、jenkins的镜像。  
https://blog.csdn.net/qq_35031494/article/details/125426380

-->
