


# 数据类型

## 1.4. 数据类型
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-106.png)  


### 1.4.1. String内部编码  
<!-- 
Redis 字符串
https://mp.weixin.qq.com/s/8Aw-A-8FdZeXBY6hQlhYUw
-->
&emsp; **<font color = "red">字符串类型的内部编码有三种：</font>**  

*  int，存储8个字节的长整型(long，2^63-1)。   
*  embstr，代表 embstr 格式的 SDS(Simple Dynamic String 简单动态字符串)， **<font color = "clime">存储小于44个字节的字符串。</font>**   
*  **<font color = "clime">raw，存储大于 44 个字节的字符串(3.2 版本之前是 39 字节)。</font>**     

&emsp; <font color = "red">Redis会根据当前值的类型和长度决定使用哪种内部编码实现。</font>  

1. embstr和raw的区别？  
&emsp; embstr的使用只分配一次内存空间（因为RedisObject和SDS是连续的），而raw需要分配两次内存空间（分别为RedisObject和SDS分配空间）。因此与raw相比，<font color = "red">embstr的好处在于创建时少分配一次空间，删除时少释放一次空间，以及对象的所有数据连在一起，寻找方便。而embstr的坏处也很明显，如果字符串的长度增加需要重新分配内存时，整个RedisObject和SDS都需要重新分配空间，</font>因此Redis中的embstr实现为只读。  
2. int和embstr什么时候转化为raw?  
&emsp; **<font color = "clime">当int数据不再是整数，或大小超过了long的范围(2^63-1=9223372036854775807)时，自动转化为embstr。</font>**  
3. embstr没有超过阈值，为什么变成raw了？  
&emsp; 对于embstr，由于其实现是只读的，因此在对embstr对象进行修改时，都会先转化为raw再进行修改。因此，只要是修改embstr对象，修改后的对象一定是raw的，无论是否达到了44个字节。  
4. 当长度小于阈值时，会还原吗？  
&emsp; 关于Redis内部编码的转换，都符合以下规律：编码转换在Redis写入数据时完成，且转换过程不可逆，只能从小内存编码向大内存编码转换（但是不包括重新set）。  

### 1.4.2. Hash内部编码  
&emsp; <font color = "clime">Redis的Hash可以使用两种数据结构实现：ziplist、dictht。</font>Hash结构当同时满足如下两个条件时底层采用了ZipList实现，一旦有一个条件不满足时，就会被转码为dictht进行存储。  

* Hash中存储的所有元素的key和value的长度都小于64byte。(通过修改hash-max-ziplist-value配置调节大小)
* Hash中存储的元素个数小于512。(通过修改hash-max-ziplist-entries配置调节大小)  

### 1.4.3. List内部编码   
&emsp; **在Redis3.2之前，List底层采用了ZipList和LinkedList实现的，在3.2之后，List底层采用了QuickList。**  
&emsp; Redis3.2之前，初始化的List使用的ZipList，List满足以下两个条件时则一直使用ZipList作为底层实现，当以下两个条件任一一个不满足时，则会被转换成LinkedList。

* List 中存储的每个元素的长度小于64byte  
* 元素个数小于512 



### 1.4.4. Set内部编码   
&emsp; Redis中列表和集合都可以用来存储字符串，但是<font color = "red">「Set是不可重复的集合，而List列表可以存储相同的字符串」，</font> **<font color = "cclime">「Set是一个特殊的value为空的Hash」，</font>** Set集合是无序的这个和后面讲的ZSet有序集合相对。  

&emsp; Redis 用intset或dictEntry存储set。当满足如下两个条件的时候，采用整数集合实现；一旦有一个条件不满足时则采用字典来实现。  

* Set 集合中的所有元素都为整数
* Set 集合中的元素个数不大于 512(默认 512，可以通过修改 set-max-intset-entries 配置调整集合大小) 


### 1.4.5. Zset内部编码   
&emsp; ZSet的底层实现是ziplist和skiplist实现的，由ziplist转换为skiplist。当同时满足以下两个条件时，采用ZipList实现；反之采用SkipList实现。

* Zset中保存的元素个数小于128。(通过修改zset-max-ziplist-entries配置来修改)  
* Zset中保存的所有元素长度小于64byte。(通过修改zset-max-ziplist-values配置来修改)  

&emsp; 和List的底层实现有些相似，对于Zset不同的是，其存储是以键值对的方式依次排列，键存储的是实际 value，值存储的是value对应的分值。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-84.png)  

-------------

## 1.5. 查看redis内部存储的操作  
&emsp; ......



