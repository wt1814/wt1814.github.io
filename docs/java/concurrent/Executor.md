



# 线程池框架Executor
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
