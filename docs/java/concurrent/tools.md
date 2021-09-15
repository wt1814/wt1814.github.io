


![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-9.png)  
&emsp; Java提供了同步工具类：[CountDownLatch（计数器）](/docs/java/concurrent/CountDownLatch.md)、[CyclicBarrier（栅栏）](/docs/java/concurrent/CyclicBarrier.md)、[Semaphore（信号量）](/docs/java/concurrent/Semaphore.md)、[Exchanger（交换器）](/docs/java/concurrent/Exchanger.md)，这几个工具类是为了能够更好控制线程之间的通讯问题。  


* 多个线程间相互协作完成任务：  
    * CountDownLatch，线程计数器，主线程等待辅线程完成各自的工作后再执行。  
    * CyclicBarrier，回环栅栏，多个线程互相等待，直到到达某个公共屏障点之后，再全部同时执行。  
* Semaphore，计数信号量，用于控制访问共享资源的线程的数目。  
* LockSupport是一个线程阻塞工具类，所有的方法都是静态方法，可以让线程在任意位置阻塞，当然阻塞之后肯定得有唤醒的方法。 

<!--
并发工具类Phaser、Exchanger使用 
https://mp.weixin.qq.com/s/6evcGMWJ8VSNh-lmYJEbrQ

https://mp.weixin.qq.com/s/JCen6ppvWYNDnB5KCsrNEA
https://mp.weixin.qq.com/s/Ib8lpezEmfDDh3Dy4Q6iDA

java中如何模拟真正的同时并发请求？
https://www.cnblogs.com/yougewe/p/9745198.html

多线程进阶－CyclicBarrier 源码超详细解析，学到就赚到 
https://mp.weixin.qq.com/s/odsutVotjJjXFX4nAhb54w

-->