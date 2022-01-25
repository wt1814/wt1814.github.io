

<!-- TOC -->

- [1. Spring的事件机制](#1-spring的事件机制)
    - [1.1. 从refresh()中说起](#11-从refresh中说起)
    - [1.2. Spring事件机制解析](#12-spring事件机制解析)
        - [1.2.1. ★★★Spring事件机制的流程](#121-★★★spring事件机制的流程)
        - [1.2.2. ApplicationEvent，事件](#122-applicationevent事件)
        - [1.2.3. ApplicationListener，事件监听器](#123-applicationlistener事件监听器)
        - [1.2.4. ApplicationEventMulticaster，事件管理者](#124-applicationeventmulticaster事件管理者)
        - [1.2.5. ApplicationEventPublisher，事件发布者](#125-applicationeventpublisher事件发布者)
    - [1.3. 使用测试](#13-使用测试)

<!-- /TOC -->

&emsp; **<font color = "clime">5个标准事件：</font>**   

* 上下文更新事件（ContextRefreshedEvent）：在调用ConfigurableApplicationContext接口中的refresh()方法时被触发。  
* 上下文开始事件（ContextStartedEvent）：当容器调用ConfigurableApplicationContext的Start()方法开始/重新开始容器时触发该事件。  
* 上下文停止事件（ContextStoppedEvent）：当容器调用ConfigurableApplicationContext的Stop()方法停止容器时触发该事件。  
* 上下文关闭事件（ContextClosedEvent）：当ApplicationContext被关闭时触发该事件。容器被关闭时，其管理的所有单例Bean都被销毁。  
* 请求处理事件（RequestHandledEvent）：在Web应用中，当一个http请求（request）结束触发该事件。如果一个bean实现了ApplicationListener接口，当一个ApplicationEvent被发布以后，bean会自动被通知。  

# 1. Spring的事件机制  
<!-- 

事件机制的实现需要三个部分,事件源,事件,事件监听器
https://zhuanlan.zhihu.com/p/114244039

https://zhuanlan.zhihu.com/p/71230214
Spring 与 Spring Boot 中的事件机制 
https://mp.weixin.qq.com/s/_0QZu5f8XYSsqAzhAcGl2Q
Spring事件机制使用
https://blog.csdn.net/weixin_39035120/article/details/86225377

https://blog.csdn.net/endless_Y/article/details/105938580
-->
&emsp; 事件监听是一种发布订阅者模式。做完某一件事情以后，需要广播一些消息或者通知，告诉其他的模块进行一些事件处理。相比发送请求，事件监听可以实现接口解耦。   

## 1.1. 从refresh()中说起
&emsp; ......

## 1.2. Spring事件机制解析  
### 1.2.1. ★★★Spring事件机制的流程
&emsp; **<font color = "clime">★★★Spring事件机制的流程：</font>**   
1. **<font color = "clime">事件机制的核心是事件。</font>** Spring中的事件是ApplicationEvent。Spring提供了5个标准事件，此外还可以自定义事件(继承ApplicationEvent)。  
2. **<font color = "clime">确定事件后，要把事件发布出去。</font>** 在事件发布类的业务代码中调用ApplicationEventPublisher#publishEvent方法（或调用ApplicationEventPublisher的子类，例如调用ApplicationContext#publishEvent）。  
3. **<font color = "blue">发布完成之后，启动监听器，自动监听。</font>** 在监听器类中覆盖ApplicationListener#onApplicationEvent方法。  
4. 最后，就是实际场景中触发事件发布，完成一系列任务。  

&emsp; **<font color = "red">实现Spring事件机制主要有4个类：</font>**  

* ApplicationEvent：事件，每个实现类表示一类事件，可携带数据。
* ApplicationListener：事件监听器，用于接收事件处理时间。
* ApplicationEventMulticaster：事件管理者，用于事件监听器的注册和事件的广播。
* ApplicationEventPublisher：事件发布者，委托ApplicationEventMulticaster完成事件发布。  


&emsp; **<font color = "clime">Spring事件机制的特性：</font>**   

* <font color = "clime">Spring的事件默认是同步的，即调用publishEvent方法发布事件后，它会处于阻塞状态，直到onApplicationEvent接收到事件并处理返回之后才继续执行下去，这种单线程同步的好处是可以进行事务管理。</font>  
* <font color = "clime">事件监听是循环往复的，如果确定事件只会发布一次，应该移除事件监听器。</font>  


### 1.2.2. ApplicationEvent，事件  
&emsp; ApplicationEvent表示事件，每个实现类表示一类事件，可携带数据。<font color = "clime">下面是一些Spring提供的标准事件，都继承了ApplicationEvent。</font>  

|事件名|注释|
|---|---|
|ContextRefreshedEvent|Spring应用上下文就绪事件|
|ContextStartedEvent|Spring应用上下文启动事件|
|ContextStopedEvent|Spring应用上下文停止事件|
|ContextClosedEvent|Spring应用上下文关闭事件|

--------

&emsp; Spring 提供5种标准的事件：  

* 上下文更新事件（ContextRefreshedEvent）：在调用ConfigurableApplicationContext接口中的refresh()方法时被触发。  
* 上下文开始事件（ContextStartedEvent）：当容器调用ConfigurableApplicationContext的Start()方法开始/重新开始容器时触发该事件。  
* 上下文停止事件（ContextStoppedEvent）：当容器调用ConfigurableApplicationContext的Stop()方法停止容器时触发该事件。  
* 上下文关闭事件（ContextClosedEvent）：当ApplicationContext被关闭时触发该事件。容器被关闭时，其管理的所有单例Bean都被销毁。  
* 请求处理事件（RequestHandledEvent）：在Web应用中，当一个http请求（request）结束触发该事件。如果一个bean实现了ApplicationListener接口，当一个ApplicationEvent被发布以后，bean会自动被通知。  


-----------

* ContextRefreshedEvent，上下文更新事件  
    &emsp; **<font color = "blue">ContextRefreshedEvent上下文更新事件发生在刷新容器（refresh()方法）的“完成刷新容器时发布对应的事件”步骤中。</font>**  
    ```java
    // AbstractApplicationContext.class
    public void refresh() throws BeansException, IllegalStateException {
    
       //...
       this.finishRefresh();
    }
    ```
    
    ```java
    protected void finishRefresh() {
       this.clearResourceCaches();
       this.initLifecycleProcessor();
       this.getLifecycleProcessor().onRefresh();
       //ContextRefreshedEvent上下文更新事件
       this.publishEvent((ApplicationEvent)(new ContextRefreshedEvent(this)));
       LiveBeansView.registerApplicationContext(this);
    }
    ```

    &emsp; 事件发布在ApplicationContext初始化或刷新时（例如，通过在ConfigurableApplicationContext接口使用refresh()方法）。这里，“初始化”意味着所有bean加载，post-processor bean被检测到并且激活，单例预先实例化，ApplicationContext对象可以使用了。只要上下文没有关闭，可以触发多次刷新，ApplicationContext提供了一种可选择的支持这种“热”刷新。例如，XmlWebApplicationContext支持热刷新，但GenericApplicationContext并非如此。具体是在AbstractApplicationContext的finishRefresh()方法中。  
* ContextStartedEvent，上下文开始事件  
    &emsp; 事件发布在ApplicationContext开始使用ConfigurableApplicationContext接口start()方法。这里，“开始”意味着所有生命周期bean接收到一个明确的起始信号。通常，这个信号用于明确停止后重新启动，但它也可以用于启动组件没有被配置为自动运行(例如，组件还没有开始初始化)。  
* ContextStoppedEvent，上下文停止事件  
    &emsp; 事件发布在ApplicationContext停止时通过使用ConfigurableApplicationContext接口上的stop()方法。在这里，“停止”意味着所有生命周期bean接收一个显式的停止信号。停止上下文可以通过重新调用start()方法。  
* ContextClosedEvent，上下文关闭事件  
    &emsp; 事件发布在ApplicationContext关闭时通过关闭ConfigurableApplicationContext接口方法。这里，“封闭”意味着所有单例bean被摧毁。一个封闭的环境达到生命的终结。它不能刷新或重启。  
* RequestHandledEvent，请求处理事件  
    &emsp; 一个特定的web事件告诉所有能处理HTTP请求的bean。这个事件是在请求完成后发布的。这个事件只适用于使用Spring的DispatcherServlet的web应用程序。  

&emsp; ApplicationEvent代码如下：  

```java
public abstract class ApplicationEvent extends EventObject {
    private static final long serialVersionUID = 7099057708183571937L;
    private final long timestamp = System.currentTimeMillis();

    public ApplicationEvent(Object source) {
        super(source);
    }

    public final long getTimestamp() {
        return this.timestamp;
    }
}
```

### 1.2.3. ApplicationListener，事件监听器  

```java
@FunctionalInterface
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {
    void onApplicationEvent(E event);
}
```
&emsp; 当事件监听器接收到它可以处理的事件，会调用onApplicationEvent()方法。注意到<font color = "red">ApplicationListener是泛型参数，这样可以实现所有继承了ApplicationEvent的监听。可以尽可能多的注册想要的事件侦听器，但是默认情况下事件监听器同步接收事件。这意味着publishEvent()方法会阻塞直到所有的事件监听器成处理完事件。这种单线程同步方法的一个特点是，当一个监听器接收到一个事件时，它运行在事务上下文的发布者线程上(如果事务上下文可用)。</font>如果事件的发布需要另一种策略(譬如多线程)需要实现ApplicationEventMulticaster接口类。  

### 1.2.4. ApplicationEventMulticaster，事件管理者  
&emsp; **<font color = "red">ApplicationEventMulticaster接口方法分为三类，注册事件监听器、移除事件监听器、发布事件。</font>**  

```java
public interface ApplicationEventMulticaster {
   void addApplicationListener(ApplicationListener<?> listener);
   void addApplicationListenerBean(String listenerBeanName);
   void removeApplicationListener(ApplicationListener<?> listener);
   void removeApplicationListenerBean(String listenerBeanName);
   void removeAllListeners();
   void multicastEvent(ApplicationEvent event);
   void multicastEvent(ApplicationEvent event, @Nullable ResolvableType eventType);
}
```

&emsp; 执行AbstractApplicationContext.initApplicationEventMulticaster() 方法时会实例化一个bean name为applicationEventMulticaster的SimpleApplicationEventMulticaster，它的父类实现了前5个方法依靠一个内部类ListenerRetriever维护了一个Set<ApplicationListener<?>\>，本质事件监听器的注册或移除就是对这个Set的添加和移除操作。  

```java
public abstract class AbstractApplicationEventMulticaster
      implements ApplicationEventMulticaster, BeanClassLoaderAware, BeanFactoryAware {
   private final ListenerRetriever defaultRetriever = new ListenerRetriever(false);
   
   @Override
   public void addApplicationListener(ApplicationListener<?> listener) {
      synchronized (this.retrievalMutex) {
         // 如果已经注册，则显式删除代理的目标，以避免对同一个侦听器进行双重调用。
         Object singletonTarget = AopProxyUtils.getSingletonTarget(listener);
         if (singletonTarget instanceof ApplicationListener) {
            this.defaultRetriever.applicationListeners.remove(singletonTarget);
         }
         this.defaultRetriever.applicationListeners.add(listener);
         this.retrieverCache.clear();
      }
   }
 
   @Override
   public void removeApplicationListener(ApplicationListener<?> listener) {
      synchronized (this.retrievalMutex) {
         this.defaultRetriever.applicationListeners.remove(listener);
         this.retrieverCache.clear();
      }
   }
 
   private class ListenerRetriever {
      public final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();
      public final Set<String> applicationListenerBeans = new LinkedHashSet<>();
      private final boolean preFiltered;
      public ListenerRetriever(boolean preFiltered) {
         this.preFiltered = preFiltered;
      }
      public Collection<ApplicationListener<?>> getApplicationListeners() {
         List<ApplicationListener<?>> allListeners = new ArrayList<>(
               this.applicationListeners.size() + this.applicationListenerBeans.size());
         allListeners.addAll(this.applicationListeners);
         if (!this.applicationListenerBeans.isEmpty()) {
            BeanFactory beanFactory = getBeanFactory();
            for (String listenerBeanName : this.applicationListenerBeans) {
               try {
                  ApplicationListener<?> listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
                  if (this.preFiltered || !allListeners.contains(listener)) {
                     allListeners.add(listener);
                  }
               }
               catch (NoSuchBeanDefinitionException ex) {
                  // Singleton listener instance (without backing bean definition) disappeared -
                  // probably in the middle of the destruction phase
               }
            }
         }
         if (!this.preFiltered || !this.applicationListenerBeans.isEmpty()) {
            AnnotationAwareOrderComparator.sort(allListeners);
         }
         return allListeners;
      }
   }
}
```

&emsp; 接口后两个方法由子类实现，可以看到SimpleApplicationEventMulticaster拥有一个Executor和ErrorHandler，分别表示监听器的调用线程池(如果不想使用单线程同步处理则可以设置一个线程池)和监听器处理事件失败的处理者(如果设置了的话)否则抛异常。  

```java
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {
   @Nullable
   private Executor taskExecutor;
   @Nullable
   private ErrorHandler errorHandler;
   public SimpleApplicationEventMulticaster() {
   }
   public SimpleApplicationEventMulticaster(BeanFactory beanFactory) {
      setBeanFactory(beanFactory);
   }
   public void setTaskExecutor(@Nullable Executor taskExecutor) {
      this.taskExecutor = taskExecutor;
   }
   public void setErrorHandler(@Nullable ErrorHandler errorHandler) {
      this.errorHandler = errorHandler;
   }
   @Override
   public void multicastEvent(ApplicationEvent event) {
      //广播事件，可以自动分析出ApplicationEvent是那种事件类型
      multicastEvent(event, resolveDefaultEventType(event));
   }
   @Override
   public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
      ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
      //调用父类方法getApplicationListeners只取得能处理此类事件的时间监听器，依次处理
      for (final ApplicationListener<?> listener : getApplicationListeners(event, type)) {
         Executor executor = getTaskExecutor();
         if (executor != null) {
            executor.execute(() -> invokeListener(listener, event));
         }
         else {
            invokeListener(listener, event);
         }
      }
   }
 
   private ResolvableType resolveDefaultEventType(ApplicationEvent event) {
      return ResolvableType.forInstance(event);
   }
 
   protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
      ErrorHandler errorHandler = getErrorHandler();
      if (errorHandler != null) {
         try {
            doInvokeListener(listener, event);
         }
         catch (Throwable err) {
            errorHandler.handleError(err);
         }
      }
      else {
         doInvokeListener(listener, event);
      }
   }
 
   @SuppressWarnings({"unchecked", "rawtypes"})
   private void doInvokeListener(ApplicationListener listener, ApplicationEvent event) {
      try {
         listener.onApplicationEvent(event);
      }
      catch (ClassCastException ex) {　　// 省略}
   }
 
   private boolean matchesClassCastMessage(String classCastMessage, Class<?> eventClass) {// 省略}
}
```

### 1.2.5. ApplicationEventPublisher，事件发布者  

```java
@FunctionalInterface
public interface ApplicationEventPublisher {
   default void publishEvent(ApplicationEvent event) {
      publishEvent((Object) event);
   }
   void publishEvent(Object event);
}
```

&emsp; ApplicationEventPublisher很简单只有两个发布事件的方法，AbstractApplicationContext是它的默认实现类，下面是具体实现。  

```java
@Override
public void publishEvent(ApplicationEvent event) {
   publishEvent(event, null);
}
@Override
public void publishEvent(Object event) {
   publishEvent(event, null);
}
protected void publishEvent(Object event, @Nullable ResolvableType eventType) {
   Assert.notNull(event, "Event must not be null");
 
   ApplicationEvent applicationEvent;
   if (event instanceof ApplicationEvent) {
      applicationEvent = (ApplicationEvent) event;
   }
   else {
      applicationEvent = new PayloadApplicationEvent<>(this, event);
      if (eventType == null) {
         eventType = ((PayloadApplicationEvent) applicationEvent).getResolvableType();
      }
   }
 
   // Multicast right now if possible - or lazily once the multicaster is initialized
   if (this.earlyApplicationEvents != null) {
      this.earlyApplicationEvents.add(applicationEvent);
   }
   else {
      getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
   }
 
   // Publish event via parent context as well...
   if (this.parent != null) {
      if (this.parent instanceof AbstractApplicationContext) {
         ((AbstractApplicationContext) this.parent).publishEvent(event, eventType);
      }
      else {
         this.parent.publishEvent(event);
      }
   }
}
```
&emsp; 可以看到事件的发布依赖于前面提到的bean name是applicationEventMulticaster的SimpleApplicationEventMulticaster。关于监听器注册有两种方法：接口实现、注解，代码如下：  

```java
//使用接口实现方式
public class RegisterListener implements ApplicationListener<UserRegisterEvent> {
    @Override
    public void onApplicationEvent(UserRegisterEvent userRegisterEvent) {
        //../省略逻辑
    }
}

//使用@EventListener注解方式
@Component
public class AnnotationRegisterListener {
    @EventListener
    public void register(UserRegisterEvent userRegisterEvent)
    {
　　　　//../省略逻辑
    }
}
```

## 1.3. 使用测试  
&emsp; ApplicationEvent抽象类、ApplicationListener接口是常常搭配使用的：  

* ApplicationEvent：是个抽象类，里面只有一个构造函数和一个长整型的timestamp。  
* ApplicationListener：是一个接口，里面只有一个onApplicationEvent方法。所以自己的类在实现该接口的时候，要实装该方法。  
* ApplicationContext：如果在上下文中部署一个实现了ApplicationListener接口的bean，那么每当在一个ApplicationEvent发布到ApplicationContext时，这个bean得到通知。  

```java
public class EmailEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;
    public String address;
    public String text;

    public EmailEvent(Object source) {
        super(source);
    }

    public EmailEvent(Object source, String address, String text) {
        super(source);
        this.address = address;
        this.text = text;
    }

    public void print(){
        System.out.println("hello spring event!");
    }
}

public class EmailListener implements ApplicationListener {

    public void onApplicationEvent(ApplicationEvent  event) {
        if(event instanceof EmailEvent){
            EmailEvent emailEvent = (EmailEvent)event;
            emailEvent.print();
            System.out.println("the source is:"+emailEvent.getSource());
            System.out.println("the address is:"+emailEvent.address);
            System.out.println("the email's context is:"+emailEvent.text);
        }
    }
}

public class Test {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        EmailEvent event = new EmailEvent("hello","boylmx@163.com","this is a email text!");
        context.publishEvent(event);
    }
}
```
&emsp; 测试结果如下：  

    hello spring event!  
    the source is:hello  
    the address is:boylmx@163.com  
    the email's context is:this is a email text! 
