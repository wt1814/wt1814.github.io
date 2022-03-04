

<!-- TOC -->

- [1. MyBatis高级使用](#1-mybatis高级使用)
    - [1.1. mybatis-generator](#11-mybatis-generator)
    - [1.2. PageHelper](#12-pagehelper)
    - [1.3. 树状查询](#13-树状查询)
    - [1.4. insertOrUpdate](#14-insertorupdate)
    - [1.5. Mybatis中if-test判断大坑](#15-mybatis中if-test判断大坑)
    - [1.6. mybatis大数据](#16-mybatis大数据)
        - [1.6.1. mybatis大数据查询](#161-mybatis大数据查询)
        - [1.6.2. 批量插入大数据](#162-批量插入大数据)
    - [1.7. 整合MyBatis](#17-整合mybatis)
        - [1.7.1. Spring整合MyBatis](#171-spring整合mybatis)
        - [1.7.2. SpringBoot整合MyBatis](#172-springboot整合mybatis)

<!-- /TOC -->

# 1. MyBatis高级使用
<!-- 
MySQL 千万数据量深分页优化, 拒绝线上故障！ 
https://mp.weixin.qq.com/s/i3wLeCSxqWKrTwgtfelumQ
-->


## 1.1. mybatis-generator  
&emsp; mybatis-generator，能够生成PO类，能生成mapper映射文件（其中包括基本的增删改查功能）、能生成mapper接口。  


## 1.2. PageHelper
<!-- 
SpringBoot集成MyBatis的分页插件PageHelper
https://www.cnblogs.com/leeego-123/articles/10832926.html
-->


## 1.3. 树状查询
<!-- 
https://www.cnblogs.com/lgjava/p/13821653.html
-->



## 1.4. insertOrUpdate
&emsp; .......
<!-- 
存在则更新 ON DUPLICATE KEY UPDATE
https://blog.csdn.net/f327888576/article/details/89490442

-->


## 1.5. Mybatis中if-test判断大坑  
<!-- 
mybatis 中 if-test 判断大坑
https://www.cnblogs.com/grasp/p/11268049.html
-->
&emsp; \<if test="takeWay == '0'"> mybatis的if判断：  
&emsp; 单个的字符要写到双引号里面才行，改为\<if test='takeWay == "1"'>或者改为\<if test="takeWay == '1'.toString() ">  



## 1.6. mybatis大数据  

### 1.6.1. mybatis大数据查询 
&emsp; [MyBatis大数据查询](/docs/SSM/MyBatis/BigData.md)  

### 1.6.2. 批量插入大数据
<!-- 
【368期】阿里巴巴为什么禁止MyBatis批量插入几千条数据使用foreach？
https://mp.weixin.qq.com/s/BW7YE8OPVe3IS03EOCm_fA
-->


## 1.7. 整合MyBatis  
### 1.7.1. Spring整合MyBatis  

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

### 1.7.2. SpringBoot整合MyBatis  
1. 引入jar包
2. 使用硬编码的方式配置bean。比如SqlSessionFactory，SqlSessionTemplate, PlatformTransactionManager。
3. 扫描接口包。
