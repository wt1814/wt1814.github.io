

<!-- TOC -->

- [1. ThreadLocal](#1-threadlocal)
    - [1.1. ThreadLocal简介](#11-threadlocal简介)
    - [1.2. ThreadLocal源码](#12-threadlocal源码)
        - [1.2.1. set()](#121-set)
        - [1.2.2. ThreadLocalMap内部类](#122-threadlocalmap内部类)
        - [1.2.3. get()](#123-get)
    - [1.3. ThreadLocal是如何实现线程隔离的？](#13-threadlocal是如何实现线程隔离的)
    - [1.4. ~~ThreadLocal内存泄露~~](#14-threadlocal内存泄露)
        - [1.4.1. ~~ThreadLocal内存模型~~](#141-threadlocal内存模型)
        - [1.4.2. ~~ThreadLocal可能的内存泄漏~~](#142-threadlocal可能的内存泄漏)
        - [1.4.3. ThreadLocalMap的key被回收后，如何获取值？](#143-threadlocalmap的key被回收后如何获取值)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. ThreadLocal源码/内存模型：  
    1. **<font color = "red">ThreadLocal#set()#getMap()方法：线程调用threadLocal对象的set(Object value)方法时，数据并不是存储在ThreadLocal对象中，</font><font color = "clime">而是将值存储在每个Thread实例的threadLocals属性中。</font>** 即，当前线程调用ThreadLocal类的set或get方法时，实际上调用的是ThreadLocalMap类对应的 get()、set()方法。  
    &emsp; ~~Thread ---> ThreadLocal.ThreadLocalMap~~
    2. **<font color = "clime">ThreadLocal.ThreadLocalMap，</font>Map结构中Entry继承WeakReference，所以Entry对应key的引用(ThreadLocal实例)是一个弱引用，Entry对Value的引用是强引用。<font color = "clime">`Key是一个ThreadLocal实例，Value是设置的值。`Entry的作用即是：为其属主线程建立起一个ThreadLocal实例与一个线程持有对象之间的对应关系。</font>**   
2. ThreadLocal是如何实现线程隔离的？   
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-85.png)  
    &emsp; ThreadLocal之所以能达到变量的线程隔离，其实就是每个线程都有一个自己的ThreadLocalMap对象来存储同一个threadLocal实例set的值，而取值的时候也是根据同一个threadLocal实例去自己的ThreadLocalMap里面找，自然就互不影响了，从而达到线程隔离的目的！  
3. **ThreadLocal内存泄露：**  
    &emsp; ThreadLocalMap使用ThreadLocal的弱引用作为key，<font color = "red">如果一个ThreadLocal不存在外部强引用时，Key(ThreadLocal实例)会被GC回收，这样就会导致ThreadLocalMap中key为null，而value还存在着强引用，只有thead线程退出以后，value的强引用链条才会断掉。</font>  
    &emsp; **<font color = "clime">但如果当前线程迟迟不结束的话，这些key为null的Entry的value就会一直存在一条强引用链：Thread Ref -> Thread -> ThreaLocalMap -> Entry -> value。永远无法回收，造成内存泄漏。</font>**  
    &emsp; 解决方案：`调用remove()方法`
4. **ThreadLocalMap的key被回收后，如何获取值？**  
    &emsp; ThreadLocal#get() ---> setInitialValue() ---> ThreadLocalMap.set(this, value); 。  
    &emsp; 通过nextIndex()不断获取table上的槽位，直到遇到第一个为null的地方，此处也将是存放具体entry的位置，在线性探测法的不断冲突中，如果遇到非空entry中的key为null，可以表明key的弱引用已经被回收，但是由于线程仍未结束生命周期被回收，而导致该entry仍未从table中被回收，那么则会在这里尝试通过replaceStaleEntry()方法，将null key的entry回收掉并set相应的值。  

# 1. ThreadLocal  
<!-- 
比较好： 什么，你的ThreadLocal内存泄漏了？ 
https://mp.weixin.qq.com/s/mH1jRiZTiHdlMBSwu3f2zg
ThreadLocal以及内存泄漏 
https://mp.weixin.qq.com/s/hjx7CHPpVjs9_Hz3pl0DgQ

**** ThreadLocal的最牛辨析！
https://mp.weixin.qq.com/s/IklA1Oil9kRh7Z_HwuAnyg


-->

## 1.1. ThreadLocal简介
&emsp; ThreadLocal的作用是每一个线程创建一个副本。  

1. 在进行对象跨层次传递的时候，使用ThreadLocal可以避免多次传递，打破层次间的束缚。   
2. 线程间层次隔离。  
3. 进行事务操作，用于存储线程事务信息。  
4. 数据库连接，Session会话管理。  

-------

&emsp; 首先说明，ThreadLocal与线程同步无关。ThreadLocal虽然提供了一种解决多线程环境下成员变量的问题，但是它并不是解决多线程共享变量的问题。  
&emsp; <font color = "red">ThreadLocal，很多地方叫做线程本地变量，也有些地方叫做线程本地存储。</font>每一个线程都会保存一份变量副本，每个线程都可以独立地修改自己的变量副本，而不会影响到其他线程，<font color = "red">是一种线程隔离的思想。</font>  

&emsp; **ThreadLocal和Synchonized关系：**  
&emsp; ThreadLocal，用于线程间的数据隔离，主要解决多线程中数据因并发产生不一致问题；Synchonized，多个线程间通信时能够获得数据共享。它们都用于解决多线程并发访问。  
&emsp; 但是ThreadLocal与synchronized有本质的区别：  

* 资源共享：  
    * lock的资源是多个线程共享的，所以访问的时候需要加锁。  
    * ThreadLocal为每一个线程都提供了变量的副本，是一个线程的本地变量，也就意味着这个变量是线程独有的，是不能与其他线程共享的。即隔离了多个线程对数据的数据共享，这样就可以避免资源竞争带来的多线程的问题。  
* 性能开销：lock是通过时间换空间的做法；ThreadLocal是典型的通过空间换时间的做法。  
* 当然它们的使用场景也是不同的，关键看资源是需要多线程之间共享的还是单线程内部共享的。  

## 1.2. ThreadLocal源码  
&emsp; ThreadLocal接口方法有4个。这些方法为每一个使用这个变量的线程都存有一份独立的副本，因此get总是返回由当前线程在调用set时设置的最新值。  

```java
public T get() { }  //取数据
public void set(T value) { }  //存数据
public void remove() { }  //删除数据。将当前线程局部变量的值删除，目的是为了减少内存的占用，该方法是JDK5.0新增的方法。需要指出的是，当线程结束后，对应该线程的局部变量将自动被垃圾回收，所以显式调用该方法清除线程的局部变量并不是必须的操作，但它可以加快内存的回收速度。
protected T initialValue() { } // 初始化的数据，用于子类自定义初始化值。返回该线程局部变量的初始值，该方法是一个protected的方法，显然是为了让子类覆盖而设计的。这个方法是一个延迟调用方法，在线程第1次调用get()或set(Object)时才执行，并且仅执行1次。ThreadLocal中的缺省实现直接返回一个null。
```

### 1.2.1. set()  
```java
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);  //todo
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}


ThreadLocalMap getMap(Thread t) {
    return t.threadLocals;
}

/**
 * Set the value associated with key.
 *
 * @param key the thread local object
 * @param value the value to be set
 */
private void set(ThreadLocal<?> key, Object value) {
    Entry[] tab = table;
    int len = tab.length;
    //根据ThreadLocal的散列值，查找对应元素在数组中的位置
    int i = key.threadLocalHashCode & (len-1);
    //采用线性探测法寻找合适位置
    for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
        ThreadLocal<?> k = e.get();
        //key存在，直接覆盖
        if (k == key) {
            e.value = value;
            return;
        }
        //todo
        // key == null，但是存在值(因为此处的e != null)，说明之前的ThreadLocal对象已经被回收了
        if (k == null) {
            //todo 
            replaceStaleEntry(key, value, i);
            return;
        }
    }
    //ThreadLocal对应的key实例不存在，new一个
    tab[i] = new Entry(key, value);
    int sz = ++size;
    //清楚陈旧的Entry(key == null的)
    // 如果没有清理陈旧的 Entry 并且数组中的元素大于了阈值，则进行 rehash
    if (!cleanSomeSlots(i, sz) && sz >= threshold)
        rehash();
}
```

&emsp; **<font color = "red">ThreadLocal#set()#getMap()方法：线程调用threadLocal对象的set(Object value)方法时，数据并不是存储在ThreadLocal对象中，</font><font color = "clime">而是将值存储在每个Thread实例的threadLocals属性中。</font>** Thread.java相关源码如下：  

```java
//与此线程有关的ThreadLocal值。由ThreadLocal类维护
ThreadLocalMap threadLocals = null;
//与此线程有关的InheritableThreadLocal值。由InheritableThreadLocal类维护
ThreadLocalMap inheritableThreadLocals = null;
```
&emsp; 从上面Thread类源代码可以看出Thread类中有一个threadLocals和一个inheritableThreadLocals变量，它们都是ThreadLocalMap类型的变量 <font color = "red">(ThreadLocalMap是ThreadLocal类的内部类)</font> 。即，具体的ThreadLocalMap实例并不是ThreadLocal保持，而是每个Thread持有，且不同的Thread持有不同的ThreadLocalMap实例，因此它们是不存在线程竞争的(不是一个全局的map)，另一个好处是每次线程死亡，所有map中引用到的对象都会随着这个Thread的死亡而被垃圾收集器一起收集。     
&emsp; 默认情况下这两个变量都是null， **<font color = "red">只有当前线程调用ThreadLocal类的set或get方法时才创建它们，实际上调用这两个方法的时候，调用的是ThreadLocalMap类对应的 get()、set()方法。</font>**  

### 1.2.2. ThreadLocalMap内部类
&emsp; ThradLocal中内部类ThreadLocalMap：  
<!-- https://mp.weixin.qq.com/s/op_ix4tPWa7l8VPg4Al1ig -->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-23.png)   
&emsp; **<font color = "clime">ThreadLocal.ThreadLocalMap，</font>Map结构中Entry继承WeakReference，所以Entry对应key的引用(ThreadLocal实例)是一个弱引用，Entry对Value的引用是强引用。<font color = "clime">Key是一个ThreadLocal实例，Value是设置的值。Entry的作用即是：为其属主线程建立起一个ThreadLocal实例与一个线程持有对象之间的对应关系。</font>**   
 
        ThreadLocalMap如何解决Hash冲突？
        ThreadLocalMap虽然是类似Map结构的数据结构，但它并没有实现Map接口。它不支持Map接口中的next方法，这意味着ThreadLocalMap中解决Hash冲突的方式并非拉链表方式。
        实际上，ThreadLocalMap 采用线性探测的方式来解决Hash冲突。所谓线性探测，就是根据初始 key 的 hashcode 值确定元素在 table 数组中的位置，如果发现这个位置上已经被其他的 key 值占用，则利用固定的算法寻找一定步长的下个位置，依次判断，直至找到能够存放的位置。

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-24.png)   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-59.png)   
&emsp; ⚠注： **<font color = "clime">一个线程可能存在多个ThreadLocal实例，即存在多个Entry节点。</font>**  

### 1.2.3. get()  
&emsp; get是获取当前线程的对应的私有变量，是之前set或者通过initialValue指定的变量，其代码如下：  

```java
public T get() {
    //获取当前线程。
    Thread t = Thread.currentThread();
    //获取线程的threadLocals属性。
    ThreadLocalMap map = getMap(t);
    if (map != null) {
        //如果线程对象的threadLocals属性不为空，则从该Map结构中，用threadLocal对象为键去查找值，如果能找到，则返回其value值，否则执行以下代码。
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    //如果线程对象的threadLocals属性为空，或未从threadLocals中找到对应的键值对，则调用该方法执行初始化。
    return setInitialValue();
}

private T setInitialValue() {
    //调用initialValue()获取默认初始化值，该方法默认返回null，子类可以重写，实现线程本地变量的初始化。
    T value = initialValue();
    //获取当前线程。
    Thread t = Thread.currentThread();
    //获取该线程对象的threadLocals属性。
    ThreadLocalMap map = getMap(t);
    //如果不为空，则将threadLocal:value存入线程对象的threadLocals属性中。
    if (map != null)
        map.set(this, value);
    else
        //否则初始化线程对象的threadLocals,然后将threadLocal:value键值对存入线程对象的threadLocals属性中。
        createMap(t, value);
    return value;
}
```

&emsp; 可以看到，其逻辑也比较简单清晰：  

* 获取当前线程的ThreadLocalMap实例
* 如果不为空，以当前ThreadLocal实例为key获取value
* 如果ThreadLocalMap为空或者根据当前ThreadLocal实例获取的value为空，则执行setInitialValue()

&emsp; setInitialValue()内部如下：  

* 调用重写的initialValue得到一个value  
* 将value放入到当前线程对应的ThreadLocalMap中  
* 如果map为空，先实例化一个map，然后赋值KV  

## 1.3. ThreadLocal是如何实现线程隔离的？  
<!-- 
http://www.noobyard.com/article/p-rthxinka-qa.html
-->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-85.png)  
&emsp; ThreadLocal之所以能达到变量的线程隔离，其实就是每个线程都有一个自己的ThreadLocalMap对象来存储同一个threadLocal实例set的值，而取值的时候也是根据同一个threadLocal实例去自己的ThreadLocalMap里面找，自然就互不影响了，从而达到线程隔离的目的！  


## 1.4. ~~ThreadLocal内存泄露~~
### 1.4.1. ~~ThreadLocal内存模型~~  
&emsp; 通过上一节的分析，其实已经很清楚ThreadLocal的相关设计了，对数据存储的具体分布也会有个比较清晰的概念。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-58.png)  
&emsp; Thread运行时，线程的的一些局部变量和引用使用的内存属于Stack(栈)区，而普通的对象是存储在Heap(堆)区。根据上图，基本分析如下：  

* 线程运行时，定义的TheadLocal对象被初始化，存储在Heap，同时线程运行的栈区保存了指向该实例的引用，也就是图中的ThreadLocalRef。
* 当ThreadLocal的set/get被调用时，虚拟机会根据当前线程的引用也就是。CurrentThreadRef找到其对应在堆区的实例，然后查看其对用的TheadLocalMap实例是否被创建，如果没有，则创建并初始化。
* Map实例化之后，也就拿到了该ThreadLocalMap的句柄，然后如果将当前ThreadLocal对象作为key，进行存取操作。
* 图中的虚线，表示key对ThreadLocal实例的引用是个弱引用。

### 1.4.2. ~~ThreadLocal可能的内存泄漏~~  
<!-- 
这4种ThreadLocal你都知道吗？ 
https://mp.weixin.qq.com/s/op_ix4tPWa7l8VPg4Al1ig
什么，你的ThreadLocal内存泄漏了？ 
https://mp.weixin.qq.com/s/mH1jRiZTiHdlMBSwu3f2zg
-->
<!-- 
&emsp; <font color = "red">ThreadLocalMap的key为ThreadLocal实例，是一个弱引用，弱引用有利于GC的回收，当key == null时，GC就会回收这部分空间，但value不一定能被回收，因为它和Current Thread之间还存在一个强引用的关系。</font>  
&emsp; 由于这个强引用的关系，会导致value无法回收，如果线程对象不消除这个强引用的关系，就可能会出现OOM。调用ThreadLocal的remove()方法进行显式处理。 


&emsp; 由于ThreadLocalMap是以弱引用的方式引用着ThreadLocal，换句话说，就是ThreadLocal是被ThreadLocalMap以弱引用的方式关联着，因此如果ThreadLocal没有被ThreadLocalMap以外的对象引用，则在下一次GC的时候，ThreadLocal实例就会被回收，那么此时ThreadLocalMap里的一组KV的K就是null了，因此在没有额外操作的情况下，此处的V便不会被外部访问到，而且只要Thread实例一直存在，Thread实例就强引用着ThreadLocalMap，因此ThreadLocalMap就不会被回收，那么这里K为null的V就一直占用着内存。  

&emsp; 综上，发生内存泄露的条件是  

* ThreadLocal实例没有被外部强引用，比如假设在提交到线程池的task中实例化的ThreadLocal对象，当task结束时，ThreadLocal的强引用也就结束了
* ThreadLocal实例被回收，但是在ThreadLocalMap中的V没有被任何清理机制有效清理
* 当前Thread实例一直存在，则会一直强引用着ThreadLocalMap，也就是说ThreadLocalMap也不会被GC

&emsp; 也就是说，如果Thread实例还在，但是ThreadLocal实例却不在了，则ThreadLocal实例作为key所关联的value无法被外部访问，却还被强引用着，因此出现了内存泄露。  
-->
&emsp; <font color = "clime">当ThreadLocalMap的key为弱引用，回收ThreadLocal时，由于ThreadLocalMap持有ThreadLocal的弱引用，即使没有手动删除，ThreadLocal也会被回收。</font>

&emsp; **<font color = "clime">ThreadLocal可能的内存泄漏：</font>**  
&emsp; ThreadLocalMap使用ThreadLocal的弱引用作为key，<font color = "red">如果一个ThreadLocal不存在外部强引用时，Key(ThreadLocal实例)会被GC回收，这样就会导致ThreadLocalMap中key为null，而value还存在着强引用，只有thead线程退出以后，value的强引用链条才会断掉。</font>  
&emsp; **<font color = "clime">但如果当前线程迟迟不结束的话，这些key为null的Entry的value就会一直存在一条强引用链：Thread Ref -> Thread -> ThreaLocalMap -> Entry -> value。永远无法回收，造成内存泄漏。</font>**  

### 1.4.3. ThreadLocalMap的key被回收后，如何获取值？  
<!-- 
https://blog.csdn.net/weixin_40318210/article/details/105885700
-->
&emsp; ThreadLocal#get() ---> setInitialValue() ---> ThreadLocalMap.set(this, value); 。  

```java
int i = key.threadLocalHashCode & (len-1);
//采用线性探测法寻找合适位置
for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
    ThreadLocal<?> k = e.get();
    //key存在，直接覆盖
    if (k == key) {
        e.value = value;
        return;
    }
    //todo
    // key == null，但是存在值(因为此处的e != null)，说明之前的ThreadLocal对象已经被回收了
    if (k == null) {
        //todo 
        replaceStaleEntry(key, value, i);
        return;
    }
}
```

&emsp; 通过nextIndex()不断获取table上得槽位，直到遇到第一个为null的地方，此处也将是存放具体entry的位置，在线性探测法的不断冲突中，如果遇到非空entry中的key为null，可以表明key的弱引用已经被回收，但是由于线程仍未结束生命周期被回收，而导致该entry仍未从table中被回收，那么则会在这里尝试通过replaceStaleEntry()方法，将null key的entry回收掉并set相应的值。  

