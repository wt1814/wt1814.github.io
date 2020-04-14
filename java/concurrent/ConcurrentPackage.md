#并发API
&emsp; java.util.concurrent包是Java并发编程包。包括5部分：
* locks：显式锁相关；  
* atomic：原子变量类相关，是构建非阻塞算法的基础；  
* collections：并发容器相关；  
* executor：线程池相关；  
* tools：同步工具相关，如信号量、闭锁、栅栏等功能；  
![avatar](../../images/java/concurrent/concurrent-1.png)
##0. 基础算法、组件
###0.1. CAS算法：
