
<!-- TOC -->

- [1. 逃逸分析](#1-逃逸分析)
    - [1.1. 基本概念](#11-基本概念)
    - [1.2. 详解](#12-详解)
        - [1.2.1. 对象逃逸分析后的状态](#121-对象逃逸分析后的状态)
        - [1.2.2. 逃逸分析优化后的结果](#122-逃逸分析优化后的结果)
            - [1.2.2.1. 对象可能分配在栈上](#1221-对象可能分配在栈上)
                - [1.2.2.1.1. 介绍](#12211-介绍)
                - [1.2.2.1.2. 示例](#12212-示例)
            - [1.2.2.2. 分离对象或标量替换](#1222-分离对象或标量替换)
                - [1.2.2.2.1. 介绍](#12221-介绍)
                - [1.2.2.2.2. 示例](#12222-示例)
            - [1.2.2.3. 同步锁消除](#1223-同步锁消除)
                - [1.2.2.3.1. 介绍](#12231-介绍)
                - [1.2.2.3.2. 示例](#12232-示例)
    - [1.3. 性能](#13-性能)

<!-- /TOC -->
&emsp; **<font color = "red">总结：</font>**  
1. <font color = "red">通过逃逸分析算法可以分析出某一个方法中的某个对象是否会被其它方法或者线程访问到。</font>如果分析结果显示某对象并不会被其他方法引用或被其它线程访问，则有可能在编译期间做一些深层次的优化。   
2. 对于NoEscape(没有逃逸)状态的对象，则不一定，具体会有这种优化情况：   
    1. 对象可能分配在栈上  
    2. 分离对象或标量替换。  
    &emsp; **<font color = "clime">在HotSpot中并没有真正的实现"栈"中分配对象的功能，取而代之的是一个叫做"标量替换"的折中办法。</font>**  
    &emsp; 什么是标量？标量，不可再分，基本数据类型；相对的是聚合量，可再分，引用类型。  
    &emsp; **当JVM通过逃逸分析，确定要将对象分配到栈上时，即时编译可以将对象打散，将对象替换为一个个很小的局部变量，将这个打散的过程叫做标量替换。** 
    3. 消除同步锁


# 1. 逃逸分析 
<!--
***  Java对象竟然会在栈上分配内存？
https://mp.weixin.qq.com/s/GcdlQ0ipXlTj7AVV3cd7Ug

https://mp.weixin.qq.com/s/BUcFh9ArENu9rlMdmlDNWg
https://mp.weixin.qq.com/s/jPIHNsQwiYNCRUQt1qXR6Q
Java中的对象不一定是在堆上分配的。  
-->  
&emsp; 随着JIT编译器的发展和逃逸分析技术的逐渐成熟，栈上分配、标量替换优化技术将会导致一些微妙的变化，<font color = "clime">所有的对象都分配到堆上也渐渐变得不那么“绝对”了。</font>——《深入理解 Java 虚拟机》  


&emsp; 经常会有面试官会问一个问题：Java中的对象都是在"堆"中创建吗？然后跟求职者大谈特谈"逃逸分析"，说通过"逃逸分析"，JVM会将实例对象分配在"栈"上。其实这种说法是并不是很严谨，最起码目前在HotSpot中并没有在栈中存储对象的实现代码！  

## 1.1. 基本概念  
&emsp; **逃逸分析是一种算法，这套算法在Java即时编译器(JIT)编译Java源代码时使用。**  
&emsp; 逃逸分析的基本行为就是分析对象动态作用域：  

* 当一个对象在方法中被定义后，对象只在方法内部使用，则认为没有发生逃逸。  
* 当一个对象在方法中被定义后，它被外部方法所引用，则认为发生逃逸。例如作为调用参数传递到其他地方中，称为方法逃逸。  


&emsp; <font color = "red">通过逃逸分析算法可以分析出某一个方法中的某个对象是否会被其它方法或者线程访问到。</font>如果分析结果显示某对象并不会被其他方法引用或被其它线程访问，则有可能在编译期间做一些深层次的优化。   

<!-- 
&emsp; JVM通过逃逸分析，能够分析出一个新对象的使用范围，并以此确定是否要将这个对象分配到堆上。如果JVM发现某些对象没有逃逸出方法，就很有可能被优化成在栈上分配。  

判断对象的作用域是否超出函数体[即:判断是否逃逸出函数体]  
  逃逸分析，是一种可以有效减少Java 程序中同步负载和内存堆分配压力的跨函数全局数据流分析算法
JIT对于对象的作用域的一个分析判断：如果一个对象在方法中定义后，不会被外部的方法引用到或是外部线程访问到，就可以认为这个这个对象不会引用逃逸。  

&emsp; 在JVM的即时编译语境下，逃逸分析将判断新建的对象是否逃逸。即时编译判断对象是否逃逸的依据：一种是对象是否被存入堆中(静态字段或者堆中对象的实例字段)，另一种就是对象是否被传入未知代码。  
&emsp; 通过逃逸分析算法可以分析出某一个方法中的某个对象是否会被其它方法或者线程访问到。如果分析结果显示某对象并不会被其它线程访问，则有可能在编译期间将对象分配在栈上，即时是线程私有的。    
&emsp; <font color = "red">逃逸指的是逃出当前线程，所以逃逸对象就是可以被其他线程访问的对象，非逃逸对象就是线程私有的对象。</font>  
-->

&emsp; 执行java程序时，可以通过如下参数开启或者关闭"逃逸分析"：  

* 开启逃逸分析：-XX:+DoEscapeAnalysis
* 关闭逃逸分析：-XX:-DoEscap

<!-- 
对象逃逸示例  

&emsp; 一种典型的对象逃逸就是：对象被复制给成员变量或者静态变量，可能被外部使用，此时变量就发生了逃逸。  
&emsp; 可以用下面的代码来表示这个现象。  

```java
/**
 * @description 对象逃逸示例1
 */
public class ObjectEscape{
    private User user;
    public void init(){
        user = new User();
    }
}
```
&emsp; 在ObjectEscape类中，存在一个成员变量user，在init()方法中，创建了一个User类的对象，并将其赋值给成员变量user。此时，对象被复制给了成员变量，可能被外部使用，此时的变量就发生了逃逸。  

&emsp; 另一种典型的场景就是：对象通过return语句返回。如果对象通过return语句返回了，此时的程序并不能确定这个对象后续会不会被使用，外部的线程可以访问到这个变量，此时对象也发生了逃逸。  
&emsp; 可以用下面的代码来表示这个现象。  

```java
/**
 * @description 对象逃逸示例2
 */
public class ObjectReturn{
    public User createUser(){
        User user = new User();
        return user;
    }
}
```
-->

## 1.2. 详解  
### 1.2.1. 对象逃逸分析后的状态
<!-- 
经过逃逸分析后，可以得到三种对象的逃逸状态：
1.GlobalEscape(全局逃逸)， 即一个对象的引用逃出了方法或者线程。例如，一个对象的引用是复制给了一个类变量，或者存储在在一个已经逃逸的对象当中，或者这个对象的引用作为方法的返回值返回给了调用方法。
2.ArgEscape(参数级逃逸)，即在方法调用过程当中传递对象的应用给一个方法。
3.NoEscape(没有逃逸)，一个可以进行标量替换的对象。可以不将这种对象分配在传统的堆上。
-->


&emsp; 在HotSpot源码中的src/share/vm/opto/escape.hpp中定义了对象进行逃逸分析后的几种状态：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-94.png)  
1. **全局逃逸(GlobalEscape)**  
&emsp; 即一个对象的作用范围逃出了当前方法或者当前线程，有以下几种场景：  

    * 对象是一个静态变量
    * **对象作为当前方法的返回值**
    * 如果复写了类的finalize方法，则此类的实例对象都是全局逃逸状态。因此为了提高性能，除非万不得已，不要轻易复写finalize方法。  

2. 参数逃逸(ArgEscape)  
&emsp; **即一个对象被作为方法参数传递或者被参数引用，** 但在调用过程中不会再被其它方法或者线程访问。  
3. 没有逃逸(NoEscape)  
&emsp; 即方法中的对象没有发生逃逸，这种对象Java即时编译器会做出进一步的优化。  

### 1.2.2. 逃逸分析优化后的结果  
&emsp; 经过"逃逸分析"之后，如果一个对象的逃逸状态是GlobalEscape 或者ArgEscape，则此对象必须被分配在"堆"内存中， **<font color = "clime">但是对于NoEscape状态的对象，则不一定，具体会有这几种优化情况：对象可能分配在栈上、分离对象或标量替换、消除同步锁。</font>**  

#### 1.2.2.1. 对象可能分配在栈上  
##### 1.2.2.1.1. 介绍
&emsp; JVM通过逃逸分析，分析出新对象的使用范围，就可能将对象(局部变量)在栈上进行分配。栈分配可以快速地在栈帧上创建和销毁对象，不用再将对象分配到堆空间，可以有效地减少JVM垃圾回收的压力。  

&emsp; 参数：  
&emsp; 如果想开启栈上分配，需要同时启用逃逸分析(-XX:+DoEscapeAnalysis)和标量替换(-XX:+EliminateAllocations,默认开启)。如果关闭逃逸分析或标量替换中任意一个，栈上分配都不会生效。

##### 1.2.2.1.2. 示例
&emsp; 比如以下代码，在一个1千万次的循环中，分别创建EscapeTest对象 t1 和 t2。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-114.png)  
&emsp; 使用如下命令执行上述代码  

    java -Xms2g -Xmx2g -XX:+PrintGCDetails -XX:-DoEscapeAnalysis EscapeTest

&emsp; 通过参数 -XX:-DoEscapeAnalysis 关闭"逃逸分析"，然后代码会在 System.in.read() 处停住，此时使用 jps 和 jmap 命令查看内存中EscapeTest对象的详细情况，如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-115.png)  
&emsp; 可以看出，此时堆内存中有2千万个EscapeTest的实例对象(t1和t2各1千万个)，GC日志如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-116.png)  
&emsp; 没有发生GC回收事件，但是eden区已经占用96%，所有的EscapeTest对象都在"堆"中分配。  
&emsp; 如果将执行命令修改为如下：  

    java -Xms2g -Xmx2g -XX:+PrintGCDetails -XX:+DoEscapeAnalysis EscapeTest

&emsp; 将"逃逸分析"开关打开，并重新查看 EscapeTest 对象情况如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-117.png)  
&emsp; 可以看出此时堆内存中只有30万个左右，并且GC日志如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-118.png)  
&emsp; 没有发生GC回收时间，EscapeTest只占用eden区的8%，说明并没有在堆中创建 EscapeTest 对象，取而代之的是分配在"栈"中。  

&emsp; 注意：  
&emsp; 有的读者可能会有疑问：开启了"逃逸分析"，NoEscape状态的对象不是会在"栈"中分配吗？为什么这里还是会有30多万个对象在"堆"中分配？  
&emsp; 这是因为使用的JDK是混合模式，通过 java -version 查看java的版本，结果如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-119.png)  

    mixed mode 代表混合模式
    在Hotspot中采用的是解释器和编译器并行的架构，所谓的混合模式就是解释器和编译器搭配使用，当程序启动初期，采用解释器执行（同时会记录相关的数据，比如函数的调用次数，循环语句执行次数），节省编译的时间。在使用解释器执行期间，记录的函数运行的数据，通过这些数据发现某些代码是热点代码，采用编译器对热点代码进行编译，以及优化（逃逸分析就是其中一种优化技术）。


#### 1.2.2.2. 分离对象或标量替换  
&emsp; 上文中，提到当"逃逸分析"后，对象状态为NoEscape时会在"栈"中进行分配。但是实际上，这种说法并不是完全准确的，由于HotSpot虚拟机目前的实现方式导致栈上分配实现起来比较复杂，需要修改JVM中大量堆优先分配的代码，因此 **<font color = "clime">在HotSpot中并没有真正的实现"栈"中分配对象的功能，取而代之的是一个叫做"标量替换"的折中办法。</font>**  

##### 1.2.2.2.1. 介绍
&emsp; 什么是标量？标量，不可再分，基本数据类型；相对的是聚合量，可再分，引用类型。  
&emsp; **当JVM通过逃逸分析，确定要将对象分配到栈上时，即时编译可以将对象打散，将对象替换为一个个很小的局部变量，将这个打散的过程叫做标量替换。** 将对象替换为一个个局部变量后，就可以非常方便的在栈上进行分配了。  
<!-- 
首先要明白标量和聚合量，基础类型和对象的引用可以理解为标量，它们不能被进一步分解。而能被进一步分解的量就是聚合量，对象就是聚合量，它可以被进一步分解成标量，将其成员变量分解为分散的变量，这就叫做标量替换。

这样，如果一个对象没有发生逃逸，那压根就不需要在"堆"中创建它，只会在栈或者寄存器上创建一些能够映射这个对象标量即可，节省了内存空间，也提升了应用程序性能。
-->

##### 1.2.2.2.2. 示例
&emsp; 比如以下两个计算和的方法：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-120.png)  
&emsp; 乍看一下，sumPrimitive方法比 sumMutableWrapper 方法简单的多，那执行效率也肯定快许多吧，但是结果却是两个方法的执行效率相差无几。这是为什么呢？  
&emsp; 在 sumMutableWrapper 方法中，MutableWrapper是不可逃逸对象，也就是说没有必要再"堆"中创建真正的MutableWrapper对象，Java即时编译器会使用标量替换对其进行优化，优化结果为下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-121.png)  
&emsp; 仔细查看，上述优化够的代码中的value也是一个中间变量，通过内联之后，会被优化为如下：  

    total += i;

&emsp; 也即是说，java源代码中的一大坨在真正执行时，只有简单的一行操作。因此sumPrimitive和 sumMutableWrapper两个方法的执行效率基本一致。  

#### 1.2.2.3. 同步锁消除  
##### 1.2.2.3.1. 介绍
&emsp; **<font color = "red">如果JVM通过逃逸分析，发现一个对象(局部变量)只能从一个线程被访问到，则访问这个对象时，可以不加同步锁。</font>** 如果程序中使用了synchronized锁，则JVM会将synchronized锁消除。  
&emsp; 这里，需要注意的是： **<font color = "clime">这种情况针对的是synchronized锁，而对于Lock锁，则JVM并不能消除。</font>**  

&emsp; 参数：  
&emsp; 要开启同步消除，需要加上-XX:+EliminateLocks参数。因为这个参数依赖逃逸分析，所以同时要打开-XX:+DoEscapeAnalysis选项。  

##### 1.2.2.3.2. 示例
&emsp; 比如以下代码：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-109.png)  
&emsp; 在lockElimination() 方法中，对象 a 永远不会被其它方法或者线程访问到，因此 a 是非逃逸对象，这就导致synchronized(a) 没有任何意义，因为在任何线程中，a 都是不同的锁对象。所以JVM会对上述代码进行优化，删除同步相关代码，以下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-110.png)  
&emsp; 对于锁消除，还有一个比较经典的使用场景：StringBuffer。  
&emsp; StringBuffer是一个使用同步方法的线程安全的类，可以用来高效地拼接不可变的字符串对象。StringBuffer内部对所有append方法都进行了同步操作，如下所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-111.png)  
&emsp; 但是在平时开发中，有很多场景其实是不需要这层线程安全保障的，因此在Java 5中又引入了一个非同步的 StringBuilder 类来作为它的备选，StringBuilder中的 append 方法并没有使用synchronized标识，如下所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-112.png)  
&emsp; 调用StringBuffer的append方法的线程，必须得获取到这个对象的内部锁（也叫监视器锁）才能进入到方法内部，在退出方法前也必须要释放掉这个锁。而StringBuilder就不需要进行这个操作，因此它的执行性能比StringBuffer的要高--至少乍看上去是这样的。  
&emsp; 不过在HotSpot虚拟机引入了"逃逸分析"之后，在调用StringBuffer对象的同步方法时，就能够自动地把锁消除掉了。从而提高StringBuffer的性能，比如以下代码：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-113.png)  
&emsp; 在getString()方法中的StringBuffer是方法内部的局部变量，并且并没有被当做方法返回值返回给调用者，因此StringBuffer是一个"非逃逸(NoEscape)"对象。  
&emsp; 执行上述代码，结果如下：  

    java TestLockEliminate
    一共耗费：720 ms


## 1.3. 性能
&emsp; 关于逃逸分析的论文在1999年就已经发表了，但直到JDK 1.6才有实现，而且这项技术到如今也并不是十分成熟的。  
&emsp; 其根本原因就是无法保证逃逸分析的性能消耗一定能高于它的消耗。虽然经过逃逸分析可以做标量替换、栈上分配、和锁消除。但是逃逸分析自身也是需要进行一系列复杂的分析的，这其实也是一个相对耗时的过程。  
&emsp; 一个极端的例子，就是经过逃逸分析之后，发现没有一个对象是不逃逸的。那这个逃逸分析的过程就白白浪费掉了。  
&emsp; 虽然这项技术并不十分成熟，但是他也是即时编译器优化技术中一个十分重要的手段。  
