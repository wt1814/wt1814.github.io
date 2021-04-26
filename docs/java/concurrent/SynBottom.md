<!-- TOC -->

- [1. Synchronized底层原理](#1-synchronized底层原理)
    - [1.1. Java对象头与monitor](#11-java对象头与monitor)
        - [1.1.1. JVM内存对象](#111-jvm内存对象)
        - [1.1.2. 对象头详解](#112-对象头详解)
            - [1.1.2.1. Mark Word](#1121-mark-word)
            - [1.1.2.2. class pointer](#1122-class-pointer)
            - [1.1.2.3. array length](#1123-array-length)
        - [1.1.3. monitor对象](#113-monitor对象)
    - [1.2. Synchronized底层实现](#12-synchronized底层实现)
        - [1.2.1. 同步代码块](#121-同步代码块)
        - [1.2.2. 同步方法](#122-同步方法)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "clime">Java对象头的MarkWord中除了存储锁状态标记外，还存有ptr_to_heavyweight_monitor(也称为管程或监视器锁)的起始地址，每个对象都存在着一个monitor与之关联。</font>**  
2. **<font color = "red">monitor运行的机制过程如下：(_EntryList队列、_Owner区域、_WaitSet队列)</font>**  
  ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-55.png)  
  * 想要获取monitor的线程，首先会进入_EntryList队列。  
  * 当某个线程获取到对象的monitor后，进入Owner区域，设置为当前线程，同时计数器count加1。  
  * **如果线程调用了wait()方法，则会进入WaitSet队列。** 它会释放monitor锁，即将owner赋值为null，count自减1，进入WaitSet队列阻塞等待。  
  * 如果其他线程调用 notify() / notifyAll()，会唤醒WaitSet中的某个线程，该线程再次尝试获取monitor锁，成功即进入Owner区域。  
  * 同步方法执行完毕了，线程退出临界区，会将monitor的owner设为null，并释放监视锁。  
3. Synchronized修饰方法、代码块  
&emsp; Synchronized方法同步：依靠的是方法修饰符上的ACC_Synchronized实现。  
&emsp; Synchronized代码块同步：使用monitorenter和monitorexit指令实现。  

# 1. Synchronized底层原理
<!--
 JOL：分析Java对象的内存布局 
https://mp.weixin.qq.com/s/wsgxJSpEbY3yrmL9mDC2sw
-->
<!-- 
17张图带你秒杀synchronized关键字 
https://mp.weixin.qq.com/s/v1XgT2X4IuF3_WNqeUVhVA
-->
<!-- 
~~
Synchronized底层原理—Monitor监视器
https://blog.csdn.net/qq_40788718/article/details/106450724?utm_source=app
-->

## 1.1. Java对象头与monitor  
<!-- 
Java对象头与monitor
https://blog.csdn.net/kking_edc/article/details/108382333
-->
<!-- 
 Monitor和Java对象头详解：  
&emsp; Synchronized用的锁标记是存放在Java对象头的Mark Word中。  

* **Java的对象：**  
&emsp; java对象在内存中的存储结构如下：    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-12.png)   
&emsp; 内存中的对象分为三部分：对象头、对象实例数据和对齐填充(数组对象多一个区域：记录数组长度)。  
&emsp; Java对象头：对象头里的数据主要是一些运行时的数据。  
&emsp; 在Hotspot虚拟机中，对象头包含2个部分：标记字段(Mark Word)和类型指针(Kass point)。其中Klass Point是是对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例，Mark Word用于存储对象自身的运行时数据，它是实现轻量级锁和偏向锁的关键。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-13.png)   
&emsp; **Java对象头中的Mark Word：**  
&emsp; Mark Word用于存储对象自身的运行时数据，如哈希码(Hash Code)、GC分代年龄、锁状态标志、线程持有锁、偏向线程ID、偏向时间戳等，这部分数据在32位和64位虚拟机中分别为32bit和64bit。一个对象头一般用2个机器码存储(在32位虚拟机中，一个机器码为4个字节即32bit),但如果对象是数组类型，则虚拟机用3个机器码来存储对象头，因为JVM虚拟机可以通过Java对象的元数据信息确定Java对象的大小，但是无法从数组的元数据来确认数组的大小，所以用一块来记录数组长度。在32位虚拟机中，Java对象头的Makr Word的默认存储结构如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-14.png)   
&emsp; 在程序运行期间，对象头中锁表标志位会发生改变。Mark Word可能发生的变化如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-15.png)   

* **Monitor：**  
&emsp; Monitor是操作系统提出来的一种高级原语，但其具体的实现模式，不同的编程语言都有可能不一样。Monitor有一个重要特点那就是，同一个时刻，只有一个线程能进入到Monitor定义的临界区中，这使得Monitor能够达到互斥的效果。但仅仅有互斥的作用是不够的，无法进入Monitor临界区的线程，它们应该被阻塞，并且在必要的时候会被唤醒。显然，monitor作为一个同步工具，也应该提供这样的机制。  
-->
&emsp; Java对象头是实现synchronized的锁对象的基础。  

### 1.1.1. JVM内存对象
<!-- 
Java对象头与monitor
https://blog.csdn.net/kking_edc/article/details/108382333
-->
&emsp; 在JVM中，对象在内存中的布局分为三块区域：对象头、实例数据和对齐填充，如下所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-65.png)   

* 对象头：包含Mark Word、class pointer、array length共3部分。  
    * **第一部分用于存储对象自身的运行时数据，如哈希码、GC分代年龄、锁标识状态、线程持有的锁、偏向线程ID等。** 这部分数据的长度在32位和64位的Java虚拟机中分别会占用32个或64个比特，官方称它为“Mark Word”。这部分是实现轻量级锁和偏向锁的关键。  
    * 另外一部分指针类型，指向对象的类元数据类型(即对象代表哪个类)。如果是数组对象，则对象头中还有一部分用来记录数组长度。  
    * 还会有一个额外的部分用于存储数组长度。  
* 实例数据：存放类的属性数据信息，包括父类的属性信息，如果是数组的实例部分还包括数组的长度，这部分内存按4字节对齐。    
* 对齐填充：JVM要求对象起始地址必须是8字节的整数倍(8字节对齐)。填充数据不是必须存在的，仅仅是为了字节对齐。   

&emsp; JVM中对象头的方式有以下两种(以32位JVM为例)  

* 普通对象：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-60.png)   
* 数组对象：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-61.png)   

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

### 1.1.2. 对象头详解  
#### 1.1.2.1. Mark Word  
&emsp; **<font color = "red">由于对象头信息是与对象自身定义的数据无关的额外存储成本，考虑到Java虚拟机的空间使用效率，</font>** **<font color = "clime">Mark Word被设计成一个非固定的动态数据结构，</font>** 以便在极小的空间内存储尽量多的信息。它会根据对象的状态复用自己的存储空间。  

&emsp; 这部分主要用来存储对象自身的运行时数据，如hashcode、gc分代年龄等。mark word的位长度为JVM的一个Word大小，也就是说32位JVM的Mark word为32位，64位JVM为64位。
为了让一个字大小存储更多的信息，JVM将字的最低两个位设置为标记位，不同标记位下的Mark Word示意如下：  

&emsp; 64位下的标记字与32位的相似：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-41.png)   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-62.png)   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-64.png)   

