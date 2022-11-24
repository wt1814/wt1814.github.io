

<!-- TOC -->

- [1. docker安装](#1-docker安装)
    - [1.1. Docker安装及使用阿里云Doker镜像加速](#11-docker安装及使用阿里云doker镜像加速)
        - [1.1.1. 安装](#111-安装)
            - [1.1.1.1. 问题](#1111-问题)
        - [1.1.2. 启动及停止](#112-启动及停止)
    - [1.2. 镜像加速](#12-镜像加速)
    - [1.3. 私有仓库搭建](#13-私有仓库搭建)

<!-- /TOC -->


# 1. docker安装  
<!-- 

Docker 容器启动报错的解决方法
https://blog.csdn.net/gybshen/article/details/119377092
https://blog.csdn.net/kfgauss/article/details/116744314
-->


&emsp; docker官方文档：https://docs.docker.com/get-started/overview/  


## 1.1. Docker安装及使用阿里云Doker镜像加速  

### 1.1.1. 安装
<!-- 
https://blog.csdn.net/m0_67390788/article/details/123830312
-->


#### 1.1.1.1. 问题  
<!-- 
Unit file docker.service does not exist
https://cloud.tencent.com/developer/article/1897235

centos8使用，缺少类库libseccomp-devel  
https://blog.csdn.net/kfgauss/article/details/116744314

    yum install libseccomp-devel

-->



### 1.1.2. 启动及停止  
1. 启动docker服务

    systemctl start docker

2. 查看Docker状态  
    1. 查看docker是否启动了,是否是运行状态

        systemctl status docker

3. 设置Docker开机自启

    systemctl enable docker

4. 禁用Docker开机自启

    systemctl disable docker

5. 重新启动Docker服务

    systemctl restart docker

6. 查看Docker信息

    docker info

7. 查看docker info中具体key的信息,例如:

    docker info | grep 'Docker Root Dir:'

8. 停止docker服务

    systemctl stop docker


## 1.2. 镜像加速
<!-- 

Docker配置阿里云镜像仓库
https://mp.weixin.qq.com/s/qp3BX2oq5dULOEBFt5XTAQ

镜像加速器地址
https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors
-->

&emsp; 由于国内网络问题，需要配置加速器来加速。修改配置文件 /etc/docker/daemon.json  
&emsp; 下面命令直接生成文件 daemon.json，直接在命令行执行即可  

```text
cat <<EOF > /etc/docker/daemon.json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "http://hub-mirror.c.163.com"
  ],
  "max-concurrent-downloads": 10,
  "log-driver": "json-file",
  "log-level": "warn",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
    },
  "data-root": "/var/lib/docker"
}
EOF
```


## 1.3. 私有仓库搭建  
......
<!-- 
 Dockerfile构建镜像、registry私服搭建和阿里云的私服仓库构建
https://mp.weixin.qq.com/s/3Lz9CcgIZXjwtwkPdkkqsA

-->

