

1. 线程池：  
    1. ForkJoinPool与CompletableFuture。  

2. 并发容器：  
    1. 什么是变量？为什么需要变量？使用什么样的变量(全局、成员、局部)？  
    2. 什么时候使用并发容器？选择哪个并发容器？  
        1. CopyOnWriteArrayList，读多写少(一些相对固定的数据)，<font color = "red">例如黑白名单、配置信息。</font>  

3. tools：  
    1. CountdownLatch，线程计数器，一个线程等待其他线程。例如多线程累加计数。  
    2. CyclicBarrier，回环栅栏，多线程相互等待，多线程结果合并。例如操作多份文件然后合并。  

4. <font color = "red">多线程使用：</font>  
    1. 异步处理
    2. 多线程处理大量数据


<!-- 

并发编程的核心问题
https://mp.weixin.qq.com/s/RYF7dIp8fVKmXyaO5rHdjg
-->
