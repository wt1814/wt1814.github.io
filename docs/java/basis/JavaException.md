
<!-- TOC -->

- [1. Java异常](#1-java异常)
    - [1.1. Java异常基本概念](#11-java异常基本概念)
    - [1.2. 常见异常](#12-常见异常)
        - [1.2.1. NullPointerException](#121-nullpointerexception)
    - [1.3. 异常处理](#13-异常处理)
        - [1.3.1. try、catch、finally](#131-trycatchfinally)
            - [1.3.1.1. 使用 try...with...resources 优雅关闭资源](#1311-使用-trywithresources-优雅关闭资源)
        - [1.3.2. throws和throw](#132-throws和throw)
        - [1.3.3. 异常处理原则](#133-异常处理原则)
    - [1.4. Exception的API](#14-exception的api)
    - [1.5. 自定义异常](#15-自定义异常)
    - [1.6. 统一异常处理](#16-统一异常处理)

<!-- /TOC -->


<!-- 
异常处理、请求失败处理
https://www.hangge.com/blog/cache/detail_2519.html

-->

# 1. Java异常  
## 1.1. Java异常基本概念  
&emsp; Throwable是所有异常的超类，下一级可以分为Error和 Exception。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/exception/exception-1.png)  
* Error：Java虚拟机无法解决的严重问题。如：JVM系统内部错误、资源耗尽等严重情况。比如：StackOverflowError和OOM。一般不编写针对性的代码进行处理。
* Exception：其它因编程错误或偶然的外在因素导致的一般性问题，可以使用针对性的代码进行处理。
异常按照发生的时间可以分为两类：编译时异常和运行时异常。  
    * 编译时异常（可查异常）：执行java.exe命名时，出现的异常 是指编译器要求必须处置的异常。即程序在运行时由于外界因素造成的一般性异常。编译器要求Java程序==必须捕获或声明所有编译时异常== 对于这类异常，如果程序不处理，可能会带来意想不到的结果。  

        |异常名称|异常描述|
        |---|---|
        |NoSuchFieldException	|表示该类没有指定名称抛出来的异常|
        |NoSuchMethodException	|表示该类没有指定方法抛出来的异常|
        |IllegalAccessException	|不允许访问某个类的异常|
        |ClassNotFoundException	|类没有找到抛出异常|

    * 运行时异常（不可查异常）：执行javac.exe命名时，可能出现的异常。是指编译器不要求强制处置的异常。一般是指编程时的逻辑错误，是程序员应该积极避免其出现的异常。java. lang. Runtime Exception类及它的子类都是运行时异常。对于这类异常，可以不作处理，因为这类异常很普遍，若全处理可能会对程序的可读性和运行效率产生影响。  

        |异常名称|异常描述|
        |---|---|
        |ArrayIndexOutOfBoundsException	|数组越界异常|
        |NullPointerException	|空指针异常|
        |IllegalArgumentException	|非法参数异常|
        |NegativeArraySizeException	|数组长度为负异常|
        |IllegalStateException	|非法状态异常|
        |ClassCastException	|类型转换异常|

## 1.2. 常见异常  
### 1.2.1. NullPointerException  
&emsp; 发生空指针异常NullPointerException的情况（对null 进行操作）：  
1. 调用null对象的实例方法。  
2. 访问或修改null对象的字段。  
3. 如果一个数组为null，试图用属性length获得其长度时。  
4. 如果一个数组为null，试图访问或修改其中某个元素时。  
5. 在需要抛出一个异常对象，而该对象为null时。  


## 1.3. 异常处理  
### 1.3.1. try、catch、finally  

#### 1.3.1.1. 使用 try...with...resources 优雅关闭资源  


### 1.3.2. throws和throw  
&emsp; Throw和throws的区别：  
&emsp; 位置不同：  

        1. throws 用在函数上，后面跟的是异常类，可以跟多个；而 throw 用在函数内，后面跟的是异常对象。  

&emsp; 功能不同：  

        2. throws 用来声明异常，让调用者只知道该功能可能出现的问题，可以给出预先的处理方式；throw 抛出具体的问题对象，执行到 throw，功能就已经结束了，跳转到调用者，并将具体的问题对象抛给调用者。也就是说 throw 语句独立存在时，下面不要定义其他语句，因为执行不到。  
        3. throws 表示出现异常的一种可能性，并不一定会发生这些异常；throw 则是抛出了异常，执行 throw 则一定抛出了某种异常对象。  
        4. 两者都是消极处理异常的方式，只是抛出或者可能抛出异常，但是不会由函数去处理异常，真正的处理异常由函数的上层调用处理。  

### 1.3.3. 异常处理原则  
......

## 1.4. Exception的API  

```
Exception e; 
e.toString();       //获取的信息包括异常类型和异常详细消息
e.getMessage();     //只是获取了异常的详细消息字符串。
e.printStackTrace();//void类型，在命令行打印异常信息在程序中出错的位置及原因，可以输出整个调用流程。便于调试用。
```
&emsp; **<font color = "red">e.printStackTrace();只在控制台打印信息，不会将异常堆栈输出到日志文件中。</font>**  

## 1.5. 自定义异常 
......

## 1.6. 统一异常处理  
&emsp; 异常处理器注解@ExceptionHandler：若在某个Controller类定义一个异常处理方法，并在方法上添加该注解，那么当出现指定的异常时，会执行该处理异常的方法，其可以使用springmvc提供的数据绑定，比如注入HttpServletRequest等，还可以接受一个当前抛出的Throwable对象。  
&emsp; @ExceptionHandler结合@ControllerAdvice，可以提供全局的统一异常处理器。  
&emsp; 实现HandlerExceptionResolver接口或继承其抽象实现 AbstractHandlerExceptionResolver，也可以实现统一异常处理。  




