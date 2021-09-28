


# Spring事务失效

1. 使用在了非public方法上。  
2. 捕获了异常，未再抛出。  
3. <font color = "red">同一个类中方法调用。</font>  
4. @Transactional的类注入失败。  
5. 多数据源(静态配置)  
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
6. 原始SSM项目，重复扫描导致事务失效  
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
7. 底层数据库引擎不支持事务。  
&emsp; Mysql引擎。MyISAM不支持事务；InnoDB支持事务。检查表的属性：  

        SHOW TABLE STATUS LIKE 'tbl_name';
        SHOW CREATE TABLE tbl_name; 
