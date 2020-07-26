

<!-- TOC -->

- [1. Dubbo使用教程](#1-dubbo使用教程)
    - [1.1. Dubbo标签的使用](#11-dubbo标签的使用)
    - [1.2. 启动时检查](#12-启动时检查)
    - [1.3. 延迟暴露](#13-延迟暴露)
    - [1.4. 并发控制](#14-并发控制)
    - [1.5. 连接控制](#15-连接控制)
    - [1.6. 延迟连接](#16-延迟连接)
    - [1.7. 令牌验证](#17-令牌验证)
    - [1.8. 日志适配](#18-日志适配)
    - [1.9. Dubbo缓存文件](#19-dubbo缓存文件)
    - [1.10. 多协议](#110-多协议)
    - [1.11. Dubbo注册中心](#111-dubbo注册中心)
        - [1.11.1. 服务注册中心zookeeper的配置](#1111-服务注册中心zookeeper的配置)
        - [1.11.2. 多注册中心](#1112-多注册中心)
    - [1.12. 直连提供者](#112-直连提供者)
    - [1.13. 只订阅](#113-只订阅)
    - [1.14. 只注册](#114-只注册)
    - [1.15. 回声测试](#115-回声测试)
    - [1.16. 静态服务](#116-静态服务)
    - [1.17. 服务分组](#117-服务分组)
    - [1.18. 多版本](#118-多版本)
    - [1.19. 分组聚合](#119-分组聚合)
    - [1.20. 线程模型](#120-线程模型)
    - [1.21. 上下文信息](#121-上下文信息)
    - [1.22. 异步调用](#122-异步调用)
    - [1.23. 参数回调](#123-参数回调)
    - [1.24. 事件通知](#124-事件通知)
    - [1.25. 本地存根](#125-本地存根)
    - [1.26. 本地伪装](#126-本地伪装)
    - [1.27. 超时时间设置](#127-超时时间设置)
    - [1.28. 集群容错](#128-集群容错)
    - [1.29. 集群的负载均衡策略](#129-集群的负载均衡策略)
    - [1.30. 优雅停机](#130-优雅停机)

<!-- /TOC -->
# 1. Dubbo使用教程  
## 1.1. Dubbo标签的使用  
&emsp; Dubbo主要配置项：配置应用信息、配置注册中心相关信息、配置服务协议、配置所有暴露服务缺省值、配置暴露服务、配置所有引用服务缺省值、配置引用服务。  

    <dubbo:application/> 	应用配置，用于配置当前应用信息，不管该应用是提供者还是消费者。
    <dubbo:protocol/> 	协议配置，用于配置提供服务的协议信息，协议由提供方指定，消费方被动接受。
    <dubbo:registry/>	注册中心配置，用于配置连接注册中心相关信息。
    <dubbo:monitor/>	监控中心配置，用于配置连接监控中心相关信息，可选。
    <dubbo:service/>	服务配置，用于暴露一个服务，定义服务的元信息，一个服务可以用多个协议暴露，一个服务也可以注册到多个注册中心。
    <dubbo:reference/>	引用配置，用于创建一个远程服务代理，一个引用可以指向多个注册中心。（check值默认为true，启动时会检查引用的服务是否已存在，不存在时报错）
    <dubbo:module/>	        模块配置，用于配置当前模块信息，可选。
    <dubbo:provider/> 	提供方的缺省值，当ProtocolConfig和ServiceConfig某属性没有配置时，采用此缺省值，可选。
    <dubbo:consumer/>	消费方缺省配置，当ReferenceConfig某属性没有配置时，采用此缺省值，可选。
    <dubbo:method/>	        方法配置，用于ServiceConfig和ReferenceConfig指定方法级的配置信息。
    <dubbo:argument/>	参数配置，用于指定方法参数配置。

&emsp; 方法级 > 接口级 > 全局配置，级别相同，则消费方优先；   

-----------

## 1.2. 启动时检查  
&emsp; Dubbo缺省会在启动时检查依赖的服务是否可用，不可用时会抛出异常，阻止Spring初始化完成，默认check=”true”，懒加载或通过API编程延迟引用服务时可check。  

* 关闭某个服务的启动时检查(没有提供者时报错)：  

        <dubbo:reference interface="com.foo.BarService" check="false" />
    
* 关闭所有服务的启动时检查(没有提供者时报错)：  

        <dubbo:consumer check="false" />
    
* 关闭注册中心启动时检查(注册订阅失败时报错)：  

        <dubbo:registry check="false" />

## 1.3. 延迟暴露  
&emsp; 如果服务需要预热时间，比如初始化缓存，等待相关资源就位等，可以使用 delay进行延迟暴露。  

* 延迟5秒暴露服务

        <dubbo:service delay="5000" />  
* 延迟到Spring初始化完成后，再暴露服务

        <dubbo:service delay="-1" />  

## 1.4. 并发控制  

* 限制com.foo.BarService的每个方法，服务器端并发执行（或占用线程池线程数）不能超过10个。  

        <dubbo:service interface="com.foo.BarService" executes="10" />
* 限制com.foo.BarService的sayHello方法，服务器端并发执行（或占用线程池线程数）不能超过10个。  

        <dubbo:service interface="com.foo.BarService">
            <dubbo:method name="sayHello" executes="10" />
        </dubbo:service>
* 限制com.foo.BarService 的每个方法，每客户端并发执行（或占用连接的请求数）不能超过10个。  

        <dubbo:service interface="com.foo.BarService" actives="10" />  

    &emsp; 或  
    
        <dubbo:reference interface="com.foo.BarService" actives="10" />
*  限制com.foo.BarService的sayHello方法，每客户端并发执行（或占用连接的请求数）不能超过10个。  

        <dubbo:service interface="com.foo.BarService">
            <dubbo:method name="sayHello" actives="10" />
        </dubbo:service>    

    &emsp; 或  
    
        <dubbo:reference interface="com.foo.BarService">
            <dubbo:method name="sayHello" actives="10" />
        </dubbo:service>  

## 1.5. 连接控制  
* 限制服务器端接受的连接不能超过10个。    

        <dubbo:provider protocol="dubbo" accepts="10" />    

    &emsp; 或  

        <dubbo:protocol name="dubbo" accepts="10" />
* 限制客户端服务使用连接不能超过10个。  

        <dubbo:reference interface="com.foo.BarService" connections="10" />    

    &emsp; 或  

        <dubbo:service interface="com.foo.BarService" connections="10" />  

## 1.6. 延迟连接
&emsp; 延迟连接用于减少长连接数。当有调用发起时，再创建长连接。  

    <dubbo:protocol name="dubbo" lazy="true" />  

## 1.7. 令牌验证
&emsp; 防止消费者绕过注册中心访问提供者，在注册中心控制权限，以决定要不要下发令牌给消费者，注册中心可灵活改变授权方式，而不需修改或升级提供者。  

1. 全局设置开启令牌验证：

        <!--随机token令牌，使用UUID生成-->
        <dubbo:provider interface="com.foo.BarService" token="true" />
        <!--固定token令牌，相当于密码-->
        <dubbo:provider interface="com.foo.BarService" token="123456" />  

2. 服务级别设置开启令牌验证：  

        <!--随机token令牌，使用UUID生成-->
        <dubbo:service interface="com.foo.BarService" token="true" />
        <!--固定token令牌，相当于密码-->
        <dubbo:service interface="com.foo.BarService" token="123456" />  

3. 协议级别设置开启令牌验证：  

        <!--随机token令牌，使用UUID生成-->
        <dubbo:protocol name="dubbo" token="true" />
        <!--固定token令牌，相当于密码-->
        <dubbo:protocol name="dubbo" token="123456" />  

## 1.8. 日志适配 
&emsp; 缺省自动查找：log4j、slf4j、jcl、jdk。可以通过以下方式配置日志输出策略：<dubbo:application logger="log4j"/>  
&emsp; 访问日志：如果你想记录每一次请求信息，可开启访问日志，类似于apache的访问日志。此日志量比较大，请注意磁盘容量。  
&emsp; 将访问日志输出到当前应用的log4j日志：  

    <dubbo:protocol accesslog="true" />
    
&emsp; 将访问日志输出到指定文件：  

    <dubbo:protocol accesslog="http://10.20.160.198/wiki/display/dubbo/foo/bar.log" />

## 1.9. Dubbo缓存文件  
&emsp; 配置方法如下：  

    <dubbo:registryfile=”${user.home}/output/dubbo.cache”/>
    
&emsp; 注意：文件的路径，应用可以根据需要调整，保证这个文件不会在发布过程中被清除。如果有多个应用进程注意不要使用同一个文件，避免内容被覆盖。  
&emsp; 这个文件会缓存：注册中心的列表、服务提供者列表。  
&emsp; 配置后，当应用重启过程中，Dubbo注册中心不可用时，则应用会从这个缓存文件读取服务提供者列表的信息，进一步保证应用可靠性。  

---------
## 1.10. 多协议  
&emsp; Dubbo允许配置多协议。有以下2种方式：  
1. 不同服务使用不同协议。不同的服务在性能上使用不同协议进行传输，比如大数据采用短连接协议，小数据大并发使用长连接协议。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-5.png)  

2. 同一服务上同时支持多种协议，需要与客户端互操作。  

        <!-- 服务提供方 -->
        <!--多协议配置-->
        <dubbo:protocol name="dubbo" port="20880"/>
        <dubbo:protocol name="rmi" port="1099"/>
        <!--使用dubbo协议暴露服务-->
        <dubbo:service interface="com.xxx.HelloService" ref="helloService" portocol="dubbo"/>
        <dubbo:service interface="com.xxx.DemoService" ref="demoService" portocol="rmi"/>

        <!-- 服务消费方 -->
        <!--多协议配置-->
        <dubbo:protocol name="dubbo" port="20880"/>
        <dubbo:protocol name="rmi" port="1099"/>
        <!--使用多个协议暴露服务-->
        <dubbo:reference  interface="com.xxx.DemoService" id="demoService" portocol="dubbo,rmi"/>  

## 1.11. Dubbo注册中心 
&emsp; Multicast注册中心，Multicast注册中心不需要任何中心节点，只要广播地址，就能进行服务注册和发现。基于网络中组播传输实现；  
&emsp; Zookeeper注册中心，基于分布式协调系统Zookeeper实现，采用Zookeeper的Watch机制实现数据变更；  
&emsp; redis注册中心，基于redis实现，采用key/Map存储，往key存储服务名和类型，Map中key存储服务URL，value服务过期时间。基于redis的发布/订阅模式通知数据变更；  
&emsp; Simple注册中心。   

### 1.11.1. 服务注册中心zookeeper的配置
* Zookeeper单机配置：
    * 方式一：  

            <dubbo:registry address="zookeeper://10.20.153.10:2181"/>
    * 方式二：  

            <dubbo:registry protocol="zookeeper" address="10.20.153.10:2181"/>
* Zookeeper集群配置:
    * 方式一：

            <dubbo:registry address="zookeeper://10.20.153.10:2181?backup=10.20.153.11:2181,10.20.153.12:2181"/>
    * 方式二：

            <dubbo:registry protocol="zookeeper" address="10.20.153.10:2181,10.20.153.11:2181,10.20.153.12/>
            
    &emsp; 其中集群配置方式一特别适用于dubbo-admin和dubbo-monitor。  

### 1.11.2. 多注册中心
&emsp; Dubbo支持同一服务向多注册中心同时注册，或者不同服务分别注册到不同的注册中心上去，甚至可以同时引用注册在不同注册中心上的同名服务。另外，注册中心是支持自定义扩展的。  
* 多注册中心注册  

        <!--  多注册中心配置 -->
        <dubbo:registry id="hangzhouRegistry" address="10.20.141.150:9090" />
        <dubbo:registry id="qingdaoRegistry" address="10.20.141.151:9010" default="false" />
        <!--  向多个注册中心注册  -->
        <dubbo:service interface="com.alibaba.hello.api.HelloService" version="1.0.0" ref="helloService" registry="hangzhouRegistry,qingdaoRegistry" />

* 多注册中心引用  

        <!--  多注册中心配置 -->
        <dubbo:registry id="chinaRegistry" address="10.20.141.150:9090" />
        <dubbo:registry id="intlRegistry" address="10.20.154.177:9010" default="false" />
        <!--  引用中文站服务 -->
        <dubbo:reference id="chinaHelloService" interface="com.alibaba.hello.api.HelloServ ice" version="1.0.0" registry="chinaRegistry" />           
        <!--   引用国际站站服务    -->
        <dubbo:reference id="intlHelloService" interface="com.alibaba.hello.api.HelloServi ce" version="1.0.0" registry="intlRegistry" />
        <!--    多注册中心配置，竖号分隔表示同时连接多个不同注册中心，同一注册中心的多个集群地址用逗号分隔   -->
        <dubbo:registry address="10.20.141.150:9090|10.20.154.177:9010" />
        <!--    引用服务  -->
        <dubbo:reference id="helloService" interface="com.alibaba.hello.api.HelloService" version="1.0.0" /> 

---------

## 1.12. 直连提供者
&emsp; 直连提供者，绕过注册中心，只测试指定服务提供者（只应在测试阶段使用）。  

    <dubbo:reference id="xxxService" interface="com.alibaba.xxx.XxxService" url="dubbo://localhost:20890" />  

## 1.13. 只订阅
&emsp; 为方便开发测试，经常会在线下共用一个所有服务可用的注册中心，让服务提供者开发方，只订阅服务(开发的服务可能依赖其它服务)，而不注册正在开发的服务，通过直连测试正在开发的服务。  
1. “只订阅”指的是需要做开发调试的服务提供者，只向注册中心订阅其所依赖的服务，但不向注册中心注册其本身可以提供的服务。  
2. “只订阅”需要结合“直连提供者”配置来进行调用测试。  
&emsp; 禁用注册配置：  

        <dubbo:registry address="10.20.153.10:9090" register="false" />

    &emsp; 或  

        <dubbo:registry address="10.20.153.10:9090?register=false" />  

## 1.14. 只注册  
&emsp; 如果有两个镜像环境，两个注册中心，有一个服务只在其中一个注册中心有部署，另一个注册中心还没来得及部署，而两个注册中心的其它应用都需要依赖此服务。这个时候，可以让服务提供者方只注册服务到另一注册中心，而不从另一注册中心订阅服务。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-6.png)  

&emsp; 禁用订阅配置：  

    <dubbo:registry id="hzRegistry" address="10.20.153.10:9090" />
    <dubbo:registry id="qdRegistry" address="10.20.141.150:9090" subscribe="false" />

&emsp; 或  

    <dubbo:registry id="hzRegistry" address="10.20.153.10:9090" />
    <dubbo:registry id="qdRegistry" address="10.20.141.150:9090?subscribe=false" />  

## 1.15. 回声测试  
&emsp; 回声测试用于检测服务是否可用，回声测试按照正常请求流程执行，能够测试整个调用是否通畅，可用于监控。  
&emsp; 所有服务自动实现 EchoService 接口，只需将任意服务引用强制转型为 EchoService，即可使用。  

    <dubbo:reference id="memberService" interface="com.xxx.MemberService" />

```java
// 远程服务引用 MemberService 
memberService = ctx.getBean("memberService");
// 强制转型为EchoService
EchoService echoService = (EchoService) memberService; 
// 回声测试可用性 
String status = echoService.$echo("OK");
assert(status.equals("OK"));
```

-------
## 1.16. 静态服务  
&emsp; 人工管理服务提供者的上线和下线，将注册中心标识为非动态管理模式。  

    <dubbo:registry address="10.20.141.150:9090" dynamic="false" />
    
&emsp; 或  

    <dubbo:registry address="10.20.141.150:9090?dynamic=false" />  

&emsp; 服务提供者初次注册时为禁用状态，需人工启用。断线时，将不会被自动删除，需人工禁用。  

## 1.17. 服务分组  
&emsp; 当一个接口有多种实现时，可以用group区分。  
&emsp; 服务  

    <dubbo:service group="feedback" interface="com.xxx.IndexService" />
    <dubbo:service group="member" interface="com.xxx.IndexService" />
    
&emsp; 引用  

    <dubbo:reference id="feedbackIndexService" group="feedback" interface="com.xxx.IndexService" />
    <dubbo:reference id="memberIndexService" group="member" interface="com.xxx.IndewxService" />
    <!-- 任意组（总是只调一个可用组的实现） -->
    <dubbo:reference id="barService" interface="com.foo.BarService" group="*" />  

## 1.18. 多版本
&emsp; 当一个接口实现，出现不兼容升级时，可以用版本号过渡，版本号不同的服务相互间不引用。可以按照以下的步骤进行版本迁移：
在低压力时间段，先升级一半提供者为新版本，再将所有消费者升级为新版本，然后将剩下的一半提供者升级为新版本。  

    <dubbo:service  interface="com.foo.BarService"  version="2.0.0" />
    <dubbo:reference id="barService" interface="com.foo.BarService" version="2.0.0" />
    <!-- 不区分版本 -->
    <dubbo:reference id="barService" interface="com.foo.BarService" version="*" />  

## 1.19. 分组聚合  
&emsp; 消费方需从每种group中调用一次返回结果，合并结果返回，实现聚合菜单项。  
&emsp; 搜索所有分组：  

    <dubbo:reference interface="com.xxx.MenuService"    group="*" merger="true" />
    
&emsp; 合并指定分组：  

    <dubbo:reference interface="com.xxx.MenuService" group="aaa,bbb" merger="true" />
    
&emsp; 指定方法合并结果，其它未指定的方法，将只调用一个Group。  

    <dubbo:reference interface="com.xxx.MenuService" group="*">
        <dubbo:method name="getMenuItems" merger="true" />
    </dubbo:service>
    
&emsp; 某个方法不合并结果，其它都合并结果。  

    <dubbo:reference interface="com.xxx.MenuService" group="*" merger="true">
        <dubbo:method name="getMenuItems" merger="false" />
    </dubbo:service>
    
&emsp; 指定合并策略，缺省根据返回值类型自动匹配，如果同一类型有两个合并器时，需指定合并器的名称。  

    <dubbo:reference interface="com.xxx.MenuService" group="*">
        <dubbo:method name="getMenuItems" merger="mymerge" />
    </dubbo:service>
    
&emsp; 指定合并方法，将调用返回结果的指定方法进行合并，合并方法的参数类型必须是返回结果类型本身。  

    <dubbo:reference interface="com.xxx.MenuService" group="*">
        <dubbo:method name="getMenuItems" merger=".addAll" />
    </dubbo:service>  

------
## 1.20. 线程模型  
&emsp; 如果事件处理的逻辑能迅速完成，并且不会发起新的IO请求，比如只是在内存中记个标识，则直接在IO线程上处理更快，因为减少了线程池调度。  
&emsp; 但如果事件处理逻辑较慢，或者需要发起新的IO请求，比如需要查询数据库，则必须派发到线程池，否则IO线程阻塞，将导致不能接收其它请求。  
&emsp; 如果用IO线程处理事件，又在事件处理过程中发起新的IO请求，比如在连接事件中发起登录请求，会报“可能引发死锁”异常，但不会真死锁。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-7.png)   

&emsp; 因此需要通过不同的派发策略和不同的线程池配置的组合来应对不同场景。  

    <dubbo:protocol name="dubbo" dispatcher="all" threadpool="fixed" threads="100" />  
    
## 1.21. 上下文信息  
&emsp; 上下文中存放的是当前调用过程中所需的环境信息，所有配置信息都将转换为URL的参数。  
&emsp; 服务消费方  

```java
// 远程调用 
xxxService.xxx();
// 本端是否为消费端，这里会返回true 
boolean isConsumerSide = RpcContext.getContext().isConsumerSide();
// 获取最后一次调用的提供方IP地址 
String serverIP = RpcContext.getContext().getRemoteHost();
// 获取当前服务配置信息，所有配置信息都将转换为URL的参数 
String application = RpcContext.getContext().getUrl().getParameter("application");
// 注意：每发起RPC调用，上下文状态会变化
yyyService.yyy();  
```

&emsp; 服务提供方  

```java
public class XxxServiceImpl implements XxxService {
    public void xxx() {
        // 本端是否为提供端，这里会返回true                               
        boolean isProviderSide = RpcContext.getContext().isProviderSide();
        // 获取调用方IP地址                    
        String clientIP = RpcContext.getContext().getRemoteHost();
        // 获取当前服务配置信息，所有配置信息都将转换为URL的参数                 
        String application = RpcContext.getContext().getUrl().getParameter("application");
        // 注意：每发起RPC调用，上下文状态会变化                             
        yyyService.yyy();
        // 此时本端变成消费端，这里会返回false 
        boolean isProviderSide = RpcContext.getContext().isProviderSide();
    }
}
```

## 1.22. 异步调用  
&emsp; 基于NIO的非阻塞实现并行调用，客户端不需要启动多线程即可完成并行调用多个远程服务，相对多线程开销较小。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-8.png)   

&emsp; 在consumer.xml中配置  

    <dubbo:reference id="fooService" interface="com.alibaba.foo.FooService">
        <dubbo:method name="findFoo" async="true" />
    </dubbo:reference>
    <dubbo:reference id="barService" interface="com.alibaba.bar.BarService">
        <dubbo:method name="findBar" async="true" />
    </dubbo:reference>

&emsp; 调用代码  

```java
// 此调用会立即返回null
fooService.findFoo(fooId);
// 拿到调用的Future引用，当结果返回后，会被通知和设置到此Future
Future<Foo> fooFuture = RpcContext.getContext().getFuture();
// 此调用会立即返回null
barService.findBar(barId);
// 拿到调用的Future引用，当结果返回后，会被通知和设置到此Future
Future<Bar> barFuture = RpcContext.getContext().getFuture();
// 此时findFoo和findBar的请求同时在执行，客户端不需要启动多线程来支持并行，而是借助NIO的非阻塞完成
// 如果foo已返回，直接拿到返回值，否则线程wait住，等待foo返回后，线程会被notify唤醒
Foo foo = fooFuture.get();
// 同理等待bar返回
Bar bar = barFuture.get();
// 如果foo需要5秒返回，bar需要6秒返回，实际只需等6秒，即可获取到foo和bar，进行接下来的处理
```
&emsp; 可以设置是否等待消息发出：  
&emsp; sent=”true”等待消息发出，消息发送失败将抛出异常。  
&emsp; sent=”false”不等待消息发出，将消息放入 IO 队列，即刻返回。  

    <dubbo:method name="findFoo" async="true" sent="true"/>
    
&emsp; 如果只是异步调用，完全忽略返回值，可以配置return=”false”，以减少Future对象的创建和管理成本。    

    <dubbo:method name="findFoo" async="true" return="false" />

## 1.23. 参数回调  
&emsp; 参数回调方式与调用本地callback或listener相同，只需要在Spring的配置文件中声明哪个参数是callback类型即可，Dubbo将基于长连接生成反向代理，这样就可以从服务器端调用客户端逻辑。  

## 1.24. 事件通知  
&emsp; 在调用之前，调用之后，出现异常时，会触发oninvoke, onreturn，onthrow三个事件，可以配置当事件发生时，通知哪个类的哪个方法。  

    <bean id ="demoCallback" class = "com.alibaba.dubbo.callback.implicit.NofifyImpl" />
    <dubbo:reference id="demoService" interface="com.alibaba.dubbo.callback.implicit.IDemoService" version="1.0.0" group="cn" >
        <dubbo:method name="get" async="true" onreturn = "demoCallback.onreturn" onthrow="demoCallback.onthrow" />
    </dubbo:reference>

&emsp; callback与async功能正交分解：async=true，表示结果是否马上返回；onreturn表示是否需要回调。   
&emsp; 组合情况：(async=false 默认)  
&emsp; 异步回调模式：async=true  onreturn="xxx"；  
&emsp; 同步回调模式：async=false  onreturn="xxx"；  
&emsp; 异步无回调：async=true；  
&emsp; 同步无回调：async=false；  

## 1.25. 本地存根  
&emsp; 远程服务后，客户端通常只剩下接口，而实现全在服务器端，但提供方有时候想在客户端也执行部分逻辑，比如：做ThreadLocal缓存，提前验证参数，调用失败后伪造容错数据等等，此时就需要在API中带上Stub，客户端生成Proxy实例，会把Proxy通过构造函数传给Stub，然后把Stub暴露给用户，Stub可以决定要不要去调Proxy。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-9.png)   

    <dubbo:service interface="com.foo.BarService" stub="true" />
    
&emsp; 或  

    <dubbo:service interface="com.foo.BarService" stub="com.foo.BarServiceStub" />
    
&emsp; 提供Stub的实现：  

```java
public class BarServiceStub implements BarService {
    private final BarService barService;
    // 构造函数传入真正的远程代理对象              
    public (BarService barService)  {
        this.barService = barService;
    }

    public String sayHello(String name) {
        // 此代码在客户端执行,你可以在客户端做ThreadLocal本地缓存，或预先验证参数是否合法，等等                             
        try {
            return barService.sayHello(name);
        } catch (Exception e) {
            // 你可以容错，可以做任何AOP拦截事项                               
            return "容错数据";
        }
    }
}
```

## 1.26. 本地伪装  
&emsp; 本地伪装通常用于服务降级，比如某验权服务，当服务提供方全部挂掉后，客户端不抛出异常，而是通过Mock数据返回授权失败。  

    <dubbo:service interface="com.foo.BarService" mock="true" />
    
&emsp; 或
    
    <dubbo:service interface="com.foo.BarService" mock="com.foo.BarServiceMock" />
    
&emsp; 在工程中提供Mock实现：  

```java
public class BarServiceMock implements BarService {
    public String sayHello(String name) {
        // 可以伪造容错数据，此方法只在出现RpcException时被执行                             
        return  "容错数据";
    }
}
```  
&emsp; 如果服务的消费方经常需要try-catch捕获异常，可以考虑改为Mock（只有出现RpcException时才执行）实现，并在Mock实现中return null，如果只是想简单的忽略异常，在2.0.11以上版本可用。  

    <dubbo:service interface="com.foo.BarService" mock="return  null" />

## 1.27. 超时时间设置  
&emsp; 有两种方式：  

* 服务提供者端设置超时时间，在Dubbo的用户文档中，推荐如果能在服务端多配置就尽量多配置，因为服务提供者比消费者更清楚提供的服务特性。  
* 服务消费者端设置超时时间，如果在消费者端设置了超时时间，以消费者端为主，即优先级更高。因为服务调用方设置超时时间控制性更灵活。如果消费方超时，服务端线程不会定制，会产生警告。  

## 1.28. 集群容错
&emsp; 在集群调用失败时，Dubbo提供了多种容错方案，缺省为failover重试。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-10.png)  

* Failover Cluster：失败自动切换，当出现失败，重试其它服务器。通常用于读操作，但重试会带来更长延迟。可通过retries=”2”来设置重试次数(不含第一次)。  

        <dubbo:service retries="2" />
    
    &emsp; 或  

        <dubbo:reference retries="2" />
        
    &emsp; 或  

        <dubbo:reference>
            <dubbo:method name="findFoo" retries="2" />
        </dubbo:reference>

* Failfast Cluster：快速失败，只发起一次调用，失败立即报错。通常用于非幂等性的写操作，比如新增记录。  
* Failsafe Cluster：失败安全，出现异常时，直接忽略。通常用于写入审计日志等操作。 

        <dubbo:service cluster="failsafe" />
    
    &emsp; 或  

        <dubbo:reference cluster="failsafe" />
        
&emsp; Failback Cluster：失败自动恢复，后台记录失败请求，定时重发。通常用于消息通知操作。  
&emsp; Forking Cluster：并行调用多个服务器，只要一个成功即返回。通常用于实时性要求较高的读操作，但需要浪费更多服务资源。可通过 forks=”2”来设置最大并行数。  
&emsp; Broadcast Cluster：广播调用所有提供者，逐个调用，任意一台报错则报错。通常用于通知所有提供者更新缓存或日志等本地资源信息。  

## 1.29. 集群的负载均衡策略  
&emsp; 在集群负载均衡时，Dubbo 提供多种均衡策略，缺省为random随机调用。  

    <!-- 服务端服务级别 -->
    <dubbo:service interface="..." loadbalance="roundrobin" />
    <!-- 客户端服务级别 -->
    <dubbo:reference interface="..." loadbalance="roundrobin" />
    <!-- 服务端方法级别 -->
    <dubbo:service interface="...">
        <dubbo:method name="..." loadbalance="roundrobin" />
    </dubbo:service>
    <!-- 客户端方法级别 -->
    <dubbo:reference interface="...">
        <dubbo:method name="..." loadbalance="roundrobin" />
    </dubbo:reference>

* Random LoadBalance:随机选取提供者策略，有利于动态调整提供者权重。截面碰撞率高，调用次数越多，分布越均匀；  
* RoundRobin LoadBalance:轮循选取提供者策略，平均分布，但是存在请求累积的问题；  
* LeastActive LoadBalance:最少活跃调用策略，解决慢提供者接收更少的请求；  
* ConstantHash LoadBalance:一致性Hash策略，使相同参数请求总是发到同一提供者，一台机器宕机，可以基于虚拟节点，分摊至其他提供者，避免引起提供者的剧烈变动；  

## 1.30. 优雅停机
&emsp; kill PID可以实现，kill -9 PID 不可以。  
