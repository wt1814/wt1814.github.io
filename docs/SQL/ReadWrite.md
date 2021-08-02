


# 读写分离实现

## 应用层实现
&emsp; 方案就是根据不同的sql语句来选择不同的数据源来操作，优点是对性能的损耗比较小，缺点是强依赖程序员。  

&emsp; 应用层解决方案：  
1. 驱动实现
    * com.mysql.jdbc.ReplicationDriver
    * Sharding-jdbc
2. MyBatis plugin(sqlType: select,update,insert)  
3. SpringAOP + mybatis plugin + 注解
4. Spring动态数据源 + mybatis plugin


## 代理中间件实现
&emsp; 常见代理中间件有MyCat...  

## 方案选择
&emsp; 代码侵入程度、性能影响...
