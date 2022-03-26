
<!-- TOC -->

- [1. AtomicStampedReference与AtomicMarkableReference](#1-atomicstampedreference与atomicmarkablereference)
    - [1.1. AtomicStampedReference示例](#11-atomicstampedreference示例)
    - [1.2. AtomicStampedReference类详解](#12-atomicstampedreference类详解)
        - [1.2.1. 源码分析](#121-源码分析)
        - [1.2.2. 示例](#122-示例)
    - [1.3. AtomicMarkableReference](#13-atomicmarkablereference)

<!-- /TOC -->

# 1. AtomicStampedReference与AtomicMarkableReference

## 1.1. AtomicStampedReference示例  
&emsp; AtomicStampedReference每次修改都会让stamp值加1，类似于版本控制号。  

## 1.2. AtomicStampedReference类详解 
<!-- 

CAS底层原理与ABA问题
https://mp.weixin.qq.com/s/FaM3jCJeLQYIcfZZlpZXeA

--> 
&emsp; <font color = "red">Java1.5中提供了AtomicStampedReference这个类，通过包装[E,int]的元组来对对象标记版本戳stamp，从而避免ABA问题。</font>这个类的compareAndSet方法作用是首先检查当前引用是否等于预期引用，并且当前标志是否等于预期标志，如果全部相等，则以原子方式将该引用和该标志的值设置为给定的更新值。 


### 1.2.1. 源码分析  
![image](http://www.wt1814.com/static/view/images/java/concurrent/concurrent-28.png)  
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
&emsp; **<font color = "blue">将元素值和版本号绑定在一起，存储在Pair的reference和stamp(邮票、戳的意思)中。</font>**  

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

### 1.2.2. 示例  
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


## 1.3. AtomicMarkableReference
&emsp; **<font color = "clime">AtomicStampedReference可以知道引用变量中途被更改了几次。有时候，并不关心引用变量更改了几次，只是单纯的关心是否更改过，所以就有了AtomicMarkableReference。</font>**  
&emsp; AtomicMarkableReference的唯一区别就是不再用int标识引用，而是使用boolean变量——表示引用变量是否被更改过。  