

<!-- TOC -->

- [1. undo log，回滚日志](#1-undo-log回滚日志)
    - [1.1. 简介](#11-简介)
    - [1.2. 写入流程及刷盘时机](#12-写入流程及刷盘时机)
    - [1.3. 对应的物理文件](#13-对应的物理文件)

<!-- /TOC -->


**<font color = "red">总结：</font>**  
1. **<font color = "clime">Undo log，回滚日志，是逻辑日记。undo log解决了事务原子性。</font>**    
2. undo log主要记录了数据的逻辑变化，比如一条INSERT语句，对应一条DELETE的undo log，对于每个UPDATE语句，对应一条相反的UPDATE的undo log，这样在发生错误时，就能回滚到事务之前的数据状态。
3. 事务开始之前，将当前的版本生成undo log。

# 1. undo log，回滚日志
<!-- 
* undo log(回滚日志)  实现原子性  
&emsp; undo log 主要为事务的回滚服务。在事务执行的过程中，除了记录redo log，还会记录一定量的undo log。<font color = "red">undo log记录了数据在每个操作前的状态，如果事务执行过程中需要回滚，就可以根据undo log进行回滚操作。</font>单个事务的回滚，只会回滚当前事务做的操作，并不会影响到其他的事务做的操作。  
&emsp; Undo记录的是已部分完成并且写入硬盘的未完成的事务，默认情况下回滚日志是记录下表空间中的(共享表空间或者独享表空间)  

&emsp; 二种日志均可以视为一种恢复操作，redo_log是恢复提交事务修改的页操作，而undo_log是回滚行记录到特定版本。二者记录的内容也不同，redo_log是物理日志，记录页的物理修改操作，而undo_log是逻辑日志，根据每行记录进行记录。  
-->
## 1.1. 简介
&emsp; **<font color = "clime">数据库事务四大特性中有一个是原子性，原子性底层就是通过undo log实现的。</font>**  
&emsp; **<font color = "clime">Undo log是逻辑日记。undo log主要记录了数据的逻辑变化，比如一条INSERT语句，对应一条DELETE的undo log，对于每个UPDATE语句，对应一条相反的UPDATE的undo log，这样在发生错误时，就能回滚到事务之前的数据状态。</font>**  

&emsp; Undo log作用：

* 回滚数据：当程序发生异常错误时等，根据执行Undo log就可以回滚到事务之前的数据状态，保证原子性，要么成功要么失败。  
* MVCC一致性视图：通过Undo log找到对应的数据版本号，是保证 MVCC 视图的一致性的必要条件。  

## 1.2. 写入流程及刷盘时机   
<!-- 
https://www.cnblogs.com/f-ck-need-u/archive/2018/05/08/9010872.html
https://guobinhit.blog.csdn.net/article/details/79345359
-->
&emsp; **<font color = "red">事务开始之前，将当前的版本生成undo log。</font>** 产生undo日志的时候，同样会伴随类似于保护事务持久化机制的redolog的产生。  

## 1.3. 对应的物理文件    
&emsp; MySQL5.6之前，undo表空间位于共享表空间的回滚段中，共享表空间的默认的名称是ibdata，位于数据文件目录中。  
&emsp; MySQL5.6之后，undo表空间可以配置成独立的文件，但是提前需要在配置文件中配置，完成数据库初始化后生效且不可改变undo log文件的个数，如果初始化数据库之前没有进行相关配置，那么就无法配置成独立的表空间了。  

&emsp; 关于MySQL5.7之后的独立undo表空间配置参数如下：

    innodb_undo_directory = /data/undospace/ --undo独立表空间的存放目录
    innodb_undo_logs = 128 --回滚段为128KB
    innodb_undo_tablespaces = 4 --指定有4个undo log文件

&emsp; 如果undo使用的共享表空间，这个共享表空间中又不仅仅是存储了undo的信息，共享表空间的默认为与MySQL的数据目录下面，其属性由参数innodb_data_file_path配置。  

![image](http://www.wt1814.com/static/view/images/SQL/sql-81.png)  

<!-- 
&emsp; 默认情况下undo文件是保持在共享表空间的，也即ibdatafile文件中，当数据库中发生一些大的事务性操作的时候，要生成大量的undo信息，全部保存在共享表空间中的。  
&emsp; 因此共享表空间可能会变的很大，默认情况下，也就是undo 日志使用共享表空间的时候，被“撑大”的共享表空间是不会也不能自动收缩的。  
&emsp; 因此，mysql5.7之后的“独立undo 表空间”的配置就显得很有必要了。  
-->