&emsp; 下面两张图是32位JVM和64位JVM中“Mark Word”所记录的信息  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-67.png)   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-68.png)   

&emsp; 其中各部分的含义如下：(<font color = "red">用对象头中markword最低的三位代表锁状态，其中1位是偏向锁位，两位是普通锁位。</font>)  

* lock：2位的锁状态标记位，由于希望用尽可能少的二进制位表示尽可能多的信息，所以设置了lock标记。该标记的值不同，整个mark word表示的含义不同。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-63.png)   
* bias_lock：对象是否启动偏向锁标记，只占1个二进制位。为1时表示对象启动偏向锁，为0时表示对象没有偏向锁。  
* age：4位的Java对象年龄。在GC中，如果对象在Survivor区复制一次，年龄增加1。当对象达到设定的阈值时，将会晋升到老年代。默认情况下，并行GC的年龄阈值为15，并发GC的年龄阈值为6。由于age只有4位，所以最大值为15，这就是-XX:MaxTenuringThreshold选项最大值为15的原因。  
* identity_hashcode：25位的对象标识Hash码，采用延迟加载技术。调用方法System.identityHashCode()计算，并会将结果写到该对象头中。当对象被锁定时，该值会移动到管程Monitor中。  
* thread：持有偏向锁的线程ID。  
* epoch：偏向时间戳。  
* ptr_to_lock_record：指向栈中锁记录的指针。  
* **<font color = "clime">ptr_to_heavyweight_monitor：指向monitor对象(也称为管程或监视器锁)的起始地址，每个对象都存在着一个monitor与之关联，对象与其monitor之间的关系有存在多种实现方式，如monitor对象可以与对象一起创建销毁或当前线程试图获取对象锁时自动生，但当一个monitor被某个线程持有后，它便处于锁定状态。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-56.png)   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-76.png)   

        为什么锁信息存放在对象头里？
        因为在Java中任意对象都可以用作锁，因此必定要有一个映射关系，存储该对象以及其对应的锁信息(比如当前哪个线程持有锁，哪些线程在等待)。一种很直观的方法是，用一个全局map，来存储这个映射关系，但这样会有一些问题：需要对map做线程安全保障，不同的Synchronized之间会相互影响，性能差；另外当同步对象较多时，该map可能会占用比较多的内存。
        所以最好的办法是将这个映射关系存储在对象头中，因为对象头本身也有一些hashcode、GC相关的数据，所以如果能将锁信息与这些信息共存在对象头中就好了。
        也就是说，如果用一个全局 map 来存对象的锁信息，还需要对该 map 做线程安全处理，不同的锁之间会有影响。所以直接存到对象头。

