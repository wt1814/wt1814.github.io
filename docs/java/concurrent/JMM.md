

<!-- TOC -->

- [1. JMM](#1-jmm)
    - [1.1. 计算机CPU缓存模型](#11-计算机cpu缓存模型)
        - [1.1.1. 三级缓存](#111-三级缓存)
        - [1.1.2. CPU缓存行](#112-cpu缓存行)
    - [1.2. JMM](#12-jmm)
        - [1.2.1. JMM中内存划分(主内存，工作内存和线程三者的交互关系)](#121-jmm中内存划分主内存工作内存和线程三者的交互关系)
        - [1.2.2. JMM内存间的交互操作](#122-jmm内存间的交互操作)
        - [1.2.3. 线程之间的通信和同步](#123-线程之间的通信和同步)
            - [1.2.3.1. ★★★线程之间的通信过程](#1231-★★★线程之间的通信过程)
    - [1.3. CPU的缓存一致性协议MESI](#13-cpu的缓存一致性协议mesi)
        - [1.3.1. 缓存失效/缓存不一致/缓存可见性](#131-缓存失效缓存不一致缓存可见性)
        - [1.3.2. 总线锁(性能低)](#132-总线锁性能低)
        - [1.3.3. MESI缓存一致性协议](#133-mesi缓存一致性协议)
            - [1.3.3.1. 总线嗅探](#1331-总线嗅探)
            - [1.3.3.2. 总线风暴](#1332-总线风暴)
    - [1.4. 伪共享](#14-伪共享)
        - [1.4.1. 伪共享问题](#141-伪共享问题)
        - [1.4.2. 伪共享示例](#142-伪共享示例)
        - [1.4.3. 避免伪共享](#143-避免伪共享)
        - [1.4.4. 实际应用(LongAdder类)](#144-实际应用longadder类)
        - [1.4.5. 小结](#145-小结)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-10.png)  

&emsp; **<font color = "red">总结：</font>**  
&emsp; **<font color = "clime">JMM相关：</font>**  
1. JVM内存划分；  
2. 单个线程操作时，8种内存间交换操作指令；  
3. 线程之间的通信和同步。线程之间的通信过程：线程对变量的操作(读取赋值等)必须在工作内存中进行，首先要将变量从主内存拷贝到自己的工作内存空间，然后对变量进行操作，操作完成后再将变量写回主内存，不能直接操作主内存中的变量，</font>各个线程中的工作内存中存储着主内存中的变量副本拷贝，<font color = "red">因此不同的线程间无法访问对方的工作内存，线程间的通信(传值)必须通过主内存来完成。</font>    

&emsp; **<font color = "clime">缓存一致性协议MESI：</font>**  

&emsp; 缓存一致性问题的产生。  
1. 缓存一致性协议中cpu通过总线嗅探机制可以感知到数据的变化从而将自己缓存里的数据失效。  
2. 总线嗅探：每个CPU不断嗅探总线上传播的数据来检查自己缓存值是否过期了，如果处理器发现自己的缓存行对应的内存地址被修改，就会将当前处理器的缓存行设置为无效状态，当处理器对这个数据进行修改操作的时候，会重新从内存中把数据读取到处理器缓存中。  
3. 总线嗅探会存在总线风暴问题。  

# 1. JMM  
<!--
CPU缓存一致性协议
https://blog.csdn.net/w1453114339/article/details/107563613
-->
<!-- 
~~
https://mp.weixin.qq.com/s/0_TDPDx8q2HmKCMyupWuNA
https://mp.weixin.qq.com/s?__biz=MzAwNDA2OTM1Ng==&mid=2453142004&idx=1&sn=81ccddb6c8b37114c022c4ad50368ecf&scene=21#wechat_redirect
-->


## 1.1. 计算机CPU缓存模型  
...

### 1.1.1. 三级缓存
&emsp; CPU是计算机的心脏，所有运算和程序最终都要由它来执行。  
&emsp; 主内存(RAM)是数据存放的地方，CPU 和主内存之间有好几级缓存，因为即使直接访问主内存也是非常慢的。  
&emsp; 如果对一块数据做相同的运算多次，那么在执行运算的时候把它加载到离 CPU 很近的地方就有意义了，比如一个循环计数，不想每次循环都跑到主内存去取这个数据来增长它。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-50.png)  
&emsp; <font color = "red">越靠近CPU的缓存越快也越小。</font>所以L1缓存很小但很快，并且紧靠着在使用它的 CPU 内核。L2大一些，也慢一些，并且仍然只能被一个单独的 CPU 核使用。L3 在现代多核机器中更普遍，仍然更大，更慢，并且被单个插槽上的所有 CPU 核共享。最后，主存保存着程序运行的所有数据，它更大，更慢，由全部插槽上的所有 CPU 核共享。  
&emsp; 当 CPU 执行运算的时候，它先去 L1 查找所需的数据，再去 L2，然后是 L3，最后如果这些缓存中都没有，所需的数据就要去主内存拿。走得越远，运算耗费的时间就越长。所以如果进行一些很频繁的运算，要确保数据在 L1 缓存中。  

### 1.1.2. CPU缓存行  
&emsp; 缓存是由缓存行组成的，通常是64字节(常用处理器的缓存行是64字节的，比较旧的处理器缓存行是 32 字节)，并且它有效地引用主内存中的一块地址。  
&emsp; <font color = "red">一个Java的long类型是8字节，因此在一个缓存行中可以存8个long类型的变量。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-51.png)  

## 1.2. JMM  
<!-- 
&emsp; Java内存模型(Java Memory Model，JMM)是一种符合顺序一致内存模型规范的，屏蔽了各种硬件和操作系统的访问差异的，保证了Java程序在各种平台下对内存的访问都能保证效果一致的机制及规范。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-6.png)  
-->
&emsp; JMM是指Java内存模型(Java Memory Model)，本身是一种抽象的概念，实际上并不存在，它描述的是一组规则或规范，通过这组规范定义了程序中各个变量(包括实例字段，静态字段和构成数组对象的元素)的访问方式。  

1. 定义程序中各种变量的访问规则
2. 把变量值存储到内存的底层细节
3. 从内存中取出变量值的底层细节

### 1.2.1. JMM中内存划分(主内存，工作内存和线程三者的交互关系)
&emsp; Java线程内存模型跟cpu缓存模型类似，是基于cpu缓存模型来建立的，Java线程内存模型是标准化的，屏蔽掉了底层不同计算机的区别。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-7.png)   
&emsp; Java内存模型划分： 

* 主内存：Java内存模型规定了所有变量都存储在主内存(Main Memory)中。（此处的主内存与物理硬件的主内存RAM 名字一样，两者可以互相类比，但此处仅是虚拟机内存的一部分）。  
    * Java堆中对象实例数据部分
    * 对应于物理硬件的内存
* 工作内存：每条线程都有自己的工作内存(Working Memory，又称本地内存，可与CPU高速缓存类比)， **<font color = "red">线程的工作内存中保存了该线程使用到的主内存中的共享变量的副本拷贝。线程对变量的所有操作都必须在工作内存进行，而不能直接读写主内存中的变量。</font>**  
    * Java栈中的部分区域
    * 优先存储于寄存器和高速缓存

<!-- 
&emsp; Java内存模型的几个规范：  
1. 所有变量存储在主内存  
2. 主内存是虚拟机内存的一部分  
3. 每条线程有自己的工作内存  
4. 线程的工作内存保存变量的主内存副本  
5. 线程对变量的操作必须在工作内存中进行  
6. 不同线程之间无法直接访问对方工作内存中的变量  
7. 线程间变量值的传递均需要通过主内存来完成  
-->

### 1.2.2. JMM内存间的交互操作  
&emsp; Java内存模型为主内存和工作内存间的变量拷贝及同步定义了8种原子性操作指令。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-8.png)   

* read(读取)：从主内存读取数据。  
* load(载入)：将主内存读取到的数据。  
* use(使用)：从工作内存读取数据来计算。  
* assign(赋值)：将计算好的值重新赋值到工作内存中。
* store(存储)：将工作内存数据写入主内存。 
* write(写入)：将store过去的变量值赋值给主内存中的变量。 
* lock(锁定)：将主内存变量加锁，标识为线程独占状态。  
* unlock(解锁)：将主内存变量解锁，解锁后其他线程可以锁定该变量。  

&emsp; 要把一个变量从主内存中复制到工作内存，就需要按顺寻地执行read和load操作，如果把变量从工作内存中同步回主内存中，就要按顺序地执行store和write操作。  
&emsp; Java内存模型只要求上述两个操作必须按顺序执行，而没有保证必须是连续执行。也就是read和load之间，store和write之间是可以插入其他指令的。  

&emsp; Java内存模型还规定了在执行上述八种基本操作时，必须满足如下规则：  

* 不允许read和load、store和write操作之一单独出现。  
* 不允许一个线程丢弃它的最近assign的操作，即变量在工作内存中改变了之后必须同步到主内存中。  
* 不允许一个线程无原因地(没有发生过任何assign操作)把数据从工作内存同步回主内存中。  
* 一个新的变量只能在主内存中诞生，不允许在工作内存中直接使用一个未被初始化(load或assign)的变量。即就是对一个变量实施use和store操作之前，必须先执行过了assign和load操作。  
* 一个变量在同一时刻只允许一条线程对其进行lock操作，lock和unlock必须成对出现  
* 如果对一个变量执行lock操作，将会清空工作内存中此变量的值，在执行引擎使用这个变量前需要重新执行load或assign操作初始化变量的值  
* 如果一个变量事先没有被lock操作锁定，则不允许对它执行unlock操作；也不允许去unlock一个被其他线程锁定的变量。  
* 对一个变量执行unlock操作之前，必须先把此变量同步到主内存中(执行store和write操作)。  

### 1.2.3. 线程之间的通信和同步  
&emsp; **JMM定义了Java虚拟机(JVM)在计算机内存(RAM)中的工作方式。JMM主要规定了以下两点：**  

* 规定了一个线程如何以及何时可以看到其他线程修改过后的共享变量的值，即线程之间共享变量的可见性。  
* 如何在需要的时候对共享变量进行同步。  

&emsp; **<font color = "lime">在并发编程需要处理的两个关键问题是：线程之间如何通信和线程之间如何同步，即以上两条标准的体现。</font>** **<font color = "red">线程通信是一种手段，而线程同步是一种目的，即线程通信的主要目的是用于线程同步。线程同步是为了解决线程安全问题。</font>**

* 线程通信  

        通信是指线程之间以何种机制来交换信息。在命令式编程中，线程之间的通信机制有两种：共享内存和消息传递。
        在共享内存的并发模型里，线程之间共享程序的公共状态，线程之间通过写-读内存中的公共状态来隐式进行通信。
        在消息传递的并发模型里，线程之间没有公共状态，线程之间必须通过明确的发送消息来显式进行通信。

* 线程同步  

        同步是指程序用于控制不同线程之间操作发生相对顺序的机制。
        在共享内存的并发模型里，同步是显式进行的。程序员必须显式指定某个方法或某段代码需要在线程之间互斥执行。
        在消息传递的并发模型里，由于消息的发送必须在消息的接收之前，因此同步是隐式进行的。    

&emsp; **<font color = "red">Java的并发采用的是共享内存模型</font>，**Java线程之间的通信总是隐式进行，整个通信过程对程序员完全透明。  

#### 1.2.3.1. ★★★线程之间的通信过程  
&emsp; 由于JVM运行程序的实体是线程，而每个线程创建时JVM都会为其创建一个工作内存(有些地方称为栈空间)，工作内存是每个线程的私有数据区域。  
&emsp; 而Java内存模型中规定所有变量都存储在主内存，主内存是共享内存区域，所有线程都可以访问，<font color = "clime">但线程对变量的操作(读取赋值等)必须在工作内存中进行，首先要将变量从主内存拷贝到自己的工作内存空间，然后对变量进行操作，操作完成后再将变量写回主内存，不能直接操作主内存中的变量，</font>各个线程中的工作内存中存储着主内存中的变量副本拷贝，<font color = "red">因此不同的线程间无法访问对方的工作内存，线程间的通信(传值)必须通过主内存来完成。</font>其简要访问过程：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-42.png)   

## 1.3. CPU的缓存一致性协议MESI
&emsp; 当多个CPU持有的缓存都来自同一个主内存的拷贝，当有某个CPU修改了这个主内存数据后，而其他CPU并不知道，那拷贝的内存将会和主内存不一致，这就是缓存不一致。那如何来保证缓存一致呢？这里就需要操作系统来共同制定一个同步规则来保证。 

### 1.3.1. 缓存失效/缓存不一致/缓存可见性  
&emsp; 当CPU写数据时，如果发现操作的变量是共享变量，即在其它CPU中也存在该变量的副本，系统会发出信号通知其它CPU将该内存变量的缓存行设置为无效。如下图所示，CPU1和CPU3 中num=1已经失效了。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-44.png)  
&emsp; 当其它CPU读取这个变量的时，发现自己缓存该变量的缓存行是无效的，那么它就会从内存中重新读取。  

<!-- 
&emsp; 如下图所示，CPU1和CPU3发现缓存的num值失效了，就重新从内存读取，num值更新为2。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-45.png)  
-->

&emsp; 怎么解决缓存一致性问题呢？使用总线锁或缓存锁。  

### 1.3.2. 总线锁(性能低)  
&emsp; 早期，cpu从主内存读取数据到高速缓存，会在总线对这个数据加锁，这样其他cpu无法去读或写这个数据，直到这个cpu使用完数据释放锁之后其他cpu才能读取该数据。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-103.png)  


### 1.3.3. MESI缓存一致性协议  
<!-- 
~~
https://mp.weixin.qq.com/s/yWifJmirZNnBrAIZrpJwyg
看懂这篇，才能说了解并发底层技术！ 
https://mp.weixin.qq.com/s/SZl2E5NAhpYM4kKv9gyQOQ
-->
&emsp; 缓存一致性协议有很多种，MESI(Modified-Exclusive-Shared-Invalid)协议其实是目前使用很广泛的缓存一致性协议，x86处理器所使用的缓存一致性协议就是基于MESI的。  

&emsp; **<font color = "clime">多个cpu从主内存读取同一个数据到各自的高速缓存，当其中某个cpu修改了缓存里的数据，该数据会马上同步回主内存，其他cpu通过</font>** **<font color = "clime">总线嗅探机制</font>** **<font color = "lime">可以感知到数据的变化从而将自己缓存里的数据失效。</font>**  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-47.png)  

#### 1.3.3.1. 总线嗅探  
&emsp; **<font color = "red">每个CPU不断嗅探总线上传播的数据来检查自己缓存值是否过期了，如果处理器发现自己的缓存行对应的内存地址被修改，就会将当前处理器的缓存行设置为无效状态，当处理器对这个数据进行修改操作的时候，会重新从内存中把数据读取到处理器缓存中。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-46.png)  

#### 1.3.3.2. 总线风暴
&emsp; 总线嗅探技术有哪些缺点？  
&emsp; 由于MESI缓存一致性协议，需要不断对主线进行内存嗅探，大量的交互会导致总线带宽达到峰值。   
&emsp; 因此不要滥用volatile，可以用锁来替代，看使用场景。  


## 1.4. 伪共享

<!-- 
https://blog.csdn.net/qq_28119741/article/details/102815659
-->

### 1.4.1. 伪共享问题
&emsp; <font color = "red">在程序运行的过程中，缓存每次更新都从主内存中加载连续的64个字节。因此，如果访问一个long类型的数组时，当数组中的一个值被加载到缓存中时，另外7个元素也会被加载到缓存中。</font>  
&emsp; 但是，如果使用的数据结构中的项在内存中不是彼此相邻的，比如链表，那么将得不到免费缓存加载带来的好处。  
&emsp; 不过，这种免费加载也有一个坏处。设想如果有个long类型的变量a，它不是数组的一部分，而是一个单独的变量，并且还有另外一个long类型的变量b紧挨着它，那么当加载a的时候将免费加载b。  
&emsp; 看起来似乎没有什么毛病，但是如果一个CPU核心的线程在对a进行修改，另一个CPU核心的线程却在对b进行读取。  
&emsp; 当前者修改 a 时，会把 a 和 b 同时加载到前者核心的缓存行中，更新完 a 后其它所有包含 a 的缓存行都将失效，因为其它缓存中的 a 不是最新值了。  
&emsp; 而当后者读取 b 时，发现这个缓存行已经失效了，需要从主内存中重新加载。  
&emsp; 请记住，缓存都是以缓存行作为一个单位来处理的，所以失效 a 的缓存的同时，也会把 b 失效，反之亦然。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-52.png)  
&emsp; 这样就出现了一个问题，<font color = "lime">b和a完全不相干，每次却要因为a的更新需要从主内存重新读取，它被缓存未命中给拖慢了。</font>  
&emsp; 这就是伪共享问题： **<font color = "lime">当多线程修改互相独立的变量时，如果这些变量共享同一个缓存行，就会无意中影响彼此的性能。</font>**  

### 1.4.2. 伪共享示例  
&emsp; 伪共享示例：  

```java
public class FalseSharingTest {

    public static void main(String[] args) throws InterruptedException {
        testPointer(new Pointer());
    }

    private static void testPointer(Pointer pointer) throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100000000; i++) {
                pointer.x++;
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000000; i++) {
                pointer.y++;
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println(System.currentTimeMillis() - start);
        System.out.println(pointer);
    }
}

class Pointer {
    volatile long x;
    volatile long y;
}
```
&emsp; 这个例子中，声明了一个 Pointer 的类，它包含x和y两个变量，一个线程对 x 进行自增1亿次，一个线程对y进行自增1亿次。  
&emsp; 可以看到，x和y完全没有任何关系，但是更新 x 的时候会把其它包含x的缓存行失效，同时也就失效了y，运行这段程序输出的时间为3890ms。  

### 1.4.3. 避免伪共享  
&emsp; 伪共享的原理中，一个缓存行是 64 个字节，一个 long 类型是 8 个字节，所以避免伪共享也很简单，笔者总结了下大概有以下三种方式：

1. <font color = "red">字节填充：在两个long类型的变量之间再加7个long类型</font>  
    &emsp; 可以把上面的Pointer改成下面这个结构：

    ```java
    class Pointer {
        volatile long x;
        long p1, p2, p3, p4, p5, p6, p7;
        volatile long y;
    }
    ```
    &emsp; 再次运行程序，会发现输出时间神奇的缩短为了695ms。

2. <font color = "red">重新创建自己的long类型，而不是java自带的long</font>  
    &emsp; 修改Pointer如下：

    ```java
    class Pointer {
        MyLong x = new MyLong();
        MyLong y = new MyLong();
    }

    class MyLong {
        volatile long value;
        long p1, p2, p3, p4, p5, p6, p7;
    }
    ```
    &emsp; 同时把 pointer.x++; 修改为 pointer.x.value++;，把 pointer.y++; 修改为 pointer.y.value++;，再次运行程序发现时间是724ms。  
3. **<font color = "lime">使用@sun.misc.Contended注解(java8)</font>**  
    &emsp; 修改MyLong如下：

    ```java
    @sun.misc.Contended
    class MyLong {
        volatile long value;
    }
    ```
    &emsp; 默认使用这个注解是无效的，需要在JVM启动参数加上-XX:-RestrictContended才会生效，，再次运行程序发现时间是718ms。  

&emsp; 注意，以上三种方式中的前两种是通过加字段的形式实现的，加的字段又没有地方使用，可能会被jvm优化掉，所以建议使用第三种方式。  

### 1.4.4. 实际应用(LongAdder类)
&emsp; 在Java中提供了多个原子变量的操作类，就是比如AtomicLong、AtomicInteger这些，通过CAS的方式去更新变量，但是失败会无限自旋尝试，导致CPU资源的浪费。  
&emsp; 为了解决高并发下的这个缺点，JDK8中新增了LongAdder类，它的使用就是对解决伪共享的实际应用。  

### 1.4.5. 小结  
1. CPU具有多级缓存，越接近CPU的缓存越小也越快；  
2. CPU缓存中的数据是以缓存行为单位处理的；  
3. CPU缓存行能带来免费加载数据的好处，所以处理数组性能非常高；  
4. CPU缓存行也带来了弊端，多线程处理不相干的变量时会相互影响，也就是伪共享；  
5. 避免伪共享的主要思路就是让不相干的变量不要出现在同一个缓存行中；  
6. 一是每两个变量之间加七个long类型；  
7. 二是创建自己的long类型，而不是用原生的；  
8. 三是使用java8提供的注解；  

