
<!-- TOC -->

- [1. ★★★线程状态介绍(线程生命周期)](#1-★★★线程状态介绍线程生命周期)
    - [1.1. 线程状态获取](#11-线程状态获取)
    - [1.2. 线程有哪几种状态？](#12-线程有哪几种状态)
        - [1.2.1. 通用的线程周期](#121-通用的线程周期)
        - [1.2.2. Java中线程的生命周期](#122-java中线程的生命周期)
    - [1.3. 线程状态切换](#13-线程状态切换)

<!-- /TOC -->

&emsp; 只有runnable到running时才会占用cpu时间片，其他都会出让cpu时间片。  
&emsp; 线程的资源有不少，但应该包含CPU资源和锁资源这两类。  

* sleep(long mills)：让出CPU资源，但是不会释放锁资源。  
* wait()：让出CPU资源和锁资源。  

&emsp; 锁是用来线程同步的，sleep(long mills)虽然让出了CPU，但是不会让出锁，其他线程可以利用CPU时间片了，但如果其他线程要获取sleep(long mills)拥有的锁才能执行，则会因为无法获取锁而不能执行，继续等待。  
&emsp; 但是那些没有和sleep(long mills)竞争锁的线程，一旦得到CPU时间片即可运行了。  


# 1. ★★★线程状态介绍(线程生命周期)
<!-- 
★★★★
https://juejin.cn/post/6986240210178670622
★★★★
https://www.codenong.com/cs105518429/
为什么 Java 线程没有 Running 状态？一下被问懵！ 
https://mp.weixin.qq.com/s/_M_VkFDCdIiXokhzqsDT_A
-->


<!-- 
~~
https://zhuanlan.zhihu.com/p/260373236
-->

## 1.1. 线程状态获取
<!-- 
JAVA使用JConsole工具查看线程状态
https://jingyan.baidu.com/article/ed15cb1b248bd05be2698116.html
-->



## 1.2. 线程有哪几种状态？
### 1.2.1. 通用的线程周期
<!-- 
https://www.jianshu.com/p/3e79ae25bfb6
-->
&emsp; 操作系统层面有5个状态，分别是：New（新建）、Runnable（就绪）、Running（运行）、Blocked（阻塞）、Dead（死亡）。


### 1.2.2. Java中线程的生命周期
&emsp; Java线程状态均来自Thread类下的State这一内部枚举类中所定义的状态：  
![image](http://182.92.69.8:8081/img/java/concurrent/thread-2.png)  
1. 新建状态（NEW）：一个尚未启动的线程处于这一状态。用new语句创建的线程处于新建状态，此时它和其他Java对象一样，仅仅在堆区中被分配了内存，并初始化其成员变量的值。
    * new Thread()
2. 就绪状态(Runnable)：当一个线程对象创建后，其他线程调用它的start()方法，该线程就进入就绪状态，Java虚拟机会为它创建方法调用栈和程序计数器。处于这个状态的线程位于可运行池中，等待获得CPU的使用权。<!-- Runnable (可运行/运行状态，等待CPU的调度)(要注意：即使是正在运行的线程，状态也是Runnable，而不是Running) -->  
    * 调用了thread.start()启动线程。
    * 被synchronized标记的代码，获取到同步监视器。
    * obj.notify()唤醒线程。
    * obj.notifyAll()唤醒线程。
    * obj.wait(time), thread.join(time)等待时间time耗尽。
3. **<font color = "red">阻塞状态（BLOCKED）</font>：** **<font color = "clime">阻塞状态是指线程因为某些原因`放弃CPU`，暂时停止运行。</font>** 当线程处于阻塞状态时，Java虚拟机不会给线程分配CPU。直到线程重新进入就绪状态(获取监视器锁)，它才有机会转到运行状态。可分为以下3种：
    * **等待阻塞(o.wait->等待对列)：运行的线程执行wait()方法，JVM会把该线程放入等待池中。(wait会释放持有的锁)**
    * **同步阻塞(lock->锁池)：运行的线程在获取对象的同步锁时，若该同步锁被别的线程占用，则JVM会把该线程放入锁池(lock pool)中。**
    * **其他阻塞状态(sleep/join)：当前线程执行了sleep()方法，或者调用了其他线程的join()方法，或者发出了I/O请求时，就会进入这个状态。**
4. **<font color = "red">等待状态（WAITING）：</font>** **<font color = "clime">一个正在无限期等待另一个线程执行一个特别的动作的线程处于这一状态。</font>**
    * threadA中调用threadB.join()，threadA将Waiting，直到threadB终止。
    * obj.wait() 释放同步监视器obj，并进入阻塞状态。
5. <font color = "red">计时等待（TIMED_WAITING）：</font>一个正在限时等待另一个线程执行一个动作的线程处于这一状态。
    * threadA中调用threadB.join(time)。
    * obj.wait(time)
    * sleep(time)。
6. 终止状态（TERMINATED）：一个已经退出的线程处于这一状态。线程会以下面三种方式结束，结束后就是死亡状态。
    * 正常结束：run()或 call()方法执行完成，线程正常结束。
    * 异常结束：线程抛出一个未捕获的Exception或Error。
    * 调用stop：直接调用该线程的stop()方法来结束该线程—该方法通常容易导致死锁，不推荐使用。

&emsp; 注意：由于wait()/wait(time)导致线程处于Waiting/TimedWaiting状态，当线程被notify()/notifyAll()/wait等待时间到之后，如果没有获取到同步监视器。会直接进入Blocked阻塞状态。

&emsp; ~~**线程阻塞BLOCKED和等待WAITING的区别**~~  
<!-- 
https://blog.csdn.net/zl18310999566/article/details/87931473
&emsp; <font color = "red">阻塞BLOCKED表示线程在等待对象的monitor锁，试图通过synchronized去获取某个锁，但是此时其他线程已经独占了monitor锁，那么当前线程就会进入等待状态WAITING。</font>  
-->
&emsp; 两者都会暂停线程的执行。两者的区别是：进入waiting状态是线程主动的，而进入blocked状态是被动的。更进一步的说，进入blocked状态是在同步(synchronized代码之外)，而进入waiting状态是在同步代码之内。  

## 1.3. 线程状态切换
&emsp; 线程状态切换图示：  
![image](http://182.92.69.8:8081/img/java/concurrent/thread-1.png)  
![image](http://182.92.69.8:8081/img/java/concurrent/thread-4.png)  
![image](http://182.92.69.8:8081/img/java/concurrent/thread-5.png)  



&emsp; 代码演示：  
<!-- https://mp.weixin.qq.com/s/L2UqbdZQk7HvZ2r-M3eMlw -->
......



