
<!-- TOC -->

- [1. CopyOnWriteArrayList](#1-copyonwritearraylist)
    - [1.1. 简介](#11-简介)
    - [1.2. ~~源码解析~~](#12-源码解析)
    - [1.3. 示例代码](#13-示例代码)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. CopyOnWriteArrayList  
&emsp; CopyOnWrite，写时复制。读操作时不加锁以保证性能不受影响；  
&emsp; **<font color = "clime">`写操作时加锁，`复制资源的一份副本，在副本上执行写操作，写操作完成后将资源的引用指向副本。</font>** CopyOnWriteArrayList源码中，基于ReentrantLock保证了增加元素和删除元素动作的互斥。   
&emsp; **优点：** 可以对CopyOnWrite容器进行并发的读，而不需要加锁，因为当前容器不会添加任何元素。`所以CopyOnWrite容器也是一种读写分离的思想，读和写不同的容器。`  
&emsp; **<font color = "clime">缺点：** **1.占内存(写时复制，new两个对象)；2.不能保证数据实时一致性。</font>**  
&emsp; **使用场景：** <font color = "clime">CopyOnWrite并发容器用于读多写少的并发场景。比如白名单，黑名单，商品类目的访问和更新场景。</font>

# 1. CopyOnWriteArrayList
## 1.1. 简介  
<!-- 
知道 CopyOnWriteArrayList 吗？
https://mp.weixin.qq.com/s/hEkUIJWEG1mJ1Ya8pa7R4w
-->
&emsp; CopyOnWrite，写时复制。读操作时不加锁以保证性能不受影响； **<font color = "clime">`写操作时加锁，`复制资源的一份副本，在副本上执行写操作，写操作完成后将资源的引用指向副本。</font>**  

&emsp; **优点：** 可以对CopyOnWrite容器进行并发的读，而不需要加锁，因为当前容器不会添加任何元素。`所以CopyOnWrite容器也是一种读写分离的思想，读和写不同的容器。`  
&emsp; **<font color = "clime">缺点：** **1.占内存(写时复制，new两个对象)；2.不能保证数据实时一致性。</font>**  
* 内存占用问题:  
&emsp; 因为CopyOnWrite的写时复制机制，所以在进行写操作的时候，内存里会同时驻扎两个对象的内存，旧的对象和新写入的对象(注意：在复制的时候只是复制容器里的引用，只是在写的时候会创建新对象添加到新容器里，而旧容器的对象还在使用，所以有两份对象内存)。如果这些对象占用的内存比较大，比如说200M左右，那么再写入100M数据进去，内存就会占用300M，那么这个时候很有可能造成频繁的Yong GC和Full GC。之前系统中使用了一个服务由于每晚使用CopyOnWrite机制更新大对象，造成了每晚15秒的Full GC，应用响应时间也随之变长。  
&emsp; 针对内存占用问题，可以通过压缩容器中的元素的方法来减少大对象的内存消耗，比如，如果元素全是10进制的数字，可以考虑把它压缩成36进制或64进制。或者不使用CopyOnWrite容器，而使用其他的并发容器，如ConcurrentHashMap。  
* 数据一致性问题，能读取到脏数据：  
&emsp; CopyOnWrite容器只能保证数据的最终一致性，不能保证数据的实时一致性。所以如果希望写入的的数据，马上能读到，不要使用CopyOnWrite容器。 

&emsp; **使用场景：** <font color = "clime">CopyOnWrite并发容器用于读多写少的并发场景。比如白名单，黑名单，商品类目的访问和更新场景。</font><font color = "red">假如有一个搜索网站，用户在这个网站的搜索框中，输入关键字搜索内容，但是某些关键字不允许被搜索。这些不能被搜索的关键字会被放在一个黑名单当中，黑名单每天晚上更新一次。当用户搜索时，会检查当前关键字在不在黑名单当中，如果在，则提示不能搜索。</font>  
&emsp; 在写多读少的场合，CopyOnWriteArrayList的性能可能不如Vector。   


&emsp; CopyOnWriteArrayList，并发版ArrayList，底层结构也是数组，和ArrayList不同之处在于：当新增和删除元素时会创建一个新的数组，在新的数组中增加或者排除指定对象，最后用新增数组替换原来的数组。  

## 1.2. ~~源码解析~~  
<!-- 
https://www.cnblogs.com/zengcongcong/p/12754067.html
-->
&emsp; CopyOnWriteArrayList基于ReentrantLock保证了增加元素和删除元素动作的互斥。在读上没有做任何锁操作，这样就保证了读的性能。  

```java
public class CopyOnWriteArrayList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable {

    final transient ReentrantLock lock = new ReentrantLock();
    private transient volatile Object[] array;

    //创建一个大小为0的数组
    public CopyOnWriteArrayList() {
        setArray(new Object[0]);
    }

    // 添加元素，有锁
    /*    add方法并没有加上synchronized关键字，它通过使用ReentrantLock来保证线程安全。*/
    public boolean add(E e) {
        final ReentrantLock lock = this.lock;
        lock.lock(); // 修改时加锁，保证并发安全
        try {
            Object[] elements = getArray(); // 当前数组
            int len = elements.length;
            Object[] newElements = Arrays.copyOf(elements, len + 1); // 创建一个新数组，比老的大一个空间
            newElements[len] = e; // 要添加的元素放进新数组
            setArray(newElements); // 用新数组替换原来的数组
            return true;
        } finally {
            lock.unlock(); // 解锁
        }
    }

    //
    public E remove(int index) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            E oldValue = get(elements, index);
            int numMoved = len - index - 1;
            if (numMoved == 0)
                setArray(Arrays.copyOf(elements, len - 1));
            else {
                Object[] newElements = new Object[len - 1];
                System.arraycopy(elements, 0, newElements, 0, index);
                System.arraycopy(elements, index + 1, newElements, index,
                        numMoved);
                setArray(newElements);
            }
            return oldValue;
        } finally {
            lock.unlock();
        }
    }

    // 读元素，不加锁，因此可能读取到旧数据
    public E get(int index) {
        return get(getArray(), index);
    }

    //创建一个新的COWIterator对象实例，并保存了一个当前数组的快照，
    // 在调用 next遍历时则仅对此快照数组进行遍历，
    // 因此遍历CopyOnWriteArrayList时不会抛出Concurrent- Modi ficatiedException
    public Iterator<E> iterator() {
        return new COWIterator<E>(getArray(), 0);
    }
}
```

## 1.3. 示例代码  

```java
import java.util.Map;
import com.ifeve.book.forkjoin.CopyOnWriteMap;

/**
 * 黑名单服务
 * @author fangtengfei
 */
public class BlackListServiceImpl {

    private static CopyOnWriteMap<String, Boolean> blackListMap = new CopyOnWriteMap<String, Boolean>(1000);

    public static boolean isBlackList(String id) {
        return blackListMap.get(id) == null ? false : true;
    }

    public static void addBlackList(String id) {
        blackListMap.put(id, Boolean.TRUE);
    }

    /**
     * 批量添加黑名单
     * @param ids
     */
    public static void addBlackList(Map<String,Boolean> ids) {
        blackListMap.putAll(ids);
    }

}
```
