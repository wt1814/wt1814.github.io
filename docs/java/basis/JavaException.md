

<!-- TOC -->

- [1. Java异常](#1-java异常)
    - [1.1. ***三种处理异常的套路](#11-三种处理异常的套路)
    - [1.2. ***三方异常包](#12-三方异常包)
    - [1.3. Java异常基本概念](#13-java异常基本概念)
    - [1.4. 常见异常](#14-常见异常)
        - [1.4.1. NullPointerException](#141-nullpointerexception)
        - [1.4.2. ClassNotFoundException和NoClassDefFoundError](#142-classnotfoundexception和noclassdeffounderror)
    - [1.5. Exception的API](#15-exception的api)
    - [1.6. 异常使用教程](#16-异常使用教程)
        - [1.6.1. 异常处理](#161-异常处理)
            - [1.6.1.1. try、catch、finally](#1611-trycatchfinally)
                - [1.6.1.1.1. 使用try...with...resources优雅关闭资源](#16111-使用trywithresources优雅关闭资源)
            - [1.6.1.2. ~~throws和throw~~](#1612-throws和throw)
            - [1.6.1.3. 异常处理原则](#1613-异常处理原则)
        - [1.6.2. 异常捕获还是抛出](#162-异常捕获还是抛出)
        - [1.6.3. ★★★异常捕获后再次抛出](#163-★★★异常捕获后再次抛出)
        - [1.6.4. 自定义异常](#164-自定义异常)
        - [1.6.5. 统一异常处理](#165-统一异常处理)
        - [1.6.6. ***Assert处理异常](#166-assert处理异常)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. throws和throw：throws用在函数上，后面跟的是异常类，可以跟多个；`而throw用在函数内，后面跟的是异常对象。`  
2. 异常捕获后再次抛出。
    * 捕获后抛出原来的异常，希望保留最新的异常抛出点。
    * 捕获后抛出新的异常，希望抛出完整的异常链。  
3. 自定义异常
4. 统一异常处理


# 1. Java异常  
<!-- 
Assert处理异常
统一异常处理介绍及实战
https://www.jianshu.com/p/3f3d9e8d1efa
java断言assert初步使用：断言开启、断言使用
https://www.cnblogs.com/qiumingcheng/p/9506201.html
异常处理、请求失败处理
https://www.hangge.com/blog/cache/detail_2519.html
-->

## 1.1. ***三种处理异常的套路
<!--

 国外大佬给出的三种处理异常的套路！ 
 https://mp.weixin.qq.com/s/fQA43DRWXFiGx2SsKcovfQ

-->

## 1.2. ***三方异常包
获取异常堆栈消息：
import org.apache.commons.lang3.exception.ExceptionUtils;
ExceptionUtils.getStackTrace(e)


自定义异常，google的一个包
import com.google.protobuf.ServiceException;
throw new ServiceException(e.getMessage(), e);


## 1.3. Java异常基本概念  
&emsp; Throwable是所有异常的超类，下一级可以分为Error和 Exception。  
![image](http://182.92.69.8:8081/img/java/exception/exception-1.png)  
* Error：Java虚拟机无法解决的严重问题。如：JVM系统内部错误、资源耗尽等严重情况。比如：StackOverflowError和OOM。一般不编写针对性的代码进行处理。
* Exception：其它因编程错误或偶然的外在因素导致的一般性问题，可以使用针对性的代码进行处理。
异常按照发生的时间可以分为两类：编译时异常和运行时异常。  
    * 编译时异常(可查异常)：执行java.exe命名时，出现的异常是指编译器要求必须处置的异常。即程序在运行时由于外界因素造成的一般性异常。编译器要求Java程序  ==必须捕获或声明所有编译时异常== 对于这类异常，如果程序不处理，可能会带来意想不到的结果。  

        |异常名称|异常描述|
        |---|---|
        |NoSuchFieldException	|表示该类没有指定名称抛出来的异常|
        |NoSuchMethodException	|表示该类没有指定方法抛出来的异常|
        |IllegalAccessException	|不允许访问某个类的异常|
        |ClassNotFoundException	|类没有找到抛出异常|

    * 运行时异常(不可查异常)：执行javac.exe命名时，可能出现的异常。是指编译器不要求强制处置的异常。一般是指编程时的逻辑错误，是程序员应该积极避免其出现的异常。java. lang. Runtime Exception类及它的子类都是运行时异常。对于这类异常，可以不作处理，因为这类异常很普遍，若全处理可能会对程序的可读性和运行效率产生影响。  

        |异常名称|异常描述|
        |---|---|
        |ArrayIndexOutOfBoundsException	|数组越界异常|
        |NullPointerException	|空指针异常|
        |IllegalArgumentException	|非法参数异常|
        |NegativeArraySizeException	|数组长度为负异常|
        |IllegalStateException	|非法状态异常|
        |ClassCastException	|类型转换异常|

## 1.4. 常见异常  
### 1.4.1. NullPointerException  
&emsp; 发生空指针异常NullPointerException的情况(对null进行操作)：  
1. 调用null对象的实例方法。  
2. 访问或修改null对象的字段。  
3. 如果一个数组为null，试图用属性length获得其长度时。  
4. 如果一个数组为null，试图访问或修改其中某个元素时。  
5. 在需要抛出一个异常对象，而该对象为null时。  

### 1.4.2. ClassNotFoundException和NoClassDefFoundError  
<!-- 

ClassNotFoundException和NoClassDefFoundError
NoClassDefFoundError异常
https://blog.csdn.net/wdw131409/article/details/78316919
https://blog.csdn.net/qq_33543634/article/details/81128096
一道非常经典的面试题，NoClassDefFoundError 和 ClassNotFoundException 有什么区别？
https://mp.weixin.qq.com/s/VreiPFOeDuQvJJD4eeCWBw
基础考察：ClassNotFoundException 和 NoClassDefFoundError 有什么区别
https://mp.weixin.qq.com/s/Fo4l1JfDc5eS95ckaxJ-dA
 
java.lang.NoClassDefFoundError
https://blog.csdn.net/qq_33543634/article/d
etails/81128096
https://blog.csdn.net/Cheng120636/article/details/80546160
https://blog.csdn.net/qq_27576335/article/details/77102385
https://www.jianshu.com/p/ee3efc3a9e61?utm_source=oschina-app
https://www.cnblogs.com/xyhz0310/p/6803950.html
-->



## 1.5. Exception的API  

```java
Exception e; 
e.toString();       //获取的信息包括异常类型和异常详细消息
e.getMessage();     //只是获取了异常的详细消息字符串。
e.printStackTrace();//void类型，在命令行打印异常信息在程序中出错的位置及原因，可以输出整个调用流程。便于调试用。
```
&emsp; **<font color = "red">e.printStackTrace();只在控制台打印信息，不会将异常堆栈输出到日志文件中。</font>**  

## 1.6. 异常使用教程  
### 1.6.1. 异常处理  
#### 1.6.1.1. try、catch、finally  

##### 1.6.1.1.1. 使用try...with...resources优雅关闭资源  


#### 1.6.1.2. ~~throws和throw~~  
&emsp; Throw和throws的区别：  
&emsp; **位置不同：**  
1. throws用在函数上，后面跟的是异常类，可以跟多个；而throw用在函数内，后面跟的是异常对象。  

&emsp; **功能不同：**  
1. throws用来声明异常，让调用者只知道该功能可能出现的问题，可以给出预先的处理方式；throw抛出具体的问题对象，执行到throw，功能就已经结束了，跳转到调用者，并将具体的问题对象抛给调用者。也就是说throw语句独立存在时，下面不要定义其他语句，因为执行不到。  
2. throws表示出现异常的一种可能性，并不一定会发生这些异常；throw则是抛出了异常，执行throw则一定抛出了某种异常对象。  
3. 两者都是消极处理异常的方式，只是抛出或者可能抛出异常，但是不会由函数去处理异常，真正的处理异常由函数的上层调用处理。  

#### 1.6.1.3. 异常处理原则  
&emsp; ......


### 1.6.2. 异常捕获还是抛出
<!-- 
Java异常中，抛出异常和捕获异常的区别有哪些呀？实际工作中是抛出好还是捕获好呢？
http://www.shouhuola.com/q-34247.html
-->


### 1.6.3. ★★★异常捕获后再次抛出
<!-- 
https://www.cnblogs.com/yangyunnb/p/6058411.html
-->

* 情况一：捕获后抛出原来的异常，希望保留最新的异常抛出点－－fillStackTrace  
* 情况二：捕获后抛出新的异常，希望抛出完整的异常链－－initCause  


### 1.6.4. 自定义异常 
&emsp; ......

### 1.6.5. 统一异常处理  
<!-- 

SpringBoot优雅的全局异常处理 
https://mp.weixin.qq.com/s/r_HjHi92owNwh5VULiaKcQ
-->

&emsp; 异常处理器注解@ExceptionHandler：若在某个Controller类定义一个异常处理方法，并在方法上添加该注解，那么当出现指定的异常时，会执行该处理异常的方法，其可以使用springmvc提供的数据绑定，比如注入HttpServletRequest等，还可以接受一个当前抛出的Throwable对象。  
&emsp; @ExceptionHandler结合@ControllerAdvice，可以提供全局的统一异常处理器。  
&emsp; 实现HandlerExceptionResolver接口或继承其抽象实现AbstractHandlerExceptionResolver，也可以实现统一异常处理。  

### 1.6.6. ***Assert处理异常  
<!-- 

https://mp.weixin.qq.com/s/nQtkqN9IwZek6LX1Bvgf6A
-->


