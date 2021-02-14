
<!-- TOC -->

- [1. CAS算法](#1-cas算法)
    - [1.1. CAS算法思想](#11-cas算法思想)
    - [1.2. CAS缺点](#12-cas缺点)
    - [1.3. Unsafe类](#13-unsafe类)

<!-- /TOC -->

# 1. CAS算法  
<!-- 
 【对线面试官】 CAS 
https://mp.weixin.qq.com/s/rdMsqbawKSC86AXlcWfkCg
-->

## 1.1. CAS算法思想  
<!-- 
CAS包含了 3个操作数：需要读写的内存位置V、进行比较的值A和拟写入的新值 B。当且仅当V的值等于A时，CAS才会通过原子方式用新值B来更新V的值，否则不会执 行任何操作。无论位置V的值是否等于A,都将返回V原有的值。(这种变化形式被称为比较 并设置，无论操作是否成功都会返回。)
-->
&emsp; **<font color = "lime">CAS，Compare And Swap，即比较并交换。一种无锁原子算法，CAS是一种乐观锁。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-29.png)   
&emsp; 在函数CAS(V,E,N)中有3个参数。读取当前值E，计算结果值V，<font color = "lime">将读取的当前值E和当前新值N作比较，如果相等，更新为V；</font>如果不相等，再次读取当前值E计算结果V，将E再和新的当前值N比较，直到相等。  
&emsp; 注：当多个线程同时使用CAS操作一个变量时，只有一个会胜出，并成功更新，其余均会失败。**一般，失败的线程不会挂起，仅是被告知失败，并且允许再次尝试，当然也允许实现的线程放弃操作(一般情况下，这是一个自旋操作，即不断的重试)**。基于这样的原理，CAS操作即使没有锁，也可以发现其他线程对当前线程的干扰。  

## 1.2. CAS缺点  
&emsp; CAS虽然很高效的解决原子操作，但是CAS仍然存在三大问题：循环时间长开销大、只能保证一个共享变量的原子操作、ABA问题。  

* 循环时间长开销大：  
  &emsp; **<font color = "red">自旋CAS如果长时间不成功，会给CPU带来非常大的执行开销。</font>** 如果JVM能支持处理器提供的pause指令那么效率会有一定的提升，pause指令有两个作用，第一它可以延迟流水线执行指令(de-pipeline)，使CPU不会消耗过多的执行资源，延迟的时间取决于具体实现的版本，在一些处理器上延迟时间是零。第二它可以避免在退出循环的时候因内存顺序冲突(memory order violation)而引起CPU流水线被清空(CPU pipeline flush)，从而提高CPU的执行效率。  
* **<font color = "red">只能保证一个共享变量的原子操作：</font>**  
  &emsp; 当对一个共享变量执行操作时，可以使用循环CAS的方式来保证原子操作，但是对多个共享变量操作时，循环CAS就无法保证操作的原子性，这个时候就可以用锁，或者有一个取巧的办法，就是把多个共享变量合并成一个共享变量来操作。比如有两个共享变量i＝2，j=a，合并一下ij=2a，然后用CAS来操作ij。<font color = "red">从Java1.5开始JDK提供了AtomicReference类来保证引用对象之间的原子性，可以把多个变量放在一个对象里来进行CAS操作。</font>    
* ABA问题(A修改为B，再修改为A)：其他线程修改数次后的值和原值相同。  
  &emsp; 因为CAS需要在操作值的时候检查下值有没有发生变化，如果没有发生变化则更新，但是如果一个值原来是A，变成了B，又变成了A，那么使用CAS进行检查时会发现它的值没有发生变化，但是实际上却变化了。  
  ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-30.png)  
  &emsp; 示例：线程1要将当前值5修改为7，线程2已经将5修改为3，可是线程3又将当前3修改为5。  
  &emsp; ABA问题的解决思路就是使用版本号。在变量前面追加上版本号，每次变量更新的时候把版本号加一，那么A－B－A 就会变成1A-2B－3A。   
  &emsp; **<font color = "red">从Java1.5开始JDK的atomic包里提供了</font>**[AtomicStampedReference](/docs/java/concurrent/6.AtomicStampedReference.md) **<font color = "red">和AtomicMarkableReference类来解决ABA问题。</font>**  

## 1.3. Unsafe类  

    此处只讲解Unsafe类，关于JUC包下的类如何使用Unsafe类，后面分析。

&emsp; <font color = "red">CAS并发原语在Java语言中体现在sum.misc.Unsafe 类中的各个方法。</font>调用Unsafe类中的CAS方法，JVM会实现出CAS汇编指令。这是一种完全依赖于硬件的功能，通过它实现了原子操作。  

    Java无法直接访问底层操作系统，而是通过本地(native)方法来访问。不过尽管如此，JVM还是开了一个后门，JDK中有一个类Unsafe，它提供了硬件级别的原子操作。
    UnSafe 类中的所有方法都是 native 修饰的，也就是说该类中的方法都是直接调用操作系统底层资源执行相应任务。
    这个类尽管里面的方法都是public的，但是并没有办法使用它们，JDK API文档也没有提供任何关于这个类的方法的解释。总而言之，对于Unsafe类的使用都是受限制的，只有授信的代码才能获得该类的实例，当然JDK库里面的类是可以随意使用的。  

<!-- 
CAS在JAVA的底层实现是通过指令 lock cmpxchg实现的。lock保证当执行cmpxchg时其他cpu不允许对其做修改，保证原子性。  
-->
&emsp; Unsafe类的三个方法：  

```java
public final native boolean compareAndSwapObject(Object paramObject1, long paramLong, Object paramObject2, Object paramObject3);
public final native boolean compareAndSwapInt(Object paramObject, long paramLong, int paramInt1, int paramInt2);
public final native boolean compareAndSwapLong(Object paramObject, long paramLong1, long paramLong2, long paramLong3);
```
