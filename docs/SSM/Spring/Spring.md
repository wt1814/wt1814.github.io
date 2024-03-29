

<!-- TOC -->

- [1. Spring](#1-spring)
    - [1.1. Spring模块](#11-spring模块)
    - [1.2. Spring Core](#12-spring-core)
        - [1.2.1. Spring IOC](#121-spring-ioc)
        - [1.2.2. Spring DI](#122-spring-di)
            - [1.2.2.1. 依赖注入与自动装配](#1221-依赖注入与自动装配)
            - [1.2.2.2. SpringBean的作用域](#1222-springbean的作用域)
            - [1.2.2.3. Spring中的单例bean的线程安全问题](#1223-spring中的单例bean的线程安全问题)
                - [1.2.2.3.1. 单例Bean，非线程安全](#12231-单例bean非线程安全)
                - [1.2.2.3.2. 无状态Bean，线程安全](#12232-无状态bean线程安全)

<!-- /TOC -->

<!-- 
【死磕 Spring】
https://mp.weixin.qq.com/mp/homepage?__biz=MzI5NTYwNDQxNA==&hid=5&sn=0966e849b09b5ed6a14151bd9936ad4c&scene=1&devicetype=android-29&version=2700163b&lang=zh_CN&nettype=cmnet&ascene=7&session_us=gh_3ec9de1135f5&wx_header=1

https://mp.weixin.qq.com/s?__biz=MzI5NTYwNDQxNA==&mid=2247484705&idx=1&sn=64cac4a15524b6698e9152f151b6beb6&scene=19#wechat_redirect

@Autowire和@Resource注解使用的正确姿势，别再用错的了！！ 
https://mp.weixin.qq.com/s/34-DdoNcpMUlZiin6Js0Xg

将Bean注入Spring容器的五种方式 
https://mp.weixin.qq.com/s/r7j_bjDrOjaYXB6cPQD8Yw

-->
&emsp; **<font color = "red">总结：</font>**  
1. **@Autowired和@Resource之间的区别：**  
    1. @Autowired默认是按照类型装配注入的，默认情况下它要求依赖对象必须存在(可以设置它的required属性为false)。
    2. @Resource默认是按照名称来装配注入的，只有当找不到与名称匹配的bean才会按照类型来装配注入。  

# 1. Spring
<!-- 
Spring 有哪几种依赖注入方式？官方是怎么建议使用的呢？ 
https://mp.weixin.qq.com/s/vY1gZhp_g78IoXJ5QQvlFQ
-->


## 1.1. Spring模块  
![image](http://182.92.69.8:8081/img/SSM/Spring/spring-1.png)  

&emsp; **Spring的核心模块：**
1. Spring Core【核心容器】：核心容器提供Spring框架的基本功能，管理着Spring应用中bean的创建、配置和管理。核心容器的主要组件是BeanFactory，它是工厂模式的实现。BeanFactory使用控制反转(IOC)模式将应用程序的配置和依赖性规范与实际的应用程序代码分开。  
2. Spring Context【应用上下文】：Spring上下文是一个配置文件，向Spring框架提供上下文信息。提供了一种框架式的对象访问方法，有些像JNDI注册器。Context封装包的特性得自于Beans封装包，并添加了对国际化(I18N)的支持(例如资源绑定)，事件传播，资源装载的方式和Context的透明创建，比如说通过Servlet容器。Spring上下文和Bean工厂都是bean容器的实现。  
3. Spring AOP【面向切面】：通过配置管理特性，Spring AOP模块直接将面向方面的编程功能集成到了Spring框架中。所以，可以很容易地使Spring框架管理的任何对象支持AOP。Spring AOP模块为基于Spring的应用程序中的对象提供了事务管理服务。  
4. Spring ORM【对象实体映射】：Spring框架插入了若干个ORM框架，从而提供了ORM的对象关系工具，其中包括JDO、Hibernate和iBatis SQL Map。所有这些都遵从Spring的通用事务和DAO异常层次结构。  
5. Spring DAO【持久层模块】：Spring进一步简化DAO开发步骤，能以一致的方式使用数据库访问技术，用统一的方式调用事务管理，避免具体的实现侵入业务逻辑层的代码中。  
6. Spring Web【Web模块】：提供了基础的针对Web开发的集成特性。Web上下文模块建立在应用程序上下文模块之上，为基于Web的应用程序提供了上下文。  
7. Spring MVC【MVC模块】：提供了Web应用的MVC实现。Spring的MVC框架并不是仅仅提供一种传统的实现，它提供了一种清晰的分离模型。  

## 1.2. Spring Core  
&emsp; Spring是轻量级框架，是因为创建对象交给了Spring容器，启动Web容器时，对象已经创建完成。避免程序运行时创建对象，消耗资源。  

&emsp; Spring基本原理：控制反转和依赖注入。Spring反向控制应用程序所需要使用的外部资源。应用程序执行需要使用外部资源。由于资源已经完全由Spring进行统一管理，因此应用程序需要依赖Spring为其提供资源，提供资源的过程称为注入。  
![image](http://182.92.69.8:8081/img/SSM/Spring/spring-2.png)  

&emsp; Spring启动时读取应用程序提供的Bean配置信息，并在Spring容器中生成一份相应的Bean配置注册表，然后根据这张注册表实例化Bean，装配好Bean之间的依赖关系，为上层应用提供准备就绪的运行环境。其中Bean缓存池为HashMap实现。  

&emsp; 依赖注入和控制反转是同一概念吗？  
&emsp; 依赖注入和控制反转是对同一件事情的不同描述，从某个方面讲，就是它们描述的角度不同。依赖注入是从应用程序的角度在描述，可以把依赖注入描述完整点：应用程序依赖容器创建并注入它所需要的外部资源；而控制反转是从容器的角度在描述，描述完整点：容器控制应用程序，由容器反向的向应用程序注入应用程序所需要的外部资源。  
&emsp; 控制反转(Spring配置文件中注册Bean)，解决对象创建的问题，对象创建交给别人。依赖注入(Java代码中注入Bean)，在创建完对象后，对象关系的处理就是依赖注入。  

### 1.2.1. Spring IOC  
&emsp; Spring IOC配置方式有三种：基于XML的配置、基于注解的配置、基于Java的配置。  

### 1.2.2. Spring DI
<!-- 
详解依赖注入与自动装配
https://www.cnblogs.com/zhuwoyao88/p/6596295.html
Spring中的三种依赖注入和三种Bean装配方式
https://blog.csdn.net/q1937915896/article/details/88178558?utm_medium=distribute.pc_aggpage_search_result.none-task-blog-2~all~first_rank_v2~rank_v25-1-88178558.nonecase
-->

#### 1.2.2.1. 依赖注入与自动装配  
<!-- 
面试官常问的Spring依赖注入和Bean的装配问题，今天给大家讲清楚！
https://mp.weixin.qq.com/s/4Pl88gZkDZv636CuviUmYQ
-->
&emsp; 依赖注入的本质就是装配，装配是依赖注入的具体行为。这就是两者的关系。  
&emsp; 依赖注入相对自动装配比较繁琐。并且要找到对应类型的bean才能装配。Spring DI有三种注入方式：构造器注入、setter方法注入、根据注解注入。  
<!-- 
https://blog.csdn.net/q1937915896/article/details/88178558?utm_medium=distribute.pc_aggpage_search_result.none-task-blog-2~all~first_rank_v2~rank_v25-1-88178558.nonecase
-->
&emsp; 《Spring实战》中给装配下了一个定义，创建应用对象之间协作关系的行为称为装配。也就是说当一个对象的属性是另一个对象时，实例化时，需要为这个对象属性进行实例化。这就是装配。如果一个对象只通过接口来表明依赖关系，那么这种依赖就能够在对象本身毫不知情的情况下，用不同的具体实现进行切换。但是这样会存在一个问题，在传统的依赖注入配置中，必须要明确要给属性装配哪一个bean的引用，一旦bean很多，就不好维护了。基于这样的场景，spring使用注解来进行自动装配，解决这个问题。自动装配就是开发人员不必知道具体要装配哪个bean的引用，这个识别的工作会由spring来完成。与自动装配配合的还有“自动检测”，这个动作会自动识别哪些类需要被配置成bean，进而来进行装配。这样我们就明白了，自动装配是为了将依赖注入“自动化”的一个简化配置的操作。  
<!--
&emsp; **<font color = "red">自动装配：</font>** 在Spring中，对象无需自己查找或创建与其关联的其他对象，由容器负责把需要相互协作的对象引用赋予各个对象，使用autowire来配置自动装载模式。  
-->
&emsp; 在Spring框架xml配置中共有5种自动装配：
1. no：不进行自动装配的，通过手工设置ref属性来进行装配bean。  
2. byName：通过bean的名称进行自动装配，如果一个bean的 property 与另一bean 的name 相同，就进行自动装配。   
3. byType：通过参数的数据类型进行自动装配。  
4. constructor：利用构造函数进行装配，并且构造函数的参数通过byType进行装配。  
5. autodetect：自动探测，如果有构造方法，通过 construct的方式自动装配，否则使用byType的方式自动装配。  

&emsp; @Autowired默认是按照类型装配注入的，默认情况下它要求依赖对象必须存在(可以设置它required属性为false)。@Autowired 注解提供了更细粒度的控制，包括在何处以及如何完成自动装配。它的用法和@Required一样，修饰setter方法、构造器、属性或者具有任意名称和/或多个参数的PN方法。  

&emsp; **@Autowired和@Resource之间的区别：**  
1. @Autowired默认是按照类型装配注入的，默认情况下它要求依赖对象必须存在(可以设置它的required属性为false)。
2. @Resource默认是按照名称来装配注入的，只有当找不到与名称匹配的bean才会按照类型来装配注入。  

&emsp; **@Qualifier 注解有什么作用？**  
&emsp; **<font color = "red">当创建多个相同类型的bean并希望仅使用属性装配其中一个bean时，可以使用@Qualifier注解和@Autowired通过指定应该装配哪个确切的bean来消除歧义。</font>**    

#### 1.2.2.2. SpringBean的作用域  
&emsp; <font color = "red">Spring容器中的bean可以分为5个范围：</font>  
* singleton：单例模式，Spring IoC 容器中只会存在一个共享的Bean实例，无论有多少个Bean引用它，始终指向同一对象。该模式在多线程下是不安全的。Singleton作用域是Spring中的缺省作用域。  
* prototype：原型模式，每次通过Spring容器获取prototype定义的bean时，容器都将创建一个新的Bean实例，每个Bean实例都有自己的属性和状态，而 singleton全局只有一个对象。根据经验，对有状态的bean使用prototype作用域，而对无状态的bean使用singleton作用域。  

        无状态的对象即是自身没有状态的对象，自然也就不会因为多个线程的交替调度而破坏自身状态导致线程安全问题。无状态对象包括经常使用的DO、DTO、VO这些只作为数据的实体模型的贫血对象，还有Service、DAO和Controller，这些对象并没有自己的状态，它们只是用来执行某些操作的。例如，每个DAO提供的函数都只是对数据库的CRUD，而且每个数据库Connection都作为函数的局部变量(局部变量是在用户栈中的，而且用户栈本身就是线程私有的内存区域，所以不存在线程安全问题)，用完即关(或交还给连接池)。

* request：每一次HTTP请求都会产生一个新的bean。在一次 Http请求中，容器会返回该Bean的同一实例。而对不同的Http请求则会产生新的 Bean，而且该bean仅在当前Http Request内有效，当前Http 请求结束，该 bean实例也将会被销毁。  
* session：在一次Http Session中，容器会返回该Bean的同一实例。而对不同的Session请求则会创建新的实例，该bean实例仅在当前Session内有效。同Http请求相同，每一次session请求创建新的实例，而不同的实例之间不共享属性，且实例仅在自己的session请求内有效，请求结束，则实例将被销毁。  
* global-session：全局session作用域，仅仅在基于portlet的web应用中才有意义，Spring5已经没有了。Portlet是能够生成语义代码(例如：HTML)片段的小型Java Web插件。它们基于portlet容器，可以像servlet一样处理HTTP请求。但是，与 servlet 不同，每个portlet 都有不同的会话。  

#### 1.2.2.3. Spring中的单例bean的线程安全问题  
<!-- 
Spring 中的bean 是线程安全的吗？ 
https://mp.weixin.qq.com/s/P2GagWGNoMLxA5AuG_I7AA


-->



##### 1.2.2.3.1. 单例Bean，非线程安全
&emsp; **单例bean的优势：**  
1. 减少了新生成实例的消耗：新生成实例消耗包括两方面，第一，spring会通过反射或者cglib来生成bean实例，这都是耗性能的操作，其次给对象分配内存也会涉及复杂算法。  
2. 减少jvm垃圾回收：由于不会给每个请求都新生成bean实例，所以自然回收的对象少了。  
3. 可以快速获取到bean：因为单例的获取bean操作除了第一次生成之外其余的都是从缓存里获取的所以很快。  

&emsp; **单例bean的劣势(线程安全问题)：**  
&emsp; 单例bean存在线程问题，主要是因为当多个线程操作同一个对象的时候，对这个对象的非静态成员变量的写操作会存在线程安全问题。即bean如果是有状态的，可能在并发场景下出现问题。而原型的bean则不会有这样问题(但也有例外，比如它被单例bean依赖)，因为给每个请求都新创建实例。  

&emsp; **解决办法：**  
1. 在Bean对象中尽量避免定义可变的成员变量。
2. <font color = "red">如果必须在bean中声明任何有状态的实例变量或类变量，那么就使用ThreadLocal把变量变为线程私有的，如果bean的实例变量或类变量需要在多个线程之间共享，那么使用synchronized、lock、CAS等实现线程同步的方法。</font>
3. <font color = "red">Spring和mybatis整合，通过代理保证sessionTemplate的安全。</font>  

##### 1.2.2.3.2. 无状态Bean，线程安全
<!--
什么是无状态Bean、有状态Bean？之前一直很含糊。    
今天日常工作中，脑子就突然灵光一现，是不是service、dao这些一般都是无状态Bean；那如果是一个List呢？ 
-->
 
