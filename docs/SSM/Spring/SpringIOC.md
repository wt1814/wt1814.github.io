

<!-- TOC -->

- [1. SpringIOC解析](#1-springioc解析)
    - [1.1. IOC容器的设计与实现](#11-ioc容器的设计与实现)
        - [1.1.1. BeanFactory](#111-beanfactory)
        - [1.1.2. ApplicationContext](#112-applicationcontext)
            - [1.1.2.1. BeanFactory和ApplicationContext的区别](#1121-beanfactory和applicationcontext的区别)
        - [1.1.3. BeanDefinition](#113-beandefinition)
        - [1.1.4. BeanDefinitionReader](#114-beandefinitionreader)
    - [1.2. Spring容器初始化的入口](#12-spring容器初始化的入口)
    - [1.3. 基于Xml的IOC容器ClassPathXmlApplicationContext 的初始化](#13-基于xml的ioc容器classpathxmlapplicationcontext-的初始化)
        - [1.3.1. ClassPathXmlApplicationContext构造函数](#131-classpathxmlapplicationcontext构造函数)
        - [1.3.2. refresh()方法](#132-refresh方法)
        - [1.3.3. 容器初始化详解(obtainFreshBeanFactory()方法)](#133-容器初始化详解obtainfreshbeanfactory方法)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. BeanFactory与ApplicationContext
2. BeanDefinition： **<font color = "red">BeanDefinition中保存了Bean信息，比如这个Bean指向的是哪个类、是否是单例的、是否懒加载、这个Bean依赖了哪些Bean等。</font>**  
3. Spring容器刷新：刷新前的准备 ---> 创建容器 ---> 预处理 ---> 后置处理器 ---> 注册事件 ---> 特殊bean ---> 监听器 ---> 非懒加载Bean ---> 发布事件 
    **<font color = "red">Spring bean容器刷新的核心 12+1个步骤完成IoC容器的创建及初始化工作：</font>**  
    1. 刷新前的准备工作。  
    2. **<font color = "red">创建IoC容器(DefaultListableBeanFactory)，加载和注册BeanDefinition对象。</font>** 个人理解：此处仅仅相当于创建Spring Bean的类，实例化是在Spring DI里。   
        &emsp; **<font color = "clime">DefaultListableBeanFactory中使用一个HashMap的集合对象存放IOC容器中注册解析的BeanDefinition。</font>**  
        ```java
        private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
        ```
    3. 对IoC容器进行一些预处理。 为BeanFactory配置容器特性，例如设置BeanFactory的类加载器，配置了BeanPostProcessor，注册了三个默认bean实例，分别是“environment”、“systemProperties”、“systemEnvironment”。  
    4. 允许在上下文子类中对bean工厂进行后处理。 本方法没有具体实现，是一个扩展点，开发人员可以根据自己的情况做具体的实现。  
    5. **<font color = "red">调用BeanFactoryPostProcessor后置处理器对BeanDefinition处理。</font>**  
    6. **<font color = "red">注册BeanPostProcessor后置处理器。</font>**  
    7. 初始化一些消息源(比如处理国际化的i18n等消息源)。  
    8. **<font color = "red">初始化应用事件多播器。</font>**  
    9. **<font color = "red">具体的子类初始化一些特殊的bean在初始化。典型的模板方法(钩子方法)。</font>**  
    10. **<font color = "red">注册一些监听器。</font>**  
    11. **<font color = "red">实例化剩余的单例bean(非懒加载方式)。</font><font color = "clime">注意事项：Bean的IoC、DI和AOP都是发生在此步骤。</font>**  
    12. **<font color = "red">完成刷新时，发布对应的事件。</font>**  
    13. 重置公共的一些缓存数据。  





# 1. SpringIOC解析
<!--
《轻松读懂spring》之 IOC的主干流程（上） 
https://mp.weixin.qq.com/s/SZn9WRZjOuGXo2sX6TU0Uw

一文搞懂Spring上下文生命周期 | spring系列第55篇 
https://mp.weixin.qq.com/s/udo8TI4VPdd5-AYbLJpZgg
Spring IoC - IoC 容器初始化 源码解析 
https://juejin.cn/post/6844903967143493640
-->

## 1.1. IOC容器的设计与实现  
&emsp; 在Spring IOC容器的设计当中，可以看到两个主要的容器系列(根据命名)： 

* 实现了BeanFactory接口的简单容器系列，只实现了容器的最基本功能；  
* ApplicationContext应用上下文，容器的高级形态，增加了许多面向框架的特性和对应用环境的适配；  

&emsp; ⚠️注：在servlet中初始化的，用的是`WebApplicationContext extends ApplicationContext`。    

### 1.1.1. BeanFactory  
<!--  
在Spring中有许多的 IOC 容器的实现供用户选择和使用，其相互关系如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Spring/spring-3.png)  
-->
&emsp; Spring Bean的创建是典型的工厂模式，这一系列的Bean工厂，也即IOC容器为开发者管理对象间的依赖关系提供了很多便利和基础服务。  
&emsp; 其中BeanFactory作为最顶层的一个接口类，它定义了IOC容器的基本功能规范。BeanFactory有三个重要的子类：ListableBeanFactory、HierarchicalBeanFactory和AutowireCapableBeanFactory。它们最终的默认实现类是<font color = "red">DefaultListableBeanFactory</font>，它实现了所有的接口。  
&emsp; 为什么定义这么多层次的接口？查阅这些接口的源码和说明发现，每个接口都有使用的场合，主要是为了区分在Spring内部在操作过程中对象的传递和转化过程时，对对象的数据访问所做的限制。例如ListableBeanFactory接口表示这些Bean是可列表化的，而HierarchicalBeanFactory表示的是这些Bean是有继承关系的，也就是每个Bean有可能有父Bean。AutowireCapableBeanFactory 接口定义 Bean 的自动装配规则。这三个接口共同定义了Bean的集合、Bean之间的关系、以及Bean行为。  
&emsp; 最基本的IOC容器接口BeanFactory，来看一下它的源码：  

```java
public interface BeanFactory {
    //对 FactoryBean 的转义定义，因为如果使用bean 的名字检索 FactoryBean 得到的对象是工厂生成的对象，
    //如果需要得到工厂本身，需要转义String FACTORY_BEAN_PREFIX = "&";
    //根据 bean 的名字，获取在 IOC 容器中得到 bean 实例
    Object getBean(String name) throws BeansException;

    //根据 bean 的名字和 Class 类型来得到 bean 实例，增加了类型安全验证机制。
    <T> T getBean(String name, @Nullable Class<T> requiredType) throws BeansException;

    Object getBean(String name, Object... args) throws BeansException;
    <T> T getBean(Class<T> requiredType) throws BeansException;
    <T> T getBean(Class<T> requiredType, Object... args) throws BeansException;

    //提供对 bean 的检索，看看是否在 IOC 容器有这个名字的
    bean boolean containsBean(String name);

    //根据 bean 名字得到 bean 实例，并同时判断这个 bean 是不是单例
    boolean isSingleton(String name) throws NoSuchBeanDefinitionException; boolean isPrototype(String name) throws NoSuchBeanDefinitionException; boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException;
    boolean isTypeMatch(String name, @Nullable Class<?> typeToMatch) throws NoSuchBeanDefinitionException;
    //得到 bean 实例的 Class 类型
    @Nullable
    Class<?> getType(String name) throws NoSuchBeanDefinitionException;

    //得到 bean 的别名，如果根据别名检索，那么其原名也会被检索出来
    String[] getAliases(String name);

}
```  
&emsp; 在 BeanFactory 里只对IOC容器的基本行为作了定义，根本不关心Bean是如何定义怎样加载的。正如只关心工厂里得到什么的产品对象，至于工厂是怎么生产这些对象的，这个基本的接口不关心。  
&emsp; 而要知道工厂是如何产生对象的，需要看具体的IOC容器实现，Spring提供了许多IOC容器的实现。 比如GenericApplicationContext，ClasspathXmlApplicationContext等。   

### 1.1.2. ApplicationContext  
&emsp; <font color = "clime">ApplicationContext接口是BeanFactory的扩展，它除了具备BeanFactory接口所拥有的全部功能外，还有应用程序上下文的一层含义</font>，主要包括：  
1. 继承自ListableBeanFactory接口，<font color = "clime">可以访问Bean工厂上下文的组件；</font>  
2. 继承自ResourceLoader接口，以通用的方式加载文件资源；  
3. 继承自ApplicationContextPublisher接口，<font color = "clime">拥有发布事件注册监听的能力；</font>  
4. 继承自 MessageSource 接口，解析消息支持国际化。  

&emsp; 它最主要的实现是ClassPathXmlApplicationContext，用来读取XML配置文件，现在使用更多的是ClassPathXmlApplicationContext而不是 XMLBeanFactory。  
&emsp; Spring也提供了多种类型的容器实现，在不同的应用场景选择：  
1. AnnotationConfigApplicationContext：从一个或多个基于java的配置类中加载上下文定义，适用于java注解的方式；  
2. ClassPathXmlApplicationContext：从类路径下的一个或多个xml配置文件中加载上下文定义，适用于xml配置的方式；  
3. FileSystemXmlApplicationContext：从文件系统下的一个或多个xml配置文件中加载上下文定义，也就是说系统盘符中加载xml配置文件；  
4. AnnotationConfigWebApplicationContext：专门为web应用准备的，适用于注解方式；  
5. XmlWebApplicationContext：从web应用下的一个或多个xml配置文件加载上下文定义，适用于xml配置方式。  

#### 1.1.2.1. BeanFactory和ApplicationContext的区别  

|BeanFactory|ApplicationContext|
|---|---|
|它使用懒加载|它使用即时加载|
|它使用语法显式提供资源对象	|它自己创建和管理资源对象|
|不支持国际化|支持国际化|
|不支持基于依赖的注解|支持基于依赖的注解|  

&emsp; 选用哪个？  
&emsp; <font color = "red">BeanFactory是延迟加载，如果Bean的某一个属性没有注入，BeanFactory加载后，直至第一次使用调用getBean方法才会抛出异常。</font>  
&emsp; <font color = "red">ApplicationContext即时加载，在初始化自身时校验，这样有利于检查所依赖属性是否注入；所以通常情况下选择使用ApplicationContext。</font>  

### 1.1.3. BeanDefinition  
&emsp; 在这些Spring提供的基本IoC容器的接口定义和实现的基础上， **<font color = "clime">Spring通过定义BeanDefinition来管理基于Spring的应用中的各种对象以及它们之间的相互依赖关系。BeanDefinition中保存了Bean信息，比如这个Bean指向的是哪个类、是否是单例的、是否懒加载、这个Bean依赖了哪些Bean等。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Spring/spring-4.png)  
&emsp; BeanDefinition抽象了Bean的定义，是让容器起作用的主要数据类型。对IoC容器来说，BeanDefinition就是对依赖反转模式中管理的对象依赖关系的数据抽象，也是容器实现依赖反转功能的核心数据结构，依赖反转功能都是围绕对这个BeanDefinition的处理来完成的。  

### 1.1.4. BeanDefinitionReader  
&emsp; Bean的解析过程非常复杂，功能被分的很细，因为这里需要被扩展的地方很多，必须保证有足够的灵活性，以应对可能的变化。Bean的解析主要就是对Spring配置文件的解析。这个解析过程主要通过BeanDefintionReader来完成。  

----
## 1.2. Spring容器初始化的入口  
&emsp; 启动容器，实际上指实例化ApplicationContext。只是在不同情况下可能有不同的表现形式。  
&emsp; 1. ClassPathXmlApplicationContext通过XML配置  

```java
public static void main(String[] args) {
    ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationfile.xml");
}
```
&emsp; 2. AnnotationConfigApplicationContext通过java config类配置  

```java
@Configuration
@ComponentScan("ric.study.demo.ioc")
public class BeanDemoConfig {
    public static void main(String... strings) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(BeanDemoConfig.class);
        System.out.println("Spring container started and is ready");
    }
}
```

----

## 1.3. 基于Xml的IOC容器ClassPathXmlApplicationContext 的初始化  
### 1.3.1. ClassPathXmlApplicationContext构造函数
&emsp; 启动 IoC 容器，即实例化ClassPathXmlApplicationContext上下文，首先查看其构造函数：  

```java
public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {
    private Resource[] configResources;

    // 如果已经有 ApplicationContext 并需要配置成父子关系，那么调用这个构造方法
    public ClassPathXmlApplicationContext(ApplicationContext parent) {
        super(parent);
    }
    ...
    public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent)
            throws BeansException {

        super(parent);
        // 根据提供的路径，处理成配置文件数组(以分号、逗号、空格、tab、换行符分割)
        setConfigLocations(configLocations);
        if (refresh) {
            refresh(); // 核心方法
        }
    }
    //...
}
```
&emsp; 入参中的configLocations就是XML配置文件的classpath。  
&emsp; **super(parent);完成了BeanDefinition的resource的定位。**setConfigLocations(configLocations);把一些带有占位符的地址解析成实际的地址。  

### 1.3.2. refresh()方法  
&emsp; SpringIOC容器对Bean配置资源的载入是从refresh()函数开始的，refresh()是一个模板方法，规定了IOC容器的启动流程，有些逻辑要交给其子类去实现。 **<font color = "red">Spring bean容器刷新的核心 12+1个步骤完成IoC容器的创建及初始化工作：</font>**  
1. 刷新前的准备工作。  
2. **<font color = "red">创建IoC容器(DefaultListableBeanFactory)，加载和注册BeanDefinition对象。</font>**  
3. 对IoC容器进行一些预处理。为BeanFactory配置容器特性，例如设置BeanFactory的类加载器，配置了BeanPostProcessor，注册了三个默认bean实例，分别是“environment”、“systemProperties”、“systemEnvironment”。  
4. 允许在上下文子类中对bean工厂进行后处理。 本方法没有具体实现，是一个扩展点，开发人员可以根据自己的情况做具体的实现。  
5. **<font color = "red">调用BeanFactoryPostProcessor后置处理器对BeanDefinition处理。</font>**  
6. **<font color = "red">注册BeanPostProcessor后置处理器。</font>**  
7. 初始化一些消息源(比如处理国际化的i18n等消息源)。  
8. **<font color = "red">初始化应用事件多播器。</font>**  
9. **<font color = "red">具体的子类初始化一些特殊的bean在初始化。典型的模板方法(钩子方法)。</font>**  
10. **<font color = "red">注册一些监听器。</font>**  
11. **<font color = "red">实例化剩余的单例bean(非懒加载方式)。</font><font color = "clime">注意事项：Bean的IoC、DI和AOP都是发生在此步骤。</font>**    
12. **<font color = "red">完成刷新时，发布对应的事件。</font>**  
13. 重置公共的一些缓存数据。  

```java
// 完成IoC容器的创建及初始化工作
@Override
public void refresh() throws BeansException, IllegalStateException {
    //加锁
    synchronized (this.startupShutdownMonitor) {

        // 1： 刷新前的准备工作。
        // 记录下容器的启动时间、给容器设置同步标识(标记“已启动”状态)、处理配置文件中的占位符
        prepareRefresh();

        //  2：告诉子类刷新内部bean 工厂。子类启动 refreshBeanFactory()方法，Bean 定义资源文件的载入从子类的 refreshBeanFactory()方法启动
        //  内部创建了IoC容器(DefaultListableBeanFactory),加载解析XML文件，存储到Document对象中。读取Document对象，并完成BeanDefinition对象的加载和注册工作
        ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

        //  3： 对IoC容器进行一些预处理
        // 为 BeanFactory 配置容器特性，例如设置 BeanFactory 的类加载器，配置了BeanPostProcessor，注册了三个默认bean实例，分别是 “environment”、“systemProperties”、“systemEnvironment”
        prepareBeanFactory(beanFactory);

        try {

            //  4:  允许在上下文子类中对bean工厂进行后置处理。
            // 【这里需要知道 BeanFactoryPostProcessor 这个知识点，Bean 如果实现了此接口，那么在容器初始化以后，Spring 会负责调用里面的 postProcessBeanFactory 方法。】
            // 本方法没有具体实现，是一个扩展点，开发人员可以根据自己的情况做具体的实现。到这里的时候，所有的 Bean 都加载、注册完成了，但是都还没有初始化
            postProcessBeanFactory(beanFactory);

            //  5： 调用BeanFactoryPostProcessor后置处理器postProcessBeanFactory(factory) 方法对BeanDefinition处理
            invokeBeanFactoryPostProcessors(beanFactory);

            //  6： 注册BeanPostProcessor后置处理器
            // 注意看和 BeanFactoryPostProcessor 的区别
            // 此接口两个方法: postProcessBeforeInitialization 和 postProcessAfterInitialization
            // 两个方法分别在 Bean 初始化之前和初始化之后得到执行。注意，到这里 Bean 还没初始化
            registerBeanPostProcessors(beanFactory);

            //  7： 初始化一些消息源(比如处理国际化的i18n等消息源)， 不详述
            initMessageSource();

            //  8： 初始化容器事件传播器， 不详述
            initApplicationEventMulticaster();

            //  9： 具体的子类初始化一些特殊的bean在初始化 singleton beans 之前)
            //  从方法名就可以知道，典型的模板方法(钩子方法)
            onRefresh();

            //  10： 为事件传播器注册事件监听器
            registerListeners();

            //  11： 实例化剩余的单例bean(非懒加载方式)
            //      注意事项：Bean的IoC、DI和AOP都是发生在此步骤
            finishBeanFactoryInitialization(beanFactory);

            //  12： 完成刷新时，广播事件，发布ApplicationContext 初始化完成
            finishRefresh();
        }

        catch (BeansException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception encountered during context initialization - " +
                        "cancelling refresh attempt: " + ex);
            }

            // 销毁已经创建的单例，避免占用资源
            destroyBeans();

            // 取消 refresh 操作，重置容器的同步标识('active' 标签).
            cancelRefresh(ex);

            // 传播异常给调用者
            throw ex;
        }

        finally {

            // 13： 重置Spring核心中的常见内省缓存，因为可能不再需要单例bean的元数据了...
            resetCommonCaches();
        }
    }
}
```
&emsp; **refresh()方法主要为IOC容器Bean的生命周期管理提供条件。**  
&emsp; Spring 容器初始化，共经历了 13 步；其中尤其需要重点关注的是：  
&emsp; 步骤2，初始化Spring容器，并构建了BeanDefinition定义  

        ClassPathXmlApplicationContext容器的初始化包括BeanDefinition的resource定位、加载和注册这三个基本的过程。  
        1. BeanDefinition的resource的定位过程，就是找到定义bean的相关的xml文件，是通过继承ResourceLoader获得的，ResourceLoader代表了加载资源的一种方式，正是策略模式的实现。  
        2. BeanDifinition的载入，就是把用户在xml中定义好的bean解析成为IoC的内部数据结构，也就是BeanDifinition。  
        3. BeanDifinition的注册，向IoC容器注册这些BeanDifinition，把BeanDifinition注册到一个Map中保存。
    
&emsp; 步骤5，BeanFactoryPostProcessor，对BeanFactory做一些后置操作  
&emsp; 步骤6，BeanPostProcessor，对bean实例在初始化前后做一些增强工作  
&emsp; 步骤10，注册一些监听器。  
&emsp; 步骤11，对剩余所有的非懒加载的BeanDefinition(bean 定义)执行bean实例化操作。<font color = "clime">注意事项：Bean的IoC、DI和AOP都是发生在此步骤。</font>    

### 1.3.3. 容器初始化详解(obtainFreshBeanFactory()方法)  
&emsp; [容器初始化详解](/docs/SSM/Spring/容器初始化详解.md)  
