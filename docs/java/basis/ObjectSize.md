

<!-- TOC -->

- [1. Java对象所占内存的大小](#1-java对象所占内存的大小)
    - [1.1. 通过代码计算对象的大小](#11-通过代码计算对象的大小)
    - [1.2. 分析java对象的组成](#12-分析java对象的组成)
        - [1.2.1. 对象头详解](#121-对象头详解)
            - [1.2.1.1. Mark Word](#1211-mark-word)
            - [1.2.1.2. class pointer](#1212-class-pointer)
            - [1.2.1.3. array length](#1213-array-length)
    - [1.3. 用例测试](#13-用例测试)
    - [1.4. 节约内存原则](#14-节约内存原则)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 在JVM中，对象在内存中的布局分为三块区域：对象头、实例数据和对齐填充。  
    * 实例数据：存放类的属性数据信息，包括父类的属性信息，如果是数组的实例部分还包括数组的长度，这部分内存按4字节对齐。    
    * 对齐填充：JVM要求对象起始地址必须是8字节的整数倍(8字节对齐)。填充数据不是必须存在的，仅仅是为了字节对齐。   
2. JVM中对象头的方式有以下两种(以32位JVM为例)  
    * 普通对象：  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-60.png)   
    * 数组对象：  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-61.png)   

    对象头：包含Mark Word、class pointer、array length共3部分。  
    1. Mark Word：  
    &emsp; **<font color = "red">由于对象头信息是与对象自身定义的数据无关的额外存储成本，考虑到Java虚拟机的空间使用效率，</font>** **<font color = "clime">Mark Word被设计成一个非固定的动态数据结构，</font>** 以便在极小的空间内存储尽量多的信息。它会根据对象的状态复用自己的存储空间。  
    &emsp; 这部分主要用来存储对象自身的运行时数据，如hashcode、gc分代年龄等。mark word的位长度为JVM的一个Word大小，也就是说32位JVM的Mark word为32位，64位JVM为64位。
    为了让一个字大小存储更多的信息，JVM将字的最低两个位设置为标记位，
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-67.png)   

    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-68.png)   
    2. class pointer：  
    &emsp; 这一部分用于存储对象的类型指针，该指针指向它的类元数据，JVM通过这个指针确定对象是哪个类的实例。该指针的位长度为JVM的一个字大小，即32位的JVM为32位，64位的JVM为64位。 
    3. array length：  
    &emsp; 如果对象是一个数组，那么对象头还需要有额外的空间用于存储数组的长度，这部分数据的长度也随着JVM架构的不同而不同：32位的JVM上，长度为32位；64位JVM则为64位。64位JVM如果开启+UseCompressedOops选项，该区域长度也将由64位压缩至32位。  

# 1. Java对象所占内存的大小
<!--
https://mp.weixin.qq.com/s/bKg-CufJf2vZshJB_ssY5w
https://www.cnblogs.com/ssskkk/p/12814931.html
https://blog.csdn.net/yunqiinsight/article/details/80431831


https://blog.csdn.net/xmtblog/article/details/103760205
-->


## 1.1. 通过代码计算对象的大小
<!-- 
https://blog.csdn.net/antony9118/article/details/54317637
-->


## 1.2. 分析java对象的组成
<!-- 
Java对象头与monitor
https://blog.csdn.net/kking_edc/article/details/108382333
-->
&emsp; 在JVM中，对象在内存中的布局分为三块区域：对象头、实例数据和对齐填充，如下所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-65.png)   

* 对象头：包含Mark Word、class pointer、array length共3部分。  
    * **第一部分用于存储对象自身的运行时数据，如哈希码、GC分代年龄、锁标识状态、线程持有的锁、偏向线程ID等。** 这部分数据的长度在32位和64位的Java虚拟机中分别会占用32个或64个比特，官方称它为“Mark Word”。这部分是实现轻量级锁和偏向锁的关键。  
    * 另外一部分指针类型，指向对象的类元数据类型(即对象代表哪个类)。如果是数组对象，则对象头中还有一部分用来记录数组长度。  
    * 还会有一个额外的部分用于存储数组长度。  
* 实例数据：存放类的属性数据信息，包括父类的属性信息，如果是数组的实例部分还包括数组的长度，这部分内存按4字节对齐。    
* 对齐填充：JVM要求对象起始地址必须是8字节的整数倍(8字节对齐)。填充数据不是必须存在的，仅仅是为了字节对齐。   

<!--   
工具：JOL = Java Object Layout   
<dependencies>
    <dependency>
        <groupId>org.openjdk.jol</groupId>
        <artifactId>jol-core</artifactId>
        <version>0.9</version>
    </dependency>
</dependencies>
-->

### 1.2.1. 对象头详解  
&emsp; JVM中对象头的方式有以下两种(以32位JVM为例)  

* 普通对象：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-60.png)   
* 数组对象：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-61.png)   

&emsp; `对象头32位与64位占用空间不同。在32位中： hash(25)+age(4)+lock(3)=32bit； 64位中：unused(25+1)+hash(31)+age(4)+lock(3)=64bit。`  

#### 1.2.1.1. Mark Word  
&emsp; **<font color = "red">由于对象头信息是与对象自身定义的数据无关的额外存储成本，考虑到Java虚拟机的空间使用效率，</font>** **<font color = "clime">Mark Word被设计成一个非固定的动态数据结构，</font>** 以便在极小的空间内存储尽量多的信息。它会根据对象的状态复用自己的存储空间。  
&emsp; 这部分主要用来存储对象自身的运行时数据，如hashcode、gc分代年龄等。mark word的位长度为JVM的一个Word大小，也就是说32位JVM的Mark word为32位，64位JVM为64位。
&emsp; 为了让一个字大小存储更多的信息，JVM将字的最低两个位设置为标记位，不同标记位下的Mark Word示意如下：  

&emsp; 64位下的标记字与32位的相似：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-41.png)   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-62.png)   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-64.png)   

