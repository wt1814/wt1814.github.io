<!-- TOC -->

- [1. MySql函数](#1-mysql函数)
    - [1.1. 控制流程函数](#11-控制流程函数)
        - [1.1.1. CASE WHEN THEN函数](#111-case-when-then函数)
        - [1.1.2. IF函数用法](#112-if函数用法)
        - [1.1.3. IFNULL函数](#113-ifnull函数)
        - [1.1.4. NULLIF函数](#114-nullif函数)

<!-- /TOC -->

# 1. MySql函数  
&emsp; **<font color = "red">控制流程函数、字符串函数、数学函数、日期时间函数、聚合函数</font>**  

## 1.1. 控制流程函数  
### 1.1.1. CASE WHEN THEN函数  
&emsp; Case具有两种格式。简单Case函数和Case搜索函数。  
&emsp; 第一种格式：简单Case函数，格式说明：  

    case 列名
    when 条件值1 then 选择项1
    when 条件值2 then 选项2.......
    else 默认值 end

&emsp; Eg：  

```sql
select case job_level when '1' then '1111' when '2' then '2222' when '3' then '3333' else 'eee' end from dbo.employee
```

&emsp; 第二种格式：Case搜索函数，格式说明：  

    case 
    when 列名=条件值1 then 选择项1
    when 列名=条件值2 then 选项2.......
    else 默认值 end

&emsp; Eg：  

```sql
update employee set e_wage = case when job_level = '1' then e_wage*1.97 when job_level = '2' then e_wage*1.07 when job_level = '3' then e_wage*1.06 else e_wage*1.05 end
```
&emsp; 比较：两种格式(列名所在位置不同)，可以实现相同的功能。  
&emsp; 简单Case函数的写法相对比较简洁，但是和Case搜索函数相比，功能方面会有些限制，比如写判断式。还有一个需要注意的问题，Case函数只返回第一个符合条件的值，剩下的Case部分将会被自动忽略。  

### 1.1.2. IF函数用法  
&emsp; 语法：IF(expr1,expr2,expr3)  
&emsp; 函数用法说明：如果expr1是TRUE，则IF()的返回值为expr2；否则返回值则为expr3。  

### 1.1.3. IFNULL函数  
&emsp; 语法：IFNULL(expr1,expr2)  
&emsp; 函数用法说明：假如expr1不为NULL，则IFNULL()的返回值为expr1；否则其返回值为expr2。  
&emsp; ifnull(null,0)----用0来替换null值；ifnull(string1,string2) 如果string1为空则用string2替换，不为空返回string1。  

### 1.1.4. NULLIF函数
&emsp; 语法：NULLIF(expr1,expr2)  
&emsp; 函数用法说明：NULLIF()函数将会检验提供的2个参数是否相等，如果相等，则返回NULL，如果不相等就返回第一个参数。  
