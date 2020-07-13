---
title: FactoryBean
date: 2020-04-08 00:00:00
tags:
    - Spring
---

<!-- TOC -->

- [1. FactoryBean](#1-factorybean)
    - [1.1. FactoryBean的作用](#11-factorybean的作用)
    - [1.2. FactoryBean源码](#12-factorybean源码)
    - [1.3. Spring中有关FactoryBean的源码](#13-spring中有关factorybean的源码)

<!-- /TOC -->

# 1. FactoryBean  
## 1.1. FactoryBean的作用   

&emsp; ***<font color = "red">FactoryBean接口生产一些工厂bean，如Spring自身提供的ProxyFactoryBean、JndiObjectFactoryBean，还有Mybatis中的SqlSessionFactory。这些Bean实例过程比较复杂。</font>***  
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
