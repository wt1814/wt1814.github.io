---
title: JDK1.8
date: 2020-01-27 00:00:00
tags:
    - JDK
---

<!-- TOC -->

- [1. 接口的默认方法与静态方法](#1-接口的默认方法与静态方法)
    - [1.1. 接口中默认方法](#11-接口中默认方法)
    - [1.2. 接口中静态方法](#12-接口中静态方法)
- [2. Lambda表达式](#2-lambda表达式)
- [3. StreamAPI](#3-streamapi)
- [4. Optional类](#4-optional类)
- [5. Date/Time API](#5-datetime-api)
- [6. 异常捕获的改变](#6-异常捕获的改变)
- [7. Base64](#7-base64)

<!-- /TOC -->


# 1. 接口的默认方法与静态方法  
&emsp; 在接口中新增了default方法和static方法，这两种方法可以有方法体。  

## 1.1. 接口中默认方法  
&emsp; JDK1.8引入了接口默认方法，其目的是为了解决接口的修改与已有的实现不兼容的问题，接口默认方法可以作为库、框架向前兼容的一种手段。  
&emsp; 在接口中定义默认方法，使用default关键字，并提供默认的实现。  

```
public interface DefaultFunctionInterface {
    default String defaultFunction() {
        return "default function";
    }
}
```
&emsp; default方法被子接口继承，也可以被其实现类所调用。default方法被继承时，可以被子接口覆写。  
&emsp; 如果一个类实现了多个接口，且这些接口中无继承关系，这些接口中若有相同的（同名，同参数）的default方法，则接口实现类会报错。接口实现类必须通过特殊语法指定该实现类要实现哪个接口的default方法，\<接口\>.super.\<方法名\>([参数])。  

## 1.2. 接口中静态方法  
&emsp; 在接口中定义静态方法。  

```
public interface StaticFunctionInterface {
    static String staticFunction() {
        return "static function";
    }
}
```
&emsp; 接口中的static方法不能被继承，也不能被实现类调用，只能被自身调用。但是静态变量会被继承。  

# 2. Lambda表达式  
[Lambda](java/JDK8/Lambda.md)  

------

# 3. StreamAPI  
[Stream](java/JDK8/Stream.md)  

-----
# 4. Optional类  
[Optional](java/JDK8/Optional.md)  

-----
# 5. Date/Time API  
[DateTime](java/JDK8/DateTime.md)  

-----
# 6. 异常捕获的改变   
&emsp; 新的try…cache可以自动关闭在try表达式中打开的对象，而无需开发者手动关闭。  
&emsp; 多个流对象打开语句，用分号分隔，不是逗号。  

```
try(ObjectInputStream in=new ObjectInputStream(new FileInputStream("p1.obj"))){
    System.out.println(Person.class.hashCode());
    Person person=(Person)in.readObject();
    System.out.println(person.staticString);
} catch (Exception e) {
    e.printStackTrace();
}
```
&emsp; 不再需要：  

```
finally{
    in.close();
}
```

# 7. Base64  
&emsp; Java8内置了Base64编码的编码器和解码器。Base64类同时还提供了对URL、MIME友好的编码器与解码器。  
* 基本：输出被映射到一组字符A-Za-z0-9+/，编码不添加任何行标，输出的解码仅支持A-Za-z0-9+/。  
* URL：输出映射到一组字符A-Za-z0-9+_，输出是URL和文件。  
* MIME：输出隐射到MIME友好格式。输出每行不超过76字符，并且使用'\r'并跟随'\n'作为分割。编码输出最后没有行分割。  

&emsp; 内嵌类：  

    static class Base64.Decoder	该类实现一个解码器用于，使用Base64编码来解码字节数据。
    static class Base64.Encoder	该类实现一个编码器，使用Base64编码来编码字节数据。 

|方法 |描述|
|---|---|
|Decoder| |
|getDecoder()	|返回Base64.Decoder，解码使用基本型base64编码方案|
|getMimeDecoder()	|返回Base64.Decoder，解码使用MIME型base64 编码方案。|
|getUrlDecoder()	|返回Base64.Decoder，解码使用URL和文件名安全型 base64编码方案。|
|Encoder| | 
|getEncoder()	|返回Base64.Encoder，编码使用基本型base64编码方案。|
|getMimeEncoder()	|返回Base64.Encoder，编码使用MIME型base64编码方案。|
|getMimeEncoder(int lineLength, byte[] lineSeparator)	|返回Base64.Encoder，编码使用MIME型base64编码方案，可以通过参数指定每行的长度及行的分隔符。|
|getUrlEncoder()	|返回Base64.Encoder，编码使用URL和文件名安全型 base64 编码方案。|

```
// 使用基本编码
String base64encodedString = Base64.getEncoder().encodeToString("runoob?java8".getBytes("utf-8"));
System.out.println("Base64 编码字符串 (基本) :" + base64encodedString);
// 解码
byte[] base64decodedBytes = Base64.getDecoder().decode(base64encodedString);
System.out.println("原始字符串: " + new String(base64decodedBytes, "utf-8"));
```

