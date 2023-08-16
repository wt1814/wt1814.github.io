
<!-- TOC -->

- [1. JVM栈](#1-jvm栈)
    - [1.1. 栈简介](#11-栈简介)
    - [1.2. 【方法】栈](#12-方法栈)
    - [1.3. 方法【栈】](#13-方法栈)
        - [1.3.1. 局部变量表](#131-局部变量表)
        - [1.3.2. 操作栈](#132-操作栈)
        - [1.3.3. 动态连接](#133-动态连接)
        - [1.3.4. 方法返回地址](#134-方法返回地址)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. <font color = "red">JVM栈描述Java方法执行的内存模型。</font>Java虚拟机栈是线程私有的。Java虚拟机栈会出现两种异常：StackOverFlowError和OutOfMemoryError。    
2. 【方法】栈  
&emsp; Java虚拟机栈中出栈入栈的元素称为“栈帧”，栈对应线程，栈帧对应方法。每个方法被执行的时候，都会创建一个栈帧，把栈帧压入栈，当方法正常返回或者抛出未捕获的异常时，栈帧就会出栈。  
3. 方法【栈】  
    &emsp; <font color = "red">Java虚拟机栈是由一个个栈帧组成，每个栈帧中都拥有：局部变量表、操作数栈、动态链接、方法出口信息。</font>  
    * 局部变量表，局部变量表用于保存函数参数和局部变量。  
    * 操作数栈，操作数栈用于保存计算过程的中间结果，作为计算过程中变量临时的存储空间。  
    * 动态连接：每个栈帧中包含一个在常量池中对当前方法的引用， 目的是支持方法调用过程的动态连接。  
    * 方法返回地址
4. `为什么不把基本类型放堆中呢？`   
&emsp; 因为其占用的空间一般是 1~8 个字节——需要空间比较少，而且因为是基本类型，所以不会出现动态增长的情况——长度固定，因此栈中存储就够了。  


# 1. JVM栈  
<!--
JVM的栈内存
https://mp.weixin.qq.com/s?__biz=MzAxMjEwMzQ5MA==&mid=2448890565&idx=2&sn=b124180bb28ff7a9eda6418486545d18&chksm=8fb540e8b8c2c9fec3c974d2610a3c808cfbfa1ad89d5c6977bff9293136aebd2ae74f7a330a&scene=178&cur_album_id=1352977495578165250#rd

https://juejin.cn/post/6844903983400632327

https://www.cnblogs.com/whu-2017/p/9629714.html


为什么不把基本类型放堆中呢？
https://mp.weixin.qq.com/s/BnM2KL9CpEWlVXa_hEcEdg
-->

## 1.1. 栈简介  
1. <font color = "red">JVM栈描述Java方法执行的内存模型。</font>  
2. Java虚拟机栈是线程私有的。  
3. Java虚拟机栈会出现两种异常：StackOverFlowError和OutOfMemoryError。  


## 1.2. 【方法】栈
<!-- 
https://juejin.cn/post/6844903983400632327

https://mp.weixin.qq.com/s?__biz=MzAxMjEwMzQ5MA==&mid=2448890565&idx=2&sn=b124180bb28ff7a9eda6418486545d18&chksm=8fb540e8b8c2c9fec3c974d2610a3c808cfbfa1ad89d5c6977bff9293136aebd2ae74f7a330a&scene=178&cur_album_id=1352977495578165250#rd

-->
&emsp; Java虚拟机栈中出栈入栈的元素称为“栈帧”，栈对应线程，栈帧对应方法。每个方法被执行的时候，都会创建一个栈帧，把栈帧压入栈，当方法正常返回或者抛出未捕获的异常时，栈帧就会出栈。  

&emsp; 执行流程如下：  
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
![image](http://182.92.69.8:8081/img/java/JVM/JVM-8.png)  


## 1.3. 方法【栈】
<!-- 
https://juejin.cn/post/6844903983400632327

-->
&emsp; 存储内容：将线程私有的不可能被其他线程访问的对象打散分配在栈上，而不是分配在堆上。打散分配意思是将对象的不同属性分别分配给不同的局部变量。  
&emsp; <font color = "red">Java虚拟机栈是由一个个栈帧组成，每个栈帧中都拥有：局部变量表、操作数栈、动态链接、方法出口信息。</font>  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-9.png)  

### 1.3.1. 局部变量表   
&emsp; `局部变量表用于保存函数参数和局部变量。`  

&emsp; 指存放方法参数和方法内部定义的局部变量的区域。局部变量表所需的内存空间在编译期间完成分配，当进入一个方法时，这个方法需要在帧中分配多大的局部变量空间是完全确定的，在方法运行期间不会改变局部变量表的大小。  
&emsp; 这里直接上代码，更好理解。  

```java
publicint test(int a, int b) {
    Object obj = newObject();
    return a + b;
}
```
&emsp; 如果局部变量是Java的8种基本数据类型，则存在局部变量表中，如果是引用类型。如new出来的String，局部变量表中存的是引用，而实例在堆中。  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-34.png)  
&emsp; `为什么不把基本类型放堆中呢？`  
<!-- 
https://mp.weixin.qq.com/s/BnM2KL9CpEWlVXa_hEcEdg
-->
&emsp; 因为其占用的空间一般是 1~8 个字节——需要空间比较少，而且因为是基本类型，所以不会出现动态增长的情况——长度固定，因此栈中存储就够了，如果把它存在堆中是没有什么意义的。可以这么说，基本类型和对象的引用都是存放在栈中，而且都是几个字节的一个数，因此在程序运行时，它们的处理方式是统一的。但是基本类型、对象引用和对象本身就有所区别了，因为一个是栈中的数据一个是堆中的数据。最常见的一个问题就是，Java 中参数传递时的问题。  


### 1.3.2. 操作栈  

&emsp; `操作数栈用于保存计算过程的中间结果，作为计算过程中变量临时的存储空间。`  

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

### 1.3.3. 动态连接  
&emsp; 每个栈帧中包含一个在常量池中对当前方法的引用， 目的是支持方法调用过程的动态连接。  

### 1.3.4. 方法返回地址  
&emsp; 方法执行时有两种退出情况：  
* 正常退出，即正常执行到任何方法的返回字节码指令，如 RETURN、 IRETURN、 ARETURN等  
* 异常退出  

&emsp; 无论何种退出情况，都将返回至方法当前被调用的位置。方法退出的过程相当于弹出当前栈帧，退出可能有三种方式：  
* 返回值压入上层调用栈帧  
* 异常信息抛给能够处理的栈帧  
* PC 计数器指向方法调用后的下一条指令  


