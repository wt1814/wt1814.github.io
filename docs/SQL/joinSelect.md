

<!-- TOC -->

- [1. 联合查询](#1-联合查询)
    - [1.1. 连接查询](#11-连接查询)
        - [1.1.1. 各种JOIN](#111-各种join)
            - [1.1.1.1. 常用的JOIN](#1111-常用的join)
            - [1.1.1.2. 延伸用法](#1112-延伸用法)
        - [1.1.2. ★★★自连接](#112-★★★自连接)
    - [1.2. 子查询](#12-子查询)
        - [1.2.1. 子查询的位置](#121-子查询的位置)
        - [1.2.2. 子查询的分类](#122-子查询的分类)
            - [1.2.2.1. 标量子查询](#1221-标量子查询)
            - [1.2.2.2. 列子查询](#1222-列子查询)
            - [1.2.2.3. 行子查询](#1223-行子查询)
        - [1.2.3. 关键字ALL SOME ANY](#123-关键字all-some-any)
        - [1.2.4. 关键字In与exists](#124-关键字in与exists)
            - [1.2.4.1. 关键字In](#1241-关键字in)
                - [1.2.4.1.1. 关键字in与关键字or与关键字between](#12411-关键字in与关键字or与关键字between)
                - [1.2.4.1.2. 关键字in超过1000个条件的处理](#12412-关键字in超过1000个条件的处理)
            - [1.2.4.2. 关键字Exists](#1242-关键字exists)
            - [1.2.4.3. In与Exists区别](#1243-in与exists区别)
        - [1.2.5. 子查询与连接查询的区别](#125-子查询与连接查询的区别)
    - [1.3. 组合查询，UNION、INTERSECT、EXCEPT](#13-组合查询unionintersectexcept)
        - [1.3.1. UNION运算符，并集](#131-union运算符并集)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. **关键字in：**  
&emsp; **<font color = "clime">in查询里面的数量最大只能1000。</font>**  
&emsp; **<font color = "red">确定给定的值是否与子查询或列表中的值相匹配。in在查询的时候，首先查询子查询的表，然后将内表和外表做一个笛卡尔积，然后按照条件进行筛选。所以</font><font color = "clime">相对内表比较小的时候，in的速度较快。</font>**  
2. exists指定一个子查询，检测行的存在。<font color = "clime">遍历循环外表，然后看外表中的记录有没有和内表的数据一样的。匹配上就将结果放入结果集中。</font><font color = "red">exists内层查询语句不返回查询的记录，而是返回一个真假值。</font>  
&emsp; **<font color = "clime">in和exists的区别：</font><font color = "red">如果子查询得出的结果集记录较少，主查询中的表较大且又有索引时应该用in，反之如果外层的主查询记录较少，子查询中的表大，又有索引时使用exists。</font>**  
3. **UNION与UNION ALL：** 默认地，UNION 操作符选取不同的值。如果允许重复的值，请使用UNION ALL。  


# 1. 联合查询
<!-- 
MySQL多表联表查询驱动表选择
https://www.jianshu.com/p/2ddcf4d9f0fe
 【223期】面试官：在MySQL查询中，为什么要用小表驱动大表 
 https://mp.weixin.qq.com/s/k4oPtfDwXfMm91Ms3IHaHw
-->


## 1.1. 连接查询  
&emsp; Where，1992年语法；Join on，1999年语法。  

### 1.1.1. 各种JOIN  
<!-- 
https://mp.weixin.qq.com/s/u66ll2Rg9mqP4WSfEK8R1g
--> 
#### 1.1.1.1. 常用的JOIN  
&emsp; 内连接、左外连接、右外连接、全外连接。  
&emsp; <font color = "red" >左外连接</font>：根据两张表的关系(外键关联)，笛卡尔过滤，也就是求出两张表的交集，<font color = "red">如果交集中，左边表的行， 在右边表中没有匹配，则该条记录左边表有数据， 右边表所有的字段都为null(如果左表数据，在右表中有多行匹配，则查询结果左表为多行显示)。</font>  

1. INNER JOIN  
&emsp; INNER JOIN 一般被译作内连接。内连接查询能将左表(表 A)和右表(表 B)中能关联起来的数据连接后返回。  
&emsp; 文氏图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-65.png)  
2. LEFT JOIN  
&emsp; LEFT JOIN 一般被译作左连接，也写作 LEFT OUTER JOIN。左连接查询会返回左表(表 A)中所有记录，不管右表(表 B)中有没有关联的数据。在右表中找到的关联数据列也会被一起返回。  
&emsp; 文氏图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-66.png)  
3. RIGHT JOIN  
&emsp; RIGHT JOIN 一般被译作右连接，也写作 RIGHT OUTER JOIN。右连接查询会返回右表(表 B)中所有记录，不管左表(表 A)中有没有关联的数据。在左表中找到的关联数据列也会被一起返回。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-67.png)  
4. FULL OUTER JOIN  
&emsp; FULL OUTER JOIN 一般被译作外连接、全连接，实际查询语句中可以写作 FULL OUTER JOIN 或 FULL JOIN。外连接查询能返回左右表里的所有记录，其中左右表里能关联起来的记录被连接后返回。  
&emsp; 文氏图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-68.png)  

#### 1.1.1.2. 延伸用法  
&emsp; ......
<!-- 
https://mp.weixin.qq.com/s/u66ll2Rg9mqP4WSfEK8R1g
--> 


### 1.1.2. ★★★自连接  
&emsp; [一些比较特殊的查询](/docs/SQL/trans.md)  


## 1.2. 子查询  
&emsp; 子查询(嵌套查询)：在一个select语句中，嵌入了另外一个select语句，那么被嵌入的select语句称之为子查询语句，外部那个select语句则称为主查询。子查询是辅助主查询的，要么充当条件，要么充当数据源。  

### 1.2.1. 子查询的位置  
1. 子查询能够出现在Where子句中(子查询在where字句中与比较运算符、列表运算符in、存在运算符exists等一起构成查询条件，完成有关操作)；
2. 也能够出现在From子句中，作为一个临时表使用；
3. 也能够出现在Select list中，作为一个字段值(select子查询返回的结果集必须是单行)来返回。

### 1.2.2. 子查询的分类  
#### 1.2.2.1. 标量子查询
&emsp; 子查询返回的结果是一个数据(一行一列)。子查询返回的值是max,min,avg等聚合函数得到的值作为一个数据。  
&emsp; 因为标量子查询只返回一个值，也可以使用其他运算符和标量子查询进行比较，如">, >=, <, <="等。  
&emsp; 示例：查询班级学生的平均身高

```sql
--其中第二个select语句就是一个标量子查询
SELECT * FROM students WHERE age > (SELECT AVG(age) FROM students);  
```

#### 1.2.2.2. 列子查询  
&emsp; 返回的结果是一列(一列多行)。格式：主查询where条件in(列子查询)  
&emsp; 示例：查询还有学生在班的所有班级名字。  

```sql
--1.找出学生表中所有的班级id，2.找出班级表中对应的名字
SELECT NAME FROM classes WHERE id IN (SELECT cls_id FROM students);
```

#### 1.2.2.3. 行子查询
&emsp; 返回的结果是一行(一行多列)。格式：主查询 where (字段1,2,...) = (行子查询)。行元素：将多个字段合成一个行元素，在行级子查询中会使用到行元素。  
&emsp; 示例：查找班级年龄最大，身高最高的学生。  

```sql
SELECT * FROM students WHERE (height,age) = (SELECT MAX(height),MAX(age) FROM students);
```

### 1.2.3. 关键字ALL SOME ANY
&emsp; 逻辑运算符(NOT、AND、OR)，比较运算符(=、<>、!=、>、>=、!>、<、<=、!<)。三者都作用于比较运算符和子查询之间，一般和嵌套查询一起用，some和any等效。  

### 1.2.4. 关键字In与exists
&emsp; 示例：一个user和一个order表，具体表的内容如下  
&emsp; user表：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-1.png)  
&emsp; order表：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-2.png)  

#### 1.2.4.1. 关键字In
&emsp; **<font color = "clime">in查询里面的数量最大只能1000。</font>**  
&emsp; **<font color = "red">确定给定的值是否与子查询或列表中的值相匹配。in在查询的时候，首先查询子查询的表，然后将内表和外表做一个笛卡尔积，然后按照条件进行筛选。所以相对内表比较小的时候，in的速度较快。</font>**  
&emsp; 具体sql语句如下：  

```sql
select * from user where user.id in (select order.user_id from order)
```
&emsp; 执行流程：  
1. 在数据库内部，执行子查询语句，执行如下代码：select order.user_id from order;执行完毕后，得到结果如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-3.png)  
2. 将查询到的结果和原有的user表做一个笛卡尔积，结果如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-4.png)  
3. 再根据user.id IN order.user_id的条件，将结果进行筛选（既比较id列和user_id 列的值是否相等，将不相等的删除）。最后，得到两条符合条件的数据。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-5.png)  

##### 1.2.4.1.1. 关键字in与关键字or与关键字between  
&emsp; number in(01,02,03)等效于numer = 01 or number = 02 or number = 03  

##### 1.2.4.1.2. 关键字in超过1000个条件的处理  
......

#### 1.2.4.2. 关键字Exists  
&emsp; exists指定一个子查询，检测行的存在。<font color = "clime">遍历循环外表，然后看外表中的记录有没有和内表的数据一样的。匹配上就将结果放入结果集中。</font><font color = "red">exists内层查询语句不返回查询的记录，而是返回一个真假值。</font>  

```sql
select user.* from user where exists(select order.user_id from order where user.id = order.user_id);
```
&emsp; 这条sql语句的执行结果和上面的in的执行结果是一样的。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-6.png)  
&emsp; 但是，不一样的是它们的执行流程完全不一样：  
1. 使用exists关键字进行查询的时候，首先查询的不是子查询的内容，而是查主查询的表，即先执行的sql语句是：`select user.* from user;`。得到的结果如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-7.png)  
2. 然后，根据表的每一条记录，执行exists(select order.user_id from order where user.id = order.user_id)，依次去判断where后面的条件是否成立。如果成立则返回true不成立则返回false。如果返回的是true的话，则该行结果保留，如果返回的是false的话，则删除该行，最后将得到的结果返回。  

#### 1.2.4.3. In与Exists区别  
&emsp; **<font color = "clime">in和exists的区别：</font><font color = "red">如果子查询得出的结果集记录较少，主查询中的表较大且又有索引时应该用in，反之如果外层的主查询记录较少，子查询中的表大，又有索引时使用exists。</font>**  

### 1.2.5. 子查询与连接查询的区别  
&emsp; 表连接都可以用子查询，但不是所有子查询都能用表连接替换，子查询比较灵活，方便，形式多样，适合用于作为查询的筛选条件，而表连接更适合查看多表的数据。  
&emsp; 子查询不一定需要两个表有关联字段，而连接查询必须有字段关联(所谓的主外键关系)。  
&emsp; 连接查询的性能优于子查询。  

----

## 1.3. 组合查询，UNION、INTERSECT、EXCEPT  
&emsp; UNION，并集；INTERSECT，交集；EXCEPT，差集。 

### 1.3.1. UNION运算符，并集  
<!-- 
当ALL随UNION一起使用时(即UNION ALL)，不消除重复行。两种情况下，派生表的每一行不是来自TABLE1就是来自TABLE2。 

&emsp; UNION：  
1. 其目的是将两个SQL语句的结果合并起来；  
2. 它的一个限制是两个SQL语句所产生的栏位需要是同样的资料种类；  
3. UNION只是将两个结果联结起来一起显示，并不是联结两个表；  
4. UNION在进行表链接后会筛选掉重复的记录。  

&emsp; UNION ALL：  
1. 这个指令的目的也是要将两个 SQL 语句的结果合并在一起；  
2. UNION ALL 和 UNION 不同之处在于 UNION ALL 会将每一个符合条件的资料都列出来，无论资料值有无重复；  
3. UNION ALL只是简单的将两个结果合并后就返回。这样，如果返回的两个结果集中有重复的数据，那么返回的结果集就会包含重复的数据了。  
-->
&emsp; UNION运算符通过组合其他两个结果表(例如TABLE1和TABLE2)并消去表中任何重复行而派生出一个结果表。UNION内部的SELECT语句必须拥有相同数量的列。列也必须拥有相似的数据类型。同时，每条SELECT语句中的列的顺序必须相同。UNION结果集中的列名总是等于UNION中第一个SELECT语句中的列名。    
&emsp; **<font color = "clime">UNION与UNION ALL：默认地，UNION 操作符选取不同的值。如果允许重复的值，请使用UNION ALL。</font>** 从效率上说，sql union all的执行效率要比sql union效率要高很多，这是因为使用sql union需要进行排重，而sql union all是不需要排重的，这一点非常重要，因为对于一些单纯地使用分表来提高效率的查询，完全可以使用sql union all。  