#### 1.1.2.2. class pointer
&emsp; 这一部分用于存储对象的类型指针，该指针指向它的类元数据，JVM通过这个指针确定对象是哪个类的实例。该指针的位长度为JVM的一个字大小，即32位的JVM为32位，64位的JVM为64位。  
&emsp; 如果应用的对象过多，使用64位的指针将浪费大量内存，统计而言，64位的JVM将会比32位的JVM多耗费50%的内存。为了节约内存可以使用选项+UseCompressedOops开启指针压缩，其中，oop即ordinary object pointer普通对象指针。开启该选项后，下列指针将压缩至32位：  

* 每个Class的属性指针(即静态变量)
* 每个对象的属性指针(即对象变量)
* 普通对象数组的每个元素指针

&emsp; 当然，也不是所有的指针都会压缩，一些特殊类型的指针JVM不会优化，比如指向PermGen的Class对象指针(JDK8中指向元空间的Class对象指针)、本地变量、堆栈元素、入参、返回值和NULL指针等。  

#### 1.1.2.3. array length  
&emsp; 如果对象是一个数组，那么对象头还需要有额外的空间用于存储数组的长度，这部分数据的长度也随着JVM架构的不同而不同：32位的JVM上，长度为32位；64位JVM则为64位。64位JVM如果开启+UseCompressedOops选项，该区域长度也将由64位压缩至32位。  

### 1.1.3. monitor对象  
<!-- 
Mutex Lock
https://www.cnblogs.com/bjlhx/p/10555194.html

&emsp; 任何对象都有一个monitor与之相关联，当且一个monitor被持有之后，它将处于锁定状态。线程执行到monitorenter指令时，将会尝试获取对象所对应的monitor所有权，即尝试获取对象的锁。
&emsp; monitor对象介绍：  
&emsp; 每个对象有一个监视器锁(monitor)，monitor本质是基于操作系统互斥(mutex)实现的，操作系统实现线程之间切换需要从用户态到内核态切换，成本非常高。一个monitor只能被一个线程拥有。
-->
&emsp; 每个对象实例都会有个Monitor对象，Monitor对象和Java对象一同创建并消毁，在Java虚拟机(HotSpot)中，Monitor是基于C++实现的，在虚拟机的ObjectMonitor.hpp文件中。ObjectMonitor的成员变量如下( Hospot 1.8) ：  

```C
ObjectMonitor() {
    _header       = NULL;
    _count        = 0;
    _waiters      = 0,
    _recursions   = 0;  // 线程重入次数
    _object       = NULL;  // 存储Monitor对象
    _owner        = NULL;  // 持有当前线程的owner
    _WaitSet      = NULL;  // wait状态的线程列表
    _WaitSetLock  = 0 ;
    _Responsible  = NULL ;
    _succ         = NULL ;
    _cxq          = NULL ;  // 单向列表
    FreeNext      = NULL ;
    _EntryList    = NULL ;  // 处于等待锁状态block状态的线程列表
    _SpinFreq     = 0 ;
    _SpinClock    = 0 ;
    OwnerIsThread = 0 ;
    _previous_owner_tid = 0;
  }
```
&emsp; ObjectMonitor中有两个队列，_WaitSet和_EntryList，用来保存ObjectWaiter对象列表(每个等待锁的线程都会被封装成ObjectWaiter对象)。  

