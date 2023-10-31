



<!-- TOC -->

- [1. Service详解](#1-service详解)
    - [1.1. 无状态集群Deployment和有状态集群Operator](#11-无状态集群deployment和有状态集群operator)
    - [1.2. Service基本用法](#12-service基本用法)
    - [1.3. 网络](#13-网络)
    - [1.4. DNS服务搭建指南](#14-dns服务搭建指南)
    - [1.5. 自定义DNS和上游DNS服务器](#15-自定义dns和上游dns服务器)
    - [1.6. Ingress：HTTP 7层路由机制](#16-ingresshttp-7层路由机制)

<!-- /TOC -->



# 1. Service详解  
<!-- 
https://blog.csdn.net/PpikachuP/article/details/89674578
-->
&emsp; **<font color = "clime">Service是Kubernetes最核心的概念，通过创建Service，可以为一组具有相同功能的容器应用提供一个统一的入口地址，并且将请求负载分发到后端的各个容器应用上。</font>** 本节对Service的使用进行详细说明，包括Service的负载均衡、外网访问、DNS服务的搭建、Ingress7层路由机制等。  

## 1.1. 无状态集群Deployment和有状态集群Operator  
  

## 1.2. Service基本用法  
&emsp; 在某些环境中，应用系统需要将一个外部数据库作为后端服务进行连接，或将另一个集群或Namespace中的服务作为服务的后端，这时可以通过创建一个无Label Selector的Service来实现。  
 

## 1.3. 网络


## 1.4. DNS服务搭建指南  
&emsp; 作为服务发现机制的基本功能，在集群内需要能够通过服务名对服务进行访问，这就需要一个集群范围的DNS服务来完成服务名到ClusterIP的解析。  

## 1.5. 自定义DNS和上游DNS服务器  
&emsp; 在实际环境中，很多用户都有自己的私有域名区域，并且希望能够集成到Kubernetes DNS的命名空间中，例如混合云用户可能希望能在集群内解析其内部的".corp"域名；用户也可能己存在一个未被Kubernetes管理的服务发现系统(例如Consul)来完成域名解析。从Kubernetes vl.6版本开始，用户可以在Kubernetes集群内配置私有DNS区域(通常称为存根域Stub Domain)和外部的上游域名服务了。  

## 1.6. Ingress：HTTP 7层路由机制  
&emsp; Service的表现形式为IP:Port，即工作在TCP/IP层。而对于基于HTTP的服务来说，不同的URL地址经常对应到不同的后端服务或者虚拟服务器(Virtual Host)，这些应用层的转发机制仅通过Kubernetes的Service机制是无法实现的。 **<font color = "clime">从Kubernetes vl.l版本开始新增Ingress资源对象，用于将不同URL的访问请求转发到后端不同的Service，以实现HTTP层的业务路由机制。Kubernetes使用一个Ingress策略定义和一个具体的Ingress Controller，两者结合并实现了一个完整的Ingress负载均衡器。</font>**  
&emsp; **<font color = "clime">使用Ingress进行负载分发时，Ingress Controller将基于Ingress规则将客户端请求直接转发到Service对应的后端Endpoint(即Pod)上，这样会跳过kube-proxy的转发功能，kube-proxy不再起作用。</font>** 如果Ingress Controller提供的是对外服务，则实际上实现的是边缘路由器的功能。  