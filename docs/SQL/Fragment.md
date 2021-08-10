



# 碎片问题
&emsp; 碎片化问题，需要掌握一定的[索引底层原理](/docs/SQL/IndexPrinciple.md)。  

1. 碎片问题的产生：  
    * 页分裂产生碎片。  
    * 删除数据产生碎片：每当MySQL从列表中删除了一行内容，该段空间就会被留空。而在一段时间内的大量删除操作，会使这种留空的空间变得比存储列表内容所使用的空间更大。当MySQL对数据进行扫描时，它扫描的对象实际是列表的容量需求上限，也就是数据被写入的区域中处于峰值位置的部分。<font color = "red">如果进行新的插入操作，MySQL将尝试利用这些留空的区域，但仍然无法将其彻底占用。</font>  
2. 碎片优化  
&emsp; 这种额外的破碎的存储空间在读取效率方面比正常占用的空间要低得多。  
&emsp; 对MySQL进行碎片整理的方法非常简单，因为MySQL已经给提供了对应的SQL指令，这个SQL指令就是OPTIMIZE TABLE，其完整语法：`OPTIMIZE [LOCAL | NO_WRITE_TO_BINLOG] TABLE table_name1 [, table_name2] ...`  
&emsp; 从上面的语法描述中，可以得知，OPTIMIZE TABLE可以一次性对多个表进行碎片整理，只需要在OPTIMIZE TABLE后面接多个表名，并以英文逗号隔开即可。  
&emsp; 此外，OPTIMIZE TABLE语句有两个可选的关键字：LOCAL和NO_WRITE_TO_BINLOG。在默认情况下，OPTIMIZE TABLE语句将会被记录到二进制日志中，如果指定了LOCAL或NO_WRITE_TO_BINLOG关键字，则不会记录。当然，一般情况下，也无需关注这两个关键字。 