
<!-- TOC -->

- [1. Volatile](#1-volatile)
    - [1.1. Volatile原理](#11-volatile原理)
    - [1.2. Volatile使用](#12-volatile使用)
        - [1.2.1. 如何正确使用volatile变量](#121-如何正确使用volatile变量)
        - [1.2.2. 状态标志](#122-状态标志)
        - [1.2.3. 单例模式的实现](#123-单例模式的实现)
    - [1.3. 推荐使用LongAdder](#13-推荐使用longadder)

<!-- /TOC -->


# 1. Volatile  
<!--
volatile 
https://mp.weixin.qq.com/s/DFCh1XE1hbikjBGEpYJguw

在某些JVM中，i--的操作要分成如下3步：
1)取得原有i值。
2)计算i-1。
3)对i进行赋值。

关键字synchronized和volatile进行一下比较：
1)关键字volatile是线程同步的轻量级实现，所以volatile性能肯定比synchronized要 好，并且volatile只能修饰于变量，而synchronized可以修饰方法，以及代码块。随着JDK 新版本的发布，synchronized关键字在执行效率上得到很大提升，在开发中使用synchronized 关键字的比率还是比较大的。
2)多线程访问volatile不会发生阻塞，而synchronized会出现阻塞。
3)volatile能保证数据的可见性，但不能保证原子性；而synchronized可以保证原子性， 也可以间接保证可见性，因为它会将私有内存和公共内存中的数据做同步。此知识点在后面 有实验做论证。
再次重申一下，关键字volatile解决的是变量在多个线程之间的可见性；而synchronized关键字解决的是多个线程之间访问资源的同步性。
线程安全包含原子性和可见性两个方面，Java的同步机制都是围绕这两个方面来确保线 程安全的。

线程安全包含原子性和可见性两个方面，Java的同步机制都是围绕这两个方面来确保线 程安全的。

关键字volatile主要使用的场合是在多个线程中可以感知实例变量 被更改了，并且可以获得最新的值使用，也就是用多线程读取共享变 量时可以获得最新值使用。
关键字volatile提示线程每次从共享内存中读取变量，而不是从私 有内存中读取，这样就保证了同步数据的可见性。但在这里需要注意 的是：如果修改实例变量中的数据，比如i++,也就是i=i+l,则这样 的操作其实并不是一个原子操作，也就是非线程安全的。表达式i++的操作步骤分解如下：
1)从内存中取出i的值；
2)计算i的值；
3)将i的值写到内存中。

除了在i++操作时使用synchronized关键字实现同步外，还可以使用Atomiclnteger原子类进行实现。
原子操作是不能分割的整体，没有其他线程能够中断或检查正在原子操作中的变量。— 个原子（atomic)类型就是一个原子操作可用的类型，它可以在没有锁的情况下做到线程安全 (thread-safe) 。

-->

&emsp; Volatile的特性：  
1. 不支持原子性。<font color = "red">它只对单个volatile变量的读/写具有原子性（只能保证对单次读/写的原子性）；</font><font color = "lime">但是对于类似i++这样的复合操作不能保证原子性。</font>  

        i++在虚拟机内部至少有3条指令（读取－修改－写入）执行。  
2. <font color = "red">实现了有序性，禁止进行指令重排序。</font>
<!-- 在volatile变量的赋值操作后⾯会有⼀个内存屏障（⽣成的汇编代码上），读操作不会被重排序到内存屏障之前。 -->
3. 实现了可见性。volatile提供happens-before的保证，使变量在多个线程间可见。变量被修改后，会立即保存在主存中，并清除工作内存中的值。这个变量不会在多个线程中存在复本，直接从内存读取。新值对其他线程来说是立即可见的。  

&emsp; **<font color = "red">总结：volatile保证了可见性和有序性，同时可以保证单次读/写的原子性。</font>**  

&emsp; **<font color = "red">synchronized和volatile比较：</font>**  

* 关键字volatile是线程同步的轻量级实现，volatile比synchronized执行成本更低，因为它不会引起线程上下文的切换和调度。  
* volatile本质是在告诉jvm当前变量在寄存器（工作内存）中的值是不确定的，需要从主存中读取；synchronized则是锁定当前变量，只有当前线程可以访问该变量，其他线程被阻塞住。  
* volatile不会造成线程的阻塞；synchronized可能会造成线程的阻塞。  
*  volatile仅能实现变量的修改可见性，不能保证原子性；而synchronized则可以保证变量的修改可见性和原子性。synchronized可以保证原子性，也可以间接保证可见性，因为它会将私有内存和公共内存中的数据做同步。  
*  volatile标记的变量不会被编译器优化；synchronized标记的变量可以被编译器优化。  
*  volatile仅能使用在变量级别；synchronized则可以使用在变量、方法、和类级别的。  

&emsp; 再次重申一下，关键字volatile解决的是变量在多个线程之间的可见性；而synchronized解决的是多个线程之间访问资源的同步性，synchronized可以使多个线程访问同一个资源具有同步性，而且它还具有将线程工作内存中的私有变量与公共内存中的变量同步的功能。  

