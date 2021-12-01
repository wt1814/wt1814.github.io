
<!-- TOC -->

- [1. Java解决并发安全](#1-java解决并发安全)
    - [1.1. JMM](#11-jmm)
        - [1.1.1. JMM中内存划分(主内存，工作内存和线程三者的交互关系)](#111-jmm中内存划分主内存工作内存和线程三者的交互关系)
        - [1.1.2. JMM内存间的交互操作](#112-jmm内存间的交互操作)
        - [1.1.3. 线程之间的通信和同步](#113-线程之间的通信和同步)
            - [1.1.3.1. ★★★线程之间的通信过程](#1131-★★★线程之间的通信过程)
    - [1.2. 内存屏障，禁止处理器重排序 / 【有序性】](#12-内存屏障禁止处理器重排序--有序性)
        - [1.2.1. JVM中的内存屏障](#121-jvm中的内存屏障)
    - [1.3. JMM中的happens-before原则](#13-jmm中的happens-before原则)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. JMM
    1. JMM内存划分：线程对变量的所有操作都必须在工作内存进行，而不能直接读写主内存中的变量。    
    2. 单个线程操作时，8种内存间交换操作指令。  
    3. 线程之间的通信和同步。线程之间的通信过程：线程对变量的操作（读取赋值等）必须在工作内存中进行，首先要将变量从主内存拷贝到自己的工作内存空间，然后对变量进行操作，操作完成后再将变量写回主内存，不能直接操作主内存中的变量，</font>各个线程中的工作内存中存储着主内存中的变量副本拷贝，<font color = "red">因此不同的线程间无法访问对方的工作内存，线程间的通信（传值）必须通过主内存来完成。</font>    
2. 内存屏障，禁止处理器重排序 / 【有序性】  
    &emsp; Java中如何保证底层操作的有序性和可见性？可以通过内存屏障。   
    &emsp; 内存屏障，禁止处理器重排序，保障缓存一致性。  
    1. 内存屏障的作用：（~~原子性~~、可见性、有序性）  
        1. （`保障可见性`）它会强制将对缓存的修改操作立即写入主存； 如果是写操作，会触发总线嗅探机制(MESI)，会导致其他CPU中对应的缓存行无效，也有 [伪共享问题](/docs/java/concurrent/PseudoSharing.md)。  
        2. （`保障有序性`）阻止屏障两侧的指令重排序。 
3. JMM中的happens-before原则：  
    &emsp; JSR-133内存模型 **<font color = "red">使用`happens-before`的概念来阐述操作之间的`内存可见性`。在JMM中，如果一个操作执行的结果需要对另一个操作可见，那么这两个操作之间必须要存在happens-before关系。</font>** 这里提到的两个操作既可以是在一个线程之内，也可以是在不同线程之间。  
    * 如果操作1 happens-before 操作2，那么第操作1的执行结果将对操作2可见，而且操作1的执行顺序排在第操作2之前。
    * 两个操作之间存在happens-before关系，并不意味着一定要按照happens-before原则制定的顺序来执行。如果重排序之后的执行结果与按照happens-before关系来执行的结果一致，那么这种重排序并不非法。  

    &emsp; happens-before原则有管理锁定（lock）规则、volatile变量规则（参考volatile原理，即内存屏障）、线程启动规则（Thread.start()）、线程终止规则（Thread.join()）、线程中断规则（Thread.interrupt()）...  
    


# 1. Java解决并发安全

<!-- 
java内存屏障
https://blog.csdn.net/breakout_alex/article/details/94379895
Happens-before 原则 
https://mp.weixin.qq.com/s/H346rAdeyIhqM-hncR1Izw

-->

<!-- 
~~

https://zhuanlan.zhihu.com/p/260081868
volatile!
https://www.cnblogs.com/jackson0714/p/java_Volatile.html
-->


## 1.1. JMM  
<!-- 
&emsp; Java内存模型(Java Memory Model，JMM)是一种符合顺序一致内存模型规范的，屏蔽了各种硬件和操作系统的访问差异的，保证了Java程序在各种平台下对内存的访问都能保证效果一致的机制及规范。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-6.png)  
-->
&emsp; JMM是指Java内存模型(Java Memory Model)，本身是一种抽象的概念，实际上并不存在，它描述的是一组规则或规范，通过这组规范定义了程序中各个变量(包括实例字段，静态字段和构成数组对象的元素)的访问方式。  

1. 定义程序中各种变量的访问规则
2. 把变量值存储到内存的底层细节
3. 从内存中取出变量值的底层细节

### 1.1.1. JMM中内存划分(主内存，工作内存和线程三者的交互关系)
&emsp; Java线程内存模型跟cpu缓存模型类似，是基于cpu缓存模型来建立的，Java线程内存模型是标准化的，屏蔽掉了底层不同计算机的区别。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-7.png)   
&emsp; Java内存模型划分： 

* 主内存：Java内存模型规定了所有变量都存储在主内存(Main Memory)中。此处的主内存与物理硬件的主内存RAM名字一样，两者可以互相类比，但此处仅是虚拟机内存的一部分。  
    * Java堆中对象实例数据部分
    * 对应于物理硬件的内存
* 工作内存：每条线程都有自己的工作内存(Working Memory，又称本地内存，可与CPU高速缓存类比)， **<font color = "red">线程的工作内存中保存了该线程使用到的主内存中的共享变量的副本拷贝。线程对变量的所有操作都必须在工作内存进行，而不能直接读写主内存中的变量。</font>**  
    * Java栈中的部分区域
    * 优先存储于寄存器和高速缓存

<!-- 
&emsp; Java内存模型的几个规范：  
1. 所有变量存储在主内存  
2. 主内存是虚拟机内存的一部分  
3. 每条线程有自己的工作内存  
4. 线程的工作内存保存变量的主内存副本  
5. 线程对变量的操作必须在工作内存中进行  
6. 不同线程之间无法直接访问对方工作内存中的变量  
7. 线程间变量值的传递均需要通过主内存来完成  
-->

### 1.1.2. JMM内存间的交互操作  
&emsp; Java内存模型为主内存和工作内存间的变量拷贝及同步定义了8种原子性操作指令。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-8.png)   

* read(读取)：从主内存读取数据。  
* load(载入)：将主内存读取到的数据。  
* use(使用)：从工作内存读取数据来计算。  
* assign(赋值)：将计算好的值重新赋值到工作内存中。
* store(存储)：将工作内存数据写入主内存。 
* write(写入)：将store过去的变量值赋值给主内存中的变量。 
* lock(锁定)：将主内存变量加锁，标识为线程独占状态。  
* unlock(解锁)：将主内存变量解锁，解锁后其他线程可以锁定该变量。  

&emsp; 要把一个变量从主内存中复制到工作内存，就需要按顺序地执行read和load操作，如果把变量从工作内存中同步回主内存中，就要按顺序地执行store和write操作。  
&emsp; Java内存模型只要求上述两个操作必须按顺序执行，而没有保证必须是连续执行。也就是read和load之间，store和write之间是可以插入其他指令的。  

&emsp; Java内存模型还规定了在执行上述八种基本操作时，必须满足如下规则：  

* 不允许read和load、store和write操作之一单独出现。  
* 不允许一个线程丢弃它的最近assign的操作，即变量在工作内存中改变了之后必须同步到主内存中。  
* 不允许一个线程无原因地(没有发生过任何assign操作)把数据从工作内存同步回主内存中。  
* 一个新的变量只能在主内存中诞生，不允许在工作内存中直接使用一个未被初始化(load或assign)的变量。即就是对一个变量实施use和store操作之前，必须先执行过了assign和load操作。  
* 一个变量在同一时刻只允许一条线程对其进行lock操作，lock和unlock必须成对出现  
* 如果对一个变量执行lock操作，将会清空工作内存中此变量的值，在执行引擎使用这个变量前需要重新执行load或assign操作初始化变量的值  
* 如果一个变量事先没有被lock操作锁定，则不允许对它执行unlock操作；也不允许去unlock一个被其他线程锁定的变量。  
* 对一个变量执行unlock操作之前，必须先把此变量同步到主内存中(执行store和write操作)。  

### 1.1.3. 线程之间的通信和同步  
&emsp; **JMM定义了Java虚拟机(JVM)在计算机内存(RAM)中的工作方式。JMM主要规定了以下两点：**  

* 规定了一个线程如何以及何时可以看到其他线程修改过后的共享变量的值，即线程之间共享变量的可见性。  
* 如何在需要的时候对共享变量进行同步。  

&emsp; **<font color = "clime">在并发编程需要处理的两个关键问题是：线程之间如何通信和线程之间如何同步，即以上两条标准的体现。</font>** **<font color = "red">线程通信是一种手段，而线程同步是一种目的，即线程通信的主要目的是用于线程同步。线程同步是为了解决线程安全问题。</font>**

* 线程通信  

        通信是指线程之间以何种机制来交换信息。在命令式编程中，线程之间的通信机制有两种：共享内存和消息传递。
        在共享内存的并发模型里，线程之间共享程序的公共状态，线程之间通过写-读内存中的公共状态来隐式进行通信。
        在消息传递的并发模型里，线程之间没有公共状态，线程之间必须通过明确的发送消息来显式进行通信。

* 线程同步  

        同步是指程序用于控制不同线程之间操作发生相对顺序的机制。
        在共享内存的并发模型里，同步是显式进行的。程序员必须显式指定某个方法或某段代码需要在线程之间互斥执行。
        在消息传递的并发模型里，由于消息的发送必须在消息的接收之前，因此同步是隐式进行的。    

&emsp; **<font color = "red">Java的并发采用的是共享内存模型</font>，**Java线程之间的通信总是隐式进行，整个通信过程对程序员完全透明。  

#### 1.1.3.1. ★★★线程之间的通信过程  
&emsp; 由于JVM运行程序的实体是线程，而每个线程创建时JVM都会为其创建一个工作内存(有些地方称为栈空间)，工作内存是每个线程的私有数据区域。  
&emsp; 而Java内存模型中规定所有变量都存储在主内存，主内存是共享内存区域，所有线程都可以访问，<font color = "clime">但线程对变量的操作(读取赋值等)必须在工作内存中进行，首先要将变量从主内存拷贝到自己的工作内存空间，然后对变量进行操作，操作完成后再将变量写回主内存，不能直接操作主内存中的变量，</font>各个线程中的工作内存中存储着主内存中的变量副本拷贝，<font color = "red">因此不同的线程间无法访问对方的工作内存，线程间的通信(传值)必须通过主内存来完成。</font>其简要访问过程：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-42.png)   




## 1.2. 内存屏障，禁止处理器重排序 / 【有序性】  
<!-- 
内存屏障有两个作⽤：  
1. 阻⽌屏障两侧的指令重排序；  
2. 强制把写缓冲区/⾼速缓存中的脏数据等写回主内存，或者让缓存中相应的数据失效。 
&emsp; **<font color= "red">内存屏障是被插入两个CPU指令之间的一种指令，用来禁止处理器指令发生重排序，从而保障有序性的。另外，为了达到屏障的效果，它也会使处理器写入、读取值之前，将主内存的值写入高速缓存，清空无效队列，从而保障可见性。</font>**  
-->
&emsp; **<font color = "red">Java中如何保证底层操作的有序性和可见性？可以通过内存屏障。</font>**  

&emsp; 什么是内存屏障？硬件层⾯，<font color = "red">内存屏障分两种：读屏障(Load Barrier)和写屏障(Store Barrier)。</font>  

&emsp; **<font color = "clime">内存屏障的作用：(~~原子性~~、可见性、有序性)</font>**  

* **<font color = "clime">(保障可见性)它会强制将对缓存的修改操作立即写入主存；</font>** **<font color = "red">如果是写操作，会触发总线嗅探机制(MESI)，会导致其他CPU中对应的缓存行无效，</font>** **<font color = "clime">也有[伪共享问题](/docs/java/concurrent/PseudoSharing.md)。</font>**  
* **<font color = "clime">(保障有序性)阻⽌屏障两侧的指令重排序。</font>** 它确保指令重排序时不会把其后面的指令排到内存屏障之前的位置，也不会把前面的指令排到内存屏障的后面；即在执行到内存屏障这句指令时，在它前面的操作已经全部完成；  



### 1.2.1. JVM中的内存屏障
<!-- 
https://blog.csdn.net/breakout_alex/article/details/94379895

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

* 如果操作1 happens-before 操作2，那么第操作1的执行结果将对操作2可见，而且操作1的执行顺序排在第操作2之前。
* 两个操作之间存在happens-before关系，并不意味着一定要按照happens-before原则制定的顺序来执行。如果重排序之后的执行结果与按照happens-before关系来执行的结果一致，那么这种重排序并不非法。  


----

&emsp; happens-before关系的定义如下：

* 如果一个操作happens-before另一个操作，那么第一个操作的执行结果将对第二个操作可见，而且第一个操作的执行顺序排在第二个操作之前。  
* 两个操作之间存在happens-before关系，并不意味着Java平台的具体实现必须要按照happens-before关系指定的顺序来执行。如果重排序之后的执行结果，与按happens-before关系来执行的结果一致，那么JMM也允许这样的重排序。  
<!-- 
&emsp; Happens-before规则主要用来约束两个操作，两个操作之间具有 happens-before关系, 并不意味着前一个操作必须要在后一个操作之前执行，happens-before 仅仅要求前一个操作(执行的结果)对后一个操作可见, (the first is visible to and ordered before the second，前一个操作的结果可以被后续的操作获取)。  
-->

-----
&emsp; happens-before关系的分析需要分为单线程和多线程的情况：  

* 单线程下的 happens-before 字节码的先后顺序天然包含happens-before关系：因为单线程内共享一份工作内存，不存在数据一致性的问题。在程序控制流路径中靠前的字节码 happens-before 靠后的字节码，即靠前的字节码执行完之后操作结果对靠后的字节码可见。然而，这并不意味着前者一定在后者之前执行。实际上，如果后者不依赖前者的运行结果，那么它们可能会被重排序。  
* 多线程下的 happens-before 多线程由于每个线程有共享变量的副本，如果没有对共享变量做同步处理，线程1更新执行操作A共享变量的值之后，线程2开始执行操作B，此时操作A产生的结果对操作B不一定可见。  

&emsp; 为了方便程序开发，Java 内存模型实现了下述的先行发生关系：  

* 程序次序规则：在一个单独的线程中，按照程序代码的执行流顺序，(时间上)先执行的操作happen—before(时间上)后执行的操作。  
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

