
<!-- TOC -->

- [1. 单例模式](#1-单例模式)
    - [1.1. 简介](#11-简介)
    - [1.2. 设计安全的单例模式](#12-设计安全的单例模式)
        - [1.2.1. 静态类使⽤](#121-静态类使⽤)
            - [1.2.1.1. ★★★单例模式与静态类](#1211-★★★单例模式与静态类)
        - [1.2.2. 懒汉式单例(非线程安全)](#122-懒汉式单例非线程安全)
        - [1.2.3. ★★★双重校验锁的形式](#123-★★★双重校验锁的形式)
        - [1.2.4. 静态内部类法](#124-静态内部类法)
        - [1.2.5. 枚举方法](#125-枚举方法)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 单例模式与static静态类：静态使用于一些非状态Bean，单例使用于状态Bean。  
2. 单例模式适用场景。  


# 1. 单例模式
<!-- 
设计模式回答
https://mp.weixin.qq.com/s/_aQuVoD3tVNB3aTEFDky7g

单例模式的优缺点和使用场景
https://www.cnblogs.com/damsoft/p/6105122.html

-->

## 1.1. 简介  

&emsp; 因为在编程开发中经常会遇到这样⼀种场景，那就是需要保证⼀个类只有⼀个实例哪怕多线程同时访问，并需要提供⼀个全局访问此实例的点。  

&emsp; **定义：** 单例模式(Singleton)，保证一个类仅有一个实例，<font color = "red">并提供一个访问它的全局访问点，并且自行实例化向整个系统提供。单例模式减少了内存开销；可以避免对资源的多重占用。</font>  
&emsp; **适用场景：** 一些资源管理器常常设计成单例模式。  
1. 需要管理的资源包括软件外部资源。例如每台计算机可以有若干个打印机。  
2. 需要管理的资源包括软件内部资源。例如大多数的软件都有一个属性(properties)文件存放系统配置，这样的系统应当由一个对象来管理一个属性文件。软件内部资源也包括例如负责记录网站来访人数的部件，记录软件系统内部事件、出错信息的部件，或是对系统的表现进行检查的部件等。这些部件都必须集中管理。  

------------

&emsp; 本章节的技术所出现的场景⾮常简单也是我们⽇常开发所能⻅到的，例如；  
1. 数据库的连接池不会反复创建  
2. spring中⼀个单例模式bean的⽣成和使⽤  
3. 在我们平常的代码中需要设置全局的的⼀些属性保存  
&emsp; 在我们的⽇常开发中⼤致上会出现如上这些场景中使⽤到单例模式，虽然单例模式并不复杂但是使⽤⾯却⽐较⼴。  

&emsp; **模式角色组成，** 从具体实现角度来说，就是以下三点：  
1. 单例模式的类定义中含有一个该类的静态私有对象(在本类中建一个实例)。  
2. 单例模式的类只提供私有的构造函数，外界无法通过构造器来创建对象。  
3. 单例模式的类提供了一个静态的公有的函数用于创建或获取它本身的静态私有对象(在类初始化时，已经自行实例化)。

## 1.2. 设计安全的单例模式  

### 1.2.1. 静态类使⽤  
```java
public class Singleton_00 {
    public static Map<String,String> cache = new ConcurrentHashMap<String,String>();
    
}
```

&emsp; 以上这种⽅式在我们平常的业务开发中⾮常场常⻅，这样静态类的⽅式可以在第⼀次运⾏的时候直接初始化Map类，同时这⾥我们也不需要到延迟加载在使⽤。  
&emsp; 在不需要维持任何状态下，仅仅⽤于全局访问，这个使⽤使⽤静态类的⽅式更加⽅便。  
&emsp; 但如果需要被继承以及需要维持⼀些特定状态的情况下，就适合使⽤单例模式。  

#### 1.2.1.1. ★★★单例模式与静态类  
<!-- 
单例模式和静态类的区别对比
https://blog.csdn.net/baidu_41878679/article/details/82823145

-->
&emsp; 1）首先单例模式会提供给你一个全局唯一的对象，静态类只是提供给你很多静态方法，这些方法不用创建对象，通过类就可以直接调用；  
&emsp; 2）如果是一个非常重的对象，单例模式可以懒加载，静态类就无法做到；  
&emsp; 那么时候时候应该用静态类，什么时候应该用单例模式呢？首先如果你只是想使用一些工具方法，那么最好用静态类，静态类比单例类更快，因为静态的绑定是在编译期进行的。如果你要维护状态信息，或者访问资源时，应该选用单例模式。还可以这样说，当你需要面向对象的能力时（比如继承、多态）时，选用单例类，当你仅仅是提供一些方法时选用静态类。  

### 1.2.2. 懒汉式单例(非线程安全)  

```java
//懒汉式单例
//在外部需要使用的时候才进行实例化
public class LazySimpleSingleton {
    private LazySimpleSingleton(){}
    //静态块， 公共内存区域
    private static LazySimpleSingleton lazy = null;
    public static LazySimpleSingleton getInstance(){
        if(lazy == null){
            lazy = new LazySimpleSingleton();
        } r
        eturn lazy;
    }
}
```
&emsp; 懒汉式也是通过一个类的静态变量实现的。但是并没有直接初始化。而是在函数getInstance()中实例化的，也就是每次想用这个实例的时候初始化的；如果已经初始化了，那么就不用初始化了。  
&emsp; 但是懒汉式，其实也有一个小缺点，就是第一次使用的时候，需要进行初始化操作，可能会有比较高的耗时。如果是已知某一个对象一定会使用到的话，其实可以采用一种饿汉的实现方式。  

### 1.2.3. ★★★双重校验锁的形式   
&emsp; DCL详解参考[Volatile](/docs/java/concurrent/Volatile.md)  

```java
public class LazyDoubleCheckSingleton {
    private volatile static LazyDoubleCheckSingleton lazy = null;

    private LazyDoubleCheckSingleton(){
        
    }
    
    public static LazyDoubleCheckSingleton getInstance(){
        // 第一重检测
        if(lazy == null){
            // 锁定代码块
            synchronized (LazyDoubleCheckSingleton.class){
                // 第二重检测
                if(lazy == null){
                    // 实例化对象
                    lazy = new LazyDoubleCheckSingleton();
                    //1.分配内存给这个对象
                    //2.初始化对象
                    //3.设置 lazy 指向刚分配的内存地址
                }
            }
        } 
        return lazy;
    }
}
```
&emsp; <font color = "red">只有在singleton == null的情况下再进行加锁创建对象，如果singleton!=null，就直接返回就行了，并没有进行并发控制。大大的提升了效率。</font>   
&emsp; <font color = "clime">从上面的代码中可以看到，其实整个过程中进行了两次singleton == null的判断，所以这种方法被称之为"双重校验锁"。</font>   
&emsp; <font color = "clime">还有值得注意的是，双重校验锁的实现方式中，静态成员变量singleton必须通过volatile来修饰，保证其初始化不被重排，否则可能被引用到一个未初始化完成的对象。</font>   

&emsp; **<font color = "clime">详见[Volatile](/docs/java/concurrent/Volatile.md)中双重校验锁的解释。</font>**  

### 1.2.4. 静态内部类法  

```java
//这种形式兼顾饿汉式的内存浪费， 也兼顾synchronized性能问题
//完美地屏蔽了这两个缺点
public class LazyInnerClassSingleton {
    //默认使用 LazyInnerClassGeneral 的时候， 会先初始化内部类
    //如果没使用的话， 内部类是不加载的
    private LazyInnerClassSingleton(){

    }
    //每一个关键字都不是多余的
    //static 是为了使单例的空间共享
    //保证这个方法不会被重写， 重载
    public static final LazyInnerClassSingleton getInstance(){
    //在返回结果以前， 一定会先加载内部类
        return LazyHolder.LAZY;
    }  
    //默认不加载
    private static class LazyHolder{
        private static final LazyInnerClassSingleton LAZY = new LazyInnerClassSingleton();
    }
}
```  
&emsp; 使用内部类的好处是，静态内部类不会在单例加载时就加载，而是在调用getInstance()方法时才进行加载，达到了类似懒汉模式的效果，而这种方法又是线程安全的。  

### 1.2.5. 枚举方法  
&emsp; Java中的枚举单例模式是使用枚举在Java中实现单例模式。解决了以下三个问题：1. 自由串行化。2. 保证只有一个实例。3. 线程安全。  

```java
public enum EnumSingleton {
    INSTANCE;
    private Object data;
    public Object getData() {
        return data;
    } 
    public void setData(Object data) {
        this.data = data;
    } 
    public static EnumSingleton getInstance(){
        return INSTANCE;
    }
}
```
&emsp; 如果想调用它的方法时，仅需要以下操作：  

```java
public class test {
    public static void main(String[] args){
        EnumSingleton.INSTANCE.getInstance();
    }
}
```