&emsp; **<font color = "red">monitor运行的机制过程如下：(_WaitSet队列和 _EntryList队列)</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-55.png)  

* 想要获取monitor的线程，首先会进入_EntryList队列。  
* 当某个线程获取到对象的monitor后，进入Owner区域，设置为当前线程,同时计数器count加1。  
* **如果线程调用了wait()方法，则会进入WaitSet队列。** 它会释放monitor锁，即将owner赋值为null，count自减1，进入WaitSet队列阻塞等待。  
* 如果其他线程调用 notify() / notifyAll()，会唤醒WaitSet中的某个线程，该线程再次尝试获取monitor锁，成功即进入Owner区域。  
* 同步方法执行完毕了，线程退出临界区，会将monitor的owner设为null，并释放监视锁。  

&emsp; 因此，monitor对象存在于每个Java对象的对象头中(存储的指针的指向)，Synchronized锁便是通过这种方式获取锁的，也是为什么Java中任意对象可以作为锁的原因，同时也是notify/notifyAll/wait等方法存在于顶级对象Object中的原因。  
<!-- 
&emsp; 那你能说说看monitor吗，到底是怎么实现了加锁和锁释放的呢？  
&emsp; 当多个线程同时访问同一段代码的时候，这些线程会被放在EntrySet集合，处于阻塞状态的线程都会被放在该列表当中，当线程获取到对象的monitor时，监视器锁monitor本质又是依赖于底层的操作系统的 Mutex Lock 来实现的，线程获取Mutex成功，这个时候其他的线程就无法获取到该Mutex。  
&emsp; 如果线程调用了wait方法，那么该线程就会释放掉持有的Mutex，并且该线程会进入到WaitSet集合(等待集合中)，等待下一次被其他的线程notify/notifyAll唤醒，如果当前线程的方法执行完毕，那么它也会释放掉所持有的Mutex。  

&emsp; 每个对象都有一个monitor对应，如果有其它的线程获取了这个对象的monitor，当前的线程就要一直等待，直到获得 monitor的线程放弃monitor，当前的线程才有机会获得monitor。  
&emsp; 如果monitor没有被任何线程获取，那么当前线程获取这个monitor，把monitor的entry count设置为1。表示这个monitor被线程1占用了。  
&emsp; 当前线程获取了monitor之后，会增加这个monitor的时间计数，来记录当前线程占用了monitor多长时间。  

有两个队列waitSet和entryList。

    当多个线程进入同步代码块时，首先进入entryList
    有一个线程获取到monitor锁后，就赋值给当前线程，并且计数器+1
    如果线程调用wait方法，将释放锁，当前线程置为null，计数器-1，同时进入waitSet等待被唤醒，调用notify或者notifyAll之后又会进入entryList竞争锁
    如果线程执行完毕，同样释放锁，计数器-1，当前线程置为null
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-69.png)  

-->
&emsp; 下面再看一下加锁的代码：  

