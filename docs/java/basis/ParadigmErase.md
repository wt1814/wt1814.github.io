

<!-- TOC -->

- [1. 范型擦除](#1-范型擦除)
    - [1.1. 泛型擦除概念](#11-泛型擦除概念)
    - [1.2. 示例](#12-示例)
    - [1.3. ~~利用反射越过泛型检查~~](#13-利用反射越过泛型检查)
    - [1.4. ★★★运行时泛型信息获取](#14-★★★运行时泛型信息获取)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 范型擦除：Java会在编译Class文件时，将范型擦除成原始类型Object。  
2. 利用反射越过泛型检查  
&emsp; 反射是获取类Class文件进行操作。通过反射获取对象后可以获得相应的add方法，并向方法里面传入任何对象。  
3. 运行时泛型信息获取

# 1. 范型擦除
<!-- 
Java-TypeToken原理及泛型擦除
https://mp.weixin.qq.com/s/oPnJGmw-fNwtgG6ioAJ-ZQ

https://mp.weixin.qq.com/s/skxnaaPz2eN1YASUlfwMDA
-->

## 1.1. 泛型擦除概念
<!-- 
&emsp; 泛型是通过类型擦除来实现的，<font color = "red">编译器在编译时擦除了所有泛型类型相关的信息，所以在运行时不存在任何泛型类型相关的信息。</font>  
&emsp; 泛型擦除具体来说就是在编译成字节码时首先进行类型检查，接着进行类型擦除（即所有类型参数都用它们的限定类型替换，包括类、变量和方法），接着如果类型擦除和多态性发生冲突时就在子类中生成桥方法解决，接着如果调用泛型方法的返回类型被擦除则在调用该方法时插入强制类型转换。  
-->
&emsp; 因为泛型的信息只存在于 java 的编译阶段，编译期编译完带有 java 泛型的程序后，其生成的 class 文件中与泛型相关的信息会被擦除掉，以此来保证程序运行的效率并不会受影响，也就说泛型类型在 jvm 中和普通类是一样的。  

&emsp; 因为泛型信息只存在于代码编译阶段，所以在进入 JVM 之前，会把与泛型相关的信息擦除，这就称为 类型擦除。  

## 1.2. 示例
<!-- 
https://www.cnblogs.com/wuqinglong/p/9456193.html
https://www.jianshu.com/p/6493fdab6ac5
-->
&emsp; 1）无限制类型擦除  

&emsp; 类型擦除前：  

```java
public class Result<T>{
    private T data;
}
```

&emsp; 类型擦除后：

```java
public class Result{
    private Object data;
}
```

&emsp; 2）有限制类型擦除

&emsp; 类型擦除前：

```java
public class Result<T extends Number>{
    private T data;
}
```

&emsp; 类型擦除后：

```java
public class Result{
    private Number data;
}
```

&emsp; 3）擦除方法中类型定义的参数

&emsp; 类型擦除前：

```java
private <T extends Number> T getValue(T value){
    return value;
}
```

&emsp; 类型擦除后：

```java
private Number getValue(Number value){
    return value;
}
```

## 1.3. ~~利用反射越过泛型检查~~
<!-- 
重要***利用反射越过泛型检查
https://www.jianshu.com/p/6493fdab6ac5
ava泛型类型擦除以及类型擦除带来的问题
https://www.cnblogs.com/wuqinglong/p/9456193.html
-->
&emsp; 反射其实就是利用类加载过后的Class对象，或者说就是用的那个.class文件里面的信息，而这里面的字节码是编译过的。  

&emsp; 通过反射获取对象后可以获得相应的add方法，并向方法里面传入任何对象。  
&emsp; 当利用反射往`List<String>`里面插入Integer的时候，在print()的时候也会出问题，其实原因都大同小异。此时的print()会调用String的方式，但是参数却是个Integer。  


## 1.4. ★★★运行时泛型信息获取
<!-- 
Java 的泛型擦除和运行时泛型信息获取
https://www.jianshu.com/p/b5bc4b7ff236
-->

&emsp; 可以通过定义类的方式（通常为匿名内部类，因为我们创建这个类只是为了获得泛型信息）在运行时获得泛型参数，从而满足例如序列化、反序列化等工作的需要。  
