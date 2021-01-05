&emsp; 平时开发工作比较忙，虽然自己有学习，自己学的很菜，但是运营公众号太菜。平时公众号发一些学习过程中的查的资料或者别人的精美文章，就是没自己的好文章。   
&emsp; 今天说说学习sql的一些感悟，一方面是自己的一个小总结，另一方面各位老铁是否能说明白其中，反正我是说不清。  

1. InnoDB索引底层：
    1. InnoDB逻辑结构页介绍  
    2. 为什么-innodb会使用b树
    3. innodb索引btree实现过程
    4. innodb索引类型（聚簇、非聚簇）  
    5. 为什么官方建议InnoDB使用自增长主键作为索引？（最重要的碎片问题）

2. MySql原理（一条SQL执行，主要是执行过程和事务的执行）  
    1. SQL写  
        1. 插入缓存
        2. 二次写
    2. 事务  
        1. 锁
        2. MVCC
        3. redo log和undo log
            1. MySQL系统还有binlog和慢查询日志
            2. 两阶段提交

<!-- 
表中数据很大：
    1. 修改表结构
    2. 删除数据

MySQL 在并发场景下会遇到的问题及解决方案～ 
https://mp.weixin.qq.com/s/tFiTv7a8VFvU-paghtGkcw
-->

<!-- 
Prometheus + Granafa 构建高大上的MySQL监控平台 
https://mp.weixin.qq.com/s/S8-_QGrKYn5rMD645BKwjQ

-->