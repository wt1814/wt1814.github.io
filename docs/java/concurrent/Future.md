
<!-- TOC -->

- [1. Future相关](#1-future相关)
    - [1.1. Future接口](#11-future接口)
    - [1.2. FutureTask类](#12-futuretask类)
    - [1.3. CompletableFuture](#13-completablefuture)
    - [1.4. CompletionService](#14-completionservice)

<!-- /TOC -->


# 1. Future相关
## 1.1. Future接口
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

## 1.2. FutureTask类
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


## 1.3. CompletableFuture
&emsp; [CompletableFuture](/docs/java/concurrent/CompletableFuture.md)  


## 1.4. CompletionService  
&emsp; [CompletionService](/docs/java/concurrent/CompletionService.md)  

