

<!-- TOC -->

- [1. JDK1.8](#1-jdk18)
    - [1.1. 接口的默认方法与静态方法](#11-接口的默认方法与静态方法)
        - [1.1.1. 接口中默认方法](#111-接口中默认方法)
        - [1.1.2. ~~接口中静态方法~~](#112-接口中静态方法)
    - [1.2. Lambda表达式](#12-lambda表达式)
    - [1.3. StreamAPI](#13-streamapi)
    - [1.4. Optional类](#14-optional类)
    - [1.5. Date/Time API](#15-datetime-api)
    - [1.6. 异常捕获的改变](#16-异常捕获的改变)
    - [1.7. Base64](#17-base64)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 接口的默认方法与静态方法  
    * <font color = "clime">接口中的default方法会被子接口继承，也可以被其实现类所调用。default方法被继承时，可以被子接口覆写。</font>  
    * <font color = "clime">接口中的static方法不能被继承，也不能被实现类调用，只能被自身调用。即不能通过接口实现类的方法调用静态方法，直接通过接口名称调用。但是静态变量会被继承。</font>  


# 1. JDK1.8
## 1.1. 接口的默认方法与静态方法  
&emsp; 在接口中新增了default方法和static方法，这两种方法可以有方法体。  

### 1.1.1. 接口中默认方法  
&emsp; JDK1.8引入了接口默认方法，其目的是为了解决接口的修改与已有的实现不兼容的问题，接口默认方法可以作为库、框架向前兼容的一种手段。  
&emsp; 在接口中定义默认方法，使用default关键字，并提供默认的实现。  

```java
public interface DefaultFunctionInterface {
    default String defaultFunction() {
        return "default function";
    }
}
```
&emsp; <font color = "clime">default方法被子接口继承，也可以被其实现类所调用。default方法被继承时，可以被子接口覆写。</font>  
&emsp; 如果一个类实现了多个接口，且这些接口中无继承关系，这些接口中若有相同的(同名，同参数)的default方法，则接口实现类会报错。接口实现类必须通过特殊语法指定该实现类要实现哪个接口的default方法，\<接口\>.super.\<方法名\>([参数])。  

### 1.1.2. ~~接口中静态方法~~
<!-- 
https://blog.csdn.net/tangshuai96/article/details/101264446
-->  
&emsp; 在接口中定义静态方法。  

```java
public interface StaticFunctionInterface {
    static String staticFunction() {
        return "static function";
    }
}
```
&emsp; <font color = "clime">接口中的static方法不能被继承，也不能被实现类调用，只能被自身调用。即不能通过接口实现类的方法调用静态方法，直接通过接口名称调用。但是静态变量会被继承。</font>  

## 1.2. Lambda表达式  
&emsp; [Lambda](/docs/java/JDK8/Lambda.md)  

## 1.3. StreamAPI  
&emsp; [Stream](/docs/java/JDK8/Stream.md)  

## 1.4. Optional类  
&emsp; [Optional](/docs/java/JDK8/Optional.md)  

## 1.5. Date/Time API  
&emsp; [DateTime](/docs/java/JDK8/DateTime.md)  

## 1.6. 异常捕获的改变   
&emsp; 新的try…cache可以自动关闭在try表达式中打开的对象，而无需开发者手动关闭。  
&emsp; 多个流对象打开语句，用分号分隔，不是逗号。  

```java
try(ObjectInputStream in=new ObjectInputStream(new FileInputStream("p1.obj"))){
    System.out.println(Person.class.hashCode());
    Person person=(Person)in.readObject();
    System.out.println(person.staticString);
} catch (Exception e) {
    e.printStackTrace();
}
```
&emsp; 不再需要：  

```java
finally{
    in.close();
}
```

## 1.7. Base64  
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
|getDecoder()|返回Base64.Decoder，解码使用基本型base64编码方案|
|getMimeDecoder()|返回Base64.Decoder，解码使用MIME型base64 编码方案。|
|getUrlDecoder()|返回Base64.Decoder，解码使用URL和文件名安全型 base64编码方案。|
|Encoder| | 
|getEncoder()|返回Base64.Encoder，编码使用基本型base64编码方案。|
|getMimeEncoder()|返回Base64.Encoder，编码使用MIME型base64编码方案。|
|getMimeEncoder(int lineLength, byte[] lineSeparator)|返回Base64.Encoder，编码使用MIME型base64编码方案，可以通过参数指定每行的长度及行的分隔符。|
|getUrlEncoder()|返回Base64.Encoder，编码使用URL和文件名安全型 base64 编码方案。|

```java
// 使用基本编码
String base64encodedString = Base64.getEncoder().encodeToString("runoob?java8".getBytes("utf-8"));
System.out.println("Base64 编码字符串 (基本) :" + base64encodedString);
// 解码
byte[] base64decodedBytes = Base64.getDecoder().decode(base64encodedString);
System.out.println("原始字符串: " + new String(base64decodedBytes, "utf-8"));
```
