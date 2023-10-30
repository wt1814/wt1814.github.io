
<!-- TOC -->

- [1. Linux和DevOps](#1-linux和devops)
    - [1.1. Linux](#11-linux)
    - [1.2. Devops](#12-devops)
        - [1.2.1. CI/CD](#121-cicd)
        - [1.2.2. DevOps](#122-devops)
        - [1.2.3. 从上往下学Docker](#123-从上往下学docker)
            - [1.2.3.1. Docker使用教程](#1231-docker使用教程)
            - [1.2.3.2. 镜像详解](#1232-镜像详解)
            - [1.2.3.3. 容器详解](#1233-容器详解)
        - [1.2.4. Kubernetes](#124-kubernetes)
            - [1.2.4.1. k8s架构](#1241-k8s架构)

<!-- /TOC -->


# 1. Linux和DevOps

## 1.1. Linux  



## 1.2. Devops
### 1.2.1. CI/CD
&emsp; `CI/CD是两个独立过程的组合：持续集成和持续部署。`  
1. Continuous Integration（持续集成）  
&emsp; 持续集成（CI）是构建软件和完成初始测试的过程。  
2. Continuous Delivery（持续交付）  
3. Continuous Deployment（持续部署）  
&emsp; 持续部署（CD）是将代码与基础设施相结合的过程，确保完成所有测试并遵循策略，然后将代码部署到预期环境中。  

### 1.2.2. DevOps
1. DevOps框架  
&emsp; 以下是一个DevOps框架。这个框架只指出那些被认可的概念和它们在某种程度上的关系。
![image](http://182.92.69.8:8081/img/devops/devops/devops-8.png)  
&emsp; **<font color = "clime">敏捷开发指的是在 DevOps 中采用敏捷思想进行软件开发，敏捷宣言无疑是很重要的一项。有多种敏捷方法可以采用，比如Scrum、看板和极限编程。</font>**  
&emsp; **<font color = "clime">持续集成提供了让多个程序员可以同时运行应用程序的最佳实践，可以频繁合并源代码、验证代码（静态测试用例）、编译和测试代码（动态测试用例）。</font>**  
&emsp; **<font color = "clime">持续交忖关注从开发、测试、验收到生产环境的高频生产能力。基于高度的自动化，极端的发布上线时间可以达到分钟级。</font>**  
2. DevOps流程  
&emsp; 下图显示了一个DevOps流程。它不是DevOps流程的正式定义，而是表述了在大多数组织机构中，为了实现一个服务而会被循环执行的合乎逻辑顺序的一系列阶段。  
&emsp; 深色部分表示开发流程，浅色部分表示运维流程。这两个流程构成了DevOps方法的核心。  
![image](http://182.92.69.8:8081/img/devops/devops/devops-1.png)  
3. 工具集  
&emsp; **<font color = "clime">DevOps一般包括版本控制&协作开发工具、自动化构建和测试工具、持续集成&交付工具、部署工具、维护工具、监控，警告&分析工具等。</font>**  
![image](http://182.92.69.8:8081/img/devops/devops/devops-3.png)  

### 1.2.3. 从上往下学Docker

#### 1.2.3.1. Docker使用教程
1. **<font color = "clime">镜像操作常用命令：pull(获取)、images(查看本地镜像)、inspect(查看镜像详细信息)、rmi(删除镜像)、commit(构建镜像)。</font>**  
2. **<font color = "clime">容器操作常用命令：run(创建并启动)、start(启动已有)、stop、exec(进入运行的容器)。</font>**  
3. **<font color = "clime">Dockerfile中包含：</font>** （# 为 Dockerfile中的注释）  
    * 基础镜像(FROM)    
    * 镜像元信息   
    * **<font color = "clime">镜像操作指令</font>** （RUN、COPY、ADD、EXPOSE、WORKDIR、ONBUILD、USER、VOLUME等）    
        * RUN命令：**run是在docker build构建镜像时，会执行的命令，** 比如安装一些软件、配置一些基础环境。  
    * **<font color = "clime">容器启动时执行指令</font>** （CMD、ENTRYPOINT）  
        * CMD命令： **cmd是在docker run启动容器时，会执行的命令，为启动的容器指定默认要运行的程序。** CMD指令指定的程序可被docker run命令行参数中指定要运行的程序所覆盖。 **<font color = "clime">注意：如果Dockerfile中如果存在多个CMD指令，仅最后一个生效。</font>**    
    ![image](http://182.92.69.8:8081/img/devops/docker/docker-9.png)  


#### 1.2.3.2. 镜像详解
1. Docker中镜像是分层的，最顶层是读写层（镜像与容器的区别），其底部依赖于Linux的UnionFS文件系统。  
2. **<font color = "red">利用联合文件系统UnionFS写时复制的特点，在启动一个容器时，Docker引擎实际上只是增加了一个可写层和构造了一个Linux容器。</font>**  

#### 1.2.3.3. 容器详解
1. 单个宿主机的多个容器是隔离的，其依赖于Linux的Namespaces、CGroups。  
2. 隔离的容器需要通信、文件共享（数据持久化）。  

### 1.2.4. Kubernetes
#### 1.2.4.1. k8s架构
1. 1). 一个容器或多个容器可以同属于一个Pod之中。 2). Pod是由Pod控制器进行管理控制，其代表性的Pod控制器有Deployment、StatefulSet等。 3). Pod组成的应用是通过Service或Ingress提供外部访问。  
2. **<font color = "red">每一个Kubernetes集群都由一组Master节点和一系列的Worker节点组成。</font>**  
    1. **<font color = "clime">Master的组件包括：API Server、controller-manager、scheduler和etcd等几个组件。</font>**  
        * **<font color = "red">API Server：K8S对外的唯一接口，提供HTTP/HTTPS RESTful API，即kubernetes API。</font>**  
        * **<font color = "red">controller-manager：负责管理集群各种资源，保证资源处于预期的状态。</font>** 
        * **<font color = "red">scheduler：资源调度，负责决定将Pod放到哪个Node上运行。</font>** 
    2. **<font color = "clime">Node节点主要由kubelet、kube-proxy、docker引擎等组件组成。</font>**  


