
<!-- TOC -->

- [1. 内存泄漏、内存溢出](#1-内存泄漏内存溢出)
    - [1.1. 简介](#11-简介)
    - [1.2. 内存溢出演示](#12-内存溢出演示)
    - [1.3. 内存泄露场景](#13-内存泄露场景)
    - [1.4. 内存溢出问题详解](#14-内存溢出问题详解)

<!-- /TOC -->

# 1. 内存泄漏、内存溢出  
<!-- 

 JAVA内存泄漏和内存溢出的区别和联系 
 https://mp.weixin.qq.com/s/QwY0PAHO_oyELPlxRuHbOQ
-->

## 1.1. 简介
&emsp; **<font color = "red">内存溢出out of memory</font>** ，是指<font color = "red">程序在申请内存时，没有足够的内存空间供其使用</font>，出现out of memory；  
&emsp; **<font color = "red">内存泄露 memory leak</font>** ，是指<font color = "red">程序在申请内存后，无法释放已申请的内存空间</font>。一次内存泄露危害可以忽略，但内存泄露堆积后果很严重，无论多少内存，迟早会被占光。内存泄露，会导致频繁的Full GC。  
&emsp; 所以内存泄漏可能会导致内存溢出，但内存溢出并不完全都是因为内存泄漏，也有可能使用了太多的大对象导致。  

&emsp; **<font color = "red">JVM堆内存溢出后，其他线程是否可继续工作？</font>**  
&emsp; 当一个线程抛出OOM异常后，它所占据的内存资源会全部被释放掉，从而不会影响其他线程的运行！  
&emsp; 其实发生OOM的线程一般情况下会死亡，也就是会被终结掉，该线程持有的对象占用的heap都会被gc了，释放内存。因为发生OOM之前要进行gc，就算其他线程能够正常工作，也会因为频繁gc产生较大的影响。  

## 1.2. 内存溢出演示  
<!--
～～
（内存溢出演示）几种典型的内存溢出案例，都在这儿了！
https://mp.weixin.qq.com/s/4SenzIeX9FqsnXAaV6IgLg
 教你写Bug，常见的 OOM 异常分析 
https://mp.weixin.qq.com/s/gIJvtd8rrZz6ttaoGLddLg

面试官：哪些场景会产生OOM？怎么解决？ 
https://mp.weixin.qq.com/s/j8_6QW_WLqlZDUjbDUbyZw

https://mp.weixin.qq.com/s/XJhtBYGMJps4B5wzNTsSVA
-->

## 1.3. 内存泄露场景  
<!-- 
～～
java内存泄漏与内存溢出
https://www.cnblogs.com/panxuejun/p/5883044.html
-->


## 1.4. 内存溢出问题详解  

<!-- 
https://mp.weixin.qq.com/s/Cz3fXRRT1B8iAd36fNTdPA
-->
