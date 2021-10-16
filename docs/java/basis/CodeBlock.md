

<!-- TOC -->

- [1. 代码块](#1-代码块)
    - [1.1. 四种代码块](#11-四种代码块)
    - [1.2. 四者之间的作用和区别](#12-四者之间的作用和区别)

<!-- /TOC -->


# 1. 代码块
<!-- 
https://blog.csdn.net/qq_37580085/article/details/108408951?utm_medium=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-1.no_search_link&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-1.no_search_link

https://jingyan.baidu.com/article/925f8cb8bf47f1c0dde056e9.html

代码块的作用
https://www.cnblogs.com/newbie27/p/10414604.html
https://blog.csdn.net/weixin_30385221/article/details/114469679


-->

## 1.1. 四种代码块
&emsp; Java语言中，代码块一般可分为四类： 普通代码块，构造代码块，静态代码块，同步代码块。   

1. 普通代码块  
&emsp; 普通代码块就是我们平常所说的方法中的方法体。  
2. 构造代码块  
&emsp; 构造代码块是由来个大括号包裹起来的代码块，其语法结构如下：  

    ```java
    {
        // …………
        // …………
    }
    ```
3. 静态代码块  
&emsp; 静态代码块是由static{}包裹起来的代码片段，其语法结构如法：  

    ```java
    static{
        // …………
        // …………
    }
    ```
4. 同步块  
&emsp; 同步代码块是由synchronized（）{}包裹起来的代码块，其语法结构如下：  

    ```java
    synchronized{
        // …………
        // …………
    }
    ```

## 1.2. 四者之间的作用和区别
&emsp; 普通代码块就是保证方法的执行。同步代码块用于多线程环境下。在多线程环境下，对共享数据的读写操作是需要互斥进行的，否则会导致数据的不一致性。同步代码块需要写在方法中。我们主要来看一下静态代码块和构造代码块的作用和区别。  
&emsp; 先看一下代码：  

```java
public class Test {
    public static void main(String[] args) {
        TestOne t = new TestOne();
        t.getTestOne();
    }
}
class TestOne{
    private static int testOne;
    static {
        testOne = 1;
        System.out.println(testOne);
    }    //静态代码块
    {
        testOne = 2;
        System.out.println(testOne);
    }    //todo 构造代码块

    public TestOne() {
        testOne = 3;
    }  //构造方法

    public  void getTestOne() {
        System.out.println(testOne);
    }
}
```

&emsp; 在TestOne类里分别定义了静态代码块，构造代码块和构造方法。  
&emsp; 输出结果：  

```text
1
2
3
```
&emsp; 可以看出，静态代码块和构造代码块先于构造方法执行，静态代码块最先执行，然后构造代码块执行，最后构造函数执行。  
&emsp; 静态代码块可以有多个，在类加载的时候最先执行，并且只执行一次，可以用来记录类加载的时机。  
&emsp; 构造代码块在静态代码块之后执行，在构造方法之前执行，并且，`构造代码块可以执行多次，只要构造方法执行一次，构造代码块也会跟随执行一次。`  
&emsp; 静态代码块和动态代码块都存在于类中，不能存在于方法体内。  
