
<!-- TOC -->

- [1. ~~并发安全解决底层~~](#1-并发安全解决底层)
    - [1.1. ~~CPU的缓存一致性~~](#11-cpu的缓存一致性)
        - [1.1.1. 缓存失效/缓存不一致/缓存可见性](#111-缓存失效缓存不一致缓存可见性)
            - [1.1.1.1. 总线锁](#1111-总线锁)
            - [1.1.1.2. 缓存锁](#1112-缓存锁)
        - [1.1.2. ~~MESI缓存一致性协议~~](#112-mesi缓存一致性协议)
            - [1.1.2.1. 总线嗅探](#1121-总线嗅探)
            - [1.1.2.2. 总线风暴](#1122-总线风暴)
    - [1.2. 内存屏障，禁止处理器重排序](#12-内存屏障禁止处理器重排序)
    - [1.3. JMM中的happens-before原则](#13-jmm中的happens-before原则)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. 缓存一致性协议  
    1. 怎么解决缓存一致性问题呢？使用总线锁或缓存锁。  
    &emsp; 缓存锁：只要保证多个CPU缓存的同一份数据是一致的就可以了，基于缓存一致性协议来实现。  
    2. MESI缓存一致性协议  
        1. 缓存一致性协议有很多种，MESI(Modified-Exclusive-Shared-Invalid)协议其实是目前使用很广泛的缓存一致性协议，x86处理器所使用的缓存一致性协议就是基于MESI的。  
        2. 其他cpu通过 总线嗅探机制 可以感知到数据的变化从而将自己缓存里的数据失效。总线嗅探，**<font color = "red">每个CPU不断嗅探总线上传播的数据来检查自己缓存值是否过期了，如果处理器发现自己的缓存行对应的内存地址被修改，就会将当前处理器的缓存行设置为无效状态，当处理器对这个数据进行修改操作的时候，会重新从内存中把数据读取到处理器缓存中。</font>**    
        2. 总线嗅探会带来总线风暴。  
2. 内存屏障  
&emsp; Java中如何保证底层操作的有序性和可见性？可以通过内存屏障。  
&emsp; 内存屏障，禁止处理器重排序，保障缓存一致性。
    1. 内存屏障的作用：（~~原子性~~、可见性、有序性）
        1.（`保障可见性`）它会强制将对缓存的修改操作立即写入主存； 如果是写操作，会触发总线嗅探机制(MESI)，会导致其他CPU中对应的缓存行无效，也有 [伪共享问题](/docs/java/concurrent/PseudoSharing.md)。 
        2.（`保障有序性`）阻止屏障两侧的指令重排序。 
3. JMM中的happens-before原则：JSR-133内存模型 **<font color = "red">使用happens-before的概念来阐述操作之间的内存可见性。在JMM中，如果一个操作执行的结果需要对另一个操作可见，那么这两个操作之间必须要存在happens-before关系。</font>** 这里提到的两个操作既可以是在一个线程之内，也可以是在不同线程之间。  
&emsp; happens-before原则有管理锁定（lock）规则、volatile变量规则（参考volatile原理，即内存屏障）、线程启动规则（Thread.start()）、线程终止规则（Thread.join()）、线程中断规则（Thread.interrupt()）...  

# 1. ~~并发安全解决底层~~
<!-- 

https://www.cnblogs.com/Courage129/p/14401680.html
https://zhuanlan.zhihu.com/p/260081868
https://mp.weixin.qq.com/s?__biz=MzI0MjE4NTM5Mg==&mid=2648975640&idx=1&sn=a8e85ae9ae8d17013490cf09ff92c54e&chksm=f110acc7c66725d19ca1c743fb721434e48ee028804a808493e959aa4196625e246ae772a965&scene=21#wechat_redirect
http://www.360doc.cn/mip/760841759.html
https://blog.csdn.net/breakout_alex/article/details/94379895
-->
<!-- 
https://www.cnblogs.com/xiaoxiongcanguan/p/13184801.html
https://www.pianshen.com/article/86211687442/
https://www.jianshu.com/p/06717ac8312c
https://www.freesion.com/article/73021012217/
https://blog.csdn.net/weixin_30808575/article/details/95331072
https://www.cnblogs.com/wewill/p/8098189.html

https://www.cnblogs.com/jackson0714/p/java_Volatile.html
-->

## 1.1. ~~CPU的缓存一致性~~
&emsp; 当多个CPU持有的缓存都来自同一个主内存的拷贝，当有某个CPU修改了这个主内存数据后，而其他CPU并不知道，那拷贝的内存将会和主内存不一致，这就是缓存不一致。那如何来保证缓存一致呢？这里就需要操作系统来共同制定一个同步规则来保证。 

### 1.1.1. 缓存失效/缓存不一致/缓存可见性  
&emsp; 当CPU写数据时，如果发现操作的变量是共享变量，即在其它CPU中也存在该变量的副本，系统会发出信号通知其它CPU将该内存变量的缓存行设置为无效。如下图所示，CPU1和CPU3 中num=1已经失效了。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-44.png)  
&emsp; 当其它CPU读取这个变量的时，发现自己缓存该变量的缓存行是无效的，那么它就会从内存中重新读取。  

<!-- 
&emsp; 如下图所示，CPU1和CPU3发现缓存的num值失效了，就重新从内存读取，num值更新为2。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-45.png)  
-->

&emsp; **<font color = "clime">怎么解决缓存一致性问题呢？使用总线锁或缓存锁。</font>**  

#### 1.1.1.1. 总线锁  
&emsp; 早期，cpu从主内存读取数据到高速缓存，会在总线对这个数据加锁，这样其他cpu无法去读或写这个数据，直到这个cpu使用完数据释放锁之后其他cpu才能读取该数据。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-46.png)  

#### 1.1.1.2. 缓存锁  
&emsp; 缓存锁：只要保证多个CPU缓存的同一份数据是一致的就可以了，基于缓存一致性协议来实现。  


### 1.1.2. ~~MESI缓存一致性协议~~  
<!-- 
~~
https://mp.weixin.qq.com/s/yWifJmirZNnBrAIZrpJwyg
看懂这篇，才能说了解并发底层技术！ 
https://mp.weixin.qq.com/s/SZl2E5NAhpYM4kKv9gyQOQ
-->
&emsp; 缓存一致性协议有很多种，MESI(Modified-Exclusive-Shared-Invalid)协议其实是目前使用很广泛的缓存一致性协议，x86处理器所使用的缓存一致性协议就是基于MESI的。  

&emsp; **<font color = "clime">多个cpu从主内存读取同一个数据到各自的高速缓存，当其中某个cpu修改了缓存里的数据，该数据会马上同步回主内存，其他cpu通过</font>** **<font color = "clime">总线嗅探机制</font>** **<font color = "red">可以感知到数据的变化从而将自己缓存里的数据失效。</font>**  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-47.png)  

#### 1.1.2.1. 总线嗅探  
&emsp; **<font color = "red">每个CPU不断嗅探总线上传播的数据来检查自己缓存值是否过期了，如果处理器发现自己的缓存行对应的内存地址被修改，就会将当前处理器的缓存行设置为无效状态，当处理器对这个数据进行修改操作的时候，会重新从内存中把数据读取到处理器缓存中。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-46.png)  

#### 1.1.2.2. 总线风暴
&emsp; 总线嗅探技术有哪些缺点？  
&emsp; 由于MESI缓存一致性协议，需要不断对主线进行内存嗅探，大量的交互会导致总线带宽达到峰值。   
&emsp; 因此不要滥用volatile，可以用锁来替代，看使用场景。  


----

## 1.2. 内存屏障，禁止处理器重排序  
&emsp; **<font color = "red">Java中如何保证底层操作的有序性和可见性？可以通过内存屏障。</font>**  

&emsp; 什么是内存屏障？硬件层⾯，<font color = "red">内存屏障分两种：读屏障(Load Barrier)和写屏障(Store Barrier)。</font>  

&emsp; **<font color = "clime">内存屏障的作用：(~~原子性~~、可见性、有序性)</font>**  

* **<font color = "clime">(保障可见性)它会强制将对缓存的修改操作立即写入主存；</font>** **<font color = "red">如果是写操作，会触发总线嗅探机制(MESI)，会导致其他CPU中对应的缓存行无效，</font>** **<font color = "clime">也有[伪共享问题](/docs/java/concurrent/PseudoSharing.md)。</font>**  
* **<font color = "clime">(保障有序性)阻⽌屏障两侧的指令重排序。</font>** 它确保指令重排序时不会把其后面的指令排到内存屏障之前的位置，也不会把前面的指令排到内存屏障的后面；即在执行到内存屏障这句指令时，在它前面的操作已经全部完成；  


<!-- 
内存屏障有两个作⽤：  
1. 阻⽌屏障两侧的指令重排序；  
2. 强制把写缓冲区/⾼速缓存中的脏数据等写回主内存，或者让缓存中相应的数据失效。 
&emsp; **<font color= "red">内存屏障是被插入两个CPU指令之间的一种指令，用来禁止处理器指令发生重排序，从而保障有序性的。另外，为了达到屏障的效果，它也会使处理器写入、读取值之前，将主内存的值写入高速缓存，清空无效队列，从而保障可见性。</font>**  
-->
&emsp; <font color = "red">Java中有4种屏障：(load载入，store存储)</font>  

|屏障类型 |简称 |指令示例|说明|
|---|---|---|---|
|StoreStore Barriers |写-写 屏障|Store1;StoreStore;Store2 |确保Store1数据对其他处理器可见(指刷新到内存)先于Store2及所有后续存储指令的存储。|
|StoreLoad Barriers |写-读 屏障 |Store1;StoreLoad;Load2 |确保Store1数据对其他处理器变得可见(指刷新到内存)先于Load2及所有后续装载指令的装载。<br/>StoreLoad Barriers会使屏障之前的所有内存访问指令(存储和装载指令)完成之后，才执行该屏障之后的内存访问指令。|
|LoadLoad Barriers|读-读 屏障 |Load1;LoadLoad;Load2 |(Load1代表加载数据，Store1表示刷新数据到内存)确保Load1数据的状态先于Load2及所有后续装载指令的装载。|
|LoadSotre Barriers|读-写 屏障|Load1;LoadStore;Store2|确保Load1数据装载先于Store2及所有后续的存储指令刷新到内存。| 

<!-- 
* LoadLoad(LL)屏障：对于这样的语句 Load1; LoadLoad; Load2，<font color = "red">在Load2及后续读取操作要读取的数据被访问前，保证Load1要读取的数据被读取完毕。</font>  
* StoreStore(SS)屏障：对于这样的语句 Store1; StoreStore; Store2，在Store2及后续写入操作执行前，保证Store1的写入操作对其它处理器可见。  
* LoadStore(LS)屏障：对于这样的语句Load1; LoadStore; Store2，在Store2及后续写入操作被执行前，保证Load1要读取的数据被读取完毕。  
* StoreLoad (SL)屏障：对于这样的语句Store1; StoreLoad; Load2，在Load2及后续所有读取操作执行前，保证Store1的写入对所有处理器可见。它的开销是四种屏障中最大的(冲刷写缓冲器，清空无效化队列)。在大多数处理器的实现中，这个屏障也被称为全能屏障，兼具其它三种内存屏障的功能。  
-->

&emsp; **Java中对内存屏障的使用，常见的有volatile关键字修饰的代码块，还可以通过Unsafe这个类来使用内存屏障。**  

&emsp; 例如：

    Store1;
    Store2;
    Load1;
    StoreLoad;  //内存屏障
    Store3;
    Load2;
    Load3;

&emsp; 对于上面的一组CPU指令(Store表示写入指令，Load表示读取指令)，StoreLoad 屏障之前的Store指令无法与StoreLoad 屏障之后的Load指令进行交换位置，即重排序。但是StoreLoad屏障之前和之后的指令是可以互换位置的，即Store1可以和Store2互换，Load2可以和Load3互换。  


## 1.3. JMM中的happens-before原则
&emsp; JSR-133内存模型 **<font color = "red">使用happens-before的概念来阐述操作之间的内存可见性。在JMM中，如果一个操作执行的结果需要对另一个操作可见，那么这两个操作之间必须要存在happens-before关系。</font>** 这里提到的两个操作既可以是在一个线程之内，也可以是在不同线程之间。  
<!-- 
&emsp; Happens-before规则主要用来约束两个操作，两个操作之间具有 happens-before关系, 并不意味着前一个操作必须要在后一个操作之前执行，happens-before 仅仅要求前一个操作(执行的结果)对后一个操作可见, (the first is visible to and ordered before the second，前一个操作的结果可以被后续的操作获取)。  
-->
&emsp; happens-before关系的分析需要分为单线程和多线程的情况：  

* 单线程下的 happens-before 字节码的先后顺序天然包含happens-before关系：因为单线程内共享一份工作内存，不存在数据一致性的问题。在程序控制流路径中靠前的字节码 happens-before 靠后的字节码，即靠前的字节码执行完之后操作结果对靠后的字节码可见。然而，这并不意味着前者一定在后者之前执行。实际上，如果后者不依赖前者的运行结果，那么它们可能会被重排序。  
* 多线程下的 happens-before 多线程由于每个线程有共享变量的副本，如果没有对共享变量做同步处理，线程1更新执行操作A共享变量的值之后，线程2开始执行操作B，此时操作A产生的结果对操作B不一定可见。  

&emsp; 为了方便程序开发，Java 内存模型实现了下述的先行发生关系：  

* 程序次序规则： 在一个单独的线程中，按照程序代码的执行流顺序，(时间上)先执行的操作happen—before(时间上)后执行的操作。  
（同一个线程中前面的所有写操作对后面的操作可见）
* 管理锁定规则：一个unlock操作happen—before后面(时间上的先后顺序)对同一个锁的lock操作。  
（如果线程1解锁了monitor a，接着线程2锁定了a，那么，线程1解锁a之前的写操作都对线程2可见(线程1和线程2可以是同一个线程)）   
* volatile变量规则：对一个volatile变量的写操作happen—before后面(时间上)对该变量的读操作。  
（如果线程1写入了volatile变量v(临界资源)，接着线程2读取了v，那么，线程1写入v及之前的写操作都对线程2可见(线程1和线程2可以是同一个线程)） 

---- 
* <font color = "red">线程启动规则：</font>Thread.start()方法happen—before调用start的线程前的每一个操作。  
(假定线程A在执行过程中，通过执行ThreadB.start()来启动线程B，那么线程A对共享变量的修改在接下来线程B开始执行前对线程B可见。注意：线程B启动之后，线程A在对变量修改线程B未必可见。)  
* <font color = "red">线程终止规则：</font>线程的所有操作都happen—before对此线程的终止检测，可以通过Thread.join()方法结束、Thread.isAlive()的返回值等手段检测到线程已经终止执行。  
(线程t1写入的所有变量，在任意其它线程t2调用t1.join()，或者t1.isAlive() 成功返回后，都对t2可见。)  
* <font color = "red">线程中断规则：</font>对线程interrupt()的调用 happen—before 发生于被中断线程的代码检测到中断时事件的发生。  
（线程t1写入的所有变量，调用Thread.interrupt()，被打断的线程t2，可以看到t1的全部操作）  

---
* 对象终结规则：一个对象的初始化完成（构造函数执行结束）happen—before它的finalize()方法的开始。  
（对象调用finalize()方法时，对象初始化完成的任意操作，同步到全部主存同步到全部cache。）  
* 传递性：如果操作A happen—before操作B，操作B happen—before操作C，那么可以得出A happen—before操作C。  
（A h-b B， B h-b C 那么可以得到 A h-b C）  

&emsp; **<font color = "red">as-if-serial规则和happens-before规则的区别：</font>**  

* as-if-serial语义保证单线程内程序的执行结果不被改变，happens-before关系保证正确同步的多线程程序的执行结果不被改变。  
* as-if-serial语义给编写单线程程序的程序员创造了一个幻境：单线程程序是按程序的顺序来执行的。happens-before关系给编写正确同步的多线程程序的程序员创造了一个幻境：正确同步的多线程程序是按happens-before指定的顺序来执行的。  
* as-if-serial语义和happens-before这么做的目的，都是为了在不改变程序执行结果的前提下，尽可能地提高程序执行的并行度。  

<!-- 
与as-if-serial 比较
as-if-serial语义：保证 单线程 内执行的结果不被改变；

happens-before：保证正确同步的 多线程 程序的执行结果不被改变

as-if-serial：创建的环境，单线程程序是顺序执行的

happens-before：正确同步的 多线程 程序是按happens-before指定的顺序来执行

二者都是为了在不改变执行结果的前提下，提高程序的并行度。
-->