&emsp; **volatile和atomic原子类区别：**  

* Volatile变量可以确保先行关系，即写操作会发生在后续的读操作之前, 但它并不能保证原子性。例如用volatile修饰i变量，那么i++ 操作就不是原子性的。  
* atomic原子类提供的atomic方法可以让这种操作具有原子性。如getAndIncrement()方法会原子性的进行增量操作把当前值加一，其它数据类型和引用变量也可以进行相似操作，但是atomic原子类一次只能操作一个共享变量，不能同时操作多个共享变量。  


## 1.1. Volatile原理  
<!-- 
~~
https://mp.weixin.qq.com/s/0_TDPDx8q2HmKCMyupWuNA
~~
-->
<!-- 
&emsp; volatile可以保证线程可见性且提供了一定的有序性，但是无法保证原子性。**<font color = "red">在JVM底层volatile是采用[内存屏障](/docs/java/concurrent/JMM.md)（也称内存栅栏）来实现的。</font>** 
--> 
&emsp; **<font color = "lime">一句话概述：在volatile写前插入写-写屏障，在volatile写后插入写-读屏障；在volatile读后插入读-读屏障、读-写屏障。</font>**

&emsp; 观察加入volatile关键字和没有加入volatile关键字时所生成的汇编代码发现，加入volatile关键字时，会多出一个lock前缀指令，lock前缀指令实际上相当于一个[内存屏障](/docs/java/concurrent/JMM.md)。

&emsp; **<font color = "lime">内存屏障的作用：</font>**  

1. **<font color = "lime">（保障有序性）阻⽌屏障两侧的指令重排序。</font>** 它确保指令重排序时不会把其后面的指令排到内存屏障之前的位置，也不会把前面的指令排到内存屏障的后面；即在执行到内存屏障这句指令时，在它前面的操作已经全部完成；  
2. **<font color = "lime">（保障可见性）它会强制将对缓存的修改操作立即写入主存；</font>** **<font color = "red">如果是写操作，会触发总线嗅探机制（MESI）,会导致其他CPU中对应的缓存行无效，会引发伪共享问题。</font>**  

&emsp; 有如下四种内存屏障：  

|屏障类型  |简称  |指令示例 |  说明|
|---|---|---|---|
|StoreStore Barriers |写-写 屏障 |Store1;StoreStore;Store2 |确保Store1数据对其他处理器可见（指刷新到内存）先于Store2及所有后续存储指令的存储。|
|StoreLoad Barriers |写-读 屏障 |Store1;StoreLoad;Load2 |确保Store1数据对其他处理器变得可见（指刷新到内存）先于Load2及所有后续装载指令的装载。<br/>StoreLoad Barriers会使屏障之前的所有内存访问指令（存储和装载指令）完成之后，才执行该屏障之后的内存访问指令。|
|LoadLoad Barriers|读-读 屏障 |Load1;LoadLoad;Load2 |(Load1代表加载数据，Store1表示刷新数据到内存)确保Load1数据的状态先于Load2及所有后续装载指令的装载。|
|LoadSotre Barriers |读-写 屏障 |Load1;LoadStore;Store2 |确保Load1数据装载先于Store2及所有后续的存储指令刷新到内存。|  
<!-- 
&emsp; StoreStore屏障可以保证在volatile写（flag赋值操作flag=true）之前，其前面的所有普通写（num的赋值操作num=1) 操作已经对任意处理器可见了，保障所有普通写在volatile写之前刷新到主内存。  
&emsp; LoadStore屏障可以保证其后面的所有普通写（num的赋值操作num=num+5) 操作必须在volatile读（if(flag)）之后执行。  
--> 

&emsp; **volatile写的场景如何插入内存屏障：**  

* **<font color = "red">在每个volatile写操作的前面插入一个StoreStore屏障（写-写 屏障）。禁止上面的普通写与下面的volatile写重排序。</font>**  
* **<font color = "red">在每个volatile写操作的后面插入一个StoreLoad屏障（写-读 屏障）。禁止上面的volatile写与下面可能有的volatile读/写重排序。</font>**  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-48.png)  

&emsp; **volatile读场景如何插入内存屏障：**  

* **<font color = "red">在每个volatile读操作的后面插入一个LoadLoad屏障（读-读 屏障）。禁止下面的普通读操作与上面的volatile读重排序。</font>**  
* **<font color = "red">在每个volatile读操作的后面插入一个LoadStore屏障（读-写 屏障）。禁止下面所有的普通写操作和上面volatile读重排序。</font>**  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-49.png)  

## 1.2. Volatile使用  
### 1.2.1. 如何正确使用volatile变量  
<!-- 
&emsp; **volatile的使用限制：**  
&emsp; 只能在有限的一些情形下使用volatile变量替代锁。要使volatile变量提供理想的线程安全，必须同时满足下面两个条件：  
1. 对变量的写操作不依赖于当前值。即变量不能有自增自减等操作，volatile不保证原子性。  
2. 该变量没有包含在具有其他变量的不变式中。  

