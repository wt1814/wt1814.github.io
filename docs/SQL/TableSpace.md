<!-- TOC -->

- [1. 表空间](#1-表空间)
    - [1.1. 逻辑存储结构](#11-逻辑存储结构)
        - [1.1.1. 表空间详解](#111-表空间详解)
        - [1.1.2. InnoDB数据页结构](#112-innodb数据页结构)
            - [1.1.2.1. InnoDB行格式](#1121-innodb行格式)
                - [1.1.2.1.1. Compact行记录格式](#11211-compact行记录格式)
                - [1.1.2.1.2. 行溢出数据](#11212-行溢出数据)
    - [1.2. InnoDB表数据文件](#12-innodb表数据文件)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; 从InnoDb存储引擎的逻辑存储结构看，所有数据都被逻辑地存放在一个空间中，称之为表空间(tablespace)。表空间又由段(segment)，区(extent)，页(page)组成。  
&emsp; **<font color = "clime">相比较之下，使用独占表空间的效率以及性能会更高一点。</font>**  
&emsp; **<font color = "clime">在InnoDB存储引擎中，默认每个页的大小为16KB(在操作系统中默认页大小是4KB)。</font>**  


# 1. 表空间  
<!--
innodb使用手册
https://zhuanlan.zhihu.com/p/111958646
-->


## 1.1. 逻辑存储结构  
&emsp; 从InnoDb存储引擎的逻辑存储结构看，所有数据都被逻辑地存放在一个空间中，称之为表空间(tablespace)。表空间又由段(segment)，区(extent)，页(page)组成。页在一些文档中有时候也称为块(block)。InnoDb逻辑存储结构图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-41.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-134.png)  

* 段(segment)：表空间由段组成，常见的段有数据段、索引段、回滚段等。  
&emsp; InnoDB存储引擎表是索引组织的，因此数据即索引，索引即数据。数据段即为B+树的叶子结点，索引段即为B+树的非索引结点。  
&emsp; 一个段包含256个区(256M大小)。  
* 区(extent)：区是由连续页组成的空间，在任何情况下每个区的大小都为1MB。  
&emsp; 为了保证区中页的连续性，InnoDB存储引擎一次从磁盘申请4~5个区。  
&emsp; 默认情况下，InnoDB存储引擎页的大小为16KB，一个区中一共64个连续的区。  
* 页(page)：页是InnoDB磁盘管理的最小单位。在InnoDB存储引擎中，默认每个页的大小为16KB。从InnoDB1.2.x版本开始，可以通过参数innodbpagesize将页的大小设置为4K，8K，16K。  
&emsp; **InnoDB存储引擎中，常见的页类型有：数据页，undo页，系统页，事务数据页，插入缓冲位图页，插入缓冲空闲列表页等。**  

### 1.1.1. 表空间详解  
&emsp; Innodb中表空间可以分为以下几种：  

* 系统表空间。系统表空间又包括了InnoDB数据字典，双写缓冲区(Doublewrite Buffer)，修改缓存(Change Buffer)，Undo日志等。  
* 独立表空间  
* undo表空间  
* 临时表空间(temporary tablespace)  
* 通用表空间(general tablespace)。通用表空间是InnoDB 使用CREATE TABLESPACE语法创建的共享表空间。    

&emsp; **共享表空间与独立表空间：**  
&emsp; 在默认情况下innodb存储引擎有一个共享表空间ibdata*，即所有的数据都存放在这个表空间中。如果启用了innodb_file_per_table参数，则每张表的数据可以存放到独立的.ibd文件的独立表空间。  
&emsp; 共享表空间以及独立表空间都是针对数据的存储方式而言的。

* 共享表空间：某一个数据库的所有的表数据，索引文件全部放在一个文件中，默认这个共享表空间的文件路径在data目录下。 默认的文件名为:ibdata1 初始化为10M。  
* 独立表空间：每一个表都将会生成以独立的文件方式来进行存储，每一个表都有一个.frm表描述文件，还有一个.ibd文件。 其中这个文件包括了单独一个表的数据内容以及索引内容，默认情况下它的存储位置也是在表的位置之中。  

&emsp; **共享表空间与独立表空间的优缺点：**    
* 共享表空间：
    * 优点：可以将表空间分成多个文件存放到各个磁盘上(表空间文件大小不受表大小的限制，如一个表可以分布在不同的文件上)。数据和文件放在一起方便管理。
    * 缺点：所有的数据和索引存放到一个文件中，虽然可以把一个大文件分成多个小文件，但是多个表及索引在表空间中混合存储，这样对于一个表做了大量删除操作后表空间中将会有大量的空隙，特别是对于统计分析，日值系统这类应用最不适合用共享表空间。

* 独立表空间：
    * 优点：  
        * 每个表都有自已独立的表空间。
        * 每个表的数据和索引都会存在自已的表空间中。
        * 可以实现单表在不同的数据库中移动。
        * 空间可以回收(除drop table操作处，表空不能自已回收)

            &emsp; Drop table操作自动回收表空间，如果对于统计分析或是日值表，删除大量数据后可以通过:alter table TableName engine=innodb;回缩不用的空间。
            对于使innodb-plugin的Innodb使用turncate table也会使空间收缩。
            对于使用独立表空间的表，不管怎么删除，表空间的碎片不会太严重的影响性能，而且还有机会处理。
    * 缺点：单表增加过大，如超过100个G。

&emsp; **<font color = "clime">相比较之下，使用独占表空间的效率以及性能会更高一点。</font>**  

### 1.1.2. InnoDB数据页结构  
<!-- 
https://zhuanlan.zhihu.com/p/111958646
-->
&emsp; 页是InnoDB磁盘管理的最小单位，每次读取数据都会读取一个页大小的数据。 **<font color = "clime">在InnoDB存储引擎中，默认每个页的大小为16KB(在操作系统中默认页大小是4KB)。</font>** 可以使用命令SHOW GLOBAL STATUS LIKE 'Innodb_page_size' 查看。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-99.png)  

&emsp; 页结构如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-135.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-136.png)  

#### 1.1.2.1. InnoDB行格式
<!-- 
https://zhuanlan.zhihu.com/p/111958646
https://juejin.cn/post/6844904190477598733#heading-14
-->
&emsp; Innodb中数据的实际都是按照行的格式存储的，每个页能存放的行的数量也是有严格的规定，最多可以存放16K / 2 - 200 即7992行。  
&emsp; mysql支持4种不同类型的行格式：Compact(5.0中引入)、Redundant(比较老)、Dynamic、Compressed。    
<!-- 
先说一个结论：页中放的行越多，innodb性能越高。所以在mysql 5.0中引入了compact行记录格式。  
-->
##### 1.1.2.1.1. Compact行记录格式    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-137.png)  

* 变长字段长度列表：变长列中存储多少字节数据是不固定的，所以在存储数据时候也需要把这些数据占用的字节数存储起来。varchar(M) M代表的是存储多少字符(mysql5.0.3之前是字节，之后是字符)。  
* NULL标志位：bit向量标识的null列  
* 记录头信息：固定占用5个字节   
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-138.png)  
* **<font color = "red">隐藏列：</font>** 每行数据除了用户定义的列之外还有可能三种隐藏列  
* **<font color = "red">事务id列：</font>** 占用6个字节，标识当前列的事务id  
* **<font color = "red">回滚指针列：</font>** 占用7个字节行id：如果该表没有指定主键的话，会有占用6字节的
* **<font color = "red">行id列</font>**  

&emsp; 综上，无论是char类型还是varchar类型，null值都不占用任何存储空间。  

##### 1.1.2.1.2. 行溢出数据  
&emsp; 一个页中至少要放两行数据，否则B+树就成了链表，所以如果行数据超出一定长度，innodb就会将行数据放到溢出页中。  

&emsp; **compact和redundant**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-139.png)  
&emsp; 如果列数据大于768字节，数据页只保存前768字节数据前缀，剩余数据保存于溢出页。  
&emsp; **dynamic和compressed**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-140.png)  
&emsp; 数据页中只存放20字节的Off Page指针，实际数据都在溢出页。  

## 1.2. InnoDB表数据文件  
&emsp; <font color = "red">在InnoDB中，数据和索引文件是合起来储存的，如图所示，InnoDB 的存储文件有两个，后缀名分别是 .frm 和 .idb，其中 .frm 是表的定义文件，</font> **<font color = "lime">而idb是数据文件/索引文件。</font>**   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SQL/sql-33.png)  

* .frm文件：与表相关的元数据信息都存放在frm文件，包括表结构的定义信息等。  
* .ibd文件或.ibdata文件：这两种文件都是存放InnoDB数据的文件，之所以有两种文件形式存放 InnoDB 的数据，是因为InnoDB的数据存储方式能够通过配置来决定是使用共享表空间存放存储数据，还是用独享表空间存放存储数据。  


&emsp; 独享表空间存储方式使用.ibd文件，并且每个表一个.ibd文件 共享表空间存储方式使用.ibdata文件，所有表共同使用一个.ibdata文件(或多个，可自己配置)。  