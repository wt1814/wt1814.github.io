

<!-- TOC -->

- [1. FactoryBean](#1-factorybean)
    - [1.1. FactoryBean简介](#11-factorybean简介)
    - [1.2. FactoryBean源码](#12-factorybean源码)
    - [1.3. Spring中有关FactoryBean的源码](#13-spring中有关factorybean的源码)

<!-- /TOC -->

# 1. FactoryBean  
<!-- 
https://www.cnblogs.com/tiancai/p/9604040.html
-->

## 1.1. FactoryBean简介   
&emsp; BeanFactory是个Factory，也就是IOC容器或对象工厂；FactoryBean是个Bean，也由BeanFactory管理。  
&emsp; `一般情况下，Spring通过反射机制利用\<bean>的class属性指定实现类实例化Bean。` **<font color = "red">在某些情况下，实例化Bean过程比较复杂，</font>** 如果按照传统的方式，则需要在\<bean>中提供大量的配置信息。配置方式的灵活性是受限的，这时采用编码的方式可能会得到一个简单的方案。 **<font color = "red">Spring为此提供了一个org.springframework.bean.factory.FactoryBean的`工厂类接口`，用户可以通过实现该接口定制实例化Bean的逻辑。</font>**  
&emsp; **<font color = "red">FactoryBean接口的一些实现类，如Spring自身提供的ProxyFactoryBean、JndiObjectFactoryBean，还有Mybatis中的SqlSessionFactoryBean，</font>** 用于生产一些复杂的Bean。  
&emsp; SqlSessionFactory部分源码：  

```java
public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>, InitializingBean, ApplicationListener<ApplicationEvent> {
    private static final Log LOGGER = LogFactory.getLog(SqlSessionFactoryBean.class);
    //...
    public SqlSessionFactory getObject() throws Exception {
        if (this.sqlSessionFactory == null) {
            this.afterPropertiesSet();
        }

        return this.sqlSessionFactory;
    }
    //...
}
```
&emsp; FactoryBean和普通Bean的区别：在Spring容器中获取FactoryBean实现类时，容器不会返回FactoryBean本身，而是返回其（getObject()）生成的对象要想获取FactoryBean的实现类本身，得在getBean(String BeanName)中的BeanName之前加上&，写成getBean(String &BeanName)。   
&emsp; ~~FactoryBean是个接口，为IOC容器中Bean的实现提供了更加灵活的方式，FactoryBean在IOC容器的基础上给Bean的实现加上了一个简单工厂模式和装饰模式。FactoryBean是一个能生产或者修饰对象生成的工厂Bean(本质上也是一个bean)。~~    


## 1.2. FactoryBean源码  
&emsp; FactoryBean接口源码：  

```java
package org.springframework.beans.factory;

public interface FactoryBean<T> {
    T getObject() throws Exception;
    Class<?> getObjectType();
    boolean isSingleton();
}
```

&emsp; 在该接口中定义了以下3个方法：  

* getObject()：返回由FactoryBean创建的Bean实例，如果isSingleton()返回true，则该实例会放到Spring容器中单实例缓存池中；  
* Singleton()：返回由FactoryBean创建的Bean实例的作用域是singleton还是prototype；  
* getObjectType()：返回FactoryBean创建的Bean类型。  

&emsp; <font color = "red">如果一个IOC容器中的Bean实现了FacgoryBean接口，通过getBean(String BeanName)获取到的Bean对象并不是FactoryBean的实现类对象，而是这个实现类中的getObject()方法返回的对象。</font>如果要想获取FactoryBean的实现类，就要getBean(&BeanName)，在BeanName之前加上&。  

## 1.3. Spring中有关FactoryBean的源码  
&emsp; FactoryBean的生产特性是在getBean中起作用的。  

```java
bean = getObjectForBeanlnstance(sharedlnstance, name, beanName, mbd);  
```
