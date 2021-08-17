
<!-- TOC -->

- [1. Spring Cloud Hytrix](#1-spring-cloud-hytrix)
    - [Hystrix配置说明](#hystrix配置说明)
    - [1.1. 服务雪崩](#11-服务雪崩)
    - [1.2. Hytrix简介](#12-hytrix简介)
    - [1.3. Hystrix原理](#13-hystrix原理)
        - [1.3.1. Hystrix工作流程](#131-hystrix工作流程)
        - [1.3.2. 熔断与降级](#132-熔断与降级)
            - [1.3.2.1. 熔断触发降级(断路器原理)](#1321-熔断触发降级断路器原理)
            - [1.3.2.2. 请求超时触发降级](#1322-请求超时触发降级)
            - [1.3.2.3. 资源隔离触发降级（依赖隔离）](#1323-资源隔离触发降级依赖隔离)
    - [1.4. Hystrix使用教程](#14-hystrix使用教程)
        - [1.4.1. Hystrix配置信息](#141-hystrix配置信息)
        - [1.4.2. 创建请求命令](#142-创建请求命令)
        - [1.4.3. ~~定义服务降级~~](#143-定义服务降级)
        - [1.4.4. 异常处理](#144-异常处理)
            - [1.4.4.1. 异常获取](#1441-异常获取)
        - [1.4.5. 命令名称、分组及线程池划分](#145-命令名称分组及线程池划分)
        - [1.4.6. 请求缓存](#146-请求缓存)
        - [1.4.7. 请求合并](#147-请求合并)
        - [1.4.8. Hystrix仪表盘](#148-hystrix仪表盘)
    - [1.5. Turbine集群监控](#15-turbine集群监控)
        - [1.5.1. 构建监控聚合服务](#151-构建监控聚合服务)
        - [1.5.2. 与消息代理结合](#152-与消息代理结合)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 服务雪崩：在微服务架构中，存在着那么多的服务单元，若一个单元出现故障，就很容易因依赖关系而引发故障的蔓延，最终导致整个系统的瘫痪。  
2. 熔断是一种[降级](/docs/microService/thinking/Demotion.md)策略。Hystrix中的降级方案：熔断触发降级、请求超时触发降级、资源（信号量、线程池）隔离触发降级 / 依赖隔离。  
&emsp; <font color = "clime">熔断的对象是服务之间的请求；`熔断策略有根据请求的数量分为信号量和线程池，还有请求的时间（即超时熔断），请求错误率（即熔断触发降级）。`</font>  
3. Hystrix工作流程：1. 包装请求 ---> 2. 发起请求 ---> 3. 缓存处理 ---> 4. 判断断路器是否打开（熔断） ---> 5. 判断是否进行业务请求（请求是否需要隔离或降级） ---> 6. 执行业务请求 ---> 7. 健康监测 ---> 8. fallback处理或返回成功的响应。  
4. <font color = "clime">微服务集群中，Hystrix的度量信息通过`Turbine`来汇集监控信息，并将聚合后的信息提供给Hystrix Dashboard来集中展示和监控。</font>  

```properties
#开启熔断机制
feign.hystrix.enabled=true
# 设置hystrix超时时间，默认1000ms
hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=60000
# 是否开启超时，默认false，不建议开启
# hystrix.command.default.execution.isolation.thread.interruptOnTimeout=false
# 最大线程数量，默认10，Fast Fail 应用，建议使用默认值。
# hystrix.threadpool.default.coreSize=20
# 允许在队列中的等待的任务数量，默认5，Fast Fail 应用，建议使用默认值。
# hystrix.threadpool.default.queueSizeRejectionThreshold=10

# queueSizeRejectionThreshold默认值是5，允许在队列中的等待的任务数量。maxQueueSize默认值是-1，队列大小。如果是Fast Fail 应用，建议使用默认值。线程池饱满后直接拒绝后续的任务，不再进行等待。即使maxQueueSize没有达到，达到queueSizeRejectionThreshold该值后，请求也会被拒绝。
```

# 1. Spring Cloud Hytrix



## Hystrix配置说明
<!--

https://blog.csdn.net/sinat_35757488/article/details/90765281

-->

```text
    统计滚动的时间窗口 default 10000 ten seconds
    withMetricsRollingStatisticalWindowInMilliseconds(10000)
    滚动时间窗口 bucket 数量 default
    withMetricsRollingStatisticalWindowBuckets(10)
    采样时间间隔 default 500
    withMetricsHealthSnapshotIntervalInMilliseconds(1)
    熔断器在整个统计时间内是否开启的阀值，默认20。也就是10秒钟内至少请求20次，熔断器才发挥起作用
    withCircuitBreakerRequestVolumeThreshold(20)
    默认:50。当出错率超过50%后熔断器启动.
    withCircuitBreakerErrorThresholdPercentage(30)
    熔断器默认工作时间,默认:5秒.熔断器中断请求5秒后会关闭重试,如果请求仍然失败,继续打开熔断器5秒,如此循环
    withCircuitBreakerSleepWindowInMilliseconds(1000)
    隔离策略
    withExecutionIsolationStrategy(ExecutionIsolationStrategy.SEMAPHORE)
    信号量隔离时最大并发请求数
    withExecutionIsolationSemaphoreMaxConcurrentRequests(2)
    命令组名，该命令属于哪一个组，可以帮助我们更好的组织命令。
    withGroupKey(HystrixCommandGroupKey.Factory.asKey(“HelloGroup”))
    命令名称，每个CommandKey代表一个依赖抽象,相同的依赖要使用相同的CommandKey名称。依赖隔离的根本就是对相同CommandKey的依赖做隔离。
    andCommandKey(HystrixCommandKey.Factory.asKey(“Hello”)
    所属线程池的名称，同样配置的命令会共享同一线程池，若不配置，会默认使用GroupKey作为线程池名称。
    andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(“HelloThreadPool”))
    命令属性，设置包括断路器的配置，隔离策略，降级设置，以及一些监控指标等。
    线程池属性，配置包括线程池大小，排队队列的大小等。

hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=1000
超时时间，默认1000ms
execution.timeout.enabled
是否开启超时，默认true
execution.isolation.thread.interruptOnTimeout
当超时的时候是否中断(interrupt) HystrixCommand.run()执行
```



## 1.1. 服务雪崩  
&emsp; 在微服务架构中，将系统拆分成了很多服务单元，各单元的应用间通过服务注册与订阅的方式互相依赖。由于每个单元都在不同的进程中运行，依赖通过远程调用的方式执行，这样就有可能因为网络原因或是依赖服务自身间题出现调用故障或延迟，而这些问题会直接导致调用方的对外服务也出现延迟，若此时调用方的请求不断增加，最后就会因等待出现故障的依赖方响应形成任务积压，最终导致自身服务的瘫痪。  
&emsp; **<font color = "clime">在微服务架构中，存在着那么多的服务单元，若一个单元出现故障，就很容易因依赖关系而引发故障的蔓延，最终导致整个系统的瘫痪，</font>** 这样的架构相较传统架构更加不稳定。为了解决这样的问题，产生了断路器等一系列的服务保护机制。  

## 1.2. Hytrix简介  
&emsp; **<font color = "red">针对服务雪崩，Spring Cloud Hystrix实现了断路器、线程隔离等一系列服务保护功能。</font>** 它也是基于Netflix的开源框架Hystrix实现的，该框架的目标在于通过控制那些访问远程系统、服务和第三方库的节点，从而对延迟和故障提供更强大的容错能力。 **<font color = "red">Hystrix具备服务降级、服务熔断、线程和信号隔离、请求缓存、请求合并以及服务监控等强大功能。</font>**  
 
&emsp; Hystrix设计目标：  

* 对来自依赖的延迟和故障进行防护和控制——这些依赖通常都是通过网络访问的  
* 阻止故障的连锁反应  
* 快速失败并迅速恢复  
* 回退并优雅降级  
* 提供近实时的监控与告警  

## 1.3. Hystrix原理  
### 1.3.1. Hystrix工作流程  
<!-- 
用Hystrix保护自己的应用
https://mp.weixin.qq.com/s/nCifoTiqhBT2Eai2UJinag
-->
&emsp; Hystrix是如何处理请求，在官网有详细介绍：https://github.com/Netflix/Hystrix/wiki/How-it-Works 。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-29.png)  
1. <font color = "red">包装请求：</font>  
&emsp; **<font color = "red">可以使用继承HystrixCommand或HystrixObservableCommand来包装业务方法；</font>**  
2. <font color = "red">发起请求：</font>  
&emsp; 使用调用Command的execute来执行一个业务方法调用；Hystrix除了提供了execute方法，另外还提供了3种方法，所有的请求入口：  

    ```	
    K             value   = command.execute();
    Future<K>     fValue  = command.queue();
    Observable<K> ohValue = command.observe();         //hot observable
    Observable<K> ocValue = command.toObservable();    //cold observable
    ```
    &emsp; 如上图所示：执行同步调用execute方法，会调用`queue().get()`方法，queue()又会调用`toObservable().toBlocking().toFuture()；`所以，所有的方法调用都依赖Observable的方法调用，只是取决于是需要同步还是异步调用；  
3. <font color = "red">缓存处理：</font>  
&emsp; 当请求来到后，会判断请求是否启用了缓存（默认是启用的），再判断当前请求是否携带了缓存Key；  
&emsp; 如果命中缓存就直接返回；否则进入剩下的逻辑；  
4. <font color = "red">判断断路器是否打开（熔断）：</font>  
&emsp; 断路器是Hystrix的设计核心，断路器是实现快速失败的重要手段（断路器打开就直接返回失败）；  
&emsp; 可以设置断路器打开一定时间后，可以进行尝试进行业务请求（默认是5000毫秒）；  
5. <font color = "red">判断是否进行业务请求（请求是否需要隔离或降级）：</font>  
&emsp; 是否进行业务请求之前还会根据当前服务处理质量，判断是否需要去请求业务服务；  
&emsp; 如果当前服务质量较低（线程池/队列/信号量已满），那么也会直接失败；  
&emsp; 线程池或信号量的选择（默认是线程池）。  
6. <font color = "red">执行业务请求：</font>  
&emsp; 当前服务质量较好，那么就会提交请求到业务服务器去；  
&emsp; HystrixObservableCommand.construct() or HystrixCommand.run()  
7. <font color = "red">健康监测：</font>  
&emsp; 根据历史的业务方法执行结果，来统计当前的服务健康指标，为断路器是否熔断等动作作为依据；　　
8. <font color = "red">fallback处理</font>  
9. <font color = "red">返回成功的响应</font>  

### 1.3.2. 熔断与降级  
&emsp; 熔断是一种[降级](/docs/microService/thinking/Demotion.md)策略。Hystrix中有三种降级方案(fallback，回退方案/降级处理方案)。

#### 1.3.2.1. 熔断触发降级(断路器原理)
<!-- 
https://mp.weixin.qq.com/s/mVQUek3m7F-9rpqpHIGrGA
-->

```java
@HystrixCommand(commandProperties = {
        @HystrixProperty(name="circuitBreaker.enabled",value ="true"),
        @HystrixProperty(name="circuitBreaker.requestVolumeThreshold",value = "5"),
        @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value = "5000"),
        @HystrixProperty(name="circuitBreaker.errorThresholdPercentage",value = "50")
},fallbackMethod = "fallback",
groupKey = "",threadPoolKey = "order-service")
@GetMapping("/hystrix/order/{num}")
public String queryOrder(@PathVariable("num")int num){
    if(num%2==0){
        return "正常访问";
    }
    //restTemplate默认有一个请求超时时间
    return  restTemplate.getForObject("http://localhost:8082/orders",String.class);
}
```
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-3.png)  
&emsp; <font color = "red">熔断器开关由关闭到打开的状态转换是通过当前服务健康状况（服务的健康状况 = 请求失败数 / 请求总数 ）和设定阈值比较决定的。</font>熔断器模式定义了熔断器开关相互转换的逻辑：  
1. 当熔断器开关关闭时，请求被允许通过熔断器。如果当前健康状况高于设定阈值，开关继续保持关闭。如果当前健康状况低于设定阈值，开关则切换为打开状态。  

        如何触发熔断？"判断阈值"
        10s钟之内，发起了20次请求，失败率超过50%。 熔断的恢复时间（熔断5s），从熔断开启到后续5s之内的请求，都不会发起到远程服务端。
  
2. 当熔断器开关打开时, 请求被禁止通过。  
3. 当熔断器开关处于打开状态，经过一段时间后（默认5s），熔断器会自动进入半开状态，这时熔断器允许有且仅一个请求通过。当请求调用成功时，熔断器恢复到关闭状态。若该请求失败，熔断器继续保持打开状态, 接下来的请求被禁止通过。  

&emsp; **<font color = "red">熔断器的开关能保证服务调用者在调用异常服务时，快速返回结果，避免大量的同步等待。</font>** 并且熔断器能在一段时间后继续侦测请求执行结果, 提供恢复服务调用的可能。  

#### 1.3.2.2. 请求超时触发降级  

```java
@HystrixCommand(fallbackMethod="timeoutFallback",
        commandProperties={@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value ="3000"),
})
@GetMapping("/hystrix/timeout")
publicStringqueryOrderTimeout(){
      return  restTemplate.getForObject("http://localhost:8082/orders",String.class);
}
```

#### 1.3.2.3. 资源隔离触发降级（依赖隔离）  
&emsp; Hystrix提供了两种资源隔离方式：线程池隔离和信号量隔离。 
 
* 线程池隔离：  
    * 未使用线程池隔离：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-9.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-10.png)  
    * 使用线程池隔离：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-11.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-12.png)  

```java
@HystrixCommand(groupKey="order-service",
    commandKey="queryOrder",
    threadPoolKey="order-service",
    threadPoolProperties={
        @HystrixProperty(name="coreSize",value="30"),//线程池大小          
        @HystrixProperty(name="maxQueueSize",value="100"),//最大队列长度
        @HystrixProperty(name= "keepAliveTimeMinutes",value="2"),//线程存活时间
        @HystrixProperty(name="queueSizeRejectionThreshold",value ="15")//拒绝请求
    },
    fallbackMethod="fallback")
```

* 信号量隔离：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-8.png)  

```java
@HystrixCommand(fallbackMethod="semaphoreQuarantineFallback",
    commandProperties={
        @HystrixProperty(
        name=HystrixPropertiesManager.EXECUTION_ISOLATION_STRATEGY, value="SEMAPHORE"),//信号量隔离
        @HystrixProperty(name=HystrixPropertiesManager.EXECUTION_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQU ESTS, value="100")//信号量最大并发数
    })
```

&emsp; 线程池隔离和信号量隔离的区别：  

|隔离方式	|隔离原理	|是否支持超时	|是否支持熔断	|是否是异步调用	|资源消耗|
|---|---|---|---|---|---|
|线程池隔离	|每个服务单独用线程池	|支持，可直接返回	|支持，当线程池到达maxSize后，再请求会触发fallback接口进行熔断	|可以是异步，也可以是同步。看调用的方法	|大，大量线程的上下文切换，容易造成机器负载高|
|信号量隔离	|通过信号量的计数器	|不支持，如果阻塞，只能通过调用协议（如：socket）超时才能返回	|支持，当信号量达到maxConcurrentRequests后。再请求会触发fallback	|同步调用，不支持异步|小，只是个计数器|

* 线程池主要优势是客户端隔离和超时设置，但是如果是海量低延迟请求时，频繁的线程切换带来的损耗也是很可观的，这种情况就可以使用信号量的策略；  
* 信号量的主要缺点就是不能处理超时，请求发送到客户端后，如果被客户端pending住，那么就需要一直等待；  

    
&emsp; **<font color = "red">信号量的开销比线程池的开销小，但是它不能设置超时和实现异步访问。所以只有在依赖服务是足够可靠的情况下才使用信号量。</font>**   

## 1.4. Hystrix使用教程  
&emsp; 参考<font color = "red">**《Spring Cloud微服务实战》** </font>  

&emsp; 可以使用Hystrix中的核心注解@HystrixCommand，通过它创建HystrixCommand的实现。同时利用fallback属性指定服务降级的实现方法。  
&emsp; 然而这些还只是Hystrix使用的一小部分，在实现一个大型分布式系统时，往往还需要更多高级的配置功能。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-41.png)  


### 1.4.1. Hystrix配置信息  

* command属性  
* collapser属性  
* threadPool属性  

```yaml
hystrix:
  command:
    default:
      execution:
        isolation:
          #熔断隔离策略为信号量隔离 SEMAPHORE 或者线程池隔离 THREAD，默认是THREAD，建议公共配置设置为THREAD。
          strategy: THREAD
          thread:
            #命令执行超时时间，毫秒，命令执行超时将会执行fallback,默认1000，建议设置值大于ribbon超时设置中的(ribbonReadTimeout + ribbonConnectTimeout) * (maxAutoRetries + 1) * (maxAutoRetriesNextServer + 1)。
            timeoutInMilliseconds: xxx 
            #超时是否中断HystrixCommand.run方法的执行，默认为true
            interruptOnTimeout: true
        timneout:
          #是否启用命令执行超时，默认为true
          enabled: true
    ClassOne#method(param): # 具体的接口配置
      execution:
        isolation:
          strategy: SEMAPHORE
          semaphore:
            maxConcurrentRequests: 10
    ClassTwo#method(param): # 具体的接口配置
      execution:
        isolation:
          strategy: THREAD
  threadpool:
    default:
      #设置并发最大的核心线程数，默认值为10。
      coreSize: 10
```


### 1.4.2. 创建请求命令  
&emsp; Hystrix命令是指HystrixCommand，它可以用来封装具体的依赖服务调用逻辑。  

### 1.4.3. ~~定义服务降级~~  
&emsp; fallback 是Hystrix 命令执行失败时使用的后备方法， 用来实现服务的降级处理逻辑。  
&emsp; 若熔断方法实现的并不是一个稳定逻辑，它依然可能会发生异常， 那么也可以为它添加@HystrixCommand注解以生成 Hystrix 命令， 同时使用 fallbackMethod来指定服务降级逻辑。  

### 1.4.4. 异常处理  

#### 1.4.4.1. 异常获取  
&emsp; 注解配置方式可以实现异常的获取。它的实现也非常简单，只需要在fallback实现方法的参数中增加Throwable e对象的定义，这样在方法内部就可以获取触发服务降级的具体异常内容了。  


### 1.4.5. 命令名称、分组及线程池划分  
&emsp; 通过设置命令组，Hystix会根据组来组织和统计命令的告警、仪表盘等信息。  
&emsp; 如果Hystrix的线程池分配仅仅依靠命令组来划分，那么它就显得不够灵活了，所以Hystrix还提供了HystrixThreadPoolKey来对线程池进行设置，通过它可以实现更细粒度的线程池划分。  

### 1.4.6. 请求缓存  
&emsp; 在高并发的场景之下，Hystrix中提供了请求缓存的功能，可以方便地开启和使用请求缓存来优化系统，达到减轻高并发时的请求线程消耗、 降低请求响应时间的效果。  

### 1.4.7. 请求合并  
&emsp; 微服务架构中的依赖通常通过远程调用实现，而远程调用中最常见的问题就是通信消耗与连接数占用。在高并发的情况之下，因通信次数的增加，总的通信时间消耗将会变得不那么理想。同时，因为依赖服务的线程池资源有限，将出现排队等待与响应延迟的清况。为了优化这两个问题，Hystrix提供了HystrixCollapser来实现请求的合并，以减少通信消耗和线程数的占用。  
&emsp; HystrixCollapser实现了在HystrixCommand之前放置一个合并处理器，将处于一个很短的时间窗（默认10毫秒）内对同一依赖服务的多个请求进行整合并以批量方式发起请求的功能（服务提供方也需要提供相应的批量实现接口）。通过HystrixCollapser的封装，开发者不需要关注线程合并的细节过程，只需关注批量化服务和处理。  


### 1.4.8. Hystrix仪表盘  
&emsp; Hystrix Dashboard主要用来实时监控Hystrix的各项指标信息。Hystrix的数据统计是采用的滑动窗口。  
&emsp; 访问Hystrix仪表盘地址：http://ip:port/hystrix  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-5.png)  
&emsp; Hystrix Dashboard共支持三种不同的监控方式， 如下所示。

* 默认的集群监控：通过URL http://turbine-hostname:port/turbine.stream 开启，实现对默认集群的监控。  
* 指定的集群监控：通过URL http://turbine-hostname:port/turbine.strearn?cluster= [clusterName] 开启，实现对clusterName集群的监控。  
* 单体应用的监控：通过URL http://hystrix-app:port/hystrix.stream 开启，实现对具体某个服务实例的监控。  

&emsp; 前两者都是对集群的监控，需要整合Turbine才能实现。  

&emsp; 仪表盘其余两个参数：  

* Delay：该参数用来控制服务器上轮询监控信息的延迟时间，默认为2000毫秒，可以通过配置该属性来降低客户端的网络和CPU消耗。  
* Title：该参数对应了上图头部标题Hystrix Stream之后的内容，默认会使用具体监控实例的URL，可以通过配置该信息来展示更合适的标题。  

&emsp; 输入URL，单击单击Monitor Stream按钮。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-6.png)  

-----
## 1.5. Turbine集群监控  
### 1.5.1. 构建监控聚合服务  
&emsp; 微服务集群中，Hystrix的度量信息通过Turbine来汇集监控信息，并将聚合后的信息提供给Hystrix Dashboard来集中展示和监控。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-7.png)  

### 1.5.2. 与消息代理结合  
&emsp; Spring Cloud在封装Turbine的时候，还封装了基于消息代理的收集实现。所以，可以将所有需要收集的监控信息都输出到消息代理中，然后Turbine服务再从消息代理中异步获取这些监控信息，最后将这些监控信息聚合并输出到Hystrix Dashboard中。通过引入消息代理。  
