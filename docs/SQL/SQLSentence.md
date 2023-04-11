

# SQL语句的四大分类  
&emsp; sql语句被分为四大类：数据定义语言DDL、数据查询语言DQL、数据操纵语言DML、数据控制功能DCL。  
* 数据定义语言DDL（Data Ddefinition Language）  
&emsp; CREATE,DROP,ALTER。即对逻辑结构等有操作的，其中包括表结构，视图和索引。  
* 数据查询语言DQL（Data Query Language）  
&emsp; SELECT。即查询操作，以select关键字。各种简单查询，连接查询等都属于DQL。  
* 数据操纵语言DML（Data Manipulation Language）  
&emsp; INSERT,UPDATE,DELETE。即对数据进行操作的，对应上面所说的查询操作 DQL与DML共同构建了多数初级程序员常用的增删改查操作。而查询是较为特殊的一种 被划分到DQL中。  
* 数据控制功能DCL（Data Control Language）  
&emsp; GRANT,REVOKE,COMMIT,ROLLBACK。即对数据库安全性完整性等有操作的，可以简单的理解为权限控制等。  


&emsp; [基本查询语句](/docs/SQL/basicSelect.md)  
&emsp; &emsp; [limit](/docs/SQL/limit.md)  
&emsp; [连接查询](/docs/SQL/joinSelect.md)  
&emsp; [高级查询](/docs/SQL/trans.md)  
&emsp; [联合主键与复合主键](/docs/SQL/CompositeKey.md)  
&emsp; [null值](/docs/SQL/null.md)  
&emsp; [加密数据模糊查询](/docs/SQL/EncryptedQuery.md)  

