

<!-- TOC -->

- [1. 数据库对象](#1-数据库对象)
    - [1.1. Sql序列](#11-sql序列)
        - [1.1.1. 序列的创建语法](#111-序列的创建语法)
        - [1.1.2. 使用序列](#112-使用序列)
    - [1.2. Sql触发器](#12-sql触发器)
        - [1.2.1. 简介](#121-简介)
        - [1.2.2. 语法](#122-语法)
        - [1.2.3. 应用场景](#123-应用场景)

<!-- /TOC -->

# 1. 数据库对象  
![image](http://182.92.69.8:8081/img/SQL/sql-69.png) ![image](http://182.92.69.8:8081/img/SQL/sql-70.png)  

&emsp; 数据库对象有表格、索引、视图、图表、缺省值、规则、触发器、语法、存储过程、用户、序列、函数等。  

## 1.1. Sql序列  
&emsp; 序列是一种数据库对象，用来自动产生一组唯一的序号；序列是一种共享式的对象，多个用户可以共同使用序列中的序号。经典应用，替换id。  
&emsp; 查看所有序列的情况：  

```sql
select * from user_sequences;  
```

### 1.1.1. 序列的创建语法  

```
CREATE SEQUENCE sequencename
[INCREMENT BY n]   定义序列增长步长，省略为1
[START WITH m]   序列起始值，省略为1
[{MAXVALUE n | NOMAXVALUE}]   序列最大值，
[{MINVALUE n | NOMINVALUE}]   序列最小值
[{CYCLE | NOCYCLE}]   到达最大值或最小值后，继续产生序列(默认NOCYCLE)
[{CACHE n | NOCACHE}];    CACHE默认是20
```
&emsp; 创建一个简单序列：  

```sql
create sequence myseq;
--或
create sequence myseq increment by 5 start with 30;
```
&emsp; 使用循环序列：循环序列是指每次调用nextval可以产生指定范围的数据，比如在1、3、5、7、9这5个数字中产生。  

```sql
create sequence myseq start with 1 increment by 2 maxvalue 10 minvalue 1 cycle cache 3;
```

### 1.1.2. 使用序列  
&emsp; **NEXTVAL：返回序列中下一个有效的值，任何用户都可以引用。** CURRVAL：存放序列的当前值，第一次使用时CURRVAL不能用。使用时需要指定序列的对象名。  

```sql
select myseq.currval from dual;
ORA-08002: 序列 MYSEQ.CURRVAL 尚未在此会话中定义
select myseq.nextval from dual;   值为1
select myseq.currval from dual;   值为1
```
&emsp; 实际应用：  

```sql
insert into member(mid,name) values (myseq.nextval,'Scott');
```

## 1.2. Sql触发器
### 1.2.1. 简介  
&emsp; 触发器(trigger)是SQL server 提供给程序员和数据分析员来保证数据完整性的一种方法，它是与表事件相关的特殊的存储过程，它的执行不是由程序调用，也不是手工启动，而是由事件来触发，比如当对一个表进行操作(insert，delete，update)时就会激活它执行。触发器经常用于加强数据的完整性约束和业务规则等。触发器可以从 DBA_TRIGGERS ，USER_TRIGGERS数据字典中查到。SQL3的触发器是一个能由系统自动执行对数据库修改的语句。  
&emsp; 触发器可以查询其他表，而且可以包含复杂的SQL语句。它们主要用于强制服从复杂的业务规则或要求。例如：您可以根据客户当前的帐户状态，控制是否允许插入新订单。  
&emsp; 触发器也可用于强制引用完整性，以便在多个表中添加、更新或删除行时，保留在这些表之间所定义的关系。然而，强制引用完整性的最好方法是在相关表中定义主键和外键约束。如果使用数据库关系图，则可以在表之间创建关系以自动创建外键约束。  
&emsp; 触发器与存储过程的唯一区别是触发器不能执行EXECUTE语句调用，而是在用户执行Transact-SQL语句时自动触发执行。  

&emsp; 触发器有如下作用：  

* 可在写入数据表前，强制检验或转换数据。  
* 触发器发生错误时，异动的结果会被撤销。  
* 部分数据库管理系统可以针对数据定义语言(DDL)使用触发器，称为DDL触发器。  
* 可依照特定的情况，替换异动的指令(INSTEAD OF)。  

### 1.2.2. 语法  

```sql
CREATE TRIGGER trigger_name trigger_time trigger_event
ON tbl_name FOR EACH ROW trigger_stmt
```

* trigger_name：标识触发器名称，用户自行指定；  
* trigger_time：标识触发时机，取值为BEFORE或AFTER；  
* trigger_event：标识触发事件，取值为INSERT、UPDATE或DELETE；  
* tbl_name：标识建立触发器的表名，即在哪张表上建立触发器；  
* trigger_stmt：触发器程序体，可以是一句SQL语句，或者用BEGIN和END包含的多条语句。  

&emsp; 可以建立6种触发器，即：BEFORE INSERT、BEFORE UPDATE、BEFORE DELETE、AFTER INSERT、AFTER UPDATE、AFTER DELETE。此外，不能同时在一个表上建立2个相同类型的触发器。  

### 1.2.3. 应用场景
1. 数据的迁移/备份。“不物理删除数据”。去除表中is_delete字段，删除表中记录。利用after delete类型的触发器将数据迁移到备份表中。  
2. 定义约束。与CHECK约束的不同：CHECK约束只能根据逻辑表达式或同一表中的另一列来验证列值。触发器可以引用其它表中的列。  
3. 级联更新。触发器可通过数据库中的相关表实现级联更改。通过级联引用完整性约束可以更有效地执行这些更改。  

&emsp; 建议：慎用触发器。触发器滥用会造成数据库及应用程序的维护困难(影响数据库的结构，同时增加了维护的复杂程序)。  
