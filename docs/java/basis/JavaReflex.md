

<!-- TOC -->

- [1. ~~反射~~](#1-反射)
    - [1.1. 反射简介](#11-反射简介)
    - [1.2. 应用场景](#12-应用场景)
    - [1.3. 使用示例](#13-使用示例)
    - [1.4. 反射API](#14-反射api)
        - [1.4.1. 获取Class对象](#141-获取class对象)
        - [1.4.2. 获取构造方法并使用](#142-获取构造方法并使用)
        - [1.4.3. 获取成员变量并调用](#143-获取成员变量并调用)
        - [1.4.4. 获取成员方法并调用](#144-获取成员方法并调用)
    - [1.5. ~~反射的实现原理~~](#15-反射的实现原理)

<!-- /TOC -->

# 1. ~~反射~~
<!-- 
面试官：什么是Java反射？它的应用场景有哪些？ 
https://mp.weixin.qq.com/s/TqSLUWYWfhHjpfI_srETJg

-->

## 1.1. 反射简介  
&emsp; 反射是在运行状态能够动态的获取该类的属性和方法，并且能够任意的使用该类的属性和方法，这种动态获取类信息以及动态的调用对象的方法的功能就是反射。  

1. **动态加载类：**  
&emsp; 编译时加载类是静态加载类，new 创建对象是静态加载类，在编译时刻就需要加载所有可用使用到的类，如果有一个用不了，那么整个文件都无法通过编译。  
&emsp; 运行时加载类是动态加载类。Class c =  Class.forName("类的全名")，不仅表示了类的类型，还表示了动态加载类，编译不会报错，在运行时才会加载，使用接口标准能更方便动态加载类的实现。功能性的类尽量使用动态加载，而不用静态加载。  
2. `破坏：可以访问任意一个对象的任意一个方法和属性，包括获取、修改私有属性。`  

&emsp; 反射机制优缺点：  

* 优点：运行期类型的判断，动态加载类，提高代码灵活度。  
* 缺点：性能瓶颈，反射相当于一系列解释操作，通知JVM要做的事情，性能比直接的java代码要慢很多。  

## 1.2. 应用场景  
<!-- https://mp.weixin.qq.com/s/_2VVj3AN-mAuguUIk9-8xg -->
&emsp; **<font color = "clime">平常开发中使用反射的实际场景有：动态代理、JDBC中的加载数据库驱动程序、Spring框架中加载bean对象。</font>**  
&emsp; **<font color = "clime">使用反射机制能够大大的增强程序的扩展性。程序执行时，(根据配置文件等条件)要动态加载某个类并执行方法。相当于switch判断。</font>**  

&emsp; 示例：工程有一个配置文件，配置文件里有个renderType设置了实现类调用哪个RenderHandler来完成动作。(RenderHandler：一个接口，有一个render方法，HighRenderHandler、LowRenderHandler、MiddleRenderHandler等一共10个实现类)。  

&emsp; 一种是使用switch判断：  

```java
//首先获得配置文件里的renderType
switch(renderType){
    case "low":renderHandler=new LowRenderHandler();
    case "high":renderHandler=new HighRenderHandler();
    case "middle":renderHandler=new MiddleRenderHandler();
    case "a":renderHandler=new ARenderHandler();
    case "b":renderHandler=new BRenderHandler();
    case "c":renderHandler=new CRenderHandler();
    default:renderHandler=new LowRenderHandler();
}
renderHandler.render();
```
&emsp; 另一种是使用反射：  

```java
//首先从配置文件里获得renderType
RendeHandler renderHandler=(RendeHandler)Class.forName(rederType).newInstance();
renderHandler.render();
```

## 1.3. 使用示例
&emsp; 反射的基本步骤：  
1. 首先获得Class对象；  
2. 然后实例化对象，获得类的属性、方法或者构造函数；  
3. 最后访问属性、调用方法、调用构造函数创建对象。  

```java
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class T1 {
    public static void main(String[] args) {
        try {
            //创建类
            Class<?> class1 = Class.forName("com.app.Util");
            //属性
            //获取nameField属性
            Field nameField = class1.getDeclaredField( "name" );
            //获取nameField的值
            String name_ = (String) nameField.get( nameField );
            System.out.println( name_ );
            //方法
            //没有返回值，没有参数
            Method getTipMethod1 = class1.getDeclaredMethod("getTips") ;
            getTipMethod1.invoke( null);
            //有返回值，没有参数
            Method getTipMethod2 = class1.getDeclaredMethod("getTip") ;
            String result_2 = (String)getTipMethod2.invoke(null);
            System.out.println( "返回值："+ result_2 );
            //没有返回值，有参数
            Method getTipMethod3 = class1.getDeclaredMethod( "getTip" , String.class  ) ;
            String result_3 = (String) getTipMethod3.invoke( null , "第三个方法"  ) ;
            System.out.println( "返回值： "+ result_3 );
            //有返回值，有参数
            Method getTipMethod4 = class1.getDeclaredMethod( "getTip" , int.class ) ;
            String result_4 = (String) getTipMethod4.invoke( null  , 1 ) ;
            System.out.println( "返回值： "+ result_4 );

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        //……
    }
}
```

## 1.4. 反射API  
&emsp; java反射机制的基本类：Field、Constractor、Method、Class、Modifier，依次分别封装了反射的属性、构造函数、方法、Class、访问权限。  

<!--实现Java反射机制的类都位于java.lang.reflect包中：
Class类：代表一个类 
Field类：代表类的成员变量(类的属性) 
Method类：代表类的方法 
Constructor类：代表类的构造方法 
Array类：提供了动态创建数组，以及访问数组的元素的静态方法 
一句话概括就是使用反射可以赋予jvm动态编译的能力，否则类的元数据信息只能用静态编译的方式实现，例如热加载，Tomcat的classloader等等都没法支持。 -->

### 1.4.1. 获取Class对象  
......

### 1.4.2. 获取构造方法并使用  
......

### 1.4.3. 获取成员变量并调用  
......

### 1.4.4. 获取成员方法并调用  
......


## 1.5. ~~反射的实现原理~~
<!-- 
Java反射机制原理探究
https://zhuanlan.zhihu.com/p/162971344

Java反射原理
https://cloud.tencent.com/developer/article/1695077?from=information.detail.%E5%8F%8D%E5%B0%84%E5%8E%9F%E7%90%86
-->
&emsp; Class类与java.lang.reflect库一起对反射的概念提供了技术支持。java.lang.reflect类库包含了Field类，Method类以及Constructor类。这些类用来表示未知类里对应的成员。Class类提供了获取getFields()、getMethods()和getConstructors()等方法，而这些方法的返回值类型就定义在java.lang.reflect当中。  

&emsp; 如果不知道某个对象的确切类型(即list引用到底是ArrayList类型还是LinkedList类型)，RTTI可以告诉你，但是有一个前提：这个类型在编译时必须已知，这样才能使用RTTI来识别它。  

&emsp; 要想理解反射的原理，必须要结合类加载机。反射机制并没有什么神奇之处，当通过反射与一个未知类型的对象打交道时，JVM只是简单地检查这个对象，看它属于哪个特定的类，然后再通过拿到的某一个类的全限定名去找这个类的Class文件 。因此，那个类的.class对于JVM来说必须是可获取的，要么在本地机器上，要么从网络获取。所以对于RTTI和反射之间的真正区别只在于:  

    RTTI，编译器在编译时打开和检查.class文件
    反射，运行时打开和检查.class文件

&emsp; 对于反射机制而言.class文件在编译时是不可获取的，所以是在运行时获取和检查.class文件。  

&emsp; 总结起来说就是，反射是通过Class类和java.lang.reflect类库一起支持而实现的，其中每一个Class类的对象都对应了一个类，这些信息在编译时期就已经被存在了.class文件里面了，Class 对象是在加载类时由 Java 虚拟机以及通过调用类加载器中的defineClass方法自动构造的。对于我们定义的每一个类，在虚拟机中都有一个应的Class对象。  

&emsp; 那么在运行时期，无论是通过字面量还是forName方法获取Class对象，都是去根据这个类的全限定名(全限定名必须是唯一的，这也间接回答了为什么类名不能重复这个问题。)然后获取对应的Class对象。  

&emsp; 总结: java虚拟机帮我们生成了类的class对象,而通过类的全限定名，我们可以去获取这个类的字节码.class文件，然后再获取这个类对应的class对象，再通过class对象提供的方法结合类Method,Filed,Constructor，就能获取到这个类的所有相关信息. 获取到这些信息之后，就可以使用Constructor创建对象，用get和set方法读取和修改与Field对象相关的字段，用invoke方法调用与Method对象关联的方法。  

------------

&emsp; 调用反射的总体流程如下：  

* 准备阶段：编译期装载所有的类，将每个类的元信息保存至Class类对象中，每一个类对应一个Class对象  
* 获取Class对象：调用x.class/x.getClass()/Class.forName() 获取x的Class对象clz（这些方法的底层都是native方法，是在JVM底层编写好的，涉及到了JVM底层，就先不进行探究了）  
* 进行实际反射操作：通过clz对象获取Field/Method/Constructor对象进行进一步操作
