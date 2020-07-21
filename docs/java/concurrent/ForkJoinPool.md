

<!-- TOC -->

- [1. ForkJoinPool](#1-forkjoinpool)
    - [1.1. ForkJoinPool内部原理，工作窃取算法](#11-forkjoinpool内部原理工作窃取算法)
        - [1.1.1. 分而治之](#111-分而治之)
        - [1.1.2. 工作窃取(Work Stealing)算法](#112-工作窃取work-stealing算法)
    - [1.2. API 描述](#12-api-描述)
        - [1.2.1. 创建ForkJoinPool:](#121-创建forkjoinpool)
        - [1.2.2. ForkJoinTask详解](#122-forkjointask详解)
        - [1.2.3. 相关使用](#123-相关使用)

<!-- /TOC -->


# 1. ForkJoinPool  
&emsp; ForkJoinPool是java 7中新增的线程池类。  
&emsp; 为什么使用ForkJoinPool？  
&emsp; ThreadPoolExecutor中每个任务都是由单个线程独立处理的，如果出现一个非常耗时的大任务(比如大数组排序)，就可能出现线程池中只有一个线程在处理这个大任务，而其他线程却空闲着，这会导致CPU负载不均衡：空闲的处理器无法帮助工作繁忙的处理器。  
&emsp; ForkJoinPool就是用来解决这种问题的：将一个大任务拆分成多个小任务后，使用fork可以将小任务分发给其他线程同时处理，使用join可以将多个线程处理的结果进行汇总；这实际上就是分治思想的并行版本。  

## 1.1. ForkJoinPool内部原理，工作窃取算法
&emsp; ForkJoinPool的两大核心是分而治之(Divide and conquer)和工作窃取(Work Stealing)算法。  

### 1.1.1. 分而治之  
&emsp; ForkJoinPool的计算方式：大任务拆中任务，中任务拆小任务，最后再汇总。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-12.png)   

### 1.1.2. 工作窃取(Work Stealing)算法  
&emsp; ForkJoinPool会把大任务拆分成多个子任务，但是ForkJoinPool并不会为每个子任务创建单独的线程。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-13.png)   

```java
public class ForkJoinWorkerThread extends Thread {
    final ForkJoinPool pool;// 工作线程所在的线程池
    final ForkJoinPool.WorkQueue workQueue; // 线程的工作队列
}
```
&emsp; 每个工作线程都有自己的工作队列WorkQueue。这是一个双端队列，它是线程私有的。双端队列的操作：push、pop、poll。push/pop只能被队列的所有者线程调用，而poll是由其它线程窃取任务时调用的。  
&emsp; ForkJoinTask中fork的子任务，将放入运行该任务的工作线程的队头，工作线程将以LIFO的顺序来处理工作队列中的任务；  
&emsp; 为了最大化地利用CPU，空闲的线程将随机从其它线程的队列中“窃取”任务来执行。从工作队列的尾部窃取任务，以减少竞争；  
&emsp; 当只剩下最后一个任务时，还是会存在竞争，是通过CAS来实现的；  

## 1.2. API 描述
&emsp; ForkJoinPool继承体系  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-6.png)  
&emsp; ForkJoinPool和ThreadPoolExecutor都是继承AbstractExecutorService抽象类，所以它和ThreadPoolExecutor的使用几乎没有多少区别，除了任务变成了ForkJoinTask以外。   
&emsp; 注意：这里运用到了一种很重要的设计原则——开闭原则——对修改关闭，对扩展开放。可见整个线程池体系一开始的接口设计就很好，新增一个线程池类，不会对原有的代码造成干扰，还能利用原有的特性。  

### 1.2.1. 创建ForkJoinPool: 
&emsp; ForkJoinPool是ExecutorService的实现类，是一种特殊的线程池。  

&emsp; java 8进一步扩展了ForkJoinPool的功能，为ForkJoinPool增加了通用池功能。

&emsp; 创建了ForkJoinPool实例，就可以调用ForkJoinPool的submit(ForkJoinTask task) 或者invoke(ForkJoinTask task) 方法来执行指定的任务。   

### 1.2.2. ForkJoinTask详解
&emsp; ForkJoinTask代表一个可以并行、合并的任务。ForkJoinTask是一个抽象类，三个抽象子类：RecursiveAction无返回值任务、RecursiveTask有返回值任务、CountedCompleter无返回值任务，完成任务后可以触发回调。  
&emsp; 两个主要方法：  

* fork()：fork()方法类似于线程的Thread.start()方法，但是它不是真的启动一个线程，而是将任务放入到工作队列中。  
* join()：join()方法类似于线程的Thread.join()方法，但是它不是简单地阻塞线程，而是利用工作线程运行其它任务。当一个工作线程中调用了join()方法，它将处理其它任务，直到注意到目标子任务已经完成了。  
  
### 1.2.3. 相关使用  
&emsp; <font color = "red">在JDK8中lamdba有个stream操作parallelStream，底层是使用ForkJoinPool实现的；</font>  
&emsp; 可以通过Executors.newWorkStealingPool(int parallelism)快速创建ForkJoinPool线程池，无参默认使用CPU数量的线程数执行任务；  

