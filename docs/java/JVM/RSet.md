
<!-- TOC -->

- [1. Card Table](#1-card-table)
    - [1.3. 跨代引用假说，记忆集和卡表](#13-跨代引用假说记忆集和卡表)
    - [1.1. Card Table是什么](#11-card-table是什么)
    - [1.2. Card Table & RSet](#12-card-table--rset)

<!-- /TOC -->

# 1. Card Table
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
## 1.3. 跨代引用假说，记忆集和卡表
<!-- 
https://mp.weixin.qq.com/s/AdF--fvDq63z0Inr2-JSHw
https://mp.weixin.qq.com/s/3YHHtuPENiV_2ZXfHHuD4A
-->
&emsp; **跨代引用假说：**  
&emsp; 分代收集并非只是简单划分一下内存区域，它至少存在一个明显的困难：对象之间不是孤立的，对象之间会存在跨代引用。假如现在要进行只局限于新生代的垃圾收集，根据根可达性分析的知识，与GC Roots之间不存在引用链即为可回收，但新生代的对象很有可能会被老年代所引用，那么老年代对象将临时加入 GC Roots 集合中，不得不再额外遍历整个老年代中的所有对象来确保可达性分析结果的正确性，这无疑为内存回收带来很大的性能负担。为了解决这个问题，就需要对分代收集理论添加一条经验法则： **<font color = "clime">跨代引用假说（跨代引用相对于同代引用仅占少数）。</font>**  


&emsp; **记忆集、卡表：**  
&emsp; 存在互相引用的两个对象，应该是倾向于同时生存或同时消亡的，举个例子，如果某个新生代对象存在跨代引用，由于老年代对象难以消亡，会使得新生代对象同样在收集时得以存活，进而年龄增长后晋升到老年代，那么跨代引用也随之消除了。 **<font color = "clime">既然跨代引用只是少数，那么就没必要去扫描整个老年代，也不必专门记录每一个对象是否存在哪些跨代引用，只需在新生代上建立一个全局的数据结构，称为记忆集(Remembered Set)，这个结构把老年代划分为若干个小块，标识出老年代的哪一块内存会存在跨代引用。此后当发生Minor GC时，只有包含了跨代引用的小块内存里的对象才会被加入GC Roots进行扫描。</font>**  
&emsp; ~~跨代引用假说的具体解决办法是：在新生代上建立一个全局的数据结构(该结构被称为“记忆集”，Remembered Set)，这个结构把老年代划分成若干小块，标识出老年代的哪一块内存会存在跨代引用。此后当发生Minor GC时，只有包含了跨代引用的小块内存里的对象才会被加入到GC Roots进行扫描。~~  


&emsp; **部分垃圾回收器使用的模型：**  

* 除Epsilon ZGC Shenandoah之外的GC都是使用逻辑分代模型  
* G1是逻辑分代，物理不分代
* 除此之外不仅逻辑分代，而且物理分代  


------------
&emsp; 跨代引用假说的具体解决办法是：在新生代上建立一个全局的数据结构(该结构被称为“记忆集”，Remembered Set)，这个结构把老年代划分成若干小块，标识出老年代的哪一块内存会存在跨代引用。此后当发生Minor GC时，只有包含了跨代引用的小块内存里的对象才会被加入到GC Roots进行扫描。 

## 1.1. Card Table是什么

&emsp; 在G1 堆中，存在一个CardTable的数据，CardTable 是由元素为1B的数组来实现的，数组里的元素称之为卡片/卡页（Page）。这个CardTable会映射到整个堆的空间，每个卡片会对应堆中的512B空间。  
&emsp; 如下图所示，在一个大小为1 GB的堆下，那么CardTable的长度为2097151 (1GB / 512B)；每个Region 大小为1 MB，每个Region都会对应2048个Card Page。  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-163.png)  
&emsp; 那么查找一个对象所在的CardPage只需要简单的计算就可以得出：(对象的地址-堆开始地址)/512    

## 1.2. Card Table & RSet
&emsp; 介绍完了CardTable，下面说说G1中RSet和CardTable如何配合工作。  
&emsp; 每个区域中都有一个RSet，通过hash表实现，这个hash表的key是引用本区域的其他区域的地址，value是一个数组，数组的元素是引用方的对象所对应的Card Page在Card Table中的下标。  
&emsp; 如下图所示，区域B中的对象b引用了区域A中的对象a，这个引用关系跨了两个区域。b对象所在的CardPage为122，在区域A的RSet中，以区域B的地址作为key，b对象所在CardPage下标为value记录了这个引用关系，这样就完成了这个跨区域引用的记录。  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-164.png)  

&emsp; 不过这个CardTable的粒度有点粗，毕竟一个CardPage有512B，在一个CardPage内可能会存在多个对象。所以在扫描标记时，需要扫描RSet中关联的整个CardPage。  

