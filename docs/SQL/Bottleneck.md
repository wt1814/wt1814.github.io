


<!-- TOC -->

- [1. MySql性能及瓶颈](#1-mysql性能及瓶颈)
    - [1.1. MySql性能(最大数据量、最大并发数、查询耗时)](#11-mysql性能最大数据量最大并发数查询耗时)
    - [1.2. 数据库瓶颈](#12-数据库瓶颈)

<!-- /TOC -->

# 1. MySql性能及瓶颈  
## 1.1. MySql性能(最大数据量、最大并发数、查询耗时)

* 最大并发数：  
&emsp; 并发数是指同一时刻数据库能处理多少个请求，由max_connections和max_user_connections决定。max_connections是指MySQL实例的最大连接数，上限值是16384，max_user_connections是指每个数据库用户的最大连接数。  
&emsp; **MySQL会为每个连接提供缓冲区，意味着消耗更多的内存。** 如果连接数设置太高，硬件吃不消，太低又不能充分利用硬件。一般要求两者比值超过10%，计算方法如下：  
        
        max_used_connections / max_connections * 100% = 3/100 *100% ≈ 3%
 
    &emsp; 查看最大连接数与响应最大连接数：  

    ```sql
    show variables like '%max_connections%';
    show variables like '%max_user_connections%';
    ```

    &emsp; 在配置文件my.cnf中修改最大连接数  

        [mysqld]
        max_connections = 100
        max_used_connections = 20

* 查询耗时0.5秒：  
&emsp; 建议将单次查询耗时控制在0.5秒以内，0.5秒是个经验值，源于用户体验的3秒原则。如果用户的操作3秒内没有响应，将会厌烦甚至退出。响应时间=客户端UI渲染耗时+网络请求耗时+应用程序处理耗时+查询数据库耗时，0.5秒就是留给数据库1/6的处理时间。 

* 最大数据量：  
&emsp; MySQL没有限制单表最大记录数，它取决于操作系统对文件大小的限制。  
![image](http://182.92.69.8:8081/img/SQL/sql-28.png)  
&emsp; <font color = "red">《阿里巴巴Java开发手册》提出单表行数超过500万行或者单表容量超过2GB，才推荐分库分表。</font>500万仅供参考，并非铁律。  


## 1.2. 数据库瓶颈
<!-- 
CTO：这四种情况下，才是考虑分库分表的时候！ 
https://mp.weixin.qq.com/s/aR53IsVYLKmx06_2zrG_Ig
-->
&emsp; **数据库瓶颈：** <font color = "clime">`不管是IO瓶颈，还是CPU瓶颈，最终都会导致数据库的活跃连接数增加，进而逼近甚至达到数据库可承载活跃连接数的阈值。在业务Service来看就是，可用数据库连接少甚至无连接可用。`</font>  
1. IO瓶颈：  
&emsp; 第一种：磁盘读IO瓶颈，热点数据太多，数据库缓存放不下，每次查询时会产生大量的IO，降低查询速度。---> 分库和垂直分表。  
&emsp; 第二种：网络IO瓶颈，请求的数据太多（MySql一般并发数200～500），网络带宽不够。---> 分库。  
2. CPU瓶颈：  
&emsp; 第一种：SQL问题，如SQL中包含join，group by，order by，非索引字段条件查询等，增加CPU运算的操作。 ---> SQL优化，建立合适的索引，在业务Service层进行业务计算。  
&emsp; 第二种：单表数据量太大（达到1000W或100G以后），查询时扫描的行太多，SQL效率低，CPU率先出现瓶颈。---> 水平分表。  
