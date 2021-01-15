
<!-- TOC -->

- [1. Redis底层实现](#1-redis底层实现)
    - [1.1. Redis源码阅读之环境搭建及准备](#11-redis源码阅读之环境搭建及准备)
    - [1.2. 对象系统RedisObject](#12-对象系统redisobject)
    - [1.3. String内部编码](#13-string内部编码)
        - [1.3.1. 采用SDS实现](#131-采用sds实现)
            - [1.3.1.1. SDS代码结构](#1311-sds代码结构)
            - [1.3.1.2. SDS动态扩展特点](#1312-sds动态扩展特点)
            - [1.3.1.3. Redis字符串的性能优势](#1313-redis字符串的性能优势)
    - [1.4. Hash内部编码](#14-hash内部编码)
        - [1.4.1. 采用ziplist压缩列表实现](#141-采用ziplist压缩列表实现)
        - [1.4.2. 采用dictht字典实现](#142-采用dictht字典实现)
    - [1.5. List内部编码](#15-list内部编码)
        - [1.5.1. 采用LinkedList双向链表实现](#151-采用linkedlist双向链表实现)
        - [1.5.2. 采用quicklist快速列表实现](#152-采用quicklist快速列表实现)
    - [1.6. Set内部编码](#16-set内部编码)
        - [1.6.1. 采用inset实现](#161-采用inset实现)
    - [1.7. Zset内部编码](#17-zset内部编码)
        - [1.7.1. 采用ZipList压缩列表实现](#171-采用ziplist压缩列表实现)
        - [1.7.2. 采用SkipList跳跃表实现](#172-采用skiplist跳跃表实现)
    - [1.8. 查看redis内部存储的操作](#18-查看redis内部存储的操作)

<!-- /TOC -->

# 1. Redis底层实现  

**《Redis设计与实现》、《Redis深度历险：核心原理和应用实践》、《Redis开发与运维》**  

<!--
连集合底层实现原理都不知道，你敢说Redis用的很溜？ 
https://mp.weixin.qq.com/s/K7Si5bl7K_pjQPHsLrPh5A
万字长文的Redis五种数据结构详解（理论+实战），建议收藏。 
https://mp.weixin.qq.com/s/ipP35Zho9STAgu_lFT79rQ
今天我才知道Redis有9种基本数据类型，据说只有5%的人知道，涨知识了
https://mp.weixin.qq.com/s/zcWvzZTwUAm2NfAQhxMqeQ

全面阐释Redis常见对象类型的底层数据结构 
https://mp.weixin.qq.com/s/QVxwJb6F99E17ZaGQlhVTQ
Redis面试题：Redis的字符串是怎么实现的？ 
https://mp.weixin.qq.com/s/h9V2a-KMLB-eeYliUksYFw
-->

## 1.1. Redis源码阅读之环境搭建及准备  
&emsp; ......
<!-- 
Redis源码阅读之: 环境搭建及准备
https://blog.csdn.net/u014563989/article/details/81066074?utm_medium=distribute.pc_relevant_download.none-task-blog-baidujs-2.nonecase&depth_1-utm_source=distribute.pc_relevant_download.none-task-blog-baidujs-2.nonecase
-->

## 1.2. 对象系统RedisObject  
&emsp; **<font color = "lime">（很重要的思想：redis设计比较复杂的对象系统，都是为了缩减内存占有！！！）</font>**  
&emsp; Redis并没有直接使用数据结构来实现数据类型，而是基于这些数据结构创建了一个对象系统RedisObject，每个对象都使用到了至少一种底层数据结构。 **<font color = "lime">Redis根据不同的使用场景和内容大小来判断对象使用哪种数据结构，从而优化对象在不同场景下的使用效率和内存占用。</font>**   

<!-- 
Redis这样设计有两个好处：
可以偷偷的改进内部编码，而对外的数据结构和命令没有影响，这样一旦开发出更优秀的内部编码，无需改动对外数据结构和命令。
多种内部编码实现可以在不同场景下发挥各自的优势。例如ziplist比较节省内存，但是在列表元素比较多的情况下，性能会有所下降。这时候Redis会根据配置选项将列表类型的内部实现转换为linkedlist。
-->

&emsp; redisObject的源代码在redis.h中，使用c语言编写。redisObject结构的定义如下所示：  

```c
typedef struct redisObject {
    unsigned type:4;
    unsigned encoding:4;
    unsigned lru:LRU_BITS;
    int refcount;
    void *ptr;
} robj;
```
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-10.png)  
* Type：是对象类型，代表一个value对象具体是何种数据类型，包括REDISSTRING，REDISLIST，REDISHASH，REDISSET和REDIS_ZSET。  
* encoding是指对象使用的数据结构，是不同数据类型在redis内部的存储方式。<font color = "red">目前有8种数据结构：int、raw、embstr(SDS)、ziplist、hashtable、quicklist、intset、skiplist。</font>  

&emsp; **<font color = "lime">Redis数据类型的底层实现如下：</font>**  

|Redis数据结构	|底层数据结构|
|---|---|
|String	|int、embstr（即SDS）、raw|
|Hash	|ziplist（压缩列表）或者dictht（字典）|
|List	|quicklist（快速列表，是ziplist压缩列表和linkedlist双端链表的组合）|
|Set	|intset（整数集合）或者dictht（字典）|
|ZSet	|ziplist（压缩列表）或者skiplist（跳跃表）|

1. <font color = "red">字典dictht用于实现Hash、Set；</font>  
2. <font color = "red">压缩列表ziplist用于实现Hsh、List、Zset；</font>  

## 1.3. String内部编码  
<!-- 
Redis 字符串
https://mp.weixin.qq.com/s/8Aw-A-8FdZeXBY6hQlhYUw
-->
&emsp; **<font color = "red">字符串类型的内部编码有三种：</font>**  

*  int，存储 8 个字节的长整型（long，2^63-1）。   
*  embstr，代表 embstr 格式的 SDS（Simple Dynamic String 简单动态字符串），存储小于44个字节的字符串。   
*  raw，存储大于 44 个字节的字符串（3.2 版本之前是 39 字节）。  

&emsp; <font color = "red">Redis会根据当前值的类型和长度决定使用哪种内部编码实现。</font>  

1. embstr和raw的区别？  
&emsp; embstr的使用只分配一次内存空间（因为RedisObject和SDS是连续的），而raw需要分配两次内存空间（分别为RedisObject和SDS分配空间）。 因此与raw相比，<font color = "red">embstr的好处在于创建时少分配一次空间，删除时少释放一次空间，以及对象的所有数据连在一起，寻找方便。而embstr的坏处也很明显，如果字符串的长度增加需要重新分配内存时，整个RedisObject和SDS都需要重新分配空间，</font>因此Redis中的embstr实现为只读。  
2. int和embstr什么时候转化为 raw?  
&emsp; 当int数据不再是整数， 或大小超过了long的范围（2^63-1=9223372036854775807）时，自动转化为embstr。  
3. embstr没有超过阈值，为什么变成raw了？  
&emsp; 对于embstr，由于其实现是只读的，因此在对embstr对象进行修改时，都会先转化为raw再进行修改。因此，只要是修改embstr对象，修改后的对象一定是raw的，无论是否达到了44个字节。  
4. 当长度小于阈值时，会还原吗？  
&emsp; 关于Redis内部编码的转换，都符合以下规律：编码转换在Redis写入数据时完成，且转换过程不可逆，只能从小内存编码向大内存编码转换（但是不包括重新 set）。  

### 1.3.1. 采用SDS实现  
<!-- 
https://mp.weixin.qq.com/s/VY31lBOSggOHvVf54GzvYw
https://mp.weixin.qq.com/s/f71rakde6KBJ_ilRf1M8xQ

-->
<!-- 
1. 什么是 SDS？ Redis中字符串的实现。在 3.2 以后的版本中，SDS 又有多种结构（sds.h）：sdshdr5、sdshdr8、sdshdr16、sdshdr32、sdshdr64，用于存储不同的长度的字符串，分别代表 2^5=32byte， 2^8=256byte，2^16=65536byte=64KB，2^32byte=4GB。  

2. 为什么 Redis 要用 SDS 实现字符串？  
&emsp; C 语言本身没有字符串类型（只能用字符数组 char[]实现）。 
    1. 使用字符数组必须先给目标变量分配足够的空间，否则可能会溢出。  
    2. 如果要获取字符长度，必须遍历字符数组，时间复杂度是 O(n)。  
    3. C 字符串长度的变更会对字符数组做内存重分配。  
    4. 通过从字符串开始到结尾碰到的第一个'\0'来标记字符串的结束，因此不能保 存图片、音频、视频、压缩文件等二进制(bytes)保存的内容，二进制不安全。  

    &emsp; SDS的特点：  
    1. <font color = "red">不用担心内存溢出问题，如果需要，会对SDS进行扩容。</font>  
    2. <font color = "red">获取字符串长度时间复杂度为 O(1)，因为定义了 len 属性。</font>  
    3. 通过“空间预分配”（ sdsMakeRoomFor）和“惰性空间释放”，防止多次重分配内存。  
    4. 判断是否结束的标志是 len 属性（它同样以'\0'结尾是因为这样就可以使用 C语言中函数库操作字符串的函数了），可以包含'\0'。 
-->


&emsp; Redis是C语言开发的，C语言有字符类型，但是Redis却没直接采用C语言的字符串类型，而是自己构建了动态字符串（SDS）的抽象类型。  
 
#### 1.3.1.1. SDS代码结构  

```c
struct sdshdr{
    // 记录已使用长度
    int len;
    // 记录空闲未使用的长度
    int free;
    // 字符数组
    char[] buf;
};
```
&emsp; 对于SDS中的定义在Redis的源码中有的三个属性int len、int free、char buf[]。  
&emsp; <font color = "red">len保存了字符串的长度，free表示buf数组中未使用的字节数量，buf数组则是保存字符串的每一个字符元素。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-77.png)  
&emsp; Redis的字符串也会遵守C语言的字符串的实现规则，即最后一个字符为空字符。然而这个空字符不会被计算在len里头。  

#### 1.3.1.2. SDS动态扩展特点
&emsp; SDS的最厉害最奇妙之处在于它的Dynamic，动态变化长度。举个例子：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-78.png)  

&emsp; 如上图所示刚开始s1 只有5个空闲位子，后面需要追加' world' 6个字符，很明显是不够的。 

&emsp; Redis会做一下三个操作：  
1. 计算出大小是否足够  
2. 开辟空间至满足所需大小  
3. 开辟与已使用大小len相同长度的空闲free空间（如果len < 1M）；开辟1M长度的空闲free空间（如果len >= 1M）。  

#### 1.3.1.3. Redis字符串的性能优势  

* 动态扩展
* 快速获取字符串长度  
* 避免缓冲区溢出  
* 降低空间分配次数提升内存使用效率  
* 二进制安全

1. <font color = "lime">快速获取字符串长度</font>  

        c语言中的字符串并不会记录自己的长度，因此「每次获取字符串的长度都会遍历得到，时间的复杂度是O(n)」，而Redis中获取字符串只要读取len的值就可，时间复杂度变为O(1)。

2. <font color = "lime">避免缓冲区溢出</font>  
&emsp; 对于Redis而言由于每次追加字符串时，<font color = "red">「SDS」会先根据len属性判断空间是否满足要求，若是空间不够，就会进行相应的空间扩展，所以「不会出现缓冲区溢出的情况」。</font>每次追加操作前都会做如下操作：  
    1. 计算出大小是否足够  
    2. 开辟空间至满足所需大小  
    3. 降低空间分配次数提升内存使用效率  

            「c语言」中两个字符串拼接，若是没有分配足够长度的内存空间就「会出现缓冲区溢出的情况」。

3. <font color = "lime">降低空间分配次数，提升内存使用效率</font>  
    &emsp; 字符串的追加、缩减操作会涉及到内存分配问题，然而内存分配问题会牵扯内存划分算法以及系统调用，所以如果频繁发生的话影响性能。所以采取了一下两种优化措施空间预分配、惰性空间回收。  

    1. <font color = "lime">空间预分配</font>   
        &emsp; 对于追加操作来说，Redis不仅会开辟空间至够用，<font color = "red">而且还会预分配未使用的空间(free)来用于下一次操作。</font>至于未使用的空间(free)的大小则由修改后的字符串长度决定。
        
        * 当修改后的字符串长度len < 1M，则会分配与len相同长度的未使用的空间(free)
        * 当修改后的字符串长度len >= 1M，则会分配1M长度的未使用的空间(free)

        &emsp; 有了这个预分配策略之后会减少内存分配次数，因为分配之前会检查已有的free空间是否够，如果够则不开辟了。
    2. <font color = "lime">惰性空间回收</font>  
        &emsp; 与上面情况相反，<font color = "red">惰性空间回收适用于字符串缩减操作。</font>比如有个字符串s1="hello world"，对s1进行sdstrim(s1," world")操作，<font color = "red">执行完该操作之后Redis不会立即回收减少的部分，而是会分配给下一个需要内存的程序。</font>

<!-- 
            SDS还提供「空间预分配」和「惰性空间释放」两种策略。在为字符串分配空间时，分配的空间比实际要多，这样就能「减少连续的执行字符串增长带来内存重新分配的次数」。
            当字符串被缩短的时候，SDS也不会立即回收不适用的空间，而是通过free属性将不使用的空间记录下来，等后面使用的时候再释放。
            具体的空间预分配原则是：「当修改字符串后的长度len小于1MB，就会预分配和len一样长度的空间，即len=free；若是len大于1MB，free分配的空间大小就为1MB」。
-->

4. <font color = "lime">二进制安全</font>  
&emsp; SDS是二进制安全的，除了可以储存字符串以外还可以储存二进制文件（如图片、音频，视频等文件的二进制数据）；而c语言中的字符串是以空字符串作为结束符，一些图片中含有结束符，因此不是二进制安全的。  

## 1.4. Hash内部编码  
&emsp; <font color = "lime">Redis的Hash可以使用两种数据结构实现：ziplist、dictht。</font>Hash结构当同时满足如下两个条件时底层采用了ZipList实现，一旦有一个条件不满足时，就会被转码为dictht进行存储。  

* Hash中存储的所有元素的key和value的长度都小于64byte。（通过修改hash-max-ziplist-value配置调节大小）
* Hash中存储的元素个数小于512。（通过修改hash-max-ziplist-entries配置调节大小）  

### 1.4.1. 采用ziplist压缩列表实现  
&emsp; ziplist是一组连续内存块组成的顺序的数据结构， **<font color = "red">是一个经过特殊编码的双向链表，它不存储指向上一个链表节点和指向下一 个链表节点的指针，而是存储上一个节点长度和当前节点长度，通过牺牲部分读写性能，来换取高效的内存空间利用率，节省空间，是一种时间换空间的思想。</font>**只用在字段个数少，字段值小的场景面。  

&emsp; 压缩列表的内存结构图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-79.png)  
&emsp; 从图中可以看出 ZipList 没有前后指针。压缩列表中每一个节点表示的含义如下所示：
1. zlbytes：4个字节的大小，记录压缩列表占用内存的字节数。
2. zltail：4个字节大小，记录表尾节点距离起始地址的偏移量，用于快速定位到尾节点的地址。
3. zllen：2个字节的大小，记录压缩列表中的节点数。
4. entry：表示列表中的每一个节点。
5. zlend：表示压缩列表的特殊结束符号'0xFF'。

&emsp; 在压缩列表中每一个entry节点又有三部分组成，包括previous_entry_ength、encoding、content。  
1. previous_entry_ength表示前一个节点entry的长度，可用于计算前一个节点的其实地址，因为它们的地址是连续的。  
2. encoding：这里保存的是content的内容类型和长度。  
3. content：content保存的是每一个节点的内容。  

&emsp; ZipList 的优缺点比较：  

* 优点：<font color = "red">内存地址连续，省去了每个元素的头尾节点指针占用的内存。</font>  
* 缺点：对于删除和插入操作比较可能会触发连锁更新反应，比如在 list 中间插入删除一个元素时，在插入或删除位置后面的元素可能都需要发生相应的移动操作。 

### 1.4.2. 采用dictht字典实现  
<!-- 

Redis 字典
https://mp.weixin.qq.com/s/DG3fOoNf-Avuud2cwa3N5A
-->

&emsp; 字典类型的底层是hashtable实现的，明白了字典的底层实现原理也就是明白了hashtable的实现原理，hashtable的实现原理可以与HashMap的是底层原理相类比。它是一个数组+链表的结构。Redis Hash使用MurmurHash2算法来计算键的哈希值，并且使用链地址法来解决键冲突，进行了一些rehash优化等。  
&emsp; dictEntry与HashMap两者在新增时都会通过key计算出数组下标，不同的是计算法方式不同，HashMap中是以hash函数的方式，而hashtable中计算出hash值后，还要通过sizemask 属性和哈希值再次得到数组下标。结构如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-81.png)  

**rehash：**  
&emsp; 在字典的底层实现中，value对象以每一个dictEntry的对象进行存储，当hash表中的存放的键值对不断的增加或者减少时，需要对hash表进行一个扩展或者收缩。  
&emsp; 这里就会和HashMap一样，也会就进行rehash操作，进行重新散列排布。从上图中可以看到有ht[0]和ht[1]两个对象，先来看看对象中的属性是干嘛用的。  
&emsp; 在hash表结构定义中有四个属性分别是table、unsigned long size、unsigned long sizemask、unsigned long used，分别表示的含义就是「哈希表数组、hash表大小、用于计算索引值，总是等于size-1、hash表中已有的节点数」。  
&emsp; ht[0]是用来最开始存储数据的，当要进行扩展或者收缩时，ht[0]的大小就决定了ht[1]的大小，ht[0]中的所有的键值对就会重新散列到ht[1]中。  
&emsp; 扩展操作：ht[1]扩展的大小是比当前 ht[0].used 值的二倍大的第一个 2 的整数幂；收缩操作：ht[0].used 的第一个大于等于的 2 的整数幂。  
&emsp; 当ht[0]上的所有的键值对都rehash到ht[1]中，会重新计算所有的数组下标值，当数据迁移完后ht[0]就会被释放，然后将ht[1]改为ht[0]，并新创建ht[1]，为下一次的扩展和收缩做准备。  

**渐进式rehash：**  
&emsp; 假如在rehash的过程中数据量非常大，Redis不是一次性把全部数据rehash成功，这样会导致Redis对外服务停止，Redis内部为了处理这种情况采用「渐进式的rehash」。  
&emsp; Redis将所有的rehash的操作分成多步进行，直到都rehash完成，具体的实现与对象中的rehashindex属性相关，「若是rehashindex 表示为-1表示没有rehash操作」。  
&emsp; 当rehash操作开始时会将该值改成0，在渐进式rehash的过程「更新、删除、查询会在ht[0]和ht[1]中都进行」，比如更新一个值先更新ht[0]，然后再更新ht[1]。  
&emsp; 而新增操作直接就新增到ht[1]表中，ht[0]不会新增任何的数据，这样保证「ht[0]只减不增，直到最后的某一个时刻变成空表」，这样rehash操作完成。  

## 1.5. List内部编码   
&emsp; 在 Redis3.2 之前，List 底层采用了 ZipList 和 LinkedList 实现的，在 3.2 之后，List 底层采用了 QuickList。  
&emsp; Redis3.2 之前，初始化的 List 使用的 ZipList，List 满足以下两个条件时则一直使用 ZipList 作为底层实现，当以下两个条件任一一个不满足时，则会被转换成 LinkedList。

* List 中存储的每个元素的长度小于64byte  
* 元素个数小于512 

### 1.5.1. 采用LinkedList双向链表实现  
&emsp; Redis的链表在双向链表上扩展了头、尾节点、元素数等属性。Redis的链表结构如下：
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-62.png)  

&emsp; 各部分作用说明：  

* head：表示 List 的头结点；通过其可以找到 List 的头节点。  
* tail：表示 List 的尾节点；通过其可以找到 List 的尾节点。  
* len：表示 List 存储的元素个数。  
* dup：表示用于复制元素的函数。  
* free：表示用于释放元素的函数。  
* match：表示用于对比元素的函数。  

&emsp; Redis中链表的特性：  
1. 每一个节点都有指向前一个节点和后一个节点的指针。  
2. 头节点和尾节点的prev和next指针指向为null，所以链表是无环的。  
3. 链表有自己长度的信息，获取长度的时间复杂度为O(1)。  

<!-- 
&emsp; 从图中可以看出Redis的linkedlist双端链表有以下特性：节点带有prev、next指针、head指针和tail指针，获取前置节点、后置节点、表头节点和表尾节点的复杂度都是O(1)。len属性获取节点数量也为O(1)。 
-->
 

### 1.5.2. 采用quicklist快速列表实现
&emsp; 在 Redis3.2 版本之后，Redis 集合采用了 QuickList 作为 List 的底层实现，QuickList 其实就是结合了 ZipList 和 LinkedList 的优点设计出来的。quicklist 存储了一个双向链表，每个节点 都是一个ziplist。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-63.png)  

&emsp; 各部分作用说明：  

* 每个listNode 存储一个指向 ZipList 的指针，ZipList 用来真正存储元素的数据。
* ZipList 中存储的元素数据总大小超过 8kb（默认大小，通过 list-max-ziplist-size 参数可以进行配置）的时候，就会重新创建出来一个 ListNode 和 ZipList，然后将其通过指针关联起来。

## 1.6. Set内部编码   
&emsp; Redis中列表和集合都可以用来存储字符串，但是<font color = "red">「Set是不可重复的集合，而List列表可以存储相同的字符串」，</font>「Set是一个特殊的value为空的Hash」，Set集合是无序的这个和后面讲的ZSet有序集合相对。  

&emsp; Redis 用intset或dictEntry存储set。当满足如下两个条件的时候，采用整数集合实现；一旦有一个条件不满足时则采用字典来实现。  

* Set 集合中的所有元素都为整数
* Set 集合中的元素个数不大于 512（默认 512，可以通过修改 set-max-intset-entries 配置调整集合大小） 

### 1.6.1. 采用inset实现  
&emsp; inset的数据结构：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-7.png)  
&emsp; inset也叫做整数集合，用于保存整数值的数据结构类型，它可以保存int16_t、int32_t 或者int64_t 的整数值。  
&emsp; 在整数集合中，有三个属性值encoding、length、contents[]，分别表示编码方式、整数集合的长度、以及元素内容，length就是记录contents里面的大小。  

&emsp; 在整数集合新增元素的时候，若是超出了原集合的长度大小，就会对集合进行升级，具体的升级过程如下：  
1. 首先扩展底层数组的大小，并且数组的类型为新元素的类型。  
2. 然后将原来的数组中的元素转为新元素的类型，并放到扩展后数组对应的位置。  
3. 整数集合升级后就不会再降级，编码会一直保持升级后的状态。  

## 1.7. Zset内部编码   
&emsp; ZSet的底层实现是ziplist和skiplist实现的，由ziplist转换为skiplist。当同时满足以下两个条件时，采用ZipList实现；反之采用SkipList实现。

* Zset中保存的元素个数小于128。（通过修改zset-max-ziplist-entries配置来修改）  
* Zset中保存的所有元素长度小于64byte。（通过修改zset-max-ziplist-values配置来修改）  

### 1.7.1. 采用ZipList压缩列表实现  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-84.png)  
&emsp; 和List的底层实现有些相似，对于Zset不同的是，其存储是以键值对的方式依次排列，键存储的是实际 value，值存储的是value对应的分值。  

### 1.7.2. 采用SkipList跳跃表实现  
&emsp; skiplist也叫做「跳跃表」，跳跃表是一种有序的数据结构，它通过每一个节点维持多个指向其它节点的指针，从而达到快速访问的目的。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-85.png)  
<!-- 
&emsp; 具体实现的结构图如下所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-82.png)  
-->
&emsp; SkipList分为两部分，dict部分是由字典实现，Zset部分使用跳跃表实现，从图中可以看出，dict和跳跃表都存储了数据，实际上dict和跳跃表最终使用指针都指向了同一份数据，即数据是被两部分共享的，为了方便表达将同一份数据展示在两个地方。  

&emsp; 在跳跃表的结构中有head和tail表示指向头节点和尾节点的指针，能快速的实现定位。level表示层数，len表示跳跃表的长度，BW表示后退指针，在从尾向前遍历的时候使用。BW下面还有两个值分别表示分值（score）和成员对象（各个节点保存的成员对象）。  

&emsp; skiplist有如下几个特点：  
1. 有很多层组成，由上到下节点数逐渐密集，最上层的节点最稀疏，跨度也最大。  
2. 每一层都是一个有序链表，至少包含两个节点，头节点和尾节点。  
3. 每一层的每一个节点都含有指向同一层下一个节点和下一层同一个位置节点的指针。  
4. 如果一个节点在某一层出现，那么该以下的所有链表同一个位置都会出现该节点。  

&emsp; 跳跃表的上面层就相当于索引层，都是为了找到最后的数据而服务的，数据量越大，条表所体现的查询的效率就越高，和平衡树的查询效率相差无几。  

&emsp; 如下图所示，红线是查找10的过程：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-83.png)  


## 1.8. 查看redis内部存储的操作  
&emsp; ......

