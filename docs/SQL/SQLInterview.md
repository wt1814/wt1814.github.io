<!-- TOC -->

- [1. SQL面试题](#1-sql面试题)
    - [1.1. case when](#11-case-when)
    - [1.2. 行列转换](#12-行列转换)
        - [1.2.1. 行列转换](#121-行列转换)
        - [1.2.2. 列转行](#122-列转行)
        - [1.2.3. 行转列(将原表字段名转为结果集中字段值)](#123-行转列将原表字段名转为结果集中字段值)

<!-- /TOC -->


# 1. SQL面试题  

## 1.1. case when  

查询两门以上不及格课程的同学的学号，以及不及格课程的平均成绩

select 学号, avg(case when 成绩<60 then 成绩 else null end)

from score

group by 学号

having sum(case when 成绩<60 then 1 else 0 end)>=2;



## 1.2. 行列转换  
### 1.2.1. 行列转换  
&emsp; 行列互转，可以分为静态互转，即事先知道要处理多少行(列)；动态互转，事先不知道处理多少行(列)。以下讨论静态互转。数据如下：  
![image](http://182.92.69.8:8081/img/SQL/sql-9.png)  
![image](http://182.92.69.8:8081/img/SQL/sql-10.png)  

### 1.2.2. 列转行  
&emsp; 在日常的工作中，使用数据库查看数据是很经常的事，数据库的数据非常多，如果此时的数据设计是一行行的设计话，就会有 **<font color = "clime">多行同一个用户的数据，</font>** 查看起来比较费劲，如果数据较多时，不方便查看，为了更加方便工作中查看数据，如果可以随时切换行列数据的显示更好。  
&emsp; <font color = "red">列转行，将原表字段值转为结果集中字段名。</font>使用case when then else end函数结合聚合函数、group by。或其他同义的函数。  
&emsp; <font color = "red">第1步，创建伪列。第2步，分组求和。</font>  

```sql
select 姓名,
SUM(case 课程 when  '语文' then 分数 else 0 end) as 语文, --伪列
SUM(case 课程 when  '数学' then 分数 else 0 end) as 数学, --伪列
SUM(case 课程 when  '物理' then 分数 else 0 end) as 物理  --伪列
from scores group by 姓名
```

### 1.2.3. 行转列(将原表字段名转为结果集中字段值)  
&emsp; <font color = "red">行转列，将原表字段名转为结果集中字段值。</font>使用union all函数。  

```sql
select 姓名,'语文' as 课程, 语文 as 分数 from scores2 
union all select 姓名, '数学' as 课程, 数学 as 分数 from scores2 
union all select 姓名, '物理' as 课程, 物理 as 分数 from scores2 
order by 姓名 desc
```
