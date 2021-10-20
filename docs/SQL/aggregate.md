
<!-- TOC -->

- [1. MySql分表聚合查询](#1-mysql分表聚合查询)
    - [1.1. 一般聚合逻辑](#11-一般聚合逻辑)
    - [1.2. 带有其他约束的聚合查询](#12-带有其他约束的聚合查询)

<!-- /TOC -->

# 1. MySql分表聚合查询
<!-- 
https://blog.csdn.net/weixin_29555609/article/details/113946668
-->

## 1.1. 一般聚合逻辑
&emsp; 一般聚合逻辑SUM/COUNT/MIN/MAX/AVG/……，除了AVG计算的情形比较特殊，需要拆分为SUM和COUNT单独计算：

```sql
select avg(x) as ax from t_xxx where y=1;
```

&emsp; 拆分为：

```sql
select sum(x) as ax_num, count(x) as ax_den from t_xxx_{SUB-TABLE-ID} where y=1;
```

&emsp; 最后使用ax_num/ax_den计算得到结果。  
&emsp; 其余的基本上就是在各个分表做操作以后再次聚合即可(即求最大值的最大值，求和的和……)，这个没什么可说的。  
&emsp; 下面我们只针对SUM、MIN，MAX，COUNT作讨论。  

## 1.2. 带有其他约束的聚合查询
&emsp; 如果一个语句带有Group By、Order By，Limit的时候事情就不是那么简单了。(TOP可以解析为这三个操作的组合上，暂不考虑)  
&emsp; 正常来说，应该把所有的子句在各个分表上都执行一遍然后再聚合，但是事实并非如此。  

&emsp; 正常聚合即可的操作组合

    group by a
    group by a order by b
    group by a limit x, y
    limit x, y

&emsp; 需要改变SQL语句的坑爹操作  

1. order by b limit x, y  
&emsp; 这个时候需要将在子库上运行的语句变为order by b limit 0, y。  
&emsp; 原理很简单，举个小栗子就可以说明：如果要求全年级考试成绩的5~10名，那么应该是拿出每个班的1~10名进行比较而不是拿出每个班的5~10名进行比较。  

2. group by a order by b limit x, y  
&emsp; 这个时候需要去掉limit条件，变为group by a order by b，这个原理也不复杂，也是一个小栗子就可以说明：  
&emsp; 如果要求考试总成绩的5~10名，那么应该将所有学生的成绩都算出来再求，而不是求每个考试科目的前十名，这样有可能会漏掉一些学生得到成绩。

&emsp; 注意点：  
&emsp; 两个需要改变limit条件的语句要慎用，否则可能会造成一些性能问题。  
