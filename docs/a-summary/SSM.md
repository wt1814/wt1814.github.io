
<!-- TOC -->

- [1. SSM](#1-ssm)
    - [1.1. Spring](#11-spring)
        - [1.1.1. Spring基础](#111-spring基础)
        - [1.1.2. Spring IOC](#112-spring-ioc)
        - [1.1.3. Spring依赖注入](#113-spring依赖注入)
            - [1.1.3.1. Spring DI](#1131-spring-di)
                - [1.1.3.1.1. Spring DI中循环依赖](#11311-spring-di中循环依赖)
                - [1.1.3.1.2. Bean的生命周期](#11312-bean的生命周期)
        - [1.1.4. IOC容器扩展](#114-ioc容器扩展)
            - [1.1.4.1. Spring可二次开发常用接口（扩展性）](#1141-spring可二次开发常用接口扩展性)
                - [1.1.4.1.1. FactoryBean](#11411-factorybean)
                - [1.1.4.1.2. 事件](#11412-事件)
                - [1.1.4.1.3. Aware接口](#11413-aware接口)
                - [1.1.4.1.4. 后置处理器](#11414-后置处理器)
                - [1.1.4.1.5. InitializingBean](#11415-initializingbean)
        - [1.1.5. SpringAOP教程](#115-springaop教程)
        - [1.1.6. SpringAOP解析](#116-springaop解析)
        - [1.1.7. Spring事务](#117-spring事务)
            - [1.1.7.1. Spring事务使用](#1171-spring事务使用)
            - [1.1.7.2. Spring事务问题](#1172-spring事务问题)
        - [1.1.8. SpringMVC解析](#118-springmvc解析)
        - [1.1.9. 过滤器、拦截器、监听器](#119-过滤器拦截器监听器)
    - [1.2. Mybatis面试题](#12-mybatis面试题)
        - [1.2.1. 解析和运行原理](#121-解析和运行原理)
            - [1.2.1.1. Mapper 接口的工作原理是什么？Mapper 接口里的方法，参数不同时，方法能重载吗？](#1211-mapper-接口的工作原理是什么mapper-接口里的方法参数不同时方法能重载吗)
        - [1.2.2. 映射器](#122-映射器)
            - [1.2.2.1. Mybatis是如何将sql执行结果封装为目标对象并返回的？都有哪些映射形式？](#1221-mybatis是如何将sql执行结果封装为目标对象并返回的都有哪些映射形式)
        - [1.2.3. 高级查询](#123-高级查询)
            - [1.2.3.1. Mybatis动态sql](#1231-mybatis动态sql)
            - [1.2.3.2. 大数据、批量操作](#1232-大数据批量操作)
        - [1.2.4. 插件](#124-插件)
            - [1.2.4.1. 简述Mybatis的插件运行原理，以及如何编写一个插件。](#1241-简述mybatis的插件运行原理以及如何编写一个插件)
            - [1.2.4.2. Mybatis分页](#1242-mybatis分页)
                - [1.2.4.2.1. Mybatis 是如何进行分页的？分页插件的原理是什么？](#12421-mybatis-是如何进行分页的分页插件的原理是什么)
                - [1.2.4.2.2. Mybatis实现分页功能](#12422-mybatis实现分页功能)
                    - [1.2.4.2.2.1. RowBounds（逻辑分页）](#124221-rowbounds逻辑分页)
                    - [1.2.4.2.2.2. MybatisPlus实现分页功能](#124222-mybatisplus实现分页功能)
                - [1.2.4.2.3. PageHelper踩坑：不安全分页导致的问题](#12423-pagehelper踩坑不安全分页导致的问题)
                - [1.2.4.2.4. 分页错误](#12424-分页错误)
                    - [1.2.4.2.4.1. 分页结果错误](#124241-分页结果错误)
                    - [1.2.4.2.4.2. order by和limit一起使用时的BUG](#124242-order-by和limit一起使用时的bug)
                - [1.2.4.2.5. 数据量过大时 limit分页效率，物理分页](#12425-数据量过大时-limit分页效率物理分页)
        - [1.2.5. 一级、二级缓存](#125-一级二级缓存)
            - [1.2.5.1. MyBatis大数据量查询](#1251-mybatis大数据量查询)
            - [1.2.5.2. MyBatis架构](#1252-mybatis架构)
            - [1.2.5.3. MyBatis SQL执行解析](#1253-mybatis-sql执行解析)
            - [1.2.5.4. SqlSession详解](#1254-sqlsession详解)
            - [1.2.5.5. Spring集成Mybatis](#1255-spring集成mybatis)
            - [1.2.5.6. MyBatis缓存](#1256-mybatis缓存)
            - [1.2.5.7. MyBatis插件解析](#1257-mybatis插件解析)

<!-- /TOC -->


# 1. SSM  
## 1.1. Spring
### 1.1.1. Spring基础
1. **@Autowired和@Resource之间的区别：**  
    1. @Autowired默认是按照类型装配注入的，默认情况下它要求依赖对象必须存在（可以设置它的required属性为false）。
    2. @Resource默认是按照名称来装配注入的，只有当找不到与名称匹配的bean才会按照类型来装配注入。  

### 1.1.2. Spring IOC
1. BeanFactory与ApplicationContext
    * BeanFactory作为最顶层的一个接口类，定义了IOC容器的基本功能规范。
    * <font color = "clime">ApplicationContext接口是BeanFactory的扩展，它除了具备BeanFactory接口所拥有的全部功能外，还有应用程序上下文的一层含义</font>，主要包括：  
        1. 继承自ListableBeanFactory接口，<font color = "clime">可以访问Bean工厂上下文的组件；</font>  
        2. 继承自ResourceLoader接口，以通用的方式加载文件资源；  
        3. 继承自ApplicationContextPublisher接口，<font color = "clime">拥有发布事件注册监听的能力；</font>  
        4. 继承自 MessageSource 接口，解析消息支持国际化。  
2. **<font color = "red">BeanDefinition中保存了Bean信息，比如这个Bean指向的是哪个类、是否是单例的、是否懒加载、这个Bean依赖了哪些Bean等。</font>**  
3. Spring容器刷新：  
    ![image](http://182.92.69.8:8081/img/draw/SpringIOC.png)  
    **<font color = "blue">（⚠★★★`利用工厂和反射创建Bean。主要包含3部分：1).（1---3步）容器本身--创建容器、2).（4---6步）容器扩展--后置处理器、3).事件，子容器，实例化Bean。`）</font>**     
    **<font color = "red">Spring bean容器刷新的核心 12个步骤完成IoC容器的创建及初始化工作：</font>**  
    1. `刷新前`的准备工作。  
    2. **<font color = "red">`创建IoC容器`(DefaultListableBeanFactory)，加载和注册BeanDefinition对象。</font>** <font color = "blue">`个人理解：此处仅仅相当于创建Spring Bean的类，实例化是在Spring DI里。`</font>   
        &emsp; **<font color = "clime">DefaultListableBeanFactory中使用一个HashMap的集合对象存放IOC容器中注册解析的BeanDefinition。</font>**  
        ```java
        private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
        ```
    3. 对IoC容器进行一些`预处理`。  
    &emsp; 为BeanFactory配置容器特性，例如设置BeanFactory的类加载器，配置了BeanPostProcessor，注册了三个默认bean实例，分别是“environment”、“systemProperties”、“systemEnvironment”。  
    -----------
    4. 允许在上下文子类中对bean工厂进行后处理。（开发者定义自己的后置处理器。）    
    5. **<font color = "red">调用BeanFactoryPostProcessor`后置处理器`对BeanDefinition处理（修改BeanDefinition对象）。</font>**  
    6. **<font color = "red">注册BeanPostProcessor后置处理器。</font>**  
    ------------ 
    7. 初始化一些消息源（比如处理国际化的i18n等消息源）。 
    8. **<font color = "red">初始化应用[事件多播器](/docs/SSM/Spring/feature/EventMulticaster.md)。</font>**     
    9. **<font color = "red">`onRefresh()，典型的模板方法(钩子方法)。不同的Spring容器做不同的事情。`比如web程序的容器ServletWebServerApplicationContext中会调用createWebServer方法去创建内置的Servlet容器。</font>**  
    10. **<font color = "red">注册一些监听器到事件多播器上。</font>**  
    11. **<font color = "red">`实例化剩余的单例bean(非懒加载方式)。`</font><font color = "blue">`注意事项：Bean的IoC、DI和AOP都是发生在此步骤。`</font>**  
    12. **<font color = "red">完成刷新时，发布对应的事件。</font>**  
    13. 重置公共的一些缓存数据。  

### 1.1.3. Spring依赖注入
#### 1.1.3.1. Spring DI
1. 加载时机：  
    1. 非懒加载，即容器启动时就加载。SpringBean默认单例。  
    &emsp; 查看AbstractApplicationContext.`refresh()`方法中实例化所有非懒加载的bean过程即方法`finishBeanFactoryInitialization`，查看此方法最后一行beanFactory.preInstantiateSingletons()实际调用此方法。  
2. 加载流程：  
    1. doGetBean()，获取Bean。  
    2. doCreateBean()创建Bean有三个关键步骤：  
    &emsp; 2.createBeanInstance()实例化、  
    &emsp; 5.populateBean()属性填充、  
    &emsp; 6.initializeBean()初始化。  

##### 1.1.3.1.1. Spring DI中循环依赖
1. Spring循环依赖的场景：均采用setter方法（属性注入）注入方式，可被解决；采用构造器和setter方法（属性注入）混合注入方式可能被解决。
2. **<font color = "red">Spring通过`三级缓存`解决：</font>**  
    ![image](http://182.92.69.8:8081/img/SSM/Spring/spring-20.png)  
    * 三级缓存: Map<String,ObjectFactory<?>> singletonFactories，`早期曝光对象工厂`，用于保存bean创建工厂，以便于后面扩展有机会创建代理对象。  
    * 二级缓存: Map<String,Object> earlySingletonObjects， **<font color = "blue">`早期曝光对象`</font>** ，`二级缓存，用于存放已经被创建，但是尚未初始化完成的Bean。`尚未经历了完整的Spring Bean初始化生命周期。
    * 一级缓存: Map<String,Object> singletonObjects，`单例对象池`，用于保存实例化、注入、初始化完成的bean实例。经历了完整的Spring Bean初始化生命周期。
3. **<font color = "clime">单例模式下Spring解决循环依赖的流程：</font>**  
    ![image](http://182.92.69.8:8081/img/SSM/Spring/spring-22.png)  
    1. Spring创建bean主要分为两个步骤，创建原始bean对象，接着去填充对象属性和初始化。  
    2. 每次创建bean之前，都会从缓存中查下有没有该bean，因为是单例，只能有一个。  
    3. 当创建beanA的原始对象后，并把它`放到三级缓存`中，接下来就该填充对象属性了，这时候发现依赖了beanB，接着就又去创建 beanB，同样的流程，创建完beanB填充属性时又发现它依赖了beanA，又是同样的流程，不同的是，这时候可以在三级缓存中查到刚放进去的原始对象beanA，所以不需要继续创建，用它注入beanB，完成beanB的创建。此时会`将beanA从三级缓存删除，放到二级缓存`。   
    4. 既然 beanB 创建好了，所以 beanA 就可以完成填充属性的步骤了，接着执行剩下的逻辑，闭环完成。  
    ---
    &emsp; 当A、B两个类发生循环引用时，在A完成实例化后，就使用实例化后的对象去创建一个对象工厂，并添加到三级缓存中。 **<font color = "blue">`如果A被AOP代理，那么通过这个工厂获取到的就是A代理后的对象，如果A没有被AOP代理，那么这个工厂获取到的就是A实例化的对象。`</font>** 当A进行属性注入时，会去创建B，同时B又依赖了A，所以创建B的同时又会去调用getBean(a)来获取需要的依赖，此时的getBean(a)会从缓存中获取：  

    * 第一步，先获取到三级缓存中的工厂。  
    * 第二步，调用对象工厂的getObject方法来获取到对应的对象，得到这个对象后将其注入到B中。紧接着B会走完它的生命周期流程，包括初始化、后置处理器等。  

    当B创建完后，会将B再注入到A中，此时A再完成它的整个生命周期。  
4. 常见问题
    1. 二级缓存能解决循环依赖嘛？  
    &emsp; 二级缓存可以解决循环依赖。  
    &emsp; 如果创建的Bean有对应的代理，那其他对象注入时，注入的应该是对应的代理对象；但是Spring无法提前知道这个对象是不是有循环依赖的情况，而正常情况下（没有循环依赖情况），Spring都是在创建好完成品Bean之后才创建对应的代理。这时候Spring有两个选择：

        * 方案一：不管有没有循环依赖，都提前创建好代理对象，并将代理对象放入缓存，出现循环依赖时，其他对象直接就可以取到代理对象并注入。
        * 方案二：不提前创建好代理对象，在出现循环依赖被其他对象注入时，才实时生成代理对象。这样在没有循环依赖的情况下，Bean就可以按着Spring设计原则的步骤来创建。  

    &emsp; `如果使用二级缓存解决循环依赖，即采用方案一，意味着所有Bean在实例化后就要完成AOP代理，`这样违背了Spring设计的原则，Spring在设计之初就是通过AnnotationAwareAspectJAutoProxyCreator这个后置处理器来在Bean生命周期的最后一步来完成AOP代理，而不是在实例化后就立马进行AOP代理。   
    &emsp; **怎么做到提前曝光对象而又不生成代理呢？**   
    &emsp; Spring就是在对象外面包一层ObjectFactory（三级缓存存放），提前曝光的是ObjectFactory对象，在被注入时才在ObjectFactory.getObject方式内实时生成代理对象，并将生成好的代理对象放入到第二级缓存Map\<String, Object> earlySingletonObjects。  

##### 1.1.3.1.2. Bean的生命周期
&emsp; **<font color = "red">SpringIOC阶段初始化容器，为SpringBean的生命周期提供环境准备。SpringDI阶段讲述了SpringBean生命周期的前半部分。</font>**  
![image](http://182.92.69.8:8081/img/SSM/Spring/spring-10.png)  
&emsp; SpringBean的生命周期主要有4个阶段：  
1. 实例化（Instantiation），可以理解为new一个对象；
2. 属性赋值（Populate），可以理解为调用setter方法完成属性注入；
3. 初始化（Initialization），包含：  
    * 激活Aware方法  
    * 前置处理  
    * 激活自定义的init方法 
    * 后置处理 
4. 销毁（Destruction）---注册Destruction回调函数。  

&emsp; Spring Bean的生命周期管理的基本思路是：在Bean出现之前，先准备操作Bean的BeanFactory，然后操作完Bean，所有的Bean也还会交给BeanFactory进行管理。再所有Bean操作准备BeanPostProcessor作为回调。 

### 1.1.4. IOC容器扩展 
&emsp; Spring的扩展点有IOC容器扩展、AOP扩展...  

#### 1.1.4.1. Spring可二次开发常用接口（扩展性）
&emsp; Spring为了用户的开发方便和特性支持，开放了一些特殊接口和类，用户可进行实现或者继承，常见的有：  

&emsp; FactoryBean  

&emsp; **Spring IOC阶段：**  
&emsp; [事件](/docs/SSM/Spring/feature/Event.md)  

&emsp; **Spring DI阶段：**  
&emsp; [Aware接口](/docs/SSM/Spring/feature/Aware.md)  
&emsp; [后置处理器](/docs/SSM/Spring/feature/BeanFactoryPostProcessor.md)  
&emsp; [InitializingBean](/docs/SSM/Spring/feature/InitializingBean.md)  

##### 1.1.4.1.1. FactoryBean
1. BeanFactory  
&emsp; BeanFactory是个Factory，也就是IOC容器或对象工厂；FactoryBean是个Bean，也由BeanFactory管理。  
2. FactoryBean：`⚠️FactoryBean，工厂Bean，首先是个Bean，其次再加上工厂模式。`  
&emsp; 一般情况下，Spring通过`反射机制`利用\<bean\>的class属性指定实现类实例化Bean。 **<font color = "red">在某些情况下，实例化Bean过程比较复杂，</font>** 如果按照传统的方式，则需要在\<bean>中提供大量的配置信息。配置方式的灵活性是受限的，这时采用编码的方式可能会得到一个简单的方案。 **<font color = "red">Spring为此提供了一个org.springframework.bean.factory.FactoryBean的`工厂类接口，用户可以通过实现该接口定制实例化Bean的逻辑。`</font>**  
&emsp; **<font color = "red">FactoryBean接口的一些实现类，如Spring自身提供的ProxyFactoryBean、JndiObjectFactoryBean，还有Mybatis中的SqlSessionFactoryBean，</font>** 用于生产一些复杂的Bean。  


##### 1.1.4.1.2. 事件
&emsp; **<font color = "clime">★★★Spring事件机制的流程：</font>**   
1. **<font color = "clime">事件机制的核心是事件。</font>** Spring中的事件是ApplicationEvent。Spring提供了5个标准事件，此外还可以自定义事件（继承ApplicationEvent）。  
2. **<font color = "clime">确定事件后，要把事件发布出去。</font>** 在事件发布类的业务代码中调用ApplicationEventPublisher#publishEvent方法（或调用ApplicationEventPublisher的子类，例如调用ApplicationContext#publishEvent）。  
3. **<font color = "blue">`发布完成之后，启动监听器，自动监听。`</font>** 在监听器类中覆盖ApplicationListener#onApplicationEvent方法。  
4. 最后，就是实际场景中触发事件发布，完成一系列任务。  


&emsp; **<font color = "clime">5个标准事件：</font>**   

* 上下文更新事件（ContextRefreshedEvent）：在调用ConfigurableApplicationContext接口中的refresh()方法时被触发。  
* 上下文开始事件（ContextStartedEvent）：当容器调用ConfigurableApplicationContext的Start()方法开始/重新开始容器时触发该事件。  
* 上下文停止事件（ContextStoppedEvent）：当容器调用ConfigurableApplicationContext的Stop()方法停止容器时触发该事件。  
* 上下文关闭事件（ContextClosedEvent）：当ApplicationContext被关闭时触发该事件。容器被关闭时，其管理的所有单例Bean都被销毁。  
* 请求处理事件（RequestHandledEvent）：在Web应用中，当一个http请求（request）结束触发该事件。如果一个bean实现了ApplicationListener接口，当一个ApplicationEvent被发布以后，bean会自动被通知。  

##### 1.1.4.1.3. Aware接口
&emsp; **<font color = "clime">容器管理的Bean一般不需要了解容器的状态和直接使用容器，但在某些情况下，是需要在Bean中直接对IOC容器进行操作的，这时候，就需要在Bean中设定对容器的感知。Spring IOC容器也提供了该功能，它是通过特定的aware接口来完成的。</font>** aware接口有以下这些：

* BeanNameAware，可以在Bean中得到它在IOC容器中的Bean实例名称。  
* BeanFactoryAware，可以在Bean中得到Bean所在的IOC容器，从而直接在Bean中使用IOC容器的服务。  
* ApplicationContextAware，可以在Bean中得到Bean所在的应用上下文，从而直接在 Bean中使用应用上下文的服务。  
* MessageSourceAware，在Bean中可以得到消息源。  
* ApplicationEventPublisherAware，在Bean中可以得到应用上下文的事件发布器，从而可以在Bean中发布应用上下文的事件。  
* ResourceLoaderAware，在Bean中可以得到ResourceLoader，从而在Bean中使用ResourceLoader加载外部对应的Resource资源。</font>  

&emsp; 在设置Bean的属性之后，调用初始化回调方法之前，Spring会调用aware接口中的setter方法。  

##### 1.1.4.1.4. 后置处理器
1. <font color = "clime">实现BeanFactoryPostProcessor接口，可以`在spring的bean创建之前，修改bean的定义属性（BeanDefinition）`。</font>  
2. <font color = "red">实现BeanPostProcessor接口，</font><font color = "blue">可以在spring容器实例化bean之后，`在执行bean的初始化方法前后，`添加一些自己的处理逻辑。</font>  

##### 1.1.4.1.5. InitializingBean
&emsp; ......  

### 1.1.5. SpringAOP教程
1. SpringAOP的主要功能是：日志记录，性能统计，安全控制，事务处理，异常处理等。 
    * 慢请求记录  
    * 使用aop + redis + Lua接口限流
2. `SpringAOP失效：`  
&emsp; 参考[Spring事务失效](/docs/SSM/Spring/SpringTransactionInvalid.md)  
&emsp; <font color = "red">同一对象内部方法嵌套调用，慎用this来调用被@Async、@Transactional、@Cacheable等注解标注的方法，this下注解可能不生效。</font>async方法中的this不是动态代理的子类对象，而是原始的对象，故this调用无法通过动态代理来增强。 
3. **<font color = "red">过滤器，拦截器和aop的区别：</font>** 过滤器拦截的是URL；拦截器拦截的是URL；Spring AOP只能拦截Spring管理Bean的访问（业务层Service）。  

### 1.1.6. SpringAOP解析
1. **<font color = "blue">自动代理触发的时机：AspectJAnnotationAutoProxyCreator是一个【后置处理器BeanPostProcessor】，</font>** 因此Spring AOP是在这一步，进行代理增强！  
2. **<font color = "clime">代理类的生成流程：1). `获取当前的Spring Bean适配的advisors；`2). `创建代理类`。</font>**   
    1. Spring AOP获取对应Bean适配的Advisors链的核心逻辑：
        1. 获取当前IoC容器中所有的Aspect类。
        2. 给每个Aspect类的advice方法创建一个Spring Advisor，这一步又能细分为： 
            1. 遍历所有 advice 方法。
            2. 解析方法的注解和pointcut。
            3. 实例化 Advisor 对象。
        3. 获取到候选的 Advisors，并且`缓存`起来，方便下一次直接获取。
        4. 从候选的Advisors中筛选出与目标类适配的Advisor。 
            1. 获取到Advisor的切入点pointcut。
            2. 获取到当前target类所有的public方法。
            3. 遍历方法，通过切入点的methodMatcher匹配当前方法，只要有一个匹配成功就相当于当前的Advisor适配。
        5. 对筛选之后的Advisor链进行排序。  
    2. 创建代理类
        1. 创建AopProxy。根据ProxyConfig 获取到了对应的AopProxy的实现类，分别是JdkDynamicAopProxy和ObjenesisCglibAopProxy。 
        2. 获取代理类。

### 1.1.7. Spring事务
#### 1.1.7.1. Spring事务使用  
1. `@Transactional(rollbackFor = Exception.class) `，Transactional`默认只回滚RuntimeException，`但是可以指定要回滚的异常类型。    
2. **<font color = "red">Spring事务属性通常由事务的传播行为、事务的隔离级别、事务的超时值、事务只读标志组成。</font>**  
    * 事务的传播行为主要分为支持当前事务和不支持当前事务。  
        &emsp; <font color = "red">PROPAGATION_REQUIRED：如果当前存在事务，则加入该事务，合并成一个事务；如果当前没有事务，则创建一个新的事务。这是默认值。</font>  
        &emsp; 下面的类型都是针对于被调用方法来说的，理解起来要想象成两个service 方法的调用才可以。  
        &emsp; **支持当前事务的情况：**  
        &emsp; 1. <font color = "red">PROPAGATION_REQUIRED：如果当前存在事务，则加入该事务，合并成一个事务；如果当前没有事务，则创建一个新的事务。这是默认值。</font>  
        &emsp; 2. PROPAGATION_SUPPORTS：如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。  
        &emsp; 3. PROPAGATION_MANDATORY：如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常，即父级方法必须有事务。  

        &emsp; **不支持当前事务的情况：**  
        &emsp; 4. PROPAGATION_REQUIRES_NEW：创建一个新的事务，如果当前存在事务，则把当前事务挂起。这个方法会独立提交事务，不受调用者的事务影响，父级异常，它也是正常提交。  
        &emsp; 5. PROPAGATION_NOT_SUPPORTED：以非事务方式运行，如果当前存在事务，则把当前事务挂起。  
        &emsp; 6. PROPAGATION_NEVER：以非事务方式运行，如果当前存在事务，则抛出异常，即父级方法必须无事务。  

        &emsp; **其他情况：**  
        &emsp; 7. PROPAGATION_NESTED：如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于PROPAGATION_REQUIRED。  
        &emsp; 嵌套事务是外部事务的一部分，只有外部事务结束后它才会被提交。由此可见，PROPAGATION_REQUIRES_NEW和PROPAGATION_NESTED的最大区别在于：PROPAGATION_REQUIRES_NEW完全是一个新的事务，而PROPAGATION_NESTED则是外部事务的子事务，如果外部事务commit，嵌套事务也会被commit， 这个规则同样适用于roll back。  
    * 事务的隔离级别，默认使用底层数据库的默认隔离级别，其他四个隔离级别和数据库的隔离级别一致。  
    * 事务只读，相当于将数据库设置成只读数据库，此时若要进行写的操作，会出现错误。  

#### 1.1.7.2. Spring事务问题
1. Spring事务失效：  
    * 捕获了异常，未再抛出。
    * 同一个类中方法调用。
    * 使用在了非public方法上。
    * @Transactional的类注入失败。
    * 多数据源（静态配置）
    * 原始SSM项目，重复扫描导致事务失效  
2. `大事务问题：将修改库的代码聚合在一起。`   


### 1.1.8. SpringMVC解析
1. **SpringMVC的工作流程：**  
    1. 找到处理器：前端控制器DispatcherServlet ---> **<font color = "red">处理器映射器HandlerMapping</font>** ---> 找到处理器Handler；  
    2. 处理器处理：前端控制器DispatcherServlet ---> **<font color = "red">处理器适配器HandlerAdapter</font>** ---> 处理器Handler ---> 执行具体的处理器Controller（也叫后端控制器） ---> Controller执行完成返回ModelAndView；  
    &emsp; 1. 处理器映射器HandlerMapping：根据请求的url查找Handler即处理器（Controller）。  
    &emsp; 2. **<font color = "blue">处理器适配器HandlAdapter：按照特定规则（HandlerAdapter要求的规则）去执行Handler。通过HandlerAdapter对处理器进行执行，这是适配器模式的应用，通过扩展适配器可以对更多类型的处理器进行执行。</font>**  
    &emsp; 3. 处理器Handler和controller区别：
    3. 返回前端控制器DispatcherServlet ---> 视图解析器ViewReslover。  
2. **SpringMVC解析：**  
    1. 在SpringMVC.xml中定义一个DispatcherServlet和一个监听器ContextLoaderListener。  
    2. 上下文在web容器中的启动：<font color = "red">由ContextLoaderListener启动的上下文为根上下文。在根上下文的基础上，还有一个与Web MVC相关的上下文用来保存控制器（DispatcherServlet）需要的MVC对象，作为根上下文的子上下文，构成一个层次化的上下文体系。</font>  
    3. **<font color = "red">`DispatcherServlet初始化和使用：`</font>**     
        1. 初始化阶段。DispatcherServlet的初始化在HttpServletBean#init()方法中。 **<font color = "red">`完成Spring MVC的组件的初始化。`</font>**    
        2. 调用阶段。这一步是由请求触发的。入口为DispatcherServlet#doService() ---> DispatcherServlet#doDispatch()。 **<font color = "blue">`逻辑即为SpringMVC处理流程。`</font>**   


### 1.1.9. 过滤器、拦截器、监听器
&emsp; 过滤前-拦截前-action执行-拦截后-过滤后  


## 1.2. Mybatis面试题

### 1.2.1. 解析和运行原理
#### 1.2.1.1. Mapper 接口的工作原理是什么？Mapper 接口里的方法，参数不同时，方法能重载吗？
&emsp; Dao 接口即 Mapper 接口。接口的全限名，就是映射文件中的 namespace 的值；接口的方法名，就是映射文件中 Mapper 的 Statement 的 id 值；接口方法内的参数，就是传递给 sql 的参数。  
&emsp; Mapper 接口是没有实现类的，当调用接口方法时，接口全限名+方法名拼接字符串作为 key 值，可唯一定位一个 MapperStatement。在 Mybatis 中，每一个 <select>、<insert>、<update>、<delete>标签，都会被解析为一个MapperStatement 对象。  

&emsp; 举例：com.mybatis3.mappers.StudentDao.findStudentById，可以唯一找到 namespace 为com.mybatis3.mappers.StudentDao 下面 id 为findStudentById 的 MapperStatement。  
&emsp; Mapper 接口里的方法，是不能重载的，因为是使用 全限名+方法名 的保存和寻找策略。Mapper 接口的工作原理是 JDK 动态代理，Mybatis 运行时会使用 JDK动态代理为 Mapper 接口生成代理对象 proxy，代理对象会拦截接口方法，转而执行 MapperStatement 所代表的 sql，然后将 sql 执行结果返回。  

### 1.2.2. 映射器
#### 1.2.2.1. Mybatis是如何将sql执行结果封装为目标对象并返回的？都有哪些映射形式？
&emsp; 第一种是使用标签，逐一定义数据库列名和对象属性名之间的映射关系。   
&emsp; 第二种是使用 sql 列的别名功能，将列的别名书写为对象属性名。  
&emsp; 有了列名与属性名的映射关系后，Mybatis 通过反射创建对象，同时使用反射给对象的属性逐一赋值并返回，那些找不到映射关系的属性，是无法完成赋值的。  


### 1.2.3. 高级查询
#### 1.2.3.1. Mybatis动态sql
&emsp; Mybatis动态sql是做什么的？都有哪些动态sql？能简述一下动态sql的执行原理不？   
&emsp; Mybatis动态sql可以让我们在Xml映射文件内，以标签的形式编写动态sql，完成逻辑判断和动态拼接sql的功能。   
&emsp; Mybatis提供了9种动态sql标签：trim|where|set|foreach|if|choose|when|otherwise|bind。  
&emsp; 其执行原理为，使用OGNL从sql参数对象中计算表达式的值，根据表达式的值动态拼接sql，以此来完成动态sql的功能。  

#### 1.2.3.2. 大数据、批量操作 
1. 批量插入
    &emsp; 两种方式：  
    1. foreach，
        ```java
        VALUES
            <foreach>
        ```
    2. 使用Batch Insert技术  
        ```java
        BatchInsertMapper insertMapper = session.getMapper(BatchInsertMapper.class);
        ```
2. Cursor的接口类用于流式查询


### 1.2.4. 插件
#### 1.2.4.1. 简述Mybatis的插件运行原理，以及如何编写一个插件。
&emsp; Mybatis仅可以编写针对ParameterHandler、ResultSetHandler、StatementHandler、Executor这4种接口的插件，Mybatis使用JDK的动态代理，为需要拦截的接口生成代理对象以实现接口方法拦截功能，每当执行这4种接口对象的方法时，就会进入拦截方法，具体就是InvocationHandler的invoke()方法，当然，只会拦截那些你指定需要拦截的方法。  
&emsp; 实现Mybatis的Interceptor接口并复写intercept()方法，然后在给插件编写注解，指定要拦截哪一个接口的哪些方法即可，记住，别忘了在配置文件中配置你编写的插件。

#### 1.2.4.2. Mybatis分页
##### 1.2.4.2.1. Mybatis 是如何进行分页的？分页插件的原理是什么？  
&emsp; Mybatis 使用 RowBounds 对象进行分页，它是针对 ResultSet 结果集执行的内存分页，而非物理分页。可以在 sql 内直接书写带有物理分页的参数来完成物理分页功能，也可以使用分页插件来完成物理分页。  
&emsp; 分页插件的基本原理是使用 Mybatis 提供的插件接口，实现自定义插件，在插件的拦截方法内拦截待执行的 sql，然后重写 sql，根据 dialect 方言，添加对应的物理分页语句和物理分页参数。   

##### 1.2.4.2.2. Mybatis实现分页功能  
<!-- 
https://blog.csdn.net/weixin_51262054/article/details/131368161
-->
&emsp; mybatis实现分页有：  
* 直接使用SQL语句，利用limit关键字分页（物理分页）  
* RowBounds（逻辑分页）  
* 第三方插件PageHelper（物理分页）  
* MybatisPlus实现分页功能  

###### 1.2.4.2.2.1. RowBounds（逻辑分页）   
在mapper接口中  

```java
@Select("select count(*) from role")
int allRoleCount();

@Select("select * from role")
List<Role>  pageRowBoundsRole(RowBounds rowBounds);
```

###### 1.2.4.2.2.2. MybatisPlus实现分页功能

 

##### 1.2.4.2.3. PageHelper踩坑：不安全分页导致的问题
<!-- 
https://www.jianshu.com/p/88d1eca40271
https://blog.51cto.com/u_15127625/3892010
mybatis使用PageHelper的bug之第一次缓存后会自己分页
https://blog.csdn.net/qq_36635569/article/details/112674497
-->

##### 1.2.4.2.4. 分页错误
###### 1.2.4.2.4.1. 分页结果错误  
&emsp; 示例一：  
```java
page 
page 
mapper1.select()
mapper2.select();
```

示例二:  
```java
PageHelper.setPage(1,10);
if(param!=null){
    list=userMapper.selectIf(param)
}eles{
    list=new ArrayList<User>();
}
```
&emsp; 主要原因：PageHelper 使用了静态的 ThreadLocal 参数，让线程绑定了分页参数， 这个参数如果没被使用就会一直留在那儿，当这个线程再次被使用时，就可能导致不该分页的方法去消费这个分页参数，这就产生了莫名其妙的分页。  
&emsp; 如果你对此不放心，你可以手动清理 ThreadLocal 存储的分页参数：PageHelper.clearPage();


###### 1.2.4.2.4.2. order by和limit一起使用时的BUG
<!-- 
https://www.jianshu.com/p/88d1eca40271
https://www.cnblogs.com/goloving/p/15203934.html
-->
&emsp; error2：sql语句中已经写了limit，pagehelper又拼接了一次，出现 'limit 1 limit 10’的情况；  
&emsp; 通过百度，了解到PageHelper使用了静态的ThreadLocal参数，分页参数和线程是绑定的；当分页参数没有被消费时，会一直存在threadlocal中，在下一次执行的sql中会拼接这些参数。  
&emsp; 那么怎么避免这种情况：分页参数紧跟 list 查询。如果先写分页，又写了别的判断逻辑，没有执行 list 查询时，那么分页参数就会在threadlocal中，下次执行sql会消费这些参数，就会导致“不安全分页”。  

##### 1.2.4.2.5. 数据量过大时 limit分页效率，物理分页
<!-- 

https://www.jianshu.com/p/88d1eca40271
-->



### 1.2.5. 一级、二级缓存  
&emsp; 一级缓存: 基于 PerpetualCache 的 HashMap 本地缓存，其存储作用域为 Session，当 Session flush 或 close 之后，该 Session 中的所有 Cache 就 将清空，默认打开一级缓存。  
&emsp; 二级缓存与一级缓存其机制相同，默认也是采用 PerpetualCache，HashMap存储，不同在于其存储作用域为 Mapper(Namespace)，并且可自定义存储源，如 Ehcache。默认不打开二级缓存，要开启二级缓存，使用二级缓存属性类需要实现 Serializable 序列化接口(可用来保存对象的状态),可在它的映射文件中配置；  
&emsp; 对于缓存数据更新机制，当某一个作用域(一级缓存 Session/二级缓存Namespaces)的进行了 C/U/D 操作后，默认该作用域下所有 select 中的缓存将被 clear。  



#### 1.2.5.1. MyBatis大数据量查询
1. `流式查询（针对查询结果集比较大）`  
&emsp; 流式查询指的是查询成功后不是返回一个集合而是返回一个迭代器，应用每次从迭代器取一条查询结果。流式查询的好处是能够降低内存使用。  
&emsp; **<font color = "clime">如果没有流式查询，想要从数据库取 1000 万条记录而又没有足够的内存时，就不得不分页查询，而分页查询效率取决于表设计，如果设计的不好，就无法执行高效的分页查询。因此流式查询是一个数据库访问框架必须具备的功能。</font>**  
&emsp; 流式查询的过程当中，数据库连接是保持打开状态的，因此要注意的是： **<font color = "clime">执行一个流式查询后，数据库访问框架就不负责关闭数据库连接了，需要应用在取完数据后自己关闭。</font>**  

#### 1.2.5.2. MyBatis架构
&emsp; **<font color = "red">Mybatis的功能架构分为三层：</font>**  

* API接口层：提供给外部使用的接口API，开发人员通过这些本地API来操纵数据库。接口层一接收到调用请求就会调用核心处理层来完成具体的数据处理。  
* 核心处理层：负责具体的SQL查找、SQL解析、SQL执行和执行结果映射处理等。它主要的目的是根据调用的请求完成一次数据库操作。  
* 基础支持层：负责最基础的功能支撑，包括连接管理、事务管理、配置加载和缓存处理，这些都是共用的东西，将它们抽取出来作为最基础的组件。为上层的数据处理层提供最基础的支撑。  

#### 1.2.5.3. MyBatis SQL执行解析
1. Mybatis Sql执行流程：   
    1. 读取核心配置文件并返回InputStream流对象。
    2. 根据InputStream流对象解析出Configuration对象，然后创建SqlSessionFactory工厂对象。
    3. 根据一系列属性从SqlSessionFactory工厂中创建SqlSession。
    4. 从SqlSession中调用Executor执行数据库操作和生成具体SQL指令。
    5. 对执行结果进行二次封装。
    6. 提交与事务。      
2. **<font color = "clime">Mapper接口动态代理类的生成：</font>** 
    * 生成代理工厂类：  
    &emsp; **<font color = "blue">解析配置文件生成sqlSessionFactory时，</font>** 会调用bindMapperForNamespace() ---> addMapper()方法， **<font color = "blue">根据mapper文件中的namespace属性值，`将接口生成动态代理类的工厂，存储在MapperRegistry对象中`。</font>** （MapperRegistry内部维护一个映射关系，每个接口对应一个`MapperProxyFactory（生成动态代理工厂类）`。）      
    * 生成对应Mapper的代理类：    
    &emsp; 在调用getMapper，根据type类型，从MapperRegistry对象中的knownMappers获取到当前类型对应的代理工厂类，然后通过代理工厂类使用`jdk自带的动态代理`生成对应Mapper的代理类。  
    ```java
    //这里可以看到每次调用都会创建一个新的代理对象返回
    return mapperProxyFactory.newInstance(sqlSession);
    ```

    ```java
    public T newInstance(SqlSession sqlSession) {
        //首先会调用这个newInstance方法
        //动态代理逻辑在MapperProxy里面
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
        //通过这里调用下面的newInstance方法
        return newInstance(mapperProxy);
    }
    @SuppressWarnings("unchecked")
    protected T newInstance(MapperProxy<T> mapperProxy) {
        //jdk自带的动态代理
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
    }
    ```

#### 1.2.5.4. SqlSession详解  


#### 1.2.5.5. Spring集成Mybatis  
0. MyBatis运行原理：1.创建SqlSessionFacory；2.从SqlSessionFactory对象中获取SqlSession对象；3.获取Mapper；4.执行操作；    
1. 创建SqlSessionFacory  
&emsp; MyBatis-Spring中创建SqlSessionFacory是由SqlSessionFactoryBean完成的。实现了InitializingBean接口、FactoryBean接口、ApplicationListener接口。  
2. 创建SqlSession   
&emsp; 在Spring中并没有直接使用DefaultSqlSession，DefaultSqlSession是线程不安全的。Spring对SqlSession 进行了一个封装，这个SqlSession的实现类就是SqlSessionTemplate。SqlSessionTemplate是线程安全的。SqlSessionTemplate通过动态代理的方式来保证DefaultSqlSession操作的线程安全性。  
3. 接口的扫描注册  
&emsp; MapperScannerConfigurer 实现了BeanDefinitionRegistryPostProcessor接口， BeanDefinitionRegistryPostProcessor 是 BeanFactoryPostProcessor的子类。  
4. 接口注入使用  
&emsp; MapperFactoryBean，因为实现了 FactoryBean 接口，同样是调用getObject()方法。  


#### 1.2.5.6. MyBatis缓存
&emsp; ......  

#### 1.2.5.7. MyBatis插件解析
1. **<font color="clime">Mybaits插件的实现主要用了拦截器、责任链和动态代理。</font>** `动态代理可以对SQL语句执行过程中的某一点进行拦截`，`当配置多个插件时，责任链模式可以进行多次拦截`。  
2. **<font color = "clime">mybatis扩展性很强，基于插件机制，基本上可以控制SQL执行的各个阶段，如执行器阶段，参数处理阶段，语法构建阶段，结果集处理阶段，具体可以根据项目业务来实现对应业务逻辑。</font>**   
    * 执行器Executor（update、query、commit、rollback等方法）；  
    * 参数处理器ParameterHandler（getParameterObject、setParameters方法）；  
    * 结果集处理器ResultSetHandler（handleResultSets、handleOutputParameters等方法）；  
    * SQL语法构建器StatementHandler（prepare、parameterize、batch、update、query等方法）；    

