
<!-- TOC -->

- [1. CAS算法](#1-cas算法)
    - [1.1. CAS算法思想](#11-cas算法思想)
    - [1.2. CAS缺点](#12-cas缺点)
    - [1.3. Unsafe类](#13-unsafe类)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; **<font color = "clime">在函数CAS(V,E,N)中有3个参数：从内存中读取的值E，计算的结果值V，内存中的当前值N(可能已经被其他线程改变)。</font>** 
&emsp; **<font color = "clime">函数流程：</font>** s1. 读取当前值E，2. 计算结果值V，<font color = "clime">3. 将读取的当前值E和当前新值N作比较，如果相等，更新为V；</font>4. 如果不相等，再次读取当前值E计算结果V，将E再和新的当前值N比较，直到相等。 

&emsp; **CAS缺点：**  

* 循环时间长开销大。自旋CAS如果长时间不成功，会给CPU带来非常大的执行开销。  
* 只能保证一个共享变量的原子操作。 **<font color = "clime">从Java1.5开始JDK提供了AtomicReference类来保证引用对象之间的原子性，可以把多个变量放在一个对象里来进行CAS操作。</font>**  
* ABA问题。  
    1. 什么是ABA问题？  
    &emsp; ABA示例：  
    &emsp; 1).在多线程的环境中，线程a从共享的地址X中读取到了对象A。  
    &emsp; 2).在线程a准备对地址X进行更新之前，线程a挂起。线程b将地址X中的值修改为了B。  
    &emsp; 3).接着线程b或者线程c将地址X中的值又修改回了A。  
    &emsp; 4).线程a恢复，接着对地址X执行CAS，发现X中存储的还是对象A，对象匹配，CAS成功。  
    2. ABA问题需不需要解决？   
    &emsp; ~~如果依赖中间变化的状态，需要解决。如果不是依赖中间变化的状态，对业务结果无影响。~~  
    3. 解决ABA问题。  
    &emsp; **<font color = "red">ABA问题的解决思路就是使用版本号。在变量前面追加上版本号，每次变量更新的时候把版本号加一，那么A－B－A 就会变成1A-2B－3A。</font>**   
    &emsp; **<font color = "clime">从Java1.5开始JDK的atomic包里提供了[AtomicStampedReference](/docs/java/concurrent/6.AtomicStampedReference.md)和AtomicMarkableReference类来解决ABA问题。</font>**  

# 1. CAS算法  
<!-- 
【对线面试官】 CAS 
https://mp.weixin.qq.com/s/rdMsqbawKSC86AXlcWfkCg
-->

## 1.1. CAS算法思想  
<!-- 
CAS包含了 3个操作数：需要读写的内存位置V、进行比较的值A和拟写入的新值 B。当且仅当V的值等于A时，CAS才会通过原子方式用新值B来更新V的值，否则不会执 行任何操作。无论位置V的值是否等于A,都将返回V原有的值。(这种变化形式被称为比较 并设置，无论操作是否成功都会返回。)
-->
&emsp; **<font color = "clime">CAS，Compare And Swap，即比较并交换。一种无锁原子算法，CAS是一种乐观锁。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-29.png)   
&emsp; **<font color = "clime">在函数CAS(V,E,N)中有3个参数：从内存中读取的值E，计算的结果值V，内存中的当前值N(可能已经被其他线程改变)。</font>** 
&emsp; **<font color = "clime">函数流程：</font>** s1. 读取当前值E，2. 计算结果值V，<font color = "clime">3. 将读取的当前值E和当前新值N作比较，如果相等，更新为V；</font>4. 如果不相等，再次读取当前值E计算结果V，将E再和新的当前值N比较，直到相等。  
&emsp; 注：当多个线程同时使用CAS操作一个变量时，只有一个会胜出，并成功更新，其余均会失败。**一般，失败的线程不会挂起，仅是被告知失败，并且允许再次尝试，当然也允许实现的线程放弃操作(一般情况下，这是一个自旋操作，即不断的重试)。**基于这样的原理，CAS操作即使没有锁，也可以发现其他线程对当前线程的干扰。  

## 1.2. CAS缺点  
&emsp; CAS虽然很高效的解决原子操作，但是CAS仍然存在三大问题：循环时间长开销大、只能保证一个共享变量的原子操作、ABA问题。  

