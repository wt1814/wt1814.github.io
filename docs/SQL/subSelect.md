
<!-- TOC -->

- [1. 分库分表查询](#1-分库分表查询)
    - [1.1. ~~分库分表多维度查询/非拆分键查询~~](#11-分库分表多维度查询非拆分键查询)
    - [1.2. 非partition key的查询问题](#12-非partition-key的查询问题)
    - [1.3. 跨分片的排序order by、分组group by以及聚合count等函数问题](#13-跨分片的排序order-by分组group-by以及聚合count等函数问题)
    - [1.4. 跨分片的排序分页](#14-跨分片的排序分页)
    - [1.5. 跨节点Join的问题](#15-跨节点join的问题)

<!-- /TOC -->

# 1. 分库分表查询

## 1.1. ~~分库分表多维度查询/非拆分键查询~~  
<!-- 
https://blog.csdn.net/coolmsn8786/article/details/100377411

分库分表下非拆分键的查询方案
https://blog.csdn.net/sinat_29774479/article/details/107555322
-->

* 记录两份数据，一份按照用户纬度分表，一份按照视频ID维度分表。按照订单使用者拆分为3个数据库，客户端、商家端、渠道端，目的是分散压力，提高吞吐量，互不影响  
* 以用户维度分库，在单个库里，以其他维度进行分区操作  
* 通过搜索引擎解决，但如果实时性要求很高，又得关系到实时搜索。

**<font color = "clime">1. 方案一：以第一维度进行拆分，第二维度拼接第一维度。2. 方案二：数据冗余。</font>**

&emsp; 以电商模型订单库为目标问题。  
1. 以用户id为分片键，订单号尾部拼接用户id后四位。根据后四位取模分片。  
2. 订单号维度查询，订单号后四位定位库。用户维度查询，支持分页。  
3. 如果涉及到其他维度，比如订单里面有商品，商家需要根据商品维度查询排序分页，可以双写一份商家备份库，因为商家查询频率不会和c端用户一样高，且对实时性要求️有容忍性，所以可以用biolog同步双写数据。  
4. 如果还有其他一些查询频率更低，且实时性无要求，也通过biolog同步一份数据到kudu数据仓库，利用大数据olap等技术做查询操作。  




## 1.2. 非partition key的查询问题  
&emsp; 水平分库分表，基于拆分策略为常用的hash法。  
1. **端上除了partition key只有一个非partition key作为条件查询**  
    * 映射法  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-19.png)  
    * 基因法  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-20.png)  
    &emsp; 注：写入时，基因法生成user_id，如图。关于xbit基因，例如要分8张表，23=8，故x取3，即3bit基因。根据user_id查询时可直接取模路由到对应的分库或分表。根据user_name查询时，先通过user_name_code生成函数生成user_name_code再对其取模路由到对应的分库或分表。id生成常用snowflake算法。  
2. **端上除了partition key不止一个非partition key作为条件查询**  
    * 映射法  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-21.png)  
    * 冗余法    
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-22.png)  
    &emsp; 注：按照order_id或buyer_id查询时路由到db_o_buyer库中，按照seller_id查询时路由到db_o_seller库中。感觉有点本末倒置！有其他好的办法吗？改变技术栈呢？  
3. **后台除了partition key还有各种非partition key组合条件查询**  
    * NoSQL法  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-23.png)  
    * 冗余法  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-24.png)  

## 1.3. 跨分片的排序order by、分组group by以及聚合count等函数问题  
&emsp; 这些是一类问题，因为它们<font color = "red">都需要基于全部数据集合进行计算。多数的代理都不会自动处理合并工作，部分支持聚合函数MAX、MIN、COUNT、SUM。</font>  
&emsp; **<font color = "red">解决方案：分别在各个节点上执行相应的函数处理得到结果后，在应用程序端进行合并。</font>** 每个结点的查询可以并行执行，因此很多时候它的速度要比单一大表快很多。但如果结果集很大，对应用程序内存的消耗是一个问题。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-16.png)  

## 1.4. 跨分片的排序分页  
&emsp; <font color = "red">一般来讲，分页时需要按照指定字段进行排序。当排序字段是分片字段时，通过分片规则可以比较容易定位到指定的分片；而当排序字段非分片字段时，情况就会变得比较复杂了。</font>为了最终结果的准确性，需要在不同的分片节点中将数据进行排序并返回，并将不同分片返回的结果集进行汇总和再次排序，最后再返回给用户。如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-17.png)  
&emsp; 上面图中所描述的只是最简单的一种情况(取第一页数据)，看起来对性能的影响并不大。但是，如果想取出第10页数据，情况又将变得复杂很多，如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-18.png)  
&emsp; 有些读者可能并不太理解，为什么不能像获取第一页数据那样简单处理(排序取出前10条再合并、排序)。其实并不难理解，因为各分片节点中的数据可能是随机的，为了排序的准确性，必须把所有分片节点的前N页数据都排序好后做合并，最后再进行整体的排序。很显然，这样的操作是比较消耗资源的，用户越往后翻页，系统性能将会越差。 

----

## 1.5. 跨节点Join的问题  
&emsp; 在单库单表的情况下，联合查询是非常容易的。但是，随着分库与分表的演变，联合查询就遇到跨库关联和跨表关系问题。在设计之初就应该尽量避免联合查询。  
&emsp; tddl、MyCAT等都支持跨分片join。如果中间不支持，跨库Join的几种解决思路：  

* 在程序中进行拼装。  
* 全局表：  
&emsp; 所谓全局表，就是有可能系统中所有模块都可能会依赖到的一些表。比较类似“数据字典”。为了避免跨库join查询，可以将这类表在其他每个数据库中均保存一份。同时，这类数据通常也很少发生修改(甚至几乎不会)，所以也不用太担心“一致性”问题。  
* 字段冗余：  
&emsp; 这是一种典型的反范式设计，在互联网行业中比较常见，通常是为了性能来避免join查询。  
&emsp; 举个电商业务中很简单的场景：“订单表”中保存“卖家 Id”的同时，将卖家的“Name”字段也冗余，这样查询订单详情的时候就不需要再去查询“卖家用户表”。  
&emsp; 字段冗余能带来便利，是一种“空间换时间”的体现。但其适用场景也比较有限，比较适合依赖字段较少的情况。最复杂的还是数据一致性问题，这点很难保证，可以借助数据库中的触发器或者在业务代码层面去保证。当然，也需要结合实际业务场景来看一致性的要求。就像上面例子，如果卖家修改了Name之后，是否需要在订单信息中同步更新呢？  

