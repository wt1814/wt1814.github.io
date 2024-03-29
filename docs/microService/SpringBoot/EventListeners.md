

<!-- TOC -->

- [1. SpringBoot事件监听机制](#1-springboot事件监听机制)
    - [1.1. 引言](#11-引言)
    - [1.2. SpringBoot事件监听机制分析](#12-springboot事件监听机制分析)
    - [1.3. SpringBoot广播内置生命周期事件流程分析](#13-springboot广播内置生命周期事件流程分析)
        - [1.3.1. 为广播SpringBoot内置生命周期事件做前期准备](#131-为广播springboot内置生命周期事件做前期准备)
            - [1.3.1.1. 加载ApplicationListener监听器实现类](#1311-加载applicationlistener监听器实现类)
            - [1.3.1.2. 加载SPI扩展类EventPublishingRunListener](#1312-加载spi扩展类eventpublishingrunlistener)
        - [1.3.2. 广播SpringBoot的内置生命周期事件](#132-广播springboot的内置生命周期事件)
    - [1.4. SpringBoot内置生命周期事件详解](#14-springboot内置生命周期事件详解)

<!-- /TOC -->

# 1. SpringBoot事件监听机制  
![image](http://182.92.69.8:8081/img/sourceCode/springBoot/boot-11.png)  

## 1.1. 引言  
&emsp; <font color = "red">在SpringBoot启动过程中，每个不同的启动阶段会分别广播不同的内置生命周期事件，然后相应的监听器会监听这些事件来执行一些初始化逻辑工作。</font>比如ConfigFileApplicationListener会监听onApplicationEnvironmentPreparedEvent事件，来加载配置文件application.properties的环境变量等。  

## 1.2. SpringBoot事件监听机制分析  
&emsp; **<font color = "clime">SpringBoot启动时广播生命周期事件步骤：</font>**    
1. 为广播SpringBoot内置生命周期事件做前期准备：    
    1. 首先加载ApplicationListener监听器实现类；  
    2. 其次加载SPI扩展类EventPublishingRunListener。  
2. SpringBoot启动时利用EventPublishingRunListener广播生命周期事件，然后ApplicationListener监听器实现类监听相应的生命周期事件执行一些初始化逻辑的工作。  
    1. 构建SpringApplication对象时<font color = "red">根据ApplicationListener接口去spring.factories配置文件中加载并实例化监听器。</font>  
    2. 在SpringBoot的启动过程中首先从spring.factories配置文件中加载并实例化EventPublishingRunListener对象。  
    3. 在SpringBoot的启动过程中会发布一系列SpringBoot内置的生命周期事件。  

## 1.3. SpringBoot广播内置生命周期事件流程分析  
&emsp; SpringBoot的启动流程：  

```java
// SpringApplication.java

public ConfigurableApplicationContext run(String... args) {
 StopWatch stopWatch = new StopWatch();
 stopWatch.start();
 ConfigurableApplicationContext context = null;
 Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
 configureHeadlessProperty();
 // 【0】新建一个SpringApplicationRunListeners对象用于发布SpringBoot启动过程中的生命周期事件
 SpringApplicationRunListeners listeners = getRunListeners(args);
 // 【1】》》》》》发布【ApplicationStartingEvent】事件，标志SpringApplication开始启动
 listeners.starting();
 try {
  ApplicationArguments applicationArguments = new DefaultApplicationArguments(
    args);
  // 【2】》》》》》发布【ApplicationEnvironmentPreparedEvent】事件，此时会去加载application.properties等配置文件的环境变量，同时也有标志环境变量已经准备好的意思
  ConfigurableEnvironment environment = prepareEnvironment(listeners,
    applicationArguments);
  configureIgnoreBeanInfo(environment);
  Banner printedBanner = printBanner(environment);
  context = createApplicationContext();
  exceptionReporters = getSpringFactoriesInstances(
    SpringBootExceptionReporter.class,
    new Class[] { ConfigurableApplicationContext.class }, context); 
  // 【3】》》》》》发布【ApplicationContextInitializedEvent】事件，标志context容器被创建且已准备好
  // 【4】》》》》》发布【ApplicationPreparedEvent】事件，标志Context容器已经准备完成
  prepareContext(context, environment, listeners, applicationArguments,
    printedBanner);
  refreshContext(context);
  afterRefresh(context, applicationArguments);
  stopWatch.stop();
  if (this.logStartupInfo) {
   new StartupInfoLogger(this.mainApplicationClass)
     .logStarted(getApplicationLog(), stopWatch);
  }
  // 【5】》》》》》发布【ApplicationStartedEvent】事件，标志spring容器已经刷新，此时所有的bean实例都已经加载完毕
  listeners.started(context);
  callRunners(context, applicationArguments);
 }
 // 【6】》》》》》发布【ApplicationFailedEvent】事件，标志SpringBoot启动失败
 catch (Throwable ex) {
  handleRunFailure(context, ex, exceptionReporters, listeners);
  throw new IllegalStateException(ex);
 }
 try {
  // 【7】》》》》》发布【ApplicationReadyEvent】事件，标志SpringApplication已经正在运行即已经成功启动，可以接收服务请求了。
  listeners.running(context);
 }
 catch (Throwable ex) {
  handleRunFailure(context, ex, exceptionReporters, null);
  throw new IllegalStateException(ex);
 }
 return context;
}
```
&emsp; <font color = "red">SpringBoot在启动过程中首先会先新建一个SpringApplicationRunListeners对象用于发布SpringBoot启动过程中的各种生命周期事件</font>，比如发布ApplicationStartingEvent，ApplicationEnvironmentPreparedEvent和ApplicationContextInitializedEvent等事件，<font color = "red">然后相应的监听器会执行一些SpringBoot启动过程中的初始化逻辑。</font>  
&emsp; 监听这些SpringBoot的生命周期事件的监听器是何时被加载实例化的呢？<font color = "red">这些执行初始化逻辑的监听器是在SpringApplication的构建过程中，根据ApplicationListener接口去spring.factories配置文件中加载并实例化的。</font>  

### 1.3.1. 为广播SpringBoot内置生命周期事件做前期准备  
#### 1.3.1.1. 加载ApplicationListener监听器实现类  
&emsp; <font color = "red">构建SpringApplication对象时，`this.setListeners(this.getSpringFactoriesInstances(ApplicationListener.class))`，从spring.factories中加载出ApplicationListener事件监听接口的SPI扩展实现类然后添加到SpringApplication对象的listeners集合中，用于后续监听SpringBoot启动过程中的事件，来执行一些初始化逻辑工作。</font>  
&emsp; SpringBoot启动时的具体监听器都实现了ApplicationListener接口，其在spring.factories部分配置如下：  
![image](http://182.92.69.8:8081/img/sourceCode/springBoot/boot-8.png)  
&emsp; 在调试时，会从所有的spring.factories配置文件中加载监听器，最终加载了10个监听器。如下图：  
![image](http://182.92.69.8:8081/img/sourceCode/springBoot/boot-9.png)  

#### 1.3.1.2. 加载SPI扩展类EventPublishingRunListener  
&emsp; <font color = "red">在SpringBoot的启动过程run()方法中首先会先新建一个SpringApplicationRunListeners对象用于发布SpringBoot启动过程中的生命周期事件。</font>  
&emsp; SpringApplicationRunListeners listeners = getRunListeners(args)代码解读：  

```java
// SpringApplication.java

private SpringApplicationRunListeners getRunListeners(String[] args) {
 // 构造一个由SpringApplication.class和String[].class组成的types
 Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
 // 1) 根据SpringApplicationRunListener接口去spring.factories配置文件中加载其SPI扩展实现类
 // 2) 构建一个SpringApplicationRunListeners对象并返回
 return new SpringApplicationRunListeners(logger, getSpringFactoriesInstances(
   SpringApplicationRunListener.class, types, this, args));
}
```
&emsp; getSpringFactoriesInstances()方法，SpringBoot根据SpringApplicationRunListener这个SPI接口去spring.factories中加载相应的SPI扩展实现类。spring.factories中SpringApplicationRunListener有以下SPI实现类：  
![image](http://182.92.69.8:8081/img/sourceCode/springBoot/boot-10.png)  
&emsp; 由上图可以看到，SpringApplicationRunListener只有EventPublishingRunListener这个SPI实现类 EventPublishingRunListener在SpringBoot的启动过程中尤其重要，由其在SpringBoot启动过程的不同阶段发布不同的SpringBoot的生命周期事件，即SpringApplicationRunListeners对象没有承担广播事件的职责，而最终是委托EventPublishingRunListener来广播事件的。  
&emsp; 从spring.factories中加载EventPublishingRunListener类后还会实例化该类。EventPublishingRunListener类源码如下，了解其如何承担发布SpringBoot生命周期事件这一职责的。    

```java
// EventPublishingRunListener.java

public class EventPublishingRunListener implements SpringApplicationRunListener, Ordered {

 private final SpringApplication application;

 private final String[] args;
 /**
  * 拥有一个SimpleApplicationEventMulticaster事件广播器来广播事件
  */
 private final SimpleApplicationEventMulticaster initialMulticaster;

 public EventPublishingRunListener(SpringApplication application, String[] args) {
  this.application = application;
  this.args = args;
  // 新建一个事件广播器SimpleApplicationEventMulticaster对象
  this.initialMulticaster = new SimpleApplicationEventMulticaster();
  // 遍历在构造SpringApplication对象时从spring.factories配置文件中获取的事件监听器
  for (ApplicationListener<?> listener : application.getListeners()) {
   // 将从spring.factories配置文件中获取的事件监听器们添加到事件广播器initialMulticaster对象的相关集合中
   this.initialMulticaster.addApplicationListener(listener);
  }
 }

 @Override
 public int getOrder() {
  return 0;
 }
 // 》》》》》发布【ApplicationStartingEvent】事件
 @Override
 public void starting() {
  this.initialMulticaster.multicastEvent(
    new ApplicationStartingEvent(this.application, this.args));
 }
 // 》》》》》发布【ApplicationEnvironmentPreparedEvent】事件
 @Override
 public void environmentPrepared(ConfigurableEnvironment environment) {
  this.initialMulticaster.multicastEvent(new ApplicationEnvironmentPreparedEvent(
    this.application, this.args, environment));
 }
 // 》》》》》发布【ApplicationContextInitializedEvent】事件
 @Override
 public void contextPrepared(ConfigurableApplicationContext context) {
  this.initialMulticaster.multicastEvent(new ApplicationContextInitializedEvent(
    this.application, this.args, context));
 }
 // 》》》》》发布【ApplicationPreparedEvent】事件
 @Override
 public void contextLoaded(ConfigurableApplicationContext context) {
  for (ApplicationListener<?> listener : this.application.getListeners()) {
   if (listener instanceof ApplicationContextAware) {
    ((ApplicationContextAware) listener).setApplicationContext(context);
   }
   context.addApplicationListener(listener);
  }
  this.initialMulticaster.multicastEvent(
    new ApplicationPreparedEvent(this.application, this.args, context));
 }
 // 》》》》》发布【ApplicationStartedEvent】事件
 @Override
 public void started(ConfigurableApplicationContext context) {
  context.publishEvent(
    new ApplicationStartedEvent(this.application, this.args, context));
 }
 // 》》》》》发布【ApplicationReadyEvent】事件
 @Override
 public void running(ConfigurableApplicationContext context) {
  context.publishEvent(
    new ApplicationReadyEvent(this.application, this.args, context));
 }
 // 》》》》》发布【ApplicationFailedEvent】事件
 @Override
 public void failed(ConfigurableApplicationContext context, Throwable exception) {
  ApplicationFailedEvent event = new ApplicationFailedEvent(this.application,
    this.args, context, exception);
  if (context != null && context.isActive()) {
   // Listeners have been registered to the application context so we should
   // use it at this point if we can
   context.publishEvent(event);
  }
  else {
   // An inactive context may not have a multicaster so we use our multicaster to
   // call all of the context's listeners instead
   if (context instanceof AbstractApplicationContext) {
    for (ApplicationListener<?> listener : ((AbstractApplicationContext) context)
      .getApplicationListeners()) {
     this.initialMulticaster.addApplicationListener(listener);
    }
   }
   this.initialMulticaster.setErrorHandler(new LoggingErrorHandler());
   this.initialMulticaster.multicastEvent(event);
  }
 }
 
 // ...省略非关键代码
}
```
&emsp; EventPublishingRunListener类实现了SpringApplicationRunListener接口，SpringApplicationRunListener接口定义了SpringBoot启动时发布生命周期事件的接口方法，而EventPublishingRunListener类正是通过实现SpringApplicationRunListener接口的starting，environmentPrepared和contextPrepared等方法来广播SpringBoot不同的生命周期事件，SpringApplicationRunListener接口源码如下：  

```java
// SpringApplicationRunListener.java

public interface SpringApplicationRunListener {

 void starting();
 void environmentPrepared(ConfigurableEnvironment environment);
 void contextPrepared(ConfigurableApplicationContext context);
 void contextLoaded(ConfigurableApplicationContext context);
 void started(ConfigurableApplicationContext context);
 void running(ConfigurableApplicationContext context);
 void failed(ConfigurableApplicationContext context, Throwable exception);
 
}
```
&emsp; EventPublishingRunListener类中有一个重要的成员属性initialMulticaster，该成员属性是SimpleApplicationEventMulticaster类对象，该类正是承担了广播SpringBoot启动时生命周期事件的职责，即EventPublishingRunListener对象没有承担广播事件的职责，而最终是委托SimpleApplicationEventMulticaster来广播事件的。 从EventPublishingRunListener的源码中也可以看到在starting,environmentPrepared和contextPrepared等方法中也正是通过调用SimpleApplicationEventMulticaster类对象的multicastEvent方法来广播事件的。  


&emsp; 从spring.factories中加载出EventPublishingRunListener类后会实例化，而实例化必然会通过EventPublishingRunListener的构造函数来进行实例化。EventPublishingRunListener的构造函数源码：  

```java
// EventPublishingRunListener.java

public EventPublishingRunListener(SpringApplication application, String[] args) {
 this.application = application;
 this.args = args;
 // 新建一个事件广播器SimpleApplicationEventMulticaster对象
 this.initialMulticaster = new SimpleApplicationEventMulticaster();
 // 遍历在构造SpringApplication对象时从spring.factories配置文件中获取的事件监听器
 for (ApplicationListener<?> listener : application.getListeners()) {
  // 将从spring.factories配置文件中获取的事件监听器们添加到事件广播器initialMulticaster对象的相关集合中
  this.initialMulticaster.addApplicationListener(listener);
 }
}
```
&emsp; 在EventPublishingRunListener的构造函数中有一个for循环会遍历之前从spring.factories中加载的监听器，然后添加到集合中缓存起来，用于以后广播各种事件时直接从这个集合中取出来即可，而不用再去spring.factories中加载，提高效率。  

### 1.3.2. 广播SpringBoot的内置生命周期事件  
&emsp; 从spring.factories配置文件中加载并实例化EventPublishingRunListener对象后，那么在SpringBoot的启动过程中会发布一系列SpringBoot内置的生命周期事件，SpringBoot启动过程中的源码：  

```java
// SpringApplication.java

public ConfigurableApplicationContext run(String... args) {
 StopWatch stopWatch = new StopWatch();
 stopWatch.start();
 ConfigurableApplicationContext context = null;
 Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
 configureHeadlessProperty();
 // 【0】新建一个SpringApplicationRunListeners对象用于发布SpringBoot启动过程中的生命周期事件
 SpringApplicationRunListeners listeners = getRunListeners(args);
 // 【1】》》》》》发布【ApplicationStartingEvent】事件，标志SpringApplication开始启动
 listeners.starting();
 try {
  ApplicationArguments applicationArguments = new DefaultApplicationArguments(
    args);
  // 【2】》》》》》发布【ApplicationEnvironmentPreparedEvent】事件，此时会去加载application.properties等配置文件的环境变量，同时也有标志环境变量已经准备好的意思
  ConfigurableEnvironment environment = prepareEnvironment(listeners,
    applicationArguments);
  configureIgnoreBeanInfo(environment);
  Banner printedBanner = printBanner(environment);
  context = createApplicationContext();
  exceptionReporters = getSpringFactoriesInstances(
    SpringBootExceptionReporter.class,
    new Class[] { ConfigurableApplicationContext.class }, context); 
  // 【3】》》》》》发布【ApplicationContextInitializedEvent】事件，标志context容器被创建且已准备好
  // 【4】》》》》》发布【ApplicationPreparedEvent】事件，标志Context容器已经准备完成
  prepareContext(context, environment, listeners, applicationArguments,
    printedBanner);
  refreshContext(context);
  afterRefresh(context, applicationArguments);
  stopWatch.stop();
  if (this.logStartupInfo) {
   new StartupInfoLogger(this.mainApplicationClass)
     .logStarted(getApplicationLog(), stopWatch);
  }
  // 【5】》》》》》发布【ApplicationStartedEvent】事件，标志spring容器已经刷新，此时所有的bean实例都已经加载完毕
  listeners.started(context);
  callRunners(context, applicationArguments);
 }
 // 【6】》》》》》发布【ApplicationFailedEvent】事件，标志SpringBoot启动失败
 catch (Throwable ex) {
  handleRunFailure(context, ex, exceptionReporters, listeners);
  throw new IllegalStateException(ex);
 }
 try {
  // 【7】》》》》》发布【ApplicationReadyEvent】事件，标志SpringApplication已经正在运行即已经成功启动，可以接收服务请求了。
  listeners.running(context);
 }
 catch (Throwable ex) {
  handleRunFailure(context, ex, exceptionReporters, null);
  throw new IllegalStateException(ex);
 }
 return context;
}
```
&emsp; <font color = "clime">在SpringBoot的启动过程中总共会发布7种不同类型的生命周期事件，来标志SpringBoot的不同启动阶段，</font>同时，这些生命周期事件的监听器们也会执行一些启动过程中的初始化逻辑，关于这些监听器的初始化逻辑将在下一篇内容中会分析。以下是SpringBoot启动过程中要发布的事件类型，其中ApplicationFailedEvent在SpringBoot启动过程中遇到异常才会发布：  

    ApplicationStartingEvent
    ApplicationEnvironmentPreparedEvent
    ApplicationContextInitializedEvent
    ApplicationPreparedEvent
    ApplicationStartedEvent
    ApplicationFailedEvent
    ApplicationReadyEvent

&emsp; 以listeners.starting();这句代码为例，看看EventPublishingRunListener对象发布事件的源码：  

```java
// SpringApplicationRunListeners.java

public void starting() {
 // 遍历listeners集合，这里实质取出的就是刚才从spring.factories中取出的SPI实现类EventPublishingRunListener
 // 而EventPublishingRunListener对象承担了SpringBoot启动过程中负责广播不同的生命周期事件
 for (SpringApplicationRunListener listener : this.listeners) {
         // 调用EventPublishingRunListener的starting方法来广播ApplicationStartingEvent事件
  listener.starting();
 }
}
```

```java
//EventPublishingRunListener.java

// 》》》》》发布【ApplicationStartingEvent】事件
public void starting() {
 // EventPublishingRunListener对象将发布ApplicationStartingEvent这件事情委托给了initialMulticaster对象
 // 调用initialMulticaster的multicastEvent方法来发布ApplicationStartingEvent事件
 this.initialMulticaster.multicastEvent(
   new ApplicationStartingEvent(this.application, this.args));
}
```
&emsp; EventPublishingRunListener对象将发布ApplicationStartingEvent这件事情委托给了SimpleApplicationEventMulticaster对象initialMulticaster，而initialMulticaster对象最终会调用其multicastEvent方法来发布ApplicationStartingEvent事件。  

## 1.4. SpringBoot内置生命周期事件详解  
[SpringBoot内置生命周期事件详解](/docs/microService/SpringBoot/SpringBootEvent.md)  