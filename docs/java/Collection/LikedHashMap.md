

<!-- TOC -->

- [1. LikedHashMap](#1-likedhashmap)
    - [1.1. ※※※LRU算法](#11-※※※lru算法)

<!-- /TOC -->

# 1. LikedHashMap  
<!-- 
https://mp.weixin.qq.com/s/pGNIEOGvOYDM5yiyMM8bRQ
-->

&emsp; LinkedHashMap 是 HashMap 的一个子类，保存了记录的插入顺序，在用 Iterator 遍历LinkedHashMap 时，先得到的记录肯定是先插入的，也可以在构造时带参数，按照访问次序排序。  

&emsp; LinkedHashMap怎么实现有序的？  
&emsp; LinkedHashMap内部维护了一个单链表，有头尾节点，同时LinkedHashMap节点Entry内部除了继承HashMap的Node属性，还有before 和 after用于标识前置节点和后置节点。可以实现按插入的顺序或访问顺序排序。  

```java
/**
 * The head (eldest) of the doubly linked list.
 */
transient LinkedHashMap.Entry<K,V> head;

/**
 * The tail (youngest) of the doubly linked list.
 */
transient LinkedHashMap.Entry<K,V> tail;
//链接新加入的p节点到链表后端
private void linkNodeLast(LinkedHashMap.Entry<K,V> p) {
    LinkedHashMap.Entry<K,V> last = tail;
    tail = p;
    if (last == null)
        head = p;
    else {
        p.before = last;
        last.after = p;
    }
}
//LinkedHashMap的节点类
static class Entry<K,V> extends HashMap.Node<K,V> {
    Entry<K,V> before, after;
    Entry(int hash, K key, V value, Node<K,V> next) {
        super(hash, key, value, next);
    }
}
```

## 1.1. ※※※LRU算法  
&emsp; LinkedHashMap的accessOrder字段，其为true可以将被访问的数据移动到链表的表尾。可以基于此特性来实现一个应用LRU策略的缓存。重载removeEldestEntry方法，当发现缓存空间已满时，即删除表头数据来释放空间。  

```java
/**
 * 基于LinkedHashMap的 LRU Cache 实现
 */
public class LRUCache<K,V> extends LinkedHashMap<K,V> {
    private int cacheSize;  // 缓存容量

    public LRUCache(int cacheSize) {
        super(16,0.75f, true);
        this.cacheSize = cacheSize;
    }

    /**
     * 超过Cache容量即会移除最近最少被使用的元素
     * @param eldest
     * @return
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return this.size() > cacheSize;
    }
}
```
&emsp; 测试用例：  

```java
public class LRUCacheTest {

    public static void testLRUCache() {
        // 建立一个容量为3基于LRU算法的缓存Map
        LRUCache<String, Integer> lruCache = new LRUCache<>(3);

        lruCache.put("Bob",37);
        lruCache.put("Tony",27);
        lruCache.put("Aaron",23);

        System.out.println("LRU Cache:");
        lruCache.forEach( (k,v) -> System.out.println("K : " + k + " V : " + v ) );

        lruCache.put("Cat", 3);
        System.out.println("add new entry LRU Cache:");
        lruCache.forEach( (k,v) -> System.out.println("K : " + k + " V : " + v ) );

        lruCache.put("Aaron", 24);
        System.out.println("access \"Aaron\" , LRU Cache:");
        lruCache.forEach( (k,v) -> System.out.println("K : " + k + " V : " + v ) );

        lruCache.get("Cat");
        lruCache.put("David", 30);
        System.out.println("access \"Cat\", add new entry, LRU Cache:");
        lruCache.forEach( (k,v) -> System.out.println("K : " + k + " V : " + v ) );
    }
}
```
&emsp; 测试结果如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-10.png)  



