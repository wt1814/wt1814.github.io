
<!-- TOC -->

- [1. 线程基本操作](#1-线程基本操作)
    - [1.1. 创建并运行java线程](#11-创建并运行java线程)
        - [1.1.1. 继承Thread父类](#111-继承thread父类)
        - [1.1.2. Runnable接口](#112-runnable接口)
        - [1.1.3. Callable接口](#113-callable接口)
        - [1.1.4. 三种创建线程的不同](#114-三种创建线程的不同)
    - [1.2. 线程传参](#12-线程传参)
        - [1.2.1. 构造方法](#121-构造方法)
        - [1.2.2. 变量和方法](#122-变量和方法)
        - [1.2.3. 回调函数](#123-回调函数)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. **Future是一个接口，它可以对具体的Runnable或者Callable任务进行取消、判断任务是否已取消、查询任务是否完成、获取任务结果。**  
2. JDK1.5为Future接口提供了一个实现类FutureTask，表示一个可以取消的异步运算。它有启动和取消运算、查询运算是否完成和取回运算结果等方法。  

# 1. 线程基本操作  
<!-- 
详细分析 Java 中实现多线程的方法有几种?(从本质上出发) 
https://mp.weixin.qq.com/s/IKU56LMVZeVFVqRf-0N0tw
-->

## 1.1. 创建并运行java线程
&emsp; Java使用Thread类代表线程，所有的线程对象都必须是Thread类或其子类的实例。  
&emsp; Java创建线程方式：1.继承Thread类；2.实现Runnable接口；3.使用Callable和Future；4.使用线程池；

### 1.1.1. 继承Thread父类
1. 定义Thread类的子类，并重写该类的run()方法(线程执行体，线程需要完成的任务)；
2. 创建Thread子类的实例；
3. 启动线程，即调用线程的start()方法；

&emsp; **匿名类实现：创建一个Thread的匿名子类：**

```java
Thread thread = new Thread(){
    public void run(){
        System.out.println("Thread Running");
    }
};
thread.start();
```

### 1.1.2. Runnable接口
1. 定义Runnable接口的实现类，重写run()方法；
2. 创建Runnable实现类的实例，并用这个实例作为Thread的target来创建Thread对象，这个Thread对象才是真正的线程对象；
3. 调用线程对象的start()方法来启动线程；

&emsp; **匿名类实现：创建一个实现Runnable接口的匿名类，如下所示：**

```java
Runnable myRunnable = new Runnable(){
    public void run(){
        System.out.println("Runnable running");
    }
}
Thread thread = new Thread(myRunnable);
thread.start();
```

### 1.1.3. Callable接口
&emsp; Callable接口提供了一个call()方法作为线程执行体，call()方法比run()方法功能要强大。1.call()方法可以有返回值；2.call()方法可以声明抛出异常。



### 1.1.4. 三种创建线程的不同
&emsp; 实现Runnable和Callable接口的类只能当做一个可以在线程中运行的任务，不是真正意义上的线程，因此最后还需要通过Thread来调用。可以说任务是通过线程驱动从而执行的。实际上所有的多线程代码都是通过运行Thread的start()方法来运行的。  
&emsp; 实现接口和继承Thread的不同：实现Runnable接口、Callable接口避免了单继承的局限性。Callable支持返回多种类型的数据。  

----
## 1.2. 线程传参
&emsp; 在多线程的异步开发模式下，数据的传递和返回和同步开发模式有很大的区别。由于线程的运行和结束是不可预料的，因此，在传递和返回数据时就无法像函数一样通过函数参数和return语句来返回数据。

### 1.2.1. 构造方法
&emsp; 在创建线程时，必须要建立一个Thread类的或其子类的实例。因此可以在调用start方法之前通过线程类的构造方法将数据传入线程。并将传入的数据使用类变量保存起来，以便线程使用。

```java
public class MyThread extends Thread{
    private String name;
    public MyThread(String name){
        this.name = name;
    }
    public void run() {
        System.out.println("hello "+name);
    }
    public static void main(String[] args){
        Thread thread = new MyThread("world");
        thread.start();
    }
}
```
&emsp; 由于这种方法是在创建线程对象的同时传递数据的，因此，在线程运行之前这些数据就就已经到位了，这样就不会造成数据在线程运行后才传入的现象。如果要传递更复杂的数据，可以使用集合、类等数据结构。使用构造方法来传递数据虽然比较安全，但如果要传递的数据比较多时，就会造成很多不便。由于Java没有默认参数，要想实现类似默认参数的效果，就得使用重载，这样不但使构造方法本身过于复杂，又会使构造方法在数量上大增。因此，要想避免这种情况，就得通过类方法或类变量来传递数据。

### 1.2.2. 变量和方法
&emsp; 向对象中传入数据一般有两次机会，第一次机会是在建立对象时通过构造方法将数据传入，另外一次机会就是在类中定义一系列的public的方法或变量。然后在建立完对象后，通过对象实例逐个赋值。

```java
public class MyThread2 extends Thread{
    private String name;
    public void setName(String name){
        this.name = name;
    }
    public void run(){
        System.out.println("hello"+name);
    }
    public static void main(String[] args){
        MyThread2 myThread = new MyThread2();
        myThread.setName("world");
        Thread thread = new Thread(myThread);
        thread.start();
    }
}
```

### 1.2.3. 回调函数 
&emsp; 上面讨论的两种向线程中传递数据的方法是最常用的。但这两种方法都是main方法中主动将数据传入线程类的。这对于线程来说，是被动接收这些数据的。然而，在有些应用中需要在线程运行的过程中动态地获取数据，如在下面代码的run方法中产生了3个随机数，然后通过Work类的process方法求这三个随机数的和，并通过Data类的value将结果返回。从这个例子可以看出，在返回value之前，必须要得到三个随机数。也就是说，这个value是无法事先就传入线程类的。

```java
class Data{
    public int value = 0;
}

class Work{
    public void process(Data data, Integer numbers){
        for (int n : numbers){
            data.value += n;
        }
    }
}

public class MyThread3 extends Thread{
    private Work work;
    
    public MyThread3(Work work){
        this.work = work;
    }
    
    public void run(){
        java.util.Random random = new java.util.Random();
        Data data = new Data();
        int n1 = random.nextInt(1000);
        int n2 = random.nextInt(2000);
        int n3= random.nextInt(3000);
        work.process(data, n1, n2, n3);// 使用回调函数   
        System.out.println(String.valueOf(n1)+"+"+String.valueOf(n2)+
        "+"+String.valueOf(n3) + "=" + data.value);
    }
    public static void main(String[] args){
        Thread thread = new MyThread3(new Work());
        thread.start();
    }
}
```
