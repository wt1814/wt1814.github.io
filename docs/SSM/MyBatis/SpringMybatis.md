

<!-- TOC -->

- [1. 整合MyBatis](#1-整合mybatis)
    - [1.1. Spring整合MyBatis](#11-spring整合mybatis)
    - [1.2. SpringBoot整合MyBatis](#12-springboot整合mybatis)

<!-- /TOC -->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-33.png)  

# 1. 整合MyBatis  
## 1.1. Spring整合MyBatis  

&emsp; 添加配置文件  

```xml
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

## 1.2. SpringBoot整合MyBatis  
1. 引入jar包
2. 使用硬编码的方式配置bean。比如SqlSessionFactory，SqlSessionTemplate, PlatformTransactionManager。
3. 扫描接口包。

