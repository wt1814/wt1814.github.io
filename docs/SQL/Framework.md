
<!-- TOC -->

- [1. MySql架构](#1-mysql架构)
    - [1.1. MySQL查询流程](#11-mysql查询流程)
    - [1.2. MySQL服务器介绍](#12-mysql服务器介绍)

<!-- /TOC -->

**<font color = "red">总结：</font>**  
1. MySQL查询流程  
&emsp; `1). 客户端请求 ---> 连接器（验证用户身份，给予权限）  ---> 2). 查询缓存（存在缓存则直接返回，不存在则执行后续操作） ---> 3). 分析器（对SQL进行词法分析和语法分析操作）  ---> 优化器（主要对执行的sql优化选择最优的执行方案方法）  ---> 4). 执行器（执行时会先看用户是否有执行权限，有才去使用这个引擎提供的接口） ---> 5). 去引擎层获取数据返回（如果开启查询缓存则会缓存查询结果）。`  
2. MySQL服务器主要分为Server层和存储引擎层。  
    1. <font color = "red">Server层包括连接器、查询缓存、分析器、优化器、执行器等。</font>涵盖MySQL的大多数核心服务功能，以及所有的内置函数（如日期、时间、数学和加密函数等），所有跨存储引擎的功能都在这一层实现，比如存储过程、触发器、视图等，还有 **<font color = "clime">一个通用的日志模块binglog日志模块。</font>**     
    2. `存储引擎：主要负责数据的存储和读取，`采用可以替换的插件式架构，支持 InnoDB、MyISAM、Memory等多个存储引擎，其中InnoDB引擎有自有的日志模块redolog模块。  

# 1. MySql架构  


## 1.1. MySQL查询流程  
<!-- 
你知道select语句和update语句分别是怎么执行的吗？
https://mp.weixin.qq.com/s/z175Z6OrLONcWUrotmjkVQ

 8张图，5大组件！了解MySQL查询语句执行过程。 
 https://mp.weixin.qq.com/s/jg0Os6ZhiP7KwFd6LLkZ1A
-->
&emsp; 很多的查询优化工作实际上就是遵循一些原则让MySQL的优化器能够按照预想的合理方式运行而已。  
&emsp; 当向MySQL发送一个请求的时候，MySQL到底做了些什么呢？  
![image](http://182.92.69.8:8081/img/SQL/sql-51.png)  
&emsp; MySQL整个查询执行过程，总的来说分为5个步骤：  
1. 客户端向MySQL服务器发送一条查询请求，进行连接。服务端进行验证。    
2. 服务器首先检查查询缓存，如果命中缓存，则立刻返回存储在缓存中的结果。否则进入下一阶段。  
3. 服务器进行SQL解析、预处理、再由优化器生成对应的执行计划。  
4. MySQL根据执行计划，调用存储引擎的API来执行查询。  
5. 将结果返回给客户端，同时缓存查询结果。  

&emsp; `1). 客户端请求 ---> 连接器（验证用户身份，给予权限）  ---> 2). 查询缓存（存在缓存则直接返回，不存在则执行后续操作） ---> 3). 分析器（对SQL进行词法分析和语法分析操作）  ---> 优化器（主要对执行的sql优化选择最优的执行方案方法）  ---> 4). 执行器（执行时会先看用户是否有执行权限，有才去使用这个引擎提供的接口） ---> 5). 去引擎层获取数据返回（如果开启查询缓存则会缓存查询结果）。`  


## 1.2. MySQL服务器介绍

![image](http://182.92.69.8:8081/img/SQL/sql-44.png)  
&emsp; **<font color = "clime">MySQL服务器主要分为Server层和存储引擎层。</font>**  
1. <font color = "red">Server层包括连接器、查询缓存、分析器、优化器、执行器等。</font>涵盖MySQL的大多数核心服务功能，以及所有的内置函数(如日期、时间、数学和加密函数等)，所有跨存储引擎的功能都在这一层实现，比如存储过程、触发器、视图等，还有 **<font color = "clime">一个通用的日志模块binglog日志模块。</font>**     
2. `存储引擎：主要负责数据的存储和读取，`采用可以替换的插件式架构，支持 InnoDB、MyISAM、Memory等多个存储引擎，其中InnoDB引擎有自有的日志模块redolog模块。  
&emsp; <font color = "red">InnoDB从MySQL5.5.5版本开始成为了默认存储引擎。即执行create table建表的时候，如果不指定引擎类型，默认使用的就是InnoDB。</font>也可以通过指定存储引擎的类型来选择别的引擎，比如在create table语句中使用engine=memory，来指定使用内存引擎创建表。不同存储引擎的表数据存取方式不同，支持的功能也不同。 

<!-- 
连接器：负责跟客户端建立连接、获取权限、维持和管理连接。
查询缓存：执行SQL语句之前，先查缓存，缓存结果可能是以key-value对方式存储的，key 是查询的语句，value 是查询的结果。
分析器：SQL词法分析，SQL语法分析

优化器：索引选择，选择一个执行效率高的，生成执行计划

执行器：操作引擎，返回执行结果

...
-->


<!-- 
1.1.1.1. 连接器  
&emsp; <font color = "red">连接器主要和身份认证和权限相关的功能相关。</font>主要负责用户登录数据库，进行用户的身份认证，包括校验账户密码，权限等操作，如果用户账户密码已通过，连接器会到权限表中查询该用户的所有权限，之后在这个连接里的权限逻辑判断都是会依赖此时读取到的权限数据，也就是说，后续只要这个连接不断开，即时管理员修改了该用户的权限，该用户也是不受影响的。  


&emsp; 第一步，先连接到这个数据库上，这时候使用的是连接器。连接器负责跟客户端建立连接、获取权限、维持和管理连接。连接命令一般是这么写的：  

    mysql -h$ip -P$port -u$user -p  

&emsp; 输完命令之后，就需要在交互对话里面输入密码。虽然密码也可以直接跟在-p后面写在命令行中，但这样可能会导致密码泄露。  
&emsp; 连接命令中的mysql是客户端工具，用来跟服务端建立连接。在完成经典的TCP握手后，连接器就要开始认证身份。  
&emsp; 如果用户名或密码不对，就会收到一个"Access denied for user"的错误，然后客户端程序结束执行。  
&emsp; 如果用户名密码认证通过，连接器会到权限表里面查出拥有的权限。之后，这个连接里面的权限判断逻辑，都将依赖于此时读到的权限。  
&emsp; 这就意味着，一个用户成功建立连接后，即使用管理员账号对这个用户的权限做了修改，也不会影响已经存在连接的权限。修改完成后，只有再新建的连接才会使用新的权限设置。  
&emsp; 连接完成后，如果没有后续的动作，这个连接就处于空闲状态，可以在show processlist命令中看到它。文本中这个图是show processlist的结果，其中的Command列显示为“Sleep”的这一行，就表示现在系统里面有一个空闲连接。  
![image](http://182.92.69.8:8081/img/SQL/sql-45.png)  
&emsp; 客户端如果太长时间没动静，连接器就会自动将它断开。这个时间是由参数wait_timeout控制的，默认值是8小时。  
&emsp; 如果在连接被断开之后，客户端再次发送请求的话，就会收到一个错误提醒： Lost connection to MySQL server during query。这时候如果你要继续，就需要重连，然后再执行请求了。  
&emsp; 数据库里面，长连接是指连接成功后，如果客户端持续有请求，则一直使用同一个连接。短连接则是指每次执行完很少的几次查询就断开连接，下次查询再重新建立一个。  
&emsp; 建立连接的过程通常是比较复杂的，所以建议在使用中要尽量减少建立连接的动作，也就是尽量使用长连接。  
&emsp; 但是全部使用长连接后，可能会发现，有些时候MySQL占用内存涨得特别快，这是因为MySQL在执行过程中临时使用的内存是管理在连接对象里面的。这些资源会在连接断开的时候才释放。所以如果长连接累积下来，可能导致内存占用太大，被系统强行杀掉(OOM)，从现象看就是MySQL异常重启了。  

&emsp; 怎么解决这个问题呢？可以考虑以下两种方案。  
&emsp; 定期断开长连接。使用一段时间，或者程序里面判断执行过一个占用内存的大查询后，断开连接，之后要查询再重连。  
&emsp; 如果用的是MySQL 5.7或更新版本，可以在每次执行一个比较大的操作后，通过执行 mysql_reset_connection来重新初始化连接资源。这个过程不需要重连和重新做权限验证，但是会将连接恢复到刚刚创建完时的状态。  

 1.1.1.2. 查询缓存  
&emsp; 查询缓存主要用来缓存所执行的SELECT语句以及该语句的结果集。  
&emsp; 连接建立后，执行查询语句的时候，会先查询缓存，MySQL会先校验这个sql 是否执行过，以Key-Value的形式缓存在内存中，Key是查询预计，Value是结果集。如果缓存key被命中，就会直接返回给客户端，如果没有命中，就会执行后续的操作，完成后也会把结果缓存起来，方便下一次调用。当然在真正执行缓存查询的时候还是会校验用户的权限，是否有该表的查询条件。  
&emsp; MySQL查询不建议使用缓存，因为查询缓存失效在实际业务场景中可能会非常频繁，假如对一个表更新的话，这个表上的所有的查询缓存都会被清空。对于不经常更新的数据来说，使用缓存还是可以的。所以，一般在大多数情况下都是不推荐去使用查询缓存的。  
&emsp; 在MySQL也提供了“按需使用”的方式。可以将参数query_cache_type设置成DEMAND，这样对于默认的SQL语句都不使用查询缓存。而对于确定要使用查询缓存的语句，可以用SQL_CACHE显式指定，像下面这个语句一样：  
&emsp; mysql> select SQL_CACHE * from T where ID=10；  
&emsp; MySQL8.0版本后删除了缓存的功能，官方也是认为该功能在实际的应用场景比较少，所以干脆直接删掉了。  

1.1.1.3. 分析器  
&emsp; MySQL没有命中缓存，那么就会进入分析器，解析SQL语句。分析器解析步骤：  
&emsp; 第一步，词法分析，一条SQL语句有多个字符串组成，首先要提取关键字，比如select，提出查询的表，提出字段名，提出查询条件等等。做完这些操作后，就会进入第二步。  
&emsp; 第二步，语法分析，主要就是判断输入的sql是否正确，是否符合MySQL的语法。如果语句不对，就会收到“You have an error in your SQL syntax”的错误提醒，比如下面这个语句select少打了开头的字母“s”。  
    
    mysql> elect * from t where ID=1;
    ERROR 1064 (42000): You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near 'elect * from t where ID=1' at line 1

&emsp; 完成这2步之后，MySQL就准备开始执行了，但是如何执行，怎么执行是最好的结果呢？这个时候需要使用优化器。  

1.1.1.4. 优化器  
&emsp; **<font color = "red">优化器是在表里面有多个索引的时候，决定使用哪个索引；或者在一个语句有多表关联(join)的时候，决定各个表的连接顺序。</font>** 可以说，经过了优化器之后可以说这个语句具体该如何执行就已经定下来。  
&emsp; 有的时候MySQL优化器采取它认为合适的索引来检索sql语句，但是可能它所采用的索引并不是我们想要的。这时就可以采用force index来强制优化器使用制定的索引。  

1.1.1.5. 执行器  
&emsp; **<font color = "red">MySQL通过分析器知道了要做什么，通过优化器知道了该怎么做，于是就进入了执行器阶段，开始执行语句。</font>**  
&emsp; 开始执行的时候，要先判断一下对这个表T有没有执行查询的权限，如果没有，就会返回没有权限的错误，如下所示(在工程实现上，如果命中查询缓存，会在查询缓存放回结果的时候，做权限验证。查询也会在优化器之前调用precheck验证权限)。  
    
    mysql> select * from T where ID=10;
    ERROR 1142 (42000): SELECT command denied to user 'b'@'localhost' for table 'T'  

&emsp; 如果有权限，就打开表继续执行。打开表的时候，执行器就会根据表的引擎定义，去使用这个引擎提供的接口。  
&emsp; 比如这个例子中的表T中，ID字段没有索引，那么执行器的执行流程是这样的：  
1. 调用InnoDB引擎接口取这个表的第一行，判断ID值是不是10，如果不是则跳过，如果是则将这行存在结果集中；  
2. 调用引擎接口取“下一行”，重复相同的判断逻辑，直到取到这个表的最后一行。  
3. 执行器将上述遍历过程中所有满足条件的行组成的记录集作为结果集返回给客户端。 
 
&emsp; 至此，这个语句就执行完成了。  

&emsp; 对于有索引的表，执行的逻辑也差不多。第一次调用的是“取满足条件的第一行”这个接口，之后循环取“满足条件的下一行”这个接口，这些接口都是引擎中已经定义好的。  
&emsp; 会在数据库的慢查询日志中看到一个rows_examined的字段，表示这个语句执行过程中扫描了多少行。这个值就是在执行器每次调用引擎获取数据行的时候累加的。  
&emsp; 在有些场景下，执行器调用一次，在引擎内部则扫描了多行，因此引擎扫描行数跟rows_examined并不是完全相同的。  
-->

