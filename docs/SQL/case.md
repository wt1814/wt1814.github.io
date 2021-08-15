

# Sql优化的一些案例  
<!-- 
**limit，子查询优化
一次 SQL 查询优化原理分析(900W+ 数据，从 17s 到 300ms)
https://mp.weixin.qq.com/s/7CAuMI7mzFxENfRCM4N65g



强制使用索引失败，要用analyze tables，看它有没有再重新组队。  
-->

## 子查询优化
<!--

子查询优化
一次非常有意思的 SQL 优化经历：从 30248.271s 到 0.001s 
https://mp.weixin.qq.com/s/rjkvvd2ia1A_Ix6rHYTgpw

-->

## 优化器选错索引  
<!--

MySQL 索引优化实例
https://mp.weixin.qq.com/s/dUgonoftcSGjlWgYpoTEUw
-->


## 四表关联分页、模糊查询优化：  
&emsp; 分页查询总数和数据两条sql，瓶颈在count(0)上。   
&emsp; 索引比较全，已无可优化空间。将某一张表的数据冗余到主表；添加一些默认查询条件，按照条件过滤。    


