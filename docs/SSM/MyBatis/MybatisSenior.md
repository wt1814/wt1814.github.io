

# MyBatis使用
<!-- 
MySQL 千万数据量深分页优化, 拒绝线上故障！ 
https://mp.weixin.qq.com/s/i3wLeCSxqWKrTwgtfelumQ
-->

## Mybatis 中 if-test 判断大坑  
<!-- 
mybatis 中 if-test 判断大坑
https://www.cnblogs.com/grasp/p/11268049.html
-->
&emsp; \<if test="takeWay == '0'"> mybatis的if判断  
&emsp; 单个的字符要写到双引号里面才行，改为\<if test='takeWay == "1"'>或者改为\<if test="takeWay == '1'.toString() ">  

## 流式查询  
<!--
 MyBatis读取大量数据（流式读取）
https://www.cnblogs.com/yifanSJ/p/12658536.html
 炸！使用 MyBatis 查询千万数据量？ 
 https://mp.weixin.qq.com/s/-gljMMrP0RcALfvigFXT1Q

JDBC三种读取方式：
1. 一次全部（默认）：一次获取全部。
2. 流式：多次获取，一次一行。
3. 游标：多次获取，一次多行

新技能 MyBatis 千万数据表，快速分页！ 
https://mp.weixin.qq.com/s/RFgPkpyCPQQOo0SKZHA9Eg
https://my.oschina.net/qalong/blog/3123826
https://mp.weixin.qq.com/s/eyYGrDqjrl3OwWLqptiZVA
-->
<!-- 
https://www.jianshu.com/p/0339c6fe8b61

MyBatis大数据量流式数据查询、数据导出
https://my.oschina.net/qalong/blog/3123826

mybatis大数据查询优化：fetchSize
https://www.jianshu.com/p/2ba501063556

-->
&emsp; 流式查询指的是查询成功后不是返回一个集合而是返回一个迭代器，应用每次从迭代器取一条查询结果。流式查询的好处是能够降低内存使用。  
&emsp; **<font color = "clime">如果没有流式查询，想要从数据库取 1000 万条记录而又没有足够的内存时，就不得不分页查询，而分页查询效率取决于表设计，如果设计的不好，就无法执行高效的分页查询。因此流式查询是一个数据库访问框架必须具备的功能。</font>**  
&emsp; 流式查询的过程当中，数据库连接是保持打开状态的，因此要注意的是：执行一个流式查询后，数据库访问框架就不负责关闭数据库连接了，需要应用在取完数据后自己关闭。  
