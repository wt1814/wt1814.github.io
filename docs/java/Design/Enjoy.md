
<!-- TOC -->

- [1. 享元模式(池化技术)](#1-享元模式池化技术)
    - [1.1. 简介](#11-简介)
    - [1.2. 结构](#12-结构)
    - [1.3. String常量池](#13-string常量池)

<!-- /TOC -->


# 1. 享元模式(池化技术)
<!-- 
https://www.cnblogs.com/kiqi/p/14054891.html
-->

## 1.1. 简介
&emsp; 享元模式（Flyweight Pattern）主要用于减少创建对象的数量，以减少内存占用和提高性能。这种类型的设计模式属于结构型模式，它提供了减少对象数量从而改善应用所需的对象结构的方式。  

&emsp; 享元模式：①将对象的公共部分抽取出来成为内部状态(实现共享)，②将随时间改变、不可共享的部分作为外部状态(通过更换外部状态实现对象复用)，从而减少创建对象的数量，以减少内存开销和提高性能。  
&emsp; 核心：共享和复用，共享(内部状态 - intrinsicState)，复用(外部状态 - extrinsicState)  


&emsp; 使用场景：  

* 共享：当只存在内部状态时，可以在多线程中共享使用(String常量池)  
* 复用：通过改变外部状态，可以更好实现对象的复用(线程池)  

&emsp; PS：外部状态在线程间需考虑并发问题，因此不适合共享，但当对象被使用完成后，通过修改外部状态，使其可以复用于下一次的访问需求。  

## 1.2. 结构
&emsp; 角色：

* FlyweightFactory(享元工厂)：多和单例模式一起使用，维护一个享元池。
* Flyweight(享元对象)：作为共享和复用的对象，其存在内部状态和外部状态。

```java
// 享元工厂，维护一个享元池，以及添加和获取享元对象
public class FlyweightFactory {
    private static Map<String, IFlyweight> pool = new HashMap<>();

    public static IFlyweight getFlyweight(String key){
        if(pool.containsKey(key)){
            return pool.get(key);
        } else {
            IFlyweight result = new ConcreteFlyweight(key);
            pool.put(key, result);
            return result;
        }
    }
}
```

```java
public interface IFlyweight {
    void operation();
    void setExtrinsicState(String extrinsicState);
}
```

```java
public class ConcreteFlyweight implements IFlyweight{
    // 内部状态，对象创建之后即不再改变，特性：共享
    private String intrinsicState;

    // 外部状态，在运行过程中可以被改变的状态(与请求相关的变量)，功能：复用
    private String extrinsicState;

    // 内部状态由构造方法进行设置，只在创建时设置，功能：共享
    public ConcreteFlyweight(String intrinsicState) {
        this.intrinsicState = intrinsicState;
    }

    // 外部状态通过方法传参，在生命周期内可以被多次修改(一般每次被获取时都会修改外部状态)
    public void setExtrinsicState(String extrinsicState) {
        this.extrinsicState = extrinsicState;
    }

    @Override
    public void operation() {
        System.out.println("Object address：" + System.identityHashCode(this));
        System.out.println("intrinsic State：" + this.intrinsicState);
        System.out.println("extrinsic State：" + this.extrinsicState);
    }
}
```

## 1.3. String常量池
&emsp; JDK String常量池优化：在编译过程中出现的字符串引用均指向常量池，在运行阶段创建的字符串则重新在堆中创建一个字符串对象  
&emsp; JDK Integer和Long，均有常量池，为[-128,127]，short和char也有  

```java
public static void stringTest() {
    String s1 = "hello";
    // s1 == s2 true - 均从String常量池中获取到常量引用
    String s2 = "hello";

    // s1 == s3 true - 编译时优化，常量之间将发生合并
    String s3 = "he" + "llo";

    // s1 == s4 false - 在运行时才能触发new指令创建对象，
    String s4 = "hel" + new String("lo");

    // s1 == s5 false - 在运行时会触发new指令创建对象，此时，常量池中存在一个String对象，在堆中又创建一个新的String对象
    // s4 == s5 false - 运行时创建的两个对象，引用不相同。
    String s5 = new String("hello");

    // s1 == s6 true - intern：从常量池中获取对应字符串引用
    String s6 = s5.intern();

    String s7 = "h";
    String s8 = "ello";

    // s1 == s9 false - 只要字符串链接过程中存在变量，则必定是运行时创建
    String s9 = s7 + s8;

    // a == b true
    Integer a = Integer.valueOf(100);
    Integer b = 100;

    // c == d false
    Integer c = Integer.valueOf(1000);
    Integer d = 1000;
}
```
