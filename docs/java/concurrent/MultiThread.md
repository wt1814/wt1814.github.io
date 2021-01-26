

<!-- TOC -->

- [1. 多线程编程](#1-多线程编程)
    - [1.1. JMM](#11-jmm)
    - [1.2. 线程同步](#12-线程同步)
        - [1.2.1. 线程安全](#121-线程安全)
        - [1.2.2. Synchronized](#122-synchronized)
        - [1.2.3. Volatile](#123-volatile)
        - [1.2.4. ThreadLocal](#124-threadlocal)
    - [1.3. 线程通信](#13-线程通信)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-27.png)  

# 1. 多线程编程  
## 1.1. JMM  
&emsp; [JMM](/docs/java/concurrent/JMM.md)  

&emsp; <font color = "red">JMM中定义了线程的两种行为：线程通信、线程同步。</font><font color = "lime">线程通信是一种手段，而线程同步是一种目的，即线程通信的主要目的是用于线程同步。线程同步是为了解决线程安全问题。</font>  

## 1.2. 线程同步  

### 1.2.1. 线程安全  
&emsp; [线程安全](/docs/java/concurrent/ThreadSafety.md)
  
### 1.2.2. Synchronized  
&emsp; [Synchronized](/docs/java/concurrent/Synchronized.md)  

### 1.2.3. Volatile  
&emsp; [Volatile](/docs/java/concurrent/Volatile.md)  

### 1.2.4. ThreadLocal  
&emsp; [ThreadLocal](/docs/java/concurrent/ThreadLocal.md)  

## 1.3. 线程通信  
&emsp; [线程通信](/docs/java/concurrent/ThreadCommunication.md)   

