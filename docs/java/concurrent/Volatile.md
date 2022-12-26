
<!-- TOC -->

- [1. Volatile](#1-volatile)
    - [1.1. Volatile的特性](#11-volatile的特性)
    - [1.2. Volatile原理](#12-volatile原理)
    - [1.3. Volatile为什么不安全？](#13-volatile为什么不安全)
    - [1.4. Volatile使用](#14-volatile使用)
        - [1.4.1. 如何正确使用Volatile变量](#141-如何正确使用volatile变量)
        - [1.4.2. 全局状态标志](#142-全局状态标志)
        - [1.4.3. ★★★单例模式的实现](#143-★★★单例模式的实现)
            - [1.4.3.1. 为什么两次判断？](#1431-为什么两次判断)
            - [1.4.3.2. 为什么要加volatile关键字？](#1432-为什么要加volatile关键字)
    - [1.5. Volatile、AtomicInteger与LongAdder](#15-volatileatomicinteger与longadder)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "clime">Volatile的特性：</font>**  
    1. 不支持原子性。<font color = "red">它只对Volatile变量的单次读/写具有原子性；</font><font color = "clime">但是对于类似i++这样的复合操作不能保证原子性。</font>    
    **<font color = "clime">Volatile为什么不安全（不保证原子性，线程切换）？</font>**    
    两个线程执行i++（i++的过程可以分为三步，首先获取i的值，其次对i的值进行加1，最后将得到的新值写回到缓存中），线程1获取i值后被挂起，线程2执行...  
    2. 实现了可见性。 **Volatile提供happens-before的保证，使变量在多个线程间可见。**  
    3. <font color = "red">实现了有序性，禁止进行指令重排序。</font>  
2. Volatile底层原理：  
    * **<font color = "clime">在Volatile写前插入写-写[屏障](/docs/java/concurrent/ConcurrencySolve.md)（禁止上面的普通写与下面的Volatile写重排序），在Volatile写后插入写-读屏障（禁止上面的Volatile写与下面可能有的Volatile读/写重排序）。</font>**  
    * **<font color = "clime">在Volatile读后插入读-读屏障（禁止下面的普通读操作与上面的Volatile读重排序）、读-写屏障（禁止下面所有的普通写操作和上面Volatile读重排序）。</font>**  
3. volatile使用场景：
    **<font color = "red">Volatile的使用场景：</font>** 关键字Volatile用于多线程环境下的单次操作（单次读或者单次写）。即Volatile主要使用的场合是在多个线程中可以感知实例变量被更改了，并且可以获得最新的值使用，也就是用多线程读取共享变量时可以获得最新值使用。  
    1. 全局状态标志。
    2. DCL详解：  
        1. 为什么两次判断？ 线程1调用第一个if(singleton==null)，可能会被挂起。  
        2. 为什么要加volatile关键字？  
        &emsp; singleton = new Singleton()非原子性操作，包含3个步骤：分配内存 ---> 初始化对象 ---> 将singleton对象指向分配的内存空间(这步一旦执行了，那singleton对象就不等于null了)。  
        &emsp; **<font color = "clime">因为指令重排序，可能编程1->3->2。如果是这种顺序，会导致别的线程拿到半成品的实例。</font>**  


# 1. Volatile  
<!--
为什么volatile不能保证原子性而Atomic可以？
https://mp.weixin.qq.com/s/RuhajO-Vj5U6cBGOxMCX7Q

https://mp.weixin.qq.com/s/WTqdSz-lc5zzelJgk4Co8g
volatile 的设计原理
https://mp.weixin.qq.com/s/MyhBFZQdMvH4m1nSbQUQWA

99%的人没弄懂volatile的设计原理
https://mp.weixin.qq.com/s/WTqdSz-lc5zzelJgk4Co8g
-->
<!-- 
~~
借鉴
https://mp.weixin.qq.com/s/0_TDPDx8q2HmKCMyupWuNA
-->
## 1.1. Volatile的特性
&emsp; Volatile的特性：  
1. 不支持原子性。<font color = "red">它只对Volatile变量的单次读/写具有原子性；</font><font color = "clime">但是对于类似i++这样的复合操作不能保证原子性。</font>  

        i++在虚拟机内部有3条指令(读取－修改－写入)执行。表达式i++的操作步骤分解如下：1)从内存中取出i的值；2)计算i的值；3)将i的值写到内存中。    

2. 实现了可见性。 **Volatile提供happens-before的保证，使变量在多个线程间可见。**变量被修改后，会立即保存在主存中，并清除工作内存中的值。这个变量不会在多个线程中存在复本，直接从内存读取。新值对其他线程来说是立即可见的。  
<!-- 在Volatile变量的赋值操作后⾯会有⼀个内存屏障(⽣成的汇编代码上)，读操作不会被重排序到内存屏障之前。 -->
3. <font color = "red">实现了有序性，禁止进行指令重排序。</font>

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
&emsp; 观察加入Volatile关键字和没有加入Volatile关键字时所生成的汇编代码发现，加入Volatile关键字时，会多出一个lock前缀指令，lock前缀指令实际上相当于一个[内存屏障](/docs/java/concurrent/ConcurrencySolve.md)。

<!-- 
&emsp; StoreStore屏障可以保证在Volatile写(flag赋值操作flag=true)之前，其前面的所有普通写(num的赋值操作num=1) 操作已经对任意处理器可见了，保障所有普通写在Volatile写之前刷新到主内存。  
&emsp; LoadStore屏障可以保证其后面的所有普通写(num的赋值操作num=num+5) 操作必须在Volatile读(if(flag))之后执行。  
--> 

&emsp; **Volatile写的场景如何插入内存屏障：**  

* **<font color = "red">在每个Volatile写操作的前面插入一个StoreStore屏障(写-写 屏障)。禁止上面的普通写与下面的Volatile写重排序。</font>**  
* **<font color = "red">在每个Volatile写操作的后面插入一个StoreLoad屏障(写-读 屏障)。禁止上面的Volatile写与下面可能有的Volatile读/写重排序。</font>**  

![image](http://182.92.69.8:8081/img/java/concurrent/multi-48.png)  

&emsp; **Volatile读场景如何插入内存屏障：**  

* **<font color = "red">在每个Volatile读操作的后面插入一个LoadLoad屏障(读-读 屏障)。禁止下面的普通读操作与上面的Volatile读重排序。</font>**  
* **<font color = "red">在每个Volatile读操作的后面插入一个LoadStore屏障(读-写 屏障)。禁止下面所有的普通写操作和上面Volatile读重排序。</font>**  

![image](http://182.92.69.8:8081/img/java/concurrent/multi-49.png)  


## 1.3. Volatile为什么不安全？  
&emsp; Volatile可以保证修改的值立即能更新到主存，其他线程也会捕捉到被修改后的值，那么为什么不能保证原子性呢？---线程切换
&emsp; 首先需要了解的是，Java中只有对基本类型变量的赋值和读取是原子操作，如i = 1的赋值操作，但是像j = i或者i++这样的操作都不是原子操作，因为它们都进行了多次原子操作，比如先读取i的值，再将i的值赋值给j，两个原子操作加起来就不是原子操作了。  
&emsp; 所以，如果一个变量被volatile修饰了，那么肯定可以保证每次读取这个变量值的时候得到的值是最新的，但是一旦需要对变量进行自增这样的非原子操作，就不会保证这个变量的原子性了。  
&emsp; 例：  
&emsp; 一个变量i被volatile修饰，两个线程想对这个变量修改，都对其进行自增操作也就是i++， **<font color = "red">i++的过程可以分为三步，首先获取i的值，其次对i的值进行加1，最后将得到的新值写回到缓存中。</font>**    
&emsp; 线程A首先得到了i的初始值100，但是还没来得及修改，就阻塞了，这时线程B开始了，它也得到了i的值，由于i的值未被修改，即使是被volatile修饰，主存的变量还没变化，那么线程B得到的值也是100，之后对其进行加1操作，得到101后，将新值写入到缓存中，再刷入主存中。根据可见性的原则，这个主存的值可以被其他线程可见。  
&emsp; 问题来了，线程A已经读取到了i的值为100，也就是说读取的这个原子操作已经结束了，所以这个可见性来的有点晚，线程A阻塞结束后，继续将100这个值加1，得到101，再将值写到缓存，最后刷入主存，所以即便是volatile具有可见性，也不能保证对它修饰的变量具有原子性。  



## 1.4. Volatile使用  
### 1.4.1. 如何正确使用Volatile变量  
<!-- 
关键字Volatile主要使用的场合是在多个线程中可以感知实例变量 被更改了，并且可以获得最新的值使用，也就是用多线程读取共享变 量时可以获得最新值使用。
-->
&emsp; **<font color = "red">Volatile的使用场景：</font>** 关键字Volatile用于多线程环境下的单次操作（单次读或者单次写）。即Volatile主要使用的场合是在多个线程中可以感知实例变量被更改了，并且可以获得最新的值使用，也就是用多线程读取共享变量时可以获得最新值使用。  

### 1.4.2. 全局状态标志
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
&emsp; 这种类型的状态标记的一个公共特性是： **<font color = "clime">通常只有一种状态转换；</font>** shutdownRequested标志从false 转换为true，然后程序停止。这种模式可以扩展到来回转换的状态标志，但是只有在转换周期不被察觉的情况下才能扩展(从false到true，再转换到false)。此外，还需要某些原子状态转换机制，例如原子变量。  

### 1.4.3. ★★★单例模式的实现  
<!-- 

DCL为什么要用volatile关键字？
https://blog.csdn.net/weixin_38106322/article/details/105753241
说说双重检查加锁单例模式为什么两次判断？
https://mp.weixin.qq.com/s/OG2_Iq0Xe1DD_ghtMYX2Ow
-->

<!-- 
单例模式的双重锁为什么要加Volatile
需要Volatile关键字的原因是，在并发情况下，如果没有Volatile关键字，在第5行会出现问题。instance = new TestInstance();可以分解为3行伪代码
a. memory = allocate() //分配内存
b. ctorInstanc(memory) //初始化对象
c. instance = memory //设置instance指向刚分配的地址
上面的代码在编译运行时，可能会出现重排序从a-b-c排序为a-c-b。在多线程的情况下会出现以下问题。当线程A在执行第5行代码时，B线程进来执行到第2行代码。假设此时A执行的过程中发生了指令重排序，即先执行了a和c，没有执行b。那么由于A线程执行了c导致instance指向了一段地址，所以B线程判断instance不为null，会直接跳到第6行并返回一个未初始化的对象。
-->

&emsp; 单例模式的实现，典型的双重检查锁定(DCL)  

```java
public class Singleton {

    //Singleton对象属性,加上volatile关键字是为了防止指定重排序,要知道singleton = new Singleton()拆分成cpu指令的话，有足足3个步骤
    private volatile static Singleton singleton;

    //对外提供的获取实例的方法
    public static Singleton getInstance() {
        // 第一重检测
        if (singleton == null) {
            // 锁定代码块
            synchronized (Singleton.class) {
                // 第二重检测
                if (singleton == null) {
                    // 实例化对象
                    singleton = new Singleton();
                    //1.分配内存给这个对象
                    //2.初始化对象
                    //3.设置 lazy 指向刚分配的内存地址
                }
            }
        }
        return singleton;
    }
}

```
&emsp; 这是一种懒汉的单例模式，使用时才创建对象，而且为了避免初始化操作的指令重排序，给instance加上了Volatile。

#### 1.4.3.1. 为什么两次判断？  
&emsp; 第一次校验：也就是第一个if（singleton==null），这个是为了代码提高代码执行效率，由于单例模式只要一次创建实例即可，所以当创建了一个实例之后，再次调用getInstance方法就不必要进入同步代码块，不用竞争锁。直接返回前面创建的实例即可。  
&emsp; 第二次校验：也就是第二个if（singleton==null），这个校验是防止二次创建实例，假如有一种情况，当singleton还未被创建时，线程t1调用getInstance方法，由于第一次判断singleton==null，此时线程t1准备继续执行，但是由于资源被线程t2抢占了，此时t2页调用getInstance方法。  
&emsp; 同样的，由于singleton并没有实例化，t2同样可以通过第一个if，然后继续往下执行，同步代码块，第二个if也通过，然后t2线程创建了一个实例singleton。  
&emsp; 此时t2线程完成任务，资源又回到t1线程，t1此时也进入同步代码块，如果没有这个第二个if，那么，t1就也会创建一个singleton实例，那么，就会出现创建多个实例的情况，但是加上第二个if，就可以完全避免这个多线程导致多次创建实例的问题。  
&emsp; 所以说：两次校验都必不可少。  

#### 1.4.3.2. 为什么要加volatile关键字？  
&emsp; 了解下singleton = new Singleton()这段代码其实不是原子性的操作，它至少分为以下3个步骤：  

1. 给singleton对象分配内存空间。  
2. 调用Singleton类的构造函数等，初始化singleton对象。  
3. 将singleton对象指向分配的内存空间，这步一旦执行了，那singleton对象就不等于null了。  

&emsp; 这里还需要知道一点，就是有时候JVM会为了优化，而做指令重排序的操作，这里的指令，指的是CPU层面的。  
&emsp; 正常情况下，singleton = new Singleton()的步骤是按照1->2->3这种步骤进行的，但是一旦JVM做了指令重排序，那么顺序很可能编程1->3->2，如果是这种顺序，可以发现，在3步骤执行完singleton对象就不等于null，但是它其实还没做步骤二的初始化工作，但是另一个线程进来时发现，singleton不等于null了，就这样把半成品的实例返回去，调用是会报错的。  

## 1.5. Volatile、AtomicInteger与LongAdder  
&emsp; &emsp; [LongAdder](/docs/java/concurrent/LongAdder.md)  
