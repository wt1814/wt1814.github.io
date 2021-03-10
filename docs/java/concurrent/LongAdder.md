


# 1.2.4.1. ★★★~~LongAdder~~  
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

&emsp; AtomicInteger在高并发环境下会有多个线程去竞争一个原子变量，而始终只有一个线程能竞争成功，而其他线程会一直通过CAS自旋尝试获取此原子变量，因此会有一定的性能消耗；<font color = "lime">而LongAdder会将这个原子变量分离成一个Cell数组，每个线程通过Hash获取到自己数组，这样就减少了乐观锁的重试次数，从而在高竞争下获得优势；而在低竞争下表现的又不是很好，可能是因为自己本身机制的执行时间大于了锁竞争的自旋时间，因此在低竞争下表现性能不如AtomicInteger。</font>  


------------------

&emsp; 在Java中提供了多个原子变量的操作类，就是比如AtomicLong、AtomicInteger这些，通过CAS的方式去更新变量，但是失败会无限自旋尝试，导致CPU资源的浪费。  
&emsp; 为了解决高并发下的这个缺点，JDK8中新增了LongAdder类，它的使用就是对解决伪共享的实际应用。  
&emsp; LongAdder继承自Striped64，内部维护了一个Cell数组，核心思想就是把单个变量的竞争拆分，多线程下如果一个Cell竞争失败，转而去其他Cell再次CAS重试。  

```java
/**
    * Table of cells. When non-null, size is a power of 2.
    */
transient volatile Cell[] cells;

/**
    * Base value, used mainly when there is no contention, but also as
    * a fallback during table initialization races. Updated via CAS.
    */
transient volatile long base;

/**
    * Spinlock (locked via CAS) used when resizing and/or creating Cells.
    */
transient volatile int cellsBusy;
```

&emsp; 解决伪共享的真正的核心就在Cell数组，可以看到，Cell数组使用了Contented注解。  
&emsp; 在上面提到数组的内存地址都是连续的，所以数组内的元素经常会被放入一个缓存行，这样的话就会带来伪共享的问题，影响性能。  
&emsp; 这里使用Contented进行填充，就避免了伪共享的问题，使得数组中的元素不再共享一个缓存行。  

```java
@sun.misc.Contended static final class Cell {
    volatile long value;
    Cell(long x) { value = x; }
    final boolean cas(long cmp, long val) {
        return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long valueOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> ak = Cell.class;
            valueOffset = UNSAFE.objectFieldOffset
                (ak.getDeclaredField("value"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
```
