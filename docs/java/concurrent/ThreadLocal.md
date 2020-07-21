

<!-- TOC -->

- [1. ThreadLocal](#1-threadlocal)
    - [1.1. ThreadLocal源码](#11-threadlocal源码)
        - [1.1.1. ThreadLocal存储结构](#111-threadlocal存储结构)
        - [1.1.2. ThreadLocal成员方法](#112-threadlocal成员方法)
            - [1.1.2.1. set()](#1121-set)
            - [1.1.2.2. get()](#1122-get)
    - [1.2. ThreadLocal使用](#12-threadlocal使用)
        - [1.2.1. ※※※正确使用](#121-※※※正确使用)
        - [1.2.2. ThreadLocal的内存泄漏](#122-threadlocal的内存泄漏)
        - [1.2.3. SimpleDateFormat非线程安全问题](#123-simpledateformat非线程安全问题)
    - [1.3. ThreadLocal局限性（变量不具有传递性）](#13-threadlocal局限性变量不具有传递性)
        - [1.3.1. 类InheritableThreadLocal的使用](#131-类inheritablethreadlocal的使用)
        - [1.3.2. 类TransmittableThreadLocal(alibaba)的使用](#132-类transmittablethreadlocalalibaba的使用)
    - [1.4. ThreadLocal的优化](#14-threadlocal的优化)

<!-- /TOC -->


![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-26.png)   

# 1. ThreadLocal  
&emsp; 首先说明，ThreadLocal与线程同步无关。ThreadLocal虽然提供了一种解决多线程环境下成员变量的问题，但是它并不是解决多线程共享变量的问题。  
&emsp; ThreadLocal，很多地方叫做线程本地变量，也有些地方叫做线程本地存储。每一个线程都会保存一份变量副本，每个线程都可以独立地修改自己的变量副本，而不会影响到其他线程，是一种线程隔离的思想。  

&emsp; **ThreadLocal和Synchonized关系：**  
&emsp; ThreadLocal，用于线程间的数据隔离，主要解决多线程中数据因并发产生不一致问题；Synchonized，多个线程间通信时能够获得数据共享。它们都用于解决多线程并发访问。  
&emsp; 但是ThreadLocal与synchronized有本质的区别：  
* 资源共享：  
&emsp; lock的资源是多个线程共享的，所以访问的时候需要加锁。  
&emsp; ThreadLocal为每一个线程都提供了变量的副本，是一个线程的本地变量，也就意味着这个变量是线程独有的，是不能与其他线程共享的。即隔离了多个线程对数据的数据共享，这样就可以避免资源竞争带来的多线程的问题。  
* 性能开销：lock是通过时间换空间的做法；ThreadLocal是典型的通过空间换时间的做法。  
* 当然它们的使用场景也是不同的，关键看资源是需要多线程之间共享的还是单线程内部共享的。  

&emsp; **ThreadLocal和线程池一起使用？**  
&emsp; ThreadLocal对象的生命周期跟线程的生命周期一样长，那么如果将ThreadLocal对象和线程池一起使用，就可能会遇到这种情况：一个线程的ThreadLocal对象会和其他线程的ThreadLocal对象串掉，一般不建议将两者一起使用。  

## 1.1. ThreadLocal源码  
### 1.1.1. ThreadLocal存储结构  
&emsp; 首先看下Thread.java类代码：  

```java
//与此线程有关的ThreadLocal值。由ThreadLocal类维护
ThreadLocalMap threadLocals = null;
//与此线程有关的InheritableThreadLocal值。由InheritableThreadLocal类维护
ThreadLocalMap inheritableThreadLocals = null;
```
&emsp; 从上面Thread类 源代码可以看出Thread 类中有一个 threadLocals 和 一个  inheritableThreadLocals 变量，它们都是 ThreadLocalMap 类型的变量。默认情况下这两个变量都是null，只有当前线程调用 ThreadLocal 类的 set或get方法时才创建它们，实际上调用这两个方法的时候，调用的是ThreadLocalMap类对应的 get()、set()方法。  

&emsp; ThradLocal中内部类ThreadLocalMap：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-23.png)   
&emsp; **<font color = "lime">ThreadLocal.ThreadLocalMap</font>**，Map结构中Entry继承WeakReference，所以Entry对应key的引用（ThreadLocal实例）是一个弱引用，Entry对Value的引用是强引用。**<font color = "lime">Key是一个ThreadLocal实例，Value是一个线程特有对象。</font>**Entry的作用即是：为其属主线程建立起一个ThreadLocal实例与一个线程特有对象之间的对应关系。  
&emsp; 具体的ThreadLocalMap实例并不是ThreadLocal保持，而是每个Thread持有，且不同的Thread持有不同的ThreadLocalMap实例, 因此它们是不存在线程竞争的(不是一个全局的map)， 另一个好处是每次线程死亡，所有map中引用到的对象都会随着这个Thread的死亡而被垃圾收集器一起收集。  

    如何解决 Hash 冲突？
    ThreadLocalMap 虽然是类似 Map 结构的数据结构，但它并没有实现 Map 接口。它不支持 Map 接口中的 next 方法，这意味着 ThreadLocalMap 中解决 Hash 冲突的方式并非 拉链表 方式。
    实际上，ThreadLocalMap 采用线性探测的方式来解决 Hash 冲突。所谓线性探测，就是根据初始 key 的 hashcode 值确定元素在 table 数组中的位置，如果发现这个位置上已经被其他的 key 值占用，则利用固定的算法寻找一定步长的下个位置，依次判断，直至找到能够存放的位置。

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-24.png)   

### 1.1.2. ThreadLocal成员方法  
&emsp; ThreadLocal接口方法有4个。这些方法为每一个使用这个变量的线程都存有一份独立的副本，因此get总是返回由当前线程在调用set时设置的最新值。  

```java
public T get() { }  //取数据
public void set(T value) { }  //存数据
public void remove() { }  //删除数据。将当前线程局部变量的值删除，目的是为了减少内存的占用，该方法是JDK5.0新增的方法。需要指出的是，当线程结束后，对应该线程的局部变量将自动被垃圾回收，所以显式调用该方法清除线程的局部变量并不是必须的操作，但它可以加快内存的回收速度。
protected T initialValue() { } // 初始化的数据，用于子类自定义初始化值。返回该线程局部变量的初始值，该方法是一个protected的方法，显然是为了让子类覆盖而设计的。这个方法是一个延迟调用方法，在线程第1次调用get()或set(Object)时才执行，并且仅执行1次。ThreadLocal中的缺省实现直接返回一个null。
```
&emsp; **ThreadLocal.set()、get()、remove()方法都是对Thread对象的threadLocals属性进行操作。**
  
#### 1.1.2.1. set()  
&emsp; 当线程调用threadLocal对象的set(Object value)方法时，数据并不是存储在ThreadLocal对象中，而是存储在Thread对象的threadLocals属性中。  

```java
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}

set(ThreadLocal key, Object value)
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
        // key == null，但是存在值（因为此处的e != null），说明之前的ThreadLocal对象已经被回收了
        if (k == null) {
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

#### 1.1.2.2. get()  

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

## 1.2. ThreadLocal使用  
&emsp; 常见的ThreadLocal用法主要有两种：
1. 在线程级别传递变量。  
&emsp; 在日常Web开发中会遇到需要把一个参数层层的传递到最内层，然后中间层根本不需要使用这个参数，或者是仅仅在特定的工具类中使用，这样完全没有必要在每一个方法里面都传递这样一个通用的参数。如果有一个办法能够在任何一个类里面想用的时候直接拿来使用就太好了。Java Web项目大部分都是基于Tomcat，每次访问都是一个新的线程，可以使用ThreadLocal，每一个线程都独享一个ThreadLocal，在接收请求的时候set特定内容，在需要的时候get这个值。  
&emsp; 最常见的ThreadLocal使用场景为用来解决数据库连接、Session管理等。  
2. 保证线程安全。  
&emsp; ThreadLocal为解决多线程程序的并发问题提供了一种新的思路。但是ThreadLocal也有局限性，阿里规范中  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-19.png)   
&emsp; 每个线程往ThreadLocal中读写数据是线程隔离，互相之间不会影响的，所以ThreadLocal无法解决共享对象的更新问题！  
&emsp; 由于不需要共享信息，自然就不存在竞争问题了，从而保证了某些情况下线程的安全，以及避免了某些情况需要考虑线程安全必须同步带来的性能损失！  

### 1.2.1. ※※※正确使用  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-20.png)   

1. **<font color = "red">使用static定义threadLocal变量，是为了确保全局只有一个保存 Integer 对象的 ThreadLocal 实例。</font>**  
2. **<font color = "lime">finally语句里调用threadLocal.remove()。</font>**

### 1.2.2. ThreadLocal的内存泄漏  
&emsp; <font color = "red">ThreadLocalMap的key为ThreadLocal实例，是一个弱引用，弱引用有利于GC的回收，当key == null时，GC就会回收这部分空间，但value不一定能被回收，因为它和Current Thread之间还存在一个强引用的关系。</font>  
&emsp; 由于这个强引用的关系，会导致value无法回收，如果线程对象不消除这个强引用的关系，就可能会出现OOM。调用ThreadLocal的remove()方法进行显式处理。 

### 1.2.3. SimpleDateFormat非线程安全问题  

```java
public class Foo{
    // SimpleDateFormat is not thread-safe, so give one to each thread
    private static final ThreadLocal<SimpleDateFormat> formatter = newThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue(){
            return new SimpleDateFormat("yyyyMMdd HHmm");
        }
    };

    public String formatIt(Date date){
        return formatter.get().format(date);
    }
}
```
&emsp; final确保ThreadLocal的实例不可更改，防止被意外改变，导致放入的值和取出来的不一致，另外还能防止ThreadLocal的内存泄漏。  

 

## 1.3. ThreadLocal局限性（变量不具有传递性）  
&emsp; ThreadLocal无法在父子线程之间传递，示例代码如下：  

```java
public class Service {
    private static ThreadLocal<Integer> requestIdThreadLocal = new ThreadLocal<>();
    public static void main(String[] args) {
        Integer reqId = new Integer(5);
        Service a = new Service();
        a.setRequestId(reqId);
    }

    public void setRequestId(Integer requestId) {
        requestIdThreadLocal.set(requestId);
        doBussiness();
    }

    public void doBussiness() {
        System.out.println("首先打印requestId:" + requestIdThreadLocal.get());
        (new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("子线程启动");
                System.out.println("在子线程中访问requestId:" + requestIdThreadLocal.get());
            }
        })).start();
    }
}
```
&emsp; 运行结果如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-25.png)   

### 1.3.1. 类InheritableThreadLocal的使用  
&emsp; 使用类InheritableThreadLocal可以在子线程中取得父线程继承下来的值。  
&emsp; InheritableThreadLocal主要用于子线程创建时，需要自动继承父线程的ThreadLocal变量，实现子线程访问父线程的threadlocal变量。  
&emsp; InheritableThreadLocal继承了ThreadLocal，并重写了childValue、getMap、createMap三个方法。  

### 1.3.2. 类TransmittableThreadLocal(alibaba)的使用  
&emsp; InheritableThreadLocal支持子线程访问在父线程中设置的线程上下文环境的实现原理是在创建子线程时将父线程中的本地变量值复制到子线程，即复制的时机为创建子线程时。  
&emsp; 但并发、多线程就离不开线程池的使用，因为线程池能够复用线程，减少线程的频繁创建与销毁，如果使用InheritableThreadLocal，那么线程池中的线程拷贝的数据来自于第一个提交任务的外部线程，即后面的外部线程向线程池中提交任务时，子线程访问的本地变量都来源于第一个外部线程，造成线程本地变量混乱。  
&emsp; TransmittableThreadLocal是阿里巴巴开源的专门解决InheritableThreadLocal的局限性，实现线程本地变量在线程池的执行过程中，能正常的访问父线程设置的线程变量。  

## 1.4. ThreadLocal的优化  
&emsp; Netty对ThreadLocal进行了优化，优化方式是继承了Thread类，实现了自己的FastThreadLocal。FastThreadLocal的吞吐量是jdk的ThreadLocal的3倍左右。  





