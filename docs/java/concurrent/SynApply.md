

<!-- TOC -->

- [1. Synchronized](#1-synchronized)
    - [1.1. Synchronized简介](#11-synchronized简介)
    - [1.2. ~~Synchronized使用~~](#12-synchronized使用)
        - [1.2.1. 类锁和对象锁](#121-类锁和对象锁)
        - [1.2.2. Synchronized同步普通方法](#122-synchronized同步普通方法)
        - [1.2.3. Synchronized同步静态方法](#123-synchronized同步静态方法)
        - [1.2.4. Synchronized同步语句块](#124-synchronized同步语句块)
            - [1.2.4.1. 同步类](#1241-同步类)
            - [1.2.4.2. 同步this实例](#1242-同步this实例)
            - [1.2.4.3. 同步对象实例](#1243-同步对象实例)
    - [1.3. Synchronized与ReentrantLock](#13-synchronized与reentrantlock)
    - [1.4. Synchronized与Object#wait()](#14-synchronized与objectwait)

<!-- /TOC -->


# 1. Synchronized  
<!--
***Synchronized 的 8 种用法，真是绝了！ 
https://mp.weixin.qq.com/s/TOkDyqAE5TToriOMX6I6tg
详解synchronized锁的各种用法及注意事项 
https://mp.weixin.qq.com/s/gKsD1U38h4MJczEFC33ydw
-->
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
## 1.2. ~~Synchronized使用~~  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-11.png)  

&emsp; Synchronized可以使用在普通方法、静态方法、同步块中。 **<font color = "clime">Synchronized作用的对象应该是唯一的。</font>** Synchronized使用在同步块中，锁粒度更小。根据锁的具体实例，又可以分为类锁和对象锁。  
&emsp; 关键字Synchronized取得的锁都是对象锁，而不是把一段代码或方法(函数)当作锁。  
<!-- 
锁非this对象具有一定的优点：如果在一个类中有很多个Synchronized方法，这时虽然能实现同步，但会受到阻塞，所以影响运行效率；但如果使用同步代码块锁非this对象，则Synchronized(非this)代码块中的程序与同步方法是异步的，不与其他锁this同步方法争抢this锁，则可 大大提高运行效率。  
在大多数的情况下，同步Synchronized代码块都不使用String作为锁对象，而改用其他，比如new Object()实例化一个 Object对象，但它并不放人缓存中。  
-->
 
### 1.2.1. 类锁和对象锁
&emsp; **<font color = "clime">前提：是否是同一个类或同一个类的实例对象。</font>**    

&emsp; **Synchronized的范围：类锁和对象锁。** 
1. 类锁：当Synchronized修饰静态方法或Synchronized修饰代码块，传入某个class对象（Synchronized (XXXX.class)）时被称为类锁。某个线程得到了一个类锁之后，其他所有被该类锁加锁方法或代码块是锁定的，其他线程是无法访问的，但是其他线程还是可以访问没有被该类锁加锁的任何代码。  
2. 对象锁：当Synchronized修饰非静态方法或Synchronized修饰代码块时，传入非class对象（Synchronized this）时被称为对象锁。某个线程得到了对象锁之后，该对象的其他被Synchronized修饰的方法(同步方法)是锁定的，其他线程是无法访问的。但是其他线程还是可以访问没有进行同步的方法或者代码；当获取到与对象关联的内置锁时，并不能阻止其他线程访问该对象，当某个线程获得对象的锁之后，只能阻止其他线程获得同一个锁。  
3. 类锁和对象锁的关系：如同每个类只有一个class对象，而类的实例可以有很多个一样，每个类只有一个类锁，每个实例都有自己的对象锁，所以不同对象实例的对象锁是互不干扰的。但是有一点必须注意的是，其实类锁只是一个概念上的东西，并不是真实存在的，它只是用来理解锁定实例方法和静态方法的区别的。 **<font color = "clime">类锁和对象锁是不一样的锁，是互相独立的，两者不存在竞争关系，不相互阻塞。</font>**  

    * **<font color = "red">类锁与对象锁不相互阻塞。</font> 如果多线程同时访问同一类的 类锁(Synchronized 修饰的静态方法)以及对象锁(Synchronized 修饰的非静态方法)，这两个方法执行是异步的，原因：类锁和对象锁是两种不同的锁。<font color = "red">线程获得对象锁的同时，也可以获得该类锁，即同时获得两个锁，这是允许的。</font>**  
    * 相同的类锁，相同的对象锁会相互阻塞。
    * 类锁对该类的所有对象都能起作用，而对象锁不能。

### 1.2.2. Synchronized同步普通方法  
&emsp; 这种方法使用虽然最简单，但是只能作用在单例上面，如果不是单例，同步方法锁将失效。  

```java
/**
 * 用在普通方法
 */
private Synchronized void SynchronizedMethod() {
    System.out.println("SynchronizedMethod");
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```
&emsp; 此时，同一个实例只有一个线程能获取锁进入这个方法。  
&emsp; 对于普通同步方法，锁是当前实例对象，进入同步代码前要获得当前实例的锁。  
&emsp; 当两个线程同时对一个对象的一个方法进行操作，只有一个线程能够抢到锁。因为一个对象只有一把锁，一个线程获取了该对象的锁之后，其他线程无法获取该对象的锁，就不能访问该对象的其他Synchronized实例方法。可是，两个线程实例化两个不同的对象，获得的锁是不同的锁，所以互相并不影响。  

### 1.2.3. Synchronized同步静态方法  
&emsp; 同步静态方法，不管有多少个类实例，同时只有一个线程能获取锁进入这个方法。  

```java
/**
 * 用在静态方法
 */
private Synchronized static void SynchronizedStaticMethod() {
    System.out.println("SynchronizedStaticMethod");
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```
&emsp; 同步静态方法是类级别的锁，一旦任何一个线程进入这个方法，其他所有线程将无法访问这个类的任何同步类锁的方法。  
&emsp; 对于静态同步方法，锁是当前类的Class对象，进入同步代码前要获得当前类对象的锁。  
&emsp; 注意：两个线程实例化两个不同的对象，但是访问的方法是静态的，此时获取的锁是同一个锁，两个线程发生了互斥(即一个线程访问，另一个线程只能等着)，因为静态方法是依附于类而不是对象的，当Synchronized修饰静态方法时，锁是class对象。  

### 1.2.4. Synchronized同步语句块  
&emsp; 对于同步代码块，锁是Synchronized括号里面配置的对象，对给定对象加锁，进入同步代码块前要获得给定对象的锁。  

#### 1.2.4.1. 同步类
&emsp; 下面提供了两种同步类的方法，锁住效果和同步静态方法一样，都是类级别的锁，同时只有一个线程能访问带有同步类锁的方法。  

```java
/**
 * 用在类
 */
private void SynchronizedClass() {
    Synchronized (TestSynchronized.class) {
        System.out.println("SynchronizedClass");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 用在类
 */
private void SynchronizedGetClass() {
    Synchronized (this.getClass()) {
        System.out.println("SynchronizedGetClass");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
&emsp; 这里的两种用法是同步块的用法，这里表示只有获取到这个类锁才能进入这个代码块。  

#### 1.2.4.2. 同步this实例  
&emsp; 这也是同步块的用法，表示锁住整个当前对象实例，只有获取到这个实例的锁才能进入这个方法。  

```java
/**
 * 用在this
 */
private void SynchronizedThis() {
    Synchronized (this) {
        System.out.println("SynchronizedThis");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
&emsp; 用法和同步普通方法锁一样，都是锁住整个当前实例。  

#### 1.2.4.3. 同步对象实例  
&emsp; 这也是同步块的用法，和上面的锁住当前实例一样，这里表示锁住整个LOCK 对象实例，只有获取到这个LOCK实例的锁才能进入这个方法。  

```java
/**
 * 用在对象
 */
private void SynchronizedInstance() {
    Synchronized (LOCK) {
        System.out.println("SynchronizedInstance");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

## 1.3. Synchronized与ReentrantLock  
&emsp; **<font color = "red">Synchronized与ReentrantLock的比较：</font>**  
1. 锁的实现：Synchronized是JVM实现的，而ReentrantLock是JDK实现的。  
2. 性能：新版本Java对Synchronized进行了很多优化，例如自旋锁等，Synchronized 与 ReentrantLock大致相同。  
3. 等待可中断：当持有锁的线程长期不释放锁的时候，正在等待的线程可以选择放弃等待，改为处理其他事情。ReentrantLock可中断，而Synchronized不行。  
4. 公平锁：公平锁是指多个线程在等待同一个锁时，必须按照申请锁的时间顺序来依次获得锁。Synchronized中的锁是非公平的，ReentrantLock默认情况下也是非公平的，但是也可以是公平的。  
5. 锁绑定多个条件。一个 ReentrantLock 可以同时绑定多个 Condition 对象。  

&emsp; **Synchronized与ReentrantLock的使用选择：**  
&emsp; 除非需要使用ReentrantLock的高级功能，否则优先使用Synchronized。这是因为Synchronized是JVM实现的一种锁机制，JVM原生地支持它，而ReentrantLock不是所有的JDK版本都支持。并且使用Synchronized不用担心没有释放锁而导致死锁问题，因为JVM会确保锁的释放。  

## 1.4. Synchronized与Object#wait()  
&emsp; 为什么线程通信的方法wait()，notify()和notifyAll()被定义在Object类里？  
&emsp; Java的每个对象中都有一个锁(monitor，也可以成为监视器)，并且wait()，notify()等方法用于等待对象的锁或者通知其他线程对象的监视器可用。在Java的线程中并没有可供任何对象使用的锁和同步器。这就是为什么这些方法是Object类的一部分，这样Java的每一个类都有用于线程间通信的基本方法。  

&emsp; **<font color = "clime">为什么wait(), notify()和notifyAll ()必须在同步方法或者同步块中被调用？</font>**  
&emsp; <font color = "red">当一个线程需要调用对象的wait()方法的时候，这个线程必须拥有该对象的锁</font>，接着它就会释放这个对象锁并进入等待状态直到其他线程调用这个对象上的notify()方法。同样的，当一个线程需要调用对象的notify()方法时，它会释放这个对象的锁，以便其他在等待的线程就可以得到这个对象锁。由于所有的这些方法都需要线程持有对象的锁，这样就只能通过同步来实现，所以它们只能在同步方法或者同步块中被调用。  
