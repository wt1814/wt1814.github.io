

<!-- TOC -->

- [1. Spring Cloud Sleuth](#1-spring-cloud-sleuth)
    - [1.1. 全链路监控功能](#11-全链路监控功能)
    - [1.2. Spring Cloud Sleuth](#12-spring-cloud-sleuth)
        - [1.2.1. Sleuth实现追踪](#121-sleuth实现追踪)
        - [1.2.2. 跟踪原理](#122-跟踪原理)
        - [1.2.3. spring-cloud-starter-sleuth功能点](#123-spring-cloud-starter-sleuth功能点)
            - [1.2.3.1. 抽样收集](#1231-抽样收集)
            - [1.2.3.2. ★★★获取当前traceId](#1232-★★★获取当前traceid)
            - [1.2.3.3. ★★★日志获取traceId](#1233-★★★日志获取traceid)
            - [1.2.3.4. 传递traceId到异步线程池](#1234-传递traceid到异步线程池)
            - [1.2.3.5. 子线程或线程池中获取 Zipkin traceId 并打印](#1235-子线程或线程池中获取-zipkin-traceid-并打印)
            - [1.2.3.6. 监控本地方法](#1236-监控本地方法)
            - [1.2.3.7. 计划任务](#1237-计划任务)
            - [1.2.3.8. TracingFilter](#1238-tracingfilter)
            - [1.2.3.9. 过滤不想跟踪的请求](#1239-过滤不想跟踪的请求)
            - [1.2.3.10. 用RabbitMq代替Http发送调用链数据](#12310-用rabbitmq代替http发送调用链数据)
    - [1.3. Zipkin](#13-zipkin)
        - [1.3.1. 与Zipkin整合](#131-与zipkin整合)
            - [1.3.1.1. 在Zipkin中图形化展示分布式链接监控数据](#1311-在zipkin中图形化展示分布式链接监控数据)
            - [1.3.1.2. 链路信息收集](#1312-链路信息收集)
                - [1.3.1.2.1. 消息中间件收集](#13121-消息中间件收集)
            - [1.3.1.3. 数据持久化](#1313-数据持久化)
            - [1.3.1.4. API接口](#1314-api接口)
        - [1.3.2. SpringMVC、dubbo集成zipkin](#132-springmvcdubbo集成zipkin)

<!-- /TOC -->

# 1. Spring Cloud Sleuth
<!-- 
一文搞懂全链路监控：方案概述与比较 | 干货
https://mp.weixin.qq.com/s/CZnVxs0vDMBhhBUiioon3g
主流微服务全链路监控系统之战 
https://mp.weixin.qq.com/s/WfTEQagsRntOpMVIZZS_Rw
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-33.png)  

## 1.1. 全链路监控功能  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-42.png)  
&emsp; 微服务分层架构之后，系统架构变得越来越复杂：  
&emsp; （1）站点层会调用业务服务层；  
&emsp; （2）业务服务会调用基础服务层；  
&emsp; （3）任何一个服务都有可能调用缓存或者数据库；  
&emsp; 一个请求，会经历多台服务器，多个进程。  

&emsp; 如此这般，一些棘手的问题如期而至：  
&emsp; **<font color = "red">(1) 如何快速定位请求异常；</font>**    
&emsp; **<font color = "red">(2) 如何快速定位性能瓶颈；</font>**  
&emsp; **<font color = "red">(3) 如何快速定位不合理调用；</font>**  
&emsp; 这些，是分布式调用链追踪系统，要解决的问题。  

&emsp; **一般的全链路监控系统，大致可分为四大功能模块：**
1. 埋点与生成日志。  
&emsp; 埋点即系统在当前节点的上下文信息，可以分为客户端埋点、服务端埋点，以及客户端和服务端双向型埋点。埋点日志通常要包含以下内容traceId、spanId、调用的开始时间，协议类型、调用方ip和端口，请求的服务名、调用耗时，调用结果，异常信息等，同时预留可扩展字段，为下一步扩展做准备；  
2. 收集和存储日志。  
&emsp; 主要支持分布式日志采集的方案，同时增加MQ作为缓冲；  
3. 分析和统计调用链路数据以及时效性。  
&emsp; 调用链跟踪分析：把同一TraceID的Span收集起来，按时间排序就是timeline。把ParentID串起来就是调用栈。  
&emsp; 抛异常或者超时，在日志里打印TraceID。利用TraceID查询调用链情况，定位问题。  
4. 展现以及决策支持。  

## 1.2. Spring Cloud Sleuth
### 1.2.1. Sleuth实现追踪  
&emsp; 在服务提供者和服务消费者引入spring-cloud-starter-sleuth依赖。  

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```
&emsp; 访问消费者接口，控制台日志显示。  
&emsp; 消费者(springcloud-consumer-sleuth)打印的日志：  

```text
2019-12-05 12:30:20.178  INFO [springcloud-consumer-sleuth,f6fb983680aab32b,f6fb983680aab32b,false] 8992 --- [nio-9090-exec-1] c.s.controller.SleuthConsumerController  : === consumer service ===
```
&emsp; 提供者(springcloud-provider-sleuth)打印的日志  

```text
2019-12-05 12:30:20.972  INFO [springcloud-provider-sleuth,f6fb983680aab32b,c70932279d3b3a54,false] 788 --- [nio-8080-exec-1] c.s.controller.SleuthProviderController  : === provider service ===
```
&emsp; INFO[]标签中为链路追踪信息。每个值的含义如下所述：  

* 第一个值: springcloud-consumer-sleuth，它记录了应用的名称，也就是application properties中spring.application.name参数配置的属性  
* 第二个值:f6fb983680aab32b, Spring Cloud Sleuth生成的一个ID，称为Trace ID，它用来标识一条请求链路。一条请求链路中包含一个Trace ID，多个Span ID  
* 第三个值:c70932279d3b3a54，Spring Cloud Sleuth生成的另外一个ID，称为Span ID，它表示一个基本的工作单元，比如发送一个HTTP请求  
* 第四个值: false，表示是否要将该信息输出到Zipkin等服务中来收集和展示。


&emsp; 上面四个值中的Trace ID和Span ID是Spring Cloud Sleuth实现分布式服务跟踪的核心，在一次服务请求链路的调用过程中，会保持并传递同一个Trace ID，从而将整个分布于不同微服务进程中的请求跟踪信息串联起来。以上面输出内容为例springcloud-consumer-sleuth和springcloud-provider-sleuth同属于一个前端服务请求资源，所以他们的Trace ID是相同的，处于同一条请求链路中。  

### 1.2.2. 跟踪原理  
&emsp; 分布式系统服务跟踪原理主要包括下面两个关键点：  

* 为了实现请求跟踪，当请求发送到分布式系统的入口端点时，只需要服务跟踪框架为该请求创建一个唯一的跟踪标识，同时在分布式系统内部流转的时候，框架始终保持传递该唯一标识，直到返回给请求方为止，这个唯一标识就是前文中提到的Trace ID。通过Trace ID的记录，就能将所有请求过程的日志关联起来。  
* 为了统计各处理单元的时间延迟，当请求到达各个服务组件时，或是处理逻辑到达某个状态时，也通过一个唯一标识来标记它的开始、具体过程以及结束，该标识就是前面提到的Span ID。对于每个Span来说，它必须有开始和结束两个节点，通过记录开始Span和结束Span的时间戳，就能统计出该Span的时间延迟，除了时间戳记录之外，它还可以包含一些其他元数据，比如事件名称、请求信息等。  

&emsp; 在SpringBoot应用中引入spring-cloud-starter-sleuth依赖之后，<font color = "red">sleuth会自动为当前应用构建起各通信通道的跟踪机制</font>，比如： 

* 通过RabbitMQ、Kafka(或者其他任何Spring Cloud Stream绑定器实现的消息中间件)传递的请求  
* 通过Zuul代理传递的请求  
* 通过RestTemplate发起的请求  

### 1.2.3. spring-cloud-starter-sleuth功能点  
#### 1.2.3.1. 抽样收集  
&emsp; 如果服务的流量很大，全部采集对传输、存储压力比较大。这个时候可以设置采样率，sleuth可以通过配置spring.sleuth.sampler.probability=X.Y（如配置为1.0，则采样率为100%，采集服务的全部追踪数据），若不配置默认采样率是0.1(即10%)。也可以通过实现bean的方式来设置采样为全部采样(AlwaysSampler)或者不采样(NeverSampler)。如  

```java
@Bean 
public Sampler defaultSampler() { 
    return new AlwaysSampler(); 
}
```

#### 1.2.3.2. ★★★获取当前traceId  
&emsp; 在项目中获取traceId，可以参考下述的方式  

```java
import brave.Tracer;

@Service
public class Breadcrumb {

    @Autowired
    private Tracer tracer;

    public String breadcrumbId() {
        return tracer.currentSpan().context().traceIdString();
    }
}
```

#### 1.2.3.3. ★★★日志获取traceId  
&emsp; 如果日志文件是有做过格式设置的，可能看不到traceId的输出，可以使用下述的日志格式。  

```xml
<configuration>
    <!-- TraceId:%X{X-B3-TraceId:-} SpanId：%X{X-B3-SpanId:-}-->
    <property name="CONSOLE_LOG_PATTERN"
              value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [traceId= %X{X-B3-TraceId:-}] [SpanId= %X{X-B3-SpanId:-}] %logger{5} - %msg%n"/>

    <appender name="rollingAppender" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/billmanager.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/logs/heuristic-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <!-- 必须指定，否则不会往文件输出内容 -->
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
            <charset>utf8</charset>
        </encoder>
        <append>false</append>
        <prudent>false</prudent>
    </appender>

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
            <charset>utf8</charset>
        </encoder>
    </appender>


    <root level="info">
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="rollingAppender"/>
    </root>
   
</configuration>
```

#### 1.2.3.4. 传递traceId到异步线程池  

1. Sleuth对异步任务是支持的，使用@Async开启一个异步任务后，Sleuth会为这个调用新创建一个Span。  
2. <font color = "clime">如果自定义了异步任务的线程池，会导致无法新创建一个Span，需要使用Sleuth提供的LazyTraceExecutor来包装下。代码如下所示。</font>  

```java
@Configuration
@EnableAutoConfiguration
public class CustomExecutorConfig extends AsyncConfigurerSupport {
    @Autowired
    BeanFactory beanFactory;
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(7);
        executor.setMaxPoolSize(42);
        executor.setQueueCapacity(11);
        executor.setThreadNamePrefix("zhangsan-");
        executor.initialize();
        return new LazyTraceExecutor(this.beanFactory, executor);
    }
}
```
&emsp; 如果直接return executor就不会有新Span，也就不会有save-log这个 Span。如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-21.png)  

#### 1.2.3.5. 子线程或线程池中获取 Zipkin traceId 并打印  
......

#### 1.2.3.6. 监控本地方法  
&emsp; 异步执行和远程调用都会新开启一个Span，如果想监控本地的方法耗时时间，可以采用埋点的方式监控本地方法，也就是开启一个新的Span。代码如下所示。   

```java
@Autowired
Tracer tracer;

@Override
public void saveLog2(String log) {
    ScopedSpan span = tracer.startScopedSpan("saveLog2");
    try {
        Thread.sleep(2000);
    } catch (Exception | Error e) {
        span.error(e);
    } finally {
        span.finish();
    }
}
```
&emsp; 通过手动埋点的方式可以创建新的Span，在 Zipkin的UI中也可以看到这个本地方法执行所消耗的时间，可以看到savelog2花费了2秒的时间，如下图所示。
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-22.png)  
&emsp; 除了使用代码手动创建 Span，还有一种更简单的方式，那就是在方法上加上下面的注解：  

```java
@NewSpan(name = "saveLog2")
```  

#### 1.2.3.7. 计划任务  
......  

#### 1.2.3.8. TracingFilter  
......  

#### 1.2.3.9. 过滤不想跟踪的请求  
......  

#### 1.2.3.10. 用RabbitMq代替Http发送调用链数据  
......

## 1.3. Zipkin
### 1.3.1. 与Zipkin整合  
&emsp; 为了实现对分布式系统做延迟监控等与时间消耗相关等需求，引入了Zipkin。  
&emsp; Zipkin的基础架构主要由4个核心组件构成。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-13.png)  

* Collector: 收集器组件，它主要处理从外部系统发送过来的跟踪信息， 将这些信息转换为Zipkin内部处理的Span格式，以支待后续的存储、分析、展示等功能。  
* Storage: 存储组件，它主要处理收集器接收到的跟踪信息，默认会将这些信息存储在内存中。也可以修改此存储策略，通过使用其他存储组件将跟踪信息存储到数据库中。  
* RESTful API: API组件，它主要用来提供外部访问接口。比如给客户端展示跟踪信息，或是外接系统访问以实现监控等。  
* Web UI: UI组件， 基于API组件实现的上层应用。通过UI组件，用户可以方便而又直观地查询和分析跟踪信息。  

&emsp; Zipkin的设计基于Google Dapper论文。核心术语如下：  
* Trace，Zipkin使用Trace结构表示对一次请求的跟踪，一次请求可能由后台的若干服务负责处理，每个服务的处理是一个Span，Span之间有依赖关系，Trace就是树结构的Span集合；  
* Span，每个服务的处理跟踪是一个Span，可以理解为一个基本的工作单元，包含了一些描述信息：id，parentId，name，timestamp，duration，annotations等，例如：  

    ```json
    {
    "traceId": "bd7a977555f6b982", //标记一次请求的跟踪，相关的Spans都有相同的traceId
    "name": "get-traces", //span的名称，一般是接口方法的名称
    "id": "ebf33e1a81dc6f71", //span id
    "parentId": "bd7a977555f6b982", //可选的id，当前Span的父Span id，通过parentId来保证Span之间的依赖关系，如果没有parentId，表示当前Span为根Span
    "timestamp": 1458702548478000, //Span创建时的时间戳，使用的单位是微秒（而不是毫秒），所有时间戳都有错误，包括主机之间的时钟偏差以及时间服务重新设置时钟的可能性，出于这个原因，Span应尽可能记录其duration
    "duration": 354374, //持续时间使用的单位是微秒（而不是毫秒）
    "annotations": [ //
        {
        "endpoint": {
            "serviceName": "zipkin-query",
            "ipv4": "192.168.1.2",
            "port": 9411
        },
        "timestamp": 1458702548786000,
        "value": "cs"
        }
    ],
    "binaryAnnotations": [ //二进制注释，旨在提供有关RPC的额外信息
        {
        "key": "lc",
        "value": "JDBCSpanStore",
        "endpoint": {
            "serviceName": "zipkin-query",
            "ipv4": "192.168.1.2",
            "port": 9411
        }
        }
    ]
    }
    ```
* Annotation：它用来及时地记录一个事件的存在。对于一个HTTP请求来说， **<font color = "red">在Sleuth中定义了下面四个核心Annotation来标识一个请求的开始和结束：</font>**  
  * cs（Client Send）：<font color = "red">该Annotation用来记录客户端发起了一个请求，同时它也标识了这个HTTP请求的开始。</font>  
  * sr（Server Received）：<font color = "red">该Annotation用来记录服务端接收到了请求，并准备开始处理它。</font><font color = "clime">通过计算sr与cs两个Annotation的时间戳之差，可以得到当前HTTP请求的网络延迟。</font>  
  * ss（Server Send）：该Annotation用来记录服务端处理完请求后准备发送请求响应信息。通过计算ss与sr两个Annotation的时间戳之差，<font color = "clime">可以得到当前服务端处理请求的时间消耗。</font>  
  * cr（Client Received）：该Annotation用来记录客户端接收到服务端的回复，同时它也标识了这个HTTP请求的结束。通过计算cr与cs两个Annotation的时间戳之差，<font color = "clime">可以得到该HTTP请求从客户端发起开始到接收服务端响应的总时间消耗。</font> 
* BinaryAnnotation：它用来对跟踪信息添加一些额外的补充说明，一般以键值对方式出现。比如：在记录HTTP请求接收后执行具体业务逻辑时，此时并没有默认的Annotation来标识该事件状态，但是有BinaryAnnotation信息对其进行补充。  

#### 1.3.1.1. 在Zipkin中图形化展示分布式链接监控数据
&emsp; 访问zipkin服务端http://ip:port ，即可展示微服务链路。  
&emsp; 如果一个服务的调用关系如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-14.png)  
&emsp; 那么此时将Span和Trace在一个系统中使用Zipkin注解的过程图形化：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-15.png)  
&emsp; 每个颜色的表明一个span(总计7个spans，从A到G)，每个span有类似的信息  

```text
Trace Id = X
Span Id = D
Client Sent
```

&emsp; 此span表示span的Trance Id是X，Span Id是D，同时它发送一个Client Sent事件。  
&emsp; spans的parent/child关系图形化如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-16.png)  

**1. spans在zipkin界面的信息解读**  
&emsp; 在Zipkin中展示了上图的跟踪信息，红框里是对上图调用span的跟踪。
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-17.png)  
&emsp; 但是当点击这个trace时，只看到4个span。
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-18.png)  
&emsp; 为什么两个界面显示的span数量不同，一个是7，一个4。
* 1个spans：来自servier1的接口http:/start 被调用，分别是Server Received (SR) 和 Server Sent (SS) annotations。  
* 2个spans：来自service1调用service2的http:/foo接口。service1端有两个span，分别为Client Sent (CS)和Client Received (CR) annotations。service2端也有两个span，分别为Server Received (SR) 和Server Sent (SS) 。物理上有2个span，但是从逻辑上说这个它们组成一个RPC调用的span。  
* 2个span：来自service2调用service3的http:/bar接口，service2端有两个span，分别为Client Sent (CS) 和 Client Received (CR) annotations。service3 端也有两个span，分别为Server Received (SR) 和Server Sent (SS) 。物理上有2个span，但是从逻辑上说它们都是同一个RPC调用的span。  
* 2个span：来自service2调用service4的http:/bar接口，service2端有两个span，分别为Client Sent (CS)和Client Received (CR) annotations。service4端也有两个span，分别为Server Received (SR) and Server Sent (SS) 。物理上有2个span，但是从逻辑上说这个它们都是同一个RPC调用的span。  

&emsp; 所以计算物理spans有7个。  

    1个来自 http:/start 被请求  
    2个来自 service1调用service2  
    2个来自 service2调用service3  
    2个来自 service2调用service4
    
&emsp; 从逻辑上说，只看到4个span。
  
    1个来自service1的接口http:/start 被请求  
    3个来自服务之前的RCP接口调用  
    
**2. Zipkin可视化错误**  
&emsp; 如果调用链路中发生接口调用失败，zipkin会默认使用红色展示信息。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-19.png)  
&emsp; 点击红色的span，可以看到详细的失败信息：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-20.png)  

#### 1.3.1.2. 链路信息收集  
##### 1.3.1.2.1. 消息中间件收集  
&emsp; Spring Cloud Sleuth 在整合 Zipkin 时，不仅实现了以 HTTP 的方式收集跟踪信息，还实现了通过消息中间件来对跟踪信息进行异步收集的封装。 通过结合 Spring Cloud Stream, 可以非常轻松地让应用客户端将跟踪信息输出到消息中间件上， 同时 Zipkin 服务端从消息中间件上异步地消费这些跟踪信息。  

#### 1.3.1.3. 数据持久化  
&emsp; 默认情况下， Zipkin Server会将跟踪信息存储在内存中，每次重启 Zipkin Server都会使之前收集的跟踪信息丢失， 并且当有大量跟踪信息时，内存存储也会成为瓶颈，所以通常情况下需要将跟踪信息对接到外部存储组件中去。  

* 存储在Mysql数据库  
* 存储在ElasticSearch  

#### 1.3.1.4. API接口  
&emsp; Zipkin不仅提供了UI模块让用户可以使用Web页面来方便地查看跟踪信息，它还提供了丰富的RESTful API接口供用户在第三方系统中调用来定制自己的跟踪信息展示或监控。可以在ZipkinServer启动时的控制台或日志中找到Zipkin服务端提供的RESTful API定义  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-32.png)  
&emsp; 可以看到Zipkin Server提供的API接口都以/api/vl路径作为前缀，它们的具体功能整理如下：  

|接口路径|请求方式|接口描述|
|---|---|---|
|/dependencies|GET|用来获取通过收集到的Span分析出的依赖关系|
|/services|GET|用来获取服务列表|
|/spans	|GET|根据服务名来获取所有的Span名|
|/spans|POST|向Zip如nServer上传Span|
|/trace/{traceid}|GET|根据Trace ID获取指定跟踪信息的Span列表|
|/traces/|GET|根据指定条件查询并返回符合条件的trace清单|

### 1.3.2. SpringMVC、dubbo集成zipkin   
<!-- 
springMVC、boot、dubbo集成zipkin做链路追踪
https://www.jianshu.com/p/6f0b7b12893f?spm=a2c6h.13066369.0.0.71523c5at7UtnA&from=timeline&isappinstalled=0
-->