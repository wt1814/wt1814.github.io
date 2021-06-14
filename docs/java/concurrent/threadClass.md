
<!-- TOC -->

- [1. Thread类详解](#1-thread类详解)
    - [1.1. Thread.java的构造函数](#11-threadjava的构造函数)
    - [1.2. ★★★线程状态介绍(线程生命周期)](#12-★★★线程状态介绍线程生命周期)
        - [1.2.1. 线程有哪几种状态？](#121-线程有哪几种状态)
        - [1.2.2. 线程状态切换](#122-线程状态切换)
    - [1.3. Thread.java的成员方法](#13-threadjava的成员方法)
        - [1.3.1. 线程的start方法和run方法的区别](#131-线程的start方法和run方法的区别)
        - [1.3.2. Thread.sleep()与Object.wait()](#132-threadsleep与objectwait)
        - [1.3.3. yield()，线程让步](#133-yield线程让步)
        - [1.3.4. Join()方法](#134-join方法)
        - [1.3.5. interrupt()与stop()，中断线程](#135-interrupt与stop中断线程)
            - [1.3.5.1. Java中对线程中断所提供的API支持](#1351-java中对线程中断所提供的api支持)
            - [1.3.5.2. ★★★线程在不同状态下对于中断所产生的反应](#1352-★★★线程在不同状态下对于中断所产生的反应)
        - [1.3.6. 守护线程](#136-守护线程)
        - [1.3.7. 线程优先级](#137-线程优先级)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 线程状态：新建、就绪、阻塞（等待阻塞(o.wait)、同步阻塞(lock)、其他阻塞(sleep/join)）、等待、计时等待、终止。  
2. 中断  
&emsp; **<font color = "red">线程在不同状态下对于中断所产生的反应：</font>**    
    * NEW和TERMINATED对于中断操作几乎是屏蔽的；  
    * RUNNABLE和BLOCKED类似， **<font color = "cclime">对于中断操作只是设置中断标志位并没有强制终止线程，对于线程的终止权利依然在程序手中；</font>**  
    * WAITING/TIMED_WAITING状态下的线程对于中断操作是敏感的，它们会抛出异常并清空中断标志位。


# 1. Thread类详解  
<!-- 
线程不是你想中断就能中断 
https://mp.weixin.qq.com/s/wTZReOSthGONsOAqYgQFaA

为什么 Java 线程没有 Running 状态？一下被问懵！ 
https://mp.weixin.qq.com/s/_M_VkFDCdIiXokhzqsDT_A
-->

## 1.1. Thread.java的构造函数

```java
//
public Thread()
//
public Thread(Runnable target)
//
public Thread(Runnable target, AccessControlContext acc)
//
public Thread(ThreadGroup group, Runnable target)
//
public Thread(String name)
//
public Thread(ThreadGroup group, String name)
//
public Thread(Runnable target, String name)
//
public Thread(ThreadGroup group, Runnable target, String name)
//
public Thread(ThreadGroup group, Runnable target, String name, long stackSize)
```
&emsp; 线程名：创建一个线程，给线程起一个名字。有助于区分不同的线程。

```java
MyRunnable runnable = new MyRunnable();
Thread thread = new Thread(runnable, "New Thread");
thread.start();
System.out.println(thread.getName());
```
&emsp; 注：MyRunnable并非Thread的子类，所以MyRunnable类并没有getName()方法。可以通过以下方式得到当前线程的引用：Thread.currentThread()。因此，通过如下代码可以得到当前线程的名字，此方法可以获取任意方法所在的线程名称。

```java
String threadName = Thread.currentThread().getName();
```
&emsp; **线程组：ThreadGroup并不能提供对线程的管理，其主要功能是对线程进行组织。** 在构造Thread时，可以显示地指定线程的Group(ThreadGroup)。如果没有显示指定，子线程会被加入父线程所在的线程组(无论如何线程都会被加入某个Thread Group之中)。

## 1.2. ★★★线程状态介绍(线程生命周期)
### 1.2.1. 线程有哪几种状态？
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/thread-2.png)  
&emsp; Java线程状态均来自Thread类下的State这一内部枚举类中所定义的状态：

* 新建状态(NEW)：一个尚未启动的线程处于这一状态。用new语句创建的线程处于新建状态，此时它和其他Java对象一样，仅仅在堆区中被分配了内存，并初始化其成员变量的值。

    * new Thread()
<!-- Runnable (可运行/运行状态，等待CPU的调度)(要注意：即使是正在运行的线程，状态也是Runnable，而不是Running) -->
* 就绪状态(Runnable)：当一个线程对象创建后，其他线程调用它的start()方法，该线程就进入就绪状态，Java虚拟机会为它创建方法调用栈和程序计数器。处于这个状态的线程位于可运行池中，等待获得CPU的使用权。

    * 调用了thread.start()启动线程。
    * 被synchronized标记的代码，获取到同步监视器。
    * obj.notify()唤醒线程。
    * obj.notifyAll()唤醒线程。
    * obj.wait(time), thread.join(time)等待时间time耗尽。

* **<font color = "red">阻塞状态(BLOCKED)</font>：** **<font color = "clime">阻塞状态是指线程因为某些原因放弃CPU，暂时停止运行。</font>** 当线程处于阻塞状态时，Java虚拟机不会给线程分配CPU。直到线程重新进入就绪状态(获取监视器锁)，它才有机会转到运行状态。可分为以下3种：

    * **等待阻塞(o.wait->等待对列)：运行的线程执行wait()方法，JVM会把该线程放入等待池中。(wait会释放持有的锁)**
    * **同步阻塞(lock->锁池)：运行的线程在获取对象的同步锁时，若该同步锁被别的线程占用，则JVM会把该线程放入锁池(lock pool)中。**
    * **其他阻塞状态(sleep/join)：当前线程执行了sleep()方法，或者调用了其他线程的join()方法，或者发出了I/O请求时，就会进入这个状态。**

* **<font color = "red">WAITING(等待)：</font>** **<font color = "clime">一个正在无限期等待另一个线程执行一个特别的动作的线程处于这一状态。</font>**

    * threadA中调用threadB.join()，threadA将Waiting，直到threadB终止。
    * obj.wait() 释放同步监视器obj，并进入阻塞状态。

* <font color = "red">TIMED_WAITING (计时等待)：</font>一个正在限时等待另一个线程执行一个动作的线程处于这一状态。

    * threadA中调用threadB.join(time)。
    * obj.wait(time)
    * sleep(time)。

* TERMINATED (终止)：一个已经退出的线程处于这一状态。线程会以下面三种方式结束，结束后就是死亡状态。

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

### 1.2.2. 线程状态切换
&emsp; 线程状态切换图示  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/thread-1.png)  

&emsp; 代码演示：  
<!-- https://mp.weixin.qq.com/s/L2UqbdZQk7HvZ2r-M3eMlw -->
......

## 1.3. Thread.java的成员方法

| 名称 | 作用 |
| ---- | ---- | 
|currentThread()|返回对当前正在执行的线程对象的引用。静态方法。|
|getId()|返回此Thread的标识符。|
|getName()|返回此线程的名称。|
|getPriority()|返回此线程的优先级。|
|getState()|返回此线程的状态。|
|getThreadGroup()|返回此线程所属的线程组。|
|interrupt()|中断此线程。|
|join()|等待这个线程死亡。|
|setDaemon(boolean on)|将此线程标记为守护程序线程或用户线程。|
|setName(String name)|将此线程的名称更改为等于参数name。|
|setPriority(int newPriority)|更改此线程的优先级。|

<!-- 
Thread.join
https://www.jianshu.com/p/fc51be7e5bc0
-->

### 1.3.1. 线程的start方法和run方法的区别
&emsp; <font color = "red">调用start方法会创建一个新的线程并启动，run方法只是启动线程后的回调函数。</font>如果调用run方法，那么执行run方法的线程不会是新创建的线程，而如果使用start方法，那么执行run方法的线程就是刚刚启动的那个线程。

&emsp; 程序验证：

```java
public class Main {
    public static void main(String[] args) {
        Thread thread = new Thread(new SubThread());
        thread.run();
        thread.start();
    }

}
class SubThread implements Runnable{

    @Override
    public void run() {
        // TODO Auto-generated method stub
        System.out.println("执行本方法的线程:"+Thread.currentThread().getName());
    }

}
```
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/thread-3.png)

### 1.3.2. Thread.sleep()与Object.wait()
&emsp; Thead.sleep()和Object.wait()都可以让线程阻塞，也都可以指定超时时间，**甚至还都会抛出中断异常InterruptedException。**

&emsp; **<font color = "red">Thead.sleep()和Object.wait()的区别：</font>**

* 是否释放锁：<font color = "red">sleep()不释放锁；wait()释放锁。</font>
* 用途不同：wait通常被用于线程间交互/通信，sleep通常被用于暂停执行。
* 用法不同：wait()方法被调用后，线程不会自动苏醒，需要别的线程调用同一个对象上的notify()或者notifyAll()方法。sleep()方法执行完成后，线程会自动苏醒。或者可以使用wait(long timeout)超时后线程会自动苏醒。

### 1.3.3. yield()，线程让步
<!-- 
yield()方法的作用是放弃当前的CPU资源，将它让给其他的任务去占用CPU执行时 间。但放弃的时间不确定，有可能刚刚放弃，马上又获得CPU时间片。
-->
&emsp; yield会使当前线程让出CPU执行时间片，与其他线程一起重新竞争CPU时间片。一般情况下，优先级高的线程有更大的可能性成功竞争得到CPU时间片， 但这又不是绝对的，有的操作系统对线程优先级并不敏感。

```java
/**
 * 提示当前线程可以让处理器忽略当前线程，去处理其他线程
 * 它是一种启发式尝试，用于改善线程之间的相对进展，否则会过度利用CPU。它的使用应与详细的分析和基准测试相结合，以确保它实际上具有所需的效果。
 * 使用这种方法很少是合适的。它可能对调试或测试目的很有用，它可能有助于重现因竞争条件而产生的错误。在设计并发控制结构(如中的那些)时，它也可能很有用
 */
public static native void yield();
```
&emsp; yield() 这个方法从以上注释可以看出，也是一个休眠自身线程的方法，同样不会释放自身锁的标识，yield()方法只是使当前线程重新回到可执行状态，  
&emsp; 所以执行yield()的线程有可能在进入到可执行状态后马上又被执行，另外yield()方法只能使同优先级或者高优先级的线程得到执行机会，这也和sleep()方法不同。

&emsp; **<font color = "clime">wait()、sleep(long)、yield()的区别：</font>**

* wait()方法会释放CPU执行权和占有的锁。
* sleep(long)方法仅释放CPU使用权，<font color = "red">锁仍然占用，线程被放入超时等待队列</font>。与yield相比，它会使线程较长时间得不到运行。
* yield()方法仅释放CPU执行权，<font color = "red">锁仍然占用，线程会被放入就绪队列，会在短时间内再次执行</font>。

### 1.3.4. Join()方法
&emsp; 在很多情况下，主线程创建并启动子线程，如果子线程中要进行大量的耗时运算，主线程往往将早于子线程结束之前结束。这时，如果主线程想等待子线程执行完成之后再结束，比如子线程处理一个数据，主线程要取得这个数据中的值，就要用到join()方法了。方法join()的作用是等待线程对象销毁。  
&emsp; 方法join具有使线程排队运行的作用，有些类似同步的运行效果。join与synchronized的区别是：join在内部使用wait()方法进行等待，而sychronized关键字使用的是“对象监视器”原理做为同步。

### 1.3.5. interrupt()与stop()，中断线程
&emsp; 参考[线程停止与中断](/docs/java/concurrent/interrupt.md)   

### 1.3.6. 守护线程
&emsp; 线程分为用户线程、守护线程。线程初始化默认为用户线程；使用setDaemon()方法将一个线程设置为守护线程。main()属于非守护线程。

```java
Thread thread = new Thread(new MyRunnable());
thread.setDaemon(true);
```
&emsp; 守护线程唯一的用途就是为其他线程提供服务。当所有非守护线程结束时，程序也就终止，同时会杀死所有守护线程。计时线程、JVM的垃圾回收、内存管理等线程都是守护线程。

### 1.3.7. 线程优先级
&emsp; 线程的最小优先级，0；线程的最大优先级，10；线程的默认优先级，5。通过调用getPriority()和setPriority(int newPriority)方法来获得和设置线程的优先级。  
&emsp; 线程优先级特性：

* 继承性：比如A线程启动B线程，则B线程的优先级与A是一样的。
* 规则性：高优先级的线程总是大部分先执行完，但不代表高优先级线程全部先执行完。
* 随机性：优先级较高的线程不一定每一次都先执行完。 