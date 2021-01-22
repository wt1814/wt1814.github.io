
<!-- TOC -->

- [1. Spring Cloud Zuul](#1-spring-cloud-zuul)
    - [1.1. 路由转发](#11-路由转发)
        - [1.1.1. 服务路由的默认规则](#111-服务路由的默认规则)
        - [1.1.2. 路径匹配](#112-路径匹配)
        - [1.1.3. 本地跳转](#113-本地跳转)
        - [1.1.4. zuul的安全与header](#114-zuul的安全与header)
        - [1.1.5. 重定向问题](#115-重定向问题)
        - [1.1.6. 使用zuul上传文件问题](#116-使用zuul上传文件问题)
        - [1.1.7. Hystrix 和 Ribbon 支持](#117-hystrix-和-ribbon-支持)
    - [1.2. 请求过滤](#12-请求过滤)
        - [1.2.1. 前置过滤器](#121-前置过滤器)
            - [1.2.1.1. 鉴权](#1211-鉴权)
            - [1.2.1.2. 流量转发](#1212-流量转发)
        - [1.2.2. 后置过滤器](#122-后置过滤器)
            - [1.2.2.1. 跨域](#1221-跨域)
            - [1.2.2.2. 统计](#1222-统计)
        - [1.2.3. 错误过滤器](#123-错误过滤器)
        - [1.2.4. 过滤器具体实现](#124-过滤器具体实现)
    - [1.3. 动态加载](#13-动态加载)
    - [1.4. zuul的高可用](#14-zuul的高可用)
        - [1.4.1. Zuul客户端也注册到了Eureka Server上](#141-zuul客户端也注册到了eureka-server上)
        - [1.4.2. Zuul客户端未注册到Eureka Server上](#142-zuul客户端未注册到eureka-server上)

<!-- /TOC -->

# 1. Spring Cloud Zuul
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-31.png)  

&emsp; Spring Cloud Zuul，微服务网关，包含hystrix、ribbon、actuator。主要有路由转发、请求过滤功能。  

## 1.1. 路由转发  
&emsp; 采用zuul.routes.<route\>.path与zuul.routes.<route\>.serviceid参数对的方式进行配置。例如：  

```properties
zuul.routes.user-service.path=/user-service/**  
zuul.routes.user-service.serviceid=user-service
```
&emsp; 还可以更简洁的配置方式：zuul.routes.<serviceid\>=<path\>, 其中<serviceid\> 用来指定路由的具体服务名，<path\>用来配置匹配的请求表达式。  

### 1.1.1. 服务路由的默认规则  
&emsp; 为Spring Cloud Zuul构建的API网关服务引入Spring Cloud Eureka之后，<font color = "red">zuul为Eureka中的每个服务都自动创建一个默认路由规则，这些默认规则的path会使用service Id配置的服务名作为请求前缀。</font>如上所示。  
&emsp; 默认情况下所有Eureka上的服务都会被Zuul自动创建映射关系进行路由，一些不希望对外开放的服务也被外部访问到。这个时候可以配置zuul.ignored-services参数来设置一个服务名匹配表达式进行判断，如果服务名匹配表达式，那么Zull将跳过这个服务，不为其创建路由规则。例如：zuul.ignored-services=*表示对所有的服务不自动创建路由规则，这样就需要为每个服配置路由规则。  

### 1.1.2. 路径匹配  
&emsp; 在Zuul中，路由匹配的路径表达式采用了Ant风格定义，它一共有下面三种通配符。  

* ?：匹配任意单个字符  
* *：匹配任意数量的字符  
* **：匹配任意数量的字符，支持多级目录  

&emsp; 在使用的路由规则匹配请求的时候是通过线性遍历的方式，在请求路径获取到第一个匹配的路由规则之后就返回并结束匹配过程。所以当存在多个匹配的路由规则时，匹配结果完全取决于路由规则的保存顺序。  

### 1.1.3. 本地跳转  
&emsp; Zuul 实现的 API 网关中，支持 forward 形式的服务端跳转配置，只需通过使用 path 与 url 的配置方式就能完成，通过 url 中使用 forward 来指定需要跳转的服务器资源路径。  

```yaml
zuul:
  routes:
    api-a:
      path: /api-a/**
      url: http://localhost:8001/
    api-b:
      path: /api-b/**
      url: forward:/local
```
&emsp; 上面的配置实现了将符合 /api-b/** 规则的请求转发到 API 网关中以 /local 为前缀的请求上，由 API 网关进行本地处理。比如，当 API 网关接收到请求 /api-b/hello，它将符合 api-b 的路由规则，所以该请求会被 API 网关转发到网关的 /local/hello 请求上进行本地处理。由于需要在API网关上实现本地跳转，所以相应的也需要为本地跳转实现对应的请求接口，否则返回404错误。

### 1.1.4. zuul的安全与header  
&emsp; **<font color = "red">在默认情况下，Spring Cloud Zuul 在请求路由时，会过滤掉HTTP请求头信息中的一些敏感信息，防止它们被传递到下游的外部服务器。默认的敏感头信息通过zuul.sensitiveHeaders 参数定义，包括Cookie、Set-Cookie、Authorization 三个属性。</font>** 这样会引发一个问题，如果使用了Spring Security、Shiro 等安全框架构建的 Web 应用通过 Spring Cloud Zuul 构建的网关来进行路由时，Cookie 信息无法传递，会导致无法实现 登录和鉴权。  

* 通过设置全局参数为空来覆盖默认，不推荐，破坏了默认设置的用意  

    ```properties
    zuul.sensitiveHeaders=
    ```
* 通过指定路由的参数来设置，仅对指定的web应用开启敏感信息传递

    ```properties
    # 对指定路由开启自定义敏感头
    zuul.routes.<router>.customSensitiveHeaders=true
    # 将指定路由的敏感头信息设置为空
    zuul.routes.<router>.sensitiveHeaders=
    ```

### 1.1.5. 重定向问题  
&emsp; 解决了Cookie后，登录成功之后，跳转的页面 URL 却是具体 Web 应用实例的地址，而不是通过网关路由地址。无视了网关作为统一的入口。通过浏览器查看请求详情，引起问题的大致原因就是Spring Security 或 Shiro 在登录完成之后，通过重定向的方式跳转到登录后的页面，状态码为302，请求头信息中的 Host 指向了具体的服务实例IP地址和端口，该问题的根本原因在于 Spring Cloud Zuul 在路由请求时，并没有将最初的Host信息设置正确。  
&emsp; 通过设置Zuul参数，可以将Host设置为最初的服务端请求地址。  

```properties
zuul.addHostHeader=true
```

### 1.1.6. 使用zuul上传文件问题  
&emsp; 对于小文件（1M以内上传），无须任何处理，即可正常上传。 **<font color = "red">对于大文件（10M以上）上传，需要为上传路径添加/zuul前缀。也可使用zuul.servlet-path自定义前缀。</font>**   
&emsp; 假设zuul.routes.microservice-file-upload=/microservice-file-upload/**  
&emsp; 如果http://{HOST}:{PORT}/upload 是微服务microservice-file-upload的上传路径，则可使用Zuul的/zuul/microservice-file-upload/upload路径上传大文件。  
&emsp; 如果Zuul使用了Ribbon做负载均衡，那么对于超大的文件（例如500M），需要提高超时设置，例如：  

```yaml
hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds: 60000
ribbon:
  ConnectTimeout: 3000
  ReadTimeout: 60000
```

### 1.1.7. Hystrix 和 Ribbon 支持
&emsp; <font color = "red">引入 Zuul 依赖时，其自动引入了 Ribbon、Hystrix 依赖</font>，所以 Zuul 天生就拥有线程隔离和断路器的自我保护功能，以及对服务调用客户端的负载均衡功能。  
&emsp; 但是 **<font color = "red">如果使用的是 path 和 url 的映射关系来配置路由规则的时候，对于路由转发的请求不会采用 HystrxiCommand 来包装，所以这类路由请求没有线程隔离和断路器的保护，并且也不会有负载均衡的能力。</font>** 因此，尽量使用 path 和 serviceId 的组合来进行配置，不仅可以保证 API 网关的健壮和稳定，也能用到 Ribbon 的客户端负载均衡功能。  
&emsp; 在使用 Zuul 搭建网关的时候，可以通过Hystrix 和 Ribbon 的参数来调整路由请求的各种超时时间等配置（参考 Ribbon 和 Hystrix 配置）。  

## 1.2. 请求过滤  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-23.png)  
&emsp; **<font color = "red">Zuul提供了四种过滤器的API，分别为前置（Pre）、路由（Route）、后置（Post）和错误（Error）四种处理方式。</font>** 一个请求会先按顺序通过所有的前置过滤器，之后在路由过滤器中转发给后端应用，得到响应后又会通过所有的后置过滤器，最后响应给客户端。在整个流程中如果发生了异常则会跳转到错误过滤器中。  
&emsp; 一般来说，<font color = "red">如果需要在请求到达后端应用前就进行处理的话，会选择前置过滤器，例如鉴权、请求转发、增加请求参数等行为。在请求完成后需要处理的操作放在后置过滤器，例如统计返回值和调用时间、记录日志、增加跨域头等行为。</font>路由过滤器一般只需要选择Zuul中内置的即可，错误过滤器一般只需要一个，这样可以在Gateway遇到错误逻辑时直接抛出异常中断流程，并直接统一处理返回结果。  

### 1.2.1. 前置过滤器
#### 1.2.1.1. 鉴权
&emsp; 一般来说整个服务的鉴权逻辑可以很复杂。  

    客户端：App、Web、Backend  
    权限组：用户、后台人员、其他开发者  
    实现：OAuth、JWT  
    使用方式：Token、Cookie、SSO

&emsp; 而对于后端应用来说，它们其实只需要知道请求属于谁，而不需要知道为什么，所以Gateway可以友善的帮助后端应用完成鉴权这个行为，并将用户的唯一标示透传到后端，而不需要、甚至不应该将身份信息也传递给后端，防止某些应用利用这些敏感信息做错误的事情。  
&emsp; Zuul默认情况下在处理后会删除请求的Authorization头和Set-Cookie头，也符合这个原则。  

#### 1.2.1.2. 流量转发
&emsp; 流量转发的含义就是将指向/a/xxx.json的请求转发到指向/b/xxx.json的请求。这个功能可能在一些项目迁移、或是灰度发布上会有一些用处。  
&emsp; 在Zuul中并没有一个很好的办法去修改Request URI。在某些Issue中开发者会建议设置requestURI这个属性，但是实际在Zuul自身的PreDecorationFilter流程中又会被覆盖一遍。  
&emsp; 不过对于一个基于Servlet的应用，使用HttpServletRequestWrapper基本可以解决一切问题，在这个场景中只需要重写其getRequestURI方法即可。  

```java
class RewriteURIRequestWrapper extends HttpServletRequestWrapper {

    private String rewriteURI;

    public RewriteURIRequestWrapper(HttpServletRequest request, String rewriteURI) {
        super(request);
        this.rewriteURI = rewriteURI;
    }
    @Override
    public String getRequestURI() {
        return rewriteURI;
    }
}
```

### 1.2.2. 后置过滤器  
#### 1.2.2.1. 跨域  
&emsp; 使用Gateway做跨域相比应用本身或是Nginx的好处是规则可以配置的更加灵活。例如一个常见的规则。  
&emsp; 对于任意的AJAX请求，返回Access-Control-Allow-Origin为 *，且 Access-Control-Allow-Credentials为true，这是一个常用的允许任意源跨域的配置，但是不允许请求携带任何Cookie。  
&emsp; 如果一个被信任的请求者需要携带Cookie，那么将它的Origin增加到白名单中。对于白名单中的请求，返回Access-Control-Allow-Origin为该域名，且 Access-Control-Allow-Credentials为true，这样请求者可以正常的请求接口，同时可以在请求接口时携带Cookie。  
&emsp; 对于302的请求，即使在白名单内也必须要设置 Access-Control-Allow-Origin为 *，否则重定向后的请求携带的Origin会为null，有可能会导致iOS低版本的某些兼容问题。  

#### 1.2.2.2. 统计  
&emsp; Gateway可以统一收集所有应用请求的记录，并写入日志文件或是发到监控系统，相比Nginx的access log，好处主要也是二次开发比较方便，比如可以关注一些业务相关的HTTP头，或是将请求参数和返回值都保存为日志打入消息队列中，便于线上故障调试。也可以收集一些性能指标发送到类似Statsd这样的监控平台。  

### 1.2.3. 错误过滤器  
&emsp; 错误过滤器的主要用法就像是Jersey中的ExceptionMapper或是Spring MVC中的@ExceptionHandler一样，在处理流程中认为有问题时，直接抛出统一的异常，错误过滤器捕获到这个异常后，就可以统一的进行返回值的封装，并直接结束该请求。  

### 1.2.4. 过滤器具体实现  
&emsp; 请求过滤类似于Java中Filter过滤器，先将所有的请求拦截下来，然后根据现场情况做出不同的处理。Zuul中的过滤器的使用：  

1. 定义过滤器：过滤器继承自ZuulFilter，有4个方法需要实现。  

```java
public class PermisFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
    RequestContext ctx = RequestContext.getCurrentContext();
    HttpServletRequest request = ctx.getRequest();
    String login = request.getParameter("login");
    if (login == null) {
        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(401);
        ctx.addZuulResponseHeader("content-type","text/html;charset=utf-8");
        ctx.setResponseBody("非法访问");
    }
    return null;
    }
}
```
&emsp; filterType：返回一个字符串代表过滤器的类型，过滤器的类型决定了过滤器在哪个生命周期执行，在zuul中定义了四种不同生命周期的过滤器类型：pre：路由之前；routing：路由之时；post：路由之后；error：发送错误调用；对应Spring AOP里的前加强、前后加强、后加强、异常处理。  
&emsp; filterOrder：过滤器的执行顺序，多个过滤器同时存在时根据这个order来决定先后顺序，越小优先级越高。  
&emsp; shouldFilter：可以写逻辑判断，判断是否要过滤。true，永远过滤。  
&emsp; 方法用来判断过滤器是否执行，true表示执行，false表示不执行，在实际开发中，可以根据当前请求地址来决定要不要对该地址进行过滤，这里直接返回true。  
&emsp; run：过滤器的具体逻辑。可以很复杂，包括查sql，nosql去判断该请求到底有没有权限访问。  
&emsp; run方法则表示过滤的具体逻辑，假设请求地址中携带了login参数的话，则认为是合法请求，否则就是非法请求，如果是非法请求的话，首先设置ctx.setSendZuulResponse(false);表示不对该请求进行路由，然后设置响应码和响应值。这个run方法的返回值在当前版本(Dalston.SR3)中暂时没有任何意义，可以返回任意值。  

2. 配置过滤器Bean：入口类中配置相关的Bean，如下：  

```java
@EnableZuulProxy
@SpringCloudApplication
public class DemoFeignApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoFeignApplication.class, args);
    }

    @Bean
    public PermisFilter permisFilter() {
        return new PermisFilter();
    }

}
```
&emsp; 此时，如果访问http://localhost:2006/api-a/hello1 ，结果如下： 非法访问。如果给请求地址加上login参数，则结果如下：Hello 张三！  

## 1.3. 动态加载  
&emsp; 作为最外部的网关，它必须具备动态更新内部逻辑的能力， 比如动态修改路由规则、 动态添加／删除过滤器等。  
&emsp; 通过Zuul实现的API网关服务具备了动态路由和动态过滤的器能力，可以在不重启API网关服务的前提下为其动态修改路由规则和添加或删除过滤器。  

* 动态路由：将API网关服务的配置文件通过 Spring Cloud Config 连接的Git仓库存储和管理，就能实现动态刷新路由规则的功能。  
* 动态过滤器：实现请求过滤器的动态加载，需要借助基于JVM实现的动态语言的帮助，比如Groovy。  

## 1.4. zuul的高可用  
&emsp; Zuul的高可用有2种场景。  

### 1.4.1. Zuul客户端也注册到了Eureka Server上  
&emsp; 只需将多个Zuul节点注册到Eureka Server上，就可实现Zuul的高可用。此时，Zuul的高可用与其他微服务的高可用没什么区别。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-24.png)  
&emsp; 当Zuul客户端也注册到Eureka Server上时，只需部署多个Zuul节点即可实现其高可用。Zuul客户端会自动从Eureka Server中查询Zuul Server的列表，并使用Ribbon负载均衡地请求Zuul集群。  

### 1.4.2. Zuul客户端未注册到Eureka Server上  
&emsp; Nginx、HAProxy、F5等负载均衡器来实现Zuul的高可用。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/SpringCloudNetflix/cloud-25.png)  
