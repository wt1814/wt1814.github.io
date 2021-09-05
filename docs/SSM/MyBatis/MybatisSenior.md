

<!-- TOC -->

- [1. MyBatis高级使用](#1-mybatis高级使用)
    - [1.1. insertOrUpdate](#11-insertorupdate)
    - [1.3. Mybatis中if-test判断大坑](#13-mybatis中if-test判断大坑)
    - [1.4. 整合MyBatis](#14-整合mybatis)
        - [1.4.1. Spring整合MyBatis](#141-spring整合mybatis)
        - [1.4.2. SpringBoot整合MyBatis](#142-springboot整合mybatis)

<!-- /TOC -->

# 1. MyBatis高级使用
<!-- 
MySQL 千万数据量深分页优化, 拒绝线上故障！ 
https://mp.weixin.qq.com/s/i3wLeCSxqWKrTwgtfelumQ
-->

## 1.1. insertOrUpdate
&emsp; .......

## 1.2. mybatis大数据查询 

## 1.3. Mybatis中if-test判断大坑  
<!-- 
mybatis 中 if-test 判断大坑
https://www.cnblogs.com/grasp/p/11268049.html
-->
&emsp; \<if test="takeWay == '0'"> mybatis的if判断：  
&emsp; 单个的字符要写到双引号里面才行，改为\<if test='takeWay == "1"'>或者改为\<if test="takeWay == '1'.toString() ">  


## 1.4. 整合MyBatis  
### 1.4.1. Spring整合MyBatis  

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

### 1.4.2. SpringBoot整合MyBatis  
1. 引入jar包
2. 使用硬编码的方式配置bean。比如SqlSessionFactory，SqlSessionTemplate, PlatformTransactionManager。
3. 扫描接口包。
