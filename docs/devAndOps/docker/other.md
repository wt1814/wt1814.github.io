


# 私有仓库

docker hub 是公开的，其他人也是可以下载，并不安全，因此还可以使用docker registry官方提供的私有仓库  

用法详解：  
https://yeasy.gitbooks.io/docker_practice/repository/registry.html  

```text
# 1.下载一个docker官方私有仓库镜像
    docker pull registry
# 2.运行一个docker私有容器仓库
docker run -d -p 5000:5000 -v /opt/data/registry:/var/lib/registry  registry
    -d 后台运行 
    -p  端口映射 宿主机的5000:容器内的5000
    -v  数据卷挂载  宿主机的 /opt/data/registry :/var/lib/registry 
    registry  镜像名
    /var/lib/registry  存放私有仓库位置
# Docker 默认不允许非 HTTPS 方式推送镜像。我们可以通过 Docker 的配置选项来取消这个限制
# 3.修改docker的配置文件，让他支持http方式，上传私有镜像
    vim /etc/docker/daemon.json 
    # 写入如下内容
    {
        "registry-mirrors": ["http://f1361db2.m.daocloud.io"],
        "insecure-registries":["192.168.11.37:5000"]
    }
# 4.修改docker的服务配置文件
    vim /lib/systemd/system/docker.service
# 找到[service]这一代码区域块，写入如下参数
    [Service]
    EnvironmentFile=-/etc/docker/daemon.json
# 5.重新加载docker服务
    systemctl daemon-reload
# 6.重启docker服务
    systemctl restart docker
    # 注意:重启docker服务，所有的容器都会挂掉

# 7.修改本地镜像的tag标记，往自己的私有仓库推送
    docker tag docker.io/peng104/hello-world-docker 192.168.11.37:5000/peng-hello
    # 浏览器访问http://192.168.119.10:5000/v2/_catalog查看仓库
# 8.下载私有仓库的镜像
    docker pull 192.168.11.37:5000/peng-hello
```
