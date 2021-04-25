

<!-- TOC -->

- [1. ConcurrentHashMap](#1-concurrenthashmap)
    - [1.1. Java8 ConcurrentHashMap](#11-java8-concurrenthashmap)
        - [1.1.1. 存储结构](#111-存储结构)
        - [1.1.2. 成员方法](#112-成员方法)
            - [1.1.2.1. put()方法](#1121-put方法)
                - [1.1.2.1.1. 协助扩容helpTransfer](#11211-协助扩容helptransfer)
                - [1.1.2.1.2. treeifyBin](#11212-treeifybin)
                - [1.1.2.1.3. addCount](#11213-addcount)
            - [1.1.2.2. get()方法](#1122-get方法)
                - [1.1.2.2.1. get()流程](#11221-get流程)
                - [1.1.2.2.2. get()为什么不需要加锁？](#11222-get为什么不需要加锁)
    - [1.2. Java7 ConcurrentHashMap](#12-java7-concurrenthashmap)
        - [1.2.1. 存储结构](#121-存储结构)
            - [1.2.1.1. 静态内部类 Segment](#1211-静态内部类-segment)
            - [1.2.1.2. 静态内部类HashEntry](#1212-静态内部类hashentry)
        - [1.2.2. 构造函数](#122-构造函数)
        - [1.2.3. 成员方法](#123-成员方法)
            - [1.2.3.1. put()方法](#1231-put方法)
                - [1.2.3.1.1. 扩容 rehash](#12311-扩容-rehash)
            - [1.2.3.2. get()方法](#1232-get方法)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. ConcurrentHashMap，JDK1.8  
    &emsp; **<font color = "red">从jdk1.8开始，ConcurrentHashMap类取消了Segment分段锁，采用Node + CAS + Synchronized来保证并发安全。</font>**  
    &emsp; **<font color = "clime">jdk1.8中的ConcurrentHashMap中synchronized只锁定当前链表或红黑树的首节点，只要节点hash不冲突，就不会产生并发，相比JDK1.7的ConcurrentHashMap效率又提升了许多。</font>**  
    1. **<font color = "clime">put()流程：</font>**
        1. 根据 key 计算出 hashcode 。  
        2. 整个过程自旋添加节点。  
        2. 判断是否需要进行初始化数组。  
        3. <font color = "red">为当前key定位出Node，如果为空表示此数组下无节点，当前位置可以直接写入数据，利用CAS尝试写入，失败则进入下一次循环。</font>  
        4. **<font color = "blue">如果当前位置的hashcode == MOVED == -1，表示其他线程插入成功正在进行扩容，则当前线程帮助进行扩容。</font>**  
        5. <font color = "red">如果都不满足，则利用synchronized锁写入数据。</font>  
        6. 如果数量大于TREEIFY_THRESHOLD则要转换为红黑树。 
        7. 最后通过addCount来增加ConcurrentHashMap的长度，并且还可能触发扩容操作。  
        
    2. **<font color = "clime">get()流程：为什么ConcurrentHashMap的读操作不需要加锁？</font>**  

        * 在1.8中ConcurrentHashMap的get操作全程不需要加锁，这也是它比其他并发集合（比如hashtable、用Collections.synchronizedMap()包装的hashmap）安全效率高的原因之一。  
        * get操作全程不需要加锁是因为Node的成员val是用volatile修饰的，和数组用volatile修饰没有关系。  
        * 数组用volatile修饰主要是保证在数组扩容的时候保证可见性。


# 1. ConcurrentHashMap   
<!-- 
ConcurrentHashMap中有十个提升性能的细节，你都知道吗？
https://mp.weixin.qq.com/s/vZZQeWaKQ2pbUDyyqpzunQ

ConcurrentHashMap线程安全吗 
https://mp.weixin.qq.com/s/ZCQPrgW6iv2IP_3RKk016g
--> 

## 1.1. Java8 ConcurrentHashMap  
<!-- 
阿里十年架构师，教你深度分析ConcurrentHashMap原理分析 
https://www.sohu.com/a/320372210_120176035
一文看懂ConcurrentHashMap
https://segmentfault.com/a/1190000022279729
-->
&emsp; **<font color = "red">从jdk1.8开始，ConcurrentHashMap类取消了Segment分段锁，采用Node + CAS + Synchronized来保证并发安全。</font>**  
&emsp; **<font color = "clime">jdk1.8中的ConcurrentHashMap中synchronized只锁定当前链表或红黑树的首节点，只要节点hash不冲突，就不会产生并发，相比JDK1.7的ConcurrentHashMap效率又提升了许多。</font>**  

### 1.1.1. 存储结构  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-11.png)  

&emsp; ConcurrentHashMap数据结构跟 jdk1.8 中 HashMap 结构保持一致，都是数组 + 链表 + 红黑树。和 HashMap1.8 相同的一些地方：  

* 底层数据结构一致  
* HashMap初始化是在第一次put元素的时候进行的，而不是init  
* HashMap的底层数组长度总是为2的整次幂  
* 默认树化的阈值为 8，而链表化的阈值为 6  
* hash算法也很类似，但多了一步& HASH_BITS，该步是为了消除最高位上的负符号。hash的负在ConcurrentHashMap中有特殊意义，表示在扩容或者是树节点。  

    ```java
    static final int HASH_BITS = 0x7fffffff; // usable bits of normal node hash

    static final int spread(int h) {
            return (h ^ (h >>> 16)) & HASH_BITS;
    }
    ```

### 1.1.2. 成员方法  
#### 1.1.2.1. put()方法  

&emsp; **<font color = "clime">put()流程：</font>**
1. 根据 key 计算出 hashcode 。  
2. 整个过程自旋添加节点。  
2. 判断是否需要进行初始化数组。  
3. <font color = "red">为当前 key 定位出Node，如果为空表示此数组下无节点，当前位置可以直接写入数据，利用 CAS 尝试写入，失败则入下一次循环。</font>  
4. **<font color = "blue">如果当前位置的 hashcode == MOVED == -1，表示其他线程插入成功正在进行扩容，则当前线程帮助进行扩容。</font>**  
5. <font color = "red">如果都不满足，则利用 synchronized 锁写入数据。</font>  
6. 如果数量大于 TREEIFY_THRESHOLD 则要转换为红黑树。 
7. 最后通过addCount来增加ConcurrentHashMap的长度，并且还可能触发扩容操作。  

```java
/** Implementation for put and putIfAbsent */
final V putVal(K key, V value, boolean onlyIfAbsent) {
    if (key == null || value == null) throw new NullPointerException();
    int hash = spread(key.hashCode());  //计算 hash 值
    int binCount = 0;  //用来记录链表的长度
    for (Node<K,V>[] tab = table;;) {  //这里其实就是自旋操作，当出现线程竞争时不断自旋
        Node<K,V> f; int n, i, fh;
        //【put第一阶段】
        if (tab == null || (n = tab.length) == 0) //如果数组为空，则进行数组初始化
            tab = initTable();  //初始化数组
        //通过 hash 值对应的数组下标得到第一个节点; 以 volatile 读的方式来读取 table 数组中的元素，保证每次拿到的数据都是最新的
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            //如果该下标返回的节点为空，则直接通过 cas 将新的值封装成 node 插入即可；如果 cas 失败，说明存在竞争，则进入下一次循环
            if (casTabAt(tab, i, null,new Node<K,V>(hash, key, value, null)))
                break;   // no lock when adding to empty bin
        }
        // -----  假如在上面这段代码中存在两个线程，在不加锁的情况下：线程 A 成功执行 casTabAt 操作后，
        //随后的线程 B 可以通过 tabAt 方法立刻看到 table[i]的改变。原因如下：线程 A 的casTabAt 操作，
        //具有 volatile 读写相同的内存语义，根据 volatile 的 happens-before 规则：
        //线程 A 的 casTabAt 操作，一定对线程 B 的 tabAt 操作可见

        //如果对应的节点存在，判断这个节点的 hash 是不是等于 MOVED(-1)，说明当前节点是ForwardingNode 节点，
        //意味着有其他线程正在进行扩容，那么当前线程直接帮助它进行扩容，因此调用 helpTransfer方法
        // 【put 方法第三阶段】
        else if ((fh = f.hash) == MOVED)
            tab = helpTransfer(tab, f);
        //【put 方法第四阶段】
        //这个方法的主要作用是，如果被添加的节点的位置已经存在节点的时候，需要以链表的方式加入到节点中
        // 如果当前节点已经是一颗红黑树，那么就会按照红黑树的规则将当前节点加入到红黑树中
        else { //进入到这个分支，说明 f 是当前 nodes 数组对应位置节点的头节点，并且不为空
            V oldVal = null;
            synchronized (f) { //给对应的头结点加锁
                if (tabAt(tab, i) == f) {//再次判断对应下标位置是否为 f 节点
                    if (fh >= 0) { //头结点的 hash 值大于 0，说明是链表
                        binCount = 1; //用来记录链表的长度
                        for (Node<K,V> e = f;; ++binCount) { //遍历链表
                            //如果发现相同的 key，则判断是否需要进行值的覆盖
                            K ek;
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                    (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                if (!onlyIfAbsent) //默认情况下，直接覆盖旧的值
                                    e.val = value;
                                break;
                            }
                            //一直遍历到链表的最末端，直接把新的值加入到链表的最后面
                            Node<K,V> pred = e;
                            if ((e = e.next) == null) {
                                pred.next = new Node<K,V>(hash, key,
                                                            value, null);
                                break;
                            }
                        }
                    }
                    //如果当前的 f 节点是一颗红黑树
                    else if (f instanceof TreeBin) {
                        Node<K,V> p;
                        binCount = 2;
                        //则调用红黑树的插入方法插入新的值
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                        value)) != null) {
                            oldVal = p.val; //同样，如果值已经存在，则直接替换
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }
            if (binCount != 0) {
                if (binCount >= TREEIFY_THRESHOLD)
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    //【put 第二阶段】
    /**
        * 在putVal方法执行完成以后，会通过addCount来增加ConcurrentHashMap中的元素个数1，并且还会可能触发扩容操作。这里会有两个非常经典的设计
        * 这里会有两个非常经典的设计
        * 1. 高并发下的扩容
        * 2. 如何保证 addCount 的数据安全性以及性能
        */
    addCount(1L, binCount);
    return null;
}
```

##### 1.1.2.1.1. 协助扩容helpTransfer  

```java
final Node<K,V>[] helpTransfer(Node<K,V>[] tab, Node<K,V> f) {
    Node<K,V>[] nextTab; int sc;
    // 判断此时是否仍然在执行扩容,nextTab=null 的时候说明扩容已经结束了
    if (tab != null && (f instanceof ForwardingNode) &&
            (nextTab = ((ForwardingNode<K,V>)f).nextTable) != null) {
        int rs = resizeStamp(tab.length);//生成扩容戳
        while (nextTab == nextTable && table == tab &&
                (sc = sizeCtl) < 0) {//说明扩容还未完成的情况下不断循环来尝试将当前线程加入到扩容操作中
            //下面部分的整个代码表示扩容结束，直接退出循环
            //transferIndex<=0 表示所有的 Node 都已经分配了线程
            //sc=rs+MAX_RESIZERS 表示扩容线程数达到最大扩容线程数
            //sc >>> RESIZE_STAMP_SHIFT !=rs， 如果在同一轮扩容中，那么 sc 无符号右移比较高位和 rs 的值，那么应该是相等的。如果不相等，说明扩容结束了
            //sc==rs+1 表示扩容结束
            if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                    sc == rs + MAX_RESIZERS || transferIndex <= 0)
                break;//跳出循环
            if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {//在低 16 位
                上增加扩容线程数
                transfer(tab, nextTab);//帮助扩容
                break; } }
        return nextTab;
    }
    return table;//返回新的数组
}
```

##### 1.1.2.1.2. treeifyBin  
&emsp; 在 putVal 的最后部分，有一个判断，如果链表长度大于 8，那么就会触发扩容或者红黑树的转化操作。  

```java
private final void treeifyBin(Node<K,V>[] tab, int index) {
    Node<K,V> b; int n, sc;
    if (tab != null) {
        if ((n = tab.length) < MIN_TREEIFY_CAPACITY) //tab 的长度是不是小于 64，如果是，则执行扩容
            tryPresize(n << 1);
        else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {//否则，将当前链表转化为红黑树结构存储
            synchronized (b) {// 将链表转换成红黑树
                if (tabAt(tab, index) == b) {
                    TreeNode<K,V> hd = null, tl = null;
                    for (Node<K,V> e = b; e != null; e = e.next) {
                        TreeNode<K,V> p =
                                new TreeNode<K,V>(e.hash, e.key, e.val,
                                        null, null);
                        if ((p.prev = tl) == null)
                            hd = p;
                        else
                            tl.next = p;
                        tl = p;
                    }
                    setTabAt(tab, index, new TreeBin<K,V>(hd));
                }
            }
        }
    }
}
```

##### 1.1.2.1.3. addCount
&emsp; 在putVal最后调用 addCount 的时候，传递了两个参数，分别是 1 和 binCount(链表长度)。  

```java
private final void addCount(long x, int check) {
    CounterCell[] as; long b, s;
    /* 判断 counterCells 是否为空，
    1. 如果为空，就通过 cas 操作尝试修改 baseCount 变量，对这个变量进行原子累加操
    作(做这个操作的意义是：如果在没有竞争的情况下，仍然采用 baseCount 来记录元素个
            数)
    2. 如果 cas 失败说明存在竞争，这个时候不能再采用 baseCount 来累加，而是通过
    CounterCell 来记录*/
    if ((as = counterCells) != null ||
            !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x))
    {
        CounterCell a; long v; int m;
        boolean uncontended = true;//是否冲突标识，默认为没有冲突
        /* 这里有几个判断
        1. 计数表为空则直接调用 fullAddCount
        2. 从计数表中随机取出一个数组的位置为空，直接调用 fullAddCount
        3. 通过 CAS 修改 CounterCell 随机位置的值，如果修改失败说明出现并发情况(这里又
        用到了一种巧妙的方法)，调用 fullAndCount
        Random 在线程并发的时候会有性能问题以及可能会产生相同的随机
        数,ThreadLocalRandom.getProbe 可以解决这个问题，并且性能要比 Random 高*/
        if (as == null || (m = as.length - 1) < 0 ||
                (a = as[ThreadLocalRandom.getProbe() & m]) == null ||
                !(uncontended =
                        U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
            fullAddCount(x, uncontended);//执行 fullAddCount 方法
            return; }
        if (check <= 1)//链表长度小于等于 1，不需要考虑扩容
            return;
        s = sumCount();//统计 ConcurrentHashMap 元素个数
    }//….
}
```

#### 1.1.2.2. get()方法  
<!-- 
★★★为什么ConcurrentHashMap的读操作不需要加锁？ 
https://mp.weixin.qq.com/s/3FCg-9kPjSAR0tN6xLW6tw
 
-->
##### 1.1.2.2.1. get()流程
&emsp; **<font color = "clime">get()流程：</font>**  
1. 根据 hash 值计算位置。  
2. 查找到指定位置，如果头节点就是要找的，直接返回它的 value。  
3. 如果头节点 hash 值小于 0 ，说明正在扩容或者是红黑树，进行查找。  
4. 如果是链表，遍历查找。    

```java
public V get(Object key) {
    Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
    // key 所在的 hash 位置
    int h = spread(key.hashCode());
    if ((tab = table) != null && (n = tab.length) > 0 &&
            (e = tabAt(tab, (n - 1) & h)) != null) {
        // 如果指定位置元素存在，头结点hash值相同
        if ((eh = e.hash) == h) {
            if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                // key hash 值相等，key值相同，直接返回元素 value
                return e.val;
        }
        //hash值为负值表示正在扩容，这个时候查的是ForwardingNode的find方法来定位到nextTable来
        //eh=-1，说明该节点是一个ForwardingNode，正在迁移，此时调用ForwardingNode的find方法去nextTable里找。
        //eh=-2，说明该节点是一个TreeBin，此时调用TreeBin的find方法遍历红黑树，由于红黑树有可能正在旋转变色，所以find里会有读写锁。
        //eh>=0，说明该节点下挂的是一个链表，直接遍历该链表即可。
        
        else if (eh < 0)
            // 头结点hash值小于0，说明正在扩容或者是红黑树，find查找
            return (p = e.find(h, key)) != null ? p.val : null;
        while ((e = e.next) != null) {
            // 是链表，遍历查找
            if (e.hash == h &&
                    ((ek = e.key) == key || (ek != null && key.equals(ek))))
                return e.val;
        }
    }
    return null;
}
```

##### 1.1.2.2.2. get()为什么不需要加锁？  
&emsp; 用volatile修饰的Node。  
&emsp; get操作可以无锁是由于Node的元素val和指针next是用volatile修饰的，在多线程环境下线程A修改结点的val或者新增节点的时候是对线程B可见的。    

```java
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    volatile V val;
    volatile Node<K,V> next;
```

&emsp; 用volatile修饰的table属性`transient volatile Node<K,V>[] table;` ，是为了使得Node数组在扩容的时候对其他线程具有可见性而加的volatile。  

## 1.2. Java7 ConcurrentHashMap  
### 1.2.1. 存储结构  
&emsp; 在 JDK1.7 中，ConcurrentHashMap 类采用了分段锁的思想，Segment(段) + HashEntry(哈希条目) + ReentrantLock。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-10.png)  

&emsp; 将HashMap进行切割，把HashMap中的哈希数组切分成Segment；每一个Segment是一个类似于HashMap的结构，包含有若干个HashEntry。  
1. <font color = "red">Segment继承ReentrantLock(可重入锁)，从而实现并发控制。 Segment的个数一旦初始化就不能改变，默认Segment的个数是16个，也可以认为ConcurrentHashMap默认支持最多16个线程并发。 </font> 
2. HashEntry用来封装映射表的键-值对；  

#### 1.2.1.1. 静态内部类 Segment  
&emsp; Segment 类继承于ReentrantLock类，从而使得Segment对象能充当可重入锁的角色。一个 Segment 就是一个子哈希表，Segment 里维护了一个 HashEntry 数组，并发环境下，对于不同 Segment 的数据进行操作是不用考虑锁竞争的。  

#### 1.2.1.2. 静态内部类HashEntry  
&emsp; HashEntry 是目前最小的逻辑处理单元。一个ConcurrentHashMap 维护一个 Segment 数组，一个Segment维护一个 HashEntry 数组。  

### 1.2.2. 构造函数  

```java
public ConcurrentHashMap() {
    //DEFAULT_INITIAL_CAPACITY，默认初始容量16
    //DEFAULT_LOAD_FACTOR，默认负载因子0.75
    //DEFAULT_CONCURRENCY_LEVEL，默认并发级别16
    this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
}

@SuppressWarnings("unchecked")
public ConcurrentHashMap(int initialCapacity,float loadFactor, int concurrencyLevel) {
    // 参数校验
    if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0)
        throw new IllegalArgumentException();
    // 校验并发级别大小，大于 1<<16，重置为 65536
    if (concurrencyLevel > MAX_SEGMENTS)
        concurrencyLevel = MAX_SEGMENTS;
    // Find power-of-two sizes best matching arguments
    // 2的多少次方
    int sshift = 0;
    int ssize = 1;
    // 这个循环可以找到 concurrencyLevel 之上最近的 2的次方值
    while (ssize < concurrencyLevel) {
        ++sshift;
        ssize <<= 1;
    }
    // 记录段偏移量
    this.segmentShift = 32 - sshift;
    // 记录段掩码
    this.segmentMask = ssize - 1;
    // 设置容量
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    // c = 容量 / ssize ，默认 16 / 16 = 1，这里是计算每个 Segment 中的类似于 HashMap 的容量
    int c = initialCapacity / ssize;
    if (c * ssize < initialCapacity)
        ++c;
    int cap = MIN_SEGMENT_TABLE_CAPACITY;
    //Segment 中的类似于 HashMap 的容量至少是2或者2的倍数
    while (cap < c)
        cap <<= 1;
    // create segments and segments[0]
    // 创建 Segment 数组，设置 segments[0]
    Segment<K,V> s0 = new Segment<K,V>(loadFactor, (int)(cap * loadFactor),
            (HashEntry<K,V>[])new HashEntry[cap]);
    Segment<K,V>[] ss = (Segment<K,V>[])new Segment[ssize];
    UNSAFE.putOrderedObject(ss, SBASE, s0); // ordered write of segments[0]
    this.segments = ss;
}
```

### 1.2.3. 成员方法  

#### 1.2.3.1. put()方法  

```java
public V put(K key, V value) {
    Segment<K,V> s;
    if (value == null)
        throw new NullPointerException();
    int hash = hash(key);
    // hash 值无符号右移 28位(初始化时获得)，然后与 segmentMask=15 做与运算
    // 其实也就是把高4位与segmentMask(1111)做与运算
    int j = (hash >>> segmentShift) & segmentMask;
    if ((s = (Segment<K,V>)UNSAFE.getObject          // nonvolatile; recheck
            (segments, (j << SSHIFT) + SBASE)) == null) //  in ensureSegment
        // 如果查找到的 Segment 为空，初始化
        s = ensureSegment(j);
    return s.put(key, hash, value, false);
}
```

&emsp; 从源码可以看出，这部分的 put 操作主要分两步：  
1. 定位segment并确保定位的Segment已初始化 
2. 调用 Segment的 put 方法。  

&emsp; Segment的 put方法实现插入元素，源码如下：  

```java
final V put(K key, int hash, V value, boolean onlyIfAbsent) {
    // 获取 ReentrantLock 独占锁，获取不到，scanAndLockForPut 获取。
    HashEntry<K,V> node = tryLock() ? null : scanAndLockForPut(key, hash, value);
    V oldValue;
    try {
        HashEntry<K,V>[] tab = table;
        // 计算要put的数据位置
        int index = (tab.length - 1) & hash;
        // CAS 获取 index 坐标的值
        HashEntry<K,V> first = entryAt(tab, index);
        for (HashEntry<K,V> e = first;;) {
            if (e != null) {
                // 检查是否 key 已经存在，如果存在，则遍历链表寻找位置，找到后替换 value
                K k;
                if ((k = e.key) == key ||
                        (e.hash == hash && key.equals(k))) {
                    oldValue = e.value;
                    if (!onlyIfAbsent) {
                        e.value = value;
                        ++modCount;
                    }
                    break;
                }
                e = e.next;
            }
            else {
                // first 有值没说明 index 位置已经有值了，有冲突，链表头插法。
                if (node != null)
                    node.setNext(first);
                else
                    node = new HashEntry<K,V>(hash, key, value, first);
                int c = count + 1;
                // 容量大于扩容阀值，小于最大容量，进行扩容
                if (c > threshold && tab.length < MAXIMUM_CAPACITY)
                    rehash(node);
                else
                    // index 位置赋值 node，node 可能是一个元素，也可能是一个链表的表头
                    setEntryAt(tab, index, node);
                ++modCount;
                count = c;
                oldValue = null;
                break;
            }
        }
    } finally {
        unlock();
    }
    return oldValue;
}
```

##### 1.2.3.1.1. 扩容 rehash  
&emsp; ConcurrentHashMap 的扩容只会扩容到原来的两倍。原数组里的数据移动到新的数组时，位置要么不变，要么变为 index+ oldSize，参数里的 node 会在扩容之后使用链表头插法插入到指定位置。  

```java
private void rehash(HashEntry<K,V> node) {
    HashEntry<K,V>[] oldTable = table;
    // 老容量
    int oldCapacity = oldTable.length;
    // 新容量，扩大两倍
    int newCapacity = oldCapacity << 1;
    // 新的扩容阀值 
    threshold = (int)(newCapacity * loadFactor);
    // 创建新的数组
    HashEntry<K,V>[] newTable = (HashEntry<K,V>[]) new HashEntry[newCapacity];
    // 新的掩码，默认2扩容后是4，-1是3，二进制就是11。
    int sizeMask = newCapacity - 1;
    for (int i = 0; i < oldCapacity ; i++) {
        // 遍历老数组
        HashEntry<K,V> e = oldTable[i];
        if (e != null) {
            HashEntry<K,V> next = e.next;
            // 计算新的位置，新的位置只可能是不便或者是老的位置+老的容量。
            int idx = e.hash & sizeMask;
            if (next == null)   //  Single node on list
                // 如果当前位置还不是链表，只是一个元素，直接赋值
                newTable[idx] = e;
            else { // Reuse consecutive sequence at same slot
                // 如果是链表了
                HashEntry<K,V> lastRun = e;
                int lastIdx = idx;
                // 新的位置只可能是不便或者是老的位置+老的容量。
                // 遍历结束后，lastRun 后面的元素位置都是相同的
                for (HashEntry<K,V> last = next; last != null; last = last.next) {
                    int k = last.hash & sizeMask;
                    if (k != lastIdx) {
                        lastIdx = k;
                        lastRun = last;
                    }
                }
                // ，lastRun 后面的元素位置都是相同的，直接作为链表赋值到新位置。
                newTable[lastIdx] = lastRun;
                // Clone remaining nodes
                for (HashEntry<K,V> p = e; p != lastRun; p = p.next) {
                    // 遍历剩余元素，头插法到指定 k 位置。
                    V v = p.value;
                    int h = p.hash;
                    int k = h & sizeMask;
                    HashEntry<K,V> n = newTable[k];
                    newTable[k] = new HashEntry<K,V>(h, p.key, v, n);
                }
            }
        }
    }
    // 头插法插入新的节点
    int nodeIndex = node.hash & sizeMask; // add the new node
    node.setNext(newTable[nodeIndex]);
    newTable[nodeIndex] = node;
    table = newTable;
}
```

&emsp; 最后的两个 for 循环，这里第一个 for 是为了寻找这样一个节点，这个节点后面的所有 next 节点的新位置都是相同的。然后把这个作为一个链表赋值到新位置。第二个 for 循环是为了把剩余的元素通过头插法插入到指定位置链表。这样实现的原因可能是基于概率统计。  

#### 1.2.3.2. get()方法  
&emsp; get方法无需加锁。由于其中涉及到的共享变量都使用volatile修饰，volatile可以保证内存可见性，所以不会读取到过期数据。get 方法只需要两步即可：  
1. 计算得到 key 的存放位置。  
2. 遍历指定位置查找相同 key 的 value 值。  

```java
public V get(Object key) {
    Segment<K,V> s; // manually integrate access methods to reduce overhead
    HashEntry<K,V>[] tab;
    int h = hash(key);
    long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;
    // 计算得到 key 的存放位置
    if ((s = (Segment<K,V>)UNSAFE.getObjectVolatile(segments, u)) != null &&
            (tab = s.table) != null) {
        for (HashEntry<K,V> e = (HashEntry<K,V>) UNSAFE.getObjectVolatile
                (tab, ((long)(((tab.length - 1) & h)) << TSHIFT) + TBASE);
             e != null; e = e.next) {
            // 如果是链表，遍历查找到相同 key 的 value。
            K k;
            if ((k = e.key) == key || (e.hash == h && key.equals(k)))
                return e.value;
        }
    }
    return null;
}
```
