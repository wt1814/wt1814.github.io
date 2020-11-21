<!-- TOC -->

- [1. 主题与分区](#1-主题与分区)
    - [1.1. 主题管理](#11-主题管理)
    - [1.2. 分区管理](#12-分区管理)
        - [1.2.1. 分区策略](#121-分区策略)
    - [1.3. 如何选择合适的分区数？](#13-如何选择合适的分区数)

<!-- /TOC -->

# 1. 主题与分区
&emsp; 《深入理解Kafka》第4章

## 1.1. 主题管理  
......

## 1.2. 分区管理  
### 1.2.1. 分区策略  
&emsp; Kafka提供了默认的分区策略（轮询、随机、按key顺序），同时支持自定义分区策略。  

```java
public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
    List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
    int numPartitions = partitions.size();
    if (keyBytes == null) {
        int nextValue = nextValue(topic);
        List<PartitionInfo> availablePartitions = cluster.availablePartitionsForTopic(topic);
        if (availablePartitions.size() > 0) {
            //轮询
            int part = Utils.toPositive(nextValue) % availablePartitions.size();
            return availablePartitions.get(part).partition();
        } else {
            // no partitions are available, give a non-available partition
            //随机
            return Utils.toPositive(nextValue) % numPartitions;
        }
    } else {
        // hash the keyBytes to choose a partition
        // 按key顺序
        return Utils.toPositive(Utils.murmur2(keyBytes)) % numPartitions;
    }
}
```
1. 首先kafka会判断有没有key，这里的key就是每条消息定义的消息键，发消息的时候在ProducerRecord(String topic, K key, V value)中指定的key。  
2. 如果没有key，会采用轮询策略，也称 Round-robin 策略，即顺序分配。比如一个主题下有 3 个分区，那么第一条消息被发送到分区 0，第二条被发送到分区 1，第三条被发送到分区 2，以此类推。当生产第 4 条消息时又会重新开始上述轮询。轮询策略有非常优秀的负载均衡表现，它总是能保证消息最大限度地被平均分配到所有分区上，故默认情况下它是最合理的分区策略。  
3. 如果有key，那么就按消息键策略，这样可以保证同一个 Key 的所有消息都进入到相同的分区里面，这样就保证了顺序性了。  


## 1.3. 如何选择合适的分区数？  
&emsp; 在 Kafka 中，性能与分区数有着必然的关系，在设定分区数时一般也需要考虑性能的因素。对不同的硬件而言，其对应的性能也会不太一样。  
&emsp; 可以使用Kafka 本身提供的用于生产者性能测试的 kafka-producer- perf-test.sh 和用于消费者性能测试的 kafka-consumer-perf-test.sh来进行测试。  
&emsp; 增加合适的分区数可以在一定程度上提升整体吞吐量，但超过对应的阈值之后吞吐量不升反降。如果应用对吞吐量有一定程度上的要求，则建议在投入生产环境之前对同款硬件资源做一个完备的吞吐量相关的测试，以找到合适的分区数阈值区间。  
&emsp; 分区数的多少还会影响系统的可用性。如果分区数非常多，如果集群中的某个 broker 节点宕机，那么就会有大量的分区需要同时进行 leader 角色切换，这个切换的过程会耗费一笔可观的时间，并且在这个时间窗口内这些分区也会变得不可用。  
&emsp; 分区数越多也会让 Kafka 的正常启动和关闭的耗时变得越长，与此同时，主题的分区数越多不仅会增加日志清理的耗时，而且在被删除时也会耗费更多的时间。  


