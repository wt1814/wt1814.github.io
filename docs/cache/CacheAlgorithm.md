<!-- TOC -->

- [1. 缓存算法](#1-缓存算法)
    - [1.1. FIFO](#11-fifo)
    - [1.2. LRU](#12-lru)
        - [1.2.1. 基于哈希链表的LRU实现](#121-基于哈希链表的lru实现)
    - [1.3. LFU](#13-lfu)

<!-- /TOC -->

# 1. 缓存算法  
&emsp; 先进先出策略 FIFO（First In，First Out）、最少使用策略 LFU（Least Frequently Used）、最近最少使用策略 LRU（Least Recently Used）。  

## 1.1. FIFO
&emsp; 在FIFO Cache设计中，核心原则就是：如果一个数据最先进入缓存中，则应该最早淘汰掉。也就是说，当缓存满的时候，应当把最先进入缓存的数据给淘汰掉。在FIFO Cache中应该支持以下操作;  
&emsp; get(key)：如果Cache中存在该key，则返回对应的value值，否则，返回-1；  
&emsp; set(key,value)：如果Cache中存在该key，则重置value值；如果不存在该key，则将该key插入到到Cache中，若Cache已满，则淘汰最早进入Cache的数据。  
   
&emsp; 那么利用什么数据结构来实现呢？下面提供一种实现思路：  
&emsp; 利用一个双向链表保存数据，当来了新的数据之后便添加到链表末尾，如果Cache存满数据，则把链表头部数据删除，然后把新的数据添加到链表末尾。在访问数据的时候，如果在Cache中存在该数据的话，则返回对应的value值；否则返回-1。如果想提高访问效率，可以利用hashmap来保存每个key在链表中对应的位置。  

## 1.2. LRU  
&emsp; LRU是Least Recently Used的缩写，即最近最少使用。选择最近最久未使用的数据删除。  
&emsp; LRU Cache具备的操作：  
1. put(key, val) 方法插入新的或更新已有键值对，如果缓存已满的话，要删除那个最久没用过的键值对以腾出位置插入。  
2. get(key) 方法获取 key 对应的 val，如果 key 不存在则返回 -1。  

<!-- 
LRU是Least Recently Used的缩写，即最近最少使用。选择最近最久未使用的数据删除。
LRU是最近最少使用策略的缩写，是根据数据的历史访问记录来进行淘汰数据，其核心思想是“如果数据最近被访问过，那么将来被访问的几率也更高”。

API接口设计：
LRU Cache具备的操作：
1. put(key, val) 方法插入新的或更新已有键值对，如果缓存已满的话，要删除那个最久没用过的键值对以腾出位置插入。
2. get(key) 方法获取 key 对应的 val，如果 key 不存在则返回 -1。

LRU Cache具备的操作：
　　1）set(key,value)：如果key在hashmap中存在，则先重置对应的value值，然后获取对应的节点cur，将cur节点从链表删除，并移动到链表的头部；若果key在hashmap不存在，则新建一个节点，并将节点放到链表的头部。当Cache存满的时候，将链表最后一个节点删除即可。
2）get(key)：如果key在hashmap中存在，则把对应的节点放到链表头部，并返回对应的value值；如果不存在，则返回-1。


基于哈希链表的LRU实现  
　　那么有没有更好的实现办法呢？
　　那就是利用链表和hashmap。当需要插入新的数据项的时候，如果新数据项在链表中存在（一般称为命中），则把该节点移到链表头部，如果不存在，则新建一个节点，放到链表头部，若缓存满了，则把链表最后一个节点删除即可。在访问数据的时候，如果数据项在链表中存在，则把该节点移到链表头部，否则返回-1。这样一来在链表尾部的节点就是最近最久未访问的数据项。


LRU 缓存算法的核心数据结构就是哈希链表，双向链表和哈希表的结合体。哈希表查找快，但是数据无固定顺序；链表有顺序之分，插入删除快，但是查找慢。


LRU算法实现：
方式一：https://mp.weixin.qq.com/s/8vtFLx6yH-2epvmKXrDiqg

方式二：

方式三：基于JDK的haxi 
-->

### 1.2.1. 基于哈希链表的LRU实现    
&emsp; **<font color = "red">LRU缓存算法的核心数据结构就是哈希链表，双向链表和哈希表的结合体。哈希表查找快，但是数据无固定顺序；链表有顺序之分，插入删除快，但是查找慢。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-4.png)  

&emsp; LRU算法实现：  
&emsp; 方式一：  

```java
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache3<K, V> {
    /**
     * 最大缓存大小
     */
    private int cacheSize;
    private LinkedHashMap<K, V> cacheMap;

    public LRUCache3(int cacheSize) {
        this.cacheSize = cacheSize;
        cacheMap = new LinkedHashMap(16, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                if (cacheSize + 1 == cacheMap.size()) {
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    public void put(K key, V value) {
        cacheMap.put(key, value);
    }

    public V get(K key) {
        return cacheMap.get(key);
    }
}
```  

&emsp; 手写LRU算法    
&emsp; 基于LinkedHashMap实现一个简单版本的LRU算法。  

```java
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int CACHE_SIZE;
    /**
     * @param cacheSize 缓存大小
     */
    // true表示让linkedHashMap按照访问顺序来进行排序，最近访问的放在头部，最老访问的放在尾部。
    public LRUCache(int cacheSize) {
        super((int) Math.ceil(cacheSize / 0.75) + 1, 0.75f, true);
        CACHE_SIZE = cacheSize;
    }

    @Override
    // 当map中的数据量大于指定的缓存个数的时候，就自动删除最老的数据。
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > CACHE_SIZE;
    }
}
```

```java
public class LRUCache<k, v> {
    //容量
    private int capacity;
    //当前有多少节点的统计
    private int count;
    //缓存节点
    private Map<k, node> nodeMap;
    private Node head;
    private Node tail;

    public LRUCache(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException(String.valueOf(capacity));
        }
        this.capacity = capacity;
        this.nodeMap = new HashMap<>();
        //初始化头节点和尾节点，利用哨兵模式减少判断头结点和尾节点为空的代码
        Node headNode = new Node(null, null);
        Node tailNode = new Node(null, null);
        headNode.next = tailNode;
        tailNode.pre = headNode;
        this.head = headNode;
        this.tail = tailNode;
    }

    public void put(k key, v value) {
        Node node = nodeMap.get(key);
        if (node == null) {
            if (count >= capacity) {
                //先移除一个节点
                removeNode();
            }
            node = new Node<>(key, value);
            //添加节点
            addNode(node);
        } else {
            //移动节点到头节点
            moveNodeToHead(node);
        }
    }

    public Node get(k key) {
        Node node = nodeMap.get(key);
        if (node != null) {
            moveNodeToHead(node);
        }
        return node;
    }

    private void removeNode() {
        Node node = tail.pre;
        //从链表里面移除
        removeFromList(node);
        nodeMap.remove(node.key);
        count--;
    }

    private void removeFromList(Node node) {
        Node pre = node.pre;
        Node next = node.next;

        pre.next = next;
        next.pre = pre;

        node.next = null;
        node.pre = null;
    }

    private void addNode(Node node) {
        //添加节点到头部
        addToHead(node);
        nodeMap.put(node.key, node);
        count++
    }

    private void addToHead(Node node) {
        Node next = head.next;
        next.pre = node;
        node.next = next;
        node.pre = head;
        head.next = node;
    }

    public void moveNodeToHead(Node node) {
        //从链表里面移除
        removeFromList(node);
        //添加节点到头部
        addToHead(node);
    }

    class Node<k, v> {
        k key;
        v value;
        Node pre;
        Node next;

        public Node(k key, v value) {
            this.key = key;
            this.value = value;
        }
    }
}
```


## 1.3. LFU
&emsp; LFU（Least Frequently Used）最近最少使用算法。它是基于“如果一个数据在最近一段时间内使用次数很少，那么在将来一段时间内被使用的可能性也很小”的思路。  
&emsp; 注意LFU和LRU算法的不同之处，LRU的淘汰规则是基于访问时间，而LFU是基于访问次数的。  
&emsp; LFU Cache应该支持的操作为：  
&emsp; get(key)：如果Cache中存在该key，则返回对应的value值，否则，返回-1；  
&emsp; set(key,value)：如果Cache中存在该key，则重置value值；如果不存在该key，则将该key插入到到Cache中，若Cache已满，则淘汰最少访问的数据。  

&emsp; 为了能够淘汰最少使用的数据，因此LFU算法最简单的一种设计思路就是 利用一个数组存储 数据项，用hashmap存储每个数据项在数组中对应的位置，然后为每个数据项设计一个访问频次，当数据项被命中时，访问频次自增，在淘汰的时候淘汰访问频次最少的数据。这样一来的话，在插入数据和访问数据的时候都能达到O(1)的时间复杂度，在淘汰数据的时候，通过选择算法得到应该淘汰的数据项在数组中的索引，并将该索引位置的内容替换为新来的数据内容即可，这样的话，淘汰数据的操作时间复杂度为O(n)。  
&emsp; 另外还有一种实现思路就是利用 小顶堆+hashmap，小顶堆插入、删除操作都能达到O(logn)时间复杂度，因此效率相比第一种实现方法更加高效。  