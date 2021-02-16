

<!-- TOC -->

- [1. 线程池](#1-线程池)
    - [1.1. 线程池简介](#11-线程池简介)
    - [1.2. 线程池框架Executor](#12-线程池框架executor)
        - [1.2.1. 线程池实现](#121-线程池实现)
            - [1.2.1.1. ThreadPoolExecutor](#1211-threadpoolexecutor)
            - [1.2.1.2. ForkJoinPool](#1212-forkjoinpool)
            - [1.2.1.3. CompletableFuture](#1213-completablefuture)
            - [1.2.1.4. Executors](#1214-executors)
                - [1.2.1.4.1. SingleThreadExecutor](#12141-singlethreadexecutor)
                - [1.2.1.4.2. FixedThreadPool](#12142-fixedthreadpool)
                - [1.2.1.4.3. CachedThreadPool](#12143-cachedthreadpool)
                - [1.2.1.4.4. ScheduledThreadPool](#12144-scheduledthreadpool)
                - [1.2.1.4.5. 阿里巴巴禁用Executors创建线程池](#12145-阿里巴巴禁用executors创建线程池)
                - [1.2.1.4.6. newWorkStealingPool](#12146-newworkstealingpool)
            - [1.2.1.5. Java8使用lamda表达式创建线程池方式](#1215-java8使用lamda表达式创建线程池方式)
        - [1.2.2. 线程池执行，ExecutorService的API](#122-线程池执行executorservice的api)
            - [1.2.2.1. execute()，提交不需要返回值的任务](#1221-execute提交不需要返回值的任务)
            - [1.2.2.2. submit()，提交需要返回值的任务](#1222-submit提交需要返回值的任务)
    - [1.3. 线程池正确用法](#13-线程池正确用法)

<!-- /TOC -->

# 1. 线程池
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-1.png)  

## 1.1. 线程池简介
&emsp; **线程池通过线程复用机制，并对线程进行统一管理，** 具有以下优点：  

* 降低系统资源消耗。通过复用已存在的线程，降低线程创建和销毁造成的消耗；  
* 提高响应速度。当有任务到达时，无需等待新线程的创建便能立即执行；  
* 提高线程的可管理性。线程是稀缺资源，如果无限制的创建，不仅会消耗大量系统资源，还会降低系统的稳定性，使用线程池可以进行对线程进行统一的分配、调优和监控。  

## 1.2. 线程池框架Executor
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-2.png)   
&emsp; Executor：所有线程池的接口。  
&emsp; ExecutorService：扩展了Executor接口。添加了一些用来管理执行器生命周期和任务生命周期的方法。  
&emsp; ThreadPoolExecutor(创建线程池方式一)：线程池的具体实现类。  
&emsp; Executors(创建线程池方式二)：提供了一系列静态的工厂方法用于创建线程池，返回的线程池都实现了ExecutorService 接口。  

### 1.2.1. 线程池实现  
&emsp; 根据返回的对象类型，<font color = "red">创建线程池可以分为几类：ThreadPoolExecutor、ScheduleThreadPoolExecutor(任务调度线程池)、ForkJoinPool、CompletableFuture。</font>  

#### 1.2.1.1. ThreadPoolExecutor  
&emsp; [ThreadPoolExecutor](/docs/java/concurrent/ThreadPoolExecutor.md)  
  
#### 1.2.1.2. ForkJoinPool  
&emsp; [ForkJoinPool](F/docs/java/concurrent/ForkJoinPool.md)  

#### 1.2.1.3. CompletableFuture  
&emsp; [CompletableFuture](/docs/java/concurrent/CompletableFuture.md)  

#### 1.2.1.4. Executors
&emsp; Java通过Executors提供四种线程池。Executors将ThreadPoolExecutor的属性已经声明定义好了。  

##### 1.2.1.4.1. SingleThreadExecutor  

```java
public static ExecutorService newSingleThreadExecutor() {        
    return new FinalizableDelegatedExecutorService (
        new ThreadPoolExecutor(1, 1,                                    
        0L, TimeUnit.MILLISECONDS,                                    
        new LinkedBlockingQueue<Runnable>()));   
}
```
&emsp; 单线程的线程池。这个线程池只有一个核心线程在工作，也就是相当于单线程串行执行所有任务。如果这个唯一的线程因为异常结束，那么会有一个新的线程来替代它。此线程池保证所有任务的执行顺序按照任务的提交顺序执行。  
&emsp; 线程池特点：核心线程数为1、最大线程数也为1、阻塞队列是LinkedBlockingQueue、keepAliveTime为0。  
&emsp; 使用场景：适用于串行执行任务的场景，一个任务一个任务地执行。  

##### 1.2.1.4.2. FixedThreadPool  

```java
public static ExecutorService newFixedThreadPool(int nThreads) {         
        return new ThreadPoolExecutor(nThreads, nThreads,                                       
            0L, TimeUnit.MILLISECONDS,                                         
            new LinkedBlockingQueue<Runnable>());     
}
```
&emsp; 定长线程池，可控制线程最大并发数，超出的线程会在队列中等待。线程池的大小一旦达到最大值就会保持不变，如果某个线程因为执行异常而结束，那么线程池会补充一个新线程。  
&emsp; 定长线程池的大小最好根据系统资源进行设置。如Runtime.getRuntime().availableProcessors()。  
&emsp; 线程池特点：核心线程数和最大线程数大小一样、没有所谓的非空闲时间，即keepAliveTime为0、阻塞队列为无界队列LinkedBlockingQueue。  
&emsp; 使用场景：FixedThreadPool适用于处理CPU密集型的任务，确保CPU在长期被工作线程使用的情况下，尽可能的少的分配线程，即适用执行长期的任务。  

##### 1.2.1.4.3. CachedThreadPool  

```java
public static ExecutorService newCachedThreadPool() {         
    return new ThreadPoolExecutor(0,Integer.MAX_VALUE,                                           
           60L, TimeUnit.SECONDS,                                       
           new SynchronousQueue<Runnable>());     
}
```
&emsp; 无界线程池，可以进行自动线程回收。如果线程池的大小超过了处理任务所需要的线程，那么就会回收部分空闲(60秒不执行任务)的线程，当任务数增加时，此线程池又可以智能的添加新线程来处理任务。线程池为无限大，当执行第二个任务时第一个任务已经完成，会复用执行第一个任务的线程，而不用每次新建线程。  
&emsp; 此线程池不会对线程池大小做限制，线程池大小完全依赖于操作系统(或者说JVM)能够创建的最大线程大小。阻塞队列SynchronousQueue是一个是缓冲区为1的阻塞队列。  
&emsp; 线程池特点：核心线程数为0、最大线程数为Integer.MAX_VALUE、阻塞队列是SynchronousQueue、非核心线程空闲存活时间为60秒。  
&emsp; 当提交任务的速度大于处理任务的速度时，每次提交一个任务，就必然会创建一个线程。极端情况下会创建过多的线程，耗尽 CPU 和内存资源。由于空闲 60 秒的线程会被终止，长时间保持空闲的CachedThreadPool不会占用任何资源。  

##### 1.2.1.4.4. ScheduledThreadPool  

```java
public static ExecutorService newScheduledThreadPool(int corePoolSize) {         
    return new ScheduledThreadPool(corePoolSize, 
              Integer.MAX_VALUE,                                                  
              DEFAULT_KEEPALIVE_MILLIS, MILLISECONDS,                                                    
              new DelayedWorkQueue());    
}
```
&emsp; 核心线程池固定，大小无限的线程池。此线程池支持定时以及周期性执行任务的需求。  
&emsp; 工作机制：  
1. 添加一个任务  
2. 线程池中的线程从DelayQueue中取任务  
3. 线程从DelayQueue中获取time大于等于当前时间的task  
4. 执行完后修改这个task的time为下次被执行的时间  
5. 这个 task 放回DelayQueue队列中  

&emsp; 线程池特点：最大线程数为Integer.MAX_VALUE、阻塞队列是DelayedWorkQueue、keepAliveTime为0、scheduleAtFixedRate() ：按某种速率周期执行、scheduleWithFixedDelay()：在某个延迟后执行。  
&emsp; 使用场景：周期性执行任务的场景，需要限制线程数量的场景。  

##### 1.2.1.4.5. 阿里巴巴禁用Executors创建线程池
&emsp; 使用无界队列的线程池会导致内存飙升吗？  
&emsp; 使用无界队列的线程池会导致内存飙升。newFixedThreadPool使用了无界的阻塞队列LinkedBlockingQueue，如果线程获取一个任务后，任务的执行时间比较长(比如，上面demo设置了10秒)，会导致队列的任务越积越多，导致机器内存使用不停飙升，最终导致OOM。  

&emsp; Executors返回线程池对象的弊端如下：  

* FixedThreadPool和SingleThreadExecutor：允许请求的队列长度为Integer.MAX_VALUE，可能堆积大量的请求，从而导致OOM。
* CachedThreadPool和ScheduledThreadPool：允许创建的线程数量为Integer.MAX_VALUE，可能会创建大量线程，从而导致OOM。

##### 1.2.1.4.6. newWorkStealingPool  
&emsp; <font color = "red">在JDK8中lamdba有个stream操作parallelStream，底层是使用ForkJoinPool实现的；</font>  
&emsp; <font color = "red">可以通过Executors.newWorkStealingPool(int parallelism)快速创建ForkJoinPool线程池，无参默认使用CPU数量的线程数执行任务；</font>  

#### 1.2.1.5. Java8使用lamda表达式创建线程池方式

```java
ExecutorService executorService = Executors.newCachedThreadPool();
executorService.execute(()->{
    try{
        Thread.sleep(2000);
        System.out.println("Thread run");
    }
    catch(InterruptedException e){
        e.printStackTrace();
    }
});
```

### 1.2.2. 线程池执行，ExecutorService的API  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/pool-14.png)   

#### 1.2.2.1. execute()，提交不需要返回值的任务  
&emsp; void execute(Runnable command); execute()的参数是一个Runnable，也没有返回值。因此提交后无法判断该任务是否被线程池执行成功。  

```java
ExecutorService executor = Executors.newCachedThreadPool();
executor.execute(new Runnable() {
    @Override
    public void run() {
        //do something
    }
});
```

#### 1.2.2.2. submit()，提交需要返回值的任务  
```java
<T> Future<T> submit(Callable<T> task);  
<T> Future<T> submit(Runnable task, T result);  
Future<?> smit(Runnable task);
```
&emsp; submit()有三种重载，参数可以是Callable也可以是Runnable。同时它会返回一个Funture对象，通过它可以判断任务是否执行成功。获得执行结果调用Future.get()方法，这个方法会阻塞当前线程直到任务完成。  

```java
//提交一个Callable任务时，需要使用FutureTask包一层
FutureTask futureTask = new FutureTask(new Callable<String>(){ //创建Callable任务
    @Override
    public String call() throws Exception {
    String result = "";
    //do something
    return result;
    }
});
Future<?> submit = executor.submit(futureTask); //提交到线程池
try{
    Object result = submit.get();//获取结果
}catch(InterruptedException e) {
    e.printStackTrace();
}catch(ExecutionException e) {
    e.printStackTrace();
}
```

## 1.3. 线程池正确用法
&emsp; [线程池的正确使用](/docs/java/concurrent/ThreadPoolUse.md)  
