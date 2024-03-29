

<!-- TOC -->

- [1. JVM内存结构/运行时数据区](#1-jvm内存结构运行时数据区)
    - [1.1. 程序计数器(Program Counter Register)](#11-程序计数器program-counter-register)
    - [1.2. JVM栈(stack)](#12-jvm栈stack)
    - [1.3. 本地方法栈](#13-本地方法栈)
    - [1.4. 堆(heap)](#14-堆heap)
        - [1.4.1. 堆简介](#141-堆简介)
        - [1.4.2. ★★★堆是分配对象存储的唯一选择吗？(逃逸分析)](#142-★★★堆是分配对象存储的唯一选择吗逃逸分析)
        - [1.4.3. Java堆内存配置项](#143-java堆内存配置项)
        - [1.4.4. 堆和栈的区别是什么？](#144-堆和栈的区别是什么)
        - [1.4.5. 堆和非堆内存](#145-堆和非堆内存)
    - [1.5. ~~方法区(永久代)~~](#15-方法区永久代)
        - [1.5.1. ~~运行时常量池~~](#151-运行时常量池)
    - [1.6. ~~元空间(不是直接内存)~~](#16-元空间不是直接内存)
    - [1.7. 直接内存（非JVM控制）](#17-直接内存非jvm控制)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. &emsp; **<font color = "red">小结：</font>**  运行时数据区。【线程独享】：程序计数器、JVM栈、本地方法栈；【线程共享区】：堆、方法区（元空间）。  
2. 程序计数器看作是当前线程所执行的字节码的行号指示器。  
3. <font color = "red">JVM栈描述Java方法执行的内存模型。</font>Java虚拟机栈中出栈入栈的元素称为“栈帧”，栈对应线程，栈帧对应方法。每个方法被执行的时候，都会创建一个栈帧，把栈帧压入栈，当方法正常返回或者抛出未捕获的异常时，栈帧就会出栈。    
&emsp; Java虚拟机栈是由一个个栈帧组成，每个栈帧中都拥有：局部变量表、操作数栈、动态链接、方法出口信息。局部变量表存储八大原始类型、对象引用、returnAddress。栈帧存储方法需要的数据。  
&emsp; `为什么不把基本类型放堆中呢？`   
&emsp; 因为其占用的空间一般是 1~8 个字节——需要空间比较少，而且因为是基本类型，所以不会出现动态增长的情况——长度固定，因此栈中存储就够了。  
4. 堆  
    1. 堆分为新生代、老年代，默认比例1: 2。 新生代又按照8: 1: 1划分为Eden区和两个Survivor区。  
    2. **<font color = "blue">在Eden区中，JVM为每个线程分配了一个私有缓存区域[TLAB(Thread Local Allocation Buffer)](/docs/java/JVM/MemoryObject.md)。</font>**    
    3. 堆是分配对象存储的唯一选择吗？[逃逸分析](/docs/java/JVM/escape.md)  
5. 方法区：
    1. 在类加载阶段，在Java堆中生成一个代表这个类的java.lang.Class对象，作为对方法区中这些数据的访问入口。  
    2. <font color = "clime">方法区的演进：</font>  
        1. 为什么JDK1.8移除永久代  
            1. 由于PermGen内存经常会溢出，引发java.lang.OutOfMemoryError: PermGen，因此JVM的开发者希望这一块内存可以更灵活地被管理，不要再经常出现这样的OOM。  
            2. 移除PermGen可以促进HotSpot JVM与JRockit VM的融合，因为JRockit没有永久代。  
        2. 演进历程：  
            * jdk1.6及之前：有永久代(permanent generation)。静态变量存放在永久代上。  
            * jdk1.7：有永久代，但已经逐步“去永久代”。[字符串常量池](/docs/java/JVM/ConstantPool.md) <font color = "red">、静态变量</font>移除，保存在堆中。  
            * jdk1.8及之后：无永久代。类型信息、字段、方法、<font color = "red">常量</font>保存在本地内存的元空间，<font color = "clime">但字符串常量池、静态变量仍在堆。</font>  
6. MetaSpace存储类的元数据信息。  
&emsp; 元空间与永久代之间最大的区别在于：元数据空间并不在虚拟机中，而是使用本地内存。元空间的内存大小受本地内存限制。  
7. 直接内存（非JVM控制）  
&emsp; 直接内存的大小并不受到java堆大小的限制，甚至不受到JVM进程内存大小的限制。它只受限于本机总内存（RAM及SWAP区或者分页文件）大小以及处理器寻址空间的限制（最常见的就是32位/64位CPU的最大寻址空间限制不同）。  
&emsp; DirectByteBuffer使用直接内存的原因有两点：  
&emsp; 1） 这块内存真正的分配并不在 Java 堆中，堆中只有一个很小的对象引用，这种方式能减轻 GC 压力  
&emsp; 2） 对于堆内对象，进行IO操作（Socket、文件读写）时需要先把对象复制一份到堆外内存再写入 Socket 或者文件，而当 DirectByteBuffer 就在堆外分配内存时可以省掉一次从堆内拷贝到堆外的操作，减少用户态到内核态的操作，性能表现会更好  
&emsp; 直接内存的回收：需注意堆外内存并不直接控制于JVM，这些内存只有在DirectByteBuffer回收掉之后才有机会被回收，而 Young GC 的时候只会将年轻代里不可达的DirectByteBuffer对象及其直接内存回收，如果这些对象大部分都晋升到了年老代，那么只能等到Full GC的时候才能彻底地回收DirectByteBuffer对象及其关联的堆外内存。因此，堆外内存的回收依赖于 Full GC




# 1. JVM内存结构/运行时数据区  
<!--

 JVM 方法区的理解
 https://mp.weixin.qq.com/s/u-7Oj_BNmvQsul-dEdxRog

元空间与直接内存的关系
https://blog.csdn.net/Ethan_199402/article/details/110431404

-->
&emsp; **<font color = "red">部分参考《深入理解java虚拟机 第3版》第2章 Java内存区域与内存溢出异常</font>**   

&emsp; Java虚拟机在执行Java程序的过程中会把它管理的内存划分成若干个不同的数据区域。JDK1.8和之前的版本略有不同。  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-7.png)  

&emsp; JVM内存区域主要分为线程私有区域【程序计数器、虚拟机栈、本地方法区】、线程共享区域【Java堆、方法区、直接内存】。  

* 线程私有数据区域生命周期与线程相同，依赖用户线程的启动/结束而创建/销毁（在 Hotspot VM内，每个线程都与操作系统的本地线程直接映射，因此这部分内存区域的存/否跟随系统本地线程的生/死对应）。  
* 线程共享区随虚拟机的启动/关闭而创建/销毁。  

## 1.1. 程序计数器(Program Counter Register)  
&emsp; <font color = "red">程序计数器是一块较小的内存空间，可以看作是当前线程所执行的字节码的行号指示器。</font>  
&emsp; PC 寄存器用来存储指向下一条指令的地址，即将要执行的指令代码。由执行引擎读取下一条指令。  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-93.png)  
&emsp; (分析：进入class文件所在目录，执行javap -v xx.class反解析(或者通过IDEA插件Jclasslib直接查看，上图)，可以看到当前类对应的Code区(汇编指令)、本地变量表、异常表和代码行偏移量映射表、常量池等信息。)  

1. 存储内容：  
&emsp; 如果线程执行的是java方法，这个计数器记录的是正在执行的虚拟字节码指令的地址。  
&emsp; 如果线程执行的是native方法，那么这个计数器的值为undefined。  
2. 主要有两个作用：  

    * 字节码解释器通过改变程序计数器来依次读取指令，从而实现代码的流程控制，如：顺序执行、选择、循环、异常处理。  
    * 在多线程的情况下，程序计数器用于记录当前线程执行的位置，从而当线程被切换回来的时候能够知道该线程上次运行到哪儿了。  

3. 为了线程切换后能恢复到正确的执行位置，每条线程都需要有一个独立的程序计数器，各线程之间计数器互不影响，独立存储，因此这类内存区域为“线程私有”的内存。  
4. 程序计数器是唯一一个不会出现OutOfMemoryError的内存区域，它的生命周期随着线程的创建而创建，随着线程的结束而死亡。 

## 1.2. JVM栈(stack)  



## 1.3. 本地方法栈  
&emsp; 本地方法栈与虚拟机栈作用相似。hotspot虚拟机中，虚拟机栈与本地方法栈是一体的。虚拟机栈为虚拟机执行Java方法服务；本地方法栈为虚拟机执行native方法服务。  
&emsp; 本地方法堆栈也会出现StackOverFlowError和OutOfMemoryError两种异常。  

## 1.4. 堆(heap)  
### 1.4.1. 堆简介  
&emsp; 存储内容：Java堆存储所有由new创建的对象(包括该对象其中的所有成员变量)和数组。  
&emsp; 堆中对象的内存需要等待GC进行回收。Java堆是垃圾收集器管理的主要区域，因此也被称作GC堆(Garbage Collected Heap)。  
&emsp; **<font color = "red">堆分类：从垃圾回收的角度，由于现在收集器基本都采用分代垃圾收集算法，所以Java堆还可以细分为：新生代和老年代，默认1: 2。新生代内存又被分成三部分，Eden、From Survivor、To Survivor，默认情况下年轻代按照8 :1 :1的比例来分配。</font>**  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-10.png)  

&emsp; **<font color = "clime">在Eden区中，JVM为每个线程分配了一个私有缓存区域[TLAB(Thread Local Allocation Buffer)](/docs/java/JVM/MemoryObject.md)。</font>**    

### 1.4.2. ★★★堆是分配对象存储的唯一选择吗？(逃逸分析)  
&emsp; 请参考[逃逸分析](/docs/java/JVM/escape.md)  

### 1.4.3. Java堆内存配置项  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-11.png)  
&emsp; **堆内存扩展：通过 -Xmx -Xms 控制。**  
&emsp; 没有直接设置老年代的参数，但是可以设置堆空间大小和新生代空间大小两个参数来间接控制。老年代空间大小=堆空间大小-年轻代大空间大小。  

|VM Switch|描述|
|---|---|
|-Xms|用于在JVM启动时设置初始堆大小|
|-Xmx|用于设置最大堆大小|
|-Xmn|设置新生区的大小，剩下的空间用于老年区|
|-XX：PermGen|用于设置永久区存初始大小|
|-XX：MaxPermGen|用于设置Perm Gen的最大尺寸|
|XX：SurvivorRatio|提供Eden区域的比例|
|XX：NewRatio|用于提供老年代/新生代大小的比例，默认值为2|

&emsp; 32位JVM和64位JVM的最大堆内存分别是多少？  
&emsp; 理论上说上32位的JVM堆内存可以到达2^32，即4GB，但实际上会比这个小很多。不同操作系统之间不同，如Windows系统大约1.5GB，Solaris大约 3GB。64位JVM允许指定最大的堆内存，理论上可以达到2^64，这是一个非常大的数字，实际上可以指定堆内存大小到100GB。  
&emsp; **一般建议堆的最大值设置为可用内存的最大值的80%。**  

### 1.4.4. 堆和栈的区别是什么？  
&emsp; 堆和栈(虚拟机栈)是完全不同的两块内存区域，一个是线程独享的，一个是线程共享的。二者之间最大的区别就是存储的内容不同：堆中主要存放对象实例；栈(局部变量表)中主要存放各种基本数据类型、对象的引用。一个对象的大小是不可估计的，或者说是可以动态变化的，但是在栈中，一个对象只对应了一个4btye的引用(堆栈分离的好处)。  
&emsp; 从作用来说，栈是运行时的单位，而堆是存储的单位。栈解决程序的运行问题，即程序如何执行，或者说如何处理数据。堆解决的是数据存储的问题，即数据怎么放、放在哪儿。在Java中一个线程就会相应有一个线程栈与之对应，因为不同的线程执行逻辑有所不同，因此需要一个独立的线程栈。而堆则是所有线程共享的。栈因为是运行单位，因此里面存储的信息都是跟当前线程(或程序)相关信息的。包括局部变量、程序运行状态、方法返回值等等；而堆只负责存储对象信息。  
&emsp; 堆的优势是可以动态地分配内存空间，需要多少内存空间不必事先告诉编译器，因为它是在运行时动态分配的。但缺点是，由于需要在运行时动态分配内存，所以存取速度较慢。  
&emsp; 栈的优势是存取速度比堆快。但缺点是，存放在栈中的数据占用多少内存空间需要在编译时确定下来，缺乏灵活性。  

### 1.4.5. 堆和非堆内存  
&emsp; JVM主要管理两种类型的内存：堆和非堆。简单来说堆就是Java代码可及的内存，是留给开发人员使用的；非堆就是JVM留给自己用的。所以方法区、JVM内部处理或优化所需的内存(如JIT编译后的代码缓存)、每个类结构(如运行时常数池、字段和方法数据)以及方法和构造方法的代码都在非堆内存中。  

## 1.5. ~~方法区(永久代)~~  
<!-- 

说下你对方法区演变过程和内部结构的理解
https://mp.weixin.qq.com/s/n4w_qQxHp6d2TB_d5FC5LA
-->

&emsp; JDK1.8以前的HotSpot JVM有方法区，也叫永久代(permanent generation)。  
&emsp; 存储内容：方法区用于存放已被虚拟机加载的类信息、常量、静态变量、即时编译器(JIT)编译后的代码等数据。  

<!-- 
&emsp; **<font color = "lime">JDK1.7开始了方法区的部分移除：符号引用(Symbols)移至native heap，字面量(interned strings)和静态变量(class statics)移至java heap。从JDK 1.8开始，移除永久代，并把方法区(主要是类型信息)移至元空间，它位于本地内存中，而不是虚拟机内存中。</font>**   
-->

&emsp; <font color = "clime">方法区的演进：</font>  

* jdk1.6及之前：有永久代(permanent generation) 。静态变量存放在永久代上  
* jdk1.7：有永久代，但已经逐步“去永久代”。[字符串常量池](/docs/java/JVM/ConstantPool.md) <font color = "red">、静态变量</font>移除，保存在堆中。  
* jdk1.8及之后：无永久代。类型信息、字段、方法、<font color = "red">常量</font>保存在本地内存的元空间，<font color = "clime">但字符串常量池、静态变量仍在堆。</font>  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-68.png)  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-69.png)  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-70.png)  

&emsp; <font color = "red">Java 8中PermGen为什么被移出HotSpot JVM了？</font>(详见：JEP 122: Remove the Permanent Generation)：  
1. 由于PermGen内存经常会溢出，引发java.lang.OutOfMemoryError: PermGen，因此JVM的开发者希望这一块内存可以更灵活地被管理，不要再经常出现这样的OOM。  
2. 移除PermGen可以促进HotSpot JVM与JRockit VM的融合，因为JRockit没有永久代。  

<!-- 
1. 为永久代设置空间大小是很难确定的。 在某些场景下，如果动态加载类过多，容易产生Perm区的O0M。比如某个实际Web工程中，因为功能点比较多，在运行过程中，要不断动态加载很多类，经常出现致命错误。 "Exception in thread' dubbo client x.x connector’java.lang.OutOfMemoryError： PermGenspace" 而元空间和永久代之间最大的区别在于：元空间并不在虚拟机中，而是使用本地内存。因此，默认情况下，元空间的大小仅受本地内存限制

2. 对永久代进行调优是很困难的
-->

&emsp; <font color = "red">~~StringTable 为什么要调整?~~</font>   
&emsp; jdk7中将StringTable放到了堆空间中。因为永久代的回收效率很低，在Full GC的时候才会触发，而Full GC是老年代的空间不足、永久代不足时才会触发，这就导致了StringTable回收效率不高。而<font color = "red">开发中会有大量的字符串被创建，回收效率低，导致永久代内存不足，放到堆里，能及时回收内存。</font>   

&emsp; 永久代的GC是和老年代(old generation)捆绑在一起的，无论谁满了，都会触发永久代和老年代的垃圾收集。  

### 1.5.1. ~~运行时常量池~~  
<!-- 
延伸：常量池 
https://mp.weixin.qq.com/s/391mANG6x1euW2Savj7ltA
&emsp; 常量池分为三种：class 文件中的常量池、运行时常量池、字符串常量池。
-->
&emsp; <font color = "red">运行时常量池：是方法区的一部分，用于存放编译器生成的各种字面量和符号引用。</font>一般来说，除了保存 Class 文件中描述的符号引用外，还会把翻译出来的直接引用也存储在运行时常量池中。  
&emsp; 运行时常量池相对于 Class 文件常量池的另外一个重要特征是具备动态性，Java语言并不要求常量一定只有编译期才能产生，也就是并非预置入Class文件中常量池的内容才能进入方法区运行时常量池，运行期间也可能将新的常量放入池中，这种特性被开发人员利用得比较多的便是String类的 intern()方法。  
&emsp; 既然运行时常量池是方法区的一部分，自然受到方法区内存的限制，当常量池无法再申请到内存时会抛出 OutOfMemoryError 异常。 

&emsp; <font color = "red">JDK1.7及之后版本的JVM已经将运行时常量池从方法区中移了出来，在 Java堆(Heap)中开辟了一块区域存放运行时常量池。</font>  

## 1.6. ~~元空间(不是直接内存)~~  
<!-- 
https://blog.csdn.net/qq_33591903/article/details/105634782
https://www.cnblogs.com/duanxz/p/3520829.html
-->
&emsp; JDK1.8版本中移除了方法区并使用 MetaSpace(元空间)作为替代实现。  
&emsp; MetaSpace存储类的元数据信息。  
&emsp; 元空间与永久代之间最大的区别在于：元数据空间并不在虚拟机中，而是使用本地内存。元空间的内存大小受本地内存限制。  
&emsp; 元空间也可能导致OutOfMemoryError异常出现。  

&emsp; **<font color = "red">~~为什么要用元空间替代方法区？~~</font>**  
1. 整个永久代有一个 JVM 本身设置固定大小上线，无法进行调整。字符串常量池存在于永久代中，在大量使用字符串的情况下，非常容易出现OOM的异常。此外，JVM加载的class的总数，方法的大小等都很难确定，因此对永久代大小的指定难以确定。太小的永久代容易导致永久代内存溢出，太大的永久代则容易导致虚拟机内存紧张。  
&emsp; <font color = "red">而元空间使用的是直接内存，受本机可用内存的限制，很难发生java.lang.OutOfMemoryError。</font>也可以通过JVM参数来指定元空间的大小。  


## 1.7. 直接内存（非JVM控制）  
<!-- 

元空间与直接内存的关系
https://blog.csdn.net/Ethan_199402/article/details/110431404
-->


