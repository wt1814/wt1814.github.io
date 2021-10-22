<!-- TOC -->

- [1. 阻塞队列](#1-阻塞队列)
    - [1.1. BlockingQueue接口](#11-blockingqueue接口)
    - [1.2. 常见BlockingQueue](#12-常见blockingqueue)
        - [1.2.1. SynchronousQueue](#121-synchronousqueue)
        - [1.2.2. ArrayBlockingQueue](#122-arrayblockingqueue)
            - [1.2.2.1. 属性](#1221-属性)
            - [1.2.2.2. 元素入队](#1222-元素入队)
            - [1.2.2.3. 获取元素](#1223-获取元素)
        - [1.2.3. LinkedBlockingQueue](#123-linkedblockingqueue)
            - [1.2.3.1. 属性](#1231-属性)
            - [1.2.3.2. 入队](#1232-入队)
            - [1.2.3.3. 出队](#1233-出队)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 阻塞队列：当队列是空的时候，从队列中获取元素的操作将会被阻塞，或者当队列是满时，往队列里添加元素的操作会被阻塞。  
2. `线程池所使用的缓冲队列，常用的是：SynchronousQueue（无缓冲等待队列）、ArrayBlockingQueue（有界缓冲等待队列）、LinkedBlockingQueue（无界缓冲等待队列）。`   
3. SynchronousQueue，没有容量，是无缓冲等待队列，是一个不存储元素的阻塞队列，会直接将任务交给消费者，必须等队列中的元素被消费后才能继续添加新的元素。  
4. LinkedBlockingQueue不同于ArrayBlockingQueue，它如果不指定容量，默认为Integer.MAX_VALUE，也就是无界队列。所以为了避免队列过大造成机器负载或者内存爆满的情况出现，在使用的时候建议手动传一个队列的大小。  
5. <font color = "red">ArrayBlockingQueue与LinkedBlockingQueue：</font> ArrayBlockingQueue预先分配好一段连续内存，更稳定；LinkedBlockingQueue读写锁分离，吞吐量更大。  

# 1. 阻塞队列  
<!-- 
详解 18 种队列，你知道几种？ 
https://mp.weixin.qq.com/s/cmA5iYF2VBv995kfB6pu0w
线程池的三种队列区别：SynchronousQueue、LinkedBlockingQueue 和ArrayBlockingQueue
https://blog.csdn.net/qq_26881739/article/details/80983495
-->

&emsp; **<font color = "clime">阻塞队列与普通队列的区别在于，</font>** 试图从空的阻塞队列中获取元素的线程将会被阻塞，直到其他的线程往空的队列插入新的元素。同样，试图往已满的阻塞队列中添加新元素的线程同样也会被阻塞，直到其他的线程使队列重新变得空闲起来，如从队列中移除一个或者多个元素，或者完全清空队列，下图展示了如何通过阻塞队列来合作：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-34.png)  
<!-- 
阻塞队列是一个在队列基础上又支持了两个附加操作的队列。  
2个附加操作：  
支持阻塞的插入方法：队列满时，队列会阻塞插入元素的线程，直到队列不满。  
支持阻塞的移除方法：队列空时，获取元素的线程会等待队列变为非空。  

-->
&emsp; <font color = "clime">阻塞队列常用于生产者和消费者的场景，生产者是向队列里添加元素的线程，消费者是从队列里取元素的线程。</font>简而言之，阻塞队列是生产者用来存放元素、消费者获取元素的容器。  

## 1.1. BlockingQueue接口  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-35.png)  
&emsp; BlockingQueue是继承自Queue的接口，在Queue的基础上增加了阻塞操作。相比Queue接口有两种形式的api，BlockingQueue则有四种形式的api，阻塞队列定义如果调用了某个函数可能当时不能立即满足结果，但很有可能在未来的某个时刻会满足。四种api定义：  
* 添加数据方法：  
    * add(e)：将元素添加到队列末尾，成功则返回true；<font color = "red">如果队列已满，则插入失败，抛出异常。</font>  
    * offer(e)：将元素添加到队列末尾，成功则返回true；<font color = "red">如果队列已满，则插入失败，返回false。</font>  
    * put(e)：将元素添加到队列末尾，<font color = "red">如果队列已满，队列会一直阻塞生产者线程直到成功。</font>  
    * offer(e,time,unit)：将元素添加到队列末尾，<font color = "red">如果队列已满，则等待一定的时间，当时间期限达到时，如果还没有插入成功，则返回false；否则返回true。</font>  
* 获取数据方法：  
    * remove()：删除队首元素，成功则返回true；如果队列为空，则删除失败，抛出异常。
    * poll()：删除队首元素，若成功则返回则返回队首元素，若队列为空，则返回null。
    * take()：从队首取元素，若队列为空，队列会一直阻塞消费者线程。
    * poll(time,unit)：从队首取元素，如果队列为空，则等待一定的时间，当时间期限达到时，如果还没有取出元素，则返回null；否则返回队首元素。

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-36.png)  
&emsp; 检查是在有数据时返回队列的第一个数据，并不会从队列中移除该数据。内部使用 ReentrantLock进行同步控制的。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-71.png)  
<!-- 
* 调用函数失败，抛出异常
* 调用失败，返回null或者false
* 调用失败，当前线程无限阻塞直到成功
* 阻塞指定是一段时间，如果还不能满足，就放弃该次操作。  
-->

## 1.2. 常见BlockingQueue  
&emsp; JDK7提供了6个阻塞队列实现类。分别是：  

* ArrayBlockingQueue：一个由数组结构组成的有界阻塞队列。  
* LinkedBlockingQueue：一个由链表结构组成的有界阻塞队列。  
* PriorityBlockingQueue：一个支持优先级排序的无界阻塞队列。  
* DelayQueue：一个使用优先级队列实现的无界阻塞队列。  
* SynchronousQueue：一个不存储元素的阻塞队列。  
* LinkedTransferQueue：一个由链表结构组成的无界阻塞队列。  

&emsp; **<font color = "red">ArrayBlockingQueue与LinkedBlockingQueue：</font>**  
&emsp; <font color = "red">ArrayBlockingQueue预先分配好一段连续内存，更稳定；LinkedBlockingQueue读写锁分离，吞吐量更大。</font>  
1. 队列大小有所不同，ArrayBlockingQueue是有界的初始化必须指定大小，而LinkedBlockingQueue可以是有界的也可以是无界的(Integer.MAX_VALUE)，对于LinkedBlockingQueue，当添加速度大于移除速度时，在无界的情况下，可能会造成内存溢出等问题。
2. 数据存储容器不同，ArrayBlockingQueue采用的是数组作为数据存储容器，而LinkedBlockingQueue采用的则是以Node节点作为连接对象的链表。
3. 由于ArrayBlockingQueue采用的是数组的存储容器，因此在插入或删除元素时不会产生或销毁任何额外的对象实例，而LinkedBlockingQueue则会生成一个额外的Node对象。这可能在长时间内需要高效并发地处理大批量数据的时，对于GC可能存在较大影响。
4. 两者的实现队列添加或移除的锁不一样，ArrayBlockingQueue实现的队列中的锁是没有分离的，即添加操作和移除操作采用的同一个ReenterLock锁，而LinkedBlockingQueue实现的队列中的锁是分离的，其添加采用的是putLock，移除采用的则是takeLock，这样能大大提高队列的吞吐量，也意味着在高并发的情况下生产者和消费者可以并行地操作队列中的数据，以此来提高整个队列的并发性能。

### 1.2.1. SynchronousQueue  


### 1.2.2. ArrayBlockingQueue  
#### 1.2.2.1. 属性  

```java
// 存储队列中的元素
final Object[] items;
// 队头
int takeIndex;
// 队尾(为null，等待接收元素)
int putIndex;
// 队列中的元素个数
int count;
// 队列锁
final ReentrantLock lock;
// 出队条件
private final Condition notEmpty;
// 入队条件
private final Condition notFull;
// 记录作用于该队列的迭代器(一个链表)
transient Itrs itrs;
```  

#### 1.2.2.2. 元素入队  

```java
// 入队，线程安全，队满时线程被阻塞
public void put(E e) throws InterruptedException {
    Objects.requireNonNull(e);
    final ReentrantLock lock = this.lock;
    
    // 申请独占锁，不允许阻塞带有中断标记的线程
    lock.lockInterruptibly();
    try {
        // 如果队列满了，需要阻塞“入队”线程
        while(count == items.length) {
            notFull.await();
        }
        // 入队
        enqueue(e);
    } finally {
        lock.unlock();
    }
}
```

```java
// 入队，线程安全，队满时不阻塞，直接返回false
public boolean offer(E e) {
    Objects.requireNonNull(e);
    final ReentrantLock lock = this.lock;
    // 申请独占锁，允许阻塞带有中断标记的线程
    lock.lock();
    try {
        // 如果队列满了，返回false
        if(count == items.length) {
            return false;
        }
        // 入队
        enqueue(e);
        return true;
    } finally {
        lock.unlock();
    }
}

// 入队，线程安全，队满时阻塞一段时间，如果在指定的时间内没有机会插入元素，则返回false
public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
    Objects.requireNonNull(e);
    
    long nanos = unit.toNanos(timeout);
    
    final ReentrantLock lock = this.lock;
    
    // 申请独占锁，不允许阻塞带有中断标记的线程
    lock.lockInterruptibly();
    try {
        // 如果队列满了，阻塞一段时间
        while(count == items.length) {
            if(nanos<=0L) {
                // 如果超时，返回false
                return false;
            }
            nanos = notFull.awaitNanos(nanos);
        }
        // 入队
        enqueue(e);
        return true;
    } finally {
        lock.unlock();
    }
}
```

#### 1.2.2.3. 获取元素  

&emsp; 插入与获取元素的方法用的都是同一个全局锁，因此即使多线程环境下，也不会发生调用插入方法完成后无法获取到这个元素的情况poll 返回队首元素，队列为空返回null 非阻塞。  

```java
// 出队，线程安全，队空时不阻塞，直接返回null
public E poll() {
    final ReentrantLock lock = this.lock;
    // 申请独占锁，允许阻塞带有中断标记的线程
    lock.lock();
    try {
        // 如果队列为空，返回null
        if(count == 0) {
            return null;
        }
        
        // 出队
        return dequeue();
    } finally {
        lock.unlock();
    }
}
```
&emsp; take返回队首元素 空队列则一直阻塞到有元素进来。  

```java
// 出队，线程安全，队空时线程被阻塞
public E take() throws InterruptedException {
    final ReentrantLock lock = this.lock;
    
    // 申请独占锁，不允许阻塞带有中断标记的线程
    lock.lockInterruptibly();
    try {
        // 如果队列为空，需要阻塞“出队”线程
        while(count == 0) {
            notEmpty.await();
        }
        // 出队
        return dequeue();
    } finally {
        lock.unlock();
    }
}
```

&emsp; poll立即返回式，有参方法可添加超时参数。  

```java
// 出队，线程安全，队空时阻塞一段时间，如果在指定的时间内没有机会取出元素，则返回null
public E poll(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    
    final ReentrantLock lock = this.lock;
    
    // 申请独占锁，不允许阻塞带有中断标记的线程
    lock.lockInterruptibly();
    try {
        // 如果队列为空，阻塞一段时间
        while(count == 0) {
            if(nanos<=0L) {
                // 如果超时，返回null
                return null;
            }
            nanos = notEmpty.awaitNanos(nanos);
        }
        // 出队
        return dequeue();
    } finally {
        lock.unlock();
    }
}
```

### 1.2.3. LinkedBlockingQueue
<!-- 
https://blog.csdn.net/tonywu1992/article/details/83419448
-->
&emsp; **<font color = "clime">LinkedBlockingQueue不同于ArrayBlockingQueue，它如果不指定容量，默认为Integer.MAX_VALUE，也就是无界队列。所以为了避免队列过大造成机器负载或者内存爆满的情况出现，在使用的时候建议手动传一个队列的大小。</font>**  

#### 1.2.3.1. 属性  

```java
/**
 * 节点类，用于存储数据
 */
static class Node<E> {
    E item;
    Node<E> next;

    Node(E x) { item = x; }
}

/** 阻塞队列的大小，默认为Integer.MAX_VALUE */
private final int capacity;

/** 当前阻塞队列中的元素个数 */
private final AtomicInteger count = new AtomicInteger();

/**
 * 阻塞队列的头结点
 */
transient Node<E> head;

/**
 * 阻塞队列的尾节点
 */
private transient Node<E> last;

/** 获取并移除元素时使用的锁，如take, poll, etc */
private final ReentrantLock takeLock = new ReentrantLock();

/** notEmpty条件对象，当队列没有数据时用于挂起执行删除的线程 */
private final Condition notEmpty = takeLock.newCondition();

/** 添加元素时使用的锁如 put, offer, etc */
private final ReentrantLock putLock = new ReentrantLock();

/** notFull条件对象，当队列数据已满时用于挂起执行添加的线程 */
private final Condition notFull = putLock.newCondition();
```

&emsp; 每个添加到LinkedBlockingQueue队列中的数据都将被封装成Node节点，添加到链表队列中，其中head和last分别指向队列的头结点和尾结点。与ArrayBlockingQueue不同的是，LinkedBlockingQueue内部分别使用了takeLock 和 putLock对并发进行控制，也就是说，添加和删除操作并不是互斥操作，可以同时进行，这样也就可以大大提高吞吐量。  
&emsp; 这里如果不指定队列的容量大小，也就是使用默认的Integer.MAX_VALUE，如果存在添加速度大于删除速度时候，有可能会内存溢出。  

#### 1.2.3.2. 入队  
&emsp; LinkedBlockingQueue提供了多种入队操作的实现来满足不同情况下的需求，入队操作有如下几种：  

* void put(E e)；
* boolean offer(E e)；
* boolean offer(E e, long timeout, TimeUnit unit)。

```java
public void put(E e) throws InterruptedException {
    if (e == null) throw new NullPointerException();
    int c = -1;
    Node<E> node = new Node<E>(e);
    final ReentrantLock putLock = this.putLock;
    final AtomicInteger count = this.count;
    // 获取锁中断
    putLock.lockInterruptibly();
    try {
        //判断队列是否已满，如果已满阻塞等待
        while (count.get() == capacity) {
            notFull.await();
        }
        // 把node放入队列中
        enqueue(node);
        c = count.getAndIncrement();
        // 再次判断队列是否有可用空间，如果有唤醒下一个线程进行添加操作
        if (c + 1 < capacity)
            notFull.signal();
    } finally {
        putLock.unlock();
    }
    // 如果队列中有一条数据，唤醒消费线程进行消费
    if (c == 0)
        signalNotEmpty();
}
```
&emsp; 小结put方法来看，它总共做了以下情况的考虑：  

* 队列已满，阻塞等待。
* 队列未满，创建一个node节点放入队列中，如果放完以后队列还有剩余空间，继续唤醒下一个添加线程进行添加。如果放之前队列中没有元素，放完以后要唤醒消费线程进行消费。

```java
public boolean offer(E e) {
    if (e == null) throw new NullPointerException();
    final AtomicInteger count = this.count;
    if (count.get() == capacity)
        return false;
    int c = -1;
    Node<E> node = new Node<E>(e);
    final ReentrantLock putLock = this.putLock;
    putLock.lock();
    try {
        // 队列有可用空间，放入node节点，判断放入元素后是否还有可用空间，
        // 如果有，唤醒下一个添加线程进行添加操作。
        if (count.get() < capacity) {
            enqueue(node);
            c = count.getAndIncrement();
            if (c + 1 < capacity)
                notFull.signal();
        }
    } finally {
        putLock.unlock();
    }
    if (c == 0)
        signalNotEmpty();
    return c >= 0;
}
```
&emsp; 可以看到offer仅仅对put方法改动了一点点，当队列没有可用元素的时候，不同于put方法的阻塞等待，offer方法直接返回false。  

```java
public boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {

    if (e == null) throw new NullPointerException();
    long nanos = unit.toNanos(timeout);
    int c = -1;
    final ReentrantLock putLock = this.putLock;
    final AtomicInteger count = this.count;
    putLock.lockInterruptibly();
    try {
        // 等待超时时间nanos，超时时间到了返回false
        while (count.get() == capacity) {
            if (nanos <= 0)
                return false;
            nanos = notFull.awaitNanos(nanos);
        }
        enqueue(new Node<E>(e));
        c = count.getAndIncrement();
        if (c + 1 < capacity)
            notFull.signal();
    } finally {
        putLock.unlock();
    }
    if (c == 0)
        signalNotEmpty();
    return true;
}
```
&emsp; 该方法只是对offer方法进行了阻塞超时处理，使用了Condition的awaitNanos来进行超时等待，这里为什么要用while循环？因为awaitNanos方法是可中断的，为了防止在等待过程中线程被中断，这里使用while循环进行等待过程中中断的处理，继续等待剩下需等待的时间。  

#### 1.2.3.3. 出队  
&emsp; LinkedBlockingQueue提供了多种出队操作的实现来满足不同情况下的需求，如下：  

* E take();
* E poll();
* E poll(long timeout, TimeUnit unit);

```java
public E take() throws InterruptedException {
    E x;
    int c = -1;
    final AtomicInteger count = this.count;
    final ReentrantLock takeLock = this.takeLock;
    takeLock.lockInterruptibly();
    try {
        // 队列为空，阻塞等待
        while (count.get() == 0) {
            notEmpty.await();
        }
        x = dequeue();
        c = count.getAndDecrement();
        // 队列中还有元素，唤醒下一个消费线程进行消费
        if (c > 1)
            notEmpty.signal();
    } finally {
        takeLock.unlock();
    }
    // 移除元素之前队列是满的，唤醒生产线程进行添加元素
    if (c == capacity)
        signalNotFull();
    return x;
}
```

&emsp; take方法看起来就是put方法的逆向操作，它总共做了以下情况的考虑：  

* 队列为空，阻塞等待。
* 队列不为空，从队首获取并移除一个元素，如果消费后还有元素在队列中，继续唤醒下一个消费线程进行元素移除。如果放之前队列是满元素的情况，移除完后要唤醒生产线程进行添加元素。

```java
private E dequeue() {
    // 获取到head节点
    Node<E> h = head;
    // 获取到head节点指向的下一个节点
    Node<E> first = h.next;
    // head节点原来指向的节点的next指向自己，等待下次gc回收
    h.next = h; // help GC
    // head节点指向新的节点
    head = first;
    // 获取到新的head节点的item值
    E x = first.item;
    // 新head节点的item值设置为null
    first.item = null;
    return x;
}
```



