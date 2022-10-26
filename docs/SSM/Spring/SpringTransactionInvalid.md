
<!-- TOC -->

- [1. Spring事务问题](#1-spring事务问题)
    - [1.1. Spring事务失效](#11-spring事务失效)
    - [1.2. 大事务](#12-大事务)

<!-- /TOC -->

1. Spring事务失效：  
    * 使用在了非public方法上。
    * 捕获了异常，未再抛出。
    * 同一个类中方法调用。
    * @Transactional的类注入失败。
    * 多数据源（静态配置）
    * 原始SSM项目，重复扫描导致事务失效  
2. 大事务问题：将修改库的代码聚合在一起。   


# 1. Spring事务问题
## 1.1. Spring事务失效
<!-- 
从源码剖析Spring事务失效问题
https://blog.csdn.net/qq_38826019/article/details/117628192
spring声明式事务底层源码分析+spring事务失效场景总结
https://blog.csdn.net/aaa_bbb_ccc_123_456/article/details/103920130
https://mp.weixin.qq.com/s/kU_sCwnkZvnFpaFesgrrGA
https://mp.weixin.qq.com/s/32TDmCUuYS06lNOAkd60iw

内部方法调用，事务不起作用的原因及解决办法
https://juejin.im/post/5d4bea09f265da03db0764a7
【原创】004 | 搭上SpringBoot事务诡异事件分析专车 
https://mp.weixin.qq.com/s?__biz=MzAxMjEwMzQ5MA==&mid=2448888540&idx=2&sn=638239681c7c5d84ae5e16580ffa5f92&chksm=8fb548f1b8c2c1e7ad8fc94dfab856f951d0802af35f896f19a2801e48b0098a4e39ab2edde0&mpshare=1&scene=1&srcid=&sharer_sharetime=1573431932501&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=2a4ff15fdd8463461fc172688f13005ff9d07ff4077d440ef372d590e51e26178d9d1f28303eb5f89463cf9998da9d89c0bc8eee258bf0679e2ab741afa73f56898ca4ea23103b2418c1a5a64bf1141e&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=jJEy3kCpzSU46vQnPYwujJ%2FMZDu5tpN7sY32I3V5fxoKvSV4rqdrYUcoZ5Odz%2FWZ
一口气说出 6种 @Transactional 注解失效场景
https://mp.weixin.qq.com/s/IcDEEft7bLhnqyo5knwUdw
面试官：你知道哪几种事务失效的场景？ 
https://mp.weixin.qq.com/s/JjzNEq26vhUO04T88uthHA

-->
<!-- 

http://events.jianshu.io/p/263689699877
-->

1. <font color = "red">同一个类中方法调用。</font>  
&emsp; 因为spring声明式事务是基于AOP实现的，是使用动态代理来达到事务管理的目的，当前类调用的方法上面加@Transactional 这个是没有任何作用的，因为 **<font color = "clime">调用这个方法的是this，没有经过 Spring 的代理类。</font>**  
2. 方法不是public的。    
&emsp; @Transactional 只能用于 public 的方法上，否则事务不会失效，如果要用在非 public 方法上，可以开启 AspectJ 代理模式。  
3. 抛出的异常不支持回滚。捕获了异常，未再抛出。  
4. 多数据源(静态配置)  
&emsp; 使用SpringAop解决多数据源事务。事务和数据源绑定，如果不给事务管理器qualifer属性，@Transactional默认会与第一个事务管理器绑定。如果使用第二个数据源，导致Transactional失效。  
&emsp; 解决方案：
    1. 配置事务管理器时添加value区分字段。  
            
            <bean id="transactionManager02" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
                <property name="dataSource" ref="dataSource02" />
                <qualifier value="ynw"></qualifier>
            </bean>
    2. java代码中添加value  

            @Transactional(value = "ynw")
            ublic HashMap<String， Object> addAppointMent(Map map) {}
5. 原始SSM项目，重复扫描导致事务失效  
&emsp; SpringMVC中context:component-scan重复扫描会引起事务失效。  
&emsp; 在主容器中(applicationContext.xml)，将Controller的注解排除掉。  

    ```xml
    <context:component-scan base-package="com">
        <context:exclude-filter type="annotation" expression="org.springframework.stereotype.Controller" />
    </context:component-scan>
    ```

    &emsp; 而在springMVC配置文件中将Service注解给去掉。 

    ```xml
    <context:component-scan base-package="com">
        <context:include-filter type="annotation" expression="org.springframework.stereotype.Controller" />
        <context:exclude-filter type="annotation" expression="org.springframework.stereotype.Service" />
    </context:component-scan>
    ```

    &emsp; 使用springMVC，并且使用其扫描器组件，对项目中加入servcie /ctroller注解的bean进行注册交给srping容器管理，在springMVC配置文件中只扫描ctroller对所有的service进行过滤掉，因为按照spring配置文件的加载顺序来讲，先加载springmvc配置文件，再加载spring配置文件，事物一般在spring配置文件中进行配置，如果此时在加载srpingMVC配置文件的时候，把service也给注册了，但是此时事物还没加载，也就导致后面的事物无法成功注入到service中。所以把对service的扫描放在spring配置文件中或是其他配置文件中。  
6. 底层数据库引擎不支持事务。  
&emsp; Mysql引擎。MyISAM不支持事务；InnoDB支持事务。检查表的属性：  

        SHOW TABLE STATUS LIKE 'tbl_name';
        SHOW CREATE TABLE tbl_name; 



## 1.2. 大事务
<!-- 
https://mp.weixin.qq.com/s/nMAsyH8z7E6XFYiKJTy4Cw
-->
