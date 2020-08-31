
1. 线程池：  
    1. ForkJoinPool与CompletableFuture。  

1. 并发容器：  
    1. 什么是变量？为什么需要变量？使用什么样的变量（全局、成员、局部）？  
    2. 什么时候使用并发容器？选择哪个并发容器？  
        1. CopyOnWriteArrayList，读多写少（一些相对固定的数据），例如黑白名单、配置信息。  

2. tools：  
    1. CyclicBarrier，回环栅栏，多线程结果合并，例如操作多份文件然后合并。  


<!-- 
java多线程累加计数
https://blog.csdn.net/wzmde007/article/details/79641084


找出5亿个整数的文件中的重复数

-->


    