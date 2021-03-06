

<!-- TOC -->

- [1. Atomic，原子类](#1-atomic原子类)
    - [1.1. atomic类简介](#11-atomic类简介)
    - [1.2. atomic分类](#12-atomic分类)
        - [1.2.1. 原子更新基本类型或引用类型](#121-原子更新基本类型或引用类型)
        - [1.2.2. 原子更新数组中的元素](#122-原子更新数组中的元素)
        - [1.2.3. 原子更新对象中的字段](#123-原子更新对象中的字段)
        - [1.2.4. 高性能原子类](#124-高性能原子类)
    - [1.3. ~~atomic分析~~](#13-atomic分析)

<!-- /TOC -->

# 1. Atomic，原子类  
<!-- 
 原子类Atomic家族，细看成员还不少 
 https://mp.weixin.qq.com/s/PWxKLmyyfdqWu0Y-mJpTsg
-->


## 1.1. atomic类简介  
&emsp; 原子操作定义：原子操作是指不会被线程调度机制打断的操作，这种操作一旦开始，就一直运行到结束，中间不会有任何线程上下文切换。原子操作是在多线程环境下避免数据不一致必须的手段。  
&emsp; 原子操作可以是一个步骤，也可以是多个操作步骤，但是其顺序不可以被打乱，也不可以被切割而只执行其中的一部分，将整个操作视作一个整体是原子性的核心特征。  
&emsp; int++并不是一个原子操作，所以当一个线程读取它的值并加1时，另外一个线程有可能会读到之前的值，这就会引发错误。为了解决这个问题，必须保证增加操作是原子的，在JDK1.5之前可以使用同步技术来做到这一点。到JDK1.5，java.util.concurrent.atomic包提供了int和long类型的包装类，它们可以自动的保证对于它们的操作是原子的并且不需要使用同步。  

&emsp; 原子类原理：原子类是基于CAS实现的。  
&emsp; 原子类与锁：原子类不是锁的常规替换方法。仅当对象的重要更新限定于单个变量时才应用它。  
&emsp; 原子类和java.lang.Integer等类的区别：原子类不提供诸如hashCode和compareTo之类的方法。因为原子变量是可变的。  

## 1.2. atomic分类  
&emsp; 原子更新基本类型、原子更新引用类型、原子更新数组、原子更新对象中的字段、高性能原子类  

### 1.2.1. 原子更新基本类型或引用类型  
&emsp; 原子更新基本类型：AtomicBoolean、AtomicInteger、AtomicLong。  
&emsp; 原子更新引用类型：AtomicReference、[AtomicStampedRerence](/docs/java/concurrent/AtomicStampedReference.md)、AtomicMarkableReference。  
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
&emsp; <font color = "clime">高性能原子类，是java8中增加的原子类，它们使用分段的思想，把不同的线程hash到不同的段上去更新，最后再把这些段的值相加得到最终的值，</font>这些类主要有：  

* Striped64是下面四个类的父类。  
* LongAccumulator，long类型的聚合器，需要传入一个long类型的二元操作，可以用来计算各种聚合操作，包括加乘等。  
* [LongAdder](/docs/java/concurrent/LongAdder.md)，long类型的累加器，LongAccumulator的特例，只能用来计算加法，且从0开始计算。  
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

