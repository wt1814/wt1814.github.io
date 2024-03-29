
<!-- TOC -->

- [1. Spring Cloud Eureka](#1-spring-cloud-eureka)
    - [1.2. Netflix Eureka](#12-netflix-eureka)
        - [1.2.1. 基础架构](#121-基础架构)
        - [1.2.2. 服务治理机制](#122-服务治理机制)
            - [1.2.2.1. 服务提供者](#1221-服务提供者)
                - [1.2.2.1.1. 服务同步](#12211-服务同步)
                - [1.2.2.1.2. 服务续约](#12212-服务续约)
            - [1.2.2.2. 服务消费者](#1222-服务消费者)
                - [1.2.2.2.1. 荻取服务](#12221-荻取服务)
                - [1.2.2.2.2. 服务调用](#12222-服务调用)
                - [1.2.2.2.3. 服务下线](#12223-服务下线)
            - [1.2.2.3. 服务注册中心](#1223-服务注册中心)
                - [1.2.2.3.1. 失效剔除](#12231-失效剔除)
                - [1.2.2.3.2. ~~自我保护~~](#12232-自我保护)
                    - [1.2.2.3.2.1. 自我保护实现逻辑](#122321-自我保护实现逻辑)
        - [1.2.3. 高可用注册中心/AP模型](#123-高可用注册中心ap模型)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 服务治理框架和产品都围绕着服务注册与服务发现机制来完成对微服务应用实例的自动化管理。  
2. Spring Cloud Eureka，使用Netflix Eureka来实现服务注册与发现，它既包含了服务端组件，也包含了客户端组件。  
3. Eureka服务治理体系包含三个核心角色：服务注册中心、服务提供者以及服务消费者。  
    * 服务提供者  
    &emsp; 服务同步、服务续约。
    * 服务消费者    
    &emsp; 荻取服务、服务调用、服务下线。
    * 服务注册中心  
        * 失效剔除  
        * 自我保护：  
        &emsp; 开启自我保护后，Eureka Server在运行期间，会统计心跳失败的比例在15分钟之内是否低于85%，如果出现低于的情况（在单机调试的时候很容易满足，实际在生产环境上通常是由于网络不稳定导致），Eureka Server会将当前的实例注册信息保护起来，让这些实例不会过期，尽可能保护这些注册信息。  
        &emsp; Eureka Server进入自我保护状态后，客户端很容易拿到实际已经不存在的服务实例，会出现调用失败的清况。  
4. <font color = "red">高可用注册中心/AP模型：EurekaServer的高可用实际上就是将自己作为服务向其他服务注册中心注册自己，这样就可以形成一组互相注册的服务注册中心，以实现服务清单的互相同步，达到高可用的效果。</font>  

# 1. Spring Cloud Eureka
<!-- 

https://www.jianshu.com/p/1a700a283107
https://www.jianshu.com/p/2fa691d4a00a
Eurka 工作流程
 
Eureka工作原理
https://blog.csdn.net/qwe86314/article/details/94552801
 
Eureka源码
https://mp.weixin.qq.com/s/etloMGMydBgC0Ll1yBgx8Q
-->


## 1.2. Netflix Eureka  
### 1.2.1. 基础架构  
&emsp; Spring Cloud Eureka，使用Netflix Eureka来实现服务注册与发现，它既包含了服务端组件，也包含了客户端组件，并且服务端与客户端均采用Java编写。  

* Eureka服务端，也称为服务注册中心。它同其他服务注册中心一样，支持高可用配置。它依托于强一致性提供良好的服务实例可用性，可以应对多种不同的故障场景。 **<font color = "red">如果Eureka以集群模式部署，当集群中有分片出现故障时，那么Eureka就转入自我保护模式。它允许在分片故障期间继续提供服务的发现和注册，当故障分片恢复运行时，集群中的其他分片会把它们的状态再次同步回来。</font>** 以在AWS上的实践为例， Netflix推荐每个可用的区域运行一个Eureka服务端，通过它来形成集群。不同可用区域的服务注册中心通过异步模式互相复制各自的状态，这意味着在任意给定的时间点每个实例关于所有服务的状态是有细微差别的。  
* Eureka客户端，主要处理服务的注册与发现。客户端服务通过注解和参数配置的方式，嵌入在客户端应用程序的代码中，<font color = "red">在应用程序运行时，Eueka客户端向注册中心注册自身提供的服务并周期性地发送心跳来更新它的服务租约。</font>同时，<font color = "red">它也能从服务端查询当前注册的服务信息并把它们缓存到本地并周期性地刷新服务状态。</font>  
![image](http://182.92.69.8:8081/img/microService/SpringCloudNetflix/cloud-35.png)  

&emsp; **Eureka服务治理体系包含三个核心角色：服务注册中心、服务提供者以及服务消费者。**  

* 服务注册中心：Eureka提供的服务端，提供服务注册与发现的功能。  
* 服务提供者：提供服务的应用，可以是Spring Boot应用，也可以是其他技术平台且遵循Eureka通信机制的应用。它将自己提供的服务注册到Eureka, 以供其他应用发现。  
* 服务消费者：消费者应用从服务注册中心获取服务列表，从而使消费者可以知道去何处调用其所需要的服务，可以使用Ribbon来实现服务消费，也可以使用Feign的消费方式。  

### 1.2.2. 服务治理机制  
![image](http://182.92.69.8:8081/img/microService/SpringCloudNetflix/cloud-1.png)  
&emsp; 根据上面的结构，下面详细了解一下，从服务注册开始到服务调用，及各个元素所涉及的一些重要通信行为。  

#### 1.2.2.1. 服务提供者  
&emsp; “服务提供者”在启动的时候会通过发送REST请求的方式将自己注册到Eureka Server上，同时带上了自身服务的一些元数据信息。Eureka Server接收到这个REST请求之后，将元数据信息存储在一个双层结构Map中，其中第一层的key是服务名，第二层的key是具体服务的实例名。  
&emsp; 在服务注册时， 需要确认一下eureka.client.register-with-eureka=true参数是否正确，该值默认为true。若设置为false将不会启动注册操作。  

##### 1.2.2.1.1. 服务同步  
&emsp; 如架构图中所示，这里的两个服务提供者分别注册到了两个不同的服务注册中心上， 也就是说，它们的信息分别被两个服务注册中心所维护。 此时， <font color = "red">由于服务注册中心之间因互相注册为服务，当服务提供者发送注册请求到一个服务注册中心时，它会将该请求转发给集群中相连的其他注册中心，从而实现注册中心之间的服务同步。</font>通过服务同步，两个服务提供者的服务信息就可以通过这两台服务注册中心中的任意一台获取到。  

##### 1.2.2.1.2. 服务续约  
&emsp; 在注册完服务之后，服务提供者会维护一个心跳用来持续告诉Eureka Server: "我还活着”，以防止Eureka Server的“剔除任务”将该服务实例从服务列表中排除出去，该操作为服务续约(Renew)。  
&emsp; 关于服务续约有两个重要属性，可以关注并根据需要来进行调整：  

```
eureka.instance.lease-renewal-interval-in-seconds=30 
eureka.instance.lease-expiration-duration-in-seconds=90
```
&emsp; eureka.instance.lease-renewal-interval-in-seconds  参数用于定义服务续约任务的调用间隔时间，默认为30秒。  
&emsp; eureka.instance.lease-expiration duration-in-seconds参数用于定义服务失效的时间， 默认为90秒。  

#### 1.2.2.2. 服务消费者  
&emsp; **服务发现与消费（服务消费者）**  
&emsp; 服务消费者，它主要完成两个目标，发现服务以及消费服务。其中，服务发现的任务由Eureka的客户端完成，而服务消费的任务由Ribbon完成。Ribbon是一个基于HTTP和TCP的客户端负载均衡器，它可以在通过客户端中配置的ribbonServerList服务端列表去轮询访问以达到均衡负载的作用。当Ribbon与Eureka联合使用时，Ribbon的服务实例清单RibbonServerList会被DiscoveryEnabledNIWSServerList重写，扩展成从Eureka注册中心中获取服务端列表。同时它也会用 NIWSDiscoveryPing来取代IPing，它将职责委托给Eureka来确定服务端是否已经启动。 

##### 1.2.2.2.1. 荻取服务  
&emsp; 此时在服务注册中心已经注册了一个服务，并且该服务有两个实例。 当启动服务消费者的时候，它会发送一个REST请求给服务注册中心，来获取上面注册的服务清单。为了性能考虑，Eureka Server会维护一份只读的服务清单来返回给客户端，同时该缓存清单会每隔30秒更新一次。  
&emsp; 获取服务是服务消费者的基础，所以必须确保eureka.client.fetch-registry=true参数没有被修改成false，该值默认为true。若希望修改缓存清单的更新时间，可以通过eureka.client.registry-fetch-interval-seconds=30参数进行修改，该参数默认值为30，单位为秒。  

##### 1.2.2.2.2. 服务调用  
&emsp; 服务消费者在获取服务清单后，通过服务名可以获得具体提供服务的实例名和该实例的元数据信息。 因为有这些服务实例的详细信息，所以客户端可以根据自己的需要决定具体调用哪个实例，在Ribbon中会默认采用轮询的方式进行调用，从而实现客户端的负载均衡。  
&emsp; 对于访问实例的选择，Eureka中有Region和Zone的概念，一个Region中可以包含多个Zone, 每个服务客户端需要被注册到一个Zone中，所以每个客户端对应一个Region和一个Zone。在进行服务调用的时候，优先访问同处一个Zone中的服务提供方，若访问不到，就访问其他的Zone。  

##### 1.2.2.2.3. 服务下线  
&emsp; 在系统运行过程中必然会面临关闭或重启服务的某个实例的情况，在服务关闭期间，不希望客户端会继续调用关闭了的实例。 所以<font color = "red">在客户端程序中，当服务实例进行正常的关闭操作时，它会触发一个服务下线的REST请求给Eurka Server，告诉服务注册中心：“我要下线了”。服务端在接收到请求之后，将该服务状态置为下线(DOWN)，并把该下线事件传播出去。</font>  

#### 1.2.2.3. 服务注册中心  
##### 1.2.2.3.1. 失效剔除  
&emsp; 有些时候，服务实例并不一定会正常下线，可能由于内存溢出、网络故障等原因使得服务不能正常工作，而服务注册中心并未收到“ 服务下线”的请求。 为了从服务列表中将这些无法提供服务的实例剔除， **<font color = "red">Eureka Server在启动的时候会创建一个定时任务， 默认每隔一段时间（默认为60秒） 将当前清单中超时（默认为90秒）没有续约的服务剔除出去。</font>**  

##### 1.2.2.3.2. ~~自我保护~~
<!-- 
https://www.jb51.net/article/181206.htm
https://www.jianshu.com/p/cb7fa0aa47a8
-->
&emsp; <font color = "clime">注：自我保护是只在服务端开启。</font>   

&emsp; 当在本地调试基于Eureka的程序时， 基本上都会碰到这样一个问题， 在服务注册中心的信息面板中出现类似下面的红色警告信息：  
![image](http://182.92.69.8:8081/img/microService/SpringCloudNetflix/cloud-2.png)  
&emsp; 实际上，该警告就是触发了EurekaServer的自我保护机制。服务注册Eureka Server之后，会维护一个心跳连接，告诉Eureka Server自己还活着。 **<font color = "red">Eureka Server在运行期间，会统计心跳失败的比例在15分钟之内是否低于85%, 如果出现低于的情况</font><font color = "clime">（在单机调试的时候很容易满足，实际在生产环境上通常是由于网络不稳定导致多数服务器心跳失败）</font><font color = "red">，Eureka Server会将当前的实例注册信息保护起来，让这些实例不会过期，尽可能保护这些注册信息。</font>**  

&emsp; **<font color = "clime">Eureka Server进入自我保护状态后</font>，会出现以下几种情况：**  

* Eureka Server不再从注册列表中移除因为长时间没有收到心跳而应该剔除的过期服务。  
* <font color = "clime">Eureka Server仍然能够接受新服务的注册和查询请求，但是不会被同步到其他节点上，保证当前节点依然可用。</font>  
* <font color = "red">在这段保护期间内实例若出现问题，客户端很容易拿到实际已经不存在的服务实例，会出现调用失败的清况</font>，所以客户端必须要有容错机制，比如可以使用请求重试、断路器等机制。

&emsp; 由于本地调试很容易触发注册中心的保护机制，这会使得注册中心维护的服务实例不那么准确。所以，在本地进行开发的时候，可以使用eureka.server.enable-self-preservation = false参数来关闭自我保护机制，以确保注册中心可以将不可用的实例正确剔除。  

###### 1.2.2.3.2.1. 自我保护实现逻辑  
![image](http://182.92.69.8:8081/img/microService/SpringCloudNetflix/cloud-37.png)  
&emsp; 在Eureka的自我保护机制中，有两个很重要的变量，Eureka的自我保护机制，都是围绕这两个变量来实现的，在AbstractInstanceRegistry这个类中定义的  

```java
protected volatile int numberOfRenewsPerMinThreshold; //每分钟最小续约数量 
protected volatile int expectedNumberOfClientsSendingRenews; //预期每分钟收到续约的 客户端数量，取决于注册到eureka server上的服务数量
```
&emsp; numberOfRenewsPerMinThreshold 表示每分钟的最小续约数量，就是Eureka Server期望每分钟收到客户端实例续约的总数的阈值。如果小于这个阈值，就会触发自我保护机制。  

![image](http://182.92.69.8:8081/img/microService/SpringCloudNetflix/cloud-38.png)  

### 1.2.3. 高可用注册中心/AP模型  
&emsp; <font color = "red">EurekaServer的高可用实际上就是将自己作为服务向其他服务注册中心注册自己，这样就可以形成一组互相注册的服务注册中心，以实现服务清单的互相同步，达到高可用的效果。</font>  
![image](http://182.92.69.8:8081/img/microService/SpringCloudNetflix/cloud-36.png)  
