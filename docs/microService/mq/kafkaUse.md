
<!-- TOC -->

- [1. kafka集群管理](#1-kafka集群管理)
    - [1.1. 集群管理](#11-集群管理)
    - [1.2. topic管理](#12-topic管理)
    - [1.3. topic动态配置管理](#13-topic动态配置管理)
    - [1.4. consumber相关管理](#14-consumber相关管理)
    - [1.5. topic分区管理](#15-topic分区管理)
    - [1.6. kafka常见脚本工具](#16-kafka常见脚本工具)
    - [1.7. MirrorMaker，镜像操作](#17-mirrormaker镜像操作)
    - [1.8. Kafka安全](#18-kafka安全)

<!-- /TOC -->

# 1. kafka集群管理  
<!-- 
~~
https://blog.csdn.net/BeiisBei/article/details/104264144
-->
&emsp; 参考《Kafka实战》第7章  

## 1.1. 集群管理

## 1.2. topic管理

## 1.3. topic动态配置管理

## 1.4. consumber相关管理

## 1.5. topic分区管理

## 1.6. kafka常见脚本工具  
&emsp; Kafka 的命令行工具在 Kafka 包的/bin目录下，主要包括服务和集群管理脚本，配置脚本，信息查看脚本，Topic 脚本，客户端脚本等。

* kafka-configs.sh：配置管理脚本
* kafka-console-consumer.sh：kafka 消费者控制台
* kafka-console-producer.sh：kafka 生产者控制台
* kafka-consumer-groups.sh：kafka 消费者组相关信息
* kafka-delete-records.sh：删除低水位的日志文件
* kafka-log-dirs.sh：kafka 消息日志目录信息
* kafka-mirror-maker.sh：不同数据中心 kafka 集群复制工具
* kafka-preferred-replica-election.sh：触发 preferred replica 选举
* kafka-producer-perf-test.sh：kafka 生产者性能测试脚本
* kafka-reassign-partitions.sh：分区重分配脚本
* kafka-replica-verification.sh：复制进度验证脚本
* kafka-server-start.sh：启动 kafka 服务
* kafka-server-stop.sh：停止 kafka 服务
* kafka-topics.sh：topic 管理脚本
* kafka-verifiable-consumer.sh：可检验的 kafka 消费者
* kafka-verifiable-producer.sh：可检验的 kafka 生产者
* zookeeper-server-start.sh：启动 zk 服务
* zookeeper-server-stop.sh：停止 zk 服务
* zookeeper-shell.sh：zk 客户端

&emsp; 通常可以使用kafka-console-consumer.sh和kafka-console-producer.sh脚本来测试 Kafka 生产和消费，kafka-consumer-groups.sh可以查看和管理集群中的 Topic，kafka-topics.sh通常用于查看 Kafka 的消费组情况。  

## 1.7. MirrorMaker，镜像操作
&emsp; <font color = "red">Kafka提供了一个镜像操作的工具kafka-mirror-marker.sh，用于将一个集群的数据同步到另外一个集群。通过这个工具可以方便的实现两个集群之间的数据迁移；</font>  
&emsp; Kafka镜像工具的本质是创建一个消费者，从源集群中待迁移的主题消费数据，然后创建一个生产者，将消费者从源集群中拉取的数据写入目标集群。  
&emsp; 【注意】：  
&emsp; 由于镜像操作的命令是启动一个生产者和一个消费者进行数据镜像操作，因此数据同步完成之后，该命令依然在等待新的数据进行同步，也就是需要客户端自己查看数据是否已经同步完成，在保证数据同步完成之后需要手动关闭该命令。同时客户端可以在目标集群中创建主题，主题的分区以及副本数可以与源集群中该主题对应的分区以及副本数不一致。  
&emsp; 如果希望镜像操作启动的生产者在写入消息的时候创建主题则需要保证目标集群已经设置auto.create.topics.enable=true  

## 1.8. Kafka安全  
&emsp; 0.9版本之后，Kafka增加了身份认证和权限控制两种安全机制；
1. 身份认证：  
&emsp; 指客户端与服务端连接进行身份认证，包括客户端与kafka代理之间的连接认证、代理之间的连接认证、代理与ZooKeeper之间的连接认证。目前支持SSL、SASL/Kerberos、SASL/PLAIN这三种认证机制；  
2. 权限控制：  
&emsp; 权限控制是指对客户端的读写操作进行权限控制，包括对于消息或者Kafka集群操作权限控制。权限控制是可插拔的，并且支持与外部的授权服务进行集成；  
&emsp; kafk-acls.sh脚本支持查询（list）、添加（add）、移除（remove）这三类权限控制的操作。要启用Kafka ACL权限控制，首先需要在server.properties文件中增加权限控制实现类的设置；  