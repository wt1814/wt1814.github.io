
<!-- TOC -->

- [1. Volatile](#1-volatile)
    - [1.1. Volatile的特性](#11-volatile的特性)
    - [1.2. Volatile原理](#12-volatile原理)
    - [1.3. Volatile使用](#13-volatile使用)
        - [1.3.1. 如何正确使用Volatile变量](#131-如何正确使用volatile变量)
        - [1.3.2. 状态标志](#132-状态标志)
        - [1.3.3. 单例模式的实现](#133-单例模式的实现)
    - [1.4. ~~推荐使用LongAdder~~](#14-推荐使用longadder)

<!-- /TOC -->

&emsp; **<font color = "lime">总结：</font>**  
&emsp; **<font color = "lime">在Volatile写前插入写-写屏障，在Volatile写后插入写-读屏障；在Volatile读后插入读-读屏障、读-写屏障。</font>**  

# 1. Volatile  
<!-- 
~~
借鉴
https://mp.weixin.qq.com/s/0_TDPDx8q2HmKCMyupWuNA
-->
## 1.1. Volatile的特性
&emsp; Volatile的特性：  
1. 不支持原子性。<font color = "red">它只对Volatile变量的单次读/写具有原子性；</font><font color = "lime">但是对于类似i++这样的复合操作不能保证原子性。</font>  

        i++在虚拟机内部有3条指令(读取－修改－写入)执行。表达式i++的操作步骤分解如下：1)从内存中取出i的值；2)计算i的值；3)将i的值写到内存中。    
2. <font color = "red">实现了有序性，禁止进行指令重排序。</font>
<!-- 在Volatile变量的赋值操作后⾯会有⼀个内存屏障(⽣成的汇编代码上)，读操作不会被重排序到内存屏障之前。 -->
3. 实现了可见性。 **Volatile提供happens-before的保证，使变量在多个线程间可见。**变量被修改后，会立即保存在主存中，并清除工作内存中的值。这个变量不会在多个线程中存在复本，直接从内存读取。新值对其他线程来说是立即可见的。  

&emsp; **<font color = "red">总结：Volatile保证了可见性和有序性，同时可以保证单次读/写的原子性。</font>**  

&emsp; **<font color = "red">Synchronized和Volatile比较：</font>**  

* 关键字Volatile是线程同步的轻量级实现，Volatile比Synchronized执行成本更低，因为它不会引起线程上下文的切换和调度。  
* Volatile本质是在告诉jvm当前变量在寄存器(工作内存)中的值是不确定的，需要从主存中读取；Synchronized则是锁定当前变量，只有当前线程可以访问该变量，其他线程被阻塞住。  
* Volatile不会造成线程的阻塞；Synchronized可能会造成线程的阻塞。  
* Volatile仅能实现变量的修改可见性，不能保证原子性；而Synchronized则可以保证变量的修改可见性和原子性。Synchronized可以保证原子性，也可以间接保证可见性，因为它会将私有内存和公共内存中的数据做同步。  
* Volatile标记的变量不会被编译器优化；Synchronized标记的变量可以被编译器优化。  
* Volatile仅能使用在变量级别；Synchronized则可以使用在变量、方法、和类级别的。  

&emsp; 再次重申一下，关键字Volatile解决的是变量在多个线程之间的可见性；而Synchronized解决的是多个线程之间访问资源的同步性，Synchronized可以使多个线程访问同一个资源具有同步性，而且它还具有将线程工作内存中的私有变量与公共内存中的变量同步的功能。  

&emsp; **Volatile和atomic原子类区别：**  

* Volatile变量可以确保先行关系，即写操作会发生在后续的读操作之前，但它并不能保证原子性。例如用Volatile修饰i变量，那么i++ 操作就不是原子性的。  
* atomic原子类提供的atomic方法可以让这种操作具有原子性。如getAndIncrement()方法会原子性的进行增量操作把当前值加一，其它数据类型和引用变量也可以进行相似操作，但是atomic原子类一次只能操作一个共享变量，不能同时操作多个共享变量。  

<!--
除了在i++操作时使用Synchronized关键字实现同步外，还可以使用Atomiclnteger原子类进行实现。
原子操作是不能分割的整体，没有其他线程能够中断或检查正在原子操作中的变量。— 个原子(atomic)类型就是一个原子操作可用的类型，它可以在没有锁的情况下做到线程安全 (thread-safe) 。
-->
## 1.2. Volatile原理  
<!-- 
如何把java文件生成汇编语言
https://mp.weixin.qq.com/s/DFCh1XE1hbikjBGEpYJguw
-->
&emsp; 观察加入Volatile关键字和没有加入Volatile关键字时所生成的汇编代码发现，加入Volatile关键字时，会多出一个lock前缀指令，lock前缀指令实际上相当于一个[内存屏障](/docs/java/concurrent/ConcurrencyProblem.md)。

&emsp; **<font color = "lime">内存屏障的作用：</font>**  

1. **<font color = "lime">(保障有序性)阻⽌屏障两侧的指令重排序。</font>** 它确保指令重排序时不会把其后面的指令排到内存屏障之前的位置，也不会把前面的指令排到内存屏障的后面；即在执行到内存屏障这句指令时，在它前面的操作已经全部完成；  
2. **<font color = "lime">(保障可见性)它会强制将对缓存的修改操作立即写入主存；</font>** **<font color = "red">如果是写操作，会触发总线嗅探机制(MESI)，会导致其他CPU中对应的缓存行无效，会引发伪共享问题。</font>**  

&emsp; 有如下四种内存屏障：  

|屏障类型  |简称  |指令示例 |  说明|
|---|---|---|---|
|StoreStore Barriers |写-写 屏障 |Store1;StoreStore;Store2 |&emsp; 确保Store1数据对其他处理器可见(指刷新到内存)先于Store2及所有后续存储指令的存储。|
|StoreLoad Barriers |写-读 屏障 |Store1;StoreLoad;Load2 |&emsp; 确保Store1数据对其他处理器变得可见(指刷新到内存)先于Load2及所有后续装载指令的装载。<br/>&emsp; StoreLoad Barriers会使屏障之前的所有内存访问指令(存储和装载指令)完成之后，才执行该屏障之后的内存访问指令。|
|LoadLoad Barriers|读-读 屏障 |Load1;LoadLoad;Load2 |&emsp; (Load1代表加载数据，Store1表示刷新数据到内存)确保Load1数据的状态先于Load2及所有后续装载指令的装载。|
|LoadSotre Barriers |读-写 屏障 |Load1;LoadStore;Store2 |&emsp; 确保Load1数据装载先于Store2及所有后续的存储指令刷新到内存。|  
<!-- 
&emsp; StoreStore屏障可以保证在Volatile写(flag赋值操作flag=true)之前，其前面的所有普通写(num的赋值操作num=1) 操作已经对任意处理器可见了，保障所有普通写在Volatile写之前刷新到主内存。  
&emsp; LoadStore屏障可以保证其后面的所有普通写(num的赋值操作num=num+5) 操作必须在Volatile读(if(flag))之后执行。  
--> 

&emsp; **Volatile写的场景如何插入内存屏障：**  

* **<font color = "red">在每个Volatile写操作的前面插入一个StoreStore屏障(写-写 屏障)。禁止上面的普通写与下面的Volatile写重排序。</font>**  
* **<font color = "red">在每个Volatile写操作的后面插入一个StoreLoad屏障(写-读 屏障)。禁止上面的Volatile写与下面可能有的Volatile读/写重排序。</font>**  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-48.png)  

&emsp; **Volatile读场景如何插入内存屏障：**  

* **<font color = "red">在每个Volatile读操作的后面插入一个LoadLoad屏障(读-读 屏障)。禁止下面的普通读操作与上面的Volatile读重排序。</font>**  
* **<font color = "red">在每个Volatile读操作的后面插入一个LoadStore屏障(读-写 屏障)。禁止下面所有的普通写操作和上面Volatile读重排序。</font>**  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-49.png)  

## 1.3. Volatile使用  
### 1.3.1. 如何正确使用Volatile变量  
<!-- 
关键字Volatile主要使用的场合是在多个线程中可以感知实例变量 被更改了，并且可以获得最新的值使用，也就是用多线程读取共享变 量时可以获得最新值使用。
-->
&emsp; **<font color = "red">Volatile的使用场景：</font>** 关键字Volatile用于多线程环境下的单次操作(单次读或者单次写)。即Volatile主要使用的场合是在多个线程中可以感知实例变量被更改了，并且可以获得最新的值使用，也就是用多线程读取共享变量时可以获得最新值使用。  

### 1.3.2. 状态标志
&emsp; 也许实现Volatile变量的规范使用仅仅是使用一个布尔状态标志，用于指示发生了一个重要的一次性事件，例如完成初始化或请求停机。  

```java
Volatile boolean shutdownRequested;  
//...
public void shutdown() {
    shutdownRequested = true;
}

public void doWork() {
    while (!shutdownRequested) {
        // do stuff  
    }
}
```
&emsp; 线程1执行doWork()的过程中，可能有另外的线程2调用了shutdown，所以boolean变量必须是Volatile。  
&emsp; 而如果使用Synchronized块编写循环要比使用Volatile状态标志编写麻烦很多。由于Volatile简化了编码，并且状态标志并不依赖于程序内任何其他状态，因此此处非常适合使用Volatile。  
&emsp; 这种类型的状态标记的一个公共特性是：通常只有一种状态转换；shutdownRequested标志从false 转换为true，然后程序停止。这种模式可以扩展到来回转换的状态标志，但是只有在转换周期不被察觉的情况下才能扩展(从false到true，再转换到false)。此外，还需要某些原子状态转换机制，例如原子变量。  

### 1.3.3. ~~单例模式的实现~~  
<!-- 
https://www.cnblogs.com/jackson0714/p/java_Volatile.html


DCL为什么要用volatile关键字？
https://blog.csdn.net/weixin_38106322/article/details/105753241
-->
<!-- 
说说双重检查加锁单例模式为什么两次判断？
https://mp.weixin.qq.com/s/OG2_Iq0Xe1DD_ghtMYX2Ow
-->
&emsp; 单例模式的实现，典型的双重检查锁定(DCL)  

```java
class VolatileSingleton {
    private static Volatile VolatileSingleton instance = null;

    private VolatileSingleton() {
        System.out.println(Thread.currentThread().getName() + "\t 我是构造方法SingletonDemo");
    }
    public static VolatileSingleton getInstance() {
        // 第一重检测
        if(instance == null) {
            // 锁定代码块
            Synchronized (VolatileSingleton.class) {
                // 第二重检测
                if(instance == null) {
                    // 实例化对象
                    instance = new VolatileSingleton();
                }
            }
        }
        return instance;
    }
}
```
&emsp; 这是一种懒汉的单例模式，使用时才创建对象，而且为了避免初始化操作的指令重排序，给instance加上了Volatile。

<!-- 
单例模式的双重锁为什么要加Volatile
需要Volatile关键字的原因是，在并发情况下，如果没有Volatile关键字，在第5行会出现问题。instance = new TestInstance();可以分解为3行伪代码
a. memory = allocate() //分配内存
b. ctorInstanc(memory) //初始化对象
c. instance = memory //设置instance指向刚分配的地址
上面的代码在编译运行时，可能会出现重排序从a-b-c排序为a-c-b。在多线程的情况下会出现以下问题。当线程A在执行第5行代码时，B线程进来执行到第2行代码。假设此时A执行的过程中发生了指令重排序，即先执行了a和c，没有执行b。那么由于A线程执行了c导致instance指向了一段地址，所以B线程判断instance不为null，会直接跳到第6行并返回一个未初始化的对象。
-->

## 1.4. ~~推荐使用LongAdder~~  
<!-- 
阿里为什么推荐使用LongAdder，而不是Volatile？ 
https://mp.weixin.qq.com/s/lpk5l4m0oFpPDDf6fl8mmQ
-->
&emsp; 阿里《Java开发手册》嵩山版：  

        【参考】Volatile解决多线程内存不可见问题。对于一写多读，是可以解决变量同步问题，但是如果多写，同样无法解决线程安全问题。  
        说明：如果是count++ 操作，使用如下类实现：AtomicInteger count = new AtomicInteger(); count.addAndGet(1); 如果是JDK8，推荐使用LongAdder对象，比AtomicLong性能更好(减少乐观锁的重试次数)。  

&emsp; **Volatile、AtomicInteger、LongAdder：**  
&emsp; volatile 在多写环境下是非线程安全的。  
&emsp; AtomicInteger在高并发环境下会有多个线程去竞争一个原子变量，而始终只有一个线程能竞争成功，而其他线程会一直通过CAS自旋尝试获取此原子变量，因此会有一定的性能消耗。  
&emsp; <font color = "lime">而LongAdder会将这个原子变量分离成一个Cell数组，每个线程通过Hash获取到自己数组，这样就减少了乐观锁的重试次数，从而在高竞争下获得优势；</font>而在低竞争下表现的又不是很好，可能是因为自己本身机制的执行时间大于了锁竞争的自旋时间，因此在低竞争下表现性能不如AtomicInteger。  
