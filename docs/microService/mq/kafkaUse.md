


<!-- 


https://blog.csdn.net/BeiisBei/article/details/104264144
-->


命令行工具

Kafka 的命令行工具在 Kafka 包的/bin目录下，主要包括服务和集群管理脚本，配置脚本，信息查看脚本，Topic 脚本，客户端脚本等。

    kafka-configs.sh：配置管理脚本
    kafka-console-consumer.sh：kafka 消费者控制台
    kafka-console-producer.sh：kafka 生产者控制台
    kafka-consumer-groups.sh：kafka 消费者组相关信息
    kafka-delete-records.sh：删除低水位的日志文件
    kafka-log-dirs.sh：kafka 消息日志目录信息
    kafka-mirror-maker.sh：不同数据中心 kafka 集群复制工具
    kafka-preferred-replica-election.sh：触发 preferred replica 选举
    kafka-producer-perf-test.sh：kafka 生产者性能测试脚本
    kafka-reassign-partitions.sh：分区重分配脚本
    kafka-replica-verification.sh：复制进度验证脚本
    kafka-server-start.sh：启动 kafka 服务
    kafka-server-stop.sh：停止 kafka 服务
    kafka-topics.sh：topic 管理脚本
    kafka-verifiable-consumer.sh：可检验的 kafka 消费者
    kafka-verifiable-producer.sh：可检验的 kafka 生产者
    zookeeper-server-start.sh：启动 zk 服务
    zookeeper-server-stop.sh：停止 zk 服务
    zookeeper-shell.sh：zk 客户端

我们通常可以使用kafka-console-consumer.sh和kafka-console-producer.sh脚本来测试 Kafka 生产和消费，kafka-consumer-groups.sh可以查看和管理集群中的 Topic，kafka-topics.sh通常用于查看 Kafka 的消费组情况。