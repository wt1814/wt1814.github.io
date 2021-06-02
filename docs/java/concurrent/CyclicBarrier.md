

<!-- TOC -->

- [1. ~~CyclicBarrier，回环栅栏~~](#1-cyclicbarrier回环栅栏)
    - [1.1. CyclicBarrier简介](#11-cyclicbarrier简介)
    - [1.2. API介绍](#12-api介绍)
    - [1.3. 使用示例](#13-使用示例)
    - [1.4. CycliBarriar和CountdownLatch有什么区别？](#14-cyclibarriar和countdownlatch有什么区别)

<!-- /TOC -->

# 1. ~~CyclicBarrier，回环栅栏~~  
<!--
https://blog.csdn.net/weixin_38481963/article/details/88070679
https://blog.csdn.net/weixin_38481963/article/details/88070679

CyclicBarrier 使用详解
https://www.jianshu.com/p/333fd8faa56e

栅栏(Barrier)类似于闭锁，它能阻塞一组线程直到某个事件发生[CPJ4,4.3]。栅栏与闭锁 的关键区别在于，所有线程必须同时到达栅栏位置，才能继续执行。闭锁用于等待事件，而栅 栏用于等待其他线程。栅栏用于实现一些协议，例如几个家庭决定在某个地方集合：“所有人 6:00在麦当劳碰头，到了以后要等其他人，之后再讨论下一步要做的事情。”

JUC并发编程之CyclicBarrier源码
https://mp.weixin.qq.com/s/qfJy9hre0_Ax6444nn2XTQ
-->
## 1.1. CyclicBarrier简介
&emsp; CyclicBarrier字面意思是回环栅栏， **<font color = "red">允许一组线程互相等待，直到到达某个公共屏障点 (common barrier point)之后，再全部同时执行。</font>** 叫做回环是因为当所有等待线程都被释放以后，CyclicBarrier可以被重用。  

**<font color = "clime">CyclicBarrier用途有两个：</font>**   

* 让一组线程等待至某个状态后再同时执行。
* 让一组线程等待至某个状态后，执行指定的任务。

&emsp; 这两个特点对应着CyclicBarrier的两个构造函数。  

```java
//创建对象的时候指定计算器大小
public CyclicBarrier(int parties) {
    this(parties, null);
}
//创建对象的时候指定计算器大小，在所有线程都运行到栅栏的时候，barrierAction会在其他线程恢复执行之前优先执行
public CyclicBarrier(int parties, Runnable barrierAction) {
    if (parties <= 0) throw new IllegalArgumentException();
    this.parties = parties;
    this.count = parties;
    this.barrierCommand = barrierAction;
}

```
<!-- 
&emsp; 应用场景： **<font color = "red"> CyclicBarrier适用于多线程结果合并的操作，用于多线程计算数据，最后合并计算结果的应用场景。</font>** <font color = "clime">比如需要统计多个Excel中的数据，然后等到一个总结果。</font>可以通过多线程处理每一个Excel，执行完成后得到相应的结果，最后通过barrierAction来计算这些线程的计算结果，得到所有Excel的总和。  

&emsp; 多线程协作处理大量数据，结合AtomicReferenceFieldUpdater，await后处理结果。 
-->

## 1.2. API介绍  
&emsp; **成员方法：**  

```java
//返回计数器数值
public int getParties() {
    
}
//返回当前在栅栏处等待的线程数目
public int getNumberWaiting() {

}
//线程在调用处等待，直到与计数器数值相同数量的线程都到调用此方法，所有线程恢复执行
//用来挂起当前线程，直至所有线程都到达 barrier 状态再同时执行后续任务
public int await() throws InterruptedException, BrokenBarrierException {

}
//让这些线程等待至一定的时间，如果还有线程没有到达 barrier 状态就直接让到达barrier的线程执行后续任务。
public int await(long timeout, TimeUnit unit)

}
//移除栅栏。执行本操作后可以继续在其他线程中使用await操作
public void reset() {

}
//
public boolean isBroken() {

}
```

## 1.3. 使用示例  
<!-- 
https://blog.csdn.net/weixin_38481963/article/details/88070679
-->

```java
public class CyclicBarrierExample {


    public static void main(String[] args) {
        final int totalThread = 10;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(totalThread);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < totalThread; i++) {
            executorService.execute(() -> {
                System.out.print("before..");
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.print("after..");
            });
        }
        executorService.shutdown();
    }
}
//    before..before..before..before..before..before..before..before..before..before..after..after..after..after..after..after..after..after..after..after..
```

## 1.4. CycliBarriar和CountdownLatch有什么区别？  

* <font color = "red">CountDownLatch的作用是允许1或N个线程等待其他线程完成执行；而CyclicBarrier则是允许N个线程相互等待。</font>  
* CountDownLatch的计数器无法被重置；CyclicBarrier的计数器可以被重置后使用，因此它被称为是循环的barrier。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/tools-1.png)


