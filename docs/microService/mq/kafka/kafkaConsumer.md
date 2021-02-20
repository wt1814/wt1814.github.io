<!-- TOC -->

- [1. kafka消费者](#1-kafka消费者)
    - [1.1. 消息消费示例](#11-消息消费示例)
    - [1.2. poll()方法详解](#12-poll方法详解)
        - [1.2.1. pollForFetches()](#121-pollforfetches)

<!-- /TOC -->

# 1. kafka消费者  
<!-- 
https://www.cnblogs.com/dennyzhangdd/p/7827564.html
-->

## 1.1. 消息消费示例  
&emsp; **自动提交消费进度**  

```java
public static void testConsumer1() {
    Properties props = new Properties();
    props.setProperty("bootstrap.servers", "localhost:9092,localhost:9082,localhost:9072");
    props.setProperty("group.id", "C_ODS_ORDERCONSUME_01");
    props.setProperty("enable.auto.commit", "true");
    props.setProperty("auto.commit.interval.ms", "1000");
    props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Arrays.asList("TOPIC_ORDER"));
    while (true) {
        ConsumerRecords<String, String>  records = consumer.poll(Duration.ofMillis(100));
        for (ConsumerRecord<String, String> record : records) {
            System.out.println("消息消费中");
            System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
        }
    }
}
```

<!-- 
&emsp; **手动提交消费进度**  

```java
public static void testConsumer2() {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:9092");
        props.setProperty("group.id", "test");
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("foo", "bar"));
        final int minBatchSize = 200;
        List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                buffer.add(record);
            }
            if (buffer.size() >= minBatchSize) {
                // insertIntoDb(buffer);
                // 省略处理逻辑
                consumer.commitSync();
                buffer.clear();
            }
        }
    }
}
```
-->
## 1.2. poll()方法详解  

```java
private ConsumerRecords<K, V> poll(final Timer timer, final boolean includeMetadataInTimeout) {
    // 获取锁，并确保消费者没有被关闭
    acquireAndEnsureOpen();
    try {
        // 判断消费者有没有订阅主题
        if (this.subscriptions.hasNoSubscriptionOrUserAssignment()) {
            throw new IllegalStateException("Consumer is not subscribed to any topics or assigned any partitions");
        }

        // poll for new data until the timeout expires
        do {
            // 安全的唤醒消费客户端
            client.maybeTriggerWakeup();

            // 传入的参数，是否需要更新偏移量
            if (includeMetadataInTimeout) {
                // 对协调器事件进行轮询。这确保协调器是已知的，并且使用者已经加入了组(如果它正在使用组管理)。如果启用了定期偏移量提交，这也将处理它们。如果超时返回false
                // 将获取位置设置为提交位置(如果有)，或者使用用户配置的偏移重置策略重置它。
                if (!updateAssignmentMetadataIfNeeded(timer)) {
                    // 超时返回空
                    return ConsumerRecords.empty();
                }
            } else {
                // 对协调器事件进行轮询。这确保协调器是已知的，并且使用者已经加入了组(如果它正在使用组管理)。如果启用了定期偏移量提交，这也将处理它们。这里传入的超时时长非常大，会一直等待直至完成对协调器时间的轮询
                // 将获取位置设置为提交位置(如果有)，或者使用用户配置的偏移重置策略重置它。
                while (!updateAssignmentMetadataIfNeeded(time.timer(Long.MAX_VALUE))) {
                    log.warn("Still waiting for metadata");
                }
            }

            // 抓取到数据，下面详细介绍 pollForFetches 方法
            final Map<TopicPartition, List<ConsumerRecord<K, V>>> records = pollForFetches(timer);
            if (!records.isEmpty()) {
                // before returning the fetched records, we can send off the next round of fetches
                // and avoid block waiting for their responses to enable pipelining while the user
                // is handling the fetched records.
                //
                // NOTE: since the consumed position has already been updated, we must not allow
                // wakeups or any other errors to be triggered prior to returning the fetched records.
                // 有获取数据的请求 或者 消费客户端有未完成的请求（这包括已经传输的请求(即飞行中的请求)和正在等待传输的请求。）
                if (fetcher.sendFetches() > 0 || client.hasPendingRequests()) {
                    // 在返回获取的数据记录之前，我们可以发送下一轮获取请求，并避免在用户处理获取的记录时阻塞等待它们的响应以启用管道。
                    client.pollNoWakeup();
                }

                // 返回消费的数据记录，不过需要先由拦截器加工一下
                return this.interceptors.onConsume(new ConsumerRecords<>(records));
            }
            // 只要没有超时，就一直循环消费数据
        } while (timer.notExpired());

        return ConsumerRecords.empty();
    } finally {
        release();
    }
}
```

1. 先获取保护该使用者不受多线程访问的轻锁。然而，当锁不可用时，不是阻塞，而是抛出一个异常（因为不支持多线程使用）， 再确保只有一个线程在消费数据，判断消费者的状态，如果被关闭就抛异常
2. 进入到循环，安全的唤醒消费客户端，这里维护了两个原子标志（线程安全）：
wakeupDisabled，线程在执行不可中断的方法；wakeup，线程中断请求。  
&emsp; 如果wakeupDisabled 为 0 并且 wakeup 是 1，则把 wakeup置为 0 并抛出异常，中断该线程。  
3. 获取消费的数据，如果不为空，发送下一轮获取数据的请求（异步）。
4. 将获取的数据经过拦截器加工后，返回结果数据。

### 1.2.1. pollForFetches()  

```java
private Map<TopicPartition, List<ConsumerRecord<K, V>>> pollForFetches(Timer timer) {
    long pollTimeout = Math.min(coordinator.timeToNextPoll(timer.currentTimeMs()), timer.remainingMs());

    // if data is available already, return it immediately
    // 获取数据，如果数据不为空，则直接返回
    final Map<TopicPartition, List<ConsumerRecord<K, V>>> records = fetcher.fetchedRecords();
    if (!records.isEmpty()) {
        return records;
    }

    // send any new fetches (won't resend pending fetches)
    // 发送任何新的读取请求(不会重新发送挂起的读取请求)
    fetcher.sendFetches();

    // We do not want to be stuck blocking in poll if we are missing some positions
    // since the offset lookup may be backing off after a failure

    // NOTE: the use of cachedSubscriptionHashAllFetchPositions means we MUST call
    // updateAssignmentMetadataIfNeeded before this method.
    // 要避免重复扫描poll()中的订阅，判断是否在元数据更新期间缓存结果
    if (!cachedSubscriptionHashAllFetchPositions && pollTimeout > retryBackoffMs) {
        pollTimeout = retryBackoffMs;
    }

    // 获取超时时长
    Timer pollTimer = time.timer(pollTimeout);
    // 再次获取数据
    client.poll(pollTimer, () -> {
        // since a fetch might be completed by the background thread, we need this poll condition
        // to ensure that we do not block unnecessarily in poll()
        // 因为后台线程可能会完成一次获取，所以我们需要这个轮询条件来确保不会在poll()中不必要地阻塞
        return !fetcher.hasCompletedFetches();
    });
    timer.update(pollTimer.currentTimeMs());

    // after the long poll, we should check whether the group needs to rebalance
    // prior to returning data so that the group can stabilize faster
    // 在长时间的获取数据之后，我们应该在返回数据之前检查一下这个群体是否需要重新平衡，以便这个群体能够更快地稳定下来
    if (coordinator.rejoinNeededOrPending()) {
        return Collections.emptyMap();
    }

    return fetcher.fetchedRecords();
}
```

1. 获取数据，因为poll方法里面在加工数据之前，就发送了获取数据的网路io请求，所以这里直接获取，如果获取到直接返回数据。  
2. 如果获取到的数据为空，那么接着发送获取数据的请求。  
3. 判断是否在元数据更新期间缓存结果，避免重复扫描poll()中的订阅。  
4. 消费客户端调用poll，判断是否有任何已完成的取数操作等待返回给用户取反作为参数 disableWakeup，如果没有已完成的取数操作等待返回给用户，即传入true，会禁用触发唤醒。  
5. 更新超时时间。在长时间的获取数据之后，应该在返回数据之前检查一下这个群体是否需要重新平衡，以便这个群体能够更快地稳定下来。  
6. 最后获取数据并返回。  
