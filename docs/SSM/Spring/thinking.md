
<!-- 
Spring中涉及的设计模式总结 
https://mp.weixin.qq.com/s/ktNs4T_OZ-neWWvtBmC-cA
Spring 中经典的 9 种设计模式，打死也要记住！ 
https://mp.weixin.qq.com/s/VzBA7DehOwYUAl7xt8KPAw
-->

&emsp; 之所以要看Spring源码，是因为Spring占据了Java的半边天。(学了Spring源码，咱能装啊。)  

* 看Spring源码的过程中，可以看看这些顶尖攻城狮是怎么敲代码的。  
* Spring的运用更加熟练了。Spring的一些高级知识点也知道了部分。  
* 学了Spring源码，在服务器启动的时候，报有关Spring的错，最起码知道个大概。  
* Spring提供了一些扩展接口，可以满足部分需求，可以实现编码解耦。  

---
&emsp; Spring容器刷新：  
1. BeanFactory与ApplicationContext
2. BeanDefinition：BeanDefinition中保存了Bean信息，比如这个Bean指向的是哪个类、是否是单例的、是否懒加载、这个Bean依赖了哪些Bean等。  
3. Spring容器刷新：  
    **<font color = "blue">（⚠★★★`利用工厂和反射创建Bean。主要包含3部分：1).容器本身--创建容器、2).容器扩展--后置处理器、3).事件，子容器，实例化Bean。`）</font>**     
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

---
&emsp;  Spring提供的扩展接口有aware接口、后置处理器、InitializingBean和DisposableBean、事件机制。**这些可扩展的接口主要分两类，一类是针对单个Bean、另一类是针对容器。**  

* 针对单个Bean，BeanNameAware、BeanPostProcessor、InitializingBean和DisposableBean。
* 针对容器，ApplicationContextAware、BeanFactoryPostProcessor、Spring提供的5种标准事件机制。  

---
&emsp;  知道了Spring提供了可扩展的接口，也简单进行了分类，当然最主要的是要知道这些接口的作用。  

* Aware接口提供了Bean对容器的感知  
&emsp;  容器管理的Bean一般不需要了解容器的状态和直接使用容器，但在某些情况下，是需要在Bean中直接对IOC容器进行操作的，这时候，就需要在Bean中设定对容器的感知。Spring IOC容器也提供了该功能，它是通过特定的aware接口来完成的。   
* BeanPostProcessor  
&emsp;  BeanPostProcessor，可以在spring容器实例化bean之后，在执行bean的初始化方法前后，添加一些自己的处理逻辑。   
&emsp;  实现BeanFactoryPostProcessor接口，可以在spring的bean创建之前，修改bean的定义属性。
* InitializingBean和DisposableBean  
&emsp;  当需要在bean的全部属性设置成功后做些特殊的处理，可以让该bean实现InitializingBean接口。  
&emsp;  当需要在bean销毁之前做些特殊的处理，可以让该bean实现DisposableBean接口，该接口也只定义了一个destory方法。  

---
&emsp;  在最后，还是需要知道这些接口的加载顺序。在SpringBean生命周期里，doCreateBean() 方法主要干三件事情：  
1. 实例化bean对象：createBeanInstance()  
2. 属性注入：populateBean()  
3. 初始化bean对象：initializeBean()。而初始化bean对象时也是干了三件事情：  
    1. 激活Aware方法  
    2. 后置处理器的应用  
    3. 激活自定义的init方法
