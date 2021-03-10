

<!-- TOC -->

- [1. Condition，等待/通知机制](#1-condition等待通知机制)
    - [1.1. Condition类提供的方法](#11-condition类提供的方法)
    - [1.2. Condition与wait/notify](#12-condition与waitnotify)
    - [1.3. 使用示例](#13-使用示例)

<!-- /TOC -->


# 1. Condition，等待/通知机制  
&emsp; 关键字synchronized与wait()和notify()/notifyAll()方法相结合可以实现等待/通知机制。ReentrantLock结合Condition也可以实现等待/通知机制。  
&emsp; Condition又称等待条件，它实现了对锁更精确的控制。

## 1.1. Condition类提供的方法  
&emsp; 等待方法：  

```java
// 当前线程进入等待状态，如果其他线程调用 condition 的 signal 或者 signalAll 方法并且当前线程获取 Lock 从 await 方法返回，如果在等待状态中被中断会抛出被中断异常
void await() throws InterruptedException
// 当前线程进入等待状态直到被通知，中断或者超时
long awaitNanos(long nanosTimeout)
// 同第二个方法，支持自定义时间单位
boolean await(long time, TimeUnit unit)throws InterruptedException
// 当前线程进入等待状态直到被通知，中断或者到了某个时间
boolean awaitUntil(Date deadline) throws InterruptedException
```

&emsp; 唤醒方法：  

```java
// 唤醒一个等待在 condition 上的线程，将该线程从等待队列中转移到同步队列中，如果在同步队列中能够竞争到 Lock 则可以从等待方法中返回
void signal()
// 与 1 的区别在于能够唤醒所有等待在 condition 上的线程
void signalAll()
```

## 1.2. Condition与wait/notify    
&emsp; Condition中的await()方法相当于Object的wait()方法，Condition中的signal()方法相当于Object的notify()方法，Condition中的signalAll()相当于Object的notifyAll()方法。  
&emsp;  **Condition 与 wait/notify的区别：**    
&emsp; 在使用notify()/notifyAll()方法进行通知时，被通知的线程是由JVM随机选择的。但使用ReentrantLock结合Condition类是可以实现“选择性通知”。  

|对比项| Condition | Object监视器 |  
| ---- | ---- | ---- |  
|使用条件|获取锁|获取锁，创建Condition对象| 
|等待队列的个数|一个|多个| 
|是否支持通知指定等待队列|支持|不支持| 
|是否支持当前线程释放锁进入等待状态|支持|支持| 
|是否支持当前线程释放锁并进入超时等待状态|支持|支持| 
|是否支持当前线程释放锁并进入等待状态直到指定最后期限|支持|不支持| 
|是否支持唤醒等待队列中的一个任务|支持|支持| 
|是否支持唤醒等待队列中的全部任务|支持|支持|   

## 1.3. 使用示例  
1. 启动waiter和signaler两个线程。  
2. waiter线程获取到锁，检查flag=false不满足条件，执行condition.await()方法将线程阻塞等待并释放锁。  
3. signaler线程获取到锁之后更改条件，将flag变为true，执行condition.signalAll()通知唤醒等待线程，释放锁。  
4. waiter线程被唤醒获取到锁，自旋检查flag=true满足条件，继续执行。  

```java
public class ConditionTest {
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition condition = lock.newCondition();
    private static volatile boolean flag = false;

    public static void main(String[] args) {
        Thread waiter = new Thread(new waiter());
        waiter.start();
        Thread signaler = new Thread(new signaler());
        signaler.start();
    }

    static class waiter implements Runnable {

        @Override
        public void run() {
            lock.lock();
            try {
                while (!flag) {
                    System.out.println(Thread.currentThread().getName() + "当前条件不满足等待");
                    try {
                        condition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(Thread.currentThread().getName() + "接收到通知条件满足");
            } finally {
                lock.unlock();
            }
        }
    }

    static class signaler implements Runnable {

        @Override
        public void run() {
            lock.lock();
            try {
                flag = true;
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }
}
```
&emsp; 输出结果：  

    Thread-0当前条件不满足等待
    Thread-0接收到通知，条件满足
