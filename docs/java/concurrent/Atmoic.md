

<!-- TOC -->

- [1. Atomic，原子类](#1-atomic原子类)
    - [1.1. atomic类简介](#11-atomic类简介)
    - [1.2. atomic分类](#12-atomic分类)
        - [1.2.1. 原子更新基本类型或引用类型](#121-原子更新基本类型或引用类型)
            - [1.2.1.1. AtomicStampedReference类详解](#1211-atomicstampedreference类详解)
                - [1.2.1.1.1. 源码分析](#12111-源码分析)
                - [1.2.1.1.2. 示例](#12112-示例)
        - [1.2.2. 原子更新数组中的元素](#122-原子更新数组中的元素)
        - [1.2.3. 原子更新对象中的字段](#123-原子更新对象中的字段)
        - [1.2.4. 高性能原子类](#124-高性能原子类)
            - [1.2.4.1. LongAdder](#1241-longadder)
    - [1.3. ~~atomic分析~~](#13-atomic分析)

<!-- /TOC -->

# 1. Atomic，原子类  
&emsp; 原子更新基本类型、原子更新引用类型、原子更新数组、原子更新对象中的字段、高性能原子类  

## 1.1. atomic类简介  
&emsp; 原子操作定义：原子操作是指不会被线程调度机制打断的操作，这种操作一旦开始，就一直运行到结束，中间不会有任何线程上下文切换。原子操作是在多线程环境下避免数据不一致必须的手段。  
&emsp; 原子操作可以是一个步骤，也可以是多个操作步骤，但是其顺序不可以被打乱，也不可以被切割而只执行其中的一部分，将整个操作视作一个整体是原子性的核心特征。  
&emsp; int++并不是一个原子操作，所以当一个线程读取它的值并加1时，另外一个线程有可能会读到之前的值，这就会引发错误。为了解决这个问题，必须保证增加操作是原子的，在JDK1.5之前可以使用同步技术来做到这一点。到JDK1.5，java.util.concurrent.atomic包提供了int和long类型的包装类，它们可以自动的保证对于它们的操作是原子的并且不需要使用同步。  

&emsp; 原子类原理：原子类是基于CAS实现的。  
&emsp; 原子类与锁：原子类不是锁的常规替换方法。仅当对象的重要更新限定于单个变量时才应用它。  
&emsp; 原子类和java.lang.Integer等类的区别：原子类不提供诸如hashCode和compareTo之类的方法。因为原子变量是可变的。  

## 1.2. atomic分类  
### 1.2.1. 原子更新基本类型或引用类型  
&emsp; 原子更新基本类型：AtomicBoolean、AtomicInteger、AtomicLong。  
&emsp; 原子更新引用类型：AtomicReference、AtomicStampedRerence、AtomicMarkableReference。  
&emsp; 这几个类的操作基本类似，底层都是调用Unsafe的compareAndSwapXxx()来实现，基本用法如下：  

```java
private static void testAtomicReference() {

    AtomicInteger atomicInteger = new AtomicInteger(1);
    atomicInteger.incrementAndGet();
    atomicInteger.getAndIncrement();
    atomicInteger.compareAndSet(3, 666);
    System.out.println(atomicInteger.get());

    AtomicStampedReference<Integer> atomicStampedReference = new AtomicStampedReference<>(1, 1);
    atomicStampedReference.compareAndSet(1, 2, 1, 3);
    atomicStampedReference.compareAndSet(2, 666, 3, 5);
    System.out.println(atomicStampedReference.getReference());
    System.out.println(atomicStampedReference.getStamp());
}
```

#### 1.2.1.1. AtomicStampedReference类详解 
<!-- 

CAS底层原理与ABA问题
https://mp.weixin.qq.com/s/FaM3jCJeLQYIcfZZlpZXeA

--> 
&emsp; <font color = "red">Java1.5中提供了AtomicStampedReference这个类，通过包装[E,int]的元组来对对象标记版本戳stamp，从而避免ABA问题。</font>这个类的compareAndSet方法作用是首先检查当前引用是否等于预期引用，并且当前标志是否等于预期标志，如果全部相等，则以原子方式将该引用和该标志的值设置为给定的更新值。 

##### 1.2.1.1.1. 源码分析  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-28.png)  
&emsp; **内部类**  

```java
private static class Pair<T> {
    final T reference;
    final int stamp;
    private Pair(T reference, int stamp) {
        this.reference = reference;
        this.stamp = stamp;
    }
    static <T> Pair<T> of(T reference, int stamp) {
        return new Pair<T>(reference, stamp);
    }
}
```
&emsp; 将元素值和版本号绑定在一起，存储在Pair的reference和stamp(邮票、戳的意思)中。  

&emsp; **属性：**  

```java
private volatile Pair<V> pair;
private static final sun.misc.Unsafe UNSAFE = sun.misc.Unsafe.getUnsafe();
private static final long pairOffset = objectFieldOffset(UNSAFE, "pair", AtomicStampedReference.class);
```
&emsp; 声明一个Pair类型的变量并使用Unsfae获取其偏移量，存储到pairOffset中。  
&emsp; CAS算法核心类，sun.misc.Unsafe提供了访问底层的机制(native()方法也有访问底层的功能)，这种机制仅供java核心类库使用。  

&emsp; **构造方法：** &emsp;  

```java
/**
 * @param initialRef 初始值
 * @param initialStamp 初始版本号
 */
public AtomicStampedReference(V initialRef, int initialStamp) {
    pair = Pair.of(initialRef, initialStamp);
}
```
&emsp; **compareAndSet()方法**  

```java
public boolean compareAndSet(V expectedReference, V newReference, int expectedStamp, int newStamp) {
    // 获取当前的(元素值，版本号)对
    Pair<V> current = pair;
    return
        // 引用没变
        expectedReference == current.reference &&
                // 版本号没变
                expectedStamp == current.stamp &&
                // 新引用等于旧引用
                ((newReference == current.reference &&
                        // 新版本号等于旧版本号
                        newStamp == current.stamp) ||
                        // 构造新的Pair对象并CAS更新
                        casPair(current, Pair.of(newReference, newStamp)));
}

private boolean casPair(Pair<V> cmp, Pair<V> val) {
    // 调用Unsafe的compareAndSwapObject()方法CAS更新pair的引用为新引用
    return UNSAFE.compareAndSwapObject(this, pairOffset, cmp, val);
}
```

* 如果元素值和版本号都没有变化，并且和新的也相同，返回true；  
* 如果元素值和版本号都没有变化，并且和新的不完全相同，就构造一个新的Pair对象并执行CAS更新pair。 

##### 1.2.1.1.2. 示例  
&emsp; 示例代码：分别用AtomicInteger和AtomicStampedReference来对初始值为100的原子整型变量进行更新，AtomicInteger会成功执行CAS操作，而加上版本戳的AtomicStampedReference对于ABA问题会执行CAS失败：  

```java
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;

public class ABA {
    private static AtomicInteger atomicInt = new AtomicInteger(100);
    private static AtomicStampedReference atomicStampedRef = new AtomicStampedReference(100, 0);

    public static void main(String[] args) throws InterruptedException {
        Thread intT1 = new Thread(new Runnable() {
            @Override
            public void run() {
                atomicInt.compareAndSet(100, 101);
                atomicInt.compareAndSet(101, 100);
            }
        });

        Thread intT2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
                boolean c3 = atomicInt.compareAndSet(100, 101);
                System.out.println(c3); // true
            }
        });

        intT1.start();
        intT2.start();
        intT1.join();
        intT2.join();

        Thread refT1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
                atomicStampedRef.compareAndSet(100, 101, atomicStampedRef.getStamp(), atomicStampedRef.getStamp() + 1);
                atomicStampedRef.compareAndSet(101, 100, atomicStampedRef.getStamp(), atomicStampedRef.getStamp() + 1);
            }
        });

        Thread refT2 = new Thread(new Runnable() {
            @Override
            public void run() {
                int stamp = atomicStampedRef.getStamp();
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                }
                boolean c3 = atomicStampedRef.compareAndSet(100, 101, stamp, stamp + 1);
                System.out.println(c3); // false
            }
        });

        refT1.start();
        refT2.start();
    }
}
```

### 1.2.2. 原子更新数组中的元素  
&emsp; 原子更新数组：AtomicIntegerArray、AtomicLongArray、AtomicReferenceArray。  
&emsp; 这几个类的操作基本类似，更新元素时都要指定在数组中的索引位置，基本用法如下：  

```java
private static void testAtomicReferenceArray() {

    AtomicIntegerArray atomicIntegerArray = new AtomicIntegerArray(10);
    atomicIntegerArray.getAndIncrement(0);
    atomicIntegerArray.getAndAdd(1, 666);
    atomicIntegerArray.incrementAndGet(2);
    atomicIntegerArray.addAndGet(3, 666);
    atomicIntegerArray.compareAndSet(4, 0, 666);

    System.out.println(atomicIntegerArray.get(0));
    System.out.println(atomicIntegerArray.get(1));
    System.out.println(atomicIntegerArray.get(2));
}
```

### 1.2.3. 原子更新对象中的字段  
&emsp; 原子更新对象的属性：AtomicIntegerFieldUpdater、AtomicLongFieldUpdater、AtomicReferenceFieldUpdater。这几个类的操作基本类似，都需要传入要更新的字段名称，基本用法如下：  

```java
private static void testAtomicReferenceField() {

    AtomicReferenceFieldUpdater<User, String> updateName = AtomicReferenceFieldUpdater.newUpdater(User.class, String.class,"name");
    AtomicIntegerFieldUpdater<User> updateAge = AtomicIntegerFieldUpdater.newUpdater(User.class, "age");
    User user = new User("tong ge", 21);
    updateName.compareAndSet(user, "tong ge", "read source code");
    updateAge.compareAndSet(user, 21, 25);
    updateAge.incrementAndGet(user);
    System.out.println(user);
}
```

### 1.2.4. 高性能原子类  
&emsp; <font color = "lime">高性能原子类，是java8中增加的原子类，它们使用分段的思想，把不同的线程hash到不同的段上去更新，最后再把这些段的值相加得到最终的值，</font>这些类主要有：  

* Striped64是下面四个类的父类。  
* LongAccumulator，long类型的聚合器，需要传入一个long类型的二元操作，可以用来计算各种聚合操作，包括加乘等。  
* LongAdder，long类型的累加器，LongAccumulator的特例，只能用来计算加法，且从0开始计算。  
* DoubleAccumulator，double类型的聚合器，需要传入一个double类型的二元操作，可以用来计算各种聚合操作，包括加乘等。  
* DoubleAdder，double类型的累加器，DoubleAccumulator的特例，只能用来计算加法，且从0开始计算。  

&emsp; 这几个类的操作基本类似，其中DoubleAccumulator和DoubleAdder底层其实也是用long来实现的，基本用法如下：  

```java
private static void testNewAtomic() {

    LongAdder longAdder = new LongAdder();
    longAdder.increment();
    longAdder.add(666);
    System.out.println(longAdder.sum());
    //667

    LongAccumulator longAccumulator = new LongAccumulator((left, right)->left + right * 2, 666);
    longAccumulator.accumulate(1);
    longAccumulator.accumulate(3);
    longAccumulator.accumulate(-4);
    System.out.println(longAccumulator.get());
    //666
}
```

#### 1.2.4.1. LongAdder  
<!-- 
阿里为什么推荐使用LongAdder，而不是volatile？ 
https://mp.weixin.qq.com/s/lpk5l4m0oFpPDDf6fl8mmQ

比AtomicLong更优秀的LongAdder确定不来了解一下吗？ 
https://mp.weixin.qq.com/s/rJAIoZLe9lnEcTj3SmgIZw

-->

&emsp; <font color = "lime">LongAdder，分段锁。</font>  

    &emsp; 阿里《Java开发手册》嵩山版：    
    &emsp; 【参考】volatile 解决多线程内存不可见问题。对于一写多读，是可以解决变量同步问题，但是如果多写，同样无法解决线程安全问题。  
    &emsp; 说明：如果是count++操作，使用如下类实现：AtomicInteger count = new AtomicInteger(); count.addAndGet(1); 如果是JDK8，推荐使用 LongAdder对象，比AtomicLong性能更好(减少乐观锁的重试次数)。  

&emsp; AtomicInteger 在高并发环境下会有多个线程去竞争一个原子变量，而始终只有一个线程能竞争成功，而其他线程会一直通过CAS自旋尝试获取此原子变量，因此会有一定的性能消耗；<font color = "lime">而LongAdder会将这个原子变量分离成一个Cell数组，每个线程通过Hash获取到自己数组，这样就减少了乐观锁的重试次数，从而在高竞争下获得优势；而在低竞争下表现的又不是很好，可能是因为自己本身机制的执行时间大于了锁竞争的自旋时间，因此在低竞争下表现性能不如 AtomicInteger。</font>  


## 1.3. ~~atomic分析~~  
&emsp; 查看AtomicInteger代码如下，可以看到该类下的方法大部分是调用了Unsafe类  

```java
public class AtomicInteger extends Number implements java.io.Serializable {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;
    static {
        try {
            //用于获取value字段相对当前对象的“起始地址”的偏移量
            valueOffset = unsafe.objectFieldOffset(AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }
    private volatile int value;
    //返回当前值
    public final int get() {
        return value;
    }
    //递增加detla
    public final int getAndAdd(int delta) {
        //三个参数，1、当前的实例 2、value实例变量的偏移量 3、当前value要加上的数(value+delta)。
        return unsafe.getAndAddInt(this, valueOffset, delta);
    }
    //递增加1
    public final int incrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
    }
    //...
}
```
&emsp; AtomicInteger底层用的是volatile的变量和CAS来进行更改数据的。volatile保证线程的可见性，多线程并发时，一个线程修改数据，可以保证其它线程立马看到修改后的值；CAS保证数据更新的原子性。  

&emsp; valueOffset：AtomicInteger中的变量valueOffset表示该变量值在内存中的偏移地址，因为UnSafe就是根据内存偏移地址获取数据。  
&emsp; volatile int value：变量value用volatile修饰，保证了多线程之间的内存可见性。  

