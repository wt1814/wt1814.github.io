
<!-- TOC -->

- [1. 中断线程](#1-中断线程)
    - [1.1. stop与interrupt](#11-stop与interrupt)
    - [1.2. Java中对线程中断所提供的API支持](#12-java中对线程中断所提供的api支持)
    - [1.3. ★★★线程在不同状态下对于中断所产生的反应](#13-★★★线程在不同状态下对于中断所产生的反应)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 中断Thread.interrupt()  
&emsp; **<font color = "red">线程在不同状态下对于中断所产生的反应：</font>**    
    * NEW和TERMINATED对于中断操作几乎是屏蔽的；  
    * RUNNABLE和BLOCKED类似， **<font color = "cclime">对于中断操作只是设置中断标志位并没有强制终止线程，对于线程的终止权利依然在程序手中；</font>**  
    * WAITING/TIMED_WAITING状态下的线程对于中断操作是敏感的，它们会抛出异常并清空中断标志位。  


# 1. 中断线程  
<!-- 
*** 线程中断机制及响应
https://www.jianshu.com/p/28bf4345b125

https://blog.csdn.net/w1014074794/article/details/51111376?utm_medium=distribute.pc_relevant.none-task-blog-baidujs_title-0&spm=1001.2101.3001.4242

https://www.cnblogs.com/yingying7/p/12434382.html

线程中断详解
https://blog.csdn.net/xinxiaoyong100440105/article/details/80931705
https://www.cnblogs.com/yangming1996/p/7612653.html

线程不是你想中断就能中断 
https://mp.weixin.qq.com/s?__biz=Mzg2ODU1MDkwMw==&mid=2247485086&idx=1&sn=e9bb817a7d13d2b33d8b167c21a33354&chksm=ceabd706f9dc5e102013948ddd644abd57beb94054bbeeb1af47ba363953bf9d8d8bdf28d20e&scene=178&cur_album_id=1681635175341817859#rd

任务和线程的启动很容易。在大多数时候，我们都会让它们运行直到结束，或者让它们自 行停止。然而，有时候希望提前结束任务或线程，或许是因为用户取消了操作，或者应用 程序需要被快速关闭。
要使任务和线程能安全、快速、可靠地停止下来，并不是一件容易的事。Java没有提供任 何机制来安全地终止线程e。但它提供了中断(Interruption),这是一种协作机制，能够使一个 线程终止另一个线程的当前工作。

一些特殊的阻塞库的方法支持中断。线程中断是一种协作机制，线程可以 通过这种机制来通知另一个线程，告诉它在合适的或者可能的情况下停止当前工作，并转而执 行其他的工作。
在Java的API或译言规.范中，并没有将中#与任何取消语义关联起来，但实际
上，如果在取消之外的其他操作中使用中断，郡么都是不合适的，并且很难支撑起更大的应用。

对中断操作的正确理解是：它并不会真正地中断一个正在运行的线程，而只是发出中断请 求，然后由线程在下一个合适的时刻中断自己。(这些时刻也被称为取消点)。有些方法，例女口 wait、sleep和join等，将严格地处理这种请求，当它们收到中断请求或者在开始执行时发现某 个已被设置好的中断状态时，将抛出一个异常。

通常，中是实现取消的最合理方式

有时想让主线程启动的一个子线程结束运行，我们就需要让这个子线程中断，不再继续执行。线程是有中断机制的，我们可以对每个线程进行中断标记，注意只是标记，中断与否还是虚拟机自己的事情，虚拟机自己家的事情，我们也就说说，不能实际操作控制他家。java中的Thread类是一个对线程进行操作的类，提供了中断线程的方法interrupt()，在API中是这么定义的(中文的翻译可能不准确)。
-->
## 1.1. stop与interrupt

&emsp; stop()方法，这个方法已经标记为过时了，强制停止线程，相当于kill -9。

&emsp; interrupt()方法，优雅的停止线程。告诉线程可以停止了，至于线程什么时候停止，取决于线程自身。  
&emsp; 线程通过检查自身是否被中断来进行相应，可以通过isInterrupted()来判断是否被中断。  
&emsp; 这种通过标识符来实现中断操作的方式能够使线程在终止时有机会去清理资源，而不是武断地将线程停止，因此这种终止线程的做法显得更加安全和优雅。

    Java中interrupted和isInterruptedd方法的区别？
    interrupted()和isInterrupted()的主要区别是前者会将中断状态清除而后者不会。Java多线程的中断机制是用内部标识来实现的，调用Thread.interrupt()来中断一个线程就会设置中断标识为true。当中断线程调用静态方法Thread.interrupted()来检查中断状态时，中断状态会被清零。而非静态方法isInterrupted()用来查询其它线程的中断状态且不会改变中断状态标识。简单的说就是任何抛出InterruptedException异常的方法都会将中断状态清零。无论如何，一个线程的中断状态有有可能被其它线程调用中断来改变。


&emsp; 在程序中经常会有一些不达到目的不会退出的线程，例如：有一个下载程序线程，该线程在没有下载成功之前是不会退出的，若此时用户觉得下载速度慢，不想下载了，这时就需要用到线程中断机制了，告诉线程，不要继续执行了，准备好退出吧。当然，线程在不同的状态下遇到中断会产生不同的响应，有点会抛出异常，有的则没有变化，有的则会结束线程。

## 1.2. Java中对线程中断所提供的API支持
&emsp; 在以前的jdk版本中，使用stop方法中断线程，但是现在的jdk版本中已经不再推荐使用该方法了，反而由以下三个方法完成对线程中断的支持。

```java
public boolean isInterrupted()
public void interrupt()
public static boolean interrupted() 
```
&emsp; 每个线程都一个状态位用于标识当前线程对象是否是中断状态。isInterrupted是一个实例方法，主要用于判断当前线程对象的中断标志位是否被标记了，如果被标记了则返回true表示当前已经被中断，否则返回false。  
&emsp; interrupt是一个实例方法，该方法用于设置当前线程对象的中断标识位。  
&emsp; interrupted是一个静态的方法，用于返回当前线程是否被中断。

## 1.3. ★★★线程在不同状态下对于中断所产生的反应
&emsp; 线程在不同状态下对于中断所产生的反应：  
&emsp; NEW和TERMINATED对于中断操作几乎是屏蔽的；  
&emsp; RUNNABLE和BLOCKED类似， **<font color = "cclime">对于中断操作只是设置中断标志位并没有强制终止线程，对于线程的终止权利依然在程序手中；</font>**  
&emsp; WAITING/TIMED_WAITING状态下的线程对于中断操作是敏感的，它们会抛出中断异常InterruptedException并清空中断标志位。
