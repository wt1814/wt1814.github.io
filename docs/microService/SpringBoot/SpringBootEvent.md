

<!-- TOC -->

- [1. SpringBoot内置生命周期事件详解](#1-springboot内置生命周期事件详解)
    - [1.1. JDK的事件基类EventObject](#11-jdk的事件基类eventobject)
    - [1.2. Spring的事件基类ApplicationEvent](#12-spring的事件基类applicationevent)
    - [1.3. SpringBoot的事件基类SpringApplicationEvent](#13-springboot的事件基类springapplicationevent)
    - [1.4. ★★★SpringBoot具体的生命周期事件类](#14-★★★springboot具体的生命周期事件类)
        - [1.4.1. ApplicationStartingEvent](#141-applicationstartingevent)
        - [1.4.2. ApplicationEnvironmentPreparedEvent](#142-applicationenvironmentpreparedevent)
        - [1.4.3. ApplicationContextInitializedEvent](#143-applicationcontextinitializedevent)
        - [1.4.4. ApplicationPreparedEvent](#144-applicationpreparedevent)
        - [1.4.5. ApplicationStartedEvent](#145-applicationstartedevent)
        - [1.4.6. ApplicationReadyEvent](#146-applicationreadyevent)
        - [1.4.7. ApplicationFailedEvent](#147-applicationfailedevent)

<!-- /TOC -->

&emsp; **<font color = "clime">SpringBoot内置的7种生命周期事件</font>**  

|发布顺序|事件|用途|
|---|---|---|
|1|ApplicationStartingEvent|在SpringApplication启动时但在环境变量Environment或容器ApplicationContext创建前触发，标志SpringApplication开始启动。|
|2|ApplicationEnvironmentPreparedEvent|在SpringApplication已经开始启动且环境变量Environment已经准备好时触发，标志环境变量已经准备好。|
|3|ApplicationContextInitializedEvent|ApplicationContextInitilizers的初始化方法已经被调用即从spring.factories中加载的initializers已经执行ApplicationContext初始化逻辑但在bean定义加载前触发，标志Application已经初始化完毕。|
|4|ApplicationPreparedEvent|在Spring容器刷新refresh前触发。|
|5|ApplicationStaredEvent|在Spring容器刷新后触发，但在调用ApplicationRunner和CommandLineRunner的run方法调用前触发，标志Spring容器已经刷新，此时所有的bean实例等都已经加载了。|
|6|ApplicationReadyEvent|只要SpringApplication可以接收服务请求时即调用完ApplicationRunner和CommandLineRunner的run方法后触发，此时标志SpringApplication已经正在运行，即成功启动。|
|7|ApplicationFailedEvent|若SpringApplicaton未能成功启动时则会catch住异常发布ApplicationFailedEvent事件，标志ApplicationFailedEvent启动失败。|


# 1. SpringBoot内置生命周期事件详解  
<!--
SpringBoot内置生命周期事件详解
https://mp.weixin.qq.com/s?__biz=MzAwMDczMjMwOQ==&mid=2247483873&idx=1&sn=b115d1167b0e27c02baa4327bd113d1b&scene=19#wechat_redirect
-->

&emsp; SpringBoot的生命周期事件，类结构图如下：  
![image](http://182.92.69.8:8081/img/sourceCode/springBoot/boot-6.png)  

&emsp; 事件类之间的关系：  
1. 最顶级的父类是JDK的事件基类EventObject；  
2. 然后Spring的事件基类ApplicationEvent继承了JDK的事件基类EventObject；  
3. 其次SpringBoot的生命周期事件基类SpringApplicationEvent继承了Spring的事件基类ApplicationEvent；  
4. 最后<font color = "red">SpringBoot具体的7个生命周期事件类</font>再继承了SpringBoot的生命周期事件基类SpringApplicationEvent。  

## 1.1. JDK的事件基类EventObject  
&emsp; EventObject类是JDK的事件基类，可以说是所有Java事件类的基本，即所有的Java事件类都直接或间接继承于该类，源码如下：  

```java
// EventObject.java

public class EventObject implements java.io.Serializable {

    private static final long serialVersionUID = 5516075349620653480L;

    /**
     * The object on which the Event initially occurred.
     */
    protected transient Object  source;
    /**
     * Constructs a prototypical Event.
     *
     * @param    source    The object on which the Event initially occurred.
     * @exception  IllegalArgumentException  if source is null.
     */
    public EventObject(Object source) {
        if (source == null)
            throw new IllegalArgumentException("null source");
        this.source = source;
    }
    /**
     * The object on which the Event initially occurred.
     *
     * @return   The object on which the Event initially occurred.
     */
    public Object getSource() {
        return source;
    }
    /**
     * Returns a String representation of this EventObject.
     *
     * @return  A a String representation of this EventObject.
     */
    public String toString() {
        return getClass().getName() + "[source=" + source + "]";
    }
}
```
&emsp; EventObject类只有一个属性source，这个属性是用来记录最初事件是发生在哪个类。举个例子，比如在SpringBoot启动过程中会发布ApplicationStartingEvent事件，而这个事件最初是在SpringApplication类中发布的，因此source就是SpringApplication对象。  

## 1.2. Spring的事件基类ApplicationEvent  
&emsp; ApplicationEvent继承了JDK的事件基类EventObject类，是Spring的事件基类，被所有Spring的具体事件类继承，源码如下：  

```java
// ApplicationEvent.java

/**
 * Class to be extended by all application events. Abstract as it
 * doesn't make sense for generic events to be published directly.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class ApplicationEvent extends EventObject {
 /** use serialVersionUID from Spring 1.2 for interoperability. */
 private static final long serialVersionUID = 7099057708183571937L;
 /** System time when the event happened. */
 private final long timestamp;
 /**
  * Create a new ApplicationEvent.
  * @param source the object on which the event initially occurred (never {@code null})
  */
 public ApplicationEvent(Object source) {
  super(source);
  this.timestamp = System.currentTimeMillis();
 }
 /**
  * Return the system time in milliseconds when the event happened.
  */
 public final long getTimestamp() {
  return this.timestamp;
 }
}
```
&emsp; ApplicationEvent有且仅有一个属性timestamp，该属性是用来记录事件发生的时间。  

## 1.3. SpringBoot的事件基类SpringApplicationEvent  
&emsp; SpringApplicationEvent类继承了Spring的事件基类ApplicationEvent，是所有SpringBoot内置生命周期事件的父类，源码如下：  

```java
/**
 * Base class for {@link ApplicationEvent} related to a {@link SpringApplication}.
 *
 * @author Phillip Webb
 */
@SuppressWarnings("serial")
public abstract class SpringApplicationEvent extends ApplicationEvent {
 private final String[] args;
 public SpringApplicationEvent(SpringApplication application, String[] args) {
  super(application);
  this.args = args;
 }
 public SpringApplication getSpringApplication() {
  return (SpringApplication) getSource();
 }
 public final String[] getArgs() {
  return this.args;
 }
}
```
&emsp; SpringApplicationEvent有且仅有一个属性args，该属性就是SpringBoot启动时的命令行参数即标注@SpringBootApplication启动类中main函数的参数。  

## 1.4. ★★★SpringBoot具体的生命周期事件类  
&emsp; **<font color = "clime">SpringBoot内置的7种生命周期事件</font>**  

|发布顺序|事件|用途|
|---|---|---|
|1|ApplicationStartingEvent|在SpringApplication启动时但在环境变量Environment或容器ApplicationContext创建前触发，标志SpringApplication开始启动。|
|2|ApplicationEnvironmentPreparedEvent|在SpringApplication已经开始启动且环境变量Environment已经准备好时触发，标志环境变量已经准备好。|
|3|ApplicationContextInitializedEvent|ApplicationContextInitilizers的初始化方法已经被调用即从spring.factories中加载的initializers已经执行ApplicationContext初始化逻辑但在bean定义加载前触发，标志Application已经初始化完毕。|
|4|ApplicationPreparedEvent|在Spring容器刷新refresh前触发。|
|5|ApplicationStaredEvent|在Spring容器刷新后触发，但在调用ApplicationRunner和CommandLineRunner的run方法调用前触发，标志Spring容器已经刷新，此时所有的bean实例等都已经加载了。|
|6|ApplicationReadyEvent|只要SpringApplication可以接收服务请求时即调用完ApplicationRunner和CommandLineRunner的run方法后触发，此时标志SpringApplication已经正在运行，即成功启动。|
|7|ApplicationFailedEvent|若SpringApplicaton未能成功启动时则会catch住异常发布ApplicationFailedEvent事件，标志ApplicationFailedEvent启动失败。|

### 1.4.1. ApplicationStartingEvent  

```java
// ApplicationStartingEvent.java

public class ApplicationStartingEvent extends SpringApplicationEvent {
 public ApplicationStartingEvent(SpringApplication application, String[] args) {
  super(application, args);
 }
}
```
&emsp; SpringBoot开始启动时便会发布ApplicationStartingEvent事件，其发布时机在环境变量Environment或容器ApplicationContext创建前但在注册ApplicationListener具体监听器之后，标志SpringApplication开始启动。  

### 1.4.2. ApplicationEnvironmentPreparedEvent  

```java
// ApplicationEnvironmentPreparedEvent.java

public class ApplicationEnvironmentPreparedEvent extends SpringApplicationEvent {
 private final ConfigurableEnvironment environment;
 /**
  * Create a new {@link ApplicationEnvironmentPreparedEvent} instance.
  * @param application the current application
  * @param args the arguments the application is running with
  * @param environment the environment that was just created
  */
 public ApplicationEnvironmentPreparedEvent(SpringApplication application,
   String[] args, ConfigurableEnvironment environment) {
  super(application, args);
  this.environment = environment;
 }
 /**
  * Return the environment.
  * @return the environment
  */
 public ConfigurableEnvironment getEnvironment() {
  return this.environment;
 }
}
```
&emsp; ApplicationEnvironmentPreparedEvent事件多了一个environment属性。ApplicationEnvironmentPreparedEvent事件的environment属性作用是利用事件发布订阅机制，相应的监听器可以从ApplicationEnvironmentPreparedEvent事件中取出environment变量，然后可以为environment属性增加属性值或读出environment变量中的值。  

    举个例子： ConfigFileApplicationListener监听器就是监听了ApplicationEnvironmentPreparedEvent事件，然后取出ApplicationEnvironmentPreparedEvent事件的environment属性，然后再为environment属性增加application.properties配置文件中的环境变量值。

&emsp; 当SpringApplication已经开始启动且环境变量Environment已经创建后，并且为环境变量Environment配置了命令行和Servlet等类型的环境变量后，此时会发布ApplicationEnvironmentPreparedEvent事件。  
&emsp; 监听ApplicationEnvironmentPreparedEvent事件的第一个监听器是ConfigFileApplicationListener，因为是ConfigFileApplicationListener监听器还要为环境变量Environment增加application.properties配置文件中的环境变量；此后还有一些也是监听ApplicationEnvironmentPreparedEvent事件的其他监听器监听到此事件时，此时可以说环境变量Environment几乎已经完全准备好了。  

### 1.4.3. ApplicationContextInitializedEvent  

```java
// ApplicationContextInitializedEvent.java

public class ApplicationContextInitializedEvent extends SpringApplicationEvent {
 private final ConfigurableApplicationContext context;
 /**
  * Create a new {@link ApplicationContextInitializedEvent} instance.
  * @param application the current application
  * @param args the arguments the application is running with
  * @param context the context that has been initialized
  */
 public ApplicationContextInitializedEvent(SpringApplication application,
   String[] args, ConfigurableApplicationContext context) {
  super(application, args);
  this.context = context;
 }
 /**
  * Return the application context.
  * @return the context
  */
 public ConfigurableApplicationContext getApplicationContext() {
  return this.context;
 }
}
```
&emsp; 可以看到ApplicationContextInitializedEvent事件多了个ConfigurableApplicationContext类型的context属性，context属性的作用同样是为了相应监听器可以拿到这个context属性执行一些逻辑，具体作用将在ApplicationPreparedEvent事件中详述。  
&emsp; ApplicationContextInitializedEvent事件在ApplicationContext容器创建后，且为ApplicationContext容器设置了environment变量和执行了ApplicationContextInitializers的初始化方法后但在bean定义加载前触发，标志ApplicationContext已经初始化完毕。  

    扩展： 可以看到ApplicationContextInitializedEvent是在为context容器配置environment变量后触发，此时ApplicationContextInitializedEvent等事件只要有context容器的话，那么其他需要environment环境变量的监听器只需要从context中取出environment变量即可，从而ApplicationContextInitializedEvent等事件没必要再配置environment属性。

### 1.4.4. ApplicationPreparedEvent  

```java
// ApplicationPreparedEvent.java

public class ApplicationPreparedEvent extends SpringApplicationEvent {
 private final ConfigurableApplicationContext context;
 /**
  * Create a new {@link ApplicationPreparedEvent} instance.
  * @param application the current application
  * @param args the arguments the application is running with
  * @param context the ApplicationContext about to be refreshed
  */
 public ApplicationPreparedEvent(SpringApplication application, String[] args,
   ConfigurableApplicationContext context) {
  super(application, args);
  this.context = context;
 }
 /**
  * Return the application context.
  * @return the context
  */
 public ConfigurableApplicationContext getApplicationContext() {
  return this.context;
 }
}
```
&emsp; 同样可以看到ApplicationPreparedEvent事件多了个ConfigurableApplicationContext类型的context属性，多了context属性的作用是能让监听该事件的监听器们能拿到context属性，监听器拿到context属性一般有如下作用：  
1. 从事件中取出context属性，然后可以增加一些后置处理器，比如ConfigFileApplicationListener监听器监听到ApplicationPreparedEvent事件后，然后取出context变量，通过context变量增加了PropertySourceOrderingPostProcessor这个后置处理器；  
2. 通过context属性取出beanFactory容器，然后注册一些bean，比如LoggingApplicationListener监听器通过ApplicationPreparedEvent事件的context属性取出beanFactory容器，然后注册了springBootLoggingSystem这个单例bean；  
3. 通过context属性取出Environment环境变量，然后就可以操作环境变量，比如PropertiesMigrationListener。  

&emsp; ApplicationPreparedEvent事件在ApplicationContext容器已经完全准备好时但在容器刷新前触发，在这个阶段bean定义已经加载完毕还有environment已经准备好可以用了。  

### 1.4.5. ApplicationStartedEvent  

```java
// ApplicationStartedEvent.java

public class ApplicationStartedEvent extends SpringApplicationEvent {
 private final ConfigurableApplicationContext context;
 /**
  * Create a new {@link ApplicationStartedEvent} instance.
  * @param application the current application
  * @param args the arguments the application is running with
  * @param context the context that was being created
  */
 public ApplicationStartedEvent(SpringApplication application, String[] args,
   ConfigurableApplicationContext context) {
  super(application, args);
  this.context = context;
 }
 /**
  * Return the application context.
  * @return the context
  */
 public ConfigurableApplicationContext getApplicationContext() {
  return this.context;
 }
}
```
&emsp; ApplicationStartedEvent事件将在容器刷新后但ApplicationRunner和CommandLineRunner的run方法执行前触发，标志Spring容器已经刷新，此时容器已经准备完毕了。  

    扩展： 这里提到了ApplicationRunner和CommandLineRunner接口有啥作用呢？一般会在Spring容器刷新完毕后，此时可能有一些系统参数等静态数据需要加载，此时就可以实现了ApplicationRunner或CommandLineRunner接口来实现静态数据的加载。

### 1.4.6. ApplicationReadyEvent  

```java
// ApplicationReadyEvent.java

public class ApplicationReadyEvent extends SpringApplicationEvent {
 private final ConfigurableApplicationContext context;
 /**
  * Create a new {@link ApplicationReadyEvent} instance.
  * @param application the current application
  * @param args the arguments the application is running with
  * @param context the context that was being created
  */
 public ApplicationReadyEvent(SpringApplication application, String[] args,
   ConfigurableApplicationContext context) {
  super(application, args);
  this.context = context;
 }
 /**
  * Return the application context.
  * @return the context
  */
 public ConfigurableApplicationContext getApplicationContext() {
  return this.context;
 }
}
```
&emsp; ApplicationReadyEvent事件在调用完ApplicationRunner和CommandLineRunner的run方法后触发，此时标志SpringApplication已经正在运行。  

### 1.4.7. ApplicationFailedEvent  

```java
// ApplicationFailedEvent.java

public class ApplicationFailedEvent extends SpringApplicationEvent {
 private final ConfigurableApplicationContext context;
 private final Throwable exception;
 /**
  * Create a new {@link ApplicationFailedEvent} instance.
  * @param application the current application
  * @param args the arguments the application was running with
  * @param context the context that was being created (maybe null)
  * @param exception the exception that caused the error
  */
 public ApplicationFailedEvent(SpringApplication application, String[] args,
   ConfigurableApplicationContext context, Throwable exception) {
  super(application, args);
  this.context = context;
  this.exception = exception;
 }
 /**
  * Return the application context.
  * @return the context
  */
 public ConfigurableApplicationContext getApplicationContext() {
  return this.context;
 }
 /**
  * Return the exception that caused the failure.
  * @return the exception
  */
 public Throwable getException() {
  return this.exception;
 }
}
```
&emsp; ApplicationFailedEvent事件除了多了一个context属性外，还多了一个Throwable类型的exception属性用来记录SpringBoot启动失败时的异常。  
&emsp; ApplicationFailedEvent事件在SpringBoot启动失败时触发，标志SpringBoot启动失败。  
