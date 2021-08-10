
<!-- TOC -->

- [1. LockSupport](#1-locksupport)
    - [1.1. API](#11-api)
    - [1.2. 使用示例](#12-使用示例)
    - [1.3. park和unpark，获取许可](#13-park和unpark获取许可)
    - [1.4. park(Object blocker)](#14-parkobject-blocker)
    - [1.5. LockSupport对应中断的响应性](#15-locksupport对应中断的响应性)

<!-- /TOC -->


# 1. LockSupport
<!-- 

https://www.jianshu.com/p/f1f2cd289205
https://www.cnblogs.com/ldq2016/p/9045947.html
通俗易懂的JUC源码剖析-LockSupport
https://mp.weixin.qq.com/s/kh5GZCRPE9fGdKeNsoP2JA

-->

&emsp; LockSupport是一个线程阻塞工具类，所有的方法都是静态方法，可以让线程在任意位置阻塞，当然阻塞之后肯定得有唤醒的方法。  

&emsp; LockSupport有哪些常用的方法。主要有两类方法：park和unpark。  

## 1.1. API

```java
public static void park(Object blocker); // 暂停当前线程
public static void parkNanos(Object blocker, long nanos); // 暂停当前线程，不过有超时时间的限制
public static void parkUntil(Object blocker, long deadline); // 暂停当前线程，直到某个时间
public static void park(); // 无期限暂停当前线程
public static void parkNanos(long nanos); // 暂停当前线程，不过有超时时间的限制
public static void parkUntil(long deadline); // 暂停当前线程，直到某个时间
public static void unpark(Thread thread); // 恢复当前线程
public static Object getBlocker(Thread t);
```

## 1.2. 使用示例
<!--

https://www.jianshu.com/p/f1f2cd289205
-->


## 1.3. park和unpark，获取许可
<!--

https://www.jianshu.com/p/f1f2cd289205
-->
&emsp; 相对于线程的stop和resume，park和unpark的先后顺序并不是那么严格。stop和resume如果顺序反了，会出现死锁现象。而park和unpark却不会。这又是为什么呢？还是看一个例子  

```java
public class LockSupportDemo {

    public static Object u = new Object();
    static ChangeObjectThread t1 = new ChangeObjectThread("t1");

    public static class ChangeObjectThread extends Thread {

        public ChangeObjectThread(String name) {
            super(name);
        }

        @Override public void run() {
            synchronized (u) {
                System.out.println("in " + getName());
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LockSupport.park();
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("被中断了");
                }
                System.out.println("继续执行");
            }
        }
    }

    public static void main(String[] args) {
        t1.start();
        LockSupport.unpark(t1);
        System.out.println("unpark invoked");
    }
}
```

&emsp; t1内部有休眠1s的操作，所以unpark肯定先于park的调用，但是t1最终仍然可以完结。这是因为park和unpark会对每个线程维持一个许可（boolean值）  

* unpark调用时，如果当前线程还未进入park，则许可为true  
* park调用时，判断许可是否为true，如果是true，则继续往下执行；如果是false，则等待，直到许可为true  


&emsp; 再看看jdk的文档描述：  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-38.png)   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/concurrent-39.png)   

## 1.4. park(Object blocker)
&emsp; (Object blocker)就是方便在线程dump的时候看到具体的阻塞对象的信息。  

```text
"t1" #10 prio=5 os_prio=31 tid=0x00007f95030cc800 nid=0x4e03 waiting on condition [0x00007000011c9000]
   java.lang.Thread.State: WAITING (parking)
    at sun.misc.Unsafe.park(Native Method)
    at java.util.concurrent.locks.LockSupport.park(LockSupport.java:304)
    // `下面的这个信息`
    at com.wtuoblist.beyond.concurrent.demo.chapter3.LockSupportDemo$ChangeObjectThread.run(LockSupportDemo.java:23) // 
    - locked <0x0000000795830950> (a java.lang.Object)
```

## 1.5. LockSupport对应中断的响应性  
<!-- 
https://www.cnblogs.com/ldq2016/p/9045947.html
-->
