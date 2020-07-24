
<!-- TOC -->

- [1. BeanFactoryPostProcessor和BeanPostProcessor](#1-beanfactorypostprocessor和beanpostprocessor)
    - [1.1. BeanFactoryPostProcessor接口](#11-beanfactorypostprocessor接口)
    - [1.2. BeanPostProcessor接口](#12-beanpostprocessor接口)
        - [1.2.1. InstantiationAwareBeanPostProcessor](#121-instantiationawarebeanpostprocessor)
    - [1.3. BeanFactoryPostProcessor和BeanPostProcessor的示例](#13-beanfactorypostprocessor和beanpostprocessor的示例)

<!-- /TOC -->

# 1. BeanFactoryPostProcessor和BeanPostProcessor  
## 1.1. BeanFactoryPostProcessor接口  
&emsp; 该接口的定义如下：  

```java
public interface BeanFactoryPostProcessor {

    //可以通过beanFactory获取bean定义信息
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
```
&emsp; <font color = "red">实现BeanFactoryPostProcessor接口，可以在spring的bean创建之前，修改bean的定义属性。</font>也就是说，Spring允许BeanFactoryPostProcessor在容器实例化任何其它bean之前读取配置元数据，并可以根据需要进行修改，例如可以把bean的scope从singleton改为prototype，也可以把property的值给修改掉。可以同时配置多个BeanFactoryPostProcessor，并通过设置'order'属性来控制各个BeanFactoryPostProcessor的执行次序。  
&emsp; 注意：BeanFactoryPostProcessor是在spring容器加载了bean的定义文件之后，在bean实例化之前执行的。接口方法的入参是ConfigurrableListableBeanFactory，使用该参数，可以获取到相关bean的定义信息。  

&emsp; <font color = "red">Spring中有内置的一些BeanFactoryPostProcessor实现类</font>，常用的有：  
* org.springframework.beans.factory.config.PropertyPlaceholderConfigurer  
* org.springframework.beans.factory.config.PropertyOverrideConfigurer  
* org.springframework.beans.factory.config.CustomEditorConfigurer：用来注册自定义的属性编辑器  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Spring/spring-6.png)  

## 1.2. BeanPostProcessor接口  
&emsp; 该接口的定义如下：  

```java
public interface BeanPostProcessor {

    //bean初始化之前调用
    @Nullable
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    //bean初始化之后调用
    @Nullable
    default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
```
&emsp; <font color = "red">BeanPostProcessor，可以在spring容器实例化bean之后，在执行bean的初始化方法前后，添加一些自己的处理逻辑。</font>这里说的初始化方法，指的是下面两种：  

* bean实现了InitializingBean接口，对应的方法为afterPropertiesSet
* 在bean定义的时候，通过init-method设置的方法


        实例化和初始化的区别
        1. 实例化----实例化的过程是一个创建Bean的过程，即调用Bean的构造函数，单例的Bean放入单例池中。
        2. 初始化----初始化的过程是一个赋值的过程，即调用Bean的setter，设置Bean的属性。
        BeanPostProcessor作用于过程(1)前后

&emsp; Spring中Bean的实例化过程图示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Spring/spring-7.png)  

&emsp; 注意：BeanPostProcessor是在spring容器加载了bean的定义文件并且实例化bean之后执行的。BeanPostProcessor的执行顺序是在BeanFactoryPostProcessor之后。  

&emsp; **BeanFactory和ApplicationContext加载BeanPostProcessor：**  
&emsp; BeanFactory和ApplicationContext对待bean后置处理器稍有不同。ApplicationContext会自动检测在配置文件中实现了BeanPostProcessor接口的所有bean，并把它们注册为后置处理器，然后在容器创建bean的适当时候调用它，因此部署一个后置处理器同部署其他的bean并没有什么区别。而使用BeanFactory实现的时候，bean 后置处理器必须通过代码显式地去注册，在IoC容器继承体系中的ConfigurableBeanFactory接口中定义了注册方法  

    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

&emsp; 另外，不要将BeanPostProcessor标记为延迟初始化。因为如果这样做，Spring容器将不会注册它们，自定义逻辑也就无法得到应用。假如在<beans/\>元素的定义中使用了'default-lazy-init'属性，请确信各个BeanPostProcessor标记为'lazy-init="false"'。  

&emsp; **<font color = "red">Spring中有内置的一些BeanPostProcessor实现类</font>**，例如：  

* org.springframework.context.annotation.CommonAnnotationBeanPostProcessor：支持@Resource注解的注入  
* org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor：支持@Required注解的注入  
* org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor：支持@Autowired注解的注入  
* org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor：支持@PersistenceUnit和@PersistenceContext注解的注入  
* org.springframework.context.support.ApplicationContextAwareProcessor：用来为bean注入ApplicationContext等容器对象  

&emsp; 这些注解类的BeanPostProcessor，在spring配置文件中，可以通过这样的配置 <context:component-scan base-package="*.*" /\> ，自动进行注册。（spring通过ComponentScanBeanDefinitionParser类来解析该标签）  

&emsp; 如果自定义了多个的BeanPostProcessor的实现类，通过实现Ordered接口，设置order属性，可以按照顺序执行实现类的方法。  

### 1.2.1. InstantiationAwareBeanPostProcessor  
&emsp; InstantiationAwareBeanPostProcessor是BeanPostProcessor的子接口，可以在Bean生命周期的另外两个时期提供扩展的回调接口，即实例化Bean之前（调用postProcessBeforeInstantiation方法）和实例化Bean之后（调用postProcessAfterInstantiation方法）。  

## 1.3. BeanFactoryPostProcessor和BeanPostProcessor的示例  
&emsp; 1. bean的定义

```java
public class CustomBean implements InitializingBean {
    private String desc;
    private String remark;

    public CustomBean() {
        System.out.println("第二步：执行CustomBean类的无参构造函数");
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        System.out.println("第三步：调用setDesc方法");
        this.desc = desc;
    }
    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        System.out.println("第四步：调用setRemark方法");
        this.remark = remark;
    }
    public void afterPropertiesSet() throws Exception {
        System.out.println("第六步：调用afterPropertiesSet方法");
        this.desc = "在初始化方法中修改之后的描述信息";
    }
    public void initMethod() {
        System.out.println("第七步：调用initMethod方法");
    }
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[描述：").append(desc);
        builder.append("， 备注：").append(remark).append("]");
        return builder.toString();
    }
}
```
&emsp; 2. 定义BeanFactoryPostProcessor  

```java
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("第一步：调用MyBeanFactoryPostProcessor的postProcessBeanFactory");
        BeanDefinition bd = beanFactory.getBeanDefinition("customBean");
        MutablePropertyValues pv =  bd.getPropertyValues();
        if (pv.contains("remark")) {
            pv.addPropertyValue("remark", "在BeanFactoryPostProcessor中修改之后的备忘信息");
        }
    }

}
```
&emsp; 3. 定义BeanPostProcessor  

```java
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("第五步：BeanPostProcessor，对象" + beanName + "调用初始化方法之前的数据： " + bean.toString());
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("第八步：BeanPostProcessor，对象" + beanName + "调用初始化方法之后的数据：" + bean.toString());
        return bean;
    }
}
```
&emsp; 4. 定义测试类  

```java
public class PostProcessorTest {
        public static void main(String[] args) {
            ApplicationContext context = new ClassPathXmlApplicationContext("spring/postprocessor.xml");
            CustomBean bean = (CustomBean) context.getBean("customBean");
            System.out.println("################ 实例化、初始化bean完成");
            System.out.println("***********下面输出结果");
            System.out.println("描述：" + bean.getDesc());
            System.out.println("备注：" + bean.getRemark());

        }
}
```

&emsp; 运行结果如下：  

    第一步：调用MyBeanFactoryPostProcessor的postProcessBeanFactory
    第二步：执行CustomBean类的无参构造函数
    第三步：调用setDesc方法
    第四步：调用setRemark方法
    第五步：BeanPostProcessor，对象customBean调用初始化方法之前的数据： [描述：原始的描述信息， 备注：在BeanFactoryPostProcessor中修改之后的备忘信息]
    第六步：调用afterPropertiesSet方法
    第七步：调用initMethod方法
    第八步：BeanPostProcessor，对象customBean调用初始化方法之后的数据：[描述：在初始化方法中修改之后的描述信息， 备注：在BeanFactoryPostProcessor中修改之后的备忘信息]
    ################ 实例化、初始化bean完成
    ***********下面输出结果
    描述：在初始化方法中修改之后的描述信息
    备注：在BeanFactoryPostProcessor中修改之后的备忘信息

*************************************************
&emsp; 分析以上结果：在bean实例化之前，首先执行BeanFactoryPostProcessor实现类的方法，然后通过调用bean的无参构造函数实例化bean，并调用set方法注入属性值。bean实例化后，执行初始化操作，调用两个初始化方法（两个初始化方法的顺序：先执行afterPropertiesSet，再执行init-method）前后，执行了BeanPostProcessor实现类的两个方法。  