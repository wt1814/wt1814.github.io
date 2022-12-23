
<!-- TOC -->

- [1. 装饰(Decorator)模式](#1-装饰decorator模式)
    - [1.1. 简介](#11-简介)
    - [1.2. 结构](#12-结构)
    - [1.3. 模式实现](#13-模式实现)
    - [1.4. 使用场景](#14-使用场景)
    - [1.5. JDK源码](#15-jdk源码)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; Decorator，装饰角色，一般是一个抽象类，继承自或实现Component（抽象构件），在它的属性里面有一个变量指向Component抽象构件，这是装饰器最关键的地方。  


# 1. 装饰(Decorator)模式  
<!--


详细讲解装饰者模式和继承之间的关系和优劣(讲道理这事儿，不怕过 就怕不够)
https://blog.csdn.net/qq_27093465/article/details/53323187
为什么装饰器模式能够取代继承？
https://juejin.cn/post/6979384113195122695

-------

https://blog.csdn.net/qq_27093465/article/details/53323187
https://mp.weixin.qq.com/s/8SD3gjoqv-yBlyXk7q0AEA

https://mp.weixin.qq.com/s?__biz=MzUzODU4MjE2MQ==&mid=2247484592&idx=1&sn=42a14aef3707745f570488be2fe77326&chksm=fad4c94ecda34058648b394acca654520e97f080aa156fb281c26eaad6f910c55e67b3063364&scene=21#wechat_redirect

https://www.cnblogs.com/volcano-liu/p/10897897.html

-->
## 1.1. 简介
&emsp; 装饰者模式(Decorator Pattern)是指在不改变原有对象的基础之上，将功能附加到对象上， **<font color = "clime">提供了比继承更有弹性的替代方案(扩展原有对象的功能)。</font>**  
&emsp; <font color = "red">装饰器模式的核心是功能拓展。使用装饰器模式可以透明且动态地扩展类的功能。</font>  

&emsp; **使用场景：**  
1. 用于扩展一个类的功能或给一个类添加附加职责。
2. 动态的给一个对象添加功能，这些功能可以再动态的撤销。
3. 需要为一批兄弟类进行改装或添加功能。  

&emsp; **在JDK中的应用，IO流**  
&emsp; java IO流是典型的装饰器模式。  
&emsp; FilterInputStream，FilterOutputStream，FilterRead，FilterWriter分别为具体装饰器的父类，相当于Decorator类，它们分别实现了InputStream，OutputStream，Reader，Writer类(这些类相当于Component，是其他组件类的父类，也是Decorator类的父类)。继承自InputStream，OutputStream，Reader，Writer这四个类的其他类是具体的组件类，每个都有相应的功能，相当于ConcreteComponent类。而继承自FilterInputStream，FilterOutputStream，FilterRead，FilterWriter这四个类的其他类就是具体的装饰器对象类，即ConcreteDecorator类。通过这些装饰器类，可以提供更加具体的有用的功能。如FileInputStream是InputStream的一个子类，从文件中读取数据流，BufferedInputStream是继承自FilterInputStream的具体的装饰器类，该类提供一个内存的缓冲区类保存输入流中的数据。使用如下的代码来使用BufferedInputStream装饰FileInputStream，就可以提供一个内存缓冲区来保存从文件中读取的输入流。  

&emsp; **优点：**  
1. **<font color = "red">`装饰器是继承的有力补充，比继承灵活，不改变原有对象的情况下动态地给一个对象扩展功能，即插即用。`</font>**  
2. 通过使用不同装饰类以及这些装饰类的排列组合，可以实现不同效果。  
3. 装饰器完全遵守开闭规则。  

&emsp; **缺点：**  
1. 会出现更多的代码，更多的类，增加程序复杂性。  
2. 动态装饰时，多层装饰时会更复杂。  

## 1.2. 结构  
<!-- 
https://www.cnblogs.com/volcano-liu/p/10897897.html
-->
&emsp; **模式角色组成：**  
![image](http://182.92.69.8:8081/img/java/design/design-11.png)  

* Component，抽象构件角色，是一个abstract class，组件对象的接口，可以给这些对象动态的添加职责。  
* ConcreteComponent，具体的处理类，用于实现operation方法。具体的组件对象，实现了组件接口。该对象通常就是被装饰器装饰的原始对象，可以给这个对象添加职责。  
* Decorator，装饰角色，所有装饰器的父类，是一个abstract class，需要定义一个与组件接口一致的接口(主要是为了实现装饰器功能的复用，即具体的装饰器A可以装饰另外一个具体的装饰器B，因为装饰器类也是一个Component)，并持有一个Component对象，该对象其实就是被装饰的对象。如果不继承组件接口类，则只能为某个组件添加单一的功能，即装饰器对象不能在装饰其他的装饰器对象。  
&emsp; 一般是一个抽象类，继承自或实现Component（抽象构件），在它的属性里面有一个变量指向Component抽象构件，这是装饰器最关键的地方。  
* ConcreteDecorator，具体的装饰器类，实现具体要向被装饰对象添加的功能。用来装饰具体的组件对象或者另外一个具体的装饰器对象。  

![image](http://182.92.69.8:8081/img/java/design/design-25.png)  
* Component: 抽象构件。是定义一个对象接口，可以给这些对象动态地添加职责。
* ConcreteComponent:具体构件。是定义了一个具体的对象，也可以给这个对象添加一些职责。
* Decorator: 抽象装饰类。是装饰抽象类，继承了Component,从外类来扩展Component类的功能，但对于Component来说，是无需知道Decorator存在的。
* ConcreteDecorator:具体装饰类，起到给Component添加职责的功能。

## 1.3. 模式实现
* Component：抽象构件, 被装饰抽象类

```java
public interface Cake {
    String nameDetail();
    Double price();
}
```

* ConcreteComponent: 抽象实现类，被装饰对象类

```java
public class CakeImpl implements Cake {
    /**
     * 手抓饼配料->鸡蛋，牛肉，蔬菜,
     * 组合方式：1.鸡蛋，2.牛肉，3.蔬菜，4. 鸡蛋+牛肉，5. 鸡蛋+蔬菜，6.牛肉＋蔬菜, 7.鸡蛋+牛肉+蔬菜
     */
    public String nameDetail() {
        return "原味手抓饼";
    }
    public Double price() {
        return 5d;
    }
}
```

* Decorator: 抽象装饰类

```java
public class CakeDecorator implements Cake{
    Cake cake;
    //装饰实现类
    public CakeDecorator(Cake cake) {
        this.cake = cake;
    }
    public String nameDetail() {
        return cake.nameDetail();
    }
    public Double price() {
        return cake.price();
    }
}
```

* ConcreteDecorator: Decorator具体实现类  
&emsp; 鸡蛋手抓饼 +1.5元

```java
public class EggCakeDecorator extends CakeDecorator{
    public EggCakeDecorator(Cake cake) {
        super(cake);
    }
    //关键部分
    @Override
    public String nameDetail() {
        return "鸡蛋，" + cake.nameDetail();
    }
    @Override
    public Double price() {
        return 1.5 + cake.price();
    }
}
```

&emsp; 牛肉手抓饼 +2元

```java
public class MeetCakeDecorator extends CakeDecorator{

    public MeetCakeDecorator(Cake cake) {
        super(cake);
    }

    //关键部分
    @Override
    public String nameDetail() {
        return "牛肉，" + cake.nameDetail();
    }
    @Override
    public Double price() {
        return 2 + cake.price();
    }
}
```

&emsp; 蔬菜手抓饼 +0.5元

```java
public class VeggCakeDecorator extends CakeDecorator{

    public VeggCakeDecorator(Cake cake) {
        super(cake);
    }

    //关键部分

    @Override
    public String nameDetail() {
        return "蔬菜，" + cake.nameDetail();
    }

    @Override
    public Double price() {
        return 0.5 + cake.price();
    }
}
```

* 测试类

```java
@Slf4j
public class Test {

    public static void main(String[] args) {
        Cake cake = new CakeImpl();
        log.info("小红想吃{}, 价格：￥{}", cake.nameDetail(), cake.price());

        CakeDecorator cakeEgg = new EggCakeDecorator(cake);
        log.info("小明想吃{}, 价格：￥{}", cakeEgg.nameDetail(), cakeEgg.price());

        CakeDecorator meetCake = new MeetCakeDecorator(cakeEgg);
        CakeDecorator veggCake = new VeggCakeDecorator(meetCake);
        log.info("小张想吃{}, 价格：￥{}", veggCake.nameDetail(), veggCake.price());
    }
}
```

* 输出结果  

```text
小红想吃原味手抓饼, 价格：￥5.0
小明想吃鸡蛋，原味手抓饼, 价格：￥6.5
小张想吃蔬菜，牛肉，鸡蛋，原味手抓饼, 价格：￥9.0
```

## 1.4. 使用场景
* 优点

    装饰者模式可以提供比继承更多的灵活性。
    可以通过一种动态的方式来扩展一个对象的功能，在运行时选择不同的装饰器，从而实现不同的行为。
    通过使用不同的具体装饰类以及这些装饰类的排列组合，可以创造出很多不同行为的组合。可以使用多个具体装饰类来装饰同一对象，得到功能更为强大的对象。
    具体构件类与具体装饰类可以独立变化，用户可以根据需要增加新的具体构件类和具体装饰类，在使用时再对其进行组合，原有代码无须改变，符合“开闭原则”。


* 缺点

    会产生很多的小对象，增加了系统的复杂性
    这种比继承更加灵活机动的特性，也同时意味着装饰模式比继承更加易于出错，排错也很困难，对于多次装饰的对象，调试时寻找错误可能需要逐级排查，较为烦琐。

* 使用场景

    在不影响其他对象的情况下，以动态、透明的方式给单个对象添加职责。
    需要动态地给一个对象增加功能，这些功能也可以动态地被撤销。当不能采用继承的方式对系统进行扩充或者采用继承不利于系统扩展和维护时。

## 1.5. JDK源码

* java.io.BufferedInputStream(InputStream)
* java.io.DataInputStream(InputStream)
* java.io.BufferedOutputStream(OutputStream)


&emsp; 源码解析

```java
//装饰器抽象类 java.io.FilterInputStream
public class FilterInputStream extends InputStream{
  protected FilterInputStream(InputStream in) {
        this.in = in;
    }
}
```

```java
//java.io.BufferedInputStream(InputStream) 装饰类
public class BufferedInputStream extends FilterInputStream {
  public BufferedInputStream(InputStream in) {
          this(in, DEFAULT_BUFFER_SIZE);
      }
}
```

```java
public class Test{
  public static void main(String[] args){
    String file = "filename.txt";    
      InputStream is = new FileInputStream(file);
       BufferedInputStream bis = new BufferedInputStream(is);
  }
}
```
