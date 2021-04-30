
<!-- TOC -->

- [1. 线程基本操作](#1-线程基本操作)
    - [1.1. 创建并运行java线程](#11-创建并运行java线程)
        - [1.1.1. 继承Thread父类](#111-继承thread父类)
        - [1.1.2. Runnable接口](#112-runnable接口)
        - [1.1.3. Callable接口](#113-callable接口)
        - [1.1.4. Future接口](#114-future接口)
        - [1.1.5. FutureTask](#115-futuretask)
        - [1.1.6. 三种创建线程的不同](#116-三种创建线程的不同)
    - [1.2. 线程传参](#12-线程传参)
        - [1.2.1. 构造方法](#121-构造方法)
        - [1.2.2. 变量和方法](#122-变量和方法)
        - [1.2.3. 回调函数](#123-回调函数)
    - [1.3. CompletionService](#13-completionservice)
    - [1.4. CompletableFuture](#14-completablefuture)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; **Future是一个接口，它可以对具体的Runnable或者Callable任务进行取消、判断任务是否已取消、查询任务是否完成、获取任务结果。**  
&emsp; Java5为Future接口提供了一个实现类FutureTask，表示一个可以取消的异步运算。它有启动和取消运算、查询运算是否完成和取回运算结果等方法。  

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

### 1.1.4. Future接口
<!-- 
 阿里架构师教你JUC-Future与FutureTask原理详解 
 https://mp.weixin.qq.com/s/HJqHMzzosCvYgv7JkgRMHQ


什么是 Callable 和Future?  
Future 接口表示异步任务，是还没有完成的任务给出的未来结果。所以说 Callable 用于产生结果， Future 用于获取结果。

什么是 FutureTask?使用 ExecutorService 启动任务。
在 Java 并发程序中 FutureTask 表示一个可以取消的异步运算。它有启动和取消运算、查询运算是否完成和取回运算结果等方法。只有当运算完成的时候结果才能取回，如果运算尚未完成 get 方法将会阻塞。一个 FutureTask 对象可以对调用了 Callable 和 Runnable 的对象进行包装， 由于 FutureTask 也是调用了 Runnable 接口所以它可以提交给Executor 来执行。

FutureTask 是什么
这个其实前面有提到过，FutureTask 表示一个异步运算的任务。FutureTask 里面可以传入一个 Callable 的具体实现类， 可以对这个异步运算的任务的结果进行等待获取、判断是否已经完成、取消任务等操作。当然， 由于 FutureTask 也是
Runnable 接口的实现类， 所以 FutureTask 也可以放入线程池中。
-->
&emsp; **Future是一个接口，它可以对具体的Runnable或者Callable任务进行取消、判断任务是否已取消、查询任务是否完成、获取任务结果。** 如果是Runnable的话返回的结果是null(下面会剖析为什么Runnable的任务，Future还能返回结果)。接口里面有以下几个方法。注意两个get方法都会阻塞当前调用get的线程，直到返回结果或者超时才会唤醒当前的线程。

&emsp; 在Future接口里定义了几个公共方法来控制它关联的任务。

```java
//视图取消该Future里面关联的Callable任务
boolean cancel(boolean mayInterruptIfRunning);
//返回Callable里call()方法的返回值，调用这个方法会导致程序阻塞，必须等到子线程结束后才会得到返回值
V get();
//返回Callable里call()方法的返回值，最多阻塞timeout时间，经过指定时间没有返回，抛出TimeoutException
V get(long timeout,TimeUnit unit);
//若Callable任务完成，返回True
boolean isDone();
//如果在Callable任务正常完成前被取消，返回True
boolean isCancelled();
```
&emsp; **<font color = "red">注：Future设置方法超时，使用get(long timeout,TimeUnit unit)方法</font>**

### 1.1.5. FutureTask
&emsp; Java5为Future接口提供了一个实现类FutureTask，表示一个可以取消的异步运算。它有启动和取消运算、查询运算是否完成和取回运算结果等方法。只有当运算完成的时候结果才能取回，如果运算尚未完成get方法将会阻塞。FutureTask既实现了Future接口，还实现了Runnable接口，因此可以作为Thread类的target。

<!-- 
&emsp; 因为Future只是一个接口，所以是无法直接用来创建对象使用的，因此就有了下面的FutureTask。  
&emsp; FutureTask不是接口，是个class。它实现了RunnableFuture接口
-->
```java
public class FutureTask<V> implements RunnableFuture<V>
```

&emsp; 而RunnableFuture接口又继承了Runnable和Future

```java
public interface RunnableFuture<V> extends Runnable, Future<V>
```
&emsp; 因此它可以作为Runnable被线程执行，又可以有Future的那些操作。它的两个构造器如下：

```java
public FutureTask(Callable<V> callable) {
    //...
}

public FutureTask(Runnable runnable, V result) {
    //...
}
```

&emsp; 使用步骤：
1. 创建Callable接口的实现类，并实现call()方法，该call()方法将作为线程执行体，并且有返回值。
2. 创建Callable实现类的实例，使用FutureTask类来包装Callable对象，该FutureTask对象封装了该Callable对象的call()方法的返回值（从java8开始可以直接使用Lambda表达式创建Callable对象）。
3. **使用FutureTask对象作为Thread对象的target创建并启动新线程。**
4. 调用FutureTask对象的get()方法来获得子线程执行结束后的返回值。

&emsp; 匿名类实现：

```java
FutureTask<String> ft = new FutureTask<String>(new Callable<String>() {
@Override
public String call() throws Exception {
    System.out.println("new Thread 3");
    return "aaaa";
}
});

Thread t3 = new Thread(ft);
t3.start();
String result = ft.get();
System.out.println(result);//输出:aaaa
```

### 1.1.6. 三种创建线程的不同
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

## 1.3. CompletionService  
&emsp; [CompletionService](/docs/java/concurrent/CompletionService.md)  

## 1.4. CompletableFuture
&emsp; [CompletableFuture](/docs/java/concurrent/CompletableFuture.md)  
