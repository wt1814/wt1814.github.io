

<!-- TOC -->

- [1. 基本查询语句](#1-基本查询语句)
    - [1.1. 基本查询结构](#11-基本查询结构)
        - [1.1.1. SQL语句的组成部分](#111-sql语句的组成部分)
        - [1.1.2. ★★★查询SQL的执行顺序](#112-★★★查询sql的执行顺序)
    - [1.2. 基本查询详解](#12-基本查询详解)
        - [1.2.1. Distinct关键字](#121-distinct关键字)
            - [1.2.1.1. Distinct多列操作](#1211-distinct多列操作)
            - [1.2.1.2. Distinct与Count(聚合函数)](#1212-distinct与count聚合函数)
            - [1.2.1.3. Distinct与group by比较(都能将结果去重)](#1213-distinct与group-by比较都能将结果去重)
        - [1.2.2. TOP关键字](#122-top关键字)
        - [1.2.3. Like关键字-1](#123-like关键字-1)
        - [1.2.4. Group By关键字，分组函数，结合聚合函数](#124-group-by关键字分组函数结合聚合函数)
            - [1.2.4.1. Group By 和 Order By和Top](#1241-group-by-和-order-by和top)
            - [1.2.4.2. Having关键字与Where的区别](#1242-having关键字与where的区别)
            - [1.2.4.3. Group By多字段分组](#1243-group-by多字段分组)
        - [1.2.5. Order By关键字](#125-order-by关键字)
        - [1.2.6. Limit，分页](#126-limit分页)
    - [1.3. 其他](#13-其他)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 基本查询SQL执行顺序：from -> on -> join -> where -> group by ->  avg,sum.... ->having -> select -> distinct -> order by -> top,limit。 
2. distinct关键字：Distinct与Count(聚合函数)，COUNT()会过滤掉为NULL的项。  
3. 分组函数  
&emsp; **<font color = "clime">查询结果集中有统计数据时，就需要使用分组函数。</font>**  
&emsp; **<font color = "red">Group By分组函数中，查询只能得到组相关的信息。组相关的信息(统计信息)：count,sum,max,min,avg。</font> 在select指定的字段要么包含在Group By语句的后面，作为分组的依据；要么被包含在聚合函数中。group by是对结果集分组，而不是查询字段分组。**  
&emsp; **<font color = "red">Group By含有去重效果。</font>**  


# 1. 基本查询语句  
&emsp; 数据库表中的一行叫做一条记录，一列是一个属性。首行是数据库表中字段或自定义别名，对应的列就是其对应的数值。  

## 1.1. 基本查询结构
&emsp; 基本查询语句：  

    SELECT select_list [INTO new_table_name] 
    [FROM table_source] [WHERE search_condition] 
    /[JOIN 表名] [ON 连接条件] [WHERE search_condition] 
    [GROUP BY group_by_expression] 
    [HAVING search_condition] 
    [ORDER BY order_expression [ASC | DESC]]
    [ LIMIT {[offset,]rowcount | row_count OFFSET offset}]  

&emsp; group by必须放在order by和limit之前。  

### 1.1.1. SQL语句的组成部分  
&emsp; **<font color = "red">SELECT语句有哪几部分构成？作用分别是什么？</font>**  
1. SELECT关键字；  
2. 谓词：DISTINCT，TOP n；  
3. 查询字段：*或用逗号分隔的字段列表；  
4. FROM子句：用逗号分隔的表或视图列表；  
5. WHERE子句：查询条件；  
6. GROUP BY子句：分组字段；  
7. HAVING子句：针对分组字段的查询条件；  
8. ORDER BY子句：排序字段列表；  
9. limit子句：分页。  

### 1.1.2. ★★★查询SQL的执行顺序  
<!-- 
&emsp; 执行顺序：from -> on -> join -> where -> group by ->  avg,sum.... ->having -> select -> distinct -> order by -> top，limit，下文讲解。 
-->
```sql
SELECT DISTINCT
    < select_list >
FROM
    < left_table > < join_type >
JOIN < right_table > ON < join_condition >
WHERE
    < where_condition >
GROUP BY
    < group_by_list >
HAVING
    < having_condition >
ORDER BY
    < order_by_condition >
LIMIT < limit_number >
```
&emsp; 查询语句如上，sql的执行顺序：  
![image](http://www.wt1814.com/static/view/images/SQL/sql-8.png)  

* FROM 连接  
    &emsp; 首先，对SELECT语句执行查询时，<font color = "red">对FROM关键字两边的表执行连接，会形成笛卡尔积，</font>这时候会产生一个虚表VT1(virtual table)。  

    &emsp; **笛卡尔积：**  

            笛卡尔乘积是指在数学中，两个集合X和Y的笛卡尔积，又称直积，表示为X × Y，第一个对象是X的成员而第二个对象是Y的所有可能有序对的其中一个成员。
            假设集合A={a, b}，集合B={0, 1, 2}，则两个集合的笛卡尔积为{(a, 0), (a, 1), (a, 2), (b, 0), (b, 1), (b, 2)}。

    &emsp; **虚表：**  

            在 MySQL 中，有三种类型的表：
            一种是永久表，永久表就是创建以后用来长期保存数据的表。
            一种是临时表，临时表也有两类，一种是和永久表一样，只保存临时数据，但是能够长久存在的；还有一种是临时创建的，SQL 语句执行完成就会删除。
            一种是虚表，虚表其实就是视图，数据可能会来自多张表的执行结果。
        
* ON 过滤  
    &emsp; 然后对FROM连接的结果进行 ON 筛选，创建VT2，把符合记录的条件存在 VT2 中。  

* JOIN 连接  
    &emsp; 第三步，如果是 OUTER JOIN(left join、right join) ，那么这一步就将添加外部行，如果是 left join 就把 ON 过滤条件的左表添加进来，如果是 right join ，就把右表添加进来，从而生成新的虚拟表 VT3。  

* WHERE 过滤  
    &emsp; 第四步，是执行WHERE过滤器，对上一步生产的虚拟表引用WHERE筛选，生成虚拟表VT4。  

* GROUP BY  
    &emsp; 根据 group by字句中的列，会对 VT4 中的记录进行分组操作，产生虚拟机表 VT5。如果应用了group by，那么后面的所有步骤都只能得到的 VT5 的列或者是聚合函数(count、sum、avg等)。  

* HAVING  
    &emsp; 紧跟着 GROUP BY 字句后面的是 HAVING，使用 HAVING 过滤，会把符合条件的放在 VT6。  

* SELECT  
    &emsp; 第七步才会执行 SELECT 语句，将 VT6 中的结果按照 SELECT 进行刷选，生成 VT7。  

* DISTINCT  
    &emsp; 在第八步中，会对 TV7 生成的记录进行去重操作，生成 VT8。事实上如果应用了group by 子句，那么 distinct 是多余的，原因同样在于，分组的时候是将列中唯一的值分成一组，同时只为每一组返回一行记录，那么所以的记录都将是不相同的。  

* ORDER BY  
    &emsp; 应用 order by 子句。按照 order_by_condition 排序 VT8，此时返回的一个游标，而不是虚拟表。sql 是基于集合的理论的，集合不会预先对它的行排序，它只是成员的逻辑集合，成员的顺序是无关紧要的。  

## 1.2. 基本查询详解  
&emsp; **《MySQL必知必会》**  
<!-- 
where 1=1
SQL 语句中 where 条件后 写上1=1 是什么意思 
https://mp.weixin.qq.com/s/tL54fxgT1tY3JpaSjR--Ow
-->

### 1.2.1. Distinct关键字  
```sql
select distinct expression[,expression...] from tables [where conditions];
```
&emsp; 针对NULL的处理： **<font color = "clime">distinct对NULL不进行过滤，即返回的结果中是包含NULL值的。</font>**  

#### 1.2.1.1. Distinct多列操作  
1. DISTINCT必须放在第一个参数前。  
&emsp; distinct name,id 过滤掉name和id两个字段都重复的记录。select id,distinct name from user，sql语句会报错，因为distinct必须放在要查询字段的开头。所以 **<font color = "clime">一般distinct用来查询不重复记录的条数。</font>**  
&emsp; **<font color = "clime">如果要查询不重复的记录，可以使用group by：</font>**   

    ```sql
    select id,name from user group by name;
    ```  
2. DISTINCT表示对后面的所有参数的拼接，取不重复的记录。即distinct作用在多个字段的时候，将所有字段值都相同的记录“去重”。  

#### 1.2.1.2. Distinct与Count(聚合函数)  
&emsp; COUNT()会过滤掉为NULL的项。  

```sql
select *, count(distinct name) from table group by name; 
```

#### 1.2.1.3. Distinct与group by比较(都能将结果去重)  

|数据分布|去重方式|原因|
|---|---|---|
|离散|group|distinct空间占用较大，在时间复杂度允许的情况下，group可以发挥空间复杂度优势|
|集中|distinct|distinct空间占用较小，可以发挥时间复杂度优势|

&emsp; 两个极端：  

* 数据列的所有数据都一样，即去重计数的结果为1时，用distinct最佳。
* 如果数据列唯一，没有相同数值，用group最好。

### 1.2.2. TOP关键字  
&emsp; select top 1 搜索最**  
&emsp; 例：从select结果中显示前4行，select top 4 * from Employee;  

### 1.2.3. Like关键字-1  
&emsp; **Sql模糊查询like条件中特殊字符需要转义后才能搜索到结果。**  

|字符|作用|转义字符|
|---|---|---|
|'|包裹搜索条件|\'|
|%|代替任意数目的任意字符|\%|
|_|代替一个任意字符|\_|
|\\\\ | 转义符号 | \\\\\\\\ |  

&emsp; 反斜线\的处理：由于MySQL在字符串中使用C转义语法(例如，用‘\n’代表一个换行字符)，在LIKE字符串中，必须将用到的‘\’双写。例如，若要查找‘\n’，必须将其写成‘\\n’。而若要查找‘\’，则必须将其写成like‘%\\\\%’;原因是反斜线符号会被语法分析程序剥离一次，在进行模式匹配时，又会被剥离一次，最后会剩下一个反斜线符号接受匹配。  

```sql
SELECT * FROM E_MDM_MATERIAL WHERE LONG_DESC LIKE '%\\\\\\\\%';
```

### 1.2.4. Group By关键字，分组函数，结合聚合函数  
&emsp; **<font color = "clime">查询结果集中有统计数据时，就需要使用分组函数。</font>**  
&emsp; **<font color = "red">Group By分组函数中，查询只能得到组相关的信息。组相关的信息(统计信息)：count,sum,max,min,avg。</font> 在select指定的字段要么包含在Group By语句的后面，作为分组的依据；要么被包含在聚合函数中。group by是对结果集分组，而不是查询字段分组。**  
&emsp; **<font color = "red">Group By含有去重效果。</font>**  

#### 1.2.4.1. Group By 和 Order By和Top  
&emsp; SQL语句查询记录中重复最多的记录。  

```sql
SELECT top 1 NAME FROM a1 GROUP BY NAME ORDER BY COUNT(*) DESC;
```

#### 1.2.4.2. Having关键字与Where的区别  
&emsp; 关键字having相当于where条件。当使用了分组查询group by，又要加条件时，使用having非where。(在SQL中增加HAVING子句原因是，WHERE关键字无法与合计函数一起使用)也可以Having和Where的联合使用。  
&emsp; where子句的作用是在对查询结果进行分组前，将不符合where条件的行去掉，即在分组之前过滤数据，where条件中不能包含聚组函数，使用where条件过滤出特定的行。having子句的作用是筛选满足条件的组，即在分组之后过滤数据，条件中经常包含聚组函数，使用having条件过滤出特定的组，也可以使用多个分组标准进行分组。  
* having只能用于group by(分组统计语句中)；
* where是用于在初始表中筛选查询，having用于在where和group by结果分组中查询；
* having子句中的每一个元素也必须出现在select列表中；
* having语句可以使用聚合函数，而where不使用；

&emsp; 示例：Having和Where的联合使用方法  

```sql
select SUM(number)from A where number >8 group by type SUM(number) > 10
```

&emsp; 当一个语句中同时含有where、group by、having及聚集函数时，执行顺序如下：1.执行where子句查找符合条件的数据；2.使用group by子句对数据进行分组；对group by子句形成的组运行聚集函数计算每一组的值；3.最后用having子句去掉不符合条件的组。  

#### 1.2.4.3. Group By多字段分组  
&emsp; GROUP BY X意思是将所有具有相同X字段值的记录放到一个分组里。  
&emsp; GROUP BY X, Y意思是将所有具有相同X字段值和Y字段值的记录放到一个分组里。   


### 1.2.5. Order By关键字  
<!-- 
ORDER BY中混合ASC和DESC

MySQL中order by的实现原理 
https://mp.weixin.qq.com/s/xSjHiixbt2JODUedSSwWUg

-->

### 1.2.6. Limit，分页  
&emsp; [limit](/docs/SQL/limit.md)  


## 1.3. 其他  
<!-- 
mysql序号rownum行号实现  
https://blog.csdn.net/vtopqx/article/details/97388684?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.channel_param&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.channel_param
-->
