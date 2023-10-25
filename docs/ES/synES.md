
<!-- TOC -->

- [1. 同步数据到ES](#1-同步数据到es)
    - [1.1. MySQL数据同步到ES的4种解决方案](#11-mysql数据同步到es的4种解决方案)
        - [1.1.1. 同步双写](#111-同步双写)
        - [1.1.2. 异步双写](#112-异步双写)
        - [1.1.3. 定时任务](#113-定时任务)
        - [1.1.4. 数据订阅](#114-数据订阅)
    - [1.2. 数据一致性](#12-数据一致性)

<!-- /TOC -->

# 1. 同步数据到ES  
<!-- 
logstash同步mysql数据到Elasticsearch实战,主要实现删除
https://blog.csdn.net/Giggle1994/article/details/111194763
-->
<!-- 

*** https://www.cnblogs.com/zeenzhou/p/12125634.html

https://blog.csdn.net/qq_39893313/article/details/123688809
https://www.cnblogs.com/zxy-come-on/p/15196602.html
https://www.zhihu.com/question/482345425/answer/2168849961
-->

<!-- 

canal
https://www.jianshu.com/p/9677ca6ca34e
使用canal增量同步mysql数据库信息到ElasticSearch
https://www.cnblogs.com/dalaoyang/p/11069850.html
canal adapter没有同步成功无异常
https://blog.csdn.net/lizz861109/article/details/113183990
CanalAdapter1.1.5版本问题收集
https://blog.csdn.net/lizz861109/article/details/112682680
Elasticsearch 如何实现相似推荐功能？
https://mp.weixin.qq.com/s/apPGngRQx6bJEmR82XlXhQ
-->

<!-- 

MySQL数据同步到ES的4种解决方案
https://baijiahao.baidu.com/s?id=1761412728761809761&wfr=spider&for=pc

https://blog.csdn.net/weixin_43735348/article/details/127580815

-->

## 1.1. MySQL数据同步到ES的4种解决方案
### 1.1.1. 同步双写


### 1.1.2. 异步双写
异步双写是指在主库上进行数据修改操作时，异步将数据写入到备库中。这种方式可以降低主库的写入延迟，并且备库出现问题时不会影响主库的性能，但是可能会存在主备数据不一致的情况。  

### 1.1.3. 定时任务
定时任务是指在固定的时间点或时间间隔内将主库中的数据同步到备库中。这种方式可以避免主库的写入延迟，同时保证备库中的数据与主库中的数据一致，但是可能会存在备库中数据的滞后问题。  


### 1.1.4. 数据订阅
数据订阅是指使用MySQL的复制功能，将主库的数据实时复制到备库中。这种方式可以保证备库中的数据与主库中的数据实时一致，但是会增加主库的读取压力，并且可能存在网络延迟等问题。  


## 1.2. 数据一致性  

ES增量同步Oracle数据是一种常见的数据同步方案，可以有效保证数据的一致性。在实现过程中，可以使用Logstash作为数据收集引擎，通过JDBC方式连接Oracle数据库，将数据同步到ES中。  

<!-- 

https://www.xjx100.cn/news/488897.html?action=onClick
-->

1. 根据更新时间，增量更新
2. 隔天全量更新 

