
&emsp; &emsp; [数据结构](/docs/microService/Redis/dataStructure.md)  
&emsp; &emsp; [SDS](/docs/microService/Redis/SDS.md)  
&emsp; &emsp; [Dictht](/docs/microService/Redis/Dictht.md)  
&emsp; &emsp; [数据类型](/docs/microService/Redis/dataType.md)  



&emsp; **<font color = "red">总结：</font>**  
1. 很重要的思想：redis设计比较复杂的对象系统，都是为了缩减内存占有！！！  
2. redis底层8种数据结构：int、raw、embstr(SDS)、ziplist、hashtable、quicklist、intset、skiplist。  
    * ziplist是一组连续内存块组成的顺序的数据结构， **<font color = "red">是一个经过特殊编码的双向链表，它不存储指向上一个链表节点和指向下一个链表节点的指针，而是存储上一个节点长度和当前节点长度，通过牺牲部分读写性能，来换取高效的内存空间利用率，节省空间，是一种时间换空间的思想。</font>** 只用在字段个数少，字段值小的场景里。  
    * QuickList其实就是结合了ZipList和LinkedList的优点设计出来的。quicklist存储了一个双向链表，每个节点都是一个ziplist。  
3. Redis会根据当前值的类型和长度决定使用哪种内部编码实现。 **<font color = "clime">Redis根据不同的使用场景和内容大小来判断对象使用哪种数据结构，从而优化对象在不同场景下的使用效率和内存占用。</font>**   
    
    * String字符串类型的内部编码有三种：
        1. int，存储8个字节的长整型(long，2^63-1)。当int数据不再是整数，或大小超过了long的范围(2^63-1=9223372036854775807)时，自动转化为embstr。  
        2. embstr，代表 embstr 格式的 SDS(Simple Dynamic String 简单动态字符串)，存储小于44个字节的字符串。  
        3. raw，存储大于 44 个字节的字符串(3.2 版本之前是 39 字节)。  
    * Hash由ziplist(压缩列表)或者dictht(字典)组成；  
    * List，「有序」「可重复」集合，由ziplist压缩列表和linkedlist双端链表的组成，在 3.2 之后采用QuickList；  
    * Set，「无序」「不可重复」集合， **<font color = "clime">是特殊的Hash结构(value为null)，</font>** 由intset(整数集合)或者dictht(字典)组成；
    * ZSet，「有序」「不可重复」集合，由skiplist(跳跃表)或者ziplist(压缩列表)组成。  


# 1. Redis底层实现  
&emsp; **~~参考《Redis设计与实现》、《Redis深度历险：核心原理和应用实践》、《Redis开发与运维》~~**  

