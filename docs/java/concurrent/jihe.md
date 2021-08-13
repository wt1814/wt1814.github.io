

<!-- TOC -->

- [1. collections](#1-collections)
    - [1.1. List，CopyOnWriteArrayList](#11-listcopyonwritearraylist)
    - [1.2. Map](#12-map)
        - [1.2.1. ConcurrentHashMap](#121-concurrenthashmap)
        - [1.2.2. ConcurrentSkipListMap](#122-concurrentskiplistmap)
    - [1.3. Set](#13-set)
    - [1.4. Queue](#14-queue)

<!-- /TOC -->

# 1. collections
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-5.png)  
&emsp; 14个并发容器按照线程安全模型分类：copy-on-write、CAS(JDK1.8 ConcurrentHashMap)、读写分离(LinkedBlockingQueue)。  

## 1.1. List，CopyOnWriteArrayList  
&emsp; [CopyOnWriteArrayList](/docs/java/concurrent/CopyOnWriteArrayList.md)  

## 1.2. Map  
&emsp; JDK中并没有提供CopyOnWriteMap。JUC容器Map的实现有ConcurrentHashMap，线程安全的哈希表，相当于线程安全的HashMap；ConcurrentSkipListMap，线程安全的有序的哈希表，相当于线程安全的TreeMap。  

### 1.2.1. ConcurrentHashMap
&emsp; 详见[ConcurrentHashMap详解](/docs/java/concurrent/ConcurrentHashMap.md)  

### 1.2.2. ConcurrentSkipListMap
&emsp; ConcurrentSkipListMap与TreeMap都是有序的哈希表。  
&emsp; ConcurrentSkipListMap线程安全，TreeMap非线程安全；  
&emsp; ConcurrentSkipListMap是通过跳表(skip list)实现的，而TreeMap是通过红黑树实现的。

## 1.3. Set  
&emsp; <font color = "red">JUC容器Set的实现有CopyOnWriteArraySet与ConcurrentSkipListSet。</font>CopyOnWriteArraySet相当于线程安全的HashSet，CopyOnWriteArraySet的实现依赖于CopyOnWriteArrayList；ConcurrentSkipListSet相当于线程安全的TreeSet，ConcurrentSkipListSet的实现依赖于ConcurrentSkipListMap。  
&emsp; CopyOnWriteArraySet适用于读多写少的高并发场合，在需要并发写的场合，则可以使用 Set s = Collections.synchronizedSet(Set<T> s)得到一个线程安全的Set。 

## 1.4. Queue  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-6.png)  
&emsp; <font color = "red">在并发队列上，JDK提供了两套实现，一个是以ConcurrentLinkedQueue为代表的高性能队列，一个是以BlockingQueue接口为代表的阻塞队列。</font>  
&emsp; ConcurrentLinkedQueue是一个适用于高并发场景下的队列。它通过无锁的方式，实现了高并发状态下的高性能。通常，ConcurrentLinkedQueue的性能要好于BlockingQueue 。  
&emsp; 与 ConcurrentLinkedQueue 的使用场景不同，[BlockingQueue](/docs/java/concurrent/BlockingQueue.md)的主要功能并不是在于提升高并发时的队列性能，而在于简化多线程间的数据共享。  
