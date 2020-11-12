
<!-- TOC -->

- [1. SpringBoot集成kafka](#1-springboot集成kafka)
    - [1.1. SpringBoot集成kafka](#11-springboot集成kafka)
    - [1.2. Hello Kafka](#12-hello-kafka)
    - [1.3. 生产者](#13-生产者)
        - [1.3.1. 带回调的生产者](#131-带回调的生产者)
        - [1.3.2. 自定义分区器](#132-自定义分区器)
        - [1.3.3. kafka事务提交](#133-kafka事务提交)
    - [1.4. 消费者](#14-消费者)
        - [1.4.1. 指定topic、partition、offset消费](#141-指定topicpartitionoffset消费)
        - [1.4.2. 批量消费](#142-批量消费)
        - [1.4.3. ConsumerAwareListenerErrorHandler 异常处理器](#143-consumerawarelistenererrorhandler-异常处理器)
        - [1.4.4. 消息过滤器](#144-消息过滤器)
        - [1.4.5. 消息转发](#145-消息转发)
        - [1.4.6. 定时启动、停止监听器](#146-定时启动停止监听器)
    - [1.5. topic管理](#15-topic管理)

<!-- /TOC -->

# 1. SpringBoot集成kafka
<!-- 
SpringBoot集成kafka全面实战
https://blog.csdn.net/yuanlong122716/article/details/105160545/

 Java人应该知道的SpringBoot For Kafka (下) 
https://mp.weixin.qq.com/s/JB660Pgypr-PvkkdGOlhag
-->

1. 生产者实践
    * 普通生产者
    * 带回调的生产者
    * 自定义分区器
    * kafka事务提交
2. 消费者实践
    * 简单消费
    * 指定topic、partition、offset消费
    * 批量消费
    * 监听异常处理器
    * 消息过滤器
    * 消息转发
    * 定时启动/停止监听器

## 1.1. SpringBoot集成kafka  
&emsp; ① 引入pom依赖  

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```
&emsp; ② application.propertise配置  

```xml
###########【Kafka集群】###########
spring.kafka.bootstrap-servers=112.126.74.249:9092,112.126.74.249:9093
###########【初始化生产者配置】###########
# 重试次数
spring.kafka.producer.retries=0
# 应答级别:多少个分区副本备份完成时向生产者发送ack确认(可选0、1、all/-1)
spring.kafka.producer.acks=1
# 批量大小
spring.kafka.producer.batch-size=16384
# 提交延时
spring.kafka.producer.properties.linger.ms=0
# 当生产端积累的消息达到batch-size或接收到消息linger.ms后,生产者就会将消息提交给kafka
# linger.ms为0表示每接收到一条消息就提交给kafka,这时候batch-size其实就没用了
​
# 生产端缓冲区大小
spring.kafka.producer.buffer-memory = 33554432
# Kafka提供的序列化和反序列化类
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
# 自定义分区器
# spring.kafka.producer.properties.partitioner.class=com.felix.kafka.producer.CustomizePartitioner
​
###########【初始化消费者配置】###########
# 默认的消费组ID
spring.kafka.consumer.properties.group.id=defaultConsumerGroup
# 是否自动提交offset
spring.kafka.consumer.enable-auto-commit=true
# 提交offset延时(接收到消息后多久提交offset)
spring.kafka.consumer.auto.commit.interval.ms=1000
# 当kafka中没有初始offset或offset超出范围时将自动重置offset
# earliest:重置为分区中最小的offset;
# latest:重置为分区中最新的offset(消费分区中新产生的数据);
# none:只要有一个分区不存在已提交的offset,就抛出异常;
spring.kafka.consumer.auto-offset-reset=latest
# 消费会话超时时间(超过这个时间consumer没有发送心跳,就会触发rebalance操作)
spring.kafka.consumer.properties.session.timeout.ms=120000
# 消费请求超时时间
spring.kafka.consumer.properties.request.timeout.ms=180000
# Kafka提供的序列化和反序列化类
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
# 消费端监听的topic不存在时，项目启动会报错(关掉)
spring.kafka.listener.missing-topics-fatal=false
# 设置批量消费
# spring.kafka.listener.type=batch
# 批量消费每次最多消费多少条消息
# spring.kafka.consumer.max-poll-records=50
```

## 1.2. Hello Kafka  
1. 简单生产者  

```java
@RestController
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
​
    // 发送消息
    @GetMapping("/kafka/normal/{message}")
    public void sendMessage1(@PathVariable("message") String normalMessage) {
        kafkaTemplate.send("topic1", normalMessage);
    }
}
```

2. 简单消费  

```java
@Component
public class KafkaConsumer {
    // 消费监听
    @KafkaListener(topics = {"topic1"})
    public void onMessage1(ConsumerRecord<?, ?> record){
        // 消费的哪个topic、partition的消息,打印出消息内容
        System.out.println("简单消费："+record.topic()+"-"+record.partition()+"-"+record.value());
    }
}
```

&emsp; 上面示例创建了一个生产者，发送消息到topic1，消费者监听topic1消费消息。监听器用@KafkaListener注解，topics表示监听的topic，支持同时监听多个，用英文逗号分隔。启动项目，postman调接口触发生产者发送消息，  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-36.png)  
&emsp; 可以看到监听器消费成功，
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-37.png)  

## 1.3. 生产者
### 1.3.1. 带回调的生产者  
&emsp; kafkaTemplate提供了一个回调方法addCallback，我们可以在回调方法中监控消息是否发送成功 或 失败时做补偿处理，有两种写法，  

```java
@GetMapping("/kafka/callbackOne/{message}")
public void sendMessage2(@PathVariable("message") String callbackMessage) {
    kafkaTemplate.send("topic1", callbackMessage).addCallback(success -> {
        // 消息发送到的topic
        String topic = success.getRecordMetadata().topic();
        // 消息发送到的分区
        int partition = success.getRecordMetadata().partition();
        // 消息在分区内的offset
        long offset = success.getRecordMetadata().offset();
        System.out.println("发送消息成功:" + topic + "-" + partition + "-" + offset);
    }, failure -> {
        System.out.println("发送消息失败:" + failure.getMessage());
    });
}
```

```java
@GetMapping("/kafka/callbackTwo/{message}")
public void sendMessage3(@PathVariable("message") String callbackMessage) {
    kafkaTemplate.send("topic1", callbackMessage).addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
        @Override
        public void onFailure(Throwable ex) {
            System.out.println("发送消息失败："+ex.getMessage());
        }
 
        @Override
        public void onSuccess(SendResult<String, Object> result) {
            System.out.println("发送消息成功：" + result.getRecordMetadata().topic() + "-"
                    + result.getRecordMetadata().partition() + "-" + result.getRecordMetadata().offset());
        }
    });
}
```

### 1.3.2. 自定义分区器  
&emsp; kafka中每个topic被划分为多个分区，那么生产者将消息发送到topic时，具体追加到哪个分区呢？这就是所谓的分区策略，Kafka 为我们提供了默认的分区策略，同时它也支持自定义分区策略。其路由机制为：  
&emsp; ① 若发送消息时指定了分区（即自定义分区策略），则直接将消息append到指定分区；  
&emsp; ② 若发送消息时未指定 patition，但指定了 key（kafka允许为每条消息设置一个key），则对key值进行hash计算，根据计算结果路由到指定分区，这种情况下可以保证同一个 Key 的所有消息都进入到相同的分区；  
&emsp; ③  patition 和 key 都未指定，则使用kafka默认的分区策略，轮询选出一个 patition；  

&emsp; ※ 来自定义一个分区策略，将消息发送到指定的partition，首先新建一个分区器类实现Partitioner接口，重写方法，其中partition方法的返回值就表示将消息发送到几号分区，  

```java
public class CustomizePartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // 自定义分区规则(这里假设全部发到0号分区)
        // ......
        return 0;
    }
​
    @Override
    public void close() {
​
    }
​
    @Override
    public void configure(Map<String, ?> configs) {
​
    }
}
```

&emsp; 在application.propertise中配置自定义分区器，配置的值就是分区器类的全路径名，  

```properties
# 自定义分区器
spring.kafka.producer.properties.partitioner.class=com.felix.kafka.producer.CustomizePartitioner
```

### 1.3.3. kafka事务提交  
&emsp; 如果在发送消息时需要创建事务，可以使用 KafkaTemplate 的 executeInTransaction 方法来声明事务，  

```java
@GetMapping("/kafka/transaction")
public void sendMessage7(){
    // 声明事务：后面报错消息不会发出去
    kafkaTemplate.executeInTransaction(operations -> {
        operations.send("topic1","test executeInTransaction");
        throw new RuntimeException("fail");
    });
​
    // 不声明事务：后面报错但前面消息已经发送成功了
   kafkaTemplate.send("topic1","test executeInTransaction");
   throw new RuntimeException("fail");
}
```

## 1.4. 消费者  
### 1.4.1. 指定topic、partition、offset消费  
&emsp; 前面在监听消费topic1的时候，监听的是topic1上所有的消息，如果我们想指定topic、指定partition、指定offset来消费呢？也很简单，@KafkaListener注解已全部提供，  

```java
/**
 * @Title 指定topic、partition、offset消费
 * @Description 同时监听topic1和topic2，监听topic1的0号分区、topic2的 "0号和1号" 分区，指向1号分区的offset初始值为8
 * @Param [record]
 * @return void
 **/
@KafkaListener(id = "consumer1",groupId = "felix-group",topicPartitions = {
        @TopicPartition(topic = "topic1", partitions = { "0" }),
        @TopicPartition(topic = "topic2", partitions = "0", partitionOffsets = @PartitionOffset(partition = "1", initialOffset = "8"))
})
public void onMessage2(ConsumerRecord<?, ?> record) {
    System.out.println("topic:"+record.topic()+"|partition:"+record.partition()+"|offset:"+record.offset()+"|value:"+record.value());
}
```

&emsp; 属性解释：  
① id：消费者ID；  
② groupId：消费组ID；  
③ topics：监听的topic，可监听多个；   
④ topicPartitions：可配置更加详细的监听信息，可指定topic、parition、offset监听。  

&emsp; 上面onMessage2监听的含义：监听topic1的0号分区，同时监听topic2的0号分区和topic2的1号分区里面offset从8开始的消息。  
&emsp; 注意：topics和topicPartitions不能同时使用；  


### 1.4.2. 批量消费
&emsp; 设置application.prpertise开启批量消费即可，  

```properties
# 设置批量消费
spring.kafka.listener.type=batch
# 批量消费每次最多消费多少条消息
spring.kafka.consumer.max-poll-records=50
```

&emsp; 接收消息时用List来接收，监听代码如下，  

```java
@KafkaListener(id = "consumer2",groupId = "felix-group", topics = "topic1")
public void onMessage3(List<ConsumerRecord<?, ?>> records) {
    System.out.println(">>>批量消费一次，records.size()="+records.size());
    for (ConsumerRecord<?, ?> record : records) {
        System.out.println(record.value());
    }
}
```

### 1.4.3. ConsumerAwareListenerErrorHandler 异常处理器
&emsp; 通过异常处理器，我们可以处理consumer在消费时发生的异常。  
&emsp; 新建一个 ConsumerAwareListenerErrorHandler 类型的异常处理方法，用@Bean注入，BeanName默认就是方法名，然后我们将这个异常处理器的BeanName放到@KafkaListener注解的errorHandler属性里面，当监听抛出异常的时候，则会自动调用异常处理器，  

```java
// 新建一个异常处理器，用@Bean注入
@Bean
public ConsumerAwareListenerErrorHandler consumerAwareErrorHandler() {
    return (message, exception, consumer) -> {
        System.out.println("消费异常："+message.getPayload());
        return null;
    };
}
​
// 将这个异常处理器的BeanName放到@KafkaListener注解的errorHandler属性里面
@KafkaListener(topics = {"topic1"},errorHandler = "consumerAwareErrorHandler")
public void onMessage4(ConsumerRecord<?, ?> record) throws Exception {
    throw new Exception("简单消费-模拟异常");
}
​
// 批量消费也一样，异常处理器的message.getPayload()也可以拿到各条消息的信息
@KafkaListener(topics = "topic1",errorHandler="consumerAwareErrorHandler")
public void onMessage5(List<ConsumerRecord<?, ?>> records) throws Exception {
    System.out.println("批量消费一次...");
    throw new Exception("批量消费-模拟异常");
}
```
&emsp; 执行看一下效果，  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-38.png)  

### 1.4.4. 消息过滤器
&emsp; 消息过滤器可以在消息抵达consumer之前被拦截，在实际应用中，我们可以根据自己的业务逻辑，筛选出需要的信息再交由KafkaListener处理，不需要的消息则过滤掉。  
&emsp; 配置消息过滤只需要为 监听器工厂 配置一个RecordFilterStrategy（消息过滤策略），返回true的时候消息将会被抛弃，返回false时，消息能正常抵达监听容器。  

```java
@Component
public class KafkaConsumer {
    @Autowired
    ConsumerFactory consumerFactory;
​
    // 消息过滤器
    @Bean
    public ConcurrentKafkaListenerContainerFactory filterContainerFactory() {
        ConcurrentKafkaListenerContainerFactory factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory);
        // 被过滤的消息将被丢弃
        factory.setAckDiscarded(true);
        // 消息过滤策略
        factory.setRecordFilterStrategy(consumerRecord -> {
            if (Integer.parseInt(consumerRecord.value().toString()) % 2 == 0) {
                return false;
            }
            //返回true消息则被过滤
            return true;
        });
        return factory;
    }
​
    // 消息过滤监听
    @KafkaListener(topics = {"topic1"},containerFactory = "filterContainerFactory")
    public void onMessage6(ConsumerRecord<?, ?> record) {
        System.out.println(record.value());
    }
}
```

&emsp; 上面实现了一个"过滤奇数、接收偶数"的过滤策略，向topic1发送0-99总共100条消息，看一下监听器的消费情况，可以看到监听器只消费了偶数，  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-39.png)  

### 1.4.5. 消息转发
&emsp; 在实际开发中，我们可能有这样的需求，应用A从TopicA获取到消息，经过处理后转发到TopicB，再由应用B监听处理消息，即一个应用处理完成后将该消息转发至其他应用，完成消息的转发。  
&emsp; 在SpringBoot集成Kafka实现消息的转发也很简单，只需要通过一个@SendTo注解，被注解方法的return值即转发的消息内容，如下，  

```java
/**
 * @Title 消息转发
 * @Description 从topic1接收到的消息经过处理后转发到topic2
 * @Param [record]
 * @return void
 **/
@KafkaListener(topics = {"topic1"})
@SendTo("topic2")
public String onMessage7(ConsumerRecord<?, ?> record) {
    return record.value()+"-forward message";
}
```

### 1.4.6. 定时启动、停止监听器
&emsp; 默认情况下，当消费者项目启动的时候，监听器就开始工作，监听消费发送到指定topic的消息，那如果我们不想让监听器立即工作，想让它在我们指定的时间点开始工作，或者在我们指定的时间点停止工作，该怎么处理呢——使用KafkaListenerEndpointRegistry，下面我们就来实现：  
&emsp; ① 禁止监听器自启动；   
&emsp; ② 创建两个定时任务，一个用来在指定时间点启动定时器，另一个在指定时间点停止定时器；  
&emsp; 新建一个定时任务类，用注解@EnableScheduling声明，KafkaListenerEndpointRegistry 在SpringIO中已经被注册为Bean，直接注入，设置禁止KafkaListener自启动，  

```java
@EnableScheduling
@Component
public class CronTimer {
​
    /**
     * @KafkaListener注解所标注的方法并不会在IOC容器中被注册为Bean，
     * 而是会被注册在KafkaListenerEndpointRegistry中，
     * 而KafkaListenerEndpointRegistry在SpringIOC中已经被注册为Bean
     **/
    @Autowired
    private KafkaListenerEndpointRegistry registry;
​
    @Autowired
    private ConsumerFactory consumerFactory;
​
    // 监听器容器工厂(设置禁止KafkaListener自启动)
    @Bean
    public ConcurrentKafkaListenerContainerFactory delayContainerFactory() {
        ConcurrentKafkaListenerContainerFactory container = new ConcurrentKafkaListenerContainerFactory();
        container.setConsumerFactory(consumerFactory);
        //禁止KafkaListener自启动
        container.setAutoStartup(false);
        return container;
    }
​
    // 监听器
    @KafkaListener(id="timingConsumer",topics = "topic1",containerFactory = "delayContainerFactory")
    public void onMessage1(ConsumerRecord<?, ?> record){
        System.out.println("消费成功："+record.topic()+"-"+record.partition()+"-"+record.value());
    }
​
    // 定时启动监听器
    @Scheduled(cron = "0 42 11 * * ? ")
    public void startListener() {
        System.out.println("启动监听器...");
        // "timingConsumer"是@KafkaListener注解后面设置的监听器ID,标识这个监听器
        if (!registry.getListenerContainer("timingConsumer").isRunning()) {
            registry.getListenerContainer("timingConsumer").start();
        }
        //registry.getListenerContainer("timingConsumer").resume();
    }
​
    // 定时停止监听器
    @Scheduled(cron = "0 45 11 * * ? ")
    public void shutDownListener() {
        System.out.println("关闭监听器...");
        registry.getListenerContainer("timingConsumer").pause();
    }
}
```
&emsp; 启动项目，触发生产者向topic1发送消息，可以看到consumer没有消费，因为这时监听器还没有开始工作，  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-40.png)  
&emsp; 11:42分监听器启动开始工作，消费消息，  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/mq/kafka/kafka-41.png)  


## 1.5. topic管理  
<!-- 

https://blog.csdn.net/yuanlong122716/article/details/105160545/
-->


