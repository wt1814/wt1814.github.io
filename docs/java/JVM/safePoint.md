

<!-- TOC -->

- [1. 安全点](#1-安全点)
    - [1.1. Stop the Word](#11-stop-the-word)
    - [1.2. 什么是安全点](#12-什么是安全点)
    - [1.3. 安全点位置](#13-安全点位置)
    - [1.4. 安全点应用场景](#14-安全点应用场景)
    - [1.5. 如何在GC发生时，所有线程都跑到最近的SafePoint上再停下来？](#15-如何在gc发生时所有线程都跑到最近的safepoint上再停下来)
    - [1.6. 安全点工作机制](#16-安全点工作机制)
    - [1.7. 安全区域又是什么？](#17-安全区域又是什么)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 安全点  
&emsp; **<font color = "clime">可达性分析算法必须是在一个确保一致性的内存快照中进行。</font>**   
&emsp; **<font color = "clime">安全点意味着在这个点时，所有工作线程的状态是确定的，JVM可以安全地执行GC。</font>**  
2. 安全区域  
&emsp; `在安全点上中断的是活跃运行的用户线程，对于已经挂起的线程该怎么处理呢？`  
&emsp; **<font color = "blue">已经挂起的线程会被认定为处在安全区域内，中断的时候不需要考虑安全区域中的线程。</font>**  
&emsp; 当前安全区域的线程要被唤醒离开安全区域时，先检查能否离开，如果GC完成了，那么线程可以离开，否则它必须等待直到收到安全离开的信号为止。  

# 1. 安全点  
<!--
http://www.mamicode.com/info-detail-2913659.html
https://blog.csdn.net/baichoufei90/article/details/85097727
https://blog.csdn.net/qian_348840260/article/details/88819502
聊聊JVM（九）理解进入safepoint时如何让Java线程全部阻塞
https://blog.csdn.net/ITer_ZC/article/details/41892567?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-5.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-5.control
抢先式中断
https://mp.weixin.qq.com/s/3YHHtuPENiV_2ZXfHHuD4A

-->

## 1.1. Stop the Word

<!-- =
*** https://blog.csdn.net/u011918260/article/details/70047159?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-11.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-11.control

https://blog.csdn.net/u011918260/article/details/70047159?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-11.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-11.control
-->

&emsp; Java中Stop-The-World机制简称STW，是在执行垃圾收集时，Java应用程序的其他所有线程都被挂起(除了垃圾收集帮助器之外)。Java中一种全局暂停现象，全局停顿，所有Java代码停止，native代码可以执行，但不能与JVM交互；这些现象多半是由于gc引起。  
&emsp; GC时的Stop the World(STW)是Java开发最大的敌人。除了GC，JVM下还会发生停顿现象。  
&emsp; JVM里有一条特殊的线程－－VM Threads，专门用来执行一些特殊的VM Operation，比如分派GC，thread dump等，这些任务都需要整个Heap，以及所有线程的状态是静止的、一致的才能进行。所以JVM引入了安全点(Safe Point)的概念，想办法在需要进行VM Operation时，通知所有的线程进入一个静止的安全点。  

<!-- 
&emsp; 除了GC，其他触发安全点的VM Operation包括：  
1. JIT相关，比如Code deoptimization， Flushing code cache ；  
2. Class redefinition(e.g. javaagent，AOP代码植入的产生的instrumentation) ；  
3. Biased lock revocation 取消偏向锁 ；  
4. Various debug operation (e.g. thread dump or deadlock check)；  
-->


## 1.2. 什么是安全点  
&emsp; 在 JVM 中如何判断对象可以被回收 一文中，知道 HotSpot 虚拟机采取的是可达性分析算法。即通过 GC Roots 枚举判定待回收的对象。  

&emsp; 那么，首先要找到哪些是 GC Roots。有两种查找 GC Roots 的方法：  

* 一种是遍历方法区和栈区查找（保守式 GC）。  
* 一种是通过 OopMap 数据结构来记录 GC Roots 的位置（准确式 GC）。  

&emsp; 很明显，保守式 GC 的成本太高。准确式 GC 的优点就是能够让虚拟机快速定位到 GC Roots。对应 OopMap 的位置即可作为一个安全点（Safe Point）。  

&emsp; 在执行 GC 操作时，所有的工作线程必须停顿。  
&emsp; 为什么呢？因为 **<font color = "clime">可达性分析算法必须是在一个确保一致性的内存快照中进行。</font>** 如果在分析的过程中对象引用关系还在不断变化，分析结果的准确性就不能保证。  

&emsp; **<font color = "clime">安全点意味着在这个点时，所有工作线程的状态是确定的，JVM可以安全地执行GC。</font>**  

## 1.3. 安全点位置  
&emsp; 安全点太多，GC过于频繁，增大运行时负荷；安全点太少，GC 等待时间太长。  

&emsp; 一般会在如下几个位置选择安全点：  

* 循环的末尾
* 方法临返回前
* 调用方法之后
* 抛异常的位置

&emsp; 为什么选定这些位置作为安全点？  
&emsp; 主要的目的就是避免程序长时间无法进入 Safe Point。比如 JVM 在做 GC 之前要等所有的应用线程进入安全点，如果有一个线程一直没有进入安全点，就会导致 GC 时 JVM 停顿时间延长。比如这里，超大的循环导致执行 GC 等待时间过长。  

## 1.4. 安全点应用场景  
&emsp; GC STW、偏向锁释放.....

## 1.5. 如何在GC发生时，所有线程都跑到最近的SafePoint上再停下来？  
&emsp; 主要有两种方式：  

* 抢断式中断：在 GC 发生时，首先中断所有线程，如果发现线程未执行到 Safe Point，就恢复线程让其运行到 Safe Point 上。
* 主动式中断：在 GC 发生时，不直接操作线程中断，而是简单地设置一个标志，让各个线程执行时主动轮询这个标志，发现中断标志为真时就自己中断挂起。

&emsp; JVM 采取的就是主动式中断。轮询标志的地方和安全点是重合的。  

## 1.6. 安全点工作机制  
&emsp; 在HotSpot虚拟机中，安全点协议是主动协作的 。每一个用户线程在安全点上都会检测一个标志位，来决定自己是否暂停执行。  

* 对于JIT编译后的代码，JIT会在代码特定的位置(通常来说，在方法的返回处和counted loop的结束处)上插入安全点代码。
* 对于解释执行的代码，JVM会设置一个2字节的dispatch tables。解释器执行的时候会经常去检查这个dispatch tables，当有安全点请求的时候，就会让线程去进行安全点检查。

## 1.7. 安全区域又是什么？  
&emsp; ~~在安全点上中断的是活跃运行的用户线程，对于已经挂起的线程该怎么处理呢？已经挂起的线程会被认定为处在安全区域内，中断的时候不需要考虑安全区域中的线程；当前安全区域的线程要被唤醒离开安全区域时，需要校验一下主动式中断策略的标志位是否为真，如果为真则继续挂起等待，否则可被唤醒继续执行。~~  

&emsp; **Safe Point是对正在执行的线程设定的。如果一个线程处于 Sleep 或中断状态，它就不能响应 JVM 的中断请求，再运行到 Safe Point 上。因此 JVM 引入了 Safe Region。**  
&emsp; Safe Region 是指在一段代码片段中，引用关系不会发生变化。在这个区域内的任意地方开始 GC 都是安全的。  
&emsp; 线程在进入 Safe Region 的时候先标记自己已进入了 Safe Region，等到被唤醒时准备离开 Safe Region 时，先检查能否离开，如果 GC 完成了，那么线程可以离开，否则它必须等待直到收到安全离开的信号为止。  
