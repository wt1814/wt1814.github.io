---
title: Java基础
date: 2020-01-27 00:00:00
tags:
    - Java
---
<!-- TOC -->

- [1. Java关键字](#1-java关键字)
    - [1.1. Java访问控制符](#11-java访问控制符)
    - [1.2. static-1](#12-static-1)
    - [1.3. final-1](#13-final-1)
- [2. Java变量](#2-java变量)
    - [2.1. 声明、初始化、实例化](#21-声明初始化实例化)
    - [2.2. 全局变量、成员变量、局部变量](#22-全局变量成员变量局部变量)
    - [2.3. Java类创建对象时初始化顺序](#23-java类创建对象时初始化顺序)
- [3. Java方法](#3-java方法)
    - [3.1. 方法重载](#31-方法重载)
    - [3.2. 方法重写](#32-方法重写)
    - [3.3. 可变参数](#33-可变参数)
    - [3.4. ※※※值传递还是引用传递？](#34-※※※值传递还是引用传递)
        - [基本类型](#基本类型)
        - [引用类型](#引用类型)
- [4. Java类、接口、抽象类](#4-java类接口抽象类)
    - [抽象类](#抽象类)
        - [4.2. 抽象类作为方法参数与返回值](#42-抽象类作为方法参数与返回值)
    - [接口](#接口)
        - [4.3. 接口作为方法参数与返回值](#43-接口作为方法参数与返回值)
    - [4.1. ※※※抽象类和接口的应用](#41-※※※抽象类和接口的应用)
- [5. Java面向对象的三大特性](#5-java面向对象的三大特性)
    - [5.1. 封装](#51-封装)
    - [5.2. 继承](#52-继承)
        - [5.2.1. super 与 this 关键字](#521-super-与-this-关键字)
    - [5.3. 多态](#53-多态)

<!-- /TOC -->

# 1. Java关键字  
## 1.1. Java访问控制符  
&emsp; JAVA语言中有公共的（public），私有的（private），保护的（protacted）和默认的（default）四种访问控制符。　  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/java-2.png)  
&emsp; 访问属性：在外部类中访问public和default的属性，可以通过类的对象名.属性名直接访问。在外部类中访问private的属性，必须通过对象的get、set方法（get、set方法声明为public）。  
&emsp; 访问方法：在外部类中访问private的方法，必须通过反射。  

## 1.2. static-1  
<!-- https://mp.weixin.qq.com/s?__biz=MzI0ODk2NDIyMQ==&mid=2247484455&idx=1&sn=582d5d2722dab28a36b6c7bc3f39d3fb&chksm=e999f135deee7823226d4da1e8367168a3d0ec6e66c9a589843233b7e801c416d2e535b383be&token=1154740235&lang=zh_CN&scene=21#wechat_redirect -->
&emsp; static 是 Java 中非常重要的关键字，static 表示的概念是 静态的，在 Java 中，static 主要用来  

* 修饰变量，static 修饰的变量称为静态变量、也称为类变量，类变量属于类所有，对于不同的类来说，static 变量只有一份，static 修饰的变量位于方法区中；static 修饰的变量能够直接通过 类名.变量名 来进行访问，不用通过实例化类再进行使用。  
* 修饰方法，static 修饰的方法被称为静态方法，静态方法能够直接通过 类名.方法名 来使用，在静态方法内部不能使用非静态属性和方法。  
* static 可以修饰代码块，主要分为两种，一种直接定义在类中，使用 static{}，这种被称为静态代码块，一种是在类中定义静态内部类，使用 static class xxx 来进行定义。  
* static 可以用于静态导包，通过使用 import static xxx  来实现，这种方式一般不推荐使用。  
* static 可以和单例模式一起使用，通过双重检查锁来实现线程安全的单例模式。

&emsp; Java中static块执行时机：<font color = "red">static块的执行发生在“初始化”的阶段。</font>类被加载了不一定就会执行静态代码块，只有一个类被主动使用的时候，静态代码才会被执行！   

## 1.3. final-1  
&emsp; final 是 Java 中的关键字，它表示的意思是 <font color="red">不可变的</font>，在 Java 中，final 主要用来  

* 修饰类，final 修饰的类不能被继承，不能被继承的意思就是不能使用 extends 来继承被 final 修饰的类。
* 修饰变量，final 修饰的变量不能被改写，不能被改写的意思有两种，对于基本数据类型来说，final 修饰的变量，其值不能被改变，final 修饰的对象，对象的引用不能被改变，但是对象内部的属性可以被修改。final 修饰的变量在某种程度上起到了不可变的效果，所以，可以用来保护只读数据，尤其是在并发编程中，因为明确的不能再为 final 变量进行赋值，有利于减少额外的同步开销。
* 修饰方法，final 修饰的方法不能被重写。  

&emsp; final 修饰符和 Java 程序性能优化没有必然联系


# 2. Java变量  
## 2.1. 声明、初始化、实例化  

```
A a;         //声明
a = null;    //初始化
a = new A(); //实例化
```

## 2.2. 全局变量、成员变量、局部变量  
1. 全局（静态）变量：在java中可以理解为用static final修饰的变量。  
&emsp; 生命周期：当类加载的时候，就开始被创建，在类中只有一份；会跟着类的消失而消失，生存时间较长。  

2. 成员变量：写在类声明的大括号中的变量, 称之为成员变量(属性, 实例变量)。      
&emsp; 初始化：成员变量可以不显式初始化，它们可以由系统设定默认值；（成员变量如果没有实例化那么变量是放在栈中；实例化了对象放在堆中，栈中放的是指向堆中对象的引用地址。）  
&emsp; 作用域：作用在整个类中(除静态方法不能使用，静态方法没有隐式的this)，被对象调用。  
&emsp; 生命周期：在对象被创建时而存在，当对象被GC回收的同时，也会消失，生存时间适中。  

3. 局部变量：写在函数或者代码块中的变量, 称之为局部变量。局部变量放在栈中，new的对象放在堆中，8种基本数据类型变量放在栈中，变量所对应的值是放在栈帧中。  
&emsp; 作用域：函数体或代码块中。  
&emsp; 初始化：局部变量可以先定义再初始化, 也可以定义的同时初始化。局部变量没有默认值，所以必须设定初始赋值。   
&emsp; 修饰符：局部变量不能加static，包括protected, private, public这些也不能加。局部变量保存在栈中。  
&emsp; 生命周期：当方法被调用时而存在，当方法调用结束而消失，生存时间短。  

## 2.3. Java类创建对象时初始化顺序  
&emsp; <font color = "red">Java类new初始化顺序：父类静态变量——父类静态代码块——子类静态代码块——父类非静态变量——父类非静态代码块——父类构造函数——子类非静态变量——子类非静态代码块——子类构造函数。如果实际类中没有定义则跳过。</font>  

```
// 主类，用来创建子类对象，验证结果
public class Main {
    public static void main(String[] args) {
        new Son();
    }
}
```

```
// 书类，用于测试对象成员变量
class Book{
    public Book(String user){
        System.out.println(user + "成员变量");
    }
}
```

```
// 子类
class Son extends Fa{
    static Book book= new Book("子类静态");
    static{
        System.out.println("子类静态代码块");
    }

    Book sBook = new Book("子类");
    {
        System.out.println("子类非静态代码块");
    }

    public Son(){
        System.out.println("子类构造方法");
    }
}
```

```
// 父类
class Fa{
    static Book book= new Book("父类静态");
    static{
        System.out.println("父类静态代码块");
    }

    Book fBook = new Book("父类");
    {
        System.out.println("父类非静态代码块");
    }
    public Fa(){
        System.out.println("父类构造方法");
    }
}
```

&emsp; 输出结果：  

```
父类静态成员变量
父类静态代码块
子类静态成员变量
子类静态代码块
父类成员变量
父类非静态代码块
父类构造方法
子类成员变量
子类非静态代码块
子类构造方法
```

# 3. Java方法  
## 3.1. 方法重载  
&emsp; 类中有多个方法,有着相同的方法名,但是方法的参数各不相同,这种情况被称为方法的重载。方法的重载可以提供方法调用的灵活性。  
&emsp; 方法重载必须满足一下条件:  
1. 方法名相同；  
2. 参数列表不同(参数的类型、个数、顺序的不同)；  
3. 方法的返回值可以不同，也可以相同。  
&emsp; 注：在java中,判断一个类中的两个个方法是否重载,主要参考两个方面：方法名字和参数列表。  

```java
public void test(Strig str){}
public void test(int a){}
public void test(Strig str,double d){}
```

## 3.2. 方法重写  
&emsp; 子类继承父类，继承了父类中的方法，但是父类中的方法并不一定能满足子类中的功能需要，所以子类中需要把方法进行重写。  
&emsp; 重写的语法：  

* 方法名必须相同  
* 参数列表必须相同  
* 访问控制修饰符可以被扩大,但是不能被缩小，public >protected >default >private  
* 抛出异常类型的范围可以被缩小,但是不能被扩大，ClassNotFoundException ---> Exception   
* 返回类型可以相同,也可以不同,如果不同的话,子类重写后的方法返回类型必须是父类方法返回类型的子类型  

## 3.3. 可变参数  
&emsp; 在不确定参数的个数时，可以使用可变的参数列表。  
1. 语法：参数类型…（三个点）。例如：void printArray（Object...）。  
&emsp; <font color = "red">每个方法最多只有一个可变参数，因为：可变参数必须是方法的最后一个参数。</font>  
2. 可变参数的类型：可变参数可以设置为任意类型：引用类型，基本类型。  
3. 参数的个数：0个参数；1个参数：如果是数组，那么就直接将这个数组作为参数传进方法里面，不再填充新的数组；多个参数：参数可以是数组，也可以是单个变量、常量；但是这时候会，将这些参数填充进新的数组里面，再将这个数组，传进方法里面；  
4. 可变参数的使用：可变参数完全可以当作一个数组来使用，或者说本质上可变参数就是一个数组。所以，数组拥有的方法、属性，可变参数一样拥有。  

```java
public void varArgMethod(int b,int... arr) {
    //和数组一样，拥有属性length
    int lenth = arr.length;
    //索引遍历
    for(int i=0;i<arr.length;i++) {
        System.out.println(arr[i]);
    }
    //forEach循环遍历
    for(int ele:arr) {
        System.out.println(ele);
    }
}
```

&emsp; 上面的例子中，可变参数的使用跟数组的使用是完全一样，也就是说，可变参数是可以等价成数组的。  
&emsp; 从反编译的结果可以看出，编译器不仅将可变参数处理成数组varArgMethod(int b, int arr[])，还处理了调用可变参数方法处的参数列表，把参数列表封装进一个数组varArgMethod(5, new int[]{7, 8, 9, 10, a})。  

```java
public <T> T underwrite(String platformCode, String uuid, Object... objects) {
    LOG.info("退保核批校验，退保时间");
    UnderWrite underWrite = (UnderWrite) objects[0];
    CancelInfo cancelInfo = (CancelInfo) objects[1];
    Policybasic policybasic = (Policybasic) objects[2];
}
```

## 3.4. ※※※值传递还是引用传递？  

&emsp; 基本概念：  
&emsp; 实参：实际参数，是提前准备好并赋值完成的变量。分配到栈上。如果是基本类型直接分配到栈上，如果是引用类型，栈上分配引用空间存储指向堆上分配的对象本身的指针。String等基本类型的封装类型比较特殊，后续讨论。  
&emsp; 形参：形式参数，方法调用时在栈上分配的实参的拷贝。  
&emsp; 按值传递：方法调用时，实际参数把它的值传递给对应的形式参数，形参接收的是原始值的一个拷贝，此时内存中存在两个相等的变量。  
&emsp; 按引用传递：方法调用时将实参的地址传递给对应的形参，实参和形参指向相同的内容。  

&emsp; ***<font color = "red">Java没有引用传递，只有值传递。</font>***基本类型和引用类型传递的都是参数的副本。

### 基本类型  

```
public class Test {
    public static void main(String[] args) {
        int value = 100;
        change(value);
        System.out.println("outer: " +  value);
    }

    static void change(int value) {
        value = 200;
        System.out.println("inner: " +  value + "\n");
    }

}
```
&emsp; 结果输出：  

```
inner: 200
outer: 100
```
&emsp; 方法修改的只是形式参数，对实际参数没有作用。方法调用结束后形式参数随着栈帧回收。  

### 引用类型  

```
public class TestDemo {
    public static void main(String[] args) {
        TestDemo2 testDemo2 = new TestDemo2();
        System.out.println("调用前:" + testDemo2.hashCode());
        testValue(testDemo2);
        System.out.println("调用后:" + testDemo2.hashCode());
    }
    
    public static void testValue(TestDemo2 testDemo) {
        testDemo = new TestDemo2();
    }
}

class TestDemo2 {
    int age = 1;
}
```
&emsp; 打印结果：  

    调用前:366712642
    调用后:366712642  

&emsp; 这里可以看到testDemo2 的值依然没有变化，调用前后所指向的内存地址值是一样的。对传入地址值的改变并不会影响原来的参数。  

&emsp; ***<font color= "red">既然是值传递，为什么参数是引用类型的时候，方法内对对象进行操作会影响原来对象，这真的是值传递么？</font>***  

```java
public class TestDemo {
    public static void main(String[] args) {
        TestDemo2 testDemo2 = new TestDemo2();
        System.out.println("调用前:" + testDemo2.age);
        testValue(testDemo2);
        System.out.println("调用后:" + testDemo2.age);
    }
    
    public static void testValue(TestDemo2 testDemo) {
        testDemo.age = 9;
    }
}

class TestDemo2 {
    int age = 1;
}
```

&emsp; 打印结果  

    调用前:1
    调用后:9  

&emsp; ***<font color = "red">传入的参数是testDemo2 对象地址值的一个拷贝，但是形参和实参的值都是一样的，都指向同一个对象，所以对象内容的改变会影响到实参。</font>***  

&emsp; 结论：***<font color = "red">JAVA的参数传递确实是值传递，不管是基本参数类型还是引用类型，形参值的改变都不会影响实参的值。如果是引用类型，形参值所对应的对象内部值的改变会影响到实参。</font>***  

# 4. Java类、接口、抽象类  
<!-- https://mp.weixin.qq.com/s/hciO815os-gfxW4Oa15ErA -->

## 抽象类  
&emsp; 抽象类是为了继承而存在的。
&emsp; 抽象类和普通类的主要有三点区别：  
1. 抽象方法必须为public或者protected（因为如果为private，则不能被子类继承，子类便无法实现该方法），缺省情况下默认为public。  
2. 抽象类不能用来创建对象；  
3. 如果一个类继承于一个抽象类，则子类必须实现父类的抽象方法。如果子类没有实现父类的抽象方法，则必须将子类也定义为为abstract类。  

### 4.2. 抽象类作为方法参数与返回值   
* 抽象类作为方法参数  
&emsp; 当遇到方法参数为抽象类类型时，要传入一个实现抽象类所有抽象方法的子类对象。如下代码演示：  

```
//抽象类
abstract class Person{
	public abstract void show();
}
```

```
class Student extends Person{
	@Override
	public void show() {
		System.out.println("重写了show方法");
	}
}
```

```
//测试类
public class Test {
	public static void main(String[] args) {
		//通过多态的方式，创建一个Person类型的变量，而这个对象实际是Student
		Person p = new Student();
		//调用method方法
		method(p);
	}
	
	//定义一个方法method，用来接收一个Person类型对象，在方法中调用Person对象的show方法
	public static void method(Person p){//抽象类作为参数
		//通过p变量调用show方法,这时实际调用的是Student对象中的show方法
		p.show();	
    }
}
```

* 抽象类作为方法返回值  
&emsp; 抽象类作为方法返回值，需要返回一个实现抽象类所有抽象方法的子类对象。如下代码演示：  

```
//抽象类
abstract class Person{
	public abstract void show();
}
```

```
class Student extends Person{
	@Override
	public void show() {
		System.out.println("重写了show方法");
	}
}
```

```
//测试类
public class Test {
	public static void main(String[] args) {
		//调用method方法，获取返回的Person对象
		Person p = method();
		//通过p变量调用show方法,这时实际调用的是Student对象中的show方法
		p.show();
	}
	
	//定义一个方法method，用来获取一个Person对象，在方法中完成Person对象的创建
	public static Person method(){
		Person p = new Student();
		return p;
	}
}
```


## 接口  
接口中可以含有 变量和方法。但是要注意，接口中的变量会被隐式地指定为public static final变量（并且只能是public static final变量，用private修饰会报编译错误），而方法会被隐式地指定为public abstract方法且只能是public abstract方法（用其他关键字，比如private、protected、static、 final等修饰会报编译错误），并且接口中所有的方法不能有具体的实现，也就是说，接口中的方法必须都是抽象方法。从这里可以隐约看出接口和抽象类的区别，接口是一种极度抽象的类型，它比抽象类更加“抽象”，并且一般情况下不在接口中定义变量。  
允许一个类遵循多个特定的接口。<font color = "red">如果一个非抽象类遵循了某个接口，就必须实现该接口中的所有方法。对于遵循某个接口的抽象类，可以不实现该接口中的抽象方法。</font>  

### 4.3. 接口作为方法参数与返回值
* 接口作为方法参数  
&emsp; 当遇到方法参数为接口类型时，那么该方法要传入一个接口实现类对象。如下代码演示。  

```
//接口
interface Smoke{
	public abstract void smoking();
}
```

```
class Student implements Smoke{
	@Override
	public void smoking() {
		System.out.println("课下吸口烟，赛过活神仙");
	}
}
```

```
//测试类
public class Test {
	public static void main(String[] args) {
		//调用method方法，获取返回的会吸烟的对象
		Smoke s = method();
		//通过s变量调用smoking方法,这时实际调用的是Student对象中的smoking方法
		s.smoking();
	}
	
	//定义一个方法method，用来获取一个具备吸烟功能的对象，并在方法中完成吸烟者的创建
	public static Smoke method(){
		Smoke sm = new Student();
		return sm;
	}
}
```

* 接口作为方法返回值  
&emsp; 当遇到方法返回值是接口类型时，那么该方法需要返回一个接口实现类对象。如下代码演示。  

```
//接口
interface Smoke{
	public abstract void smoking();
}
```

```
class Student implements Smoke{
	@Override
	public void smoking() {
		System.out.println("课下吸口烟，赛过活神仙");
	}
}
```

```
//测试类
public class Test {
	public static void main(String[] args) {
		//通过多态的方式，创建一个Smoke类型的变量，而这个对象实际是Student
		Smoke s = new Student();
		//调用method方法
		method(s);
	}
	
	//定义一个方法method，用来接收一个Smoke类型对象，在方法中调用Smoke对象的show方法
	public static void method(Smoke sm){//接口作为参数
		//通过sm变量调用smoking方法，这时实际调用的是Student对象中的smoking方法
		sm.smoking();
	}
}
```

## 4.1. ※※※抽象类和接口的应用  
<!-- https://m.nowcoder.com/questionTerminal?uuid=16194802568b45d3ada8e57cc10b5d51 -->
&emsp; ***抽象类和接口的区别：***
&emsp; 抽象类中可以定义一些子类的公共方法，子类只需要增加新的功能，不需要重复写已经存在的方法；而接口中只是对方法的申明和常量的定义。  
1. 变量：抽象类中的成员变量可以是各种类型的，而接口中的成员变量只能是public static final类型的；  
2. 普通方法：抽象类可以提供成员方法的实现细节，而接口中只能存在public abstract方法。JDK1.8以上，接口允许有默认方法。  
3. 静态：抽象类可以有静态代码块和静态方法。JDK1.8以上，接口允许有静态方法。  
4. 一个类只能继承一个抽象类，而一个类却可以实现多个接口。  

&emsp; ***接口的应用场景***：在定义相互调用规则时，可以使用接口。面向接口进行编程的好处，是能极大降低软件系统的相互耦合性，接口的定义者按照接口进行调用，而实现者去实现接口。  
&emsp; ***抽象类的应用场景***：在既需要统一的接口，又需要实例变量或缺省的方法的情况下，可以使用抽象类。最常见的有：
1. 定义了一组接口，但又不想强迫每个实现类都必须实现所有的接口。可以用abstract class定义一组方法体，甚至可以是空方法体，然后由子类选择自己所感兴趣的方法来覆盖。
2. 某些场合下，只靠纯粹的接口不能满足类与类之间的协调，还必需类中表示状态的变量来区别不同的关系。abstract的中介作用可以很好地满足这一点。
3. 规范了一组相互协调的方法，其中一些方法是共同的，与状态无关的，可以共享的，无需子类分别实现；而另一些方法却需要各个子类根据自己特定的状态来实现特定的功能。  

&emsp; 一般情况下，在开始使用的时候用接口，后来实现的子类里有些子类有共同属性，或者相同的方法实现，所以提取出来一个抽象类，作为类和接口的中介。

# 5. Java面向对象的三大特性  
## 5.1. 封装  
&emsp; 在定义一个对象的特性的时候，有必要决定这些特性的可见性，即哪些特性对外部是可见的，哪些特性用于表示内部状态。通常，应禁止直接访问一个对象中数据的实际表示，而应通过操作接口来访问，这称为信息隐藏。
封装的步骤：使用访问控制符private，提供get、set方法。  

## 5.2. 继承  
&emsp; 继承的特性：
* 子类拥有父类非 private 的属性、方法。  
* 子类可以拥有自己的属性和方法，即子类可以对父类进行扩展。  
* 子类可以用自己的方式实现父类的方法。  
* Java 的继承是单继承，但是可以多重继承，单继承就是一个子类只能继承一个父类，多重继承就是，例如 A 类继承 B 类，B 类继承 C 类，所以按照关系就是 C 类是 B 类的父类，B 类是 A 类的父类，这是 Java 继承区别于 C++ 继承的一个特性。  
* 提高了类之间的耦合性（继承的缺点，耦合度高就会造成代码之间的联系越紧密，代码独立性越差）。  

### 5.2.1. super 与 this 关键字  
&emsp; super关键字：引用当前对象的父类；this关键字：指向自己的引用。  
&emsp; ***<font color = "red">super主要有两种用法：</font>***
1. ***super.成员变量/super.成员方法，主要用来在子类中调用父类的同名成员变量或者方法。***
2. ***super(parameter1,parameter2....)，主要用在（父类中没有无参构造函数）子类的构造器中显示地调用父类的构造器，且必须是子类构造器的第一个语句。***  

## 5.3. 多态  

<!-- 
Java 多态的实现机制
https://mp.weixin.qq.com/s/a3gS8-rhINeMORRczwKhrg
-->
&emsp; <font color = "red">多态是指程序中定义的引用变量所指向的具体类型和通过该引用变量发出的方法调用在编程时并不确定，而是在程序运行期间才确定。</font>即一个引用变量到底会指向哪个类的实例对象，该引用变量发出的方法调用到底是哪个类中实现的方法，必须在由程序运行期间才能决定。  
&emsp; 多态存在的三个必要条件：继承、重写、父类引用指向子类对象。  
&emsp; 在Java中有两种形式可以实现多态：继承（多个子类对同一方法的重写）和接口（实现接口并覆盖接口中同一方法）。  

&emsp; 多态的定义格式：父类的引用变量指向子类对象  

```
父类类型  变量名 = new 子类类型();
变量名.方法名();  
```

<table>
    <tr>
        <td>List list = new ArrayList(); </td>
        <td>父类引用指向子类对象，使用子类的数据结构和具体的方法实现。但是只能调用父类的方法，不能调用子类的私有方法。</td>
    </tr>
    <tr>
        <td>ArrayList list = new ArrayList();</td>
        <td>子类引用指向自身对象</td>
    </tr>
</table>

&emsp; 父类引用指向子类对象时：  
1. 若子类覆盖了某方法，则父类引用调用子类重新定义的新方法；
2. 若子类未覆盖某方法，则父类引用调用父类本身的旧方法；
3. 若子类覆盖了某属性，但父类引用仍调用父类本身的旧属性。无论子类有没有覆盖父类属性，父类引用都会指向父类本身的属性；  
4. 若子类未覆盖某属性，则父类引用调用父类本身的旧属性；  
5. 父类引用不能访问子类新定义的方法；  
