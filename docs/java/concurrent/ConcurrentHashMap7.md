<!-- TOC -->

- [1. Java7 ConcurrentHashMap](#1-java7-concurrenthashmap)
    - [1.1. 存储结构](#11-存储结构)
        - [1.1.1. 静态内部类 Segment](#111-静态内部类-segment)
        - [1.1.2. 静态内部类HashEntry](#112-静态内部类hashentry)
    - [1.2. 构造函数](#12-构造函数)
    - [1.3. 成员方法](#13-成员方法)
        - [1.3.1. put()方法](#131-put方法)
            - [1.3.1.1. 扩容 rehash](#1311-扩容-rehash)
        - [1.3.2. get()方法](#132-get方法)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 在JDK1.7中，ConcurrentHashMap类采用了分段锁的思想，Segment(段) + HashEntry(哈希条目) + ReentrantLock。  
2. Segment继承ReentrantLock(可重入锁)，从而实现并发控制。Segment的个数一旦初始化就不能改变，默认Segment的个数是16个，也可以认为ConcurrentHashMap默认支持最多16个线程并发。  

# 1. Java7 ConcurrentHashMap  
## 1.1. 存储结构  
&emsp; 在 JDK1.7 中，ConcurrentHashMap 类采用了分段锁的思想，Segment(段) + HashEntry(哈希条目) + ReentrantLock。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-10.png)  

&emsp; 将HashMap进行切割，把HashMap中的哈希数组切分成Segment；每一个Segment是一个类似于HashMap的结构，包含有若干个HashEntry。  
1. <font color = "red">Segment继承ReentrantLock(可重入锁)，从而实现并发控制。 Segment的个数一旦初始化就不能改变，默认Segment的个数是16个，也可以认为ConcurrentHashMap默认支持最多16个线程并发。 </font> 
2. HashEntry用来封装映射表的键-值对；  

### 1.1.1. 静态内部类 Segment  
&emsp; Segment 类继承于ReentrantLock类，从而使得Segment对象能充当可重入锁的角色。一个 Segment 就是一个子哈希表，Segment 里维护了一个 HashEntry 数组，并发环境下，对于不同 Segment 的数据进行操作是不用考虑锁竞争的。  

### 1.1.2. 静态内部类HashEntry  
&emsp; HashEntry 是目前最小的逻辑处理单元。一个ConcurrentHashMap 维护一个 Segment 数组，一个Segment维护一个 HashEntry 数组。  

## 1.2. 构造函数  

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

## 1.3. 成员方法  

### 1.3.1. put()方法  

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

#### 1.3.1.1. 扩容 rehash  
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

### 1.3.2. get()方法  
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
