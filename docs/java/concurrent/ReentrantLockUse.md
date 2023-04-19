

<!-- TOC -->

- [1. ReentrantLock，重入锁](#1-reentrantlock重入锁)
    - [1.1. ReentrantLock与synchronized比较](#11-reentrantlock与synchronized比较)
    - [1.2. 使用示例](#12-使用示例)

<!-- /TOC -->

# 1. ReentrantLock，重入锁
<!--
ReentrantLock
https://mp.weixin.qq.com/s/EALE52sIS7OH4bIRTczPPw
重入锁的核心功能委托给内部类Sync实现，并且根据是否是公平锁有FairSync和NonfairSync两种实现。这是一种典型的策略模式。
https://mp.weixin.qq.com/s/GDno-X1N8zc98h9MZ8_KoA
-->

&emsp; ReentrantLock(Re-Entrant-Lock)，可重入互斥锁，具有与synchronized隐式锁相同的基本行为和语义，但扩展了功能。  

## 1.1. ReentrantLock与synchronized比较 
<!-- 
到底什么是重入锁？拜托，一次搞清楚！ 
https://mp.weixin.qq.com/s/c70bsOVJxtXMiJijkfzLzQ
-->
&emsp; Java提供了两种锁机制来控制多个线程对共享资源的互斥访问，第一个是JVM实现的synchronized，而另一个是JDK实现的ReentrantLock。  
&emsp; ReentrantLock与synchronized的联系：Lock接口提供了与synchronized关键字类似的同步功能，但需要在使用时手动获取锁和释放锁。ReentrantLock和synchronized都是可重入的互斥锁。  
&emsp; **<font color = "red">Lock接口与synchronized关键字的区别(Lock的优势全部体现在构造函数、方法中)：</font>**  
1. （支持非公平）ReenTrantLock可以指定是公平锁还是非公平锁。而synchronized只能是非公平锁。所谓的公平锁就是先等待的线程先获得锁。  
2. Lock接口可以尝试非阻塞地获取锁，当前线程尝试获取锁。如果这一时刻锁没有被其他线程获取到，则成功获取并持有锁。  
3. （可被中断）Lock接口能被中断地获取锁，与synchronized不同，获取到锁的线程能够响应中断，当获取到的锁的线程被中断时，中断异常将会被抛出，同时锁会被释放。可以使线程在等待锁的时候响应中断；  
4. （支持超时/限时等待）Lock接口可以在指定的截止时间之前获取锁，如果截止时间到了依旧无法获取锁，则返回。可以让线程尝试获取锁，并在无法获取锁的时候立即返回或者等待一段时间；  
5. （可实现选择性通知，锁可以绑定多个条件）ReenTrantLock提供了一个Condition(条件)类，用来实现分组唤醒需要唤醒的一些线程，而不是像synchronized要么随机唤醒一个线程要么唤醒全部线程。  


&emsp; **什么时候选择用ReentrantLock代替synchronized？**  
&emsp; 在确实需要一些synchronized所没有的特性的时候，比如时间锁等候、可中断锁等候、无块结构锁、多个条件变量或者锁投票。ReentrantLock还具有可伸缩性的好处，应当在高度争用的情况下使用它，但是请记住，大多数synchronized块几乎从来没有出现过争用，所以可以把高度争用放在一边。建议用synchronized开发，直到确实证明synchronized不合适，而不要仅仅是假设如果使用ReentrantLock“性能会更好”。  

## 1.2. 使用示例  
&emsp; 在使用重入锁时，一定要在程序最后释放锁。一般释放锁的代码要写在finally里。否则，如果程序出现异常，Lock就永远无法释放了。(synchronized的锁是JVM最后自动释放的。)  

```java
private final ReentrantLock lock = new ReentrantLock();

try {
if (lock.tryLock(5, TimeUnit.SECONDS)) { //如果已经被lock，尝试等待5s，看是否可以获得锁，如果5s后仍然无法获得锁则返回false继续执行
    // lock.lockInterruptibly();可以响应中断事件
    try {
        //操作
    } finally {
        lock.unlock();
    }
}
} catch (InterruptedException e) {
    e.printStackTrace(); //当前线程被中断时(interrupt)，会抛InterruptedException
}
```
