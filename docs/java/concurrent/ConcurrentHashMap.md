

<!-- TOC -->

- [1. Java8 ConcurrentHashMap](#1-java8-concurrenthashmap)
    - [1.1. 存储结构](#11-存储结构)
    - [1.2. 成员方法](#12-成员方法)
        - [1.2.1. put()方法](#121-put方法)
            - [1.2.1.1. 协助扩容helpTransfer](#1211-协助扩容helptransfer)
                - [1.2.1.1.1. ~~帮助扩容transfer~~](#12111-帮助扩容transfer)
            - [1.2.1.2. treeifyBin](#1212-treeifybin)
            - [1.2.1.3. addCount](#1213-addcount)
        - [1.2.2. get()方法](#122-get方法)
            - [1.2.2.1. get()流程](#1221-get流程)
            - [1.2.2.2. ★★★get()为什么不需要加锁？](#1222-★★★get为什么不需要加锁)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; **<font color = "red">从jdk1.8开始，ConcurrentHashMap类取消了Segment分段锁，采用Node + CAS + Synchronized来保证并发安全。</font>**  
&emsp; **<font color = "clime">jdk1.8中的ConcurrentHashMap中synchronized只锁定当前链表或红黑树的首节点，只要节点hash不冲突，就不会产生并发，相比JDK1.7的ConcurrentHashMap效率又提升了许多。</font>**  
1. **<font color = "clime">put()流程：</font>**
    1. 根据 key 计算出 hashcode 。  
    2. 整个过程自旋添加节点。  
    2. 判断是否需要进行初始化数组。  
    3. <font color = "red">为当前key定位出Node，如果为空表示此数组下无节点，当前位置可以直接写入数据，利用CAS尝试写入，失败则进入下一次循环。</font>  
    4. **<font color = "blue">如果当前位置的hashcode == MOVED == -1，表示其他线程插入成功正在进行扩容，则当前线程`帮助进行扩容`。</font>**  
    5. <font color = "red">如果都不满足，则利用synchronized锁写入数据。</font>  
    6. 如果数量大于TREEIFY_THRESHOLD则要转换为红黑树。 
    7. 最后通过addCount来增加ConcurrentHashMap的长度，并且还可能触发扩容操作。  
2. 协助扩容  
&emsp; `ConcurrentHashMap并没有直接加锁，而是采用CAS实现无锁的并发同步策略，最精华的部分是它可以利用多线程来进行协同扩容。简单来说，它把Node数组当作多个线程之间共享的任务队列，然后通过维护一个指针来划分每个线程锁负责的区间，每个线程通过区间逆向遍历来实现扩容，一个已经迁移完的bucket会被替换为一个ForwardingNode节点，标记当前bucket已经被其他线程迁移完了。`   
3. **<font color = "clime">get()流程：为什么ConcurrentHashMap的读操作不需要加锁？</font>**  
    1. 在1.8中ConcurrentHashMap的get操作全程不需要加锁，这也是它比其他并发集合（比如hashtable、用Collections.synchronizedMap()包装的hashmap）安全效率高的原因之一。  
    2. get操作全程不需要加锁是因为Node的成员val是用volatile修饰的，和数组用volatile修饰没有关系。  
    3. 数组用volatile修饰主要是保证在数组扩容的时候保证可见性。  


# 1. Java8 ConcurrentHashMap  
<!--

ConcurrentHashMap如何实现扩容机制之学习笔记
https://blog.csdn.net/weixin_42022924/article/details/102865519

ConcurrentHashMap 扩容
https://blog.csdn.net/zzu_seu/article/details/106698150
https://blog.csdn.net/ZOKEKAI/article/details/90051567
https://blog.51cto.com/janephp/2413949

阿里十年架构师，教你深度分析ConcurrentHashMap原理分析 
https://www.sohu.com/a/320372210_120176035
一文看懂ConcurrentHashMap
https://segmentfault.com/a/1190000022279729
-->
<!-- 
ConcurrentHashMap 底层原理
https://blog.csdn.net/qq_33591903/article/details/106634270

ConcurrentHashMap中有十个提升性能的细节，你都知道吗？
https://mp.weixin.qq.com/s/vZZQeWaKQ2pbUDyyqpzunQ

ConcurrentHashMap线程安全吗 
https://mp.weixin.qq.com/s/ZCQPrgW6iv2IP_3RKk016g
--> 

&emsp; **<font color = "red">从jdk1.8开始，ConcurrentHashMap类取消了Segment分段锁，采用Node + CAS + Synchronized来保证并发安全。</font>**  
&emsp; **<font color = "clime">jdk1.8中的ConcurrentHashMap中synchronized只锁定当前链表或红黑树的首节点，只要节点hash不冲突，就不会产生并发，相比JDK1.7的ConcurrentHashMap效率又提升了许多。</font>**  

## 1.1. 存储结构  
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

## 1.2. 成员方法  
### 1.2.1. put()方法  

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

#### 1.2.1.1. 协助扩容helpTransfer  

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
            if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {//在低 16 位上增加扩容线程数
                transfer(tab, nextTab);//帮助扩容
                break; } }
        return nextTab;
    }
    return table;//返回新的数组
}
```

##### 1.2.1.1.1. ~~帮助扩容transfer~~  
<!-- 
https://blog.csdn.net/luzhensmart/article/details/105968886

https://blog.csdn.net/qq_33591903/article/details/106634270

-->
&emsp; 扩容是ConcurrentHashMap的精华之一，扩容操作的核心在于数据的转移，在单线程环境下数据的转移很简单，无非就是把旧数组中的数据迁移到新的数组。但是这在多线程环境下，在扩容的时候其他线程也可能正在添加元素，这时又触发了扩容怎么办？可能大家想到的第一个解决方案是加互斥锁，把转移过程锁住，虽然是可行的解决方案，但是会带来较大的性能开销。因为互斥锁会导致所有访问临界区的线程陷入到阻塞状态，持有锁的线程耗时越长，其他竞争线程就会一直被阻塞，导致吞吐量较低。而且还可能导致死锁。 而`ConcurrentHashMap并没有直接加锁，而是采用CAS实现无锁的并发同步策略，最精华的部分是它可以利用多线程来进行协同扩容。简单来说，它把Node数组当作多个线程之间共享的任务队列，然后通过维护一个指针来划分每个线程锁负责的区间，每个线程通过区间逆向遍历来实现扩容，一个已经迁移完的bucket会被替换为一个ForwardingNode节点，标记当前bucket已经被其他线程迁移完了。`  

#### 1.2.1.2. treeifyBin  
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

#### 1.2.1.3. addCount
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

### 1.2.2. get()方法  
<!-- 
★★★为什么ConcurrentHashMap的读操作不需要加锁？ 
https://mp.weixin.qq.com/s/3FCg-9kPjSAR0tN6xLW6tw
 
-->
#### 1.2.2.1. get()流程
&emsp; **<font color = "clime">get()流程：</font>**  
1. 根据hash值计算位置。  
2. 查找到指定位置，如果头节点就是要找的，直接返回它的value。  
3. 如果头节点hash值小于0，说明正在扩容或者是红黑树，进行查找。  
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

#### 1.2.2.2. ★★★get()为什么不需要加锁？  
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