&emsp; 实际上，这些条件表明，可以被写入volatile变量的这些有效值独立于任何程序的状态，包括变量的当前状态。  
&emsp; 第一个条件的限制使volatile变量不能用作线程安全计数器。虽然增量操作（x++）看上去类似一个单独操作，实际上它是一个由（读取－修改－写入）操作序列组成的组合操作，必须以原子方式执行，而volatile不能提供必须的原子特性。实现正确的操作需要使x 的值在操作期间保持不变，而volatile变量无法实现这点。（然而，如果只从单个线程写入，那么可以忽略第一个条件。）  
-->
&emsp; **<font color = "red">volatile的使用场景：</font>**  
&emsp; 关键字volatile用于多线程环境下的单次操作(单次读或者单次写)。即volatile主要使用的场合是在多个线程中可以感知实例变量被更改了，并且可以获得最新的值使用，也就是用多线程读取共享变量时可以获得最新值使用。  

### 1.2.2. 状态标志
&emsp; 也许实现volatile变量的规范使用仅仅是使用一个布尔状态标志，用于指示发生了一个重要的一次性事件，例如完成初始化或请求停机。  

```java
volatile boolean shutdownRequested;  
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
&emsp; 线程1执行doWork()的过程中，可能有另外的线程2调用了shutdown，所以boolean变量必须是volatile。  
&emsp; 而如果使用synchronized块编写循环要比使用volatile状态标志编写麻烦很多。由于volatile简化了编码，并且状态标志并不依赖于程序内任何其他状态，因此此处非常适合使用volatile。  
&emsp; 这种类型的状态标记的一个公共特性是：通常只有一种状态转换；shutdownRequested标志从false 转换为true，然后程序停止。这种模式可以扩展到来回转换的状态标志，但是只有在转换周期不被察觉的情况下才能扩展(从false到true，再转换到false)。此外，还需要某些原子状态转换机制，例如原子变量。  

### 1.2.3. 单例模式的实现  
<!-- 
https://www.cnblogs.com/jackson0714/p/java_volatile.html
-->
&emsp; 单例模式的实现，典型的双重检查锁定（DCL）  

```java
class VolatileSingleton {
    private static volatile VolatileSingleton instance = null;

    private VolatileSingleton() {
        System.out.println(Thread.currentThread().getName() + "\t 我是构造方法SingletonDemo");
    }
    public static VolatileSingleton getInstance() {
        // 第一重检测
        if(instance == null) {
            // 锁定代码块
            synchronized (VolatileSingleton.class) {
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
&emsp; 这是一种懒汉的单例模式，使用时才创建对象，而且为了避免初始化操作的指令重排序，给instance加上了volatile。

<!-- 
单例模式的双重锁为什么要加volatile
需要volatile关键字的原因是，在并发情况下，如果没有volatile关键字，在第5行会出现问题。instance = new TestInstance();可以分解为3行伪代码
a. memory = allocate() //分配内存
b. ctorInstanc(memory) //初始化对象
c. instance = memory //设置instance指向刚分配的地址
上面的代码在编译运行时，可能会出现重排序从a-b-c排序为a-c-b。在多线程的情况下会出现以下问题。当线程A在执行第5行代码时，B线程进来执行到第2行代码。假设此时A执行的过程中发生了指令重排序，即先执行了a和c，没有执行b。那么由于A线程执行了c导致instance指向了一段地址，所以B线程判断instance不为null，会直接跳到第6行并返回一个未初始化的对象。
-->

## 1.3. 推荐使用LongAdder  
<!-- 
阿里为什么推荐使用LongAdder，而不是volatile？ 
https://mp.weixin.qq.com/s/lpk5l4m0oFpPDDf6fl8mmQ
-->
&emsp; 阿里《Java开发手册》嵩山版：    
&emsp; 【参考】volatile解决多线程内存不可见问题。对于一写多读，是可以解决变量同步问题，但是如果多写，同样无法解决线程安全问题。  
&emsp; 说明：如果是count++ 操作，使用如下类实现：AtomicInteger count = new AtomicInteger(); count.addAndGet(1); 如果是JDK8，推荐使用 LongAdder对象，比AtomicLong性能更好（减少乐观锁的重试次数）。  

&emsp; AtomicInteger在高并发环境下会有多个线程去竞争一个原子变量，而始终只有一个线程能竞争成功，而其他线程会一直通过CAS自旋尝试获取此原子变量，因此会有一定的性能消耗；<font color = "lime">而LongAdder会将这个原子变量分离成一个Cell数组，每个线程通过Hash获取到自己数组，这样就减少了乐观锁的重试次数，从而在高竞争下获得优势；而在低竞争下表现的又不是很好，可能是因为自己本身机制的执行时间大于了锁竞争的自旋时间，因此在低竞争下表现性能不如AtomicInteger。</font>  




