
<!-- TOC -->

- [1. Zookeeper使用](#1-zookeeper使用)
    - [1.1. Zookeeper的API](#11-zookeeper的api)
        - [1.1.1. 原生API](#111-原生api)
        - [1.1.2. ZkClient](#112-zkclient)
        - [1.1.3. Curator](#113-curator)
    - [1.2. Zookeeper监控](#12-zookeeper监控)
        - [1.2.1. Zookeeper的四字命令](#121-zookeeper的四字命令)
        - [1.2.2. IDEA zookeeper插件的使用](#122-idea-zookeeper插件的使用)
        - [1.2.3. JMX](#123-jmx)
    - [1.3. 部署](#13-部署)

<!-- /TOC -->

# 1. Zookeeper使用  
## 1.1. Zookeeper的API  
### 1.1.1. 原生API  
......  

### 1.1.2. ZkClient  
&emsp; Zkclient是由Datameer的工程师开发的开源客户端，对Zookeeper的原生API进行了包装，实现了超时重连，Watcher反复注册等功能。  

### 1.1.3. Curator  
&emsp; Curator是Netflix公司开源的一个Zookeeper客户端，与Zookeeper提供的原生客户端相比，Curator的抽象层次更高，简化了Zookeeper客户端的开发量。  

## 1.2. Zookeeper监控  
<!-- 
taokeeper
-->
### 1.2.1. Zookeeper的四字命令  
.......  

### 1.2.2. IDEA zookeeper插件的使用  
......  

### 1.2.3. JMX  
......


## 1.3. 部署
&emsp; ZooKeeper 的三种部署方式：  

* 单机模式，即部署在单台机器上的一个 ZK 服务，适用于学习、了解 ZK 基础功能；
* 伪分布模式，即部署在一台机器上的多个（原则上大于3个）ZK 服务，伪集群，适用于学习、开发和测试；
* 全分布式模式（复制模式），即在多台机器上部署服务，真正的集群模式，生产环境中使用。


