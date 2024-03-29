
<!-- TOC -->

- [1. Java关键字](#1-java关键字)
    - [1.1. Java访问控制符](#11-java访问控制符)
    - [1.2. static](#12-static)
        - [1.2.1. 静态方法可以调用成员变量吗？](#121-静态方法可以调用成员变量吗)
        - [1.2.2. static与JVM](#122-static与jvm)
    - [1.3. final](#13-final)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. static关键字：  
    1. 方便在没有创建对象的情况下进行调用（方法/变量）。  
    2. static使用： 1). static修饰变量、 2). 修饰方法、 3). static 可以修饰代码块，主要分为两种，一种直接定义在类中，使用static{}，这种被称为静态代码块，一种是在类中定义静态内部类，使用static class xxx来进行定义、 4). static可以和单例模式一起使用，通过双重检查锁来实现线程安全的单例模式、 5).静态导包。     
    3. 静态方法可以调用成员变量吗？ `注⚠️：static静态方法引用类变量，变量需要static修饰。`  
    4. static与JVM： 
        1. static作为类变量，只被加载一次。  
        2. 类的加载流程中，准备阶段、初始化阶段。  
        3. static变量被存放在方法区中。    


# 1. Java关键字
## 1.1. Java访问控制符  
&emsp; JAVA语言中有公共的(public)，私有的(private)，保护的(protacted)和默认的(default)四种访问控制符。　  
![image](http://182.92.69.8:8081/img/java/JDK/basics/java-2.png)  
&emsp; 访问属性：在外部类中访问public和default的属性，可以通过类的对象名.属性名直接访问。在外部类中访问private的属性，必须通过对象的get、set方法(get、set方法声明为public)。  
&emsp; 访问方法：在外部类中访问private的方法，必须通过反射。  

## 1.2. static  
<!-- 
https://www.cnblogs.com/dolphin0520/p/3799052.html
-->

&emsp; 方便在没有创建对象的情况下进行调用（方法/变量）。  

---------

<!-- https://mp.weixin.qq.com/s?__biz=MzI0ODk2NDIyMQ==&mid=2247484455&idx=1&sn=582d5d2722dab28a36b6c7bc3f39d3fb&chksm=e999f135deee7823226d4da1e8367168a3d0ec6e66c9a589843233b7e801c416d2e535b383be&token=1154740235&lang=zh_CN&scene=21#wechat_redirect -->
&emsp; static 是 Java 中非常重要的关键字，static 表示的概念是 静态的，在 Java 中，static 主要用来  

* 修饰变量，static 修饰的变量称为静态变量、也称为类变量，类变量属于类所有，对于不同的类来说，static 变量只有一份，static 修饰的变量位于方法区中；static 修饰的变量能够直接通过 类名.变量名 来进行访问，不用通过实例化类再进行使用。  
* 修饰方法，static 修饰的方法被称为静态方法，静态方法能够直接通过 类名.方法名 来使用，在静态方法内部不能使用非静态属性和方法。  
* static 可以修饰代码块，主要分为两种，一种直接定义在类中，使用static{}，这种被称为静态代码块，一种是在类中定义静态内部类，使用static class xxx来进行定义。  
* static 可以和单例模式一起使用，通过双重检查锁来实现线程安全的单例模式。
* static 可以用于静态导包，通过使用 import static xxx  来实现，这种方式一般不推荐使用。  

&emsp; **Java中static块执行时机：<font color = "red">static块的执行发生在“初始化”的阶段。</font>类被加载了不一定就会执行静态代码块，只有一个类被主动使用的时候，静态代码才会被执行！**   

&emsp; 使用案例：  

```java
private static String localIP;

static{
    try {
        InetAddress inetAddress = InetAddress.getLocalHost();
        localIP = inetAddress.getHostAddress();
    } catch (UnknownHostException e) {
        e.printStackTrace();
        log.error("未获取到本机的IP地址，发送给财务系统的IP为空");
    }
}
```

### 1.2.1. 静态方法可以调用成员变量吗？  
<!-- 
https://blog.csdn.net/bulubuluu/article/details/75688584
-->

### 1.2.2. static与JVM
<!-- 
静态变量与JVM内存分配
https://blog.csdn.net/ziyonghong/article/details/81163060

https://bestqiang.github.io/2019/05/06/%E4%BB%8Eclass%E6%96%87%E4%BB%B6%E4%B8%8EJVM%E5%8A%A0%E8%BD%BD%E6%9C%BA%E5%88%B6%E7%90%86%E8%A7%A3final%E3%80%81static%E3%80%81static-final/
-->

## 1.3. final  
<!-- 
浅析Java中的final关键字
https://www.cnblogs.com/dolphin0520/p/3736238.html
-->
&emsp; final 是 Java 中的关键字，它表示的意思是 <font color="red">不可变的</font>，在 Java 中，final 主要用来  

* 修饰类，final 修饰的类不能被继承，不能被继承的意思就是不能使用 extends 来继承被 final 修饰的类。
* 修饰变量，final 修饰的变量不能被改写，不能被改写的意思有两种，对于基本数据类型来说，final 修饰的变量，其值不能被改变，final 修饰的对象，对象的引用不能被改变，但是对象内部的属性可以被修改。final 修饰的变量在某种程度上起到了不可变的效果，所以，可以用来保护只读数据，尤其是在并发编程中，因为明确的不能再为 final 变量进行赋值，有利于减少额外的同步开销。
* 修饰方法，final 修饰的方法不能被重写。  

&emsp; final修饰符和Java程序性能优化没有必然联系。


