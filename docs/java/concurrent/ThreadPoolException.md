
<!-- TOC -->

- [1. ~~线程池的异常~~](#1-线程池的异常)
    - [1.1. 线程池执行过程中遇到异常会发生什么？](#11-线程池执行过程中遇到异常会发生什么)
        - [1.1.1. 异常处理问题](#111-异常处理问题)
        - [1.1.2. 线程遇到未处理的异常就结束了](#112-线程遇到未处理的异常就结束了)
        - [1.1.3. 线程池中线程频繁出现未捕获异常](#113-线程池中线程频繁出现未捕获异常)
    - [1.2. 异常处理](#12-异常处理)
        - [1.2.1. 直接catch](#121-直接catch)
        - [1.2.2. 线程池实现](#122-线程池实现)
            - [1.2.2.1. 自定义线程池](#1221-自定义线程池)
            - [1.2.2.2. 实现Thread.UncaughtExceptionHandler接口](#1222-实现threaduncaughtexceptionhandler接口)
            - [1.2.2.3. 继承ThreadGroup](#1223-继承threadgroup)
            - [1.2.2.4. 采用Future模式](#1224-采用future模式)
    - [1.3. 总结](#13-总结)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 线程池未处理异常：
    1. 线程遇到未处理的异常就结束了。  
    2. 当线程池中线程频繁出现未捕获的异常，那线程的复用率就大大降低了，需要不断地创建新线程。  
2. 线程池中线程中异常尽量手动捕获。  

# 1. ~~线程池的异常~~
<!-- 
****
https://mp.weixin.qq.com/s/7pQoC9PSqN43sbfhr0IUmA
-->

## 1.1. 线程池执行过程中遇到异常会发生什么？   
### 1.1.1. 异常处理问题  
&emsp; java线程池ThreadPoolExecutor，真正执行代码的部分是runWorker()方法。  

```java
final void runWorker(Worker w) {
    //...
    try {
        beforeExecute(wt, task);
        Throwable thrown = null;
        try {
            task.run();//执行程序逻辑
        } catch (RuntimeException x) {//捕获RuntimeException
            thrown = x; throw x; //抛出异常
        } catch (Error x) {
            thrown = x; throw x;
        } catch (Throwable x) {
            thrown = x; throw new Error(x);
        } finally {
            afterExecute(task, thrown);//执行后续逻辑
        }
    } finally {
        task = null;
        w.completedTasks++;
        w.unlock();
    }
    //...
}
```
&emsp; 程序会捕获包括Error在内的所有异常，并且在程序最后，将出现过的异常和当前任务传递给afterExecute方法。而ThreadPoolExecutor中的afterExecute方法是没有任何实现的。  

&emsp; ThreadPoolExecutor这种处理方式会有什么问题？  
&emsp; 这样做能够保证提交的任务抛出了异常不会影响其他任务的执行，同时也不会对用来执行该任务的线程产生任何影响。然而afterExecute()没有做任何处理，所以如果任务抛出了异常，也无法立刻感知到。 即使感知到了，也无法查看异常信息。  
&emsp; 解决方案：  

* 在提交的任务中将异常捕获并处理，不抛给线程池。  
* 异常抛给线程池，但是要及时处理抛出的异常。  

### 1.1.2. 线程遇到未处理的异常就结束了
&emsp; 这个好理解，当线程出现未捕获异常的时候就执行不下去了，留给它的就是垃圾回收了。

### 1.1.3. 线程池中线程频繁出现未捕获异常
&emsp; 当线程池中线程频繁出现未捕获的异常，那线程的复用率就大大降低了，需要不断地创建新线程。

&emsp; 做个实验：

```java
public class ThreadExecutor {

 private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
   new ArrayBlockingQueue<>(200), new ThreadFactoryBuilder().setNameFormat("customThread %d").build());

 @Test
 public void test() {
  IntStream.rangeClosed(1, 5).forEach(i -> {
   try {
    Thread.sleep(100);
   } catch (InterruptedException e) {
    e.printStackTrace();
   }
   threadPoolExecutor.execute(() -> {
     int j = 1/0;
  });});
 }
}
```

&emsp; 新建一个只有一个线程的线程池，每隔0.1s提交一个任务，任务中是一个1/0的计算。

```java
Exception in thread "customThread 0" java.lang.ArithmeticException: / by zero
 at thread.ThreadExecutor.lambda$null$0(ThreadExecutor.java:25)
 at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
 at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
 at java.lang.Thread.run(Thread.java:748)
Exception in thread "customThread 1" java.lang.ArithmeticException: / by zero
 at thread.ThreadExecutor.lambda$null$0(ThreadExecutor.java:25)
 at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
 at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
 at java.lang.Thread.run(Thread.java:748)
Exception in thread "customThread 2" java.lang.ArithmeticException: / by zero
 at thread.ThreadExecutor.lambda$null$0(ThreadExecutor.java:25)
 at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
 at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
 at java.lang.Thread.run(Thread.java:748)
Exception in thread "customThread 3" java.lang.ArithmeticException: / by zero
 at thread.ThreadExecutor.lambda$null$0(ThreadExecutor.java:25)
 at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
 at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
 at java.lang.Thread.run(Thread.java:748)
Exception in thread "customThread 4" java.lang.ArithmeticException: / by zero
 at thread.ThreadExecutor.lambda$null$0(ThreadExecutor.java:25)
 at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
 at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
 at java.lang.Thread.run(Thread.java:748)
Exception in thread "customThread 5" java.lang.ArithmeticException: / by zero
 at thread.ThreadExecutor.lambda$null$0(ThreadExecutor.java:25)
 at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
 at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
 at java.lang.Thread.run(Thread.java:748)
```

&emsp; 可见每次执行的线程都不一样，之前的线程都没有复用。原因是因为出现了未捕获的异常。

## 1.2. 异常处理

### 1.2.1. 直接catch  
&emsp; 提交的任务，将所有可能的异常都Catch住，并且自行处理。  

### 1.2.2. 线程池实现  
&emsp; 有以下四种实现方式。  

#### 1.2.2.1. 自定义线程池  
&emsp; 自定义线程池，继承ThreadPoolExecutor并复写其afterExecute(Runnable r, Throwable t)方法。  
![image](http://www.wt1814.com/static/view/images/java/concurrent/threadPool-7.png)  

#### 1.2.2.2. 实现Thread.UncaughtExceptionHandler接口  
&emsp; 实现Thread.UncaughtExceptionHandler接口，实现void uncaughtException(Thread t, Throwable e)方法，并将该handler传递给线程池的ThreadFactory。  
![image](http://www.wt1814.com/static/view/images/java/concurrent/threadPool-8.png)  

#### 1.2.2.3. 继承ThreadGroup  
&emsp; 覆盖uncaughtException方法。(与实现Thread.UncaughtExceptionHandler接口类似，因为ThreadGroup类本身就实现了Thread.UncaughtExceptionHandler接口)  
![image](http://www.wt1814.com/static/view/images/java/concurrent/threadPool-9.png)  
&emsp; 注意：上面三种方式针对的都是通过execute(xx)的方式提交任务，如果提交任务用的是submit()方法，那么上面的三种方式都将不起作用，而应该使用下面的方式。  

#### 1.2.2.4. 采用Future模式  
&emsp; <font color = "red">如果提交任务的时候使用的方法是submit，那么该方法将返回一个Future对象，所有的异常以及处理结果都可以通过future对象获取。</font>  
&emsp; 采用Future模式，将返回结果以及异常放到Future中，在Future中处理  
![image](http://www.wt1814.com/static/view/images/java/concurrent/threadPool-11.png)  

## 1.3. 总结
&emsp; 1、线程池中线程中异常尽量手动捕获  
&emsp; 2、通过设置ThreadFactory的UncaughtExceptionHandler可以对未捕获的异常做保底处理，通过execute提交任务，线程依然会中断，而通过submit提交任务，可以获取线程执行结果，线程异常会在get执行结果时抛出。  