&emsp; 下面两张图是32位JVM和64位JVM中“Mark Word”所记录的信息  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-67.png)   

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-68.png)   

&emsp; 其中各部分的含义如下：(<font color = "red">用对象头中markword最低的三位代表锁状态，其中1位是偏向锁位，两位是普通锁位。</font>)  

* lock：2位的锁状态标记位，由于希望用尽可能少的二进制位表示尽可能多的信息，所以设置了lock标记。该标记的值不同，整个mark word表示的含义不同。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-63.png)   
* bias_lock：对象是否启动偏向锁标记，只占1个二进制位。为1时表示对象启动偏向锁，为0时表示对象没有偏向锁。  
* age：4位的Java对象年龄。在GC中，如果对象在Survivor区复制一次，年龄增加1。当对象达到设定的阈值时，将会晋升到老年代。默认情况下，并行GC的年龄阈值为15，并发GC的年龄阈值为6。由于age只有4位，所以最大值为15，这就是-XX:MaxTenuringThreshold选项最大值为15的原因。  
* identity_hashcode：25位的对象标识Hash码，采用延迟加载技术。调用方法System.identityHashCode()计算，并会将结果写到该对象头中。当对象被锁定时，该值会移动到管程Monitor中。  
* thread：持有偏向锁的线程ID。  
* epoch：偏向时间戳。  
* ptr_to_lock_record：指向栈中锁记录的指针。  
* **<font color = "clime">ptr_to_heavyweight_monitor：指向monitor对象(也称为管程或监视器锁)的起始地址，每个对象都存在着一个monitor与之关联，对象与其monitor之间的关系有存在多种实现方式，如monitor对象可以与对象一起创建销毁或当前线程试图获取对象锁时自动生，但当一个monitor被某个线程持有后，它便处于锁定状态。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-56.png)   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-76.png)   

        为什么锁信息存放在对象头里？
        因为在Java中任意对象都可以用作锁，因此必定要有一个映射关系，存储该对象以及其对应的锁信息(比如当前哪个线程持有锁，哪些线程在等待)。一种很直观的方法是，用一个全局map，来存储这个映射关系，但这样会有一些问题：需要对map做线程安全保障，不同的Synchronized之间会相互影响，性能差；另外当同步对象较多时，该map可能会占用比较多的内存。
        所以最好的办法是将这个映射关系存储在对象头中，因为对象头本身也有一些hashcode、GC相关的数据，所以如果能将锁信息与这些信息共存在对象头中就好了。
        也就是说，如果用一个全局 map 来存对象的锁信息，还需要对该 map 做线程安全处理，不同的锁之间会有影响。所以直接存到对象头。

#### 1.2.1.2. class pointer
&emsp; 这一部分用于存储对象的类型指针，该指针指向它的类元数据，JVM通过这个指针确定对象是哪个类的实例。该指针的位长度为JVM的一个字大小，即32位的JVM为32位，64位的JVM为64位。  
&emsp; 如果应用的对象过多，使用64位的指针将浪费大量内存，统计而言，64位的JVM将会比32位的JVM多耗费50%的内存。为了节约内存可以使用选项+UseCompressedOops开启指针压缩，其中，oop即ordinary object pointer普通对象指针。开启该选项后，下列指针将压缩至32位：  

* 每个Class的属性指针(即静态变量)
* 每个对象的属性指针(即对象变量)
* 普通对象数组的每个元素指针

&emsp; 当然，也不是所有的指针都会压缩，一些特殊类型的指针JVM不会优化，比如指向PermGen的Class对象指针(JDK8中指向元空间的Class对象指针)、本地变量、堆栈元素、入参、返回值和NULL指针等。  

#### 1.2.1.3. array length  
&emsp; 如果对象是一个数组，那么对象头还需要有额外的空间用于存储数组的长度，这部分数据的长度也随着JVM架构的不同而不同：32位的JVM上，长度为32位；64位JVM则为64位。64位JVM如果开启+UseCompressedOops选项，该区域长度也将由64位压缩至32位。  


## 1.3. 用例测试
<!-- 
https://www.cnblogs.com/ssskkk/p/12814931.html
-->


## 1.4. 节约内存原则
<!-- 
https://mp.weixin.qq.com/s/bKg-CufJf2vZshJB_ssY5w
-->