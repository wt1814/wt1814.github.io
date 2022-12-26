
<!-- TOC -->

- [1. 线程池框架Executor](#1-线程池框架executor)
    - [1.1. 线程池简介](#11-线程池简介)
    - [1.2. 线程池框架Executor](#12-线程池框架executor)

<!-- /TOC -->


# 1. 线程池框架Executor
<!-- 

-->

## 1.1. 线程池简介
&emsp; **线程池通过线程复用机制，并对线程进行统一管理，** 具有以下优点：  

* 降低系统资源消耗。通过复用已存在的线程，降低线程创建和销毁造成的消耗；  
* 提高响应速度。当有任务到达时，无需等待新线程的创建便能立即执行；  
* 提高线程的可管理性。线程是稀缺资源，如果无限制的创建，不仅会消耗大量系统资源，还会降低系统的稳定性，使用线程池可以进行对线程进行统一的分配、调优和监控。  



## 1.2. 线程池框架Executor
![image](http://182.92.69.8:8081/img/java/concurrent/threadPool-2.png)   
![image](http://182.92.69.8:8081/img/java/concurrent/threadPool-21.png)   


&emsp; Executor：所有线程池的接口。  
&emsp; ExecutorService：扩展了Executor接口。添加了一些用来管理执行器生命周期和任务生命周期的方法。  
&emsp; ThreadPoolExecutor(创建线程池方式一)：线程池的具体实现类。  
&emsp; Executors(创建线程池方式二)：提供了一系列静态的工厂方法用于创建线程池，返回的线程池都实现了ExecutorService 接口。  

* Executor：线程池的最上层接口，提供了任务提交的基础方法。  
* ExecutorService：提供了线程池管理的上层接口，如池销毁、任务提交、异步任务提交。  
* ScheduledExecutorService：提供任务定时或周期执行方法的 ExecutorService。  
* AbstractExecutorService：为 ExecutorService 的任务提交方法提供了默认实现。  
* ThreadPoolExecutor：大名鼎鼎线程池类，提供线程和任务的调度策略。  
* ScheduledThreadPoolExecutor：属于线程池的一种，它可以允许任务延迟或周期执行，类似java的Timer。  
* ForkJoinPool：JDK1.7加入的成员，也是线程池的一种。只允许执行 ForkJoinTask 任务，它是为那些能够被递归地拆解成子任务的工作类型量身设计的。其目的在于能够使用所有可用的运算资源来提升应用性能。  
* Executors：创建各种线程池的工具类。  

&emsp; 简单来说，线程池分为三种：基础线程池ThreadPoolExecutor、延时任务线程池 ScheduledThreadPoolExecutor 和分治线程池ForkJoinPool。每种线程池中都有其支持的任务类型，后面我们在源码分析中，会穿插讲解各个线程池中遇到的各种任务类型。  

---------

&emsp; Executor框架由三个部分组成：  

* 工作任务：Runnable/Callable 接口
    * 工作任务就是Runnable/Callable接口的实现，可以被线程池执行
* 执行机制：Executor接口、ExecutorService接口、ScheduledExecutorService接口
    * ThreadPoolExecutor 是最核心的线程池实现，用来执行被提交的任务
    * ScheduledThreadPoolExecutor 是任务调度的线程池实现，可以在给定的延迟后运行命令，或者定期执行命令(它比Timer更灵活)
    * ForkJoinPool是一个并发执行框架
* 异步计算的结果：Future接口
    * 实现Future接口的FutureTask类，代表异步计算的结果


