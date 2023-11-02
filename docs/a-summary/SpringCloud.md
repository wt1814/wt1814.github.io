
<!-- TOC -->

- [1. SpringCloud](#1-springcloud)
    - [1.1. SpringBoot](#11-springboot)
        - [1.1.1. SpringBoot常见面试题](#111-springboot常见面试题)
            - [1.1.1.1. SpringBoot有哪些优点?](#1111-springboot有哪些优点)
            - [1.1.1.2. SpringBoot常用注解](#1112-springboot常用注解)
        - [1.1.2. SpringBoot启动过程](#112-springboot启动过程)
            - [1.1.2.1. SpringApplication初始化](#1121-springapplication初始化)
            - [1.1.2.2. run()方法运行过程](#1122-run方法运行过程)
            - [1.1.2.3. SpringBoot事件监听机制](#1123-springboot事件监听机制)
                - [1.1.2.3.1. 事件监听步骤](#11231-事件监听步骤)
                - [1.1.2.3.2. 内置生命周期事件](#11232-内置生命周期事件)
            - [1.1.2.4. SpringBoot事件回调](#1124-springboot事件回调)
        - [1.1.3. SpringBoot自动配置（获取 ---> 注入 ---> 装配）](#113-springboot自动配置获取-----注入-----装配)
            - [1.1.3.1. 获取自动配置，注解@SpringBootApplication](#1131-获取自动配置注解springbootapplication)
            - [1.1.3.2. 加载自动配置流程](#1132-加载自动配置流程)
            - [1.1.3.3. 内置Tomcat](#1133-内置tomcat)
        - [1.1.4. 自定义strater](#114-自定义strater)
    - [1.2. SpringCloud](#12-springcloud)
        - [1.2.1. SpringCloud组件](#121-springcloud组件)
        - [1.2.2. 网关](#122-网关)
            - [1.2.2.1. 网关介绍](#1221-网关介绍)
            - [1.2.2.2. Spring Cloud Gateway](#1222-spring-cloud-gateway)
                - [介绍](#介绍)
                - [路由](#路由)
            - [1.2.2.3. Zuul](#1223-zuul)
        - [1.2.3. 注册中心和配置中心](#123-注册中心和配置中心)
            - [1.2.3.1. 注册中心和配置中心介绍](#1231-注册中心和配置中心介绍)
            - [注册中心宕机](#注册中心宕机)
            - [1.2.3.2. Eureka](#1232-eureka)
        - [1.2.4. Ribbon](#124-ribbon)
        - [1.2.5. Feign](#125-feign)
        - [1.2.6. Hytrix](#126-hytrix)
        - [1.2.7. Sleuth](#127-sleuth)
        - [1.2.8. Admin](#128-admin)

<!-- /TOC -->


# 1. SpringCloud

## 1.1. SpringBoot
### 1.1.1. SpringBoot常见面试题
#### 1.1.1.1. SpringBoot有哪些优点?  
&emsp; 减少开发，减少测试时间。  
&emsp; 使用JavaConfig有助于避免使用XML。  
&emsp; 避免大量的Maven导入和各种版本冲突。  
&emsp; 提供意见发展方法。   
&emsp; 通过提供默认值快速开始开发。    
&emsp; 没有单独的Web服务器需要。这意味着你不再需要启动Tomcat，Glassfish或其他任何东西。  
&emsp; 需要更少的配置 因为没有web.xml文件。只需添加用@ Configuration注释的类，然后添加用@Bean注释的方法，Spring将自动加载对象并像以前一样对其进行管理。您甚至可以将@Autowired添加到bean方法中，以使Spring自动装入需要的依赖关系中。   


#### 1.1.1.2. SpringBoot常用注解  
<!-- 

https://baijiahao.baidu.com/s?id=1713045744318175557&wfr=spider&for=pc
-->
1. Spring Boot注解
    1. @SpringBootApplication 是 @Configuration、@EnableAutoConfiguration、@ComponentScan 注解的集合。  
    &emsp; 根据 SpringBoot 官网，这三个注解的作用分别是：    
    &emsp; @EnableAutoConfiguration：启用 SpringBoot 的自动配置机制    
    &emsp; @ComponentScan： 扫描被@Component (@Service,@Controller)注解的 bean，注解默认会扫描该类所在的包下所有的类。  
    &emsp; @Configuration：允许在 Spring 上下文中注册额外的 bean 或导入其他配置类   

    2. @EnableAutoConfiguration  
    &emsp; @EnableAutoConfiguration注解用于通知Spring，根据当前类路径下引入的依赖包，自动配置与这些依赖包相关的配置项。   

    3. @ConditionalOnClass与@ConditionalOnMissingClass

2. Spring Web MVC注解
    2. @CrossOrigin  

3. Spring Bean注解  


### 1.1.2. SpringBoot启动过程
&emsp; SpringApplication.run()中首先new SpringApplication对象，然后调用该对象的run方法。<font color = "red">`即run()方法主要包括两大步骤：`</font>  
1. 创建SpringApplication对象；  
2. 运行run()方法。  

#### 1.1.2.1. SpringApplication初始化
0. SpringBoot的启动过程`(new SpringApplication(primarySources)).run(args)`包含两个流程：1，SpringApplication的初始化；2，run()方法执行。  
1. **<font color = "blue">构造过程一般是对构造函数的一些成员属性赋值，做一些初始化工作。</font><font color = "blue">SpringApplication有6个属性：`资源加载器`、资源类集合、应用类型、`初始化器`、`监听器`、`包含main函数的主类`。</font>**  
	1. 给resourceLoader属性赋值，resourceLoader属性，资源加载器，此时传入的resourceLoader参数为null；  
	2. **<font color = "clime">初始化资源类集合并去重。</font>** 给primarySources属性赋值，primarySources属性即`SpringApplication.run(MainApplication.class,args);`中传入的MainApplication.class，该类为SpringBoot项目的启动类，主要通过该类来扫描Configuration类加载bean；
	3. **<font color = "clime">判断当前是否是一个 Web 应用。</font>** 给webApplicationType属性赋值，webApplicationType属性，代表应用类型，根据classpath存在的相应Application类来判断。因为后面要根据webApplicationType来确定创建哪种Environment对象和创建哪种ApplicationContext；
	4. **<font color = "blue">设置应用上下文初始化器。</font>** 给initializers属性赋值，initializers属性为List<ApplicationContextInitializer<?\>>集合，利用SpringBoot的SPI机制从spring.factories配置文件中加载，后面在初始化容器的时候会应用这些初始化器来执行一些初始化工作。因为SpringBoot自己实现的SPI机制比较重要；  
	5. **<font color = "blue">设置监听器。</font>** 给listeners属性赋值，listeners属性为List<ApplicationListener<?\>>集合，同样利用SpringBoot的SPI机制从spring.factories配置文件中加载。因为SpringBoot启动过程中会在不同的阶段发射一些事件，所以这些加载的监听器们就是来监听SpringBoot启动过程中的一些生命周期事件的；
	6. **<font color = "clime">推断主入口应用类。</font>** 给mainApplicationClass属性赋值，mainApplicationClass属性表示包含main函数的类，即这里要推断哪个类调用了main函数，然后把这个类的全限定名赋值给mainApplicationClass属性，用于后面启动流程中打印一些日志。

2. **<font color = "clime">SpringApplication初始化中第4步（设置应用上下文初始化器）和第5步（设置监听器）都是利用SpringBoot的[SPI机制](/docs/java/basis/SPI.md)来加载扩展实现类。SpringBoot通过以下步骤实现自己的SPI机制：</font>**  
	1. 首先获取线程上下文类加载器;  
	2. 然后利用上下文类加载器从spring.factories配置文件中加载所有的SPI扩展实现类并放入缓存中；  
	3. 根据SPI接口从缓存中取出相应的SPI扩展实现类；  
	4. 实例化从缓存中取出的SPI扩展实现类并返回。  

#### 1.1.2.2. run()方法运行过程
1. **<font color = "clime">运行流程，分3步：</font>**  
    1. **<font color = "clime">运行流程，分3步，具体有11步：</font>**  
        1. 创建所有Spring运行监听器并发布应用启动事件、准备环境变量、创建容器。 
        2. 容器准备（为刚创建的容器对象做一些初始化工作，准备一些容器属性值等）、刷新容器。 
        3. 执行刷新容器后的后置处理逻辑、调用ApplicationRunner和CommandLineRunner的run方法。  
    2. **<font color = "clime">`内置生命周期事件：`</font>** <font color = "red">在SpringBoot启动过程中，每个不同的启动阶段会分别发布不同的内置生命周期事件。</font>  
    3. **<font color = "clime">`事件回调机制：`</font>** <font color = "red">run()阶段涉及了比较重要的[事件回调机制](/docs/microService/SpringBoot/eventCallback.md)，回调4个监听器（ApplicationContextInitializer、ApplicationRunner、CommandLineRunner、SpringApplicationRunListener）中的方法与加载项目中组件到IOC容器中。</font>


    ```java
    // SpringApplication.java
    public ConfigurableApplicationContext run(String... args) {
        // 创建并启动计时监控类。new 一个StopWatch用于统计run启动过程花了多少时间
        StopWatch stopWatch = new StopWatch();
        // 开始计时，首先记录了当前任务的名称，默认为空字符串，然后记录当前Spring Boot应用启动的开始时间
        stopWatch.start();
        ConfigurableApplicationContext context = null;
        // exceptionReporters集合用来存储异常报告器，用来报告SpringBoot启动过程的异常
        Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
        // 配置系统属性headless，即“java.awt.headless”属性，默认为ture
        // 其实是想设置该应用程序,即使没有检测到显示器,也允许其启动.对于服务器来说,是不需要显示器的,所以要这样设置.
        configureHeadlessProperty();
        // 【1】创建所有 Spring 运行监听器并发布应用启动事件
        //从spring.factories配置文件中加载到EventPublishingRunListener对象并赋值给SpringApplicationRunListeners
        // EventPublishingRunListener对象主要用来发布SpringBoot启动过程中内置的一些生命周期事件，标志每个不同启动阶段
        SpringApplicationRunListeners listeners = getRunListeners(args);
        // 启动SpringApplicationRunListener的监听，表示SpringApplication开始启动。
        // 》》》》》发布【ApplicationStartingEvent】事件
        listeners.starting();
        try {
            // 初始化默认应用参数类，封装命令行参数
            // 创建ApplicationArguments对象，封装了args参数
            ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
            // 【2】准备环境变量，包括系统变量，环境变量，命令行参数，默认变量，servlet相关配置变量，随机值，
            // JNDI属性值，以及配置文件（比如application.properties）等，注意这些环境变量是有优先级的
            // 》》》》》发布【ApplicationEnvironmentPreparedEvent】事件
            ConfigurableEnvironment environment = prepareEnvironment(listeners,applicationArguments);
            // 配置spring.beaninfo.ignore属性，默认为true，即跳过搜索BeanInfo classes.
            configureIgnoreBeanInfo(environment);
            // 【3】控制台打印SpringBoot的bannner标志
            Banner printedBanner = printBanner(environment);
            // 【4】创建容器
            // 根据不同类型创建不同类型的spring applicationcontext容器
            // 因为这里是servlet环境，所以创建的是AnnotationConfigServletWebServerApplicationContext容器对象
            context = createApplicationContext();
            // 【5】准备异常报告器
            // 从spring.factories配置文件中加载异常报告期实例，这里加载的是FailureAnalyzers
            // 注意FailureAnalyzers的构造器要传入ConfigurableApplicationContext，因为要从context中获取beanFactory和environment
            exceptionReporters = getSpringFactoriesInstances(
                    SpringBootExceptionReporter.class,
                    new Class[] { ConfigurableApplicationContext.class }, context); // ConfigurableApplicationContext是AnnotationConfigServletWebServerApplicationContext的父接口
            // 【6】容器准备
            //为刚创建的AnnotationConfigServletWebServerApplicationContext容器对象做一些初始化工作，准备一些容器属性值等
            // 1）为AnnotationConfigServletWebServerApplicationContext的属性AnnotatedBeanDefinitionReader和ClassPathBeanDefinitionScanner设置environgment属性
            // 2）根据情况对ApplicationContext应用一些相关的后置处理，比如设置resourceLoader属性等
            // 3）在容器刷新前调用各个ApplicationContextInitializer的初始化方法，ApplicationContextInitializer是在构建SpringApplication对象时从spring.factories中加载的
            // 4）》》》》》发布【ApplicationContextInitializedEvent】事件，标志context容器被创建且已准备好
            // 5）从context容器中获取beanFactory，并向beanFactory中注册一些单例bean，比如applicationArguments，printedBanner
            // 6）TODO 加载bean到application context，注意这里只是加载了部分bean比如mainApplication这个bean，大部分bean应该是在AbstractApplicationContext.refresh方法中被加载？这里留个疑问先
            // 7）》》》》》发布【ApplicationPreparedEvent】事件，标志Context容器已经准备完成
            prepareContext(context, environment, listeners, applicationArguments,printedBanner);
            // 【7】刷新容器，IOC 容器初始化（如果是 Web 应用还会创建嵌入式的 Tomcat），扫描、创建、加载所有组件
            // 1）在context刷新前做一些准备工作，比如初始化一些属性设置，属性合法性校验和保存容器中的一些早期事件等；
            // 2）让子类刷新其内部bean factory,注意SpringBoot和Spring启动的情况执行逻辑不一样
            // 3）对bean factory进行配置，比如配置bean factory的类加载器，后置处理器等
            // 4）完成bean factory的准备工作后，此时执行一些后置处理逻辑，子类通过重写这个方法来在BeanFactory创建并预准备完成以后做进一步的设置
            // 在这一步，所有的bean definitions将会被加载，但此时bean还不会被实例化
            // 5）执行BeanFactoryPostProcessor的方法即调用bean factory的后置处理器：
            // BeanDefinitionRegistryPostProcessor（触发时机：bean定义注册之前）和BeanFactoryPostProcessor（触发时机：bean定义注册之后bean实例化之前）
            // 6）注册bean的后置处理器BeanPostProcessor，注意不同接口类型的BeanPostProcessor；在Bean创建前后的执行时机是不一样的
            // 7）初始化国际化MessageSource相关的组件，比如消息绑定，消息解析等
            // 8）初始化事件广播器，如果bean factory没有包含事件广播器，那么new一个SimpleApplicationEventMulticaster广播器对象并注册到bean factory中
            // 9）AbstractApplicationContext定义了一个模板方法onRefresh，留给子类覆写，比如ServletWebServerApplicationContext覆写了该方法来创建内嵌的tomcat容器
            // 10）注册实现了ApplicationListener接口的监听器，之前已经有了事件广播器，此时就可以派发一些early application events
            // 11）完成容器bean factory的初始化，并初始化所有剩余的单例bean。这一步非常重要，一些bean postprocessor会在这里调用。
            // 12）完成容器的刷新工作，并且调用生命周期处理器的onRefresh()方法，并且发布ContextRefreshedEvent事件
            refreshContext(context);
            // 【8】应用上下文刷新后置处理，从 IOC 容器中获取所有的 ApplicationRunner 和 CommandLineRunner 进行回调
            afterRefresh(context, applicationArguments);
            // 停止stopWatch计时
            stopWatch.stop();
            // 输出日志记录执行主类名、时间信息
            if (this.logStartupInfo) {
                new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
            }
            // 》》》》》发布【ApplicationStartedEvent】事件，标志spring容器已经刷新，此时所有的bean实例都已经加载完毕
            listeners.started(context);
            // 【9】调用ApplicationRunner和CommandLineRunner的run方法，实现spring容器启动后需要做的一些东西比如加载一些业务数据等
            callRunners(context, applicationArguments);
        }
        // 【10】若启动过程中抛出异常，此时用FailureAnalyzers来报告异常
        // 并》》》》》发布【ApplicationFailedEvent】事件，标志SpringBoot启动失败
        catch (Throwable ex) {
            handleRunFailure(context, ex, exceptionReporters, listeners);
            throw new IllegalStateException(ex);
        }

        try {
            // 发布应用上下文就绪事件，触发所有SpringApplicationRunListener 监听器的running事件方法。
            // 》》》》》发布【ApplicationReadyEvent】事件，标志SpringApplication已经正在运行即已经成功启动，可以接收服务请求了。
            listeners.running(context);
        }
        // 若出现异常，此时仅仅报告异常，而不会发布任何事件
        catch (Throwable ex) {
            handleRunFailure(context, ex, exceptionReporters, null);
            throw new IllegalStateException(ex);
        }
        // 【11】最终返回容器
        return context;
    }
    ```

#### 1.1.2.3. SpringBoot事件监听机制
##### 1.1.2.3.1. 事件监听步骤
&emsp; **<font color = "clime">SpringBoot启动时广播生命周期事件步骤：</font>**    
1. 为广播SpringBoot内置生命周期事件做前期准备：    
    1. 首先加载ApplicationListener监听器实现类；  
    2. 其次加载SPI扩展类EventPublishingRunListener。  
2. SpringBoot启动时利用EventPublishingRunListener广播生命周期事件，然后ApplicationListener监听器实现类监听相应的生命周期事件执行一些初始化逻辑的工作。  
    1. 构建SpringApplication对象时<font color = "red">根据ApplicationListener接口去spring.factories配置文件中加载并实例化监听器。</font>  
    2. 在SpringBoot的启动过程中首先从spring.factories配置文件中加载并实例化EventPublishingRunListener对象。  
    3. 在SpringBoot的启动过程中会发布一系列SpringBoot内置的生命周期事件。  

##### 1.1.2.3.2. 内置生命周期事件
&emsp; **<font color = "clime">SpringBoot内置的7种生命周期事件</font>**  

|发布顺序|事件|用途|
|---|---|---|
|1|ApplicationStartingEvent|在SpringApplication`启动时`但在环境变量Environment或容器ApplicationContext创建前触发，标志SpringApplication开始启动。|
|2|ApplicationEnvironmentPreparedEvent|在SpringApplication`已经开始启动`且环境变量Environment已经准备好时触发，标志环境变量已经准备好。|
|3|ApplicationContextInitializedEvent|ApplicationContextInitilizers的初始化方法已经被调用即从spring.factories中加载的initializers已经执行ApplicationContext初始化逻辑但在bean定义加载前触发，标志Application已经初始化完毕。|
|4|ApplicationPreparedEvent|在Spring容器`刷新refresh前`触发。|
|5|ApplicationStaredEvent|在Spring`容器刷新后`触发，但在调用ApplicationRunner和CommandLineRunner的run方法调用前触发，标志Spring容器已经刷新，此时所有的bean实例等都已经加载了。|
|6|ApplicationReadyEvent|只要SpringApplication可以接收服务请求时即调用完ApplicationRunner和CommandLineRunner的run方法后触发，此时标志SpringApplication已经正在运行，即成功启动。|
|7|ApplicationFailedEvent|若SpringApplicaton`未能成功启动时`则会catch住异常发布ApplicationFailedEvent事件，标志ApplicationFailedEvent启动失败。|


#### 1.1.2.4. SpringBoot事件回调
&emsp; **<font color = "clime">SpringBoot事件回调：</font>**  

* **<font color = "red">ApplicationContextInitializer，IOC容器初始化时被回调；</font>**  
* **<font color = "red">SpringApplicationRunListener，SpringBoot启动过程中多次被回调；</font>**  
* **<font color = "red">ApplicationRunner，容器启动完成后被回调；</font>**  
* **<font color = "red">CommandLineRunner，ApplicationRunner之后被回调。</font>**  

### 1.1.3. SpringBoot自动配置（获取 ---> 注入 ---> 装配）
&emsp; 包含两部分：1. 注解@SpringBootApplication，获取自动配置；2. 加载自动配置流程。

#### 1.1.3.1. 获取自动配置，注解@SpringBootApplication
1. @SpringBootApplication  
    * @SpringBootConfiguration，其实就携带了一个@Configuration注解，代表类是一个Spring的配置类。  
    * @ComponentScan，其仅仅是指定包，而并没有扫描这些包，更没有装配其中的类，这个真正扫描并装配这些类是@EnableAutoConfiguration完成的。    
    * `@EnableAutoConfiguration`，自动装配    
2. `@EnableAutoConfiguration`：
    1. @AutoConfigurationPackage，~~将添加该注解的类所在的package 作为 自动配置package 进行管理。~~ 
    2. 使用@Import将所有符合自动配置条件的bean定义加载到IOC容器。   
        1. @Import({AutoConfigurationImportSelector.class})，开启自动配置，导入AutoConfigurationImportSelector类。  
        2. AutoConfigurationImportSelector#getCandidateConfigurations()方法获取配置文件spring.factories所有候选的配置，剔除重复部分，再剔除@SpringbootApplication注解里exclude的配置，才得到最终的配置类名集合。  

3. **<font color = "red">小结：</font>** 3种类型的注解：1).@SpringBootConfiguration、@Configuration；2).@ComponentScan、@AutoConfigurationPackage；3).@Import   


#### 1.1.3.2. 加载自动配置流程
1. `启动对象的注入`：在SpringBoot启动流程的`容器准备阶段`prepareContext()会将@SpringBootApplication--->@Component对象注册到容器中。  
2. `自动装配入口`，从SpringBoot启动流程的`刷新容器阶段`refresh()开始。 

#### 1.1.3.3. 内置Tomcat
1. SpringBoot内置Tomcat，可以对比SpringBoot自动配置运行流程了解。  
2. Tomcat的自动装配：自动装配过程中，Web容器所对应的自动配置类为ServletWebServerFactoryAutoConfiguration，该类导入了EmbeddedTomcat，EmbeddedJetty，EmbeddedUndertow三个类，可以根据用户的需求去选择使用哪一个web服务器，默认情况下使用的是tomcat。  
3. Tomcat的启动：在容器刷新refreshContext(context)步骤完成。  

### 1.1.4. 自定义strater
1. 第一步，创建maven项目  
	1. 命名潜规则  
	&emsp; spring-boot-starter-XX是springboot官方的starter；XX-spring-boot-starter是第三方扩展的starter。
	2. 项目pom文件
2. `第二步，写自动配置逻辑`
	1. 编写业务逻辑  
	2. 定义配置文件对应类  
    	* @ConfigurationProperties 配置属性文件，需要指定前缀prefix。
    	* @EnableConfigurationProperties 启用配置，需要指定启用的配置类。
    	* @NestedConfigurationProperty 当一个类中引用了外部类，需要在该属性上加该注解。
	3. 定义自动配置类，自动暴露功能接口。  
        ```java
        @ConditionalOnProperty(prefix = "xxxxx", name = "enable",havingValue = "true", matchIfMissing = true)
        ```
3. 第三步，应用加载starter的配置，有两种方式：主动加载、被动加载。  


## 1.2. SpringCloud
### 1.2.1. SpringCloud组件
1. **SpringCloud子项目：**  
![image](http://182.92.69.8:8081/img/microService/SpringCloudNetflix/cloud-27.png)  
&emsp; Spring Cloud是一个微服务框架，相比Dubbo等RPC框架，Spring Cloud提供全套的分布式系统解决方案。Spring Cloud为微服务架构开发涉及的配置管理，服务治理，熔断机制，智能路由，微代理，控制总线，一次性token，全局一致性锁，leader选举，分布式session，集群状态管理等操作提供了一种简单的开发方式。   
&emsp; Spring Cloud对微服务基础框架Netflix的多个开源组件进行了封装，同时又实现了和云端平台以及和Spring Boot开发框架的集成。   

2. ★★★各组件作用  
    1. 注册中心，解决了服务之间的自动发现。  
    &emsp; 在没有注册中心时候，服务间调用需要知道被调方的地址或者代理地址。当服务更换部署地址，就不得不修改调用当中指定的地址或者修改代理配置。  
    &emsp; 而有了注册中心之后，每个服务在调用别人的时候只需要知道服务名称就好，继续地址都会通过注册中心同步过来。    
    2. 配置中心，每个服务都需要必要的配置信息才能运行，所以一套集中式的、动态的配置管理设施是必不可少的。  
    2. 服务分发(Ribbon)：客户端负载均衡，Ribbon中提供了多种负载均衡策略：轮询、随机、重试、权重等。  
    3. 声明式调用(OpenFeign)
    3. 服务熔断(Hystrix)，解决服务雪崩问题。    
    4. 链路监控(Sleuth)：如何快速定位请求异常；如何快速定位性能瓶颈；如何快速定位不合理调用。  
    5. 服务网关

3. ★★★**<font color = "clime">Spring Cloud各组件运行流程：</font>**  
    1. 外部或者内部的非Spring Cloud项目都统一通过微服务网关(Zuul)来访问内部服务。客户端的请求首先经过负载均衡(Ngnix)，再到达服务网关(Zuul集群)；  
    2. 网关接收到请求后，从注册中心(Eureka)获取可用服务；  
    3. 由Ribbon进行负载均衡后，分发到后端的具体实例；  
    4. 微服务之间也可通过Feign进行通信处理业务；  
    5. Hystrix负责处理服务超时熔断；Hystrix dashboard，Turbine负责监控Hystrix的熔断情况，并给予图形化的展示；  
    6. 服务的所有的配置文件由配置服务管理，配置服务的配置文件放在git仓库，方便开发人员随时改配置。  

### 1.2.2. 网关
#### 1.2.2.1. 网关介绍    
1. 为什么需要网关？  
&emsp; 传统的单体架构中只有一个服务开放给客户端调用，但是微服务架构中是将一个系统拆分成多个微服务，那么作为客户端如何去调用这些微服务呢？如果没有网关的存在，只能在本地记录每个微服务的调用地址。  
&emsp; 无网关的微服务架构往往存在以下问题：  
* 客户端多次请求不同的微服务，增加客户端代码或配置编写的复杂性。
* 认证复杂，每个服务都需要独立认证。
* 存在跨域请求，在一定场景下处理相对复杂。

2. 网关的基本功能  
&emsp; 网关是所有微服务的门户，路由转发仅仅是最基本的功能，除此之外还有其他的一些功能，比如：认证、鉴权、熔断、限流、日志监控等等.........  


#### 1.2.2.2. Spring Cloud Gateway
##### 介绍
1. Spring Cloud Gateway 具有如下特性：
    * 基于Spring Framework 5、Project Reactor 和 Spring Boot 2.0 进行构建；
    * 动态路由：能够匹配任何请求属性；  
    * 集成 Spring Cloud 服务发现功能；
    * 可以对路由指定 Predicate（断言）和 Filter（过滤器）；
    * 易于编写的 Predicate（断言）和 Filter（过滤器）；
    * 集成Hystrix的断路器功能；
    * 请求限流功能；
    * 支持路径重写。  

2. Spring Cloud Gateway重要概念？  
&emsp; 路由（route）：gateway的基本构建模块。它由ID、目标URI、断言集合和过滤器集合组成。如果聚合断言结果为真，则匹配到该路由。  
&emsp; &emsp; `集成注册中心实现负载`；
&emsp; &emsp; 实现动态路由。动态路由是与静态路由相对的一个概念，指路由器能够根据路由器之间的交换的特定路由信息自动地建立自己的路由表，并且能够根据链路和节点的变化适时地进行自动调整。当网络中节点或节点间的链路发生故障，或存在其它可用路由时，动态路由可以自行选择最佳的可用路由并继续转发报文  
&emsp; 断言（Predicate ）：参照Java8的新特性Predicate，允许开发人员匹配HTTP请求中的任何内容，比如头或参数。  
&emsp; 过滤器（filter）：可以在返回请求之前或之后修改请求和响应的内容。  

##### 路由
1. 配置  
&emsp; 1）id：我们自定义的路由 ID，保持唯一  
&emsp; 2）uri：目标服务地址  
&emsp; 3）predicates：路由条件，Predicate 接受一个输入参数，返回一个布尔值结果。该接口包含多种默认方法来将 Predicate 组合成其他复杂的逻辑（比如：与，或，非）。  
&emsp; 4）filters：过滤规则，暂时没用。  
&emsp; 上面这段配置的意思是，配置了一个id为product-service的路由规则，当访问网关请求地址以product开头时，会自动转发到地址：http://127.0.0.1:9002/。  

2. 路由规则介绍：  
&emsp; SpringCloudGateway的功能很强大，前面我们只是使用了predicates进行了简单的条件匹配，其实SpringCloudGataway帮我们内置了很多Predicates功能。在SpringCloudGateway中Spring利用Predicate的特性实现了各种路由匹配规则，有通过Header、请求参数等不同的条件来进行作为条件匹配到对应的路由。  

![image](http://182.92.69.8:8081/img/microService/SpringCloudNetflix/cloud-46.png)  


#### 1.2.2.3. Zuul
1. Spring Cloud Zuul，微服务网关，包含hystrix、ribbon、actuator。主要有路由转发、请求过滤功能。  
2. **<font color = "red">Zuul提供了四种过滤器的API，分别为前置（Pre）、路由（Route）、后置（Post）和错误（Error）四种处理方式。</font>**  
3. 动态加载：  
&emsp; 作为最外部的网关，它必须具备动态更新内部逻辑的能力，比如动态修改路由规则、动态添加／删除过滤器等。  
&emsp; 通过Zuul实现的API网关服务具备了动态路由和动态过滤器能力，可以在不重启API网关服务的前提下为其动态修改路由规则和添加或删除过滤器。   

### 1.2.3. 注册中心和配置中心
#### 1.2.3.1. 注册中心和配置中心介绍
1. 注册中心：  
    1. 为了解决微服务架构中的服务实例维护问题。  
    2. 包含服务注册和服务发现 
        1. 服务注册：当微服务均启动，并向注册中心注册自己的服务之后，注册中心就会维护类似下面的一个`服务清单`。另外，服务注册中心还需要以心跳的方式去监测清单中的服务是否可用，若不可用需要从服务清单中剔除，达到排除故障服务的效果。  
        2. 服务发现：调用方需要向服务注册中心咨询服务，并获取所有服务的实例清单，以实现对具体服务实例的访问。比如，现有服务C希望调用服务A，服务C就需要向注册中心发起咨询服务请求，服务注册中心就会将服务A的位置清单返回给服务C，如按上例服务A的情况，C便获得了服务A的两个可用位置192.168.0.100:8000和192.168.0.101:8000。当服务C要发起调用的时候，便从该清单中以某种轮询策略取出一个位置来进行服务调用。  
    
2. 配置中心： 
&emsp; 由于每个服务都需要必要的配置信息才能运行，所以一套集中式的、动态的配置管理设施是必不可少的。  

#### 注册中心宕机 

&emsp; 分3种场景来阐述注册中心宕机下服务的异常情况：    

* 新启动的服务下的异常  
* 运行中的服务的异常  
* 运行中且重启过的服务  

&emsp; 解决方案：修改内存中本地缓存保存到文件中。  

#### 1.2.3.2. Eureka
1. 服务治理框架和产品都围绕着服务注册与服务发现机制来完成对微服务应用实例的自动化管理。  
2. Spring Cloud Eureka，使用Netflix Eureka来实现服务注册与发现，它既包含了服务端组件，也包含了客户端组件。  
3. Eureka服务治理体系包含三个核心角色：服务注册中心、服务提供者以及服务消费者。  
    * 服务提供者  
    &emsp; 服务同步、服务续约。
    * 服务消费者    
    &emsp; 荻取服务、服务调用、服务下线。  
    &emsp; 服务下线：在系统运行过程中必然会面临关闭或重启服务的某个实例的情况，在服务关闭期间，不希望客户端会继续调用关闭了的实例。 所以<font color = "red">在客户端程序中，当服务实例进行正常的关闭操作时，它会触发一个服务下线的REST请求给Eurka Server，告诉服务注册中心：“我要下线了”。服务端在接收到请求之后，将该服务状态置为下线(DOWN)，并把该下线事件传播出去。</font>  
    * 服务注册中心  
        * 失效剔除  
        * 自我保护：  
        &emsp; 开启自我保护后，Eureka Server在运行期间，会统计心跳失败的比例在15分钟之内是否低于85%，如果出现低于的情况（在单机调试的时候很容易满足，实际在生产环境上通常是由于网络不稳定导致），Eureka Server会将当前的实例注册信息保护起来，让这些实例不会过期，尽可能保护这些注册信息。  
        &emsp; Eureka Server进入自我保护状态后，客户端很容易拿到实际已经不存在的服务实例，会出现调用失败的清况。  
4. <font color = "red">高可用注册中心/AP模型：EurekaServer的高可用实际上就是将自己作为服务向其他服务注册中心注册自己，这样就可以形成一组互相注册的服务注册中心，以实现服务清单的互相同步，达到高可用的效果。</font>  


### 1.2.4. Ribbon
&emsp; 负载均衡大体可以分为两类：集中式、进程内。前者也被称为服务端负载均衡，其一般位于服务集群的前端，统一接收、处理、转发客户端的请求。典型的包括F5硬件、LVS、Nginx等技术方案；而后者也被称为客户端负载均衡，其是在客户端侧根据某种策略选择合适的服务实例直接进行请求，其典型代表有Ribbon。  

### 1.2.5. Feign


### 1.2.6. Hytrix
1. <font color = "red">小结：</font>服务雪崩（为什么要有熔断？） ---> Hystrix工作流程 ---> fallback处理（数量、时间、错误率） ---> 线程池隔离与信号量隔离触发降级。   
1. `服务雪崩（为什么要有熔断？）`：在微服务架构中，存在着那么多的服务单元，若一个单元出现故障，就很容易因依赖关系而引发故障的蔓延，最终导致整个系统的瘫痪。  
2. `Hystrix工作流程`：1. 包装请求 ---> 2. 发起请求 ---> 3. 缓存处理 ---> 4. 判断断路器是否打开（熔断） ---> 5. 判断是否进行业务请求（请求是否需要隔离或降级） ---> 6. 执行业务请求 ---> 7. 健康监测 ---> 8. `fallback处理`或返回成功的响应。  
3. `fallback处理（数量、时间、错误率）`：熔断是一种[降级](/docs/microService/thinking/Demotion.md)策略。Hystrix中的降级方案：熔断触发降级、请求超时触发降级、`资源（信号量、线程池）`隔离触发降级 / 依赖隔离。（<font color = "clime">熔断的对象是服务之间的请求；`熔断策略有根据请求的【数量】分为信号量和线程池，还有请求的【时间】（即超时熔断），【请求错误率】（即熔断触发降级）。`</font>）  
4. `线程池隔离与信号量隔离触发降级`：  
  1. 线程池隔离：请求线程和每个服务单独用线程池。  
  &emsp; 比如现在有3个业务调用分别是查询订单、查询商品、查询用户，且这三个业务请求都是依赖第三方服务-订单服务、商品服务、用户服务。`为每一个服务接口单独开辟一个线程池，`保持与其他服务接口线程的隔离，提高该服务接口的独立性和高可用。   
    1. 优点：  
      &emsp; 使用线程池隔离可以完全隔离依赖的服务，请求线程可以快速放回。  
      &emsp; 当线程池出现问题时，线程池隔离是独立的，不会影响其他服务和接口。  
      &emsp; 当失败的服务再次变得可用时，线程池将清理并可立即恢复，而不需要一个长时间的恢复。  
      &emsp; 独立的线程池提高了并发性。   
    2. 缺点：  
      &emsp; 线程池隔离的主要缺点是它们增加计算开销（CPU）。每个命令的执行涉及到排队、调度和上下文切换都是在一个单独的线程上运行的。    
  2. ~~线程池隔离：~~  
    1. 调用线程和hystrixCommand线程不是同一个线程，并发请求数受到线程池（不是容器tomcat的线程池，而是hystrixCommand所属于线程组的线程池）中的线程数限制，默认是10。
    2. 这个是默认的隔离机制
    3. hystrixCommand线程无法获取到调用线程中的ThreadLocal中的值
  3. 信号量隔离：
    1. 调用线程和hystrixCommand线程是同一个线程，默认最大并发请求数是10  
    2. 调用数度快，开销小，由于和调用线程是处于同一个线程，所以必须确保调用的微服务可用性足够高并且返回快才用  
  4. 什么情况下使用线程池隔离/信号量隔离？  
    * 请求并发量大，并且耗时长（请求耗时长一般是计算量大，或读数据库）：采用线程池隔离策略，这样的话，可以保证大量的容器（tomcat）线程可用，不会由于服务原因，一直处于阻塞或等待状态，快速失败返回。  
    * 请求并发量大，并且耗时短（请求耗时长一般是计算量大，或读缓存）：采用信号量隔离策略，因为这类服务的返回通常会非常的快，不会占用容器线程太长时间，而且也减少了线程切换的一些开销，提高了缓存服务的效率。  
5. <font color = "clime">微服务集群中，Hystrix的度量信息通过`Turbine`来汇集监控信息，并将聚合后的信息提供给Hystrix Dashboard来集中展示和监控。</font>  

### 1.2.7. Sleuth
1. 分布式调用链追踪系统，可以解决的问题：  
    **<font color = "red">(1) `如何快速定位请求异常；`</font>**    
    **<font color = "red">(2) `如何快速定位性能瓶颈；`</font>**  
    **<font color = "red">(3) 如何快速定位不合理调用；</font>**  
2. **<font color = "red">埋点日志通常包含：traceId、spanId、调用的开始时间，协议类型、调用方ip和端口，请求的服务名、调用耗时，调用结果，异常信息等，同时预留可扩展字段，为下一步扩展做准备；</font>**  
3. spring-cloud-starter-sleuth功能点：
    1. 有关traceId：获取当前traceId、日志获取traceId、传递traceId到异步线程池、子线程或线程池中获取Zipkin traceId并打印。  
    2. sleuth自定义信息。增加自定义属性、添加自定义Tag标签。  
    3. 监控本地方法：异步执行和远程调用都会新开启一个Span，如果想监控本地的方法耗时时间，可以采用埋点的方式监控本地方法，也就是开启一个新的Span。  
    4. ...  

### 1.2.8. Admin
1. spring-boot-starter-actuator依赖中已经实现的一些原生端点。根据端点的作用，<font color = "clime">可以将原生端点分为以下三大类：</font>  
    * 应用配置类：获取应用程序中加载的应用配置、环境变量、自动化配置报告等与Spring Boot应用密切相关的配置类信息。  
    * 度量指标类：获取应用程序运行过程中用于监控的度量指标，比如内存信息、线程池信息、HTTP请求统计等。  
    * 操作控制类：提供了对应用的关闭等操作类功能。  
2. 监控通知
3. 集成spring security


