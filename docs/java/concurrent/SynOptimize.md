<!-- TOC -->

- [1. Synchronized锁优化](#1-synchronized锁优化)
    - [1.1. 锁消除](#11-锁消除)
    - [1.2. 锁粗化](#12-锁粗化)
    - [1.3. 锁降级](#13-锁降级)
    - [1.4. 锁升级](#14-锁升级)
        - [1.4.1. 偏向锁](#141-偏向锁)
            - [1.4.1.1. 偏向锁升级](#1411-偏向锁升级)
            - [1.4.1.2. 偏向锁的性能(偏向锁的启动)](#1412-偏向锁的性能偏向锁的启动)
            - [1.4.1.3. 偏向锁的失效](#1413-偏向锁的失效)
        - [1.4.2. 轻量级锁](#142-轻量级锁)
        - [1.4.3. 重量级锁](#143-重量级锁)
        - [1.4.4. 锁状态总结](#144-锁状态总结)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "clime">锁降级：</font>** <font color = "red">Hotspot在1.8开始有了锁降级。在STW期间JVM进入安全点时，如果发现有闲置的monitor(重量级锁对象)，会进行锁降级。</font>   
2. 锁升级
	1. 偏向锁：  
        1.  **<font color = "bule">偏向锁状态</font>**  
            * **<font color = "clime">匿名偏向(Anonymously biased)</font>** 。在此状态下thread pointer为NULL(0)，意味着还没有线程偏向于这个锁对象。第一个试图获取该锁的线程将会面临这个情况，使用原子CAS指令可将该锁对象绑定于当前线程。这是允许偏向锁的类对象的初始状态。
            * **<font color = "clime">可重偏向(Rebiasable)</font>** 。在此状态下，偏向锁的epoch字段是无效的（与锁对象对应的class的mark_prototype的epoch值不匹配）。下一个试图获取锁对象的线程将会面临这个情况，使用原子CAS指令可将该锁对象绑定于当前线程。**在批量重偏向的操作中，未被持有的锁对象都被置于这个状态，以便允许被快速重偏向。**
            * **<font color = "clime">已偏向(Biased)</font>** 。这种状态下，thread pointer非空，且epoch为有效值——意味着其他线程正在持有这个锁对象。
        2. 偏向锁获取： 
            1. 判断是偏向锁时，检查对象头Mark Word中记录的Thread Id是否是当前线程ID。  
            2. 如果对象头Mark Word中Thread Id不是当前线程ID，则进行CAS操作，企图将当前线程ID替换进Mark Word。如果当前对象锁状态处于匿名偏向锁状态（可偏向未锁定），则会替换成功（ **<font color = "clime">将Mark Word中的Thread id由匿名0改成当前线程ID，</font>** 在当前线程栈中找到内存地址最高的可用Lock Record，将线程ID存入）。  
            3. 如果对象锁已经被其他线程占用，则会替换失败，开始进行偏向锁撤销，这也是偏向锁的特点，一旦出现线程竞争，就会撤销偏向锁； 
        3. 偏向锁撤销： 
            1. 等到安全点，检查持有偏向锁的线程是否还存活。如果线程还存活，则检查线程是否在执行同步代码块中的代码，如果是，则升级为轻量级锁，进行CAS竞争锁； 
            2. 如果持有偏向锁的线程未存活，或者持有偏向锁的线程未在执行同步代码块中的代码， **<font color = "red">则进行校验是否允许重偏向。</font>**   
                1. **<font color = "clime">如果不允许重偏向，则撤销偏向锁，将Mark Word设置为无锁状态（未锁定不可偏向状态），然后升级为轻量级锁，进行CAS竞争锁；</font><font color = "blue">(偏向锁被被重置为无锁状态，这种策略是为了提高获得锁和释放锁的效率。)</font>**     
                2. 如果允许重偏向，设置为匿名偏向锁状态，CAS将偏向锁重新指向线程A（在对象头和线程栈帧的锁记录中存储当前线程ID）； 
            3. 唤醒暂停的线程，从安全点继续执行代码。 
	2. 轻量级锁：
		1. 偏向锁升级为轻量级锁之后，对象的Markword也会进行相应的的变化。   
            1. 线程在自己的栈桢中创建锁记录LockRecord。
            2. 将锁对象的对象头中的MarkWord复制到线程刚刚创建的锁记录中。
            3. 将锁记录中的Owner指针指向锁对象。
            4. 将锁对象的对象头的MarkWord替换为指向锁记录的指针。
		2. 自旋锁：轻量级锁在加锁过程中，用到了自旋锁。自旋锁分为固定次数自旋锁（在JDK 1.6之前，自旋次数默认是10次）和自适应自旋锁。
		3. 新线程获取轻量级锁
			1. 获取轻量锁过程当中会在当前线程的虚拟机栈中创建一个Lock Record的内存区域去存储获取锁的记录DisplacedMarkWord，
			2. 然后使用CAS操作将锁对象的Mark Word更新成指向刚刚创建的Lock Record的内存区域DisplacedMarkWord的地址。  
		4. 已经获取轻量级锁的线程的解锁： **<font color = "red">轻量级锁的锁释放逻辑其实就是获得锁的逆向逻辑，通过CAS操作把线程栈帧中的LockRecord替换回到锁对象的MarkWord中。</font>** 
    3. 重量级锁  
    &emsp; **<font color = "clime">为什么有了自旋锁还需要重量级锁？</font>**  
    &emsp; 偏向锁、自旋锁都是用户空间完成。重量级锁是需要向内核申请。  
    &emsp; 自旋是消耗CPU资源的，如果锁的时间长，或者自旋线程多，CPU会被大量消耗；重量级锁有等待队列，所有拿不到锁的线程进入等待队列，不需要消耗CPU资源。  


# 1. Synchronized锁优化
<!--
详解synchronized和锁升级，以及偏向锁和轻量级锁的升级 
https://mp.weixin.qq.com/s/ZCuqrA8W3qcCnjwZu6A4lQ
且听我一个故事讲透一个锁原理之synchronized 
https://mp.weixin.qq.com/s/i1jtqQcP7kz5uS86GZ6Lgg
轻量级锁到底是怎么回事啊啊啊啊
https://mp.weixin.qq.com/s/ds78cpv8Yv5CdrNsLI9sYg
偏向锁的降级
https://blog.csdn.net/zxx901221/article/details/83022095
Synchronized
https://www.cnblogs.com/dennyzhangdd/p/6734638.html
初始Synchronized关键字的偏向锁、轻量锁、重量锁 
https://mp.weixin.qq.com/s/AloGilUSxjoNVDHTfq1ZGQ
关于 synchronized 锁优化 
https://mp.weixin.qq.com/s/XHrAXyLbJf5Rs6UbXiYY1A
-->
&emsp; 为了进一步改进高效并发，HotSpot虚拟机开发团队在JDK 5升级到JDK 6版本上花费了大量精力实现各种锁优化。如适应性自旋、锁消除、锁粗化、偏向锁和轻量级锁等，这些技术都是为了在线程之间更高效地共享数据及解决竞争问题，从而提高程序的执行效率。  

## 1.1. 锁消除  
<!-- https://juejin.im/post/5d2303b5f265da1b8f1ae5b0 -->
&emsp; 锁消除是指虚拟机即时编译器在运行时，对一些代码上要求同步，但是被检测到不可能存在共享数据竞争的锁进行清除。锁清除的主要判定依据来源于`逃逸分析`的数据支持，如果判断在一段代码中，堆上的所有数据都不会逃逸出去从而被其他线程访问到，那就可以把它们当做栈上数据对待，认为它们是线程私有的，同步加锁自然就无需进行。  
&emsp; 简单来说，Java中使用同步来保证数据的安全性，但是<font color = "red">对于一些明显不会产生竞争的情况下，Jvm会根据现实执行情况对代码进行锁消除以提高执行效率。</font>  

&emsp; 示例：  

```java
public void add(String str1,String str2){
         StringBuffer sb = new StringBuffer();
         sb.append(str1).append(str2);
}
```
&emsp; StringBuffer是线程安全的，因为它的关键方法都是被Synchronized修饰过的，但看上面这段代码，会发现，sb这个引用只会在add方法中使用，不可能被其它线程引用(因为是局部变量，栈私有)，因此sb是不可能共享的资源，JVM会自动消除StringBuffer对象内部的锁。  

## 1.2. 锁粗化  
&emsp; 原则上，在编写代码的时候，总是推荐将同步块的作用范围限制得尽量小，一直在共享数据的实际作用域才进行同步，这样是为了使得需要同步的操作数量尽可能变小，如果存在锁竞争，那等待线程也能尽快拿到锁。  
&emsp; 大部分情况下，上面的原则都是没有问题的，但是如果一系列的连续操作都对同一个对象反复加锁和解锁，那么会带来很多不必要的性能消耗。  
&emsp; 如果虚拟机探测到有这样<font color = "clime">一串零碎的操作都对同一个对象加锁，将会把加锁同步的范围扩展(粗化)到整个操作序列到外部。</font>  

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
&emsp; JVM会检测到这样一连串的操作都对同一个对象加锁(while 循环内 100 次执行 append，没有锁粗化的就要进行 100 次加锁/解锁)，此时 JVM 就会将加锁的范围粗化到这一连串的操作的外部(比如 while 虚幻体外)，使得这一连串操作只需要加一次锁即可。  

## 1.3. 锁降级  
<!-- 
https://zhuanlan.zhihu.com/p/28505703
-->
&emsp; **<font color = "clime">锁降级：</font>** <font color = "red">Hotspot在1.8开始有了锁降级。在STW期间JVM进入安全点时如果发现有闲置的monitor(重量级锁对象)，会进行锁降级。</font>  

## 1.4. 锁升级  
&emsp; 锁主要存在四种状态，依次是：无锁状态（普通对象）、偏向锁状态、轻量级锁状态、重量级锁状态，它们会随着竞争的激烈而逐渐升级。锁升级流程如下：   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-79.png)   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-80.png)   


### 1.4.1. 偏向锁  
#### 1.4.1.1. 偏向锁升级
<!-- 
偏向锁
https://blog.csdn.net/sunboylife/article/details/89223962
https://baijiahao.baidu.com/s?id=1630535202760061296&wfr=spider&for=pc
https://baijiahao.baidu.com/s?id=1630535202760061296&wfr=spider&for=pc
-->
&emsp; <font color = "red">偏向锁定义：</font>偏向锁是指偏向于让第一个获取锁对象的线程，这个线程在之后获取该锁就不再需要进行同步操作。 
<!-- 
偏向锁：当线程访问同步块获取锁时，会在对象头和栈帧中的锁记录里存储偏向锁的线程ID，之后这个线程再次进入同步块时都不需要CAS来加锁和解锁了，偏向锁会永远偏向第一个获得锁的线程，如果后续没有其他线程获得过这个锁，持有锁的线程就永远不需要进行同步，反之，当有其他线程竞争偏向锁时，持有偏向锁的线程就会释放偏向锁。可以用过设置-XX:+UseBiasedLocking开启偏向锁。
偏向锁 - markword 上记录当前线程指针，下次同一个线程加锁的时候，不需要争用，只需要判断线程指针是否同一个，所以，偏向锁，偏向加锁的第一个线程 。hashCode备份在线程栈上 线程销毁，锁降级为无锁

顾名思义，偏向锁会偏向某个线程，其不会造成用户态与内核态的切换。偏向锁在第一次加锁时会偏向拿到锁的线程，当这个线程再来加锁时，就可以直接拿到锁而不用做其他的逻辑判断，所以在这种场景下其性能最高。不过，如果有其他线程再来加锁的话，JVM就会把偏向锁膨胀为轻量锁(没有锁竞争)或重量锁(有锁竞争)了。
-->
&emsp; 偏向锁的获得和撤销流程图解：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-31.png)   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-81.png)   


&emsp; 当JVM启用了偏向锁模式（JDK6以上默认开启），新创建对象的Mark Word中的Thread Id为0，说明此时处于可偏向但未偏向任何线程，也叫做匿名偏向状态(anonymously biased)。  

&emsp; **偏向锁获取：**  

1. 线程A第一次访问同步块时，先检测对象头Mark Word中的标志位是否为01，依此判断此时对象锁是否处于无锁状态或者偏向锁状态（匿名偏向锁）；  
2. 然后判断偏向锁标志位是否为1，如果不是，则进入轻量级锁逻辑（使用CAS竞争锁），如果是，则进入下一步流程；  
3. **判断是偏向锁时，检查对象头Mark Word中记录的Thread Id是否是当前线程ID，如果是，则表明当前线程已经获得对象锁，以后该线程进入同步块时，不需要CAS进行加锁，只会往当前线程的栈中添加一条Displaced Mark Word为空的Lock Record中，用来统计重入的次数（如图为当对象所处于偏向锁时，当前线程重入3次，线程栈帧中Lock Record记录）。**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-82.png)  
&emsp; 退出同步块释放偏向锁时，则依次删除对应Lock Record，但是不会修改对象头中的Thread Id；  
&emsp; 注：偏向锁撤销是指在获取偏向锁的过程中因不满足条件导致要将锁对象改为非偏向锁状态，而偏向锁释放是指退出同步块时的过程。  
4. **如果对象头Mark Word中Thread Id不是当前线程ID，则进行CAS操作，企图将当前线程ID替换进Mark Word。如果当前对象锁状态处于匿名偏向锁状态（可偏向未锁定），则会替换成功（将Mark Word中的Thread id由匿名0改成当前线程ID，在当前线程栈中找到内存地址最高的可用Lock Record，将线程ID存入），获取到锁，执行同步代码块；**  
5. **如果对象锁已经被其他线程占用，则会替换失败，开始进行偏向锁撤销，这也是偏向锁的特点，一旦出现线程竞争，就会撤销偏向锁；**  

&emsp; **偏向锁撤销：**  

6. 偏向锁的撤销需要等待全局安全点（safe point，代表了一个状态，在该状态下所有线程都是暂停的），暂停持有偏向锁的线程，检查持有偏向锁的线程状态（遍历当前JVM的所有线程，如果能找到，则说明偏向的线程还存活），如果线程还存活，则检查线程是否在执行同步代码块中的代码，如果是，则升级为轻量级锁，进行CAS竞争锁；  
&emsp; 注：每次进入同步块（即执行monitorenter）的时候都会以从高往低的顺序在栈中找到第一个可用的Lock Record，并设置偏向线程ID；每次解锁（即执行monitorexit）的时候都会从最低的一个Lock Record移除。所以如果能找到对应的Lock Record说明偏向的线程还在执行同步代码块中的代码。  
7. 如果持有偏向锁的线程未存活，或者持有偏向锁的线程未在执行同步代码块中的代码，则进行校验是否允许重偏向。  
8. 如果不允许重偏向，则撤销偏向锁，将Mark Word设置为无锁状态（未锁定不可偏向状态），然后升级为轻量级锁，进行CAS竞争锁；(**<font color = "red">偏向锁被被重置为无锁状态，这种策略是为了提高获得锁和释放锁的效率。</font>** )  
9. 如果允许重偏向，设置为匿名偏向锁状态，CAS将偏向锁重新指向线程A（在对象头和线程栈帧的锁记录中存储当前线程ID）；  
10. 唤醒暂停的线程，从安全点继续执行代码。  


<!--
1. **<font color = "red">偏向锁整体流程：</font>**
    1. **<font color = "clime">当锁对象第一次被线程获得的时候，进入偏向状态，标记为 |1|01|(前面对象内存布局图中说明了，这属于偏向锁状态)。同时使用CAS操作将线程ID (ThreadID)记录到Mark Word 中，</font>** 如果 CAS 操作成功，这个线程以后每次进入这个锁相关的同步块就不需要再进行任何同步操作。  
    2. <font color = "red">新线程去尝试获取这个锁时，</font>根据锁对象目前是否处于被锁定的状态决定是否撤销偏向(偏向模式设置为“0”)，<font color = "red">撤销后标志位恢复到未锁定(标志位为“01”)或轻量级锁定(标志位为“00”)的状态。</font>  
    &emsp; 新线程争抢锁成功，对原线程进行锁撤销；抢占失败，升级为轻量级锁。  
2. **<font color = "clime">新线程获取偏向锁：(根据偏向锁的状态以及cas是否成功，来决定是否撤销偏向锁、升级轻量级锁。)</font>**  
    1. **<font color = "clime">首先获取锁对象的Markword，判断是否处于可偏向状态。(biased_lock=1、且 ThreadId 为空)</font>**  
    2. **<font color = "red">如果是可偏向状态，则通过CAS操作，把当前线程的ID写入到MarkWord。</font>**   
        * 如果cas成功，表示已经获得了锁对象的偏向锁，接着执行同步代码块。  
        * 如果cas失败，说明有其他线程已经获得了偏向锁，这种情况说明当前锁存在竞争，需要撤销已获得偏向锁的线程，并且把它持有的锁升级为轻量级锁(这个操作需要等到全局安全点，也就是没有线程在执行字节码)才能执行。
    3. **<font color = "red">如果是已偏向状态，需要检查markword中存储的ThreadID是否等于当前线程的ThreadID。</font>**   
        * 如果相等，不需要再次获得锁，可直接执行同步代码块。
        * 如果不相等，说明当前锁偏向于其他线程，需要<font color = "red">撤销偏向锁并升级到轻量级锁</font>。
3. **<font color = "lime">偏向锁的撤销流程：</font>**  
    &emsp; 偏向锁的撤销并不是把对象恢复到无锁可偏向状态(因为偏向锁并不存在锁释放的概念)，而是在获取偏向锁的过程中，发现cas失败也就是存在线程竞争时，直接把被偏向的锁对象升级到被加了轻量级锁的状态。  
    &emsp; 对原持有偏向锁的线程进行撤销时，原获得偏向锁的线程有两种情况：  
    1. 原获得偏向锁的线程如果已经退出了临界区，也就是同步代码块执行完了， **<font color = "clime">那么这个时候会把对象头设置成无锁状态</font>** ，并且争抢锁的线程可以基于 CAS 重新偏向之前线程。
    2. 如果原获得偏向锁的线程的同步代码块还没执行完，处于临界区之内，这个时候会把原获得偏向锁的线程升级为轻量级锁后继续执行同步代码块。
 -->
<!--
撤销锁
如果有其他线程出现，尝试获取偏向锁，让偏向锁处于竞争状态，那么当前偏向锁就会撤销。撤销偏向锁时，首先会暂停持有偏向锁的线程，并将线程ID设为空，然后检查该线程是否存活：

    当暂停线程非存活，则设置对象头为无锁状态。
    当暂停线程存活，执行偏向锁的栈，最后对象头的保存其他获取到偏向锁的线程ID或者转向无锁状态。
-->

-------

&emsp; 引用《阿里手册：码出高效》的描述再理解一次：

        偏向锁是为了在资源没有被多线程竞争的情况下尽量减少锁带来的性能开销。
        在锁对象的对象头中有一个ThreadId字段，当第一个线程访问锁时，如果该锁没有被其他线程访问过，即 ThreadId字段为空，那么JVM让其持有偏向锁，并将ThreadId字段的值设置为该线程的ID。当下一次获取锁的时候，会判断ThreadId是否相等，如果一致就不会重复获取锁，从而提高了运行效率。
        如果存在锁的竞争情况，偏向锁就会被撤销并升级为轻量级锁。

&emsp; 偏向锁、轻量级锁的状态转化及对象Mark Word的关系如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-29.png)  
 
&emsp; 图中，偏向锁的重偏向和撤销偏向时，如何判断对象是否已经锁定？  
&emsp; HotSpot支持存储释放偏向锁，以及偏向锁的批量重偏向和撤销。这个特性可以通过JVM的参数进行切换，而且这是默认支持的。  
&emsp; Unlock状态下Mark Word的一个比特位用于标识该对象偏向锁是否被使用或者是否被禁止。如果该bit位为0，则该对象未被锁定，并且禁止偏向；如果该bit位为1，则意味着该对象处于以下三种状态：  

* 匿名偏向(Anonymously biased)。在此状态下thread pointer为NULL(0)，意味着还没有线程偏向于这个锁对象。第一个试图获取该锁的线程将会面临这个情况，使用原子CAS指令可将该锁对象绑定于当前线程。这是允许偏向锁的类对象的初始状态。
* 可重偏向(Rebiasable)。在此状态下，偏向锁的epoch字段是无效的(与锁对象对应的class的mark_prototype的epoch值不匹配)。下一个试图获取锁对象的线程将会面临这个情况，使用原子CAS指令可将该锁对象绑定于当前线程。**在批量重偏向的操作中，未被持有的锁对象都被置于这个状态，以便允许被快速重偏向。**
* 已偏向(Biased)。这种状态下，thread pointer非空，且epoch为有效值——意味着其他线程正在持有这个锁对象。

#### 1.4.1.2. 偏向锁的性能(偏向锁的启动)   
<!-- 
https://zhuanlan.zhihu.com/p/127884116
-->
&emsp; 偏向锁可以提高带有同步但无竞争的程序性能，但它同样是一个带有效益权衡(Trade Off)性质的优化，也就是说它并非总是对程序运行有利。 **<font color = "clime">如果程序中大多数的锁都总是被多个不同的线程访问，那偏向模式就是多余的。</font>** 在具体问题具体分析的前提下，有时候使用参数-XX：-UseBiasedLocking来禁止偏向锁优化反而可以提升性能。  
&emsp; 并且从锁升级的图中能看到两个路线，“偏向锁未启动-->普通对象”和“偏向锁已启动-->匿名偏向对象”。  
&emsp; JVM默认会设置一个4秒钟的偏向延迟，也就是说JVM启动4秒钟内创建出的所有对象都是不可偏向的(也就是上图中的无锁不可偏向状态)，如果对这些对象去加锁，加的会是轻量锁而不是偏向锁。  

#### 1.4.1.3. 偏向锁的失效  
&emsp; 如果计算过对象的hashcode()，则对象无法进入偏向状态。    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-39.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-70.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-40.png)  

&emsp; 轻量级锁重量级锁的hashCode存在什么地方？  
&emsp; 存在线程栈中，轻量级锁的LR中，或是代表重量级锁的ObjectMonitor的成员中。  

### 1.4.2. 轻量级锁  
&emsp; 轻量级锁在加锁过程中，用到了自旋锁。  

<!-- 
自旋锁：由于大部分时候，锁被占用的时间很短，共享变量的锁定时间也很短，所有没有必要挂起线程，用户态和内核态的来回上下文切换严重影响性能。自旋的概念就是让线程执行一个忙循环，可以理解为就是啥也不干，防止从用户态转入内核态，自旋锁可以通过设置-XX:+UseSpining来开启，自旋的默认次数是10次，可以使用-XX:PreBlockSpin设置。
-->
&emsp; **<font color = "clime">偏向锁是否一定比自旋锁效率高？</font>**  
&emsp; <font color = "red">不一定，在明确知道会有多线程竞争的情况下，偏向锁肯定会涉及锁撤销，这时候直接使用自旋锁。</font>  
&emsp; <font color = "red">JVM启动过程，一般会有很多线程竞争，所以默认情况启动时不打开偏向锁，过一段时间再打开。</font>  

&emsp; 轻量级锁及锁膨胀流程：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-30.png)  

1. <font color = "red">偏向锁升级为轻量级锁之后，对象的Markword也会进行相应的的变化。</font>  
    1. <font color = "clime">线程在自己的栈桢中创建锁记录LockRecord。</font>  
    2. 将锁对象的对象头中的MarkWord复制到线程刚刚创建的锁记录中。  
    3. <font color = "clime">将锁记录中的Owner指针指向锁对象。</font>  
    4. 将锁对象的对象头的MarkWord替换为指向锁记录的指针。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-32.png)  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-33.png)  
2. <font color = "red">自旋锁：</font>  
&emsp; 轻量级锁在加锁过程中，用到了自旋锁。自旋锁分为固定次数自旋锁和自适应自旋锁。轻量级锁是针对竞争锁对象线程不多且线程持有锁时间不长的场景，因为阻塞线程需要CPU从用户态转到内核态，代价很大，如果一个刚刚阻塞不久就被释放代价有大。  
&emsp;  自旋锁与自适应自旋：    
&emsp; <font color = "red">为了让线程等待，只需要让线程执行一个忙循环(自旋)，这项技术就是所谓的自旋锁。</font>引入自旋锁的原因是互斥同步对性能最大的影响是阻塞的实现，管钱线程和恢复线程的操作都需要转入内核态中完成，给并发带来很大压力。自旋锁让物理机器有一个以上的处理器的时候，能让两个或以上的线程同时并行执行。就可以让后面请求锁的那个线程 “稍等一下” ，但不放弃处理器的执行时间，看看持有锁的线程是否很快就会释放锁。为了让线程等待，只需让线程执行一个忙循环(自旋)，这项技术就是所谓的自旋锁。  
&emsp; 自旋锁虽然能避免进入阻塞状态从而减少开销，但是它需要进行忙循环操作占用 CPU 时间，它只适用于共享数据的锁定状态很短的场景。  
&emsp; 在 JDK 1.6之前，自旋次数默认是10次，用户可以使用参数-XX:PreBlockSpin来更改。  
&emsp; **<font color = "red">JDK1.6引入了自适应的自旋锁。自适应意味着自旋的时间不再固定了，而是由前一次在同一个锁上的自旋时间及锁的拥有者的状态来决定。(这个应该属于试探性的算法)。</font>**  
3. 新线程获取轻量级锁：  
&emsp; **<font color = "red">1. 获取轻量锁过程当中会在当前线程的虚拟机栈中创建一个Lock Record的内存区域去存储获取锁的记录DisplacedMarkWord。</font>  
&emsp; <font color = "clime">2. 然后使用CAS操作将锁对象的Mark Word更新成指向刚刚创建的Lock Record的内存区域DisplacedMarkWord的地址，</font>** 如果这个操作成功，就说明线程获取了该对象的锁，把对象的Mark Word 标记成 00，表示该对象处于轻量级锁状态。失败时，会判断是否是该线程之前已经获取到锁对象了，如果是就进入同步块执行。如果不是，那就是有多个线程竞争这个锁对象，那轻量锁就不适用于这个情况了，要膨胀成重量级锁。  

        线程A获取轻量级锁时会把对象头中的MarkWord复制一份到线程A的栈帧中创建用于存储锁记录的空间DisplacedMarkWord，然后使用CAS将对象头中的内容替换成线程A存储DisplacedMarkWord的地址。如果这时候出现线程B来获取锁，线程B也跟线程A同样复制对象头的MarkWord到自己的DisplacedMarkWord中，如果线程A锁还没释放，这时候那么线程B的CAS操作会失败，会继续自旋，当然不可能让线程B一直自旋下去，自旋到一定次数(固定次数/自适应)就会升级为重量级锁。 
4. 已经获取轻量级锁的线程的解锁。   
&emsp; **<font color = "red">轻量级锁的锁释放逻辑其实就是获得锁的逆向逻辑，通过CAS操作把线程栈帧中的LockRecord替换回到锁对象的MarkWord中。</font>** 如果成功，则释放锁。如果失败，表示当前锁存在竞争，释放锁，唤醒被挂起的线程，开始新一轮竞争。  

### 1.4.3. 重量级锁
<!-- 

为什么说Synchronized是重量级锁？

云霄：这个问题，上面有涉及，Synchronized底层是monitor实现，监视器锁monitor本质又是依赖于底层的操作系统的 Mutex Lock 来实现的，这就涉及到操作系统让线程从用户态切换到内核态。这个成本非常高，状态之间的转换需要相对比较长的时间，这就是为什么Synchronized 效率低的原因。因此，这种依赖于操作系统 Mutex Lock 所实现的锁我们称之为重量级锁。  

-->

参考[Synchronized底层原理](/docs/java/concurrent/SynBottom.md)  
&emsp; **<font color = "clime">为什么有了自旋锁还需要重量级锁？</font>**  
&emsp; 偏向锁、自旋锁都是用户空间完成。重量级锁是需要向内核申请。  
&emsp; 自旋是消耗CPU资源的，如果锁的时间长，或者自旋线程多，CPU会被大量消耗；重量级锁有等待队列，所有拿不到锁的线程进入等待队列，不需要消耗CPU资源。  
&emsp; **<font color = "clime">升级为重量级锁过程：</font>**  
&emsp; 升级重量级锁--->向操作系统申请资源，linux mutex，CPU从3级-0级系统调用，线程挂起，进入等待队列，等待操作系统的调度，然后再映射回用户空间。  

### 1.4.4. 锁状态总结  
&emsp; JDK 1.6 引入了偏向锁和轻量级锁，从而让锁拥有了四个状态： **无锁状态(unlocked)(普通对象，MarkWord标志位01，没有线程执行同步方法/代码块时的状态)** 、偏向锁状态(biasble)、轻量级锁状态(lightweight locked)和重量级锁状态(inflated)。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-66.png)  


|状态|标志位|描述|优点|缺点|应用场景|
|---|---|---|---|---|---|
|偏向锁	|010|<font color = "red">无实际竞争，让一个线程一直持有锁，在其他线程需要竞争锁(只cas一次)的时候，再释放锁</font>|加锁解锁不需要额外消耗|如果线程间存在竞争，会有撤销锁的消耗|只有一个线程进入临界区|
|轻量级|00|<font color = "red">无实际竞争，多个线程交替使用锁；允许短时间的锁竞争</font>|竞争的线程不会阻塞|如果线程一直得不到锁，会一直自旋，消耗CPU|多个线程交替进入临界区|
|重量级	|10|<font color = "clime">有实际竞争，且锁竞争时间长</font>|线程竞争不使用自旋，不消耗CPU|线程阻塞，响应时间长|多个线程同时进入临界区| 
