---
title: Lambda
date: 2020-01-28 00:00:00
tags:
    - JDK
---

<!-- TOC -->

- [1. Lambda表达式](#1-lambda表达式)
    - [1.1. 函数式接口](#11-函数式接口)
    - [1.2. Lambda表达式](#12-lambda表达式)
        - [1.2.1. 简介](#121-简介)
        - [1.2.2. 语法-1](#122-语法-1)
        - [1.2.3. 使用教程](#123-使用教程)
            - [1.2.3.1. 变量作用域（调用Lambda表达式外部参数）](#1231-变量作用域调用lambda表达式外部参数)
            - [1.2.3.2. 泛型函数式接口](#1232-泛型函数式接口)
            - [1.2.3.3. Lambda表达式作为参数传递](#1233-lambda表达式作为参数传递)
            - [1.2.3.4. Lambda表达式作为返回值](#1234-lambda表达式作为返回值)
    - [1.3. 方法引用、构造方法引用](#13-方法引用构造方法引用)
    - [1.4. 常用函数式接口](#14-常用函数式接口)

<!-- /TOC -->

# 1. Lambda表达式  
## 1.1. 函数式接口  
&emsp; 函数式接口：接口中只有一个抽象方法。可以有默认方法、静态方法，可以覆写Object类中的public方法。  
&emsp; 标记注解@FunctionalInterface用于声明接口是函数式接口，此接口中多于一个抽象方法，编译器会报错。但是创建函数式接口并不需要@FunctionalInterface，此注解只是用来提供信息，也就是更显示的说明此接口是函数式接口。根据java8的定义，任何只有一个抽像方法的接口都是函数式接口（可以包括其它变量成员和方法，只要抽像方法是一个就可以），没有@FunctionalInterface注解，也是函数式接口。  

```java
@FunctionalInterface
interface Converter<F, T> {
    T convert(F from);
}
```
&emsp; **<font color = "lime">函数式接口的实例创建三种方式：lambda表达式；方法引用；构造方法引用。</font>**   

## 1.2. Lambda表达式  
### 1.2.1. 简介  
&emsp; Lambda表达式是一个匿名函数，即没有函数名的函数。用于创建一个函数式接口的实例。  
<!--
Lambda表达式会被匹配到函数式接口的抽象方法上（Lambda表达式是函数式接口中唯一抽象方法的方法体）。Lambda表达式的参数的类型和数量必须与函数式接口内的抽象方法的参数兼容；返回类型必须兼容；并且lambda表达式可能抛出的异常必须能被该方法接受。 
 --> 
&emsp; <font color = "red">Lambda表达式只是对函数式接口中抽象方法的引用，并不执行，将其赋值给了一个变量。若要执行，使用（变量.方法名称）。</font>  
  
### 1.2.2. 语法-1    
&emsp; Lambda表达式语法：一个Lambda表达式由用逗号分隔的参数列表、–>符号与{}函数体三部分表示。函数体既可以是一个表达式，也可以是一个语句块。  
1. 方法体为表达式，该表达式的值作为返回值返回。  (parameters) -> expression  
2. 方法体为代码块，必须用{}来包裹起来。  

```java
(parameters参数列表) -> { statements; }
<函数式接口>  <变量名> = (参数1，参数2...) -> {
    //方法体
}
```
&emsp; Lambda表达式的参数：     
1. 如果形参列表为空，只需保留()。  
2. 如果形参只有1个，()可以省略，只需要参数的名称即可。  
3. 对于Lambda表达式中的多个参数，如果需要显示声明一个参数的类型，那么必须为所有的参数都声明类型。  
4. 形参列表的数据类型会自动推断；参数的类型既可以明确声明，也可以根据上下文来推断。eg：(int a)与(a)效果相同。  

&emsp; 类型推断：在Lambda表达式中，不需要明确指出参数类型，javac编译器会通过上下文自动推断参数的类型信息。根据上下文推断类型的行为称为类型推断。Java8提升了Java中已经存在的类型推断系统，使得对Lambda表达式的支持变得更加强大。javac会寻找紧邻lambda表达式的一些信息通过这些信息来推断出参数的正确类型。  

&emsp; Lambda的表达式、方法体：  
1. 如果执行语句只有1句，无返回值，{}可以省略；若有返回值，则若想省去{}，则必须同时省略return，且执行语句也保证只有1句。  
2. 如果Lambda表达式的主体包含一条以上语句，则表达式必须包含在花括号{}中（形成代码块）。且需要一个return返回值，但若函数式接口里面方法返回值是void，则无需返回值。语句块中，return语句会把控制权交给匿名方法的调用者；break和continue只能在循环中使用；如果函数体有返回值，那么函数体内部的每一条路径都必须返回值。  


        可选类型声明：不需要声明参数类型，编译器可以统一识别参数值。
        可选的参数圆括号：一个参数无需定义圆括号，但多个参数需要定义圆括号。
        可选的大括号：如果主体包含了一个语句，就不需要使用大括号。
        可选的返回关键字：如果主体只有一个表达式返回值则编译器会自动返回值，大括号需要指定明表达式返回了一个数值

### 1.2.3. 使用教程  
#### 1.2.3.1. 变量作用域（调用Lambda表达式外部参数）  
&emsp; 访问外层作用域定义的局部变量、类的属性：  

* 访问局部变量：lambda表达式若访问了局部变量，则局部变量必须是final的。若局部变量没有加final关键字，系统会自动添加，此后在修改该局部变量，会编译错误。  
* 访问类的属性：lambda内部使用this关键字（或不使用）访问或修改全局变量、实例方法。  

```java
public class Test2 {
    public static void main(String[] args) {
        Test2 test = new Test2();
        test.method();
    }
    @Override
    public String toString() {
        return "Lambda";
    }
    public void method() {
        Runnable runnable = () -> {
            System.out.println(this.toString());
        };
        new Thread(runnable).start();
    }
}
```

#### 1.2.3.2. 泛型函数式接口  
&emsp; Lambda表达式自身不能指定类型参数。因此Lambda表达式不能是泛型。但是与Lambda表达式关联的函数式接口可以泛型。此时，Lambda表达式的目标类型部分由声明函数式接口引用时指定的参数类型决定。  

```java
interface SomeFunc<T> {
    T func(T t);
}
```

```java
class GenericFunctionalInterfaceDemo {
    public static void main(String[] args) {
        SomeFunc<String> reverse = (str) -> {
            int i;
            String result = "";
            for (i = str.length() - 1; i >= 0; i--) {
                result += str.charAt(i);
            }
            return result;
        };
        System.out.println("lambda reserved is " + reverse.func("lambda"));

        SomeFunc<Integer> factorial = (n) -> {
            int result = 1;
            for (int i = 1; i <= n; i++) {
                result = result * i;
            }
            return result;
        };
        System.out.println("The factorial of 3 is " + factorial.func(3));
    }
}
```
结果：  

    lambda reserved is adbmal
    The factorial of 3 is 6  

&emsp; 分析：T指定了func()函数的返回类型和参数类型。这意味着它与任何只接收一个参数，并返回一个相同类型的值的lambda表达式兼容。  
&emsp; SomeFunc接口用于提供对两种不同类型的lambda表达式的引用。第一种表达式使用String类型，第二种表达式使用Integer类型。因此，同一个函数式接口可以用于reserve lambda表达式和factorial lambda表达式。区别仅在于传递给SomeFunc的参数类型。  

#### 1.2.3.3. Lambda表达式作为参数传递  
&emsp; 为了将lambda表达式作为参数传递，接收lambda表达式的参数的类型必须是与该lambda表达式兼容的函数式接口的类型。  
&emsp; 注：Lambda表达式作为方法参数使用。  

```java
//Use lambda expressions as an argument to method
interface StringFunc {
    String func(String n);
}
```

```java
class lambdasAsArgumentsDemo {
    static String stringOp(StringFunc sf, String s) {
        return sf.func(s);
    }
    public static void main(String[] args) {
        String inStr = "lambda add power to java";
        String outStr;
        System.out.println("Here is input string: " + inStr);
        
        //Lambda表达式作为方法参数使用
        //第一种方式
        outStr = stringOp((str) -> str.toUpperCase(), inStr);
        System.out.println("The string in uppercase: " + outStr);
        //第二种方式
        outStr = stringOp((str) ->
        {
            String result = "";
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) != '') {
                    result += str.charAt(i);
                }
            }
            return result;
        }, inStr);
        System.out.println("The string with spaces removed: " + outStr);
        //第三种方式
        //当块lambda特别长，不适合嵌入到方法的调用中时，很容易把块lambda赋给一个函数式接口变量。然后，可以简单地把该引用传递给方法。
        StringFunc reverse = (str) ->
        {
            String result = "";
            for (int i = str.length() - 1; i >= 0; i--) {
                result += str.charAt(i);
            }
            result result;
        };
        System.out.println("The string reserved: " + stringOp(reverse, inStr));
    }
}
```
&emsp; 分析：当块lambda看上去特别长，不适合嵌入到方法的调用中时，很容易把块lambda赋给一个函数式接口变量。然后，可以简单地把该引用传递给方法。  

#### 1.2.3.4. Lambda表达式作为返回值  
......

## 1.3. 方法引用、构造方法引用  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/java8/java-1.png)  
&emsp; **<font color= "lime">当Lambda表达式中只是执行一个方法调用时，可以舍弃Lambda表达式，直接通过方法引用的形式可读性更高一些。</font>** 方法引用是一种更简洁易懂的Lambda表达式。方法引用是用来直接访问类或者实例的已经存在的方法或者构造方法（如果是类，不用实例化对象）。方法引用提供了一种引用而不执行方法的方式，它需要由兼容的函数式接口构成的目标类型上下文。计算时，方法引用会创建函数式接口的一个实例。  
&emsp; 方法引用的标准形式是：类名/对象的引用::方法名/new（注意：只需要写方法名，不需要写括号）。有以下四种形式的方法引用：  

|类型	|示例|
|---|---|	
|引用静态方法	|ContainingClass::staticMethodName	|接受一个Class类型的参数|
|引用某个对象的实例方法	|containingObject::instanceMethodName	|方法没有参数。|
|引用某个类型的任意对象的实例方法	|ContainingType::methodName	|方法接受一个参数。与以上不同的地方在于，以上是在列表元素上分别调用方法，而此是在某个对象上调用方法，将列表元素作为参数传入|
|引用构造方法	|ClassName::new	|构造器方法是没有参数|

## 1.4. 常用函数式接口  
&emsp; Function，类型转换；  Supplier，供给型接口；  Consumer，消费接口；  Predicate，断言型接口；
