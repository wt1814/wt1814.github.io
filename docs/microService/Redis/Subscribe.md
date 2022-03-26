<!-- TOC -->

- [1. Redis发布订阅](#1-redis发布订阅)
    - [1.1. 发布订阅简介](#11-发布订阅简介)
    - [1.2. redis发布订阅和rabbitmq的区别](#12-redis发布订阅和rabbitmq的区别)
    - [1.3. redis发布订阅命令](#13-redis发布订阅命令)
    - [1.4. 代码实练](#14-代码实练)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; **<font color = "clime">redis与其他mq相比：轻量级，低延迟，高并发，低可靠性。</font>**  
&emsp; **<font color = "clime">常见使用场景：实时聊天、博客的粉丝文章推送......</font>**  


# 1. Redis发布订阅  
<!-- 
Redis 消息队列的三种方案(List、Streams、Pub/Sub) 
https://mp.weixin.qq.com/s/_q0bI62iFrG8h-gZ-bCvNQ
Redis源码学习(59)-Redis可持久化的消息队列（3） 
https://mp.weixin.qq.com/s/i9e_JHng_etzJcLkHCBiEQ
-->

## 1.1. 发布订阅简介  
&emsp; Redis 提供了基于“发布/订阅”模式的消息机制，发送者(publish)发送消息，订阅者(subscribe)接收消息。Redis作为消息发布和订阅之间的服务器，起到桥梁的作用，在Redis里面有一个channel的概念，也就是频道，发布者通过指定发布到某个频道，然后只要有订阅者订阅了该频道，该消息就会发送给订阅者。Redis客户端可以订阅任意数量的频道。  

&emsp; 下图展示了频道channel1，以及订阅这个频道的三个客户端——client2、client5和client1之间的关系：  
![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-100.png)  
&emsp; 当有新消息通过 PUBLISH 命令发送给频道 channel1 时， 这个消息就会被发送给订阅它的三个客户端：  
![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-101.png)  

&emsp; **<font color = "red">使用场景：</font>**  
&emsp; Redis的发布与订阅的功能应用还是比较广泛的，它的应用场景有很多。比如：最常见的就是实现实时聊天的功能，还有就是博客的粉丝文章的推送，当博主推送原创文章的时候，就会将文章实时推送给博主的粉丝。  

## 1.2. redis发布订阅和rabbitmq的区别  
* 可靠性  
&emsp; redis：没有相应的机制保证消息的可靠消费，如果发布者发布一条消息，而没有对应的订阅者的话，这条消息将丢失，不会存在内存中；  
&emsp; rabbitmq：具有消息消费确认机制，如果发布一条消息，还没有消费者消费该队列，那么这条消息将一直存放在队列中，直到有消费者消费了该条消息，以此可以保证消息的可靠消费；
* 实时性  
&emsp; redis：实时性高，redis作为高效的缓存服务器，所有数据都存在在服务器中，所以它具有更高的实时性。
* 消费者负载均衡  
&emsp; rabbitmq队列可以被多个消费者同时监控消费，但是每一条消息只能被消费一次，由于rabbitmq的消费确认机制，因此它能够根据消费者的消费能力而调整它的负载；  
&emsp; redis发布订阅模式，一个队列可以被多个消费者同时订阅，当有消息到达时，会将该消息依次发送给每个订阅者；  
* 持久性  
&emsp; redis：redis的持久化是针对于整个redis缓存的内容，可以将整个redis实例持久化到磁盘，以此来做数据备份，防止异常情况下导致数据丢失。  
&emsp; rabbitmq：队列，消息都可以选择性持久化，持久化粒度更小，更灵活；  
* 队列监控  
&emsp; rabbitmq实现了后台监控平台，可以在该平台上看到所有创建的队列的详细情况，良好的后台管理平台可以方面我们更好的使用；  
&emsp; redis没有所谓的监控平台。  

&emsp; **<font color = "clime">总结：</font>**  
&emsp; redis：轻量级，低延迟，高并发，低可靠性；  
&emsp; rabbitmq：重量级，高可靠，异步，不保证实时；  
&emsp; rabbitmq是一个专门的AMQP协议队列，它的优势就在于提供可靠的队列服务，并且可做到异步，而redis主要是用于缓存的，redis的发布订阅模块，可用于实现及时性，且可靠性低的功能。  

## 1.3. redis发布订阅命令  

* PSUBSCRIBE pattern [pattern1 ....]
    * 说明：订阅一个或多个符合给定模式的频道，每个模式以*作为匹配符
    * 参数：pattern(给定的模式)
    * 返回：接受到的信息
* PUNSUBSCRIBE pattern [pattern1 ....]
    * 说明：用于退订所有给定模式的频道
    * 参数：pattern(给定的模式)
    * 返回：这个命令在不同的客户端中有不同的表现。
* SUBSCRIBE channel [channel1 ...]
    * 说明：用于订阅给定的一个或多个频道的信息
    * 参数：channel(给定的频道名)
    * 返回：接收到的信息
* UNSUBSCRIBE channel [channel1 ...]
    * 说明：用于退订给定的一个或多个频道的信息
    * 参数：channel(给定的频道名)
    * 返回：这个命令在不同的客户端中有不同的表现
* PUBLISH channel message
    * 说明：用于将信息发送到指定的频道
    * 参数：channel(频道名称)，message(将要发送的信息)
    * 返回：接收到此消息的订阅者数量
* PUBSUB < subcommand > argument [argument1 ....]
    * 说明：用于查看订阅与发布系统状态，它由数个不同格式的子命令组成
    * 参数：subcommand(子命令)，argument(子命令参数)
    * 返回：由活跃频道组成的列表
    * 子命令如下  
    
    |subcommand | argument | 说明|  
    |---|---|---|  
    |CHANNELS|\[pattern]|返回指定模式pattern的活跃的频道，指定返回由SUBSCRIBE订阅的频道|  
    |NUMSUB	|channel channel2 ...|返回指定频道的订阅数量|  
    |NUMPAT| |返回订阅模式的数量，注意：这个命令返回的不是订阅模式的客户端的数量，而是客户端订阅的所有模式的数量总和|  

## 1.4. 代码实练  
<!-- 
~~
https://mp.weixin.qq.com/s/ConI2dcD9F9crSx0qVa7GQ
https://mp.weixin.qq.com/s?__biz=MzI5NTYwNDQxNA==&mid=2247486105&idx=2&sn=f4b4734951ec262ad67c865be940e5c5&chksm=ec505348db27da5ee9b956e40963b0abb52f739863a82c7d1838ca1a92928f50facc012ccd12&scene=21#wechat_redirect
-->

&emsp; (1)首先第一步想要操作Redis，再SpringBoot项目中引入jedis的依赖，毕竟jedis是官方推荐使用操作Redis的工具。  

```xml
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>2.9.0</version>
</dependency>
```

&emsp; (2)然后创建发布者Publisher，用于消息的发布，具体代码如下：  

```java
package com.ldc.org.myproject.demo.redis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 发布者
 *
 */
public class Publisher extends Thread{
 // 连接池 
 private final JedisPool jedisPool;
 // 发布频道名称
 private String name;
 
 public Publisher(JedisPool jedisPool, String name) {
  super();
  this.jedisPool = jedisPool;
  this.name = name;
 }
 
 @Override
 public void run() {
  // 获取要发布的消息
  BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  // 获取连接
  Jedis resource = jedisPool.getResource();
  while (true) {
   String message = null;
   try {
    message = reader.readLine();
    if (!"exit".equals(message)) {
     // 发布消息
     resource.publish(name, "发布者:"+Thread.currentThread().getName()+"发布消息："+message);
    } else {
     break;
    }
   } catch (IOException e) {
    e.printStackTrace();
   }
  }
 }
}
```

&emsp; (3)接着创建订阅类Subscriber，并且继承JedisPubSub 类，重写onMessage、onSubscribe、onUnsubscribe三个方法，这三个方法的调用时机在注释上都有说明，具体的实现代码如下：  

```java
package com.ldc.org.myproject.demo.redis;

import com.fasterxml.jackson.core.sym.Name;
import redis.clients.jedis.JedisPubSub;

/**
 * 订阅者
 */
public class Subscriber extends JedisPubSub {
 //订阅频道名称
 private String name;
 
 public Subscriber(String name) {
  this.name = name;
 }

 /**
  * 订阅者收到消息时会调用
  */
 @Override
 public void onMessage(String channel, String message) {
  // TODO Auto-generated method stub
  super.onMessage(channel, message);
  System.out.println("频道："+channel+"  接受的消息为："+message);
 }

 /**
  * 订阅了频道会被调用
  */
 @Override
 public void onSubscribe(String channel, int subscribedChannels) {
  System.out.println("订阅了频道:"+channel+"  订阅数为："+subscribedChannels);
 }

 /**
  * 取消订阅频道会被调用
  */
 @Override
 public void onUnsubscribe(String channel, int subscribedChannels) {
  System.out.println("取消订阅的频道："+channel+"  订阅的频道数量为："+subscribedChannels);
 }
}
```

&emsp; (4)这次创建的才是真正的订阅者SubThread，上面的Subscriber是指为了测试实订阅的时候或者发布消息，能够有信息输出：  

```java
package com.ldc.org.myproject.demo.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 订阅者线程
 *
 */
public class SubThread extends Thread {
 
 private final JedisPool jedisPool;
 
 private final Subscriber subscriber;
 
 private String name;
 
 public SubThread(JedisPool jedisPool,Subscriber subscriber,String name) {
  super();
  this.jedisPool = jedisPool;
  this.subscriber = subscriber;
  this.name = name;
 }
 
 @Override
 public void run() {
  Jedis jedis = null;
  try {
   jedis = jedisPool.getResource();
   // 订阅频道为name
   jedis.subscribe(subscriber, name);
  } catch (Exception e) {
   System.err.println("订阅失败");
      e.printStackTrace();
  } finally {
   if (jedis!=null) {
     // jedis.close();
     //归还连接到redis池中
    jedisPool.returnResource(jedis);
   }
  }
 }
}
```

&emsp; (5)后面就是测试了，分别测试发布与订阅的测试，发布者为TestPublisher，订阅者为TestSubscriber：  

```java
package com.ldc.org.myproject.demo.redis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import redis.clients.jedis.JedisPool;

public class TestPublisher {
 
 public static void main(String[] args) throws InterruptedException {
  JedisPool jedisPool = new JedisPool("192.168.163.155");
  // 向ldc频道发布消息
  Publisher publisher = new Publisher(jedisPool, "ldc");
  publisher.start();
 }
}
```

&emsp; 订阅者  

```java
package com.ldc.org.myproject.demo.redis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.JedisPool;

public class TestSubscriber1 {
 
 public static void main(String[] args) throws InterruptedException {
  JedisPool jedisPool = new JedisPool("192.168.163.155",6379);
  Subscriber subscriber = new Subscriber("黎杜");
  // 订阅ldc频道
  SubThread thread= new SubThread(jedisPool, subscriber, "ldc");
  thread.start();
  Thread.sleep(600000);
  // 取消订阅
  subscriber.unsubscribe("ldc");
 }
}
```

&emsp; 这里为了测试方便就直接创建线程的方式，更好的话可以使用线程池的方式通过线程池的submit方法来执行线程，若是不用了可以使用shutdown方式关闭。  