```text
void ATTR ObjectMonitor::enter(TRAPS) {
  // The following code is ordered to check the most common cases first
  // and to reduce RTS->RTO cache line upgrades on SPARC and IA32 processors.

  //获取当前线程指针
  Thread * const Self = THREAD ;
  void * cur ;

  //尝试通过CAS将OjectMonitor的_owner设置为当前线程；
  cur = Atomic::cmpxchg_ptr (Self, &_owner, NULL) ;

  //尝试失败
  if (cur == NULL) {
     // Either ASSERT _recursions == 0 or explicitly set _recursions = 0.
     assert (_recursions == 0   , "invariant") ;
     assert (_owner      == Self, "invariant") ;
     // CONSIDER: set or assert OwnerIsThread == 1
     return ;
  }

  //如果cur等于当前线程，说明当前线程已经持有锁，即为锁重入，_recursions自增，并获得锁。 
  if (cur == Self) {
     // TODO-FIXME: check for integer overflow!  BUGID 6557169.
     _recursions ++ ;
     return ;
  }

  //第一次设置_owner成功,锁的重入次数_recursions设置为1，_owner设置为当前线程，
  if (Self->is_lock_owned ((address)cur)) {
    assert (_recursions == 0, "internal state error");
    _recursions = 1 ;
    // Commute owner from a thread-specific on-stack BasicLockObject address to
    // a full-fledged "Thread *".
    _owner = Self ;
    OwnerIsThread = 1 ;
    return ;
  }

    //代码省略。。。。。

    // TODO-FIXME: change the following for(;;) loop to straight-line code.
    //如果竞争是失败的，会进入下面中的无线循环，反复调用EnterI方法，自旋尝试获取锁
    for (;;) {
      jt->set_suspend_equivalent();
      // cleared by handle_special_suspend_equivalent_condition()
      // or java_suspend_self()

      EnterI (THREAD) ;

      if (!ExitSuspendEquivalent(jt)) break ;

      //
      // We have acquired the contended monitor, but while we were
      // waiting another thread suspended us. We don't want to enter
      // the monitor while suspended because that would surprise the
      // thread that suspended us.
      //
          _recursions = 0 ;
      _succ = NULL ;
      exit (false, Self) ;

      jt->java_suspend_self();
    }

    //代码省略。。。。。。。。。。。。。
```

&emsp; 下面再看一下释放锁的代码：  

```text
void ATTR ObjectMonitor::exit(bool not_suspended, TRAPS) {
   Thread * Self = THREAD ;
   if (THREAD != _owner) {
     if (THREAD->is_lock_owned((address) _owner)) {
       // Transmute _owner from a BasicLock pointer to a Thread address.
       // We don't need to hold _mutex for this transition.
       // Non-null to Non-null is safe as long as all readers can
       // tolerate either flavor.
       assert (_recursions == 0, "invariant") ;
       _owner = THREAD ;
       _recursions = 0 ;
       OwnerIsThread = 1 ;
     } else {
       // NOTE: we need to handle unbalanced monitor enter/exit
       // in native code by throwing an exception.
       // TODO: Throw an IllegalMonitorStateException ?
       TEVENT (Exit - Throw IMSX) ;
       assert(false, "Non-balanced monitor enter/exit!");
       if (false) {
          THROW(vmSymbols::java_lang_IllegalMonitorStateException());
       }
       return;
     }
   }

   if (_recursions != 0) {
     _recursions--;        // this is simple recursive enter
     TEVENT (Inflated exit - recursive) ;
     return ;
   }

   //代码省略。。。。。。。。。。。。。
}
```

## 1.2. Synchronized底层实现  

```java
public class SyncDemo {

    public Synchronized void play() {}

    public void learn() {
        Synchronized(this) {
        }
    }
}
```

<!-- 编译完成，我们去对应目录执行 javap -c xxx.class 命令查看反编译的文件： 
https://juejin.cn/post/6888112467747176456 
先将SynchronizedDemo.java使用javac SynchronizedDemo.java命令将其编译成SynchronizedDemo.class。然后使用javap -c SynchronizedDemo.class反编译字节码。  
通过javap -v -c SynchronizedMethodDemo.class命令反编译SynchronizedMethodDemo类。-v参数即-verbose，表示输出反编译的附加信息。下面以反编译普通方法为例。  
-->
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

  public Synchronized void play();
    descriptor: ()V
    flags: ACC_PUBLIC, ACC_Synchronized
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

* **<font color = "red">方法同步：依靠的是方法修饰符上的ACC_Synchronized实现。</font>**  
* **<font color = "red">代码块同步：使用monitorenter和monitorexit指令实现。</font>**  

### 1.2.1. 同步代码块  
&emsp; monitorenter指令插入到同步代码块的开始位置，monitorexit指令插入到同步代码块的结束位置，<font color = "red">JVM需要保证每一个monitorenter都有一个monitorexit与之相对应。</font>  

&emsp; 两条指令的作用：  
* monitorenter：  
&emsp; 每一个对象都会和一个监视器monitor关联。监视器被占用时会被锁住，其他线程无法来获取该monitor。  
&emsp; <font color = "red">线程执行monitorenter指令时尝试获取monitor的所有权，当monitor被占用时就会处于锁定状态。</font>过程如下：
    1. <font color = "red">如果monitor的进入数为0，则该线程进入monitor，然后将进入数设置为1，该线程即为monitor的所有者。</font>  
    2. 如果线程已经占有该monitor，只是重新进入，则进入monitor的进入数加1，所以Synchronized关键字实现的锁是可重入的锁。  
    3. 如果其他线程已经占用了monitor，则该线程进入阻塞状态，直到monitor的进入数为0，再重新尝试获取monitor的所有权。  
