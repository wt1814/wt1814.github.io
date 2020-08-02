
<!-- TOC -->

- [1. Aware接口，Bean对容器对感知](#1-aware接口bean对容器对感知)
    - [1.1. ApplicationContextAware接口](#11-applicationcontextaware接口)
        - [1.1.1. ApplicationContextAware接口解析](#111-applicationcontextaware接口解析)
    - [1.2. BeanNameAware接口](#12-beannameaware接口)

<!-- /TOC -->

# 1. Aware接口，Bean对容器对感知  
&emsp; **<font color = "lime">容器管理的Bean一般不需要了解容器的状态和直接使用容器，但在某些情况下，是需要在Bean中直接对IOC容器进行操作的，这时候，就需要在Bean中设定对容器的感知。Spring IOC容器也提供了该功能，它是通过特定的aware接口来完成的。</font>** <font color = "red">aware接口有以下这些：

* BeanNameAware ，可以在Bean中得到它在IOC容器中的Bean实例名称。  
* BeanFactoryAware，可以在Bean中得到Bean所在的IOC容器，从而直接在Bean中使用IOC容器的服务。  
* ApplicationContextAware，可以在Bean中得到Bean所在的应用上下文，从而直接在 Bean中使用应用上下文的服务。  
* MessageSourceAware，在Bean中可以得到消息源。  
* ApplicationEventPublisherAware，在Bean中可以得到应用上下文的事件发布器，从而可以在Bean中发布应用上下文的事件。  
* ResourceLoaderAware，在Bean中可以得到ResourceLoader，从而在Bean中使用ResourceLoader加载外部对应的Resource资源。</font>  

&emsp; 在设置Bean的属性之后，调用初始化回调方法之前，Spring会调用aware接口中的setter方法。  

## 1.1. ApplicationContextAware接口  

&emsp; AbstractApplicationContext类是Spring容器应用上下文的一个抽象父类，ApplicationContextAware是用来获取spring的上下文。当一个类需要获取ApplicationContext实例时，可以通过工具类直接实现ApplicationContextAware接口，返回ApplicationContext对象。如下代码所示：  

```java
@Component
public class BeansUtils implements ApplicationContextAware {

    private static ApplicationContext context;

    public static <T> T getBean(Class<T> bean) {
        return context.getBean(bean);
    }
    public static <T> T getBean(String var1, @Nullable Class<T> var2){
        return context.getBean(var1, var2);
    }

    public static ApplicationContext getContext() {
        return context;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        BeansUtils.context = context;
    }
}
```

### 1.1.1. ApplicationContextAware接口解析
&emsp; ApplicationContextAware接口源码：   

```java
public interface ApplicationContextAware {
    void setApplicationContext(ApplicationContext applicationcontext) throws BeansException;
}
```
&emsp; 这里只有一个方法setApplicationContext(ApplicationContext applicationcontext),它是一个回调函数，在Bean中通过实现这个函数，可以在容器回调该aware接口方法时使注入的applicationcontext引用在Bean中保存下来，供Bean需要使用Applicationcontext的基本服务时使用。这个对setApplicationContext方法的回调是由容器自动完成的。  

&emsp; 在初始化的时候，从入口类ClassPathXmlApplicationContext->AbstractApplicationContext类的的refresh方法 ->prepareBeanFactory方法：  

```java
protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    // 设置 BeanFactory 的类加载器，我们知道 BeanFactory 需要加载类，也就需要类加载器，这里设置为当前 ApplicationContext 的类加载器
    beanFactory.setBeanClassLoader(getClassLoader());
    // 设置 BeanExpressionResolver
    beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver());
    beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));
    // 添加一个 BeanPostProcessor，这个 processor 比较简单，实现了 Aware 接口的几个特殊的 beans 在初始化的时候，这个 processor 负责回调
    beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
    // 下面几行的意思就是，如果某个 bean 依赖于以下几个接口的实现类，在自动装配的时候忽略它们，Spring 会通过其他方式来处理这些依赖。
    beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
    beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
    beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
    beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);
    beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
    beanFactory.registerResolvableDependency(ResourceLoader.class, this);
    beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
    beanFactory.registerResolvableDependency(ApplicationContext.class, this);

    if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
        beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
        beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
    }// 如果没有定义 "environment" 这个 bean，那么 Spring 会 "手动" 注册一个
    if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
        beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
    }
    // 如果没有定义 "systemProperties" 这个 bean，那么 Spring 会 "手动" 注册一个
    if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
        beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
    }
    // 如果没有定义 "systemEnvironment" 这个 bean，那么 Spring 会 "手动" 注册一个
    if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
        beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
    }
}
```

&emsp; beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this)); spring源码里面将ApplicationContextAwareProcessor加入到BeanPostProcessor处理器里面了，并且传的是一个ApplicationContext类型参数进去。  

```java
class ApplicationContextAwareProcessor implements BeanPostProcessor {

    private final ConfigurableApplicationContext applicationContext;
    //beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));调用此构造方法把ApplicationContext传过来
    public ApplicationContextAwareProcessor(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    //实例化之前进行的处理
    public Object postProcessBeforeInitialization(final Object bean, String beanName) throws BeansException {
        AccessControlContext acc = null;

        if (System.getSecurityManager() != null &&
                (bean instanceof EnvironmentAware || bean instanceof EmbeddedValueResolverAware ||
                        bean instanceof ResourceLoaderAware || bean instanceof ApplicationEventPublisherAware ||
                        bean instanceof MessageSourceAware || bean instanceof ApplicationContextAware)) {
            acc = this.applicationContext.getBeanFactory().getAccessControlContext();
        }

        if (acc != null) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    //给Aware的实现类set值进去
                    invokeAwareInterfaces(bean);
                    return null;
                }
            }, acc);
        }
        else {
            //给Aware的实现类set值进去
            invokeAwareInterfaces(bean);
        }

        return bean;
    }

    private void invokeAwareInterfaces(Object bean) {
        if (bean instanceof Aware) {
            if (bean instanceof EnvironmentAware) {
                ((EnvironmentAware) bean).setEnvironment(this.applicationContext.getEnvironment());
            }
            if (bean instanceof EmbeddedValueResolverAware) {
                ((EmbeddedValueResolverAware) bean).setEmbeddedValueResolver(
                        new EmbeddedValueResolver(this.applicationContext.getBeanFactory()));
            }
            if (bean instanceof ResourceLoaderAware) {
                ((ResourceLoaderAware) bean).setResourceLoader(this.applicationContext);
            }
            if (bean instanceof ApplicationEventPublisherAware) {
                ((ApplicationEventPublisherAware) bean).setApplicationEventPublisher(this.applicationContext);
            }
            if (bean instanceof MessageSourceAware) {
                ((MessageSourceAware) bean).setMessageSource(this.applicationContext);
            }
            //判读是否是属于ApplicationContextAware接口的类
            if (bean instanceof ApplicationContextAware) {
                //调用实现类的setApplicationContext方法把applicationContext set进去
                ((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
            }
        }
    }
　　// ... ... 
}
```

&emsp; 作为依赖注入的一部分，postProcessBeforelnitialization会在initializeBean的实现过程 中被调用，从而实现对aware接口的相关注入。  

## 1.2. BeanNameAware接口  
&emsp; BeanNameAware接口可以在Bean中得到它在IOC容器中的Bean实例名称。  
&emsp; BeanNameAware接口源码：  

```java
public interface BeanNameAware extends Aware {
    void setBeanName(String var1);
}
```

```java
public class BeanTest implements BeanNameAware{

    private String beanName;

    @Override
    public void setBeanName(String beanName) {
        // 让Bean获取自己在BeanFactory配置中的名字（根据情况是id或者name）
        this.beanName=beanName;     
    }
}
```
&emsp; setBeanName()方法Spring会自动调用。setBeanName()会在Spring自身完成Bean配置之后，且在调用任何Bean生命周期回调（初始化或者销毁）方法之前就调用这个方法。换言之，在程序中使用BeanFactory.getBean(String beanName)之前，Bean的名字就已经设定好了。所以，程序中可以使用BeanName而不用担心它没有被初始化。  

 

