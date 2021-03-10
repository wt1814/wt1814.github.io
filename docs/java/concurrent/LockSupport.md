

# LockSupport
<!-- 

Java 线程中断(interrupt)与阻塞 (park)的区别
https://blog.csdn.net/higherzjm/article/details/82700264

https://www.jianshu.com/p/f1f2cd289205

通俗易懂的JUC源码剖析-LockSupport
https://mp.weixin.qq.com/s/kh5GZCRPE9fGdKeNsoP2JA
-->

&emsp; LockSupport是一个线程阻塞工具类，所有的方法都是静态方法，可以让线程在任意位置阻塞，当然阻塞之后肯定得有唤醒的方法。  
