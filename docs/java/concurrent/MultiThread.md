

<!-- TOC -->

- [1. 并发编程](#1-并发编程)
    - [1.1. 多线程带来的风险](#11-多线程带来的风险)
    - [1.2. 并发编程原理](#12-并发编程原理)
    - [1.3. 线程同步](#13-线程同步)
        - [1.3.1. 线程安全](#131-线程安全)
        - [1.3.2. Synchronized](#132-synchronized)
        - [1.3.3. Volatile](#133-volatile)
        - [1.3.4. ThreadLocal](#134-threadlocal)
    - [1.4. 线程通信](#14-线程通信)
    - [1.5. 线程活跃性](#15-线程活跃性)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-27.png)  

# 1. 并发编程  
## 1.1. 多线程带来的风险  
&emsp; **<font color = "red">多线程带来的风险：</font>**  
1. 安全性问题  
&emsp; 线程安全性可能是非常复杂的，在没有充足同步的情况下，多个线程中的操作执行顺序是不可预测的，甚至会产生奇怪的结果。  
2. 活跃性问题  
&emsp; 安全性的含义是“永远不发生糟糕的事情”，而活跃性则关注于另一个目标，即“某件正确的事情最终会发生”。当某个操作充法继续执行下去时，就会发生活跃性问题。在串行程序中，活跃性问题的形式之一就是无意中造成的无限循环，从而使循环之后的代码无法得到执行。线程将带来其他一些活跃性问题。  
3. 性能问题  
&emsp; 与活跃性问题密切相关的是性能问题。活跃性意味着某件正确的事情最终会发生，但却不够好，因为通常希望正确的事情尽快发生。性能问题包括多个方面，例如服务时间过长，响应不灵敏，吞吐率过低，资源消耗过高，或者可伸缩性较低等。与安全性和活跃性一样，在多线程程序中不仅存在与单线程程序相同的性能问题，而且还存在由于使用线程而引入的其他性能问题。  
&emsp; 在设计良好的并发应用程序中，线程能提升程序的性能，但无论如何，线程总会带来某种程度的运行时开销。在多线程程序中，当线程调度器临时挂起活跃线程并转而运行另一个线程时，就会频繁地出现上下文切换操作(Context Switch)，这种操作将带来极大的开销：保存和恢复执行上下文，丢失局部性，并且CPU时间将更多地花在线程调度而不是线程运行上。当线程共享数据时，必须使用同步机制，而这些机制往往会抑制某些编译器优化，使内存缓存区中的数据无效，以及增加共享内存总线的同步流量。所有这些因素都将带来额外的性能开销。 


## 1.2. 并发编程原理  
&emsp; [CPU缓存及JMM](/docs/java/concurrent/JMM.md)  

&emsp; <font color = "clime">JMM中定义了线程的两种行为：线程通信、线程同步。</font><font color = "lime">线程通信是一种手段，而线程同步是一种目的，即线程通信的主要目的是用于线程同步。线程同步是为了解决线程安全问题。</font>  

&emsp; [并发安全问题产生原因](/docs/java/concurrent/ConcurrencyProblem.md)  
&emsp; [并发安全解决底层](/docs/java/concurrent/ConcurrencySolve.md)  
&emsp; [伪共享问题](/docs/java/concurrent/PseudoSharing.md)   

## 1.3. 线程同步  
### 1.3.1. 线程安全  


&emsp; [线程安全解决方案](/docs/java/concurrent/ThreadSafety.md)
  
### 1.3.2. Synchronized  
&emsp; [Synchronized应用](/docs/java/concurrent/SynApply.md)  
&emsp; [Synchronized底层原理](/docs/java/concurrent/SynBottom.md)  
&emsp; [Synchronized优化](/docs/java/concurrent/SynOptimize.md) 

### 1.3.3. Volatile  
&emsp; [Volatile](/docs/java/concurrent/Volatile.md)  

### 1.3.4. ThreadLocal  
&emsp; [ThreadLocal](/docs/java/concurrent/ThreadLocal.md)  

## 1.4. 线程通信  
&emsp; [线程通信](/docs/java/concurrent/ThreadCommunication.md)   

## 1.5. 线程活跃性  
&emsp; [线程活跃性](/docs/java/concurrent/Activity.md)  
