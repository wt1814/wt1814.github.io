
<!-- TOC -->

- [1. Thread类详解](#1-thread类详解)
    - [1.1. Thread.java的构造函数](#11-threadjava的构造函数)
    - [1.2. ★★★线程状态介绍(线程生命周期)](#12-★★★线程状态介绍线程生命周期)
    - [1.3. Thread.java的成员方法](#13-threadjava的成员方法)
        - [1.3.1. 线程的start方法和run方法的区别](#131-线程的start方法和run方法的区别)
        - [1.3.2. Thread.sleep()与Object.wait()](#132-threadsleep与objectwait)
        - [1.3.3. yield()，线程让步](#133-yield线程让步)
        - [1.3.4. ~~Join()方法~~](#134-join方法)
        - [1.3.5. interrupt()与stop()，中断线程](#135-interrupt与stop中断线程)
        - [1.3.6. 守护线程](#136-守护线程)
        - [1.3.7. 线程优先级](#137-线程优先级)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 6种线程状态：  

2. yield()，线程让步。 yield会使当前线程让出CPU执行时间片，与其他线程一起重新竞争CPU时间片。  
3. thread.join()把指定的线程加入到当前线程，可以将两个交替执行的线程合并为顺序执行的线程。比如在线程B中调用了线程A的Join()方法，直到线程A执行完毕后，才会继续执行线程B。  


# 1. Thread类详解  
<!-- 

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
&emsp; [线程状态](/docs/java/concurrent/threadState.md)  

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


### 1.3.4. ~~Join()方法~~
&emsp; thread.join()把指定的线程加入到当前线程，可以将两个交替执行的线程合并为顺序执行的线程。比如在线程B中调用了线程A的Join()方法，直到线程A执行完毕后，才会继续执行线程B。  
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
