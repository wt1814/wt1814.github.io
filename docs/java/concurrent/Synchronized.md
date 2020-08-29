

<!-- TOC -->

- [1. Synchronized](#1-synchronized)
    - [1.1. synchronized使用](#11-synchronized使用)
        - [1.1.1. 类锁和对象锁](#111-类锁和对象锁)
        - [1.1.2. synchronized同步普通方法](#112-synchronized同步普通方法)
        - [1.1.3. synchronized同步静态方法](#113-synchronized同步静态方法)
        - [1.1.4. synchronized同步语句块](#114-synchronized同步语句块)
            - [1.1.4.1. 同步类](#1141-同步类)
            - [1.1.4.2. 同步this实例](#1142-同步this实例)
            - [1.1.4.3. 同步对象实例](#1143-同步对象实例)
    - [1.2. synchronized与ReentrantLock](#12-synchronized与reentrantlock)
    - [1.3. synchronized与Object#wait()](#13-synchronized与objectwait)
    - [1.4. synchronized原理](#14-synchronized原理)
        - [1.4.1. 反编译Synchronized代码块](#141-反编译synchronized代码块)
            - [1.4.1.1. 同步代码块](#1411-同步代码块)
            - [1.4.1.2. 同步方法](#1412-同步方法)
    - [1.5. synchronized的锁优化](#15-synchronized的锁优化)
        - [1.5.1. 锁消除](#151-锁消除)
        - [1.5.2. 锁粗化](#152-锁粗化)
        - [1.5.3. 了解HotSpot虚拟机对象的内存布局](#153-了解hotspot虚拟机对象的内存布局)
        - [1.5.4. 偏向锁](#154-偏向锁)
            - [1.5.4.1. 偏向锁的性能](#1541-偏向锁的性能)
            - [1.5.4.2. 偏向锁的失效](#1542-偏向锁的失效)
        - [1.5.5. 轻量级锁](#155-轻量级锁)
        - [1.5.6. 重量级锁](#156-重量级锁)
        - [1.5.7. 锁状态总结](#157-锁状态总结)

<!-- /TOC -->

<!-- 
 死磕Synchronized底层实现 
 https://mp.weixin.qq.com/s/ca_7lurrWVcA3bLCL7UJcQ
-->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-16.png)  

# 1. Synchronized  
&emsp; **<font color = "red">synchronized能够保证在同一时刻最多只有一个线程执行该段代码。</font>**  

&emsp; **synchronized的特性：**  

* 原子性：保证被synchronized修饰的一个或者多个操作，在执行的过程中不会被任何的因素打断，即所谓的原子操作，直到锁被释放。  
* 可见性：保证持有锁的当前线程在释放锁之前，对共享变量的修改会刷新到主存中，并对其它线程可见。  
* 有序性：保证多线程时刻中只有一个线程执行，线程执行的顺序都是有序的。  
* 可重入性：保证在多线程中，有其他的线程试图竞争持有锁的临界资源时，其它的线程会处于等待状态，而当前持有锁的线程可以重复的申请自己持有锁的临界资源。  

&emsp; Synchronized可以禁止指令重排吗？不可以。  
&emsp; <font color = "red">即然Synchronized无法禁止指令重排，为何可以保证有序性？</font>  
&emsp; <font color = "red">Synchronized遵守as-if-serial语义（在java中，不管怎么排序，都不能影响单线程程序的执行结果）。</font><font color = "lime">某个线程执行到被synchronized修饰的代码之前，会先进行加锁。执行完代码后才进行解锁。在这个期间，其他线程无法获得锁。也就是在这段时间，被synchronized修饰的代码是单线程执行的。满足了as-if-serial语义的一个前提。</font>  

<!-- 
https://mp.weixin.qq.com/s/fL1ixtmiqKo83aUJ-cfrpg
-->

## 1.1. synchronized使用  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-11.png)  

&emsp; synchronized可以使用在普通方法、静态方法、同步块中。synchronized使用在同步块中，锁粒度更小。根据锁的具体实例，又可以分为类锁和对象锁。  

### 1.1.1. 类锁和对象锁
&emsp; **synchronized的范围：类锁和对象锁。** 
1. 类锁：当synchronized修饰静态方法或synchronized修饰代码块传入某个class对象（synchronized (XXXX.class)）时被称为类锁。某个线程得到了一个类锁之后，其他所有被该类锁加锁方法或代码块是锁定的，其他线程是无法访问的，但是其他线程还是可以访问没有被该类锁加锁的任何代码。  
2. 对象锁：当synchronized修饰非静态方法或synchronized修饰代码块时传入非class对象（synchronized this)）时被称为对象锁。某个线程得到了对象锁之后，该对象的其他被 synchronized修饰的方法（同步方法）是锁定的，其他线程是无法访问的。但是其他线程还是可以访问没有进行同步的方法或者代码；当获取到与对象关联的内置锁时，并不能阻止其他线程访问该对象，当某个线程获得对象的锁之后，只能阻止其他线程获得同一个锁。  
3. 类锁和对象锁的关系：如同每个类只有一个class对象，而类的实例可以有很多个一样，每个类只有一个类锁，每个实例都有自己的对象锁，所以不同对象实例的对象锁是互不干扰的。但是有一点必须注意的是，其实类锁只是一个概念上的东西，并不是真实存在的，它只是用来理解锁定实例方法和静态方法的区别的。 **<font color = "lime">类锁和对象锁是不一样的锁，是互相独立的，两者不存在竞争关系，不相互阻塞。</font>**  

    * **<font color = "red">类锁与对象锁不相互阻塞。</font> 如果多线程同时访问同一类的 类锁（synchronized 修饰的静态方法）以及对象锁（synchronized 修饰的非静态方法）这两个方法执行是异步的，原因：类锁和对象锁是两种不同的锁。<font color = "red">线程获得对象锁的同时，也可以获得该类锁，即同时获得两个锁，这是允许的。</font>**  
    * 相同的类锁，相同的对象锁会相互阻塞。
    * 类锁对该类的所有对象都能起作用，而对象锁不能。

### 1.1.2. synchronized同步普通方法  
&emsp; 这种方法使用虽然最简单，但是只能作用在单例上面，如果不是单例，同步方法锁将失效。  

```java
/**
 * 用在普通方法
 */
private synchronized void synchronizedMethod() {
    System.out.println("synchronizedMethod");
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```
&emsp; 此时，同一个实例只有一个线程能获取锁进入这个方法。  
&emsp; 对于普通同步方法，锁是当前实例对象，进入同步代码前要获得当前实例的锁。  
&emsp; 当两个线程同时对一个对象的一个方法进行操作，只有一个线程能够抢到锁。因为一个对象只有一把锁，一个线程获取了该对象的锁之后，其他线程无法获取该对象的锁，就不能访问该对象的其他synchronized实例方法。可是，两个线程实例化两个不同的对象，获得的锁是不同的锁，所以互相并不影响。  

### 1.1.3. synchronized同步静态方法  
&emsp; 同步静态方法，不管有多少个类实例，同时只有一个线程能获取锁进入这个方法。  

```java
/**
 * 用在静态方法
 */
private synchronized static void synchronizedStaticMethod() {
    System.out.println("synchronizedStaticMethod");
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```
&emsp; 同步静态方法是类级别的锁，一旦任何一个线程进入这个方法，其他所有线程将无法访问这个类的任何同步类锁的方法。  
&emsp; 对于静态同步方法，锁是当前类的Class对象，进入同步代码前要获得当前类对象的锁。  
&emsp; 注意：两个线程实例化两个不同的对象，但是访问的方法是静态的，此时获取的锁是同一个锁，两个线程发生了互斥（即一个线程访问，另一个线程只能等着），因为静态方法是依附于类而不是对象的，当synchronized修饰静态方法时，锁是class对象。  

### 1.1.4. synchronized同步语句块  
&emsp; 对于同步代码块，锁是synchronized括号里面配置的对象，对给定对象加锁，进入同步代码块前要获得给定对象的锁。  

#### 1.1.4.1. 同步类
&emsp; 下面提供了两种同步类的方法，锁住效果和同步静态方法一样，都是类级别的锁，同时只有一个线程能访问带有同步类锁的方法。  

```java
/**
 * 用在类
 */
private void synchronizedClass() {
    synchronized (TestSynchronized.class) {
        System.out.println("synchronizedClass");
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
private void synchronizedGetClass() {
    synchronized (this.getClass()) {
        System.out.println("synchronizedGetClass");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
&emsp; 这里的两种用法是同步块的用法，这里表示只有获取到这个类锁才能进入这个代码块。  

#### 1.1.4.2. 同步this实例  
&emsp; 这也是同步块的用法，表示锁住整个当前对象实例，只有获取到这个实例的锁才能进入这个方法。  

```java
/**
 * 用在this
 */
private void synchronizedThis() {
    synchronized (this) {
        System.out.println("synchronizedThis");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
&emsp; 用法和同步普通方法锁一样，都是锁住整个当前实例。  

#### 1.1.4.3. 同步对象实例  
&emsp; 这也是同步块的用法，和上面的锁住当前实例一样，这里表示锁住整个LOCK 对象实例，只有获取到这个LOCK实例的锁才能进入这个方法。  

```java
/**
 * 用在对象
 */
private void synchronizedInstance() {
    synchronized (LOCK) {
        System.out.println("synchronizedInstance");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

## 1.2. synchronized与ReentrantLock  
&emsp; **<font color = "red">synchronized与ReentrantLock的比较：</font>**  
1. 锁的实现：synchronized是JVM实现的，而ReentrantLock是JDK实现的。  
2. 性能：新版本Java对synchronized进行了很多优化，例如自旋锁等，synchronized 与 ReentrantLock大致相同。  
3. 等待可中断：当持有锁的线程长期不释放锁的时候，正在等待的线程可以选择放弃等待，改为处理其他事情。ReentrantLock可中断，而synchronized不行。  
4. 公平锁：公平锁是指多个线程在等待同一个锁时，必须按照申请锁的时间顺序来依次获得锁。synchronized中的锁是非公平的，ReentrantLock默认情况下也是非公平的，但是也可以是公平的。  
5. 锁绑定多个条件。一个 ReentrantLock 可以同时绑定多个 Condition 对象。  

&emsp; **synchronized与ReentrantLock的使用选择：**  
&emsp; 除非需要使用ReentrantLock的高级功能，否则优先使用synchronized。这是因为synchronized是JVM实现的一种锁机制，JVM原生地支持它，而 ReentrantLock 不是所有的JDK版本都支持。并且使用synchronized不用担心没有释放锁而导致死锁问题，因为JVM会确保锁的释放。  

## 1.3. synchronized与Object#wait()  
&emsp; 为什么线程通信的方法wait(), notify()和notifyAll()被定义在Object 类里？  
&emsp; Java的每个对象中都有一个锁(monitor，也可以成为监视器) 并且wait()，notify()等方法用于等待对象的锁或者通知其他线程对象的监视器可用。在Java的线程中并没有可供任何对象使用的锁和同步器。这就是为什么这些方法是Object类的一部分，这样Java的每一个类都有用于线程间通信的基本方法。  

&emsp; **<font color = "lime">为什么wait(), notify()和notifyAll ()必须在同步方法或者同步块中被调用？</font>**  
&emsp; <font color = "red">当一个线程需要调用对象的wait()方法的时候，这个线程必须拥有该对象的锁</font>，接着它就会释放这个对象锁并进入等待状态直到其他线程调用这个对象上的notify()方法。同样的，当一个线程需要调用对象的notify()方法时，它会释放这个对象的锁，以便其他在等待的线程就可以得到这个对象锁。由于所有的这些方法都需要线程持有对象的锁，这样就只能通过同步来实现，所以它们只能在同步方法或者同步块中被调用。  

## 1.4. synchronized原理  
<!-- 
 synchronized 原理知多少
https://mp.weixin.qq.com/s/KpJZFLTeCxiuxQeiyEEJpQ
 Synchronized的底层实现快问快答 
https://mp.weixin.qq.com/s/fL1ixtmiqKo83aUJ-cfrpg

synchronized 同步语句块的实现使用的是 monitorenter 和 monitorexit 指令，其中monitorenter 指令指向同步代码块的开始位置，monitorexit 指令则指明同步代码块的结束位置。当执行 monitorenter 指令时，线程试图获取锁也就是获取 monitor(monitor对象存在于每个Java对象的对象头中，synchronized 锁便是通过这种方式获取锁的，也是为什么Java中任意对象可以作为锁的原因) 的持有权.当计数器为0则可以成功获取，获取后将锁计数器设为1也就是加1。相应的在执行monitorexit 指令后，将锁计数器设为0，表明锁被释放。如果获取对象锁失败，那当前线程就要阻塞等待，直到锁被另外一个线程释放为止。

-->

### 1.4.1. 反编译Synchronized代码块  
<!-- 
https://www.cnblogs.com/huangyin/p/6586469.html
-->

```java
public class SyncDemo {

    public synchronized void play() {}

    public void learn() {
        synchronized(this) {
        }
    }
}
```
&emsp; 利用javap工具查看生成的class文件信息分析Synchronized，下面是部分信息:  

    查看字节码工具：  
    Show Uytecode With jclasslib
    Show Bytccodc

```java
public com.zzw.juc.sync.SyncDemo();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1        // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 8: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   Lcom/zzw/juc/sync/SyncDemo;

  public synchronized void play();
    descriptor: ()V
    flags: ACC_PUBLIC, ACC_SYNCHRONIZED
    Code:
      stack=0, locals=1, args_size=1
         0: return
      LineNumberTable:
        line 10: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       1     0  this   Lcom/zzw/juc/sync/SyncDemo;

  public void learn();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=3, args_size=1
         0: aload_0
         1: dup
         2: astore_1
         3: monitorenter
         4: aload_1
         5: monitorexit
         6: goto          14
         9: astore_2
        10: aload_1
        11: monitorexit
        12: aload_2
        13: athrow
        14: return
      Exception table:
         from    to  target type
             4     6     9   any
             9    12     9   any
```
&emsp; JVM基于进入和退出Monitor对象来实现方法同步和代码块同步，但两者实现细节不同。  

* **<font color = "red">方法同步：依靠的是方法修饰符上的ACC_SYNCHRONIZED实现。</font>**  
* **<font color = "red">代码块同步：使用monitorenter和monitorexit指令实现。</font>**  

#### 1.4.1.1. 同步代码块  
&emsp; 同步代码块：monitorenter指令插入到同步代码块的开始位置，monitorexit指令插入到同步代码块的结束位置，<font color = "red">JVM需要保证每一个monitorenter都有一个monitorexit与之相对应。</font>  
&emsp; monitorenter操作的目标一定要是一个对象，类型是reference。Reference实际就是堆里的一个存放对象的地址。每个对象（reference）都有一个monitor对应，如果有其它的线程获取了这个对象的monitor，当前的线程就要一直等待，直到获得 monitor的线程放弃monitor，当前的线程才有机会获得monitor。  
&emsp; 如果monitor没有被任何线程获取，那么当前线程获取这个monitor，把monitor的entry count设置为1。表示这个monitor被线程1占用了。  
&emsp; 当前线程获取了monitor之后，会增加这个monitor的时间计数，来记录当前线程占用了monitor多长时间。  

<!-- 
&emsp; 任何对象都有一个monitor与之相关联，当且一个monitor被持有之后，它将处于锁定状态。线程执行到monitorenter指令时，将会尝试获取对象所对应的monitor所有权，即尝试获取对象的锁。
&emsp; monitor对象介绍：  
&emsp; 每个对象有一个监视器锁（monitor），monitor本质是基于操作系统互斥（mutex）实现的，操作系统实现线程之间切换需要从用户态到内核态切换，成本非常高。一个monitor只能被一个线程拥有。

```text
    monitor对象重要属性说明：
_owner：指向持有ObjectMonitor对象的线程；
_WaitSet：存放处于wait状态的线程队列；
_EntryList：存放处于等待锁block状态的线程队列；
_recursions：锁的重入次数；
_count：用来记录该线程获取锁的次数；

    假如当前线程A、B、C同时访问同步块。假如A获取到锁，也就会将 monitor 对象中的 _owner 的值赋值为当前线程ID。B、C线程会进入EntryList中。count =1 ，recursions=1。假如A线程第二次进入同步快，count = 2, recursions=2，当前线程退出时，count和recursions会减一，直到count=0, recursions=0时，说明线程A释放了monitor锁，然后会唤醒EntryList中的线程，EntryList线程会竞争monitor，竞争到了，和线程A的操作一致。
```
-->

&emsp; 两条指令的作用：  
* monitorenter：  
&emsp; <font color = "red">线程执行monitorenter指令时尝试获取monitor的所有权，当monitor被占用时就会处于锁定状态。</font>过程如下：
    1. <font color = "red">如果monitor的进入数为0，则该线程进入monitor，然后将进入数设置为1，该线程即为monitor的所有者。</font>  
    2. 如果线程已经占有该monitor，只是重新进入，则进入monitor的进入数加1，所以synchronized关键字实现的锁是可重入的锁。  
    3. 如果其他线程已经占用了monitor，则该线程进入阻塞状态，直到monitor的进入数为0，再重新尝试获取monitor的所有权。  

* monitorexit：  
&emsp; 执行monitorexit的线程必须是objectref所对应的monitor的所有者。  
&emsp; 指令执行时，monitor的进入数减1，如果减1后进入数为0，当前线程释放monitor，不再是这个monitor的所有者。其他被这个monitor阻塞的线程可以尝试去获取这个monitor的所有权。  

&emsp; **<font color = "red">同步代码块中会出现两次的monitorexit。</font>** 这是因为一个线程对一个对象上锁了，后续就一定要解锁，第二个monitorexit是为了保证在线程异常时，也能正常解锁，避免造成死锁。  

&emsp; 总结：Synchronized的实现原理，Synchronized的语义底层是通过一个monitor的对象来完成，其实wait/notify等方法也依赖于monitor对象，这就是为什么只有在同步的块或者方法中才能调用wait/notify等方法，否则会抛出java.lang.IllegalMonitorStateException的异常的原因。  

#### 1.4.1.2. 同步方法  
&emsp; synchronized方法会被翻译成普通的方法调用和返回指令，如：invokevirtual、areturn指令，在JVM字节码层面并没有任何特别的指令来实现被synchronized修饰的方法，<font color= "lime">而是在Class文件的方法表中将该方法的access_flags字段中的synchronized标志位置1，表示该方法是同步方法，</font>并使用调用该方法的对象或该方法所属的Class在JVM的内部对象表示Klass做为锁对象。  

## 1.5. synchronized的锁优化
&emsp; **<font color = "lime">锁升级过程主要是理解偏向锁、轻量级锁的升级过程。</font>**    
&emsp; **<font color = "lime">~~一句话概述：新线程竞争锁时，获取锁对象的 Markword。起初只有一个线程，会获取到偏向锁，当另一个线程竞争锁，只cas一次，抢占到撤销原线程的偏向锁；抢占不到升级成轻量级锁；轻量级锁加锁过程中会使用自旋锁，新线程自旋多次获取轻量级锁失败（锁对象不是当前线程），会升级成重量级锁。并且已经获取轻量级锁的线程在释放锁时，也会升级成重量级锁。~~</font>**  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-38.png)   

&emsp; 为了进一步改进高效并发，HotSpot虚拟机开发团队在JDK 5升级到JDK 6版本上花费了大量精力实现各种锁优化。如适应性自旋、锁消除、锁粗化、偏向锁和轻量级锁等，这些技术都是为了在线程之间更高效地共享数据及解决竞争问题，从而提高程序的执行效率。  
&emsp; 锁主要存在四种状态，依次是：无锁状态、偏向锁状态、轻量级锁状态、重量级锁状态，它们会随着竞争的激烈而逐渐升级。 **<font color = "red">偏向锁可以被重置为无锁状态，这种策略是为了提高获得锁和释放锁的效率。</font>**   
&emsp; **<font color = "lime">锁降级：</font>** <font color = "red">Hotspot在1.8开始有了锁降级。在STW期间JVM进入安全点时如果发现有闲置的monitor（重量级锁对象），会进行锁降级。</font>  
<!-- 
 Monitor和Java对象头详解：  
&emsp; Synchronized用的锁标记是存放在Java对象头的Mark Word中。  

* **Java的对象：**  
&emsp; java对象在内存中的存储结构如下：    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-12.png)   
&emsp; 内存中的对象分为三部分：对象头、对象实例数据和对齐填充（数组对象多一个区域：记录数组长度）。  
&emsp; Java对象头：对象头里的数据主要是一些运行时的数据。  
&emsp; 在Hotspot虚拟机中，对象头包含2个部分：标记字段（Mark Word)和类型指针（Kass point)。其中Klass Point是是对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例，Mark Word用于存储对象自身的运行时数据，它是实现轻量级锁和偏向锁的关键。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-13.png)   
&emsp; **Java对象头中的Mark Word：**  
&emsp; Mark Word用于存储对象自身的运行时数据，如哈希码（Hash Code）、GC分代年龄、锁状态标志、线程持有锁、偏向线程ID、偏向时间戳等，这部分数据在32位和64位虚拟机中分别为32bit和64bit。一个对象头一般用2个机器码存储（在32位虚拟机中，一个机器码为4个字节即32bit）,但如果对象是数组类型，则虚拟机用3个机器码来存储对象头，因为JVM虚拟机可以通过Java对象的元数据信息确定Java对象的大小，但是无法从数组的元数据来确认数组的大小，所以用一块来记录数组长度。在32位虚拟机中，Java对象头的Makr Word的默认存储结构如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-14.png)   
&emsp; 在程序运行期间，对象头中锁表标志位会发生改变。Mark Word可能发生的变化如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-15.png)   

* **Monitor：**  
&emsp; Monitor是操作系统提出来的一种高级原语，但其具体的实现模式，不同的编程语言都有可能不一样。Monitor有一个重要特点那就是，同一个时刻，只有一个线程能进入到Monitor定义的临界区中，这使得Monitor能够达到互斥的效果。但仅仅有互斥的作用是不够的，无法进入Monitor临界区的线程，它们应该被阻塞，并且在必要的时候会被唤醒。显然，monitor作为一个同步工具，也应该提供这样的机制。  
-->


### 1.5.1. 锁消除  
<!-- https://juejin.im/post/5d2303b5f265da1b8f1ae5b0 -->
&emsp; 锁消除是指虚拟机即时编译器在运行时，对一些代码上要求同步，但是被检测到不可能存在共享数据竞争的锁进行清除。锁清除的主要判定依据来源于逃逸分析的数据支持，如果判断在一段代码中，堆上的所有数据都不会逃逸出去从而被其他线程访问到，那就可以把它们当做栈上数据对待，认为它们是线程私有的，同步枷锁自然就无需进行。  
&emsp; 简单来说，Java中使用同步来保证数据的安全性，但是<font color = "red">对于一些明显不会产生竞争的情况下，Jvm会根据现实执行情况对代码进行锁消除以提高执行效率。</font>  

&emsp; 示例：  

```java
public void add(String str1,String str2){
         StringBuffer sb = new StringBuffer();
         sb.append(str1).append(str2);
}
```
 &emsp; StringBuffer是线程安全的，因为它的关键方法都是被synchronized修饰过的，但看上面这段代码，会发现，sb这个引用只会在add方法中使用，不可能被其它线程引用（因为是局部变量，栈私有），因此sb是不可能共享的资源，JVM会自动消除StringBuffer对象内部的锁。  

### 1.5.2. 锁粗化  
&emsp; 原则上，在编写代码的时候，总是推荐将同步块的作用范围限制得尽量小，一直在共享数据的实际作用域才进行同步，这样是为了使得需要同步的操作数量尽可能变小，如果存在锁竞争，那等待线程也能尽快拿到锁。  
&emsp; 大部分情况下，上面的原则都是没有问题的，但是如果一系列的连续操作都对同一个对象反复加锁和解锁，那么会带来很多不必要的性能消耗。  
&emsp; 如果虚拟机探测到有这样<font color = "lime">一串零碎的操作都对同一个对象加锁，将会把加锁同步的范围扩展（粗化）到整个操作序列到外部。</font>  

<!-- 
如果一系列的连续操作都对同一个对象反复加锁和解锁，频繁的加锁操作就会导致性能损耗。
当多个彼此靠近的同步块可以合并到一起，形成一个同步块的时候，就会进行锁粗化。该方法还有一种变体，可以把多个同步方法合并为一个方法。如果所有方法都用一个锁对象，就可以尝试这种方法。
-->

&emsp; 示例：  

```java
public String test(String str){
       
       int i = 0;
       StringBuffer sb = new StringBuffer():
       while(i < 100){
           sb.append(str);
           i++;
       }
       return sb.toString():
}
```
&emsp; JVM会检测到这样一连串的操作都对同一个对象加锁（while 循环内 100 次执行 append，没有锁粗化的就要进行 100 次加锁/解锁），此时 JVM 就会将加锁的范围粗化到这一连串的操作的外部（比如 while 虚幻体外），使得这一连串操作只需要加一次锁即可。  

### 1.5.3. 了解HotSpot虚拟机对象的内存布局  

&emsp; 下面讲解Synchroized的偏向锁、轻量级锁、重量级锁。 **<font color = "red">需要先了解HotSpot虚拟机对象的内存布局。</font>**在64位的HotSpot虚拟机中，不同状态下对象头的存储内容如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-41.png)   
&emsp; HotSpot虚拟机的对象头（Object Header）分为三部分：对象头(Header)、实例数据(Instance Data)、对齐填充(Padding)。<font color = "red">用对象头中markword最低的三位代表锁状态，其中1位是偏向锁位，两位是普通锁位。</font>  

* 对象头：
    * 第一部分用于存储对象自身的运行时数据，如哈希码、GC分代年龄、锁标识状态、线程持有的锁、偏向线程ID等。这部分数据的长度在32位和64位的Java虚拟机中分别会占用32个或64个比特，官方称它为“Mark Word”。这部分是实现轻量级锁和偏向锁的关键。  
    * 另外一部分指针类型，指向对象的类元数据类型（即对象代表哪个类）。如果是数组对象，则对象头中还有一部分用来记录数组长度。  
    * 还会有一个额外的部分用于存储数组长度。  
* 实例数据：存储对象真正的有效信息（包括父类继承下来的和自己定义的）  
* 对齐填充：JVM要求对象起始地址必须是8字节的整数倍（8字节对齐） 

&emsp; **<font color = "red">由于对象头信息是与对象自身定义的数据无关的额外存储成本，考虑到Java虚拟机的空间使用效率，</font>** **<font color = "lime">Mark Word被设计成一个非固定的动态数据结构，</font>** 以便在极小的空间内存储尽量多的信息。它会根据对象的状态复用自己的存储空间。  

        为什么锁信息存放在对象头里？
        因为在Java中任意对象都可以用作锁，因此必定要有一个映射关系，存储该对象以及其对应的锁信息（比如当前哪个线程持有锁，哪些线程在等待）。一种很直观的方法是，用一个全局map，来存储这个映射关系，但这样会有一些问题：需要对map做线程安全保障，不同的synchronized之间会相互影响，性能差；另外当同步对象较多时，该map可能会占用比较多的内存。
        所以最好的办法是将这个映射关系存储在对象头中，因为对象头本身也有一些hashcode、GC相关的数据，所以如果能将锁信息与这些信息共存在对象头中就好了。
        也就是说，如果用一个全局 map 来存对象的锁信息，还需要对该 map 做线程安全处理，不同的锁之间会有影响。所以直接存到对象头。

<!--   
工具：JOL = Java Object Layout   
<dependencies>
    <dependency>
        <groupId>org.openjdk.jol</groupId>
        <artifactId>jol-core</artifactId>
        <version>0.9</version>
    </dependency>
</dependencies>
-->

### 1.5.4. 偏向锁  
&emsp; <font color = "red">偏向锁定义：</font>偏向锁是指偏向于让第一个获取锁对象的线程，这个线程在之后获取该锁就不再需要进行同步操作。 

<!-- 
偏向锁 - markword 上记录当前线程指针，下次同一个线程加锁的时候，不需要争用，只需要判断线程指针是否同一个，所以，偏向锁，偏向加锁的第一个线程 。hashCode备份在线程栈上 线程销毁，锁降级为无锁
-->

&emsp; 偏向锁的获得和撤销流程图解：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-31.png)   

1. **<font color = "red">偏向锁整体流程：</font>**
    1. **<font color = "lime">当锁对象第一次被线程获得的时候，进入偏向状态，标记为 |1|01|（前面对象内存布局图中说明了，这属于偏向锁状态）。同时使用CAS操作将线程ID （ThreadID）记录到Mark Word 中，</font>** 如果 CAS 操作成功，这个线程以后每次进入这个锁相关的同步块就不需要再进行任何同步操作。  
    2. <font color = "red">新线程去尝试获取这个锁时，偏向模式就马上宣告结束。</font>根据锁对象目前是否处于被锁定的状态决定是否撤销偏向（偏向模式设置为“0”），<font color = "red">撤销后标志位恢复到未锁定（标志位为“01”）或轻量级锁定（标志位为“00”）的状态。</font>  
    &emsp; 新线程争抢锁成功，对原线程进行锁撤销；抢占失败，升级为轻量级锁。  
----
2. **<font color = "lime">新线程获取偏向锁：（根据偏向锁的状态以及cas是否成功，来决定是否撤销偏向锁、升级轻量级锁。）</font>**  
    1. **<font color = "lime">首先获取锁对象的Markword，判断是否处于可偏向状态。（biased_lock=1、且 ThreadId 为空）</font>**  
    2. **<font color = "red">如果是可偏向状态，则通过CAS操作，把当前线程的ID写入到 MarkWord。</font>**   
        * 如果 cas 成功，表示已经获得了锁对象的偏向锁，接着执行同步代码块。  
        * 如果 cas 失败，说明有其他线程已经获得了偏向锁，这种情况说明当前锁存在竞争，需要撤销已获得偏向锁的线程，并且把它持有的锁升级为轻量级锁（这个操作需要等到全局安全点，也就是没有线程在执行字节码）才能执行。
    3. **<font color = "red">如果是已偏向状态，需要检查markword中存储的ThreadID是否等于当前线程的ThreadID。</font>**   
        * 如果相等，不需要再次获得锁，可直接执行同步代码块。
        * 如果不相等，说明当前锁偏向于其他线程，需要<font color = "red">撤销偏向锁并升级到轻量级锁</font>。
3. **<font color = "lime">偏向锁的撤销流程：</font>**  
    &emsp; 偏向锁的撤销并不是把对象恢复到无锁可偏向状态（因为偏向锁并不存在锁释放的概念），而是在获取偏向锁的过程中，发现 cas 失败也就是存在线程竞争时，直接把被偏向的锁对象升级到被加了轻量级锁的状态。  
    &emsp; 对原持有偏向锁的线程进行撤销时，原获得偏向锁的线程有两种情况：  
    1. 原获得偏向锁的线程如果已经退出了临界区，也就是同步代码块执行完了，那么这个时候会把对象头设置成无锁状态并且争抢锁的线程可以基于 CAS 重新偏向之前线程。
    2. 如果原获得偏向锁的线程的同步代码块还没执行完，处于临界区之内，这个时候会把原获得偏向锁的线程升级为轻量级锁后继续执行同步代码块。

----
&emsp; 引用《阿里手册：码出高效》的描述再理解一次：

        偏向锁是为了在资源没有被多线程竞争的情况下尽量减少锁带来的性能开销。
        在锁对象的对象头中有一个ThreadId字段，当第一个线程访问锁时，如果该锁没有被其他线程访问过，即 ThreadId字段为空，那么JVM让其持有偏向锁，并将ThreadId字段的值设置为该线程的ID。当下一次获取锁的时候，会判断ThreadId是否相等，如果一致就不会重复获取锁，从而提高了运行效率。
        如果存在锁的竞争情况，偏向锁就会被撤销并升级为轻量级锁。

&emsp; 偏向锁、轻量级锁的状态转化及对象Mark Word的关系如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-29.png)  
 
&emsp; 图中，偏向锁的重偏向和撤销偏向时如果判断对象是否已经锁定？  
&emsp; HotSpot支持存储释放偏向锁，以及偏向锁的批量重偏向和撤销。这个特性可以通过JVM的参数进行切换，而且这是默认支持的。  
&emsp; Unlock状态下Mark Word的一个比特位用于标识该对象偏向锁是否被使用或者是否被禁止。如果该bit位为0，则该对象未被锁定，并且禁止偏向；如果该bit位为1，则意味着该对象处于以下三种状态：  

* 匿名偏向(Anonymously biased)。在此状态下thread pointer为NULL(0)，意味着还没有线程偏向于这个锁对象。第一个试图获取该锁的线程将会面临这个情况，使用原子CAS指令可将该锁对象绑定于当前线程。这是允许偏向锁的类对象的初始状态。
* 可重偏向(Rebiasable)。在此状态下，偏向锁的epoch字段是无效的（与锁对象对应的class的mark_prototype的epoch值不匹配）。下一个试图获取锁对象的线程将会面临这个情况，使用原子CAS指令可将该锁对象绑定于当前线程。**在批量重偏向的操作中，未被持有的锁对象都被至于这个状态，以便允许被快速重偏向。**
* 已偏向(Biased)。这种状态下，thread pointer非空，且epoch为有效值——意味着其他线程正在持有这个锁对象。

#### 1.5.4.1. 偏向锁的性能   
&emsp; 偏向锁可以提高带有同步但无竞争的程序性能，但它同样是一个带有效益权衡（Trade Off）性质的优化，也就是说它并非总是对程序运行有利。 **<font color = "lime">如果程序中大多数的锁都总是被多个不同的线程访问，那偏向模式就是多余的。</font>** 在具体问题具体分析的前提下，有时候使用参数-XX：-UseBiasedLocking来禁止偏向锁优化反而可以提升性能。 

#### 1.5.4.2. 偏向锁的失效  
&emsp; 如果计算过对象的hashcode()，则对象无法进入偏向状态。    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-39.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-40.png)  

&emsp; 轻量级锁重量级锁的hashCode存在什么地方？
&emsp; 存在线程栈中，轻量级锁的LR中，或是代表重量级锁的ObjectMonitor的成员中。  

### 1.5.5. 轻量级锁  
&emsp; 轻量级锁在加锁过程中，用到了自旋锁。  
&emsp; **<font color = "red">为什么有了自旋锁还需要重量级锁？</font>**  
&emsp; 自旋是消耗CPU资源的，如果锁的时间长，或者自旋线程多，CPU会被大量消耗。  
&emsp; 重量级锁有等待队列，所有拿不到锁的线程进入等待队列，不需要消耗CPU资源。  

&emsp; **偏向锁是否一定比自旋锁效率高？**  
&emsp; 不一定，在明确知道会有多线程竞争的情况下，偏向锁肯定会涉及锁撤销，这时候直接使用自旋锁。  
&emsp; JVM启动过程，一般会有很多线程竞争，所以默认情况启动时不打开偏向锁，过一段时间再打开。  

&emsp; 轻量级锁及锁膨胀流程：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-30.png)  

1. <font color = "red">偏向锁升级为轻量级锁之后，对象的Markword也会进行相应的的变化。</font>  
    1. <font color = "lime">线程在自己的栈桢中创建锁记录LockRecord。</font>  
    2. 将锁对象的对象头中的MarkWord复制到线程刚刚创建的锁记录中。  
    3. <font color = "lime">将锁记录中的Owner指针指向锁对象。</font>  
    4. 将锁对象的对象头的MarkWord替换为指向锁记录的指针。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-32.png)  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-33.png)  
2. <font color = "red">自旋锁</font>  
&emsp; 轻量级锁在加锁过程中，用到了自旋锁。自旋锁分为固定次数自旋锁和自适应自旋锁。轻量级锁是针对竞争锁对象线程不多且线程持有锁时间不长的场景，因为阻塞线程需要CPU从用户态转到内核态，代价很大，如果一个刚刚阻塞不久就被释放代价有大。  
&emsp;  自旋锁与自适应自旋：    
&emsp; <font color = "red">为了让线程等待，只需要让线程执行一个忙循环（自旋），这项技术就是所谓的自旋锁。</font>引入自旋锁的原因是互斥同步对性能最大的影响是阻塞的实现，管钱线程和恢复线程的操作都需要转入内核态中完成，给并发带来很大压力。自旋锁让物理机器有一个以上的处理器的时候，能让两个或以上的线程同时并行执行。就可以让后面请求锁的那个线程 “稍等一下” ，但不放弃处理器的执行时间，看看持有锁的线程是否很快就会释放锁。为了让线程等待，只需让线程执行一个忙循环（自旋），这项技术就是所谓的自旋锁。  
&emsp; 自旋锁虽然能避免进入阻塞状态从而减少开销，但是它需要进行忙循环操作占用 CPU 时间，它只适用于共享数据的锁定状态很短的场景。  
&emsp; 在 JDK 1.6之前，自旋次数默认是10次，用户可以使用参数-XX:PreBlockSpin来更改。  
&emsp; **<font color = "red">JDK1.6引入了自适应的自旋锁。自适应意味着自旋的时间不再固定了，而是由前一次在同一个锁上的自旋时间及锁的拥有者的状态来决定。（这个应该属于试探性的算法）。</font>**  
3. 已经获取轻量级锁的线程的解锁。   
&emsp; 轻量级锁的锁释放逻辑其实就是获得锁的逆向逻辑，通过CAS操作把线程栈帧中的LockRecord替换回到锁对象的MarkWord中，如果成功表示没有竞争。如果失败，表示当前锁存在竞争，那么轻量级锁就会膨胀成为重量级锁。  
4. 新线程获取轻量级锁：  
&emsp; **<font color = "red">1. 获取轻量锁过程当中会当在前线程的虚拟机栈中创建一个Lock Record的内存区域去存储获取锁的记录DisplacedMarkWord，</font>  
&emsp; <font color = "lime">2. 然后使用CAS操作将锁对象的Mark Word更新成指向刚刚创建的Lock Record的内存区域DisplacedMarkWord的地址，</font>** 如果这个操作成功，就说明线程获取了该对象的锁，把对象的Mark Word 标记成 00，表示该对象处于轻量级锁状态。失败时，会判断是否是该线程之前已经获取到锁对象了，如果是就进入同步块执行。如果不是，那就是有多个线程竞争这个锁对象，那轻量锁就不适用于这个情况了，要膨胀成重量级锁。  

        线程A获取轻量级锁时会把对象头中的MarkWord复制一份到线程A的栈帧中创建用于存储锁记录的空间DisplacedMarkWord，然后使用CAS将对象头中的内容替换成线程A存储DisplacedMarkWord的地址。如果这时候出现线程B来获取锁，线程B也跟线程A同样复制对象头的MarkWord到自己的DisplacedMarkWord中，如果线程A锁还没释放，这时候那么线程B的CAS操作会失败，会继续自旋，当然不可能让线程B一直自旋下去，自旋到一定次数（固定次数/自适应）就会升级为重量级锁。 

### 1.5.6. 重量级锁
&emsp; <font color = "lime">重量级锁是依赖对象内部的monitor锁来实现的，而monitor又依赖操作系统的MutexLock(互斥锁)来实现的，所以重量级锁也称为互斥锁。</font>  

&emsp; 升级为重量级锁过程：  
&emsp; 升级重量级锁--->向操作系统申请资源，linux mutex，CPU从3级-0级系统调用，线程挂起，进入等待队列，等待操作系统的调度，然后再映射回用户空间。  


&emsp; <font color = "lime">为什么说重量级线程开销很大？</font>  
&emsp; 当系统检查到锁是重量级锁之后，会把等待想要获得锁的线程进行阻塞，被阻塞的线程不会消耗cpu。但是阻塞或者唤醒一个线程时，都需要操作系统来帮忙，这就需要从用户态转换到内核态，而转换状态是需要消耗很多时间的，有可能比用户执行代码的时间还要长。

### 1.5.7. 锁状态总结  
&emsp; JDK 1.6 引入了偏向锁和轻量级锁，从而让锁拥有了四个状态： **无锁状态（unlocked）(MarkWord标志位01，没有线程执行同步方法/代码块时的状态)** 、偏向锁状态（biasble）、轻量级锁状态（lightweight locked）和重量级锁状态（inflated）。  

&emsp; 重量级排序 ：无锁 > 轻量级锁 > 偏向锁 > 重量级锁    
&emsp; **<font color = "lime">锁降级：</font>** <font color = "red">Hotspot在1.8开始有了锁降级。在STW期间JVM进入安全点时如果发现有闲置的monitor（重量级锁对象），会进行锁降级。</font>

&emsp; **<font color = "lime">synchronized锁对比：</font>**  
&emsp; 用户空间锁VS重量级锁：  

* 偏向锁、自旋锁都是用户空间完成。  
* 重量级锁是需要向内核申请。  

|状态|标志位|描述|优点|缺点|应用场景|
|---|---|---|---|---|---|
|偏向锁	|010|<font color = "red">无实际竞争，让一个线程一直持有锁，在其他线程需要竞争锁（只cas一次）的时候，再释放锁</font>|加锁解锁不需要额外消耗	|如果线程间存在竞争，会有撤销锁的消耗	|只有一个线程进入临界区|
|轻量级|00|<font color = "red">无实际竞争，多个线程交替使用锁；允许短时间的锁竞争</font>|竞争的线程不会阻塞|如果线程一直得不到锁，会一直自旋，消耗CPU|多个线程交替进入临界区|
|重量级	|10|<font color = "lime">有实际竞争，且锁竞争时间长</font>|线程竞争不使用自旋，不消耗CPU|线程阻塞，响应时间长|多个线程同时进入临界区| 

