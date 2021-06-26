

<!-- TOC -->

- [1. undo log和binlog(两个逻辑日志)](#1-undo-log和binlog两个逻辑日志)
    - [1.1. undo log，回滚日志](#11-undo-log回滚日志)
        - [1.1.1. 简介](#111-简介)
        - [1.1.2. 写入流程及刷盘时机](#112-写入流程及刷盘时机)
        - [1.1.3. 对应的物理文件](#113-对应的物理文件)
    - [1.2. binlog，二进制日志(归档日志)](#12-binlog二进制日志归档日志)
        - [1.2.1. 简介](#121-简介)
        - [1.2.2. 写入流程及刷盘时机](#122-写入流程及刷盘时机)
        - [1.2.3. binlog日志格式](#123-binlog日志格式)
        - [1.2.4. 物理文件](#124-物理文件)

<!-- /TOC -->


**<font color = "red">总结：</font>**  
1. undo log
    1. **<font color = "clime">Undo log，回滚日志，是逻辑日记。undo log解决了事务原子性。</font>**    
    2. undo log主要记录了数据的逻辑变化，比如一条INSERT语句，对应一条DELETE的undo log，对于每个UPDATE语句，对应一条相反的UPDATE的undo log，这样在发生错误时，就能回滚到事务之前的数据状态。
    3. 事务开始之前，将当前的版本生成undo log。
2. binlog
    1.  binlog是mysql的逻辑日志，并且由Server层进行记录，使用任何存储引擎的mysql数据库都会记录binlog日志。  
    2. 在实际应用中，主要用在两个场景：主从复制和数据恢复。  
    3. 写入流程：SQL修改语句先写Binlog Buffer，事务提交时，按照一定的格式刷到磁盘中。binlog刷盘时机：对于InnoDB存储引擎而言，mysql通过sync_binlog参数控制biglog的刷盘时机。  

# 1. undo log和binlog(两个逻辑日志)
## 1.1. undo log，回滚日志
<!-- 
* undo log(回滚日志)  实现原子性  
&emsp; undo log 主要为事务的回滚服务。在事务执行的过程中，除了记录redo log，还会记录一定量的undo log。<font color = "red">undo log记录了数据在每个操作前的状态，如果事务执行过程中需要回滚，就可以根据undo log进行回滚操作。</font>单个事务的回滚，只会回滚当前事务做的操作，并不会影响到其他的事务做的操作。  
&emsp; Undo记录的是已部分完成并且写入硬盘的未完成的事务，默认情况下回滚日志是记录下表空间中的(共享表空间或者独享表空间)  

&emsp; 二种日志均可以视为一种恢复操作，redo_log是恢复提交事务修改的页操作，而undo_log是回滚行记录到特定版本。二者记录的内容也不同，redo_log是物理日志，记录页的物理修改操作，而undo_log是逻辑日志，根据每行记录进行记录。  
-->
### 1.1.1. 简介
&emsp; **<font color = "clime">数据库事务四大特性中有一个是原子性，原子性底层就是通过undo log实现的。</font>**  
&emsp; **<font color = "clime">Undo log是逻辑日记。undo log主要记录了数据的逻辑变化，比如一条INSERT语句，对应一条DELETE的undo log，对于每个UPDATE语句，对应一条相反的UPDATE的undo log，这样在发生错误时，就能回滚到事务之前的数据状态。</font>**  

&emsp; Undo log作用：

* 回滚数据：当程序发生异常错误时等，根据执行Undo log就可以回滚到事务之前的数据状态，保证原子性，要么成功要么失败。  
* MVCC一致性视图：通过Undo log找到对应的数据版本号，是保证 MVCC 视图的一致性的必要条件。  

### 1.1.2. 写入流程及刷盘时机   
<!-- 
https://www.cnblogs.com/f-ck-need-u/archive/2018/05/08/9010872.html
https://guobinhit.blog.csdn.net/article/details/79345359
-->
&emsp; **<font color = "red">事务开始之前，将当前的版本生成undo log。</font>** 产生undo日志的时候，同样会伴随类似于保护事务持久化机制的redolog的产生。  

### 1.1.3. 对应的物理文件    
&emsp; MySQL5.6之前，undo表空间位于共享表空间的回滚段中，共享表空间的默认的名称是ibdata，位于数据文件目录中。  
&emsp; MySQL5.6之后，undo表空间可以配置成独立的文件，但是提前需要在配置文件中配置，完成数据库初始化后生效且不可改变undo log文件的个数，如果初始化数据库之前没有进行相关配置，那么就无法配置成独立的表空间了。  

&emsp; 关于MySQL5.7之后的独立undo表空间配置参数如下：

    innodb_undo_directory = /data/undospace/ --undo独立表空间的存放目录
    innodb_undo_logs = 128 --回滚段为128KB
    innodb_undo_tablespaces = 4 --指定有4个undo log文件

&emsp; 如果undo使用的共享表空间，这个共享表空间中又不仅仅是存储了undo的信息，共享表空间的默认为与MySQL的数据目录下面，其属性由参数innodb_data_file_path配置。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-81.png)  

<!-- 
&emsp; 默认情况下undo文件是保持在共享表空间的，也即ibdatafile文件中，当数据库中发生一些大的事务性操作的时候，要生成大量的undo信息，全部保存在共享表空间中的。  
&emsp; 因此共享表空间可能会变的很大，默认情况下，也就是undo 日志使用共享表空间的时候，被“撑大”的共享表空间是不会也不能自动收缩的。  
&emsp; 因此，mysql5.7之后的“独立undo 表空间”的配置就显得很有必要了。  
-->


## 1.2. binlog，二进制日志(归档日志)  
### 1.2.1. 简介  
&emsp; binlog用于记录数据库执行的写入性操作(不包括查询)信息，以二进制的形式保存在磁盘中。 **<font color = "red">binlog是mysql的逻辑日志，并且由Server层进行记录，使用任何存储引擎的mysql数据库都会记录binlog日志。</font>**  
&emsp; binlog是通过追加的方式进行写入的，可以通过max_binlog_size参数设置每个binlog文件的大小，当文件大小达到给定值之后，会生成新的文件来保存日志。  

&emsp; **<font color = "red">作用：</font>**  
&emsp; **<font color = "blue">在实际应用中，主要用在两个场景：主从复制和数据恢复</font>**   

* 主从复制：在Master端开启binlog，然后将binlog发送到各个Slave端，Slave端重放binlog从而达到主从数据一致。  
* 数据恢复：通过使用mysqlbinlog工具来恢复数据。  

### 1.2.2. 写入流程及刷盘时机   
&emsp; **写入流程：** **<font color = "clime">SQL修改语句先写Binlog Buffer，事务提交时，按照一定的格式刷到磁盘中。</font>**  
&emsp; **binlog刷盘时机：** **<font color = "clime">对于InnoDB存储引擎而言，mysql通过sync_binlog参数控制biglog的刷盘时机，取值范围是0-N：</font>**  

    0：不去强制要求，由系统自行判断何时写入磁盘；
    1：每次commit的时候都要将binlog写入磁盘；
    N：每N个事务，才会将binlog写入磁盘。

&emsp; 从上面可以看出，sync_binlog最安全的是设置是1，这也是MySQL 5.7.7之后版本的默认值。但是设置一个大一些的值可以提升数据库性能，因此实际情况下也可以将值适当调大，牺牲一定的一致性来获取更好的性能。  

### 1.2.3. binlog日志格式  
&emsp; binlog日志有三种格式，分别为STATMENT、ROW和MIXED。在 MySQL 5.7.7之前，默认的格式是STATEMENT， **MySQL 5.7.7之后，默认值是ROW。** 日志格式通过binlog-format指定。

* STATMENT 基于SQL语句的复制(statement-based replication, SBR)，每一条会修改数据的sql语句会记录到binlog中。优点：不需要记录每一行的变化，减少了binlog日志量，节约了IO， 从而提高了性能；缺点：在某些情况下会导致主从数据不一致，比如执行sysdate()、slepp()等。  
* ROW 基于行的复制(row-based replication, RBR)，不记录每条sql语句的上下文信息，仅需记录哪条数据被修改了。优点：不会出现某些特定情况下的存储过程、或function、或trigger的调用和触发无法被正确复制的问题；缺点：会产生大量的日志，尤其是alter table的时候会让日志暴涨。  
* MIXED 基于STATMENT和ROW两种模式的混合复制(mixed-based replication, MBR)，一般的复制使用STATEMENT模式保存binlog，对于STATEMENT模式无法复制的操作使用ROW模式保存binlog。  

### 1.2.4. 物理文件  
&emsp; 配置文件的路径为log_bin_basename，binlog日志文件按照指定大小，当日志文件达到指定的最大的大小之后，进行滚动更新，生成新的日志文件。  
&emsp; 对于每个binlog日志文件，通过一个统一的index文件来组织。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-83.png)  