* monitorexit：  
&emsp; 执行monitorexit的线程必须是objectref所对应的monitor的所有者。  
&emsp; 指令执行时，monitor的进入数减1，如果减1后进入数为0，当前线程释放monitor，不再是这个monitor的所有者。其他被这个monitor阻塞的线程可以尝试去获取这个monitor的所有权。  

&emsp; **<font color = "red">同步代码块中会出现两次的monitorexit。</font>** 这是因为一个线程对一个对象上锁了，后续就一定要解锁，第二个monitorexit是为了保证在线程异常时，也能正常解锁，避免造成死锁。  

&emsp; 总结：Synchronized的实现原理，Synchronized的语义底层是通过一个monitor的对象来完成，其实wait/notify等方法也依赖于monitor对象，这就是为什么只有在同步的块或者方法中才能调用wait/notify等方法，否则会抛出java.lang.IllegalMonitorStateException的异常的原因。  

<!-- 
monitorenter ：
每个对象有一个监视器锁(monitor)。当monitor被占用时就会处于锁定状态，线程执行monitorenter指令时尝试获取monitor的所有权，过程如下：

1、如果monitor的进入数为0，则该线程进入monitor，然后将进入数设置为1，该线程即为monitor的所有者。

2、如果线程已经占有该monitor，只是重新进入，则进入monitor的进入数加1.

3.如果其他线程已经占用了monitor，则该线程进入阻塞状态，直到monitor的进入数为0，再重新尝试获取monitor的所有权。

monitorexit：

　　执行monitorexit的线程必须是objectref所对应的monitor的所有者。

　　指令执行时，monitor的进入数减1，如果减1后进入数为0，那线程退出monitor，不再是这个monitor的所有者。其他被这个monitor阻塞的线程可以尝试去获取这个 monitor 的所有权。

　　Synchronized的语义底层是通过一个monitor的对象来完成，其实wait/notify等方法也依赖于monitor对象，这就是为什么只有在同步的块或者方法中才能调用wait/notify等方法，否则会抛出java.lang.IllegalMonitorStateException的异常的原因。

「monitorenter」：
Java对象天生就是一个Monitor，当monitor被占用，它就处于锁定的状态。
每个对象都与一个监视器关联。且只有在有线程持有的情况下，监视器才被锁定。
执行monitorenter的线程尝试获得monitor的所有权：

如果与objectref关联的监视器的条目计数为0，则线程进入监视器，并将其条目计数设置为1。然后，该线程是monitor的所有者。如果线程已经拥有与objectref关联的监视器，则它将重新进入监视器，从而增加其条目计数。这个就是锁重入。如果另一个线程已经拥有与objectref关联的监视器，则该线程将阻塞，直到该监视器的条目计数为零为止，然后再次尝试获取所有权。
「monitorexit」：
一个或多个MonitorExit指令可与Monitorenter指令一起使用，它们共同实现同步语句。
尽管可以将monitorenter和monitorexit指令用于提供等效的锁定语义，但它们并未用于同步方法的实现中。
JVM在完成monitorexit时的处理方式分为正常退出和出现异常时退出：

常规同步方法完成时监视器退出由Java虚拟机的返回指令处理。也就是说程序正常执行完毕的时候，JVM有一个指令会隐式的完成monitor的退出---monitorexit，这个指令是athrow。如果同步语句出现了异常时，JVM的异常处理机制也能monitorexit。
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-57.png)  
简单的加锁解锁过程
因此，执行同步代码块后首先要执行monitorenter指令，退出的时候monitorexit指令。  
-->

### 1.2.2. 同步方法  
&emsp; 当JVM执行引擎执行某一个方法时，其会从方法区中获取该方法的access_flags，检查其是否有ACC_SYNCRHONIZED标识符，若是有该标识符，则说明当前方法是同步方法，需要先获取当前对象的monitor，再来执行方法，方法执行完后再释放monitor。在方法执行期间，其他任何线程都无法再获得同一个monitor对象。 其实本质上没有区别，只是方法的同步是一种隐式的方式来实现，无需通过字节码来完成。  