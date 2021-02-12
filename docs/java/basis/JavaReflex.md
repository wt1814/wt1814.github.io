

<!-- TOC -->

- [1. 反射](#1-反射)
    - [1.1. 反射简介](#11-反射简介)
    - [1.2. 应用场景](#12-应用场景)
    - [1.3. 使用示例](#13-使用示例)
    - [1.4. 反射API](#14-反射api)
        - [1.4.1. 获取Class对象](#141-获取class对象)
        - [1.4.2. 获取构造方法并使用](#142-获取构造方法并使用)
        - [1.4.3. 获取成员变量并调用](#143-获取成员变量并调用)
        - [1.4.4. 获取成员方法并调用](#144-获取成员方法并调用)

<!-- /TOC -->

# 1. 反射
## 1.1. 反射简介  
&emsp; 反射是在运行状态能够动态的获取该类的属性和方法，并且能够任意的使用该类的属性和方法，这种动态获取类信息以及动态的调用对象的方法的功能就是反射。  

1. **动态加载类：**  
&emsp; 编译时加载类是静态加载类，new 创建对象是静态加载类，在编译时刻就需要加载所有可用使用到的类，如果有一个用不了，那么整个文件都无法通过编译。  
&emsp; 运行时加载类是动态加载类。Class c =  Class.forName("类的全名")，不仅表示了类的类型，还表示了动态加载类，编译不会报错，在运行时才会加载，使用接口标准能更方便动态加载类的实现。功能性的类尽量使用动态加载，而不用静态加载。  
2. 可以访问任意一个对象的任意一个方法和属性，包括获取、修改私有属性。  

&emsp; 反射机制优缺点：  

* 优点：运行期类型的判断，动态加载类，提高代码灵活度。  
* 缺点：性能瓶颈，反射相当于一系列解释操作，通知JVM要做的事情，性能比直接的java代码要慢很多。  

## 1.2. 应用场景  

&emsp; **平常开发中使用反射的实际场景有：动态代理、JDBC中的加载数据库驱动程序、Spring框架中加载bean对象。** 
<!-- https://mp.weixin.qq.com/s/_2VVj3AN-mAuguUIk9-8xg -->

&emsp; 使用反射机制能够大大的增强程序的扩展性。程序执行时，(根据配置文件等条件)要动态加载某个类并执行方法。相当于switch判断。  

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

