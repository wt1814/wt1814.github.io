
<!-- TOC -->

- [1. java.lang.Object类](#1-javalangobject类)
    - [1.1. 类构造器](#11-类构造器)
    - [1.2. 成员方法](#12-成员方法)
        - [1.2.1. equals方法](#121-equals方法)
            - [1.2.1.1. 为什么重写equals方法？](#1211-为什么重写equals方法)
        - [1.2.2. hashCode方法](#122-hashcode方法)
            - [1.2.2.1. hashCode值是怎么获取的？](#1221-hashcode值是怎么获取的)
            - [1.2.2.2. ★★★为什么equals方法重写，建议也一起重写hashcode方法？](#1222-★★★为什么equals方法重写建议也一起重写hashcode方法)
        - [1.2.3. getClass方法](#123-getclass方法)
        - [1.2.4. toString方法](#124-tostring方法)
        - [1.2.5. notify()/notifyAll()/wait()-1](#125-notifynotifyallwait-1)

<!-- /TOC -->

# 1. java.lang.Object类  

<!--
泛型的作用，以及与Object的区别
https://www.cnblogs.com/xiaoxiong2015/p/12705815.html

面试官:为什么 wait() 方法需要写在while里,而不是if? 
https://mp.weixin.qq.com/s/KhNX3xHJQbuYeUjY0G3bhw

-->
<!-- 
equasl 和 hashcode
https://mp.weixin.qq.com/s/qfm-Xq1ZNJFJdSZ58qSLLA
-->

&emsp; Object类的结构图：  
![image](http://www.wt1814.com/static/view/images/java/JDK/basics/java-1.png)  

## 1.1. 类构造器
&emsp; 类构造器是创建Java对象的方法之一。一般都使用new关键字来进行实例，还可以在构造器中进行相应的初始化操作。  
&emsp; <font color = "red">在一个Java类中必须存在一个构造器，如果没有添加，系统在编译时会默认创建一个无参构造。</font>  

## 1.2. 成员方法  

### 1.2.1. equals方法  
&emsp; Object类中的equals方法：  

```java
public boolean equals(Object obj) {
    return (this == obj);
}
```
<!--&emsp; 在Object类中，==运算符和equals方法是等价的，都是比较两个对象的引用是否相等，从另一方面来讲，如果两个对象的引用相等，那么这两个对象一定是相等的。-->

&emsp; **==和equals的区别：**  
*  == 是 Java 中一种操作符。  
    * 对于基本数据类型来说， == 判断的是两边的值是否相等。  
    * 对于引用类型来说， == 判断的是两边的引用是否相等，也就是判断两个对象是否指向了同一块内存区域。  
* equals 是 Java 中所有对象的父类，即 Object 类定义的一个方法。<font color = "red">它只能比较对象，它表示的是引用双方的值是否相等。</font>所以，并不是说 == 比较的就是引用是否相等，equals 比较的就是值，这需要区分来说的。  
    &emsp; equals 用作对象之间的比较具有如下特性：  

    * 自反性：对于任何非空引用 x 来说，x.equals(x) 应该返回 true。  
    * 对称性：对于任何非空引用 x 和 y 来说，若x.equals(y)为 true，则y.equals(x)也为 true。  
    * 传递性：对于任何非空引用的值来说，有三个值，x、y 和 z，如果x.equals(y) 返回true，y.equals(z) 返回true，那么x.equals(z) 也应该返回true。  
    * 一致性：对于任何非空引用 x 和 y 来说，如果 x.equals(y) 相等的话，那么它们必须始终相等。  
    * 非空性：对于任何非空引用的值 x 来说，x.equals(null) 必须返回 false。  

#### 1.2.1.1. 为什么重写equals方法？
&emsp; 对于自定义的一个对象，如果不重写equals方法，那么在比较对象的时候就是调用Object类的equals方法，也就是用==运算符比较两个对象。  

### 1.2.2. hashCode方法  

```java
public native int hashCode();
```
&emsp; hashCode()是一个用native声明的本地方法，返回对象的散列码，是int类型的数值。  
&emsp; **<font color = "clime">java中的hashCode有两个作用：</font>**  
1. Object的hashCode返回对象的内存地址。  
2. 对象重写的hashCode配合基于散列的集合一起正常运行，这样的散列集合包括HashSet、HashMap以及HashTable等。对于大量的元素比较时直接比较equals效率低下，可先判断hashCode再判断equals，因为不同的对象可能返回相同的hashCode(如"Aa"和"BB"的hashCode就一样)，所以比较时有时需要再比较equals。hashCode只是起辅助作用。为了使字符串计算出来的hashCode尽可能的少重复，即降低哈希算法的冲突率，设计者选择了31这个乘数。  

#### 1.2.2.1. hashCode值是怎么获取的？  
&emsp; 每个对象都有hashcode，通过对象的内部地址(也就是物理地址)转换成一个整数，然后该整数通过hash函数的算法就得到了hashcode。所以hashcode是在hash表中对应的位置。  
&emsp; HashCode用来在散列表(如HashSet、HashMap以及HashTable等)中确定对象的存储地址。hashCode仅在散列表中才有用。  

#### 1.2.2.2. ★★★为什么equals方法重写，建议也一起重写hashcode方法？  
<!-- 
重写equals就必须重写hashCode的原理分析
https://www.cnblogs.com/wang-meng/p/7501378.html
https://blog.csdn.net/u012557538/article/details/89861552
-->
&emsp; **<font color = "blue">在某些业务场景下，需要使用自定义类作为哈希表的键。用HashMap存入自定义的类时，如果不重写这个自定义类的equals和hashCode方法，得到的结果会和预期的不一样。</font>**  

### 1.2.3. getClass方法  
&emsp; getClass方法是一个本地方法，被final修饰子类不能够重写，通过该方法可以获取类的元数据和方法信息。它能够获取一个类的定义信息，然后使用反射去访问类的全部信息，包括函数和字段。  
&emsp; getClass方法返回的是一个对象的运行时类对象。Java中还有一种这样的用法，通过类名.class 获取这个类的类对象，这两种用法有什么区别呢？  
&emsp; 父类：Parent.class  

```java
public class Parent {}
```
&emsp; 子类：Son.class  

```java
public class Son extends Parent{}
```
&emsp; 测试：  

```java
@Test
public void testClass(){
    Parent p = new Son();
    System.out.println(p.getClass());
    System.out.println(Parent.class);
}
```
&emsp; 打印结果： 

    class com.ys.test.Son
    class com.ys.test.Parent  

&emsp; 结论：class是一个类的属性，能获取该类编译时的类对象，而getClass()是一个类的方法，它是获取该类运行时的类对象。  

### 1.2.4. toString方法
&emsp; toString方法是一个非本地方法。逻辑是获取class名称加上@再加上十六进制的hashCode。<font color = "red">当用System.out.println()输出对象的时候实际上会调用对象的toString方法，所以想看到自定义的输出信息时可以重写对象的toString方法。</font>  

&emsp; **<font color = "red">为什么说System.out.println()输出对象的时候会调用对象的toString方法：</font>**  
&emsp; 在System类中可以看到有一个out属性，类型是PrintStream：public final static PrintStream out = null;  
&emsp; 再看PrintStream的println方法，会调用String.valueOf方法将对象转换为String类型：  

```java
public void println(Object x) {
    String s = String.valueOf(x);
    synchronized (this) {
        print(s);
        newLine();
    }
}
```
&emsp; 而在String类的valueOf方法中：  

```java
public static String valueOf(Object obj) {
    return (obj == null) ? "null" : obj.toString();
}
```
&emsp; 如果对象不是null的话，返回的是对象的toString()方法。  
&emsp; 所以System.out.println()输出对象的时候会调用对象的toString方法。  

### 1.2.5. notify()/notifyAll()/wait()-1  
&emsp; **<font color = "red">为什么在Object类中定义wait和notify方法？</font>**  
1. wait和notify不仅仅是普通方法或同步工具，更重要的是它们是 Java中两个线程之间的通信机制。对语言设计者而言，如果不能通过Java关键字(例如 synchronized)实现通信此机制，同时又要确保这个机制对每个对象可用，那么 Object类则是正确声明位置。记住同步和等待通知是两个不同的领域，不要把它们看成是相同的或相关的。同步是提供互斥并确保 Java 类的线程安全，而 wait 和 notify 是两个线程之间的通信机制。    
2. 每个对象都可上锁，这是在 Object 类而不是 Thread 类中声明wait和 notify的另一个原因。  
3. 在Java中为了进入代码的临界区，线程需要锁定并等待锁定，它们不知道哪些线程持有锁，而只是知道锁被某个线程持有，并且它们应该等待取得锁，而不是去了解哪个线程在同步块内，并请求它们释放锁定。  
4. Java是基于Hoare的监视器的思想。在Java中，所有对象都有一个监视器。  

