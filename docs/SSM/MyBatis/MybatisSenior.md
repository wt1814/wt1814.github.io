

<!-- TOC -->

- [1. MyBatis高级使用](#1-mybatis高级使用)
    - [1.1. 树状查询](#11-树状查询)
    - [1.2. insertOrUpdate](#12-insertorupdate)
    - [1.3. Mybatis中if-test判断大坑](#13-mybatis中if-test判断大坑)
    - [1.4. mybatis大数据](#14-mybatis大数据)
        - [1.4.1. mybatis大数据查询](#141-mybatis大数据查询)
        - [1.4.2. 批量插入大数据](#142-批量插入大数据)
    - [1.5. 整合MyBatis](#15-整合mybatis)
        - [1.5.1. Spring整合MyBatis](#151-spring整合mybatis)
        - [1.5.2. SpringBoot整合MyBatis](#152-springboot整合mybatis)

<!-- /TOC -->

# 1. MyBatis高级使用
<!-- 
MySQL 千万数据量深分页优化, 拒绝线上故障！ 
https://mp.weixin.qq.com/s/i3wLeCSxqWKrTwgtfelumQ
-->

## 1.1. 树状查询
<!-- 
https://www.cnblogs.com/lgjava/p/13821653.html
-->



## 1.2. insertOrUpdate
&emsp; .......
<!-- 
存在则更新 ON DUPLICATE KEY UPDATE
https://blog.csdn.net/f327888576/article/details/89490442

-->


## 1.3. Mybatis中if-test判断大坑  
<!-- 
mybatis 中 if-test 判断大坑
https://www.cnblogs.com/grasp/p/11268049.html
-->
&emsp; \<if test="takeWay == '0'"> mybatis的if判断：  
&emsp; 单个的字符要写到双引号里面才行，改为\<if test='takeWay == "1"'>或者改为\<if test="takeWay == '1'.toString() ">  



## 1.4. mybatis大数据  

### 1.4.1. mybatis大数据查询 
&emsp; [MyBatis大数据查询](/docs/SSM/MyBatis/BigData.md)  


### 1.4.2. 批量插入大数据
<!-- 
【368期】阿里巴巴为什么禁止MyBatis批量插入几千条数据使用foreach？
https://mp.weixin.qq.com/s/BW7YE8OPVe3IS03EOCm_fA
-->



## 1.5. 整合MyBatis  
### 1.5.1. Spring整合MyBatis  

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

### 1.5.2. SpringBoot整合MyBatis  
1. 引入jar包
2. 使用硬编码的方式配置bean。比如SqlSessionFactory，SqlSessionTemplate, PlatformTransactionManager。
3. 扫描接口包。
