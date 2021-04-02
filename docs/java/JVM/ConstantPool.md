
<!-- TOC -->

- [1. 常量池](#1-常量池)
    - [1.1. 常量池](#11-常量池)
        - [1.1.1. 前言：字面量、常量、变量](#111-前言字面量常量变量)
        - [1.1.2. 常量池](#112-常量池)
    - [1.2. 包装类对象池技术](#12-包装类对象池技术)
    - [1.3. 小结](#13-小结)

<!-- /TOC -->

# 1. 常量池
<!--
 终于搞懂了Java8的内存结构，再也不纠结方法区和常量池了！ 
 https://mp.weixin.qq.com/s/WvdPQ8JsR9qqWMlvX7ockA
JAVA常量池，一篇文章就足够入门了。（含图解）
https://blog.csdn.net/qq_41376740/article/details/80338158
常量池 
https://baike.baidu.com/item/%E5%B8%B8%E9%87%8F%E6%B1%A0/3855836?fr=aladdin
Java中的几种常量池
https://blog.csdn.net/luzhensmart/article/details/86565496
常量池详解
https://zhuanlan.zhihu.com/p/64839455
JVM常量池浅析
https://www.jianshu.com/p/cf78e68e3a99
-->

## 1.1. 常量池  


### 1.1.1. 前言：字面量、常量、变量  
<!-- 
https://blog.csdn.net/luzhensmart/article/details/86565496
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-132.png)  

### 1.1.2. 常量池
<!-- 
已经明确的一点是 字符串常量池、静态变量 在jdk7时从方法区移入**Java堆**中，那么运行时常量池呢？

https://www.pianshen.com/article/72411279197/
-->

&emsp; 常量池分为以下三种：class文件常量池、运行时常量池、全局字符串常量池。  
1. class文件常量池(Class Constant Pool)  
&emsp; class常量池是在编译的时候每个class都有的，在编译阶段，存放的是常量(文本字符串、final常量等)和符号引用。  
2. 运行时常量池(Runtime Constant Pool)  
&emsp; 运行时常量池是在类加载完成之后， **将每个class常量池中的符号引用值转存到运行时常量池中** ，也就是说，每个class都有一个运行时常量池，类在解析之后，将符号引用替换成直接引用，与全局常量池中的引用值保持一致。  
3. 全局字符串常量池(String Pool)  
&emsp; 全局字符串常量池中存放的内容是在类加载完成后存到String Pool中的，在每个VM中只有一份，存放的是字符串常量的引用值(在堆中生成字符串对象实例)。  

## 1.2. 包装类对象池技术
&emsp; 包装类的对象池(也有称常量池)和JVM的静态/运行时常量池没有任何关系。静态/运行时常量池有点类似于符号表的概念，与对象池相差甚远。  
&emsp; 包装类的对象池是池化技术的应用，并非是虚拟机层面的东西，而是 Java 在类封装里实现的。打开 Integer 的源代码，找到 cache 相关的内容：  

```java
/**
    * Cache to support the object identity semantics of autoboxing for values between
    * -128 and 127 (inclusive) as required by JLS.
    * <p>
    * The cache is initialized on first usage. The size of the cache
    * may be controlled by the {@code -XX:AutoBoxCacheMax=<size>} option.
    * During VM initialization, java.lang.Integer.IntegerCache.high property
    * may be set and saved in the private system properties in the
    * sun.misc.VM class.
    */

private static class IntegerCache {
    static final int low = -128;
    static final int high;
    static final Integer cache[];

    static {
// high value may be configured by property
        int h = 127;
        String integerCacheHighPropValue =
                sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
        if (integerCacheHighPropValue != null) {
            try {
                int i = parseInt(integerCacheHighPropValue);
                i = Math.max(i, 127);
// Maximum array size is Integer.MAX_VALUE
                h = Math.min(i, Integer.MAX_VALUE - (-low) - 1);
            } catch (NumberFormatException nfe) {
// If the property cannot be parsed into an int, ignore it.
            }
        }
        high = h;

        cache = new Integer[(high - low) + 1];
        int j = low;
        for (int k = 0; k < cache.length; k++)
            cache[k] = new Integer(j++);

// range [-128, 127] must be interned (JLS7 5.1.7)
        assert IntegerCache.high >= 127;
    }

    private IntegerCache() {
    }
}

/**
    * Returns an {@code Integer} instance representing the specified
    * {@code int} value. If a new {@code Integer} instance is not
    * required, this method should generally be used in preference to
    * the constructor {@link #Integer(int)}, as this method is likely
    * to yield significantly better space and time performance by
    * caching frequently requested values.
    * <p>
    * This method will always cache values in the range -128 to 127,
    * inclusive, and may cache other values outside of this range.
    *
    * @param i an {@code int} value.
    * @return an {@code Integer} instance representing {@code i}.
    * @since 1.5
    */
public static Integer valueOf(int i) {
    if (i >= IntegerCache.low && i <= IntegerCache.high)
        return IntegerCache.cache[i + (-IntegerCache.low)];
    return new Integer(i);
}
```

&emsp; IntegerCache 是 Integer 在内部维护的一个静态内部类，用于对象缓存。通过源码知道，Integer 对象池在底层实际上就是一个变量名为 cache 的数组，里面包含了 -128 ～ 127 的 Integer 对象实例。  
&emsp; 使用对象池的方法就是通过 Integer.valueOf() 返回 cache 中的对象，像 Integer i = 10 这种自动装箱实际上也是调用 Integer.valueOf() 完成的。  
&emsp; 如果使用的是 new 构造器，则会跳过 valueOf()，所以不会使用对象池中的实例。  

```java
Integer i1 = 10;
Integer i2 = 10;
Integer i3 = new Integer(10);
Integer i4 = new Integer(10);
Integer i5 = Integer.valueOf(10);
 
System.out.println(i1 == i2); // true
System.out.println(i2 == i3); // false
System.out.println(i3 == i4); // false
System.out.println(i1 == i5); // true
```

&emsp; 注意到注释中的一句话 “The cache is initialized on first usage”，缓存池的初始化在第一次使用的时候已经全部完成，这涉及到设计模式的一些应用。这和常量池中字面量的保存有很大区别，Integer 不需要显示地出现在代码中才添加到池中，初始化时它已经包含了所有需要缓存的对象。  

## 1.3. 小结  


