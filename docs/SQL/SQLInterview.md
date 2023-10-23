<!-- TOC -->

- [1. SQL面试题](#1-sql面试题)
    - [SQL面试题](#sql面试题)
    - [1.1. case when](#11-case-when)
    - [1.2. 行列转换](#12-行列转换)
        - [1.2.1. 行列转换](#121-行列转换)
        - [1.2.2. 列转行](#122-列转行)
        - [1.2.3. 行转列(将原表字段名转为结果集中字段值)](#123-行转列将原表字段名转为结果集中字段值)

<!-- /TOC -->


# 1. SQL面试题  
<!--
https://www.cnblogs.com/smileberry/p/7411855.html
https://zhuanlan.zhihu.com/p/359621510

-->

## SQL面试题 
1. 用一条SQL语句查询出每门课都大于80分的学生姓名 name kecheng fenshu   
A: select distinct name from score where name not in (select distinct name from score where score<=80)  

B:select distince name t1 from score where 80< all (select score from score where name=t1);  


1、查询“001”课程比“002”课程成绩高的所有学生的学号；  
select a.S#
from (select s#,score from SC where C#=’001′) a,
(select s#,score from SC where C#=’002′) b
where a.score>b.score and a.s#=b.s#;   


14、查询各科成绩前三名的记录:(不考虑成绩并列情况)
SELECT t1.S# as 学生ID,t1.C# as 课程ID,Score as 分数
FROM SC t1
WHERE score IN (SELECT TOP 3 score
FROM SC
WHERE t1.C#= C#
ORDER BY score DESC)
ORDER BY t1.C#; 

15、查询每门功成绩最好的前两名
SELECT t1.S# as 学生ID,t1.C# as 课程ID,Score as 分数
FROM SC t1
WHERE score IN (SELECT TOP 2 score
FROM SC
WHERE t1.C#= C#
ORDER BY score DESC )
ORDER BY t1.C#;


查询两门以上不及格课程的同学的学号，以及不及格课程的平均成绩

select 学号, avg(case when 成绩<60 then 成绩 else null end)

from score

group by 学号

having sum(case when 成绩<60 then 1 else 0 end)>=2;
另一种写法：

select 学号, avg(成绩)

from score

where 成绩<60

group by 学号

having count(课程号)>=2;


查询没有学全所有课的学生的学号、姓名
select 学号, 姓名

from student

where exists (

select 学号

from score

group by 学号

having count(课程号) < 3

);


查询只选修了两门课的全部学生的学号和姓名

select 学号, 姓名

from student

where exists (

select 学号

from score

group by 学号

having count(课程号) = 2

);




查询各科成绩前2名的记录

(select 课程号,学号, 成绩

from score

where 课程号 = '0001'

order by 成绩 DESC

limit 2)

union ALL

(select 课程号,学号, 成绩

from score

where 课程号 = '0002'

order by 成绩 DESC

limit 2)

union ALL

(select 课程号,学号, 成绩

from score

where 课程号 = '0003'

order by 成绩 DESC

limit 2);


查询出每门课程的80分及以上和没到80分的人数

select

课程号,

sum(case when 成绩 > 80 then 1 else 0 end) as 80分及以上人数,

sum(case when 成绩 <= 80 then 1 else 0 end) as 没到80分人数

from score

group by 课程号;



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
