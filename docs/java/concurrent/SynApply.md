

<!-- TOC -->

- [1. Synchronized介绍](#1-synchronized介绍)
    - [1.1. Synchronized简介](#11-synchronized简介)
    - [1.2. Synchronized与ReentrantLock](#12-synchronized与reentrantlock)
    - [1.3. Synchronized与Object#wait()](#13-synchronized与objectwait)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. ★★★Synchronized是解决线程安全的阻塞同步方案。Synchronized具有原子性、可见性、有序性、可重入性。  
2. ★★★Synchronized底层和优化。
3. ★★★Synchronized的使用：类锁和对象锁、和ReentrantLock比较、Object#wait()方法。 


# 1. Synchronized介绍  

## 1.1. Synchronized简介
&emsp; **<font color = "red">Synchronized能够保证在同一时刻最多只有一个线程执行该段代码。</font>**  

&emsp; **Synchronized的特性：**  

* 原子性：保证被Synchronized修饰的一个或者多个操作，在执行的过程中不会被任何的因素打断，即所谓的原子操作，直到锁被释放。  
* 可见性：保证持有锁的当前线程在释放锁之前，对共享变量的修改会刷新到主存中，并对其它线程可见。  
* 有序性：保证多线程时刻中只有一个线程执行，线程执行的顺序都是有序的。  
* 可重入性：保证在多线程中，有其他的线程试图竞争持有锁的临界资源时，其它的线程会处于等待状态，而当前持有锁的线程可以重复的申请自己持有锁的临界资源。  

<!-- 
关键字Synchronized拥有锁重人的功能，也就是在使用Synchronized时，当一个线程 得到一个对象锁后，再次请求此对象锁时是可以再次得到该对象的锁的。这也证明在一个 Synchronized方法/块的内部调用本类的其他Synchronized方法/块时，是永远可以得到锁的。

“可重人锁”的概念是：自己可以再次获取自己的内部锁。 比如有1条线程获得了某个对象的锁，此时这个对象锁还没 有释放，当其再次想要获取这个对象的锁的时候还是可以获 取的，如果不可锁重人的话，就会造成死锁。
-->
&emsp; Synchronized可以禁止指令重排吗？不可以。  
&emsp; <font color = "red">即然Synchronized无法禁止指令重排，为何可以保证有序性？</font>  
&emsp; <font color = "red">Synchronized遵守as-if-serial语义（在java中，不管怎么排序，都不能影响单线程程序的执行结果）。</font><font color = "clime">某个线程执行到被Synchronized修饰的代码之前，会先进行加锁。执行完代码后才进行解锁。在这个期间，其他线程无法获得锁。也就是在这段时间，被Synchronized修饰的代码是单线程执行的。满足了as-if-serial语义的一个前提。</font>  
<!-- 
https://mp.weixin.qq.com/s/fL1ixtmiqKo83aUJ-cfrpg
-->

## 1.2. Synchronized与ReentrantLock  
&emsp; **<font color = "red">Synchronized与ReentrantLock的比较：</font>**  
1. 锁的实现：Synchronized是JVM实现的，而ReentrantLock是JDK实现的。  
2. 性能：新版本Java对Synchronized进行了很多优化，例如自旋锁等，Synchronized 与 ReentrantLock大致相同。  
3. 等待可中断：当持有锁的线程长期不释放锁的时候，正在等待的线程可以选择放弃等待，改为处理其他事情。ReentrantLock可中断，而Synchronized不行。  
4. 公平锁：公平锁是指多个线程在等待同一个锁时，必须按照申请锁的时间顺序来依次获得锁。Synchronized中的锁是非公平的，ReentrantLock默认情况下也是非公平的，但是也可以是公平的。  
5. 锁绑定多个条件。一个 ReentrantLock 可以同时绑定多个 Condition 对象。  

&emsp; **Synchronized与ReentrantLock的使用选择：**  
&emsp; 除非需要使用ReentrantLock的高级功能，否则优先使用Synchronized。这是因为Synchronized是JVM实现的一种锁机制，JVM原生地支持它，而ReentrantLock不是所有的JDK版本都支持。并且使用Synchronized不用担心没有释放锁而导致死锁问题，因为JVM会确保锁的释放。  

## 1.3. Synchronized与Object#wait()  
&emsp; 为什么线程通信的方法wait()，notify()和notifyAll()被定义在Object类里？  
&emsp; Java的每个对象中都有一个锁(monitor，也可以成为监视器)，并且wait()，notify()等方法用于等待对象的锁或者通知其他线程对象的监视器可用。在Java的线程中并没有可供任何对象使用的锁和同步器。这就是为什么这些方法是Object类的一部分，这样Java的每一个类都有用于线程间通信的基本方法。  

&emsp; **<font color = "clime">为什么wait(), notify()和notifyAll ()必须在同步方法或者同步块中被调用？</font>**  
&emsp; <font color = "red">当一个线程需要调用对象的wait()方法的时候，这个线程必须拥有该对象的锁</font>，接着它就会释放这个对象锁并进入等待状态直到其他线程调用这个对象上的notify()方法。同样的，当一个线程需要调用对象的notify()方法时，它会释放这个对象的锁，以便其他在等待的线程就可以得到这个对象锁。由于所有的这些方法都需要线程持有对象的锁，这样就只能通过同步来实现，所以它们只能在同步方法或者同步块中被调用。  

