---
title: run()方法运行
date: 2020-04-14 00:00:00
tags:
    - SpringBoot
---

<!-- TOC -->

- [1. SpringApplication实例run()方法运行过程](#1-springapplication实例run方法运行过程)
    - [1.1. 关键流程解析](#11-关键流程解析)
        - [1.1.1. 步骤3：设置系统属性java.awt.headless的值](#111-步骤3设置系统属性javaawtheadless的值)
        - [1.1.2. 步骤6：根据运行监听器和应用参数来准备 Spring 环境](#112-步骤6根据运行监听器和应用参数来准备-spring-环境)
        - [1.1.3. 步骤8：准备应用上下文](#113-步骤8准备应用上下文)

<!-- /TOC -->

# 1. SpringApplication实例run()方法运行过程
&emsp; 进入SpringApplication#run方法中，一路点击#run方法。  

<!-- 
```java
public ConfigurableApplicationContext run(String... args) {

    // 1、创建并启动计时监控类
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();//首先记录了当前任务的名称，默认为空字符串，然后记录当前 Spring Boot 应用启动的开始时间
    // 2、初始化应用上下文和异常报告集合
    ConfigurableApplicationContext context = null;
    Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList();
    // 3、设置系统属性 `java.awt.headless` 的值，默认值为：true
    this.configureHeadlessProperty();
    // 4、创建所有 Spring 运行监听器并发布应用启动事件
    // 从类路径下找到 META/INF/Spring.factories 获取 SpringApplicationRunListeners
    SpringApplicationRunListeners listeners = this.getRunListeners(args);
    // 回调所有 SpringApplicationRunListeners 的 starting() 方法
    listeners.starting();
    Collection exceptionReporters;
    try {
        // 5、初始化默认应用参数类，封装命令行参数
        ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        // 6、根据运行监听器和应用参数来准备 Spring 环境
        //包括创建环境，创建环境完成后回调 SpringApplicationRunListeners#environmentPrepared()方法，表示环境准备完成
        ConfigurableEnvironment environment = this.prepareEnvironment(listeners, applicationArguments);
        this.configureIgnoreBeanInfo(environment);
        // 7、打印 Banner
        Banner printedBanner = this.printBanner(environment);
        // 8、创建应用上下文（决定创建 web 的 IOC 容器还是普通的 IOC 容器）
            //根据不同的应用类型初始化不同的上下文应用类
        context = this.createApplicationContext();
        // 9、准备异常报告器
        exceptionReporters = this.getSpringFactoriesInstances(SpringBootExceptionReporter.class, new Class[]{ConfigurableApplicationContext.class}, context);
        // 10、准备应用上下文
        /*
         * 将 environment 保存到 IOC 容器中，并且调用 applyInitializers() 方法
         * applyInitializers() 方法回调之前保存的所有的 ApplicationContextInitializer 的 initialize() 方法
         * 然后回调所有的 SpringApplicationRunListener#contextPrepared() 方法
         * 最后回调所有的 SpringApplicationRunListener#contextLoaded() 方法
         */
        this.prepareContext(context, environment, listeners, applicationArguments, printedBanner);
        // 11、刷新应用上下文，IOC 容器初始化（如果是 Web 应用还会创建嵌入式的 Tomcat），扫描、创建、加载所有组件
        this.refreshContext(context);
        // 12、应用上下文刷新后置处理，从 IOC 容器中获取所有的 ApplicationRunner 和 CommandLineRunner 进行回调
        this.afterRefresh(context, applicationArguments);
        // 13、停止计时监控类
        stopWatch.stop();
        // 14、输出日志记录执行主类名、时间信息
        if (this.logStartupInfo) {
            (new StartupInfoLogger(this.mainApplicationClass)).logStarted(this.getApplicationLog(), stopWatch);
        }
        // 15、发布应用上下文启动完成事件，调用所有SpringApplicationRunListeners#started()方法
        listeners.started(context);
        // 16、执行ApplicationRunner和CommandLineRunner两种运行器
        this.callRunners(context, applicationArguments);
    } catch (Throwable var10) {
        this.handleRunFailure(context, var10, exceptionReporters, listeners);
        throw new IllegalStateException(var10);
    }
    try {
        // 17、发布应用上下文就绪事件，触发所有SpringApplicationRunListener 监听器的running事件方法。
        listeners.running(context);
        // 18、返回应用上下文
        return context;
    } catch (Throwable var9) {
        this.handleRunFailure(context, var9, exceptionReporters, (SpringApplicationRunListeners)null);
        throw new IllegalStateException(var9);
    }
}
```
-->

```
// SpringApplication.java

public ConfigurableApplicationContext run(String... args) {
	// new 一个StopWatch用于统计run启动过程花了多少时间
	StopWatch stopWatch = new StopWatch();
	// 开始计时
	stopWatch.start();
	ConfigurableApplicationContext context = null;
	// exceptionReporters集合用来存储异常报告器，用来报告SpringBoot启动过程的异常
	Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
	// 配置headless属性，即“java.awt.headless”属性，默认为ture
	// 其实是想设置该应用程序,即使没有检测到显示器,也允许其启动.对于服务器来说,是不需要显示器的,所以要这样设置.
	configureHeadlessProperty();
	// 【1】从spring.factories配置文件中加载到EventPublishingRunListener对象并赋值给SpringApplicationRunListeners
	// EventPublishingRunListener对象主要用来发射SpringBoot启动过程中内置的一些生命周期事件，标志每个不同启动阶段
	SpringApplicationRunListeners listeners = getRunListeners(args);
	// 启动SpringApplicationRunListener的监听，表示SpringApplication开始启动。
	// 》》》》》发射【ApplicationStartingEvent】事件
	listeners.starting();
	try {
		// 创建ApplicationArguments对象，封装了args参数
		ApplicationArguments applicationArguments = new DefaultApplicationArguments(
				args);
		// 【2】准备环境变量，包括系统变量，环境变量，命令行参数，默认变量，servlet相关配置变量，随机值，
		// JNDI属性值，以及配置文件（比如application.properties）等，注意这些环境变量是有优先级的
		// 》》》》》发射【ApplicationEnvironmentPreparedEvent】事件
		ConfigurableEnvironment environment = prepareEnvironment(listeners,
				applicationArguments);
		// 配置spring.beaninfo.ignore属性，默认为true，即跳过搜索BeanInfo classes.
		configureIgnoreBeanInfo(environment);
		// 【3】控制台打印SpringBoot的bannner标志
		Banner printedBanner = printBanner(environment);
		// 【4】根据不同类型创建不同类型的spring applicationcontext容器
		// 因为这里是servlet环境，所以创建的是AnnotationConfigServletWebServerApplicationContext容器对象
		context = createApplicationContext();
		// 【5】从spring.factories配置文件中加载异常报告期实例，这里加载的是FailureAnalyzers
		// 注意FailureAnalyzers的构造器要传入ConfigurableApplicationContext，因为要从context中获取beanFactory和environment
		exceptionReporters = getSpringFactoriesInstances(
				SpringBootExceptionReporter.class,
				new Class[] { ConfigurableApplicationContext.class }, context); // ConfigurableApplicationContext是AnnotationConfigServletWebServerApplicationContext的父接口
		// 【6】为刚创建的AnnotationConfigServletWebServerApplicationContext容器对象做一些初始化工作，准备一些容器属性值等
		// 1）为AnnotationConfigServletWebServerApplicationContext的属性AnnotatedBeanDefinitionReader和ClassPathBeanDefinitionScanner设置environgment属性
		// 2）根据情况对ApplicationContext应用一些相关的后置处理，比如设置resourceLoader属性等
		// 3）在容器刷新前调用各个ApplicationContextInitializer的初始化方法，ApplicationContextInitializer是在构建SpringApplication对象时从spring.factories中加载的
		// 4）》》》》》发射【ApplicationContextInitializedEvent】事件，标志context容器被创建且已准备好
		// 5）从context容器中获取beanFactory，并向beanFactory中注册一些单例bean，比如applicationArguments，printedBanner
		// 6）TODO 加载bean到application context，注意这里只是加载了部分bean比如mainApplication这个bean，大部分bean应该是在AbstractApplicationContext.refresh方法中被加载？这里留个疑问先
		// 7）》》》》》发射【ApplicationPreparedEvent】事件，标志Context容器已经准备完成
		prepareContext(context, environment, listeners, applicationArguments,
				printedBanner);
		// 【7】刷新容器，这一步至关重要，以后会在分析Spring源码时详细分析，主要做了以下工作：
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
		// 【8】执行刷新容器后的后置处理逻辑，注意这里为空方法
		afterRefresh(context, applicationArguments);
		// 停止stopWatch计时
		stopWatch.stop();
		// 打印日志
		if (this.logStartupInfo) {
			new StartupInfoLogger(this.mainApplicationClass)
					.logStarted(getApplicationLog(), stopWatch);
		}
		// 》》》》》发射【ApplicationStartedEvent】事件，标志spring容器已经刷新，此时所有的bean实例都已经加载完毕
		listeners.started(context);
		// 【9】调用ApplicationRunner和CommandLineRunner的run方法，实现spring容器启动后需要做的一些东西比如加载一些业务数据等
		callRunners(context, applicationArguments);
	}
	// 【10】若启动过程中抛出异常，此时用FailureAnalyzers来报告异常
	// 并》》》》》发射【ApplicationFailedEvent】事件，标志SpringBoot启动失败
	catch (Throwable ex) {
		handleRunFailure(context, ex, exceptionReporters, listeners);
		throw new IllegalStateException(ex);
	}

	try {
		// 》》》》》发射【ApplicationReadyEvent】事件，标志SpringApplication已经正在运行即已经成功启动，可以接收服务请求了。
		listeners.running(context);
	}
	// 若出现异常，此时仅仅报告异常，而不会发射任何事件
	catch (Throwable ex) {
		handleRunFailure(context, ex, exceptionReporters, null);
		throw new IllegalStateException(ex);
	}
	// 【11】最终返回容器
	return context;
}
```
&emsp; 主要步骤总结如下：  
1. 从spring.factories配置文件中加载EventPublishingRunListener对象，该对象拥有SimpleApplicationEventMulticaster属性，即在SpringBoot启动过程的不同阶段用来发射内置的生命周期事件;  
2. 准备环境变量，包括系统变量，环境变量，命令行参数，默认变量，servlet相关配置变量，随机值以及配置文件（比如application.properties）等;
3. 控制台打印SpringBoot的bannner标志；  
4. 根据不同类型环境创建不同类型的applicationcontext容器，因为这里是servlet环境，所以创建的是AnnotationConfigServletWebServerApplicationContext容器对象；  
5. 从spring.factories配置文件中加载FailureAnalyzers对象,用来报告SpringBoot启动过程中的异常；  
6. 为刚创建的容器对象做一些初始化工作，准备一些容器属性值等，对ApplicationContext应用一些相关的后置处理和调用各个ApplicationContextInitializer的初始化方法来执行一些初始化逻辑等；  
7. 刷新容器，重要的一步。<font color = "red">比如调用bean factory的后置处理器，注册BeanPostProcessor后置处理器，初始化事件广播器且广播事件，初始化剩下的单例bean和SpringBoot创建内嵌的Tomcat服务器等等重要且复杂的逻辑都在这里实现；</font>  
8. 执行刷新容器后的后置处理逻辑，注意这里为空方法；  
9. 调用ApplicationRunner和CommandLineRunner的run方法，我们实现这两个接口可以在spring容器启动后需要的一些东西比如加载一些业务数据等;  
10. 报告启动异常，即若启动过程中抛出异常，此时用FailureAnalyzers来报告异常;  
11. 最终返回容器对象，这里调用方法没有声明对象来接收。  

        将关键步骤再浓缩总结下：  
        1. 构建SpringApplication对象，用于启动SpringBoot；  
        2. 从spring.factories配置文件中加载EventPublishingRunListener对象用于在不同的启动阶段发射不同的生命周期事件；  
        3. 准备环境变量，包括系统变量，环境变量，命令行参数及配置文件（比如application.properties）等；  
        4. 创建容器ApplicationContext;  
        5. 为第4步创建的容器对象做一些初始化工作，准备一些容器属性值等，同时调用各个ApplicationContextInitializer的初始化方法来执行一些初始化逻辑等；  
        6. 刷新容器，这一步至关重要，是重点中的重点，太多复杂逻辑在这里实现；  
        7. 调用ApplicationRunner和CommandLineRunner的run方法，可以实现这两个接口在容器启动后来加载一些业务数据等;  


&emsp; <font color = "red">在SpringBoot启动过程中，每个不同的启动阶段会分别发射不同的内置生命周期事件。</font>比如在准备environment前会发射ApplicationStartingEvent事件，在environment准备好后会发射ApplicationEnvironmentPreparedEvent事件，在刷新容器前会发射ApplicationPreparedEvent事件等，总之SpringBoot总共内置了7个生命周期事件，除了标志SpringBoot的不同启动阶段外。<font color = "red">同时一些监听器也会监听相应的生命周期事件从而执行一些启动初始化逻辑。</font>比如ConfigFileApplicationListener会监听onApplicationEnvironmentPreparedEvent事件来加载环境变量等。  
<!-- 
&emsp; 在SpringBoot启动过程中，每个不同的启动阶段会分别发射不同的内置生命周期事件，然后相应的监听器会监听这些事件来执行一些初始化逻辑工作比如ConfigFileApplicationListener会监听onApplicationEnvironmentPreparedEvent事件来加载环境变量等。  
-->
    run() 阶段主要是回调4个监听器(ApplicationContextInitializer、ApplicationRunner、CommandLineRunner、SpringApplicationRunListener)中的方法与加载项目中组件到 IOC 容器中，而所有需要回调的监听器都是从类路径下的 META/INF/Spring.factories 中获取，从而达到启动前后的各种定制操作。  

## 1.1. 关键流程解析  
&emsp; 从上述流程中，挑以下几个进行分析。

### 1.1.1. 步骤3：设置系统属性java.awt.headless的值  

```java
this.configureHeadlessProperty();
```
&emsp; 设置该默认值为：true，Java.awt.headless = true 有什么作用？  
&emsp; 对于一个Java服务器来说经常要处理一些图形元素，例如地图的创建或者图形和图表等。这些API基本上总是需要运行一个X-server以便能使用AWT（Abstract Window Toolkit，抽象窗口工具集）。然而运行一个不必要的 X-server 并不是一种好的管理方式。有时你甚至不能运行 X-server,因此最好的方案是运行 headless 服务器，来进行简单的图像处理。  
&emsp; 参考：www.cnblogs.com/princessd8251/p/4000016.html  

### 1.1.2. 步骤6：根据运行监听器和应用参数来准备 Spring 环境  

```java
ConfigurableEnvironment environment = this.prepareEnvironment(listeners, applicationArguments);
this.configureIgnoreBeanInfo(environment);
```
&emsp; this.prepareEnvironment()源码：  

```java
private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments) {
    // 6.1) 获取（或者创建）应用环境
        //分为标准 Servlet 环境和标准环境
    ConfigurableEnvironment environment = this.getOrCreateEnvironment();
    // 6.2) 配置应用环境
        // 配置 property sources
        //配置 Profiles
    this.configureEnvironment((ConfigurableEnvironment)environment, applicationArguments.getSourceArgs());
    ConfigurationPropertySources.attach((Environment)environment);
    listeners.environmentPrepared((ConfigurableEnvironment)environment);
    this.bindToSpringApplication((ConfigurableEnvironment)environment);
    if (!this.isCustomEnvironment) {
        environment = (new EnvironmentConverter(this.getClassLoader())).convertEnvironmentIfNecessary((ConfigurableEnvironment)environment, this.deduceEnvironmentClass());
    }

    ConfigurationPropertySources.attach((Environment)environment);
    return (ConfigurableEnvironment)environment;
}
```

### 1.1.3. 步骤8：准备应用上下文  

```java
this.prepareContext(context, environment, listeners, applicationArguments, printedBanner);
```
&emsp; this.prepareContext()源码：  

```java
private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment, SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
    //绑定环境到上下文
    context.setEnvironment(environment);
    //配置上下文的 bean 生成器及资源加载器
    this.postProcessApplicationContext(context);
    //为上下文应用所有初始化器
    this.applyInitializers(context);
    //触发所有 SpringApplicationRunListener 监听器的 contextPrepared 事件方法
    listeners.contextPrepared(context);
    //记录启动日志
    if (this.logStartupInfo) {
        this.logStartupInfo(context.getParent() == null);
        this.logStartupProfileInfo(context);
    }
    //注册两个特殊的单例bean
    ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
    beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
    if (printedBanner != null) {
        beanFactory.registerSingleton("springBootBanner", printedBanner);
    }

    if (beanFactory instanceof DefaultListableBeanFactory) {
        ((DefaultListableBeanFactory)beanFactory).setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
    }

    if (this.lazyInitialization) {
        context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
    }
    //加载所有资源
    Set<Object> sources = this.getAllSources();
    Assert.notEmpty(sources, "Sources must not be empty");
    this.load(context, sources.toArray(new Object[0]));
    //触发所有 SpringApplicationRunListener 监听器的 contextLoaded 事件方法
    listeners.contextLoaded(context);
}
```

 



