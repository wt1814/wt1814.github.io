
<!-- TOC -->

- [1. HashMap源码](#1-hashmap源码)
    - [1.1. HashMap类定义](#11-hashmap类定义)
    - [1.2. 属性(数据结构)](#12-属性数据结构)
        - [1.2.1. 源码](#121-源码)
        - [1.2.2. Hash表数据结构](#122-hash表数据结构)
        - [1.2.3. 树形化结构](#123-树形化结构)
        - [1.2.4. HashMap的内部类](#124-hashmap的内部类)
    - [1.3. 构造函数](#13-构造函数)
    - [1.4. 成员方法](#14-成员方法)
        - [1.4.1. hash()函数(扰动函数)](#141-hash函数扰动函数)
        - [1.4.2. put()，插入](#142-put插入)
            - [1.4.2.1. 时序图及说明](#1421-时序图及说明)
            - [1.4.2.2. 具体源码](#1422-具体源码)
        - [1.4.3. resize()，扩容机制](#143-resize扩容机制)
            - [1.4.3.1. 时序图及说明](#1431-时序图及说明)
            - [1.4.3.2. 具体源码](#1432-具体源码)
                - [1.4.3.2.1. resize()方法](#14321-resize方法)
                - [1.4.3.2.2. split()，红黑树处理](#14322-split红黑树处理)
    - [1.5. HashMap在JDK1.7和JDK1.8中的区别总结](#15-hashmap在jdk17和jdk18中的区别总结)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. HashMap数据结构：  
    1. Hash表数据结构：  
    &emsp; 初始容量为16；  
    &emsp; HashMap在发生hash冲突的时候用的是链地址法。JDK1.7中使用头插法，JDK1.8使用尾插法。  
    &emsp; loadFactor加载因子0.75f；  
    2. 树形化结构：  
    &emsp; 树形化：把链表转换成红黑树，树化需要满足以下两个条件：链表长度大于等于8；table数组长度大于等于64。  
    &emsp; 解除树形化：阈值6。
2. HashMap成员方法：  
    1. hash()函数/扰动函数：  
    &emsp; hash函数会根据传递的key值进行计算， 1)首先计算key的hashCode值， 2)然后再对hashcode进行无符号右移操作， 3)最后再和hashCode进行异或 ^ 操作。（即让hashcode的高16位和低16位进行异或操作。）   
    &emsp; **<font color = "clime">`看似“多余”的2、3步`的好处是增加了随机性，减少了碰撞冲突的可能性。</font>**    
    2. put()函数：
        1. 在put的时候，首先对key做hash运算，计算出该key所在的index。
        2. 如果没碰撞，直接放到数组中；
        3. 如果碰撞了，如果key是相同的，则替掉到原来的值；
        4. 如果key不同，需要判断目前数据结构是链表还是红黑树，根据不同的情况来进行插入。
        5. 最后判断哈希表是否满了(当前哈希表大小*负载因子)，如果满了，则扩容。  
    2. 扩容机制：JDK 1.8扩容条件是数组长度大于阈值或链表转为红黑树且数组元素小于64时。  
        * 单节点迁移。  
        * 如果节点是红黑树类型的话则需要进行红黑树的拆分：`拆分成高低位链表，如果链表长度大于6，需要把链表升级成红黑树。`
        * 对链表进行迁移。会对链表中的节点进行分组，进行迁移后，一类的节点位置在原索引，一类在原索引+旧数组长度。 ~~通过 hash & oldCap(原数组大小)的值来判断，若为0则索引位置不变，不为0则新索引=原索引+旧数组长度~~

# 1. HashMap源码

## 1.1. HashMap类定义  

```java
public class HashMap<K,V> extends AbstractMap<K,V> implements Map<K,V>, Cloneable, Serializable
```
&emsp; Cloneable空接口，表示可以克隆； Serializable序列化； AbstractMap，提供Map实现接口。  

## 1.2. 属性(数据结构)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-5.png)  

### 1.2.1. 源码
```java
//默认的初始化容量为16，必须是2的n次幂
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

//最大容量为 2^30
static final int MAXIMUM_CAPACITY = 1 << 30;

//默认的加载因子0.75，乘以数组容量得到的值，用来表示元素个数达到多少时，需要扩容。
//为什么设置 0.75 这个值呢，简单来说就是时间和空间的权衡。
//若小于0.75如0.5，则数组长度达到一半大小就需要扩容，空间使用率大大降低，
//若大于0.75如0.8，则会增大hash冲突的概率，影响查询效率。
static final float DEFAULT_LOAD_FACTOR = 0.75f;

//当链表长度过长时，会有一个阈值，超过这个阈值8就会转化为红黑树
static final int TREEIFY_THRESHOLD = 8;

//当红黑树上的元素个数，减少到6个时，就退化为链表
static final int UNTREEIFY_THRESHOLD = 6;

//链表转化为红黑树，除了有阈值的限制，还有另外一个限制，需要数组容量至少达到64，才会树化。
//这是为了避免，数组扩容和树化阈值之间的冲突。
static final int MIN_TREEIFY_CAPACITY = 64;

//存放所有Node节点的数组
transient Node<K,V>[] table;

//存放所有的键值对
transient Set<Map.Entry<K,V>> entrySet;

//map中的实际键值对个数，即数组中元素个数
transient int size;

//每次结构改变时，都会自增，fail-fast机制，这是一种错误检测机制。
//当迭代集合的时候，如果结构发生改变，则会发生 fail-fast，抛出异常。
transient int modCount;

//数组扩容阈值
int threshold;

//加载因子
final float loadFactor;

//普通单向链表节点类
static class Node<K,V> implements Map.Entry<K,V> {
    //key的hash值，put和get的时候都需要用到它来确定元素在数组中的位置
    final int hash;
    final K key;
    V value;
    //指向单链表的下一个节点
    Node<K,V> next;

    Node(int hash, K key, V value, Node<K,V> next) {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }
}

//转化为红黑树的节点类
static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
    //当前节点的父节点
    TreeNode<K,V> parent;
    //左孩子节点
    TreeNode<K,V> left;
    //右孩子节点
    TreeNode<K,V> right;
    //指向前一个节点
    TreeNode<K,V> prev;    // needed to unlink next upon deletion
    //当前节点是红色或者黑色的标识
    boolean red;
    TreeNode(int hash, K key, V val, Node<K,V> next) {
        super(hash, key, val, next);
    }
}
```

### 1.2.2. Hash表数据结构
&emsp; **HashMap中hash函数设计：** 参考下文成员方法hash()章节。    
&emsp; **HashMap在发生hash冲突的时候用的是链地址法。** **<font color = "red">JDK1.7中使用头插法，JDK1.8使用尾插法。</font>**  
&emsp; **在HashMap的数据结构中，有两个参数可以影响HashMap的性能：初始容量(inital capacity)和负载因子(load factor)。** 初始容量和负载因子也可以修改，具体实现方式，可以在对象初始化的时候，指定参数。  

* initialCapacity数组的初始容量为16。可以在构造方法中指定。

    &emsp; static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;HashMap的默认初始容量是1 << 4 = 16， << 是一个左移操作，它相当于是   
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-11.png)  
    &emsp; <font color = "red">HashMap的数组长度为什么一定是2的幂次方？</font>  
    &emsp; **HashMap是通过一个名为tableSizeFor的方法来确保HashMap数组长度永远为2的幂次方的。源码查看构造函数部分。**  
    &emsp; 为什么要把数组长度设计为2的幂次方呢？  
    &emsp; 当数组长度为2的幂次方时，可以使用位运算来计算元素在数组中的下标。  
* loadFactor加载因子0.75f。所谓的加载因子就是HashMap的容量达到0.75时的时候，会自动扩容并重新哈希resize()，扩容后的HashMap容量是之前容量的两倍，所以数组的长度总是2的n次方。(例：假设有一个HashMap的初始容量为16，那么扩容的阀值就是0.75 * 16 = 12。也就是说，在打算存入第13个值的时候，HashMap会先执行扩容)。  

    &emsp; <font color = "clime">哈希因子为什么默认为0.75？</font>“哈希冲突”和“空间利用率”矛盾的一个折衷。  
    &emsp; 1. 如果loadFactor太小如0.5，则数组长度达到一半大小就需要扩容，空间使用率大大降低；map中的table需要不断的扩容，扩容是个耗时的过程。  
    &emsp; 2. 如果loadFactor太大如0.8，那么map中table放满了也不不会扩容，导致冲突越来越多，解决冲突而起的链表越来越长，查询效率越来越低。  
    &emsp; 3. 而0.75是一个折中的值，是一个比较理想的值，这是一个在时间和空间上的一个折中。

    <!-- 
    &emsp; 加载因子也能通过构造方法中指定，默认的负载因子是0.75f，这是一个在时间和空间上的一个折中；较高的值减少了空间开销，但增加了查找成本(主要表现在HaspMap的get和put操作)。如果指定大于1，则数组不会扩容，牺牲了性能不过提升了内存。
    -->  
* threshold数组扩容阈值。即：HashMap数组总容量 * 加载因子。 **<font color = "red">记录当前数组的最大容量。当前容量大于或等于该值时会执行扩容 resize()。</font>** 扩容的容量为当前HashMap总容量的两倍。比如，当前HashMap的总容量为16，那么扩容之后为32。  
    
        threshold除了用于存放扩容阈值还有其他作用吗？
        在新建HashMap对象时，threshold还会被用来存初始化时的容量。HashMap直到第一次插入节点时，才会对table进行初始化，避免不必要的空间浪费。

### 1.2.3. 树形化结构
&emsp; 在JDK1.8中，HashMap是由数组+链表+红黑树构成，新增了红黑树作为底层数据结构。链表长度大于8的时候，链表会转成红黑树；当红黑树的节点数小于6时，会转化成链表。  
&emsp; **<font color = "clime">为什么使用红黑树？</font>**  
&emsp; JDK1.7中，<font color = "red">如果哈希碰撞过多，拉链过长，</font>极端情况下，所有值都落入了同一个桶内，这就退化成了一个链表。<font color = "red">通过key值查找要遍历链表，效率较低。</font>JDK1.8在解决哈希冲突时，当链表长度大于阈值(默认为8)时，将链表转化为红黑树，以减少搜索时间。  

* TREEIFY_THRESHOLD树形化阈值。当链表的节点个数大于等于这个值时，会将链表转化为红黑树。  

    &emsp; 理想情况下，使用随机的哈希码，节点分布在hash桶中的频率遵循泊松分布，<font color = "red">按照泊松分布的公式计算，链表中节点个数为8时的概率为0.00000006，这个概率足够低了，并且到8个节点时，红黑树的性能优势也会开始展现出来，因此8是一个较合理的数字。</font>  

* UNTREEIFY_THRESHOLD解除树形化阈值。当链表的节点个数小于等于这个值时，会将红黑树转换成普通的链表。

    &emsp; 为什么在少于6的时候而不是8的时候才将红黑树转换为链表呢？  
    &emsp; 假设设计成大于8时链表转换为红黑树，小于8的时候又转换为链表。如果一个hashmap不停的插入、删除。 **<font color = "red">hashmap中的个数不停地在8徘徊，那么就会频繁的发生链表和红黑树之间转换，效率非常低。</font>** 因此，6和8之间来一个过渡值可以减缓这种情况造成的影响。

* **<font color = "clime">MIN_TREEIFY_CAPACITY树形化阈值的第二条件，数组扩容临界值。当数组的长度小于这个值时，当树形化阈值达标时，链表也不会转化为红黑树，而是优先扩容数组resize()。</font>**  

    &emsp; **<font color = "red">为什么 table 数组容量大于等于 64 才树化？</font>**  
    &emsp; 因为当 table 数组容量比较小时，键值对节点 hash 的碰撞率可能会比较高，进而导致链表长度较长。这个时候应该优先扩容，而不是立马树化。

&emsp; 小结：<font color = "clime">把链表转换成红黑树，树化需要满足以下两个条件：链表长度大于等于 8；table 数组长度大于等于 64。</font>  

### 1.2.4. HashMap的内部类
&emsp; HashMap 内部有很多内部类，扩展了 HashMap 的一些功能，EntrySet类就是其中一种，该类较为简单，无内部属性，可以理解为一个工具类，对HashMap进行了简单的封装，提供了方便的遍历、删除等操作。  
&emsp; 调用HashMap的entrySet()方法就可以返回EntrySet实例对象，为了不至于每次调用该方法都返回新的EntrySet对象，所以设置该属性，缓存EntrySet实例  。  

## 1.3. 构造函数  

```java
//默认构造函数，初始化加载因子loadFactor = 0.75
public HashMap() {
    this.loadFactor = DEFAULT_LOAD_FACTOR;
}
/**
 *传入初始容量大小，使用默认负载因子值来初始化HashMap对象
 */
public HashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
}
/**
 * @param initialCapacity 指定初始化容量
 * @param loadFactor 加载因子 0.75
 */
public HashMap(int initialCapacity, float loadFactor) {
    //初始化容量不能小于 0 ，否则抛出异常
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " +
                initialCapacity);
    //如果初始化容量大于2的30次方，则初始化容量都为2的30次方
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    //如果加载因子小于0，或者加载因子是一个非数值，抛出异常
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " +
                loadFactor);
    this.loadFactor = loadFactor;
    this.threshold = tableSizeFor(initialCapacity);
}
```

```java
// 返回大于等于initialCapacity的最小的二次幂数值。
// >>> 操作符表示无符号右移，高位取0。
// | 按位或运算
static final int tableSizeFor(int cap) {
    int n = cap - 1;//确保第一次出现1的位及其后一位都是1
    n |= n >>> 1;//确保前两次出现的1及其后两位都是1
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}
```

&emsp; 需要注意的是，传入的initialCapacity并不是实际的初始容量，<font color= "red">HashMap通过tableSizeFor()将initialCapacity调整为大于等于该值的最小2次幂。</font>  
&emsp; <font color = "red">该算法让最高位的 1 后面的位全变为 1。最后再让结果 n+1，即得到了 2 的整数次幂的值。</font>  
<!-- 
让 cap-1 再赋值给 n 的目的是另找到的目标值大于或等于原值。例如二进制 1000，十进制数值为 8。如果不对它减1而直接操作，将得到答案 10000，即 16。显然不是结果。减 1 后二进制为 111，再进行操作则会得到原来的数值 1000，即 8。通过一系列位运算大大提高效率。
-->
1. cap - 1 是为了处理 cap 本身就是 2 的N次方的情况。让cap-1再赋值给n的目的是使得找到的目标值大于或等于原值。例如二进制 1000，十进制数值为 8。如果不对它减1而直接操作，将得到答案 10000，即 16。显然不是结果。减 1 后二进制为 111，再进行操作则会得到原来的数值 1000，即 8。通过一系列位运算大大提高效率。  
2. \>>>(无符号右移)：例如 a >>> b 指的是将 a 向右移动 b 指定的位数，右移后左边空出的位用零来填充，移出右边的位被丢弃。  
3. <font color = "clime">运算符 |= ，它表示的是按位或，双方都转换为二进制，来进行与操作。</font>(「a+=b 的意思是 a=a+b」，那么同理：a |= b 就是 a = a | b。)  

&emsp; 完整示例：  
&emsp; ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-12.png)  
&emsp; 上面采用了一个比较大的数字进行扩容，由上图可知 2^29 次方的数组经过一系列的或操作后，会算出来结果是2^30次方。所以扩容后的数组长度是原来的2倍。  

## 1.4. 成员方法  
### 1.4.1. hash()函数(扰动函数)  
&emsp; **<font color = "red">无论增加、删除还是查找键值对，定位到数组的位置都是很关键的第一步。</font>** 在HashMap中并不是直接通过key的hashcode方法获取哈希值，而是通过内部自定义的hash方法计算哈希值。  
```java
static final int hash(Object key) {
    int h;
    //允许key为null，hash = 0
    //h = key.hashCode()，取hashCode值
    //(h >>> 16)，高位参与运算
    //h ^ (h >>> 16)，高位与低位异或运算
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

    1. \>>>: 无符号右移操作，它指的是「无符号右移，也叫逻辑右移，即若该数为正，则高位补0，而若该数为负数，则右移后高位同样补0」，也就是不管是正数还是负数，右移都会在空缺位补0。  
    2. ^：布尔运算符，异或运算。

&emsp; HashMap的Hash规则：(hash函数会根据传递的key值进行计算，首先计算key的hashCode值，然后再对hashcode进行无符号右移操作，最后再和hashCode进行异或^操作)
1. 计算hash值，得到一个int类型的值(32 位)，int hash = key.hashCode()。
2. 异或上 hash值无符号右移16位后的值(让hashcode的高16位和低16位进行异或操作)。hash = hash ^ (hash >>> 16)。
3. 注：如果是计算数组下标(插入/查找的时候，计算key应该被映射到散列表的什么位置)，还需位置计算公式index = (n - 1) & hash ，其中 n 是容量。

&emsp; 为什么获取hashcode()，还需要将自己右移16位与自己进行异或呢？  
&emsp; **<font color = "clime">先算出正常的哈希值，然后与高16位做异或运算，产生最终的哈希值。这样做的好处是增加了随机性，减少了碰撞冲突的可能性。</font>**  
&emsp; **<font color = "red">~~hash函数称为“扰动函数”。目的是为了减少哈希碰撞，使table里的数据分布的更均匀。并且采用位运算，比较高效。~~</font>**  

<!-- 
&emsp; <font color = "red">因为容量较小的时候，在计算 index 时，真正用到的其实就只有低几位，假如不融合高低位，那么假设 hashcode() 返回的值都是高位的变动的话，那么很容易造成散列的值都是同一个。</font>但是，假如将高位和低位融合之后，高位的数据变动会最终影响到 index 的变换，所以依然可以保持散列的随机性。  
-->

&emsp; 在计算 index 的时候，为什么不使用 hash(key) % capacity ，而使用index = (n - 1) & hash ？  
&emsp; ~~这是因为移位运算相比取余运算会更快。那么为什么 hash(key) & (capacity - 1) 也可以呢？这是因为在 B 是 2 的幂情况下：A % B = A & (B - 1)。如果 A 和 B 进行取余，其实相当于把 A 那些不能被 B 整除的部分保留下来。从二进制的方式来看，其实就是把 A 的低位给保留了下来。B-1 相当于一个“低位掩码”，而与的操作结果就是散列值的高位全部置为 0 ，只保留低位，而低位正好是取余之后的值。取个例子，A = 24，B =16，那么 A%B=8，从二进制角度来看 A =11000 ，B = 10000。A 中不能被 B 整除的部分其实就是 1000 这个部分。接下去，需要将这部分保留下来的话，其实就是使用 01111 这个掩码并跟 A 进行与操作，即可将1000 保留下来，作为 index 的值。而 01111 这个值又等于 B-1。所以 A &(B-1)= A%B。但是这个前提是 B 的容量是 2 的幂，那么如何保证呢？可以看到，在设置初始大小的时候，无论设置了多少，都会被转换为 2 的幂的一个数。之外，扩容的时候也是按照 2 倍进行扩容的。所以 B 的值是 2 的幂是没问题的。~~  

<!-- 
&emsp; hash函数是先得到key 的hashcode(32位的int值)，然后让hashcode的高16位和低16位进行异或操作。  
&emsp; (h = key.hashCode()) ^ (h >>> 16) 是为了让高位数据与低位数据进行异或，变相的让高位数据参与到计算中，int 有 32 位，右移 16 位就能让低 16 位和高 16 位进行异或，也是为了增加 hash 值的随机性。  
-->

### 1.4.2. put()，插入 
#### 1.4.2.1. 时序图及说明
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-18.png)  

&emsp; **<font color = "clime">插入元素方法：</font>**   
&emsp; <font color = "clime">在put的时候，首先对key做hash运算，计算出该key所在的index。如果没碰撞，直接放到数组中，如果碰撞了，如果key是相同的，则替换掉原来的值。如果key不同，需要判断目前数据结构是链表还是红黑树，根据不同的情况来进行插入。最后判断哈希表是否满了(当前哈希表大小*负载因子)，如果满了，则扩容。</font>  

1. 计算 key 的 hash 值。  
&emsp; 计算方式是 (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);  
2. 检查当前数组是否为空，为空需要进行初始化，初始化容量是 16 ，负载因子默认 0.75。  
3. 计算 key 在数组中的坐标。计算方式：(容量 - 1) & hash。 
4. 如果计算出的坐标元素为空，创建节点加入，put结束。  
    1. 如果当前数组容量大于负载因子设置的容量，进行扩容。  
5. 如果计算出的坐标元素有值。  
    1. 如果准备插入节点和要插入节点的hash和参数key相等，记录插入位置，后面会覆盖value。  
    2. 如果要插入的节点是红黑树节点，则调用红黑树的插入操作。
    3. 否则插入的节点是链表节点。循环遍历：
        1. 如果 next 节点为空，把要加入的值和key加入next节点。插入节点超过8层，转换红黑树。  
        2. 可能还会出现准备插入节点和要插入节点的hash和参数key相等，直接返回。
6. 判断是否需要扩容。  

#### 1.4.2.2. 具体源码
&emsp; JDK1.8put()方法源码部分：  

```java
/**
 * put方法
 */
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}
```
```java
/**
 * 插入元素方法
 */
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    //1、判断数组table是否为空或为null
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    //2、判断数组下标table[i]==null
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
        //3、判断table[i]的首个元素是否和传入的key一样
        if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
            //4、判断table[i] 是否为treeNode
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            //5、遍历table[i]，判断链表长度是否大于8
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    //长度大于8，转红黑树结构
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        //传入的K元素已经存在，直接覆盖value
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    //6、判断size是否超出最大容量
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```


<!-- 
步骤二：(n - 1) & hash
https://mp.weixin.qq.com/s/wIjAj4rAAZccAl-yhmj_TA
-->


&emsp; 红黑树插入方法，源码部分：  

```java
/**
 * 红黑树的插入操作
 */
final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
                               int h, K k, V v) {
    Class<?> kc = null;
    boolean searched = false;
    TreeNode<K,V> root = (parent != null) ? root() : this;
    for (TreeNode<K,V> p = root;;) {
        //dir:遍历的方向， ph:p节点的hash值
        int dir, ph; K pk;
        //红黑树是根据hash值来判断大小
        // -1:左孩子方向 1:右孩子方向
        if ((ph = p.hash) > h)
            dir = -1;
        else if (ph < h)
            dir = 1;
            //如果key存在的话就直接返回当前节点
        else if ((pk = p.key) == k || (k != null && k.equals(pk)))
            return p;
            //如果当前插入的类型和正在比较的节点的Key是Comparable的话，就直接通过此接口比较
        else if ((kc == null &&
                (kc = comparableClassFor(k)) == null) ||
                (dir = compareComparables(kc, k, pk)) == 0) {
            if (!searched) {
                TreeNode<K,V> q, ch;
                searched = true;
                //尝试在p的左子树或者右子树中找到了目标元素
                if (((ch = p.left) != null &&
                        (q = ch.find(h, k, kc)) != null) ||
                        ((ch = p.right) != null &&
                                (q = ch.find(h, k, kc)) != null))
                    return q;
            }
            //获取遍历的方向
            dir = tieBreakOrder(k, pk);
        }
        //上面的所有if-else判断都是在判断下一次进行遍历的方向，即dir
        TreeNode<K,V> xp = p;
        //当下面的if判断进去之后就代表找到了目标操作元素,即xp
        if ((p = (dir <= 0) ? p.left : p.right) == null) {
            Node<K,V> xpn = xp.next;
            //插入新的元素
            TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
            if (dir <= 0)
                xp.left = x;
            else
                xp.right = x;
            //因为TreeNode今后可能退化成链表，在这里需要维护链表的next属性
            xp.next = x;
            //完成节点插入操作
            x.parent = x.prev = xp;
            if (xpn != null)
                ((TreeNode<K,V>)xpn).prev = x;
            //插入操作完成之后就要进行一定的调整操作了
            moveRootToFront(tab, balanceInsertion(root, x));
            return null;
        }
    }
}
```

### 1.4.3. resize()，扩容机制  
<!-- 
https://www.jianshu.com/p/87d2ef48e645
-->

&emsp; **<font color = "red">~~JDK1.8 优化成直接把链表拆成高位和低位两部分，通过位运算来决定放在原索引处或者原索引加原数组长度的偏移量处。~~</font>**  

&emsp; HashMap在什么条件下扩容？  
&emsp; **<font color = "clime">JDK 1.8扩容条件是数组长度大于阈值或链表转为红黑树且数组元素小于64时，在首次插入值的时候也会进行扩容。</font>**  

```java
//数组长度大于阈值，就扩容
if (++size > threshold)
resize();

//链表转为红黑树时，若此时数组长度小于64，扩容数组
if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
resize();
```

#### 1.4.3.1. 时序图及说明   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-15.png)  

&emsp; <font color = "red">HashMap每次扩容都是建立一个新的table数组，长度和容量阈值都变为原来的两倍，然后把原数组元素重新映射到新数组上。</font>  
&emsp; 具体步骤如下：  
1. 首先会判断table数组长度，如果大于0说明已被初始化过，那么按当前table数组长度的2倍进行扩容，阈值也变为原来的2倍。  
2. 若table数组未被初始化过，且threshold(阈值)大于0，说明调用了HashMap(initialCapacity, loadFactor) 构造方法，那么就把数组大小设为threshold。  
3. 若table数组未被初始化，且threshold为0，说明调用HashMap()构造方法，那么就把数组大小设为16，threshold设为16*0.75。  
4. 接着需要判断是否第一次初始化。如果不是，那么<font color = "red">扩容之后，要重新计算键值对的位置，并把它们移动到合适的位置上去。</font>  
    1. 如果是单个节点，直接重新计算下标值，移动。  
    2. 如果节点是红黑树类型的话则需要进行红黑树的拆分：拆分成高低位链表，如果链表长度大于6，需要把链表升级成红黑树。  
    3. <font color = "red">对链表进行迁移。会对链表中的节点，进行分组，进行迁移后，一类的节点位置在原索引，一类在原索引+旧数组长度。</font>


&emsp; <font color = "clime">对链表进行迁移的注意点：</font>JDK1.8HashMap扩容阶段重新映射元素时不需要像1.7版本那样重新去一个个计算元素的 hash 值，<font color = "clime">而是通过 hash & oldCap(原数组大小)的值来判断，若为0则索引位置不变，不为0则新索引=原索引+旧数组长度，</font>为什么呢？具体原因如下：  
&emsp; 因为使用的是2次幂的扩展(指长度扩为原来2倍)，所以，元素的位置要么是在原位置，要么是在原位置再移动2次幂的位置。因此，在扩充 HashMap 的时候，不需要像 JDK1.7 的实现那样重新计算 hash，只需要看看原来的 hash 值新增的那个 bit 是 1 还是 0 就好了，是 0 的话索引没变，是 1 的话索引变成“原索引 +oldCap。  
&emsp; 这点其实也可以看做长度为 2 的幂次方的一个好处，也是 HashMap 1.7 和 1.8 之间的一个区别。  
&emsp; 示例：扩容前 table 的容量为16，a 节点和 b 节点在扩容前处于同一索引位置。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-19.png)  
&emsp; 扩容后，table 长度为32，新表的 n - 1 只比老表的 n - 1 在高位多了一个1(图中标红)。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-20.png)  
&emsp; 因为 2 个节点在老表是同一个索引位置，因此计算新表的索引位置时，只取决于新表在高位多出来的这一位(图中标红)，而这一位的值刚好等于 oldCap。  
&emsp; 因为只取决于这一位，所以只会存在两种情况：1)  (e.hash & oldCap) == 0 ，则新表索引位置为“原索引位置” ；2)(e.hash & oldCap) == 1，则新表索引位置为“原索引 + oldCap 位置”。  

#### 1.4.3.2. 具体源码

##### 1.4.3.2.1. resize()方法
```java
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;//原数组如果为null，则长度赋值0
    int oldThr = threshold;
    int newCap, newThr = 0;
    if (oldCap > 0) {//如果原数组长度大于0
        if (oldCap >= MAXIMUM_CAPACITY) {//数组大小如果已经大于等于最大值(2^30)
            threshold = Integer.MAX_VALUE;//修改阈值为int的最大值(2^31-1)，这样以后就不会扩容了
            return oldTab;
        }
        //原数组长度大于等于初始化长度16，并且原数组长度扩大1倍也小于2^30次方
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                oldCap >= DEFAULT_INITIAL_CAPACITY)
            newThr = oldThr << 1; // 阀值扩大1倍
    }
    else if (oldThr > 0) //旧阀值大于0，则将新容量直接等于就阀值 
        newCap = oldThr;
    else {//阀值等于0，oldCap也等于0(集合未进行初始化)
        newCap = DEFAULT_INITIAL_CAPACITY;//数组长度初始化为16
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);//阀值等于16*0.75=12
    }
    //计算新的阀值上限
    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    if (oldTab != null) {
        //把每个bucket都移动到新的buckets中
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                oldTab[j] = null;//元数据j位置置为null，便于垃圾回收
                if (e.next == null)//数组没有下一个引用(不是链表)
                    newTab[e.hash & (newCap - 1)] = e;
                else if (e instanceof TreeNode)//红黑树
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // preserve order
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        next = e.next;
                        //原索引
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                        //原索引+oldCap
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    //原索引放到bucket里
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }
                    //原索引+oldCap放到bucket里
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```

##### 1.4.3.2.2. split()，红黑树处理  
&emsp; 红黑树的拆分和链表的逻辑基本一致，不同的地方在于，重新映射后，会将红黑树拆分成两条链表，根据链表的长度，判断需不需要把链表重新进行树化。  
<!-- 
https://blog.csdn.net/m0_46657043/article/details/106574422
-->

```java
final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
    TreeNode<K,V> b = this;
    // Relink into lo and hi lists, preserving order
    // 和链表同样的套路，分成高位和低位
    TreeNode<K,V> loHead = null, loTail = null;
    TreeNode<K,V> hiHead = null, hiTail = null;
    int lc = 0, hc = 0;
    /**
      * TreeNode 是间接继承于 Node，保留了 next，可以像链表一样遍历
      * 这里的操作和链表的一毛一样
      */
    for (TreeNode<K,V> e = b, next; e != null; e = next) {
        next = (TreeNode<K,V>)e.next;
        e.next = null;
        // bit 就是 oldCap
        if ((e.hash & bit) == 0) {
            if ((e.prev = loTail) == null)
                loHead = e;
            else
            // 尾插
                loTail.next = e;
            loTail = e;
            ++lc;
        }
        else {
            if ((e.prev = hiTail) == null)
                hiHead = e;
            else
                hiTail.next = e;
            hiTail = e;
            ++hc;
        }
    }

    // 树化低位链表
    if (loHead != null) {
        // 如果 loHead 不为空，且链表长度小于等于 6，则将红黑树转成链表
        if (lc <= UNTREEIFY_THRESHOLD)
            tab[index] = loHead.untreeify(map);
        else {
            /**
              * hiHead == null 时，表明扩容后，
              * 所有节点仍在原位置，树结构不变，无需重新树化
              */
            tab[index] = loHead;
            if (hiHead != null) // (else is already treeified)
                loHead.treeify(tab);
        }
    }
    // 树化高位链表，逻辑与上面一致
    if (hiHead != null) {
        if (hc <= UNTREEIFY_THRESHOLD)
            tab[index + bit] = hiHead.untreeify(map);
        else {
            tab[index + bit] = hiHead;
            if (loHead != null)
                hiHead.treeify(tab);
        }
    }
}
```

-----

<!-- 
https://www.jianshu.com/p/87d2ef48e645

rehash时，链表怎么处理？  
&emsp; 正常是把所有元素都重新计算一下下标值，再决定放入哪个桶，JDK1.8优化成直接把链表拆成高位和低位两条，通过位运算来决定放在原索引处或者原索引加原数组长度的偏移量处。
rehash时，红黑树怎么处理？
&emsp; 红黑树的拆分和链表的逻辑基本一致，不同的地方在于，重新映射后，会将红黑树拆分成两条链表，根据链表的长度，判断需不需要把链表重新进行树化。   

-->

## 1.5. HashMap在JDK1.7和JDK1.8中的区别总结  

* 数组+链表改成了数组+链表或红黑树；  
* 链表的插入方式从头插法改成了尾插法，简单说就是插入时，如果数组位置上已经有元素，1.7将新元素放到数组中，原始节点作为新节点的后继节点，1.8遍历链表，将元素放置到链表的最后；  
* 扩容的时候1.7需要对原数组中的元素进行重新hash定位在新数组的位置，1.8采用更简单的判断逻辑，位置不变或索引+旧容量大小；  
* 在插入时，1.7先判断是否需要扩容，再插入，1.8先进行插入，插入完成再判断是否需要扩容；  

