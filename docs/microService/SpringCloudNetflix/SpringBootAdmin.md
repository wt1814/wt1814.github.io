

<!-- TOC -->

- [1. Spring Cloud Admin](#1-spring-cloud-admin)
    - [1.1. 在Spring Cloud中基于Eureka的Spring Boot Admin的搭建](#11-在spring-cloud中基于eureka的spring-boot-admin的搭建)
    - [1.2. actuator原生端点](#12-actuator原生端点)
        - [1.2.1. 度量指标类详解](#121-度量指标类详解)
    - [1.3. 监控通知](#13-监控通知)
    - [1.4. 集成spring security](#14-集成spring-security)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. spring-boot-starter-actuator依赖中已经实现的一些原生端点。根据端点的作用，<font color = "clime">可以将原生端点分为以下三大类：</font>  
    * 应用配置类：获取应用程序中加载的应用配置、环境变量、自动化配置报告等与Spring Boot应用密切相关的配置类信息。  
    * 度量指标类：获取应用程序运行过程中用于监控的度量指标，比如内存信息、线程池信息、HTTP请求统计等。  
    * 操作控制类：提供了对应用的关闭等操作类功能。  
2. 监控通知
3. 集成spring security


# 1. Spring Cloud Admin
<!-- 

基于 Eureka 的 Spring Boot Admin 的搭建
https://blog.csdn.net/A_Story_Donkey/article/details/81483781
https://blog.csdn.net/hubo_88/article/details/80671192
https://blog.csdn.net/yangqinfeng1121/article/details/80951989

使用 SpringBoot Admin 监控你的 SpringBoot 程序
https://mp.weixin.qq.com/s?__biz=MzU2MTI4MjI0MQ==&mid=2247488714&idx=2&sn=377e7280d0e3ee3f7e11df8999335724&chksm=fc7a7964cb0df07219133a551757ea4779001cc0f6fcb3c26e2902042fa5596790f8dd90d9a8&mpshare=1&scene=1&srcid=&sharer_sharetime=1577626683349&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=f774ade767760dde2b290c72a0fcd7be3dbbd74eac3bf2bcbcce473b2437ffc20ae21b92147e8ecbc619e390208162cefb1655546df9a64c50394169925950a113197191a75dd8eb87a404be061750ef&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=AXfDz07%2BiF0Muw12u5lcp90%3D&pass_ticket=IEJj8WR9o9bm52xS1gtlEz9nMuzFB%2FY6%2BZtZBPZob6QGyEKOfqoCURKWrn0iRayw
重要端点：
https://mp.weixin.qq.com/s/bGYO8QRiCDmBsAxw-crMPQ

集成spring security
https://www.cnblogs.com/forezp/p/10242004.html
https://www.jianshu.com/p/d29663c1bddd
-->

## 1.1. 在Spring Cloud中基于Eureka的Spring Boot Admin的搭建  
&emsp; ......

&emsp; Spring Boot Admin依赖了spring-boot-starter-actuator，引入该依赖能够自动为Spring Boot构建的应用提供一系列用于监控的端点。在Spring Boot工程中新增spring-boot-starter-actuator依赖。具体如下：  

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```  
&emsp; 为了保证actuator接口的敏感性，在配置文件中，开放安全属性配置：  

```properties
management.security.enabled=false
```  
&emsp; 重新启动应用。此时，可以在控制台中看到如下图所示的输出：  
![image](http://182.92.69.8:8081/img/microService/SpringCloudNetflix/cloud-26.png)  

## 1.2. actuator原生端点  
&emsp; spring-boot-starter-actuator依赖中已经实现的一些原生端点。根据端点的作用，<font color = "clime">可以将原生端点分为以下三大类：</font>  

* 应用配置类：获取应用程序中加载的应用配置、环境变量、自动化配置报告等与Spring Boot应用密切相关的配置类信息。  
* 度量指标类：获取应用程序运行过程中用于监控的度量指标，比如内存信息、线程池信息、HTTP请求统计等。  
* 操作控制类：提供了对应用的关闭等操作类功能。  

&emsp; 注意：  
1. 每一个端点都可以通过配置来单独禁用或者启动。  
2. 不同于Actuator 1.x，Actuator 2.x 的大多数端点默认被禁掉。Actuator 2.x中的默认端点增加了/actuator前缀。默认暴露的两个端点为/actuator/health和/actuator/info。  

### 1.2.1. 度量指标类详解  
&emsp; 度量指标类端点提供的报告内容则是动态变化的，这些端点提供了应用程序在运行过程中的一些快照信息，比如：内存使用情况、HTTP请求统计、外部资源指标等。  

* <font color = "red">/metrics：该端点用来返回当前应用的各类重要度量指标，比如：内存信息、线程信息、垃圾回收信息等。</font>    

    ```json
    {
    "mem": 541305,
    "mem.free": 317864,
    "processors": 8,
    "instance.uptime": 33376471,
    "uptime": 33385352,
    "systemload.average": -1,
    "heap.committed": 476672,
    "heap.init": 262144,
    "heap.used": 158807,
    "heap": 3701248,
    "nonheap.committed": 65856,
    "nonheap.init": 2496,
    "nonheap.used": 64633,
    "nonheap": 0,
    "threads.peak": 22,
    "threads.daemon": 20,
    "threads.totalStarted": 26,
    "threads": 22,
    "classes": 7669,
    "classes.loaded": 7669,
    "classes.unloaded": 0,
    "gc.ps_scavenge.count": 7,
    "gc.ps_scavenge.time": 118,
    "gc.ps_marksweep.count": 2,
    "gc.ps_marksweep.time": 234,
    "httpsessions.max": -1,
    "httpsessions.active": 0,
    "gauge.response.beans": 55,
    "gauge.response.env": 10,
    "gauge.response.hello": 5,
    "gauge.response.metrics": 4,
    "gauge.response.configprops": 153,
    "gauge.response.star-star": 5,
    "counter.status.200.beans": 1,
    "counter.status.200.metrics": 3,
    "counter.status.200.configprops": 1,
    "counter.status.404.star-star": 2,
    "counter.status.200.hello": 11,
    "counter.status.200.env": 1
    }
    ```
    &emsp; 从上面的示例中，可以看到有这些重要的度量值： 

    ```text
    系统信息：包括处理器数量processors、运行时间uptime和instance.uptime、系统平均负载systemload.average。   
    mem.*：内存概要信息，包括分配给应用的总内存数量以及当前空闲的内存数量。这些信息来自java.lang.Runtime。   
    heap.*：堆内存使用情况。这些信息来自java.lang.management.MemoryMXBean接口中getHeapMemoryUsage方法获取的java.lang.management.MemoryUsage。  
    nonheap.*：非堆内存使用情况。这些信息来自java.lang.management.MemoryMXBean接口中getNonHeapMemoryUsage方法获取的java.lang.management.MemoryUsage。  
    threads.*：线程使用情况，包括线程数、守护线程数（daemon）、线程峰值（peak）等，这些数据均来自java.lang.management.ThreadMXBean。   
    classes.*：应用加载和卸载的类统计。这些数据均来自java.lang.management.ClassLoadingMXBean。   
    gc.*：垃圾收集器的详细信息，包括垃圾回收次数gc.ps_scavenge.count、垃圾回收消耗时间gc.ps_scavenge.time、标记-清除算法的次数gc.ps_marksweep.count、标记-清除算法的消耗时间gc.ps_marksweep.time。这些数据均来自java.lang.management.GarbageCollectorMXBean。  
    httpsessions.*：Tomcat容器的会话使用情况。包括最大会话数httpsessions.max和活跃会话数httpsessions.active。该度量指标信息仅在引入了嵌入式Tomcat作为应用容器的时候才会提供。  
    gauge.*：HTTP请求的性能指标之一，它主要用来反映一个绝对数值。比如上面示例中的gauge.response.hello: 5，它表示上一次hello请求的延迟时间为5毫秒。  
    counter.*：HTTP请求的性能指标之一，它主要作为计数器来使用，记录了增加量和减少量。如上示例中counter.status.200.hello: 11，它代表了hello请求返回200状态的次数为11。  
    ```  
    &emsp; /metrics端点可以提供应用运行状态的完整度量指标报告，这项功能非常的实用，但是对于监控系统中的各项监控功能，它们的监控内容、数据收集频率都有所不同，如果每次都通过全量获取报告的方式来收集，略显粗暴。所以，还可以通过/metrics/{name}接口来更细粒度的获取度量信息，比如可以通过访问/metrics/mem.free来获取当前可用内存数量。  

* <font color = "red">/health：该端点用来获取应用的各类健康指标信息。</font>在spring-boot-starter-actuator模块中自带实现了一些常用资源的健康指标检测器。这些检测器都通过HealthIndicator接口实现，并且会根据依赖关系的引入实现自动化装配，比如用于检测磁盘的DiskSpaceHealthIndicator、检测DataSource连接是否可用的DataSourceHealthIndicator等。  
&emsp; 有时候，可能还会用到一些Spring Boot的Starter POMs中还没有封装的产品来进行开发，比如：当使用RocketMQ作为消息代理时，由于没有自动化配置的检测器，所以需要自己来实现一个用来采集健康信息的检测器。比如，可以在Spring Boot的应用中，为org.springframework.boot.actuate.health.HealthIndicator接口实现一个对RocketMQ的检测器类：  

    ```java
    @Component
    public class RocketMQHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            int errorCode = check();
            if (errorCode != 0) {
            return Health.down().withDetail("Error Code", errorCode).build();
            }
            return Health.up().build();
        }

        private int check() {
        // 对监控对象的检测操作
        }
    }
    ```  
    &emsp; 通过重写health()函数来实现健康检查，返回的Heath对象中，共有两项内容，一个是状态信息，除了该示例中的UP与DOWN之外，还有UNKNOWN和OUT_OF_SERVICE，可以根据需要来实现返回；还有一个详细信息，采用Map的方式存储，在这里通过withDetail函数，注入了一个Error Code信息，也可以填入一下其他信息，比如，检测对象的IP地址、端口等。重新启动应用，并访问/health接口，在返回的JSON字符串中，将会包含了如下信息：  

    ```json
    "rocketMQ": {
        "status": "UP"
    }
    ```  
* **<font color = "red">/heapdump：访问http://localhost:8080/actuator/heapdump 会自动生成一个Jvm的堆文件 heapdump。</font>** 可以使用JDK自带的Jvm监控工具VisualVM打开此文件查看内存快照。  

* **<font color = "red">/dump：该端点用来暴露程序运行中的线程信息。</font>** 它使用java.lang.management.ThreadMXBean的dumpAllThreads方法来返回所有含有同步信息的活动线程详情。  
* /trace：该端点用来返回基本的HTTP跟踪信息。默认情况下，跟踪信息的存储采用org.springframework.boot.actuate.trace.InMemoryTraceRepository实现的内存方式，始终保留最近的100条请求记录。它记录的内容格式如下：  

    ```json
    [
        {
            "timestamp": 1482570022463,
            "info": {
                "method": "GET",
                "path": "/metrics/mem",
                "headers": {
                    "request": {
                        "host": "localhost:8881",
                        "connection": "keep-alive",
                        "cache-control": "no-cache",
                        "user-agent": "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36",
                        "postman-token": "9817ea4d-ad9d-b2fc-7685-9dff1a1bc193",
                        "accept": "*/*",
                        "accept-encoding": "gzip, deflate, sdch",
                        "accept-language": "zh-CN,zh;q=0.8"
                    },
                    "response": {
                        "X-Application-Context": "hello:dev:8881",
                        "Content-Type": "application/json;charset=UTF-8",
                        "Transfer-Encoding": "chunked",
                        "Date": "Sat, 24 Dec 2016 09:00:22 GMT",
                        "status": "200"
                    }
                }
            }
        }
    ]
    ```

## 1.3. 监控通知  
<!-- 
Spring Cloud Admin健康检查 邮件、钉钉群通知 
https://mp.weixin.qq.com/s/9hDOrd5POPgeaf3lbHHWFA

-->
&emsp; Spring Cloud Admin健康检查，发送邮件、钉钉群通知。   


## 1.4. 集成spring security  
&emsp; 引入依赖：  

```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
&emsp; 定义安全校验规则，覆盖Spring Security 的默认配置。  

```java
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 
 */
@Configuration
public class ActuatorSecurityConfig extends WebSecurityConfigurerAdapter {

    /*
     * version1:
     * 1. 限制 '/shutdown'端点的访问，只允许ACTUATOR_ADMIN访问
     * 2. 允许外部访问其他的端点
     * 3. 允许外部访问静态资源
     * 4. 允许外部访问 '/'
     * 5. 其他的访问需要被校验
     * version2:
     * 1. 限制所有端点的访问，只允许ACTUATOR_ADMIN访问
     * 2. 允许外部访问静态资源
     * 3. 允许外部访问 '/'
     * 4. 其他的访问需要被校验
     */

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // version1
/*        http.authorizeRequests()
                .requestMatchers(EndpointRequest.to(ShutdownEndpoint.class))
                .hasRole("ACTUATOR_ADMIN")
                .requestMatchers(EndpointRequest.toAnyEndpoint())
                .permitAll()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .permitAll()
                .antMatchers("/")
                .permitAll()
                .antMatchers("/**")
                .authenticated()
                .and()
                .httpBasic();*/

        // version2
        http.authorizeRequests()
                .requestMatchers(EndpointRequest.toAnyEndpoint())
                .hasRole("ACTUATOR_ADMIN")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .permitAll()
                .antMatchers("/")
                .permitAll()
                .antMatchers("/**")
                .authenticated()
                .and()
                .httpBasic();
    }
}
```  
&emsp; application.properties的相关配置如下：  

```properties
#Spring Security Default user name and password
spring.security.user.name=actuator
spring.security.user.password=actuator
spring.security.user.roles=ACTUATOR_ADMIN
```  
