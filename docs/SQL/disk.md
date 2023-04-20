

&emsp; 内存数据落盘整体思路分析：  
![image](http://182.92.69.8:8081/img/SQL/sql-173.png)  
&emsp; `InnoDB内存缓冲池中的数据page要完成持久化的话，是通过两个流程来完成的，一个是脏页（数据）落盘；一个是预写redo log日志。（对应Mysql的两个内存区域：buffer pool和redo log buffer。）`  



&emsp; 官方文档：https://dev.mysql.com/doc/refman/5.7/en/innodb-on-disk-structures.html  
![image](http://182.92.69.8:8081/img/SQL/sql-133.png)  

&emsp; [表空间](/docs/SQL/TableSpace.md)  
&emsp; [MySql事务日志](/docs/SQL/log.md)  
&emsp; &emsp; [redoLog](/docs/SQL/redoLog.md)  
&emsp; &emsp; [binLog使用](/docs/SQL/Binlog.md)  
&emsp; [崩溃恢复](/docs/SQL/CrashRecovery.md)  
&emsp; [Double Write](/docs/SQL/DoubleWrite.md)  


