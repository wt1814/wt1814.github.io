
<!-- TOC -->

- [1. 并发安全问题](#1-并发安全问题)
    - [1.1. 并发安全问题及含义](#11-并发安全问题及含义)
    - [1.2. 缓存导致了可见性问题](#12-缓存导致了可见性问题)
    - [1.3. 线程切换带来的原子性问题](#13-线程切换带来的原子性问题)
    - [1.4. 编译优化带来的有序性问题](#14-编译优化带来的有序性问题)
        - [1.4.1. 重排序](#141-重排序)
        - [1.4.2. 重排序分类](#142-重排序分类)
        - [1.4.3. 重排序规则](#143-重排序规则)
            - [1.4.3.1. 重排序遵守数据依赖性](#1431-重排序遵守数据依赖性)
            - [1.4.3.2. 重排序遵守as-if-serial语义](#1432-重排序遵守as-if-serial语义)
        - [1.4.4. 重排序对多线程的影响](#144-重排序对多线程的影响)
        - [1.4.5. JMM中的happens-before原则](#145-jmm中的happens-before原则)
    - [1.5. 内存屏障](#15-内存屏障)
    - [1.6. 内存屏障带来的伪共享问题](#16-内存屏障带来的伪共享问题)
        - [1.6.1. CPU缓存架构](#161-cpu缓存架构)
        - [1.6.2. CPU缓存行](#162-cpu缓存行)
        - [1.6.3. 伪共享](#163-伪共享)
        - [1.6.4. 避免伪共享](#164-避免伪共享)
        - [1.6.5. 小结](#165-小结)

<!-- /TOC -->

&emsp; **<font color = "lime">总结：</font>**  
&emsp; 并发安全的3个问题：  

* 缓存导致了可见性问题
* 线程切换带来的原子性问题
* 编译优化带来的有序性问题  
    * 重排序及其分类
    * 重排序遵守的规则
    * 重排序对多线程的影响
    * JMM中的happens-before原则

&emsp; **<font color = "red">Java中如何保证底层操作的有序性和可见性？可以通过内存屏障。</font>**  
&emsp; 内存屏障会引发伪共享问题。  

# 1. 并发安全问题
<!-- 
【Java 并发003】原理层面：Java并发三特性全解析
https://www.cnblogs.com/maoqizhi/p/13909179.html
-->
<!--
~~ 
https://mp.weixin.qq.com/s/DaCTrm8y9vWeaJyHfbRoTw
-->

## 1.1. 并发安全问题及含义  
&emsp; 并发编程存在原子性、可见性、有序性问题。  

* 原子性，即一系列操作要么都执行，要么都不执行。  
&emsp;  线程切换会导致原子性问题。  
* 可见性，当一个线程修改了共享变量的值时，其他线程能够立即得知这个修改。  
&emsp; 由于多核CPU，每个CPU核都有高速缓存，会缓存共享变量，某个线程对共享变量的修改会改变高速缓存中的值，但却不会马上写入内存。另一个线程读到的是另一个核缓存的共享变量的值，**出现缓存不一致问题。**  
* 有序性，即程序执行的顺序按照代码的先后顺序执行。    
&emsp; 编译器和处理器会对指令进行重排，以优化指令执行性能，重排不会改变单线程执行结果，但在多线程中可能会引起各种各样的问题，包括有序性。  
&emsp; 关于有序性：如果在本线程内观察，所有的操作都是有序的；如果在一个线程中观察另一个线程，所有的操作都是无序的。前半句是指“线程内似表现为串行的语义”(Within-Thread As-If-Serial Semantics)，后半句是指“指令重排序”现象和“工作内存与主内存同步延迟”现象。  

&emsp; **总结： <font color = "red">出现线程安全问题的原因：</font>**  

* 线程切换带来的原子性问题；
* 缓存不能及时刷新导致的可见性问题；
* 编译器优化带来的有序性问题；  

&emsp; “缓存不能及时刷新“和“编译器为了优化性能而改变程序中语句的先后顺序”都是重排序的一种。   

------
&emsp; 在计算机中cpu、缓存、I/O设备这个三者之间速度的差异一直存在问题，cpu>缓存>I/O设备。按照串行的逻辑，程序的整体速度取决速度最慢的I/O设备。  

&emsp; 为了提高程序的整体性能，做了如下的升级  

* 增加cpu缓存，平衡cpu和内存的速度差异
* 操作系统增加多进程、多线程；通过分时复用平衡CPU和I/O设备间的速度差异
* 编译器指令优化，可以充分利用缓存，提高执行效率

&emsp; 但是凡事有利必有弊，性能提高的同时，也会引发一些问题。并发编程中的问题源头就是因为这个导致的。  

## 1.2. 缓存导致了可见性问题  
&emsp; 目前操作系统都是多核的，cpu都有自己的缓存，这个时候就需要考虑数据的一致性问题。某一个变量被多个线程操作；相互的操作是不可见的。这个时候就会出现问题了，这就是可见性问题。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-78.png)  

## 1.3. 线程切换带来的原子性问题  
&emsp; 现代操作系统都是基于线程调度的，java并发程序出现的多线程，会涉及到线程切换，在一条语句可能需要多个cpu指令完成。例如代码count+=1大概需要三条指令。  

* 把变量 count 从内存加载到CPU的寄存器中
* 在寄存器中把变量 count + 1
* 把变量 count 写入到内存(缓存机制导致可能写入的是CPU缓存而不是内存)

&emsp; 操作系统做任务切换，可以发生在任何一条CPU指令执行完，所以并不是高级语言中的一条语句，不要被 count += 1 这个操作蒙蔽了双眼。假设count = 0，线程A执行完 指令1 后 ，做任务切换到线程B执行了 指令1、指令2、指令3后，再做任务切换回线程A。会发现虽然两个线程都执行了 count += 1 操作。但是得到的结果并不是2，而是1。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-77.png)  
&emsp; 如果 count += 1 是一个不可分割的整体，线程的切换可以发生在 count += 1 之前或之后，但是不会发生在中间，就像个原子一样。**把一个或者多个操作在 CPU 执行的过程中不被中断的特性称为原子性。**

## 1.4. 编译优化带来的有序性问题    
### 1.4.1. 重排序  
&emsp; 在执行程序时，为了提供性能，处理器和编译器常常会对指令进行重排序，但是<font color = "lime">不能随意重排序，它需要满足以下两个条件：</font>  

* <font color = "red">在单线程环境下不能改变程序运行的结果；</font>  
* <font color = "red">存在数据依赖关系的不允许重排序。</font>  

&emsp; 需要注意的是：重排序不会影响单线程环境的执行结果，但是会破坏多线程的执行语义。  

### 1.4.2. 重排序分类  
&emsp; 从Java源代码到最终实际执行的指令序列，会分别经历下面三种重排序：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-2.png)  

<!-- 
1. 编译器优化的重排序。编译器在不改变单线程程序语义的前提下，可以重新安排语句的执行顺序。  
2. 指令级并行的重排序。现代处理器采用了指令级并行技术来将多条指令重叠执行。如果不存在数据依赖性，处理器可以改变语句对应机器指令的执行顺序。  
3. 内存系统的重排序。由于处理器使用缓存和读／写缓冲区，这使得加载和存储操作看上去可能是在乱序执行。  
-->

1. 编译器优化：对于没有数据依赖关系的操作，编译器在编译的过程中会进行一定程度的重排。  
2. 指令重排序：CPU优化行为，也是会对不存在数据依赖关系的指令进行一定程度的重排。  
3. 内存系统重排序：内存系统没有重排序，但是由于有缓存的存在，使得程序整体上会表现出乱序的行为。  

&emsp; 上面的这些重排序都可能导致多线程程序出现内存可见性问题。对于编译器，JMM的编译器重排序规则会禁止特定类型的编译器重排序(不是所有的编译器重排序都要禁止)。对于处理器重排序，JMM的处理器重排序规则会要求Java编译器在生成指令序列时，插入特定类型的内存屏障指令，通过内存屏障指令来禁止特定类型的处理器重排序(不是所有的处理器重排序都要禁止)。  
&emsp; JMM属于语言级的内存模型，它确保在不同的编译器和不同的处理器平台之上，通过禁止特定类型的编译器重排序和处理器重排序，为程序员提供一致的内存可见性保证。  

### 1.4.3. 重排序规则  
#### 1.4.3.1. 重排序遵守数据依赖性  
&emsp; 如果两个操作访问同一个变量，且这两个操作中有一个为写操作，此时这两个操作之间就存在数据依赖性。数据依赖分下列三种类型：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-3.png)  
&emsp; 上面三种情况，只要重排序两个操作的执行顺序，程序的执行结果将会被改变。  

&emsp; 编译器和处理器可能会对操作做重排序。编译器和处理器在重排序时，会遵守数据依赖性，编译器和处理器不会改变存在数据依赖关系的两个操作的执行顺序。  
&emsp; 注意，这里所说的数据依赖性仅针对单个处理器中执行的指令序列和单个线程中执行的操作，不同处理器之间和不同线程之间的数据依赖性不被编译器和处理器考虑。  

#### 1.4.3.2. 重排序遵守as-if-serial语义  
&emsp; **<font color = "red">as-if-serial语义的意思指：不管怎么重排序(编译器和处理器为了提高并行度)，(单线程)程序的执行结果不能被改变。编译器，runtime和处理器都必须遵守as-if-serial语义。</font>**  

### 1.4.4. 重排序对多线程的影响  
&emsp; 示例代码：  

```java
class Demo {
    int a = 0;
    boolean flag = false;

    public void write() {
        a = 1;            //1
        flag = true;    //2
    }

    public void read() {
        if(flag) {            //3
            int i = a * a;    //4
        }
    }
}
```
&emsp; 由于操作1和2没有数据依赖关系，编译器和处理器可以对这两个操作重排序；操作3和操作4没有数据依赖关系，编译器和处理器也可以对这两个操作重排序。  

1. 当操作1和操作2重排序时，可能会产生什么效果？  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-4.png)  
&emsp; 如上图所示，操作1和操作2做了重排序。程序执行时，线程A首先写标记变量flag，随后线程B读这个变量。由于条件判断为真，线程B将读取变量a。此时，变量a还根本没有被线程A写入，在这里多线程程序的语义被重排序破坏了！  
2. 当操作3和操作4重排序时会产生什么效果(借助这个重排序，可以顺便说明控制依赖性)。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-5.png)  
&emsp; 在程序中，操作3和操作4存在控制依赖关系。当代码中存在控制依赖性时，会影响指令序列执行的并行度。为此，编译器和处理器会采用猜测(Speculation)执行来克服控制相关性对并行度的影响。以处理器的猜测执行为例，执行线程B 的处理器可以提前读取并计算a * a，然后把计算结果临时保存到一个名为重排序缓冲(reorder buffer ROB)的硬件缓存中。当接下来操作3的条件判断为真时，就把该计算结果写入变量i中。  

&emsp; 从图中可以看出，猜测执行实质上对操作3和4做了重排序。重排序在这里破坏了多线程程序的语义！  
&emsp; 在单线程程序中，对存在控制依赖的操作重排序，不会改变执行结果(这也是as-if-serial语义允许对存在控制依赖的操作做重排序的原因)；但在多线程程序中，对存在控制依赖的操作重排序，可能会改变程序的执行结果。  

----------

### 1.4.5. JMM中的happens-before原则
&emsp; JSR-133内存模型 **<font color = "red">使用happens-before的概念来阐述操作之间的内存可见性。在JMM中，如果一个操作执行的结果需要对另一个操作可见，那么这两个操作之间必须要存在happens-before关系。</font>** 这里提到的两个操作既可以是在一个线程之内，也可以是在不同线程之间。  
<!-- 
&emsp; Happens-before规则主要用来约束两个操作，两个操作之间具有 happens-before关系, 并不意味着前一个操作必须要在后一个操作之前执行，happens-before 仅仅要求前一个操作(执行的结果)对后一个操作可见, (the first is visible to and ordered before the second，前一个操作的结果可以被后续的操作获取)。  
-->
&emsp; happens-before关系的分析需要分为单线程和多线程的情况：  

* 单线程下的 happens-before 字节码的先后顺序天然包含happens-before关系：因为单线程内共享一份工作内存，不存在数据一致性的问题。在程序控制流路径中靠前的字节码 happens-before 靠后的字节码，即靠前的字节码执行完之后操作结果对靠后的字节码可见。然而，这并不意味着前者一定在后者之前执行。实际上，如果后者不依赖前者的运行结果，那么它们可能会被重排序。  
* 多线程下的 happens-before 多线程由于每个线程有共享变量的副本，如果没有对共享变量做同步处理，线程1更新执行操作A共享变量的值之后，线程2开始执行操作B，此时操作A产生的结果对操作B不一定可见。  

&emsp; 为了方便程序开发，Java 内存模型实现了下述的先行发生关系：  

* 程序次序规则： 在一个单独的线程中，按照程序代码的执行流顺序，(时间上)先执行的操作happen—before(时间上)后执行的操作。  
(同一个线程中前面的所有写操作对后面的操作可见)
* 管理锁定规则：一个unlock操作happen—before后面(时间上的先后顺序)对同一个锁的lock操作。  
(如果线程1解锁了monitor a，接着线程2锁定了a，那么，线程1解锁a之前的写操作都对线程2可见(线程1和线程2可以是同一个线程))  
* volatile变量规则：对一个volatile变量的写操作happen—before后面(时间上)对该变量的读操作。  
(如果线程1写入了volatile变量v(临界资源)，接着线程2读取了v，那么，线程1写入v及之前的写操作都对线程2可见(线程1和线程2可以是同一个线程)) 

---- 
* <font color = "red">线程启动规则：</font>Thread.start()方法happen—before调用用start的线程前的每一个操作。  
(假定线程A在执行过程中，通过执行ThreadB.start()来启动线程B，那么线程A对共享变量的修改在接下来线程B开始执行前对线程B可见。注意：线程B启动之后，线程A在对变量修改线程B未必可见。)  
* <font color = "red">线程终止规则：</font>线程的所有操作都happen—before对此线程的终止检测，可以通过Thread.join()方法结束、Thread.isAlive()的返回值等手段检测到线程已经终止执行。  
(线程t1写入的所有变量，在任意其它线程t2调用t1.join()，或者t1.isAlive() 成功返回后，都对t2可见。)  
* <font color = "red">线程中断规则：</font>对线程interrupt()的调用 happen—before 发生于被中断线程的代码检测到中断时事件的发生。  
(线程t1写入的所有变量，调用Thread.interrupt()，被打断的线程t2，可以看到t1的全部操作)  

---
* 对象终结规则：一个对象的初始化完成(构造函数执行结束)happen—before它的finalize()方法的开始。  
(对象调用finalize()方法时，对象初始化完成的任意操作，同步到全部主存同步到全部cache。)  
* 传递性：如果操作A happen—before操作B，操作B happen—before操作C，那么可以得出A happen—before操作C。  
(A h-b B， B h-b C 那么可以得到 A h-b C)  

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

----

## 1.5. 内存屏障  
&emsp; **<font color = "red">Java中如何保证底层操作的有序性和可见性？可以通过内存屏障。</font>**  

&emsp; 什么是内存屏障？硬件层⾯，<font color = "red">内存屏障分两种：读屏障(Load Barrier)和写屏障(Store Barrier)。</font>  

&emsp; **<font color = "lime">内存屏障的作用：</font>**  

* **<font color = "lime">(保障有序性)阻⽌屏障两侧的指令重排序。</font>** 它确保指令重排序时不会把其后面的指令排到内存屏障之前的位置，也不会把前面的指令排到内存屏障的后面；即在执行到内存屏障这句指令时，在它前面的操作已经全部完成；  
* **<font color = "lime">(保障可见性)它会强制将对缓存的修改操作立即写入主存；</font>** **<font color = "red">如果是写操作，会触发总线嗅探机制(MESI)，会导致其他CPU中对应的缓存行无效，</font>** **<font color = "clime">会引发伪共享问题。</font>**  

<!-- 
内存屏障有两个作⽤：  
1. 阻⽌屏障两侧的指令重排序；  
2. 强制把写缓冲区/⾼速缓存中的脏数据等写回主内存，或者让缓存中相应的数据失效。 
&emsp; **<font color= "red">内存屏障是被插入两个CPU指令之间的一种指令，用来禁止处理器指令发生重排序，从而保障有序性的。另外，为了达到屏障的效果，它也会使处理器写入、读取值之前，将主内存的值写入高速缓存，清空无效队列，从而保障可见性。</font>**  
-->
&emsp; <font color = "red">常见的4种屏障：(load载入，store存储)</font>  

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

## 1.6. 内存屏障带来的伪共享问题
<!-- 
https://blog.csdn.net/qq_28119741/article/details/102815659
-->

### 1.6.1. CPU缓存架构
&emsp; CPU是计算机的心脏，所有运算和程序最终都要由它来执行。  
&emsp; 主内存(RAM)是数据存放的地方，CPU 和主内存之间有好几级缓存，因为即使直接访问主内存也是非常慢的。  
&emsp; 如果对一块数据做相同的运算多次，那么在执行运算的时候把它加载到离 CPU 很近的地方就有意义了，比如一个循环计数，不想每次循环都跑到主内存去取这个数据来增长它吧。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-50.png)  
&emsp; <font color = "red">越靠近 CPU 的缓存越快也越小。</font>  
&emsp; 所以 L1 缓存很小但很快，并且紧靠着在使用它的 CPU 内核。  
&emsp; L2 大一些，也慢一些，并且仍然只能被一个单独的 CPU 核使用。  
&emsp; L3 在现代多核机器中更普遍，仍然更大，更慢，并且被单个插槽上的所有 CPU 核共享。  
&emsp; 最后，主存保存着程序运行的所有数据，它更大，更慢，由全部插槽上的所有 CPU 核共享。  
&emsp; 当 CPU 执行运算的时候，它先去 L1 查找所需的数据，再去 L2，然后是 L3，最后如果这些缓存中都没有，所需的数据就要去主内存拿。  
&emsp; 走得越远，运算耗费的时间就越长。  
&emsp; 所以如果进行一些很频繁的运算，要确保数据在 L1 缓存中。  

### 1.6.2. CPU缓存行  
&emsp; 缓存是由缓存行组成的，通常是64字节(常用处理器的缓存行是 64 字节的，比较旧的处理器缓存行是 32 字节)，并且它有效地引用主内存中的一块地址。  
&emsp; <font color = "red">一个Java的long类型是8字节，因此在一个缓存行中可以存8个long类型的变量。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-51.png)  
&emsp; <font color = "red">在程序运行的过程中，缓存每次更新都从主内存中加载连续的64个字节。因此，如果访问一个long类型的数组时，当数组中的一个值被加载到缓存中时，另外7个元素也会被加载到缓存中。</font>  
&emsp; 但是，如果使用的数据结构中的项在内存中不是彼此相邻的，比如链表，那么将得不到免费缓存加载带来的好处。  
&emsp; 不过，这种免费加载也有一个坏处。设想如果有个long类型的变量a，它不是数组的一部分，而是一个单独的变量，并且还有另外一个long类型的变量b紧挨着它，那么当加载a的时候将免费加载b。  
&emsp; 看起来似乎没有什么毛病，但是如果一个CPU核心的线程在对a进行修改，另一个CPU核心的线程却在对b进行读取。  
&emsp; 当前者修改 a 时，会把 a 和 b 同时加载到前者核心的缓存行中，更新完 a 后其它所有包含 a 的缓存行都将失效，因为其它缓存中的 a 不是最新值了。  
&emsp; 而当后者读取 b 时，发现这个缓存行已经失效了，需要从主内存中重新加载。  
&emsp; 请记住，缓存都是以缓存行作为一个单位来处理的，所以失效 a 的缓存的同时，也会把 b 失效，反之亦然。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-52.png)  
&emsp; 这样就出现了一个问题，<font color = "lime">b和a完全不相干，每次却要因为a的更新需要从主内存重新读取，它被缓存未命中给拖慢了。</font>  
&emsp; 这就是伪共享问题。  

### 1.6.3. 伪共享  
&emsp; **<font color = "lime">当多线程修改互相独立的变量时，如果这些变量共享同一个缓存行，就会无意中影响彼此的性能，这就是伪共享。</font>**  
&emsp; 伪共享示例：  

```java
public class FalseSharingTest {

    public static void main(String[] args) throws InterruptedException {
        testPointer(new Pointer());
    }

    private static void testPointer(Pointer pointer) throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100000000; i++) {
                pointer.x++;
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000000; i++) {
                pointer.y++;
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println(System.currentTimeMillis() - start);
        System.out.println(pointer);
    }
}

class Pointer {
    volatile long x;
    volatile long y;
}
```
&emsp; 这个例子中，声明了一个 Pointer 的类，它包含x和y两个变量，一个线程对 x 进行自增1亿次，一个线程对y进行自增1亿次。  
&emsp; 可以看到，x和y完全没有任何关系，但是更新 x 的时候会把其它包含x的缓存行失效，同时也就失效了y，运行这段程序输出的时间为3890ms。  

### 1.6.4. 避免伪共享  
&emsp; 伪共享的原理中，一个缓存行是 64 个字节，一个 long 类型是 8 个字节，所以避免伪共享也很简单，笔者总结了下大概有以下三种方式：

1. <font color = "red">在两个long类型的变量之间再加7个long类型</font>  
    &emsp; 可以把上面的Pointer改成下面这个结构：

    ```java
    class Pointer {
        volatile long x;
        long p1, p2, p3, p4, p5, p6, p7;
        volatile long y;
    }
    ```
    &emsp; 再次运行程序，会发现输出时间神奇的缩短为了695ms。

2. <font color = "red">重新创建自己的long类型，而不是java自带的long</font>  
    &emsp; 修改Pointer如下：

    ```java
    class Pointer {
        MyLong x = new MyLong();
        MyLong y = new MyLong();
    }

    class MyLong {
        volatile long value;
        long p1, p2, p3, p4, p5, p6, p7;
    }
    ```
    &emsp; 同时把 pointer.x++; 修改为 pointer.x.value++;，把 pointer.y++; 修改为 pointer.y.value++;，再次运行程序发现时间是724ms。  
3. **<font color = "lime">使用@sun.misc.Contended注解(java8)</font>**  
    &emsp; 修改MyLong如下：

    ```java
    @sun.misc.Contended
    class MyLong {
        volatile long value;
    }
    ```
    &emsp; 默认使用这个注解是无效的，需要在JVM启动参数加上-XX:-RestrictContended才会生效，，再次运行程序发现时间是718ms。  
    &emsp; 注意，以上三种方式中的前两种是通过加字段的形式实现的，加的字段又没有地方使用，可能会被jvm优化掉，所以建议使用第三种方式。  

### 1.6.5. 小结  
1. CPU具有多级缓存，越接近CPU的缓存越小也越快；  
2. CPU缓存中的数据是以缓存行为单位处理的；  
3. CPU缓存行能带来免费加载数据的好处，所以处理数组性能非常高；  
4. CPU缓存行也带来了弊端，多线程处理不相干的变量时会相互影响，也就是伪共享；  
5. 避免伪共享的主要思路就是让不相干的变量不要出现在同一个缓存行中；  
6. 一是每两个变量之间加七个long类型；  
7. 二是创建自己的 long 类型，而不是用原生的；  
8. 三是使用java8提供的注解；  

