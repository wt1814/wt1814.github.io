

<!-- TOC -->

- [1. kubectl命令行工具](#1-kubectl命令行工具)
    - [1.1. ★★★kubectl用法概述](#11-★★★kubectl用法概述)
    - [1.2. ~~kubectl常用命令~~](#12-kubectl常用命令)

<!-- /TOC -->



# 1. kubectl命令行工具

&emsp; <font color = "clime">kubectl作为客户端CLI工具，可以让用户通过命令行的方式对Kubernetes集群进行操作。</font>  

## 1.1. ★★★kubectl用法概述  
&emsp; **<font color = "clime">kubectl命令行的语法如下：</font>**  

```text
$ kubectl [command] [TYPE] [NAME] [flags]
```
&emsp; 其中，command、TYPE、NAME、flags的含义如下。  
1. **<font color = "clime">command：子命令，用于操作Kubemetes集群资源对象的命令，例如create、delete、describe、get、exec等。</font>**  
2. **<font color = "red">TYPE：资源对象的类型，区分大小写，能以单数形式、复数形式或者简写形式表示。</font>** 例如以下3种TYPE是等价的。  

    ```text
    $ kubectl get pod podl  
    $ kubectl get pods podl  
    $ kubectl get po podl
    ```
3. **<font color = "red">NAME：资源对象的名称，区分大小写。</font>** 如果不指定名称，则系统将返回属于TYPE的全部对象的列表，例如$kubectl get pods将返回所有Pod的列表。
4. flags：kubectl子命令的可选参数，例如使用"-s”指定apiserver的URL地址而不用默认值。  

## 1.2. ~~kubectl常用命令~~
&emsp; [kubectl命令表](http://docs.kubernetes.org.cn/683.html)  
1. 创建资源对象  
    &emsp; 根据yaml配置文件一次性创建service和rc：  

    ```text
    $ kubectl create -f my-service.yaml -f my-rc.yaml 
    ``` 
    &emsp; 根据<directory>目录下所有.yaml、.yml、.json文件的定义进行创建操作: 
    ```text 
    $ kubectl create -f <directory>  
    ```
2. 查看资源对象  
    &emsp; 查看所有Pod列表：  
    ```text
    $ kubectl get pods 
    ``` 
    &emsp; 查看rc和service列表：  
    ```text
    $ kubectl get rc,service 
    ``` 
3. 描述资源对象  
    &emsp; 显示Node的详细信息：  

    ```text
    $ kubectl describe nodes <node-name>  
    ```
    &emsp; 显示Pod的详细信息：  

    ```text
    $ kubectl describe pods <pod-name> 
    ``` 
    &emsp; 显示由RC管理的Pod的信息：  

    ```text
    $ kubectl describe pods <rc-name> 
    ``` 
4. 删除资源对象  
    &emsp; 基于pod.yaml定义的名称删除Pod：  

    ```text
    $ kubectl delete -f pod.yaml  
    ```
    &emsp; 删除所有包含某个label的Pod和service：  

    ```text
    $ kubectl delete pods,services -1 name=<label-name>  
    ```
    &emsp; 删除所有Pod:  

    ```text
    $ kubectl delete pods --all  
    ```
5. 执行容器的命令  
    &emsp; 执行Pod的date命令，默认使用Pod中的第1个容器执行：  

    ```text
    $ kubectl exec <pod-name> date 
    ``` 
    &emsp; 指定Pod中某个容器执行date命令：  

    ```text
    $ kubectl exec <pod-name> -c <container-name> date 
    ``` 
    &emsp; 通过bash获得Pod中某个容器的TTY，相当于登录容器：  

    ```text
    $ kubectl exec -ti <pod-name> -c <container-name> /bin/bash  
    ```
6. 查看容器的日志  
    &emsp; 查看容器输出到stdout的日志：  

    ```text
    $ kubectl logs <pod-name>
    ```  
    &emsp; 跟踪查看容器的日志，相当于tail-f命令的结果：  

    ```text
    $ kubectl logs -f <pod-name> -c <container-name> 
    ``` 