* 循环时间长开销大：  
&emsp; **<font color = "lime">自旋CAS如果长时间不成功，会给CPU带来非常大的执行开销。</font>** ~~如果JVM能支持处理器提供的pause指令那么效率会有一定的提升，pause指令有两个作用，第一它可以延迟流水线执行指令(de-pipeline)，使CPU不会消耗过多的执行资源，延迟的时间取决于具体实现的版本，在一些处理器上延迟时间是零。第二它可以避免在退出循环的时候因内存顺序冲突(memory order violation)而引起CPU流水线被清空(CPU pipeline flush)，从而提高CPU的执行效率。~~  
* **<font color = "red">只能保证一个共享变量的原子操作：</font>**  
&emsp; 当对一个共享变量执行操作时，可以使用循环CAS的方式来保证原子操作，但是对多个共享变量操作时，循环CAS就无法保证操作的原子性，这个时候就可以用锁，或者有一个取巧的办法，就是把多个共享变量合并成一个共享变量来操作。比如有两个共享变量i＝2，j=a，合并一下ij=2a，然后用CAS来操作ij。  
&emsp; **<font color = "clime">从Java1.5开始JDK提供了AtomicReference类来保证引用对象之间的原子性，可以把多个变量放在一个对象里来进行CAS操作。</font>**    
* **<font color = "red">ABA问题(A修改为B，再修改为A)：其他线程修改数次后的值和原值相同。</font>**  
    1. 什么是ABA问题？  
    &emsp; ABA示例：  
    &emsp; 1).在多线程的环境中，线程a从共享的地址X中读取到了对象A。  
    &emsp; 2).在线程a准备对地址X进行更新之前，线程a挂起。线程b将地址X中的值修改为了B。  
    &emsp; 3).接着线程b或者线程c将地址X中的值又修改回了A。  
    &emsp; 4).线程a恢复，接着对地址X执行CAS，发现X中存储的还是对象A，对象匹配，CAS成功。   
    2. ABA问题需不需要解决？   
    &emsp; ~~如果依赖中间变化的状态，需要解决。如果不是依赖中间变化的状态，对业务结果无影响。~~  
    3. 解决ABA问题。
    &emsp; **<font color = "red">ABA问题的解决思路就是使用版本号。在变量前面追加上版本号，每次变量更新的时候把版本号加一，那么A－B－A 就会变成1A-2B－3A。</font>**   
    &emsp; **<font color = "clime">从Java1.5开始JDK的atomic包里提供了[AtomicStampedReference](/docs/java/concurrent/6.AtomicStampedReference.md)和AtomicMarkableReference类来解决ABA问题。</font>**  

## 1.3. Unsafe类  
<!-- 
CAS在JAVA的底层实现是通过指令 lock cmpxchg实现的。lock保证当执行cmpxchg时其他cpu不允许对其做修改，保证原子性。  
-->

    此处只讲解Unsafe类，关于JUC包下的类如何使用Unsafe类，后面分析。

&emsp; <font color = "red">CAS并发原语在Java语言中体现在sum.misc.Unsafe 类中的各个方法。</font>调用Unsafe类中的CAS方法，JVM会实现出CAS汇编指令。这是一种完全依赖于硬件的功能，通过它实现了原子操作。  

    Java无法直接访问底层操作系统，而是通过本地(native)方法来访问。不过尽管如此，JVM还是开了一个后门，JDK中有一个类Unsafe，它提供了硬件级别的原子操作。
    UnSafe 类中的所有方法都是 native 修饰的，也就是说该类中的方法都是直接调用操作系统底层资源执行相应任务。
    这个类尽管里面的方法都是public的，但是并没有办法使用它们，JDK API文档也没有提供任何关于这个类的方法的解释。总而言之，对于Unsafe类的使用都是受限制的，只有授信的代码才能获得该类的实例，当然JDK库里面的类是可以随意使用的。  


&emsp; Unsafe类的三个方法：  

```java
public final native boolean compareAndSwapObject(Object paramObject1, long paramLong, Object paramObject2, Object paramObject3);
public final native boolean compareAndSwapInt(Object paramObject, long paramLong, int paramInt1, int paramInt2);
public final native boolean compareAndSwapLong(Object paramObject, long paramLong1, long paramLong2, long paramLong3);
```
