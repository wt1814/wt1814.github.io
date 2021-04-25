
<!-- TOC -->

- [1. Dictht](#1-dictht)
    - [1.1. Dictht数据结构](#11-dictht数据结构)
    - [1.2. rehash与渐进式rehash](#12-rehash与渐进式rehash)
        - [1.2.1. rehash](#121-rehash)
        - [1.2.2. 渐进式rehash](#122-渐进式rehash)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "red">rehash：</font>**  
&emsp; dictEntry有有ht[0]和ht[1]两个对象。  
&emsp; 扩展操作：ht[1]扩展的大小是比当前 ht[0].used 值的二倍大的第一个2的整数幂；收缩操作：ht[0].used 的第一个大于等于的 2 的整数幂。  
&emsp; **<font color = "clime">当ht[0]上的所有的键值对都rehash到ht[1]中，会重新计算所有的数组下标值，当数据迁移完后，ht[0]就会被释放，然后将ht[1]改为ht[0]，并新创建ht[1]，为下一次的扩展和收缩做准备。</font>**  
2. **<font color = "red">渐进式rehash：</font>**  
&emsp; **<font color = "clime">Redis将所有的rehash的操作分成多步进行，直到都rehash完成。</font>**  
&emsp; **<font color = "red">在渐进式rehash的过程「更新、删除、查询会在ht[0]和ht[1]中都进行」，比如更新一个值先更新ht[0]，然后再更新ht[1]。</font>**   
&emsp; **<font color = "clime">而新增操作直接就新增到ht[1]表中，ht[0]不会新增任何的数据，</font><font color = "red">这样保证「ht[0]只减不增，直到最后的某一个时刻变成空表」，这样rehash操作完成。</font>**  

# 1. Dictht
<!-- 
Redis 字典
https://mp.weixin.qq.com/s/DG3fOoNf-Avuud2cwa3N5A
-->
## 1.1. Dictht数据结构
&emsp; 字典类型的底层是hashtable实现的，明白了字典的底层实现原理也就是明白了hashtable的实现原理，hashtable的实现原理可以与HashMap的是底层原理相类比。它是一个数组+链表的结构。Redis Hash使用MurmurHash2算法来计算键的哈希值，并且使用链地址法来解决键冲突，进行了一些rehash优化等。  
&emsp; dictEntry与HashMap两者在新增时都会通过key计算出数组下标，不同的是计算法方式不同，HashMap中是以hash函数的方式，而hashtable中计算出hash值后，还要通过sizemask属性和哈希值再次得到数组下标。结构如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-81.png)  

## 1.2. rehash与渐进式rehash
### 1.2.1. rehash  
&emsp; 在字典的底层实现中，value对象以每一个dictEntry的对象进行存储，当hash表中的存放的键值对不断的增加或者减少时，需要对hash表进行一个扩展或者收缩。  
&emsp; 这里就会和HashMap一样，也会就进行rehash操作，进行重新散列排布。 **<font color = "red">从上图中可以看到有ht[0]和ht[1]两个对象，</font>** 先来看看对象中的属性是干嘛用的。    
&emsp; 在hash表结构定义中有四个属性分别是table、unsigned long size、unsigned long sizemask、unsigned long used，分别表示的含义就是「哈希表数组、hash表大小、用于计算索引值(总是等于size-1)、hash表中已有的节点数」。  
&emsp; ht[0]是用来最开始存储数据的，当要进行扩展或者收缩时，ht[0]的大小就决定了ht[1]的大小，ht[0]中的所有的键值对就会重新散列到ht[1]中。  
&emsp; 扩展操作：ht[1]扩展的大小是比当前ht[0].used值的二倍大的第一个2的整数幂；收缩操作：ht[0].used 的第一个大于等于的2的整数幂。  
&emsp; **<font color = "clime">当ht[0]上的所有的键值对都rehash到ht[1]中，会重新计算所有的数组下标值，当数据迁移完后，ht[0]就会被释放，然后将ht[1]改为ht[0]，并新创建ht[1]，为下一次的扩展和收缩做准备。</font>**  

### 1.2.2. 渐进式rehash  
&emsp; 假如在rehash的过程中数据量非常大，Redis不是一次性把全部数据rehash成功，这样会导致Redis对外服务停止，Redis内部为了处理这种情况采用「渐进式的rehash」。  
&emsp; **<font color = "clime">Redis将所有的rehash的操作分成多步进行，直到都rehash完成，</font>** 具体的实现与对象中的rehashindex属性相关，「若是rehashindex 表示为-1表示没有rehash操作」。  
&emsp; 当rehash操作开始时会将该值改成0， **<font color = "red">在渐进式rehash的过程「更新、删除、查询会在ht[0]和ht[1]中都进行」，比如更新一个值先更新ht[0]，然后再更新ht[1]。</font>**  
&emsp; **<font color = "clime">而新增操作直接就新增到ht[1]表中，ht[0]不会新增任何的数据，</font><font color = "red">这样保证「ht[0]只减不增，直到最后的某一个时刻变成空表」，这样rehash操作完成。</font>**  
