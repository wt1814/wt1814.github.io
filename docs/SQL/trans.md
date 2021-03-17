

<!-- TOC -->

- [1. 一些特殊查询](#1-一些特殊查询)
    - [1.1. 行列转换](#11-行列转换)
        - [1.1.1. 行列转换](#111-行列转换)
        - [1.1.2. 列转行](#112-列转行)
        - [1.1.3. 行转列(将原表字段名转为结果集中字段值)](#113-行转列将原表字段名转为结果集中字段值)
    - [1.2. 树状图](#12-树状图)
        - [层次化查询](#层次化查询)
        - [递归查询](#递归查询)
    - [1.3. 自连接](#13-自连接)
        - [1.3.1. 用SQL自连接查询处理列之间的关系](#131-用sql自连接查询处理列之间的关系)
        - [1.3.2. SQL自连接查询表示其它关系](#132-sql自连接查询表示其它关系)

<!-- /TOC -->


# 1. 一些特殊查询
## 1.1. 行列转换  
<!-- 

-->

### 1.1.1. 行列转换  
&emsp; 行列互转，可以分为静态互转，即事先知道要处理多少行(列)；动态互转，事先不知道处理多少行(列)。以下讨论静态互转。数据如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-9.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-10.png)  

### 1.1.2. 列转行  
&emsp; 在日常的工作中，使用数据库查看数据是很经常的事，数据库的数据非常多，如果此时的数据设计是一行行的设计话，就会有 **<font color = "lime">多行同一个用户的数据，</font>** 查看起来比较费劲，如果数据较多时，不方便查看，为了更加方便工作中查看数据，如果可以随时切换行列数据的显示更好。  
&emsp; <font color = "red">列转行，将原表字段值转为结果集中字段名。</font>使用case when then else end函数结合聚合函数、group by。或其他同义的函数。  
&emsp; <font color = "red">第1步，创建伪列。第2步，分组求和。</font>  

```sql
select 姓名,
SUM(case 课程 when  '语文' then 分数 else 0 end) as 语文, --伪列
SUM(case 课程 when  '数学' then 分数 else 0 end) as 数学, --伪列
SUM(case 课程 when  '物理' then 分数 else 0 end) as 物理  --伪列
from scores group by 姓名
```

### 1.1.3. 行转列(将原表字段名转为结果集中字段值)  
&emsp; <font color = "red">行转列，将原表字段名转为结果集中字段值。</font>使用union all函数。  

```sql
select 姓名,'语文' as 课程, 语文 as 分数 from scores2 
union all select 姓名, '数学' as 课程, 数学 as 分数 from scores2 
union all select 姓名, '物理' as 课程, 物理 as 分数 from scores2 
order by 姓名 desc
```


## 1.2. 树状图  
<!-- 
SQL 高级查询 ——（层次化查询，递归）
https://cloud.tencent.com/developer/article/1559389?from=information.detail.%E5%B1%82%E6%AC%A1%E5%8C%96%E6%9F%A5%E8%AF%A2
-->
### 层次化查询  
&emsp; **<font color = "clime">用到CONNECT BY和START WITH语法。</font>**   

&emsp; 层次化结构可以理解为树状数据结构，由节点构成。比如常见的组织结构由一个总经理，多个副总经理，多个部门部长组成。再比如在生产制造中一件产品会有多个子零件组成。举个简单的例子，如下图所示  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-155.png)  

汽车作为根节点，下面包含发动机和车身两个子节点，而子节点又是由其他叶节点构成。(叶节点表示没有子节点的节点)  
假如要把这些产品信息存储到数据库中，会形成如下数据表。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-156.png)  


表结构.png

我们用 parent_product_id 列表示当前产品的父产品是哪一个。

那么用 SQL 语句如何进行层次化查询呢？这里就要用到 CONNECT BY 和 START WITH 语法。
我们先把 SQL 写出来，再来解释其中的含义。

```sql
SELECT
  level,
  id,
  parent_product_id,
  name
FROM
  product
  START WITH id  = 1
  CONNECT BY prior id = parent_product_id
ORDER BY
  level
```

查询结果如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-157.png)  
解释一下：LEVEL 列表示当前产品属于第几层级。START WITH 表示从哪一个产品开始查询,CONNECT BY PRIOR 表示父节点与子节点的关系，每一个产品的 ID 指向一个父产品。  
如果把 START WITH 的查询起点改为 id = 2,重新运行上面的 SQL 语句将会得到如下结果：  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-158.png)  


因为 id=2 的产品是车身，就只能查到车身下面的子产品。  

当然，我们可以把查询结果美化一下，使其更有层次感，我们让根节点下面的 LEVEL 前面加几个空格即可。把上面的 SQL 稍微修改一下。为每个 LEVEL 前面增加 2*(LEVEL-1)个空格，这样第二层就会增加两个空格，第三层会增加四个空格。  

SELECT
  level,
  id,
  parent_product_id,
  LPAD(' ', 2 * (level - 1)) || name AS name
FROM
  product
  START WITH id  = 1
  CONNECT BY prior id = parent_product_id

查询结果已经有了层次感，如下图：  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-159.png)  




### 递归查询  




## 1.3. 自连接  
&emsp; 自连接处理列与列之间的逻辑关系；  

### 1.3.1. 用SQL自连接查询处理列之间的关系  
&emsp; <font color = "red">SQL自连接解决了列与列之间的逻辑关系之层次关系。</font>当所要查询的信息都出于同一个表，而又不能直接通过该表的各个列的直接层次关系得到最终结果的时候，那么应该考虑使用表的自连接查询。  

```sql
SELECT FIRST.CNumber, SECOND.PCNumber FROM Course FIRST, Course SECOND WHERE FIRST.PCNumber=SECOND.CNumber;  
```
&emsp; 在这个代码中，只涉及到一个表，即课程信息表COURSE(CNumber，CName， PCNumber)，其中CNumber是该课程的课程号，PCNumber是该课程的先修课课程号。查询结果集是FIRST表中的课程号CNumber和该课程号所对应的间接先修课课程号。  

### 1.3.2. SQL自连接查询表示其它关系  
&emsp; <font color = "red">SQL自连接查询还可用于处理列之间的顺序关系、因果关系等多种逻辑关系。</font>此外，SQL自身查询还可以用于处理单列本身的逻辑关系。  
&emsp; 对单列的逻辑关系的处理，示例：  

```sql
SELECT FIRST.Num,FIRST Stop,SECOND.Stop FROM Route FIRST, Route SECOND WHERE FIRST.NUM=SECOND.NUM;
```
&emsp; 表Route(Num， Stop)，可以表示某一线路的火车的车站线路信息。Num表示该车的车次号，Stop表示该车次停靠的城市名称。上面的代码，可以求出某一线路的火车可以联通的任意两个城市的名称。原来表Route中的每一个元组，只能表示车号和该车的某一站点的信息，实际上，这是“1Vs1”的映射关系。  

&emsp; 对单一的列进行连接处理，示例：  

```sql
SELECT FIRST.Num,SECOND.Num,FIRST.Stop FROM Route FIRST, Route SECOND WHERE FRIST.Stop=SECOND.Stop;
```
&emsp; 上面的SQL代码，求出了路经相同城市的车次的信息。原表中的车次和车站是“1Vs1”关系，通过自连接后，得到了车次和车站的“多Vs1”关系。  

---