


# Card Table
<!-- 

CSet
https://mp.weixin.qq.com/s/6J5TsbGQy0V_iocz85Pm0g
RSet 
https://mp.weixin.qq.com/s?__biz=MzA5OTY2NzEwOQ==&mid=2247488674&idx=1&sn=d8cdd4188f80e73087ad5ad5610d8588&chksm=90ff8968a788007e38a8cd412591a5ed9ddd345b8be0817db7f235fa3790a0275010b45ca3b3&scene=178&cur_album_id=1818501418614341632#rd
Card Table
https://mp.weixin.qq.com/s?__biz=MzA5OTY2NzEwOQ==&mid=2247488684&idx=1&sn=c2790d3dcb3411b9ae1d7468144e0ece&chksm=90ff8966a78800704e820eea6efe6cd1690d30920f007f45950e1eafc80e45bd019f4a6dc664&scene=178&cur_album_id=1818501418614341632#rd
写屏障 
https://mp.weixin.qq.com/s/8x6-3vNg7MEehHZdspDEfA
-->


## Card Table是什么

&emsp; 在G1 堆中，存在一个CardTable的数据，CardTable 是由元素为1B的数组来实现的，数组里的元素称之为卡片/卡页（Page）。这个CardTable会映射到整个堆的空间，每个卡片会对应堆中的512B空间。  
&emsp; 如下图所示，在一个大小为1 GB的堆下，那么CardTable的长度为2097151 (1GB / 512B)；每个Region 大小为1 MB，每个Region都会对应2048个Card Page。  
![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-163.png)  
&emsp; 那么查找一个对象所在的CardPage只需要简单的计算就可以得出：(对象的地址-堆开始地址)/512    

## Card Table & RSet
&emsp; 介绍完了CardTable，下面说说G1中RSet和CardTable如何配合工作。  
&emsp; 每个区域中都有一个RSet，通过hash表实现，这个hash表的key是引用本区域的其他区域的地址，value是一个数组，数组的元素是引用方的对象所对应的Card Page在Card Table中的下标。  
&emsp; 如下图所示，区域B中的对象b引用了区域A中的对象a，这个引用关系跨了两个区域。b对象所在的CardPage为122，在区域A的RSet中，以区域B的地址作为key，b对象所在CardPage下标为value记录了这个引用关系，这样就完成了这个跨区域引用的记录。  
![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-164.png)  

&emsp; 不过这个CardTable的粒度有点粗，毕竟一个CardPage有512B，在一个CardPage内可能会存在多个对象。所以在扫描标记时，需要扫描RSet中关联的整个CardPage。  

