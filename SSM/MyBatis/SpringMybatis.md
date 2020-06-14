---
title: Spring整合Mybatis
date: 2020-04-19 00:00:00
tags:
    - Mybatis
---



# 整合MyBatis  

## Spring整合MhyBatis  

&emsp; 添加配置文件  

```
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource" />
    <!-- 自动扫描entity目录, 省掉Configuration.xml里的手工配置 -->
    <property name="mapperLocations" value="classpath:com/test/mapping/*.xml" />
</bean>
<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
    <property name="basePackage" value="com.test.dao" />
    <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory" />
</bean>
```


## SpringBoot整合MyBatis  
1. 引入jar包
2. 使用硬编码的方式配置bean。比如SqlSessionFactory，SqlSessionTemplate, PlatformTransactionManager。
3. 扫描接口包。


# Spring整合MyBatis原理  
&emsp; Spring整合MyBatis并不会对MyBatis内部进行改造，只会进行集成，对其实现进行了包装。  
&emsp; MyBatis运行原理：  

    1. 创建SqlSessionFacory；
    2. 从SqlSessionFactory对象中获取 SqlSession对象；
    3. 获取Mapper；
    4. 执行操作；

## 创建SqlSessionFacory  
&emsp; MyBatis-Spring中创建SqlSessionFacory是由SqlSessionFactoryBean完成的。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-24.png)  

* InitializingBean接口：实现了这个接口，那么当bean初始化的时候，spring就会调用该接口的实现类的afterPropertiesSet方法，去实现当spring初始化该Bean 的时候所需要的逻辑。afterPropertiesSet()会在 bean 的属性值设置完的时候被调用。  
* FactoryBean接口：实现了该接口的类，在调用getBean进行实例化的时候会返回该工厂返回的实例对象。实际上调用的是getObject()方法，getObject() 方法里面调用的也是 afterPropertiesSet()方法。  
* ApplicationListener接口：实现了该接口，如果注册了该监听的话，那么就可以了监听到Spring的一些事件，然后做相应的处理。  






