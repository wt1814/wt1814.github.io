---
title: Java异常
date: 2020-01-28 00:00:00
tags:
    - Java
---


# Java异常  
## Java异常的概念、分类  
&emsp; Throwable是所有异常的超类，下一级可以分为Error和 Exception。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/exception/exception-1.png)  
* Error：Java虚拟机无法解决的严重问题。如：JVM系统内部错误、资源耗尽等严重情况。比如：StackOverflowError和OOM。一般不编写针对性的代码进行处理。
* Exception：其它因编程错误或偶然的外在因素导致的一般性问题，可以使用针对性的代码进行处理。
异常按照发生的时间可以分为两类：编译时异常和运行时异常。  
    * 编译时异常（可查异常）：执行java.exe命名时，出现的异常 是指编译器要求必须处置的异常。即程序在运行时由于外界因素造成的一般性异常。编译器要求Java程序==必须捕获或声明所有编译时异常== 对于这类异常，如果程序不处理，可能会带来意想不到的结果。  

        |异常名称	|异常描述|
        |---|---|
        |NoSuchFieldException	|表示该类没有指定名称抛出来的异常|
        |NoSuchMethodException	|表示该类没有指定方法抛出来的异常|
        |IllegalAccessException	|不允许访问某个类的异常|
        |ClassNotFoundException	|类没有找到抛出异常|

    * 运行时异常（不可查异常）：执行javac.exe命名时，可能出现的异常。是指编译器不要求强制处置的异常。一般是指编程时的逻辑错误，是程序员应该积极避免其出现的异常。java. lang. Runtime Exception类及它的子类都是运行时异常。对于这类异常，可以不作处理，因为这类异常很普遍，若全处理可能会对程序的可读性和运行效率产生影响。  

        |异常名称	|异常描述|
        |---|---|
        |ArrayIndexOutOfBoundsException	|数组越界异常|
        |NullPointerException	|空指针异常|
        |IllegalArgumentException	|非法参数异常|
        |NegativeArraySizeException	|数组长度为负异常|
        |IllegalStateException	|非法状态异常|
        |ClassCastException	|类型转换异常|

### 常见异常  
#### NullPointerException  
&emsp; 发生空指针异常NullPointerException的情况（对null 进行操作）：  
1. 调用null对象的实例方法。  
2. 访问或修改null对象的字段。  
3. 如果一个数组为null，试图用属性length获得其长度时。  
4. 如果一个数组为null，试图访问或修改其中某个元素时。  
5. 在需要抛出一个异常对象，而该对象为null时。  










