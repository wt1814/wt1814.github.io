

<!-- TOC -->

- [1. MyBatis高级使用](#1-mybatis高级使用)
    - [1.1. 批量插入或更新](#11-批量插入或更新)
    - [1.2. foreach集合对象](#12-foreach集合对象)
    - [1.3. mybatis-generator](#13-mybatis-generator)
    - [1.4. PageHelper](#14-pagehelper)
    - [1.5. 树状查询](#15-树状查询)
    - [1.6. insertOrUpdate](#16-insertorupdate)
    - [1.7. Mybatis中if-test判断大坑](#17-mybatis中if-test判断大坑)
    - [1.8. mybatis大数据](#18-mybatis大数据)
        - [1.8.1. mybatis大数据查询](#181-mybatis大数据查询)
        - [1.8.2. 批量插入大数据](#182-批量插入大数据)
    - [1.9. 整合MyBatis](#19-整合mybatis)
        - [1.9.1. Spring整合MyBatis](#191-spring整合mybatis)
        - [1.9.2. SpringBoot整合MyBatis](#192-springboot整合mybatis)
    - [1.10. Intellij IDEA中Mybatis Mapper自动注入警告的解决方案](#110-intellij-idea中mybatis-mapper自动注入警告的解决方案)

<!-- /TOC -->

# 1. MyBatis高级使用
<!-- 
MySQL 千万数据量深分页优化, 拒绝线上故障！ 
https://mp.weixin.qq.com/s/i3wLeCSxqWKrTwgtfelumQ
-->


## 1.1. 批量插入或更新  
<!-- 
https://blog.csdn.net/qq_58772217/article/details/125281973
-->
1. ON DUPLICATE KEY UPDATE检查```主键或唯一索引字段```是否冲突。  
2. update的字段值与现存的字段值相同，则不更新。  
3. 动态更新字段值用VALUES(字段名称)。  

## 1.2. foreach集合对象  
<!-- 

https://blog.csdn.net/TaoShao521/article/details/108808982
-->


## 1.3. mybatis-generator  
&emsp; [mybatis-generator](/docs/SSM/MyBatis/MybatisGenerator.md) 

## 1.4. PageHelper
<!-- 
SpringBoot集成MyBatis的分页插件PageHelper
https://www.cnblogs.com/leeego-123/articles/10832926.html

https://pagehelper.github.io/docs/howtouse/
-->
&emsp; [PageHelper](/docs/SSM/MyBatis/PageHelper.md)  


## 1.5. 树状查询
<!-- 
https://www.cnblogs.com/lgjava/p/13821653.html

https://blog.csdn.net/qq_35558665/article/details/106310333
1.collection 的column与id的column相同，property为实体类中子集合的名字，select与查询方法名字相同
2.查询时一定要将id和parentId都查出来，否则mybaits无法完成递归，我用*查就更没问题了。实体类中也要有父id那个属性
3、以上实现至针对传入参数为一个值的时候，parentId传进去的值要是最顶级的，这里就是0

-->



## 1.6. insertOrUpdate
&emsp; .......
<!-- 
存在则更新 ON DUPLICATE KEY UPDATE
https://blog.csdn.net/f327888576/article/details/89490442

-->


## 1.7. Mybatis中if-test判断大坑  
<!-- 
mybatis 中 if-test 判断大坑
https://www.cnblogs.com/grasp/p/11268049.html
-->
&emsp; \<if test="takeWay == '0'"> mybatis的if判断：  
&emsp; 单个的字符要写到双引号里面才行，改为\<if test='takeWay == "1"'>或者改为\<if test="takeWay == '1'.toString() ">  



## 1.8. mybatis大数据  

### 1.8.1. mybatis大数据查询 
&emsp; [MyBatis大数据查询](/docs/SSM/MyBatis/BigData.md)  

### 1.8.2. 批量插入大数据
<!-- 
【368期】阿里巴巴为什么禁止MyBatis批量插入几千条数据使用foreach？
https://mp.weixin.qq.com/s/BW7YE8OPVe3IS03EOCm_fA

Mybatis批量插入数据的两种方式
https://blog.csdn.net/ylforever/article/details/126592028
-->

&emsp; 两种方式：  
1. foreach，
    ```java
    VALUES
        <foreach>
    ```
2. 使用Batch Insert技术  
    ```java
    BatchInsertMapper insertMapper = session.getMapper(BatchInsertMapper.class);
    ```


## 1.9. 整合MyBatis  
### 1.9.1. Spring整合MyBatis  

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

### 1.9.2. SpringBoot整合MyBatis  
1. 引入jar包
2. 使用硬编码的方式配置bean。比如SqlSessionFactory，SqlSessionTemplate, PlatformTransactionManager。
3. 扫描接口包。


## 1.10. Intellij IDEA中Mybatis Mapper自动注入警告的解决方案
<!-- 
Intellij IDEA中Mybatis Mapper自动注入警告的6种解决方案 
https://mp.weixin.qq.com/s?__biz=MzA4NjgxMjQ5Mg==&mid=2665762835&idx=1&sn=5794527649410ef35dc2382941345484&chksm=84d20230b3a58b2646ed333b3ae9ebf299d8d06da0c286302f74c86cfaeb3910b0b04805bbcb&mpshare=1&scene=1&srcid=&sharer_sharetime=1571876850857&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=6f23511bf9e1c01f24cbca42a703bf7244b1622f167e09344917306bbe5af0769908126efc68bd0fe3dc571d186dbc6c864e010fadc49798e3e7c63f72ced8c00fd3d27ff922fcd564c085580ad06213&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=tOysFrIpapzI%2FSWUdTcbYKvSoWjfuug2aUNd5keR9%2BIBFSeAaxr3gVVWD9yTgncJ

-->

