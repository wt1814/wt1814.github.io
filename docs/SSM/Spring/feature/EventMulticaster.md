

    在spring源码中和事件涉及到的主要概念如下：
        事件（ApplicationEvent）监听器（ApplicationEventListener）事件发布器（ApplicationEventPublisher）事件多播器（ApplicationEventMulticaster）
    具体源码层面的涉及如下：
        容器刷新refresh方法中：
            initApplicationEventMulticaster()：初始化事件多播器registerListeners();：注册事件监听器finishRefresh()方法中调用publishEvent(new ContextRefreshedEvent(this))方法发布容器刷新事件。

# 事件多播器

<!-- 
https://blog.csdn.net/pengweismile/article/details/103207017
https://www.cnblogs.com/nijunyang/p/12339757.html?ivk_sa=1024320u
-->

&emsp; **<font color = "blue">Spring容器的refresh方法，容器启动过程中，事件监听listener相关的主要是这三个方法：</font>**  
* initApplicationEventMulticaster方法初始化事件多播器，后续的事件发布都是由多播器来发布的；  
* registerListeners注册监听器到前面初始化好的多播器上面去；
* finishRefresh容器启动完成最后刷新，发布ContextRefreshedEvent事件。  


1. 初始化多播器：获取bean工厂对象ConfigurableListableBeanFactory，判断容器中是否有applicationEventMulticaster多播器，如果没有则创建一个一个简单事件多播器SimpleApplicationEventMulticaster并注册到容器中，后续使用

```java
public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";
```

```java
protected void initApplicationEventMulticaster() {
    ConfigurableListableBeanFactory beanFactory = getBeanFactory();
    if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
        this.applicationEventMulticaster =
                beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
        if (logger.isDebugEnabled()) {
            logger.debug("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
        }
    }
    else {
        this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
        beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
        if (logger.isDebugEnabled()) {
            logger.debug("Unable to locate ApplicationEventMulticaster with name '" +
                    APPLICATION_EVENT_MULTICASTER_BEAN_NAME +
                    "': using default [" + this.applicationEventMulticaster + "]");
        }
    }
}
```

2. 注册监听器到多播器上并发布早期事件：首先获取容器中已有的监听器（成品对象，从第一张图中可以看到我们自己的组件对象在registerListeners方法调用的时候 还没有初始化，是在下面的finishBeanFactoryInitialization方法中才进行初始化的），注册到多播器；然后获取bean定义中的监听器，也就是自己定义的监听器；同样也注册到多播器上去；最后如果有早期事件就去发布早期事件（multicastEvent方法），这些事件只能由已经实例化的监听器监听，自定义的监听器初始化是在finishBeanFactoryInitialization方法中。

```java
protected void registerListeners() {
    // Register statically specified listeners first.
    for (ApplicationListener<?> listener : getApplicationListeners()) {
        getApplicationEventMulticaster().addApplicationListener(listener);
    }

    // Do not initialize FactoryBeans here: We need to leave all regular beans
    // uninitialized to let post-processors apply to them!
    String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
    for (String listenerBeanName : listenerBeanNames) {
        getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
    }

    // Publish early application events now that we finally have a multicaster...
    Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
    this.earlyApplicationEvents = null;
    if (earlyEventsToProcess != null) {
        for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
            getApplicationEventMulticaster().multicastEvent(earlyEvent);
        }
    }
}
```

&emsp; 发布事件：multicastEvent方法----->invokeListener方法---->doInvokeListener方法调用监听器的onApplicationEvent

```java
// SimpleApplicationEventMulticaster
@Override
public void multicastEvent(ApplicationEvent event) {
    multicastEvent(event, resolveDefaultEventType(event));
}

@Override
public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
    ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
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
```

&emsp; 可以看到这里支持异步发送，但是从上面我们初始化简单多播器的时候，executer对象并没有赋值，因此始终是同步发布。如果我们想实现异步发布事件，那么就要让上面初始化多播器的逻辑走进第一个分支。我们可以在容器中自己继承SimpleApplicationEventMulticaster，并初始化一个线程池，然后将其注册到容器中，bean的名字必须使用“applicationEventMulticaster”，因为此时容器还没有创建真正的对象，只有这个名字的bean定义才会马上去创建对象。这样就可以实现异步发布事件了。

3. 执行finishRefresh方法发布ContextRefreshedEvent事件，标志的容器已经启动完成。

```java
protected void finishRefresh() {
    // Clear context-level resource caches (such as ASM metadata from scanning).
    clearResourceCaches();

    // Initialize lifecycle processor for this context.
    initLifecycleProcessor();

    // Propagate refresh to lifecycle processor first.
    getLifecycleProcessor().onRefresh();

    // Publish the final event.
    publishEvent(new ContextRefreshedEvent(this));

    // Participate in LiveBeansView MBean, if active.
    LiveBeansView.registerApplicationContext(this);
}
```

&emsp; 监听器的流程完了，我们现在来看下使用
