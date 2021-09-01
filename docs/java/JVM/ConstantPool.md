
<!-- TOC -->

- [1. 常量池](#1-常量池)
    - [1.1. 常量池](#11-常量池)
        - [1.1.1. 前言：字面量、常量、变量](#111-前言字面量常量变量)
        - [1.1.2. 常量池](#112-常量池)
    - [1.2. 包装类对象池技术](#12-包装类对象池技术)
    - [1.3. 小结](#13-小结)

<!-- /TOC -->

# 1. 常量池
<!--
终于搞懂了Java8的内存结构，再也不纠结方法区和常量池了！ 
https://mp.weixin.qq.com/s/WvdPQ8JsR9qqWMlvX7ockA
JAVA常量池，一篇文章就足够入门了。（含图解）
https://blog.csdn.net/qq_41376740/article/details/80338158
常量池 
https://baike.baidu.com/item/%E5%B8%B8%E9%87%8F%E6%B1%A0/3855836?fr=aladdin
Java中的几种常量池
https://blog.csdn.net/luzhensmart/article/details/86565496
常量池详解
https://zhuanlan.zhihu.com/p/64839455
JVM常量池浅析
https://www.jianshu.com/p/cf78e68e3a99
https://blog.csdn.net/qq_34039868/article/details/103957965
-->

## 1.1. 常量池  


### 1.1.1. 前言：字面量、常量、变量  
<!-- 
https://blog.csdn.net/luzhensmart/article/details/86565496
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-132.png)  

### 1.1.2. 常量池
<!-- 
已经明确的一点是 字符串常量池、静态变量 在jdk7时从方法区移入**Java堆**中，那么运行时常量池呢？

https://www.pianshen.com/article/72411279197/
-->

&emsp; **<font color = "clime">常量池分为以下三种：class文件常量池、运行时常量池、全局字符串常量池。</font>**   
1. class文件常量池(Class Constant Pool)  
&emsp; class常量池是在编译的时候每个class都有的，在编译阶段，存放的是常量(文本字符串、final常量等)和符号引用。  
2. 运行时常量池(Runtime Constant Pool)  
&emsp; 运行时常量池是在类加载完成之后， **<font color = "clime">将每个class常量池中的符号引用值转存到运行时常量池中</font>** ，也就是说，每个class都有一个运行时常量池，类在解析之后，将符号引用替换成直接引用，与全局常量池中的引用值保持一致。  
3. 全局字符串常量池(String Pool)  
&emsp; 全局字符串常量池中存放的内容是在类加载完成后存到String Pool中的，在每个VM中只有一份，存放的是字符串常量的引用值（在堆中生成字符串对象实例）。  

## 1.2. 包装类对象池技术
&emsp; 包装类的对象池（也有称常量池）和JVM的静态/运行时常量池没有任何关系。静态/运行时常量池有点类似于符号表的概念，与对象池相差甚远。  
&emsp; 包装类的对象池是池化技术的应用，并非是虚拟机层面的东西，而是 Java 在类封装里实现的。打开 Integer 的源代码，找到cache相关的内容：  
&emsp; 详情参考[Java基础数据类型](/docs/java/basis/BasicsDataType.md)  

## 1.3. 小结  

