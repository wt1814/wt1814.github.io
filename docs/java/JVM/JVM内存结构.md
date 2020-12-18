

<!-- TOC -->

- [1. JVM内存结构](#1-jvm内存结构)
    - [1.1. JVM运行时数据区](#11-jvm运行时数据区)
        - [1.1.1. 程序计数器(Program Counter Register)](#111-程序计数器program-counter-register)
        - [1.1.2. JVM栈(stack)](#112-jvm栈stack)
            - [1.1.2.1. 栈存储内容详解](#1121-栈存储内容详解)
        - [1.1.3. 本地方法栈](#113-本地方法栈)
        - [1.1.4. 堆(heap)](#114-堆heap)
            - [1.1.4.1. 堆是分配对象存储的唯一选择吗？（逃逸分析）](#1141-堆是分配对象存储的唯一选择吗逃逸分析)
            - [1.1.4.2. Java堆内存配置项](#1142-java堆内存配置项)
            - [1.1.4.3. 堆和栈的区别是什么？](#1143-堆和栈的区别是什么)
            - [1.1.4.4. 堆和非堆内存](#1144-堆和非堆内存)
        - [1.1.5. ~~方法区（永久代）~~](#115-方法区永久代)
            - [1.1.5.1. ~~运行常量池~~](#1151-运行常量池)
        - [1.1.6. 元空间（直接内存）](#116-元空间直接内存)
    - [1.2. 内存中对象](#12-内存中对象)
        - [1.2.1. 对象的创建过程](#121-对象的创建过程)
            - [1.2.1.1. TLAB](#1211-tlab)
        - [1.2.2. 对象的内存布局](#122-对象的内存布局)
        - [1.2.3. 对象的访问定位](#123-对象的访问定位)
        - [1.2.4. 堆内存分配策略](#124-堆内存分配策略)
            - [1.2.4.1. 对象优先在Eden分配](#1241-对象优先在eden分配)
            - [1.2.4.2. 大对象直接进入老年代](#1242-大对象直接进入老年代)
            - [1.2.4.3. 长期存活的对象将进入老年代](#1243-长期存活的对象将进入老年代)
            - [1.2.4.4. 动态对象年龄判定](#1244-动态对象年龄判定)
            - [1.2.4.5. 空间分配担保](#1245-空间分配担保)
    - [1.3. 内存泄漏、内存溢出](#13-内存泄漏内存溢出)
        - [1.3.1. 内存溢出演示-1](#131-内存溢出演示-1)
        - [内存泄露场景](#内存泄露场景)
        - [1.3.2. 内存溢出问题详解](#132-内存溢出问题详解)
    - [1.4. 延伸：常量池](#14-延伸常量池)

<!-- /TOC -->

&emsp; **<font color = "red">部分参考《深入理解java虚拟机 第3版》第2章 Java内存区域与内存溢出异常</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-51.png)  

<!-- 
 生产实践经验：线上系统的 JVM 内存是越大越好吗？ 
 https://mp.weixin.qq.com/s/RtSJU0oHnUZqADLQe3u_ZQ

靠13 张图解 Java 中的内存模型入职快手！
https://mp.weixin.qq.com/s/FkWCx9XhpjSOj8gojMSoPw

Java中的对象和数组都是在堆上分配的吗？ 
https://mp.weixin.qq.com/s/FbWOodOs3jw3CYotpX46BA
https://mp.weixin.qq.com/s/nOKXc4FDHz7FjfTiH1FVyA


JVM 内存结构 
https://mp.weixin.qq.com/s/mWIsVIYkn7ts02mdmvRndA

https://mp.weixin.qq.com/s/WVGZIBXsIVYPMfhkqToh_Q

https://mp.weixin.qq.com/s/nzEt7_FyOUMPY5wOJ-EeKg
-->

# 1. JVM内存结构  

&emsp; **<font color = "lime">1. 检测类是否被加载 2. 为对象分配内存 3. 为分配的内存空间初始化零值 4. 对对象进行其他设置 5.执行init方法。</font>**    

## 1.1. JVM运行时数据区  
&emsp; Java虚拟机在执行Java程序的过程中会把它管理的内存划分成若干个不同的数据区域。JDK1.8和之前的版本略有不同。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-7.png)  

&emsp; JVM 内存区域主要分为线程私有区域【程序计数器、虚拟机栈、本地方法区】、线程共享区域【Java 堆、方法区、直接内存】。  

* 线程私有数据区域生命周期与线程相同，依赖用户线程的启动/结束而创建/销毁（在 Hotspot VM 内，每个线程都与操作系统的本地线程直接映射，因此这部分内存区域的存/否跟随系统本地线程的生/死对应）。  
* 线程共享区随虚拟机的启动/关闭而创建/销毁。  

### 1.1.1. 程序计数器(Program Counter Register)  
&emsp; <font color = "red">程序计数器是一块较小的内存空间，可以看作是当前线程所执行的字节码的行号指示器。</font>  
1. 存储内容：  
&emsp; 如果线程执行的是java方法，这个计数器记录的是正在执行的虚拟字节码指令的地址。  
&emsp; 如果线程执行的是native方法，那么这个计数器的值为undefined。  
2. 主要有两个作用：  

    * 字节码解释器通过改变程序计数器来依次读取指令，从而实现代码的流程控制，如：顺序执行、选择、循环、异常处理。  
    * 在多线程的情况下，程序计数器用于记录当前线程执行的位置，从而当线程被切换回来的时候能够知道该线程上次运行到哪儿了。  

3. 为了线程切换后能恢复到正确的执行位置，每条线程都需要有一个独立的程序计数器，各线程之间计数器互不影响，独立存储，因此这类内存区域为“线程私有”的内存。  
4. 程序计数器是唯一一个不会出现OutOfMemoryError的内存区域，它的生命周期随着线程的创建而创建，随着线程的结束而死亡。 

### 1.1.2. JVM栈(stack)  
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

#### 1.1.2.1. 栈存储内容详解 
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



### 1.1.3. 本地方法栈  
&emsp; 本地方法栈与虚拟机栈作用相似。hotspot虚拟机中，虚拟机栈与本地方法栈是一体的。虚拟机栈为虚拟机执行Java方法服务；本地方法栈为虚拟机执行native方法服务。  
&emsp; 本地方法堆栈也会出现StackOverFlowError和OutOfMemoryError两种异常。  

### 1.1.4. 堆(heap)  
&emsp; 存储内容：Java堆存储所有由new创建的对象（包括该对象其中的所有成员变量）和数组。  
&emsp; 堆中对象的内存需要等待GC进行回收。Java堆是垃圾收集器管理的主要区域，因此也被称作GC堆（Garbage Collected Heap）。  
&emsp; **<font color = "red">堆分类：从垃圾回收的角度，由于现在收集器基本都采用分代垃圾收集算法，所以Java堆还可以细分为：新生代和老年代。新生代内存又被分成三部分，Eden、From Survivor、To Survivor，默认情况下年轻代按照8 :1 :1的比例来分配。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-10.png)  

#### 1.1.4.1. 堆是分配对象存储的唯一选择吗？（逃逸分析）  
<!-- 
https://mp.weixin.qq.com/s/BUcFh9ArENu9rlMdmlDNWg
https://mp.weixin.qq.com/s/jPIHNsQwiYNCRUQt1qXR6Q
-->  
&emsp; 通过逃逸分析算法可以分析出某一个方法中的某个对象是否会被其它方法或者线程访问到。如果分析结果显示某对象并不会被其它线程访问，则有可能在编译期间将对象分配在栈上，即时是线程私有的。    
&emsp; <font color = "red">逃逸指的是逃出当前线程，所以逃逸对象就是可以被其他线程访问的对象，非逃逸对象就是线程私有的对象。</font>  

&emsp; 举例：逃逸对象和非逃逸对象  

```
public class StackTest {
    public static User user1;

    public static void runAway1() {
        user1 = new User();// 逃逸对象
    }

    public static void runAway2() {
        User user2 = new User();// 非逃逸对象
    }
}
```

#### 1.1.4.2. Java堆内存配置项  
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

#### 1.1.4.3. 堆和栈的区别是什么？  
&emsp; 堆和栈（虚拟机栈）是完全不同的两块内存区域，一个是线程独享的，一个是线程共享的。二者之间最大的区别就是存储的内容不同：堆中主要存放对象实例；栈（局部变量表）中主要存放各种基本数据类型、对象的引用。一个对象的大小是不可估计的，或者说是可以动态变化的，但是在栈中，一个对象只对应了一个4btye的引用（堆栈分离的好处）。  
&emsp; 从作用来说，栈是运行时的单位，而堆是存储的单位。栈解决程序的运行问题，即程序如何执行，或者说如何处理数据。堆解决的是数据存储的问题，即数据怎么放、放在哪儿。在Java中一个线程就会相应有一个线程栈与之对应，因为不同的线程执行逻辑有所不同，因此需要一个独立的线程栈。而堆则是所有线程共享的。栈因为是运行单位，因此里面存储的信息都是跟当前线程（或程序）相关信息的。包括局部变量、程序运行状态、方法返回值等等；而堆只负责存储对象信息。  
&emsp; 堆的优势是可以动态地分配内存空间，需要多少内存空间不必事先告诉编译器，因为它是在运行时动态分配的。但缺点是，由于需要在运行时动态分配内存，所以存取速度较慢。  
&emsp; 栈的优势是存取速度比堆快。但缺点是，存放在栈中的数据占用多少内存空间需要在编译时确定下来，缺乏灵活性。  

#### 1.1.4.4. 堆和非堆内存  
&emsp; JVM主要管理两种类型的内存：堆和非堆。简单来说堆就是Java代码可及的内存，是留给开发人员使用的；非堆就是JVM留给自己用的。所以方法区、JVM内部处理或优化所需的内存(如JIT编译后的代码缓存)、每个类结构(如运行时常数池、字段和方法数据)以及方法和构造方法的代码都在非堆内存中。  

### 1.1.5. ~~方法区（永久代）~~  
&emsp; JDK1.8以前的HotSpot JVM有方法区，也叫永久代(permanent generation)。  
&emsp; 存储内容：方法区用于存放已被虚拟机加载的类信息、常量、静态变量、即时编译器（JIT）编译后的代码等数据。  

<!-- 
&emsp; **<font color = "lime">JDK1.7开始了方法区的部分移除：符号引用(Symbols)移至native heap，字面量(interned strings)和静态变量(class statics)移至java heap。从JDK 1.8开始，移除永久代，并把方法区（主要是类型信息）移至元空间，它位于本地内存中，而不是虚拟机内存中。</font>**   
-->

&emsp; <font color = "lime">方法区的演进：</font>  

* jdk1.6及之前：有永久代（permanent generation） ，静态变量存放在永久代上  
* jdk1.7：有永久代，但已经逐步“去永久代”，<font color = "red">字符串常量池、静态变量</font>移除，保存在堆中。  
* jdk1.8及之后：无永久代，类型信息、字段、方法、<font color = "red">常量</font>保存在本地内存的元空间，<font color = "lime">但字符串常量池、静态变量仍在堆。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-68.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-69.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-70.png)  

&emsp; <font color = "red">Java 8 中 PermGen 为什么被移出 HotSpot JVM 了？</font>（详见：JEP 122: Remove the Permanent Generation）：  
1. 由于 PermGen 内存经常会溢出，引发java.lang.OutOfMemoryError: PermGen，因此 JVM 的开发者希望这一块内存可以更灵活地被管理，不要再经常出现这样的 OOM。  
2. 移除 PermGen 可以促进 HotSpot JVM 与 JRockit VM 的融合，因为 JRockit 没有永久代。  

<!-- 
1. 为永久代设置空间大小是很难确定的。 在某些场景下，如果动态加载类过多，容易产生Perm区的O0M。比如某个实际Web工程中，因为功能点比较多，在运行过程中，要不断动态加载很多类，经常出现致命错误。 "Exception in thread' dubbo client x.x connector’java.lang.OutOfMemoryError： PermGenspace" 而元空间和永久代之间最大的区别在于：元空间并不在虚拟机中，而是使用本地内存。因此，默认情况下，元空间的大小仅受本地内存限制

2. 对永久代进行调优是很困难的
-->

&emsp; <font color = "red">StringTable 为什么要调整?</font>   
&emsp; jdk7中将StringTable放到了堆空间中。因为永久代的回收效率很低，在Full GC的时候才会触发，而Full GC 是老年代的空间不足、永久代不足时才会触发，这就导致了StringTable回收效率不高。而<font color = "red">开发中会有大量的字符串被创建，回收效率低，导致永久代内存不足，放到堆里，能及时回收内存。</font>   

&emsp; 永久代的GC是和老年代(old generation)捆绑在一起的，无论谁满了，都会触发永久代和老年代的垃圾收集。  

#### 1.1.5.1. ~~运行常量池~~  
&emsp; <font color = "red">运行时常量池：是方法区的一部分，用于存放编译器生成的各种字面量和符号引用。</font>一般来说，除了保存 Class 文件中描述的符号引用外，还会把翻译出来的直接引用也存储在运行时常量池中。  
&emsp; 运行时常量池相对于 Class 文件常量池的另外一个重要特征是具备动态性，Java 语言并不要求常量一定只有编译期才能产生，也就是并非预置入Class文件中常量池的内容才能进入方法区运行时常量池，运行期间也可能将新的常量放入池中，这种特性被开发人员利用得比较多的便是String类的 intern()方法。  
&emsp; 既然运行时常量池是方法区的一部分，自然受到方法区内存的限制，当常量池无法再申请到内存时会抛出 OutOfMemoryError 异常。 

&emsp; <font color = "red">JDK1.7及之后版本的JVM已经将运行时常量池从方法区中移了出来，在 Java堆（Heap）中开辟了一块区域存放运行时常量池。</font>  

### 1.1.6. 元空间（直接内存）  
&emsp; JDK1.8 版本中移除了方法区并使用 MetaSpace（元空间）作为替代实现。  
&emsp; MetaSpace 存储类的元数据信息。  
&emsp; 元空间与永久代之间最大的区别在于：元数据空间并不在虚拟机中，而是使用本地内存。元空间的内存大小受本地内存限制。  
&emsp; 元空间也可能导致OutOfMemoryError异常出现。  

&emsp; **<font color = "red">为什么要用元空间替代方法区？</font>**  
1. <font color = "red">因为直接内存，JVM将会在 IO 操作上具有更高的性能，因为它直接作用于本地系统的 IO 操作。</font>而非直接内存，也就是堆内存中的数据，如果要作 IO 操作，会先复制到直接内存，再利用本地 IO 处理。  
&emsp; 从数据流的角度，非直接内存是下面这样的作用链：本地 IO --> 直接内存 --> 非直接内存 --> 直接内存 --> 本地 IO。  
&emsp; 而直接内存是：本地 IO --> 直接内存 --> 本地 IO。  
2. 整个永久代有一个 JVM 本身设置固定大小上线，无法进行调整，<font color = "red">而元空间使用的是直接内存，受本机可用内存的限制，并且永远不会得到 java.lang.OutOfMemoryError。</font>也可以通过JVM参数来指定元空间的大小。  


## 1.2. 内存中对象  

<!--
https://mp.weixin.qq.com/s/wsgxJSpEbY3yrmL9mDC2sw
-->


### 1.2.1. 对象的创建过程  
&emsp; 简述一下Java中创建一个对象的过程？  
&emsp; 解析：回答这个问题首先就要清楚类的生命周期  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-5.png)  
&emsp; Java中对象的创建就是在堆上分配内存空间的过程，此处说的对象创建仅限于new关键字创建的普通Java对象，不包括数组对象的创建。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-61.png)  
1. <font color = "red">检测类是否被加载</font>  
&emsp; 当虚拟机执行到new时，会先去常量池中查找这个类的符号引用。如果能找到符号引用，说明此类已经被加载到方法区（方法区存储虚拟机已经加载的类的信息），可以继续执行；如果找不到符号引用，就会使用类加载器执行类的加载过程，类加载完成后继续执行。  
2. <font color = "lime">为对象分配内存</font>  
&emsp; 类加载完成以后，虚拟机就开始为对象分配内存，此时所需内存的大小就已经确定了。只需要在堆上分配所需要的内存即可。  
    &emsp; 具体的分配内存有两种情况：第一种情况是内存空间绝对规整，第二种情况是内存空间是不连续的。  

    * 对于内存绝对规整的情况相对简单一些，虚拟机只需要在被占用的内存和可用空间之间移动指针即可，这种方式被称为指针碰撞。  
    * 对于内存不规整的情况稍微复杂一点，这时候虚拟机需要维护一个列表，来记录哪些内存是可用的。分配内存的时候需要找到一个可用的内存空间，然后在列表上记录下已被分配，这种方式称为空闲列表。  
        
    &emsp; <font color = "red">分配内存的时候也需要考虑线程安全问题，有两种解决方案：</font>  

    * 第一种是采用同步的办法，使用CAS来保证操作的原子性。
    * 另一种是每个线程分配内存都在自己的空间内进行，即是每个线程都在堆中预先分配一小块内存，称为本地线程分配缓冲（TLAB），分配内存的时候再TLAB上分配，互不干扰。
3. <font color = "red">为分配的内存空间初始化零值</font>  
&emsp; 对象的内存分配完成后，还需要将对象的内存空间都初始化为零值，这样能保证对象即使没有赋初值，也可以直接使用。  
4. <font color = "red">对对象进行其他设置</font>  
&emsp; 分配完内存空间，初始化零值之后，虚拟机还需要对对象进行其他必要的设置，设置的地方都在对象头中，包括这个对象所属的类，类的元数据信息，对象的hashcode，GC分代年龄等信息。  
5. <font color = "red">执行init方法</font>  
&emsp; 执行完上面的步骤之后，在虚拟机里这个对象就算创建成功了，但是对于Java程序来说还需要执行init方法才算真正的创建完成，因为这个时候对象只是被初始化零值了，还没有真正的去根据程序中的代码分配初始值，调用了init方法之后，这个对象才真正能使用。  

&emsp; 到此为止一个对象就产生了，这就是new关键字创建对象的过程。过程如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-60.png)  

#### 1.2.1.1. TLAB  
&emsp; ......
<!-- 
https://mp.weixin.qq.com/s/jPIHNsQwiYNCRUQt1qXR6Q

https://mp.weixin.qq.com/s/fyorrpT5-hFpIS5aEDNjZA
-->

### 1.2.2. 对象的内存布局  
&emsp; 对象的内存布局包括三个部分：对象头，实例数据和对齐填充。

* 对象头：对象头包括两部分信息，第一部分是存储对象自身的运行时数据，如哈希码，GC分代年龄，锁状态标志，线程持有的锁等等。第二部分是类型指针，即对象指向类元数据的指针。  
* 实例数据：就是数据  
* 对齐填充：不是必然的存在，就是为了对齐  

### 1.2.3. 对象的访问定位  
&emsp; <font color = "red">对象的访问定位有两种：句柄定位和直接指针。</font>  

* 句柄定位：Java 堆会画出一块内存来作为句柄池，reference中存储的就是对象的句柄地址，而句柄中包含了对象实例数据与类型数据各自的具体地址信息。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-58.png)  
* 直接指针访问：java堆对象的不居中就必须考虑如何放置访问类型数据的相关信息，而reference中存储的直接就是对象地址。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-59.png)  

### 1.2.4. 堆内存分配策略  
&emsp; JVM分配内存机制有三大原则和担保机制。具体如下所示：  

* 优先分配到eden区
* 大对象直接进入到老年代
* 长期存活的对象分配到老年代
* 空间分配担保  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-79.png)  

#### 1.2.4.1. 对象优先在Eden分配  
&emsp; 在JVM内存模型中，JVM年轻代堆内存可以划分为一块Eden区和两块Survivor区。在大多数情况下, 对象在新生代Eden区中分配, 当Eden区没有足够空间分配时，JVM发起一次Minor GC，将Eden区和其中一块Survivor区内尚存活的对象放入另一块Survivor区域，如果在Minor GC期间发现新生代存活对象无法放入空闲的Survivor区，则会通过空间分配担保机制使对象提前进入老年代。  

#### 1.2.4.2. 大对象直接进入老年代  
&emsp; Serial和ParNew两款收集器提供了-XX:PretenureSizeThreshold的参数，令大于该值的大对象直接在老年代分配, 这样做的目的是避免在Eden区和Survivor区之间产生大量的内存复制(大对象一般指需要大量连续内存的Java对象，如很长的字符串和数组), 因此大对象容易导致还有不少空闲内存就提前触发GC以获取足够的连续空间。  

#### 1.2.4.3. 长期存活的对象将进入老年代  
&emsp; 虚拟机采用了分代收集的思想来管理内存，那么内存回收时就必须能识别哪些对象应放在新生代，哪些对象应放在老年代中。为此，虚拟机为每个对象定义了一个对象年龄(Age)计数器，对象在Eden出生如果经第一次Minor GC后仍然存活，且能被Survivor容纳的话，将被移动到Survivor空间中，并将年龄设为1。以后对象在Survivor区中每经历一次Minor GC，年龄就+1。当增加到一定程度(-XX:MaxTenuringThreshold，默认15)，将会被晋升到老年代。对象晋升老年代的年龄阈值，可以通过参数-XX:MaxTenuringThreshold设置。  

#### 1.2.4.4. 动态对象年龄判定  
&emsp; 为了更好地适应不同程序的内存情况，JVM并不总是要求对象的年龄必须达到MaxTenuringThreshold才能晋升老年代: 如果在Survivor空间中相同年龄所有对象大小的总和大于Survivor空间的一半，年龄大于或等于该年龄的对象就可以直接进入老年代，而无须等到晋升年龄。  

#### 1.2.4.5. 空间分配担保  
&emsp; JVM在发生Minor GC之前，虚拟机会检查老年代最大可用的连续空间是否大于新生代所有对象的总空间，如果大于，则此次Minor GC是安全的如果小于，则虚拟机会查看HandlePromotionFailure设置项的值是否允许担保失败。如果HandlePromotionFailure=true，那么会继续检查老年代最大可用连续空间是否大于历次晋升到老年代的对象的平均大小，如果大于则尝试进行一次Minor GC，但这次Minor GC依然是有风险的；如果小于或者HandlePromotionFailure=false，则改为进行一次Full GC。  

<!-- 
2.2. 内存分配方式  
&emsp; 分配方式有“指针碰撞”和“空闲列表”两种，选择哪种分配方式由Java堆是否规整决定，而Java堆是否规整又由所采用的垃圾收集器是否带有压缩整理功能决定，即取决于GC收集器的算法是"标记-清除"，还是"标记-整理"（也称作"标记-压缩"），值得注意的是，复制算法内存也是规整的。  

2.2.1. 指针碰撞  
&emsp; 假设Java堆中内存是绝对规整的，所有用过的内存都放一边，空闲的内存放另一边，中间放着一个指针作为分界点的指示器，所分配内存就仅仅是把哪个指针向空闲空间那边挪动一段与对象大小相等的举例，这种分配方案就叫指针碰撞。  

2.2.2. 空闲列表  
&emsp; 有一个列表，其中记录中哪些内存块可用，在分配的时候从列表中找到一块足够大的空间划分给对象实例，然后更新列表中的记录。这就叫做空闲列表。  

2.3. ※※※内存分配并发问题  
&emsp; 问题产生：空闲列表中一块内存被分配后，还未更新空闲列表时，另一对象请求分配该地址。线程不安全。虚拟机采用两种方式来保证线程安全：  
&emsp; CAS（乐观锁）+失败重试：虚拟机采用CAS配上失败重试的方式保证更新操作的原子性。  
&emsp; TLAB：为每一个线程预先在Eden区分配一块儿内存，JVM在给线程中的对象分配内存时，首先在TLAB分配，当对象大于TLAB中的剩余内存或TLAB的内存已用尽时，再采用上述的CAS进行内存分配。  
-->


## 1.3. 内存泄漏、内存溢出  
<!-- 

Java内存泄漏 
https://mp.weixin.qq.com/s/Q3yGYfRpgIwUFYAaOC-8eA
-->
&emsp; **<font color = "red">内存溢出out of memory</font>** ，是指<font color = "red">程序在申请内存时，没有足够的内存空间供其使用</font>，出现out of memory；  
&emsp; **<font color = "red">内存泄露 memory leak</font>** ，是指<font color = "red">程序在申请内存后，无法释放已申请的内存空间</font>。一次内存泄露危害可以忽略，但内存泄露堆积后果很严重，无论多少内存，迟早会被占光。内存泄露，会导致频繁的Full GC。  
&emsp; 所以内存泄漏可能会导致内存溢出，但内存溢出并不完全都是因为内存泄漏，也有可能使用了太多的大对象导致。  

&emsp; **<font color = "red">JVM堆内存溢出后，其他线程是否可继续工作？</font>**  
&emsp; 当一个线程抛出OOM异常后，它所占据的内存资源会全部被释放掉，从而不会影响其他线程的运行！  
&emsp; 其实发生OOM的线程一般情况下会死亡，也就是会被终结掉，该线程持有的对象占用的heap都会被gc了，释放内存。因为发生OOM之前要进行gc，就算其他线程能够正常工作，也会因为频繁gc产生较大的影响。  

### 1.3.1. 内存溢出演示-1  
<!--
（内存溢出演示）几种典型的内存溢出案例，都在这儿了！
https://mp.weixin.qq.com/s/4SenzIeX9FqsnXAaV6IgLg
 教你写Bug，常见的 OOM 异常分析 
https://mp.weixin.qq.com/s/gIJvtd8rrZz6ttaoGLddLg

面试官：哪些场景会产生OOM？怎么解决？ 
https://mp.weixin.qq.com/s/j8_6QW_WLqlZDUjbDUbyZw

https://mp.weixin.qq.com/s/XJhtBYGMJps4B5wzNTsSVA


-->

### 内存泄露场景  

<!-- 
java内存泄漏与内存溢出
https://www.cnblogs.com/panxuejun/p/5883044.html

-->


### 1.3.2. 内存溢出问题详解  


-----
-----
-----
## 1.4. 延伸：常量池  
<!-- 
https://mp.weixin.qq.com/s/391mANG6x1euW2Savj7ltA
-->
&emsp; 常量池分为三种：class 文件中的常量池、运行时常量池、字符串常量池。

