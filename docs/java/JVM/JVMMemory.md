

<!-- TOC -->

- [1. JVM内存结构/运行时数据区](#1-jvm内存结构运行时数据区)
    - [1.1. 程序计数器(Program Counter Register)](#11-程序计数器program-counter-register)
    - [1.2. JVM栈(stack)](#12-jvm栈stack)
        - [1.2.1. 栈存储内容详解](#121-栈存储内容详解)
    - [1.3. 本地方法栈](#13-本地方法栈)
    - [1.4. 堆(heap)](#14-堆heap)
        - [1.4.1. 堆简介](#141-堆简介)
        - [1.4.2. ★★★堆是分配对象存储的唯一选择吗？(逃逸分析)](#142-★★★堆是分配对象存储的唯一选择吗逃逸分析)
        - [1.4.3. Java堆内存配置项](#143-java堆内存配置项)
        - [1.4.4. 堆和栈的区别是什么？](#144-堆和栈的区别是什么)
        - [1.4.5. 堆和非堆内存](#145-堆和非堆内存)
    - [1.5. ~~方法区(永久代)~~](#15-方法区永久代)
        - [1.5.1. ~~运行时常量池~~](#151-运行时常量池)
    - [1.6. ~~元空间(直接内存)~~](#16-元空间直接内存)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-51.png)  

&emsp; **<font color = "red">总结：</font>**  

1. 运行时数据区。线程独享：程序计数器、JVM栈、本地方法栈；线程共享区：堆、方法区(元空间)。  
2. Java虚拟机栈是由一个个栈帧组成，每个栈帧中都拥有：局部变量表、操作数栈、动态链接、方法出口信息。局部变量表存储八大原始类型、对象引用、returnAddress。 
3. 堆  
&emsp; **<font color = "clime">在Eden区中，JVM为每个线程分配了一个私有缓存区域[TLAB(Thread Local Allocation Buffer)](/docs/java/JVM/MemoryObject.md)。</font>**    
&emsp; 堆是分配对象存储的唯一选择吗？[逃逸分析](/docs/java/JVM/escape.md)  
4. <font color = "clime">方法区的演进：</font>  
    * jdk1.6及之前：有永久代(permanent generation) ，静态变量存放在永久代上  
    * jdk1.7：有永久代，但已经逐步“去永久代”，<font color = "red">字符串常量池、静态变量</font>移除，保存在堆中。  
    * jdk1.8及之后：无永久代，类型信息、字段、方法、<font color = "red">常量</font>保存在本地内存的元空间，<font color = "clime">但字符串常量池、静态变量仍在堆。</font>  


# 1. JVM内存结构/运行时数据区  
<!--
 终于搞懂了Java8的内存结构，再也不纠结方法区和常量池了！ 
 https://mp.weixin.qq.com/s/WvdPQ8JsR9qqWMlvX7ockA
JAVA常量池，一篇文章就足够入门了。（含图解）
https://blog.csdn.net/qq_41376740/article/details/80338158
常量池 
https://baike.baidu.com/item/%E5%B8%B8%E9%87%8F%E6%B1%A0/3855836?fr=aladdin
Java中的几种常量池
https://blog.csdn.net/luzhensmart/article/details/86565496
常量池详解
https://zhuanlan.zhihu.com/p/64839455
JVM常量池浅析
https://www.jianshu.com/p/cf78e68e3a99
-->

<!--
Metaspace 之一：Metaspace整体介绍（永久代被替换原因、元空间特点、元空间内存查看分析方法）
https://www.cnblogs.com/duanxz/p/3520829.html

JVM 内存结构 
https://mp.weixin.qq.com/s/mWIsVIYkn7ts02mdmvRndA
https://mp.weixin.qq.com/s/jPIHNsQwiYNCRUQt1qXR6Q
-->
&emsp; **<font color = "red">部分参考《深入理解java虚拟机 第3版》第2章 Java内存区域与内存溢出异常</font>**   

&emsp; Java虚拟机在执行Java程序的过程中会把它管理的内存划分成若干个不同的数据区域。JDK1.8和之前的版本略有不同。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-7.png)  

&emsp; JVM内存区域主要分为线程私有区域【程序计数器、虚拟机栈、本地方法区】、线程共享区域【Java堆、方法区、直接内存】。  

* 线程私有数据区域生命周期与线程相同，依赖用户线程的启动/结束而创建/销毁（在 Hotspot VM内，每个线程都与操作系统的本地线程直接映射，因此这部分内存区域的存/否跟随系统本地线程的生/死对应）。  
* 线程共享区随虚拟机的启动/关闭而创建/销毁。  

## 1.1. 程序计数器(Program Counter Register)  
&emsp; <font color = "red">程序计数器是一块较小的内存空间，可以看作是当前线程所执行的字节码的行号指示器。</font>  
&emsp; PC 寄存器用来存储指向下一条指令的地址，即将要执行的指令代码。由执行引擎读取下一条指令。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-93.png)  
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
1. <font color = "red">JVM栈描述Java方法执行的内存模型。</font>Java虚拟机栈中出栈入栈的元素称为“栈帧”，栈对应线程，栈帧对应方法。每个方法被执行的时候，都会创建一个栈帧，把栈帧压人栈，当方法正常返回或者抛出未捕获的异常时，栈帧就会出栈。执行流程如下：  
&emsp; 示例代码：  

    ```java
    int main() {
        int a = 1;
        int ret = 0;
        int res = 0;
        ret = add(3, 5);
        res = a + ret;
        printf("%d", res);
        reuturn 0;
    }

    int add(int x, int y) {
        int sum = 0;
        sum = x + y;
        return sum;
    }
    ```
    &emsp; main()函数调用了add()函数，获取计算结果，并且与临时变量a相加，最后打印res的值。下图展示了在执行到add()函数时，函数调用栈的情况。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-8.png)  
2. 存储内容：将线程私有的不可能被其他线程访问的对象打散分配在栈上，而不是分配在堆上。打散分配意思是将对象的不同属性分别分配给不同的局部变量。  
&emsp; <font color = "red">Java虚拟机栈是由一个个栈帧组成，每个栈帧中都拥有：局部变量表、操作数栈、动态链接、方法出口信息。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-9.png)  
3. Java虚拟机栈是线程私有的。  
4. Java虚拟机栈会出现两种异常：StackOverFlowError和 OutOfMemoryError。  

### 1.2.1. 栈存储内容详解 
&emsp; **1. 局部变量表：**  
&emsp; 指存放方法参数和方法内部定义的局部变量的区域。局部变量表所需的内存空间在编译期间完成分配，当进入一个方法时，这个方法需要在帧中分配多大的局部变量空间是完全确定的，在方法运行期间不会改变局部变量表的大小。  
&emsp; 这里直接上代码，更好理解。  

```java
publicint test(int a, int b) {
    Object obj = newObject();
    return a + b;
}
```
&emsp; 如果局部变量是Java的8种基本数据类型，则存在局部变量表中，如果是引用类型。如new出来的String，局部变量表中存的是引用，而实例在堆中。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-34.png)  
&emsp; **2. 操作栈**  
&emsp; Java虚拟机的解释执行引擎称为“基于栈的执行引擎”，其中所指的“栈”就是操作数栈。当JVM为方法创建栈帧的时候，在栈帧中为方法创建一个操作数栈，保证方法内指令可以完成工作。  
&emsp; 还是用实操理解一下。  

```java
public class OperandStackTest {

    public int sum(int a, int b) {
        return a + b;
    }
}
```
&emsp; 编译生成 .class文件之后，再反汇编查看汇编指令  

```java
> javac OperandStackTest.java
> javap -v OperandStackTest.class> 1.txt
```

```java
public int sum(int, int);
descriptor: (II)I
flags: ACC_PUBLIC
Code:
    stack=2, locals=3, args_size=3 // 最大栈深度为2 局部变量个数为3
        0: iload_1 // 局部变量1 压栈
        1: iload_2 // 局部变量2 压栈
        2: iadd    // 栈顶两个元素相加，计算结果压栈
        3: ireturn
    LineNumberTable:
    line 10: 0
```
&emsp; **3. 动态连接**  
&emsp; 每个栈帧中包含一个在常量池中对当前方法的引用， 目的是支持方法调用过程的动态连接。  

&emsp; **4. 方法返回地址**  
&emsp; 方法执行时有两种退出情况：  
* 正常退出，即正常执行到任何方法的返回字节码指令，如 RETURN、 IRETURN、 ARETURN等  
* 异常退出  

&emsp; 无论何种退出情况，都将返回至方法当前被调用的位置。方法退出的过程相当于弹出当前栈帧，退出可能有三种方式：  
* 返回值压入上层调用栈帧  
* 异常信息抛给能够处理的栈帧  
* PC 计数器指向方法调用后的下一条指令  

<!-- 
1.1.2.3. 栈上分配举例   
......
https://mp.weixin.qq.com/s/Tv-0hjIgN9Grqvch1fFUiA -->

## 1.3. 本地方法栈  
&emsp; 本地方法栈与虚拟机栈作用相似。hotspot虚拟机中，虚拟机栈与本地方法栈是一体的。虚拟机栈为虚拟机执行Java方法服务；本地方法栈为虚拟机执行native方法服务。  
&emsp; 本地方法堆栈也会出现StackOverFlowError和OutOfMemoryError两种异常。  

## 1.4. 堆(heap)  
### 1.4.1. 堆简介  
&emsp; 存储内容：Java堆存储所有由new创建的对象(包括该对象其中的所有成员变量)和数组。  
&emsp; 堆中对象的内存需要等待GC进行回收。Java堆是垃圾收集器管理的主要区域，因此也被称作GC堆(Garbage Collected Heap)。  
&emsp; **<font color = "red">堆分类：从垃圾回收的角度，由于现在收集器基本都采用分代垃圾收集算法，所以Java堆还可以细分为：新生代和老年代。新生代内存又被分成三部分，Eden、From Survivor、To Survivor，默认情况下年轻代按照8 :1 :1的比例来分配。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-10.png)  

&emsp; **<font color = "clime">在Eden区中，JVM为每个线程分配了一个私有缓存区域[TLAB(Thread Local Allocation Buffer)](/docs/java/JVM/MemoryObject.md)。</font>**    

### 1.4.2. ★★★堆是分配对象存储的唯一选择吗？(逃逸分析)  
&emsp; 请参考[逃逸分析](/docs/java/JVM/escape.md)  

### 1.4.3. Java堆内存配置项  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-11.png)  
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
&emsp; JDK1.8以前的HotSpot JVM有方法区，也叫永久代(permanent generation)。  
&emsp; 存储内容：方法区用于存放已被虚拟机加载的类信息、常量、静态变量、即时编译器(JIT)编译后的代码等数据。  

<!-- 
&emsp; **<font color = "lime">JDK1.7开始了方法区的部分移除：符号引用(Symbols)移至native heap，字面量(interned strings)和静态变量(class statics)移至java heap。从JDK 1.8开始，移除永久代，并把方法区(主要是类型信息)移至元空间，它位于本地内存中，而不是虚拟机内存中。</font>**   
-->

&emsp; <font color = "clime">方法区的演进：</font>  

* jdk1.6及之前：有永久代(permanent generation) ，静态变量存放在永久代上  
* jdk1.7：有永久代，但已经逐步“去永久代”，<font color = "red">字符串常量池、静态变量</font>移除，保存在堆中。  
* jdk1.8及之后：无永久代，类型信息、字段、方法、<font color = "red">常量</font>保存在本地内存的元空间，<font color = "clime">但字符串常量池、静态变量仍在堆。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-68.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-69.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-70.png)  

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

## 1.6. ~~元空间(直接内存)~~  
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
